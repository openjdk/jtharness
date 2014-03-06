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
package com.sun.javatest.report;

import com.sun.javatest.Status;
import com.sun.javatest.util.I18NResourceBundle;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * HTML format of the report.
 */
public class HTMLReport implements ReportFormat {

    @Override
    public ReportLink write(ReportSettings s, File dir) throws IOException {
        reportDir = dir;
        setKflData(s.getKflSorter());
        setResults(s.getSortedResults());


        ArrayList mainSecs = new ArrayList(3);
        ArrayList auxSecs = new ArrayList(3);

        // optional section
        ConfigSection cs = new ConfigSection(this, s, dir, i18n);
        if (s.isConfigSectionEnabled()) {
            mainSecs.add(cs);
            auxSecs.add(cs);
        }

        // optional section
        // create instance only if we are generating the summary or
        // one or more result files (failed.html, ...)

        // slightly workaround ifs here to prevent unnecessary
        // initialization if it is not going to be used
        ResultSection rs = null;
        if (s.isResultsEnabled() || s.isKflEnabled()) {
            // necessary because the result section generates a sorted
            // set of results
            rs = new ResultSection(this, s, dir, i18n, getResults());
        }

        if (s.isResultsEnabled()) {
            mainSecs.add(rs);
        }

        if (s.isStateFileEnabled(Status.PASSED) ||
            s.isStateFileEnabled(Status.ERROR) ||
            s.isStateFileEnabled(Status.NOT_RUN) ||
            s.isStateFileEnabled(Status.FAILED)) {
            if (rs == null) {
                    rs = new ResultSection(this, s, dir, i18n, getResults());
            }
            auxSecs.add(rs);
        }

        // optional section
        KflSection kfl;
        if (s.isKflEnabled()) {
           kfl = new KflSection(this, s, dir, i18n, kflSorter);
           mainSecs.add(kfl);
           auxSecs.add(kfl);
        }

        // optional section
        if (s.isKeywordSummaryEnabled()) {
            mainSecs.add(new StatisticsSection(this, s, dir, i18n));
            auxSecs.add(new StatisticsSection(this, s, dir, i18n));
        }

        HTMLSection[] mainSections = new HTMLSection[mainSecs.size()];
        mainSecs.toArray(mainSections);

        HTMLSection[] auxSections = new HTMLSection[auxSecs.size()];
        auxSecs.toArray(auxSections);

        // prepare main report file
        Writer writer = null;
        if (s.reportHtml && s.indexHtml) {
            writer = new DuplexWriter(
                        openWriter(reportDir, REPORT_HTML),
                        openWriter(reportDir, INDEX_HTML));
        }
        else if (s.reportHtml) {
            writer = openWriter(reportDir, REPORT_HTML);
        }
        else if (s.indexHtml) {
            writer = openWriter(reportDir, INDEX_HTML);
        }
        else {
            // no main report output specified in settings
        }

        // if the writer is null, the user did not ask for the main
        // report
        ReportWriter.initializeDirectory(reportDir);
        if (writer != null) {
            ReportWriter out = new ReportWriter(writer,
                            i18n.getString("report.title"), i18n);

            // test suite name
            String testSuiteName = s.getInterview().getTestSuite().getName();
            if (testSuiteName != null) {
                out.startTag(HTMLWriterEx.H2);
                out.writeI18N("report.testSuite", testSuiteName);
                out.endTag(HTMLWriterEx.H2);
            }

            // info from sections for main report
            out.startTag(HTMLWriterEx.UL);
            for (int i = 0; i < mainSections.length; i++) {
                out.startTag(HTMLWriterEx.LI);
                mainSections[i].writeContents(out);
                out.endTag(HTMLWriterEx.LI);
            }
            out.endTag(HTMLWriterEx.UL);

            for (int i = 0; i < mainSections.length; i++) {
                out.startTag(HTMLWriterEx.P);
                out.startTag(HTMLWriterEx.HR);
                mainSections[i].writeSummary(out);
                out.newLine();
            }

            out.close();
        }

        for (int i = 0; i < auxSections.length; i++) {
            auxSections[i].writeExtraFiles();
        }

        File f;
        if (s.indexHtml) {
            f = new File(ID + File.separator + "index.html");
        } else if (s.reportHtml) {
            f = new File(ID + File.separator + "report.html");
        } else {
            f = new File(ID + File.separator);
        }

        return new ReportLink(i18n.getString("index.htmltype.txt"),
                getBaseDirName(), i18n.getString("index.desc.html"), f);
    }

    @Override
    public String getReportID() {
        return ID;
    }

    @Override
    public String getBaseDirName() {
        return ID;
    }

    @Override
    public String getTypeName() {
        return ID;
    }

    @Override
    public boolean acceptSettings(ReportSettings s) {
        return s.isHtmlEnabled();
    }

