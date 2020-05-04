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

import com.oracle.tck.lib.autd2.unittests.TestObject;
import com.oracle.tck.lib.autd2.unittests.ValuesComparison;
import com.sun.tck.lib.tgf.DataFactory;
import com.sun.tck.lib.tgf.Values;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Intersect {

    @Test
    public void testIntersect_1() {
        Values values_1 = DataFactory.createColumn(1, 2, 3, 4, 5);
        Values values_2 = DataFactory.createColumn(3,4,5,6,7);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {3} );
        expected.add( new Object[] {4} );
        expected.add( new Object[] {5} );
        Values result = values_1.intersect(values_2);
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testIntersect_1_cache() {
        Values values_1 = DataFactory.createColumn(1,2,3,4,5);
        Values values_2 = DataFactory.createColumn(3,4,5,6,7);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {3} );
        expected.add( new Object[] {4} );
        expected.add( new Object[] {5} );
        Values result = values_1.intersect(values_2);
        ValuesComparison.compare(result.createCache(), expected);
    }

    @Test
    public void testIntersect_1_3() {
        Values values_1 = DataFactory.createColumn(3,4,5);
        Values values_2 = DataFactory.createColumn(8);
        List<Object[]> expected = new ArrayList<Object[]>();
        Values result = values_1.intersect(values_2);
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testIntersect_1_4() {
        Values values_1 = DataFactory.createColumn(2);
        Values values_2 = DataFactory.createColumn(1,2,3);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] { 2 } );
        Values result = values_1.intersect(values_2);
        ValuesComparison.compare(result, expected);
    }


    @Test
    public void testIntersect_1_1() {
        Values values_1 = DataFactory.createColumn(new TestObject("one"), new TestObject("two"), new TestObject("three"));
        Values values_2 = DataFactory.createColumn(new TestObject("three"), new TestObject("four"), new TestObject("one"));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {new TestObject("one")} );
        expected.add( new Object[] {new TestObject("three")} );
        Values result = values_1.intersect(values_2);
        ValuesComparison.compare(result, expected);
    }


    @Test
    public void testIntersect_1_2() {
        Values values_1 = DataFactory.createColumn(
                new TestObject("one"), new TestObject("two"), new TestObject("three"));
        Values values_2 = DataFactory.createColumn(
                new TestObject("five"), new TestObject("six"), new TestObject("seven"));
        List<Object[]> expected = new ArrayList<Object[]>();
        Values result = values_1.intersect(values_2);
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testIntersect_1_5() {
        Values values_1 = DataFactory.createValues(new Object[][] {
                {new TestObject("one"), new TestObject("two")},
                {new TestObject("three"), new TestObject("three")}
        });
        Values result = values_1.intersect(new Object[][] {
                {new TestObject("one_"), new TestObject("two_")},
                {new TestObject("three_"), new TestObject("three_")}
        });
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testIntersect_1_6() {
        Values values_1 = DataFactory.createValues(new Object[][] {
                {new TestObject("one"), new TestObject("two")},
                {new TestObject("three"), new TestObject("three")}
        });
        Values result = values_1.intersect(new Object[][] {
                {new TestObject("one"), new TestObject("two")},
                {new TestObject("three_"), new TestObject("three_")}
        });
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {new TestObject("one"), new TestObject("two")} );
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testIntersect_1_7() {
        Values values_1 = DataFactory.createValues(new Object[][] {
                {new TestObject("one"), new TestObject("two")},
                {new TestObject("three"), new TestObject("four")},
                {new TestObject("five"), new TestObject("six")}
        });
        Values result = values_1.intersect(new Object[][] {
                {new TestObject("one"), new TestObject("two")},
                {new TestObject("three_"), new TestObject("three_")}
        });
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {new TestObject("one"), new TestObject("two")} );
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testIntersect_1_7_cache() {
        Values values_1 = DataFactory.createValues(new Object[][] {
                {new TestObject("one"), new TestObject("two")},
                {new TestObject("three"), new TestObject("four")},
                {new TestObject("five"), new TestObject("six")}
        });
        Values result = values_1.intersect(new Object[][] {
                {new TestObject("one"), new TestObject("two")},
                {new TestObject("three_"), new TestObject("three_")}
        });
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {new TestObject("one"), new TestObject("two")} );
        ValuesComparison.compare(result.createCache(), expected);
    }

    @Test
    public void testIntersect_1_8() {
        Values values_1 = DataFactory.createColumn(
                new TestObject("one"), new TestObject("two"), new TestObject("three"), new TestObject("four")
        ).pseudoMultiply(
                new TestObject("five"), new TestObject("six"), new TestObject("seven"), new TestObject("eight")
        );
        Values result = values_1.intersect(new Object[][] {
                { new TestObject("two"), new TestObject("six") },
                { new TestObject("open"), new TestObject("closed") },
                { new TestObject("one"), new TestObject("five") }
        });
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {new TestObject("one"), new TestObject("five")} );
        expected.add( new Object[] {new TestObject("two"), new TestObject("six")} );
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testIntersect_0() {
        Values values_1 = DataFactory.createColumn(1,2,3,4,5);
        Values values_2 = DataFactory.createColumn(6,7,8);
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values_1.intersect(values_2), expected);
    }

    @Test
    public void testIntersect_2() {
        Values values_1 = DataFactory.createColumn("one");
        Values values_2 = DataFactory.createColumn("one");
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {"one"} );
        ValuesComparison.compare(values_1.intersect(values_2), expected);
    }

    @Test
    public void testIntersect_3() {
        Values values_1 = DataFactory.createColumn("one");
        Values values_2 = DataFactory.createColumn("two");
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values_1.intersect(values_2), expected);
    }

    @Test
    public void testIntersect_4() {
        Values values_1 = DataFactory.createColumn("one");
        Values values_2 = DataFactory.createColumn("two");
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values_2.intersect(values_1), expected);
    }


}
