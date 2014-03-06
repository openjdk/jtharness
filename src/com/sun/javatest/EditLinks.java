/*
 * $Id$
 *
 * Copyright (c) 1996, 2009, Oracle and/or its affiliates. All rights reserved.
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
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import com.sun.javatest.util.I18NResourceBundle;

/**
 * A utility to rewrite links within a set of HTML files.
 */
public class EditLinks
{
    /**
     * An exception to report bad command line arguments.
     */
    public static class BadArgs extends Exception {
        BadArgs(I18NResourceBundle i18n, String key) {
            super(i18n.getString(key));
        }
        BadArgs(I18NResourceBundle i18n, String key, Object arg) {
            super(i18n.getString(key, arg));
        }
    }

    /**
     * Command line entry point.<br>
     * Usage:
     * <pre>
     *    java com.sun.javatest.EditLinks options files...
     * </pre>
     * Arguments:
     * <dl>
     * <dt>-e oldPrefix newPrefix
     * <dd>Links beginning with oldPrefix are rewritten to begin with newPrefix
     * <dt>-ignore file
     * <dd>Ignore files and directories named 'file' when scanning directories.
     *     E.g. -ignore SCCS
     * <dt>-o file
     * <dd>Output file or directory. It should only be a file if the input is a
     *     single file; otherwise it should be a directory.
     * <dt>files...
     * <dd>Input files or directories to be copied, with the links edited.
     * </dl>
     * @param args Command line arguments, per the usage as described.
     */
    public static void main(String[] args) {
        try {
            if (args.length == 0)
                usage(System.err);
            else {
                EditLinks el = new EditLinks(args);
                if (el.edits.length == 0)
                    System.err.println(i18n.getString("editLinks.noEdits"));
                el.run();
            }
        }
        catch (BadArgs e) {
            System.err.println(e.getMessage());
            usage(System.err);
            System.exit(1);
        }
        catch (IOException e) {
            System.err.println(e);
        }
        catch (Throwable t) {
            t.printStackTrace();
            System.exit(2);
        }
    }

    /**
     * Write out short command line help.
     * @param out A stream to which to write the help.
     */
    private static void usage(PrintStream out) {
        String program = System.getProperty("program", "java " + EditLinks.class.getName());
        String msg = i18n.getString("editLinks.usage", program);
        int start = 0;
        int i;
        while ((i = msg.indexOf("\n", start)) != -1) {
            System.err.println(msg.substring(start, i));
            start = i + 1;
        }
        if (start < msg.length())
            System.err.println(msg.substring(start));
    }

    /**
     * Create an empty editor object.
     */
    public EditLinks() {
    }

