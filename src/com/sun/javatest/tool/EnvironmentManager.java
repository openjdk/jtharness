/*
 * $Id$
 *
 * Copyright (c) 2001, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.lang.SecurityManager;

import com.sun.javatest.JavaTestSecurityManager;
import com.sun.javatest.TestEnvironment;
import com.sun.javatest.util.HelpTree;
import com.sun.javatest.util.I18NResourceBundle;

/**
 * A command manager to handle the command line options for adding
 * default values into every test environment.
 * The supported options are:
 * <ul>
 * <li><code>-EsysProps</code>: add all the system properties into every
 *  environment
 * <li><code>-E</code><i>name</i><code>=</code><i>value</i>: set <i>name</i>
 * to <i>value</i> in every environment
 * </ul>
 */
public class EnvironmentManager extends CommandManager
{
    EnvironmentManager() {
        // flush any prior tables
        TestEnvironment.clearDefaultPropTables();

        // set basic defaults
        String[] stdSysProps = {"user.dir", "user.home"};
        Map m = new HashMap();
        for (int i = 0; i < stdSysProps.length; i++) {
            String name = stdSysProps[i];
            m.put(name, System.getProperty(name));
        }
        TestEnvironment.addDefaultPropTable(i18n.getString("env.def.sysProps"), m);
    }


    public HelpTree.Node getHelp() {
        String[] opts = { "EsysProps", "EnameVar" };
        return new HelpTree.Node(i18n, "env", opts);
    }

    public boolean parseCommand(String cmd, ListIterator argIter, CommandContext ctx)
        throws Command.Fault
    {
        if (cmd.equalsIgnoreCase("EsysProps")) {
            ctx.addCommand(new ESysPropsCommand());
            return true;
        }

        if (cmd.startsWith("E") && (cmd.indexOf('=') > 0)) {
            int sep = cmd.indexOf('=');
            String name = cmd.substring(1, sep).trim();
            String value = cmd.substring(sep + 1).trim();
            ctx.addCommand(new ENameValueCommand(name, value));
            return true;
        }

        return false;
    }

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(ConfigManager.class);

    //--------------------------------------------------------------------------

    private class ENameValueCommand extends Command {
        ENameValueCommand(String name, String value) {
            super("E" + name + "=" + value);
            this.name = name;
            this.value = value;
        }

        public void run(CommandContext ctx) throws Fault {
            Map m = new HashMap(1);  // would be nice to have a singleton map
            m.put(name, value);
            TestEnvironment.addDefaultPropTable(i18n.getString("env.def.cmdLine"), m);
        }

        private String name;
        private String value;
    }

    //--------------------------------------------------------------------------

    private class ESysPropsCommand extends Command {
        ESysPropsCommand() {
            super("EsysProps");
        }

        public void run(CommandContext ctx) throws Fault {
            try {
                Map sysProps;
                SecurityManager sc = System.getSecurityManager();
                if (sc instanceof JavaTestSecurityManager) {
                    // open up the properties access permission to get system props.
                    boolean prev = ((JavaTestSecurityManager) sc).setAllowPropertiesAccess(true);
                    try {
                        sysProps = System.getProperties();
                    }
                    finally {
                        ((JavaTestSecurityManager) sc).setAllowPropertiesAccess(prev);
                    }
                }
                else {
                    // if not JTSecurityManager, try to get properties anyway
                    // and handle the exception if we can't get them
                    sysProps = System.getProperties();
                }
                TestEnvironment.addDefaultPropTable(i18n.getString("env.def.sysProps"), sysProps);
            }
            catch (SecurityException e) {
                throw new Fault(i18n, "env.cantAccessSysProps", e);
            }
        }
    }
}
