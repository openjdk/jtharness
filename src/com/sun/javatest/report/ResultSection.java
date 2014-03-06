/*
 * $Id$
 *
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;

import com.sun.javatest.Status;
import com.sun.javatest.TestDescription;
import com.sun.javatest.TestResult;
import com.sun.javatest.TestResultTable;
import com.sun.javatest.util.I18NResourceBundle;

/**
 * Summarize the status, pass/fail/error of the tests which we are reporting on.
 * Also generate output in failed.html, error.html, etc...
 */
class ResultSection extends HTMLSection {
    ResultSection(HTMLReport parent, ReportSettings settings, File dir, I18NResourceBundle i18n,
            TreeSet[] sortedResults) {
        super(i18n.getString("result.title"), settings, dir, parent);
        this.i18n = i18n;

        headings = new String[] {
            i18n.getString("result.heading.passed"),
            i18n.getString("result.heading.failed"),
            i18n.getString("result.heading.errors"),
            i18n.getString("result.heading.notRun")
        };

        resultTable = settings.getInterview().getWorkDirectory().getTestResultTable();
        initFiles = settings.getInitialFiles();
        lists = sortedResults;

        for (TreeSet s: sortedResults)
            totalFound += s.size();
        /*
        lists = new TreeSet[Status.NUM_STATES];
        for (int i = 0; i < lists.length; i++ )
            lists[i] = new TreeSet(new TestResultsByNameComparator());

        Iterator iter;
        try {
            TestFilter[] fs = null;

            // Note: settings.filter should not really be null, modernized clients
            //   of this class should set the filter before asking for a report.
            if (settings.filter == null)
                fs = new TestFilter[0];
            else
                fs = new TestFilter[] {settings.filter};


            iter = ((initFiles == null)
                    ? resultTable.getIterator(fs)
                    : resultTable.getIterator(initFiles, fs));
        }
        catch (TestResultTable.Fault f) {
            throw new JavaTestError(i18n.getString("result.testResult.err"));
        }

        for (; iter.hasNext(); ) {
            TestResult tr = (TestResult) (iter.next());
            Status s = tr.getStatus();
            TreeSet list = lists[s == null ? Status.NOT_RUN : s.getType()];
            list.add(tr);
            totalFound++;
        }

        parent.setResults(lists);
        */
    }

    @Override
    void writeSummary(ReportWriter out) throws IOException {
        super.writeSummary(out);

        out.startTag(HTMLWriterEx.TABLE);
        out.writeAttr(HTMLWriterEx.BORDER, 1);


        boolean thirdColumn = false;
        boolean secondColumn = false;
        for (int i = 0; i < lists.length; i++ ) {
            thirdColumn |= (settings.isStateFileEnabled(i) && hasGroupedReport(i));
            secondColumn |= settings.isStateFileEnabled(i) ;
        }
        String grouped = i18n.getString("result.grouped");
        String plain = i18n.getString("result.plain");

        for (int i = 0; i < lists.length; i++ ) {
            String reportFile = HTMLReport.files[fileCodes[i]];
            TreeSet l = lists[i];

            int n = l.size();
            if (n > 0) {
                out.startTag(HTMLWriterEx.TR);
                out.writeTH(headings[i], HTMLWriterEx.ROW);
                out.startTag(HTMLWriterEx.TD);
                out.writeAttr(HTMLWriterEx.ALIGN, HTMLWriterEx.RIGHT);
                out.write(Integer.toString(n));
                out.endTag(HTMLWriterEx.TD);

                if (secondColumn) {
                    out.startTag(HTMLWriterEx.TD);
                    if (settings.isStateFileEnabled(i)) {
                        out.writeLink(reportFile, plain);
                    } else {
                        out.writeLine(" ");
                    }
                    out.endTag(HTMLWriterEx.TD);
                }

                if (thirdColumn) {
                    out.startTag(HTMLWriterEx.TD);
                    if (hasGroupedReport(i) && settings.isStateFileEnabled(i)) {
                        out.writeLink(HTMLReport.files[groupedFileCodes[i]], grouped);
                    } else {
                        out.writeLine(" ");
                    }
                    out.endTag(HTMLWriterEx.TD);
                }

                out.endTag(HTMLWriterEx.TR);
            }

        }

        out.startTag(HTMLWriterEx.TR);
        out.writeTH(i18n.getString("result.total"), HTMLWriterEx.ROW);
        out.writeTD(Integer.toString(totalFound));

        if (secondColumn) {
            out.writeTD("");
        }

        if (thirdColumn) {
            out.writeTD("");
        }

        out.endTag(HTMLWriterEx.TR);
        out.endTag(HTMLWriterEx.TABLE);
    }

