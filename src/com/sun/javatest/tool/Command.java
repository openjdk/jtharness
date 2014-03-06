/*
 * $Id$
 *
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.net.URL;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Vector;

import com.sun.javatest.InterviewParameters;
import com.sun.javatest.util.I18NResourceBundle;

/**
 * A class to represent a command to be executed.
 * Commands are typically read from the command line or from command files.
 */
public abstract class Command
{
    /**
     * This exception is used to report problems with a specific command.
     */
    public class Fault extends Exception
    {
        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         */
        public Fault(I18NResourceBundle i18n, String s) {
            super(i18n.getString(s));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An argument to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        public Fault(I18NResourceBundle i18n, String s, Object o) {
            super(i18n.getString(s, o));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An array of arguments to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        public Fault(I18NResourceBundle i18n, String s, Object[] o) {
            super(i18n.getString(s, o));
        }

        /**
         * Create a Fault, by wrapping a CommandContext Fault.
         * The message string will be propagated directly;
         * the argument fault will be set as the cause for this fault.
         * @param e A CommandContext.Fault to wrap.
         */
        public Fault(CommandContext.Fault e) {
            super(e.getMessage(), e);
        }

        /**
         * Get the command that created this fault.
         * @return the command that created this fault
         */
        public Command getCommand() {
            return Command.this;
        }

    }

    /**
     * Create an instance of a command.
     * @param name The name for this command.
     * The name will be saved as the first entry as the argument array.
     */
    protected Command(String name) {
        args = new Vector();
        args.add(name);
    }

    /**
     * Record another argument in the argument array.
     * @param arg the argument to be added
     */
    protected void addArg(String arg) {
        args.add(arg);
    }

    /**
     * Get another argument from the iterator, and add it to the argument array.
     * @param argIter the iterator from which to get the next argument
     * @return the next argument from the iterator
     */
    protected String nextArg(Iterator argIter) {
        String s = (String) argIter.next();
        addArg(s);
        return s;
    }

    /**
     * Back up the iterator to reject an argument, and remove the corresponding
     * entry from the argument array.
     * @param argIter the iterator from which teh argument was obtained
     */
    protected void putbackArg(ListIterator argIter) {
        argIter.previous();
        args.remove(args.size() - 1);
    }

    /**
     * Get the array of arguments for this command.
     * The first element in the array will be the command name;
     * the subsequent arguments will be the ones added by the addArg method.
     * @return the array of arguments for this command
     */
    public String[] getArgs() {
        String[] a = new String[args.size()];
        args.copyInto(a);
        return a;
    }

    /**
     * Get a printable representation of this command.
     * The string is composed of the entries in the argument array.
     * @return a printable representation of this command
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < args.size(); i++) {
            if (sb.length() > 0)
                sb.append(' ');
            String arg = (String) (args.elementAt(i));
            boolean hasSpace = (arg.indexOf(' ') != -1);
            boolean hasQuote = (arg.indexOf('"') != -1);
            boolean hasEscape = (arg.indexOf('\\') != -1);
            if (hasSpace)
                sb.append('"');
            if (hasQuote || hasEscape) {
                for (int ci = 0; ci < arg.length(); ci++) {
                    char c = arg.charAt(ci);
                    if (c == '"' || c == '\\')
                        sb.append('\\');
                    sb.append(c);
                }
            }
            else
                sb.append(arg);
            if (hasSpace)
                sb.append('"');
        }
        return sb.toString();
    }

    /**
     * Get the desktop mode for this command. Valid responses are one of
     * DEFAULT_DTMODE, DESKTOP_NOT_REQUIRED_DTMODE, DESKTOP_REQUIRED_DTMODE.
     * The default is DESKTOP_NOT_REQUIRED_DTMODE if isActionCommand is true,
     * and DESKTOP_DEFAULT_DTMODE otherwise.
     * @return a value indicating the desktop mode for this command
     * @see #DEFAULT_DTMODE
     * @see #DESKTOP_NOT_REQUIRED_DTMODE
     * @see #DESKTOP_REQUIRED_DTMODE
     */
    public int getDesktopMode() {
        return (isActionCommand() ? DESKTOP_NOT_REQUIRED_DTMODE : DEFAULT_DTMODE);
    }

    /**
     * Get the classpath to load the custom splash screen from.
     * At this location, it is expected that a resource bundle prefixed with
     * "splash" will be available.  The search strategy given in ResourceBundle
     * will be used, with the returned File as the classpath for the class loader.
     * The limited classpath/classloader is used to make this operation as fast
     * as possible, rather than requiring that the command's entire context be
     * loaded.
     *
     * In the resource bundle, there should be a property named
     * <code>startup.icon</code>.
     * @return the location of the splash screen resource bundle
     * @see java.util.ResourceBundle
     * @since 4.0
     */
    public URL getCustomSplash() {
        return null;
    }

    ClassLoader getCustomHelpLoader() {
        return null;
    }

    /**
     * A value to indicate that a command accepts the default desktop mode.
     * This means that it neither requires nor discourages the use of a desktop
     * for its use.
     * @see #getDesktopMode
     * @see #DESKTOP_NOT_REQUIRED_DTMODE
     * @see #DESKTOP_REQUIRED_DTMODE
     */
    public static final int DEFAULT_DTMODE = 0;

    /**
     * A value to indicate that a command does not require the use of
     * a desktop to function.
     * @see #getDesktopMode
     * @see #DEFAULT_DTMODE
     * @see #DESKTOP_REQUIRED_DTMODE
     */
    public static final int DESKTOP_NOT_REQUIRED_DTMODE = 1;

    /**
     * A value to indicate that a command requires the use of
     * a desktop to function.
     * @see #getDesktopMode
     * @see #DEFAULT_DTMODE
     * @see #DESKTOP_NOT_REQUIRED_DTMODE
     */
    public static final int DESKTOP_REQUIRED_DTMODE = 2;

    /**
     * Check whether this command is an action command or not. Action commands
     * are those that do work such as running tests, writing a report, etc.
     * The default implementation is to return false.
     * @return true if this command is an action command, and false otherwise
     */
    public boolean isActionCommand() {
        return false;
    }

    /**
     * Execute the work embodied by this command, using the given command context.
     * @param ctx context information that may be set up by preceding commands.
     * @throws Command.Fault if there is an error while executing this command
     */
    public abstract void run(CommandContext ctx) throws Fault;

    /**
     * A convenience method to get the configuration from a command context,
     * and rewrapping any exception that might occur.
     * @param ctx the command context from which to get the configuration
     * @return the current configuration from the command context
     * @throws Command.Fault if there is a problem obtaining or evaluating
     * the configuration.
     */
    protected InterviewParameters getConfig(CommandContext ctx)
        throws Command.Fault
    {
        try {
            return ctx.getConfig();
        }
        catch (CommandContext.Fault e) {
            throw new Fault(e);
        }
    }

    private Vector args;
}
