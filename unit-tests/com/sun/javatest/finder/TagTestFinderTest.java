/*
 * $Id$
 *
 * Copyright (c) 2001, 2019, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.finder;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;

import com.sun.javatest.TU;
import com.sun.javatest.TestDescription;
import com.sun.javatest.TestFilter;
import com.sun.javatest.TestFinder;
import com.sun.javatest.TestFinderQueue;
import com.sun.javatest.finder.TagTestFinder;
import org.junit.Assert;
import org.junit.Test;

public class TagTestFinderTest {

    @Test
    public void test() throws IOException, TestFinder.Fault {
        TagTestFinderTest t = new TagTestFinderTest();
        boolean ok = t.run(System.out);
        Assert.assertTrue(ok);
    }

    public boolean run(PrintStream out) throws IOException, TestFinder.Fault {
        File testSuite = new File(TU.getPathToData() + File.separator + "tagtests");
        int expected = 2;    // exception is possible

        testSuite = new File(testSuite.getCanonicalPath());
        File testSuiteDir = new File(testSuite.getParent());
        this.out = out;

        boolean ok = true;
        ok &= testTests(testSuite, null, expected, 0, null);
    /*
    ok &= testTests(testSuiteDir, null, 11, 0, dirWalk);
    ok &= testTests(testSuiteDir, new String[] { }, 0, 0, dirWalk);
    ok &= testTests(testSuiteDir, new String[] { testSuiteDir.getPath() }, 11, 0, dirWalk);
    ok &= testTests(testSuite,    new String[] { testSuite.getPath() }, 11, 0, webWalk);
    ok &= testTests(testSuiteDir, new String[] { "badFile" }, 0, 2, dirWalk);
    ok &= testTests(testSuiteDir, new String[] { "com" }, 0, 2, dirWalk);
    ok &= testTests(testSuiteDir, new String[] { "comp" }, 6, 0, dirWalk);
    ok &= testTests(testSuiteDir, new String[] { "comp/" }, 6, 0, dirWalk);
    ok &= testTests(testSuite,    new String[] { "comp/index.htm" }, 0, 2, webWalk);
    ok &= testTests(testSuite,    new String[] { "comp/index.html" }, 6, 0, webWalk);
    ok &= testTests(testSuiteDir, new String[] { "exec/" }, 5, 0, dirWalk);
    ok &= testTests(testSuiteDir, new String[] { "exec/index.html#ExecFailError"}, 1, 0, dirWalk);
    */

        return ok;
    }

    boolean testTests(File testSuite, String[] tests,
                      int expectedTests, int expectedErrors,
                      String[] args)
            throws TestFinder.Fault {
        if (tests == null)
            out.println("Checkingresults for null initial files");
        else {
            out.println("Checking results for " + tests.length + " initial files");
            for (int i = 0; i < tests.length; i++) {
                out.println(" " + i + ": " + tests[i]);
            }
        }

        TagTestFinder tf = new TagTestFinder();
        tf.init(null, testSuite, null);
        TestFinderQueue tfq = new TestFinderQueue();
        tfq.setTestFinder(tf);
        tfq.setTests(tests);
        tfq.addObserver(new TFQObserver(tf));

        boolean ok = check(tfq, expectedTests, expectedErrors);
        if (ok)
            out.println("check OK");
        else
            out.println("***** check failed");
        out.println();

        return ok;
    }

    boolean check(TestFinderQueue tfq, int expectedTests, int expectedErrors) {
        boolean ok = true;

        TestDescription td;
        int n = 0;
        while ((td = tfq.next()) != null)
            n++;

        out.print("read " + tfq.getTestsFoundCount() + " tests");
        if (tfq.getTestsFoundCount() == expectedTests)
            out.println(", as expected");
        else {
            out.println(", but expected " + expectedTests);
            ok = false;
        }

        out.print("found " + tfq.getErrorCount() + " errors");
        if (tfq.getErrorCount() == expectedErrors)
            out.println(", as expected");
        else {
            out.println(", but expected " + expectedErrors);
            ok = false;
        }

        return ok;
    }

    void show(TestDescription td, String label) {
        if (td == null)
            out.println(label + ": null");
        else {
            out.println(label + ": " + td.getRootRelativeURL());
            for (Iterator<String> i = td.getParameterKeys(); i.hasNext(); ) {
                String key = i.next();
                String value = td.getParameter(key);
                out.println("   " + key + ": " + value);
            }
        }
    }

    class TFQObserver implements TestFinderQueue.Observer {
        TFQObserver(TestFinder tf) {
            this.tf = tf;
        }

        public void found(File file) {
        }

        public void reading(File file) {
        }

        public void done(File file) {
        }

        public void found(TestDescription td) {
        }

        public void ignored(TestDescription td, TestFilter f) {
        }

        public void done(TestDescription td) {
        }

        public void flushed() {
        }

        public void error(String msg) {
            out.println("***** error reported from " + tf.getClass().getName());
            out.println(msg);
        }

        public void error(TestDescription td, String msg) {
            out.println("***** error reported from " + tf.getClass().getName());
            out.println(td.getRootRelativeURL() + ": " + msg);
        }

        private TestFinder tf;
    }

    private static final String[] noArgs = {};
    private PrintStream out;
}
