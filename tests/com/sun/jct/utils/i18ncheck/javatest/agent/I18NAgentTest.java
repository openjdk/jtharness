/*
 * $Id$
 *
 * Copyright (c) 2006, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jct.utils.i18ncheck.javatest.agent;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;

import com.sun.javatest.agent.AgentMonitorCommandManager;
import com.sun.javatest.agent.AgentMonitorToolManager;
import com.sun.javatest.tool.Desktop;

public class I18NAgentTest
{
    public static void main(final String[] args) {
        try {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new I18NAgentTest().run(args);
                    System.exit(0);
                }
            });

        }
        catch (Throwable t) {
            t.printStackTrace();
            System.exit(2);
        }
    }

    void run(String[] args) {
        AgentMonitorCommandManager cm = new AgentMonitorCommandManager();
        cm.getHelp();

        Desktop d = new Desktop();
        AgentMonitorToolManager tm = (AgentMonitorToolManager) (d.getToolManager(AgentMonitorToolManager.class));
        Action[] actions = tm.getWindowOpenMenuActions();
        // start tool via the tool manager's action
        actions[0].actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
    }
}
