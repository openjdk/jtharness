/*
 * $Id$
 *
 * Copyright (c) 1996, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.tool;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Array;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A component that displays an editable list of items.
 */
public class EditableList extends JComponent implements Accessible
{
    /**
     * Create an empty component, using a standard UIFactory for this class,
     * and using resources beginning with "list.".
     */
    public EditableList() {
        this(new UIFactory(EditableList.class, null), "list");
    }

    private static UIFactory getDefaultUIF() {
        // use EditableList.class instead of "this" to get correct i18n
        if (defaultUIF == null)
            defaultUIF = new UIFactory(EditableList.class, null);  // no help required
        return defaultUIF;
    }

    /**
     * Create an empty component, using a specified UIFactory and resource prefix.
     * @param uif The UIFactory used to construct the component
     * @param uiKey The prefix for any UI resources that may be required
     */
    public EditableList(UIFactory uif, String uiKey) {
        this.uif = uif;
        setLayout(new BorderLayout());
        listModel = new DefaultListModel(); // need to force the type of model
        listModel.addListDataListener(listener);
        list = uif.createList(uiKey, listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(renderer);

        list.addListSelectionListener(listener);
        add(new JScrollPane(list), BorderLayout.CENTER);

        JToolBar bar = new JToolBar(JToolBar.VERTICAL);
        bar.setFloatable(false);
        bar.add(addBtn  = createButton(uiKey + ".add"));
        bar.add(removeBtn = createButton(uiKey + ".remove"));
        bar.add(upBtn = createButton(uiKey + ".up"));
        bar.add(downBtn = createButton(uiKey + ".down"));
        add(bar, BorderLayout.EAST);

        updateButtons();
        setBorder(BorderFactory.createEtchedBorder());
    }

    /**
     * Get the accessible context for this pane.
     * @return the accessible context for this pane
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null)
            accessibleContext = new AccessibleJComponent() { };
        return accessibleContext;
    }

    /**
     * Set whether or not the list can be edited by the user.
     * @param b if true, the component can be edited by the user;
     *   if false, it cannot
     */
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        list.setEnabled(b);
        updateButtons();
    }

    /**
     * Set the items in the list. Any previous items are removed first.
     * @param items the array of items to be put in the list.
     * @see #getItems
     */
    public void setItems(Object[] items) {
        listModel.clear();
        if (items != null) {
            for (int i = 0; i < items.length; i++)
                listModel.addElement(items[i]);
        }
    }

    /**
     * Remove all entries from the list.
     */
    public void clear() {
        listModel.clear();
    }

    /**
     * Get the items currently in the list.
     * @return an array containing the items currently in the list
     * @see #setItems
     */
    public Object[] getItems() {
        return listModel.toArray();
    }


    /**
     * Get the items currently in the list, in an array of a specific type.
     * @param c the component type of the array to be returned
     * @return an array containing the items currently in the list
     * @see #setItems
     */
    public Object[] getItems(Class c) {
        Object[] items = (Object[]) (Array.newInstance(c, listModel.size()));
        listModel.copyInto(items);
        return items;
    }

    /**
     * Get the tool tip text that appears on the list.
     * (Separate tool tip text will appear on the buttons to manipulate the list.)
     * @return the tool tip text that appears on the list
     * @see #setToolTipText
     */
    public String getToolTipText() {
        return list.getToolTipText();
    }


    /**
     * Set the tool tip text that appears on the list.
     * (Separate tool tip text will appear on the buttons to manipulate the list.)
     * @param tip the tool tip text to appear on the list
     * @see #getToolTipText
     */
    public void setToolTipText(String tip) {
        list.setToolTipText(tip);
    }

    /**
     * Add a listener to be notified of events when the list data changes.
     * @param l the listener to be notified
     * @see #removeListDataListener
     */
    public void addListDataListener(ListDataListener l) {
        listenerList.add(ListDataListener.class, l);
    }

    /**
     * Remove a listener that was previously added to be notified of
     * events when the list data changes.
     * @param l the listener to be notified
     * @see #addListDataListener
     */
    public void removeListDataListener(ListDataListener l) {
        listenerList.remove(ListDataListener.class, l);
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

    /**
     * Get the display value for an item in the list. By default, the display
     * value is the item itself.
     * @param item the object for which to get the display value
     * @return the display value for the specified item
     */
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
        return JOptionPane.showInputDialog(this, uif.getI18NString("list.add.txt"));
    }

