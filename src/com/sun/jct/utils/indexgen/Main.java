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
package com.sun.jct.utils.indexgen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;

/**
 * <p>A utility to generate an index from a set of HTML files and directories.
 * Index entries are indicated inline by HTML of the form
 * <pre>
 *    &lt;a name=XXX&gt;&lt;!-- index: abc; pqr --&gt;&lt;/a&gt;
 * </pre>
 * This will create entries in the index under "abc" and "pqr" that link back to "XXX".
 * The entries can have sub-entries, separated by ":", as in:
 * <pre>
 *    &lt;a name=XXX&gt;&lt;!-- index: abc:def; pqr:stu --&gt;&lt;/a&gt;
 * This will create entries in the index under "abc"/"def" and "pqr"/"stu"
 * that link back to "XXX". The format is the same as for FrameMaker index marker
 * entries.
 *
 * <p>The output can be in one or both of two forms.
 *
 * <p>The index can be output as a JavaHelp-compatible index.xml file,
 * with an associated map file. The map file can be merged with any other
 * maps with the "mapmerge" utility.
 *
 * <p>Or, the index can be output as a single index.html file, containing the
 * sorted set of index entries and their references.
 */
public class Main {
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
        out.println("-mapOut map.xml");
        out.println("        Specify the location of the map.xml file to be written.");
        out.println("-htmlOut index.html");
        out.println("        Specify the location of the index.html file.");
        out.println("-xmlOut index.xml");
        out.println("        Specify the location of the index.xml file.");
        out.println("-srcpath dir;dir;...");
        out.println("        Specify the a path in which to look for source files.");
        out.println("files...");
        out.println("        HTML files and directories.");
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
            if (args[i].equalsIgnoreCase("-htmlout") && i + 1 < args.length) {
                htmlOutFile = new File(args[++i]);
            }
            else if (args[i].equalsIgnoreCase("-xmlout") && i + 1 < args.length) {
                xmlOutFile = new File(args[++i]);
            }
            else if (args[i].equalsIgnoreCase("-mapout") && i + 1 < args.length) {
                mapOutFile = new File(args[++i]);
            }
            else if (args[i].equalsIgnoreCase("-mapdir") && i + 1 < args.length) {
                mapDir = new File(args[++i]);
            }
            else if (args[i].equalsIgnoreCase("-srcPath") && i + 1 < args.length) {
                path = splitPath(args[++i]);
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

        public void setHtmlOutFile(File file) {
            m.htmlOutFile = file;
        }

        public void setXmlOutFile(File file) {
            m.xmlOutFile = file;
        }

        public void setMapOutFile(File file) {
            m.mapOutFile = file;
        }

        public void setMapDir(File file) {
            m.mapDir = file;
        }

        public void setDir(File dir) {
            getImplicitFileSet().setDir(dir);
        }

        public void execute() {
            FileScanner s = getImplicitFileSet().getDirectoryScanner(getProject());
            m.path = new File[] { s.getBasedir() };
            m.addFiles(s.getIncludedFiles());

            try {
                m.run();
            } catch (BadArgs e) {
                throw new BuildException(e.getMessage());
            } catch (IOException e) {
                throw new BuildException(e);
            }
        }
    }

    public void addFiles(String[] paths) {
        if (paths == null)
            return;
        List/*<File>*/ files = new ArrayList/*<File>*/();
        if (inFiles != null)
            files.addAll(Arrays.asList(inFiles));
        for (int i = 0; i < paths.length; i++)
            files.add(new File(paths[i]));
        inFiles = (File[]) files.toArray(new File[files.size()]);
    }

    private void run() throws BadArgs, IOException {
        if (inFiles == null || inFiles.length == 0)
            throw new BadArgs("no input files specified");

        if (htmlOutFile == null && mapOutFile == null && xmlOutFile == null)
            throw new BadArgs("no output files specified");

        if (xmlOutFile != null && mapOutFile == null )
            throw new BadArgs("no map output file specified");

        if (mapOutFile != null && xmlOutFile == null)
            throw new BadArgs("no XML output file specified");

        root = new Node();

        read(inFiles);

        writeIndex();
    }

