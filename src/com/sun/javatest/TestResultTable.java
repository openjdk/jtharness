/*
 * $Id$
 *
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
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
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

import com.sun.javatest.tool.Preferences;
import com.sun.javatest.util.Debug;
import com.sun.javatest.util.DynamicArray;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.StringArray;
import com.sun.javatest.httpd.RootRegistry;

/**
 * Collected results from a test suite.
 * The data is represented as TestResult objects, although the test itself
 * may not have been run, but just "found" so far.  TestResultTable is
 * populated by using a TestFinder, and is subsequently updated as tests are
 * run by the harness.
 */

// debug values:
// 1 - info messages, stack traces on
// 2 - search details
// 10 - more scan/insert/remove detail
// 11 - heavy scan/insert/remove detail
// 99 - everything
// NOTE: stack traces are on for all levels above 0

public class TestResultTable {
    public void dispose() {
        if (trCache != null) {
            trCache.shutdown();
        }
    }

    /**
     * Observer to monitor changes to a TestResultTable.
     */
    public interface Observer {
        /**
         * The oldValue has been replaced by the newValue.
         *
         * @param oldValue Previous value being overwritten.
         * @param newValue The new value stored in the TRT.
         */
        void update(TestResult oldValue, TestResult newValue);

        /**
         * The given test was changed, but previously existed in this TRT.
         * This is not a guarantee of change, but is the best possible hint.
         *
         * @param whichTR The test which was altered.
         */
        void updated(TestResult whichTR);

        /*
         void stalled(String reason);
         void ready();
         */
    }

    /**
     * Tree-aware observer of the TRT.
     *
     * @since 3.0
     */
    public interface TreeObserver {
       /**
        * A node has been inserted into the tree.
        * @param path The path to the node that was inserted.  Does not include
        *        the actual object which is new.
        * @param what The actual object that changed.  So <code>path</code> plus
        *        this is the entire path.  The type will either be TestResult or
        *        TreeNode.
        * @param index The index in <code>path[path.length-1]</code> where the
        *        new node was placed.
        */
       void nodeInserted(TreeNode[] path, Object what, int index);

       /**
        * A node has changed.
        * In the case of a test changing, the old object is the test result
        * being replaced.  In the case of a branch changing, the old object is
        * the same as the what object.
        *
        * @param path The path to the node that changed.  Does not include
        *        the actual object which changed.
        * @param what The actual object that changed.  So <code>path</code> plus
        *        this is the entire path.  The type will either be TestResult or
        *        TreeNode.
        * @param index The index in <code>path[path.length-1]</code> that changed.
        * @param old The old value at the changed location.
        */
       void nodeChanged(TreeNode[] path, Object what, int index, Object old);

       /**
        * An item has been removed from the tree.
        *
        * @param path The path to the node that changed.  Does not include
        *        the actual object which changed.
        * @param what The actual object that was removed.  So <code>path</code> plus
        *        this is the entire path.  The type will either be TestResult or
        *        TreeNode.
        * @param index The index in <code>path[path.length-1]</code> that was
        *        removed.
        */
       void nodeRemoved(TreeNode[] path, Object what, int index);
    }

    /**
     * Extension to TreeObserver to receive notifications related
     * to events happened on tree nodes.
     */
    public interface TreeEventObserver extends TreeObserver {

        /**
         * A refresh has been stared on the node. All children
         * will be recursively refreshed but only one notification about the
         * original node will be delivered.
         *
         * @param origin Node the refresh has been originated
         *
         */
        void startRefresh(TreeNode origin);

        /**
         * A refresh has been finished on the node. In spite of the all children
         * will be recursively refreshed the only one notification about the
         * original node will be delivered.
         *
         * @param origin Node the refresh has been originated
         *
         */
        void finishRefresh(TreeNode origin);
    }

    /**
     * Observer interface to watch a single tree node.
     *
     * @since 3.0
     */
    public interface TreeNodeObserver {
        /**
         * A TreeNode has been inserted into the given parent node.
         *
         * @param parent The node which acquired the new node.  This is the same as
         *        the object that the observer attached to.
         * @param newNode The node which was added.
         * @param index The index at which the node was added.
         */
        public void insertedBranch(TreeNode parent, TreeNode newNode, int index);

        /**
         * A TestResult has been inserted into the given parent node.
         *
         * @param parent The node which acquired the new test.  This is the same as
         *        the object that the observer attached to.
         * @param test The test which was added.
         * @param index The index at which the test was added.
         */
        public void insertedResult(TreeNode parent, TestResult test, int index);

        /**
         * A TestResult has been replaced in the given parent node.
         *
         * @param parent The node which acquired the new test.  This is the same as
         *        the object that the observer attached to.
         * @param oldTest The test which was replaced.
         * @param newTest The test which took the old test's place.
         * @param index The index at which activity occurred.
         */
        public void replacedResult(TreeNode parent, TestResult oldTest,
                                   TestResult newTest, int index);

        /**
         * A TreeNode has been removed from the given parent node.
         *
         * @param parent The node which acquired the new test.  This is the same as
         *        the object that the observer attached to.
         * @param index The index at which the removed node resided in the parent.
         */
        public void removedBranch(TreeNode parent, int index);

        /**
         * A TestResult has been removed from the given parent node.
         *
         * @param parent The node which acquired the new test.  This is the same as
         *        the object that the observer attached to.
         * @param test The test which was removed.
         * @param index The index at which the removed test resided in the parent.
         */
        public void removedResult(TreeNode parent, TestResult test, int index);

        /**
         * The statistics counters of the node have changed.
         *
         * @param node The node whose counters were invalidated.
         *        This is the same as the node which this observer attached to.
         */
        public void countersInvalidated(TreeNode node);
    }


    /**
     * Exception class to communicate any special conditions which may occur
     * while using this class.
     */
    public static class Fault extends Exception
    {
        Fault(I18NResourceBundle i18n, String s) {
            super(i18n.getString(s));
        }

        Fault(I18NResourceBundle i18n, String s, Object o) {
            super(i18n.getString(s, o));
        }

        Fault(I18NResourceBundle i18n, String s, Object[] o) {
            super(i18n.getString(s, o));
        }
    }

    /**
     * Create a table ready to be occupied.
     *
     * @deprecated This method will become private.
     * XXX make this method private
     */
    public TestResultTable() {
        //table = new Hashtable();
        statusTables = new Hashtable[Status.NUM_STATES];
        for (int i = 0; i < statusTables.length; i++)
            statusTables[i] = new Hashtable();

        root = new TRT_TreeNode(this, null);

        /* OLD
        // cache compression policy
        int prop = Integer.getInteger("jt.cacheThreshold", 40).intValue();
        if (prop < 0 || prop > 100)
            prop = 40;

        COMPRESSION_THRESHOLD = prop/100.0f;
        */

        instanceId++;
        if (com.sun.javatest.httpd.HttpdServer.isActive()) {
            String url = "/trt/" + instanceId;
            httpHandle = new TRT_HttpHandler(this, url, instanceId);
            RootRegistry.getInstance().addHandler(url, "Test Result Table",
                                                  httpHandle);
            RootRegistry.associateObject(this, httpHandle);
        }

        rtc = new RequestsToCache();
            }


    /**
     * Create a table for the tests in a work directory and its
     * associated test suite and test finder.
     *
     * @param wd The work directory to associate with this table.
     * @since 3.0
     */
    public TestResultTable(WorkDirectory wd) {
        this();
        setWorkDirectory(wd);
    }

    /**
     * Create a table for the tests in a work directory and its
     * associated test suite, overriding the test suite's default test finder.
     *
     * @param wd The work directory to associate with this table.
     * @param tf The finder to use.  Do not use this constructor unless
     *        necessary.
     * @see #TestResultTable(WorkDirectory)
     * @since 3.0
     */
    public TestResultTable(WorkDirectory wd, TestFinder tf) {
        this();
        setWorkDirectory(wd, tf);
    }

    /**
     * Set the test finder for this object.
     * It is illegal to call this method once the test finder for a instance
     * has been set.  Rather than use this method, it is probably better to
     * supply the finder at construction time.
     *
     * @param tf The finder to use.
     * @throws IllegalStateException Thrown if the finder for this object is already set.
     * @see #getTestFinder
     * @since 3.0
     */
    public void setTestFinder(TestFinder tf) {
        if (finder != null) {
            throw new IllegalStateException(i18n.getString("trt.alreadyFinder"));
        }

        finder = tf;

        if (trCache == null)
            initFinder();
    }

    /**
     * Get the workdir associated with this object.
     *
     * @return The workdir.  Null if not available.
     * @deprecated Use getWorkDirectory
     */
    public WorkDirectory getWorkDir() {
        return getWorkDirectory();
    }

    /**
     * Get the work directory associated with this object.
     *
     * @return The work directory, or null if none set.
     * @see #setWorkDirectory
     */
    public WorkDirectory getWorkDirectory() {
        return workDir;
    }

    /**
     * Set the work directory associated with this object.
     * Once set, it cannot be changed.
     *
     * @param wd The work directory, or null if none set.
     * @see #getWorkDirectory
     */
    public void setWorkDirectory(WorkDirectory wd) {
        setWorkDirectory(wd, wd.getTestSuite().getTestFinder());
    }

    // this method exists to support the obsolete constructor
    // TestResultTable(WorkDirectory wd, TestFinder tf).
    // When that constructor can be deleted, the main body of this
    // method should be merged with setWorkDirectory(WorkDirectory wd)
    private void setWorkDirectory(WorkDirectory wd, TestFinder tf) {
        if (wd == null)
            throw new NullPointerException();

        if (workDir == wd) {
            // already set
            return;
        }

        if (workDir != null && workDir != wd) {
            // already set to something else
            throw new IllegalStateException();
        }

        if (finder != null && finder != tf)
            throw new IllegalStateException();

        workDir = wd;
        finder = tf;

        //root = new TRT_TreeNode(this, null);
        initFinder();

        /*OLD
        // do this in the background because of possible high cost
        Thread thr = new Thread("TRT background cache init.") {
            public void run() {
                try {
                    trCache = new TestResultCache(workDir, true);
                    trCache.setObserver(updater);
                    setCacheInitialized(true);
                }
                catch (TestResultCache.Fault f) {
                    if (debug > 0)
                        f.printStackTrace(Debug.getWriter());

                    // XXX ack, what can we do?!
                    //throw new Fault(i18n, "trt.trcCreate", f.getMessage());
                }   // try
            }   // run()
        };
        thr.setPriority(Thread.MIN_PRIORITY + 2);
        thr.start();
        */
        try {
            trCache = new TestResultCache(workDir, updater);
        }
        catch (IOException e) {
            // should consider throwing Fault, but that will destabilize too many APIs for now
            updater.error(e);
        }
    }

