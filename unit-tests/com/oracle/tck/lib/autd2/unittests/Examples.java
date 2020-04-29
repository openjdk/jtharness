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
import com.sun.tck.TU;
import com.sun.tck.lib.ExpectedExceptions;
import com.sun.tck.lib.tgf.DataFactory;
import com.sun.tck.lib.tgf.TestData;
import com.sun.tck.lib.tgf.Transform;
import com.sun.tck.lib.tgf.Values;
import com.sun.tck.lib.tgf.data.Longs;
import com.sun.tck.lib.tgf.data.Operation;
import com.sun.tck.test.TestCase;
import com.sun.tck.test.TestGroup;
import org.junit.Assert;
import org.junit.Test;


import java.awt.Color;
import java.awt.color.ColorSpace;
import java.util.Arrays;
import java.util.List;
import java.util.RandomAccess;
import java.util.stream.IntStream;

import static com.sun.tck.lib.Assert.assertEquals;
import static com.sun.tck.lib.Assert.assertTrue;
import static com.sun.tck.lib.tgf.DataFactory.createColumn;
import static com.sun.tck.lib.tgf.DataFactory.createRow;
import static com.sun.tck.lib.tgf.data.Operation.TYPE.MULTIPLY;
import static java.awt.Color.*;
import static java.util.stream.Collectors.toList;

/**
 *
 */
public class Examples {


    @Test
    public void test_01() {
        @TestGroup
        class Test {
            @TestCase
            @ExpectedExceptions(ArithmeticException.class)
            public void test(@Longs({2L, 3L, 1000L, Long.MAX_VALUE}) long y) {
                Math.multiplyExact(Long.MAX_VALUE, y);
            }
        }
        TestResult testResult = TU.runTestGroup(new Test());
        Assert.assertTrue(testResult.isOK());
        Assert.assertEquals("test cases: 1; all passed", testResult.getMessage());
    }


    @Test
    public void test_02() {
        @TestGroup
        class Test {
            Values spaces =
                    createColumn(RED, GREEN, WHITE).filter(Color::getColorSpace);

            @TestCase
            public void test(@TestData("spaces") ColorSpace cs) {
                Assert.assertTrue(cs.isCS_sRGB());
            }
        }
        TestResult testResult = TU.runTestGroup(new Test());
        Assert.assertTrue(testResult.isOK());
        Assert.assertEquals("test cases: 1; all passed", testResult.getMessage());
    }

    @Test
    public void test_03() {
        @TestGroup
        class Test {
            Values colors =
                    createColumn(BLUE, BLACK, WHITE)
                            .<Color>filter(c -> createRow(c.darker(), c.brighter()));

            @TestCase
            @TestData("colors")
            public void test(Color darker, Color brighter) {
                assertTrue(darker.getRed() <= brighter.getRed());
                assertTrue(darker.getGreen() <= brighter.getGreen());
                assertTrue(darker.getBlue() <= brighter.getBlue());
            }
        }
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(new Test());
        Assert.assertTrue(s.isOK());
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }

    @Test
    public void test_03_array() {
        @TestGroup
        class Test {
            Values quantities =
                    createColumn(
                            new Color[]{BLUE, BLACK, WHITE},
                            new Color[]{BLACK, WHITE, BLUE, GREEN},
                            new Color[]{BLUE, BLACK},
                            new Color[]{RED}
                    ).<Color[]>filter(colors -> createRow(colors.length));

            @TestCase
            @TestData("quantities")
            public void test(int quantity) {
                assertTrue(quantity > 0);
            }
        }
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(new Test());
        Assert.assertTrue(s.isOK());
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }


    @Test
    public void test_03_array_01() {
        @TestGroup
        class Test {
            Values quantities =
                    createColumn(
                            new Color[]{BLUE},
                            new Color[]{BLACK},
                            new Color[]{BLUE, YELLOW},
                            new Color[]{RED}
                    ).filter((Color[] colors) -> createRow(colors.length));

            @TestCase
            @TestData("quantities")
            public void test(int quantity) {
                assertTrue(quantity > 0);
            }
        }
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(new Test());
        Assert.assertTrue(s.isOK());
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }

    @Test
    public void test_03_array_01_old_approach() {
        @TestGroup
        class Test {
            Values blues =
                    createColumn(
                            new Color[]{BLACK, BLACK, RED, ORANGE},
                            new Color[]{BLUE},
                            new Color[]{RED, WHITE}
                    ).filter(
                            new Object() {
                                @Transform
                                Values tr(Object[] colors) {
                                    return createRow(((Color) colors[0]).getBlue());
                                }
                            });

            @TestCase
            @TestData("blues")
            public void test(int blue) {
                assertTrue(blue >= 0);
            }
        }
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(new Test());
        Assert.assertTrue(s.isOK());
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }

    @Test
    public void test_03_array_01_old_approach_2() {
        @TestGroup
        class Test {
            Values blues =
                    createColumn(
                            new Color[]{BLACK, BLACK, RED, ORANGE},
                            new Color[]{BLUE},
                            new Color[]{RED, WHITE}
                    ).filter(
                            new Object() {
                                @Transform
                                Values t(Color[] colors) {
                                    return createRow(colors[0].getBlue());
                                }
                            });

            @TestCase
            @TestData("blues")
            public void test(int blue) {
                assertTrue(blue >= 0);
            }
        }
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(new Test());
        Assert.assertTrue(s.isOK());
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }

    @Test
    public void test_03_array_01_old_approach_2_01() {
        @TestGroup
        class Test {
            Values blues =
                    createColumn(
                            new Color[]{BLACK, BLACK, RED, ORANGE},
                            new Color[]{BLUE},
                            new Color[]{},
                            new Color[]{RED, WHITE}
                    ).filter(
                            new Object() {
                                @Transform
                                Values t(Color[] colors) {
                                    if (colors.length > 0) {
                                        return createRow(colors[0].getBlue());
                                    } else {
                                        return null;
                                    }
                                }
                            });

            @TestCase
            @TestData("blues")
            public void test(int blue) {
                assertTrue(blue >= 0);
            }
        }
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(new Test());
        Assert.assertTrue(s.isOK());
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }

    @Test
    public void test_03_array_01_old_approach_2_02() {
        @TestGroup
        class Test {
            Values blues =
                    createColumn(
                            new Color[]{BLACK, BLACK, RED, ORANGE},
                            new Color[]{BLUE},
                            new Color[]{},
                            new Color[]{RED, WHITE}
                    ).filter(
                            new Object() {
                                @Transform
                                Values t(Object[] colors) {
                                    if (colors.length > 0) {
                                        return createRow(((Color) colors[0]).getBlue());
                                    } else {
                                        return null;
                                    }
                                }
                            });

            @TestCase
            @TestData("blues")
            public void test(int blue) {
                assertTrue(blue >= 0);
            }
        }
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(new Test());
        Assert.assertTrue(s.isOK());
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }


    @Test
    public void test_03_array_01_old_approach_twoArrays() {
        @TestGroup
        class Test {
            Values mix =
                    createColumn(
                            new Color[]{BLACK, BLACK, RED, ORANGE},
                            new Color[]{BLUE},
                            new Color[]{BLUE},
                            new Color[]{RED, WHITE}
                    ).multiply(
                            new Color[]{BLACK, BLACK, RED, ORANGE},
                            new Color[]{BLUE},
                            new Color[]{BLUE},
                            new Color[]{RED, WHITE}
                    )
                            .filter(
                                    new Object() {
                                        @Transform
                                        Values t(Color[] c1s, Color[] c2s) {
                                            return createRow(c1s[0].getBlue() + c2s[0].getRed());
                                        }
                                    });

            @TestCase
            @TestData("mix")
            public void test(int mix) {
                assertTrue(mix >= 0);
            }
        }
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(new Test());
        Assert.assertTrue(s.isOK());
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }


    @Test
    public void test_03_array_01_new_approach_twoArrays() {

        Assert.assertTrue(Arrays.asList(1, 2, 3) instanceof RandomAccess);
        @TestGroup
        class Test {
            Values mix =
                    createColumn(
                            new Color[]{BLACK, BLACK, RED, ORANGE},
                            new Color[]{BLUE},
                            new Color[]{BLUE},
                            new Color[]{RED, WHITE}
                    ).multiply(
                            new Color[]{BLACK, BLACK, RED, ORANGE},
                            new Color[]{BLUE},
                            new Color[]{BLUE},
                            new Color[]{RED, WHITE}
                    )
                            .filter((Color[] c1s, Color[] c2s) -> createRow(c1s[0].getBlue() + c2s[0].getRed()));

            @TestCase
            @TestData("mix")
            public void test(int mix) {
                assertTrue(mix >= 0);
            }
        }
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(new Test());
        Assert.assertTrue(s.isOK());
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }

    @Test
    public void test_04() {
        @TestGroup
        class Test {

            List<Integer> component =
                    IntStream.range(55, 99).boxed().collect(toList());

            int counter = 0;

            @TestCase
            @Operation(MULTIPLY)
            public void test(@TestData("component") int r,
                             @TestData("component") int g,
                             @TestData("component") int b) {
                Color color = new Color(r, g, b);
                assertEquals(r, color.getRed());
                assertEquals(g, color.getGreen());
                assertEquals(b, color.getBlue());
                counter++;
            }
        }
        Test tg = new Test();
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(tg);
        Assert.assertTrue(s.isOK());
        Assert.assertEquals(85184, tg.counter);
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }

