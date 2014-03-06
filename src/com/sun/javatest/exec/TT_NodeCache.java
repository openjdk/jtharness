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

import java.util.Hashtable;
import java.util.Vector;

import com.sun.javatest.Status;
import com.sun.javatest.TestDescription;
import com.sun.javatest.TestFilter;
import com.sun.javatest.TestResult;
import com.sun.javatest.TestResultTable;
import com.sun.javatest.util.Debug;
import com.sun.javatest.util.DynamicArray;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cached information about a particular tree node.
 * We want to collect pass/fail stats and other variable information so it can
 * be used to render a node quickly when requested by JTree.  Code here should
 * never run on the GUI thread.
 *
 * <p>
 * Objects of this type run to completion and are then immutable.
 */
class TT_NodeCache implements Runnable {

    /**
     * Construct a cache object which will collect info about the given node
     * with respect to the supplied filter.
     *
     * @param n Node to examine, should not be null.
     * @param f Filter to use when producing information.  Can be null.
     */
    TT_NodeCache(TestResultTable.TreeNode n, TestFilter f, Logger l) {
        filter = f;
        node = n;
        log = l;

        // all the states plus filtered out
        testLists = new Vector[Status.NUM_STATES + 1];
        for (int i = 0; i < Status.NUM_STATES + 1; i++) {
            testLists[i] = new Vector();
        }
    }

    /**
     * Start or resume processing.
     */
    public void run() {
        if (debug) {
            Debug.println("TT_NodeCache starting");
            Debug.println("   -> " + this);
            Debug.println("   -> node " + node + "(" + node.getName() + ")");
            Debug.println("   -> filter=" + filter);
            Debug.println("   -> old state=" + state);
        }

        // needed for a new run, a paused object will already have one
        if (it == null) {
            it = init();
        }

        if (debug) {
            Debug.println("   -> iterator= " + it);
        }

        state = COMPUTING;

        process();
    }

    /**
     * Pause processing, and return immediately.
     * If the node has completed processing, calling this method has no
     * effect.
     * Be careful with MT activities when using <tt>pause() resume() isPaused()</tt>.
     * @see #resume()
     */
    void pause() {
        if (state < COMPLETED) {
            state = PAUSED;
            if (debug) {
                Debug.println("TT_NodeCache for " + node.getName() + " pausing...");
            }
        }
    }

    /**
     * Continue processing after a pause.
     * @see #pause
     * @throws IllegalStateException if <tt>pause()</tt> was not previously called.
     */
    void resume() {
        if (state != PAUSED) {
            throw new IllegalStateException("Cache node not previously paused.");
        }
        if (debug) {
            Debug.println("TT_NodeCache for " + node.getName() + " resuming...");
        }
        state = COMPUTING;
        process();
    }

    /**
     * @return True if the replacement has an effect on the statistics.  False
     *         if it does not.
     */
    synchronized boolean add(TestResultTable.TreeNode[] path, TestResult what, int index) {
        boolean result = false;
        boolean wouldAccept = false;     // special case because of filtering
        boolean needsProcessing = false;

        // not even running yet
        if (it == null) {
            return false;
        }

        try {
            TestDescription td = what.getDescription();
            TestFilter rejector = null;

            synchronized (fObs) {
                wouldAccept = filter.accepts(what, fObs);
                if (!wouldAccept && fObs.lastTd == td) {
                    rejector = fObs.lastRejector;
                    fObs.clear();
                }
            }   // sync

            needsProcessing = (!it.isPending(what));

            if (!needsProcessing) {
                // It's still going to come out of the iterator.
                // Make sure it's not the one which is committed to be next.
                // Would be nice to work around this in a different way, either
                // here or in the iterator.
                Object peek = it.peek();
                if (peek instanceof TestResult &&
                        ((TestResult) peek).getTestName().equals(what.getTestName())) {
                    it.next();      // consume
                    needsProcessing = true;
                } else {
                }
            } else {
            }

            if (needsProcessing) {
                if (!wouldAccept) {
                    // add to filtered out list
                    localRejectCount++;
                    testLists[testLists.length - 1].add(what);
                    rejectReasons.put(what, rejector);
                } else {
                    int type = what.getStatus().getType();

                    // unfortunately the add/remove messages don't seem to
                    // be 100% symmetric, or else there is some other race
                    // condition.  I tried to find the problem, but 0-5 tests out
                    // of 10000 will end up added twice when loading a workdir at
                    // startup. 12/9/2002
                    if (!testLists[type].contains(what)) {
                        stats[type]++;
                        testLists[type].add(what);
                    } else {
                    }
                }

                result = true;
            } else {    // will be counted later...
                result = false;
            }

            // send out notifications if needed
            notify((wouldAccept ? what.getStatus().getType() + TT_NodeCacheObserver.OFFSET_FROM_STATUS
                    : TT_NodeCacheObserver.MSGS_FILTERED),
                    true, path, what, index);

            if (result) {
                notifyStats();
            }
        } catch (TestResult.Fault f) {

            //if (debug) {
            //    f.printStackTrace(Debug.getWriter());
            //    Debug.println(msg);
            // }

            if (log != null && log.isLoggable(Level.SEVERE)) {
                String msg = "TT_NodeCache - TR fault, purging old info. " + what.getTestName();
                log.log(Level.SEVERE, msg, f);
            }

            // TR is somehow corrupt, remove it
            node.getEnclosingTable().resetTest(what.getTestName());
            result = false;
        } catch (TestFilter.Fault f) {

            f.printStackTrace(Debug.getWriter());

            if (log != null && log.isLoggable(Level.SEVERE)) {
                log.log(Level.SEVERE, "TT_NodeCache - filter is broken", f);
            }

            // filter is broken, shove everything into filtered out.
            // this behavior is similar to what would happen in the same
            // case when the harness is iterating to select tests to run.
            // trying to only do this if counting of this test would have
            // been necessary.
            if (needsProcessing) {
                localRejectCount++;
                testLists[testLists.length - 1].add(what);
            // this reject will not have a reason entry in
            // rejectReasons
            }

            // ignore error and don't do anything
            result = false;
        }

        return result;
    }

