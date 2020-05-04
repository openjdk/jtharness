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
import com.oracle.tck.lib.autd2.TestRunner;
import com.oracle.tck.lib.autd2.TestedStatement;

import com.sun.javatest.Status;
import com.sun.tck.lib.AssertionFailedException;
import com.sun.tck.lib.ExpectedExceptions;
import com.sun.tck.lib.TestFailedException;
import com.sun.tck.test.TestCase;
import com.sun.tck.test.TestGroup;
import com.sun.tck.test.TestedAPI;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class FailingSimpleTestCases {

    PrintWriter log = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.err, StandardCharsets.UTF_8)), true);
    PrintWriter ref = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8)), true);

    @TestGroup
    public static class OneVoidTestCase {
        static int testCaseMethodCalled;

        @TestCase
        public void test123423() {
            testCaseMethodCalled++;
            throw new AssertionFailedException("Something baaad");
        }
    }

    @Test
    public void oneTestCase() {
        OneVoidTestCase.testCaseMethodCalled = 0;
        TestResult s = TU.runTestGroup(new OneVoidTestCase(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals("test cases: 1; all failed", s.getMessage());
        Assert.assertEquals(1, OneVoidTestCase.testCaseMethodCalled);
    }

    @TestGroup
    public static class OneVoidTestCase_wTestedStatement_NoTestedAPI {
        static int testCaseMethodCalled;

        @TestCase
        @TestedStatement("statement to test")
        public void test123423() {
            testCaseMethodCalled++;
            throw new AssertionFailedException("Something baaad");
        }
    }

    @Test
    public void oneTestCase_wTestedStatement_noTestedAPI() {
        OneVoidTestCase_wTestedStatement_NoTestedAPI.testCaseMethodCalled = 0;
        TestResult s = TU.runTestGroup(new OneVoidTestCase_wTestedStatement_NoTestedAPI(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals("test cases: 1; all failed", s.getMessage());
        Assert.assertEquals(1, OneVoidTestCase_wTestedStatement_NoTestedAPI.testCaseMethodCalled);
    }

    @Test
    public void oneTestCase_wTestedStatement_noTestedAPI_testSelfRun() {
        OneVoidTestCase_wTestedStatement_NoTestedAPI.testCaseMethodCalled = 0;
        PrintWriter log = new PrintWriter(System.err, true);
        PrintWriter ref = new PrintWriter(System.out, true);
        TestResult s = TU.runTestGroup(new OneVoidTestCase_wTestedStatement_NoTestedAPI());
        Assert.assertFalse(s.isOK());
        Assert.assertEquals("test cases: 1; all failed", s.getMessage());
        Assert.assertEquals(1, OneVoidTestCase_wTestedStatement_NoTestedAPI.testCaseMethodCalled);
    }

    @TestGroup
    @TestedAPI(testedPackage = "a.b.com")
    public static class OneVoidTestCase_wTestedStatement_TestedAPI {
        static int testCaseMethodCalled;

        @TestCase
        @TestedStatement("statement to test")
        public void test123423() {
            testCaseMethodCalled++;
            throw new AssertionFailedException("Something baaad");
        }
    }

    @Test
    public void oneTestCase_wTestedStatement_TestedAPI() {
        OneVoidTestCase_wTestedStatement_TestedAPI.testCaseMethodCalled = 0;
        TestResult s = TU.runTestGroup(new OneVoidTestCase_wTestedStatement_TestedAPI(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals("test cases: 1; all failed", s.getMessage());
        Assert.assertEquals(1, OneVoidTestCase_wTestedStatement_TestedAPI.testCaseMethodCalled);
    }


    @Test
    public void testedAPI_package() {

        @TestedAPI(testedPackage = "x.e.r")
        @TestGroup
        class MyTest {
            @TestCase
            @TestedStatement("a statement")
            public void testCase() {
                Assert.fail();
            }
        }

        TestResult s = TU.runTestGroup(new MyTest(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals("test cases: 1; all failed", s.getMessage());
    }

    @Test
    public void testedAPI_noTestedAPI() {

        @TestGroup
        class MyTest {
            @TestCase
            @TestedStatement("a statement")
            public void testCase() {
                Assert.fail();
            }
        }

        TestResult s = TU.runTestGroup(new MyTest(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals("test cases: 1; all failed", s.getMessage());
    }


    @Test
    public void testedAPI_packages() {

        @TestedAPI(testedPackage = {"x.e.r", "s.e.g", "e.t.r"})
        @TestGroup
        class MyTest {
            @TestCase
            @TestedStatement("a statement")
            public void testCase() {
                Assert.fail();
            }
        }

        TestResult s = TU.runTestGroup(new MyTest(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals("test cases: 1; all failed", s.getMessage());
    }

    @Test
    public void testedAPI_packages_class() {

        @TestedAPI(
                testedPackage = {"x.e.r", "s.e.g", "e.t.r"},
                testedClass = "TheClass")
        @TestGroup
        class MyTest {
            @TestCase
            @TestedStatement("a statement")
            public void testCase() {
                Assert.fail();
            }
        }

        TestResult s = TU.runTestGroup(new MyTest(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals("test cases: 1; all failed", s.getMessage());
    }

    @Test
    public void testedAPI_packages_classes() {

        @TestedAPI(
                testedPackage = {"x.e.r", "s.e.g", "e.t.r"},
                testedClass = {"TheClass", "AnotherClass"})
        @TestGroup
        class MyTest {
            @TestCase
            @TestedStatement("a statement")
            public void testCase() {
                Assert.fail();
            }
        }

        TestResult s = TU.runTestGroup(new MyTest(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals("test cases: 1; all failed", s.getMessage());
    }

    @Test
    public void testedAPI_packages_classes_method() {

        @TestedAPI(
                testedPackage = {"x.e.r", "s.e.g", "e.t.r"},
                testedClass = {"TheClass", "AnotherClass"},
                testedMember = "method")
        @TestGroup
        class MyTest {
            @TestCase
            @TestedStatement("a statement")
            public void testCase() {
                Assert.fail();
            }
        }

        TestResult s = TU.runTestGroup(new MyTest(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals("test cases: 1; all failed", s.getMessage());
    }

    @Test
    public void testedAPI_packages_classes_methods() {

        @TestedAPI(
                testedPackage = {"x.e.r", "s.e.g", "e.t.r"},
                testedClass = {"TheClass", "AnotherClass"},
                testedMember = {"method1", "method2"})
        @TestGroup
        class MyTest {
            @TestCase
            @TestedStatement("a statement")
            public void testCase() {
                Assert.fail();
            }
        }

        TestResult s = TU.runTestGroup(new MyTest(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals("test cases: 1; all failed", s.getMessage());
    }

    @Test
    public void testedAPI_package_class_method() {

        @TestedAPI(
                testedPackage = "s.e.g",
                testedClass = "TheClass",
                testedMember = "method1")
        @TestGroup
        class MyTest {
            @TestCase
            @TestedStatement("a statement")
            public void testCase() {
                Assert.fail();
            }
        }

        TestResult s = TU.runTestGroup(new MyTest(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals("test cases: 1; all failed", s.getMessage());
    }

    @Test
    public void testedAPI_package_class_methods() {

        @TestedAPI(
                testedPackage = "s.e.g",
                testedClass = "TheClass",
                testedMember = {"method1", "method2"})
        @TestGroup
        class MyTest {
            @TestCase
            @TestedStatement("a statement")
            public void testCase() {
                Assert.fail();
            }
        }

        TestResult s = TU.runTestGroup(new MyTest(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals("test cases: 1; all failed", s.getMessage());
    }

    @Test
    public void testedAPI_package_classes_methods() {

        @TestedAPI(
                testedPackage = "s.e.g",
                testedClass = {"TheClass", "TheClass"},
                testedMember = {"method1", "method2"})
        @TestGroup
        class MyTest {
            @TestCase
            @TestedStatement("a statement")
            public void testCase() {
                Assert.fail();
            }
        }

        TestResult s = TU.runTestGroup(new MyTest(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals("test cases: 1; all failed", s.getMessage());
    }

    @Test
    public void testedAPI_package_classes() {

        @TestedAPI(
                testedPackage = "s.e.g",
                testedClass = {"TheClass", "TheClass"})
        @TestGroup
        class MyTest {
            @TestCase
            @TestedStatement("a statement")
            public void testCase() {
                Assert.fail();
            }
        }

        TestResult s = TU.runTestGroup(new MyTest(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals("test cases: 1; all failed", s.getMessage());
    }

    @TestGroup

    public static class OneStatusTestCase {
        static int testCaseMethodCalled;

        @TestCase
        public Status test43563564356() {
            testCaseMethodCalled++;
            return Status.failed("OK");
        }
    }

    @TestGroup
    public static class OneStatusTestCase_extendsAUTD2 {
        static int testCaseMethodCalled;

        @TestCase
        public Status test43563564356() {
            testCaseMethodCalled++;
            return Status.failed("OK");
        }
    }

    @Test
    public void oneTestCase_status() {
        OneStatusTestCase.testCaseMethodCalled = 0;
        TestResult s = TU.runTestGroup(new OneStatusTestCase());
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals("test cases: 1; all failed", s.getMessage());
        Assert.assertEquals(1, OneStatusTestCase.testCaseMethodCalled);
    }

    @Test
    public void oneTestCase_status_extendingAUTD2_selfRun() {
        OneStatusTestCase_extendsAUTD2.testCaseMethodCalled = 0;
        PrintWriter log = new PrintWriter(System.err, true);
        PrintWriter ref = new PrintWriter(System.out, true);
        TestResult s = TU.runTestGroup( new OneStatusTestCase_extendsAUTD2());
        Assert.assertFalse(s.isOK());
        Assert.assertEquals("test cases: 1; all failed", s.getMessage());
        Assert.assertEquals(1, OneStatusTestCase_extendsAUTD2.testCaseMethodCalled);
    }

    @TestGroup
    public static class TwoVoidTestCases_OneFails {
        static int testCaseMethodCalled1;
        static int testCaseMethodCalled2;

        @TestCase
        public void test1() {
            testCaseMethodCalled1++;
            throw new AssertionFailedException("Something baaad");
        }

        @TestCase
        public void test2() {
            testCaseMethodCalled2++;
        }
    }


    @TestGroup
    public static class TwoVoidTestCases_OneFails_wAssertion {
        static int testCaseMethodCalled1;
        static int testCaseMethodCalled2;

        @TestCase
        @TestedStatement(value = "test something", source = "a.b.c.d")
        public void test1() {
            testCaseMethodCalled1++;
            throw new AssertionFailedException("Something baaad");
        }

        @TestCase
        public void test2() {
            testCaseMethodCalled2++;
        }
    }

    @Test
    public void twoTestCases() {
        TwoVoidTestCases_OneFails.testCaseMethodCalled1 = 0;
        TwoVoidTestCases_OneFails.testCaseMethodCalled2 = 0;
        TestResult s = TU.runTestGroup(new TwoVoidTestCases_OneFails(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals(1, TwoVoidTestCases_OneFails.testCaseMethodCalled1);
        Assert.assertEquals(1, TwoVoidTestCases_OneFails.testCaseMethodCalled2);
        Assert.assertEquals("test cases: 2; passed: 1; failed: 1; failed: [test1]", s.getMessage());
    }

    @Test
    public void twoTestCases_wAssertion() {
        TwoVoidTestCases_OneFails_wAssertion.testCaseMethodCalled1 = 0;
        TwoVoidTestCases_OneFails_wAssertion.testCaseMethodCalled2 = 0;
        TestResult s = TU.runTestGroup(new TwoVoidTestCases_OneFails_wAssertion(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals(1, TwoVoidTestCases_OneFails_wAssertion.testCaseMethodCalled1);
        Assert.assertEquals(1, TwoVoidTestCases_OneFails_wAssertion.testCaseMethodCalled2);
        Assert.assertEquals("test cases: 2; passed: 1; failed: 1; failed: [test1]", s.getMessage());
    }

    @TestGroup
    public static class TwoVoidTestCases_BothFails {
        static int testCaseMethodCalled1;
        static int testCaseMethodCalled2;

        @TestCase
        public void test1() {
            testCaseMethodCalled1++;
            throw new AssertionFailedException("Something baaad 1");
        }

        @TestCase
        public void test2() {
            testCaseMethodCalled2++;
            throw new AssertionFailedException("Something baaad 2");
        }
    }

    @Test
    public void twoTestCases_BothFails() {
        TwoVoidTestCases_BothFails.testCaseMethodCalled1 = 0;
        TwoVoidTestCases_BothFails.testCaseMethodCalled2 = 0;
        TestResult s = TU.runTestGroup(new TwoVoidTestCases_BothFails(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals(1, TwoVoidTestCases_BothFails.testCaseMethodCalled1);
        Assert.assertEquals(1, TwoVoidTestCases_BothFails.testCaseMethodCalled2);
        Assert.assertEquals("test cases: 2; all failed", s.getMessage());
    }

    @TestGroup
    public static class TwoStatusTestCases_BothFail {
        static int testCaseMethodCalled1;
        static int testCaseMethodCalled2;

        @TestCase
        public Status test1() {
            testCaseMethodCalled1++;
            return Status.failed("Failed");
        }

        @TestCase
        public Status test2() {
            testCaseMethodCalled2++;
            return Status.failed("Failed");
        }
    }

    @Test
    public void twoStatusTestCases() {
        TwoStatusTestCases_BothFail.testCaseMethodCalled1 = 0;
        TwoStatusTestCases_BothFail.testCaseMethodCalled2 = 0;
        TestResult s = TU.runTestGroup(new TwoStatusTestCases_BothFail(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals(1, TwoStatusTestCases_BothFail.testCaseMethodCalled1);
        Assert.assertEquals(1, TwoStatusTestCases_BothFail.testCaseMethodCalled2);
        Assert.assertEquals("test cases: 2; all failed", s.getMessage());
    }

    @Test
    public void twoStatusTestCases_run2() {
        TwoStatusTestCases_BothFail.testCaseMethodCalled1 = 0;
        TwoStatusTestCases_BothFail.testCaseMethodCalled2 = 0;
        PrintWriter log = new PrintWriter(System.err, true);
        PrintWriter ref = new PrintWriter(System.out, true);
        TestResult s = TU.runTestGroup(new TwoStatusTestCases_BothFail(), log, ref, TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals(1, TwoStatusTestCases_BothFail.testCaseMethodCalled1);
        Assert.assertEquals(1, TwoStatusTestCases_BothFail.testCaseMethodCalled2);
        Assert.assertEquals("test cases: 2; all failed", s.getMessage());
    }

    @Test
    public void twoStatusTestCases_selfRun() {
        TwoStatusTestCases_BothFail.testCaseMethodCalled1 = 0;
        TwoStatusTestCases_BothFail.testCaseMethodCalled2 = 0;
        PrintWriter log = new PrintWriter(System.err, true);
        PrintWriter ref = new PrintWriter(System.out, true);
        TestResult s = TU.runTestGroup(new TwoStatusTestCases_BothFail());
        Assert.assertFalse(s.isOK());
        Assert.assertEquals(1, TwoStatusTestCases_BothFail.testCaseMethodCalled1);
        Assert.assertEquals(1, TwoStatusTestCases_BothFail.testCaseMethodCalled2);
        Assert.assertEquals("test cases: 2; all failed", s.getMessage());
    }

    @TestGroup
    public static class TwoStatusTestCases_OneFails {
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
            return Status.failed("Failed");
        }
    }

    @Test
    public void twoStatusTestCases_oneFails() {
        TwoStatusTestCases_OneFails.testCaseMethodCalled1 = 0;
        TwoStatusTestCases_OneFails.testCaseMethodCalled2 = 0;
        TestResult s = TU.runTestGroup(new TwoStatusTestCases_OneFails(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals(1, TwoStatusTestCases_OneFails.testCaseMethodCalled1);
        Assert.assertEquals(1, TwoStatusTestCases_OneFails.testCaseMethodCalled2);
        Assert.assertEquals("test cases: 2; passed: 1; failed: 1; failed: [test2]", s.getMessage());
    }

    @TestGroup
    public static class TwoStatusTestCases_OneFails_ExceptionThrowing {
        static int testCaseMethodCalled1;
        static int testCaseMethodCalled2;

        @TestCase
        @ExpectedExceptions(IllegalArgumentException.class)
        public Status test1() {
            testCaseMethodCalled1++;
            throw new IllegalArgumentException();
        }

        @TestCase
        public Status test2() {
            testCaseMethodCalled2++;
            return Status.failed("Failed");
        }
    }

    @Test
    public void twoStatusTestCases_oneFails_ExceptionThrowing() {
        TwoStatusTestCases_OneFails_ExceptionThrowing.testCaseMethodCalled1 = 0;
        TwoStatusTestCases_OneFails_ExceptionThrowing.testCaseMethodCalled2 = 0;
        TestResult s = TU.runTestGroup(new TwoStatusTestCases_OneFails_ExceptionThrowing(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals(1, TwoStatusTestCases_OneFails_ExceptionThrowing.testCaseMethodCalled1);
        Assert.assertEquals(1, TwoStatusTestCases_OneFails_ExceptionThrowing.testCaseMethodCalled2);
        Assert.assertEquals("test cases: 2; passed: 1; failed: 1; failed: [test2]", s.getMessage());
    }


    @TestGroup
    public static class TwoStatusTestCases_OneFails_ExceptionThrowing_1 {
        static int testCaseMethodCalled1;
        static int testCaseMethodCalled2;

        @TestCase
        @ExpectedExceptions(IllegalArgumentException.class)
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

    @Test
    public void twoStatusTestCases_oneFails_ExceptionThrowing_1() {
        TwoStatusTestCases_OneFails_ExceptionThrowing_1.testCaseMethodCalled1 = 0;
        TwoStatusTestCases_OneFails_ExceptionThrowing_1.testCaseMethodCalled2 = 0;
        TestResult s = TU.runTestGroup(new TwoStatusTestCases_OneFails_ExceptionThrowing_1(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals(1, TwoStatusTestCases_OneFails_ExceptionThrowing_1.testCaseMethodCalled1);
        Assert.assertEquals(1, TwoStatusTestCases_OneFails_ExceptionThrowing_1.testCaseMethodCalled2);
        Assert.assertEquals("test cases: 2; passed: 1; failed: 1; failed: [test1]", s.getMessage());
    }

    @TestGroup
    public static class ThreeVoidTestCases {
        static int testCaseMethodCalled1;
        static int testCaseMethodCalled2;
        static int testCaseMethodCalled3;

        @TestCase
        public void test1() {
            testCaseMethodCalled1++;
            throw new AssertionFailedException("Something baaad");
        }

        @TestCase
        public void test2() {
            testCaseMethodCalled2++;
            throw new AssertionFailedException("Something baaad");
        }

        @TestCase
        public void test3() {
            testCaseMethodCalled3++;
            throw new AssertionFailedException("Something baaad");
        }
    }

    @TestGroup
//
    public static class ThreeVoidTestCases_WStatusProcessor {
        static int testCaseMethodCalled1;
        static int testCaseMethodCalled2;
        static int testCaseMethodCalled3;

        @TestCase
        public void test1() {
            testCaseMethodCalled1++;
            throw new AssertionFailedException("Something baaad");
        }

        @TestCase
        public void test2() {
            testCaseMethodCalled2++;
            throw new AssertionFailedException("Something baaad");
        }

        @TestCase
        public void test3() {
            testCaseMethodCalled3++;
            throw new AssertionFailedException("Something baaad");
        }
    }

    @Test
    public void threeTestCases() {
        ThreeVoidTestCases.testCaseMethodCalled1 = 0;
        ThreeVoidTestCases.testCaseMethodCalled2 = 0;
        ThreeVoidTestCases.testCaseMethodCalled3 = 0;
        TestResult s = TU.runTestGroup(new ThreeVoidTestCases(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals(1, ThreeVoidTestCases.testCaseMethodCalled1);
        Assert.assertEquals(1, ThreeVoidTestCases.testCaseMethodCalled2);
        Assert.assertEquals(1, ThreeVoidTestCases.testCaseMethodCalled3);
        Assert.assertEquals("test cases: 3; all failed", s.getMessage());
    }

    @Test
    public void threeTestCases_selfRun() {
        ThreeVoidTestCases.testCaseMethodCalled1 = 0;
        ThreeVoidTestCases.testCaseMethodCalled2 = 0;
        ThreeVoidTestCases.testCaseMethodCalled3 = 0;
        PrintWriter log = new PrintWriter(System.err, true);
        PrintWriter ref = new PrintWriter(System.out, true);
        TestResult s = TU.runTestGroup(new ThreeVoidTestCases());
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals(1, ThreeVoidTestCases.testCaseMethodCalled1);
        Assert.assertEquals(1, ThreeVoidTestCases.testCaseMethodCalled2);
        Assert.assertEquals(1, ThreeVoidTestCases.testCaseMethodCalled3);
        Assert.assertEquals("test cases: 3; all failed", s.getMessage());
    }

    @Test
    public void threeVoidTestCases_wStatusRecognizingProcessor() {
        ThreeVoidTestCases_WStatusProcessor.testCaseMethodCalled1 = 0;
        ThreeVoidTestCases_WStatusProcessor.testCaseMethodCalled2 = 0;
        ThreeVoidTestCases_WStatusProcessor.testCaseMethodCalled3 = 0;
        TestResult s = TU.runTestGroup(new ThreeVoidTestCases_WStatusProcessor(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals(1, ThreeVoidTestCases_WStatusProcessor.testCaseMethodCalled1);
        Assert.assertEquals(1, ThreeVoidTestCases_WStatusProcessor.testCaseMethodCalled2);
        Assert.assertEquals(1, ThreeVoidTestCases_WStatusProcessor.testCaseMethodCalled3);
        Assert.assertEquals("test cases: 3; all failed", s.getMessage());
    }

    @TestGroup

    public static class ThreeMixedTestCases_AllFailed {
        static int testCaseMethodCalled1;
        static int testCaseMethodCalled2;
        static int testCaseMethodCalled3;

        @TestCase
        public void test1() {
            testCaseMethodCalled1++;
            throw new AssertionFailedException("Something baaad");
        }

        @TestCase
        public Status test2() {
            testCaseMethodCalled2++;
            return Status.failed("Failed");
        }

        @TestCase
        public void test3() {
            testCaseMethodCalled3++;
            throw new AssertionFailedException("Something baaad");
        }
    }

    @Test
    public void threeMixedTestCases() {
        ThreeMixedTestCases_AllFailed.testCaseMethodCalled1 = 0;
        ThreeMixedTestCases_AllFailed.testCaseMethodCalled2 = 0;
        ThreeMixedTestCases_AllFailed.testCaseMethodCalled3 = 0;
        TestResult s = TU.runTestGroup(new ThreeMixedTestCases_AllFailed(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals(1, ThreeMixedTestCases_AllFailed.testCaseMethodCalled1);
        Assert.assertEquals(1, ThreeMixedTestCases_AllFailed.testCaseMethodCalled2);
        Assert.assertEquals(1, ThreeMixedTestCases_AllFailed.testCaseMethodCalled3);
        Assert.assertEquals("test cases: 3; all failed", s.getMessage());
    }

    @TestGroup
//
    public static class ThreeMixedTestCases_FailedStatus {
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
            return Status.failed("Failed");
        }

        @TestCase
        public void test3() {
            testCaseMethodCalled3++;
        }
    }

    @Test
    public void threeMixedTestCases_FailedStatus() {
        ThreeMixedTestCases_FailedStatus.testCaseMethodCalled1 = 0;
        ThreeMixedTestCases_FailedStatus.testCaseMethodCalled2 = 0;
        ThreeMixedTestCases_FailedStatus.testCaseMethodCalled3 = 0;
        TestResult s = TU.runTestGroup(new ThreeMixedTestCases_FailedStatus(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals(1, ThreeMixedTestCases_FailedStatus.testCaseMethodCalled1);
        Assert.assertEquals(1, ThreeMixedTestCases_FailedStatus.testCaseMethodCalled2);
        Assert.assertEquals(1, ThreeMixedTestCases_FailedStatus.testCaseMethodCalled3);
        Assert.assertEquals("test cases: 3; passed: 2; failed: 1; failed: [test2]", s.getMessage());
    }

    @TestGroup
//
    public static class ThreeMixedTestCases_ExceptionExpectedFromStatus_1 {
        static int testCaseMethodCalled1;
        static int testCaseMethodCalled2;
        static int testCaseMethodCalled3;

        @TestCase
        public void test1() {
            testCaseMethodCalled1++;
        }

        @TestCase
        @ExpectedExceptions(IllegalArgumentException.class)
        public Status test2() {
            testCaseMethodCalled2++;
            return Status.failed("Failed");
        }

        @TestCase
        public void test3() {
            testCaseMethodCalled3++;
        }
    }

    @Test
    public void threeMixedTestCases_ExceptionExpectedFromStatus_1() {
        ThreeMixedTestCases_ExceptionExpectedFromStatus_1.testCaseMethodCalled1 = 0;
        ThreeMixedTestCases_ExceptionExpectedFromStatus_1.testCaseMethodCalled2 = 0;
        ThreeMixedTestCases_ExceptionExpectedFromStatus_1.testCaseMethodCalled3 = 0;
        TestResult s = TU.runTestGroup(new ThreeMixedTestCases_ExceptionExpectedFromStatus_1(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals(1, ThreeMixedTestCases_ExceptionExpectedFromStatus_1.testCaseMethodCalled1);
        Assert.assertEquals(1, ThreeMixedTestCases_ExceptionExpectedFromStatus_1.testCaseMethodCalled2);
        Assert.assertEquals(1, ThreeMixedTestCases_ExceptionExpectedFromStatus_1.testCaseMethodCalled3);
        Assert.assertEquals("test cases: 3; passed: 2; failed: 1; failed: [test2]", s.getMessage());
    }

    @TestGroup
    public static class ThreeMixedTestCases_ExceptionExpectedFromStatus_2 {
        static int testCaseMethodCalled1;
        static int testCaseMethodCalled2;
        static int testCaseMethodCalled3;

        @TestCase
        public void test1() {
            testCaseMethodCalled1++;
        }

        @TestCase
        @ExpectedExceptions(IllegalArgumentException.class)
        public Status test2() {
            testCaseMethodCalled2++;
            return Status.passed("Failed");
        }

        @TestCase
        public void test3() {
            testCaseMethodCalled3++;
        }
    }

    @Test
    public void threeMixedTestCases_ExceptionExpectedFromStatus_2() {
        ThreeMixedTestCases_ExceptionExpectedFromStatus_2.testCaseMethodCalled1 = 0;
        ThreeMixedTestCases_ExceptionExpectedFromStatus_2.testCaseMethodCalled2 = 0;
        ThreeMixedTestCases_ExceptionExpectedFromStatus_2.testCaseMethodCalled3 = 0;
        TestResult s = TU.runTestGroup(new ThreeMixedTestCases_ExceptionExpectedFromStatus_2(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals(1, ThreeMixedTestCases_ExceptionExpectedFromStatus_2.testCaseMethodCalled1);
        Assert.assertEquals(1, ThreeMixedTestCases_ExceptionExpectedFromStatus_2.testCaseMethodCalled2);
        Assert.assertEquals(1, ThreeMixedTestCases_ExceptionExpectedFromStatus_2.testCaseMethodCalled3);
        Assert.assertEquals("test cases: 3; passed: 2; failed: 1; failed: [test2]", s.getMessage());
    }

    @TestGroup

    public static class ThreeMixedTestCases_ExceptionExpected_OtherReturnsStatus_3 {
        static int testCase_1_called;
        static int testCase_2_called;
        static int testCase_3_called;

        @TestCase
        @ExpectedExceptions(IllegalArgumentException.class)
        public void test1() {
            testCase_1_called++;
            throw new IllegalArgumentException();
        }

        @TestCase
        public Status test2() {
            testCase_2_called++;
            return Status.passed("Status OK");
        }

        @TestCase
        public void test3() {
            testCase_3_called++;
        }
    }

    @Test
    public void threeMixedTestCases_ExceptionExpectedFromStatus_3() {
        ThreeMixedTestCases_ExceptionExpected_OtherReturnsStatus_3.testCase_1_called = 0;
        ThreeMixedTestCases_ExceptionExpected_OtherReturnsStatus_3.testCase_2_called = 0;
        ThreeMixedTestCases_ExceptionExpected_OtherReturnsStatus_3.testCase_3_called = 0;
        TestResult s = TU.runTestGroup(new ThreeMixedTestCases_ExceptionExpected_OtherReturnsStatus_3(), TU.EMPTY_ARGV);
        Assert.assertTrue(s.isOK());
        Assert.assertEquals(1, ThreeMixedTestCases_ExceptionExpected_OtherReturnsStatus_3.testCase_1_called);
        Assert.assertEquals(1, ThreeMixedTestCases_ExceptionExpected_OtherReturnsStatus_3.testCase_2_called);
        Assert.assertEquals(1, ThreeMixedTestCases_ExceptionExpected_OtherReturnsStatus_3.testCase_3_called);
        Assert.assertEquals("test cases: 3; all passed", s.getMessage());
    }

    @Test
    public void threeMixedTestCases_ExceptionExpected_OtherReturns_Failed() {
        final AtomicBoolean testCase_1_called =  new AtomicBoolean();
        final AtomicBoolean testCase_2_called = new AtomicBoolean();
        final AtomicBoolean testCase_3_called = new AtomicBoolean();

        @TestGroup

        class TestGr {
            @TestCase
            @ExpectedExceptions(IllegalArgumentException.class)
            public void test1() {
                testCase_1_called.set(true);
                throw new IllegalArgumentException();
            }
            @TestCase public Status test2() {
                testCase_2_called.set(true);
                return Status.failed("Status FAILED");
            }
            @TestCase public void test3() {
                testCase_3_called.set(true);
            }
        }

        TestResult s = TU.runTestGroup(new TestGr(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertTrue(testCase_1_called.get());
        Assert.assertTrue(testCase_2_called.get());
        Assert.assertTrue(testCase_3_called.get());
        Assert.assertEquals("test cases: 3; passed: 2; failed: 1; failed: [test2]", s.getMessage());
    }

    @Test
    public void threeMixedTestCases_ExceptionExpected_OtherReturnsStatuse_Passed() {
        final AtomicBoolean testCase_1_called =  new AtomicBoolean();
        final AtomicBoolean testCase_2_called = new AtomicBoolean();
        final AtomicBoolean testCase_3_called = new AtomicBoolean();

        @TestGroup

        class TestGr {
            @TestCase
            @ExpectedExceptions(IllegalArgumentException.class)
            public void test1() {
                testCase_1_called.set(true);
                throw new IllegalArgumentException();
            }
            @TestCase public Status test2() {
                testCase_2_called.set(true);
                return Status.passed("Status passed");
            }
            @TestCase public Status test3() {
                testCase_3_called.set(true);
                return Status.passed("Status passed");
            }
        }

        TestResult s = TU.runTestGroup(new TestGr(), TU.EMPTY_ARGV);
        Assert.assertTrue(s.isOK());
        Assert.assertTrue(testCase_1_called.get());
        Assert.assertTrue(testCase_2_called.get());
        Assert.assertTrue(testCase_3_called.get());
        Assert.assertEquals("test cases: 3; all passed", s.getMessage());
    }


    @Test
    public void threeMixedTestCases_ExceptionExpected_OtherReturnsStatuse_Passed_selfRun() {
        final AtomicBoolean testCase_1_called =  new AtomicBoolean();
        final AtomicBoolean testCase_2_called = new AtomicBoolean();
        final AtomicBoolean testCase_3_called = new AtomicBoolean();

        @TestGroup
        class TestGr {
            @TestCase
            @ExpectedExceptions(IllegalArgumentException.class)
            public void test1() {
                testCase_1_called.set(true);
                throw new IllegalArgumentException();
            }
            @TestCase public Status test2() {
                testCase_2_called.set(true);
                return Status.passed("Status passed");
            }
            @TestCase public Status test3() {
                testCase_3_called.set(true);
                return Status.passed("Status passed");
            }
        }

        PrintWriter log = new PrintWriter(System.err, true);
        PrintWriter ref = new PrintWriter(System.out, true);
        TestResult s = TU.runTestGroup(new TestGr());
        Assert.assertTrue(s.isOK());
        Assert.assertTrue(testCase_1_called.get());
        Assert.assertTrue(testCase_2_called.get());
        Assert.assertTrue(testCase_3_called.get());
        Assert.assertEquals("test cases: 3; all passed", s.getMessage());
    }

    @Test
    public void threeMixedTestCases_ExceptionExpected_OtherReturnsStatuses_ExceptionNotThrown() {
        final AtomicBoolean testCase_1_called =  new AtomicBoolean();
        final AtomicBoolean testCase_2_called = new AtomicBoolean();
        final AtomicBoolean testCase_3_called = new AtomicBoolean();

        @TestGroup class TestGr {
            @TestCase
            @ExpectedExceptions(IllegalArgumentException.class)
            public void test1() {
                testCase_1_called.set(true);
            }
            @TestCase public Status test2() {
                testCase_2_called.set(true);
                return Status.passed("Status passed");
            }
            @TestCase public Status test3() {
                testCase_3_called.set(true);
                return Status.passed("Status passed");
            }
        }

        TestResult s = TU.runTestGroup(new TestGr(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertTrue(testCase_1_called.get());
        Assert.assertTrue(testCase_2_called.get());
        Assert.assertTrue(testCase_3_called.get());
        Assert.assertEquals("test cases: 3; passed: 2; failed: 1; failed: [test1]", s.getMessage());
    }

    @Test
    public void threeMixedTestCases_ForgotToAddStatusRecognizingProcessor() {
        final AtomicBoolean testCase_1_called =  new AtomicBoolean();
        final AtomicBoolean testCase_2_called = new AtomicBoolean();
        final AtomicBoolean testCase_3_called = new AtomicBoolean();

        @TestGroup
        class TestGr {
            @TestCase
            @ExpectedExceptions(IllegalArgumentException.class)
            public void test1() {
                testCase_1_called.set(true);
            }
            @TestCase public Status test2() {
                testCase_2_called.set(true);
                return Status.passed("Status passed");
            }
            @TestCase public Status test3() {
                testCase_3_called.set(true);
                return Status.passed("Status passed");
            }
        }

        TestResult testResult = TestRunner.run(new TestGr(), log, ref, TU.TG_PROC, TU.TC_PROC_STATUS_NOT_EXPECTED);
        Assert.assertFalse(testResult.isOK());
        Assert.assertTrue(testCase_1_called.get());
        Assert.assertTrue(testCase_2_called.get());
        Assert.assertTrue(testCase_3_called.get());
        Assert.assertEquals("test cases: 3; all failed", testResult.getMessage());
    }

    @Test
    public void threeMixedTestCases_ForgotToAddStatusRecognizingProcessor_01() {
        final AtomicBoolean testCase_1_called =  new AtomicBoolean();
        final AtomicBoolean testCase_2_called = new AtomicBoolean();
        final AtomicBoolean testCase_3_called = new AtomicBoolean();

        @TestGroup
        class TestGr {
            @TestCase
            @ExpectedExceptions(IllegalArgumentException.class)
            public void test1() {
                testCase_1_called.set(true);
                throw new IllegalArgumentException();
            }
            @TestCase public Status test2() {
                testCase_2_called.set(true);
                return Status.passed("Status passed");
            }
            @TestCase public void test3() {
                testCase_3_called.set(true);
            }
        }

        TestResult testResult = TestRunner.run(new TestGr(), log, ref, TU.TG_PROC, TU.TC_PROC_STATUS_NOT_EXPECTED);
        Assert.assertFalse(testResult.isOK());
        Assert.assertTrue(testCase_1_called.get());
        Assert.assertTrue(testCase_2_called.get());
        Assert.assertTrue(testCase_3_called.get());
        Assert.assertEquals("test cases: 3; passed: 2; failed: 1; failed: [test2]", testResult.getMessage());
    }

    @Test
    public void threeMixedTestCases_AUTD2StatusRecognizingProcessor_01() {
        final AtomicBoolean testCase_1_called =  new AtomicBoolean();
        final AtomicBoolean testCase_2_called = new AtomicBoolean();
        final AtomicBoolean testCase_3_called = new AtomicBoolean();

        @TestGroup
        class TestGr {
            @TestCase
            @ExpectedExceptions(IllegalArgumentException.class)
            public void test1() {
                testCase_1_called.set(true);
                throw new IllegalArgumentException();
            }
            @TestCase public Status test2() {
                testCase_2_called.set(true);
                return Status.passed("Status passed");
            }
            @TestCase public void test3() {
                testCase_3_called.set(true);
            }
        }

        TestResult s = TU.runTestGroup(new TestGr(), TU.EMPTY_ARGV);
        Assert.assertTrue(s.isOK());
        Assert.assertTrue(testCase_1_called.get());
        Assert.assertTrue(testCase_2_called.get());
        Assert.assertTrue(testCase_3_called.get());
        Assert.assertEquals("test cases: 3; all passed", s.getMessage());
    }

    @Test
    public void threeMixedTestCases_ExceptionExpected_OtherReturnsMixedStatuses_ExceptionNotThrown() {
        final AtomicBoolean testCase_1_called =  new AtomicBoolean();
        final AtomicBoolean testCase_2_called = new AtomicBoolean();
        final AtomicBoolean testCase_3_called = new AtomicBoolean();

        @TestGroup

        class TestGr {
            @TestCase
            @ExpectedExceptions(IllegalArgumentException.class)
            public void test1() {
                testCase_1_called.set(true);
            }
            @TestCase public Status test2() {
                testCase_2_called.set(true);
                return Status.passed("Status passed");
            }
            @TestCase public Status test3() {
                testCase_3_called.set(true);
                return Status.failed("Status FAILED");
            }
        }

        TestResult s = TU.runTestGroup(new TestGr(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertTrue(testCase_1_called.get());
        Assert.assertTrue(testCase_2_called.get());
        Assert.assertTrue(testCase_3_called.get());
        Assert.assertEquals("test cases: 3; passed: 1; failed: 2; failed: [test1, test3]", s.getMessage());
    }

    @Test
    public void ExceptionsThrownFromStatus() {
        final AtomicBoolean testCase_1_called =  new AtomicBoolean();
        final AtomicBoolean testCase_2_called = new AtomicBoolean();
        final AtomicBoolean testCase_3_called = new AtomicBoolean();

        @TestGroup

        class TestGr {
            @TestCase
            public void test1() {
                testCase_1_called.set(true);
            }
            @TestCase public Status test2() {
                testCase_2_called.set(true);
                throw new RuntimeException();
            }
            @TestCase public Status test3() {
                testCase_3_called.set(true);
                throw new RuntimeException();
            }
        }

        TestResult s = TU.runTestGroup(new TestGr(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertTrue(testCase_1_called.get());
        Assert.assertTrue(testCase_2_called.get());
        Assert.assertTrue(testCase_3_called.get());
        Assert.assertEquals("test cases: 3; passed: 1; failed: 2; failed: [test2, test3]", s.getMessage());
    }

    @Test
    public void ExceptionsThrownFromStatus_01() {
        final AtomicBoolean testCase_1_called =  new AtomicBoolean();
        final AtomicBoolean testCase_2_called = new AtomicBoolean();
        final AtomicBoolean testCase_3_called = new AtomicBoolean();

        @TestGroup
        class TestGr {
            @TestCase
            public void test1() {
                testCase_1_called.set(true);
            }
            @TestCase public Status test2() {
                testCase_2_called.set(true);
                throw new RuntimeException();
            }
            @TestCase public Status test3() {
                testCase_3_called.set(true);
                return Status.passed("Well. OK");
            }
        }

        TestResult s = TU.runTestGroup(new TestGr(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertTrue(testCase_1_called.get());
        Assert.assertTrue(testCase_2_called.get());
        Assert.assertTrue(testCase_3_called.get());
        Assert.assertEquals("test cases: 3; passed: 2; failed: 1; failed: [test2]", s.getMessage());

        testCase_1_called.set(false);
        testCase_2_called.set(false);
        testCase_3_called.set(false);
        PrintWriter log = new PrintWriter(System.err, true);
        PrintWriter ref = new PrintWriter(System.out, true);
        s = TU.runTestGroup(new TestGr());
        Assert.assertTrue(!s.isOK());
        Assert.assertTrue(testCase_1_called.get());
        Assert.assertTrue(testCase_2_called.get());
        Assert.assertTrue(testCase_3_called.get());
        Assert.assertEquals("test cases: 3; passed: 2; failed: 1; failed: [test2]", s.getMessage());
    }


    @TestGroup

    public static class ThreeMixedTestCases_Failed2VoidTests {
        static int testCaseMethodCalled1;
        static int testCaseMethodCalled2;
        static int testCaseMethodCalled3;

        @TestCase
        public void test1() {
            testCaseMethodCalled1++;
            throw new AssertionFailedException("Something baaad 1");
        }

        @TestCase
        public Status test2() {
            testCaseMethodCalled2++;
            return Status.passed("OK");
        }

        @TestCase
        public void test3() {
            testCaseMethodCalled3++;
            throw new AssertionFailedException("Something baaad 2");
        }
    }

    @Test
    public void threeMixedTestCases_Failed2VoidTests() {
        ThreeMixedTestCases_Failed2VoidTests.testCaseMethodCalled1 = 0;
        ThreeMixedTestCases_Failed2VoidTests.testCaseMethodCalled2 = 0;
        ThreeMixedTestCases_Failed2VoidTests.testCaseMethodCalled3 = 0;
        TestResult s = TU.runTestGroup(new ThreeMixedTestCases_Failed2VoidTests(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals(1, ThreeMixedTestCases_Failed2VoidTests.testCaseMethodCalled1);
        Assert.assertEquals(1, ThreeMixedTestCases_Failed2VoidTests.testCaseMethodCalled2);
        Assert.assertEquals(1, ThreeMixedTestCases_Failed2VoidTests.testCaseMethodCalled3);
        Assert.assertEquals("test cases: 3; passed: 1; failed: 2; failed: [test1, test3]", s.getMessage());
    }


    @TestGroup

    public static class TwoVoidTwoNonVoid {
        static int testCaseMethodCalled1;
        static int testCaseMethodCalled2;
        static int testCaseMethodCalled3;
        static int testCaseMethodCalled4;

        @TestCase
        public Status test1() {
            testCaseMethodCalled1++;
            return Status.failed("FAILED");
        }

        @TestCase
        public void test2() {
            testCaseMethodCalled2++;
        }

        @TestCase
        public void test3() {
            testCaseMethodCalled3++;
            throw new TestFailedException("Test failed");
        }

        @TestCase
        public Status test4() {
            testCaseMethodCalled4++;
            return Status.passed("OK");
        }
    }

    @TestGroup
    public static class FourVoidTestCases_wAssertion_forgotToRecognizeStatus {
        static int testCaseMethodCalled1;
        static int testCaseMethodCalled2;
        static int testCaseMethodCalled3;
        static int testCaseMethodCalled4;

        @TestCase
        @TestedStatement(
                value = {
                        "It needs to behave this way: ...",
                        "Constrains are: ..."},
                source = "doc1"
        )
        @TestedStatement(
                value = "Additional constraints are: ...",
                source = "doc2"
        )
        public Status test1() {
            testCaseMethodCalled1++;
            return Status.failed("FAILED");
        }

        @TestCase
        @TestedStatement(value = "23094857ldfg ;lsdjfg l;sdjf g;lsdj gsdf gsd", source = "a.c.b.d")
        public void test2() {
            testCaseMethodCalled2++;
        }

        @TestCase
        @TestedStatement(value = "23094857ldfg ;lsdjfg l;sdjf g;lsdj gsdf gsd", source = "f.g.d.d")
        public void test3() {
            testCaseMethodCalled3++;
            throw new TestFailedException("Test failed");
        }

        @TestCase
        @TestedStatement(value = "23094857ldfg ;lsdjfg l;sdjf g;lsdj gsdf gsd", source = "e.r.t.y")
        public Status test4() {
            testCaseMethodCalled4++;
            return Status.passed("OK");
        }
    }
    @TestGroup

    public static class FourVoidTestCases_wAssertion {
        static int testCaseMethodCalled1;
        static int testCaseMethodCalled2;
        static int testCaseMethodCalled3;
        static int testCaseMethodCalled4;

        @TestCase
        @TestedStatement(
                value = {
                        "It needs to behave this way: ...",
                        "Constrains are: ..."},
                source = "doc1"
        )
        @TestedStatement(
                value = "Additional constraints are: ...",
                source = "doc2"
        )
        public Status test1() {
            testCaseMethodCalled1++;
            return Status.failed("FAILED");
        }

        @TestCase
        @TestedStatement(value = "23094857ldfg ;lsdjfg l;sdjf g;lsdj gsdf gsd", source = "a.c.b.d")
        public void test2() {
            testCaseMethodCalled2++;
        }

        @TestCase
        @TestedStatement(value = "23094857ldfg ;lsdjfg l;sdjf g;lsdj gsdf gsd", source = "f.g.d.d")
        public void test3() {
            testCaseMethodCalled3++;
            throw new TestFailedException("Test failed");
        }

        @TestCase
        @TestedStatement(value = "23094857ldfg ;lsdjfg l;sdjf g;lsdj gsdf gsd", source = "e.r.t.y")
        public Status test4() {
            testCaseMethodCalled4++;
            return Status.passed("OK");
        }
    }

    @Test
    public void fourVoidTestCases() {
        TwoVoidTwoNonVoid.testCaseMethodCalled1 = 0;
        TwoVoidTwoNonVoid.testCaseMethodCalled2 = 0;
        TwoVoidTwoNonVoid.testCaseMethodCalled3 = 0;
        TwoVoidTwoNonVoid.testCaseMethodCalled4 = 0;
        TestResult s = TU.runTestGroup(new TwoVoidTwoNonVoid(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals(1, TwoVoidTwoNonVoid.testCaseMethodCalled1);
        Assert.assertEquals(1, TwoVoidTwoNonVoid.testCaseMethodCalled2);
        Assert.assertEquals(1, TwoVoidTwoNonVoid.testCaseMethodCalled3);
        Assert.assertEquals(1, TwoVoidTwoNonVoid.testCaseMethodCalled4);
        Assert.assertEquals("test cases: 4; passed: 2; failed: 2; failed: [test1, test3]", s.getMessage());
    }

    /**
     * Testcases returning status are not processed by default and using TestRunner.
     */
    @Test
    public void fourVoidTestCases_wAssertion_NoStatusRecognition() {
        FourVoidTestCases_wAssertion_forgotToRecognizeStatus.testCaseMethodCalled1 = 0;
        FourVoidTestCases_wAssertion_forgotToRecognizeStatus.testCaseMethodCalled2 = 0;
        FourVoidTestCases_wAssertion_forgotToRecognizeStatus.testCaseMethodCalled3 = 0;
        FourVoidTestCases_wAssertion_forgotToRecognizeStatus.testCaseMethodCalled4 = 0;
        TestResult testResult = TestRunner.run(
                new FourVoidTestCases_wAssertion_forgotToRecognizeStatus(), log, ref, TU.TG_PROC, TU.TC_PROC_STATUS_NOT_EXPECTED);
        Assert.assertFalse(testResult.isOK());
        Assert.assertEquals(1, FourVoidTestCases_wAssertion_forgotToRecognizeStatus.testCaseMethodCalled1);
        Assert.assertEquals(1, FourVoidTestCases_wAssertion_forgotToRecognizeStatus.testCaseMethodCalled2);
        Assert.assertEquals(1, FourVoidTestCases_wAssertion_forgotToRecognizeStatus.testCaseMethodCalled3);
        Assert.assertEquals(1, FourVoidTestCases_wAssertion_forgotToRecognizeStatus.testCaseMethodCalled4);
        Assert.assertEquals("test cases: 4; passed: 1; failed: 3; failed: [test1, test3, test4]", testResult.getMessage());
    }

    @Test
    public void fourVoidTestCases_wAssertion() {
        FourVoidTestCases_wAssertion.testCaseMethodCalled1 = 0;
        FourVoidTestCases_wAssertion.testCaseMethodCalled2 = 0;
        FourVoidTestCases_wAssertion.testCaseMethodCalled3 = 0;
        FourVoidTestCases_wAssertion.testCaseMethodCalled4 = 0;
        TestResult s = TU.runTestGroup(new FourVoidTestCases_wAssertion(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals(1, FourVoidTestCases_wAssertion.testCaseMethodCalled1);
        Assert.assertEquals(1, FourVoidTestCases_wAssertion.testCaseMethodCalled2);
        Assert.assertEquals(1, FourVoidTestCases_wAssertion.testCaseMethodCalled3);
        Assert.assertEquals(1, FourVoidTestCases_wAssertion.testCaseMethodCalled4);
        Assert.assertEquals("test cases: 4; passed: 2; failed: 2; failed: [test1, test3]", s.getMessage());
    }


    @TestGroup
    public static class OneStatusErrorTestCase {
        static int testCaseMethodCalled;

        @TestCase
        public Status test43563564356() {
            testCaseMethodCalled++;
            return Status.failed("error");
        }
    }

    @Test
    public void oneTestCase_statusError() {
        OneStatusErrorTestCase.testCaseMethodCalled = 0;
        TestResult s = TU.runTestGroup(new OneStatusErrorTestCase(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals("test cases: 1; all failed", s.getMessage());
        Assert.assertEquals(1, OneStatusErrorTestCase.testCaseMethodCalled);
    }

    @TestGroup
    public static class OneTestCaseThrownException1 {
        static int testCaseMethodCalled;

        @TestCase
        public void test32423423() {
            testCaseMethodCalled++;
            throw new IllegalArgumentException("hey!");
        }
    }

    @Test
    public void oneTestCaseThrownException1() {
        OneTestCaseThrownException1.testCaseMethodCalled = 0;
        TestResult s = TU.runTestGroup(new OneTestCaseThrownException1(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals("test cases: 1; all failed", s.getMessage());
        Assert.assertEquals(1, OneTestCaseThrownException1.testCaseMethodCalled);
    }

    @TestGroup
    public static class OneTestCaseThrownException2 {
        static int testCaseMethodCalled;

        @TestCase
        public void test32423423() throws Throwable {
            testCaseMethodCalled++;
            throw new Throwable("hey!!!!");
        }
    }

    @TestGroup
    public static class OneTestCaseThrownException3_wAssertion {
        static int testCaseMethodCalled;

        @TestCase
        @TestedStatement(value = "Could though or could not", source = "unknown")
        public void test32423423() throws Throwable {
            testCaseMethodCalled++;
            throw new Throwable("hey!!!!");
        }
    }

    @Test
    public void oneTestCaseThrownException2() {
        OneTestCaseThrownException2.testCaseMethodCalled = 0;
        TestResult s = TU.runTestGroup(new OneTestCaseThrownException2(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals("test cases: 1; all failed", s.getMessage());
        Assert.assertEquals(1, OneTestCaseThrownException2.testCaseMethodCalled);
    }

    @Test
    public void oneTestCaseThrownException3_wAssertion() {
        OneTestCaseThrownException3_wAssertion.testCaseMethodCalled = 0;
        TestResult s = TU.runTestGroup(new OneTestCaseThrownException3_wAssertion(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals("test cases: 1; all failed", s.getMessage());
        Assert.assertEquals(1, OneTestCaseThrownException3_wAssertion.testCaseMethodCalled);
    }


}
