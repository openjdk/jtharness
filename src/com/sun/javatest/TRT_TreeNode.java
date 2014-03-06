/*
 * $Id$
 *
 * Copyright (c) 2001, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 *
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or questions.
 *
 */

package com.sun.javatest;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.HashMap;

import com.sun.javatest.util.Debug;
import com.sun.javatest.util.DynamicArray;
import com.sun.javatest.util.I18NResourceBundle;

import com.sun.javatest.TestResultTable.TreeNode;
import java.util.Iterator;

/**
 * This is the implementation of a tree node structure for TestResultTable.
 * Only the interface implementation is meant to be exposed.  Assumptions are made
 * that this is the only node class (implementation of TreeNode) used in the tree.
 */

public class TRT_TreeNode implements TestResultTable.TreeNode {
    // ------- See interface in TestResultTable for docs. on these methods
    /**
     * Add an observer to watch this node for changes.
     */
    public synchronized void addObserver(TestResultTable.TreeNodeObserver obs) {
        TestResultTable.TreeNodeObserver[] observers =
            (TestResultTable.TreeNodeObserver[])observerTable.get(this);

        if (observers == null) observers = new TestResultTable.TreeNodeObserver[0];
        observers = (TestResultTable.TreeNodeObserver[])(DynamicArray.append(observers, obs));
        observerTable.put(this, observers);
    }

    /**
     * Remove an observer that was previously added.
     */
    public synchronized void removeObserver(TestResultTable.TreeNodeObserver obs) {
        TestResultTable.TreeNodeObserver[] observers =
            (TestResultTable.TreeNodeObserver[])observerTable.get(this);
        if (observers == null)
            return;

        observers = (TestResultTable.TreeNodeObserver[])(DynamicArray.remove(observers, obs));
        if (observers == null)
            observerTable.remove(this);
        else
            observerTable.put(this, observers);
    }

    /**
     * Find out how many tests are in this node and below.
     * If you invoke this on a node which is being lazily read from a
     * TestFinder, this may cause a synchronous retrieval of data from the
     * TestFinder.  <b>Use with caution!</b>
     */
    public int getSize() {
        scanSubtree(this);

        return counter;
    }

    /**
     * Get the estimated number of tests below this node.  Mainly useful for
     * low contention hints about the size.  No locking is involved in retrieving
     * the information.
     */
    public int getEstimatedSize() {
        return counter;
    }

    public TestResultTable.TreeNode getParent() {
        return parent;
    }

    public boolean isRoot() {
        if (parent != null) return false;
        else return true;
    }

    public TestResultTable getEnclosingTable() {
        return table;
    }

    public boolean isUpToDate() {
        // compare timestamp in the future
        if (lastScanDate == 0) return false;
        else return true;
    }

    /**
     * Find out how many children this node contains.
     * If you invoke this on a node which is being lazily read from a
     * TestFinder, this may cause a synchronous retrieval of data from the
     * TestFinder.
     */
    public int getChildCount() {
        scanIfNeeded();

        if (childs == null) return 0;
        else return childs.length;
    }

    public Object getChild(int index) {
        return getChild(index, false);
    }

    /**
     * Get the child at the specified location.
     * May be either a TestResult or TestResultTable.TreeNode.
     * If you invoke this on a node which is being lazily read from a
     * TestFinder, this may cause a synchronous retrieval of data from the
     * TestFinder.
     *
     * @return Null if there are no children here or the specified index if out of
     *         range.
     */
    Object getChild(int index, boolean suppressScan) {
        if (!suppressScan)
            scanIfNeeded();

        if (childs == null || index >= childs.length)
            return null;
        else
            return childs[index];
    }

    /**
     * @return List of TestResult objects in this node.  null if none
     */
    public TestResult[] getTestResults() {
        scanIfNeeded();

        TestResult[] leafs = null;

        if (childs != null && childs.length != 0) {
            for (int i = 0; i < childs.length; i++) {
                if (childs[i] instanceof TestResult)
                    leafs = (TestResult[])(DynamicArray.append(leafs, childs[i]));
            }   // for
        }

        return leafs;
    }

    /**
     * Get only the children of this node which are branches.
     * @return List of children nodes objects in this node.  null if none.
     */
    public TestResultTable.TreeNode[] getTreeNodes() {
        scanIfNeeded();

        if (childs == null)
            return null;

        TestResultTable.TreeNode[] leafs = null;

        for (int i = 0; i < childs.length; i++) {
            if (childs[i] instanceof TestResultTable.TreeNode)
                leafs = (TestResultTable.TreeNode[])(DynamicArray.append(leafs, childs[i]));
        }

        return leafs;
    }

    public String getName() {
        return name;
    }

    public boolean isLeaf(int index) {
        scanIfNeeded();

        if (index < 0 || index >= childs.length)
            return false;
        else if (childs[index] instanceof TestResult)
            return true;
        else if (childs[index] instanceof TRT_TreeNode) {
            // if there are no nodes or tests below, then...
            return (childs == null || childs.length == 0);
        }
        else        // should never be the case
            return false;
    }

    public int[] getChildStatus() {
        scanSubtree(this);

        if (childStats == null)
            refreshChildStats(this);

        return childStats;
    }

    public int getIndex(Object target) {
        return getIndex(target, false);
    }

    int getIndex(Object target, boolean suppressScan) {
        if (!suppressScan)
            scanIfNeeded();

        if (target == null)
            return -2;
        else if (childs == null)
            return -1;      // not found
        else {
            for (int i = 0; i < childs.length; i++)
                if (childs[i] == target) return i;
        }

        // not found
        return -1;
    }

    public TestResult matchTest(String url) {
        scanIfNeeded();

        if (debug > 1)
            Debug.println("Matching Test URL: " + name + " in " + this);

        TestResult found = null;

        // I don't think this is a valid check
        // in this context, this would end up checking for a dir which is
        // relative to the cwd
        //if (file.isDirectory())
        //   throw new JavaTestError(i18n, "trttn.noPaths");

        if (childs == null || childs.length == 0) return null;

        for (int i = 0; i < childs.length; i++) {
            if (childs[i] instanceof TestResult) {
                TestResult tr = (TestResult)(childs[i]);

                try {
                    String name = tr.getDescription().getRootRelativeURL();
                }
                catch (TestResult.Fault f) {
                    throw new JavaTestError(i18n, "trttn.noTd", f);
                }

                if (debug > 1)
                    Debug.println("   -> trying to match against " + name);

                if ( name.equals(url) ) {
                    found = (TestResult)childs[i];
                    i = childs.length;    // exit loop
                }
                else
                    found = null;
            }
        }

        return found;
    }

    // ------ end of interface impl ------
    // ------ private methods begin ------

