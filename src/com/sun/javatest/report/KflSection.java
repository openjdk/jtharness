/*
 * $Id$
 *
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.SortedSet;

import com.sun.javatest.KnownFailuresList;
import com.sun.javatest.TestResult;
import com.sun.javatest.TestResultTable;
import com.sun.javatest.tool.Preferences;
import com.sun.javatest.util.I18NResourceBundle;

/**
 * Known failures report emitter.
 * @since 4.4
 */
class KflSection extends HTMLSection {

    KflSection(HTMLReport parent, ReportSettings settings, File dir, I18NResourceBundle i18n,
            KflSorter data) {
        super(i18n.getString("kfl.title"), settings, dir, parent);
        this.i18n = i18n;
        sorter = data;

        resultTable = settings.getInterview().getWorkDirectory().getTestResultTable();
        kfl = settings.getInterview().getKnownFailuresList();

        i18n.getString("kfl.f2e.heading");
        i18n.getString("kfl.f2e.notes");
        i18n.getString("kfl.f2e.title");

        i18n.getString("kfl.f2m.heading");
        i18n.getString("kfl.f2m.notes");
        i18n.getString("kfl.f2m.title");

        i18n.getString("kfl.f2nr.heading");
        i18n.getString("kfl.f2nr.notes");
        i18n.getString("kfl.f2nr.title");

        i18n.getString("kfl.f2p.heading");
        i18n.getString("kfl.f2p.notes");
        i18n.getString("kfl.f2p.title");

        i18n.getString("kfl.new.heading");
        i18n.getString("kfl.new.notes");
        i18n.getString("kfl.new.title");

        i18n.getString("kfl.errors.heading");
        i18n.getString("kfl.errors.notes");
        i18n.getString("kfl.errors.title");

        i18n.getString("kfl.f2f.heading");
        i18n.getString("kfl.f2f.notes");
        i18n.getString("kfl.f2f.title");

        i18n.getString("kfl.tc_f2e.heading");
        i18n.getString("kfl.tc_f2e.notes");
        i18n.getString("kfl.tc_f2e.summary");
        i18n.getString("kfl.tc_f2e.title");

        i18n.getString("kfl.tc_f2m.heading");
        i18n.getString("kfl.tc_f2m.notes");
        i18n.getString("kfl.tc_f2m.summary");
        i18n.getString("kfl.tc_f2m.title");

        i18n.getString("kfl.tc_f2nr.heading");
        i18n.getString("kfl.tc_f2nr.notes");
        i18n.getString("kfl.tc_f2nr.summary");
        i18n.getString("kfl.tc_f2nr.title");

        i18n.getString("kfl.tc_f2p.heading");
        i18n.getString("kfl.tc_f2p.notes");
        i18n.getString("kfl.tc_f2p.summary");
        i18n.getString("kfl.tc_f2p.title");

        i18n.getString("kfl.tc_new.heading");
        i18n.getString("kfl.tc_new.notes");
        i18n.getString("kfl.tc_new.summary");
        i18n.getString("kfl.tc_new.title");
    }

