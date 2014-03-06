/*
 * $Id$
 *
 * Copyright (c) 1996, 2012, Oracle and/or its affiliates. All rights reserved.
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
import java.awt.Toolkit;
import javax.help.BadIDException;
import javax.help.HelpSet;
import javax.help.InvalidHelpSetContextException;
import javax.help.JHelpContentViewer;
import javax.help.Map.ID;
import javax.accessibility.AccessibleContext;
import javax.swing.JComponent;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.sun.interview.ErrorQuestion;
import com.sun.interview.Help;
import com.sun.interview.Interview;
import com.sun.interview.Question;

class InfoPanel extends JComponent
{
    public InfoPanel(Interview interview) {
        this.interview = interview;
        infoHelpSet = Help.getHelpSet(interview);
        viewer = new JHelpContentViewer(infoHelpSet);
        viewer.setName("help");
        viewer.setToolTipText(i18n.getString("info.tip"));
        AccessibleContext v_ac = viewer.getAccessibleContext();
        v_ac.setAccessibleName(i18n.getString("info.name"));
        v_ac.setAccessibleDescription(viewer.getToolTipText());
        setName("info");
        setLayout(new BorderLayout());
        addAncestorListener(new Listener());
        add(viewer, BorderLayout.CENTER);
    }

    public HelpSet getHelpSet() {
        return infoHelpSet;
    }

    public void setCurrentID(Question q) {
        try {
            ID id = Help.getHelpID(q);
            // uugh
            if (id == null)
                System.err.println("WARNING: no help for " + q.getKey());
            else {
                //System.err.println("IP: set help ID " + id);
                viewer.setCurrentID(id);
                //System.err.println("IP: currentURL " + viewer.getCurrentURL());
                //System.err.println("IP: currentTitle " + viewer.getDocumentTitle());
            }
        }
        catch (BadIDException e) {
            System.err.println("WARNING: no help for " + q.getKey());
        }
        catch (InvalidHelpSetContextException e) {
            e.printStackTrace();
        }
    }

    public Dimension getPreferredSize() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = new Dimension(PREFERRED_WIDTH*tk.getScreenResolution(),
                                    PREFERRED_HEIGHT*tk.getScreenResolution());
        return d;
    }

    private Interview interview;
    private HelpSet infoHelpSet;
    private JHelpContentViewer viewer;
    private Listener listener = new Listener();

    private static final I18NResourceBundle i18n = I18NResourceBundle.getDefaultBundle();

    private static final int PREFERRED_WIDTH = 4; // inches
    private static final int PREFERRED_HEIGHT = 3; // inches

    private class Listener implements AncestorListener, Interview.Observer
    {
        // ---------- from AncestorListener -----------

        public void ancestorAdded(AncestorEvent e) {
            interview.addObserver(this);
            currentQuestionChanged(interview.getCurrentQuestion());
        }

        public void ancestorMoved(AncestorEvent e) { }

        public void ancestorRemoved(AncestorEvent e) {
            interview.removeObserver(this);
        }

        //----- from Interview.Observer -----------

        public void pathUpdated() { }

        public void currentQuestionChanged(Question q) {
            if (!(q instanceof ErrorQuestion))
                setCurrentID(q);
        }
    }
}
