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

import jthtest.Test;
import jthtest.Tools;
import jthtest.tools.ConfigDialog;
import jthtest.tools.Configuration;
import jthtest.tools.JTFrame;

/**
 *
 * @author linfar
 */
public class Config_Edit06 extends Test {

    public void testImpl() throws Exception {
    mainFrame = new JTFrame(true);

    mainFrame.openDefaultTestSuite();
    addUsedFile(mainFrame.createWorkDirectoryInTemp());

    Configuration configuration = mainFrame.getConfiguration();
    configuration.load(Tools.CONFIG_NAME, false);

    ConfigDialog config = configuration.openByKey();
    config.pushDoneConfigEditor();
    if (config.getConfigDialog().isVisible()) {
        errors.add("Config editor is not closed");
    }

//    startJavatestNewDesktop();
//
//    JFrameOperator mainFrame = findMainFrame();
//
//    openTestSuite(mainFrame);
//    createWorkDirInTemp(mainFrame);
//    openConfigFile(openLoadConfigDialogByMenu(mainFrame), DEFAULT_JTI);
//    waitForConfigurationLoading(mainFrame, DEFAULT_JTI);
//
//    openConfigDialogByKey(mainFrame);
//    JDialogOperator config = findConfigEditor(mainFrame);
//    pushDoneConfigEditor(config);
//    if(config.isVisible())
//        throw new JemmyException("Config editor is not closed");
    }

    @Override
    public String getDescription() {
    /*
     * Start JavaTest with the -NewDesktop option. Create a new workdirectory.
     * Bring up Load confiugration under Configure menu. Bring up Bring up
     * configuration editor by doing Ctrl-E. Click on Done button. It will dismiss
     * the editor dialog box. The Done button should dismiss the editor dialog box.
     */
    return "Start JavaTest with the -NewDesktop option. Create a new workdirectory. Bring up Load confiugration under Configure menu. Bring up Bring up configuration editor by doing Ctrl-E. Click on Done button. It will dismiss the editor dialog box. The Done button should dismiss the editor dialog box. ";
    }
}
