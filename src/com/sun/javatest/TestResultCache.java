/*
 * $Id$
 *
 * Copyright (c) 2001, 2010, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.sun.javatest;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.sun.javatest.util.Debug;
import com.sun.javatest.util.Fifo;
import com.sun.javatest.util.I18NResourceBundle;
import java.util.logging.Logger;

/**
 * Class which maintains a cache of the results currently existing inside a
 * work directory.  It is designed to allow the harness to get general
 * information (name, status) about tests without having to open all the
 * result files.
 */
public class TestResultCache {

    /**
     * Interface which allows an external class to monitor the cache's
     * activities.
     */
    public static interface Observer {
        /**
         * Called when tests have been read from the cache file.
         * @param tests the tests that have been read
         */
        void update(Map tests);

        /**
         * Called periodically while waiting to access the cache.
         * @param timeSoFar the time so far that a client has been waiting to
         * access the cache
         */
        void waitingForLock(long timeSoFar);

        /**
         * Called when the timed out waiting for access to the cache.
         */
        void timeoutWaitingForLock();

        /**
         * Called when the cache has been locked.
         */
        void acquiredLock();

        /**
         * Called when the lock on the cache has been released.
         */
        void releasedLock();

        /**
         * Called when starting to (re)build the cache.
         * @param reset currently, always true
         */
        void buildingCache(boolean reset);

        /**
         * Called when a test has been found to put in the cache.
         * @param tr the test that is being put in the cache
         */
        void buildingCache(TestResult tr);

        /**
         * Called when the cache has been (re)built.
         */
        void builtCache();

        /**
         * Called when a serious error has occurred and the cache is unable to continue.
         * @param t an object identifying the error that occurred
         */
        void error(Throwable t);
    };

    /**
     * Primary constructor for a cache.
     * @param workDir the work directory to attach to
     * @param observer the observer to notify of cache events
     * @throws IOException if an error occurs reading the cache file
     */
    public TestResultCache(WorkDirectory workDir, Observer observer)
                            throws IOException {
        this.workDir = workDir;
        this.observer = observer;

        weakWorkDir = new WeakReference(workDir);
        weakObserver = new WeakReference(observer);

        cacheFile = workDir.getSystemFile(V2_FILENAME);
        lockFile = workDir.getSystemFile(V2_LOCKNAME);

        File old = workDir.getSystemFile(V1_FILENAME);
        if (old.exists()) {
            workDir.log(i18n, "trc.rmCachev1", old.getAbsolutePath());
            old.delete();
        }

        old = workDir.getSystemFile(V1_LOCKNAME);
        if (old.exists()) {
            workDir.log(i18n, "trc.rmLockv1", old.getAbsolutePath());
            old.delete();
        }

        raf = new RandomAccessFile(cacheFile, "rw");

        worker = new Thread() {
                public void run() {
                    doWorkUntilDone();
                }
            };
        worker.setName("TestResultCache.worker" + (workerNumber++) + "[" + workDir.getRoot() + "]");
        worker.setDaemon(true); // allows thread to run during shutdown

        // ensure priority is lower than that of the calling thread, but don't
        // want priority too low because we want cache updated in a timely fashion
        int prio = Math.max(0, Math.min(Thread.currentThread().getPriority() - 1, 3));
        worker.setPriority(prio);

        worker.start();

        shutdownHandler = new Thread() {
                public void run() {
                    shutdown();
                }
            };
        Runtime.getRuntime().addShutdownHook(shutdownHandler);
    }

    /**
     * Insert a test result into the cache.
     * The cache file will be updated asynchronously.
     * @param tr the test to be inserted.
     **/
    synchronized void insert(TestResult tr) {
        if (DEBUG_TESTS)
            Debug.println("TRC.insert " + tr.getWorkRelativePath() + " " + tr.getStatus());

        reviveWeakReferences();
        testsToWrite.insert(tr);
        notifyAll();
    }

    /**
     * Request an update via the observer of all the tests in the cache.
     */
    synchronized void requestFullUpdate() {
        if (!shutdownRequested) {
            reviveWeakReferences();
            fullUpdateRequested = true;
            notifyAll();
        }
    }

    /**
     * Check if a call to compress the cache is advised.
     */
    synchronized boolean needsCompress() {
        return compressNeeded;
    }

    /**
     * Compress the cache file, eliminating any obsolete entries.
     * The cache file will be updated asynchronously.
     */
    synchronized void compress() {
        if (!shutdownRequested) {
            reviveWeakReferences();
            compressRequested = true;
            notifyAll();
        }
    }

