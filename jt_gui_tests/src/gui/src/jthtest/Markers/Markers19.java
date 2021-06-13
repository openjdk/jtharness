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
 * Enable Bookmarks from the Bookmarks menu. Select the couple of questions
 * from the history list. Mark the questions by selecting Mark Current Question
 * from the Bookmarks menu. Click on Show Only Marked Questions. All the marked
 * questions will be displayed as "...". Select one of the "...". Double click
 * on "..." in the list. The open group of questions will be displayed. Verify
 * that all double click on the "..." in the list will open the the group
 * questions.
 */
package jthtest.Markers;

import java.lang.reflect.InvocationTargetException;
import jthtest.Test;
import jthtest.tools.ConfigDialog;
import jthtest.tools.Configuration;
import jthtest.tools.JTFrame;

/**
 *
 * @author linfar
 */
public class Markers19 extends Test {

    public void testImpl() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        mainFrame = new JTFrame(true);

        mainFrame.openDefaultTestSuite();
        addUsedFile(mainFrame.createWorkDirectoryInTemp());
        Configuration configuration = mainFrame.getConfiguration();
        configuration.load(CONFIG_NAME, true);

        ConfigDialog config = configuration.openByKey();

        int[] indexes = new int[]{4, 5, 6, 8, 9};
        config.getBookmarks_EnableBookmarks().push();

        config.setBookmarkedByMenu(indexes);
        String[] namesAll = config.getElementsNames();
        config.getBookmarks_ShowOnlyBookmarkedMenu().push();
        String namesHidden[] = config.getElementsNames();

        config.openGroupByMouse(namesAll, namesHidden);

//    startJavatestNewDesktop();
//
//    JFrameOperator mainFrame = findMainFrame();
//
//    closeQS(mainFrame);
//    openTestSuite(mainFrame);
//    createWorkDirInTemp(mainFrame);
//    openConfigFile(openLoadConfigDialogByMenu(mainFrame), CONFIG_NAME);
//    Config_Edit.waitForConfigurationLoading(mainFrame, CONFIG_NAME);
//
//    openConfigDialogByKey(mainFrame);
//    JDialogOperator config = findConfigEditor(mainFrame);
//
//    int[] indexes = new int[]{4, 5, 6, 8, 9};
//
//    pushEnableBookmarks(config);
//    setBookmarkedByMenu(config, indexes);
//    String[] namesAll = getElementsNames(config);
//    pushShowOnlyBookmarked(config);
//    String[] namesHidden = getElementsNames(config);
//
//    openGroupByMouse(config, namesAll, namesHidden);
    }

    @Override
    public String getDescription() {
        return "Start JavaTest with the -NewDesktop option. Create a workdirectory. Load an existing JTI file. Bring up configuration editor by doing Ctrl-E. Select the Enable Bookmarks from the Bookmarks menu. Select the couple of questions from the history list. Mark the questions by selecting Mark Current Question from the Bookmarks menu. Click on Show Only Marked Questions. All the marked questions will be displayed as \"...\". Select one of the \"...\". Double click on \"...\" in the list. The open group of questions will be displayed. Verify that all double click on the \"...\" in the list will open the the group questions.";
    }
}
