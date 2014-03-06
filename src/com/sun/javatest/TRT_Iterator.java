/*
 * $Id$
 *
 * Copyright (c) 2001, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.Vector;

import com.sun.javatest.util.Debug;
import com.sun.javatest.util.I18NResourceBundle;


/**
 * This class processes the iteration with the following priorities:
 * <ol>
 * <li>return any tests which were explicitly supplied at construction time
 * <li>serially exhaust all tests underneath each node specified at construction time
 * <li>return tests found by walking the tree in an ``in-order'' manner
 * </ul>
 * The iterator operates by immediately returning explicit tests.  If not explicit
 * tests are available, the ``next'' methods always keeps one ahead of the current
 * request so that it can answer the ``has more?'' question.  On each ``next'' request,
 * it searches for the next test it can distribute.
 */
class TRT_Iterator implements TestResultTable.TreeIterator {
/*
 * path[pathIdx] points to the next available TestResult
 * finished indicates that we're done
 */
    /**
     * Initialize the outQueue and result stats.  This is here only to share
     * construction code and does not create a usable object.
     */
    protected TRT_Iterator() {
        outQueue = new LinkedList();
        resultStats = new int[Status.NUM_STATES];
        nodeIndex = -1;
        currFrame = null;
    }

    TRT_Iterator(TestResultTable.TreeNode node) {
        this();
        nodes = new TestResultTable.TreeNode[1];
        nodes[0] = node;
        init(nodes);

        if (debug)
            Debug.println("Created TreeIterator without filters, one initial node.");
    }

    /**
     * Enumerate elements below and including the specified node.
     *
     * @param filters Which filters to apply to the tests found.  May be null.
     */
    TRT_Iterator(TestResultTable.TreeNode node, TestFilter[] filters) {
        this();
        this.filters = filters;
        nodes = new TestResultTable.TreeNode[1];
        nodes[0] = node;
        init(nodes);

        if (debug)
            Debug.println("Created TreeIterator with filters and one initial node.");
    }

    /**
     * Enumerate elements below and including the specified nodes.
     * This is mainly here to support initial URLs.
     *
     * @param nodes Which nodes to enumerate.  May be null or zero length.  This will be
     *        shallow copied.
     * @param filters Which filters to apply to the tests found.  May be null.
     */
    TRT_Iterator(TestResultTable.TreeNode[] nodes, TestFilter[] filters) {
        this();
        this.filters = filters;

        if (nodes != null) {
            this.nodes = new TestResultTable.TreeNode[nodes.length];
            System.arraycopy(nodes, 0, this.nodes, 0, nodes.length);
        }

        init(this.nodes);

        if (debug)
            Debug.println("Created TreeIterator with filters and initial nodes.");
    }

    /**
     * Enumerate elements below and including the specified nodes.
     * This is mainly here to support initial URLs.
     *
     * @param nodes Which nodes to enumerate.  May be null or zero length.  This will be
     *        shallow copied.
     * @param trs   A given set of TestResults which should be enumerated.  These
     *              are sent through the filters.  This set of tests is shallow copied.
     * @param filters Which filters to apply to the tests found.  May be null.
     */
    TRT_Iterator(TestResultTable.TreeNode[] nodes, TestResult[] trs, TestFilter[] filters) {
        this();
        this.filters = filters;

        if (trs != null && trs.length != 0) {
            ArrayList names = new ArrayList();

            // prime the outqueue with the given tests
            for (int i = 0; i < trs.length; i++) {
                try {
                    if (wouldAccept(trs[i]) == -1) {
                        outQueue.addLast(trs[i]);
                        names.add(trs[i].getDescription().getRootRelativeURL());
                    }
                    else { }
                }   // try
                catch (TestResult.Fault f) {
                }
            }   // for

            initialTests = new String[names.size()];
            names.toArray(initialTests);
        }

        if (nodes != null) {
            this.nodes = new TestResultTable.TreeNode[nodes.length];
            System.arraycopy(nodes, 0, this.nodes, 0, nodes.length);
        }

        init(this.nodes);

        if (debug)
            Debug.println("Created TreeIterator with filters, nodes and initial TR set.");
    }

