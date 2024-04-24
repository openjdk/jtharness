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

import javax.swing.tree.TreePath;
import jthtest.Test;
import jthtest.Tools;
import jthtest.tools.JTFrame;
import jthtest.tools.TestTree;

public class TestTree5 extends Test {

     @Override
     public void testImpl() throws Exception {
          mainFrame = new JTFrame(true);

          mainFrame.openDefaultTestSuite();
          addUsedFile(mainFrame.createWorkDirectoryInTemp());
          mainFrame.getConfiguration().load(Tools.CONFIG_NAME, true);

          TestTree tree = mainFrame.getTestTree();

          tree.click(2);
          tree.click(1);

          tree.selectRows(1, 2);
          TreePath[] selectedPaths = tree.getSelectedRows();

          TreePath root = tree.getRoot();
          tree.clickPopup(root);
          TreePath[] selectedPaths1 = tree.getSelectedRows();
          if (selectedPaths.length != selectedPaths1.length) {
               errors.add("Selection changed when performing right click");
          } else {
               for (int i = 0; i < selectedPaths.length; i++) {
                    if (!selectedPaths[i].equals(selectedPaths1[i])) {
                         errors.add("Selection changed when performing right click");
                         break;
                    }
               }
          }

                mainFrame.click();
          tree.selectRows(1);
          tree.clickPopup(root);
          selectedPaths = tree.getSelectedRows();
          if (selectedPaths.length != 1) {
               errors.add("Selection has " + selectedPaths.length + " elements while expected 1");
          }
          if (!selectedPaths[0].equals(root)) {
               errors.add("Root element " + root + " was expected to be selected, found " + selectedPaths[0]);
          }

                mainFrame.click();
          tree.selectRows(1, 2, 3, 4);
          TreePath path = tree.getPathForRow(5);
          tree.click(path);
          selectedPaths = tree.getSelectedRows();
          if (selectedPaths.length != 1) {
               errors.add("Selection has " + selectedPaths.length + " elements while expected 1");
          }
          if (!selectedPaths[0].equals(path)) {
               errors.add("Element " + path + " was expected to be selected, found " + selectedPaths[0]);
          }
     }

     @Override
     public String getDescription() {
          return "This test checks that performing right-click when only 1 test is selected will replace selection and in case when many tests are selecter will not replace selection.";
     }
}
