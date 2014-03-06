/*
 * $Id$
 *
 * Copyright (c) 2004, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.agent;

import java.awt.event.ActionEvent;
import java.util.Map;
import javax.swing.Action;

import com.sun.javatest.tool.Desktop;
import com.sun.javatest.tool.ToolAction;
import com.sun.javatest.tool.Tool;
import com.sun.javatest.tool.ToolManager;


/**
 * The ToolManager for {@link AgentMonitorTool agent monitor} window.
 */
public class AgentMonitorToolManager extends ToolManager
{
    /**
     * Create an agent tool manager.
     * @param desktop the desktop for which this manager will manage agent monitor tools
     */
    public AgentMonitorToolManager(Desktop desktop) {
        super(desktop);
    }

    //----------------------------------------------------------------------------

    public Action[] getWindowOpenMenuActions() {
        Action a = new ToolAction(i18n, "tmgr.openMonitor") {
            public void actionPerformed(ActionEvent e) {
                startTool();
            }
        };
        return new Action[] { a };
    }

    //----------------------------------------------------------------------------

    public Tool restoreTool(Map m) {
        AgentMonitorTool t = getTool();
        t.restore(m);
        return t;
    }

    //----------------------------------------------------------------------------

    /**
     * Start the {@link AgentMonitorTool agent monitor} window.
     */
    public Tool startTool() {
        AgentMonitorTool t = getTool();

        Desktop d = getDesktop();
        if (!d.containsTool(t))
            d.addTool(t);
        d.setSelectedTool(t);

        return t;
    }

    /**
     * Access the singleton tool managed by this tool manager.
     * @return the one agent monitor tool managed by this tool manager
     */
    public AgentMonitorTool getTool() {
        if (tool == null) {
            tool = new AgentMonitorTool(this);
            tool.addObserver(new Tool.Observer() {
                    public void shortTitleChanged(Tool t, String newValue) { }

                    public void titleChanged(Tool t, String newValue) { }

                    public void toolDisposed(Tool t) {
                        if (t == tool)
                            tool = null;
                    }
                });
        }

        return tool;
    }

    private AgentMonitorTool tool;
}

