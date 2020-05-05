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
package com.oracle.tck.lib.autd2.unittests.tgfported;

import com.oracle.tck.lib.autd2.unittests.IntPair;
import com.sun.tck.lib.tgf.*;
import com.sun.tck.test.TestCase;
import com.sun.tck.test.TestGroup;
import org.junit.Assert;
import org.junit.Test;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class ComplexExclusions {




    @Test
    public void test_pseudo_4() {
        check(
                DataFactory.createColumn(1, 2, 3, 4).pseudoMultiply(5, 6, 7, 8),
                new HashSet<IntPair>(),
                new HashSet<IntPair>(),
                new HashSet<IntPair>(),
                TGFUtils.EXCLUDE, "myTest," +
                "blabla,foo"
        );
    }






    @Test
    public void test_multi_3() {
        check(
                DataFactory.createColumn(1, 2, 3, 7).multiply(4, 5, 6, 8),
                new HashSet<IntPair>(),
                new HashSet<IntPair>(),
                new HashSet<IntPair>(),
                TGFUtils.EXCLUDE, "myTest," +
                "blabla," +
                "foo"
        );
    }

    @Test
    public void test_multi_4() {
        Values values = DataFactory.createColumn(1, 2, 3, 7);
        check(
                values.multiply(values).multiply(values).multiply(values),
                new HashSet<IntPair>(),
                new HashSet<IntPair>(),
                new HashSet<IntPair>(),
                TGFUtils.EXCLUDE, "myTest,blabla,foo"
        );
    }

    @Test
    public void test_multi_5() {
        Values values = DataFactory.createColumn(1, 2, 3, 7);
        check(
                values.multiply(values).multiply(values).multiply(values),
                new HashSet<IntPair>(),
                new HashSet<IntPair>(),
                new HashSet<IntPair>(),
                TGFUtils.EXCLUDE, "myTest,blabla,foo"
        );
    }

    @Test
    public void test_multi_6() {
        Values values = DataFactory.createColumn(1, 2, 3, 7);
        check(
                values.multiply(values).multiply(values).multiply(values),
                new HashSet<IntPair>(),
                new HashSet<IntPair>(),
                new HashSet<IntPair>(),
                TGFUtils.EXCLUDE, "myTest,blabla,foo"
        );
    }

    @Test
    public void test_multi_7() {
        Values values = DataFactory.createColumn(1, 2, 3, 7, 456, 45);
        check(
                values.multiply(values).multiply(values).multiply(values),
                new HashSet<IntPair>(),
                new HashSet<IntPair>(),
                new HashSet<IntPair>(),
                TGFUtils.EXCLUDE, "myTest,blabla,foo"
        );
    }

    @Test
    public void test_multi_8() {
        Values values = DataFactory.createColumn(1, 2, 3, 7, 456, 45);
        check(
                values.multiply(values).multiply(values).multiply(values),
                new HashSet<IntPair>(),
                new HashSet<IntPair>(),
                new HashSet<IntPair>(),
                TGFUtils.EXCLUDE, "myTest,blabla,foo"
        );
    }

    @Test
    public void test_mix_9() {
        Values values = DataFactory.createColumn(1, 2, 3, 7, 456, 45);
        check(
                values.multiply(values).multiply(values).multiply(values).pseudoMultiply(values),
                new HashSet<IntPair>(),
                new HashSet<IntPair>(),
                new HashSet<IntPair>(),
                TGFUtils.EXCLUDE, "myTest,blabla,foo"
        );
    }

    @Test
    public void test_mix_10() {
        Values values = DataFactory.createColumn(1, 2, 3, 7, 456, 45);
        check(
                values.multiply(values).intersect(values).multiply(values).pseudoMultiply(values),
                new HashSet<IntPair>(),
                new HashSet<IntPair>(),
                new HashSet<IntPair>(),
                TGFUtils.EXCLUDE, "myTest,blabla,foo"
        );
    }

    private void check(final Values values,
                       final Set<IntPair> expectedMyTestPasedInPairs,
                       final Set<IntPair> expectedBlablaPasedInPairs,
                       final Set<IntPair> expectedFooPasedInPairs,
                       String... args) {
        checkNormal(values, expectedMyTestPasedInPairs, expectedBlablaPasedInPairs, expectedFooPasedInPairs, args);
    }

    private void checkNormal(final Values values, final Set<IntPair> expectedMyTestPasedInPairs, final Set<IntPair> expectedBlablaPasedInPairs, final Set<IntPair> expectedFooPasedInPairs, String... args) {
        final int[] myTestCounter = new int[]{0};
        final int[] blablaCounter = new int[]{0};
        final int[] fooCounter = new int[]{0};

        @TestGroup
        class Normal {
            protected Values mySetup() {
                return values;
            }

            @TestCase
            @TestData("mySetup")
            public void myTest(int i, int j) {
                myTestCounter[0]++;
                Assert.assertTrue(expectedMyTestPasedInPairs.contains(new IntPair(i, j)));
            }

            @TestCase
            @TestData("mySetup")
            public void blabla(int i, int j) {
                blablaCounter[0]++;
                Assert.assertTrue(expectedBlablaPasedInPairs.contains(new IntPair(i, j)));
            }

            @TestCase
            @TestData("mySetup")
            public void foo(int i, int j) {
                fooCounter[0]++;
                Assert.assertTrue(expectedFooPasedInPairs.contains(new IntPair(i, j)));
            }
        }

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new Normal(), args);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(expectedMyTestPasedInPairs.size(), myTestCounter[0]);
        Assert.assertEquals(expectedBlablaPasedInPairs.size(), blablaCounter[0]);
        Assert.assertEquals(expectedFooPasedInPairs.size(), fooCounter[0]);
    }

    private void checkThreaded(final Values values, final Set<IntPair> expectedMyTestPasedInPairs, final Set<IntPair> expectedBlablaPasedInPairs, final Set<IntPair> expectedFooPasedInPairs, String... args) {

        final int[] myTestCounter = new int[]{0};
        final int[] blablaCounter = new int[]{0};
        final int[] fooCounter = new int[]{0};

//        @EventDispatchThread
        class Normal {
            protected Values mySetup() {
                return values;
            }

            @TestCase
            @TestData("mySetup")
            public void myTest(int i, int j) {
                Assert.assertTrue(SwingUtilities.isEventDispatchThread());
                myTestCounter[0]++;
                Assert.assertTrue(expectedMyTestPasedInPairs.contains(new IntPair(i, j)));
            }

            @TestCase
            @TestData("mySetup")
            public void blabla(int i, int j) {
                Assert.assertTrue(SwingUtilities.isEventDispatchThread());
                blablaCounter[0]++;
                Assert.assertTrue(expectedBlablaPasedInPairs.contains(new IntPair(i, j)));
            }

            @TestCase
            @TestData("mySetup")
            public void foo(int i, int j) {
                fooCounter[0]++;
                Assert.assertTrue(expectedFooPasedInPairs.contains(new IntPair(i, j)));
            }
        }

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new Normal(), args);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(expectedMyTestPasedInPairs.size(), myTestCounter[0]);
        Assert.assertEquals(expectedBlablaPasedInPairs.size(), blablaCounter[0]);
        Assert.assertEquals(expectedFooPasedInPairs.size(), fooCounter[0]);
    }


}
