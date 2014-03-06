/*
 * $Id$
 *
 * Copyright (c) 2002, 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.report;

import java.util.Comparator;

import com.sun.javatest.TestDescription;
import com.sun.javatest.TestResult;

class TestResultsByFileComparator implements Comparator {
    public int compare(Object o1, Object o2) {
        TestResult a = (TestResult)o1;
        TestResult b = (TestResult)o2;

        // The simplest way to compare two URLs would be to compare their
        // external forms, but generating them generates garbage at a
        // prodigious rate.  So, since we know URLs are represented
        // as  protocol  host  post  file  ref  we compare the constituent
        // parts in order.

        try {
            TestDescription da = a.getDescription();
            TestDescription db = b.getDescription();

            int rf = compare(da.getFile().getPath(), db.getFile().getPath());
            if (rf != 0)
                return rf;
            int rr = compare(da.getId(), db.getId());
            return rr;
        }
        catch (TestResult.Fault e) {
            // bad files go at the end ?
            return 1;
        }
    }

    private static int compare(String a, String b) {
        if (a == null && b == null)
            return 0;

        if (a == null)
            return -1;

        if (b == null)
            return +1;

        return a.compareTo(b);
    }
}
