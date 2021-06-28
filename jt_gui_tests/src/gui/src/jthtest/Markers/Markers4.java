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
package jthtest.Markers;

import java.lang.reflect.InvocationTargetException;
import javax.swing.Icon;
import jthtest.Test;
import jthtest.tools.ConfigDialog;
import jthtest.tools.Configuration;
import jthtest.tools.JTFrame;

/**
 *
 * @author linfar
 */
public class Markers4 extends Test {

    public void testImpl() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        mainFrame = new JTFrame(true);

        mainFrame.openDefaultTestSuite();
        addUsedFile(mainFrame.createWorkDirectoryInTemp());
        Configuration config = mainFrame.getConfiguration();
        config.load(CONFIG_NAME, true);
        ConfigDialog cd = config.openByKey();

        // test body
        cd.getBookmarks_EnableBookmarks().push();
        Icon emptyIcon = cd.getIcon(1);
        cd.setBookmarkedByPopup(1);
        if (cd.getIcon(1) == emptyIcon) {
            errors.add("Icon wasn't found");
        }
        warnings.add("predefined warning: popup menu doesn't open without left clicking");

        //        startJavatestNewDesktop();
        //
        //        JFrameOperator mainFrame = findMainFrame();
        //
        //        closeQS(mainFrame);
        //        openTestSuite(mainFrame);
        //        createWorkDirInTemp(mainFrame);
        //        openConfigFile(openLoadConfigDialogByMenu(mainFrame), CONFIG_NAME);
        //        Config_Edit.waitForConfigurationLoading(mainFrame, CONFIG_NAME);
        //
        //        openConfigDialogByKey(mainFrame);
        //        JDialogOperator config = findConfigEditor(mainFrame);
        //
        //        pushEnableBookmarks(config);
        //        Icon emptyIcon = getIcon(config, 1);
        //        setBookmarkedByPopup(config, 1);
        //        if (getIcon(config, 1) == emptyIcon) {
        //            throw new JemmyException("Icon wasn't found");
        //        }
        //        System.out.println("predefined warning: popup menu doesn't open without left clicking");
    }

    @Override
    public String getDescription() {
        return "This test checks whether bookmark Icon is set to selected Question. The test starts JT, opens default TS, creates temporary WorkDirectory and loads default configuration. Then it takes Icon from second question, bookmarks this question by popup and checks that icon was changed.";
    }
}
