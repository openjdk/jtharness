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
package com.oracle.tck.lib.autd2.unittests.tgfported.beforeafter;

import com.oracle.tck.lib.autd2.*;
import com.oracle.tck.lib.autd2.unittests.BaseTestGroup;
import com.oracle.tck.lib.autd2.unittests.TU;
import com.sun.tck.lib.tgf.*;
import com.sun.tck.test.TestCase;
import com.sun.tck.test.TestGroup;

import org.junit.Assert;
import org.junit.Test;

import javax.swing.*;
import java.io.PrintWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import org.mockito.InOrder;




public class TestBefore {

    @Test
    public void beforeWasCalled() {

        final boolean[] beforeCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};
        final int[] beforeCounter = new int[]{0};

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new Mytest() {
            private void myBefore() {
                org.junit.Assert.assertFalse(methodCalled[0]);
                beforeCalled[0] = true;
                beforeCounter[0]++;
            }

            @TestCase
            @TestData("setup")
            @com.sun.tck.lib.tgf.Before("myBefore")
            public void test(int i) {
                org.junit.Assert.assertTrue(beforeCalled[0]);
                methodCalled[0] = true;
                counter[0]++;
            }
        }, TU.EMPTY_ARGV);
        assertTrue(status.isOK());
        assertTrue(beforeCalled[0]);
        assertTrue(methodCalled[0]);
        assertEquals(3, counter[0]);
        assertEquals(1, beforeCounter[0]);
    }

    @Test
    public void beforeIsBeforeValuesInitialization() {
        final boolean[] beforeCalled = new boolean[1];
        final boolean[] valuesInited = new boolean[1];
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            Object[] objects() {
                valuesInited[0] = true;
                return new Object[]{new Object()};
            }

            private void before() {
                Assert.assertFalse(valuesInited[0]);
                beforeCalled[0] = true;
            }

            @TestCase
            @TestData("objects")
            @Before("before")
            public void test(Object o) {
                Assert.assertTrue(beforeCalled[0]);
            }
        }, TU.EMPTY_ARGV);
        assertTrue(status.isOK());
        assertTrue(beforeCalled[0]);
        assertTrue(valuesInited[0]);
    }


    @Test
    public void beforeWasCalled_NoParams() {

        final boolean[] beforeCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};
        final int[] beforeCounter = new int[]{0};

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new Mytest() {
            private void myBefore() {
                org.junit.Assert.assertFalse(methodCalled[0]);
                beforeCalled[0] = true;
                beforeCounter[0]++;
            }

            @TestCase
            @com.sun.tck.lib.tgf.Before("myBefore")
            public void test() {
                org.junit.Assert.assertTrue(beforeCalled[0]);
                methodCalled[0] = true;
                counter[0]++;
            }
        }, TU.EMPTY_ARGV);
        assertTrue(status.isOK());
        assertTrue(beforeCalled[0]);
        assertTrue(methodCalled[0]);
        assertEquals(1, counter[0]);
        assertEquals(1, beforeCounter[0]);
    }


    @Test
    public void beforeOfNonVoidTypeWasCalled() {
        final boolean[] beforeCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};
        final int[] beforeCounter = new int[]{0};

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new Mytest() {
            private JOptionPane myBefore() {
                org.junit.Assert.assertFalse(methodCalled[0]);
                beforeCalled[0] = true;
                beforeCounter[0]++;
                return new JOptionPane();
            }

            @TestCase
            @TestData("setup")
            @com.sun.tck.lib.tgf.Before("myBefore")
            public void test(int i) {
                org.junit.Assert.assertTrue(beforeCalled[0]);
                methodCalled[0] = true;
                counter[0]++;
            }
        }, TU.EMPTY_ARGV);
        assertTrue(status.isOK());
        assertTrue(beforeCalled[0]);
        assertTrue(methodCalled[0]);
        assertEquals(3, counter[0]);
        assertEquals(1, beforeCounter[0]);
        Assert.assertEquals("Passed. test cases: 1; all passed", status.toString());
    }

    @Test
    public void beforeNotFound() {

        PrintWriter log = TU.createMockedPrintWriter();
        PrintWriter ref = TU.createMockedPrintWriter();


        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};
        TestResult status = TU.runTestGroup(new Mytest() {
                                               @TestCase
                                               @TestData("setup")
                                               @com.sun.tck.lib.tgf.Before("myBeforeThatDoesNotExist")
                                               public void test(int i) {
                                                   methodCalled[0] = true;
                                                   counter[0]++;
                                               }
                                           }
                , log, ref, TU.EMPTY_ARGV
        );
        assertFalse(status.isOK());
        assertFalse(methodCalled[0]);
        assertEquals(0, counter[0]);


        Assert.assertEquals("Failed. test cases: 1; all failed",
                status.toString());

        InOrder inOrder = inOrder(log, ref);
        inOrder.verify(log).println("Method \"myBeforeThatDoesNotExist\" doesn't exist.");
        inOrder.verify(log).println("test: Failed. Failed trying to invoke @Before method \"myBeforeThatDoesNotExist\"");
        inOrder.verify(log).flush();
        inOrder.verify(ref).flush();

        verifyNoMoreInteractions(log, ref);

    }

    @Test
    public void beforeNotFound_NoParams() {

        PrintWriter log = TU.createMockedPrintWriter();
        PrintWriter ref = TU.createMockedPrintWriter();

        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};

        TestResult status = TU.runTestGroup(new Mytest() {
                                               @TestCase
                                               @com.sun.tck.lib.tgf.Before("myBeforeThatDoesNotExist")
                                               public void testWithNoParams() {
                                                   methodCalled[0] = true;
                                                   counter[0]++;
                                               }
                                           }
                , log, ref, TU.EMPTY_ARGV
        );

        assertFalse(status.isOK());
        assertFalse(methodCalled[0]);
        assertEquals(0, counter[0]);

        Assert.assertEquals("Failed. test cases: 1; all failed",
                status.toString());


        InOrder inOrder = inOrder(log, ref);
        inOrder.verify(log).println("Method \"myBeforeThatDoesNotExist\" doesn't exist.");
        inOrder.verify(log).println("testWithNoParams: Failed. Failed trying to invoke @Before method \"myBeforeThatDoesNotExist\"");
        inOrder.verify(log).flush();
        inOrder.verify(ref).flush();

        verifyNoMoreInteractions(log, ref);

    }

    @Test
    public void beforeThrowsException() {

        PrintWriter log = TU.createMockedPrintWriter();
        PrintWriter ref = TU.createMockedPrintWriter();

        final boolean[] beforeCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};

        TestResult status = TU.runTestGroup(new Mytest() {

                                               private void myBefore() {
                                                   org.junit.Assert.assertFalse(methodCalled[0]);
                                                   beforeCalled[0] = true;
                                                   throw new ExceptionThrownFromBefore();
                                               }

                                               @TestCase
                                               @TestData("setup")
                                               @com.sun.tck.lib.tgf.Before("myBefore")
                                               public void test(int i) {
                                                   // this won't be called
                                                   methodCalled[0] = true;
                                                   counter[0]++;
                                               }
                                           }
                , log, ref, TU.EMPTY_ARGV
        );
        assertFalse(status.isOK());
        assertTrue(beforeCalled[0]);
        assertFalse(methodCalled[0]);
        assertEquals(0, counter[0]);


        Assert.assertEquals("Failed. test cases: 1; all failed",
                status.toString());

        InOrder inOrder = inOrder(log, ref);
        inOrder.verify(log).println("hahaha, it's mocked printStackTrace method");
        inOrder.verify(log).println("Method \"myBefore\" has thrown an exception com.oracle.tck.lib.autd2.unittests.tgfported.beforeafter.TestBefore$ExceptionThrownFromBefore: Before throws this exception");
        inOrder.verify(log).println("test: Failed. Failed trying to invoke @Before method \"myBefore\"");
        inOrder.verify(log).flush();
        inOrder.verify(ref).flush();
        verifyNoMoreInteractions(log, ref);
    }

    @Test
    public void beforeThrowsException_nullOutPut() {

        final boolean[] beforeCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};

        TestResult status = TU.runTestGroup(new Mytest() {

            private void myBefore() {
                org.junit.Assert.assertFalse(methodCalled[0]);
                beforeCalled[0] = true;
                throw new ExceptionThrownFromBefore();
            }

            @TestCase
            @TestData("setup")
            @com.sun.tck.lib.tgf.Before("myBefore")
            public void test(int i) {
                // this won't be called
                methodCalled[0] = true;
                counter[0]++;
            }
        }, null, null, TU.EMPTY_ARGV);
        assertFalse(status.isOK());
        assertTrue(beforeCalled[0]);
        assertFalse(methodCalled[0]);
        assertEquals(0, counter[0]);

        Assert.assertEquals("Failed. test cases: 1; all failed",
                status.toString());
    }

    @Test
    public void beforeThrowsException_NoParams() {

        PrintWriter log = TU.createMockedPrintWriter();
        PrintWriter ref = TU.createMockedPrintWriter();

        final boolean[] beforeCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};

        TestResult status = TU.runTestGroup(new Mytest() {

                                               private void myBefore() {
                                                   org.junit.Assert.assertFalse(methodCalled[0]);
                                                   beforeCalled[0] = true;
                                                   throw new ExceptionThrownFromBefore();
                                               }

                                               @TestCase
                                               @com.sun.tck.lib.tgf.Before("myBefore")
                                               public void myTestWithNoParams() {
                                                   // this won't be called
                                                   methodCalled[0] = true;
                                                   counter[0]++;
                                               }
                                           }
                , log, ref, TU.EMPTY_ARGV
        );
        assertFalse(status.isOK());
        assertTrue(beforeCalled[0]);
        assertFalse(methodCalled[0]);
        assertEquals(0, counter[0]);

        Assert.assertEquals("Failed. test cases: 1; all failed",
                status.toString());

        InOrder inOrder = inOrder(log, ref);
        inOrder.verify(log).println("hahaha, it's mocked printStackTrace method");
