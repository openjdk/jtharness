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
/*
 *  Start JavaTest with the -NewDesktop option. Create a new workdirectory.
 * Bring up Bring up configuration editor by doing Ctrl-E. Answer one question
 * in the interview. Click on Done button. A Save configuration file chooser
 * will display. Enter a valid jti name. Bring up Bring up configuration editor
 * by doing Ctrl-E. The name of the jti file should be displayed. A Save
 * Configuration File chooser should be display to save the jti file.
 */

package jthtest.Config_Edit;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JTextField;
import jthtest.Tools;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 *
 * @author linfar
 */
public class Config_Edit7 extends Config_Edit {
    public static void main(String args[])  {
    JUnitCore.main("jthtest.gui.Config_Edit.Config_Edit7");
    }

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
    if(!findConfigEditor(mainFrame).getTitle().contains("some_jti")) {
        File f = new File(LOCAL_PATH + "jt_gui_test_temp_some_jti-this_file_will_be_deleted.jti");
        f.delete();
        throw new JemmyException("Wrong jti filename in config editor: " + findConfigEditor(mainFrame).getTitle());
    }
        File f = new File(LOCAL_PATH + "jt_gui_test_temp_some_jti-this_file_will_be_deleted.jti");
        f.delete();
    }

    private void saveConfig() {
    File f = new File(LOCAL_PATH + "jt_gui_test_temp_some_jti-this_file_will_be_deleted.jti");
    if(f.exists())
        f.delete();
    JDialogOperator save = new JDialogOperator(getExecResource("ce.okToClose.title"));
    new JButtonOperator(save, "Ok").push();
    save = new JDialogOperator(getExecResource("ce.save.title"));
    JTextFieldOperator tf;

    tf = new JTextFieldOperator((JTextField)Tools.getComponent(save, new String[] {"Folder name:", "File name:"}));
    tf.enterText("jt_gui_test_temp_some_jti-this_file_will_be_deleted");
    }
}
