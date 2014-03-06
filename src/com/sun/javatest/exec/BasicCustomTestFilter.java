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

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Map;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.sun.interview.Interview;
import com.sun.interview.Question;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.InitialUrlFilter;
import com.sun.javatest.Keywords;
import com.sun.javatest.KeywordsFilter;
import com.sun.javatest.Parameters.ExcludeListParameters;
import com.sun.javatest.Parameters.MutableExcludeListParameters;
import com.sun.javatest.Status;
import com.sun.javatest.StatusFilter;
import com.sun.javatest.TestFilter;
import com.sun.javatest.TestDescription;
import com.sun.javatest.TestResultTable;
import com.sun.javatest.TestSuite;
import com.sun.javatest.ObservableTestFilter;
import com.sun.javatest.ObservableTestFilter.Observer;
import com.sun.javatest.util.DynamicArray;
import com.sun.javatest.util.StringArray;
import com.sun.javatest.tool.TestTreeSelectionPane;
import com.sun.javatest.tool.UIFactory;
import java.util.HashSet;

/**
 * This filter allows the user to configure the filter using
 * the filtering attributes normally found in the parameter
 * section of an interview:
 * <ul>
 * <li>Initial URLs
 * <li>Status
 * <li>Exclude List
 * <li>Keywords
 * </ul>
 *
 *<p>
 * The settings for this panel are global, so any changes made affect all
 * exec tool instances.
 */
class BasicCustomTestFilter extends ConfigurableTestFilter {
    // UIFactory parameter assume this class stays in the exec package

    BasicCustomTestFilter(String name, ExecModel e, UIFactory uif) {
        super(name, e);
        this.uif = uif;
        init(null);
    }

    BasicCustomTestFilter(Map map, ExecModel e, UIFactory uif) {
        super(map, e);
        this.uif = uif;
        init(map);
    }

    BasicCustomTestFilter(ExecModel e, UIFactory uif) {
        super(uif.getI18NString("basicTf.namePrefix"), e);
        this.uif = uif;

        init(null);
    }

    ConfigurableTestFilter cloneInstance() {
        return new BasicCustomTestFilter(uif.getI18NString("basicTf.namePrefix") +
                instanceCount, execModel, uif);
    }

    // override superclass methods
    // observers must be static data since all exec tool instances
    // must be notified of changes
    @Override
    public void addObserver(Observer o) {
        obs = (Observer[]) DynamicArray.append(obs, o);
    }

    @Override
    public void removeObserver(Observer o) {
        obs = (Observer[]) DynamicArray.remove(obs, o);
    }

    @Override
    protected void notifyUpdated(ObservableTestFilter filter) {
        // obs will be null if called indirectly via load(map) from the
        // superclass constructor, because super(...) is defined to be
        // called before instance variables are initialized.
        // (Hence the danger of overriding methods called from a superclass
        // constructor.)  11/12/02
        if (obs == null) {
            return;
        }

        for (int i = 0; i < obs.length; i++) {
            obs[i].filterUpdated(filter);
        }
    }

    @Override
    boolean load(Map map) {
        boolean result = super.load(map);
        activeSettings = new SettingsSnapshot(map);
        putSettings(activeSettings);
        activateSettings(activeSettings);

        notifyUpdated(this);

        return result;
    }

    @Override
    boolean save(Map map) {
        boolean result = super.save(map);
        activeSettings.save(map);

        return result;
    }

    void update(InterviewParameters ip) {
        activateSettings(activeSettings);
    }

    synchronized JComponent getEditorPane() {
        if (editorPane == null) {
            editorPane = uif.createTabbedPane("basicTf.tabs", createTabPanels());
            editorPane.setTabPlacement(SwingConstants.TOP);

            try {
                if (activeSettings == null) {
                    activeSettings = grabSettings();
                } else {
                    putSettings(activeSettings);
                }
            } catch (IllegalStateException e) {
                throw new IllegalStateException("Illegal state of BCTF GUI on startup.");
            }   // catch

            editorPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        }

        return editorPane;
    }

