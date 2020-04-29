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

import com.oracle.tck.lib.autd2.TestResult;
import com.oracle.tck.lib.autd2.TestedStatement;

import com.sun.javatest.Status;
import com.sun.tck.TU;
import com.sun.tck.test.TestCase;
import com.sun.tck.test.TestGroup;
import org.junit.Assert;
import org.junit.Test;

import java.io.PrintWriter;

/**
 *
 */
public class PassingSimpleTestCases {

    @TestGroup
    public static class OneVoidTestCase {
        static int testCaseMethodCalled;

        @TestCase
        public void test() {
            testCaseMethodCalled++;
        }
    }

    @Test
    public void oneTestCase() {
        OneVoidTestCase.testCaseMethodCalled = 0;
        TestResult s = TU.runTestGroup(new OneVoidTestCase(), TU.EMPTY_ARGV);
        Assert.assertTrue(s.isOK());
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
        Assert.assertEquals(1, OneVoidTestCase.testCaseMethodCalled);
    }

    @TestGroup
    public static class OneStatusTestCase {
        static int testCaseMethodCalled;

        @TestCase
        public Status test() {
            testCaseMethodCalled++;
            return Status.passed("OK");
        }
    }

    @Test
    public void oneTestCase_status() {
        OneStatusTestCase.testCaseMethodCalled = 0;
        TestResult s = TU.runTestGroup(new OneStatusTestCase(), TU.EMPTY_ARGV);
        Assert.assertTrue(s.isOK());
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
        Assert.assertEquals(1, OneStatusTestCase.testCaseMethodCalled);
    }

    @TestGroup
    public static class TwoVoidTestCases {
        static int testCaseMethodCalled1;
        static int testCaseMethodCalled2;

        @TestCase
        public void test1() {
            testCaseMethodCalled1++;
        }

        @TestCase
        public void test2() {
            testCaseMethodCalled2++;
        }
    }

    @Test
    public void twoTestCases() {
        TwoVoidTestCases.testCaseMethodCalled1 = 0;
        TwoVoidTestCases.testCaseMethodCalled2 = 0;
        TestResult s = TU.runTestGroup(new TwoVoidTestCases(), TU.EMPTY_ARGV);
        Assert.assertTrue(s.isOK());
        Assert.assertEquals(1, TwoVoidTestCases.testCaseMethodCalled1);
        Assert.assertEquals(1, TwoVoidTestCases.testCaseMethodCalled2);
        Assert.assertEquals("test cases: 2; all passed", s.getMessage());
    }

    @TestGroup

    public static class TwoStatusTestCases {
        static int testCaseMethodCalled1;
        static int testCaseMethodCalled2;

        @TestCase
        public Status test1() {
            testCaseMethodCalled1++;
            return Status.passed("OK");
        }

        @TestCase
        public Status test2() {
            testCaseMethodCalled2++;
            return Status.passed("OK");
        }
    }

    @TestGroup

    public static class TwoStatusTestCasesWAssertion {
        static int testCaseMethodCalled1;
        static int testCaseMethodCalled2;

        @TestCase
        @TestedStatement(value = "wlerjglwe;jrgl;erjg", source = "a.g.f.dfg")
        public Status test1() {
            testCaseMethodCalled1++;
            return Status.passed("OK");
        }

        @TestCase
        @TestedStatement("wlerjglwe;jrgl;erjg")
        public Status test2() {
            testCaseMethodCalled2++;
            return Status.passed("OK");
        }
    }

    @Test
    public void twoStatusTestCases() {
        TwoStatusTestCases.testCaseMethodCalled1 = 0;
        TwoStatusTestCases.testCaseMethodCalled2 = 0;
        TestResult s = TU.runTestGroup(new TwoStatusTestCases(), TU.EMPTY_ARGV);
        Assert.assertTrue(s.isOK());
        Assert.assertEquals(1, TwoStatusTestCases.testCaseMethodCalled1);
        Assert.assertEquals(1, TwoStatusTestCases.testCaseMethodCalled2);
        Assert.assertEquals("test cases: 2; all passed", s.getMessage());
    }