    /**
     * @return True if the replacement has an effect on the statistics.  False
     *         if it does not.
     */
    synchronized boolean remove(TestResultTable.TreeNode[] path, TestResult what, int index) {
        boolean result = false;
        // special case because of filtering

        // not even running yet
        if (it == null) {
            return false;
        }

        int type = what.getStatus().getType();

        if (!it.isPending(what)) {          // check iterator position
            int[] rmList = locateTestInLists(what, type, -1);

            if (rmList[0] != -1) {
                testLists[rmList[0]].remove(rmList[1]);

                // decrement counter
                if (rmList[0] < stats.length) {
                    stats[rmList[0]]--;
                } else {
                    localRejectCount--;
                }

                // send out notifications if needed
                notify(rmList[0] + TT_NodeCacheObserver.OFFSET_FROM_STATUS, false, path, what, index);
                result = true;
            } else {
            }
        } else {        // pending in iterator
            Object peek = it.peek();
            if (peek instanceof TestResult &&
                    ((TestResult) peek).getTestName().equals(what.getTestName())) {
                it.next();          // consume

                // do it again to make it happen
                result = remove(path, what, index);
            }

        }

        if (result) {
            notifyStats();
        }

        return result;
    }

    /**
     * @return True if the replacement has an effect on the statistics.  False
     *         if it does not.
     */
    synchronized boolean replace(TestResultTable.TreeNode[] path, TestResult what,
            int index, TestResult old) {
        boolean result = false;
        // not even running yet
        if (it == null) {
            return false;
        }

        if (!it.isPending(what)) {          // check iterator position
            int typeNew = what.getStatus().getType();
            int typeOld = old.getStatus().getType();

            // filtering of old does not work because of the status filter
            boolean wouldAcceptNew = false;
            TestFilter rejector = null;

            try {
                TestDescription td = what.getDescription();

                synchronized (fObs) {
                    wouldAcceptNew = filter.accepts(what, fObs);
                    if (!wouldAcceptNew && fObs.lastTd == td) {
                        rejector = fObs.lastRejector;
                        fObs.clear();
                    }
                }       // sync
            } // try
            catch (TestResult.Fault f) {

                f.printStackTrace(Debug.getWriter());
                if (log != null && log.isLoggable(Level.SEVERE)) {
                    log.log(Level.SEVERE, "TT_NodeCache - problem with test result", f);
                }

                // ignore error and don't do anything
                return false;
            } // catch
            catch (TestFilter.Fault f) {

                f.printStackTrace(Debug.getWriter());
                if (log != null && log.isLoggable(Level.SEVERE)) {
                    log.log(Level.SEVERE, "TT_NodeCache - filter is broken", f);
                }


                // ignore error and don't do anything
                return false;
            }   // catch

            // inserting into one of the status lists or the filtered out list
            int targetList = (wouldAcceptNew ? typeNew : testLists.length - 1);
            int[] rmList = null;

            // optimization to search based on expected location of old test
            // and optimize out null changes
            if (what == old) {
                if (typeOld != typeNew) {       // special case optimization
                    // no change to lists
                    rmList = new int[]{-1, -1};
                } else {
                    rmList = locateTestInLists(old, Status.NOT_RUN, targetList);

                    if (rmList[0] == targetList) {
                        // same TR object, same status
                        // use -1 to specify no remove action
                        rmList[0] = -1;
                        rmList[1] = -1;
                    }
                }
            } else {
                rmList = locateTestInLists(old, typeOld, targetList);
            }

            if (rmList[0] != -1) {
                testLists[rmList[0]].remove(rmList[1]);
                testLists[targetList].add(what);

                // decrement counter
                if (rmList[0] < stats.length) {
                    stats[rmList[0]]--;
                } else {
                    localRejectCount--;
                    rejectReasons.remove(what);
                }

                // increment counter
                if (targetList < stats.length) {
                    stats[targetList]++;
                } else {
                    localRejectCount++;
                    rejectReasons.put(what, rejector);
                }

                // remove the old
                // add the new
                notify(rmList[0] + TT_NodeCacheObserver.OFFSET_FROM_STATUS,
                        false, path, old, index);
                notify(targetList + TT_NodeCacheObserver.OFFSET_FROM_STATUS,
                        true, path, what, index);

                result = true;
            } else {
                // must be a null change
            }
        } else // this TR has yet to be processed
        {
            result = false;
        }

        if (result) {
            notifyStats();
        }

        return result;
    }

