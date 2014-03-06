/*
 * $Id$
 *
 * Copyright (c) 2001, 2010, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.util.I18NResourceBundle;

/**
 * A test filter that filters tests according to their prior execution status.
 */
public class StatusFilter extends TestFilter {
    /**
     * Create a filter that filters tests according to their execution status.
     * @param statusValues an array of booleans, indexed by the standard
     * {@link com.sun.javatest.Status Status} constants, that indicate
     * which status values should be accepted (passed) by the filter,
     * and which should be rejected.
     * @param trt a test result table in which to look up the value of
     * the tests being checked by the filter
     * @see com.sun.javatest.Status#PASSED
     * @see com.sun.javatest.Status#FAILED
     * @see com.sun.javatest.Status#ERROR
     * @see com.sun.javatest.Status#NOT_RUN
     */
    public StatusFilter(boolean[] statusValues, TestResultTable trt) {
        if (statusValues == null || trt == null)
            throw new NullPointerException();

        if (statusValues.length != Status.NUM_STATES)
            throw new IllegalArgumentException();

        this.statusValues = statusValues;
        this.trt = trt;
    }

    /**
     * Get the array of booleans, indexed by the standard
     * {@link com.sun.javatest.Status Status} constants, that indicate
     * which status values should be accepted (passed) by the filter,
     * and which should be rejected.
     * @return an array of booleans indicating which status values should be accepted by the filter
     * @see com.sun.javatest.Status#PASSED
     * @see com.sun.javatest.Status#FAILED
     * @see com.sun.javatest.Status#ERROR
     * @see com.sun.javatest.Status#NOT_RUN
     */
    public boolean[] getStatusValues() {
        return statusValues;
    }

    /**
     * Get the test result table in which to look up tests to
     * see if they should be accepted by the filter or not.
     * @return a test result table
     */
    public TestResultTable getTestResultTable() {
        return trt;
    }

    @Override
    public String getName() {
        return i18n.getString("statusFilter.name");
    }

    @Override
    public String getDescription() {
        return i18n.getString("statusFilter.description");
    }

    @Override
    public String getReason() {
        return i18n.getString("statusFilter.reason");
    }

    @Override
    public boolean accepts(TestDescription td) throws Fault {
        TestResult tr = null;

        synchronized (trt) {
           tr = trt.lookup(td);
        }

        if (tr == null)
            throw new Fault(i18n, "statusFilter.cantFindTest", td.getRootRelativeURL());
        Status s = tr.getStatus();
        if (s == null)
            throw new Fault(i18n, "statusFilter.noStatus", td.getRootRelativeURL());
        return statusValues[s.getType()];
    }

    @Override
    public boolean accepts(TestResult tr) throws Fault {
        assert(tr != null);

        Status s = tr.getStatus();
        if (s == null)
            throw new Fault(i18n, "statusFilter.noStatus", tr.getTestName());
        return statusValues[s.getType()];
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if ( !(o instanceof StatusFilter))
            return false;

        StatusFilter other = (StatusFilter) o;
        for (int i = 0; i < Status.NUM_STATES; i++) {
            if (statusValues[i] != other.statusValues[i])
                return false;
        }

        return (trt == other.trt);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        if (statusValues != null) {
            for (boolean v : statusValues) {
                hash = 67 * hash + (v ? 1 : 0);
            }
        }
        hash = 67 * hash + (this.trt != null ? this.trt.hashCode() : 0);
        return hash;
    }

    private boolean[] statusValues;
    private TestResultTable trt;
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(ExcludeListFilter.class);
}