    @Test
    public void test_05() {
        @TestGroup
        class TestTransformingIterator {

            Values multiplyValues = DataFactory.createValues(new Comparable[][]{{1, 2}, {3, 4}})
                    .multiply(DataFactory.createValues(new Comparable[][]{{5, 6}, {7, 8}}))
                    .filter((Object[] i) -> DataFactory.createRow(i));


            @TestCase
            @TestData("multiplyValues")
            public void testToString(Comparable a, Comparable b, Comparable c, Comparable d) {

                //Control won't come here, it fails at TransformingIterator.
            }
        }
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(new TestTransformingIterator());
        Assert.assertTrue(s.isOK());
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }

    @Test
    public void test_05_01() {
        @TestGroup
        class TestTransformingIterator {

            Values multiplyValues = DataFactory.createValues(new Comparable[][]{{1, 2}, {3, 4}})
                    .multiply(DataFactory.createValues(new Comparable[][]{{5, 6}, {7, 8}}))
                    .filter((Object[] i) -> DataFactory.createRow(i));


            @TestCase
            @TestData("multiplyValues")
            public void testToString(Comparable a, Comparable b, Comparable c, Comparable d) {
                System.out.println("TestTransformingIterator.testToString");
            }
        }
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(new TestTransformingIterator());
        Assert.assertTrue(s.isOK());
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }

    @Test
    public void test_05_02() {
        @TestGroup
        class TestTransformingIterator {

            Values multiplyValues = DataFactory.createValues(new Comparable[][]{{1}, {3}})
                    .filter((Object[] i) -> DataFactory.createRow(i.length));


            @TestCase
            @TestData("multiplyValues")
            public void testToString(Integer d) {
            }
        }
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(new TestTransformingIterator());
        Assert.assertTrue(s.isOK());
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }

    @Test
    public void test_05_02_04() {
        @TestGroup
        class TestTransformingIterator {

            Values multiplyValues = DataFactory.createValues(new Comparable[][]{{1}, {3}})
                    .filter((Object[] i) -> DataFactory.createRow(i));


            @TestCase
            @TestData("multiplyValues")
            public void testToString(Integer d) {
            }
        }
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(new TestTransformingIterator());
        Assert.assertTrue(s.isOK());
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }

    @Test
    public void test_05_02_01() {
        @TestGroup
        class TestTransformingIterator {

            Values multiplyValues = DataFactory.createValues(new Comparable[][]{{1}, {3, 4}})
                    .filter((Object[] i) -> DataFactory.createRow(i.length));


            @TestCase
            @TestData("multiplyValues")
            public void testToString(Integer d) {
            }
        }
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(new TestTransformingIterator());
        Assert.assertTrue(s.isOK());
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }

    @Test
    public void test_05_02_02() {
        @TestGroup
        class TestTransformingIterator {

            Values multiplyValues = DataFactory.createValues(new Comparable[][]{{1, 5}, {3, 4}})
                    .filter((Object[] i) -> DataFactory.createRow(i));


            @TestCase
            @TestData("multiplyValues")
            public void testToString(Integer d, Integer d1) {
            }
        }
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(new TestTransformingIterator());
        Assert.assertTrue(s.isOK());
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }

    @Test
    public void test_05_03() {
        @TestGroup
        class TestTransformingIterator {

            Values multiplyValues = DataFactory.createValues(new Comparable[][]{{3}, {3, 4}})
                    .filter((Object i) -> {
                        return DataFactory.createRow(i.toString().length());
                    });


            @TestCase
            @TestData("multiplyValues")
            public void testToString(Integer i) {
            }
        }
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(new TestTransformingIterator());
        Assert.assertTrue(s.isOK());
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }

    @Test
    public void test_05_04() {
        @TestGroup
        class TestTransformingIterator {

            Values multiplyValues = DataFactory.createValues(new Comparable[][]{{1}, {3}})
                    .filter((Comparable i) -> DataFactory.createRow(i));


            @TestCase
            @TestData("multiplyValues")
            public void testToString(Integer d) {
            }
        }
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(new TestTransformingIterator());
        Assert.assertTrue(s.isOK());
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }

    @Test(expected = java.lang.RuntimeException.class)
    // TODO rethink TransformingIterator design to make this possible
    public void test_06() {
        @TestGroup
        class TestTransformingIterator {

            Values multiplyValues = DataFactory.createValues(new Comparable[][]{{1, 2}, {3, 4}})
                    .multiply(DataFactory.createValues(new Comparable[][]{{5, 6}, {7, 8}}))
                    .filter((Comparable... i) -> DataFactory.createRow(i));


            @TestCase
            @TestData("multiplyValues")
            public void testToString(Comparable a) {

                //Control won't come here, it fails at TransformingIterator.
            }
        }
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(new TestTransformingIterator());
        Assert.assertFalse(s.isOK());

    }


