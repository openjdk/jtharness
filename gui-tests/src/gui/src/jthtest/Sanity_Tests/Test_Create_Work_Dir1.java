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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import jthtest.menu.Menu;

public class Test_Create_Work_Dir1 extends Create_Work_Dir {

    public static void main(String[] args) {
        JUnitCore.main("jthtest.gui.Sanity_Tests.Test_Create_Work_Dir1");
    }

    @Test
    public void testCreate_Work_Dir1() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {

        // Opening a test suite using previous test.
        openTestSuite(mainFrame);
        // Find the "DemoTS" tab
        waitForWDLoading(mainFrame, WDLoadingResult.SOME_NOTRUN);

        // Clicking on the File menu option and then selecting create work directory
        // option.
        JMenuBarOperator jmbo = new JMenuBarOperator(mainFrame);
        jmbo.pushMenuNoBlock("File", "/");
        Menu.getFile_CreateWorkDirectoryMenu(mainFrame).pushNoBlock();

        // Selecting the "Create work directory" dialog box and then entering "temp_dir"
        // in the text field.
        JDialogOperator openDialog = new JDialogOperator(getToolResource("wdc.new.title"));
        // Find edit box for file name
        new JTextFieldOperator(openDialog, "").enterText("temp_dir");

        // Check if dialog box closed properly.
        if (openDialog.isVisible()) {
            fail("Failed because Create Dialog is still open.");
        }

        // Wait for the Test suite to load properly.
        waitForWDLoading(mainFrame, WDLoadingResult.SOME_NOTRUN);
        new JTabbedPaneOperator(mainFrame, TAB_CAPTION);

        // verifying path of the newly created work directory.
        Path path = Paths.get("./temp_dir");
        String message = "Failure due to error in creation of work directory.";
        assertTrue(message, Files.exists(path));
    }

}