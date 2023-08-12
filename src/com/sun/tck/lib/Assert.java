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

package com.sun.tck.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;


/**
 * This helper class provides methods for checking simple assertions.
 * In case of not satisfying checked assertion <code>AssertionFailedException</code> is thrown.
 * Please note that a framework that executes tests using this helper class must
 * handle thrown exceptions correctly - count them as "regular" failures, not as unexpected exceptions.
 * Collection of methods is inspired by JUnit's <code>org.junit.Assert</code> class.
 * As in JUnit these methods can be used directly:
 * <code>Assert.assertSomething(...)</code>,
 * but they could look better being statically imported:<br/>
 * <pre>
 * import static com.sun.tck.lib.Assert.*;
 *    ...
 *    public void testSomething {
 *        assertSomething(...);
 *    }
 *    ...
 * </pre>
 * @see com.sun.tck.lib.AssertionFailedException
 */
public class Assert {

    /**
     * Asserts that a condition is true.
     * If the condition is false then a test is considered failed.
     * @param condition condition to be checked
     * @param message message that assumed to be helpful in case of failure
     * @throws AssertionFailedException
     */
    public static void assertTrue(boolean condition, String message) {
        assertTrue(condition, message, null);
    }

    public static void assertTrue(boolean condition) {
        assertTrue(condition, "Expected: true, was: false", null);
    }

    public static void assertTrue(boolean condition, String message, Throwable cause) {
        if (!condition) {
            fail(message, cause);
        }
    }

    /**
     * Asserts that the first argument is null.
     * If the object is not null then a test is considered failed.
     * @param object Object to check
     * @param message message that assumed to be helpful in case of failure
     * @throws AssertionFailedException
     */
    public static void assertNull(Object object, String message) {
        assertNull(object, message, null);
    }

    public static void assertNull(Object object) {
        assertNull(object, "Expected: null, was: " + prettyToString(object), null);
    }

    public static void assertNull(Object object, String message, Throwable cause) {
        if (object != null) {
            fail(message, cause);
        }
    }

    /**
     * Asserts that a condition is false.
     * If the condition is true then a test is considered failed.
     * @param condition condition to be checked
     * @param message message that assumed to be helpful in case of failure
     * @throws AssertionFailedException
     */
    public static void assertFalse(boolean condition, String message) {
        assertFalse(condition, message, null);
    }

    public static void assertFalse(boolean condition) {
        assertFalse(condition, "Expected: false, was: true", null);
    }

    public static void assertFalse(boolean condition, String message, Throwable cause) {
        if (condition) {
            fail(message, cause);
        }
    }

    /**
     * Asserts that the first argument is not null.
     * If the object is null then a test is considered failed.
     * @param object Object to check
     * @param message message that assumed to be helpful in case of failure
     * @throws AssertionFailedException
     */
    public static void assertNotNull(Object object, String message) {
        assertNotNull(object, message, null);
    }

    public static void assertNotNull(Object object) {
        assertNotNull(object, "Expected not null, was null", null);
    }





    public static void assertNotNull(Object object, String message, Throwable cause) {
        if (object == null) {
            fail(message, cause);
        }
    }


    /**
     * Asserts that two objects are equal.
     * If the two objects are not equal then a test is considered failed.
     * Two null arguments are considered equal.
     * Non-null arguments are compared with Object.equal(Object) method.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @throws AssertionFailedException
     */
    public static void assertEquals(Object expected, Object actual) {
        assertEquals(expected, actual, createEqualsFailedMessage(expected, actual), null);
    }

    /**
     * Asserts that two objects are equal.
     * If the two objects are not equal then a test is considered failed.
     * Two null arguments are considered equal.
     * Non-null arguments are compared with Object.equal(Object) method.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @param message a message to use
     * @throws AssertionFailedException
     */
    public static void assertEquals(Object expected, Object actual, String message) {
        assertEquals(expected, actual, message + "\n" + createEqualsFailedMessage(expected, actual), null);
    }

    /**
     * Asserts that two objects are equal.
     * If the two objects are not equal then a test is considered failed.
     * Two null arguments are considered equal.
     * Non-null arguments are compared with Object.equal(Object) method.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @param cause the cause for AssertionFailedException
     * @throws AssertionFailedException
     */
    public static void assertEquals(Object expected, Object actual, Throwable cause) {
        assertEquals(expected, actual, createEqualsFailedMessage(expected, actual), cause);
    }

    /**
     * Asserts that two objects are equal.
     * If the two objects are not equal then a test is considered failed.
     * Two null arguments are considered equal.
     * Non-null arguments are compared with Object.equal(Object) method.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @param message a message to use
     * @param cause the cause for AssertionFailedException
     * @throws AssertionFailedException
     */
    public static void assertEquals(Object expected, Object actual, String message, Throwable cause) {
        if (!(expected == null ? actual == null : expected.equals(actual))) {
            fail(message, cause);
        }
    }

    private static String createEqualsFailedMessage(Object expected, Object actual) {
        return "Expected equal to : \"" + prettyToString(expected)
                + "\", was given: \"" + prettyToString(actual) + "\"";
    }

