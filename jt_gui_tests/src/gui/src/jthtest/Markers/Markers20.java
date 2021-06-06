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
/*
 * Start JavaTest with the -NewDesktop option. Create a workdirectory. Load an
 * existing JTI file. Bring up configuration editor by doing Ctrl-E. Select the
 * Enable Bookmarks from the Bookmarks menu. Select the couple of questions
 * from the history list. Mark the questions by selecting Mark Current Question
 * from the Bookmarks menu. Click on Show Only Marked Questions. All the marked
 * questions will be displayed as "...". Select one of the "...". Double click
 * on "..." in the list. The open group of questions will be displayed. Double
 * click again and the list will be disappear. Verify that all double click on
 * the group of questions that were open will close the group.
 */

package jthtest.Markers;

import java.lang.reflect.InvocationTargetException;
import jthtest.Config_Edit.Config_Edit;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author linfar
 */
public class Markers20 extends Markers {
    public static void main(String args[]) {
    JUnitCore.main("jthtest.gui.Markers.Markers20");
    }

    @Test
    public void testMarkers20() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
    startJavatestNewDesktop();

    JFrameOperator mainFrame = findMainFrame();

    closeQS(mainFrame);
    openTestSuite(mainFrame);
    createWorkDirInTemp(mainFrame);
    openConfigFile(openLoadConfigDialogByMenu(mainFrame), CONFIG_NAME);
    Config_Edit.waitForConfigurationLoading(mainFrame, CONFIG_NAME);

    openConfigDialogByKey(mainFrame);
    JDialogOperator config = findConfigEditor(mainFrame);

    int[] indexes = new int[]{4, 5, 6, 8, 9};

    pushEnableBookmarks(config);
    setBookmarkedByMenu(config, indexes);
    String[] namesAll = getElementsNames(config);
    pushShowOnlyBookmarked(config);
    String[] namesHidden = getElementsNames(config);

    openGroupByMouse(config, namesAll, namesHidden);
    closeGroupByMouse(config, namesAll, namesHidden);
    }
}
