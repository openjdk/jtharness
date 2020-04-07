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


import java.util.HashSet;
import java.util.Set;

/**
 * Container for collection of data values.
 * Provides basic algebraic operations and returns a new instance of <code>Values</code>
 * as the result of calculation.
 *
 * @see com.sun.tck.lib.tgf.Runner
 */
public interface Values extends Iterable<Object[]> {

    /**
     * A shortcut method.
     * Fully multiplies this set of values with given array of objects.
     * This method is equal to the following method call:
     * <pre>
       multiply(DataFactory.createValues(objs))
     * </pre>
     * @param objs array of objects to multiply with
     * @return resulting set of values
     */
    Values multiply(Object... objs);

    /**
     * A shortcut method.
     * This method is equal to the following method call:
     * <pre>
       multiply(DataFactory.createValues(objs))
     * </pre>
     * @param objs objects to multiply with
     * @return resulting set of values
     */
    Values multiply(Object[][] objs);

    /**
     * Fully multiplies this set of values with the given one.
     * <p>A sample code:<br/>
     * <pre>
     public Values data() {
         return DataFactory.createValues("a", "b").multiply(1, 2);
     }

     <code>@TestCase</code>
     <code>@TestData("data")</code>
     public void test(String s, int i) {
         System.out.println("s = " + s + ", i = " + i);
     }
     * </pre>
     * Will produce the following output:
     * <pre>
     s = a, i = 1
     s = a, i = 2
     s = b, i = 1
     s = b, i = 2
     * </pre>
     * @param values set of values to multiply with
     * @return resulting set of values
     */
    Values multiply(Values values);

    /**
     * A shortcut method.
     * Aligns given array of objects with rows stored in this
     * Values. Resulting number of rows will be the maximum between
     * number of rows in this values and given number of objects.
     * Data set returned by this method is equal to the one returned by the following method call:
     * <pre>
       pseudoMultiply(DataFactory.createValues(objs))
     * </pre>
     * @param objs array of objects to pseudoMultiply with
     * @return resulting set of values
     */
    Values pseudoMultiply(Object... objs);

    /**
     * A shortcut method.
     * Aligns given array of objects with rows stored in this
     * Values. Resulting number of rows will be the maximum between
     * number of rows in this values and given number of objects.
     * This method is equal to the following method call:
     * <pre>
       pseudoMultiply(DataFactory.createValues(objs))
     * </pre>
     * @param objs objects to pseudoMultiply with
     * @return resulting set of values
     */
    Values pseudoMultiply(Object[][] objs);

    /**
     * Aligns given rows from given ValueDet with rows stored in this
     * Values. Resulting number of rows will be the maximum between
     * number of rows in this values and number of rows
     * stored in given Value Set.
     * In other words - this operation could be called "pseudo-multiplication" in JTF terms.
     * <p>A sample code:<br/>
     * <pre>
     public Values data() {
         return DataFactory.createValues("a", "b").pseudoMultiply(1, 2);
     }

     <code>@TestCase</code>
     <code>@TestData("data")</code>
     public void test(String s, int i) {
         System.out.println("s = " + s + ", i = " + i);
     }
     * </pre>
     * Will produce the following output:
     * <pre>
     s = a, i = 1
     s = b, i = 2
     * </pre>
     * Say, if we will create
     * <pre>
     DataFactory.createValues("a", "b", "c").pseudoMultiply(1, 2, 3, 4, 5, 6, 7)
     * </pre>
     * then the following table could be drawn as illustration of result, where "x" is a match, "." is not.
     * <pre>
         1   2   3   4   5   6   7
     a   x   .   .   x   .   .   x
     b   .   x   .   .   x   .   .
     c   .   .   x   .   .   x   .
     * </pre>
     * @param values set of values to pseudoMultiply with
     * @return resulting set of values
     */
    Values pseudoMultiply(Values values);

