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

package jthtest.Config_Load;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.operators.JFrameOperator;

public class Config_Load1 extends Config_Load {
    public static void main(String[] args) {
        JUnitCore.main("jthtest.gui.Config_Load.Config_Load1");
    }

    @Test
    public void testConfig_Load1() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {

        // Start Java Test application with -newDesktop
        startJavatestNewDesktop();

        // get the reference of mainframe
        JFrameOperator mainFrame = findMainFrame();

        // Close Quick Start
        closeQS(mainFrame);

        // Open default test suite
        openTestSuite(mainFrame);

        // Create work directory
        createWorkDirInTemp(mainFrame);

        // Verify that file chooser should be displayed for loading an existing jti
        // file.
        openLoadConfigDialogByMenu(mainFrame);
    }

        // TestCase Description
        public String getDescription() {
            return "This test case verifies that Load button under configure menu will bring up a file chooser to select a jti file to be used.";
        }
}
