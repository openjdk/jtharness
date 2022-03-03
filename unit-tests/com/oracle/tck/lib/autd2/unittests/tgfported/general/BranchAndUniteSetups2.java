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
package com.oracle.tck.lib.autd2.unittests.tgfported.general;

import com.oracle.tck.lib.autd2.unittests.TU;
import com.sun.tck.lib.tgf.DataFactory;
import com.sun.tck.lib.tgf.TestData;
import com.sun.tck.lib.tgf.Values;
import com.sun.tck.test.TestCase;
import com.sun.tck.test.TestGroup;
import org.junit.Assert;
import static org.mockito.Mockito.*;


/**
 *
 */
public class BranchAndUniteSetups2 {


    private static String[] ROW_1 = {"1", "2", "3"};
    private static String[] ROW_2 = {"4", "5", "6"};
    private static String[] ROW_3 = {"7", "8", "9"};
    private static String[] ROW_4 = {"10", "11", "12"};

    @org.junit.Test public void test() {

        final BranchAndUniteSetups2.MyTestedInstance mock = mock(BranchAndUniteSetups2.MyTestedInstance.class);


        @TestGroup
        class MyTest {

            Values setup() {
                return DataFactory.createColumn((Object[])ROW_1).multiply((Object[])ROW_2).unite(
                        DataFactory.createColumn((Object[])ROW_3).multiply((Object[])ROW_4)
                );
            }

            @TestCase
            @TestData("setup")
            public void test(String s1, String s2) {
                mock.method(s1, s2);
            }
        }

        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new MyTest());

        Assert.assertTrue(status.isOK());          for (String s1 : ROW_1) {
            for (String s2 : ROW_2) {
                verify(mock).method(s1, s2);
            }
        }

        for (String s3 : ROW_3) {
            for (String s4 : ROW_4) {
                verify(mock).method(s3, s4);
            }
        }

    }

    @org.junit.Test
    public void test_2() {

        final BranchAndUniteSetups2.MyTestedInstance mock = mock(BranchAndUniteSetups2.MyTestedInstance.class);


        @TestGroup
        class MyTest {

            Values setup() {
                return DataFactory.createColumn("i_1", "i_2").multiply("j_1", "j_2").unite(
                        DataFactory.createColumn("k_1", "k_2").multiply("m_1", "m_2")
                );
            }

            @TestCase
            @TestData("setup")
            public void test(String s1, String s2) {
                mock.method(s1, s2);
            }
        }

        com.oracle.tck.lib.autd2.TestResult status = TU.runTestGroup(new MyTest());

        Assert.assertTrue(status.isOK());

        verify(mock).method("i_1", "j_1");
        verify(mock).method("i_1", "j_2");
        verify(mock).method("i_2", "j_1");
        verify(mock).method("i_2", "j_2");
        verify(mock).method("k_1", "m_1");
        verify(mock).method("k_1", "m_2");
        verify(mock).method("k_2", "m_1");
        verify(mock).method("k_2", "m_2");
        verifyNoMoreInteractions(mock);
    }


    public static interface MyTestedInstance {
        void method(String s1, String s2);
    }


}