    /**
     * Expedite any outstanding updates to the cache file.
     * The cache file will be updated asynchronously.
     */
    synchronized void flush() {
        reviveWeakReferences();
        flushRequested = true;
        notifyAll();
    }

    boolean workerAlive() {
        return worker != null;
    }

    //-------------------------------------------------------------------------------------
    //
    // the main worker code

    private static final int DEFAULT_COMPRESS_PERCENT_LEVEL = 40;

    private void doWorkUntilDone() {
        int compressPercentLevel =
            Integer.getInteger("javatest.trc.cacheThreshold",
                               DEFAULT_COMPRESS_PERCENT_LEVEL).intValue();
        compressPercentLevel = Math.max(1, Math.min(compressPercentLevel, 100));

        boolean haveWork = true;  // first time in, we need to read/build the cache
        fullUpdateRequested = true; // first time in, update client when initialized

        try {
            while (haveWork) {
                doWork();

                synchronized (this) {
                    if (flushRequested && testsToWrite.size() == 0)
                        flushRequested = false;

                    long timeLastWork = System.currentTimeMillis();
                    haveWork = isWorkAvailable(timeLastWork);
                    while (!haveWork && !shutdownRequested) {
                        workDir = null;
                        observer = null;

                        if (DEBUG_SYNC)
                            Debug.println("TRC.worker waiting");

                        wait(MIN_TEST_READ_INTERVAL);
                        haveWork = isWorkAvailable(timeLastWork);

                        if (DEBUG_SYNC)
                            Debug.println("TRC.worker awake haveWork=" + haveWork);
                    }

                    // wake up the workDir and observer (if still available)
                    reviveWeakReferences();

                    // if workDir and/or observer are null, then the WD and TRT
                    // that opened this cache have been GC-ed. This means that this
                    // thread has woken up for an external reason, such as noticing
                    // that the cache file size has changed.  Since the client has
                    // gone away, there is nothing left to do but return.
                    if (workDir == null || observer == null)
                        return;

                    // re-evaluate compressNeeded now, while still synchronized
                    // so that totalEntryCount and uniqueInitialEntry do not have
                    // to be synchronized inside doWork()
                    if (totalEntryCount == 0)
                        compressNeeded = false;
                    else {
                        // when we read/wrote the beginning of the cache file, there was a set of
                        // unique entries. Everything after that is essentially unknown.
                        // If that latter part gets disproportionately big, set compressNeeded.
                        int uncompressedEntryCount = totalEntryCount - uniqueInitialEntryCount;
                        compressNeeded = (uncompressedEntryCount * 100 / totalEntryCount > compressPercentLevel);
                    }
                }
            }
        }
        catch (IOException e) {
            // A serious error has occurred that cannot be recovered from;
            // report it, and give up
            observer.error(e);
        }
        catch (InterruptedException e) {
            // should never happen, but if it does, we were idle anyway
            // so just return, and exit the worker thread
        }
        finally {
            try {
                raf.close();
            }
            catch (IOException e) {
                // ignore
            }

            synchronized (this) {
                worker = null;
                notifyAll();
            }
        }
    }

    private void doWork() throws IOException {
        Map tests = null;
        boolean rebuildCache = false;

        getLock();

        try {
            // if cache file exists and has content, read it all or read updates
            // as appropriate; if any errors occur, zap the file, so it will be rebuilt
            try {
                if (raf.length() > 0) {
                    tests = readCache();
                } else {
                    rebuildCache = true;
                }
            }
            catch (Throwable e) {
                // cache appears to be corrupt; empty it and rebuild it
                if (DEBUG_BASIC)
                    Debug.println("TRC.corrupt " + e);
                workDir.log(i18n, "trc.reloadFault", e);
                rebuildCache = true;
                raf.setLength(0);
            }

            // if we're rebuilding the cache and the VM has started to shut down,
            // quit rebuilding the cache and give up, as quickly as possible,
            // leaving the next client to rebuild the cache instead
            if (rebuildCache && shutdownRequested) {
                testsToWrite.flush();
                raf.setLength(0);
                return;
            }

            // if cache is empty, rebuild it from .jtr files;
            // note that a valid cache with no tests has length 4
            if (rebuildCache) {
                observer.buildingCache(rebuildCache);
                tests = readJTRFiles();
                observer.builtCache();
            }

            if (rebuildCache || compressRequested || raf.length() == 0) {
                writeCache(tests);
                // it is safe to clear the compressRequested flag because the client
                // can only set it to true (not false, nor read it)
                compressRequested = false;
            }

            // write any outstanding tests
            updateCache(tests);
        }
        finally {
            // if we're exiting abnormally, leave the next user to clean up
            // if the file is corrupt; for now, it is better to unlock the
            // file to say we're not using it any more
            releaseLock();
        }

        if (updateNeeded) {
            if (!shutdownRequested) {
                observer.update(tests);
            }
            // it is safe to clear the fullUpdateRequested flag because the client
            // can only set it to true (not false, nor read it)
            fullUpdateRequested = false;
        }
    }

