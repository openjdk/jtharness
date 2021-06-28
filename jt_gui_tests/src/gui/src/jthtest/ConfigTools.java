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
package jthtest;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.JLabel;
import javax.swing.JTextField;
import jthtest.menu.Menu;
import jthtest.tools.JTFrame;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JMenuOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author linfar
 */
public class ConfigTools extends Tools {

    public static class ConfigDialog {

    private JDialogOperator config;
    private JTFrame mainFrame;

    private ConfigDialog(JTFrame mainFrame) {
        this.mainFrame = mainFrame;
        config = new JDialogOperator(mainFrame.getJFrameOperator(), getExecResource("ce.name"));
    }

    public static ConfigDialog openConfigCreation(JTFrame mainFrame) {
        new JMenuOperator(mainFrame.getJFrameOperator(), getExecResource("ch.menu")).pushMenuNoBlock(new String[]{getExecResource("ch.menu"), getExecResource("ch.new.act")});
        return new ConfigDialog(mainFrame);
    }

    public static ConfigDialog openConfigCreationBlock(JTFrame mainFrame) {
        new JMenuOperator(mainFrame.getJFrameOperator(), getExecResource("ch.menu")).pushMenu(new String[]{getExecResource("ch.menu"), getExecResource("ch.new.act")});
        return new ConfigDialog(mainFrame);
    }

    public static ConfigDialog openConfigDialogByKey(JTFrame mainFrame) {
        mainFrame.getJFrameOperator().pressKey(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK);
        return new ConfigDialog(mainFrame);
    }

    public static ConfigDialog openConfigDialogByMenu(JTFrame mainFrame) {
        Menu.getConfigure_EditConfigurationMenu(mainFrame.getJFrameOperator());
        return new ConfigDialog(mainFrame);
    }

    public void pushDoneConfigEditor() {
        new JButtonOperator(config, DONE_BUTTON).push();
    }

    public void pushNextConfigEditor() {
        new JButtonOperator(config, NEXT_BUTTON).push();
    }

    public void pushBackConfigEditor() {
        new JButtonOperator(config, PREV_BUTTON).push();
    }

    public void pushLastConfigEditor() {
        new JButtonOperator(config, LAST_BUTTON).push();
    }

    public void selectQuestion(int index) {
        new JListOperator(config).selectItem(index);
    }

    public void saveConfig(String name) {
        getFile_SaveMenu(config).pushNoBlock();
        JDialogOperator saving = new JDialogOperator(getSaveConfigurationDialogName());

        JTextFieldOperator tf;

        tf = new JTextFieldOperator((JTextField) Tools.getComponent(saving, new String[]{"Folder name:", "File name:", "Folder Name:", "File Name:"}));
        tf.enterText(name);
    }

    public boolean isFullConfiguration() {
        JListOperator list = new JListOperator(config);
        return ((JLabel) list.getRenderedComponent(list.getModel().getSize() - 1)).getText().equals(" Congratulations!");
    }
    }
    public static final String SECOND_CONFIG_NAME = "democonfig_second.jti";
    public static final String DONE_BUTTON = "Done";
    public static final String NEXT_BUTTON = "Next";
    public static final String PREV_BUTTON = "Back";
    public static final String LAST_BUTTON = "Last";
    public static final String CONFIG_LOADER_DIALOG_NAME_EI18N = "wdc.loadconfig";
    public static final String CONFIG_LOADER_LOAD_BUTTON_NAME_EI18N = "wdc.load.btn";
    public static final String CONFIG_LOADER_CONFIG_LOCATION_DIALOG_EI18N = "wdc.configchoosertitle";
    public static final String CONFIG_LOADER_BROWSE_BUTTON = "Browse";
    public static final String SAVE_CONFIG_FILE_DIALOG_NAME_EI18N = "ce.save.title";

    public boolean isFullConfiguration(JDialogOperator config) {
    JListOperator list = new JListOperator(config);
    return ((JLabel) list.getRenderedComponent(list.getModel().getSize() - 1)).getText().equals(" Congratulations!");
    }

