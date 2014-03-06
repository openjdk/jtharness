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
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.help.HelpBroker;
import javax.accessibility.AccessibleContext;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.KeyStroke;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import com.sun.interview.FinalQuestion;
import com.sun.interview.Interview;
import com.sun.interview.Question;

/**
 * A widget comprising the basic panels for a wizard: an index for the
 * questions on the current path, the main question panel, an optional
 * panel to display question-specific help, and an optional button area.
 */
public class WizPane extends JPanel
{
    /**
     * Create a WizPane object for a specified interview.
     * @param i The interview for which this object is to be used.
     */
    public WizPane(Interview i) {
        this(i, true);
    }

    /**
     * Create a WizPane object for a specified interview.
     * @param i The interview for which this object is to be used.
     * @param enableInfo WHether or not this pane should include
     * an embedded help pane to display a question's "more info".
     */
    public WizPane(Interview i, boolean enableInfo) {
        interview = i;
        infoEnabled = enableInfo;
        initGUI();
    }

    /**
     * Set the help broker in which context sensitive help and default menu help
     * is displayed. If not set, a default help broker will be created.
     * @param helpBroker The help broker to use for context sensitive and menu help.
     */
    public void setHelpBroker(HelpBroker helpBroker) {
        helpHelpBroker = helpBroker;
    }

    /**
     * Set the prefix string for the help IDs for context sensitive help and default menu help.
     * If not set, the default is "wizard.".
     * @param helpPrefix A prefix to be used for all context sentive help and menu entries.
     */
    public void setHelpPrefix(String helpPrefix) {
        helpHelpPrefix = helpPrefix;
    }

    /**
     * Get thecurrent button panel, if it has been set, or null if no
     * button panel has been set.
     * @return the button panel if one has been set, or null otherwise
     * @see #setButtonPanel
     */
    public JPanel getButtonPanel() {
        return buttonPanel;
    }

    /**
     * Set a button panel to be displayed underneath the index and question
     * panes.  By default, no such panel is provided or displayed.
     * @param buttonPanel the panel to be displayed under the index
     * and question panel
     * @see #getButtonPanel
     */
    public void setButtonPanel(JPanel buttonPanel) {
        this.buttonPanel = buttonPanel;
        body.add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Determine if the interview in this WizPane has been edited or not.
     * Any pending edits in the question panel are applied first.
     * @return true if this interview is marked as having been edited
     */
    public boolean isEdited() {
        questionPanel.saveCurrentResponse();
        return interview.isEdited();
    }

    /**
     * Determine if this interview has question-specific help available,
     * and hence whether this panel can display such help.
     * @return true if this panel have question-specific help to display,
     * and false otherwise
     */
    public boolean hasInfo() {
        return (infoPanel != null);
    }

    /**
     * Determine if question-specific help is visible within this panel.
     * @return true if question-specific help is visible within
     * this panel, and false otherwise
     * @see #setInfoVisible
     */
    public boolean isInfoVisible() {
        return (main instanceof JSplitPane);
    }

    /**
     * Specify whether question-specific help should be visible within this panel.
     * @param b true if question-specific help should be visible within this panel,
     * and false otherwise
     * @see #isInfoVisible
     */
    public void setInfoVisible(boolean b) {
        // verify there is an infoPanel to be made visible
        if (infoPanel == null)
            throw new IllegalStateException();

        // check if already set as desired
        if (b == isInfoVisible())
            return;

        // get dimensions of body and info panel
        Dimension bodySize = body.getSize();
        if (bodySize.width == 0)
            bodySize = body.getPreferredSize();

        Dimension infoSize = infoPanel.getSize();
        if (infoSize.width == 0)
            infoSize = infoPanel.getPreferredSize();

        // update the wizpane content
        remove(main);

        if (b) {
            // set main to body+info; remove border, because JSplitPane adds in
            // its own padding
            body.setBorder(null);
            JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, body, infoPanel);
            sp.setDividerLocation(bodySize.width - 1);
            main = sp;
            infoPanel.setCurrentID(interview.getCurrentQuestion());
        }
        else {
            // set main to body; add a border to stand in for the padding
            // that JSplitPane would otherwise give
            body.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            main = body;
        }

        add(main);

        // adjust the size of the window up or down as appropriate
        Window window = (Window) (SwingUtilities.getAncestorOfClass(Window.class, this));
        if (window != null) {
            Dimension winSize = window.getSize();
            int divWidth = new JSplitPane().getDividerSize();
            int newWidth = winSize.width;
            newWidth += (b ? +1 : -1) * (infoSize.width + divWidth);
            window.setSize(newWidth, winSize.height);
        }
    }

