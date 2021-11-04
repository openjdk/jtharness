/*
 * $Id$
 *
 * Copyright (c) 2001, 2021, Oracle and/or its affiliates. All rights reserved.
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

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

public class Test_QSW3 extends Config_New {
    // This test case checks if Quick Start wizard starts running test when start
    // test check box is clicked and a valid configuration file is loaded.
    public static void main(String[] args) {
        JUnitCore.main("jthtest.gui.Sanity_Tests.Test_QSW3");
    }

    @Test
    public void test() throws InterruptedException {
        // Selecting the quick start dialog box and checking the header.
        JDialogOperator quickstart = new JDialogOperator(getExecResource("qsw.title"));

        // Selecting the next and back buttons.
        JButtonOperator next = new JButtonOperator(quickstart, 1);

        // Selecting the checkbox named 'start a new run'.
        JRadioButtonOperator qsw_check = new JRadioButtonOperator(quickstart, 0);
        qsw_check.push();
        next.push();

        // pressing browse button.
        JButtonOperator browse = new JButtonOperator(quickstart, 0);
        browse.push();

        // Selecting Test suite to open.
        JDialogOperator opentestdialog = new JDialogOperator(getToolResource("tsc.title"));
        new JTextFieldOperator(opentestdialog, "").enterText("demots");
        next.push();

        // Selecting the configuration template radio button and then selecting the
        // configuration file.
        JRadioButtonOperator test = new JRadioButtonOperator(quickstart, 1);
        test.push();

        Path p = Paths.get("democonfig.jti");
        String configfile = p.toAbsolutePath().toString();
        System.out.println(configfile);

        browse = new JButtonOperator(quickstart, 0);
        browse.push();
        JDialogOperator loadconfig = new JDialogOperator(getExecResource("ce.load.title"));
        new JTextFieldOperator(loadconfig, "").enterText(configfile);
        next.push();

        // Creating directory.
        browse = new JButtonOperator(quickstart, 0);
        browse.push();
        JDialogOperator createworkdialog = new JDialogOperator(getToolResource("wdc.new.title"));
        new JTextFieldOperator(createworkdialog, "").enterText("temp_qsw_dir7");
        next.push();

        // Selecting and testing check box function.
        JCheckBoxOperator start_ce = new JCheckBoxOperator(quickstart, 0);
        JCheckBoxOperator start_test = new JCheckBoxOperator(quickstart, 1);

        if (!start_ce.isSelected()) {
            fail("Expected: Start configuration Editor check box should be selected by default. \nActual: Start configuration Editor check box is not selected by default.");
        }

        start_ce.push();
        start_test.push();

        if (start_test.isSelected() && start_ce.isSelected()) {
            fail("Expected: Selecting Start test run checkbox should not select Start configuration Editor checkbox. \nActual: Selecting Start test run checkbox has selected Start configuration Editor checkbox.");
        }

        JButtonOperator finish = new JButtonOperator(quickstart, 2);
        finish.push();

        if (quickstart.isVisible()) {
            fail("Expected: Quick Start Wizard should not visible after clicking on finish button. \nActual: Quick Start Wizard is still visible even after clicking on finish button.");
        }

        // Check that tests started performing.
        JTextFieldOperator strip = new JTextFieldOperator(mainFrame, new NameComponentChooser("strip.msg"));
        int time = 0;

        // Waiting for Execution of tests to finish.
        while (time < 60) {
            if (strip.getText().startsWith("Finished")) {
                break;
            } else if (!strip.getText().startsWith("Running")) {
                fail("Expected: message starting with 'Running' has to be displayed in the strip message field. \nActual: wrong message "
                        + strip.getText() + "is displayed in the strip message field.");
            } else {
                // Sleeping for one second if message in strip starts with 'Running'.
                Thread.sleep(1000);
            }
        }

        assertTrue("Expected: tests should run successfully. \nActual: Error while tests are running.",
                strip.getText().startsWith("Finished"));

    }
}
