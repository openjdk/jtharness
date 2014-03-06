/*
 * $Id$
 *
 * Copyright (c) 2006, 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.logging;

import javax.accessibility.AccessibleContext;

import java.awt.Component;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import com.sun.javatest.util.I18NResourceBundle;

public class ErrorDialogHandler extends StreamHandler {

    public ErrorDialogHandler() {
        super();
        super.setLevel(Level.CONFIG);
    }

    public synchronized void publish(LogRecord record) {
        StringBuffer args = new StringBuffer(record.getMessage());

        argsToArr[0] = args.toString();

        if (record.getThrown() != null) {
            StackTraceElement[] trace = record.getThrown().getStackTrace();
            String[] message = new String[trace.length + 1];
            message[0] = record.getThrown().toString();
            for (int i = 1; i < message.length; i++)
                message[i] = trace[i-1].toString();

            argsToArr[0] = i18n.getString("logger.exception.message") + record.getSourceClassName() +
                "," + record.getSourceMethodName();
            showError("logger.error.message", argsToArr, message);
        } else if (record.getLevel().intValue() > 800) {
            showError("logger.error.message", argsToArr, null);
        } else {
            showInformationDialog("logger.info.message", argsToArr[0]);
        }
        super.publish(record);
        super.flush();
    }

    private void showError(String text, String[] args, String[] trace) {
        String title = i18n.getString("logger.dialog.error");
        ActionListener al = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Component c = (Component) (e.getSource());
                    JOptionPane op = (JOptionPane) SwingUtilities.getAncestorOfClass(JOptionPane.class, c);
                    op.setValue(c); // JOptionPane expects the value to be set to the selected button
                    op.setVisible(false);
                }
            };
        // button setup
        JButton okBtn = new JButton(i18n.getString("logger.dialog.ok.btn"));
        okBtn.setActionCommand("logger.dialog.ok");
        okBtn.setName("logger.dialog.ok");
        int mne = getI18NMnemonic("logger.dialog.ok.mne");
        if (mne != 0)
            okBtn.setMnemonic(mne);
        String tip = i18n.getString("logger.dialog.ok.tip");
        okBtn.setToolTipText(tip);
        okBtn.addActionListener(al);


        // setup output string
        StringBuffer traceString = new StringBuffer();
        for (String arg : args) {
            traceString.append(arg);
            traceString.append("\n");
        }

        if (trace != null) {
            traceString.append(":\n");
            for (int i = 0; i < trace.length; i++) {
                traceString.append(trace[i]);
                if (i != (trace.length -1))
                    traceString.append("\n\tat ");      // needs i18n
            }
        }


        JTextArea txt = new JTextArea(i18n.getString(traceString.toString()));
        txt.setName("logger.message");
        txt.setOpaque(false);
        txt.setEditable(false);
        txt.setLineWrap(false);
        // The height is effectively ignored in the next line (just don't use 0.)
        // The text will be laid out, wrapping lines, for the width, and the
        // preferred height will thereby be determined accordingly.
        txt.setSize(new Dimension(7 * DOTS_PER_INCH, Integer.MAX_VALUE));
        // override JTextArea focus traversal keys, resetting them to
        // the Component default (i.e. the same as for the parent.)
        txt.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
        txt.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
        AccessibleContext ac = txt.getAccessibleContext();
        ac.setAccessibleName(i18n.getString("logger.message.name"));
        ac.setAccessibleDescription(i18n.getString("logger.message.desc"));

        Object content = txt;
        Dimension d = txt.getMinimumSize();

        if (trace != null) {
            // we definitely want it to have scrolling
            JScrollPane sp = new JScrollPane(txt);
            sp.setPreferredSize(new Dimension(Math.round(6.f * DOTS_PER_INCH),
                                    Math.round(2.f * DOTS_PER_INCH)));
            content = sp;
        }
        else if (d.width > Math.round(6.f * DOTS_PER_INCH) || d.height > Math.round(2.f * DOTS_PER_INCH)) {
            // need scrolling ?
            JScrollPane sp = new JScrollPane(txt);
            sp.setPreferredSize(new Dimension(Math.round(6.f * DOTS_PER_INCH),
                                    Math.round(2.f * DOTS_PER_INCH)));
            content = sp;
        }

        JOptionPane.showOptionDialog(null,
                                     content,
                                     i18n.getString("logger.message.title"),
                                     JOptionPane.DEFAULT_OPTION,
                                     JOptionPane.ERROR_MESSAGE,
                                     null,
                                     new Object[] { okBtn },
                                     null);
    }

    private void showInformationDialog(String title, String text) {
        JTextArea txt = new JTextArea(text);
        txt.setName("literal");
        txt.setOpaque(false);
        txt.setEditable(false);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        // The height is effectively ignored in the next line (just don't use 0.)
        // The text will be laid out, wrapping lines, for the width, and the
        // preferred height will thereby be determined accordingly.
        txt.setSize(new Dimension(7 * DOTS_PER_INCH, Integer.MAX_VALUE));
        // override JTextArea focus traversal keys, resetting them to
        // the Component default (i.e. the same as for the parent.)
        txt.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
        txt.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
        AccessibleContext ac = txt.getAccessibleContext();
        ac.setAccessibleName(i18n.getString("logger.message.name"));
        ac.setAccessibleDescription(i18n.getString("logger.message.desc"));
        Dimension d = txt.getMinimumSize();
        Object content = txt;
        // need scrolling ?
        if (d.width > Math.round(6.f * DOTS_PER_INCH) ||
            d.height > Math.round(2.f * DOTS_PER_INCH)) {
            JScrollPane sp = new JScrollPane(txt);
            sp.setPreferredSize(new Dimension(Math.round(6.f * DOTS_PER_INCH),
                                    Math.round(2.f * DOTS_PER_INCH)));
            content = sp;
        }

        JOptionPane.showMessageDialog(null,
                                        content,
                                        i18n.getString("logger.info.message.title"),
                                        JOptionPane.INFORMATION_MESSAGE,
                                        null);
    }

    private static int getI18NMnemonic(String key) {
        String keyString = i18n.getString(key);
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyString);
        if (keyStroke != null)
            return keyStroke.getKeyCode();
        else
            //System.err.println("WARNING: bad mnemonic keystroke for " + key + ": " + keyString);
            return 0;
    }



    private String[] argsToArr = new String[1];
    private static I18NResourceBundle i18n= I18NResourceBundle.getBundleForClass(ErrorDialogHandler.class);
    private static final int DOTS_PER_INCH = Toolkit.getDefaultToolkit().getScreenResolution();
}

