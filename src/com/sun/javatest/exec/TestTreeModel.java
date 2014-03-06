/*
 * $Id$
 *
 * Copyright (c) 2002, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License occured 2 only, as
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
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import com.sun.javatest.JavaTestError;
import com.sun.javatest.Parameters;
import com.sun.javatest.TRT_TreeNode;
import com.sun.javatest.TestFilter;
import com.sun.javatest.TestResult;
import com.sun.javatest.TestResultTable;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.util.Debug;
import com.sun.javatest.util.DynamicArray;
import com.sun.javatest.util.StringArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Logger;

/**
 * This is the data model bridge between JT Harness core and the GUI.
 */// NOTE: if you are going to request the worker lock and the htLock, you
//       MUST do it in that order.  Be mindful of holding the htLock and
//       calling a synchronized method, that is illegal and you must
//       release the htLock before making the method call.
class TestTreeModel implements TreeModel, TestResultTable.TreeObserver {

    TestTreeModel() {
    }

    TestTreeModel(Parameters p, FilterSelectionHandler filterHandler, UIFactory uif) {
        this.filterHandler = filterHandler;
        this.uif = uif;

        cache = new Hashtable();
        cacheQueue = new LinkedList();
        suspendedQueue = new LinkedList();

        cacheWorker = new CacheWorker();
        cacheWorker.setPriority(Thread.MIN_PRIORITY + 1);
        cacheWorker.start();

        setParameters(p);

        watcher = new FilterWatcher();
        filterHandler.addObserver(watcher);
        lastFilter = filterHandler.getActiveFilter();
    }

    synchronized void dispose() {
        disposed = true;

        if (trt != null) {
            trt.removeObserver(this);
            trt.dispose();
            trt = null;
        }

        filterHandler.removeObserver(watcher);

        if (cacheWorker != null) {
            cacheWorker.interrupt();
            cacheWorker = null;
        }

        params = null;
    }

    TestFilter getTestFilter() {
        return filterHandler.getActiveFilter();
    }

    Parameters getParameters() {
        return params;
    }

    // TreeModel methods
    public void addTreeModelListener(TreeModelListener l) {
        treeModelListeners = (TreeModelListener[]) DynamicArray.append(treeModelListeners, l);
    }

    public Object getChild(Object parent, int index) {
        if (parent instanceof TT_BasicNode) {
            return ((TT_BasicNode) parent).getChildAt(index);
        } else {
            return null;
        }
    }

    public int getChildCount(Object parent) {
        if (parent instanceof TreeNode) {
            return ((TreeNode) parent).getChildCount();
        } else {
            return -1;
        }
    }

    public int getIndexOfChild(Object parent, Object child) {
        if (parent instanceof TT_BasicNode) {
            return ((TT_BasicNode) parent).getIndex((TT_TreeNode) child);
        } else {
            return -1;
        }
    }

    public Object getRoot() {
        if (disposed) {
            if (debug > 0) {
                Debug.println("TTM - getRoot() ignored, model has been disposed.");
            }
            return null;
        }

        // trt is never supposed to be null
        if (root == null) {
            root = new TT_BasicNode(null, (TRT_TreeNode) (trt.getRoot()), trt.getTestFinder().getComparator());
        }
        return root;
    }

    public boolean isLeaf(Object node) {
        if (node == null) {
            return true;
        }

        if (node instanceof TT_BasicNode) {
            return false;
        } else if (node instanceof TT_TestNode) {
            return true;
        } else {
            throw new IllegalArgumentException(uif.getI18NString("tree.badType"));
        }
    }

    public void removeTreeModelListener(TreeModelListener l) {
        treeModelListeners = (TreeModelListener[]) DynamicArray.remove(treeModelListeners, l);
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        // XXX
        System.err.println(getClass().getName() + ": VFPC");
    }

    // TRT TreeObserver methods
    public void nodeInserted(final TestResultTable.TreeNode[] path,
            final Object what, final int index) {

        if (!EventQueue.isDispatchThread()) {
            Runnable t = new Runnable() {

                public void run() {
                    nodeInserted0(path, what, index);
                }
            };  // Runnable

            EventQueue.invokeLater(t);
        }
        else {
            nodeInserted0(path, what, index);
        }
    }


