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

import com.oracle.tck.lib.autd2.unittests.TU;
import com.oracle.tck.lib.autd2.unittests.TestObject;
import com.oracle.tck.lib.autd2.unittests.ValuesComparison;
import com.sun.tck.lib.tgf.AbstractValue;
import com.sun.tck.lib.tgf.DataFactory;
import com.sun.tck.lib.tgf.Values;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class Constructors {

    @Test
    public void testConstructor_Column_1() {
        Values values = DataFactory.createColumn("1", "2", "3", "4");
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"1"});
        expected.add(new Object[]{"2"});
        expected.add(new Object[]{"3"});
        expected.add(new Object[]{"4"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testConstructor_Column_1_cache() {
        Values values = DataFactory.createColumn("1", "2", "3", "4").createCache();
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"1"});
        expected.add(new Object[]{"2"});
        expected.add(new Object[]{"3"});
        expected.add(new Object[]{"4"});
        ValuesComparison.compare(values, expected);
    }


    @Test
    public void testConstructor_Column_2() {
        Values values = DataFactory.createColumn(1, 2, 3, 3);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1});
        expected.add(new Object[]{2});
        expected.add(new Object[]{3});
        expected.add(new Object[]{3});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testConstructor_Column_3() {
        final String s = TU.generateRandomString();
        Values values = DataFactory.createColumn(new TestObject(s + "suff"));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{new TestObject(s + "suff")});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testConstructor_Column_4() {
        final String s1 = TU.generateRandomString();
        final String s2 = TU.generateRandomString();
        final String s3 = TU.generateRandomString();
        Values values = DataFactory.createColumn(
                new TestObject(s1),
                new TestObject(s2),
                new TestObject(s3)

        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{new TestObject(s1)});
        expected.add(new Object[]{new TestObject(s2)});
        expected.add(new Object[]{new TestObject(s3)});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testConstructor_TwoDim_1() {
        Values values = DataFactory.createValues(
                new Object[][] {
                        {"a", "b"},
                        {"c", "d"},
                        {"e", "f"}
                });
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", "b"});
        expected.add(new Object[]{"c", "d"});
        expected.add(new Object[]{"e", "f"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testConstructor_TwoDim_2() {
        Values values = DataFactory.createValues(
                new TestObject[][] {
                        {new TestObject("a"), new TestObject("b")},
                        {new TestObject("b"), new TestObject("c")},
                        {new TestObject("e"), new TestObject("d")},
                        {new TestObject("x"), new TestObject("y")},
                });
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new TestObject[]{new TestObject("a"), new TestObject("b")});
        expected.add(new TestObject[]{new TestObject("b"), new TestObject("c")});
        expected.add(new TestObject[]{new TestObject("e"), new TestObject("d")});
        expected.add(new TestObject[]{new TestObject("x"), new TestObject("y")});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testConstructor_Row_1() {
        final String s1 = TU.generateRandomString();
        final String s2 = TU.generateRandomString();
        final String s3 = TU.generateRandomString();
        Values values = DataFactory.createRow(
                new TestObject(s1),
                new TestObject(s2),
                new TestObject(s3)
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { new TestObject(s1), new TestObject(s2), new TestObject(s3) });
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testConstructor_Row_2() {
        final String s1 = TU.generateRandomString();
        Values values = DataFactory.createRow(
                new TestObject(s1)
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { new TestObject(s1) });
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testConstructor_Row_3() {
        Values values = DataFactory.createRow(
                "1", "2", "a", "c", "b", "c"
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new String[] { "1", "2", "a", "c", "b", "c" });
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testConstructor_Row_3_cache() {
        Values values = DataFactory.createRow (
                "1", "2", "a", "c", "b", "c"
        ).createCache();
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new String[] { "1", "2", "a", "c", "b", "c" });
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testConstructor_Row_4() {
        Values values = DataFactory.createRow(
                567, "a", "c", "b", "c_", new TestObject("x")
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { 567, "a", "c", "b", "c_", new TestObject("x") });
        ValuesComparison.compare(values, expected);
    }


    @Test
    public void testConstructor_Row_5() {
        Values values = DataFactory.createRow( "a", "b", 1 );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { "a", "b", 1 });
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testConstructor_Row_5_cache() {
        Values values = DataFactory.createRow( "a", "b", 1 ).createCache();
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { "a", "b", 1 });
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_LazyInitialization() {
        Values values = DataFactory.createColumn(
        new AbstractValue<Object>() {
            public Object create() {
                return new TestObject("a");
            }
        },
        new AbstractValue<Object>() {
            public Object create() {
                return new TestObject("b");
            }
        },
        new AbstractValue<Object>() {
            public Object create() {
                return new TestObject("c");
            }
        });

        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { new TestObject("a") });
        expected.add(new Object[] { new TestObject("b") });
        expected.add(new Object[] { new TestObject("c") });

        ValuesComparison.compare(values, expected);

    }

}
