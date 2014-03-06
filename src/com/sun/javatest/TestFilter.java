/*
 * $Id$
 *
 * Copyright (c) 1996, 2012, Oracle and/or its affiliates. All rights reserved.
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
 * A filter interface for TestDescriptions.
 *
 * Implementations of this class <b>should</b> implement the <code>equals()</code>
 * and <code>hashCode()</code> as per the spec in java.lang.Object.  This is used
 * by the harness for file equality purposes which looking for changes and
 * optimizations to perform.
 *
 * @see TestDescription
 */
public abstract class TestFilter
{
    /**
     * This exception is to report problems that occur while filtering tests.
     */
    public static class Fault extends Exception
    {
        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         */
        public Fault(I18NResourceBundle i18n, String s) {
            super(i18n.getString(s));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An argument to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        public Fault(I18NResourceBundle i18n, String s, Object o) {
            super(i18n.getString(s, o));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An array of arguments to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        public Fault(I18NResourceBundle i18n, String s, Object[] o) {
            super(i18n.getString(s, o));
        }
    }

    /**
     * An interface for notification about why a test has been filtered out.
     */
    public interface Observer {
        /**
         * Notification methodcalled when a test has been rejected.
         * @param d The test that has been rejected.
         * @param rejector The filter rejecting the test.
         */
        void rejected(TestDescription d, TestFilter rejector);
    }

    /**
     * Get the name of this filter, as might be used to choose of of a set
     * of filters.  This string should be localized.
     * @return the name of this filter
     */
    public abstract String getName();

    /**
     * Get a description of this filter, as might be used to give more details
     * about this filter than provided by {@link #getName}.  This string should
     * be localized.
     * @return a description of this filter
     */
    public abstract String getDescription();

    /**
     * Get the reason why this filter would reject a test, as might be used
     * in reports.  This string should be localized.
     * @return a rejection reason for this filter
     */
    public abstract String getReason();

    /**
     * Determines whether a TestDescription should be accepted or rejected
     * by this filter.
     *
     * @param td        the TestDescription to check
     * @return true if td should be included in collection; false otherwise.
     * @throws TestFilter.Fault if an error occurs while trying to determine if this test
     * should be accepted or not.
     */
    public abstract boolean accepts(TestDescription td) throws Fault;

    public boolean accepts(TestResult tr) throws Fault, TestResult.Fault {
        return accepts(tr.getDescription());
    }

    /**
     * Determines whether a TestDescription should be accepted or rejected
     * by this filter; if rejected, it is reported to the provided observer.
     *
     * @param td        The TestDescription to check.
     * @param o         An observer, which will be notified if the test is
     *                  rejected by this filter.
     * @return true if td should be included in collection; false otherwise.
     * @throws TestFilter.Fault if an error occurs while trying to determine if this test
     * should be accepted or not.
     */
    public boolean accepts(TestDescription td, Observer o) throws Fault {
        if (accepts(td))
            return true;
        else {
            o.rejected(td, this);
            return false;
        }
    }


    public boolean accepts(TestResult tr, Observer o) throws Fault, TestResult.Fault {
        return accepts(tr.getDescription(), o);
    }
}
