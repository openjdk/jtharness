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
package jthtest.Config_Load;

import java.awt.Component;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import jthtest.ConfigTools;
import jthtest.Test;
import jthtest.Tools;
import jthtest.tools.Configuration;
import jthtest.tools.JTFrame;
import org.netbeans.jemmy.operators.JMenuItemOperator;

/**
 *
 * @author linfar
 */
public class Config_Load06 extends Test {
//    private JFrameOperator mainFrame;

    public void testImpl() throws Exception {
	JTFrame mainFrame = new JTFrame(true);

	mainFrame.openDefaultTestSuite();
	addUsedFile(mainFrame.createWorkDirectoryInTemp());

	Configuration configuration = mainFrame.getConfiguration();
	configuration.load(Tools.CONFIG_NAME, true);
	configuration.load(ConfigTools.SECOND_CONFIG_NAME, true);

	checkMenu(mainFrame);

//	startJavatestNewDesktop();
//
//	mainFrame = findMainFrame();
//
//	openTestSuite(mainFrame);
//
//	createWorkDirInTemp(mainFrame);
//
//	openConfigFile(openLoadConfigDialogByMenu(mainFrame), DEFAULT_JTI);
//	openConfigFile(openLoadConfigDialogByMenu(mainFrame), SECOND_JTI);
//
//	checkMenu();
    }

//    private void checkMenu() {
    private void checkMenu(JTFrame mainFrame) {
	JMenuItemOperator item = mainFrame.getConfigure_LoadRecentConfigurationMenu();
	if(!item.isEnabled()) {
	    errors.add("Configuration -> Load Recent Configuration menu is not enabled while expected");
	    return;
	}
	JMenuItemOperator[] elements = mainFrame.getConfigure_LoadRecentConfiguration_subMenu();
	if (elements.length != 2) {
	    errors.add("Count of elements is " + elements.length + " while expected 2");
	}
	if (elements.length > 0) {
	    if (!elements[0].getText().endsWith(ConfigTools.SECOND_CONFIG_NAME)) {
		errors.add("First element in the list is not 'democonfig_second.jti'");
	    }
	}
	if (elements.length > 1) {
	    if (!elements[1].getText().endsWith(ConfigTools.CONFIG_NAME)) {
		errors.add("Second element in the list is not 'democonfig.jti'");
	    }
	}

//	JMenuItem item = new JMenuOperator(mainFrame, getExecResource("ch.menu")).pushMenu(("ch.menu") + "|" + getExecResource("ce.history.menu"), "|");
//	MenuElement[] elements = item.getSubElements()[0].getSubElements();
//	if (elements.length != 2) {
//	    throw new JemmyException("Count of elements is not 2");
//	}
//	if (!((JMenuItem) elements[0]).getText().endsWith(SECOND_JTI)) {
//	    throw new JemmyException("First element in the list is not 'democonfig_second.jti'");
//	}
//	if (!((JMenuItem) elements[1]).getText().endsWith(DEFAULT_JTI)) {
//	    throw new JemmyException("Second element in the list is not 'democonfig.jti'");
//	}
    }

    @Override
    public String getDescription() {
	return "This test loads 2 different configuration files and checks that Recent Configuration menu contains both of them";
    }
}
