/*
 * $Id$
 *
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.interview.wizard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Array;
import java.text.MessageFormat;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A component that displays an editable list of values.
 */
public class EditableList extends JPanel
{
    /**
     * Create an EditableList.
     * @param uiKey A string used as the base of a key to look up resource values
     * for this item.
     * @param items An array of strings to display as initial values in the list.
     */
    public EditableList(String uiKey, Object[] items) {
        setLayout(new BorderLayout());
        setName(uiKey);
        setFocusable(false);

        listModel = new DefaultListModel(); // need to force the type of model
        if (items != null) {
            for (int i = 0; i < items.length; i++)
                listModel.addElement(items[i]);
        }
        list = new JList(listModel);
        list.setName(uiKey);
        list.setToolTipText(i18n.getString(uiKey + ".tip"));
        list.getAccessibleContext().setAccessibleName(list.getName());
        list.getAccessibleContext().setAccessibleDescription(list.getToolTipText());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(renderer);
        list.addListSelectionListener(listener);
        list.addMouseListener(listener);
        list.setVisibleRowCount(5);

        JScrollPane sp = new JScrollPane(list);
        sp.setName(uiKey + ".sp");
        sp.setFocusable(false);
        add(sp, BorderLayout.CENTER);

        JToolBar bar = new JToolBar(JToolBar.VERTICAL);
        bar.setName(uiKey + "bar");
        bar.setFocusable(false);
        bar.setFloatable(false);
        bar.add(addBtn  = createButton("elst.add"));
        bar.add(removeBtn = createButton("elst.remove"));
        bar.add(upBtn = createButton("elst.up"));
        bar.add(downBtn = createButton("elst.down"));
        add(bar, BorderLayout.EAST);
        updateButtons();
        setBorder(BorderFactory.createEtchedBorder());
    }

    public int getItemCount() {
        return listModel.getSize();
    }

    public Object getItem(int index) {
        return listModel.elementAt(index);
    }

    /**
     * Get the set of items in the list.
     * @return the set of items currently in the list
     */
    public Object[] getItems() {
        return listModel.toArray();
    }

    /**
     * Get the items currently in the list, in an array of a specific type.
     * @param c the component type of the array to be returned
     * @return an array containing the items currently in the list
     */
    public Object[] getItems(Class c) {
        Object[] items = (Object[]) (Array.newInstance(c, listModel.size()));
        listModel.copyInto(items);
        return items;
    }

    public int getSelectedIndex() {
        return list.getSelectedIndex();
    }

    public Object getSelectedItem() {
        return list.getSelectedValue();
    }

    public void setSelectedItem(Object item) {
        list.setSelectedValue(item, true);
    }

    public void addListDataListener(ListDataListener l) {
        listModel.addListDataListener(l);
    }

    public void removeListDataListener(ListDataListener l) {
        listModel.removeListDataListener(l);
    }

    /**
     * Specify whether or not duplicates should be allowed in the list.
     * @param b true if duplicates should be allowed, and false otherwise
     * @see #isDuplicatesAllowed
     */
    public void setDuplicatesAllowed(boolean b) {
        duplicatesAllowed = b;
    }

    /**
     * Check whether or not duplicates should be allowed in the list.
     * @return true if duplicates should be allowed, and false otherwise
     * @see #setDuplicatesAllowed
     */
    public boolean isDuplicatesAllowed() {
        return duplicatesAllowed;
    }

    protected Object getDisplayValue(Object item) {
        return item;
    }

    /**
     * Invoked to get a new item to put in the list, when the user clicks
     * the "Add" button". The default is to show an input dialog to allow
     * the user to type in a new string. Subtypes may override this method
     * to provide other ways of specifying items to be added, such as a
     * file chooser.
     * @return an object to be added to the list, or null if no object
     * to be added.
     */
    protected Object getNewItem() {
        return JOptionPane.showInputDialog(this, i18n.getString("elst.add.title"));
    }

    /**
     * Invoked to get a new item to replace an existing item in the list.
     * The default is to show an input dialog to allow
     * the user to type in a new string. Subtypes may override this method
     * to provide other ways of specifying items to be added, such as a
     * file chooser.
     * @param the item to be replaced in the list
     * @return an object to replace the old item the list, or null if no
     * replacement should occur.
     */
    protected Object getNewItem(Object oldItem) {
        return JOptionPane.showInputDialog(this, i18n.getString("elst.change.title"), oldItem);
    }

    protected void itemsChanged() {
    }