    /**
     * Invoked to get a new item to replace an existing item in the list.
     * The default is to show an input dialog to allow
     * the user to type in a new string. Subtypes may override this method
     * to provide other ways of specifying items to be added, such as a
     * file chooser.
     * @param oldItem the item to be replaced in the list
     * @return an object to replace the old item the list, or null if no
     * replacement should occur.
     */
    protected Object getNewItem(Object oldItem) {
        return JOptionPane.showInputDialog(this, uif.getI18NString("list.change.txt"), oldItem);
    }

    private JButton createButton(String uiKey) {
        JButton b = uif.createButton(uiKey);
        // set max size so button can grow within toolbar
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        b.addActionListener(listener);
        // should be in uif.createButton?
        b.setMnemonic(uif.getI18NString(uiKey+ ".mne").charAt(0));
        return b;
    }

    private void insertItem() {
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

    private void removeSelectedItem() {
        if (!list.isSelectionEmpty())
            listModel.remove(list.getSelectedIndex());
    }

    private void moveSelectedItemUp() {
        if (!list.isSelectionEmpty()) {
            int i = list.getSelectedIndex();
            if (i > 0) {
                swap(i, i - 1);
                list.setSelectedIndex(i - 1);
            }
        }
    }

    private void moveSelectedItemDown() {
        if (!list.isSelectionEmpty()) {
            int i = list.getSelectedIndex();
            if (i + 1 < listModel.size()) {
                swap(i, i + 1);
                list.setSelectedIndex(i + 1);
            }
        }
    }
    private void editItem(int index) {
        Object newItem = getNewItem(listModel.getElementAt(index));

        if (!duplicatesAllowed && listModel.contains(newItem)) {
            showDuplicateError(newItem);
            return;
        }

        if (newItem != null)
            listModel.set(index, newItem);
    }

    private void showDuplicateError(Object item) {
        String text = uif.getI18NString("list.duplicate.text",
                                        new Object[] { getDisplayValue(item) });

        String title = uif.getI18NString("list.duplicate.title");

        JOptionPane.showMessageDialog(this,
                                      text,
                                      title,
                                      JOptionPane.INFORMATION_MESSAGE);
    }

    private class Renderer
        extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return super.getListCellRendererComponent(list,
                                                      getDisplayValue(value),
                                                      index,
                                                      isSelected,
                                                      cellHasFocus);
        }
    }

    private class Listener
        implements ActionListener, ListDataListener, ListSelectionListener, MouseListener
    {
        // ActionListener events, for buttons
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src == addBtn)
                insertItem();
            else if (src == removeBtn)
                removeSelectedItem();
            else if (src == upBtn)
                moveSelectedItemUp();
            else if (src == downBtn)
                moveSelectedItemDown();

            updateButtons();
        }

        // ListSelect events, to update buttons depending on list selection
        public void valueChanged(ListSelectionEvent e) {
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

        // ListData events, to redispatch to client, with EditableList.this as the source
        public void contentsChanged(ListDataEvent e) {
            ListDataEvent e2 = null;
            Object[] listeners = listenerList.getListenerList();
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == ListDataListener.class) {
                    if (e2 == null)
                        e2 = new ListDataEvent(EditableList.this, e.getType(), e.getIndex0(), e.getIndex1());
                    ((ListDataListener)listeners[i+1]).contentsChanged(e2);
                }
            }
        }

        public void intervalAdded(ListDataEvent e) {
            ListDataEvent e2 = null;
            Object[] listeners = listenerList.getListenerList();
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == ListDataListener.class) {
                    if (e2 == null)
                        e2 = new ListDataEvent(EditableList.this, e.getType(), e.getIndex0(), e.getIndex1());
                    ((ListDataListener)listeners[i+1]).intervalAdded(e2);
                }
            }
        }

        public void intervalRemoved(ListDataEvent e) {
            ListDataEvent e2 = null;
            Object[] listeners = listenerList.getListenerList();
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == ListDataListener.class) {
                    if (e2 == null)
                        e2 = new ListDataEvent(EditableList.this, e.getType(), e.getIndex0(), e.getIndex1());
                    ((ListDataListener)listeners[i+1]).intervalRemoved(e2);
                }
            }
        }
    }

    private void updateButtons() {
        boolean enabled = isEnabled();
        addBtn.setEnabled(enabled);
        if (list.isSelectionEmpty() || !enabled) {
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

    /**
     * The factory used to create the GUI elements of the component.
     */
    protected final UIFactory uif;

    // only initialize this if required
    private static UIFactory defaultUIF;

    /**
     * The list model that contains the elements of the list.
     */
    protected DefaultListModel listModel;
    private JList list;
    private JButton addBtn;
    private JButton removeBtn;
    private JButton upBtn;
    private JButton downBtn;
    private Listener listener = new Listener();
    private Renderer renderer = new Renderer();

    private boolean duplicatesAllowed;
}
