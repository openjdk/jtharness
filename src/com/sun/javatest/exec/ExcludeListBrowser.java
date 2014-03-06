/*
 * $Id$
 *
 * Copyright (c) 2003, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import com.sun.interview.Question;
import com.sun.interview.Interview;
import com.sun.javatest.ExcludeList;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.Parameters;
import com.sun.javatest.tool.ToolDialog;
import com.sun.javatest.tool.UIFactory;

class ExcludeListBrowser extends ToolDialog
{
    ExcludeListBrowser(Container parent, UIFactory uif) {
        super(parent, uif, "elb");

        listener = new Listener();
    }

    public void show(InterviewParameters params) {
        this.params = params;
        setVisible(true);
    }

    private void updateContent() {
        ExcludeList exclList = params.getExcludeList();
        File[] exclFiles;
        Parameters.ExcludeListParameters eParams =
            params.getExcludeListParameters();
        if (eParams instanceof Parameters.MutableExcludeListParameters)
            exclFiles = ((Parameters.MutableExcludeListParameters) eParams).getExcludeFiles();
        else
            exclFiles = null;

        // rely on interview caching to allow reference equality here
        if (list != exclList || files != exclFiles || model == null || list.size() != model.getRowCount()) {
            setTable(exclFiles, exclList);
    }
    }

    private void setTable(File[] files, ExcludeList list) {

        this.list = list;
        this.files = files;

        model = new ExcludeListTableModel(list);
        table.setModel(model);

    if (model.getRowCount() == 0) {
            setI18NTitle("elb.title0");
    } else {
            setI18NTitle("elb.title1", "" + model.getRowCount());
    }

    }

    protected void initGUI() {
        setHelp("exclList.window.csh");

        // fix
        // TO DO...
        //fileField = uif.createOutputField("elb.file", 30);
        //fileField.setBorder(null);
        //fileField.setHorizontalAlignment(JTextField.RIGHT);
        //setHeadExtras(fileField);
        JPanel body = uif.createPanel("elb.body", new GridBagLayout(), false);
        body.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        int dpi = uif.getDotsPerInch();
        body.setPreferredSize(new Dimension(5 * dpi, 2 * dpi));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weighty = 1;
        c.insets.bottom = 5;
        /*
        list = uif.createList("elb.list");
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                selectEntry((ExcludeList.Entry)(list.getSelectedValue()));
            }
        });

        list.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object o, int index, boolean isSelected, boolean cellHasFocus) {
                String name = entryToString((ExcludeList.Entry)o);
                return super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
            }
        });
        list.setVisibleRowCount(3);
        body.add(uif.createScrollPane(list), c);
        */
        table = new JTable();
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                showSelectedEntry();
            }
        });

        uif.setAccessibleInfo(table, "elb.tbl");
        uif.setToolTip(table, "elb.tbl");

        body.add(new JScrollPane(table), c);

        GridBagConstraints lc = new GridBagConstraints();
        lc.insets.top = 2;
        lc.insets.right = 5;
        lc.anchor = GridBagConstraints.EAST;

        GridBagConstraints fc = new GridBagConstraints();
        fc.gridwidth = GridBagConstraints.REMAINDER;
        fc.insets.top = 2;
        fc.weightx = 1;
        fc.fill = GridBagConstraints.HORIZONTAL;

        JLabel synopsisLabel = uif.createLabel("elb.synopsis", true);
        body.add(synopsisLabel, lc);
        synopsisField = uif.createOutputField("elb.synopsis", synopsisLabel);
        body.add(synopsisField, fc);

        JLabel kwLabel = uif.createLabel("elb.kws", true);
        body.add(kwLabel, lc);
        kwField = uif.createOutputField("elb.kws", kwLabel);
        body.add(kwField, fc);

        JLabel bugIdsLabel = uif.createLabel("elb.bugids", true);
        body.add(bugIdsLabel, lc);
        bugIdsField = uif.createOutputField("elb.bugids", bugIdsLabel);
        body.add(bugIdsField, fc);

        setBody(body);

        JButton helpBtn = uif.createHelpButton("elb.help", "exclList.window.csh");
        JButton closeBtn = uif.createCloseButton("elb.close");
        setButtons(new JButton[] { helpBtn, closeBtn }, closeBtn);

        setComponentListener(listener);
    }

    private void showSelectedEntry() {
        ExcludeList.Entry e = model.getEntry(table.getSelectedRow());

        if (e == null) {
            synopsisField.setText("");
            kwField.setText("");
            bugIdsField.setText("");
        }
        else {
            synopsisField.setText(e.getSynopsis());
            kwField.setText(getKeywords(e));
            bugIdsField.setText(getBugIds(e));
        }
    }

    private String entryToString(ExcludeList.Entry e) {
        String u = e.getRelativeURL();
        String tc = e.getTestCases();
        return (tc == null ? u : u + "[" + tc + "]");
    }

    private String getBugIds(ExcludeList.Entry e) {
        String[] bugIds = e.getBugIdStrings();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bugIds.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(bugIds[i]);
        }
        return sb.toString();
    }

    private String getKeywords(ExcludeList.Entry e) {
        String[] keywords = e.getPlatforms();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < keywords.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(keywords[i]);
        }
        return sb.toString();
    }


    private InterviewParameters params;
    private File[] files;
    private ExcludeList list;

    private ExcludeListTableModel model;
    private JTable table;
    private JTextField synopsisField;
    private JTextField kwField;
    private JTextField bugIdsField;

    private Listener listener;

    private class Listener
        extends ComponentAdapter
        implements Interview.Observer
    {
        // ComponentListener
        public void componentShown(ComponentEvent e) {
            params.addObserver(this);
            updateContent();
        }

        public void componentHidden(ComponentEvent e) {
            params.removeObserver(this);
        }

        // Interview.Observer
        public void currentQuestionChanged(Question q) {
        }

        public void pathUpdated() {
            updateContent();
        }
    }

    private static final int TEST_NAME_COL = 0;
    private static final int TEST_CASE_COL = 1;
    private static final int BUG_COL = 2;
    private static final int KEYWORDS_COL = 3;
    private static final int SYNOPSIS_COL = 4;
    private static final int COLUMN_COUNT = 5;

    private String[] columnNames;

    private class ExcludeListTableModel implements TableModel
    {
        ExcludeListTableModel(ExcludeList list) {
            if (columnNames == null) {
                columnNames = new String[COLUMN_COUNT];
                columnNames[TEST_NAME_COL] = uif.getI18NString("elb.col.testName");
                columnNames[TEST_CASE_COL] = uif.getI18NString("elb.col.testCase");
                columnNames[BUG_COL] = uif.getI18NString("elb.col.bugId");
                columnNames[KEYWORDS_COL] = uif.getI18NString("elb.col.keywords");
                columnNames[SYNOPSIS_COL] = uif.getI18NString("elb.col.synopsis");
            }

            // The following operation is slow and should arguably be
            // done by a worker thread, perhaps using the nested List class as
            // a Runnable.
            SortedSet sortedEntries = new TreeSet(new Comparator() {
                    public int compare(Object o1, Object o2) {
                        String s1 = entryToString((ExcludeList.Entry)o1);
                        String s2 = entryToString((ExcludeList.Entry)o2);
                        return s1.compareTo(s2);
                    }
                });

            if (list != null) {
                for (Iterator iter = list.getIterator(false); iter.hasNext(); ) {
                    ExcludeList.Entry ee = (ExcludeList.Entry) (iter.next());
                    sortedEntries.add(ee);
                }
            }

            entries = new ExcludeList.Entry[sortedEntries.size()];
            sortedEntries.toArray(entries);
        }

        ExcludeList.Entry getEntry(int index) {
            return (index < 0 || index >= entries.length ? null : entries[index]);
        }

        public void addTableModelListener(TableModelListener l) {
            // model never changes, so ignore listener
        }

        public Class getColumnClass(int columnIndex) {
            // for now, all are strings
            return String.class;
        }

        public int getColumnCount() {
            return COLUMN_COUNT;
        }

        public String getColumnName(int index) {
            return columnNames[index];
        }

        public int getRowCount() {
            return entries.length;
        }

        public Object getValueAt(int rowIndex, int colIndex) {
            ExcludeList.Entry e = entries[rowIndex];
            switch (colIndex) {
            case TEST_NAME_COL:
                return e.getRelativeURL();

            case TEST_CASE_COL:
                return e.getTestCases();

            case BUG_COL:
                return getBugIds(e);

            case KEYWORDS_COL:
                return getKeywords(e);

            case SYNOPSIS_COL:
                return e.getSynopsis();
            }

            throw new IllegalArgumentException();
        }

        public boolean isCellEditable(int rowIndex, int colIndex) {
            return false;
        }

        public void removeTableModelListener(TableModelListener l) {
            // model never changes, so ignore listener
        }

        public void setValueAt(Object aValue, int rowIndex, int colIndex) {
            throw new UnsupportedOperationException();
        }

        private ExcludeList.Entry[] entries;

    }
}
