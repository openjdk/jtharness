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

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.text.Keymap;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import com.sun.interview.InetAddressQuestion;
import com.sun.interview.Question;

public class InetAddressQuestionRenderer
    implements QuestionRenderer
{
    public JComponent getQuestionRendererComponent(Question qq, ActionListener listener) {
        q = (InetAddressQuestion) qq;
        int type = q.getType();
        int style = q.getStyle();
        InetAddress[] suggestions = q.getSuggestions();

        if (type == InetAddressQuestion.IPv4
            && style == InetAddressQuestion.IPv4
            && suggestions == null)
            return createIPv4Panel(q, listener);
        else
            return createIPv6Panel(q, listener);
    }

    protected JPanel createIPv4Panel(final InetAddressQuestion q, ActionListener listener) {
        InetAddress v = q.getValue();
        byte[] addr = (v == null ? new byte[4] : v.getAddress());
        fields = new JTextField[4];

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setName("inet");
        panel.setFocusable(false);

        GridBagConstraints c = new GridBagConstraints();

        for (int i = 0; i < 4; i++) {
            String lblKey = (i == 0 ? "inet.ip" : "inet.dot");
            JLabel label = new JLabel(i18n.getString(lblKey + ".lbl"));
            label.setDisplayedMnemonic(i == 0 ? i18n.getString(lblKey + ".mne").charAt(0)
                                       : '0' + i);
            label.setToolTipText(i18n.getString(lblKey + ".tip"));
            panel.add(label, c);

            byte b = addr[i];
            JTextField field = new JTextField(String.valueOf(b < 0 ? b + 256 : b), 3);
            field.setName("inet." + i);
            field.setToolTipText(i18n.getString("inet.field.tip", new Integer(i)));
            field.getDocument().addDocumentListener(new ActionDocListener(field, listener, EDITED));
            Keymap keymap = field.addKeymap("IP field", field.getKeymap());
            //keymap.addActionForKeyStroke(enterKey, enterListener);
            keymap.setDefaultAction(createKeyMapAction());
            field.setKeymap(keymap);
            label.setLabelFor(field);

            if (i == 3) {
                c.weightx = 1;
                c.anchor = GridBagConstraints.WEST;
            }

            panel.add(field, c);

            if (i > 0)
                fields[i-1].putClientProperty("next", field);

            fields[i] = field;
        }

        Runnable valueSaver = new ValueSaver();

        panel.putClientProperty(VALUE_SAVER, valueSaver);

        return panel;

    }

    protected AbstractAction createKeyMapAction() {
        return new KeyMapAction();
    }

    protected class KeyMapAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            JTextField src = (JTextField) (e.getSource());
            JTextField next = (JTextField) (src.getClientProperty("next"));
            String cmd = e.getActionCommand();
            if (cmd.equals(".")) {
                if (next != null) {
                    // got a dot and a successor field; step forward
                    next.requestFocus();
                    next.getCaret().setDot(0);
                    next.getCaret().moveDot(next.getText().length());
                    return;
                }
            }
            else if (Character.isDigit(cmd.charAt(0))) {
                // got a digit
                int savedDot = src.getCaret().getDot();
                int savedMark = src.getCaret().getMark();
                String savedText = src.getText();

                Action delegate = new javax.swing.text.DefaultEditorKit.InsertContentAction();
                delegate.actionPerformed(e);

                String newText = src.getText();
                int newValue = Integer.parseInt(newText);

                if (newText.length() <= 3 && newValue <= 255) {
                    // new text is good; move on to next field if appropriate
                    // (otherwise leave focus here, for next character)
                    if (src.getCaretPosition() == 3 && next != null) {
                        next.requestFocus();
                        next.getCaret().setDot(0);
                        next.getCaret().moveDot(next.getText().length());
                    }
                    return;
                }
                else {
                    // reset text
                    src.setText(savedText);
                    src.getCaret().setDot(savedMark);
                    src.getCaret().moveDot(savedDot);
                }
            }
            else if (cmd.charAt(0) == '\n')
                // ignore spurious newlines; don't know why we are getting them
                // (they occur when doing enter in previous question)
                return;
            fields[0].getToolkit().beep();
        }
    };

    protected class ValueSaver implements Runnable {
        public void run() {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < 4; i++) {
                String s = fields[i].getText();
                if (s == null || s.length() == 0)
                    sb.append("0");
                else
                    sb.append(s);
                if (i < 3)
                    sb.append(".");
            }
            try {
                InetAddress a = InetAddress.getByName(sb.toString());
                q.setValue(a);
            }
            catch (UnknownHostException e) {
                // ignore ... should not happen
            }
        }
    }

    protected JPanel createIPv6Panel(InetAddressQuestion q, ActionListener listener) {
        InetAddress[] suggestions = q.getSuggestions();

        String[] ss;
        if (suggestions == null)
            ss = null;
        else {
            ss = new String[suggestions.length];
            for (int i = 0; i < suggestions.length; i++) {
                InetAddress sugg = suggestions[i];
                ss[i] = (sugg == null ? "" : sugg.getHostAddress());
            }
        }

        final JButton lookupBtn = new JButton(i18n.getString("inet.lookup.btn"));
        lookupBtn.setName("inet.lookup.btn");
        lookupBtn.setMnemonic(i18n.getString("inet.lookup.mne").charAt(0));
        lookupBtn.setToolTipText(i18n.getString("inet.lookup.tip"));

        int type = q.getType();
        int width = (type == InetAddressQuestion.IPv4 ? 16 : 0);

        final TypeInPanel p = new TypeInPanel("inet",
                                              q,
                                              width,
                                              ss,
                                              lookupBtn,
                                              listener);

        lookupPane = new LookupPane(type);

        lookupBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JDialog d = lookupPane.createDialog(p, i18n.getString("inet.lookup.title"));
                d.setVisible(true);
                p.setValue((String) (lookupPane.getValue()));
            }
            });

        return p;
    }

    public String getInvalidValueMessage(Question q) {
        return null;
    }

    protected JTextField[] fields;
    protected InetAddressQuestion q;
    protected JOptionPane lookupPane;

    private static final I18NResourceBundle i18n = I18NResourceBundle.getDefaultBundle();
    protected static final Color INVALID_VALUE_COLOR = i18n.getErrorColor();

    protected class LookupPane extends JOptionPane
        implements ActionListener, DocumentListener, ListSelectionListener
    {
        public LookupPane(int type) {
            super(new JPanel(new GridBagLayout()));
            setMessageType(JOptionPane.QUESTION_MESSAGE);
            this.type = type;

            JPanel msgPanel = (JPanel) (getMessage());

            GridBagConstraints c = new GridBagConstraints();

            JLabel lbl = new JLabel(i18n.getString("inet.lookup.name.lbl"));
            lbl.setDisplayedMnemonic(i18n.getString("inet.lookup.name.mne").charAt(0));
            c.insets.right = 5;
            msgPanel.add(lbl, c);

            nameField = new JTextField(16);
            nameField.setActionCommand(LOOKUP);
            nameField.addActionListener(this);
            lbl.setLabelFor(nameField);
            c.insets.right = 0;
            c.weightx = 1;
            msgPanel.add(nameField, c);

            String r = i18n.getString("inet.lookup.icon");
            URL url = getClass().getResource(r);
            JButton lookupBtn = (url == null ? new JButton(r)
                : new JButton(new ImageIcon(url)));
            lookupBtn.setActionCommand(LOOKUP);
            lookupBtn.addActionListener(this);
            c.weightx = 0;
            c.insets.left = 5;
            c.gridwidth = GridBagConstraints.REMAINDER;
            msgPanel.add(lookupBtn, c);

            errorField = new JTextField(16);
            errorField.setName("inet.lookup.error");
            errorField.setEditable(false);
            errorField.setBorder(null);
            errorField.setFont(errorField.getFont().deriveFont(Font.BOLD));
            errorField.setForeground(INVALID_VALUE_COLOR);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets.left = 0;
            c.weightx = 1;
            msgPanel.add(errorField, c);

            listModel = new DefaultListModel();
            list = new JList(listModel);
            list.setVisibleRowCount(3);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.addListSelectionListener(this);
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1;
            msgPanel.add(new JScrollPane(list), c);

            okBtn = new JButton(i18n.getString("inet.lookup.ok.btn"));
            okBtn.setActionCommand(OK);
            okBtn.setEnabled(false); // enabled with a valid list selection
            okBtn.addActionListener(this);

            JButton cancelBtn = new JButton(i18n.getString("inet.lookup.cancel.btn"));
            cancelBtn.setActionCommand(OK);
            cancelBtn.addActionListener(this);

            setOptions(new Object[] { okBtn, cancelBtn });
        }

        //----- for ActionListener -------------------

        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (cmd.equals(LOOKUP)) {
                String name = nameField.getText();
                if (name == null || name.length() == 0)
                    errorField.setText(i18n.getString("inet.lookup.noName.err"));
                else {
                    InetAddress[] addrs;
                    try {
                        addrs = InetAddress.getAllByName(name);
                    }
                    catch (UnknownHostException ex) {
                        addrs = null;
                    }

                    listModel.clear();

                    if (addrs == null || addrs.length == 0)
                        errorField.setText(i18n.getString("inet.lookup.notFound.err"));
                    else {
                        errorField.setText("");
                        for (int i = 0; i < addrs.length; i++)
                            listModel.addElement(addrs[i].getHostAddress());
                        list.setSelectedIndex(0);
                    }
                }
            }
            else if (cmd.equals(OK)) {
                setValue(list.getSelectedValue());
                SwingUtilities.getRoot(this).setVisible(false);
            }
            else if (cmd.equals(CANCEL)) {
                setValue(null);
                SwingUtilities.getRoot(this).setVisible(false);
            }
        }

        //----- for DocumentListener -------------------

        public void insertUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        public void removeUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        public void changedUpdate(DocumentEvent e) {
            errorField.setText("");
        }

        //----- for ListSelectionListener -----------

        public void valueChanged(ListSelectionEvent e) {
            okBtn.setEnabled(list.getSelectedValue() != null);
        }

        protected int type;
        protected JTextField nameField;
        protected JTextField errorField;
        protected DefaultListModel listModel;
        protected JList list;
        protected JButton okBtn;

        protected static final String LOOKUP = "lookup";
        protected static final String OK = "ok";
        protected static final String CANCEL = "cancel";
    }
}
