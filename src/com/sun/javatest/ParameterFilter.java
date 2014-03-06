/*
 * $Id$
 *
 * Copyright (c) 2001, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;

import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.StringArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This filter knows how to deal with the the Parameters interface to get
 * the necessary filtering effect.
 *
 * @see com.sun.javatest.Parameters
 */

public class ParameterFilter extends ObservableTestFilter {
    public ParameterFilter() {
        super();
    }

    // ------- TestFilter ---------
    @Override
    public String getName() {
        return i18n.getString("pFilter.name");
    }

    @Override
    public String getDescription() {
        return i18n.getString("pFilter.desc");
    }

    @Override
    public String getReason() {
        return i18n.getString("pFilter.reason");
    }

    @Override
    public boolean accepts(TestDescription td) throws Fault {
        return accepts(td, null);
    }

    @Override
    public boolean accepts(TestDescription td, TestFilter.Observer o) throws Fault {
        return accepts(td, null, o);
    }

    @Override
    public boolean accepts(TestResult tr, TestFilter.Observer o) throws Fault {
        try {
            TestDescription td = tr.getDescription();
            return accepts(td, tr, o);
        }
        catch (TestResult.Fault f) {
            throw new Fault(i18n, "pFilter.trNoTd", f);
        }

    }

    private boolean accepts(TestDescription td, TestResult tr, TestFilter.Observer o)
            throws Fault {
        // need to handle the initial url filter as a special case
        rl.lock();
        try {
            if (filters == null || filters.length == 0) {
                if (iurlFilter == null) {
                    return true;
                } else {
                    boolean result = iurlFilter.accepts(td);
                    if (!result && o != null) {
                        o.rejected(td, iurlFilter);
                    }
                }
            }

            for (int i = 0; i < filters.length; i++) {
                boolean result = false;
                if (tr != null && filters[i] instanceof StatusFilter) {
                    try {
                        // optimization allow code to avoid searching for the TR object
                        result = filters[i].accepts(tr);
                    } catch (TestResult.Fault f) {
                        // fallback to old behavior
                        result = filters[i].accepts(td);
                    }
                } else {
                    result = filters[i].accepts(td);
                }
                if (!result) {
                    if (o != null) {
                        o.rejected(td, filters[i]);
                    }

                    return false;
                }
            }   // for

            // check initial URL filter
            if (iurlFilter != null && !iurlFilter.accepts(td)) {
                if (o != null) {
                    o.rejected(td, iurlFilter);
                }

                return false;
            }
        } finally {
            rl.unlock();
        }

        // test accepted
        return true;
    }

    // ------- Composite overrides ---------
    /**
     * Gets the set of filters that the parameters have supplied.
     * Depending on the initial url setting, an InitialUrlFilter may or may not
     * be included in this set.  The returned array has already be shallow copied.
     *
     * @return The filters in use.  This is affected by the isFilterTests()
     *         state.
     * @see com.sun.javatest.InitialUrlFilter
     */
    public TestFilter[] getTestFilters() {
        TestFilter[] copy = null;

        rl.lock();
        try {

            // allocate shallow copy array
            // change size depending on whether url filter is to be included
            if (iurlFilter != null) {
                copy = new TestFilter[filters.length + 1];
                copy[copy.length-1] = iurlFilter;
            }
            else {
                copy = new TestFilter[filters.length];
            }

            System.arraycopy(filters, 0, copy, 0, filters.length);
        } finally {
            rl.unlock();
        }
        return copy;
    }

    // ---------- methods for this class -----------

    /**
     * Should be called whenever the parameters or filters inside
     * may have changed.
     */
    public void update(Parameters p) {
        wl.lock();
        boolean wasUpdated = false;
        try {
            if (p == null) {
                boolean isUpdated = (filters != null);
                filters = null;
                iurlFilter = null;
                if (isUpdated) {
                    notifyUpdated(this);
                }
                return;
            }

            TestFilter[] newFilters = p.getFilters();

            if (newFilters == null && filters == null) {
                // do nothing, no change
            } else if ((newFilters == null && filters != null)
                    || (filters == null && newFilters != null)) {
                filters = newFilters;
                wasUpdated = true;
            } else if (newFilters.length == filters.length) {
                // do set comparison on the old and new filters
                if (!CompositeFilter.equals(newFilters, filters)) {
                    filters = newFilters;
                    wasUpdated = true;
                }
            } else {   // there are more or fewer filters than before
                filters = newFilters;
                wasUpdated = true;
            }

            // null or empty check is done by the filter class
            // should be smart about setting wasUpdated flag

            String[] initStrings = p.getTests();
            File[] initFiles = stringsToFiles(initStrings);

            // could optimize out this code if rmInitFiles is false
            iurlFilter = new InitialUrlFilter(initFiles);
            wasUpdated = (wasUpdated || !StringArray.join(initStrings).equals(lastInitStrings));
            lastInitStrings = StringArray.join(initStrings);

        } finally {
            wl.unlock();
        }

        if (wasUpdated) {
            notifyUpdated(this);
        }
    }

    public InitialUrlFilter getIUrlFilter() {
        return iurlFilter;
    }

    private static File[] stringsToFiles(String[] tests) {
        if (tests == null)
            return null;

        File[] files = new File[tests.length];
        for (int i = 0; i < tests.length; i++)
            files[i] = new File(tests[i]);

        return files;
    }

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock rl = rwl.readLock();
    private final Lock wl = rwl.writeLock();

    private InitialUrlFilter iurlFilter;    // not appended into filters
    private String lastInitStrings;
    private TestFilter[] filters;
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(ParameterFilter.class);

}

