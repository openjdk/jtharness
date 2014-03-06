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
package com.sun.jct.utils.glossarygen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;

/**
 * A utility to generate a glossary from a set of HTML files and directories.
 * The glossary terms are taken from the individual &lt;h1&gt; tags in the various files;
 * The glossary definitions are the body of those files.
 * Thus, to add a new entry into the glossary, it is simply necessary to add a
 * new file into the set of files passed to this program.
 *
 * <p>The output can be in one or both of two forms.
 *
 * <p>The glossary can be output as a JavaHelp-compatible glossary.xml file, with an associated
 * map file. The map file can be merged with any other maps with the "mapmerge"
 * utility.
 *
 * <p>Or, the glossary can be output as a single glossary.html file, containing the
 * sorted set of terms and their definitions. In the form, any &lt;h*&gt; tags in the
 * body are replaced with &lt;p class="glossaryHead*"&gt;.
 *
 * <p>The input files can have keywords associated with them, which can be used to
 * filter the files selected for the glossary. These keywords can be provided in
 * META tag, as follows:<br>
 * &lt;META name="glossaryKeywords" content="<i>space-separated list of keywords</i>"&gt;
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
        out.println("-htmlOut glossary.html");
        out.println("        Specify the location of the glossary.html file.");
        out.println("-xmlOut glossary.xml");
        out.println("        Specify the location of the glossary.xml file.");
        out.println("-key keyword");
        out.println("        Specify a keyword to filter HTML files.");
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
            else if (args[i].equalsIgnoreCase("-key") && i + 1 < args.length) {
                keyword = args[++i];
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

        public void setKeyword(String key) {
            m.keyword = key;
        }

        public void setDir(File dir) {
            getImplicitFileSet().setDir(dir);
        }

        public void execute() {
            FileScanner s = getImplicitFileSet().getDirectoryScanner(getProject());
            m.addFiles(s.getBasedir(), s.getIncludedFiles());

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

    private void run() throws BadArgs, IOException {
        if (inFiles == null || inFiles.length == 0)
            throw new BadArgs("no input files specified");

        if (htmlOutFile == null && mapOutFile == null && xmlOutFile == null)
            throw new BadArgs("no output files specified");

        if (xmlOutFile != null && mapOutFile == null )
            throw new BadArgs("no map output file specified");

        if (mapOutFile != null && xmlOutFile == null)
            throw new BadArgs("no XML output file specified");

        if (mapOutFile != null && mapDir == null)
            mapDir = mapOutFile.getParentFile();

        glossary = new TreeMap();

        read(inFiles);

        PrintWriter glossaryOut = (xmlOutFile == null ? null
                                   : new PrintWriter(new BufferedWriter(new FileWriter(xmlOutFile))));
        if (glossaryOut != null) {
            glossaryOut.println("<?xml version='1.0' encoding='ISO-8859-1'  ?>");
            glossaryOut.println("<!DOCTYPE index");
            glossaryOut.println("  PUBLIC \"-//Sun Microsystems Inc.//DTD JavaHelp Index Version 1.0//EN\"");
            glossaryOut.println("         \"http://java.sun.com/products/javahelp/index_1_0.dtd\">");
            glossaryOut.println("");
            glossaryOut.println("<index version=\"1.0\">");
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
            htmlOut.println("Glossary");
            htmlOut.println("</title>");
            htmlOut.println("<LINK REL=\"stylesheet\" TYPE=\"text/css\" HREF=\"../jthelp.css\" TITLE=\"Style\">");
            htmlOut.println("</head>");
            htmlOut.println("<body>");
            htmlOut.println("<h1 class=\"glossary\">Glossary</h1>");
        }

        char currLetter = 0;

        for (Iterator iter = glossary.values().iterator(); iter.hasNext(); ) {
            Entry e = (Entry) (iter.next());
            if (!e.matches(keyword))
                continue;

            String key = e.getKey();
            char initial = key.charAt(0);
            if (Character.isLetter(initial) && initial != currLetter) {
                for (char c = (currLetter == 0 ? 'A' : (char) (currLetter + 1));
                     c <= initial; c++) {
                    if (glossaryOut != null) {
                        glossaryOut.println("");
                        glossaryOut.println("<!-- " + c + " -->");
                    }
                    if (htmlOut != null) {
                        htmlOut.println("");
                        htmlOut.println("<p class=\"glossaryHead2\">" + c + "</p>");
                    }
                }
                currLetter = initial;
            }

            if (glossaryOut != null)
                glossaryOut.println("<indexitem text=\"" + key + "\" target=\"" + getTarget(key) + "\"/>");

            if (mapOut != null)
                mapOut.println("<mapID target=\"" + getTarget(key) + "\" url=\"" + getRelativeFile(mapDir, e.getFile()) + "\" />");

            if (htmlOut != null)
                htmlOut.println(e.getText());
        }

        for (char c = (currLetter == 0 ? 'A' : (char) (currLetter + 1)); c <= 'Z'; c++) {
            if (htmlOut != null)
                htmlOut.println("<p class=\"glossaryHead2\">" + c + "</p>");
        }

        if (htmlOut != null) {
            htmlOut.println("</body>");
            htmlOut.println("</html>");
            htmlOut.close();
        }

        if (mapOut != null) {
            mapOut.println("</map>");
            mapOut.close();
        }

        if (glossaryOut != null) {
            glossaryOut.println("</index>");
            glossaryOut.close();
        }
    }

    private void read(File[] files) throws IOException {
        for (int i = 0; i < files.length; i++)
            read(files[i]);
    }

    private void read(File file) throws IOException {
        if (file.isDirectory()) {
            if (!file.getName().equals("SCCS"))
                read(file.listFiles());
        }
        else {
            if (file.getName().endsWith(".html")) {
                Entry e = new Entry(file);
                glossary.put(e.getKey().toUpperCase(), e);
            }
        }
    }

    private File getRelativeFile(File dir, File file) {
        String dp = dir.getPath() + "/";
        String fp = file.getPath();
        return (fp.startsWith(dp) ? new File(fp.substring(dp.length())) : file);
    }

    private static String getTarget(String key) {
        StringBuffer sb = new StringBuffer();
        sb.append("glossary.");
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

    private File[] inFiles;
    private File htmlOutFile;
    private File mapOutFile;
    private File mapDir;
    private File xmlOutFile;
    private String keyword;
    private Map glossary;

}


class Entry {
    Entry(File f) throws IOException {
        file = f;
        Reader in = new BufferedReader(new FileReader(f));
        Writer out = new StringWriter();
        copy(in, out);
        text = out.toString().trim();
        head1 = head1.trim();

        // System.err.println("<<< " + head1 + " >>>");
        // System.err.println(text);
        // System.err.println();
        // System.err.println();
    }

    File getFile() {
        return file;
    }

    String getKey() {
        return head1;
    }

    String getText() {
        return text;
    }

    boolean matches(String keyword) {
        if (keyword == null)
            return true;

        if (keywords == null || keywords.size() == 0)
            return true;

        return keywords.contains(keyword);
    }

    /**
     * Copy the input stream to the output, looking for tags that need
     * to be rewritten.
     */
    private void copy(Reader in, Writer out) throws IOException {
        this.in = in;
        this.out = out;
        head1 = "";
        hIndent = 2;
        copyMode = NO_COPY;
        line = 1;
        nextCh();
        while (c >= 0) {
            if (c == '<') {
                if (copyMode == COPY) {
                    copyMode = PENDING_COPY;
                    pendingCopy.setLength(0);
                }

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
                    String endTag = scanIdentifier();
                    if (copyMode != PENDING_COPY)
                        skipTag();
                    else if (isBody(endTag))
                        scanBody(false);
                    else if (isHead(endTag))
                        scanHead(Character.getNumericValue(endTag.charAt(1)), false);
                    else
                        skipTag();
                    break;

                default:
                    String startTag = scanIdentifier();
                    if (isBody(startTag))
                        scanBody(true);
                    else if (isMeta(startTag))
                        scanMeta();
                    else if (copyMode != PENDING_COPY)
                        skipTag();
                    //else if (isArea(startTag))
                    //  scanArea();
                    else if (isHead(startTag))
                        scanHead(Character.getNumericValue(startTag.charAt(1)), true);
                    //else if (isImage(startTag))
                    //  scanImage();
                    else if (isLink(startTag))
                        scanLink();
                    //else if (isMap(startTag))
                    //  scanMap();
                    else
                        skipTag();
                }
            }
            else {
                if (inHead1)
                    head1 += ((char) c);
                nextCh();
            }
        }

    }