    @Test
    public void twoStatusTestCases_1() {
        TwoStatusTestCasesWAssertion.testCaseMethodCalled1 = 0;
        TwoStatusTestCasesWAssertion.testCaseMethodCalled2 = 0;
        TestResult s = TU.runTestGroup(new TwoStatusTestCasesWAssertion(), TU.EMPTY_ARGV);
        Assert.assertTrue(s.isOK());
        Assert.assertEquals(1, TwoStatusTestCasesWAssertion.testCaseMethodCalled1);
        Assert.assertEquals(1, TwoStatusTestCasesWAssertion.testCaseMethodCalled2);
        Assert.assertEquals("test cases: 2; all passed", s.getMessage());
    }


    @TestGroup
    public static class ThreeVoidTestCases {
        static int testCaseMethodCalled1;
        static int testCaseMethodCalled2;
        static int testCaseMethodCalled3;

        @TestCase
        public void test1() {
            testCaseMethodCalled1++;
        }

        @TestCase
        public void test2() {
            testCaseMethodCalled2++;
        }

        @TestCase
        public void test3() {
            testCaseMethodCalled3++;
        }
    }

    @Test
    public void threeTestCases() {
        ThreeVoidTestCases.testCaseMethodCalled1 = 0;
        ThreeVoidTestCases.testCaseMethodCalled2 = 0;
        ThreeVoidTestCases.testCaseMethodCalled3 = 0;
        TestResult s = TU.runTestGroup(new ThreeVoidTestCases(), TU.EMPTY_ARGV);
        Assert.assertTrue(s.isOK());
        Assert.assertEquals(1, ThreeVoidTestCases.testCaseMethodCalled1);
        Assert.assertEquals(1, ThreeVoidTestCases.testCaseMethodCalled2);
        Assert.assertEquals(1, ThreeVoidTestCases.testCaseMethodCalled3);
        Assert.assertEquals("test cases: 3; all passed", s.getMessage());
    }

    @TestGroup
    public static class ThreeVoidTestCases_WAssertion {
        static int testCaseMethodCalled1;
        static int testCaseMethodCalled2;
        static int testCaseMethodCalled3;

        @TestCase
        @TestedStatement(value = "wl;etj;lwerjt ;lwerj l;wejrtl;wert", source = "sdfgsdg")
        public void test1() {
            testCaseMethodCalled1++;
        }

        @TestCase
        public void test2() {
            testCaseMethodCalled2++;
        }

        @TestCase
        @TestedStatement(value = "wl;etj;lwerjt ;lwerj l;wejrtl;wert", source = "d.f.g.h")
        public void test3() {
            testCaseMethodCalled3++;
        }
    }

    @Test
    public void threeTestCases_wAssertion() {
        ThreeVoidTestCases_WAssertion.testCaseMethodCalled1 = 0;
        ThreeVoidTestCases_WAssertion.testCaseMethodCalled2 = 0;
        ThreeVoidTestCases_WAssertion.testCaseMethodCalled3 = 0;
        TestResult s = TU.runTestGroup(new ThreeVoidTestCases_WAssertion(), TU.EMPTY_ARGV);
        Assert.assertTrue(s.isOK());
        Assert.assertEquals(1, ThreeVoidTestCases_WAssertion.testCaseMethodCalled1);
        Assert.assertEquals(1, ThreeVoidTestCases_WAssertion.testCaseMethodCalled2);
        Assert.assertEquals(1, ThreeVoidTestCases_WAssertion.testCaseMethodCalled3);
        Assert.assertEquals("test cases: 3; all passed", s.getMessage());
    }

    @TestGroup
    public static class ThreeMixedTestCases {
        static int testCaseMethodCalled1;
        static int testCaseMethodCalled2;
        static int testCaseMethodCalled3;

