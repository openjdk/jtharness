/*
 * $Id$
 *
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.tool.jthelp;

import com.sun.javatest.tool.UIFactory;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class JTHelpProgressBar extends Component implements PropertyChangeListener {

    private JProgressBar progressBar;
    private SwingWorker<Void, Void> task;
    private JDialog frame;
    private JPanel сontentPane;
    private UIFactory uif;


    public JTHelpProgressBar(SwingWorker<Void, Void> progressTask) {
        uif = new UIFactory(this, null);

        task = progressTask;
        сontentPane = new JPanel(new BorderLayout());
        JLabel waitText = uif.createLabel("help.wait");
        progressBar = uif.createProgressBar("help.progress", JProgressBar.HORIZONTAL);

        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        JPanel panel = new JPanel();
        panel.add(waitText);
        panel.add(progressBar);

        сontentPane.add(panel, BorderLayout.PAGE_START);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName())) {
            final int progress = (Integer) evt.getNewValue();
            SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
        }
        if (SwingWorker.StateValue.DONE == evt.getNewValue()) {
            SwingUtilities.invokeLater(() -> frame.dispose());
        }
    }


    public void createAndShowGUI() {

        frame = new JDialog();
        frame.setModal(true);
        frame.setUndecorated(true);
        frame.getRootPane().setBorder(BorderFactory.createLineBorder(uif.getI18NColor("help.progress")));

        сontentPane.setOpaque(true);
        frame.setContentPane(сontentPane);
        frame.pack();

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);

        task.addPropertyChangeListener(JTHelpProgressBar.this);
        task.execute();

        frame.setVisible(true);

    }

}
