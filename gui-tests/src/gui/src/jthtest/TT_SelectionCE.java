/*
 * $Id$
 *
 * Copyright (c) 2011,2024 Oracle and/or its affiliates. All rights reserved.
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
package jthtest;

import java.util.Arrays;
import jthtest.Test;
import jthtest.tools.JTFrame;
import jthtest.tools.Task.Waiter;
import jthtest.tools.TestTree;

public abstract class TT_SelectionCE extends Test {

     /**
      * This is a base class for TT_SelectionCE which contains TestTree functions
      */

     public TT_SelectionCE(String description, int[] rows) {
          this.testDescription = description;
          this.rows = rows;
     }

     protected abstract void make();

     protected void openTree() {
          tree.click(2);
          tree.click(1);
     }

     protected boolean check(int init[], int res[]) {
          return !Arrays.equals(init, res);
     }

     @Override
     public final void testImpl() throws Exception {
          mainFrame = new JTFrame(true);
          mainFrame.openDefaultTestSuite();
          addUsedFile(mainFrame.createWorkDirectoryInTemp());

          tree = mainFrame.getTestTree();

          openTree();
          tree.selectRows(rows);
          make();

          // waiter 10 sec
          Waiter waiter = new Waiter() {

               @Override
               protected boolean check() {
                    res = tree.getSelectedRowsIndexes();
                    if (res == null) {
                         return false;
                    }
                    Arrays.sort(res);

                    return !TT_SelectionCE.this.check(rows, res);
               }

               int[] res = {};

               @Override
               protected String getTimeoutExceptionDescription() {
                    return String.format(
                              "Error occured - selection lost. Initialy selected rows: %s. After %s selected rows: %s",
                              Arrays.toString(rows), testDescription, Arrays.toString(res));
               }
          };

          waiter.getResult();
     }

     protected TestTree tree;
     private String testDescription = "";
     private int[] rows;
}
