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

package jthtest.New;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.junit.Before;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;

import jthtest.Tools;

/**
 *
 * @author naryl
 */
public class New extends Tools {
//	private static final String TEMP_WD = "New_temp_wd";

	public static void startTestRun(JDialogOperator dialog) {
		//Click on the "New" radio button
		new JRadioButtonOperator(dialog, "Start a new test run").push();
	}

	public static void pickTempWorkDir(JDialogOperator dialog) {

		deleteDirectory(new File(TEMP_WD_NAME));
		getTextField(dialog, "Work directory").typeText(TEMP_WD_NAME);

	}

	public static void pickExistingWorkDir(JDialogOperator dialog) {

		getTextField(dialog, "Work directory").typeText(TEMP_WD_NAME);

	}

	public static void pickWorkDirWithConfiguration(JDialogOperator dialog) {
		getTextField(dialog, "Work directory").typeText(DEFAULT_WD_NAME);
	}

	public static void done(JDialogOperator dialog) {
		new JButtonOperator(dialog, "Done").push();
	}

	protected JFrameOperator mainFrame;
	protected JDialogOperator quickStartDialog;
	
	@Before
	public void setUp() throws InterruptedException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
		startJavatest(NEWDESKTOP_ARG);
		mainFrame = findMainFrame();
		quickStartDialog = openQuickStart(mainFrame);
	}
	
}
