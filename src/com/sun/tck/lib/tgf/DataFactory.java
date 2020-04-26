/*
 * $Id$
 *
 * Copyright (c) 2001, 2020, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tck.lib.tgf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A factory that provides implementations of <code>Values</code> interface
 * with given data inside.
 */
public class DataFactory {

    /**
     * Creates new instance of class <code>Values</code>
     * that represents a set of values for a single parameter
     * of a parameterized testcase. In other words it creates a basic
     * one-dimensional set of values.
     *
     * <p>A sample code:<br/>
     * <pre>
     public Values sampleSetup() {
         return DataFactory.createColumn("a", "b", "c", "d");
     }

     <code>@TestCase</code>
     <code>@TestData("sampleSetup")</code>
     public void test(String s) {
         System.out.println("s = " + s);
     }
     * </pre>
     * Will produce the following output:
     * <pre>
     s = a
     s = b
     s = c
     s = d
     * </pre>
     * @param objects objects to use
     * @return instance of Values
     */
    public static Values createColumn(Object... objects) {
        return createValues(new LeafIterator(objects));
    }

    /**
     * Creates new Values that represents a set of tuples of values.
     * Each tuple represent one set of parameter's values for a parameterized
     * testcase. A testcase can be executed once per each tuple in the set.
     * In other words this is two-dimensional value set where each row
     * represent a single micro testcase and each column corresponds to a
     * parameter of the parameterized testcase.
     * Note, that number of columns must correspond number of arguments
     * of test method, otherwise runtime exception will occur.
     *
     * <p>A sample code:<br/>
     * <pre>
     public Values sampleSetup() {
         return DataFactory.createValues(
                 new Object[][]{
                         {1, "a"},
                         {2, "b"},
                         {3, "c"},
                         {4, "d"}
                 }
         );
     }

     <code>@TestCase</code>
     <code>@TestData("sampleSetup")</code>
     public void test(int i, String s) {
         System.out.println("i = " + i + ", s = " + s);
     }
     * </pre>
     * Will produce the following output:
     * <pre>
     i = 1, s = a
     i = 2, s = b
     i = 3, s = c
     i = 4, s = d
     * </pre>
     *
     * @param objects final table of argument values
     * @return instance of Values
     */
    public static Values createValues(Object[][] objects) {
        return createValues(new LeafIterator(objects));
    }

    /**
     * Creates table of data values.
     * @param ints resulting table of argument values
     * @return instance of Values
     */
    public static Values createValues(int[][] ints) {
        return createValues(new LeafIterator(ints));
    }

    /**
     * Creates table of data values.
     * @param booleans resulting table of argument values
     * @return instance of Values
     */
    public static Values createValues(boolean[][] booleans) {
        return createValues(new LeafIterator(booleans));
    }

    /**
     * Creates table of data values.
     * @param bytes resulting table of argument values
     * @return instance of Values
     */
    public static Values createValues(byte[][] bytes) {
        return createValues(new LeafIterator(bytes));
    }

    /**
     * Creates table of data values.
     * @param chars resulting table of argument values
     * @return instance of Values
     */
    public static Values createValues(char[][] chars) {
        return createValues(new LeafIterator(chars));
    }

    /**
     * Creates table of data values.
     * @param doubles resulting table of argument values
     * @return instance of Values
     */
    public static Values createValues(double[][] doubles) {
        return createValues(new LeafIterator(doubles));
    }

    /**
     * Creates table of data values.
     * @param floats resulting table of argument values
     * @return instance of Values
     */
    public static Values createValues(float[][] floats) {
        return createValues(new LeafIterator(floats));
    }

    /**
     * Creates table of data values.
     * @param longs resulting table of argument values
     * @return instance of Values
     */
    public static Values createValues(long[][] longs) {
        return createValues(new LeafIterator(longs));
    }

    /**
     * Creates table of data values.
     * @param shorts resulting table of argument values
     * @return instance of Values
     */
    public static Values createValues(short[][] shorts) {
        return createValues(new LeafIterator(shorts));
    }

    /**
     * Creates new Values that represent a single invocation of macro test case - a set of values
     * for all arguments. For example, if you create a row in the following way
     * <pre>
     Values values = DataFactory.createRow( "a", "b", 1 );
     * </pre>
     * be prepared to have method with argument types - String, String, Integer.
     * Values above will produce only single micro test case - one invocation of method.
     * @param objects argument values for single micro test case
     * @return instance of Values
     */
    public static Values createRow(Object... objects) {
        return createValues( objects.length >0 ? new LeafIterator(new Object[][] {objects}) : new LeafIterator(new Object[0][0]));
    }

