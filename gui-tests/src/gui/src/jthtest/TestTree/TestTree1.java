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
import jthtest.tools.JTFrame;
import jthtest.tools.TestTree;
import org.netbeans.jemmy.TimeoutExpiredException;

public class TestTree1 extends Test {

     @Override
     public void testImpl() throws Exception {
          mainFrame = new JTFrame(true);

          mainFrame.openDefaultTestSuite();

          TestTree tree = mainFrame.getTestTree();
          TreePath root = tree.getRoot();
          if (!tree.isExpanded(root)) {
               tree.click(root);
          }
          tree.click(root);
          try {
               tree.waitForCollapse(root);
          } catch (TimeoutExpiredException e) {
               if (!tree.isExpanded(root)) {
                    errors.add("Tree root is not collapsed after mouse click");
               }
          }
          tree.click(root);
          try {
               tree.waitForExpand(root);
          } catch (TimeoutExpiredException e) {
               if (!tree.isExpanded(root)) {
                    errors.add("Tree root is not expanded after mouse click");
               }
          }
     }

     @Override
     public String getDescription() {
          return "This test checks that root element would be collapsed and expanded after double click on it";
     }
}