    private synchronized void nodeInserted0(final TestResultTable.TreeNode[] path,
            final Object what, final int index) {
        if (disposed)
            return;

        Object[] nodes = {what};

        TreeModelEvent tme;
        TT_BasicNode[] transPath = translatePath(path, true);

        if (transPath == null) {
            return; // error condition
        }

        // accomplish the primary task - insertion
        if (what instanceof TestResultTable.TreeNode) {
            /*
            if (!relevantNodes.contains(path[path.length - 1])) {
                return;
            }*/
            TestResultTable.TreeNode tn = (TestResultTable.TreeNode)what;
            int[] newPositions = transPath[transPath.length-1].addNodes(new TestResultTable.TreeNode[] {tn});

            if (debug > 0) {
                Debug.println("TTM - Node " + what + " inserting, path len=" + path.length);
            }
            if (path == null || path.length == 0) // root
            {
                tme = new TreeModelEvent(this, new TreePath(getRoot()));
            } else {
                tme = new TreeModelEvent(this, transPath, newPositions, nodes);
            }
            //if (cacheWorker != null && !cacheWorker.isPaused()) {
                notifyModelListeners(tme, Notifier.INS);
            //}
        } else {            // test result
            TestResult tr = (TestResult) what;

            // BK remove first part of if - should not happen
            if (transPath != null /* && relevantNodes.contains(transPath[transPath.length - 1])*/) {
                if (debug > 0) {
                    Debug.println("TTM - Node " + what + " inserted, path len=" + path.length);
                    Debug.println("   -> inserting " + tr.getTestName());
                    Debug.println("   -> mutable " + tr.isMutable());
                    Debug.println("   -> status " + tr.getStatus().getType());
                    Debug.println("   -> Thread: " + Thread.currentThread());
                }

//                if (cacheWorker != null && !cacheWorker.isPaused()) {
                    // BK optimize reuse of transPath var above
                    // notifyInserted(makeEvent(path, what, index));
                    processInsert(transPath, tr);
//                }
            }
            else {
            }

            // send updates to parent nodes for book keeping
            for (int i = transPath.length - 1; i >= 0; i--) {
                // walk path thru cache
                TT_NodeCache ni = null;

                synchronized (htLock) {
                    ni = (TT_NodeCache) (cache.get(transPath[i].getTableNode()));
                    //ni = getNodeInfo(transPath[i].getTableNode(), false);
                }   // sync

                // notify cache nodes
                // most of the time ni would probably be null
                if (ni != null) {
                    boolean result = false;
                    synchronized (ni.getNode()) {
                        result = ni.add(path, (TestResult) what, index);
                    }   // sync

                    if (result || relevantNodes.contains(transPath[i])) {
                        // if change detected, and relevant, then broadcast
                        // update of summary numbers is passive because it retrieves
                        // updates itself.
                        // we need to tell the tree when to update though
                        if (transPath[i].isRoot()) {
                            tme = new TreeModelEvent(this, new TreePath(transPath[i]));
                        } else {
                            tme = makeEvent(TestResultTable.getObjectPath(path[i - 1]), path[i], path[i - 1].getIndex(path[i]));
                        }
                        if (cacheWorker != null && !cacheWorker.isPaused()) {
                            notifyModelListeners(tme, Notifier.CHANGE);
                        }
                    }
                }
            }   // for
        }
    }

    public void nodeChanged(final TestResultTable.TreeNode[] path,
            final Object what, final int index, final Object old) {
        if (!EventQueue.isDispatchThread()) {
            Runnable t = new Runnable() {

                public void run() {
                    nodeChanged0(path, what, index, old);
                }
            };  // Runnable

            EventQueue.invokeLater(t);
        }
        else {
            nodeChanged0(path, what, index, old);
        }
    }

    private synchronized void nodeChanged0(TestResultTable.TreeNode[] path, Object what,
            int index, Object old) {
        if (disposed)
            return;

        if (what instanceof TestResultTable.TreeNode) {
            // check relevance
            if (relevantNodes.contains(path[path.length - 1])) {
                notifyModelListeners(makeEvent(path, what, index), Notifier.CHANGE);
                notifyModelListeners(new TreeModelEvent(this, path, null, null), Notifier.STRUCT);
            }
        } else {            // test result
            if (relevantTests.contains(old) && old != what) {
                relevantTests.remove(old);
                relevantTests.add((TestResult) what);
            }

            TT_BasicNode[] transPath = translatePath(path, false);
            if (transPath != null) {
                TreeModelEvent where = transPath[transPath.length-1].replaceTest((TestResult)what, false);
                if (where == null && debug > 0) {
                    Debug.println("No test insertion pt for " + ((TestResult)what).getTestName());
                }
            }
            else {
                // ignored.  should log it.
            }

            for (int i = path.length - 1; i >= 0; i--) {
                // walk path thru cache
                TT_NodeCache ni = null;

                synchronized (htLock) {
                    ni = cache.get(path[i]);
                }   // sync

                // notify cache nodes
                if (ni != null) {
                    boolean result = ni.replace(path, (TestResult) what, index, (TestResult) old);

                    /* dead code
                    if (result && relevantNodes.contains(transPath[i])) {
                    // if change detected, and relevant, then broadcast
                    //notifyChanged(makeEvent(path, what, index));
                    }*/
                }
            }   // for
        }
    }

    public void nodeRemoved(final TestResultTable.TreeNode[] path,
            final Object what, final int index) {

        if (!EventQueue.isDispatchThread()) {
            Runnable t = new Runnable() {

                public void run() {
                    nodeRemoved0(path, what, index);
                }
            };  // Runnable

            EventQueue.invokeLater(t);
        }
        else {
            nodeRemoved0(path, what, index);
        }
    }

    private synchronized void nodeRemoved0(TestResultTable.TreeNode[] path, Object what, int index) {
        if (disposed)
            return;
        if (what instanceof TestResultTable.TreeNode) {
            //if (true || relevantNodes.contains(path[path.length - 1])) {
            if (debug > 0) {
                Debug.println("TTM - Node " + what + " removed, path len=" + path.length);
            }

            TT_BasicNode[] transPath = translatePath(path, false);
            TestResultTable.TreeNode node = (TestResultTable.TreeNode)what;
            TT_TreeNode tn = transPath[transPath.length-1].findByName(node.getName());
            int where = transPath[transPath.length-1].removeNode(tn);

            if (where >= 0) {
                TreeModelEvent tme = new TreeModelEvent(this, transPath, new int[] {where},
                        new Object[] {tn});
                notifyModelListeners(tme, Notifier.DEL);
            }
        } else {            // test result
            TestResult tr = (TestResult) what;
            TT_BasicNode[] transPath = translatePath(path,false);

            relevantTests.remove(tr);

            //if (debug > 0 && relevantNodes.contains(transPath[transPath.length - 1])) {
            if (debug > 0) {
                //if (debug > 0) {
                Debug.println("TTM - Node " + what + " removed, path len=" + path.length);
                Debug.println("    -> Removing " + tr.getTestName());
                Debug.println("    -> Thread: " + Thread.currentThread());
            }

            processRemove(transPath, tr, index);

            for (int i = path.length - 1; i >= 0; i--) {
                // walk path thru cache
                TT_NodeCache ni = null;

                synchronized (htLock) {
                    ni = cache.get(path[i]);
                }   // sync

                // notify cache nodes
                if (ni != null) {
                    // need to lock node before locking the cache node
                    boolean result = false;
                    synchronized (ni.getNode()) {
                        result = ni.remove(path, (TestResult) what, index);
                    }   // sync
                }
            }   // for
        }
    }

