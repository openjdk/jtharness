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
import jthtest.tools.JTFrame;
import jthtest.tools.TestTree;

/**
 *
 * @author at231876
 */
public class TestTree02 extends Test {

	@Override
	public void testImpl() throws Exception {
		mainFrame = new JTFrame(false);

		mainFrame.openDefaultTestSuite();

		TestTree testTree = mainFrame.getTestTree();
		TreePath[] visibleRows = testTree.getVisibleRows();
		testTree.click(visibleRows[1]);
		if(testTree.getVisibleRowCount() != 9) {
			errors.add("There are " + testTree.getVisibleRowCount() + " visible rows in the tree while 9 expected (1st click)");
		}
		testTree.click(visibleRows[1]);
		if(testTree.getVisibleRowCount() != 3) {
			errors.add("There are " + testTree.getVisibleRowCount() + " visible rows in the tree while 3 expected (2nd click)");
		}
		TreePath[] click = testTree.click(visibleRows[2]);
		if(testTree.getVisibleRowCount() != 6) {
			errors.add("There are " + testTree.getVisibleRowCount() + " visible rows in the tree while 6 expected (3rd click)");
		}
		testTree.click(click[2]);
		if(testTree.getVisibleRowCount() != 9) {
			errors.add("There are " + testTree.getVisibleRowCount() + " visible rows in the tree while 9 expected (4th click)");
		}
		testTree.click(visibleRows[2]);
		if(testTree.getVisibleRowCount() != 3) {
			errors.add("There are " + testTree.getVisibleRowCount() + " visible rows in the tree while 3 expected (5th click)");
		}
	}

	@Override
	public String getDescription() {
		return "This test checks that rows are expanded/collapsed normaly.";
	}
}
