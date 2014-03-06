/*
 * $Id$
 *
 * Copyright (c) 2002, 2009, Oracle and/or its affiliates. All rights reserved.
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
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import javax.help.CSH;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.sun.javatest.InterviewParameters;
import com.sun.javatest.Parameters.ExcludeListParameters;
import com.sun.javatest.Parameters.MutableExcludeListParameters;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.tool.EditableFileList;
import com.sun.javatest.tool.FileChooser;
import com.sun.javatest.tool.UIFactory;

/**
 * Standard values view, exclude list panel.
 */
class CE_ExcludeListPane extends CE_StdPane
{
    CE_ExcludeListPane(UIFactory uif, InterviewParameters config) {
        super(uif, config, "excl");

        updateConfig();
        initGUI();
    }

    boolean isOKToClose() {
        if (mutableExcludeListParameters == null)
            return true;

        if (noneBtn.isSelected())
            return true;
        else if (initialBtn.isSelected())
            return initialPanel.isOKToClose();
        else if (latestBtn.isSelected())
            return latestPanel.isOKToClose();
        else if (customBtn.isSelected())
            return customPanel.isOKToClose();
        else
            throw new Error();
    }

    void updateConfig() {
        if (config == null)
            return;

        excludeListParameters = config.getExcludeListParameters();
        if (excludeListParameters instanceof MutableExcludeListParameters)
            mutableExcludeListParameters = (MutableExcludeListParameters) excludeListParameters;
        else
            mutableExcludeListParameters = null;
    }

    void load() {
        updateConfig();

        if (mutableExcludeListParameters != null) {
            int mode = mutableExcludeListParameters.getExcludeMode();
            switch (mode) {
            case MutableExcludeListParameters.NO_EXCLUDE_LIST:
                noneBtn.setSelected(true);
                break;

            case MutableExcludeListParameters.INITIAL_EXCLUDE_LIST:
                initialBtn.setSelected(true);
                break;

            case MutableExcludeListParameters.LATEST_EXCLUDE_LIST:
                latestBtn.setSelected(true);
                break;

            case MutableExcludeListParameters.CUSTOM_EXCLUDE_LIST:
                customBtn.setSelected(true);
                break;
            }

            TestSuite testSuite = config.getTestSuite();
            WorkDirectory workDir = config.getWorkDirectory();

            noneBtn.setEnabled(true);

            File initialExcludeList = testSuite.getInitialExcludeList();
            initialBtn.setEnabled(initialExcludeList != null);
            initialPanel.load(initialExcludeList);

            URL latestExcludeListURL = testSuite.getLatestExcludeList();
            File latestExcludeListFile = workDir.getSystemFile("latest.jtx");
            latestBtn.setEnabled(latestExcludeListURL != null);
            latestPanel.load(latestExcludeListURL, latestExcludeListFile);

            customBtn.setEnabled(true);
            customPanel.load();
            customPanel.setEnabled(true);
        }
        else {
            noneBtn.setEnabled(false);
            initialBtn.setEnabled(false);
            latestBtn.setEnabled(false);

            customBtn.setSelected(true);
            customBtn.setEnabled(false);
            customPanel.clear();
            customPanel.setEnabled(false);
        }
    }

    void save() {
        if (mutableExcludeListParameters != null) {
            int mode;
            if (customBtn.isSelected())
                mode = MutableExcludeListParameters.CUSTOM_EXCLUDE_LIST;
            else if (initialBtn.isSelected())
                mode = MutableExcludeListParameters.INITIAL_EXCLUDE_LIST;
            else if (latestBtn.isSelected())
                mode = MutableExcludeListParameters.LATEST_EXCLUDE_LIST;
            else
                mode = MutableExcludeListParameters.NO_EXCLUDE_LIST;
            mutableExcludeListParameters.setExcludeMode(mode);

            // no values to save from blankPanel and initialPanel
            latestPanel.save();
            customPanel.save();
        }
    }

    void setCheckExcludeListListener(ActionListener l) {
        latestPanel.setCheckExcludeListListener(l);
    }

