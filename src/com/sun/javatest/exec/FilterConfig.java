/*
 * $Id$
 *
 * Copyright (c) 2001, 2011, Oracle and/or its affiliates. All rights reserved.
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
import java.awt.Component;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Hashtable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.sun.javatest.ObservableTestFilter;
import com.sun.javatest.TestFilter;
import com.sun.javatest.TestSuite;
import com.sun.javatest.tool.ToolDialog;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.util.Debug;
import com.sun.javatest.util.DynamicArray;

/**
 * The primary purpose is to hold the a variety of named filter sets from
 * which the user will select one (the "active" one).
 */
public class FilterConfig {
     /**
      * Observe changes to the state of the view.  The changes to the view state
      * will generally be the result of user actions.
      */
    public interface Observer {
       /**
        * The state of the given filter has changed.
        */
        public void filterUpdated(TestFilter f);

        public void filterAdded(TestFilter f);

       /**
        * Removing the active filter will result in an exception.
        */
        public void filterRemoved(TestFilter f);
    }

    public FilterConfig(ExecModel e, JComponent parent, UIFactory uif) {
        // load default filters here instead of ExecTool to avoid instantiating
        // them
        execModel = e;
        this.parentComponent = parent;
        this.uif = uif;
    }

    /**
     * Add a test filter to the list of possible filters.
     * Care should be taken not to add a filter more than once.
     */
    synchronized void add(TestFilter f) {
        if (f == null)
            return;

        // record this filter officially
        filters = (TestFilter[])DynamicArray.append(filters, f);

        // register as change observer if possible
        if (f instanceof ObservableTestFilter) {
            ((ObservableTestFilter)f).addObserver(listener);
        }

        if (f instanceof ConfigurableTestFilter && fep != null) {
            fep.addConfigurableFilter((ConfigurableTestFilter)f);
        }

        // tell people about the addition
        // obs should never be null
        for (int i = 0; i < obs.length; i++)
            obs[i].filterAdded(f);

        if (fep != null)
            fep.listModel.addElement(f);
    }

    /**
     * Remove a test filter from the list of possible filters.
     * You cannot remove the active filter.
     *
     * @param f Should not be null.  Should also be a filter which was previously
     *        added to this object.
     * @throws FilterInUseFault Thrown if you try to remove the active filter.
     */
    synchronized void remove(TestFilter f) {
        if (f == null)
            return;

        // remove this filter officially
        filters = (TestFilter[])DynamicArray.remove(filters, f);

        if (f instanceof ObservableTestFilter)
            ((ObservableTestFilter)f).removeObserver(listener);

        if (f instanceof ConfigurableTestFilter && fep != null) {
            fep.removeConfigurableFilter((ConfigurableTestFilter)f);
        }

        // tell people about the removal
        // obs should never be null
        for (int i = 0; i < obs.length; i++)
            obs[i].filterRemoved(f);

        if (fep != null)
            fep.listModel.removeElement(f);
    }

    synchronized String[] getFilterNames() {
        int count = filters.length;
        String[] names = new String[count];

        for (int i = 0; i < count; i++)
            names[i] = filters[i].getName();

        return names;
    }

    synchronized TestFilter[] getFilters() {
        int count = filters.length;
        TestFilter[] copy = new TestFilter[count];
        System.arraycopy(filters, 0, copy, 0, count);

        return copy;
    }

    /**
     * Get a filter by name.
     *
     * @return Null if no match.
     */
    synchronized TestFilter getFilter(String name) {
        int count = filters.length;

        for (int i = 0; i < count; i++)
            if (name.equals(filters[i].getName()))
                return filters[i];

        // not found
        return null;
    }

    /**
     * Does the view contain the given filter.
     * Done by reference compare.
     */
    synchronized boolean contains(TestFilter f) {
        for (int i = 0; i < filters.length; i++)
            if (filters[i] == f)
                return true;

        // not found
        return false;
    }

    synchronized FilterSelectionHandler createFilterSelectionHandler() {
        FilterSelectionHandler fsh = new FilterSelectionHandler(this, uif);
        handlers = (FilterSelectionHandler[])DynamicArray.append(handlers, fsh);

        return fsh;
    }

    public synchronized void addObserver(Observer o) {
        if (o == null)
            return;

        if (obs == null)
            obs = new Observer[0];

        obs = (Observer[])DynamicArray.append(obs, o);
    }

    public synchronized void removeObserver(Observer o) {
        obs = (Observer[])DynamicArray.remove(obs, o);
    }