    /**
     * Needed in cases where we need to get the return value from insert().
     * Only used during node insertion right now.  It is not really legal
     * to create one of these without immediately populating it with
     * a name.
     */
    TRT_TreeNode(TestResultTable table, TestResultTable.TreeNode parent) {
        childs = null;
        counter = 0;
        name = null;        // the only node with this value null is the root
        this.table = table;
        this.parent  = (TRT_TreeNode)parent;

    }

    /**
     * Get the current size of this subtree without causing a subtree scan.
     *
     * @see #getSize()
     */
    int getCurrentSize() {
        return counter;
    }

    /**
     * Increment the child status counter for the given Status type.
     * The parameter should correspond to one of the states defined in
     * the Status object.
     *
     * @deprecated Use bubbleUpChildStat()
     * @see Status
     */
    void incChildStat(int which) {
        childStats[which]++;
    }

    /**
     * Increment the child status counter for the given Status type.
     * The parameter should correspond to one of the states defined in
     * the Status object.
     *
     * @deprecated Use bubbleUpChildStat()
     * @see Status
     */
    void decChildStat(int which) {
        childStats[which]--;
    }

    void invalidateChildStats() {
        childStats = null;
        notifyCounterChange();
        TRT_TreeNode parent = (TRT_TreeNode)(getParent());

        if (parent != null)
            parent.invalidateChildStats();
    }

    boolean isChildStatsValid() {
        return (childStats == null);
    }

    /**
     * Increment the child status counter for the given Status type.
     * The parameter should correspond to one of the states defined in
     * the Status object.
     *
     * @deprecated
     * @see Status
     */
    static void bubbleUpChildStat(TRT_TreeNode node, int which) {
        node.childStats[which]++;
        TRT_TreeNode parent = (TRT_TreeNode)(node.getParent());

        if (parent != null) bubbleUpChildStat(parent, which);
    }

    /**
     * Decrement then increment the child status counter for the given Status type.
     * The parameters should correspond to one of the states defined in
     * the Status object. This method facilitates updating of TestResults - changing
     * from one status to another.  This change in similar to bubbleUpChildStat()
     * in that it automatically affects node from the current location up to the
     * root.
     *
     * @see Status
     */
    static void swapChildStat(TRT_TreeNode node, int oldStatus, int newStatus) {
        node.childStats[oldStatus]--;
        node.childStats[newStatus]++;

        TRT_TreeNode parent = (TRT_TreeNode)(node.getParent());

        if (parent != null) swapChildStat(parent, oldStatus, newStatus);
    }

    /**
     * Increment the counter that tracks how many nodes are under this node.
     *
     * @deprecated Use bubbleUpCounterInc() instead
     * @see #bubbleUpCounterInc()
     */
    void incNodeCounter() {
        counter++;
    }

    /**
     * Finds a TestResult in this node with the given name (test URL).
     * This is a match against the filename, not the test name.  This is not recursive.
    TestResult matchTRFile(String name) {
        scanIfNeeded();

        if (debug > 1) Debug.println("Matching TR: " + name + " in " + this);

        File file = new File(name);
        TestResult found = null;

        // I don't think this is a valid check
        // in this context, this would end up checking for a dir which is
        // relative to the cwd
        //if (file.isDirectory())
        //   throw new JavaTestError(i18n, "trttn.noPaths");

        if (childs == null || childs.length == 0) return null;

        for (int i = 0; i < childs.length; i++) {
            if (childs[i] instanceof TestResult) {
                File trName = new File(
                    ((TestResult)(childs[i])).getWorkRelativePath());

                if (debug > 1)
                    Debug.println("   -> trying to match against " + trName.getName());

                if ( name.equals(trName.getName()) ) {
                    found = (TestResult)childs[i];
                    i = childs.length;    // exit loop
                }
                else
                    found = null;
            }
        }

        return found;
    }
     */

    /**
     * Finds a TRT_TreeNode in this node with the given name.
     *
     * @param url The test URL, including the test id.  Basically output from
     *            TestDescription.getRootRelativeURL().
     * @return The requested TRT_TreeNode, null if not found.
     */
    TestResult getTestResult(String url) {
        int index = getTestIndex(url);
        if (index != -1)
            try {
                return (TestResult)(getChild(index));
            }
            catch(ClassCastException e) {
                // this better not happen
                // if it does, then getTestIndex() is broken
                throw new JavaTestError(i18n, "trttn.badCast");
            }
        else
            return null;
    }

    /**
     * Finds a TRT_TreeNode in this node with the given name.
     *
     * @param name Node name you want.  For example: "java_lang", but not
     *             "api/java_lang"
     * @return The requested TRT_TreeNode, null if not found.
     */
    TRT_TreeNode getTreeNode(String name, boolean suppressScan) {
        if (!suppressScan)
            scanIfNeeded();

        int index = getNodeIndex(name, suppressScan);
        if (index != -1)
            try {
                return (TRT_TreeNode)(getChild(index, suppressScan));
            }
            catch(ClassCastException e) {
                // this better not happen
                // if it does, then getNodeIndex() is broken
                throw new JavaTestError(i18n, "trttn.badCast");
            }
        else
            return null;
    }

    /**
     * Search for the given test result by the test URL.
     * The test URL will be converted to a work relative JTR path and then found
     * using getResultIndex().
     *
     * @param url The test URL, including the test id.  Basically output from
     *            TestDescription.getRootRelativeURL().
     * @return The index of the request test result.  -1 if not found.
     */
    int getTestIndex(String url) {
        if (url == null)
            throw new JavaTestError(i18n, "trttn.nullSearch");

        // we match on the JTR path, which should be equivalent to the test URL
        int location = getResultIndex(TestResult.getWorkRelativePath(url),
                                      false);
        return location;
    }

    /**
     * Search for the given test result by the work relative path.
     * This is a direct comparison of the TestResult.getWorkRelativePath() strings.
     *
     * @return The index of the request test result.  -1 if not found.
     */
    synchronized int getResultIndex(String jtrPath, boolean suppressScan) {
        if (jtrPath == null)
            throw new JavaTestError(i18n, "trttn.nullSearch");

        if (!suppressScan)
            scanIfNeeded();

        int found = -1;

        if (childs != null && childs.length != 0) {
            for (int i = 0; i < childs.length; i++) {
                if (childs[i] instanceof TestResult) {
                    TestResult tr = (TestResult)childs[i];

                    if (tr.getWorkRelativePath().equals(jtrPath)) {
                        found = i;
                        break;
                    }
                    else {
                        // not a previous instance of this TR, skip it
                        // placeholder to avoid dangling else
                    }
                }
                else {
                    // this is a TreeNode, skip it
                    // placeholder to avoid dangling else
                }
            }   // for
        }   // outer if

        return found;
    }