    boolean isPaused() {
        return (state == PAUSED);
    }

    /**
     * Is this object eligible to run.
     * This translates to being either unprocessed or paused.
     */
    boolean canRun() {
        return ((state == PAUSED || state == NOT_COMPUTED) &&
                valid == true);
    }

    void halt() {
        if (debug) {
            Debug.println("TT_NodeCache thread stopping");
            Debug.println("   -> " + this);
        }

        state = ABORTED;
        valid = false;
    }

    /**
     * Is info still being collected?
     * The state is not considered active if the processing is paused.
     *
     * @return True if information is incomplete, false otherwise.
     * @see #isPaused()
     */
    boolean isActive() {
        return (state == COMPUTING);
    }

    /**
     * Has all the information been collected.
     * @return True if all information is up to date, and will not change
     *         unless this node is invalidated.
     */
    boolean isComplete() {
        return (state == COMPLETED);
    }

    boolean isAborted() {
        return (state == ABORTED);
    }

    /**
     * Invalidate any information in this node cache.
     * @see #isValid()
     */
    void invalidate() {
        valid = false;
    }

    /**
     * Has the data in this node been invalidated.
     * A node may be valid while it is active, but becomes invalid when
     * notified that the constraints that it is executing with are no longer
     * correct.  It may also become invalid if the thread is interrupted.
     * @see #invalidate()
     */
    boolean isValid() {
        return valid;
    }

    TestResultTable.TreeNode getNode() {
        return node;
    }

    TestFilter getFilter() {
        return filter;
    }

    /**
     * Get the pass fail error notrun stats.
     * The data may be in flux if the data is still being collected, use
     * <tt>isActive()</tt> to anticipate this.
     * @return An array of size Status.NUM_STATES.  This is not a copy, do not
     *         alter.
     * @see #isActive()
     * @see com.sun.javatest.Status#NUM_STATES
     */
    int[] getStats() {
        return stats;
    }

    /**
     * Find out how many tests were rejected by filters.
     * @return Number of rejected tests found in and below this node.
     */
    int getRejectCount() {
        if (it != null) {
            return it.getRejectCount() + localRejectCount;
        } else {
            return localRejectCount;
        }
    }

    TestFilter getRejectReason(TestResult tr) {
        return (TestFilter) (rejectReasons.get(tr));
    }

    /**
     * Snapshot the current data and add an observer.
     * This is an atomic operation so that you can get completely up to date
     * and monitor all changes going forward.
     *
     * @param obs The observer to attach.  Must not be null.
     * @param needSnapshot Does the caller want a snapshot of the current test lists.
     *        True if yes, false if not.
     * @return A copy of the Vectors that contain the current list of tests.  Null if
     *         <tt>needSnapshot</tt> is false.
     */
    synchronized Vector[] addObserver(TT_NodeCacheObserver obs, boolean needSnapshot) {
        // snapshot the current data
        // must be done before adding the observer to ensure correct data
        // delivery to client

        Vector[] cp = null;
        if (needSnapshot) {
            cp = new Vector[testLists.length];
            for (int i = 0; i < testLists.length; i++) {
                cp[i] = (Vector) (testLists[i].clone());
            }
        }

        if (obs != null) {
            observers = (TT_NodeCacheObserver[]) DynamicArray.append(observers, obs);
        }

        return cp;
    }

