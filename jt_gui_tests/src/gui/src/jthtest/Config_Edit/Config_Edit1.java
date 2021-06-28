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
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 *
 * @author linfar
 */
public class Config_Edit1 extends Test {

    public Config_Edit1() {
        super();
        depricated = true;
    }

    public void testImpl() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        mainFrame = new JTFrame(true);

        mainFrame.openDefaultTestSuite();
        addUsedFile(mainFrame.createWorkDirectoryInTemp());
        Configuration conf = mainFrame.getConfiguration();
        conf.load(CONFIG_NAME, true);

        ConfigDialog cd = conf.openByKey();
        checkAnswers(cd.getConfigDialog());
        cd.closeByMenu();
        cd = conf.openByKey();
        checkAnswers(cd.getConfigDialog());

        //    startJavatestNewDesktop();
        //
        //    JFrameOperator mainFrame = findMainFrame();
        //
        //    openTestSuite(mainFrame);
        //
        //    createWorkDirInTemp(mainFrame);
        //
        //    openConfigFile(openLoadConfigDialogByMenu(mainFrame), CONFIG_NAME);
        //    waitForConfigurationLoading(mainFrame, CONFIG_NAME);
        //
        //    openConfigDialogByKey(mainFrame);
        //    checkAnswers(findConfigEditor(mainFrame));
        //    openConfigDialogByKey(mainFrame);
        //    checkAnswers(findConfigEditor(mainFrame));
    }

    private void checkAnswers(JDialogOperator config) {
        JListOperator list = new JListOperator(config);
        list.setSelectedIndex(list.getModel().getSize() - 1);
        if (!new JTextFieldOperator(config, new NameComponentChooser("qu.title")).getText().equals("Congratulations!")) {
            throw new JemmyException("Configuration is not complete");
        }
    }

    @Override
    public String getDescription() {
        return "This test checks that reopening Config Editor will show 2 full configurations. This test is depricated as far as Config Editor is now modal and it is not possible to open 2 such dialogs.";
    }
}