    private static final int MIN_TEST_READ_INTERVAL = 10000; // 10 seconds
    private static final int MIN_TEST_WRITE_INTERVAL = 10000; // 10 seconds

    private boolean isWorkAvailable(long timeLastWork) {
        if (compressRequested || flushRequested || fullUpdateRequested) {
            if (DEBUG_CHECK_WORK)
                Debug.println("TRC.haveWork (request"
                              + (compressRequested ? ":compress" : "")
                              + (flushRequested ? ":flush" : "")
                              + (fullUpdateRequested ? ":update" : "") + ")");
            return true;
        }

        long now = System.currentTimeMillis();

        if (now - timeLastWork >= MIN_TEST_WRITE_INTERVAL && testsToWrite.size() > 0) {
            if (DEBUG_CHECK_WORK)
                Debug.println("TRC.haveWork (" + testsToWrite.size() + " tests)");
            return true;
        }

        try {
            if (now - timeLastWork >= MIN_TEST_READ_INTERVAL && raf.length() != lastFileSize) {
                if (DEBUG_CHECK_WORK)
                    Debug.println("TRC.haveWork (file size changed: " + raf.length() + ")");
                return true;
            }
        }
        catch (IOException e) {
            // if an error occurred, we ought to let the worker thread go investigate
            if (DEBUG_CHECK_WORK)
                Debug.println("TRC.haveWork (" + e.getMessage() + ")");
            return true;
        }

        return false;
    }

    //-------------------------------------------------------------------------------------
    //
    // shutdown handler code

    private static final int MAX_SHUTDOWN_TIME = 30000; // 30 seconds

    synchronized void shutdown() {
        if (DEBUG_BASIC)
            Debug.println("TRC.worker shutdown, " + testsToWrite.size() + " tests to flush");

        // shutdownRequested is initially false, and gets set true here;
        // once set true, it stays true and never goes false again, which
        // reduces the need for synchronized access
        shutdownRequested = true;
        if (testsToWrite.size() > 0)
            flushRequested = true;
        notifyAll();

        long now = System.currentTimeMillis();
        long end = now + MAX_SHUTDOWN_TIME;
        try {
            while (worker != null & now < end) {
                if (DEBUG_SYNC)
                    Debug.println("TRC.shutdown waiting for worker to exit");

                wait(end - now);
                now = System.currentTimeMillis();
            }

            if (DEBUG_SYNC)
                Debug.println("TRC.shutdown done");
        }
        catch (InterruptedException e) {
            // ignore
        }

        // important to avoid memory leak !!!
        try {
            if (shutdownHandler != null) {
                Runtime.getRuntime().removeShutdownHook(shutdownHandler);
            }
        }
        catch(IllegalStateException ex) {
            // it's ok if shutdown is in process now
        }
    }

    //-------------------------------------------------------------------------------------
    //
    // Read a set of tests from the *.jtr files in the work directory

    private Map readJTRFiles() {
        final long start = System.currentTimeMillis();
        Map tests = new TreeMap();
        readJTRFiles(workDir.getRoot(), tests);

        // these lines are all for logging benchmark info
        final long time = System.currentTimeMillis() - start;
        Logger log = null;
        try {
            log = workDir.getTestSuite().createLog(workDir, null,
                       i18n.getString("core.log.name"));
        }
        catch (TestSuite.DuplicateLogNameFault f) {
            try {
                log = workDir.getTestSuite().getLog(workDir, i18n.getString("core.log.name"));
            }
            catch (TestSuite.NoSuchLogFault f2) { }
        }

        if (log != null) {
            Integer loadTime = new Integer((int) (time / 1000));
            Object[] params = new Object[]{loadTime};
            String output = i18n.getString("trc.log.rtime", params);
            log.info(output);
            if (DEBUG_BASIC) Debug.println(output);
        }

        return tests;
    }