    //  ----- private -----
    private void processRemove(final TT_BasicNode[] path, final TestResult tr,
            final int index) {
        if (!EventQueue.isDispatchThread()) {
            Runnable t = new Runnable() {

                public void run() {
                    processRemove0(path, tr, index);
                }
            };  // Runnable

            EventQueue.invokeLater(t);
        }
        else {
            processRemove0(path, tr, index);
        }
    }

    private void processRemove0(TT_BasicNode[] path, TestResult tr, int index) {
        //TT_TestNode tn = path[path.length - 1].findByName(tr);
        TreeModelEvent tme = path[path.length - 1].removeTest(tr);
        //if (tn == null || pos < 0) {
        if (tme == null) {
            return;
        }

        for (int i = 0; i < treeModelListeners.length; i++) {
            ((TreeModelListener) treeModelListeners[i]).treeNodesRemoved(tme);
        }
    }

    private void processInsert(final TT_BasicNode[] path, final TestResult tr) {
        if (!EventQueue.isDispatchThread()) {
            Runnable t = new Runnable() {

                public void run() {
                    processInsert0(path, tr);
                }
            };  // Runnable

            EventQueue.invokeLater(t);
        } else {
            processInsert0(path, tr);
        }
    }

    private void processInsert0(TT_BasicNode[] path, TestResult tr) {
        TT_TestNode newNode = new TT_TestNode(path[path.length - 1], tr);
        //int pos = path[path.length - 1].addTest(newNode, sortComparator);
        TreeModelEvent tme = path[path.length - 1].addTest(newNode, sortComparator);
        if (tme == null) return;

        for (int i = 0; i < treeModelListeners.length; i++) {
            ((TreeModelListener) treeModelListeners[i]).treeNodesInserted(tme);
        }

    }

    TreePath resolveUrl(String path) {
        if (path == null || path.length() == 0 || root == null) {
            return null;
        }
        ArrayList<TT_TreeNode> al = new ArrayList();
        al.add(root);
        TT_BasicNode spot = root;

        StringBuffer sb = new StringBuffer(path);

        while (sb.length() > 0) {
            int slash = sb.indexOf("/");
            String current = null;
            if (slash < 0) {
                current = sb.toString();
                sb.setLength(0);
            } else {
                current = sb.substring(0, slash);
                sb = sb.delete(0, slash+1);
            }

            TT_TreeNode node = spot.findByName(current);

            if (node == null) {
                return null;
            } else if (node instanceof TT_BasicNode) {
                al.add(node);
                spot = (TT_BasicNode) node;
            } else if (node instanceof TT_TestNode) {
                al.add(node);
                sb.setLength(0);
            }
        }

        return new TreePath(al.toArray());
    }

    void addRelevantNode(TT_TreeNode node) {
        relevantNodes.add(node);
    }

    void removeRelevantNode(TT_TreeNode node) {
        relevantNodes.remove(node);
    }

    void addRelevantTest(TestResult tr) {
        relevantTests.add(tr);
    }

    void removeRelevantTest(TestResult tr) {
        if (relevantTests != null && tr != null) {
            relevantTests.remove(tr);
        }
    }

    /**
     * Hint that the given node is of more importance than others.
     */
    void setActiveNode(TT_BasicNode node) {
        TT_NodeCache ni = null;

        synchronized (htLock) {
            ni = cache.get(node.getTableNode());
        }

        if (ni != null) {
            cacheWorker.requestActiveNode(ni);
        } else {
            // uh...why don't we have a node?
            // create and schedule?
        }
    }

    /**
     * Create an event from the raw TRT data, so it can be passed to the GUI tree.
     */
    private TreeModelEvent makeEvent(TestResultTable.TreeNode[] path, Object target, int index) {
        if (getRoot() == null) {
            return null;
        }
        int[] inds = {index};
        // per the TreeModelEvent javadoc, the path is supposed to lead to the parent of the
        // changed node

        TT_BasicNode[] transPath = translatePath(path, false);

        TT_TreeNode[] transTarget = new TT_TreeNode[1];
        if (target instanceof TestResult &&
                transPath[transPath.length - 1] instanceof TT_BasicNode) {
            TT_TestNode mtn = ((TT_BasicNode) (transPath[transPath.length - 1])).findByName((TestResult) target);
            if (mtn != null) {
                transTarget[0] = mtn;
            } else {
                return null;    // no matching test
            }
        } else {
            TT_TreeNode mtn = ((TT_BasicNode) (transPath[transPath.length - 1])).findByName(
                    ((TestResultTable.TreeNode) target).getName());
            if (mtn != null) {
                transTarget[0] = mtn;
            } else {
                return null;    // no matching test
            }
        }

        if (debug > 1) {
            Debug.println("TTM Broadcasing " + target + " change message...");
            //Debug.println("   -> Status = " + ((TestResult)target).getStatus().toString());
            Debug.println("   -> Path len=" + transPath.length);
            Debug.println("   -> Index = " + index);
            Debug.println("   -> target = " + Arrays.toString(transTarget));
        }

        return new TreeModelEvent(this, transPath, inds, transTarget);
    }