    /**
     * Unites rows of argument values with rows stored in a given value set.
     * Rows from this Values will go first, then rows from given Values
     * <p>A sample code:<br/>
     * <pre>
     public Values data() {
         return DataFactory.createValues("a", "b").unite(
                DataFactory.createValues("c", "d"));
     }

     <code>@TestCase</code>
     <code>@TestData("data")</code>
     public void test(String s) {
         System.out.println("s = " + s );
     }
     * </pre>
     * Will produce the following output:
     * <pre>
     s = a
     s = b
     s = c
     s = d
     * </pre>
     *
     * Note: if number of columns in this Values object and in the argument are not equal
     * then data set with various row length will be produced. Such data set may cause IllegalArgumentException
     * when used as data for a non-varargs test case or in other similar situations.
     *
     * @param values set of values to unite with
     * @return resulting set of values
     */
    Values unite(Values values);

    /**
     * A shortcut method.
     * Unites rows of argument values with rows stored in a given value set.
     * This method is equal to the following method call:
     * <pre>
       unite(DataFactory.createValues(objs))
     * </pre>
     *
     * Note: if number of columns in this Values object is not equal to 1
     * then data set with various row length will be produced. Such data set may cause IllegalArgumentException
     * when used as data for a non-varargs test case or in other similar situations.
     *
     * @param objs objects to unite with
     * @return resulting set of values
     */
    Values unite(Object... objs);

    /**
     * A shortcut method.
     * Unites rows of argument values with rows stored in a given value set.
     * This method is equal to the following method call:
     * <pre>
       unite(DataFactory.createValues(objs))
     * </pre>
     *
     * Note: if number of columns in this Values object and lengths of the rows of the objs array are not equal
     * then data set with various row length will be produced. Such data set may cause IllegalArgumentException
     * when used as data for a non-varargs test case or in other similar situations.
     *
     * @param objs objects to unite with
     * @return resulting set of values
     */
    Values unite(Object[][] objs);

    /**
     * Intersects rows of argument values with rows stored in a given value set.
     * <p>A sample code:<br/>
     * <pre>
     public Values intersect() {
         return DataFactory.createValues("a", "b", "c").intersect(
                DataFactory.createValues("b", "c", "d"));
     }

     <code>@TestCase</code>
     <code>@TestData("intersect")</code>
     public void test(String s) {
         System.out.println("s = " + s );
     }
     * </pre>
     * Will produce the following output:
     * <pre>
     s = b
     s = c
     * </pre>
     * @param values set of values to intersect with
     * @return resulting set of values
     */
    Values intersect(Values values);

    /**
     * A shortcut method.
     * This method is equal to the following method call:
     * <pre>
       intersect(DataFactory.createValues(objs))
     * </pre>
     * @param objs set of values to intersect with
     * @return resulting set of values
     */
    Values intersect(Object... objs);

    /**
     * A shortcut method.
     * This method is equal to the following method call:
     * <pre>
       intersect(DataFactory.createValues(objs))
     * </pre>
     * @param objs values to intersect with
     * @return resulting set of values
     */
    Values intersect(Object[][] objs);

    /**
     * This method provides possibility to create user-defined,
     * custom operations upon sets of values.
     * Implementation of all algebraic methods create their corresponding iterators and
     * call this method.
     * If test developer wants to mix sets of values in a very special way he/she has to
     * extend <code>NodeIterator</code> and pass it here.
     * @param values set of values to intersect with
     * @return resulting set of values
     * @param nodeIterator iterator that mixes in its own way
     * this current set of values and a given one.
     * Please see sources of classes from <code>com.sun.tck.lib.tgf.algebra</code> package as sample implementations.
     * @see NodeIterator
     */
    Values operate(Values values, NodeIterator nodeIterator);

    /**
     * Performs a filtering for data flow produced by this instance.
     * Provided filter may be both produce additional data basing on provided and reduce, filtering out
     * unnecessary data items.
     * <p> For example, it may be used to filter arrays containing only min, pref, max integers that
     * satisfy condition <code>min &lt;= pref &lt;= max</code> out of all possible combinations between three arrays of integers.
     * <pre>
     final Values values = createColumn(INTS).multiply(INTS).multiply(INTS).filter(
             new Object() {
                 <code>@Transform</code> Values create(int min, int pref, int max) {
                     if (min &lt;= pref && pref &lt;= max) {
                         return createRow(min, pref, max);
                     } else {
                         return null;
                     }
                 }
             });
     * </pre>
     * If three integers don't satisfy the condition then <code>null</code> is returned - that means
     * theese three integers are filtered out.
     * @param filter a filter or transformator to use
     * @return resulting Values
     */
    Values filter(Object filter);

