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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

import javax.swing.JLabel;

import jthtest.menu.Menu;

public class Test_QSM_Exclude1 extends Config_New {

    public static void main(String[] args) {
        JUnitCore.main("jthtest.gui.Sanity_Tests.Test_QSM_Exclude1");
    }

    /**
     * Check exclude list adding in Quick set mode configuration editor
     *
     * @throws ClassNotFoundException
     *
     * @throws InvocationTargetException
     *
     * @throws NoSuchMethodException
     *
     */
    @Test
    public void test() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException,
            FileNotFoundException, InterruptedException, IOException {
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

        QSM_tabs.setSelectedIndex(1);
        JRadioButtonOperator test = new JRadioButtonOperator(QSM_tabs, 3);
        test.push();
        JButtonOperator add = new JButtonOperator(QSM_tabs, "Add");
        add.push();
        JDialogOperator addDialog = new JDialogOperator("Select");
        new JTextFieldOperator(addDialog, "").enterText("qsm_exclude.jtx");
        jmbo1.pushMenuNoBlock("File", "/");
        jmbo1.pushMenuNoBlock("File/Save", "/");
        pushDoneConfigEditor(configEditorDialog);
        JComboBoxOperator viewfilter = new JComboBoxOperator(mainFrame, 0);
        viewfilter.selectItem(1);
        JTabbedPaneOperator mainpane = new JTabbedPaneOperator(mainFrame, new NameComponentChooser("br.tabs"));
        mainpane.setSelectedIndex(6);
        JTableOperator filtered = new JTableOperator(mainpane);
        String testname = ((JLabel) filtered.getRenderedComponent(0, 0)).getText();

        assertEquals(
                "Wrong test filtered when filtering via exclude list filter. Expected first test: lists/DoublyLinkedList/InsertTest.java#id0 But was: "
                        + testname,
                "lists/DoublyLinkedList/InsertTest.java#id0", testname);

        jmbo.pushMenuNoBlock("Configure", "/");
        Menu.getConfigure_EditConfigurationMenu(mainFrame).pushNoBlock();
        configEditorDialog = new JDialogOperator(getExecResource("ce.name"));
        list = new JListOperator(configEditorDialog);
        list.selectItem(1);
        jmbo1 = new JMenuBarOperator(configEditorDialog);
        jmbo1.pushMenu("View", "/");
        jmbo1.pushMenu("View/Quick Set Mode", "/");
        QSM_tabs = new JTabbedPaneOperator(configEditorDialog);
        QSM_tabs.setSelectedIndex(1);
        JListOperator test1 = new JListOperator(QSM_tabs, 0);
        test1.selectItem(0);
        JButtonOperator remove = new JButtonOperator(QSM_tabs, "Remove");
        remove.push();
        JRadioButtonOperator test2 = new JRadioButtonOperator(QSM_tabs, 0);
        test2.push();
        jmbo1.pushMenuNoBlock("File", "/");
        jmbo1.pushMenuNoBlock("File/Save", "/");
        pushDoneConfigEditor(configEditorDialog);
    }
}
