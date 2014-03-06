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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLDecoder;
import java.util.ListIterator;

import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.LineParser;

/**
 * A class to parse a series of commands, with the help of their associated command managers.
 */
public class CommandParser
{
    /**
     * Thrown when a bad command line argument is encountered.
     */
    public class Fault extends Exception
    {
        /**
         * Create a Fault exception.
         * @param i18n A resource bundle in which to find the detail message.
         * @param key The key for the detail message.
         */
        Fault(I18NResourceBundle i18n, String key) {
            super(i18n.getString(key));
        }

        /**
         * Create a Fault exception.
         * @param i18n A resource bundle in which to find the detail message.
         * @param key The key for the detail message.
         * @param arg An argument to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        Fault(I18NResourceBundle i18n, String key, Object arg) {
            super(i18n.getString(key, arg));
        }

        /**
         * Create a Fault exception.
         * @param i18n A resource bundle in which to find the detail message.
         * @param key The key for the detail message.
         * @param args An array of arguments to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        Fault(I18NResourceBundle i18n, String key, Object[] args) {
            super(i18n.getString(key, args));
        }

        Fault(Command.Fault e) {
            super(e.getMessage(), e);
        }
    }

    /**
     * Create a parser to parse the commands accepted by a set of command managers.
     * @param mgrs the command managers for the commands that can be parsed
     */
    public CommandParser(CommandManager[] mgrs) {
        this.mgrs = mgrs;
    }

    /**
     * Parse command line arguments into a series of commands.
     * @param args the args to be parsed
     * @param ctx the context in which to store the commands that are created
     * @throws CommandParser.Fault if there are any problems with a command that is parsed,
     * such as missing or inappropriate options
     */
    public void parse(String[] args, CommandContext ctx)
        throws Fault
    {
        parse(args, false, ctx);
    }

    /**
     * Parse command line arguments into a series of commands.
     * @param args the args to be parsed
     * @param urlEncoded whether or not the args have been encoded according to the
     * "application/x-www-form-urlencoded" MIME format.
     * @param ctx the context in which to store the commands that are created
     * @throws CommandParser.Fault if there are any problems with a command that is parsed,
     * such as missing or inappropriate options
     */
    public void parse(String[] args, boolean urlEncoded, CommandContext ctx)
        throws Fault
    {
        if (urlEncoded) {
            for (int i = 0; i < args.length; i++) {
                try {
                    args[i] = URLDecoder.decode(args[i], "UTF-8");
                }
                catch (Throwable e) {
                    throw new Fault(i18n, "cmdp.cantDecode", new Object[] { args[i], e.toString() });
                }
            }
        }

        // scan args, converting them to commands
        for (ListIterator iter = getIterator(trim(args)); iter.hasNext(); ) {
            String arg = (String) (iter.next());
            decodeArg(arg, iter, ctx);
        }
    }

    private void decodeArg(String arg, ListIterator argIter, CommandContext ctx)
        throws CommandParser.Fault
    {
        // special case for Windows-like help option
        if (arg.equals("/?"))
            arg = "-?";

        if (arg.startsWith("@")) {
            // for backward compatibility, allow @file
            File file = new File(arg.substring(1));
            read(file, ctx);
            return;
        }

        // check command managers for one that will handle the arg
        if (arg.startsWith("-")) {
            String cmd = arg.substring(1);
            if (cmd.equals("read")) {
                if (argIter.hasNext()) {
                    String arg2 = ((String) (argIter.next()));
                    // consider supporting URLs here
                    File f = new File(arg2);
                    read(f, ctx);
                    return;
                }
                else
                    throw new Fault(i18n, "cmdp.badReadOpt");
            }

            for (int i = 0; i < mgrs.length; i++) {
                CommandManager m = mgrs[i];
                try {
                    if (m.parseCommand(cmd, argIter, ctx))
                        return;
                }
                catch (Command.Fault e) {
                    throw new Fault(e);
                }
            }

            throw new Fault(i18n, "cmdp.badOpt", arg);
        }

        if (arg.indexOf(" ") >= 0) {
            // inline commands in a string
            // incidentally, means we don't support filenames with spaces in
            //OLD: read(arg, ctx);
            try {
                LineParser p = new LineParser(new StringReader(arg));
                read(p, ctx);
                return;
            }
            catch (LineParser.Fault e) {
                throw new Fault(i18n, "cmdp.errorInString", e.getMessage());
            }
        }

        // last arg may be a filename
        if (!argIter.hasNext()) {
            try {
                File file = new File(arg);
                if (file.exists()) {
                    ctx.addCommand(ConfigManager.getOpenCommand(file));
                    return;
                }
            }
            catch (Command.Fault e) {
                throw new Fault(i18n, "cmdp.badFileOpt", e.getMessage());
            }
        }

        // arg not recognized
        throw new Fault(i18n, "cmdp.badOpt", arg);
    }

