/*
 * $Id$
 *
 * Copyright (c) 2002, 2012, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.mrep;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import com.sun.javatest.report.ReportDirChooser;
import com.sun.javatest.tool.IconFactory;
import com.sun.javatest.tool.UIFactory;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.io.IOException;
import javax.swing.filechooser.FileView;

class FilesPane extends JPanel {

    FilesPane(UIFactory uif, final ActionListener nextListener) {
        this.uif = uif;
        this.listener = new Listener();

        setName("files");
        setLayout(new GridBagLayout());
        setFocusable(false);
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JLabel title = uif.createLabel("files.title");
        GridBagConstraints lc = new GridBagConstraints();
        lc.gridwidth = GridBagConstraints.REMAINDER;
        lc.weightx = 1.0;
        lc.fill = GridBagConstraints.HORIZONTAL;
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(5,5,15,5);
        this.add(title, lc);


        JLabel wdLabel = uif.createLabel("files.resultLabel", true);
        lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.EAST;
        lc.insets = new Insets(5,5,0,5);
        this.add(wdLabel, lc);

        resultField = uif.createInputField("files.result", wdLabel);
        resultField.addActionListener(listener);
        resultField.addKeyListener(listener);
        resultField.addFocusListener(listener);

        GridBagConstraints fc = new GridBagConstraints();
        fc.insets = new Insets(5,0,0,5);
        fc.weightx = 1.0;
        fc.fill = GridBagConstraints.HORIZONTAL;
        this.add(resultField, fc);

        resultBtn = uif.createButton("files.result.browse", listener);
        GridBagConstraints bc = new GridBagConstraints();
        bc.insets = new Insets(5,0,0,5);
        bc.gridwidth = GridBagConstraints.REMAINDER;
        this.add(resultBtn, bc);

        // Report directory UI ends
        // Panel with xml files begin

        GridBagConstraints pan = new GridBagConstraints();
        pan.gridwidth = GridBagConstraints.REMAINDER;
        pan.fill = GridBagConstraints.BOTH;
        pan.insets = new Insets(10,0,5,0);
        pan.weightx = 1.0;
        pan.weighty = 1.0;

        JScrollPane js = new JScrollPane();
        js.setBorder(uif.createTitledBorder("files.merged"));
        js.setName("files.in");
        js.setViewportView(new MergedSubPanel(uif));
        js.createVerticalScrollBar();
        js.getViewport().setName("files.inview");
        this.add(js, pan);

        Dimension d = this.getPreferredSize();

        int dpi = uif.getDotsPerInch();
        this.setPreferredSize(new Dimension(Math.max(d.width, 5 * dpi),
                d.height));

        // Panel with xml files begin
        // Buttons begin

        nextBtn = uif.createButton("files.next", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (checkInput())
                    nextListener.actionPerformed(e);
            }
        });
        nextBtn.setEnabled(false);
        JButton cancelBtn = uif.createCancelButton("files.cancel");
        JButton helpBtn = uif.createHelpButton("files.help",
                "mergeReports.window.csh");
        buttons = new JButton[] { nextBtn, cancelBtn, helpBtn };
        JPanel buttonsPanel = uif.createPanel("files.but");
        GridBagConstraints co = new GridBagConstraints();
        co.anchor = GridBagConstraints.EAST;
        co.weightx = 1;
        co.gridwidth = 3;
        co.insets = new Insets(5,0,0,0);
        this.add(buttonsPanel, co);
        buttonsPanel.setLayout(new GridLayout(1, 3, 5, 5));
        for (int i = 0; i < buttons.length; i++) {
            buttonsPanel.add(buttons[i]);
        }

    }

    boolean checkInput() {
        String[] merged = getXmlFiles();

        // check the files
        for (int i=0; i < merged.length; i++) {
            if (!FilesPane.isXMLReport(new File(merged[i]))) {
                uif.showError("files.wrongfileformat", merged[i]);
                return false;
            }
        }

        if ("".equals(getResultDir())) {
            uif.showError("files.nooutfile");
            return false;
        }

        List mergedList = new ArrayList();
        for (int i = 0; i < merged.length; i++) {
            merged[i] = merged[i].trim();
            if ( !"".equals(merged[i])) {
                if (mergedList.contains(merged[i])) {
                    uif.showError("files.duplicateinputfiles", merged[i]);
                    return false;
                }
                mergedList.add(merged[i]);
            }
        }

        if (mergedList.size() < 1) {
            uif.showError("files.noinputfiles");
            return false;
        }

        return true;
    }



    private void enableNext() {
        nextBtn.setEnabled(isNextEnabled());
    }

    private boolean isNextEnabled() {
        boolean resDirEnabled = true;   // block "next" button when result directory is not provided (should be true to unblock)
        resultField.setBackground(UIFactory.getDefaultInputColor());
        if ("".equals(getResultDir()) && "".equals(resultField.getText().trim())) {
            if(!resultField.hasFocus())
                resultField.setBackground(UIFactory.getInvalidInputColor());
            resDirEnabled = false;
        }

        boolean error = false;  // block "next" button if any error occurred (should be false to unblock)
        ArrayList<String> used = new ArrayList<String>();
        for (int i = 0; i < merged.size(); i++) {
            JTextField tField = (JTextField) merged.get(i);
            String s = tField.getText().trim();
            tField.setBackground(UIFactory.getDefaultInputColor());
            if (!"".equals(s)) {        // do nothing, if value is empty
                if(!used.contains(s)) { // check, if such value was already used
                    used.add(s);
                    if (!isXMLReport(new File(s))) {    // check file is xml report
                        error = true;
                        if(!tField.hasFocus())
                            tField.setBackground(UIFactory.getInvalidInputColor());
                    } else {
                        tField.setBackground(UIFactory.getValidInputColor());
                    }
                } else {
                    error = true;
                    tField.setBackground(UIFactory.getInvalidInputColor());
                    ((JTextField)merged.get(used.indexOf(s))).setBackground(UIFactory.getInvalidInputColor());
                }
            }
        }
        if(used.isEmpty()) // none file provided
            return false;

        return !error && resDirEnabled;
    }


    class MergedSubPanel extends JPanel {

        MergedSubPanel(UIFactory uif) {
            setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
            setName("tool.merged");
            setLayout(new GridBagLayout());

            merged = new ArrayList();
            this.mergedBtns = new ArrayList();

            addMore = uif.createButton("files.addmore", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addXmlToMerge();
                }
            });
            GridBagConstraints bc = new GridBagConstraints();
            bc.gridwidth = GridBagConstraints.REMAINDER;
            bc.anchor = GridBagConstraints.NORTH;
            bc.insets.top = 5;
            bc.gridx = 1;
            this.add(addMore, bc);

            addXmlToMerge();
            addXmlToMerge();
        }

        private void addXmlToMerge() {
            int num = merged.size();
            GridBagConstraints fc = new GridBagConstraints();
            fc.fill = GridBagConstraints.BOTH;
            fc.weightx = 1.0;
            fc.insets.top = 5;
            fc.insets.left = 5;

            GridBagConstraints bc = new GridBagConstraints();
            bc.gridwidth = GridBagConstraints.REMAINDER;
            bc.insets.top = 5;
            bc.insets.left = 10;
            bc.insets.right = 5;
            bc.fill = GridBagConstraints.HORIZONTAL;

            JTextField mergedField = uif.createInputField("files.input"/* + num*/);
            uif.setAccessibleName(mergedField, "files.input");
            mergedField.addKeyListener(listener);
            mergedField.addFocusListener(listener);
            this.add(mergedField, fc);
            JButton xmlBtn = uif.createButton("files.result.browse",
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            Object src = e.getSource();
                            for (int i = 0; i < mergedBtns.size(); i++) {
                                if (src == mergedBtns.get(i)) {
                                    chooseXmlReportFile((JTextField) merged
                                            .get(i));
                                }
                            }
                            enableNext();
                        }
                    });
            xmlBtn.setMnemonic('1' + num);
            this.add(xmlBtn, bc);

            merged.add(mergedField);
            mergedBtns.add(xmlBtn);
            this.remove(addMore);
            bc.anchor = GridBagConstraints.NORTH;
            bc.gridy = num + 1;
            bc.gridx = 1;
            bc.weighty = 1.0;
            this.add(addMore, bc);
            this.updateUI();
        }

        private java.util.List mergedBtns;
        private JButton addMore;

    }

    private void chooseXmlReportFile(JTextField field) {
        if (xmlFileChooser == null) {
            xmlFileChooser = new JFileChooser();
            xmlFileChooser.setFileView(new XMLReportView());
            xmlFileChooser.setCurrentDirectory(null);
            xmlFileChooser.addChoosableFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    return (f.isDirectory() || isXMLReport(f));
                }

                public String getDescription() {
                    return uif.getI18NString("files.xmlFiles");
                }
            });
        }

        if (!field.getText().trim().equals("")) {
            File entered = new File(field.getText());
            if (entered.exists()) {
                xmlFileChooser.setCurrentDirectory(entered);
            }
        }

        int action = xmlFileChooser.showOpenDialog(null);
        if (action != JFileChooser.APPROVE_OPTION)
            return;

        File cf = xmlFileChooser.getSelectedFile();
        String cfp;
        if (cf == null)
            cfp = "";
        else {
            cfp = cf.getPath();
            if (!cfp.endsWith(".xml"))
                cfp += ".xml";
        }
        field.setText(cfp);
    }

    JButton[] getButtons() {
        return buttons;
    }

    private void showReportChooserDialog() {

        if (reportDirChooser == null)
            reportDirChooser = new ReportDirChooser();
        reportDirChooser.setMode(ReportDirChooser.NEW);
        File f = new File(getResultDir());
        if (f.exists() && f.isDirectory()) {
            reportDirChooser.setCurrentDirectory(f);
        }
        int option = reportDirChooser.showDialog(resultField);
        if (option != JFileChooser.APPROVE_OPTION)
            return;

        resultField.setText(reportDirChooser.getSelectedFile().getAbsolutePath());
    }

    static boolean isXMLReport(File f) {

        String schemaLocation = "xsi:noNamespaceSchemaLocation=\"Report.xsd\"";
        String formatVersion = "formatVersion=\"v1\"";

        if (!f.getName().endsWith(".xml")) return false;
        FileReader fr = null;
        try {
            fr = new FileReader(f);
            BufferedReader r = new BufferedReader(fr);
            boolean hasSchema, hasVersion = false;

            // line 1
            String line = r.readLine();
            hasSchema = hasLineContent(line, schemaLocation);
            hasVersion = hasLineContent(line, formatVersion);

            // line 2
            line = r.readLine();
            hasSchema = (hasSchema || hasLineContent(line, schemaLocation));
            hasVersion = (hasVersion || hasLineContent(line, formatVersion));
            return (hasSchema && hasVersion);
        } catch (IOException ex) {
            return false;
        } finally {
            try {
                if (fr != null) fr.close();
            } catch (IOException ex) {
                // nothing...
            }
        }
    }

    /**
     * Determines if a String contains the target String.
     * @return True if target is a substring of line, false otherwise and
     *    false if either argument is null.
     */
    private static boolean hasLineContent(String line, String target) {
        if (line == null || target == null)
            return false;

        return (line.indexOf(target) >= 0 ? true : false);
    }

    private class XMLReportView extends FileView {

        public XMLReportView() {
            super();
            icon = IconFactory.getReportIcon();
        }

        public Icon getIcon(File f) {
            return isXMLReport(f) ? icon : null;
        }

        private Icon icon;
    }

    private UIFactory uif;

    private Listener listener;

    String getResultDir() {
        return resultField.getText();
    }

    String[] getXmlFiles() {
        int l=0;
        for (int i = 0; i < merged.size(); i++) {
            String s = ((JTextField)merged.get(i)).getText().trim();
            if (!"".equals(s)) l++;
        }

        String[] result = new String[l];
        l = 0;
        for (int i = 0; i < merged.size(); i++) {
            String s = ((JTextField)merged.get(i)).getText().trim();
            if (!"".equals(s)) {
                result[l++] = ((JTextField)merged.get(i)).getText().trim();
            }
        }
        return result;
    }

    private List merged;
    static final String OK = "OK";

    private class Listener extends ComponentAdapter implements ActionListener, FocusListener, KeyListener {
        // ComponentListener
        public void componentShown(ComponentEvent e) {

        }

        // ActionListener
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src == resultBtn) {
                showReportChooserDialog();
                enableNext();
            }

        }

        public void focusGained(FocusEvent e) {
            enableNext();
        }

        public void focusLost(FocusEvent e) {
            enableNext();
        }

        public void keyTyped(KeyEvent e) {}

        public void keyPressed(KeyEvent e) {}

        public void keyReleased(KeyEvent e) {
            enableNext();
        }
    };

    private JButton resultBtn;
    private JButton[] buttons;
    private JButton nextBtn;

    private JTextField resultField;

    private JFileChooser xmlFileChooser;
    private ReportDirChooser reportDirChooser;

    //keys for option values used for save & restore
    static final String REPORT_DIR = "reportDir";
}
