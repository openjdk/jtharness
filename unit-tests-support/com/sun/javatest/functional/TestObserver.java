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

package com.sun.javatest.functional;

import com.sun.javatest.Harness;
import com.sun.javatest.Status;

import static junit.framework.Assert.*;

public class TestObserver implements Harness.Observer {

    private static int[] stats = {-1, -1, -1, -1};
    private static int skipped = -1;

    @Override
    public void notifyOfTheFinalStats(int skipped, int[] stats) {
        TestObserver.skipped = skipped;
        TestObserver.stats = stats;
    }

    public static int[] getStats() {
        return stats;
    }

    public static void assertFinalStats(int expectedPassed, int expectedFailed, int expectedErrors, int expectedNotRun, int expectedSkipped) {
        assertEquals("Unexpected number of PASSED tests", expectedPassed, stats[Status.PASSED]);
        assertEquals("Unexpected number of FAILED tests", expectedFailed, stats[Status.FAILED]);
        assertEquals("Unexpected number of ERROR tests", expectedErrors, stats[Status.ERROR]);
        assertEquals("Unexpected number of NOT_RUN tests", expectedNotRun, stats[Status.NOT_RUN]);
        assertEquals("Unexpected number of skipped tests", expectedSkipped, TestObserver.skipped);
    }
}
