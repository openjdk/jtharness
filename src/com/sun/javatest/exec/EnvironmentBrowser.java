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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;

import com.sun.interview.Interview;
import com.sun.interview.Question;
import com.sun.javatest.TestEnvironment;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.tool.ToolDialog;
import com.sun.javatest.tool.UIFactory;

class EnvironmentBrowser extends ToolDialog
{

    EnvironmentBrowser(JComponent parent, UIFactory uif) {
        super(parent, uif, "env");

        listener = new Listener();

        envTableModel = new ElementsTableModel();
    }

    public void show(InterviewParameters params) {
        this.params = params;
        setVisible(true);
    }

    private void setEnv(TestEnvironment env) {
        this.env = env;

        if (env == null || env.getName().trim().length() == 0)
            setI18NTitle("env.title.unset");
        else
            setI18NTitle("env.title.name", env.getName());

        envTableModel.setEnvironment(env);
    }

    protected void initGUI() {
        setHelp("env.window.csh");

        JPanel body = uif.createPanel("env.body", false);
        body.setLayout(new GridBagLayout());
        body.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        table = uif.createTable("env.data", envTableModel);
        // make default size small to reduce change of ToolSubPanel scrollbars
        table.setPreferredScrollableViewportSize(new Dimension(100, 100));
        table.setCellSelectionEnabled(true);
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Object src = e.getSource();
                if (src instanceof JTableHeader) {
                    JTableHeader th = (JTableHeader)src;
                    int col = th.columnAtPoint(e.getPoint());
                    envTableModel.sort(col);
                }
            }
        });
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(listener);
        table.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getSelectionModel().addListSelectionListener(listener);

        JScrollPane table_sp = uif.createScrollPane(table);
        int dpi = uif.getDotsPerInch();
        table_sp.setPreferredSize(new Dimension(6 * dpi, 3 * dpi));

        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.insets.bottom = 10;
        c.weightx = 1;
        c.weighty = 1;
        body.add(table_sp, c);

        JLabel lbl = uif.createLabel("env.value", true);
        c.insets.bottom = 0;
        c.weighty = 0;
        body.add(lbl, c);

        text = uif.createTextArea("env.value", lbl);
        text.setRows(5);
        text.setLineWrap(true);
        text.setEditable(false);

        c.weighty = 0.5;
        body.add(new JScrollPane(text), c);

        setBody(body);

        JButton helpBtn = uif.createHelpButton("env.help", "env.window.csh");
        JButton closeBtn = uif.createCloseButton("env.close");
        setButtons(new JButton[] { helpBtn, closeBtn }, closeBtn);

        setComponentListener(listener);
    }

    private InterviewParameters params;
    private Listener listener;
    private TestEnvironment env;
    private ElementsTableModel envTableModel;
    private JTable table;
    private JTextArea text;
    static private String[] headings;

    private static final int KEY = 0;
    private static final int VALUE = 1;
    private static final int DEFINED_IN_FILE = 2;
    private static final int DEFINED_IN_ENV = 3;

    private class Listener
        extends ComponentAdapter
        implements ListSelectionListener, Interview.Observer
    {
        // ComponentListener
        public void componentShown(ComponentEvent e) {
            params.addObserver(listener);
            updateContent();
        }

        public void componentHidden(ComponentEvent e) {
            params.removeObserver(listener);
        }

        // ListSelectionListener
        public void valueChanged(ListSelectionEvent e) {
            //System.err.println(e);
            int r = table.getSelectedRow();
            int c = table.getSelectedColumn();
            if (r == -1 || c == -1)
                text.setText("");
            else {
                Object o = table.getModel().getValueAt(r, c);
                if (o == null) {
                    text.setFont(text.getFont().deriveFont(Font.ITALIC));
                    text.setForeground(Color.gray);
                    text.setText(uif.getI18NString("env.unset"));
                }
                else {
                    text.setFont(text.getFont().deriveFont(Font.PLAIN));
                    text.setForeground(table.getForeground());
                    text.setText(String.valueOf(o));
                }
            }
        }

        // Interview.Observer
        public void currentQuestionChanged(Question q) {
        }

        public void pathUpdated() {
            updateContent();
        }

        private void updateContent() {
            setEnv(params.getEnv());
        }
    };

    private class EnvEntryComparator implements Comparator {
        EnvEntryComparator(int sortMode, String[] inherits) {
            this.sortMode = sortMode;
            this.inherits = inherits;
        }

        public int compare(Object o1, Object o2) {
            TestEnvironment.Element e1 = (TestEnvironment.Element)o1;
            TestEnvironment.Element e2 = (TestEnvironment.Element)o2;
            // the following should be a switch statement, but JDK
            // 1.1.7 can't compile it: doesn't recognize KEY etc as
            // constants.
            if (sortMode == KEY)
                // key should always be unique, so should be enough to sort on that
                return (e1.getKey().compareTo(e2.getKey()));
            else if (sortMode == VALUE) {
                // value probably unique, but if not, sort on key as well
                int c = (e1.getValue().compareTo(e2.getValue()));
                return (c != 0 ? c : e1.getKey().compareTo(e2.getKey()));
            }
            else if (sortMode == DEFINED_IN_ENV) {
                // defined_in probably not unique, so sort on key as well
                int i1 = getInheritsIndex(e1.getDefinedInEnv());
                int i2 = getInheritsIndex(e2.getDefinedInEnv());
                return (i1 < i2 ? -1 :
                        i1 > i2 ? +1 : e1.getKey().compareTo(e2.getKey()));
            }
            else if (sortMode == DEFINED_IN_FILE) {
                // defined_in probably not unique, so sort on key as well
                int c = (e1.getDefinedInFile().compareTo(e2.getDefinedInFile()));
                return (c != 0 ? c : e1.getKey().compareTo(e2.getKey()));
            }
            else {
                return 0;
            }
        }

        private int getInheritsIndex(String s) {
            for (int i = 0; i < inherits.length; i++) {
                if (inherits[i].equals(s))
                    return i;
            }
            return inherits.length;
        }

        private int sortMode;
        private String[] inherits;
    }

    private class ElementsTableModel extends AbstractTableModel {
        ElementsTableModel() {
            if (headings == null) {
                headings = new String[4];
                headings[KEY] = uif.getI18NString("env.head.key");
                headings[VALUE] = uif.getI18NString("env.head.value");
                headings[DEFINED_IN_FILE] = uif.getI18NString("env.head.defInFile");
                headings[DEFINED_IN_ENV] = uif.getI18NString("env.head.defInEnv");
            }
        }

        public synchronized void setEnvironment(TestEnvironment env) {
            int oldRowCount = getRowCount();
            currEnv = env;

            if (currEnv == null)
                elems = null;
            else {
                Collection e = currEnv.elements();
                elems = (TestEnvironment.Element[]) (e.toArray(new TestEnvironment.Element[e.size()]));
                Arrays.sort(elems, new EnvEntryComparator(KEY, currEnv.getInherits()));
            }
            int newRowCount = getRowCount();

            int commonRowCount = Math.min(oldRowCount, newRowCount);

            if (commonRowCount > 0) {
                // the rows in common have changed
                fireTableRowsUpdated(0, commonRowCount - 1);
            }

            if (newRowCount > oldRowCount) {
                // the new table is bigger: so rows have been added
                fireTableRowsInserted(commonRowCount, newRowCount - 1);
            }
            else if (newRowCount < oldRowCount) {
                // the new table is smaller, so rows have been removed
                fireTableRowsDeleted(commonRowCount, oldRowCount - 1);
            }
        }

        public void sort(int columnIndex) {
            if (elems != null) {
                Arrays.sort(elems, new EnvEntryComparator(columnIndex, currEnv.getInherits()));
                fireTableRowsUpdated(0, elems.length - 1);
            }
        }

        private void update() {
        }

        public synchronized int getRowCount() {
            return (elems == null ? 0 : elems.length);
        }

        public int getColumnCount() {
            // might be nice to make this more dynamic ...
            // have "defined in env" and "defined in file" be dynamic, specified on View menu
            return 4; // key, value, defined_in_env, defined_in_file
        }

        public String getColumnName(int columnIndex) {
            return headings[columnIndex];
        }

        public Class getColumnClass(int columnIndex) {
            return String.class;
        }

        public synchronized Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex < 0 || rowIndex >= getRowCount()
                || columnIndex < 0 || columnIndex >= getColumnCount())
                throw new IllegalArgumentException();

            TestEnvironment.Element e = elems[rowIndex];
            switch (columnIndex) {
            case KEY:
                return e.getKey();
            case DEFINED_IN_ENV:
                return e.getDefinedInEnv();
            case DEFINED_IN_FILE:
                return e.getDefinedInFile();
            case VALUE:
                return e.getValue();
            default:
                throw new Error();
            }
        }

        private TestEnvironment.Element[] elems;
        private TestEnvironment currEnv;
    }
}
