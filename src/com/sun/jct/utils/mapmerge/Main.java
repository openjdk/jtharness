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
package com.sun.jct.utils.mapmerge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FileSet;

/**
 * A utility to merge JavaHelp map files.
 */
public class Main
{
    /**
     * An exception to report bad command line arguments.
     */
    public static class BadArgs extends Exception {
        BadArgs(String msg) {
            super(msg);
        }
    }

    /**
     * Command line entry point.<br>
     * @param args Command line arguments, per the usage as described.
     */
    public static void main(String[] args) {
        try {
            if (args.length == 0)
                usage(System.err);
            else {
                Main m = new Main(args);
                m.run();
            }
        }
        catch (BadArgs e) {
            System.err.println(e);
            usage(System.err);
            System.exit(1);
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
        String program = System.getProperty("program", "java " + Main.class.getName());
        out.println("Usage:");
        out.println("   " + program + " options files...");
        out.println("");
        out.println("Arguments:");
        out.println("-o file");
        out.println("        Output file.");
        out.println("files...");
        out.println("        Input files to be merged.");
    }

    public Main() { }

    /**
     * Create an object based on command line args.
     * It is an error if no input files or no output file is given.
     * @param args Command line args.
     * @see #main
     * @throws Main.BadArgs if problems are found in the given arguments.
     */
    public Main(String[] args) throws BadArgs {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-o") && i + 1 < args.length) {
                outFile = new File(args[++i]);
            }
            else {
                inFiles = new File[args.length - i];
                for (int j = 0; j < inFiles.length; j++)
                    inFiles[j] = new File(args[i++]);
            }

        }
    }

    public static class Ant extends MatchingTask {
        private Main m = new Main();
        private List/*<FileSet>*/ fileSets = new ArrayList/*<FileSet>*/();

        public void setOutFile(File file) {
            m.outFile = file;
        }

        public void addFileSet(FileSet fs) {
            fileSets.add(fs);
        }

        public void execute() {
            for (Iterator iter = fileSets.iterator(); iter.hasNext(); ) {
                FileSet fs = (FileSet) iter.next();
                FileScanner s = fs.getDirectoryScanner(getProject());
                m.addFiles(s.getBasedir(), s.getIncludedFiles());
            }
            try {
                m.run();
            } catch (BadArgs e) {
                throw new BuildException(e.getMessage());
            } catch (IOException e) {
                throw new BuildException(e);
            }
        }
    }

    public void addFiles(File baseDir, String[] paths) {
        if (paths == null)
            return;
        List/*<File>*/ files = new ArrayList/*<File>*/();
        if (inFiles != null)
            files.addAll(Arrays.asList(inFiles));
        for (int i = 0; i < paths.length; i++)
            files.add(new File(baseDir, paths[i]));
        inFiles = (File[]) files.toArray(new File[files.size()]);
    }

    private void run() throws BadArgs, IOException
    {

        if (inFiles == null || inFiles.length == 0)
            throw new BadArgs("no input files specified");

        if (outFile == null)
            throw new BadArgs("no output file specified");

        map = new TreeMap();

        for (int i = 0; i < inFiles.length; i++)
            read(inFiles[i]);

        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
        out.println("<?xml version='1.0' encoding='ISO-8859-1' ?>");
        out.println("<!DOCTYPE map");
        out.println("  PUBLIC \"-//Sun Microsystems Inc.//DTD JavaHelp Map Version 1.0//EN\"");
        out.println("         \"http://java.sun.com/products/javahelp/map_1_0.dtd\">");
        out.println("<map version=\"1.0\">");

        int maxLen = 0;
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry e = (Map.Entry) (iter.next());
            String target = (String) (e.getKey());
            String url = (String) (e.getValue());
            maxLen = Math.max(maxLen, target.length());
        }

        for (Iterator iter = map.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry e = (Map.Entry) (iter.next());
            String target = (String) (e.getKey());
            String url = (String) (e.getValue());
            out.print("  <mapID target=\"" + target + "\"  ");
            for (int i = target.length(); i < maxLen; i++)
                out.print(' ');
            out.println(" url=\"" + url + "\" />");
        }

        out.println("</map>");
        out.close();
    }

    private void read(File f) throws IOException {
        Reader in = new BufferedReader(new FileReader(f));
        currFile = f;
        read(in);
        in.close();
    }

    private void read(Reader in) throws IOException {
        this.in = in;
        line = 1;
        nextCh();
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

                case '?':
                    nextCh();
                    skipTag();
                    break;

                case '/':
                    nextCh();
                    skipTag();
                    break;

                default:
                    String startTag = scanIdentifier();
                    if (isMapID(startTag))
                        scanMapID(true);
                    else
                        skipTag();
                }
            }
            else {
                nextCh();
            }
        }

    }

    private boolean isMapID(String tag) {
        return tag.equals("mapID");
    }

    private void scanMapID(boolean start) throws IOException {
        String target = null;
        String url = null;

        skipSpace();
        while (c != '>') {
            if (c == '/') {
                nextCh();
                if (c == '>')
                    break;
                else
                    throw new IOException("error parsing HTML input (" + currFile + ":" + line + ")");
            }

            String att = scanIdentifier();
            if (att.equals("target")) {
                target = scanValue();
            }
            else if (att.equals("url")) {
                url = scanValue();
            }
            else
                scanValue();
            skipSpace();
        }
        map.put(target, url);
        nextCh();
    }

    /**
     * Read an identifier
     */
    private String scanIdentifier() throws IOException {
        StringBuffer buf = new StringBuffer();
        while (true) {
            if ((c >= 'a') && (c <= 'z')) {
                buf.append((char)c);
                nextCh();
            } else if ((c >= 'A') && (c <= 'Z')) {
                buf.append((char)c);
                nextCh();
            } else if ((c >= '0') && (c <= '9')) {
                buf.append((char)c);
                nextCh();
            } else if (c == '-') {  // needed for <META HTTP-EQUIV ....>
                buf.append((char)c);
                nextCh();
            } else
                if (buf.length() == 0)
                    throw new IOException("Identifier expected (" + currFile + ":" + line + ")");
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
                throw new IOException("mismatched quotes (" + currFile + ":" + line + ")");
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
        while (c != -1 && (numHyphens < 2 || c != '>')) {
            if (c == '-')
                numHyphens++;
            else
                numHyphens = 0;
            nextCh();
            //System.out.print((char)c);
        }
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
     * Skip the contents of a tag i.e. <...>
     */
    private void skipTag() throws IOException {
        skipSpace();
        while (c != '>') {
            if (c == '/' || c == '?') {
                nextCh();
                if (c == '>')
                    break;
                else
                    throw new IOException("error parsing HTML input (" + currFile + ":" + line + ")");
            }

            String att = scanIdentifier();
            if (att == "")
                throw new IOException("error parsing HTML input (" + currFile + ":" + line + ")");
            String value = scanValue();
            skipSpace();
        }
        nextCh();
    }


    /**
     * Read the next character.
     */
    private void nextCh() throws IOException {
        c = in.read();
        if (c == '\n')
            line++;
    }


    private File[] inFiles;
    private File outFile;
    private Map map;

    private Reader in;
    private int c;
    private File currFile;
    private int line;

}
