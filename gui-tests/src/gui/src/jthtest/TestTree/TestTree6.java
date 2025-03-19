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

import jthtest.Test;
import jthtest.Tools;
import jthtest.tools.JTFrame;
import jthtest.tools.TestTree;

public class TestTree6 extends Test {

     /**
      * This test checks that collapsing and expanding again nesting test folder will
      * not collapse nested test folders
      */

     @Override
     public void testImpl() throws Exception {
          mainFrame = new JTFrame(true);

          mainFrame.openDefaultTestSuite();
          addUsedFile(mainFrame.createWorkDirectoryInTemp());
          mainFrame.getConfiguration().load(Tools.CONFIG_NAME, true);

          TestTree tree = mainFrame.getTestTree();

          tree.click(2);
          tree.click(5);
          tree.click(4);
          tree.click(3);
          tree.click(1);

          if (tree.getVisibleRowCount() != 24) {
               errors.add(
                         "Tree contains " + tree.getVisibleRowCount() + " rows when 23 expected (all paths are expanded)");
          }

          tree.click(8);

          if (tree.getVisibleRowCount() != 10) {
               errors.add("Tree contains " + tree.getVisibleRowCount() + " rows when 9 expected");
          }

          tree.click(8);

          if (tree.getVisibleRowCount() != 24) {
               errors.add(
                         "Tree contains " + tree.getVisibleRowCount() + " rows when 23 expected (all paths are expanded)");
          }
     }
}
