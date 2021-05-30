/*
 * $Id$
 *
 * Copyright (c) 2009, 2011, Oracle and/or its affiliates. All rights reserved.
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
package jthtest.Config_New;

import java.io.File;
import jthtest.Test;
import jthtest.tools.ConfigDialog;
import jthtest.tools.JTFrame;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 *
 * @author linfar
 */
public class Config_New03 extends Test {
//    JDialogOperator config;

    ConfigDialog cd;

    public void testImpl() throws Exception {
	JTFrame mainFrame = new JTFrame(true);

	mainFrame.openDefaultTestSuite();
	addUsedFile(mainFrame.createWorkDirectoryInTemp());

	cd = mainFrame.getConfiguration().create(true);

	fillConfiguration();

	if (!cd.isFullConfiguration()) {
	    errors.add("");
	}

//	startJavatestNewDesktop();
//
//	JFrameOperator mainFrame = findMainFrame();
//	closeQS(mainFrame);
//
//	openTestSuite(mainFrame);
//	createWorkDirInTemp(mainFrame);
//
//	openConfigCreationBlock(mainFrame);
//	config = findConfigEditor(mainFrame);
//
//	fillConfiguration();
//	if(!isFullConfiguration(config))
//	    throw new JemmyException("Configuration is not full after filling while expected");
    }

    private void fillConfiguration() {
	JDialogOperator config = cd.getConfigDialog();
	cd.pushLastConfigEditor();
	new JTextFieldOperator(config, 1).typeText("some config");
	cd.pushLastConfigEditor();
	new JTextFieldOperator(config, 1).typeText("some description");
	cd.pushLastConfigEditor();
	JTableOperator table = new JTableOperator(config);
	table.clickOnCell(0, 0);
	cd.pushLastConfigEditor();
        String text; 
        if (File.separatorChar == '/') {
            text = System.getProperty("java.home") + "/bin/java";
        } else {
            text = System.getProperty("java.home") + "\\bin\\java.exe";
        }
	new JTextFieldOperator(config, new NameComponentChooser("file.txt")).typeText(text);
	cd.pushLastConfigEditor();


//        pushLastConfigEditor(config);
//        new JTextFieldOperator(config, 1).typeText("some config");
//        pushLastConfigEditor(config);
//        new JTextFieldOperator(config, 1).typeText("some description");
//	pushLastConfigEditor(config);
//	JTableOperator table = new JTableOperator(config);
//	table.clickOnCell(0, 0);
//	pushLastConfigEditor(config);
//	new JTextFieldOperator(config, new NameComponentChooser("file.txt")).typeText(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
//	pushLastConfigEditor(config);
    }
}
