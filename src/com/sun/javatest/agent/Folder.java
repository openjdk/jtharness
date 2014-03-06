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
package com.sun.javatest.agent;

import java.awt.AWTEventMulticaster;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.ItemSelectable;
import java.awt.Panel;
import java.awt.Polygon;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

/**
 * Folder is a panel with a layout based on CardLayout that also provides GUI controls
 * to allow a user-selected component to be displayed.
 */

class Folder extends Panel implements ItemSelectable
{
    private class Entry
    {
        Entry(Component comp, String name, Color color, boolean visibleTab) {
            this.comp = comp;
            this.name = name;
            this.color = color;
            this.visibleTab = visibleTab;
        }

        Component comp;
        String name;
        Color color;
        boolean visibleTab;
    }

    private class Layout extends CardLayout
    {
        public Dimension minimumLayoutSize(Container parent) {
            Dimension size = super.minimumLayoutSize(parent);
            int width = Math.max(size.width, ((Folder)parent).getMinimumWidth());
            return new Dimension(width, size.height);
        }

        public Dimension preferredLayoutSize(Container parent) {
            Dimension size = super.preferredLayoutSize(parent);
            int width = Math.max(size.width, ((Folder)parent).getMinimumWidth());
            return new Dimension(width, size.height);
        }
    }

    public Folder() {
        this(0, 0);
    }

    public Folder(int hgap, int vgap) {
        this(hgap, vgap, 10, 5, 5);
    }

