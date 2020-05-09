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
package com.oracle.tck.lib.autd2.unittests.tgfported.testruntimeskipping;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.sun.tck.lib.Assert.*;
import static com.sun.tck.lib.tgf.Values.*;

/**
 *
 */
public class NotApplicableIndices {


    @Test
    public void indexIAE() {
        assertThrows(IllegalArgumentException.class, () -> ExcludedRange.of(-1));
        assertThrows(IllegalArgumentException.class, () -> ExcludedRange.of(-4));
        assertThrows(IllegalArgumentException.class, () -> ExcludedRange.of(-12341232344L));
        assertThrows(IllegalArgumentException.class, () -> ExcludedRange.of(Long.MIN_VALUE));
    }

    @Test
    public void indexEqHc() {
        ExcludedIndex index = ExcludedRange.of(1234);
        assertTrue(index.equals(index));
        assertTrue(index.equals(ExcludedRange.of(1234)));

        assertFalse(index.equals(null));
        assertFalse(index.equals("1234"));
        assertFalse(index.equals(ExcludedRange.of(234, 1234)));
        assertFalse(index.equals(ExcludedRange.of(4, 34)));
        assertFalse(index.equals(ExcludedRange.of(12345)));

        assertTrue(index.hashCode() == index.hashCode());
        assertTrue(index.hashCode() == ExcludedRange.of(1234).hashCode());
        assertTrue(index.hashCode() != ExcludedRange.of(2345).hashCode());
        assertTrue(index.hashCode() != ExcludedRange.of(0).hashCode());
        assertTrue(index.hashCode() != ExcludedRange.of(14, 1234).hashCode());
    }

    @Test
    public void indexExcluded() {
        assertTrue(ExcludedRange.of(0).excluded(0));
        assertTrue(ExcludedRange.of(12).excluded(12));
        assertTrue(ExcludedRange.of(Long.MAX_VALUE).excluded(Long.MAX_VALUE));
        assertFalse(ExcludedRange.of(0).excluded(1));
        assertFalse(ExcludedRange.of(1).excluded(0));
        assertFalse(ExcludedRange.of(1).excluded(-1));
        assertFalse(ExcludedRange.of(12).excluded(Long.MAX_VALUE));
        assertFalse(ExcludedRange.of(Long.MAX_VALUE).excluded(Long.MIN_VALUE));
    }

    @Test
    public void rangeIAE() {
        assertThrows(IllegalArgumentException.class, () -> ExcludedRange.of(-1, 12));
        assertThrows(IllegalArgumentException.class, () -> ExcludedRange.of(-1, -13));
        assertThrows(IllegalArgumentException.class, () -> ExcludedRange.of(1, -13));
        assertThrows(IllegalArgumentException.class, () -> ExcludedRange.of(1, 1));
        assertThrows(IllegalArgumentException.class, () -> ExcludedRange.of(Long.MIN_VALUE, 1));
    }

    @Test
    public void indexRangeEqHc() {
        ExcludedRange index = ExcludedRange.of(1234, 4321);
        assertTrue(index.equals(index));
        assertTrue(index.equals(ExcludedRange.of(1234, 4321)));
        assertTrue(ExcludedRange.of(1234, 4321).equals(ExcludedRange.of(1234, 4321)));

        assertFalse(index.equals(null));
        assertFalse(index.equals("1234"));
        assertFalse(index.equals(String.class));
        assertFalse(index.equals(ExcludedRange.of(1234, 1235)));
        assertFalse(index.equals(ExcludedRange.of(1234, 4322)));
        assertFalse(index.equals(ExcludedRange.of(0, Long.MAX_VALUE)));
        assertFalse(index.equals(ExcludedRange.of(1234)));
        assertFalse(index.equals(ExcludedRange.of(4321)));
        assertFalse(index.equals(ExcludedRange.of(0)));

        assertTrue(index.hashCode() == index.hashCode());
        assertTrue(index.hashCode() == ExcludedRange.of(1234, 4321).hashCode());
        assertTrue(index.hashCode() != ExcludedRange.of(45, 47).hashCode());
        assertTrue(index.hashCode() != ExcludedRange.of(0, 1).hashCode());
        assertTrue(index.hashCode() != ExcludedRange.of(1234).hashCode());
        assertTrue(index.hashCode() != ExcludedRange.of(4321).hashCode());
    }

