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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.awt.Rectangle;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;

import javax.swing.JLabel;

import jthtest.menu.Menu;

public class Test_QSM_SaveB1 extends Config_New {

    public static void main(String[] args) {
        JUnitCore.main("jthtest.gui.Sanity_Tests.Test_QSM_Save1B");
    }

    // This function is taken from the jthtest.tools.configdialog class.
    public void clickOnCheckbox(JTreeOperator tree, int row) {
        Rectangle r = tree.getRowBounds(row);
        prepareToClickOnRow(tree, row);
        int x = Operator.getDefaultMouseButton();
        tree.clickMouse((int) r.getX() + 6, (int) r.getY() + (int) (r.getHeight() / 2), 1, x, 0, false);
//        Tools.pause(1);
    }

    // This function is taken from the jthtest.tools.configdialog class.
    public void prepareToClickOnRow(JTreeOperator tree, int row) {
        tree.makeComponentVisible();
        tree.scrollToRow(row);
        tree.makeVisible(tree.getPathForRow(row));
        if (!tree.isVisible(tree.getPathForRow(row))) {
            System.out.println(
                    "Error, row is not visible after prepairing. Index " + row + ", tree width " /* getWidth() */);
        }
    }

    // This function is used to fill fields in a particular tab.
    public void filltab(JDialogOperator config, JTabbedPaneOperator tab, int tab_num) throws FileNotFoundException {
        if (tab_num == 0) {
            JRadioButtonOperator test = new JRadioButtonOperator(tab, 1);
            test.push();

            JTreeOperator tree = new JTreeOperator(tab, 0);

            clickOnCheckbox(tree, 1);
        } else if (tab_num == 1) {
            // push other radio button.
            JRadioButtonOperator test = new JRadioButtonOperator(tab, 3);
            test.push();

            JButtonOperator add = new JButtonOperator(tab, "Add");
            add.push();
            JDialogOperator addDialog = new JDialogOperator("Select");
            String filename = "demo.jtx";
            new JTextFieldOperator(addDialog, "").enterText(filename);
        } else if (tab_num == 2) {
            JRadioButtonOperator test = new JRadioButtonOperator(tab, 1);
            test.push();

            JButtonOperator add = new JButtonOperator(tab, "Add");
            add.push();
            JDialogOperator addDialog = new JDialogOperator("Select");
            String filename = "knownfailures.kfl";
            new JTextFieldOperator(addDialog, "").enterText(filename);
        } else if (tab_num == 3) {
            JCheckBoxOperator test = new JCheckBoxOperator(tab, 0);
            test.push();

            JCheckBoxOperator passed = new JCheckBoxOperator(tab, 1);
            passed.push();
        } else if (tab_num == 4) {
            new JTextFieldOperator(tab, 0).enterText("2");
            new JTextFieldOperator(tab, 1).enterText("2");
        } else {
            fail("Wrong tab number");
        }
    }

    // Function to check title of a particular tab.
    public boolean checktab(JDialogOperator config, JTabbedPaneOperator tab, int tab_num) {
        if (tab_num == 0) {
            return true;
        } else if (tab_num == 1) {
            JListOperator test = new JListOperator(tab, 0);
            String filepath = ((JLabel) test.getRenderedComponent(0)).getText();
            Path path = Paths.get("demo.jtx");
            String actualpath = path.toAbsolutePath().toString();
            // assertEquals("Error in tab2 that is Exclude List.",filepath,actualpath);
            // System.out.println(filepath);
            // System.out.println(actualpath);
            if (!filepath.equals(actualpath)) {
                fail("Error in tab 3 that is exclude list tab");
            }
        } else if (tab_num == 2) {
            JListOperator test = new JListOperator(tab, 0);
            String filepath = ((JLabel) test.getRenderedComponent(0)).getText();
            Path path = Paths.get("knownfailures.kfl");
            String actualpath = path.toAbsolutePath().toString();
            // assertEquals("Error in tab2 that is Exclude List.",filepath,actualpath);
            if (!filepath.equals(actualpath)) {
                fail("Error in tab 3 that is KF list tab");
            }
        } else if (tab_num == 3) {
            JCheckBoxOperator passed = new JCheckBoxOperator(tab, 1);
            JCheckBoxOperator other1 = new JCheckBoxOperator(tab, 2);
            JCheckBoxOperator other2 = new JCheckBoxOperator(tab, 3);
            JCheckBoxOperator other3 = new JCheckBoxOperator(tab, 4);
            if (!passed.isSelected() || other1.isSelected() || other2.isSelected() || other3.isSelected()) {
                fail("Error in tab4 that is selection tab**update this name**");
            }
        } else if (tab_num == 4) {
            String test1 = new JTextFieldOperator(tab, 0).getText();
            String test2 = new JTextFieldOperator(tab, 1).getText();
            assertEquals("Error in tab5 that is concurrency test.", test1, "2");
            assertEquals("Error in tab5 that is concurrency test.", test2, "2");
        } else {
            fail("Wrong tab number");
        }
        return false;
    }

