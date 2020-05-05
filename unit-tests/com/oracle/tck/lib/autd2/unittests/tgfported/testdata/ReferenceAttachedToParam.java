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
import com.sun.tck.lib.tgf.data.Classes;
import com.sun.tck.lib.tgf.data.Operation;
import com.sun.tck.lib.tgf.data.Strings;
import com.sun.tck.test.TestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ReferenceAttachedToParam {

    @org.junit.Test
    public void int_1() {
        final List<Integer> list = new ArrayList<Integer>();
        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new BaseTestGroup() {

            Values data = DataFactory.createColumn(45, 7, 3);

            @TestCase
            public void test(@TestData("data") int i) throws Throwable {
                list.add(i);
            }
        });
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 3, list.size());
        Assert.assertEquals(45, list.get(0), 0.0);
        Assert.assertEquals(7, list.get(1), 0.0);
        Assert.assertEquals(3, list.get(2), 0.0);
    }

    @org.junit.Test
    public void bool_1() {
        final List<Boolean> list = new ArrayList<Boolean>();
        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new BaseTestGroup() {
            Boolean[] data__ = {true, false, false, false};

            @TestCase
            public void test(@TestData("data__") boolean b) throws Throwable {
                list.add(b);
            }
        });

        Assert.assertTrue(status.isOK());
        Assert.assertEquals(4, list.size());
        Assert.assertEquals(true, list.get(0));
        Assert.assertEquals(false, list.get(1));
        Assert.assertEquals(false, list.get(2));
        Assert.assertEquals(false, list.get(3));
    }


    @org.junit.Test
    public void class_1() {
        final List<Class<?>> list = new ArrayList<Class<?>>();
        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new BaseTestGroup() {
            Class[] classes = {String.class, Throwable.class};

            @TestCase
            public void test(@TestData("classes") Class<?> c) throws Throwable {
                list.add(c);
            }
        });
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 2, list.size());
        Assert.assertEquals(String.class, list.get(0));
        Assert.assertEquals(Throwable.class, list.get(1));
    }

    @org.junit.Test
    public void shorts_1() {
        final List<Integer> list = new ArrayList<Integer>();
        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new BaseTestGroup() {
            Values ints = DataFactory.createColumn(23, 423);

            @TestCase
            public void test(@TestData("ints") int i) throws Throwable {
                list.add(i);
            }
        });
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 2, list.size());
        Assert.assertEquals(23, list.get(0), 0.0);
        Assert.assertEquals(423, list.get(1), 0.0);
    }

    @org.junit.Test
    public void mixed_default() {
        final List<Class<?>> list_1 = new ArrayList<Class<?>>();
        final List<String> list_2 = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new BaseTestGroup() {
            Values strings_ = DataFactory.createColumn("a", "b", "c");
            Class[] classes = {String.class, Throwable.class};

            @TestCase
            public void test(@TestData("classes") Class<?> c, @TestData("strings_") String s) throws Throwable {
                list_1.add(c);
                list_2.add(s);
            }
        });
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 3, list_1.size());
        Assert.assertEquals( 3, list_2.size());
        Assert.assertEquals(String.class, list_1.get(0));
        Assert.assertEquals(Throwable.class, list_1.get(1));
        Assert.assertEquals(String.class, list_1.get(2));

        Assert.assertEquals("a", list_2.get(0));
        Assert.assertEquals("b", list_2.get(1));
        Assert.assertEquals("c", list_2.get(2));
    }

    @org.junit.Test
    public void mixed_pseudomult() {
        final List<Class<?>> list_1 = new ArrayList<Class<?>>();
        final List<String> list_2 = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new BaseTestGroup() {
            Values strings_ = DataFactory.createColumn("a", "b", "c");
            Class[] classes = {String.class, Throwable.class};

            @TestCase
            @Operation(Operation.TYPE.PSEUDOMULTYPLY)
            public void test(@TestData("classes") Class<?> c,
                             @TestData("strings_") String s) throws Throwable {
                list_1.add(c);
                list_2.add(s);
            }
        });
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 3, list_1.size());
        Assert.assertEquals( 3, list_2.size());
        Assert.assertEquals(String.class, list_1.get(0));
        Assert.assertEquals(Throwable.class, list_1.get(1));
        Assert.assertEquals(String.class, list_1.get(2));

        Assert.assertEquals("a", list_2.get(0));
        Assert.assertEquals("b", list_2.get(1));
        Assert.assertEquals("c", list_2.get(2));
    }

    @org.junit.Test
    public void mixed_mult_1() {
        final List<Class<?>> list_1 = new ArrayList<Class<?>>();
        final List<String> list_2 = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new BaseTestGroup() {
            Values strings_ = DataFactory.createColumn("a", "b", "c");
            Class[] classes = {String.class, Throwable.class};

            @TestCase
            @Operation(Operation.TYPE.MULTIPLY)
            public void test(@TestData("classes") Class<?> c,
                             @TestData("strings_") String s) throws Throwable {
                list_1.add(c);
                list_2.add(s);
            }
        });
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 6, list_1.size());
        Assert.assertEquals( 6, list_2.size());
        Assert.assertEquals(String.class, list_1.get(0));
        Assert.assertEquals(String.class, list_1.get(1));
        Assert.assertEquals(String.class, list_1.get(2));
        Assert.assertEquals(Throwable.class, list_1.get(3));
        Assert.assertEquals(Throwable.class, list_1.get(4));
        Assert.assertEquals(Throwable.class, list_1.get(5));

        Assert.assertEquals("a", list_2.get(0));
        Assert.assertEquals("b", list_2.get(1));
        Assert.assertEquals("c", list_2.get(2));
        Assert.assertEquals("a", list_2.get(3));
        Assert.assertEquals("b", list_2.get(4));
        Assert.assertEquals("c", list_2.get(5));
    }

    @org.junit.Test
    public void mixed_mult_2() {
        final List<Class<?>> list_1 = new ArrayList<Class<?>>();
        final List<String> list_2 = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new BaseTestGroup() {
            Values strings_ = DataFactory.createColumn("aa", "bb", "cc");

            @TestCase
            @Operation(Operation.TYPE.MULTIPLY)
            public void test(@Classes({String.class, Throwable.class}) Class<?> c,
                             @TestData("strings_") String s) throws Throwable {
                list_1.add(c);
                list_2.add(s);
            }
        });
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 6, list_1.size());
        Assert.assertEquals( 6, list_2.size());
        Assert.assertEquals(String.class, list_1.get(0));
        Assert.assertEquals(String.class, list_1.get(1));
        Assert.assertEquals(String.class, list_1.get(2));
        Assert.assertEquals(Throwable.class, list_1.get(3));
        Assert.assertEquals(Throwable.class, list_1.get(4));
        Assert.assertEquals(Throwable.class, list_1.get(5));

        Assert.assertEquals("aa", list_2.get(0));
        Assert.assertEquals("bb", list_2.get(1));
        Assert.assertEquals("cc", list_2.get(2));
        Assert.assertEquals("aa", list_2.get(3));
        Assert.assertEquals("bb", list_2.get(4));
        Assert.assertEquals("cc", list_2.get(5));
    }

    @org.junit.Test
    public void mixed_mult_3() {
        final List<Class<?>> list_1 = new ArrayList<Class<?>>();
        final List<String> list_2 = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new BaseTestGroup() {
            private Values classes_ = DataFactory.createColumn(String.class, Throwable.class);

            @TestCase
            @Operation(Operation.TYPE.MULTIPLY)
            public void test(@TestData("classes_") Class<?> c,
                             @Strings({"x", "y", "z"}) String s) throws Throwable {
                list_1.add(c);
                list_2.add(s);
            }
        });
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 6, list_1.size());
        Assert.assertEquals( 6, list_2.size());
        Assert.assertEquals(String.class, list_1.get(0));
        Assert.assertEquals(String.class, list_1.get(1));
        Assert.assertEquals(String.class, list_1.get(2));
        Assert.assertEquals(Throwable.class, list_1.get(3));
        Assert.assertEquals(Throwable.class, list_1.get(4));
        Assert.assertEquals(Throwable.class, list_1.get(5));

        Assert.assertEquals("x", list_2.get(0));
        Assert.assertEquals("y", list_2.get(1));
        Assert.assertEquals("z", list_2.get(2));
        Assert.assertEquals("x", list_2.get(3));
        Assert.assertEquals("y", list_2.get(4));
        Assert.assertEquals("z", list_2.get(5));
    }



}

