/*
 * $Id$
 *
 * Copyright (c) 2001, 2020, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest;


import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.NoSuchElementException;

import com.sun.javatest.finder.HTMLTestFinder;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the TestResultTable class.
 * Argument 1 is the test suite root file for web walk
 * Argument 2 is the test suite root for dir walk
 * Argument 3 is a scratch work dir
 */
public class TableWithFinderTest {

    private File testWeb;    // web walk location
    private File testDir;    // dir walk location
    private WorkDirectory webWorkDir;    // work directory for web walk
    private WorkDirectory dirWorkDir;    // work directory for dir walk
    private PrintStream log;


    @Test
    public void main() {
        System.out.println("Starting");
        TableWithFinderTest t = new TableWithFinderTest();
        boolean ok = false;

        try {
            ok = t.run(new String[]{
                    TestUtil.getPathToTestTestSuite("variety_tests", "testsuite.html"),
                    TestUtil.getPathToTestTestSuite("variety_tests"),
                    TestUtil.createTempDirAndReturnAbsPathString("TableWithFinderTest-workdir")
            }, System.out);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            ok = false;
        }

        System.out.println("Finishing...");
        Assert.assertTrue(ok);
    }

    public boolean run(String[] args, PrintStream log) {
        boolean localResult = true;
        this.log = log;

        if (args == null || args.length != 3) {
            log.println("Error, specify a testsuite file, then directory please, then workdir");
            log.println("Args are: <testsuite filename for web walk> <test dir name for dir walk> <work dir>");
            return false;
        }

        int nTests = 0;

        try {
            testWeb = new File(new File(args[0]).getCanonicalPath());
        } catch (IOException e) {
            log.println("Error processing the web walk argument.");
            e.printStackTrace(log);
            return false;
        }

        try {
            testDir = new File(new File(args[1]).getCanonicalPath());
        } catch (IOException e) {
            log.println("Error processing the dir walk argument.");
            e.printStackTrace(log);
            return false;
        }

        try {
            File workDir = new File(new File(args[2]).getCanonicalPath());
            if (workDir.exists()) {
                clearDir(workDir);
            }

            TestSuite webTestSuite = TestSuite.open(testWeb);
            webWorkDir = WorkDirectory.create(new File(workDir, "webWalk"), webTestSuite);

            TestSuite dirTestSuite = TestSuite.open(testDir);
            dirWorkDir = WorkDirectory.create(new File(workDir, "dirWalk"), dirTestSuite);
        } catch (IOException e) {
            log.println("Error processing the work dir argument.");
            e.printStackTrace(log);
            return false;
        } catch (TestSuite.Fault e) {
            log.println("Error processing the work dir argument.");
            e.printStackTrace(log);
            return false;
        } catch (WorkDirectory.Fault e) {
            log.println("Error processing the work dir argument.");
            e.printStackTrace(log);
            return false;
        }

        log.println("Web walk location is: " + testWeb);
        log.println("Dir walk location is: " + testDir);

        log.println("** Starting web walk tests... **");
        boolean result = testWebWalk();

        if (!result) {
            log.println("Web walk tests failed.");
            return false;
        }

        log.println("\n \n ** Starting dir walk tests... **");
        result = testDirWalk();

        if (!result) {
            log.println("Dir walk tests failed.");
            return false;
        }

        return true;
    }

    private boolean testWebWalk() {
        HTMLTestFinder tf = createWebWalk();
        TestResultTable tab = null;

        tab = new TestResultTable(webWorkDir, tf);

        TestResultTable.TreeNode root = tab.getRoot();


        int origRootSize = root.getSize();
        log.println("root reports " + root.getSize() + " nodes.");

        int foundCount = testEnum(tab);

        log.println("root now reports " + root.getSize() + " nodes.");

        if (root.getSize() != origRootSize) {
            log.println("Root size before and after enumerating not the same.");
            return false;
        }

        return true;
    }

    private boolean testDirWalk() {
        HTMLTestFinder tf = createDirWalk();
        TestResultTable tab = null;

        tab = new TestResultTable(dirWorkDir, tf);

        TestResultTable.TreeNode root = tab.getRoot();

        int origRootSize = root.getSize();
        log.println("root reports " + root.getSize() + " nodes.");

        int foundCount = testEnum(tab);

        log.println("root now reports " + root.getSize() + " nodes.");

        if (root.getSize() != origRootSize) {
            log.println("Root size before and after enumerating not the same.");
            return false;
        }

        return true;
    }

    private HTMLTestFinder createWebWalk() {
        HTMLTestFinder tf = new HTMLTestFinder();
        try {
            // TestFinder.init() takes an environment to support some test finders.
            // The implementation of HTMLTestFinder does not require a
            // TestEnvironment, thus, it's fine to create an empty one.
            TestEnvironment env = null;
            String[] finderArgs = {};
            try {
                env = new TestEnvironment("", new HashMap<>(), "");
            } catch (TestEnvironment.Fault e) {
                log.println("unable to create empty test environment");
                e.printStackTrace(log);
                System.exit(2);
            }
            tf.init(finderArgs, testWeb, null, null, env);
        } catch (TestFinder.Fault e) {
            log.println("Error initializing test-finder class: com.sun.javatest.finder.HTMLTestFinder");
            e.printStackTrace(log);
            System.exit(2);
        }

        tf.setMode(HTMLTestFinder.WEB_WALK);
        return tf;
    }

    private HTMLTestFinder createDirWalk() {
        HTMLTestFinder tf = new HTMLTestFinder();
        try {
            // TestFinder.init() takes an environment to support some test finders.
            // The implementation of HTMLTestFinder does not require a
            // TestEnvironment, thus, it's fine to create an empty one.
            TestEnvironment env = null;
            String[] finderArgs = {};
            try {
                env = new TestEnvironment("", new HashMap<>(), "");
            } catch (TestEnvironment.Fault e) {
                log.println("unable to create empty test environment");
                e.printStackTrace(log);
                System.exit(2);
            }
            tf.init(finderArgs, testDir, null, null, env);
        } catch (TestFinder.Fault e) {
            log.println("Error initializing test-finder class: com.sun.javatest.finder.HTMLTestFinder");
            e.printStackTrace(log);
            System.exit(2);
        }

        tf.setMode(HTMLTestFinder.DIR_WALK);
        return tf;
    }

    int testEnum(TestResultTable tab) {
        log.println("Starting enumeration...");
        Enumeration<TestResult> tabItems = tab.elements();

        int tabCount = 0;

        try {
            while (tabItems.hasMoreElements()) {
                // may fail internally or cast incorrectly
                TestResult tr = tabItems.nextElement();
                log.println(tr.getWorkRelativePath());

                if (tr == null) {
                    tabCount = -1;
                    log.println("Enumerator returned null.");
                    break;
                }

                tabCount++;
            }
        } catch (NoSuchElementException e) {
            log.println("TRT Enumerator did not function properly (hasMoreElements()).");
            e.printStackTrace(log);
            tabCount = -1;
        }

        log.println("Enumerator returned " + tabCount + " objects.");

        return tabCount;
    }

    private static void clearDir(File dir) {
        String[] list = dir.list();
        if (list != null) {
            for (String aList : list) {
                File f = new File(dir, aList);
                if (f.isDirectory()) {
                    clearDir(f);
                }
                f.delete();
            }
        }
    }


}

