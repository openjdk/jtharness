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
package jthtest.TestTree;

import com.sun.javatest.tool.IconFactory;
import javax.swing.Icon;
import javax.swing.tree.TreePath;
import jthtest.Test;
import jthtest.Tools;
import jthtest.tools.JTFrame;
import jthtest.tools.TestTree;

public class TestTree3 extends Test {

     @Override
     public void testImpl() throws Exception {
          mainFrame = new JTFrame(true);

          mainFrame.openDefaultTestSuite();
          addUsedFile(mainFrame.createWorkDirectoryInTemp());
          mainFrame.getConfiguration().load(Tools.CONFIG_NAME, true);

          TestTree tree = mainFrame.getTestTree();

          tree.click(2);
          tree.click(4);
          tree.click(1);

          tree.selectRows(2, 3, 5, 7, 9, 10);

          Icon testNotRunIcon = IconFactory.getTestIcon(IconFactory.NOT_RUN, false, true);
          Icon dirNotRunIcon = IconFactory.getTestFolderIcon(IconFactory.NOT_RUN, false, true);

          Icon testPassedIcon = IconFactory.getTestIcon(IconFactory.PASSED, false, true);
          Icon directoryPassedIcon = IconFactory.getTestFolderIcon(IconFactory.PASSED, false, true);

          Icon testFiledIcon = IconFactory.getTestIcon(IconFactory.FAILED, false, true);
          Icon dirFiledIcon = IconFactory.getTestFolderIcon(IconFactory.FAILED, false, true);

          tree.runTests().waitForDone();

          TreePath[] paths = tree.getSelectedRows();
          for (TreePath path : paths) {
               Icon icon = tree.getIcon(path);
               if (tree.isTest(path)) {
                    if (icon != testPassedIcon) {
                         errors.add("Icon on path " + path + " is invalid");
                    }
               } else {
                    if (path.toString().contains("DoublyLinkedList")) {
                         if (icon != dirFiledIcon) {
                              errors.add("Icon on path " + path + " is invalid");
                         }
                    } else {
                         if (icon != directoryPassedIcon) {
                              errors.add("Icon on path " + path + " is invalid");
                         }
                    }
               }
          }

          tree.click(9);
          if (tree.getIcon(10) != testPassedIcon) {
               errors.add("Icon on path " + tree.getPathForRow(10) + " is invalid");
          }
          if (tree.getIcon(11) != testPassedIcon) {
               errors.add("Icon on path " + tree.getPathForRow(11) + " is invalid");
          }
          if (tree.getIcon(12) != testFiledIcon) {
               errors.add("Icon on path " + tree.getPathForRow(12) + " is invalid");
          }
          if (tree.getIcon(13) != testPassedIcon) {
               errors.add("Icon on path " + tree.getPathForRow(13) + " is invalid");
          }

          tree.clearResults();

          tree.click(19);
          for (int i = 0; i < tree.getVisibleRowCount(); i++) {
               TreePath path = tree.getPathForRow(i);
               Icon icon = tree.getIcon(path);
               if (tree.isTest(path)) {
                    if (icon != testNotRunIcon) {
                         errors.add("Icon on tests path " + path + " is invalid after clearing");
                    }
               } else {
                    if (icon != dirNotRunIcon) {
                         errors.add("Icon on directories path " + path + " is invalid after clearing");
                    }
               }
          }
     }

     @Override
     public String getDescription() {
          return "This test selects some tests in the test tree, executes them, checks their icons, clears them and checks icons again.";
     }
}
