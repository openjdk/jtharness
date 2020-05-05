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
package com.oracle.tck.lib.autd2.unittests.tgfported.testdata;

import com.oracle.tck.lib.autd2.unittests.BaseTestGroup;
import com.oracle.tck.lib.autd2.unittests.TU;
import com.sun.tck.lib.tgf.DataFactory;
import com.sun.tck.lib.tgf.TestData;
import com.sun.tck.lib.tgf.Values;
import com.sun.tck.test.TestCase;
import org.junit.Assert;

import java.util.HashSet;
import java.util.Set;

/**
 */
public class DataQueriesFromFields {


    @org.junit.Test
    public void test_1() {

        final Set<Integer> set = new HashSet<Integer>();

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            private ValuesHolder holder = new ValuesHolder();

            @TestCase
            @TestData("holder.setup")
            public void test(int i) throws Throwable {
                set.add(i);
            }
        }, TU.EMPTY_ARGV);

        Assert.assertTrue(status.isOK());
        Assert.assertEquals(4, set.size());
        Assert.assertTrue(set.contains(1));
        Assert.assertTrue(set.contains(3));
        Assert.assertTrue(set.contains(7));
        Assert.assertTrue(set.contains(9));
    }

    @org.junit.Test
    public void test_2() {

        final Set<Integer> set = new HashSet<Integer>();

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            private ValuesHolder2 holder2 = new ValuesHolder2();

            @TestCase
            @TestData("holder2.holder.setup")
            public void test(int i) throws Throwable {
                set.add(i);
            }
        }, TU.EMPTY_ARGV);

        Assert.assertTrue(status.isOK());
        Assert.assertEquals(4, set.size());
        Assert.assertTrue(set.contains(1));
        Assert.assertTrue(set.contains(3));
        Assert.assertTrue(set.contains(7));
        Assert.assertTrue(set.contains(9));
    }

    @org.junit.Test
    public void test_3() {

        final Set<Integer> set = new HashSet<Integer>();

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            private ValuesHolder3 holder3 = new ValuesHolder3();

            @TestCase
            @TestData("holder3.holder2.holder.setup")
            public void test(int i) throws Throwable {
                set.add(i);
            }
        }, TU.EMPTY_ARGV);

        Assert.assertTrue(status.isOK());
        Assert.assertEquals(4, set.size());
        Assert.assertTrue(set.contains(1));
        Assert.assertTrue(set.contains(3));
        Assert.assertTrue(set.contains(7));
        Assert.assertTrue(set.contains(9));
    }

    @org.junit.Test
    public void test_4() {

        final Set<Integer> set = new HashSet<Integer>();

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            private ValuesHolder3 holder3 = new ValuesHolder3();

            @TestCase
            @TestData("holder3.holder.setup")
            public void test(int i) throws Throwable {
                set.add(i);
            }
        }, TU.EMPTY_ARGV);

        Assert.assertTrue(status.isOK());
        Assert.assertEquals(4, set.size());
        Assert.assertTrue(set.contains(1));
        Assert.assertTrue(set.contains(3));
        Assert.assertTrue(set.contains(7));
        Assert.assertTrue(set.contains(9));
    }

    @org.junit.Test
    public void test_5() {

        final Set<Integer> set = new HashSet<Integer>();

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            private ValuesHolder4 holder4 = new ValuesHolder4();

            @TestCase
            @TestData("holder4.ints")
            public void test(int i) throws Throwable {
                set.add(i);
            }
        }, TU.EMPTY_ARGV);

        Assert.assertTrue(status.isOK());
        Assert.assertEquals(4, set.size());
        Assert.assertTrue(set.contains(344));
        Assert.assertTrue(set.contains(556));
        Assert.assertTrue(set.contains(655));
        Assert.assertTrue(set.contains(33));
    }


    class ValuesHolder {
        private Values setup = DataFactory.createColumn(1, 3, 7, 9);
    }

    class ValuesHolder2 {

        private ValuesHolder holder = new ValuesHolder();
    }
    class ValuesHolder3 {

        private ValuesHolder2 holder2() {
            return new ValuesHolder2();
        }
        private ValuesHolder holder() {
            return new ValuesHolder();
        }

    }

    class ValuesHolder4 {
        int[] ints = {344, 556, 655, 33};
    }

}
