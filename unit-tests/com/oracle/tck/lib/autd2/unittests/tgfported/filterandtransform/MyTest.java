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
package com.oracle.tck.lib.autd2.unittests.tgfported.filterandtransform;

import com.oracle.tck.lib.autd2.unittests.BaseTestGroup;
import com.oracle.tck.lib.autd2.unittests.TU;
import com.oracle.tck.lib.autd2.unittests.TestObject;
import com.oracle.tck.lib.autd2.unittests.ValuesComparison;
import com.sun.tck.lib.tgf.*;
import com.sun.tck.test.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.sun.tck.lib.tgf.DataFactory.*;
import static com.sun.tck.lib.tgf.DataFactory.createColumn;

/**
 *
 */
public class MyTest {

    @Test
    public void test_1() {

        final Values result =
                createColumn("1", "2", "3").filter(
                new Object() {
                    public @Transform
                    Values build(String s) {
                        return createColumn(
                                s + " one",
                                s + " two",
                                s + " three"
                        );
                    }
                }
        );

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{"1 one"});
        expected.add(new Object[]{"1 two"});
        expected.add(new Object[]{"1 three"});
        expected.add(new Object[]{"2 one"});
        expected.add(new Object[]{"2 two"});
        expected.add(new Object[]{"2 three"});
        expected.add(new Object[]{"3 one"});
        expected.add(new Object[]{"3 two"});
        expected.add(new Object[]{"3 three"});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void test_2() {

        final Values result =
                createColumn("1", "2", "3").filter(
                new Object() {
                    @Transform public Values someStrangeName(String s) {
                        return createColumn(
                                s + " one",
                                s + " two",
                                s + " three"
                        );
                    }
                }
        );

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{"1 one"});
        expected.add(new Object[]{"1 two"});
        expected.add(new Object[]{"1 three"});
        expected.add(new Object[]{"2 one"});
        expected.add(new Object[]{"2 two"});
        expected.add(new Object[]{"2 three"});
        expected.add(new Object[]{"3 one"});
        expected.add(new Object[]{"3 two"});
        expected.add(new Object[]{"3 three"});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void test_3() {

        final Values result =
                createColumn("1", "2").multiply("a", "b").filter(
                new Object() {
                    @Transform public Values nameDoesntMatter(String s1, String s2) {
                        return createColumn(
                                s1 + s2
                        );
                    }
                }
        );

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{"1a"});
        expected.add(new Object[]{"1b"});
        expected.add(new Object[]{"2a"});
        expected.add(new Object[]{"2b"});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void test_4() {

        final Values result =
                createColumn(12, 14).pseudoMultiply(2, 3).filter(
                new Object() {
                    @Transform public Values nameDoesntMatter(int s1, int s2) {
                        return createColumn(
                                s1 + s2
                        );
                    }
                }
        );

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{14});
        expected.add(new Object[]{17});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void test_oneValue() {

        final Values result =
                createColumn(1).filter(
                new Object() {
                    @Transform public Values someName0123809(int s1) {
                        return createColumn(
                                s1
                        );
                    }
                }
        );

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{1});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void test_self_1() {
        final Values ab = createColumn("a", "b");
        final Values result = ab.filter(
                new Object() {
                    @Transform public Values someName0123809(String s1) {
                        return ab;
                    }
                }
        );
        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{"a"});
        expected.add(new Object[]{"b"});
        expected.add(new Object[]{"a"});
        expected.add(new Object[]{"b"});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void test_color() {
        Integer[] component = {0, 100, 255};
        final Values colors =
            createColumn(component).multiply(component).multiply(component).filter(
                    (Integer r, Integer g, Integer b) -> new Color(r, g, b)
            );

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{new Color(0,0,0)});
        expected.add(new Object[]{new Color(0,0,100)});
        expected.add(new Object[]{new Color(0,0,255)});
        expected.add(new Object[]{new Color(0,100,0)});
        expected.add(new Object[]{new Color(0,100,100)});
        expected.add(new Object[]{new Color(0,100,255)});
        expected.add(new Object[]{new Color(0,255,0)});
        expected.add(new Object[]{new Color(0,255,100)});
        expected.add(new Object[]{new Color(0,255,255)});
        expected.add(new Object[]{new Color(100,0,0)});
        expected.add(new Object[]{new Color(100,0,100)});
        expected.add(new Object[]{new Color(100,0,255)});
        expected.add(new Object[]{new Color(100,100,0)});
        expected.add(new Object[]{new Color(100,100,100)});
        expected.add(new Object[]{new Color(100,100,255)});
        expected.add(new Object[]{new Color(100,255,0)});
        expected.add(new Object[]{new Color(100,255,100)});
        expected.add(new Object[]{new Color(100,255,255)});
        expected.add(new Object[]{new Color(255,0,0)});
        expected.add(new Object[]{new Color(255,0,100)});
        expected.add(new Object[]{new Color(255,0,255)});
        expected.add(new Object[]{new Color(255,100,0)});
        expected.add(new Object[]{new Color(255,100,100)});
        expected.add(new Object[]{new Color(255,100,255)});
        expected.add(new Object[]{new Color(255,255,0)});
        expected.add(new Object[]{new Color(255,255,100)});
        expected.add(new Object[]{new Color(255,255,255)});
        ValuesComparison.compare(colors, expected);
    }

    @Test
    public void test_2ints() {
        final Values result = createColumn(1, 2).pseudoMultiply(2, 3).filter(
                new Object() {
                    @Transform
                    String someMethod(int i, int j) {
                        return "1";
                    }
                }
        );

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{"1"});
        expected.add(new Object[]{"1"});
        ValuesComparison.compare(result, expected);
    }


    @Test
    public void test_self_2() {
        final Values ab = createColumn("a", "b");
        final Values result = ab.filter(
                new Object() {
                    @Transform public Values someName0123809(String s1) {
                        return ab.filter( new Object() {
                            @Transform private Values someNameAgain(String s1) {
                                return ab;
                            }
                        }
                        );
                    }
                }
        );
        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{"a"});
        expected.add(new Object[]{"b"});
        expected.add(new Object[]{"a"});
        expected.add(new Object[]{"b"});
        expected.add(new Object[]{"a"});
        expected.add(new Object[]{"b"});
        expected.add(new Object[]{"a"});
        expected.add(new Object[]{"b"});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void test_self_3() {
        final Values ab = createColumn("a", "b");
        final Values result = ab.filter(
                new Object() {
                    @Transform public Values someName0123809(String s1) {
                        return ab.filter( new Object() {
                            @Transform private Values someNameAgain(String s1) {
                                return ab.filter( new Object() {
                                    @Transform private Values metthod(String s) {
                                        return ab;
                                    }
                                }
                                );
                            }
                        }
                        );
                    }
                }
        );
        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{"a"});
        expected.add(new Object[]{"b"});
        expected.add(new Object[]{"a"});
        expected.add(new Object[]{"b"});
        expected.add(new Object[]{"a"});
        expected.add(new Object[]{"b"});
        expected.add(new Object[]{"a"});
        expected.add(new Object[]{"b"});
        expected.add(new Object[]{"a"});
        expected.add(new Object[]{"b"});
        expected.add(new Object[]{"a"});
        expected.add(new Object[]{"b"});
        expected.add(new Object[]{"a"});
        expected.add(new Object[]{"b"});
        expected.add(new Object[]{"a"});
        expected.add(new Object[]{"b"});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void test_self_4() {
        final Values ab = createColumn("a", "b");
        final Values cd = createColumn("c", "d");
        final Values result =
                ab.multiply(cd).filter(
                new Object() {
                    @Transform public Values someName0123809(String s1, String s2) {
                        return createColumn(s1, s2).multiply(cd);
                    }
                }
        );
        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{"a", "c"});
        expected.add(new Object[]{"a", "d"});
        expected.add(new Object[]{"c", "c"});
        expected.add(new Object[]{"c", "d"});

        expected.add(new Object[]{"a", "c"});
        expected.add(new Object[]{"a", "d"});
        expected.add(new Object[]{"d", "c"});
        expected.add(new Object[]{"d", "d"});

        expected.add(new Object[]{"b", "c"});
        expected.add(new Object[]{"b", "d"});
        expected.add(new Object[]{"c", "c"});
        expected.add(new Object[]{"c", "d"});

        expected.add(new Object[]{"b", "c"});
        expected.add(new Object[]{"b", "d"});
        expected.add(new Object[]{"d", "c"});
        expected.add(new Object[]{"d", "d"});

        ValuesComparison.compare(result, expected);
    }


    @Test(expected = RuntimeException.class)
    public void test_noFilteringMetod_1() {

        final Values result = createColumn(
                createColumn(12, 14).pseudoMultiply(2, 3)).filter(
                new Object() {
                }
        );
        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        ValuesComparison.compare(result, expected);
    }

    @Test(expected = RuntimeException.class)
    public void test_wrongArgTypes() {
                createColumn(1, 2).pseudoMultiply(2, 3).filter(
                        new Object() {
                            Values someMethod(String s, int i) {
                                return createColumn("1", "2");
                            }
                        }
                );
    }

    @Test
    public void test_wrongNumberOfArgs_messageChecking() {
        try {
            final Values filter = createColumn(1, 2).pseudoMultiply(2, 3).filter(
                    new Object() {
                        @Transform
                        Values someMethod_1(String s, int i, double x) {
                            return createColumn("1", "2");
                        }
                    }
            );

            // we have to iterate to start inner machinery

            for (Object[] objects : filter) {      }

            Assert.fail("Exception not thrown");
        } catch (RuntimeException e) {
            Assert.assertEquals(
                    "wrong number of arguments",
                    e.getMessage());
        }
    }

