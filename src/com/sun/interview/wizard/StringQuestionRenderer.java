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
package com.sun.interview.wizard;

import com.sun.interview.Question;
import com.sun.interview.StringQuestion;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;

public class StringQuestionRenderer
        implements QuestionRenderer {
    private static final I18NResourceBundle i18n = I18NResourceBundle.getDefaultBundle();

    @Override
    public JComponent getQuestionRendererComponent(Question q, ActionListener listener) {
        StringQuestion sq = (StringQuestion) q;
        int nomMaxLen = sq.getNominalMaxLength();

        if (nomMaxLen > 80) {
            return createTextArea(sq, listener);
        } else {
            return createTypeInPanel(sq, listener);
        }
    }

    @Override
    public String getInvalidValueMessage(Question q) {
        return null;
    }

    protected JPanel createTextArea(StringQuestion q, ActionListener listener) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setName("str");
        panel.setFocusable(false);

        JLabel label = new JLabel(i18n.getString("str.area.lbl"));
        label.setName("str.area.lbl");
        label.setDisplayedMnemonic(i18n.getString("str.area.mne").charAt(0));
        label.setToolTipText(i18n.getString("str.area.tip"));
        panel.add(label, BorderLayout.NORTH);

        JTextArea textArea = new JTextArea(q.getValue());
        textArea.setName("str");
        textArea.setLineWrap(true);
        //textArea.addActionListener(listener);
        textArea.getDocument().addDocumentListener(new ActionDocListener(textArea, listener, EDITED));
        textArea.setToolTipText(label.getToolTipText());
        label.setLabelFor(textArea);

        JScrollPane sp = new JScrollPane(textArea);
        sp.setName("str.sp");
        sp.setFocusable(false);
        panel.add(sp, BorderLayout.CENTER);

        panel.putClientProperty(VALUE_SAVER, createValueSaver(q, textArea));

        return panel;
    }

    protected JPanel createTypeInPanel(StringQuestion q, ActionListener listener) {

        return new TypeInPanel("str",
                q,
                q.getNominalMaxLength(),
                q.getSuggestions(),
                null,
                listener);
    }

    protected Runnable createValueSaver(final StringQuestion q,
                                        final JTextArea textArea) {

        return () -> q.setValue(textArea.getText());
    }
}