    @Override
    public List<ReportFormat> getSubReports() {
        return Collections.<ReportFormat>emptyList();
    }


    // --------------- Utility Methods --------------------------------------

    /**
     * Gets the standard report file name used in JT Harness.
     * Note that this returns the file names which are used for the main
     * report only, not the aux. HTML files.
     * @return The report name.
     */
    public static String[] getReportFilenames() {
        return new String[] {REPORT_NAME, NEW_REPORT_NAME};
    }

    /**
     * Gets the file name based one the input code.
     * @param code The code name for the file.
     * @return The file name.
     */
    public static String getFile(int code) {
        return files[code];
    }

    File getReportDirectory() {
        return reportDir;
    }

    Writer openWriter(File reportDir, int code) throws IOException {
        return new BufferedWriter(new FileWriter(new File(reportDir, files[code])));
    }

    public void setResults(TreeSet[] results) {
        this.results = results;
    }

    TreeSet[] getResults() {
        return results;
    }

    public void setKflData(KflSorter s) {
        kflSorter = s;
    }

    // ----------------------------------------------------------------------

    private static final String ID = "html";

    File reportDir;

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(HTMLReport.class);

    private TreeSet[] results;
    private KflSorter kflSorter;

    // The name of the root file for a set of report files.
    static final String REPORT_NAME = "report.html";
    static final String NEW_REPORT_NAME = "index.html";

    // html anchors to be used in the output
    static final String[] anchors = {
       "selection",
       "execution",
       "kfl",
       "locations",
       "keywordSummary"
    };

    // The following must be kept in sync with the preceding list
    static final int
        SELECT_ANCHOR  = 0,
        EXEC_ANCHOR    = 1,
        KFL_ANCHOR     = 2,
        LOC_ANCHOR     = 3,
        KEYWORD_ANCHOR = 4;

    static final String SECOND_FAILED_REPORT = "failed_gr.html";
    static final String SECOND_PASSED_REPORT = "passed_gr.html";
    static final String SECOND_ERROR_REPORT = "error_gr.html";

    static final String[] files = {
        REPORT_NAME,
        NEW_REPORT_NAME,
        "config.html",
        "env.html",
        "excluded.html",
        "passed.html",
        "failed.html",
        "error.html",
        "notRun.html",
        KflSection.FAIL2PASS,
        KflSection.FAIL2ERROR,
        KflSection.FAIL2MISSING,
        KflSection.NEWFAILURES,
        SECOND_PASSED_REPORT,
        SECOND_FAILED_REPORT,
        SECOND_ERROR_REPORT
    };

    // The following must be kept in sync with the preceding list
    static final int
        REPORT_HTML = 0,
        INDEX_HTML = 1,
        CONFIG_HTML = 2,
        ENV_HTML = 3,
        EXCLUDED_HTML = 4,
        PASSED_HTML = 5,
        FAILED_HTML = 6,
        ERROR_HTML = 7,
        NOTRUN_HTML = 8,
        KFL_F2P = 9,
        KFL_F2E = 10,
        KFL_F2M = 11,
        KFL_NEW = 12,
        PASSED_HTML_2 = 13,
        FAILED_HTML_2 = 14,
        ERROR_HTML_2 = 15;

    // -------------------- Inner Class --------------------------------------

    /**
     * Duplicates output onto n writers.
     */
    static class DuplexWriter extends Writer {
        public DuplexWriter(Writer[] writers) {
            if (writers == null)
                return;

            targets = new Writer[writers.length];
            System.arraycopy(writers, 0, targets, 0, writers.length);
        }

        public DuplexWriter(Writer w1, Writer w2) {
            targets = new Writer[2];
            targets[0] = w1;
            targets[1] = w2;
        }

        public void close() throws IOException {
            for (int i = 0; i < targets.length; i++)
                targets[i].close();
        }

        public void flush() throws IOException {
            for (int i = 0; i < targets.length; i++)
                targets[i].flush();
        }

        @Override
        public void write(char[] cbuf) throws IOException {
            for (int i = 0; i < targets.length; i++)
                targets[i].write(cbuf);
        }

        public void write(char[] cbuf, int off, int len) throws IOException {
            for (int i = 0; i < targets.length; i++)
                targets[i].write(cbuf, off, len);
        }

        @Override
        public void write(int c) throws IOException {
            for (int i = 0; i < targets.length; i++)
                targets[i].write(c);
        }

        @Override
        public void write(String str) throws IOException {
            for (int i = 0; i < targets.length; i++)
                targets[i].write(str);
        }

        @Override
        public void write(String str, int off, int len) throws IOException {
            for (int i = 0; i < targets.length; i++)
                targets[i].write(str, off, len);
        }

        private Writer[] targets;
    }

}