    /**
     * How many tests have been found so far.
     *
     * @return A number greater than or equal to zero.
     * @since 3.0
     */
    public int getCurrentTestCount() {
        return root.getCurrentSize();
    }

    void starting() {
        /*OLD
        isRunning++;
        */
    }

    void finished() {
        // do on background thread? (OLD suggestion -- now asynchronous)
        if (trCache != null) {
            if (needsCacheCompress()) {
                if (debug > 0) {
                    Debug.print("TRT.finished(), attempting cache compress...");
                }
                trCache.compress();
            }
            if (debug > 0) {
                Debug.print("TRT.finished(), requesting cache flush...");
            }
        }
    }

    /**
     * Update the information in the table with a new test result.
     * The supplied TestResult may exist in the table already, it can replace
     * an existing test or it can be completely new.  Doing this operation will
     * trigger appropriate observer messages.
     *
     * @param tr The test to insert or update.
     * @throws JavaTestError Throws if the result cache throws an error.
     */
    public void update(TestResult tr) {
        update(tr, false);
    }

    /**
     * Update the information in the table with a new test result.
     * The supplied TestResult may exist in the table already, it can replace
     * an existing test or it can be completely new.  Doing this operation will
     * trigger appropriate observer messages.
     *
     * @param tr The test to insert or update.
     * @param suppressScan Request that test finder activity be suppressed if possible
     * @throws JavaTestError Throws if the result cache throws an error.
     */
    public void update(TestResult tr, boolean suppressScan) {
        TestResult prev = insert(tr, suppressScan);
        updateNotify(tr, prev);
    }

    /**
     * Complete the notification process for insertion of a new result.
     * This method adds it to the cache, sends notifications and does other
     * bookkeeping only.
     *
     * This method was introduced to allow updates to occur from non-root
     * branches (bottom up updates) or updates from insertions into the table
     * which are usual during test execution (top down).
     * @param tr Result inserted.
     * @param prev Result replaced (if any).
     */
    void updateNotify(TestResult tr, TestResult prev) {

        if (tr != prev) {
            tr.shareStatus(statusTables);

            for (int i = 0; i < observers.length; i++)
                observers[i].update(prev, tr);
        }
        else {
            // tests are the same, we are probably changing the status
            for (int i = 0; i < observers.length; i++)
                observers[i].updated(tr);
        }

        testsInUpdate.add(tr);

        // things which haven't been run are not put in cache
        // doing so will cause problems in the cache and possibly other
        // places because there is never a JTR file to reload the result from
        if (trCache!= null && !updateInProgress && tr.getStatus().getType() != Status.NOT_RUN) {
            trCache.insert(tr);
        }

        testsInUpdate.remove(tr);
    }

    /**
     * This method blocks until the work directory data has been completely
     * synchronized.  It is recommended that you use this before creating and
     * using an iterator to ensure that you get consistent and up to date
     * data.  It would also be advisable to do this before forcing the VM to
     * exit.
     * @return Always true.  Reserved for later use.
     *
     * @since 3.0.1
     */
    public synchronized boolean waitUntilReady() {
        while ( ((workDir != null) && !cacheInitialized) ||
                  updateInProgress) {
            try {
                wait();
            }
            catch (InterruptedException e) {
                if (debug > 0)
                    e.printStackTrace(Debug.getWriter());
            }
        }

        return true;
    }

    /**
     * Determine the update status of the table.
     *
     * @return True if the table is in a consistent state.
     * @see #waitUntilReady
     */
    public synchronized boolean isReady() {
        return (cacheInitialized && !updateInProgress);
    }

    /**
     * Find a specific instance of a test result.
     *
     * @param td The test description which corresponds to the target result.
     * @return The requested test result object, null if not found.
     */
    public TestResult lookup(TestDescription td) {
        // transforms to JTR path style to do lookup
        return lookup(TestResult.getWorkRelativePath(td.getRootRelativeURL()));
    }

    /**
     * Find a specific instance of a test result.
     * If you only have the test URL, use TestResult.getWorkRelativePath() to get
     * the jtrPath parameter for this method.
     *
     * @param jtrPath The work relative path of the test result file.
     *                Output from TestResult.getWorkRelativePath() is
     *                the best source of this info.
     * @return The requested test result object, null if not found.
     */
    public TestResult lookup(String jtrPath) {
        // no tree yet
        if (root == null) return null;

        return findTest((TRT_TreeNode)root, jtrPath, jtrPath);
    }

    /**
     * Take a URL and find the node in the current test tree to which it refers
     * to.  The resulting node may be:
     * <ul>
     * <li>A folder (non-leaf) node.  Type <code>TreeNode</code>
     * <li>A test (leaf) node.  Type <code>TestResult</code>
     * </ul>
     * @param url A forward-slash separated path referring to node names along a
     *        path originating at the root node.
     * @return The nodes that the given URL refers to.  Null if no match.
     * @since 3.2
     */
    public Object resolveUrl(String url) {
        return lookupNode(root, url);
    }

    /**
     * Validate that a path is valid in this TestResultTable according to the
     * rules for initial URLs.
     *
     * @param path The path to validate.  Should be internal URL format (forward
     *        slashes, relative to test root, ...)  Null is acceptable but will
     *        immediately result in null being returned.
     * @return True if the given path is valid in this instance, false otherwise.
     * @since 3.0.2
     */
    public boolean validatePath(String path) {
        if (path == null)
            return false;

        Object[] result = lookupInitURL(root, path);

        if (result != null && result.length > 0)
            return true;        // ok
        else
            return false;       // no matches for path
    }

    /**
     * Find the specified test, recording the path from the root.
     *
     * @since 3.0
     * @param target The test to generate the path for.
     * @return The path to the root of the enclosing TRT.  Null if the operation
     *      could not be completed.  The target node is not included in the
     *      returned array.  Index 0 will be the TRT root.
     */
    public static TreeNode[] getObjectPath(TestResult target) {
        if (target == null)
            return null;

        Vector path = new Vector();
        TreeNode loc = target.getParent();
        while (loc != null) {
            path.insertElementAt(loc, 0);
            loc = loc.getParent();
            // getParent() will be null for the root node
        }   // while

        TreeNode[] result = new TreeNode[path.size()];
        path.copyInto(result);

        if (debug == 2 || debug == 99) {
            Debug.println("TRT - getObjectPath() results:");
            Debug.println("   -> target: " + target.getTestName());
            Debug.println("   -> resulting path length: " + result.length);
        }

        if (result == null || result.length == 0)
            return null;
        else
            return result;
    }

    /**
     * Find the specified test, recording the path from the root.
     *
     * @since 3.0
     * @param target The node to generate the path for.
     * @return The path to the root of the enclosing TRT.  Null if the operation
     *      could not be completed.  The target node is included in the returned array
     *      as the last element, index 0 will be the TRT root.
     */
    public static TreeNode[] getObjectPath(TreeNode target) {
        if (target == null)
            return null;

        Vector path = new Vector();
        TreeNode loc = target;
        while (loc != null) {
            path.insertElementAt(loc, 0);
            loc = loc.getParent();      // getParent() will be null for the root node
        }   // while

        TreeNode[] result = new TreeNode[path.size()];
        path.copyInto(result);

        if (debug == 2 || debug == 99) {
            Debug.println("TRT - getObjectPath() results:");
            Debug.println("   -> target RRP: " + TestResultTable.getRootRelativePath(target));
            Debug.println("   -> resulting path length: " + result.length);
        }

        return result;
    }

    /**
     * List all the tests in the tree.
     *
     * @return An iterator which returns all tests in the tree.
     * @since 3.0
     */
    public TreeIterator getIterator() {
        if (root == null)
            return NullEnum.getInstance();
        else
            return getIterator(root);
    }

    /**
     * List all the tests in the tree.
     *
     * @return An enumerator which returns all tests in the tree.
     * @see #getIterator()
     * @since 3.0
     */
    public Enumeration elements() {
        return getIterator();
    }

    /**
     * List all the tests in the tree subject to the given filters.
     *
     * @param filters The Filters to run tests through before "selecting"
     *        them for iteration.  May be null.
     * @return An iterator which returns all tests in the tree after removing
     *         those filtered out by the filters.
     * @since 3.0
     */
    public TreeIterator getIterator(TestFilter[] filters) {
        if (root == null)
            return NullEnum.getInstance();
        else
            return getIterator(root, filters);
    }

    /**
     * Same description as getIterator() method with same args.
     *
     * @param filters The Filters to run tests through before "selecting"
     *        them for iteration.  May be null.
     * @return An enumerator which returns all tests in the tree after removing
     *         those filtered out by the filters.
     * @since 3.0
     * @see #getIterator()
     */
    public Enumeration elements(TestFilter[] filters) {
        return getIterator(filters);
    }

    /**
     * List all the tests under this node.
     *
     * @param node The tree node to being the iteration at.
     * @return An iterator which return all the tests below the given node.
     * @since 3.0
     */
    public static TreeIterator getIterator(TreeNode node) {
        if (node == null)
            return NullEnum.getInstance();
        else
            return new TRT_Iterator(node);
    }

    /**
     * List all the tests under this node.
     *
     * @param node The tree node to being the iteration at.
     * @return An enumerator which return all the tests below the given node.
     * @see #getIterator()
     * @since 3.0
     */
    public static Enumeration elements(TreeNode node) {
        return getIterator(node);
    }

    /**
     * Get an iterator capable of producing a filtered view of the test suite.
     * If the node parameter is null, an iterator with no items will be returned.
     * An empty or null set of filters is acceptable and will result in unfiltered
     * iteration.
     *
     * @param node The tree node to being the iteration at.  May be null.
     * @param filter The filter to run tests through before "selecting"
     *        them for iteration.
     * @return An iterator which returns test below the given node after
     *         removing any tests which the filter rejects.
     * @since 3.0
     */
    public static TreeIterator getIterator(TreeNode node, TestFilter filter) {
        if (node == null)
            return NullEnum.getInstance();
        else {
            TestFilter[] filters = new TestFilter[] {filter};
            return new TRT_Iterator(node, filters);
        }
    }

