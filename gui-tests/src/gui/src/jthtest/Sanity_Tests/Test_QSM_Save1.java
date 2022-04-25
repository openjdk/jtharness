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

public class Test_QSM_Save1 extends Config_New {
    public static void main(String[] args) {
        JUnitCore.main("jthtest.gui.Sanity_Tests.Test_QSM_Save1");
    }

    /**
     * Verify that Save button under File menu in Quick set mode configuration
     * editor will save the current configuration back in a file if the file was
     * loaded.
     *
     * @throws ClassNotFoundException
     *
     * @throws InvocationTargetException
     *
     * @throws NoSuchMethodException
     */
    @Test
    public void test() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException,
            FileNotFoundException, InterruptedException, IOException {
        int qsm_index = 0;
        JMenuBarOperator jmbo = new JMenuBarOperator(mainFrame);
        Menu.getFile_Open_WorkDirectoryMenu(mainFrame).pushNoBlock();
        JDialogOperator openDialog = new JDialogOperator(getToolResource("wdc.open.title"));
        new JTextFieldOperator(openDialog, "").enterText("demowd_config");

        jmbo.pushMenuNoBlock("Configure", "/");
        Menu.getConfigure_EditConfigurationMenu(mainFrame).pushNoBlock();
        JDialogOperator configEditorDialog = new JDialogOperator(getExecResource("ce.name"));
        JListOperator list = new JListOperator(configEditorDialog);
        list.selectItem(1);

        JMenuBarOperator jmbo1 = new JMenuBarOperator(configEditorDialog);
        jmbo1.pushMenu("View", "/");
        jmbo1.pushMenu("View/Quick Set Mode", "/");
        JTabbedPaneOperator QSM_tabs = new JTabbedPaneOperator(configEditorDialog);

        while (qsm_index < 5) {
            QSM_tabs.setSelectedIndex(qsm_index);
            editQSMTab(configEditorDialog, QSM_tabs, qsm_index);
            qsm_index++;
        }
        jmbo1.pushMenu("File", "/");
        jmbo1.pushMenuNoBlock("File/Save", "/");
        pushDoneConfigEditor(configEditorDialog);

        jmbo.pushMenuNoBlock("Configure", "/");
        Menu.getConfigure_LoadConfigurationMenu(mainFrame).pushNoBlock();
        JDialogOperator loadConfigDialog = new JDialogOperator(getExecResource("wdc.loadconfig"));
        JCheckBoxOperator launch_config_checkbox = new JCheckBoxOperator(loadConfigDialog);
        launch_config_checkbox.clickMouse();
        new JButtonOperator(loadConfigDialog, "Browse").push();
        JDialogOperator loadConfigfinderDialog = new JDialogOperator(getExecResource("wdc.configchoosertitle"));
        JTextFieldOperator browsetext = new JTextFieldOperator(loadConfigfinderDialog);
        browsetext.clearText();
        browsetext.enterText("democonfig.jti");

        JButtonOperator loadbutton = new JButtonOperator(loadConfigDialog, getExecResource("wdc.load.btn"));
        loadbutton.push();

        configEditorDialog = new JDialogOperator(getExecResource("ce.name"));
        list = new JListOperator(configEditorDialog);
        list.selectItem(1);

        jmbo1 = new JMenuBarOperator(configEditorDialog);
        jmbo1.pushMenu("View", "/");
        jmbo1.pushMenu("View/Quick Set Mode", "/");
        QSM_tabs = new JTabbedPaneOperator(configEditorDialog);

        while (qsm_index < 5) {
            QSM_tabs.setSelectedIndex(qsm_index);
            checkQSMTab(configEditorDialog, QSM_tabs, qsm_index);
            resetQSMTab(configEditorDialog, QSM_tabs, qsm_index);
            qsm_index++;
        }

        jmbo1 = new JMenuBarOperator(configEditorDialog);
        jmbo1.pushMenu("File", "/");
        jmbo1.pushMenuNoBlock("File/Save", "/");
        pushDoneConfigEditor(configEditorDialog);

    }

    /**
     * This function is to click on CheckBox on Quick Set Mode
     *
     * @param JTreeOperator
     *
     * @param row
     */
    public void clickOnCheckbox(JTreeOperator tree, int row) {
        Rectangle r = tree.getRowBounds(row);
        clickOnTreeRow(tree, row);
        int x = Operator.getDefaultMouseButton();
        tree.clickMouse((int) r.getX() + 6, (int) r.getY() + (int) (r.getHeight() / 2), 1, x, 0, false);
    }

    /**
     * This function is to click on Tests Tree Row on Quick Set Mode
     *
     * @param JTreeOperator
     *
     * @param row
     */
    public void clickOnTreeRow(JTreeOperator tree, int row) {
        tree.makeComponentVisible();
        tree.scrollToRow(row);
        tree.makeVisible(tree.getPathForRow(row));
    }

