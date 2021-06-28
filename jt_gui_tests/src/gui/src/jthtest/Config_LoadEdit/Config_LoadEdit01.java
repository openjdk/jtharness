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

import jthtest.Test;
import jthtest.Tools;
import jthtest.tools.ConfigDialog;
import jthtest.tools.Configuration;
import jthtest.tools.JTFrame;

/**
 *
 * @author linfar
 */
public class Config_LoadEdit01 extends Test {

    public void testImpl() throws Exception {
    JTFrame mainFrame = new JTFrame(true);

    mainFrame.openDefaultTestSuite();
    addUsedFile(mainFrame.createWorkDirectoryInTemp());

    Configuration configuration = mainFrame.getConfiguration();
    configuration.load(Tools.CONFIG_NAME, true);

    ConfigDialog configDialog = configuration.openByKey();
    configDialog.openBrowser(true);

//    startJavatestNewDesktop();
//
//    JFrameOperator mainFrame = findMainFrame();
//
//    closeQS(mainFrame);
//
//    openTestSuite(mainFrame);
//    createWorkDirInTemp(mainFrame);
//    openConfigFile(openLoadConfigDialogByMenu(mainFrame), DEFAULT_JTI);
//
//    Config_Edit.waitForConfigurationLoading(mainFrame, DEFAULT_JTI);
//    openConfigDialogByKey(mainFrame);
//    findConfigEditor(mainFrame);
//
//    openLoadConfigDialogByMenu(mainFrame);
    }

    @Override
    public String getDescription() {
    /*
     * Start JavaTest with the -NewDesktop option. Create an new workdirectory.
     * Bring up Load configuration under Configure menu. Bring up Edit
     * configuration by doing Ctrl-E. Bring Load under file menu. A file chooser
     * will be displayed to select a jti file to be used. Click on Cancel button
     * to dismiss the dialog box. A file chooser should be displayed for loading
     * an existing jti file.
     */
    return "Start JavaTest with the -NewDesktop option. Create an new workdirectory. Bring up Load configuration under Configure menu. Bring up Edit configuration by doing Ctrl-E. Bring Load under file menu. A file chooser will be displayed to select a jti file to be used. Click on Cancel button to dismiss the dialog box. A file chooser should be displayed for loading an existing jti file.";
    }
}
