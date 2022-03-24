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
import org.netbeans.jemmy.operators.JFrameOperator;

import jthtest.Config_Load.Config_Load;

public class Test_Config_Load1 extends ConfigTools {
    public static void main(String[] args) {
        JUnitCore.main("jthtest.gui.Config_Load.Config_Load1");
    }

    /**
     * This test is to verify that Load button under configuration will bring up a
     * file browser to select a jti file to be used.
     *
     * @throws ClassNotFoundException    If the class does not find it in the
     *                                   classpath.
     *
     * @throws InvocationTargetException Holds an exception thrown by an invoked
     *                                   method or constructor.
     *
     * @throws NoSuchMethodException     Occurs when a method is called that exists
     *                                   at compile-time, but does not exist at
     *                                   runtime.
     */
    @Test
    public void testConfig_Load1() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {

        startJavatestNewDesktop();
        JFrameOperator mainFrame = findMainFrame();
        closeQS(mainFrame);

        openTestSuite(mainFrame);
        createWorkDirInTemp(mainFrame);

        if (!(openLoadConfigDialogByMenu(mainFrame).isVisible())) {
            fail("Load button under configuration does not bring up a file browser to select a jti file to be used");
        }

    }
}
