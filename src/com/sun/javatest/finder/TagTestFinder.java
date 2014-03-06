/*
 * $Id$
 *
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.util.Map;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import com.sun.javatest.TestFinder;
import com.sun.javatest.util.I18NResourceBundle;

/**
 * This class searches out test descriptions found in a file system
 * tree. Each attribute of the test description consists of JavaDoc-like tags
 * which provide an tag-name and associated value.  The associated test script
 * for the test suite will interpret the values.  All tags for a given test
 * description must be within the same block comment.  A file may contain
 * multiple test descriptions.
 *
 * @see TestFinder
 */
public class TagTestFinder extends TestFinder
{
    /**
     * Constructs the list of file names to exclude for pruning in the search
     * for files to examine for test descriptions.  This constructor also sets
     * the allowable comment formats.
     */
    public TagTestFinder() {
        exclude(excludeNames);
        addExtension(".java", JavaCommentStream.class);
    }

    /**
     * Decode the arg at a specified position in the arg array.  If overridden
     * by a subtype, the subtype should try and decode any arg it recognizes,
     * and then call super.decodeArg to give the superclass(es) a chance to
     * recognize any arguments.
     *
     * @param args The array of arguments
     * @param i    The next argument to be decoded.
     * @return     The number of elements consumed in the array; for example,
     *             for a simple option like "-v" the result should be 1; for an
     *             option with an argument like "-f file" the result should be
     *             2, etc.
     * @throws TestFinder.Fault If there is a problem with the value of the current arg,
     *             such as a bad value to an option, the Fault exception can be
     *             thrown.  The exception should NOT be thrown if the current
     *             arg is unrecognized: in that case, an implementation should
     *             delegate the call to the supertype.
     */
    protected int decodeArg(String[] args, int i) throws Fault {
        if ("-fast".equalsIgnoreCase(args[i])) {
            fastScan = true;
            return 1;
        }
        else
            return super.decodeArg(args, i);
    }

    /**
     * Scan a file, looking for test descriptions and/or more files to scan.
     * @param file The file to scan
     */
    public void scan(File file) {
        currFile = file;
        if (file.isDirectory())
            scanDirectory(file);
        else
            scanFile(file);
    }

    /**
     * Get the name of the file currently being scanned.
     * @return the name of the file currently being scanned.
     */
    // Ideally, we should be able to get the current line number as well,
    // (for error messages)
    protected File getCurrentFile() {
        return currFile;
    }

    /**
     * Exclude all files with a particular name from being scanned.
     * This will typically be for directories like SCCS, Codemgr_wsdata, etc
     * @param name The name of files to be excluded.
     */
    public void exclude(String name) {
        excludeList.put(name, name);
    }

