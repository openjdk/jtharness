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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Dictionary;
import java.util.Enumeration;

import com.sun.javatest.KeyTest;
import com.sun.javatest.TU;
import com.sun.javatest.TestDescription;
import com.sun.javatest.TestFilter;
import com.sun.javatest.TestFinder;
import com.sun.javatest.TestFinderQueue;
import com.sun.javatest.finder.HTMLTestFinder;
import org.junit.Assert;
import org.junit.Test;

/*
 * Test the HTMLTestFinder in web walk mode.  It should produce an error if a
 * href target does not exist.  This code is based on HTMLTestFinderTest.
 */

public class MissingLinkTest {

    @Test
    public void test() throws IOException, TestFinder.Fault {
        MissingLinkTest t = new MissingLinkTest();
        boolean ok = t.run(System.out);
        Assert.assertTrue(ok);
    }

    public boolean run(PrintStream out) throws IOException, TestFinder.Fault {
        this.out = out;
        File testSuite = new File(TU.getPathToData() + File.separator + "finder/data/missingLink/testsuite.html");
        File testSuiteDir = new File(testSuite.getParent());
        String[] webWalk = {"-webWalk"};
        boolean ok = true;

        testSuite = new File(testSuite.getCanonicalPath());

        HTMLTestFinder hf = new HTMLTestFinder();
        hf.init(webWalk, testSuite, null);
        TestFinderQueue hfq = new TestFinderQueue();
        hfq.setTestFinder(hf);
        hfq.addObserver(new TFQObserver(hf));
        hfq.setTests((String[])null);

        if (ok)
            out.println("check OK");
        else
            out.println("***** check failed");
        out.println();

        TestDescription td;
        int n = 0;
        while ((td = hfq.next()) != null)
            n++;

        out.print("Read " + hfq.getTestsFoundCount() + " tests.");
        out.print("Found " + hfq.getErrorCount() + " errors.");
        if (hfq.getErrorCount() == expectedErrors)
            out.println(", as expected");
        else {
            out.println(", but expected " + expectedErrors);
            ok = false;
        }

        return ok;
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

    private PrintStream out;
    private static int expectedErrors = 1;
}
