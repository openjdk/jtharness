/*
 * $Id$
 *
 * Copyright (c) 1996, 2022, Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.tck.lib.autd2.unittests.tgfported.ValuesImplSlow;
import com.oracle.tck.lib.autd2.unittests.TU;
import com.oracle.tck.lib.autd2.unittests.TestObject;
import com.oracle.tck.lib.autd2.unittests.ValuesComparison;
import com.sun.tck.lib.tgf.DataFactory;
import com.sun.tck.lib.tgf.TestData;
import com.sun.tck.lib.tgf.Values;
import com.sun.tck.test.TestCase;
import com.sun.tck.test.TestGroup;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import static org.mockito.Mockito.*;
import static com.sun.tck.lib.tgf.DataFactory.*;
import static org.junit.Assert.assertTrue;


public class Mixed {

    @Test
    public void test_0() {
        Values v_1 = createColumn();
        Values v_2 = createColumn("a");
        Values v_3 = createColumn("a", "b");

        Values vfinal = v_3.pseudoMultiply(v_1.unite(v_2));

        List<Object[]> expected = new ArrayList<>();
        expected.add(new Object[] {"a", "a"} );
        expected.add(new Object[] {"b", "a"} );
        ValuesComparison.compare(vfinal, expected);

    }

    @Test
    public void test_012() {
        Values v_1 = createColumn();
        Values v_2 = createColumn("a");

        Values vfinal = (v_1.unite(v_2)).pseudoMultiply(v_1.unite(v_2));

        List<Object[]> expected = new ArrayList<>();
        expected.add(new Object[] {"a", "a"} );
        ValuesComparison.compare(vfinal, expected);

    }

    @Test
    public void test_013() {
        Values v_1 = createColumn();
        Values v_2 = createColumn("a");
        Values v_3 = createColumn("a", "b");

        Values vfinal = (v_1.unite(v_3)).pseudoMultiply(v_1.unite(v_3));

        List<Object[]> expected = new ArrayList<>();
        expected.add(new Object[] {"a", "a"} );
        expected.add(new Object[] {"b", "b"} );
        ValuesComparison.compare(vfinal, expected);

    }

    @Test
    public void test_0_03() {
        Values v_1 = createColumn();
        Values v_2 = createColumn("a");
        Values v_3 = createColumn("a", "b");

        Values vfinal = v_3.multiply(v_1.unite(v_2));

        List<Object[]> expected = new ArrayList<>();
        expected.add(new Object[] {"a", "a"} );
        expected.add(new Object[] {"b", "a"} );
        ValuesComparison.compare(vfinal, expected);

    }

    @Test
    public void test_0_003() {
        Values v_1 = createColumn();
        Values v_2 = createColumn();
        Values v_3 = createColumn();

        Values vfinal = v_3.multiply(v_1.unite(v_2));

        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(vfinal, expected);

    }

    @Test
    public void test_00001() {
        Values v_1 = createColumn();
        Values v_2 = createColumn();
        Values v_3 = createColumn("a", "b");

        Values vfinal = v_3.pseudoMultiply(v_1.unite(v_2));

        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(vfinal, expected);

    }

    @Test
    public void test_00002() {
        Values v_1 = createColumn();
        Values v_2 = createColumn();
        Values v_3 = createColumn("a", "b");

        Values vfinal = v_3.multiply(v_1.unite(v_2));

        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(vfinal, expected);

    }

    @Test
    public void test_00003() {
        Values v_1 = createColumn();
        Values v_2 = createColumn();
        Values v_3 = createColumn("a", "b");

        Values vfinal = v_3.unite(v_1.unite(v_2));

        List<Object[]> expected = new ArrayList<>();
        expected.add(new Object[] {"a"} );
        expected.add(new Object[] {"b"} );
        ValuesComparison.compare(vfinal, expected);

    }

    @Test
    public void test_00004() {
        Values v_1 = createColumn("a", "b");
        Values v_2 = createColumn();
        Values v_3 = createColumn();

        Values vfinal = v_3.unite(v_1.unite(v_2));

        List<Object[]> expected = new ArrayList<>();
        expected.add(new Object[] {"a"} );
        expected.add(new Object[] {"b"} );
        ValuesComparison.compare(vfinal, expected);

    }

    @Test
    public void test_001() {
        Values v_1 = createColumn("a");
        Values v_2 = createColumn();
        Values v_3 = createColumn("a", "b");

        Values vfinal = v_3.pseudoMultiply(v_1.unite(v_2));

        List<Object[]> expected = new ArrayList<>();
        expected.add(new Object[] {"a", "a"} );
        expected.add(new Object[] {"b", "a"} );
        ValuesComparison.compare(vfinal, expected);

    }

    @Test
    public void test_00() {
        Values v_1 = createColumn();
        Values v_2 = createColumn("a");
        Values v_3 = createColumn("a", "b");

        // o1.with( o2.with(v1, v2), v3  )
        Values vfinal = v_1.unite(v_2).pseudoMultiply(v_3);

        List<Object[]> expected = new ArrayList<>();
        expected.add(new Object[] {"a", "a"} );
        expected.add(new Object[] {"a", "b"} );
        ValuesComparison.compare(vfinal, expected);

    }

    @Test
    public void test_01() {
        Values v_1 = createColumn("c");
        Values v_2 = createColumn("a");
        Values v_3 = createColumn("a", "b");

        // o1.with( o2.with(v1, v2), v3  )
        Values vfinal = v_1.unite(v_2).pseudoMultiply(v_3);

        List<Object[]> expected = new ArrayList<>();
        expected.add(new Object[] {"c", "a"} );
        expected.add(new Object[] {"a", "b"} );
        ValuesComparison.compare(vfinal, expected);

    }

    @Test
    public void test_0_04() {
        Values v_1 = createColumn();
        Values v_2 = createColumn("a");
        Values v_3 = createColumn("a", "b", "c");

        Values vfinal = v_3.pseudoMultiply(v_1.unite(v_2));

        List<Object[]> expected = new ArrayList<>();
        expected.add(new Object[] {"a", "a"} );
        expected.add(new Object[] {"b", "a"} );
        expected.add(new Object[] {"c", "a"} );
        ValuesComparison.compare(vfinal, expected);

    }

    @Test
    public void test_0_01() {
        Values v_2 = createColumn("a");
        Values v_3 = createColumn("a", "b");

        Values vfinal = v_3.pseudoMultiply(unite(v_2));

        List<Object[]> expected = new ArrayList<>();
        expected.add(new Object[] {"a", "a"} );
        expected.add(new Object[] {"b", "a"} );
        ValuesComparison.compare(vfinal, expected);

    }

    @Test
    public void test_0_02() {
        Values v_2 = createColumn("a");
        Values v_3 = createColumn("a", "b");

        Values vfinal = v_3.pseudoMultiply(v_2);

        List<Object[]> expected = new ArrayList<>();
        expected.add(new Object[] {"a", "a"} );
        expected.add(new Object[] {"b", "a"} );
        ValuesComparison.compare(vfinal, expected);

    }

    @Test
    public void test_0_0() {
        Values v_1 = createColumn();
        Values v_2 = createColumn("a", "v");
        Values v_3 = createColumn("a", "b");

        Values vfinal = v_3.pseudoMultiply(v_1.unite(v_2));

        List<Object[]> expected = new ArrayList<>();
        expected.add(new Object[] {"a", "a"} );
        expected.add(new Object[] {"b", "v"} );
        ValuesComparison.compare(vfinal, expected);

    }

    @Test
    public void test_0_1() {
        Values v_1 = createColumn();
        Values v_2 = createColumn("a");
        Values v_3 = createColumn();

        Values vfinal = v_3.pseudoMultiply(v_1.unite(v_2));

        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(vfinal, expected);

    }

    @Test
    public void test_0_2() {
        Values v_1 = createColumn();
        Values v_2 = createColumn();
        Values v_3 = createColumn();

        Values vfinal = v_3.pseudoMultiply(v_1.unite(v_2));

        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(vfinal, expected);

    }

    @Test
    public void test_0_3() {
        Values v_1 = createColumn();
        Values v_2 = createColumn();
        Values v_3 = createColumn("x");

        Values vfinal = v_3.pseudoMultiply(v_1.unite(v_2));

        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(vfinal, expected);

    }

    @Test
    public void test_0_4() {
        Values v_1 = createColumn("a");
        Values v_2 = createColumn();
        Values v_3 = createColumn("x");

        Values vfinal = v_3.pseudoMultiply(v_1.unite(v_2));
        List<Object[]> expected = new ArrayList<>();
        expected.add(new Object[] {"x", "a"} );
        ValuesComparison.compare(vfinal, expected);

    }

    @Test
    public void test_1_1() {
        Values values = createColumn(1, 2);
        values = values.multiply(createColumn(3, 4));
        values = values.pseudoMultiply("a", "b", "c", "d");
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {1, 3, "a"} );
        expected.add(new Object[] {1, 4, "b"} );
        expected.add(new Object[] {2, 3, "c"} );
        expected.add(new Object[] {2, 4, "d"} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_1_2() {
        Values values_1 = createColumn("a", "b", "c", "d");
        Values values_2 = createColumn(1, 2).multiply(
                createColumn(3, 4)
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {"a", 1, 3} );
        expected.add( new Object[] {"b", 1, 4} );
        expected.add( new Object[] {"c", 2, 3} );
        expected.add( new Object[] {"d", 2, 4} );
        ValuesComparison.compare(values_1.pseudoMultiply(values_2), expected);
    }

    @Test
    public void test_1_2_withEmpty_1() {
        Values values_1 = createColumn("a", "b", "c", "d");
        Values values_2 = createColumn(1, 2).multiply(
                createColumn(3, 4)
        ).multiply( createColumn() );
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values_1.pseudoMultiply(values_2), expected);
    }

    @Test
    public void test_withEmpty_1() {
        Values values = createColumn("a").intersect().multiply("a");
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_withEmpty_5() {
        Values values = createColumn().multiply().intersect("a");
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_withEmpty_5_implSlow() {
        Values values = new ValuesImplSlow().multiply().intersect(new ValuesImplSlow("a"));
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_withEmpty_1_implSlow() {
        Values values = new ValuesImplSlow("a").intersect().multiply(new ValuesImplSlow("a"));
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_withEmpty_2_implSlow() {
        Values values = new ValuesImplSlow("a").intersect().pseudoMultiply(new ValuesImplSlow("a"));
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_withEmpty_2() {
        Values values = createColumn("a", "b").intersect().multiply("a");
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_withEmpty_3() {
        Values values = createColumn("a", "b", "c").intersect().multiply("a");
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_withEmpty_4() {
        Values values = createColumn("a", "b", "c").intersect().multiply("a", "b", "c");
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_withEmpty_40() {
        Values values = createColumn("a", "b").multiply( createColumn().intersect("a") );
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_withEmpty_41() {
        Values values = createColumn("b").multiply( createColumn().intersect("a") );
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_withEmpty_40_dumbImpl() {
        Values values = new ValuesImplSlow("w").multiply( new ValuesImplSlow().intersect("a") );
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_withEmpty_41_dumbImpl() {
        Values values = new ValuesImplSlow("w").pseudoMultiply( new ValuesImplSlow().intersect("a") );
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_50() {
        Values values = new ValuesImplSlow("a").multiply("a").intersect(new ValuesImplSlow("a"));
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_50_1() {
        Values values = new ValuesImplSlow("a").multiply();
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_50_2() {
        Values values = new ValuesImplSlow().multiply("a");
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_50_3() {
        Values values = new ValuesImplSlow().pseudoMultiply("a");
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_50_4() {
        Values values = new ValuesImplSlow("a").pseudoMultiply();
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_51() {
        Values values = createColumn("a").multiply("a").intersect("a");
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }



    @Test
    public void test_1_2_withEmpty_2() {
        Values values_1 = createColumn("a", "b", "c", "d").pseudoMultiply(createRow());
        Values values_2 = createColumn(1, 2).multiply(
                createColumn(3, 4) ).multiply( createColumn() );
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values_1.pseudoMultiply(values_2), expected);
    }

    @Test
    public void test_1_2_cached() {
        Values values_1 = createColumn("a", "b", "c", "d");
        Values values_2 = createColumn(1, 2).multiply(
                createColumn(3, 4)
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] {"a", 1, 3} );
        expected.add( new Object[] {"b", 1, 4} );
        expected.add( new Object[] {"c", 2, 3} );
        expected.add( new Object[] {"d", 2, 4} );
        ValuesComparison.compare(values_1.pseudoMultiply(values_2).createCache(), expected);
    }

    @Test
    public void testCombine_Diag_1() {
        Values values = createColumn(1, 2);
        values = values.multiply(createColumn(3, 4));
        values = values.pseudoMultiply(createColumn(3));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {1, 3, 3});
        expected.add(new Object[] {1, 4, 3});
        expected.add(new Object[] {2, 3, 3});
        expected.add(new Object[] {2, 4, 3});
        ValuesComparison.compare (values, expected);
    }

    @Test
    public void testCombine_Diag_1_cached() {
        Values values = createColumn(1, 2);
        values = values.multiply(createColumn(3, 4));
        values = values.pseudoMultiply(createColumn(3));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3, 3});
        expected.add(new Object[]{1, 4, 3});
        expected.add(new Object[]{2, 3, 3});
        expected.add(new Object[]{2, 4, 3});
        ValuesComparison.compare(values.createCache(), expected);
    }

    @Test
    public void testCombine_Diag_2() {
        Values values = createColumn(1, 2);
        values = values.multiply(createColumn(3, 4));
        values = values.pseudoMultiply(createColumn(67, 45, 79, 13));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3, 67});
        expected.add(new Object[]{1, 4, 45});
        expected.add(new Object[]{2, 3, 79});
        expected.add(new Object[]{2, 4, 13});
        ValuesComparison.compare(values, expected);
    }

    @Test

    public void test_Combine_Diag_Combine() {
        Values values_1 = createColumn(1, 2);
        values_1 = values_1.multiply(createColumn(3, 4));

        Values values_2 = createColumn(6, 7);
        values_2 = values_2.multiply(createColumn(8, 9));

        Values result = values_1.pseudoMultiply(values_2);

        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3, 6, 8});
        expected.add(new Object[]{1, 4, 6, 9});
        expected.add(new Object[]{2, 3, 7, 8});
        expected.add(new Object[]{2, 4, 7, 9});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void testDiag_Combine_1() {
        Values values = createColumn(1, 2);
        values = values.pseudoMultiply(createColumn(3, 4));
        values = values.multiply(createColumn(7, 8));
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 3, 7});
        expected.add(new Object[]{1, 3, 8});
        expected.add(new Object[]{2, 4, 7});
        expected.add(new Object[]{2, 4, 8});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_Combine_2_1() {
        Values vs1 = createColumn("a", "b", "c", "d", "e");
        Values vs2 = createColumn("x", "y", "z");
        Values vs3 = createColumn(1);
        Values vs4 = createColumn(new TestObject("2"));

        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", "x", 1, new TestObject("2")});
        expected.add(new Object[]{"b", "y", 1, new TestObject("2")});
        expected.add(new Object[]{"c", "z", 1, new TestObject("2")});
        expected.add(new Object[]{"d", "x", 1, new TestObject("2")});
        expected.add(new Object[]{"e", "y", 1, new TestObject("2")});
        ValuesComparison.compare(vs1.pseudoMultiply(vs2).multiply(vs3).pseudoMultiply(vs4), expected);
    }

    @Test
    public void testDiag_Combine_2_2() {
        Values vs1 = createColumn("a", "b", "c", "d", "e");
        Values vs2 = createColumn("x", "y", "z");
        Values vs3 = createColumn(1).pseudoMultiply(new TestObject("2"));

        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", "x", 1, new TestObject("2")});
        expected.add(new Object[]{"b", "y", 1, new TestObject("2")});
        expected.add(new Object[]{"c", "z", 1, new TestObject("2")});
        expected.add(new Object[]{"d", "x", 1, new TestObject("2")});
        expected.add(new Object[]{"e", "y", 1, new TestObject("2")});
        ValuesComparison.compare(vs1.pseudoMultiply(vs2).multiply(vs3), expected);
    }

    @Test
    public void testDiag_Combine_2_3() {
        Values vs1 = createColumn("a", "b", "c", "d", "e");
        Values vs2 = createColumn("x", "y", "z");
        Values vs3 = createColumn(1).pseudoMultiply(new TestObject("2"));

        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", "x", 1, new TestObject("2")});
        expected.add(new Object[]{"b", "y", 1, new TestObject("2")});
        expected.add(new Object[]{"c", "z", 1, new TestObject("2")});
        expected.add(new Object[]{"d", "x", 1, new TestObject("2")});
        expected.add(new Object[]{"e", "y", 1, new TestObject("2")});
        ValuesComparison.compare(vs1.pseudoMultiply(vs2).pseudoMultiply(vs3), expected);
    }

    @Test
    public void testDiag_Combine_2_3_cached() {
        Values vs1 = createColumn("a", "b", "c", "d", "e");
        Values vs2 = createColumn("x", "y", "z");
        Values vs3 = createColumn(1).pseudoMultiply(new TestObject("2"));

        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", "x", 1, new TestObject("2")});
        expected.add(new Object[]{"b", "y", 1, new TestObject("2")});
        expected.add(new Object[]{"c", "z", 1, new TestObject("2")});
        expected.add(new Object[]{"d", "x", 1, new TestObject("2")});
        expected.add(new Object[]{"e", "y", 1, new TestObject("2")});
        ValuesComparison.compare(vs1.pseudoMultiply(vs2).pseudoMultiply(vs3).createCache(), expected);
    }




    @org.junit.Test
    public void test_1() {

        final MyClass instance = mock(MyClass.class);

        @TestGroup
        class MyTest {

            private Values setup() {
                return createColumn("i_1", "i_2", "i_3").pseudoMultiply("j_1", "j_2", "j_3");
            }

            @TestCase
            @TestData("setup")
            public void test(String s1, String s2) {
                instance.method_1(s1, s2);
            }
        }

        final com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new MyTest());

        assertTrue(status.isOK());

        verify(instance).method_1("i_1", "j_1");
        verify(instance).method_1("i_2", "j_2");
        verify(instance).method_1("i_3", "j_3");
        verifyNoMoreInteractions(instance);
    }

    @org.junit.Test
    public void test_1_Multiplication() {

        final MyClass instance = mock(MyClass.class);

        @TestGroup
        class MyTest {

            private Values setup() {
                return createColumn("i_1", "i_2", "i_3").multiply("j_1", "j_2", "j_3");
            }

            @TestData("setup")
            @TestCase
            public void test(String s1, String s2) {
                instance.method_1(s1, s2);
            }
        }


        final com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new MyTest());
        System.out.println("status = " + status);
        assertTrue(status.isOK());

        verify(instance).method_1("i_1", "j_1");
        verify(instance).method_1("i_1", "j_2");
        verify(instance).method_1("i_1", "j_3");
        verify(instance).method_1("i_2", "j_1");
        verify(instance).method_1("i_2", "j_2");
        verify(instance).method_1("i_2", "j_3");
        verify(instance).method_1("i_3", "j_1");
        verify(instance).method_1("i_3", "j_2");
        verify(instance).method_1("i_3", "j_3");
        verifyNoMoreInteractions(instance);

    }

    @org.junit.Test
    public void test_2() {

        final MyClass mock = mock(MyClass.class);

        @TestGroup
        class MyTest {

            private Values setup() {
                return createColumn("i1", "i2", "i3", "i4").pseudoMultiply("j1", "j2", "j3", "j4").pseudoMultiply("k1", "k2", "k3", "k4");
            }

            @TestCase
            @TestData("setup")
            public void test(String s1, String s2, String s3) {
                mock.method_2(s1, s2, s3);
            }
        }

        final com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new MyTest());
        System.out.println("status = " + status);
        assertTrue(status.isOK());

        verify(mock).method_2("i1", "j1", "k1");
        verify(mock).method_2("i2", "j2", "k2");
        verify(mock).method_2("i3", "j3", "k3");
        verify(mock).method_2("i4", "j4", "k4");
        verifyNoMoreInteractions(mock);

    }

    @org.junit.Test
    public void test_binded_together() {

        final MyClass mock = mock(MyClass.class);

        @TestGroup
        class MyTest {

            private Values setup() {
                return createColumn("i1", "i2", "i3").multiply("j1", "j2", "j3").pseudoMultiply("k1", "k2", "k3");
            }

            @TestCase
            @TestData("setup")
            public void test(String s1, String s2, String s3) {
                mock.method_2(s1, s2, s3);
            }
        }


        final com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new MyTest());
        System.out.println("status = " + status);
        assertTrue(status.isOK());
        verify(mock).method_2("i1", "j1", "k1");
        verify(mock).method_2("i1", "j2", "k2");
        verify(mock).method_2("i1", "j3", "k3");
        verify(mock).method_2("i2", "j1", "k1");
        verify(mock).method_2("i2", "j2", "k2");
        verify(mock).method_2("i2", "j3", "k3");
        verify(mock).method_2("i3", "j1", "k1");
        verify(mock).method_2("i3", "j2", "k2");
        verify(mock).method_2("i3", "j3", "k3");
        verifyNoMoreInteractions(mock);

    }


    @org.junit.Test
    public void test_binded_together_2() {

        final MyClass mock = mock(MyClass.class);

        @TestGroup
        class MyTest {

            private Values setup() {
                return createColumn("i1", "i2", "i3").pseudoMultiply("j1", "j2", "j3").pseudoMultiply("k1", "k2", "k3").multiply("n1", "n2", "n3");
            }

            @TestCase
            @TestData("setup")
            public void test(String s1, String s2, String s3, String s4) {
                mock.method_3(s1, s2, s3, s4);
            }
        }

        final com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new MyTest());
        System.out.println("status = " + status);
        assertTrue(status.isOK());

        verify(mock).method_3("i1", "j1", "k1", "n1");
        verify(mock).method_3("i1", "j1", "k1", "n2");
        verify(mock).method_3("i1", "j1", "k1", "n3");
        verify(mock).method_3("i2", "j2", "k2", "n1");
        verify(mock).method_3("i2", "j2", "k2", "n2");
        verify(mock).method_3("i2", "j2", "k2", "n3");
        verify(mock).method_3("i3", "j3", "k3", "n1");
        verify(mock).method_3("i3", "j3", "k3", "n2");
        verify(mock).method_3("i3", "j3", "k3", "n3");
        verifyNoMoreInteractions(mock);

    }


    @org.junit.Test
    public void test_binded_with_merge_ann_use() {

        final MyClass mock = mock(MyClass.class);

        @TestGroup
        class MyTest {

            private Values setup_1() {
                return createColumn("i1", "i2", "i3").pseudoMultiply("j1", "j2", "j3").unite(
                        createColumn("k1", "k2", "k3").pseudoMultiply("n1", "n2", "n3")
                );
            }

            @TestCase
            @TestData("setup_1")
            public void test(String s1, String s2) {
                mock.method_1(s1, s2);
            }
        }

        final com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new MyTest());
        System.out.println("status = " + status);
        assertTrue(status.isOK());

        verify(mock).method_1("i1", "j1");
        verify(mock).method_1("i2", "j2");
        verify(mock).method_1("i3", "j3");
        verify(mock).method_1("k1", "n1");
        verify(mock).method_1("k2", "n2");
        verify(mock).method_1("k3", "n3");
        verifyNoMoreInteractions(mock);
    }

    @org.junit.Test
    public void test_binded_with_merge_ann_use_noMult() {

        final MyClass mock = mock(MyClass.class);

        @TestGroup
        class MyTest {

            private Values setup_1() {
                return createColumn("i1", "i2", "i3").pseudoMultiply("j1", "j2", "j3").unite(
                       createColumn("k1", "k2", "k3").pseudoMultiply("n1", "n2", "n3")
                );
            }

            @TestCase
            @TestData("setup_1")
            public void test(String s1, String s2) {
                mock.method_1(s1, s2);
            }
        }

        final com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new MyTest());

        assertTrue(status.isOK());

        verify(mock).method_1("i1", "j1");
        verify(mock).method_1("i2", "j2");
        verify(mock).method_1("i3", "j3");
        verify(mock).method_1("k1", "n1");
        verify(mock).method_1("k2", "n2");
        verify(mock).method_1("k3", "n3");
        verifyNoMoreInteractions(mock);
    }

    @org.junit.Test
    public void test_binded_with_merge_ann_use_ArrayOfObjects() {

        final MyClass mock = mock(MyClass.class);

        @TestGroup
        class MyTest {

            private Values setup_1() {
                return createValues(new Object[][] {
                        {"i1", "j1"},
                        {"i2", "j2"},
                        {"i3", "j3"}
                }).unite(createValues(new Object[][] {
                        {"k1", "n1"},
                        {"k2", "n2"},
                        {"k3", "n3"}
                }));
            }

            @TestCase
            @TestData("setup_1")
            public void test(String s1, String s2) {
                mock.method_1(s1, s2);
            }
        }



        final com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new MyTest());
        assertTrue(status.isOK());

        verify(mock).method_1("i1", "j1");
        verify(mock).method_1("i2", "j2");
        verify(mock).method_1("i3", "j3");
        verify(mock).method_1("k1", "n1");
        verify(mock).method_1("k2", "n2");
        verify(mock).method_1("k3", "n3");


    }

    @org.junit.Test
    public void test_InvalidArgSetsLength() {

        final MyClass mock = mock(MyClass.class);

        @TestGroup
        class MyTest {

            private Values setup() {
                return createColumn("i1", "i2", "i3", "i4").pseudoMultiply("j1", "j2").pseudoMultiply("k1", "k2", "k4");
            }

            @TestCase
            @TestData("setup")
            public void test(String s1, String s2, String s3) {
                mock.method_2(s1, s2, s3);
            }
        }


        TU.runTestGroup(new MyTest());

        // this is enough
        verify(mock).method_2("i1", "j1", "k1");
        verify(mock).method_2("i2", "j2", "k2");

    }

    private interface MyClass {
        void method_1(String s1, String s2);
        void method_2(String s1, String s2, String s3);
        void method_3(String s1, String s2, String s3, String s4);
    }

    @Test
    public void testDiag_pseudoMult_intersect_1() {
        Values values = pseudoMultiply(
                createColumn(1),
                createColumn(1,2,3,4,5,6).intersect(2,3),
                createColumn(3,4,5).pseudoMultiply(6,7,8,9),
                createColumn(1,2,3,4,5).pseudoMultiply("a","b","c","d").pseudoMultiply("x","y","z"),
                createColumn(0,1).pseudoMultiply(115)
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
    public void testDiag_pseudoMult_intersect_2() {
        Values values = pseudoMultiply(
                createColumn(1,2,3,4,5,6).intersect(2,3),
                createColumn(4,5,6,7,8,9,10)
                );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {2, 4});
        expected.add(new Object[] {3, 5});
        expected.add(new Object[] {2, 6});
        expected.add(new Object[] {3, 7});
        expected.add(new Object[] {2, 8});
        expected.add(new Object[] {3, 9});
        expected.add(new Object[] {2, 10});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_pseudoMult_intersect_3() {
        Values values = pseudoMultiply(
                createColumn(1),
                createColumn(1,2,3,4,5,6).intersect(2,3),
                createColumn(4,5,6,7)
                );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {1, 2, 4});
        expected.add(new Object[] {1, 3, 5});
        expected.add(new Object[] {1, 2, 6});
        expected.add(new Object[] {1, 3, 7});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_pseudoMult_intersect_4() {
        Values values = pseudoMultiply(
                createColumn(1),
                createColumn(1,2,3,4,5,6).intersect(2,3).intersect(2),
                createColumn(4,5,6,7)
                );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {1, 2, 4});
        expected.add(new Object[] {1, 2, 5});
        expected.add(new Object[] {1, 2, 6});
        expected.add(new Object[] {1, 2, 7});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_pseudoMult_intersect_5() {
        Values values = pseudoMultiply(
                createColumn(1),
                createColumn(1,2,3,4,5,6).intersect(2,3).intersect(2).intersect(999),
                createColumn(4,5,6,7)
                );
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_pseudoMult_intersect_6() {
        Values values = pseudoMultiply(
                createColumn(1),
                createColumn(1,2,3,4,5,6).intersect(2,3).intersect(2).intersect(999),
                createColumn(4,5,6,7)
                ).multiply( createColumn(56, 67, 89).intersect(67) );
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_pseudoMult_intersect_7() {
        Values values = pseudoMultiply(
                createColumn(1),
                createColumn(1,2,3,4,5,6).intersect(2,3).intersect(2).intersect(999),
                createColumn(4,5,6,7)
                ).multiply( createColumn(56, 67, 89).intersect(67) ).pseudoMultiply(5, 8);
        values = createColumn(6).pseudoMultiply(values);
        values = values.pseudoMultiply(
                createColumn(7).intersect(7).intersect(7)
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_pseudoMult_intersect_8() {
        Values values = pseudoMultiply(
                createColumn(1).intersect(1).pseudoMultiply(8, 9),
                createColumn(4,5,6).multiply(3).pseudoMultiply(6, 7),
                createColumn("a", "b", "c", "d")
                );

        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { 1, 8, 4, 3, 6, "a" });
        expected.add(new Object[] { 1, 9, 5, 3, 7, "b" });
        expected.add(new Object[] { 1, 8, 6, 3, 6, "c" });
        expected.add(new Object[] { 1, 8, 4, 3, 6, "d" });
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_pseudoMult_intersect_9() {
        Values values = pseudoMultiply(
                createColumn(6, 1, 9).intersect(1).pseudoMultiply(8, 9).unite(createRow(5, 6)),
                createColumn(8, 6, 7, 9, 10, 11, 12)
                );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { 1, 8, 8  });
        expected.add(new Object[] { 1, 9, 6  });
        expected.add(new Object[] { 5, 6, 7  });
        expected.add(new Object[] { 1, 8, 9  });
        expected.add(new Object[] { 1, 9, 10 });
        expected.add(new Object[] { 5, 6, 11 });
        expected.add(new Object[] { 1, 8, 12 });
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_pseudoMult_intersect_10() {
        Values values =
                createRow(1, 9, 6).unite(createRow(5, 6, 11)).intersect(
                        pseudoMultiply(
                        createColumn(6, 1, 9).intersect(1).pseudoMultiply(8, 9).unite(createRow(5, 6)),
                        createColumn(8, 6, 7, 9, 10, 11, 12)
                        )
                );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { 1, 9, 6 });
        expected.add(new Object[] { 5, 6, 11 });
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_pseudoMult_intersect_11() {
        Values values =
                createRow(1, 9, 6).unite(createRow(5, 6, 11)).intersect(
                        pseudoMultiply( createColumn(6, 1, 9).intersect(1).pseudoMultiply(8, 9).unite(createRow(5, 6)),
                        createColumn(8, 6, 7, 9, 10, 11, 12)
                        )
                );
        values = createColumn(5,6,7).pseudoMultiply(values);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { 5, 1, 9, 6 });
        expected.add(new Object[] { 6, 5, 6, 11 });
        expected.add(new Object[] { 7, 1, 9, 6 });
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_pseudoMult_intersect_12() {
        Values values =
                createRow(1, 9, 6).unite(createRow(5, 6, 11)).intersect(
                        createRow(1,9,6).unite(createRow(4,5,7)).unite(createRow(5, 6, 11))
                );
        values = createColumn(5,6,7).pseudoMultiply(values);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { 5, 1, 9, 6 });
        expected.add(new Object[] { 6, 5, 6, 11 });
        expected.add(new Object[] { 7, 1, 9, 6 });
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_pseudoMult_intersect_13() {
        Values values =
                createRow(1, 9, 6).unite(createRow(5, 6, 11)).intersect(
                        pseudoMultiply( createColumn(1, 5).pseudoMultiply(9, 6).pseudoMultiply(6, 11))
                );
        values = createColumn(5,6,7).pseudoMultiply(values);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { 5, 1, 9, 6 });
        expected.add(new Object[] { 6, 5, 6, 11 });
        expected.add(new Object[] { 7, 1, 9, 6 });
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_pseudoMult_intersect_14() {
        Values values =
                createRow(1, 9, 6).unite(createRow(5, 6, 11)).intersect(
                        pseudoMultiply( createColumn(6, 1, 9).intersect(1).pseudoMultiply(8, 9).unite(createRow(5, 6)),
                        createColumn(8, 6, 7, 9, 10, 11, 12)
                        )
                );
        values = createColumn(5,6,7).pseudoMultiply(values);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { 5, 1, 9, 6 });
        expected.add(new Object[] { 6, 5, 6, 11 });
        expected.add(new Object[] { 7, 1, 9, 6 });
        ValuesComparison.compare(values, expected);
    }


    @Test
    public void testDiag_pseudoMult_intersect_15() {
        Values values =
                createRow(1, 9).unite(createRow(5, 6)).intersect(
                        pseudoMultiply( createColumn(6, 1, 9).intersect(1).pseudoMultiply(8, 9).unite(createRow(5, 6))
                        )
                );
        values = createColumn(5,6,7,8).pseudoMultiply(values);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { 5, 1, 9 });
        expected.add(new Object[] { 6, 5, 6 });
        expected.add(new Object[] { 7, 1, 9 });
        expected.add(new Object[] { 8, 5, 6 });
        ValuesComparison.compare(values, expected);
    }


    @Test
    public void testDiag_pseudoMult_intersect_16() {
        Values values =
                createRow(1, 9).unite(createRow(5, 6)).intersect(
                        pseudoMultiply(
                                createColumn(1).unite(9).unite(5),
                                createColumn(9, 6, 6, 9, 10, 11, 12)
                        )
                );
        values = createColumn(5,6,7,8).pseudoMultiply(values);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { 5, 1, 9});
        expected.add(new Object[] { 6, 5, 6});
        expected.add(new Object[] { 7, 1, 9});
        expected.add(new Object[] { 8, 5, 6});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_pseudoMult_intersect_17() {
        Values values = createRow(1, 9).unite(createRow(5, 6))
                .intersect(
                        pseudoMultiply(
                                createColumn(1,9,5),
                                createColumn(99, 6, 6, 9, 10, 11, 12)
                        )
                );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { 1, 9});
        expected.add(new Object[] { 5, 6});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testDiag_pseudoMult_intersect_18() {
        Values values = createColumn("a", "b").pseudoMultiply(1,2,3).pseudoMultiply(4,5,6,7,8,9)
                .intersect(createRow("a", 3, 6).unite(createRow("b", 2, 5)).unite(createRow("b", 89, 56)) );
        values = pseudoMultiply(values);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { "b", 2, 5});
        expected.add(new Object[] { "a", 3, 6});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_mult_impl_implSlow() {
        Values values = DataFactory.createColumn(1).multiply(new ValuesImplSlow("a"));
        List<Object[]> expected = new ArrayList<>();
        expected.add(new Object[]{ 1, "a" });
        ValuesComparison.compare(values, expected);
    }
}
