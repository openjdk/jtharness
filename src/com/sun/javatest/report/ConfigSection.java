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

import com.sun.interview.WizPrint;
import com.sun.javatest.ExcludeList;
import com.sun.javatest.Keywords;
import com.sun.javatest.Parameters;
import com.sun.javatest.TestEnvironment;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.StringArray;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A report generator for sets of test results.
 */
class ConfigSection extends HTMLSection {
    private final I18NResourceBundle i18n;

    ConfigSection(HTMLReport parent, ReportSettings s, File dir, I18NResourceBundle i18n) {
        super(i18n.getString("config.title"), s, dir, parent);
        this.i18n = i18n;
    }

    @Override
    void writeContents(ReportWriter repWriter) throws IOException {
        super.writeContents(repWriter);

        if (settings.isQuestionLogEnabled()) {
            repWriter.startTag(HTMLWriterEx.UL);
            repWriter.startTag(HTMLWriterEx.LI);
            repWriter.writeLink(HTMLReport.files[HTMLReport.CONFIG_HTML],
                    i18n.getString("config.confInterview"));
            repWriter.endTag(HTMLWriterEx.LI);
            repWriter.endTag(HTMLWriterEx.UL);
        }

        if (settings.isStdEnabled()) {
            repWriter.startTag(HTMLWriterEx.UL);
            repWriter.startTag(HTMLWriterEx.LI);
            repWriter.writeLink("#" + HTMLReport.anchors[HTMLReport.SELECT_ANCHOR],
                    i18n.getString("config.selectValue"));
            repWriter.endTag(HTMLWriterEx.LI);

            if (settings.isEnvEnabled()) {
                repWriter.startTag(HTMLWriterEx.LI);
                repWriter.writeLink("#" + HTMLReport.anchors[HTMLReport.EXEC_ANCHOR],
                        i18n.getString("config.execValue"));
                repWriter.endTag(HTMLWriterEx.LI);
            }

            repWriter.startTag(HTMLWriterEx.LI);
            repWriter.writeLink("#" + HTMLReport.anchors[HTMLReport.LOC_ANCHOR],
                    i18n.getString("config.locValue"));
            repWriter.endTag(HTMLWriterEx.LI);
            repWriter.endTag(HTMLWriterEx.UL);
        }
    }

    @Override
    void writeSummary(ReportWriter repWriter) throws IOException {
        super.writeSummary(repWriter);

        // info about test suite
        repWriter.startTag(HTMLWriterEx.TABLE);
        repWriter.writeAttr(HTMLWriterEx.BORDER, 1);
        repWriter.writeAttr(HTMLWriterEx.SUMMARY, i18n.getString("config.testSuite"));
        repWriter.startTag(HTMLWriterEx.TR);
        repWriter.writeTH(i18n.getString("config.testSuite"), HTMLWriterEx.ROW);
        repWriter.startTag(HTMLWriterEx.TD);

        TestSuite ts = settings.getInterview().getTestSuite();
        if (ts != null) {
            repWriter.writeLink(ts.getRoot());
        } else {
            repWriter.write(i18n.getString("config.noTestSuite"));
        }

        repWriter.endTag(HTMLWriterEx.TD);
        repWriter.endTag(HTMLWriterEx.TR);
        repWriter.endTag(HTMLWriterEx.TABLE);

        // standard values
        if (settings.isStdEnabled()) {
            writeStdValSummary(repWriter);
        }

        // optional section
        if (settings.isEnvEnabled()) {
            writeExecutionSummary(repWriter);
        }

        // non-optional section
        // shows workdir, and report dir
        writeLocationSummary(repWriter);
    }

    @Override
    void writeExtraFiles() throws IOException {
        // optional section
        // tied to question log option
        if (settings.isQuestionLogEnabled()) {
            writeConfigInterview();
        }

        // optional section
        // tied to writing env values in main report
        if (settings.isEnvEnabled()) {
            writeEnvironment();
        }

        // tied to writing standard values in main report
        if (settings.isStdEnabled()) {
            writeExcludeList();
        }
    }