    /**
     * Asserts that two arrays are equal.
     * If the two arrays are not equal then a test is considered failed.
     * Two null arrays are considered equal.
     * Non-null arrays are compared with Arrays.deepEquals() method.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @throws AssertionFailedException
     */
    public static void assertEquals(Object[] expected, Object[] actual) {
        assertEquals(expected, actual, createEqualsFailedMessage(expected, actual), null);
    }

    public static void assertEquals(Object[] expected, Object[] actual, String message) {
        assertEquals(expected, actual, message + "\n" + createEqualsFailedMessage(expected, actual), null);
    }

    public static void assertEquals(Object[] expected, Object[] actual, Throwable cause ) {
        assertEquals(expected, actual, createEqualsFailedMessage(expected, actual), cause);
    }

    public static void assertEquals(Object[] expected, Object[] actual, String message, Throwable cause) {
        if (!(expected == null ? actual == null : Arrays.deepEquals(expected, actual))) {
            fail(message, cause);
        }
    }

    public static void assertEquals(long[] expected, long[] actual) {
        assertEquals(expected, actual, createEqualsFailedMessage(expected, actual), null);
    }

    public static void assertEquals(long[] expected, long[] actual, String message) {
        assertEquals(expected, actual, message + "\n" + createEqualsFailedMessage(expected, actual), null);
    }

    public static void assertEquals(long[] expected, long[] actual, Throwable cause) {
        assertEquals(expected, actual, createEqualsFailedMessage(expected, actual), cause);
    }

    public static void assertEquals(long[] expected, long[] actual, String message, Throwable cause) {
        if (!(expected == null ? actual == null : Arrays.equals(expected, actual))) {
            fail(message, cause);
        }
    }

    public static void assertEquals(int[] expected, int[] actual) {
        assertEquals(expected, actual, createEqualsFailedMessage(expected, actual), null);
    }

    public static void assertEquals(int[] expected, int[] actual, String message) {
        assertEquals(expected, actual, message + "\n" + createEqualsFailedMessage(expected, actual), null);
    }

    public static void assertEquals(int[] expected, int[] actual, Throwable cause) {
        assertEquals(expected, actual, createEqualsFailedMessage(expected, actual), cause);
    }

    public static void assertEquals(int[] expected, int[] actual, String message, Throwable cause) {
        if (!(expected == null ? actual == null : Arrays.equals(expected, actual))) {
            fail(message, cause);
        }
    }

    public static void assertEquals(short[] expected, short[] actual) {
        assertEquals(expected, actual, createEqualsFailedMessage(expected, actual), null);
    }

    public static void assertEquals(short[] expected, short[] actual, String message) {
        assertEquals(expected, actual, message + "\n" + createEqualsFailedMessage(expected, actual), null);
    }

    public static void assertEquals(short[] expected, short[] actual, Throwable cause ) {
        assertEquals(expected, actual, createEqualsFailedMessage(expected, actual), cause);
    }

    public static void assertEquals(short[] expected, short[] actual, String message, Throwable cause) {
        if (!(expected == null ? actual == null : Arrays.equals(expected, actual))) {
            fail(message, cause);
        }
    }

    public static void assertEquals(char[] expected, char[] actual) {
        assertEquals(expected, actual, createEqualsFailedMessage(expected, actual), null);
    }

    public static void assertEquals(char[] expected, char[] actual, String message) {
        assertEquals(expected, actual, message + "\n" + createEqualsFailedMessage(expected, actual), null);
    }

    public static void assertEquals(char[] expected, char[] actual, Throwable cause) {
        assertEquals(expected, actual, createEqualsFailedMessage(expected, actual), cause);
    }

    public static void assertEquals(char[] expected, char[] actual, String message, Throwable cause) {
        if (!(expected == null ? actual == null : Arrays.equals(expected, actual))) {
            fail(message, cause);
        }
    }

    public static void assertEquals(byte[] expected, byte[] actual) {
        assertEquals(expected, actual, createEqualsFailedMessage(expected, actual), null);
    }

    public static void assertEquals(byte[] expected, byte[] actual, String message) {
        assertEquals(expected, actual, message + "\n" + createEqualsFailedMessage(expected, actual), null);
    }

    public static void assertEquals(byte[] expected, byte[] actual, Throwable cause) {
        assertEquals(expected, actual, createEqualsFailedMessage(expected, actual), cause);
    }

    public static void assertEquals(byte[] expected, byte[] actual, String message, Throwable cause) {
        if (!(expected == null ? actual == null : Arrays.equals(expected, actual))) {
            fail(message, cause);
        }
    }

    public static void assertEquals(boolean[] expected, boolean[] actual) {
        assertEquals(expected, actual, createEqualsFailedMessage(expected, actual), null);
    }

    public static void assertEquals(boolean[] expected, boolean[] actual, String message) {
        assertEquals(expected, actual, message + "\n" + createEqualsFailedMessage(expected, actual), null);
    }

    public static void assertEquals(boolean[] expected, boolean[] actual, Throwable cause) {
        assertEquals(expected, actual, createEqualsFailedMessage(expected, actual), cause);
    }

