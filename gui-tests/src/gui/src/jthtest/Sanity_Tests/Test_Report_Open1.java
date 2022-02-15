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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import jthtest.menu.Menu;

public class Test_Report_Open1 extends Config_New {

    public static void main(String[] args) {
        JUnitCore.main("jthtest.gui.Sanity_Tests.Test_Report_Open1");
    }

    // Function to check if all neccesary files are created or not.
    public void checkreport(String name) {
        Path p[] = new Path[13];
        p[0] = Paths.get("./" + name + "/index.html");
        p[1] = Paths.get("./" + name + "/reportdir.dat");
        p[2] = Paths.get("./" + name + "/xml/report.xml");
        p[3] = Paths.get("./" + name + "/text/summary.txt");
        p[4] = Paths.get("./" + name + "/html/config.html");
        p[5] = Paths.get("./" + name + "/html/env.html");
        p[6] = Paths.get("./" + name + "/html/error.html");
        p[7] = Paths.get("./" + name + "/html/excluded.html");
        p[8] = Paths.get("./" + name + "/html/failed.html");
        p[9] = Paths.get("./" + name + "/html/notRun.html");
        p[10] = Paths.get("./" + name + "/html/passed.html");
        p[11] = Paths.get("./" + name + "/html/report.css");
        p[12] = Paths.get("./" + name + "/html/report.html");

        for (int i = 0; i < 13; i++) {
            assertTrue("file number " + i + " not found.", Files.exists(p[i]));
        }
    }

    @Test
    public void test19() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException,
            FileNotFoundException, InterruptedException, IOException {
        // Opening Work Directory.
        JMenuBarOperator jmbo = new JMenuBarOperator(mainFrame);
        jmbo.pushMenuNoBlock("File", "/");
        Menu.getFile_Open_WorkDirectoryMenu(mainFrame).pushNoBlock();

        JDialogOperator openDialog = new JDialogOperator(getToolResource("wdc.open.title"));
        // Find edit box for file name
        String dirname = "demowd_config";
        new JTextFieldOperator(openDialog, "").enterText(dirname); // for build.xml use "demowd" else use "temp_dir".

        // Checking if work directory opened properly or not.
        if (openDialog.isVisible()) {
            fail("Failure because the dialog window 'open work directory' didnt close properly and is still visible on screen.");
        }

        // Opening a report via Report->Open Report.
        jmbo.pushMenuNoBlock("Report", "/");
        Menu.getReport_OpenReportMenu(mainFrame).pushNoBlock();

        // Selecting a Open report dialog box and entering a report name.
        JDialogOperator reportDialog = new JDialogOperator(getExecResource("rh.open.title"));
        JTextFieldOperator openreporttext = new JTextFieldOperator(reportDialog, "");
        openreporttext.enterText("temp_report_0");
        openreporttext.enterText("index.html");

        // Selecting Report Browser and linking on link 'HTML Report'.
        JDialogOperator reportdialog = new JDialogOperator("Report Browser");
        JEditorPaneOperator test1 = new JEditorPaneOperator(reportdialog);
        // HTMLDocument doc = (HTMLDocument)test1.getDocument();
        Thread.sleep(1000);
        test1.clickMouse(test1.modelToView(test1.getPositionByText("HTML Report")).x,
                test1.modelToView(test1.getPositionByText("HTML Report")).y, 1);
        Thread.sleep(1000);
        // Checking if correct page opens up after clicking the link.
        JEditorPaneOperator test2 = new JEditorPaneOperator(reportdialog);
        assertTrue("Failure Beacuse Wrong page or unexpected error dialog box appeared on screen.",
                test2.getText().contains("JT Harness : Report"));
    }
}