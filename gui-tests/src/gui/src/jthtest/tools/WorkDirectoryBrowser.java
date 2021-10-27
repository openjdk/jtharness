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


package jthtest.tools;

import jthtest.Tools;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import javax.swing.*;
import java.io.File;

public class WorkDirectoryBrowser {

    public static final String OPEN_DIALOG_TI18N_NAME = "wdc.open.title";
    public static final String CREATE_DIALOG_TI18N_NAME = "wdc.new.title";
    public static final String CREATE_BUTTON_TI18N_NAME = "wdc.new.btn";
    public static final String OPEN_BUTTON_TI18N_NAME = "wdc.open.btn";
    public static final String CANCEL_BUTTON_TI18N_NAME = "uif.cancel.btn";
    public static final String[] TEXT_FIELD_NAMES = {"Folder name:", "File name:", "Folder Name:", "File Name:"};
    private Types type;
    private JDialogOperator dialog;
    private boolean opened = false;
    private String path = "";
    private String name = "";
    private String fullPath = "";

    private WorkDirectoryBrowser(Types type) {
        this.type = type;
        switch (type) {
            case CREATE:
                dialog = new JDialogOperator(getCreateWorkDirectoryDialogName());
                break;
            case OPEN:
                dialog = new JDialogOperator(getOpenWorkDirectoryDialogName());
                break;
        }
        opened = true;
    }

    ;

    public static WorkDirectoryBrowser create() {
        return new WorkDirectoryBrowser(Types.CREATE);
    }

    public static WorkDirectoryBrowser open() {
        return new WorkDirectoryBrowser(Types.OPEN);
    }

    public static String getOpenWorkDirectoryDialogName() {
        return Tools.getToolResource(OPEN_DIALOG_TI18N_NAME);
    }

    public static String getCreateWorkDirectoryDialogName() {
        return Tools.getToolResource(CREATE_DIALOG_TI18N_NAME);
    }

    public static String getCancelButtonName() {
        return Tools.getToolResource(CANCEL_BUTTON_TI18N_NAME);
    }

    public void setPath(String path, String name) {
        if (opened) {
            if (("true").equalsIgnoreCase(System.getProperty("com.sun.javatest.exec.templateMode"))) {
                setPath(path);
                setName(name);
            } else {
                this.path = path;
                this.name = name;
                fullPath = path + File.separator + name;
                JTextFieldOperator tf = getFileTextField();
                tf.clearText();
                tf.typeText(fullPath);
            }
        }
    }

    public void setPath(String path) {
        this.path = path + File.separator;
        if (name != null) {
            fullPath = this.path + name;
        } else {
            fullPath = this.path;
        }
        if (opened) {
            if (("true").equalsIgnoreCase(System.getProperty("com.sun.javatest.exec.templateMode"))) {
                JButtonOperator op = new JButtonOperator(dialog, "Browse...");
                op.push();
                JDialogOperator d = new JDialogOperator("Please select work directory");
                JTextFieldOperator tf = new JTextFieldOperator(d);
                tf.clearText();
                tf.enterText(this.path);
            } else {
                JTextFieldOperator tf = getFileTextField();
                tf.clearText();
                tf.typeText(fullPath);
            }
        }
    }

    public void setFullPath(String path) {
        int separator = path.lastIndexOf(File.separator);
        if (separator != -1) {
            setPath(path.substring(0, separator));
        }
        setName(path.substring(separator + 1, path.length()));
    }

    public void setName(String name) {
        this.name = name;
        if (path != null) {
            fullPath = path + name;
        } else {
            fullPath = name;
        }
        if (opened) {
            if (("true").equals(System.getProperty("com.sun.javatest.exec.templateMode"))) {
//                JTextFieldOperator tf = new JTextFieldOperator((JTextField) Tools.getComponent(dialog, new String[]{"Work Directory Name:"}));
                JTextFieldOperator tf = new JTextFieldOperator(dialog);
                tf.clearText();
                tf.typeText(this.name);
            } else {
                JTextFieldOperator tf = getFileTextField();
                tf.clearText();
                tf.typeText(fullPath);
            }
        }
    }

    public void cancel() {
        getCancelButton().push();
    }

    public void commit() {
        getCommitButton().push();
    }

    public JButtonOperator getCancelButton() {
        return new JButtonOperator(dialog, getCancelButtonName());
    }

    public JButtonOperator getCommitButton() {
        return new JButtonOperator(dialog, getCommitButtonName());
    }

    private JTextFieldOperator getFileTextField() {
        return new JTextFieldOperator((JTextField) Tools.getComponent(dialog, TEXT_FIELD_NAMES));
    }

    public String getCommitButtonName() {
        switch (type) {
            case CREATE:
                return Tools.getToolResource(CREATE_BUTTON_TI18N_NAME);
            case OPEN:
                return Tools.getToolResource(OPEN_BUTTON_TI18N_NAME);
            default:
                return "Create";
        }
    }

    public boolean isOpened() {
        return opened;
    }

    public static enum Types {

        CREATE, OPEN
    }
}
