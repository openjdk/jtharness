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
 * A test for com.sun.demoapi.lists.SortedList.remove.
 *
 * @test
 * @sources RemoveTest.java
 * @executeClass com.sun.demots.tests.lists.SortedList.RemoveTest
 */
public class RemoveTest implements Test
{
    /**
     * Standard command-line entry point.
     * @param args command line args (ignored)
     */
    public static void main(String[] args) {
        PrintWriter err = new PrintWriter(System.err, true);
        Test t = new RemoveTest();
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

        Integer i0 = Integer.valueOf(0);
        Integer i10 = Integer.valueOf(10);
        Integer i100 = Integer.valueOf(100);
        Integer i1000 = Integer.valueOf(1000);
        Integer i10000 = Integer.valueOf(10000);

        boolean ok = true;

        ok = ok & test(new Object[] { i0 }, intComp);
        ok = ok & test(new Object[] { i0, i10 }, intComp);
        ok = ok & test(new Object[] { i0, i10, i100 }, intComp);
        ok = ok & test(new Object[] { i0, i10, i100, i1000 }, intComp);
        ok = ok & test(new Object[] { i0, i10, i100, i1000, i10000 }, intComp);
        ok = ok & test(new Object[] { i0, i0, i10, i100 }, intComp);
        ok = ok & test(new Object[] { i0, i10, i0, i100 }, intComp);
        ok = ok & test(new Object[] { i0, i10, i100, i0 }, intComp);

        if (ok)
            return Status.passed("OK");
        else
            return Status.failed("one or more test cases failed");
    }

    boolean test(Object[] data, SortedList.Comparator c) {
        err.println("test: " + toString(data, 0, data.length));
        boolean ok1 = testRemoveFirst(data, c);
        boolean ok2 = testRemoveLast(data, c);
        boolean ok3 = testRemoveMiddle(data, c);
        return (ok1 & ok2 & ok3);
    }

    boolean testRemoveFirst(Object[] data, SortedList.Comparator c) {
        SortedList list = createList(data, c);
        Object[] ref = sortedCopy(data, c);

        for (int i = 0; i < ref.length; i++) {
            int refLen = ref.length - i;
            // select item to remove
            Object o = ref[0];
            // remove from list
            list.remove(o);
            // remove from reference array
            if (remove(ref, 0, refLen, o))
                refLen--;
            // compare list and reference
            if (!verify(list, ref, 0, refLen)) {
                err.println("removeFirst mismatch:");
                err.println("  expected: " + toString(ref, 0, refLen));
                err.println("     found: " + list);
                return false;
            }

        }

        return true;
    }

    boolean testRemoveLast(Object[] data, SortedList.Comparator c) {
        SortedList list = createList(data, c);
        Object[] ref = sortedCopy(data, c);

        for (int i = 0; i < ref.length; i++) {
            int refLen = ref.length - i;
            // select item to remove
            Object o = ref[refLen - 1];
            // remove from list
            list.remove(o);
            // remove from reference array
            if (remove(ref, 0, refLen, o))
                refLen--;
            // compare list and reference
            if (!verify(list, ref, 0, refLen)) {
                err.println("removeLast mismatch:");
                err.println("  expected: " + toString(ref, 0, refLen));
                err.println("     found: " + list);
                return false;
            }

        }

        return true;
    }

    boolean testRemoveMiddle(Object[] data, SortedList.Comparator c) {
        SortedList list = createList(data, c);
        Object[] ref = sortedCopy(data, c);

        for (int i = 0; i < ref.length; i++) {
            int refLen = ref.length - i;
            // select item to remove
            Object o = ref[refLen / 2];
            // remove from list
            list.remove(o);
            // remove from reference array
            if (remove(ref, 0, refLen, o))
                refLen--;
            // compare list and reference
            if (!verify(list, ref, 0, refLen)) {
                err.println("removeLast mismatch:");
                err.println("  expected: " + toString(ref, 0, refLen));
                err.println("     found: " + list);
                return false;
            }

        }

        return true;
    }

    Object[] copy(Object[] data) {
        Object[] d = new Object[data.length];
        System.arraycopy(data, 0, d, 0, data.length);
        return d;
    }

    SortedList createList(Object[] data, SortedList.Comparator c) {
        SortedList list = new SortedList(c, false);
        for (int i = data.length - 1; i >= 0; i--)
            list.insert(data[i]);
        return list;
    }

    boolean remove(Object[] data, int offset, int length, Object o) {
        for (int i = offset; i < offset + length; i++) {
            boolean found = (data[i] == null ? o == null : data[i].equals(o));
            if (found) {
                System.arraycopy(data, i + 1, data, i, offset + length - i - 1);
                return true;
            }
        }
        // not found
        return false;
    }

    void sort(Object[] data, int offset, int length, SortedList.Comparator c) {
        for (int i = offset; i < offset + length; i++) {
            for (int j = i; j > offset && c.compare(data[j - 1], data[j]) > 0; j--) {
                Object o = data[j];
                data[j] = data[j-1];
                data[j - 1] = o;
            }
        }
    }

    Object[] sortedCopy(Object[] data, SortedList.Comparator c) {
        Object[] newData = copy(data);
        sort(newData, 0, data.length, c);
        return newData;
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


    boolean verify(SortedList l, Object[] ref, int offset, int length) {
        int i = offset;
        for (SortedList.Entry e = l.getFirst(); e != null; e = e.getNext()) {
            if (i < ref.length) {
                Object d = e.getData();
                if (d == null) {
                    if (ref[i] != null)
                    // data difference found
                        return false;
                }
                else if (!d.equals(ref[i]))
                    // data difference found
                    return false;
            }
            else
                // list is longer than ref
                return false;

            i++;
        }

        return (i == offset + length);
    }

    /**
     * A stream to which to write info about test failures.
     */
    private PrintWriter err;
}
