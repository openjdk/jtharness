/*
 * $Id$
 *
 * Copyright (c) 2004, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.sun.interview.Interview;
import com.sun.interview.Question;

public class TypeInPanel extends JPanel
{
    public TypeInPanel(String uiKey, final Question q, int fieldWidth,
            String[] suggestions, JButton btn, ActionListener listener) {
        setLayout(new GridBagLayout());
        setName(uiKey);
        setFocusable(false);

        GridBagConstraints c = new GridBagConstraints();
        if (fieldWidth <= 0) {
            c.anchor = GridBagConstraints.WEST;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 1;
        }

        JLabel label = new JLabel(i18n.getString(uiKey + ".lbl"));
        label.setName(uiKey + ".lbl");
        label.setDisplayedMnemonic(i18n.getString(uiKey + ".mne").charAt(0));
        label.setToolTipText(i18n.getString(uiKey + ".tip"));
        add(label, c);

        c.gridwidth = 1;

        if (suggestions == null) {
            field = new JTextField(q.getStringValue());
            field.setName(uiKey + ".txt");
            field.addActionListener(listener);
            field.getDocument().addDocumentListener(new ActionDocListener(field, listener, QuestionRenderer.EDITED));  // uugh dependency on QuestionRenderer
            field.setToolTipText(label.getToolTipText());
            label.setLabelFor(field);

            if (fieldWidth <= 0)
                c.fill = GridBagConstraints.HORIZONTAL;
            else
                field.setColumns(fieldWidth);

            add(field, c);
        }
        else {
            choice = new JComboBox();
            choice.setName(uiKey + ".chc");
            choice.setEditable(true);
            choice.setSelectedItem(q.getStringValue());
            //choice.addActionListener(listener);
            label.setLabelFor(choice);

            Component editComp =  choice.getEditor().getEditorComponent();
            editComp.setFont(editComp.getFont().deriveFont(Font.PLAIN));
            if (editComp instanceof Accessible) {
                if (editComp.getName() == null)
                    editComp.setName(uiKey + ".chc.ed");
                AccessibleContext ed_ac = editComp.getAccessibleContext();
                ed_ac.setAccessibleName(i18n.getString(uiKey + ".chc.ed.name"));
                ed_ac.setAccessibleDescription(i18n.getString(uiKey + ".chc.ed.desc"));
            }

            for (int i = 0; i < suggestions.length; i++)
                choice.addItem(suggestions[i]);

            if (fieldWidth <= 0) {
                c.fill = GridBagConstraints.HORIZONTAL;
                c.weightx = 1;
            }
            else {
                c.anchor = GridBagConstraints.WEST;
                c.weightx = 0;
            }

            add(choice, c);
        }

        if (btn != null) {
            c.insets.left = 10;
            c.weightx = 0;
            add(btn, c);
        }

        Runnable valueSaver = new Runnable() {
                public void run() {
                    try {
                        q.setValue(getValue());
                    }
                    catch (Interview.Fault e) {
                        throw new Error(e);
                    }
                }
            };

        putClientProperty(QuestionRenderer.VALUE_SAVER, valueSaver);
    }

    protected String getValue() {
        if (field != null)
            return field.getText();
        else  {
            if (choice.isEditable()) {
                return choice.getEditor().getItem().toString();
            } else {
                return choice.getSelectedItem().toString();
            }
        }
    }

    protected void setValue(String value) {
        if (field != null)
            field.setText(value);
        else
            choice.setSelectedItem(value);
    }

    protected JTextField field;
    protected JComboBox choice;

    private static final I18NResourceBundle i18n = I18NResourceBundle.getDefaultBundle();
}

