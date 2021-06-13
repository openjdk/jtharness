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
 * Start JavaTest with -newdesktop. The JavaTest Quick Start wizard will be displayed.
 * Click on Start a new test run. Click on Next button. Specify an existing testsuite.
 * Click on Next button. Click on Use an configuration Template. Specify an exiting
 * template file and click on Next button. Click on Next button. Specify a new
 * workdirectory and click on Next button. Uncheck the Start the Configuration editor,
 * check the Start test run and click on Finish button. The tests will be run in
 * JavaTest. Verify the correct numbers of pass/fail/not run in the right panel
 */
package jthtest.New;

import java.io.File;
import javax.swing.JTextField;
import jthtest.Test;
import jthtest.Tools;
import jthtest.tools.ConfigDialog;
import jthtest.tools.JTFrame;
import jthtest.tools.QSWizard;
import jthtest.tools.QSWizard.ChooseConfigurationPanel;
import jthtest.tools.QSWizard.ChooseTestSuitePanel;
import jthtest.tools.QSWizard.ChooseWorkDirectoryPanel;
import jthtest.tools.QSWizard.NewTestsuiteAlmostDone;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author naryl
 */
public class New10 extends Test {

    public void testImpl() throws Exception {
        JTFrame.closeQSOnOpen = false;
        mainFrame = new JTFrame(true);

        QSWizard qs = new QSWizard();
        QSWizard.GreetingsPanel gp = (QSWizard.GreetingsPanel) qs.getPanel();
        gp.setStartNewTestRun();
        ChooseTestSuitePanel ctsp = gp.pushNext();

        ctsp.setTestsuite(TEST_SUITE_NAME);
        ChooseConfigurationPanel ccp = ctsp.pushNext();

        ccp.setUseAConfigurationTemplate();
        ccp.setPath(TEMPLATE_NAME);
        ChooseWorkDirectoryPanel cwdp = ccp.pushNext();

        addUsedFile(LOCAL_PATH + TEMP_WD_NAME);
        cwdp.setWorkDirectory(LOCAL_PATH + TEMP_WD_NAME);
        NewTestsuiteAlmostDone ntad = cwdp.pushNext();

        ntad.setStartTestRun(true);
        ntad.setStartTheConfigEditor(false);
        ntad.pushNext();

        JDialogOperator create = new JDialogOperator("Configuration Required");
        new JButtonOperator(create, "OK").push();

        ConfigDialog cd = mainFrame.getConfiguration().findConfigEditorDialog();
        cd.pushLastConfigEditor();
        cd.pushDoneConfigEditor();

        JDialogOperator save = new JDialogOperator("Save Configuration File");
        JTextFieldOperator path =
                new JTextFieldOperator(Tools.<JTextField>getComponentPar(save, new String[]{"File Name:", "Folder Name:", "File name:", "Folder name:"}));
        File f = new File(LOCAL_PATH + File.separator + "temp_configuration_file_that_will_be_deleted.jti");
        if (f.exists()) {
            if (f.isDirectory()) {
                Tools.deleteDirectory(f);
            } else {
                f.delete();
            }
        }

        addUsedFile(f);
        path.enterText(f.getAbsolutePath());

        mainFrame.waitForExecutionDone();
        int[] expectedCounters = new int[]{16, 1, 0, 0, 17, 0, 17};
        mainFrame.waitForCounters(expectedCounters);

//        startTestRun(quickStartDialog);
//
//    next(quickStartDialog);
//
//    pickDefaultTestsuite(quickStartDialog);
//
//    next(quickStartDialog);
//
//    useConfigTemplate(quickStartDialog);
//
//    next(quickStartDialog);
//
//    pickTempWorkDir(quickStartDialog);
//
//    next(quickStartDialog);
//
//    finish(quickStartDialog, false, true);
//
//    JDialogOperator create = new JDialogOperator("Configuration Required");
//    new JButtonOperator(create, "OK").push();
//
//    JDialogOperator config = ConfigTools.findConfigEditor(mainFrame);
//    ConfigTools.pushLastConfigEditor(config);
//    ConfigTools.pushDoneConfigEditor(config);
//    JDialogOperator save = new JDialogOperator("Save Configuration File");
//    JTextFieldOperator path =
//        new JTextFieldOperator(Tools.<JTextField>getComponentPar(save, new String[]{"File Name:", "Folder Name:", "File name:", "Folder name:"}));
//    File f = new File(Tools.LOCAL_PATH + File.separator + "temp_configuration_file_that_will_be_deleted.jti");
//    if (f.exists()) {
//        if (f.isDirectory()) {
//        deleteDirectory(f);
//        } else {
//        f.delete();
//        }
//    }
//
//    path.enterText(f.getAbsolutePath());
//
//    try {
//        new JTextFieldOperator(mainFrame, "Finished test run.");
//        pause(2);
//
//        checkCounters(mainFrame, new int[]{16, 1, 0, 0, 17, 0, 17});
//    } finally {
//        f.deleteOnExit();
//    }
    }

    @Override
    public String getDescription() {
        return "Start JavaTest with -newdesktop. The JavaTest Quick Start wizard will be displayed. Click on Start a new test run. Click on Next button. Specify an existing testsuite. Click on Next button. Click on Use an configuration Template. Specify an exiting template file and click on Next button. Click on Next button. Specify a new workdirectory and click on Next button. Uncheck the Start the Configuration editor, check the Start test run and click on Finish button. The tests will be run in JavaTest. Verify the correct numbers of pass/fail/not run in the right panel";
    }
}