//    private boolean isArea(String tag) {
//      return tag.equals("area");
//    }
//
//    /**
//     * Process the contents of <area ... href=...>
//     */
//    private void scanArea() throws IOException {
//      out.write(pendingCopy.toString());
//      copyMode = COPY;
//
//      skipSpace();
//      while (c != '>') {
//          String att = scanIdentifier();
//          if (att.equalsIgnoreCase("href") && copyMode == COPY) {
//              // the current character should be a whitespace or =
//              // either way, we just write out =
//              out.write('=');
//              copyMode = NO_COPY;
//              String target = scanValue();
//              URL t = new URL(currURL, target);
//              String link = t.getFile();
//              if (link.startsWith(basePath))
//                  link = link.substring(basePath.length());
//              if (link.endsWith(".html"))
//                  link = link.substring(0, link.length() - 5);
//              String ref = t.getRef();
//              if (ref != null && ref.length() > 0)
//                  link = link + "!" + ref;
//              out.write("\"");
//              out.write('#' + link);
//              out.write("\" ");
//              copyMode = COPY;
//          }
//          else
//              scanValue();
//          skipSpace();
//      }
//      nextCh();
//    }

    private boolean isBody(String tag) {
        return tag.equals("body");
    }

    private void scanBody(boolean start) throws IOException {
        if (start) {
            skipSpace();
            while (c != '>') {
                scanIdentifier();
                scanValue();
                skipSpace();
            }
            nextCh(); // skip past >
            copyMode = COPY;
//          String link = currURL.getFile();
//          if (link.startsWith(basePath))
//              link = link.substring(basePath.length());
            String link = file.getName();
            if (link.endsWith(".html"))
                link = link.substring(0, link.length() - 5);
            out.write("\n<!-- file: " + file + " -->\n<a name=\"" + link + "\">");
        }
        else {
            copyMode = NO_COPY;
        }
    }

    private boolean isHead(String tag) {
        return ( tag.length() == 2
                 && tag.charAt(0) == 'h'
                 && Character.isDigit(tag.charAt(1)) );
    }

    private void scanHead(int level, boolean start) throws IOException {

        if (copyMode == PENDING_COPY) {
            int n = Math.min(hIndent + level, 6);
            out.write('<');
            // for glossary.pdf, we change <hn> to <p> and default class to glossaryHeadn
            out.write(start ? "p" : "/p");

// standard head code
//          if (!start)
//              out.write('/');
//          out.write('h');
//          out.write(String.valueOf(n));

            copyMode = COPY;

            String className = null;
            skipSpace();
            while (c != '>') {
                String name = scanIdentifier();
                String value = scanValue();
                if (name.equalsIgnoreCase("class"))
                    className = value;
                skipSpace();
            }

            if (start && className == null) {
                // write default class
                out.write(" class=\"glossaryHead");
                out.write(String.valueOf(n));
                out.write('"');
            }

            nextCh(); // skip past >

            if (level == 1)
                inHead1 = start;

//          if (start && autoNumberLevel > 0) {
//              hNums[n - 1]++;
//              if (n < 6)
//                  hNums[n] = 0;
//              if (n <= autoNumberLevel) {
//                  for (int i = 0; i < n; i++) {
//                      out.write(String.valueOf(hNums[i]));
//                      out.write('.');
//                  }
//                  out.write("&nbsp;");
//              }
//          }
        }
    }