    TT_BasicNode[] translatePath(TestResultTable.TreeNode[] path, boolean create) {
        if (path == null)
            return null;

        TT_BasicNode location = (TT_BasicNode)getRoot();
        TT_BasicNode[] transPath = new TT_BasicNode[path.length];
        transPath[0] = location;

        for (int i = 1; i < path.length; i++) {
            TT_TreeNode mnode = location.findByName(path[i].getName());
            if (create && mnode == null) {
                location.addNodes(new TestResultTable.TreeNode[] {path[i]});
                mnode = location.findByName(path[i].getName());
            }
            else if (!(mnode instanceof TT_BasicNode)) {
                return null; // ignore event
            }
            transPath[i] = (TT_BasicNode) mnode;
            location = transPath[i];
        }

        return transPath;
    }

    String[] pathsToStrings(TreePath[] paths) {
        if (paths == null || paths.length == 0 ||
            !(getRoot() instanceof TT_BasicNode))
            return null;

        String[] result = new String[paths.length];
        for (int i = 0; i < paths.length; i++) {
            TT_TreeNode tn = (TT_TreeNode)(paths[i].getLastPathComponent());
            result[i] = tn.getLongPath();
        }

        return result;
    }

    TreePath[] urlsToPaths(String[] urls) {
        ArrayList<TreePath> result = new ArrayList();

        for (int i = 0; i < urls.length; i++) {
            TreePath thisOne = urlToPath(urls[i]);
            if (thisOne == null)
                continue;   // skipped for some reason
            else
                result.add(thisOne);
        }

        TreePath[] res = new TreePath[result.size()];
        result.toArray(res);
        return res;
    }

    TreePath urlToPath(String url) {
        if (url == null)
            return null;

        String[] urlPath = StringArray.splitList(url, "/");
        TT_TreeNode[] transPath = new TT_TreeNode[urlPath.length+1];
        TT_BasicNode location = (TT_BasicNode)getRoot();
        transPath[0] = location;

        for (int i = 0; i < urlPath.length; i++) {
            if (location == null)
                return null;    // special exit condition for the loop

            TT_TreeNode mnode = location.findByName(urlPath[i]);
            if (mnode == null) {
                return null;
            }

            transPath[i+1] = mnode;
            location = (transPath[i+1] instanceof TT_BasicNode ?
                        (TT_BasicNode)transPath[i+1] : null);
        }

        return new TreePath(transPath);
    }

    void setParameters(Parameters p) {
        if (p != null) {
            params = p;
            init();
        } else {
            TestResultTable dummy = new TestResultTable();
            setTestResultTable(dummy);

            if (debug > 0) {
                Debug.println("TTM - dummy TRT, root = " + dummy.getRoot());
            }
        }
    }

    synchronized private void init() {
        if (params == null) {
            return;
        }
        WorkDirectory wd = params.getWorkDirectory();
        TestSuite ts = params.getTestSuite();

        if (wd != null) {       // full config w/TS and WD
            if (debug > 0) {
                Debug.println("TTM - initializing with workdir");
            }
            log = getLog(wd);
            TestResultTable newTrt = wd.getTestResultTable();
            try {
                newTrt.getLock().lock();
                setTestResultTable(newTrt);
            } finally {
                newTrt.getLock().unlock();
            }

            sortComparator = newTrt.getTestFinder().getComparator();
        } else if (ts != null) {        // TS, but no workdir

            if (trt != null && trt.getTestFinder() == null) {
                // configure a finder on the temporary TRT, this allows us to
                // populate the table before we have a workdir
                try {
                    trt.getLock().lock();
                    trt.setTestFinder(ts.getTestFinder());
                    setTestResultTable(trt);
                } finally {
                    trt.getLock().unlock();
                    sortComparator = trt.getTestFinder().getComparator();

                }

                if (debug > 0) {
                    Debug.println("TTM - params set, no WD; setting finder on temp. TRT");                //notifyFullStructure();
                }
            } else {
                // already have a finder
                if (debug > 0) {
                    Debug.println("TTM - temp. TRT already has finder");
                }
            }


        } // no WD or TS
        else {
            if (debug > 0) {
                Debug.println("TTM - params set, no WD or TS");            // should we explicitly reset the model?
            }
        }
    }

    void pauseWork() {
        cacheWorker.setPaused(true);
        // should consider synchronizing with cacheWorker before checking
        // condition
        while (cacheWorker != null && cacheWorker.isAlive() && !cacheWorker.stopping && !cacheWorker.isReallyPaused) {
            try {
                synchronized (cacheWorker) {
                    cacheWorker.notifyAll();
                    cacheWorker.wait();
                }
            } catch (InterruptedException e) {
            }
        }
    }

    void unpauseWork() {
        if (cacheWorker == null) {
            return;
        }

        invalidateNodeInfo();
        cacheWorker.setPaused(false);
        synchronized (cacheWorker) {
            cacheWorker.notifyAll();
        }
    }

    boolean isWorkPaused() {
        return cacheWorker.isPaused();
    }