    private void readJTRFiles(File dir, Map tests) {
        File[] entries = dir.listFiles();
        if (entries != null) {
            // monitor shutdownRequested and give up if set true;
            // no specific notification is passed back in this case;
            // it is assumed the caller will also check shutdownRequested
            // and act appropriately
            for (int i = 0; i < entries.length && !shutdownRequested; i++) {
                File f = entries[i];
                if (f.isDirectory())
                    readJTRFiles(f, tests);
                else if (TestResult.isResultFile(f)) {
                    try {
                        TestResult tr = new TestResult(f);
                        tests.put(tr.getWorkRelativePath(), tr);
                    }
                    catch (TestResult.ResultFileNotFoundFault e) {
                        // hmm, should not happen, since we just read the directory
                        workDir.log(i18n, "trc.lostjtr", f);
                    }
                    catch (TestResult.ReloadFault e) {
                        // delete this jtr and continue
                        // should we inform TRT? perhaps via observer
                        workDir.log(i18n, "trc.badjtr", f);
                        f.delete();
                    }
                }
                entries[i] = null;  // just to help GC
            }
        }
    }

    private TestResult reload(Map tests, TestResult tr) {
        File jtr = workDir.getFile(tr.getWorkRelativePath());
        try {
            return new TestResult(jtr);
        }
        catch (TestResult.ResultFileNotFoundFault e) {
            // test is presumably not run or has been purged
            // this notRun status will persist in cache until cache rebuilt
            String name = tr.getTestName();
            tr = new TestResult(name, workDir, Status.notRun(""));
            tests.put(name, tr); // in case fullUpdateRequested
            return tr;
        }
        catch (TestResult.ReloadFault e) {
                                // bad .jtr, delete it
            workDir.log(i18n, "trc.badjtr", jtr);
            jtr.delete();
            // this notRun status will persist in cache until cache rebuilt
            String name = tr.getTestName();
            tr = new TestResult(name, workDir, Status.notRun("previous results corrupted"));
            tests.put(name, tr); // in case fullUpdateRequested
            return tr;
        }
    }

    //-------------------------------------------------------------------------------------
    //
    // Read the cache

    private Map readCache()
        throws IOException, IllegalArgumentException
    {
        final long start = System.currentTimeMillis();

        if (DEBUG_WORK)
            Debug.println("TRC.readCache");

        raf.seek(0);
        int fileSerial = raf.readInt();

        if (DEBUG_WORK)
            Debug.println("TRC.readCache serial=" + fileSerial);

        if (lastFileSize == -1 || fileSerial != lastSerial
            || fullUpdateRequested || compressRequested) {
            updateNeeded = (fullUpdateRequested
                            || fileSerial != lastSerial
                            || raf.length() > lastFileSize);
            // read full cache
            lastSerial = fileSerial;
            totalEntryCount = 0;
            Map tests = readCacheEntries();
            uniqueInitialEntryCount = tests.size();

            if (DEBUG_WORK)
                Debug.println("TRC.readCache read all (" + tests.size() + " tests, " + uniqueInitialEntryCount + " unique)");

            final long time = System.currentTimeMillis() - start;
            Logger log = null;
            try {
                log = workDir.getTestSuite().createLog(workDir, null,
                           i18n.getString("core.log.name"));
            }
            catch (TestSuite.DuplicateLogNameFault f) {
                try {
                    log = workDir.getTestSuite().getLog(workDir, i18n.getString("core.log.name"));
                }
                catch (TestSuite.NoSuchLogFault f2) { }
            }

            if (log != null) {
                Integer loadTime = new Integer((int) (time / 1000));
                Object[] params = new Object[]{loadTime};
                String output = i18n.getString("trc.log.ptime", params);
                log.info(output);
                if (DEBUG_BASIC) Debug.println(output);
            }

            return tests;
        }
        else if (raf.length() > lastFileSize) {
            // just read updates from file
            raf.seek(lastFileSize);
            Map tests = readCacheEntries();

            if (DEBUG_WORK)
                Debug.println("TRC.readCache read update (" + tests.size() + " tests)");

            updateNeeded = true;
            return tests;
        }
        else {
            // no updates available
            updateNeeded = false;
            return null;
        }
    }

