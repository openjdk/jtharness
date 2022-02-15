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
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

import javax.swing.JCheckBox;

import jthtest.menu.Menu;

public class Test_Report_Create1 extends Config_New {

    public static void main(String[] args) {
        JUnitCore.main("jthtest.gui.Sanity_Tests.Test_Report_Create1");
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
            assertTrue("File number " + i + " not found. Expected File at path: " + p[i].toString(),
                    Files.exists(p[i]));
        }
    }

    @Test
    public void test18() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException,
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
            fail("Failure because the dialog window 'open work directory' didnt close properly.");
        }

        // Repeating the test for all four options in the drop down available.
        for (int j = 0; j < 4; j++) {
            // Opening a report via Report->Create Report.
            jmbo.pushMenuNoBlock("Report", "/");
            Menu.getReport_CreateReportMenu(mainFrame).pushNoBlock();

            // Selecting a Create report dialog box and selecting jth dropdown element.
            JDialogOperator reportDialog = new JDialogOperator(getExecResource("nrd.title"));
            JComboBoxOperator dropdown = new JComboBoxOperator(reportDialog, 0);
            dropdown.setSelectedIndex(j);

            // Selecting a checking the list of check boxes.
            JListOperator typelist = new JListOperator(reportDialog, new NameComponentChooser("nrd.typel"));
            for (int i = 0; i < 3; i++) {
                JCheckBox reporttype = (JCheckBox) typelist.getModel().getElementAt(i);
                if (!reporttype.isSelected()) {
                    reporttype.setSelected(true);
                }
            }

            // Entering report directory name and pressing create button.
            JTextFieldOperator reportname = new JTextFieldOperator(reportDialog, 0);
            String report_dir = "temp_report_" + j;
            // reportname.clearText();
            reportname.setText(report_dir);
            JButtonOperator createbutton = new JButtonOperator(reportDialog, new NameComponentChooser("nrd.ok"));
            createbutton.push();

            // Pressing "No" on show report prompt.
            JDialogOperator viewreportques = new JDialogOperator(getExecResource("nrd.showReport.title"));
            new JButtonOperator(viewreportques, 1).push();
            // Calling check function.
            checkreport(report_dir);
        }
    }
}