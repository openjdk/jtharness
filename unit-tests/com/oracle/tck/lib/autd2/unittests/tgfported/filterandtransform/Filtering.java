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

import com.oracle.tck.lib.autd2.TestResult;
import com.oracle.tck.lib.autd2.unittests.BaseTestGroup;
import com.oracle.tck.lib.autd2.unittests.TU;
import com.oracle.tck.lib.autd2.unittests.ValuesComparison;
import com.sun.tck.lib.tgf.*;
import com.sun.tck.test.TestCase;
import org.junit.Assert;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static com.sun.tck.lib.tgf.DataFactory.createColumn;
import static com.sun.tck.lib.tgf.DataFactory.createRow;
import static com.sun.tck.lib.tgf.DataFactory.createValues;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Filtering {



    @Test
    public void minPrefMaxSizes() {
        Integer[] INTS = new Integer[] {0, 1, 2};
        final Values values = createColumn((Object[])INTS).multiply((Object[])INTS).multiply((Object[])INTS).filter(
                new Object() {
                    @Transform
                    Values create(int min, int pref, int max) {
                        if (min <= pref && pref <= max) {
                            return createRow(min, pref, max);
                        } else {
                            return null;
                        }
                    }
                });

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{0, 0, 0});
        expected.add(new Object[]{0, 0, 1});
        expected.add(new Object[]{0, 0, 2});
        expected.add(new Object[]{0, 1, 1});
        expected.add(new Object[]{0, 1, 2});
        expected.add(new Object[]{0, 2, 2});
        expected.add(new Object[]{1, 1, 1});
        expected.add(new Object[]{1, 1, 2});
        expected.add(new Object[]{1, 2, 2});
        expected.add(new Object[]{2, 2, 2});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void equalStrings() {
        final Object filter = new Object() {
            @Transform
            Values create(String s1, String s2) {
                if (s1.equals(s2)) {
                    return createRow(s1, s2);
                } else {
                    return null;
                }
            }
        };
        final Values values = createColumn("a", "b", "c").multiply("a", "b", "c").filter(filter);

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{"a", "a"});
        expected.add(new Object[]{"b", "b"});
        expected.add(new Object[]{"c", "c"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void equalStrings_lambda() {
        final Values values = createColumn("a", "b", "c").multiply("a", "b", "c").filter(
                (String s1, String s2) -> s1.equals(s2) ? createRow(s1, s2) : null );
        final LinkedList<Object[]> expected = new LinkedList<>();
        expected.add(new Object[]{"a", "a"});
        expected.add(new Object[]{"b", "b"});
        expected.add(new Object[]{"c", "c"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void equalStrings_2() {
        final Values source = createColumn("a", "b", "c").multiply("a", "b", "c");
        final Values values = source.filter(
                new Object() {
                    @Transform
                    Values create(String s1, String s2) {
                        if (s1.equals(s2)) {
                            return createRow(s1, s2);
                        } else {
                            return null;
                        }
                    }
                });
        Values result = values.unite(values);

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{"a", "a"});
        expected.add(new Object[]{"b", "b"});
        expected.add(new Object[]{"c", "c"});
        expected.add(new Object[]{"a", "a"});
        expected.add(new Object[]{"b", "b"});
        expected.add(new Object[]{"c", "c"});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void equalStrings_3() {
        final Values source = createColumn("a", "b", "c").multiply("a", "b", "c");
        final Values values =  source.filter( new Object() {
                    @Transform
                    Values create(String s1, String s2) {
                        if (s1.equals(s2)) {
                            return createRow(s1, s2);
                        } else {
                            return null;
                        }
                    }
                });
        Values result = values.unite(source);

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{"a", "a"});
        expected.add(new Object[]{"b", "b"});
        expected.add(new Object[]{"c", "c"});
        expected.add(new Object[]{"a", "a"});
        expected.add(new Object[]{"a", "b"});
        expected.add(new Object[]{"a", "c"});
        expected.add(new Object[]{"b", "a"});
        expected.add(new Object[]{"b", "b"});
        expected.add(new Object[]{"b", "c"});
        expected.add(new Object[]{"c", "a"});
        expected.add(new Object[]{"c", "b"});
        expected.add(new Object[]{"c", "c"});
        ValuesComparison.compare(result, expected);
    }

    @Test
    public void equalStrings_4() {
        final Values source = createColumn("a", "b", "c").multiply("a", "b", "c");
        final Values values = source.filter(
                new Object() {
                    @Transform
                    Values create(String s1, String s2) {
                        if (s1.equals(s2)) {
                            return createRow(s1, s2);
                        } else {
                            return null;
                        }
                    }
                });
        Values result = values.unite(values);

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();

        expected.add(new Object[]{"a", "a"});
        expected.add(new Object[]{"b", "b"});
        expected.add(new Object[]{"c", "c"});
        expected.add(new Object[]{"a", "a"});
        expected.add(new Object[]{"b", "b"});
        expected.add(new Object[]{"c", "c"});

        ValuesComparison.compare(result, expected);
    }

    @Test
    public void equalStrings_5() {
        final Values source = createColumn("a", "b", "c").multiply("a", "b", "c");
        final Values values = source.filter(
                new Object() {
                    @Transform
                    Values create(String s1, String s2) {
                        if (s1.equals(s2)) {
                            return createRow(s1, s2);
                        } else {
                            return null;
                        }
                    }
                });
        Values result = values.intersect(source);

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();

        expected.add(new Object[]{"a", "a"});
        expected.add(new Object[]{"b", "b"});
        expected.add(new Object[]{"c", "c"});

        ValuesComparison.compare(result, expected);
    }

    @Test
    public void equalStrings_6() {
        final Values source = createColumn("a", "b", "c").multiply("a", "b", "c");
        final Values values = source.filter(
                new Object() {
                    @Transform
                    Values create(String s1, String s2) {
                        if ( !s1.equals(s2) ) {
                            return createRow(s1, s2);
                        } else {
                            return null;
                        }
                    }
                });

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();

        expected.add(new Object[]{"a", "b"});
        expected.add(new Object[]{"a", "c"});
        expected.add(new Object[]{"b", "a"});
        expected.add(new Object[]{"b", "c"});
        expected.add(new Object[]{"c", "a"});
        expected.add(new Object[]{"c", "b"});

        ValuesComparison.compare(values, expected);
    }

    @Test
    public void equalStrings_7_nothing() {
        final Values source = createColumn("a", "b", "c").multiply("a", "b", "c");
        final Values values = source.filter(
                new Object() {
                    @Transform
                    Values create(String s1, String s2) {
                            return null;
                    }
                });

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();


        ValuesComparison.compare(values, expected);
    }

    @Test
    public void equalStrings_7_nothing_lambda() {
        final Values source = createColumn("a", "b", "c").multiply("a", "b", "c");
        final Values values = source.filter( (String s1, String s2) -> null);
        final LinkedList<Object[]> expected = new LinkedList<>();


        ValuesComparison.compare(values, expected);
    }

    @Test
    public void equalStrings_8_one() {
        final Values source = createColumn(1, 2, 3).multiply(4, 5, 6);
        final Values values = source.filter(
                new Object() {
                    @Transform Values create(int i1, int i2) {
                        if (i1 == 2 && i2 == 5 ) {
                            return createRow(i1, i2);
                        } else {
                            return null;
                        }
                    }
                });

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();

        expected.add(new Object[]{2, 5});

        ValuesComparison.compare(values, expected);

    }

    @Test
    public void equalStrings_8_one_lambda() {
        final Values source = createColumn(1, 2, 3).multiply(4, 5, 6);
        final Values values = source.filter( (Integer i1, Integer i2) -> {
            if (i1 == 2 && i2 == 5) {
                return createRow(i1, i2);
            } else {
                return null;
            }
        }       );

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();

        expected.add(new Object[]{2, 5});

        ValuesComparison.compare(values, expected);

    }


    @Test
    public void equalStrings_9() {
        final Values source = createColumn("a", "b", "c").multiply("a", "b", "c");
        final Values values = source.filter(
                new Object() {
                    @Transform
                    Values create(final String s1, final String s2) {
                        return source.filter( new Object() {
                            @Transform Values create(String b1, String b2) {
                                if (s1.equals(s2) && b1.equals(b2)) {
                                    return createRow(s1+s2+b1+b2);
                                } else {
                                    return null;
                                }
                            }
                        });
                    }
                });

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();

        expected.add(new Object[]{"aaaa"});
        expected.add(new Object[]{"aabb"});
        expected.add(new Object[]{"aacc"});
        expected.add(new Object[]{"bbaa"});
        expected.add(new Object[]{"bbbb"});
        expected.add(new Object[]{"bbcc"});
        expected.add(new Object[]{"ccaa"});
        expected.add(new Object[]{"ccbb"});
        expected.add(new Object[]{"cccc"});

        ValuesComparison.compare(values, expected);
    }

    @Test
    public void equalStrings_9_lambda() {
        final Values source = createColumn("a", "b", "c").multiply("a", "b", "c");
        final Values values = source.filter(
                (String s1, String s2) -> source.filter((String b1, String b2) -> s1.equals(s2) && b1.equals(b2) ? createRow(s1 + s2 + b1 + b2) : null));

        final LinkedList<Object[]> expected = new LinkedList<>();

        expected.add(new Object[]{"aaaa"});
        expected.add(new Object[]{"aabb"});
        expected.add(new Object[]{"aacc"});
        expected.add(new Object[]{"bbaa"});
        expected.add(new Object[]{"bbbb"});
        expected.add(new Object[]{"bbcc"});
        expected.add(new Object[]{"ccaa"});
        expected.add(new Object[]{"ccbb"});
        expected.add(new Object[]{"cccc"});

        ValuesComparison.compare(values, expected);
    }

    @Test
    public void equalStrings_9_1() {
        final Values source = createColumn("a", "b").multiply("a", "b");
        final Values values = source.filter(
                new Object() {
                    @Transform
                    Values create(final String s1, final String s2) {
                        return source.filter( new Object() {
                            @Transform Values create(String b1, String b2) {
                                if (s1.equals(s2) && b1.equals(b2)) {
                                    return createRow(s1+s2+b1+b2);
                                } else {
                                    return null;
                                }
                            }
                        });
                    }
                });

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();

        expected.add(new Object[]{"aaaa"});
        expected.add(new Object[]{"aabb"});
        expected.add(new Object[]{"bbaa"});
        expected.add(new Object[]{"bbbb"});

        ValuesComparison.compare(values, expected);
    }

    @Test
    public void equalStrings_10() {
        final Values source = createColumn("a", "b", "c").multiply("a", "b", "c");

        final Object filter = new Object() {
            @Transform Values create(final String s1, final String s2) {
                if( s1.equals(s2)) {
                    return createRow( s1 + "1", s2 + "2" );
                } else {
                    return null;
                }
            }
        };
        final Values values_1 = source.filter(filter);
        final Values values_2 = values_1.filter(filter);

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();

        ValuesComparison.compare(values_2, expected);
    }


    @Test
    public void testWrappingOneValue_1() {
        createBaselineGroups();

    }
    public static Values createBaselineGroups() {
        return createGroupLayouts().filter(new Object() {
            public
            @Transform
            Values create(GroupLayout layout) {
                return DataFactory.createColumn((Object[])createBaselineGroups(layout));
            }
        });
    }

    public static Values createGroupLayouts() {
        return createContainers().filter(new Object() {
            @Transform
            GroupLayout create(Container container) {
                final GroupLayout layout = new GroupLayout(container);
                container.setLayout(layout);
                return layout;
            }
        }
        );
    }

    public static Values createContainers() {
        return DataFactory.createColumn(
                new JPanel(), new JTree(),
                new JTable(), new JButton(),
                new Container() {
                }
        );
    }


    public static AbstractValue<?>[] createBaselineGroups(final GroupLayout layout) {
        return new AbstractValue<?>[]{
                new AbstractValue<GroupLayout.Group>() {
                    public GroupLayout.Group create() {
                        return layout.createBaselineGroup(true, true);
                    }
                },
                new AbstractValue<GroupLayout.Group>() {
                    public GroupLayout.Group create() {
                        return layout.createBaselineGroup(false, true);
                    }
                },
                new AbstractValue<GroupLayout.Group>() {
                    public GroupLayout.Group create() {
                        return layout.createBaselineGroup(true, false);
                    }
                },
                new AbstractValue<GroupLayout.Group>() {
                    public GroupLayout.Group create() {
                        return layout.createBaselineGroup(false, false);
                    }
                }
        };
    }

    @Test
    public void arrayReturned_01() {

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            private Values values() {
                Values values = createColumn(0, 1, 2).filter(
                        new Object() {
                            @Transform Integer[] create(int i) {
                                return new Integer[]{i, i + 1, i + 2};
                            }
                        }
                );

                return createColumn("a", "b", "c").pseudoMultiply(values);
            }

            @TestCase
            @TestData("values")
            public void wrongTest(String s, Integer integer) throws Throwable {
                System.out.println("s = " + s);
                System.out.println("integers = " + integer);
            }
        }, TU.EMPTY_ARGV);
        assertTrue( status.isOK() );
    }

    @Test
    public void arrayReturned_01_lambda () {

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            private Values values() {
                Values values = createColumn(0, 1, 2).filter( (Integer i) -> new Integer[] {i, i + 1, i + 2} );

                return createColumn("a", "b", "c").pseudoMultiply(values);
            }

            @TestCase
            @TestData("values")
            public void wrongTest(String s, Integer integer) throws Throwable {
                System.out.println("s = " + s);
                System.out.println("integers = " + integer);
            }
        }, TU.EMPTY_ARGV);
        assertTrue( status.isOK() );
    }


    @Test
    public void returningArrayUntouched() {
        final java.util.List<Integer[]> passed = new ArrayList<Integer[]>();

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            private Values values() {
                return createColumn(0, 1, 2).filter(
                        new Object() {
                            @Transform
                            Values create(int i) {
                                return DataFactory.createValues(new Object[][]{
                                        {new Integer[]{i, i + 1, i + 2}}}
                                );
                            }
                        });
            }

            @TestCase
            @TestData("values")
            public void test(Integer[] integer) throws Throwable {
                passed.add(integer);
            }
        }, TU.EMPTY_ARGV);
        assertEquals(3, passed.size());
        assertArrayEquals(new Integer[] {0,1,2}, passed.get(0));
        assertArrayEquals(new Integer[] {1,2,3}, passed.get(1));
        assertArrayEquals(new Integer[] {2,3,4}, passed.get(2));


        assertTrue( status.isOK() );
    }

    @Test
    public void listReturned_01() {

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            private Values values() {
                Values values = createColumn(0, 1, 2).filter(
                        new Object() {
                            @Transform
                            java.util.List<Integer> create(int i) {
                                return Arrays.asList(i, i + 1, i + 2);
                            }
                        });
                return createColumn("a", "b", "c").pseudoMultiply(values);
            }

            @TestCase
            @TestData("values")
            public void wrongTest(String s, Integer integer) throws Throwable {
                System.out.println("s = " + s);
                System.out.println("integers = " + integer);
            }
        }, TU.EMPTY_ARGV);
        assertTrue( status.isOK() );
    }

    @Test
    public void iteratorReturned_01() {

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            private Values values() {
                Values values = createColumn(0, 1, 2).filter(
                        new Object() {
                            @Transform
                            Iterator<Integer> create(int i) {
                                return Arrays.asList(i, i + 1, i + 2).iterator();
                            }
                        });
                return createColumn("a", "b", "c").pseudoMultiply(values);
            }

            @TestCase
            @TestData("values")
            public void wrongTest(String s, Integer integer) throws Throwable {
                System.out.println("s = " + s);
                System.out.println("integers = " + integer);
            }
        }, TU.EMPTY_ARGV);
        assertTrue( status.isOK() );
    }

    @Test
    public void listReturned_01_lambda() {

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            private Values values() {
                Values values = createColumn(0, 1, 2).filter( (Integer i) -> Arrays.asList(i, i + 1, i + 2));
                return createColumn("a", "b", "c").pseudoMultiply(values);
            }

            @TestCase
            @TestData("values")
            public void wrongTest(String s, Integer integer) throws Throwable {
                System.out.println("s = " + s);
                System.out.println("integers = " + integer);
            }
        }, TU.EMPTY_ARGV);
        assertTrue( status.isOK() );
    }

    @Test
    public void arrayReturned_02() {

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            private Values values() {
                Values values = createColumn(0, 1, 2).filter(
                        new Object() {
                            @Transform
                            Integer[][] create(int i) {
                                return new Integer[][]{
                                        {i, i + 1},
                                        {i, i + 2},
                                        {i, i + 20}
                                };
                            }
                        });
                return createColumn("a", "b", "c").pseudoMultiply(values);
            }

            @TestCase
            @TestData("values")
            public void wrongTest(String s, Integer integer1, Integer integer2) throws Throwable {
                System.out.println("s = " + s);
                System.out.println("integer1 = " + integer1);
                System.out.println("integer2 = " + integer2);
            }
        }, TU.EMPTY_ARGV);
        assertTrue( status.isOK() );
    }

    @Test
    public void arrayReturned_02_lambda() {

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {

            private Values values() {
                Values values = createColumn(0, 1, 2).filter( (Integer i) -> new Integer[][]{
                        {i, i + 1},
                        {i, i + 2},
                        {i, i + 20}
                });
                return createColumn("a", "b", "c").pseudoMultiply(values);
            }

            @TestCase
            @TestData("values")
            public void wrongTest(String s, Integer integer1, Integer integer2) throws Throwable {
                System.out.println("s = " + s);
                System.out.println("integer1 = " + integer1);
                System.out.println("integer2 = " + integer2);
            }
        }, TU.EMPTY_ARGV);
        assertTrue( status.isOK() );
    }


    @Test
    public void columnSelector() {
        final Values source = createValues(
                new Object[][]
                        {{1, 2, 3, 4},
                                {10, 20, 30, 40}}
        );

        class ColumnFilter {
            private int[] selectedColumns;
            ColumnFilter(int... selectedColumns) {
                this.selectedColumns = selectedColumns;
            }
            @Transform Values filter(Object... data) {
                java.util.List<Object> result = new ArrayList<Object>();
                for (int i : selectedColumns) {
                    result.add(data[i]);
                }
                return DataFactory.createRow(result.toArray());
            }
        }

        final Values values = source.filter(new ColumnFilter(0,2));

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{1, 3});
        expected.add(new Object[]{10, 30});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void varargs_1() {
        checkVarargs_1(new Object() {
            @Transform
            Values create(Object[] objects) {
                return createRow(objects);
            }
        });
    }

    @Test
    public void varargs_2() {
        checkVarargs_1(new Object() {
            @Transform
            Values create(Object... objects) {
                return createRow(objects);
            }
        });
    }

    private void checkVarargs_1(Object filter) {
        final Values source = createColumn("a", "b").multiply("c", "d");
        final Values values = source.filter(filter);
        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{"a", "c"});
        expected.add(new Object[]{"a", "d"});
        expected.add(new Object[]{"b", "c"});
        expected.add(new Object[]{"b", "d"});
        ValuesComparison.compare(values, expected);
    }


    @Test
    public void test_01() {
        final Values source = createColumn("a", "b").multiply(1, 2);
        final Values values = source.filter(new Object() {
            @Transform Values create(String s, int i) {
                return createRow(s, i);
            }
        });
        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{"a", 1});
        expected.add(new Object[]{"a", 2});
        expected.add(new Object[]{"b", 1});
        expected.add(new Object[]{"b", 2});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_01_lambda() {
        final Values source = createColumn("a", "b").multiply(1, 2);
        final Values values = source.filter((String s, Integer i) -> createRow(s, i));
        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{"a", 1});
        expected.add(new Object[]{"a", 2});
        expected.add(new Object[]{"b", 1});
        expected.add(new Object[]{"b", 2});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_02() {
        final Values source = createColumn("a", "b").multiply(1, 2);
        final Values values = source.filter(new Object() {
            @Transform Values create(String s, Integer i) {
                return createRow(s, i);
            }
        });
        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{"a", 1});
        expected.add(new Object[]{"a", 2});
        expected.add(new Object[]{"b", 1});
        expected.add(new Object[]{"b", 2});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_03() {
        final Values source = createColumn("a", "b").multiply(1, 2).pseudoMultiply(3, 4);
        final Values values = source.filter(new Object() {
            @Transform Values create(String s, Integer i, int j) {
                return createRow(s, i, j);
            }
        });
        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{"a", 1, 3});
        expected.add(new Object[]{"a", 2, 4});
        expected.add(new Object[]{"b", 1, 3});
        expected.add(new Object[]{"b", 2, 4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_04() {
        final Values source = createColumn("a", "b").multiply(1, 2).pseudoMultiply(3, 4);
        final Values values = source.filter(new Object() {
            @Transform Values create(Object... objects) {
                return createRow(objects);
            }
        });
        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{"a", 1, 3});
        expected.add(new Object[]{"a", 2, 4});
        expected.add(new Object[]{"b", 1, 3});
        expected.add(new Object[]{"b", 2, 4});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testRowCreation() {
        final Values source = createValues(new Integer[][]{{1, 2}, {3, 4}, {5, 6}});
        final Values values = source.filter(
                new Object() {
                    @Transform
                    Values create(int i, int j) {
                            return createColumn("a" + i + j, "b" + i + j);
                    }
                });

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{"a12"});
        expected.add(new Object[]{"b12"});
        expected.add(new Object[]{"a34"});
        expected.add(new Object[]{"b34"});
        expected.add(new Object[]{"a56"});
        expected.add(new Object[]{"b56"});

        ValuesComparison.compare(values, expected);
    }

    @Test
    public void testRowCreation_primitiveInts() {
        final Values source = createValues(new int[][]{{1, 2}, {3, 4}, {5, 6}});
        final Values values = source.filter(
                new Object() {
                    @Transform
                    Values create(int i, int j) throws Throwable {
                            return createColumn("a" + i + j, "b" + i + j);
                    }
                });

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{"a12"});
        expected.add(new Object[]{"b12"});
        expected.add(new Object[]{"a34"});
        expected.add(new Object[]{"b34"});
        expected.add(new Object[]{"a56"});
        expected.add(new Object[]{"b56"});

        ValuesComparison.compare(values, expected);
    }


    @Test
    public void absorbingExceptions_testPassed() {
        final ArrayList<String> arrayList = new ArrayList<String>();
        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            Values values =
                    createColumn(String.class, Object.class).filter((Class<?>c) -> c.getConstructor().newInstance());

            @TestCase
            @TestData("values")
            public void myTest(Object s) {
                arrayList.add(s.getClass().getCanonicalName());
            }
        }, TU.EMPTY_ARGV);
        Assert.assertEquals(2, arrayList.size());
        Assert.assertEquals("java.lang.String", arrayList.get(0));
        Assert.assertEquals("java.lang.Object", arrayList.get(1));
        Assert.assertTrue(status.isOK());
        Assert.assertEquals("Passed. test cases: 1; all passed", status.toString());
    }

    @Test
    public void absorbingExceptions_exceptionThrownOut() {
        try {
        final TestResult run = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            Values values =
                    createColumn(Double.class, Class.class).filter((Values.Transformer1<Class<?>>) c -> c.getConstructor().newInstance());

            @TestCase
            @TestData("values")
            public void myTest(Object s) {
                System.out.println("s = " + s);
            }
        });
            Assert.fail("Exception expected");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof InvocationTargetException);
            Assert.assertTrue(e.getCause().getCause() instanceof NoSuchMethodException);
            Assert.assertEquals("java.lang.Double.<init>()", e.getCause().getCause().getMessage());
        }
    }

    @Test
    public void absorbingExceptions_exceptionThrownOut_iteratingFilter() {
        try {
                Values values =
                        createColumn(Double.class, Class.class).filter((Values.Transformer1<Class<?>>) c -> c.getConstructor().newInstance());
            for (Object[] value : values) {
            }
            Assert.fail("Exception expected");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof InvocationTargetException);
            Assert.assertTrue(e.getCause().getCause() instanceof NoSuchMethodException);
            Assert.assertEquals("java.lang.Double.<init>()", e.getCause().getCause().getMessage());
        }
    }

    @Test
    public void noExceptionIfNotIterating() {
        Values values =
                createColumn(Double.class, Class.class).filter((Values.Transformer1<Class<?>>)c -> c.getConstructor().newInstance());
    }

    @Test
    public void noExceptionIfNotIterating_1() {
        Values values =
                createColumn(Double.class, Class.class).filter((Class<?>c) -> {throw new Error("Delayed!");});
    }

    @Test
    public void absorbingExceptions_exceptionThrownOut_1() {
        final ArrayList<String> arrayList = new ArrayList<String>();
        final Throwable throwable = new Throwable();
        try {
            com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
                Values values =
                        createColumn("1", "2").filter((String s) -> { throw throwable;});

                @TestCase
                @TestData("values")
                public void myTest(Object s) {
                    arrayList.add(s.getClass().getCanonicalName());
                }
            });
            Assert.fail("Exception expected");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof InvocationTargetException);
            Assert.assertSame(throwable, e.getCause().getCause());
        }
    }

    @Test
    public void test_initialSettingDoesntCallFilter() {

        ArrayList<Integer> passed = new ArrayList<>();

        Values values = DataFactory.createColumn(1, 2, 3, 4, 5).filter((Integer i) -> {
            passed.add(i);
            return i;
        });

        Assert.assertEquals(0, passed.size());
        for (Object[] objects : values) {  /* this will populate 'passed' array */}
        Assert.assertEquals(5, passed.size());
        Assert.assertEquals(Integer.valueOf(1), passed.get(0));
        Assert.assertEquals(Integer.valueOf(2), passed.get(1));
        Assert.assertEquals(Integer.valueOf(3), passed.get(2));
        Assert.assertEquals(Integer.valueOf(4), passed.get(3));
        Assert.assertEquals(Integer.valueOf(5), passed.get(4));

        final LinkedList<Object[]> expected = new LinkedList<Object[]>();
        expected.add(new Object[]{1});
        expected.add(new Object[]{2});
        expected.add(new Object[]{3});
        expected.add(new Object[]{4});
        expected.add(new Object[]{5});
        ValuesComparison.compare(values, expected);
    }


    @Test
    public void test_exception() {
        ArrayList<Integer> passed = new ArrayList<>();

        com.oracle.tck.lib.autd2.TestResult status = com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
            Values data = DataFactory.createColumn(1, 2, 3, 4, 5).filter((Integer i) -> {
                return i;
            });

            @TestCase
            @TestData("data")
            public void myTest(int i) {
                passed.add(i);
            }
        }, TU.EMPTY_ARGV);
        Assert.assertEquals(!status.isOK(), false);
        Assert.assertEquals(status.isOK(), true);

        Assert.assertEquals(5, passed.size());
        Assert.assertArrayEquals(new Integer[]{1, 2, 3, 4, 5}, passed.toArray(new Object[passed.size()]));

    }

    @Test
    public void totallyFilteredOut() {
        Values dataToEmpty= DataFactory.createColumn(3, 0).multiply(3, 1).multiply(3, 4)
                .multiply(4,5).multiply(-1, 0, 1).multiply(4,6,7).<Integer, Integer, Integer, Integer, Integer, Integer>filter(
                        (major, minor, security, pre, build, opt) -> null
                );
        int counter = 0;
        for (Object[] objects : dataToEmpty) {
            counter++;
        }
        System.out.println("counter = " + counter);
        Assert.assertEquals(0, counter);
    }

    @Test
    public void transformedInOneValue() {
        final Iterable<Object[]> iterable = DataFactory.createColumn(3, 0,4).multiply(3, 1).multiply(3, 4)
                .multiply(4,5).multiply(-1, 0, 1).multiply(4,6,7).<Integer, Integer, Integer, Integer, Integer, Integer>filter(
                (major, minor, security, pre, build, opt) -> "abc"
        );
        int counter = 0;
        for (Object[] objects : iterable) {
            counter++;
        }
        Assert.assertEquals(216, counter);
    }


}