    // Function to reset Fields in a particular tab.
    public void resettab(JDialogOperator config, JTabbedPaneOperator tab, int tab_num) {
        if (tab_num == 0) {
            JRadioButtonOperator test = new JRadioButtonOperator(tab, 1);
            test.push();

            JTreeOperator tree = new JTreeOperator(tab, 0);

            clickOnCheckbox(tree, 1);

            JRadioButtonOperator test1 = new JRadioButtonOperator(tab, 0);
            test1.push();
        } else if (tab_num == 1) {
            JListOperator test1 = new JListOperator(tab, 0);
            test1.selectItem(0);
            JButtonOperator remove = new JButtonOperator(tab, "Remove");
            remove.push();

            JRadioButtonOperator test = new JRadioButtonOperator(tab, 0);
            test.push();
        } else if (tab_num == 2) {
            JListOperator test1 = new JListOperator(tab, 0);
            test1.selectItem(0);
            JButtonOperator remove = new JButtonOperator(tab, "Remove");
            remove.push();

            JRadioButtonOperator test = new JRadioButtonOperator(tab, 0);
            test.push();
        } else if (tab_num == 3) {
            JCheckBoxOperator passed = new JCheckBoxOperator(tab, 1);
            passed.push();

            JCheckBoxOperator test = new JCheckBoxOperator(tab, 0);
            test.push();
        } else if (tab_num == 4) {
            new JTextFieldOperator(tab, 0).enterText("1");
            new JTextFieldOperator(tab, 1).enterText("1");
        } else {
            fail("Wrong tab number");
        }
    }

    @Test
    public void test14() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException,
            FileNotFoundException, InterruptedException, IOException {
        // Opening a work directory with a configuration file already loaded.
        JMenuBarOperator jmbo = new JMenuBarOperator(mainFrame);
        jmbo.pushMenuNoBlock("File", "/");
        Menu.getFile_Open_WorkDirectoryMenu(mainFrame).pushNoBlock();

        JDialogOperator openDialog = new JDialogOperator(getToolResource("wdc.open.title"));
        // Find edit box for file name
        String dirname = "demowd_config";
        new JTextFieldOperator(openDialog, "").enterText(dirname); // for build.xml use "demowd" else use "temp_dir".

        if (openDialog.isVisible()) {
            fail("Failure because the dialog window 'open work directory' didnt close properly.");
        }

        // Opening Configuration editor.
        jmbo.pushMenuNoBlock("Configure", "/");
        Menu.getConfigure_LoadConfigurationMenu(mainFrame).pushNoBlock();

        // Selecting load configuration dialog box.
        JDialogOperator loadConfigDialog = new JDialogOperator(getExecResource("wdc.loadconfig"));

        // Pushing "Browse" Button for opening config finder dialog box.

        // Selecting Configuration finder dialog box and further Text area inside it.
        JCheckBoxOperator launch_config_checkbox = new JCheckBoxOperator(loadConfigDialog);

        // enabling checkbox on load config dialog.
        launch_config_checkbox.clickMouse();

        new JButtonOperator(loadConfigDialog, "Browse").push();
        JDialogOperator loadConfigfinderDialog = new JDialogOperator(getExecResource("wdc.configchoosertitle"));
        JTextFieldOperator browsetext = new JTextFieldOperator(loadConfigfinderDialog);
        browsetext.clearText();
        browsetext.enterText("democonfig.jti");

        // Pressing load to load the selected file.
        JButtonOperator loadbutton = new JButtonOperator(loadConfigDialog, getExecResource("wdc.load.btn"));
        loadbutton.push();

        JDialogOperator configEditorDialog = new JDialogOperator(getExecResource("ce.name"));
        JListOperator list = new JListOperator(configEditorDialog);
        list.selectItem(1);

        JMenuBarOperator jmbo1 = new JMenuBarOperator(configEditorDialog);
        jmbo1.pushMenu("View", "/");
        jmbo1.pushMenu("View/Quick Set Mode", "/");

        // Now QSM is open and we will make changes here.
        JTabbedPaneOperator QSM_tabs = new JTabbedPaneOperator(configEditorDialog);
        for (int i = 0; i < 5; i++) {
            QSM_tabs.setSelectedIndex(i);
            filltab(configEditorDialog, QSM_tabs, i);
        }

        // Clicking save to save the configuration.
        jmbo1.pushMenuNoBlock("File", "/");
        jmbo1.pushMenuNoBlock("File/Save", "/");

        Thread.sleep(1000);
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

        configEditorDialog = new JDialogOperator(getExecResource("ce.name"));
        list = new JListOperator(configEditorDialog);
        list.selectItem(1);

        jmbo1 = new JMenuBarOperator(configEditorDialog);
        jmbo1.pushMenu("View", "/");
        jmbo1.pushMenu("View/Quick Set Mode", "/");

        // Now we have loaded the config file again and we are checking the values that
        // we change above and also reset them as we check
        // so that it can be used in the next part that is part B.
        QSM_tabs = new JTabbedPaneOperator(configEditorDialog);
        for (int i = 0; i < 5; i++) {
            QSM_tabs.setSelectedIndex(i);
            checktab(configEditorDialog, QSM_tabs, i);
            resettab(configEditorDialog, QSM_tabs, i);
        }

        jmbo1 = new JMenuBarOperator(configEditorDialog);
        jmbo1.pushMenuNoBlock("File", "/");
        jmbo1.pushMenuNoBlock("File/Save", "/");

        pushDoneConfigEditor(configEditorDialog);

        if (configEditorDialog.isVisible()) {
            fail("Failure Because pressing done button didn't close the configuration editor window.");
        }

    }

}