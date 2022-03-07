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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

import javax.swing.JTextField;

import jthtest.Tools;
import jthtest.Config_Edit.Config_Edit;

public class Test_Config_Edit3 extends Config_Edit {
    public static void main(String[] args) {
        JUnitCore.main("jthtest.gui.Sanity_Tests.Test_Config_Edit3");
    }

    /**
     * This test case Verify that \"Done\" button in an empty Configuration Editor
     * will bring up a Save Configuration file browser.
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
    @SuppressWarnings("deprecation")
    @Test
    public void testConfig_Edit7() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {

        startJavatestNewDesktop();
        JFrameOperator mainFrame = findMainFrame();
        openTestSuite(mainFrame);
        createWorkDirInTemp(mainFrame);

        openConfigFile(openLoadConfigDialogByMenu(mainFrame), CONFIG_NAME);
        waitForConfigurationLoading(mainFrame, CONFIG_NAME);
        openConfigCreation(mainFrame);
        JDialogOperator config = findConfigEditor(mainFrame);

        pushNextConfigEditor(config);
        new JTextFieldOperator(config, new NameComponentChooser("str.txt")).typeText("some_jti");

        pushDoneConfigEditor(config);
        saveConfig();
        openConfigDialogByKey(mainFrame);

        if (!findConfigEditor(mainFrame).getTitle().contains("some_jti")) {
            File f = new File(LOCAL_PATH + "jt_gui_test_temp_some_jti-this_file_will_be_deleted.jti");
            f.delete();
            throw new JemmyException("Wrong jti filename in config editor: " + findConfigEditor(mainFrame).getTitle());
        }
        File f = new File(LOCAL_PATH + "jt_gui_test_temp_some_jti-this_file_will_be_deleted.jti");
        f.delete();
    }

    /**
     * This method clicks on OK button on Save Configuration file browser and save
     * the configuration (.jti) file.
     *
     * @throws IOException Input/Output exceptions (I/O), and they occur whenever an
     *                     input or output operation is failed or interpreted.
     */
    @SuppressWarnings("deprecation")
    private void saveConfig() {
        File f = new File(LOCAL_PATH + "jt_gui_test_temp_some_jti-this_file_will_be_deleted.jti");
        if (f.exists())
            f.delete();
        JDialogOperator save = new JDialogOperator(getExecResource("ce.okToClose.title"));
        new JButtonOperator(save, "Ok").push();
        save = new JDialogOperator(getExecResource("ce.save.title"));
        JTextFieldOperator tf;

        tf = new JTextFieldOperator(
                (JTextField) Tools.getComponent(save, new String[] { "Folder name:", "File name:" }));
        tf.enterText("jt_gui_test_temp_some_jti-this_file_will_be_deleted");
    }

}