    private Map readCacheEntries()
        throws IOException, IllegalArgumentException
    {
        Map tests = new TreeMap();
        while (raf.getFilePointer() < raf.length()) {
            String name = raf.readUTF();
            int status = raf.readInt();
            String reason = raf.readUTF();
            long endTime = raf.readLong();
            TestResult tr = new TestResult(name, workDir, new Status(status, reason), endTime);
            File f = tr.getFile();
            if (!f.exists()) {
                tr.resetFile();
            }
            tests.put(tr.getWorkRelativePath(), tr);
            totalEntryCount++; // count all entries, including duplicates
        }
        lastFileSize = raf.length();
        return tests;
    }

    //-------------------------------------------------------------------------------------
    //
    // Write the cache

    private void writeCache(Map tests) throws IOException {
        if (tests == null)
            throw new IllegalStateException();

        // merge any tests in testsToWrite
        // testsToWrite is a thread-safe fifo, so it is safe to keep reading
        // it till its empty, even though some tests may even have been added
        // after the worker woke up
        TestResult tr;
        while ((tr = (TestResult) (testsToWrite.remove())) != null) {
            // check if test is in the set we've just read
            String name = tr.getTestName();
            TestResult tr2 = (TestResult) (tests.get(name));
            // if the cache file contains a conflicting entry,
            // reload the test from the .jtr file; otherwise, add it to the cache
            if (tr2 != null && !tr2.getStatus().equals(tr.getStatus()))
                reload(tests, tr);
            else
                tests.put(tr.getWorkRelativePath(), tr);
        }

        // write cache
        raf.seek(0);
        long now = System.currentTimeMillis();
        lastSerial = (int) ((now >> 16) + (now & 0xffff));
        raf.writeInt(lastSerial);

        for (Iterator iter = tests.values().iterator(); iter.hasNext(); ) {
            tr = (TestResult) (iter.next());
            writeCacheEntry(tr);
        }

        if (DEBUG_WORK)
            Debug.println("TRC.writeCache write all (" + tests.size() + " tests)");

        raf.setLength(raf.getFilePointer());
        lastFileSize = raf.length();
        uniqueInitialEntryCount = totalEntryCount = tests.size();
    }

    private void updateCache(Map tests) throws IOException {
        // testsToWrite is a thread-safe fifo, so it is safe to keep reading
        // it till its empty, even though some tests may even have been added
        // after the worker woke up
        int debugCount = 0;
        raf.seek(lastFileSize);
        TestResult tr;
        while ((tr = (TestResult) (testsToWrite.remove())) != null) {
            if (tests != null) {
                // check if test is in the set we've just read
                String name = tr.getTestName();
                TestResult tr2 = (TestResult) (tests.get(name));
                if (tr2 != null) {
                    // cache also contains an entry for this test:
                    // reload from .jtr file in case of conflict
                    if (!tr2.getStatus().equals(tr.getStatus()))
                        tr = reload(tests, tr);
                }
            }
            writeCacheEntry(tr);
            debugCount++;
        }
        if (DEBUG_WORK && debugCount > 0)
            Debug.println("TRC.writeCache write update (" + debugCount + " tests)");

        raf.setLength(raf.getFilePointer());
        lastFileSize = raf.length();
    }

    private void writeCacheEntry(TestResult tr) throws IOException {
        String name = tr.getTestName();
        Status status = tr.getStatus();
        raf.writeUTF(name);
        raf.writeInt(status.getType());

        String reason = status.getReason();

        if (reason == null) {
            reason = "";
        }

        // writeUTF() can only accept a limited string length, additionally,
        // in this field and in the cache, the full data is not needed
        if (reason.length() > MAX_REASON_LENGTH) {
            reason = reason.substring(0,15) +
                        "[...]" +
                 reason.substring(reason.length()-MAX_REASON_LENGTH+20);
        }
        raf.writeUTF(reason);


        raf.writeLong(tr.getEndTime());
        totalEntryCount++;
    }

    //-------------------------------------------------------------------------------------
    //
    // lock acquisition and release

    // lock acquisition and notification parameters
    private static final long INITIAL_LOCK_NOTIFY_TIME = 20000; // 20 seconds
    private static final long LOCK_NOTIFY_INTERVAL = 60000;     // 60 second
    private static final long MAX_LOCK_WAIT_TIME =
        Integer.getInteger("javatest.trc.timeout", 5).intValue() * 60000;
                                                                // default 5 minutes

