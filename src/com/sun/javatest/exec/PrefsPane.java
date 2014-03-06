/*
 * $Id$
 *
 * Copyright (c) 2002, 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.exec;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Map;
import javax.help.HelpBroker;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.sun.javatest.report.ReportManager;
import com.sun.javatest.tool.Preferences;
import com.sun.javatest.tool.PreferencesPane;
import com.sun.javatest.tool.UIFactory;
import java.awt.Font;


class PrefsPane extends PreferencesPane
{
    PrefsPane(HelpBroker helpBroker) {
        uif = new UIFactory(this, helpBroker);
        initGUI();
    }

    @Override
    public PreferencesPane[] getChildPanes() {
        if (configEditorPane == null)
            configEditorPane = new ConfigEditorPane();
        if (reportingPane == null)
            reportingPane = new ReportingPane();

        if (childPanes == null)
            childPanes = new PreferencesPane[] { configEditorPane, reportingPane };
        return childPanes;
    }

    public String getText() {
        return uif.getI18NString("ep.title");
    }

    @Override
    public void load(Map m) {
        super.load(m);
        String p = (String) (m.get(ExecTool.TOOLBAR_PREF));
        toolBarChk.setSelected(p == null || p.equals("true"));
        p = (String) (m.get(ExecTool.FILTER_WARN_PREF));
        filterWarnChk.setSelected(p == null || p.equals("true"));
        p = (String) (m.get(ExecTool.TESTS2RUN_PREF));
        tests2RunChk.setSelected((p == null ? false : p.equals("true")));
        p = (String) (m.get("javatest.sortExecution"));
        testSortingChk.setSelected((p == null ? false : p.equals("false")));
        p = (String) (m.get(TP_OutputSubpanel.LINE_WRAP_PREF));
        // selected by default
        wrapResChk.setSelected((p == null ? true : p.equals("true")));
    }

    @Override
    public void save(Map m) {
        super.save(m);
        m.put(ExecTool.TOOLBAR_PREF, String.valueOf(toolBarChk.isSelected()));
        m.put(ExecTool.FILTER_WARN_PREF, String.valueOf(filterWarnChk.isSelected()));
        m.put(ExecTool.TESTS2RUN_PREF, String.valueOf(tests2RunChk.isSelected()));
        m.put("javatest.sortExecution", Boolean.toString(!testSortingChk.isSelected()));
        m.put(TP_OutputSubpanel.LINE_WRAP_PREF, String.valueOf(wrapResChk.isSelected()));
    }

    private void initGUI() {
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;

        add(createToolBarPanel(), c);
        add(createFilterPanel(), c);
        add(createExecutionPanel(), c);
        add(createTestRunMsgPanel(), c);

        c.weighty = 1;
        add(Box.createVerticalGlue(), c);

    }

    private JPanel createToolBarPanel() {
        JPanel p = uif.createPanel("exec.prefs", new GridBagLayout(), false);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        p.setBorder(uif.createTitledBorder("ep.toolbar"));
        toolBarChk = uif.createCheckBox("ep.toolbar", true);
        // override default a11y name
        uif.setAccessibleName(toolBarChk, "ep.toolbar");
        p.add(toolBarChk, c);
        return p;
    }

    private JPanel createFilterPanel() {
        // could have setting to ask user what the default filter to use is
        JPanel p = uif.createPanel("exec.prefs.filter", new GridBagLayout(), false);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        p.setBorder(uif.createTitledBorder("ep.filt"));
        filterWarnChk= uif.createCheckBox("ep.filt", true);
        // override default a11y name
        uif.setAccessibleName(filterWarnChk, "ep.filt");
        p.add(filterWarnChk, c);
        return p;
    }

    private JPanel createExecutionPanel() {
        JPanel p = uif.createPanel("exec.prefs.exec", new GridBagLayout(), false);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        p.setBorder(uif.createTitledBorder("ep.exec"));

        tests2RunChk= uif.createCheckBox("ep.tests2run", true);
        // override default a11y name
        uif.setAccessibleName(tests2RunChk, "ep.tests2run");
        p.add(tests2RunChk, c);

        testSortingChk = uif.createCheckBox("ep.sorttests", true);
        c.gridy = GridBagConstraints.PAGE_END;
        p.add(testSortingChk, c);
        return p;
    }

    private JPanel createTestRunMsgPanel() {
        JPanel p = uif.createPanel("exec.prefs.testrun", new GridBagLayout(), false);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        p.setBorder(uif.createTitledBorder("ep.testrun"));
        wrapResChk= uif.createCheckBox("ep.wrapres", true);
        uif.setAccessibleName(wrapResChk, "ep.wrapres");
        p.add(wrapResChk, c);
        return p;
    }


    private UIFactory uif;
    private JCheckBox toolBarChk;
    private JCheckBox filterWarnChk;
    private JCheckBox tests2RunChk;
    private JCheckBox testSortingChk;   // deceptive name, see i18n description
    private JCheckBox wrapResChk;
    private ConfigEditorPane configEditorPane;
    private ReportingPane reportingPane;
    private PreferencesPane[] childPanes;

    private class ConfigEditorPane extends PreferencesPane {
        ConfigEditorPane() {
            initGUI();
        }

        public String getText() {
            return uif.getI18NString("ep.ce.title");
        }

        @Override
        public void load(Map m) {
            String mp = (String) (m.get(InterviewEditor.MORE_INFO_PREF));
            moreInfoChk.setSelected(mp == null || mp.equals("true"));
        }

        @Override
        public void save(Map m) {
            m.put(InterviewEditor.MORE_INFO_PREF, String.valueOf(moreInfoChk.isSelected()));
        }

        private void initGUI() {
            setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 1;

            add(createDefaultViewPanel(), c);

            c.weighty = 1;
            add(Box.createVerticalGlue(), c);
        }

        private JPanel createDefaultViewPanel() {
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.WEST;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets.left = 10;
            c.weightx = 1;
            c.weighty = 0;

            JPanel p = new JPanel(new GridBagLayout());

            JTextArea infoTa = uif.createMessageArea("ep.ce.info");
            infoTa.setOpaque(false);
            add(infoTa, c);

            p.setBorder(uif.createTitledBorder("ep.ce.defView"));
            moreInfoChk = uif.createCheckBox("ep.ce.moreInfo", true);
            // override default a11y name
            uif.setAccessibleName(moreInfoChk, "ep.ce.moreInfo");

            c.insets.top = 10;
            p.add(moreInfoChk, c);
            return p;
        }

        private JCheckBox moreInfoChk;
    }

    private class ReportingPane extends PreferencesPane {
        ReportingPane() {
            initGUI();
        }

        public String getText() {
            return uif.getI18NString("ep.rpt.title");
        }

        @Override
        public void load(Map m) {
            String mp = (String) (m.get(ReportManager.BUGRPT_URL_PREF));
            bugUrlTf.setText(mp);
        }

        @Override
        public void save(Map m) {
            m.put(ReportManager.BUGRPT_URL_PREF, bugUrlTf.getText());
        }

        private void initGUI() {
            setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 1;

            add(createDefaultViewPanel(), c);

            c.weighty = 1;
            add(Box.createVerticalGlue(), c);
        }

        private JPanel createDefaultViewPanel() {
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.BASELINE_LEADING;
            c.gridwidth = 1;
            //c.insets.left = 10;
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = 0;
            c.weighty = c.weightx = 1;

            JPanel p = new JPanel(new GridBagLayout());
            JLabel lbl = uif.createLabel("ep.rpt.url");
            //JTextArea infoTa = uif.createMessageArea("ep.rpt.url");
            //lbl.setOpaque(false);
            p.add(lbl, c);

            bugUrlTf = uif.createInputField("ep.rpt.url", lbl);
            bugUrlTf.setColumns(30);
            // override default a11y name
            uif.setAccessibleName(bugUrlTf, "ep.rpt.url");

            //c.insets.top = 10;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 5;
            p.add(bugUrlTf, c);

            JTextArea hint = uif.createMessageArea("ep.rpt.url");
            hint.setFont(hint.getFont().deriveFont(Font.ITALIC));
            hint.setBorder(BorderFactory.createEmptyBorder());
            c.gridy = c.gridx = 1;    // under bugUrlTf
            c.weightx = 1;
            p.add(hint, c);

            return p;
        }

        private JTextField bugUrlTf;
    }
}
