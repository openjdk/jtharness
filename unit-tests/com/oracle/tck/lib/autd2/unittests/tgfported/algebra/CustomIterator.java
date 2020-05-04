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

import com.oracle.tck.lib.autd2.unittests.ValuesComparison;
import com.sun.tck.lib.tgf.DataFactory;
import com.sun.tck.lib.tgf.NodeIterator;
import com.sun.tck.lib.tgf.Values;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CustomIterator {

    @Test
    public void test_1_1() {
        Values v1 = DataFactory.createColumn(1, 2, 3, 4, 5, 6, 7);
        Values v2 = DataFactory.createColumn(8, 9, 10, 11, 12, 13   );
        Values values = v1.operate(v2, new OddPairs());
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] { 1, 8 } );
        expected.add( new Object[] { 3, 10 } );
        expected.add( new Object[] { 5, 12 } );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_1_1_cached() {
        Values v1 = DataFactory.createColumn(1, 2, 3,  4,  5,   6, 7);
        Values v2 = DataFactory.createColumn(8, 9, 10, 11, 12, 13   );
        Values values = v1.operate(v2, new OddPairs());
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] { 1, 8 } );
        expected.add( new Object[] { 3, 10 } );
        expected.add( new Object[] { 5, 12 } );
        ValuesComparison.compare(values.createCache(), expected);
    }

    public static class OddPairs extends NodeIterator {
        public void shift() {
            getRight().shift();
            if (getRight().hasNext()) {
                getRight().shift();
            }
            getLeft().shift();
            if (getLeft().hasNext()) {
                getLeft().shift();
            }
        }

        public boolean hasNext() {
            return getLeft().hasNext() && getRight().hasNext();
        }
    }

/*
    @Test
    public void test_90degreeRotator_1() {
        Values v1 = DataFactory.createColumn(1, 2, 3);
        Values values = v1.operate(v1, new Rotator90() );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] { 1, 2, 3 } );
        ValuesComparison.compare(values, expected);
    }

    @Test
    public void test_90degreeRotator_2() {
        Values v1 = DataFactory.unite(DataFactory.createRow(1, 2, 3),
                                      DataFactory.createRow(4, 5, 6));
        Values values = v1.operate(v1, new Rotator90() );
        List<Object[]> expected = new ArrayList<Object[]>();
        expected.add( new Object[] { 4, 1 } );
        expected.add( new Object[] { 5, 2 } );
        expected.add( new Object[] { 6, 3 } );
        ValuesComparison.compare(values, expected);
    }


    public static class Rotator90 extends NodeIterator {

        protected ArrayList<Object[]> data;

        public void shift() {
            iterator.shift();
        }

        public boolean hasNext() {
            if (iterator == null) {
                initIterator();
            }
            return iterator.hasNext();
        }

        private void initIterator() {
            ValuesIterator iterator = getLeft();
            ArrayList<Object[]> objects = new ArrayList<Object[]>();
            while (iterator.hasNext()) {
                objects.add( iterator.next() );
            }
            int objSize = objects.size();
            int oneObjLength = objects.get(0).length;
            Object[][] cache = new Object[oneObjLength][objSize];
            for (int i = 0; i< oneObjLength; i++) {
                for (int j = 0; i<objSize; i++) {
                    cache[i][j] = objects.get(objSize - j - 1)[i];
                }
            }
            // look at how cached iterator is constructed
        }
    }
*/

}
