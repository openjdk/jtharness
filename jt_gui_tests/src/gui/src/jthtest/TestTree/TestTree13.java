/*
 * $Id$
 *
 * Copyright (c) 2009, 2010, Oracle and/or its affiliates. All rights reserved.
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
import com.sun.javatest.tool.IconFactory;
import javax.swing.Icon;

/**
 *
 * @author at231876
 */
public class TestTree13 extends Test {
    private TestTree tree;
    private Icon passedTest = IconFactory.getTestIcon(IconFactory.PASSED, false, true);
    private Icon failedTest = IconFactory.getTestIcon(IconFactory.FAILED, false, true);
    private Icon passedTestFolder = IconFactory.getTestFolderIcon(IconFactory.PASSED, false, true);
    private Icon failedTestFolder = IconFactory.getTestFolderIcon(IconFactory.FAILED, false, true);

    @Override
    public void testImpl() throws Exception {
        mainFrame = new JTFrame(true);

        mainFrame.openDefaultTestSuite();
        addUsedFile(mainFrame.createWorkDirectoryInTemp());
        mainFrame.getConfiguration().load(Tools.CONFIG_NAME, true);

        tree = mainFrame.getTestTree();

        tree.click(2);
        tree.click(5);
        tree.click(4);
        tree.click(3);
        tree.click(1);

        tree.clearResults();
        mainFrame.runTests().waitForDone();
        checkTests();
        tree.runAllTests().waitForDone();
        checkTests();
    }

    @Override
    public String getDescription() {
        return "This test clears all the tree, then runs all tests firstly by menu and then by mouse and checks icons";
    }

    private void checkTests() {
        for (int i = 0; i < tree.getVisibleRowCount(); i++) {
            TreePath path = tree.getPathForRow(i);
            Icon icon = tree.getIcon(path);
            if (i == 0) {
                if (icon != failedTestFolder) {
                    errors.add("Tree root icon is not failed as expected");
                }
            } else if (i == 8) {
                if (icon != failedTestFolder) {
                    errors.add("Icon on path " + path + " is not failed as expected");
                }
            } else if (i == 9) {
                if (icon != failedTestFolder) {
                    errors.add("Icon on path " + path + " is not failed as expected");
                }
            } else if (i == 12) {
                if (icon != failedTest) {
                    errors.add("Icon on path " + path + " is not failed as expected");
                }
            } else {
                if (tree.isTest(path)) {
                    if (icon != passedTest) {
                        errors.add("Icon on path " + path + " is not passed as expected");
                    }
                } else {
                    if (icon != passedTestFolder) {
                        errors.add("Icon on path " + path + " is not passed as expected");
                    }
                }
            }
        }
    }
}
