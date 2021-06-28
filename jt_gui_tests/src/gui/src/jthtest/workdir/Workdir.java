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
package jthtest.workdir;

import java.io.File;
import javax.swing.JTextField;
import jthtest.menu.Menu;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import static jthtest.Tools.*;

/**
 *
 * @author at231876
 */
public class Workdir {

    public static String TO_DELETE_TEMP_WD_NAME = "some_temp_wd_that_will_be_deleted";
    public static final String OPEN_DIALOG_TI18N_NAME = "wdc.open.title";
    public static final String CREATE_DIALOG_TI18N_NAME = "wdc.new.title";
    public static final String COMMIT_BUTTON_EI18N_NAME = "wdc.create.btn";
    public static final String[] TEXT_FIELD_NAMES = {"Folder name:", "File name:", "Folder Name:", "File Name:"};

    public static String getOpenWorkDirectoryDialogName() {
    return getToolResource(OPEN_DIALOG_TI18N_NAME);
    }

    public static String getCreateWorkDirectoryDialogName() {
    return getToolResource(CREATE_DIALOG_TI18N_NAME);
    }

    public static String getCommitButtonName() {
    return getExecResource("wdc.create.btn");
    }

    public static JDialogOperator openOpenWorkDirectoryDialog(JFrameOperator frame) {
    Menu.getFile_Open_WorkDirectoryMenu(frame).push();
    return new JDialogOperator(frame, getOpenWorkDirectoryDialogName());
    }

    public static JDialogOperator openCreateWorkDirectoryDialog(JFrameOperator frame) {
    Menu.getFile_CreateWorkDirectoryMenu(frame).push();
    return new JDialogOperator(frame, getCreateWorkDirectoryDialogName());
    }

    public static void chooseWorkDirectoryInDialog(JDialogOperator wdDialog, String wdPath) {
    JTextFieldOperator fileField = new JTextFieldOperator((JTextField) getComponent(wdDialog, TEXT_FIELD_NAMES));
    fileField.clearText();
    fileField.typeText(wdPath);
    }

    public static void commitWorkDirectoryDialog(JDialogOperator wdDialog) {
    new JButtonOperator(wdDialog, getCommitButtonName()).push();
    }

    public static void openWorkDirectory(String wdPath, JFrameOperator frame) {
    JDialogOperator wdDialog = openOpenWorkDirectoryDialog(frame);
    JTextFieldOperator fileField = new JTextFieldOperator((JTextField) getComponent(wdDialog, TEXT_FIELD_NAMES));
    fileField.enterText(wdPath);
    }

    public static void createWorkDirectory(String wdPath, JFrameOperator frame) {
    JDialogOperator wdDialog = openCreateWorkDirectoryDialog(frame);
    JTextFieldOperator fileField = new JTextFieldOperator((JTextField) getComponent(wdDialog, TEXT_FIELD_NAMES));
    fileField.enterText(wdPath);
    }
    public static void createWorkDirectory(String wdPath, boolean delete, JFrameOperator frame) {
    if(delete) {
        deleteDirectory(wdPath);
    }
    JDialogOperator wdDialog = openCreateWorkDirectoryDialog(frame);
    JTextFieldOperator fileField = new JTextFieldOperator((JTextField) getComponent(wdDialog, TEXT_FIELD_NAMES));
    fileField.enterText(wdPath);
    }

    public static String createWorkDirectoryInTemp(JFrameOperator mainFrame) {
    int attempts = 0;

    JDialogOperator wdDialog = openCreateWorkDirectoryDialog(mainFrame);

    String path = TEMP_PATH + TEMP_WD_NAME;
    while (attempts < 10) {
        File file = new File(path);
        if (!file.exists()) {
        break;
        }
        deleteDirectory(file);
        file = new File(path);
        if (!file.exists()) {
        break;
        }
        path = TEMP_PATH + TEMP_WD_NAME + (int) (Math.random() * 10000);
        attempts++;
    }
    if (attempts >= 10) {
        throw new JemmyException("error");
    }
    JTextFieldOperator tf;

    tf = new JTextFieldOperator((JTextField) getComponent(wdDialog, new String[]{"Folder name:", "File name:", "Folder Name:", "File Name:"}));
    tf.enterText(path);
    return path;
    }

    public static boolean verifyWorkdirCreation() {
    return new File(TEMP_PATH + TEMP_WD_NAME).exists();
    }

    public static boolean verifyWorkdirCreation(String path) {
    return new File(path).exists();
    }
}
