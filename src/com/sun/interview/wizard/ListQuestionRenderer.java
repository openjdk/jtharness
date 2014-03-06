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

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.sun.interview.Interview;
import com.sun.interview.ListQuestion;
import com.sun.interview.Question;

public class ListQuestionRenderer
    implements QuestionRenderer
{
    public JComponent getQuestionRendererComponent(Question qq, ActionListener listener) {
        ListQuestion q = (ListQuestion) qq;
        if (q.isEnd())
            return createMoreButton(q); // no need for listener since no data input fields
        else
            return createList(q, listener);
    }

    public String getInvalidValueMessage(Question qq) {
        ListQuestion q = (ListQuestion) qq;
        return i18n.getString("list.invalid", new Integer(q.getIncompleteBodyCount()));
    }

    protected JComponent createMoreButton(final ListQuestion q) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setName("more");
        panel.setFocusable(false);

        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.SOUTHEAST;
        JButton btn = new JButton(i18n.getString("list.more.btn"));
        btn.setName("list.more.btn");
        btn.setMnemonic(i18n.getString("list.more.mne").charAt(0));
        btn.setToolTipText(i18n.getString("list.more.tip"));
        btn.addActionListener(createMoreBtnListener(q));
        panel.add(btn, c);
        return panel;
    }

    protected ActionListener createMoreBtnListener(final ListQuestion q) {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ListQuestion listStart = q.getOther();
                /* suppress automatic new
                ListQuestion.Body[] bodies = listStart.getBodies();
                ListQuestion.Body[] newBodies = new ListQuestion.Body[bodies.length + 1];
                System.arraycopy(bodies, 0, newBodies, 0, bodies.length);
                newBodies[newBodies.length - 1] = listStart.createBody(newBodies.length - 1);
                listStart.setBodies(newBodies, newBodies.length - 1);
                */
                try {
                    getRootInterview(q).setCurrentQuestion(listStart);
                }
                catch (Interview.Fault ignore) {
                }
            }
        };
    }

    protected JComponent createList(final ListQuestion q, ActionListener listener) {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setName("list");
        panel.setFocusable(false);

        JLabel label = new JLabel(i18n.getString("list.lbl"));
        label.setName("list.lbl");
        label.setDisplayedMnemonic(i18n.getString("list.mne").charAt(0));
        label.setToolTipText(i18n.getString("list.tip"));
        panel.add(label, BorderLayout.NORTH);

        final EditableList list = new BodyList(q);
        ListQuestion.Body seln = q.getSelectedBody();
        if (seln != null)
            list.setSelectedItem(seln);
        label.setLabelFor(list);

        panel.add(list, BorderLayout.CENTER);

        /* no need for valueSaver since list update is immediate
        Runnable valueSaver = new Runnable() {
            public void run() {
                ListQuestion.Body[] bodies = list.getBodies();
                q.setBodies(bodies, list.getSelectedIndex());
                q.setValue(list.getSelectedIndex());
            }
        };

        panel.putClientProperty(VALUE_SAVER, valueSaver);
        */

        return panel;
    }

    protected static Interview getRootInterview(Question q) {
        Interview i = q.getInterview();
        while (i.getParent() != null)
            i = i.getParent();
        return i;
    }

    private static final I18NResourceBundle i18n = I18NResourceBundle.getDefaultBundle();

    protected class BodyList extends EditableList {
        BodyList(ListQuestion q) {
            super("list", q.getBodies());
            question = q;
        }

        protected Object getDisplayValue(Object item) {
            if (item instanceof ListQuestion.Body) {
                ListQuestion.Body b = (ListQuestion.Body) item;
                String s = b.getSummary();
                if (s == null)
                    s = b.getDefaultSummary();

                if (b.isBodyFinishable())
                    return s;
                else
                    return "<html><b><font color=\"" + INVALID_VALUE_COLOR + "\">*</font></b> "
                        + escape(s)
                        + " <font color=\"" + INVALID_VALUE_COLOR + "\">"
                        + escape(INVALID_VALUE)
                        + "</font></html>";
            }
            else
                return item;
        }

        protected Object getNewItem() {
            // we know that the newly created item will be inserted
            // after the current selection if there is one,
            // or at the end of the list
            int i = getSelectedIndex();
            if (i == -1)
                i = getItemCount();
            return question.createBody(i);
        }

        protected Object getNewItem(Object item) {
            question.setValue(getSelectedIndex());
            try {
                getRootInterview(question).next();
            }
            catch (Interview.Fault ignore) {
            }
            return null;
        }

        protected void insertItem() {
            super.insertItem();
            itemsChanged(); // pre-empt EditableList call, since we want to do auto-next
            try {
                getRootInterview(question).next();
            }
            catch (Interview.Fault ignore) {
            }
        }


        protected void itemsChanged() {
            question.setBodies(getBodies(), getSelectedIndex());
        }

        protected void selectedItemChanged() {
            itemsChanged();
            question.setValue(getSelectedIndex());
        }

        private ListQuestion.Body[] getBodies() {
            ListQuestion.Body[] bodies = (ListQuestion.Body[]) (getItems(ListQuestion.Body.class));
            return bodies;
        }

        private String escape(String text) {
            if (text == null)
                return "";

            // check to see if there are any special characters
            boolean specialChars = false;
            for (int i = 0; i < text.length() && !specialChars; i++) {
                switch (text.charAt(i)) {
                case '<': case '>': case '&':
                    specialChars = true;
                }
            }

            // if there are special characters write the string character at a time;
            // otherwise, write it out as is
            if (specialChars) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < text.length(); i++) {
                    char c = text.charAt(i);
                    switch (c) {
                    case '<': sb.append("&lt;"); break;
                    case '>': sb.append("&gt;"); break;
                    case '&': sb.append("&amp;"); break;
                    default: sb.append(c);
                    }
                }
                return sb.toString();
            }
            else
                return text;
        }

        private ListQuestion question;
    }

    protected static final String INVALID_VALUE = i18n.getString("list.incomplete.txt");
    protected static final String INVALID_VALUE_COLOR = i18n.getString("i18n.error.clr");
}