    /**
     * Finds a TestResult in this node with the same <b>Test URL</b> as the given TR.
     * This is not a reference comparison, nor is this a recursive search.
     *
     * @return The location of the requested TestResult.
     */
    int getTestIndex(TestResult target, boolean suppressScan) {
        if (target == null)
            throw new JavaTestError(i18n, "trttn.nullSearch");

        // we match on the JTR path, which should be equivalent to the test URL
        int location = getResultIndex(target.getWorkRelativePath(),
                                      suppressScan);
        return location;
    }

    /**
     * Finds a TRT_TreeNode in this node with the given name.
     *
     * @param name Node name you want.  For example: "java_lang", but not
     *             "api/java_lang"
     * @return The index of the requested TRT_TreeNode.
     */
    synchronized int getNodeIndex(String name, boolean suppressScan) {
        if (!suppressScan)
            scanIfNeeded();

        int found = -1;

        if (name == null)
            throw new JavaTestError(i18n, "trttn.nullSearch");

        if (childs == null || childs.length == 0)
            found = -1;
        else {
            for (int i = 0; i < childs.length; i++) {
                if (childs[i] instanceof TRT_TreeNode) {
                    TRT_TreeNode tn = (TRT_TreeNode)childs[i];
                    if (tn.getName().equals(name)) {
                        found = i;
                        break;
                    }
                    else {
                        // not a match
                        // placeholder to avoid dangling else
                    }
                }
                else {
                    // this is a TestResult, skip it
                    // placeholder to avoid dangling else
                }
            }   // for
        }   // outer else

        return found;
    }

// ---- BEGIN lazy tree with finder ----
    /**
     * In the case where a test finder is being used, nodes are read lazily.
     */
    synchronized void scanIfNeeded() {
        if (debug > 0) {
            Debug.println("starting scanIfNeeded() on node " + getName());
        }

        // this check is sufficient for now, but later we will want to scan
        // files for changes (maybe by timestamp)
        // if the finder is null, then we are operating in traditional mode
        if (table.getTestFinder() == null || isUpToDate()) {
            return;
        }

        if (childs == null)
            childs = new Object[0];
        /*
        File thisDir = new File(table.getTestFinder().getRootDir().getAbsolutePath() + File.separator +
                                TestResultTable.getRootRelativePath(this));
        // I think we rely on File to correct mixed file seps.
        // it might be a better idea to do a replace operation on the last item
        // since we know it will contains forward slashes
        File thisDir = new File(table.getTestSuiteRoot().getAbsolutePath() + File.separator +
                                TestResultTable.getRootRelativePath(this));
        */


        // special case for root
        if (isRoot() && filesToScan == null /* && root timestamp change check */ ) {
            File thisDir = table.getTestSuiteRoot();
            lastScanDate = table.getLastModifiedTime(thisDir);
            processFile(thisDir);

            // to prevent infinite recursion
            if (filesToScan == null) filesToScan = new String[0];

            for (int i = 0; i < filesToScan.length; i++) {
                processFile(new File(filesToScan[i]));
            }
        }

        File thisDir = new File(TestResultTable.getRootRelativePath(this));
        long lmd = table.getLastModifiedTime(thisDir);
        if (lmd <= lastScanDate)
            return;


        if (filesToScan != null) {
            // should be seeded to skip the root, which handled above?
            for (int i = 0; i < filesToScan.length; i++) {

                if (filesToScan[i] == this.name) {
                    processFile(thisDir);
                }
                else {
                    processFile(new File(TestResultTable.getRootRelativePath(this) +
                                File.separator + filesToScan[i]));
                }
            }   // for
        }
        else {
            if (debug > 0)
                Debug.println("refreshing contents of " + getName() + lastScanDate);
            refreshIfNeeded();
        }

        lastScanDate = lmd;

        // send observer msg?
        // delete unneeded objects from TestResultTable.TreeNode?
    }

    // SPECIAL TEST REFRESH/REPLACE METHODS
    synchronized TestResult resetTest(int index, TestResult tr) {
        if (!(childs[index] == tr))
            return null;

        // remove the JTR file
        File result = tr.getFile();

        // do this in in case the TR doesn't know that it has a JTR on disk
        WorkDirectory workdir = (table == null ? null : table.getWorkDirectory());
        if (result == null && workdir != null)
            result = workdir.getFile(tr.getWorkRelativePath());

        if (result != null) {
            boolean wasDeleted = result.delete();

            if (workdir != null)
                workdir.clearAnnotations(tr);
        }

        String name = tr.getTestName();
        String filename = null;
        int lastSlash = name.lastIndexOf('/');
        int lastHash = name.lastIndexOf('#');

        if (lastHash > lastSlash)
            filename = name.substring(0, lastHash);
        else
            filename = name;

        TestResult newTr = null;
        TestDescription oldTd = null;

        try {
            oldTd = tr.getDescription();
        }
        catch (TestResult.Fault f) {
            // oh well, recover without it
            // oldTd will be null, and we'll generate a new one
        }

        TestDescription newTd = updateTestDescription(name, oldTd);

        // this is bad actually, it means the test may not exist
        // anymore
        if (newTd == null)      // create an "emergency" one
            newTd = new TestDescription(table.getTestFinder().getRoot(),
                            new File(filename), new HashMap());

        newTr = TestResult.notRun(newTd);

        if (newTr != null) {        // success
            return replaceTest(newTr, index);
        }

        // operation failed if we reach this point
        return null;
    }