    /**
     * Notify all observers that a filter has changed state.
     * This call has no effect if the given filter is not the active
     * view filter.
     */
    synchronized void notifyUpdated(TestFilter f) {
        if (obs == null)    // this really should not happen
            return;

        for (int i = 0; i < obs.length; i++) {
            obs[i].filterUpdated(f);
        }   // for
    }

    /**
     * Show the filter editor.
     */
    synchronized void showEditorDialog() {
        if (fep == null)
            fep = new FilterEditorPanel(parentComponent, uif);

        fep.setVisible(true);
    }

    /**
     * Show the filter editor, with the given filter selected.
     * @param f the filter to show the user initially
     */
    synchronized void showEditorDialog(TestFilter f) {
        if (fep == null)
            fep = new FilterEditorPanel(parentComponent, uif);

        fep.setSelectedFilter(f);
        fep.setVisible(true);
    }

    /**
     * Hide the editor dialog.  No effect if the dialog isn't visible or
     * initialized.
     */
    synchronized void hideEditorDialog(Frame parent) {
        fep.setVisible(false);
    }

    private class Listener
        extends ComponentAdapter
        implements ObservableTestFilter.Observer
    {
        public void filterUpdated(ObservableTestFilter otf) {
            notifyUpdated(otf);
        }

        // ComponentListener
        public void componentHidden(ComponentEvent e) {
            fep.doReset();
        }
    }

    // too much overhead to impl. all the methods if we use a Vector
    // we'll accept the hit when we add a new filter, which isn't that
    // often
    private TestFilter[] filters = new TestFilter[0];
    private UIFactory uif;
    private ExecModel execModel;
    private Listener listener = new Listener();
    private Observer[] obs = new Observer[0];
    private FilterSelectionHandler[] handlers;

    //private JDialog editDialog;
    private FilterEditorPanel fep;
    private JComponent parentComponent;

    private static boolean debug = Debug.getBoolean(FilterConfig.class);

    private class FilterEditorPanel
        extends ToolDialog
        implements ListSelectionListener, ActionListener
    {
        FilterEditorPanel(Component parent, UIFactory uif) {
            super(parent, uif, "fconfig");
        }

        // ActionListener
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            String result = null;

            if (source == doneBut) {

                if (mode == EDITABLE) {
                    //Debug.println("FC - in edit mode, attempting to save.");
                    result = doApply();
                }
                else {
                    //Debug.println("FC - not in edit mode, not attempting to save.");
                }

                if (result == null)
                    //OLD editDialog.hide();
                    setVisible(false);
                else {
                    // save failed...
                }
            }
            else if (source == applyBut) {
                result = doApply();
            }
            else if (source == resetBut) {
                if (mode == EDITABLE)
                    doReset();
            }
            else if (source == cancelBut) {
                if (mode == EDITABLE)
                    doReset();

                //OLD editDialog.hide();
                setVisible(false);
            }
            /*
            else if (source == createBut) {
                Object tf = (TestFilter)(leftList.getSelectedValue());
                if (!(tf instanceof ConfigurableTestFilter))
                    throw new IllegalStateException("");

                ConfigurableTestFilter ctf = (ConfigurableTestFilter)tf;

                FilterConfig.this.add(ctf.cloneInstance());
            }
            else if (source == deleteBut) {
                Object tf = (TestFilter)(leftList.getSelectedValue());
                if (!(tf instanceof ConfigurableTestFilter))
                    throw new IllegalStateException("");

                ConfigurableTestFilter ctf = (ConfigurableTestFilter)tf;

                FilterConfig.this.remove(ctf);
            }
            */

            if (result != null) {       // error occurred
                uif.showError("fconfig.commit", result);
            }
        }

        void setSelectedFilter(TestFilter f) {
            if (listModel != null) {
                int ind = listModel.indexOf(f);
                if (ind >= 0)
                    selectIndex(ind);
            }
            else {
                // necessary for correct initialization, since the ToolDialog
                // does lazy initialization
                // initGUI() will process this later
                selectedFilter = f;
            }
        }

        // ListSelectionListener
        public void valueChanged(ListSelectionEvent e) {
            //int newSelected = e.getLastIndex();
            int newSelected = leftList.getSelectedIndex();
            // list selection has changed
            if (newSelected < 0) {
                selectIndex(0);
                lastSelected = 0;
            }
            else if (newSelected != lastSelected) {
                selectIndex(newSelected);
                lastSelected = newSelected;
            }
        }

