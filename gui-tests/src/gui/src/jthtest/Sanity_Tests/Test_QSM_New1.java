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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import javax.swing.JLabel;

import jthtest.menu.Menu;

public class Test_QSM_New1 extends Open_Test_Suite {
    public static void main(String[] args) {
        JUnitCore.main("jthtest.gui.Sanity_Tests.Test_QSM_New1");
    }

    /**
     * Verify that New button under File menu in the Quick Set mode configuration
     * editor in an existing directory will reset Configuration Editor to empty.
     *
     * @throws ClassNotFoundException
     *
     * @throws InvocationTargetException
     *
     * @throws NoSuchMethodException
     *
     */
    @Test
    public void test()
            throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InterruptedException {

        JMenuBarOperator jmbo = new JMenuBarOperator(mainFrame);
        Menu.getFile_Open_WorkDirectoryMenu(mainFrame).pushNoBlock();
        JDialogOperator openDialog = new JDialogOperator(getToolResource("wdc.open.title"));
        new JTextFieldOperator(openDialog, "").enterText("demowd_config");
        waitForWDLoading(mainFrame, WDLoadingResult.SOME_NOTRUN);
        jmbo.pushMenuNoBlock("Configure", "/");
        Menu.getConfigure_EditConfigurationMenu(mainFrame).pushNoBlock();

        JDialogOperator configEditorDialog = new JDialogOperator(getExecResource("ce.name"));
        JMenuBarOperator jmbo1 = new JMenuBarOperator(configEditorDialog);
        jmbo1.pushMenu("View", "/");
        jmbo1.pushMenu("View/Quick Set Mode", "/");
        JDialogOperator qsm = new JDialogOperator(getExecResource("ce.name"));
        JMenuBarOperator qsm_jmbo = new JMenuBarOperator(qsm);
        qsm_jmbo.pushMenu("File", "/");
        mainFrame.pressKey(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK);

        JListOperator list = new JListOperator(configEditorDialog);
        String lastlistitem = ((JLabel) list.getRenderedComponent(list.getModel().getSize() - 1)).getText();
        String secondlastlistitem = ((JLabel) list.getRenderedComponent(list.getModel().getSize() - 2)).getText();

        assertEquals("Last item was Expected: 'More...' But was: " + lastlistitem, " More...", lastlistitem);
        assertEquals("Second Last item was Expected: 'Configuration Name' But was: " + secondlastlistitem,
                "   Configuration Name", secondlastlistitem);
    }

}
