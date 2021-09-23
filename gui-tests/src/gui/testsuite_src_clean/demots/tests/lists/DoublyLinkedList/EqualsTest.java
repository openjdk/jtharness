/*
 * $Id$
 *
 * Copyright (c) 2001, 2021, Oracle and/or its affiliates. All rights reserved.
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


package com.sun.demots.tests.lists.DoublyLinkedList;

import java.io.PrintWriter;

import com.sun.javatest.Status;
import com.sun.javatest.Test;

import com.sun.demoapi.lists.LinkedList;

/**
 * A test for com.sun.demoapi.lists.LinkedList.equals.
 *
 * @test
 * @sources EqualsTest.java
 * @executeClass com.sun.demots.tests.lists.LinkedList.EqualsTest
 */
public class EqualsTest implements Test {
    /**
     * A stream to which to write info about test failures.
     */
    private PrintWriter err;

    /**
     * Standard command-line entry point.
     *
     * @param args command line args (ignored)
     */
    public static void main(String[] args) {
        PrintWriter err = new PrintWriter(System.err, true);
        Test t = new EqualsTest();
        Status s = t.run(args, null, err);
        s.exit();
    }

    /**
     * Main test method. The test consists of a series of test cases;
     * the test passes only if all the individual test cases pass.
     *
     * @param args ignored
     * @param out  ignored
     * @param err  a stream to which to write details about test failures
     * @return a Status object indicating if the test passed or failed
     */
    public Status run(String[] args, PrintWriter out, PrintWriter err) {
        // save error stream to which to write error messages
        this.err = err;

        boolean ok = true;

        Integer one = new Integer(1);
        Integer two = new Integer(2);
        Integer three = new Integer(3);

        ok = ok & positive_test(new Object[]{});
        ok = ok & positive_test(new Object[]{null});
        ok = ok & positive_test(new Object[]{"a"});
        ok = ok & positive_test(new Object[]{one, "a"});
        ok = ok & positive_test(new Object[]{one, null, "a"});
        ok = ok & positive_test(new Object[]{"a", "b", "c", "d"});
        ok = ok & positive_test(new Object[]{one, two, three});
        ok = ok & positive_test(new Object[]{one, "a", null, two});
        ok = ok & positive_test(new Object[]{"a", "a", "a", "a"});
        ok = ok & positive_test(new Object[]{"a", "a", one, one});
        ok = ok & positive_test(new Object[]{"a", one, "a", one});
        ok = ok & negative_test(new Object[]{}, new Object[]{"a"});
        ok = ok & negative_test(new Object[]{"a"}, new Object[]{"b"});
        ok = ok & negative_test(new Object[]{"a"}, new Object[]{one});
        ok = ok & negative_test(new Object[]{"a"}, new Object[]{null});
        ok = ok & negative_test(new Object[]{"a", one}, new Object[]{"b", one});
        ok = ok & negative_test(new Object[]{"a", one}, new Object[]{one, one});
        ok = ok & negative_test(new Object[]{"a", one}, new Object[]{null, one});
        ok = ok & negative_test(new Object[]{"a", one}, new Object[]{"a", one, "b"});
        ok = ok & negative_test(new Object[]{"a", one}, one);
        ok = ok & negative_test(new Object[]{"a", one}, "a");

        if (ok)
            return Status.passed("OK");
        else
            return Status.failed("one or more test cases failed");
    }

    boolean positive_test(Object[] data) {
        // create the list two different ways from the same data
        LinkedList list1 = new LinkedList();
        LinkedList list2 = new LinkedList();
        for (int i = 0; i < data.length; i++) {
            list1.insert(data[data.length - i - 1]);
            list2.append(data[i]);
        }

        if (!list1.equals(list2)) {
            err.println("mismatch:");
            err.println("  list1: " + list1);
            err.println("  list2: " + list2);
            return false;
        }

        return true;
    }

    boolean negative_test(Object[] data1, Object[] data2) {
        LinkedList list1 = new LinkedList();
        for (int i = 0; i < data1.length; i++)
            list1.insert(data1[data1.length - i - 1]);

        LinkedList list2 = new LinkedList();
        for (int i = 0; i < data2.length; i++)
            list2.insert(data2[data2.length - i - 1]);

        if (list1.equals(list2)) {
            err.println("lists matched unexpectedly:");
            err.println("  list1: " + list1);
            err.println("  list2: " + list2);
            return false;
        }

        return true;
    }

    boolean negative_test(Object[] data1, Object data2) {
        LinkedList list1 = new LinkedList();
        for (int i = 0; i < data1.length; i++)
            list1.insert(data1[data1.length - i - 1]);

        if (list1.equals(data2)) {
            err.println("lists matched unexpectedly:");
            err.println("  list1: " + list1);
            err.println("  data2: " + data2);
            return false;
        }

        return true;
    }
}