        /**
         * Process a list selection change.  It is assumed that the selection
         * does not equal the current/last selection.  This should always be
         * called on the event thread.
         */
        private void selectIndex(int index) {
            leftList.setSelectedIndex(index);

            // update right panel
            selectedFilter = (TestFilter)(listModel.elementAt(index));
            if (selectedFilter instanceof ConfigurableTestFilter) {
                if (mode == -1 || mode == UNEDITABLE) {
                    nameCards.show(namePanel, NAMING_ACTIVE);
                    //deleteBut.setEnabled(true);
                    //createBut.setEnabled(true);
                    applyBut.setEnabled(true);
                    resetBut.setEnabled(true);
                    mode = EDITABLE;
                }

                ConfigurableTestFilter ctf = (ConfigurableTestFilter)selectedFilter;
                namingName.setText(ctf.getName());

                String cardKey = (String)(configPanelHash.get(ctf));
                configCards.show(configPanel, cardKey);
            }
            else if (selectedFilter instanceof TestFilter) {
                if (mode == -1 || mode == EDITABLE) {
                    nameCards.show(namePanel, NAMING_EMPTY);
                    configCards.show(configPanel, CONFIG_EMPTY);
                    //createBut.setEnabled(false);
                    //deleteBut.setEnabled(false);
                    applyBut.setEnabled(false);
                    resetBut.setEnabled(false);
                    mode = UNEDITABLE;
                }
            }
            else {
                // hummm
            }

            fillInfo(selectedFilter);
            leftList.requestFocus();
        }

        protected void initGUI() {
            setHelp("execFilters.dialog.csh");

            TestSuite ts = execModel.getTestSuite();
            String tsName = ((ts == null ? uif.getI18NString("fconfig.dTitle.unknown") : ts.getName()));
            setI18NTitle("fconfig.dTitle", tsName);

            JPanel body = uif.createPanel("fe.body", false);
            body.setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.VERTICAL;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.insets.left = 10;
            gbc.insets.right = 10;
            gbc.weighty = 1;
            gbc.weightx = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;

            leftList = uif.createList("fconfig.list");
            leftList.setModel(createListModel());
            leftList.setCellRenderer(RenderingUtilities.createFilterListRenderer());
            leftList.addListSelectionListener(this);
            leftList.setBorder(BorderFactory.createEtchedBorder());
            leftList.getInsets().left = 5;
            leftList.getInsets().right = 5;
            uif.setAccessibleInfo(leftList, "fconfig.list");

            JPanel spacer = new JPanel();
            spacer.setBorder(uif.createTitledBorder("fconfig.list"));
            spacer.setLayout(new BorderLayout());
            spacer.add(leftList, BorderLayout.CENTER);

            body.add(spacer, gbc);

            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 1;
            JComponent rightPanel = createRightPanel();
            body.add(rightPanel, gbc);

            int dpi = uif.getDotsPerInch();
            body.setPreferredSize(new Dimension(7 * dpi, 5 * dpi));
            setBody(body);

            initButtons();

            setComponentListener(listener);

            //leftList.setNextFocusableComponent(rightPanel.getNextFocusableComponent());
            //rightPanel.setNextFocusableComponent(applyBut);
            //helpBut.setNextFocusableComponent(leftList);

            mode = -1;

            // process any stored state
            if (selectedFilter != null)
                setSelectedFilter(selectedFilter);
            else
                selectIndex(0);     // the default
        }

        private ListModel createListModel() {
            listModel = new DefaultListModel();

            for (int i = 0; i < filters.length; i++)
                listModel.addElement(filters[i]);

            return listModel;
        }

        private JComponent createRightPanel() {
            EMPTY_CONFIG = createEmptyItem("fconfig.empt.conf");
            EMPTY_NAMING = createEmptyItem("fconfig.empt.name");

            EMPTY_CONFIG.setName("config");
            EMPTY_NAMING.setName("naming");

            JPanel pan = new JPanel(new GridBagLayout());
            pan.setName("rightFilter");
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = GridBagConstraints.RELATIVE;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.NORTH;
            gbc.insets.left = 10;
            gbc.insets.right = 10;
            gbc.weighty = 0;
            gbc.weightx = 1;

            JComponent nextPanel = createInfoPanel();
            nextPanel.setBorder(uif.createTitledBorder("fconfig.panel.info"));
            pan.add(nextPanel, gbc);

            nextPanel = createNamingPanel();
            nextPanel.setBorder(uif.createTitledBorder("fconfig.panel.naming"));
            pan.add(nextPanel, gbc);

            nextPanel = createConfigPanel();
            nextPanel.setBorder(uif.createTitledBorder("fconfig.panel.config"));
            gbc.weighty = 1;
            pan.add(nextPanel, gbc);

            return pan;
        }

