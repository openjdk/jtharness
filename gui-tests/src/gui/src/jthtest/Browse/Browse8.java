/*
 * $Id$
 *
 * Copyright (c) 2009, 2010, Oracle and/or its affiliates. All rights reserved.
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
package jthtest.Browse;

import org.junit.Test;
import org.junit.runner.JUnitCore;

public class Browse8 extends Browse {

    public static void main(String[] args) {
        JUnitCore.main("jthtest.gui.Browse.Browse8");
    }

    @Test
    public void test_correct_numbers_shown_after_testsuite_loaded() {

        // click on Browse the Test Suite Radio button
        browseTestsuite(quickStartDialog);

        // click on next button
        next(quickStartDialog);

        // Select the test suite
        pickDefaultTestsuite(quickStartDialog);

        // click on next button
        next(quickStartDialog);

        // Select the complete configuration template
        useConfigTemplate(quickStartDialog);

        // click on next button
        next(quickStartDialog);

        // start configuration editor
        startConfigEditor(quickStartDialog);

        // click on finish button
        finish(quickStartDialog, false);

        // verify that the correct numbers are shown in the right panel
        checkCounters(mainFrame, new int[] { 0, 0, 0, 22, 22, 0, 22 });

    }

    // TestCase Description
    public String getDescription() {
        return "This test case verifies that the correct numbers will be shown in the right panel after loading a test suite with complete configuration.";
    }
}