    /**
     * Same description as getIterator() method with same args.
     *
     * @param node The tree node to being the enumeration at.  May be null.
     * @param filter The filter to run tests through before "selecting"
     *        them for enumeration.  May be null.
     * @return An enumerator which returns test below the given node after
     *         removing any tests which the filter rejects.
     * @see #getIterator()
     * @since 3.0
     */
    public static Enumeration elements(TreeNode node, TestFilter filter) {
        return getIterator(node, filter);
    }

    /**
     * Get an iterator capable of producing a filtered view of the test suite.
     * If the node parameter is null, an iterator with no items will be returned.
     * An empty or null set of filters is acceptable and will result in unfiltered
     * iteration.
     *
     * @param node The tree node to begin enumerating at.  May be null.
     * @param filters The test filters to apply to any tests found.
     * @return An iterator which returns test below the given node after
     *         removing any tests which the filters reject.
     * @since 3.0
     */
    public static TreeIterator getIterator(TreeNode node, TestFilter[] filters) {
        if (node == null)
            return NullEnum.getInstance();
        else
            return new TRT_Iterator(node, filters);
    }

    /**
     * Same description as getIterator() method with same args.
     *
     * @param node The tree node to begin enumerating at.  May be null.
     * @param filters The test filters to apply to any tests found.
     * @return An enumerator which returns test below the given node after
     *         removing any tests which the filters reject.
     * @see #getIterator()
     * @since 3.0
     */
    public static Enumeration elements(TreeNode node, TestFilter[] filters) {
        return getIterator(node, filters);
    }

    /**
     * Get an enumerator capable of producing a filtered view of the test
     * suite.  This can be used to obtain a view of the test suite based on an
     * initial URL selection.  The URL can specify a folder/directory, a
     * specific test, or a file which contains one or more tests.  If the given
     * URL parameter is null, an iterator with no elements will be returned.
     * An empty or null set of filters is acceptable and will result in
     * unfiltered iteration.
     *
     * @param url The test URL to scan.  This value should have already be
     *            normalized to a '/' file separator.  May be null.
     * @param filters The test filters to apply to any tests found.  May be null.
     * @return An enumerator which returns test below the given location after
     *         removing any tests which the filters reject.
     * @see #getIterator()
     * @since 3.0
     */
    public Enumeration elements(String url, TestFilter[] filters) {
        if (url == null)
            return NullEnum.getInstance();
        else {
            String[] urls = {url};
            return elements(urls, filters);
        }
    }

    /**
     * Get an iterator capable of producing a filtered view of the test suite.
     * This can be used to obtain a view of the test suite based on an initial
     * URL selection.  The URL can specify a folder/directory, a specific test,
     * or a file which contains one or more tests.  If the initial urls are
     * null or zero length, a filtered iterator of the root will be returned.
     * An empty or null set of filters is acceptable and will result in
     * unfiltered iteration.
     *
     * @param tests The set of files base the iterator on.  May be null.
     *        If this set is not empty, the contents should have already been
     *        validated using the validatePath() method.
     * @param filters The test filters to apply to any tests found.  May be null.
     * @return An iterator which return the union of tests specified by the
     *         initial files but not removed by the filters.
     * @throws TestResultTable.Fault Thrown if the given initialUrls are invalid.
     * @since 3.0
     * @see #validatePath
     */
    public TreeIterator getIterator(File[] tests, TestFilter[] filters) throws Fault {
        String[] urls = preProcessInitFiles(tests);

        if (urls != null && urls.length > 0)
            return getIterator(urls, filters);
        else
            return getIterator(filters);
    }

    /**
     * Same as getIterator() with the same args.
     *
     * @param tests The set of files base the enumerator on.  May be null.
     * @param filters The test filters to apply to any tests found.  May be null.
     * @return An enumerator which return the union of tests specified by the
     *         initial files but not removed by the filters.
     * @throws TestResultTable.Fault Thrown if the given initialUrls are invalid.
     * @see #getIterator()
     * @since 3.0
     */
    public Enumeration elements(File[] tests, TestFilter[] filters) throws Fault {
        return getIterator(tests, filters);
    }

    /**
     * Get an iterator capable of producing a filtered view of the test suite.
     * This can be used to obtain a view of the test suite based on an initial
     * URL selection.  An empty or null set of filters is acceptable and will
     * result in unfiltered iteration.
     *
     * @param paths The test URLs to scan.  Values should have already be normalized to a
     *            '/' file separator.  May not be null.
     * @param filters The test filters to apply to any tests found.  May be null.
     * @return An iterator which return the union of tests specified by the
     *         URLs but not removed by the filters.
     * @since 3.0
     */
    public TreeIterator getIterator(String[] paths, TestFilter[] filters) {
        LinkedList<TreeNode> initNodes = new LinkedList<TreeNode>();
        LinkedList<TestResult> initTests = new LinkedList<TestResult>();

        String[] urls = sortByName(paths); // sorting in any case to improve performance of distilling
        urls = distillUrls(urls);

        if (!Boolean.parseBoolean(
            Preferences.access().getPreference("javatest.sortExecution", "true"))) {
            // need to exclude extra elements from initial array
            if (urls.length != paths.length) {
                urls = removeMissing(paths, urls);
            } else {
                // nothing was distilled - just taking paths
                urls = paths;
            }
        }

        for (int i = 0; i < urls.length; i++) {
            Object[] objs = lookupInitURL(root, urls[i]);
            if (debug == 1 || debug == 99)
                Debug.println("TRT.lookupInitURL gave back " + Arrays.toString(objs));

            if (objs == null)   // no match
                continue;
            else if (objs instanceof TreeNode[]) {
                // don't add duplicates
                if (!initNodes.contains((TreeNode) objs[0]))
                    initNodes.add((TreeNode) objs[0]);
            }
            else if (objs instanceof TestResult[]) {
                initTests.addAll(Arrays.asList((TestResult[]) objs));
                // XXX should uniquify
            }
            else {
                // XXX should this be more friendly?
                //     or maybe it should be ignored
                throw new IllegalArgumentException(i18n.getString("trt.invalidIURL", urls[i]));
            }
        }   // for

        if ((initNodes == null || initNodes.size() == 0) &&
            (initTests == null || initTests.size() == 0)) {
            if (debug == 1 || debug == 99)
                Debug.println("None of the initial URLs could be looked up.");

            return NullEnum.getInstance();
        }

        if (initTests.size() > 0) {
            if (debug == 1 || debug == 99)
                Debug.println("Using combo TreeIterator, " + initTests.size() +
                            " tests, " + initNodes.size() + " nodes.");
            return new TRT_Iterator(initNodes.toArray(new TreeNode[0]), initTests.toArray(new TestResult[0]), filters);
        }
        else
            return new TRT_Iterator(initNodes.toArray(new TreeNode[0]), filters);
    }

    /**
     * This method is the same as getIterator() with the same params.
     *
     * @param urls The test URLs to scan.  Values should have already be normalized to a
     *            '/' file separator.
     * @param filters The test filters to apply to any tests found.
     * @return An enumerator which return the union of tests specified by the
     *         URLs but not removed by the filters.
     * @see #getIterator()
     * @since 3.0
     */
    public Enumeration elements(String[] urls, TestFilter[] filters) {
        return getIterator(urls, filters);
    }

    /**
     * Find out the size of the entire tree.  This is a high overhead call, use
     * with caution.
     *
     * @return The number of tests in the tree.
     */
    public int size() {
        if (root == null)
            return 0;
        else
            return root.getSize();
    }

    /**
     * Insert the given test into the tree.
     *
     * @param tr The test to insert.  A null value is ignored.
     * @return The old value.  Null if none or the parameter was null.
     */
    TestResult insert(TestResult tr) {
        return insert(tr, false);
    }

    /**
     * Insert the given test into the tree.
     *
     * @param tr The test to insert.  A null value is ignored.
     * @param suppressScan Request that test finder activity be suppressed.
     * @return The old value.  Null if none or the parameter was null.
     */
    TestResult insert(TestResult tr, boolean suppressScan) {
        if (tr == null)
            return null;

        String key = tr.getWorkRelativePath();
        //maxDepth = 0;

        TRT_TreeNode[] path = new TRT_TreeNode[0];

        return insert(root, key, tr, path, suppressScan);
    }

    /**
     * Insert the given test and indicate that test's previous status.
     *
     * @param tr The test to insert, may or may not already exist in the table.
     *           A null value is ignored.
     * @param oldStatus The previous status of the given test.
     * @return The old result which was replaced.  Null if the parameter was null or
     *         there was no previous value.
     */
    TestResult insert(TestResult tr, Status oldStatus) {
        if (tr == null)
            return null;

        String key = tr.getWorkRelativePath();
        //maxDepth = 0;

        TRT_TreeNode[] path = new TRT_TreeNode[0];

        return insert(root, key, tr, path, false);
    }

    /**
     * Get the root TreeNode of this result table.
     *
     * @return The root of the tree.
     */
    public TestResultTable.TreeNode getRoot() {
        return root;
    }

    /**
     * Get the root URL of the test suite.
     * This may not match that given by the environment if the environment's
     * URL is partially invalid for some reason.
     *
     * @return A file representing the path to the root of the testsuite.
     */
    public File getTestSuiteRoot() {
        return suiteRoot;
    }

    /**
     * Get the finder that TRT is using to read the test suite.
     * Unless the TRT was constructed using a TestFinder, this value will
     * most likely be null.
     *
     * @return The active test finder.  Null if no finder is being used.
     * @see #setTestFinder
     * @since 3.0
     */
    public TestFinder getTestFinder() {
        return finder;
    }

    /**
     * Get the path to this node, relative to the root of the testsuite.
     * The returned URL does not have a trailing slash, nor does it begin
     * with one.
     * @param node The node to find the path to.
     * @return The URL to the given node, with '/' as the path separator.
     *         Zero length string if the node is a root.
     * @since 3.0
     */
    public static String getRootRelativePath(TreeNode node) {
        if (node.isRoot()) return "";

        StringBuffer name = new StringBuffer(node.getName());
        node = node.getParent();

        while (node != null && !node.isRoot()) {
            name.insert(0, '/');
            name.insert(0, node.getName());
            node = node.getParent();
        }

        return name.toString();
    }

