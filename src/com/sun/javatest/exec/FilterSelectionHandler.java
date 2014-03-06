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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;

import com.sun.javatest.TestFilter;
import com.sun.javatest.tool.ToolAction;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.util.Debug;
import com.sun.javatest.util.DynamicArray;
import com.sun.javatest.util.OrderedTwoWayTable;
import java.util.Arrays;

class FilterSelectionHandler {
     /**
      * Observe changes to the state of the view.  The changes to the view state
      * will generally be the result of user actions.
      */
    public interface Observer {
       /**
        * The state of the given filter has changed.
        */
        public void filterUpdated(TestFilter f);

       /**
        * The system is requesting a different filter.
        */
        public void filterSelected(TestFilter f);

        public void filterAdded(TestFilter f);

       /**
        * Removing the active filter will result in an exception.
        */
        public void filterRemoved(TestFilter f);
    }

    FilterSelectionHandler(FilterConfig fc, UIFactory uif) {
        this.filterConfig = fc;
        this.uif = uif;

        listener = new Listener();

        filterConfig.addObserver(listener);
    }

    public void addObserver(Observer o) {
        if (o == null)
            return;

        if (obs == null)
            obs = new Observer[0];

        obs = (Observer[])DynamicArray.append(obs, o);
    }

    public void removeObserver(Observer o) {
        obs = (Observer[])DynamicArray.remove(obs, o);
    }

    /**
     * Get the gui component that allows the user to select and configure
     * the active filter.  This should only be called once for each instance.
     * Only the last instance of the selector widget will be used.
     */
    JComponent getFilterSelector() {
        // may need to start offering the caller a way to control layout
        // fill and anchor settings...
        JPanel panel = uif.createPanel("fconfig.sp", new GridBagLayout(), false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        selectBox = uif.createLiteralChoice("fconfig.box", filterConfig.getFilters());
        selectBox.addActionListener(listener);
        selectBox.setRenderer(RenderingUtilities.createFilterListRenderer());
        uif.setAccessibleName(selectBox, "fconfig.box");
        panel.add(selectBox, gbc);

        gbc.gridx = 1;

        configButton = uif.createButton("fconfig.config");
        // make button smaller, with minimal border
        configButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEtchedBorder(),
            BorderFactory.createEmptyBorder(0,3,0,3)));
        configButton.addActionListener(listener);
        configButton.setMaximumSize(configButton.getPreferredSize());
        uif.setAccessibleName(configButton, "fconfig.config");
        gbc.insets.left = 11;
        gbc.weightx = 0.0;
        panel.add(configButton, gbc);
        panel.setMaximumSize(panel.getPreferredSize());

        // make sure a filter is selected
        if (activeFilter == null) {
            TestFilter[] filters = filterConfig.getFilters();
            if (filters != null && filters.length > 0)
                setFilter(filters[0]);
        }
        else {
            selectBox.setSelectedItem(activeFilter);
        }

        configButton.setEnabled((activeFilter instanceof ConfigurableTestFilter));

