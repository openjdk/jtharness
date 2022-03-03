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
import com.oracle.tck.lib.autd2.unittests.TU;
import com.sun.tck.lib.tgf.DataFactory;
import com.sun.tck.lib.tgf.TestData;
import com.sun.tck.lib.tgf.Values;
import com.sun.tck.test.TestCase;
import com.sun.tck.test.TestGroup;

import org.junit.Assert;
import org.junit.Test;

import javax.swing.*;
import java.io.PrintWriter;

import static com.oracle.tck.lib.autd2.unittests.tgfported.beforeafter.TestAfter.ExceptionThrownFromAfter.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import org.mockito.InOrder;





public class TestAfter {







    @Test
    public void afterWasCalled() {

        final boolean[] afterCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};
        final int[] afterCounter = new int[]{0};

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new TestAfter.Mytest() {

            private void myAfter() {
                org.junit.Assert.assertTrue(methodCalled[0]);
                afterCalled[0] = true;
                afterCounter[0]++;
            }

            @TestCase // this is optional
            @TestData("setup")
            @com.sun.tck.lib.tgf.After("myAfter")
            public void test(int i) {
                org.junit.Assert.assertFalse(afterCalled[0]);
                methodCalled[0] = true;
                counter[0]++;
            }
        }, TU.EMPTY_ARGV);
        assertTrue(status.isOK());
        assertTrue(afterCalled[0]);
        assertTrue(methodCalled[0]);
        assertEquals(3, counter[0]);
        assertEquals(1, afterCounter[0]);
        Assert.assertEquals(
                "Passed. test cases: 1; all passed",
                status.toString());
    }

    @Test
    public void afterWasCalled_NoParams() {

        final boolean[] afterCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};
        final int[] afterCounter = new int[]{0};

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new TestAfter.Mytest() {

            private void myAfter() {
                org.junit.Assert.assertTrue(methodCalled[0]);
                afterCalled[0] = true;
                afterCounter[0]++;
            }

            @TestCase
            @com.sun.tck.lib.tgf.After("myAfter")
            public void test() {
                org.junit.Assert.assertFalse(afterCalled[0]);
                methodCalled[0] = true;
                counter[0]++;
            }
        }, TU.EMPTY_ARGV);
        assertTrue(status.isOK());
        assertTrue(afterCalled[0]);
        assertTrue(methodCalled[0]);
        assertEquals(1, counter[0]);
        assertEquals(1, afterCounter[0]);
        Assert.assertEquals(
                "Passed. test cases: 1; all passed",
                status.toString());
    }

    @Test
    public void afterOfNonVoidTypeWasCalled() {

        final boolean[] afterCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};
        final int[] afterCounter = new int[]{0};

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new TestAfter.Mytest() {

            private JWindow myAfter() {
                org.junit.Assert.assertTrue(methodCalled[0]);
                afterCalled[0] = true;
                afterCounter[0]++;
                return new JWindow();
            }

            @TestCase
            @TestData("setup")
            @com.sun.tck.lib.tgf.After("myAfter")
            public void test(int i) {
                org.junit.Assert.assertFalse(afterCalled[0]);
                methodCalled[0] = true;
                counter[0]++;
            }
        }, TU.EMPTY_ARGV);
        assertTrue(status.isOK());
        assertTrue(afterCalled[0]);
        assertTrue(methodCalled[0]);
        assertEquals(3, counter[0]);
        assertEquals(1, afterCounter[0]);
    }

    @Test
    public void afterOfNonVoidTypeWasCalled_NoParams() {

        final boolean[] afterCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};
        final int[] afterCounter = new int[]{0};

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new TestAfter.Mytest() {

            private JWindow myAfter() {
                org.junit.Assert.assertTrue(methodCalled[0]);
                afterCalled[0] = true;
                afterCounter[0]++;
                return new JWindow();
            }

            @TestCase
            @com.sun.tck.lib.tgf.After("myAfter")
            public void test() {
                org.junit.Assert.assertFalse(afterCalled[0]);
                methodCalled[0] = true;
                counter[0]++;
            }
        }, TU.EMPTY_ARGV);
        assertTrue(status.isOK());
        assertTrue(afterCalled[0]);
        assertTrue(methodCalled[0]);
        assertEquals(1, counter[0]);
        assertEquals(1, afterCounter[0]);
    }

    @Test
    public void afterThrowsException() {

        PrintWriter log = TU.createMockedPrintWriter();
        PrintWriter ref = TU.createMockedPrintWriter();


        final boolean[] afterCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};
        final int[] afterCounter = new int[]{0};


        TestResult status = TU.runTestGroup(new TestAfter.Mytest() {

                                               private void myAfter() {
                                                   org.junit.Assert.assertTrue(methodCalled[0]);
                                                   afterCalled[0] = true;
                                                   afterCounter[0]++;
                                                   throw new ExceptionThrownFromAfter();
                                               }

                                               @TestCase
                                               @TestData("setup")
                                               @com.sun.tck.lib.tgf.After("myAfter")
                                               public void test(int i) {
                                                   org.junit.Assert.assertFalse(afterCalled[0]);
                                                   methodCalled[0] = true;
                                                   counter[0]++;
                                               }
                                           }
                , log, ref, TU.EMPTY_ARGV
        );
        assertTrue(!status.isOK());
        assertTrue(afterCalled[0]);
        assertTrue(methodCalled[0]);
        assertEquals(3, counter[0]);
        assertEquals(1, afterCounter[0]);

        InOrder inOrder = inOrder(log, ref);
        inOrder.verify(log).println("Haha - this is overriden printStackTrace " + RANDOM);
        inOrder.verify(log).println("Method \"myAfter\" has thrown an exception com.oracle.tck.lib.autd2.unittests.tgfported.beforeafter.TestAfter$ExceptionThrownFromAfter: This exception is thrown from After " +
                RANDOM);
        inOrder.verify(log).println("test: Failed. Failed trying to invoke @After method \"myAfter\"");
        inOrder.verify(log).flush();
        inOrder.verify(ref).flush();
        verifyNoMoreInteractions(log, ref);
    }

    @Test
    public void afterThrowsException_nullOutput() {

        final boolean[] afterCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};
        final int[] afterCounter = new int[]{0};


        TestResult status = TU.runTestGroup(new TestAfter.Mytest() {

            private void myAfter() {
                org.junit.Assert.assertTrue(methodCalled[0]);
                afterCalled[0] = true;
                afterCounter[0]++;
                throw new ExceptionThrownFromAfter();
            }

            @TestCase
            @TestData("setup")
            @com.sun.tck.lib.tgf.After("myAfter")
            public void test(int i) {
                org.junit.Assert.assertFalse(afterCalled[0]);
                methodCalled[0] = true;
                counter[0]++;
            }
        }, null, null, TU.EMPTY_ARGV);
        assertTrue(!status.isOK());
        assertTrue(afterCalled[0]);
        assertTrue(methodCalled[0]);
        assertEquals(3, counter[0]);
        assertEquals(1, afterCounter[0]);
    }

    @Test
    public void afterThrowsException_NoParams() {

        PrintWriter log = TU.createMockedPrintWriter();
        PrintWriter ref = TU.createMockedPrintWriter();

        final boolean[] afterCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};
        final int[] afterCounter = new int[]{0};

        TestResult status = TU.runTestGroup(new TestAfter.Mytest() {

                                               private void myAfter() {
                                                   org.junit.Assert.assertTrue(methodCalled[0]);
                                                   afterCalled[0] = true;
                                                   afterCounter[0]++;
                                                   throw new ExceptionThrownFromAfter();
                                               }

                                               @TestCase
                                               @com.sun.tck.lib.tgf.After("myAfter")
                                               public void test() {
                                                   org.junit.Assert.assertFalse(afterCalled[0]);
                                                   methodCalled[0] = true;
                                                   counter[0]++;
                                               }
                                           }
                , log, ref, TU.EMPTY_ARGV
        );
        assertTrue(!status.isOK());
        assertTrue(afterCalled[0]);
        assertTrue(methodCalled[0]);
        assertEquals(1, counter[0]);
        assertEquals(1, afterCounter[0]);


        InOrder inOrder = inOrder(log, ref);
        inOrder.verify(log).println("Haha - this is overriden printStackTrace " + RANDOM);
        inOrder.verify(log).println("Method \"myAfter\" has thrown an exception com.oracle.tck.lib.autd2.unittests.tgfported.beforeafter.TestAfter$ExceptionThrownFromAfter: This exception is thrown from After " +
                RANDOM);
        inOrder.verify(log).println("test: Failed. Failed trying to invoke @After method \"myAfter\"");
        inOrder.verify(log).flush();
        inOrder.verify(ref).flush();
        verifyNoMoreInteractions(log, ref);

    }

    @Test
    public void afterCalledOnce() {
        final boolean[] afterCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};
        final int[] afterCounter = new int[]{0};

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new TestAfter.Mytest() {
            protected Values mySetup() {
                return DataFactory.createColumn(1, 2, 3);
            }

            private void myAfter() {
                org.junit.Assert.assertTrue(methodCalled[0]);
                // to be sure that it was called only once
                org.junit.Assert.assertFalse(afterCalled[0]);
                afterCalled[0] = true;
                afterCounter[0]++;
            }

            @TestCase
            @TestData("mySetup")
            @com.sun.tck.lib.tgf.After("myAfter")
            public void test(int i) {
                org.junit.Assert.assertFalse(afterCalled[0]);
                methodCalled[0] = true;
                counter[0]++;
            }
        }, TU.EMPTY_ARGV);
        assertTrue(status.isOK());
        assertTrue(afterCalled[0]);
        assertTrue(methodCalled[0]);
        assertEquals(3, counter[0]);
        assertEquals(1, afterCounter[0]);
    }

    @Test
    public void inheritingAfter() {
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};

        PredefinedAfter testInstance = new PredefinedAfter() {
            protected Values mySetup() {
                return DataFactory.createColumn(1, 2, 3);
            }

            @TestCase
            @TestData("mySetup")
            @com.sun.tck.lib.tgf.After("after")
            public void test(int i) {
                methodCalled[0] = true;
                counter[0]++;
            }
        };
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(testInstance, TU.EMPTY_ARGV);
        assertTrue(status.isOK());
        assertTrue(testInstance.initialAfterWasCalled);
        assertTrue(methodCalled[0]);
        assertEquals(3, counter[0]);
    }

    @Test
    public void inheritingAfter_NoParams() {
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};

        PredefinedAfter testInstance = new PredefinedAfter() {
            @TestCase
            @com.sun.tck.lib.tgf.After("after")
            public void test() {
                methodCalled[0] = true;
                counter[0]++;
            }
        };
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(testInstance, TU.EMPTY_ARGV);
        assertTrue(status.isOK());
        assertTrue(testInstance.initialAfterWasCalled);
        assertTrue(methodCalled[0]);
        assertEquals(1, counter[0]);
    }

    @Test
    public void overridingAfter() {
        final boolean[] newAfterCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};
        final int[] afterCounter = new int[]{0};

        PredefinedAfter testInstance = new PredefinedAfter() {
            protected Values mySetup() {
                return DataFactory.createColumn(1, 2, 3);
            }

            protected void after() {
                org.junit.Assert.assertTrue(methodCalled[0]);
                // to be sure that it was called only once
                org.junit.Assert.assertFalse(newAfterCalled[0]);
                newAfterCalled[0] = true;
                afterCounter[0]++;
            }

            @TestCase
            @TestData("mySetup")
            @com.sun.tck.lib.tgf.After("after")
            public void test(int i) {
                org.junit.Assert.assertFalse(newAfterCalled[0]);
                methodCalled[0] = true;
                counter[0]++;
            }
        };
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(testInstance, TU.EMPTY_ARGV);
        assertTrue(status.isOK());
        assertTrue(newAfterCalled[0]);
        assertFalse(testInstance.initialAfterWasCalled);
        assertTrue(methodCalled[0]);
        assertEquals(3, counter[0]);
        assertEquals(1, afterCounter[0]);
    }

    @Test
    public void afterNotFound() {

        PrintWriter log = TU.createMockedPrintWriter();
        PrintWriter ref = TU.createMockedPrintWriter();

        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};


        TestResult status = TU.runTestGroup(new TestAfter.Mytest() {
                                               protected Values mySetup() {
                                                   return DataFactory.createColumn(1, 2, 3);
                                               }

                                               @TestCase
                                               @TestData("mySetup")
                                               @com.sun.tck.lib.tgf.After("myAfterThatDoesnotExist")
                                               public void test(int i) {
                                                   methodCalled[0] = true;
                                                   counter[0]++;
                                               }
                                           }
                , log, ref, TU.EMPTY_ARGV
        );
        assertTrue(!status.isOK());
        assertTrue(methodCalled[0]);
        assertEquals(3, counter[0]);

        InOrder inOrder = inOrder(log, ref);
        inOrder.verify(log).println("Method \"myAfterThatDoesnotExist\" doesn't exist.");
        inOrder.verify(log).println("test: Failed. Failed trying to invoke @After method \"myAfterThatDoesnotExist\"");
        inOrder.verify(log).flush();
        inOrder.verify(ref).flush();
        verifyNoMoreInteractions(log, ref);

    }

    @Test
    public void afterNotFound_NoParams() {

        PrintWriter log = TU.createMockedPrintWriter();
        PrintWriter ref = TU.createMockedPrintWriter();

        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};


        TestResult status = TU.runTestGroup(new TestAfter.Mytest() {
                                               @TestCase
                                               @com.sun.tck.lib.tgf.After("myAfterThatDoesnotExist")
                                               public void test() {
                                                   methodCalled[0] = true;
                                                   counter[0]++;
                                               }
                                           }
                , log, ref, TU.EMPTY_ARGV
        );
        assertTrue(!status.isOK());
        assertTrue(methodCalled[0]);
        assertEquals(1, counter[0]);


        InOrder inOrder = inOrder(log, ref);
        inOrder.verify(log).println("Method \"myAfterThatDoesnotExist\" doesn't exist.");
        inOrder.verify(log).println("test: Failed. Failed trying to invoke @After method \"myAfterThatDoesnotExist\"");
        inOrder.verify(log).flush();
        inOrder.verify(ref).flush();
        verifyNoMoreInteractions(log, ref);


    }

    @Test
    public void invalidSignature() {
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new TestAfter.Mytest() {
            protected Values mySetup() {
                return DataFactory.createColumn(1, 2, 3);
            }

            protected boolean afterWithInvalidSignature(int i, int j) {
                org.junit.Assert.assertTrue(methodCalled[0]);
                // to be sure that it was called only once
                return true;
            }

            @TestCase
            @TestData("mySetup")
            @com.sun.tck.lib.tgf.After("afterWithInvalidSignature")
            public void test(int i) {
                methodCalled[0] = true;
                counter[0]++;
            }
        }, TU.EMPTY_ARGV);
        assertTrue(!status.isOK());
        assertTrue(methodCalled[0]);
        assertEquals(3, counter[0]);
    }

    @Test
    public void ifTestThrowsExceptionAfterIsNotCalled() {

        final boolean[] afterCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};
        final int[] afterCounter = new int[]{0};

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new TestAfter.Mytest() {

            private void myAfter() {
                org.junit.Assert.assertTrue(methodCalled[0]);
                afterCalled[0] = true;
                afterCounter[0]++;
            }

            @TestCase
            @TestData("setup")
            @com.sun.tck.lib.tgf.After("myAfter")
            public void test(int i) {
                org.junit.Assert.assertFalse(afterCalled[0]);
                methodCalled[0] = true;
                counter[0]++;
                throw new RuntimeException("Thrown from test");
            }
        }, TU.EMPTY_ARGV);
        assertFalse(status.isOK());
        assertTrue(afterCalled[0]);
        assertTrue(methodCalled[0]);
        assertEquals(3, counter[0]);
        assertEquals(1, afterCounter[0]);
    }

    @Test
    public void ifTestThrowsExceptionAfterIsNotCalled_NoParams() {

        final boolean[] afterCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};
        final int[] afterCounter = new int[]{0};

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new TestAfter.Mytest() {

            private void myAfter() {
                org.junit.Assert.assertTrue(methodCalled[0]);
                afterCalled[0] = true;
                afterCounter[0]++;
            }

            @TestCase
            @com.sun.tck.lib.tgf.After("myAfter")
            public void test() {
                org.junit.Assert.assertFalse(afterCalled[0]);
                methodCalled[0] = true;
                counter[0]++;
                throw new RuntimeException("Thrown from test");
            }
        }, TU.EMPTY_ARGV);
        assertFalse(status.isOK());
        assertTrue(afterCalled[0]);
        assertTrue(methodCalled[0]);
        assertEquals(1, counter[0]);
        assertEquals(1, afterCounter[0]);
    }

    @Test
    public void runtimeSkippingFromAfter_testPasses() {

        final boolean[] afterCalled = new boolean[1];
        final boolean[] methodCalled = new boolean[1];
        final int[] counter = new int[]{0};
        final int[] afterCounter = new int[]{0};

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new TestAfter.Mytest() {
            protected Values mySetup() {
                return DataFactory.createColumn(1, 2, 3);
            }

            private void myAfter() {
                org.junit.Assert.assertTrue(methodCalled[0]);
                // to be sure that it was called only once
                org.junit.Assert.assertFalse(afterCalled[0]);
                afterCalled[0] = true;
                afterCounter[0]++;
                com.sun.tck.lib.Assert.reportNotApplicable(true);
            }

            @TestCase
            @TestData("mySetup")
            @com.sun.tck.lib.tgf.After("myAfter")
            public void test(int i) {
                org.junit.Assert.assertFalse(afterCalled[0]);
                methodCalled[0] = true;
                counter[0]++;
            }
        }, TU.EMPTY_ARGV);
        assertTrue(status.isOK());
        assertEquals("test cases: 1; all not applicable", status.getMessage());
        assertTrue(afterCalled[0]);
        assertTrue(methodCalled[0]);
        assertEquals(3, counter[0]);
        assertEquals(1, afterCounter[0]);
    }

    @Test
    public void runtimeSkippingFromAfter_testFails() {


        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new TestAfter.Mytest() {
            protected Values mySetup() {
                return DataFactory.createColumn(1, 2, 3);
            }

            private void myAfter() {
                com.sun.tck.lib.Assert.reportNotApplicable(true);
            }

            @TestCase
            @TestData("mySetup")
            @com.sun.tck.lib.tgf.After("myAfter")
            public void test(int i) {
                Assert.fail("Failed!!!");
            }
        }, TU.EMPTY_ARGV);
        assertTrue(status.isOK());
        assertEquals("test cases: 1; all not applicable", status.getMessage());
    }

    public static class ExceptionThrownFromAfter extends RuntimeException {
        public static String RANDOM = TU.generateRandomString();

        public ExceptionThrownFromAfter() {
            super("This exception is thrown from After " + RANDOM);
        }

        public void printStackTrace(PrintWriter s) {
            s.println("Haha - this is overriden printStackTrace " + RANDOM);
        }
    }

    @TestGroup
    public abstract static class Mytest {
        protected Values setup() {
            return DataFactory.createColumn(1, 1, 1);
        }
    }

    @TestGroup
    public abstract static class PredefinedAfter {

        boolean initialAfterWasCalled;

        protected void after() {
            initialAfterWasCalled = true;
        }

        protected Values setup() {
            return DataFactory.createColumn(1);
        }
    }


}

