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

import java.lang.reflect.InvocationTargetException;
import jthtest.Config_Edit.Config_Edit;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 *
 * @author linfar
 */
public class Config_Load4 extends Config_Load {
    private JFrameOperator mainFrame;
    
    public static void main(String[] args) {
	JUnitCore.main("jthtest.gui.Config_Load.Config_Load4");
    }
    
    @Test
    public void testConfig_Load4() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
	startJavatestNewDesktop();
	
	mainFrame = findMainFrame();
	
	openTestSuite(mainFrame);
	
	createWorkDirInTemp(mainFrame);
	
	openConfigFile(openLoadConfigDialogByMenu(mainFrame), CONFIG_NAME);
	Config_Edit.waitForConfigurationLoading(mainFrame, CONFIG_NAME);
	
	openConfigDialogByKey(mainFrame);
	
	pushDoneConfigEditor(findConfigEditor(mainFrame));
	
	openConfigFile(openLoadConfigDialogByMenu(mainFrame), SECOND_CONFIG_NAME);
	Config_Edit.waitForConfigurationLoading(mainFrame, SECOND_CONFIG_NAME);

	openConfigDialogByKey(mainFrame);
	
	if(!verifyOpeningNewConfigFile()) {
	    throw new JemmyException("Old config is shown in config editor");
	}
    }
    
    private boolean verifyOpeningNewConfigFile() {
	JDialogOperator config = findConfigEditor(mainFrame);
	JListOperator list = new JListOperator(config);
	list.selectItem(1);
	return new JTextFieldOperator(config, new NameComponentChooser("str.txt")).getText().equals(SECOND_CONFIG_NAME);
    }
}
