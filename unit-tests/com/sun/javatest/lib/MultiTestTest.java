/*
 * $Id$
 *
 * Copyright (c) 1996, 2020, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.lib;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

import com.sun.javatest.Status;
import org.junit.Assert;
import org.junit.Test;

public class MultiTestTest {


    Vector<String> testcases = new Vector<>();
    Vector<String> sortedTestcases = new Vector<>();

    @Test
    public void test() {
        MultiTestTest test = new MultiTestTest();
        boolean ok = test.run(System.out);
        Assert.assertTrue(ok);
    }

    public boolean run(PrintStream out) {
        boolean ok = true;
        String[] args = new String[0];

        sortedTestcases.addElement("test0001");
        sortedTestcases.addElement("test0002");
        sortedTestcases.addElement("test0003");
        sortedTestcases.addElement("test0004");
        sortedTestcases.addElement("test0005");

        if (!testcaseExecutionCheck(args) || !testcaseOrderCheck(args)) {
            failed();
            return false;
        }

        return true;
    }

    private void failed() {
        System.out.println("MultiTest tests failed...");
    }

    private boolean testcaseExecutionCheck(String[] args) {
        Vector<String> v = new Vector<>();

        // verify that all testcases are executed
        SampleTest test1 = new SampleTest();
        test1.run(args, System.err, System.out);
        v = sort(testcases);
        if (!v.equals(sortedTestcases)) {
            System.out.println("testcaseExecutionCheck: all testcases were not executed.");
            return false;
        }

        return true;
    }

    private boolean testcaseOrderCheck(String[] args) {
        Vector<String> reverseSortedTestcases = new Vector<>();

        reverseSortedTestcases.addElement("test0005");
        reverseSortedTestcases.addElement("test0004");
        reverseSortedTestcases.addElement("test0003");
        reverseSortedTestcases.addElement("test0002");
        reverseSortedTestcases.addElement("test0001");

        // verify that testcases are executed in ascending order
        SampleTest test2 = new SampleTest();
        System.setProperty("multitest.testcaseOrder", "sorted");
        testcases.removeAllElements();
        test2.run(args, System.err, System.out);
        if (!testcases.equals(sortedTestcases)) {
            System.out.println("testcaseOrderCheck: testcases were not executed in ascending order.");
            return false;
        }

        // verify that testcases are executed in descending order
        SampleTest test3 = new SampleTest();
        System.setProperty("multitest.testcaseOrder", "reverseSorted");
        testcases.removeAllElements();
        Status s = test3.run(args, System.err, System.out);
        if (!testcases.equals(reverseSortedTestcases)) {
            System.out.println("testcaseOrderCheck: testcases were not executed in descending order.");
            return false;
        }

        return true;
    }

    public Vector<String> sort(Vector<String> v) {
        Vector<String> sorted = new Vector<>();
        String[] arr = new String[v.size()];

        int j = 0;
        for (Enumeration<String> e = v.elements(); e.hasMoreElements(); ) {
            arr[j] = e.nextElement();
            j++;
        }
        Arrays.sort(arr);

        for (String anArr : arr) sorted.addElement(anArr);

        return sorted;
    }

    public class SampleTest extends MultiTest {
        public Status test0003() {
            testcases.addElement("test0003");
            return Status.passed("");
        }

        public Status test0005() {
            testcases.addElement("test0005");
            return Status.passed("");
        }

        public Status test0002() {
            testcases.addElement("test0002");
            return Status.passed("");
        }

        public Status test0001() {
            testcases.addElement("test0001");
            return Status.passed("");
        }

        public Status test0004() {
            testcases.addElement("test0004");
            return Status.passed("");
        }

    }

}