    /**
     * This method will attempt to swap the table if compatible to avoid a full JTree
     * swap.
     */
    private void setTestResultTable(TestResultTable newTrt) {
        // NOTE: may want to run this on when we figure out how important params are
        // see note in setParameters()
        //if (params == null)
        //    throw IllegalStateException();

        // assumptions:
        // - a model must have a non-null TRT
        if (isCompatible(trt, newTrt)) {
            swapTables(newTrt);
        } else {
            if (trt != null) {
                trt.removeObserver(this);
            }
            trt = newTrt;
            trt.addObserver(this);
        }

        root = new TT_BasicNode(null, (TRT_TreeNode) (trt.getRoot()),
                (trt.getTestFinder() == null ? null : trt.getTestFinder().getComparator()));

        // prime relevant nodes with root and first level
        relevantNodes = Collections.synchronizedSet(new HashSet());
        relevantTests = Collections.synchronizedSet(new HashSet());

        addRelevantNode((TT_TreeNode) getRoot());
        TT_BasicNode tn = ((TT_BasicNode) getRoot());
        for (int i = 0; i < ((TT_BasicNode) getRoot()).getChildCount(); i++) {
            addRelevantNode((TT_TreeNode) (tn.getChildAt(i)));
        }
        notifyFullStructure();

        if (debug > 0) {
            Debug.println("TTM - Model watching " + trt);
            if (trt.getWorkDir() != null) {
                Debug.println("   -> Workdir=" + trt.getWorkDir());
                Debug.println("   -> Workdir path=" + trt.getWorkDir().getPath());
                Debug.println("   -> root = " + trt.getRoot());
            }
        }
    }

    static boolean isCompatible(TestResultTable t1, TestResultTable t2) {
        // assumptions:
        // - a TRT must have a finder and testsuite root to be comparable
        // - the finder class objects must be equal for TRTs to be compatible
        // - testsuite root paths must be equal
        if (t1 == null || t2 == null ||
                t1.getTestSuiteRoot() == null || t2.getTestSuiteRoot() == null ||
                t1.getTestFinder() == null || t2.getTestFinder() == null) {
            if (debug > 1) {
                Debug.println("TTM - isCompatible() false because one or both TRTs are incomplete.");
                if (t1 != null && t2 != null) {
                    Debug.println("t1 root = " + t1.getTestSuiteRoot());
                    Debug.println("t2 root = " + t2.getTestSuiteRoot());
                    Debug.println("t1 finder= " + t1.getTestFinder());
                    Debug.println("t2 finder= " + t2.getTestFinder());
                }
            }

            return false;
        } else if (!t1.getTestSuiteRoot().getPath().equals(t2.getTestSuiteRoot().getPath())) {
            if (debug > 1) {
                Debug.println("TTM - isCompatible() failed because testsuite paths differ.");
            }
            return false;
        } else if (t1.getTestFinder() != t2.getTestFinder()) {
            if (debug > 1) {
                Debug.println("TTM - isCompatible() failed because TestFinders differ.");
            }
            return false;
        } else {
            return true;
        }
    }

    TestResultTable getTestResultTable() {
        return trt;
    }

    /**
     * Retrieve the info about the given node.  The returned info object may be
     * either a running thread or a finished thread, depending on whether recent
     * data is available.
     * @param node The node to get information for.
     * @param highPriority Should the task of retrieving this information be
     *        higher than normal.
     */
    TT_NodeCache getNodeInfo(TestResultTable.TreeNode node, boolean highPriority) {
        TT_NodeCache ni = null;
        boolean wakeWorker = false;

        // this line is outside the synchronized zone to prevent a
        // deadlock when a filter change occurs and the cache is
        // invalidated (on the event thread)
        TestFilter activeFilter = filterHandler.getActiveFilter();

        synchronized (htLock) {
            ni = cache.get(node);

            if (ni == null) {
                ni = new TT_NodeCache(node, activeFilter, log);
                cache.put(node, ni);

                if (!highPriority) // high pri. case below
                {
                    cacheQueue.addFirst(ni);
                }
                wakeWorker = true;      // because this is a new node
            } else if (highPriority) {
                // reorder for high priority
                // could cancel current task, but that would complicate things
                int index = cacheQueue.indexOf(ni);

                if (index >= 0) {
                    cacheQueue.remove(index);
                }
            }
        }   // synchronized

        // this is done outside the htLock block to ensure proper locking
        // order.
        if (highPriority) {
            cacheWorker.requestActiveNode(ni);
            wakeWorker = true;
        }

        // forward info for persistent storage and later use...
        if (!statsForwarded && node.isRoot() && ni.isComplete()) {
            int[] stats = ni.getStats();
            int total = 0;

            for (int i = 0; i < stats.length; i++) {
                total += stats[i];
            }
            total += ni.getRejectCount();

            if (params != null) {
                WorkDirectory wd = params.getWorkDirectory();

                if (wd != null) {
                    wd.setTestSuiteTestCount(total);
                    statsForwarded = true;
                }
            }
        }

        if (wakeWorker) {
            synchronized (cacheWorker) {
                cacheWorker.notify();
            }
        }
        return ni;
    }

