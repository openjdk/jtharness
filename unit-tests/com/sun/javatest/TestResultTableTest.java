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
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;

import com.sun.javatest.finder.HTMLTestFinder;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class TestResultTableTest {

    @Test
    public void demotck() throws Keywords.Fault, TestResult.Fault {
        Assert.assertTrue(new TestResultTableTest().run(System.out, TestUtil.getPathToTestTestSuite("demotck")));
    }

    @Test
    public void initurl() throws Keywords.Fault, TestResult.Fault {
        Assert.assertTrue(new TestResultTableTest().run(System.out, TestUtil.getPathToTestTestSuite("initurl")));
    }

    @Test
    public void simplehtml() throws Keywords.Fault, TestResult.Fault {
        Assert.assertTrue(new TestResultTableTest().run(System.out, TestUtil.getPathToTestTestSuite("simplehtml")));
    }

    /**
     * Test the TestResultTable class.
     * Argument 1 is the work dir with jtr files.
     */
    public boolean run(PrintStream log, String... args)
            throws Keywords.Fault, TestResult.Fault {

        boolean localResult = true;
        this.log = log;

        if (args == null || args.length != 1) {
            log.println("Error, specify a testsuite file please.");
            return false;
        }

        int nTests = 0;

        File testSuite;
        try {
            testSuite = new File(new File(args[0]).getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace(log);
            return false;
        }

        String[] finderArgs = {};
        log.println("Reading files from " + testSuite);

        // run a TestFinder over the tests and check the test descriptions
        // that are found
        TestFinder tf = new HTMLTestFinder();
        try {
            // TestFinder.init() takes an environment to support some test finders.
            // The implementation of HTMLTestFinder does not require a
            // TestEnvironment, thus, it's fine to create an empty one.
            TestEnvironment env = null;
            try {
                env = new TestEnvironment("", new Hashtable<>(), "");
            } catch (TestEnvironment.Fault e) {
                log.println("unable to create empty test environment");
                return false;
            }
            tf.init(finderArgs, testSuite, null, null, env);
        } catch (TestFinder.Fault e) {
            log.println("Error initializing test-finder class: com.sun.javatest.finder.HTMLTestFinder");
            return false;
        }

        Vector<TestResult> testResults = new Vector<>();
        TestResultTable tab = new TestResultTable();
        TestFinderQueue tfq = new TestFinderQueue();
        tfq.setTestFinder(tf);
        tfq.setTests(null);
        //tfq.addObserver(new TFQObserver());

        TestDescription td;
        while ((td = tfq.next()) != null) {
            log.println("found " + td.getRootRelativeURL());
            TestResult tr = new TestResult(td);
            testResults.addElement(tr);
            tab.update(tr);
            nTests++;
        }

        log.println("Created and recorded " + tab.size() + " test results.");

        // check the table size
        if (tab.size() != nTests) {
            log.println("Table size() incorrect.  Got " + tab.size() + ", expected " + nTests);
            return false;
        }

        // test the enumerator for completeness
        if (testEnum(tab, (Vector<TestResult>) testResults.clone()) != nTests) {
            log.println("TestResultTable Enumeration test failed.");
            return false;
        } else
            log.println("Enumerator tests passed.");

        // lookup a test
        if (!testLookup(tab, (Vector<TestResult>) testResults.clone())) {
            log.println("Lookup test failed.");
            return false;
        } else
            log.println("Lookup test passed.");

        if (!testFilter(tab, "testme")) {
            log.println("Filtering test failed.");
            return false;
        } else
            log.println("Filtering test passed.");

        // pass
        return true;
    }

    int testEnum(TestResultTable tab, Vector<TestResult> testResults) {
        Enumeration<TestResult> tabItems = tab.elements();

        int tabCount = 0;

        try {
            while (tabItems.hasMoreElements()) {
                // may fail internally or cast incorrectly
                TestResult tr = tabItems.nextElement();

                if (tr == null) {
                    tabCount = -1;
                    log.println("Enumerator returned null.");
                    break;
                }

                if (!testResults.removeElement(tr)) {
                    tabCount = -1;
                    log.println("Enumerator returned unexpected TestResult object.");
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

        // make sure enum returned all the expected tests
        if (testResults.size() != 0) {
            tabCount = -1;
            log.println("Enumerator did not return a complete set of tests.");
        }

        return tabCount;
    }

    boolean testLookup(TestResultTable tab, Vector<TestResult> testResults) {
        boolean localResult = true;
        int count = 0;

        // the loop and casts should be guaranteed to work by
        // previous tests
        try {
            for (int i = 0; i < testResults.size(); i++) {
                TestResult target = testResults.elementAt(i);

                TestDescription td = target.getDescription();

                TestResult tr = tab.lookup(td);

                if (tr == null) {
                    localResult = false;
                    log.println("Unable to lookup test" + td.getRootRelativeURL());
                    break;
                }

                if (tr != target) {
                    localResult = false;
                    log.println("-----");
                    log.println("TRT Lookup did not return the same object that was inserted.");
                    log.println("Tried to find: ");
                    log.println("      " + td.getRootRelativeURL());
                    log.println("      " + td);
                    log.println("But got: ");
                    log.println("      " + tr.getDescription().getRootRelativeURL());
                    log.println("      " + tr);
                    log.println("-----");
                    break;
                }

                count++;
            }   // for
        } catch (TestResult.Fault | ClassCastException f) {
            f.printStackTrace(log);
            localResult = false;
        }

        if (localResult)
            log.println("Sucessfully looked up " + count + " test results.");

        return localResult;
    }

    boolean testFilter(TestResultTable tab, String theKeyword)
            throws Keywords.Fault, TestResult.Fault {
        boolean localResult = true;
        int tabCount = 0;

        Keywords kw = Keywords.create("any of", theKeyword);
        TestFilter filter = new KeywordsFilter(kw);

        Enumeration<TestResult> tabItems = TestResultTable.elements(tab.getRoot(), filter);

        try {
            while (tabItems.hasMoreElements()) {
                // may fail internally or cast incorrectly
                TestResult tr = tabItems.nextElement();

                if (tr == null) {
                    log.println("Enumerator returned null.");
                    localResult = false;
                    break;
                }

                TestDescription td = tr.getDescription();
                Set<String> words = td.getKeywordTable();

                if (!words.contains(theKeyword)) {
                    log.println("Filtered Enumerator returned TestResult without the keyword.");
                    localResult = false;
                    break;
                }

                log.println("Filter approved " + tr.getTestName());

                tabCount++;
            }
        } catch (NoSuchElementException e) {
            log.println("Filtered TRT Enumerator did not function properly (hasMoreElements()).");
            e.printStackTrace(log);
            localResult = false;
        }

        log.println("Filtered Enumerator returned " + tabCount + " objects.");

        return localResult;
    }

    private PrintStream log;
}