    private void initGUI() {
        CSH.setHelpIDString(this, "confEdit.excludeTab.csh");

        JPanel p = uif.createPanel("ce.excl", new BorderLayout(), false);

        initToolBar();
        p.add(toolBar, BorderLayout.WEST);

        body = uif.createPanel("ce.excl.body", new CardLayout(), false);
        body.setBorder(BorderFactory.createEtchedBorder());

        blankPanel = uif.createPanel("ce.excl.blank");
        blankPanel.setName("blank");
        body.add(blankPanel, blankPanel.getName());

        customPanel = new CustomPanel();
        body.add(customPanel, customPanel.getName());

        initialPanel = new InitialPanel();
        body.add(initialPanel, initialPanel.getName());

        latestPanel = new LatestPanel();
        body.add(latestPanel, latestPanel.getName());

        p.add(body, BorderLayout.CENTER);

        addBody(p);

        if (mutableExcludeListParameters == null)
            // can't accurately render info about the exclude list,
            // so mark the pane disabled
            setEnabled(false);
    }

    private void initToolBar() {
        toolBar = new JToolBar(JToolBar.VERTICAL);
        toolBar.setFloatable(false);

        btnGrp = new ButtonGroup();
        noneBtn = uif.createRadioButton("ce.excl.none", btnGrp);
        noneBtn.addChangeListener(listener);
        toolBar.add(noneBtn);

        initialBtn = uif.createRadioButton("ce.excl.initial", btnGrp);
        initialBtn.addChangeListener(listener);
        toolBar.add(initialBtn);

        latestBtn = uif.createRadioButton("ce.excl.latest", btnGrp);
        latestBtn.addChangeListener(listener);
        toolBar.add(latestBtn);

        customBtn = uif.createRadioButton("ce.excl.custom", btnGrp);
        customBtn.addChangeListener(listener);
        toolBar.add(customBtn);
    }

    ExcludeListParameters excludeListParameters;
    MutableExcludeListParameters mutableExcludeListParameters;

    private JToolBar toolBar;
    private ButtonGroup btnGrp;
    private JRadioButton customBtn;
    private JRadioButton initialBtn;
    private JRadioButton latestBtn;
    private JRadioButton noneBtn;

    private JPanel body;
    private JPanel blankPanel;
    private CustomPanel customPanel;
    private InitialPanel initialPanel;
    private LatestPanel latestPanel;

    private Listener listener = new Listener();

    private class Listener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            Component comp;
            if (customBtn.isSelected())
                comp = customPanel;
            else if (initialBtn.isSelected())
                comp = initialPanel;
            else if (latestBtn.isSelected())
                comp = latestPanel;
            else
                comp = blankPanel;