    /**
     * Create an editor object based on command line args.
     * It is an error if no edits, no input files, or no output file is given.
     * @param args Command line args.
     * @see #main
     * @throws EditLinks.BadArgs if problems are found in the given arguments.
     */
    public EditLinks(String[] args) throws BadArgs {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-e") && i + 2 < args.length) {
                String oldPrefix = args[++i];
                String newPrefix = args[++i];
                addEdit(oldPrefix, newPrefix);
            }
            else if (args[i].equals("-ignore") && i + 2 < args.length) {
                ignore(args[++i]);
            }
            else if (args[i].equals("-o") && i + 1 < args.length) {
                outFile = new File(args[++i]);
            }
            else if (args[i].startsWith("-")) {
                throw new BadArgs(i18n, "editLinks.badOpt", args[i]);
            }
            else {
                inFiles = new File[args.length - i];
                for (int j = 0; j < inFiles.length; j++)
                    inFiles[j] = new File(args[i++]);
            }
        }

        if (inFiles == null || inFiles.length == 0)
            throw new BadArgs(i18n, "editLinks.noInput");

        if (outFile == null) {
            if (inFiles.length == 1)
                outFile = inFiles[0];
            else
                throw new BadArgs(i18n, "editLinks.noOutput");
        }
    }

    /**
     * Add another edit to be applied when the files are edited.
     * @param oldPrefix The prefix of HTML references to be updated.
     * @param newPrefix The replacement value for occurrences of oldPrefix.
     */
    public void addEdit(String oldPrefix, String newPrefix) {
        String[][] newEdits = new String[edits.length + 1][];
        System.arraycopy(edits, 0, newEdits, 0, edits.length);
        newEdits[edits.length] = new String[] { oldPrefix, newPrefix };
        edits = newEdits;
    }

    /**
     * Add another file to be ignored when the files are edited.
     * For example, specify "SCCS" to ignore SCCS directories.
     * @param file The name of a file to be ignored when editing.
     */
    public void ignore(String file) {
        ignores.add(file);
    }

    /**
     * Edit the files set up by the {@link #EditLinks(String[])} constructor.
     * @throws IOException if any errors occur while editing the specified files.
     */
    public void run() throws IOException {
        edit(inFiles, outFile);
    }

    /**
     * Edit the given files, using the current set of edits and ignores.
     * The source files may be files or directories; the destination can
     * be a directory, or a file if the source is a single file.
     * @param src An array of files or directories of files to be edited.
     * @param dest A destination file for the edit.
     * @throws IOException if any problems occur while editing the specified
     * files.
     * @throws IllegalArgumentException if the destination is a single file
     * but the source file is not.
     * @see #edit(File, File)
     */
    public void edit(File[] src, File dest) throws IOException {
        for (int i = 0; i < src.length; i++)
            edit(src[i], dest);
    }

    /**
     * Edit the given file, using the current set of edits and ignores.
     * The source files may be file or directory; the destination can
     * be a directory, or a file if the source is a single file.
     * @param src A file or directory of files to be edited.
     * @param dest A destination file for the edit.
     * @throws IOException if any problems occur while editing the specified
     * files.
     * @throws IllegalArgumentException if the destination is a single file
     * but the source file is not.
     * @see #edit(File, File)
     */
    public void edit(File src, File dest) throws IOException {
        if (!src.exists())
            throw new FileNotFoundException(src.getPath());

        if (src.isDirectory()) {
            if (!dest.exists()) {
                if (!dest.mkdir())
                    throw new FileNotFoundException(dest.getPath());
            }
            else if (!dest.isDirectory())
                throw new IllegalArgumentException(i18n.getString("editLinks.dirExpected", dest));

            File canonicalDest = dest.getCanonicalFile();
            ignores.add(canonicalDest);
            String[] files = src.list();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    String file = files[i];
                    File srcFile = new File(src, file);
                    File destFile = new File(dest, file);
                    if (ignores.contains(file) || ignores.contains(srcFile.getCanonicalFile())) {
                        continue;
                    }
                    edit(srcFile, destFile);
                }
            }
            ignores.remove(canonicalDest);
        }
        else {
            if (dest.exists() && dest.isDirectory()) {
                // source is a file, but the target is a directory, so assume a
                // file of the same name in the target directory
                dest = new File(dest, src.getName());
            }

            if (src.getName().endsWith(".html")) {
                in = new BufferedReader(new FileReader(src));
                if (dest.equals(src)) {
                    int size = (int)(src.length());
                    char data[] = new char[size];
                    for (int total = 0; total < data.length; ) {
                        total += in.read(data, total, data.length - total);
                    }
                    in.close();
                    String s = new String(data, 0, data.length);
                    in = new StringReader(s);
                }
                out = new BufferedWriter(new FileWriter(dest));
                currFile = src;
                line = 1;
                edit(in, out);
                in.close();
                out.close();
            }
            else
                copyFile(src, dest);
        }
    }

    /**
     * Copy a file.
     */
    private void copyFile(File from, File to) throws IOException {
        if (from.equals(to))
            return;

        int size = (int)(from.length());
        byte data[] = new byte[size];

        InputStream in = new BufferedInputStream(new FileInputStream(from));
        try {
            for (int total = 0; total < data.length; ) {
                total += in.read(data, total, data.length - total);
            }
        }
        finally {
            in.close();
        }

        OutputStream out = new BufferedOutputStream(new FileOutputStream(to));
        try {
            out.write(data, 0, data.length);
        }
        finally {
            out.close();
        }
    }

    /**
     * Copy the input stream to the output, looking for <a href="....">.
     * If a link is found that begins with one of the given edit strings,
     * it is rewritten to begin with the replacement string.
     */
    private void edit(Reader in, Writer out) throws IOException {
        copying = false;
        nextCh();
        copying = true;
        while (c >= 0) {
            if (c == '<') {
                nextCh();
                skipSpace();
                switch (c) {
                case '!':
                    nextCh();
                    if (c == '-') {
                        nextCh();
                        if (c == '-') {
                            nextCh();
                            skipComment();
                        }
                    }
                    break;

                case '/':
                    nextCh();
                    scanIdentifier();
                    skipTag();
                    break;

                default:
                    String tag = scanIdentifier();
                    if (tag.equals("a"))
                        scanLink();
                    else
                        skipTag();
                }
            }
            else {
                nextCh();
            }
        }

    }

    /**
     * Process the contents of <a href=...>
     */
    private void scanLink() throws IOException {
        skipSpace();
        while (c != '>') {
            String att = scanIdentifier();
            if (att.equalsIgnoreCase("href")) {
                // the current character should be a whitespace or =
                // either way, we just write out =
                out.write('=');
                copying = false;
                String target = scanValue();
                out.write('"');
                out.write(edit(target));
                out.write('"');
                copying = true;
            }
            else
                scanValue();
            skipSpace();
        }
        nextCh();
    }

    /**
     * Read an identifier, and lowercase it
     */
    private String scanIdentifier() throws IOException {
        StringBuffer buf = new StringBuffer();
        while (true) {
            if ((c >= 'a') && (c <= 'z')) {
                buf.append((char)c);
                nextCh();
            } else if ((c >= 'A') && (c <= 'Z')) {
                buf.append((char)('a' + (c - 'A')));
                nextCh();
            } else if ((c >= '0') && (c <= '9')) {
                buf.append((char)c);
                nextCh();
            } else if (c == '-') {  // needed for <META HTTP-EQUIV ....>
                buf.append((char)c);
                nextCh();
            } else
                if (buf.length() == 0)
                    throw new IOException(i18n.getString("editLinks.idExpected",
                                                         new Object[] { currFile, new Integer(line) }));
                else
                    return buf.toString();
        }
    }

    /**
     * Read the value of an HTML attribute, which may be quoted.
     */
    private String scanValue() throws IOException {
        skipSpace();
        if (c != '=')
            return "";

        int quote = -1;
        nextCh();
        skipSpace();
        if ((c == '\'') || (c == '\"')) {
            quote = c;
            nextCh();
            skipSpace();
        }
        StringBuffer buf = new StringBuffer();
        while (((quote < 0) && (c != ' ') && (c != '\t') &&
                (c != '\n') && (c != '\r') && (c != '>')) ||
               ((quote >= 0) && (c != quote))) {
            if (c == -1 || c == '\n' || c == '\r') {
                throw new IOException(i18n.getString("editLinks.mismatchQuotes",
                                                         new Object[] { currFile, new Integer(line) }));
            }
            buf.append((char)c);
            nextCh();
        }
        if (c == quote)
            nextCh();
        skipSpace();
        return buf.toString();
    }

    /**
     * Skip an HTML comment  <!-- ... -->
     */
    private void skipComment() throws IOException {
        // a comment sequence is "<!--" ... "-->"
        // at the time this is called, "<!--" has been read;
        int numHyphens = 0;
        //System.out.print("SKIPCOMMENT: <!--" + (char)c);
        while (c != -1 && (numHyphens < 2 || c != '>')) {
            if (c == '-')
                numHyphens++;
            else
                numHyphens = 0;
            nextCh();
            //System.out.print((char)c);
        }
        //System.out.println("END SKIPCOMMENT");
        nextCh();
    }

    /**
     * Skip whitespace.
     */
    private void skipSpace() throws IOException {
        while ((c == ' ') || (c == '\t') || (c == '\n') || (c == '\r')) {
            nextCh();
        }
    }

    /**
     * Skip the contents of an HTML tag i.e. <...>
     */
    private void skipTag() throws IOException {
        skipSpace();
        while (c != '>') {
            String att = scanIdentifier();
            if (att == "")
                throw new IOException(i18n.getString("editLinks.badHTML",
                                                         new Object[] { currFile, new Integer(line) }));
            String value = scanValue();
            skipSpace();
        }
        nextCh();
    }

    /**
     * Read the next character, after writing out the previous one, if <code>copying</code>
     * is set to <code>true</code>.
     */
    private void nextCh() throws IOException {
        if (copying)
            out.write((char) c);
        c = in.read();
        if (c == '\n')
            line++;
    }

    /**
     * Edit the contents of a link. If it begins with one of the current set of
     * edits, the prefix is replaced with the corresponding new value.
     */
    private String edit(String ref) {
        for (int i = 0; i < edits.length; i++) {
            String[] entry = edits[i];
            if (ref.startsWith(entry[0])) {
                String oldHead = entry[0];
                String newHead = entry[1];
                char oldSep = guessSep(oldHead);
                char newSep = guessSep(newHead);
                String oldTail = ref.substring(oldHead.length());
                String newTail;
                if (oldSep != 0 && newSep != 0 && oldSep != newSep)
                    newTail = oldTail.replace(oldSep, newSep);
                else
                    newTail = oldTail;
                return (newHead + newTail);
            }
        }
        return ref;
    }

    /**
     * Guess the file separator in a string by looking for whichever
     * is later of / and \. If neither are found, the result is 0.
     */
    private static char guessSep(String path) {
        int fwd = path.lastIndexOf('/');
        int back = path.lastIndexOf('\\');
        if (fwd > back)
            return '/';
        else if (back > fwd)
            return '\\';
        else
            return (char)0;
    }

    private File[] inFiles = new File[0];
    private File outFile;
    private String[][] edits = new String[0][];
    private Set ignores = new HashSet();

    private int c;
    private int line;
    private File currFile;
    private Reader in;
    private Writer out;
    private boolean copying;

    private static final I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(EditLinks.class);
}