    /**
     * Used to find a branch node somewhere in the tree based on a path.
     * If the <tt>path</tt> string is of zero length (the empty string), the
     * <tt>node</tt> parameter is returned.  This is desirable for proper operation
     * because the path to the root is the empty path.
     *
     * @param node Where to start the search
     * @param path The work relative position of the JTR (TestResult.getWorkRelativePath())
     * @return The node with the given path relative to the given node.  Null if not found.
     * @throws IllegalArgumentException If the starting node or path is null.
     * @since 3.0
     */
    public static TreeNode findNode(TreeNode node, String path) {
        if (node == null)
            throw new IllegalArgumentException(i18n.getString("trt.nodeNull"));
        if (path == null)
            throw new IllegalArgumentException(i18n.getString("trt.pathNull"));

        // special case, should only happen on first call of this method
        if (path.length() == 0)
            return node;

        String dir = getDirName(path);
        TreeNode tn = null;

        if (debug > 9)
            Debug.println("TRT.findNode() looking for " + path + " in " + node.getName());

        if (dir.equals(path)) { // last element of the path
            tn = ((TRT_TreeNode)(node)).getTreeNode(path, false);
        } else {                        // recurse
            TreeNode next = ((TRT_TreeNode)node).getTreeNode(dir, false);
            if (next != null)
                tn = findNode(next, behead(path));
            else { // not found
            }
        }

        return tn;
    }

    /**
     * Add a general purpose observer.
     *
     * @param o The observer to attach.  Must never be null.
     */
    public synchronized void addObserver(Observer o) {
        if (o == null)
            throw new NullPointerException();
        observers = (Observer[])DynamicArray.append(observers, o);
    }

    /**
     * Remove a general purpose observer.
     * Removing an observer which is not attached has no effect.
     *
     * @param o The observer to remove.
     */
    public synchronized void removeObserver(Observer o) {
        observers = (Observer[])DynamicArray.remove(observers, o);
    }

    /**
     * Add a tree-aware observer.
     *
     * @param obs The observer to attach.  Must never be null.
     */
    public void addObserver(TreeObserver obs) {
        treeObservers = (TreeObserver[])DynamicArray.append(treeObservers, obs);
    }

    /**
     * Remove a tree-aware observer.
     * Removing an observer which is not attached has no effect.
     *
     * @param obs The observer to remove.
     */
    public void removeObserver(TreeObserver obs) {
        if (treeObservers != null)
            treeObservers = (TreeObserver[])(DynamicArray.remove(treeObservers, obs));
    }

    /**
     * This method purges the given test, including attempting to delete the
     * associated JTR file, then replaces it with a basic <code>NOT_RUN</code>
     * test of the same name.  This operation has no effect if the given test
     * is not in the tree.
     * <p>
     * Matching objects for removal is done only by reference.  The operation
     * may fail (return <code>null</code>) if the test exists, but is not the
     * same object.  If you really want to remove a test by name, you can use
     * <code>resetTest(String)</code>.
     * <p>
     * NOTE: This method will cause waitUntilReady() to block.
     *
     * @param tr The test to find, purge and replace.
     * @return The new <code>NOT_RUN</code> test.  Null if the operation fails
     *         because the test could not be found.
     * @see #resetTest(String)
     * @since 3.0
     */
    public synchronized TestResult resetTest(TestResult tr) {
        TestResult newTest = null;

        workDir.log(i18n, "trt.rstTest", tr.getTestName());
        TreeNode[] location = getObjectPath(tr);

        if (location == null) {    // operation failed
            // do nothing
            // result = null
            newTest = lookup(tr.getWorkRelativePath());
            if (debug > 0)
                Debug.println("Recovered test by replacement (1). " + newTest);
        }
        else {
            TRT_TreeNode targetNode = (TRT_TreeNode)(location[location.length-1]);
            int index = targetNode.getIndex(tr, false);
            if (index >= 0) {
                newTest = targetNode.resetTest(index, tr);
                if (newTest == null && debug > 0)
                    Debug.println("reset of test " + tr.getTestName() + " failed.");
                else {
                    /*OLD
                    try {
                    */
                        // Insert into cache?
                        // this will cause a cache error on flush because it can't reload the
                        // result, but that should get it out of the cache
                        if (trCache != null) {
                            testsInUpdate.add(newTest);
                            trCache.insert(newTest);
                            testsInUpdate.remove(newTest);
                        }
                        notifyRemoveLeaf(location, tr, index);
                        notifyNewLeaf(location, newTest, index);
                    /*OLD
                    }   // try
                    catch (TestResultCache.Fault f) {
                        if (debug > 0)
                            f.printStackTrace(Debug.getWriter());
                        else { }

                        throw new JavaTestError(i18n, "trt.trcFault", f);
                    }   // catch
                    */
                }   // inner else
            }   // middle if
            else {
                newTest = lookup(tr.getWorkRelativePath());
                if (debug > 0)
                    Debug.println("Recovered test by replacement (2). " + newTest);
            }   // middle else
        }   // else

        return newTest;
    }

    /**
     * This method purges the given test, including attempting to delete the
     * associated JTR file, then replaces it with a basic <code>NOT_RUN</code>
     * test of the same name.  This operation has no effect if the given test
     * is not in the tree.  The <code>resetTest(TestResult)</code> method is
     * more efficient than this one, use it if you already have the object.
     * <p>
     * NOTE: This method may cause waitUntilReady() to block.
     *
     * @param testName The test to find, purge and replace.  This is of the form
     *        given by TestResult.getTestName().
     * @return The new <code>NOT_RUN</code> test.  Null if the given test name
     *         could not be found.
     * @see com.sun.javatest.TestResult#getTestName
     * @since 3.0
     */
    public synchronized TestResult resetTest(String testName) {
        TestResult tr = findTest(root, TestResult.getWorkRelativePath(testName), testName);
        if (tr == null)
            return null;
        else
            return resetTest(tr);
    }

    /**
     * Refresh a test if the files on disk have been modified since the test was read.
     * @param test The path for the test to be refreshed
     * @return true if a refresh was needed, false otherwise.
     * @throws TestResultTable.Fault if the test indicated cannot be located for
     *         refreshing.
     */
    public synchronized boolean refreshIfNeeded(String test) throws Fault {
        TestResult tr = lookup(TestResult.getWorkRelativePath(test));

        if (tr == null)
            throw new Fault(i18n, "trt.refreshNoTest", test);

        TreeNode[] path = getObjectPath(tr);

        if (path == null)
            return false;

        TRT_TreeNode tn = (TRT_TreeNode)path[path.length-1];
        TestResult newTr = tn.refreshIfNeeded(tr);

        if (newTr != tr)
            notifyChangeLeaf(TestResultTable.getObjectPath(tn),
                             newTr, tn.getTestIndex(newTr, false), tr);

        return false;
    }

    /**
     * Refresh a folder if the files on disk have been modified since the
     * folder was read. Notifies observers of the refresh happened.
     * @param node the node representing the folder to be refreshed
     * @return true if any refreshing was needed, false otherwise.
     * @throws TestResultTable.Fault if the node indicated cannot be located for
     *         refreshing.
     */
    public synchronized boolean refreshIfNeeded(TreeNode node) throws Fault {
        if (node.getEnclosingTable() != this)
            throw new IllegalStateException("refresh requested for node not owned by this table");

        notifyStartRefresh(node);
        try {
            return recursiveRefresh((TRT_TreeNode)node);
        } finally {
            notifyFinishRefresh(node);
        }
    }

    public synchronized boolean prune() throws Fault {
        if (root == null)
            return false;

        boolean changes = false;

        root.scanIfNeeded();
        TreeNode[] nodes = root.getTreeNodes();

        if (nodes == null)
            return changes;

        for (int i = 0; i < nodes.length; i++) {
            changes = (changes || prune(nodes[i]));
        }   // for

        return changes;
    }

    /**
     * @return True if some nodes were pruned, false otherwise.
     */
    synchronized public boolean prune(TreeNode node) throws Fault {
        TRT_TreeNode parent = ((TRT_TreeNode)(node.getParent()));

        if (node.getChildCount() == 0) {
            int index = parent.rmChild((TRT_TreeNode)node);

            if (index != -1)
                notifyRemoveLeaf(getObjectPath(parent), node, index);

            return (index != -1 ? true : false);
        }

        TreeNode[] nodes = node.getTreeNodes();

        if (nodes == null)
            return false; // must mean there are tests in this node

        for (int i = 0; i < nodes.length; i++) {
            prune(nodes[i]);
        }   // for

        if (node.getChildCount() == 0) {
            // prune
            int index = parent.rmChild((TRT_TreeNode)node);
            if (index != -1)
                notifyRemoveLeaf(getObjectPath(parent), node, index);

            return (index != -1 ? true : false);
        }

        return false;
    }

    // ------- Private methods begin --------

    /**
     * Temporary method to suppress finder activity.
     * Called from Harness when in batch mode.  You should
     * <b>not</b> change this after it has been set. It
     * should be set before iteration and insertion of
     * work directory information takes place.
     */
    void suppressFinderScan(boolean state) {
        if (state == false)
            suppressFinderScan = false;
        else if (workDir != null) {
            TestSuite ts = workDir.getTestSuite();
            if (ts != null && (
                ts.getTestRefreshBehavior(TestSuite.DELETE_NONTEST_RESULTS) ||
                ts.getTestRefreshBehavior(TestSuite.REFRESH_ON_RUN) ||
                ts.getTestRefreshBehavior(TestSuite.CLEAR_CHANGED_TEST)) ) {
                suppressFinderScan = false;
            }
            else
                suppressFinderScan = state;     // i.e. true
        }
        else
            suppressFinderScan = state;     // i.e. true

    }

    boolean isFinderScanSuppressed() {
        return suppressFinderScan;
    }

    /**
     * Determine if the path represented by the file is a branch or a leaf.
     * This is the semantic equivalent of File.isDirectory(), but shielded
     * behind this method for finders which do not use the filesystem.
     * @param f The file to check.  May not be null.
     */
    boolean isBranchFile(File f) {
        return finder.isFolder(f);
    }

    /**
     * Determine the last logical time that a file was modified.
     * This is the semantic equivalent of File.lastModified(), but shielded
     * behind this method for finders which do not use the filesystem.
     * @param f The file to check.  May not be null.
     */
    long getLastModifiedTime(File f) {
        // this must be upgraded for binary test finder scanning to work without
        // the actual files in the tests directory
        return finder.lastModified(f);
    }

    private class DisassembledUrl {
        private String[] data;
        private String initStr;

        public DisassembledUrl(String str) {
            data = StringArray.splitList(str, "/");
            initStr = str;
        }
    }

    private static class SortingComparator implements Comparator<DisassembledUrl> {
        Comparator<Object> c;

        public SortingComparator(Comparator c) {
            this.c = c;
        }

