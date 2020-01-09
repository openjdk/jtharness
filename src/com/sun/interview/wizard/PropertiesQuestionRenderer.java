/*
 * $Id$
 *
 * Copyright (c) 2006, 2016, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.interview.PropertiesQuestion;
import com.sun.interview.Question;
import com.sun.javatest.tool.UIFactory;

import javax.accessibility.AccessibleContext;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.CellEditor;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class PropertiesQuestionRenderer implements QuestionRenderer {
    private static final I18NResourceBundle i18n = I18NResourceBundle.getDefaultBundle();
    protected Runnable valueSaver;
    protected Map<String, JTable> tables;

    // TABLE BUILDING
    protected RenderingUtilities.PropCellRenderer renderer;
    protected PropertiesQuestion question;
    protected JPanel panel;

    @Override
    public JComponent getQuestionRendererComponent(Question qq, ActionListener listener) {
        question = (PropertiesQuestion) qq;

        tables = new HashMap<>();

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setName("properties");
        panel.setFocusable(false);

        if (question.getValue() == null) {
            showEmptyQuestion(panel);
            return panel;
        }

        // add table(s)
        addGroup(null, panel, listener);

        // note that empty groups are not returned by the next call
        String[] groups = question.getGroups();
        if (groups != null) {
            for (String group : groups) {
                addGroup(group, panel, listener);
            }   // for
        }

        if (panel.getComponentCount() == 0) {
            showEmptyQuestion(panel);
        }

        valueSaver = new Runnable() {
            @Override
            public void run() {
                Set<String> keys = tables.keySet();
                for (String key : keys) {
                    JTable table = tables.get(key);
                    CellEditor editor = table.getCellEditor();
                    if (editor != null) {
                        editor.stopCellEditing();
                    }
                }
            }
        };

        panel.putClientProperty(VALUE_SAVER, valueSaver);


        // This inserted to handle programmatically fired events
        // when user click 'X' button in ConfigEditor
        panel.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent e) {

            }

            @Override
            public void ancestorMoved(AncestorEvent e) {

            }

            @Override
            public void ancestorRemoved(AncestorEvent e) {
                if (valueSaver != null) {
                    valueSaver.run();
                }
            }
        });


        return panel;
    }

    @Override
    public String getInvalidValueMessage(Question q) {
        return null;
    }

    protected void showEmptyQuestion(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 2.0;

        // component to hold the message, with lots of 508 adjustments
        JTextArea txt = new JTextArea(i18n.getString("props.empty.txt"));
        txt.setOpaque(false);
        txt.setEditable(false);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
        txt.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
        AccessibleContext ac = txt.getAccessibleContext();
        ac.setAccessibleName(i18n.getString("props.message.name"));
        ac.setAccessibleDescription(i18n.getString("props.message.desc"));

        panel.add(txt, gbc);
    }

    // UTILITY

    private void addGroup(String group, JPanel panel, ActionListener listener) {
        TableModel model = createTableModel(group, listener);
        // don't show empty tables
        if (model.getRowCount() == 0) {
            return;
        }

        if (tables.isEmpty()) {
            Component box = Box.createVerticalStrut(20);
            box.setFocusable(false);
            panel.add(box);
        }

        // null group is for ungrouped properties
        if (group != null) {
            JLabel label = new JLabel(question.getGroupDisplayName(group));
            label.setName(question.getGroupDisplayName(group));
            //label.setDisplayedMnemonic(i18n.getString("int.sldr.mne").charAt(0));
            //label.setToolTipText(i18n.getString("int.sldr.tip"));
            panel.add(label);
            Component box = Box.createVerticalStrut(5);
            box.setFocusable(false);
            panel.add(box);
        }
        /*
        else {
            Component box = Box.createVerticalStrut(1);
            box.setFocusable(false);
            panel.add(box, c);
        }
        */

        if (renderer == null ||
                renderer.getQuestion() != question) {
            renderer = new RenderingUtilities.PropCellRenderer(question);
        }

        JTable table = createTable(model);
        table.setBorder(BorderFactory.createEtchedBorder());
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.setBackground(UIFactory.Colors.WINDOW_BACKGROUND.getValue());

        // setup key column
        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn tc = table.getColumnModel().getColumn(i);
            tc.setCellRenderer(getCellRenderer(i));
            TableCellEditor editor = getCellEditor(i);
            if (editor != null) {
                tc.setCellEditor(editor);
            }
            tc.setResizable(true);
        }

        //panel.add(new JScrollPane(table), c);
        panel.add(table.getTableHeader());

        panel.add(table);

        Component box = Box.createVerticalStrut(20);
        box.setFocusable(false);
        panel.add(box);

        tables.put(group, table);
    }

    protected TableCellEditor getCellEditor(int column) {
        if (column == 1) {
            return new RenderingUtilities.PCE(question);
        } else {
            return null;
        }
    }

    protected TableCellRenderer getCellRenderer(int column) {
        return renderer;
    }

    protected String[] getTableHeaders() {
        return new String[]{question.getKeyHeaderName(),
                question.getValueHeaderName()};
    }

    protected TableModel createTableModel(String group, ActionListener l) {
        return new PropTableModel(getTableHeaders(), group, question, l);
    }

    protected JTable createTable(TableModel model) {
        return new PropJTable(model);
    }

    private void fireEditedEvent(Object src, ActionListener l) {
        ActionEvent e = new ActionEvent(src,
                ActionEvent.ACTION_PERFORMED,
                EDITED);
        l.actionPerformed(e);
    }

    protected class PropJTable extends JTable {
        protected PropJTable(TableModel model) {
            super(model);
            setIntercellSpacing(new Dimension(4, 4));
            setRowHeight((int) (getRowHeight() * 1.5));
            setFocusable(false);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            if (column == 0) {
                return false;
            }

            if (column == 1 &&
                    question.isReadOnlyValue(question.getKeyPropertyName((String) getValueAt(row, 0)))) {
                return false;
            }

            return true;
        }
    }

    protected class PropTableModel extends DefaultTableModel {
        protected PropertiesQuestion q;

            /*
         String getColumnName(int column) {
             if (column > headers.length - 1)
                 return super.getColumnName();
             else
                 return headers[column];
         }

        String[] headers;
         */
        protected ActionListener editedListener;

        protected PropTableModel(String[] headers, String group, PropertiesQuestion q,
                                 ActionListener listener) {
            super();
            this.q = q;
            editedListener = listener;

            setColumnCount(headers.length);

            String[][] d = q.getGroup(group);

            if (d != null) {
                ArrayList<String> rm = null;
                for (String[] aD1 : d) {
                    if (!q.isEntryVisible(aD1[0])) {
                        if (rm == null) {
                            rm = new ArrayList<>();
                        } else {
                        }
                        rm.add(aD1[0]);
                    } else {
                        // this entry is visible
                    }
                }   // for

                // remove items from d
                if (rm != null) {
                    String[][] d2 = new String[d.length - rm.size()][2];
                    int pos = 0;
                    for (String[] aD : d) {
                        if (rm.contains(aD[0])) {
                            continue;
                        } else {
                            d2[pos][0] = aD[0];
                            d2[pos][1] = aD[1];
                            pos++;
                        }
                        // assert: pos == d2.length
                    }   // loop should fill d2.length!

                    d = d2;
                }

                for (int i = 0; i < d.length; i++) {
                    if (q.getPresentationKeys() != null && q.getPresentationKeys().get(d[i][0]) != null) {
                        d[i][0] = q.getPresentationKeys().get(d[i][0]);
                    }
                }

                setDataVector(d, headers);
            }

            /* old code which doesn't support invisibility
            if (d != null)
                setDataVector(d, headers);
            */
        }

        @Override
        public void setValueAt(Object o, int row, int col) {
            if (col == 1) {
                String key = q.getKeyPropertyName((String) getValueAt(row, 0));

                q.updateProperty(key, (String) o);
                fireEditedEvent(this, editedListener);
                fireTableCellUpdated(row, 0);
                fireTableCellUpdated(row, 1);
                o = q.getValue().get(key);
            }

            super.setValueAt(o, row, col);
        }
    }
}
