/*
 * $Id$
 *
 * Copyright (c) 1996, 2021, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.tck.lib.autd2.unittests.tgfported.plainfailing;

import com.oracle.tck.lib.autd2.unittests.BaseTestGroup;
import com.oracle.tck.lib.autd2.unittests.CustomException;
import com.oracle.tck.lib.autd2.unittests.TU;
import com.sun.tck.lib.tgf.DataFactory;
import com.sun.tck.lib.tgf.TestData;
import com.sun.tck.lib.tgf.Values;
import com.sun.tck.test.TestCase;
import com.sun.tck.test.TestGroup;

import org.junit.Assert;
import org.junit.Test;

import java.io.PrintWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import org.mockito.InOrder;


public class TestCaseThrowsException {



    @Test
    public void unexpectedRuntimeException() {
        PrintWriter log = TU.createMockedPrintWriter();
        PrintWriter ref = TU.createMockedPrintWriter();


        BaseTestGroup baseTestGroup = new BaseTestGroup() {
            Values setup() {
                return DataFactory.createColumn(115);
            }

            @TestCase
            @TestData("setup")
            public void theTest(int i) {
                throw new CustomException(Integer.toString(i));
            }
        };
        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(
                baseTestGroup, log, ref);

        Assert.assertTrue(!status.isOK());
        Assert.assertEquals("Failed. test cases: 1; all failed", status.toString());

        InOrder inOrder = inOrder(log, ref);
        inOrder.verify(log).println("Testcase \"theTest(0)\" has thrown an unexpected exception com.oracle.tck.lib.autd2.unittests.CustomException: 115");
        inOrder.verify(log).println(CustomException.STACKTRACE_LINE);
        inOrder.verify(log).println("Testcase \"theTest(0)\" failed with arguments [115]");
        inOrder.verify(log).println("theTest: Failed. Testcase \"theTest(0)\" has thrown an unexpected exception com.oracle.tck.lib.autd2.unittests.CustomException: 115");
        inOrder.verify(log).flush();
        inOrder.verify(ref).flush();
        verifyNoMoreInteractions(log, ref);
    }




    @Test
    public void primitiveTest() {

        @TestGroup
        class _TestCase {

            private boolean properMethodWasCalled;
            private boolean wrongMethodWasCalled;

            private Values setup() {
                return DataFactory.createColumn(1);
            }

            @TestCase
            @TestData("setup")
            public void test(int i) {
                this.properMethodWasCalled = true;
                throw new RuntimeException("Test just failed");
            }

             void testThatShouldNotBeCalled() {
                this.wrongMethodWasCalled = true;
            }

             boolean isProperMethodWasCalled() {
                return properMethodWasCalled;
            }

             boolean isWrongMethodWasCalled() {
                return wrongMethodWasCalled;
            }

        }

        _TestCase testCase = new _TestCase();
        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(testCase);
        assertTrue(testCase.isProperMethodWasCalled());
        assertFalse(testCase.isWrongMethodWasCalled());

        assertTrue(!status.isOK());
        assertEquals("test cases: 1; all failed",
                status.getMessage());
    }






}
