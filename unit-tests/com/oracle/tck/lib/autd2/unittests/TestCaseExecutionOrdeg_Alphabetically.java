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

package com.oracle.tck.lib.autd2.unittests;

import com.sun.tck.test.TestCase;
import com.sun.tck.test.TestGroup;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 *
 */
public class TestCaseExecutionOrdeg_Alphabetically {



    @TestGroup
    public static class MyTG1 {

        static int counter1;
        static int counter2;
        static int counter3;
        static int counter4;

        static List<String> tcs = new ArrayList<String>();

        @TestCase
        public void tcA() {
            Assert.assertEquals(0, tcs.size());
            counter1++;
            tcs.add("tcA");
        }

        @TestCase
        public void tcB() {
            Assert.assertEquals(1, tcs.size());
            Assert.assertEquals("tcA", tcs.get(0));
            counter2++;
            tcs.add("tcB");
        }

        @TestCase
        public void tcC() {
            Assert.assertEquals(2, tcs.size());
            Assert.assertEquals("tcA", tcs.get(0));
            Assert.assertEquals("tcB", tcs.get(1));
            counter3++;
            tcs.add("tcC");
        }

        @TestCase
        public void tcD() {
            Assert.assertEquals(3, tcs.size());
            Assert.assertEquals("tcA", tcs.get(0));
            Assert.assertEquals("tcB", tcs.get(1));
            Assert.assertEquals("tcC", tcs.get(2));
            counter4++;
            tcs.add("tcD");
        }

    }

    @Test
    public void test01() {
        MyTG1.counter1 = 0;
        MyTG1.counter2 = 0;
        MyTG1.counter3 = 0;
        MyTG1.counter4 = 0;

        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new MyTG1(), TU.EMPTY_ARGV);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(1, MyTG1.counter1);
        Assert.assertEquals(1, MyTG1.counter2);
        Assert.assertEquals(1, MyTG1.counter3);
        Assert.assertEquals(1, MyTG1.counter4);

        Assert.assertEquals(4, MyTG1.tcs.size());
        Assert.assertEquals("tcA", MyTG1.tcs.get(0));
        Assert.assertEquals("tcB", MyTG1.tcs.get(1));
        Assert.assertEquals("tcC", MyTG1.tcs.get(2));
        Assert.assertEquals("tcD", MyTG1.tcs.get(3));

    }

}
