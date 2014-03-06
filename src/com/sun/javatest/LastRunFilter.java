/*
 * $Id$
 *
 * Copyright (c) 2004, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;

import com.sun.javatest.util.I18NResourceBundle;

/**
 * This filter designed to show only the results from the last test run.
 */
public class LastRunFilter extends ObservableTestFilter {


    /**
     * Create uninitialized filter.  Will not work until the work directory is set or
     * the last start time is set.
     * @see #setLastStartTime
     * @see #setWorkDirectory
     */
    public LastRunFilter() {
        super();
    }

    public LastRunFilter(WorkDirectory wd) {
        this();

        setWorkDirectory(wd);
    }

    /**
     * Can be called at any time to change the context of this filter.
     */
    public void setWorkDirectory(WorkDirectory wd) {
        if (wd == null)
            return;

        try {
            LastRunInfo info = LastRunInfo.readInfo(wd);
            lastStart = info.getStartTime() - (info.getStartTime() % 1000L);
            testURLs = info.getTestURLs();
            fileTestURLs = null;
            // warning: time accuracy in a JTR is 1 second
            //   comparing below one second (system clock) may cause unexpected
            //   results
            //lastStart = info.getStartTime();
        }
        catch (IOException e) {
        }

        workdir = wd;

        notifyUpdated(this);
    }

    public void setLastStartTime(long time) {
        // warning: time accuracy in a JTR is 1 second
        //   comparing below one second (system clock) may cause unexpected
        //   results
        //lastStart = time;
        lastStart = time - (time % 1000L);
        notifyUpdated(this);
    }

    public boolean isWorkDirectorySet() {
        return (workdir != null);
    }

    // ------- TestFilter ---------
    public String getName() {
        return i18n.getString("ltr.name");
    }

    public String getDescription() {
        return i18n.getString("ltr.desc");
    }

    public String getReason() {
        return i18n.getString("ltr.reason");
    }

    /**
     * @return Array of testURLs executed
     */
    public File[] getTestURLs() {
        if (fileTestURLs == null) {
            fileTestURLs = new File[testURLs.size()];
            for (int i = 0; i < fileTestURLs.length; i++) {
                fileTestURLs[i] = new File((String)testURLs.get(i));
            }
        }
        return fileTestURLs;
    }

    /**
     * Clears list of testURLs. This method is invoked prior the test run.
     */
    public void clearTestURLs() {
        testURLs.clear();
        fileTestURLs = null;
    }

    /**
     * Adds testURL to the list of testURLs. This method is invoked
     * when a test is selected to be executed.
     *
     * @param url - test url of the test to execute
     */
    public void addTestURL(String url) {
        testURLs.add(url);
        fileTestURLs = null;
    }

    @Override
    public boolean accepts(TestDescription td) throws Fault {
        return accepts(td, null);
    }

    public boolean accepts(TestDescription td, TestFilter.Observer o)
                    throws Fault {

        if (workdir == null)
            return true;

        TestResultTable trt = workdir.getTestResultTable();

        if (trt == null)
            return true;

        TestResult tr = trt.lookup(td);
        if (tr != null) {
            long et = tr.getEndTime();
            if (et >= lastStart)
                return true;
            else {
                if (o != null)
                    o.rejected(td, this);
                return false;
            }
        }
        else
            return true;

    }

    private WorkDirectory workdir;
    private long lastStart;
    private ArrayList testURLs = new ArrayList();
    private File[] fileTestURLs = null; // testURLs as file array
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(LastRunFilter.class);
}
