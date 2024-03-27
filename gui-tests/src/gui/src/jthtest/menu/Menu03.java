/*
 * $Id$
 *
 * Copyright (c) 2009, 2024, Oracle and/or its affiliates. All rights reserved.
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

package jthtest.menu;

import jthtest.Test;
import static jthtest.Tools.*;
import static jthtest.menu.Menu.*;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;

public class Menu03 extends Test {

    @Override
    public void testImpl() throws Exception {
	startJavatest();

	JFrameOperator mainFrame = findMainFrame();
	openTestSuite(mainFrame);

	JMenuItemOperator item;
	getFile_CreateWorkDirectoryMenu(mainFrame);
	getConfigureMenu(mainFrame);
	item = getConfigure_EditConfigurationMenu(mainFrame);
	if(item.isEnabled())
	    errors.add("Configure->Edit Configuration menu is enabled while expected to be disabled");
	item = getConfigure_EditQuickSetMenu(mainFrame);
	if(item.isEnabled())
	    errors.add("Configure->Edit Quick Set menu is enabled while expected to be disabled");
	getConfigure_LoadConfigurationMenu(mainFrame);
	getConfigure_LoadRecentConfigurationMenu(mainFrame);
	getConfigure_NewConfigurationMenu(mainFrame);

	getRunTestsMenu(mainFrame);
	getRunTests_MonitorProgressMenu(mainFrame);
	getRunTests_StartMenu(mainFrame);
	getRunTests_StopMenu(mainFrame);

	getReportMenu(mainFrame);
	getReport_CreateReportMenu(mainFrame);
	getReport_OpenReportMenu(mainFrame);

	getViewMenu(mainFrame);
	getView_ConfigurationMenu(mainFrame);
	getView_FilterMenu(mainFrame);
	getView_LogsMenu(mainFrame);
	getView_PropertiesMenu(mainFrame);
	getView_TestSuiteErrorsMenu(mainFrame);
	
    }

}