    public static void assertEquals(boolean[] expected, boolean[] actual, String message, Throwable cause) {
        if (!(expected == null ? actual == null : Arrays.equals(expected, actual))) {
            fail(message, cause);
        }
    }

    public static void assertEquals(float[] expected, float[] actual) {
        assertEquals(expected, actual, createEqualsFailedMessage(expected, actual), null);
    }

    public static void assertEquals(float[] expected, float[] actual, String message) {
        assertEquals(expected, actual, message + "\n" + createEqualsFailedMessage(expected, actual), null);
    }

    public static void assertEquals(float[] expected, float[] actual, Throwable cause) {
        assertEquals(expected, actual, createEqualsFailedMessage(expected, actual), cause);
    }

    public static void assertEquals(float[] expected, float[] actual, String message, Throwable cause) {
        if (!(expected == null ? actual == null : Arrays.equals(expected, actual))) {
            fail(message, cause);
        }
    }

    public static void assertEquals(double[] expected, double[] actual) {
        assertEquals(expected, actual, createEqualsFailedMessage(expected, actual), null);
    }

    public static void assertEquals(double[] expected, double[] actual, String message) {
        assertEquals(expected, actual, message + "\n" + createEqualsFailedMessage(expected, actual), null);
    }

    public static void assertEquals(double[] expected, double[] actual, Throwable cause) {
        assertEquals(expected, actual, createEqualsFailedMessage(expected, actual), cause);
    }

    public static void assertEquals(double[] expected, double[] actual, String message, Throwable cause) {
        if (!(expected == null ? actual == null : Arrays.equals(expected, actual))) {
            fail(message, cause);
        }
    }

    /**
     * Asserts that two objects are not equal.
     * If the two objects are equal then a test is considered failed.
     * Two null arguments are considered equal.
     * Non-null arguments are compared with Object.equal(Object) method.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @throws AssertionFailedException
     */
    public static void assertNotEquals(Object expected, Object actual) {
        assertNotEquals(expected, actual, createUnequalsFailedMessage(expected, actual), null);
    }

    public static void assertNotEquals(Object expected, Object actual, String message) {
        assertNotEquals(expected, actual, message + "\n" + createUnequalsFailedMessage(expected, actual), null);
    }

    public static void assertNotEquals(Object expected, Object actual, Throwable cause) {
        assertNotEquals(expected, actual, createUnequalsFailedMessage(expected, actual), cause);
    }

    public static void assertNotEquals(Object expected, Object actual, String message, Throwable cause) {
        if (expected == null ? actual == null : expected.equals(actual)) {
            fail(message, cause);
        }
    }

    private static String createUnequalsFailedMessage(Object expected, Object actual) {
        return "Expected unequal objects, were equal: \"" + prettyToString(expected)
                + "\", \"" + prettyToString(actual) + "\"";
    }

    /**
     * Asserts that two arrays are not equal.
     * If the two arrays are equal then a test is considered failed.
     * Two null arrays are considered equal.
     * Non-null arrays are compared with Object.equal(Object) method.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @throws AssertionFailedException
     */
    public static void assertNotEquals(Object[] expected, Object[] actual) {
        assertNotEquals(expected, actual, createUnequalsFailedMessage(expected, actual), null);
    }

    public static void assertNotEquals(Object[] expected, Object[] actual, String message) {
        assertNotEquals(expected, actual, message + "\n" + createUnequalsFailedMessage(expected, actual), null);
    }

    public static void assertNotEquals(Object[] expected, Object[] actual, Throwable cause) {
        assertNotEquals(expected, actual, createUnequalsFailedMessage(expected, actual), cause);
    }

    public static void assertNotEquals(Object[] expected, Object[] actual, String message, Throwable cause) {
        if (expected == null ? actual == null : Arrays.deepEquals(expected, actual)) {
            fail(message, cause);
        }
    }

    public static void assertNotEquals(long[] expected, long[] actual) {
        assertNotEquals(expected, actual, createUnequalsFailedMessage(expected, actual), null);
    }

    public static void assertNotEquals(long[] expected, long[] actual, String message) {
        assertNotEquals(expected, actual, message + "\n" + createUnequalsFailedMessage(expected, actual), null);
    }

    public static void assertNotEquals(long[] expected, long[] actual, Throwable cause) {
        assertNotEquals(expected, actual, createUnequalsFailedMessage(expected, actual), cause);
    }

    public static void assertNotEquals(long[] expected, long[] actual, String message, Throwable cause) {
        if (expected == null ? actual == null : Arrays.equals(expected, actual)) {
            fail(message, cause);
        }
    }

    public static void assertNotEquals(int[] expected, int[] actual) {
        assertNotEquals(expected, actual, createUnequalsFailedMessage(expected, actual), null);
    }

    public static void assertNotEquals(int[] expected, int[] actual, String message) {
        assertNotEquals(expected, actual, message + "\n" + createUnequalsFailedMessage(expected, actual), null);
    }

    public static void assertNotEquals(int[] expected, int[] actual, Throwable cause) {
        assertNotEquals(expected, actual, createUnequalsFailedMessage(expected, actual), cause);
    }

    public static void assertNotEquals(int[] expected, int[] actual, String message, Throwable cause) {
        if (expected == null ? actual == null : Arrays.equals(expected, actual)) {
            fail(message, cause);
        }
    }

