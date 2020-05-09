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
package com.oracle.tck.lib.autd2.unittests.tgfported.general;

import com.oracle.tck.lib.autd2.unittests.TU;
import com.sun.tck.test.TestGroup;
import org.junit.Test;

import static com.sun.tck.lib.Assert.*;

/**
 *
 */
public class NoTestsFound {

    @Test
    public void test() {

        NoTestsFound._TestCase testCase = new NoTestsFound._TestCase();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(testCase, TU.EMPTY_ARGV);

        assertFalse(testCase.isMethod_1_was_called());
        assertFalse(testCase.isMethod_2_was_called());

        assertTrue(status.isOK());
        assertEquals("Passed. No test cases found (or all test cases excluded.)", status.toString());
    }

    @TestGroup
    public static class _TestCase {

        private boolean method_1_was_called;
        private boolean method_2_was_called;

        void test1() {
           this.method_1_was_called = true;
           throw new RuntimeException();
        }

         void test2() {
           this.method_2_was_called = true;
        }

        boolean isMethod_1_was_called() {
            return method_1_was_called;
        }

        boolean isMethod_2_was_called() {
            return method_2_was_called;
        }

    }


}
