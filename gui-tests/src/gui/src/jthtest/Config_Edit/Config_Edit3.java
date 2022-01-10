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

package jthtest.Config_Edit;

import java.lang.reflect.InvocationTargetException;

import jthtest.Test;
import jthtest.tools.ConfigDialog;
import jthtest.tools.Configuration;
import jthtest.tools.JTFrame;

public class Config_Edit3 extends Test {

    public void testImpl() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {

        mainFrame = new JTFrame(true);

        // Open default test suite, create workdirectory and load configuration
        mainFrame.openDefaultTestSuite();
        addUsedFile(mainFrame.createWorkDirectoryInTemp());
        Configuration conf = mainFrame.getConfiguration();
        conf.load(CONFIG_NAME, true);

        // push back button on configuration editor and verify back button is disabled
        ConfigDialog cd = conf.openByKey();
        cd.pushBackConfigEditor();
        if (cd.getBackButton().isEnabled()) {
            errors.add("Back button is enabled while unexpected");
        }

        // push next and back buttons and verify that the configuration editor will go
        // back to previous question
        cd.pushNextConfigEditor();
        cd.pushBackConfigEditor();
        if (!cd.isSelectedIndex(0)) {
            errors.add("After back button pushing list selection is not on first question");
        }
    }

    // TestCase Description
    public String getDescription() {
        return "This test case verifies that Back button in the configuration editor will go back to the previous question.";
    }
}
