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

import javax.accessibility.Accessible;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URL;

public class JHelpContentViewer extends JPanel implements Accessible {

    private JEditorPane pane;
    private HelpSet helpSet;

    public JHelpContentViewer(HelpSet hs) {
        super();

        helpSet = hs;
        setLayout(new BorderLayout());
        pane = new JEditorPane();
        pane.setContentType("text/html");
        pane.setEditable(false);

        JScrollPane editorScrollPane = new JScrollPane(pane);
        editorScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        add(editorScrollPane);
        updateUI();
    }

    public void setCurrentID(HelpID helpId) {

        URL hurl = helpSet.getLocalMap().get(helpId.getId());
        if (hurl == null) {
            hurl = helpSet.getCombinedMap().get(helpId.getId());
        }

        try {
            if (hurl != null) {
                pane.setPage(hurl);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
