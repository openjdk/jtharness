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


package jthtest.tools;

import jthtest.Tools;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.NameComponentChooser;

import javax.swing.*;

public class ReportDialog {

    private final String TAB_FILES_NAME = "HTML Files";
    private final String TAB_OPTIONS_NAME = "HTML Options";
    private JDialogOperator rep;
    private Tabs tab;
    private boolean fPutInIndex, fPutInReport, fGeneratePassed, fGenerateError, fGenerateFailed,
            fGenerateNotRun;
    private boolean eBackUp;
    private int eBackUpNum;
    private boolean oConfig, oConfig_QuestionLog, oConfig_TestEnv, oConfig_StdValues, oResults, oKeywords, kflTC, kflf2f, kflf2e, kflf2m;

    public ReportDialog(JDialogOperator d) {
        rep = d;
        tab = Tabs.HTML_OPTIONS_TAB;
        select();
    }

    public ReportDialog(JDialogOperator d, boolean init) {
        rep = d;
        tab = Tabs.UNSET;
        select();

        if (init) {
            eBackUp = new JCheckBoxOperator(rep, new NameComponentChooser("nrd.backup.bak")).isSelected();
            eBackUpNum = Integer.parseInt(Tools.getTextField(rep, Tools.getExecResource("nrd.backup.bak.level.lbl")).getText());
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
            selectTab(Tabs.KFL_OPTIONS_TAB);
            kflTC = new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlkfl.checktc")).isSelected();
            kflf2e = new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlkfl.f2e")).isSelected();
            kflf2f = new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlkfl.f2f")).isSelected();
            kflf2m = new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlkfl.missing")).isSelected();
        } else {
            setExtraBackUp(true);
            setExtraBackUpNum(1);
            setFilesAll(true);
            setOptionsAll(true);
            setKFLAll(true);
        }
    }

    public static boolean setHtmlChecked(JDialogOperator rep, boolean set) {
        JCheckBox element = getListElement(rep, 0);
        boolean temp = element.isSelected();
        element.setSelected(set);
        return temp;
    }

    public static JCheckBox getListElement(JDialogOperator rep, int index) {
        JListOperator types = getList(rep);
        return (JCheckBox) types.getModel().getElementAt(index);
    }

    public static JListOperator getList(JDialogOperator rep) {
        JListOperator types = new JListOperator(rep, new NameComponentChooser("nrd.typel"));
        return types;
    }

    public static JDialogOperator findShowReportDialog() {
        return new JDialogOperator(Tools.getExecResource("nrd.showReport.title"));
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

    public boolean isKFLCheckForTestcases() {
        return kflTC;
    }

    public void setKFLCheckForTestcases(boolean b) {
        if (kflTC == b)
            return;
        if (tab != Tabs.KFL_OPTIONS_TAB) {
            selectTab(Tabs.KFL_OPTIONS_TAB);
        }
        new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlkfl.checktc")).setSelected(b);
        kflTC = b;
    }

    public boolean isKFLFail2Fail() {
        return kflf2f;
    }

    public void setKFLFail2Fail(boolean b) {
        if (kflf2f == b)
            return;
        if (tab != Tabs.KFL_OPTIONS_TAB) {
            selectTab(Tabs.KFL_OPTIONS_TAB);
        }
        new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlkfl.f2f")).setSelected(b);
        kflf2f = b;
    }

    public boolean isKFLFail2Error() {
        return kflf2e;
    }

    public void setKFLFail2Error(boolean b) {
        if (kflf2e == b)
            return;
        if (tab != Tabs.KFL_OPTIONS_TAB) {
            selectTab(Tabs.KFL_OPTIONS_TAB);
        }
        new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlkfl.f2e")).setSelected(b);
        kflf2e = b;
    }

    public boolean isKFLFail2Missing() {
        return kflf2m;
    }

    public void setKFLFail2Missing(boolean b) {
        if (kflf2m == b)
            return;
        if (tab != Tabs.KFL_OPTIONS_TAB) {
            selectTab(Tabs.KFL_OPTIONS_TAB);
        }
        new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlkfl.missing")).setSelected(b);
        kflf2m = b;
    }

    private void select() {
        setHtmlChecked(rep, true);
    }

    public boolean setHtmlChecked(boolean set) {
        JCheckBox element = getListElement(rep, 0);
        boolean temp = element.isSelected();
        element.setSelected(set);
        return temp;
    }

    public boolean setXmlChecked(boolean set) {
        JCheckBox element = getListElement(rep, 2);
        boolean temp = element.isSelected();
        element.setSelected(set);
        return temp;
    }

    public boolean setPlainChecked(boolean set) {
        JCheckBox element = getListElement(rep, 1);
        boolean temp = element.isSelected();
        element.setSelected(set);
        return temp;
    }

    public boolean setTypeChecked(int num, boolean set) {
        JCheckBox element = getListElement(rep, num);
        boolean temp = element.isSelected();
        element.setSelected(set);
        return temp;
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
        Tools.getTextField(rep, Tools.getExecResource("nrd.backup.bak.level.lbl")).typeText(num.toString());
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

    public void setKFLAll(boolean b) {
        if (tab != Tabs.KFL_OPTIONS_TAB) {
            selectTab(Tabs.KFL_OPTIONS_TAB);
        }
        new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlkfl.checktc")).setSelected(b);
        new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlkfl.f2e")).setSelected(b);
        new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlkfl.f2f")).setSelected(b);
        new JCheckBoxOperator(rep, new NameComponentChooser("nrd.htmlkfl.missing")).setSelected(b);
        kflTC = kflf2e = kflf2f = kflf2m = b;
    }

    public void setPath(String path) {
        new JButtonOperator(rep, Tools.getExecResource("nrd.browse.btn")).push();
        JDialogOperator browser = new JDialogOperator("Report Directory");
        Tools.getTextField(browser, "File Name:").typeText(path);
        new JButtonOperator(browser, "Open").push();
    }

    public void pushCreate() {
        new JButtonOperator(rep, Tools.getExecResource("nrd.ok.btn")).push();
    }

    public enum Tabs {

        HTML_FILES_TAB, HTML_OPTIONS_TAB, KFL_OPTIONS_TAB, UNSET
    }
}
