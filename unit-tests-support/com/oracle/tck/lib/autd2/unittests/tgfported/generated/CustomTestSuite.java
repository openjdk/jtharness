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
package com.oracle.tck.lib.autd2.unittests.tgfported.algebra.generated;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.AssertionFailedError;

import java.util.*;

public class CustomTestSuite implements Test {


    private final Set<CustomTestSuite> suites = new HashSet<CustomTestSuite>();
    private final Multiplier multiplier;
    private ValuesIterator valuesIterator;
    private int numTestCases;

    public CustomTestSuite(Multiplier multiplier, Map<String, Engine.Dimension> dimensions) {
        this.multiplier = multiplier;
        numTestCases = 1;
        for (Map.Entry<String, Engine.Dimension> entry : dimensions.entrySet()) {
            Engine.Dimension value = entry.getValue();
            Object[] objects = value.getAspects().toArray();
            numTestCases *= objects.length;
            if (valuesIterator == null) {
                valuesIterator = new Leaf(entry.getKey(), objects);
            } else {
                MultiplyIterator it = new MultiplyIterator();
                it.setLeft( this.valuesIterator.createCopy());
                it.setRight( new Leaf(entry.getKey(), objects) );
                valuesIterator = it;
            }
        }
    }

    public void run(final TestResult result) {
        valuesIterator.root = true;
        while (valuesIterator.hasNext()) {
            Map<String, Object> map = valuesIterator.next();
            result.startTest(CustomTestSuite.this);
            try {
                if (result.shouldStop()) {
                    return;
                }
                multiplier.multiply(map);
            }
            catch (AssertionFailedError e) {
                result.addFailure(this, e);
            }
            catch (ThreadDeath e) {
                throw e;
            }
            catch (Throwable e) {
                result.addError(this, e);
            }
            result.endTest(CustomTestSuite.this);
        }
    }

    public String toString() {
        return valuesIterator.toString();
    }

    void addSuite(CustomTestSuite test) {
        suites.add(test);
    }

    public int countTestCases() {
        return numTestCases;
    }

    public int testCount() {
        return suites.size();
    }


    static class MultiplyIterator extends NodeIterator {
        public void shift() {
            getRight().shift();
            if (!getRight().hasNext()) {
                getLeft().shift();
                if (getLeft().hasNext()) {
                    getRight().rollback();
                }
            }
        }
        public boolean hasNext() {
            return getLeft().hasNext() && getRight().hasNext();
        }
    }


    public static abstract class NodeIterator extends ValuesIterator {

        private ValuesIterator left;
        private ValuesIterator right;
        private static final String COMMA = ", ";

        protected void setLeft(ValuesIterator left) {
            this.left = left;
        }

        protected ValuesIterator getRight() {
            return right;
        }

        protected ValuesIterator getLeft() {
            return left;
        }

        protected void setRight(ValuesIterator right) {
            this.right = right;
        }

        protected void rollback() {
            right.rollback();
            left.rollback();
        }

        public Map<String, Object> next() {
            final Map<String, Object> leftObjects = left.next();
            final Map<String, Object> rightObjects = right.next();

            HashMap<String, Object> result = new HashMap<String, Object>();
            result.putAll(leftObjects);
            result.putAll(rightObjects);

            if (root) {
                shift();
            }
            return result;
        }

        public String toString() {
            return left.toString() + COMMA + right.toString();
        }

        protected ValuesIterator createCopy() {
            try {
                NodeIterator iterator = getClass().newInstance();
                iterator.setLeft(left.createCopy());
                iterator.setRight(right.createCopy());
                return iterator;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static abstract class ValuesIterator implements Iterator<Map<String, Object>> {

        protected boolean root;

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public abstract void shift();

        protected abstract void rollback();

        protected abstract ValuesIterator createCopy();

    }

    static class Leaf extends ValuesIterator {

        private final Object[] objs;
        private int i;
        private String name;
        private String toString = "Undefined";

        public Leaf(String name, Object... objs) {
            this.name = name;
            this.objs = objs;
            this.i = 0;
        }

        public Map<String, Object> next() {
            HashMap<String, Object> result = new HashMap<String, Object>();
            result.put(name, objs[i]);
            toString =  name + " = " + objs[i];
            if (root) {
                shift();
            }
            return result;
        }

        public void shift() {
            i++;
        }

        protected void rollback() {
            i = 0;
        }

        protected ValuesIterator createCopy() {
            return new Leaf(name, objs);
        }

        public boolean hasNext() {
            return i < objs.length;
        }

        public String toString() {
            return toString;
        }
    }




}
