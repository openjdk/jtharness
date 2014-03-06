/*
 * $Id$
 *
 * Copyright (c) 2002, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;

import com.sun.interview.ChoiceQuestion;
import com.sun.interview.Question;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import javax.swing.AbstractCellEditor;
import javax.swing.ButtonGroup;
import javax.swing.CellEditor;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public class ChoiceQuestionRenderer
        implements QuestionRenderer
{

    public JComponent getQuestionRendererComponent(Question qq, ActionListener listener) {
        q = (ChoiceQuestion)qq;
        displayChoices = q.getDisplayChoices();
        values = q.getChoices();
        starts_from = values[0] == null ? 1 : 0;
        editedListener = listener;

        return createChoiceTable();
    }

    public String getInvalidValueMessage(Question q) {
        return null;
    }


    protected JComponent createChoiceTable() {

        TableModel tm = createTableModel();
        final JTable tbl = new JTable(tm);
        tbl.setOpaque(false);


        rb = new JRadioButton[displayChoices.length - starts_from];
        ButtonGroup bg = new ButtonGroup();

        String v = q.getValue();
        for(int i = 0; i < rb.length; i++) {

            rb[i] = new JRadioButton(displayChoices[i + starts_from],
                                                (values[i + starts_from] == v));
            rb[i].setActionCommand(values[i + starts_from]);

            rb[i].setName("chc.btn." + values[i + starts_from]);
            if (i < 10)
                rb[i].setMnemonic('0' + i);

            rb[i].setToolTipText(i18n.getString("chc.btn.tip"));
            rb[i].getAccessibleContext().setAccessibleName(rb[i].getName());
            rb[i].getAccessibleContext().setAccessibleDescription(rb[i].getToolTipText());

            rb[i].setBackground(tbl.getBackground());
            rb[i].setOpaque(false);

            rb[i].setFocusPainted(false);
            bg.add(rb[i]);

            rb[i].addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                   CellEditor editor = tbl.getCellEditor();
                   if (editor != null) {
                       editor.stopCellEditing();
                   }
               }
            });
        }

        tbl.setPreferredScrollableViewportSize(new Dimension(DOTS_PER_INCH, DOTS_PER_INCH));
        tbl.setShowHorizontalLines(false);
        tbl.setShowVerticalLines(false);
        tbl.setTableHeader(null);

        tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbl.setRowSelectionAllowed(false);
        tbl.setColumnSelectionAllowed(false);
        tbl.setToolTipText(i18n.getString("chcArr.tbl.tip"));

        TableColumn col0 = tbl.getColumnModel().getColumn(0);
        TableCellRenderer r = createCellRenderer();
        col0.setCellRenderer(r);
        TableCellEditor e = createCellEditor();
        col0.setCellEditor(e);

        col0.setPreferredWidth(getColumnWidth(tbl, 0) + 20);

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
        int rowCount = model.getRowCount();

        for(int i = 0; i < rowCount; i++) {
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

    protected static final int DOTS_PER_INCH = Toolkit.getDefaultToolkit().getScreenResolution();

    protected void fireEditedEvent(Object src, ActionListener l) {
        ActionEvent e = new ActionEvent(src,
                                        ActionEvent.ACTION_PERFORMED,
                                        EDITED);
        l.actionPerformed(e);
    }


    protected TableModel createTableModel() {
        return new TestTableModel();
    }

    protected class TestTableModel extends AbstractTableModel {

        public Class getColumnClass(int c) {
            return String.class;
        }

        public int getColumnCount() {
            return 1;
        }

        public int getRowCount() {
            return displayChoices.length - starts_from;
        }

        public Object getValueAt(int r, int c) {
            return values[r + starts_from];
        }

        public void setValueAt(Object o, int r, int c) {
            if (c == 0) {
                q.setValue(values[r + starts_from]);
                fireEditedEvent(this, editedListener);
            }
        }

        public boolean isCellEditable(int r, int c) {
            return true;
        }

    };

    protected TableCellRenderer createCellRenderer() {
        return new TestTableRenderer();
    }

    protected class TestTableRenderer implements TableCellRenderer {

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            return rb[row];
        }
    };

    protected TableCellEditor createCellEditor() {
        return new TestTableEditor();
    }

    protected class TestTableEditor extends AbstractCellEditor
            implements TableCellEditor {
        public Object getCellEditorValue() {
            return null;
        }
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {

            rb[row].setSelected(true);
            return rb[row];
        }
    };


    protected ChoiceQuestion q;
    protected String[] displayChoices;
    protected String[] values;
    protected int starts_from;
    protected ActionListener editedListener;
    protected JRadioButton[] rb;

    private static final I18NResourceBundle i18n = I18NResourceBundle.getDefaultBundle();

}
