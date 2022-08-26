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
package jthtest.New;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import junit.framework.Assert;

public class New3 extends New {

    /**
     * This test case verifies that choosing to create a new configuration will no
     * not start a new configuration if the start configuration editor checkbox is
     * unchecked.
     */

    public static void main(String[] args) {
        JUnitCore.main("jthtest.gui.New.New3");
    }

    @Test
    public void testNew3() throws InterruptedException {
        startTestRun(quickStartDialog);

        next(quickStartDialog);

        pickDefaultTestsuite(quickStartDialog);

        next(quickStartDialog);

        createConfiguration(quickStartDialog);

        next(quickStartDialog);

        pickTempWorkDir(quickStartDialog);

        next(quickStartDialog);

        finish(quickStartDialog, false, false);

        mainFrame = new JFrameOperator(WINDOWNAME);

        Assert.assertTrue(
                "choosing to create a new configuration will not start a new configuration if the start configuration editor checkbox is unchecked.",
                mainFrame.getOwnedWindows().length != 3);
    }

}