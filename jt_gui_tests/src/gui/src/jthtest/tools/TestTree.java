/*
 * $Id$
 *
 * Copyright (c) 2009, 2011, Oracle and/or its affiliates. All rights reserved.
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
package jthtest.tools;

import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.tree.TreePath;
import jthtest.Tools;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 *
 * @author at231876
 */
public class TestTree {

	private JTreeOperator tree;
	private JTFrame frame;

	public TestTree(JTFrame mainFrame) {
		tree = new JTreeOperator(mainFrame.getJFrameOperator());
		frame = mainFrame;
	}

	public boolean[] getRowsVisibility() {
		int rows = tree.getVisibleRowCount();
		boolean[] visibleRows = new boolean[rows];
		for (int i = 0; i < rows; i++) {
			visibleRows[i] = tree.isVisible(tree.getPathForRow(i));
		}
		return visibleRows;
	}

	public TreePath[] getVisibleRows() {
		int rows = tree.getVisibleRowCount();
		LinkedList<TreePath> paths = new LinkedList<TreePath>();
		for (int i = 0; i < rows; i++) {
			TreePath path = tree.getPathForRow(i);
			if (tree.isVisible(path)) {
				paths.add(path);
			}
		}
		return paths.toArray(new TreePath[0]);
	}

	public int getVisibleRowCount() {
		int rows = tree.getRowCount();
		int count = 0;
		for (int i = 0; i < rows; i++) {
			if (tree.isVisible(tree.getPathForRow(i))) {
				count++;
			}
		}
		return count;
	}

	public Icon getIcon(int row) {
		return (((JLabel) tree.getRenderedComponent(tree.getPathForRow(row))).getIcon());
	}

	public Icon getIcon(TreePath path) {
		return (((JLabel) tree.getRenderedComponent(path)).getIcon());
	}

	public TreePath[] click(TreePath path) {
		tree.clickOnPath(path, 2);
		return tree.getChildPaths(path);
	}

	public TreePath[] click(int row) {
		TreePath path = tree.getPathForRow(row);
		tree.clickOnPath(path, 2);
		return tree.getChildPaths(path);
	}

	public void clickPopup(TreePath path) {
		tree.clickOnPath(path, 1, MouseEvent.BUTTON3_MASK);
	}

	public void clickPopup(int row) {
		TreePath path = tree.getPathForRow(row);
		tree.clickOnPath(path, 1, MouseEvent.BUTTON3_MASK);
	}

	public TreePath getRoot() {
		return tree.getPathForRow(0);
	}

	public boolean isVisible(TreePath path) {
		return tree.isVisible(path);
	}

	public boolean isExpanded(TreePath path) {
		return tree.isExpanded(path);
	}

	public void waitForExpand(TreePath path) {
		int time = 0;
		try {
			while (time < Tools.MAX_WAIT_TIME) {
				Thread.sleep(50);
				time += 50;
				if (isExpanded(path)) {
					return;
				}
			}
			throw new TimeoutExpiredException("Waiting for expanding path " + path);
		} catch (InterruptedException ex) {
			Logger.getLogger(TestTree.class.getName()).log(Level.SEVERE, null, ex);
			throw new JemmyException("Exception occured", ex);
		}
	}

	public void waitForCollapse(TreePath path) {
		int time = 0;
		try {
			while (time < Tools.MAX_WAIT_TIME) {
				Thread.sleep(50);
				time += 50;
				if (tree.isCollapsed(path)) {
					return;
				}
			}
			throw new TimeoutExpiredException("Waiting for collapsing path " + path);
		} catch (InterruptedException ex) {
			Logger.getLogger(TestTree.class.getName()).log(Level.SEVERE, null, ex);
			throw new JemmyException("Exception occured", ex);
		}
	}

	public void selectRows(int... rows) {
		tree.setSelectionRows(rows);
	}

	public TreePath[] getSelectedRows() {
		return tree.getSelectionPaths();
	}

	public int[] getSelectedRowsIndexes() {
		return tree.getSelectionRows();
	}

	public boolean isTest(TreePath path) {
		return tree.getModel().isLeaf(path.getLastPathComponent());
	}

	public boolean isDirectory(TreePath path) {
		return !isTest(path);
	}

	public TreePath getPathForRow(int i) {
		return tree.getPathForRow(i);
	}

	public void clearResults() {
		clickPopup(getRoot());
		new JPopupMenuOperator(frame.getJFrameOperator()).pushMenuNoBlock("Clear Results");
		new JButtonOperator(new JDialogOperator("Confirm Clear Operation"), "Yes").push();
		new Task.Waiter() {

			@Override
			protected boolean check() {
				return frame.getPassedCounter() == 0 && frame.getNotRunCounter() == frame.getAllTestsCounter();
			}

			@Override
			protected String getTimeoutExceptionDescription() {
				return "Timeout expired while waiting for clearing tests";
			}
		}.waitForDone();
	}

	public Task<Boolean> runTests() {
		if (tree.getSelectionCount() < 1) {
			return frame.runTests(0);
		}
                
                return frame.runTests(tree.getSelectionRows()[0]);
	}

	public Task<Boolean> runAllTests() {
		return frame.runTests(0);
	}
}
