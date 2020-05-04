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

import com.oracle.tck.lib.autd2.unittests.ValuesComparison;
import com.sun.tck.lib.tgf.ReducingIterator;
import com.sun.tck.lib.tgf.Values;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.sun.tck.lib.tgf.DataFactory.createColumn;
import static com.sun.tck.lib.tgf.DataFactory.createRow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 *
 */
public class Reduce {

    @Test(expected = IllegalArgumentException.class)
    public void testReduce_IAE_0() {
        createColumn(1, 2, 3).reduceTo(-100);
    }

    @Test
    public void testReduce_IAE_Message() {
        try {
            createColumn(1, 2, 3).reduceTo(-100);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(),
                    ReducingIterator.MESSAGE_INVALID_RANGE
            );
        }
    }

    @Test
    public void testReduce_IAE_Message_1() {
        try {
            createColumn(1, 2, 3).reduceTo(0);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(),
                    ReducingIterator.MESSAGE_INVALID_RANGE
            );
        }
    }

    @Test
    public void testReduce_NoDataLeft_Message_1() {
        Values values = createColumn(1, 2, 3).reduceTo(0.00001);
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }


    @Test
    public void testReduce_IAE_1() {
        try {
            Values values = createColumn(1, 2, 3).reduceTo(0);
            ValuesComparison.compare(values, new ArrayList<Object[]>());
            fail("IAE not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals(ReducingIterator.MESSAGE_INVALID_RANGE, e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReduce_IAE_2() {
        createColumn(1, 2, 3).reduceTo(1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReduce_IAE_3() {
        createColumn(1, 2, 3).reduceTo(100.0);
    }

    // todo test message
    @Test
    public void testReduce_toZero_1() {
        Values values =
                createColumn(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).reduceTo(0.0000001);
        ValuesComparison.compare(values, new ArrayList<Object[]>());

    }

    @Test
    public void testReduce_toZero_2() {
        Values values =
                createColumn(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).reduceTo(0.05);
        ValuesComparison.compare(values, new ArrayList<Object[]>());

    }

    @Test
    public void testReduce_1_1() {
        Values values =
                createColumn(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).reduceTo(0.09);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{7});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testReduce_2() {
        Values values =
                createColumn(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).reduceTo(0.1);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{6});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testReduce_3() {
        Values values =
                createColumn(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).reduceTo(0.15);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testReduce_4() {
        Values values =
                createColumn(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).reduceTo(0.19);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{4});
        expected.add(new Object[]{9});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testReduce_5() {
        Values values =
                createColumn(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).reduceTo(0.2);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{3});
        expected.add(new Object[]{8});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testReduce_5_1() {
        Values values =
                createColumn(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).reduceTo(0.3);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{3});
        expected.add(new Object[]{6});
        expected.add(new Object[]{9});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testReduce_6() {
        Values values =
                createColumn(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).reduceTo(0.5);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{2});
        expected.add(new Object[]{4});
        expected.add(new Object[]{6});
        expected.add(new Object[]{8});
        expected.add(new Object[]{10});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testReduce_6_1() {
        Values values =
                createColumn(1, 2, 3, 4).reduceTo(0.5);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{2});
        expected.add(new Object[]{4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testReduce_6_8() {
        Values values =
                createColumn(1, 2, 3, 4).reduceTo(0.2);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{3});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testReduce_6_9() {
        Values values =
                createColumn(1, 2, 3, 4).reduceTo(0.25);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{3});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testReduce_6_10() {
        Values values =
                createColumn(1, 2, 3, 4).reduceTo(0.43);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{2});
        expected.add(new Object[]{4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testReduce_6_3() {
        Values values =
                createColumn(1, 2, 3, 4).reduceTo(0.6);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{2});
        expected.add(new Object[]{3});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testReduce_6_4() {
        Values values =
                createColumn(1, 2, 3, 4).reduceTo(0.70);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{2});
        expected.add(new Object[]{3});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testReduce_6_5() {
        Values values =
                createColumn(1, 2, 3, 4).reduceTo(0.75);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{2});
        expected.add(new Object[]{3});
        expected.add(new Object[]{4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testReduce_6_7() {
        Values values =
                createColumn(1, 2, 3, 4).reduceTo(0.76);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1});
        expected.add(new Object[]{2});
        expected.add(new Object[]{3});
        expected.add(new Object[]{4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testReduce_6_6() {
        Values values =
                createColumn(1, 2, 3, 4).reduceTo(0.8);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1});
        expected.add(new Object[]{2});
        expected.add(new Object[]{3});
        expected.add(new Object[]{4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testReduce_6_2() {
        Values values =
                createColumn(100, 200).reduceTo(0.5);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{200});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testReduce_7() {
        Values values =
                createColumn(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).reduceTo(0.6);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{2});
        expected.add(new Object[]{3});
        expected.add(new Object[]{5});
        expected.add(new Object[]{7});
        expected.add(new Object[]{8});
        expected.add(new Object[]{10});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testReduce_8() {
        Values values =
                createColumn(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).reduceTo(0.9);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{2});
        expected.add(new Object[]{3});
        expected.add(new Object[]{4});
        expected.add(new Object[]{5});
        expected.add(new Object[]{6});
        expected.add(new Object[]{7});
        expected.add(new Object[]{8});
        expected.add(new Object[]{9});
        expected.add(new Object[]{10});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testReduce_9() {
        Values values =
                createColumn(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).reduceTo(0.98);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1});
        expected.add(new Object[]{2});
        expected.add(new Object[]{3});
        expected.add(new Object[]{4});
        expected.add(new Object[]{5});
        expected.add(new Object[]{6});
        expected.add(new Object[]{7});
        expected.add(new Object[]{8});
        expected.add(new Object[]{9});
        expected.add(new Object[]{10});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testReduce_10() {
        Values values =
                createColumn(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).reduceTo(0.95);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1});
        expected.add(new Object[]{2});
        expected.add(new Object[]{3});
        expected.add(new Object[]{4});
        expected.add(new Object[]{5});
        expected.add(new Object[]{6});
        expected.add(new Object[]{7});
        expected.add(new Object[]{8});
        expected.add(new Object[]{9});
        expected.add(new Object[]{10});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testReduce_11() {
        Values values =
                createColumn("a", "b", "c").reduceTo(0.33333333);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"c"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testReduce_12() {
        Values values =
                createColumn("a", "b", "c").reduceTo(0.666666666);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"b"});
        expected.add(new Object[]{"c"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testReduce_empty_1() {
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(createColumn().reduceTo(0.25), expected);
    }

    @Test
    public void testReduce_empty_2() {
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(createColumn().reduceTo(0.999), expected);
    }

    @Test
    public void testReduce_empty_3() {
        try {
            List<Object[]> expected = new ArrayList<Object[]>();
            ValuesComparison.compare(createColumn().reduceTo(0.0), expected);
            fail("IAE not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals(ReducingIterator.MESSAGE_INVALID_RANGE, e.getMessage());
        }
    }

    @Test
    public void testReduce_empty_4() {
        try {
            List<Object[]> expected = new ArrayList<Object[]>();
            ValuesComparison.compare(createColumn().reduceTo(1.0), expected);
            fail("IAE not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals(ReducingIterator.MESSAGE_INVALID_RANGE, e.getMessage());
        }
    }

    @Test
    public void testReduce_empty_5() {
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(createRow().reduceTo(0.05), expected);
    }

    @Test
    public void testReduce_empty_6() {
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(createRow().reduceTo(0.999), expected);
    }



}
