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

import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;

public class Test_Open_Test_Suite1 extends Open_Test_Suite {

    public static void main(String[] args) {
        JUnitCore.main("jthtest.gui.Sanity_Tests.Test_Open_Test_Suite1");
    }

    @Test
    public void testOpenTestSuite1() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {

        // Function in Tools.java used to open test suits
        openTestSuite(mainFrame);
        // Find the "DemoTS" tab
        // Wait until "some test cases are yet to run" appears in text field.
        waitForWDLoading(mainFrame, WDLoadingResult.SOME_NOTRUN);
        JTabbedPaneOperator tab = new JTabbedPaneOperator(mainFrame, TAB_CAPTION);
        if (!tab.isVisible()) {
            fail("Error while loading test suite.");
        }
    }

}