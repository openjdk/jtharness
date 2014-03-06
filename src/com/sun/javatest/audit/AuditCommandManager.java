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
package com.sun.javatest.audit;

import java.util.ListIterator;

import com.sun.javatest.Parameters;
import com.sun.javatest.tool.Command;
import com.sun.javatest.tool.CommandContext;
import com.sun.javatest.tool.CommandManager;
import com.sun.javatest.tool.Desktop;
import com.sun.javatest.util.HelpTree;
import com.sun.javatest.util.I18NResourceBundle;


/**
 * The CommandManager for audit support.
 */
public class AuditCommandManager extends CommandManager
{

    public HelpTree.Node getHelp() {
        String[] cmds = {
            "audit",
            "showAudit"
        };
        return new HelpTree.Node(i18n, "cmgr.help", cmds);
    }

    //----------------------------------------------------------------------------

    public boolean parseCommand(String cmd, ListIterator argIter, CommandContext ctx)
        throws Command.Fault
    {
        if (cmd.equalsIgnoreCase(AuditCommand.getName())) {
            ctx.addCommand(new AuditCommand(argIter));
            return true;
        }

        if (cmd.equalsIgnoreCase(ShowAuditCommand.getName())) {
            ctx.addCommand(new ShowAuditCommand(argIter));
            return true;
        }

        return false;
    }

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(AuditCommandManager.class);

    //----------------------------------------------------------------------------

    private static class AuditCommand extends Command
    {
        static String getName() {
            return "audit";
        }

        AuditCommand(ListIterator argIter) throws Fault {
            super(getName());

            while (argIter.hasNext()) {
                String arg = nextArg(argIter);
                if (arg.equalsIgnoreCase("-showEnvValues"))
                    showAllEnvValues = true;
                else if (arg.equalsIgnoreCase("-showMultipleEnvValues"))
                    showMultipleEnvValues = true;
                else {
                    putbackArg(argIter);
                    break;
                }
            }
        }

        public boolean isActionCommand() {
            return true;
        }

        public void run(CommandContext ctx) throws Fault {
            Parameters p = getConfig(ctx);

            Audit a = new Audit(p);
            a.report(System.out, showAllEnvValues, showMultipleEnvValues);

            if (a.isOK())
                ctx.printMessage(i18n, "audit.ok");
            else
                ctx.printErrorMessage(i18n, "audit.failed");
        }

        private boolean showAllEnvValues;
        private boolean showMultipleEnvValues;
    }

    //----------------------------------------------------------------------------

    private static class ShowAuditCommand extends Command {
        static String getName() {
            return "showAudit";
        }

        ShowAuditCommand(ListIterator argIter) {
            super(getName());
        }

        public int getDesktopMode() {
            return DESKTOP_REQUIRED_DTMODE;
        }

        public void run(CommandContext ctx) {
            Desktop d = ctx.getDesktop();
            AuditToolManager tm = (AuditToolManager) (d.getToolManager(AuditToolManager.class));
            tm.startTool();
        }
    }
}

