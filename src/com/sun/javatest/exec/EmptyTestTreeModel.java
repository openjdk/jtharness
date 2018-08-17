/*
 * $Id$
 *
 * Copyright (c) 2008, 2009, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.*;
import com.sun.javatest.TestResultTable.TreeNode;
import com.sun.javatest.tool.UIFactory;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeModelListener;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Empty tree model for showing fake test tree used
 * if FeatureManager.NO_TREE_WITHOUT_WD set
 * mostly contains stab methods
 */
class EmptyTestTreeModel extends TestTreeModel implements TreeModel, TestResultTable.TreeObserver  {
    private FilterSelectionHandler filterHandler;
    private UIFactory uif;
    private Parameters params;
    private ArrayList<TreeModelListener> treeModelListeners = new ArrayList<>();
    private ArrayList<TestResultTable.TreeNodeObserver> rootObservers = new ArrayList<>();
    private TT_NodeCache oldInfo;
    private TreeNode oldNode;

    private TestResultTable.TreeNode root = new TestResultTable.TreeNode() {

        @Override
        public void addObserver(TestResultTable.TreeNodeObserver obs) {
            rootObservers.add(obs);
        }

        @Override
        public void removeObserver(TestResultTable.TreeNodeObserver obs) {
            rootObservers.remove(obs);
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public TestResultTable.TreeNode getParent() {
            return null;
        }

        @Override
        public boolean isRoot() {
            return true;
        }

        @Override
        public TestResultTable getEnclosingTable() {
            return null;
        }

        @Override
        public boolean isUpToDate() {
            return false;
        }

        @Override
        public int getChildCount() {
            return 0;
        }

        @Override
        public Object getChild(int index) {
            return null;
        }

        @Override
        public TestResult[] getTestResults() {
            return new TestResult[0];
        }

        @Override
        public TestResultTable.TreeNode[] getTreeNodes() {
            return new TestResultTable.TreeNode[0];
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public boolean isLeaf(int index) {
            return false;
        }

        @Override
        public int[] getChildStatus() {
            return new int[0];
        }

        @Override
        public int getIndex(Object target) {
            return 0;
        }

        @Override
        public TestResult matchTest(String url) {
            return null;
        }
    };

    EmptyTestTreeModel(Parameters p, FilterSelectionHandler filterHandler, UIFactory uif) {
        this.filterHandler = filterHandler;
        this.uif = uif;
        this.params = p;
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        return null;
    }

    @Override
    public int getChildCount(Object parent) {
        return 0;
    }

    @Override
    public boolean isLeaf(Object node) {
        return false;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return 0;
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        treeModelListeners.add(l);

    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        treeModelListeners.remove(l);
    }

    public Collection<TreeModelListener> getTreeModelListeners() {
        return treeModelListeners;
    }


    @Override
    public void nodeInserted(TestResultTable.TreeNode[] path, Object what, int index) {

    }

    @Override
    public void nodeChanged(TestResultTable.TreeNode[] path, Object what, int index, Object old) {

    }

    @Override
    public void nodeRemoved(TestResultTable.TreeNode[] path, Object what, int index) {

    }

    @Override
    synchronized TT_NodeCache getNodeInfo(TestResultTable.TreeNode node, boolean highPriority) {
        if (node != oldNode || oldInfo == null) {
            oldNode = node;
            oldInfo = new TT_NodeCache(null, getTestFilter(), null);
        }
        return oldInfo;
    }

    @Override
    TestFilter getTestFilter() {
        return filterHandler.getActiveFilter();
    }

    void setActiveNode(TestResultTable.TreeNode node) {
    }

    @Override
    void pauseWork() {
    }

    @Override
    void unpauseWork() {
    }

    @Override
    void setParameters(Parameters p) {
        params = p;
    }

    @Override
    Parameters getParameters() {
        return params;
    }

    @Override
    TestResultTable getTestResultTable() {
        return null;
    }

    @Override
    void invalidateNodeInfo(TestResultTable.TreeNode[] path) {

    }

    @Override
    void invalidateNodeInfo() {

    }

    public Collection<TestResultTable.TreeNodeObserver> getRootObservers() {
        return rootObservers;
    }

    @Override
    synchronized void dispose() {
        treeModelListeners.clear();
        rootObservers.clear();
    }

}
