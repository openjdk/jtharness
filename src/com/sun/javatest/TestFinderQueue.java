/*
 * $Id$
 *
 * Copyright (c) 1996, 2009, Oracle and/or its affiliates. All rights reserved.
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
import java.util.Hashtable;
import java.util.Vector;

import com.sun.javatest.util.DynamicArray;
import com.sun.javatest.util.Fifo;
import com.sun.javatest.util.I18NResourceBundle;

/**
 * An iterator-based interface to the tests in a test suite, as read by a test finder.
 */
public class TestFinderQueue {
    /**
     * This interface provides a means for TestFinder to report on events that
     * might be of interest as it executes.
     */
    public static interface Observer
    {
        /**
         * Another file which needs to be read has been found.
         * @param file the file which was found
         */
        void found(File file);

        /**
         * A file is being read.
         * @param file the file being read
         */
        void reading(File file);

        /**
         * A file has been read.
         * @param file the file which was read
         */
        void done(File file);

        /**
         * A test description has been found.
         * @param td the test description which was found
         */
        void found(TestDescription td);

        /**
         * A test description which was previously found, has been rejected by
         * a test filter, and so has not been put in the queue of tests to be executed.
         * @param td the test description which was rejected by the filter
         * @param f the filter which rejected the test
         */
        void ignored(TestDescription td, TestFilter f);

        /**
         * A test description that was previously put in the test finder queue
         * has been taken from the queue and passed back to the client caller.
         * @param td the test description which was taken from the queue
         */
        void done(TestDescription td);

        /**
         * The queue of tests has been flushed.
         */
        void flushed();

        /**
         * An error was reported by the test finder while reading a file.
         * @param msg a detail message describing the error
         */
        void error(String msg);

        /**
         * An error was reported by the test finder while reading a file.
         * @param td the test description to which the error applies
         * @param msg a detail message describing the error
         */
        void error(TestDescription td, String msg);
    }

    /**
     * Create a test finder queue.
     */
    public TestFinderQueue() {
    }

    /**
     * Create a test finder queue, using a specified test finder.
     * @param finder the test finder to be used to read the tests
     */
    public TestFinderQueue(TestFinder finder) {
        setTestFinder(finder);
    }

    /**
     * Get the test finder being used by this object.
     * @return the test finder being used by this object
     * @see #setTestFinder
     */
    public TestFinder getTestFinder() {
        return testFinder;
    }

    /**
     * Set the test finder to be used by this object.
     * It may only be set once.
     * @param finder the test finder to be used by this object
     * @throws NullPointerException if the finder is null
     * @throws IllegalStateException if the finder has already been set
     * @see #getTestFinder
     */
    public void setTestFinder(TestFinder finder) {
        if (finder == null)
            throw new NullPointerException();

        if (testFinder != null && testFinder != finder)
            throw new IllegalStateException();

        this.testFinder = finder;
        testFinder.setErrorHandler(new TestFinder.ErrorHandler() {
            public void error(String msg) {
                errorCount++;
                notifier.error(msg);
            }
        });
    }

    /**
     * Set an array of filters that will be used to filter the tests read by the
     * test finder. Each test must be accepted by all the filters to be put in
     * the queue.
     * @param filters the filters to be used.
     */
    public void setFilters(TestFilter[] filters) {
        this.filters = filters;
    }