        public int compare(DisassembledUrl o1, DisassembledUrl o2) {
            String[] s1 = o1.data, s2 = o2.data;
            for (int i = 0; i < s1.length; i++) {
                if (i >= s2.length)
                    return 1;

                int comp = c.compare(s1[i], s2[i]);
                if (comp == 0)
                    continue;
                else
                    return comp;
            }

            // this case means that all s1 body exists in s2
            if (s2.length > s1.length)
                return -1;

            // s1 is exact copy of s2. Should not happen really.
            return 0;
        }
    }

    /**
     * Sort test and folder URLs by their ordering the tree, which is ultimately
     * determined by the <code>TestFinder</code>.
     */
    private String[] sortByName(String[] in) {
        if (in == null || in.length <= 1)
            return in;

        Comparator c = finder.getComparator();

        if (c == null) {
            // show warning message?
            c = Collator.getInstance(Locale.US);
            ((Collator)c).setStrength(Collator.PRIMARY);
        }

        DisassembledUrl[] elements = new DisassembledUrl[in.length];
        for (int i = 0; i < in.length; i++) {
            elements[i] = new DisassembledUrl(in[i]);
        }

        Arrays.sort(elements, new SortingComparator(c)); // using standard way to sort the array

        String result[] = new String[elements.length];
        int i = 0;
        for(DisassembledUrl s: elements)
            result[i++] = s.initStr;

        return result;
    }

    private int compareStringArrays(Comparator c, String[] s1, String[] s2) {
        // loop until names become unequal
        for (int i = 0; i < s1.length; i++) {
            if (i >= s2.length)
                return 1;

            int comp = c.compare(s1[i], s2[i]);
            if (comp == 0)
                continue;
            else
                return comp;
        }

        return 0;
    }

    /**
     * Removes from sorted array all overlapping entries.
     * E.g. {"root/a/b.html", "root/a/b.html#a", "root/a/b.htmls", "root/c", "root/c/d"}
     * would be {"root/a/b.html", "root/a/b.htmls", "root/c"}
     * complexity: n
     * complexity including quicksort: n*n*ln(n)
     *
     * @param urls A sorted list of test urls
     */
    public static String[] distillUrls(String[] urls) {
        // this method should guarantee that no one test would be runned twice.
        // testcases (in brackets are paths that should be thrown out):
        // 1) root, [root/a/b/c]; root/a, root/b, [root/b/c]; root/a.html, root/a.htmls; root/a.html, [root/a.html#boo]; root, [root/a], [root/b]

        // no need to process in these cases
        if (urls == null || urls.length <= 1)
            return urls;

        LinkedList<String> result = new LinkedList<String>();
        result.add(urls[0]);

        // as the array is expected to be sorted, it's known that foo/boo.html is just before foo/boo.html#anything

        String prev = urls[0]; // should not be testcase (should not contain '#' chars)
        if (prev.contains("#"))
            prev = "//##//"; // can't be start of testname
        for (int i = 1; i < urls.length; i++) {
            if (isPartOf(urls[i], prev)) {
                continue;
            }

            if (!urls[i].contains("#"))
                prev = urls[i];
            result.add(urls[i]);
        }


        if (result.size() == urls.length)
            // Nothing was thrown out. No need to reconstruct array.
            return urls;
        else {
            String[] s = new String[result.size()];
            return result.toArray(s);
        }
    }

    private static boolean isPartOf(String part, String of) {
        // if S2 is a part of S1 (e.g. S1="root/a", S2="root/a/t.html#2"), then S2 = S1 + diff, where diff can only start with '#' or '/'
        if (part.length() <= of.length())
            return false;

        if (part.startsWith(of) && (part.charAt(of.length()) == '#' || part.charAt(of.length()) == '/'))
            return true;

        return false;
    }

    /**
     * Creates a new array which contains all elements from missingIn ordered as in removeFrom
     *
     * @param removeFrom unsorted not distilled array
     * @param missingIn sorted and distilled array
     * @return a new array which contains all elements from missingIn ordered as in removeFrom
     */
    private String[] removeMissing(String[] removeFrom, String missingIn[]) {
        String[] result = new String[missingIn.length];
        int i = 0;
        outer:
        for (String path: removeFrom) {
            for (String url: missingIn) {
                // if a path is in distilled array - save it and continue with next
                if (path == url) {
                    result[i++] = path;
                    continue outer;
                }
            }
        }
        return result;
    }

    private synchronized void updateFromCache(Map m) {
        updateInProgress = true;

        // this method could use further optimization to take advantage
        //     of its sorted nature
        cachedResults = m;

        if (rtc.getRequests() != null) {
            for (TestDescription td : rtc.getRequests()) {
                String testRes = TestResult.getWorkRelativePath(td.getRootRelativeURL());
                TestResult tr = (TestResult)m.get(testRes);
                if (tr != null) {
                    tr.setTestDescription(td);
                    update(tr, suppressFinderScan);
                }
            }
        }
        rtc.clear();

        if (!cacheInitialized)
            cacheInitialized = true;

        updateInProgress = false;

        notifyAll();
    }

    /**
     * Recursively Insert the given test into the tree, recording the insertion
     * path along the way.  This is <em>not</em> a general purpose method.
     *
     * @param path Remaining part of the path.  Must not be null.
     *        The expected format is: foo/bar/baz.html#bear
     * @param tr   The test result object we are storing
     * @return The test result which was replaced by this operation, null if no
     *         previous entry existed.
     */
    synchronized TestResult insert(TRT_TreeNode node, String path, TestResult tr,
                                   TRT_TreeNode[] rec) {
        return insert(node, path, tr, rec, false);
    }

    /**
     * Recursively Insert the given test into the tree, recording the insertion
     * path along the way.  This is <em>not</em> a general purpose method.
     *
     * @param path Remaining part of the path.  Must not be null.
     *        The expected format is: foo/bar/baz.html#bear
     * @param tr   The test result object we are storing
     * @param suppressScan Request that test finder activity be suppressed.
     * @return The test result which was replaced by this operation, null if no
     *         previous entry existed.
     */
    synchronized TestResult insert(TRT_TreeNode node, String path, TestResult tr,
                                   TRT_TreeNode[] rec, boolean suppressScan) {
        if (debug > 9)
            Debug.println("TRT Beginning insert " + path);

        String newPath = behead(path);

        if (path == newPath) {
            // this should be the test name, make it a leaf

            // last parameter allows the TR to be dropped if it does not exist
            // in the test suite.
            TestResult oldTR = node.addChild(tr, suppressScan, !cacheInitialized);
            //tr.setParent(node);   // now done in TRT_TreeNode.addChild()
            rec = (TRT_TreeNode[])DynamicArray.append(rec, node);

            // index will be -1 if the node insertion was rejected
            // perhaps upgrade the code so that addChild() throws and
            // exception
            int index = node.getIndex(tr, suppressScan);

            if (oldTR == null) {
                if (debug > 10) {
                    Debug.println("   => Inserted TR: " + tr.getTestName());
                    Debug.println("   => Test Ref: " + tr);
                    Debug.println("   => Status is: " + Status.typeToString(tr.getStatus().getType()));
                    Debug.println("   => TRT: " + this);
                    Debug.println("   => Node Ref: " + node);
                    Debug.println("   => Node path: " + getRootRelativePath(node));
                    Debug.println("   => Index in node: " + node.getIndex(tr, suppressScan));
                }   // debug

                if (index != -1)
                    notifyNewLeaf(rec, tr, node.getIndex(tr, suppressScan));
            }
            else if (oldTR == tr) {
                if (debug > 10) {
                    Debug.println("   => Ignored new TR: " + tr.getTestName());
                    Debug.println("   => Test Ref: " + tr);
                    Debug.println("   => Status is: " + Status.typeToString(tr.getStatus().getType()));
                    Debug.println("   => RESETTING IT! " + updateInProgress);
                }

                if (updateInProgress)
                    resetTest(tr.getTestName());
            }
            else {
                if (debug > 10) {
                    Debug.println("   => Updated TR: " + tr.getTestName());
                    Debug.println("   => Test Ref: " + tr);
                    Debug.println("   => Status is: " + Status.typeToString(tr.getStatus().getType()));
                    Debug.println("   => TRT: " + this);
                    Debug.println("   => Node Ref: " + node);
                    Debug.println("   => Node path: " + getRootRelativePath(node));
                    Debug.println("   => Index in node: " + index);
                }   // debug

                if (index == -1) {
                    // insert was ignored for some reason
                }
                else if (oldTR != null && oldTR != tr) {
                    // handover known info if new tr is minimal
                    if (tr.isShrunk()) {
                        try {
                            TestDescription desc = oldTR.getDescription();
                            if (desc != null)
                                tr.setTestDescription(desc);
                        }
                        catch (TestResult.Fault f) {
                            // give up
                        }
                    }

                    //notifyChangeLeaf(rec, tr, index, oldTR);
                    notifyRemoveLeaf(rec, oldTR, index);
                    notifyNewLeaf(rec, tr, index);
                }
                else
                    notifyChangeLeaf(rec, tr, index, oldTR);
            }

            return oldTR;
        }
        else {
            // has at least 1 dir name left
            // find or create a TRT_TreeNode and follow it

            String nextDir = getDirName(path);
            TRT_TreeNode next = node.getTreeNode(nextDir, suppressScan);

            if (next == null) {     // create branch
                TRT_TreeNode tn = new TRT_TreeNode(this, node);
                tn.setName(getDirName(nextDir));
                node.addChild(tn, suppressScan);

                rec = (TRT_TreeNode[])DynamicArray.append(rec, tn);
                notifyNewBranch(rec, tn, node.getIndex(tn, suppressScan));

                return insert(tn, newPath, tr, rec, suppressScan);
            }
            else {                  // no work, just recurse
                rec = (TRT_TreeNode[])DynamicArray.append(rec, node);
                return insert(next, newPath, tr, rec, suppressScan);
            }

        }
    }

    /**
     *
     * @return true if any refreshing was needed, false otherwise.
     */
    private boolean recursiveRefresh(TRT_TreeNode node) {
        boolean result = node.refreshIfNeeded();

        TreeNode[] children = node.getTreeNodes();
        if (children != null)
            for (int i = 0; i < children.length; i++)
                result |= recursiveRefresh((TRT_TreeNode)children[i]);

        return result;
    }

    // package private for TRT_TreeNode for now
    // need a better solution
    void notifyNewBranch(TreeNode[] where, TreeNode what, int index) {
        if (treeObservers == null) return;

        for (int i = 0; i < treeObservers.length; i++) {
            treeObservers[i].nodeInserted(where, what, index);
        }
    }

