/*
 * $Id$
 *
 * Copyright (c) 2009, 2025, Oracle and/or its affiliates. All rights reserved.
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

package jthtest.ConfigSearchFind;

/**
 * This test case verifies that an exactly string in the interview could be displayed when Consider case checkbox is check on.
 */

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;
import jthtest.ConfigTools;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.Dumper;
import org.netbeans.jemmy.util.NameComponentChooser;

import jthtest.Config_Load.Config_Load;
import jthtest.Tools.WDLoadingResult;
import jthtest.Config_Edit.Config_Edit;
import jthtest.menu.Menu;

public class ConfigSearchFind5 extends ConfigTools {
     public static void main(String[] args) {
          JUnitCore.main("jthtest.gui.ConfigSearchFind.ConfigSearchFind5");
     }

     @Test
     public void testConfig_Load1() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException,
               InterruptedException, FileNotFoundException {

          // Start Java Test application with -newDesktop
          startJavatestNewDesktop();

          // get the reference of mainframe
          JFrameOperator mainFrame = findMainFrame();

          // Close Quick Start Dialog window
          closeQS(mainFrame);

          // Open default test suite
          openTestSuite(mainFrame);

          // Create work directory
          createWorkDirInTemp(mainFrame);

          // Load existing configuration file
          openConfigFile(openLoadConfigDialogByMenu(mainFrame), CONFIG_NAME);
          Config_Edit.waitForConfigurationLoading(mainFrame, CONFIG_NAME);

          // Edit Configuration
          JMenuBarOperator jmbo = new JMenuBarOperator(mainFrame);
          jmbo.pushMenuNoBlock("Configure", "/");
          Menu.getConfigure_EditConfigurationMenu(mainFrame).pushNoBlock();
          JDialogOperator configEditorDialog = new JDialogOperator(getExecResource("ce.name"));

          JDialogOperator config = findConfigEditor(mainFrame);
          pushNextConfigEditor(config);

          clickOnFind(configEditorDialog, "Java");

          ComponentChooser qu1 = new NameComponentChooser("qu.title");
          JTextFieldOperator testConfig1 = new JTextFieldOperator(configEditorDialog, qu1);
          Thread.sleep(2000);
          Assert.assertTrue("The interview should display the question that has Java and not java string in it.",
                    testConfig1.getText().equals("Java Virtual Machine"));

     }

     // Click on Search-->Find
     public void clickOnFind(JDialogOperator configEditorDialog, String textToSearch) throws InterruptedException {
          JMenuBarOperator jmbo1 = new JMenuBarOperator(configEditorDialog);
          jmbo1.pushMenu("Search", "/");
          jmbo1.pushMenu("Search/Find...", "/");
          JDialogOperator FindQuestion = new JDialogOperator("Find Question");
          Thread.sleep(1000);
          JTextFieldOperator javastring = new JTextFieldOperator(FindQuestion);
          javastring.enterText(textToSearch);

          ComponentChooser findCase = new NameComponentChooser("find.case");
          JCheckBoxOperator considerCase = new JCheckBoxOperator(FindQuestion, findCase);
          Thread.sleep(2000);
          considerCase.doClick();

          JButtonOperator findButton = new JButtonOperator(FindQuestion, "Find");
          findButton.doClick();
     }

}