    /**
     * Unites passed Values altogether
     * @param vs values to unite
     * @return resulting Values
     */
    public static Values unite(Values... vs) {
        if (vs == null) return createColumn((Object)null);
        Values current = vs.length > 0 ? vs[0] : createColumn();
        for (int i = 1; i < vs.length; i++) current = current.unite(vs[i]);
        return current;
    }

    /**
     * Multiplies passed Values altogether
     * @param vs values to multiply
     * @return resulting Values
     */
    public static Values multiply(Values... vs) {
        if (vs == null) return createColumn((Object)null);
        Values current = vs.length > 0 ? vs[0] : createColumn();
        for (int i = 1; i < vs.length; i++)  current = current.multiply(vs[i]);
        return current;
    }

    /**
     * Pseudomultiplies passed Values altogether
     * @param vs values to pseudomultiply
     * @return resulting Values
     */
    public static Values pseudoMultiply(Values... vs) {
        if (vs == null) return createColumn((Object)null);
        Values current = vs.length > 0 ? vs[0] : createColumn();
        for (int i = 1; i < vs.length; i++)  current = current.pseudoMultiply(vs[i]);
        return current;
    }

    /**
     * Creates <code>Values</code> instance
     * wrapping provided <code>ValuesIterator</code>
     * @param iterator values iterator to use
     */
    public static Values createValues(ValuesIterator iterator) {
        return new ValuesImpl(iterator);
    }

    public static Values adaptData(Object o) {
        Values values;
        if (o == null) {
            values =  new ValuesImpl(EmptyIterator.EMPTY_ITERATOR);
        } else if (o instanceof Values) {
            values = (Values) o;
        } else if (o instanceof ValuesIterator) {
            values = DataFactory.createValues((ValuesIterator)o);
        } else if (o instanceof Object[][]) {
            values = DataFactory.createValues((Object[][]) o);
        } else if (o instanceof int[][]) {
            values = DataFactory.createValues((int[][]) o);
        } else if (o instanceof boolean[][]) {
            values = DataFactory.createValues((boolean[][]) o);
        } else if (o instanceof byte[][]) {
            values = DataFactory.createValues((byte[][]) o);
        } else if (o instanceof char[][]) {
            values = DataFactory.createValues((char[][]) o);
        } else if (o instanceof double[][]) {
            values = DataFactory.createValues((double[][]) o);
        } else if (o instanceof float[][]) {
            values = DataFactory.createValues((float[][]) o);
        } else if (o instanceof long[][]) {
            values = DataFactory.createValues((long[][]) o);
        } else if (o instanceof short[][]) {
            values = DataFactory.createValues((short[][]) o);
        } else if (o instanceof Object[]) {
            values = DataFactory.createColumn((Object[]) o);
        } else if (o instanceof int[]) {
            values = new ValuesImpl(new LeafIterator((int[]) o));
        } else if (o instanceof boolean[]) {
            values = new ValuesImpl(new LeafIterator((boolean[]) o));
        } else if (o instanceof byte[]) {
            values = new ValuesImpl(new LeafIterator((byte[]) o));
        } else if (o instanceof char[]) {
            values = new ValuesImpl(new LeafIterator((char[]) o));
        } else if (o instanceof double[]) {
            values = new ValuesImpl(new LeafIterator((double[]) o));
        } else if (o instanceof float[]) {
            values = new ValuesImpl(new LeafIterator((float[]) o));
        } else if (o instanceof long[]) {
            values = new ValuesImpl(new LeafIterator((long[]) o));
        } else if (o instanceof short[]) {
            values = new ValuesImpl(new LeafIterator((short[]) o));
        } else if (o instanceof List) {
            values = DataFactory.createColumn(((List)o).toArray());
        } else if (o instanceof Iterator) {
            List result = new ArrayList();
            ((Iterator<Object>)o).forEachRemaining(result::add);
            if (result.size() > 0 && result.get(0) instanceof Object[]) {
                values = new ValuesImpl(new CachedIterator((ArrayList<Object[]>) result));
            } else{
                values = DataFactory.createColumn(result.toArray());
            }
        } else {
            values = DataFactory.createColumn(o);
        }
        return values;
    }

}
