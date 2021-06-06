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
 * Start JavaTest with the -NewDesktop option. Create a new workdirectory.
 * Bring up Load confiugration under Configure menu. Bring up
 * configuration editor by doing Ctrl-E. The Back button is grayed out. Click
 * on Next button. It will navigate to the next question. Click on Back button.
 * It should navigate to the previous question. The Back button navigates back
 * an forth between questions.
 */
package jthtest.Config_Edit;

import java.lang.reflect.InvocationTargetException;
import jthtest.Test;
import jthtest.tools.ConfigDialog;
import jthtest.tools.Configuration;
import jthtest.tools.JTFrame;

/**
 *
 * @author linfar
 */
public class Config_Edit3 extends Test {

    public void testImpl() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        mainFrame = new JTFrame(true);

        mainFrame.openDefaultTestSuite();
        addUsedFile(mainFrame.createWorkDirectoryInTemp());
        Configuration conf = mainFrame.getConfiguration();
        conf.load(CONFIG_NAME, true);

        ConfigDialog cd = conf.openByKey();
        cd.pushBackConfigEditor();
        if (cd.getBackButton().isEnabled()) {
            errors.add("Back button is enabled while unexpected");
        }
        cd.pushNextConfigEditor();
        cd.pushBackConfigEditor();
        if (!cd.isSelectedIndex(0)) {
            errors.add("After back button pushing list selection is not on first question");
        }

//    startJavatestNewDesktop();
//
//    JFrameOperator mainFrame = findMainFrame();
//
//    openTestSuite(mainFrame);
//    createWorkDirInTemp(mainFrame);
//    openConfigFile(openLoadConfigDialogByMenu(mainFrame), CONFIG_NAME);
//    waitForConfigurationLoading(mainFrame, CONFIG_NAME);
//
//    openConfigDialogByKey(mainFrame);
//    JDialogOperator config = findConfigEditor(mainFrame);
//
//    JButtonOperator previous = new JButtonOperator(config, "Back");
//    previous.push();
//    if(previous.isEnabled())
//        throw new JemmyException("Back button is enabled while unexpected");
//    pushNextConfigEditor(config);
//    previous.push();
//    if(!new JListOperator(config).isSelectedIndex(0))
//        throw new JemmyException("After back button pushing list selection is not on first page");
    }

    @Override
    public String getDescription() {
        return "Start JavaTest with the -NewDesktop option. Create a new workdirectory. Bring up Load confiugration under Configure menu. Bring up configuration editor by doing Ctrl-E. The Back button is grayed out. Click on Next button. It will navigate to the next question. Click on Back button. It should navigate to the previous question. The Back button navigates back an forth between questions.";
    }
}
