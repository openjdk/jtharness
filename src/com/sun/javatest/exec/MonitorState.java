/*
 * $Id$
 *
 * Copyright (c) 2001, 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.exec;

import java.awt.EventQueue;
import java.util.ArrayList;

import com.sun.javatest.Harness;
import com.sun.javatest.Parameters;
import com.sun.javatest.Status;
import com.sun.javatest.TestResult;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.util.DynamicArray;

/**
 * This class captures the state of the Harness for the purpose of
 * presenting the user with useful status/progress info.
 */

class MonitorState {
    MonitorState(Harness h) {
        h.addObserver(dispatcher);
        harness = h;
        startTime = -1l;
        finishTime = -1l;
        stats = new int[Status.NUM_STATES];
    }

    /**
     * Monitor the harness state.
     * By definition, all observations will be broadcast on the event thread.
     * Be short and timely in processing these events.
     */
    interface Observer {
        /**
         * A new test run is starting.
         */
        public void starting();

        /**
         * The tests have stopped running, and the harness is now doing
         * cleanup.
         */
        public void postProcessing();

        /**
          * A test run is being stopped by something.  This is not the same
          * as finishing.
          */
        public void stopping();

        /**
         * A test run is finishing.
         *
         * @param allOk Did all the tests pass?
         */
        public void finished(boolean allOk);
    }

    void addObserver(Observer o) {
        obs = (Observer[])DynamicArray.append(obs, o);
    }

    void removeObserver(Observer o) {
        obs = (Observer[])DynamicArray.remove(obs, o);
    }

    /**
     * Is the harness currently doing a test run.
     */
    boolean isRunning() {
        return running;
    }

    /**
     * Time since the start of the run.
     *
     * @return Zero at the instant we start a run or when no run has been
     *         performed yet.
     */
    long getElapsedTime() {
        /*
        // we don't have a valid time available
        if (startTime == -1)
            return 0L;

        // provide the current or last time
        long now = System.currentTimeMillis();

        // answer depends whether we are running now or not
        if (finishTime < 0) {
            // we are still running
            return now - startTime;
        }
        else {
            return finishTime - startTime;
        }
        */

        return harness.getElapsedTime();
    }

    /**
     * Find out the estimated time required to complete the remaining tests.
     *
     * @return A time estimate in milliseconds.  Zero if no run is in progress or
     *         no estimate is available.
     */
    synchronized long getEstimatedTime() {
        return harness.getEstimatedTime();
    }

    /**
     * Discover the number of tests found so far in this testsuite.
     *
     * @return estimated number of tests in the entire testsuite, -1 if unknown
     */
    synchronized int getEstimatedTotalTests() {
        WorkDirectory wd = harness.getResultTable().getWorkDir();
        if (wd == null)
            return -1;

        int count = wd.getTestSuiteTestCount();

        if (count == -1)
            count = wd.getTestSuite().getEstimatedTestCount();

        // make sure we return -1
        return (count < 0 ? -1 : count);
    }

    /**
     * Discover the number of tests found so far by the system to be executed
     * during this run.
     *
     * @return Number of tests found so far to be executed based on the current
     *         parameters.
     */
    synchronized int getTestsFoundCount() {
        return harness.getTestsFoundCount();
    }

    /**
     * Convenience method to determine how many tests have finished for the
     * current or last run.
     */
    synchronized int getTestsDoneCount() {
        int done = 0;
        for (int i = 0; i < stats.length; i++)
            done += stats[i];

        return done;
    }

    /**
     * Convenience method to determine how many tests have still need
     * to be run for the current or last run.
     */
    synchronized int getTestsRemainingCount() {
        return getTestsFoundCount() - getTestsDoneCount();
    }

    /**
     * Get the test run statistics.
     *
     * @return array of counts, based on the indexes of Status types.
     */
    synchronized int[] getStats() {
        // we could consider trusting the client and not doing the copy
        int[] copy = new int[stats.length];
        System.arraycopy(stats, 0, copy, 0, stats.length);
        return copy;
    }

    /**
     * @return Array representing the currently executing tests. Zero-length
     *     array if there are no tests running.
     */
    TestResult[] getRunningTests() {
        TestResult[] trs = new TestResult[0];
        synchronized (vLock) {
            if (runningTests != null && runningTests.size() > 0)
                trs = runningTests.toArray(trs);
        }

        return trs;
    }

    /**
     * Provide synchronized access to the statistics array.
     */
    private synchronized void incrementStat(int state) {
        // this mainly called by the harness observer implementation
        stats[state]++;
    }

    private synchronized void resetStats() {
        stats = new int[Status.NUM_STATES];
    }

    class Dispatcher implements Harness.Observer {

        public void startingTestRun(Parameters p) {
            running = true;

            startTime = System.currentTimeMillis();
            finishTime = -1l;
            resetStats();

            synchronized (vLock) {
                runningTests.clear();
            }

            notifySimple(0);
        }

        public void startingTest(TestResult tr) {
            synchronized (vLock) {
                if (!runningTests.contains(tr))
                    runningTests.add(tr);
            }
        }

        public void finishedTest(TestResult tr) {
            synchronized (vLock) {
                runningTests.remove(tr);
            }

            incrementStat(tr.getStatus().getType());
        }

        public void stoppingTestRun() {
            notifySimple(1);
        }

        public void finishedTesting() {
            finishTime = System.currentTimeMillis();

            synchronized (vLock) {
                runningTests.clear();
            }

            notifySimple(2);
        }

        public void finishedTestRun(boolean allOk) {
            running = false;
            notifyComplete(allOk);
        }

        public void error(String msg) {
        }

        // --------- private ----------
        private void notifySimple(final int which) {
            if (!EventQueue.isDispatchThread()) {
                Runnable cmd = new Runnable() {
                    public void run() {
                        MonitorState.Dispatcher.this.notifySimple(which);
                    }   // run()
                };      // end anon. class

                EventQueue.invokeLater(cmd);
            }
            else {      // now on event thread
                for (int i = 0; i < obs.length; i++)
                    switch (which) {
                        case 0:
                            obs[i].starting(); break;
                        case 1:
                            obs[i].stopping(); break;
                        case 2:
                            obs[i].postProcessing(); break;
                        default:
                            throw new IllegalStateException();
                    }   // switch
                // end for
            }   // else
        }

        private void notifyComplete(final boolean allOk) {
            if (!EventQueue.isDispatchThread()) {
                Runnable cmd = new Runnable() {
                    public void run() {
                        MonitorState.Dispatcher.this.notifyComplete(allOk);
                    }   // run()
                };      // end anon. class

                EventQueue.invokeLater(cmd);
            }
            else {      // now on event thread
                for (int i = 0; i < obs.length; i++)
                    obs[i].finished(allOk);
            }
        }
    }

    // instance vars for MonitorState
    private Harness harness;
    private boolean running;
    private Observer[] obs = new Observer[0];
    private volatile int[] stats;

    private ArrayList<TestResult> runningTests = new ArrayList(5);
    private final Object vLock = new Object();

    /**
     * Basis for the elapsed time.
     */
    private long startTime;
    private long finishTime;    // only used if startTime != -1 and finishTime >= 0
    private Dispatcher dispatcher = new Dispatcher();
}

