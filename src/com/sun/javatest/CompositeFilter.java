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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.sun.javatest.util.I18NResourceBundle;

/**
 * A filter which uses one or more filters to implement its accept method.
 * This implementation only support immutable composites.
 * The observer messages it sends out are adjusted so that the internal filter
 * that rejected the test is returned, rather than the composite filter
 * itself.  This allows observing clients to see which non-composite filter
 * actually rejected the test in question.  Composite filters within composite
 * filters should also be transparent to the observer mechanism.
 */
public class CompositeFilter extends TestFilter {
    /**
     * This is the preferred constructor which creates a filter composed of the
     * given filters, with descriptive string from a resource bundle.
     * Inside the given resource bundle, the following keys should provided with
     * the supplied prefix:
     * <ul>
     * <li>description
     * <li>name
     * <li>reason
     * </ul>
     * See the <tt>TestFilter</tt> class for a description of these values.
     * <p>
     * An empty set of filters is accepted for convenience, but will result
     * allowing all tests passing through for filtering to be accepted.
     *
     * @param filters The filters that constitute this composite filter.
     *        Must never be null.
     * @param bundle The bundle to get the description, name and reason from.
     *        Must never be null.
     * @param prefix Prefix with which begins the I18N key for the description,
     *        name and reason.  Must never be null.
     * @see com.sun.javatest.TestFilter
     */
    public CompositeFilter(TestFilter[] filters, I18NResourceBundle bundle,
                           String prefix) {
        // XXX could start checking for null parameter
        this.filters = filters;

        description = bundle.getString(prefix + ".description");
        name = bundle.getString(prefix + ".name");
        reason = bundle.getString(prefix + ".reason");

        // paranoid checks
        if (description == null)
            description = i18n.getString("compFilter.unset.description");
        if (name == null)
            name = i18n.getString("compFilter.unset.name");
        if (reason == null)
            reason = i18n.getString("compFilter.unset.reason");
    }

    /**
     * Construct an unnamed filter composed of the given filters.
     * By using an unnamed filter, any clients querying this object for name,
     * description or rejection reason will get a general-purpose string.
     * This is usually acceptable for composites which are operating at a level
     * where messages will never be user-visible.
     * <p>
     * An empty set of filters is accepted for convienience, but will result
     * allowing all tests passing through for filtering to be accepted.
     *
     * @param filters The filters that constitute this composite filter.
     *        Must never be null.
     * @see #CompositeFilter(TestFilter[],I18NResourceBundle,String)
     */
    public CompositeFilter(TestFilter[] filters) {
        if (filters == null)
            throw new NullPointerException();
        this.filters = filters;
    }

    // ------- TestFilter ---------
    public String getName() {
        if (name == null)
            return i18n.getString("compFilter.name");
        else
            return name;
    }

    public String getDescription() {
        if (description == null)
            return i18n.getString("compFilter.description");
        else
            return description;
    }

    public String getReason() {
        if (reason == null)
            return i18n.getString("compFilter.reason");
        else
            return reason;
    }

    public boolean accepts(TestDescription td) throws Fault {
        if (filters == null || filters.length == 0)
            return true;

        try {
            for (int i = 0; i < filters.length; i++)
                if (!filters[i].accepts(td))
                    return false;

            // made it; accepted
            return true;
        }
        catch (RuntimeException e) {
            throw new Fault(i18n, "compFilter.exception", e);
        }
    }

    public boolean accepts(TestDescription td, TestFilter.Observer o)
                throws TestFilter.Fault {
        if (filters == null || filters.length == 0)
            return true;

        for (int i = 0; i < filters.length; i++)
            // this works well since the observer only has a rejected method
            if (!filters[i].accepts(td, o)) {
                return false;
            }

        return true;
    }

    /**
     * This implementation will compare the filters in the parameter filter
     * to the ones in this instance.  If the parameter is not a CompositeFilter,
     * this test fails immediately.  Recursive checks do not occur - meaning that
     * composites inside composites are not checked.  An empty set is equivalent to
     * an empty set.  This is a element-wise compare only.
     *
     * @param tf The object to be checked for equality with this one
     * @return true if this object equals the argument object
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object tf) {
        if (tf == null)
            return false;

        // if this composite contains one filter, should we
        // compare it to the given filter?
        // how transitive should this comparison be?...recursive composites
        if (!(tf instanceof CompositeFilter))
            return false;

        TestFilter[] thoseFilters = ((CompositeFilter)tf).getFilters();

        // no filters in both is equal
        if ((filters == null || filters.length == 0) && thoseFilters.length == 0)
            return true;

        // both sets have at least 1 element
        // if they don't have the same size, then they are not equal
        if (filters.length != thoseFilters.length)
            return false;

        return equals(filters, thoseFilters);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + (this.filters != null ?
            new HashSet(Arrays.asList(this.filters)).hashCode() : 0);
        return hash;
    }

    // ---------- new methods ----------
    /**
     * Return the filters being used internally by this composite.
     *
     * @return The filters in use.  Zero length array if none.  Never null.
     *         Shallow copy.
     */
    public TestFilter[] getFilters() {
        if (filters == null)
            return new TestFilter[0];

        TestFilter[] copy = new TestFilter[filters.length];
        System.arraycopy(filters, 0, copy, 0, filters.length);

        return copy;
    }

    /**
     * Check if two arrays are equal, using set-equality.
     *
     * @param array1 First set of filters; may not be null.
     * @param array2 Second set of filters; may not be null.
     * @return true of the two arrays of filters are equivalent.
     */
    public static boolean equals(TestFilter[] array1, TestFilter[] array2) {
        List list1 = Arrays.asList(array1);
        List list2 = Arrays.asList(array2);

        HashSet set1 = new HashSet(list1);
        HashSet set2 = new HashSet(list2);

        return set1.equals(set2);
    }

    private TestFilter[] filters;
    private String description;
    private String name;
    private String reason;
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(CompositeFilter.class);
}

