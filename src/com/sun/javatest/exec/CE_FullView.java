/*
 * $Id$
 *
 * Copyright (c) 2002, 2012, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.help.CSH;
import javax.help.Map;
import javax.help.JHelpContentViewer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.sun.interview.ErrorQuestion;
import com.sun.interview.Help;
import com.sun.interview.Interview;
import com.sun.interview.Question;
import com.sun.interview.wizard.WizPane;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.tool.UIFactory;

class CE_FullView extends CE_View
{
    CE_FullView(InterviewParameters config,
                JHelpContentViewer infoPanel, UIFactory uif, ActionListener l) {
        super(config, infoPanel, uif, l);
        initGUI();
    }

    JMenu getMarkerMenu() {
        return wizPane.getMarkerMenu();
    }

    JMenu getSearchMenu() {
        return searchMenu;
    }

    boolean isOKToClose() {
        return true;
    }

    void load() {
        wizPane.setMarkersEnabled(config.getMarkersEnabled());
        wizPane.setMarkersFilterEnabled(config.getMarkersFilterEnabled());
    }

    void save() {
        // this line ensures that all pending edits the user has are
        // committed.  Applies mainly to things which have a editing component
        // that does not write synchronously to the Question, e.g. a cell being
        // edited in a table
        wizPane.prepareClosing();
        wizPane.save();

        config.setMarkersEnabled(wizPane.getMarkersEnabled());
        config.setMarkersFilterEnabled(wizPane.getMarkersFilterEnabled());
    }

    void refresh() {
        config.updatePath();
    }

    boolean isTagVisible() {
        return wizPane.isTagVisible();
    }

    void setTagVisible(boolean v) {
        wizPane.setTagVisible(v);
    }

    private void initGUI() {
        setName(FULL);
        CSH.setHelpIDString(this, "confEdit.fullView.csh");

        String[] searchMenuItems = { FIND, FIND_NEXT };
        searchMenu = uif.createMenu("ce.search", searchMenuItems, localListener);

        setLayout(new BorderLayout());
        initBody();
        initButtons();
    }

    private void initBody() {
        wizPane = new WizPane(config, false);
        wizPane.setHelpPrefix("confEdit.");
        wizPane.setHelpBroker(uif.getHelpBroker());
        wizPane.addAncestorListener(localListener);
        add(wizPane, BorderLayout.CENTER);
    }

    protected void setCustomRenderers(java.util.Map customRenderers) {
        wizPane.setCustomRenderers(customRenderers);
    }


    private void initButtons() {
        JPanel btnPanel = uif.createPanel("ce.full.btns", new GridBagLayout(), false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets.top = 5;
        c.insets.bottom = 11;  // value from JL&F Guidelines
        c.insets.right = 5;    // value from JL&F Guidelines
        c.insets.left = 11;

        // Message Area, grow to fit
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        msgField = uif.createOutputField("ce.msgs");
        msgField.setBorder(null);
        msgField.setEnabled(false);
        completeMsg = uif.getI18NString("ce.msgs.complete");
        incompleteMsg = uif.getI18NString("ce.msgs.incomplete");
        btnPanel.add(msgField, c);

        // Back
        c.weightx = 0;
        c.insets.left = 0;
        backBtn = uif.createButton("ce.full.back", localListener, BACK);
        backBtn.setIcon(uif.createIcon("ce.full.back"));
        backBtn.setHorizontalTextPosition(JLabel.TRAILING);
        btnPanel.add(backBtn, c);
        // Next
        nextBtn = uif.createButton("ce.full.next", localListener, NEXT);
        nextBtn.setIcon(uif.createIcon("ce.full.next"));
        nextBtn.setHorizontalTextPosition(JLabel.LEADING);
        btnPanel.add(nextBtn, c);
        // Last
        lastBtn = uif.createButton("ce.full.last", localListener, LAST);
        lastBtn.setIcon(uif.createIcon("ce.full.last"));
        lastBtn.setHorizontalTextPosition(JLabel.LEADING);
        btnPanel.add(lastBtn, c);
        // short gap, then Done
        c.insets.left = 20;
        c.insets.right = 11;    // value from JL&F Guidelines
        JButton doneBtn = uif.createButton("ce.done", listener, DONE);
        btnPanel.add(doneBtn, c);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void showInfoForQuestion(Question q) {
        if (q instanceof ErrorQuestion)
            return;

        Map.ID id = Help.getHelpID(q);
        // uugh
        if (id == null)
            System.err.println("WARNING: no help for " + q.getKey());
        else
            showInfo(id);
    }

    private class Listener
        implements ActionListener, AncestorListener, Interview.Observer
    {
        // ---------- from ActionListener -----------

        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (cmd.equals(NEXT)) {
                // hmm, arguably, if the filter is *not* enabled,
                // then nextVisible() should be the same as next() anyway
                if (wizPane.getMarkersFilterEnabled())
                    wizPane.nextVisible();
                else
                    wizPane.next();
            }
            else if (cmd.equals(BACK)) {
                // hmm, arguably, if the filter is *not* enabled,
                // then nextVisible() should be the same as next() anyway
                if (wizPane.getMarkersFilterEnabled())
                    wizPane.prevVisible();
                else
                    wizPane.prev();
            }
            else if (cmd.equals(LAST)) {
                // hmm, arguably, if the filter is *not* enabled,
                // then nextVisible() should be the same as next() anyway
                if (wizPane.getMarkersFilterEnabled())
                    wizPane.lastVisible();
                else
                    wizPane.last();
            }
            else if (cmd.equals(FIND)) {
                wizPane.find();
            }
            else if (cmd.equals(FIND_NEXT)) {
                wizPane.findNext();
            }
        }

        // ---------- from AncestorListener -----------

        public void ancestorAdded(AncestorEvent e) {
            config.addObserver(this);
            pathUpdated();
            currentQuestionChanged(config.getCurrentQuestion());
        }

        public void ancestorMoved(AncestorEvent e) { }

        public void ancestorRemoved(AncestorEvent e) {
            config.removeObserver(this);
        }

        // ---------- from Interview.Observer -------

        public void pathUpdated() {
            String msg = (config.isFinishable() ? completeMsg : incompleteMsg);
            if (msg == null || msg.length() == 0) {
                msgField.setEnabled(false);
                msgField.setText("");
            }
            else {
                msgField.setEnabled(true);
                msgField.setText(msg);
            }

        }

        public void currentQuestionChanged(Question q) {
            boolean first = config.isFirst(q);
            boolean last = config.isLast(q);
            backBtn.setEnabled(!first);
            nextBtn.setEnabled(!last);
            lastBtn.setEnabled(!last);

            if (isInfoVisible())
                showInfoForQuestion(q);
        }
    };


    /**
     * This method invokes when config editor is going to be closed.
     * This made to allow components handle feature closing
     */
    public void prepareClosing() {
        wizPane.prepareClosing();
    }

    private JMenu searchMenu;
    private WizPane wizPane;
    private JTextField msgField;
    private String completeMsg;
    private String incompleteMsg;
    private JButton backBtn;
    private JButton nextBtn;
    private JButton lastBtn;
    private Listener localListener = new Listener();

    private static final String BACK = "back";
    private static final String NEXT = "next";
    private static final String LAST = "last";
    private static final String FIND = "find";
    private static final String FIND_NEXT = "findNext";
}
