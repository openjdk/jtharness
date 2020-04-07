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

import static java.lang.System.arraycopy;

/**
 * A basic class to express any particular algebraic or pseudo-algebraic
 * operation with sets of values.<br>
 * Data from the left and from the right parts of expression are considered as flows of values.
 * Classes that extend this class define the way these two flows mix together in resulting flow of data.<p>
 * Another way to think about this class is to treat it as a filter.
 * @see Values
 * @see MultiplyIterator
 * @see PseudoMultiplyIterator
 * @see UniteIterator
 * @see IntersectIterator
 */
public abstract class NodeIterator extends ValuesIterator {

    private ValuesIterator left;
    private ValuesIterator right;

    /**
     * Defines the left part of expression.
     * This method is called by data engine.
     * Override if you want to do some additional actions.
     * @param left left part of expression
     */
    protected void setLeft(ValuesIterator left) {
        this.left = left;
    }

    /**
     * Returns the right part of expression
     * @return right part of expression
     */
    protected ValuesIterator getRight() {
        return right;
    }

    /**
     * Returns the left part of expression
     * @return left part of expression
     */
    protected ValuesIterator getLeft() {
        return left;
    }

    /**
     * Defines the right part of expression.
     * This method is called by data engine.
     * Override if you want to do some additional actions.
     * @param right right part of expression
     */
    protected void setRight(ValuesIterator right) {
        this.right = right;
    }

    /**
     * This method is called by engine to ask for a reset in data iteration.
     * In other words - pointer must be set to the beginning of the flow that current iterator produces.
     * By default this method performs a rollback both in left and right iterators
     * of the expression.
     * Please override if rollback must be done in some other, special way.
     * @see com.sun.tck.lib.tgf.UniteIterator
     * @see IntersectIterator
     */
    protected void rollback() {
        right.rollback();
        left.rollback();
    }

    /**
     * Provides current array of argument values.
     * By default arrays of values from left anr right sides
     * of expression are simply concatenated.
     * Please override this method if you have to mix them in some other way.
     * @return array of argument values
     * @see com.sun.tck.lib.tgf.UniteIterator
     * @see IntersectIterator
     */
    public Object[] next() {
        final Object[] leftObjects = left.next();
        final Object[] rightObjects = right.next();
        final Object[] result = new Object[leftObjects.length + rightObjects.length];
        arraycopy( leftObjects, 0, result, 0, leftObjects.length );
        arraycopy( rightObjects, 0, result, leftObjects.length, rightObjects.length );
        if (root) {
            shift();
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public ValuesIterator createCopy() {
        try {
            final NodeIterator iterator = getClass().getDeclaredConstructor().newInstance();
            iterator.setLeft(left.createCopy());
            iterator.setRight(right.createCopy());
            return iterator;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
