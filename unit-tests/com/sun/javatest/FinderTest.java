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
package com.sun.javatest;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

import com.sun.javatest.finder.HTMLTestFinder;
import com.sun.javatest.util.StringArray;
import org.junit.Assert;
import org.junit.Test;

public class FinderTest {

    @Test
    public void test() {
        FinderTest ft = new FinderTest();
        boolean ok = ft.run(new String[] {TestUtil.getPathToData() + File.separator + "finder" + File.separator + "data" + File.separator + "findertest" + File.separator + "testsuite.html"}, System.out);
        Assert.assertTrue(ok);
    }


    @SuppressWarnings("deprecation")
    public boolean run(String[] args, PrintStream out) {
        int nTests = 0;

        this.out = out;

        // expect args to be finderTestSuite
        if (args.length != 1) {
            failed("Wrong # args (expected 2, got " + args.length + ")");
            return false;
        }


        File testSuite;
        try {
            testSuite = new File(new File(args[0]).getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace(out);
            return false;
        }

        String[] finderArgs = {};
        out.println("Reading files from " + testSuite);

        // run a TestFinder over the tests and check the test descriptions
        // that are found
        TestFinder tf = new HTMLTestFinder();
        try {
            // TestFinder.init() takes an environment to support some test finders.
            // The implementation of HTMLTestFinder does not require a
            // TestEnvironment, thus, it's fine to create an empty one.
            TestEnvironment env = null;
            try {
                env = new TestEnvironment("", new HashMap<>(), "");
            } catch (TestEnvironment.Fault e) {
                failed("unable to create empty test environment");
                return false;
            }
            tf.init(finderArgs, testSuite, null, null, env);
        } catch (TestFinder.Fault e) {
            failed("Error initializing test-finder class: com.sun.javatest.finder.HTMLTestFinder");
            return false;
        }
        TestFinderQueue tfq = new TestFinderQueue();
        tfq.setTestFinder(tf);
        tfq.setTests((String[])null);
        tfq.addObserver(new TFQObserver());
        TestDescription td;
        while ((td = tfq.next()) != null) {
            out.println("found " + td.getRootRelativeURL());
            nTests++;

            String title = td.getTitle();
            if (!title.startsWith(expectedTitlePrefix)) {
                out.println("title is " + title);
                out.println("expected title to begin with `" + expectedTitlePrefix + "'");
                failed("unexpected title found for " + td.getRootRelativeURL());
            }

            String name = td.getName();
            if (!name.startsWith(expectedNamePrefix)) {
                out.println("name is " + name);
                out.println("expected name to begin with `" + expectedNamePrefix + "'");
                failed("unexpected name found for " + td.getRootRelativeURL());
            }

            String[] source = td.getSources();
            if (!(source.length == 1 && source[0].equals(expectedSource))) {
                out.println("source is " + StringArray.join(source));
                out.println("expected source to be `" + expectedSource + "'");
                failed("unexpected source found for " + td.getRootRelativeURL());
            }

            String executeClass = td.getExecuteClass();
            if (!executeClass.equals(expectedClass)) {
                out.println("class is " + executeClass);
                out.println("expected class to be `" + expectedClass + "'");
                failed("unexpected executeClass found for " + td.getRootRelativeURL());
            }

        }

        if (nTests != expectedTests) {
            out.println("number of tests found: " + nTests);
            out.println("expected number of tests: " + expectedTests);
            failed("unexpected number of tests found");
        }

        out.flush();
        return ok;
    }

    private class TFQObserver implements TestFinderQueue.Observer {
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
            out.println("***** error reported from testfinder");
            out.println(msg);
            ok = false;
        }

        public void error(TestDescription td, String msg) {
            out.println("***** error reported from testfinder");
            out.println(td.getRootRelativeURL() + ": " + msg);
            ok = false;
        }
    }

    private void failed(String s) {
        out.println(s);
        ok = false;
    }

    private boolean ok = true;
    private PrintStream out;

    private static final String expectedTitlePrefix = "Finder test:";
    private static final String expectedNamePrefix = "testsuite_finder";
    private static final String expectedSource = "FinderTest.java";
    private static final String expectedClass = "javasoft.sqe.tests.harness.finder.FinderTest";
    private static final int expectedTests = 6;
}
