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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.Permission;
import java.util.Arrays;
import java.util.LinkedList;
import jthtest.tools.JTFrame;
import jthtest.tools.Task.Waiter;
import org.netbeans.jemmy.JemmyException;

/**
 *
 * @author at231876
 */
public abstract class Test {

    public static final String TEMP_WD_NAME = "javatest_guitest_demowd";
    public static final String DEFAULT_WD_NAME = "demowd_config";
    public static final String WD_RUN_NAME = "demowd_run";
    public static final String TEST_SUITE_NAME = "demots";
    public static final String TESTCASES_TEST_SUITE_NAME = "demots_withtestcases";
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
    public static final String KFL_MISSING_PATH = "knownfailures_missing.kfl";
    public static final String KFL_RECURSIVE_PATH = "knownfailures_recursive.kfl";
    public static final String KFL_TC_ALL_PATH = "kfl_tc_all.kfl";
    public static boolean showWarnings = true;
    public static boolean ignoreDepricated = true;
    public static boolean notrunKnownFail = false;
    public static boolean overrideKnownFail = false;

    public static void printBools() {
        System.out.println("showWarnings " + showWarnings);
        System.out.println("ignoreDepricated " + ignoreDepricated);
        System.out.println("notrunKnownFail " + notrunKnownFail);
        System.out.println("overrideKnownFail " + overrideKnownFail);
    }

    static {
        String temp = System.getProperty("user.dir") + File.separator;
        if (temp == null || "".equals(temp)) {
            File tmp = new File("");
            temp = tmp.getAbsolutePath();
        }
        DEFAULT_PATH = LOCAL_PATH = temp;

        TEMP_PATH = temp + File.separator + "temp";

        USER_HOME_PATH = System.getProperty("user.home") + File.separator;

        showWarnings = getBooleanProperty("jt_gui_test.showWarnings");
        ignoreDepricated = getBooleanProperty("jt_gui_test.ignoreDepricated");
        notrunKnownFail = getBooleanProperty("jt_gui_test.not_run_knownfail");
        overrideKnownFail = getBooleanProperty("jt_gui_test.override_knownfail");
    }

    private static boolean getBooleanProperty(String property) {
        String prop = System.getProperty(property);
//        System.out.println(property + " " + prop);
        return prop != null && Boolean.parseBoolean(prop);
    }
    protected LinkedList<String> errors;
    protected LinkedList<String> warnings;
    protected LinkedList<File> usedFiles;
    protected boolean depricated = false;
    protected boolean knownFail = false;
    protected boolean catchAnyExceptions = false;
    protected JTFrame mainFrame;
    protected LinkedList<File> toCopyFiles;
    protected final StringWriter OUT = new StringWriter();
    protected final StringWriter ERR = new StringWriter();
    private final PrintStream ST_OUT = System.out;
    private final PrintStream ST_ERR = System.err;

    public Test() {
        System.setOut(new java.io.PrintStream(new java.io.OutputStream() {

            @Override
            public void write(int b) throws IOException {
                OUT.write(b);
                ST_OUT.write(b);
            }
        }));
        System.setErr(new java.io.PrintStream(new java.io.OutputStream() {

            @Override
            public void write(int b) throws IOException {
                ERR.write(b);
                ST_ERR.write(b);
            }
        }));

        warnings = new LinkedList<String>();
        errors = new LinkedList<String>();
        usedFiles = new LinkedList<File>();
        toCopyFiles = new LinkedList<File>();
        toCopyFiles.add(new File(DEFAULT_WD_NAME));
        toCopyFiles.add(new File(TEST_SUITE_NAME));
        toCopyFiles.add(new File(CONFIG_NAME));
        toCopyFiles.add(new File(REPORT_WD_PATH));
        toCopyFiles.add(new File(ConfigTools.SECOND_CONFIG_NAME));
    }