//    private boolean isImage(String tag) {
//      return tag.equals("img");
//    }
//
//    /**
//     * Process the contents of <a href=...>
//     */
//    private void scanImage() throws IOException {
//      out.write(pendingCopy.toString());
//      copyMode = COPY;
//
//      skipSpace();
//      while (c != '>') {
//          String att = scanIdentifier();
//          if (att.equalsIgnoreCase("src") && copyMode == COPY) {
//              // the current character should be a whitespace or =
//              // either way, we just write out =
//              out.write('=');
//              copyMode = NO_COPY;
//              String src = scanValue();
//              URL u = new URL(currURL, src);
//              String srcPath = u.getFile();
//              // if the path refers to an entry in the /images directory,
//              // check for a matching entry in the /pdfImages directory
//              // and use that if found.
//              int imagesIndex = srcPath.indexOf("/images/");
//              if (imagesIndex >= 0) {
//                  String pdfImagePath = srcPath.substring(0, imagesIndex)
//                      + "/pdfImages/"
//                      + srcPath.substring(imagesIndex + "/images/".length());
//                  if (new File(pdfImagePath).exists())
//                      srcPath = pdfImagePath;
//              }
//              out.write('"');
//              out.write(srcPath);
//              out.write('"');
//              copyMode = COPY;
//          }
//          else if (att.equalsIgnoreCase("usemap") && copyMode == COPY) {
//              // the current character should be a whitespace or =
//              // either way, we just write out =
//              out.write('=');
//              copyMode = NO_COPY;
//              String target = scanValue();
//              URL t = new URL(currURL, target);
//              String link = t.getFile();
//              if (link.startsWith(basePath))
//                  link = link.substring(basePath.length());
//              if (link.endsWith(".html"))
//                  link = link.substring(0, link.length() - 5);
//              String ref = t.getRef();
//              if (ref != null && ref.length() > 0)
//                  link = link + "!" + ref;
//              out.write("\"");
//              out.write('#' + link);
//              out.write("\" ");
//              copyMode = COPY;
//          }
//          else
//              scanValue();
//          skipSpace();
//      }
//      nextCh();
//    }

    private boolean isLink(String tag) {
        return tag.equals("a");
    }

    /**
     * Process the contents of <a href=...>
     */
    private void scanLink() throws IOException {
        out.write(pendingCopy.toString());
        copyMode = COPY;

        skipSpace();
        while (c != '>') {
            String att = scanIdentifier();
            if (att.equalsIgnoreCase("href") && copyMode == COPY) {
                // the current character should be a whitespace or =
                // either way, we just write out =
                out.write('=');
                copyMode = NO_COPY;
                String target = scanValue();
                URL t = new URL(file.toURL(), target);
//              String link = t.getFile();
                String link = target;
//              if (link.startsWith(basePath))
//                  link = link.substring(basePath.length());
                if (link.endsWith(".html"))
                    link = link.substring(0, link.length() - 5);
                String ref = t.getRef();
                if (ref != null && ref.length() > 0)
                    link = link + "!" + ref;
                out.write('"');
                out.write('#' + link);
                out.write('"');
                copyMode = COPY;
            }
            else if (att.equalsIgnoreCase("name") && copyMode == COPY) {
                // the current character should be a whitespace or =
                // either way, we just write out =
                out.write('=');
                copyMode = NO_COPY;
                String oldName = scanValue();
//              String name = currURL.getFile();
                String name = file.getPath();
//              if (name.startsWith(basePath))
//                  name = name.substring(basePath.length());
                if (name.endsWith(".html"))
                    name = name.substring(0, name.length() - 5);
                name = name + "!" + oldName;
                out.write('"');
                out.write(name);
                out.write('"');
                copyMode = COPY;
            }
            else
                scanValue();
            skipSpace();
        }
        nextCh();
    }