    /**
     * Refresh this entire node if necessary.
     * This means create any new nodes, and update any test descriptions.
     * @return True if some changes in this node were needed, false otherwise.
     * @see com.sun.javatest.TestResultTable#refreshIfNeeded(String)
     */
    public synchronized boolean refreshIfNeeded() {
        if (filesToScan != null ||
            (table != null && table.isFinderScanSuppressed() && lastScanDate == 0)) {
            //File thisDir = new File(getTestSuiteRootPathPrefix(),
        //              TestResultTable.getRootRelativePath(this));
            File thisDir = new File(TestResultTable.getRootRelativePath(this));
            TestFinder finder = table.getTestFinder();

            File[] files = null;
            long thisScanDate = table.getLastModifiedTime(thisDir);

            // may be less than if the custom finder starts to return a
            // bogus value - like zero or 1 for whatever reason
            if (thisScanDate <= lastScanDate)
                return false;

            synchronized (finder) {
                finder.read(thisDir);
                // we should not get back any tests
                File[] tmp = finder.getFiles();

                // shallow copy before releasing finder
                files = new File[tmp.length];
                System.arraycopy(tmp, 0, files, 0, tmp.length);
            }   // sync


            long cachedScanDate = lastScanDate;
            // need to update lastScanDate before the loop to avoid
            // excessive recursion
            lastScanDate = thisScanDate;
            ArrayList<TreeNode> nodesUsed = new ArrayList<TreeNode>();
            ArrayList<TestResult> usedTests = new ArrayList<TestResult>();

            for (int i = 0; i < files.length; i++) {
                if (table.isBranchFile(files[i])) {
                    nodesUsed.add(updateDirectory(files[i]));
                }
                else {
                    // scan if file is newer than the last time this folder
                    // was scanned (cachedScanDate)
                    if (table.getLastModifiedTime(files[i]) > cachedScanDate)
                        usedTests.addAll(updateFile(files[i]));
                    else {
                    }
                }
            }   // for

            // look for deleted nodes
            TreeNode[] origNodes = getTreeNodes();
            if (origNodes != null && origNodes.length != 0) {
               for (Iterator<TreeNode> i = nodesUsed.iterator(); i.hasNext(); ) {
                   TreeNode n = i.next();
                   for (int j = 0; j < origNodes.length; j++)
                       if (origNodes[j] == n)
                           origNodes[j] = null;
               }        // for

               for (int i = 0; i < origNodes.length; i++)
                    if (origNodes[i] != null) {
                        rmChild((TRT_TreeNode) origNodes[i]);
                    }
            }
            // discard
            origNodes = null;
            nodesUsed = null;

            // look for tests which no longer exist (from finder)
            TestResult[] currTests = getTestResults();
            if (currTests != null && currTests.length != 0) {
               for (Iterator<TestResult> i = usedTests.iterator(); i.hasNext(); ) {
                   TestResult t = i.next();
                   for (int j = 0; j < currTests.length; j++)
                       if (currTests[j] == t)
                           currTests[j] = null;
               }        // for

               for (int i = 0; i < currTests.length; i++)
                    if (currTests[i] != null) {
                        //System.out.println("** Removing test " + currTests[i].getTestName());
                        if (rmChild(currTests[i]) != -1) {
                            table.notifyRemoveLeaf(table.getObjectPath(this), currTests[i], i);
                        }
                    }
            }
            currTests = null;
            usedTests = null;
        }
        else {
            if (debug > 0)
                Debug.println("nothing to refresh in " + getName());
        }

        return true;
    }

    /**
     * @return the new test result object.  may be the same if no
     *         refresh was needed.  A null indicates a error.
     */
    synchronized TestResult refreshIfNeeded(TestResult test) {
        TestResult result = null;

        try {
            TestDescription oldTd = test.getDescription();
            TestDescription newTd = updateTestDescription(test.getTestName(),
                                        oldTd);
            if (oldTd == newTd)
                result = test;      // no update needed
            else {
                TestResult newTr = TestResult.notRun(newTd);

                if (newTr != null) {        // success
                    //replaceTest(newTr, getTestIndex(test));
                    addChild(newTr, false);
                    result = newTr;
                }
            }
        }
        catch (TestResult.Fault f) {
            if (debug > 0)
                f.printStackTrace(Debug.getWriter());

            result = test;
        }   // catch

        return result;
    }