    /**
     * A helper functional interface for lambdas operating with one argument
     * which are accepted by method <code>filter(Transformer1)</code>.
     * It is not designed to be implemented explicitly.
     */
    @FunctionalInterface
    interface Transformer1<T1> {
        Object transform(T1 t1) throws Throwable;
    }

    /**
     * A helper functional interface for lambdas operating with two arguments
     * which are accepted by method <code>filter(Transformer2)</code>.
     * It is not designed to be implemented explicitly.
     */
    @FunctionalInterface
    interface Transformer2<T1, T2> {
        Object transform(T1 t1, T2 t2) throws Throwable;
    }

    /**
     * A helper functional interface for lambdas operating with three arguments
     * which are accepted by method <code>filter(Transformer3)</code>.
     * It is not designed to be implemented explicitly.
     */
    @FunctionalInterface
    interface Transformer3<T1, T2, T3> {
        Object transform(T1 t1, T2 t2, T3 t3) throws Throwable;
    }

    /**
     * A helper functional interface for lambdas operating with four arguments
     * which are accepted by method <code>filter(Transformer4)</code>.
     * It is not designed to be implemented explicitly.
     */
    @FunctionalInterface
    interface Transformer4<T1, T2, T3, T4> {
        Object transform(T1 t1, T2 t2, T3 t3, T4 t4) throws Throwable;
    }

    /**
     * A helper functional interface for lambdas operating with five arguments
     * which are accepted by method <code>filter(Transformer5)</code>.
     * It is not designed to be implemented explicitly.
     */
    @FunctionalInterface
    interface Transformer5<T1, T2, T3, T4, T5> {
        Object transform(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) throws Throwable;
    }

    /**
     * A helper functional interface for lambdas operating with six arguments
     * which are accepted by method <code>filter(Transformer6)</code>.
     * It is not designed to be implemented explicitly.
     */
    @FunctionalInterface
    interface Transformer6<T1, T2, T3, T4, T5, T6> {
        Object transform(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) throws Throwable;
    }

    /**
     * A helper functional interface for lambdas operating with seven arguments
     * which are accepted by method <code>filter(Transformer7)</code>.
     * It is not designed to be implemented explicitly.
     */
    @FunctionalInterface
    interface Transformer7<T1, T2, T3, T4, T5, T6, T7> {
        Object transform(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7) throws Throwable;
    }

    /**
     * A helper functional interface for lambdas operating with eight arguments
     * which are accepted by method <code>filter(Transformer8)</code>.
     * It is not designed to be implemented explicitly.
     */
    @FunctionalInterface
    interface Transformer8<T1, T2, T3, T4, T5, T6, T7, T8> {
        Object transform(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8) throws Throwable;
    }

    /**
     * A helper functional interface for lambdas operating with nine arguments
     * which are accepted by method <code>filter(Transformer9)</code>.
     * It is not designed to be implemented explicitly.
     */
    @FunctionalInterface
    interface Transformer9<T1, T2, T3, T4, T5, T6, T7, T8, T9> {
        Object transform(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9) throws Throwable;
    }

    /**
     * Method accepts lambda with two arguments as a transforming function, a typical code looks like:
     * <code>Values v = DataFactory.createColumn("a", "b", "c").filter((String s) -> s.toUpperCase());</code>
     */
    default <T1> Values filter(Transformer1<T1> tr1) {
        return filter(new Object() {
            @Transform Object transform(T1 t1) throws Throwable{
                return tr1.transform(t1);
            }
        });
    }

    /**
     * Method accepts lambda with two arguments as a transforming function, a typical code looks like:
     * <pre> Values v = DataFactory.createColumn("a", "b", "c")
     *                  .pseudoMultiply(1, 2, 3)
     *                  .filter((String s, Integer i) -> s + i);</pre>
     */
    default <T1, T2> Values filter(Transformer2<T1, T2> tr2) {
        return filter(new Object() {
            @Transform Object transform(T1 t1, T2 t2) throws Throwable{
                return tr2.transform(t1, t2);
            }
        });
    }