//    @Test
//    public void test_wrongNumberOfArgs_messageChecking_lambda() {
//        try {
//                createColumn(1, 2).pseudoMultiply(2, 3).filter( (String s, Integer i, Double x) -> createColumn("1", "2")
//            );
//            Assert.fail("Exception not thrown");
//        } catch (RuntimeException e) {
//            Assert.assertTrue(
//                    "thrown when tried to invoke method \"transform\": java.lang.IllegalArgumentException: wrong number of arguments".equals(e.getMessage())
//                    ||
//                   "thrown when tried to invoke method \"transform\": java.lang.IllegalArgumentException".equals(e.getMessage())
//                    );
//        }
//    }

    @Test
    public void test_wrongNumberOfArgs_safetyWorkaround() {
        createColumn(1, 2).pseudoMultiply(2, 3).filter(
                new Object() {
                    @Transform
                    Values someMethod_1(Object... args) {
                        if (args.length != 3) {
                            return null;
                        }
                        String s = (String) args[0];
                        int i = (Integer) args[1];
                        double x = (Double) args[2];
                        return createColumn("1", "2");
                    }
                }
        );
    }

    @Test
    public void test_dataFilteredOutCompletely_NotAppEx_messageChecking() {

        final ArrayList<String> arrayList = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            Values setupOne() {
                return createColumn(1, 2).
                        filter(new Object() {
                            @Transform
                            String filterOut(int i) {
                                return null;
                            }
                        }).filter(new Object() {
                    @Transform String end(String s) {
                        return "abc";
                    }
                });
            }

            @TestCase
            @TestData("setupOne")
            public void myTest(String s) {
                arrayList.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertEquals(0, arrayList.size());
        Assert.assertTrue(status.isOK());
        Assert.assertEquals("Passed. test cases: 1; all passed", status.toString());

    }

    @Test
    public void test_dataFilteredOutCompletely_SafetyCheck() {
                createColumn(1, 2).
                        filter(new Object() {
                            @Transform
                            String filterOut(int i) {
                                return null;
                            }
                        }).filter(new Object() {
                    @Transform
                    String end(Object... args) {
                        if (args.length != 1) {
                            return null;
                        }
                        String s = (String) args[1];
                        return "abc";
                    }
                });
    }

    @Test
    public void test_dataFilteredOutPartly_NotAppEx_messageChecking() {
                createColumn(1, 2).
                        filter(new Object() {
                            @Transform
                            String method(int i) {
                                return "a";
                            }
                        }).multiply()
                        .filter(new Object() {
                            @Transform
                            String end(String s, int i) {
                                return null;
                            }
                        });
    };

    @Test
    public void test_dataFilteredOutPartly_safetyCheck() {
        createColumn(1, 2).
                filter(new Object() {
                    @Transform
                    String method(int i) {
                        return "a";
                    }
                }).multiply()
                .filter(new Object() {
                    @Transform
                    String end(Object... args) {
                        if (args.length != 2) {
                            return null;
                        }
                        String s = (String) args[0];
                        int i = (Integer) args[1];
                        return "sjkg";
                    }
                });
    }

    @Test
    public void testRun_DataFilteredOutCompletely() {
        final ArrayList<String> arrayList = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            Values setupOne() {
                return createColumn(1, 2).
                        filter(new Object() {
                            @Transform
                            String filterOut(int i) {
                                return "a";
                            }
                        }).multiply().filter(new Object() {
                            @Transform
                            String end(String s) {
                                return null;
                            }
                        });
            }

            @TestCase
            @TestData("setupOne")
            public void myTest(String s) {
                arrayList.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertEquals(0, arrayList.size());
        Assert.assertTrue(status.isOK());
        Assert.assertEquals("Passed. test cases: 1; all passed", status.toString());
    }

    @Test
    public void testRun_DataFilteredOutCompletely2() {
        final ArrayList<String> arrayList = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            Values setupOne() {
                return createColumn(1, 2).
                        filter(new Object() {
                            @Transform
                            String filterOut(int i) {
                                return null;
                            }
                        });
            }

            @TestCase
            @TestData("setupOne")
            public void myTest(String s) {
                arrayList.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertEquals(0, arrayList.size());
        Assert.assertTrue(status.isOK());
        Assert.assertEquals("Passed. test cases: 1; all passed", status.toString());
    }


    @Test
    public void testRun_DataFilteredOutCompletely3() {
        final ArrayList<String> arrayList = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            Values setupOne() {
                return createColumn(1, 2).
                        filter(new Object() {
                            @Transform
                            String filterOut(int i) {
                                return null;
                            }
                        }).multiply(1, 2, 3);
            }

            @TestCase
            @TestData("setupOne")
            public void myTest(String s) {
                arrayList.add(s);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertEquals(0, arrayList.size());
        Assert.assertTrue(status.isOK());
        Assert.assertEquals("Passed. test cases: 1; all passed", status.toString());
    }


    @Test
    public void testRun_DataOK() {
        final ArrayList<String> arrayList = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            Values setupOne() {
                return createColumn(1, 2).
                        filter(new Object() {
                            @Transform
                            String filterOut(int i) {
                                return "a";
                            }
                        }).multiply(createColumn("a", "b", "d").filter(new Object() {
                    @Transform
                    String end(String s) {
                        return s;
                    }
                }));

            }

            @TestCase
            @TestData("setupOne")
            public void myTest(String s1, String s2) {
                arrayList.add(s1);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertEquals(6, arrayList.size());
        Assert.assertTrue(status.isOK());
        Assert.assertEquals("Passed. test cases: 1; all passed", status.toString());
    }

    @Test
    public void testRun_DataOK_safetyFilterAtTheEnd() {
        final ArrayList<String> arrayList = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            Values setupOne() {
                return createColumn(1, 2).
                        filter(new Object() {
                            @Transform
                            String filterOut(int i) {
                                return "a";
                            }
                        }).multiply(createColumn("a", "b", "d").filter(new Object() {
                    @Transform
                    String end(String s) {
                        return s;
                    }
                })).filter(new Object() {
                    // if data construction failed, this prefents test failing
                    // instead of it test is skipped
                    @Transform
                    Values safetyFilter(String s1, String s2) {
                        return createRow(s1, s2);
                    }
                });

            }

            @TestCase
            @TestData("setupOne")
            public void myTest(String s1, String s2) {
                arrayList.add(s1);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertEquals(6, arrayList.size());
        Assert.assertTrue(status.isOK());
        Assert.assertEquals("Passed. test cases: 1; all passed", status.toString());
    }

    @Test
    public void testRun_Data_partlyFilteredInTheEnd() {
        final ArrayList<String> arrayList = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            Values setupOne() {
                return createColumn(1, 2).
                        filter(new Object() {
                            @Transform
                            String filterOut(int i) {
                                return "a";
                            }
                        }).multiply(createColumn("a", "b", "d").filter(new Object() {
                    @Transform
                    String end(String s) {
                        return null;
                    }
                }));

            }

            @TestCase
            @TestData("setupOne")
            public void myTest(String s1, String s2, String s3, String s4) {
                arrayList.add(s1);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertEquals(0, arrayList.size());
        Assert.assertTrue(status.isOK());
        Assert.assertEquals("Passed. test cases: 1; all passed", status.toString());
    }

    @Test
    public void testRun_Data_partlyFilteredInTheEnd_safetyFilter() {
        final ArrayList<String> arrayList = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            Values setupOne() {
                return createColumn(1, 2).
                        filter(new Object() {
                            @Transform
                            String filterOut(int i) {
                                return "a";
                            }
                        }).multiply(createColumn("a", "b", "d").filter(new Object() {
                    @Transform
                    String end(String s) {
                        return null;
                    }
                })).filter(new Object() {
                    // if data construction failed, this prefents test failing
                    // instead of it test is skipped
                    @Transform
                    Values safetyFilter(Object... args) {
                        if (args.length != 2) {
                            // just skipping
                            return null;
                        }
                        String s1 = (String) args[0], s2 = (String) args[1];

                        return createRow(s1, s2);
                    }
                });
            }

            @TestCase
            @TestData("setupOne")
            public void myTest(String s1, String s2) {
                arrayList.add(s1);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertEquals(0, arrayList.size());
        Assert.assertTrue(status.isOK());
        Assert.assertEquals("Passed. test cases: 1; all passed", status.toString());
    }



    @Test
    public void test_noFilteringMetod_2() {

        try {
            final Values result = createColumn(
                    createColumn(1)).filter(
                    new Object() {
                    }
            );
            Assert.fail("Exception not thrown");
        } catch (RuntimeException e) {
            Assert.assertEquals(
                    "No method annotated with @Transform was found. Unable to create filter",
                    e.getMessage());
        }

    }

    @Test
    public void test_noFilteringMetod_3() {

        try {
            final Values result = createColumn(
                    createColumn(1)).filter(
                    new Object() {
                        Values someMethod_1(int i) {
                            return createColumn("1", "2");
                        }
                    }
            );
            Assert.fail("Exception not thrown");
        } catch (RuntimeException e) {
            Assert.assertEquals(
                    "No method annotated with @Transform was found. Unable to create filter",
                    e.getMessage());
        }
    }

    @Test
    public void test_noFilteringMetod_4() {

        try {
            final Values result = createColumn(
                    createColumn(1)).filter(
                    new Object() {
                        String someMethod_1(int i) {
                            return Integer.toString(i);
                        }
                    }
            );
            Assert.fail("Exception not thrown");
        } catch (RuntimeException e) {
            Assert.assertEquals(
                    "No method annotated with @Transform was found. Unable to create filter",
                    e.getMessage());
        }
    }


    @Test
    public void notValuesReturned_1() {

        final Values result =
                createColumn("1", "2", "3").filter(
                new Object() {
                    public @Transform String build(String s) {
                        return s + "suffix";
                    }
                }
        );

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{"1suffix"});
        expected.add(new Object[] { "2suffix" });
        expected.add(new Object[] { "3suffix" });
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void notValuesReturned_2() {
        final Values result =
                createColumn("1", "2", "3").filter(
                new Object() {
                    public @Transform
                    TestObject build(String s) {
                        return new TestObject(s);
                    }
                }
        );
        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{new TestObject("1")});
        expected.add(new Object[] { new TestObject("2") });
        expected.add(new Object[] { new TestObject("3") });
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void wrappingOneDimensionalArray_1() {
        final Values result =
                createColumn("1", "2", "3").filter(
                new Object() {
                    public @Transform
                    String[] build(String s) {
                        return new String[] {s + "one", s + "two", s + "three"};
                    }
                }
        );
        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new String[]{"1" + "one"});
        expected.add(new String[]{"1" + "two"});
        expected.add(new String[]{"1" + "three"});
        expected.add(new String[]{"2" + "one"});
        expected.add(new String[]{"2" + "two"});
        expected.add(new String[]{"2" + "three"});
        expected.add(new String[]{"3" + "one"});
        expected.add(new String[] { "3" + "two" });
        expected.add(new String[] { "3" + "three" });
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void wrappingList_1() {
        final Values result =
                createColumn("1", "2", "3").filter(
                new Object() {
                    public @Transform
                    List build(String s) {
                        return Arrays.asList(s + "one", s + "two", s + "three_");
                    }
                }
        );
        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new String[]{"1" + "one"});
        expected.add(new String[]{"1" + "two"});
        expected.add(new String[]{"1" + "three_"});
        expected.add(new String[]{"2" + "one"});
        expected.add(new String[]{"2" + "two"});
        expected.add(new String[]{"2" + "three_"});
        expected.add(new String[]{"3" + "one"});
        expected.add(new String[] { "3" + "two" });
        expected.add(new String[] { "3" + "three_" });
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void wrappingIterator_1() {
        final Values result =
                createColumn("1", "2", "3").filter(
                new Object() {
                    public @Transform
                    Iterator build(String s) {
                        return Arrays.asList(s + "one", s + "two", s + "three_").iterator();
                    }
                }
        );
        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new String[]{"1" + "one"});
        expected.add(new String[]{"1" + "two"});
        expected.add(new String[]{"1" + "three_"});
        expected.add(new String[]{"2" + "one"});
        expected.add(new String[]{"2" + "two"});
        expected.add(new String[]{"2" + "three_"});
        expected.add(new String[]{"3" + "one"});
        expected.add(new String[] { "3" + "two" });
        expected.add(new String[] { "3" + "three_" });
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void wrappingOneDimensionalArray_2() {
        final Values result =
                createColumn("1", "2", "3").filter(
                new Object() {
                    public @Transform
                    TestObject[] build(String s) {
                        return new TestObject[] {
                                new TestObject(s),
                                new TestObject(s+s),
                                new TestObject(s+s+s)

                        };
                    }
                }
        );
        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{new TestObject("1")});
        expected.add(new Object[]{new TestObject("11")});
        expected.add(new Object[]{new TestObject("111")});
        expected.add(new Object[]{new TestObject("2")});
        expected.add(new Object[]{new TestObject("22")});
        expected.add(new Object[]{new TestObject("222")});
        expected.add(new Object[]{new TestObject("3")});
        expected.add(new Object[]{new TestObject("33")});
        expected.add(new Object[]{new TestObject("333")});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void multiplyingByOne() {
        final Values result =
                createColumn("1", "2", "3").multiply(1).filter((String s, Integer i) -> {
                            System.out.println("s = " + s);
                            System.out.println("i = " + i);
                    return createRow(s, i);
                });
        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{"1", 1});
        expected.add(new Object[]{"2", 1});
        expected.add(new Object[]{"3", 1});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void multiplyingByOne_noIteration() {
        AtomicBoolean methodCalled = new AtomicBoolean();
        final Values result =
                createColumn("1", "2", "3").multiply(1).filter((String s, Integer i) -> {
                    methodCalled.set(true);
                    return createRow(s, i);
                });

        Assert.assertFalse(methodCalled.get());
    }

    @Test
    public void multiplyingByOne_iteration_toString() {
        AtomicBoolean methodCalled = new AtomicBoolean();
        final Values result =
                createColumn("1", "2", "3").multiply(1).filter((String s, Integer i) -> {
                    methodCalled.set(true);
                    return createRow(s, i);
                });

        System.out.println("result = " + result);
        Assert.assertTrue(methodCalled.get());
    }

    @Test
    public void wrappingList_2() {
        final Values result =
                createColumn("1", "2", "3").filter(
                new Object() {
                    public @Transform
                    List<TestObject> build(String s) {
                        return Arrays.asList(
                                new TestObject(s),
                                new TestObject(s+s),
                                new TestObject(s+s+s)
                        );
                    }
                }
        );
        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{new TestObject("1")});
        expected.add(new Object[]{new TestObject("11")});
        expected.add(new Object[]{new TestObject("111")});
        expected.add(new Object[]{new TestObject("2")});
        expected.add(new Object[]{new TestObject("22")});
        expected.add(new Object[]{new TestObject("222")});
        expected.add(new Object[]{new TestObject("3")});
        expected.add(new Object[]{new TestObject("33")});
        expected.add(new Object[]{new TestObject("333")});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void wrappingIterator_2() {
        final Values result =
                createColumn("1", "2", "3").filter(
                new Object() {
                    public @Transform
                    Iterator<TestObject> build(String s) {
                        return Arrays.asList(
                                new TestObject(s),
                                new TestObject(s+s),
                                new TestObject(s+s+s)
                        ).iterator();
                    }
                }
        );
        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{new TestObject("1")});
        expected.add(new Object[]{new TestObject("11")});
        expected.add(new Object[]{new TestObject("111")});
        expected.add(new Object[]{new TestObject("2")});
        expected.add(new Object[]{new TestObject("22")});
        expected.add(new Object[]{new TestObject("222")});
        expected.add(new Object[]{new TestObject("3")});
        expected.add(new Object[]{new TestObject("33")});
        expected.add(new Object[]{new TestObject("333")});
        ValuesComparison.compare(result, expected);
    }


    @Test
    public void wrappingTwoDimensionalArray_1() {
        final Values result =
                createColumn("1", "2").filter(
                new Object() {
                    public @Transform
                    String[][] build(String s) {
                        return new String[][] {
                                {s, s, s+"last"},
                                {s+s, s+s, s+"last"},
                                {s+s+s, s+s+s, s+"last"}
                        };
                    }
                }
        );
        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add( new Object[]{"1", "1", "1last"});
        expected.add( new Object[]{"11", "11", "1last"});
        expected.add( new Object[]{"111", "111", "1last"});
        expected.add( new Object[]{"2", "2", "2last"});
        expected.add( new Object[]{"22", "22", "2last"});
        expected.add( new Object[]{"222", "222", "2last"});

        ValuesComparison.compare(result, expected);
    }

    @Test
    public void wrappingIteratorOfArrays_1() {
        final Values result =
                createColumn("1", "2").filter(
                new Object() {
                    public @Transform
                    Iterator<Object[]> build(String s) {
                        return Arrays.<Object[]>asList(
                                new String[] {s, s, s+"last"},
                                new String[]{s+s, s+s, s+"last"},
                                new String[] {s+s+s, s+s+s, s+"last"}).iterator();
                    }
                }
        );
        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add( new Object[]{"1", "1", "1last"});
        expected.add( new Object[]{"11", "11", "1last"});
        expected.add( new Object[]{"111", "111", "1last"});
        expected.add( new Object[]{"2", "2", "2last"});
        expected.add( new Object[]{"22", "22", "2last"});
        expected.add( new Object[]{"222", "222", "2last"});

        ValuesComparison.compare(result, expected);
    }

    @Test
    public void wrappingTwoDimensionalArray_2() {
        final Values result = createColumn("a", "b", "c").filter(
                new Object() {
                    public @Transform
                    TestObject[][] build(String s) {
                        return new TestObject[][] {
                                { new TestObject(s+1), new TestObject(s+2) },
                                { new TestObject(s+3), new TestObject(s+4) },
                        };
                    }
                }
        );
        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[] {new TestObject("a1"), new TestObject("a2")});
        expected.add(new Object[] {new TestObject("a3"), new TestObject("a4")});
        expected.add(new Object[] {new TestObject("b1"), new TestObject("b2")});
        expected.add(new Object[] {new TestObject("b3"), new TestObject("b4")});
        expected.add(new Object[] {new TestObject("c1"), new TestObject("c2")});
        expected.add(new Object[] {new TestObject("c3"), new TestObject("c4")});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void checkOneCallPerRow_1() {
        final int[] intHolder = { 0 };
        createColumn(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .filter((Integer i) -> {
                    intHolder[0] += 1;
                    return i;
                })
                .forEach(row -> {
                });

        Assert.assertEquals(intHolder[0], 10);
    }

    @Test
    public void checkOneCallPerRow_2() {
        final int[] intHolder = { 0 };
        createColumn(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .filter((Integer i) -> {
                    intHolder[0] += 1;
                    return i % 2 == 0 ? null : i; // remove evens
                })
                .forEach(row -> {
                });

        Assert.assertEquals(intHolder[0], 10);
    }

    @Test
    public void checkOneCallPerRow_3() {
        final int[] intHolder = { 0 };
        createColumn(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .filter((Integer i) -> {
                    intHolder[0] += 1;
                    return i % 2 == 1 ? null : i; // remove odds
                })
                .forEach(row -> {});

        Assert.assertEquals(intHolder[0], 10);
    }

    @Test
    public void checkOneCallPerRow_4() {
        final int[] intHolder = { 0 };
        createColumn(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .filter((Integer i) -> {
                    intHolder[0] += 1;
                    return i < 3 || i > 8 ? null : i; // remove 1, 2, 9, 10 - start and end
                })
                .forEach(row -> {
                });

        Assert.assertEquals(intHolder[0], 10);
    }

    @Test
    public void checkOneCallPerRow_5() {
        final int[] intHolder = { 0 };
        createColumn(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .filter((Integer i) -> {
                    intHolder[0] += 1;
                    return i > 2 && i < 5 || i > 6 && i < 9 ? null : i; // remove 3, 4, 7, 8 - groups in the middle
                })
                .forEach(row -> {});

        Assert.assertEquals(intHolder[0], 10);
    }

    @Test
    public void checkEmptySource_noNpe() {
        Values filtered = createColumn().filter((Integer i) -> i);

        filtered.forEach(row -> {});

        ValuesIterator iterator = filtered.iterator();
        Assert.assertFalse(iterator.hasNext());
        iterator.shift();
    }

    @Test(expected = NoSuchElementException.class)
    public void checkEmptySource_NoSuchElement() {
        createColumn().filter((Integer i) -> i).iterator().next();
    }


    private static Values.Transformer1<Integer> removeDuplicatesTransformer() {
        return new Values.Transformer1<Integer>() {
            private final Set<Integer> set = new HashSet<>();

            @Override
            public Object transform(Integer integer) throws Throwable {
                boolean isNew = set.add(integer);

                return isNew ? integer : null;
            }
        };
    }

    @Test
    public void statefulCheck() {

        Values multipliedRight = createColumn(1, 2, 2, 1, 1)
                .filter(removeDuplicatesTransformer())
                .multiply("a", "b");
        printValues(multipliedRight);
        System.out.println("=============");
        Values multipliedLeft = createColumn("a", "b")
                .multiply(
                        createColumn(1, 2, 2, 1, 1).filter(removeDuplicatesTransformer())
                );
        printValues(multipliedLeft);
        System.out.println("=============");
        Values multiplyLeftCached = createColumn("a", "b")
                .multiply(
                        createColumn(1, 2, 2, 1, 1).filter(removeDuplicatesTransformer()).createCache()
                );
        printValues(multiplyLeftCached);
    }


    private static void printValues(Values values) {
        for (Object[] value : values) {
            System.out.println(Arrays.deepToString(value));
        }

    }


}
