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
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

public class Configuration {

    public static final String STATUS_BAR_CONFIGURATION_FIELD_NAME = "bcc.Configuration";
    public static int MAX_WAIT_TIME = 10000;
    private final JTFrame mainFrame;
    private ConfigDialog configDialog;

    public Configuration(JTFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public boolean isLoaded() {
        return false;
    }

    public String getName() {
        if (isLoaded()) {
            return "";
        } else {
            return null;
        }
    }

    public ConfigurationBrowser openBrowser(boolean block) {
        JMenuItemOperator menu = mainFrame.getConfigure_LoadConfigurationMenu();
        if (block) {
            menu.push();
        } else {
            menu.pushNoBlock();
        }

        return ConfigurationBrowser.open();
    }

    public void load(String path, String name, boolean block) {
        ConfigurationBrowser browser = openBrowser(block);
        browser.setPath(path + File.separator + name);
        browser.commit();
        //waitForConfigurationLoading(name);
    }

    public void load(File file, boolean block) {
        ConfigurationBrowser browser = openBrowser(block);
        browser.setPath(file.getPath());
        browser.commit();
        waitForConfigurationLoading(file.getName());
    }

    public void load(String name, boolean block) {
        ConfigurationBrowser browser = openBrowser(block);
        browser.setPath(Tools.LOCAL_PATH + File.separator + name);
        browser.commit();
        waitForConfigurationLoading(name);
    }

    public ConfigDialog create(boolean block) {
        JMenuItemOperator menu = mainFrame.getConfigure_NewConfigurationMenu();
        if (block) {
            menu.push();
        } else {
            menu.pushNoBlock();
        }
        return findConfigEditorDialog();
    }

    public ConfigDialog openByKey() {
        mainFrame.getJFrameOperator().requestFocus();
        mainFrame.getJFrameOperator().clickMouse();
        Tools.pause(1);
        mainFrame.getJFrameOperator().pressKey(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK);
        return findConfigEditorDialog();
    }

    public ConfigDialog openByMenu(boolean block) {
        JMenuItemOperator menu = mainFrame.getConfigure_EditConfigurationMenu();
        if (block) {
            menu.push();
        } else {
            menu.pushNoBlock();
        }
        return findConfigEditorDialog();
    }

    public ConfigDialog findConfigEditorDialog() {
//    if (configDialog == null) {
//        configDialog = new ConfigDialog(mainFrame);
//    }

        return new ConfigDialog(mainFrame);
    }

    public JTextFieldOperator getConfigurationNameField() {
        return new JTextFieldOperator(mainFrame.getJFrameOperator(), new NameComponentChooser(STATUS_BAR_CONFIGURATION_FIELD_NAME));
    }

    public void waitForConfigurationLoading(String name) {
        JTextFieldOperator label = getConfigurationNameField();
        int time = 0;
        while (!label.getText().equals(name)) {
            try {
                Thread.sleep(100);
                time += 100;
                if (time > MAX_WAIT_TIME)
                    throw new JemmyException("Configuration loading error");
            } catch (InterruptedException ex) {
            }
        }
    }
}
