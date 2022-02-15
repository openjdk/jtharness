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

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.util.NameComponentChooser;

import javax.swing.JLabel;

import jthtest.menu.Menu;

public class Test_QSM_Test1 extends Config_New {
    // This is the 17th Sanity Test. It tests functionality of Tests to run option
    // in QSM of configuration editor.
    public static void main(String[] args) {
        JUnitCore.main("jthtest.gui.Sanity_Tests.Test_QSM_Test1");
    }

    // This function is used to click on the check boxes in JTreeOperator.
    public void clickOnCheckbox(JTreeOperator tree, int row) {
        Rectangle r = tree.getRowBounds(row);
        prepareToClickOnRow(tree, row);
        int x = Operator.getDefaultMouseButton();

        tree.clickMouse((int) r.getX() + 6, (int) r.getY() + (int) (r.getHeight() / 2), 1, x, 0, false);

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

    @Test
    public void test17() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException,
            FileNotFoundException, InterruptedException, IOException {

        // Opening a Work directory.
        JMenuBarOperator jmbo = new JMenuBarOperator(mainFrame);
        jmbo.pushMenuNoBlock("File", "/");
        Menu.getFile_Open_WorkDirectoryMenu(mainFrame).pushNoBlock();

        JDialogOperator openDialog = new JDialogOperator(getToolResource("wdc.open.title"));
        // Find edit box for file name
        String dirname = "demowd_config";
        new JTextFieldOperator(openDialog, "").enterText(dirname); // for build.xml use "demowd" else use "temp_dir".

        // Checking if open work directory dialog box disappears.
        if (openDialog.isVisible()) {
            fail("Failure because the dialog window 'open work directory' didnt close properly.");
        }

        // Opening configuration editor via edit configuration.
        jmbo.pushMenuNoBlock("Configure", "/");
        Menu.getConfigure_EditConfigurationMenu(mainFrame).pushNoBlock();

        // Selecting configration editor and moving to question one.
        JDialogOperator configEditorDialog = new JDialogOperator(getExecResource("ce.name"));
        JListOperator list = new JListOperator(configEditorDialog);
        list.selectItem(1);

        JMenuBarOperator jmbo1 = new JMenuBarOperator(configEditorDialog);
        // Changing mode to QSM.
        jmbo1.pushMenu("View", "/");
        jmbo1.pushMenu("View/Quick Set Mode", "/");

        // Now QSM is open and we will make changes here.
        JTabbedPaneOperator QSM_tabs = new JTabbedPaneOperator(configEditorDialog);

        // Selecting and editing tab1 that is test tab.
        QSM_tabs.setSelectedIndex(0);
        JRadioButtonOperator test = new JRadioButtonOperator(QSM_tabs, 1);
        test.push();
        JTreeOperator tree = new JTreeOperator(QSM_tabs, 0);
        clickOnCheckbox(tree, 1);

        // Saving the changes and pressing done.
        jmbo1.pushMenuNoBlock("File", "/");
        jmbo1.pushMenuNoBlock("File/Save", "/");
        pushDoneConfigEditor(configEditorDialog);

        if (configEditorDialog.isVisible()) {
            fail("Failure Because pressing done button didn't close the configuration editor window.");
        }

        // Selecting 'current configuration' on the dropdown on mainframe.
        JComboBoxOperator viewfilter = new JComboBoxOperator(mainFrame, 0);
        viewfilter.selectItem(1);

        // selecting 'filtered test tab'.
        JTabbedPaneOperator mainpane = new JTabbedPaneOperator(mainFrame, new NameComponentChooser("br.tabs"));
        mainpane.setSelectedIndex(6);

        JTableOperator filtered = new JTableOperator(mainpane);

        // checking if correct tests are filtered or not.
        String testname = ((JLabel) filtered.getRenderedComponent(0, 0)).getText();
        assertEquals(
                "Wrong test filtered while seperating tests via tests to run filter. Expected first test: BigNum/AddTest.java#id0 But was: "
                        + testname,
                "BigNum/AddTest.java#id0", testname);

        // Reopening the configuration editor.
        jmbo.pushMenuNoBlock("Configure", "/");
        Menu.getConfigure_EditConfigurationMenu(mainFrame).pushNoBlock();

        configEditorDialog = new JDialogOperator(getExecResource("ce.name"));
        list = new JListOperator(configEditorDialog);
        list.selectItem(1);

        // Changing to QSM.
        jmbo1 = new JMenuBarOperator(configEditorDialog);
        jmbo1.pushMenu("View", "/");
        jmbo1.pushMenu("View/Quick Set Mode", "/");

        // Now QSM is open and we will make changes here.
        QSM_tabs = new JTabbedPaneOperator(configEditorDialog);

        // Resetting changes done in the configuration.
        QSM_tabs.setSelectedIndex(0);
        JRadioButtonOperator test3 = new JRadioButtonOperator(QSM_tabs, 1);
        test3.push();
        tree = new JTreeOperator(QSM_tabs, 0);
        clickOnCheckbox(tree, 1);

        JRadioButtonOperator test1 = new JRadioButtonOperator(QSM_tabs, 0);
        test1.push();

        // Saving back the configuration.
        jmbo1.pushMenuNoBlock("File", "/");
        jmbo1.pushMenuNoBlock("File/Save", "/");

        pushDoneConfigEditor(configEditorDialog);

        if (configEditorDialog.isVisible()) {
            fail("Failure Because pressing done button didn't close the configuration editor window.");
        }
    }
}