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

import com.sun.interview.ChoiceQuestion;
import com.sun.interview.Question;

import javax.swing.AbstractCellEditor;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.CellEditor;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.util.Objects;

public class ChoiceQuestionRenderer
        implements QuestionRenderer {

    private static final I18NResourceBundle i18n = I18NResourceBundle.getDefaultBundle();
    protected ChoiceQuestion q;
    protected String[] displayChoices;
    protected String[] values;
    protected int starts_from;
    protected ActionListener editedListener;
    protected JRadioButton[] rb;

    @Override
    public JComponent getQuestionRendererComponent(Question qq, ActionListener listener) {
        q = (ChoiceQuestion) qq;
        displayChoices = q.getDisplayChoices();
        values = q.getChoices();
        starts_from = values[0] == null ? 1 : 0;
        editedListener = listener;

        return createChoiceTable();
    }

    @Override
    public String getInvalidValueMessage(Question q) {
        return null;
    }

    protected JComponent createChoiceTable() {

        rb = new JRadioButton[displayChoices.length - starts_from];
        JPanel itemsPanel = new JPanel();
        BoxLayout layout = new BoxLayout(itemsPanel, BoxLayout.Y_AXIS);
        itemsPanel.setLayout(layout);
        ButtonGroup bg = new ButtonGroup();

        String v = q.getValue();
        for (int i = 0; i < rb.length; i++) {

            rb[i] = new JRadioButton(displayChoices[i + starts_from],
                    Objects.equals(values[i + starts_from], v));
            rb[i].setActionCommand(values[i + starts_from]);

            rb[i].setName("chc.btn." + values[i + starts_from]);
            if (i < 10) {
                rb[i].setMnemonic('0' + i);
            }

            rb[i].setToolTipText(i18n.getString("chc.btn.tip"));
            rb[i].getAccessibleContext().setAccessibleName(rb[i].getName());
            rb[i].getAccessibleContext().setAccessibleDescription(rb[i].getToolTipText());

            rb[i].setOpaque(false);

            rb[i].setFocusPainted(true);
            bg.add(rb[i]);

            final int index = i;
            rb[i].addActionListener(e -> {
                q.setValue(values[index + starts_from]);
                fireEditedEvent(rb[index], editedListener);
            });
            itemsPanel.add(rb[i]);
        }


        JScrollPane sp = new JScrollPane(itemsPanel);
        sp.setName("chcArr.sp");

        JLabel lbl = new JLabel(i18n.getString("chcArr.tbl.lbl"));
        lbl.setName("chcArr.tbl.lbl");
        lbl.setDisplayedMnemonic(i18n.getString("chcArr.tbl.mne").charAt(0));
        lbl.setToolTipText(i18n.getString("chcArr.tbl.tip"));
        lbl.setLabelFor(sp);

        JPanel result = new JPanel(new BorderLayout());
        result.add(lbl, BorderLayout.NORTH);
        result.add(sp, BorderLayout.CENTER);

        return result;
    }

    protected void fireEditedEvent(Object src, ActionListener l) {
        ActionEvent e = new ActionEvent(src,
                ActionEvent.ACTION_PERFORMED,
                EDITED);
        l.actionPerformed(e);
    }
}