    /**
     * Set the initial set of files to be read by the test finder.
     * Additional files may be read as a result of reading these and subsequent files.
     * @param initTests the initial set of files to be read by the test finder
     */
    public synchronized void setTests(String[] initTests) {
        File testSuiteRoot = testFinder.getRoot();

        // make canonical copy of tests
        // really ought not to be using File, since the tests may contain a trailing #xxx
        File[] files;
        if (initTests == null)
            // ensure not null
            files = new File[] {testSuiteRoot};
        else {
            files = new File[initTests.length];
            for (int i = 0; i < initTests.length; i++) {
                files[i] = new File(initTests[i]);
            }
        }

        rootDir = (testSuiteRoot.isDirectory() ?
                   testSuiteRoot : new File(testSuiteRoot.getParent()));

        // build up the fifo of tests to be used by readNextFile

        tests = new Fifo();
        currInitialFile = null;

        for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                String n = f.getName();
                // in pass 0, only select initial files without #
                // in pass 1, only select initial files with #
                if ((n.indexOf("#") != -1) == (pass == 0))
                    continue;

                // ensure all absolute, or if relative, make them relative
                // to rootDir
                if (!f.isAbsolute())
                    f = new File(rootDir, f.getPath());
                // ensure no trailing file separator
                String p = f.getPath();
                if (p.endsWith(File.separator))
                    f = new File(p.substring(0, p.length() - 1));
                tests.insert(f);
            }
        }

        filesRemainingCount = filesToRead.size() + tests.size();
    }

    /**
     * Set a flag indicating whether it is OK to find no tests in the
     * specified set of files. If set to false, and if no tests have
     * been found by the time the last file has been read, an error
     * will be notified to any observers.
     * @param zeroTestsOK set to true to suppress an error being generated
     * if no tests are found by the time that all files have been read
     */
    public void setZeroTestsOK(boolean zeroTestsOK) {
        this.zeroTestsOK = zeroTestsOK;
    }

    /**
     * Set the queue to "repeat" a set of test descriptions by putting
     * them in the test found queue again.
     * @param tds the test descriptions to be "found again".
     * @deprecated retained for historical purposes
     */
    public void repeat(TestDescription[] tds) {
        if (tests == null)
            tests = new Fifo(); // for now
        for (int i = 0; i < tds.length; i++) {
            TestDescription td = tds[i];
            testDescsFound.insert(td);
            testsFoundCount++;
            notifier.found(td);
        }
    }



    /**
     * Get the next test description if one is available, or null when all have
     * been returned.
     *
     * @return     A test description or null.
     */
    public TestDescription next() {
        TestDescription td;

        synchronized (this) {
            while (needReadAhead() && readNextFile()) /*NO-OP*/;

            // read files until there is a test description available or there
            // are no more files.
            while ((td = (TestDescription)(testDescsFound.remove())) == null) {
                boolean ok = readNextFile();
                if (!ok)
                    return null;
            }

            // note testsDone, for readAhead
            testsDoneCount++;
        }

        notifier.done(td);
        return td;
    }


    //--------------------------------------------------------------------------

    /**
     * Get the root directory for the test finder.
     * @return the root directory, as set in the test finder
     */
    public File getRoot() {
        return rootDir;
    }

    //--------------------------------------------------------------------------
    //
    // these are all carefully arranges to not need to be synchronized

    /**
     * Get the number of files that have been found so far.
     * @return the number of files that have been found so far
     */
    public int getFilesFoundCount() {
        return filesFound.size();
    }

    /**
     * Get the number of files that have been found and read so far.
     * @return the number of files that have been found and read so far
     */
    public int getFilesDoneCount() {
        return filesDoneCount;
    }

    /**
     * Get the number of files that have been found but not yet read so far.
     * @return the number of files that have been found but not yet read so far
     */
    public int getFilesRemainingCount() {
        return filesRemainingCount;
    }

    /**
     * Get the number of tests that have been found so far.
     * @return the number of tests that have been found so far
     */
    public int getTestsFoundCount() {
        return testsFoundCount;
    }

    /**
     * Get the number of tests that have been read from this object so far.
     * @return the number of tests that have been read from this object so far
     */
    public int getTestsDoneCount() {
        return testsDoneCount;
    }

    /**
     * Get the number of tests which have been found but not yet from this
     * object so far.
     * @return the number of tests which have been found but not yet read
     * from this object so far
     */
    public int getTestsRemainingCount() {
        return testDescsFound.size();
    }

    /**
     * Get the number of errors that have been found so far by the test finder
     * while reading the tests.
     * @return the number of errors that have been found so far by the test finder
     * while reading the tests.
     */
    public int getErrorCount() {
        return errorCount;
    }

    //--------------------------------------------------------------------------

    /**
     * Add an observer to monitor the progress of the TestFinder.
     * @param o the observer
     */
    public void addObserver(Observer o) {
        notifier.addObserver(o);
    }

    /**
     * Remove an observer form the set currently monitoring the progress
     * of the TestFinder.
     * @param o the observer
     */
    public void removeObserver(Observer o) {
        notifier.removeObserver(o);
    }

    //--------------------------------------------------------------------------


    /**
     * Set the amount of read-ahead done by the finder.
     * @param mode      acceptable values are as follows:
     * <dl> <dt> 0: no read ahead
     * <dd> Files are not read ahead more than necessary
     * <dt> 1: low read ahead
     * <dd> A low priority thread is created to read the test files
     * when the system is otherwise idle
     * <dt> 2: medium read ahead
     * <dd> A low priority thread is created to read the test files
     * when the system is otherwise idle. In addition, if the number
     * of tests done approaches the number of tests read, then more
     * tests will be read.
     * <dt> 3: full and immediate read ahead
     * <dd> All the tests will be read now
     * </dl>
     */
    public synchronized void setReadAheadMode(byte mode) {
        switch (mode) {
        case NO_READ_AHEAD:
        case FULL_READ_AHEAD:
            readAheadMode = mode;
            readAheadWorker = null; // worker will note this and go away
            break;

        case LOW_READ_AHEAD:
        case MEDIUM_READ_AHEAD:
            readAheadMode = mode;
            if (readAheadWorker == null) {
                readAheadWorker = new Thread() {
                    public void run() {
                        // This is intended to be interruptible and
                        // relies on safe atomic access to worker
                        while ((readAheadWorker == this) && readNextFile()) /*NO-OP*/;
                        // when thread exits; flatten pointer if still current
                        synchronized (TestFinderQueue.this) {
                            if (readAheadWorker == this)
                                readAheadWorker = null;
                        }
                    }
                };
                readAheadWorker.setName("TestFinderQueue:Worker:" + workerIndex++);
                readAheadWorker.setPriority(Thread.MIN_PRIORITY);
                readAheadWorker.start();
            }
            break;

        default:
            throw new IllegalArgumentException("invalid value for mode");
        }
    }

    /**
     * A constant specifying that the test finder queue should not perform
     * any read ahead.
     */
    public static final byte NO_READ_AHEAD = 0;

    /**
     * A constant specifying the test finder queue should perform minimal
     * read ahead.
     */
    public static final byte LOW_READ_AHEAD = 1;

    /**
     * A constant specifying the test finder queue should perform medium
     * (typical) read ahead.
     */
    public static final byte MEDIUM_READ_AHEAD = 2;

    /**
     * A constant specifying the test finder queue should perform complete
     * read ahead, reading all tests from the test finder before returning any
     * from this object.
     */
    public static final byte FULL_READ_AHEAD = 3;

    /**
     * Flush all readahead.
     */
    public void flush() {
        synchronized (this) {
            filesToRead.setSize(0);
            tests.flush();
            testDescsFound.flush();
            filesRemainingCount = 0;
        }
        notifier.flushed();
    }


    /**
     * This method is called from next() to determine if any readAhead
     * should be done there and then, before getting the next TestDescription
     * for the client.
     */
    private boolean needReadAhead() {
        switch (readAheadMode) {
        case FULL_READ_AHEAD:
            return true;
        case MEDIUM_READ_AHEAD:
            // return true if not many tests read yet, or if testsDoneCount
            // is greater than a certain percentage of testsFoundCount.
            // This percentage increases inverse exponentially on the
            // number of tests found. The intent is to try and keep
            // progress meters based on testsDoneCount and testsFoundCount helpful,
            // while permitting readAhead to be done.
            // The formula has the following datapoints:
            // testsFoundCount: 1000   percent: 18%
            // testsFoundCount: 10000  percent: 86%
            if (testsFoundCount < 100)
                // don't let tests start until at least 100 have been read
                return true;
            else {
                double percent = 1 - Math.exp(-0.0002 * testsFoundCount);
                return (testsDoneCount > (testsFoundCount * percent));
            }
        default:
            return false;
        }
    }

    //---------------------------------------------------------------

    private synchronized boolean readNextFile() {
        if (filesToRead.isEmpty()) {
            // have we finished reading an initial file and found no test descriptions in it?
            // if so, inform the caller
            if (currInitialFile != null
                && testsFoundCountBeforeCurrInitialFile == testsFoundCount
                && !zeroTestsOK) {
                errorCount++;
                notifier.error(i18n.getString("finder.noTests", currInitialFile));
            }

            // are there any more tests that have not been read?
            // check until we find one (just one).
            while (filesToRead.isEmpty() && !tests.isEmpty()) {
                currInitialFile = (File)tests.remove();
                foundFile(currInitialFile);
            }

            // if we didn't find any more initial files, there is nothing more to do
            if (filesToRead.isEmpty()) {
                currInitialFile = null;
                return false;
            }
            else
                testsFoundCountBeforeCurrInitialFile = testsFoundCount;
        }


        File f = (File)(filesToRead.lastElement());
        filesToRead.setSize(filesToRead.size() - 1);
        filesRemainingCount = filesToRead.size() + tests.size();

        String path = f.getPath();
        int index = path.indexOf('#');
        if (index != -1) {
            selectedId = path.substring(index + 1);
            f = new File(path.substring(0, index));
        }

        // The filesToRead are maintained in a vector that approximates a stack:
        // new entries are added at or near the end, and entries are taken from
        // the end. The subtlety is to add the new files found in file in reverse
        // order, so that when removed from the end, they are used in the
        // correct order. This is done by inserting each new file found at a fixed
        // place, corresponding to the end of the vector as it is when scan() starts.
        // The net effect is to push up files added earlier in this scan, so they
        // they will be used first.   The overall net effect is that of a depth-first
        // search for test descriptions.
        fileInsertPosn = filesToRead.size();

        notifier.reading(f);
        try {
            testFinder.read(f);
        }
        finally {
            TestDescription[] tds = testFinder.getTests();
            for (int i = 0; i < tds.length; i++) {
                foundTestDescription(tds[i]);
            }

            File[] files = testFinder.getFiles();
            for (int i = 0; i < files.length; i++) {
                foundFile(files[i]);
            }

            // done limiting tests to this id
            selectedId = null;
            filesDoneCount++;
            notifier.done(f);

            /*
            if (filesToRead.isEmpty()) {
                // we have read all the files we can
                // flush various tables to free up any space used
                filesFound.clear();
                testsInFile.clear();
            }
            */

        }

        // read a file OK
        return true;
    }

    /**
     * Add a file to the queue of files waiting to be read.
     * It will be added to the queue if it has not already been read or is
     * on the queue waiting to be read, and if the finder is not looking for
     * a specific test in the current file.
     * @param newFile The file to be queued
     */
    private void foundFile(File newFile) {
        // only accept new files if not looking for a specific test in the
        // current file
        if (selectedId == null) {
            Object prev = filesFound.put(newFile.getPath(), newFile);
            if (prev == null) {
                filesToRead.insertElementAt(newFile, fileInsertPosn);
                notifier.found(newFile);
            }
        }
    }

    private void foundTestDescription(TestDescription td) {
        // if we are not searching for a specific test, or if we are and we have
        // found it, then add the test to the list of tests we have found
        if (selectedId == null || selectedId.equals(td.getId())) {
            if (filters != null) {
                for (int i = 0; i < filters.length; i++) {
                    TestFilter filter = filters[i];
                    try {
                        if (!filter.accepts(td)) {
                            notifier.ignored(td, filter);
                            return;
                        }
                    }
                    catch (TestFilter.Fault e) {
                        errorCount++;
                        notifier.error(td, e.getMessage());
                        return;
                    }
                }
            }

            testDescsFound.insert(td);
            testsFoundCount++;
            notifier.found(td);
        }
    }

    //---------------------------------------------------------------

    private static class Notifier implements Observer {
        public synchronized void addObserver(Observer o) {
            observers = (Observer[])DynamicArray.append(observers, o);
        }

        public synchronized void removeObserver(Observer o) {
            observers = (Observer[])DynamicArray.remove(observers, o);
        }

        public synchronized void found(File file) {
            for (int i = 0; i < observers.length; i++)
                observers[i].found(file);
        }

        public synchronized void reading(File file) {
            for (int i = 0; i < observers.length; i++)
                observers[i].reading(file);
        }

        public synchronized void done(File file) {
            for (int i = 0; i < observers.length; i++)
                observers[i].done(file);
        }

        public synchronized void found(TestDescription td) {
            for (int i = 0; i < observers.length; i++)
                observers[i].found(td);
        }

        public synchronized void ignored(TestDescription td, TestFilter f) {
            for (int i = 0; i < observers.length; i++)
                observers[i].ignored(td, f);
        }

        public synchronized void done(TestDescription td) {
            for (int i = 0; i < observers.length; i++)
                observers[i].done(td);
        }

        public synchronized void flushed() {
            for (int i = 0; i < observers.length; i++)
                observers[i].flushed();
        }

        public synchronized void error(String msg) {
            for (int i = 0; i < observers.length; i++)
                observers[i].error(msg);
        }

        public synchronized void error(TestDescription td, String msg) {
            for (int i = 0; i < observers.length; i++)
                observers[i].error(td, msg);
        }

        private Observer[] observers = new Observer[0];
    }

    //----------member variables------------------------------------------------


    private TestFinder testFinder;
    private Fifo tests;
    private TestFilter[] filters;
    private String selectedId;
    private File rootDir;
    private File currInitialFile;
    private int testsFoundCountBeforeCurrInitialFile;
    private boolean zeroTestsOK;

    private Vector filesToRead  = new Vector(32, 8);
    private int fileInsertPosn;
    private Fifo testDescsFound = new Fifo();
    private int filesRemainingCount;
    private int filesDoneCount;
    private int testsDoneCount;
    private int testsFoundCount;
    private int errorCount;

    private Hashtable filesFound  = new Hashtable();

    private byte readAheadMode;
    private Thread readAheadWorker;
    private static int workerIndex;

    private Notifier notifier = new Notifier();
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(TestFinder.class);
}
