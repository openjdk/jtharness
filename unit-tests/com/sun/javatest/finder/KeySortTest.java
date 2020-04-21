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
package com.sun.javatest.finder;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import com.sun.javatest.TestDescription;
import com.sun.javatest.TestFilter;
import com.sun.javatest.TestFinder;
import com.sun.javatest.TestFinderQueue;
import com.sun.javatest.TestUtil;
import com.sun.javatest.finder.HTMLTestFinder;
import com.sun.javatest.util.StringArray;
import org.junit.Assert;
import org.junit.Test;

public class KeySortTest {

    @Test
    public void test() throws IOException, TestFinder.Fault {
        boolean ok;
        KeySortTest kst = new KeySortTest();
        ok = kst.run(new String[]{TestUtil.getPathToTestTestSuite("demotck", "testsuite.html")}, System.out);
        Assert.assertTrue(ok);
    }


    public boolean run(String[] args, PrintStream out) throws IOException, TestFinder.Fault {
        File testSuite = new File(new File(args[0]).getAbsolutePath());
        String[] tests = {testSuite.getPath()};

        HTMLTestFinder tf = new HTMLTestFinder();
        tf.init(new String[]{"-webWalk"}, testSuite, null);

        TestFinderQueue tfq = new TestFinderQueue();
        tfq.setTestFinder(tf);
        tfq.setTests(tests);
        tfq.addObserver(new TFQObserver(tf, out));

        boolean ok = true;
        int n = 0;

        TestDescription td;
        while ((td = tfq.next()) != null) {
            out.println("test: " + td.getRootRelativeURL());
            n++;
            String keywords = td.getParameter("keywords");
            String[] keys = StringArray.split(keywords);
            for (int i = 0; i < keys.length - 2; i++) {
                int x = keys[i].compareTo(keys[i + 1]);
                if (x >= 0) {
                    out.println("error: " + keywords);
                    ok = false;
                }
            }
        }

        out.println(n + " tests found");
        return ok;
    }

    class TFQObserver implements TestFinderQueue.Observer {
        TFQObserver(TestFinder tf, PrintStream out) {
            this.tf = tf;
            this.out = out;
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
            out.println(td.getRootRelativeURL() + ": ");
            out.println(msg);
        }

        private TestFinder tf;
        private PrintStream out;
    }

    private boolean ok = true;
}
