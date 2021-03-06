/*
 * $Id$
 *
 * Copyright (c) 2006, 2012, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.CompositeFilter;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.TestFilter;
import com.sun.javatest.exec.ContextManager;
import com.sun.javatest.exec.ExecTool;
import com.sun.javatest.report.CustomReport;
import com.sun.javatest.report.CustomReport.ReportConfigPanel;
import com.sun.javatest.report.ReportSettings;
import com.sun.javatest.tool.Desktop;
import com.sun.javatest.tool.Tool;
import com.sun.javatest.tool.UIFactory;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

class OptionsPane extends JPanel {

    private static final int DOTS_PER_INCH = Toolkit.getDefaultToolkit().getScreenResolution();
    private DefaultListModel<JCheckBox> listModel;
    private JList<JCheckBox> list;
    private CustomReport[] customReports;
    private JTextArea descriptionArea;
    private Map<JCheckBox, CustomReport> customBoxes;
    private JCheckBox cbXml;
    private JButton backBtn;
    private JButton okBtn;
    private JButton cancelBtn;
    private JCheckBox handleConfilctsBox;
    private JCheckBox resolveAsRecentBox;
    private ActionListener chTabListener;
    private ActionListener okListener;
    private JButton helpBtn;
    private Desktop desktop;
    private UIFactory uif;
    OptionsPane(UIFactory uif, Desktop desktop, ActionListener chTabListener,
                ActionListener okListener) {
        this.uif = uif;
        this.desktop = desktop;
        this.chTabListener = chTabListener;
        this.okListener = okListener;
        initGUI();
    }

    boolean resolveByRecent() {
        return resolveAsRecentBox.isSelected();
    }

    boolean isXmlReport() {
        return cbXml.isSelected();
    }

    CustomReport[] getCustomSelected() {
        return getActiveCustomReports().toArray(new CustomReport[0]);
    }

    protected void initGUI() {
        setName("opts");
        setFocusable(false);
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = uif.createLabel("opts.title");
        GridBagConstraints lc = new GridBagConstraints();
        lc.gridwidth = GridBagConstraints.REMAINDER;
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(5, 5, 15, 5);
        lc.fill = GridBagConstraints.HORIZONTAL;
        lc.weightx = 1.0;
        this.add(title, lc);

        lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.NORTHEAST;
        lc.gridheight = 2;
        lc.insets.right = 10;
        JLabel conflictLabel = uif.createLabel("opts.conflict", true);
        this.add(conflictLabel, lc);

        handleConfilctsBox = uif.createCheckBox("opts.handleconflict", true);
        lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.NORTHEAST;
        lc.fill = GridBagConstraints.HORIZONTAL;
        lc.gridwidth = GridBagConstraints.REMAINDER;
        handleConfilctsBox.setEnabled(false);
        this.add(handleConfilctsBox, lc);

        resolveAsRecentBox = uif.createCheckBox("opts.resolverecent", true);
        lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.NORTHEAST;
        lc.fill = GridBagConstraints.HORIZONTAL;
        this.add(resolveAsRecentBox, lc);

// --------------------------------------------------------------------------------
        JPanel bottom = uif.createPanel("opts.bottom", false);
        bottom.setLayout(new BorderLayout());
        bottom.setBorder(BorderFactory.createCompoundBorder(
                uif.createTitledBorder("opts.bottom"),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));


        listModel = new DefaultListModel<>();

        // populate list and card panel
        CardLayout cards = new CardLayout();
        JPanel p = uif.createPanel("opts.typecards", cards, false);

        // standard report
        cbXml = uif.createCheckBox("opts.type.xml", false);
        listModel.addElement(cbXml);
        p.add("opts.type.xml", uif.createPanel("opts.blank", false));

        // custom entries
        getCustomReports(p);

        list = uif.createList("opts.typel", listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        SelectListener sl = new SelectListener(list, p, cards);
        list.addMouseListener(sl);
        list.addKeyListener(sl);
        list.addListSelectionListener(sl);
        list.setCellRenderer(new CheckBoxListCellRenderer());
        list.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(0, 3, 0, 3)));


        descriptionArea = uif.createMessageArea("opts.type.desc");
        descriptionArea.setRows(3);
        Font f = descriptionArea.getFont();
        f = f.deriveFont(Font.PLAIN, f.getSize2D() - 1);
        descriptionArea.setFont(f);


        if (customBoxes != null && list != null) {
            CustomReport val = customBoxes.get(list.getSelectedValue());
            if (val != null) {
                descriptionArea.setText(val.getDescription());
            }
        }

        JPanel rightCenter = uif.createPanel("opts.blank", false);
        rightCenter.setLayout(new BorderLayout());
        //rightCenter.setBackground(Color.magenta);
        rightCenter.add(descriptionArea, BorderLayout.PAGE_START);
        rightCenter.add(p, BorderLayout.CENTER);
        bottom.add(rightCenter, BorderLayout.CENTER);

        bottom.add(list, BorderLayout.WEST);

        lc.insets.left = 0;
        lc.gridwidth = 1;
        lc.gridy = 3;
        lc.anchor = GridBagConstraints.NORTHWEST;
        lc.gridwidth = GridBagConstraints.REMAINDER;
        lc.fill = GridBagConstraints.BOTH;
        lc.weighty = 1.0;

        add(bottom, lc);

//---------------------------------------------------------------------------------

        okBtn = uif.createButton("opts.ok", okListener);
        cancelBtn = uif.createCancelButton("opts.cancel");
        backBtn = uif.createButton("opts.back", chTabListener);
        helpBtn = uif.createHelpButton("opts.help",
                "mergeReports.window.csh");
        JButton[] buttons = {okBtn, cancelBtn, helpBtn};

        JPanel buttonsPanel = uif.createPanel("opts.but");
        GridBagConstraints co = new GridBagConstraints();
        co.anchor = GridBagConstraints.EAST;
        co.weightx = 1;
        co.gridwidth = 3;
        co.insets = new Insets(10, 0, 0, 0);

        GridBagConstraints bb = new GridBagConstraints();
        bb.anchor = GridBagConstraints.WEST;
        bb.insets = new Insets(10, 0, 0, 0);
        this.add(backBtn, bb);

        this.add(buttonsPanel, co);
        buttonsPanel.setLayout(new GridLayout(1, 3, 5, 5));

        GridBagConstraints b = new GridBagConstraints();
        b.anchor = GridBagConstraints.EAST;
        co.insets = new Insets(10, 0, 0, 0);
        for (JButton button : buttons) {
            buttonsPanel.add(button);

        }

        list.setSelectedIndex(0);
    }

    /**
     * @return number of custom reports added
     */
    private int getCustomReports(JPanel p) {
        int result = 0;
        Tool[] tools = desktop.getTools();
        List<CustomReport> customReportsList = new ArrayList<>();
        for (Tool tool1 : tools) {
            if (tool1 instanceof ExecTool) {
                // should not be using report types from ExecTool
                // should have a separate list available for this tool
                ExecTool tool = (ExecTool) tool1;
                InterviewParameters ip = tool.getInterviewParameters();
                TestFilter tf = new CompositeFilter(ip.getFilters());
                ReportSettings rs = new ReportSettings(ip);
                rs.setFilter(tf);
                ContextManager cm = tool.getContextManager();
                if (cm != null && cm.getCustomReports() != null) {
                    for (CustomReport cr : cm.getCustomReports()) {
                        cr.setEnviroment(rs);
                        customReportsList.add(cr);
                    }
                }
            }
        }   // for

        customReports = customReportsList
                .toArray(new CustomReport[0]);
        if (customReports == null || customReports.length == 0) {
            customReports = null;
            return 0;
        }

        customBoxes = new HashMap<>();

        for (CustomReport customReport : customReports) {
            JCheckBox cb = new JCheckBox(customReport.getName());
            cb.setName(customReport.getReportId());
            listModel.addElement(cb);
            customBoxes.put(cb, customReport);

            ReportConfigPanel[] ops = customReport.getOptionPanes();
            if (ops == null || ops.length == 0) {
                // no config panels, use blank
                p.add(customReport.getReportId(), uif.createPanel(
                        "opts.blank", false));
            } else {
                // tabbed pane for all supplied panels
                JTabbedPane tp = uif.createTabbedPane("opts.custom.tabs");
                for (ReportConfigPanel op : ops) {
                    tp.addTab(op.getPanelName(), op);
                }

                p.add(customReport.getReportId(), tp);
            }
            result++;
        } // for

        return result;
    }

    private ArrayList<CustomReport> getActiveCustomReports() {

        ArrayList<CustomReport> customReps = new ArrayList<>();
        if (customBoxes != null && !customBoxes.isEmpty()) {
            for (JCheckBox box : customBoxes.keySet()) {
                if (box.isSelected()) {
                    customReps.add(customBoxes.get(box));
                }
            }
        }
        return customReps;
    }

    JButton[] getButtons() {
        return new JButton[]{backBtn, okBtn, cancelBtn, helpBtn};
    }

    /*
     * PropertyChangeListener for enabling/disabling container's content
     */
    private static class PanelEnableListener implements PropertyChangeListener {
        private Container theContainer;
        private Set<Component> enabledComp;

        /**
         * @param container Container for controlling
         */
        PanelEnableListener(Container container) {
            theContainer = container;
        }

        /**
         * Catches changes of "enabled" property
         * and changes enabled status for all child components
         */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("enabled".equals(evt.getPropertyName())) {
                boolean oldV = ((Boolean) evt.getOldValue()).booleanValue();
                boolean newV = ((Boolean) evt.getNewValue()).booleanValue();
                if (oldV && !newV) {
                    // disable
                    Iterator<Component> chIt = collectChildren(theContainer,
                            new ArrayList<>()).iterator();
                    enabledComp = new HashSet<>();
                    while (chIt.hasNext()) {
                        Component c = chIt.next();
                        if (c.isEnabled()) {
                            enabledComp.add(c);
                            c.setEnabled(false);
                        }
                    }

                } else if (!oldV && newV && enabledComp != null) {
                    // enable
                    for (Component c : collectChildren(theContainer,
                            new ArrayList<>())) {
                        if (enabledComp.contains(c)) {
                            c.setEnabled(true);
                        }
                    }
                }
            }
        }

        /**
         * Recursively gathers all children components
         */
        private Collection<Component> collectChildren(Container comp, Collection<Component> c) {
            Component[] ch = comp.getComponents();
            for (Component aCh : ch) {
                c.add(aCh);
                if (aCh instanceof Container) {
                    collectChildren((Container) aCh, c);
                }
            }
            return c;
        }
    }

    private static class CheckBoxListCellRenderer implements ListCellRenderer<JCheckBox> {
        @Override
        public Component getListCellRendererComponent(JList<? extends JCheckBox> list, JCheckBox comp,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            // assert: value is a JCheckBox
            if (isSelected) {
                comp.setOpaque(true);
                comp.setBackground(UIFactory.Colors.TEXT_HIGHLIGHT_COLOR.getValue());
            } else {
                comp.setOpaque(false);
                comp.setForeground(Color.black);
            }

            return comp;
        }
    }

    /**
     * This listener changes options state against checkboxes
     */
    private class SelectListener extends MouseInputAdapter implements
            KeyListener, ListSelectionListener {

        Object lastSelected;
        JList<JCheckBox> list;
        ListModel<JCheckBox> listModel;
        JPanel panel;
        CardLayout cards;
        double emptyCBW = new JCheckBox("").getPreferredSize().getWidth() + 2;

        /**
         * @param lst        JList of checkboxes
         * @param p          parent Panel
         * @param cardLayout The CardLayout for options
         */
        SelectListener(JList<JCheckBox> lst, JPanel p, CardLayout cardLayout) {
            list = lst;
            listModel = list.getModel();
            lastSelected = listModel.getElementAt(0);
            panel = p;
            cards = cardLayout;
        }

        @Override
        public void keyTyped(KeyEvent e) {
            if (e.getKeyChar() == ' ') {
                process(list.getSelectedIndex());
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getPoint().getX() <= emptyCBW) {
                process(list.locationToIndex(e.getPoint()));
            }
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            int index = list.getSelectedIndex();
            JCheckBox box = listModel.getElementAt(index);

            if (lastSelected != box) {
                cards.show(panel, box.getName());
                lastSelected = box;
            }
            enablePanel(box);
        }

        private void enablePanel(JCheckBox box) {
            for (int i = 0; i < panel.getComponentCount(); i++) {
                JComponent tab = (JComponent) panel.getComponent(i);
                tab.setEnabled(box.isSelected());
            }

            if (box == cbXml) {
                descriptionArea.setText(uif.getI18NString("opts.xmlDesc.txt"));
                return;
            }

            if (customBoxes != null) {
                CustomReport rep = customBoxes.get(box);
                if (rep != null) {
                    if (rep.getOptionPanes() != null) {
                        for (int i = 0; i < rep.getOptionPanes().length; i++) {
                            rep.getOptionPanes()[i].setEnabled(box.isSelected());
                        }   // for
                    }

                    descriptionArea.setText(rep.getDescription());
                }
            }
        }

        private void process(int index) {
            JCheckBox box = listModel.getElementAt(index);

            if (lastSelected == box) {
                box.doClick();
                list.repaint(); // important!
                enablePanel(box);
            }
            lastSelected = box;
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
        }
    }
}