    String commitEditorSettings() {
        SettingsSnapshot nowSettings = null;

        try {
            nowSettings = grabSettings();
        } catch (IllegalStateException e) {
            // indicates that the GUI settings are not valid
            return e.getMessage();
        }   // catch

        if (!activeSettings.equals(nowSettings)) {
            return activateSettings(nowSettings);
        } else {
            return null;
        }
    }

    void resetEditorSettings() {
        // set GUI to match live settings
        putSettings(activeSettings);
    }

    boolean isEditorChanged() {
        SettingsSnapshot nowSettings = null;

        try {
            nowSettings = grabSettings();
        } catch (IllegalStateException e) {
            // indicates that the GUI settings are not valid
            return true;
        }   // catch
        if (activeSettings.equals(nowSettings)) {
            return false;
        } else {
            return true;
        }
    }

    // TestFilter interface
    public boolean accepts(TestDescription td)
            throws TestFilter.Fault {
        return accepts(td, null);
    }

    public boolean accepts(TestDescription td, TestFilter.Observer o)
            throws TestFilter.Fault {
        if (statusFilterNeedsUpdate)
            updateStatusFilter();

        for (int i = 0; i < activeFilters.length; i++) {
            if (activeFilters[i] != null &&
                    !activeFilters[i].accepts(td)) {
                if (o != null) {
                    o.rejected(td, activeFilters[i]);
                } else {
                }

                return false;
            }   // outer if
        }   // for

        return true;
    }

    public String getBaseName() {
        return NAME;
    }

    public String getName() {
        return instanceName;
    }

    public String getReason() {
        return REASON;
    }

    public String getDescription() {
        return DESCRIPTION;
    }

    // ----- PRIVATE -----
    private void init(Map map) {
        if (NAME == null) {
            NAME = uif.getI18NString("basicTf.name");
        }

        if (REASON == null) {
            REASON = uif.getI18NString("basicTf.reason");
        }

        if (DESCRIPTION == null) {
            DESCRIPTION = uif.getI18NString("basicTf.description");
        }

        if (map != null) {
            activeSettings = new SettingsSnapshot(map);
        } else {
            activeSettings = new SettingsSnapshot();
        }

        instanceCount++;

        // not using this from superclass
        observers = null;
        activateSettings(activeSettings);
    }

    /**
     * Make the given settings the active ones.
     * Except under exceptional conditions, the filters are recreated and
     * an update message is sent.
     */
    private String activateSettings(SettingsSnapshot nowSettings) {
        KeywordsFilter newKeyFilter = null;
        InitialUrlFilter newUrlFilter = null;
        TestFilter newJtxFilter = null;
        StatusFilter newStatusFilter = null;
        TestFilter newTsfFilter = null;

        InterviewParameters ip = execModel.getInterviewParameters();
        TestSuite ts = execModel.getTestSuite();
        updateInterviewObserver(ip);

        // recreate filters
        if (nowSettings.urlsEnabled) {
            // converting to strings to avoid any confusion
            // files vs urls
            if (nowSettings.initialUrls != null) {
                newUrlFilter = new InitialUrlFilter(nowSettings.initialUrls);
            }
        }

        if (nowSettings.keywordsEnabled) {
            try {
                String[] validKeywords = ts.getKeywords();
                HashSet validKeywordsSet;
                if (validKeywords == null) {
                    validKeywordsSet = null;
                } else {
                    validKeywordsSet = new HashSet(Arrays.asList(validKeywords));
                }

                Keywords kw = Keywords.create(kwModeToType(nowSettings.keyChoice),
                        nowSettings.keyString, validKeywordsSet);
                newKeyFilter = new KeywordsFilter(kw);
            } catch (Keywords.Fault f) {
                return f.getMessage();
            }

        }

        if (nowSettings.statusEnabled) {
            // this filter won't work without a TRT
            TestResultTable trt = execModel.getActiveTestResultTable();
            if (trt != null) {
                newStatusFilter = new StatusFilter(
                        nowSettings.statusFields,
                        trt);
                statusFilterNeedsUpdate = false;
            } else {
                statusFilterNeedsUpdate = true;
            }
        } else {
            // to clear any old status
            statusFilterNeedsUpdate = false;
        }

        if (nowSettings.jtxEnabled) {
            // we only support copying exclude list from interview
            // right now
            if (ip != null) {
                // may set var. to null, but that's okay
                newJtxFilter = ip.getExcludeListFilter();
            }
        }

        if (nowSettings.tsfEnabled) {
            // we only support copying exclude list from interview
            // right now
            if (ip != null && ts != null) {
                // may set filter to null, but that's okay
                newTsfFilter = ts.createTestFilter(ip.getEnv());
            }
        }

        // can abort anytime before this
        // commit now
        keyFilter = newKeyFilter;
        urlFilter = newUrlFilter;
        jtxFilter = newJtxFilter;
        statusFilter = newStatusFilter;
        tsfFilter = newTsfFilter;

        // initialize the array if not yet done so
        if (activeFilters == null) {
            activeFilters = new TestFilter[NUM_FILTERS];
        }

        activeFilters[KEY_FILTER] = keyFilter;
        activeFilters[URL_FILTER] = urlFilter;
        activeFilters[JTX_FILTER] = jtxFilter;
        activeFilters[STATUS_FILTER] = statusFilter;
        activeFilters[TSS_FILTER] = tsfFilter;

        activeSettings = nowSettings;
        updateExcludeInfo();

        // notify observers
        notifyUpdated(this);
        return null;
    }

