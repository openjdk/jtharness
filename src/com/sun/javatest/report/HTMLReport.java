/*
 * $Id$
 *
 * Copyright (c) 2004, 2016, Oracle and/or its affiliates. All rights reserved.
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
import com.sun.javatest.TestResult;
import com.sun.javatest.util.I18NResourceBundle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * HTML format of the report.
 */
public class HTMLReport implements ReportFormat {
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
            SELECT_ANCHOR = 0,
            EXEC_ANCHOR = 1,
            KFL_ANCHOR = 2,
            LOC_ANCHOR = 3,
            KEYWORD_ANCHOR = 4;
    static final String SECOND_FAILED_REPORT = "failed_gr.html";
    static final String SECOND_PASSED_REPORT = "passed_gr.html";
    static final String SECOND_NOTRUN_REPORT = "notRun_gr.html";


    // --------------- Utility Methods --------------------------------------
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
            SECOND_ERROR_REPORT,
            SECOND_NOTRUN_REPORT
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
            ERROR_HTML_2 = 15,
            NOTRUN_HTML_2 = 16;
    private static final String ID = "html";
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(HTMLReport.class);
    /**
     * The charset to request for the report output.
     * Defaulted to UTF-8, if this is not available at runtime, code will use
     * the default charset provided by the runtime.
     *
     * @see java.nio.charset.Charset#defaultCharset
     */
    protected Charset reportCharset;
    /**
     * Default charset to use.  This is checked against the runtime availability
     * before being used.
     */
    protected String DEFAULT_CHARSET = "UTF-8";

    // ----------------------------------------------------------------------
    File reportDir;
    private List<TreeSet<TestResult>> results;
    private KflSorter kflSorter;

    /**
     * Gets the standard report file name used in JT Harness.
     * Note that this returns the file names which are used for the main
     * report only, not the aux. HTML files.
     *
     * @return The report name.
     */
    public static String[] getReportFilenames() {
        return new String[]{REPORT_NAME, NEW_REPORT_NAME};
    }

    /**
     * Gets the file name based one the input code.
     *
     * @param code The code name for the file.
     * @return The file name.
     */
    public static String getFile(int code) {
        return files[code];
    }

