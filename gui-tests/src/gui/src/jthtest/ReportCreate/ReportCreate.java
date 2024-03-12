/*
 * $Id$
 *
 * Copyright (c) 2001, 2021, Oracle and/or its affiliates. All rights reserved.
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


package jthtest.ReportCreate;

import jthtest.ReportTools;
import jthtest.Tools;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.NameComponentChooser;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportCreate extends ReportTools {

    public static void selectCustomFilter(JFrameOperator mainFrame) {
        new JMenuOperator(mainFrame, getExecResource("ce.view.menu")).pushMenu(getExecResource("ce.view.menu") + "|" + getExecResource("fconfig.submenu.menu") + "|4 Custom", "|");
    }

    public static void chooseFilter(JDialogOperator rep, FiltersType type) {
        JComboBoxOperator chooser = new JComboBoxOperator(rep, new NameComponentChooser("fconfig.box"));
        switch (type) {
            case LAST_TEST_RUN:
                chooser.setSelectedIndex(0);
                break;
            case CURRENT_CONFIGURATION:
                chooser.setSelectedIndex(1);
                break;
//        case CURRENT_TEMPLATE: chooser.setSelectedIndex(2); break;
            case ALL_TESTS:
                chooser.setSelectedIndex(2);
                break;
            case CUSTOM:
                chooser.setSelectedIndex(3);
                break;
        }
    }

    public static void checkSpecifiedReportFiles(HtmlReport report, String path) {
        if (report.isFGenerateFailed()) {
            if (!(new File(path + "html" + File.separator + "failed.html").exists())) {
                throw new JemmyException("failed.html was not created");
            }
        } else {
            if (new File(path + "html" + File.separator + "falied.html").exists()) {
                throw new JemmyException("failed.html was created while unexpected");
            }
        }

        if (report.isFGenerateError()) {
            if (!(new File(path + "html" + File.separator + "error.html").exists())) {
                throw new JemmyException("passed.html was not created");
            }
        } else {
            if (new File(path + "html" + File.separator + "error.html").exists()) {
                throw new JemmyException("passed.html was created while unexpected");
            }
        }

        if (report.isFGenerateNotRun()) {
            if (!(new File(path + "html" + File.separator + "notRun.html").exists())) {
                throw new JemmyException("passed.html was not created");
            }
        } else {
            if (new File(path + "html" + File.separator + "notRun.html").exists()) {
                throw new JemmyException("passed.html was created while unexpected");
            }
        }

        if (report.isFGeneratePassed()) {
            if (!(new File(path + "html" + File.separator + "passed.html").exists())) {
                throw new JemmyException("passed.html was not created");
            }
        } else {
            if (new File(path + "html" + File.separator + "passed.html").exists()) {
                throw new JemmyException("passed.html was created while unexpected");
            }
        }

    }

    public static void createFakeRepDir(String path) {
        try {
            if (new File(path).exists()) {
                deleteDirectory(new File(path));
            }
            new File(path).mkdirs();
            new File(path + "index.html").createNewFile();
            new File(path + "reportdir.dat").createNewFile();
            new File(path + "html").mkdir();
            new File(path + "html" + File.separator + "report.html").createNewFile();
            new File(path + "html" + File.separator + "report.css").createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(ReportCreate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static class HtmlReport {

        private final String TAB_FILES_NAME = "HTML Files";
        private final String TAB_OPTIONS_NAME = "HTML Options";
        private JDialogOperator rep;
        private Tabs tab;
        private boolean fPutInIndex, fPutInReport, fGeneratePassed, fGenerateError, fGenerateFailed,
                fGenerateNotRun;
        private boolean eBackUp;
        private int eBackUpNum;
        private boolean oConfig, oConfig_QuestionLog, oConfig_TestEnv, oConfig_StdValues, oResults, oKeywords;

        public HtmlReport(JDialogOperator d) {
            rep = d;
            tab = Tabs.HTML_OPTIONS_TAB;
            select();
        }

        public HtmlReport(JDialogOperator d, boolean init) {
            rep = d;
            tab = Tabs.UNSET;
            select();

            if (init) {
                eBackUp = new JCheckBoxOperator(rep, new NameComponentChooser("nrd.backup.bak")).isSelected();
                eBackUpNum = Integer.parseInt(getTextField(rep, getExecResource("nrd.backup.bak.level.lbl")).getText());
                selectTab(Tabs.HTML_FILES_TAB);
                fPutInIndex = new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlf.idx")).isSelected();
                fPutInReport = new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlf.rpt")).isSelected();
                fGeneratePassed = new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlf.pass")).isSelected();
                fGenerateError = new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlf.err")).isSelected();
                fGenerateFailed = new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlf.fail")).isSelected();
                fGenerateNotRun = new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlf.nr")).isSelected();
                selectTab(Tabs.HTML_OPTIONS_TAB);
                oConfig = new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlops.config")).isSelected();
                oConfig_QuestionLog = new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlops.ql")).isSelected();
                oConfig_TestEnv = new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlops.env")).isSelected();
                oConfig_StdValues = new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlops.std")).isSelected();
                oResults = new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlops.res")).isSelected();
                oKeywords = new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlops.kw")).isSelected();
            } else {
                setExtraBackUp(true);
                setExtraBackUpNum(1);
                setFilesAll(true);
                setOptionsAll(true);
            }
        }

        public boolean isEBackUp() {
            return eBackUp;
        }

        public int getEBackUpNum() {
            return eBackUpNum;
        }

        public boolean isFGenerateError() {
            return fGenerateError;
        }

        public boolean isFGenerateFailed() {
            return fGenerateFailed;
        }

        public boolean isFGenerateNotRun() {
            return fGenerateNotRun;
        }

        public boolean isFGeneratePassed() {
            return fGeneratePassed;
        }

        public boolean isFPutInIndex() {
            return fPutInIndex;
        }

        public boolean isFPutInReport() {
            return fPutInReport;
        }

        public boolean isOConfig() {
            return oConfig;
        }

        public boolean isOConfig_QuestionLog() {
            return oConfig_QuestionLog;
        }

        public boolean isOConfig_StdValues() {
            return oConfig_StdValues;
        }

        public boolean isOConfig_TestEnv() {
            return oConfig_TestEnv;
        }

        public boolean isOKeywords() {
            return oKeywords;
        }

        public boolean isOResults() {
            return oResults;
        }

        private void select() {
            setHtmlChecked(rep, true);
        }

        public void selectTab(Tabs tab) {
            switch (tab) {
                case HTML_FILES_TAB:
                    new JTabbedPaneOperator(rep, new NameComponentChooser("nrd.tabs")).selectPage(TAB_FILES_NAME);
                    break;
                case HTML_OPTIONS_TAB:
                    new JTabbedPaneOperator(rep, new NameComponentChooser("nrd.tabs")).selectPage(TAB_OPTIONS_NAME);
                    break;
            }
            this.tab = Tabs.HTML_FILES_TAB;
        }

        public void setExtraBackUp(boolean b) {
            new JCheckBoxOperator(rep, new NameComponentChooser("nrd.backup.bak")).setSelected(b);
            eBackUp = b;
        }

        public void setExtraBackUpNum(Integer num) {
            getTextField(rep, getExecResource("nrd.backup.bak.level.lbl")).typeText(num.toString());
            eBackUpNum = num;
        }

        public void setFilesPutInIndex(boolean b) {
            if (tab != Tabs.HTML_FILES_TAB) {
                selectTab(Tabs.HTML_FILES_TAB);
            }
            new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlf.idx")).setSelected(b);
            fPutInIndex = b;
        }

        public void setFilesPutInReport(boolean b) {
            if (tab != Tabs.HTML_FILES_TAB) {
                selectTab(Tabs.HTML_FILES_TAB);
            }
            new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlf.rpt")).setSelected(b);
            fPutInReport = b;
        }

        public void setFilesGeneratePassedTests(boolean b) {
            if (tab != Tabs.HTML_FILES_TAB) {
                selectTab(Tabs.HTML_FILES_TAB);
            }
            new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlf.pass")).setSelected(b);
            fGeneratePassed = b;
        }

        public void setFilesGenerateErrorTests(boolean b) {
            if (tab != Tabs.HTML_FILES_TAB) {
                selectTab(Tabs.HTML_FILES_TAB);
            }
            new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlf.err")).setSelected(b);
            fGenerateError = b;
        }

        public void setFilesGenerateFailedTests(boolean b) {
            if (tab != Tabs.HTML_FILES_TAB) {
                selectTab(Tabs.HTML_FILES_TAB);
            }
            new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlf.fail")).setSelected(b);
            fGenerateFailed = b;
        }

        public void setFilesGenerateNotRunTests(boolean b) {
            if (tab != Tabs.HTML_FILES_TAB) {
                selectTab(Tabs.HTML_FILES_TAB);
            }
            new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlf.nr")).setSelected(b);
            fGenerateNotRun = b;
        }

        public void setOptionsConfiguration(boolean config, boolean questionLog, boolean testEnviroment, boolean standartVal) {
            if (tab != Tabs.HTML_OPTIONS_TAB) {
                selectTab(Tabs.HTML_OPTIONS_TAB);
            }
            new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlops.config")).setSelected(config);
            oConfig = config;
            if (config) {
                new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlops.ql")).setSelected(questionLog);
                oConfig_QuestionLog = questionLog;
                new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlops.env")).setSelected(testEnviroment);
                oConfig_TestEnv = testEnviroment;
                new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlops.std")).setSelected(standartVal);
                oConfig_StdValues = standartVal;
            } else {
                oConfig_QuestionLog = oConfig_StdValues = oConfig_TestEnv = false;
            }
        }

        public void setOptionsResults(boolean b) {
            if (tab != Tabs.HTML_OPTIONS_TAB) {
                selectTab(Tabs.HTML_OPTIONS_TAB);
            }
            new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlops.res")).setSelected(b);
            oResults = b;
        }

        public void setOptionsKeyword(boolean b) {
            if (tab != Tabs.HTML_OPTIONS_TAB) {
                selectTab(Tabs.HTML_OPTIONS_TAB);
            }
            new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlops.kw")).setSelected(b);
            oKeywords = b;
        }

        public void setOptionsAll(boolean b) {
            if (tab != Tabs.HTML_OPTIONS_TAB) {
                selectTab(Tabs.HTML_OPTIONS_TAB);
            }
            new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlops.ql")).setSelected(b);
            new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlops.env")).setSelected(b);
            new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlops.std")).setSelected(b);
            new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlops.config")).setSelected(b);
            new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlops.res")).setSelected(b);
            new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlops.kw")).setSelected(b);
            oConfig = oConfig_QuestionLog = oConfig_StdValues = oConfig_TestEnv = oKeywords = oResults = b;
        }

        public void setFilesAll(boolean b) {
            if (tab != Tabs.HTML_FILES_TAB) {
                selectTab(Tabs.HTML_FILES_TAB);
            }
            new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlf.nr")).setSelected(b);
            new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlf.fail")).setSelected(b);
            new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlf.err")).setSelected(b);
            new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlf.pass")).setSelected(b);
            new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlf.rpt")).setSelected(b);
            new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlf.idx")).setSelected(b);
            fGenerateError = fGenerateFailed = fGenerateNotRun = fGeneratePassed = fPutInIndex = fPutInReport = b;
        }

        public enum Tabs {

            HTML_FILES_TAB, HTML_OPTIONS_TAB, UNSET
        }
    }

    public static class ReportBrowser {

        private JDialogOperator browser;
        private String text;
        private String[] urls;
        private String basePath;

        public ReportBrowser(String path) {
            browser = new JDialogOperator("Report Browser");
            text = null;
            urls = null;
            basePath = path;
        }

        public void waitForPageLoading(String ends, String contains) {
            int time = 0;
            ends = ends.replaceAll("[\t\n\r\f ]", "");
            contains = contains.replaceAll("[\t\n\r\f ]", "");
            String text = getTextForced().replaceAll("[\t\n\r\f ]", "");
            do {
                try {
                    if (time > 60000) {
                        throw new JemmyException("Page containing '" + contains + "' and '" + ends + "' was not opened. Full page: \n" + text);
                    }
                    Thread.sleep(100);
                    time += 100;
                    text = getTextForced().replaceAll("[\t\n\r\f ]", "");
                } catch (InterruptedException ex) {
                    Logger.getLogger(ReportCreate.class.getName()).log(Level.SEVERE, null, ex);
                }
            } while (!text.contains(ends) || !text.contains(contains));
        }

        public JDialogOperator getBrowser() {
            return browser;
        }

        public void home() {
            new JButtonOperator(browser, new NameComponentChooser("Home")).push();
            urls = null;
            text = null;
        }

        public String[] getUrls() {
            if (urls != null) {
                return urls;
            }
            if (text == null) {
                getText();
            }

            int pos = 0;
            int last = 0;
            ArrayList<String> tempList = new ArrayList<String>();
            while ((pos = text.indexOf("href", pos)) != -1) {
                pos += 6;
                last = text.indexOf("\"", pos);
                tempList.add(text.substring(pos, last));
                pos = last;
            }
            urls = new String[tempList.size()];
            urls = tempList.toArray(urls);
            return urls;
        }

        public void clickUrl(String url) throws MalformedURLException {
            clickUrl(new URL(url));
        }

        public void clickUrl(final URL url) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    public void run() {
                        JEditorPane editor = (JEditorPane) (new JEditorPaneOperator(browser).getSource());
                        final HyperlinkEvent hyperlinkEvent = new HyperlinkEvent(editor, HyperlinkEvent.EventType.ACTIVATED, url);
                        new JEditorPaneOperator(browser, new NameComponentChooser("text")).fireHyperlinkUpdate(hyperlinkEvent);
                        urls = null;
                        text = null;
                        Tools.pause(2);
                    }
                });
            } catch (InterruptedException ex) {
                Logger.getLogger(ReportCreate.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(ReportCreate.class.getName()).log(Level.SEVERE, null, ex);
            }
            urls = null;
            text = null;
        }

        public String getText() {
            if (text == null) {
                text = new JEditorPaneOperator(browser, new NameComponentChooser("text")).getText();
            }
            return text;
        }

        public URL getPath() {
            JComboBoxOperator p = new JComboBoxOperator(browser, new NameComponentChooser("np.choice"));
            URL u = (URL) (p.getModel().getSelectedItem());
            return u;
        }

        private String getTextForced() {
            text = new JEditorPaneOperator(browser, new NameComponentChooser("text")).getText();
            return text;
        }
    }

    public static class HtmlReportChecker {

        private String basePath;
        private ReportBrowser browser;
        private HtmlReport htmlReport;

        public HtmlReportChecker(String path, HtmlReport report) {
            basePath = path;
            browser = new ReportBrowser(path);
            htmlReport = report;
        }

        ;

        private void checkReportPage() throws JemmyException {
            String htmlPath = basePath + "html" + File.separator;
            String[] urls = browser.getUrls();
            StringBuffer error = new StringBuffer("");
            int i = -1;

            if (htmlReport.isFGenerateError()) {
                if (!(new File(htmlPath + "error.html").exists())) {
                    error.append("error.html was not found at the path '" + htmlPath + "' while expected\n");
                }
            } else {
                if (new File(htmlPath + "error.html").exists()) {
                    error.append("error.html was found at the path '" + htmlPath + "' while unexpected\n");
                }
            }

            if (htmlReport.isFGenerateFailed()) {
                if (!(new File(htmlPath + "failed.html").exists())) {
                    error.append("failed.html was not found at the path '" + htmlPath + "' while expected\n");
                }
            } else {
                if (new File(htmlPath + "failed.html").exists()) {
                    error.append("failed.html was found at the path '" + htmlPath + "' while unexpected\n");
                }
            }

            if (htmlReport.isFGenerateNotRun()) {
                if (!(new File(htmlPath + "notRun.html").exists())) {
                    error.append("notRun.html was not found at the path '" + htmlPath + "' while expected\n");
                }
            } else {
                if (new File(htmlPath + "notRun.html").exists()) {
                    error.append("notRun.html was found at the path '" + htmlPath + "' while unexpected\n");
                }
            }

            if (htmlReport.isFGeneratePassed()) {
                if (!(new File(htmlPath + "passed.html").exists())) {
                    error.append("passed.html was not found at the path '" + htmlPath + "' while expected\n");
                }
            } else {
                if (new File(htmlPath + "passed.html").exists()) {
                    error.append("passed.html was found at the path '" + htmlPath + "' while unexpected\n");
                }
            }

            if (htmlReport.isFPutInIndex()) {
                if (!(new File(htmlPath + "index.html").exists())) {
                    error.append("index.html was not found at the path '" + htmlPath + "' while expected\n");
                }
            } else {
                if (new File(htmlPath + "index.html").exists()) {
                    error.append("index.html was found at the path '" + htmlPath + "' while unexpected\n");
                }
            }

            if (htmlReport.isFPutInReport()) {
                if (!(new File(htmlPath + "report.html").exists())) {
                    error.append("report.html was not found at the path '" + htmlPath + "' while expected\n");
                }
            } else {
                if (new File(htmlPath + "report.html").exists()) {
                    error.append("report.html was found at the path '" + htmlPath + "' while unexpected\n");
                }
            }

            if (htmlReport.isOConfig()) {
                checkFileUrls(urls);
                i = findInStringArray(urls, "#Configuration and Other Settings");
                if (i == -1) {
                    error.append("report doesn't contain configuration url '#Configuration and Other Settings'\n");
                }
                if (!browser.getText().replaceAll("[^a-zA-Z]", " ").trim().replaceAll(" +", " ").contains("Configuration and Other Settings")) {
                    error.append("report doesn't contain configuration block\n");
                }
//        i = findInStringArray(urls, "#locations");
//        if (i == -1) {
//            error.append("report doesn't contain url to 'Where to put the results' block\n");
//        }
                if (!browser.getText().contains("name=locations") && !browser.getText().contains("id=\"locations\"")) {
                    error.append("report doesn't contain 'Where to put the results' block\n");
                }

                if (htmlReport.isOConfig_QuestionLog()) {
                    i = findInStringArray(urls, "config.html");
                    if (i == -1) {
                        error.append("report doesn't contain url to config.html\n");
                    }
                    if (!(new File(htmlPath + "config.html").exists())) {
                        error.append("config.html was not found at the path '" + htmlPath + "' while expected\n");
                    }
                } else {
                    i = findInStringArray(urls, "config.html");
                    if (i != -1) {
                        error.append("report contains url to config.html while unexpected\n");
                    }
                    if (new File(htmlPath + "config.html").exists()) {
                        error.append("config.html was found at the path '" + htmlPath + "' while unexpected\n");
                    }
                }

                if (htmlReport.isOConfig_StdValues()) {
                    i = findInStringArray(urls, "#selection");
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
                    i = findInStringArray(urls, "#selection");
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

                if (htmlReport.isOConfig_TestEnv()) {
//            i = findInStringArray(urls, "#execution");
//            if (i == -1) {
//            error.append("report doesn't contain url to test enviroment ('How to run') block\n");
//            }
                    if (!browser.getText().contains("name=execution") && !browser.getText().contains("id=\"execution\"")) {
                        error.append("report doesn't contain test enviroment ('How to run') block\n");
                    }
                    if (!(new File(htmlPath + "env.html").exists())) {
                        error.append("env.html was not found at the path '" + htmlPath + "' while expected\n");
                    }
                } else {
                    i = findInStringArray(urls, "#execution");
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
                i = findInStringArray(urls, "#Configuration and Other Settings");
                if (i != -1) {
                    error.append("report contains configuration url '#Configuration and Other Settings' while unexpected\n");
                }
                if (browser.getText().contains("name=\"Configuration and Other Settings\"")) {
                    error.append("report contains configuration block while unexpected\n");
                }
                i = findInStringArray(urls, "config.html");
                if (i != -1) {
                    error.append("report contains url to config.html while unexpected\n");
                }
                if (new File(htmlPath + "config.html").exists()) {
                    error.append("config.html was found at the path '" + htmlPath + "' while unexpected\n");
                }
                i = findInStringArray(urls, "#selection");
                if (i != -1) {
                    error.append("report contains url to standart values block while unexpected\n");
                }
                if (browser.getText().contains("name=\"selection\"")) {
                    error.append("report contains standart values block while unexpected\n");
                }
                i = findInStringArray(urls, "#locations");
                if (i != -1) {
                    error.append("report contains url to 'Where to put the results' block while unexpected\n");
                }
                if (browser.getText().contains("name=\"locations\"")) {
                    error.append("report contains 'Where to put the results' block while unexpected\n");
                }
                i = findInStringArray(urls, "#execution");
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

            if (htmlReport.isOKeywords()) {
                i = findInStringArray(urls, "#Statistics");
                if (i == -1) {
                    error.append("report doesn't contain url to Statistics block (produced by options-keywords)\n");
                }
                if (!browser.getText().contains("name=Statistics") && !browser.getText().contains("id=\"Statistics\"")) {
                    error.append("report doesn't contain Statistics block (produced by options-keywords)\n");
                }
                i = findInStringArray(urls, "#keywordSummary");
                if (i == -1) {
                    error.append("report doesn't contain url to keywordSummary block\n");
                }
                if (!browser.getText().contains("name=keywordSummary") && !browser.getText().contains("id=\"keywordSummary\"")) {
                    error.append("report doesn't contain keywordSummary block\n");
                }
            } else {
                i = findInStringArray(urls, "#Statistics");
                if (i != -1) {
                    error.append("report contains url to Statistics block while unexpected\n");
                }
                if (browser.getText().contains("name=\"Statistics\"")) {
                    error.append("report contains Statistics block while unexpected\n");
                }
                i = findInStringArray(urls, "#keywordSummary");
                if (i != -1) {
                    error.append("report contains url to keywordSummary block while unexpected\n");
                }
                if (browser.getText().contains("name=\"keywordSummary\"")) {
                    error.append("report contains keywordSummary block while unexpected\n");
                }
            }

            if (htmlReport.isOResults()) {
                i = findInStringArray(urls, "#Results");
                if (i == -1) {
                    error.append("report doesn't contain url to Results block\n");
                }
                if (!browser.getText().contains("name=Results") && !browser.getText().contains("id=\"Results\"")) {
                    error.append("report doesn't contain Results block\n");
                }
            } else {
                i = findInStringArray(urls, "#Results");
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
            if (htmlReport.isFPutInIndex()) {
                i = findInStringArray(urls, "html/index.html");
                t = "index";
            } else {
                if (htmlReport.isFPutInReport()) {
                    i = findInStringArray(urls, "html/report.html");
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

        private void goToHtmlReport(ReportType type) {
            String path = browser.getPath().getPath();
            if (path.equals(basePath + "html/report.html") && type == ReportType.REPORT_HTML) {
                return;
            }
            if (path.equals(basePath + "html/index.html") && type == ReportType.INDEX_HTML) {
                return;
            }

            if (type == ReportType.INDEX_HTML && htmlReport.isFPutInIndex()) {
                if (!path.equals(basePath + "index.html")) {
                    browser.home();
                    browser.waitForPageLoading("</html>", TESTSUITENAME);
                }
                String url = getUrlToHtmlReportPage();

                URL u = urlFile(basePath + File.separator + url);
                browser.clickUrl(u);
                browser.waitForPageLoading("</html>", "<title>" + WINDOWNAME + " Harness : Report");
            }

            if (type == ReportType.REPORT_HTML && htmlReport.isFPutInReport()) {
                URL u = urlFile(basePath + File.separator + "html");
                u = urlFile(basePath + File.separator + "html" + File.separator + "report.html");
                browser.clickUrl(u);
                browser.waitForPageLoading("</html>", "<title>" + WINDOWNAME + " Harness : Report");
            }
        }

        public void commitMainCheck() {
            if (htmlReport.isFPutInIndex()) {
                goToHtmlReport(ReportType.INDEX_HTML);
                checkReportPage();
            }
            if (htmlReport.isFPutInReport()) {
                goToHtmlReport(ReportType.REPORT_HTML);
                checkReportPage();
            }
            if (!htmlReport.isFPutInIndex() && !htmlReport.isFPutInReport()) {
                throw new JemmyException("NIY");
            }


        }

        private StringBuffer checkFileUrls(String urls[]) {
            StringBuffer temp = new StringBuffer("");
            boolean ts = false, wd = false, rp = false;
            for (int i = 0; i < urls.length; i++) {
                if (urls[i].equals(LOCAL_PATH + TEST_SUITE_NAME)) {
                    ts = true;
                }
                if (urls[i].equals(LOCAL_PATH + REPORT_WD_PATH)) {
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

        private enum ReportType {

            INDEX_HTML, REPORT_HTML
        }
    }
}