    private boolean hasGroupedReport(int st) {
        return st == Status.FAILED || st == Status.PASSED || st == Status.ERROR ;
    }

    @Override
    void writeExtraFiles() throws IOException {
        writeStatusFiles();
    }

    private void writeStatusFiles() throws IOException {
        for (int i = 0; i < lists.length; i++ ) {
            // each file is optional
            if (!settings.isStateFileEnabled(i))
                continue;

            writeUnGroupedReport(i);

            if (hasGroupedReport(i)) {
                // re-sort it
                TreeSet newS = new TreeSet(new TestResultsByStatusAndTitleComparator());
                newS.addAll(lists[i]);
                lists[i] = newS;

                writeGroupedReport(i);
            }
       }
    }

    private void writeUnGroupedReport(int i) throws IOException {

        ReportWriter out = openAuxFile(fileCodes[i], headings[i], i18n);
        try {
            TreeSet list = lists[i];
            if (list.size() > 0) {
                boolean inList = false;

                for (Iterator iter = list.iterator(); iter.hasNext(); ) {
                    TestResult e = (TestResult) (iter.next());
                    String title;
                    try {
                        TestDescription e_td = e.getDescription();
                        title = e_td.getTitle();
                    }
                    catch (TestResult.Fault ex) {
                        title = null;
                    }

                    Status e_s = e.getStatus();
                    if (!inList) {
                        inList = true;
                    }

                    //File eFile = e.getFile();
                    String eWRPath = e.getWorkRelativePath();
                    File eFile = new File(workDirRoot, eWRPath.replace('/', File.separatorChar));
                    String eName = e.getTestName();
                    if (eFile == null || e_s.getType() == Status.NOT_RUN)
                        out.write(eName);
                    else
                        out.writeLink(eFile, eName);

                    if (title != null)
                        out.write(": " + title);
                    out.startTag(HTMLWriterEx.BR);
                    out.newLine();
                }
            }
        }
        finally {
            out.close();
        }


    }

    private void writeGroupedReport(int i) throws IOException {
        ReportWriter out = openAuxFile(groupedFileCodes[i], headings[i], i18n);
        out.write(i18n.getString("result.groupByStatus"));
        try {
            TreeSet list = lists[i];
            if (list.size() > 0) {
                boolean inList = false;
                String currentHead = null;
                for (Iterator iter = list.iterator(); iter.hasNext(); ) {
                    TestResult e = (TestResult) (iter.next());
                    String title;
                    try {
                        TestDescription e_td = e.getDescription();
                        title = e_td.getTitle();
                    }
                    catch (TestResult.Fault ex) {
                        title = null;
                    }

                    Status e_s = e.getStatus();
                    if (!e_s.getReason().equals(currentHead)) {
                        currentHead = e_s.getReason();
                        if (inList) {
                            inList = false;
                            out.endTag(HTMLWriterEx.UL);
                            out.newLine();
                        }
                        out.startTag(HTMLWriterEx.H4);
                        out.write(currentHead.length() == 0 ? i18n.getString("result.noReason") : currentHead);
                        out.endTag(HTMLWriterEx.H4);
                        out.newLine();
                    }
                    if (!inList) {
                        inList = true;
                        out.startTag(HTMLWriterEx.UL);
                    }
                    out.startTag(HTMLWriterEx.LI);

                    //File eFile = e.getFile();
                    String eWRPath = e.getWorkRelativePath();
                    File eFile = new File(workDirRoot, eWRPath.replace('/', File.separatorChar));
                    String eName = e.getTestName();
                    if (eFile == null || e_s.getType() == Status.NOT_RUN)
                        out.write(eName);
                    else
                        out.writeLink(eFile, eName);

                    if (title != null)
                        out.write(": " + title);
                    out.newLine();
                }
                if (inList) {
                    inList = false;
                    out.endTag(HTMLWriterEx.UL);
                }
            }
        }
        finally {
            out.close();
        }
    }

    private TestResultTable resultTable;
    private File[] initFiles;

    TreeSet[] lists;
    private int totalFound;

    private final int[] fileCodes = {
        HTMLReport.PASSED_HTML,
        HTMLReport.FAILED_HTML,
        HTMLReport.ERROR_HTML,
        HTMLReport.NOTRUN_HTML
    };

    private final int[] groupedFileCodes = {
        HTMLReport.PASSED_HTML_2,
        HTMLReport.FAILED_HTML_2,
        HTMLReport.ERROR_HTML_2
    };

    private final I18NResourceBundle i18n;

    private final String[] headings;
}