        /**
         * ui key should have uikey.txt, uikey.desc, and uikey values in
         * the bundle.  these are for the text itself, a11y description and
         * a11y name respectively.
         */
        private JComponent createEmptyItem(String uikey) {
            JTextField item = uif.createHeading(uikey);
            uif.setAccessibleInfo(item, uikey);
            item.setHorizontalAlignment(SwingConstants.CENTER);

            return item;
        }

        private JComponent createInfoPanel() {
            JPanel pan = new JPanel(new GridBagLayout());
            pan.setName("info");
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.weightx = 5;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.EAST;

            gbc.insets.left = 15;
            gbc.insets.right = 15;

            JLabel lab = uif.createLabel("fconfig.info.name");
            infoName = uif.createOutputField("fconfig.info.name", lab);
            infoName.setEditable(false);
            infoName.setBorder(BorderFactory.createEmptyBorder());
            lab.setDisplayedMnemonic(uif.getI18NString("fconfig.info.name.mne").charAt(0));
            lab.setHorizontalAlignment(SwingConstants.RIGHT);

            // row 1
            // label in first column
            pan.add(lab, gbc);

            // add text field in second column
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;
            pan.add(infoName, gbc);

            lab = uif.createLabel("fconfig.info.desc");
            lab.setHorizontalAlignment(SwingConstants.RIGHT);
            infoDesc = uif.createTextArea("fconfig.info.desc", lab);
            infoDesc.setRows(3);
            infoDesc.setEditable(false);
            infoDesc.setBorder(BorderFactory.createEmptyBorder());
            infoDesc.setLineWrap(true);
            infoDesc.setWrapStyleWord(true);
            infoDesc.setBackground(UIFactory.Colors.TRANSPARENT.getValue());
            infoDesc.setOpaque(false);
            lab.setDisplayedMnemonic(uif.getI18NString("fconfig.info.desc.mne").charAt(0));

            // row 2
            // label in first column
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.NORTHEAST;
            pan.add(lab, gbc);

            // add text area in second column
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;
            pan.add(infoDesc, gbc);

            /*
            // NOTE intentional space added below to defeat
            //  i18n check in build while this is commented out
            lab = uif.createLabel ("fconfig.info.reason");
            lab.setHorizontalAlignment(SwingConstants.RIGHT);
            infoReason = uif.createTextArea("fconfig.info.reason");
            infoReason.setRows(2);
            infoReason.setEditable(false);
            infoReason.setBorder(BorderFactory.createEmptyBorder());
            infoReason.setLineWrap(true);
            infoReason.setWrapStyleWord(true);
            infoReason.setOpaque(false);
            lab.setLabelFor(infoReason);

            // row 3
            // label in first column
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.NORTHEAST;
            pan.add(lab, gbc);

            // add text area in second column
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;
            pan.add(infoReason, gbc);
            */
            return pan;
        }

        private JPanel createNamingPanel() {
            nameCards = new CardLayout();
            namePanel = new JPanel(nameCards);

            JPanel pan = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.weightx = 5;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.EAST;

            gbc.insets.left = 15;
            gbc.insets.right = 15;

            JLabel lab = uif.createLabel("fconfig.naming.name");
            namingName = uif.createOutputField("fconfig.naming.name", lab);
            namingName.setEditable(true);
            namingName.setEnabled(true);
            lab.setDisplayedMnemonic(uif.getI18NString("fconfig.naming.name.mne").charAt(0));
            lab.setHorizontalAlignment(SwingConstants.RIGHT);

            // row 1
            // label in first column
            pan.add(lab, gbc);

            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            pan.add(namingName, gbc);

            namePanel.add(NAMING_ACTIVE, pan);
            namePanel.add(NAMING_EMPTY, EMPTY_NAMING);
            nameCards.show(namePanel, NAMING_EMPTY);

            return namePanel;
        }

        private JPanel createConfigPanel() {
            configCards = new CardLayout();
            configPanel = new JPanel(configCards);
            configPanelHash = new Hashtable();

            // insert panels?
            configPanel.add(CONFIG_EMPTY, EMPTY_CONFIG);

            for (int i = 0; i < listModel.getSize(); i++) {
                if (listModel.getElementAt(i) instanceof ConfigurableTestFilter) {
                    ConfigurableTestFilter ctf =
                        (ConfigurableTestFilter)listModel.getElementAt(i);
                    addConfigurableFilter(ctf);
                }
            }   // for

            return configPanel;
        }