    /**
     * Determine if the given file is a new directory and update necessary
     * structures if it is new.
     * @return Returns the corresponding node that was updated.
     */
    synchronized TreeNode updateDirectory(File f) {
        // update files to scan
        // if missing, add new node
        TreeNode[] nodes = getTreeNodes();
        String dirName = makeNodeRelative(f);
        boolean found = false;
        TreeNode theNode = null;

        if (nodes != null) {
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i].getName().equals(dirName)) {
                    found = true;
                    theNode = nodes[i];
                    break;
                }
                else { } //continue
            }   // for
        }
        else    // node nodes, must be a new directory
            found = false;


        if (!found) {       // need to add node
            try {
                TRT_TreeNode newNode = createDirNode(this, dirName);
                int thisIndex = getIndex(newNode);
                notifyInsBranch(newNode, thisIndex);

                // need to generate modified path for observer msg
                TreeNode[] fullPath = table.getObjectPath(newNode);
                TRT_TreeNode[] modPath = new TRT_TreeNode[fullPath.length-1];
                System.arraycopy(fullPath, 0, modPath, 0, fullPath.length-1);
                table.notifyNewBranch(modPath, newNode, thisIndex);

                newNode.scanIfNeeded();
                theNode = newNode;
            }
            catch (TRT_TreeNode.Fault e) {
                if (debug > 0)
                    e.printStackTrace(Debug.getWriter());
            }   // catch
            // send observer msg?
        }

        return theNode;
    }

    synchronized ArrayList<TestResult> updateFile(File fileToScan) {
        // check file date
        // scan the file
        // add new ones, compare existing ones
        ArrayList<TestResult> result = new ArrayList<TestResult>();

        if (debug > 1)
            System.out.println("Updating file " + fileToScan.getPath());

        TestFinder finder = table.getTestFinder();
        TestDescription[] tds = null;
        // what if test ids removed?
        synchronized (finder) {
            finder.read(fileToScan);
            TestDescription[] tds_tmp = finder.getTests();

            // shallow copy for safety
            tds = new TestDescription[tds_tmp.length];
            System.arraycopy(tds_tmp, 0, tds, 0, tds_tmp.length);
        }       // sync

        for (int i = 0; i < tds.length; i++) {
            TestResult tr = getTestResult(tds[i].getRootRelativeURL());
            TestDescription oldTd = null;
            try {
                // drop thru if tr is null
                if (tr != null)
                    oldTd = tr.getDescription();
            }
            catch (TestResult.Fault f) {
                // oh well, recover without it
                // oldTd will be null, and we'll generate a new one
            }   // catch

            if (tr == null || oldTd == null || !tds[i].equals(oldTd)) {
                TestResult newTr = TestResult.notRun(tds[i]);

                if (tr == null && oldTd == null) {
                    TestResult tmpTr = table.getCachedResult(tds[i]);
                    if (tmpTr != null) {
                        newTr = tmpTr;
                    }
                }

                table.update(newTr, false);
                if (debug > 1)
                    System.out.println("New test added");
                TreeNode[] pathToHere = table.getObjectPath(this);
                int index = getTestIndex(newTr, false);

                if (tr == null)
                    table.notifyNewLeaf(pathToHere, newTr, index);
                else {
                    table.notifyRemoveLeaf(pathToHere, tr, index);
                    table.notifyNewLeaf(pathToHere, newTr, index);
                }

                result.add(newTr);
            }
            else {
                result.add(tr);
            }       // replacement not necessary
        }   // for

        return result;
    }

    /**
     * Updates the TD if needed.
     * @param url This is required if the test description is null.  may be null
     *        if the <tt>oldTd</tt> is not null.  This parameter is
     *        preferred over data derived from the second parameter.
     * @param oldTd Used for comparison purposes, may be null.
     * @return A new TD if it has changed, the original if not.  Null will only be
     *         returned as a last resort and indicates that the source TD cannot
     *         be found.
     * @throws IllegalArgumentException If both arguments are null.
     */
    synchronized TestDescription updateTestDescription(String url,
                                    TestDescription oldTd) {
        String name = url;

        if (name == null && oldTd == null)
            throw new NullPointerException("both arguments cannot be null");

        if (name == null)
            name = oldTd.getRootRelativeURL();

        String filename = null;
        int lastSlash = name.lastIndexOf('/');
        int lastHash = name.lastIndexOf('#');
        TestDescription possibleNew = null;

        if (lastHash > lastSlash)
            filename = name.substring(0, lastHash);
        else
            filename = name;

        File fileToScan = new File(filename);

        // do this if the file seems to need rescanning, or we
        // don't seem to have an "old" TD
        if (table.getLastModifiedTime(fileToScan) > lastScanDate ||
            oldTd == null) {
            // run the finder on the correct file
            // find the matching TD
            TestFinder finder = table.getTestFinder();
            synchronized (finder) {
                finder.read(fileToScan);
                TestDescription[] tds = finder.getTests();
                for (int i = 0; i < tds.length; i++)
                    if (tds[i].getRootRelativeURL().equals(name)) {
                        possibleNew = tds[i];   // found
                        break;
                    }
            }   // sync
        }
        else {
        }
        // postcondition - oldTd may still be null

        if (possibleNew == null) {
            return oldTd;
        }
        else if (oldTd == null || !oldTd.equals(possibleNew)) {
            // only return the new one if it doesn't match
            return possibleNew;
        }
        else {
            return oldTd;
        }
    }

    private TestResult replaceTest(TestResult newTr, int index) {
        TestResult oldTr = (TestResult)childs[index];

        childs[index] = newTr;
        notifyReplacedResult(oldTr, newTr, index);
        newTr.setParent(this);
        oldTr.setParent(null);
        //invalidateChildStats();
        return newTr;
    }

    // -- END REFRESH METHODS --

    /**
     * Run the finder over the file and file away the resulting tests and files.
     * Depending on what Finder.getFiles() returns, additional files will be
     * scanned.  The final result:
     * <ul>
     *    <li>Tests in this node are created.
     *    <li>Subdirectories of this node are created, but not scanned
     * </ul>
     * The return value is meant to aid the caller in determining which tests
     * are still found by the finder.  See the calling code to see how they use
     * this info.
     * @return The test results which were recognized by this scan.
     */
    private ArrayList<TestResult> processFile(File file) {
        if (debug> 0) {
            Debug.println("--- Entering processFile() ---");
            Debug.println("This node's name: " + TestResultTable.getRootRelativePath(this));
            Debug.println("Processing file : " + file.getPath());
        }
        ArrayList<TestResult> result = new ArrayList<TestResult>();

        TestDescription[] tds = null;
        File[] files = null;
        TestFinder tf = table.getTestFinder();

        // finder object has state, so we need to control access to it
        synchronized (tf) {
            tf.read(file);
            TestDescription[] tds_tmp = tf.getTests();
            File[] files_tmp = tf.getFiles();

            // shallow copy everything just in case...
            // copy tests array
            if (tds_tmp != null && tds_tmp.length != 0) {
                tds = new TestDescription[tds_tmp.length];
                System.arraycopy(tds_tmp, 0, tds, 0, tds_tmp.length);
            }
            else
                tds = new TestDescription[0];

            // copy files array
            if (files_tmp != null && files_tmp.length != 0) {
                files = new File[files_tmp.length];
                System.arraycopy(files_tmp, 0, files, 0, files_tmp.length);
            }
            else
                files = new File[0];

            if (debug > 0)
                Debug.println("Read " + tds.length + " tests, and " +
                              files.length + " files.");
        }   // sync

        // process the tests
        for (TestDescription td : tds) {
            TestResult tr = table.getCachedResult(td);
            if (tr == null) {
                tr = TestResult.notRun(td);
            }

            result.add(addChild(tr, true));
        }   // for

        if (debug > 0)
            Debug.println("processFile() done scanning, now inserting");

        // process the files
        insertFinderFiles(this, files);

        return result;
    }

    /**
     * Takes the files the Finder returned and creates any needed nodes.
     */
    private synchronized void insertFinderFiles(TRT_TreeNode node, File[] files) {

        for (int i = 0; i < files.length; i++) {
            if (debug > 1) {
                Debug.print("   => into ");
                Debug.println(node.getName());
                Debug.print("   => original file is: ");
                Debug.println(files[i].getPath());
            }

            String url = makeNodeRelative(files[i]);

            if (debug > 1)
                Debug.println("   => stripped file is: " + url);

            // strip the trailing slash
            if (url.charAt(url.length()-1) == '/')
                url = url.substring(0, url.length()-1);

            // determine if url is node local
            if (url.indexOf('/') == -1) {
                // could be local file or directory name
                //     file.html
                //     directory
                if (getEnclosingTable().isBranchFile(files[i])) {
                    // file or dir?
                    if (debug > 1)
                        Debug.println("   => directory, creating node");

                    try {
                        TRT_TreeNode newNode = createDirNode(node, url);
                    }
                    catch (Fault f) {
                        throw new JavaTestError(i18n, "trttn.nameClash", f);
                    }
                }
                else {
                    // file, web walk?
                    if (debug > 1)
                        Debug.println("   => file, adding to scan list");

                    addToScanList(this, url, files[i]);
                }
            }
            else {
                // could be:
                //    directory/
                //    directory/file.html
                //    infinite subdirs with or w/o file.html at the end
                recursiveFinderInsert(this, files[i], url);
            }

            if (debug > 1)
                Debug.println("**");
        }
    }

    /**
     * Takes a URL and inserts it into the tree starting at the specified node.
     * The URL can end in either a directory or a file.  In the case of a directory,
     * an empty node is left behind.  In the case of a file, a node with filesToScan
     * will be created (appended).  No tests are added in this recursive process -
     * test scanning occurs after this recursive process finishes.
     *
     * @param fullFile The file that the finder returned.
     * @param url The node relative URL to be inserted.
     */
    private synchronized void recursiveFinderInsert(TRT_TreeNode node, File fullFile,
                                                           String url) {
        if (debug > 0)
            Debug.println("Recursive insert: " + url + " into " + node.getName());

        String newPath = TestResultTable.behead(url);

        if (url == newPath) {
            // last file or dir in the fullFile path
            // Ex:
            //   index.html
            //   aDirectory

            if (getEnclosingTable().isBranchFile(fullFile)) {
                // creating empty node right here
                if (debug > 0)
                    Debug.println("    -> Creating empty node and leaving.");

                TRT_TreeNode newNode = new TRT_TreeNode(table, node);
                newNode.setName(newPath);
                node.addChild(newNode, false);
            }
            else {
                // queue the file to be scanned when this node is scanned
                addToScanList(node, url, fullFile);
            }

            //TestResultTable.this.notifyNewLeaf(rec, tr, getIndex(tr));
        }
        else {
            // has at least 1 dir name left
            // find or create a TestResultTable.TreeNode and follow it
            String newName = TestResultTable.getDirName(url);

            try {
                TRT_TreeNode newNode = createDirNode(node, newName);
                recursiveFinderInsert(newNode, fullFile, newPath);
            }
            catch (Fault f) {
                throw new JavaTestError(i18n, "trttn.nameClash", f);
            }

        }
    }

    /**
     * Creates a node with the given parent and name if it does not already exist.
     * Note that this method fail to create the dir node silently if it already
     * is there.
     *
     * @param parent The parent of the node to be created.
     * @param name The name of the node to create.
     * @return The resulting node that was created (or found by lookup).
     * @throws Fault If a TestResult is present in the parent node with the requested
     *               name, then this exception is thrown.
     */
    private TRT_TreeNode createDirNode(TRT_TreeNode parent, String name) throws Fault {
        if (debug > 1)
            Debug.println("   => Trying to create dir node for: " + name);

        // check for collisions
        int possibleTR = parent.getResultIndex(name, true);

        if (debug > 1)
            Debug.println("   => TR index in parent: " + possibleTR);

        if (possibleTR != -1)
            throw new Fault(i18n, "trttn.alreadyExists", name);

        int location = parent.getNodeIndex(name, true);
        TRT_TreeNode theNode = null;

        if (debug > 1)
            Debug.println("   => index in parent: " + location);

        if (location == -1) {
            if (debug > 1) {
                Debug.println("   => Creating " + name);
                Debug.println("   => Local node is : " + parent);
            }

            theNode = new TRT_TreeNode(table, parent);
            theNode.setName(name);
            if (debug > 1)
                Debug.println("   => New child node is : " + theNode);

            // instruct the node to scan this file later
            addToScanList(theNode, theNode.name, new File(name));

            parent.addChild(theNode, true);
        }
        else {
            if (debug > 1) {
                Debug.println("   => Node exists, delegating.");
                Debug.println("   => index " + location + " in node " + parent.getName());
            }

            try {
                theNode = (TRT_TreeNode)(parent.getChild(location, true));
                if (debug > 1)
                    Debug.println("   => " + theNode);
            }
            catch (ClassCastException e) {
                throw new JavaTestError(i18n, "trttn.unexpecCast", e);
            }
        }

        return theNode;
    }

    /**
     * Forces the reading of all nodes underneath this node.
     * Use with caution, this could kill performance if invoked on a node
     * located towards the top of a large test suite.
     */
     void scanSubtree(TRT_TreeNode node) {
        if (table.getTestFinder() == null) return;

        //if (debug > 1)
        //    Debug.println("scanning subtree starting at " + node.getName());

        node.scanIfNeeded();

        TRT_TreeNode [] children = (TRT_TreeNode[])(node.getTreeNodes());

        if (children != null && children.length != 0) {
            for (int i = 0; i < children.length; i++)
                scanSubtree(children[i]);
        }
    }

    /**
     * Remove any path info preceding the testsuite root relative path.
     *
     * @return A node relative URL.  File separator guaranteed to be '/' now.
     */
    private String makeNodeRelative(File file) {
        if (debug > 1)
            Debug.println("relativizing: " + file.getPath());

        if (file.isAbsolute()) {
            // strip length of workdir and strip length of fullPath
            int distToDel = getTestSuitePathLen();

            if (debug > 1) {
                Debug.println("  -> URL is absolute (" + file.getPath().length() + " chars), stripping.");
                //Debug.println("  -> Stripping: " + table.getTestFinder().getRootDir());
                //Debug.println("  -> Stripping: " + table.getTestSuiteRoot().getPath());
                Debug.println("  -> Stripping " + distToDel + " characters.");
            }

            //int distToDel = (table.getWorkDir() == null ? 0 : table.getTestFinder().getRootDir().getAbsolutePath().length() + 1);
            //int distToDel = table.getTestSuiteRoot().getAbsolutePath().length() + 1;
            String rrp = TestResultTable.getRootRelativePath(this);

            if (debug > 1)
                Debug.println("  -> removing rrp: " + rrp);

            distToDel += ((rrp == null || rrp.length() == 0) ? 0 : rrp.length() + 1);

            String platformPath = file.getPath().substring(distToDel);

            if (debug > 1)
                Debug.println("  -> final node path: " + platformPath);

            return platformPath.replace(File.separatorChar, '/');
        }
        else {
            // path seems to be relative to the root of the testsuite
            String rrp = TestResultTable.getRootRelativePath(this);
            String thisFilePath = file.getPath().replace(File.separatorChar, '/');

            if (debug > 1)
                Debug.println("  -> check for RRP against: " + rrp);

            if (rrp != null && rrp.length() != 0 && thisFilePath.startsWith(rrp)) {
                // strip length of fullPath
                if (debug > 1)
                    Debug.println("  -> URL is root relative, stripping");

                int distToDel = rrp.length() + 1;
                if (thisFilePath.length() > distToDel) {
                    // rm leading path plus the last path sep.
                    return thisFilePath.substring(distToDel);
                } else {
                    // rrp == file
                    return "";
                }
            }
            else {
                // path is assumed to be relative to this node, this really
                // violates TestFinder rules
                if (debug > 1)
                    Debug.println("  -> relative, continue");

                return thisFilePath;
            }
        }
    }

    private static void addToScanList(TRT_TreeNode node, String file, File fullFile) {
        if (debug > 1) {
            Debug.println("   => Adding " + file + " to scan list and leaving.");
            Debug.println("   => Local node is : " + node);
            Debug.println("   -> local size b4: " +
                            (node.filesToScan == null ? 0 : node.filesToScan.length));
        }

        int i = 0;

        // check for a name clash
        if (node.filesToScan != null) {
            // the first entry in the list is (always?) a special case
            if (node.filesToScan.length > 0 && node.filesToScan[0].equals(node.getName()))
                i = 1;

            for (; i < node.filesToScan.length; i++)
                if (node.filesToScan[i].equals(file)) break;
        }

        // if no conflicts were found, append it
        if (node.filesToScan == null || i >= node.filesToScan.length)
            node.filesToScan = (String[])DynamicArray.append(node.filesToScan, file);
        else {
            // name collision, ignore
            // actually, with scan suppression, this may be normal
            if (debug > 1)
                Debug.println("Warning: File " +
                    fullFile.getPath() +
                    " may be referenced more than once in the test suite.  Ignoring.");
        }

        if (debug > 1) {
            Debug.println("   -> local size after: " +
                    (node.filesToScan == null ? 0 : node.filesToScan.length));
        }
    }

