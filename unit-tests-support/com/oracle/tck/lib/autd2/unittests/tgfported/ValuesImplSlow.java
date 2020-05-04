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
package com.oracle.tck.lib.autd2.unittests.tgfported;

import com.sun.tck.lib.tgf.LeafIterator;
import com.sun.tck.lib.tgf.NodeIterator;
import com.sun.tck.lib.tgf.Values;
import com.sun.tck.lib.tgf.ValuesIterator;

import java.util.*;

/**
 *
 */
public class ValuesImplSlow implements Values {

    private LinkedList<Object[]> data;

    private Values createCopy() {
        ValuesImplSlow values = new ValuesImplSlow();
        values.data = (LinkedList<Object[]>) this.data.clone();
        return values;
    }

    private ValuesImplSlow() { }

    private void setData(LinkedList<Object[]> data) {
        this.data = data;
    }

    public ValuesIterator copyOfIterator() {
        return new LeafIterator(data.toArray(new Object[data.size()][]));
    }

    public ValuesIterator iterator() {
        return new LeafIterator(data.toArray(new Object[data.size()][])) {
            { root = true; }
        };
    }

    public ValuesImplSlow(Object[][] data) {
        this.data = new LinkedList<>();
        this.data.addAll(Arrays.asList(data));
    }

    public ValuesImplSlow(Object... objs) {
        if (objs == null) {
            // workaround for "DataFactory.createColumn(null)"
            objs = new Object[]{null};
        } /*else if (objs.length == 0) {
            // workaround for createColumn("nothing passed in")
            LinkedList<Object[]> result = new LinkedList<Object[]>();
            result.add(new Object[]{});
            setData(result);
            return;
        }*/
        LinkedList<Object[]> result = new LinkedList<Object[]>();
        for (Object object : objs) {
            result.add(new Object[]{object});
        }
        setData(result);
    }

    public Values multiply(Values values) {
        LinkedList<Object[]> newData = new LinkedList<Object[]>();
//        if (data.size() == 0) {
//            data.add(new Object[] {});
//        }
//        if (((ValuesImplSlow)values).data.size() == 0) {
//            ((ValuesImplSlow)values).data.add(new Object[] {});
//        }
        for (Object[] thisObject : data) {
            for (Object[] thatObject : ValuesImplSlow.getData(values)) {
                List<Object> row = new ArrayList<Object>();
                if (thisObject.length > 0) row.addAll(Arrays.asList(thisObject));
                if (thatObject.length > 0) row.addAll(Arrays.asList(thatObject));
                newData.add(row.toArray());
            }
        }
        ValuesImplSlow implSlow = new ValuesImplSlow();
        implSlow.setData(newData);
        return implSlow;
    }

    private static LinkedList<Object[]> getData(Values values) {
        LinkedList<Object[]> result = new LinkedList<Object[]>();
        for (Object[] objects : values) {
            result.add(objects);
        }
        return result;
    }

    public Values pseudoMultiply(Values values) {
//        if (data.size() == 0) {
//            data.add(new Object[] {});
//        }
//        if (((ValuesImplSlow)values).data.size() == 0) {
//            ((ValuesImplSlow)values).data.add(new Object[] {});
//        }

        LinkedList<Object[]> newData = new LinkedList<Object[]>();
        LinkedList<Object[]> objects_1 = ValuesImplSlow.getData(this);
        LinkedList<Object[]> objects_2 = ValuesImplSlow.getData(values);

        int size_1 = objects_1.size();
        int size_2 = objects_2.size();


        int result_size;
        if (size_1 == 0 || size_2 == 0) {
            result_size = 0;
        } else {
            result_size = Math.max(size_1, size_2);
        }


        for (int i = 0; i < result_size; i++) {
            List<Object> row = new ArrayList<Object>();
            int index_1;
            int index_2;


            if (size_1 == result_size) {
                index_1 = i;                    // magic number :-)
                index_2 = (size_2 != 0 ? (i % size_2) : -1);
            } else {
                index_1 = (size_1 != 0 ? (i % size_1) : -1);
                index_2 = i;
            }

            if (index_1 >= 0) row.addAll(Arrays.asList(objects_1.get(index_1)));
            if (index_2 >= 0) row.addAll(Arrays.asList(objects_2.get(index_2)));

            newData.add(row.toArray());
        }

        ValuesImplSlow implSlow = new ValuesImplSlow();
        implSlow.setData(newData);
        return implSlow;
    }

    public Values unite(Values values) throws IllegalArgumentException {

        LinkedList<Object[]> newData = new LinkedList<Object[]>();
        newData.addAll(data);
        for (Object[] objects : values) {
                newData.add(objects);
            }
        ValuesImplSlow implSlow = new ValuesImplSlow();
        implSlow.setData(newData);
        return implSlow;
    }

    public Values unite(Object... objs) throws IllegalArgumentException {
        return this.unite(new ValuesImplSlow(objs));
    }

    public Values unite(Object[][] objs) throws IllegalArgumentException {
        return this.unite(new ValuesImplSlow(objs));
    }

    public Values intersect(Values values) {
        LinkedList<Object[]> result = new LinkedList<Object[]>();
        LinkedList<Object[]> thisData = purge(this);
        LinkedList<Object[]> thatData = purge(values);
        for (Object[] objects1 : thisData) {
            for (Object[] objects2 : thatData) {
                if (Arrays.deepEquals(objects1, objects2)) {
                    result.add(objects1);
                    break;
                }
            }
        }
        ValuesImplSlow implSlow = new ValuesImplSlow();

        implSlow.setData(result);
        return implSlow;
    }

    private boolean containsEmpty(LinkedList<Object[]> objects) {
        return objects.size() == 1 && Arrays.equals(new Object[]{}, objects.get(0));
    }

    public Values intersect(Object... objs) {
        return this.intersect(new ValuesImplSlow(objs));
    }

    public Values intersect(Object[][] objs) {
        return this.intersect(new ValuesImplSlow(objs));
    }

    public Values operate(Values values, NodeIterator nodeIterator) {
        throw new UnsupportedOperationException();
    }

    public Values filter(Object filter) {
        throw new UnsupportedOperationException();
    }

    public Values createCache() {
        return this.createCopy();
    }

    public Values reduceTo(double precent) {
        throw new UnsupportedOperationException();
    }

    private LinkedList<Object[]> purge(Values values) {
        LinkedList<Object[]> result = new LinkedList<Object[]>();
        for (Object[] aValueSet : values) {
            result.add(aValueSet);
        }
        return result;
    }

    public Values multiply(Object... objs) {
        return this.multiply(new ValuesImplSlow(objs));
    }

    public Values multiply(Object[][] objs) {
        return this.multiply(new ValuesImplSlow(objs));
    }

    public Values pseudoMultiply(Object... objs) {
        return this.pseudoMultiply(new ValuesImplSlow(objs));
    }

    public Values pseudoMultiply(Object[][] objs) {
        return this.pseudoMultiply(new ValuesImplSlow(objs));
    }

    public Values markNotApplicable() {
        throw new UnsupportedOperationException();
    }

    public Values markNotApplicable(String reason) {
        throw new UnsupportedOperationException();
    }

    public Values markNotApplicable(ExcludedIndices indices) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExcludedIndices getNotApplicableRowIndices() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNotApplicable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getReasonNotApplicaple() {
        throw new UnsupportedOperationException();
    }
}
