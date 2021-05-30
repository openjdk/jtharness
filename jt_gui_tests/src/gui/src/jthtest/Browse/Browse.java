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

package jthtest.Browse;

import java.lang.reflect.InvocationTargetException;

import jthtest.CreateWorkdir.CreateWorkdir;
import org.junit.Before;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JMenuOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;

import jthtest.Tools;

/**
 *
 * @author naryl
 */
public class Browse extends Tools {
	static void browseTestsuite(JDialogOperator quickStartDialog) {
		//Click on the "Browse" radio button
		new JRadioButtonOperator(quickStartDialog, "Browse the test suite").push();
	}
	
	public static void pickWorkDir(JFrameOperator mainFrame) {
		
//		JDialogOperator wrkDir = new JDialogOperator(mainFrame, "Work Directory Required");
		
//		new JButtonOperator(wrkDir, "Create Work Directory").push();

	    JDialogOperator wd = CreateWorkdir.findWorkDir(mainFrame);
	    CreateWorkdir.createWorkDirInTemp(CreateWorkdir.findWorkDirectoryCreation(mainFrame), true);
	}

	protected JFrameOperator mainFrame;
	protected JDialogOperator quickStartDialog;
	
	@Before
	public void setUp() throws InterruptedException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
		startJavatest(NEWDESKTOP_ARG);
		mainFrame = findMainFrame();
//		new JMenuOperator(mainFrame).pushMenuNoBlock("File|Open Quick Start Wizard ...", "|");
		quickStartDialog = findQuickStart(mainFrame);
	}
	
}
