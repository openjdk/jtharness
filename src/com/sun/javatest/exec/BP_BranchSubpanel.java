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

import java.awt.Color;
import javax.swing.JPanel;

import com.sun.javatest.TestResult;
import com.sun.javatest.TestResultTable;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.util.I18NResourceBundle;
import java.util.ArrayList;

/**
 * Base class for the individual displays of the BranchPanel.
 */
abstract class BP_BranchSubpanel extends JPanel {

    BP_BranchSubpanel(String name, UIFactory uif, BP_Model model, TestTreeModel ttm,
            String uiKey) {
        setName(name);
        setBackground(Color.white);
        uif.setAccessibleInfo(this, uiKey);

        this.uif = uif;
        this.model = model;
        this.ttm = ttm;
    }

    void setTreeModel(TestTreeModel ttm) {
        this.ttm = ttm;
    }

    boolean isUpdateRequired(TT_BasicNode currNode) {
        return (subpanelNode != currNode);
    }

    protected void updateSubpanel(TT_BasicNode currNode) {
        subpanelNode = currNode;
    }

    /**
     * The test filters have either completely changed or they have
     * changed state.  In either case, subpanels should update
     * anything which depends on the filters.
     */
    protected void invalidateFilters() {
        filtersInvalidated = true;
    }

    protected void showMessage(String msg) {
        // this not on the event thread yet, it gets switched in
        // BranchModel
        lastMsg = msg;

        if (isVisible()) {
            model.showMessage(lastMsg);
        }
    }

    protected void showMesasge(I18NResourceBundle i18n, String key) {
        showMessage(i18n.getString(key));
    }

    // this method is to allow the GUI to restore the message if the
    // active panel is changed
    protected String getLastMessage() {
        return lastMsg;
    }

    protected void showTest(final TestResult tr) {
        TT_BasicNode root = (TT_BasicNode) (ttm.getRoot());
        if (root == null) {
            return;        // construct the path required by the model
        }
        TestResultTable.TreeNode p = tr.getParent();
        TestResultTable.TreeNode[] path = TestResultTable.getObjectPath(tr);

        if (path == null || path.length == 0) {
            // special root case - a test at the root
            TT_TreeNode node = root.findByName(tr.getTestName());
            if (node == null) {
                return;
            }
        }

        TT_BasicNode spot = root;
        ArrayList<TT_TreeNode> list = new ArrayList(path.length + 1);
        list.add(root);
        for (int i = 1; i < path.length; i++) {
            try {
                spot = (TT_BasicNode) spot.findByName(path[i].getName());
                if (spot == null)
                    return;     // not available in tree

                list.add(spot);
            } catch (ClassCastException e) {
                // for some reason the path isn't valid
                // assume that spot was a TT_TestNode for some reason
                return;
            }
        }

        TT_TreeNode tn = spot.findByName(tr);
        if (!(tn instanceof TT_TestNode))
            return;     // path does not refer to a test apparently

        Object[] fp = new Object[list.size() + 1];
        list.toArray(fp);
        fp[fp.length-1] = tn;       // test node is the last in the path

        model.showTest(tr, fp);
    }
    protected TT_BasicNode subpanelNode;
    protected UIFactory uif;
    protected String lastMsg;
    protected BP_Model model;
    protected TestTreeModel ttm;
    protected boolean filtersInvalidated;
}

