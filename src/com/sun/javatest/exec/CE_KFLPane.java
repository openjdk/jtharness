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
package com.sun.javatest.exec;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import javax.help.CSH;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.sun.javatest.InterviewParameters;
import com.sun.javatest.tool.EditableFileList;
import com.sun.javatest.tool.FileChooser;
import com.sun.javatest.tool.UIFactory;

/**
 * Standard values view, known failures list (KFL) panel.
 */
class CE_KFLPane extends CE_StdPane {

    CE_KFLPane(UIFactory uif, InterviewParameters config) {
        super(uif, config, "kfl");

        updateConfig();
        initGUI();
    }

    boolean isOKToClose() {
        if (kflFiles == null) {
            return true;
        }

        if (noneBtn.isSelected()) {
            return true;
        } else if (customBtn.isSelected()) {
            return customPanel.isOKToClose();
        } else {
            throw new Error();
        }
    }

    void updateConfig() {
        if (config == null) {
            return;
        } else {
            // because pane is usually disabled on init
            setEnabled(true);
        }

        kflFiles = config.getKnownFailureFiles();
    }

    void load() {
        updateConfig();

        // just in case, null means there are no files!
        if (kflFiles != null && kflFiles.length == 0) {
            kflFiles = null;
        }

        if (kflFiles != null) {
            customBtn.setSelected(true);
            //customBtn.setEnabled(false);
            noneBtn.setSelected(false);
            //noneBtn.setEnabled(true);

            customPanel.load();
        } else {
            //noneBtn.setEnabled(false);
            noneBtn.setSelected(true);
            customBtn.setSelected(false);
            //customBtn.setEnabled(true);
            customPanel.clear();
            customPanel.setEnabled(false);
        }
    }

    void save() {
        if (customBtn.isSelected()) {
            customPanel.save();
        } else {
            config.setKnownFailureFiles(null);
        }
    }

    private void initGUI() {
        CSH.setHelpIDString(this, "confEdit.kflTab.csh");

        JPanel p = uif.createPanel("ce.kfl", new BorderLayout(), false);
        initToolBar();
        p.add(toolBar, BorderLayout.NORTH);

        customPanel = new CustomPanel();
        p.add(customPanel, BorderLayout.CENTER);
        addBody(p);

        if (config == null) {
            setEnabled(false);
        }
    }

    private void initToolBar() {
        toolBar = new JToolBar(JToolBar.HORIZONTAL);
        toolBar.setFloatable(false);

        btnGrp = new ButtonGroup();
        noneBtn = uif.createRadioButton("ce.kfl.none", btnGrp);
        noneBtn.addChangeListener(listener);
        toolBar.add(noneBtn);

        customBtn = uif.createRadioButton("ce.kfl.custom", btnGrp);
        customBtn.addChangeListener(listener);
        toolBar.add(customBtn);
    }
    private File[] kflFiles;
    private JToolBar toolBar;
    private ButtonGroup btnGrp;
    private JRadioButton customBtn;
    private JRadioButton noneBtn;
    private CustomPanel customPanel;
    private Listener listener = new Listener();

    private class Listener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (noneBtn.isSelected()) {
                customPanel.setEnabled(false);
            } else {
                customPanel.setEnabled(true);
            }
        }
    }

    private class CustomPanel extends JPanel {

        CustomPanel() {
            super(new GridBagLayout());
            setName("custom");
            GridBagConstraints c = new GridBagConstraints();

            filesLabel = uif.createLabel("ce.kfl.custom.files", true);
            c.insets.top = 5;
            c.insets.bottom = 5;
            c.insets.left = 5;
            add(filesLabel, c);

            FileChooser chooser = new FileChooser(true);
            chooser.addChoosableExtension(".kfl",
                    uif.getI18NString("ce.kfl.kflFiles"));
            // more configure ...
            filesField = new EditableFileList(chooser);
            filesField.setDuplicatesAllowed(false);
            uif.setToolTip(filesField, "ce.kfl.custom.files");
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets.right = 5;
            c.weightx = 1;
            c.weighty = 1;
            filesLabel.setLabelFor(filesField);
            add(filesField, c);
        }

        @Override
        public void setEnabled(boolean b) {
            filesField.setEnabled(b);
            filesLabel.setEnabled(b);
        }

        void clear() {
            filesField.clear();
        }

        boolean isOKToClose() {
            File tsr = config.getTestSuite().getRoot();
            File[] files = filesField.getFiles();
            if (files.length == 0) {
                uif.showError("ce.kfl.custom.noFiles");
                return false;
            }
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                File absFile = (file.isAbsolute() ? file
                        : new File(tsr, file.getPath()));
                if (!absFile.exists()) {
                    uif.showError("ce.kfl.custom.cantFindFile", file);
                    return false;
                }
                if (!(absFile.isFile() && absFile.canRead())) {
                    uif.showError("ce.kfl.custom.badFile", file);
                    return false;
                }
            }
            return true;
        }

        void load() {
            filesField.setFiles(kflFiles);
        }

        void save() {
            File[] files = filesField.getFiles();
            if (files != null && files.length > 0) {
                config.setKnownFailureFiles(files);
            } else {
                config.setKnownFailureFiles(null);
            }
        }
        private EditableFileList filesField;
        private JLabel filesLabel;
    }
}
