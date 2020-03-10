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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Hashtable;

import com.sun.javatest.TestDescription;
import com.sun.javatest.util.DynamicArray;
import org.junit.Assert;
import org.junit.Test;

public class DynamicArrayTest {

    private PrintStream out = System.out;

    @Test
    public void test() {

        boolean result = true;

        if (!runAppendOps()) {
            out.println("Append operation tests FAILED.");
            result = false;
        }

        if (!runEndpoints()) {
            out.println("Endpoint test FAILED.");
            result = false;
        }

        if (!runOddOps()) {
            out.println("Odd length operation test FAILED.");
            result = false;
        }

        if (!runEvenOps()) {
            out.println("Even length operation test FAILED.");
            result = false;
        }

        if (!runFailTests()) {
            out.println("Negative tests FAILED.");
            result = false;
        }

        if (!runObjectRms()) {
            out.println("Remove by reference tests failed.");
            result = false;
        }

        if (!runInserts()) {
            out.println("Some insert tests failed.");
            result = false;
        }

        if (!runClasses()) {
            out.println("Class type tests failed.");
            result = false;
        }

        Assert.assertTrue(result);
    }

    /**
     * Convenience method which creates a array of <tt>count</tt> objects.
     */
    private Integer[] newIntArr(int count) {
        Integer[] array = new Integer[count];

        for (int i = 0; i < count; i++) {
            array[i] = new Integer(i);
        }

        return array;
    }

    private boolean runAppendOps() {
        boolean localResult = true;

        Object[] data1 = newIntArr(4);
        Object[] back = null;

        // ---- remove beginning of even size array ----
        back = DynamicArray.append(data1, new Integer(99));

        back = null;
        Object[] data2 = null;
        back = DynamicArray.append(data2, new Integer(100));

        back = null;
        Object[] data3 = newIntArr(0);
        back = DynamicArray.append(data3, new Integer(101));

        back = null;
        try {
            back = DynamicArray.append(null, null);
            // don't expect to be here
            localResult = false;
        } catch (IllegalArgumentException e) {
            // expect to be here
        }

        return localResult;
    }

