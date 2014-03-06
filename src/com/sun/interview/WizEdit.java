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
package com.sun.interview;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * An API (with a basic front-end application) for batch editing an
 * interview.
 */
public class WizEdit
{
    /**
     * This exception is used to indicate a problem with the command line arguments.
     */

    public static class BadArgs extends Exception
    {
        /**
         * Create a BadArgs exception.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         */
        BadArgs(ResourceBundle i18n, String s) {
            super(i18n.getString(s));
        }

        /**
         * Create a BadArgs exception.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An argument to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        BadArgs(ResourceBundle i18n, String s, Object o) {
            super(MessageFormat.format(i18n.getString(s), new Object[] {o}));
        }


        /**
         * Create a BadArgs exception.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An array of arguments to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        BadArgs(ResourceBundle i18n, String s, Object[] o) {
            super(MessageFormat.format(i18n.getString(s), o));
        }
    }

    /**
     * This exception is to report problems that occur while editing
     * the responses to questions in an interview.
     */
    public static class Fault extends Exception
    {
        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         */
        Fault(ResourceBundle i18n, String s) {
            super(i18n.getString(s));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An argument to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        Fault(ResourceBundle i18n, String s, Object o) {
            super(MessageFormat.format(i18n.getString(s), new Object[] {o}));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An array of arguments to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        Fault(ResourceBundle i18n, String s, Object[] o) {
            super(MessageFormat.format(i18n.getString(s), o));
        }
    }

    /**
     * Simple command-line front-end to the facilities of the API.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        try {
            Vector v = new Vector();
            File interviewFile = null;
            File outFileName = null;

            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-o") && i + 1 < args.length)
                    outFileName = new File(args[++i]);
                else if (args[i].equals("-e"))
                    v.addElement(args[++i]);
                else if (args[i].startsWith("-"))
                    throw new BadArgs(i18n, "edit.badOption", args[i]);
                else if (i == args.length - 1 && args[i].endsWith(".jti"))
                    interviewFile = new File(args[i]);
                else
                    throw new BadArgs(i18n, "edit.badOption", args[i]);
            }

            if (interviewFile == null)
                throw new BadArgs(i18n, "edit.noInterview");

            Interview interview;

            try {
                InputStream in = new BufferedInputStream(new FileInputStream(interviewFile));
                Properties p = new Properties();
                p.load(in);
                String interviewClassName = (String)p.get("INTERVIEW");
                if (interviewClassName == null)
                    throw new Fault(i18n, "edit.noInterview");
                Class ic = Class.forName(interviewClassName);
                interview = (Interview)(ic.newInstance());
                interview.load(p, false);
            }
            catch (FileNotFoundException e) {
                throw new Fault(i18n, "edit.cantFindFile", interviewFile);
            }
            catch (IOException e) {
                throw new Fault(i18n, "edit.cantReadFile", e);
            }

            String[] cmds = new String[v.size()];
            v.copyInto(cmds);

            WizEdit editor = new WizEdit(interview);
            editor.edit(cmds);

            try {
                OutputStream out = new BufferedOutputStream(new FileOutputStream(outFileName));
                Properties p = new Properties();
                interview.save(p);
                p.store(out, "Interview: " + interview.getTitle());
            }
            catch (IOException e) {
                throw new Fault(i18n, "edit.cantWriteFile", e);
            }
        }
        catch (BadArgs e) {
            System.err.println("Error: " + e.getMessage());
            //usage();
            System.exit(1);
        }
        catch (Interview.Fault e) {
            System.err.println("Problem reading file: " + e);
            System.exit(2);
        }
        catch (ClassNotFoundException e) {
            System.err.println("Problem reading file: the interview could not be loaded because some classes that are required by the interview were not found on your classpath. The specific exception that occurred was: " + e);
            System.exit(2);
        }
        catch (IllegalAccessException e) {
            System.err.println("Problem reading file: the interview could not be loaded because some classes that are required by the interview caused access violations. The specific exception that occurred was: " + e);
            System.exit(2);
        }
        catch (InstantiationException e) {
            System.err.println("Problem reading file: the interview could not be loaded because some classes that are required by the interview could not be instantiated. The specific exception that occurred was: " + e);
            System.exit(2);
        }
        catch (Fault e) {
            System.err.println(e.getMessage());
            System.exit(2);
        }
    }

    /**
     * Create an editor for the questions in an interview.
     * @param interview The interview containing the responses to be edited.
     */
    public WizEdit(Interview interview) {
        this.interview = interview;
    }

    /**
     * Set whether or not the edit should be done verbosely.
     * @param verbose Set to true for verbose mode, and false otherwise.
     * @see #setVerbose(boolean, PrintStream)
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Set whether or not the edit should be done verbosely,
     * and set the stream to which tracing information should be output.
     * @param verbose Set to true for verbose mode, and false otherwise.
     * @param out The stream to which verbose output should be directed.
     */
    public void setVerbose(boolean verbose, PrintStream out) {
        this.verbose = verbose;
        this.out = out;
    }

    /**
     * Apply a series of edits to the set of responses in an interview.
     * The edits are applied one at a time, and as each edit is applied,
     * the set of questions in the current interview path may change:
     * specifically, the set of questions after the one that is edited
     * may change.
     * @param cmds A set of editing commands to apply to the responses.
     * @throws WizEdit.Fault if there is a problem while applying the edits.
     * @see #edit(String)
     */
    public void edit(String[] cmds) throws Fault {
        for (int i = 0; i < cmds.length; i++)
            edit(cmds[i]);
    }

    /**
     * Apply an edit to the set of responses in an interview.
     * After the edit is applied, the set of questions in the
     * current interview path may change: specifically, the set
     * of questions after the one that is edited may change.
     * @param cmd An edit command to apply to the responses.
     * @throws WizEdit.Fault if there is a problem while applying the edit.
     * @see #edit(String[])
     */
    public void edit(String cmd) throws Fault {
        if (cmd == null || cmd.length() == 0)
            throw new Fault(i18n, "edit.nullCmd");
        char delim = cmd.charAt(0);
        int left = 0;
        int center = cmd.indexOf(delim, left+1);
        if (center == -1)
            throw new Fault(i18n, "edit.badCmd", cmd);
        int right = cmd.indexOf(delim, center+1);
        String searchText = cmd.substring(left+1, center);
        String replaceText = cmd.substring(center+1, right);
        if (searchText.length() == 0)
            throw new Fault(i18n, "edit.badCmd", cmd);

        Hashtable answers = new Hashtable();
        interview.save(answers);

        Question[] path = interview.getPath();
        for (int i = 0; i < path.length; i++) {
            Question q = path[i];
            try {
                String answer = (String)(answers.get(q.getTag()));
                if (answer == null)
                    continue;
                // // currently hardwired: considerCase: false; word match: false
                // int pos = match(searchText, answer, false, false);
                // if (pos >= 0) {
                //     String newAnswer = answer.substring(0, pos)
                //      + replaceText
                //      + answer.substring(pos+searchText.length());
                if (answer.equalsIgnoreCase(searchText)) {
                    String newAnswer = replaceText;
                    q.setValue(newAnswer);

                    if (verbose) {
                        Hashtable h = new Hashtable();
                        q.save(h);
                        out.println("Question:     " + q.getSummary());
                        out.println("changed from: " + answer);
                        out.println("          to: " + h.get(q.getTag()));
                    }
                }
            }
            catch (Interview.Fault e) {
                throw new Fault(i18n, "edit.cantSetValue",
                                new Object[] { q.getSummary(), e.getMessage() });
            }
        }

    }

    private static int match(String s1, String s2, boolean considerCase, boolean word) {
        int s1len = s1.length();
        int s2len = s2.length();
        for (int i = 0; i <= s2len - s1len; i++) {
            if (s1.regionMatches(!considerCase, 0, s2, i, s1len)) {
                if (!word || (word &&
                              ( (i == 0 || isBoundaryCh(s2.charAt(i-1)))
                                && (i+s1len == s2.length() || isBoundaryCh(s2.charAt(i+s1len))) )))
                    return i;
            }
        }
        return -1;
    }

    private static boolean isBoundaryCh(char c) {
        return !(Character.isUnicodeIdentifierStart(c)
                 || Character.isUnicodeIdentifierPart(c));
    }

    private Interview interview;
    private boolean considerCase = false;
    private boolean word = false;
    private boolean verbose;
    private PrintStream out = System.err;

    private static final ResourceBundle i18n = Interview.i18n;
}