// ---- END lazy tree with finder ----
    synchronized TestResult addChild(TestResult tr, boolean suppressScan) {
        return addChild(tr, suppressScan, false);
    }

    /**
     * Put the given test result into this node, replacing an existing one if
     * needed.
     *
     * @param tr Test to add to this node.
     * @param suppressScan Suppress finder activity if possible
     * @param drop If true, drop the test being inserted if it does not already exist.
     * @return Null if an existing test was not replaced, the one that was
     *         originally there otherwise.  The return value may be the same
     *         as the supplied parameter.
     */
    synchronized TestResult addChild(TestResult tr, boolean suppressScan, boolean drop) {
        if (!suppressScan)
            scanIfNeeded();

        // does not really need i18n
        if (tr == null) throw new JavaTestError(i18n, "trttn.nullNode");

        if (debug > 1) {
            Debug.println("Adding test " + tr.getTestName());
            Debug.println("   -> " + tr.getStatus().toString());
            Debug.println("   -> local node ref: " + this);
            Debug.println("   -> local node name: " + this.getName());
            Debug.println("   -> local size: " +
                            (childs == null ? 0 : childs.length));
        }

        int oldIndex = getTestIndex(tr, suppressScan);
        TestResult oldTR = null;

        if (oldIndex == -1) {
            if (drop && table.getWorkDirectory() != null) {
                return null;
            }
            else {
                // previous result does not exist
                if (debug > 1)
                    Debug.println("   -> no old entry for " + tr);

                try {
                    // XXX should cache result of Class.forName
                    childs = DynamicArray.append(childs, tr, Class.forName("java.lang.Object"));
                    tr.setParent(this);
                    bubbleUpCounterInc();
                    notifyInsResult(tr, childs.length-1);
                }       // try
                catch (ClassNotFoundException e) {
                    throw new JavaTestError(i18n, "trttn.noObject", e);
                }       // catch
            }
        }
        else if (shouldReplaceTest(oldIndex, tr, suppressScan)) {
            // replace a previous result
            oldTR = (TestResult)childs[oldIndex];
            childs[oldIndex] = tr;
            if (debug > 0) {
                Debug.println("   -> ** replacing existing TR with " + tr);
                Debug.println("   -> " + tr.getTestName());
                Debug.println("   -> old status " + oldTR.getStatus().toString());
                Debug.println("   -> node: " + this);
            }

            oldTR.setParent(null);
            tr.setParent(this);

            notifyReplacedResult(oldTR, tr, oldIndex);
        }   // else if
        else {
            // not inserting
            // does this produce correct semantics?
            if (debug > 1) {
                Debug.println("   -> ** TRT selectively ignoring insert of " + tr);
                //Debug.println("   -> " + ((TestResult)(childs[oldIndex])).getTestName());
                // a cast exception here indicates a problem, the item at oldIndex
                // should indeed be a TestResult
                Debug.println("   -> old status: " + ((TestResult)(childs[oldIndex])).getStatus().toString());
                Debug.println("   -> curr. ref in TRT: " + childs[oldIndex]);
                Debug.println("   -> ignored new ref.: " + tr);
            }

            return tr;
        }

        invalidateChildStats();

        return oldTR;
    }

    /**
     * Inserts the TRT_TreeNode into the current node.
     * It is the callers responsibility to make sure that a node with the
     * same name does not already exist.
     */
    synchronized void addChild(TRT_TreeNode tn, boolean suppressScan) {
        if (tn == null) throw new JavaTestError(i18n, "trttn.nullNode");

        if (!suppressScan)
            scanIfNeeded();

        try {
            childs = DynamicArray.append(childs, tn,
                                         Class.forName("java.lang.Object"));
        }
        catch (ClassNotFoundException e) {
            throw new JavaTestError(i18n, "trttn.noObject", e);
        }
    }

    /**
     * Note that this does not remove the result from the work directory if it
     * exists there.
     */
    synchronized int rmChild(TRT_TreeNode tn) {
        if (childs == null)
            throw new IllegalStateException("Node is empty!");

        for (int i = 0; i < childs.length; i++) {
            if (childs[i] == tn) {
                Object[] newarr = DynamicArray.remove(childs, i);
                childs = (newarr == null ? new Object[0] : newarr);
                invalidateChildStats();
                notifyRemovedBranch(i);

                getEnclosingTable().notifyRemoveLeaf(
                        TestResultTable.getObjectPath(this), tn, i);
                return i;
            }
        }   // for

        return -1;      // not found!
    }

        /**
     * Note that this does not remove the result from the work directory if it
     * exists there.
     */
    synchronized int rmChild(TestResult tr) {
        if (childs == null)
            throw new IllegalStateException("Node is empty!");

        for (int i = 0; i < childs.length; i++) {
            if (childs[i] == tr) {
                Object[] newarr = DynamicArray.remove(childs, i);
                childs = (newarr == null ? new Object[0] : newarr);
                invalidateChildStats();
                notifyRemovedResult(tr, i);
                return i;
            }
        }   // for

        return -1;      // not found!
    }

    void setName(String name) {
        this.name = name;
    }

    /**
     * Determine whether or not a test object should replace an existing
     * one.  This is done by comparing the test URL, the status, then the
     * status string.  If they are all equivalent, then replacement is not
     * necessary.  The test URL should always match if you are calling this
     * method, but false will be returned if they do not.
     *
     * @param index The index in the current node to compare the possible
     *        replacement test.
     * @param newone The test to compare to the one at the given index.  Cannot
     *        be null.
     * @return True if the new test is the same test with a different status.
     *         False if the index is invalid, the <code>newone</code> is null,
     *         or the tests don't match in some way.
     */
    private boolean shouldReplaceTest(int index, TestResult newone,
                                      boolean suppressScan) {
        // check for out of range indexes, types and null
        if (!(childs[index] instanceof TestResult) || newone == null ||
            index < 0 || index >= childs.length)
            return false;

        TestResult orig = (TestResult)(childs[index]);

        // check that test names match
        // this actually should never fail
        if (!(orig.getTestName().equals(newone.getTestName())))
            return false;

        // special case - replace an outdated test when loading workdir
        //    note that this can be a very high cost operation - may
        //    cause loading of JTR file from disk.  Should be disabled
        //    by default.
        if (table != null && table.getWorkDirectory() != null &&
            table.getWorkDirectory().getTestSuite() != null &&
                table.getWorkDirectory().getTestSuite().getTestRefreshBehavior(TestSuite.CLEAR_CHANGED_TEST))
                try {
                    // if TD compare enabled
                    TestDescription tdnew = newone.getDescription();
                    TestDescription tdcurr = orig.getDescription();


                    // take the one with valid TD!
                    if (tdnew == null && tdcurr != null)
                        return false;
                    else if (tdnew != null && tdcurr == null)
                        return true;
                    else if (! (tdnew == null && tdcurr == null))
                        if (!tdnew.equals(tdcurr))
                            return false;       // based on cache status?
                }
                catch (TestResult.Fault f) {
                    // ignore, continue.  could log it
                }

        Status newstat = newone.getStatus();
        Status oldstat = orig.getStatus();

        if (newstat.getType() == Status.NOT_RUN &&
            oldstat.getType() != Status.NOT_RUN) {
            return false;
        }

        // check that status type is the same
        if (oldstat.getType() != newstat.getType())
            return true;

        // check that the status message string is the same
        // this is the best possible check we can do based on the info
        // that would be available from the TRC.  If we ask for more, we
        // risk reloading the JTR which is expensive.
        if (!(oldstat.getReason().equals(newstat.getReason())))
            return true;

        boolean oldShrunk = orig.isShrunk();
        boolean newShrunk = newone.isShrunk();
        // we are being given more data, take that
        if (oldShrunk && !newShrunk)
            return true;

        // both are populated, use the newer one
        if (!oldShrunk && !newShrunk)
            return true;

        // they are roughly equivalent
        return false;
    }

    /**
     * Recalculate the counters which track the status of tests below this node.
     * This can be a high cost calculation, but will do nothing if all the counters
     * are up to date.
     * It is assumed that if a node has null childStats, that all nodes between
     * it and the root are also marked invalid.
     */
    private static void refreshChildStats(TRT_TreeNode node) {
        if (node.childStats != null) return;        // nothing to do

        node.childStats = new int[Status.NUM_STATES];

        for (int i= 0; i < node.childs.length; i++) {
            if (node.childs[i] instanceof TRT_TreeNode) {
                // node is another branch
                TRT_TreeNode child = (TRT_TreeNode)(node.childs[i]);
                int[] stats = child.getChildStatus();

                for (int j = 0; j < stats.length; j++)
                    node.childStats[j] += stats[j];
            }
            else {
                // node is a test result
                TestResult tr = (TestResult)(node.childs[i]);
                node.childStats[tr.getStatus().getType()]++;
            }
        }
    }

    /**
     * Move up the tree to the root and increment the counter at each node.
     * The current node's counter IS incremented.
     */
    void bubbleUpCounterInc() {
        counter++;
        notifyCounterChange();
        TRT_TreeNode parent = (TRT_TreeNode)(getParent());

        if (parent != null) parent.bubbleUpCounterInc();
    }

    private int getTestSuitePathLen() {
        // testsuite location can either be:
        //    /tmp/foo/tests/testsuite.html
        //    /tmp/foo/tests
        // XXX find out how to do this without java.io.File
        if (table.getTestSuiteRoot().isFile())
            return table.getTestSuiteRoot().getParent().length() + 1;
        else
            return table.getTestSuiteRoot().getAbsolutePath().length() + 1;
    }

    /**
     * Return the prefix path for a testsuite root, to which a relative path can be attached.
     * This method exists to ensure compatibility when a webwalk-style root is being used, which ends
     * in an unusable testsuite.html.  So <tt>/tmp/JCK/tests/testsuite.html</tt> will be transformed into
     * <tt>/tmp/JCK/tests</tt>.  No trailing path separator will be added.
     */
    private File getTestSuiteRootPathPrefix() {
        File tsr = table.getTestSuiteRoot();

        // XXX find out how to do this without java.io.File
        if (tsr.isFile())
            return tsr.getParentFile();
        else
            return tsr;
    }

    // --- Observer notification util. code ---
    // these methods assume that the parent of the action is "this"

    private void notifyInsBranch(TRT_TreeNode newNode, int index) {
        TestResultTable.TreeNodeObserver[] observers = (TestResultTable.TreeNodeObserver[])(observerTable.get(this));

        if (observers != null)
            for (int i = 0; i < observers.length; i ++)
                observers[i].insertedBranch(this, newNode, index);
    }

    private void notifyInsResult(TestResult test, int index) {
        TestResultTable.TreeNodeObserver[] observers = (TestResultTable.TreeNodeObserver[])(observerTable.get(this));

        if (observers != null)
            for (int i = 0; i < observers.length; i ++)
                observers[i].insertedResult(this, test, index);
    }

    private void notifyReplacedResult(TestResult oldTest, TestResult newTest, int index) {
        TestResultTable.TreeNodeObserver[] observers = (TestResultTable.TreeNodeObserver[])(observerTable.get(this));

        if (observers != null)
            for (int i = 0; i < observers.length; i ++)
                observers[i].replacedResult(this, oldTest, newTest, index);
    }

    private void notifyRemovedBranch(int index) {
        TestResultTable.TreeNodeObserver[] observers = (TestResultTable.TreeNodeObserver[])(observerTable.get(this));

        if (observers != null)
            for (int i = 0; i < observers.length; i ++)
                observers[i].removedBranch(this, index);
    }

    private void notifyRemovedResult(TestResult test, int index) {
        TestResultTable.TreeNodeObserver[] observers = (TestResultTable.TreeNodeObserver[])(observerTable.get(this));

        if (observers != null)
            for (int i = 0; i < observers.length; i ++)
                observers[i].removedResult(this, test, index);
    }

    private void notifyCounterChange() {
        TestResultTable.TreeNodeObserver[] observers = (TestResultTable.TreeNodeObserver[])(observerTable.get(this));
        if (observers != null)
            for (int i = 0; i < observers.length; i ++) {
                observers[i].countersInvalidated(this);
            }
    }

    /**
     * null if the node has not been scanned, zero length if it is actually empty
     */
    private Object[] childs;            // contains combo of TreeNodes or TestResults
    private TRT_TreeNode parent;        // should never be null, unless root
    private TestResultTable table;      // what table this node is in

    private int counter;                // nodes below this point and including self
    private int[] childStats;

    private String name;                // basically the directory name, null means root node

    //private int maxDepth;            // currently unused
    //private int currDepth;           // currently unused

    private long lastScanDate;

    // no per-instance array of observers, use a static Hashtable of arrays
    private static Hashtable observerTable = new Hashtable(16);

    /**
     * List of files that makeup the on-disk contents of this node.
     * These are probably HTML files with test descriptions in them.  The string is a
     * URL which is relative to the current location, so the typical entry will be
     * "index.html".  The only special case is a reference to the "name" field of the
     * node which indicates that the current directory should be scanned by the
     * TestFinder.  This is to support directory walk style finders.
     */
    private String[] filesToScan;       // in cases where the finder must scan files

    //static protected boolean debug = Boolean.getBoolean("debug." + TRT_TreeNode.class.getName());
    static protected int debug = Debug.getInt(TRT_TreeNode.class);
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(TRT_TreeNode.class);

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

}   // TRT_TreeNode