    private void read(File[] files) throws IOException {
        for (int i = 0; i < files.length; i++)
            read(files[i]);
    }

    private void read(File file) throws IOException {
        if (path == null)
            read(file, file);
        else {
            for (int i = 0; i < path.length; i++) {
                File f = new File(path[i], file.getPath());
                if (f.exists()) {
                    read(f, file);
                    return;
                }
            }
            throw new FileNotFoundException(file.getPath());
        }
    }

    private void read(File absFile, File relFile) throws IOException {

        if (absFile.isDirectory()) {
            if (!absFile.getName().equals("SCCS")) {
                String[] files = absFile.list();
                for (int i = 0; i < files.length; i++) {
                    read(new File(absFile, files[i]),
                         (relFile.getPath().equals(".") ? new File(files[i]) : new File(relFile, files[i])));
                }
            }
            return;
        }

        if (!absFile.getName().endsWith(".html"))
            return;

        // ordinary file -- scan it looking for index entries
        in = new BufferedReader(new FileReader(absFile));
        currFile = relFile;
        currName = null;
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
                            scanComment();
                        }
                    }
                    break;

                case '/':
                    nextCh();
                    String endTag = scanIdentifier();
                    if (isLink(endTag)) {
                        currName = "";
                        skipTag();
                    }
                    else
                        skipTag();
                    break;

                default:
                    String startTag = scanIdentifier();
                    if (isLink(startTag))
                        scanLink();
                    else
                        skipTag();
                }
            }
            else
                nextCh();
        }

    }

    private boolean isLink(String tag) {
        return tag.equals("a");
    }

    /**
     * Process the contents of <a href=...>
     */
    private void scanLink() throws IOException {
        skipSpace();
        while (c != '>') {
            String att = scanIdentifier();
            String value = scanValue();
            if (att.equalsIgnoreCase("name"))
                currName = value;
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
     * Scan an HTML comment  <!-- ... -->
     */
    private void scanComment() throws IOException {
        // a comment sequence is "<!--" ... "-->"
        // at the time this is called, "<!--" has been read;
        StringBuffer text = new StringBuffer("<!--");
        int numHyphens = 0;
        while (c != -1 && (numHyphens < 2 || c != '>')) {
            if (c == '-')
                numHyphens++;
            else
                numHyphens = 0;
            text.append((char) c);
            nextCh();
            //System.out.print((char)c);
        }
        text.append((char) c);
        nextCh();

        String comment = text.toString().substring(4, text.length() - 3).trim();

        if (comment.startsWith("index:")) {
            String[] entries = split(comment.substring(6).trim(), ';');
            for (int i = 0; i < entries.length; i++)
                addToIndex(split(entries[i].trim(), ':'), currFile, currName);
        }
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

    private static String escape(String s) {
        for (int i = 0; i < s.length(); i++) {
            switch (s.charAt(i)) {
            case '<': case '>': case '&':
                StringBuffer sb = new StringBuffer(s.length()*2);
                for (int j = 0; j < s.length(); j++) {
                    char c = s.charAt(j);
                    switch (c) {
                    case '<': sb.append("&lt;"); break;
                    case '>': sb.append("&gt;"); break;
                    case '&': sb.append("&amp;"); break;
                    default: sb.append(c);
                    }
                }
                return sb.toString();
            }
        }
        return s;
    }

    private void addToIndex(String[] path, File file, String ref) {
        Node node = root.getChild(path);
        String href = file.getPath();
        if (ref != null && ref.length() > 0)
            href = href + "#" + ref;
        node.setInfo(href);
    }

    private void writeIndex() throws IOException {

        PrintWriter indexOut = (xmlOutFile == null ? null
                                   : new PrintWriter(new BufferedWriter(new FileWriter(xmlOutFile))));
        if (indexOut != null) {
            indexOut.println("<?xml version='1.0' encoding='ISO-8859-1'  ?>");
            indexOut.println("<!DOCTYPE index");
            indexOut.println("  PUBLIC \"-//Sun Microsystems Inc.//DTD JavaHelp Index Version 1.0//EN\"");
            indexOut.println("         \"http://java.sun.com/products/javahelp/index_1_0.dtd\">");
            indexOut.println("");
            indexOut.println("<index version=\"1.0\">");
        }

        PrintWriter mapOut = (mapOutFile == null ? null
                              : new PrintWriter(new BufferedWriter(new FileWriter(mapOutFile))));
        if (mapOut != null) {
            mapOut.println("<?xml version='1.0' encoding='ISO-8859-1' ?>");
            mapOut.println("<!DOCTYPE map");
            mapOut.println("  PUBLIC \"-//Sun Microsystems Inc.//DTD JavaHelp Map Version 1.0//EN\"");
            mapOut.println("         \"http://java.sun.com/products/javahelp/map_1_0.dtd\">");
            mapOut.println("<map version=\"1.0\">");
        }

        PrintWriter htmlOut = (htmlOutFile == null ? null
                               : new PrintWriter(new BufferedWriter(new FileWriter(htmlOutFile))));
        if (htmlOut != null) {
            htmlOut.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">");
            htmlOut.println("<html>");
            htmlOut.println("<head>");
            htmlOut.println("<title>");
            htmlOut.println("Index");
            htmlOut.println("</title>");
            htmlOut.println("<LINK REL=\"stylesheet\" TYPE=\"text/css\" HREF=\"../jthelp.css\" TITLE=\"Style\">");
            htmlOut.println("</head>");
            htmlOut.println("<body>");
            htmlOut.println("<h1>Index</h1>");
        }

        char currLetter = 0;

        for (Iterator iter = root.iterator(); iter.hasNext(); ) {
            Node node = (Node) (iter.next());
            String name = node.getName();
            char initial = Character.toUpperCase(name.charAt(0));
            if (Character.isLetter(initial) && initial != currLetter) {
                for (char c = (currLetter == 0 ? 'A' : (char) (currLetter + 1));
                     c <= initial; c++) {
                    if (htmlOut != null) {
                        htmlOut.println("");
                        htmlOut.println("<p class=\"index0\">" + c + "</p>");
                    }
                }
                currLetter = initial;
            }

            write(indexOut, mapOut, htmlOut, node, 0);
        }

        if (htmlOut != null) {
            for (char c = (char) (currLetter + 1); c <= 'Z'; c++)
                htmlOut.println("<p class=\"index0\">" + c + "</p>");
            htmlOut.println("</body>");
            htmlOut.println("</html>");
            htmlOut.close();
        }

        if (mapOut != null) {
            mapOut.println("</map>");
            mapOut.close();
        }

        if (indexOut != null) {
            indexOut.println("</index>");
            indexOut.close();
        }

    }

    private void write(PrintWriter xmlOut, PrintWriter mapOut, PrintWriter htmlOut, Node node, int depth) {
        String href = node.getInfo();

        if (htmlOut != null) {
            htmlOut.write("<p class=\"index");
            htmlOut.write(String.valueOf(depth + 1));
            htmlOut.write("\">");
            if (href != null) {
                htmlOut.write("<a href=\"");
                htmlOut.write(escapeString(href));
                htmlOut.write("\">");
            }
            htmlOut.write(node.getName());
            if (href != null)
                htmlOut.write("</a>");
            htmlOut.write("</p>\n");
        }

        if (xmlOut != null) {
            xmlOut.write("<indexitem text=\"");
            xmlOut.write(escapeString(node.getName()));
            xmlOut.write("\" ");
            if (href != null) {
                xmlOut.write(" target=\"");
                xmlOut.write(escapeString(getTarget(href)));
                xmlOut.write("\" ");
                if (mapOut != null) {
                    mapOut.println("<mapID target=\""
                                   + escapeString(getTarget(href))
                                   + "\" url=\""
                                   + escapeString(href)
                                   + "\" />");
                }
            }
            xmlOut.println(node.getChildCount() == 0 ? "/>" : ">");
        }

        if (node.getChildCount() > 0) {
            for (Iterator iter = node.iterator(); iter.hasNext(); ) {
                Node child = (Node) (iter.next());
                write(xmlOut, mapOut, htmlOut, child, depth + 1);
            }

            if (xmlOut != null)
                xmlOut.println("</indexitem>");
        }
    }

    private static String[] split(String s, char sep) {
        Vector v = new Vector();
        int start = -1;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == sep)  {
                if (start != -1)
                    v.addElement(s.substring(start, i).trim());
                start = -1;
            } else {
                if (start == -1)
                    start = i;
            }
        }
        if (start != -1)
            v.addElement(s.substring(start).trim());
        String[] a = new String[v.size()];
        v.copyInto(a);
        return a;
    }

    private static File[] splitPath(String s) {
        Vector v = new Vector();
        int start = -1;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == File.pathSeparatorChar)  {
                if (start != -1)
                    v.addElement(new File(s.substring(start, i)));
                start = -1;
            } else {
                if (start == -1)
                    start = i;
            }
        }
        if (start != -1)
            v.addElement(new File(s.substring(start)));
        File[] a = new File[v.size()];
        v.copyInto(a);
        return a;
    }

    private static String getTarget(String key) {
        String file;
        String ref;

        int hash = key.lastIndexOf("#");
        if (hash == -1) {
            file = key;
            ref = null;
        }
        else {
            file = key.substring(0, hash);
            ref = key.substring(hash + 1);
        }

        if (file.endsWith(".html"))
            file = file.substring(0, file.length() - 5);

        if (ref == null)
            key = file;
        else
            key = file + "#" + ref;

        StringBuffer sb = new StringBuffer();
        sb.append("index.");
        boolean needUpper = false;
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (Character.isLetter(c)) {
                sb.append(needUpper ? Character.toUpperCase(c) : c);
                needUpper = false;
            }
            else
                needUpper = true;
        }
        return sb.toString();
    }

    private static String escapeString(String text) {

        // check to see if there are any special characters
        boolean specialChars = false;
        for (int i = 0; i < text.length() && !specialChars; i++) {
            switch (text.charAt(i)) {
            case '<': case '>': case '&': case '"':
                specialChars = true;
            }
        }

        // if there are special characters rewrite the string with escaped characters
        // otherwise, return it as is
        if (specialChars) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                switch (c) {
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '&': sb.append("&amp;"); break;
                case '"': sb.append("&quot;"); break;
                default: sb.append(c);
                }
            }
            return sb.toString();
        }
        else
            return text;
    }

    private File[] path;
    private File[] inFiles;
    private File htmlOutFile;
    private File mapOutFile;
    private File mapDir;
    private File xmlOutFile;

    private Reader in;
    private int c;
    private int line;

    private File currFile;
    private String currName;
    private Node root;

    private static Iterator nullIterator = new Iterator() {
            public boolean hasNext() {
                return false;
            }
            public Object next() {
                return null;
            }
            public void remove() {
            }
        };

    private Comparator indexComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                Node n1 = (Node) o1;
                Node n2 = (Node) o2;
                return n1.getName().compareToIgnoreCase(n2.getName());
            }

            public boolean equals(Object o) {
                return false;
            }
        };

    private class Node
    {
        Node() { }

        Node(Node parent, String name) {
            this.name = name;
            parent.add(this);
        }

        String getName() {
            return name;
        }

        void setInfo(String info) {
            this.info = info;
        }

        String getInfo() {
            return info;
        }

        Node getChild(String name) {
            if (children != null) {
                for (Iterator iter = children.iterator(); iter.hasNext(); ) {
                    Node child = (Node) (iter.next());
                    if (child.name.equals(name))
                        return child;
                }
            }

            return new Node(this, name);
        }

        Node getChild(String[] path) {
            Node c = this;
            for (int index = 0; index < path.length; index++)
                c = c.getChild(path[index]);
            return c;
        }

        int getChildCount() {
            return (children == null ? 0 : children.size());
        }

        Iterator iterator() {
            return (children == null ? nullIterator : children.iterator());
        }

        private void add(Node child) {
            if (children == null)
                children = new TreeSet(indexComparator);
            children.add(child);
        }

        private String name;
        private Set children;
        private String info;
    }

}
