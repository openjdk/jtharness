/*
 * $Id$
 *
 * Copyright (c) 2004, 2010, Oracle and/or its affiliates. All rights reserved.
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.sun.interview.Interview;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.TestSuite;
import com.sun.javatest.tool.FileChooser;
import com.sun.javatest.tool.ToolDialog;
import com.sun.javatest.tool.UIFactory;

class CE_TemplateDialog extends ToolDialog
{
    CE_TemplateDialog(JComponent parent, InterviewParameters config, ExecModel model, UIFactory uif) {
        super(parent, uif, "ct");

        this.config = config;
        this.model = model;

        fileChooser = new FileChooser(true);
        fileChooser.addChoosableExtension(".jti",
                                          uif.getI18NString("ct.jtiFiles"));
    }

    public void setVisible(boolean on) {
        if (on) {
            if (markersCheckBox == null)
                initGUI();

            markersCheckBox.setSelected(config.getMarkersEnabled());
            filterCheckBox.setEnabled(markersCheckBox.isSelected());
            filterCheckBox.setSelected(config.getMarkersFilterEnabled());
        }

        super.setVisible(on);
    }

    protected void initGUI() {
        setI18NTitle("ct.title");
        setHelp("confEdit.templateDialog.csh");

        listener = new Listener();

        JPanel body = uif.createPanel("ct.body", false);
        body.setLayout(new GridBagLayout());
        body.setBorder(BorderFactory.createEmptyBorder(10, 10, 30, 10));

        GridBagConstraints c = new GridBagConstraints();

        JLabel fileLbl = uif.createLabel("ct.file");
        c.insets.right = 10;
        body.add(fileLbl, c);

        JPanel p = uif.createPanel("ct.file", false);
        p.setLayout(new BorderLayout());

        fileField = uif.createInputField("ct.file", 32);
        fileField.getDocument().addDocumentListener(listener);
        fileLbl.setLabelFor(fileField);
        p.add(fileField, BorderLayout.CENTER);

        browseBtn = uif.createButton("ct.browse", listener);
        p.add(browseBtn, BorderLayout.EAST);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets.top = 10;
        c.insets.right = 0;
        c.weightx = 1;
        body.add(p, c);

        GridBagConstraints c2 = new GridBagConstraints();
        body.add(Box.createHorizontalGlue(), c2);

        testSuiteCheckBox = uif.createCheckBox("ct.testSuite");
        testSuiteCheckBox.addChangeListener(listener);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.insets.top = 0;
        body.add(testSuiteCheckBox, c);

        body.add(Box.createHorizontalGlue(), c2);

        workDirCheckBox = uif.createCheckBox("ct.workDir");
        workDirCheckBox.setEnabled(testSuiteCheckBox.isSelected());
        body.add(workDirCheckBox, c);

        body.add(Box.createHorizontalGlue(), c2);

        markersCheckBox = uif.createCheckBox("ct.markers");
        markersCheckBox.addChangeListener(listener);
        body.add(markersCheckBox, c);

        body.add(Box.createHorizontalGlue(), c2);

        filterCheckBox = uif.createCheckBox("ct.filter");
        filterCheckBox.setEnabled(markersCheckBox.isSelected());
        body.add(filterCheckBox, c);

        body.add(Box.createHorizontalGlue(), c2);

        clearCheckBox = uif.createCheckBox("ct.clear");
        body.add(clearCheckBox, c);

        setBody(body);

        okBtn = uif.createButton("ct.ok", listener);
        okBtn.setEnabled(false); // enable when file field looks OK
        JButton cancelBtn = uif.createCancelButton("ct.cancel");
        JButton helpBtn = uif.createHelpButton("ct.help","confEdit.templateDialog.csh" );
        setButtons(new JButton[] { okBtn, cancelBtn, helpBtn }, null);
    }

    private void doBrowse() {
        String fileFieldText = fileField.getText();
        File f = null;

        if (fileFieldText.length() > 0) {
            f = new File(fileFieldText);
            if (f.isDirectory())
                fileChooser.setCurrentDirectory(f);
            else
                fileChooser.setSelectedFile(f);
        }

        File file = InterviewEditor.saveConfigFile(model.getContextManager(), parent, uif, fileChooser, f, false);

        if (file != null)
            fileField.setText(file.getPath());
    }

    private boolean doSave() {
        String fileFieldText = fileField.getText();
        if (fileFieldText.length() == 0)
            return false;

        File file = new File(fileFieldText);

        // if file exists, leave well enough alone;
        // otherwise, make sure it ends with .jti
        if (!file.exists()) {
            String path = file.getPath();
            if (!path.endsWith(".jti"))
                file = new File(path + ".jti");
        }

        // if file exists, make sure user wants to overwrite it
        if (file.exists()) {
            int rc = uif.showYesNoCancelDialog("ce.save.warn");
            switch (rc) {
            case JOptionPane.YES_OPTION:
                break;  // use this file

            default:
                return false; // save unsuccessful
            }
        }

        InterviewParameters c;
        boolean createdNew = false;
        if (clearCheckBox.isSelected()
            || markersCheckBox.isSelected() != config.getMarkersEnabled()
            || filterCheckBox.isSelected() != config.getMarkersFilterEnabled()) {
            try {
                c = config.getTestSuite().createInterview();
                createdNew = true;
                HashMap data = new HashMap();
                config.save(data);
                c.load(data, false);
            }
            catch (Interview.Fault e) {
                // ignore, for now; should not happen
                return false;
            }
            catch (TestSuite.Fault e) {
                // ignore, for now; should not happen
                return false;
            }

            c.setMarkersEnabled(markersCheckBox.isSelected());
            c.setMarkersFilterEnabled(filterCheckBox.isSelected());

            if (clearCheckBox.isSelected())
                c.clearMarkedResponses(null); // null == default marker name
        }
        else
            c = config;

        boolean tsb = testSuiteCheckBox.isSelected();
        boolean wdb = workDirCheckBox.isSelected();

        try {
            c.saveAs(file, tsb, (tsb == false ? false : wdb), true);
            if (createdNew) {
                c.dispose();
            }
            return true;
        }
        catch (IOException e) {
            uif.showError("ct.save", e.toString());
            return false;
        }
        catch (Interview.Fault e) {
            uif.showError("ct.save", e.getMessage());
            return false;
        }
    }

    private InterviewParameters config;
    private FileChooser fileChooser;
    private JTextField fileField;
    private JButton browseBtn;
    private JCheckBox testSuiteCheckBox;
    private JCheckBox workDirCheckBox;
    private JCheckBox markersCheckBox;
    private JCheckBox filterCheckBox;
    private JCheckBox clearCheckBox;
    private JButton okBtn;

    private ExecModel model;

    private Listener listener;

    private class Listener
        implements ActionListener, ChangeListener, DocumentListener
    {
        //----- for ActionListener ----------------------

        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src == browseBtn)
                doBrowse();
            else if (src == okBtn) {
                boolean ok = doSave();
                if (ok)
                    setVisible(false);
            }
        }

        //----- for ChangeListener ----------------------

        public void stateChanged(ChangeEvent e) {
            Object src = e.getSource();
            if (src == testSuiteCheckBox) {
                workDirCheckBox.setEnabled(testSuiteCheckBox.isSelected());
            }
            else if (src == markersCheckBox) {
                filterCheckBox.setEnabled(markersCheckBox.isSelected());
            }
        }

        //----- for DocumentListener ----------------------

        public void insertUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        public void removeUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        public void changedUpdate(DocumentEvent e) {
            String path = fileField.getText();
            boolean ok;

            if (path.length() == 0)
                ok = false;
            else {
                File f = new File(path);
                if (f.exists() && f.isDirectory())
                    ok = false;
                else {
                    File parent = f.getParentFile();
                    ok = (parent != null && parent.exists() && parent.isDirectory());
                }
            }

            okBtn.setEnabled(ok);
        }
    }
}
