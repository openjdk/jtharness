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
package com.sun.javatest.mrep;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.sun.javatest.report.CustomReport;
import com.sun.javatest.tool.Desktop;
import com.sun.javatest.tool.ToolDialog;
import com.sun.javatest.tool.UIFactory;
import java.awt.event.ComponentAdapter;

class OptionsDialog extends ToolDialog
{
    OptionsDialog(final ReportTool tool, ActionListener okListener,  UIFactory uif,
            Desktop desktop) {
        super(tool, uif, "opts");
        this.tool = tool;
        this.okListener = okListener;
        this.desktop = desktop;
        setComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                tool.updateGUI();
            }
        });
        setHelp("report.options.csh");
    }

    String getResultPath() {
        return ((FilesPane)panes[0]).getResultDir();
    }

    String[] getXmlFiles() {
        return ((FilesPane)panes[0]).getXmlFiles();
    }

    boolean handleAsConflict() {
        return true;
    }

    boolean resolveByRecent() {
        return ((OptionsPane)panes[1]).resolveByRecent();
    }

    boolean isXmlReport() {
        return ((OptionsPane)panes[1]).isXmlReport();
    }

    CustomReport[] getCustomReports() {
        return ((OptionsPane)panes[1]).getCustomSelected();
    }

    void updateCustomReports() {

    }

    // remove this function by something better XXX
    private void setButtonsSize() {
        JButton[] bb = ((OptionsPane)panes[1]).getButtons();
        JButton[] fb = ((FilesPane)panes[0]).getButtons();
        JButton[] all = new JButton[bb.length + fb.length];
        int max = 0;
        for (int i = 0; i < all.length; i++) {
            if (i < bb.length) {
                all[i] = bb[i];
            } else {
                all[i] = fb[i - bb.length];
            }
            max = max > all[i].getMinimumSize().width ? max
                    : all[i].getMinimumSize().width;
        }
        for (int i = 0; i < all.length; i++) {
            all[i].setPreferredSize(new Dimension(max, all[i].getPreferredSize().height));
            all[i].setMinimumSize(all[i].getPreferredSize());
        }
    }


    protected void windowClosingAction(AWTEvent e) {
        cleanUp();
    }

    protected void initGUI() {
        setI18NTitle("opts.title");
        JPanel tabs = new JPanel();
        CardLayout cl = new CardLayout();
        tabs.setLayout(cl);
        panes = new JPanel[] {
            new FilesPane(uif, new ChangeTabListener(cl, "1", tabs)),
            new OptionsPane(uif, desktop, new ChangeTabListener(cl, "0", tabs), okListener)
        };


        attachYardKeeper(((FilesPane)panes[0]).getButtons()[1]);
        attachYardKeeper(((OptionsPane)panes[1]).getButtons()[1]);
        attachYardKeeper(((OptionsPane)panes[1]).getButtons()[2]);

        //Better not to restrict dialog size
        //int dpi = uif.getDotsPerInch();
        body = uif.createPanel("opts.fields", new BorderLayout(), false);
        //body.setPreferredSize(new Dimension(6 * dpi, 3 * dpi));
        for (int i = 0; i < panes.length; i++) {
            //uif.addTab(tabs, "tool.tabs." + panes[i].getName(), panes[i]);
            tabs.add(panes[i], "" + i);
        }

        body.add(tabs);
        setBody(body);


        setButtonsSize();
        pack();
    }

    private void attachYardKeeper(JButton btn) {
        ActionListener[] listeners = btn.getActionListeners();

        if (listeners.length == 0) {
            btn.addActionListener(new YardKeeper(null));
        } else {
            ActionListener last = listeners[listeners.length-1];
            btn.removeActionListener(last);
            btn.addActionListener(new YardKeeper(last));
        }
    }

    protected void updateGUI() {
        setButtonsSize();
    }

    boolean checkInput() {
        return ((FilesPane)panes[0]).checkInput();
    }

    class ChangeTabListener implements ActionListener {
        String key;
        CardLayout cl;
        Container parent;
        ChangeTabListener(CardLayout cl, String key, Container parent) {
            this.key = key;
            this.cl = cl;
            this.parent = parent;
        }
        public void actionPerformed(ActionEvent e) {
            cl.show(parent, key);
        }
    }

    public void cleanUp() {
        setBody(null);
    }

    private class YardKeeper implements ActionListener {
        private ActionListener chain;

        private YardKeeper(ActionListener chain) {
            this.chain = chain;
        }

        public void actionPerformed(ActionEvent e) {
            if (chain != null)
               chain.actionPerformed(e);
        }
    }



    private ReportTool tool;
    private ActionListener okListener;
    private JPanel body;
    private Desktop desktop;

    private JPanel[] panes;
}