        @TestCase
        public void test1() {
            testCaseMethodCalled1++;
        }

        @TestCase
        public Status test2() {
            testCaseMethodCalled2++;
            return Status.passed("OK");
        }

        @TestCase
        public void test3() {
            testCaseMethodCalled3++;
        }
    }

    @TestGroup
    public static class ThreeMixedTestCases2 {
        static int testCaseMethodCalled1;
        static int testCaseMethodCalled2;
        static int testCaseMethodCalled3;

        @TestCase
        public void test1() {
            testCaseMethodCalled1++;
        }

        @TestCase
        public Status test2() {
            testCaseMethodCalled2++;
            return Status.passed("OK");
        }

        @TestCase
        public void test3() {
            testCaseMethodCalled3++;
        }
    }

    @Test
    public void threeMixedTestCases() {
        ThreeMixedTestCases.testCaseMethodCalled1 = 0;
        ThreeMixedTestCases.testCaseMethodCalled2 = 0;
        ThreeMixedTestCases.testCaseMethodCalled3 = 0;
        TestResult s = TU.runTestGroup(new ThreeMixedTestCases(), TU.EMPTY_ARGV);
        Assert.assertTrue(s.isOK());
        Assert.assertEquals(1, ThreeMixedTestCases.testCaseMethodCalled1);
        Assert.assertEquals(1, ThreeMixedTestCases.testCaseMethodCalled2);
        Assert.assertEquals(1, ThreeMixedTestCases.testCaseMethodCalled3);
        Assert.assertEquals("test cases: 3; all passed", s.getMessage());
    }

    @Test
    public void threeMixedTestCases_selfRun() {
        ThreeMixedTestCases2.testCaseMethodCalled1 = 0;
        ThreeMixedTestCases2.testCaseMethodCalled2 = 0;
        ThreeMixedTestCases2.testCaseMethodCalled3 = 0;
        PrintWriter log = new PrintWriter(System.err, true);
        PrintWriter ref = new PrintWriter(System.out, true);
        TestResult s = TU.runTestGroup(new ThreeMixedTestCases2());
        Assert.assertTrue(s.isOK());
        Assert.assertEquals(1, ThreeMixedTestCases2.testCaseMethodCalled1);
        Assert.assertEquals(1, ThreeMixedTestCases2.testCaseMethodCalled2);
        Assert.assertEquals(1, ThreeMixedTestCases2.testCaseMethodCalled3);
        Assert.assertEquals("test cases: 3; all passed", s.getMessage());
    }


    @TestGroup
    public static class FourVoidTestCases {
        static int testCaseMethodCalled1;
        static int testCaseMethodCalled2;
        static int testCaseMethodCalled3;
        static int testCaseMethodCalled4;

        @TestCase
        public void test1() {
            testCaseMethodCalled1++;
        }

        @TestCase
        public void test2() {
            testCaseMethodCalled2++;
        }

        @TestCase
        public void test3() {
            testCaseMethodCalled3++;
        }

        @TestCase
        public void test4() {
            testCaseMethodCalled4++;
        }
    }

    @Test
    public void fourTestCases() {
        FourVoidTestCases.testCaseMethodCalled1 = 0;
        FourVoidTestCases.testCaseMethodCalled2 = 0;
        FourVoidTestCases.testCaseMethodCalled3 = 0;
        FourVoidTestCases.testCaseMethodCalled4 = 0;
        TestResult s = TU.runTestGroup(new FourVoidTestCases(), TU.EMPTY_ARGV);
        Assert.assertTrue(s.isOK());
        Assert.assertEquals(1, FourVoidTestCases.testCaseMethodCalled1);
        Assert.assertEquals(1, FourVoidTestCases.testCaseMethodCalled2);
        Assert.assertEquals(1, FourVoidTestCases.testCaseMethodCalled3);
        Assert.assertEquals(1, FourVoidTestCases.testCaseMethodCalled4);
        Assert.assertEquals("test cases: 4; all passed", s.getMessage());
    }


}