    // advisory - many thread access this.  you should lock this object before
    // calling.  See BP_TestListSubpanel.reset(TT_NodeCache), which locks this,
    // THEN itself (the GUI component) for proper locking sequence, since the
    // highest contention is for this cache object.
    synchronized void removeObserver(TT_NodeCacheObserver obs) {
        observers = (TT_NodeCacheObserver[]) DynamicArray.remove(observers, obs);
    }

    // ------------- PRIVATE -----------------
    private void process() {
        final TestResultTable trt = node.getEnclosingTable();
        if (trt == null) {
            return;
        }
        try {
            trt.getLock().lock();
            while (state != ABORTED && state != PAUSED && it.hasNext()) {
                try {
                    synchronized (node) {       // to maintain locking order
                        synchronized (this) {   // sync to lockout during add/remove/replace
                            if (!it.hasNext()) // need to recheck after locking
                            {
                                continue;
                            }

                            TestResult tr = (TestResult) it.next();
                            TestDescription td = tr.getDescription();
                            TestFilter rejector = null;
                            boolean wouldAccept = false;

                            synchronized (fObs) {
                                try {
                                    wouldAccept = filter.accepts(tr, fObs);
                                } catch (TestFilter.Fault f) {
                                    f.printStackTrace(Debug.getWriter());
                                    if (log != null && log.isLoggable(Level.SEVERE)) {
                                        log.log(Level.SEVERE, "TT_NodeCache - filter is broken", f);
                                    }

                                    // assume it is accepted, this is what would
                                    // happen for test execution as well
                                    wouldAccept = true;
                                }       // catch

                                if (!wouldAccept && fObs.lastTd == td) {
                                    rejector = fObs.lastRejector;
                                    fObs.clear();
                                }
                            }   // sync

                            if (wouldAccept) {
                                int type = tr.getStatus().getType();

                                if (!testLists[type].contains(tr)) {
                                    stats[type]++;
                                    testLists[type].add(tr);

                                    // XXX We are not producing
                                    // the all the parameters.  it seems to be overkill
                                    // for what we are currently using this event for.
                                    // Perhaps the API need to be revisited.
                                    notify(type + TT_NodeCacheObserver.OFFSET_FROM_STATUS,
                                            true, null, tr, -1);
                                } else {
                                    // duplicate, no action
                                }

                            } else {
                                // filtered out list
                                testLists[testLists.length - 1].add(tr);
                                rejectReasons.put(tr, rejector);
                                localRejectCount++;

                                // XXX We are not producing
                                // the all the parameters.  it seems to be overkill
                                // for what we are currently using this event for.
                                // Perhaps the API need to be revisited.
                                notify(TT_NodeCacheObserver.MSGS_FILTERED,
                                        true, null, tr, -1);
                            }
                        }       // sync this
                    }   // sync node
                } // try
                catch (TestResult.Fault f) {
                    f.printStackTrace(Debug.getWriter());
                    if (log != null && log.isLoggable(Level.SEVERE)) {
                        log.log(Level.SEVERE, "TT_NodeCache - problem with test result", f);
                    }

                // try to recover it?
                // ignore error and don't do anything
                }   // catch

                notifyStats();
            }   // while

            if (state != PAUSED) {
                cleanup();
            }

        } finally {
            trt.getLock().unlock();
        }
    }

    /**
     * Find a particular test in the test lists.  You can specify which lists to
     * search first or last to help mitigate the O(n) search performance.  It is
     * assumed that synchronization for access to the lists has been taken care of.
     *
     * @param tr The test to locate.
     * @param firstListToCheck Hint of where to start looking.  -1 to specify none.
     * @param lastListToCheck Hint of where the last place to look should be.
     *          -1 to specify none.
     * @return Array describing [0] which list the item was found in, [1] at
     *         what index.  If [0] is greater than -1, then so will [1].  [0]
     *         of -1 indicates that then item was not found.
     */
    private int[] locateTestInLists(TestResult tr, int firstListToCheck,
            int lastListToCheck) {
        int[] result = new int[2];
        result[0] = -1;
        result[1] = -1;

        if (firstListToCheck >= 0) {
            int possible = testLists[firstListToCheck].indexOf(tr);
            if (possible != -1) {
                // done with search
                result[0] = firstListToCheck;
                result[1] = possible;
            }
        } else {
        }

        // do a more exhaustive search if not found yet
        if (result[0] == -1) {
            for (int i = 0; i < testLists.length; i++) {
                // skip the lists which have been checked or that should be
                // checked last
                // this is just a performance optimization
                if (i == firstListToCheck || i == lastListToCheck) {
                    continue;
                }

                int possible = testLists[i].indexOf(tr);
                if (possible != -1) {
                    // found in list i, position possible
                    // done with search
                    result[0] = i;
                    result[1] = possible;
                    break;
                }
            }   // for
        }

        // if still not found, check the list which was specified to be checked
        // last, this is just a performance optimization
        if (result[0] == -1 && lastListToCheck >= 0) {
            int possible = testLists[lastListToCheck].indexOf(tr);
            if (possible != -1) {
                result[0] = lastListToCheck;
                result[1] = possible;
            }
        }

        return result;
    }

