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

import jthtest.Test;
import jthtest.Tools;
import jthtest.tools.ConfigDialog;
import jthtest.tools.Configuration;
import jthtest.tools.JTFrame;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 *
 * @author linfar
 */
public class Config_Load05 extends Test {

//    JFrameOperator mainFrame;
//    JDialogOperator config;
    public void testImpl() throws Exception {

    mainFrame = new JTFrame(true);

    mainFrame.openDefaultTestSuite();
    addUsedFile(mainFrame.createWorkDirectoryInTemp());

    Configuration configuration = mainFrame.getConfiguration();
    configuration.load(Tools.CONFIG_NAME, true);

    ConfigDialog config = configuration.openByKey();
    makeChange(config);

    config.load(Tools.CONFIG_NAME, true);

    JDialogOperator warningDialog = new JDialogOperator(mainFrame.getJFrameOperator(), "Warning: Unsaved Changes");
    if (!warningDialog.isVisible()) {
        throw new JemmyException("Dialog is not visible");
    }
    new JButtonOperator(warningDialog, "Yes").push();

    restoreChange(config);
    config.pushDoneConfigEditor();

//    try {
//        startJavatestNewDesktop();
//
//        mainFrame = findMainFrame();
//
//        openTestSuite(mainFrame);
//
//        createWorkDirInTemp(mainFrame);
//
//        openConfigFile(openLoadConfigDialogByMenu(mainFrame), DEFAULT_JTI);
//        Config_Edit.waitForConfigurationLoading(mainFrame, DEFAULT_JTI);
//
//        openConfigDialogByKey(mainFrame);
//        config = findConfigEditor(mainFrame);
//        makeChange();
//
//        openConfigFile(openLoadConfigDialogByMenu(mainFrame), DEFAULT_JTI);
//
//        JDialogOperator warningDialog = new JDialogOperator(mainFrame, "Warning: Unsaved Changes");
//        if (!warningDialog.isVisible()) {
//        throw new JemmyException("Dialog is not visible");
//        }
//        new JButtonOperator(warningDialog, "Yes").push();
//
//        openConfigDialogByMenu(mainFrame);
//        config = findConfigEditor(mainFrame);
//        restoreChange();
//        pushDoneConfigEditor(config);
//    } finally {
//    }
    }

//    private void makeChange() {
    private void makeChange(ConfigDialog config) {
    JListOperator list = new JListOperator(config.getConfigDialog());
    list.selectItem(1);
    new JTextFieldOperator(config.getConfigDialog(), new NameComponentChooser("str.txt")).setText("changed democonfig");
    }

//    private void restoreChange() {
    private void restoreChange(ConfigDialog config) {
    JListOperator list = new JListOperator(config.getConfigDialog());
    list.selectItem(1);
    JTextFieldOperator text = new JTextFieldOperator(config.getConfigDialog(), new NameComponentChooser("str.txt"));
    if (!text.getText().equals("changed democonfig")) {
        throw new JemmyException("Config file wasn't saved");
    }
    text.setText("democonfig");
    }
}
