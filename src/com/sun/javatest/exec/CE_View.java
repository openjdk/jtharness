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

import java.awt.event.ActionListener;
import javax.help.InvalidHelpSetContextException;
import javax.help.JHelpContentViewer;
import javax.help.Map;
import javax.swing.JPanel;

import com.sun.javatest.InterviewParameters;
import com.sun.javatest.JavaTestError;
import com.sun.javatest.tool.ToolDialog;
import com.sun.javatest.tool.UIFactory;

abstract class CE_View extends JPanel
{
    protected CE_View(InterviewParameters config,
                      JHelpContentViewer infoPanel,
                      UIFactory uif, ActionListener l) {
        this.config = config;
        this.infoPanel = infoPanel;
        this.uif = uif;
        listener = l;
        setFocusable(false);
    }

    abstract boolean isOKToClose();

    abstract void load();

    abstract void save();

    void refresh() { }

    public void setParentToolDialog(ToolDialog d) {
        toolDialog = d;
    }

    public ToolDialog getParentToolDialog() {
        return toolDialog;
    }

    protected boolean isInfoVisible() {
        return (infoPanel != null);
    }

    protected void showInfo(Map.ID id) {
        try {
            // Note: infoPanel may be null if no help set found when creating ConfigEditor
            if (infoPanel != null)
                infoPanel.setCurrentID(id);
        }
        catch (InvalidHelpSetContextException e) {
            JavaTestError.unexpectedException(e);
        }
    }

    protected ToolDialog toolDialog;

    protected InterviewParameters config;
    protected JHelpContentViewer infoPanel;
    protected UIFactory uif;
    protected ActionListener listener;

    static final String FULL = "full";
    static final String STD = "std";
    static final String DONE = "done";
}
