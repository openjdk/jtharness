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
package com.oracle.tck.lib.autd2.unittests.tgfported.executeargs;

import com.oracle.tck.lib.autd2.unittests.BaseTestGroup;
import com.sun.tck.lib.tgf.DataFactory;
import com.sun.tck.lib.tgf.TestData;
import com.sun.tck.lib.tgf.Values;
import com.sun.tck.test.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.oracle.tck.lib.autd2.unittests.TU.*;
import static com.sun.tck.lib.tgf.TGFUtils.*;

/**
 *
 */
public class Exclusion {


    private void check_2(String... args) {
        final int[] methodCounter = new int[]{0};
        final int[] blablaCounter = new int[]{0};
        final int[] fooCounter = new int[]{0};
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            protected Values mySetup() {
                return DataFactory.createColumn(11, 22, 33, 44, 55);
            }

            @TestCase
            @TestData("mySetup")
            public void myTest(int i) {
                methodCounter[0]++;
            }

            @TestCase
            @TestData("mySetup")
            public void blabla(int i) {
                blablaCounter[0]++;
            }

            @TestCase
            @TestData("mySetup")
            public void foo(int i) {
                fooCounter[0]++;
            }

        }, args);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(5, methodCounter[0]);
        Assert.assertEquals(0, blablaCounter[0]);
        Assert.assertEquals(0, fooCounter[0]);
    }

    @Test
    public void tempTest_2() {
        check_2(
                "4532542345", "ewrtwert", "Wertwert",
                EXCLUDE_WORD, "blabla",
                EXCLUDE_WORD, "foo",
                "dfsgdfg");
    }

    @Test
    public void tempTest32() {
        check_2(EXCLUDE_WORD, "blabla", "adsfasfd",
                EXCLUDE_WORD, "foo");
    }

    @Test
    public void tempTest33() {
        check_2(EXCLUDE_WORD, "blabla,foo", "adsfasfd");
    }

    @Test
    public void tempTest34() {
        check_2(EXCLUDE_WORD, "foo,blabla", "adsfasfd");
    }

    @Test
    public void tempTest35() {
        check_2(EXCLUDE_WORD, "foo,blabla");
    }

    @Test
    public void tempTest36() {
        check_2(EXCLUDE_WORD, "blabla,foo");
    }

    @Test
    public void tempTest37() {
        check_2("myTest", EXCLUDE_WORD, "blabla,foo");
    }

    @Test
    public void tempTest_nothingUseful_1() {
        checkNothingFound(new String[]{});
    }

    @Test
    public void tempTest_nothingUseful_2() {
        checkNothingFound(new String[]{"1341241234"});
    }

    @Test
    public void tempTest_nothingUseful_2_1() {
        checkNothingFound(new String[]{EXCLUDE_WORD});
    }

    @Test
    public void tempTest_nothingUseful_3() {
        checkNothingFound(new String[]{"234234", "324234", "dfgdsfg", "dfsgdf"});
    }

    @Test
    public void tempTest_nothingUseful_4() {
        checkNothingFound(new String[]{"234234", "324234", "dfgdsfg", EXCLUDE_WORD});
    }

    @Test
    public void tempTest_nothingUseful_5() {
        checkNothingFound(new String[]{"234234", "324234", "dfgdsfg", EXCLUDE_WORD, "wertwert"});
    }

    @Test
    public void tempTest_nothingUseful_6() {
        checkNothingFound(new String[]{"234234", "324234", "dfgdsfg", EXCLUDE_WORD, "wertwert,wertwert,wertwet#3.4"});
    }

    @Test
    public void tempTest_nothingUseful_8() {
        checkNothingFound(new String[]{"23,4234", "324234", EXCLUDE_WORD});
    }

    private void checkNothingFound(String[] args) {
        final int[] methodCounter = new int[]{0};
        final ArrayList<Integer> expectedArgValues =
                new ArrayList<Integer>() {
                    {
                        add(1);
                        add(2);
                        add(3);
                    }
                };
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            protected Values mySetup() {
                return DataFactory.createColumn(1, 2, 3);
            }

            @TestCase
            @TestData("mySetup")
            public void myTest(int i) {
                Assert.assertTrue(i + " is not among expected", expectedArgValues.contains(i));
                methodCounter[0]++;
            }
        }, args);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(3, methodCounter[0]);
    }

    @Test
    public void test_excludedArg() {
        final int[] methodCounter = new int[]{0};
        final ArrayList<Integer> expectedArgValues =
                new ArrayList<Integer>() {
                    {
                        add(2);
                        add(3);
                    }
                };
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            protected Values mySetup() {
                return DataFactory.createColumn(1, 2, 3);
            }

            @TestCase
            @TestData("mySetup")
            public void myTest(int i) {
                Assert.assertTrue(i + " is not among expected", expectedArgValues.contains(i));
                methodCounter[0]++;
            }
        }, new String[]{EXCLUDE_WORD, "myTest"});
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(0, methodCounter[0]);
    }

    @Test
    public void test_excludedWholeTest_1() {
        check_ExcludeWholeTest(EXCLUDE_WORD, "myTest");
    }

    @Test
    public void test_excludedWholeTest_2() {
        check_ExcludeWholeTest(EXCLUDE_WORD, "myTest,someOtherTest");
    }

    @Test
    public void test_excludedWholeTest_3() {
        check_ExcludeWholeTest(EXCLUDE_WORD, "qwer", EXCLUDE_WORD, "myTest,someOtherTest");
    }

    @Test
    public void test_excludedWholeTest_4() {
        check_ExcludeWholeTest(EXCLUDE_WORD, "someOtherTest,r,wert,ert,myTest,tr,rty");
    }

    private void check_ExcludeWholeTest(String... args) {
        final int[] methodCounter = new int[]{0};
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            protected Values mySetup() {
                return DataFactory.createColumn(1, 2, 3);
            }

            @TestCase
            @TestData("mySetup")
            public void myTest(int i) {
                methodCounter[0]++;
            }
        }, args);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(0, methodCounter[0]);
    }


    @Test
    public void test_Mixed_1() {
        final int[] myTestCounter = new int[]{0};
        final int[] blablaCounter = new int[]{0};
        final int[] fooCounter = new int[]{0};
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            private Values mySetup() {
                return DataFactory.createColumn(11, 22, 33, 44, 55, 66);
            }

            @TestCase
            public void myTest() {
                myTestCounter[0]++;
            }

            @TestCase
            public void blabla() {
                blablaCounter[0]++;
            }

            @TestCase
            @TestData("mySetup")
            public void foo(int i) {
                fooCounter[0]++;
            }

        }, new String[]{EXCLUDE_WORD});
        Assert.assertTrue(status.isOK());
        Assert.assertEquals("Passed. test cases: 3; all passed", status.toString());
        Assert.assertEquals(1, myTestCounter[0]);
        Assert.assertEquals(1, blablaCounter[0]);
        Assert.assertEquals(6, fooCounter[0]);
    }

    @Test
    public void test_Mixed_2() {
        check_Mixed_2(EXCLUDE_WORD, " myTest");
    }

    @Test
    public void test_Mixed_2_1() {
        check_Mixed_2(EXCLUDE_WORD, "one, myTest,two");
    }

    @Test
    public void test_Mixed_2_2() {
        check_Mixed_2(EXCLUDE_WORD, "sdfgsdg", "qwerty", EXCLUDE_WORD, "one,myTest,two");
    }

    @Test
    public void test_Mixed_2_3() {
        check_Mixed_2(EXCLUDE_WORD, " one", EXCLUDE_WORD, "myTest", EXCLUDE_WORD, "   two   ");
    }

    @Test
    public void test_Mixed_2_4() {
        check_Mixed_2(EXCLUDE_WORD, "one ", EXCLUDE_WORD, "myTest", EXCLUDE_WORD, " two,qwerqwer,adfadfasdf,asdfasdf");
    }

    private void check_Mixed_2(String... args) {
        final int[] myTestCounter = new int[]{0};
        final int[] blablaCounter = new int[]{0};
        final int[] fooCounter = new int[]{0};
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            private Values mySetup() {
                return DataFactory.createColumn(11, 22, 33, 44, 55, 66);
            }

            @TestCase
            public void myTest() {
                myTestCounter[0]++;
            }

            @TestCase
            public void blabla() {
                blablaCounter[0]++;
            }

            @TestCase
            @TestData("mySetup")
            public void foo(int i) {
                fooCounter[0]++;
            }

        }, args);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals("Passed. test cases: 2; all passed", status.toString());
        Assert.assertEquals(0, myTestCounter[0]);
        Assert.assertEquals(1, blablaCounter[0]);
        Assert.assertEquals(6, fooCounter[0]);
    }

    @Test
    public void test_Mixed_3_1() {
        check_Mixed_3(EXCLUDE_WORD, "myTest", EXCLUDE_WORD, "blabla");
    }

    @Test
    public void test_Mixed_3_1_1() {
        check_Mixed_3(EXCLUDE_WORD, "myTest", EXCLUDE_WORD, "blabla", EXCLUDE_WORD, EXCLUDE_WORD);
    }

    @Test
    public void test_Mixed_3_2() {
        check_Mixed_3(EXCLUDE_WORD, "myTest,blabla");
    }

    @Test
    public void test_Mixed_3_3() {
        check_Mixed_3(EXCLUDE_WORD, "blabla,myTest");
    }

    @Test
    public void test_Mixed_3_4() {
        check_Mixed_3(EXCLUDE_WORD, "myTest,blabla,somethingElse");
    }

    @Test
    public void test_Mixed_3_5() {
        check_Mixed_3(EXCLUDE_WORD, "blabla", "something", EXCLUDE_WORD, "myTest,somethingElse");
    }

    @Test
    public void test_Mixed_3_6() {
        check_Mixed_3(EXCLUDE_WORD, "myTest,asdf23141234sdfasfd,blabla", EXCLUDE_WORD, "somethingElse");
    }

    private void check_Mixed_3(String... args) {
        final int[] myTestCounter = new int[]{0};
        final int[] blablaCounter = new int[]{0};
        final int[] fooCounter = new int[]{0};
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            private Values mySetup() {
                return DataFactory.createColumn(11, 22, 33, 44, 55, 66);
            }

            @TestCase
            public void myTest() {
                myTestCounter[0]++;
            }

            @TestCase
            public void blabla() {
                blablaCounter[0]++;
            }

            @TestCase
            @TestData("mySetup")
            public void foo(int i) {
                fooCounter[0]++;
            }

        }, args);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals("Passed. test cases: 1; all passed", status.toString());
        Assert.assertEquals(0, myTestCounter[0]);
        Assert.assertEquals(0, blablaCounter[0]);
        Assert.assertEquals(6, fooCounter[0]);
    }

    private void check_Mixed_4(List<Integer> allData,
                               List<Integer> expectedPassedToTestCase,
                               String expectedStatusMessage,
                               final int expectedMyTestCounter,
                               final int expectedBlablaCounter, String... args) {
        final int[] myTestCounter = new int[]{0};
        final int[] blablaCounter = new int[]{0};
        final int[] fooCounter = new int[]{0};
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            private List mySetup() {
                return allData;
            }

            @TestCase
            public void myTest() {
                myTestCounter[0]++;
            }

            @TestCase
            public void blabla() {
                blablaCounter[0]++;
            }

            @TestCase
            @TestData("mySetup")
            public void foo(int i) {
                Assert.assertEquals(
                        (int) expectedPassedToTestCase.get(fooCounter[0]), i);
                fooCounter[0]++;
            }

        }, args);
        Assert.assertTrue(status.isOK());
        Assert.assertEquals(expectedStatusMessage, status.toString());
        Assert.assertEquals(expectedMyTestCounter, myTestCounter[0]);
        Assert.assertEquals(expectedBlablaCounter, blablaCounter[0]);
        Assert.assertEquals(expectedPassedToTestCase.size(), fooCounter[0]);
    }

    @Test
    public void test_Mixed_4_1() {
        check_Mixed_4(
                List.of(11, 22, 33, 44, 55, 66), List.of(11, 22, 33, 44, 55, 66),
                "Passed. test cases: 1; all passed", 0, 0,
                EXCLUDE_WORD, "myTest,blabla", EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void test_Mixed_4_2() {
        check_Mixed_4(
                List.of(11, 22, 33), List.of(11, 22, 33),
                "Passed. test cases: 2; all passed", 1, 0,
                EXCLUDE_WORD, "blabla", EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void test_Mixed_4_2_1() {
        check_Mixed_4(
                List.of(11, 22, 33), List.of(),
                "Passed. test cases: 1; all passed", 1, 0,
                EXCLUDE_WORD, "blabla,foo");
    }

    @Test
    public void test_Mixed_4_2_2() {
        check_Mixed_4(
                List.of(11, 22, 33), List.of(),
                "Passed. No test cases found (or all test cases excluded.)", 0, 0,
                EXCLUDE_WORD, "blabla,foo,myTest");
    }

    @Test
    public void test_Micro_1() {
        check_Mixed_4(
                List.of(11, 22, 33),
                List.of(22, 33),
                "Passed. test cases: 2; all passed", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "0" + INDICES_END, EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void test_Micro_1_restIgnored() {
        check_Mixed_4(
                List.of(11, 22, 33),
                List.of(22, 33),
                "Passed. test cases: 2; all passed", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "0" + INDICES_END + ")()()()()()", EXCLUDE_WORD, "somethingElse");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_Micro_1_IEA() {
        check_Mixed_4(
                List.of(11, 22, 33),
                List.of(22, 33),
                "Passed. test cases: 2; all passed", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "0", EXCLUDE_WORD, "somethingElse");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_Micro_1_EmptyRange() {
        check_Mixed_4(
                List.of(11, 22, 33),
                List.of(22, 33),
                "Passed. test cases: 2; all passed", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + INDICES_END, EXCLUDE_WORD, "somethingElse");
    }


    @Test(expected = IllegalArgumentException.class)
    public void test_Micro_1_BrokenRange() {
        check_Mixed_4(
                List.of(11, 22, 33),
                List.of(22, 33),
                "Passed. test cases: 2; all passed", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + INDICES_START + INDICES_END, EXCLUDE_WORD, "somethingElse");
    }


    @Test
    public void test_Micro_2() {
        check_Mixed_4(
                List.of(11, 22, 33),
                List.of(11, 33),
                "Passed. test cases: 2; all passed", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "1" + INDICES_END, EXCLUDE_WORD, "somethingElse");
    }


    @Test
    public void test_Micro_2_repeatedSeveralTimes() {
        check_Mixed_4(
                List.of(11, 22, 33),
                List.of(11, 33),
                "Passed. test cases: 2; all passed", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "1" + INDICES_END,
                INDICES_START + "1",
                INDICES_START + "1",
                INDICES_START + "1",
                INDICES_START + "1",
                EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void test_Micro_3() {
        check_Mixed_4(
                List.of(11, 2, 33),
                List.of(11, 2),
                "Passed. test cases: 2; all passed", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "2" + INDICES_END, EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void test_Micro_twoIndices() {
        check_Mixed_4(
                List.of(5, 2, 34),
                List.of(34),
                "Passed. test cases: 2; all passed", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "0" +
                        INDEX_SEPARATOR + "1" + INDICES_END, EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void test_Micro_twoIndices_1() {
        check_Mixed_4(
                List.of(5, 2, 34),
                List.of(5),
                "Passed. test cases: 2; all passed", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "1" +
                        INDEX_SEPARATOR + "2" + INDICES_END,
                EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void test_Micro_twoIndices_1_repeated() {
        check_Mixed_4(
                List.of(5, 2, 34),
                List.of(5),
                "Passed. test cases: 2; all passed", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "1" +
                        INDEX_SEPARATOR + "1" +
                        INDEX_SEPARATOR + "1" +
                        INDEX_SEPARATOR + "1" +
                        INDEX_SEPARATOR + "2" + INDICES_END,
                INDEX_SEPARATOR + "2",
                INDEX_SEPARATOR + "2" + INDICES_END,
                EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void test_Micro_threeIndices_1() {
        check_Mixed_4(
                List.of(5, 2, 34),
                List.of(),
                "Passed. test cases: 2; passed: 1; not applicable: 1", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "0" +
                        INDEX_SEPARATOR + "1" +
                        INDEX_SEPARATOR + "2" + INDICES_END,
                EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void test_Micro_threeIndices_2() {
        check_Mixed_4(
                List.of(5, 2, 34),
                List.of(),
                "Passed. test cases: 2; passed: 1; not applicable: 1", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "1" +
                        INDEX_SEPARATOR + "0" +
                        INDEX_SEPARATOR + "2" + INDICES_END,
                EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void test_Micro_threeIndices_3() {
        check_Mixed_4(
                List.of(5, 2, 34),
                List.of(),
                "Passed. test cases: 2; passed: 1; not applicable: 1", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "2" +
                        INDEX_SEPARATOR + "1" +
                        INDEX_SEPARATOR + "0" + INDICES_END,
                EXCLUDE_WORD, "somethingElse");
    }

    @Test(expected = NumberFormatException.class)
    public void test_Micro_threeIndices_3_broken() {
        check_Mixed_4(
                List.of(5, 2, 34),
                List.of(),
                "Passed. test cases: 2; passed: 1; not applicable: 1", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "2" +
                        INDEX_SEPARATOR + "1" +
                        INDEX_SEPARATOR + "g" + INDICES_END,
                EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void test_Micro_threeIndices_2_mentionedTwice() {
        check_Mixed_4(
                List.of(5, 2, 34),
                List.of(),
                "Passed. test cases: 2; passed: 1; not applicable: 1", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "1" +
                        INDEX_SEPARATOR + "0" +
                        INDEX_SEPARATOR + "0" +
                        INDEX_SEPARATOR + "2" + INDICES_END,
                EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void test_Micro5_several_separate_indices() {
        check_Mixed_4(
                List.of(8, 5, 89, 4, 15),
                List.of(),
                "Passed. test cases: 2; passed: 1; not applicable: 1", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "0" +
                        INDEX_SEPARATOR + "1" +
                        INDEX_SEPARATOR + "2" +
                        INDEX_SEPARATOR + "3" +
                        INDEX_SEPARATOR + "4" + INDICES_END,
                EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void test_Micro5_several_separate_indices_1() {
        check_Mixed_4(
                List.of(8, 5, 89, 4, 15),
                List.of(5, 4),
                "Passed. test cases: 2; all passed", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "0" +
                        INDEX_SEPARATOR + "2" +
                        INDEX_SEPARATOR + "4" + INDICES_END,
                EXCLUDE_WORD, "somethingElse");
    }


    @Test
    public void test_Micro5_several_separate_indices_1_secondColumnIgnored() {
        check_Mixed_4(
                List.of(8, 5, 89, 4, 15),
                List.of(5, 4),
                "Passed. test cases: 2; all passed", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "0" +
                        INDEX_SEPARATOR + "2" +
                        INDEX_SEPARATOR + "4" + INDICES_END + INDICES_START + "1" + INDICES_END,
                EXCLUDE_WORD, "somethingElse");
    }


    @Test
    public void test_Micro5_fullRange() {
        check_Mixed_4(
                List.of(8, 5, 89, 4, 15),
                List.of(),
                "Passed. test cases: 2; passed: 1; not applicable: 1", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "0" +
                        RANGE_DELIMITER + "4" + INDICES_END,
                EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void test_Micro5_fullRange_exceedingRange() {
        check_Mixed_4(
                List.of(8, 5, 89, 4, 15),
                List.of(),
                "Passed. test cases: 2; passed: 1; not applicable: 1", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "0" +
                        RANGE_DELIMITER + "10" + INDICES_END,
                EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void test_Micro5_severalRanges() {
        check_Mixed_4(
                List.of(8, 5, 89, 4, 15, 6),
                List.of(4),
                "Passed. test cases: 2; all passed", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "0" +
                        RANGE_DELIMITER + "2" +
                        INDEX_SEPARATOR + "4" +
                        RANGE_DELIMITER + "5" + INDICES_END,
                EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void test_Micro5_severalRanges_filteringCompletely() {
        check_Mixed_4(
                List.of(8, 5, 89, 4, 15, 6),
                List.of(),
                "Passed. test cases: 2; passed: 1; not applicable: 1", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "0" +
                        RANGE_DELIMITER + "2" +
                        INDEX_SEPARATOR + "3" +
                        RANGE_DELIMITER + "5" + INDICES_END,
                EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void test_Micro5_severalRanges_filteringCompletely_1() {
        check_Mixed_4(
                List.of(8, 5, 89, 4, 15, 6),
                List.of(),
                "Passed. test cases: 2; passed: 1; not applicable: 1", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "0" +
                        RANGE_DELIMITER + "1" +
                        INDEX_SEPARATOR + "1" +
                        RANGE_DELIMITER + "3" +
                        INDEX_SEPARATOR + "4" + RANGE_DELIMITER + "5" + INDICES_END,
                EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void test_Micro5_severalRanges_filteringCompletely_2_overlap() {
        check_Mixed_4(
                List.of(8, 5, 89, 4, 15, 6),
                List.of(),
                "Passed. test cases: 2; passed: 1; not applicable: 1", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "0" +
                        RANGE_DELIMITER + "1" +
                        INDEX_SEPARATOR + "1" +
                        RANGE_DELIMITER + "5" +
                        INDEX_SEPARATOR + "3" + RANGE_DELIMITER + "5" + INDICES_END,
                EXCLUDE_WORD, "somethingElse");
    }


    @Test(expected = IllegalArgumentException.class)
    public void test_incorrectRange() {
        check_Mixed_4(
                List.of(8, 5, 89, 4, 15, 6),
                List.of(),
                "Passed. test cases: 2; passed: 1; not applicable: 1", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "0" +
                        RANGE_DELIMITER + "0",
                EXCLUDE_WORD, "somethingElse");
    }


    @Test(expected = IllegalArgumentException.class)
    public void test_incorrectRange_1() {
        check_Mixed_4(
                List.of(8, 5, 89, 4, 15, 6),
                List.of(),
                "Passed. test cases: 2; passed: 1; not applicable: 1", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "1" +
                        RANGE_DELIMITER + "0",
                EXCLUDE_WORD, "somethingElse");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_incorrectRange_2() {
        check_Mixed_4(
                List.of(8, 5, 89, 4, 15, 6),
                List.of(),
                "Passed. test cases: 2; passed: 1; not applicable: 1", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "-5" +
                        RANGE_DELIMITER + "0",
                EXCLUDE_WORD, "somethingElse");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_incorrectRange_tripleRange() {
        check_Mixed_4(
                List.of(8, 5, 89, 4, 15, 6),
                List.of(),
                "Passed. test cases: 2; passed: 1; not applicable: 1", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "0" + RANGE_DELIMITER + "2" + RANGE_DELIMITER + "4",
                EXCLUDE_WORD, "somethingElse");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_incorrectRange_tripleRange_01() {
        check_Mixed_4(
                List.of(8, 5, 89, 4, 15, 6),
                List.of(),
                "Passed. test cases: 2; passed: 1; not applicable: 1", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "0" + RANGE_DELIMITER + "0" + INDICES_END);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_incorrectRange_tripleRange_02() {
        check_Mixed_4(
                List.of(8, 5, 89, 4, 15, 6),
                List.of(),
                "Passed. test cases: 2; passed: 1; not applicable: 1", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "1" + RANGE_DELIMITER + "0" + INDICES_END);
    }


    @Test
    public void test_Micro6_severalRanges_1() {
        check_Mixed_4(
                List.of(8, 5, 89, 4, 15, 6, 7, 8),
                List.of(8, 4),
                "Passed. test cases: 2; all passed", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "1" +
                        RANGE_DELIMITER + "2" +
                        INDEX_SEPARATOR + "4" +
                        RANGE_DELIMITER + "7" + INDICES_END,
                EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void test_Micro6_severalRanges_2() {
        check_Mixed_4(
                List.of(8, 5, 89, 4, 15, 6, 7, 8),
                List.of(8, 4, 8),
                "Passed. test cases: 2; all passed", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "1" +
                        RANGE_DELIMITER + "2" +
                        INDEX_SEPARATOR + "4" +
                        RANGE_DELIMITER + "6" + INDICES_END,
                EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void test_Micro6_severalRanges_3() {
        check_Mixed_4(
                List.of(8, 5, 89, 4, 15, 6, 7, 8),
                List.of(8, 4, 8),
                "Passed. test cases: 2; all passed", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "4" +
                        RANGE_DELIMITER + "6" +
                        INDEX_SEPARATOR + "1" +
                        RANGE_DELIMITER + "2" + INDICES_END,
                EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void test_Micro6_severalRanges_4() {
        check_Mixed_4(
                List.of(8, 5, 89, 4, 15, 6, 7, 8),
                List.of(8, 5, 89, 4, 15),
                "Passed. test cases: 2; all passed", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "5" +
                        RANGE_DELIMITER + "7" +
                        INDEX_SEPARATOR + "5" +
                        RANGE_DELIMITER + "7" + INDICES_END,
                EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void test_Micro6_severalRanges_5() {
        check_Mixed_4(
                List.of(8, 5, 89, 4, 15, 6, 7, 8),
                List.of(8),
                "Passed. test cases: 2; all passed", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "1" +
                        RANGE_DELIMITER + "7" +
                        INDEX_SEPARATOR + "3" +
                        RANGE_DELIMITER + "5" + INDICES_END,
                EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void test_Micro6_severalRanges_5_1() {
        check_Mixed_4(
                List.of(8, 5, 89),
                List.of(5, 89),
                "Passed. test cases: 3; all passed", 1, 1,
                EXCLUDE_WORD, "foo" + INDICES_START + "0" + INDICES_END);
    }



    @Test
    public void test_Micro6_allTheNumbers() {
        check_Mixed_4(
                List.of(8, 5, 89, 4, 15, 6, 7, 8),
                List.of(),
                "Passed. test cases: 2; passed: 1; not applicable: 1", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "0" +
                        INDEX_SEPARATOR + "1" +
                        INDEX_SEPARATOR + "2" +
                        INDEX_SEPARATOR + "3" +
                        INDEX_SEPARATOR + "4" +
                        INDEX_SEPARATOR + "5" +
                        INDEX_SEPARATOR + "6" +
                        INDEX_SEPARATOR + "7" +
                        INDICES_END);
    }

    @Test
    public void test_Micro6_fullRange() {
        check_Mixed_4(
                List.of(8, 5, 89, 4, 15, 6, 7, 8),
                List.of(),
                "Passed. test cases: 2; passed: 1; not applicable: 1", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "0" + RANGE_DELIMITER + "7" +
                        INDICES_END);
    }

    @Test
    public void test_Micro6_severalRanges_6() {
        check_Mixed_4(
                List.of(8, 5, 89, 4, 15, 6, 7, 8),
                List.of(8, 15),
                "Passed. test cases: 2; all passed", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "1" +
                        RANGE_DELIMITER + "3" +
                        INDEX_SEPARATOR + "5" +
                        RANGE_DELIMITER + "7" + INDICES_END + INDICES_START + "3452345" + INDICES_END,
                EXCLUDE_WORD, "somethingElse");
    }

    @Test(expected = NumberFormatException.class)
    public void test_Micro6_severalRanges_7_NFE() {
        check_Mixed_4(
                List.of(9, 5, 89, 4, 15, 6, 7, 8),
                List.of(9),
                "Passed. test cases: 2; all passed", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "1" +
                        INDEX_SEPARATOR + "5" +
                        INDICES_START + "2" +
                        RANGE_DELIMITER + "7" + INDICES_END,
                EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void test_Micro6_severalRanges_7() {
        check_Mixed_4(
                List.of(9, 5, 89, 4, 15, 6, 7, 8),
                List.of(9),
                "Passed. test cases: 2; all passed", 1, 0,
                EXCLUDE_WORD, "blabla,foo" +
                        INDICES_START + "1" +
                        INDEX_SEPARATOR + "5" +
                        INDEX_SEPARATOR + "2" +
                        RANGE_DELIMITER + "7" + INDICES_END,
                EXCLUDE_WORD, "somethingElse");
    }

    @Test
    public void excludingNotParameterizedWithParameterNumbers_02() {
        check_Mixed_4(
                List.of(8, 5, 89, 4, 15, 6),
                List.of(),
                "Passed. test cases: 2; all passed", 1, 1,
                EXCLUDE_WORD, "foo,blabla" +
                        INDICES_START + "0" + RANGE_DELIMITER + "50" + INDICES_END);
    }



}
