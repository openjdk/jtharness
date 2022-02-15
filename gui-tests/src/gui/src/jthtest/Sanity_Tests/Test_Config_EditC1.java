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

import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

import jthtest.menu.Menu;

public class Test_Config_EditC1 extends Config_New {

    public static void main(String[] args) {
        JUnitCore.main("jthtest.gui.Sanity_Tests.Test_Config_Edit1C");
    }

    // Function for checking title of current question.
    public boolean checkquestiontitle(JDialogOperator config, int question_num) {
        ComponentChooser n = new NameComponentChooser("qu.title");
        JTextFieldOperator test = new JTextFieldOperator(config, n);
        // System.out.println(test.getText());
        if (question_num == 1) {
            if (test.getText().equals("Welcome!")) {
                return true;
            }
        } else if (question_num == 2) {
            if (test.getText().equals("Configuration Name")) {
                return true;
            }
        } else if (question_num == 3) {
            if (test.getText().equals("Description")) {
                return true;
            }
        } else if (question_num == 4) {
            if (test.getText().equals("How to Run Tests")) {
                return true;
            }
        } else if (question_num == 5) {
            if (test.getText().equals("Java Virtual Machine")) {
                return true;
            }
        } else if (question_num == 6) {
            if (test.getText().equals("Test Verboseness")) {
                return true;
            }
        } else {
            fail("Failure due to wrong question number");
        }
        return false;
    }

    @Test
    public void test8() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException,
            FileNotFoundException, InterruptedException {

        // Opening work directory.
        JMenuBarOperator jmbo = new JMenuBarOperator(mainFrame);
        jmbo.pushMenuNoBlock("File", "/");
        Menu.getFile_Open_WorkDirectoryMenu(mainFrame).pushNoBlock();

        JDialogOperator openDialog = new JDialogOperator(getToolResource("wdc.open.title"));
        // Find edit box for file name
        String dirname = "demowd";
        new JTextFieldOperator(openDialog, "").enterText(dirname);

        // Checking if open dialog box disappeared or not.
        if (openDialog.isVisible()) {
            fail("Failure because the dialog window 'open work directory' didnt close properly.");
        }

        // Pressing Configuration->Edit configuration.
        jmbo.pushMenuNoBlock("Configure", "/");
        Menu.getConfigure_LoadConfigurationMenu(mainFrame).pushNoBlock();

        // Selecting load configuration dialog box.
        JDialogOperator loadConfigDialog = new JDialogOperator(getExecResource("wdc.loadconfig"));

        // Pushing "Browse" Button for opening config finder dialog box.
        // Selecting Configuration finder dialog box and further Text area inside it.
        JCheckBoxOperator launch_config_checkbox = new JCheckBoxOperator(loadConfigDialog);

        launch_config_checkbox.clickMouse();
        new JButtonOperator(loadConfigDialog, "Browse").push();
        JDialogOperator loadConfigfinderDialog = new JDialogOperator(getExecResource("wdc.configchoosertitle"));
        JTextFieldOperator browsetext = new JTextFieldOperator(loadConfigfinderDialog);
        browsetext.clearText();
        browsetext.enterText("democonfig.jti");

        // Pressing load to load the selected file.
        JButtonOperator loadbutton = new JButtonOperator(loadConfigDialog, getExecResource("wdc.load.btn"));
        loadbutton.push();

        // Selecting question one from the list of questions.
        JDialogOperator configEditorDialog = new JDialogOperator(getExecResource("ce.name"));
        JListOperator list = new JListOperator(configEditorDialog);
        list.selectItem(1);

        // Checking and filling configuration.
        for (int i = 2; i < 6; i++) {
            if (!checkquestiontitle(configEditorDialog, i)) {
                fail("Failure because question number " + i + " has mismatching titles.");
            }
            // fillquestion(configEditorDialog,i);
            pushNextConfigEditor(configEditorDialog);
        }

    }

}