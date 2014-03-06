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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.sun.javatest.util.HelpTree;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.StringArray;

/**
 * A command supporting an extensible set of boolean options.
 * Valid options must be registered with the addOption method.
 * The current setting of the various options can be obtained from
 * the current CommandContext.
 */
public class VerboseCommand extends Command
{
    /**
     * Add a option to the set recognized by this command.
     * @param name the name of the option to be accepted.
     * @param node the help node for this option
     */
    public static void addOption(String name, HelpTree.Node node) {
        ensureAllOptionsInitialized();
        allOptions.put(name, node);
    }

    /**
     * The name of the default option. If the verbose command is given without
     * options, then CommandContext.getVerboseOptionValue(DEFAULT) will be set true.
     */
    private static final String DEFAULT = "default";
    private static final String MAX = "max";
    private static final String QUIET = "quiet";
    private static final String DATE = "date";
    private static final String NO_PREFIX = "no-";

    private static void ensureAllOptionsInitialized() {
        if (allOptions == null) {
            allOptions = new TreeMap();
            allOptions.put(MAX,  new HelpTree.Node(i18n, "verb.max"));
            allOptions.put(QUIET, new HelpTree.Node(i18n, "verb.quiet"));
        }
    }
    private static Map allOptions;

    //--------------------------------------------------------------------------


    VerboseCommand(String cmd) throws Fault {
        super(cmd);

        int chop = cmd.indexOf(CMD);

        if (chop == -1)
            throw new IllegalArgumentException();

        String workstr = cmd.substring(chop + CMD.length());

        if (workstr.length() == 0) {
            optionValues.put(DEFAULT, Boolean.TRUE);
        }
        else if (workstr.charAt(0) == ':') {
            // rm colon
            workstr = workstr.substring(1);
            processOptions(workstr);
        }
        else
            throw new Fault(i18n, "verb.badOpts");
    }

    private void processOptions(String ops) throws Fault {
        ensureAllOptionsInitialized();
        String[] items = StringArray.splitList(ops, ",");

        if (items == null)
            throw new Fault(i18n, "verb.noOpts");

        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            boolean on;
            if (item.startsWith(NO_PREFIX)) {
                on = false;
                item = item.substring(NO_PREFIX.length());
            }
            else
                on = true;

            if (!allOptions.containsKey(item.toLowerCase()))
                throw new Fault(i18n, "verb.badOpt", item);

            optionValues.put(item, new Boolean(on));
        }
    }

    public void run(CommandContext ctx) throws Fault {
        for (Iterator iter = optionValues.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry e = (Map.Entry) (iter.next());
            String name = (String) (e.getKey());
            boolean value = ((Boolean) (e.getValue())).booleanValue();
            if (name.equalsIgnoreCase(MAX))
                ctx.setVerboseMax(value);
            else if (name.equalsIgnoreCase(QUIET))
                ctx.setVerboseQuiet(value);
            else if (name.equalsIgnoreCase(DATE))
                ctx.setVerboseTimestampEnabled(value);
            else
                ctx.setVerboseOptionValue(name, value);
        }
    }

    static HelpTree.Node getHelp() {
        ensureAllOptionsInitialized();

        HelpTree.Node[] nodes = new HelpTree.Node[allOptions.size()];
        int i = 0;
        for (Iterator iter = allOptions.values().iterator(); iter.hasNext(); )
            nodes[i++] = (HelpTree.Node) (iter.next());

        return new HelpTree.Node(i18n, "verb", nodes);
    }

    static String getName() {
        return CMD;
    }

    private HashMap optionValues = new HashMap();  // HashMap<name,boolean>

    private static final String CMD = "verbose";
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(VerboseCommand.class);
}