    /**
     * Determine the index of a particular test in a vector.
     */
    private int searchList(TestResult target, Vector list) {
        int possible = list.indexOf(target);
        return possible;
    }

    private void cleanup() {
        if (state == ABORTED) {
            // no valid info
            return;
        }

        state = COMPLETED;
    }

    /**
     * Prepare to process this node.
     * Avoid calling until it is time to process the node.
     * @return Iterator for this node, or null if not possible.
     */
    private TestResultTable.TreeIterator init() {
        if (node == null) {
            valid = false;
            state = ABORTED;
            return null;
        }

        return TestResultTable.getIterator(node);
    }

    private synchronized void notify(int type, boolean isAdd,
            TestResultTable.TreeNode[] path,
            TestResult what, int index) {
        if (observers.length == 0) {
            return;
        }

        for (int i = 0; i < observers.length; i++) {
            boolean[] mask = observers[i].getEventMasks();
            if (mask[0] || mask[type]) {
                if (isAdd) {
                    observers[i].testAdded(type, path, what, index);
                } else {
                    observers[i].testRemoved(type, path, what, index);
                }
            }
        }   // for
    }

    private synchronized void notifyStats() {
        if (observers.length == 0) {
            return;
        }

        for (int i = 0; i < observers.length; i++) {
            boolean[] mask = observers[i].getEventMasks();
            if (mask[0] || mask[TT_NodeCacheObserver.MSGS_STATS]) {
                observers[i].statsUpdated(stats);
            }
        }   // for
    }


    private final TestResultTable.TreeNode node;
    private final Logger log;
    private TestResultTable.TreeIterator it;
    private TestFilter filter;
    private int[] stats = new int[Status.NUM_STATES];
    private int localRejectCount;
    private Hashtable rejectReasons = new Hashtable();
    private final FilterObserver fObs = new FilterObserver();
    private TT_NodeCacheObserver[] observers = new TT_NodeCacheObserver[0];
    private Vector[] testLists;     // could use unsynchronized data structure
    private volatile int state;
    private volatile boolean valid = true;
    private static final int NOT_COMPUTED = 0;
    private static final int COMPUTING = 1;
    private static final int COMPLETED = 2;
    private static final int PAUSED = 3;
    private static final int ABORTED = 4;
    private static final boolean debug = Debug.getBoolean(TT_NodeCache.class);

    static abstract class TT_NodeCacheObserver {

        public TT_NodeCacheObserver() {
            interestList = new boolean[EVENT_LIST_SIZE];
        }

        /**
         * Find out what messages this observer is interested in.
         */
        public boolean[] getEventMasks() {
            return interestList;
        }

        public abstract void testAdded(int messageType,
                TestResultTable.TreeNode[] path, TestResult what, int index);

        public abstract void testRemoved(int messageType,
                TestResultTable.TreeNode[] path, TestResult what, int index);

        public abstract void statsUpdated(int[] stats);
        protected boolean[] interestList;
        public static final int EVENT_LIST_SIZE = 7;
        public static final int MSGS_ALL = 0;
        public static final int MSGS_STATS = 1;
        public static final int MSGS_PASSED = 2;
        public static final int MSGS_FAILED = 3;
        public static final int MSGS_ERRORS = 4;
        public static final int MSGS_NOT_RUNS = 5;
        public static final int MSGS_FILTERED = 6;
        public static final int OFFSET_FROM_STATUS = 2;
    }

    static class FilterObserver implements TestFilter.Observer {

        public void rejected(TestDescription d, TestFilter rejector) {
            lastTd = d;
            lastRejector = rejector;
        }

        public void clear() {
            lastTd = null;
            lastRejector = null;
        }
        TestDescription lastTd;
        TestFilter lastRejector;
    }

}
