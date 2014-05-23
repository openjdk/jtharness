/*
 * $Id$
 *
 * Copyright (c) 2002, 2014, Oracle and/or its affiliates. All rights reserved.
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
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.sun.interview.ChoiceArrayQuestion;
import com.sun.interview.Question;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public class ChoiceArrayQuestionRenderer
        implements QuestionRenderer
{

    public JComponent getQuestionRendererComponent(Question qq, ActionListener listener) {
        q = (ChoiceArrayQuestion)qq;
        displayChoices = q.getDisplayChoices();
        values = q.getValue();
        editedListener = listener;

        return createChoiceTable();
    }

    public String getInvalidValueMessage(Question q) {
        return null;
    }

    protected JComponent createChoiceTable() {
        AbstractTableModel tm = createTableModel();
        JTable tbl = new JTable(tm);

        tbl.setPreferredScrollableViewportSize(new Dimension(DOTS_PER_INCH, DOTS_PER_INCH));
        tbl.setShowHorizontalLines(false);
        tbl.setShowVerticalLines(false);
        tbl.setTableHeader(null);
        tbl.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        tbl.setRowSelectionAllowed(false);
        tbl.setColumnSelectionAllowed(false);
        tbl.setToolTipText(i18n.getString("chcArr.tbl.tip"));

        tbl.addKeyListener(createKeyListener(tm));

        TableColumn col0 = tbl.getColumnModel().getColumn(0);
        col0.setPreferredWidth(24);
        col0.setMaxWidth(24);
        col0.setResizable(false);

        TableColumn col1 = tbl.getColumnModel().getColumn(1);
        col1.setPreferredWidth(getColumnWidth(tbl, 1) + 20);

        final JScrollPane sp = new JScrollPane(tbl);
        sp.setName("chcArr.sp");
        sp.getViewport().setBackground(tbl.getBackground());


        JLabel lbl = new JLabel(i18n.getString("chcArr.tbl.lbl"));
        lbl.setName("chcArr.tbl.lbl");
        lbl.setDisplayedMnemonic(i18n.getString("chcArr.tbl.mne").charAt(0));
        lbl.setToolTipText(i18n.getString("chcArr.tbl.tip"));
        lbl.setLabelFor(sp);

        tbl.setRowHeight(getRowHeight());

        JPanel result = new JPanel(new BorderLayout());
        result.add(lbl, BorderLayout.NORTH);
        result.add(sp, BorderLayout.CENTER);

        return result;
    }

    protected int getColumnWidth(JTable table, int colIndex) {
        int width = -1;

        TableModel model = table.getModel();

        for(int i = 0; i < model.getRowCount(); i++) {
            TableCellRenderer r = table.getCellRenderer(i, colIndex);
            Component c = r.getTableCellRendererComponent(table,
                model.getValueAt(i, colIndex),
                false, false, i, colIndex);
            width = Math.max(width, c.getPreferredSize().width);
        }

        return width;
    }
    protected int getRowHeight() {
        return 22;
    }


    protected AbstractTableModel createTableModel() {
        return new TestTableModel();
    }

    protected class TestTableModel extends AbstractTableModel {
        public Class getColumnClass(int c) {
            return (c == 0 ? Boolean.class : String.class);
        }

        public int getColumnCount() {
            return 2;
        }

        public int getRowCount() {
            return displayChoices.length;
        }

        public Object getValueAt(int r, int c) {
            return (c == 0 ? (Object) (new Boolean(values[r])) : displayChoices[r]);
        }

        public void setValueAt(Object o, int r, int c) {
            if (c == 0) {
                values[r] = ((Boolean) o).booleanValue();
                q.setValue(values);
                fireEditedEvent(this, editedListener);
            }
        }

        public boolean isCellEditable(int r, int c) {
            return (c == 0 ? true : false);
        }
    };

    protected KeyListener createKeyListener(AbstractTableModel tm) {
        return new TestKeyListener(tm);
    }

    protected class TestKeyListener extends KeyAdapter {
        public TestKeyListener(AbstractTableModel tm) {
            this.tm = tm;
        }
        public void keyPressed(KeyEvent e) {
           if((e.getModifiersEx() & e.CTRL_DOWN_MASK) != 0 && e.getKeyCode() == e.VK_A) {
               boolean allSelected = true;
               for(int i = 0; i < tm.getRowCount(); i++) {
                   if(tm.getValueAt(i, 0).equals(new Boolean(false))) {
                       allSelected = false;
                       break;
                   }

               }
               for(int i = 0; i < tm.getRowCount(); i++) {
                   tm.setValueAt(new Boolean(!allSelected), i, 0);
                   TableModelEvent ev = new TableModelEvent(tm, i, i,
                                        TableModelEvent.ALL_COLUMNS,
                                        TableModelEvent.UPDATE);
                   tm.fireTableChanged(ev);
               }
           }

        }
        protected AbstractTableModel tm;
    };

    protected String[] displayChoices;
    protected boolean[] values;
    protected ChoiceArrayQuestion q;
    protected ActionListener editedListener;


    protected void fireEditedEvent(Object src, ActionListener l) {
        ActionEvent e = new ActionEvent(src,
                                        ActionEvent.ACTION_PERFORMED,
                                        EDITED);
        l.actionPerformed(e);
    }

    private static final I18NResourceBundle i18n = I18NResourceBundle.getDefaultBundle();
    protected static final int DOTS_PER_INCH = Toolkit.getDefaultToolkit().getScreenResolution();

}