            ((CardLayout)(body.getLayout())).show(body, comp.getName());
        }
    }

    private class CustomPanel extends JPanel {
        CustomPanel() {
            super(new GridBagLayout());
            setName("custom");
            GridBagConstraints c = new GridBagConstraints();

            JLabel filesLabel = uif.createLabel("ce.excl.custom.files", true);
            c.insets.top = 5;
            c.insets.bottom = 5;
            c.insets.left = 5;
            add(filesLabel, c);

            FileChooser chooser = new FileChooser(true);
            chooser.addChoosableExtension(".jtx",
                                          uif.getI18NString("ce.excl.jtxfiles"));
            // more configure ...
            filesField = new EditableFileList(chooser);
            filesField.setDuplicatesAllowed(false);
            uif.setToolTip(filesField, "ce.excl.custom.files");
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets.right = 5;
            c.weightx = 1;
            c.weighty = 1;
            filesLabel.setLabelFor(filesField);
            add(filesField, c);
        }

        public void setEnabled(boolean b) {
            filesField.setEnabled(b);
        }

        void clear() {
            filesField.clear();
        }

        boolean isOKToClose() {
            File tsr = config.getTestSuite().getRoot();
            File[] files = filesField.getFiles();
            if (files.length == 0) {
                uif.showError("ce.excl.custom.noFiles");
                return false;
            }
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                File absFile = (file.isAbsolute() ? file
                                : new File(tsr, file.getPath()));
                if (!absFile.exists()) {
                    uif.showError("ce.excl.custom.cantFindFile", file);
                    return false;
                }
                if (!(absFile.isFile() && absFile.canRead())) {
                    uif.showError("ce.excl.custom.badFile", file);
                    return false;
                }
            }
            return true;
        }

        void load() {
            File[] files = mutableExcludeListParameters.getCustomExcludeFiles();
            filesField.setFiles(files);
        }

        void save() {
            File[] files = filesField.getFiles();
            mutableExcludeListParameters.setCustomExcludeFiles(files);
        }

        private EditableFileList filesField;
    }

    private class InitialPanel extends JPanel {
        InitialPanel() {
            super(new GridBagLayout());
            setName("initial");
            GridBagConstraints c = new GridBagConstraints();

            JLabel fileLabel = uif.createLabel("ce.excl.initial.file", true);
            c.anchor = GridBagConstraints.NORTH;
            c.insets.top = 20;
            c.insets.bottom = 5;
            c.insets.left = 5;
            c.insets.right = 5;
            c.weighty = 1;
            add(fileLabel, c);

            fileArea = uif.createTextArea("ce.excl.initial.file");
            fileArea.setMinimumSize(new Dimension(100, 10));
            fileArea.setEditable(false);
            fileArea.setOpaque(false);
            fileArea.setLineWrap(true);
            fileArea.setBorder(null);
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1;
            fileLabel.setLabelFor(fileArea);
            add(fileArea, c);
        }

        boolean isOKToClose() {
            if (initialFile == null) {
                uif.showError("ce.excl.initial.noFile");
                return false;
            }
            else if (!initialFile.exists()) {
                uif.showError("ce.excl.initial.cantFindFile", initialFile);
                return false;
            }
            else if (!(initialFile.isFile() && initialFile.canRead())) {
                uif.showError("ce.excl.initial.cantReadFile", initialFile);
                return false;
            }
            else
                return true;
        }

        void load(File initialFile) {
            this.initialFile = initialFile;
            if (initialFile == null)
                fileArea.setText("");
            else
                fileArea.setText(initialFile.getPath());
        }

        private JTextArea fileArea;
        private File initialFile;
    }

    private class LatestPanel extends JPanel
        implements ActionListener, ChangeListener
    {
        LatestPanel() {
            super(new BorderLayout());
            setName("latest");

            // ----- head -----
            JPanel head = uif.createPanel("ce.excl.latest.head",
                                new GridBagLayout(),
                                false);
            head.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            GridBagConstraints lc = new GridBagConstraints();
            lc.anchor = GridBagConstraints.NORTHWEST;
            lc.insets.right = 5;

            GridBagConstraints fc = new GridBagConstraints();
            fc.fill = GridBagConstraints.HORIZONTAL;
            fc.weightx = 1;
            fc.gridwidth = GridBagConstraints.REMAINDER;

            JLabel urlLabel = uif.createLabel("ce.excl.latest.url", true);
            head.add(urlLabel, lc);

            urlField = uif.createTextArea("ce.excl.latest.url");
            urlField.setEditable(false);
            urlField.setLineWrap(true);
            urlField.setOpaque(false);
            urlField.setBorder(null);
            urlField.setMinimumSize(new Dimension(100, 10));
            urlField.setSize(new Dimension(2 * uif.getDotsPerInch(), Integer.MAX_VALUE));
            urlLabel.setLabelFor(urlField);
            head.add(urlField, fc);

            JLabel dateLabel = uif.createLabel("ce.excl.latest.date", true);
            head.add(dateLabel, lc);

            dateField = uif.createOutputField("ce.excl.latest.date", dateLabel);
            dateField.setBorder(null);
            dateLabel.setLabelFor(dateField);
            head.add(dateField, fc);
            add(head, BorderLayout.NORTH);

            // ----- body -----
            JPanel body = uif.createPanel("ce.excl.latest.body",
                                        new GridBagLayout(),
                                        false);
            body.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

            GridBagConstraints c = new GridBagConstraints();

            autoCheck = uif.createCheckBox("ce.excl.latest.auto");
            autoCheck.addChangeListener(this);
            c.weightx = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.WEST;
            body.add(autoCheck, c);

            btnGrp = new ButtonGroup();

            c = new GridBagConstraints();
            c.insets.left = 17;
            daysButton = uif.createRadioButton("ce.excl.latest.everyXDays", btnGrp);
            daysButton.addChangeListener(this);
            body.add(daysButton, c);

            daysField = uif.createInputField("ce.excl.latest.days", 3);
            daysField.setText("7");
            c.insets.left = 0;
            c.insets.right = 5;
            body.add(daysField, c);

            daysLabel = uif.createHeading("ce.excl.latest.days");
            daysLabel.setFont(daysButton.getFont());
            daysLabel.setForeground(daysButton.getForeground());
            body.add(daysLabel, c);

            runButton = uif.createRadioButton("ce.excl.latest.everyRun", btnGrp);
            runButton.addChangeListener(this);
            c.insets.left = 20;
            c.anchor = GridBagConstraints.WEST;
            c.weightx = 1;
            body.add(runButton, c);

            add(body, BorderLayout.CENTER);

            // ----- foot -----
            JPanel foot = uif.createPanel("ce.excl.latest.foot",
                                        new GridBagLayout(),
                                        false);
            foot.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            c = new GridBagConstraints();
            c.anchor = GridBagConstraints.EAST;
            c.weightx = 1;
            nowButton = uif.createButton("ce.excl.latest.now", this);
            foot.add(nowButton, c);

            add(foot, BorderLayout.SOUTH);
        }

        boolean isOKToClose() {
            if (autoCheck.isSelected() && daysButton.isSelected()) {
                String dt = daysField.getText();
                if (dt == null || dt.length() ==0) {
                    uif.showError("ce.excl.latest.noDays");
                    return false;
                }
                try {
                    int d = Integer.parseInt(dt);
                    if (d <= 0) {
                        uif.showError("ce.excl.latest.badDays");
                        return false;
                    }
                }
                catch (NumberFormatException e) {
                    uif.showError("ce.excl.latest.badDays");
                    return false;
                }
            }

            File l = config.getWorkDirectory().getSystemFile("latest.jtx");
            if (!l.exists() && !autoCheck.isSelected()) {
                uif.showError("ce.excl.latest.noFile");
                return false;
            }

            return true;
        }

        void load(URL latestURL, File latestFile) {
            if (latestURL == null)
                urlField.setText("");
            else
                urlField.setText(latestURL.toString());

            this.latestFile = latestFile;
            updateDateField();

            boolean ac = mutableExcludeListParameters.isLatestExcludeAutoCheckEnabled();
            autoCheck.setSelected(ac);

            int acm = mutableExcludeListParameters.getLatestExcludeAutoCheckMode();
            if (acm == MutableExcludeListParameters.CHECK_EVERY_X_DAYS)
                daysButton.setSelected(true);
            else
                runButton.setSelected(true);

            int days = mutableExcludeListParameters.getLatestExcludeAutoCheckInterval();
            if (days < 0)
                daysField.setText("");
            else
                daysField.setText(String.valueOf(days));
        }

        void save() {
            boolean ac = autoCheck.isSelected();
            mutableExcludeListParameters.setLatestExcludeAutoCheckEnabled(ac);

            int acm = (daysButton.isSelected()
                       ? MutableExcludeListParameters.CHECK_EVERY_X_DAYS
                       : MutableExcludeListParameters.CHECK_EVERY_RUN);
            mutableExcludeListParameters.setLatestExcludeAutoCheckMode(acm);

            try {
                String ds = daysField.getText();
                if (ds.length() > 0) {
                    int days = Integer.parseInt(ds);
                    mutableExcludeListParameters.setLatestExcludeAutoCheckInterval(days);
                }
            }
            catch (NumberFormatException e) {
                mutableExcludeListParameters.setLatestExcludeAutoCheckInterval(Integer.MIN_VALUE);
            }
        }

        void setCheckExcludeListListener(ActionListener l) {
            checkExcludeListListener = l;
        }

        public void actionPerformed(ActionEvent e) {
            if (checkExcludeListListener != null) {
                checkExcludeListListener.actionPerformed(e);
                updateDateField();
            }
        }


        public void stateChanged(ChangeEvent e) {
            boolean ac = autoCheck.isSelected();
            daysButton.setEnabled(ac);
            daysField.setEnabled(ac && daysButton.isSelected());
            daysLabel.setEnabled(ac);
            runButton.setEnabled(ac);
        }

        private void updateDateField() {
            long latestLastModified = latestFile.lastModified();
            if (latestLastModified <= 0)
                dateField.setText(uif.getI18NString("ce.excl.latest.dateNotAvailable"));
            else {
                DateFormat df = DateFormat.getDateTimeInstance();
                dateField.setText(df.format(new Date(latestLastModified)));
            }
        }

        private JTextArea urlField;
        private JTextField dateField;
        private JCheckBox autoCheck;
        private ButtonGroup btnGrp;
        private JRadioButton daysButton;
        private JTextField daysField;
        private JTextField daysLabel;
        private JRadioButton runButton;
        private JButton nowButton;

        private File latestFile;
        private ActionListener checkExcludeListListener;
    }
}
