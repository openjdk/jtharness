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


package com.sun.demots.tests.lists.SortedList;

import java.io.PrintWriter;

import com.sun.javatest.Status;
import com.sun.javatest.Test;

import com.sun.demoapi.lists.SortedList;

/**
 * A test for com.sun.demoapi.lists.SortedList.insert.
 *
 * @test
 * @sources InsertTest.java
 * @executeClass com.sun.demots.tests.lists.SortedList.InsertTest
 */
public class InsertTest implements Test {
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
        Test t = new InsertTest();
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

        SortedList.Comparator intComp = new SortedList.Comparator() {
            public int compare(Object a, Object b) {
                int ia = ((Integer) a).intValue();
                int ib = ((Integer) b).intValue();
                return (ia < ib ? -1 : ia == ib ? 0 : 1);
            }
        };

        Integer i0 = Integer.valueOf(0);
        Integer i10 = Integer.valueOf(10);
        Integer i100 = Integer.valueOf(100);
        Integer i1000 = Integer.valueOf(1000);
        Integer i10000 = Integer.valueOf(10000);

        boolean ok = true;

        ok = ok & test(new Object[]{i0, i10, i100, i1000, i10000}, intComp);
        ok = ok & test(new Object[]{i10000, i1000, i100, i10, i0}, intComp);
        ok = ok & test(new Object[]{i0, i10000, i10, i1000, i100}, intComp);
        ok = ok & test(new Object[]{i0, i0, i0, i0, i0}, intComp);
        ok = ok & test(new Object[]{i0, i0, i10000, i0, i0}, intComp);
        ok = ok & test(new Object[]{i0, i0, i10000, i10000, i0}, intComp);
        ok = ok & test(new Object[]{i0, i0, i10000, i10000, i0}, intComp);
        ok = ok & test(new Object[]{i0, i10000, i10000, i10000, i10000}, intComp);

        if (ok)
            return Status.passed("OK");
        else
            return Status.failed("one or more test cases failed");
    }

    boolean test(Object[] data, SortedList.Comparator c) {
        return testAllowDups(data, c) && testNoDups(data, c);
    }

    boolean testAllowDups(Object[] data, SortedList.Comparator c) {
        err.println("test: " + toString(data, 0, data.length));
        SortedList list = new SortedList(c, false);
        Object[] expected = new Object[data.length];

        int n = 0;
        for (int i = 0; i < data.length; i++) {
            list.insert(data[i]);
            expected[n++] = data[i];
            sort(expected, 0, n, c);
            if (!verify(list, expected, 0, n, c)) {
                err.println("mismatch:");
                err.println("  expected: " + toString(expected, 0, n));
                err.println("     found: " + list);
                return false;
            }
        }

        return true;
    }

    boolean testNoDups(Object[] data, SortedList.Comparator c) {
        SortedList list = new SortedList(c, true);
        Object[] expected = new Object[data.length];

        int n = 0;
        for (int i = 0; i < data.length; i++) {
            list.insert(data[i]);
            insert:
            {
                for (int j = 0; j < n; j++) {
                    if (c.compare(data[i], expected[j]) == 0)
                        break insert;
                }
                expected[n++] = data[i];
                sort(expected, 0, n, c);
            }
            if (!verify(list, expected, 0, n, c)) {
                err.println("mismatch:");
                err.println("  expected: " + toString(expected, 0, n));
                err.println("     found: " + list);
                return false;
            }
        }

        return true;
    }

    void sort(Object[] data, int offset, int length, SortedList.Comparator c) {
        for (int i = offset; i < offset + length; i++) {
            for (int j = i; j > offset && c.compare(data[j - 1], data[j]) > 0; j--) {
                Object o = data[j];
                data[j] = data[j - 1];
                data[j - 1] = o;
            }
        }
    }

    String toString(Object[] array, int offset, int length) {
        StringBuffer sb = new StringBuffer();
        for (int i = offset; i < (offset + length); i++) {
            if (i > offset)
                sb.append(",");
            sb.append(String.valueOf(array[i]));
        }
        return sb.toString();
    }

    boolean verify(SortedList l, Object[] expected, int offset, int length, SortedList.Comparator c) {
        int i = offset;
        for (SortedList.Entry e = l.getFirst(); e != null; e = e.getNext()) {
            if (i < expected.length) {
                if (c.compare(e.getData(), expected[i]) != 0)
                    // data difference found
                    return false;
            } else
                // list is longer than expected
                return false;

            i++;
        }

        return (i == offset + length);
    }
}