    public static int findRow(JTableOperator table, String searching) {
    int i;
    for (i = 0; i < table.getRowCount(); i++) {
        if (table.getValueAt(i, 0).toString().equals(searching)) {
        return i;
        }
    }
    return -1;
    }

    public static JDialogOperator openLoadConfigDialogByMenu(JFrameOperator mainFrame) {
    Menu.getConfigure_LoadConfigurationMenu(mainFrame).pushNoBlock();
    return new JDialogOperator(getLoadConfigurationDialogName());
    }

    public static void openConfigDialogByKey(JFrameOperator mainFrame) {
    mainFrame.requestFocus();
    mainFrame.clickMouse();
        pause(1);
    mainFrame.requestFocus();
    mainFrame.pressKey(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK);
    }

    public static void openConfigDialogByMenu(JFrameOperator mainFrame) {
    Menu.getConfigure_EditConfigurationMenu(mainFrame);
    }

    public static void pushDoneConfigEditor(JDialogOperator config) {
    new JButtonOperator(config, DONE_BUTTON).push();
    }

    public static void pushNextConfigEditor(JDialogOperator config) {
    new JButtonOperator(config, NEXT_BUTTON).push();
    }

    public static void pushBackConfigEditor(JDialogOperator config) {
    new JButtonOperator(config, PREV_BUTTON).push();
    }

    public static void pushLastConfigEditor(JDialogOperator config) {
    new JButtonOperator(config, LAST_BUTTON).push();
    }

    public static void openConfigFile(JDialogOperator fileChooser, String path, String name) {
    new JButtonOperator(fileChooser, CONFIG_LOADER_BROWSE_BUTTON).push();

    JDialogOperator fc = new JDialogOperator(getConfigLocationDialogName());
    JTextFieldOperator tf = new JTextFieldOperator(fc);
    tf.enterText(path + File.separator + name);

    new JButtonOperator(fileChooser, getConfigLoaderLoadButtonName()).push();
    }

    public static void openConfigFile(JDialogOperator fileChooser, String name) {
    openConfigFile(fileChooser, LOCAL_PATH, name);
    }

    public static void openConfigCreation(JFrameOperator mainFrame) {
    Menu.getConfigure_NewConfigurationMenu(mainFrame).pushNoBlock();
    }

    public static void openConfigCreationBlock(JFrameOperator mainFrame) {
    Menu.getConfigure_NewConfigurationMenu(mainFrame).push();
    }

    public static void selectQuestion(JDialogOperator config, int index) {
    new JListOperator(config).selectItem(index);
    }

    public static void saveConfig(JDialogOperator config, String name) {
    getFile_SaveMenu(config).pushNoBlock();
    JDialogOperator saving = new JDialogOperator(getSaveConfigurationDialogName());

    JTextFieldOperator tf;

    tf = new JTextFieldOperator((JTextField) Tools.getComponent(saving, new String[]{"Folder name:", "File name:", "File Name:", "Folder Name:"}));
    tf.enterText(name);
    }

    public static JMenuBarOperator getMenu(JDialogOperator configDialog) {
    return new JMenuBarOperator(configDialog);
    }

    public static JMenuItemOperator getFile_SaveMenu(JDialogOperator configDialog) {
    return getMenu(configDialog).showMenuItem(new String[]{getFileMenuName(), getFile_SaveMenuName()});
    }

    public static String getLoadConfigurationDialogName() {
    return getExecResource(CONFIG_LOADER_DIALOG_NAME_EI18N);
    }

    public static String getSaveConfigurationDialogName() {
    return getExecResource(SAVE_CONFIG_FILE_DIALOG_NAME_EI18N);
    }

    public static String getConfigLocationDialogName() {
    return getExecResource(CONFIG_LOADER_CONFIG_LOCATION_DIALOG_EI18N);
    }

    public static String getConfigLoaderLoadButtonName() {
    return getExecResource(CONFIG_LOADER_LOAD_BUTTON_NAME_EI18N);
    }

    public static String getFileMenuName() {
    return "File";
    }

    public static String getFile_SaveMenuName() {
    return "Save";
    }
}