    /**
     * Invalidate any collected data about any of the nodes on the given path.
     * This operation includes stopping any threads scanning for info and
     * removing the entries from the cache.
     *
     * @param path Nodes to invalidate in the cache, must not be null.
     *             May be length zero.
     */
    void invalidateNodeInfo(TestResultTable.TreeNode[] path) {
        // do this as a batch
        // we can reverse the sync and for if stalls are noticeable
        synchronized (htLock) {
            for (int i = 0; i < path.length; i++) {
                TT_NodeCache info = cache.get(path[i]);

                if (info != null) {
                    if (debug > 1) {
                        Debug.println("TTM - halting thread and removed from node cache");
                        Debug.println("   -> " + path[i]);
                        Debug.println("   -> " + info);
                    }

                    info.halt();
                    cache.remove(info.getNode());
                    boolean wasInQueue = cacheQueue.remove(info);

                    info = new TT_NodeCache(info.getNode(), filterHandler.getActiveFilter(), log);
                    cache.put(info.getNode(), info);
                    cacheQueue.addFirst(info);
                }
            }   // for
        }   // synchronized

        // wake up the worker
        synchronized (cacheWorker) {
            cacheWorker.notify();
        }
    }

    /**
     * Invalidate all collected node information.
     * This is likely used when the internal contents of the parameters have
     * changed.  If the cache is not invalidated at certain points, rendering
     * of the tree may be incorrect.
     */
    void invalidateNodeInfo() {
        synchronized (htLock) {
            Enumeration e = cache.keys();
            while (e.hasMoreElements()) {
                (cache.get(e.nextElement())).invalidate();
            }   // while

            cache = new Hashtable();
            cacheQueue = new LinkedList();
            suspendedQueue = new LinkedList();
        }

        // reprocess any needed nodes
        Iterator it = relevantNodes.iterator();
        while (it.hasNext()) {
            TT_TreeNode tn = (TT_TreeNode) it.next();
            if (tn instanceof TT_BasicNode) {
                getNodeInfo(((TT_BasicNode) tn).getTableNode(), false);
            }
        }       // while
    }

    /**
     * Invalidate any collected data about the given node.
     * This operation includes stopping any thread scanning for info and
     * removing the entry from the cache.
     *
     * @param node The node to invalidate in the cache.  Must not be null.
     * @deprecated The cache will be smart enough to not need this.
     */
     void invalidateNodeInfo(TestResultTable.TreeNode node) {
        invalidateNodeInfo(new TestResultTable.TreeNode[]{node});
    }

    /**
     * Trusting method which assumes that the given TRT is compatible with the
     * current one.  Strange things will happen if it is not!
     */
    void swapTables(TestResultTable newTrt) {
        if (newTrt == trt || newTrt == null) {
            return;
        }
        if (debug > 1) {
            Debug.println("Swapping TRTs under the covers.");
            Debug.println("   -> OLD=" + trt);
            Debug.println("   -> NEW=" + newTrt);
        }

        trt.removeObserver(this);
        trt = newTrt;
        trt.addObserver(this);
    }

    private void notifyModelListeners(TreeModelEvent e, int eType) {
        if (treeModelListeners != null) {
            Notifier n = new Notifier(eType, treeModelListeners, e, uif);
            if (!EventQueue.isDispatchThread()) {
                EventQueue.invokeLater(n);
            }
            else {
                n.run();
            }
        }
    }


    void notifyFullStructure() {
        if (debug > 0) {
            Debug.println("TTM - sending full structure change event to model listeners.");
        }
        invalidateNodeInfo();

        Object[] path = {getRoot()};
        TreeModelEvent e = new TreeModelEvent(this, path);
        notifyModelListeners(e, Notifier.STRUCT);
    }

    protected void finalize() throws Throwable {
        super.finalize();

        if (trt != null) {
            trt.removeObserver(this);
        }
    }

    private Logger getLog(WorkDirectory wd) {
        Logger log = null;
        final String logName = uif.getI18NString("tree.log.name");
        try {
            log = wd.getTestSuite().createLog(wd, null, logName);
        }
        catch (TestSuite.DuplicateLogNameFault f) {
            try {
                log = wd.getTestSuite().getLog(wd, logName);
            }
            catch (TestSuite.NoSuchLogFault f2) { }
        }
        return log;
    }

    private TT_BasicNode root;
    private UIFactory uif;
    private TestResultTable trt;
    private Parameters params;
    private FilterSelectionHandler filterHandler;
    private TestFilter lastFilter;
    private Comparator sortComparator;
    private TreeModelListener[] treeModelListeners = new TreeModelListener[0];
    private boolean statsForwarded;
    private boolean disposed;
    private CacheWorker cacheWorker;
    private FilterWatcher watcher;
    private Set<TT_TreeNode> relevantNodes;
    private Set<TestResult> relevantTests;  // not used anymore

    private Logger log;
    /**
     * Stores state info about individual nodes.
     * The key is a TestResultTable.TreeNode, and the value is a TT_NodeCache
     * object.  Use the htLock when accessing the cache.
     */
    protected Hashtable<TestResultTable.TreeNode, TT_NodeCache> cache;
    /**
     * Queue of items to be processed.
     * "First" is the most recently added, "last" is the next to be processed.
     */
    protected LinkedList<TT_NodeCache> cacheQueue;
    /**
     * Queue of items which are in the middle of being processed.
     * "First" is the most recently added, "last" is the next to be processed.
     */
    protected LinkedList<TT_NodeCache> suspendedQueue;
    protected final Object htLock = new Object();
    private static final int CACHE_THREAD_PRI = Thread.MIN_PRIORITY;
    private static final int CACHE_NOTI_THR_PRI = Thread.MIN_PRIORITY;
    protected static int debug = Debug.getInt(TestTreeModel.class);

    // ************** inner classes ***************
    private class CacheWorker extends Thread {

        CacheWorker() {
            super("Test Tree Cache Worker");
        }

