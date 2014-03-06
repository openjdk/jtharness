/*
 * $Id$
 *
 * Copyright (c) 2001, 2009, Oracle and/or its affiliates. All rights reserved.
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
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.text.View;

import com.sun.javatest.TestResult;
import com.sun.javatest.tool.UIFactory;

/**
 * Base class of all subpanels of TestPanel that need to display property list
 * type information.
 */

abstract class TP_PropertySubpanel
    extends TP_Subpanel
{
    protected TP_PropertySubpanel(UIFactory uif, String uiKey) {
        super(uif, uiKey);
        setLayout(new BorderLayout());
        setOpaque(false);

        JTextField caption = uif.createHeading("test." + uiKey + ".caption");
        caption.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(caption, BorderLayout.NORTH);

        table = new Table(uif);

        JScrollPane sp = uif.createScrollPane(table);
        sp.getViewport().setViewPosition(new Point(0, 0));
        sp.getViewport().setBackground(Color.white);
        add(sp, BorderLayout.CENTER);
    }

    void setHead(String nameTitle, String valueTitle) {
        table.setHead(nameTitle, valueTitle);
    }

    protected void updateSubpanel(TestResult currTest) {
        //System.err.println("TP_PS: updateSubpanel");
        super.updateSubpanel(currTest);
        table.reset();
    }

    protected void updateEntries(Map map) {
        for (Iterator i = map.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry) (i.next());
            String key = (String) (e.getKey());
            String val = (String) (e.getValue());
            table.updateEntry(key, val);
        }
    }

    protected void updateEntry(String key, String val) {
        table.updateEntry(key, val);
    }

    private Table table;

    private class Table extends JPanel
        implements ComponentListener, Scrollable
    {
        Table(UIFactory uif) {
            addComponentListener(this);
            setLayout(null);
            setBackground(Color.white);
            entries = new TreeMap();

            // space to go around text
            Border bsp = BorderFactory.createEmptyBorder(2, 4, 2, 4); // top, left, bottom, right

            // Border for head components, including head corner above possible scrollbar
            // (lines above and below)
            Border bh = BorderFactory.createMatteBorder(1, 0, 1, 0, Color.lightGray);
            headBorder = BorderFactory.createCompoundBorder(bh, bsp);

            // Border for body components (line below)
            Border br = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.lightGray);
            bodyBorder = BorderFactory.createCompoundBorder(br, bsp);

            nameLabel = uif.createHeading("test.table.name");
            nameLabel.setBorder(headBorder);

            valueLabel = uif.createHeading("test.table.value");
            valueLabel.setBorder(headBorder);
        }

        void setHead(String nameTitle, String valueTitle) {
            nameLabel.setText(nameTitle);
            valueLabel.setText(valueTitle);
        }

        void updateEntry(String key, String value) {
            //System.err.println("TP_PS.Table: updateEntry " + key + "=" + value);
            Entry e = (Entry) (entries.get(key));
            if (e == null) {
                e = new Entry(key, value);
                entries.put(key, e);
                maxNameStringWidth = Math.max(maxNameStringWidth, getFontMetrics(getFont()).stringWidth(key));
            }
            else
                e.valueText.setText(value);

            revalidate();
        }

        void reset() {
            //System.err.println("TP_PS.Table: reset");
            entries.clear();
            removeAll();
            maxNameStringWidth = 100;
            if (!inScrollPane) {
                add(nameLabel);
                add(valueLabel);
            }
            revalidate();
        }

        // JComponent

        public void addNotify() {
            super.addNotify();
            configureEnclosingScrollPane();
        }

        public void removeNotify() {
            super.removeNotify();
            unconfigureEnclosingScrollPane();
        }

        public void revalidate() {
            // real revalidate does not work inside scrollpanes ... sigh
            // so emulate the necessary behavior instead
            //System.err.println("TP_PS.Table: revalidate");
            if (inScrollPane) {
                //System.err.println("TP_PS.Table: revalidate inScrollPane");
                synchronized (getTreeLock()) {
                    if (pendingValidate == false) {
                        //System.err.println("TP_PS.Table: revalidate inScrollPane !valid");
                        invalidate();
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                //System.err.println("TP_PS.Table: revalidate callback");
                                synchronized (getTreeLock()) {
                                    validate();
                                    pendingValidate = false;
                                }
                            }
                        });
                        pendingValidate = true;
                    }
                }
            }
            else
                super.revalidate();
        }

        // ComponentListener

        public void componentHidden(ComponentEvent e) {
            //System.err.println("TP_PS.Table: componentHidden");
        }

        public void componentMoved(ComponentEvent e) {
            //System.err.println("TP_PS.Table: componentMoved");
        }

        public void componentResized(ComponentEvent e) {
            //System.err.println("TP_PS.Table: componentResized " + getSize());
            revalidate();
        }

        public void componentShown(ComponentEvent e) {
            //System.err.println("TP_PS.Table: componentShown");
        }

        // Layout

        public void doLayout() {
            //System.err.println("TP_PS.Table: doLayout");
            synchronized (getTreeLock()) {
                Insets ni = bodyBorder.getBorderInsets(this);
                int nameWidth = ni.left + maxNameStringWidth + 10 + ni.right; // allow padding
                int valueWidth = Math.max(getWidth() - nameWidth, 200);

                int h = nameLabel.getPreferredSize().height;
                nameLabel.setBounds(0, 0, nameWidth, h);
                valueLabel.setBounds(nameWidth, 0, valueWidth, h);

                int y = (inScrollPane ? 0 : h);

                for (Iterator iter = entries.values().iterator(); iter.hasNext(); ) {
                    Entry e = (Entry) (iter.next());
                    // need to take insets into account for value, since we are dealing
                    // with the elemental view inside the valueField
                    Insets vi = e.valueText.getInsets();
                    View v = e.valueText.getUI().getRootView(e.valueText);
                    v.setSize(valueWidth, Integer.MAX_VALUE);
                    h = vi.top + ((int) (v.getPreferredSpan(View.Y_AXIS))) + vi.bottom;
                    e.nameField.setBounds(0, y, nameWidth, h);
                    e.valueText.setBounds(nameWidth, y, valueWidth, h);
                    y += h;
                }
            }
        }

        public Dimension getMinimumSize() {
            //System.err.println("TP_PS.Table: minimumLayoutSize");
            int h = (inScrollPane ? 0 : nameLabel.getPreferredSize().height);
            for (Iterator iter = entries.values().iterator(); iter.hasNext(); ) {
                Entry e = (Entry) (iter.next());
                h += e.valueText.getMinimumSize().height;
            }
            return new Dimension(maxNameStringWidth + 400, h);
        }

        public Dimension getPreferredSize() {
            //System.err.println("TP_PS.Table: preferredLayoutSize");
            int h = (inScrollPane ? 0 : nameLabel.getPreferredSize().height);
            for (Iterator iter = entries.values().iterator(); iter.hasNext(); ) {
                Entry e = (Entry) (iter.next());
                h += e.valueText.getPreferredSize().height;
            }
            return new Dimension(maxNameStringWidth + 400, h);
        }

        // Scrollable

        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            switch(orientation) {
            case SwingConstants.VERTICAL:
                return visibleRect.height / 10;
            case SwingConstants.HORIZONTAL:
                return visibleRect.width / 10;
            default:
                throw new IllegalArgumentException("Invalid orientation: " + orientation);
            }
        }

        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            switch(orientation) {
            case SwingConstants.VERTICAL:
                return visibleRect.height;
            case SwingConstants.HORIZONTAL:
                return visibleRect.width;
            default:
                throw new IllegalArgumentException("Invalid orientation: " + orientation);
            }
        }

        // private

        private void configureEnclosingScrollPane() {
            //System.err.println("TP_PS.Table: configureEnclosingScrollPane");
            Container p = getParent();
            if (p instanceof JViewport) {
                Container gp = p.getParent();
                if (gp instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane)gp;
                    // Make certain we are the viewPort's view and not, for
                    // example, the rowHeaderView of the scrollPane -
                    // an implementor of fixed columns might do this.
                    JViewport viewport = scrollPane.getViewport();
                    if (viewport == null || viewport.getView() != this)
                        return;
                    inScrollPane = true;
                    scrollPane.setColumnHeaderView(new Header());
                    JPanel corner = new JPanel();
                    corner.setBackground(Color.white);
                    corner.setBorder(headBorder);
                    scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, corner);
                }
            }
        }

        private void unconfigureEnclosingScrollPane() {
            Container p = getParent();
            if (p instanceof JViewport) {
                Container gp = p.getParent();
                if (gp instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane)gp;
                    // Make certain we are the viewPort's view and not, for
                    // example, the rowHeaderView of the scrollPane -
                    // an implementor of fixed columns might do this.
                    JViewport viewport = scrollPane.getViewport();
                    if (viewport == null || viewport.getView() != this)
                        return;
                    inScrollPane = false;
                    scrollPane.setColumnHeaderView(null);
                }
            }
        }

        private SortedMap entries;
        private int maxNameStringWidth = 100;
        private JTextField nameLabel;
        private JTextField valueLabel;
        private Border headBorder;
        private Border bodyBorder;
        private boolean inScrollPane;
        private boolean pendingValidate;

        private class Entry
        {
            Entry(String name, String value) {
                this.name = name;
                this.value = value;

                nameField = uif.createOutputField("test.table.entry.name", name);
                nameField.setBorder(bodyBorder);
                nameField.setEditable(false);
                nameField.setOpaque(false);
                add(nameField);

                int width = Math.max(getWidth() - maxNameStringWidth, 200);
                valueText = uif.createTextArea("test.table.entry.value");
                valueText.setText(value);
                valueText.setBorder(bodyBorder);
                valueText.setEditable(false);
                valueText.setLineWrap(true);
                add(valueText);
            }

            String name;
            JTextField nameField;
            String value;
            JTextArea valueText;
        }

        private class Header extends JPanel {
            Header() {
                setLayout(null);
                setOpaque(true);
                setBackground(Color.white);
                add(nameLabel);
                add(valueLabel);
            }

            public Dimension getMinumumSize() {
                return new Dimension(Table.this.getMinimumSize().width,
                                     nameLabel.getMinimumSize().height);
            }

            public Dimension getPreferredSize() {
                return new Dimension(Table.this.getPreferredSize().width,
                                     nameLabel.getPreferredSize().height);
            }

            // doLayout -- nameValue and valueLabel are placed by Table.doLayout
        }

    }

}