    protected void selectedItemChanged() {
    }

    protected void insertItem() {
        Object newItem = getNewItem();

        if (!duplicatesAllowed && listModel.contains(newItem)) {
            showDuplicateError(newItem);
            return;
        }

        if (newItem != null) {
            if (list.isSelectionEmpty())
                listModel.addElement(newItem);
            else
                listModel.add(1 + list.getSelectedIndex(), newItem);
            list.setSelectedValue(newItem, true);
        }
    }

    protected void removeSelectedItem() {
        if (!list.isSelectionEmpty())
            listModel.remove(list.getSelectedIndex());
    }

    protected void moveSelectedItemUp() {
        if (!list.isSelectionEmpty()) {
            int i = list.getSelectedIndex();
            if (i > 0) {
                swap(i, i - 1);
                list.setSelectedIndex(i - 1);
            }
        }
    }

    protected void moveSelectedItemDown() {
        if (!list.isSelectionEmpty()) {
            int i = list.getSelectedIndex();
            if (i + 1 < listModel.size()) {
                swap(i, i + 1);
                list.setSelectedIndex(i + 1);
            }
        }
    }

    protected void editItem(int index) {
        Object newItem = getNewItem(listModel.getElementAt(index));

        if (!duplicatesAllowed && listModel.contains(newItem)) {
            showDuplicateError(newItem);
            return;
        }

        if (newItem != null)
            listModel.set(index, newItem);
    }


    protected JButton createButton(String uiKey) {
        JButton b = new JButton(i18n.getString(uiKey + ".btn"));
        b.setName(uiKey);
        b.setToolTipText(i18n.getString(uiKey + ".tip"));
        b.setMnemonic(i18n.getString(uiKey + ".mne").charAt(0));
        // set max size so button can grow within toolbar
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        b.addActionListener(listener);
        return b;
    }

    private void showDuplicateError(Object item) {
        String text = MessageFormat.format(i18n.getString("elst.duplicate.text"),
                                           new Object[] { getDisplayValue(item) });

        String title = i18n.getString("elst.duplicate.title");

        JOptionPane.showMessageDialog(this,
                                      text,
                                      title,
                                      JOptionPane.INFORMATION_MESSAGE);
    }

    protected class Renderer
        extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return super.getListCellRendererComponent(list,
                                                      getDisplayValue(value),
                                                      index,
                                                      isSelected,
                                                      cellHasFocus);
        }
    }

    protected class Listener
        implements ActionListener, ListSelectionListener, MouseListener {
        // ActionListener, for add, remove, up, down buttons
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src == addBtn) {
                insertItem();
            }
            else if (src == removeBtn) {
                removeSelectedItem();
            }
            else if (src == upBtn) {
                moveSelectedItemUp();
            }
            else if (src == downBtn) {
                moveSelectedItemDown();
            }
            itemsChanged();
            updateButtons();
        }

        // ListSelectionListener, to track list selection changes
        public void valueChanged(ListSelectionEvent e) {
            selectedItemChanged();
            updateButtons();
        }

        // MouseListener, to react to double click in list
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                int index = list.locationToIndex(e.getPoint());
                if(index != -1)
                    editItem(index);
            }
        }

        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) { }
        public void mousePressed(MouseEvent e) { }
        public void mouseReleased(MouseEvent e) { }
    }

    protected void updateButtons() {
        if (list.isSelectionEmpty()) {
            removeBtn.setEnabled(false);
            upBtn.setEnabled(false);
            downBtn.setEnabled(false);
        }
        else {
            removeBtn.setEnabled(true);
            int i = list.getSelectedIndex();
            upBtn.setEnabled(i > 0);
            downBtn.setEnabled((i + 1 < listModel.size()));
        }
    }


    private void swap(int i1, int i2) {
        Object o1 = listModel.elementAt(i1);
        Object o2 = listModel.elementAt(i2);
        listModel.set(i1, o2);
        listModel.set(i2, o1);
    }

    protected Listener createListener() {
        return new Listener();
    }

    protected Renderer createRenderer() {
        return new Renderer();
    }

    protected boolean duplicatesAllowed;
    protected Listener listener = createListener();
    protected Renderer renderer = createRenderer();
    protected DefaultListModel listModel;
    protected JList list;
    protected JButton addBtn;
    protected JButton removeBtn;
    protected JButton upBtn;
    protected JButton downBtn;

    private static final I18NResourceBundle i18n = I18NResourceBundle.getDefaultBundle();
}
