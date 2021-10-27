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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

public class Test_QSW1 extends Config_New {
    // This test case checks if Quick Start wizard Shows correct display text and
    // next and back button works.
    public static void main(String[] args) {
        JUnitCore.main("jthtest.gui.Sanity_Tests.Test_QSW1");
    }

    @Test
    public void test() {
        // Selecting the quick start dialog box and checking the header.
        JDialogOperator quickstart = new JDialogOperator(getExecResource("qsw.title"));
        JTextFieldOperator qsw_header = new JTextFieldOperator(quickstart, 0);

        // Checking header text
        assertEquals("Failed because of Incorrect header text - ", "Welcome to the JT Harness!", qsw_header.getText());

        // Checking Text inside the body.
        JTextAreaOperator qsw_text = new JTextAreaOperator(quickstart, 0);
        assertEquals("Failed because of Incorrect text in text body - ",
                "This Quick Start Guide will lead you through the process of setting up the JT Harness to perform common tasks.\n"
                        + "\n" + "Which of the following tasks do you want to do?",
                qsw_text.getText());

        // Selecting the next and back buttons.
        JButtonOperator next = new JButtonOperator(quickstart, 1);
        JButtonOperator back = new JButtonOperator(quickstart, 0);

        // Selecting the checkbox named 'start a new run'.
        JTextFieldOperator qsw_temp;
        JRadioButtonOperator qsw_check = new JRadioButtonOperator(quickstart, 0);
        qsw_check.push();
        next.push();

        // Checking text header that appears after next button is press.
        qsw_temp = new JTextFieldOperator(quickstart, 0);
        assertEquals("Failed because of Incorrect header text - ", "Test Suite", qsw_temp.getText());
        back.push();

        // Repeating the above process for 2 more times for the remaining 2 radio
        // buttons on the main QS wizard dialog.
        qsw_temp = new JTextFieldOperator(quickstart, 0);
        assertEquals("Failed because of Incorrect header text - ", "Welcome to the JT Harness!", qsw_temp.getText());

        qsw_check = new JRadioButtonOperator(quickstart, 1);
        qsw_check.push();

        next.push();
        qsw_temp = new JTextFieldOperator(quickstart, 0);
        assertEquals("Failed because of Incorrect header text - ", "Work Directory", qsw_temp.getText());

        back.push();
        qsw_temp = new JTextFieldOperator(quickstart, 0);
        assertEquals("Failed because of Incorrect header text - ", "Welcome to the JT Harness!", qsw_temp.getText());

        qsw_check = new JRadioButtonOperator(quickstart, 2);
        qsw_check.push();
        next.push();
        qsw_temp = new JTextFieldOperator(quickstart, 0);
        assertEquals("Failed because of Incorrect header text - ", "Test Suite", qsw_temp.getText());

        back.push();
        qsw_temp = new JTextFieldOperator(quickstart, 0);
        assertEquals("Failed because of Incorrect header text - ", "Welcome to the JT Harness!", qsw_temp.getText());
    }
}
