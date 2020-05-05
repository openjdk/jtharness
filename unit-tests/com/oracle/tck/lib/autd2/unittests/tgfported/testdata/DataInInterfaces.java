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
package com.oracle.tck.lib.autd2.unittests.tgfported.testdata;

import com.oracle.tck.lib.autd2.NonTestCase;
import com.oracle.tck.lib.autd2.unittests.TU;
import com.sun.tck.lib.tgf.DataFactory;
import com.sun.tck.lib.tgf.TestData;
import com.sun.tck.lib.tgf.Values;
import com.sun.tck.test.TestCase;
import com.sun.tck.test.TestGroup;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DataInInterfaces {

    @org.junit.Test
    public void staticMethod() {

        final List<Integer> set = new ArrayList<>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseInterface1() {
            @TestCase
            @TestData("setupStatic")
            public void test(int i) throws Throwable {
                set.add(i);
            }
        }, TU.EMPTY_ARGV);

        Assert.assertTrue(status.isOK());
        Assert.assertEquals(4, set.size());
        Assert.assertEquals(1, (int) set.get(0));
        Assert.assertEquals(3444, (int) set.get(1));
        Assert.assertEquals(7, (int) set.get(2));
        Assert.assertEquals(9, (int) set.get(3));
    }

    @org.junit.Test
    public void defaultMethod() {

        final List<Integer> set = new ArrayList<>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseInterface2() {
            @TestCase
            @TestData("setupDefault")
            public void test(int i) throws Throwable {
                set.add(i);
            }
        }, TU.EMPTY_ARGV);

        Assert.assertTrue(status.isOK());
        Assert.assertEquals(4, set.size());
        Assert.assertEquals(1, (int) set.get(0));
        Assert.assertEquals(3, (int) set.get(1));
        Assert.assertEquals(7, (int) set.get(2));
        Assert.assertEquals(9234234, (int) set.get(3));
    }

    @TestGroup
    static interface BaseInterface1 {
        public final Values setupStatic = DataFactory.createColumn(1, 3444, 7, 9);
    }

    @TestGroup
    static interface BaseInterface2 {
        @NonTestCase
        default Values setupDefault() {
            return DataFactory.createColumn(1, 3, 7, 9234234);
        }
    }

}