    // package private for TRT_TreeNode for now
    // need a better solution
    void notifyNewLeaf(TreeNode[] where, TestResult what, int index) {
        if (treeObservers == null) return;

        for (int i = 0; i < treeObservers.length; i++) {
            treeObservers[i].nodeInserted(where, what, index);
        }
    }

    private void notifyChangeLeaf(TreeNode[] where, TestResult what, int index,
                          TestResult old) {
        if (treeObservers == null) return;

        for (int i = 0; i < treeObservers.length; i++) {
            treeObservers[i].nodeChanged(where, what, index, old);
        }
    }

    // package private for TRT_TreeNode for now
    // need a better solution
    void notifyRemoveLeaf(TreeNode[] where, TestResult what, int index) {
        if (treeObservers == null) return;

        for (int i = 0; i < treeObservers.length; i++) {
            treeObservers[i].nodeRemoved(where, what, index);
        }
    }

    void notifyRemoveLeaf(TreeNode[] where, TreeNode what, int index) {
        if (treeObservers == null) return;

        for (int i = 0; i < treeObservers.length; i++) {
            treeObservers[i].nodeRemoved(where, what, index);
        }
    }

    void notifyStartRefresh(TreeNode origin) {
        if (treeObservers == null) return;

        for (int i = 0; i < treeObservers.length; i++) {
            if (treeObservers[i] instanceof TreeEventObserver) {
                ((TreeEventObserver)treeObservers[i]).startRefresh(origin);
            }
        }
    }
    void notifyFinishRefresh(TreeNode origin) {
        if (treeObservers == null) return;

        for (int i = 0; i < treeObservers.length; i++) {
            if (treeObservers[i] instanceof TreeEventObserver) {
                ((TreeEventObserver)treeObservers[i]).finishRefresh(origin);
            }
        }
    }

    /**
     * Make all the initial files usable.  This method turns the file paths
     * into paths relative to the testsuite root.  It also removes any files
     * which match the testsuite root path.  The <tt>getPath()</tt> value is
     * used to get the path to resolve.
     *
     * @param tests Files to resolve.  May be zero length or null.
     * @return Root relative paths to the given file in internal URL format.
     *         Null <b>or</b> zero length if the given parameter was an empty
     *         set, or the given files could not be resolved in this TRT.
     * @throws Fault Will occur if any of the initial files are completely invalid.
     *         validatePath() can be used to validate things in advance.
     * @see #validatePath
     * @see java.io.File#getPath()
     */
    private String[] preProcessInitFiles(File[] tests) throws Fault {
        if (tests == null || tests.length == 0) {
            if (debug > 1)
                Debug.println("Initial files set empty.");
            return null;
        }

        if (debug > 1) {
            Debug.println("Initial files: ");
            for (int i = 0; i < tests.length; i++)
                Debug.println("  + " + tests[i].getPath());
        }

        String[] files = new String[tests.length];
        int filesLen = files.length;
        int distToDel =
            (getWorkDir() == null ? 0 : finder.getRootDir().getAbsolutePath().length() + 1);

        for (int i = 0; i < tests.length; i++) {
            if (debug > 1)
                Debug.println(" *** init url resolve begin ***");
            String relativeURL = null;

            if (finder.getRootDir().equals(tests[i])) {
                // jtreg produces initial URLs which are equal to the testsuite root,
                // this isn't necessary, so we ignore those
                //     maybe this should trigger a special case which causes all initial
                //     URLs to be ignored and runs the whole testsuite
                filesLen--;
                if (debug > 1)
                    Debug.println("An initial URL equals testsuite root, ignoring it.");

                continue;
            }
            else if (tests[i].isAbsolute()) {
                String rrp = getRootRelativePath(getRoot());

                if (debug > 1) {
                    Debug.println("  -> Initial URL is absolute, stripping from " +
                                       tests[i].getPath());
                    Debug.println("  -> Stripping: " + finder.getRootDir());
                    Debug.println("  -> removing rrp: " + rrp);
                }

                String thisInitPath = tests[i].getPath();

                if (!thisInitPath.startsWith(finder.getRootDir().getPath())) {
                    throw new Fault(i18n, "trt.badInitFile", thisInitPath);
                }

                distToDel += ((rrp == null || rrp.length() == 0) ? 0 : rrp.length() + 1);

                // strip length of testsuite root
                String platformPath = thisInitPath.substring(distToDel);

                relativeURL = platformPath.replace(File.separatorChar, '/');
            }
            else
                relativeURL = tests[i].getPath().replace(File.separatorChar, '/');

            files[i] = relativeURL;
        }

        if (filesLen != tests.length) {
            String[] newFiles = new String[filesLen];
            System.arraycopy(files, 0, newFiles, 0, filesLen);
            files = newFiles;
        }

        if (debug > 1)
            Debug.println("*** finished preprocessing of init urls ***");

        return files;
    }

    /**
     * @param where Where to start searching.
     * @param fullPath Work relative path to JTR to look for.  Must not be null.
     * @param path Current part of the path, used to support recursion.
     * @return null will be returned if the node cannot be located.
     */
    private static TestResult findTest(TreeNode where, String fullPath, String path) {
        ((TRT_TreeNode)where).scanIfNeeded();

        // this is an internal routine, so params are expected to be non-null

        if (debug == 2 || debug == 99)
            Debug.println("TRT looking for " + path + " IN " + where.getName());

        String dir = TestResultTable.getDirName(path);
        TestResult tr = null;

        if (dir == path) {
            if (debug == 2 || debug == 99)
                Debug.println("    -> Looking for TR in this node.");

            int location = ((TRT_TreeNode)where).getResultIndex(fullPath, false);
            if (location != -1) {
                tr = (TestResult)(where.getChild(location));
                if (debug == 2 || debug == 99) {
                    Debug.println("    -> TRT.findTest() located " + tr);
                    Debug.println("");
                }   // debug
            }
            else {
                // not found, branches exist, leaf does not
                if (debug == 2 || debug == 99) {
                    Debug.println("    -> TRT.findTest(): unable to find node " + fullPath);
                    Debug.println("");
                }   // debug
            }
        }
        else {
            if (debug == 2 || debug == 99)
                Debug.println("    -> Looking for branch name: " + dir);

            TRT_TreeNode tn = ((TRT_TreeNode)where).getTreeNode(dir, false);

            if (tn != null) {
                // go down a level
                tr = findTest(tn, fullPath, TestResultTable.behead(path));
            }
            else {
                // not found, a branch name does not exits
                if (debug == 2 || debug == 99)
                    Debug.println("TRT.findTest(): unable to find node " + fullPath);
            }
        }   // outer else

        return tr;
    }

    /**
     * Get a node by path.
     * @since 3.2
     */
    private static Object lookupNode(TreeNode where, String url) {
        TreeNode tn = findNode(where, url);
        // matched a branch!
        if (tn != null) {
            return tn;
        }

        // if url is exec/index.html#ExecSucc
        // that should match a test with exactly that name
        String jtrPath = TestResult.getWorkRelativePath(url);
        TestResult tr = findTest((TRT_TreeNode)where, jtrPath, jtrPath);
        return tr;      // may be null
    }

    /**
     * Find a branch or leaf which matches the URL.
     * This method searches in the following order for the given URL:
     * <ul>
     *    <li>Branch match
     *    <li>Test URL match (up to and including the test id)
     *    <li>Set of tests match (e.g. multiple test ids, same file)
     * </ul>
     *
     * This method is designed to deprecate lookup(TreeNode, String[]).
     *
     * @param where Node to start recursive search at.
     * @param url The directory name, test URL (TestDescription.getRootRelativeURL), or
     *            file prefix of a set of test ids (index.html might match {index.html#t1,
     *            index.html#t2, index.html#t3})
     * @return Null if no matches.  A TreeNode, or a non-empty set of TestResults.
     */
    private static Object[] lookupInitURL(TreeNode where, String url) {
        if (where == null || url == null)
            throw new IllegalArgumentException("Starting node or URL may not be null!");

        if (debug == 2 || debug == 99) {
            Debug.println("Starting iurl lookup on " + url + " in " + where.getName());
        }

        Object simple = lookupNode(where, url);
        if (simple != null) {
            if (debug == 2 || debug == 99 && simple instanceof TreeNode) {
                Debug.println("  -> simple match found " + getRootRelativePath((TreeNode)simple));
            }

            if (simple instanceof TestResult)
                return new TestResult[] {(TestResult)simple};
            else
                return new TreeNode[] {(TreeNode)simple};
        }

        // first find the node directly above where we want to search
        // if the url is foo/fivetests.html
        // that should match foo/fivetests.html#1
        //                   foo/fivetests.html#2
        //                   foo/fivetests.html#3
        //                   foo/fivetests.html#4
        //                   foo/fivetests.html#5
        if (debug == 2 || debug == 99) {
            Debug.println("TRT looking for tests beginning with " + url + " IN " + where.getName());
            Debug.println("   -> retrieving possible TRs from " + betail(url));
        }
        TreeNode tn = findNode(where, betail(url));
        if (tn == null) {   // the parent dir of the requested test does not exist
            if (debug == 2 || debug == 99)
                Debug.println("   -> No parent node found!");
            return null;
        }

        TestResult[] trs = tn.getTestResults();
        // found anything?
        if (trs == null || trs.length == 0)
            return null;

        // try to partial match a test
        Vector v = new Vector();
        try {
            for (int i = 0; i < trs.length; i++) {
                if (trs[i].getDescription().getRootRelativeURL().startsWith(url))
                    v.addElement(trs[i]);           // match
            }   // for
        }
        catch (TestResult.Fault f) {
            // this is a very bad thing I think
            throw new JavaTestError(i18n, "trt.trNoTd", f);
        }

        if (v.size() > 0) {
            trs = new TestResult[v.size()];
            v.copyInto(trs);
        }
        else {
            // no matches
            trs = null;
        }

        return trs;
    }

    /**
     * Once the finder variable has been set, this should be called to initialize
     * the rest of the object state.
     */
    private void initFinder() {
        suiteRoot = finder.getRoot();
    }

    void awakeCache() {
        if (trCache == null || !trCache.workerAlive()) {
            try {
                trCache = new TestResultCache(workDir, updater);
            }
            catch (IOException e) {
                // should consider throwing Fault, but that will destabilize too many APIs for now
                updater.error(e);
            }
        }
    }


