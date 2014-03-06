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

import com.sun.javatest.InitialUrlFilter;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.JavaTestError;
import com.sun.javatest.KnownFailuresList;
import com.sun.javatest.LastRunFilter;
import com.sun.javatest.ParameterFilter;
import com.sun.javatest.Status;
import com.sun.javatest.TestFilter;
import com.sun.javatest.TestResult;
import com.sun.javatest.TestResultTable;
import com.sun.javatest.tool.Preferences;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.StringArray;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Specify what parts of the reports to generate.
 */
public class ReportSettings {

    // preference constants
    private static final String PREFS_GEN_HTML = "rpt.type.html";
    private static final String PREFS_GEN_PLAIN = "rpt.type.plain";
    private static final String PREFS_GEN_XML = "rpt.type.xml";
    private static final String PREFS_GEN_COF = "rpt.type.cof";
    private static final String PREFS_COF_TC = "rpt.cof.tc";
    private static final String PREFS_HTML_CONFIG = "rpt.html.config";
    private static final String PREFS_HTML_QL = "rpt.html.ql";
    private static final String PREFS_HTML_ENV = "rpt.html.env";
    private static final String PREFS_HTML_STD = "rpt.html.std";
    private static final String PREFS_HTML_RES = "rpt.html.res";
    private static final String PREFS_HTML_KWS = "rpt.html.kws";
    private static final String PREFS_HTML_KFL = "rps.html.kfl";
    private static final String PREFS_HTML_REPORTF = "rpt.html.reportf";
    private static final String PREFS_HTML_INDEXF = "rpt.html.htmlf";
    private static final String PREFS_HTML_STATEF = "rpt.html.statef";
    private static final String PREFS_HTML_KFLF2E = "rps.html.kflf2e";
    private static final String PREFS_HTML_KFLF2F = "rpt.html.kflf2f";
    private static final String PREFS_HTML_KFLMISSING = "rpt.html.kflmissing";
    private static final String PREFS_HTML_KFLTC = "rpt.html.kfltc";
    private static final String PREFS_BACK = "rpt.bak.enable";
    private static final String PREFS_BACK_NUM = "rpt.bak.num";

    public ReportSettings() {
        for (int i = 0; i < stateFiles.length; i++) {
            stateFiles[i] = true;
        }
    }

    public ReportSettings(InterviewParameters p) {
        this();
        ip = p;
    }

    /**
     * Creates a new ReportEnviroment instance refers to the given file.
     */
    public ReportSettings(File xmlReportFile, File[] in) {
        this.xmlReportFile = xmlReportFile;
        this.mif = in;
    }

