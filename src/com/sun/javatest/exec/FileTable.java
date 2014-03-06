/*
 * $Id$
 *
 * Copyright (c) 2006, 2011, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.tool.UIFactory;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

public class FileTable extends JTable {



    public FileTable(FileSystemTableModel model, UIFactory uif) {
        super(model);
        this.uif = uif;
        setCellSelectionEnabled(false);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setRowSelectionAllowed(true);
        setShowGrid(false);
        getColumnModel().getColumn(0).setCellRenderer(new IconRenderer());
        autoResizeColumns = new ArrayList<Resize>();

        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        getColumnModel().getColumn(0).setMinWidth(130);
        getColumnModel().getColumn(2).setMinWidth(130);
        addAutoResizeColumn(2, true);

        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                updateWidth();
            }
        });
        updateWidth();
    }

    /**
     * Selects column to be autoresized
     * @param column column index, should be &lt;= 0 and &gt; getColumnCount()
     * @param keepSize shows if it is needed to keep width of column when the model
     * is changed
     * true: width of the column will be the same or greater at model change
     * false: width will always equal the longest name
     * @return false in case this column is already autoresizing, true if column was added
     */
    public boolean addAutoResizeColumn(int column, boolean keepSize) {
        if(column < 0 || column >= getColumnCount())
            throw new IllegalArgumentException("Column " + column + " doesn't exist in the table");

        for(Resize r: autoResizeColumns)
            if(r.column == column)
                return false;

        autoResizeColumns.add(new Resize(column, keepSize));
        return true;
    }

    /**
     * Selects column not to be autoresized
     * @param column column index
     * @return true in case column was set not to be autoresized,
     * false in case column wasn't autoresized
     */
    public boolean removeAutoResizeColumn(int column) {
        for(Resize r: autoResizeColumns) {
            if(r.column == column) {
                autoResizeColumns.remove(r);
                return true;
            }
        }
        return false;
    }

    /**
     * Autoresize all selected columns
     */
    void updateWidth() {
        for(Resize r: autoResizeColumns) {
            updateWidth(r.column, r.keep);
        }
    }

    /**
     * Autoresize column to fit the longest containing name
     * @param columnIndex column index
     * @param keepWidth shows if it is needed to keep width of column when the model
     * is changed
     * true: width of the column will be the same or greater at model change
     * false: width will always equal the longest name
     */
    void updateWidth(int columnIndex, boolean keepWidth) {
        int width = 0;
        TableColumn column = getColumnModel().getColumn(columnIndex);
        // if it is needed to keep width - starting width is set to old value
        if(keepWidth)
            width = column.getWidth();

        boolean modified = false;
        for(int i = 0; i < getRowCount(); i++) {
            Object o = getValueAt(i, columnIndex);
            if(o != null) {
                String s = o.toString();
                int stringWidth = getFontMetrics(getFont()).stringWidth(s);
                if(stringWidth > width) {
                    width = stringWidth;
                    modified = true;
                }
            }
        }

        if(modified)
            width *= 1.1; // table lines also have some width
        column.setPreferredWidth(width);
    }

    private class IconRenderer extends DefaultTableCellRenderer {

        {
            up = uif.createIcon("upper");
            dir = uif.createIcon("folder");
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            setFont(table.getFont());
            setIcon(null);
            setText("");

            if (isSelected) {
                super.setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                super.setForeground(table.getForeground());
                super.setBackground(table.getBackground());
            }

            if (value instanceof FileTableNode) {
                FileTableNode fn = (FileTableNode) value;
                if (fn.getMode() != 'f') {
                    if (fn.getMode() == 'u') {
                        setIcon(up);
                        return this;
                    }
                    if (fn.getMode() == 'd') {
                        setIcon(dir);
                        setText(fn.toString());
                        return this;
                    }
                }
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }

        private Icon up;
        private Icon dir;

    }

    /**
     * A simple structure that contains column index and keepSize property
     */
    private static class Resize {
        private int column;
        private boolean keep;

        public Resize(int column, boolean keepSize) {
            this.column = column;
            this.keep = keepSize;
        }
    }

    ArrayList<Resize> autoResizeColumns; // list of columns to be autoresized
    private UIFactory uif;


}
