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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Iterator taking care of filtering and transforming data flows.
 */
public class TransformingIterator extends ValuesIterator {

    private ValuesIterator parentIterator;
    private ValuesIterator childIterator;

    private Object filter;
    private Method filteringMethod;

    /**
     * Creates new instance
     * @param parentIterator data supplying iterator
     * @param filter object to be used as a filter
     */
    public TransformingIterator(ValuesIterator parentIterator, Object filter) {
        this.parentIterator = parentIterator;
        this.filter = filter;
        setupFilteringMethod(filter);
    }

    /**
     * First found method returning Values will be considered as filtering one
     * @param filter object to search method in
     */
    private void setupFilteringMethod(Object filter) {
        final List<Method> methods = ReflectionUtils.getAllMethods(filter.getClass());
        for (Method method : methods) {
            if (method.getAnnotation(Transform.class) != null) {
                filteringMethod = method;
                filteringMethod.setAccessible(true);
                return;
            }
        }
        throw new RuntimeException("No method annotated with @Transform was found. Unable to create filter");
    }

    /**
     * Encapsulating child iterator for delaying its set-up until the real iteration starts.
     * Otherwise (without lazy init) there are unwanted warm-up calls to the filtering function.
     */
    private ValuesIterator getChildIterator() {
        if (childIterator == null) {
            setupChildIterator();
            if (childIterator == null) {
                childIterator = EmptyIterator.EMPTY_ITERATOR;
            }
        }
        return childIterator;
    }

    /**
     * {@inheritDoc}
     * Implementation intentionally does not use recursion to avoid stack overflow.
     */
    public void shift() {
        // shift to the next filtered value
        getChildIterator().shift();
        // if the child is exhausted then proceed to the next one;
        // repeat it until we find a non-empty child
        // or exhaust the parent
        while (!getChildIterator().hasNext()) {
            // the child is exhausted, proceed to the next value to filter
            parentIterator.shift();
            if (!parentIterator.hasNext()) {
                // the parent is exhausted, no more values to shift to
                break;
            }
            // found a new value to filter, create new child
            setupChildIterator();
            // if the child starts with a skipped value...
            if (!getChildIterator().hasNext()) {
                // ... go to the first non-skipped
                // (we're returning to the start of the while loop after that,
                // so if the child has no non-skipped values we'll continue with the next value in parent)
                getChildIterator().shift();
            } else {
                break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void rollback() {
        parentIterator.rollback();
        childIterator = null;
    }

    /**
     * {@inheritDoc}
     */
    protected ValuesIterator createCopy() {
        return new TransformingIterator(parentIterator.createCopy(), filter);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        // if no next value then try to find one
        if (!getChildIterator().hasNext()) {
            shift();
        }
        return getChildIterator().hasNext();
    }

    /**
     * {@inheritDoc}
     */
    public Object[] next() {
        while ( getChildIterator() instanceof EmptyIterator && parentIterator.hasNext() ) {
            shift();
        }
        final Object[] result = getChildIterator().next();
        if (root) {
            shift();
        }
        return result;
    }

    private void setupChildIterator() {
        if (!parentIterator.hasNext()) {
            return;
        }
        final Object[] sourceValues = parentIterator.next();
        try {
            Object result;
            if (isArrayTypeArgument()) {
                if (sourceValues.length == 1 && sourceValues[0].getClass().isArray()) {
                    result = filteringMethod.invoke(filter, sourceValues);
                } else {
                    result = filteringMethod.invoke(filter, (Object) sourceValues);
                }
            } else {
                if (sourceValues.length > 1 && filteringMethod.getParameterTypes().length == 1) {
                    result = filteringMethod.invoke(filter, (Object) sourceValues);
                } else {
                    try {
                        result = filteringMethod.invoke(filter, sourceValues);
                    } catch (InvocationTargetException e) {
                        if (e.getCause() instanceof ClassCastException) {
                            // making another attempt
                            result = filteringMethod.invoke(filter, (Object) sourceValues);
                        } else {
                            throw e;
                        }
                    } catch (IllegalArgumentException e) {
                        // making yet another attempt
                        result = filteringMethod.invoke(filter, (Object) sourceValues);
                    }
                }
            }
            final Values values = DataFactory.adaptData(result);
            childIterator = values.copyOfIterator();
        } catch (Throwable e) {
            throw e instanceof RuntimeException
                    ? (RuntimeException)e
                    : new RuntimeException(e);
        }
    }

    private boolean isArrayTypeArgument() {
        return filteringMethod.getParameterTypes().length == 1 &&
                filteringMethod.getParameterTypes()[0].isArray();
    }

    /**
     * Returns string representation of a resulting child iterator
     */
    public String toString() {
        return getChildIterator().toString();
    }

}