    // retry parameters
    private static final int INITIAL_RETRY_DELAY_TIME = 500;    // 0.5 second
    private static final int MAX_RETRY_DELAY_TIME = 10000;      // 10 seconds

    private void getLock() throws IOException {
        long start = System.currentTimeMillis();
        int retryDelay = INITIAL_RETRY_DELAY_TIME;
        long lastNotified = 0;

        // for JDK 1.4, should consider using raf.getChannel().tryLock()
        while (!lockFile.createNewFile()) {
            long now = System.currentTimeMillis();
            long timeSinceStart = now - start;
            if (timeSinceStart < INITIAL_LOCK_NOTIFY_TIME) {
                // no need to worry, yet
                continue;
            }
            else if (timeSinceStart < MAX_LOCK_WAIT_TIME) {
                // slowly getting nervous: periodically notify observer
                long timeSinceLastNotified = now - lastNotified;
                if (lastNotified == 0
                    || timeSinceLastNotified > LOCK_NOTIFY_INTERVAL) {
                    observer.waitingForLock(timeSinceStart);
                    lastNotified = System.currentTimeMillis();
                }

                // slowly increase delay while we are waiting
                if (retryDelay < MAX_RETRY_DELAY_TIME)
                    retryDelay = Math.min(2 * retryDelay, MAX_RETRY_DELAY_TIME);
            }
            else {
                // full fledge panic: trash the lock and rebuild the cache
                observer.timeoutWaitingForLock();
                workDir.log(i18n, "trc.lockTimeout");

                try {
                    if (raf != null)
                        raf.close();
                }
                catch (IOException e) {
                    // ignore
                }

                cacheFile.delete();
                lockFile.delete();

                raf = new RandomAccessFile(cacheFile, "rw");

                // leave caller to repopulate cache
            }

            // sleep for a bit before retrying to avoid busy waiting
            try {
                Thread.currentThread().sleep(retryDelay);
            }
            catch (InterruptedException e) {
                // ignore
            }

        }

        observer.acquiredLock();
    }

    private void releaseLock() {
        // for JDK 1.4, should consider releasing the FileLock obtained from
        // raf.getChannel().tryLock()
        lockFile.delete();
        observer.releasedLock();
    }

    //-------------------------------------------------------------------------------------

    // When the work thread sleeps, it zaps the main references to observer and workDir
    // to the possibility that the WD and TRT might be GCed, and just retains weak references.
    // These weak references get revived when the worker thread has work to do.
    private void reviveWeakReferences() {
        if (workDir == null)
            workDir = (WorkDirectory) weakWorkDir.get();

        if (observer == null)
            observer = (Observer) weakObserver.get();
    }

    //-------------------------------------------------------------------------------------

    // general instance data
    private Observer observer;
    private WeakReference weakObserver;
    private WorkDirectory workDir;
    private WeakReference weakWorkDir;
    private File cacheFile;
    private File lockFile;
    private Thread worker;
    private Thread shutdownHandler;

    // worker thread data
    private RandomAccessFile raf;
    private int uniqueInitialEntryCount;
    private int totalEntryCount;
    private int lastSerial;
    private long lastFileSize;
    private boolean updateNeeded;

    // synchronized data
    private boolean fullUpdateRequested;
    private boolean compressNeeded;
    private boolean compressRequested;
    private boolean flushRequested;
    private boolean shutdownRequested;
    private Fifo testsToWrite = new Fifo();

    private static final String V1_FILENAME = "ResultCache.jtw";
    private static final String V1_LOCKNAME = V1_FILENAME + ".lck";
    private static final String V2_FILENAME = "ResultCache2.jtw";
    private static final String V2_LOCKNAME = V2_FILENAME + ".lck";

    // other
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(TestResultCache.class);
    private static int workerNumber; // for naming threads

    // maximum length of reason string written into cache
    // writeUTF can only write a limited length string, see writeCacheEntry()
    private static final int MAX_REASON_LENGTH = 256;

    private static int debug = Debug.getInt(TestResultCache.class);
    private static final boolean DEBUG_BASIC =      (debug >= 1);  // basic messages and stack trace
    private static final boolean DEBUG_TESTS =      (debug >= 2);  // details about tests
    private static final boolean DEBUG_WORK  =      (debug >= 3);  // details about work done
    private static final boolean DEBUG_CHECK_WORK = (debug >= 4);  // details when checking for work
    private static final boolean DEBUG_SYNC =       (debug >= 5);  // details about thread syncs
}
