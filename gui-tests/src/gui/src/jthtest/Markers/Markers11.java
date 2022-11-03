/*
 * $Id$
 *
 * Copyright (c) 2001, 2022, Oracle and/or its affiliates. All rights reserved.
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

/**
 * This test case verifies that answer for current question could be cleared from a popup menu.
 */

import java.lang.reflect.InvocationTargetException;
import jthtest.Config_Edit.Config_Edit;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

public class Markers11 extends Markers {
    public static void main(String args[]) {
        JUnitCore.main("jthtest.gui.Markers.Markers11");
    }

    @Test
    public void testMarkers11() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        startJavatestNewDesktop();

        JFrameOperator mainFrame = findMainFrame();

        closeQS(mainFrame);
        openTestSuite(mainFrame);
        createWorkDirInTemp(mainFrame);
        openConfigFile(openLoadConfigDialogByMenu(mainFrame), CONFIG_NAME);
        Config_Edit.waitForConfigurationLoading(mainFrame, CONFIG_NAME);

        openConfigDialogByKey(mainFrame);
        JDialogOperator config = findConfigEditor(mainFrame);

        pushEnableBookmarks(config);

        selectQuestion(config, 2);
        new JTextFieldOperator(config, new NameComponentChooser("str.txt"))
                .typeText("some description that must be cleared");
        setBookmarkedByMenu(config, 2);
        clearByPopup(config, 2);

        if (!new JTextFieldOperator(config, new NameComponentChooser("str.txt")).getText().equals(""))
            throw new JemmyException("Text wasn't cleared up: '"
                    + new JTextFieldOperator(config, new NameComponentChooser("str.txt")).getText()
                    + "' while expected ''");
        System.out.println(
                "Pre-defined warning: Mark sometimes desapperas while clearing by menu - bookmark saves current state of answer; First question is 'Configuratoin name' and it can't be cleared; a new question is generated while clearing up");
    }
}

