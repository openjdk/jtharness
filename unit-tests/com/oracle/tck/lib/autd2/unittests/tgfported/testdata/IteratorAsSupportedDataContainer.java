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
import com.sun.tck.lib.tgf.TestData;
import com.sun.tck.lib.tgf.data.Operation;
import com.sun.tck.test.TestCase;
import com.sun.tck.test.TestGroup;
import org.junit.Assert;
import org.junit.Test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

import static com.sun.tck.lib.Assert.assertEquals;
import static com.sun.tck.lib.tgf.data.Operation.TYPE.MULTIPLY;

public class IteratorAsSupportedDataContainer {


    @org.junit.Test
    public void iteratorAsReturnType_ints() {
        final List<Integer> is = new ArrayList<Integer>();
        final List<Integer> js = new ArrayList<Integer>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            Iterator<Object[]> intIterator = Arrays.<Object[]>asList(
                    new Integer[]{89898, 176253},
                    new Integer[]{34324, 343424}
            ).iterator();

            @TestCase
            @TestData("intIterator")
            public void test(int i, int j) throws Throwable {
                is.add(i);
                js.add(j);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(2, is.size());
        Assert.assertEquals(2, js.size());
        Assert.assertEquals(89898, (int) is.get(0));
        Assert.assertEquals(176253, (int) js.get(0));
        Assert.assertEquals(34324, (int) is.get(1));
        Assert.assertEquals(343424, (int) js.get(1));
    }

    @org.junit.Test
    public void iteratorAsReturnType_strings() {
        final List<String> is = new ArrayList<String>();
        final List<String> js = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            Iterator<Object[]> stringITerator = Arrays.<Object[]>asList(
                    new String[]{"a", "b"},
                    new String[]{"v", "n"}
            ).iterator();

            @TestCase
            @TestData("stringITerator")
            public void test(String i, String j) throws Throwable {
                is.add(i);
                js.add(j);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(2, is.size());
        Assert.assertEquals(2, js.size());
        Assert.assertEquals("a", is.get(0));
        Assert.assertEquals("b", js.get(0));
        Assert.assertEquals("v", is.get(1));
        Assert.assertEquals("n", js.get(1));
    }

    @org.junit.Test
    public void iteratorAsReturnType_mix() {
        final List<Integer> is = new ArrayList<Integer>();
        final List<Integer> js = new ArrayList<Integer>();
        final List<String> strings = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            Iterator<Object[]> mixIterator = Arrays.<Object[]>asList(
                    new Object[]{89898, 176253, "123"},
                    new Object[]{34324, 343424, "345"}
            ).iterator();

            @TestCase
            @TestData("mixIterator")
            public void test(int i, int j, String s) throws Throwable {
                is.add(i);
                js.add(j);
                strings.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(2, is.size());
        Assert.assertEquals(2, js.size());
        Assert.assertEquals(2, strings.size());
        Assert.assertEquals(89898, (int) is.get(0));
        Assert.assertEquals(176253, (int) js.get(0));
        Assert.assertEquals("123", strings.get(0));
        Assert.assertEquals(34324, (int) is.get(1));
        Assert.assertEquals(343424, (int) js.get(1));
        Assert.assertEquals("345", strings.get(1));
    }

    @org.junit.Test
    public void iteratorAsReturnType_mix_severalIterators() {
        final List<Integer> is = new ArrayList<Integer>();
        final List<Integer> js = new ArrayList<Integer>();
        final List<String> strings = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            Iterator<Object[]> mixIterator = Arrays.<Object[]>asList(
                    new Object[]{89898, 176253, "123"},
                    new Object[]{34324, 343424, "345"}
            ).iterator();

            Iterator<Integer> iter_1 = Arrays.asList(89898, 34324).iterator();
            Iterator<Integer> iter_2 = Arrays.asList(176253, 343424).iterator();
            Iterator<String> iter_3 = Arrays.asList("123", "345").iterator();

            @TestCase
            public void test(@TestData("iter_1") int i, @TestData("iter_2") int j, @TestData("iter_3") String s) throws Throwable {
                is.add(i);
                js.add(j);
                strings.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(2, is.size());
        Assert.assertEquals(2, js.size());
        Assert.assertEquals(2, strings.size());
        Assert.assertEquals(89898, (int) is.get(0));
        Assert.assertEquals(176253, (int) js.get(0));
        Assert.assertEquals("123", strings.get(0));
        Assert.assertEquals(34324, (int) is.get(1));
        Assert.assertEquals(343424, (int) js.get(1));
        Assert.assertEquals("345", strings.get(1));
    }

    @org.junit.Test
    public void iteratorAsReturnType_ints_2() {
        final List<Integer> is = new ArrayList<Integer>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            Iterator<Integer[]> intIterator = Arrays.<Integer[]>asList(
                    new Integer[]{89898},
                    new Integer[]{34324},
                    new Integer[]{34}
            ).iterator();

            @TestCase
            @TestData("intIterator")
            public void test(int i) throws Throwable {
                is.add(i);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(3, is.size());

        Assert.assertEquals(89898, (int) is.get(0));
        Assert.assertEquals(34324, (int) is.get(1));
        Assert.assertEquals(34, (int) is.get(2));
    }

    @org.junit.Test
    public void iteratorAsReturnType_String_2() {

        final List<String> passed = new ArrayList<>();

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            Iterator<String> strings = Arrays.asList("a", "bb", "cc").iterator();

            @TestCase
            public void test(@TestData("strings") String s) throws Throwable {
                passed.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(3, passed.size());
        Assert.assertEquals(passed.get(0), "a");
        Assert.assertEquals(passed.get(1), "bb");
        Assert.assertEquals(passed.get(2), "cc");
    }


    @Test
    public void test_04_IteratorOfIntegers() {
        @TestGroup
        class Test {

            Iterator<Integer> component() {
                return IntStream.range(55, 67).iterator();
            }

            int counter;

            @TestCase
            @Operation(MULTIPLY)
            public void test(@TestData("component") int r,
                             @TestData("component") int g,
                             @TestData("component") int b) {
                Color color = new Color(r, g, b);
                assertEquals(r, color.getRed());
                assertEquals(g, color.getGreen());
                assertEquals(b, color.getBlue());
                counter++;
            }
        }
        Test tg = new Test();
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(tg);
        Assert.assertTrue(s.isOK());
        Assert.assertEquals(1728, tg.counter);
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }

    @Test
    public void test_04_IteratorOfIntegers_2() {
        @TestGroup
        class Test {

            Iterator<Integer> component = IntStream.range(55, 98).iterator();
            int counter;

            @TestCase
            public void test(@TestData("component") int r) {
                counter++;
            }
        }
        Test tg = new Test();
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(tg);
        Assert.assertTrue(s.isOK());
        Assert.assertEquals(43, tg.counter);
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }

    @Test
    public void test_04_IteratorOfIntegers_01() {
        @TestGroup
        class Test {

            Iterator<Integer> component = Arrays.asList(1, 2, 3, 4, 5).iterator();
            int counter = 0;

            @TestCase
            public void test(@TestData("component") int x) {
                counter++;
            }
        }
        Test tg = new Test();
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(tg);
        Assert.assertTrue(s.isOK());
        Assert.assertEquals(5, tg.counter);
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }

    @Test
    public void test_04_IteratorOfObjectArrays_01() {
        @TestGroup
        class Test {

            Iterator<Object[]> component = Arrays.asList(
                    new Object[]{1, "1"},
                    new Object[]{2, "3"},
                    new Object[]{6, "4"},
                    new Object[]{5, "5"}
            ).iterator();
            int counter = 0;

            @TestCase
            @TestData("component")
            public void test(int x, String s) {
                counter++;
            }
        }
        Test tg = new Test();
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(tg);
        Assert.assertTrue(s.isOK());
        Assert.assertEquals(4, tg.counter);
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }

}