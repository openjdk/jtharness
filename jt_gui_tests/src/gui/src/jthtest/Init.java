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
package jthtest;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import java.io.File;
import java.io.FileNotFoundException;

import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.Dumper;
import org.netbeans.jemmy.util.NameComponentChooser;
import jthtest.tools.JTFrame;
import org.netbeans.jemmy.operators.JCheckBoxOperator;

/**
 *
 * @author at231876
 */
public class Init extends ConfigTools {

    private static JTFrame mainFrame;
    private static String targetDir;

    private static abstract class RunManager {

        public abstract void runTests(JFrameOperator mainFrame);
    }

    public static void main(String args[]) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InterruptedException, FileNotFoundException {
        if (args.length < 2) {
            System.out.println("please provide paths for config and template files: config_path template_path [javahome_path]");
            System.exit(1);
        }
        String javaPath;
        if (args.length > 2) {
            javaPath = args[2];
        } else {
            javaPath = System.getProperty("java.home");
            if (javaPath == null) {
                System.out.println("please provide javahome path in arguments or in system properties: config_path template_path [javahome_path]");
                System.exit(1);
            } else {
                if (File.separatorChar == '/') {
                    javaPath += "/bin/java";
                } else {
                    javaPath += "\\bin\\java.exe";
                }
            }
        }
        System.setProperty("com.sun.javatest.exec.templateMode", "true");
                JTFrame.closeQSOnOpen = false;
        mainFrame = JTFrame.startJTWithDefaultTestSuite();
        //Tools.pause(500);

        mainFrame.getFile_PreferencesMenu().pushNoBlock();
        JDialogOperator prefs = new JDialogOperator(WINDOWNAME + " Harness Preferences");
        new JTreeOperator(prefs).selectRow(2);
        new JCheckBoxOperator(prefs, "Warn if All Tests filter is selected when test runs starts").setSelected(true);
        new JButtonOperator(prefs, "OK").push();

        targetDir = new File("").getAbsolutePath();
        File toDelete = mainFrame.createWorkDirectoryInTemp();
        repairConfig("democonfig.jti", javaPath);
        repairConfig("democonfig_second.jti", javaPath);
        repairConfig("democonfig with spaces.jti", javaPath);

        mainFrame.getMenuBar().showMenuItem(new String[]{"Configure", "Load Template..."}).push();
        JDialogOperator dop = new JDialogOperator("Load Template");
        new JButtonOperator(dop, "Browse...").push();
        JDialogOperator locd = new JDialogOperator("Template Location");
        new JTextFieldOperator(locd).enterText(Tools.LOCAL_PATH + File.separator + "demotemplate.jtm");
        new JCheckBoxOperator(dop, "Launch Template Editor").push();
        new JButtonOperator(dop, "Load").push();

        JDialogOperator edit = new JDialogOperator("Template Editor");
        new JButtonOperator(edit, "Last").push();
        new JTextFieldOperator(edit, new NameComponentChooser("file.txt")).enterText(javaPath);
        new JButtonOperator(edit, "Last").push();
        new JButtonOperator(edit, "Done").push();

        Tools.deleteDirectory(toDelete);
        Thread.sleep(4000);
        closeJT(mainFrame.getJFrameOperator());
    }

    public static void repairConfig(String name, String javaPath) {
//        openConfigFile(openLoadConfigDialogByMenu(mainFrame), name);
        mainFrame.getConfiguration().load(name, true);

        jthtest.tools.ConfigDialog configDialog = mainFrame.getConfiguration().openByMenu(true);
//        openConfigDialogByMenu(mainFrame);
//        JDialogOperator config = findConfigEditor(mainFrame);
        configDialog.selectQuestion(4);
        JTextFieldOperator file = new JTextFieldOperator(configDialog.getConfigDialog(), new NameComponentChooser("file.txt"));
        file.setText("");
        file.typeText(javaPath);

        configDialog.pushLastConfigEditor();
        configDialog.selectQuestion(1);
        configDialog.pushDoneConfigEditor();
    }
}
