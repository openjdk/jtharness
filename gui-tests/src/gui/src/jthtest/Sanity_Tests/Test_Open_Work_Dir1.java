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

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

import jthtest.menu.Menu;

public class Test_Open_Work_Dir1 extends Open_Work_Dir {

    public static void main(String[] args) {
        JUnitCore.main("jthtest.gui.Sanity_Tests.Test_Open_Work_Dir1");
    }

    @Test
    public void testOpenTestSuite1()
            throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, FileNotFoundException {

        // Opening work directory.
        JMenuBarOperator jmbo = new JMenuBarOperator(mainFrame);
        jmbo.pushMenuNoBlock("File", "/");
        Menu.getFile_Open_WorkDirectoryMenu(mainFrame).pushNoBlock();

        JDialogOperator openDialog = new JDialogOperator(getToolResource("wdc.open.title"));
        // Find edit box for file name
        String dirname = "demowd";
        new JTextFieldOperator(openDialog, "").enterText(dirname);

        if (openDialog.isVisible()) {
            fail("Failure because the open work directory dialog window didnt close.");
        }

        // Finding the text field which displays the current work directory name.
        ComponentChooser n = new NameComponentChooser("bcc.WorkDir");
        JTextFieldOperator work_directory_dialog = new JTextFieldOperator(mainFrame, n);

        waitForWDLoading(mainFrame, WDLoadingResult.SOME_NOTRUN);
        new JTabbedPaneOperator(mainFrame, TAB_CAPTION);

        // verifying that name of our work directory matches with the one written in the
        // text field.
        assertEquals("Failure because current working directory is not same as " + dirname + ".",
                work_directory_dialog.getText(), dirname);
    }

}