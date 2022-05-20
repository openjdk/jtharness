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
package jthtest.Config_LoadEdit;

import java.lang.reflect.InvocationTargetException;

import jthtest.ConfigTools;
import jthtest.Test;
import jthtest.Tools;
import jthtest.tools.ConfigDialog;
import jthtest.tools.Configuration;
import jthtest.tools.JTFrame;

public class Config_LoadEdit2 extends Test {

    /**
     * This test case verifies that Load button under file menu in configuration
     * editor will bring up a configuration with a valid jti file.
     */

    public void testImpl() throws Exception {
        JTFrame mainFrame = new JTFrame(true);

        mainFrame.openDefaultTestSuite();
        addUsedFile(mainFrame.createWorkDirectoryInTemp());

        Configuration configuration = mainFrame.getConfiguration();
        configuration.load(Tools.CONFIG_NAME, true);

        ConfigDialog configDialog = configuration.openByKey();
        configDialog.load(ConfigTools.SECOND_CONFIG_NAME, true);

        if (!configDialog.getConfigDialog().getTitle().contains(ConfigTools.SECOND_CONFIG_NAME)) {
            errors.add(
                    "Configuration editor dialog box has invalid title - " + configDialog.getConfigDialog().getTitle());
        }
    }
}
