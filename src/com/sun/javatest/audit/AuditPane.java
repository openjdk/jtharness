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
package com.sun.javatest.audit;

import java.awt.CardLayout;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.sun.javatest.tool.UIFactory;

abstract class AuditPane extends JPanel {
    AuditPane(String uiKey, UIFactory uif) {
        this.uif = uif;

        setName(uiKey);
        setLayout(new CardLayout());
        setFocusable(false);

        textArea = new JTextArea();
        textArea.setName(uiKey + ".txt");
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        uif.setAccessibleInfo(textArea, uiKey + ".txt");

        // don't really expect to need a scrollpane, but use one for visual
        // consistency with body
        JScrollPane sp = uif.createScrollPane(textArea,
                                         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                         JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(sp, "text");

        // leave subtypes to create other content
    }

    void show(String message) {
        textArea.setText(message);
        ((CardLayout)(getLayout())).show(this, "text");
    }

    abstract void show(Audit audit);

    protected void setBody(JComponent body) {
        add(body, "body");
    }

    protected void showBody() {
        ((CardLayout)(getLayout())).show(this, "body");
    }

    protected final UIFactory uif;
    private JTextArea textArea;
    private JComponent body;
}