    private boolean needsCacheCompress() {
        /*OLD
        int etc = workDir.getTestSuiteTestCount();

        if (etc > 0) {
            int percent = uncompressedTestCount / etc;

            if (percent > COMPRESSION_THRESHOLD) {
                return true;
            }
        }

        if (trCache.needsCompress())
            return true;

        return false;
        */
        return trCache.needsCompress();
    }

    protected void finalize() throws Throwable {
        super.finalize();
        // cleanup all the http stuff
        RootRegistry.getInstance().removeHandler(httpHandle);
        RootRegistry.unassociateObject(this, httpHandle);
        httpHandle = null;
    }

    /**
     * Update the flag indicating that an update is in progress and notify
     * any and all threads that might be interested.
     */
    private synchronized void setUpdateInProgress(boolean b) {
        updateInProgress = b;
        notifyAll();
    }

    /**
     * This method returns TestResult from map of test results, collected by
     * TestResultCache worker. If worker didn't finished his work yet, method
     * returns null and adds TestDescription of requested result to a special
     * set.
     * TestResults from this set will be updated after cache worker finishes
     * its work.
     */
    public TestResult getCachedResult(TestDescription td) {
        if (cachedResults != null) {
            String url = TestResult.getWorkRelativePath(td.getRootRelativeURL());
                TestResult res = (TestResult)cachedResults.get(url);
                if (res != null) {
                    res.setTestDescription(td);
                }
                return res;
        }
        else {
            rtc.addToUpdateFromCache(td);
            return null;
        }
    }

    /**
     * Inner class, which specifies methods to work with set of TestDescriptions,
     * which need to be updated after cache will finish his work.
     */
    private class RequestsToCache {
        private HashSet<TestDescription> needUpdateFromCache;

        public synchronized void addToUpdateFromCache(TestDescription td) {
            if (needUpdateFromCache == null) {
                needUpdateFromCache = new HashSet();
            }
            needUpdateFromCache.add(td);
        }

        public HashSet<TestDescription> getRequests () {
            return needUpdateFromCache;
        }

        public synchronized void clear() {
            needUpdateFromCache = null;
        }
    }

    private Map<String, TestResult> cachedResults;
    private RequestsToCache rtc;

    /**
     * Update the flag indicating that the cache has been initialized and notify
     * any and all threads that might be interested.
     */
    /*OLD
    private synchronized void setCacheInitialized(boolean b) {
        cacheInitialized = b;
        notifyAll();
    }
    */

    /*OLD
    private static final String formatVersion = "JavaTest/Results/2.0";
    */
    private Hashtable[] statusTables;
                                // tables indexed by status.type mapping status.reason
                                // to a unique status object
    private WorkDirectory workDir;
    private TestFinder finder;
    private String[] finderErrors =new String[0];
    private Observer[] observers = new Observer[0];
    private TRT_HttpHandler httpHandle;     // the http handler for this instance
    private TreeObserver[] treeObservers = new TreeObserver[0];
    private TestResultCache trCache;

    private boolean suppressFinderScan = false; // false is traditional

    private Updater updater = new Updater();

    /**
     * You must hold the lock on this object first, then change this variable, then
     * release in the opposite order.
     */
    private volatile boolean updateInProgress;
    private volatile boolean cacheInitialized = false;

    private Vector testsInUpdate = new Vector();

    /*
     * Effectively a count of the number of instances of TRTs that have been created.
     * It does not represent the number of existing instances though; the
     * purpose of the counter is to provide a unique identifier.  To use, increment
     * the number then use it.
     */
    private static int instanceId;

    /*OLD
     * How much of the testsuite must be run before we force a cache
     * compression.
     * /
    private static float COMPRESSION_THRESHOLD = 0.40f;
    */

    private TRT_TreeNode root;
    private File suiteRoot;

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(TestResultTable.class);

    // BEGIN INNER CLASSES
    // the public interface to a node.  impl. is in TRT_TreeNode
    /**
     * Interface to a node which contitutes the skeleton of the test result tree.
     * These are in most cases equivalent to directories or folders.
     */
    public interface TreeNode {
        /**
         * Add a observer for this particular node.
         *
         * @param obs The observer to attach to this node.  Should never be
         *        null.
         */
        public void addObserver(TestResultTable.TreeNodeObserver obs);

        /**
         * Add a observer for this particular node.
         * @param obs The observer to remove.  No effect if it was never
         *        attached.
         */
        public void removeObserver(TestResultTable.TreeNodeObserver obs);

        /**
         * Find out how many nodes are contained in this one and below.
         * Use with care, this may be a high overhead call.
         *
         * @return The number of tests under this node.
         */
        public int getSize();

        /**
         * Get the parent of this node.
         *
         * @return Null if this is the root.  Another TreeNode otherwise.
         */
        public TreeNode getParent();

        /**
         * Is this the root of a tree.
         * A parent of null indicates this state.
         *
         * @return True if this is the root node, false otherwise.
         */
        public boolean isRoot();

        /**
         * Find out what TestResultTable this node is part of.
         *
         * @return The TRT instance which this node is contained.
         */
        public TestResultTable getEnclosingTable();

        /**
         * Has the finder been used to scan this node from secondary storage?.
         * This is an important performance consideration, since reading nodes
         * may cause a noticable pause.  This method may allow you to defer
         * performance penalties.  This method has no effect if a finder is not
         * being used by the TestResultTable.
         *
         * @return True if this location in tree has already been processed,
         *         false otherwise.
         */
        public boolean isUpToDate();

        /**
         * Find out how many children this node contains.
         * If you invoke this on a node which is being lazily read from a
         * TestFinder, this may cause a synchronous retrieval of data from the
         * TestFinder.
         *
         * @return The number of immediate children this node has.
         */
        public int getChildCount();

        /**
         * Get the child at the specified location.
         * May be either a TestResult or TreeNode.
         * If you invoke this on a node which is being lazily read from a
         * TestFinder, this may cause a synchronous retrieval of data from the
         * TestFinder.
         *
         * @param index The location to retrieve.
         * @return Null if there are no children here or the specified index if out of
         *         range.
         */
        public Object getChild(int index);

        /**
         * Get any immediate children of this node which are test result objects.
         *
         * @return List of TestResult objects in this node.  null if none
         */
        public TestResult[] getTestResults();

        /**
         * Get any immediate children of this node which are tree nodes.
         *
         * @return List of children nodes objects in this node.  null if none
         */
        public TreeNode[] getTreeNodes();

        /**
         * The name of this node, not including all the ancestors names.
         * This does not return a URL to this node from the root.
         *
         * @return Immediate name of this node, not the full name.
         * @see #getRootRelativePath
         */
        public String getName();

        /**
         * Is the given element of this node a leaf.
         * In general this means that the element is a TestResult.  It may also
         * mean that the TreeNode is empty, which indicates a testsuite which is not
         * well designed and needs trimming.
         *
         * @param index The element index of this node.  An out of range index
         *        will return false.
         * @return True if the element at the given index is a leaf.
         */
        public boolean isLeaf(int index);

        /**
         * Get the statistics for the state of tests under this node.
         *
         * @return An array of length Status.NUM_STATES, positionally representing
         *         the number of tests under this node with that state.
         */
        public int[] getChildStatus();

        /**
         * Search for a specific item in this node.
         *
         * @param target The target object should either be of type TreeNode or TestResult
         * @return The index at which the target object is located in this node.
         *         -1 if not found in in this node.  -2 if the parameter is null
         */
        public int getIndex(Object target);

        /**
         * Finds a TestResult in this node with the given name.
         * This is a match against the test URL, not the filename.  This is not
         * recursive of course; it just does a straight match against all tests
         * in this node.
         *
         * @param url The full name of the test to find.
         * @return The matching test result object, or null if not found.  Also
         *         null if this node has no children.
         * @see com.sun.javatest.TestDescription#getRootRelativeURL()
         */
        public TestResult matchTest(String url);
    }   // TreeNode

    private static class NullEnum extends TRT_Iterator {
        private NullEnum() {
            super();
        }

        // --- Iterator interface ---
        public boolean hasNext() {
            return hasMoreElements();
        }

        public Object next() {
            return nextElement();
        }

        /**
         * Do not call this method.
         *
         * @throws UnsupportedOperationException Not available for this iterator.
         */
        public void remove() {
            throw new UnsupportedOperationException("Cannot remove from TestResultTable thhrough iterator.  Do not call this method.");
        }

        // --- Enumerator interface ---
        public boolean hasMoreElements() {
            return false;
        }

        public Object nextElement() {
            throw new NoSuchElementException(i18n.getString("trt.noElements"));
        }

        public static NullEnum getInstance() {
            if (instance == null) instance = new NullEnum();

            return instance;
        }

        private static NullEnum instance;
    }   // NullEnum

    /**
     * Defines an iterator/enumerator interface for retrieving tests out of the
     * tree.  This is a read-only interface, so <code>remove()</code> is not
     * supported.
     */
    public interface TreeIterator extends Enumeration, Iterator {
        // --- Enumerator interface  ---
        public abstract boolean hasMoreElements();
        public abstract Object nextElement();

        // --- Iterator interface ---
        public abstract boolean hasNext();
        public abstract Object next();

        /**
         * Do not call this method.
         *
         * @throws UnsupportedOperationException Not available for this iterator.
         */
        public abstract void remove();

        // --- Statistics info ---
        /**
         * Find out how many tests were rejected by filters while doing iteration.
         * This number will be available despite the setting on setRecordRejects().
         *
         * @return The number of tests found by rejected by the filters.  The
         *         value will be between zero and max. int.
         */
        public abstract int getRejectCount();

        /**
         * Should the rejected tests be tracked.
         * The default is to not record this info, activating this feature will
         * make <tt>getFilterStats()</tt> return useful info.  The setting can
         * be changed at any time, but never resets the statistics.  The
         * recorded statistics represent iterator output during this object's
         * lifetime, while this feature was enabled.  The setting here does not
         * affect information from <tt>getRejectCount()</tt>
         * @param state True to activate this feature, false to disable.
         * @see com.sun.javatest.TestResultTable.TreeIterator#getFilterStats()
         */
        public abstract void setRecordRejects(boolean state);

        /**
         * Find out which states the test which have been enumerated already were in.
         * The result is valid at any point in time, and represent the stats for the
         * entire selected set of tests when hasMoreElements() is false.
         *
         * @return Indexes refer to those values found in Status
         * @see com.sun.javatest.Status
         */
        public abstract int[] getResultStats();

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
         * @since 3.0.3
         */
        public abstract Hashtable getFilterStats();

        // --- misc info ---
        /**
         * Find out what the effective filters are.
         *
         * @return Null if there are no active filters.
         */
        public abstract TestFilter[] getFilters();