    private void updateStatusFilter() {
        if (!statusFilterNeedsUpdate)
            return;

        TestResultTable trt = execModel.getActiveTestResultTable();
        if (trt != null) {
            activeFilters[STATUS_FILTER] = new StatusFilter(
                    activeSettings.statusFields,
                    trt);
            statusFilterNeedsUpdate = false;
        }
    }

    /**
     * Attach observer to the given interview.
     * Automatically detaches from other interviews being observed.
     * @param ip The interview to observe.  For convenience, if this param
     *        is null, any current observers will be detached.
     */
    private void updateInterviewObserver(InterviewParameters ip) {
        if (ip == null && intObs != null) {
            intObs.getInterview().removeObserver(intObs);
        } else if (intObs == null || intObs.getInterview() != ip) {
            if (intObs != null) {
                intObs.getInterview().removeObserver(intObs);
            }

            if (ip != null) {
                intObs = new InterviewObserver(ip);
                ip.addObserver(intObs);
            }
        }
    }

    private void updateExcludeInfo() {
        // check to see if GUI is init-ed
        if (jtxMode == null) {
            return;
        }

        InterviewParameters ip = execModel.getInterviewParameters();

        if (ip == null) // nothing to do
        {
            return;
        }

        boolean isUnknown = true;
        ExcludeListParameters elp = ip.getExcludeListParameters();

        if (ip != null) {
            elp = ip.getExcludeListParameters();
            if (elp instanceof MutableExcludeListParameters) {
                MutableExcludeListParameters melp = (MutableExcludeListParameters) elp;
                int mode = melp.getExcludeMode();
                switch (mode) {
                    case MutableExcludeListParameters.NO_EXCLUDE_LIST:
                        jtxMode.setText(uif.getI18NString("basicTf.exclude.mode.none"));
                        setExcludeFiles(null);
                        isUnknown = false;
                        break;
                    case MutableExcludeListParameters.INITIAL_EXCLUDE_LIST:
                        jtxMode.setText(uif.getI18NString("basicTf.exclude.mode.initial"));
                        setExcludeFiles(melp.getExcludeFiles());
                        isUnknown = false;
                        break;
                    case MutableExcludeListParameters.LATEST_EXCLUDE_LIST:
                        jtxMode.setText(uif.getI18NString("basicTf.exclude.mode.latest"));
                        setExcludeFiles(null);
                        isUnknown = false;
                        break;
                    case MutableExcludeListParameters.CUSTOM_EXCLUDE_LIST:
                        jtxMode.setText(uif.getI18NString("basicTf.exclude.mode.custom"));
                        setExcludeFiles(melp.getCustomExcludeFiles());
                        isUnknown = false;
                        break;
                }   // switch

            }
        }

        // done here to avoid duplicating code above
        if (isUnknown) {
            jtxMode.setText(uif.getI18NString("basicTf.exclude.mode.unknown"));
            jtxFiles.removeAllElements();
        }
    }