    public Test(File... files) {
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

    private String getErrorMessage() {
        if (errors.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder("Failed: \n");
        for (String s : errors) {
            builder.append(s).append("\n");
        }
        String description = getDescription();
        if (description != null) {
            builder.append("\n").append(description).append("\n");
        }
        return builder.toString();
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
//        printBools();
        if (!depricated || ignoreDepricated) {
            Worker w = null;
            try {
                if (!knownFail || !notrunKnownFail) {
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
                }
            } finally {
                System.setOut(ST_OUT);
                System.setErr(ST_ERR);
                String outString = OUT.getBuffer().toString();
                
                BufferedReader readOut = new BufferedReader(new java.io.StringReader(outString));
                String line; 
                LinkedList<String> exceptions = new LinkedList<String>();
                boolean outCatch = false;
                while ((line = readOut.readLine()) != null) {
                    if (line.contains("Exception in thread")) {
                        outCatch = true;
                        exceptions.add(line);
                    }
                }
                readOut.close();
                String errString = ERR.getBuffer().toString();
                
                readOut = new BufferedReader(new java.io.StringReader(errString));
                boolean errCatch = false;
                while ((line = readOut.readLine()) != null) {
                    if (line.contains("Exception in thread")) {
                        errCatch = true;
                        exceptions.add(line);
                    }
                }
                
                if (catchAnyExceptions && !exceptions.isEmpty()) {
                    errors.add("JavaTest threw exceptions in system output:\n" + Arrays.toString(exceptions.toArray(new String[exceptions.size()])));
                    if (outCatch) {
                        System.out.println(outString);
                    }
                    if (errCatch) {
                        System.out.println(errString);
                    }
                }

                try {
                    Waiter releaser = new Waiter(20000) { // 20 seconds to remove temp files and close JT

                        @Override
                        protected boolean check() {
                            return true;
                        }
                    };
                    releaseResources(w.error == null); // if passed - delete test directory forcely
                    releaser.stopWaiter();
                } catch (Exception e) {
                    warnings.add("Timeout exception while releasing resources. " + e.getMessage());
                }

                if (knownFail && !overrideKnownFail) {
                    if (w != null && w.error != null) {
                        warnings.add("Test is failed but is marked as known failure. Exception: " + w.error.getMessage());
                    } else if (!errors.isEmpty()) {
                        warnings.add("Test is failed but is marked as known failure. Errors: " + getErrorMessage());
                    } else {
                        warnings.add("Test is passed but is marked as known failure");
                    }
                    if (showWarnings) {
                        showWarnings();
                    }
                } else {
                    if (showWarnings) {
                        showWarnings();
                    }

                    if (!errors.isEmpty() || (w != null && w.error != null)) {
                        JemmyException ex;
                        if (w != null && w.error != null) {
                            ex = new JemmyException(getErrorMessage(), w.error);
                        } else {
                            ex = new JemmyException(getErrorMessage());
                        }
                        throw ex;
                    }
                }
            }
        } else {
            System.out.println("Test is depticated. Marking as Passed. ");
        }
    }

    private void showWarnings() {
        if (!warnings.isEmpty() && showWarnings) {
            System.out.println("Warnings: ");
            for (String s : warnings) {
                System.out.println(s);
            }
        }
    }

    public abstract void testImpl() throws Exception;

    public String getDescription() {
        return null;
    }

    public void releaseImpl() throws Exception {
    }

    private void releaseResources(boolean force) {
        try {
            releaseImpl();
            if (mainFrame != null && mainFrame.getJFrameOperator().isVisible()) {
                mainFrame.closeAllTools();
                mainFrame.closeFrame();
            }
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
        if (force && LOCAL_PATH.contains(TESTS_DIRECTORY_PREFIX + this.getClass().getSimpleName())) {
            Tools.deleteDirectory(LOCAL_PATH);
        }
    }
    public int maxTime = 600000; // 10 minutes
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
            } catch (ExitException ex) {
            } catch (Throwable ex) {
                this.error = ex;
            }
            isWorking = false;
        }
    }

    @org.junit.Before
    public void setUp() {
        System.setSecurityManager(new ExceptionOnExitSecurityManager());
    }

    @org.junit.After
    public void tearDown() {
        System.setSecurityManager(null);
    }

    public static class ExitException extends SecurityException {
    }

    public static class ExceptionOnExitSecurityManager extends SecurityManager {

        @Override
        public void checkPermission(Permission perm) {
        }

        @Override
        public void checkPermission(Permission perm, Object context) {
        }

        @Override
        public void checkExit(int status) {
            super.checkExit(status);
            throw new ExitException();
        }
    }
}
