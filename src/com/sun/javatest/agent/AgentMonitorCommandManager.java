/*
 * $Id$
 *
 * Copyright (c) 2004, 2012, Oracle and/or its affiliates. All rights reserved.
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

import java.util.ListIterator;

import com.sun.javatest.tool.Command;
import com.sun.javatest.tool.CommandContext;
import com.sun.javatest.tool.Desktop;
import com.sun.javatest.util.I18NResourceBundle;


/**
 * The ToolManager for {@link AgentMonitorTool agent monitor} window.
 */
public class AgentMonitorCommandManager extends AgentMonitorBatchCommandManager
{


    @Override
    public boolean parseCommand(String cmd, ListIterator argIter, CommandContext ctx)
        throws Command.Fault
    {

        if (super.parseCommand(cmd, argIter, ctx)) {
            return true;
        }

        if (cmd.equalsIgnoreCase(ShowMonitorCommand.getName())) {
            ctx.addCommand(new ShowMonitorCommand());
            return true;
        }

        return false;
    }
    @Override
    String[] getCommands() {
        String[] cmds = super.getCommands();
        String[] newCmds = new String[cmds.length + 1];
        System.arraycopy(cmds, 0, newCmds, 0, cmds.length);
        newCmds[cmds.length] = ShowMonitorCommand.getName();
        return newCmds;
    }

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(AgentMonitorCommandManager.class);

    //----------------------------------------------------------------------------

    private static class ShowMonitorCommand extends Command {
        static String getName() {
            return "monitorAgent";
        }

        ShowMonitorCommand() {
            super(getName());
        }

        @Override
        public int getDesktopMode() {
            return DESKTOP_REQUIRED_DTMODE;
        }

        public void run(CommandContext ctx) {
            Desktop d = ctx.getDesktop();
            AgentMonitorToolManager tm = (AgentMonitorToolManager) (d.getToolManager(AgentMonitorToolManager.class));
            tm.startTool();
        }
    }
}

