/*
 * $Id$
 *
 * Copyright (c) 1996, 2016, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.TestResultTable.TreeIterator;
import com.sun.javatest.httpd.HttpdServer;
import com.sun.javatest.httpd.RootRegistry;
import com.sun.javatest.util.BackupPolicy;
import com.sun.javatest.util.DynamicArray;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.ReadAheadIterator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The object responsible for coordinating the execution of a test run.
 */
public class Harness {
    private static final boolean ZERO_TESTS_OK = true;
    private static final boolean ZERO_TESTS_ERROR = false;
    private static final int DEFAULT_READ_AHEAD = 100;
    private static File classDir;

    //--------------------------------------------------------------------------
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(Harness.class);
    private BackupPolicy backupPolicy;
    private int autostopThreshold;
    private HarnessHttpHandler httpHandler;
    private Trace trace;
    private Thread worker;


    //--------------------------------------------------------------------------
    private Parameters params;

    //--------------------------------------------------------------------------
    private TestSuite testSuite;

    //--------------------------------------------------------------------------
    private WorkDirectory workDir;

    //--------------------------------------------------------------------------
    private ExcludeList excludeList;
    private TestResultTable.TreeIterator testIter;

    //--------------------------------------------------------------------------
    private int readAheadMode = ReadAheadIterator.FULL;
    private ReadAheadIterator<TestResult> raTestIter;
    private int numTestsDone;
    private TestEnvironment env;
    private TestResultTable resultTable;
    private Notifier notifier = new Notifier();
    private long startTime = -1L;
    private long finishTime = -1L;
    private long cleanupFinishTime = -1L;
    private long testsStartTime = -1L;
    private boolean isBatchRun;
    private boolean stopping;
    public static final String DEBUG_OBSERVER_CLASSNAME_SYS_PROP = "com.sun.javatest.Harness.DEBUG_OBSERVER";

    {
        Integer i = Integer.getInteger("javatest.autostop.threshold");
        autostopThreshold = i == null ? 0 : i.intValue();
    }

    /**
     * Instantiate a harness.
     *
     * @param classDir The class dir to put in the environment for otherJVM tests
     * @see #Harness()
     * @see #setClassDir
     * @deprecated Use Harness() instead
     */
    @java.lang.Deprecated
    public Harness(File classDir) {
        this();
        setClassDir(classDir);
    }

    /**
     * Instantiate a harness.
     */
    public Harness() {
        backupPolicy = BackupPolicy.noBackups();

        params = null;

        if (!Boolean.getBoolean("javatest.noTraceRequired")) {
            trace = new Trace(backupPolicy);
            addObserver(trace);
        }

        String debugObserverClassName = System.getProperty(DEBUG_OBSERVER_CLASSNAME_SYS_PROP);
        if (debugObserverClassName != null) {
            try {
                Observer debugObserverInstance =
                        Class.forName(debugObserverClassName).asSubclass(Observer.class).getConstructor().newInstance();
                addObserver(debugObserverInstance);
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate debug observer: " + debugObserverClassName);
            }
        }

        // web server
        if (HttpdServer.isActive()) {
            httpHandler = new HarnessHttpHandler(this);
            RootRegistry.getInstance().addHandler("/harness", "JT Harness",
                    httpHandler);
        }
    }

    /**
     * Get the class directory or jar file containing JT Harness.
     *
     * @return the class directory or jar file containing JT Harness
     * @see #setClassDir
     */
    public static File getClassDir() {
        return classDir;
    }

    /**
     * Specify the class directory or jar file containing JT Harness.
     *
     * @param classDir the class directory or jar file containing JT Harness
     * @see #getClassDir
     */
    public static void setClassDir(File classDir) {
        if (Harness.classDir != null && Harness.classDir != classDir) {
            throw new IllegalStateException(i18n.getString("harness.classDirAlreadySet"));
        }
        Harness.classDir = classDir;
    }

    private static ArrayList<String> listFilterNames(TestFilter... filters) {
        ArrayList<String> result = new ArrayList<>();

        if (filters == null || filters.length == 0) {
            return result;      // i.e. empty
        }

        for (TestFilter f : filters) {
            // we don't care about composite wrappers, recurse into them
            if (f instanceof CompositeFilter) {
                result.addAll(listFilterNames(((CompositeFilter) f).getFilters()));
            } else if (f instanceof AllTestsFilter) {
                continue;
            } else {
                result.add(f.getName());
            }
        }

        // for convenience, null is never returned
        return result;
    }

