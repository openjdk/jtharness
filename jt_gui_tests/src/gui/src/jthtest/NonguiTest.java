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
package jthtest;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import org.netbeans.jemmy.JemmyException;

/**
 *
 * @author at231876
 */
public abstract class NonguiTest {

    public static final String TEMP_WD_NAME = "javatest_guitest_demowd";
    public static final String DEFAULT_WD_NAME = "demowd_config";
    public static final String WD_RUN_NAME = "demowd_run";
    public static final String TEST_SUITE_NAME = "demots";
    public static final String TEMPLATE_NAME = "demotemplate.jtm";
    public static final String REPORT_NAME = "demoreport";
    public static String TEMP_PATH;
    public static String LOCAL_PATH;
    public static String DEFAULT_PATH;
    public static String USER_HOME_PATH;
    public static final String CONFIG_NAME = "democonfig.jti";
    public static final String TESTS_DIRECTORY_PREFIX = "tests" + File.separator;
    public static final String NEWDESKTOP_ARG = "-newdesktop";
    public static final String WINDOWNAME = System.getProperty("jt_gui_test.name");
    public static final String TESTSUITENAME = "DemoTS 1.0 Test Suite (Tag Tests)";
    public static final String REPORT_WD_PATH = "demowd_run";
    public static final String KFL_NORMAL_PATH = "knownfailures.kfl";
    public static final String KFL_PASSED_PATH = "knownfailures_passed.kfl";
    public static boolean showWarnings = true;

    static {
        String temp = System.getProperty("user.dir") + File.separator;
        if (temp == null || "".equals(temp)) {
            File tmp = new File("");
            temp = tmp.getAbsolutePath();
        }
        DEFAULT_PATH = LOCAL_PATH = temp;

        TEMP_PATH = temp + File.separator + "temp";

        USER_HOME_PATH = System.getProperty("user.home") + File.separator;

        String showWarningsStr = System.getProperty("jt_gui_test.showWarnings");
        if (showWarningsStr != null) {
            showWarnings = Boolean.parseBoolean(showWarningsStr);
        }
    }
    protected LinkedList<String> errors;
    protected LinkedList<String> warnings;
    protected LinkedList<File> usedFiles;
    protected boolean depricated = false;
    private boolean ignoreDepricated = true;
    protected LinkedList<File> toCopyFiles;

    public NonguiTest() {
        warnings = new LinkedList<String>();
        errors = new LinkedList<String>();
        usedFiles = new LinkedList<File>();
        toCopyFiles = new LinkedList<File>();
    }

    public NonguiTest(File... files) {
        errors = new LinkedList<String>();
        warnings = new LinkedList<String>();
        usedFiles = new LinkedList<File>();
        toCopyFiles = new LinkedList<File>();
        for (File f : files) {
            toCopyFiles.add(f);
        }
    }

    public void addUsedFile(File f) {
        usedFiles.add(f);
    }

    public void addUsedFile(String f) {
        usedFiles.add(new File(f));
    }

    public void addToCopyFile(String f) {
        toCopyFiles.add(new File(f));
    }

    private void preTest() throws IOException {
        File name = new File(TESTS_DIRECTORY_PREFIX + this.getClass().getSimpleName());
        if (name.exists()) {
            Tools.deleteDirectory(name);
        }
        name.mkdirs();
        Tools.DEFAULT_PATH = Tools.USER_HOME_PATH = Tools.LOCAL_PATH = name.getAbsolutePath() + File.separator;
        Tools.TEMP_PATH = name.getAbsolutePath() + File.separator + "temp" + File.separator;
        DEFAULT_PATH = USER_HOME_PATH = LOCAL_PATH = name.getAbsolutePath() + File.separator;
        TEMP_PATH = name.getAbsolutePath() + File.separator + "temp" + File.separator;
        for (File f : toCopyFiles) {
            Tools.copyDirectory(f, name);
        }
    }

    @org.junit.Test//(timeout=1200000)
    public final void test() throws Throwable {
        if (!depricated || !ignoreDepricated) {
            Worker w = null;
            try {
                preTest();
                w = new Worker();
                Thread tr = new Thread(w);
                tr.start();

                t = 0;
                while (t < maxTime && w.isWorking) {
                    try {
                        Thread.sleep(step);
                        t += step;
                    } catch (InterruptedException ex) {
                    }
                }

                if (w.isWorking) {
                    tr.interrupt();
                    errors.add("Test was interrupted. Waited " + t / 1000 + "s");
                    throw new InterruptedException();
                }
            } finally {
                releaseResources();
                if (w != null && w.error != null) {
                    throw w.error;
                }
                if (!warnings.isEmpty() && showWarnings) {
                    System.out.println("Warnings: ");
                    for (String s : warnings) {
                        System.out.println(s);
                    }
                }
                if (!errors.isEmpty()) {
                    StringBuilder builder = new StringBuilder("Failed: \n");
                    for (String s : errors) {
                        builder.append(s).append("\n");
                    }
                    String description = getDescription();
                    if (description != null) {
                        builder.append("\n").append(description).append("\n");
                    }

                    throw new JemmyException(builder.toString());
                }
            }
        } else {
            System.out.println("Test is depticated. Marking as Passed. ");
        }
    }

    public abstract void testImpl() throws Exception;

    public String getDescription() {
        return null;
    }

    public void releaseImpl() throws Exception {
    }

    private void releaseResources() {
        try {
            releaseImpl();
        } catch (Exception e) {
        }
        for (File f : usedFiles) {
            try {
                if (f.exists()) {
                    if (f.isDirectory()) {
                        Tools.deleteDirectory(f);
                    } else if (f.isFile()) {
                        f.delete();
                    }
                    if (f.exists()) {
                        System.err.println("Temporary file " + f.getAbsolutePath() + " was not removed. Please remove it manualy.");
                    }
                }
            } catch (Exception e) {
            }
        }
    }
    public int maxTime = 1200000; // 20 minutes
    public int step = 1000;
    public int t = 0;
    public boolean isWorkReady = false;

    private class Worker implements Runnable {

        public Throwable error = null;
        public boolean isWorking = true;

        @Override
        public void run() {
            try {
                testImpl();
            } catch (Throwable ex) {
                this.error = ex;
            }
            isWorking = false;
        }
    }
}
