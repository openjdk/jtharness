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
/*
 * To change this template, choose Browse | Templates
 * and open the template in the editor.
 */
package jthtest.New;

import java.io.File;
import javax.swing.JTextField;
import jthtest.ConfigTools;
import jthtest.Tools;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.operators.DialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author naryl
 */
public class New10_1 extends New {

	public static void main(String[] args) {
		JUnitCore.main("jthtest.gui.New.New10");
	}

	@Test
	public void testNew10() throws InterruptedException {
		// known problem - cr6957366

		if (true)
			return;

		startTestRun(quickStartDialog);

		next(quickStartDialog);

		pickDefaultTestsuite(quickStartDialog);

		next(quickStartDialog);

		useConfigTemplate(quickStartDialog);

		next(quickStartDialog);

		pickTempWorkDir(quickStartDialog);

		next(quickStartDialog);

		finish(quickStartDialog, false, true);

		JDialogOperator create = new JDialogOperator("Configuration Required");
		new JButtonOperator(create, "OK").push();

		JDialogOperator config = ConfigTools.findConfigEditor(mainFrame);
		ConfigTools.pushDoneConfigEditor(config);
		JDialogOperator save = new JDialogOperator("Save Configuration File");
		JTextFieldOperator path =
				new JTextFieldOperator(Tools.<JTextField>getComponentPar(save, new String[]{"File Name:", "Folder Name:", "File name:", "Folder name:"}));
		File f = new File(Tools.LOCAL_PATH + File.separator + "temp_configuration_file_that_will_be_deleted.jti");
		if (f.exists()) {
			if (f.isDirectory()) {
				deleteDirectory(f);
			} else {
				f.delete();
			}
		}

		path.typeText(f.getAbsolutePath());
		try {
			new JTextFieldOperator(mainFrame, "Finished test run.");

			checkCounters(mainFrame, new int[]{16, 1, 0, 0, 17, 0, 17});

		} finally {
			f.deleteOnExit();
		}
	}
}
