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
import com.sun.tck.lib.tgf.DataFactory;
import com.sun.tck.lib.tgf.Values;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.sun.tck.lib.tgf.DataFactory.*;
import static com.sun.tck.lib.tgf.DataFactory.createColumn;

public class Unite {

    @Test
    public void testUnite_0_6() {
        Values values = createColumn().unite(createColumn("a"));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {"a"} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_0_7() {
        Values values = createColumn().unite(createColumn("a", "b"));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {"a"} );
        expected.add( new Object[] {"b"} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_0_8() {
        Values values = createColumn().unite(createColumn("a", "b")).unite();
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {"a"} );
        expected.add( new Object[] {"b"} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_0_8_1() {
        Values values = createColumn().unite(createValues(new Object[][] {{"a", "b"}})).unite();
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {"a", "b"} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_0_9() {
        Values values = createColumn("a").unite();
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {"a"} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_0_10() {
        Values values = createColumn("a", "b").unite();
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {"a"} );
        expected.add( new Object[] {"b"} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_1_1() {
        Values values = createColumn(1, 2, 3);
        values = values.unite(createColumn(4, 5, 6));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {1} );
        expected.add( new Object[] {2} );
        expected.add( new Object[] {3} );
        expected.add( new Object[] {4} );
        expected.add( new Object[] {5} );
        expected.add( new Object[] {6} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_self_1() {
        Values values = createColumn(1, 2, 3);
        values = values.unite(values);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {1} );
        expected.add( new Object[] {2} );
        expected.add( new Object[] {3} );
        expected.add( new Object[] {1} );
        expected.add( new Object[] {2} );
        expected.add( new Object[] {3} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_self_2() {
        Values values = createColumn(1, 3);
        values = DataFactory.unite(values, values, values);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {1} );
        expected.add( new Object[] {3} );
        expected.add( new Object[] {1} );
        expected.add( new Object[] {3} );
        expected.add( new Object[] {1} );
        expected.add( new Object[] {3} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_1_1_multy() {
        Values values = createColumn(1, 2, 3);
        values = unite(values, createColumn(4, 5, 6));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {1} );
        expected.add( new Object[] {2} );
        expected.add( new Object[] {3} );
        expected.add( new Object[] {4} );
        expected.add( new Object[] {5} );
        expected.add( new Object[] {6} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_1_1_cache() {
        Values values = createColumn(1, 2, 3);
        values = values.unite(createColumn(4, 5, 6));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {1} );
        expected.add( new Object[] {2} );
        expected.add( new Object[] {3} );
        expected.add( new Object[] {4} );
        expected.add( new Object[] {5} );
        expected.add( new Object[] {6} );
        ValuesComparison.compare(values.createCache(), expected);
    }

    @Test
    public void testUnite_1_1_1() {
        Values values = createColumn(1, 2, 3);
        values = values.unite(4, 5, 6);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {1} );
        expected.add( new Object[] {2} );
        expected.add( new Object[] {3} );
        expected.add( new Object[] {4} );
        expected.add( new Object[] {5} );
        expected.add( new Object[] {6} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_0_1() {
        Values values = createValues(new Object[][]{{3, 4}});
        values = values.unite(createValues(new Object[][]{{3, 4}}));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {3, 4} );
        expected.add( new Object[] {3, 4} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_0_1_1() {
        Values values = createRow(3, 4);
        values = values.unite(createRow(3, 4));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {3, 4} );
        expected.add( new Object[] {3, 4} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_0_1_1_cached() {
        Values values = createRow(3, 4);
        values = values.unite(createRow(3, 4));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {3, 4} );
        expected.add( new Object[] {3, 4} );
        ValuesComparison.compare(values.createCache(), expected);
    }

    @Test
    public void testUnite_0_3() {
        Values values = createValues(new Object[][]{{3, 4}});
        values = values.unite(new Object[][]{{3, 4}, {8, 4}});
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {3, 4} );
        expected.add( new Object[] {3, 4} );
        expected.add( new Object[] {8, 4} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_0_3_multy() {
        Values values = unite(
                createRow(3, 4),
                createRow(3, 4),
                createRow(8, 4)
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {3, 4} );
        expected.add( new Object[] {3, 4} );
        expected.add( new Object[] {8, 4} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_0_3_multy_0() {
        Values values = unite(
                createRow(3, 4)
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {3, 4} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_0_3_multy_null() {
        Values values = unite((Values[]) null);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {null} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_0_3_multy_empty() {
        Values values = unite();
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_0_8_multy_empty() {
        Values values = unite(createColumn(), createColumn());
        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_0_9_multy_empty() {
        Values values = unite(createRow(), createRow());
        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_0_4() {
        Values values = createValues(new Object[][]{{3, 4, 1}});
        values = values.unite( new Object[][] {{3, 4, 2}, {8, 4, 0}} );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {3, 4, 1} );
        expected.add( new Object[] {3, 4, 2} );
        expected.add( new Object[] {8, 4, 0} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_0_4_1() {
        Values values = createRow(3, 4, 1);
        values = values.unite( new Object[][] {{3, 4, 2}, {8, 4, 0}} );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {3, 4, 1} );
        expected.add( new Object[] {3, 4, 2} );
        expected.add( new Object[] {8, 4, 0} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_0_4_1_multy() {
        Values values = DataFactory.unite(
                createRow(3, 4, 1),
                createValues(new Object[][]{{3, 4, 2}, {8, 4, 0}})
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {3, 4, 1} );
        expected.add( new Object[] {3, 4, 2} );
        expected.add( new Object[] {8, 4, 0} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_0_4_1_multy_1() {
        Values values = DataFactory.unite(
                createValues(new Object[][]{{3, 4, 2}, {8, 4, 0}})
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {3, 4, 2} );
        expected.add( new Object[] {8, 4, 0} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_0_5() {
        Values values = createValues(new Object[][]{{3, 4, 1}});
        values = values.unite( new Object[][] {{3, 4, 2}, {8, 4, 0}} );
        values = values.unite( new Object[][] {{0, 0, 1}, {1, 0, 0}} );
        values = values.unite( new Object[][] {{"1", "2", "3"}, {"3", "2", "1"}} );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {3, 4, 1} );
        expected.add( new Object[] {3, 4, 2} );
        expected.add( new Object[] {8, 4, 0} );
        expected.add( new Object[] {0, 0, 1} );
        expected.add( new Object[] {1, 0, 0} );
        expected.add( new Object[] {"1", "2", "3"} );
        expected.add( new Object[] {"3", "2", "1"} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_0_5_multy() {
        Values values = unite(
                createValues(new Object[][]{{3, 4, 1}}),
                createValues(new Object[][]{{3, 4, 2}, {8, 4, 0}}),
                createValues(new Object[][]{{0, 0, 1}, {1, 0, 0}}),
                createValues(new Object[][]{{"1", "2", "3"}, {"3", "2", "1"}})
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {3, 4, 1} );
        expected.add( new Object[] {3, 4, 2} );
        expected.add( new Object[] {8, 4, 0} );
        expected.add( new Object[] {0, 0, 1} );
        expected.add( new Object[] {1, 0, 0} );
        expected.add( new Object[] {"1", "2", "3"} );
        expected.add( new Object[] {"3", "2", "1"} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_0_5_cached() {
        Values values = createRow(3, 4, 1);
        values = values.unite( new Object[][] {{3, 4, 2}, {8, 4, 0}} );
        values = values.unite( new Object[][] {{0, 0, 1}, {1, 0, 0}} );
        values = values.unite( new Object[][] {{"1", "2", "3"}, {"3", "2", "1"}} );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {3, 4, 1} );
        expected.add( new Object[] {3, 4, 2} );
        expected.add( new Object[] {8, 4, 0} );
        expected.add( new Object[] {0, 0, 1} );
        expected.add( new Object[] {1, 0, 0} );
        expected.add( new Object[] {"1", "2", "3"} );
        expected.add( new Object[] {"3", "2", "1"} );
        ValuesComparison.compare(values.createCache(), expected);
    }

    @Test
    public void testUnite_0_2() {
        Values values = createValues(new Object[][]{{3, 4}});
        values = values.unite(new Object[][] {{3, 4}});
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {3, 4} );
        expected.add( new Object[] {3, 4} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_1() {
        Values values = createValues(new Object[][]{{1, 2}});
        values = values.unite(createValues(new Object[][] {{3, 4}}));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 2});
        expected.add(new Object[]{3, 4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_1_2() {
        Values values = createValues(new Object[][]{{1, 2}});
        values = values.unite(new Object[][] {{3, 4}});
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 2});
        expected.add(new Object[]{3, 4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_1_3() {
        Values values = createRow(1, 2);
        values = values.unite(createRow(3, 4));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 2});
        expected.add(new Object[]{3, 4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_2() {
        Values values = createColumn(1);
        values = values.unite(createColumn(4));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1});
        expected.add(new Object[]{4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_2_multy() {
        Values values = unite(createColumn(1), createColumn(4));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1});
        expected.add(new Object[]{4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_shortcut_SingleItem() {
        Values values = unite(createColumn(1, 4));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1});
        expected.add(new Object[]{4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_2_multy_2() {
        Values values = unite(createColumn(1));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_2_multy_3() {
        Values values = unite(createRow(1, 4));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1,4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_2_multy_4() {
        Values values = unite(
                createRow(1, 4),
                createRow(6, 7),
                createRow(4, 9),
                createRow(2, 7)
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1,4});
        expected.add(new Object[]{6,7});
        expected.add(new Object[]{4,9});
        expected.add(new Object[]{2,7});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_2_multy_5() {
        Values values = unite(
                createColumn(1, 4),
                createColumn(6, 7),
                createColumn(4, 9)
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1});
        expected.add(new Object[]{4});
        expected.add(new Object[]{6});
        expected.add(new Object[]{7});
        expected.add(new Object[]{4});
        expected.add(new Object[]{9});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_2_multy_6() {
        Values values = unite(
                createColumn(1, 4),
                createColumn(6, 7),
                createColumn(4, 9)
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1});
        expected.add(new Object[]{4});
        expected.add(new Object[]{6});
        expected.add(new Object[]{7});
        expected.add(new Object[]{4});
        expected.add(new Object[]{9});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_2_multy_7() {
        Values values = unite(
                createRow(1, 4),
                createColumn(6, 7),
                createRow(4, 9)
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 4});
        expected.add(new Object[]{6});
        expected.add(new Object[]{7});
        expected.add(new Object[]{4, 9});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_2_multy_7_1() {
        Values values =
                createRow(1,4).unite(4);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 4});
        expected.add(new Object[]{4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_2_multy_7_2() {
        Values values =
                createRow(1).unite(4);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1});
        expected.add(new Object[]{4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_2_multy_8() {
        Values values = unite(
                createRow(1, 4),
                createColumn(6, 7).multiply(8, 9),
                createRow(4, 9)
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 4});
        expected.add(new Object[]{6, 8});
        expected.add(new Object[]{6, 9});
        expected.add(new Object[]{7, 8});
        expected.add(new Object[]{7, 9});
        expected.add(new Object[]{4, 9});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_3() {
        Values values =
                createColumn(1,6)
                .multiply(8,9)
                .unite(
                        createColumn(10, 11).multiply(45, 67));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 8});
        expected.add(new Object[]{1, 9});
        expected.add(new Object[]{6, 8});
        expected.add(new Object[]{6, 9});
        expected.add(new Object[]{10, 45});
        expected.add(new Object[]{10, 67});
        expected.add(new Object[]{11, 45});
        expected.add(new Object[]{11, 67});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_3_multy() {
        Values values = unite(
                createColumn(1, 6).multiply(8, 9),
                createColumn(10, 11).multiply(45, 67)
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 8});
        expected.add(new Object[]{1, 9});
        expected.add(new Object[]{6, 8});
        expected.add(new Object[]{6, 9});
        expected.add(new Object[]{10, 45});
        expected.add(new Object[]{10, 67});
        expected.add(new Object[]{11, 45});
        expected.add(new Object[]{11, 67});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_3_multy_cached() {
        Values values = unite(
                createColumn(1, 6).multiply(8, 9),
                createColumn(10, 11).multiply(45, 67)
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 8});
        expected.add(new Object[]{1, 9});
        expected.add(new Object[]{6, 8});
        expected.add(new Object[]{6, 9});
        expected.add(new Object[]{10, 45});
        expected.add(new Object[]{10, 67});
        expected.add(new Object[]{11, 45});
        expected.add(new Object[]{11, 67});
        ValuesComparison.compare(values.createCache(), expected);
    }

    @Test
    public void testUnite_3_cached() {
        Values values =
                createColumn(1,6)
                .multiply(8,9)
                .unite(
                        createColumn(10, 11).multiply(45, 67));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 8});
        expected.add(new Object[]{1, 9});
        expected.add(new Object[]{6, 8});
        expected.add(new Object[]{6, 9});
        expected.add(new Object[]{10, 45});
        expected.add(new Object[]{10, 67});
        expected.add(new Object[]{11, 45});
        expected.add(new Object[]{11, 67});
        ValuesComparison.compare(values.createCache(), expected);
    }

    @Test
    public void testUnite_4() {
        Values values = createColumn(1, 6);
        values = values.multiply(createColumn(4,8));
        values = values.unite(createValues(new Object[][] {{1, 2}} ));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1,4});
        expected.add(new Object[]{1,8});
        expected.add(new Object[]{6,4});
        expected.add(new Object[]{6,8});
        expected.add(new Object[]{1,2});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_4_1() {
        Values values = createColumn(1, 6);
        values = values.multiply(4,8);
        values = values.unite(new Object[][] {{1, 2}} );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1,4});
        expected.add(new Object[]{1,8});
        expected.add(new Object[]{6,4});
        expected.add(new Object[]{6,8});
        expected.add(new Object[]{1,2});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_5() {
        Values values = createColumn(1, 2, 3);
        values = values.multiply(4,5);
        values = values.unite(
                createColumn("a", "b", "c").multiply(9, 0) );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 4});
        expected.add(new Object[]{1, 5});
        expected.add(new Object[]{2, 4});
        expected.add(new Object[]{2, 5});
        expected.add(new Object[]{3, 4});
        expected.add(new Object[]{3, 5});
        expected.add(new Object[]{"a", 9});
        expected.add(new Object[]{"a", 0});
        expected.add(new Object[]{"b", 9});
        expected.add(new Object[]{"b", 0});
        expected.add(new Object[]{"c", 9});
        expected.add(new Object[]{"c", 0});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_5_1() {
        Values values = createColumn(1, 2, 3);
        values = values.multiply(4,5);
        values = values.unite(
                createColumn("a", "b", "c").multiply(9, 0) );
        values = values.unite(new Object[][] {{"x", 2},{"t", 3},{"v", 4}});
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 4});
        expected.add(new Object[]{1, 5});
        expected.add(new Object[]{2, 4});
        expected.add(new Object[]{2, 5});
        expected.add(new Object[]{3, 4});
        expected.add(new Object[]{3, 5});
        expected.add(new Object[]{"a", 9});
        expected.add(new Object[]{"a", 0});
        expected.add(new Object[]{"b", 9});
        expected.add(new Object[]{"b", 0});
        expected.add(new Object[]{"c", 9});
        expected.add(new Object[]{"c", 0});
        expected.add(new Object[]{"x", 2});
        expected.add(new Object[]{"t", 3});
        expected.add(new Object[]{"v", 4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testUnite_6_1() {
        Values v1 = createColumn(0, 1);
        Values v2 = createColumn( 2, 3 );
        Values result   = v1.unite(v2).multiply( v1 );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {0, 0});
        expected.add(new Object[] {0, 1});
        expected.add(new Object[] {1, 0});
        expected.add(new Object[] {1, 1});
        expected.add(new Object[] {2, 0});
        expected.add(new Object[] {2, 1});
        expected.add(new Object[] {3, 0});
        expected.add(new Object[] {3, 1});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_6_1_multy() {
        Values v1 = createColumn(0, 1);
        Values v2 = createColumn( 2, 3 );
        Values result = unite(
                v1, v2
        ).multiply(v1);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {0, 0});
        expected.add(new Object[] {0, 1});
        expected.add(new Object[] {1, 0});
        expected.add(new Object[] {1, 1});
        expected.add(new Object[] {2, 0});
        expected.add(new Object[] {2, 1});
        expected.add(new Object[] {3, 0});
        expected.add(new Object[] {3, 1});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_6_2() {
        Values v1 = createColumn(0, 1);
        Values v2 = createColumn( 2, 3 );
        Values result   = v1.multiply(v1.unite(v2));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{0, 0});
        expected.add(new Object[]{0, 1});
        expected.add(new Object[]{0, 2});
        expected.add(new Object[]{0, 3});
        expected.add(new Object[]{1, 0});
        expected.add(new Object[]{1, 1});
        expected.add(new Object[]{1, 2});
        expected.add(new Object[]{1, 3});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_6_2_multy() {
        Values v1 = createColumn(0, 1);
        Values v2 = createColumn( 2, 3 );
        Values result   = v1.multiply(unite(v1, v2));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{0, 0});
        expected.add(new Object[]{0, 1});
        expected.add(new Object[]{0, 2});
        expected.add(new Object[]{0, 3});
        expected.add(new Object[]{1, 0});
        expected.add(new Object[]{1, 1});
        expected.add(new Object[]{1, 2});
        expected.add(new Object[]{1, 3});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_Multi_1() {
        Values v1 = createColumn( 0, 1 );
        Values v2 = createColumn( 2, 3 );

        Values result   = v1.multiply(unite(v1, v2));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{0, 0});
        expected.add(new Object[]{0, 1});
        expected.add(new Object[]{0, 2});
        expected.add(new Object[]{0, 3});
        expected.add(new Object[]{1, 0});
        expected.add(new Object[]{1, 1});
        expected.add(new Object[]{1, 2});
        expected.add(new Object[]{1, 3});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_1() {
        Values result = createColumn( 0, 1 ).unite(createColumn());
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{0});
        expected.add(new Object[]{1});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_Array_1() {
        Values result = createColumn( 0, 1 ).unite(createColumn(new Object[0]));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{0});
        expected.add(new Object[]{1});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_Array_2() {
        Values result = createColumn( 0, 1, 89 ).unite(createColumn(new Object[0]));
        List<Object[]> expected = new ArrayList<>();
        expected.add(new Object[]{0});
        expected.add(new Object[]{1});
        expected.add(new Object[]{89});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_Array_3() {
        Values result = createColumn( 0, 1, 89 ).unite(createColumn(new Object[0])).unite(88);
        List<Object[]> expected = new ArrayList<>();
        expected.add(new Object[]{0});
        expected.add(new Object[]{1});
        expected.add(new Object[]{89});
        expected.add(new Object[]{88});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_Array_3_1() {
        Values result = createColumn( 0, 1, 89 ).unite(createColumn()).unite(88);
        List<Object[]> expected = new ArrayList<>();
        expected.add(new Object[]{0});
        expected.add(new Object[]{1});
        expected.add(new Object[]{89});
        expected.add(new Object[]{88});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_Array_3_2() {
        Values result = createColumn( 0, 1, 89 ).unite(createRow()).unite(88);
        List<Object[]> expected = new ArrayList<>();
        expected.add(new Object[]{0});
        expected.add(new Object[]{1});
        expected.add(new Object[]{89});
        expected.add(new Object[]{88});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_Array_4() {
        Values result = createColumn(55 ).unite(createColumn(new Object[0])).unite(88);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{55});
        expected.add(new Object[]{88});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_Array_4_4() {
        Values result = createColumn(55 ).unite(createColumn((Object[])new Object[0][0])).unite(88);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{55});
        expected.add(new Object[]{88});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_Array_4_5() {
        Values result = createColumn(55 ).unite().unite(88);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{55});
        expected.add(new Object[]{88});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_Array_8() {
        Values result = createRow(5, 5).unite(createColumn(new Object[0])).unite(88);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{5, 5});
        expected.add(new Object[]{88});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_2() {
        Values result   = createColumn(0, 1).unite(createColumn()).unite(createColumn());
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{0});
        expected.add(new Object[]{1});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_2_1() {
        Values result   = createRow().unite(createColumn(0, 1)).unite(createColumn()).unite(createColumn());
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{0});
        expected.add(new Object[]{1});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_2_2() {
        Values result   = unite().unite(createRow()).unite(createColumn(0, 1)).unite(createColumn()).unite(createColumn());
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{0});
        expected.add(new Object[]{1});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_2_3() {
        Values result   = unite().unite().unite(createColumn(0, 1)).unite(createColumn()).unite(createColumn());
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{0});
        expected.add(new Object[]{1});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_2_4() {
        Values result   = createColumn().unite().unite().unite(createColumn(0, 1)).unite(createColumn()).unite(createColumn());
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{0});
        expected.add(new Object[]{1});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_2_5() {
        Values result   = createRow().unite().unite().unite(createColumn(0, 1)).unite(createColumn()).unite(createColumn());
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{0});
        expected.add(new Object[]{1});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_3() {
        Values result   = createColumn(4, 1).unite(createColumn()).unite(createColumn());
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{4});
        expected.add(new Object[]{1});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_4() {
        Values result = createColumn().unite(createColumn(120, 111));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{120});
        expected.add(new Object[]{111});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_5() {
        Values result = unite(
                createColumn(50),
                createColumn(),
                createColumn(),
                createColumn(51),
                createColumn()
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{50});
        expected.add(new Object[]{51});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_6() {
        Values result = unite(
                createColumn(),
                createColumn(),
                createColumn()
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_7() {
        Values result = unite(
                createColumn( 5 ),
                createColumn(),
                createColumn(),
                createColumn( 6 ),
                createColumn()
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{5});
        expected.add(new Object[]{6});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_8() {
        Values result = unite(
                createColumn( 5 ),
                createRow()
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{5});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_9() {
        Values result = unite(
                createRow(5),
                createRow()
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{5});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_10() {
        Values result = unite(
                createRow(),
                createRow()
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_11() {
        Values result = unite(
                createColumn(),
                createRow(),
                createColumn()
        );
        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_11_1() {
        Values result = unite(
                createColumn(),
                createRow(),
                createColumn(),
                createValues(new boolean[0][0])
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_11_2() {
        Values result = unite(
                createColumn(),
                createRow(),
                createColumn(),
                createValues(new Object[0][0])
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_12() {
        Values result = unite(
                createColumn(),
                createColumn(45),
                createColumn(),
                createRow(48),
                createColumn(),
                createRow(5, 6),
                createRow(),
                createColumn()
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{45});
        expected.add(new Object[]{48});
        expected.add(new Object[]{5, 6});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_13() {
        Values result = unite(
                createColumn(5, 6),
                createRow(),
                createRow(5),
                createColumn(3)
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{5});
        expected.add(new Object[]{6});
        expected.add(new Object[]{5});
        expected.add(new Object[]{3});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_14() {
        Values result = unite ( createColumn() );
        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_with_empty_15() {
        Values result = unite ( createRow() );
        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testUnite_7() {
        Values correctValues = createColumn( 0, 1 );
        Values wrongValues = createColumn( 2, 3 );
        Values wrongAndCorrect = wrongValues.unite(correctValues);
        Values redWrong   = wrongValues.multiply(wrongAndCorrect).multiply(wrongAndCorrect);
        Values greenWrong = wrongAndCorrect.multiply(wrongValues).multiply(wrongAndCorrect);
        Values blueWrong  = wrongAndCorrect.multiply(wrongAndCorrect).multiply(wrongValues);
        Values result = redWrong.unite(greenWrong).unite(blueWrong);
        for (Object[] objects : result) {
            System.out.println("Arrays.toString() = " + Arrays.toString(objects));
        }
    }

}