    public void write(Preferences prefs) {
        prefs.setPreference(PREFS_GEN_HTML, Boolean.toString(genHtml));
        prefs.setPreference(PREFS_GEN_PLAIN, Boolean.toString(genPlain));
        prefs.setPreference(PREFS_GEN_XML, Boolean.toString(genXml));
        prefs.setPreference(PREFS_GEN_COF, Boolean.toString(genCof));
        prefs.setPreference(PREFS_COF_TC, Boolean.toString(genCofTestCases));
        prefs.setPreference(PREFS_HTML_CONFIG, Boolean.toString(genConfig));
        prefs.setPreference(PREFS_HTML_QL, Boolean.toString(genQl));
        prefs.setPreference(PREFS_HTML_ENV, Boolean.toString(genEnv));
        prefs.setPreference(PREFS_HTML_STD, Boolean.toString(genStd));
        prefs.setPreference(PREFS_HTML_RES, Boolean.toString(genResults));
        prefs.setPreference(PREFS_HTML_KFL, Boolean.toString(genKfl));
        prefs.setPreference(PREFS_HTML_KWS, Boolean.toString(genKws));
        prefs.setPreference(PREFS_HTML_REPORTF, Boolean.toString(reportHtml));
        prefs.setPreference(PREFS_HTML_INDEXF, Boolean.toString(indexHtml));
        prefs.setPreference(PREFS_HTML_KFLF2E, Boolean.toString(kflF2e));
        prefs.setPreference(PREFS_HTML_KFLF2F, Boolean.toString(kflF2f));
        prefs.setPreference(PREFS_HTML_KFLMISSING, Boolean.toString(kflMissing));
        prefs.setPreference(PREFS_HTML_KFLTC, Boolean.toString(kflTestCases));

        // html state files
        // encoded as a comma sep. list
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < stateFiles.length; i++) {
            sb.append(Boolean.toString(stateFiles[i]));
            if (i + 1 < stateFiles.length) {
                sb.append(",");
            }
        } // for
        // for
        prefs.setPreference(PREFS_HTML_STATEF, sb.toString());
        prefs.setPreference(PREFS_BACK, Boolean.toString(doBackups));
        prefs.setPreference(PREFS_BACK_NUM, Integer.toString(backups));
    }

    public static ReportSettings create(Preferences prefs) {
        ReportSettings result = new ReportSettings();
        // special check to see if pref settings are available
        // if not we will use the defaults
        String tst = prefs.getPreference(PREFS_GEN_HTML);
        if (tst == null) {
            return result;
        } else {
            result.genHtml = parseBoolean(tst);
        }
        result.genPlain = parseBoolean(prefs.getPreference(PREFS_GEN_PLAIN));
        result.genXml = parseBoolean(prefs.getPreference(PREFS_GEN_XML));
        result.genCof = parseBoolean(prefs.getPreference(PREFS_GEN_COF));
        result.genCofTestCases = parseBoolean(prefs.getPreference(PREFS_COF_TC));
        result.genConfig = parseBoolean(prefs.getPreference(PREFS_HTML_CONFIG));
        result.genQl = parseBoolean(prefs.getPreference(PREFS_HTML_QL));
        result.genEnv = parseBoolean(prefs.getPreference(PREFS_HTML_ENV));
        result.genStd = parseBoolean(prefs.getPreference(PREFS_HTML_STD));
        result.genResults = parseBoolean(prefs.getPreference(PREFS_HTML_RES));
        result.genKfl = parseBoolean(prefs.getPreference(PREFS_HTML_KFL));
        result.genKws = parseBoolean(prefs.getPreference(PREFS_HTML_KWS));
        result.reportHtml = parseBoolean(prefs.getPreference(PREFS_HTML_REPORTF));
        result.indexHtml = parseBoolean(prefs.getPreference(PREFS_HTML_INDEXF));
        result.kflF2e = parseBoolean(prefs.getPreference(PREFS_HTML_KFLF2E));
        result.kflF2f = parseBoolean(prefs.getPreference(PREFS_HTML_KFLF2F));
        result.kflMissing = parseBoolean(prefs.getPreference(PREFS_HTML_KFLMISSING));
        result.kflTestCases = parseBoolean(prefs.getPreference(PREFS_HTML_KFLTC));
        // html state files
        // encoded as a comma sep. list
        String[] states = StringArray.splitList(prefs.getPreference(PREFS_HTML_STATEF), ",");
        if (states != null) {
            for (int i = 0; i < states.length; i++) {
                result.stateFiles[i] = parseBoolean(states[i]);
            } // for
            // for
        }
        result.doBackups = parseBoolean(prefs.getPreference(PREFS_BACK));
        try {
            result.backups = Integer.parseInt(prefs.getPreference(PREFS_BACK_NUM));
        } catch (NumberFormatException e) {
            // leave as default
            // leave as default
        }
        return result;
    }

    public void setInterview(InterviewParameters p) {
        ip = p;
    }

    public void setFilter(TestFilter f) {
        filter = f;
    }

    public void setEnableHtmlReport(boolean state) {
        genHtml = state;
    }

    public void setEnableXmlReport(boolean state) {
        genXml = state;
    }

    public void setEnablePlainReport(boolean state) {
        genPlain = state;
    }

    public void setEnableCOFReport(boolean state) {
        genCof = state;
    }

    public void setUseTestCases(boolean state) {
        genCofTestCases = state;
    }

    public void setShowConfigSection(boolean state) {
        genConfig = state;
    }

    public void setShowQuestionLog(boolean state) {
        genQl = state;
    }

    public void setShowEnvLog(boolean state) {
        genEnv = state;
    }

    public void setShowStdValues(boolean state) {
        genStd = state;
    }

    public void setShowResults(boolean state) {
        genResults = state;
    }

    public void setShowKflReport(boolean state) {
        genKfl = state;
    }

    public void setShowKeywordSummary(boolean state) {
        genKws = state;
    }

    /**
     * @param status PASS, FAIL, ERROR, NOT_RUN constant from Status
     */
    public void setEnableHtmlStateFile(int status, boolean state) {
        if (status >= stateFiles.length) {
            return; // error condition
            // error condition
        }
        stateFiles[status] = state;
    }

    public void setHtmlMainReport(boolean reporthtml, boolean indexhtml) {
        reportHtml = reporthtml;
        indexHtml = indexhtml;
    }

    public void setEnableKflF2e(boolean state) {
        kflF2e = state;
    }

    public void setEnableKflF2f(boolean state) {
        kflF2f = state;
    }

    public void setEnableKflMissing(boolean state) {
        kflMissing = state;
    }

    public void setEnableKflTestCases(boolean state) {
        kflTestCases = state;
    }

    public void setEnableBackups(boolean state) {
        doBackups = state;
    }

    public void setBackupLevels(int n) {
        if (n > 0) {
            backups = n;
        }
    }

    public boolean isHtmlEnabled() {
        return genHtml;
    }

    public boolean isXmlEnabled() {
        return genXml;
    }

    public boolean isPlainEnabled() {
        return genPlain;
    }

    public boolean isCOFEnabled() {
        return genCof;
    }

    public boolean isCOFTestCasesEnabled() {
        return genCofTestCases;
    }

    public boolean isConfigSectionEnabled() {
        return genConfig;
    }

    public boolean isQuestionLogEnabled() {
        return genQl;
    }

    public boolean isEnvEnabled() {
        return genEnv;
    }

    public boolean isStdEnabled() {
        return genStd;
    }

    public boolean isResultsEnabled() {
        return genResults;
    }

    public boolean isKflEnabled() {
        return genKfl;
    }

    public boolean isKeywordSummaryEnabled() {
        return genKws;
    }

    public boolean isIndexHtmlEnabled() {
        return indexHtml;
    }

    public boolean isReportHtmlEnabled() {
        return reportHtml;
    }

    public boolean isStateFileEnabled(int status) {
        if (status >= stateFiles.length) {
            return false; // error condition
            // error condition
        }
        return stateFiles[status];
    }

    public boolean isKflTestCasesEnabled() {
        return kflTestCases;
    }

    public boolean isKflMissingEnabled() {
        return kflMissing;
    }

    public boolean isKflF2eEnabled() {
        return kflF2e;
    }

    public boolean isKflF2fEnabled() {
        return kflF2f;
    }

    public boolean isBackupsEnabled() {
        return doBackups;
    }

    public int getBackupLevel() {
        return backups;
    }

    /*
     optimization will be enforced if filter is ParameterFilter
     public void setAllowInitFilesOptimize(boolean s) {
     optimizeInitUrl = s;
     }
     */
    public File[] getInitialFiles() {
        // Optimization: If the filter is a ParameterFilter and
        // rejects all tests not specified in the initial URLs
        // it's enough to iterate over initial URLs only
        if (filter instanceof ParameterFilter) {
            File[] initFiles;
            InitialUrlFilter iuf = ((ParameterFilter) filter).getIUrlFilter();
            if (iuf == null) {
                initFiles = null;
            } else {
                if (iuf.getInitFiles() != null) {
                    initFiles = iuf.getInitFiles();
                } else if (iuf.getInitStrings() != null) {
                    String[] s = iuf.getInitStrings();
                    initFiles = new File[s.length];
                    for (int i = 0; i < s.length; i++) {
                        initFiles[i] = new File(s[i]);
                    }
                } else {
                    initFiles = null;
                }
            }
            return initFiles;
        } else if (filter instanceof LastRunFilter) {
            return ((LastRunFilter) filter).getTestURLs();
        }
        return null;
    }

    public TestFilter getTestFilter() {
        return filter;
    }

    public InterviewParameters getInterview() {
        return ip;
    }

    private static boolean parseBoolean(String s) {
        if (s == null) {
            return false;
        } else {
            return s.equalsIgnoreCase("true");
        }
    }

    void cleanup() {
        if (tmpXmlReportFile != null) {
            tmpXmlReportFile.delete();
        }
        if (exchangeData != null) {
            exchangeData.clear();
        }
    }

    public void setXMLReportFile(File f) {
        xmlReportFile = f;
    }

    public void setMergingFiles(File[] files) {
        mif = files;
    }

    /**
     * Returns array of File objects that were sources for Report Converter tool
     * or empty array if Report Converter was not used.
     *
     * @return array of source files
     */
    public File[] getMergingFiles() {
        return mif;
    }

    /**
     * Give Map for data exchange between custom reports during the same report
     * session. Can be used for sharing intermediate results between reports for
     * optimization.
     *
     * @return Map for data exchange
     */
    public Map getExchangeData() {
        if (exchangeData == null) {
            exchangeData = new HashMap();
        }
        return exchangeData;
    }

    void setupSortedResults() {
        if (sortedResults != null) {
            return;
        }
        TestResultTable resultTable = ip.getWorkDirectory().getTestResultTable();
        File[] initFiles = getInitialFiles();
        sortedResults = new TreeSet[Status.NUM_STATES];
        for (int i = 0; i < sortedResults.length; i++) {
            sortedResults[i] = new TreeSet(new TestResultsByNameComparator());
        }
        Iterator iter;
        try {
            TestFilter[] fs = null;
            // Note: settings.filter should not really be null, modernized clients
            //   of this class should set the filter before asking for a report.
            if (filter == null) {
                fs = new TestFilter[0];
            } else {
                fs = new TestFilter[]{filter};
            }
            iter = ((initFiles == null) ? resultTable.getIterator(fs) : resultTable.getIterator(initFiles, fs));
        } catch (TestResultTable.Fault f) {
            throw new JavaTestError(ReportSettings.i18n.getString("result.testResult.err"));
        }
        for (; iter.hasNext();) {
            TestResult tr = (TestResult) (iter.next());
            Status s = tr.getStatus();
            TreeSet list = sortedResults[s == null ? Status.NOT_RUN : s.getType()];
            list.add(tr);
        }
    }

    private static class TestResultsByNameComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            TestResult tr1 = (TestResult) o1;
            TestResult tr2 = (TestResult) o2;
            return tr1.getTestName().compareTo(tr2.getTestName());
        }
    }

    void setupKfl() {
        if (kflSorter != null) {
            return;
        }
        setupSortedResults();
        KnownFailuresList kfl = getInterview().getKnownFailuresList();
        TestResultTable resultTable = getInterview().getWorkDirectory().getTestResultTable();
        kflSorter = new KflSorter(kfl, resultTable, isKflTestCasesEnabled());
        kflSorter.setF2eEnabled(isKflF2eEnabled());
        kflSorter.setF2fEnabled(isKflF2fEnabled());
        kflSorter.setMissingEnabled(isKflMissingEnabled());
        kflSorter.run(sortedResults);
    }

    KflSorter getKflSorter() {
        return kflSorter;
    }

    TreeSet[] getSortedResults() {
        return sortedResults;
    }

    public void setCustomReports(List<CustomReport> customReportCollection) {
        customReports = customReportCollection;
    }

    public List<CustomReport> getCustomReports() {
        return customReports;
    }

    private TreeSet[] sortedResults;
    private KflSorter kflSorter;
    File xmlReportFile = null;
    File tmpXmlReportFile = null;
    private File[] mif = new File[0];
    private HashMap exchangeData;
    private InterviewParameters ip;

    private List<CustomReport> customReports = Collections.emptyList();

    TestFilter filter;
    // default (legacy) values provided
    boolean genHtml = true; // generate HTML?
    // generate HTML?
    boolean genPlain = true; // generate summary.txt?
    // generate summary.txt?
    boolean genCof = false; // generate cof.xml?
    // generate cof.xml?
    boolean genXml = false; // generate summary.xml?
    // generate summary.xml?
    private boolean genCofTestCases = true;
    boolean genConfig = true; // generate config section
    // generate config section
    boolean genQl = true;
    boolean genEnv = true;
    boolean genStd = true;
    boolean genResults = true;
    boolean genKfl = true;
    boolean genKws = true;
    boolean kflMissing = true;
    boolean kflF2e = true;
    boolean kflF2f = true;
    boolean kflTestCases = true;
    boolean reportHtml = true; // use report.html
    // use report.html
    boolean indexHtml = false; // use index.html
    // use index.html
    boolean[] stateFiles = new boolean[Status.NUM_STATES];
    boolean doBackups = true;
    int backups = 1; // backup levels
    // backup levels
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(ReportSettings.class);
}
