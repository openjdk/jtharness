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

package com.sun.javatest.util;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class StringArrayTest {

    @Test
    public void test() {
        boolean ok = true;

        ok = ok && testSplit();
        ok = ok && testSplitList();

        Assert.assertTrue(ok);
    }

    private static boolean testSplit() {
        String[] r = StringArray.split(t6);
        if (!Arrays.equals(r6, r)) {
            System.out.println("Split 1 failed");
            return false;
        }

        return true;
    }

    private static boolean testSplitList() {
        String[] r = StringArray.splitList(t1, ",");
        if (!Arrays.equals(r1, r)) {
            System.out.println("SplitList 1 failed");
            return false;
        }

        r = StringArray.splitList(t2, ",");
        if (!Arrays.equals(r2, r)) {
            System.out.println("SplitList 2 failed");
            return false;
        }

        r = StringArray.splitList(t3, "and");
        if (!Arrays.equals(r3, r)) {
            System.out.println("SplitList 3 failed");
            return false;
        }

        r = StringArray.splitList(t4, ",");
        if (!Arrays.equals(r4, r)) {
            System.out.println("SplitList 4 failed");
            return false;
        }

        r = StringArray.splitList(t5, ",");
        if (!Arrays.equals(r5, r)) {
            System.out.println("SplitList 5 failed");
            return false;
        }

        // all pass
        return true;
    }

    private static final String t1 = "foo,bar,baz";
    private static final String[] r1 = {"foo", "bar", "baz"};

    private static final String t2 = "foo, bar,   baz";
    private static final String[] r2 = r1;

    private static final String t3 = "foo and bar and baz";
    private static final String[] r3 = {"foo ", "bar ", "baz"};

    private static final String t4 = "foo, bar, baz,";
    private static final String[] r4 = r2;

    private static final String t5 = "foo, bar, baz, ";
    private static final String[] r5 = r2;

    private static final String t6 = "foo bar baz";
    private static final String[] r6 = r1;
}
