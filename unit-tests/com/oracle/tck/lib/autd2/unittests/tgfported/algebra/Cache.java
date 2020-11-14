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
package com.oracle.tck.lib.autd2.unittests.tgfported.algebra;

import com.oracle.tck.lib.autd2.unittests.BaseTestGroup;
import com.oracle.tck.lib.autd2.unittests.TU;
import com.oracle.tck.lib.autd2.unittests.TestObject;
import com.oracle.tck.lib.autd2.unittests.ValuesComparison;
import com.sun.tck.lib.tgf.*;
import com.sun.tck.test.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class Cache {


    @Test
    public void test_1() {
        Values values = DataFactory.createColumn(
                new AbstractValue<Object>() {
                    public Object create() {
                        return new TestObject("a");
                    }
                },
                new AbstractValue<Object>() {
                    public Object create() {
                        return new TestObject("b");
                    }
                },
                new AbstractValue<Object>() {
                    public Object create() {
                        return new TestObject("c");
                    }
                }
        ).createCache();

        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] { new TestObject("a") });
        expected.add(new Object[] { new TestObject("b") });
        expected.add(new Object[] { new TestObject("c") });

        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_2() {
        Values values = DataFactory.createColumn(
        new AbstractValue<Object>() {
            public Object create() {
                return new TestObject("a");
            }
        },
        new AbstractValue<Object>() {
            public Object create() {
                return new TestObject("b");
            }
        },
        new AbstractValue<Object>() {
            public Object create() {
                return new TestObject("c");
            }
        });

        final Values cache = values.createCache();

        final Object[] result = ValuesComparison.createArray(cache);

        List<Object[]> expectedList = new ArrayList<Object[]>();
        expectedList.add(new Object[] { new TestObject("a") });
        expectedList.add(new Object[] { new TestObject("b") });
        expectedList.add(new Object[] { new TestObject("c") });

        final Object[] expected = expectedList.toArray();

        Assert.assertArrayEquals(expected, result);
    }

    @Test
    public void test_3() {
        Values values = DataFactory.createColumn(
        new AbstractValue<Object>() {
            public Object create() {
                return new TestObject("a");
            }
        },
        new AbstractValue<Object>() {
            public Object create() {
                return new TestObject("b");
            }
        },
        new AbstractValue<Object>() {
            public Object create() {
                return new TestObject("c");
            }
        },
        new AbstractValue<Object>() {
            public Object create() {
                return new TestObject("x");
            }
        }).multiply(1,2,3,4,5,6,7,8,9).multiply(89, 45, 23, 78);

        ValuesComparison.checkCachedReturnsTheSame(values);
    }

    @Test
    public void test_4() {
        Values values = DataFactory.createColumn(
                "az", "ac", "dv", "tg"
        ).multiply(1,2,3,4,5,6,7,8,9).multiply(89, 45, 23, 78).unite(
                DataFactory.createRow( "sd",345, 2345));
        ValuesComparison.checkCachedReturnsTheSame(values);
    }

    @Test
    public void performance_1() {
        final Values column = createTimeConsumingColumn();
        final long deltaNonCached = runTest(column, false); // 18 creations
        System.out.println("delta = " + deltaNonCached);
        final long deltaCached = runTest(column, true); // 3 creations
        System.out.println("delta = " + deltaCached);

                                                   //   18 / 3 = 6
        Assert.assertTrue(deltaNonCached > deltaCached * 4);
    }

    private long runTest(final Values column, boolean createCache) {
        final long start = System.currentTimeMillis();

        final Values dataToUse = createCache ? column.createCache() : column;

        TU.runTestGroup(new BaseTestGroup() {
            Values setup() {
                return dataToUse.multiply(dataToUse);
            }

            @TestCase
            @TestData("setup")
            public void theTest(TestObject t1, TestObject t2) {
            }
        });
        return System.currentTimeMillis() - start;
    }

    private static final int MILLIS = 100;

    private Values createTimeConsumingColumn() {
        return DataFactory.createColumn(
                new AbstractValue<TestObject>() {
                    public TestObject create() {
                        try {
                            Thread.sleep(MILLIS);
                        } catch (InterruptedException e) {  }
                        return new TestObject("a");
                    }
                },
                new AbstractValue<TestObject>() {
                    public TestObject create() {
                        try {
                            Thread.sleep(MILLIS);
                        } catch (InterruptedException e) {  }
                        return new TestObject("b");
                    }
                },
                new AbstractValue<TestObject>() {
                    public TestObject create() {
                        try {
                            Thread.sleep(MILLIS);
                        } catch (InterruptedException e) {  }
                        return new TestObject("c");
                    }
                }
        );
    }


}
