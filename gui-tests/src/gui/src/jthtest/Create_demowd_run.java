/*
 * $Id$
 *
 * Copyright (c) 2001, 2021, Oracle and/or its affiliates. All rights reserved.
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

import jthtest.tools.JTFrame;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

public class Create_demowd_run extends ConfigTools {

    private static JTFrame mainFrame;
    private static String targetDir;

    public static void main(String args[]) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InterruptedException, FileNotFoundException {

        System.setProperty("com.sun.javatest.exec.templateMode", "true");
        JTFrame.closeQSOnOpen = false;
        mainFrame = JTFrame.startJTWithDefaultTestSuite();
        //Tools.pause(500);

        targetDir = new File("").getAbsolutePath();
        openTestSuite(mainFrame.getJFrameOperator());
        createWD("demowd_run", args[0], new RunManager() {

            public void runTests(JFrameOperator mainFrame) {
                final JTreeOperator tree = findTree(mainFrame);
                tree.selectRow(2);
                Create_demowd_run.mainFrame.runTests(2);
            }
        });
        closeJT(mainFrame.getJFrameOperator());
    }

    static void createWD(String name, String config, RunManager run) throws InterruptedException {
        mainFrame.getWorkDirectory().createWorkDirectory(targetDir, name, true);
        if (config != null) {
            mainFrame.getConfiguration().load(targetDir, config, true);
        }
        if (run != null && config != null) {
            run.runTests(mainFrame.getJFrameOperator());
        }
    }

    private static abstract class RunManager {

        public abstract void runTests(JFrameOperator mainFrame);
    }

}