    // --- Enumerator interface  ---
    @Override
    public boolean hasMoreElements() {
        synchronized(outQueueLock) {
            if (!finished) {
                return true;
            }
            else
                return false;
        }   // sync
    }

    @Override
    public Object nextElement() {
        TestResult val = null;
        synchronized (outQueueLock) {
            if (hasMoreElements()) {
                val = outQueue.removeFirst();

                findNext();

                // XXX refresh val from TRT?
                // the test in the TRT may have changed since it was selected by
                resultStats[val.getStatus().getType()]++;
                return val;
            }
            else {
                throw new NoSuchElementException(i18n.getString("trt.noElements"));
            }
        }       // sync

    }

    // --- Iterator interface ---
    @Override
    public boolean hasNext() {
        return hasMoreElements();
    }

    @Override
    public Object next() {
        return nextElement();
    }

    /**
     * Do not call this method.
     *
     * @throws UnsupportedOperationException Not available for this iterator.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove from TestResultTable through iterator.  Do not call this method.");
    }

    // --- Statistics info ---
    /**
     * Find out how many tests have been processed.
     * This count includes tests which have been filtered out and is an
     * instantaneous snapshot.
     */
    public int getProcessedCount() {
        return absoluteCount;
    }

    @Override
    public int getRejectCount() {
        return rejectCount;
    }

    /**
     * Determines whether or not filtered out tests are recorded for future use
     * or not.  This does not affect operation of <tt>getRejectCount()</tt>.
     * By default, this setting is false.
     * @param state True if rejected tests should be logged, false otherwise.
     */
    @Override
    public void setRecordRejects(boolean state) {
        recordRejects = state;

        if (recordRejects == true) {
            // only create new objects if they don't exist
            // users can turn on and off this feature if they want
            if (filteredTRs == null)
                filteredTRs = new Hashtable(10);        // not likely to have > 10 filters

            if (fo == null)
                fo = new FilterObserver();
        }
    }

    @Override
    public int[] getResultStats() {
        int[] copy = new int[resultStats.length];
        System.arraycopy(resultStats, 0, copy, 0, resultStats.length);

        return copy;
    }

    /**
     * Find out which filters rejected which tests.
     * The data is valid at any point in time; hasNext() does not have to
     * be false.  Note that filters are evaluated in the order shown in getFilters()
     * and that the statistics only registered the <em>first</em> filter that rejected
     * the test; there may be additional filters which would also reject any given
     * test.
     * <p>
     * The hashtable has keys of TestResults, and values which are TestFilters.
     * Because of CompositeFilters, the set of filters found in the ``values''
     * is not necessarily equivalent to those given by getFilters().
     *
     * @return Array as described or null if no tests have been rejected yet.
     */
    @Override
    public Hashtable getFilterStats() {
        if (filteredTRs == null)
            return null;
        else {
            // create shallow, reformatted version of stored data
            // keys are TRs, values are TestFilters.
            Hashtable out = new Hashtable();

            synchronized (rejLock) {
                Enumeration keys = filteredTRs.keys();
                // each key is a TestFilter
                while (keys.hasMoreElements()) {
                    // could cast this to TestFilter, but why?
                    Object thisKey = keys.nextElement();

                    // the HT value is a Vector or TRs
                    Iterator it = ((Vector)(filteredTRs.get(thisKey))).iterator();

                    while (it.hasNext()) {
                        out.put(it.next(), thisKey);
                    }   // inner while
                }   // outer while
            }   // sync

            return out;
        }
    }

    /**
     * Find out what the effective filters are.
     *
     * @return Null if there are no active filters.
     */
    @Override
    public TestFilter[] getFilters() {
        if (filters == null || filters.length == 0)
            return null;
        else {
            // shallow copy it
            TestFilter[] copy = new TestFilter[filters.length];
            System.arraycopy(filters, 0, copy, 0, filters.length);
            return copy;
        }

    }

