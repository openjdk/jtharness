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
import com.oracle.tck.lib.autd2.unittests.ValuesComparison;
import com.sun.tck.lib.tgf.AbstractValue;
import com.sun.tck.lib.tgf.DataFactory;
import com.sun.tck.lib.tgf.Values;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TestUtilTest {

    @Test
    public void test_pos_1() {
        List<Object[]> one = new ArrayList<Object[]>();
        List<Object[]> two = new ArrayList<Object[]>();
        ValuesComparison.compare(one, two);
    }

    @Test
    public void test_pos_2() {
        List<Object[]> one = new ArrayList<Object[]>();
        List<Object[]> two = new ArrayList<Object[]>();
        one.add(new Object[]{});
        two.add(new Object[]{});
        ValuesComparison.compare(one, two);
    }

    @Test
    public void test_pos_3() {
        List<Object[]> one = new ArrayList<Object[]>();
        List<Object[]> two = new ArrayList<Object[]>();
        one.add(new Object[]{1});
        two.add(new Object[]{1});
        ValuesComparison.compare(one, two);
    }

    @Test
    public void test_pos_4() {
        List<Object[]> one = new ArrayList<Object[]>();
        List<Object[]> two = new ArrayList<Object[]>();
        one.add(new Object[]{1});
        one.add(new Object[]{2});
        two.add(new Object[]{1});
        two.add(new Object[]{2});
        ValuesComparison.compare(one, two);
    }

    @Test
    public void test_pos_5() {
        List<Object[]> one = new ArrayList<Object[]>();
        List<Object[]> two = new ArrayList<Object[]>();
        one.add(new Object[]{"1"});
        one.add(new Object[]{"2"});
        two.add(new Object[]{"1"});
        two.add(new Object[]{"2"});
        ValuesComparison.compare(one, two);
    }

    @Test(expected = AssertionError.class)
    public void test_neg_1() {
        List<Object[]> one = new ArrayList<Object[]>();
        List<Object[]> two = new ArrayList<Object[]>();
        two.add(new Object[]{});
        ValuesComparison.compare(one, two);
    }

    @Test(expected = AssertionError.class)
    public void test_neg_2() {
        List<Object[]> one = new ArrayList<Object[]>();
        List<Object[]> two = new ArrayList<Object[]>();
        one.add(new Object[]{1});
        two.add(new Object[]{2});
        ValuesComparison.compare(one, two);
    }

    @Test(expected = AssertionError.class)
    public void test_neg_3() {
        List<Object[]> one = new ArrayList<Object[]>();
        List<Object[]> two = new ArrayList<Object[]>();
        one.add(new Object[]{1});
        one.add(new Object[]{2});
        two.add(new Object[]{2});
        ValuesComparison.compare(one, two);
    }

    @Test(expected = AssertionError.class)
    public void test_neg_4() {
        List<Object[]> one = new ArrayList<Object[]>();
        List<Object[]> two = new ArrayList<Object[]>();
        one.add(new Object[]{2});
        two.add(new Object[]{1});
        two.add(new Object[]{2});
        ValuesComparison.compare(one, two);
    }

    @Test(expected = AssertionError.class)
    public void test_neg_5() {
        List<Object[]> one = new ArrayList<Object[]>();
        List<Object[]> two = new ArrayList<Object[]>();
        two.add(new Object[]{1});
        two.add(new Object[]{2});
        ValuesComparison.compare(one, two);
    }

    @Test(expected = AssertionError.class)
    public void test_neg_6() {
        List<Object[]> one = new ArrayList<Object[]>();
        List<Object[]> two = new ArrayList<Object[]>();
        two.add(new Object[]{1});
        ValuesComparison.compare(one, two);
    }

    @Test(expected = AssertionError.class)
    public void test_neg_7() {
        List<Object[]> one = new ArrayList<Object[]>();
        List<Object[]> two = new ArrayList<Object[]>();
        one.add(new Object[]{});
        two.add(new Object[]{2});
        ValuesComparison.compare(one, two);
    }

    @Test(expected = AssertionError.class)
    public void test_neg_8() {
        List<Object[]> one = new ArrayList<Object[]>();
        List<Object[]> two = new ArrayList<Object[]>();
        one.add(new Object[]{1});
        one.add(new Object[]{2});
        two.add(new Object[]{2});
        two.add(new Object[]{1});
        ValuesComparison.compare(one, two);
    }

    @Test(expected = AssertionError.class)
    public void test_neg_9() {
        List<Object[]> one = new ArrayList<Object[]>();
        List<Object[]> two = new ArrayList<Object[]>();
        one.add(new Object[]{"a"});
        one.add(new Object[]{"b"});
        one.add(new Object[]{"b"});
        one.add(new Object[]{"b"});
        one.add(new Object[]{"c"});

        two.add(new Object[]{"a"});
        two.add(new Object[]{"b"});
        two.add(new Object[]{"b"});
        two.add(new Object[]{"c"});
        two.add(new Object[]{"b"});
        ValuesComparison.compare(one, two);
    }

    @Test(expected = AssertionError.class)
    public void test_neg_10() {
        List<Object[]> one = new ArrayList<Object[]>();
        List<Object[]> two = new ArrayList<Object[]>();
        one.add(new Object[]{"a", 1});
        one.add(new Object[]{"b", 1});
        one.add(new Object[]{"b", 2});
        one.add(new Object[]{"b", 2});
        one.add(new Object[]{"c", 3});

        two.add(new Object[]{"a", 1});
        two.add(new Object[]{"b", 1});
        two.add(new Object[]{"b", 2});
        two.add(new Object[]{"c", 3});
        two.add(new Object[]{"b", 2});
        ValuesComparison.compare(one, two);
    }

    @Test
    public void test_pos_6() {
        List<Object[]> one = new ArrayList<Object[]>();
        List<Object[]> two = new ArrayList<Object[]>();
        one.add(new Object[]{"a"});
        one.add(new Object[]{"b"});
        one.add(new Object[]{"b"});
        one.add(new Object[]{"b"});
        one.add(new Object[]{"c"});

        two.add(new Object[]{"a"});
        two.add(new Object[]{"b"});
        two.add(new Object[]{"b"});
        two.add(new Object[]{"b"});
        two.add(new Object[]{"c"});
        ValuesComparison.compare(one, two);
    }

    @Test
    public void test_pos_7() {
        List<Object[]> one = new ArrayList<Object[]>();
        List<Object[]> two = new ArrayList<Object[]>();
        one.add(new Object[]{"a", 1});
        one.add(new Object[]{"b", 1});
        one.add(new Object[]{"b", 2});
        one.add(new Object[]{"b", 2});
        one.add(new Object[]{"c", 3});

        two.add(new Object[]{"a", 1});
        two.add(new Object[]{"b", 1});
        two.add(new Object[]{"b", 2});
        two.add(new Object[]{"b", 2});
        two.add(new Object[]{"c", 3});
        ValuesComparison.compare(one, two);
    }

    @Test
    public void test_pos_8() {
        List<Object[]> one = new ArrayList<Object[]>();
        one.add(new Object[]{"a", 1});
        one.add(new Object[]{"b", 1});
        one.add(new Object[]{"b", 2});
        one.add(new Object[]{"b", 2});
        one.add(new Object[]{"c", 3});
        one.add(new Object[]{"abc", 123});

        ValuesComparison.compare(one, one);
    }

    @Test
    public void test_pos_9() {

        List<Object[]> one = new ArrayList<Object[]>();
        one.add(new Object[]{"a", 1});
        one.add(new Object[]{"a", 2});
        one.add(new Object[]{"b", 1});
        one.add(new Object[]{"b", 2});

        final Values two = DataFactory.createColumn("a", "b").multiply(1, 2);

        ValuesComparison.compare(two, one);
    }

    @Test(expected = AssertionError.class)
    public void test_neg_11() {
        List<Object[]> one = new ArrayList<Object[]>();
        one.add(new Object[]{"a", 1});
        one.add(new Object[]{"a", 2});
        one.add(new Object[]{"b", 2});
        one.add(new Object[]{"b", 1});
        final Values two = DataFactory.createColumn("a", "b").multiply(1, 2);
        ValuesComparison.compare(two, one);
    }


    @Test
    public void test_pos_10() {

        List<Object[]> one = new ArrayList<Object[]>();
        one.add(new Object[]{"a", 44});
        one.add(new Object[]{"a", 44});
        one.add(new Object[]{"b", 44});
        one.add(new Object[]{"a", 44});

        final Values two = DataFactory.createValues(
                new Object[][]{{"a", 44}, {"a", 44}} ).unite(
                new Object[][]{{"b", 44}, {"a", 44}}
        );

        ValuesComparison.compare(two, one);
    }

    @Test(expected = AssertionError.class)
    public void test_neg_12() {

        List<Object[]> one = new ArrayList<Object[]>();
        one.add(new Object[]{"a", 44});
        one.add(new Object[]{"b", 44});
        one.add(new Object[]{"a", 44});
        one.add(new Object[]{"a", 44});

        final Values two = DataFactory.createValues(
                new Object[][]{{"a", 44}, {"a", 44}} ).unite(
                new Object[][]{{"b", 44}, {"a", 44}}
        );

        ValuesComparison.compare(two, one);
    }

    @Test
    public void testCreateArray() {
        ArrayList<Object[]> arrayList = new ArrayList<Object[]>();
        arrayList.add(new Object[]{"a", "b", 4});
        arrayList.add(new Object[]{43, 90});
        String s = TU.generateRandomString();
        arrayList.add(new Object[]{s, 45});
        Object[][] array = ValuesComparison.createArray(arrayList);
        Assert.assertArrayEquals(
                new Object[][]{{"a", "b", 4}, {43, 90}, {s, 45}}, array
        );
    }

    @Test
    public void testCreateCollection_1() {

        ArrayList<Object[]> arrayList = new ArrayList<Object[]>();
        arrayList.add(new Object[]{"rtyua", "b567", 764});
        arrayList.add(new Object[]{433, 970});
        String s = TU.generateRandomString();
        arrayList.add(new Object[]{s, 485});
        ArrayList<Object[]> collection = ValuesComparison.createCollection(arrayList);

        Assert.assertArrayEquals(  collection.toArray(), arrayList.toArray()  );
    }

    @Test
    public void testCreateCollection_2() {

        ArrayList<Object[]> arrayList = new ArrayList<Object[]>();
        String s1 = TU.generateRandomString();
        final String s2 = TU.generateRandomString();

        arrayList.add(new Object[]{"rtyua", new AbstractValue(){
            public Object create() {
                return s2;
            }
        }, 764});
        arrayList.add(new Object[]{s1, 485});

        ArrayList<Object[]> collection = ValuesComparison.createCollection(arrayList);

        Assert.assertArrayEquals(
                new Object[][]{{"rtyua",s2, 764},{s1, 485}}, collection.toArray()
        );
    }

}