    public static void assertNotEquals(short[] expected, short[] actual) {
        assertNotEquals(expected, actual, createUnequalsFailedMessage(expected, actual), null);
    }

    public static void assertNotEquals(short[] expected, short[] actual, String message) {
        assertNotEquals(expected, actual, message + "\n" + createUnequalsFailedMessage(expected, actual), null);
    }

    public static void assertNotEquals(short[] expected, short[] actual, Throwable cause) {
        assertNotEquals(expected, actual, createUnequalsFailedMessage(expected, actual), cause);
    }

    public static void assertNotEquals(short[] expected, short[] actual, String message, Throwable cause) {
        if (expected == null ? actual == null : Arrays.equals(expected, actual)) {
            fail(message, cause);
        }
    }

    public static void assertNotEquals(char[] expected, char[] actual) {
        assertNotEquals(expected, actual, createUnequalsFailedMessage(expected, actual), null);
    }

    public static void assertNotEquals(char[] expected, char[] actual, String message) {
        assertNotEquals(expected, actual, message + "\n" + createUnequalsFailedMessage(expected, actual), null);
    }

    public static void assertNotEquals(char[] expected, char[] actual, Throwable cause) {
        assertNotEquals(expected, actual, createUnequalsFailedMessage(expected, actual), cause);
    }

    public static void assertNotEquals(char[] expected, char[] actual, String message, Throwable cause) {
        if (expected == null ? actual == null : Arrays.equals(expected, actual)) {
            fail(message, cause);
        }
    }

    public static void assertNotEquals(byte[] expected, byte[] actual) {
        assertNotEquals(expected, actual, createUnequalsFailedMessage(expected, actual), null);
    }

    public static void assertNotEquals(byte[] expected, byte[] actual, String message) {
        assertNotEquals(expected, actual, message + "\n" + createUnequalsFailedMessage(expected, actual), null);
    }

    public static void assertNotEquals(byte[] expected, byte[] actual, Throwable cause) {
        assertNotEquals(expected, actual, createUnequalsFailedMessage(expected, actual), cause);
    }

    public static void assertNotEquals(byte[] expected, byte[] actual, String message, Throwable cause) {
        if (expected == null ? actual == null : Arrays.equals(expected, actual)) {
            fail(message, cause);
        }
    }

    public static void assertNotEquals(boolean[] expected, boolean[] actual) {
        assertNotEquals(expected, actual, createUnequalsFailedMessage(expected, actual), null);
    }

    public static void assertNotEquals(boolean[] expected, boolean[] actual, String message) {
        assertNotEquals(expected, actual, message + "\n" + createUnequalsFailedMessage(expected, actual), null);
    }

    public static void assertNotEquals(boolean[] expected, boolean[] actual, Throwable cause) {
        assertNotEquals(expected, actual, createUnequalsFailedMessage(expected, actual), cause);
    }

    public static void assertNotEquals(boolean[] expected, boolean[] actual, String message, Throwable cause) {
        if (expected == null ? actual == null : Arrays.equals(expected, actual)) {
            fail(message, cause);
        }
    }

    public static void assertNotEquals(float[] expected, float[] actual) {
        assertNotEquals(expected, actual, createUnequalsFailedMessage(expected, actual), null);
    }

    public static void assertNotEquals(float[] expected, float[] actual, String message) {
        assertNotEquals(expected, actual, message + "\n" + createUnequalsFailedMessage(expected, actual), null);
    }

    public static void assertNotEquals(float[] expected, float[] actual, Throwable cause) {
        assertNotEquals(expected, actual, createUnequalsFailedMessage(expected, actual), cause);
    }

    public static void assertNotEquals(float[] expected, float[] actual, String message, Throwable cause) {
        if (expected == null ? actual == null : Arrays.equals(expected, actual)) {
            fail(message, cause);
        }
    }

    public static void assertNotEquals(double[] expected, double[] actual) {
        assertNotEquals(expected, actual, createUnequalsFailedMessage(expected, actual), null);
    }

    public static void assertNotEquals(double[] expected, double[] actual, String message) {
        assertNotEquals(expected, actual, message + "\n" + createUnequalsFailedMessage(expected, actual), null);
    }

    public static void assertNotEquals(double[] expected, double[] actual, Throwable cause) {
        assertNotEquals(expected, actual, createUnequalsFailedMessage(expected, actual), cause);
    }

    public static void assertNotEquals(double[] expected, double[] actual, String message, Throwable cause) {
        if (expected == null ? actual == null : Arrays.equals(expected, actual)) {
            fail(message, cause);
        }
    }

    /**
     * Asserts that two objects refer to the same object.
     * Two null arguments are considered same.
     * @param expected the expected object
     * @param actual the object to compare to <code>expected</code>
     * @throws AssertionFailedException
     */
    public static void assertSame(Object expected, Object actual) {
        assertSame(expected, actual, createAssertSameFailed(expected, actual), null);
    }

    public static void assertSame(Object expected, Object actual, String message) {
        assertSame(expected, actual, message + "\n" + createAssertSameFailed(expected, actual), null);
    }

