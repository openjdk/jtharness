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

import com.oracle.tck.lib.autd2.unittests.BaseTestGroup;
import com.oracle.tck.lib.autd2.unittests.TU;
import com.sun.tck.lib.tgf.DataFactory;
import com.sun.tck.lib.tgf.TestData;
import com.sun.tck.lib.tgf.Values;
import com.sun.tck.lib.tgf.data.Ints;
import com.sun.tck.lib.tgf.data.Operation;
import com.sun.tck.lib.tgf.data.Strings;
import com.sun.tck.test.TestCase;
import com.sun.tck.test.TestGroup;
import org.junit.Assert;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class InlinedArgData_ManyArgs {


    @org.junit.Test
    public void int_1() {
        final List<Integer> list = new ArrayList<Integer>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Ints({45, 7, 3}) int i,
                             @Ints({8, 34, 9}) int j,
                             @Ints({7, 5, 14}) int k) throws Throwable {
                list.add(i);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 3, list.size());
        Assert.assertEquals(45, list.get(0), 0.0);
        Assert.assertEquals(7, list.get(1), 0.0);
        Assert.assertEquals(3, list.get(2), 0.0);
    }

    @org.junit.Test
    public void intString_1() {
        final List<Integer> ints = new ArrayList<Integer>();
        final List<String> strings = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Ints({1, 2, 3}) int i,
                             @Strings({"a", "b"}) String s) {
                ints.add(i);
                strings.add(s);
                System.out.println(MessageFormat.format("i = {0}, s = {1}", i, s));
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 3, ints.size());
        Assert.assertEquals(1, ints.get(0), 0.0);
        Assert.assertEquals(2, ints.get(1), 0.0);
        Assert.assertEquals(3, ints.get(2), 0.0);

        Assert.assertEquals( 3, strings.size());
        Assert.assertEquals("a", strings.get(0));
        Assert.assertEquals("b", strings.get(1));
        Assert.assertEquals("a", strings.get(2));
    }

    @org.junit.Test
    public void intString_1_old() {
        final List<Integer> ints = new ArrayList<Integer>();
        final List<String> strings = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            Values data = DataFactory.createColumn(1, 2, 3).pseudoMultiply("a", "b");

            @TestCase
            @TestData("data")
            public void test(int i, String s) {
                ints.add(i);
                strings.add(s);
                System.out.println(MessageFormat.format("i = {0}, s = {1}", i, s));
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 3, ints.size());
        Assert.assertEquals(1, ints.get(0), 0.0);
        Assert.assertEquals(2, ints.get(1), 0.0);
        Assert.assertEquals(3, ints.get(2), 0.0);

        Assert.assertEquals( 3, strings.size());
        Assert.assertEquals("a", strings.get(0));
        Assert.assertEquals("b", strings.get(1));
        Assert.assertEquals("a", strings.get(2));
    }

    @org.junit.Test
    public void intString_mult_1() {
        final List<Integer> ints = new ArrayList<Integer>();
        final List<String> strings = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            @Operation(Operation.TYPE.MULTIPLY)
            public void test(@Ints({1, 2, 3}) int i,
                             @Strings({"a", "b"}) String s) {
                ints.add(i);
                strings.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 6, ints.size());
        Assert.assertEquals(1, ints.get(0), 0.0);
        Assert.assertEquals(1, ints.get(1), 0.0);
        Assert.assertEquals(2, ints.get(2), 0.0);
        Assert.assertEquals(2, ints.get(3), 0.0);
        Assert.assertEquals(3, ints.get(4), 0.0);
        Assert.assertEquals(3, ints.get(5), 0.0);

        Assert.assertEquals( 6, strings.size());
        Assert.assertEquals("a", strings.get(0));
        Assert.assertEquals("b", strings.get(1));
        Assert.assertEquals("a", strings.get(2));
        Assert.assertEquals("b", strings.get(3));
        Assert.assertEquals("a", strings.get(4));
        Assert.assertEquals("b", strings.get(5));
    }

    @org.junit.Test
    public void intString_mult_2() {
        final List<Integer> ints = new ArrayList<Integer>();
        final List<String> strings = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            private List<Integer> ints() {
                return Arrays.asList(1, 2, 3);
            }

            @TestCase
            @Operation(Operation.TYPE.MULTIPLY)
            public void test(@TestData("ints") int i,
                             @Strings({"a", "b"}) String s) {
                ints.add(i);
                strings.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 6, ints.size());
        Assert.assertEquals(1, ints.get(0), 0.0);
        Assert.assertEquals(1, ints.get(1), 0.0);
        Assert.assertEquals(2, ints.get(2), 0.0);
        Assert.assertEquals(2, ints.get(3), 0.0);
        Assert.assertEquals(3, ints.get(4), 0.0);
        Assert.assertEquals(3, ints.get(5), 0.0);

        Assert.assertEquals( 6, strings.size());
        Assert.assertEquals("a", strings.get(0));
        Assert.assertEquals("b", strings.get(1));
        Assert.assertEquals("a", strings.get(2));
        Assert.assertEquals("b", strings.get(3));
        Assert.assertEquals("a", strings.get(4));
        Assert.assertEquals("b", strings.get(5));
    }

    @org.junit.Test
    public void intString_mult_3() {
        final List<Integer> ints = new ArrayList<Integer>();
        final List<String> strings = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            List<Integer> intsData = Arrays.asList(1, 2, 3);

            @TestCase
            @Operation(Operation.TYPE.MULTIPLY)
            public void test(@TestData("intsData") int i,
                             @Strings({"a", "b"}) String s) {
                ints.add(i);
                strings.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 6, ints.size());
        Assert.assertEquals(1, ints.get(0), 0.0);
        Assert.assertEquals(1, ints.get(1), 0.0);
        Assert.assertEquals(2, ints.get(2), 0.0);
        Assert.assertEquals(2, ints.get(3), 0.0);
        Assert.assertEquals(3, ints.get(4), 0.0);
        Assert.assertEquals(3, ints.get(5), 0.0);

        Assert.assertEquals( 6, strings.size());
        Assert.assertEquals("a", strings.get(0));
        Assert.assertEquals("b", strings.get(1));
        Assert.assertEquals("a", strings.get(2));
        Assert.assertEquals("b", strings.get(3));
        Assert.assertEquals("a", strings.get(4));
        Assert.assertEquals("b", strings.get(5));
    }

    @org.junit.Test
    public void intString_mult_4() {
        final List<Integer> ints = new ArrayList<Integer>();
        final List<String> strings = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            List<Integer> intsData = Arrays.asList(1, 2, 3);
            String[] stringsData = {"a", "b"};

            @TestCase
            @Operation(Operation.TYPE.MULTIPLY)
            public void test(@TestData("intsData") int i,
                             @TestData("stringsData") String s) {
                ints.add(i);
                strings.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 6, ints.size());
        Assert.assertEquals(1, ints.get(0), 0.0);
        Assert.assertEquals(1, ints.get(1), 0.0);
        Assert.assertEquals(2, ints.get(2), 0.0);
        Assert.assertEquals(2, ints.get(3), 0.0);
        Assert.assertEquals(3, ints.get(4), 0.0);
        Assert.assertEquals(3, ints.get(5), 0.0);

        Assert.assertEquals( 6, strings.size());
        Assert.assertEquals("a", strings.get(0));
        Assert.assertEquals("b", strings.get(1));
        Assert.assertEquals("a", strings.get(2));
        Assert.assertEquals("b", strings.get(3));
        Assert.assertEquals("a", strings.get(4));
        Assert.assertEquals("b", strings.get(5));
    }


    @Test
    public void compatibleTypesOfData_1() {
        MyTest testInstance = new MyTest() {
            Values myInts = DataFactory.createColumn( 1, 2, 3);
            @TestCase
            public void wrongTest(@TestData("myInts") int i, @Strings({"a", "b", "c"}) String s) {
                methodWasCalled = true;
            }
        };
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(testInstance, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertTrue(testInstance.methodWasCalled);
        Assert.assertEquals(
                "Passed. test cases: 1; all passed",
                status.toString());
    }

    @TestGroup
    private class MyTest {
        boolean methodWasCalled;
    }

}
