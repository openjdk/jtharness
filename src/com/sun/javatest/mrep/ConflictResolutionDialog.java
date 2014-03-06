/*
 * $Id$
 *
 * Copyright (c) 2006, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.sun.javatest.tool.UIFactory;
import javax.swing.JOptionPane;

public class ConflictResolutionDialog extends JDialog {

    private String resolveButtonStr = "resolve";
    private String cancelButtonStr = "cancel";
    private String useMostRecentCheckBoxStr = "useMost";

    private JCheckBox preferredReportCheckBox;
    private JCheckBox useMostRecentCheckBox;

    private JButton resolveButton;
    private JButton cancelButton;

    private DefaultListModel listModel;
    private JList list;

    private int     selectedIndex;
    private boolean bPreferredReport;
    private boolean bUseMostRecent;

    private UIFactory uif;

    private boolean cancel = false;


    public ConflictResolutionDialog(JFrame parent, String testName, String[] reportsList, boolean bPreferredSet, UIFactory uif) {
        super(parent, true);
        this.uif = uif;

        setName("conflict");
        setTitle(uif.getI18NString("conflict.name"));
        setResizable(false);

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        ConflictResolutionActionListener conflictResolutionListener = new ConflictResolutionActionListener();

        JLabel text = uif.createLabel("conflict.text");
        text.setText(text.getText() + " " + testName);


        text.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JPanel textPanel = uif.createPanel("conflict.text.panel", new FlowLayout(FlowLayout.CENTER));
        textPanel.add(text);

        Box vBox = Box.createVerticalBox();

        JLabel chooseText =uif.createLabel("conflict.chooseText");
        //text.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JPanel chooseTextPanel = uif.createPanel("conflict.choosePanel", new FlowLayout(FlowLayout.CENTER));
        chooseTextPanel.add(chooseText);

        // Build list box
        listModel=new DefaultListModel();
        for (int i=0; i< reportsList.length; i++) {
            listModel.addElement(reportsList[i]);
        }
        list = uif.createList("conflict.list", listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ReportsListSelectionListener rl = new ReportsListSelectionListener();
        list.addListSelectionListener(rl);
        Border brd = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK);
        list.setBorder(brd);
        JScrollPane scrollPane = uif.createScrollPane(list);
        Box hBox = Box.createHorizontalBox();
        //hBox.add(Box.createHorizontalStrut(20));
        hBox.add(scrollPane);
        //hBox.add(Box.createHorizontalStrut(20));

        preferredReportCheckBox = uif.createCheckBox("conflict.preffered");
        preferredReportCheckBox.setMnemonic(0);
        preferredReportCheckBox.setEnabled(false);
        JPanel preferredReportPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        preferredReportPanel.add(preferredReportCheckBox);


        useMostRecentCheckBox = uif.createCheckBox("conflict.most.recent");
        useMostRecentCheckBox.setMnemonic(1);
        useMostRecentCheckBox.addActionListener(conflictResolutionListener);
        useMostRecentCheckBox.setActionCommand(useMostRecentCheckBoxStr);
        JPanel useRecentPanel = uif.createPanel("conflict.recent", new FlowLayout(FlowLayout.LEFT));
        useRecentPanel.add(useMostRecentCheckBox);



        vBox.setBorder(BorderFactory.createEmptyBorder(0,20,0,20));
        vBox.add(chooseTextPanel);
        vBox.add(hBox);
        // if preferred report was already chosen, in previous dialogs, it should not be seen here
        if (!bPreferredSet) {
            vBox.add(preferredReportPanel);
        }
        vBox.add(useRecentPanel);


        // Build control buttons
        JPanel controlButtonsPanel = uif.createPanel("conflict.control", new FlowLayout(FlowLayout.CENTER));
        JPanel p2 = new JPanel();
        p2.setLayout(new GridLayout(1,0,5,5));

        resolveButton = uif.createButton("conflict.resolve");
        resolveButton.setMnemonic(0);
        resolveButton.addActionListener(conflictResolutionListener);
        resolveButton.setActionCommand(resolveButtonStr);
        resolveButton.setEnabled(false);

        cancelButton = uif.createButton("conflict.cancel");
        cancelButton.addActionListener(conflictResolutionListener);
        cancelButton.setActionCommand(cancelButtonStr);

        p2.add(resolveButton);
        p2.add(cancelButton);

        controlButtonsPanel.add(p2);
        controlButtonsPanel.setBorder(BorderFactory.createEmptyBorder(20,0,0,0));

        cp.add(textPanel, BorderLayout.NORTH);
        cp.add(vBox, BorderLayout.CENTER);
        cp.add(controlButtonsPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    public int  getSelectedIndex() {
        return selectedIndex;
    }
    public boolean getPreferredReport() {
        return bPreferredReport;
    }

    public boolean getUseMostRecent() {
        return bUseMostRecent;
    }

    public boolean wasCanceled() {
        return cancel;
    }

    class CancelException extends Exception {

    }


    class ConflictResolutionActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            String cmd = e.getActionCommand();
            if (cmd.equals(cancelButtonStr)) {

                if (uif.showYesNoDialog("conflict.areyousure") != JOptionPane.YES_OPTION)
                    return;

                ConflictResolutionDialog.this.cancel = true;
                ConflictResolutionDialog.this.dispose();
            } else if (cmd.equals(resolveButtonStr)) {
                bUseMostRecent = useMostRecentCheckBox.isSelected();
                bPreferredReport = preferredReportCheckBox.isSelected();
                selectedIndex = list.getSelectedIndex();
                ConflictResolutionDialog.this.dispose();

            } else if (cmd.equals(useMostRecentCheckBoxStr)) {
                if ((list.getSelectedValues().length == 0) &&
                        (!useMostRecentCheckBox.isSelected())) {
                    resolveButton.setEnabled(false);
                } else {
                    resolveButton.setEnabled(true);
                }


                if (useMostRecentCheckBox.isSelected()) {
                    list.setEnabled(false);
                    preferredReportCheckBox.setEnabled(false);
                } else {
                    list.setEnabled(true);
                    preferredReportCheckBox.setEnabled(true);
                }
            } else
                ; // ignore events on all other objects
        }
    }


    class ReportsListSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            if(e.getValueIsAdjusting()) return;

            if ((list.getSelectedValues().length == 0) &&
                    (!useMostRecentCheckBox.isSelected())) {
                resolveButton.setEnabled(false);
            } else {
                resolveButton.setEnabled(true);
            }


            if (list.getSelectedValues().length == 0) {
                preferredReportCheckBox.setEnabled(false);
            } else {
                preferredReportCheckBox.setEnabled(true);
            }

        }
    }

}
