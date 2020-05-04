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
import com.sun.tck.lib.tgf.Values;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.sun.tck.lib.tgf.DataFactory.*;

/**
 *
 */
public class PseudoMultiply {

    @Test
    public void testDiag_enlarge_2_1() {
        Values values = createColumn(1, 2, 7, 8, 9);
        values = values.pseudoMultiply(3, 4);
        List<Object[]> expected = new ArrayList<Object[]>();

        expected.add(new Object[]{1, 3});
        expected.add(new Object[]{2, 4});
        expected.add(new Object[]{7, 3});
        expected.add(new Object[]{8, 4});
        expected.add(new Object[]{9, 3});
        ValuesComparison.compare(values, expected);

    }

    @Test
    public void testDiag_enlarge_2_1_1() {
        Values values = createColumn(1, 2, 7, 8, 9);
        values = values.pseudoMultiply(3, 4);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3});
        expected.add(new Object[]{2, 4});
        expected.add(new Object[]{7, 3});
        expected.add(new Object[]{8, 4});
        expected.add(new Object[]{9, 3});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_12() {
        Values values = createColumn(1).pseudoMultiply(2)
                .pseudoMultiply(3).pseudoMultiply(4).pseudoMultiply(5);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 2, 3, 4, 5});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_13() {
        Values values = createColumn(1).pseudoMultiply(2)
                .pseudoMultiply(3).pseudoMultiply(4).pseudoMultiply(5).pseudoMultiply().pseudoMultiply(createRow());
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_2_1_cachedTheSame() {
        Values values = createColumn(1, 2, 7, 8, 9);
        values = values.pseudoMultiply(3, 4);
        ValuesComparison.checkCachedReturnsTheSame(values);
    }

    @Test
    public void testDiag_cachedTheSame_1() {
        Values values = createColumn(1, 2, 7, 8, 9);
        values = values.pseudoMultiply("12", "a", "b", "c", "e", 4, 435);
        ValuesComparison.checkCachedReturnsTheSame(values);
    }

    @Test
    public void testDiag_cachedTheSame_2() {
        Values values = createColumn(1, 2, 7, 8, 9, 89);
        values = values.pseudoMultiply(31, 42, 27, 18, 79, 89, 56);
        values = values.pseudoMultiply(56, 34, 66, 89, 90);
        ValuesComparison.checkCachedReturnsTheSame(values);
    }

    @Test
    public void testDiag_cachedTheSame_3() {
        Values values = createColumn(1);
        values = values.pseudoMultiply(31);
        ValuesComparison.checkCachedReturnsTheSame(values);
    }

    @Test
    public void testDiag_cachedTheSame_4() {
        Values values = createColumn(1).pseudoMultiply();
        values = values.pseudoMultiply(31).pseudoMultiply(createRow());
        ValuesComparison.checkCachedReturnsTheSame(values);
    }

    @Test
    public void testDiag_enlarge_2_2() {
        Values values = createColumn(3, 4);
        values = values.pseudoMultiply(createColumn(1, 2, 7, 8, 9));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{3, 1});
        expected.add(new Object[]{4, 2});
        expected.add(new Object[]{3, 7});
        expected.add(new Object[]{4, 8});
        expected.add(new Object[]{3, 9});
        ValuesComparison.compare(values, expected);
    }


    @Test
    public void testDiag_0() {
        Values values = createColumn(1);
        values = values.pseudoMultiply(createColumn(4));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_0_shortcut() {
        Values values = pseudoMultiply( createColumn(1), createColumn(4));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_empty_shortcut() {
        Values values = pseudoMultiply(  );
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_null_shortcut() {
        Values values = pseudoMultiply( (Values[])null );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{null});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_single_shortcut() {
        Values values = pseudoMultiply( createColumn(1,2,3) );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1});
        expected.add(new Object[]{2});
        expected.add(new Object[]{3});
        ValuesComparison.compare(values, expected);
    }


    @Test
    public void testDiag_1() {
        Values values = createColumn(1, 2);
        values = values.pseudoMultiply(createColumn(3, 4));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3});
        expected.add(new Object[]{2, 4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_1_shortcut() {
        Values values = pseudoMultiply(createColumn(1, 2), createColumn(3, 4));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3});
        expected.add(new Object[]{2, 4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_1_1_1() {
        Values values = createColumn(1, 2);
        values = values.pseudoMultiply(new Object[][] {{8, 9}, {10, 30}});
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1,  8, 9});
        expected.add(new Object[]{2, 10, 30});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_1_1_2() {
        Values values = createColumn(1, 2);
        values = values.pseudoMultiply(new Object[][] {{8, 9}, {10, 30}, {45, 67}});
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1,  8, 9});
        expected.add(new Object[]{2, 10, 30});
        expected.add(new Object[]{1, 45, 67});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_1_1() {
        Values values = createColumn("a", "a", "a");
        values = values.pseudoMultiply(createColumn("b", "b","b"));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", "b"});
        expected.add(new Object[]{"a", "b"});
        expected.add(new Object[]{"a", "b"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_1_twice() {
        Values values = createColumn(1, 2);
        values = values.pseudoMultiply(createColumn(3, 4));
        values = values.pseudoMultiply(createColumn(5, 6));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3, 5});
        expected.add(new Object[]{2, 4, 6});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_1_twice_shortcut() {
        Values values = pseudoMultiply( createColumn(1, 2), createColumn(3, 4), createColumn(5, 6));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3, 5});
        expected.add(new Object[]{2, 4, 6});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_6() {
        Values values = createColumn("a", "b", "c");
        values = values.pseudoMultiply(createColumn(1, 2, 3));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", 1});
        expected.add(new Object[]{"b", 2});
        expected.add(new Object[]{"c", 3});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_7() {
        Values values = createColumn("a");
        values = values.pseudoMultiply(createColumn(1));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", 1});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_3() {
        Values values = createColumn( 3, 4 );
        values = values.pseudoMultiply(createColumn(  1, 2, 7, 8, 9 ));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{3, 1});
        expected.add(new Object[]{4, 2});
        expected.add(new Object[]{3, 7});
        expected.add(new Object[]{4, 8});
        expected.add(new Object[]{3, 9});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_2_3() {
        Values values = createColumn(1, 2, 7);
        values = values.pseudoMultiply(createColumn(3));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3});
        expected.add(new Object[]{2, 3});
        expected.add(new Object[]{7, 3});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_2_4() {
        Values values = createColumn(1, 2, 7);
        values = values.pseudoMultiply(createRow(3, 56, 78));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3, 56, 78});
        expected.add(new Object[]{2, 3, 56, 78});
        expected.add(new Object[]{7, 3, 56, 78});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_cache() {
        Values values = createColumn(1, 2, 7);
        values = values.pseudoMultiply(createRow(3, 56, 78));
        ValuesComparison.checkCachedReturnsTheSame(values);
    }

    // for the following two we can make TestMD approach -
    // checking the same result through different operations on the same data
    @Test
    public void testDiag_enlarge_1() {
        Values values = createColumn(1, 2);
        values = values.pseudoMultiply(createColumn(3, 4, 5, 6));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3});
        expected.add(new Object[]{2, 4});
        expected.add(new Object[]{1, 5});
        expected.add(new Object[]{2, 6});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_1_shortcut() {
        Values values = pseudoMultiply(createColumn(1, 2), createColumn(3, 4, 5, 6));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3});
        expected.add(new Object[]{2, 4});
        expected.add(new Object[]{1, 5});
        expected.add(new Object[]{2, 6});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_1_twice() {
        Values values = createColumn(1, 2);
        values = values.pseudoMultiply(createColumn(3, 4, 5, 6));
        values = values.pseudoMultiply(createColumn(90, 100, 110, 500, 600, 700));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3, 90});
        expected.add(new Object[]{2, 4, 100});
        expected.add(new Object[]{1, 5, 110});
        expected.add(new Object[]{2, 6, 500});
        expected.add(new Object[]{1, 3, 600});
        expected.add(new Object[]{2, 4, 700});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_1_twice_cache() {
        Values values = createColumn(1, 2);
        values = values.pseudoMultiply(createColumn(3, 4, 5, 6));
        values = values.pseudoMultiply(createColumn(90, 100, 110, 500, 600, 700));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3, 90});
        expected.add(new Object[]{2, 4, 100});
        expected.add(new Object[]{1, 5, 110});
        expected.add(new Object[]{2, 6, 500});
        expected.add(new Object[]{1, 3, 600});
        expected.add(new Object[]{2, 4, 700});
        ValuesComparison.compare(values.createCache(), expected);
    }

    @Test
    public void testDiag_enlarge_1_compareCached() {
        Values values = createColumn(1, 2);
        values = values.pseudoMultiply(createColumn(3, 4, 5, 6));
        values = values.pseudoMultiply(createColumn(90, 100, 110, 500, 600, 700));
        ValuesComparison.checkCachedReturnsTheSame(values);
    }

    @Test
    public void testDiag_enlarge_2_twice() {
        Values values = createColumn(1, 2, 7, 8, 9);
        values = values.pseudoMultiply(createColumn(3, 4));
        values = values.pseudoMultiply(createColumn(1, 2, 3, 4, 5));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {1, 3, 1});
        expected.add(new Object[] {2, 4, 2});
        expected.add(new Object[] {7, 3, 3});
        expected.add(new Object[] {8, 4, 4});
        expected.add(new Object[] {9, 3, 5});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_2_twice_shortcut() {
        Values values = pseudoMultiply(
                createColumn(1, 2, 7, 8, 9),
                createColumn(3, 4),
                createColumn("a"),
                createColumn(1, 2, 3, 4, 5));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {1, 3, "a", 1});
        expected.add(new Object[] {2, 4, "a", 2});
        expected.add(new Object[] {7, 3, "a", 3});
        expected.add(new Object[] {8, 4, "a", 4});
        expected.add(new Object[] {9, 3, "a", 5});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_2_twice_shortcut_1() {
        Values values = pseudoMultiply(
                createColumn(1),
                createColumn(2, 3),
                createColumn(3, 4, 5),
                createColumn(6, 7, 8, 9),
                createColumn(1, 2, 3, 4, 5));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {1, 2, 3, 6, 1});
        expected.add(new Object[] {1, 3, 4, 7, 2});
        expected.add(new Object[] {1, 2, 5, 8, 3});
        expected.add(new Object[] {1, 2, 3, 9, 4});
        expected.add(new Object[] {1, 2, 3, 6, 5});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_2_twice_shortcut_2() {
        Values values = pseudoMultiply(
                createColumn(1, 2, 3, 4, 5),
                createColumn(6, 7, 8, 9),
                createColumn(3, 4, 5),
                createColumn(2, 3),
                createColumn(1));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {1, 6, 3, 2, 1});
        expected.add(new Object[] {2, 7, 4, 3, 1});
        expected.add(new Object[] {3, 8, 5, 2, 1});
        expected.add(new Object[] {4, 9, 3, 3, 1});
        expected.add(new Object[] {5, 6, 4, 2, 1});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_2_twice_shortcut_3() {
        Values values = pseudoMultiply(
                createColumn(1),
                createColumn(2, 3),
                createColumn(3, 4, 5),
                createColumn(6, 7, 8, 9),
                createColumn(1, 2, 3, 4, 5),
                createColumn("a", "b", "c", "d"),
                createColumn("x", "y", "z"),
                createColumn(0, 1),
                createColumn(115)
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {1, 2, 3, 6, 1, "a", "x", 0, 115});
        expected.add(new Object[] {1, 3, 4, 7, 2, "b", "y", 1, 115});
        expected.add(new Object[] {1, 2, 5, 8, 3, "c", "z", 0, 115});
        expected.add(new Object[] {1, 2, 3, 9, 4, "d", "x", 1, 115});
        expected.add(new Object[] {1, 2, 3, 6, 5, "a", "y", 0, 115});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_2_twice_shortcut_4() {
        Values values = pseudoMultiply(
                createColumn(1),
                createColumn(2, 3),
                createColumn(3, 4, 5).pseudoMultiply(6, 7, 8, 9),
                createColumn(1, 2, 3, 4, 5).pseudoMultiply("a", "b", "c", "d").pseudoMultiply("x", "y", "z"),
                createColumn(0, 1).pseudoMultiply(115)
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {1, 2, 3, 6, 1, "a", "x", 0, 115});
        expected.add(new Object[] {1, 3, 4, 7, 2, "b", "y", 1, 115});
        expected.add(new Object[] {1, 2, 5, 8, 3, "c", "z", 0, 115});
        expected.add(new Object[] {1, 3, 3, 9, 4, "d", "x", 1, 115});
        expected.add(new Object[] {1, 2, 3, 6, 5, "a", "y", 0, 115});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_2_5() {
        Values values =
                createColumn(1)
                        .pseudoMultiply(2,3)
                        .pseudoMultiply(4,5,6)
                        .pseudoMultiply(7,8,9,10);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {1, 2, 4, 7});
        expected.add(new Object[] {1, 3, 5, 8});
        expected.add(new Object[] {1, 2, 6, 9});
        expected.add(new Object[] {1, 2, 4, 10});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_2_12() {
        Values values =
                createColumn(1)
                        .pseudoMultiply(2,3)
                        .pseudoMultiply(4,5,6)
                        ;
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {1, 2, 4});
        expected.add(new Object[] {1, 3, 5});
        expected.add(new Object[] {1, 2, 6});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_2_8() {
        Values values =
                createColumn(1)
                        .pseudoMultiply(2,3,4)
                        .pseudoMultiply(7,8,9,10,11,12);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {1, 2, 7});
        expected.add(new Object[] {1, 3, 8});
        expected.add(new Object[] {1, 4, 9});
        expected.add(new Object[] {1, 2, 10});
        expected.add(new Object[] {1, 3, 11});
        expected.add(new Object[] {1, 4, 12});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_2_9() {
        Values values =
                createColumn(1, 0)
                        .pseudoMultiply(2,3,4)
                        .pseudoMultiply(7,8,9,10,11,12);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {1, 2, 7});
        expected.add(new Object[] {0, 3, 8});
        expected.add(new Object[] {1, 4, 9});
        expected.add(new Object[] {1, 2, 10});
        expected.add(new Object[] {0, 3, 11});
        expected.add(new Object[] {1, 4, 12});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_2_10() {
        Values values =
                createColumn(1, 0)
                        .pseudoMultiply(2,3,4)
                        .pseudoMultiply(7,8,9,10);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {1, 2, 7});
        expected.add(new Object[] {0, 3, 8});
        expected.add(new Object[] {1, 4, 9});
        expected.add(new Object[] {1, 2, 10});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_2_10_1() {
        Values values =
                createColumn(1, 0)
                        .pseudoMultiply(2,3,4,5,6)
                        .pseudoMultiply(7,8,9,10,11,12,13);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {1, 2, 7});
        expected.add(new Object[] {0, 3, 8});
        expected.add(new Object[] {1, 4, 9});
        expected.add(new Object[] {0, 5, 10});
        expected.add(new Object[] {1, 6, 11});
        expected.add(new Object[] {1, 2, 12});
        expected.add(new Object[] {0, 3, 13});
        ValuesComparison.compare(values, expected);
    }


    @Test
    public void testDiag_enlarge_2_6() {
        Values values =
                pseudoMultiply(
                    createColumn(1).pseudoMultiply(2, 3),
                    createColumn(4, 5, 6).pseudoMultiply(7, 8, 9, 10)
                );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 2, 4, 7});
        expected.add(new Object[]{1, 3, 5, 8});
        expected.add(new Object[]{1, 2, 6, 9});
        expected.add(new Object[]{1, 3, 4, 10});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_2_7() {
        Values values =
                pseudoMultiply(
                        createColumn(1),
                        createColumn(2, 3).pseudoMultiply(4, 5, 6).pseudoMultiply(7, 8, 9, 10)
                );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 2, 4, 7});
        expected.add(new Object[]{1, 3, 5, 8});
        expected.add(new Object[]{1, 2, 6, 9});
        expected.add(new Object[]{1, 2, 4, 10});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_4() {
        Values values = createColumn("a", "b", "c", "d", "e", "f");
        values = values.pseudoMultiply(createColumn(1, 2, 3));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", 1});
        expected.add(new Object[]{"b", 2});
        expected.add(new Object[]{"c", 3});
        expected.add(new Object[]{"d", 1});
        expected.add(new Object[]{"e", 2});
        expected.add(new Object[]{"f", 3});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_4_1_1() {
        Values values = createColumn("a", "b", "c", "d", "e", "f");
        values = values.pseudoMultiply(1, 2, 3);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", 1});
        expected.add(new Object[]{"b", 2});
        expected.add(new Object[]{"c", 3});
        expected.add(new Object[]{"d", 1});
        expected.add(new Object[]{"e", 2});
        expected.add(new Object[]{"f", 3});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_4_1_2() {
        Values values = createColumn("a", "b", "c", "d", "e", "f");
        values = values.pseudoMultiply(new Object[][] {{1, 1},{2, 2},{3, 3}});
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", 1, 1});
        expected.add(new Object[]{"b", 2, 2});
        expected.add(new Object[]{"c", 3, 3});
        expected.add(new Object[]{"d", 1, 1});
        expected.add(new Object[]{"e", 2, 2});
        expected.add(new Object[]{"f", 3, 3});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_4_1() {
        Values values = createColumn("a", "b", "c", "d", "e", "f", "g", "h");
        values = values.pseudoMultiply(createColumn(1, 2, 3));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", 1});
        expected.add(new Object[]{"b", 2});
        expected.add(new Object[]{"c", 3});
        expected.add(new Object[]{"d", 1});
        expected.add(new Object[]{"e", 2});
        expected.add(new Object[]{"f", 3});
        expected.add(new Object[]{"g", 1});
        expected.add(new Object[]{"h", 2});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_5() {
        Values values = createColumn("a", "b", "c");
        values = values.pseudoMultiply(createColumn(1, 2, 3, 4, 5, 6));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", 1});
        expected.add(new Object[]{"b", 2});
        expected.add(new Object[]{"c", 3});
        expected.add(new Object[]{"a", 4});
        expected.add(new Object[]{"b", 5});
        expected.add(new Object[]{"c", 6});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_6() {
        final Values c1 = createColumn(1, 2, 3);
        final Values c2 = createColumn("a", "b", "c", "d", "e", "f", "g");
        final Values c3 = createColumn(true, false);
        Values values = c1.pseudoMultiply(c2).pseudoMultiply(c3);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, "a", true});
        expected.add(new Object[]{2, "b", false});
        expected.add(new Object[]{3, "c", true});
        expected.add(new Object[]{1, "d", false});
        expected.add(new Object[]{2, "e", true});
        expected.add(new Object[]{3, "f", false});
        expected.add(new Object[]{1, "g", true});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_6_shortcut() {
        final Values c1 = createColumn(1, 2, 3);
        final Values c2 = createColumn("a", "b", "c", "d", "e", "f", "g");
        final Values c3 = createColumn(true, false);
        Values values = pseudoMultiply(c1, c2, c3);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, "a", true});
        expected.add(new Object[]{2, "b", false});
        expected.add(new Object[]{3, "c", true});
        expected.add(new Object[]{1, "d", false});
        expected.add(new Object[]{2, "e", true});
        expected.add(new Object[]{3, "f", false});
        expected.add(new Object[]{1, "g", true});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_8() {
        Values values = createColumn(1, 2, 3, 4, 5, 6, 7);
        values = values.pseudoMultiply(createColumn("a", "b", "c"));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, "a"});
        expected.add(new Object[]{2, "b"});
        expected.add(new Object[]{3, "c"});
        expected.add(new Object[]{4, "a"});
        expected.add(new Object[]{5, "b"});
        expected.add(new Object[]{6, "c"});
        expected.add(new Object[]{7, "a"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_8_1() {
        Values values = createColumn(1, 2, 3, 4, 5, 6, 7);
        values = values.pseudoMultiply(new Object[][] {{"a", "x"}, {"b", "y"}, {"c", "z"}});
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, "a", "x"});
        expected.add(new Object[]{2, "b", "y"});
        expected.add(new Object[]{3, "c", "z"});
        expected.add(new Object[]{4, "a", "x"});
        expected.add(new Object[]{5, "b", "y"});
        expected.add(new Object[]{6, "c", "z"});
        expected.add(new Object[]{7, "a", "x"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_5_1() {
        Values values = createColumn("a", "b", "c");
        values = values.pseudoMultiply(createColumn(1, 2, 3, 4, 5, 6, 7, 8));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", 1});
        expected.add(new Object[]{"b", 2});
        expected.add(new Object[]{"c", 3});
        expected.add(new Object[]{"a", 4});
        expected.add(new Object[]{"b", 5});
        expected.add(new Object[]{"c", 6});
        expected.add(new Object[]{"a", 7});
        expected.add(new Object[]{"b", 8});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_enlarge_5_1_cache() {
        Values values = createColumn("a", "b", "c");
        values = values.pseudoMultiply(createColumn(1, 2, 3, 4, 5, 6, 7, 8));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", 1});
        expected.add(new Object[]{"b", 2});
        expected.add(new Object[]{"c", 3});
        expected.add(new Object[]{"a", 4});
        expected.add(new Object[]{"b", 5});
        expected.add(new Object[]{"c", 6});
        expected.add(new Object[]{"a", 7});
        expected.add(new Object[]{"b", 8});
        ValuesComparison.compare(values.createCache(), expected);
    }

    @Test
    public void testDiag_enlarge_5_1_checkCache() {
        Values values = createColumn("a", "b", "c");
        values = values.pseudoMultiply(createColumn(1, 2, 3, 4, 5, 6, 7, 8));
        ValuesComparison.checkCachedReturnsTheSame(values);
    }


    @Test
    public void test_empty_1() {
        Values values = createColumn(1, 2, 77).pseudoMultiply(createColumn());
        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_1_1() {
        Values values = createColumn(1, 2).pseudoMultiply(createColumn());
        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_1_2() {
        Values values = createColumn(1, 2).pseudoMultiply();
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_1_3() {
        Values values = createRow(1, 2).pseudoMultiply();
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_1_4() {
        Values values = createValues(new int[0][0]).pseudoMultiply();
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_1_5() {
        Values values = createRow(new Object[0][0]).pseudoMultiply();
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_1_6() {
        Values values = createRow(new Object[0][0]).pseudoMultiply().multiply(1, 2);
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_1_7() {
        Values values = createColumn("1", "2").pseudoMultiply().multiply(1, 2);
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_1_8() {
        Values values = createColumn("1", "2").pseudoMultiply().multiply().multiply();
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_1_9() {
        Values values = createColumn("1", "2").pseudoMultiply().multiply().multiply();
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_1_10() {
        Values values = createColumn("1", "2").pseudoMultiply().pseudoMultiply().pseudoMultiply().pseudoMultiply().pseudoMultiply().pseudoMultiply().pseudoMultiply();
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_1_11() {
        Values values = createColumn().pseudoMultiply().pseudoMultiply().pseudoMultiply().pseudoMultiply().pseudoMultiply().pseudoMultiply().pseudoMultiply();
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_1_12() {
        Values values = createColumn().pseudoMultiply().pseudoMultiply(1).pseudoMultiply().pseudoMultiply().pseudoMultiply().pseudoMultiply().pseudoMultiply();
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_1_12_1() {
        Values values = createRow().pseudoMultiply(56, 56).pseudoMultiply(1).pseudoMultiply().pseudoMultiply().pseudoMultiply().pseudoMultiply().pseudoMultiply();
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }


    @Test
    public void test_empty_1_13() {
        Values values = createColumn(123).pseudoMultiply(234).pseudoMultiply(1).pseudoMultiply().pseudoMultiply().pseudoMultiply().pseudoMultiply().pseudoMultiply().pseudoMultiply(1);
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_1_14() {
        Values values = createColumn(123).pseudoMultiply(234).pseudoMultiply(1).pseudoMultiply().pseudoMultiply(1);
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_1_15() {
        Values values = createColumn(123).pseudoMultiply(234).pseudoMultiply(1).pseudoMultiply(new String[0][0]).pseudoMultiply(1);
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_2() {
        Values values = createColumn().pseudoMultiply(createColumn(1, 2, 78));
        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_3() {
        Values values = createRow().pseudoMultiply(createColumn(1, 2, 78));
        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_3_shortcut() {
        Values values = pseudoMultiply(createRow(), createColumn(1, 2, 78));
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_3_1() {
        Values values = pseudoMultiply();
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_3_2() {
        Values values = pseudoMultiply(createColumn());
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_3_3() {
        Values values = pseudoMultiply(createColumn(), createColumn(), createColumn());
        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_3_4() {
        Values values = pseudoMultiply(createColumn(), createColumn(), createColumn(1, 2));
        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_3_5() {
        Values values = pseudoMultiply( createColumn(), createColumn(), createColumn(1), createColumn(4));
        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_3_6() {
        Values values = pseudoMultiply( createColumn(1), createColumn(), createColumn(1));
        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_3_7() {
        Values values = pseudoMultiply( createColumn(1), createRow(), createColumn(1));
        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_3_8() {
        Values values = pseudoMultiply( createColumn(1), createValues(new short[0][0]), createColumn(1));
        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_4() {
        Values values = createColumn(1, 2, 78).pseudoMultiply(createRow());
        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(values, expected);
    }


    @Test
    public void test_empty_5() {
        Values values = createColumn(1, 2, 78).pseudoMultiply(createRow());
        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_6() {
        Values values = createColumn(1, 3, 4).pseudoMultiply(createRow()).pseudoMultiply(createRow());
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_7() {
        Values values = createColumn(5, 6, 7).pseudoMultiply(createColumn()).pseudoMultiply(createColumn());
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_8() {
        Values values =
                createColumn().pseudoMultiply(createColumn(5, 6, 7)).pseudoMultiply(createColumn()).pseudoMultiply(createColumn());
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_9() {
        Values values =
                createColumn().pseudoMultiply(createColumn(5, 6, 7)).pseudoMultiply(createColumn()).pseudoMultiply(5, 9).pseudoMultiply().pseudoMultiply();
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_10() {
        Values values = createColumn(1, 2, 78).pseudoMultiply();
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_empty_11() {
        Values values =
                createRow().pseudoMultiply(createColumn(8, 9, 0)).pseudoMultiply(createRow()).pseudoMultiply(5, 9).pseudoMultiply().pseudoMultiply(createColumn());
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }



}
