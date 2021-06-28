/*
 * $Id$
 *
 * Copyright (c) 2002, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.demots.tests.lists.SortedList;

import java.io.PrintWriter;

import com.sun.javatest.Status;
import com.sun.javatest.Test;

import com.sun.demoapi.lists.SortedList;

/**
 * A test for com.sun.demoapi.lists.SortedList.equals.
 *
 * @test
 * @sources EqualsTest.java
 * @executeClass com.sun.demots.tests.lists.SortedList.EqualsTest
 */
public class EqualsTest implements Test
{
    /**
     * Standard command-line entry point.
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
     * @param args ignored
     * @param out  ignored
     * @param err  a stream to which to write details about test failures
     * @return a Status object indicating if the test passed or failed
     */
    public Status run(String[] args, PrintWriter out, PrintWriter err) {
    // save error stream to which to write error messages
    this.err = err;

    SortedList.Comparator intComp = new SortedList.Comparator() {
        public int compare(Object a, Object b) {
            int ia = ((Integer) a).intValue();
            int ib = ((Integer) b).intValue();
            return (ia < ib ? -1 : ia == ib ? 0 : 1);
        }
        };

    Integer one = new Integer(1);
    Integer two = new Integer(2);
    Integer three = new Integer(3);

    boolean ok = true;

    ok = ok & positive_test(new Object[] { }, intComp);
    ok = ok & positive_test(new Object[] { one }, intComp);
    ok = ok & positive_test(new Object[] { one, two }, intComp);
    ok = ok & positive_test(new Object[] { one, two, three }, intComp);
    ok = ok & positive_test(new Object[] { one, two, one, two }, intComp);
    ok = ok & negative_test(new Object[] { }, new Object[] { one }, intComp);
    ok = ok & negative_test(new Object[] { one }, new Object[] { two }, intComp);
    ok = ok & negative_test(new Object[] { one, one }, new Object[] { two, one }, intComp);
    ok = ok & negative_test(new Object[] { one, two }, new Object[] { one, two, three }, intComp);

    if (ok)
        return Status.passed("OK");
    else
        return Status.failed("one or more test cases failed");
    }

    boolean positive_test(Object[] data, SortedList.Comparator c) {
    // create the list two different ways from the same data
    SortedList list1 = new SortedList(c, true);
    SortedList list2 = new SortedList(c, true);
    for (int i = 0; i < data.length; i++) {
        list1.insert(data[data.length -i - 1]);
        list2.insert(data[i]);
    }

    if (!list1.equals(list2)) {
        err.println("mismatch:");
        err.println("  list1: " + list1);
        err.println("  list2: " + list2);
        return false;
    }

    return true;
    }

    boolean negative_test(Object[] data1, Object[] data2, SortedList.Comparator c) {
    SortedList list1 = new SortedList(c, true);
    for (int i = 0; i < data1.length; i++)
        list1.insert(data1[data1.length - i - 1]);

    SortedList list2 = new SortedList(c, true);
    for (int i = 0; i < data2.length; i++)
        list2.insert(data2[data2.length - i - 1]);

    if (list1.equals(list2)){
        err.println("lists matched unexpectedly:");
        err.println("  list1: " + list1);
        err.println("  list2: " + list2);
        return false;
    }

    return true;
    }

    boolean negative_test(Object[] data1, Object data2, SortedList.Comparator c) {
    SortedList list1 = new SortedList(c, true);
    for (int i = 0; i < data1.length; i++)
        list1.insert(data1[data1.length - i - 1]);

    if (list1.equals(data2)){
        err.println("lists matched unexpectedly:");
        err.println("  list1: " + list1);
        err.println("  data2: " + data2);
        return false;
    }

    return true;
    }

    /**
     * A stream to which to write info about test failures.
     */
    private PrintWriter err;
}