    private boolean runEndpoints() {
        boolean localResult = true;

        Object[] data1 = newIntArr(4);
        Object[] back = null;

        // ---- remove beginning of even size array ----
        back = DynamicArray.remove(data1, 0);

        try {
            // check new size
            if (back.length != data1.length - 1) {
                localResult = false;
                out.println("Length is the same after endpoint delete. (1)");
            }

            // check type
            Integer foo = (Integer) back[0];

            // check for proper deletion
            if (back[0] == data1[0]) {
                localResult = false;
                out.println("Deleted element is still there (1).");
            }

            if (back[0] != data1[1]) {
                localResult = false;
                out.println("Data missing after endpoint delete (1).");
            }
        } catch (ClassCastException e) {
            localResult = false;
            out.println("Cast exception in endpoint check (1).");
        } catch (NullPointerException e) {
            localResult = false;
            out.println(
                    "Unexpected null pointer exception during endpoint check (1).");
            e.printStackTrace(out);
        } catch (ArrayIndexOutOfBoundsException e) {
            localResult = false;
            out.println(
                    "Unexpected array index exception during endpoint check (1).");
            e.printStackTrace(out);
        }

        Object[] data2 = newIntArr(3);
        back = null;

        // ----- remove beginning of odd size array ----
        back = DynamicArray.remove(data2, 0);

        try {
            // check new size
            if (back.length != data2.length - 1) {
                localResult = false;
                out.println("Length is the same after endpoint delete. (2)");
            }

            // check type
            Integer foo = (Integer) back[0];

            // check for proper deletion
            if (back[0] == data2[0]) {
                localResult = false;
                out.println("Deleted element is still there (2).");
            }

            if (back[0] != data2[1]) {
                localResult = false;
                out.println("Data missing after endpoint delete (2).");
            }
        } catch (ClassCastException e) {
            localResult = false;
            out.println("Cast exception in endpoint check (2).");
        } catch (NullPointerException e) {
            localResult = false;
            out.println(
                    "Unexpected null pointer exception during endpoint check (2).");
            e.printStackTrace(out);
        } catch (ArrayIndexOutOfBoundsException e) {
            localResult = false;
            out.println(
                    "Unexpected array index exception during endpoint check (2).");
            e.printStackTrace(out);
        }

        back = null;

        // remove end of even size array
        Object[] data3 = newIntArr(4);
        back = DynamicArray.remove(data3, data3.length - 1);

        try {
            // check new size
            if (back.length != data3.length - 1) {
                localResult = false;
                out.println("Length is the same after endpoint delete. (3)");
            }

            // check type
            Integer foo = (Integer) back[back.length - 1];

            // check for proper deletion
            if (back[back.length - 1] == data3[data3.length - 1]) {
                localResult = false;
                out.println("Deleted element is still there (3).");
            }

            if (back[back.length - 1] != data3[data3.length - 2]) {
                localResult = false;
                out.println("Data missing after endpoint delete (3).");
            }
        } catch (ClassCastException e) {
            localResult = false;
            out.println("Cast exception in endpoint check (3).");
        } catch (NullPointerException e) {
            localResult = false;
            out.println(
                    "Unexpected null pointer exception during endpoint check (3).");
            e.printStackTrace(out);
        } catch (ArrayIndexOutOfBoundsException e) {
            localResult = false;
            out.println(
                    "Unexpected array index exception during endpoint check (3).");
            e.printStackTrace(out);
        }

        back = null;

        // remove end of odd size array
        Object[] data4 = newIntArr(3);
        back = DynamicArray.remove(data4, data4.length - 1);

        try {
            // check new size
            if (back.length != data4.length - 1) {
                localResult = false;
                out.println("Length is the same after endpoint delete. (4)");
            }

            // check type
            Integer foo = (Integer) back[back.length - 1];

            // check for proper deletion
            if (back[back.length - 1] == data4[data4.length - 1]) {
                localResult = false;
                out.println("Deleted element is still there (4).");
            }

            if (back[back.length - 1] != data4[data4.length - 2]) {
                localResult = false;
                out.println("Data missing after endpoint delete (4).");
            }
        } catch (ClassCastException e) {
            localResult = false;
            out.println("Cast exception in endpoint check (4).");
        } catch (NullPointerException e) {
            localResult = false;
            out.println(
                    "Unexpected null pointer exception during endpoint check (4).");
            e.printStackTrace(out);
        } catch (ArrayIndexOutOfBoundsException e) {
            localResult = false;
            out.println(
                    "Unexpected array index exception during endpoint check (4).");
            e.printStackTrace(out);
        }

        return localResult;
    }

    private boolean runOddOps() {
        boolean localResult = true;

        Object[] back = null;

        // left of delete is odd, right is odd
        Object[] data1 = newIntArr(7);
        back = DynamicArray.remove(data1, 3);

        try {
            // check new size
            if (back.length != data1.length - 1) {
                localResult = false;
                out.println("Length is the same after delete in even test. (1)");
            }

            // check for proper deletion
            if (back[2] != data1[2]) {
                localResult = false;
                out.println("Improper element move in even ops test. (1a)");
                System.out.println(back[2] + " - " + data1[2]);
            }

            if (back[3] != data1[4]) {
                localResult = false;
                out.println("Improper element move in even ops test. (1b)");
                System.out.println(back[3] + " - " + data1[4]);
            }
        } catch (ClassCastException e) {
            localResult = false;
            out.println("Cast exception in even ops check (1).");
        } catch (NullPointerException e) {
            localResult = false;
            out.println(
                    "Unexpected null pointer exception during even ops check (1).");
            e.printStackTrace(out);
        } catch (ArrayIndexOutOfBoundsException e) {
            localResult = false;
            out.println(
                    "Unexpected array index exception during even ops check (1).");
            e.printStackTrace(out);
        }

        back = null;

        // left and right of delete are even
        Object[] data2 = newIntArr(7);
        back = DynamicArray.remove(data2, 4);

        try {
            // check new size
            if (back.length != data2.length - 1) {
                localResult = false;
                out.println("Length is the same after delete in even test. (2)");
            }

            // check for proper deletion
            if (back[3] != data2[3]) {
                localResult = false;
                out.println("Improper element move in even ops test. (2a)");
                System.out.println(back[3] + " - " + data2[3]);
            }

            if (back[4] != data2[5]) {
                localResult = false;
                out.println("Improper element move in even ops test. (2b)");
                System.out.println(back[4] + " - " + data2[5]);
            }
        } catch (ClassCastException e) {
            localResult = false;
            out.println("Cast exception in even ops check (2).");
        } catch (NullPointerException e) {
            localResult = false;
            out.println(
                    "Unexpected null pointer exception during even ops check (2).");
            e.printStackTrace(out);
        } catch (ArrayIndexOutOfBoundsException e) {
            localResult = false;
            out.println(
                    "Unexpected array index exception during even ops check (2).");
            e.printStackTrace(out);
        }

        return localResult;
    }