    /**
     * Find out what the effective initial URLs for this iterator are.
     * The returned array can be any combination of URLs to individual tests
     * or URLs to directories.  Remember these are URLs, so the paths are not
     * platform specific.
     * <p>
     * Note: this method isn't free, cache the data if you need to call this
     * method repeatedly.  The initial URLs will not change over time for any
     * particular instance of this class.
     *
     * @return Null if no specific nodes or tests were requested.  Any array of the initial
     *         URLs otherwise.
     */
    @Override
    public String[] getInitialURLs() {
        String[] list = null;
        ArrayList urls = new ArrayList();

        // can we correctly show the effective urls from here?  what about TCs?
        if (nodes != null) {
            for (int i = 0; i < nodes.length; i++)
                urls.add(TestResultTable.getRootRelativePath(nodes[i]));
        }

        if (initialTests != null) {
            for (int i = 0; i < initialTests.length; i++)
                urls.add(initialTests[i]);
        }

        if (urls.size() > 0) {
            list = new String[urls.size()];
            urls.toArray(list);
        }

        return list;
    }

    @Override
    public Object peek() {
        if (hasNext())
            synchronized (outQueueLock) {
                return outQueue.getFirst();
            }   // sync
        else
            return null;
    }

    @Override
    public boolean isPending(TestResult node) {
        if (!hasMoreElements())
            return false;

        String testName = node.getTestName();
        String partial = testName;

        synchronized (outQueueLock) {
            // compare to outgoing test result objects
            if (outQueue.size() > 0)
                for (int i = 0; i < outQueue.size(); i++) {
                    TestResult tr = outQueue.get(i);
                    if (tr.getTestName().equals(testName)) {
                        return true;
                    }
                }   // for
        }       // sync

        boolean result = false;
        boolean done = false;
        if (nodes != null) {
            for (int i = nodeIndex; i < nodes.length; i++) {
                String nodePath = TestResultTable.getRootRelativePath(nodes[i]);

                if (testName.startsWith(nodePath)) {
                    if (i > nodeIndex) {    // haven't processed that init. node yet
                        result = true;
                        done = true;
                        break;
                    }
                    else if (i < nodeIndex) { // passed this location already
                        result = false;
                        done = true;
                        break;
                    }
                    else {                  // working at this location
                        // if this is a root iterator, we always end up in this
                        // case

                        // remove the "known" part of the location
                        // if we are at node lang/foo, the test name
                        // lang/foo/bar/baz.html becomes bar/baz.html and
                        // we continue...
                        if (nodePath.length() != 0)     // root nodePath is ""
                            partial = testName.substring(nodePath.length()+1);

                        // traverse the stack
                        // XXX this could be made more efficient
                        for (int j = 0; j < stack.size(); j++) {
                            String dir = TestResultTable.getDirName(partial);
                            partial = TestResultTable.behead(partial);

                            // XXX special cases here...

                            // it would be nicer if we could just cast to TRT.TreeNode
                            PseudoFrame frame = (PseudoFrame)(stack.elementAt(j));
                            TRT_TreeNode tn = (TRT_TreeNode)(frame.getNode());
                            int pos = tn.getNodeIndex(dir, false);
                            int currIndex = frame.getCurrentIndex();

                            // special case where test is at top of testsuite
                            if (testName.indexOf("/") == -1)
                                pos = currIndex;

                            // possible outcomes:
                            // 1 test in question can not be found
                            // 2 test path indicates that we've passed it's position
                            // 3 we are working along the test's path
                            // 4 the test's path is ahead of our position

                            // not present or already past
                            if (pos == -1 || (pos < currIndex)) {   // case 1-2
                                result = false;
                                done = true;
                                break;
                            }
                            else if (pos == frame.getCurrentIndex()) { // case 3
                                if (partial.indexOf('/') != -1) {
                                    // more depth needed, go around again
                                    continue;
                                }
                                else if (j+1 == stack.size()) {     // test should be in currFrame
                                    done = false;   // jump out to special case
                                    break;
                                }
                                else {                              // test should be in this frame
                                    switch (checkTestPosition(frame, testName)) {
                                    case -1:
                                    case 0 :
                                        result = false;
                                        done = true;
                                        break;
                                    case 2 :
                                        result = true;
                                        done = true;
                                        break;
                                    case 1 :
                                    default:
                                        throw new IllegalStateException();
                                    }   // switch

                                    break;
                                }
                            }
                            else {                                  // case 4
                                result = true;
                                done = true;
                                break;
                            }
                        }   // inner for

                        // assertions:
                        // the only ways out of the loop above
                        // - determined answer that this method is answering
                        //   in which case done == true
                        // - loop exhausted the stack and done == false

                        // need to check exact iterator location
                            // chop at depth of incoming node
                            // chop stack size
                            // search for first path comp. in currFrame
                            // match node name, compare to location
                    }
                }
            }   // outer for

            // special case to check currFrame which is never
            // represented on the stack
            if (!done) {
                TRT_TreeNode tn = (TRT_TreeNode)(currFrame.getNode());
                String dir = TestResultTable.getDirName(partial);
                partial = TestResultTable.behead(partial);

                if (partial.indexOf("/") == -1) {
                    switch (checkTestPosition(currFrame, testName)) {
                    case -1:
                    case 0 :
                        result = false;
                        break;
                    case 2 :
                        result = true;
                        break;
                    case 1 :
                    default:
                        throw new IllegalStateException();
                    }   // switch
                }
                else if (!dir.equals(currFrame.getNode().getName())) {
                    result = false;         // can't find location
                    // test is *below* this node
                    dir = TestResultTable.getDirName(partial);
                    tn = (TRT_TreeNode)(currFrame.getNode());
                    int pos = tn.getNodeIndex(dir, false);
                    int currIndex = currFrame.getCurrentIndex();

                    // possible outcomes:
                    // 1 test in question can not be found
                    // 2 test path indicates that we've passed it's position
                    // 3 we are working along the test's path
                    // 4 the test's path is ahead of our position

                    if (pos == -1) {                        // case 1
                        result = false;
                    }
                    else if (pos < currIndex) {             // case 2
                        result = false;
                    }
                    else if (pos == currIndex) {            // case 3
                        // not possible?
                        throw new IllegalStateException("");
                    }
                    else {                                          // case 4
                        result = true;
                    }
                }
                else {
                    int pos = tn.getTestIndex(testName);
                    if (pos == -1) {        // not found
                        result = false;
                    }

                    if (pos > currFrame.getCurrentIndex()) {
                        result = true;
                    }
                    else {
                        result = false;
                    }
                }
            }   // if !done

        }   // outer if

        return result;
    }