        public void run() {
            try {
                synchronized (CacheWorker.this) {
                    wait();
                }
            } catch (InterruptedException e) {
                // we're terminated I guess
                return;
            }

            while (!stopping) {
                if (paused) {
                    try {
                        synchronized (CacheWorker.this) {
                            isReallyPaused = true;
                            notifyAll();
                            wait();
                            isReallyPaused = false;
                        }
                        continue;
                    } catch (InterruptedException e) {
                        // we're terminated I guess
                        stopping = true;
                        isReallyPaused = false;
                        continue;
                    }                // poll the queue for work to do
                }
                currentUnit = getNextUnit();

                // nothing to do, wait
                if (currentUnit == null) {
                    try {
                        synchronized (CacheWorker.this) {
                            notifyAll();
                            wait();
                        }
                        continue;
                    } // try
                    catch (InterruptedException e) {
                        // we're terminated I guess
                        stopping = true;
                        continue;
                    }   // catch
                } else {
                    if (!currentUnit.canRun()) {
                        //throw new IllegalStateException("cache malfunction - attempting to re-populate node  " + currentUnit.isPaused() + "   " + currentUnit.isValid() + "  " + currentUnit.isComplete());
                        continue;
                    }

                    // do the work
                    if (debug > 0) {
                        Debug.println("TTM cache processing " + currentUnit.getNode().getName());
                    }
                    currentUnit.run();

                    if (!currentUnit.isPaused() && currentUnit.isValid()) {
                        finishJob(currentUnit);
                    }
                }
            }   // while

            this.currentUnit = null;
            this.priorityUnit = null;

        }

        /**
         * Only call this to terminate all cache activity.
         * There is currently no way to restart the cache.
         */
        /* should not override Thread.interrupt
        public void interrupt() {
        stopping = true;
        }
         */
        /**
         * Find out which node is currently being processed.
         * @return The node which is currently being worked on, null if no work is
         *         in progress.
         */
        public TT_NodeCache getActiveNode() {
            return currentUnit;
        }

        void setPaused(boolean state) {
            synchronized (this) {
                if (state != paused) {
                    paused = state;
                    if (paused && currentUnit != null) {
                        currentUnit.pause();
                        suspendedQueue.addFirst(currentUnit);
                    }

                    // wake up
                    if (!paused) {
                        this.notify();
                    }
                }
            }
        }

        synchronized boolean isPaused() {
            return paused;
        }
        // ------- private -------
        /**
         * Request that the worker process the given unit ASAP.
         * This method will first verify that the given node is a candidate for
         * work.  This method is synchronized to avoid context switching into
         * other parts of this class.
         * @param what Which node to give attention to.
         */
        synchronized void requestActiveNode(TT_NodeCache what) {
            if (what != null && currentUnit != what && what.canRun()) {
                // This will cause processing on the worker thread to terminate
                // and it will do selection via getNextUnit().  That will return
                // the priority unit below.
                priorityUnit = what;
                if (currentUnit != null) {
                    currentUnit.pause();
                    suspendedQueue.addFirst(currentUnit);
                }
            }
        }

        /**
         * From the available queues to be processed, select the most ripe.
         */
        private TT_NodeCache getNextUnit() {
            boolean wasPriority = false;
            TT_NodeCache next = null;

            synchronized (htLock) {
                // schedule next node in this priority:
                // 1) a priority unit
                // 2) a previously suspended unit
                // 3) the next unit in the queue
                if (priorityUnit != null) {
                    wasPriority = true;
                    next = priorityUnit;
                    priorityUnit = null;
                } else {
                    switch (SCHEDULING_ALGO) {
                        case QUEUE:
                            next = selectByQueuing();
                            break;
                        case DEPTH:
                            next = selectByDepth();
                            break;
                        default:
                            next = selectByQueuing();
                    }
                }

                // additional selection criteria
                if (next != null && !next.canRun()) // discard unrunnable jobs
                {
                    return getNextUnit();
                } else // ok, schedule this one
                {
                    return next;
                }
            }   // sync
        }

        // -------- various scheduling algorithms ----------
        /**
         * Simply do it in the order the requests came in.
         * The root is excluded from selection until the last possible
         * time.
         */
        private TT_NodeCache selectByQueuing() {
            TT_NodeCache selection = null;

            if (suspendedQueue.size() > 0) {
                selection = (TT_NodeCache) (suspendedQueue.removeLast());
            } else if (cacheQueue.size() > 0) {
                selection = (TT_NodeCache) (cacheQueue.removeLast());
            }
            if (selection != null &&
                    selection.getNode().isRoot() && // trying to avoid root
                    (cacheQueue.size() > 0 || suspendedQueue.size() > 0)) {
                cacheQueue.addFirst(selection);
                return selectByQueuing();
            }

            return selection;
        }

        /**
         * Select the deepest node in the cacheQueue.
         */
        private TT_NodeCache selectByDepth() {
            // note must already have htLock
            // note with this algo., the suspend and cache queues are
            //      basically equivalent
            TT_NodeCache selected = null;
            int depth = -1;
            LinkedList theList = cacheQueue;
            boolean notDone = true;
            int count = 0;

            if (cacheQueue.size() == 0) {
                if (suspendedQueue.size() == 0) {
                    notDone = false;
                } else {
                    theList = suspendedQueue;
                }
            } else {
            }

            while (notDone) {
                TT_NodeCache possible = (TT_NodeCache) (theList.get(count));
                int thisDepth = TestResultTable.getObjectPath(possible.getNode()).length;

                if (thisDepth > depth) {
                    theList.remove(count);

                    // requeue the last deepest node found
                    if (selected != null) {
                        cacheQueue.addFirst(selected);
                        // adjust the counter since we just added one
                        if (theList == cacheQueue) {
                            count++;
                        }
                    }
                    depth = thisDepth;
                    selected = possible;
                }

                count++;
                if (count >= theList.size()) {
                    if (theList == suspendedQueue) {
                        notDone = false;
                    } else if (suspendedQueue.size() != 0) {
                        theList = suspendedQueue;
                        count = 0;
                    } else {
                        notDone = false;
                    }
                } else {
                }
            }   // while

            return selected;
        }