    @Override
    public ReportLink write(ReportSettings repSettings, File dir) throws IOException {
        reportDir = dir;
        initCharset();
        setKflData(repSettings.getKflSorter());
        setResults(repSettings.getSortedTestResults());


        List<HTMLSection> mainSecs = new ArrayList<>(3);
        List<HTMLSection> auxSecs = new ArrayList<>(3);

        // optional section
        ConfigSection cs = new ConfigSection(this, repSettings, dir, i18n);
        if (repSettings.isConfigSectionEnabled()) {
            mainSecs.add(cs);
            auxSecs.add(cs);
        }

        // optional section
        // create instance only if we are generating the summary or
        // one or more result files (failed.html, ...)

        // slightly workaround ifs here to prevent unnecessary
        // initialization if it is not going to be used
        ResultSection resultSection = null;
        if (repSettings.isResultsEnabled() || repSettings.isKflEnabled()) {
            // necessary because the result section generates a sorted
            // set of results
            resultSection = new ResultSection(this, repSettings, dir, i18n, getResults());
        }

        if (repSettings.isResultsEnabled()) {
            mainSecs.add(resultSection);
        }

        if (repSettings.isStateFileEnabled(Status.PASSED) ||
                repSettings.isStateFileEnabled(Status.ERROR) ||
                repSettings.isStateFileEnabled(Status.NOT_RUN) ||
                repSettings.isStateFileEnabled(Status.FAILED)) {
            if (resultSection == null) {
                resultSection = new ResultSection(this, repSettings, dir, i18n, getResults());
            }
            auxSecs.add(resultSection);
        }

        // optional section
        KflSection kfl;
        if (repSettings.isKflEnabled()) {
            kfl = new KflSection(this, repSettings, dir, i18n, kflSorter);
            mainSecs.add(kfl);
            auxSecs.add(kfl);
        }

        // optional section
        if (repSettings.isKeywordSummaryEnabled()) {
            mainSecs.add(new StatisticsSection(this, repSettings, dir, i18n));
            auxSecs.add(new StatisticsSection(this, repSettings, dir, i18n));
        }

        HTMLSection[] mainSections = new HTMLSection[mainSecs.size()];
        mainSecs.toArray(mainSections);

        HTMLSection[] auxSections = new HTMLSection[auxSecs.size()];
        auxSecs.toArray(auxSections);

        // prepare main report file
        Writer writer = null;
        if (repSettings.reportHtml && repSettings.indexHtml) {
            writer = new DuplexWriter(
                    openWriter(reportDir, REPORT_HTML),
                    openWriter(reportDir, INDEX_HTML));
        } else if (repSettings.reportHtml) {
            writer = openWriter(reportDir, REPORT_HTML);
        } else if (repSettings.indexHtml) {
            writer = openWriter(reportDir, INDEX_HTML);
        } else {
            // no main report output specified in settings
        }

        // if the writer is null, the user did not ask for the main
        // report
        ReportWriter.initializeDirectory(reportDir);
        if (writer != null) {
            ReportWriter repWriter = new ReportWriter(writer,
                    i18n.getString("report.title"), i18n, reportCharset);

            // test suite name
            String testSuiteName = repSettings.getInterview().getTestSuite().getName();
            if (testSuiteName != null) {
                repWriter.startTag(HTMLWriterEx.H2);
                repWriter.writeI18N("report.testSuite", testSuiteName);
                repWriter.endTag(HTMLWriterEx.H2);
            }

            if (repSettings.getTestFilter() != null && repSettings.getTestFilter().getName() != null) {
                repWriter.startTag(HTMLWriterEx.H3);
                repWriter.writeI18N("report.filter", repSettings.getTestFilter().getName());
                repWriter.endTag(HTMLWriterEx.H3);
            }

            // info from sections for main report
            repWriter.startTag(HTMLWriterEx.UL);
            for (HTMLSection section : mainSections) {
                repWriter.startTag(HTMLWriterEx.LI);
                section.writeContents(repWriter);
                repWriter.endTag(HTMLWriterEx.LI);
            }
            repWriter.endTag(HTMLWriterEx.UL);

            for (HTMLSection section : mainSections) {
                repWriter.startTag(HTMLWriterEx.HR);
                section.writeSummary(repWriter);
                repWriter.newLine();
            }

            repWriter.close();
        }

        for (HTMLSection auxSection : auxSections) {
            auxSection.writeExtraFiles();
        }

        File f;
        if (repSettings.indexHtml) {
            f = new File(ID + File.separator + "index.html");
        } else if (repSettings.reportHtml) {
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

    File getReportDirectory() {
        return reportDir;
    }

    Writer openWriter(File reportDir, int code) throws IOException {
        File fout = new File(reportDir, files[code]);
        OutputStreamWriter osw =
                new OutputStreamWriter(new FileOutputStream(fout), reportCharset);
        //FileWriter fw = new FileWriter(new File(reportDir, files[code]));
        return new BufferedWriter(osw);
    }

    List<TreeSet<TestResult>> getResults() {
        return results;
    }

    public void setResults(List<TreeSet<TestResult>> results) {
        this.results = results;
    }

    public void setKflData(KflSorter s) {
        kflSorter = s;
    }

    private void initCharset() {
        String userCS = System.getProperty("javatest.report.html.charset");
        if (userCS != null && Charset.isSupported(userCS)) {
            try {
                reportCharset = Charset.forName(userCS);
            } catch (Exception e) {
            }
        }

        // next, attempt JT preferred charset
        if (reportCharset == null && Charset.isSupported(DEFAULT_CHARSET)) {
            try {
                reportCharset = Charset.forName(DEFAULT_CHARSET);
            } catch (Exception e) {
            }
        }

        // default if still not set
        if (reportCharset == null) {
            reportCharset = Charset.defaultCharset();
        }
    }

    // -------------------- Inner Class --------------------------------------

    /**
     * Duplicates output onto n writers.
     */
    static class DuplexWriter extends Writer {
        private Writer[] targets;

        public DuplexWriter(Writer... writers) {
            if (writers == null) {
                return;
            }

            targets = new Writer[writers.length];
            System.arraycopy(writers, 0, targets, 0, writers.length);
        }

        public DuplexWriter(Writer w1, Writer w2) {
            targets = new Writer[2];
            targets[0] = w1;
            targets[1] = w2;
        }

        @Override
        public void close() throws IOException {
            for (Writer target : targets) {
                target.close();
            }
        }

        @Override
        public void flush() throws IOException {
            for (Writer target : targets) {
                target.flush();
            }
        }

        @Override
        public void write(char[] cbuf) throws IOException {
            for (Writer target : targets) {
                target.write(cbuf);
            }
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            for (Writer target : targets) {
                target.write(cbuf, off, len);
            }
        }

        @Override
        public void write(int c) throws IOException {
            for (Writer target : targets) {
                target.write(c);
            }
        }

        @Override
        public void write(String str) throws IOException {
            for (Writer target : targets) {
                target.write(str);
            }
        }

        @Override
        public void write(String str, int off, int len) throws IOException {
            for (Writer target : targets) {
                target.write(str, off, len);
            }
        }
    }

}