    @Override
    void writeSummary(ReportWriter out) throws IOException {
        super.writeSummary(out);

        out.write(i18n.getString("kfl.files.list"));
        File[] kfls = settings.getInterview().getKnownFailureFiles();

        if (kfls != null && kfls.length > 0) {
            out.startTag(HTMLWriterEx.UL);

            for (File f : kfls) {
                out.startTag(HTMLWriterEx.LI);
                out.writeLink(f.toURI().toString(), f.getCanonicalPath());
            }   // for

            out.endTag(HTMLWriterEx.UL);
        } else {
            out.write(i18n.getString("kfl.nofiles"));
            out.newLine();
            return;     // no need to continue with section
        }

        if (kfl == null) {
            out.startTag(HTMLWriterEx.P);
            out.write(i18n.getString("kfl.unable"));
            out.startTag(HTMLWriterEx.BR);
            out.newLine();
            return;
        }

        /*
        sorter = new KflSorter(kfl, resultTable, settings.isKflTestCasesEnabled());
        sorter.setF2eEnabled(settings.isKflF2eEnabled());
        sorter.setF2fEnabled(settings.isKflF2fEnabled());
        sorter.setMissingEnabled(settings.isKflMissingEnabled());

        int errors = sorter.run(parent.getResults());
         */

        out.startTag(HTMLWriterEx.TABLE);
        out.writeAttr(HTMLWriterEx.BORDER, 1);

        out.startTag(HTMLWriterEx.TR);
        out.writeTH(i18n.getString("kfl.changes.hdr"), HTMLWriterEx.ROW);
        out.writeTH(i18n.getString("kfl.tests.hdr", Integer.toString(sorter.getErrorCount())), HTMLWriterEx.ROW);

        if (settings.isKflTestCasesEnabled()) {
            out.writeTH(i18n.getString("kfl.tc.hdr", Integer.toString(sorter.getTestCasesErrorCount())), HTMLWriterEx.ROW);
        }

        // FAIL to PASS
        out.startTag(HTMLWriterEx.TR);
        out.writeTH(i18n.getString("kfl.f2p.summary"), HTMLWriterEx.ROW);

        out.startTag(HTMLWriterEx.TD);
        out.writeLink(FAIL2PASS, Integer.toString(sorter.getSet(KflSorter.Transitions.FAIL2PASS).size()));
        out.endTag(HTMLWriterEx.TD);

        if (settings.isKflTestCasesEnabled()) {
            out.startTag(HTMLWriterEx.TD);
            out.writeLink(TC_FAIL2PASS, Integer.toString(sorter.getSet(KflSorter.Transitions.TC_FAIL2PASS).size()));
            out.endTag(HTMLWriterEx.TD);
        }

        // FAIL to ERROR
        if (settings.isKflF2eEnabled()) {
            out.startTag(HTMLWriterEx.TR);
            out.writeTH(i18n.getString("kfl.f2e.summary"), HTMLWriterEx.ROW);

            out.startTag(HTMLWriterEx.TD);
            out.writeLink(FAIL2ERROR, Integer.toString(sorter.getSet(KflSorter.Transitions.FAIL2ERROR).size()));
            out.endTag(HTMLWriterEx.TD);

            if (settings.isKflTestCasesEnabled()) {
                out.startTag(HTMLWriterEx.TD);
                out.writeLink(TC_FAIL2ERROR, Integer.toString(sorter.getSet(KflSorter.Transitions.TC_FAIL2ERROR).size()));
                out.endTag(HTMLWriterEx.TD);
            }
        }

        // UNRELATED ERRORS
        out.startTag(HTMLWriterEx.TR);
        out.writeTH(i18n.getString("kfl.errors.summary"), HTMLWriterEx.ROW);

        out.startTag(HTMLWriterEx.TD);
        out.writeLink(OTHER_ERRORS, Integer.toString(sorter.getSet(KflSorter.Transitions.OTHER_ERRORS).size()));
        out.endTag(HTMLWriterEx.TD);

        // print that no TC info is available
        out.startTag(HTMLWriterEx.TD);
        out.write(i18n.getString("kfl.f2f.notc"));
        out.endTag(HTMLWriterEx.TD);

        // FAIL to NOT RUN
        out.startTag(HTMLWriterEx.TR);
        out.writeTH(i18n.getString("kfl.f2nr.summary"), HTMLWriterEx.ROW);

        out.startTag(HTMLWriterEx.TD);
        out.writeLink(FAIL2NOTRUN, Integer.toString(sorter.getSet(KflSorter.Transitions.FAIL2NOTRUN).size()));
        out.endTag(HTMLWriterEx.TD);

        if (settings.isKflTestCasesEnabled()) {
            out.startTag(HTMLWriterEx.TD);
            out.writeLink(TC_FAIL2NOTRUN, Integer.toString(sorter.getSet(KflSorter.Transitions.TC_FAIL2NOTRUN).size()));
            out.endTag(HTMLWriterEx.TD);
        }

        if (settings.isKflMissingEnabled()) {
            out.startTag(HTMLWriterEx.TR);
            out.writeTH(i18n.getString("kfl.f2m.summary"), HTMLWriterEx.ROW);

            // FAIL to MISSING
            out.startTag(HTMLWriterEx.TD);
            out.writeLink(FAIL2MISSING, Integer.toString(sorter.getSet(KflSorter.Transitions.FAIL2MISSING).size()));
            out.endTag(HTMLWriterEx.TD);

            if (settings.isKflTestCasesEnabled()) {
                out.startTag(HTMLWriterEx.TD);
                out.writeLink(TC_FAIL2MISSING, Integer.toString(sorter.getSet(KflSorter.Transitions.TC_FAIL2MISSING).size()));
                out.endTag(HTMLWriterEx.TD);
            }
        }

        // NEW FAILURES
        out.startTag(HTMLWriterEx.TR);
        out.writeTH(i18n.getString("kfl.new.summary"), HTMLWriterEx.ROW);
        out.startTag(HTMLWriterEx.TD);
        out.writeLink(NEWFAILURES, Integer.toString(sorter.getSet(KflSorter.Transitions.NEWFAILURES).size()));
        out.endTag(HTMLWriterEx.TD);

        if (settings.isKflTestCasesEnabled()) {
            out.startTag(HTMLWriterEx.TD);
            out.writeLink(TC_NEWFAILURES, Integer.toString(sorter.getSet(KflSorter.Transitions.TC_NEWFAILURES).size()));
            out.endTag(HTMLWriterEx.TD);
        }

        out.endTag(HTMLWriterEx.TR);

        // FAIL to FAIL
        if (settings.isKflF2fEnabled()) {
            out.startTag(HTMLWriterEx.TR);
            out.writeTH(i18n.getString("kfl.f2f.summary"), HTMLWriterEx.ROW);
            out.startTag(HTMLWriterEx.TD);
            out.writeLink(FAIL2FAIL, Integer.toString(sorter.getSet(KflSorter.Transitions.FAIL2FAIL).size()));
            out.endTag(HTMLWriterEx.TD);

            if (settings.isKflTestCasesEnabled()) {
                out.startTag(HTMLWriterEx.TD);
                out.write(i18n.getString("kfl.f2f.notc"));
                out.endTag(HTMLWriterEx.TD);
            }
        }

        out.endTag(HTMLWriterEx.TABLE);
    }