//    private boolean isMap(String tag) {
//      return tag.equals("map");
//    }
//
//    /**
//     * Process the contents of <map name=...>
//     */
//    private void scanMap() throws IOException {
//      out.write(pendingCopy.toString());
//      copyMode = COPY;
//
//      skipSpace();
//      while (c != '>') {
//          String att = scanIdentifier();
//          if (att.equalsIgnoreCase("name") && copyMode == COPY) {
//              // the current character should be a whitespace or =
//              // either way, we just write out =
//              out.write('=');
//              copyMode = NO_COPY;
//              String oldName = scanValue();
//              String name = currURL.getFile();
//              if (name.startsWith(basePath))
//                  name = name.substring(basePath.length());
//              if (name.endsWith(".html"))
//                  name = name.substring(0, name.length() - 5);
//              name = name + "!" + oldName;
//              out.write('"');
//              out.write(name);
//              out.write('"');
//              copyMode = COPY;
//          }
//          else
//              scanValue();
//          skipSpace();
//      }
//      nextCh();
//    }

    private boolean isMeta(String tag) {
        return tag.equals("meta");
    }

    private void scanMeta() throws IOException {
        String name = "";
        String content = "";

        skipSpace();
        while (c != '>') {
            String attr_name = scanIdentifier();
            String attr_val = scanValue();
            if (attr_name.equalsIgnoreCase("name"))
                name = attr_val;
            else if (attr_name.equalsIgnoreCase("content"))
                content = attr_val;
            skipSpace();
        }
        nextCh();

//      if (name.equalsIgnoreCase("hIndent")) {
//          hIndent = Integer.parseInt(content);
//      }
        if (name.equalsIgnoreCase("glossaryKeywords")) {
            keywords = new HashSet(Arrays.asList(split(content)));
        }
    }

    private boolean isTitle(String tag) {
        return tag.equals("title");
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
                    throw new IOException("Identifier expected (" + file + ":" + line + ")");
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
                throw new IOException("mismatched quotes (" + file + ":" + line + ")");
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

        String comment = text.toString();

        switch (copyMode) {

        case PENDING_COPY:
            if (comment.equalsIgnoreCase("<!--CopyOff-->")) {
                copyMode = SUPPRESS_COPY;
                pendingCopy.setLength(0);
            }
            else {
                out.write(comment);
                copyMode = COPY;
            }
            break;

        case SUPPRESS_COPY:
            if (comment.equalsIgnoreCase("<!--CopyOn-->"))
                copyMode = COPY;
            break;
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
                throw new IOException("error parsing HTML input (" + file + ":" + line + ")");
            String value = scanValue();
            skipSpace();
        }
        nextCh();

        if (copyMode == PENDING_COPY) {
            out.write(pendingCopy.toString());
            copyMode = COPY;
        }
    }

    /**
     * Read the next character.
     */
    private void nextCh() throws IOException {
        switch (copyMode) {
        case COPY:
            out.write((char) c);
            break;

        case PENDING_COPY:
            pendingCopy.append((char) c);
            break;
        }

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

    private static String[] split(String s) {
        Vector v = new Vector();
        int start = -1;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_') {
                if (start == -1)
                    start = i;
            }
            else {
                if (start != -1)
                    v.addElement(s.substring(start, i));
                start = -1;
            }
        }
        if (start != -1)
            v.addElement(s.substring(start));
        String[] a = new String[v.size()];
        v.copyInto(a);
        return a;
    }

    private File file;
    private String head1;
    private String text;
    private Set keywords;

    private Reader in;
    private Writer out;
    private int c;
    private boolean inHead1;
    private int line;
    private int copyMode;
    private static final int NO_COPY = 0, PENDING_COPY = 1, SUPPRESS_COPY = 2, COPY = 3;
    private StringBuffer pendingCopy = new StringBuffer();
    private int hIndent;
    private int[] hNums = new int[6];
}