        /**
         * Find out what the effective initial URLs for this enumerator are.
         * The returned array can be any combination of URLs to individual tests
         * or URLs to directories.  Remember these are URLs, so the paths are not
         * platform specific.
         *
         * @return Null if no nodes or tests were found.  Any array of the initial
         *         URLs otherwise.
         */
        public abstract String[] getInitialURLs();

        /**
         * Peek into the future to see which object will be returned next.
         * @return The next object scheduled to come out of <code>next()</code>,
         *         or null if <code>hasNext()</code> is false.
         */
        public abstract Object peek();

        /**
         * Will the iterator be returning the given node later.
         * There is no checking to ensure that the parameter is within the
         * iterator's "address space".  The comparison is not reference based,
         * but location based, so, will the test at the location indicated by
         * the given test be evaluated for iterator later?  This query is done
         * without respect to the filters.
         * @param node The test result indicating the location in question.
         * @return True if the test in question has not been passed by the
         *         iterator and may still be returned.  False if the given
         *         test will not subsequently be returned by this iterator.
         */
        public abstract boolean isPending(TestResult node);
    }   // TreeIterator

    class Updater implements TestResultCache.Observer
    {
        //-----methods from TestResultCache.Observer-----
        public void update(Map tests) {
            updateFromCache(tests);
        }

        public void waitingForLock(long timeSoFar) {
            // in time, could propogate this message to TRT.Observer so that
            // GUI code could present the info better, but, for now, stay basic
            int seconds = (int) (timeSoFar/1000);
            int minutes = seconds/60;
            seconds = seconds - 60*minutes;
            writeI18N("trt.waitingForLock",
                      new Object[] { workDir.getRoot(), new Integer(minutes), new Integer(seconds) });
        }

        public void timeoutWaitingForLock() {
            // in time, could propogate this message to TRT.Observer so that
            // GUI code could present the info better, but, for now, stay basic
            writeI18N("trt.timeoutForLock", workDir.getRoot());
        }

        public void acquiredLock() {
            // in time, could propogate this message to TRT.Observer so that
            // GUI code could present the info better, but, for now, stay basic
        }

        public void releasedLock() {
            // in time, could propogate this message to TRT.Observer so that
            // GUI code could present the info better, but, for now, stay basic
        }

        public void buildingCache(boolean reset) {
            // in time, could propogate this message to TRT.Observer so that
            // GUI code could present the info better, but, for now, stay basic
            rebuildCount = 0;
        }

        public void buildingCache(TestResult tr) {
            // in time, could propogate this message to TRT.Observer so that
            // GUI code could present the info better, but, for now, stay basic
            rebuildCount++;
            if (rebuildCount == 100)
                writeI18N("trt.rebuild");
            else if ((rebuildCount % 100) == 0)
                System.err.println(".");
        }

        public void builtCache() {
            // in time, could propogate this message to TRT.Observer so that
            // GUI code could present the info better, but, for now, stay basic
            if (rebuildCount > 0)
                System.err.println();
        }

        public void error(Throwable t) {
            // in time, could propogate this message to TRT.Observer so that
            // GUI code could present the info better, but, for now, stay basic
            writeI18N("trt.cacheError", t);
            t.printStackTrace();
        }

        private void writeI18N(String key) {
            System.err.println(i18n.getString(key));
        }

        private void writeI18N(String key, Object arg) {
            System.err.println(i18n.getString(key, arg));
        }

        private void writeI18N(String key, Object[] args) {
            System.err.println(i18n.getString(key, args));
        }

        private int rebuildCount;

        /*OLD
        public void newEntry(TestResult result) {
            if (debug > 9)
                Debug.println("TRT - New TR from cache: " + result.getWorkRelativePath());

            if (testsInUpdate.contains(result)) {
                // this TRT instance just inserted this test
                if (debug > 9)
                    Debug.println("   -> ignoring");

                return;
            }

            if (result != null)
                insert(result);
        }
        */

        /*
        public void cacheResetting() {
            if (debug)
                Debug.println("TRT - cache indicates restart beginning.");
        }
        */

        /*OLD
        public void resetCache()  {
            synchronized (TestResultTable.this) {
                while (updateInProgress) {
                    try {
                        TestResultTable.this.wait(3000); // will be notified when updated; time allows debug monitor

                        // abort
                        if (cacheShutdown)
                            return;
                    }
                    catch (InterruptedException e) {
                        if (debug > 0)
                            e.printStackTrace(Debug.getWriter());
                    }   // catch
                }   // while

                setUpdateInProgress(true);
            }

            // hope this doesn't happen too often, because it is really
            // expensive
            if (debug > 0)
                Debug.println("TRT(" +TestResultTable.this+") - Received cache reset message.");

            // NOTE: this will result in flushing the pre-existing test
            //       results which may contain full JTR info.  getEntries()
            //       always produces minimal size TR objects

            Thread updateThread = new Thread() {
                { setName("TRT Background Cache Reset"); }

                public void run() {
                    // too expensive to do in the foreground
                    try {
                        final Set newSet = trCache.getEntries();

                        // abort if not needed
                        if (cacheShutdown) {
                            setUpdateInProgress(false);
                            return;
                        }

                        update(newSet);
                    }
                    catch (TestResultCache.CacheShutdownFault f) {
                        // ignore, handled below...
                    }
                    catch (TestResultCache.Fault f) {
                        if (debug > 0)
                            f.printStackTrace();
                        throw new JavaTestError(i18n, "trt.noEntries", f);
                    }   // catch

                    setUpdateInProgress(false);

                }

                protected void finalize() throws Throwable {
                    setUpdateInProgress(false);
                }
            };  // anonymous thread

            // abort if not needed
            if (cacheShutdown) {
                return;
            }

            updateThread.setPriority(Thread.MIN_PRIORITY + 2);
            updateThread.start();
        }
        */

        /*OLD
        public void cacheShutdown() {
            cacheShutdown = true;
        }
        */
    }

    /**
     * Keeps track of the path to a specific node in the TestResultTable tree.
     * This class is made available so that a path which consists of zero or more
     * TreeNodes plus zero or one TestResult can be represented without using a
     * Object[].
     */
    public class PathRecord {
        PathRecord() {
        }

        /**
         * Create a path with only one element, the target TestResult.
         */
        PathRecord(TestResult tr) {
            this.tr = tr;
        }

        /**
         * Create a path which represents the path to a leaf node, the TestResult.
         */
        PathRecord(TreeNode[] path, TestResult tr) {
            this.tr = tr;
            nodes = path;
        }

        /**
         * @param path The TreeNode objects that correspond to the path.
         * @param inds The indexes of each TreeNode in the tree.
         *             This is really duplicated information, but will remove the
         *             need to search for a node at each level.
         */
        PathRecord(TreeNode[] path, int[] inds) {
            nodes = path;
            this.inds = inds;
        }

        /**
         * The end of the path.
         */
        void setTestResult(TestResult tr) {
            this.tr = tr;
        }

        /**
         * @exception JavaTestError Will be thrown if you attempt to add a node to a
         *            path that already has a leaf (TestResult) assigned.
         */
        void addNode(TreeNode tn) {
            if (tr != null) throw new JavaTestError(i18n, "trt.invalidPath");

            nodes = (TreeNode[])DynamicArray.append(nodes, tn);
        }

/*
        public synchronized void append(Object node, int index) {
            int[] newArr = new int[inds.length+1];
            System.arraycopy(inds, 0, newArr, 0, inds.length);
            inds = newArr;

            nodes = DynamicArray.append(nodes, node);

            System.out.println("Path Recorded");
            for (int i = 0; i < nodes.length; i++)
                System.out.println(nodes[i] + "   " + inds[i]);
        }
*/

        /**
         * Provides the indexes into each node provided by <code>getNodes()</code>.
         *
         * @return The indexes of the corresponding TreeNode at each level.  Null if
         *         no index information is available;
         */
        public int[] getIndicies() {
            if (inds == null) {
                inds = generateInds(tr);
            }

            return inds;
        }

        /**
         * Get the nodes that represent the path.
         *
         * @return The path, closest to the root at the beginning of the array.
         */
        public TreeNode[] getNodes() {
            if (nodes == null) {
                nodes = generateNodes(tr);
            }

            return nodes;
        }

        /**
         * Generate the path to a given test.
         *
         * @param tr The test to generate the path to.
         * @return The path that leads to the given test.
         */
        public /* static */ TreeNode[] generateNodes(TestResult tr) {
            if (tr == null)  return null;

            TreeNode[] nodes = null;
            TreeNode node = tr.getParent();

            while (node != null) {
                nodes = (TreeNode[])DynamicArray.insert(nodes, node, 0);
                node = node.getParent();
            }

            return nodes;
        }

        private /* static */ int[] generateInds(TestResult tr) {
            // XXX implement me!
            return null;
        }

        private TreeNode[] nodes;
        private int[] inds;
        private TestResult tr;
    }   // PathRecord

    /**
     * Remove one directory from the beginning of the path.
     *
     * @param path The path to manipulate.
     * @return Beheaded path, or the <b>same</b> object if there is
     *         no leading directory to strip.
     */
    static String behead(String path) {
        int index = path.indexOf("/");

        if (index == -1)
            return path;
        else {
            // assume file separator is 1 char
            return path.substring(index+1);
            //return path.substring(index+File.separator.length());
        }
    }

    /**
     * Gives the first directory name in the path.
     *
     * @param path The path to manipulate.
     */
    static String getDirName(String path) {
        int index = path.indexOf('/');

        if (index == -1)
            return path;
        else
            return path.substring(0, index);
    }

    /**
     * Opposite of behead - removes the last filename.
     *
     * @param path The path to manipulate.
     */
    static String betail(String path) {
        int index = path.lastIndexOf('/');

        if (index == -1)
            return path;
        else
            return path.substring(0, index);
    }

    /**
     * Does the given array contain the given object.
     * @return True if o is in arr, false if arr is null or zero length,or
     *         does not contain o.
     */
    static boolean arrayContains(Object[] arr, Object o) {
        if (arr == null || arr.length == 0)
            return false;
        else {
            for (int i = 0; i < arr.length; i++)
                if (arr[i] == o)
                    return true;
        }

        return false;
    }

    private ReentrantLock processLock = new ReentrantLock();

    public ReentrantLock getLock() {
        return processLock;
    }

    static final Status notYetRun = Status.notRun("test is awaiting execution");
    private static int debug = Debug.getInt(TestResultTable.class);
}
