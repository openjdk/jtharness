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
package com.oracle.tck.lib.autd2.unittests.tgfported;

/**
 *
 */
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.lang.reflect.Array;

import com.oracle.tck.lib.autd2.unittests.ValuesComparison;
import com.sun.tck.lib.tgf.DataFactory;
import com.sun.tck.lib.tgf.TestData;
import com.sun.tck.lib.tgf.TestDataCollector;
import com.sun.tck.lib.tgf.Values;
import com.sun.tck.lib.tgf.data.*;
import org.junit.Test;
import org.junit.Assert;

import javax.swing.*;

public class TestInlinedDataCollector {

    @Test
    public void int_1() {
        class MyClass {
            void test(@Ints({1,2,3,4}) int i) {}
        }
        Values values = TestDataCollector.collectInlinedData(getTestMethod(MyClass.class), null, null, null);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1});
        expected.add(new Object[]{2});
        expected.add(new Object[]{3});
        expected.add(new Object[]{4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void int_2() {
        class MyClass {
            void test(@Ints({1,2,3,4}) int i,
                      @Ints({67, 88, 99}) int j) {}
        }
        Values values = TestDataCollector.collectInlinedData(getTestMethod(MyClass.class), null, null, null);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 67});
        expected.add(new Object[]{2, 88});
        expected.add(new Object[]{3, 99});
        expected.add(new Object[]{4, 67});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void int_3() {
        class MyClass {
            void test(@Ints({112}) int i,
                      @Ints({673}) int j) {}
        }
        Values values = TestDataCollector.collectInlinedData(getTestMethod(MyClass.class), null, null, null);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{112, 673});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void boolean_1() {
        class MyClass {
            void test(@Booleans({true, true}) boolean b1,
                      @Booleans({false, false}) boolean b2) {}
        }
        Values values = TestDataCollector.collectInlinedData(getTestMethod(MyClass.class), null, null, null);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{true, false});
        expected.add(new Object[]{true, false});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void boolean_int_1() {
        class MyClass {
            void test(@Booleans({false, true}) boolean b1,
                      @Booleans({true, false}) boolean b2,
                      @Ints({1,2,3,4,5}) int i) {}
        }
        Values values = TestDataCollector.collectInlinedData(getTestMethod(MyClass.class), null, null, null);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{false, true, 1});
        expected.add(new Object[]{true, false, 2});
        expected.add(new Object[]{false, true, 3});
        expected.add(new Object[]{true, false, 4});
        expected.add(new Object[]{false, true, 5});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void bytes_1() {
        class MyClass {
            void test(@Bytes({1, 3, 90}) byte b1,
                      @Bytes({4, 5, 70}) byte b2) {}
        }
        Values values = TestDataCollector.collectInlinedData(getTestMethod(MyClass.class), null, null, null);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{(byte)1, (byte)4});
        expected.add(new Object[]{(byte)3, (byte)5});
        expected.add(new Object[]{(byte)90, (byte)70});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void chars_1() {
        class MyClass {
            void test(@Chars({11, 3, 90}) char b1,
                      @Chars({41, 5, 70}) char b2) {}
        }
        Values values = TestDataCollector.collectInlinedData(getTestMethod(MyClass.class), null, null, null);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{(char)11, (char)41});
        expected.add(new Object[]{(char)3,  (char)5});
        expected.add(new Object[]{(char)90, (char)70});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void classes_1() {
        class MyClass {
            void test(@Classes({NullPointerException.class, Number.class, Array.class}) Class c1,
                      @Classes({Integer.class, Double.class, JPanel.class}) Class c2) {}
        }
        Values values = TestDataCollector.collectInlinedData(getTestMethod(MyClass.class), null, null, null);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{NullPointerException.class, Integer.class});
        expected.add(new Object[]{Number.class,  Double.class});
        expected.add(new Object[]{Array.class, JPanel.class});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void doubles_1() {
        class MyClass {
            void test(@Doubles({1d, 67.5d}) double b1,
                      @Doubles({7d, 90d}) double b2) {}
        }
        Values values = TestDataCollector.collectInlinedData(getTestMethod(MyClass.class), null, null, null);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{(double)1, (double)7});
        expected.add(new Object[]{(double)67.5,  (double)90});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void floates_1() {
        class MyClass {
            void test(@Floats({1f, 67.5f}) float b1,
                      @Floats({73f, 90f}) float b2) {}
        }
        Values values = TestDataCollector.collectInlinedData(getTestMethod(MyClass.class), null, null, null);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1f, 73f});
        expected.add(new Object[]{67.5f, 90f});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void longs_1() {
        class MyClass {
            void test(@Longs({1l, 67l}) long b1,
                      @Longs({72l, 90l}) long b2) {}
        }
        Values values = TestDataCollector.collectInlinedData(getTestMethod(MyClass.class), null, null, null);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1l, 72l});
        expected.add(new Object[]{67l, 90l});
        ValuesComparison.compare(values, expected);
    }


    @Test
    public void shorts_1() {
        class MyClass {
            void test(@Shorts({23, 234}) short s1,
                      @Shorts({45, 89}) short s2) {}
        }
        Values values = TestDataCollector.collectInlinedData(getTestMethod(MyClass.class), null, null, null);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{(short)23, (short)45});
        expected.add(new Object[]{(short)234, (short)89});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void shorts_2() {
        class MyClass {
            @Operation(Operation.TYPE.PSEUDOMULTYPLY)
            void test(@Shorts({23, 234}) short s1,
                      @Shorts({45, 89}) short s2) {}
        }
        Values values = TestDataCollector.collectInlinedData(getTestMethod(MyClass.class), null, null, null);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{(short)23, (short)45});
        expected.add(new Object[]{(short)234, (short)89});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void strings_1() {
        class MyClass {
            void test(@Strings({"a", "b"}) String s1,
                      @Strings({"c", "d"}) String s2) {}
        }
        Values values = TestDataCollector.collectInlinedData(getTestMethod(MyClass.class), null, null, null);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", "c"});
        expected.add(new Object[]{"b", "d"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void strings_2() {
        class MyClass {
            @Operation(Operation.TYPE.PSEUDOMULTYPLY)
            void test(@Strings({"a", "b"}) String s1,
                      @Strings({"c", "d"}) String s2) {}
        }
        Values values = TestDataCollector.collectInlinedData(getTestMethod(MyClass.class), null, null, null);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", "c"});
        expected.add(new Object[]{"b", "d"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void mixed_1_defaultoperation() {
        class MyClass {
            void test(@Booleans({true,        false}) boolean b1,
                      @Bytes({1,              2}) byte b2,
                      @Chars({'a',            'v', 't'}) char c,
                      @Classes({String.class, Number.class}) Class ccc,
                      @Doubles({1d,           4d}) double d,
                      @Floats ({4f,           7f, 8f}) float f,
                      @Ints({89,              90}) int i,
                      @Longs({89l,            78l}) long l,
                      @Shorts({23,            45, 67}) short sss,
                      @Strings({"45",         "abc", "cdd"}) String s) {}
        }
        Values values = TestDataCollector.collectInlinedData(getTestMethod(MyClass.class), null, null, null);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {true,  (byte)1, 'a', String.class, (double)1, (float)4, (int)89, (long)89, (short)23, "45"});
        expected.add(new Object[] {false, (byte)2, 'v', Number.class, (double)4, (float)7, (int)90, (long)78, (short)45, "abc"});
        expected.add(new Object[] {true,  (byte)1, 't', String.class, (double)1, (float)8, (int)89, (long)89l, (short)67, "cdd"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void mixed_pseudomultiply_specified() {
        class MyClass {
            @Operation(Operation.TYPE.PSEUDOMULTYPLY)
            void test(@Booleans({true,        false}) boolean b1,
                      @Bytes({1,              2}) byte b2,
                      @Chars({'a',            'v', 't'}) char c,
                      @Classes({String.class, Number.class}) Class ccc,
                      @Doubles({1d,           4d}) double d,
                      @Floats ({4f,           7f, 8f}) float f,
                      @Ints({89,              90}) int i,
                      @Longs({89l,            78l}) long l,
                      @Shorts({23,            45, 67}) short sss,
                      @Strings({"45",         "abc", "cdd"}) String s) {}
        }
        Values values = TestDataCollector.collectInlinedData(getTestMethod(MyClass.class), null, null, null);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {true,  (byte)1, 'a', String.class, (double)1, (float)4, (int)89, (long)89, (short)23, "45"});
        expected.add(new Object[] {false, (byte)2, 'v', Number.class, (double)4, (float)7, (int)90, (long)78, (short)45, "abc"});
        expected.add(new Object[] {true,  (byte)1, 't', String.class, (double)1, (float)8, (int)89, (long)89l, (short)67, "cdd"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void strings_2_multiply() {
        class MyClass {
            @Operation(Operation.TYPE.MULTIPLY)
            void test(@Strings({"a", "b"}) String s1,
                      @Strings({"c", "d"}) String s2) {}
        }
        Values values = TestDataCollector.collectInlinedData(getTestMethod(MyClass.class), null, null, null);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", "c"});
        expected.add(new Object[]{"a", "d"});
        expected.add(new Object[]{"b", "c"});
        expected.add(new Object[]{"b", "d"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void strings_3_multiply() {
        class MyClass {
            @Operation(Operation.TYPE.MULTIPLY)
            void test(@Strings({"a", "b"}) String s1,
                      @Strings({"c", "d"}) String s2,
                      @Strings({"e", "f"}) String s3 ) {}
        }
        Values values = TestDataCollector.collectInlinedData(getTestMethod(MyClass.class), null, null, null);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", "c", "e"});
        expected.add(new Object[]{"a", "c", "f"});
        expected.add(new Object[]{"a", "d", "e"});
        expected.add(new Object[]{"a", "d", "f"});
        expected.add(new Object[]{"b", "c", "e"});
        expected.add(new Object[]{"b", "c", "f"});
        expected.add(new Object[]{"b", "d", "e"});
        expected.add(new Object[]{"b", "d", "f"});
        ValuesComparison.compare(values, expected);
    }


    static class MyClass4 {
        String[] myStrings = {"a", "b"};
        @Operation(Operation.TYPE.MULTIPLY)
        void test(@TestData("myStrings") String s1,
                  @Strings({"c", "d"}) String s2,
                  @Strings({"e", "f"}) String s3 ) {}
    }
    @Test
    public void strings_4_multiply() {
        Values values = TestDataCollector.getData(new MyClass4(), MyClass4.class, null, getTestMethod(MyClass4.class));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", "c", "e"});
        expected.add(new Object[]{"a", "c", "f"});
        expected.add(new Object[]{"a", "d", "e"});
        expected.add(new Object[]{"a", "d", "f"});
        expected.add(new Object[]{"b", "c", "e"});
        expected.add(new Object[]{"b", "c", "f"});
        expected.add(new Object[]{"b", "d", "e"});
        expected.add(new Object[]{"b", "d", "f"});
        ValuesComparison.compare(values, expected);
    }

    static class MyClass5 {
        String[] myStringsMethod() { return new String[]{"a", "b"};  }
        Values myOtherStrings = DataFactory.createColumn("c", "d");
        @Operation(Operation.TYPE.MULTIPLY)
        void test(@TestData("myStringsMethod") String s1,
                  @TestData("myOtherStrings") String s2,
                  @Strings({"e", "f"}) String s3 ) {}
    }
    @Test
    public void strings_5_multiply() {
        Values values = TestDataCollector.getData(new MyClass5(), MyClass5.class, null, getTestMethod(MyClass5.class));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", "c", "e"});
        expected.add(new Object[]{"a", "c", "f"});
        expected.add(new Object[]{"a", "d", "e"});
        expected.add(new Object[]{"a", "d", "f"});
        expected.add(new Object[]{"b", "c", "e"});
        expected.add(new Object[]{"b", "c", "f"});
        expected.add(new Object[]{"b", "d", "e"});
        expected.add(new Object[]{"b", "d", "f"});
        ValuesComparison.compare(values, expected);
    }

    static class MyClass6 {
        String[] myStrings = {"aa", "bb"};
        @Operation(Operation.TYPE.MULTIPLY)
        void test(@TestData("myStrings") String s1,
                  @TestData("myStrings") String s2) {}
    }
    @Test
    public void strings_6_multiply() {
        Values values = TestDataCollector.getData(new MyClass6(), MyClass6.class, null, getTestMethod(MyClass6.class));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"aa", "aa"});
        expected.add(new Object[]{"aa", "bb"});
        expected.add(new Object[]{"bb", "aa"});
        expected.add(new Object[]{"bb", "bb"});
        ValuesComparison.compare(values, expected);
    }


    @Test
    public void doubles_2_multiply() {
        class MyClass {
            @Operation(Operation.TYPE.MULTIPLY)
            void test(@Doubles({1d, 2.5d}) double b1,
                      @Doubles({3d, 8d}) double b2) {}
        }
        Values values = TestDataCollector.collectInlinedData(getTestMethod(MyClass.class), null, null, null);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{(double)1, (double)3});
        expected.add(new Object[]{(double)1, (double)8});
        expected.add(new Object[]{(double)2.5, (double)3});
        expected.add(new Object[]{(double)2.5, (double)8});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_1() {
        class MyClass {
            void test(@Strings({"fee", "fie", "foe", "foo"}) String s,
                      @Chars  ({  'f',   'i',   'e',   'e'}) char c,
                      @Ints   ({    0,     1,     2,    -1}) int expectedIndexOf) {
                Assert.assertEquals(expectedIndexOf, s.indexOf(c) );
            }
        }
        Values values = TestDataCollector.collectInlinedData(getTestMethod(MyClass.class), null, null, null);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"fee", 'f', 0});
        expected.add(new Object[]{"fie", 'i', 1});
        expected.add(new Object[]{"foe", 'e', 2});
        expected.add(new Object[]{"foo", 'e', -1});
        ValuesComparison.compare(values, expected);
    }

    private Method getTestMethod(Class<? extends Object> aClass) {
        Method[] methods = aClass.getDeclaredMethods();
        for (Method method : methods) {
            if ("test".equals(method.getName())) {
                return method;
            }
        }
        return null;
    }


}