    private void writeStdValSummary(ReportWriter out) throws IOException {
        out.startTag(HTMLWriterEx.H3);
        out.writeLinkDestination(HTMLReport.anchors[HTMLReport.SELECT_ANCHOR],
                i18n.getString("config.selectValue"));
        out.endTag(HTMLWriterEx.H3);

        out.startTag(HTMLWriterEx.TABLE);
        out.writeAttr(HTMLWriterEx.BORDER, 1);
        out.writeAttr(HTMLWriterEx.SUMMARY, i18n.getString("config.selectValue"));

        TestSuite ts = settings.getInterview().getTestSuite();

        String[] tests = settings.getInterview().getTests();
        out.startTag(HTMLWriterEx.TR);
        out.writeTH(i18n.getString("config.tests.hdr"), HTMLWriterEx.ROW);
        out.startTag(HTMLWriterEx.TD);

        if (tests != null && tests.length > 0) {
            for (int i = 0; i < tests.length; i++) {
                if (i > 0) {
                    out.startTag(HTMLWriterEx.BR);
                }
                File file = new File(ts.getTestsDir(), tests[i]);

                // don't try to link if it doesn't exist _now_
                // this is a combination of legacy behavior (link always)
                // and better behavior for new test suites which are more
                // virtual and that may remap file locations
                if (file.exists()) {
                    out.writeLink(file, tests[i]);
                } else {
                    out.write(tests[i]);
                }
            }
        } else {
            out.write(i18n.getString("config.tests.all"));
        }
        out.endTag(HTMLWriterEx.TD);
        out.endTag(HTMLWriterEx.TR);

        out.startTag(HTMLWriterEx.TR);
        out.writeTH(i18n.getString("config.previous.hdr"), HTMLWriterEx.ROW);

        boolean[] b = settings.getInterview().getPriorStatusValues();
        if (b != null) {
            String[] ss = {i18n.getString("config.status.passed"),
                    i18n.getString("config.status.failed"),
                    i18n.getString("config.status.error"),
                    i18n.getString("config.status.notRun")};

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < b.length; i++) {
                if (b[i]) {
                    if (sb.length() > 0) {
                        sb.append(" or "); // XXX needs i18n
                    }
                    sb.append(ss[i]);
                }
            }  // for

            out.writeTD(sb.toString());
        } else {
            out.writeTD(i18n.getString("config.previous.none"));
        }

        out.endTag(HTMLWriterEx.TR);

        // Print exclude list summary
        // NOTE: there may be more than one exclude list
        //       the single ExcludeList from params contains the union

        out.startTag(HTMLWriterEx.TR);
        out.writeTH(i18n.getString("config.excludeTests"), HTMLWriterEx.ROW);
        out.startTag(HTMLWriterEx.TD);

        ExcludeList excludeList = settings.getInterview().getExcludeList();
        if (excludeList != null) {
            // content cell
            out.writeI18N("config.entries", Integer.valueOf(excludeList.size()));

            Parameters.ExcludeListParameters exclParams = settings.getInterview().getExcludeListParameters();
            File[] excludeFiles = null;
            if (exclParams instanceof Parameters.MutableExcludeListParameters) {
                excludeFiles =
                        ((Parameters.MutableExcludeListParameters) exclParams).getExcludeFiles();
            }

            if (excludeFiles != null && excludeFiles.length > 0) {
                for (File excludeFile : excludeFiles) {
                    out.startTag(HTMLWriterEx.BR);
                    out.writeLink(HTMLReport.files[HTMLReport.EXCLUDED_HTML],
                            excludeFile.getPath());
                }   // for
            }
        } else {
            out.write(i18n.getString("config.jtx.nofiles"));
        }

        out.endTag(HTMLWriterEx.TD);
        out.endTag(HTMLWriterEx.TR);

        // do not display if immutable?
        int concurrency = settings.getInterview().getConcurrency();
        out.startTag(HTMLWriterEx.TR);
        out.writeTH(i18n.getString("config.concurrency"), HTMLWriterEx.ROW);
        out.startTag(HTMLWriterEx.TD);
        out.write(Integer.toString(concurrency));
        out.endTag(HTMLWriterEx.TD);
        out.endTag(HTMLWriterEx.TR);

        float timeout = settings.getInterview().getTimeoutFactor();
        out.startTag(HTMLWriterEx.TR);
        out.writeTH(i18n.getString("config.timeOut"), HTMLWriterEx.ROW);
        out.startTag(HTMLWriterEx.TD);
        out.write(NumberFormat.getInstance().format(timeout));
        out.endTag(HTMLWriterEx.TD);
        out.endTag(HTMLWriterEx.TR);

        // keywords
        out.startTag(HTMLWriterEx.TR);
        out.writeTH(i18n.getString("config.keywords.hdr"), HTMLWriterEx.ROW);

        Keywords keywords = settings.getInterview().getKeywords();
        if (keywords != null) {
            out.writeTD(settings.getInterview().getKeywords().toString());
        } else {
            out.writeTD(i18n.getString("config.keywords.none"));
        }
        out.endTag(HTMLWriterEx.TR);

