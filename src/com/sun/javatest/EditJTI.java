/*
 * $Id$
 *
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.ResourceBundle;

import com.sun.interview.Interview;
import com.sun.interview.Question;
import com.sun.interview.WizPrint;
import com.sun.javatest.util.BackupPolicy;
import com.sun.javatest.util.I18NResourceBundle;

/**
 * This class provides a utility for command-line editing of configuration (.jti) files.
 * It is intended to be invoked from the command line, as in: <pre>
 * java com.sun.javatest.EditJIT options...
 * </pre>
 * For details of the options, use the <code>-help</code> option.
 */
public class EditJTI
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
         */     BadArgs(ResourceBundle i18n, String s, Object o) {
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
     * This exception is used to report problems that arise when using this API.
     */
    public static class Fault extends Exception
    {
        Fault(I18NResourceBundle i18n, String s) {
            super(i18n.getString(s));
        }

        Fault(I18NResourceBundle i18n, String s, Object o) {
            super(i18n.getString(s, o));
        }

        Fault(I18NResourceBundle i18n, String s, Object[] o) {
            super(i18n.getString(s, o));
        }
    }

    /**
     * Command line entry point. Run with <code>-help</code> to get
     * brief command line help.  Warning: this method uses System.exit
     * and so does not return if called directly.
     * @param args Comamnd line arguments.
     */
    public static void main(String[] args) {
        try {
            EditJTI e = new EditJTI();
            boolean ok = e.run(args);
            System.exit(ok ? 0 : 1);
        }
        catch (BadArgs e) {
            System.err.println(e.getMessage());
            usage(System.err);
            System.exit(2);
        }
        catch (Fault e) {
            System.err.println(e.getMessage());
            System.exit(3);
        }
    }

    /**
     * Print out brief command line help.
     * @param out the stream to which to write the command line help.
     */
    public static void usage(PrintStream out) {
        String prog = System.getProperty("program", "java " + EditJTI.class.getName());
        out.println(i18n.getString("editJTI.usage.title"));
        out.print("  ");
        out.print(prog);
        out.println(i18n.getString("editJTI.usage.summary"));

        out.println(i18n.getString("editJTI.usage.options"));
        out.println(i18n.getString("editJTI.usage.help1"));
        out.println(i18n.getString("editJTI.usage.help2"));
        out.println(i18n.getString("editJTI.usage.help3"));
        out.println(i18n.getString("editJTI.usage.classpath1"));
        out.println(i18n.getString("editJTI.usage.classpath2"));
        out.println(i18n.getString("editJTI.usage.log1"));
        out.println(i18n.getString("editJTI.usage.log2"));
        out.println(i18n.getString("editJTI.usage.outfile1"));
        out.println(i18n.getString("editJTI.usage.outfile2"));
        out.println(i18n.getString("editJTI.usage.path1"));
        out.println(i18n.getString("editJTI.usage.path2"));
        out.println(i18n.getString("editJTI.usage.preview1"));
        out.println(i18n.getString("editJTI.usage.preview2"));
        out.println(i18n.getString("editJTI.usage.ts1"));
        out.println(i18n.getString("editJTI.usage.ts2"));
        out.println(i18n.getString("editJTI.usage.verbose1"));
        out.println(i18n.getString("editJTI.usage.verbose2"));
        out.println("");
        out.println(i18n.getString("editJTI.usage.edit"));
        out.println(i18n.getString("editJTI.usage.set"));
        out.println(i18n.getString("editJTI.usage.search"));
        out.println("");
    }

    /**
     * Run the utility, without exiting.  Any messages are written to
     * the standard output stream.
     * @param args command line args
     * @return true if the resulting configuration is valid (complete),
     * and false otherwise.
     * @throws EditJTI.BadArgs if there is an error analysing the args
     * @throws EditJTI.Fault if there is an error executing the args
     */
    public boolean run(String[] args) throws BadArgs, Fault {
        PrintWriter out = new PrintWriter(System.out);
        try {
            return run(args, out);
        }
        finally {
            out.flush();
        }
    }


    /**
     * Run the utility, without exiting, writing any messages to a specified stream.
     * @param args command line args
     * @param out the stream to which to write any messages
     * @return true if the resulting configuration is valid (complete),
     * and false otherwise.
     * @throws EditJTI.BadArgs if there is an error analysing the args
     * @throws EditJTI.Fault if there is an error executing the args
     */
    public boolean run(String[] args, PrintWriter out) throws BadArgs, Fault {
        File inFile = null;
        File outFile = null;
        File logFile = null;
        File classPath = null;
        File testSuitePath = null;
        File workDirPath = null;
        String[] editCmds = null;
        boolean helpFlag = false;
        boolean previewFlag = false;
        boolean showPathFlag = false;
        boolean verboseFlag = false;

        for (int i = 0; i < args.length; i++) {
            if ((args[i].equals("-o") || args[i].equals("-out"))
                && i + 1 < args.length) {
                checkUnset(outFile, args[i]);
                outFile = new File(args[++i]);
            }
            else if ((args[i].equals("-i") || args[i].equals("-in"))
                && i + 1 < args.length) {
                checkUnset(inFile, args[i]);
                inFile = new File(args[++i]);
            }
            else if ((args[i].equals("-l") || args[i].equals("-log"))
                     && i + 1 < args.length) {
                checkUnset(logFile, args[i]);
                logFile = new File(args[++i]);
            }
            else if (args[i].equals("-n") || args[i].equals("-preview"))
                previewFlag = true;
            else if (args[i].equals("-p") || args[i].equals("-path"))
                showPathFlag = true;
            else if (args[i].equals("-v") || args[i].equals("-verbose")  )
                verboseFlag = true;
            else if ((args[i].equals("-cp") || args[i].equals("-classpath")) && i + 1 < args.length) {
                checkUnset(classPath, args[i]);
                classPath = new File(args[++i]);
            }
            else if ((args[i].equals("-ts") || args[i].equals("-testsuite")) && i + 1 < args.length) {
                checkUnset(testSuitePath, args[i]);
                testSuitePath = new File(args[++i]);
            }
            else if ((args[i].equals("-wd") || args[i].equals("-workdir")) && i + 1 < args.length) {
                checkUnset(testSuitePath, args[i]);
                workDirPath = new File(args[++i]);
            }
            else if (args[i].equals("-help") || args[i].equals("-usage") || args[i].equals("/?") )
                helpFlag = true;
            else if (args[i].startsWith("-"))
                throw new BadArgs(i18n, "editJTI.badOption", args[i]);
            else if (i <= args.length - 1) {
                if (inFile == null) {
                    editCmds = new String[args.length - 1 - i];
                    System.arraycopy(args, i, editCmds, 0, editCmds.length);
                    inFile = new File(args[args.length - 1]);
                }
                else {
                    editCmds = new String[args.length - i];
                    System.arraycopy(args, i, editCmds, 0, editCmds.length);
                }
                i = args.length - 1;
            }
            else
                throw new BadArgs(i18n, "editJTI.badOption", args[i]);
        }

        if (args.length == 0 || helpFlag) {
            usage(System.out);
            if (inFile == null)
                return true;
        }

        if (classPath != null && testSuitePath != null)
            throw new BadArgs(i18n, "editJTI.cantHaveClassPathAndTestSuite");

        if (inFile == null)
            throw new BadArgs(i18n, "editJTI.noInterview");

        // if (editCmds.length == 0 && outFile == null && logFile == null && !showPathFlag)
        //     throw new BadArgs(...no.actions....);

        verbose = verboseFlag;
        this.out = out;

        try {
            /* the following looks nice and simple, but breaks compatibility
               with 3.1.4, because InterviewParameters.open will try and open
               the wd in the .jti file if not given explicitly -- and previously,
               this was not required/done.  So, only use the simple code if wd
               is set, and use the old 3.1.4 code if wd is not set.
            */
            /* See comment above
            if (workDirPath != null || testSuitePath != null) {
                interview = InterviewParameters.open(testSuitePath, workDirPath, inFile);
            }
            */
            if (workDirPath != null)
                interview = InterviewParameters.open(testSuitePath, workDirPath, inFile);
            else if (testSuitePath != null) {
                // only open the test suite, not the work dir
                TestSuite ts;
                try {
                    ts = TestSuite.open(testSuitePath);
                }
                catch (FileNotFoundException e) {
                    throw new Fault(i18n, "editJTI.cantFindTestSuite", testSuitePath);
                }
                catch (TestSuite.NotTestSuiteFault e) {
                    throw new Fault(i18n, "editJTI.notATestSuite", testSuitePath);
                }
                catch (TestSuite.Fault e) {
                    throw new Fault(i18n, "editJTI.cantOpenTestSuite",
                                    new Object[] { testSuitePath, e });
                }
                load(inFile, ts);
            }
            // End of patches for 3.1.4 compatibility
            else if (classPath != null) {
                URLClassLoader loader = new URLClassLoader(new URL[] { classPath.toURL() });
                load(inFile, loader);
            }
            else
                load(inFile);

        }
        catch (Interview.Fault e) {
            throw new Fault(i18n, "editJTI.cantOpenFile",
                            new Object[] { inFile.getPath(), e.getMessage() });
        }
        catch (FileNotFoundException e) {
            throw new Fault(i18n, "editJTI.cantFindFile", inFile.getPath());
        }
        catch (IOException e) {
            throw new Fault(i18n, "editJTI.cantOpenFile",
                            new Object[] { inFile.getPath(), e });
        }
        catch (IllegalStateException e) {
            // only occurs if keywords are being used in the config, and the
            // test suite is not available.  user needs to specify -wd or -ts
            if (verbose)
                e.printStackTrace();

            throw new Fault(i18n, "editJTI.badState", e.getMessage());
        }

        if (NUM_BACKUPS > 0)
            interview.setBackupPolicy(BackupPolicy.simpleBackups(NUM_BACKUPS));

        if (editCmds != null)
            edit(editCmds);


        if (showPathFlag)
            showPath();

        try {
            if (logFile != null) {
                if (previewFlag) {
                    String msg = i18n.getString("editJTI.wouldWriteLog", logFile);
                    out.println(msg);
                }
                else
                    writeLog(logFile);
            }
        }
        catch (IOException e) {
            throw new Fault(i18n, "editJTI.cantWriteLog",
                            new Object[] { logFile.getPath(), e });
        }


        try {
            if (previewFlag) {
                String msg;
                if (interview.isEdited())
                    msg = i18n.getString("editJTI.wouldSaveEdited",
                                (outFile != null ? outFile : inFile));
                else if (outFile != null)
                    msg = i18n.getString("editJTI.wouldSaveNotEdited", outFile);
                else
                    msg = i18n.getString("editJTI.wouldNotSave");
                out.println(msg);
            }
            else {
                if (outFile != null)
                    save(outFile);
                else if (interview.isEdited())
                    save(inFile);
            }
        }
        catch (Interview.Fault e) {
            throw new Fault(i18n, "editJTI.cantOpenFile",
                            new Object[] {
                                (outFile == null || outFile.getPath() == null ?
                                 "??": outFile.getPath()), e });
        }
        catch (IOException e) {
            File f = (outFile == null ? interview.getFile() : outFile);
            throw new Fault(i18n, "editJTI.cantSaveFile",
                            new Object[] { f.getPath(), e });
        }

        return (interview.isFinishable());
    }

    /**
     * Load a configuration file to be edited.
     * @param inFile the file to be loaded
     * @throws IOException if there is a problem reading the file
     * @throws Interview.Fault if there is a problem loading the interview data from the file
     */
    public void load(File inFile) throws IOException, Interview.Fault {
        // this opens the interview via the work directory and test suite;
        // the test suite implicitly knows its classpath via the .jtt file
        interview = InterviewParameters.open(inFile);
        interview.setEdited(false);
    }

    /**
     * Load a configuration file to be edited.
     * @param inFile the file to be loaded
     * @param ts the test suite for which the interview is to be loaded
     * @throws IOException if there is a problem reading the file
     * @throws Interview.Fault if there is a problem loading the interview data from the file
     * @throws EditJTI.Fault if there is a problem creating the interview for the testsuite
     */
    public void load(File inFile, TestSuite ts)
        throws IOException, Interview.Fault, Fault
    {
        // this opens the interview via the work directory and test suite;
        // the test suite implicitly knows its classpath via the .jtt file
        try {
            interview = ts.createInterview();
        }
        catch (TestSuite.Fault e) {
            throw new Fault(i18n, "editJTI.cantCreateInterviewForTestSuite",
                            new Object[] { ts.getPath(), e.getMessage() });
        }
        interview.load(inFile);
        interview.setEdited(false);
    }

    /**
     * Load a configuration file to be edited, using a specified class loader
     * to load the interview class.
     * @param inFile the file to be loaded
     * @param loader the class loader to be used to load the interview class
     * @throws IOException if there is a problem reading the file
     * @throws Interview.Fault if there is a problem loading the interview data from the file
     * @throws EditJTI.Fault if there is a problem creating the interview for the testsuite
     */
    public void load(File inFile, URLClassLoader loader)
        throws IOException, Interview.Fault, Fault
    {
        InputStream in = new BufferedInputStream(new FileInputStream(inFile));
        Properties p = new Properties();
        p.load(in);
        in.close();

        String interviewClassName = (String) (p.get("INTERVIEW"));
        try {
            Class interviewClass = loader.loadClass(interviewClassName);

            interview = (InterviewParameters)(interviewClass.newInstance());
        }
        catch (ClassCastException e) {
            throw new Fault(i18n, "editJTI.invalidInterview", inFile);
        }
        catch (ClassNotFoundException e) {
            throw new Fault(i18n, "editJTI.cantFindClass",
                            new Object[] { interviewClassName, inFile });
        }
        catch (InstantiationException e) {
            throw new Fault(i18n, "editJTI.cantInstantiateClass",
                            new Object[] { interviewClassName, inFile });
        }
        catch (IllegalAccessException e) {
            throw new Fault(i18n, "editJTI.cantAccessClass",
                            new Object[] { interviewClassName, inFile });
        }
        finally {
            try { if (in != null) in.close(); } catch (IOException e) {}
        }

        interview.load(inFile);
        interview.setEdited(false);
    }

    /**
     * Save the edited configuration in a specified file.
     * @param file The file in which to save the configuration
     * @throws IOException if there is a problem while writing the file
     * @throws Interview.Fault if there is a problem while saving the interview data
     */
    public void save(File file) throws IOException, Interview.Fault {
        interview.save(file);
    }

    /**
     * Show the current question path for the configuration.
     */
    public void showPath() {
        Question[] path = interview.getPath();

        int indent = 0;
        for (int i = 0; i < path.length; i++)
            indent = Math.max(indent, path[i].getTag().length());
        indent = Math.min(indent, MAX_INDENT);

        for (int i = 0; i < path.length; i++) {
            Question q = path[i];
            String tag = q.getTag();
            String value = q.getStringValue();
            out.print(tag);
            int l = tag.length();
            if (l > MAX_INDENT && value != null && value.length() > 0) {
                out.println();
                l = 0;
            }
            for (int x = l; x < indent; x++)
                out.print(' ');
            out.print(' ');
            out.println(value == null ? "" : value);
        }
    }

    /**
     * Write a log of the questions that determine the current configuration.
     * @param logFile the file to which to write the log
     * @throws IOException if there is a problem while writing the log file
     */
    public void writeLog(File logFile) throws IOException {
        WizPrint wp = new WizPrint(interview);
        wp.setShowResponses(true);
        wp.setShowResponseTypes(false);
        wp.setShowTags(true);
        BufferedWriter out = new BufferedWriter(new FileWriter(logFile));
        wp.write(out);
    }

    /**
     * Apply a series of edits to the current configuration.
     * @param cmds the editing commands to be applied
     * @throws EditJTI.Fault if there is a problem while applying the edit commands.
     * @see #edit(String)
     */
    public void edit(String[] cmds) throws Fault {
        for (int i = 0; i < cmds.length; i++) {
            edit(cmds[i]);
        }
    }

    /**
     * Apply an edit to the current configuration.
     * @param cmd the editing command to be applied
     * Currently, two forms of command are supported: <dl>
     * <dt><em>tag-name=value</em>
     * <dd>Set the response to the question whose value is <em>tag-name</em> to <em>value</em>
     * <dt><em>/search/replace/</em>
     * <dd>For all questions on the current path, change instances of <em>search</em> to  <em>replace</em>
     * </dl>
     * @throws EditJTI.Fault if there is a problem while applying the edit commands.
     * @see #edit(String[])
     */
    public void edit(String cmd) throws Fault {
        if (cmd == null || cmd.length() == 0)
            return;

        int eqIndex = cmd.indexOf('=');
        if (Character.isJavaIdentifierStart(cmd.charAt(0)) && eqIndex > 0)
            setValue(cmd.substring(0, eqIndex), cmd.substring(eqIndex + 1));
        else if (cmd.toLowerCase().startsWith("import:")) {
            importFile(new File(cmd.substring("import:".length())));
        }
        else {
            int left = 0;
            // could support a command letter in front?
            char delim = cmd.charAt(left);
            int center = cmd.indexOf(delim, left + 1);
            if (center == -1)
                throw new Fault(i18n, "editJTI.badCmd", cmd);
            // could support trailing flags?
            int right = cmd.length() - 1;
            if (cmd.charAt(right) != delim)
                throw new Fault(i18n, "editJTI.badCmd", cmd);
            String searchText = cmd.substring(left + 1, center);
            String replaceText = cmd.substring(center + 1, right);
            if (searchText.length() == 0)
                throw new Fault(i18n, "editJTI.badCmd", cmd);
            setMatchingValues(searchText, replaceText);
        }
    }

    private void importFile(File file) throws Fault {

        InputStream in;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
        }
        catch (FileNotFoundException e) {
            throw new Fault(i18n, "editJTI.cantFindImport", file);
        }
        catch (IOException e) {
            throw new Fault(i18n, "editJTI.cantOpenImport",
                            new Object[] { file, e });
        }

        Properties p;
        try {
            p = new Properties();
            p.load(in);
            in.close();
        }
        catch (IOException e) {
            throw new Fault(i18n, "editJTI.cantReadImport",
                            new Object[] { file, e });
        }
        finally {
            try { if (in != null) in.close(); } catch (IOException e) {}
        }

        // for each question on the path, see if there is a corresponding
        // imported value
        Question[] path = interview.getPath();
        for (int i = 0; i < path.length; i++) {
            Question q = path[i];
            String v = p.getProperty(q.getTag());
            if (v != null) {
                setValue(q, v);
                path = interview.getPath(); // update path in case tail has changed
            }
        }
    }

    private void setMatchingValues(String searchText, String replaceText) throws Fault {
        boolean found = false;

        Question[] path = interview.getPath();
        for (int i = 0; i < path.length; i++) {
            Question q = path[i];
            String currValue = q.getStringValue();
            if (currValue == null)
                continue;
            // currently hardwired: considerCase: false; word match: false
            int pos = match(searchText, currValue, false, false);
            if (pos >= 0) {
                String newValue = currValue.substring(0, pos)
                    + replaceText
                    + currValue.substring(pos + searchText.length());
                setValue(q, newValue);
                found = true;
                path = interview.getPath(); // update path in case tail has changed
            }
        }
        if (!found)
            throw new Fault(i18n, "editJTI.cantFindMatch", searchText);
    }

    private void setValue(String tag, String value) throws Fault {
        Question[] path = interview.getPath();
        for (int i = 0; i < path.length; i++) {
            Question q = path[i];
            if (q.getTag().equals(tag)) {
                setValue(q, value);
                return;
            }
        }
        throw new Fault(i18n, "editJTI.cantFindQuestion", tag);
    }

    private void setValue(Question q, String value) throws Fault {
        try {
            String oldValue = q.getStringValue();
            q.setValue(value);
            if (verbose)
                out.println(i18n.getString("editJTI.update",
                    new Object[] { q.getTag(), oldValue, q.getStringValue() }));
        }
        catch (Interview.Fault e) {
            throw new Fault(i18n, "editJTI.cantSetValue", new Object[] { q.getTag(), e.getMessage() } );
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

    private static void checkUnset(Object item, String option)
        throws BadArgs
    {
        if (item != null)
            throw new BadArgs(i18n, "editJTI.dupOption",option);
    }

    private InterviewParameters interview;
    private boolean verbose;
    private PrintWriter out;

    private static int MAX_INDENT = Integer.getInteger("EditJTI.maxIndent", 32).intValue();
    private static int NUM_BACKUPS = Integer.getInteger("EditJTI.numBackups", 2).intValue();

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(EditJTI.class);
}
