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

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import jthtest.ConfigTools;

public class Test_Config_Save2 extends ConfigTools {
    public static void main(String args[]) {
        JUnitCore.main("jthtest.gui.Config_SaveEdit.Test_Config_SaveEdit2");
    }
   /**
     * Verify that "File->Save" menu item in Configuration Editor will save the
     * current configuration in a new file if there is no file name.
     *
     * @throws ClassNotFoundException
     *
     * @throws InvocationTargetException
     *
     * @throws NoSuchMethodException
     *
     */
    @Test
    public void testConfig_SaveEdit2()
            throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InterruptedException {
        File f = new File(LOCAL_PATH + "jt_gui_test_Config_SaveEdit2_test_this_file_will_be_deleted.jti");
        if (f.exists())
            f.delete();
        f = null;
        startJavatestNewDesktop();
        JFrameOperator mainFrame = findMainFrame();
        closeQS(mainFrame);

        openTestSuite(mainFrame);
        createWorkDirInTemp(mainFrame);
        openConfigCreation(mainFrame);
        JDialogOperator config = findConfigEditor(mainFrame);
        saveConfig(config, "jt_gui_test_Config_SaveEdit2_test_this_file_will_be_deleted");

        f = new File(LOCAL_PATH + "jt_gui_test_Config_SaveEdit2_test_this_file_will_be_deleted.jti");
        int t = 0;
        while (t < 1000 && !f.exists()) {
            f = new File(LOCAL_PATH + "jt_gui_test_Config_SaveEdit2_test_this_file_will_be_deleted.jti");
            t += 100;
            Thread.sleep(100);
        }
        if (!f.exists())
            throw new JemmyException("File was not created");
        f.delete();
    }
}
