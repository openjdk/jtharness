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
package com.sun.javatest.exec;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.URL;
import javax.help.HelpModel;
import javax.help.JHelpContentViewer;
import javax.help.Map;
import javax.help.event.HelpModelEvent;
import javax.help.event.HelpModelListener;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.sun.interview.Interview;
import com.sun.interview.Question;

class DetailsBrowser extends JDialog {
    DetailsBrowser(Component parent, Interview interview, JHelpContentViewer infoPanel) {
        super(getFrameParent(parent), "Configuration Editor Details Browser", false);
        //setUndecorated(false);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets.left = 5;
        c.insets.right = 5;
        c.insets.bottom = 5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;

        Listener listener = new Listener();

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.EAST;
        lc.insets.left = 10;

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.gridwidth = GridBagConstraints.REMAINDER;
        fc.weightx = 1;
        fc.insets.left = 10;
        fc.insets.right = 10;

        JPanel qp = new JPanel(new GridBagLayout());
        qp.setBorder(BorderFactory.createTitledBorder("Current Question"));

        JLabel tagLbl = new JLabel("tag:");
        qp.add(tagLbl, lc);
        tagField = new JTextField(64);
        tagField.setBorder(null);
        tagField.setEditable(false);
        qp.add(tagField, fc);

        JLabel keyLbl = new JLabel("key:");
        qp.add(keyLbl, lc);
        keyField = new JTextField(64);
        keyField.setBorder(null);
        keyField.setEditable(false);
        qp.add(keyField, fc);

        update(interview.getCurrentQuestion());
        interview.addObserver(listener);

        p.add(qp, c);

        if (infoPanel != null) {
            JPanel mp = new JPanel(new GridBagLayout());
            mp.setBorder(BorderFactory.createTitledBorder("More Info"));

            JLabel hsLbl = new JLabel("helpset:");
            mp.add(hsLbl, lc);
            hsField = new JTextField(64);
            hsField.setBorder(null);
            hsField.setEditable(false);
            mp.add(hsField, fc);

            JLabel idLbl = new JLabel("id:");
            mp.add(idLbl, lc);
            idField = new JTextField(64);
            idField.setBorder(null);
            idField.setEditable(false);
            mp.add(idField, fc);

            JLabel urlLbl = new JLabel("url:");
            mp.add(urlLbl, lc);
            urlField = new JTextField(64);
            urlField.setBorder(null);
            urlField.setEditable(false);
            mp.add(urlField, fc);

            HelpModel m = infoPanel.getModel();
            update(m.getCurrentID(), m.getCurrentURL());
            m.addHelpModelListener(listener);

            p.add(mp, c);
        }


        setContentPane(p);
        pack();

    }

    void setQuestionInfoEnabled(boolean on) {
        keyField.setEnabled(on);
        tagField.setEnabled(on);
    }

    /*
    private void update(Question q) {
        tagField.setText(q.getTag());
        keyField.setText(q.getKey());
        try {
            if (helpSet != null) {
                try {
                    Map.ID mid = Map.ID.create(q.getKey(), helpSet);
                    URL u = helpSet.getLocalMap().getURLFromID(mid);
                    urlField.setText(u.toString());
                }
                catch (MalformedURLException e) {
                    urlField.setText(e.toString());
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    */

    private void update(Question q) {
        tagField.setText(q.getTag());
        keyField.setText(q.getKey());
    }

    private void update(Map.ID id, URL u) {
        hsField.setText(id.getHelpSet().getTitle());
        idField.setText(id.getIDString());
        urlField.setText(u.toString());
    }

    private static Frame getFrameParent(Component c) {
        return (Frame)(SwingUtilities.getAncestorOfClass(Frame.class, c));
    }


    private JTextField tagField;
    private JTextField keyField;
    private JTextField hsField;
    private JTextField idField;
    private JTextField urlField;

    private class Listener
        implements HelpModelListener, Interview.Observer
    {
        // HelpModelListener
        public void idChanged(HelpModelEvent e) {
            update(e.getID(), e.getURL());
        }

        // Interview.Observer
        public void pathUpdated() { }

        public void currentQuestionChanged(Question q) {
            update(q);
        }

        public void finished() { }
    }
}