    @Test
    public void test_07() {
        @TestGroup
        class TestTransformingIterator {

            Values multiplyValues = DataFactory.createValues(new Comparable[][]{{1, 2}, {3, 4}})
                    .multiply(DataFactory.createValues(new Comparable[][]{{5, 6}, {7, 8}}))
                    .filter((Comparable c1, Comparable c2, Comparable c3, Comparable c4) -> DataFactory.createRow(c1));


            @TestCase
            @TestData("multiplyValues")
            public void testToString(Integer i) {
            }
        }
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(new TestTransformingIterator());
        Assert.assertTrue(s.isOK());
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }


    @Test
    public void test_08() {
        @TestGroup
        class TestTransformingIterator {

            Values multiplyValues = DataFactory.createValues(new Comparable[][]{{1, 2}, {3, 4}})
                    .filter((Object[] objectsToUse) -> DataFactory.createRow(objectsToUse));

            @TestCase
            @TestData("multiplyValues")
            public void testToString(Comparable a, Comparable b) {
            }
        }
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(new TestTransformingIterator());
        Assert.assertTrue(s.isOK());
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }

    @Test
    public void test_09() {
        @TestGroup
        class TestTransformingIterator {

            Values multiplyValues = DataFactory.createValues(new Comparable[][]{{1, 2}, {3, 4}})
                    .filter((Comparable[] objectsToUse) -> DataFactory.createRow(objectsToUse));

            @TestCase
            @TestData("multiplyValues")
            public void testToString(Comparable a, Comparable b) {
            }
        }
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(new TestTransformingIterator());
        Assert.assertTrue(s.isOK());
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }

    @Test
    public void test_10() {
        @TestGroup
        class TestTransformingIterator {

            int calls;
            Values multiplyValues = DataFactory.createValues(new Comparable[][]{{}, {3, 4}})
                    .filter((Comparable[] objectsToUse) -> {
                        calls++;
                        return DataFactory.createRow((Object[]) objectsToUse);
                    });

            @TestCase
            @TestData("multiplyValues")
            public void testToString(Comparable a, Comparable b) {
                System.out.println("TestTransformingIterator.testToString");
            }
        }
        TestTransformingIterator tg = new TestTransformingIterator();
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(tg);
        Assert.assertTrue(s.isOK());
        Assert.assertEquals(2, tg.calls);
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }

    @Test
    public void test_10_01() {
        @TestGroup
        class TestTransformingIterator {

            int calls;
            Values multiplyValues = DataFactory.createValues(new Comparable[][]{{}, {3, 4}})
                    .filter((Object[] objectsToUse) -> {
                        calls++;
                        return DataFactory.createRow((Object[]) objectsToUse);
                    });

            @TestCase
            @TestData("multiplyValues")
            public void testToString(Comparable a, Comparable b) {
                System.out.println("TestTransformingIterator.testToString");
            }
        }
        TestTransformingIterator tg = new TestTransformingIterator();
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(tg);
        Assert.assertTrue(s.isOK());
        Assert.assertEquals(2, tg.calls);
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }

    @Test
    public void test_11() {
        @TestGroup
        class TestTransformingIterator {
            int calls;
            Values multiplyValues = DataFactory.createValues(new Comparable[][]{{}, {3, 4}})
                    .filter((Object objectsToUse) -> {
                        calls++;
                        return DataFactory.createRow(objectsToUse);
                    });

            @TestCase
            @TestData("multiplyValues")
            public void testToString(Object a) {
                System.out.println("TestTransformingIterator.testToString");
            }
        }
        TestTransformingIterator tg = new TestTransformingIterator();
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(tg);
        Assert.assertTrue(s.isOK());
        Assert.assertEquals(2, tg.calls);
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }

    @Test
    public void from_VMTests() {

        @TestGroup
        class TestTransformingIterator {

            Values frames() {
                Values rowsOfTypes = DataFactory.createValues(new String[][]{{}, {"a"}, {"a", "b"}, {"a", "b", "c", "d"}});

                Values frames = rowsOfTypes
                        .filter(new Object() {
                            @Transform
                            public Object transform(Object[] row1) {
                                Object[] copiedArray = Arrays.copyOf(row1, row1.length, (Class<? extends Object[]>) String[].class);
                                return DataFactory.createRow((Object) copiedArray);
                            }
                        })
                        .filter((String[] stackState) -> {
                            return new Object();
                        });
                return frames;
            }

            @TestCase
            @TestData("frames")
            public void testToString(Object i) {
                System.out.println("TestTransformingIterator.testToString");
                System.out.println("i = " + i);

            }
        }
        com.oracle.tck.lib.autd2.TestResult s = TU.runTestGroup(new TestTransformingIterator());
        Assert.assertTrue(s.isOK());
        Assert.assertEquals("test cases: 1; all passed", s.getMessage());
    }


}
