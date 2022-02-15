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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

import jthtest.menu.Menu;

public class Test_Config_Save1 extends Config_New {

    public static void main(String[] args) {
        JUnitCore.main("jthtest.gui.Sanity_Tests.Test_Config_Save1");
    }

    // Below fill question function is modified for this test case please be careful
    // while using it.
    public void fillquestion(JDialogOperator config, int question_num) throws FileNotFoundException {
        if (question_num == 1) {
            return;
        } else if (question_num == 2) {
            ComponentChooser n = new NameComponentChooser("str.txt");
            JTextFieldOperator test = new JTextFieldOperator(config, n);
            test.clearText();
            test.typeText("test11_name");
        } else if (question_num == 3) {
            ComponentChooser n = new NameComponentChooser("str.txt");
            JTextFieldOperator test = new JTextFieldOperator(config, n);
            test.clearText();
            test.typeText("test11_desc");
        } else if (question_num == 4) {
            JTableOperator test_table = new JTableOperator(config);
            test_table.clickOnCell(0, 0);
        } else if (question_num == 5) {
            String os = System.getProperty("os.name");
            System.out.println(os);
            boolean iswindows = false;
            if (os.startsWith("Windows")) {
                iswindows = true;
            }
            ComponentChooser n = new NameComponentChooser("file.txt");
            JTextFieldOperator test = new JTextFieldOperator(config, n);
            test.clearText();
            Path p = Paths.get(System.getenv("JAVA_HOME"));
            Path p1;
            if (iswindows) {
                p1 = Paths.get(p.toString(), "bin", "java.exe");
            } else {
                p1 = Paths.get(p.toString(), "bin", "java");
            }

            test.typeText(p1.toString());
        } else if (question_num == 6) {
            JTableOperator test_table = new JTableOperator(config);
            test_table.clickOnCell(1, 0);
            // Dumper.dumpAll( "C:\\Users\\arpit\\Desktop\\test11_2.txt" );
        } else {
            fail("Failure due to wrong question number");
        }
        return;
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

    // Function to check the response in the current question.
    public boolean checkresponse(JDialogOperator config, int question_num) throws FileNotFoundException, IOException {
        if (question_num == 1) {
            return true;
        } else if (question_num == 2) {
            ComponentChooser n = new NameComponentChooser("str.txt");
            JTextFieldOperator test = new JTextFieldOperator(config, n);
            if (test.getText().equals("test11_name")) {
                return true;
            }
        } else if (question_num == 3) {
            ComponentChooser n = new NameComponentChooser("str.txt");
            JTextFieldOperator test = new JTextFieldOperator(config, n);
            // System.out.println(test.getText());
            if (test.getText().equals("test11_desc")) {
                return true;
            }
        } else if (question_num == 4 || question_num == 5) {
            return true;
        } else if (question_num == 6) {
            // read 19th line as it contains information on test verbose question.
            String s = Files.readAllLines(Paths.get("democonfig.jti")).get(19);
            // split the string at = sign.
            String[] tokens = s.split("=");
            // compare with string you set in the first half of the code.
            if (tokens[1].equals("medium")) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void test11() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException,
            FileNotFoundException, InterruptedException, IOException {
        // Opening work directory.
        JMenuBarOperator jmbo = new JMenuBarOperator(mainFrame);
        jmbo.pushMenuNoBlock("File", "/");
        Menu.getFile_Open_WorkDirectoryMenu(mainFrame).pushNoBlock();

        JDialogOperator openDialog = new JDialogOperator(getToolResource("wdc.open.title"));
        // Find edit box for file name
        String dirname = "demowd";
        new JTextFieldOperator(openDialog, "").enterText(dirname); // for build.xml use "demowd" else use "temp_dir".

        // Checking if open dialog box disappeared or not.
        if (openDialog.isVisible()) {
            fail("Failure because the dialog window 'open work directory' didnt close properly.");
        }

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
        for (int i = 2; i < 7; i++) {
            if (!checkquestiontitle(configEditorDialog, i)) {
                fail("Failure because question number " + i + " has mismatching titles.");
            }
            fillquestion(configEditorDialog, i);
            pushNextConfigEditor(configEditorDialog);
        }

        // Saving current changes.
        JMenuBarOperator jmbo1 = new JMenuBarOperator(configEditorDialog);
        jmbo1.pushMenuNoBlock("File", "/");
        jmbo1.pushMenuNoBlock("File/Save", "/");

        pushDoneConfigEditor(configEditorDialog);

        if (configEditorDialog.isVisible()) {
            fail("Failure Because pressing done button didn't close the configuration editor window.");
        }

        jmbo.pushMenuNoBlock("Configure", "/");
        Menu.getConfigure_LoadConfigurationMenu(mainFrame).pushNoBlock();

        // Selecting load configuration dialog box.
        loadConfigDialog = new JDialogOperator(getExecResource("wdc.loadconfig"));

        // Pushing "Browse" Button for opening config finder dialog box.

        // Selecting Configuration finder dialog box and further Text area inside it.
        launch_config_checkbox = new JCheckBoxOperator(loadConfigDialog);

        launch_config_checkbox.clickMouse();
        new JButtonOperator(loadConfigDialog, "Browse").push();
        loadConfigfinderDialog = new JDialogOperator(getExecResource("wdc.configchoosertitle"));
        browsetext = new JTextFieldOperator(loadConfigfinderDialog);
        browsetext.clearText();
        browsetext.enterText("democonfig.jti");

        // Pressing load to load the selected file.
        loadbutton = new JButtonOperator(loadConfigDialog, getExecResource("wdc.load.btn"));
        loadbutton.push();

        // Selecting question one from the list of questions.
        configEditorDialog = new JDialogOperator(getExecResource("ce.name"));
        list = new JListOperator(configEditorDialog);
        list.selectItem(1);

        // Checking responses after reloading the config file.
        for (int i = 2; i < 7; i++) {
            if (!checkresponse(configEditorDialog, i)) {
                fail("Wrong response on question number " + i + ".");
            }
            pushNextConfigEditor(configEditorDialog);
        }
    }

}