    public static void assertSame(Object expected, Object actual, Throwable cause) {
        assertSame(expected, actual, createAssertSameFailed(expected, actual), cause);
    }

    public static void assertSame(Object expected, Object actual, String message, Throwable cause) {
        if (expected != actual) {
            fail(message, cause);
        }
    }

    private static String createAssertSameFailed(Object expected, Object actual) {
        return "Expected same as : \"" + prettyToString(expected) + "\", was given: \"" + prettyToString(actual) + "\"";
    }

    /**
     * Asserts that two objects do not refer to the same object.
     * Two null arguments are considered same.
     * @param expected the expected object
     * @param actual the object to compare to <code>expected</code>
     * @throws AssertionFailedException
     */
    public static void assertNotSame(Object expected, Object actual) {
        assertNotSame(expected, actual, assertNotSameFailedMessage(expected), null);
    }

    public static void assertNotSame(Object expected, Object actual, String message) {
        assertNotSame(expected, actual, message + "\n" + assertNotSameFailedMessage(expected), null);
    }

    public static void assertNotSame(Object expected, Object actual, Throwable cause) {
        assertNotSame(expected, actual, assertNotSameFailedMessage(expected), cause);
    }

    public static void assertNotSame(Object expected, Object actual, String message, Throwable cause) {
        if (expected == actual) {
            fail(message, cause);
        }
    }

    private static String assertNotSameFailedMessage(Object expected) {
        return "Expected not same : \"" + prettyToString(expected) + "\"";
    }

    /**
     * Initiates test failure by throwing exception with no specific message.
     * @throws AssertionFailedException
     */
    public static void fail() {
        fail("A general failure");
    }

    /**
     * Initiates test failure by throwing exception.
     * @param message message that will be wrapped with exception
     * @throws AssertionFailedException
     */
    public static void fail(String message) {
        throw new AssertionFailedException(message);
    }

    public static void fail(String message, Throwable cause) {
        throw new AssertionFailedException(message, cause);
    }

    private static String prettyToString(Object object) {
        if (object instanceof Object[]) {
            return Arrays.deepToString((Object[])object);
        } else if (object instanceof long[]) {
            return Arrays.toString((long[])object);
        } else if (object instanceof int[]) {
            return Arrays.toString((int[])object);
        } else if (object instanceof short[]) {
            return Arrays.toString((short[])object);
        } else if (object instanceof char[]) {
            return Arrays.toString((char[])object);
        } else if (object instanceof byte[]) {
            return Arrays.toString((byte[])object);
        } else if (object instanceof boolean[]) {
            return Arrays.toString((boolean[])object);
        } else if (object instanceof float[]) {
            return Arrays.toString((float[])object);
        } else if (object instanceof double[]) {
            return Arrays.toString((double[])object);
        } else {
            return String.valueOf(object);
        }
    }

    /**
     * Reports that testcase is not applicable.
     */
    public static void reportNotApplicable() {
        throw new NotApplicableException();
    }

    /**
     * Reports that testcase is not applicable if passed condition is true.
     */
    public static void reportNotApplicable(boolean ifCondition) {
        if (ifCondition) {
            throw new NotApplicableException();
        }
    }

    /**
     * Reports that testcase is not applicable due to specified reason.
     */
    public static void reportNotApplicable(String reason) {
        throw new NotApplicableException(reason);
    }

    /**
     * Reports that testcase is not applicable
     * due to specified reason if passed condition is true.
     */
    public static void reportNotApplicable(boolean ifCondition, String reason) {
        if (ifCondition) {
            throw new NotApplicableException(reason);
        }
    }

    /**
     * Asserts that two primitive float values are equal.
     * If the two values are not equal then a test is considered failed.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @throws AssertionFailedException if values are not equal
     */
    public static void assertEquals(float expected, float actual) {
        if (!Float.valueOf(expected).equals(Float.valueOf(actual))) {
            throw new AssertionFailedException(createEqualsFailedMessage(expected, actual), null);
        }
    }

    /**
     * Asserts that two primitive float values are equal.
     * Provided error message is used in the output.
     * If the two values are not equal then a test is considered failed.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @throws AssertionFailedException if values are not equal
     */
    public static void assertEquals(float expected, float actual, String message) {
        if (!Float.valueOf(expected).equals(Float.valueOf(actual))) {
            throw new AssertionFailedException(message + "\n" + createEqualsFailedMessage(expected, actual), null);
        }
    }

    /**
     * Asserts that two primitive float values are equal.
     * If the two values are not equal then a test is considered failed.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @param message a message to use
     * @param cause the cause for AssertionFailedException
     * @throws AssertionFailedException if values are not equal
     */
    public static void assertEquals(float expected, float actual, String message, Throwable cause) {
        if (!Float.valueOf(expected).equals(Float.valueOf(actual))) {
            throw new AssertionFailedException(message, cause);
        }
    }

    /**
     * Asserts that two primitive short values are equal.
     * If the two values are not equal then a test is considered failed.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @throws AssertionFailedException if values are not equal
     */
    public static void assertEquals(short expected, short actual) {
        if (expected != actual) {
            throw new AssertionFailedException(createEqualsFailedMessage(expected, actual), null);
        }
    }

