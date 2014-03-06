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
package com.sun.javatest.exec;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.HashSet;
import javax.help.CSH;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Caret;

import com.sun.javatest.InterviewParameters;
import com.sun.javatest.Keywords;
import com.sun.javatest.TestSuite;
import com.sun.javatest.Parameters.KeywordsParameters;
import com.sun.javatest.Parameters.MutableKeywordsParameters;
import com.sun.javatest.tool.UIFactory;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import javax.swing.JDialog;
import javax.swing.WindowConstants;

/**
 * Standard values, keyword pane.
 */
class CE_KeywordsPane extends CE_StdPane {
    CE_KeywordsPane(UIFactory uif, InterviewParameters config) {
        super(uif, config, "keywords");

        updateConfig();
        initGUI();
    }

    boolean isOKToClose() {
        if (mutableKeywordsParameters == null)
            return true;

        if (!selectCheck.isSelected())
            return true;

        try {
            TestSuite ts = config.getTestSuite();
            String[] validKeywords = ts.getKeywords();
            HashSet validKeywordsSet;
            if (validKeywords == null)
                validKeywordsSet = null;
            else
                validKeywordsSet = new HashSet(Arrays.asList(validKeywords));

            String kc = (String) (keywordsChoice.getSelectedItem());
            String type;
            if (kc.equals(ANY_OF))
                type = Keywords.ANY_OF;
            else if (kc.equals(ALL_OF))
                type = Keywords.ALL_OF;
            else
                type = Keywords.EXPR;
            Keywords k = Keywords.create(type, keywordsField.getText(),
                    validKeywordsSet);
            return true;
        } catch (Keywords.Fault e) {
            uif.showError("ce.keywords.invalid", e.getMessage());
            return false;
        }
    }

    void updateConfig() {
        // track interview changes
        keywordsParameters = config.getKeywordsParameters();
        if (keywordsParameters != null
                && keywordsParameters instanceof MutableKeywordsParameters)
            mutableKeywordsParameters = (MutableKeywordsParameters) keywordsParameters;
        else
            mutableKeywordsParameters = null;
    }

    void load() {
        updateConfig();

        // update GUI
        if (mutableKeywordsParameters != null) {
            selectCheck.setEnabled(true);
            setEnabled(true);
            int km = mutableKeywordsParameters.getKeywordsMode();
            selectCheck.setSelected(km == MutableKeywordsParameters.MATCH_KEYWORDS);

            int kmm = mutableKeywordsParameters.getMatchKeywordsMode();
            switch (kmm) {
                case MutableKeywordsParameters.ANY_OF:
                    keywordsChoice.setSelectedItem(ANY_OF);
                    break;

                case MutableKeywordsParameters.ALL_OF:
                    keywordsChoice.setSelectedItem(ALL_OF);
                    break;

                case MutableKeywordsParameters.EXPR:
                    keywordsChoice.setSelectedItem(EXPR);
                    break;
            }

            String kmv = mutableKeywordsParameters.getMatchKeywordsValue();
            keywordsField.setText(kmv == null ? "" : kmv);
        } else if (keywordsParameters != null) {
            setEnabled(true);
            String expr = keywordsParameters.getKeywords().toString();
            selectCheck.setSelected(expr.length() == 0);
            selectCheck.setEnabled(false);

            keywordsChoice.setSelectedItem(EXPR);
            keywordsField.setText(expr);
            keywordsChoice.setEnabled(false);
        } else {
            setEnabled(false); // may be too late to hide tab
            selectCheck.setEnabled(false);

            keywordsChoice.setSelectedItem(EXPR);
            keywordsField.setText("");
            keywordsChoice.setEnabled(false);
        }

        enableKeywordFields();
    }

    void save() {
        if (mutableKeywordsParameters != null) {
            int km = (selectCheck.isSelected()
            ? MutableKeywordsParameters.MATCH_KEYWORDS
                    : MutableKeywordsParameters.NO_KEYWORDS);
            mutableKeywordsParameters.setKeywordsMode(km);

            String kc = (String) (keywordsChoice.getSelectedItem());
            int kmm;
            if (kc.equals(ANY_OF))
                kmm = MutableKeywordsParameters.ANY_OF;
            else if (kc.equals(ALL_OF))
                kmm = MutableKeywordsParameters.ALL_OF;
            else
                kmm = MutableKeywordsParameters.EXPR;
            mutableKeywordsParameters.setMatchKeywords(kmm, keywordsField.getText());
        }
    }

