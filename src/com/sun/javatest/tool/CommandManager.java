/*
 * $Id$
 *
 * Copyright (c) 2004, 2011, Oracle and/or its affiliates. All rights reserved.
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

/**
 * Command managers are relatively lightweight managers for the command objects
 * that embody end-user command-line functionality. There can be multiple command
 * managers, and each manager typically manages a set of related command classes.
 *
 * @see Command
 */
public abstract class CommandManager {
    /**
     * Get an object embodying the command line help for the commands managed
     * by this CommandManager.
     * @return an object embodying the command line help for the commands managed
     * by this CommandManager
     */
    public abstract HelpTree.Node getHelp();

    /**
     * Parse a command (and any arguments it might take).
     * @param cmd the command to be parsed
     * @param argIter an iterator from which to get any arguments that
     * might be required by the option
     * @param ctx a context object to use while parsing the command
     * @return true if the command is recognized and successfully parsed,
     * or false if the command is not recognized by this command manager
     * @throws Command.Fault if the command is recognized by this command manager
     * but could not be successfully parsed or otherwise handled.
     */
    public abstract boolean parseCommand(String cmd, ListIterator argIter, CommandContext ctx)
        throws Command.Fault;

    /**
     * A convenience routine for subtypes to use to see if one string matches another.
     * The two strings match if they are equal, ignoring case.
     * @param s1 A string, such as the command name, to be matched
     * @param s2 Another string, such as a command name, to be matched
     * @return true if the strings match, and false otherwise
     */
    protected static boolean isMatch(String s1, String s2) {
        return (s1.equalsIgnoreCase(s2));
    }

    /**
     * A convenience routine for subtypes to use to see if a string
     * matches one of a set of strings.
     * Two strings match if they are equal, ignoring case.
     * @param s1 A string, such as the command name, to be matched
     * @param s2 An array of strings, such as command names, to be matched
     * @return true if a match is found, and false otherwise
     */
    protected static boolean isMatch(String s1, String[] s2) {
        for (int i = 0; i < s2.length; i++) {
            if (s1.equalsIgnoreCase(s2[i]))
                return true;
        }
        return false;
    }

    /**
     * A convenience routine for subtypes to use to see if a string
     * matches a prefix string.
     * A match occurs if the argument string begins with the prefix string,
     * ignoring case.
     * @param arg A string, such as the command name, to be matched
     * @param prefix The prefix to be matched
     * @return true if a match is found, and false otherwise
     */
    protected static boolean isPrefixMatch(String arg, String prefix) {
        String s1 = prefix.toUpperCase();
        String s2 = arg.toUpperCase();
        if (s2.startsWith(s1))
            return true;
        else
            return false;
    }
}
