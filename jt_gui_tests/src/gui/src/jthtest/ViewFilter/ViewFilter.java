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

package jthtest.ViewFilter;

import java.lang.reflect.InvocationTargetException;

import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JMenuOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

import jthtest.Tools;

/**
 *
 * @author naryl
 */
public class ViewFilter extends Tools {
	

	protected static final String[] failedListsTests = new String[]{"lists/DoublyLinkedList/InsertTest.java"};
	protected static final String[] passedListsTests = new String[]{"lists/DoublyLinkedList/AppendTest.java", "lists/DoublyLinkedList/EqualsTest.java", "lists/DoublyLinkedList/RemoveTest.java", "lists/LinkedList/AppendTest.java", "lists/LinkedList/EqualsTest.java", "lists/LinkedList/InsertTest.java", "lists/LinkedList/RemoveTest.java", "lists/SortedList/EqualsTest.java", "lists/SortedList/InsertTest.java", "lists/SortedList/RemoveTest.java"};
	protected static final String[] listsTests = new String[]{"lists/DoublyLinkedList/AppendTest.java", "lists/DoublyLinkedList/EqualsTest.java", "lists/DoublyLinkedList/InsertTest.java", "lists/DoublyLinkedList/RemoveTest.java", "lists/LinkedList/AppendTest.java", "lists/LinkedList/EqualsTest.java", "lists/LinkedList/InsertTest.java", "lists/LinkedList/RemoveTest.java", "lists/SortedList/EqualsTest.java", "lists/SortedList/InsertTest.java", "lists/SortedList/RemoveTest.java"};
	protected static final String[] bignumTests = new String[]{"BigNum/AddTest.java", "BigNum/CompareTest.java", "BigNum/EqualsTest.java", "BigNum/LongConstrTest.java", "BigNum/StringConstrTest.java", "BigNum/SubtractTest.java"};
	protected static final String[] allTests = new String[]{"BigNum/AddTest.java", "BigNum/CompareTest.java", "BigNum/EqualsTest.java", "BigNum/LongConstrTest.java", "BigNum/StringConstrTest.java", "BigNum/SubtractTest.java", "lists/DoublyLinkedList/AppendTest.java", "lists/DoublyLinkedList/EqualsTest.java", "lists/DoublyLinkedList/InsertTest.java", "lists/DoublyLinkedList/RemoveTest.java", "lists/LinkedList/AppendTest.java", "lists/LinkedList/EqualsTest.java", "lists/LinkedList/InsertTest.java", "lists/LinkedList/RemoveTest.java", "lists/SortedList/EqualsTest.java", "lists/SortedList/InsertTest.java", "lists/SortedList/RemoveTest.java"};
	
	public void startWithDefaultWorkdir() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
		startJavatest(TEST_SUITE_NAME, DEFAULT_WD_NAME, CONFIG_NAME);
		mainFrame = findMainFrame();
		
		// wait for "Some tests in this folder have not been run" label
		new JTextFieldOperator(mainFrame, getExecResource("br.worst.3"));
	}
	
	public void startWithRunWorkdir() throws InterruptedException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
		startJavatest(TEST_SUITE_NAME, WD_RUN_NAME, CONFIG_NAME);
		mainFrame = findMainFrame();
		new JTextFieldOperator(mainFrame, "There are some failed tests in this folder");
	}

	public static JDialogOperator openConfigurationEditor(JFrameOperator mainFrame) {
		new JMenuOperator(mainFrame, "Configure").pushMenu("Configure|Edit Configuration...", "|");
		return new JDialogOperator(mainFrame, "Configuration Editor");
	}

	public static JDialogOperator openFilterEditor(JFrameOperator frame) {
		new JMenuOperator(frame, "View").pushMenu("View|Filter|Configure Filters ...", "|");
		return new JDialogOperator(frame, "Filter Editor");
	}
	
	public static void selectFilter(JDialogOperator filterEditor, int filter) {
		new JListOperator(filterEditor, new NameComponentChooser("fconfig.list")).clickOnItem(filter, 1);
	}
	
	public static void selectFilter(JFrameOperator frame, String filter) {
		new JMenuOperator(frame, "View").pushMenu("View|Filter|" + filter, "|");
	}
	
	public static void chooseTab(ContainerOperator mainFrame, String tab) {
		new JTabbedPaneOperator(mainFrame).selectPage(tab);
	}

	public static void setKeywordFilter(JFrameOperator mainFrame, int type, String value) {

		JDialogOperator filterEditor = openFilterEditor(mainFrame);

		selectFilter(filterEditor, 3);

		chooseTab(filterEditor, "Keywords");

		new JRadioButtonOperator(filterEditor, "Match").push();

		new JComboBoxOperator(filterEditor).selectItem(type);

		new JTextFieldOperator(filterEditor, new NameComponentChooser("basicTf.keywords.field")).enterText(value);
		
	}

	public static void disableKeywordFilter(JFrameOperator mainFrame) {

		JDialogOperator filterEditor = openFilterEditor(mainFrame);

		selectFilter(filterEditor, 4);

		chooseTab(filterEditor, "Keywords");

		new JRadioButtonOperator(filterEditor, "All Tests").push();

	}

	public static void setPrevStateFilter(JFrameOperator mainFrame, String prevState) {

		JDialogOperator filterEditor = openFilterEditor(mainFrame);

		selectFilter(filterEditor, 3);

		chooseTab(filterEditor, "Prior Status");

		new JRadioButtonOperator(filterEditor, "Any Of"). setSelected(true);

		new JCheckBoxOperator(filterEditor, "Passed").setSelected(false);

		new JCheckBoxOperator(filterEditor, "Failed").setSelected(false);

		new JCheckBoxOperator(filterEditor, "Error").setSelected(false);

		new JCheckBoxOperator(filterEditor, "Not Run").setSelected(false);

		new JCheckBoxOperator(filterEditor, prevState).setSelected(true);

		ok(filterEditor);

		selectFilter(mainFrame, "Custom");
                
                pause(5);
	}

	protected JFrameOperator mainFrame;
	
}