    @Test
    public void rangeExcluded() {
        assertTrue(ExcludedRange.of(0, 1).excluded(0));
        assertTrue(ExcludedRange.of(0, 1).excluded(1));
        assertFalse(ExcludedRange.of(0, 1).excluded(-1));
        assertFalse(ExcludedRange.of(0, 1).excluded(2));

        assertTrue(ExcludedRange.of(0, Long.MAX_VALUE).excluded(0));
        assertTrue(ExcludedRange.of(0, Long.MAX_VALUE).excluded(Long.MAX_VALUE));
        assertTrue(ExcludedRange.of(0, Long.MAX_VALUE).excluded(1));
        assertTrue(ExcludedRange.of(0, Long.MAX_VALUE).excluded(5));
        assertTrue(ExcludedRange.of(0, Long.MAX_VALUE).excluded(1234));
        assertFalse(ExcludedRange.of(0, Long.MAX_VALUE).excluded(-1));

        assertTrue(ExcludedRange.of(15, 16).excluded(15));
        assertTrue(ExcludedRange.of(15, 16).excluded(16));
        assertFalse(ExcludedRange.of(15, 16).excluded(14));
        assertFalse(ExcludedRange.of(15, 16).excluded(17));

        assertTrue(ExcludedRange.of(5, 25).excluded(15));
        assertTrue(ExcludedRange.of(5, 25).excluded(5));
        assertTrue(ExcludedRange.of(5, 25).excluded(25));
        assertFalse(ExcludedRange.of(5, 25).excluded(2));
        assertFalse(ExcludedRange.of(5, 25).excluded(4));
        assertFalse(ExcludedRange.of(5, 25).excluded(26));

    }


    @Test
    public void indicesExclude_empty() {
        ExcludedIndices indices = ExcludedIndices.create();
        checkNoIndices(indices, true);
        assertFalse(indices.isExcluded(0));
        assertFalse(indices.isExcluded(Long.MAX_VALUE));
        assertFalse(indices.isExcluded(-1));
        assertFalse(indices.isExcluded(-12345));

    }

    void checkNoIndices(ExcludedIndices excludedIndices, boolean noIndicesExpected) {
        Assert.assertEquals(noIndicesExpected, excludedIndices.noIndicesSpecified());
    }

    @Test
    public void indicesExclude_singleIndex() {
        ExcludedIndices excludedIndices = ExcludedIndices.create().exclude(0);
        checkNoIndices(excludedIndices, false);
        assertTrue(excludedIndices.isExcluded(0));
        assertFalse(excludedIndices.isExcluded(1));
        assertFalse(excludedIndices.isExcluded(-1));
    }

    @Test
    public void indicesExclude_twoIndices() {
        ExcludedIndices indices = ExcludedIndices.create().exclude(0).exclude(1);
        checkNoIndices(indices, false);
        assertTrue(indices.isExcluded(0));
        assertTrue(indices.isExcluded(1));
        assertFalse(indices.isExcluded(-1));
        assertFalse(indices.isExcluded(2));
        assertFalse(indices.isExcluded(Long.MAX_VALUE));
    }

    @Test
    public void indicesExclude_sixIndices() {
        checkZeroToFiveCovered(ExcludedIndices.create().exclude(0).exclude(1).exclude(2).exclude(3).exclude(4).exclude(5));
    }

    @Test
    public void indicesExclude_threeIndices() {
        ExcludedIndices indices = ExcludedIndices.create().exclude(0).exclude(Long.MAX_VALUE).exclude(5);
        checkNoIndices(indices, false);

        assertTrue(indices.isExcluded(0));
        assertTrue(indices.isExcluded(Long.MAX_VALUE));
        assertTrue(indices.isExcluded(5));
        assertFalse(indices.isExcluded(-1));
        assertFalse(indices.isExcluded(6));
        assertFalse(indices.isExcluded(Long.MIN_VALUE));
        assertFalse(indices.isExcluded(Long.MAX_VALUE - 1));
    }

    @Test
    public void indicesExclude_oneRange() {
        ExcludedIndices indices = ExcludedIndices.create().exclude(0, 1);
        checkNoIndices(indices, false);

        assertTrue(indices.isExcluded(0));
        assertTrue(indices.isExcluded(1));
        assertFalse(indices.isExcluded(-1));
        assertFalse(indices.isExcluded(2));
        assertFalse(indices.isExcluded(Long.MAX_VALUE));
    }