        return panel;
    }

    /**
     * Get the filtering submenu.
     */
    synchronized JMenu getFilterMenu() {
        menuGroup = new ButtonGroup();
        filterMenuTable = new OrderedTwoWayTable();

        Action showEditorAction = new ToolAction(uif, "fconfig.submenu.edit") {
            public void actionPerformed(ActionEvent e) {
                filterConfig.showEditorDialog(getActiveFilter());
            }
        };

        editMenu = uif.createMenu("fconfig.submenu");

        TestFilter[] filters = filterConfig.getFilters();
        for (int i = 0; i < filters.length; i++)
            addToMenu(filters[i], -1);

        // make sure a filter is selected
        // addToMenu() also tries to do this
        if (activeFilter == null) {
            if (filters != null && filters.length > 0)
                setFilter(filters[0]);
        }

        // radio set of filter names
        editMenu.addSeparator();
        editMenu.add(uif.createMenuItem(showEditorAction));
        updateMenu();

        return editMenu;

    }

    /**
     * Set the active filter.
     * If the name cannot be matched, the request will be ignored.
     *
     * @param name Should never be null.
     */
    synchronized void setFilter(String name) {
        setFilter(filterConfig.getFilter(name));
    }

    /**
     * Set the active filter.
     * If the filter supplied is part of the known set of filters, it
     * becomes the active filter and all observers are notified.
     *
     * @param f A null filter will be ignored.
     */
    synchronized void setFilter(TestFilter f) {
        if (f == null)
            return;
        if (filterConfig.contains(f)) {
            activeFilter = f;

            if (selectBox != null)
                selectBox.setSelectedItem(activeFilter);

            if (configButton != null)
                configButton.setEnabled((f instanceof ConfigurableTestFilter));

            updateMenu();

            for (int i = 0; i < obs.length; i++)
                obs[i].filterSelected(f);
        }
    }

    /**
     * Return the filter currently selected.
     *
     * @return Null if no filter is active.
     */
    synchronized TestFilter getActiveFilter() {
        return activeFilter;
    }

    /**
     * Hint to update any meta info about the filter if necessary.
     */
    void updateFilterMetaInfo(TestFilter f) {
        // update the drop down box if needed
        if (activeFilter == f && selectBox != null)
            selectBox.repaint();

        // update menu item if needed
        // could check editMenu, filterMenuTable or menuGroup
        if (editMenu != null) {
            int index = filterMenuTable.getKeyIndex(f);

            if (index >= 0) {
                JMenuItem jmi = (JMenuItem)(filterMenuTable.getValueAt(index));
        int mne = jmi.getMnemonic();
        if (mne > 0)
            jmi.setText((char)mne + " " + f.getName());
        else
            jmi.setText(f.getName());
        }
    }
    }

    /**
     * Add the filter to the list of filters in the menu.
     * No action is taken if the menu system is not initilized.  No
     * checking for duplicate insertions occurs.
     * @param location Position to place the new item at.  -1 indicates
     *        indifference.
     * @param f The test filter to add.
     */
    private synchronized void addToMenu(TestFilter f, int location) {
        if (editMenu == null)
            return;

    boolean[] mnemonics = new boolean[10];
    Arrays.fill(mnemonics, false);

        // guess a mnemonic
        for (int i = 0; i < editMenu.getItemCount(); i++) {
            JMenuItem mi = editMenu.getItem(i);
            if (mi != null) {
                int itemMne = mi.getMnemonic() - '0' - 1;
                if (itemMne == -1) itemMne = 9;
                if (itemMne >= 0 && itemMne <= 9)
                    mnemonics[itemMne] = true;
            }
        }

    int mne = -1;
    for (int i = 0; i < mnemonics.length; i++) {
        if (mnemonics[i] == false) {
            mne = i;
            break;
        }
    }

    if (mne == 9) {
        mne = '0';
    } else if (mne >= 0) {
        mne = mne + 1 + '0';
    }

        String text = (mne == 0 ? "" : (char) mne) + " " + f.getName();
        JRadioButtonMenuItem b = new JRadioButtonMenuItem(text);
        b.setName(f.getName());
        if (mne != 0)
            b.setMnemonic(mne);
        b.getAccessibleContext().setAccessibleDescription(f.getDescription());
        b.addActionListener(listener);
        menuGroup.add(b);

        filterMenuTable.put(f, b);

        if (location < 0)       // no preference, insert as last filter
            editMenu.insert(b, menuGroup.getButtonCount()-1);
        else
            editMenu.insert(b, location);

        if (f == activeFilter)
            b.setEnabled(true);
    }

    /**
     * This should never be used on an active filter.
     * No action is taken if the menu system is not initilized.
     */
    private synchronized void removeFromMenu(TestFilter f) {
        if (editMenu == null)
            return;

        int where = filterMenuTable.getKeyIndex(f);
        if (where != -1) {      // found, continue
            JRadioButtonMenuItem mi = (JRadioButtonMenuItem)(filterMenuTable.getValueAt(where));
            editMenu.remove(mi);
            filterMenuTable.remove(where);
            menuGroup.remove(mi);
        }
    }

    /**
     * Currently just selects the correct active filter.
     */
    private void updateMenu() {
        if (editMenu == null)
            return;

        // select the right item in the menu
        int where = filterMenuTable.getKeyIndex(activeFilter);
        if (where != -1) {
            JRadioButtonMenuItem mi = (JRadioButtonMenuItem)(filterMenuTable.getValueAt(where));
            mi.setSelected(true);
        }

    }

    private FilterConfig filterConfig;
    private UIFactory uif;

    private TestFilter activeFilter;
    private JComboBox selectBox;
    private JButton configButton;
    private JMenu editMenu;
    private ButtonGroup menuGroup;

    private Listener listener;
    private Observer[] obs = new Observer[0];
    private OrderedTwoWayTable filterMenuTable;     // filter, menu item
    private static boolean debug = Debug.getBoolean(FilterConfig.class);

    class Listener implements ActionListener, FilterConfig.Observer {
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source == selectBox) {
                String action = e.getActionCommand();
                TestFilter vf = (TestFilter)(selectBox.getSelectedItem());

                if (vf == getActiveFilter()) {
                    if (debug)
                        Debug.println("FC - keeping filter");

                    return;
                }
                else {
                    if (debug)
                        Debug.println("FC - changing filter");

                    setFilter(vf);
                }
            }
            else if (source == configButton) {
                TestFilter vf = (TestFilter)(selectBox.getSelectedItem());
                filterConfig.showEditorDialog(vf);
            }
            else if (source instanceof JRadioButtonMenuItem) {
                // a filter menu item
                int which = filterMenuTable.getValueIndex(source);
                if (which != -1) {
                    TestFilter f = (TestFilter)(filterMenuTable.getKeyAt(which));
                    setFilter(f);
                }
            }
        }

        public void filterUpdated(TestFilter f) {
            if (obs == null)    // this really should not happen
                return;

            if (activeFilter == f) {
                for (int i = 0; i < obs.length; i++)
                    obs[i].filterUpdated(f);
            }
        }

        public void filterAdded(TestFilter f) {
            // add to the list box
            if (selectBox != null)
                selectBox.addItem(f);

            // update the menu
            addToMenu(f, -1);

            // tell people about the addition
            // obs should never be null
            for (int i = 0; i < obs.length; i++)
                obs[i].filterAdded(f);
        }

       /**
        * Removing the active filter will result in an exception.
        */
        public void filterRemoved(TestFilter f) {
            // remove from list box
            if (selectBox != null)
                selectBox.removeItem(f);

            // update the menu
            removeFromMenu(f);

            // tell people about the removal
            // obs should never be null
            for (int i = 0; i < obs.length; i++)
                obs[i].filterRemoved(f);
        }
    }
}

