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

package com.oracle.tck.lib.autd2.unittests.tgfported.asserts;

import com.oracle.tck.lib.autd2.unittests.TU;
import com.sun.tck.TestObject;
import com.sun.tck.lib.Assert;
import com.sun.tck.lib.AssertionFailedException;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class TestPureAssert {

    // todo test messages in case when additional exception is passed for all asserts

    @Test
    public void instantiation() {
        new Assert();
    }

    private static String RANDOM_STRING;

    @Before
    public void reset() {
        RANDOM_STRING = TU.generateRandomString();
    }

    @Test
    public void testAssertTrue_Positive() {
        com.sun.tck.lib.Assert.assertTrue(true, RANDOM_STRING);
    }

    @Test
    public void testAssertTrue_Positive_NoMessage() {
        com.sun.tck.lib.Assert.assertTrue(true);
    }


    @Test
    public void testAssertTrue_Positive_exception() {
        com.sun.tck.lib.Assert.assertTrue(true, RANDOM_STRING, new AbstractMethodError());
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertTrue_Negative() {
        com.sun.tck.lib.Assert.assertTrue(false, RANDOM_STRING);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertTrue_Negative_NoMessage() {
        com.sun.tck.lib.Assert.assertTrue(false);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertTrue_Negative_exeption() {
        com.sun.tck.lib.Assert.assertTrue(false, RANDOM_STRING, new RuntimeException());
    }

    @Test
    public void testAssertTrue_Negative_cause() {
        RuntimeException exception = new RuntimeException("Mine");
        try {
            com.sun.tck.lib.Assert.assertTrue(false, RANDOM_STRING, exception);
            fail("Not thrown anything");
        } catch (AssertionFailedException e) {
            assertSame(exception, e.getCause());
            assertEquals(RANDOM_STRING, e.getMessage());
        }
    }

    @Test
    public void testAssertTrue_Negative_Message() {
        try {
            com.sun.tck.lib.Assert.assertTrue(false, RANDOM_STRING);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals(RANDOM_STRING, e.getMessage());
        }
    }

    @Test
    public void testAssertTrue_Negative_NoMessage_Check() {
        try {
            com.sun.tck.lib.Assert.assertTrue(false);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected: true, was: false", e.getMessage());
        }
    }

    @Test
    public void testAssertNull_Positive() {
        com.sun.tck.lib.Assert.assertNull(null, RANDOM_STRING);
    }

    @Test
    public void testAssertNull_Positive_NoMessage() {
        com.sun.tck.lib.Assert.assertNull(null);
    }

    @Test
    public void testAssertNull_Positive_exception() {
        com.sun.tck.lib.Assert.assertNull(null, RANDOM_STRING, new RuntimeException());
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertNull_Negative() {
        com.sun.tck.lib.Assert.assertNull("234", RANDOM_STRING);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertNull_Negative_NoMessage() {
        com.sun.tck.lib.Assert.assertNull("234123132");
    }

    @Test
    public void testAssertNull_Negative_exception() {
        RuntimeException cause = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertNull("234", RANDOM_STRING, cause);
            fail("Nothing thrown");
        } catch (AssertionFailedException e) {
            assertSame(cause, e.getCause());
            assertEquals(RANDOM_STRING, e.getMessage());
        }
    }

    @Test
    public void testAssertNull_Negative_Message() {
        try {
            com.sun.tck.lib.Assert.assertNull("3143245", RANDOM_STRING);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals(RANDOM_STRING, e.getMessage());
        }
    }

    @Test
    public void testAssertNull_Negative_NoMessage_Check() {
        try {
            com.sun.tck.lib.Assert.assertNull("31432412341245");
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected: null, was: 31432412341245", e.getMessage());
        }
    }

    @Test
    public void testAssertFalse_Positive() {
        com.sun.tck.lib.Assert.assertFalse(false, RANDOM_STRING);
    }

    @Test
    public void testAssertFalse_Positive_NoMessage() {
        com.sun.tck.lib.Assert.assertFalse(false);
    }

    @Test
    public void testAssertFalse_Positive_exception() {
        com.sun.tck.lib.Assert.assertFalse(false, RANDOM_STRING, new RuntimeException());
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertFalse_Negative() {
        com.sun.tck.lib.Assert.assertFalse(true, RANDOM_STRING);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertFalse_Negative_NoMessage() {
        com.sun.tck.lib.Assert.assertFalse(true);
    }

    @Test
    public void testAssertFalse_Negative_exception() {
        RuntimeException cause = new RuntimeException("234234");
        try {
            com.sun.tck.lib.Assert.assertFalse(true, RANDOM_STRING, cause);
            fail();
        } catch (AssertionFailedException e) {
            assertSame(cause, e.getCause());
            assertEquals(RANDOM_STRING, e.getMessage());

        }
    }

    @Test
    public void testAssertFalse_Negative_Message() {
        try {
            com.sun.tck.lib.Assert.assertFalse(true, RANDOM_STRING);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals(RANDOM_STRING, e.getMessage());
        }
    }

    @Test
    public void testAssertFalse_Negative_NoMessage_Check() {
        try {
            com.sun.tck.lib.Assert.assertFalse(true);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected: false, was: true", e.getMessage());
        }
    }

    @Test
    public void testAssertNotNull_Positive() {
        com.sun.tck.lib.Assert.assertNotNull("blabla", RANDOM_STRING);
    }

    @Test
    public void testAssertNotNull_Positive_NoMessage() {
        com.sun.tck.lib.Assert.assertNotNull("blabla");
    }

    @Test
    public void testAssertNotNull_Positive_Exception() {
        com.sun.tck.lib.Assert.assertNotNull("blabla", RANDOM_STRING, new RuntimeException());
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertNotNull_Negative() {
        com.sun.tck.lib.Assert.assertNotNull(null, RANDOM_STRING);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertNotNull_Negative_NoMessage() {
        com.sun.tck.lib.Assert.assertNotNull(null);
    }

    @Test
    public void testAssertNotNull_Negative_exception() {
        RuntimeException runtimeException = new RuntimeException("234234");
        try {
            com.sun.tck.lib.Assert.assertNotNull(null, RANDOM_STRING, runtimeException);
            fail();
        } catch (AssertionFailedException e) {
            assertSame(runtimeException, e.getCause());
            assertEquals(RANDOM_STRING, e.getMessage());
        }
    }

    @Test
    public void testAssertNotNull_Negative_Message() {
        try {
            com.sun.tck.lib.Assert.assertNotNull(null, RANDOM_STRING);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals(RANDOM_STRING, e.getMessage());
        }
    }

    @Test
    public void testAssertNotNull_Negative_NoMessage_Check() {
        try {
            com.sun.tck.lib.Assert.assertNotNull(null);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected not null, was null", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Positive() {
        com.sun.tck.lib.Assert.assertEquals(
                new TestObject("object" + RANDOM_STRING),
                new TestObject("object" + RANDOM_STRING)
        );
    }

    @Test
    public void testAssertEquals_Positive_primitive_long() {
        com.sun.tck.lib.Assert.assertEquals(1029834098213098L, 1029834098213098L);
        com.sun.tck.lib.Assert.assertEquals(Long.MIN_VALUE, Long.MIN_VALUE);
        com.sun.tck.lib.Assert.assertEquals(Long.MAX_VALUE, Long.MAX_VALUE);
    }


    @Test
    public void testAssertEquals_Positive_primitive_double() {
        com.sun.tck.lib.Assert.assertEquals(1029.83409d, 1029.83409d);
    }

    @Test
    public void testAssertEquals_Positive_primitive_double_zeros() {
        com.sun.tck.lib.Assert.assertEquals(0.0d, 0.0d);
        com.sun.tck.lib.Assert.assertEquals(+0.0d, 0.0d);
        com.sun.tck.lib.Assert.assertEquals(0.0d, +0.0d);
        com.sun.tck.lib.Assert.assertEquals(-0.0d, -0.0d);
    }

    @Test
    public void testAssertEquals_Positive_primitive_doublefloat_zeros() {
        com.sun.tck.lib.Assert.assertEquals(0.0d, 0.0f);
        com.sun.tck.lib.Assert.assertEquals(+0.0f, 0.0d);
        com.sun.tck.lib.Assert.assertEquals(0.0f, +0.0d);
        com.sun.tck.lib.Assert.assertEquals(-0.0d, -0.0f);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Negative_primitive_double_diffZeros() {
        com.sun.tck.lib.Assert.assertEquals(+0.0d, -0.0d);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Negative_primitive_double_diffZeros_1() {
        com.sun.tck.lib.Assert.assertEquals(-0.0d, 0.0d);
    }

    @Test
    public void testAssertEquals_Positive_primitive_infinity() {
        com.sun.tck.lib.Assert.assertEquals(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        com.sun.tck.lib.Assert.assertEquals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testAssertEquals_Positive_primitive_double_NaN() {
        com.sun.tck.lib.Assert.assertEquals(Double.NaN, Double.NaN);
    }

    @Test
    public void testAssertEquals_Positive_primitive_float() {
        com.sun.tck.lib.Assert.assertEquals(29.834f, 29.834f);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Negative_primitive_float_diffZeros() {
        com.sun.tck.lib.Assert.assertEquals(+0.0f, -0.0f);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Negative_primitive_float_diffZeros_1() {
        com.sun.tck.lib.Assert.assertEquals(-0.0f, +0.0f);
    }

    @Test
    public void testAssertEquals_Positive_primitive_float_zeros() {
        com.sun.tck.lib.Assert.assertEquals(0.0f, 0.0f);
        com.sun.tck.lib.Assert.assertEquals(+0.0f, 0.0f);
        com.sun.tck.lib.Assert.assertEquals(0.0f, +0.0f);
        com.sun.tck.lib.Assert.assertEquals(-0.0f, -0.0f);
    }


    @Test
    public void testAssertEquals_Positive_primitive_float_infinity() {
        com.sun.tck.lib.Assert.assertEquals(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        com.sun.tck.lib.Assert.assertEquals(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
    }

    @Test
    public void testAssertEquals_Positive_primitive_float_NaN() {
        com.sun.tck.lib.Assert.assertEquals(Float.NaN, Float.NaN);
    }


    @Test
    public void testAssertEquals_Positive_primitive_short() {
        com.sun.tck.lib.Assert.assertEquals((short) 455434, (short) 455434);
        com.sun.tck.lib.Assert.assertEquals(Short.MAX_VALUE, Short.MAX_VALUE);
        com.sun.tck.lib.Assert.assertEquals(Short.MIN_VALUE, Short.MIN_VALUE);
    }

    @Test
    public void testAssertEquals_Positive_primitive_int() {
        com.sun.tck.lib.Assert.assertEquals(890890, 890890);
        com.sun.tck.lib.Assert.assertEquals(Integer.MAX_VALUE, Integer.MAX_VALUE);
        com.sun.tck.lib.Assert.assertEquals(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    @Test
    public void testAssertEquals_Positive_primitive_byte() {
        com.sun.tck.lib.Assert.assertEquals((byte) 112, (byte) 112);
        com.sun.tck.lib.Assert.assertEquals(Byte.MAX_VALUE, Byte.MAX_VALUE);
        com.sun.tck.lib.Assert.assertEquals(Byte.MIN_VALUE, Byte.MIN_VALUE);
    }

    @Test
    public void testAssertEquals_Positive_primitive_char() {
        com.sun.tck.lib.Assert.assertEquals((char) 32, (char) 32);
        com.sun.tck.lib.Assert.assertEquals(Character.MAX_VALUE, Character.MAX_VALUE);
        com.sun.tck.lib.Assert.assertEquals(Character.MIN_VALUE, Character.MIN_VALUE);
    }

    @Test
    public void testAssertEquals_Positive_primitive_boolean_true() {
        com.sun.tck.lib.Assert.assertEquals(true, true);
    }

    @Test
    public void testAssertEquals_Positive_primitive_boolean_false() {
        com.sun.tck.lib.Assert.assertEquals(false, false);
    }

    @Test
    public void testAssertEquals_Positive_exception() {
        com.sun.tck.lib.Assert.assertEquals(
                new TestObject("object" + RANDOM_STRING),
                new TestObject("object" + RANDOM_STRING), new RuntimeException()
        );
    }

    @Test
    public void testAssertEquals_Positive_message_exception() {
        com.sun.tck.lib.Assert.assertEquals(
                new TestObject("object" + RANDOM_STRING),
                new TestObject("object" + RANDOM_STRING),
                "234234", new RuntimeException()
        );
    }

    @Test
    public void testAssertEquals_Positive_message() {
        com.sun.tck.lib.Assert.assertEquals(
                new TestObject("object" + RANDOM_STRING),
                new TestObject("object" + RANDOM_STRING), "a message"
        );
    }

    @Test
    public void testAssertEquals_Positive_arrays_object() {
        com.sun.tck.lib.Assert.assertEquals(
                new TestObject[]{new TestObject(RANDOM_STRING)},
                new TestObject[]{new TestObject(RANDOM_STRING)}
        );
    }

    @Test
    public void testAssertEquals_Positive_arrays_object_2dim() {
        com.sun.tck.lib.Assert.assertEquals(
                new TestObject[][]{{new TestObject(RANDOM_STRING)}, {new TestObject("562")}},
                new TestObject[][]{{new TestObject(RANDOM_STRING)}, {new TestObject("562")}}
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_long() {
        com.sun.tck.lib.Assert.assertEquals(
                new long[]{124, Long.MAX_VALUE},
                new long[]{124, Long.MAX_VALUE}
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays2dim_long() {
        com.sun.tck.lib.Assert.assertEquals(
                new long[][]{{124, Long.MAX_VALUE}, {Long.MIN_VALUE}},
                new long[][]{{124, Long.MAX_VALUE}, {Long.MIN_VALUE}}
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_message() {
        com.sun.tck.lib.Assert.assertEquals(
                new long[]{124, Long.MAX_VALUE},
                new long[]{124, Long.MAX_VALUE}, "324234234"
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_2dim_message() {
        com.sun.tck.lib.Assert.assertEquals(
                new long[][]{{124, Long.MAX_VALUE}, {56, 89}},
                new long[][]{{124, Long.MAX_VALUE}, {56, 89}}, "345tertw435345"
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_long_message_exception() {
        com.sun.tck.lib.Assert.assertEquals(
                new long[]{124, Long.MAX_VALUE},
                new long[]{124, Long.MAX_VALUE}, "23423432",
                new RuntimeException()
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_long_exception() {
        com.sun.tck.lib.Assert.assertEquals(
                new long[]{124, Long.MAX_VALUE},
                new long[]{124, Long.MAX_VALUE},
                new RuntimeException()
        );
    }

    @Test
    public void testAssertEquals_Negative_Arrays_long() {
        long[] arr_1 = {123, 45};
        long[] arr_2 = {78, 45};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[123, 45]\", was given: \"[78, 45]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_long_2dim() {
        long[][] arr_1 = {{123, 45}, {23}};
        long[][] arr_2 = {{78, 45}, {5}};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[[123, 45], [23]]\", was given: \"[[78, 45], [5]]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_long_exception() {
        long[] arr_1 = {123, 45};
        long[] arr_2 = {78, 45};
        RuntimeException cause = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2, cause);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[123, 45]\", was given: \"[78, 45]\"", e.getMessage());
            assertSame(cause, e.getCause());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_long_message() {
        long[] arr_1 = {123, 45};
        long[] arr_2 = {78, 45};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2, "Mess" + RANDOM_STRING);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Mess" + RANDOM_STRING + "\nExpected equal to : \"[123, 45]\", was given: \"[78, 45]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_long_message_exception() {
        long[] arr_1 = {1323, 445};
        long[] arr_2 = {758, 425};
        RuntimeException runtimeException = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2, "Mess" + RANDOM_STRING, runtimeException);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Mess" + RANDOM_STRING, e.getMessage());
            assertSame(runtimeException, e.getCause());
        }
    }

    @Test
    public void testAssertEquals_Positive_Arrays_int() {
        com.sun.tck.lib.Assert.assertEquals(
                new int[]{23, Integer.MAX_VALUE},
                new int[]{23, Integer.MAX_VALUE}
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_int_2dim() {
        com.sun.tck.lib.Assert.assertEquals(
                new int[][]{{23, Integer.MAX_VALUE}, {4, 6}},
                new int[][]{{23, Integer.MAX_VALUE}, {4, 6}}
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_int_message_exception() {
        com.sun.tck.lib.Assert.assertEquals(
                new int[]{23, Integer.MAX_VALUE},
                new int[]{23, Integer.MAX_VALUE}, "234234243", new RuntimeException()
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_int_exception() {
        com.sun.tck.lib.Assert.assertEquals(
                new int[]{23, Integer.MAX_VALUE},
                new int[]{23, Integer.MAX_VALUE}, new RuntimeException()
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_int_message() {
        com.sun.tck.lib.Assert.assertEquals(
                new int[]{23, Integer.MAX_VALUE},
                new int[]{23, Integer.MAX_VALUE}, "123123123"
        );
    }

    @Test
    public void testAssertEquals_Negative_Arrays_int() {
        int[] arr_1 = {6, 5};
        int[] arr_2 = {6, 6};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[6, 5]\", was given: \"[6, 6]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_int_2dim() {
        int[][] arr_1 = {{6, 5}, {7, 5}};
        int[][] arr_2 = {{6, 6}, {9, 3}};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[[6, 5], [7, 5]]\", was given: \"[[6, 6], [9, 3]]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_exception_int() {
        int[] arr_1 = {6, 5};
        int[] arr_2 = {6, 6};
        RuntimeException exception = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2, exception);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[6, 5]\", was given: \"[6, 6]\"", e.getMessage());
            assertSame(exception, e.getCause());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_int_message() {
        int[] arr_1 = {6, 5};
        int[] arr_2 = {6, 6};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2, "Messs" + RANDOM_STRING);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Messs" + RANDOM_STRING + "\nExpected equal to : \"[6, 5]\", was given: \"[6, 6]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_int_message_exception() {
        int[] arr_1 = {6, 5};
        int[] arr_2 = {6, 6};
        RuntimeException exception = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2, "Messs" + RANDOM_STRING, exception);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Messs" + RANDOM_STRING, e.getMessage());
            assertSame(exception, e.getCause());
        }
    }

    @Test
    public void testAssertEquals_Positive_Arrays_short() {
        com.sun.tck.lib.Assert.assertEquals(
                new short[]{67, Short.MAX_VALUE},
                new short[]{67, Short.MAX_VALUE}
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_short_2dim() {
        com.sun.tck.lib.Assert.assertEquals(
                new short[][]{{67, Short.MAX_VALUE}, {45, 56}},
                new short[][]{{67, Short.MAX_VALUE}, {45, 56}}
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_short_3dim() {
        com.sun.tck.lib.Assert.assertEquals(
                new short[][][]{{{67, Short.MAX_VALUE}}},
                new short[][][]{{{67, Short.MAX_VALUE}}}
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_short_message() {
        com.sun.tck.lib.Assert.assertEquals(
                new short[]{67, Short.MAX_VALUE},
                new short[]{67, Short.MAX_VALUE}, "23424234234"
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_short_exception() {
        com.sun.tck.lib.Assert.assertEquals(
                new short[]{67, Short.MAX_VALUE},
                new short[]{67, Short.MAX_VALUE}, new RuntimeException()
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_short_message_exception() {
        com.sun.tck.lib.Assert.assertEquals(
                new short[]{67, Short.MAX_VALUE},
                new short[]{67, Short.MAX_VALUE}, "34324234", new AbstractMethodError()
        );
    }

    @Test
    public void testAssertEquals_Negative_Arrays_short() {
        short[] arr_1 = {45, 78};
        short[] arr_2 = {90, 23};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[45, 78]\", was given: \"[90, 23]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_short_2dim() {
        short[][] arr_1 = {{45, 78}, {3, 8}};
        short[][] arr_2 = {{90, 23}, {9, 3}};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[[45, 78], [3, 8]]\", was given: \"[[90, 23], [9, 3]]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_short_exception() {
        short[] arr_1 = {45, 78};
        short[] arr_2 = {90, 23};
        RuntimeException exception = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2, exception);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[45, 78]\", was given: \"[90, 23]\"", e.getMessage());
            assertSame(exception, e.getCause());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_short_message() {
        short[] arr_1 = {45, 78};
        short[] arr_2 = {90, 23};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2, "M1212" + RANDOM_STRING);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("M1212" + RANDOM_STRING + "\nExpected equal to : \"[45, 78]\", was given: \"[90, 23]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_short_message_exception() {
        short[] arr_1 = {445, 786};
        short[] arr_2 = {960, 223};
        RuntimeException exception = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2, "M1212" + RANDOM_STRING, exception);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("M1212" + RANDOM_STRING, e.getMessage());
            assertSame(exception, e.getCause());
        }
    }

    @Test
    public void testAssertEquals_Positive_Arrays_char() {
        com.sun.tck.lib.Assert.assertEquals(
                new char[]{67, Character.MAX_VALUE},
                new char[]{67, Character.MAX_VALUE}
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_char_2dim() {
        com.sun.tck.lib.Assert.assertEquals(
                new char[][]{{67, Character.MAX_VALUE}},
                new char[][]{{67, Character.MAX_VALUE}});
    }

    @Test
    public void testAssertEquals_Positive_Arrays_char_message_exception() {
        com.sun.tck.lib.Assert.assertEquals(
                new char[]{12, Character.MAX_VALUE},
                new char[]{12, Character.MAX_VALUE}, "123123", new RuntimeException()
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_char_exception() {
        com.sun.tck.lib.Assert.assertEquals(
                new char[]{12, Character.MAX_VALUE},
                new char[]{12, Character.MAX_VALUE}, new RuntimeException()
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_char_message() {
        com.sun.tck.lib.Assert.assertEquals(
                new char[]{12, Character.MAX_VALUE},
                new char[]{12, Character.MAX_VALUE}, "123123423423"
        );
    }

    @Test
    public void testAssertEquals_Negative_Arrays_char() {
        char[] arr_1 = {'a', 'b'};
        char[] arr_2 = {'e', 'f'};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[a, b]\", was given: \"[e, f]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_char_2dim() {
        char[][] arr_1 = {{'a', 'b'}, {'f', 'e'}};
        char[][] arr_2 = {{'e', 'f'}, {'y', 'u'}};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[[a, b], [f, e]]\", was given: \"[[e, f], [y, u]]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_char_exception() {
        char[] arr_1 = {'a', 'b'};
        char[] arr_2 = {'e', 'f'};
        RuntimeException runtimeException = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2, runtimeException);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[a, b]\", was given: \"[e, f]\"", e.getMessage());
            assertSame(runtimeException, e.getCause());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_char_message() {
        char[] arr_1 = {'a', 'b'};
        char[] arr_2 = {'e', 'f'};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2, "7879sdf987s9a7fsda97fd");
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("7879sdf987s9a7fsda97fd\nExpected equal to : \"[a, b]\", was given: \"[e, f]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_char_message_exception() {
        char[] arr_1 = {'a', 'b'};
        char[] arr_2 = {'b', 's'};
        RuntimeException exception = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2, "7879sdf987s9a7fsda97fd", exception);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("7879sdf987s9a7fsda97fd", e.getMessage());
            assertSame(exception, e.getCause());
        }
    }

    @Test
    public void testAssertEquals_Positive_Arrays_byte() {
        com.sun.tck.lib.Assert.assertEquals(
                new byte[]{67, Byte.MAX_VALUE},
                new byte[]{67, Byte.MAX_VALUE}
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_byte_2dim() {
        com.sun.tck.lib.Assert.assertEquals(
                new byte[][]{{67, Byte.MAX_VALUE}, {23}},
                new byte[][]{{67, Byte.MAX_VALUE}, {23}}
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_byte_message_exception() {
        com.sun.tck.lib.Assert.assertEquals(
                new byte[]{67, Byte.MAX_VALUE},
                new byte[]{67, Byte.MAX_VALUE}, RANDOM_STRING, new RuntimeException()
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_byte_exception() {
        com.sun.tck.lib.Assert.assertEquals(
                new byte[]{67, Byte.MAX_VALUE},
                new byte[]{67, Byte.MAX_VALUE}, new RuntimeException()
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_byte_message() {
        com.sun.tck.lib.Assert.assertEquals(
                new byte[]{67, Byte.MAX_VALUE},
                new byte[]{67, Byte.MAX_VALUE}, RANDOM_STRING
        );
    }

    @Test
    public void testAssertEquals_Negative_Arrays_byte() {
        byte[] arr_1 = {45, 78};
        byte[] arr_2 = {90, 23};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[45, 78]\", was given: \"[90, 23]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays2dim_byte() {
        byte[][] arr_1 = {{45, 78}, {3, 7}};
        byte[][] arr_2 = {{90, 23}, {8, 9}};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[[45, 78], [3, 7]]\", was given: \"[[90, 23], [8, 9]]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_byte_exception() {
        byte[] arr_1 = {45, 78};
        byte[] arr_2 = {90, 23};
        RuntimeException cause = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertEquals(arr_1, arr_2, cause);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[45, 78]\", was given: \"[90, 23]\"", e.getMessage());
            assertSame(cause, e.getCause());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_byte_message() {
        byte[] arr_1 = {45, 78};
        byte[] arr_2 = {90, 23};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2, "kljhsadfuiyewqoiy987");
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("kljhsadfuiyewqoiy987\nExpected equal to : \"[45, 78]\", was given: \"[90, 23]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_byte_message_exception() {
        byte[] arr_1 = {45, 78};
        byte[] arr_2 = {90, 23};
        RuntimeException exception = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2, "kljhsadfuiyewqoiy987", exception);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("kljhsadfuiyewqoiy987", e.getMessage());
            assertSame(exception, e.getCause());
        }
    }

    @Test
    public void testAssertEquals_Positive_Arrays_boolean() {
        com.sun.tck.lib.Assert.assertEquals(
                new boolean[]{true, false, true},
                new boolean[]{true, false, true}
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_boolean_2dim() {
        com.sun.tck.lib.Assert.assertEquals(
                new boolean[][]{{true, false, true}, {false}, {true}},
                new boolean[][]{{true, false, true}, {false}, {true}}
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_boolean_message_exception() {
        com.sun.tck.lib.Assert.assertEquals(
                new boolean[]{true, false, true},
                new boolean[]{true, false, true}, "23123" + RANDOM_STRING, new RuntimeException()
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_boolean_exception() {
        com.sun.tck.lib.Assert.assertEquals(
                new boolean[]{true, false, true},
                new boolean[]{true, false, true}, new RuntimeException()
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_boolean_message() {
        com.sun.tck.lib.Assert.assertEquals(
                new boolean[]{true, false, true},
                new boolean[]{true, false, true}, "23123" + RANDOM_STRING
        );
    }

    @Test
    public void testAssertEquals_Negative_Arrays_boolean() {
        boolean[] arr_1 = {true, false};
        boolean[] arr_2 = {false, true};
        try {
            com.sun.tck.lib.Assert.assertEquals(arr_1, arr_2);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[true, false]\", was given: \"[false, true]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays2dim_boolean() {
        boolean[][] arr_1 = {{true, false}, {false, false}};
        boolean[][] arr_2 = {{false, true}, {false, false}};
        try {
            com.sun.tck.lib.Assert.assertEquals(arr_1, arr_2);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[[true, false], [false, false]]\", was given: \"[[false, true], [false, false]]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_boolean_2dim() {
        boolean[][] arr_1 = {{true, false}, {true, false}};
        boolean[][] arr_2 = {{false, true}, {true, false}};
        try {
            com.sun.tck.lib.Assert.assertEquals(arr_1, arr_2);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[[true, false], [true, false]]\", was given: \"[[false, true], [true, false]]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_boolean_exception() {
        boolean[] arr_1 = {true, false};
        boolean[] arr_2 = {false, true};
        RuntimeException exception = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertEquals(arr_1, arr_2, exception);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[true, false]\", was given: \"[false, true]\"",
                    e.getMessage());
            assertSame(exception, e.getCause());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_boolean_message() {
        boolean[] arr_1 = {true, false};
        boolean[] arr_2 = {false, true};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2, "9879870000");
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("9879870000\nExpected equal to : \"[true, false]\", was given: \"[false, true]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_boolean_message_exception() {
        boolean[] arr_1 = {true, false};
        boolean[] arr_2 = {false, true};
        RuntimeException exception = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2, "9879870000", exception);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("9879870000", e.getMessage());
            assertSame(exception, e.getCause());
        }
    }

    @Test
    public void testAssertEquals_Positive_Arrays_float() {
        com.sun.tck.lib.Assert.assertEquals(
                new float[]{0.3f, 0.5f},
                new float[]{0.3f, 0.5f}
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_float_2dim() {
        com.sun.tck.lib.Assert.assertEquals(
                new float[][]{{0.3f, 0.5f}, {51f}},
                new float[][]{{0.3f, 0.5f}, {51f}}
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_float_message_exception() {
        com.sun.tck.lib.Assert.assertEquals(
                new float[]{0.3f, 0.5f},
                new float[]{0.3f, 0.5f}, RANDOM_STRING + "231123213", new RuntimeException()
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_float_message() {
        com.sun.tck.lib.Assert.assertEquals(
                new float[]{0.3f, 0.5f},
                new float[]{0.3f, 0.5f}, RANDOM_STRING + "231123213"
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_float_exception() {
        com.sun.tck.lib.Assert.assertEquals(
                new float[]{0.3f, 0.5f},
                new float[]{0.3f, 0.5f}, new RuntimeException()
        );
    }

    @Test
    public void testAssertEquals_Negative_Arrays_float() {
        float[] arr_1 = {0.7f, 0.9f};
        float[] arr_2 = {14.0f};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[0.7, 0.9]\", was given: \"[14.0]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays2dim_float() {
        float[][] arr_1 = {{0.7f, 0.9f}};
        float[] arr_2 = {14.0f};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[[0.7, 0.9]]\", was given: \"[14.0]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_float_exception() {
        float[] arr_1 = {0.7f, 0.9f};
        float[] arr_2 = {14.0f};
        AbstractMethodError cause = new AbstractMethodError();
        try {
            com.sun.tck.lib.Assert.assertEquals(arr_1, arr_2, cause);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[0.7, 0.9]\", was given: \"[14.0]\"", e.getMessage());
            assertSame(cause, e.getCause());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_float_message() {
        float[] arr_1 = {0.7f, 0.9f};
        float[] arr_2 = {14.0f};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2, "lhlkajhdlfk");
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("lhlkajhdlfk\nExpected equal to : \"[0.7, 0.9]\", was given: \"[14.0]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_float_message_exception() {
        float[] arr_1 = {0.7f, 0.9f};
        float[] arr_2 = {14.0f};
        RuntimeException exception = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2, "lhlkajhdlfk", exception);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("lhlkajhdlfk", e.getMessage());
            assertSame(exception, e.getCause());
        }
    }

    @Test
    public void testAssertEquals_Positive_Arrays_double() {
        com.sun.tck.lib.Assert.assertEquals(
                new double[]{0.3, 1.5},
                new double[]{0.3, 1.5}
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays2dim_double() {
        com.sun.tck.lib.Assert.assertEquals(
                new double[][]{{0.3, 1.5}, {6.7}},
                new double[][]{{0.3, 1.5}, {6.7}}
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays3dim_double() {
        com.sun.tck.lib.Assert.assertEquals(
                new double[][][]{{{0.3, 1.5}, {6.7}}},
                new double[][][]{{{0.3, 1.5}, {6.7}}}
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_double_message_exception() {
        com.sun.tck.lib.Assert.assertEquals(
                new double[]{0.3, 1.5},
                new double[]{0.3, 1.5}, RANDOM_STRING + "34234234234", new RuntimeException()
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_double_exception() {
        com.sun.tck.lib.Assert.assertEquals(
                new double[]{0.3, 1.54},
                new double[]{0.3, 1.54}, new RuntimeException()
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_double_message() {
        com.sun.tck.lib.Assert.assertEquals(
                new double[]{0.3, 1.51},
                new double[]{0.3, 1.51}, RANDOM_STRING + "34234234234"
        );
    }

    @Test
    public void testAssertEquals_Negative_Arrays_double() {
        double[] arr_1 = {890};
        double[] arr_2 = {1152.8};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[890.0]\", was given: \"[1152.8]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_double_2dim() {
        double[][] arr_1 = {{890}, {78.9, 3.789}};
        double[][] arr_2 = {{1152.8}, {56, 7, 9, 90.3}};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[[890.0], [78.9, 3.789]]\", was given: \"[[1152.8], [56.0, 7.0, 9.0, 90.3]]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_double_exception() {
        double[] arr_1 = {890};
        double[] arr_2 = {1152.8};
        RuntimeException runtimeException = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertEquals(arr_1, arr_2, runtimeException);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[890.0]\", was given: \"[1152.8]\"", e.getMessage());
            assertSame(runtimeException, e.getCause());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_double_message() {
        double[] arr_1 = {890};
        double[] arr_2 = {1152.8};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2, "opiuopisduf09-80980");
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("opiuopisduf09-80980\nExpected equal to : \"[890.0]\", was given: \"[1152.8]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_double_message_exception() {
        double[] arr_1 = {890};
        double[] arr_2 = {1152.8};
        RuntimeException exception = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2, "opiuopisduf09-80980", exception);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("opiuopisduf09-80980", e.getMessage());
            assertSame(exception, e.getCause());
        }
    }

    @Test
    public void testAssertEquals_Positive_Arrays_Object() {
        com.sun.tck.lib.Assert.assertEquals(
                new TestObject[]{new TestObject("one")},
                new TestObject[]{new TestObject("one")}
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_Object_3dim() {
        com.sun.tck.lib.Assert.assertEquals(
                new TestObject[][][]{{{new TestObject("one")}}},
                new TestObject[][][]{{{new TestObject("one")}}}
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_Object_message_exception() {
        com.sun.tck.lib.Assert.assertEquals(
                new TestObject[]{new TestObject("one")},
                new TestObject[]{new TestObject("one")}, "234234234", new RuntimeException()
        );
    }

    @Test
    public void testAssertEquals_Positive_Arrays_Object_exception() {
        com.sun.tck.lib.Assert.assertEquals(
                new TestObject[]{new TestObject("one")},
                new TestObject[]{new TestObject("one")}, new RuntimeException()
        );
    }

    @Test
    public void testAssertEquals_Negative_Arrays_Object() {
        TestObject[] arr_1 = new TestObject[]{new TestObject("one")};
        TestObject[] arr_2 = new TestObject[]{new TestObject("two")};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[TestObject:one]\", was given: \"[TestObject:two]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays2dim_Object() {
        TestObject[][] arr_1 = new TestObject[][]{{new TestObject("one")}, {new TestObject("three")}};
        TestObject[][] arr_2 = new TestObject[][]{{new TestObject("two")}, {new TestObject("four")}};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[[TestObject:one], [TestObject:three]]\", was given: \"[[TestObject:two], [TestObject:four]]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_Object_exception() {
        TestObject[] arr_1 = new TestObject[]{new TestObject("one")};
        TestObject[] arr_2 = new TestObject[]{new TestObject("two")};
        RuntimeException runtimeException = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2, runtimeException);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[TestObject:one]\", was given: \"[TestObject:two]\"", e.getMessage());
            assertSame(runtimeException, e.getCause());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_Object_message_exception() {
        TestObject[] arr_1 = new TestObject[]{new TestObject("one")};
        TestObject[] arr_2 = new TestObject[]{new TestObject("two")};
        RuntimeException exception = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertEquals(arr_1, arr_2, RANDOM_STRING + "123123", exception);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals(RANDOM_STRING + "123123", e.getMessage());
            assertSame(exception, e.getCause());
        }
    }

    @Test
    public void testAssertEquals_Negative_Arrays_Object_message() {
        TestObject[] arr_1 = new TestObject[]{new TestObject("one")};
        TestObject[] arr_2 = new TestObject[]{new TestObject("two")};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    arr_1, arr_2, "kljhsadfhll");
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("kljhsadfhll" + "\nExpected equal to : \"[TestObject:one]\", was given: \"[TestObject:two]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Positive_sameObject() {
        TestObject testObject = new TestObject("object" + RANDOM_STRING);
        com.sun.tck.lib.Assert.assertEquals(testObject, testObject);
    }

    @Test
    public void testAssertEquals_Positive_sameObject_exception() {
        TestObject testObject = new TestObject("object" + RANDOM_STRING);
        com.sun.tck.lib.Assert.assertEquals(testObject, testObject, new RuntimeException());
    }

    @Test
    public void testAssertEquals_Positive_sameObject_message() {
        TestObject testObject = new TestObject("object" + RANDOM_STRING);
        com.sun.tck.lib.Assert.assertEquals(testObject, testObject, "12312123123");
    }

    @Test
    public void testAssertEquals_Positive_sameObject_message_exception() {
        TestObject testObject = new TestObject("object" + RANDOM_STRING);
        com.sun.tck.lib.Assert.assertEquals(testObject, testObject, "12312123123", new RuntimeException());
    }

    @Test
    public void testAssertEquals_Positive_sameArray() {
        TestObject[] testObjects = new TestObject[]{new TestObject("object0" + RANDOM_STRING), new TestObject("object1" + RANDOM_STRING)};
        com.sun.tck.lib.Assert.assertEquals(testObjects, testObjects);
    }

    @Test
    public void testAssertEquals_Positive_null() {
        com.sun.tck.lib.Assert.assertEquals((Object) null, (Object) null);
    }

    @Test
    public void testAssertEquals_Positive_null_exception() {
        com.sun.tck.lib.Assert.assertEquals((Object) null, (Object) null, new RuntimeException());
    }

    @Test
    public void testAssertEquals_Positive_null_message() {
        com.sun.tck.lib.Assert.assertEquals((Object) null, (Object) null, "32423423432");
    }

    @Test
    public void testAssertEquals_Positive_null_message_exception() {
        com.sun.tck.lib.Assert.assertEquals((Object) null, (Object) null, "32423423432", new RuntimeException());
    }

    @Test
    public void testAssertEquals_Positive_null_array() {
        com.sun.tck.lib.Assert.assertEquals((Object[]) null, (Object[]) null);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Negative() {
        com.sun.tck.lib.Assert.assertEquals(
                new TestObject("first" + RANDOM_STRING),
                new TestObject("second" + RANDOM_STRING)
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Negative_primitive_long() {
        com.sun.tck.lib.Assert.assertEquals(89345634563453L, 8834564357653743375L);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Negative_primitive_longInt() {
        com.sun.tck.lib.Assert.assertEquals(89345634563453L, 643576537);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Negative_primitive_double() {
        com.sun.tck.lib.Assert.assertEquals(8.4444119d, 8.4444118d);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Negative_primitive_float() {
        com.sun.tck.lib.Assert.assertEquals(8.444411f, 8.444412f);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Negative_primitive_short() {
        com.sun.tck.lib.Assert.assertEquals((short) 843, (short) 845);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Negative_primitive_shortByte() {
        com.sun.tck.lib.Assert.assertEquals((short) 843, (byte) 5);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Negative_primitive_int() {
        com.sun.tck.lib.Assert.assertEquals(43, 85);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Negative_primitive_byte() {
        com.sun.tck.lib.Assert.assertEquals((byte) 84, (byte) 85);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Negative_primitive_char() {
        com.sun.tck.lib.Assert.assertEquals((char) 84, (char) 85);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Negative_primitive_boolean_1() {
        com.sun.tck.lib.Assert.assertEquals(true, false);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Negative_primitive_boolean_2() {
        com.sun.tck.lib.Assert.assertEquals(false, true);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Negative_message() {
        com.sun.tck.lib.Assert.assertEquals(
                new TestObject("first" + RANDOM_STRING),
                new TestObject("second" + RANDOM_STRING), " They are not equal"
        );
    }

    @Test
    public void testAssertEquals_Negative_Array_1() {
        String[] strings_1 = {"a", "b", "c"};
        String[] strings_2 = {"d", "e", "f"};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    strings_1, strings_2);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[a, b, c]\", was given: \"[d, e, f]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Array_1_2dim() {
        String[][] strings_1 = {{"a", "b", "c"}, {"asd"}};
        String[][] strings_2 = {{"d", "e", "f"}, {"gfh"}};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    strings_1, strings_2);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[[a, b, c], [asd]]\", was given: \"[[d, e, f], [gfh]]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Array_2() {
        boolean[] booleans_1 = {true};
        boolean[] booleans_2 = {false};
        try {
            com.sun.tck.lib.Assert.assertEquals(
                    booleans_1, booleans_2);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected equal to : \"[true]\", was given: \"[false]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_Message() {
        TestObject testObject_1 = new TestObject("first" + RANDOM_STRING);
        TestObject testObject_2 = new TestObject("second" + RANDOM_STRING);
        try {
            com.sun.tck.lib.Assert.assertEquals(testObject_1, testObject_2);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals(
                    "Expected equal to : \"" + testObject_1 + "\", was given: \"" + testObject_2 + "\"",
                    e.getMessage());
        }
    }

    @Test
    public void testAssertEquals_Negative_exception() {
        TestObject testObject_1 = new TestObject("first" + RANDOM_STRING);
        TestObject testObject_2 = new TestObject("second" + RANDOM_STRING);
        RuntimeException cause = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertEquals(testObject_1, testObject_2, cause);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals(
                    "Expected equal to : \"" + testObject_1 + "\", was given: \"" + testObject_2 + "\"",
                    e.getMessage());
            assertSame(cause, e.getCause());
        }
    }

    @Test
    public void testAssertEquals_Negative_message_exception() {
        TestObject testObject_1 = new TestObject("first" + RANDOM_STRING);
        TestObject testObject_2 = new TestObject("second" + RANDOM_STRING);
        RuntimeException exception = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertEquals(testObject_1, testObject_2, RANDOM_STRING + "message", exception);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals(RANDOM_STRING + "message", e.getMessage());
            assertSame(exception, e.getCause());
        }
    }

    @Test
    public void testAssertEquals_Negative_Message_custom() {
        TestObject testObject_1 = new TestObject("1" + RANDOM_STRING);
        TestObject testObject_2 = new TestObject("2" + RANDOM_STRING);
        try {
            com.sun.tck.lib.Assert.assertEquals(testObject_1, testObject_2, "MyMessage" + RANDOM_STRING);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals(
                    "MyMessage" + RANDOM_STRING + "\n" + "Expected equal to : \"" +
                            testObject_1 + "\", was given: \"" + testObject_2 + "\"",
                    e.getMessage());
        }
    }


    @Test
    public void testAssertNotEqual_Positive() {
        com.sun.tck.lib.Assert.assertNotEquals(
                new TestObject("first"),
                new TestObject("second"));
    }

    @Test
    public void testAssertNotEqual_Positive_exception() {
        com.sun.tck.lib.Assert.assertNotEquals(
                new TestObject("first"),
                new TestObject("second"), new RuntimeException());
    }

    @Test
    public void testAssertNotEqual_Positive_message() {
        com.sun.tck.lib.Assert.assertNotEquals(
                new TestObject("first"),
                new TestObject("second"), "23421412sfsaf234234234sdf234234");
    }

    @Test
    public void testAssertNotEqual_PositiveArrays() {
        com.sun.tck.lib.Assert.assertNotEquals(
                new TestObject[]{new TestObject("first"), new TestObject("second")},
                new TestObject[]{new TestObject("second")});
    }

    @Test
    public void testAssertNotEqual_PositiveArrays_2dim() {
        com.sun.tck.lib.Assert.assertNotEquals(
                new TestObject[][]{{new TestObject("first"), new TestObject("second")}},
                new TestObject[][]{{new TestObject("second")}, {new TestObject("third")}}
        );
    }

    @Test
    public void testAssertNotEqual_PositiveArrays_2dim_message() {
        com.sun.tck.lib.Assert.assertNotEquals(
                new TestObject[][]{{new TestObject("first"), new TestObject("second")}},
                new TestObject[][]{{new TestObject("second")}, {new TestObject("third")}},
                "kerhwtklwehtlkrhtwelkrhtsblgd kjlshgdgh"
        );
    }

    @Test
    public void testAssertNotEqual_PositiveArrays_message() {
        com.sun.tck.lib.Assert.assertNotEquals(
                new TestObject[]{new TestObject("first"), new TestObject("second")},
                new TestObject[]{new TestObject("second")},
                "234241wrwrqwer23423421342134");
    }

    @Test
    public void testAssertNotEqual_PositiveArrays_exception() {
        com.sun.tck.lib.Assert.assertNotEquals(
                new TestObject[]{new TestObject("first"), new TestObject("second")},
                new TestObject[]{new TestObject("second")},
                new RuntimeException());
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertNotEquals_Negative_equal() {
        com.sun.tck.lib.Assert.assertNotEquals(
                new TestObject(RANDOM_STRING),
                new TestObject(RANDOM_STRING)
        );
    }

    @Test
    public void testAssertNotEquals_Negative_message() {
        TestObject to = new TestObject(RANDOM_STRING);
        try {
            com.sun.tck.lib.Assert.assertNotEquals(
                    to,
                    to, "234241234dfasfsaf234234"
            );
            fail("AssertionFailedException was not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("234241234dfasfsaf234234\nExpected unequal objects, were equal: \"" +
                    to +
                    "\", \"" +
                    to +
                    "\"", e.getMessage());
        }
    }

    @Test
    public void testAssertNotEquals_Negative_message_exception() {
        RuntimeException exception = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertNotEquals(
                    new TestObject(RANDOM_STRING),
                    new TestObject(RANDOM_STRING), RANDOM_STRING, exception
            );
            fail("AssertionFailedException was not thrown");
        } catch (AssertionFailedException e) {
            assertEquals(RANDOM_STRING, e.getMessage());
            assertSame(exception, e.getCause());
        }
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertNotEquals_Negative_equal_arrays() {
        com.sun.tck.lib.Assert.assertNotEquals(
                new TestObject[]{new TestObject(RANDOM_STRING)},
                new TestObject[]{new TestObject(RANDOM_STRING)}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertNotEquals_Negative_equal_arrays_2dim() {
        com.sun.tck.lib.Assert.assertNotEquals(
                new TestObject[][]{{new TestObject(RANDOM_STRING)}, {new TestObject(RANDOM_STRING + "123")}},
                new TestObject[][]{{new TestObject(RANDOM_STRING)}, {new TestObject(RANDOM_STRING + "123")}}
        );
    }

    @Test
    public void testAssertNotEquals_arrays_equal_message() {
        TestObject[] tos = {new TestObject(RANDOM_STRING)};
        try {
            com.sun.tck.lib.Assert.assertNotEquals(
                    tos,
                    tos, "234234234"
            );
            fail("AssertionFailedException was not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("234234234\nExpected unequal objects, were equal: \"" +
                    Arrays.toString(tos) + "\", \"" + Arrays.toString(tos) + "\"", e.getMessage());
        }
    }

    @Test
    public void testAssertNotEquals_arrays2dim_equal_message() {
        final TestObject[][] tos = {{new TestObject(RANDOM_STRING)}, {new TestObject("123")}};
        try {
            com.sun.tck.lib.Assert.assertNotEquals(
                    tos,
                    tos,
                    "234234234"
            );
            fail("AssertionFailedException was not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("234234234\nExpected unequal objects, were equal: \"" +
                    Arrays.deepToString(tos) +
                    "\", \"" +
                    Arrays.deepToString(tos) +
                    "\"", e.getMessage());
        }
    }

    @Test
    public void testAssertNotEquals_Negative_arrays_exception() {
        RuntimeException exception = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertNotEquals(
                    new TestObject[]{new TestObject(RANDOM_STRING)},
                    new TestObject[]{new TestObject(RANDOM_STRING)}, exception
            );
            fail("AssertionFailedException was not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected unequal objects, were equal: \"[TestObject:" +
                    RANDOM_STRING +
                    "]\", \"[TestObject:" +
                    RANDOM_STRING +
                    "]\"", e.getMessage());
            assertSame(exception, e.getCause());
        }
    }

    @Test
    public void testAssertNotEquals_Negative_arrays2dim_exception() {
        RuntimeException exception = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertNotEquals(
                    new TestObject[][]{{new TestObject("sdf")}, {new TestObject("123")}},
                    new TestObject[][]{{new TestObject("sdf")}, {new TestObject("123")}},
                    exception
            );
            fail("AssertionFailedException was not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected unequal objects, were equal: \"[[TestObject:sdf], [TestObject:123]]\", \"[[TestObject:sdf], [TestObject:123]]\"", e.getMessage());
            assertSame(exception, e.getCause());
        }
    }

    @Test
    public void testAssertNotEquals_Negative_equal_message_exception() {
        RuntimeException exception = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertNotEquals(
                    new TestObject[]{new TestObject(RANDOM_STRING)},
                    new TestObject[]{new TestObject(RANDOM_STRING)}, "234234234", exception
            );
            fail("AssertionFailedException was not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("234234234", e.getMessage());
            assertSame(exception, e.getCause());
        }
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertNotEquals_Negative_same() {
        TestObject testObject = new TestObject(RANDOM_STRING);
        com.sun.tck.lib.Assert.assertNotEquals(
                testObject,
                testObject
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertNotEquals_Negative_same_arrays() {
        TestObject[] testObject = new TestObject[]{new TestObject(RANDOM_STRING), new TestObject(RANDOM_STRING + "123")};
        com.sun.tck.lib.Assert.assertNotEquals(
                testObject,
                testObject
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertNotEquals_Negative_null() {
        com.sun.tck.lib.Assert.assertNotEquals(null, null);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertNotEquals_Negative_nullArrays() {
        com.sun.tck.lib.Assert.assertNotEquals((Object[]) null, (Object[]) null);
    }

    @Test
    public void testAssertNotEquals_Negative_Message() {
        TestObject testObject_1 = new TestObject("theFirst") {
            public boolean equals(Object o) {
                return true;
            }
        };
        TestObject testObject_2 = new TestObject("theSecond") {
            public boolean equals(Object o) {
                return true;
            }
        };
        try {
            com.sun.tck.lib.Assert.assertNotEquals(testObject_1, testObject_2);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals(
                    "Expected unequal objects, were equal: \"TestObject:theFirst\", \"TestObject:theSecond\"",
                    e.getMessage());
        }
    }

    @Test
    public void testAssertNotEquals_Negative_exception() {
        TestObject testObject_1 = new TestObject("theFirst") {
            public boolean equals(Object o) {
                return true;
            }
        };
        TestObject testObject_2 = new TestObject("theSecond") {
            public boolean equals(Object o) {
                return true;
            }
        };
        RuntimeException cause = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertNotEquals(testObject_1, testObject_2, cause);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals(
                    "Expected unequal objects, were equal: \"TestObject:theFirst\", \"TestObject:theSecond\"",
                    e.getMessage());
            assertSame(cause, e.getCause());
        }
    }

    @Test
    public void testAssertNotEquals_Negative_Message_arrays() {
        TestObject testObject_1 = new TestObject("theFirst") {
            public boolean equals(Object o) {
                return true;
            }
        };
        TestObject testObject_2 = new TestObject("theSecond") {
            public boolean equals(Object o) {
                return true;
            }
        };
        try {
            com.sun.tck.lib.Assert.assertNotEquals(
                    new TestObject[]{testObject_1}, new TestObject[]{testObject_2});
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals(
                    "Expected unequal objects, were equal: \"[TestObject:theFirst]\", \"[TestObject:theSecond]\"",
                    e.getMessage());
        }
    }


    @Test
    public void testAssertSame_Positive() {
        TestObject testObject = new TestObject(RANDOM_STRING);
        com.sun.tck.lib.Assert.assertSame(testObject, testObject);
    }

    @Test
    public void testAssertSame_Positive_exception() {
        TestObject testObject = new TestObject(RANDOM_STRING);
        com.sun.tck.lib.Assert.assertSame(testObject, testObject, new RuntimeException());
    }

    @Test
    public void testAssertSame_Positive_message() {
        TestObject testObject = new TestObject(RANDOM_STRING);
        com.sun.tck.lib.Assert.assertSame(testObject, testObject, "3490275092375");
    }

    @Test
    public void testAssertSame_Positive_message_exception() {
        TestObject testObject = new TestObject(RANDOM_STRING);
        com.sun.tck.lib.Assert.assertSame(testObject, testObject, "3490275092375", new IllegalAccessException());
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSame_Negative() {
        com.sun.tck.lib.Assert.assertSame(new TestObject(RANDOM_STRING), new TestObject(RANDOM_STRING));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSame_Negative_exception() {
        com.sun.tck.lib.Assert.assertSame(new TestObject(RANDOM_STRING), new TestObject(RANDOM_STRING), new RuntimeException());
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSame_Negative_message() {
        com.sun.tck.lib.Assert.assertSame(new TestObject(RANDOM_STRING), new TestObject(RANDOM_STRING), "345234523");
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSame_Negative_notEqual() {
        com.sun.tck.lib.Assert.assertSame(new TestObject("first"), new TestObject("second"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSame_Negative_notEqual_message() {
        com.sun.tck.lib.Assert.assertSame(new TestObject("first"), new TestObject("second"), "123424");
    }

    @Test
    public void testAssertSame_Negative_standardMessage() {
        try {
            com.sun.tck.lib.Assert.assertSame(
                    new TestObject(RANDOM_STRING),
                    new TestObject(RANDOM_STRING));
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected same as : \"" +
                    new TestObject(RANDOM_STRING) +
                    "\", was given: \"" +
                    new TestObject(RANDOM_STRING) +
                    "\"", e.getMessage());
        }
    }

    @Test
    public void testAssertSame_Negative_standardMessage_exception() {
        RuntimeException runtimeException = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertSame(
                    new TestObject(RANDOM_STRING),
                    new TestObject(RANDOM_STRING), runtimeException);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertSame(runtimeException, e.getCause());
            assertEquals("Expected same as : \"" +
                    new TestObject(RANDOM_STRING) +
                    "\", was given: \"" +
                    new TestObject(RANDOM_STRING) +
                    "\"", e.getMessage());
        }
    }

    @Test
    public void testAssertSame_Negative_Message_customMessage() {
        final TestObject to = new TestObject(RANDOM_STRING);
        final TestObject to1 = new TestObject(RANDOM_STRING);
        try {
            com.sun.tck.lib.Assert.assertSame(
                    to,
                    to1, "werwer12534523535");
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("werwer12534523535\nExpected same as : \"" +
                    to +
                    "\", was given: \"" +
                    to1 +
                    "\"", e.getMessage());
        }
    }

    @Test
    public void testAssertSame_Negative_Message_exception_message() {
        RuntimeException exception = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertSame(
                    new TestObject(RANDOM_STRING),
                    new TestObject(RANDOM_STRING), "sdfsdfsdfsf", exception);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("sdfsdfsdfsf", e.getMessage());
            assertSame(exception, e.getCause());
        }
    }

    @Test
    public void testAssertSame_Negative_Message_arrays() {
        try {
            com.sun.tck.lib.Assert.assertSame(
                    new TestObject[]{new TestObject(RANDOM_STRING)},
                    new TestObject[]{new TestObject(RANDOM_STRING)});
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected same as : \"[" +
                    new TestObject(RANDOM_STRING) +
                    "]\", was given: \"[" +
                    new TestObject(RANDOM_STRING) +
                    "]\"", e.getMessage());
        }
    }

    @Test
    public void testAssertNotSame_Positive_Equal() {
        com.sun.tck.lib.Assert.assertNotSame(
                new TestObject(RANDOM_STRING),
                new TestObject(RANDOM_STRING)
        );
    }

    @Test
    public void testAssertNotSame_Positive_Equal_message() {
        com.sun.tck.lib.Assert.assertNotSame(
                new TestObject(RANDOM_STRING),
                new TestObject(RANDOM_STRING), RANDOM_STRING + "werwerew34wdfdsf"
        );
    }

    @Test
    public void testAssertNotSame_Positive_Equal_message_exception() {
        com.sun.tck.lib.Assert.assertNotSame(
                new TestObject(RANDOM_STRING),
                new TestObject(RANDOM_STRING), RANDOM_STRING + "werwerew34wdfdsf", new RuntimeException()
        );
    }

    @Test
    public void testAssertNotSame_Positive_Equal_Arrays() {
        com.sun.tck.lib.Assert.assertNotSame(
                new TestObject[]{new TestObject(RANDOM_STRING)},
                new TestObject[]{new TestObject(RANDOM_STRING)}
        );
    }

    @Test
    public void testAssertNotSame_Positive_Equal_Arrays_1() {
        TestObject testObject = new TestObject(RANDOM_STRING);
        com.sun.tck.lib.Assert.assertNotSame(
                new TestObject[]{testObject},
                new TestObject[]{testObject}
        );
    }

    @Test
    public void testAssertNotSame() {
        com.sun.tck.lib.Assert.assertNotSame(
                new TestObject("first" + RANDOM_STRING),
                new TestObject("second" + RANDOM_STRING)
        );
    }

    @Test
    public void testAssertNotSame_Positive_exception() {
        com.sun.tck.lib.Assert.assertNotSame(
                new TestObject("first" + RANDOM_STRING),
                new TestObject("second" + RANDOM_STRING), new RuntimeException()
        );
    }

    @Test
    public void testAssertNotSame_Positive_Different_arrays() {
        com.sun.tck.lib.Assert.assertNotSame(
                new TestObject[]{new TestObject("first" + RANDOM_STRING)},
                new TestObject[]{new TestObject("second" + RANDOM_STRING)}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertNotSame_Negative() {
        TestObject testObject = new TestObject(RANDOM_STRING);
        com.sun.tck.lib.Assert.assertNotSame(testObject, testObject);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertNotSame_Negative_exception() {
        TestObject testObject = new TestObject(RANDOM_STRING);
        com.sun.tck.lib.Assert.assertNotSame(testObject, testObject, new RuntimeException());
    }

    @Test
    public void testAssertNotSame_Negative_Message() {
        TestObject testObject = new TestObject(RANDOM_STRING);
        try {
            com.sun.tck.lib.Assert.assertNotSame(testObject, testObject);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected not same : \"" +
                    testObject +
                    "\"", e.getMessage());
        }
    }

    @Test
    public void testAssertNotSame_Negative_exception_1() {
        TestObject testObject = new TestObject(RANDOM_STRING);
        RuntimeException cause = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertNotSame(testObject, testObject, cause);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertSame(cause, e.getCause());
            assertEquals("Expected not same : \"" +
                    testObject +
                    "\"", e.getMessage());
        }
    }

    @Test
    public void testAssertNotSame_Negative_CustomMessage() {
        TestObject testObject = new TestObject(RANDOM_STRING);
        try {
            com.sun.tck.lib.Assert.assertNotSame(testObject, testObject, RANDOM_STRING + "32423434");
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals(RANDOM_STRING + "32423434\nExpected not same : \"" + testObject + "\"", e.getMessage());
        }
    }

    @Test
    public void testAssertNotSame_Negative_CustomMessage_throwable() {
        TestObject testObject = new TestObject(RANDOM_STRING);
        RuntimeException runtimeException = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.assertNotSame(testObject, testObject,
                    RANDOM_STRING + "32423434", runtimeException);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals(RANDOM_STRING + "32423434", e.getMessage());
            assertSame(runtimeException, e.getCause());
        }
    }

    @Test
    public void testAssertNotSame_Negative_Message_arrays() {
        TestObject[] testObject
                = new TestObject[]{new TestObject(RANDOM_STRING)};
        try {
            com.sun.tck.lib.Assert.assertNotSame(testObject, testObject);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals("Expected not same : \"" +
                    Arrays.toString(testObject) +
                    "\"", e.getMessage());
        }
    }

    @Test(expected = AssertionFailedException.class)
    public void testFail() {
        com.sun.tck.lib.Assert.fail(RANDOM_STRING);
    }

    @Test(expected = AssertionFailedException.class)
    public void testFail_exception() {
        com.sun.tck.lib.Assert.fail(RANDOM_STRING, new RuntimeException());
    }

    @Test
    public void testFail_Message() {
        try {
            com.sun.tck.lib.Assert.fail(RANDOM_STRING);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals(RANDOM_STRING, e.getMessage());
        }
    }

    @Test
    public void testFail_Message_exception() {
        RuntimeException runtimeException = new RuntimeException();
        try {
            com.sun.tck.lib.Assert.fail(RANDOM_STRING, runtimeException);
            fail("Exception not thrown");
        } catch (AssertionFailedException e) {
            assertEquals(RANDOM_STRING, e.getMessage());
            assertSame(runtimeException, e.getCause());
        }
    }


    // assertSmartEquals: Common Object tests
    @Test
    public void testAssertSmartEquals_Positive() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new TestObject("object" + RANDOM_STRING),
                new TestObject("object" + RANDOM_STRING)
        );
    }

    @Test
    public void testAssertSmartEquals_Positive_1() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                true,
                true
        );
    }

    @Test
    public void testAssertSmartEquals_Positive_2() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                1.0d,
                1.0d
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Negative() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new TestObject("object" + RANDOM_STRING),
                new TestObject("object1" + RANDOM_STRING)
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Negative_1() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                0.0d,
                0.0f
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Negative_2() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                1,
                1L
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Negative_3() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                1,
                true
        );
    }

    // assertSmartEquals: Object[] tests
    @Test
    public void testAssertSmartEquals_Array_Object_Positive() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new TestObject[]{new TestObject("object" + RANDOM_STRING)},
                new TestObject[]{new TestObject("object" + RANDOM_STRING)}
        );
    }

    @Test
    public void testAssertSmartEquals_Array_Object_Positive_1() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new Object[]{new TestObject("object" + RANDOM_STRING), "123"},
                new Object[]{new TestObject("object" + RANDOM_STRING), "123"}
        );
    }

    @Test
    public void testAssertSmartEquals_Array_Object_3d_Positive() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new TestObject[][]{{new TestObject("object" + RANDOM_STRING)}},
                new TestObject[][]{{new TestObject("object" + RANDOM_STRING)}}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_Object_Negative() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new TestObject[]{new TestObject("object" + RANDOM_STRING)},
                new TestObject[]{new TestObject("object1" + RANDOM_STRING)}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_Object_Negative_1() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new Object[]{new TestObject("object" + RANDOM_STRING), "234"},
                new Object[]{new TestObject("object" + RANDOM_STRING), "123"}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_Object_Negative_2() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new Object[]{new TestObject("object" + RANDOM_STRING)},
                new Object[]{new TestObject("object" + RANDOM_STRING), "123"}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_Object_3d_Negative() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new TestObject[][]{{new TestObject("object" + RANDOM_STRING)}},
                new TestObject[][]{{new TestObject("object1" + RANDOM_STRING)}}
        );
    }

    // assertSmartEquals: double[] tests
    @Test
    public void testAssertSmartEquals_Array_double_Positive() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new double[]{0.0d},
                new double[]{0.0d}
        );
    }

    @Test
    public void testAssertSmartEquals_Array_double_Positive_1() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new double[]{0.0d, 3d},
                new double[]{0.0d, 3d}
        );
    }

    @Test
    public void testAssertSmartEquals_Array_double_3d_Positive() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new double[][]{{0.0d, 3d}, {2d}},
                new double[][]{{0.0d, 3d}, {2d}}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_double_Negative() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new double[]{0.0d, 3d},
                new double[]{1.0d, 3d}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_double_Negative_1() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new double[]{1.0d},
                new double[]{1.0d, 3d}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_double_Negative_2() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new double[]{1.0d, 15d},
                new double[]{1.0d, 3d}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_double_3d_Negative() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new double[][]{{0.0d, 3d}, {2d}},
                new double[][]{{0.0d, 3d}, {1d}}
        );
    }

    // assertSmartEquals: float[] tests
    @Test
    public void testAssertSmartEquals_Array_float_Positive() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new float[]{0.0f},
                new float[]{0.0f}
        );
    }

    @Test
    public void testAssertSmartEquals_Array_float_Positive_1() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new float[]{0.0f, 3f},
                new float[]{0.0f, 3f}
        );
    }

    @Test
    public void testAssertSmartEquals_Array_float_3d_Positive() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new float[][]{{0.0f, 3f}, {2f}},
                new float[][]{{0.0f, 3f}, {2f}}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_float_Negative() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new float[]{0.0f, 3f},
                new float[]{1.0f, 3f}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_float_Negative_1() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new float[]{1.0f},
                new float[]{1.0f, 3f}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_float_Negative_2() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new float[]{1.0f, 15f},
                new float[]{1.0f, 3f}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_float_3d_Negative() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new float[][]{{0.0f, 3f}, {2f}},
                new float[][]{{0.0f, 3f}, {1f}}
        );
    }

    // assertSmartEquals: int[] tests
    @Test
    public void testAssertSmartEquals_Array_int_Positive() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new int[]{0},
                new int[]{0}
        );
    }

    @Test
    public void testAssertSmartEquals_Array_int_Positive_1() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new int[]{0, 3},
                new int[]{0, 3}
        );
    }

    @Test
    public void testAssertSmartEquals_Array_int_3d_Positive() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new int[][]{{0, 3}, {2}},
                new int[][]{{0, 3}, {2}}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_int_Negative() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new int[]{0, 3},
                new int[]{1, 3}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_int_Negative_1() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new int[]{1},
                new int[]{1, 3}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_int_Negative_2() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new int[]{1, 15},
                new int[]{1, 3}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_int_3d_Negative() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new int[][]{{0, 3}, {2}},
                new int[][]{{0, 3}, {1}}
        );
    }

    // assertSmartEquals: long[] tests
    @Test
    public void testAssertSmartEquals_Array_long_Positive() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new long[]{0},
                new long[]{0}
        );
    }

    @Test
    public void testAssertSmartEquals_Array_long_Positive_1() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new long[]{0, 3},
                new long[]{0, 3}
        );
    }

    @Test
    public void testAssertSmartEquals_Array_long_3d_Positive() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new long[][]{{0, 3}, {2}},
                new long[][]{{0, 3}, {2}}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_long_Negative() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new long[]{0, 3},
                new long[]{1, 3}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_long_Negative_1() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new long[]{1},
                new long[]{1, 3}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_long_Negative_2() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new long[]{1, 15},
                new long[]{1, 3}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_long_3d_Negative() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new long[][]{{0, 3}, {2}},
                new long[][]{{0, 3}, {1}}
        );
    }

    // assertSmartEquals: short[] tests
    @Test
    public void testAssertSmartEquals_Array_short_Positive() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new short[]{0},
                new short[]{0}
        );
    }

    @Test
    public void testAssertSmartEquals_Array_short_Positive_1() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new short[]{0, 3},
                new short[]{0, 3}
        );
    }

    @Test
    public void testAssertSmartEquals_Array_short_3d_Positive() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new short[][]{{0, 3}, {2}},
                new short[][]{{0, 3}, {2}}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_short_Negative() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new short[]{0, 3},
                new short[]{1, 3}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_short_Negative_1() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new short[]{1},
                new short[]{1, 3}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_short_Negative_2() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new short[]{1, 15},
                new short[]{1, 3}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_short_3d_Negative() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new short[][]{{0, 3}, {2}},
                new short[][]{{0, 3}, {1}}
        );
    }

    // assertSmartEquals: byte[] tests
    @Test
    public void testAssertSmartEquals_Array_byte_Positive() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new byte[]{0},
                new byte[]{0}
        );
    }

    @Test
    public void testAssertSmartEquals_Array_byte_Positive_1() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new byte[]{0, 3},
                new byte[]{0, 3}
        );
    }

    @Test
    public void testAssertSmartEquals_Array_byte_3d_Positive() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new byte[][]{{0, 3}, {2}},
                new byte[][]{{0, 3}, {2}}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_byte_Negative() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new byte[]{0, 3},
                new byte[]{1, 3}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_byte_Negative_1() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new byte[]{1},
                new byte[]{1, 3}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_byte_Negative_2() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new byte[]{1, 15},
                new byte[]{1, 3}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_byte_3d_Negative() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new byte[][]{{0, 3}, {2}},
                new byte[][]{{0, 3}, {1}}
        );
    }

    // assertSmartEquals: char[] tests
    @Test
    public void testAssertSmartEquals_Array_char_Positive() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new char[]{0},
                new char[]{0}
        );
    }

    @Test
    public void testAssertSmartEquals_Array_char_Positive_1() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new char[]{0, 3},
                new char[]{0, 3}
        );
    }

    @Test
    public void testAssertSmartEquals_Array_char_3d_Positive() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new char[][]{{0, 3}, {2}},
                new char[][]{{0, 3}, {2}}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_char_Negative() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new char[]{0, 3},
                new char[]{1, 3}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_char_Negative_1() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new char[]{1},
                new char[]{1, 3}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_char_Negative_2() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new char[]{1, 15},
                new char[]{1, 3}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_char_3d_Negative() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new char[][]{{0, 3}, {2}},
                new char[][]{{0, 3}, {1}}
        );
    }

    // assertSmartEquals: boolean[] tests
    @Test
    public void testAssertSmartEquals_Array_boolean_Positive() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new boolean[]{true},
                new boolean[]{true}
        );
    }

    @Test
    public void testAssertSmartEquals_Array_boolean_Positive_1() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new boolean[]{true, false},
                new boolean[]{true, false}
        );
    }

    @Test
    public void testAssertSmartEquals_Array_boolean_3d_Positive() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new boolean[][]{{true, false}, {true}},
                new boolean[][]{{true, false}, {true}}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_boolean_Negative() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new boolean[]{false, true},
                new boolean[]{false, false}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_boolean_Negative_1() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new boolean[]{false},
                new boolean[]{false, true}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_boolean_Negative_2() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new boolean[]{true, false},
                new boolean[]{true, true}
        );
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertSmartEquals_Array_boolean_3d_Negative() {
        com.sun.tck.lib.Assert.assertSmartEquals(
                new boolean[][]{{true, false}, {true}},
                new boolean[][]{{true, true}, {true}}
        );
    }


    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_1_neg() {
        com.sun.tck.lib.Assert.assertEquals(Collections.singleton("1"), Collections.singleton("2"));
    }

    @Test
    public void testAssertEquals_Iterable_1_pos() {
        com.sun.tck.lib.Assert.assertEquals(Collections.singleton("1"), Collections.singleton("1"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_2_neg() {
        com.sun.tck.lib.Assert.assertEquals(Collections.singleton("1"), Arrays.asList("2"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_2_pos() {
        com.sun.tck.lib.Assert.assertEquals(Collections.singleton("1"), Arrays.asList("1"));
    }

    @Test
    public void testAssertEquals_Iterable_2_pos_1() {
        com.sun.tck.lib.Assert.assertEquals(Collections.singleton("1"), of("1"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_2_pos_3() {
        com.sun.tck.lib.Assert.assertEquals(Collections.singleton(null), Arrays.asList((String) null));
    }

    @Test
    public void testAssertEquals_Iterable_2_pos_33() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList((String) null), Arrays.asList((String) null));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_2_neg_3() {
        com.sun.tck.lib.Assert.assertEquals(Collections.singleton(null), Arrays.asList(1));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_3_neg() {
        com.sun.tck.lib.Assert.assertEquals(Collections.emptyList(), Collections.singleton(3));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_3_neg_1() {
        com.sun.tck.lib.Assert.assertEquals(Collections.singleton(3), Collections.emptyList());
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_3_neg_3() {
        com.sun.tck.lib.Assert.assertEquals(Collections.emptyList(), Collections.singleton(null));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_3_neg_4() {
        com.sun.tck.lib.Assert.assertEquals(Collections.singleton(null), Collections.emptyList());
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_3_pos() {
        com.sun.tck.lib.Assert.assertEquals(Collections.emptyList(), Collections.emptySet());
    }

    @Test
    public void testAssertEquals_Iterable_3_pos_() {
        com.sun.tck.lib.Assert.assertEquals(Collections.emptyList(), Collections.emptyList());
    }

    @Test
    public void testAssertEquals_Iterable_3_pos_1__() {
        com.sun.tck.lib.Assert.assertEquals(Collections.emptySet(), Collections.emptySet());
    }

    @Test
    public void testAssertEquals_Iterable_3_pos_2() {
        com.sun.tck.lib.Assert.assertEquals(new Vector<>(), Arrays.asList());
    }

    @Test
    public void testAssertEquals_Iterable_3_pos_1() {
        com.sun.tck.lib.Assert.assertEquals(Collections.emptySet(), Collections.emptySet());
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_4_neg() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "2"), of("3", "4"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_4_neg_1() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "2"), of("3", "4"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_4_neg_5() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList(null, "2"), Arrays.asList("3", null));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_4_neg_6() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("2", null), Arrays.asList(null, "4"));
    }

    @Test
    public void testAssertEquals_Iterable_4_pos_() {
        com.sun.tck.lib.Assert.assertEquals(of("1", "2"), of("2", "1"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_4_pos() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "2"), of("2", "1"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_4_pos_1() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList(null, "2"), Arrays.asList("2", null));
    }

    @Test
    public void testAssertEquals_Iterable_4_pos_5() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList(null, "2"), Arrays.asList(null, "2"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_4_neg_3() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "2"), Arrays.asList("2", "2"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_4_neg_8() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", null), Arrays.asList(null, null));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_4_pos_2() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "2"), Arrays.asList("2", "1"));
    }

    @Test
    public void testAssertEquals_Iterable_4_pos_22() {
        com.sun.tck.lib.Assert.assertEquals(new HashSet<>(Arrays.asList("1", "2")), new HashSet<>(Arrays.asList("2", "1")));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_4_pos_3() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "2"), new Vector<String>() {{
            add("2");
            add("1");
        }});
    }

    @Test
    public void testAssertEquals_Iterable_4_pos_3234() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "2"), new Vector<String>() {{
            add("1");
            add("2");
        }});
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_neg_1() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "2"), Arrays.asList("1"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_neg_15() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "2"), Arrays.asList((String) null));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_neg_13() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "2"), Arrays.asList("1", "1"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_neg_14() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "1"), Arrays.asList("1", "2"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_neg_2() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1"), Arrays.asList("1", "2"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_neg_20() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1"), Arrays.asList(null, null, null));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_neg_21() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList(null, null, null), Arrays.asList("1"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_neg_3() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1"), Arrays.asList());
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_neg_4() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList(), Arrays.asList("2"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_neg_5() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "2"), Arrays.asList());
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_neg_6() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList(), Arrays.asList("1", "2"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_pos_5() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "2"), Arrays.asList("2", "1"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_pos_6() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("2", "1"), Arrays.asList("1", "2"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_pos_10() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("2", "1"), Arrays.asList("2", "1", "3"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_pos_87() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("2", "1", "5"), Arrays.asList("2", "1"));
    }

    @Test
    public void testAssertEquals_Iterable_5_pos_11() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("2", "2"), Arrays.asList("2", "2"));
    }

    @Test
    public void testAssertEquals_Iterable_5_pos_16() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList(null, null), Arrays.asList(null, null));
    }

    public void testAssertEquals_Iterable_5_pos_7() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "2"), Arrays.asList("2", "1"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_pos_8() {
        com.sun.tck.lib.Assert.assertEquals(of("2", "1"), Arrays.asList("1", "2"));
    }

    @Test
    public void testAssertEquals_Iterable_5_pos_9() {
        com.sun.tck.lib.Assert.assertEquals(of("2", "1"), new HashSet<>(Arrays.asList("1", "2")));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_neg_7() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "2"), Arrays.asList("1", "2", "4"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_neg_8() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "2", "4"), Arrays.asList("1", "2"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_neg_9() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList(), Arrays.asList("1", "2", "4"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_neg_10() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "2", "4"), Arrays.asList());
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_pos_18() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList(null, null, null), Arrays.asList());
    }


    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_neg_11() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "2"), Arrays.asList("1", "2", "4"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_neg_12() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "2", "4"), Arrays.asList("1", "2"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_neg_17() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "2", "1"), Arrays.asList("1", "1", "1"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_neg_18() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "1", "1"), Arrays.asList("1", "2", "1"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_neg_24() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList(null, null, null), Arrays.asList("3", null, null));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_pos_25() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList(null, "3", null), Arrays.asList("3", null, null));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_neg_25() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("3", "3", null), Arrays.asList("3", null, null));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_5_neg_27() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("3", null, null), Arrays.asList("3", "3", null));
    }


    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_6_neg_1() {
        com.sun.tck.lib.Assert.assertEquals(of("3", "1", "2"), Arrays.asList("1", "2", "4"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_6_pos_2() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("3", "1", "2"), Arrays.asList("1", "2", "4"));
    }

    public void testAssertEquals_Iterable_6_pos_3() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("3", "1", "2"), Arrays.asList("3", "1", "2"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_6_pos_5() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "3", "2"), Arrays.asList("3", "1", "2"));
    }

    @Test
    public void testAssertEquals_Iterable_6_pos_4() {
        List<String> iterable = Arrays.asList("3", "1", "2");
        com.sun.tck.lib.Assert.assertEquals(iterable, iterable);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_6_pos_6() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "3", "2"), Arrays.asList("3", "1", "2"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_6_neg_8() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "3", "2"), Arrays.asList("3", "1", "2", "3"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_6_pos_8() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "3", "2", "3"), Arrays.asList("3", "1", "2", "3"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_7_neg_9() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "3", "2", "3"), Arrays.asList("3", "11", "2", "3"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_7_neg_10() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("3"), Arrays.asList("3", "11", "2", "3"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_7_neg_11() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("3", "11", "2", "3"), Arrays.asList("3"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_7_neg_12() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "2", "3"), Arrays.asList("1", "2", "3", "3"));
    }

    @Test
    public void testAssertEquals_Iterable_7_pos_12() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "2", "3", "4"), Arrays.asList("1", "2", "3", "4"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_7_neg_15() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList(), Arrays.asList("1", "2", "3", "3"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_7_neg_16() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "2", "3", "3"), Arrays.asList());
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_7_neg_17() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList(), Arrays.asList("1", "1", "1", "1"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_7_neg_18() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "1", "1", "1", "1"), Arrays.asList());
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_7_neg_19() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1"), Arrays.asList("1", "1", "1", "1"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_7_neg_26() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList((String) null), Arrays.asList(null, null, null, null));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_7_neg_20() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "1", "1", "1"), Arrays.asList("1"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_7_neg_21() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "1"), Arrays.asList("1", "1", "1", "1"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_7_pos_21() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "1", null, null), Arrays.asList(null, "1", null, "1"));
    }

    @Test
    public void testAssertEquals_Iterable_7_pos_21_() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "1", null, null), Arrays.asList("1", "1", null, null));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_7_neg_27() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "1", null, null), Arrays.asList(null, "1", null, "1", null));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_7_neg_29() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList(null, "1", null, "1", null), Arrays.asList("1", "1", null, null));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_7_pos_31() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList(null, "1", null, "1", null), Arrays.asList("1", "1", null, null, null));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_7_pos_29() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList(null, "1", null, "1", null), Arrays.asList("1", "1", null, null, null));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_7_neg_22() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "1", "1", "1"), Arrays.asList("1", "1"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_7_neg_24() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "1", "1"), Arrays.asList("1", "1", "1", "1"));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Iterable_7_neg_25() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList("1", "1", "1", "1"), Arrays.asList("1", "1", "1"));
    }

    @Test
    public void testAssertEquals_List_1() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList(1, 2, 3), Arrays.asList(1, 2, 3));
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_List_2() {
        com.sun.tck.lib.Assert.assertEquals(Arrays.asList(1, 2, 3), Arrays.asList(1, 3, 2));
    }

    @Test
    public void testAssertEquals_Set_1() {
        Set<Integer> hs = new HashSet<>();
        hs.add(1);
        hs.add(2);
        hs.add(3);

        com.sun.tck.lib.Assert.assertEquals(of(1, 2, 3), hs);
        com.sun.tck.lib.Assert.assertEquals(hs, of(1, 2, 3));

        com.sun.tck.lib.Assert.assertEquals(of(1, 3, 2), hs);
        com.sun.tck.lib.Assert.assertEquals(hs, of(2, 1, 3));
    }

    Set<Object> of(Object... objs) {
        HashSet<Object> objects = new HashSet<>();
        for (Object object : objs) {
            objects.add(object);
        }
        return objects;
    }

    @Test
    public void testAssertEquals_Set_2() {
        Set<Number> hs1 = new HashSet<>();
        hs1.add(1);
        hs1.add(2);
        hs1.add(null);
        hs1.add(null);

        Set<Number> hs2 = new HashSet<>();
        hs2.add(1);
        hs2.add(2);
        hs2.add(null);
        com.sun.tck.lib.Assert.assertEquals(hs1, hs2);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Set_3() {
        Set<String> ts = new TreeSet<>();
        ts.add("one");
        ts.add("ne");
        ts.add("e");

        Set<Number> hs = new HashSet<>();
        hs.add(2);
        hs.add(1);
        hs.add(3);
        com.sun.tck.lib.Assert.assertEquals(hs, ts);
    }

    @Test(expected = AssertionFailedException.class)
    public void testAssertEquals_Set_4() {
        Set<String> ts = new TreeSet<>();
        ts.add("one");
        ts.add("ne");
        ts.add("e");

        Set<Number> hs = new HashSet<>();
        hs.add(2);
        hs.add(1);
        hs.add(3);
        com.sun.tck.lib.Assert.assertEquals(ts, hs);
    }


    @Test(expected = AssertionFailedException.class)
    public void test_assertThrows_not_throws_01() {
        com.sun.tck.lib.Assert.assertThrows(
                Throwable.class,
                () -> {
                });
    }

    @Test(expected = AssertionFailedException.class)
    public void test_assertThrows_not_throws_02() {
        com.sun.tck.lib.Assert.assertThrows(
                RuntimeException.class,
                () -> {
                });
    }


    @Test(expected = AssertionFailedException.class)
    public void test_assertThrows_wrongType_01() {
        com.sun.tck.lib.Assert.assertThrows(
                RuntimeException.class,
                () -> {
                    throw new Throwable();
                });
    }

    @Test(expected = AssertionFailedException.class)
    public void test_assertThrows_wrongType_02() {
        com.sun.tck.lib.Assert.assertThrows(
                IllegalArgumentException.class,
                () -> {
                    throw new IllegalStateException();
                });
    }

    @Test(expected = AssertionFailedException.class)
    public void test_assertThrows_wrongType_03() {
        com.sun.tck.lib.Assert.assertThrows(
                Error.class,
                () -> {
                    throw new IllegalArgumentException();
                });
    }


    @Test
    public void test_assertThrows_OK_01() {
        com.sun.tck.lib.Assert.assertThrows(
                Throwable.class,
                () -> {
                    throw new Throwable();
                });
    }

    @Test
    public void test_assertThrows_OK_02() {
        com.sun.tck.lib.Assert.assertThrows(
                ArithmeticException.class,
                () -> {
                    throw new ArithmeticException();
                });
    }

    @Test
    public void test_assertThrows_OK_subtype_01() {
        com.sun.tck.lib.Assert.assertThrows(
                Throwable.class,
                () -> {
                    throw new RuntimeException();
                });
    }

    @Test
    public void test_assertThrows_OK_subtype_02() {
        com.sun.tck.lib.Assert.assertThrows(
                RuntimeException.class,
                () -> {
                    throw new IllegalArgumentException();
                });
    }


}