    @Override
    void writeExtraFiles() throws IOException {
        // will be null if the KFL files weren't usable or there were none
        // see writeSummary()
        if (kfl == null || sorter == null)
            return;

        writeSet(FAIL2MISSING, "kfl.f2m", sorter.getSet(KflSorter.Transitions.FAIL2MISSING));
        writeSet(FAIL2PASS, "kfl.f2p", sorter.getSet(KflSorter.Transitions.FAIL2PASS));
        writeSet(FAIL2ERROR, "kfl.f2e", sorter.getSet(KflSorter.Transitions.FAIL2ERROR));
        writeSet(FAIL2NOTRUN, "kfl.f2nr", sorter.getSet(KflSorter.Transitions.FAIL2NOTRUN));
        writeSet(FAIL2FAIL, "kfl.f2f", sorter.getSet(KflSorter.Transitions.FAIL2FAIL));
        writeSet(NEWFAILURES, "kfl.new", sorter.getSet(KflSorter.Transitions.NEWFAILURES));
        writeSet(OTHER_ERRORS, "kfl.errors", sorter.getSet(KflSorter.Transitions.OTHER_ERRORS));

        if (settings.isKflTestCasesEnabled()) {
            writeSet(TC_FAIL2MISSING, "kfl.tc_f2m", sorter.getSet(KflSorter.Transitions.TC_FAIL2MISSING));
            writeSet(TC_FAIL2PASS, "kfl.tc_f2p", sorter.getSet(KflSorter.Transitions.TC_FAIL2PASS));
            writeSet(TC_FAIL2ERROR, "kfl.tc_f2e", sorter.getSet(KflSorter.Transitions.TC_FAIL2ERROR));
            writeSet(TC_FAIL2NOTRUN, "kfl.tc_f2nr", sorter.getSet(KflSorter.Transitions.TC_FAIL2NOTRUN));
            writeSet(TC_NEWFAILURES, "kfl.tc_new", sorter.getSet(KflSorter.Transitions.TC_NEWFAILURES));
        }
    }

    void writeSet(String file, String prefix, SortedSet<KflSorter.TestDiff> tests) throws IOException {
        ReportWriter out = new ReportWriter(openWriter(file), i18n.getString(prefix + ".title"), i18n);
        out.write(i18n.getString(prefix + ".notes"));
        out.startTag(HTMLWriterEx.P);

        if (tests == null || tests.size() == 0) {
            out.write(i18n.getString("kfl.noItemsInSet"));
            out.newLine();
            out.close();
            return;
        }

        out.startTag(HTMLWriterEx.TABLE);
        out.writeAttr("border", "1");
        out.startTag(HTMLWriterEx.TR);
        out.startTag(HTMLWriterEx.TH);
        out.write(i18n.getString("kfl.table.bugs.txt"));
        out.endTag(HTMLWriterEx.TH);
        out.startTag(HTMLWriterEx.TH);
        out.write(i18n.getString("kfl.table.URL.txt"));
        out.endTag(HTMLWriterEx.TH);
        out.endTag(HTMLWriterEx.TR);

        for (KflSorter.TestDiff diff : tests) {
            TestResult tr = diff.getTestResult();

            out.startTag(HTMLWriterEx.TR);
            out.startTag(HTMLWriterEx.TD);

            writeBugs(diff, out);
            out.newLine();

            out.endTag(HTMLWriterEx.TD);
            out.startTag(HTMLWriterEx.TD);

            if (tr != null && tr.isReloadable()) {
                String eWRPath = tr.getWorkRelativePath();
                File eFile = new File(workDirRoot, eWRPath.replace('/', File.separatorChar));
                // note, possible that the file doesn't exist, especially in the case
                // of NOT_RUN.
                out.writeLink(eFile, diff.getName());
            } else {
                out.write(diff.getName());
            }

            out.endTag(HTMLWriterEx.TD);
            out.endTag(HTMLWriterEx.TR);

        }   // for

        out.endTag(HTMLWriterEx.TABLE);
        out.close();
    }