    /**
     * Asserts that two primitive short values are equal.
     * Provided error message is used in the output.
     * If the two values are not equal then a test is considered failed.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @param message a message to use
     * @throws AssertionFailedException if values are not equal
     */
    public static void assertEquals(short expected, short actual, String message) {
        if (expected != actual) {
            throw new AssertionFailedException(message + "\n" + createEqualsFailedMessage(expected, actual), null);
        }
    }

    /**
     * Asserts that two primitive short values are equal.
     * If the two values are not equal then a test is considered failed.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @param message a message to use
     * @param cause the cause for AssertionFailedException
     * @throws AssertionFailedException if values are not equal
     */
    public static void assertEquals(short expected, short actual, String message, Throwable cause) {
        if (expected != actual) {
            throw new AssertionFailedException(message, cause);
        }
    }

    /**
     * Asserts that two primitive int values are equal.
     * If the two values are not equal then a test is considered failed.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @throws AssertionFailedException if values are not equal
     */
    public static void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionFailedException(createEqualsFailedMessage(expected, actual), null);
        }
    }

    /**
     * Asserts that two primitive int values are equal.
     * Provided error message is used in the output.
     * If the two values are not equal then a test is considered failed.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @param message a message to use
     * @throws AssertionFailedException if values are not equal
     */
    public static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionFailedException(message + "\n" + createEqualsFailedMessage(expected, actual), null);
        }
    }

    /**
     * Asserts that two primitive int values are equal.
     * If the two values are not equal then a test is considered failed.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @param message a message to use
     * @param cause the cause for AssertionFailedException
     * @throws AssertionFailedException if values are not equal
     */
    public static void assertEquals(int expected, int actual, String message, Throwable cause) {
        if (expected != actual) {
            throw new AssertionFailedException(message, cause);
        }
    }

    /**
     * Asserts that two primitive byte values are equal.
     * If the two values are not equal then a test is considered failed.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @throws AssertionFailedException if values are not equal
     */
    public static void assertEquals(byte expected, byte actual) {
        if (expected != actual) {
            throw new AssertionFailedException(createEqualsFailedMessage(expected, actual), null);
        }
    }

    /**
     * Asserts that two primitive byte values are equal.
     * Provided error message is used in the output.
     * If the two values are not equal then a test is considered failed.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @param message a message to use
     * @throws AssertionFailedException if values are not equal
     */
    public static void assertEquals(byte expected, byte actual, String message) {
        if (expected != actual) {
            throw new AssertionFailedException(message + "\n" + createEqualsFailedMessage(expected, actual), null);
        }
    }

    /**
     * Asserts that two primitive byte values are equal.
     * If the two values are not equal then a test is considered failed.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @param message a message to use
     * @param cause the cause for AssertionFailedException
     * @throws AssertionFailedException if values are not equal
     */
    public static void assertEquals(byte expected, byte actual, String message, Throwable cause) {
        if (expected != actual) {
            throw new AssertionFailedException(message, cause);
        }
    }

    /**
     * Asserts that two primitive char values are equal.
     * If the two values are not equal then a test is considered failed.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @throws AssertionFailedException if values are not equal
     */
    public static void assertEquals(char expected, char actual) {
        if (expected != actual) {
            throw new AssertionFailedException(createEqualsFailedMessage(expected, actual), null);
        }
    }

    /**
     * Asserts that two primitive char values are equal.
     * Provided error message is used in the output.
     * If the two values are not equal then a test is considered failed.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @param message a message to use
     * @throws AssertionFailedException if values are not equal
     */
    public static void assertEquals(char expected, char actual, String message) {
        if (expected != actual) {
            throw new AssertionFailedException(message + "\n" + createEqualsFailedMessage(expected, actual), null);
        }
    }

    /**
     * Asserts that two primitive char values are equal.
     * If the two values are not equal then a test is considered failed.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @param message a message to use
     * @throws AssertionFailedException if values are not equal
     */
    public static void assertEquals(char expected, char actual, String message, Throwable cause) {
        if (expected != actual) {
            throw new AssertionFailedException(message, cause);
        }
    }

    /**
     * Asserts that two primitive boolean values are equal.
     * If the two values are not equal then a test is considered failed.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @throws AssertionFailedException if values are not equal
     */
    public static void assertEquals(boolean expected, boolean actual) {
        if (expected != actual) {
            throw new AssertionFailedException(createEqualsFailedMessage(expected, actual), null);
        }
    }

    /**
     * Asserts that two primitive boolean values are equal.
     * Provided error message is used in the output.
     * If the two values are not equal then a test is considered failed.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @param message a message to use
     * @throws AssertionFailedException if values are not equal
     */
    public static void assertEquals(boolean expected, boolean actual, String message) {
        if (expected != actual) {
            throw new AssertionFailedException(message + "\n" + createEqualsFailedMessage(expected, actual), null);
        }
    }

    /**
     * Asserts that two primitive boolean values are equal.
     * Provided error message is used in the output.
     * If the two values are not equal then a test is considered failed.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @param message a message to use
     * @param cause the cause for AssertionFailedException
     * @throws AssertionFailedException if values are not equal
     */
    public static void assertEquals(boolean expected, boolean actual, String message, Throwable cause) {
        if (expected != actual) {
            throw new AssertionFailedException(message, cause);
        }
    }

    /**
     * Asserts that two primitive double values are equal.
     * If the two values are not equal then a test is considered failed.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @throws AssertionFailedException if values are not equal
     */
    public static void assertEquals(double expected, double actual) {
        if (!Double.valueOf(expected).equals(Double.valueOf(actual))) {
            throw new AssertionFailedException(createEqualsFailedMessage(expected, actual), null);
        }
    }

    /**
     * Asserts that two primitive double values are equal. Provided error message is used in the output.
     * If the two values are not equal then a test is considered failed.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @param message a message to use
     * @throws AssertionFailedException if values are not equal
     */
    public static void assertEquals(double expected, double actual, String message) {
        if (!Double.valueOf(expected).equals(Double.valueOf(actual))) {
            throw new AssertionFailedException(message + "\n" + createEqualsFailedMessage(expected, actual), null);
        }
    }

    /**
     * Asserts that two primitive double values are equal. Provided error message is used in the output.
     * If the two values are not equal then a test is considered failed.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @param message a message to use
     * @param cause the cause for AssertionFailedException
     * @throws AssertionFailedException if values are not equal
     */
    public static void assertEquals(double expected, double actual, String message, Throwable cause) {
        if (!Double.valueOf(expected).equals(Double.valueOf(actual))) {
            throw new AssertionFailedException(message, cause);
        }
    }

    /**
     * Asserts that two primitive long values are equal.
     * If the two values are not equal then a test is considered failed.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @throws AssertionFailedException if values are not equal
     */
    public static void assertEquals(long expected, long actual) {
        if (expected != actual) {
            throw new AssertionFailedException(createEqualsFailedMessage(expected, actual), null);
        }
    }

    /**
     * Asserts that two primitive long values are equal.
     * If the two values are not equal then a test is considered failed.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @param message a message to use
     * @throws AssertionFailedException if values are not equal
     */
    public static void assertEquals(long expected, long actual, String message) {
        if (expected != actual) {
            throw new AssertionFailedException(message + "\n" + createEqualsFailedMessage(expected, actual), null);
        }
    }

    /**
     * Asserts that two primitive long values are equal.
     * If the two values are not equal then a test is considered failed.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @param message a message to use
     * @param cause the cause for AssertionFailedException
     * @throws AssertionFailedException if values are not equal
     */
    public static void assertEquals(long expected, long actual, String message, Throwable cause) {
        if (expected != actual) {
            throw new AssertionFailedException(message, cause);
        }
    }

    /**
     * Asserts that two objects are equal. Uses different equality comparison methods depending on object type.
     * It compares arrays element by element, compares primitive types by their special methods, etc.
     * Compares arrays by containing
     * If the two objects are not equal then a test is considered failed.
     * Two null arguments are considered equal.
     * Non-null arguments are compared with Object.equal(Object) method.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @throws AssertionFailedException
     */
    public static void assertSmartEquals(Object expected, Object actual) {
        assertSmartEquals(expected, actual, createEqualsFailedMessage(expected, actual), null);
    }

    /**
     * Asserts that two objects are equal. Uses different equality comparison methods depending on object type.
     * It compares arrays element by element, compares primitive types by their special methods, etc.
     * Compares arrays by containing
     * If the two objects are not equal then a test is considered failed.
     * Two null arguments are considered equal.
     * Non-null arguments are compared with Object.equal(Object) method.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @param message a message to use
     * @throws AssertionFailedException
     */
    public static void assertSmartEquals(Object expected, Object actual, String message) {
        assertSmartEquals(expected, actual, message + "\n" + createEqualsFailedMessage(expected, actual), null);
    }

    /**
     * Asserts that two objects are equal. Uses different equality comparison methods depending on object type.
     * It compares arrays element by element, compares primitive types by their special methods, etc.
     * Compares arrays by containing
     * If the two objects are not equal then a test is considered failed.
     * Two null arguments are considered equal.
     * Non-null arguments are compared with Object.equal(Object) method.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @param cause the cause for AssertionFailedException
     * @throws AssertionFailedException
     */
    public static void assertSmartEquals(Object expected, Object actual, Throwable cause) {
        assertSmartEquals(expected, actual, createEqualsFailedMessage(expected, actual), cause);
    }

    /**
     * Asserts that two objects are equal. Uses different equality comparison methods depending on object type.
     * It compares arrays element by element, compares primitive types by their special methods, etc.
     * Compares arrays by containing
     * If the two objects are not equal then a test is considered failed
     * Two null arguments are considered equal.
     * Non-null arguments are compared with Object.equal(Object) method.
     * @param expected expected value
     * @param actual the value to check against <code>expected</code>
     * @param message a message to use
     * @param cause the cause for AssertionFailedException
     * @throws AssertionFailedException
     */
    public static void assertSmartEquals(Object expected, Object actual, String message, Throwable cause) {
        if(expected == null && actual == null){
            return;
        }

        if(expected instanceof double[] && actual instanceof double[]){
            assertEquals((double[])expected, (double[])actual, message, cause);
            return;
        }

        if(expected instanceof float[] && actual instanceof float[]){
            assertEquals((float[])expected, (float[])actual, message, cause);
            return;
        }

        if(expected instanceof long[] && actual instanceof long[]){
            assertEquals((long[])expected, (long[])actual, message, cause);
            return;
        }

        if(expected instanceof int[] && actual instanceof int[]){
            assertEquals((int[])expected, (int[])actual, message, cause);
            return;
        }

        if(expected instanceof short[] && actual instanceof short[]){
            assertEquals((short[])expected, (short[])actual, message, cause);
            return;
        }

        if(expected instanceof byte[] && actual instanceof byte[]){
            assertEquals((byte[])expected, (byte[])actual, message, cause);
            return;
        }

        if(expected instanceof char[] && actual instanceof char[]){
            assertEquals((char[])expected, (char[])actual, message, cause);
            return;
        }

        if(expected instanceof boolean[] && actual instanceof boolean[]){
            assertEquals((boolean[])expected, (boolean[])actual, message, cause);
            return;
        }

        if(expected instanceof Object[] && actual instanceof Object[]){
            assertEquals((Object[])expected, (Object[])actual, message, cause);
            return;
        }
        assertEquals(expected, actual, message, cause);
    }

    /**
     * Asserts that two <code>List</code> instances are equal
     * by comparing their elements taking the order of elements into account.
     * @param expectedList expected list
     * @param actualList list to check against the expected
     * @throws AssertionFailedException if lists are not equal
     */
    public static void assertEquals(List<?> expectedList, List<?> actualList) {
        ListIterator<?> expectedIterator = expectedList.listIterator();
        ListIterator<?> actualIterator = actualList.listIterator();
        Assert.assertEquals(expectedList.size(), actualList.size(),
                "Sizes of expected and actual lists differ - size() returned different values");
        while (expectedIterator.hasNext() && actualIterator.hasNext()) {
            Object expected = expectedIterator.next();
            Object actual = actualIterator.next();
            Assert.assertEquals(expected, actual,
                    "Expected element " + expected + " does not exist at the expected position in the actual list: " + actualList);
        }
        Assert.assertFalse(expectedIterator.hasNext() ,
                "According to instance returned by listIterator() passed list instances is shorter than expected");
        Assert.assertFalse(actualIterator.hasNext() ,
                "According to instance returned by listIterator() passed list instances is longer than expected");
    }

    /**
     * Asserts that two <code>Set</code> instances are equal
     * by comparing their elements NOT taking the order of elements into account.
     * Besides size checking method Set.contains() is used to verify that every element
     * from the expected set is present in the checked set
     * @param expectedSet expected set
     * @param actualSet set to check against the expected
     * @throws AssertionFailedException if sets are not equal
     */
    public static void assertEquals(Set<?> expectedSet, Set<?> actualSet) {
        Assert.assertEquals(expectedSet.size(), actualSet.size(),
                "Sizes of expected and actual sets differ - size() returned different values");
        for (Object expectedElement : expectedSet) {
            try {
                Assert.assertTrue(actualSet.contains(expectedElement),
                        "Expected element " + expectedElement + " does not exist in actual set: " + actualSet);
            } catch (ClassCastException | NullPointerException e) {
                // method contains could throw
                // ClassCastException - if the type of the specified element is incompatible with this set (optional)
                // NullPointerException - if the specified element is null and this set does not permit null elements (optional)
                Assert.fail("Expected element " + expectedElement + " does not exist in actual set: " + actualSet);
            }
        }
    }

    /**
     * A functional interface allowing to wrap any action
     * which could throw any type of exception.
     */
    @FunctionalInterface
    public interface Thrower {
        void throwThrowable() throws Throwable;
    }

    /**
     * Invokes the specified function and checks that it throws one of the expected exceptions
     */
    public static void assertThrows(Thrower thrower, Class<?>... expectedExceptions) {
        if (expectedExceptions.length == 0) {
            fail("Please specify exception(s) that are expected to be thrown");
        }
        try {
            thrower.throwThrowable();
        } catch (Throwable t) {
            for (Class<?> expected : expectedExceptions) {
                if (expected.isAssignableFrom(t.getClass())) {
                    return; // OK
                }
            }
            List<String> names = new ArrayList<>();
            for (Class<?> expected : expectedExceptions) {
                names.add(expected.getName());
            }
            fail("None of the expected " + names + " was thrown. "
                    + t.getClass().getName() + " was thrown instead", t);
        }
        List<String> names = new ArrayList<>();
        for (Class<?> expected : expectedExceptions) {
            names.add(expected.getName());
        }
        fail("None of the expected " + names + " was thrown");
    }

    /**
     * Invokes the specified function and checks that it throws the expected exception instance
     */
    public static void assertThrows(Thrower thrower, Throwable expectedExceptionInstance) {
        try {
            thrower.throwThrowable();
            fail("The expected throwable " + expectedExceptionInstance + " was not thrown");
        } catch (Throwable t) {
            assertSame(expectedExceptionInstance, t);
        }
    }

}
