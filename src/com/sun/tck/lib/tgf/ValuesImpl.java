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

class ValuesImpl implements Values {

    private ValuesIterator valuesIterator;
    private boolean notApplicable;
    private String  reasonOfNotApplicable;
    private ExcludedIndices notApplicableRowIndices = ExcludedIndices.create();

    public Values filter(Object filter) {
        return new ValuesImpl(new TransformingIterator(this.copyOfIterator(), filter));
    }

    public Values createCache() {
        return new ValuesImpl(new CachedIterator(this));
    }

    public Values reduceTo(double ratio) {
        return new ValuesImpl( new ReducingIterator(this, ratio));
    }

    ValuesImpl(ValuesIterator valuesIterator) {
        this.valuesIterator = valuesIterator;
    }

    @Override
    public boolean isNotApplicable() {
        return notApplicable;
    }

    @Override
    public String getReasonNotApplicaple() {
        return reasonOfNotApplicable;
    }

    public ValuesIterator iterator() {
        // needed in case of reusing the same data many times
        valuesIterator = copyOfIterator();
        valuesIterator.root = true;
        return valuesIterator;
    }

    @Override
    public ValuesIterator copyOfIterator() {
        return valuesIterator.createCopy();
    }

    public Values operate(Values values, NodeIterator nodeIterator) {
        nodeIterator.setLeft(this.copyOfIterator());
        nodeIterator.setRight(values.copyOfIterator());
        return new ValuesImpl(nodeIterator);
    }

    public Values multiply(Values values) {
        return operate(values, new MultiplyIterator());
    }

    public Values pseudoMultiply(Values values) {
        return operate(values, new PseudoMultiplyIterator());
    }

    public Values unite(Values values) throws IllegalArgumentException {
        return operate(values, new UniteIterator());
    }

    public Values intersect(Values values) {
        return operate(values, new IntersectIterator());
    }

    // todo test delegation
    public Values unite(Object... objs) throws IllegalArgumentException {
        return unite(DataFactory.createColumn(objs));
    }

    // todo test delegation
    public Values unite(Object[][] objs) throws IllegalArgumentException {
        return unite(DataFactory.createValues(objs));
    }

    // todo test delegation
    public Values intersect(Object... objs) {
        return intersect(DataFactory.createColumn(objs));
    }

    // todo test delegation
    public Values intersect(Object[][] objs) {
        return intersect(DataFactory.createValues(objs));
    }

    // todo test delegation
    public Values pseudoMultiply(Object... objs) {
        return pseudoMultiply(DataFactory.createColumn(objs));
    }

    // todo test delegation
    public Values pseudoMultiply(Object[][] objs) {
        return pseudoMultiply(DataFactory.createValues(objs));
    }

    // todo test delegation
    public Values multiply(Object... objs) {
        return multiply(DataFactory.createColumn(objs));
    }

    // todo test delegation
    public Values multiply(Object[][] objs) {
        return multiply(DataFactory.createValues(objs));
    }

    public String toString() {
        return valuesIterator.toString();
    }

    public Values markNotApplicable() {
        return markNotApplicable("");
    }

    public Values markNotApplicable(String reason) {
       notApplicable = true;
       reasonOfNotApplicable = reason;
       return this;
    }

    public Values markNotApplicable(ExcludedIndices indices) {
        notApplicableRowIndices = indices;
        return this;
    }

    public ExcludedIndices getNotApplicableRowIndices() {
        return notApplicableRowIndices;
    }
}
