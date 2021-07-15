/*
 * $Id$
 *
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
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
package jthtest.tools;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import jthtest.ReportCreate.ReportCreate.ReportBrowser;
import jthtest.Test;
import jthtest.Tools;
import org.netbeans.jemmy.JemmyException;

public class ReportChecker {

    public static final String F2E = "ahref=kfl_fail2error.html>";
    public static final String F2F = "<ahref=kfl_fail2fail.html>";
    public static final String F2M = "ahref=kfl_fail2missing.html>";
    public static final String F2N = "<ahref=kfl_fail2notrun.html>";
    public static final String F2P = "<ahref=kfl_fail2pass.html>";
    //public static final String KFL_1 = "name=KnownFailureAnalysis";
    //public static final String KFL_2 = "name=KnownFailureAnalysis";
    public static final String KFL_1 = "aid=#KnownFailureAnalysis";
    public static final String KFL_2 = "ahref=#KnownFailureAnalysis";
    public static final String NF = "<ahref=kfl_newfailures.html>";
    public static final String TESTS_TEXT = "<thscope=row>Tests(";
    public static final String NOT_CALC = "<td>(notcalculated)</td>";
    public static final String TC_F2E_LINK = "<ahref=kfl_tc_fail2error.html>";
    public static final String TC_F2M_LINK = "<ahref=kfl_tc_fail2missing.html>";
    public static final String TC_F2N_LINK = "<ahref=kfl_tc_fail2notrun.html>";
    public static final String TC_F2P_LINK = "<ahref=kfl_tc_fail2pass.html>";
    public static final String TC_NEW_LINK = "<ahref=kfl_tc_newfailures.html>";
    public static final String TEST_CASES_TEXT = "<thscope=row>TestCases(";
    private String basePath;
    private ReportBrowser browser;
    private ReportDialog reportDialog;

    public enum ReportType {

        INDEX_HTML, REPORT_HTML
    };

    public ReportChecker(String path, ReportDialog report) {
        basePath = path;
        browser = new ReportBrowser(path);
        reportDialog = report;
    }

    private void checkReportPage() throws JemmyException {
        String htmlPath = basePath + "html" + File.separator;
        String[] urls = browser.getUrls();
        StringBuffer error = new StringBuffer("");
        int i = -1;

        if (reportDialog.isFGenerateError()) {
            if (!(new File(htmlPath + "error.html").exists())) {
                error.append("error.html was not found at the path '" + htmlPath + "' while expected\n");
            }
        } else {
            if (new File(htmlPath + "error.html").exists()) {
                error.append("error.html was found at the path '" + htmlPath + "' while unexpected\n");
            }
        }

        if (reportDialog.isFGenerateFailed()) {
            if (!(new File(htmlPath + "failed.html").exists())) {
                error.append("failed.html was not found at the path '" + htmlPath + "' while expected\n");
            }
        } else {
            if (new File(htmlPath + "failed.html").exists()) {
                error.append("failed.html was found at the path '" + htmlPath + "' while unexpected\n");
            }
        }

        if (reportDialog.isFGenerateNotRun()) {
            if (!(new File(htmlPath + "notRun.html").exists())) {
                error.append("notRun.html was not found at the path '" + htmlPath + "' while expected\n");
            }
        } else {
            if (new File(htmlPath + "notRun.html").exists()) {
                error.append("notRun.html was found at the path '" + htmlPath + "' while unexpected\n");
            }
        }

        if (reportDialog.isFGeneratePassed()) {
            if (!(new File(htmlPath + "passed.html").exists())) {
                error.append("passed.html was not found at the path '" + htmlPath + "' while expected\n");
            }
        } else {
            if (new File(htmlPath + "passed.html").exists()) {
                error.append("passed.html was found at the path '" + htmlPath + "' while unexpected\n");
            }
        }

        if (reportDialog.isFPutInIndex()) {
            if (!(new File(htmlPath + "index.html").exists())) {
                error.append("index.html was not found at the path '" + htmlPath + "' while expected\n");
            }
        } else {
            if (new File(htmlPath + "index.html").exists()) {
                error.append("index.html was found at the path '" + htmlPath + "' while unexpected\n");
            }
        }

        if (reportDialog.isFPutInReport()) {
            if (!(new File(htmlPath + "report.html").exists())) {
                error.append("report.html was not found at the path '" + htmlPath + "' while expected\n");
            }
        } else {
            if (new File(htmlPath + "report.html").exists()) {
                error.append("report.html was found at the path '" + htmlPath + "' while unexpected\n");
            }
        }

        if (reportDialog.isOConfig()) {
            checkFileUrls(urls);
            i = Tools.findInStringArray(urls, "#Configuration and Other Settings");
            if (i == -1) {
                error.append("report doesn't contain configuration url '#Configuration and Other Settings'\n");
            }
            if (!browser.getText().contains("name=\"Configuration and Other Settings\"")) {
                error.append("report doesn't contain configuration block\n");
            }
//        i = findInStringArray(urls, "#locations");
//        if (i == -1) {
//            error.append("report doesn't contain url to 'Where to put the results' block\n");
//        }
            if (!browser.getText().contains("name=locations") && !browser.getText().contains("name=\"locations\"")) {
                error.append("report doesn't contain 'Where to put the results' block\n");
            }

            if (reportDialog.isOConfig_QuestionLog()) {
                i = Tools.findInStringArray(urls, "config.html");
                if (i == -1) {
                    error.append("report doesn't contain url to config.html\n");
                }
                if (!(new File(htmlPath + "config.html").exists())) {
                    error.append("config.html was not found at the path '" + htmlPath + "' while expected\n");
                }
            } else {
                i = Tools.findInStringArray(urls, "config.html");
                if (i != -1) {
                    error.append("report contains url to config.html while unexpected\n");
                }
                if (new File(htmlPath + "config.html").exists()) {
                    error.append("config.html was found at the path '" + htmlPath + "' while unexpected\n");
                }
            }

            if (reportDialog.isOConfig_StdValues()) {
                i = Tools.findInStringArray(urls, "#selection");
                if (i == -1) {
                    error.append("report doesn't contain url to standart values block\n");
                }
                if (!browser.getText().contains("name=\"selection\"")) {
                    error.append("report doesn't contain standart values block\n");
                }
                if (!new File(htmlPath + "excluded.html").exists()) {
                    error.append("excluded.html wasn't found at the path '" + htmlPath + "'\n");
                }
            } else {
                i = Tools.findInStringArray(urls, "#selection");
                if (i != -1) {
                    error.append("report contains url to standart values block while unexpected\n");
                }
                if (browser.getText().contains("name=\"selection\"")) {
                    error.append("report contains standart values block while unexpected\n");
                }
                if (new File(htmlPath + "excluded.html").exists()) {
                    error.append("excluded.html was found at the path '" + htmlPath + "' while unexpected\n");
                }
            }

            if (reportDialog.isOConfig_TestEnv()) {
//            i = findInStringArray(urls, "#execution");
//            if (i == -1) {
//            error.append("report doesn't contain url to test enviroment ('How to run') block\n");
//            }
                if (!browser.getText().contains("name=execution") && !browser.getText().contains("name=\"execution\"")) {
                    error.append("report doesn't contain test enviroment ('How to run') block\n");
                }
                if (!(new File(htmlPath + "env.html").exists())) {
                    error.append("env.html was not found at the path '" + htmlPath + "' while expected\n");
                }
            } else {
                i = Tools.findInStringArray(urls, "#execution");
                if (i != -1) {
                    error.append("report contains url to test enviroment ('How to run') block while unexpected\n");
                }
                if (browser.getText().contains("name=\"execution\"")) {
                    error.append("report contains test enviroment ('How to run') block while unexpected\n");
                }
                if (new File(htmlPath + "env.html").exists()) {
                    error.append("env.html was found at the path '" + htmlPath + "' while unexpected\n");
                }
            }
        } else {
            i = Tools.findInStringArray(urls, "#Configuration and Other Settings");
            if (i != -1) {
                error.append("report contains configuration url '#Configuration and Other Settings' while unexpected\n");
            }
            if (browser.getText().contains("name=\"Configuration and Other Settings\"")) {
                error.append("report contains configuration block while unexpected\n");
            }
            i = Tools.findInStringArray(urls, "config.html");
            if (i != -1) {
                error.append("report contains url to config.html while unexpected\n");
            }
            if (new File(htmlPath + "config.html").exists()) {
                error.append("config.html was found at the path '" + htmlPath + "' while unexpected\n");
            }
            i = Tools.findInStringArray(urls, "#selection");
            if (i != -1) {
                error.append("report contains url to standart values block while unexpected\n");
            }
            if (browser.getText().contains("name=\"selection\"")) {
                error.append("report contains standart values block while unexpected\n");
            }
            i = Tools.findInStringArray(urls, "#locations");
            if (i != -1) {
                error.append("report contains url to 'Where to put the results' block while unexpected\n");
            }
            if (browser.getText().contains("name=\"locations\"")) {
                error.append("report contains 'Where to put the results' block while unexpected\n");
            }
            i = Tools.findInStringArray(urls, "#execution");
            if (i != -1) {
                error.append("report contains url to test enviroment ('How to run') block while unexpected\n");
            }
            if (browser.getText().contains("name=\"execution\"")) {
                error.append("report contains test enviroment ('How to run') block while unexpected\n");
            }
            if (new File(htmlPath + "env.html").exists()) {
                error.append("env.html was found at the path '" + htmlPath + "' while unexpected\n");
            }
            if (new File(htmlPath + "excluded.html").exists()) {
                error.append("excluded.html was found at the path '" + htmlPath + "' while unexpected\n");
            }
        }

        if (reportDialog.isOKeywords()) {
            i = Tools.findInStringArray(urls, "#Statistics");
            if (i == -1) {
                error.append("report doesn't contain url to Statistics block (produced by options-keywords)\n");
            }
            if (!browser.getText().contains("name=Statistics") && !browser.getText().contains("name=\"Statistics\"")) {
                error.append("report doesn't contain Statistics block (produced by options-keywords)\n");
            }
            i = Tools.findInStringArray(urls, "#keywordSummary");
            if (i == -1) {
                error.append("report doesn't contain url to keywordSummary block\n");
            }
            if (!browser.getText().contains("name=keywordSummary") && !browser.getText().contains("name=\"keywordSummary\"")) {
                error.append("report doesn't contain keywordSummary block\n");
            }
        } else {
            i = Tools.findInStringArray(urls, "#Statistics");
            if (i != -1) {
                error.append("report contains url to Statistics block while unexpected\n");
            }
            if (browser.getText().contains("name=\"Statistics\"")) {
                error.append("report contains Statistics block while unexpected\n");
            }
            i = Tools.findInStringArray(urls, "#keywordSummary");
            if (i != -1) {
                error.append("report contains url to keywordSummary block while unexpected\n");
            }
            if (browser.getText().contains("name=\"keywordSummary\"")) {
                error.append("report contains keywordSummary block while unexpected\n");
            }
        }

        if (reportDialog.isOResults()) {
            i = Tools.findInStringArray(urls, "#Results");
            if (i == -1) {
                error.append("report doesn't contain url to Results block\n");
            }
            if (!browser.getText().contains("name=Results") && !browser.getText().contains("name=\"Results\"")) {
                error.append("report doesn't contain Results block\n");
            }
        } else {
            i = Tools.findInStringArray(urls, "#Results");
            if (i != -1) {
                error.append("report contains url to Results block while unexpected\n");
            }
            if (browser.getText().contains("name=\"Results\"")) {
                error.append("report contains Results block while unexpected\n");
            }
        }
        if (error.length() > 0) {
            throw new JemmyException(error.toString());
        }
    }

    private String getUrlToHtmlReportPage() {
        String[] urls = browser.getUrls();
        int i = -1;
        String t = "";
        if (reportDialog.isFPutInIndex()) {
            i = Tools.findInStringArray(urls, "html/index.html");
            t = "index";
        } else {
            if (reportDialog.isFPutInReport()) {
                i = Tools.findInStringArray(urls, "html/report.html");
                t = "report";
            } else {
                throw new JemmyException("NIY - nor index.html nor report.html selected");
            }
        }
        if (i == -1) {
            throw new JemmyException("Url to html report page was not found. Tring to browse from '" + browser.getPath().toString() + "' to " + t);
        }

        return urls[i];
    }

    public void goToHtmlReport(ReportType type) {
        String path = browser.getPath().getPath();
        if (path.equals(basePath + "html/report.html") && type == ReportType.REPORT_HTML) {
            return;
        }
        if (path.equals(basePath + "html/index.html") && type == ReportType.INDEX_HTML) {
            return;
        }

        if (type == ReportType.INDEX_HTML && reportDialog.isFPutInIndex()) {
            if (!path.equals(basePath + "index.html")) {
                browser.home();
                browser.waitForPageLoading("</html>", Test.TESTSUITENAME);
            }
            String url = getUrlToHtmlReportPage();

            URL u = Tools.urlFile(basePath + File.separator + url);
            browser.clickUrl(u);
            browser.waitForPageLoading("</small></body></html>", "<title>" + Test.WINDOWNAME + " Harness : Report");
        }

        if (type == ReportType.REPORT_HTML && reportDialog.isFPutInReport()) {
            URL u = Tools.urlFile(basePath + File.separator + "html");
            browser.clickUrl(u);
            browser.waitForPageLoading("</html>", "Directory listing for html");
            u = Tools.urlFile(basePath + File.separator + "html" + File.separator + "report.html");
            browser.clickUrl(u);
            browser.waitForPageLoading("</small></body></html>", "<title>" + Test.WINDOWNAME + " Harness : Report");
        }

        Tools.pause(1);
    }

    public void commitMainCheck() {
        if (reportDialog.isFPutInIndex()) {
            goToHtmlReport(ReportType.INDEX_HTML);
            checkReportPage();
        }
        if (reportDialog.isFPutInReport()) {
            goToHtmlReport(ReportType.REPORT_HTML);
            checkReportPage();
        }
        if (!reportDialog.isFPutInIndex() && !reportDialog.isFPutInReport()) {
            throw new JemmyException("NIY");
        }


    }

    private StringBuffer checkFileUrls(String urls[]) {
        StringBuffer temp = new StringBuffer("");
        boolean ts = false, wd = false, rp = false;
        for (int i = 0; i < urls.length; i++) {
            if (urls[i].equals(Test.LOCAL_PATH + Test.TEST_SUITE_NAME)) {
                ts = true;
            }
            if (urls[i].equals(Test.LOCAL_PATH + Test.REPORT_WD_PATH)) {
                wd = true;
            }
            if (urls[i].equals(basePath + "html")) {
                rp = true;
            }
        }
        if (!ts) {
            temp.append("Url for testsuite was not found");
        }
        if (!wd) {
            temp.append("Url for workdirektory was not found");
        }
        if (!rp) {
            temp.append("Url for report directory was not found");
        }
        return temp;
    }

    public KFLValues getKFLList() throws JemmyException {
        if (reportDialog.isFPutInIndex()) {
            goToHtmlReport(ReportType.INDEX_HTML);
            return checkKFLImpl();
        }
        if (reportDialog.isFPutInReport()) {
            goToHtmlReport(ReportType.REPORT_HTML);
            return checkKFLImpl();
        }

        return null;
    }

    private KFLValues checkKFLImpl() {
        String htmlPath = basePath + "html" + File.separator;
        String[] urls = browser.getUrls();
        StringBuilder error = new StringBuilder("");

        if (Tools.findInStringArray(urls, "Known Failure Analysis") >= 0) {
            error.append("Url to KFL block was not found");
        }

        KFLValues res = new KFLValues();

        String text = browser.getText().replaceAll("[\t\n\r\f \"']", "");
        if (!text.replace("%20", "").contains(KFL_1) && !text.replace("%20", "").contains(KFL_2)) {
            error.append("report doesn't contain Known Failure Analysis block\n");
        }

        if (reportDialog.isKFLCheckForTestcases()) {

            String s = TEST_CASES_TEXT;
            int i = text.indexOf(s);
            if (i < 0) {
                error.append("report doesn't contain Test Cases column in KFL section while the option is turned on\n");
            } else {
                int j = text.indexOf(')', i + s.length());
                res.testcases = Integer.parseInt(text.substring(i + s.length(), j));
            }


            s = TC_F2P_LINK;
            i = text.indexOf(s);
            if (i < 0) {
                error.append("report doesn't contain Fail2Passed TestCases link in KFL section while the option is turned on\n");
            } else {
                int j = text.indexOf('<', i + s.length());
                res.tc_f2p = Integer.parseInt(text.substring(i + s.length(), j));
            }


            if (reportDialog.isKFLFail2Error()) {
                s = TC_F2E_LINK;
                i = text.indexOf(s);
                if (i < 0) {
                    error.append("report doesn't contain Fail2Error TestCases link in KFL section while the option is turned on\n");
                } else {
                    int j = text.indexOf('<', i + s.length());
                    res.tc_f2e = Integer.parseInt(text.substring(i + s.length(), j));
                }
            }


            s = TC_F2N_LINK;
            i = text.indexOf(s);
            if (i < 0) {
                error.append("report doesn't contain Fail2NotRun TestCases link in KFL section while the option is turned on\n");
            } else {
                int j = text.indexOf('<', i + s.length());
                res.tc_f2n = Integer.parseInt(text.substring(i + s.length(), j));
            }


            if (reportDialog.isKFLFail2Missing()) {
                s = TC_F2M_LINK;
                i = text.indexOf(s);
                if (i < 0) {
                    error.append("report doesn't contain Fail2Missing TestCases link in KFL section while the option is turned on\n");
                } else {
                    int j = text.indexOf('<', i + s.length());
                    res.tc_f2m = Integer.parseInt(text.substring(i + s.length(), j));
                }
            }


            s = TC_NEW_LINK;
            i = text.indexOf(s);
            if (i < 0) {
                error.append("report doesn't contain NewFailures TestCases link in KFL section while the option is turned on\n");
            } else {
                int j = text.indexOf('<', i + s.length());
                res.tc_nf = Integer.parseInt(text.substring(i + s.length(), j));
            }


            if (reportDialog.isKFLFail2Fail()) {
                if (!text.contains(NOT_CALC)) {
                    error.append("report doesn't contain 'not calculated' label that should appear in Fail2Fail TestCases cell\n");
                }
            }

        } else {
            if (text.contains(TEST_CASES_TEXT)) {
                error.append("report contain TestCases column while option is turned off\n");
            }
        }


        String s = F2F;
        int i = text.indexOf(s);
        if (reportDialog.isKFLFail2Fail()) {
            if (i < 0) {
                error.append("report doesn't contain Fail2Fail link while option is turned on\n");
            } else {
                int j = text.indexOf('<', i + s.length());
                res.f2f = Integer.parseInt(text.substring(i + s.length(), j));
            }
        } else {
            if (i > -1) {
                error.append("report contain Fail2Fail link while option is turned off\n");
            }
        }

        s = F2E;
        i = text.indexOf(s);
        if (reportDialog.isKFLFail2Error()) {
            if (i < 0) {
                error.append("report doesn't contain Fail2Error link while option is turned on\n");
            } else {
                int j = text.indexOf('<', i + s.length());
                res.f2e = Integer.parseInt(text.substring(i + s.length(), j));
            }
        } else {
            if (i > -1) {
                error.append("report contain Fail2Error link while option is turned off\n");
            }
        }


        s = F2M;
        i = text.indexOf(s);
        if (reportDialog.isKFLFail2Missing()) {
            if (i < 0) {
                error.append("report doesn't contain Fail2Missing link while option is turned on\n");
            } else {
                int j = text.indexOf('<', i + s.length());
                res.f2m = Integer.parseInt(text.substring(i + s.length(), j));
            }
        } else {
            if (i > -1) {
                error.append("report contain Fail2Missing link while option is turned off\n");
            }
        }


        s = TESTS_TEXT;
        i = text.indexOf(s);
        if (i < 0) {
            error.append("report doesn't contain 'Tests' label\n");
        } else {
            int j = text.indexOf(')', i + s.length());
            res.tests = Integer.parseInt(text.substring(i + s.length(), j));
        }


        s = F2P;
        i = text.indexOf(s);
        if (i < 0) {
            error.append("report doesn't contain Fail2Pass link\n");
        } else {
            int j = text.indexOf('<', i + s.length());
            res.f2p = Integer.parseInt(text.substring(i + s.length(), j));
        }


        s = F2N;
        i = text.indexOf(s);
        if (i < 0) {
            error.append("report doesn't contain Fail2NotRun link\n");
        } else {
            int j = text.indexOf('<', i + s.length());
            res.f2n = Integer.parseInt(text.substring(i + s.length(), j));
        }


        s = NF;
        i = text.indexOf(s);
        if (i < 0) {
            error.append("report doesn't contain NewFailures link\n");
        } else {
            int j = text.indexOf('<', i + s.length());
            res.nf = Integer.parseInt(text.substring(i + s.length(), j));
        }

        if (error.length() > 0) {
            throw new JemmyException(error.toString() + "\n" + text);
        }

        return res;
    }

    public static class KFLValues {

        public KFLValues() {
        }

        public KFLValues(int tests, int f2f, int f2e, int f2p, int f2m, int f2n, int nf, int testcases, int tc_f2e, int tc_f2p, int tc_f2m, int tc_f2n, int tc_nf) {
            this.tc_f2e = tc_f2e;
            this.tc_f2p = tc_f2p;
            this.tc_f2m = tc_f2m;
            this.tc_f2n = tc_f2n;
            this.tc_nf = tc_nf;
            this.f2f = f2f;
            this.f2e = f2e;
            this.f2p = f2p;
            this.f2m = f2m;
            this.f2n = f2n;
            this.nf = nf;
            this.tests = tests;
            this.testcases = testcases;
        }
        public int tc_f2e = -1;
        public int tc_f2p = -1;
        public int tc_f2m = -1;
        public int tc_f2n = -1;
        public int tc_nf = -1;
        public int f2f = -1;
        public int f2e = -1;
        public int f2p = -1;
        public int f2m = -1;
        public int f2n = -1;
        public int nf = -1;
        public int tests = -1;
        public int testcases = -1;

        @Override
        public String toString() {
            return String.format("tests %d f2f %d f2e %d f2p %d f2m %d f2n %d nf %d testcases %d tc_f2e %d tc_f2p %d tc_f2m %d tc_f2n %d tc_nf %d", tests, f2f, f2e, f2p, f2m, f2n, nf, testcases, tc_f2e, tc_f2p, tc_f2m, tc_f2n, tc_nf);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final KFLValues other = (KFLValues) obj;
            if (this.tc_f2e != other.tc_f2e) {
                return false;
            }
            if (this.tc_f2p != other.tc_f2p) {
                return false;
            }
            if (this.tc_f2m != other.tc_f2m) {
                return false;
            }
            if (this.tc_f2n != other.tc_f2n) {
                return false;
            }
            if (this.tc_nf != other.tc_nf) {
                return false;
            }
            if (this.f2f != other.f2f) {
                return false;
            }
            if (this.f2e != other.f2e) {
                return false;
            }
            if (this.f2p != other.f2p) {
                return false;
            }
            if (this.f2m != other.f2m) {
                return false;
            }
            if (this.f2n != other.f2n) {
                return false;
            }
            if (this.nf != other.nf) {
                return false;
            }
            if (this.tests != other.tests) {
                return false;
            }
            if (this.testcases != other.testcases) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 37 * hash + this.tc_f2e;
            hash = 37 * hash + this.tc_f2p;
            hash = 37 * hash + this.tc_f2m;
            hash = 37 * hash + this.tc_f2n;
            hash = 37 * hash + this.tc_nf;
            hash = 37 * hash + this.f2f;
            hash = 37 * hash + this.f2e;
            hash = 37 * hash + this.f2p;
            hash = 37 * hash + this.f2m;
            hash = 37 * hash + this.f2n;
            hash = 37 * hash + this.nf;
            hash = 37 * hash + this.tests;
            hash = 37 * hash + this.testcases;
            return hash;
        }
    }
}
