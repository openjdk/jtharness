/*
 * $Id$
 *
 * Copyright (c) 2002, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.tool;

import com.sun.javatest.TestResult;
import com.sun.javatest.TestResultTable;

/**
 * A pane for selecting tests in a test result table.
 */
public class TestTreeSelectionPane extends TreeSelectionPane
{
    /**
     * Create a pane for selecting tests found in a test result table.
     * @param trt the test result table from which to get the tests
     * that may be selected
     */
    public TestTreeSelectionPane(TestResultTable trt) {
        super(new TRTModel(trt));
    }

    private static class TRTModel implements Model
    {
        TRTModel(TestResultTable trt) {
            this.trt = trt;
        }

        public Object getRoot() {
            return trt.getRoot();
        }

        public int getChildCount(Object node) {
            if (node == null)
                throw new NullPointerException();
            else if (node instanceof TestResultTable.TreeNode)
                return ((TestResultTable.TreeNode) node).getChildCount();
            else if (node instanceof TestResult)
                return 0;
            else
                throw new IllegalArgumentException();
        }

        public Object getChild(Object node, int index) {
            if (node == null)
                throw new NullPointerException();
            else if (node instanceof TestResultTable.TreeNode)
                return ((TestResultTable.TreeNode) node).getChild(index);
            else if (node instanceof TestResult)
                return null;
            else
                throw new IllegalArgumentException();
        }

        public String getName(Object node) {
            if (node == null)
                throw new NullPointerException();
            else if (node instanceof TestResultTable.TreeNode)
                return ((TestResultTable.TreeNode) node).getName();
            else if (node instanceof TestResult) {
                TestResult tr = (TestResult) node;
                String fullName = tr.getTestName();
                int lastSlash = fullName.lastIndexOf("/");
                return (lastSlash == -1
                        ? fullName
                        : fullName.substring(lastSlash+1));

            }
            else
                throw new IllegalArgumentException();
        }

        public String getPath(Object node) {
            if (node == null)
                throw new NullPointerException();
            else if (node instanceof TestResult)
                return ((TestResult) node).getTestName();
            else if (node instanceof TestResultTable.TreeNode) {
                TestResultTable.TreeNode tn = (TestResultTable.TreeNode) node;
                if (tn.isRoot())
                    return tn.getName();
                else
                    return getPath(tn.getParent() + "/" + tn.getName());
            }
            else
                throw new IllegalArgumentException();
        }

        public boolean isLeaf(Object node) {
            if (node == null)
                throw new NullPointerException();
            else if (node instanceof TestResult)
                return true;
            else if (node instanceof TestResultTable.TreeNode)
                return false;
            else
                throw new IllegalArgumentException();
        }

        private TestResultTable trt;
    }
}
