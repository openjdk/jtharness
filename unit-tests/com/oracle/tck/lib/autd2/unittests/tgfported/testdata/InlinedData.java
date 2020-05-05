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
import com.sun.tck.lib.tgf.data.*;
import com.sun.tck.test.TestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class InlinedData {



    @org.junit.Test
    public void int_1() {
        final List<Integer> list = new ArrayList<Integer>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Ints({45, 7, 3}) int i) throws Throwable {
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
    public void int_2() {
        final List<Integer> list = new ArrayList<Integer>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Ints(867) int d) throws Throwable {
                list.add(867);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 1, list.size());
        Assert.assertEquals(867, list.get(0), 0.0);
    }


    @org.junit.Test
    public void bool_1() {

        final List<Boolean> list = new ArrayList<Boolean>();

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            @TestCase
            public void test(@Booleans({true, false, false, false}) boolean b) throws Throwable {
                list.add(b);
            }
        }, TU.EMPTY_ARGV);

        Assert.assertTrue(status.isOK());
        Assert.assertEquals(4, list.size());
        Assert.assertEquals(true, list.get(0));
        Assert.assertEquals(false, list.get(1));
        Assert.assertEquals(false, list.get(2));
        Assert.assertEquals(false, list.get(3));
    }

    @org.junit.Test
    public void bool_2() {
        final List<Boolean> list = new ArrayList<Boolean>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Booleans(true) boolean b) throws Throwable {
                list.add(b);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(true, list.get(0));
    }


    @org.junit.Test
    public void booleans_default() {
        final List<Boolean> list = new ArrayList<Boolean>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Booleans boolean b) throws Throwable {
                list.add(b);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(false, list.get(0));
        Assert.assertEquals(true, list.get(1));
    }



    @org.junit.Test
    public void double_1() {
        final List<Double> list = new ArrayList<Double>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Doubles({45.56, 7.7, 1.1}) Double s) throws Throwable {
                list.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 3, list.size());
        Assert.assertEquals(45.56, list.get(0), 0.0);
        Assert.assertEquals(7.7, list.get(1), 0.0);
        Assert.assertEquals(1.1, list.get(2), 0.0);
    }


    @org.junit.Test
    public void double_2() {
        final List<Double> list = new ArrayList<Double>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Doubles(8.5) double d) throws Throwable {
                list.add(d);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 1, list.size());
        Assert.assertEquals(8.5, list.get(0), 0.0);
    }


    @org.junit.Test
    public void strings_1() {
        final List<String> list = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Strings({"a", "b", "c"}) String s) throws Throwable {
                list.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 3, list.size());
        Assert.assertEquals("a", list.get(0));
        Assert.assertEquals("b", list.get(1));
        Assert.assertEquals("c", list.get(2));
    }

    @org.junit.Test
    public void strings_1_1() {
        final List<String> list = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Strings({"a", "b", "c"}) String s) throws Throwable {
                list.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 3, list.size());
        Assert.assertEquals("a", list.get(0));
        Assert.assertEquals("b", list.get(1));
        Assert.assertEquals("c", list.get(2));
    }

    @org.junit.Test
    public void strings_1_2() {
        final List<String> list = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Strings({"a", "b", "c"}) String s) throws Throwable {
                list.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 3, list.size());
        Assert.assertEquals("a", list.get(0));
        Assert.assertEquals("b", list.get(1));
        Assert.assertEquals("c", list.get(2));
    }

    @org.junit.Test
    public void strings_2() {
        final List<String> list = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Strings("34563456435634645631324") String s) throws Throwable {
                list.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 1, list.size());
        Assert.assertEquals("34563456435634645631324", list.get(0));
    }

    @org.junit.Test
    public void floats_1() {
        final List<Float> list = new ArrayList<Float>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Floats({12.4f, 7.8f, 0.5f}) float f) throws Throwable {
                list.add(f);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 3, list.size());
        Assert.assertEquals(12.4f, list.get(0), 0.0);
        Assert.assertEquals(7.8f, list.get(1), 0.0);
        Assert.assertEquals(0.5f, list.get(2), 0.0);
    }

    @org.junit.Test
    public void floats_2() {
        final List<Float> list = new ArrayList<Float>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Floats(67.4f) float f) throws Throwable {
                list.add(f);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 1, list.size());
        Assert.assertEquals(67.4f, list.get(0), 0.0);
    }

    @org.junit.Test
    public void longs_1() {
        final List<Long> list = new ArrayList<Long>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Longs({234234, 23423}) long f) throws Throwable {
                list.add(f);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 2, list.size());
        Assert.assertEquals(234234, list.get(0), 0.0);
        Assert.assertEquals(23423, list.get(1), 0.0);
    }

    @org.junit.Test
    public void longs_2() {
        final List<Long> list = new ArrayList<Long>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Longs(5544555) long l) throws Throwable {
                list.add(l);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 1, list.size());
        Assert.assertEquals(5544555, list.get(0), 0.0);
    }

    @org.junit.Test
    public void shorts_1() {
        final List<Short> list = new ArrayList<Short>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Shorts({23, 423}) short s) throws Throwable {
                list.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 2, list.size());
        Assert.assertEquals(23, list.get(0), 0.0);
        Assert.assertEquals(423, list.get(1), 0.0);
    }

    @org.junit.Test
    public void shorts_2() {
        final List<Short> list = new ArrayList<Short>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Shorts(1423) short s) throws Throwable {
                list.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 1, list.size());
        Assert.assertEquals(1423, list.get(0), 0.0);
    }


    @org.junit.Test
    public void chars_1() {
        final List<Character> list = new ArrayList<Character>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Chars({28, 1}) char c) throws Throwable {
                list.add(c);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 2, list.size());
        Assert.assertEquals(28, list.get(0), 0.0);
        Assert.assertEquals(1, list.get(1), 0.0);
    }

    @org.junit.Test
    public void chars_2() {
        final List<Character> list = new ArrayList<Character>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Chars(458) char c) throws Throwable {
                list.add(c);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 1, list.size());
        Assert.assertEquals(458, list.get(0), 0.0);
    }

    @org.junit.Test
    public void bytes_1() {
        final List<Byte> list = new ArrayList<Byte>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Bytes({48, 2}) byte b) throws Throwable {
                list.add(b);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 2, list.size());
        Assert.assertEquals(48, list.get(0), 0.0);
        Assert.assertEquals(2, list.get(1), 0.0);
    }

    @org.junit.Test
    public void bytes_1_1() {
        final List<Byte> list = new ArrayList<Byte>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Bytes({48, 2}) byte b) throws Throwable {
                list.add(b);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 2, list.size());
        Assert.assertEquals(48, list.get(0), 0.0);
        Assert.assertEquals(2, list.get(1), 0.0);
    }

    @org.junit.Test
    public void bytes_1_2() {
        final List<Byte> list = new ArrayList<Byte>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Bytes({48, 2}) byte b) throws Throwable {
                list.add(b);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 2, list.size());
        Assert.assertEquals(48, list.get(0), 0.0);
        Assert.assertEquals(2, list.get(1), 0.0);
    }

    @org.junit.Test
    public void bytes_2() {
        final List<Byte> list = new ArrayList<Byte>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Bytes(56) byte b) throws Throwable {
                list.add(b);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 1, list.size());
        Assert.assertEquals(56, list.get(0), 0.0);
    }

    @org.junit.Test
    public void class_1() {
        final List<Class<?>> list = new ArrayList<Class<?>>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Classes({String.class, Throwable.class}) Class<?> c) throws Throwable {
                list.add(c);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 2, list.size());
        Assert.assertEquals(String.class, list.get(0));
        Assert.assertEquals(Throwable.class, list.get(1));
    }

    @org.junit.Test
    public void class_2() {
        final List<Class<?>> list = new ArrayList<Class<?>>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            @TestCase
            public void test(@Classes(Number.class) Class<?> c) throws Throwable {
                list.add(c);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals( 1, list.size());
        Assert.assertEquals(Number.class, list.get(0));
    }


}