    private void initGUI() {
        CSH.setHelpIDString(this, "confEdit.keywordsTab.csh");

        JPanel p = uif.createPanel("ce.keywords", new BorderLayout(), false);

        // ----- body -----
        JPanel body = uif.createPanel("ce.keywords.body",
                new GridBagLayout(),
                false);
        body.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));

        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        selectCheck = uif.createCheckBox("ce.keywords.select");
        selectCheck.addChangeListener(listener);
        body.add(selectCheck, c);

        String[] kc = {ANY_OF, ALL_OF, EXPR};
        keywordsChoice = uif.createChoice("ce.keywords.choice", kc);
        keywordsChoice.addItemListener(listener);
        c.gridwidth = 1;
        c.insets.left = 17;
        c.weightx = 0;
        body.add(keywordsChoice, c);

        keywordsField = uif.createInputField("ce.keywords.field", 20);
        uif.setAccessibleName(keywordsField, "ce.keywords.field");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets.left = 0;
        c.weightx = 1;
        body.add(keywordsField, c);

        p.add(body, BorderLayout.CENTER);

        // ----- foot -----
        JPanel foot = uif.createPanel("ce.keywords.foot",
                new GridBagLayout(),
                false);
        foot.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        Icon dropDownIcon = new DropDownIcon();

        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        c.insets.left = 5;
        c.gridwidth = 1;
        keywordBtn = uif.createButton("ce.keywords.keyword", listener);
        keywordBtn.setIcon(dropDownIcon);
        keywordBtn.setHorizontalTextPosition(JLabel.LEADING);
        foot.add(keywordBtn, c);

        c.weightx = 0;
        opBtn = uif.createButton("ce.keywords.op", listener);
        opBtn.setIcon(dropDownIcon);
        opBtn.setHorizontalTextPosition(JLabel.LEADING);
        foot.add(opBtn, c);
        p.add(foot, BorderLayout.SOUTH);

        addBody(p);

        String[] opItems = { AND, OR, NOT, PARENS };
        opPopup = uif.createPopupMenu("ce.keywords.op", opItems, listener);

        keywords = config.getTestSuite().getKeywords();

        if (keywords != null) {
            if (keywords.length == 0) {
                // this test suite explicitly does not support keywords,
                // so disable the entire pane
                setEnabled(false);
            } else {
                // this test suite specifies valid keywords,
                // so create a popup menu for them
                keywordPopup = uif.createPopupMenu("ce.keywords.keyword");
                keywordChooser = false;
                for (int i = 0; i < keywords.length; i++)
                    keywordPopup.add(uif.createLiteralMenuItem(keywords[i], listener));
                if (keywordPopup.getPreferredSize().getHeight() > (Toolkit.getDefaultToolkit().getScreenSize().height - 50)) {
                    keywordPopup = null;
                    keywordChooser = true;
                }
            }
        }
    }

    private class KeywordChooser extends JDialog {

        private JScrollPane sp;
        private JList lst;
        private JButton ok, cancel;

        KeywordChooser(Dialog parent, String [] keywords, Listener listener ) {
            super(parent);
            initUI(keywords, listener);
        }

        KeywordChooser(Frame parent, String [] keywords, Listener listener ) {
            super(parent);
            initUI(keywords, listener);
        }

        KeywordChooser(String [] keywords, Listener listener ) {
            super();
            initUI(keywords, listener);
        }

        private void initUI(String [] keywords, final Listener listener ) {
            GridBagConstraints gbc;

            sp = new JScrollPane();
            lst = new JList(keywords);

            ok = uif.createButton("keywordChooser.insert", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Object[] sels = lst.getSelectedValues();
                    String inserted = "";
                    for (int i = 0; i < sels.length; i++) {
                        inserted += sels[i] + " ";
                    }
                    listener.insert(inserted.trim());
                    lst.clearSelection();
                    setVisible(false);
                    dispose();
                }
            });


            cancel = uif.createButton("keywordChooser.cancel", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    lst.clearSelection();
                    setVisible(false);
                    dispose();
                }
            }) ;

            JPanel btnP = new JPanel();
            btnP.setLayout(new GridLayout(1,2,5,0));
            btnP.add(cancel);
            btnP.add(ok);

            getContentPane().setLayout(new GridBagLayout());

            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            setModal(true);
            setUndecorated(true);
            JPopupMenu samplePopup = new JPopupMenu();
            getRootPane().setBorder(samplePopup.getBorder());

            sp.setViewportView(lst);
            lst.setBackground(samplePopup.getBackground());

            gbc = new GridBagConstraints();
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.insets = new Insets(5, 5, 5, 5);
            getContentPane().add(sp, gbc);
            gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.anchor = GridBagConstraints.SOUTHEAST;
            gbc.insets = new Insets(0, 5, 5, 5);
            getContentPane().add(btnP, gbc);
            pack();
            Dimension s = getPreferredSize();
            s.height = (int) (Toolkit.getDefaultToolkit().getScreenSize().height * 0.75);
            ok.setEnabled(false);
            lst.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    ok.setEnabled(lst.getSelectedIndex() != -1);
                }
            });

            setSize(s);
        }

        private void setLocationRelativeToXY(Component xC, Component yC) {
            setLocationRelativeTo(xC);
            Point xP = getLocation();
            setLocationRelativeTo(yC);
            Point yP = getLocation();
            setLocation(new Point(xP.x, yP.y));
        }

    }


    private void enableKeywordFields() {
        boolean b = selectCheck.isEnabled() && selectCheck.isSelected();
        keywordsChoice.setEnabled(b);
        keywordsField.setEnabled(b);
        opBtn.setEnabled(b && keywordsChoice.getSelectedItem().equals(EXPR));
        keywordBtn.setEnabled(b && (keywordPopup != null || keywordChooser));
    }

    private KeywordsParameters keywordsParameters;
    private MutableKeywordsParameters mutableKeywordsParameters;
    private JCheckBox selectCheck;
    private JComboBox keywordsChoice;
    private JTextField keywordsField;
    private JButton keywordBtn;
    private JPopupMenu keywordPopup;
    private JButton opBtn;
    private JPopupMenu opPopup;
    private Listener listener = new Listener();

    private static final String ALL_OF = "allOf";
    private static final String ANY_OF = "anyOf";
    private static final String EXPR   = "expr";
    private static final String IGNORE = "ignore";

    private static final String AND = "and";
    private static final String OR = "or";
    private static final String NOT = "not";
    private static final String PARENS = "parens";

    private boolean keywordChooser;
    private String[] keywords;

    private class Listener
            implements ActionListener, ChangeListener, ItemListener {
        // ---------- ActionListener -----------------------------------

        public void actionPerformed(ActionEvent e) {
            Component src = (Component) (e.getSource());
            String cmd = e.getActionCommand();
            if (src == keywordBtn) {
                if (keywordPopup != null) {
                    show(keywordBtn, keywordPopup);
                } else if (keywordChooser) {
                    KeywordChooser kc;
                    Container parent = toolDialog.getDialogParent();
                    if (parent instanceof Dialog) {
                        kc = new KeywordChooser((Dialog)parent, keywords, listener);
                    }
                    else if (parent instanceof Frame) {
                        kc = new KeywordChooser((Frame)parent, keywords, listener);
                    }
                    else {
                        kc = new KeywordChooser(keywords, listener);
                    }
                    kc.setLocationRelativeToXY(keywordBtn, CE_KeywordsPane.this);
                    kc.setVisible(true);
                }
            }
            else if (src == opBtn)
                show(opBtn, opPopup);
            else if (cmd.equals(AND))
                insert("&");
            else if (cmd.equals(OR))
                insert("|");
            else if (cmd.equals(NOT))
                insert("!");
            else if (cmd.equals(PARENS)) {
                insert("()");
                keywordsField.setCaretPosition(keywordsField.getCaretPosition() - 1);
            } else if (src.getParent() == keywordPopup)
                insert(cmd);
        }

        // show a popup menu relative to its invoking button
        private void show(Component ref, JPopupMenu menu) {
            menu.show(ref, 0, ref.getHeight());
        }

        // insert text into the keywords field
        void insert(String s) {
            if (s.length() == 0)
                return;

            String t = keywordsField.getText();

            Caret caret = keywordsField.getCaret();
            int p1 = Math.min(caret.getDot(), caret.getMark());
            int p2 = Math.max(caret.getDot(), caret.getMark());

            boolean needSpaceBefore = Character.isLetterOrDigit(s.charAt(0))
            && (p1 > 0) && Character.isLetterOrDigit(t.charAt(p1 - 1));
            if (needSpaceBefore)
                s = " " + s;

            boolean needSpaceAfter = Character.isLetterOrDigit(s.charAt(s.length() - 1))
            && (p2 < t.length()) && Character.isLetterOrDigit(t.charAt(p2));
            if (needSpaceAfter)
                s = s + " ";

            keywordsField.replaceSelection(s);
        }

        // ---------- ChangeListener -----------------------------------

        public void stateChanged(ChangeEvent e) {
            enableKeywordFields();
        }

        // ---------- ItemListener -----------------------------------

        public void itemStateChanged(ItemEvent e) {
            enableKeywordFields();
        }
    }

    private static class DropDownIcon implements Icon {

        public void paintIcon(Component c, Graphics g, int x, int y) {
            JComponent component = (JComponent)c;
            int iconWidth = getIconWidth();

            g.translate( x, y );
            g.setColor( component.isEnabled() ?
                UIFactory.Colors.CONTROL_INFO.getValue() :
                UIFactory.Colors.CONTROL_SHADOW.getValue() );
            g.drawLine( 0, 0, iconWidth - 1, 0 );
            g.drawLine( 1, 1, 1 + (iconWidth - 3), 1 );
            g.drawLine( 2, 2, 2 + (iconWidth - 5), 2 );
            g.drawLine( 3, 3, 3 + (iconWidth - 7), 3 );
            g.drawLine( 4, 4, 4 + (iconWidth - 9), 4 );

            g.translate( -x, -y );
        }

        public int getIconWidth() { return 10; }

        public int getIconHeight()  { return 5; }

    }
}