    /**
     * Check whether the question's tag is visible.
     * @return true if the question's tag is visible, and false otherwise
     */
    public boolean isTagVisible() {
        return questionPanel.isTagVisible();
    }

    /**
     * Specify whether or not the question's tag should be made visible.
     * @param v true if the question's tag should be made visible, and false otherwise
     */
    public void setTagVisible(boolean v) {
        questionPanel.setTagVisible(v);
    }

    /**
     * Check whether or not markers should be enabled.
     * @return whether or not markers should be enabled
     * @see #setMarkersEnabled
     */
    public boolean getMarkersEnabled() {
        return pathPanel.getMarkersEnabled();
    }

    /**
     * Specify whether or not markers should be enabled.
     * @param on whether or not markers should be enabled
     * @see #getMarkersEnabled
     */
    public void setMarkersEnabled(boolean on) {
        pathPanel.setMarkersEnabled(on);
    }

    /**
     * Check whether or not the history list should be filtered to
     * just show questions which have been markered.
     * @return whether or not the  history list should be filtered to
     * just show questions which have been markered
     * @see #setMarkersFilterEnabled
     */
    public boolean getMarkersFilterEnabled() {
        return pathPanel.getMarkersFilterEnabled();
    }

    /**
     * Specify whether or not the history list should be filtered to
     * just show questions which have been markered.
     * @param on whether or not the  history list should be filtered to
     * just show questions which have been markered
     * @see #getMarkersFilterEnabled
     */
    public void setMarkersFilterEnabled(boolean on) {
        pathPanel.setMarkersFilterEnabled(on);
    }

    /**
     * Get a menu of operations to go on a menu bar containing operations
     * for manipulating markers.
     * @return a menu of marker operations
     */
    public JMenu getMarkerMenu() {
        return pathPanel.getMarkerMenu();
    }

    /**
     * Save any pending values currently being displayed in the question panel.
     */
    public void save() {
        questionPanel.saveCurrentResponse();
    }

    /**
     * Step back to the previous question in the interview, after
     * saving any pending edits to the current question.
     * @see Interview#prev
     * @see #prevVisible
     * @see #next
     */
    public void prev() {
        questionPanel.saveCurrentResponse();
        try {
            interview.prev();
        }
        catch (Interview.Fault e) {
            // exception normally means no more questions
            // e.printStackTrace();
        }
    }

    /**
     * Step forward to the previous visible question in the interview path, after
     * saving any pending edits to the current question.
     * @see #prev
     * @see #nextVisible
     */
    public void prevVisible() {
        questionPanel.saveCurrentResponse();
        setCurrentQuestion(pathPanel.getPrevVisible());
    }

    /**
     * Step forward to the next question in the interview, after
     * saving any pending edits to the current question.
     * @see Interview#next
     * @see #nextVisible
     * @see #prev
     */
    public void next() {
        questionPanel.saveCurrentResponse();
        try {
            interview.next();
        }
        catch (Interview.Fault e) {
            // exception normally means no more questions,
            // which should only be  because the value of the current
            // question is invalid
            // e.printStackTrace();
            // questionPanel.getToolkit().beep();
            questionPanel.showValueInvalidMessage();
        }
    }

    /**
     * Step forward to the next visible question in the interview path, after
     * saving any pending edits to the current question.
     * @see #prevVisible
     * @see #next
     */
    public void nextVisible() {
        questionPanel.saveCurrentResponse();
        setCurrentQuestion(pathPanel.getNextVisible());

    }

    /**
     * Step forward to the last question in the interview, after
     * saving any pending edits to the current question.
     * @see #next
     * @see Interview#last
     */
    public void last() {
        questionPanel.saveCurrentResponse();
        try {
            interview.last();
        }
        catch (Interview.Fault e) {
            // exception normally means no more questions,
            // which should only be  because the value of the current
            // question is invalid
            // e.printStackTrace();
            //questionPanel.getToolkit().beep();
            questionPanel.showValueInvalidMessage();
        }
    }

    /**
     * Step forward to the last question in the interview, after
     * saving any pending edits to the current question.
     * @see Interview#last
     * @see #last
     */
    public void lastVisible() {
        questionPanel.saveCurrentResponse();
        Question cq = interview.getCurrentQuestion();
        Question lq = pathPanel.getLastVisible();

        if (lq == cq) {
            if ( !(lq instanceof FinalQuestion))
                questionPanel.showValueInvalidMessage();
        }
        else
            setCurrentQuestion(lq);

    }

