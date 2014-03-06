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
package com.sun.javatest.finder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.Hashtable;
import java.util.Vector;

import com.sun.javatest.TestFinder;
import com.sun.javatest.util.I18NResourceBundle;

/**
 * This class searches out test descriptions as represented by
 * certain HTML elements in a hierarchy of HTML files.
 *
 * @see TestFinder
 */
public class HTMLTestFinder extends TestFinder
{
    /**
     * Create an HTMLTestFinder.
     */
    public HTMLTestFinder() {
        // init the tables used when scanning directories
        // if necessary, the tables could be dynamically updated by the
        // init args; this is not currently supported
        excludeList = new Hashtable(excludeNames.length);
        for (int i = 0; i < excludeNames.length; i++)
            excludeList.put(excludeNames[i], excludeNames[i]);

        extensionTable = new Hashtable(extensions.length);
        for (int i = 0; i < extensions.length; i++)
            extensionTable.put(extensions[i], extensions[i]);
    }

    protected int decodeArg(String[] args, int i) throws Fault {
        if (args[i].equalsIgnoreCase("-webWalk")) {
            mode = WEB_WALK;
            return 1;
        }
        else if (args[i].equalsIgnoreCase("-dirWalk")) {
            mode = DIR_WALK;
            return 1;
        }
        else if (args[i].equalsIgnoreCase("-IGNORE-ERRORS")) {
            ignoreErrors = true;
            return 1;
        }
        else
            return super.decodeArg(args, i);
    }

    public File getRoot() {
        // we assume that providedRoot exists - see Parameters class
        File providedRoot = super.getRoot();

        if (mode == DIR_WALK && providedRoot.isFile()) {
            validatedRoot = new File(providedRoot.getParent());
        }
        else if (mode == WEB_WALK && providedRoot.isDirectory()) {
            throw new IllegalStateException (i18n.getString("html.badRootForWebWalk", providedRoot.getPath()));
        }
        else {
            validatedRoot = new File(providedRoot.getPath());
        }

        return validatedRoot;
    }

    protected void localizedError(String msg) {
        if (!ignoreErrors)
            super.localizedError(msg);
    }