    /**
     * Write a list of bug ids, hyperlinked if possible.  Comma separated.
     */
    void writeBugs(final KflSorter.TestDiff diff, final ReportWriter writer)
            throws IOException {
        KnownFailuresList.Entry[] e = null;

        // find associated KFL entries
        if (diff.getTestCase() == null) {
            e = kfl.find(diff.getTestName());
        }
        else {
            KnownFailuresList.Entry ee = kfl.find(diff.getTestName(),
                    diff.getTestCase());
            if (ee != null)
                e = new KnownFailuresList.Entry[] {ee};
        }

        // no entry, nothing to print
        if (e == null || e.length == 0) {
            // force the associated entry if possible
            if (diff.getKflEntry() != null)
                e = new KnownFailuresList.Entry[] {diff.getKflEntry()};
            else
                return;
        }

        String urlPrefix = Preferences.access().getPreference(ReportManager.BUGRPT_URL_PREF, null);
        HashSet<String> hs = new HashSet();

        for (int i = 0; i < e.length; i++) {
            String[] bugs = e[i].getBugIdStrings();
            if (bugs == null || bugs.length == 0) {
                return;
            }

            for (int j = 0; j < bugs.length; j++) {
                // old style kfl and jtx have zero as a placeholder, we don't
                // want to print it
                if (bugs[j].equals("0000000") || bugs[j].equals("0"))
                    continue;

                // already been printed once
                if (hs.contains(bugs[j])) {
                    continue;
                }
                else {
                    hs.add(bugs[j]);
                }

                if (urlPrefix != null && urlPrefix.length() > 0) {
                    writer.writeLink(urlPrefix + bugs[j], bugs[j]);
                    writer.newLine();
                } else {
                    writer.write(bugs[j]);
                    writer.newLine();
                }

                if (bugs.length != j+1)
                    writer.write(",");
            }
        }   // for

        writer.newLine();
    }

    Writer openWriter(String file) throws IOException {
        return new BufferedWriter(new FileWriter(new File(reportDir, file)));
    }

    /*
    private void grabResults() {
    resultTable = settings.ip.getWorkDirectory().getTestResultTable();
    initFiles = settings.getInitialFiles();

    for (int i = 0; i < lists.length; i++ )
    lists[i] = new TreeSet(new TestResultsByStatusAndTitleComparator());

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
    SortedSet list = lists[s == null ? Status.NOT_RUN : s.getType()];
    list.add(tr);
    totalFound++;
    }
    }

    private void writeCheckFailed() {
    System.err.println("Writing check failed");
    System.err.println("  Passed size = " + lists[0].size());
    System.err.println("  Total size = " + totalFound);

    }

    private void writeStatusFiles() throws IOException {
    for (int i = 0; i < results.length; i++ ) {
    // each file is optional
    if (!settings.isStateFileEnabled(i))
    continue;

    ReportWriter out = openAuxFile(fileCodes[i], headings[i], i18n);
    out.write(i18n.getString("result.groupByStatus"));
    try {
    SortedSet list = lists[i];
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
    }
     */
    private TestResultTable resultTable;
    private KflSorter sorter;
    private KnownFailuresList kfl;

    private final I18NResourceBundle i18n;

    static final String FAIL2PASS = "kfl_fail2pass.html";
    static final String FAIL2ERROR = "kfl_fail2error.html";
    static final String FAIL2MISSING = "kfl_fail2missing.html";
    static final String FAIL2NOTRUN = "kfl_fail2notrun.html";
    static final String FAIL2FAIL = "kfl_fail2fail.html";
    static final String NEWFAILURES = "kfl_newfailures.html";
    static final String OTHER_ERRORS = "kfl_otherErrors.html";

    static final String TC_FAIL2PASS = "kfl_tc_fail2pass.html";
    static final String TC_FAIL2ERROR = "kfl_tc_fail2error.html";
    static final String TC_FAIL2MISSING = "kfl_tc_fail2missing.html";
    static final String TC_FAIL2NOTRUN = "kfl_tc_fail2notrun.html";
    static final String TC_NEWFAILURES = "kfl_tc_newfailures.html";
}
