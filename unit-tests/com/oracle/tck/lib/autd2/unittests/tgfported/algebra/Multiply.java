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
package com.oracle.tck.lib.autd2.unittests.tgfported.algebra;

import com.oracle.tck.lib.autd2.unittests.BaseTestGroup;
import com.oracle.tck.lib.autd2.unittests.TU;
import com.oracle.tck.lib.autd2.unittests.ValuesComparison;
import com.sun.tck.lib.tgf.DataFactory;
import com.sun.tck.lib.tgf.TestData;
import com.sun.tck.lib.tgf.Values;
import com.sun.tck.test.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.sun.tck.lib.tgf.DataFactory.*;

/**
 *
 */
public class Multiply {

    @Test
    public void testCombineCombine_3() {
        Values values_1 = createColumn(1, 2);
        Values values_2 = createColumn(3, 4);
        Values values_3 = createColumn(5, 6);

        values_2 = values_2.multiply(values_3);
        values_1 = values_1.multiply(values_2);
        checkCombineCombine_3(values_1);
    }

    @Test
    public void testCombineCombine_3_supportingMethod_1() {
        Values values_1 = createColumn(1, 2);
        Values values_2 = createColumn(3, 4);
        Values values_3 = createColumn(5, 6);
        checkCombineCombine_3(multiply(values_1, values_2, values_3));
    }

    @Test
    public void testCombineCombine_3_supportingMethod_2() {
        Values values_1 = createColumn(1, 2);
        Values values_2 = createColumn(3, 4);
        Values values_3 = createColumn(5, 6);
        checkCombineCombine_3(multiply(
                multiply(values_1, values_2), values_3)
        );
    }

    @Test
    public void testCombineCombine_3_supportingMethod_3() {
        Values values_1 = createColumn(1, 2);
        Values values_2 = createColumn(3, 4);
        Values values_3 = createColumn(5, 6);
        checkCombineCombine_3(multiply(
                multiply(values_1, values_2, values_3))
        );
    }

    @Test
    public void testCombineCombine_3_supportingMethod_withEmpty_1() {
        Values values_1 = createColumn(1, 2);
        Values values_2 = createColumn(3, 4);
        Values values_3 = createColumn(5, 6);
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(multiply(values_1, createColumn(), values_2, values_3), expected);
    }

    @Test
    public void testCombineCombine_3_supportingMethod_withEmpty_2() {
        Values values_1 = createColumn(1, 2);
        Values values_2 = createColumn(3, 4);
        Values values_3 = createColumn(5, 6);
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(multiply(createColumn(), values_1, createColumn(), values_2, createColumn(), values_3, createColumn()), expected);
    }

    @Test
    public void testCombineCombine_3_supportingMethod_withEmpty_3() {
        Values values_1 = createColumn(1, 2);
        Values values_2 = createColumn(3, 4);
        Values values_3 = createColumn(5, 6);
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(multiply(createColumn(), values_1, createColumn().pseudoMultiply(), values_2, createColumn(), values_3, createColumn()), expected);
    }

    @Test
    public void testCombineCombine_3_supportingMethod_withEmpty_4() {
        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(createColumn().multiply(1,2,3,4), expected);
    }

    @Test
    public void testRun_DataFilteredOutCompletely4() {
        final ArrayList<String> arrayList = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            Values setupOne() {
                return DataFactory.createColumn().multiply(1, 2, 3);
            }

            @TestCase
            @TestData("setupOne")
            public void myTest(String s) {
                arrayList.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertEquals(0, arrayList.size());
        Assert.assertTrue(status.isOK());
        Assert.assertEquals("Passed. test cases: 1; all passed", status.toString());
    }


    private void checkCombineCombine_3(Values values) {
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3, 5});
        expected.add(new Object[]{1, 3, 6});
        expected.add(new Object[]{1, 4, 5});
        expected.add(new Object[]{1, 4, 6});
        expected.add(new Object[]{2, 3, 5});
        expected.add(new Object[]{2, 3, 6});
        expected.add(new Object[]{2, 4, 5});
        expected.add(new Object[]{2, 4, 6});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombineCombine_3_cacheIsExpected() {
        Values values_1 = createColumn(1, 2);
        Values values_2 = createColumn(3, 4);
        Values values_3 = createColumn(5, 6);

        values_2 = values_2.multiply(values_3);
        values_1 = values_1.multiply(values_2);
        checkCombineCombine_3(values_1.createCache());
    }

