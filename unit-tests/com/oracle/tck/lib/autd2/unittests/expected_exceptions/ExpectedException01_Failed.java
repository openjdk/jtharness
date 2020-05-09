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
package com.oracle.tck.lib.autd2.unittests.expected_exceptions;

import com.oracle.tck.lib.autd2.unittests.TU;
import com.sun.tck.lib.ExpectedExceptions;
import com.sun.tck.test.TestCase;
import com.sun.tck.test.TestGroup;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class ExpectedException01_Failed {

    @TestGroup
    public static class TestGroup01 {
        static int testCaseMethodCalled;

        @TestCase
        @ExpectedExceptions({NullPointerException.class})
        public void test123423() {
            testCaseMethodCalled++;
        }
    }

    @Test
    public void oneTestCase() {
        TestGroup01.testCaseMethodCalled = 0;
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(new TestGroup01(), TU.EMPTY_ARGV);
        Assert.assertTrue(!s.isOK());
        Assert.assertEquals("test cases: 1; all failed", s.getMessage());
        Assert.assertEquals(1, TestGroup01.testCaseMethodCalled);
    }

}
