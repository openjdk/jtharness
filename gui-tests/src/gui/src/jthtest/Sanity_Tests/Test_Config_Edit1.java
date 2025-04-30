/*
 * $Id$
 *
 * Copyright (c) 2001, 2022, Oracle and/or its affiliates. All rights reserved.
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

package jthtest.Sanity_Tests;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

import jthtest.Config_Edit.Config_Edit;
import jthtest.menu.Menu;

public class Test_Config_Edit1 extends Config_Edit {
    public static void main(String args[]) {
        JUnitCore.main("jthtest.gui.Sanity_Tests.Test_Config_Edit1");
    }

    /**
     * This test case verifies that Next button in the configuration editor will go
     * forward to the next question.
     *
     * @throws ClassNotFoundException
     *
     * @throws InvocationTargetException
     *
     * @throws NoSuchMethodException
     */
    @Test
    public void test_next_button_config_editor_next_question()
            throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        int interview_question_num = 1;
        startJavatestNewDesktop();
        JFrameOperator mainFrame = findMainFrame();

        openTestSuite(mainFrame);
        createWorkDirInTemp(mainFrame);
        openConfigFile(openLoadConfigDialogByMenu(mainFrame), CONFIG_NAME);
        waitForConfigurationLoading(mainFrame, CONFIG_NAME);

        JMenuBarOperator jmbo = new JMenuBarOperator(mainFrame);
        jmbo.pushMenuNoBlock("Configure", "/");
        Menu.getConfigure_NewConfigurationMenu(mainFrame).pushNoBlock();
        JDialogOperator config = findConfigEditor(mainFrame);

        JDialogOperator configEditorDialog = new JDialogOperator(getExecResource("ce.name"));

        while (interview_question_num <= 6) {
            fillInterviewQuestion(configEditorDialog, interview_question_num);
            pushNextConfigEditor(configEditorDialog);
            interview_question_num++;
        }

        if (!new JListOperator(config).isSelectedIndex(new JListOperator(config).getSelectedIndex()))
            throw new JemmyException("After next button pushing list selection is not on proper page");
    }

    public void fillInterviewQuestion(JDialogOperator config, int interview_question_num) {
        ComponentChooser n = new NameComponentChooser("str.txt");
        switch (interview_question_num) {
        case 1:
            return;
        case 2:
            JTextFieldOperator testName = new JTextFieldOperator(config, n);
            testName.typeText("test_name");
            break;
        case 3:
            JTextFieldOperator description = new JTextFieldOperator(config, n);
            description.typeText("test_desc");
            break;
        case 4:
            JRadioButtonOperator testTable = new JRadioButtonOperator(config);
            testTable.doClick();
            break;
        case 5:
            boolean isWindows = false;
            if (System.getProperty("os.name").startsWith("Windows")) {
                isWindows = true;
            }
            ComponentChooser fileText = new NameComponentChooser("file.txt");
            JTextFieldOperator testConfig = new JTextFieldOperator(config, fileText);
            testConfig.clearText();
            Path p;
            if (isWindows) {
                p = Paths.get(System.getProperty("java.home").toString(), "bin", "java.exe");
            } else {
                p = Paths.get(System.getProperty("java.home").toString(), "bin", "java");
            }
            testConfig.typeText(p.toString());
            break;
        default:
            return;
        }
    }
}