    // ------------------ TreeIterator Private --------------------
    /**
     * Initialize state.  Should be called during construction, but after filters are set.
     * outQueue should be populated before you call this method.  This method should only be
     * called at construction time since it is not synchronized.
     *
     * @param nodes May be null or zero length.
     */
    private void init(TestResultTable.TreeNode[] nodes) {
        if (nodes != null && nodes.length == 0)
            this.nodes = null;
        else
            this.nodes = nodes;

        boolean hasNodes = nextNode();

        if (hasNodes)
            findNext();
        else if (outQueue.size() == 0)
            finished = true;
        // else outQueue.length != 0, so we continue
    }

    /**
     * Move the object state to provide the next available element.
     */
    private synchronized void findNext() {
        boolean done = false;

        if (finished == true)
            return;

        while (!done) {
            // init(),nextFrame() will set currFrame if there are any nodes to
            // process.  If this is null, our work is done.
            if (currFrame == null) {
                synchronized (outQueueLock) {
                    if (outQueue.size() == 0)
                        finished = true;
                }   // sync
                return;
            }

            // get data from current node
            int nextIndex = currFrame.nextIndex();
            if (nextIndex == -1) {          // done with this node
                nextFrame();
                continue;
            }
            else {                          // keep going
                Object anonNode = currFrame.getNode().getChild(nextIndex);
                if (anonNode == null) {     // error condition really
                    nextFrame();
                    continue;               // attempt to recover by skipping
                }

                if (anonNode instanceof TestResultTable.TreeNode) {
                    // need to recurse into this node
                    // setup stack frames and recurse
                    TestResultTable.TreeNode node = (TestResultTable.TreeNode)anonNode;
                    push(currFrame);
                    currFrame = new PseudoFrame(node);
                    continue;               // recurse
                }
                else {
                    // we can assume that the node is a test since it is not
                    // null and not a node
                    TestResult test = (TestResult)anonNode;

                    // check the filters
                    try {
                        int would = wouldAccept(test);

                        if (would >= 0) {               // rejected
                            continue;           // recurse
                        }
                    }
                    catch (TestResult.Fault f) {

                        TestResultTable trt = null;
                        // need handle onto TRT to reset
                        // could use TR.getParent(), but it's
                        // probably better not to trust it
                        if (nodes != null && nodes[0] != null)
                            trt = nodes[0].getEnclosingTable();
                        else if (test.getParent() != null) {
                            trt = test.getParent().getEnclosingTable();
                        }
                        else
                            continue;

                        if (trt == null)
                            continue;
                        else {
                            test = trt.resetTest(test);
                            // nowr retry once
                            try {
                                if (wouldAccept(test) >= 0)
                                    continue;   // rejected
                            }
                            catch (TestResult.Fault f2) {
                                if (debug)
                                    f2.printStackTrace(Debug.getWriter());
                                // give up
                                continue;
                            }
                        }
                    }

                    // not filtered out
                    // put this test into the output queue
                    synchronized (outQueueLock) {
                        outQueue.addLast(test);
                    }   // sync

                    done = true;
                }
            }
        }   // while
    }

