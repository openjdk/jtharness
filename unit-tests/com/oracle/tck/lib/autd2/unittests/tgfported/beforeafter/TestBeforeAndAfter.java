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
package com.oracle.tck.lib.autd2.unittests.tgfported.beforeafter;

import com.oracle.tck.lib.autd2.unittests.BaseTestGroup;
import com.oracle.tck.lib.autd2.unittests.TU;
import com.sun.tck.lib.tgf.*;
import com.sun.tck.test.TestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class TestBeforeAndAfter {


    @Test
    public void plain() {

        final boolean[] afterCalled = new boolean[1];
        final boolean[] beforeCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];

        final int[] methodCounter = new int[]{0};
        final int[] afterCounter = new int[]{0};
        final int[] beforeCounter = new int[]{0};

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            protected Values mySetup() {
                return DataFactory.createColumn(1, 2, 3);
            }

            private void myAfter() {
                org.junit.Assert.assertTrue(beforeCalled[0]);
                org.junit.Assert.assertTrue(methodCalled[0]);
                org.junit.Assert.assertFalse(afterCalled[0]);
                afterCalled[0] = true;
                afterCounter[0]++;
            }

            private void myBefore() {
                org.junit.Assert.assertFalse(beforeCalled[0]);
                org.junit.Assert.assertFalse(methodCalled[0]);
                org.junit.Assert.assertFalse(afterCalled[0]);
                beforeCalled[0] = true;
                beforeCounter[0]++;
            }

            @TestCase
            @Before("myBefore")
            @After("myAfter")
            @TestData("mySetup")
            public void test(int i) {
                org.junit.Assert.assertFalse(afterCalled[0]);
                org.junit.Assert.assertTrue(beforeCalled[0]);
                methodCalled[0] = true;
                methodCounter[0]++;
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertTrue(afterCalled[0]);
        Assert.assertTrue(beforeCalled[0]);
        Assert.assertTrue(methodCalled[0]);
        Assert.assertEquals(3, methodCounter[0]);
        Assert.assertEquals(1, afterCounter[0]);
        Assert.assertEquals(1, beforeCounter[0]);
    }

    @Test
    public void plain_NoArgs() {

        final boolean[] afterCalled = new boolean[1];
        final boolean[] beforeCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];

        final int[] methodCounter = new int[]{0};
        final int[] afterCounter = new int[]{0};
        final int[] beforeCounter = new int[]{0};

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            protected Values mySetup() {
                return DataFactory.createColumn(1, 2, 3);
            }

            private void myAfter() {
                org.junit.Assert.assertTrue(beforeCalled[0]);
                org.junit.Assert.assertTrue(methodCalled[0]);
                org.junit.Assert.assertFalse(afterCalled[0]);
                afterCalled[0] = true;
                afterCounter[0]++;
            }

            private void myBefore() {
                org.junit.Assert.assertFalse(beforeCalled[0]);
                org.junit.Assert.assertFalse(methodCalled[0]);
                org.junit.Assert.assertFalse(afterCalled[0]);
                beforeCalled[0] = true;
                beforeCounter[0]++;
            }

            @Before("myBefore")
            @After("myAfter")
            @TestCase
            public void testWithNoArguments() {
                org.junit.Assert.assertFalse(afterCalled[0]);
                org.junit.Assert.assertTrue(beforeCalled[0]);
                methodCalled[0] = true;
                methodCounter[0]++;
            }
        }, TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertTrue(afterCalled[0]);
        Assert.assertTrue(beforeCalled[0]);
        Assert.assertTrue(methodCalled[0]);
        Assert.assertEquals(1, methodCounter[0]);
        Assert.assertEquals(1, afterCounter[0]);
        Assert.assertEquals(1, beforeCounter[0]);
    }

    @Test
    public void beforeThrowsException() {

        final boolean[] afterCalled = new boolean[1];
        final boolean[] beforeCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];

        final int[] methodCounter = new int[]{0};
        final int[] afterCounter = new int[]{0};
        final int[] beforeCounter = new int[]{0};

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            protected Values mySetup() {
                return DataFactory.createColumn(1, 2, 3);
            }

            private void myAfter() {
                afterCalled[0] = true;
                afterCounter[0]++;
                org.junit.Assert.fail("This must not be called");
            }

            private void myBefore() {
                org.junit.Assert.assertFalse(beforeCalled[0]);
                org.junit.Assert.assertFalse(methodCalled[0]);
                org.junit.Assert.assertFalse(afterCalled[0]);
                beforeCalled[0] = true;
                beforeCounter[0]++;
                throw new RuntimeException("Exception thrown from before");
            }

            @TestCase
            @Before("myBefore")
            @After("myAfter")
            @TestData("mySetup")
            public void test(int i) {
                methodCalled[0] = true;
                methodCounter[0]++;
                org.junit.Assert.fail("This must not be called");
            }
        }, TU.EMPTY_ARGV);
        Assert.assertFalse(status.isOK());
        Assert.assertFalse(afterCalled[0]);
        Assert.assertTrue(beforeCalled[0]);
        Assert.assertFalse(methodCalled[0]);
        Assert.assertEquals(0, methodCounter[0]);
        Assert.assertEquals(0, afterCounter[0]);
        Assert.assertEquals(1, beforeCounter[0]);
    }

    @Test
    public void beforeThrowsException_NoParams() {

        final boolean[] afterCalled = new boolean[1];
        final boolean[] beforeCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];

        final int[] methodCounter = new int[]{0};
        final int[] afterCounter = new int[]{0};
        final int[] beforeCounter = new int[]{0};

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            protected Values mySetup() {
                return DataFactory.createColumn(1, 2, 3);
            }

            private void myAfter() {
                afterCalled[0] = true;
                afterCounter[0]++;
                org.junit.Assert.fail("This must not be called");
            }

            private void myBefore() {
                org.junit.Assert.assertFalse(beforeCalled[0]);
                org.junit.Assert.assertFalse(methodCalled[0]);
                org.junit.Assert.assertFalse(afterCalled[0]);
                beforeCalled[0] = true;
                beforeCounter[0]++;
                throw new RuntimeException("Exception thrown from before");
            }

            @TestCase
            @Before("myBefore")
            @After("myAfter")
            public void test() {
                methodCalled[0] = true;
                methodCounter[0]++;
                org.junit.Assert.fail("This must not be called");
            }
        }, TU.EMPTY_ARGV);
        Assert.assertFalse(status.isOK());
        Assert.assertFalse(afterCalled[0]);
        Assert.assertTrue(beforeCalled[0]);
        Assert.assertFalse(methodCalled[0]);
        Assert.assertEquals(0, methodCounter[0]);
        Assert.assertEquals(0, afterCounter[0]);
        Assert.assertEquals(1, beforeCounter[0]);
    }


}
