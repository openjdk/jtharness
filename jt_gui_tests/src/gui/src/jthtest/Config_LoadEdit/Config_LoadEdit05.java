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
package jthtest.Config_LoadEdit;

import jthtest.ConfigTools;
import jthtest.Test;
import jthtest.Tools;
import jthtest.tools.ConfigDialog;
import jthtest.tools.Configuration;
import jthtest.tools.JTFrame;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 *
 * @author linfar
 */
public class Config_LoadEdit05 extends Test {

    public void testImpl() throws Exception {
    JTFrame mainFrame = new JTFrame(true);

    mainFrame.openDefaultTestSuite();
    addUsedFile(mainFrame.createWorkDirectoryInTemp());

    Configuration configuration = mainFrame.getConfiguration();
    configuration.load(Tools.CONFIG_NAME, true);

    ConfigDialog configDialog = configuration.openByKey();
    new JTextFieldOperator(configDialog.getConfigDialog(), new NameComponentChooser("str.txt")).typeText("some_change");

    configDialog.load(ConfigTools.SECOND_CONFIG_NAME, true);
    JDialogOperator warning = new JDialogOperator(Tools.getExecResource("ce.load.warn.title"));
    new JButtonOperator(warning, "Cancel").push();

//    startJavatestNewDesktop();
//
//    JFrameOperator mainFrame = findMainFrame();
//
//    closeQS(mainFrame);
//
//    openTestSuite(mainFrame);
//    createWorkDirInTemp(mainFrame);
//    openConfigFile(openLoadConfigDialogByMenu(mainFrame), DEFAULT_JTI);
//    Config_Edit.waitForConfigurationLoading(mainFrame, DEFAULT_JTI);
//
//    openConfigDialogByKey(mainFrame);
//    JDialogOperator config = findConfigEditor(mainFrame);
//    new JTextFieldOperator(config, new NameComponentChooser("str.txt")).typeText("some_change");
//
//    openConfigFile(openLoadConfigDialogByMenu(mainFrame), SECOND_JTI);
//    Config_Edit.waitForConfigurationLoading(mainFrame, SECOND_JTI);
//
//    JDialogOperator warning = new JDialogOperator(getExecResource("ch.edited.warn.title"));
//    new JButtonOperator(warning, "Cancel").push();
    }

    @Override
    public String getDescription() {
    /*
     * Start JavaTest with the -NewDesktop option. Create a new workdirectory.
     * Bring up Load configuration under Configure menu. Select an existing jti
     * file. Click on Load File. Bring up Edit configuration by doing Ctrl-E to
     * verify that the jti is loaded. Make one change to the config file. Bring
     * up Load configuration under file menu. A warning should be displayed to
     * ask if the changes need to be saved. Click on NO button. The new change
     * will be saved.
     */
    return "Start JavaTest with the -NewDesktop option. Create a new workdirectory. Bring up Load configuration under Configure menu. Select an existing jti file. Click on Load File. Bring up Edit configuration by doing Ctrl-E to verify that the jti is loaded. Make one change to the config file. Bring up Load configuration under file menu. A warning should be displayed to ask if the changes need to be saved. Click on NO button. The new change will be saved.";
    }
}