    /**
     * This function is to edit configuration on Quick Set Mode. For example: Tests,
     * ExcludeLists, Known Failures, prior status and Execution
     *
     * @param JDialogOperator
     *
     * @param JTabbedPaneOperator
     *
     * @param tab_num
     *
     * @throws FileNotFoundException
     */
    public void editQSMTab(JDialogOperator config, JTabbedPaneOperator tab, int tab_num) throws FileNotFoundException {
        JRadioButtonOperator jrbo = null;
        JButtonOperator add = null;
        switch (tab_num) {
        case 0:
            jrbo = new JRadioButtonOperator(tab, 1);
            jrbo.push();
            JTreeOperator tree = new JTreeOperator(tab, 0);
            clickOnCheckbox(tree, 1);
            break;
        case 1:
            JRadioButtonOperator jrbo1 = new JRadioButtonOperator(tab, 3);
            jrbo1.push();
            add = new JButtonOperator(tab, "Add");
            add.push();
            JDialogOperator addDialog = new JDialogOperator("Select");
            new JTextFieldOperator(addDialog, "").enterText("demo.jtx");
            break;
        case 2:
            jrbo = new JRadioButtonOperator(tab, 1);
            jrbo.push();
            add = new JButtonOperator(tab, "Add");
            add.push();
            JDialogOperator addDialog1 = new JDialogOperator("Select");
            new JTextFieldOperator(addDialog1, "").enterText("knownfailures.kfl");
            break;
        case 3:
            JCheckBoxOperator jcbo = new JCheckBoxOperator(tab, 0);
            jcbo.push();
            JCheckBoxOperator passed = new JCheckBoxOperator(tab, 1);
            passed.push();
            break;
        case 4:
            new JTextFieldOperator(tab, 0).enterText("2");
            new JTextFieldOperator(tab, 1).enterText("3");
            break;
        default:
            fail("Trying to select non-existing tab on QSM");
        }
    }

    /**
     * This function is to check/verify configuration on Quick Set Mode. For
     * example: Tests, ExcludeLists, Known Failures, prior status and Execution
     *
     * @param JDialogOperator
     *
     * @param JTabbedPaneOperator
     *
     * @param tab_num
     */
    public void checkQSMTab(JDialogOperator config, JTabbedPaneOperator tab, int tab_num) {
        JListOperator jlo = null;
        String actualpath = "";
        String filepath = "";
        Path path;
        switch (tab_num) {
        case 0:
            return;
        case 1:
            jlo = new JListOperator(tab, 0);
            filepath = ((JLabel) jlo.getRenderedComponent(0)).getText();
            path = Paths.get("demo.jtx");
            actualpath = path.toAbsolutePath().toString();
            if (!filepath.equals(actualpath)) {
                fail("Error in tab2 on QSM i.e. Exclude List");
            }
            break;
        case 2:
            jlo = new JListOperator(tab, 0);
            filepath = ((JLabel) jlo.getRenderedComponent(0)).getText();
            path = Paths.get("knownfailures.kfl");
            actualpath = path.toAbsolutePath().toString();
            if (!filepath.equals(actualpath)) {
                fail("Error in tab3 on QSM i.e. Known Failures (KFL)");
            }
            break;
        case 3:
            JCheckBoxOperator passed = new JCheckBoxOperator(tab, 1);
            JCheckBoxOperator other1 = new JCheckBoxOperator(tab, 2);
            JCheckBoxOperator other2 = new JCheckBoxOperator(tab, 3);
            JCheckBoxOperator other3 = new JCheckBoxOperator(tab, 4);
            if (!passed.isSelected() || other1.isSelected() || other2.isSelected() || other3.isSelected()) {
                fail("Error in tab4 on QSM i.e. Prior Status");
            }
            break;
        case 4:
            String jtfo = new JTextFieldOperator(tab, 0).getText();
            String jtfo1 = new JTextFieldOperator(tab, 1).getText();
            assertEquals("Error in tab5 on QSM i.e. Execution.", jtfo, "2");
            assertEquals("Error in tab5 on QSM i.e. Execution", jtfo1, "3");
            break;
        default:
            fail("Trying to select non-existing tab on QSM");
        }
    }

    /**
     * This function is to rest configuration on Quick Set Mode. For example: Tests,
     * ExcludeLists, Known Failures, prior status and Execution
     *
     * @param JDialogOperator
     *
     * @param JTabbedPaneOperator
     *
     * @param tab_num
     */
    public void resetQSMTab(JDialogOperator config, JTabbedPaneOperator tab, int tab_num) {
        JRadioButtonOperator jro = null;
        JButtonOperator remove = null;
        JListOperator jlo = null;
        switch (tab_num) {
        case 0:
            JRadioButtonOperator jro1 = new JRadioButtonOperator(tab, 1);
            jro1.push();
            JTreeOperator tree = new JTreeOperator(tab, 0);
            clickOnCheckbox(tree, 1);
            jro = new JRadioButtonOperator(tab, 0);
            jro.push();
            break;
        case 1:
            jlo = new JListOperator(tab, 0);
            jlo.selectItem(0);
            remove = new JButtonOperator(tab, "Remove");
            remove.push();
            jro = new JRadioButtonOperator(tab, 0);
            jro.push();
            break;
        case 2:
            jlo = new JListOperator(tab, 0);
            jlo.selectItem(0);
            remove = new JButtonOperator(tab, "Remove");
            remove.push();
            jro = new JRadioButtonOperator(tab, 0);
            jro.push();
            break;
        case 3:
            JCheckBoxOperator passed = new JCheckBoxOperator(tab, 1);
            passed.push();
            JCheckBoxOperator jcbo = new JCheckBoxOperator(tab, 0);
            jcbo.push();
            break;
        case 4:
            new JTextFieldOperator(tab, 0).enterText("1");
            new JTextFieldOperator(tab, 1).enterText("1");
            break;
        default:
            fail("Trying to select non-existing tab on QSM");
        }
    }
}