    private static String formatFilterList(List<String> names) {
        if (names == null || names.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (String s : names) {
            sb.append("- ");
            sb.append(s);
            sb.append("\n");
        }

        return sb.toString();
    }

    private static String formatFilterStats(String[] tests,
                                            TreeIterator iter) {
        TRT_Iterator treeit = null;

        if (iter == null || !(iter instanceof TRT_Iterator)) {
            return "";
        } else {
            treeit = (TRT_Iterator) iter;
        }

        TestFilter[] filters = treeit.getFilters();
        HashMap<TestFilter, ArrayList<TestDescription>> map = treeit.getFilterStats();
        Set<TestFilter> keyset = map.keySet();
        StringBuilder sb = new StringBuilder();

        // special case for Tests to Run because there is no associated
        // TestFilter
        if (tests != null && tests.length > 0) {
            sb.append("- ");
            sb.append("Tests to Run (" + tests.length + " path(s) specified)");
            sb.append("\n");
        }

        for (TestFilter f : keyset) {
            ArrayList<TestDescription> tds = map.get(f);
            // this works, should consider switching to something which has more
            // rendering control - RTF
            sb.append("- ");
            sb.append(tds.size());
            sb.append(" due to ");        // could use upgrade for readability
            sb.append(f.getName());
            sb.append("\n");
            sb.append("    ");      // indentation
            sb.append(f.getReason());
            sb.append("\n");
        }   // for

        return sb.toString();
    }

    /**
     * Get the backup policy object used by this harness, used to determine
     * the policy for backing up files before overwriting them.
     *
     * @return the backup policy object used by this harness
     * @see #setBackupPolicy
     */
    public BackupPolicy getBackupPolicy() {
        return backupPolicy;
    }

    /**
     * Set the backup policy object to be used by this harness,
     * used to determine the policy for backing up files before
     * overwriting them.
     *
     * @param bp the backup policy object used by this harness
     * @see #getBackupPolicy
     */
    public void setBackupPolicy(BackupPolicy bp) {
        backupPolicy = bp;
    }

    /**
     * Check if a trace file should be generated while performing a test run.
     *
     * @return true if and only if a trace file should be generated
     * @see #setTracingRequired
     */
    public boolean isTracingRequired() {
        return trace != null;
    }

    /**
     * Set whether a trace file should be generated while performing a test run.
     *
     * @param b whether or not a trace file should be generated
     * @see #isTracingRequired
     */
    public void setTracingRequired(boolean b) {
        if (b && trace == null) {
            trace = new Trace(backupPolicy);
            addObserver(trace);
            //currentResults.addObserver(trace);
        } else if (!b && trace != null) {
            removeObserver(trace);
            //currentResults.removeObserver(trace);
            trace = null;
        }
    }

    /**
     * Get the current parameters of the harness.
     *
     * @return null if the parameters have not been set.
     */
    public Parameters getParameters() {
        return params;
    }

    /**
     * Get the current test environment being used by the harness.
     * This is similar to getParameters().getEnv(), except that the environment
     * returned here has some standard additional fields set by the harness
     * itself.
     *
     * @return null if the environment has not been set.
     */
    public TestEnvironment getEnv() {
        return env;
    }

    /**
     * Get the current set of results.  This will either be the set of results
     * from which are currently running, or the results from the last run.
     *
     * @return null if no results are currently available.  This will be the case
     * if the Harness has not been run, or the parameters have been changed
     * without doing a new run.
     */
    public TestResultTable getResultTable() {
        WorkDirectory wd = params == null ? null : params.getWorkDirectory();
        return wd == null ? null : wd.getTestResultTable();
    }

    /**
     * Add an observer to be notified during the execution of a test run.
     * Observers are notified of events in the reverse order they were added --
     * the most recently added observer gets notified first.
     *
     * @param o the observer to be added
     * @see #removeObserver
     */
    public void addObserver(Observer o) {
        notifier.addObserver(o);
    }

    //----------member variables-----------------------------------------------------

    /**
     * Remove a previously registered observer so that it will no longer
     * be notified during the execution of a test run.
     * It is safe for observers to remove themselves during a notification;
     * most obviously, an observer may remove itself during finishedTesting()
     * or finishedTestRun().
     *
     * @param o the observer to be removed
     * @see #addObserver
     */
    public void removeObserver(Observer o) {
        notifier.removeObserver(o);
    }

    /**
     * Start running all the tests defined by a new set of parameters.
     * The tests are run asynchronously, in a separate worker thread.
     *
     * @param p The parameters to be set when the tests are run.
     *          Any errors in the parameters are reported to
     *          any registered observers.
     * @throws Harness.Fault if the harness is currently running tests
     *                       and so cannot start running any more tests right now.
     * @see #isRunning
     * @see #stop
     * @see #waitUntilDone
     */
    public void start(Parameters p) throws Fault {
        startWorker(p);
    }

    /**
     * Wait until the harness completes the current task.
     *
     * @throws InterruptedException if the thread making the call is
     *                              interrupted.
     */
    public synchronized void waitUntilDone() throws InterruptedException {
        while (worker != null) {
            wait();
        }
    }

    /**
     * Stop the harness executing any tests. If no tests are running,
     * the method does nothing; otherwise it notifies any observers,
     * and interrupts the thread doing the work. The worker may carry
     * on for a short time after this method is called, while it waits
     * for all the related tasks to complete.
     *
     * @see #waitUntilDone
     */
    public synchronized void stop() {
        if (worker != null) {
            if (!stopping) {
                notifier.stoppingTestRun();
                stopping = true;
            }
            worker.interrupt();
        }
    }

    /**
     * Run the tests defined by a new set of parameters.
     *
     * @param params The parameters to be used; they will be validated first.
     * @return true if and only if all the selected tests were executed successfully, and all passed
     * @throws Harness.Fault if the harness is currently running tests
     *                       and cannot start running any more tests right now.
     * @see #isRunning
     * @see #stop
     * @see #waitUntilDone
     */
    public boolean batch(Parameters params)
            throws Fault {
        isBatchRun = true;
        // allow full read-ahead by default now - as of 3.2.1
        // this allows the not run field of verbose mode to work
        if (Boolean.getBoolean("javatest.noReadAhead")) {
            readAheadMode = ReadAheadIterator.NONE;
        }

        synchronized (this) {
            if (worker != null) {
                throw new Fault(i18n, "harness.alreadyRunning");
            }
            worker = Thread.currentThread();
        }

        // parameters will be checked later, in runTests, but check here
        // too to specifically verify that workDir is set
        if (!params.isValid()) {
            throw new Harness.Fault(i18n, "harness.incompleteParameters",
                    params.getErrorMessage());
        }

        boolean ok = false;
        try {
            workDir = params.getWorkDirectory();
            resultTable = workDir.getTestResultTable();
            // XXX this was a performance enhancer for 3.x
            //   it will not work if the user expects previous results
            //   to be erased/hidden if they no longer exist in the test
            //   suite.  that goal and this one are fundamentally opposed
            //resultTable.suppressFinderScan(true);
            ok = runTests(params, ZERO_TESTS_OK);
        } catch (TestSuite.Fault e) {
            throw new Fault(i18n, "harness.testsuiteError", e.getMessage());
        } finally {
            synchronized (this) {
                worker = null;
                notifyAll();
            }

            notifier.finishedTestRun(ok);
            isBatchRun = false;
        }

        return ok;
    }

    /**
     * Check if the harness is currently executing a test suite or not.
     *
     * @return true if and only if the harness is currently executing a test suite.
     * @see #start
     * @see #batch
     * @see #stop
     * @see #waitUntilDone
     */
    public boolean isRunning() {
        return worker != null;
    }

    /**
     * Was the harness invoked in batch mode?  If it is not in batch mode, this
     * typically implies that the user is using an interactive GUI interface.
     *
     * @return True if the harness is running and was invoked in batch mode.
     * @throws IllegalStateException If the harness is not running, care should
     *                               be taken to handle this in case the run terminates.
     */
    public synchronized boolean isBatchRun() {
        if (!isRunning()) {
            throw new IllegalStateException();
        }

        return isBatchRun;
    }

    /**
     * Indicates whether the harness has located all the tests it will execute.
     * If true, then getTestsFoundCount() will return the number of test
     * which will be executed during this test run; assuming the harness does not
     * halt for special cases (errors, user request, etc...).  If false,
     * getTestsFoundCount() returns the number of tests located so
     * far.
     *
     * @return True if all tests have been located.  False if the harness is
     * still looking for tests.  Always false if the harness is not
     * running.
     * @see #isRunning()
     * @see #getTestsFoundCount()
     */
    public boolean isAllTestsFound() {
        if (isRunning() && raTestIter != null) {
            return raTestIter.isSourceExhausted();
        } else {
            return false;
        }
    }

    /**
     * Find time since the start of the current or last run.
     * If no run is in progress, this is the time it took to complete the
     * last run.
     *
     * @return Zero if no run has ever been started yet.  Elapsed time in
     * milliseconds otherwise.
     */
    public long getElapsedTime() {
        long time = 0L;

        if (startTime == -1L) {
            time = 0L;                  // no data avail.
        } else if (cleanupFinishTime == -1L) {    // isRunning() isn't good enough
            long now = System.currentTimeMillis();
            time = now - startTime;     // we are still running
        } else {
            time = cleanupFinishTime - startTime;
        }

        return time;
    }

    /**
     * Get the time at which the last run start.
     *
     * @return Time when the last run started in milliseconds.  -1 if there is
     * no previous run.
     * @see #getFinishTime
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Get the time at which the last run finished.  This is the time when
     * the last test completed, and does not include post-run cleanup time.
     *
     * @return Time when the last run finished in milliseconds.  -1 if there is
     * no previous run or a run is in progress.
     * @see #getStartTime
     */
    public long getFinishTime() {
        return finishTime;
    }

    /**
     * Get the time at which cleanup of the entire run was completed.
     * This is after the time when the last test completed.
     *
     * @return Time when the run finished in milliseconds.  -1 if there is
     * no previous run or a run is in progress.
     * @see #getStartTime
     */
    public long getCleanupFinishTime() {
        return cleanupFinishTime;
    }

    public long getTotalCleanupTime() {
        if (cleanupFinishTime < finishTime || cleanupFinishTime == -1L) {
            return -1L;
        } else {
            return cleanupFinishTime - finishTime;
        }
    }

    public long getTotalSetupTime() {
        if (testsStartTime < startTime || testsStartTime == -1L) {
            return -1L;
        } else {
            return testsStartTime - startTime;
        }
    }

    /**
     * Find out the estimated time required to complete the remaining tests.
     *
     * @return A time estimate in milliseconds.  Zero if no run is in progress or
     * no estimate is available.
     */
    public long getEstimatedTime() {
        if (isRunning() == false || numTestsDone == 0) {
            return 0L;
        }

        return getElapsedTime() * (getTestsFoundCount() - numTestsDone) / numTestsDone;
    }

    /**
     * Find out how many tests to run have been located so far.  Data will pertain
     * to the previous run (if any) if isRunning() is false.  The return will be
     * zero if isRunning() is false and there is no previous run for this instance
     * of the Harness.
     *
     * @return Number of tests which the harness will try to run.  Greater than or
     * equal to zero and less than or equal to the total number of tests in
     * the testsuite.
     * @see #isRunning()
     */
    public int getTestsFoundCount() {
        if (raTestIter == null) {
            return 0;
        }

        synchronized (raTestIter) {
            return raTestIter.getUsedElementCount() + raTestIter.getOutputQueueSize();
        }
    }

    /**
     * Returns test tree iterator containing useful info and stats about the test run
     * @return test tree iterator containing test run data
     */
    public TreeIterator getTestIterator() {
        return testIter;
    }

    /**
     * Set the threshold for automatic halting of a test run.
     * The current algorithm is to begin at zero, add one for every failure,
     * five for every error and subtract two for each pass.  This value must be
     * set before the run begins, do not change it during a run.
     *
     * @see #getAutostopThreshold
     */
    public void setAutostopThreshold(int n) {
        autostopThreshold = n;
    }

    /**
     * @see #setAutostopThreshold
     */
    public int getAutostopThreshold(int n) {
        return autostopThreshold;
    }

    /**
     * Start a worker thread going to perform run tests asynchronously.
     */
    private synchronized void startWorker(final Parameters p) throws Fault {
        if (worker != null) {
            throw new Fault(i18n, "harness.alreadyRunning");
        }

        worker = new Thread(() -> {
            boolean ok = false;
            try {
                ok = runTests(p, ZERO_TESTS_ERROR);
            } catch (Fault | TestSuite.Fault e) {
                notifyLocalizedError(e.getMessage());
            } finally {
                synchronized (Harness.this) {
                    worker = null;
                    Harness.this.notifyAll();
                }

                notifier.finishedTestRun(ok);
            }
        });

        worker.setName("Harness:Worker");
        worker.setPriority(Thread.NORM_PRIORITY - 2); // below AWT!
        worker.start();
    }

    /**
     * This method is the one that does the work and runs the tests. Any parameters
     * should have been set up in the constructor.
     *
     * @return The result is `true' if and only if all tests passed.
     */
    // This methods notifies observers for startingTestRun and stoppingTestRun.
    // The caller should notify finishedTestRun when it is OK to run start again
    // (i.e. when worker has been reset to null.
    protected TestRunner prepareTestRunner(Parameters p) throws Fault, TestSuite.Fault{
        stopping = false;
        startTime = System.currentTimeMillis();
        testsStartTime = -1L;
        cleanupFinishTime = -1L;
        finishTime = -1L;
        numTestsDone = 0;

        if (!p.isValid()) {
            throw new Harness.Fault(i18n, "harness.incompleteParameters",
                    p.getErrorMessage());
        }
        params = p;

        // get lots of necessary values from parameters
        testSuite = params.getTestSuite();
        workDir = params.getWorkDirectory();
        resultTable = workDir.getTestResultTable();
        excludeList = params.getExcludeList();

        workDir.log(i18n, "harness.starting");

        // for compatibility with scripts that expect the timeout factor
        // to be an integer, we write out the timeout factor as both an
        // integer and a floating point number. The integer value is
        // determined by rounding up the floating point number.
        float tf = params.getTimeoutFactor();
        if (Float.isNaN(tf)) {
            tf = 1.0f;
        }

        String[] timeoutFactors = {
                String.valueOf((int) Math.ceil(tf)),
                String.valueOf(tf)
        };

        env = params.getEnv();
        env.put("javatestTimeoutFactor", timeoutFactors);
        env.putUrlAndFile("javatestClassDir", classDir);
        env.putUrlAndFile("harnessClassDir", classDir);  // backwards compatibility
        env.putUrlAndFile("javatestWorkDir", workDir.getRoot());

        // allow architect to reset root of TS in env. if needed
        // esp. for backwards compatibility with JT 2.x test suites
        String altTSRoot = testSuite.getTestSuiteInfo("env.tsRoot");
        // need to validate alt and change into File
        File atsr = altTSRoot == null ? null : new File(altTSRoot);

        if (atsr != null && atsr.exists()) {
            env.putUrlAndFile("testSuiteRoot", atsr);
            env.putUrlAndFile("testSuiteRootDir",
                    atsr.isDirectory() ? atsr : atsr.getParentFile());
        } else {
            // normal case
            env.putUrlAndFile("testSuiteRoot", testSuite.getRoot());
            env.putUrlAndFile("testSuiteRootDir", testSuite.getRootDir());
        }


        // notify the test suite we are starting
        testSuite.starting(this);
        notifier.startingTestRun(params);

        testIter = createTreeIterator();
        raTestIter = getTestsIterator(testIter);

        // autostopThreshold is currently defined by a system property,
        // but could come from parameters
        if (autostopThreshold > 0) {
            addObserver(new Autostop(autostopThreshold));
        }

        TestRunner r = testSuite.createTestRunner();
        r.setWorkDirectory(workDir);
        r.setBackupPolicy(backupPolicy);
        r.setEnvironment(env);
        r.setExcludeList(excludeList);

        int concurrency = params.getConcurrency();
        concurrency = Math.max(1, Math.min(concurrency,
                Parameters.ConcurrencyParameters.MAX_CONCURRENCY));
        r.setConcurrency(concurrency);

        r.setNotifier(notifier);
        return r;
    }

    /**
     * This method is the one that does the work and runs the tests. Any parameters
     * should have been set up in the constructor.
     *
     * @return The result is `true' if and only if all tests passed.
     */
    // This methods notifies observers for startingTestRun and stoppingTestRun.
    // The caller should notify finishedTestRun when it is OK to run start again
    // (i.e. when worker has been reset to null.
    private boolean runTests(Parameters p, boolean zeroTestsOK)
            throws Fault, TestSuite.Fault {
        TestRunner r = prepareTestRunner(p);
        TestURLCollector testURLCollector = new TestURLCollector();
        notifier.addObserver(testURLCollector);
        testsStartTime = System.currentTimeMillis();
        boolean ok = true; // default return/finished notification value
        try {
            ok = r.runTests(new Iterator<TestDescription>() {
                @Override
                public boolean hasNext() {
                    return stopping ? false : raTestIter.hasNext();
                }

                @Override
                public TestDescription next() {
                    TestResult tr = raTestIter.next();
                    try {
                        return tr.getDescription();
                    } catch (TestResult.Fault e) {
                        stopping = true;
                        throw new JavaTestError(i18n, "harness.trProb", tr.getWorkRelativePath(), e);
                    }
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            });
        } catch (InterruptedException e) {
            // swallow interrupts, because we're just going to wind up the run
        }
        notifier.removeObserver(testURLCollector);

        finishTime = System.currentTimeMillis();

        // calling only this version of ::finishedTesting - implemented by Notifier,
        // it performs further notification of observers.
        // The no-arg version of this method is not overridden by Notifier - so does nothing.
        notifier.finishedTesting(testIter);

        // calculate number of tests executed
        // NOTE: the stats here don't indicate what the results of the test run were
        int[] stats = testIter.getResultStats();
        int iteratorCount = 0;
        for (int stat : stats) {
            iteratorCount += stat;
        }

        if (iteratorCount == 0 && !zeroTestsOK) {
            TestFilter[] filters = params.getFilters();
            // no tests are in the error, pass, fail categories -> none selected
            notifyError(i18n, "harness.noTests",
                    formatFilterList(listFilterNames(filters)), testIter.getRejectCount(), formatFilterStats(params.getTests(), testIter));
            ok = false;
        } else {
            /* user is notified of this in real-time
               although it may not be evident in batch mode
            if (resultTable.getTestFinder().getErrorCount() > 0) {
                notifyError(i18n, "harness.finderError");
                ok = false;
            }
            */
        }

        if (ok && (notifier.getErrorCount() > 0 || notifier.getFailedCount() > 0)) {
            ok = false;
        }

        try {
            LastRunInfo.writeInfo(workDir, startTime, finishTime,
                    env.getName(), testURLCollector.testURLs);
        } catch (IOException e) {
            // ignore
        }

        // TRT may need to reread the entire cache
        resultTable.waitUntilReady();

        workDir.log(i18n, "harness.done", Integer.valueOf(ok ? 0 : 1));
        cleanupFinishTime = System.currentTimeMillis();
        return ok;
    }

    public ReadAheadIterator<TestResult> getTestsIterator(TreeIterator iter) throws Fault {
        if (iter == null) {
            iter = createTreeIterator();
        }
        return new ReadAheadIterator<>(iter, readAheadMode, DEFAULT_READ_AHEAD);
    }

    private TreeIterator createTreeIterator() throws Fault {
        // get items required to select the tests to be run
        String[] tests = params.getTests();
        TestFilter[] filters = params.getFilters();

        resultTable.waitUntilReady();

        TreeIterator iter;

        // get the appropriate iterator from TRT
        if (tests == null || tests.length == 0) {
            iter = resultTable.getIterator(filters);
        } else {
            try {
                // CLEANUP REQUIRED: validation only occurs on Files, not Strings
                // resultTable.getIterator should validate strings too
                File[] files = new File[tests.length];
                for (int i = 0; i < tests.length; i++) {
                    files[i] = new File(tests[i]);
                }
                iter = resultTable.getIterator(files, filters);
            } catch (TestResultTable.Fault err) {
                throw new Harness.Fault(i18n, "harness.badInitFiles",
                        err.getMessage());
            }
        }

        return iter;
    }

    private void notifyError(I18NResourceBundle i18n, String key) {
        notifyLocalizedError(i18n.getString(key));
    }

    private void notifyError(I18NResourceBundle i18n, String key, Object arg) {
        notifyLocalizedError(i18n.getString(key, arg));
    }

    private void notifyError(I18NResourceBundle i18n, String key, Object... args) {
        notifyLocalizedError(i18n.getString(key, args));
    }

    private void notifyLocalizedError(String msg) {
        notifier.error(msg);
    }

    /**
     * Informs all the observers of the final stats gathered after the test run
     * @param filterStats the final statistics about happened test filtering
     * @param stats status type is the number of element, value is the number or tests with that type of status
     */
    public void notifyOfTheFinalStats(Map<TestFilter, List<TestDescription>> filterStats, int... stats) {
        notifier.notifyOfTheFinalStats(filterStats, stats);
    }

    /**
     * This interface provides a means for Harness to report
     * on events that might be of interest as it executes.
     */
    public interface Observer {
        /**
         * The harness is beginning to execute tests.
         * Default implementation does nothing.
         *
         * @param params the parameters for the test run
         */
        default void startingTestRun(Parameters params) {}

        /**
         * The harness is about to run the given test.
         * Default implementation does nothing.
         *
         * @param tr The test result which is going to receive the data
         *           from the current execution of that test.
         */
        default void startingTest(TestResult tr) {}

        /**
         * The harness has finished running the given test.
         * This message is sent without respect to the resulting test's
         * completion status (pass, fail, etc...).
         * Default implementation does nothing.
         *
         * @param tr The result object containing the results from the
         *           execution which was just completed.
         */
        default void finishedTest(TestResult tr) {}

        /**
         * The harness is about to stop a test run, before it has finished
         * executing all the specified tests. The method is not notified if
         * the test run completes normally, after executing all the specified
         * tests.
         * Default implementation does nothing.
         */
        default void stoppingTestRun() {}

        /**
         * The harness has finished running tests and is doing other activities
         * (writing the report, updating caches, etc...).  This message will
         * be broadcast both when error conditions terminate the run or when
         * a test completes normally. It may provide a reasonable opportunity
         * for a client to clean up any resources that were used during the test
         * run, before a new run is started.
         * Default implementation does nothing.
         * @deprecated it is recommended to override {@code finishedTesting(TestResultTable.TreeIterator)} instead
         */
        default void finishedTesting() {}

        /**
         * An enhanced new version of {@code finishedTesting} method
         * that allows to evaluate given {@code TestResultTable.TreeIterator} instance and get
         * any extra info about the finished test run.
         * Both versions of this method would be called after test run is finished.
         * Default implementation does nothing.
         */
        default void finishedTesting(TestResultTable.TreeIterator treeIterator) {}

        /**
         * The test run has been completed, either because the user requested
         * that the harness stop, the harness decided to terminate the test run,
         * or all requested tests have been run.  The harness is now ready to
         * perform another test run. Note that since the actions of other observers
         * are undefined, a new test run may have already been started by the time
         * this method is called for any specific observer.
         * Default implementation does nothing.
         *
         * @param allOK True if all tests passed, false otherwise.
         */
        default void finishedTestRun(boolean allOK) {}

        /**
         * The given error occurred.
         * Default implementation does nothing.
         *
         * @param msg A description of the error event.
         */
        default void error(String msg) {}

        /**
         * Is called when final test run stats are available for evaluation.
         * @param filterStats the final statistics about happened test filtering
         * @param stats status type is the number of element, value is the number or tests with that type of status
         */
        default void notifyOfTheFinalStats(Map<TestFilter, List<TestDescription>> filterStats, int... stats) {}
    }

    /**
     * This exception is used to report problems while executing a test run.
     */
    public static class Fault extends Exception {
        Fault(I18NResourceBundle i18n, String s) {
            super(i18n.getString(s));
        }

        Fault(I18NResourceBundle i18n, String s, Throwable cause) {
            super(i18n.getString(s), cause);
        }

        Fault(I18NResourceBundle i18n, String s, Object o) {
            super(i18n.getString(s, o));
        }

        Fault(I18NResourceBundle i18n, String s, Object... o) {
            super(i18n.getString(s, o));
        }
    }

    /**
     * Class that collects executed tests
     */
    static class TestURLCollector implements Harness.Observer {
        final List<String> testURLs = new ArrayList<>();

        TestURLCollector() {
        }

        @Override
        public synchronized void startingTest(TestResult tr) {
            testURLs.add(tr.getTestName());
        }

    }

    private class Notifier implements Harness.Observer {
        private Observer[] observers = new Observer[0];
        private volatile int errCount, failCount;

        void addObserver(Observer o) {
            if (o == null) {
                throw new NullPointerException();
            }
            observers = DynamicArray.append(observers, o);
        }

        void removeObserver(Observer o) {
            observers = DynamicArray.remove(observers, o);
        }

        @Override
        public void startingTestRun(Parameters params) {
            resultTable.starting();

            // protect against removing observers during notification
            Observer[] stableObservers = observers;
            for (int i = stableObservers.length - 1; i >= 0; i--) {
                stableObservers[i].startingTestRun(params);
            }
        }

        @Override
        public void startingTest(TestResult tr) {
            // protect against removing observers during notification
            Observer[] stableObservers = observers;
            for (int i = stableObservers.length - 1; i >= 0; i--) {
                stableObservers[i].startingTest(tr);
            }
        }

        @Override
        public void finishedTest(TestResult tr) {
            numTestsDone++;
            resultTable.update(tr);
            // protect against removing observers during notification
            Observer[] stableObservers = observers;
            for (int i = stableObservers.length - 1; i >= 0; i--) {
                stableObservers[i].finishedTest(tr);
            }

            switch (tr.getStatus().getType()) {
                case Status.FAILED:
                    synchronized (this) {
                        failCount++;
                    }
                    break;
                case Status.ERROR:
                    synchronized (this) {
                        errCount++;
                    }
                    break;
                // XXX possibility exists for NOT_RUN, this should also
                //     be recorded as a problem, or is it a "problem"?
                default:
            }   // switch
        }

        @Override
        public void stoppingTestRun() {
            // protect against removing observers during notification
            Observer[] stableObservers = observers;
            for (int i = stableObservers.length - 1; i >= 0; i--) {
                stableObservers[i].stoppingTestRun();
            }
        }

        @Override
        public void finishedTesting(TestResultTable.TreeIterator treeIterator) {
            resultTable.finished();

            // protect against removing observers during notification
            Observer[] stableObservers = observers;
            for (int i = stableObservers.length - 1; i >= 0; i--) {
                stableObservers[i].finishedTesting();
                stableObservers[i].finishedTesting(treeIterator);
            }
        }

        @Override
        public void finishedTestRun(boolean allOK) {
            // protect against removing observers during notification
            Observer[] stableObservers = observers;
            for (int i = stableObservers.length - 1; i >= 0; i--) {
                stableObservers[i].finishedTestRun(allOK);
            }
        }

        @Override
        public void error(String msg) {
            // protect against removing observers during notification
            Observer[] stableObservers = observers;
            for (int i = stableObservers.length - 1; i >= 0; i--) {
                stableObservers[i].error(msg);
            }
        }

        @Override
        public void notifyOfTheFinalStats(Map<TestFilter, List<TestDescription>> filterStats, int... stats) {
            // protect against removing observers during notification
            Observer[] stableObservers = observers;
            for (int i = stableObservers.length - 1; i >= 0; i--) {
                stableObservers[i].notifyOfTheFinalStats(filterStats, stats);
            }
        }

        synchronized int getErrorCount() {
            return errCount;
        }

        synchronized int getFailedCount() {
            return failCount;
        }
    }

    class Autostop implements Harness.Observer {
        private int level;
        private int threshold;

        Autostop(int threshold) {
            this.threshold = threshold;
        }

        @Override
        public void finishedTest(TestResult tr) {
            switch (tr.getStatus().getType()) {
                case Status.FAILED:
                    level++;
                    break;
                case Status.ERROR:
                    level += 5;
                    break;
                default:
                    level = Math.max(level - 2, 0);
            }
            if (level >= threshold) {
                Harness.this.notifyError(i18n, "harness.tooManyErrors");
                stop();
            }
        }

    }
}
