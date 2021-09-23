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

import com.sun.demoapi.lists.DoublyLinkedList;

/**
 * A test for com.sun.demoapi.lists.DoublyLinkedList.insert.
 *
 * @test
 * @sources InsertTest.java
 * @executeClass com.sun.demots.tests.lists.DoublyLinkedList.InsertTest
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

        boolean ok = true;

        Integer one = new Integer(1);
        Integer two = new Integer(2);
        Integer three = new Integer(3);

        ok = ok & test(new Object[]{"a", "b", "c", "d"});
        ok = ok & test(new Object[]{one, two, three});
        ok = ok & test(new Object[]{one, "a", null, two});
        ok = ok & test(new Object[]{"a", "a", "a", "a"});
        ok = ok & test(new Object[]{"a", "a", one, one});
        ok = ok & test(new Object[]{"a", one, "a", one});

        if (ok)
            return Status.passed("OK");
        else
            return Status.failed("one or more test cases failed");
    }

    boolean test(Object[] data) {
        String expected = "";
        DoublyLinkedList list = new DoublyLinkedList();
        for (int i = 0; i < data.length; i++) {
            Object d = data[i];
            // The following line is deliberately incorrect, so that
            // the tutorial can illustrate the effects of a test that
            // fails.  The line should really be
            //    list.insert(d);
            list.append(d);
            if (i == 0)
                expected = String.valueOf(d);
            else
                expected = String.valueOf(d) + "," + expected;

            if (!verify(list, expected)) {
                err.println("mismatch:");
                err.println("  expected: " + expected);
                err.println("     found: " + list);
                return false;
            }
        }
        return true;
    }

    boolean verify(DoublyLinkedList l, String expected) {
        return verifyForwards(l, expected) && verifyBackwards(l, expected);
    }

    boolean verifyForwards(DoublyLinkedList l, String expected) {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (DoublyLinkedList.Entry e = l.getFirst(); e != null; e = e.getNext()) {
            if (first)
                first = false;
            else
                sb.append(",");
            sb.append(String.valueOf(e.getData()));
        }

        //System.err.println(l);
        return (sb.toString().equals(expected));
    }

    boolean verifyBackwards(DoublyLinkedList l, String expected) {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (DoublyLinkedList.Entry e = l.getLast(); e != null; e = e.getPrevious()) {
            if (first)
                first = false;
            else
                sb.insert(0, ",");
            sb.insert(0, String.valueOf(e.getData()));
        }

        //System.err.println(l);
        return (sb.toString().equals(expected));
    }
}

