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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import com.sun.javatest.functional.TestBase;
import org.junit.Assert;
import org.junit.Test;

public class SelectionTest extends TestBase implements Harness.Observer {

    @Test
    public void test() throws IOException {
        SelectionTest t = new SelectionTest();
        String args[] = {
                System.getProperty("build.classes"),
                TestUtil.getPathToTestTestSuite("demotck", "testsuite.html"),
                "basic.jte",
                TestUtil.createTempDirAndReturnAbsPathString("selectionTest-workdir")
        };

        boolean ok = t.run(args, System.out);
        System.out.println("passed? " + ok);
        Assert.assertTrue(ok);
    }

    public boolean run(String[] args, PrintStream log) {
        try {
            this.log = log;

            if (args.length != 4) {
                log.println("Wrong # args (expected 4, got " + args.length + ")");
                return false;
            }

            File harnessClassDir = new File(args[0]);
            basicTestSuite = TestSuite.open(new File(args[1]));
            envFile = new File(args[2]);

            File workDir = new File(args[3]);
            testWorkDir = WorkDirectory.create(workDir, basicTestSuite);

            log.println("harnessClassDir: " + harnessClassDir);
            log.println("basic test suite: " + basicTestSuite.getRoot());
            log.println("envFile: " + envFile);
            log.println("work: " + testWorkDir.getRoot());

            Properties p = System.getProperties();
            TestEnvironment.addDefaultPropTable("(system properties)", com.sun.javatest.util.PropertyUtils.convertToStringProps(p));
            HashMap<String, String> m = new HashMap<>();
            m.put("java.home", java_home);
            TestEnvironment.addDefaultPropTable("(SelectionTest)", m);

            // first, we run the harness over the basic tests to
            // set the "previous" status of each test to a known state
            Parameters params = createParameters();

            Harness harness = new Harness(harnessClassDir);
            harness.addObserver(this);
            log.println("running tests to set status to known state");
            harness.batch(params);
            log.println("ok");

            log.println("checking initial file selection");
            checkInitialFileSelection("comp", "Comp", 6);
            checkInitialFileSelection("exec", "Exec", 5);

            log.println("checking selection according to previous state");
            checkPreviousStateSelection(Status.PASSED, "Should Pass", 5);
            checkPreviousStateSelection(Status.FAILED, "Should Fail", 5);
            checkPreviousStateSelection(Status.ERROR, "Should Error", 1);

            log.println("checking selection according to keywords");
            checkKeywordSelection("expr/ShouldPass", "TITLE/Should Pass", 5);
            checkKeywordSelection("expr/ShouldFail", "TITLE/Should Fail", 5);
            checkKeywordSelection("expr/ShouldError", "TITLE/Should Error", 1);
            checkKeywordSelection("expr/ShouldExecute", "ID/Exec", 5);
            checkKeywordSelection("expr/!ShouldExecute", "ID/Comp", 6);
            checkKeywordSelection("expr/ShouldExecute & ShouldFail", "TITLE/Should Fail", 3);
            checkKeywordSelection("expr/ShouldExecute & !ShouldFail", "TITLE/Should", 2);
            checkKeywordSelection("all of/ShouldExecute ShouldFail", "TITLE/Should Fail", 3);
            checkKeywordSelection("any of/ShouldPass ShouldFail", "TITLE/Should", 10);

            return ok;
        } catch (TestSuite.Fault e) {
            log.println("problem opening test suite: " + e);
            return false;
        } catch (WorkDirectory.Fault e) {
            log.println("problem creating work directory: " + e);
            return false;
        } catch (Harness.Fault e) {
            log.println("problem running harness: " + e);
            return false;
        } catch (FileNotFoundException e) {
            log.println("can't find file " + e.getMessage());
            return false;
        } finally {
            log.flush();
        }
    }


    public synchronized void report(String s) {
        log.println(s);
    }

    public synchronized void report(String[] s) {
        printlns(s);
    }

    public synchronized void error(String s) {
        log.println("Error: " + s);
    }

    public synchronized void error(String[] s) {
        String[] s2 = new String[s.length + 1];
        s2[0] = "Error:";
        for (int i = 0; i < s.length; i++) {
            s2[i + 1] = "-- " + s[i];
        }
        printlns(s2);
    }

    public void startingTestRun(Parameters params) {
    }

    public void startingTest(TestResult tr) {
    }

    public void finishedTest(TestResult tr) {
    }

    public void stoppingTestRun() {
    }

    public void finishedTesting() {
    }

    public void finishedTestRun(boolean allOK) {
    }

    private void printlns(String[] msgs) {
        for (String msg : msgs) {
            log.println(msg);
        }
    }

    private FileParameters createParameters() {
        FileParameters params = new FileParameters();
        params.setTestSuite(basicTestSuite);
        params.setWorkDirectory(testWorkDir);
        params.setTests((String[]) null);
        params.setExcludeFiles((File[]) null);
        params.setKeywords(FileParameters.EXPR, null);
        params.setPriorStatusValues(null);
        params.setEnvFiles(new File[]{envFile});
        params.setEnvName("basic");
        params.setReportDir(testWorkDir.getRoot());
        return params;
    }

