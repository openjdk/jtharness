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

import java.util.Arrays;

/**
 *  Iterates over a set of values in the built data expression.
 */
public class LeafIterator extends ValuesIterator {

    private final Object[][] objs;
    private int i;

    /**
     * Creates an instance wrapping passed two-dimensional array of values
     */
    public LeafIterator(Object[][] objs) {
        this.objs = Arrays.copyOf(objs, objs.length);
    }

    LeafIterator(int[][] ints) {
        objs = new Integer[ints.length][];
        for (int i = 0; i < ints.length; i++) {
            objs[i] = new Integer[ints[i].length];
            for (int j = 0; j < ints[i].length; j++) {
                objs[i][j] = ints[i][j];
            }
        }
    }

    LeafIterator(int[] ints) {
        this.objs = new Integer[ints.length][];
        for (int i = 0; i < ints.length; i++) {
            this.objs[i] = new Integer[]{ints[i]};
        }
    }

    LeafIterator(boolean[][] booleans) {
        objs = new Boolean[booleans.length][];
        for (int i = 0; i < booleans.length; i++) {
            objs[i] = new Boolean[booleans[i].length];
            for (int j = 0; j < booleans[i].length; j++) {
                objs[i][j] = booleans[i][j];
            }
        }
    }

    LeafIterator(boolean[] booleans) {
        this.objs = new Boolean[booleans.length][];
        for (int i = 0; i < booleans.length; i++) {
            this.objs[i] = new Boolean[]{booleans[i]};
        }
    }

    LeafIterator(byte[][] bytes) {
        objs = new Byte[bytes.length][];
        for (int i = 0; i < bytes.length; i++) {
            objs[i] = new Byte[bytes[i].length];
            for (int j = 0; j < bytes[i].length; j++) {
                objs[i][j] = bytes[i][j];
            }
        }
    }

    LeafIterator(byte[] bytes) {
        this.objs = new Byte[bytes.length][];
        for (int i = 0; i < bytes.length; i++) {
            this.objs[i] = new Byte[]{bytes[i]};
        }
    }

    LeafIterator(char[][] chars) {
        objs = new Character[chars.length][];
        for (int i = 0; i < chars.length; i++) {
            objs[i] = new Character[chars[i].length];
            for (int j = 0; j < chars[i].length; j++) {
                objs[i][j] = chars[i][j];
            }
        }
    }

    LeafIterator(char[] chars) {
        this.objs = new Character[chars.length][];
        for (int i = 0; i < chars.length; i++) {
            this.objs[i] = new Character[]{chars[i]};
        }
    }

    LeafIterator(double[][] doubles) {
        objs = new Double[doubles.length][];
        for (int i = 0; i < doubles.length; i++) {
            objs[i] = new Double[doubles[i].length];
            for (int j = 0; j < doubles[i].length; j++) {
                objs[i][j] = doubles[i][j];
            }
        }
    }

    LeafIterator(double[] doubles) {
        this.objs = new Double[doubles.length][];
        for (int i = 0; i < doubles.length; i++) {
            this.objs[i] = new Double[]{doubles[i]};
        }
    }

    LeafIterator(float[][] floats) {
        objs = new Float[floats.length][];
        for (int i = 0; i < floats.length; i++) {
            objs[i] = new Float[floats[i].length];
            for (int j = 0; j < floats[i].length; j++) {
                objs[i][j] = floats[i][j];
            }
        }
    }

    LeafIterator(float[] floats) {
        this.objs = new Float[floats.length][];
        for (int i = 0; i < floats.length; i++) {
            this.objs[i] = new Float[]{floats[i]};
        }
    }

    LeafIterator(long[][] longs) {
        objs = new Long[longs.length][];
        for (int i = 0; i < longs.length; i++) {
            objs[i] = new Long[longs[i].length];
            for (int j = 0; j < longs[i].length; j++) {
                objs[i][j] = longs[i][j];
            }
        }
    }

    LeafIterator(long[] longs) {
        this.objs = new Long[longs.length][];
        for (int i = 0; i < longs.length; i++) {
            this.objs[i] = new Long[]{longs[i]};
        }
    }

    LeafIterator(short[][] shorts) {
        objs = new Short[shorts.length][];
        for (int i = 0; i < shorts.length; i++) {
            objs[i] = new Short[shorts[i].length];
            for (int j = 0; j < shorts[i].length; j++) {
                objs[i][j] = shorts[i][j];
            }
        }
    }

    LeafIterator(short[] shorts) {
        this.objs = new Short[shorts.length][];
        for (int i = 0; i < shorts.length; i++) {
            this.objs[i] = new Short[]{shorts[i]};
        }
    }

    /**
     * Creates an instance wrapping passed array of values
     */
    public LeafIterator(Object... objs) {
        if (objs == null) {
            // workaround for "DataFactory.createColumn(null)"
            objs = new Object[] {null};
        }
        this.objs = new Object[objs.length][];
        for (int i = 0; i < objs.length; i++) {
            this.objs[i] = new Object[]{objs[i]};
        }
    }

    /**
     * {@inheritDoc}
     */
    public void shift() {
        i++;
    }

    /**
     * {@inheritDoc}
     */
    protected void rollback() {
        i = 0;
    }

    /**
     * {@inheritDoc}
     */
    protected ValuesIterator createCopy() {
        return new LeafIterator(objs);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        return i < objs.length;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] next() {
        Object[] result = objs[i];
        if (root) {
            shift();
        }
        return result;
    }

    /**
     * Returns current index
     */
    public String toString() {
        return String.valueOf(i);
    }
}