//        log.println("Failed trying to invoke @Before method \"myBefore\"");
        inOrder.verify(log).println("Method \"myBefore\" has thrown an exception com.oracle.tck.lib.autd2.unittests.tgfported.beforeafter.TestBefore$ExceptionThrownFromBefore: Before throws this exception");
        inOrder.verify(log).println("myTestWithNoParams: Failed. Failed trying to invoke @Before method \"myBefore\"");
        inOrder.verify(log).flush();
        inOrder.verify(ref).flush();
        verifyNoMoreInteractions(log, ref);

    }

    @Test
    public void beforeCalledOnce() {

        final boolean[] beforeCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};
        final int[] beforeCounter = new int[]{0};

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new Mytest() {
            private void myBefore() {
                org.junit.Assert.assertFalse(methodCalled[0]);
                // to be sure that it was called only once
                org.junit.Assert.assertFalse(beforeCalled[0]);
                beforeCalled[0] = true;
                beforeCounter[0]++;
            }

            @TestCase
            @TestData("setup")
            @com.sun.tck.lib.tgf.Before("myBefore")
            public void test(int i) {
                org.junit.Assert.assertTrue(beforeCalled[0]);
                methodCalled[0] = true;
                counter[0]++;
            }
        }, TU.EMPTY_ARGV);
        assertTrue(status.isOK());
        assertTrue(beforeCalled[0]);
        assertTrue(methodCalled[0]);
        assertEquals(3, counter[0]);
        assertEquals(1, beforeCounter[0]);
        Assert.assertEquals("Passed. test cases: 1; all passed",
                status.toString());
    }

    @Test
    public void beforeCalledOnce_Mixed() {

        final boolean[] beforeCalled = new boolean[1];
        final boolean[] testCalled = new boolean[1];
        final boolean[] anotherTestCalled = new boolean[1];
        final int[] testCounter = new int[]{0};
        final int[] anotherTestCounter = new int[]{0};
        final int[] beforeCounter = new int[]{0};

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new Mytest() {
            private void myBefore() {
                org.junit.Assert.assertFalse(testCalled[0]);

                if (!anotherTestCalled[0]) {
                    org.junit.Assert.assertFalse(beforeCalled[0]);
                }
                beforeCalled[0] = true;
                beforeCounter[0]++;
            }

            @TestCase
            @TestData("setup")
            @com.sun.tck.lib.tgf.Before("myBefore")
            public void test(int i) {
                org.junit.Assert.assertTrue(beforeCalled[0]);
                testCalled[0] = true;
                testCounter[0]++;
            }

            @TestCase
            @com.sun.tck.lib.tgf.Before("myBefore")
            public void anotherTest() {
                org.junit.Assert.assertTrue(beforeCalled[0]);
                anotherTestCalled[0] = true;
                anotherTestCounter[0]++;
            }
        }, TU.EMPTY_ARGV);
        assertTrue(status.isOK());
        assertTrue(beforeCalled[0]);
        assertTrue(testCalled[0]);
        assertTrue(anotherTestCalled[0]);
        assertEquals(3, testCounter[0]);
        assertEquals(1, anotherTestCounter[0]);
        assertEquals(2, beforeCounter[0]);
        Assert.assertEquals("Passed. test cases: 2; all passed",
                status.toString());
    }

    @Test
    public void beforeCalledOnce_NoParams() {

        final boolean[] beforeCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};
        final int[] beforeCounter = new int[]{0};

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new Mytest() {

            private void myBefore() {
                org.junit.Assert.assertFalse(methodCalled[0]);
                // to be sure that it was called only once
                org.junit.Assert.assertFalse(beforeCalled[0]);
                beforeCalled[0] = true;
                beforeCounter[0]++;
            }

            @TestCase
            @com.sun.tck.lib.tgf.Before("myBefore")
            public void testCaseWithNoArgs() {
                org.junit.Assert.assertTrue(beforeCalled[0]);
                methodCalled[0] = true;
                counter[0]++;
            }
        }, TU.EMPTY_ARGV);
        assertTrue(status.isOK());
        assertTrue(beforeCalled[0]);
        assertTrue(methodCalled[0]);
        assertEquals(1, counter[0]);
        assertEquals(1, beforeCounter[0]);
        Assert.assertEquals("Passed. test cases: 1; all passed",
                status.toString());
    }

    @Test
    public void overridingBefore() {

        final boolean[] newBeforeCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};
        final int[] beforeCounter = new int[]{0};

        PredefinedBefore testInstance = new PredefinedBefore() {
            protected void before() {
                org.junit.Assert.assertFalse(methodCalled[0]);
                newBeforeCalled[0] = true;
                beforeCounter[0]++;
            }

            @TestCase
            @TestData("setup")
            @com.sun.tck.lib.tgf.Before("before")
            public void test(int i) {
                org.junit.Assert.assertTrue(newBeforeCalled[0]);
                methodCalled[0] = true;
                counter[0]++;
            }
        };
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(testInstance, TU.EMPTY_ARGV);
        assertTrue(status.isOK());
        assertFalse(testInstance.initialBeforeWasCalled);
        assertTrue(newBeforeCalled[0]);
        assertTrue(methodCalled[0]);
        assertEquals(3, counter[0]);
        assertEquals(1, beforeCounter[0]);
        Assert.assertEquals("Passed. test cases: 1; all passed",
                status.toString());
    }

    @Test
    public void overridingBefore_NoParams() {

        final boolean[] newBeforeCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};
        final int[] beforeCounter = new int[]{0};

        PredefinedBefore testInstance = new PredefinedBefore() {

            protected void before() {
                org.junit.Assert.assertFalse(methodCalled[0]);
                newBeforeCalled[0] = true;
                beforeCounter[0]++;
            }

            @TestCase
            @com.sun.tck.lib.tgf.Before("before")
            public void test() {
                org.junit.Assert.assertTrue(newBeforeCalled[0]);
                methodCalled[0] = true;
                counter[0]++;
            }
        };
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(testInstance, TU.EMPTY_ARGV);
        assertTrue(status.isOK());
        assertFalse(testInstance.initialBeforeWasCalled);
        assertTrue(newBeforeCalled[0]);
        assertTrue(methodCalled[0]);
        assertEquals(1, counter[0]);
        assertEquals(1, beforeCounter[0]);
        Assert.assertEquals("Passed. test cases: 1; all passed",
                status.toString());
    }

    @Test
    public void inheritingBefore() {

        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};

        PredefinedBefore testInstance = new PredefinedBefore() {

            @TestCase
            @TestData("setup")
            @com.sun.tck.lib.tgf.Before("before")
            public void test(int i) {
                org.junit.Assert.assertTrue(initialBeforeWasCalled);
                methodCalled[0] = true;
                counter[0]++;
            }
        };
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(testInstance, TU.EMPTY_ARGV);
        assertTrue(status.isOK());
        assertTrue(testInstance.initialBeforeWasCalled);
        assertTrue(methodCalled[0]);
        assertEquals(3, counter[0]);
        Assert.assertEquals("Passed. test cases: 1; all passed",
                status.toString());
    }

    @Test
    public void invalidSignature() {
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};

        PredefinedBefore testInstance = new PredefinedBefore() {

            @TestCase
            @TestData("setup")
            @com.sun.tck.lib.tgf.Before("beforeThatDoesntexist")
            public void test(int i) {
                methodCalled[0] = true;
                counter[0]++;
            }
        };
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(testInstance, TU.EMPTY_ARGV);
        assertFalse(status.isOK());
        assertFalse(testInstance.initialBeforeWasCalled);
        assertFalse(methodCalled[0]);
        assertEquals(0, counter[0]);
        Assert.assertEquals("Failed. test cases: 1; all failed",
                status.toString());
    }

    @Test
    public void invalidSignature_NoParams() {
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};

        PredefinedBefore testInstance = new PredefinedBefore() {

            @TestCase
            @com.sun.tck.lib.tgf.Before("beforeThatDoesntexist")
            public void testWithNoParams() {
                methodCalled[0] = true;
                counter[0]++;
            }
        };
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(testInstance, TU.EMPTY_ARGV);
        assertFalse(status.isOK());
        assertFalse(testInstance.initialBeforeWasCalled);
        assertFalse(methodCalled[0]);
        assertEquals(0, counter[0]);
        Assert.assertEquals("Failed. test cases: 1; all failed",
                status.toString());
    }

    @Test
    public void invalidSignature_Mixed() {
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};

        PredefinedBefore testInstance = new PredefinedBefore() {

            @TestCase
            @TestData("setup")
            @com.sun.tck.lib.tgf.Before("beforeThatDoesntexist")
            public void test_2(int i) {
                methodCalled[0] = true;
                counter[0]++;
            }

            @TestCase
            @com.sun.tck.lib.tgf.Before("beforeThatDoesntexist")
            public void test_1() {
                methodCalled[0] = true;
                counter[0]++;
            }
        };
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(testInstance, TU.EMPTY_ARGV);
        assertFalse(status.isOK());
        assertFalse(testInstance.initialBeforeWasCalled);
        assertFalse(methodCalled[0]);
        assertEquals(0, counter[0]);
        Assert.assertEquals("Failed. test cases: 2; all failed",
                status.toString());
    }

    private static class ExceptionThrownFromBefore extends RuntimeException {
        private ExceptionThrownFromBefore() {
            super("Before throws this exception");
            // overriden to be shure that is' called by Runner
        }

        public void printStackTrace(PrintWriter s) {
            s.println("hahaha, it's mocked printStackTrace method");
        }
    }

    @TestGroup
    public abstract static class Mytest {
        protected Values setup() {
            return DataFactory.createColumn(1, 2, 3);
        }
    }

    @TestGroup
    public abstract static class PredefinedBefore extends Mytest {
        boolean initialBeforeWasCalled;

        protected void before() {
            initialBeforeWasCalled = true;
        }

    }


}
