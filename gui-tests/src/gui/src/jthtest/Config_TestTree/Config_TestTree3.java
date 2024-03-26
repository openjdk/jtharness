/*
 * $Id$
 *
 * Copyright (c) 2009, 2024, Oracle and/or its affiliates. All rights reserved.
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

public class Config_TestTree3 extends Test {
     /**
      * This test verifies that multiple test results can be cleared Test Tree by
      * pressing right mouse button -> Clear Results.
      */
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
          if (initRowCount != 4) {
               errors.add("Initially there are not 4 visible rows in the tree");
          }
          if (initSelected != 1) {
               errors.add("Initially there are " + initSelected + " selected rows in the tree while 1 (root) expected");
          }

          tree.openContextMenu(-1).pushExpandAll();
          if (tree.getRowCount() != 29) {
               errors.add("There are " + tree.getRowCount() + " visible rows while expected 29");
          }

          tree.clickOnCheckbox(7);
          tree.clickOnCheckbox(8);
          if (stree.getSelection().length != 6) {
               errors.add("There are " + stree.getSelection().length + " selected rows while expected 6");
          }

          tree.openContextMenu(9).pushSelectAll();
          if (stree.getSelection().length != 7) {
               errors.add("There are " + stree.getSelection().length + " selected rows while expected 7");
          }

          tree.openContextMenu(1).pushDeselectAll();
          tree.clickOnCheckbox(10);
          if (stree.getSelection().length != 4) {
               errors.add("There are " + stree.getSelection().length + " selected rows while expected 4");
          }

          tree.openContextMenu(0).pushDeselectAll();
          if (stree.getSelection().length != 0) {
               errors.add("There are " + stree.getSelection().length + " selected rows while expected 0");
          }

          tree.openContextMenu(3).pushSelectAll();
          tree.openContextMenu(4).pushSelectAll();
          if (stree.getSelection().length != 2) {
               errors.add("There are " + stree.getSelection().length + " selected rows while expected 2");
          }

          tree.openContextMenu(9).pushCollapseAll();
          if (tree.getRowCount() != 25) {
               errors.add("There are " + tree.getRowCount() + " visible rows while expected 25");
          }
     }
}