    /**
     * Is this location the top of an initial node.
     * In the simplist case where we are iterating from the root, is this the
     * root node?
     */
    private boolean isTop(TestResultTable.TreeNode where) {
        if (where.isRoot() || nodes[nodeIndex] == where)
            return true;
        else
            return false;
    }

    /**
     * Move to the next initial node by changing the value of currFrame.
     * This method selects the next node in the queue to process.  currFrame will
     * be null if this method returns false;
     *
     * @return True if at least one node to enum. has been found.  False
     *         otherwise.
     */
    private boolean nextNode() {
        // stacks only valid in the context of a single node
        // we create a new one when we move to a new initial node
        stack = new Stack();

        // invalidate the current frame
        // NOTE: a null currFrame will terminate the iteration in findNext()
        currFrame = null;

        if (nodes != null && ++nodeIndex < nodes.length) {
            // more on to the next node
            TestResultTable.TreeNode next = nodes[nodeIndex];
            currFrame = new PseudoFrame(next);

            return true;
        }
        else {
            // no more nodes
            return false;
        }
    }

    /**
     * Move down one level in the tree.
     * The path basically operates like a program stack and remembers what we
     * were doing at each level so we can continue when we get back there.
    private void push(TestResultTable.TreeNode newLocation) {
        // increase available recording depth
        if (pathIdx + 1 == path.length) {
            // need more array space
            int[] temp = new int[path.length+1];
            System.arraycopy(path, 0, temp, 0, path.length);
            path = temp;
        }

        pathIdx++;
        path[pathIdx] = -1;
        location = (TestResultTable.TreeNode)newLocation;
    }
     */

    private void push(PseudoFrame frame) {
        stack.push(frame);
    }

    /**
     * @return The next frame on the stack.  Null if stack is empty.
     */
    private PseudoFrame pop() {
        if (!stack.empty())
            return (PseudoFrame)(stack.pop());
        else
            return null;
    }

    /**
     * Get the next frame off the stack or goto the next node in the input queue.
     */
    private void nextFrame() {
        currFrame = pop();

        // if we reach the bottom of the stack, we need a new node
        // it will set currFrame for us
        if (currFrame == null)
            nextNode();
    }

    /**
     * This method used to be boolean, but was changed to int to collect stats.
     *
     * @param tr The test to run through the filter, must not be null
     * @return The index into filters[] which rejected the test, -1 if it would be accepted.
     * @throws TestResult.Fault May happen when requesting info from the TestResult,
     *         probably a reload fault.
     */
    private int wouldAccept(TestResult tr) throws TestResult.Fault {
        if (filters == null || filters.length == 0)
            return -1;

        if (debug)
            Debug.println("Iterator checking filter for: " + tr.getWorkRelativePath());

        absoluteCount++;
        currentResult = tr;

        for (int i = 0; i < filters.length; i++) {
            boolean accepted = true;
            try {
                if (fo == null)
                    accepted = filters[i].accepts(tr.getDescription());
                else
                    accepted = filters[i].accepts(tr.getDescription(), fo);
            }
            catch (TestFilter.Fault f) {
                accepted = true;
                if (debug)
                    Debug.println("   -> exception while checking filter: " + f.getMessage());
            }   // catch

            if (!accepted) {
                if (debug) {
                    Debug.println("   -> Rejected by: " + filters[i]);
                    Debug.println("   -> Test Status: " + tr.getStatus().getType());
                }

                rejectCount++;
                // rejected
                return i;
            }
        }   // for

        // accepted
        return -1;
    }

