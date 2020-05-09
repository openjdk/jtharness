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
import com.sun.tck.lib.tgf.DataFactory;
import com.sun.tck.lib.tgf.TestData;
import com.sun.tck.lib.tgf.Values;
import com.sun.tck.test.TestCase;
import com.sun.tck.test.TestGroup;

/**
 *
 */
public class SeveralAnnotationsRecognized extends junit.framework.TestCase {

    public void test() {


        SeveralAnnotationsRecognized._TestCase testCase = new SeveralAnnotationsRecognized._TestCase();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(testCase, TU.EMPTY_ARGV);

        assertTrue(testCase.isTest0001WasCalled());
        assertTrue(testCase.isTest0002WasCalled());
        assertTrue(testCase.isTest0003WasCalled());
        assertTrue(testCase.isTest0004WasCalled());

        assertTrue(status.isOK());
    }

    @TestGroup
    protected class _TestCase {

        private boolean test0001WasCalled;
        private boolean test0002WasCalled;
        private boolean test0003WasCalled;
        private boolean test0004WasCalled;


        private Values setup() {
            return DataFactory.createColumn(1);
        }

        @TestCase
        @TestData("setup")
        public void test0001(int i) {
            this.test0001WasCalled = true;
        }

        @TestCase
        @TestData("setup")
        public void test0002(int i) {
            this.test0002WasCalled = true;
        }

        @TestCase
        @TestData("setup")
        public void test0003(int i) {
            this.test0003WasCalled = true;
        }

        @TestCase
        @TestData("setup")
        public void test0004(int i) {
            this.test0004WasCalled = true;
        }

        boolean isTest0001WasCalled() {
            return test0001WasCalled;
        }

        boolean isTest0002WasCalled() {
            return test0002WasCalled;
        }

        boolean isTest0003WasCalled() {
            return test0003WasCalled;
        }

        boolean isTest0004WasCalled() {
            return test0004WasCalled;
        }

    }


}
