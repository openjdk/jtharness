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
import com.sun.tck.test.TestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class ArraysOfPrimitives {

    @org.junit.Test
    public void arrayAsReturnType_ints_special_case() {
        final List<Integer> is = new ArrayList<Integer>();
        final List<Integer> js = new ArrayList<Integer>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            int[][] ints = {{89898, 176253}};

            @TestCase
            @TestData("ints")
            public void test(int i, int j) throws Throwable {
                is.add(i);
                js.add(j);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(1, is.size());
        Assert.assertEquals(1, js.size());
        Assert.assertEquals(89898, (int)is.get(0));
        Assert.assertEquals(176253, (int)js.get(0));
    }

    @org.junit.Test
    public void arrayAsReturnType_ints_special_case_1() {
        final List<Integer> is = new ArrayList<Integer>();
        final List<Integer> js = new ArrayList<Integer>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            int[][] ints = {{89898, 176253}, {34324, 343424}};

            @TestCase
            @TestData("ints")
            public void test(int i, int j) throws Throwable {
                is.add(i);
                js.add(j);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(2, is.size());
        Assert.assertEquals(2, js.size());
        Assert.assertEquals(89898, (int)is.get(0));
        Assert.assertEquals(176253, (int)js.get(0));
        Assert.assertEquals(34324, (int)is.get(1));
        Assert.assertEquals(343424, (int)js.get(1));
    }

    @org.junit.Test
    public void arrayAsReturnType_bools_special_case() {
        final List<Boolean> is = new ArrayList<Boolean>();
        final List<Boolean> js = new ArrayList<Boolean>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            boolean[][] bools = {{true, false}};

            @TestCase
            @TestData("bools")
            public void test(boolean i, boolean j) throws Throwable {
                is.add(i);
                js.add(j);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(1, is.size());
        Assert.assertEquals(1, js.size());
        Assert.assertEquals(true, (boolean)is.get(0));
        Assert.assertEquals(false, (boolean)js.get(0));
    }

    @org.junit.Test
    public void arrayAsReturnType_bools_special_case_1() {
        final List<Boolean> is = new ArrayList<Boolean>();
        final List<Boolean> js = new ArrayList<Boolean>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            boolean[][] bools = {{true, false}, {false, true}};

            @TestCase
            @TestData("bools")
            public void test(boolean i, boolean j) throws Throwable {
                is.add(i);
                js.add(j);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(2, is.size());
        Assert.assertEquals(2, js.size());
        Assert.assertEquals(true, (boolean)is.get(0));
        Assert.assertEquals(false, (boolean)js.get(0));
        Assert.assertEquals(false, (boolean)is.get(1));
        Assert.assertEquals(true, (boolean)js.get(1));
    }

    @org.junit.Test
    public void arrayAsReturnType_bytes_special_case() {
        final List<Byte> is = new ArrayList<Byte>();
        final List<Byte> js = new ArrayList<Byte>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            byte[][] bytes = {{3, 18}};

            @TestCase
            @TestData("bytes")
            public void test(byte i, byte j) throws Throwable {
                is.add(i);
                js.add(j);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(1, is.size());
        Assert.assertEquals(1, js.size());
        Assert.assertEquals(3, (byte)is.get(0));
        Assert.assertEquals(18, (byte)js.get(0));
    }

    @org.junit.Test
    public void arrayAsReturnType_bytes_special_case_1() {
        final List<Byte> is = new ArrayList<Byte>();
        final List<Byte> js = new ArrayList<Byte>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            byte[][] bytes = {{34, 12}, {89, 29}};

            @TestCase
            @TestData("bytes")
            public void test(byte i, byte j) throws Throwable {
                is.add(i);
                js.add(j);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(2, is.size());
        Assert.assertEquals(2, js.size());
        Assert.assertEquals(34, (byte)is.get(0));
        Assert.assertEquals(12, (byte)js.get(0));
        Assert.assertEquals(89, (byte)is.get(1));
        Assert.assertEquals(29, (byte)js.get(1));
    }

    @org.junit.Test
    public void arrayAsReturnType_chars_special_case() {
        final List<Character> is = new ArrayList<Character>();
        final List<Character> js = new ArrayList<Character>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            char[][] chars = {{3, 18}};

            @TestCase
            @TestData("chars")
            public void test(char i, char j) throws Throwable {
                is.add(i);
                js.add(j);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(1, is.size());
        Assert.assertEquals(1, js.size());
        Assert.assertEquals(3, (char)is.get(0));
        Assert.assertEquals(18, (char)js.get(0));
    }

    @org.junit.Test
    public void arrayAsReturnType_chars_special_case_1() {
        final List<Character> is = new ArrayList<Character>();
        final List<Character> js = new ArrayList<Character>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            char[][] chars = {{34, 12}, {89, 29}};

            @TestCase
            @TestData("chars")
            public void test(char i, char j) throws Throwable {
                is.add(i);
                js.add(j);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(2, is.size());
        Assert.assertEquals(2, js.size());
        Assert.assertEquals(34, (char)is.get(0));
        Assert.assertEquals(12, (char)js.get(0));
        Assert.assertEquals(89, (char)is.get(1));
        Assert.assertEquals(29, (char)js.get(1));
    }

    @org.junit.Test
    public void arrayAsReturnType_doubles_special_case() {
        final List<Double> is = new ArrayList<Double>();
        final List<Double> js = new ArrayList<Double>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            double[][] doubles = {{3.96786, 8678.18}};

            @TestCase
            @TestData("doubles")
            public void test(double i, double j) throws Throwable {
                is.add(i);
                js.add(j);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(1, is.size());
        Assert.assertEquals(1, js.size());
        Assert.assertEquals(3.96786, (double)is.get(0), 0.0001);
        Assert.assertEquals(8678.18, (double)js.get(0), 0.0001);
    }

    @org.junit.Test
    public void arrayAsReturnType_doubles_special_case_1() {
        final List<Double> is = new ArrayList<Double>();
        final List<Double> js = new ArrayList<Double>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            double[][] doubles = {{5387.986, 6878.18}, {587.6786, 868.1678}};

            @TestCase
            @TestData("doubles")
            public void test(double i, double j) throws Throwable {
                is.add(i);
                js.add(j);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(2, is.size());
        Assert.assertEquals(2, js.size());
        Assert.assertEquals(5387.986, (double)is.get(0), 0.0001);
        Assert.assertEquals(6878.18, (double)js.get(0), 0.0001);
        Assert.assertEquals(587.6786, (double)is.get(1), 0.0001);
        Assert.assertEquals(868.1678, (double)js.get(1), 0.0001);
    }

    @org.junit.Test
    public void arrayAsReturnType_floats_special_case() {
        final List<Float> is = new ArrayList<Float>();
        final List<Float> js = new ArrayList<Float>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            float[][] floats = {{3.65786f, 9078.0966f}};

            @TestCase
            @TestData("floats")
            public void test(float i, float j) throws Throwable {
                is.add(i);
                js.add(j);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(1, is.size());
        Assert.assertEquals(1, js.size());
        Assert.assertEquals(3.65786f, is.get(0), 0.0001);
        Assert.assertEquals(9078.0966f, js.get(0), 0.0001);
    }

    @org.junit.Test
    public void arrayAsReturnType_floats_special_case_1() {
        final List<Float> is = new ArrayList<Float>();
        final List<Float> js = new ArrayList<Float>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            float[][] floats = {{538767.96f, 878.1f}, {57.6f, 78.1678f}};

            @TestCase
            @TestData("floats")
            public void test(float i, float j) throws Throwable {
                is.add(i);
                js.add(j);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(2, is.size());
        Assert.assertEquals(2, js.size());
        Assert.assertEquals(538767.96f, is.get(0), 0.0001);
        Assert.assertEquals(878.1f, js.get(0), 0.0001);
        Assert.assertEquals(57.6f, is.get(1), 0.0001);
        Assert.assertEquals(78.1678f, js.get(1), 0.0001);
    }

    @org.junit.Test
    public void arrayAsReturnType_longs_special_case() {
        final List<Long> is = new ArrayList<Long>();
        final List<Long> js = new ArrayList<Long>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            long[][] longs = {{234234234l, 234023400034l}};

            @TestCase
            @TestData("longs")
            public void test(long i, long j) throws Throwable {
                is.add(i);
                js.add(j);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(1, is.size());
        Assert.assertEquals(1, js.size());
        Assert.assertEquals(234234234l, (long)is.get(0));
        Assert.assertEquals(234023400034l, (long)js.get(0));
    }

    @org.junit.Test
    public void arrayAsReturnType_longs_special_case_1() {
        final List<Long> is = new ArrayList<Long>();
        final List<Long> js = new ArrayList<Long>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            long[][] longs = {{234l, 234245435l}, {89000004l, 2345234573450l}};

            @TestCase
            @TestData("longs")
            public void test(long i, long j) throws Throwable {
                is.add(i);
                js.add(j);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(2, is.size());
        Assert.assertEquals(2, js.size());
        Assert.assertEquals(234l, (long)is.get(0));
        Assert.assertEquals(234245435l, (long)js.get(0));
        Assert.assertEquals(89000004l, (long)is.get(1));
        Assert.assertEquals(2345234573450l, (long)js.get(1));
    }

    @org.junit.Test
    public void arrayAsReturnType_shorts_special_case() {
        final List<Short> is = new ArrayList<Short>();
        final List<Short> js = new ArrayList<Short>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            short[][] shorts = {{23234, 23400}};

            @TestCase
            @TestData("shorts")
            public void test(short i, short j) throws Throwable {
                is.add(i);
                js.add(j);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(1, is.size());
        Assert.assertEquals(1, js.size());
        Assert.assertEquals(23234, (short)is.get(0));
        Assert.assertEquals(23400, (short)js.get(0));
    }

    @org.junit.Test
    public void arrayAsReturnType_shorts_special_case_1() {
        final List<Short> is = new ArrayList<Short>();
        final List<Short> js = new ArrayList<Short>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            short[][] shorts = {{243, 23}, {9009, 32000}};

            @TestCase
            @TestData("shorts")
            public void test(short i, short j) throws Throwable {
                is.add(i);
                js.add(j);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(2, is.size());
        Assert.assertEquals(2, js.size());
        Assert.assertEquals(243, (short)is.get(0));
        Assert.assertEquals(23, (short)js.get(0));
        Assert.assertEquals(9009, (short)is.get(1));
        Assert.assertEquals(32000, (short)js.get(1));
    }


    // ----------------------------------------------------------------------------------------

    @org.junit.Test
    public void arrayAsReturnType_ints() {
        final Set<Integer> set = new HashSet<Integer>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            int[] ints = {1, 23, 7, 9};

            @TestCase
            public void test(@TestData("ints") int i) {
                set.add(i);
            }
        }, TU.EMPTY_ARGV);

        Assert.assertTrue(status.isOK());
        Assert.assertEquals(4, set.size());
        Assert.assertTrue(set.contains(1));
        Assert.assertTrue(set.contains(23));
        Assert.assertTrue(set.contains(7));
        Assert.assertTrue(set.contains(9));
    }


    @org.junit.Test
    public void arrayAsReturnType_booleans() {
        final Set<Boolean> set = new HashSet<Boolean>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            boolean[] booleans = {true, false};

            @TestCase
            public void test(@TestData("booleans") boolean b) throws Throwable {
                set.add(b);
            }
        }, TU.EMPTY_ARGV);

        Assert.assertTrue(status.isOK());
        Assert.assertEquals(2, set.size());
        Assert.assertTrue(set.contains(true));
        Assert.assertTrue(set.contains(false));
    }


    @org.junit.Test
    public void arrayAsReturnType_byte() {
        final Set<Byte> set = new HashSet<Byte>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            byte[] bytes = {0, 1, 120, 45, 8};

            @TestCase
            public void test(@TestData("bytes") byte b) throws Throwable {
                set.add(b);
            }
        }, TU.EMPTY_ARGV);

        Assert.assertTrue(status.isOK());
        Assert.assertEquals(5, set.size());
        Assert.assertTrue(set.contains((byte)0));
        Assert.assertTrue(set.contains((byte)1));
        Assert.assertTrue(set.contains((byte)120));
        Assert.assertTrue(set.contains((byte)45));
        Assert.assertTrue(set.contains((byte)8));
    }

    @org.junit.Test
    public void arrayAsReturnType_char() {
        final Set<Character> set = new HashSet<Character>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            char[] chars = {0, 30, 76, 230};

            @TestCase
            public void test(@TestData("chars") char c) throws Throwable {
                set.add(c);
            }
        }, TU.EMPTY_ARGV);

        Assert.assertTrue(status.isOK());
        Assert.assertEquals(4, set.size());
        Assert.assertTrue(set.contains((char)0));
        Assert.assertTrue(set.contains((char)30));
        Assert.assertTrue(set.contains((char)76));
        Assert.assertTrue(set.contains((char)230));
    }

    @org.junit.Test
    public void arrayAsReturnType_double() {
        final Set<Double> set = new HashSet<Double>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            double[] doubles = {0., 3.4, 67.3456, Double.MAX_VALUE};

            @TestCase
            public void test(@TestData("doubles") double d) throws Throwable {
                set.add(d);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(4, set.size());
        Assert.assertTrue(set.contains(0.));
        Assert.assertTrue(set.contains(3.4));
        Assert.assertTrue(set.contains(67.3456));
        Assert.assertTrue(set.contains(Double.MAX_VALUE));
    }

    @org.junit.Test
    public void arrayAsReturnType_float() {
        final Set<Float> set = new HashSet<Float>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            float[] floats = {0.787878f, 73.f, 48324.234f, Float.MIN_VALUE};

            @TestCase
            public void test(@TestData("floats") float f) throws Throwable {
                set.add(f);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(4, set.size());
        Assert.assertTrue(set.contains(0.787878f));
        Assert.assertTrue(set.contains(73.f));
        Assert.assertTrue(set.contains(48324.234f));
        Assert.assertTrue(set.contains(Float.MIN_VALUE));
    }

    @org.junit.Test
    public void arrayAsReturnType_long() {
        final Set<Long> set = new HashSet<Long>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            long[] longs = {Long.MIN_VALUE, Long.MAX_VALUE, 523400023467l, 23424234332424l};

            @TestCase
            public void test(@TestData("longs") long l) throws Throwable {
                set.add(l);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(4, set.size());
        Assert.assertTrue(set.contains(Long.MIN_VALUE));
        Assert.assertTrue(set.contains(Long.MAX_VALUE));
        Assert.assertTrue(set.contains(523400023467l));
        Assert.assertTrue(set.contains(23424234332424l));
    }


    @org.junit.Test
    public void arrayAsReturnType_short() {
        final Set<Short> set = new HashSet<Short>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            short[] shorts = {Short.MIN_VALUE, (short) 938576, (short) 23487, (short) 809, (short) 43};

            @TestCase
            public void test(@TestData("shorts") short s) throws Throwable {
                set.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(5, set.size());
        Assert.assertTrue(set.contains(Short.MIN_VALUE));
        Assert.assertTrue(set.contains((short)938576));
        Assert.assertTrue(set.contains((short)23487));
        Assert.assertTrue(set.contains((short)809));
        Assert.assertTrue(set.contains((short)43));
    }

}