        private synchronized void finishJob(TT_NodeCache item) {
            // do not send update message if it's not important (onscreen)
            /* if (true || !relevantNodes.contains(item.getNode())) {
                return;
            }*/
            TreeModelEvent e = null;
            TestResultTable.TreeNode node = item.getNode();

            // switch event format if the node is the root
            if (node.isRoot()) {
                e = new TreeModelEvent(this, new Object[]{getRoot()}, (int[]) null, (Object[]) null);
            } else {
                // full path to the node, inclusive
                //TestResultTable.TreeNode[] fp = TestResultTable.getObjectPath(node);

                // partial path - for JTree event
                //TestResultTable.TreeNode[] pp = new TestResultTable.TreeNode[fp.length - 1];
                //System.arraycopy(fp, 0, pp, 0, fp.length - 1);
                TreePath path = resolveUrl(TestResultTable.getRootRelativePath(node));
                if (path != null) {
                    // index in parent of target node - required by JTree
                    // ignore this operation if we are at the root (length==1)
                    TreePath pp = path.getParentPath();
                    TT_BasicNode bn = (TT_BasicNode)(path.getLastPathComponent());
                    int index = ((TT_BasicNode)(pp.getLastPathComponent())).getIndex(bn);

                    //e = makeEvent(pp, bn, index);
                    e = new TreeModelEvent(this, pp, new int[] {index}, new Object[] {bn});
                }
            }

            // dispatch event
            notifyModelListeners(e, Notifier.CHANGE);

            if (debug > 0)
                Debug.println("NodeCache done for " + item.getNode().getName());
        }
        private volatile boolean paused;
        private volatile boolean stopping;
        private volatile TT_NodeCache priorityUnit;
        private volatile TT_NodeCache currentUnit;
        private volatile boolean isReallyPaused;
        private static final int QUEUE = 0;
        private static final int DEPTH = 1;
        private static final int SCHEDULING_ALGO = DEPTH;
    }

    // inner class
    /**
     * Object used to physically dispatch any model update events onto the event thread.
     */
    private static class Notifier implements Runnable {

        /**
         * Create a event notification object to be scheduled on the GUI event thread.
         * The type translates to a switch between the different possible observer
         * methods.
         *
         * @param eType Type of observer message to generate.
         *        Must not be greater than zero, see the defined constants.
         * @param listeners The listeners to notify.  This is shallow copied immediately.
         *        Must not be null.  May be of zero length.
         * @param e The event to give to the listeners.
         *        Must not be null.
         */
        Notifier(int eType, TreeModelListener[] listeners, TreeModelEvent e,
                UIFactory uif) {
            type = eType;
            this.e = e;
            this.uif = uif;

            // make shallow copy
            TreeModelListener[] copy = new TreeModelListener[listeners.length];
            System.arraycopy(listeners, 0, copy, 0, listeners.length);
            l = copy;
        }

        public void run() {
            if (e == null)
                return;

            switch (type) {
                case CHANGE:
                    Object[] path = e.getPath();
                    if (path != null && path.length > 0 &&
                        path[path.length-1] instanceof TT_BasicNode) {
                        TT_BasicNode bn = (TT_BasicNode)(path[path.length-1]);
                        if (e.getChildIndices() != null &&
                            e.getChildIndices().length >= 1) {
                            if (bn.getChildCount() <= e.getChildIndices()[0])
                                return;
                        }
                    }

                    for (int i = 0; i < l.length; i++) {
                        ((TreeModelListener) l[i]).treeNodesChanged(e);
                    }
                    break;
                case STRUCT:
                    for (int i = 0; i < l.length; i++) {
                        ((TreeModelListener) l[i]).treeStructureChanged(e);
                    }
                    break;
                case INS:
                    for (int i = 0; i < l.length; i++) {
                        ((TreeModelListener) l[i]).treeNodesInserted(e);
                    }
                    break;
                case DEL:
                    for (int i = 0; i < l.length; i++) {
                        ((TreeModelListener) l[i]).treeNodesRemoved(e);
                    }
                    break;
                default:
                    throw new JavaTestError(uif.getI18NString("tree.noEType"));
            }   // switch
        }
        TreeModelListener[] l;
        int type;
        TreeModelEvent e;
        UIFactory uif;
        static final int CHANGE = 0;
        static final int STRUCT = 1;
        static final int INS = 2;
        static final int DEL = 3;
    }

    // FilterSelectionHandler.Observer - may not be on event thread
    private class FilterWatcher implements FilterSelectionHandler.Observer {

        public void filterUpdated(TestFilter f) {
            //notifyFullStructure();
            invalidateNodeInfo();
        }

        public void filterSelected(TestFilter f) {
            //notifyFullStructure();
            if (!lastFilter.equals(f)) {
                invalidateNodeInfo();
            }
            lastFilter = f;
        }

        public void filterAdded(TestFilter f) {
            // don't care
        }

        public void filterRemoved(TestFilter f) {
            // don't care
        }
    }
}