        out.endTag(HTMLWriterEx.TABLE);
    }

    private void writeExecutionSummary(ReportWriter out) throws IOException {
        out.startTag(HTMLWriterEx.H3);
        out.writeLinkDestination(HTMLReport.anchors[HTMLReport.EXEC_ANCHOR],
                i18n.getString("config.execValue"));
        out.endTag(HTMLWriterEx.H3);

        out.startTag(HTMLWriterEx.TABLE);
        out.writeAttr(HTMLWriterEx.BORDER, 1);
        out.writeAttr(HTMLWriterEx.SUMMARY, i18n.getString("config.execValue"));

        File[] envFiles = null;

        // Question Log
        if (settings.isQuestionLogEnabled()) {
            out.startTag(HTMLWriterEx.TR);
            out.writeTH(i18n.getString("config.configInterview"), HTMLWriterEx.ROW);
            out.startTag(HTMLWriterEx.TD);
            String name = settings.getInterview().getName();
            if (name == null) {
                out.writeLink(HTMLReport.files[HTMLReport.CONFIG_HTML],
                        i18n.getString("config.confInterview"));
            } else {
                out.writeLink(HTMLReport.files[HTMLReport.CONFIG_HTML], name);
            }

            if (!settings.getInterview().isValid()) {
                out.writeWarning(i18n.getString("config.intIncomplete"));
            }
            out.endTag(HTMLWriterEx.TD);
            out.endTag(HTMLWriterEx.TR);
        }

        // env
        Parameters.EnvParameters envParams = settings.getInterview().getEnvParameters();
        if (envParams != null &&
                envParams instanceof Parameters.LegacyEnvParameters) {
            envFiles = ((Parameters.LegacyEnvParameters) envParams).getEnvFiles();
            if (envFiles != null) {
                out.startTag(HTMLWriterEx.TR);
                out.writeTH(i18n.getString("config.envFiles"), HTMLWriterEx.ROW);
                out.startTag(HTMLWriterEx.TD);
                for (File envFile : envFiles) {
                    out.writeLink(new File(envFile.getPath()));
                    out.startTag(HTMLWriterEx.BR);
                }
                out.endTag(HTMLWriterEx.TD);
                out.endTag(HTMLWriterEx.TR);
            }
        }

        TestEnvironment env = settings.getInterview().getEnv();
        String envName = env != null ? env.getName() : null;

        if (envName != null && !envName.isEmpty()) {
            out.startTag(HTMLWriterEx.TR);
            out.writeTH(i18n.getString("config.env"), HTMLWriterEx.ROW);
            out.startTag(HTMLWriterEx.TD);
            out.writeLink(HTMLReport.files[HTMLReport.ENV_HTML], envName);
            out.endTag(HTMLWriterEx.TD);
            out.endTag(HTMLWriterEx.TR);
        }

        out.endTag(HTMLWriterEx.TABLE);
    }

    private void writeLocationSummary(ReportWriter out) throws IOException {
        WorkDirectory workDir = settings.getInterview().getWorkDirectory();
        out.startTag(HTMLWriterEx.H3);
        out.writeLinkDestination(HTMLReport.anchors[HTMLReport.LOC_ANCHOR],
                i18n.getString("config.locValue"));
        out.endTag(HTMLWriterEx.H3);

        out.startTag(HTMLWriterEx.TABLE);
        out.writeAttr(HTMLWriterEx.BORDER, 1);
        out.writeAttr(HTMLWriterEx.SUMMARY, i18n.getString("config.locValue"));

        out.startTag(HTMLWriterEx.TR);
        out.writeTH(i18n.getString("config.wd"), HTMLWriterEx.ROW);
        out.startTag(HTMLWriterEx.TD);
        out.writeLink(workDir.getRoot());
        out.endTag(HTMLWriterEx.TD);
        out.endTag(HTMLWriterEx.TR);

        out.startTag(HTMLWriterEx.TR);
        out.writeTH(i18n.getString("config.rd"), HTMLWriterEx.ROW);
        out.startTag(HTMLWriterEx.TD);
        out.writeLink(reportDir);
        out.endTag(HTMLWriterEx.TD);
        out.endTag(HTMLWriterEx.TR);

        out.endTag(HTMLWriterEx.TABLE);
    }

    private void writeConfigInterview() throws IOException {
        WizPrint wp = new WizPrint(settings.getInterview());
        wp.setShowResponses(true);
        wp.setShowTags(true);
        wp.write(openWriter(HTMLReport.CONFIG_HTML));
    }

    private void writeEnvironment() throws IOException {
        TestEnvironment env = settings.getInterview().getEnv();
        String envName = (env != null) ? env.getName() : null;

        ReportWriter out = openAuxFile(HTMLReport.ENV_HTML,
                i18n.getString("config.env.title", envName), i18n);

        if (env == null) {
            out.write(i18n.getString("config.noEnv"));
            return;
        }


        String desc = env.getDescription();

        if (desc != null) {
            out.startTag(HTMLWriterEx.H2);
            out.write(desc);
            out.endTag(HTMLWriterEx.H2);
        }

        Set<String[]> envTable = new TreeSet<>(new StringArrayComparator());

        for (TestEnvironment.Element envElem : env.elements()) {
            String[] envTableRow = {envElem.getKey(), envElem.getValue()};
            envTable.add(envTableRow);
        }

        out.startTag(HTMLWriterEx.TABLE);
        out.writeAttr(HTMLWriterEx.BORDER, 1);
        out.writeAttr(HTMLWriterEx.SUMMARY, i18n.getString("config.env.title", envName));
        for (String[] envEntry : envTable) {
            out.startTag(HTMLWriterEx.TR);

            for (String anEnvEntry : envEntry) {
                out.startTag(HTMLWriterEx.TD);
                out.write(anEnvEntry);
                out.endTag(HTMLWriterEx.TD);
            }

            out.endTag(HTMLWriterEx.TR);
        }
        out.endTag(HTMLWriterEx.TABLE);
        out.close();
    }

    private void writeExcludeList() throws IOException {
        ReportWriter out = openAuxFile(HTMLReport.EXCLUDED_HTML,
                i18n.getString("config.excludedTests"), i18n);

        ExcludeList excludeList = settings.getInterview().getExcludeList();

        if (excludeList == null || excludeList.size() == 0) {
            out.writeI18N("config.excl.none");
        } else {
            SortedSet<ExcludeList.Entry> sortedEntries = new TreeSet<>(new ExcludeListEntryComparator());
            for (Iterator<?> iter = excludeList.getIterator(false); iter.hasNext(); ) {
                sortedEntries.add((ExcludeList.Entry) iter.next());
            }

            out.startTag(HTMLWriterEx.TABLE);
            out.writeAttr(HTMLWriterEx.BORDER, 1);
            out.writeAttr(HTMLWriterEx.SUMMARY, i18n.getString("config.excludedTests"));
            out.startTag(HTMLWriterEx.TR);
            out.startTag(HTMLWriterEx.TH);
            out.writeI18N("config.excl.name");
            out.endTag(HTMLWriterEx.TH);
            out.startTag(HTMLWriterEx.TH);
            out.writeI18N("config.excl.testcase");
            out.endTag(HTMLWriterEx.TH);
            out.startTag(HTMLWriterEx.TH);
            out.writeI18N("config.excl.bugids");
            out.endTag(HTMLWriterEx.TH);
            out.startTag(HTMLWriterEx.TH);
            out.writeI18N("config.excl.platforms");
            out.endTag(HTMLWriterEx.TH);
            out.startTag(HTMLWriterEx.TH);
            out.writeI18N("config.excl.synopsis");
            out.endTag(HTMLWriterEx.TH);
            out.endTag(HTMLWriterEx.TR);

            for (ExcludeList.Entry e : sortedEntries) {
                out.startTag(HTMLWriterEx.TR);
                writeTD(out, e.getRelativeURL());
                writeTD(out, e.getTestCases());

                out.startTag(HTMLWriterEx.TD);
                String[] bugIds = e.getBugIdStrings();
                for (int i = 0; i < bugIds.length; i++) {
                    if (i > 0) {
                        out.write(" ");
                    }
                    out.write(bugIds[i]);
                }
                out.endTag(HTMLWriterEx.TD);

                writeTD(out, StringArray.join(e.getPlatforms()));
                writeTD(out, e.getSynopsis());

                out.endTag(HTMLWriterEx.TR);
            }
            out.endTag(HTMLWriterEx.TABLE);
        }

        out.newLine();
        out.close();
    }

    private void writeTD(ReportWriter out, String text) throws IOException {
        out.startTag(HTMLWriterEx.TD);
        if (text == null || text.isEmpty()) {
            out.writeEntity("&nbsp;");
        } else {
            out.write(text);
        }
        out.endTag(HTMLWriterEx.TD);
    }

    private static class ExcludeListEntryComparator implements Comparator<ExcludeList.Entry> {
        private static int compare(String[] a, String... b) {
            int alen = a == null ? 0 : a.length;
            int blen = b == null ? 0 : b.length;
            for (int i = 0; i < Math.min(alen, blen); i++) {
                int c = compare(a[i], b[i]);
                if (c != 0) {
                    return c;
                }
            }
            return alen < blen ? -1 : alen == blen ? 0 : +1;
        }

        private static int compare(String a, String b) {
            return a == null && b == null ? 0
                    : a == null ? -1
                    : b == null ? +1
                    : a.compareTo(b);
        }

        @Override
        public int compare(ExcludeList.Entry e1, ExcludeList.Entry e2) {
            int x = compare(e1.getRelativeURL(), e2.getRelativeURL());
            if (x == 0) {
                x = compare(e1.getTestCases(), e2.getTestCases());
            }
            return x;

        }
    }

}
