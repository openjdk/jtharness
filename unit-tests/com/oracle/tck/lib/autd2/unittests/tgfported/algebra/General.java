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
import com.sun.tck.lib.tgf.*;
import com.sun.tck.test.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class General {

    @Test(expected = UnsupportedOperationException.class)
    public void removingUnsupportedInValuesIterator() {
        new ValuesIterator() {

            public void shift() { }

            protected void rollback() {    }

            protected ValuesIterator createCopy() { return null;     }

            public boolean hasNext() {
                return false;
            }
            public Object[] next() {
                return new Object[0];
            }
        }.remove();
    }

    @Test
    public void test_1() {
        List<Object[]> expected = new ArrayList<Object[]>();
        final List<Object[]> actual = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", 1});
        expected.add(new Object[]{"a", 2});
        expected.add(new Object[]{"b", 1});
        expected.add(new Object[]{"b", 2});

        Object test = new BaseTestGroup() {

            Values sampleSetup() {
                return DataFactory.createColumn("a", "b").multiply(1, 2);
            }

            @TestCase
            @TestData("sampleSetup")
            public void test(String s, int i) {
                actual.add(new Object[]{s, i});
            }
        };
        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(test);
        ValuesComparison.compare(expected, actual);
        Assert.assertTrue(status.isOK());
    }

    @Test
    public void test_1_1() {
        List<Object[]> expected = new ArrayList<Object[]>();
        final List<Object[]> actual = new ArrayList<Object[]>();
        expected.add(new Object[]{13.3f, 1});
        expected.add(new Object[]{13.3f, 2});
        expected.add(new Object[]{5f, 1});
        expected.add(new Object[]{5f, 2});

        Object test = new BaseTestGroup() {
            Values sampleSetup() {
                return DataFactory.createColumn(13.3f, 5f).multiply(1, 2);
            }

            @TestCase
            @TestData("sampleSetup")
            public void test(float f, int i) {
                actual.add(new Object[]{f, i});
            }
        };
        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(test);
        ValuesComparison.compare(expected, actual);
        Assert.assertTrue(status.isOK());
    }

    @Test
    public void test_1_field() {
        List<Object[]> expected = new ArrayList<Object[]>();
        final List<Object[]> actual = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", 1});
        expected.add(new Object[]{"a", 2});
        expected.add(new Object[]{"b", 1});
        expected.add(new Object[]{"b", 2});

        Object test = new BaseTestGroup() {
            private Values sampleField = DataFactory.createColumn("a", "b").multiply(1, 2);

            @TestCase
            @TestData("sampleField")
            public void test(String s, int i) {
                actual.add(new Object[]{s, i});
            }
        };
        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(test);
        ValuesComparison.compare(expected, actual);
        Assert.assertTrue(status.isOK());
    }

    @Test
    public void test_2() {
        List<Object[]> expected = new ArrayList<Object[]>();
        final List<Object[]> actual = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", 1});
        expected.add(new Object[]{"b", 2});

        BaseTestGroup test = new BaseTestGroup() {
            Values sampleSetup() {
                return DataFactory.createColumn("a", "b").pseudoMultiply(1, 2);
            }

            @TestCase
            @TestData("sampleSetup")
            public void test(String s, int i) {
                actual.add(new Object[]{s, i});
            }
        };
        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(test);
        ValuesComparison.compare(expected, actual);
        Assert.assertTrue(status.isOK());
    }

    @Test
    public void test_3() {
        List<Object[]> expected = new ArrayList<Object[]>();
        final List<Object[]> actual = new ArrayList<Object[]>();
        expected.add(new Object[]{"a"});
        expected.add(new Object[]{"b"});
        expected.add(new Object[]{"c"});
        expected.add(new Object[]{"d"});

        Object test = new BaseTestGroup() {

            Values sampleSetup() {
                return DataFactory.createColumn("a", "b").unite(
                        DataFactory.createColumn("c", "d"));
            }

            @TestCase
            @TestData("sampleSetup")
            public void test(String s) {
                actual.add(new Object[]{s});
            }
        };
        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(test);
        ValuesComparison.compare(expected, actual);
        Assert.assertTrue(status.isOK());
    }

    @Test
    public void test_4() {
        List<Object[]> expected = new ArrayList<Object[]>();
        final List<Object[]> actual = new ArrayList<Object[]>();
        expected.add(new Object[]{"b"});
        expected.add(new Object[]{"c"});

        Object test = new BaseTestGroup() {

            Values sampleSetup() {
                return DataFactory.createColumn("a", "b", "c").intersect(
                        DataFactory.createColumn("b", "c", "d"));
            }

            @TestCase
            @TestData("sampleSetup")
            public void test(String s) {
                System.out.println("s = " + s );
                actual.add(new Object[]{s});
            }
        };
        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(test);
        ValuesComparison.compare(expected, actual);
        Assert.assertTrue(status.isOK());
    }

    @Test
    public void test_5() {
        List<Object[]> expected = new ArrayList<Object[]>();
        final List<Object[]> actual = new ArrayList<Object[]>();
        expected.add(new Object[]{"b"});
        expected.add(new Object[]{"c"});

        Object test = new BaseTestGroup() {

            Class[] createArray() {
                return new Class[] {null, Class.class, Object.class};
            }

            Values sampleSetup() {
                return DataFactory.createColumn((Object[])createArray()).multiply(
                       DataFactory.createColumn((Object[])createArray()));
            }

            @TestCase
            @TestData("sampleSetup")
            public void test(Class o1, Class o2) {
                System.out.println("o1 = " + o1);
                System.out.println("o2 = " + o2);
            }
        };
        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(test);
        Assert.assertTrue(status.isOK());
    }


}
