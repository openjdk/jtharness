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
/*
 * Start JavaTest with the -NewDesktop option. Create a workdirectory. Load an 
 * existing JTI file. Bring up configuration editor by doing Ctrl-E. Select the 
 * Enable Bookmarks from the Bookmarks menu. Select the first question from the 
 * history list. Mark the question by selecting Mark Current Question from the 
 * Bookmarks menu. Select Clear the Answer for the Current Question from the 
 * Bookmarks menu. Verify that the answer for selected question will be set to 
 * empty. 
 */
package jthtest.Markers;

import java.lang.reflect.InvocationTargetException;
import jthtest.Test;
import jthtest.tools.ConfigDialog;
import jthtest.tools.Configuration;
import jthtest.tools.JTFrame;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 *
 * @author linfar
 */
public class Markers10 extends Test {

    public void testImpl() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        mainFrame = new JTFrame(true);

        mainFrame.openDefaultTestSuite();
        addUsedFile(mainFrame.createWorkDirectoryInTemp());
        Configuration configuration = mainFrame.getConfiguration();
        configuration.load(CONFIG_NAME, true);
        ConfigDialog cd = configuration.openByKey();

        // test body
        cd.getBookmarks_EnableBookmarks().push();
        cd.selectQuestion(2);
        JTextFieldOperator op = new JTextFieldOperator(cd.getConfigDialog(), new NameComponentChooser("str.txt"));
        op.typeText("some description that must be cleared");
        cd.setBookmarkedByMenu(2);
        cd.clearByMenu(2);

        op = new JTextFieldOperator(cd.getConfigDialog(), new NameComponentChooser("str.txt"));
        if (!op.getText().equals("")) {
            errors.add("Text wasn't cleared up: '" + op.getText() + "' while expected ''");
        }
        warnings.add("Pre-defined warning: Mark sometimes desapperas while clearing by menu - bookmark saves current state of answer; First question is 'Configuratoin name' and it can't be cleared; a new question is generated while clearing up");

//	startJavatestNewDesktop();
//	
//	JFrameOperator mainFrame = findMainFrame();
//	
//	closeQS(mainFrame);
//	openTestSuite(mainFrame);
//	createWorkDirInTemp(mainFrame);
//	openConfigFile(openLoadConfigDialogByMenu(mainFrame), CONFIG_NAME);
//	Config_Edit.waitForConfigurationLoading(mainFrame, CONFIG_NAME);
//	
//	openConfigDialogByKey(mainFrame);
//	JDialogOperator config = findConfigEditor(mainFrame);
//
//	pushEnableBookmarks(config);
//	
//	selectQuestion(config, 2);
//	new JTextFieldOperator(config, new NameComponentChooser("str.txt")).typeText("some description that must be cleared");
//	setBookmarkedByMenu(config, 2);
//	clearByMenu(config, 2);
//	
//	if(!new JTextFieldOperator(config, new NameComponentChooser("str.txt")).getText().equals(""))
//	    throw new JemmyException("Text wasn't cleared up: '" + new JTextFieldOperator(config, new NameComponentChooser("str.txt")).getText() + "' while expected ''");
//	System.out.println("Pre-defined warning: Mark sometimes desapperas while clearing by menu - bookmark saves current state of answer; First question is 'Configuratoin name' and it can't be cleared; a new question is generated while clearing up");
    }

    @Override
    public String getDescription() {
        return "Start JavaTest with the -NewDesktop option. Create a workdirectory. Load an existing JTI file. Bring up configuration editor by doing Ctrl-E. Select the Enable Bookmarks from the Bookmarks menu. Select the first question from the history list. Mark the question by selecting Mark Current Question from the Bookmarks menu. Select Clear the Answer for the Current Question from the Bookmarks menu. Verify that the answer for selected question will be set to empty.";
    }
}