    private void setCurrentQuestion(Question q) {
        if (q == null)
            questionPanel.showValueInvalidMessage();
        else {
            try {
                interview.setCurrentQuestion(q);
            }
            catch (Interview.Fault ignore) {
                // should only happen if the question is not on the path,
                // in which case PathPanel gave us bad info
                throw new IllegalStateException();
            }
        }
    }

    /**
     * Display a "search dialog" that may be used to search for a particular
     * question within the interview.
     */
    public void find() {
        if (searchDialog == null)
            searchDialog = SearchDialog.create(this, interview, helpHelpBroker, helpHelpPrefix);
        searchDialog.setVisible(true);
    }

    /**
     * Advance to the next matching question, according to the criteria
     * in the "search dialog" displayed by <code>find</code>. The dialog
     * will be displayed if it has not previously been displayed.
     */
    public void findNext() {
        if (searchDialog == null)
            searchDialog = SearchDialog.create(this, interview, helpHelpBroker, helpHelpPrefix);

        searchDialog.find();
    }

    /**
     * This method invokes when config editor is going to be closed.
     * This made to allow components handle feature closing
     */
    public void prepareClosing() {
        questionPanel.prepareClosing();
    }

    private void initGUI() {
        setInfo(this, "wizPane");
        setLayout(new BorderLayout());

        questionPanel = new QuestionPanel(interview);
        questionPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        questionPanel.setNextAction(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (getMarkersFilterEnabled())
                        nextVisible();
                    else
                        next();
                }
            });

        pathPanel = new PathPanel(questionPanel, interview);
        JScrollPane psp = new JScrollPane(pathPanel,
                                          JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                          JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        psp.setName(pathPanel.getName() + ".sp");
        psp.setFocusable(false);

        body = new JPanel(new BorderLayout());
        body.setName("wizPane.body");
        body.setFocusable(false);
        //body.add(psp, BorderLayout.WEST);

        JScrollPane qsp = new JScrollPane(questionPanel,
                                          JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                          JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        qsp.setName(questionPanel.getName() + ".sp");
        qsp.setFocusable(false);

        //body.add(qsp, BorderLayout.CENTER);

        JSplitPane lsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, psp, qsp);
        body.add(lsp, BorderLayout.CENTER);

        // buttonPanel may be added to the SOUTH

        /*
        body.registerKeyboardAction(listener, DETAILS, detailsKey,
                                           JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        */
        body.registerKeyboardAction(listener, FIND_NEXT, KeyStroke.getKeyStroke("F3"),
                                           JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        if (infoEnabled && interview.getHelpSet() != null)
            infoPanel = new InfoPanel(interview);


        // set wizPane contents to be body+info, or just body
        // could factor initial preference in here
        if (infoPanel == null) {
            body.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            main = body;
        }
        else {
            body.setBorder(null);
            main = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, body, infoPanel);
            setInfo(main, "wizPane.split");
        }

        add(main);
    }

    private void setInfo(JComponent jc, String uiKey) {
        jc.setName(uiKey);
        AccessibleContext ac = jc.getAccessibleContext();
        ac.setAccessibleName(i18n.getString(uiKey + ".name"));
        ac.setAccessibleDescription(i18n.getString(uiKey + ".desc"));
    }

    public void setCustomRenderers(Map customRenderers) {
        questionPanel.setCustomRenderers(customRenderers);
    }

    private class Listener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            /*if (cmd.equals(DETAILS)) {
                 if (detailsBrowser == null) {
                     Window window = (Window) (SwingUtilities.getAncestorOfClass(Window.class, WizPane.this));
                     HelpSet hs = (infoPanel == null ? null : infoPanel.getHelpSet());
                     detailsBrowser = new DetailsBrowser(window, interview, hs);
                 }
                 detailsBrowser.setVisible(true);
             }
             else */ if (cmd.equals(FIND_NEXT)) {
                findNext();
            }
        }
    }
    private Listener listener = new Listener();


    private Interview interview;
    private boolean infoEnabled;

    // help for context sensitive help (F1)
    private HelpBroker helpHelpBroker;
    private String helpHelpPrefix = "wizard.";

    private JComponent main;
    private JPanel body;
    private PathPanel pathPanel;
    private QuestionPanel questionPanel;
    private JPanel buttonPanel;
    private InfoPanel infoPanel;
    private SearchDialog searchDialog;
    /*
    private DetailsBrowser detailsBrowser;
    private static final KeyStroke detailsKey = KeyStroke.getKeyStroke("shift alt D");
    */

    private static String DETAILS = "details";
    private static String FIND_NEXT = "findNext";

    private static final I18NResourceBundle i18n = I18NResourceBundle.getDefaultBundle();
}
