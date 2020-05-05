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

import com.sun.tck.lib.tgf.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static com.sun.tck.lib.tgf.DataFactory.*;

public class MassiveFiltering {

    /**
     * This test caused StackOverflowError when recursion was used in TransformingIterator
     */
    @Test
    public void wasStackOverflow_01() {
        final Iterable<Object[]> iterable =
                createColumn(0, 1, 3, 0, 4, 3)
                        .multiply(0, 1, 3, 1, 34, 45, 5)
                        .multiply(0, 1, 3, 4, 9, 0, 9)
                        .multiply(4, 5, 6, 7, 7, 8, 9, 8)
                        .multiply(1, 0, 1, 10, 4, 5, 5, 4)
                        .multiply(4, 3, 3, 4, 5, 6, 6, 7)
                        .<Integer, Integer, Integer, Integer, Integer, Integer>filter((a, b, c, d, e, f) -> null
                        );
        int counter = 0;
        for (Object[] objects : iterable) {
            counter++;
        }
        Assert.assertEquals(0, counter);
    }

    /**
     * This test caused StackOverflowError when recursion was used in TransformingIterator
     */
    @Test
    public void wasStackOverflow_02() {
        final Iterable<Object[]> iterable =
                createColumn(0, 0, 4, 3)
                        .multiply(5, 45, 34, 45, 5)
                        .multiply(0, 1, 3, 4, 9, 0, 9)
                        .multiply(4, 6, 74, 5, 5, 8, 9, 8)
                        .multiply(1, 0, 4, 5, 5, 4)
                        .multiply(4, 3, 6, 6, 7)
                        .multiply(4, 6, 7)
                        .multiply(4, 3, 3, 6, 7)
                        .multiply(4, 7)
                        .<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>filter((a, b, c, d, e, f, g, h, k) -> null
                        );
        int counter = 0;
        for (Object[] objects : iterable) {
            counter++;
        }
        Assert.assertEquals(0, counter);
    }

    /**
     * This test caused StackOverflowError when recursion was used in TransformingIterator
     */
    @Test
    public void wasStackOverflow_03() {
        final Iterable<Object[]> iterable =
                createColumn(0, 1, 3, 0, 4, 3, 0, 4, 3, 0, 4, 3)
                        .multiply(0, 1, 3, 1, 34, 1, 3, 1, 34, 1, 3, 1, 34, 45, 5, 4, 5, 5)
                        .multiply(0, 1, 3, 4, 9, 5, 8, 8, 0, 9)
                        .multiply(4, 5, 6, 7, 7, 6, 7, 7, 6, 7, 7, 8, 9, 8)
                        .multiply(1, 0, 1, 10, 4, 5, 5, 4)
                        .multiply(4, 3, 3, 4, 5, 6, 6, 6, 6, 6, 6, 7)
                        .<Integer, Integer, Integer, Integer, Integer, Integer>filter((a, b, c, d, e, f) -> null
                        );
        int counter = 0;
        for (Object[] objects : iterable) {
            counter++;
        }
        Assert.assertEquals(0, counter);
    }


    /**
     * This test caused StackOverflowError when recursion was used in TransformingIterator
     */
    @Test
    public void filteredOut_01() {
        final Iterable<Object[]> iterable =
                createColumn(0)
                        .multiply(45, 5)
                        .multiply()
                        .multiply(8)
                        .multiply()
                        .filter((Integer a) -> null
                        );
        int counter = 0;
        for (Object[] objects : iterable) {
            counter++;
        }
        Assert.assertEquals(0, counter);
    }


    /**
     * This test caused StackOverflowError when recursion was used in TransformingIterator
     */
    @Test
    public void filteredOut_02() {
        final Iterable<Object[]> iterable =
                createColumn(0)
                        .multiply(45, 5)
                        .multiply()
                        .multiply(8)
                        .<Integer>filter(x -> null)
                        .<Integer>filter(x -> null)
                        .<Integer>filter(x -> null)
                        .<Integer>filter(x -> null)
                        .<Integer>filter(a -> null)
                        .multiply(8);

        int counter = 0;
        for (Object[] objects : iterable) {
            counter++;
        }
        Assert.assertEquals(0, counter);
    }

    @Test
    public void test01() {
        final Iterable<Object[]> iterable =
                createColumn(0, 1, 3, 0, 4, 3)
                        .multiply(0, 1, 3, 1, 34, 45, 5)
                        .multiply(0, 1, 3, 4, 9, 0, 9)
                        .multiply(4, 5, 6, 7, 7, 8, 9, 8)
                        .multiply(1, 0, 1, 10, 4, 5, 5, 4)
                        .multiply(4, 3, 3, 4, 5, 6, 6, 7)
                        .<Integer, Integer, Integer, Integer, Integer, Integer>filter((a, b, c, d, e, f) -> "x"
                        );
        int counter = 0;
        for (Object[] objects : iterable) {
            counter++;
        }
        Assert.assertEquals(150528, counter);
    }

    @Test
    public void test02() {
        final Iterable<Object[]> iterable =
                createColumn(0, 1, 3, 0, 4, 3)
                        .multiply(0, 1, 3, 1, 34, 45, 5, 34, 45, 5, 34, 45, 5, 34, 45, 5)
                        .multiply(0, 1, 3, 4, 9, 0, 9)
                        .multiply(4, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 7, 8, 9, 8)
                        .multiply(1, 0, 1, 10, 4, 5, 5, 4)
                        .multiply(4, 3, 3, 4, 5, 6, 6, 6, 6, 6, 6, 6, 6, 7)
                        .<Integer, Integer, Integer, Integer, Integer, Integer>filter((a, b, c, d, e, f) -> 5
                        );
        int counter = 0;
        for (Object[] objects : iterable) {
            counter++;
        }
        Assert.assertEquals(1279488, counter);
    }

}