    @Test
    public void indicesExclude_rangeAndIndex() {
        ExcludedIndices indices = ExcludedIndices.create().exclude(0, 5).exclude(700);
        checkNoIndices(indices, false);

        assertTrue(indices.isExcluded(0));
        assertTrue(indices.isExcluded(1));
        assertTrue(indices.isExcluded(2));
        assertTrue(indices.isExcluded(3));
        assertTrue(indices.isExcluded(4));
        assertTrue(indices.isExcluded(5));
        assertTrue(indices.isExcluded(700));
        assertFalse(indices.isExcluded(-1));
        assertFalse(indices.isExcluded(6));
        assertFalse(indices.isExcluded(699));
        assertFalse(indices.isExcluded(701));
        assertFalse(indices.isExcluded(Long.MAX_VALUE));
        assertFalse(indices.isExcluded(Long.MIN_VALUE));
    }

    @Test
    public void testRangeZeroFive() {
        checkZeroToFiveCovered(ExcludedIndices.create().exclude(0, 4).exclude(2, 5));

        checkZeroToFiveCovered(ExcludedIndices.create().exclude(0, 2).exclude(3, 5));
        checkZeroToFiveCovered(ExcludedIndices.create().exclude(0, 1).exclude(3, 5).exclude(2, 3));
        checkZeroToFiveCovered(ExcludedIndices.create().exclude(0, 5).exclude(3, 5).exclude(2, 3));
    }

    @Test
    public void test5to8and13to25() {
        check5To8And13To25(ExcludedIndices.create().exclude(5, 8).exclude(13, 25));
    }

    @Test
    public void indicesExclude_twoRangesDontoverlap_01() {
        ExcludedIndices indices = ExcludedIndices.create().exclude(0, 3).exclude(4, 5);
        checkZeroToFiveCovered(indices);
    }

    private void checkZeroToFiveCovered(ExcludedIndices indices) {
        checkNoIndices(indices, false);
        assertTrue(indices.isExcluded(0));
        assertTrue(indices.isExcluded(1));
        assertTrue(indices.isExcluded(2));
        assertTrue(indices.isExcluded(3));
        assertTrue(indices.isExcluded(4));
        assertTrue(indices.isExcluded(5));
        assertFalse(indices.isExcluded(-1));
        assertFalse(indices.isExcluded(-10));
        assertFalse(indices.isExcluded(6));
        assertFalse(indices.isExcluded(699));
        assertFalse(indices.isExcluded(701));
        assertFalse(indices.isExcluded(Long.MAX_VALUE));
        assertFalse(indices.isExcluded(Long.MIN_VALUE));
    }

    private void check5To8And13To25(ExcludedIndices indices) {
        checkNoIndices(indices, false);

        assertFalse(indices.isExcluded(0));
        assertFalse(indices.isExcluded(1));
        assertFalse(indices.isExcluded(4));
        assertTrue(indices.isExcluded(5));
        assertTrue(indices.isExcluded(6));
        assertTrue(indices.isExcluded(7));
        assertTrue(indices.isExcluded(8));
        assertFalse(indices.isExcluded(9));
        assertFalse(indices.isExcluded(10));
        assertFalse(indices.isExcluded(11));
        assertFalse(indices.isExcluded(12));
        for (int i = 13; i <= 25; i++) {
            assertTrue(indices.isExcluded(i));
        }

        assertFalse(indices.isExcluded(26));
        assertFalse(indices.isExcluded(699));
        assertFalse(indices.isExcluded(701));
        assertFalse(indices.isExcluded(Long.MAX_VALUE));
        assertFalse(indices.isExcluded(Long.MIN_VALUE));
    }

    @Test
    public void indicesExclude_twoRangesDontoverlap_02() {
        ExcludedIndices indices = ExcludedIndices.create().exclude(0, 2).exclude(4, 5);
        check_twoRangesDontoverlap_02(indices);
    }

    @Test
    public void indicesExclude_threeRangesDontoverlap_02_1() {
        ExcludedIndices indices = ExcludedIndices.create().exclude(0, 1).exclude(2).exclude(4, 5);
        check_twoRangesDontoverlap_02(indices);
    }

    private void check_twoRangesDontoverlap_02(ExcludedIndices indices) {
        checkNoIndices(indices, false);

        assertTrue(indices.isExcluded(0));
        assertTrue(indices.isExcluded(1));
        assertTrue(indices.isExcluded(2));
        assertFalse(indices.isExcluded(3));
        assertTrue(indices.isExcluded(4));
        assertTrue(indices.isExcluded(5));
        assertFalse(indices.isExcluded(-1));
        assertFalse(indices.isExcluded(6));
        assertFalse(indices.isExcluded(Long.MAX_VALUE));
        assertFalse(indices.isExcluded(Long.MIN_VALUE));
    }


}