    /**
     * Method accepts lambda with three arguments as a transforming function, a typical code looks like:
     * <pre> Integer[] component = {0, 100, 255};
     *       Values colors =  createColumn(component).multiply(component).multiply(component).filter(
     *                               (Integer r, Integer g, Integer b) -> new Color(r, g, b)
     *                        );</pre>
     */
    default <T1, T2, T3> Values filter(Transformer3<T1, T2, T3> tr3) {
        return filter(new Object() {
            @Transform Object transform(T1 t1, T2 t2, T3 t3) throws Throwable{
                return tr3.transform(t1, t2, t3);
            }
        });
    }

    /**
     * Method accepts lambda with four arguments as a transforming function.
     */
    default <T1, T2, T3, T4> Values filter(Transformer4<T1, T2, T3, T4> tr4) {
        return filter(new Object() {
            @Transform Object transform(T1 t1, T2 t2, T3 t3, T4 t4) throws Throwable {
                return tr4.transform(t1, t2, t3, t4);
            }
        });
    }

    /**
     * Method accepts lambda with five arguments as a transforming function.
     */
    default <T1, T2, T3, T4, T5> Values filter(Transformer5<T1, T2, T3, T4, T5> tr5) {
        return filter(new Object() {
            @Transform Object transform(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) throws Throwable {
                return tr5.transform(t1, t2, t3, t4, t5);
            }
        });
    }

    /**
     * Method accepts lambda with six arguments as a transforming function.
     */
    default <T1, T2, T3, T4, T5, T6> Values filter(Transformer6<T1, T2, T3, T4, T5, T6> tr6) {
        return filter(new Object() {
            @Transform Object transform(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) throws Throwable {
                return tr6.transform(t1, t2, t3, t4, t5, t6);
            }
        });
    }

    /**
     * Method accepts lambda with seven arguments as a transforming function.
     */
    default <T1, T2, T3, T4, T5, T6, T7> Values filter(Transformer7<T1, T2, T3, T4, T5, T6, T7> tr7) {
        return filter(new Object() {
            @Transform Object transform(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7) throws Throwable {
                return tr7.transform(t1, t2, t3, t4, t5, t6, t7);
            }
        });
    }

    /**
     * Method accepts lambda with eight arguments as a transforming function.
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8> Values filter(Transformer8<T1, T2, T3, T4, T5, T6, T7, T8> tr8) {
        return filter(new Object() {
            @Transform Object transform(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8) throws Throwable {
                return tr8.transform(t1, t2, t3, t4, t5, t6, t7, t8);
            }
        });
    }

    /**
     * Method accepts lambda with nine arguments as a transforming function.
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, T9> Values filter(Transformer9<T1, T2, T3, T4, T5, T6, T7, T8, T9> tr9) {
        return filter(new Object() {
            @Transform Object transform(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9) throws Throwable {
                return tr9.transform(t1, t2, t3, t4, t5, t6, t7, t8, t9);
            }
        });
    }

    /** Iterates over all values that this instance provides and and create a caches of them.
     * This method is useful when it's more optimal to have a pre-built set of data
     * than calculate each set of values on demand.
     * Practice shows that this method should be mostly applied when there's a reused data structure
     * that uses {@link AbstractValue} wrapper, but there's no need to create
     * a fresh data instance for every iteration of some testcase.
     * @see AbstractValue
     * @return fully built array of data
     */
    Values createCache();

    /**
     * Method performs a reduction in number of rows of data
     * provided by instance of {@link Values}.
     * Reduction is performed uniformly.
     * For the following sample:
     * <pre>
     Values values =
             DataFactory.createColumn(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).reduceTo(0.5);
     * </pre>
     * Remaining elements will be [ 2, 4, 6, 8, 10 ]
     * @param precent percentage of remaining number of rows after reduction
     * @return reduced {@code Values}
     */
    Values reduceTo(double precent);

