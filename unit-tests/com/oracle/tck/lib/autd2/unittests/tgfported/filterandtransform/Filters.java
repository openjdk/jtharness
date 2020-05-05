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
import com.oracle.tck.lib.autd2.unittests.ValuesComparison;
import com.sun.tck.lib.tgf.DataFactory;
import com.sun.tck.lib.tgf.TestData;
import com.sun.tck.lib.tgf.Transform;
import com.sun.tck.lib.tgf.Values;
import com.sun.tck.test.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static com.sun.tck.lib.tgf.DataFactory.createColumn;
import static com.sun.tck.lib.tgf.DataFactory.createValues;

/**
 *
 */
public class Filters {


    @Test
    public void test_1() {
        Values values = createColumn(1, 2);

        values = values.filter( new Object(){
            @Transform
            Values filter(int i) {
                return createColumn( i-1, i, i+1);
            }
        });
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{0});
        expected.add(new Object[]{1});
        expected.add(new Object[]{2});
        expected.add(new Object[]{1});
        expected.add(new Object[]{2});
        expected.add(new Object[]{3});
            ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_exception() {
        try {
            com.oracle.tck.lib.autd2.unittests.TU.runTestGroup(new BaseTestGroup() {
                private Values data() {
                    return createColumn(1, 2).filter(new Object() {
                        @Transform
                        Values filter(int i) {
                            throw new Error("123");
                        }
                    });
                }

                @TestCase
                @TestData("data")
                public void myTest(int i) {
                }
            }, TU.EMPTY_ARGV);
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof InvocationTargetException);
            Assert.assertTrue(e.getCause().getCause() instanceof Error);
            Assert.assertEquals("123", e.getCause().getCause().getMessage());
        }
    }

    /**
     * Testing that no exceptions are thrown
     */
    @Test
    public void test_laziness() {
        new BaseTestGroup() {
            private Values data() {
                return createColumn(1, 2).filter(new Object() {
                    @Transform
                    Values filter(int i) {
                        throw new Error("123");
                    }
                });
            }

            @TestCase
            @TestData("data")
            public void myTest(int i) {
            }
        };
    }

    /**
     * Testing that no exceptions are thrown
     */
    @Test
    public void test_laziness_1() {
        final BaseTestGroup baseTestGroup = new BaseTestGroup() {
            Values data = createColumn(1, 2).filter(new Object() {
                @Transform
                Values filter(int i) {
                    throw new Error("123");
                }
            });

            @TestCase
            @TestData("data")
            public void myTest(int i) {
            }
        };
    }

    @Test
    public void test_1_cache() {
        Values values = createColumn(1, 2);

        values = values.filter( new Object(){
            @Transform Values filter(int i) {
                return createColumn( i-1, i, i+1);
            }
        });
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{0});
        expected.add(new Object[]{1});
        expected.add(new Object[]{2});
        expected.add(new Object[]{1});
        expected.add(new Object[]{2});
        expected.add(new Object[]{3});
        ValuesComparison.compare(values.createCache(), expected);
    }

    @Test
    public void test_2() {
        Values values = createColumn(1, 2);

        values = values.filter( new Object(){
            @Transform Values filter(int i) {
                return createColumn( i, i+1).multiply( i-1, i );
            }
        });
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 0});
        expected.add(new Object[]{1, 1});
        expected.add(new Object[]{2, 0});
        expected.add(new Object[]{2, 1});
        expected.add(new Object[]{2, 1});
        expected.add(new Object[]{2, 2});
        expected.add(new Object[]{3, 1});
        expected.add(new Object[]{3, 2});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_2_cache() {
        Values values = createColumn(1, 2);

        values = values.filter( new Object(){
            @Transform Values filter(int i) {
                return createColumn( i, i+1).multiply( i-1, i );
            }
        });
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, 0});
        expected.add(new Object[]{1, 1});
        expected.add(new Object[]{2, 0});
        expected.add(new Object[]{2, 1});
        expected.add(new Object[]{2, 1});
        expected.add(new Object[]{2, 2});
        expected.add(new Object[]{3, 1});
        expected.add(new Object[]{3, 2});
        ValuesComparison.compare(values.createCache(), expected);
    }

    @Test
    public void test_3() {

        Values values_2 = createColumn(1, 2).filter(new Object(){
            @Transform Values filter(int i) {
                return createColumn( i, i+1);
            }
        });
        Values values_1 =  createColumn(3, 4);
        Values result = values_1.multiply(values_2);

        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[]{3, 1} );
        expected.add( new Object[]{3, 2} );
        expected.add( new Object[]{3, 2} );
        expected.add( new Object[]{3, 3} );

        expected.add( new Object[]{4, 1} );
        expected.add( new Object[]{4, 2} );
        expected.add( new Object[]{4, 2} );
        expected.add( new Object[]{4, 3} );

        ValuesComparison.compare(result, expected);
    }

    @Test
    public void test_4() {

        Values values_2 = createColumn(1, 2).filter( new Object(){
            @Transform Values filter(int i) {
                return createValues( new Object[][] {{i, i+1}, {i+2, i+3}});
            }
        });
        Values values_1 =  createColumn(3, 4);
        Values result = values_1.multiply(values_2);

        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[]{3, 1, 2} );
        expected.add( new Object[]{3, 3, 4} );
        expected.add( new Object[]{3, 2, 3} );
        expected.add( new Object[]{3, 4, 5} );

        expected.add( new Object[]{4, 1, 2} );
        expected.add( new Object[]{4, 3, 4} );
        expected.add( new Object[]{4, 2, 3} );
        expected.add( new Object[]{4, 4, 5} );


        ValuesComparison.compare(result, expected);
    }

    @Test
    public void test_4_cache() {

        Values values_2 = createColumn(1, 2).filter( new Object(){
            @Transform Values filter(int i) {
                return createValues( new Object[][] {{i, i+1}, {i+2, i+3}});
            }
        });
        Values values_1 =  createColumn(3, 4);
        Values result = values_1.multiply(values_2);

        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[]{3, 1, 2} );
        expected.add( new Object[]{3, 3, 4} );
        expected.add( new Object[]{3, 2, 3} );
        expected.add( new Object[]{3, 4, 5} );

        expected.add( new Object[]{4, 1, 2} );
        expected.add( new Object[]{4, 3, 4} );
        expected.add( new Object[]{4, 2, 3} );
        expected.add( new Object[]{4, 4, 5} );

        ValuesComparison.compare(result.createCache(), expected);
    }

    @Test
    public void test_5_firstOut_multiply() {
        Values values = createColumn(0, 1, 2);
        values = values.filter( new Object(){
            @Transform Integer filter(int i) {
                return i != 0 ? i : null;
            }
        }).multiply("a", "b", "c");
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, "a"});
        expected.add(new Object[]{1, "b"});
        expected.add(new Object[]{1, "c"});
        expected.add(new Object[]{2, "a"});
        expected.add(new Object[]{2, "b"});
        expected.add(new Object[]{2, "c"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_5_firstOut_pseudoMultiply() {
        Values values = createColumn(0, 1, 2);
        values = values.filter( new Object(){
            @Transform Integer filter(int i) {
                return i != 0 ? i : null;
            }
        }).pseudoMultiply("a", "b", "c");
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, "a"});
        expected.add(new Object[]{2, "b"});
        expected.add(new Object[]{1, "c"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_5_firstOut_pseudoMultiply_lambda() {
        Values values = createColumn(0, 1, 2);
        values = values.filter((Integer i)-> i != 0 ? i : null).pseudoMultiply("a", "b", "c");
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1, "a"});
        expected.add(new Object[]{2, "b"});
        expected.add(new Object[]{1, "c"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_5_firstOut_intersect() {
        Values values = createColumn(0, 1, 2);
        values = values.filter( new Object(){
            @Transform Integer filter(int i) {
                return i != 0 ? i : null;
            }
        }).intersect(0, 4, 6, 1, 8, 9, 2);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{1});
        expected.add(new Object[]{2});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_5_firstTwoOut() {
        Values values = createColumn(0, 1, 2, 3);
        values = values.filter( new Object(){
            @Transform Integer filter(int i) {
                return i > 1 ? i : null;
            }
        }).multiply("a", "b", "c");
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{2, "a"});
        expected.add(new Object[]{2, "b"});
        expected.add(new Object[]{2, "c"});
        expected.add(new Object[]{3, "a"});
        expected.add(new Object[]{3, "b"});
        expected.add(new Object[]{3, "c"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_5_secondOut() {

        Values values = createColumn(0, 1, 2);
        values = values.filter( new Object(){
            @Transform Integer filter(int i) {
                return i != 1 ? i : null;
            }
        }).multiply("a", "b", "c");
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{0, "a"});
        expected.add(new Object[]{0, "b"});
        expected.add(new Object[]{0, "c"});
        expected.add(new Object[]{2, "a"});
        expected.add(new Object[]{2, "b"});
        expected.add(new Object[]{2, "c"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_5_severalInTheMiddle() {
        Values values = createColumn(0, 1, 2, 3, 4);
        values = values.filter( new Object(){
            @Transform Integer filter(int i) {
                return i!=1 && i!=2 ? i : null;
            }
        }).multiply("a", "b");
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{0, "a"});
        expected.add(new Object[]{0, "b"});
        expected.add(new Object[]{3, "a"});
        expected.add(new Object[]{3, "b"});
        expected.add(new Object[]{4, "a"});
        expected.add(new Object[]{4, "b"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_5_severalInTheMiddle_pseudoMult() {
        Values values = createColumn(0, 1, 2, 3, 4);
        values = values.filter( new Object(){
            @Transform Integer filter(int i) {
                return i!=1 && i!=2 ? i : null;
            }
        }).pseudoMultiply("a", "b");
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{0, "a"});
        expected.add(new Object[]{3, "b"});
        expected.add(new Object[]{4, "a"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_nothingLeft_multiply_reversed() {
        Values values = createColumn("a", "b", "c").multiply(
            createColumn(1, 2, 3).filter(new Object() {
                @Transform Integer filter(int i) { return null; }
            })
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_nothingLeft_multiply_reversed_lambda() {
        Values values = createColumn("a", "b", "c").multiply(
            createColumn(1, 2, 3).filter((Integer i) -> null)
        );
        List<Object[]> expected = new ArrayList<>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_nothingLeft_intersect_reversed() {
        Values values = createColumn("a", "b", "c").intersect(
            createColumn(1, 2, 3).filter(new Object() {
                @Transform Integer filter(int i) { return null; }
            })
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_nothingLeft_pseudoMultiply_reversed() {
        Values values = createColumn("a", "b", "c").pseudoMultiply(
            createColumn(1, 2, 3).filter(new Object() {
                @Transform Integer filter(int i) { return null; }
            })
        );
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_nothingLeft_unite_reversed() {
        Values values = createColumn("a", "b", "c").unite(
            createColumn(1, 2, 3).filter(new Object() {
                @Transform Integer filter(int i) { return null; }
            })
        );

        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {"a"} );
        expected.add(new Object[] {"b"} );
        expected.add(new Object[] {"c"} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_nothingLeft_1() {
        Values values = createColumn(0).filter( new Object(){
            @Transform Integer filter(int i) {
                return null;
            }
        });
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_nothingLeft_multiply() {

        Values values = createColumn(0, 1, 2).filter( new Object(){
            @Transform Integer filter(int i) {
                return null;
            }
        }).multiply("a", "b", "c");
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_nothingLeft_intersect() {
        Values values = createColumn(0, 1, 2).filter( new Object(){
            @Transform Integer filter(int i) {
                return null;
            }
        }).intersect("a", "b", "c");
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_nothingLeft_pseudoMultiply() {
        Values values = createColumn(0, 1, 2).filter( new Object(){
            @Transform Integer filter(int i) {
                return null;
            }
        }).pseudoMultiply("a", "b", "c");
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_nothingLeft_2() {
        Values values = createColumn(0, 1, 2);
        values = values.filter( new Object(){
            @Transform Integer filter(int i) {
                return null;
            }
        });
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_nothingLeft_createCache() {
        Values values = createColumn(0, 1, 2);
        values = values.filter( new Object(){
            @Transform Integer filter(int i) {
                return null;
            }
        }).createCache();
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_nothingLeft_doubleFiltering_1() {
        Values values = createColumn(0, 1, 2);
        values = values.filter( new Object(){
            @Transform Integer filter(int i) {
                return null;
            }
        }).filter( new Object() {
                                //   :-)
            @Transform Integer filter() {
                return null;
            }
        });
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_nothingLeft_doubleFiltering_2() {
        Values values = createColumn(0, 1, 2);
        values = values.filter( new Object(){
            @Transform Integer filter(int i) {
                return null;
            }
        }).filter( new Object() {
            @Transform Integer filter() {
                return 15;
            }
        });
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }


    @Test
    public void test_nothingLeft_unite() {
        Values values = createColumn(0, 1, 2);
        values = values.filter( new Object(){
            @Transform Integer filter(int i) {
                return null;
            }
        }).unite("a", "b", "c");
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {"a"} );
        expected.add(new Object[] {"b"} );
        expected.add(new Object[] {"c"} );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_nothingLeft_reduce() {
        Values values = createColumn(0, 1, 2);
        values = values.filter( new Object(){
            @Transform Integer filter(int i) {
                return null;
            }
        }).reduceTo(0.7);
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }


    @Test
    public void test_nothingLeft_intersect_1() {
        Values values = createColumn(0, 1, 2);
        values = values.filter( new Object(){
            @Transform Integer filter(int i) {
                return null;
            }
        }).intersect(7, 8, 9);
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_nothingLeft_multiply_0() {
        Values values = createColumn(0, 1, 2);
        values = values.filter( new Object(){
            @Transform Integer filter(int i) {
                return null;
            }
        }).multiply(7, 8, 9);
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_nothingLeft_middle() {
        Values values = createColumn(0, 1, 2);
        values = values.filter( new Object(){
            @Transform Integer filter(int i) {
                return null;
            }
        }).filter(new Object() {
            @Transform Integer filter() {
                return 2;
            }
        });
        List<Object[]> expected = new ArrayList<Object[]>();

        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_5_thirdOut() {
        Values values = createColumn(0, 1, 2);
        values = values.filter( new Object(){
            @Transform Integer filter(int i) {
                return i != 2 ? i : null;
            }
        }).multiply("a", "b", "c");
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{0, "a"});
        expected.add(new Object[]{0, "b"});
        expected.add(new Object[]{0, "c"});
        expected.add(new Object[]{1, "a"});
        expected.add(new Object[]{1, "b"});
        expected.add(new Object[]{1, "c"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_5_thirdOut_pseudoMult() {
        Values values = createColumn(0, 1, 2);
        values = values.filter( new Object(){
            @Transform Integer filter(int i) {
                return i != 2 ? i : null;
            }
        }).pseudoMultiply("a", "b", "c");
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{0, "a"});
        expected.add(new Object[]{1, "b"});
        expected.add(new Object[]{0, "c"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_5_thirdOut_unite() {
        Values values = createColumn(0, 1, 2);
        values = values.filter( new Object(){
            @Transform Integer filter(int i) {
                return i != 2 ? i : null;
            }
        }).unite("a", "b", "c");
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{0});
        expected.add(new Object[]{1});
        expected.add(new Object[]{"a"});
        expected.add(new Object[]{"b"});
        expected.add(new Object[]{"c"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_5_lastTwoOut_mult() {
        Values values = createColumn(0, 1, 2, 3, 4);
        values = values.filter( new Object(){
            @Transform Integer filter(int i) {
                return i < 2 ? i : null;
            }
        }).multiply("a", "b", "c");
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{0, "a"});
        expected.add(new Object[]{0, "b"});
        expected.add(new Object[]{0, "c"});
        expected.add(new Object[]{1, "a"});
        expected.add(new Object[]{1, "b"});
        expected.add(new Object[]{1, "c"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_5_lastTwoOut_pseudoMult() {
        Values values = createColumn(0, 1, 2, 3, 4);
        values = values.filter( new Object(){
            @Transform Integer filter(int i) {
                return i < 2 ? i : null;
            }
        }).pseudoMultiply("a", "b", "c");
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{0, "a"});
        expected.add(new Object[]{1, "b"});
        expected.add(new Object[]{0, "c"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_5_lastTwoOut_unite() {
        Values values = createColumn(0, 1, 2, 3, 4);
        values = values.filter( new Object(){
            @Transform Integer filter(int i) {
                return i < 2 ? i : null;
            }
        }).unite("a", "b", "c");
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{0});
        expected.add(new Object[]{1});
        expected.add(new Object[]{"a"});
        expected.add(new Object[]{"b"});
        expected.add(new Object[]{"c"});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_6_firstOut() {
        Values values = createColumn(0, 1, 2).filter( new Object(){
            @Transform Integer filter(int i) {
                return i != 0 ? i : null;
            }
        });
        values = createColumn("a", "b", "c").multiply(values);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", 1});
        expected.add(new Object[]{"a", 2});
        expected.add(new Object[]{"b", 1});
        expected.add(new Object[]{"b", 2});
        expected.add(new Object[]{"c", 1});
        expected.add(new Object[]{"c", 2});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_nothingLeft_multiply_1() {
        Values values = createColumn(0, 1, 2).filter( new Object(){
            @Transform Integer filter(int i) {
                return null;
            }
        });

        values = createColumn("a", "b", "c").multiply(values);
        List<Object[]> expected = new ArrayList<Object[]>();

        ValuesComparison.compare(values, expected);
    }


    @Test
    public void test_nothingLeft_multiply_2() {
        Values values = createColumn(0, 1, 2).filter( new Object(){
            @Transform Integer filter(int i) {
                return null;
            }
        });
        values = values.multiply(values);
        List<Object[]> expected = new ArrayList<Object[]>();
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_6_secondOut() {
        Values values = createColumn(0, 1, 2).filter( new Object(){
            @Transform Integer filter(int i) {
                return i != 1 ? i : null;
            }
        });
        values = createColumn("a", "b", "c").multiply(values);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", 0});
        expected.add(new Object[]{"a", 2});
        expected.add(new Object[]{"b", 0});
        expected.add(new Object[]{"b", 2});
        expected.add(new Object[]{"c", 0});
        expected.add(new Object[]{"c", 2});
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_7_lastOut() {
        Values values = createColumn(0, 1, 2).filter( new Object(){
            @Transform Integer filter(int i) {
                return i != 2 ? i : null;
            }
        });
        values = createColumn("a", "b", "c").multiply(values);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[]{"a", 0});
        expected.add(new Object[]{"a", 1});
        expected.add(new Object[]{"b", 0});
        expected.add(new Object[]{"b", 1});
        expected.add(new Object[]{"c", 0});
        expected.add(new Object[]{"c", 1});
        ValuesComparison.compare(values, expected);
    }


    @Test
    public void droppingOut() {

        final boolean somecondition = true;

        Object filter = new Object() {
            @Transform String filter(String s) {
                if (somecondition && "b".equals(s)) {
                    return null;
                } else {
                    return s;
                }
            }
        };
        Values filtered = createColumn("a", "b", "c").filter(filter);


        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {"a"} );
        expected.add(new Object[] {"c"} );
        ValuesComparison.compare(filtered, expected);
    }

    @Test
    public void oneArg_lambda() {

        Values toUpperCase = DataFactory.createColumn("a", "b", "c").filter((String s) -> s.toUpperCase());

        List<Object[]> expected = new ArrayList<>();
        expected.add(new Object[] {"A"} );
        expected.add(new Object[] {"B"} );
        expected.add(new Object[] {"C"} );
        ValuesComparison.compare(toUpperCase, expected);
    }

    @Test
    public void twoArg_lambda() {

        Values v = DataFactory
                .createColumn("a", "b", "c")
                .pseudoMultiply(1, 2, 3)
                .filter((String s, Integer i) -> s + i);

        List<Object[]> expected = new ArrayList<>();
        expected.add(new Object[] {"a1"} );
        expected.add(new Object[] {"b2"} );
        expected.add(new Object[] {"c3"} );
        ValuesComparison.compare(v, expected);
    }

    @Test
    public void fourArg_lambda() {

        Values v = DataFactory
                .createColumn("a", "b", "c")
                .pseudoMultiply(1, 2, 3)
                .pseudoMultiply(2, 3, 5)
                .pseudoMultiply(3, 4, 6)
                .filter((String s, Integer i, Integer j, Integer k) -> s + i + j + k);

        List<Object[]> expected = new ArrayList<>();
        expected.add(new Object[] {"a123"} );
        expected.add(new Object[] {"b234"} );
        expected.add(new Object[] {"c356"} );
        ValuesComparison.compare(v, expected);
    }

    @Test
    public void fiveArg_lambda() {

        Values v = DataFactory
                .createColumn("a", "b", "c")
                .pseudoMultiply(1, 2, 3)
                .pseudoMultiply(2, 3, 5)
                .pseudoMultiply(3, 4, 6)
                .pseudoMultiply("x", "y", "z")
                .filter((String s, Integer i, Integer j, Integer k, String s1) -> s + i + j + k + s1);

        List<Object[]> expected = new ArrayList<>();
        expected.add(new Object[] {"a123x"} );
        expected.add(new Object[] {"b234y"} );
        expected.add(new Object[] {"c356z"} );
        ValuesComparison.compare(v, expected);
    }

    @Test
    public void droppingOut_lambda() {

        final boolean somecondition = true;
        Values filtered = createColumn("a", "b", "c").filter( (String s) -> somecondition && "b".equals(s) ? null : s);
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add(new Object[] {"a"} );
        expected.add(new Object[] {"c"} );
        ValuesComparison.compare(filtered, expected);
    }



}
