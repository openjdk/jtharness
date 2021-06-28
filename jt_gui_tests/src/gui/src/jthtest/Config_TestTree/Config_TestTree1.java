/*
 * $Id$
 *
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
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
package jthtest.Config_TestTree;

import com.sun.interview.wizard.selectiontree.SelectionTree;
import jthtest.Test;
import jthtest.tools.ConfigDialog;
import jthtest.tools.Configuration;
import jthtest.tools.JTFrame;

/**
 *
 * @author andrey
 */
public class Config_TestTree1 extends Test {

    @Override
    public void testImpl() throws Exception {
        mainFrame = new JTFrame(true);
        mainFrame.openDefaultTestSuite();
        addUsedFile(mainFrame.createWorkDirectoryInTemp());
        Configuration conf = mainFrame.getConfiguration();
        conf.load(CONFIG_NAME, true);

        ConfigDialog cd = conf.openByMenu(true);
        ConfigDialog.QuestionTree tree = cd.getQuestionTree();

        int initRowCount = tree.getRowCount();
        SelectionTree stree = tree.getTree();
        int initSelected = stree.getSelection().length;
        if (initRowCount != 3) {
            errors.add("Initially there are not 3 visible rows in the tree");
        }
        if (initSelected != 1) {
            errors.add("Initially there are " + initSelected + " selected rows in the tree while 1 (root) expected");
        }

        tree.clickOnRow(0);
        if (stree.getSelectionRows()[0] != 0) {
            errors.add(stree.getSelectionRows()[0] + " row is selected while expected 0");
        }
        tree.clickOnRow(1);
        if (stree.getSelectionRows()[0] != 1) {
            errors.add(stree.getSelectionRows()[0] + " row is selected while expected 1");
        }
        tree.clickOnRow(2);
        if (stree.getSelectionRows()[0] != 2) {
            errors.add(stree.getSelectionRows()[0] + " row is selected while expected 2");
        }

        if (tree.getRowCount() != initRowCount) {
            errors.add("Visible row count changes after clicking on row");
        }
        if (initSelected != stree.getSelection().length) {
            errors.add("Selected row count changed: was " + initSelected + " now " + stree.getSelection().length);
        }

        tree.clickOnCheckbox(0);
        if (stree.getSelection().length != 0) {
            errors.add("There are " + stree.getSelection().length + " selected row while 0 expected");
        }
        tree.clickOnCheckbox(1);
        if (stree.getSelection().length != 1) {
            errors.add("There are " + stree.getSelection().length + " selected row while 1 expected");
        }
        tree.clickOnCheckbox(2);
        if (stree.getSelection().length != 1) {
            errors.add("There are " + stree.getSelection().length + " selected row while 1 expected (root)");
        }

        if (tree.getRowCount() != initRowCount) {
            errors.add("Visible row count changes after clicking on checkbox");
        }

        tree.clickOnArrow(0);
        if (tree.getRowCount() != 1) {
            errors.add("Tree is not collapsed after clicking on first row. Rows visible: " + tree.getRowCount());
        }

        tree.clickOnArrow(0);
        if (tree.getRowCount() != initRowCount) {
            errors.add("Tree is not expanded after clicking on first row");
        }

        tree.clickOnArrow(2);
        if (tree.getRowCount() == initRowCount) {
            errors.add("Tree is not expanded after clicking on 2 row. Rows visible: " + tree.getRowCount());
        }
        initRowCount = tree.getRowCount();


        tree.clickOnArrow(0);
        if (tree.getRowCount() != 1) {
            errors.add("Tree is not collapsed after clicking on first row. Rows visible: " + tree.getRowCount());
        }

        tree.clickOnArrow(0);
        if (tree.getRowCount() != initRowCount) {
            errors.add("Tree is not expanded after clicking on first row");
        }
    }
}