    private void read(File file, CommandContext ctx)
        throws Fault
    {
        LineParser p;
        try {
            p = new LineParser(file);
        }
        catch (FileNotFoundException e) {
            throw new Fault(i18n, "cmdp.cantFindFile", file);
        }
        catch (IOException e) {
            throw new Fault(i18n, "cmdp.cantOpenFile", new Object[] { file, e });
        }

        try {
            read(p, ctx);
        }
        catch (Fault e) {
            throw new Fault(i18n, "cmdp.errorInFile",
                            new Object[] { file,
                                           new Integer(p.getLineNumber()),
                                           e.getMessage() });
        }
        catch (LineParser.Fault e) {
            throw new Fault(i18n, "cmdp.errorInFile",
                            new Object[] { file,
                                           new Integer(p.getLineNumber()),
                                           e.getMessage() } );
        }
    }

    private void read(LineParser p, CommandContext ctx)
        throws Fault, LineParser.Fault
    {
        File file = p.getFile();
        String[] line;

    nextLine:
        while ((line = p.readLine()) != null) {
            ListIterator iter = getIterator(line);
            String cmd = ((String) (iter.next()));

            if (cmd.equals("read")) {
                if (iter.hasNext()) {
                    String arg = ((String) (iter.next()));
                    // consider supporting URLs here
                    File f = new File(arg);
                    if (!f.isAbsolute())
                        f = new File(file.getParentFile(), f.getPath());
                    // would be nice to have p.push(f) to avoid recursion here

                    if (!iter.hasNext()) {
                        read(f, ctx);
                        continue nextLine;
                    }
                }

                throw new Fault(i18n, "cmdp.badReadCmd");
            }

            for (int i = 0; i < mgrs.length; i++) {
                CommandManager m = mgrs[i];
                try {
                    if (m.parseCommand(cmd, iter, ctx)) {
                        if (iter.hasNext())
                            throw new Fault(i18n, "cmdp.excessArgs", cmd);
                        else
                            continue nextLine;
                    }
                }
                catch (Command.Fault e) {
                    throw new Fault(e);
                }
            }

            throw new Fault(i18n, "cmdp.badCmd", cmd);
        }
    }

    private static String[] trim(String[] args) {
        String[] trimArgs = new String[args.length];
        for (int i = 0; i < args.length; i++)
            trimArgs[i] = args[i].trim();
        return trimArgs;
    }

    private static ListIterator getIterator(final String[] args) {
        return (new ListIterator() {
                public void add(Object o) {
                    throw new UnsupportedOperationException();
                }

                public boolean hasNext() {
                    return (index < args.length);
                }

                public boolean hasPrevious() {
                    return (index > 0);
                }

                public Object next() {
                    return (index < args.length ? args[index++] : null);
                }

                public int nextIndex() {
                    return index;
                }

                public Object previous() {
                    return (index > 0 ? args[--index] : null);
                }

                public int previousIndex() {
                    return (index - 1);
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }

                public void set(Object obj) {
                    throw new UnsupportedOperationException();
                }

                private int index;
            });
    }

    private CommandManager[] mgrs;
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(CommandParser.class);

    static final String TRACE_PREFIX = "+ ";
}