    /**
     * Marks this set of data as not applicable for testcases.
     */
    Values markNotApplicable();

    /**
     * Marks this set of data as not applicable for testcases due to specified reason.
     */
    Values markNotApplicable(String reason);

    /**
     * Marks the given selection of data row indices as not applicable.
     * @param indices
     */
    Values markNotApplicable(ExcludedIndices indices);


    /**
     * Marks the given row indices as not applicable
     * @param indices indices of rows to mark as not applicable
     * @return this values instance
     */
    default Values markNotApplicable(long... indices) {
        ExcludedIndices excluded = ExcludedIndices.create();
        for (long index : indices) {
            excluded.exclude(index);
        }
        return markNotApplicable(excluded);
    }

    /**
     * Returns row indices which were marked as not applicable
     */
    ExcludedIndices getNotApplicableRowIndices();

    /**
     * Returns true if data is marked as not applicable, false otherwise
     * @return true if data is marked as not applicable, false otherwise
     */
    boolean isNotApplicable();

    /**
     * If data is marked as not applicable then returns string describing reason of this. Otherwise returns null.
     * @return string describing reason why this data is marked as not applicable or null if data is applicable
     */
    String getReasonNotApplicaple();

    /**
     * Creates a copy of this Values object's iterator with no side effects
     * (no changing of state of this Values object or it's child iterators).
     * Use this method to get a copy of this object's iterator when you are not going to iterate over it immediately
     * (e.g. in transforming operations such as {@link #multiply(Values)}.
     * @return Copy of this object's iterator
     */
    ValuesIterator copyOfIterator();

    /**
     * Returns this Values object's iterator and optionally prepares it to be iterated over
     * (state changes may occur).
     * Use this method when you're going to iterate over this Values.
     * @return This Values object's iterator
     */
    @Override
    ValuesIterator iterator();

    static class ExcludedIndex {
        protected long index;

        private ExcludedIndex(long index) {
            if (index < 0) {
                throw new IllegalArgumentException("Given index " + index + " is negative");
            }
            this.index = index;
        }

        public boolean excluded(long value) {
            return this.index == value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ExcludedIndex that = (ExcludedIndex) o;

            if (index != that.index) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return (int) (index ^ (index >>> 32));
        }
    }

    static class ExcludedRange extends ExcludedIndex {
        private long rangeEnd;

        private ExcludedRange(long begin, long end) {
            super(begin);
            if (end < 0) {
                throw new IllegalArgumentException("Range end " + end + " is negative");
            }
            if (begin > end) {
                throw new IllegalArgumentException("Range start " + begin + " is greater than range end " + end);
            }
            if (begin == end) {
                throw new IllegalArgumentException("Range start " + begin + " is equal to range end. Please use ExcludedIndex::of instead");
            }
            this.rangeEnd = end;
        }

        public static ExcludedRange of(long begin, long end) {
            return new ExcludedRange(begin, end);
        }

        public static ExcludedIndex of(long index) {
            return new ExcludedIndex(index);
        }

        @Override
        public boolean excluded(long value) {
            return value >= super.index && value <= rangeEnd;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            ExcludedRange that = (ExcludedRange) o;

            if (rangeEnd != that.rangeEnd) {
                return false;
            }
            if (super.index != that.index) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (int) (rangeEnd ^ (rangeEnd >>> 32));
            result = 31 * result + (int) (super.index ^ (super.index >>> 32));
            return result;
        }
    }

    public static class ExcludedIndices {
        private ExcludedIndices() {
        }

        public static ExcludedIndices create() {
            return new ExcludedIndices();
        }

        private final Set<ExcludedIndex> indices = new HashSet<>();

        public ExcludedIndices exclude(long index) {
            indices.add(ExcludedRange.of(index));
            return this;
        }

        public ExcludedIndices exclude(long begin, long end) {
            indices.add(ExcludedRange.of(begin, end));
            return this;
        }

        public boolean noIndicesSpecified() {
            return indices.isEmpty();
        }

        public boolean isExcluded(long index) {
            for (ExcludedIndex range : indices) {
                if (range.excluded(index)) {
                    return true;
                }
            }
            return false;
        }
    }

}