    private boolean runEvenOps() {
        boolean localResult = true;
        Object[] back = null;

        // left of delete is odd, right is even
        Object[] data1 = newIntArr(8);
        back = DynamicArray.remove(data1, 3);

        try {
            // check new size
            if (back.length != data1.length - 1) {
                localResult = false;
                out.println("Length is the same after delete in even test. (1)");
            }

            // check for proper deletion
            if (back[2] != data1[2]) {
                localResult = false;
                out.println("Improper element move in even ops test. (1a)");
                System.out.println(back[2] + " - " + data1[2]);
            }

            if (back[3] != data1[4]) {
                localResult = false;
                out.println("Improper element move in even ops test. (1b)");
                System.out.println(back[3] + " - " + data1[4]);
            }
        } catch (ClassCastException e) {
            localResult = false;
            out.println("Cast exception in even ops check (1).");
        } catch (NullPointerException e) {
            localResult = false;
            out.println(
                    "Unexpected null pointer exception during even ops check (1).");
            e.printStackTrace(out);
        } catch (ArrayIndexOutOfBoundsException e) {
            localResult = false;
            out.println(
                    "Unexpected array index exception during even ops check (1).");
            e.printStackTrace(out);
        }

        back = null;

        // left of delete is even, right is odd
        Object[] data2 = newIntArr(8);
        back = DynamicArray.remove(data2, 4);

        try {
            // check new size
            if (back.length != data2.length - 1) {
                localResult = false;
                out.println("Length is the same after delete in even test. (2)");
            }

            // check for proper deletion
            if (back[3] != data2[3]) {
                localResult = false;
                out.println("Improper element move in even ops test. (2a)");
                System.out.println(back[3] + " - " + data2[3]);
            }

            if (back[4] != data2[5]) {
                localResult = false;
                out.println("Improper element move in even ops test. (2b)");
                System.out.println(back[4] + " - " + data2[5]);
            }
        } catch (ClassCastException e) {
            localResult = false;
            out.println("Cast exception in even ops check (2).");
        } catch (NullPointerException e) {
            localResult = false;
            out.println(
                    "Unexpected null pointer exception during even ops check (2).");
            e.printStackTrace(out);
        } catch (ArrayIndexOutOfBoundsException e) {
            localResult = false;
            out.println(
                    "Unexpected array index exception during even ops check (2).");
            e.printStackTrace(out);
        }

        return localResult;
    }

    private boolean runFailTests() {
        boolean localResult = true;

        Object[] back = null;

        // Integer and Float are not compatible
        try {
            Object[] data1 = newIntArr(2);
            back = DynamicArray.append(data1, new Float(1.2));
            localResult = false;
            out.println("Stored inappropriate entry in array.");
        } catch (ArrayStoreException e) {
        }

        back = null;

        // invalid array, NullPointerException expected
        try {
            Object[] data2 = null;
            back = DynamicArray.remove(data2, 1);
            localResult = false;
            out.println("Null array allowed remove.");
        } catch (IllegalArgumentException e) {
        }

        back = null;

        // empty array/invalid index, IllegalArgumentException expected
        try {
            Object[] data3 = new Object[0];
            back = DynamicArray.remove(data3, 1);
            localResult = false;
            out.println("Invalid index removal did not work as expected. (1)");
        } catch (IllegalArgumentException e) {
        }

        back = null;

        // invalid index, IllegalArgumentException expected
        try {
            Object[] data4 = newIntArr(2);
            back = DynamicArray.remove(data4, 4);
            localResult = false;
            out.println("Invalid index removal did not work as expected. (2)");
        } catch (IllegalArgumentException e) {
        }


        back = null;

        // invalid object to remove, transparent failure expected
        Object[] data5 = newIntArr(2);
        back = DynamicArray.remove(data5, new Integer(1995));

        if (back != data5 || back == null) {
            // we should have gotten the old array back
            localResult = false;
            out.println("Invalid object removal did not work as expected. (2)");
        }

        back = null;

        // invalid array, transparent failure expected
        Object[] data6 = null;
        back = DynamicArray.remove(data6, new Integer(1996));

        if (back != data6) {
            // we should have gotten the old array back
            localResult = false;
            out.println("Null array allowed remove. (3)");
        }
        return localResult;
    }

