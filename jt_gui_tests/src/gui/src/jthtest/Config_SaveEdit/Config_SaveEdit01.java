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
/*
 * Load a configuration file, open Configuration Editor. Change value in description 
 * and save configuration. Close WD, reopen it, value should be changed
 */
package jthtest.Config_SaveEdit;

import java.io.File;
import jthtest.Test;
import jthtest.Tools;
import jthtest.tools.ConfigDialog;
import jthtest.tools.Configuration;
import jthtest.tools.JTFrame;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 *
 * @author linfar
 */
public class Config_SaveEdit01 extends Test {

	public void testImpl() throws Exception {
		mainFrame = new JTFrame(true);

		mainFrame.openDefaultTestSuite();
		File createdWD = mainFrame.createWorkDirectoryInTemp();
		addUsedFile(createdWD);

		Configuration configuration = mainFrame.getConfiguration();
		configuration.load(Tools.CONFIG_NAME, true);

		ConfigDialog config = configuration.openByKey();
		config.selectQuestion(2);
		JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(config.getConfigDialog(), new NameComponentChooser("str.txt"));
		String save = jTextFieldOperator.getText();
		jTextFieldOperator.setText("some_description");
		config.getFile_SaveMenu().push();
		config.pushDoneConfigEditor();

		mainFrame.closeCurrentTool();

		mainFrame.getWorkDirectory().openWorkDirectory(createdWD);
		Tools.waitForWDLoading(mainFrame.getJFrameOperator(), Tools.WDLoadingResult.SOME_NOTRUN);

		config = configuration.openByKey();
		config.selectQuestion(2);
		jTextFieldOperator = new JTextFieldOperator(config.getConfigDialog(), new NameComponentChooser("str.txt"));
		if (!jTextFieldOperator.getText().equals("some_description")) {
			errors.add("description was not saved");
		}
		jTextFieldOperator.setText(save);
		config.pushDoneConfigEditor();
		mainFrame.closeAllTools();

//	startJavatestNewDesktop();
//	JFrameOperator mainFrame = findMainFrame();
//	closeQS(mainFrame);
//
//	openTestSuite(mainFrame);
//	createWorkDirInTemp(mainFrame);
//	openConfigFile(openLoadConfigDialogByMenu(mainFrame), DEFAULT_JTI);
//	Config_Edit.waitForConfigurationLoading(mainFrame, DEFAULT_JTI);
//
//	openConfigDialogByKey(mainFrame);
//	JDialogOperator config = findConfigEditor(mainFrame);
//
//	selectQuestion(config, 2); // description
//	JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(config, new NameComponentChooser("str.txt"));
//	String save = jTextFieldOperator.getText();
//	jTextFieldOperator.setText("some_description");
//	new JMenuOperator(config, "File").pushMenuNoBlock(new String[] {"File", "Save"});
//	pushDoneConfigEditor(config);
//
//	new JMenuOperator(mainFrame, "File").pushMenu(new String[] {"File", "Close"});
//	CreateWorkdir.createWorkDir(CreateWorkdir.openWorkDirectoryOpening(mainFrame), TEMP_PATH, TEMP_WD, false);
////	openWorkDirectory(mainFrame, TEMP_PATH + TEMP_WD);
//	waitForWDLoading(mainFrame, WDLoadingResult.SOME_NOTRUN);
//
//	openConfigDialogByKey(mainFrame);
//	config = findConfigEditor(mainFrame);
//
//	selectQuestion(config, 2);
//	jTextFieldOperator = new JTextFieldOperator(config, new NameComponentChooser("str.txt"));
//	if(!jTextFieldOperator.getText().equals("some_description"))
//	    throw new JemmyException("description was not saved");
//	jTextFieldOperator.setText(save);
//	pushDoneConfigEditor(config);
	}

	@Override
	public String getDescription() {
		return "Load a configuration file, open Configuration Editor. Change value in description and save configuration. Close WD, reopen it, value should be changed.";
	}
}
