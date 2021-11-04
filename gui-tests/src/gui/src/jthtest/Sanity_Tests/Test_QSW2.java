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

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

public class Test_QSW2 extends Config_New {
    // This test case checks if Quick Start wizard Opens up configuration editor.
    public static void main(String[] args) {
        JUnitCore.main("jthtest.gui.Sanity_Tests.Test_QSW2");
    }

    @Test
    public void test() {
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
        JRadioButtonOperator test = new JRadioButtonOperator(quickstart, 0);
        test.push();
        next.push();
        browse = new JButtonOperator(quickstart, 0);
        browse.push();
        JDialogOperator createworkdialog = new JDialogOperator(getToolResource("wdc.new.title"));
        new JTextFieldOperator(createworkdialog, "").enterText("temp_qsw_dir1");
        next.push();

        // Selecting and testing check box function.
        JCheckBoxOperator start_ce = new JCheckBoxOperator(quickstart, 0);
        JCheckBoxOperator start_test = new JCheckBoxOperator(quickstart, 1);

        if (!start_ce.isSelected()) {
            fail("Expected: Start configuration Editor check box should be selected by default. \nActual: Start configuration Editor check box not selected by default.");
        }

        start_ce.push();
        start_test.push();

        if (start_test.isSelected() && !start_ce.isSelected()) {
            fail("Expected: Start test run checkbox should not select without Start configuration Editor check box being set. \nActual: Start test run checkbox is selected without start config editor check box being set.");
        }

        // Pressing finish button.
        start_test.push();
        JButtonOperator finish = new JButtonOperator(quickstart, 2);
        finish.push();

        if (quickstart.isVisible()) {
            fail("Expected: Quick Start Wizard should not visible after clicking on finish button. \nActual: Quick Start Wizard is still visible even after clicking on finish.");
        }

        // Waiting for the configuration editor to open up.
        JDialogOperator config_editor = new JDialogOperator(getExecResource("ce.name"));

        if (!config_editor.isVisible()) {
            fail("Expected: Configuration Editor should visible after closing Quick Start Wizard. \nActual: Configuration Editor is not visible after closing Quick Start Wizard.");
        }
    }
}