    /**
     * Testing removals.
     */
    private boolean runObjectRms() {
        boolean localResult = true;

        Integer[] back = null;

        // valid removal of an object
        Integer[] data1 = {new Integer(311), new Integer(911)};
        back = DynamicArray.remove(data1, data1[0]);

        if (back.length != 1 || back[0] != data1[1]) {
            localResult = false;
            out.println("Object removal unsuccessful.");
        }

        back = null;

        // remove null, shouldn't do anything
        String[] data2 = {"foo", "bar"};
        String[] back1 = null;
        back1 = DynamicArray.remove(data2, null);

        if (back1.length != 2) {
            localResult = false;
            out.println("Removing a null when none exists did damage.");
        }

        back = null;

        // remove null, should remove the middle item
        File[] data3 = {new File("foo"), null, new File("baz")};
        File[] back2 = null;
        back2 = DynamicArray.remove(data3, null);

        if (back2.length != 2) {
            localResult = false;
            out.println("Could not remove a null.");
        }

        return localResult;
    }

    private boolean runInserts() {
        boolean localResult = true;

        Integer[] back = null;

        // insert object in the middle
        Integer[] data1 = {new Integer(311), new Integer(911), new Integer(0)};
        Integer newInt = new Integer(611);
        back = DynamicArray.insert(data1, newInt, 1);

        if (back.length != 4 || back[1] != newInt) {
            localResult = false;
            out.println("Middle of array insertion failed.");
        }

        data1 = null;
        back = null;

        // insert object at the beginning
        Integer[] data2 = {new Integer(311), new Integer(911)};
        back = DynamicArray.insert(data2, newInt, 0);

        if (back.length != 3 || back[0] != newInt) {
            localResult = false;
            out.println("Beginning of array insertion failed.");
        }

        data2 = null;
        back = null;

        // insert object at the end
        Integer[] data3 = {new Integer(311), new Integer(911)};
        back = DynamicArray.insert(data3, newInt, 2);

        if (back.length != 3 || back[2] != newInt) {
            localResult = false;
            out.println("End of array insertion failed.");
        }

        data3 = null;
        back = null;

        return localResult;
    }

    /**
     * Make sure that the type of the array is correct.
     */
    private boolean runClasses() {
        Integer[] arr = null;
        Class<? extends Integer> intClass = null;
        Class<? extends Boolean> boolClass = null;

        try {
            intClass = Class.forName("java.lang.Integer").asSubclass(Integer.class);
            boolClass = Class.forName("java.lang.Boolean").asSubclass(Boolean.class);
        } catch (ClassNotFoundException e) {
            e.printStackTrace(out);
            return false;
        }

        // check to see that is uses the supplied class if the orig. arr is null
        arr = DynamicArray.append(arr, new Integer(1), intClass);

        if (arr.getClass().getComponentType() != intClass) {
            out.println("New array type does not correspond to the requested type.");
            return false;
        }

        try {
            arr = (Integer[]) DynamicArray.append(arr, new Float(1.0), intClass);
            // we are supposed to end up in the catch
            return false;
        } catch (java.lang.ArrayStoreException e) {
            // OK
        }

        // check to see that it does not use the supplied class for a non-null orig. arr
        arr = new Integer[0];
        arr = (Integer[]) DynamicArray.append(arr, new Integer(1), boolClass);

        if (arr.getClass().getComponentType() != intClass) {
            out.println("Array type should not be the requested type.");
            out.println("append(<non-null array>, <Integer object>, <Boolean Class Object>)");
            out.println("Didn't get back Integer[] as expected.");
            return false;
        }

        return true;
    }


}