        private void initButtons() {
            applyBut = uif.createButton("fconfig.edit.apply", this);
            resetBut = uif.createButton("fconfig.edit.reset", this);
            cancelBut = uif.createCancelButton("fconfig.edit.cancel", this);
            doneBut = uif.createButton("fconfig.edit.done", this);
            helpBut = uif.createHelpButton("fconfig.edit.help", "execFilters.dialog.csh");
            setButtons(new JButton[] { applyBut, resetBut, cancelBut, doneBut, helpBut },
                       doneBut);
        }

        private void addConfigurableFilter(ConfigurableTestFilter ctf) {
            JComponent comp = ctf.getEditorPane();
            String thisName = "ctf" + ++configCounter;

            if (comp != null)
                configPanel.add(thisName, comp);
            else
                configPanel.add(thisName, EMPTY_CONFIG);

            // to lookup later
            configPanelHash.put(ctf, thisName);
        }

        private void removeConfigurableFilter(ConfigurableTestFilter ctf) {
            configPanelHash.remove(ctf);
        }

        private void fillInfo(TestFilter f) {
            if (f == null)
                return;

            if (f instanceof ConfigurableTestFilter) {
                infoName.setText(((ConfigurableTestFilter)(f)).getBaseName());
            }
            else
                infoName.setText(f.getName());

            infoDesc.setText(f.getDescription());
            //infoReason.setText(f.getReason());
        }

        private void fillNaming(ConfigurableTestFilter f) {
            namingName.setText(f.getName());
        }

        /**
         * Attempt the apply operation.
         * @return Non-null if the apply was successful, null otherwise.  If non-null
         *         the value is a localized error message for the user.
         */
        private String doApply() {
            if (mode == UNEDITABLE)
                throw new IllegalStateException("filter is uneditable, cannot apply changes");

            ConfigurableTestFilter ctf = (ConfigurableTestFilter)selectedFilter;

            // should check to see if it's changed
            String newName = namingName.getText();
            if (validateName(newName) != -1) {
                return uif.getI18NString("fconfig.edit.badName", newName);
            }
            else if (newName != null)
                ctf.setInstanceName(newName);

            String status = ctf.commitEditorSettings();

            if (handlers != null)
                for (int i = 0; i < handlers.length; i++)
                    handlers[i].updateFilterMetaInfo(ctf);

            return status;
        }

        void doReset() {
            // reset all known configurable filters
            for (int i = 0; i < listModel.getSize(); i++) {
                if (listModel.elementAt(i) instanceof
                    ConfigurableTestFilter) {
                    ConfigurableTestFilter ctf =
                        (ConfigurableTestFilter)listModel.elementAt(i);
                    ctf.resetEditorSettings();
                }
            }   // for
        }

        /**
         * @return The position of the invalid character.
         */
        private int validateName(String text) {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (Character.isLetterOrDigit(c) ||
                    c == '-' ||
                    c == '_' ||
                    c == '.' ||
                    c == ' ' ||
                    c == ',')
                    continue;
                else
                    return i;
            }   // for

            return -1;
        }

        private JSplitPane split;
        private JList leftList;
        private DefaultListModel listModel;
        private int lastSelected = -1;
        private TestFilter selectedFilter;

        // just to help track current state
        private int mode;
        //private boolean focusSet;
        private static final int EDITABLE = 0;
        private static final int UNEDITABLE = 1;

        // dialog buttons
        private JButton applyBut;
        private JButton helpBut;
        private JButton doneBut;
        private JButton cancelBut;
        private JButton createBut;
        private JButton deleteBut;
        private JButton resetBut;

        // info panel components
        private JTextArea infoDesc;
        private JTextField infoName;
        //private JTextArea infoReason;

        // naming panel
        private CardLayout nameCards;
        private JPanel namePanel;
        private JTextField namingName;

        // config panel
        private CardLayout configCards;
        private JPanel configPanel;
        private Hashtable configPanelHash;
        private int configCounter;          // to make a unique string

        private JComponent EMPTY_CONFIG;
        private JComponent EMPTY_NAMING;
        private JComponent EMPTY_INFO;

        private int NUMBER;

        private static final String CONFIG_ACTIVE = "configa";
        private static final String CONFIG_EMPTY = "confige";
        private static final String NAMING_ACTIVE = "naminga";
        private static final String NAMING_EMPTY = "naminge";
        private static final String INFO_ACTIVE = "infoa";
        private static final String INFO_EMPTY = "infoe";
    }

}