    @Test
    public void testCombineCombine_3_cacheIsEqualToOriginal() {
        Values values_1 = createColumn(1, 2);
        Values values_2 = createColumn(3, 4);
        Values values_3 = createColumn(5, 6);

        values_2 = values_2.multiply(values_3);
        values_1 = values_1.multiply(values_2);
        ValuesComparison.compare(values_1.createCache(), values_1);
    }

    @Test
    public void testCombineCombine_3_cache() {
        Values values_1 = createColumn(1, 2);
        Values values_2 = createColumn(3, 4);
        Values values_3 = createColumn(5, 6);

        values_2 = values_2.multiply(values_3);
        values_1 = values_1.multiply(values_2);

        ValuesComparison.checkCachedReturnsTheSame(values_1);
    }

    @Test
    public void testCombineCombine_4_cache() {
        Values values_1 = createColumn(1, 2, 72);
        Values values_2 = createColumn(3, 4, 45);
        Values values_3 = createColumn(5, 45, 90);

        values_2 = values_2.multiply(values_3);
        values_1 = values_1.multiply(values_2);

        ValuesComparison.checkCachedReturnsTheSame(values_1);
    }

    @Test
    public void testCombineCombine_5_cache() {
        ValuesComparison.checkCachedReturnsTheSame(
                createColumn(1, 2, 72, 234)
                        .multiply(3, 4, 45, 678)
                        .multiply(5, 45, 90, 345)
        );
    }

