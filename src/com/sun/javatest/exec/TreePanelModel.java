/*
 * $Id$
 *
 * Copyright (c) 2001, 2009, Oracle and/or its affiliates. All rights reserved.
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

import javax.swing.tree.TreePath;
import com.sun.javatest.TestResult;
import com.sun.javatest.TestResultTable;

/**
 * Interface to which components can request that certain tests or nodes be
 * selected.  This is really the state model for the exec tool tree and
 * associated panels.  The TreePanelModel is the primary state model for these
 * components, and TestTreeModel the data model.
 *
 * @see com.sun.javatest.exec.TestTreeModel
 */

// using "Object" node type for now.  Unless the tree model is changed, it will always
// be a TestResultTable.TreeNode type.

interface TreePanelModel {
    // notification
    public void nodeSelected(Object node, TreePath path);
    public void testSelected(TestResult node, TreePath path);
    public void nodeUnSelected(Object node, TreePath path);
    public void testUnSelected(TestResult node, TreePath path);

    // requests
    public void showNode(Object node, TreePath path);
    public void showNode(String url);

    public void showTest(TestResult node, TreePath path);
    public void showTest(TestResult node);
    public void showTest(String url);

    public void hideNode(Object node, TreePath path);
    public void hideTest(TestResult node, TreePath path);

    /**
     * Get the active test result table.
     * This value may change over time.
     */
    public TestResultTable getTestResultTable();

    /**
     * Find out which test is selected.
     * @return null if a test is not selected.  Otherwise the string from
     *         <code>TestResult.getTestName()</code>.
     */
    public String getSelectedTest();

    //public String getSelectedNode();          // uncomment to use

    /**
     * Return the folder or test that the user is currently viewing.
     */
    //public Object getSelectedObject();        // uncomment to use

    public boolean isActive(TT_TreeNode node);
    public boolean isActive(TestResult tr);

    /**
     * Pause background processing.
     */
    void pauseWork();

    /**
     * UnPause background processing.
     */
    void unpauseWork();

    void refreshTree();
}

