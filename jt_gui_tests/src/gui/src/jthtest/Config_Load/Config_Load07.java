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
package jthtest.Config_Load;

import jthtest.ConfigTools;
import jthtest.Test;
import jthtest.tools.ConfigDialog;
import jthtest.tools.Configuration;
import jthtest.tools.JTFrame;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 *
 * @author linfar
 */
public class Config_Load07 extends Test {
//    JFrameOperator mainFrame;

    public void testImpl() throws Exception {
	JTFrame mainFrame = new JTFrame(true);

	mainFrame.openDefaultTestSuite();
	addUsedFile(mainFrame.createWorkDirectoryInTemp());

	Configuration configuration = mainFrame.getConfiguration();
	configuration.load(ConfigTools.CONFIG_NAME, true);

	ConfigDialog config = configuration.openByKey();
	config.load(ConfigTools.SECOND_CONFIG_NAME, true);

	verifyOpeningNewConfigFile(config);

//	startJavatestNewDesktop();
//
//	mainFrame = findMainFrame();
//
//	closeQS(mainFrame);
//
//	openTestSuite(mainFrame);
//
//	createWorkDirInTemp(mainFrame);
//
//	JDialogOperator fileChooser = openLoadConfigDialogByMenu(mainFrame);
//	openConfigFile(fileChooser, DEFAULT_JTI);
//	Config_Edit.waitForConfigurationLoading(mainFrame, DEFAULT_JTI);
//	openConfigDialogByKey(mainFrame);
//
//	fileChooser = openLoadConfigDialogByMenu(mainFrame);
//	openConfigFile(fileChooser, SECOND_JTI);
//	Config_Edit.waitForConfigurationLoading(mainFrame, SECOND_JTI);
//	openConfigDialogByKey(mainFrame);
//
//	verifyOpeningNewConfigFile();
    }

//    private boolean verifyOpeningNewConfigFile() {
    private boolean verifyOpeningNewConfigFile(ConfigDialog config) {
	JListOperator list = new JListOperator(config.getConfigDialog());
	list.selectItem(1);
	return new JTextFieldOperator(config.getConfigDialog(), new NameComponentChooser("str.txt")).getText().equals(ConfigTools.SECOND_CONFIG_NAME);
    }

    @Override
    public String getDescription() {
	return "Load a configuration, open Configuration Editor. Load another configuration from it. Configuration Editor internals should be repainted";
    }
}