    @Test
    public void testCombineCombine_3_3() {
        Values values = createColumn(1, 2);
        values = values.multiply(new Object[][] {{4, 5}, {6, 7}} );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { 1, 4, 5 } );
        expected.add(new Object[] { 1, 6, 7 } );
        expected.add(new Object[] { 2, 4, 5 } );
        expected.add(new Object[] { 2, 6, 7 } );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testMultiplySelf_1() {
        Values values = createColumn(5, 3);
        values = values.multiply(values);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { 5, 5 } );
        expected.add(new Object[] { 5, 3 } );
        expected.add(new Object[] { 3, 5 } );
        expected.add(new Object[] { 3, 3 } );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testMultiplySelf_2() {
        Object o1 = new Object();
        Object o2 = new Object();
        Values values = createColumn(o1, o2);
        values = values.multiply(values);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { o1, o1 } );
        expected.add(new Object[] { o1, o2 } );
        expected.add(new Object[] { o2, o1 } );
        expected.add(new Object[] { o2, o2 } );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_empty_1() {
        Values values = createColumn();
        values = values.multiply(createColumn());
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_empty_2() {
        Values values = createColumn().multiply( createColumn(1, 2));
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_empty_3() {
        Values values = createRow().multiply( createColumn(1, 2));
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_empty_4() {
        Values values = createColumn(1, 2).multiply(createColumn());
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_empty_5() {
        Values values = createColumn(1, 2).multiply(createRow());
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_empty_6() {
        Values values = createColumn(1).multiply(createRow());
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_empty_7() {
        Values values = createColumn(1).multiply(createColumn());
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_empty_8() {
        Values values = createColumn(1).multiply(createColumn()).multiply(5, 6);
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_empty_9() {
        Values values = createColumn(1).multiply(createRow()).multiply(5, 6);
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_empty_10() {
        Values values = createColumn(1).multiply().multiply(5, 6);
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_empty_11() {
        Values values = createColumn(159).multiply().multiply().multiply().multiply(6);
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_empty_12() {
        Values values = createRow().multiply( createColumn(1, 2));
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testMultiplyUtil_Null() {
        Values values = multiply((Values[])null);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { null } );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testMultiplyUtil_empty() {
        Values values = multiply();
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testMultiplyUtil_SingleItem() {
        Values values = multiply( createRow(34, 78) );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { 34, 78 } );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testMultiplyUtil_SingleItem_1() {
        Values values = multiply( createColumn(3, 7) );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { 3 } );
        expected.add(new Object[] { 7 } );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombineCombine_3_3_1() {
        Values values = createColumn(7);
        values = values.multiply(new Object[][] {{4, 5}, {6, 7}} );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { 7, 4, 5 } );
        expected.add(new Object[] { 7, 6, 7 } );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombineCombine_3_3_1_cachedIsExpected() {
        Values values = createColumn(7);
        values = values.multiply(new Object[][] {{4, 5}, {6, 7}} );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { 7, 4, 5 } );
        expected.add(new Object[] { 7, 6, 7 } );
        ValuesComparison.compare(values.createCache(), expected);
    }

    @Test
    public void testCombineCombine_3_1() {
        Values values_0 = createColumn(0);
        Values values_1 = createColumn(1, 2);
        Values values_2 = createColumn(3, 4);
        Values values_3 = createColumn(5, 6);

        values_2 = values_2.multiply(values_3);
        values_1 = values_1.multiply(values_2);
        values_0 = values_0.multiply(values_1);

        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{0, 1, 3, 5});
        expected.add(new Object[]{0, 1, 3, 6});
        expected.add(new Object[]{0, 1, 4, 5});
        expected.add(new Object[]{0, 1, 4, 6});
        expected.add(new Object[]{0, 2, 3, 5});
        expected.add(new Object[]{0, 2, 3, 6});
        expected.add(new Object[]{0, 2, 4, 5});
        expected.add(new Object[]{0, 2, 4, 6});
        ValuesComparison.compare(values_0, expected);
    }

    @Test
    public void testCombineCombine_3_1_withEmpty() {
        Values values_0 = createColumn(0).multiply();
        Values values_1 = createColumn(1, 2).multiply().multiply();
        Values values_2 = createColumn().multiply( createColumn(3, 4) );
        Values values_3 = createColumn(5, 6);

        values_2 = values_2.multiply(values_3).multiply();
        values_1 = values_1.multiply(values_2);
        values_0 = values_0.multiply(values_1);

        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values_0, expected);
    }

    @Test
    public void testCombineCombine_3_1_cacheIsExpected() {
        Values values_0 = createColumn(0);
        Values values_1 = createColumn(1, 2);
        Values values_2 = createColumn(3, 4);
        Values values_3 = createColumn(5, 6);

        values_2 = values_2.multiply(values_3);
        values_1 = values_1.multiply(values_2);
        values_0 = values_0.multiply(values_1);

        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{0, 1, 3, 5});
        expected.add(new Object[]{0, 1, 3, 6});
        expected.add(new Object[]{0, 1, 4, 5});
        expected.add(new Object[]{0, 1, 4, 6});
        expected.add(new Object[]{0, 2, 3, 5});
        expected.add(new Object[]{0, 2, 3, 6});
        expected.add(new Object[]{0, 2, 4, 5});
        expected.add(new Object[]{0, 2, 4, 6});
        ValuesComparison.compare(values_0.createCache(), expected);
    }

    @Test
    public void testCombineCombine_3_1_cache() {
        Values values_0 = createColumn(0);
        Values values_1 = createColumn(1, 2);
        Values values_2 = createColumn(3, 4);
        Values values_3 = createColumn(5, 6);

        values_2 = values_2.multiply(values_3);
        values_1 = values_1.multiply(values_2);
        values_0 = values_0.multiply(values_1);

        ValuesComparison.checkCachedReturnsTheSame(values_0);
    }


    @Test
    public void testCombineCombine_1() {
        Values values = createColumn(1, 2);
        values = values.multiply(createColumn(3, 4));
        values = values.multiply(createColumn(5));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3, 5});
        expected.add(new Object[]{1, 4, 5});
        expected.add(new Object[]{2, 3, 5});
        expected.add(new Object[]{2, 4, 5});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombineCombine_1_withEmpty_1() {
        Values values = createColumn(1, 2);
        values = values.multiply(createColumn(3, 4));
        values = values.multiply(createColumn(5)).multiply(createColumn());
        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombineCombine_1_withEmpty_1_1() {
        Values values = createColumn(1, 2);
        values = values.multiply(createColumn(3, 4));
        values = values.multiply(createColumn(5)).multiply();
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombineCombine_1_withEmpty_1_2() {
        Values values = createColumn();
        values = values.multiply(createColumn(3, 4));
        values = values.multiply(createColumn(5));
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombineCombine_1_withEmpty_1_3() {
        Values values = createRow();
        values = values.multiply(createColumn(3, 4));
        values = values.multiply(createColumn(5));
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombineCombine_1_withEmpty_1_4() {
        Values values = createValues(new byte[0][0]);
        values = values.multiply(createColumn(3, 4));
        values = values.multiply(createColumn(5));
        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombineCombine_1_withEmpty_1_5() {
        Values values = createValues(new Object[0][0]);
        values = values.multiply(createColumn(3, 4));
        values = values.multiply(createColumn(5));
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombineCombine_1_withEmpty_2() {
        Values values = createColumn(1, 2);
        values = values.multiply(createColumn(3, 4));
        values = values.multiply(createColumn(5)).multiply(createRow());
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombineCombine_1_empty() {
        Values values = createColumn(1, 2).multiply( createRow() );
        values = values.multiply(createColumn(3, 4)).pseudoMultiply( createRow() );
        values = values.multiply(createColumn(5));
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_1_2() {
        Values values = createColumn(1, 2, 3);
        values = values.multiply(createColumn(5, 6));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 5});
        expected.add(new Object[]{1, 6});
        expected.add(new Object[]{2, 5});
        expected.add(new Object[]{2, 6});
        expected.add(new Object[]{3, 5});
        expected.add(new Object[]{3, 6});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_1_2_uniteWithEmpty_1() {
        Values values = createColumn(1);
        values = values.multiply(createColumn(5, 6).unite());
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 5});
        expected.add(new Object[]{1, 6});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_1_2_uniteWithEmpty_2() {
        Values values = createColumn(2).unite();
        values = values.multiply(createColumn(5, 6).unite());
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{2, 5});
        expected.add(new Object[]{2, 6});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_1_4() {
        Values values = createColumn(1, 2, 3);
        values = values.multiply(createColumn(5, 6, 7));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 5});
        expected.add(new Object[]{1, 6});
        expected.add(new Object[]{1, 7});
        expected.add(new Object[]{2, 5});
        expected.add(new Object[]{2, 6});
        expected.add(new Object[]{2, 7});
        expected.add(new Object[]{3, 5});
        expected.add(new Object[]{3, 6});
        expected.add(new Object[]{3, 7});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_1() {
        Values values = createColumn(1, 2);
        values = values.multiply(createColumn(3, 4));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3});
        expected.add(new Object[]{1, 4});
        expected.add(new Object[]{2, 3});
        expected.add(new Object[]{2, 4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_1_8() {
        Values values = createColumn(1, 1);
        values = values.multiply(createColumn(2, 2));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 2});
        expected.add(new Object[]{1, 2});
        expected.add(new Object[]{1, 2});
        expected.add(new Object[]{1, 2});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_1_10() {
        Values values = createColumn("blabla");
        values = values.multiply(values);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"blabla", "blabla"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_1_10_util() {
        Values values = createColumn("blabla");
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"blabla", "blabla"});
        ValuesComparison.compare(multiply(values, values), expected);
    }

    @Test
    public void testCombine_1_9() {
        Values values = createColumn("a","a","a","a","a","a" );
        values = values.multiply(createColumn(Math.PI));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", Math.PI});
        expected.add(new Object[]{"a", Math.PI});
        expected.add(new Object[]{"a", Math.PI});
        expected.add(new Object[]{"a", Math.PI});
        expected.add(new Object[]{"a", Math.PI});
        expected.add(new Object[]{"a", Math.PI});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_1_6() {
        Values values = createColumn(1);
        values = values.multiply(createColumn(3, 4));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3});
        expected.add(new Object[]{1, 4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_1_7() {
        Values values = createColumn(1,2);
        values = values.multiply(createColumn(4));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 4});
        expected.add(new Object[]{2, 4});
        ValuesComparison.compare(values, expected);
    }


    @Test
    public void testCombineCombine_2() {
        Values values = createColumn(1, 2);
        values = values.multiply(createColumn(3, 4));
        values = values.multiply(createColumn(5, 6));
        checkCombineCombine_3(values);
    }

    @Test
    public void testCombine_0() {
        Values values = createColumn(6775);
        values = values.multiply(createColumn(3455));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{6775, 3455});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_0_1() {
        Values values = createColumn("a");
        values = values.multiply(createColumn("b"));
        values = values.multiply(createColumn("c"));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", "b", "c"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_0_1_util() {
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", "b", "c"});
        ValuesComparison.compare(
                multiply(createColumn("a"), createColumn("b"), createColumn("c")    ), expected);
    }

    @Test
    public void testCombine_0_1_1() {
        Values values = createColumn("a");
        values = values.multiply(createColumn("b", "x"));
        values = values.multiply(createColumn("c"));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", "b", "c"});
        expected.add(new Object[]{"a", "x", "c"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_0_1_1_1() {
        Values values = createColumn("a");
        values = values.multiply(createColumn("b", "x"));
        values = values.multiply(createColumn("c"));
        values = values.multiply(createColumn("d"));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", "b", "c", "d"});
        expected.add(new Object[]{"a", "x", "c", "d"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_0_1_1_2() {
        Values values = createColumn("a");
        values = values.multiply(createColumn("b", "x"));
        values = values.multiply(createColumn("c"));
        values = values.multiply(createColumn("d", 90));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", "b", "c", "d"});
        expected.add(new Object[]{"a", "b", "c", 90});
        expected.add(new Object[]{"a", "x", "c", "d"});
        expected.add(new Object[]{"a", "x", "c", 90});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_0_2() {
        Values values = createColumn("a");
        values = values.multiply(createColumn("b"));
        values = values.multiply(createColumn("c"));
        values = values.multiply(createColumn(100));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", "b", "c", 100});
        ValuesComparison.compare(values, expected);
    }


    @Test
    public void testCombine_1_3() {
        Values values = createColumn(1, 2);
        values = values.multiply(createColumn(3, 4, 5));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3});
        expected.add(new Object[]{1, 4});
        expected.add(new Object[]{1, 5});
        expected.add(new Object[]{2, 3});
        expected.add(new Object[]{2, 4});
        expected.add(new Object[]{2, 5});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_1_3_util() {

        Values values =
                multiply(
                        createColumn(1, 2),
                        createColumn(3, 4, 5)
                );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3});
        expected.add(new Object[]{1, 4});
        expected.add(new Object[]{1, 5});
        expected.add(new Object[]{2, 3});
        expected.add(new Object[]{2, 4});
        expected.add(new Object[]{2, 5});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombine_1_5() {
        Values values = createColumn(1, 2);
        values = values.multiply(createColumn(3, 4, 5));
        values = values.multiply(createColumn(6));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3, 6});
        expected.add(new Object[]{1, 4, 6});
        expected.add(new Object[]{1, 5, 6});
        expected.add(new Object[]{2, 3, 6});
        expected.add(new Object[]{2, 4, 6});
        expected.add(new Object[]{2, 5, 6});
        ValuesComparison.compare(values, expected);
    }


    @Test
    public void testCombine_1_1() {
        Values values = createColumn(1, 2);
        values = values.multiply(3, 4);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3});
        expected.add(new Object[]{1, 4});
        expected.add(new Object[]{2, 3});
        expected.add(new Object[]{2, 4});
        ValuesComparison.compare(values, expected);
    }


    @Test
    public void testCombine_1_1_util() {

        Values values =
                multiply(
                        createColumn(1, 2),
                        createColumn(3, 4)
                );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3});
        expected.add(new Object[]{1, 4});
        expected.add(new Object[]{2, 3});
        expected.add(new Object[]{2, 4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testCombineCombine_4() {
        Values values_1 = createColumn(1, 2);
        Values values_2 = createColumn(3, 4);
        Values values_3 = createColumn(5, 6);
        Values values_4 = createColumn(7, 8);

        values_1 = values_1.multiply(values_2);
        values_3 = values_3.multiply(values_4);
        values_1 = values_1.multiply(values_3);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3, 5, 7});
        expected.add(new Object[]{1, 3, 5, 8});
        expected.add(new Object[]{1, 3, 6, 7});
        expected.add(new Object[]{1, 3, 6, 8});
        expected.add(new Object[]{1, 4, 5, 7});
        expected.add(new Object[]{1, 4, 5, 8});
        expected.add(new Object[]{1, 4, 6, 7});
        expected.add(new Object[]{1, 4, 6, 8});
        expected.add(new Object[]{2, 3, 5, 7});
        expected.add(new Object[]{2, 3, 5, 8});
        expected.add(new Object[]{2, 3, 6, 7});
        expected.add(new Object[]{2, 3, 6, 8});
        expected.add(new Object[]{2, 4, 5, 7});
        expected.add(new Object[]{2, 4, 5, 8});
        expected.add(new Object[]{2, 4, 6, 7});
        expected.add(new Object[]{2, 4, 6, 8});
        ValuesComparison.compare(values_1, expected);
    }

    @Test
    public void testCombineCombine_2_2() {
        Values values = createColumn(1);
        values = values.multiply(createColumn(2));
        values = values.multiply(createColumn(3,4));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 2, 3});
        expected.add(new Object[]{1, 2, 4});
        ValuesComparison.compare(values, expected);
    }


    @Test
    public void testCombineCombine_2_1() {
        Values values = createColumn(1);
        values = values.multiply(createColumn(2));
        values = values.multiply(createColumn(3));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 2, 3});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_10() {
        Values values =
                createColumn(1)
                .multiply(createColumn(2,3))
                .multiply(createColumn(3,4,5));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {1, 2, 3});
        expected.add(new Object[] {1, 2, 4});
        expected.add(new Object[] {1, 2, 5});
        expected.add(new Object[] {1, 3, 3});
        expected.add(new Object[] {1, 3, 4});
        expected.add(new Object[] {1, 3, 5});
        ValuesComparison.compare(values, expected);
    }



    @Test
    public void testRun_empty_1() {
        final ArrayList<String> arrayList = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            Values setupOne() {
                return DataFactory.createColumn().multiply(1, 2, 3);
            }

            @TestCase
            @TestData("setupOne")
            public void myTest(String s) {
                arrayList.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertEquals(0, arrayList.size());
        Assert.assertTrue(status.isOK());
        Assert.assertEquals("Passed. test cases: 1; all passed", status.toString());
    }

    @Test
    public void testRun_empty_2() {
        final ArrayList<String> arrayList = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            Values setupOne() {
                return DataFactory.createColumn(1, 2, 3).multiply();
            }

            @TestCase
            @TestData("setupOne")
            public void myTest(String s) {
                arrayList.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertEquals(0, arrayList.size());
        Assert.assertTrue(status.isOK());
        Assert.assertEquals("Passed. test cases: 1; all passed", status.toString());
    }

    @Test
    public void testRun_empty_3() {
        final ArrayList<String> arrayList = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            Values setupOne() {
                return DataFactory.createColumn().multiply();
            }
            @TestCase
            @TestData("setupOne")
            public void myTest(String s) {
                arrayList.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertEquals(0, arrayList.size());
        Assert.assertTrue(status.isOK());
        Assert.assertEquals("Passed. test cases: 1; all passed", status.toString());
    }

    @Test
    public void testRun_empty_4() {
        final ArrayList<String> arrayList = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            Values setupOne() {
                return DataFactory.createColumn().pseudoMultiply(1, 3, 4);
            }
            @TestCase
            @TestData("setupOne")
            public void myTest(String s) {
                arrayList.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertEquals(0, arrayList.size());
        Assert.assertTrue(status.isOK());
        Assert.assertEquals("Passed. test cases: 1; all passed", status.toString());
    }

    @Test
    public void testRun_empty_5() {
        final ArrayList<String> arrayList = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            Values setupOne() {
                return DataFactory.createColumn(5, 6, 7, 8).pseudoMultiply();
            }
            @TestCase
            @TestData("setupOne")
            public void myTest(String s) {
                arrayList.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertEquals(0, arrayList.size());
        Assert.assertTrue(status.isOK());
        Assert.assertEquals("Passed. test cases: 1; all passed", status.toString());
    }

}