    Folder(int hgap, int vgap, int border, int slant, int tabSpace) {
        this.hgap = hgap;
        this.vgap = vgap;
        this.border = border;
        this.slant = slant;
        this.tabSpace = tabSpace;
        this.tabpad = 10;
        //setLayout(new Layout());
        setLayout(new CardLayout());

        MouseListener mouseListener = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                Folder.this.mousePressed(e.getX(), e.getY());
            }
        };

        addMouseListener(mouseListener);
    }

    public Insets getInsets() {
        FontMetrics fm = getFontMetrics(getFont());
        int left_right = border + hgap;
        int top = border + (fm == null ? 0 : fm.getHeight()) + tabpad + vgap;
        int bottom = border + vgap;
        return new Insets(top, left_right, bottom, left_right);
    }

    public Component add(String name, Component comp) {
        entries.addElement(new Entry(comp, name, null, true));
        return super.add(name, comp);
    }

    public Component add(String name, Component comp, Color color) {
        entries.addElement(new Entry(comp, name, color, true));
        return super.add(name, comp);
    }

    public Component add(String name, Component comp, Color color, boolean visibleTab) {
        entries.addElement(new Entry(comp, name, color, visibleTab));
        return super.add(name, comp);
    }


    public void showTab(String name) {
        showTab(name, true);
    }

    public void hideTab(String name) {
        showTab(name, false);
    }

    public void showTab(String name, boolean visible) {
        for (int i = 0; i < entries.size(); i++) {
            Entry e = (Entry)entries.elementAt(i);
            if (e.name.equals(name)) {
                if (e.visibleTab != visible) {
                    e.visibleTab = visible;
                    repaint();
                }
                return;
            }
        }
    }

    public String getNextVisibleTab() {
        int currentIndex = Math.max(0, getCurrentIndex());
        for (int i = currentIndex + 1; i < entries.size(); i++) {
            Entry e = (Entry)entries.elementAt(i);
            if (e.visibleTab)
                return e.name;
        }
        return null;
    }

    public String getPrevVisibleTab() {
        int currentIndex = getCurrentIndex();
        for (int i = currentIndex - 1; i >= 0; i--) {
            Entry e = (Entry)entries.elementAt(i);
            if (e.visibleTab)
                return e.name;
        }
        return null;
    }

    public Component current() {
        for (int i = 0; i < entries.size(); i++) {
            Entry e = (Entry)entries.elementAt(i);
            if (e.comp.isVisible())
                return e.comp;
        }
        return null;
    }

    public void show(Component comp) {
        for (int i = 0; i < entries.size(); i++) {
            Entry e = (Entry)entries.elementAt(i);
            if (e.comp == comp) {
                show(e);
                return;
            }
        }
    }

    public void show(String name) {
        for (int i = 0; i < entries.size(); i++) {
            Entry e = (Entry)entries.elementAt(i);
            if (e.name.equals(name)) {
                show(e);
                return;
            }
        }
    }

    public void paint(Graphics g) {

        if (entries == null || entries.isEmpty()) {
            return;
        }

        Dimension dims = getSize();

        FontMetrics fm = g.getFontMetrics();
        int tabHeight = fm.getHeight() + tabpad;
        Entry selected = null;
        for (int i = 0; i < entries.size(); i++) {
            Entry e = (Entry)entries.elementAt(i);
            if (e.comp.isVisible()) {
                selected = e;
                break;
            }
        }
        if (selected != null) {
            g.setColor(selected.color != null ? selected.color : getBackground());
        }
        g.fillRoundRect(hgap, tabHeight + vgap,
            dims.width - 2*hgap - 1, dims.height - tabHeight - 2*vgap - 1,
            border, border);
        g.setColor(getForeground());
        g.drawRoundRect(hgap, tabHeight + vgap,
              dims.width - 2*hgap - 1, dims.height - tabHeight - 2*vgap - 1,
              border, border);
        int x = border + hgap;
        int baseLine = tabHeight + vgap;
        for (int i = 0; i < entries.size(); i++) {
            Entry e = (Entry)entries.elementAt(i);
            int tabH = (e.visibleTab || e.comp.isVisible() ? tabHeight : tabHeight / 3);
            int w = fm.stringWidth(e.name);
            Polygon tab = new Polygon();
            tab.addPoint(x, baseLine);
            tab.addPoint(x + slant, baseLine - tabH);
            tab.addPoint(x + slant + w, baseLine - tabH);
            tab.addPoint(x + slant + w + slant, baseLine);
            if (e.color != null) {
                g.setColor(e.color);
                g.fillPolygon(tab);
            }
            g.setColor(getForeground());
            g.drawLine(tab.xpoints[0], tab.ypoints[0], tab.xpoints[1], tab.ypoints[1]);
            g.drawLine(tab.xpoints[1], tab.ypoints[1], tab.xpoints[2], tab.ypoints[2]);
            g.drawLine(tab.xpoints[2], tab.ypoints[2], tab.xpoints[3], tab.ypoints[3]);
            if (e.visibleTab || e.comp.isVisible())
                g.drawString(e.name, x + slant, baseLine - tabpad/2 - fm.getDescent());
            if (e.comp.isVisible()) {
                g.setColor(e.color != null ? e.color : getBackground());
                g.drawLine(tab.xpoints[0], tab.ypoints[0], tab.xpoints[3], tab.ypoints[3]);
            }
            x = tab.xpoints[3] + tabSpace;
        }

        super.paint(g);
    }

    public void addItemListener(ItemListener l) {
        itemListener = AWTEventMulticaster.add(itemListener, l);
    }

    public void removeItemListener(ItemListener l) {
        itemListener = AWTEventMulticaster.remove(itemListener, l);
    }

    public Object[] getSelectedObjects() {
        Entry ce = getCurrentEntry();
        if (ce == null)
            return null;
        else
            return new Object[] { ce.comp };
    }

    int getMinimumWidth() {
        FontMetrics fm = getFontMetrics(getFont());
        int w = border + 2 * hgap + tabSpace;
        for (int i = 0; i < entries.size(); i++) {
          Entry e = (Entry)entries.elementAt(i);
          w += slant + (fm == null ? 0 : fm.stringWidth(e.name)) + slant + tabSpace;
        }
        return w;
    }

    private void show(Entry e) {
        Entry prevEntry = getCurrentEntry();
        Component prevComp = (prevEntry == null ? null : prevEntry.comp);

        ((CardLayout)getLayout()).show(this, e.name);
        repaint(); // needed to repaint the tab area

        if (itemListener != null) {
            ItemEvent ev1 =
                new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED,
                              prevComp, ItemEvent.DESELECTED);
            ItemEvent ev2 =
                new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED,
                              e.comp, ItemEvent.SELECTED);
            itemListener.itemStateChanged(ev1);
            itemListener.itemStateChanged(ev2);
        }
    }

    private int getCurrentIndex() {
        for (int i = 0; i < entries.size(); i++) {
            Entry e = (Entry)entries.elementAt(i);
            if (e.comp.isVisible())
                return i;
        }
        return -1;
    }

    private Entry getCurrentEntry() {
        int i = getCurrentIndex();
        return (i == -1 ? null : (Entry)(entries.elementAt(i)) );
    }

    void mousePressed(int mouseX, int mouseY) {
        FontMetrics fm = getFontMetrics(getFont());
        if (vgap < mouseY  &&  mouseY < border + fm.getHeight() + vgap) {
            int x = border + hgap;
            for (int i = 0; i < entries.size(); i++) {
                Entry e = (Entry)entries.elementAt(i);
                int w = fm.stringWidth(e.name);
                x += slant + w + slant + tabSpace;
                if (mouseX < x) {
                    show(e);
                    break;
                }
            }
        }
    }


    private ItemListener itemListener;
    private Vector entries = new Vector();
    private int border;
    private int slant;
    private int tabSpace;
    private int hgap;
    private int vgap;
    private int tabpad;
}

