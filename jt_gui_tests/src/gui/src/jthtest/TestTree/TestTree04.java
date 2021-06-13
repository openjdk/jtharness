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

import com.sun.javatest.tool.IconFactory;
import javax.swing.Icon;
import jthtest.Test;
import jthtest.Tools;
import jthtest.tools.JTFrame;
import jthtest.tools.TestTree;

/**
 *
 * @author at231876
 */
public class TestTree04 extends Test {

    @Override
    public void testImpl() throws Exception {
        mainFrame = new JTFrame(true);

        mainFrame.openDefaultTestSuite();
        addUsedFile(mainFrame.createWorkDirectoryInTemp());
        mainFrame.getConfiguration().load(Tools.CONFIG_NAME, true);

        TestTree tree = mainFrame.getTestTree();

        tree.click(1);
        tree.selectRows(3);

        Icon passedTestIcon = IconFactory.getTestIcon(IconFactory.PASSED, false, true);
        Icon passedTestFolderIcon = IconFactory.getTestFolderIcon(IconFactory.PASSED, false, true);
        Icon notrunTestFolderIcon = IconFactory.getTestFolderIcon(IconFactory.NOT_RUN, false, true);
        Icon notrunTestIcon = IconFactory.getTestIcon(IconFactory.NOT_RUN, false, true);

        tree.runTests().waitForDone();

        if (tree.getIcon(3) != passedTestIcon) {
            errors.add("Incorrect icon for path " + tree.getPathForRow(3) + ", passed icon expected");
        }
        if (tree.getIcon(2) != notrunTestIcon) {
            errors.add("Incorrect icon for path " + tree.getPathForRow(2) + ", not run icon expected");
        }
        if (tree.getIcon(4) != notrunTestIcon) {
            errors.add("Incorrect icon for path " + tree.getPathForRow(4) + ", not run icon expected");
        }

        if (tree.getIcon(1) != notrunTestFolderIcon) {
            errors.add("Incorrect icon for path " + tree.getPathForRow(1) + ", not run icon expected");
        }

        tree.selectRows(2, 4, 5, 6, 7);

        tree.runTests().waitForDone();

        if (tree.getIcon(1) != passedTestFolderIcon) {
            errors.add("Incorrect icon for path " + tree.getPathForRow(1) + ", passed icon expected");
        }
    }

    @Override
    public String getDescription() {
        return "This test checks that is case when one of many test is runned in test folder, folder will have 'not run' icon. When all others tests are runned - it should obtain new status.";
    }
}