    /**
     * @return -1==not found, 0==before, 1==current pos., 2==after, 3==error
     */
    private static int checkTestPosition(PseudoFrame frame, String testName) {
        // need to determine if it is before or after the
        // current iterator position
        TRT_TreeNode tn = (TRT_TreeNode)(frame.getNode());
        int targetIndex = tn.getTestIndex(testName);
        int currIndex = frame.getCurrentIndex();
        int result = 3;

        if (targetIndex == -1)
            result = -1;
        else if (targetIndex < currIndex)
            result = 0;
        else if (targetIndex == currIndex)
            result = 1;
        else if (targetIndex > currIndex)
            result = 2;
        else
            result = 3;

        return result;
    }

    /**
    private TestResultTable.TreeNode traverseToNode(TestResultTable.TreeNode start, String path) {
        String curr;
        TestResultTable.TreeNode pos;

        while (path != null) {
        }   // while
    }
    */

    /**
     * Tests (literally) that were provided by the client through one of the
     * constructors.  This is mainly here to facilitate recall of the initial URL
     * list if requested.
     */
    private String[] initialTests;

    /**
     * Queue which holds all outgoing tests.  This is initially contains the tests
     * provided by the client.
     */
    private final Object outQueueLock = new Object();
    private LinkedList<TestResult> outQueue;  // queue of tests to be given out

    private TestResultTable.TreeNode[] nodes;       // which nodes we are enumerating
    private int nodeIndex;                          // which node are we working on

    private TestFilter[] filters;
    private int[] resultStats;          // pass/fail/error statistics
    private int absoluteCount;          // how many tests were processed


    // filter rejection info
    private int rejectCount;
    private boolean recordRejects;      // true when we are collecting reject stats
    private Hashtable filteredTRs;      // key==TestFilter  value=Vector of TestResults
    private TestResult currentResult;   // necessary communicate with filter observer, yuck
    private Object rejLock;
    private FilterObserver fo;          // null if feature is disabled

    // ------ state information ------
    private Stack stack;
    private PseudoFrame currFrame;

    private boolean finished = false;
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(TRT_Iterator.class);
    protected boolean debug = Debug.getBoolean(TRT_Iterator.class);

    // INNER CLASS
    /**
     * We can't use real recursion to implement an iterator, so this is a simulated,
     * persistent stack frame which will store state as we traverse the tree.
     */
    private static class PseudoFrame {
        /**
         * @param node The tree node which this frame is responsible for.
         */
        PseudoFrame(TestResultTable.TreeNode node) {
            this.node = node;
            this.currIndex = -1;
        }

        /**
         * Next index in this node to process.
         *
         * @return -1 if work in this node is complete
         */
        int nextIndex() {
            if (++currIndex < node.getChildCount())
                return currIndex;
            else
                return -1;
        }

        /**
         * What node does this frame track?
         */
        TestResultTable.TreeNode getNode() {
            return node;
        }

        /**
         * What was the last index returned.
         * @return A value greater than zero, unless the node has been
         *         exhausted, in which case -1.
         */
        int getCurrentIndex() {
            return currIndex;
        }

        private int currIndex;
        private TestResultTable.TreeNode node;
    }

    private class FilterObserver implements TestFilter.Observer {
        @Override
        public void rejected(TestDescription d, TestFilter rejector) {
            synchronized (rejLock) {
                Vector vec = (Vector)(filteredTRs.get(rejector));
                if (vec == null) {
                    vec = new Vector();
                    filteredTRs.put(rejector, vec);
                }

                // remove later...
                try {
                    if (currentResult.getDescription() != d)
                        throw new JavaTestError("TRT_Iterator observered TR.TD does not match filtered one.");
                }
                catch (TestResult.Fault f) {
                    throw new JavaTestError("TRT_Iterator cannot determine TR source info.", f);
                }   // sanity check

                vec.add(currentResult);

            }   // sync
        }
    }
}       // TRT_Iterator