    /**
     * Specify the mode for how this test finder determines the child files
     * to be scanned.  The default is <tt>DIR_WALK</tt>.
     * @param mode One of {@link #WEB_WALK} or {@link #DIR_WALK}
     * @see #getMode
     */
    public void setMode(int mode) {
        switch (mode) {
        case DIR_WALK:
        case WEB_WALK:
            this.mode = mode;
            break;

        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Get the current mode for how this test finder determines the child files
     * to be scanned.
     * @return One of {@link #WEB_WALK} or {@link #DIR_WALK}
     * @see #setMode
     */
    public int getMode() {
        return mode;
    }

    //-----internal routines----------------------------------------------------


    protected void scan(File file) {
        if (file.isDirectory())
            scanDirectory(file);
        else
            scanFile(file);
    }

    private void scanDirectory(File dir) {
        if (mode == WEB_WALK)
            return;

        // scan the contents of the directory, checking for
        // subdirectories and other files that should be scanned
        String[] names = dir.list();
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            // if the file should be ignored, skip it
            // This is typically for directories like SCCS etc
            if (excludeList.containsKey(name))
                continue;

            File file = new File(dir, name);
            if (file.isDirectory()) {
                // if its a directory, add it to the list to be scanned
                foundFile(file);
            }
            else {
                // if its a file, check its extension
                int dot = name.lastIndexOf('.');
                if (dot == -1)
                    continue;
                String extn = name.substring(dot);
                if (extensionTable.containsKey(extn)) {
                    // extension has a comment reader, so add it to the
                    // list to be scanned
                    foundFile(file);
                }
            }
        }
    }

    private void scanFile(File file) {
        String tag;

        input = null;
        lastName = null;

        // We want to check that the names defined in the file are unique.
        // Maintain the following tables:
        namesInFile.clear();

        try {
            input = new BufferedReader(new FileReader(file));
            currFile = file;
            line = 1;
            nextCh();
            while (c >= 0) {
                switch (c) {
                case '<' :
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
                        tag = scanIdentifier();
                        if (tag.equals("dl"))
                            endDefList();
                        else if (tag.equals("td") || tag.equals("th"))
                            endTableData();
                        else if (tag.equals("tr") || tag.equals("table"))
                            endTableRow();
                        skipTag();

                        if (inTestDescription() && tag.equals(endTestDescriptionTag)) {
                            foundTestDescription(params, file, line);
                            params = null;
                        }
                        break;

                      default:
                        tag = scanIdentifier();
                        if (tag.equals("a"))
                            scanLink(file);
                        else if (tag.equals("table"))
                            scanTable(file);
                        else if (tag.equals("tr"))
                            scanTableRow();
                        else if (tag.equals("td") || tag.equals("th"))
                            scanTableData();
                        else if (tag.equals("dl"))
                            scanDefList(file);
                        else if (tag.equals("dt"))
                            scanDefTerm();
                        else if (tag.equals("dd"))
                            scanDefData();
                        else
                            skipTag();
                    }
                    break;

                case ' ':
                case '\f':
                case '\t':
                case '\r':
                case '\n':
                    if (text != null && text.length() > 0 && text.charAt(text.length()-1) != ' ')
                        text.append(' ');
                    nextCh();
                    break;

                case '&':
                    String replace = null;
                    nextCh();
                    if (c == '#') {
                        int n = 0;
                        nextCh();
                        tag = "#";
                        while ('0' <= c && c <= '9') {
                            tag += ((char) c);
                            n = (n * 10) + (c - '0');
                            nextCh();
                        }
                        replace = "" + ((char)n);
                    }
                    else {
                        tag = scanIdentifier();
                        if (tag.equals("lt"))
                            replace = "<";
                        else if (tag.equals("gt"))
                            replace = ">";
                        else if (tag.equals("amp"))
                            replace = "&";
                        else if (tag.equals("copy"))
                            replace = "©";
                        if (replace == null)
                            replace = "&" + tag + ((char) c);
                    }
                    if (c != ';') {
                        if (!Character.isWhitespace((char) c))
                            tag += ((char) c);
                        error(i18n, "html.badEscape", new Object[] {tag, file});
                    }
                    if (text != null)
                        text.append(replace);
                    nextCh();
                    break;

                default:
                    if (text != null)
                        text.append((char)c);
                    nextCh();
                }
            }
        }
        catch (FileNotFoundException ex) {
            error(i18n, "html.cantFindFile", file);
        }
        catch (IOException ex) {
            error(i18n, "html.ioError", new Object[] {file, new Integer(line), ex});
        }
        finally {
            if (input != null) {
              try {
                  input.close();
              }
              catch (IOException e) {
              }
              input = null;
            }
        }
    }

    /**
     * Get the name of the file currently being read.
     * @return the name of the file currently being read.
     */
    protected File getCurrentFile() {
        return currFile;
    }

    private void nextCh() throws IOException {
        c = input.read();
        if (c == '\n')
            line++;
    }

    private boolean inTestDescription() {
        return (params != null);
    }

    //-----internal routines------------------------------------------------------
    //
    // detect test descriptions in tables

    private void scanTable(File context) throws IOException {
        if (debug)
            System.err.println("scanning table starting in line " + line);

        String id = lastName; // default
        skipSpace();
        while (c != '>') {
            String att = scanIdentifier();
            String value = scanValue();
            skipSpace();
            if (att.equals("class") && "TestDescription".equals(value)) {
                params = new Hashtable();
                endTestDescriptionTag = "table";
            } else if (att.equals("id"))
                id = value;
        }
        nextCh();
        if (params != null && id != null)
            processEntry(params, "id", id);
    }

    private void scanTableRow() throws IOException {
        skipTag();
        if (params != null) {
            endTableRow();
            tableRow = new Vector();
        }
    }

    private void endTableRow() throws IOException {
        if (params != null && tableRow != null) {
            // ensure any outstanding <td> is closed
            endTableData();
            if (tableRow.size() == 2)
                processEntry(params, (String)tableRow.elementAt(0), (String)tableRow.elementAt(1));
            tableRow = null;
        }
    }

    private void scanTableData() throws IOException {
        skipTag();
        if (params != null && tableRow != null) {
            // ensure any outstanding <td> is closed
            endTableData();
            text = new StringBuffer();
        }
    }

    private void endTableData() throws IOException {
        if (params != null && tableRow != null && text != null) {
            while (text.length() > 0 && text.charAt(text.length() - 1) == ' ')
                text.setLength(text.length() -1);
            tableRow.addElement(new String(text));
            text = null;
        }
    }


    //-----internal routines----------------------------------------------------
    //
    // detect test descriptions in definition lists

    private void scanDefList(File context) throws IOException {
        String id = lastName; // default
        skipSpace();
        while (c != '>') {
            String att = scanIdentifier();
            String value = scanValue();
            skipSpace();
            if (att.equals("class") && "TestDescription".equals(value)) {
                params = new Hashtable();
                endTestDescriptionTag = "dl";
            } else if (att.equals("id"))
                id = value;
        }
        nextCh();
        if (params != null && id != null)
            processEntry(params, "id", id);
    }

    private void scanDefTerm() throws IOException {
        skipTag();
        if (params != null) {
            if (defTerm != null && text != null) {
                while (text.length() > 0 && text.charAt(text.length() - 1) == ' ')
                    text.setLength(text.length() -1);
                String defData = new String(text);
                processEntry(params, defTerm, defData);
            }
            defTerm = null;
            text = new StringBuffer();
        }
    }

    private void scanDefData() throws IOException {
        skipTag();
        if (params != null && text != null) {
            while (text.length() > 0 && text.charAt(text.length() - 1) == ' ')
                text.setLength(text.length() -1);
            defTerm = new String(text);
            text = new StringBuffer();
        }
    }

    private void endDefList() throws IOException {
        if (params != null) {
            if (defTerm != null && text != null) {
                while (text.length() > 0 && text.charAt(text.length() - 1) == ' ')
                    text.setLength(text.length() -1);
                String defData = new String(text);
                processEntry(params, defTerm, defData);
            }
            defTerm = null;
            text = null;
        }
    }


    //-----internal routines----------------------------------------------------
    //
    // general lexical support

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
                    throw new IOException("Identifier expected");
                else
                    return buf.toString();
        }
    }

    private void scanLink(File context) throws IOException {
        skipSpace();
        while (c != '>') {
            String att = scanIdentifier();
            String value = scanValue();
            skipSpace();
            if (mode == WEB_WALK &&
                att.equals("href") &&
                value.indexOf(':') == -1 &&     // no protocol
                !value.startsWith("/") &&       // no host or port or absolute files
                !value.startsWith("../")) {     // no backing up the tree
                // remove trailing #ref, if any
                int refStart = value.lastIndexOf('#');
                if (refStart != -1)
                    value = value.substring(0, refStart);

                // strip trailing whitespace
                value = value.trim();

                File file = new File(context.getParent(), value.replace('/', File.separatorChar));
                String f = file.getPath();
                if ((f.endsWith(".html") || f.endsWith(".htm")))
                    foundFile(file);
            }
            if (att.equals("name")) {
                lastName = value;
                Integer here = new Integer(line);
                Integer prev = (Integer)namesInFile.put(value, here);
                if (prev != null) {
                    error(i18n, "html.multipleName",
                          new Object[] {value, context, here, prev});
                }
            }
        }
        nextCh();
        //long exitTime = System.currentTimeMillis();
        //System.out.println(exitTime-enterTime);
    }

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
                error(i18n, "html.quoteMismatch", new Object[] {currFile, new Integer(line)});
                break;
            }
            buf.append((char)c);
            nextCh();
        }
        if (c == quote)
            nextCh();
        skipSpace();
        return buf.toString();
    }

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

    private void skipSpace() throws IOException {
        while ((c == ' ') || (c == '\t') || (c == '\n') || (c == '\r')) {
            nextCh();
        }
    }

    private void skipTag() throws IOException {
        skipSpace();
        while (c != '>') {
            String att = scanIdentifier();
            if (att == "")
                throw new IOException("error parsing HTML input");
            String value = scanValue();
            skipSpace();
        }
        nextCh();
    }

    //----------member variables------------------------------------------------

    private Hashtable namesInFile = new Hashtable();
    private Hashtable excludeList;
    private static final String[] excludeNames = {"SCCS", "deleted_files"};
    private Hashtable extensionTable;
    private static final String[] extensions = {".html", ".htm"};

    /**
     * A value for {@link #setMode} to specify that the child files
     * within the test tree should be determined from the HTML &lt;a href=...&gt; tags.
     */
    public static final int WEB_WALK = 1;

    /**
     * A value for {@link #setMode} to specify that the child files
     * within the test tree should be determined by sub-directories and
     * HTML files within directories.
     */
    public static final int DIR_WALK = 2;

    private int mode = DIR_WALK; // default
    private boolean ignoreErrors = false;

    private File validatedRoot;
    private File currFile;
    private Reader input;
    private int c;
    private int line;
    private StringBuffer text;

    private String endTestDescriptionTag;
    private Hashtable params;
    private String defTerm;   // collects test description parameter name
    private Vector tableRow;  // collects test description info from <TR><TD>....</TD>....etc...</TR>
    private String lastName;

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(HTMLTestFinder.class);
}
