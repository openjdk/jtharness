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
package com.sun.javatest.tool;

import java.util.ListIterator;

import com.sun.javatest.util.HelpTree;
import com.sun.javatest.util.I18NResourceBundle;

/**
 * A tool manager to handle the command line options for the JT Harness desktop.
 * <ul>
 * <li><code>-newDesktop</code>: behave as though this is the first time startup
 * <li><code>-cleanDesktop</code>: synonym for newDesktop (backward compatibility)
 * </ul>
 */
public class DesktopManager extends CommandManager
{
    public HelpTree.Node getHelp() {
        String[] cmds = {"cleanDesktop", "newDesktop", "resume", "laf"};
        return new HelpTree.Node(i18n, "dt.opts", cmds);
    }

    Desktop createDesktop() {
        Desktop d = new Desktop();
        if (firstTimeFlag)
            d.setFirstTime(firstTimeFlag);
        if (resumeFlag) {
            d.setRestoreOnStart(true);
        }
        return d;
    }

    Desktop createDesktop(CommandContext ctx) {
        Desktop d = new Desktop(ctx);
        if (firstTimeFlag)
            d.setFirstTime(firstTimeFlag);
        if (resumeFlag) {
            d.setRestoreOnStart(true);
        }
        return d;
    }


    //----------------------------------------------------------------------------

    public boolean parseCommand(String cmd, ListIterator argIter, CommandContext ctx)
        throws Command.Fault
    {
        if (cmd.equalsIgnoreCase("newDesktop") || cmd.equalsIgnoreCase("cleanDesktop")) {
            firstTimeFlag = true;
            ctx.addCommand(new NewDesktopCommand());
            return true;
        } else if (cmd.equalsIgnoreCase("resume")) {
            resumeFlag = true;
            ctx.addCommand(new ResumeCommand());
            return true;
        } else if (cmd.equalsIgnoreCase("laf")) {
            ctx.addCommand(new SetLafCommand(argIter, ctx));
            return true;
        }
        return false;
    }

    private boolean firstTimeFlag;
    private boolean resumeFlag;
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(ConfigManager.class);

    private static class NewDesktopCommand extends Command {
        NewDesktopCommand() {
            super("newDesktop");
        }

        public void run(CommandContext ctx) {
        }
    }

    private static class ResumeCommand extends Command {

        public ResumeCommand() {
            super("resume");
        }

        public void run(CommandContext ctx) throws Fault {
        }
    }

    private static class SetLafCommand extends Command {

        private SetLafCommand(ListIterator argIter, CommandContext ctx) throws Fault {
            super("laf");

            if (!argIter.hasNext()) {
                throw new Fault(i18n, "dt.opts.laf.missingArg");
            }

            int laf;

            String lafName = (String) argIter.next();
            if ("nimbus".equalsIgnoreCase(lafName)) {
                laf = CommandContext.NIMBUS_LAF;
            } else if ("metal".equalsIgnoreCase(lafName)) {
                laf = CommandContext.METAL_LAF;
            } else if ("system".equalsIgnoreCase(lafName) || "sys".equalsIgnoreCase(lafName)) {
                laf = CommandContext.SYSTEM_LAF;
            } else if ("default".equalsIgnoreCase(lafName)) {
                laf = CommandContext.DEFAULT_LAF;
            } else {
                throw new Fault(i18n, "dt.opts.laf.badArg", new Object[] {lafName});
            }

            ctx.setPreferredLookAndFeel(laf);
        }

        @Override
        public void run(CommandContext ctx) throws Fault {
        }
    }
}
