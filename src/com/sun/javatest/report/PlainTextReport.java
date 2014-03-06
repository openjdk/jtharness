/*
 * $Id$
 *
 * Copyright (c) 2004, 2012, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.JavaTestError;
import com.sun.javatest.TestFilter;
import com.sun.javatest.TestResult;
import com.sun.javatest.TestResultTable;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.TextWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Plain text version of the report.
 */
public class PlainTextReport implements ReportFormat {

    public static String[] getReportFilenames() {
        return files;
    }

    @Override
    public ReportLink write(ReportSettings s, File dir) throws IOException {
        TestResultTable resultTable = s.getInterview().getWorkDirectory().getTestResultTable();

        File[] initFiles = s.getInitialFiles();

        SortedSet tests = new TreeSet(new TestResultsByFileComparator());
        int width = 0;

        Iterator iter = null;
        try {
            if (initFiles == null) {
                iter = resultTable.getIterator(new TestFilter[] {s.filter});
            }
            else {
                iter = resultTable.getIterator(initFiles,new TestFilter[] {s.filter});
            }
        }
        catch (TestResultTable.Fault f) {
            throw new JavaTestError(i18n.getString("report.testResult.err"));
        }   // catch

        for (; iter.hasNext(); ) {
            TestResult tr = (TestResult) (iter.next());
            // build a list of TestResults, sorted by test name
            width = Math.max(width, tr.getTestName().length());
            tests.add(tr);
        }

        TextWriter out = new TextWriter(openWriter(dir, files[SMRY_TXT]));
        for (iter = tests.iterator(); iter.hasNext(); ) {
            TestResult tr = (TestResult) (iter.next());
            String u = tr.getTestName();
            out.print(u);
            for (int sp = u.length(); sp < width; sp++)
                out.print(" ");
            out.print("  ");
            out.println(tr.getStatus().toString());
        }
        out.close();

        // add kfl rep
        KflPlainText kfl = new KflPlainText(s);
        kfl.write(dir, s.isKflTestCasesEnabled());

        return new ReportFormat.ReportLink(i18n.getString("index.plaintype.txt"),
                getBaseDirName(), i18n.getString("index.desc.txt"), dir);
    }

    @Override
    public String getReportID() {
        return "pt";
    }

    @Override
    public String getBaseDirName() {
        return "text";
    }

    @Override
    public String getTypeName() {
        return "txt";
    }

    @Override
    public boolean acceptSettings(ReportSettings s) {
        return s.isPlainEnabled();
    }

    @Override
    public List<ReportFormat> getSubReports() {
        return Collections.<ReportFormat>emptyList();
    }

    private Writer openWriter(File reportDir, String filename) throws IOException {
        return new BufferedWriter(new FileWriter(new File(reportDir, filename)));
    }

    // these fields must have synchronized indexes
    private static final String[] files = {"summary.txt"};
    private static final int SMRY_TXT = 0;

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(PlainTextReport.class);
}
