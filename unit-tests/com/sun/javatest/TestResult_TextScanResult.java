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
package com.sun.javatest;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestResult_TextScanResult {

    @Test
    public void empty()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("");
        assertEquals(0, scan.numBackslashes);
        assertEquals(0, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }

    @Test
    public void space()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan(" ");
        assertEquals(0, scan.numBackslashes);
        assertEquals(1, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertTrue(scan.needsFinalNewline);
    }

    @Test
    public void n()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("\n");
        assertEquals(0, scan.numBackslashes);
        assertEquals(1, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }

    @Test
    public void r()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("\r");
        assertEquals(0, scan.numBackslashes);
        assertEquals(1, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }

    @Test
    public void space_n()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan(" \n");
        assertEquals(0, scan.numBackslashes);
        assertEquals(1, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }

    @Test
    public void space_r()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan(" \r");
        assertEquals(0, scan.numBackslashes);
        assertEquals(1, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }

    @Test
    public void space_rn()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan(" \r\n");
        assertEquals(0, scan.numBackslashes);
        assertEquals(1, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }

    @Test
    public void space_nr()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan(" \n\r");
        assertEquals(0, scan.numBackslashes);
        assertEquals(2, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }

    @Test
    public void rr()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("\r\r");
        assertEquals(0, scan.numBackslashes);
        assertEquals(2, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }

    @Test
    public void rn()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("\r\n");
        assertEquals(0, scan.numBackslashes);
        assertEquals(1, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }

    @Test
    public void nr()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("\n\r");
        assertEquals(0, scan.numBackslashes);
        assertEquals(2, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }

    @Test
    public void rnrn()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("\r\n\r\n");
        assertEquals(0, scan.numBackslashes);
        assertEquals(2, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }

    /**
     * n, rn, r
     */
    @Test
    public void nrnr()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("\n\r\n\r");
        assertEquals(0, scan.numBackslashes);
        assertEquals(3, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }

    @Test
    public void nnnn()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("\n\n\n\n");
        assertEquals(0, scan.numBackslashes);
        assertEquals(4, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }

    @Test
    public void nn()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("\n\n");
        assertEquals(0, scan.numBackslashes);
        assertEquals(2, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }

    @Test
    public void blanckLine()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan(" ");
        assertEquals(0, scan.numBackslashes);
        assertEquals(1, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertTrue(scan.needsFinalNewline);
    }

    @Test
    public void oneSymbol()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("x");
        assertEquals(0, scan.numBackslashes);
        assertEquals(1, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertTrue(scan.needsFinalNewline);
    }

    @Test
    public void oneUnicodeSymbol()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("\u1234");
        assertEquals(0, scan.numBackslashes);
        assertEquals(1, scan.numLines);
        assertEquals(1, scan.numNonASCII);
        assertTrue(scan.needsEscape);
        assertTrue(scan.needsFinalNewline);
    }

    @Test
    public void twoUnicodeSymbols()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("\u5432abc\u1234");
        assertEquals(0, scan.numBackslashes);
        assertEquals(1, scan.numLines);
        assertEquals(2, scan.numNonASCII);
        assertTrue(scan.needsEscape);
        assertTrue(scan.needsFinalNewline);
    }

    @Test
    public void twoLines_n()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("first\nsecond");
        assertEquals(0, scan.numBackslashes);
        assertEquals(2, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertTrue(scan.needsFinalNewline);
    }

    @Test
    public void twoLines_r()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("first\rsecond");
        assertEquals(0, scan.numBackslashes);
        assertEquals(2, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertTrue(scan.needsFinalNewline);
    }

    @Test
    public void twoLines_rn()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("first\r\nsecond");
        assertEquals(0, scan.numBackslashes);
        assertEquals(2, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertTrue(scan.needsFinalNewline);
    }

    @Test
    public void twoLines_n_endsWith_n()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("first\nsecond\n");
        assertEquals(0, scan.numBackslashes);
        assertEquals(2, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }

    @Test
    public void twoLines_n_endsWith_rn()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("first\nsecond\r\n");
        assertEquals(0, scan.numBackslashes);
        assertEquals(2, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }

    @Test
    public void twoLines_n_endsWith_r()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("first\nsecond\r");
        assertEquals(0, scan.numBackslashes);
        assertEquals(2, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }

    @Test
    public void twoLines_r_endsWith_r()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("first\rsecond\r");
        assertEquals(0, scan.numBackslashes);
        assertEquals(2, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }

    @Test
    public void twoLines_rn_endsWith_n()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("first\r\nsecond\n");
        assertEquals(0, scan.numBackslashes);
        assertEquals(2, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }

    @Test
    public void twoLines_rn_endsWith_rn()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("first\r\nsecond\r\n");
        assertEquals(0, scan.numBackslashes);
        assertEquals(2, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }


    @Test
    public void threeLines_n()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("first\nsecond\nthird");
        assertEquals(0, scan.numBackslashes);
        assertEquals(3, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertTrue(scan.needsFinalNewline);
    }

    @Test
    public void threeLines_n_endsWith_n()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("first\nsecond\nthird\n");
        assertEquals(0, scan.numBackslashes);
        assertEquals(3, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }

    @Test
    public void threeLines_n_r()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("first\nsecond\rthird");
        assertEquals(0, scan.numBackslashes);
        assertEquals(3, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertTrue(scan.needsFinalNewline);
    }

    @Test
    public void threeLines_r_n()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("first\rsecond\nthird");
        assertEquals(0, scan.numBackslashes);
        assertEquals(3, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertTrue(scan.needsFinalNewline);
    }

    @Test
    public void threeLines_r_n_endsWith_r()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("first\rsecond\nthird\r");
        assertEquals(0, scan.numBackslashes);
        assertEquals(3, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }

    @Test
    public void threeLines_r_n_endsWith_n()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("first\rsecond\nthird\n");
        assertEquals(0, scan.numBackslashes);
        assertEquals(3, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }

    @Test
    public void threeLines_n_n_endsWith_n()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("first\rsecond\nthird\n");
        assertEquals(0, scan.numBackslashes);
        assertEquals(3, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }

    @Test
    public void threeLines_r_n_endsWith_rn()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("first\rsecond\nthird\r\n");
        assertEquals(0, scan.numBackslashes);
        assertEquals(3, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertFalse(scan.needsFinalNewline);
    }

    @Test
    public void threeLines_r_r()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("first\rsecond\rthird");
        assertEquals(0, scan.numBackslashes);
        assertEquals(3, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertTrue(scan.needsFinalNewline);
    }

    @Test
    public void threeLines_n_rn()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("first\nsecond\r\nthird");
        assertEquals(0, scan.numBackslashes);
        assertEquals(3, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertTrue(scan.needsFinalNewline);
    }

    @Test
    public void threeLines_rn_rn()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("first\r\nsecond\r\nthird");
        assertEquals(0, scan.numBackslashes);
        assertEquals(3, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertTrue(scan.needsFinalNewline);
    }

    @Test
    public void threeLines_rn_r()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("first\r\nsecond\rthird");
        assertEquals(0, scan.numBackslashes);
        assertEquals(3, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertTrue(scan.needsFinalNewline);
    }

    @Test
    public void fourLines_nr_r()  {
        TestResult.TextScanResult scan = TestResult.TextScanResult.scan("first\n\rsecond\rthird");
        assertEquals(0, scan.numBackslashes);
        assertEquals(4, scan.numLines);
        assertEquals(0, scan.numNonASCII);
        assertFalse(scan.needsEscape);
        assertTrue(scan.needsFinalNewline);
    }

}

