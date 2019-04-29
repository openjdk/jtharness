/*
 * $Id$
 *
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.TestSuite;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CustomReportFormat implements ReportFormat {

    private static final List<CustomReport> NO_REPORTS = Collections.emptyList();
    private CustomReport theCustomReport;
    private List<ReportFormat> subreports = new ArrayList<>();

    public CustomReportFormat() {
    }

    private CustomReportFormat(CustomReport r) {
        theCustomReport = r;
    }

    @Override
    public ReportLink write(ReportSettings s, File dir) {
        if (theCustomReport != null) {
            File f = theCustomReport.createReport(dir);
            return new ReportLink(theCustomReport.getName(), theCustomReport.getReportId(),
                    theCustomReport.getDescription(), f);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public String getReportID() {
        if (theCustomReport != null) {
            return theCustomReport.getReportId();
        } else {
            return null;
        }
    }

    @Override
    public String getBaseDirName() {
        if (theCustomReport != null) {
            return theCustomReport.getReportId();
        } else {
            return null;
        }
    }

    @Override
    public String getTypeName() {
        if (theCustomReport != null) {
            return theCustomReport.getReportId();
        }
        return null;
    }

    @Override
    public boolean acceptSettings(ReportSettings s) {
        // extract custom reports from settings
        if (theCustomReport == null) {
            subreports.clear();
            for (CustomReport cr : s.getCustomReports()) {
                subreports.add(new CustomReportFormat(cr));
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public List<ReportFormat> getSubReports() {
        return subreports;
    }

    private List<CustomReport> getCustomReports(ReportSettings settings) throws ReflectiveOperationException {
        TestSuite ts = settings.getInterview().getTestSuite();
        if (settings.getInterview() == null || settings.getInterview().getTestSuite() == null) {
            return NO_REPORTS;
        }
        return getCustomReports(ts);
    }

    private List<CustomReport> getCustomReports(TestSuite testSuite) throws ReflectiveOperationException {
        String cls = testSuite.getTestSuiteInfo("tmcontext");
        Report.CustomReportManager cm;

        try {
            if (cls == null) {
                // use default implementation
                cm = Class.forName("com.sun.javatest.exec.ContextManager")
                        .asSubclass(Report.CustomReportManager.class).getDeclaredConstructor().newInstance();
            } else {
                cm = Class.forName(cls, true, testSuite.getClassLoader())
                        .asSubclass(Report.CustomReportManager.class).getDeclaredConstructor().newInstance();
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            return NO_REPORTS;
        }

        CustomReport[] customReports = cm.getCustomReports();
        if (customReports == null || customReports.length == 0) {
            return NO_REPORTS;
        }
        return Arrays.asList(customReports);
    }


}
