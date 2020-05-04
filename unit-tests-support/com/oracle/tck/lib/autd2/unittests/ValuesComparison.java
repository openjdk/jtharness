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
package com.oracle.tck.lib.autd2.unittests;

import com.sun.tck.lib.tgf.AbstractValue;
import com.sun.tck.lib.tgf.Values;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 *
 */
public class ValuesComparison {

    public static void compare(Values values,
                               Values expected) {
        compare(values, createCollection(expected));
    }

    public static void compare(Iterable<Object[]> iterable,
                               Collection<Object[]> expected) {
        checkEqualResultsForEveryIteration(iterable);


        int i = 0;
        int counter = 0;
        for (Object[] initial : iterable) {

            LinkedList<Object> args = new LinkedList<Object>();
            for (Object object : initial) {
                if (object instanceof AbstractValue) {
                    args.add(((AbstractValue) object).doCreate());
                } else {
                    args.add(object);
                }
            }
            Object[] ret = args.toArray();

            boolean found = false;
            int innerCounter = 0;
            for (Object[] exp : expected) {
                if (Arrays.deepEquals(exp, ret) && innerCounter == counter) {
                    found = true;
                    break;
                }
                innerCounter++;
            }
            Assert.assertTrue(Arrays.toString(ret) + " not found at position " + counter, found);
            i++;
            counter++;
        }
        Assert.assertEquals( expected.size(), i );
    }


    private static void checkEqualResultsForEveryIteration(Iterable<Object[]> values) {
        final Object[][] objects_1 = ValuesComparison.createArray(values);
        final Object[][] objects_2 = ValuesComparison.createArray(values);
        final Object[][] objects_3 = ValuesComparison.createArray(values);
        Arrays.deepEquals(objects_1, objects_2);
        Arrays.deepEquals(objects_2, objects_3);
    }


    public static Object[][] createArray(Iterable<Object[]> values) {
        ArrayList<Object[]> arrayList = ValuesComparison.createCollection(values);
        Object[][] result = new Object[arrayList.size()][];
        for (int i = 0; i < result.length; i++) {
            result[i] = arrayList.get(i);
        }
        return result;
    }

    public static ArrayList<Object[]> createCollection(Iterable<Object[]> values) {
        ArrayList<Object[]> result = new ArrayList<Object[]>();
        for (Object[] initial : values) {
            LinkedList<Object> args = new LinkedList<Object>();
            for (Object object : initial) {
                if (object instanceof AbstractValue) {
                    args.add(((AbstractValue) object).doCreate());
                } else {
                    args.add(object);
                }
            }
            Object[] ret = args.toArray();
            result.add(ret);
        }
        return result;
    }

    public static void checkCachedReturnsTheSame(Values values) {
        final Values cache = values.createCache();
        compare(cache, values);
        checkEqualResultsForEveryIteration(cache);
        final Object[][] objects_1 = ValuesComparison.createArray(cache);
        final Object[][] objects_2 = ValuesComparison.createArray(cache);
        org.junit.Assert.assertEquals(objects_1.length, objects_2.length);
        Arrays.deepEquals(objects_1, objects_2);
        for (int i =0; i<objects_1.length; i++) {
            final Object[] first = objects_1[i];
            final Object[] second = objects_2[i];
            for (int j = 0; j < first.length; j++) {
                org.junit.Assert.assertFalse( first[j] instanceof AbstractValue);
                org.junit.Assert.assertFalse( second[j] instanceof AbstractValue);
                org.junit.Assert.assertSame( first[j], second[j] );
            }
        }
    }


}
