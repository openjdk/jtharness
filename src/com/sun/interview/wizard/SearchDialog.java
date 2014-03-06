/*
 * $Id$
 *
 * Copyright (c) 1996, 2009, Oracle and/or its affiliates. All rights reserved.
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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import javax.accessibility.AccessibleContext;
import javax.help.HelpBroker;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import com.sun.interview.Interview;
import com.sun.interview.Question;

class SearchDialog extends JDialog
{
    static SearchDialog create(Component parent, Interview i,
                               HelpBroker helpBroker, String helpPrefix) {
        Dialog d = (Dialog)(SwingUtilities.getAncestorOfClass(Dialog.class, parent));
        if (d != null)
            return new SearchDialog(d, i, helpBroker, helpPrefix);
        else {
            Frame f = (Frame)(SwingUtilities.getAncestorOfClass(Frame.class, parent));
            return new SearchDialog(f, i, helpBroker, helpPrefix);
        }
    }

    SearchDialog(Frame parent, Interview i, HelpBroker helpBroker, String helpPrefix) {
        super(parent);
        init(parent, i, helpBroker, helpPrefix);
    }

    SearchDialog(Dialog parent, Interview i, HelpBroker helpBroker, String helpPrefix) {
        super(parent);
        init(parent, i, helpBroker, helpPrefix);
    }

    private void init(Component parent, Interview i, HelpBroker helpBroker, String helpPrefix) {
        interview = i;
        this.helpBroker = helpBroker;
        this.helpPrefix = helpPrefix;

        setName("find");
        setTitle(i18n.getString("find.title"));
        AccessibleContext ac = getAccessibleContext();
        ac.setAccessibleName(i18n.getString("find.name"));
        ac.setAccessibleDescription(i18n.getString("find.desc"));

        Container contentPane = getContentPane();
        contentPane.setName("content");
        contentPane.setFocusable(false);
        contentPane.setLayout(new BorderLayout());

        JPanel p = new JPanel(new GridBagLayout());
        p.setName("main");
        p.setFocusable(false);

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.EAST;
        lc.insets.right = 10;

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;

        JLabel textLbl = createLabel("find.field", true);
        p.add(textLbl, lc);

        textField = new JTextField("", 32);
        textField.setName("find.text");
        textField.addActionListener(listener);
        textField.setActionCommand(FIND);
        textField.setToolTipText(textLbl.getAccessibleContext().getAccessibleDescription());
        textLbl.setLabelFor(textField);
        c.insets.bottom = 10;
        p.add(textField, c);

        JLabel whereLbl = createLabel("find.where", true);
        p.add(whereLbl, lc);

        String[] choices = {TITLE, QUESTION, ANSWER, ANYWHERE};
        whereChoice = createChoice("find.where", choices);
        whereLbl.setLabelFor(whereChoice);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.insets.bottom = 0;
        p.add(whereChoice, c);

        JLabel optionsLbl = createLabel("find.options", false);
        p.add(optionsLbl, lc);

        JPanel p2 = new JPanel();
        p2.setName("options");
        p2.setFocusable(false);

        caseChk = createCheckBox("find.case");
        p2.add(caseChk);
        wordChk = createCheckBox("find.word");
        p2.add(wordChk);
        p.add(p2, c);

        p.registerKeyboardAction(listener, CLOSE, escapeKey,
                                 JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        p.setBorder(BorderFactory.createEmptyBorder(/*top*/12, /*left*/12, /*bottom*/0, /*right*/11));

        contentPane.add(p, BorderLayout.CENTER);

        JPanel btns = new JPanel(new GridBagLayout());
        btns.setName("btns");
        btns.setFocusable(false);
        c = new GridBagConstraints();
        c.weightx = 1;
        c.anchor = GridBagConstraints.EAST;
        btns.add(createButton("find.find", FIND, listener), c);
        c.insets.left = 5;
        c.weightx = 0;
        btns.add(createButton("find.close", CLOSE, listener), c);
        if (helpBroker != null)
            btns.add(createButton("find.help", HELP, listener), c);

        btns.setBorder(BorderFactory.createEmptyBorder(/*top*/11, /*left*/12, /*bottom*/11, /*right*/11));

        contentPane.add(btns, BorderLayout.SOUTH);

        if (helpBroker != null)
            helpBroker.enableHelpKey(getRootPane(), helpPrefix + "search.csh", null);

        pack();
        setLocationRelativeTo(parent);
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b)
            textField.requestFocus();
    }

    public void find() {
        if (textField.getText().length() == 0)
            setVisible(true);
        else {
            // just get this once for the query
            Hashtable answers = new Hashtable();
            interview.save(answers);
            Question[] path = interview.getPath();
            Question curr = interview.getCurrentQuestion();
            for (int i = 0; i < path.length; i++) {
                if (path[i] == curr) {
                    for (int j = 0; j < path.length; j++) {
                        Question q = path[(i + 1 + j)%path.length];
                        String answer = (String)(answers.get(q.getTag()));
                        if (matches(q, answer, caseChk.isSelected(), wordChk.isSelected())) {
                            try {
                                interview.setCurrentQuestion(q);
                            }
                            catch (Interview.Fault e) {
                            }
                            return;
                        }
                    }
                    getToolkit().beep();
                    return;
                }
            }
        }
    }

    private boolean matches(Question q, String answer, boolean considerCase, boolean word) {
        String text = textField.getText();
        String where = (String)(whereChoice.getSelectedItem());
        if (where.equals(TITLE))
            return match(text, q.getSummary(), considerCase, word);
        else if (where.equals(QUESTION))
            return match(text, q.getText(), considerCase, word);
        else if (where.equals(ANSWER))
            return (answer != null && match(text, answer, considerCase, word));
        else
            return ( match(text, q.getSummary(), considerCase, word) ||
                     match(text, q.getText(), considerCase, word) ||
                     (answer != null && match(text, answer, considerCase, word)) );
    }

    private static boolean match(String s1, String s2, boolean considerCase, boolean word) {
        int s1len = s1.length();
        int s2len = s2.length();
        for (int i = 0; i <= s2len - s1len; i++) {
            if (s1.regionMatches(!considerCase, 0, s2, i, s1len)) {
                if (word)
                    return ( (i == 0 || isBoundaryCh(s2.charAt(i-1)))
                             && (i+s1len == s2.length() || isBoundaryCh(s2.charAt(i+s1len))) );
                else
                    return true;
            }
        }
        return false;
    }

    private static boolean isBoundaryCh(char c) {
        return !(Character.isUnicodeIdentifierStart(c)
                 || Character.isUnicodeIdentifierPart(c));
    }

    private JButton createButton(String uiKey, String actionCommand, ActionListener l) {
        JButton b = new JButton(i18n.getString(uiKey + ".btn"));
        b.setName(uiKey);
        b.setMnemonic(getMnemonic(uiKey));
        b.setActionCommand(actionCommand);
        b.addActionListener(l);
        setToolTipText(b, uiKey);
        //b.registerKeyboardAction(listener, actionCommand, enterKey, JComponent.WHEN_FOCUSED);
        return b;
    }

    private JCheckBox createCheckBox(String uiKey) {
        JCheckBox b = new JCheckBox(i18n.getString(uiKey + ".ckb"));
        b.setName(uiKey);
        b.setMnemonic(getMnemonic(uiKey));
        setToolTipText(b, uiKey);
        return b;
    }

    private JComboBox createChoice(final String uiKey, final String[] choiceKeys) {
        // create a cache of the presentation string, for use when
        // rendering, but otherwise, let the JComboBox work in terms of the
        // choiceKeys
        final String[] choices = new String[choiceKeys.length];
        for (int i = 0; i < choices.length; i++)
            choices[i] = i18n.getString(uiKey + "." + choiceKeys[i] + ".chc");

        JComboBox choice = new JComboBox(choiceKeys);
        choice.setName(uiKey);
        AccessibleContext ac = choice.getAccessibleContext();
        ac.setAccessibleName(i18n.getString(uiKey + ".tip"));

        choice.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object o, int index, boolean isSelected, boolean cellHasFocus) {
                Object c = o;
                for (int i = 0; i < choiceKeys.length; i++) {
                    if (choiceKeys[i] == o) {
                        c = choices[i];
                        break;
                    }
                }
                return super.getListCellRendererComponent(list, c, index, isSelected, cellHasFocus);
            }
        });

        setToolTipText(choice, uiKey);
        ac.setAccessibleDescription(choice.getToolTipText());
        return choice;
    }

    private JLabel createLabel(String uiKey, boolean needMnemonic) {
        JLabel l = new JLabel(i18n.getString(uiKey + ".lbl"));
        l.setName(uiKey);
        if (needMnemonic)
            l.setDisplayedMnemonic(getMnemonic(uiKey));
        setToolTipText(l, uiKey);
        return l;
    }

    private char getMnemonic(String uiKey) {
        return i18n.getString(uiKey + ".mne").charAt(0);
    }

    private void setToolTipText(JComponent c, String uiKey) {
        c.setToolTipText(i18n.getString(uiKey + ".tip"));
    }

    private ActionListener listener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (cmd.equals(FIND))
                find();
            else if (cmd.equals(CLOSE))
                setVisible(false);
            else if (cmd.equals(HELP)) {
                helpBroker.setCurrentID(helpPrefix + "search.csh");
                helpBroker.setDisplayed(true);
            }
        }
    };

    private Interview interview;
    private Question currentQuestion;
    private HelpBroker helpBroker;
    private String helpPrefix;
    private JTextField textField;
    private JComboBox whereChoice;
    private JCheckBox caseChk;
    private JCheckBox wordChk;

    private static final String ANSWER = "answer";
    private static final String ANYWHERE = "anywhere";
    private static final String CLOSE = "close";
    private static final String FIND = "find";
    private static final String HELP = "help";
    private static final String QUESTION = "question";
    private static final String TITLE = "title";
    private static final KeyStroke escapeKey = KeyStroke.getKeyStroke("ESCAPE");

    private static final I18NResourceBundle i18n = I18NResourceBundle.getDefaultBundle();
}
