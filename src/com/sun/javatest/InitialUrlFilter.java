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

/**
 * Filters tests based on a set of initial files or URLs.
 * Any test which does not begin with one of the supplied initial URLs
 * is rejected.  This is the obvious way to select subsets of a test suite
 * based on the test suite hierarchy.
 * Comparisons of initial URLs vs. test URLs are done without respect to
 * case.
 *
 */
public class InitialUrlFilter extends TestFilter {
    /**
     * Create a filter based on the given files.  These file paths must be
     * relative to the testsuite root.  String matching against the paths
     * represented will be used to filter tests.  You may specify a path to a
     * "directory", a specific test, or to a filename which contains tests.
     *
     * @param initFiles Files which specify the tests to be accepted.  Null or
     * zero length arrays are accepted and result in accepts() always returning
     * true.
     */
    public InitialUrlFilter(File[] initFiles) {
        this.initFiles = initFiles;
        this.initStrings = null;
        // preprocess
        if (initFiles == null || initFiles.length == 0) {
            initUrls = null;
        }
        else {
            initUrls = new String[initFiles.length];

            // validate, make path lower case, change path sep.
            for (int i = 0; i < initFiles.length; i++) {
                if (initFiles[i].isAbsolute())  // illegal, based on javadoc spec.
                    throw new IllegalArgumentException(initFiles[i].getPath());

                initUrls[i] = initFiles[i].getPath().toLowerCase();

                // fix path sep. IF needed
                if (File.separatorChar != '/')
                    initUrls[i] = initUrls[i].replace(File.separatorChar, '/');
            }   // for
        }
    }

    /**
     * Create a filter based on the given initial URLs.  These URLs must be
     * forward slash separated and relative to the testsuite root.  String
     * matching against the paths represented will be used to filter tests.
     * You may specify a path to a "directory", a specific test, or to a
     * filename which contains tests.
     *
     * @param initialUrls Paths which specify the tests to be accepted.  Null
     * or zero length arrays are accepted and result in accepts() always
     * returning true.
     */
    public InitialUrlFilter(String[] initialUrls) {
        initStrings = initialUrls;
        initFiles = null;
        // preprocess, make a copy
        if (initialUrls == null || initialUrls.length == 0) {
            initUrls = null;
        }
        else {
            initUrls = new String[initialUrls.length];
            //System.arraycopy(initialUrls, 0, initUrls, 0, initialUrls.length);
            for (int i = 0; i < initialUrls.length; i++) {
                initUrls[i] = initialUrls[i].toLowerCase();
            }   // for
        }
    }

    public boolean accepts(TestDescription td) {
        if (initUrls == null) // all urls being accepted
            return true;

        String testUrl = td.getRootRelativeURL().toLowerCase();

        // other parts of the code should ensure this is not null
        for (int i = 0; i < initUrls.length; i++) {
            String urlI = initUrls[i];
            if (isInitialUrlMatch(testUrl, initUrls[i]))
                return true;
        }   // for

        // no init. urls specified (initUrls.length == 0) OR
        // all init. urls processed
        return false;
    }

    /**
     * Find out if a given URL falls under a particular initial URL.
     * This effectively compares one incoming URL to one in a set of known
     * initial URLs.
     * @param toCheck The incoming name to check.  This might originate from a
     *        TestDescription being filtered.
     * @param compareTo The known initial URL to compare the previous parameter to.
     *        This would probably originate from a set of parameters.
     * @return True if the toCheck URL falls within the range specified by the compareTo
     *         URL.
     */
    public static boolean isInitialUrlMatch(String toCheck, String compareTo) {
        if (toCheck.equals(compareTo))  // direct match of test
            return true;
        // a startsWith match must end on one of the delimiter characters to
        // be a valid match.  the delim. can either be on the initial URL or
        // test URL
        // during a beginsWith:
        //    - is the last char of the initUrl a delimiter?
        //    - is the next char in the test URL a delimiter?
        else if (toCheck.startsWith(compareTo) &&
                 (isDelimiter(compareTo.charAt(compareTo.length()-1)) ||
                  isDelimiter(toCheck.charAt(compareTo.length())))) {
            return true;
        }
        else
            return false;
    }

    public File[] getInitFiles() {
        return initFiles;
    }

    public String[] getInitStrings() {
        return initStrings;
    }

    public String getName() {
        return i18n.getString("iurlFilter.name");
    }

    public String getDescription() {
        return i18n.getString("iurlFilter.description");
    }

    public String getReason() {
        return i18n.getString("iurlFilter.reason");
    }

    /**
     * Is this a delimiter that ends/begins a valid startsWith segment.
     */
    private static boolean isDelimiter(char c) {
        if (c == '/' || c == '#')
            return true;
        else
            return false;
    }

    private final String[] initUrls;
    private final File[] initFiles;
    private final String[] initStrings;
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(ExcludeListFilter.class);
}