    /**
     * Exclude all files with particular names from being scanned.
     * This will typically be for directories like SCCS, Codemgr_wsdata, etc
     * @param names The names of files to be excluded.
     */
    public void exclude(String[] names) {
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            excludeList.put(name, name);
        }
    }

    /**
     * Undo an exclude operation.
     * @param name The filename to stop ignoring, should never be null.
     * @see TagTestFinder#exclude
     * @see TagTestFinder#isExcluded(String)
     */
    public void unexclude(String name) {
        excludeList.remove(name);
    }

    /**
     * Determine if the given extension is excluded from scanning.
     * @param s The extension to check for (should not contain the leading dot)
     * @return True if it is excluded, false otherwise.
     * @see TagTestFinder#exclude(java.lang.String)
     * @see TagTestFinder#exclude(java.lang.String[])
     * @see TagTestFinder#unexclude(String)
     */
    public boolean isExcluded(String s) {
        return excludeList.containsKey(s);
    }

    /**
     * Nominate a class to read files that have a particular extension.
     * @param extn      The extension for which this class is to be used
     * @param commentStreamClass
     *                  A class to read files of a particular extension.
     *                  The class must be a subtype of CommentStream
     */
    public void addExtension(String extn, Class commentStreamClass) {
        if (!extn.startsWith("."))
            throw new IllegalArgumentException("extension must begin with `.'");
        if (!CommentStream.class.isAssignableFrom(commentStreamClass))
            throw new IllegalArgumentException("class must be a subtype of " + CommentStream.class.getName());

        extensionTable.put(extn, commentStreamClass);
    }

    /**
     * Get the class used to handle an extension.
     * @param extn The extension in question
     * @return the class previously registered with addExtension
     */
    public Class getClassForExtension(String extn) {
        return (Class)extensionTable.get(extn);
    }

    /**
     * Set the initial tag to be checked for in a test description.
     * If set to null, no initial tag is required.  The default value
     * for the initial tag is "test".  (i.e. @test must appear in the
     * test description.)
     * @param tag The tag to be checked for.
     * @see #getInitialTag
     */
    public void setInitialTag(String tag) {
        initialTag = tag;
    }

    /**
     * Get the current value of the initial tag that is checked for
     * in a test description. If null, no tag is required.
     * @return the value of the required initial tag, or null if none required.
     * @see #setInitialTag
     */
    public String getInitialTag() {
        return initialTag;
    }


    //-----internal routines----------------------------------------------------

    /**
     * Scan a directory, looking for more files to scan
     * @param dir The directory to scan
     */
    private void scanDirectory(File dir) {
        // scan the contents of the directory, checking for
        // subdirectories and other files that should be scanned
        String[] names = dir.list();
        if (names == null) {
            // from File spec, only null if an i/o problem occured
            error(i18n, "tag.readError", dir.getAbsolutePath());
            return;
        }

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
                int dot = name.indexOf('.');
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

    /**
     * Scan a file, looking for comments and in the comments, for test
     * description data.
     * @param file The file to scan
     */
    protected void scanFile(File file) {
        int testDescNumber = 0;
        String name = file.getName();
        int dot = name.indexOf('.');
        if (dot == -1)
            return;
        String extn = name.substring(dot);
        Class csc = (Class)(extensionTable.get(extn));
        if (csc == null) {
            error(i18n, "tag.noParser", new Object[] {file, extn});
            return;
        }
        CommentStream cs = null;
        try {
            cs = (CommentStream)(csc.newInstance());
        }
        catch (InstantiationException e) {
            error(i18n, "tag.cantCreateClass", new Object[] {csc.getName(), extn});
            return;
        }
        catch (IllegalAccessException e) {
            error(i18n, "tag.cantAccessClass", new Object[] {csc.getName(), extn});
            return;
        }

        try {
            cs.init(new BufferedReader(new FileReader(file)));
            if (fastScan)
                cs.setFastScan(true);

            String comment;
            while ((comment = cs.readComment()) != null) {
                Map tagValues = parseComment(comment, file);
                if (tagValues.isEmpty())
                    continue;

                if (tagValues.get("id") == null) {
                    if (testDescNumber  != 0)
                        tagValues.put("id", "id" + (new Integer(testDescNumber)).toString());
                    testDescNumber++;
                }

                // The "test" marker can now be removed so that we don't waste
                // space unnecessarily.  We need to do the remove *after* the
                // isEmpty() check because of the potential to interfere with
                // defaults based on file extension. (i.e. The TD /* @test */
                // still needs to evaluate to a valid test description.)
                tagValues.remove("test");

                foundTestDescription(tagValues, file, /*line*/0);
            }
        }
        catch (FileNotFoundException e) {
            error(i18n, "tag.cantFindFile", file);
        }
        catch (IOException e) {
            error(i18n, "tag.ioError", file);
        }
        finally {
            try {
                cs.close();
            }
            catch (IOException e) {
            }
        }
    }


    /**
     * Given a comment, find all tags of interest.  Return a map
     * containing the name-value pairs for those tags.  If a duplicate
     * name is found, the last name-value will be returned.
     * @param comment   The comment to be parsed.
     * @param currFile  The name of the file currently being read.
     * @return A map containing the name-value pairs read from the comment.
     */
    protected Map parseComment(String comment, File currFile) {
        Map tagValues = new HashMap();
        int tagStart = 0;
        int tagEnd   = 0;

//      System.out.println(comment);
        while (true) {
            tagStart = findTagStart(comment, tagEnd);
            if (tagStart == -1)
                return tagValues;

            tagEnd = findTagEnd(comment, tagStart);
            String tag = comment.substring(tagStart, tagEnd);

            int pos = 0;
            while ((pos < tag.length())
                   && !Character.isWhitespace(tag.charAt(pos)))
                pos++;

            String name = tag.substring(1, pos);
            while ((pos < tag.length())
                   && Character.isWhitespace(tag.charAt(pos)))
                pos++;
            String value = tag.substring(pos);
            value = value.replace('\n', ' ').replace('\r', ' ').trim();

            // The first token of the leading comment in the defining file
            // must be "@test" (or whatever the initialTag is set to.)
            if (tagValues.isEmpty() && initialTag != null && !name.equals(initialTag))
                return tagValues; // i.e. empty

            processEntry(tagValues, name, value);
        }
    }

    /**
     * Identify the start of the next tag.  The start tag is identified as the
     * first token which begins with '@'.  Tokens are space-separated, thus we
     * need to look for the pattern: " @.".  If the very first token of the
     * string begins with '@' then the pattern is "@.".
     *
     * @param s    The string to search.
     * @param pos  The position in the string to start looking for the beginning
     *             of the tag.
     * @return     The position in the string of the start of the next tag.
     */
    private int findTagStart(String s, int pos) {
        while (true) {
            pos = s.indexOf("@", pos);
            if ((pos == -1) || (pos >= (s.length()-1)))
                return -1;
            if (((pos == 0) && !Character.isWhitespace(s.charAt(pos+1)))
                || ((pos > 0) && !Character.isWhitespace(s.charAt(pos+1))
                    && Character.isWhitespace(s.charAt(pos-1))))
                return pos;
            pos++;
        }
    }

    /**
     * Identify the end of the next tag.  The end is identified as either the
     * position before the next tag start or the end of the string.
     *
     * @param s    The string to search.
     * @param tagStart The position in the string which marks the beginning of
     *             the tag.
     * @return     The position in the string of the end of the current tag.
     */
    private int findTagEnd(String s, int tagStart) {
        int end = findTagStart(s, tagStart+1);
        if (end == -1) {
            return s.length();
        }
        return end;
    }

    //----------member variables------------------------------------------------

    private File currFile;
    private HashMap excludeList   = new HashMap();
    private HashMap extensionTable = new HashMap();
    private boolean fastScan = false;
    private String initialTag = "test";

    //private int testDescNumber;
    private static final String[] excludeNames = {
        "SCCS", "deleted_files", ".svn", ".git", ".hg"
    };

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(TagTestFinder.class);

}
