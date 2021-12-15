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

package jthtest.Config_Edit;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JListOperator;

public class Config_Edit4 extends Config_Edit {
	public static void main(String args[]) {
		JUnitCore.main("jthtest.gui.Config_Edit.Config_Edit4");
	}

	@Test
	public void test_next_button_config_editor_next_question()
			throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {

		// start javatest with -newDesktop option
		startJavatestNewDesktop();
		JFrameOperator mainFrame = findMainFrame();

		// Open test suite and load configuration
		openTestSuite(mainFrame);
		createWorkDirInTemp(mainFrame);
		openConfigFile(openLoadConfigDialogByMenu(mainFrame), CONFIG_NAME);
		waitForConfigurationLoading(mainFrame, CONFIG_NAME);

		openConfigDialogByKey(mainFrame);
		JDialogOperator config = findConfigEditor(mainFrame);

		// push next button on configuration editor
		pushNextConfigEditor(config);

		// verify that Next button in the configuration editor will go forward to the
		// next question
		if (!new JListOperator(config).isSelectedIndex(2))
			throw new JemmyException("After next button pushing list selection is not on third page");
	}

	// TestCase Description
	public String getDescription() {
		return "This test case verifies that Next button in the configuration editor will go forward to the next question.";
	}
}