    private void checkInitialFileSelection(String initialFile, String prefix,
                                           int expected) {
        FileParameters params = createParameters();
        params.setTests(new String[]{initialFile});

        int found = 0;

        TestDescription td = null;
        TestResultTable table = params.getWorkDirectory().getTestResultTable();
        Iterator<TestResult> it = table.getIterator(new String[]{initialFile}, params.getFilters());
        while (it.hasNext()) {
            TestResult tr = it.next();
            try {
                td = tr.getDescription();
            } catch (TestResult.Fault f) {
                log.println("Problem getting TD: " + f);
                failed("TestResult would not give TestDescription");
            }

            if (!td.getId().startsWith(prefix)) {
                log.println("initial File: " + initialFile);
                log.println("found test: " + td.getRootRelativeURL());
                log.println("expected prefix is: " + prefix);
                failed("unexpected test found");
            }
            found++;
        }

        if (found != expected) {
            log.println("expected number of tests: " + expected);
            log.println("found: " + found);
            failed("wrong number of tests found");
        }
    }

    private void checkPreviousStateSelection(int state, String prefix, int expected) {
        FileParameters params = createParameters();
        boolean[] b = new boolean[Status.NUM_STATES];
        b[state] = true;
        params.setPriorStatusValues(b);

        int found = 0;

        TestDescription td = null;
        Iterator<TestResult> it = createTestEnum(params);
        while (it.hasNext()) {
            TestResult tr = it.next();
            try {
                td = tr.getDescription();
            } catch (TestResult.Fault f) {
                log.println("Problem getting TD: " + f);
                failed("TestResult would not give TestDescription");
            }

            if (!td.getTitle().startsWith(prefix)) {
                log.println("state: " + state);
                log.println("found test: " + td.getRootRelativeURL());
                log.println("title: " + td.getTitle());
                log.println("expected prefix of title is: " + prefix);
                failed("unexpected test found");
            }
            found++;
        }

        if (found != expected) {
            log.println("state: " + state);
            log.println("expected number of tests: " + expected);
            log.println("found: " + found);
            failed("wrong number of tests found");
        }
    }

    private void checkKeywordSelection(String keyInfo, String matchInfo, int expected) {

        String matchField = matchInfo.substring(0, matchInfo.indexOf('/'));
        String matchPrefix = matchInfo.substring(matchInfo.indexOf('/') + 1);
        String keyType = keyInfo.substring(0, keyInfo.indexOf('/'));
        String keyText = keyInfo.substring(keyInfo.indexOf('/') + 1);

        FileParameters params = createParameters();
        //params.put(Parameters.KEYWORD_OP, keyType);
        int op = keyType.equals("expr") ? FileParameters.MutableKeywordsParameters.EXPR
                : keyType.equals("all of") ? FileParameters.MutableKeywordsParameters.ALL_OF
                : keyType.equals("any of") ? FileParameters.MutableKeywordsParameters.ANY_OF
                : -1;
        params.setKeywords(op, keyText);

        //harness.setParameters(params);
        //TestFinderQueue tfq = createTestFinderQueue();

        int found = 0;

        TestDescription td = null;
        Iterator<TestResult> it = createTestEnum(params);

        //while ((td = tfq.next()) != null) {
        while (it.hasNext()) {
            TestResult tr = it.next();
            try {
                td = tr.getDescription();
            } catch (TestResult.Fault f) {
                log.println("Problem getting TD: " + f);
                failed("TestResult would not give TestDescription");
            }

            String matchData = matchField.equals("TITLE") ? td.getTitle() :
                    matchField.equals("ID") ? td.getId() : null;
            if (!matchData.startsWith(matchPrefix)) {
                log.println("keyInfo: " + keyInfo);
                log.println("found test: " + td.getRootRelativeURL());
                failed("unexpected test found");
            }
            found++;
        }

        if (found != expected) {
            log.println("keyInfo: " + keyInfo);
            log.println("expected number of tests: " + expected);
            log.println("found: " + found);
            failed("wrong number of tests found");
        }
    }

    private void failed(String s) {
        log.println(">>>>>>>>>" + s);
        ok = false;
    }

    /*
    private TestFinderQueue createTestFinderQueue() throws FileNotFoundException, TestSuite.Fault {
    Parameters params = harness.getParameters();
    File testSuiteRoot = params.getTestSuiteRoot();
    File[] tests = params.getTests();
    TestFilter[] filters = params.getFilters(harness.getResultTable());
    TestSuite ts = TestSuite.open(testSuiteRoot);
    TestFinder tf = ts.createTestFinder();
    TestFinderQueue tfq = new TestFinderQueue();
    tfq.setTestFinder(tf);
    tfq.setTests(tests);
    tfq.setFilters(filters);
    return tfq;
    }
    */

    private Iterator<TestResult> createTestEnum(Parameters params) {
    /*
    File testSuiteRoot = params.getTestSuiteRoot();
    TestSuite ts = TestSuite.create(params.getEnv());
    TestFinder tf = ts.createTestFinder(testSuiteRoot);
    */
        //Parameters params = harness.getParameters();
        TestResultTable trt = params.getWorkDirectory().getTestResultTable();
        TestFilter[] filters = params.getFilters();
        String[] tests = params.getTests();

        String[] initialURLs;

        if (tests != null) {
            initialURLs = tests;
        } else {
            initialURLs = new String[0];
        }

        System.out.println(filters.length + " filters.");

        Iterator<TestResult> it;

        if (initialURLs.length == 0) {
            it = trt.getIterator(trt.getRoot(), filters);
        } else {
            it = trt.getIterator(initialURLs, filters);
        }

        return it;
    }

    // executeArgs
    private TestSuite basicTestSuite;
    private File envFile;
    private WorkDirectory testWorkDir;

    private boolean ok = true;
    private PrintStream log;

}
