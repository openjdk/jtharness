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
package jthtest.Config_SaveEdit;

import java.io.File;

import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

import jthtest.Test;
import jthtest.Tools;
import jthtest.tools.ConfigDialog;
import jthtest.tools.Configuration;
import jthtest.tools.JTFrame;

public class Config_SaveEdit1 extends Test {

    /**
     * This test case verifies that saving an interview file from file menu in
     * configuration editor will save the configuration file in a loaded file.
     */

    public void testImpl() throws Exception {

        mainFrame = new JTFrame(true);
        mainFrame.openDefaultTestSuite();
        File createdWD = mainFrame.createWorkDirectoryInTemp();
        addUsedFile(createdWD);

        Configuration configuration = mainFrame.getConfiguration();
        configuration.load(Tools.CONFIG_NAME, true);

        ConfigDialog config = configuration.openByKey();
        config.selectQuestion(2);
        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(config.getConfigDialog(),
                new NameComponentChooser("str.txt"));
        String save = jTextFieldOperator.getText();
        jTextFieldOperator.setText("some_description");

        config.getFile_SaveMenu().push();
        config.pushDoneConfigEditor();

        mainFrame.closeCurrentTool();
        mainFrame.getWorkDirectory().openWorkDirectory(createdWD);
        Tools.waitForWDLoading(mainFrame.getJFrameOperator(), Tools.WDLoadingResult.SOME_NOTRUN);

        config = configuration.openByKey();
        config.selectQuestion(2);

        jTextFieldOperator = new JTextFieldOperator(config.getConfigDialog(), new NameComponentChooser("str.txt"));
        if (!jTextFieldOperator.getText().equals("some_description")) {
            errors.add("description was not saved");
        }

        jTextFieldOperator.setText(save);
        config.pushDoneConfigEditor();
        mainFrame.closeCurrentTool();
        mainFrame.closeAllTools();
    }
}
