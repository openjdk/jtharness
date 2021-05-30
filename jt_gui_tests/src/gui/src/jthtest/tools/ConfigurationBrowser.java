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
package jthtest.tools;

import jthtest.Tools;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author at231876
 */
public class ConfigurationBrowser {
    public static final String CONFIG_LOADER_DIALOG_NAME_EI18N = "wdc.loadconfig";
    public static final String CONFIG_LOADER_LOAD_BUTTON_NAME_EI18N = "wdc.load.btn";
    public static final String CONFIG_LOADER_CONFIG_LOCATION_DIALOG_EI18N = "wdc.configchoosertitle";
    public static final String CONFIG_LOADER_BROWSE_BUTTON = "Browse";

    private JDialogOperator browser;
    private boolean closed;

    public ConfigurationBrowser() {
	browser = new JDialogOperator(getLoadConfigurationDialogName());
	closed = false;
    }

    public static ConfigurationBrowser open() {
	return new ConfigurationBrowser();
    }

    public void setPath(String path) {
	new JButtonOperator(browser, CONFIG_LOADER_BROWSE_BUTTON).push();

	JDialogOperator fc = new JDialogOperator(getConfigLocationDialogName());
	JTextFieldOperator tf = new JTextFieldOperator(fc);
        tf.clearText();
	tf.enterText(path);
    }

    public void commit() {
	new JButtonOperator(browser, getConfigLoaderLoadButtonName()).push();
	closed = true;
    }

    public void cancel() {
	closed = true;
    }

    public static String getLoadConfigurationDialogName() {
	return Tools.getExecResource(CONFIG_LOADER_DIALOG_NAME_EI18N);
    }

    public static String getConfigLocationDialogName() {
	return Tools.getExecResource(CONFIG_LOADER_CONFIG_LOCATION_DIALOG_EI18N);
    }

    public static String getConfigLoaderLoadButtonName() {
	return Tools.getExecResource(CONFIG_LOADER_LOAD_BUTTON_NAME_EI18N);
    }
}