    /**
     * Adds exclude list files to the list.
     * If null or zero length, the list is cleared.
     * @param files The files to add.  Null ok.
     */
    private void setExcludeFiles(File[] files) {
        jtxFiles.removeAllElements();

        if (files == null || files.length == 0) {
            return;
        } else {
            for (int i = 0; i < files.length; i++) {
                jtxFiles.addElement(files[i].getPath());
            }
        }
    }

    private JComponent[] createTabPanels() {
        JComponent[] items = {createTestsPanel(), createKeywordPanel(),
            createStatusPanel(), createExcludePanel(),
            createSpecialPanel()};

        return items;
    }

    // KEYWORD PANEL
    private JComponent createKeywordPanel() {
        JPanel p = uif.createPanel(
                "basicTf.keywords.mainPanel", new GridBagLayout(), false);
        p.setName("keywords");

        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        keyBtnGrp = new ButtonGroup();
        keyAllBtn = uif.createRadioButton("basicTf.keywords.all", keyBtnGrp);
        keyAllBtn.setMnemonic(uif.getI18NString("basicTf.keywords.all.mne").charAt(0));
        p.add(keyAllBtn, c);

        keyMatchBtn = uif.createRadioButton("basicTf.keywords.match", keyBtnGrp);
        keyMatchBtn.setMnemonic(uif.getI18NString("basicTf.keywords.match.mne").charAt(0));
        keyMatchBtn.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                enableKeywordFields();
            }
        });
        c.weightx = 0;
        c.gridwidth = 1;
        p.add(keyMatchBtn, c);

        String[] kc = {ANY_OF, ALL_OF, EXPR};
        keywordsChoice = uif.createChoice("basicTf.keywords.choice", kc);
        p.add(keywordsChoice, c);

        keywordsField = uif.createInputField("basicTf.keywords.field", 20);
        keywordsField.setEditable(true);
        uif.setAccessibleInfo(keywordsField, "basicTf.keywords.field");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        p.add(keywordsField, c);

        keyAllBtn.setSelected(true);
        enableKeywordFields();
        return p;
    }

    private void enableKeywordFields() {
        boolean b = keyMatchBtn.isSelected();
        keywordsChoice.setEnabled(b);
        keywordsField.setEnabled(b);
    }

    // STATUS PANEL
    private JComponent createStatusPanel() {
        JPanel p = uif.createPanel(
                "basicTf.status.mainPanel", new GridBagLayout(), false);
        p.setName("status");

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;

        statusBtnGrp = new ButtonGroup();
        statusAllBtn = uif.createRadioButton("basicTf.status.all", statusBtnGrp);
        statusAllBtn.setMnemonic(uif.getI18NString("basicTf.status.all.mne").charAt(0));
        p.add(statusAllBtn, c);

        statusAnyOfBtn = uif.createRadioButton("basicTf.status.anyOf", statusBtnGrp);
        statusAnyOfBtn.setMnemonic(uif.getI18NString("basicTf.status.anyOf.mne").charAt(0));
        statusAnyOfBtn.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                enableStatusFields();
            }
        });
        c.gridwidth = 1;
        c.weightx = 0;
        p.add(statusAnyOfBtn, c);

        JPanel row = new JPanel(new GridBagLayout());
        row.setBorder(BorderFactory.createEtchedBorder());
        GridBagConstraints rc = new GridBagConstraints();
        rc.insets.left = 10;

        statusChecks[Status.PASSED] =
                uif.createCheckBox("basicTf.status.prev.passed", false);
        row.add(statusChecks[Status.PASSED], rc);

        statusChecks[Status.FAILED] =
                uif.createCheckBox("basicTf.status.prev.failed", true);
        row.add(statusChecks[Status.FAILED], rc);

        statusChecks[Status.ERROR] =
                uif.createCheckBox("basicTf.status.prev.error", true);
        row.add(statusChecks[Status.ERROR], rc);

        rc.insets.right = 10;
        statusChecks[Status.NOT_RUN] =
                uif.createCheckBox("basicTf.status.prev.notRun", true);
        row.add(statusChecks[Status.NOT_RUN], rc);
        uif.setToolTip(row, "basicTf.status.prev");

        statusAllBtn.setSelected(true);
        enableStatusFields();
        p.add(row, c);

        return p;
    }

    private void enableStatusFields() {
        boolean enable = statusAnyOfBtn.isEnabled() && statusAnyOfBtn.isSelected();
        for (int i = 0; i < statusChecks.length; i++) {
            statusChecks[i].setEnabled(enable);
        }
    }

    // INIT URLS
    private JComponent createTestsPanel() {
        final JPanel p = uif.createPanel(
                "basicTf.tests.mainPanel", new BorderLayout(), false);
        p.setName("tests");

        // more configure ...
        lastTrt = execModel.getActiveTestResultTable();
        testsField = new TestTreeSelectionPane(lastTrt);

        p.add(testsField, BorderLayout.CENTER);

        p.addComponentListener(new ComponentAdapter() {

            public void componentShown(ComponentEvent e) {
                TestResultTable nowTrt = execModel.getActiveTestResultTable();
                // replaces widget with an updated one
                // only really important if the test suite contents (structure)
                // changes
                // this code basically keeps it in sync with the main tree,
                // otherwise it might be left behind watching a temporary TRT
                if (nowTrt != lastTrt) {
                    TestTreeSelectionPane newTree = new TestTreeSelectionPane(nowTrt);
                    String[] paths = testsField.getSelection();     // save
                    p.remove(testsField);
                    testsField = newTree;
                    testsField.setSelection(paths);                 // restore
                    p.add(testsField, BorderLayout.CENTER);
                    lastTrt = nowTrt;
                }
            }
        });

        return p;
    }

    // JTX lists
    private JComponent createExcludePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setName("exclude");

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.ipadx = 10;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;

        // column 1
        c.gridwidth = 3;
        jtxCheckBox = uif.createCheckBox("basicTf.exclude", false);
        jtxCheckBox.setMnemonic(uif.getI18NString("basicTf.exclude.mne").charAt(0));
        p.add(jtxCheckBox, c);

        // create some margins
        c.gridy = 1;
        p.add(Box.createVerticalStrut(5), c);

        c.gridy = 2;
        c.gridwidth = 1;

        c.gridheight = 2;
        p.add(Box.createHorizontalStrut(8), c);

        c.gridheight = 1;
        c.gridx = 2;

        // labels
        final JLabel modeLab = uif.createLabel("basicTf.exclude.mode");
        modeLab.setDisplayedMnemonic(uif.getI18NString("basicTf.exclude.mode.mne").charAt(0));
        modeLab.setEnabled(jtxCheckBox.isSelected());
        p.add(modeLab, c);

        c.gridy = 3;
        final JLabel fileLab = uif.createLabel("basicTf.exclude.file");
        fileLab.setDisplayedMnemonic(uif.getI18NString("basicTf.exclude.file.mne").charAt(0));
        fileLab.setEnabled(jtxCheckBox.isSelected());
        p.add(fileLab, c);

        // column 2
        c.gridy = 2;
        c.gridx = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        jtxMode = uif.createOutputField("basicTf.exclude.mode", modeLab);
        jtxMode.setBorder(BorderFactory.createEmptyBorder());
        jtxMode.setEditable(false);
        jtxMode.setEnabled(jtxCheckBox.isSelected());
        uif.setAccessibleInfo(jtxMode, "basicTf.exclude.mode");
        p.add(jtxMode, c);

        c.gridy = 3;
        c.weightx = 2;
        c.weighty = 2;
        c.fill = GridBagConstraints.BOTH;
        jtxFiles = new DefaultListModel();
        jtxFileList = uif.createList("basicTf.exclude.file", jtxFiles);
        jtxFileList.setEnabled(jtxCheckBox.isSelected());
        uif.setAccessibleInfo(jtxFileList, "basicTf.exclude.file");
        fileLab.setLabelFor(jtxFileList);

        // might need to add a scroll panel here
        p.add(uif.createScrollPane(jtxFileList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), c);

        jtxCheckBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jtxFileList.setEnabled(jtxCheckBox.isSelected());
                jtxMode.setEnabled(jtxCheckBox.isSelected());
                modeLab.setEnabled(jtxCheckBox.isSelected());
                fileLab.setEnabled(jtxCheckBox.isSelected());
            }
        });

        return p;
    }

    // test suite specific filter
    private JComponent createSpecialPanel() {
        String k = "basicTf.tsf";
        JPanel p = uif.createPanel("basicTf.tsf", new BorderLayout(), false);
        p.setName("special");

        tsfCheckBox = uif.createCheckBox("basicTf.tsf", false);
        tsfCheckBox.setMnemonic(uif.getI18NString("basicTf.tsf.mne").charAt(0));
        p.add(tsfCheckBox, BorderLayout.CENTER);

        return p;
    }

    /**
     * Capture the settings in the current GUI.
     * @return A snapshot of the settings currently entred into the GUI.
     * @throws IllegalStateException If any part of the GUI has a state which
     *         does not produce a consistent setting.  The message of this
     *         exception will contain an internationalized message to help the
     *         user resolve the problem.
     */
    private SettingsSnapshot grabSettings() {
        SettingsSnapshot shot = new SettingsSnapshot();

        // grad top level togggle state of each type
        shot.keywordsEnabled = !keyAllBtn.isSelected();
        //shot.urlsEnabled = !allTestsBtn.isSelected();
        shot.urlsEnabled = true;        // always enabled in current impl.
        shot.statusEnabled = statusAnyOfBtn.isSelected();
        shot.jtxEnabled = jtxCheckBox.isSelected();
        shot.tsfEnabled = tsfCheckBox.isSelected();

        // grab status checkbox state
        boolean oneSelected = false;
        shot.statusFields = new boolean[statusChecks.length];
        for (int i = 0; i < statusChecks.length; i++) {
            shot.statusFields[i] = statusChecks[i].isSelected();
            oneSelected = oneSelected || shot.statusFields[i];
        }

        if (shot.statusEnabled && !oneSelected) {
            throw new IllegalStateException(uif.getI18NString("basicTf.badStatus"));
        }

        // init. urls
        shot.initialUrls = testsField.getSelection();

        // special case post processing for root selection
        // if length is one and that string is zero length, that indicates
        // a root selection
        if (shot.initialUrls != null &&
                shot.initialUrls.length == 1 &&
                shot.initialUrls[0].length() == 0) {
            shot.initialUrls = null;
        }

        // keywords
        shot.keyChoice = (String) (keywordsChoice.getSelectedItem());
        shot.keyString = keywordsField.getText();

        return shot;
    }

    /**
     * Put a set of settings into the GUI.
     */
    private void putSettings(SettingsSnapshot s) {
        // no GUI, don't do anything
        if (editorPane == null) {
            return;
        }

        keyAllBtn.setSelected(!s.keywordsEnabled);
        keyMatchBtn.setSelected(s.keywordsEnabled);

        statusAllBtn.setSelected(!s.statusEnabled);
        statusAnyOfBtn.setSelected(s.statusEnabled);

        jtxCheckBox.setSelected(s.jtxEnabled);
        tsfCheckBox.setSelected(s.tsfEnabled);

        for (int i = 0; i < statusChecks.length; i++) {
            statusChecks[i].setSelected(s.statusFields[i]);
        }

        testsField.setSelection(s.initialUrls);

        keywordsChoice.setSelectedItem(s.keyChoice);
        keywordsField.setText(s.keyString);

        updateExcludeInfo();
    }

    // Utility methods
    private static String kwModeToType(String mode) {
        if (mode == ALL_OF) {
            return "all of";
        } else if (mode == ANY_OF) {
            return "any of";
        } else {
            return EXPR;
        }
    }

    private Observer[] obs = new Observer[0];
    private static int instanceCount;
    private TestResultTable lastTrt;
    private InterviewObserver intObs;

    private static int NUM_FILTERS = 5;
    private static int KEY_FILTER = 0;
    private static int URL_FILTER = 1;
    private static int JTX_FILTER = 2;
    private static int STATUS_FILTER = 3;
    private static int TSS_FILTER = 4;

    private SettingsSnapshot activeSettings;
    private TestFilter[] activeFilters;     // for convenience
    private KeywordsFilter keyFilter;
    private InitialUrlFilter urlFilter;
    private TestFilter jtxFilter;
    private StatusFilter statusFilter;
    private TestFilter tsfFilter;           // test suite filter
    private final UIFactory uif;
    private JTabbedPane editorPane;

    private boolean statusFilterNeedsUpdate;

    // keyword info
    private ButtonGroup keyBtnGrp;
    private JRadioButton keyAllBtn;
    private JRadioButton keyMatchBtn;
    private JComboBox keywordsChoice;
    private JTextField keywordsField;
    private static final String ALL_OF = "allOf";
    private static final String ANY_OF = "anyOf";
    private static final String EXPR = "expr";

    // status info
    private ButtonGroup statusBtnGrp;
    private JRadioButton statusAllBtn;
    private JRadioButton statusAnyOfBtn;
    private JCheckBox[] statusChecks = new JCheckBox[Status.NUM_STATES];

    // tests info
    private ButtonGroup testsBtnGrp;
    //private JRadioButton allTestsBtn;
    //private JRadioButton selectTestsBtn;
    private TestTreeSelectionPane testsField;

    // checkboxes to enable exclude list from interview and test suite filter
    private JCheckBox jtxCheckBox;
    private JCheckBox tsfCheckBox;

    // jtx info fields
    private JTextField jtxMode;
    private JList jtxFileList;
    private DefaultListModel jtxFiles;
    private static String NAME,  REASON,  DESCRIPTION;

    /**
     * Necessary to track changes which occur in the active interview.
     */
    private class InterviewObserver implements Interview.Observer {

        InterviewObserver(InterviewParameters i) {
            interview = i;
        }

        InterviewParameters getInterview() {
            return interview;
        }

        public void currentQuestionChanged(Question q) {
            // ignore
        }

        public void pathUpdated() {
            TestFilter underTest;
            boolean needsUpdate = false;
            updateExcludeInfo();

            // determine if jtx and tsf filters were updated
            if (activeSettings.jtxEnabled) {
                // we only support copying exclude list from interview
                // right now
                InterviewParameters ip = execModel.getInterviewParameters();

                if (ip != null) {
                    // may set var. to null, but that's okay
                    underTest = ip.getExcludeListFilter();
                    if (underTest != null &&
                            (jtxFilter == null || !underTest.equals(jtxFilter))) {
                        needsUpdate = true;
                    } else if (jtxFilter != null) // jtx filter now null
                    {
                        needsUpdate = true;
                    }
                }
            }

            if (needsUpdate) {
                activateSettings(activeSettings);
                return;     // rest of checking no longer needed
            }

            if (activeSettings.tsfEnabled) {
                // we only support copying exclude list from interview
                // right now
                InterviewParameters ip = execModel.getInterviewParameters();
                TestSuite ts = execModel.getTestSuite();

                if (ip != null && ts != null) {
                    underTest = ts.createTestFilter(ip.getEnv());
                    if (underTest != null) {
                        if (tsfFilter == null || !underTest.equals(tsfFilter)) {
                            needsUpdate = true;
                        } else {
                        }
                    } else if (tsfFilter != null) // tsf filter now null
                    {
                        needsUpdate = true;
                    } else {
                    }
                } else {
                }
            }

            if (needsUpdate) {
                activateSettings(activeSettings);
                return;     // rest of checking no longer needed
            }
        }
        private InterviewParameters interview;
    }

    private class SettingsSnapshot {

        SettingsSnapshot() {
            statusFields = new boolean[Status.NUM_STATES];
            urlsEnabled = true;
            keyChoice = EXPR;
            keyString = "";
        }

        SettingsSnapshot(Map m) {
            this();
            load(m);
        }

        public boolean equals(Object settings) {

            if (settings == null) {
                return false;
            }

            SettingsSnapshot incoming = (SettingsSnapshot) settings;

            if (!Arrays.equals(initialUrls, incoming.initialUrls)) {
                return false;
            }

            if (keywordsEnabled != incoming.keywordsEnabled) {
                return false;
            } else {
                if (keyChoice != incoming.keyChoice) {
                    return false;
                }

                // destination of structure is to handle null values
                if (keyString == null) {
                    if (keyString != incoming.keyString) {
                        return false;
                    } else {
                    }
                } else {
                    if (!keyString.equals(incoming.keyString)) {
                        return false;
                    } else {
                    }
                }
            }   // outer else

            if (statusEnabled != incoming.statusEnabled) {
                return false;
            } else if (!Arrays.equals(statusFields, incoming.statusFields)) {
                return false;
            }

            if (jtxEnabled != incoming.jtxEnabled) {
                return false;
            }

            if (tsfEnabled != incoming.tsfEnabled) {
                return false;
            }

            return true;
        }   // equals()

        void save(Map map) {
            map.put(MAP_URL_ENABLE, booleanToInt(urlsEnabled));
            map.put(MAP_KEY_ENABLE, booleanToInt(keywordsEnabled));
            map.put(MAP_STATUS_ENABLE, booleanToInt(statusEnabled));
            map.put(MAP_JTX_ENABLE, booleanToInt(jtxEnabled));
            map.put(MAP_TSF_ENABLE, booleanToInt(tsfEnabled));

            for (int i = 0; i < statusFields.length; i++) {
                map.put(MAP_STATUS_PREFIX + i, booleanToInt(statusFields[i]));
            }

            map.put(MAP_URLS, StringArray.join(initialUrls));

            map.put(MAP_KEY_CHOICE, keyChoice);
            map.put(MAP_KEY_STRING, keyString);
        }

        void load(Map map) {
            urlsEnabled = intToBoolean((String) (map.get(MAP_URL_ENABLE)));
            keywordsEnabled = intToBoolean((String) (map.get(MAP_KEY_ENABLE)));
            statusEnabled = intToBoolean((String) (map.get(MAP_STATUS_ENABLE)));
            jtxEnabled = intToBoolean((String) (map.get(MAP_JTX_ENABLE)));
            tsfEnabled = intToBoolean((String) (map.get(MAP_TSF_ENABLE)));

            for (int i = 0; i < Status.NUM_STATES; i++) {
                statusFields[i] = intToBoolean((String) (map.get(MAP_STATUS_PREFIX + i)));
            }   // for

            initialUrls = StringArray.split((String) (map.get(MAP_URLS)));

            keyChoice = (String) (map.get(MAP_KEY_CHOICE));
            keyString = (String) (map.get(MAP_KEY_STRING));

            validate();
        }

        private void validate() {
            // conserve strings, but also sync with the actual
            // objects in the choice list for easy use
            if (keyChoice.equals(ALL_OF)) {
                keyChoice = ALL_OF;
            } else if (keyChoice.equals(ANY_OF)) {
                keyChoice = ANY_OF;
            } else if (keyChoice.equals(EXPR)) {
                keyChoice = EXPR;
            } else {
                keyChoice = ALL_OF;     // unknown, use default
            }
        }

        String booleanToInt(boolean val) {
            if (val) {
                return "1";
            } else {
                return "0";
            }
        }

        boolean intToBoolean(String num) {
            if (num == null) {
                return false;
            }

            if (num.equals("1")) {
                return true;
            } else {
                return false;
            }
        }
        boolean urlsEnabled;
        boolean keywordsEnabled;
        boolean statusEnabled;
        boolean jtxEnabled;
        boolean tsfEnabled;
        boolean[] statusFields;
        String[] initialUrls;
        String keyChoice;
        String keyString;
        private static final String MAP_URL_ENABLE = "urlsEnabled";
        private static final String MAP_KEY_ENABLE = "keyEnabled";
        private static final String MAP_STATUS_ENABLE = "statusEnable";
        private static final String MAP_JTX_ENABLE = "jtxEnable";
        private static final String MAP_TSF_ENABLE = "tsfEnable";
        private static final String MAP_URLS = "urls";
        private static final String MAP_KEY_CHOICE = "keyChoice";
        private static final String MAP_KEY_STRING = "keyString";
        private static final String MAP_STATUS_PREFIX = "status";
    }
}
