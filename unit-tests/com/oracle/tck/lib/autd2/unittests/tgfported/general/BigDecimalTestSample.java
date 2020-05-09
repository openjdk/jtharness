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
import org.junit.Assert;

import java.math.BigDecimal;

import static java.math.BigDecimal.valueOf;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class BigDecimalTestSample {


    @org.junit.Test
    public void test() {

        @TestGroup
        class MyBigDecimalTest {

            Values setup_1() {
                return DataFactory.createColumn(valueOf(0), valueOf(3)).multiply(valueOf(63), valueOf(17));
            }

            Values setup_2() {
                return DataFactory.createColumn(valueOf(1), valueOf(4)).multiply(valueOf(33), valueOf(7));
            }

            Values setup_3() {
                return setup_1().unite(setup_2());
            }



            @TestCase
            @TestData("setup_3")
            public void testMultiply(BigDecimal instance, BigDecimal anotherInstance) {
                long expectedResult = instance.longValue() * anotherInstance.longValue();
                BigDecimal result = instance.multiply(anotherInstance);
                Assert.assertEquals(expectedResult, result.longValue());
            }

            @TestCase
            @TestData("setup_2")
            public void testAdd(BigDecimal instance, BigDecimal anotherInstance) {
                long expectedResult = instance.longValue() + anotherInstance.longValue();
                BigDecimal result = instance.add(anotherInstance);
                Assert.assertEquals(expectedResult, result.longValue());
            }

        }

        final MyBigDecimalTest test = new MyBigDecimalTest();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(test, TU.EMPTY_ARGV);

        assertTrue(status.isOK());
        System.out.println(status);

    }


}
