/*
 * $Id$
 *
 * Copyright (c) 2001, 2012, Oracle and/or its affiliates. All rights reserved.
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import com.sun.javatest.util.DynamicArray;
import com.sun.javatest.util.I18NResourceBundle;

/**
 * A set of tests to be excluded from a test run.
 */

public class ExcludeList
{
    /**
     * This exception is used to report problems manipulating an exclude list.
     */
    public static class Fault extends Exception
    {
        Fault(I18NResourceBundle i18n, String s, Object o) {
            super(i18n.getString(s, o));
        }
    }

    /**
     * Test if a file appears to be for an exclude list, by checking the extension.
     * @param f The file to be tested.
     * @return <code>true</code if the file appears to be an exclude list.
     */
    public static boolean isExcludeFile(File f) {
        return f.getPath().endsWith(EXCLUDEFILE_EXTN);
    }

    /**
     * Create a new exclude list.
     */
    public ExcludeList() {
    }

    /**
     * Create an ExcludeList from the data contained in a file.
     * @param f The file to be read.
     * @throws FileNotFoundException if the file cannot be found
     * @throws IOException if any problems occur while reading the file
     * @throws ExcludeList.Fault if the data in the file is ionconsistent
     * @see #ExcludeList(File[])
     */
    public ExcludeList(File f)
        throws FileNotFoundException, IOException, Fault
    {
        this(f, false);
    }

    /**
     * Create an ExcludeList from the data contained in a file.
     * @param f The file to be read.
     * @param strict Indicate if strict data checking rules should be used.
     * @throws FileNotFoundException if the file cannot be found
     * @throws IOException if any problems occur while reading the file
     * @throws ExcludeList.Fault if the data in the file is inconsistent
     * @see #ExcludeList(File[])
     * @see #setStrictModeEnabled(boolean)
     */
    public ExcludeList(File f, boolean strict)
        throws FileNotFoundException, IOException, Fault
    {
        setStrictModeEnabled(strict);
        if (f != null) {
            FileReader fr = new FileReader(f);
            BufferedReader in = new BufferedReader(fr);
            try {
                Parser p = new Parser(in);
                Entry e;
                while ((e = p.readEntry()) != null)
                    addEntry(e);

                title = p.getTitle();
            }
            finally {
                in.close();
                fr.close();
            }
        }
    }


    /**
     * Create an ExcludeList from the data contained in a series of files.
     * @param files The file to be read.
     * @throws FileNotFoundException if any of the files cannot be found
     * @throws IOException if any problems occur while reading the files.
     * @throws ExcludeList.Fault if the data in the files is inconsistent
     * @see #ExcludeList(File)
     */
    public ExcludeList(File[] files)
        throws FileNotFoundException, IOException, Fault
    {
        this(files, false);
    }

    /**
     * Create an ExcludeList from the data contained in a series of files.
     * @param files The file to be read.
     * @param strict Indicate if strict data checking rules should be used.
     * @throws FileNotFoundException if any of the files cannot be found
     * @throws IOException if any problems occur while reading the files.
     * @throws ExcludeList.Fault if the data in the files is inconsistent
     * @see #ExcludeList(File)
     * @see #setStrictModeEnabled(boolean)
     */
    public ExcludeList(File[] files, boolean strict)
        throws FileNotFoundException, IOException, Fault
    {
        setStrictModeEnabled(strict);
        for (int i = 0; i < files.length; i++) {
            ExcludeList et = new ExcludeList(files[i], strict);
            merge(et);
        }
    }

    /**
     * Specify whether strict mode is on or not. In strict mode, calls to addEntry
     * may generate an exception in the case of conflicts, such as adding an entry
     * to exclude a specific test case when the entire test is already excluded.
     * @param on true if strict mode should be enabled, and false otherwise
     * @see #isStrictModeEnabled
     */
    public void setStrictModeEnabled(boolean on) {
        //System.err.println("EL.setStrictModeEnabled " + on);
        strict = on;
    }

    /**
     * Check whether strict mode is enabled or not. In strict mode, calls to addEntry
     * may generate an exception in the case of conflicts, such as adding an entry
     * to exclude a specific test case when the entire test is already excluded.
     * @return true if strict mode is enabled, and false otherwise
     * @see #setStrictModeEnabled
     */
    public boolean isStrictModeEnabled() {
        return strict;
    }

    /**
     * Test if a specific test is completely excluded according to the table.
     * It is completely excluded if there is an entry, and the test case field is null.
     * @param td A test description for the test being checked.
     * @return <code>true</code> if the table contains an entry for this test.
     */
    public boolean excludesAllOf(TestDescription td) {
        return excludesAllOf(td.getRootRelativeURL());
    }

    /**
     * Test if a specific test is completely excluded according to the table.
     * It is completely excluded if there is an entry, and the test case field is null.
     * @param url The test-suite root-relative URL for the test.
     * @return <code>true</code> if the table contains an entry for this test.
     */
    public boolean excludesAllOf(String url) {
        Object o = table.get(new Key(url));
        return (o != null && o instanceof Entry && ((Entry)o).testCase == null);
    }

    /**
     * Test if a specific test is partially or completely excluded according to the table.
     * It is so excluded if there is any entry in the table for the test.
     * @param td A test description for the test being checked.
     * @return <code>true</code> if the table contains an entry for this test.
     */
    public boolean excludesAnyOf(TestDescription td) {
        return excludesAnyOf(td.getRootRelativeURL());
    }

    /**
     * Test if a specific test is partially or completely excluded according to the table.
     * It is so excluded if there is any entry in the table for the test.
     * @param url The test-suite root-relative URL for the test.
     * @return <code>true</code> if the table contains an entry for this test.
     */
    public boolean excludesAnyOf(String url) {
        Object o = table.get(new Key(url));
        return (o != null);
    }

    /**
     * Get the test cases to be excluded for a test.
     *
     * @param td A test description for the test being checked.
     * @return an array of test case names if any test cases are to
     * be excluded. The result is null if the test is not found or is
     * completely excluded without specifying test cases.  This may be
     * a mix of single TC strings or a comma separated list of them.
     */
    public String[] getTestCases(TestDescription td) {
        Key key = new Key(td.getRootRelativeURL());
        synchronized (table) {
            Object o = table.get(key);
            if (o == null)
                // not found
                return null;
            else if (o instanceof Entry) {
                Entry e = (Entry)o;
                if (e.testCase == null)
                    // entire test excluded
                    return null;
                else
                    return (new String[] {e.testCase});
            }
            else {
                Entry[] ee = (Entry[])o;
                String[] testCases = new String[ee.length];
                for (int i = 0; i < ee.length; i++)
                    testCases[i] = ee[i].testCase;
                return testCases;
            }
        }
    }

    /**
     * Add an entry to the table.
     * @param e The entry to be added; if an entry already exists for this test
     *  description, it will be replaced.
     * @throws ExcludeList.Fault if the entry is for the entire test and
     *   there is already an entry for a test case for this test, or vice versa.
     */
    public void addEntry(Entry e) throws Fault {
        synchronized (table) {
            Key key = new Key(e.relativeURL);
            Object o = table.get(key);

            if (o == null) {
                // easy case: nothing already exists in the table, so just
                // add this one
                table.put(key, e);
            }
            else if (o instanceof Entry) {
                // a single entry exists in the table, so need to check for
                // invalid combinations of test cases and tests
                Entry curr = (Entry)o;
                if (curr.testCase == null) {
                    if (e.testCase == null)
                        // overwrite existing entry for entire test
                        table.put(key, e);
                    else {
                        if (strict) {
                            // can't exclude test case when entire test already excluded
                            throw new Fault(i18n, "excl.cantExcludeCase", e.relativeURL);
                        }
                        // else ignore new entry since entire test is already excluded
                    }
                }
                else {
                    if (e.testCase == null) {
                        if (strict) {
                            // can't exclude entire test when test case already excluded
                            throw new Fault(i18n, "excl.cantExcludeTest", e.relativeURL);
                        }
                        else {
                            // overwrite existing entry for a test case with
                            // new entry for entire test
                            table.put(key, e);
                        }
                    }
                    else if (curr.testCase.equals(e.testCase)) {
                        // overwrite existing entry for the same test case
                        table.put(key, e);
                    }
                    else {
                        // already excluded one test case, now we need to exclude
                        // another; make an array to hold both entries against the
                        // one key
                        table.put(key, new Entry[] {curr, e});
                    }
                }
            }
            else {
                // if there is an array, it must be for unique test cases
                if (e.testCase == null) {
                    if (strict) {
                        // can't exclude entire test when selected test cases already excluded
                        throw new Fault(i18n, "excl.cantExcludeTest", e.relativeURL);
                    }
                    else {
                        // overwrite existing entry for list of test cases with
                        // new entry for entire test
                        table.put(key, e);
                    }
                }
                else {
                    Entry[] curr = (Entry[])o;
                    for (int i = 0; i < curr.length; i++) {
                        if (curr[i].testCase.equals(e.testCase)) {
                            curr[i] = e;
                            return;
                        }
                    }
                    // must be a new test case, add it into the array
                    table.put(key, DynamicArray.append(curr, e));
                }
            }

        }
    }

    /**
     * Locate an entry for a test.
     * @param url The root relative URL for the test; the URL may include
     * a test case if necessary included in square brackets after the URL proper.
     * @return The entry for the test, or null if there is none.
     */
    public Entry getEntry(String url) {
        String testCase = null;
        if (url.endsWith("]")) {
            int i = url.lastIndexOf("[");
            if (i != -1) {
                testCase = url.substring(i+1, url.length()-1);
                url = url.substring(0, i);
            }
        }
        return getEntry(url, testCase);
    }

    /**
     * Locate an entry for a test.
     *
     * @param url The root relative URL for the test.
     * @param testCase An optional test case to be taken into account.  This cannot
     *  be a comma separated list.  A value of null will match any entry with the given
     *  url.
     * @return The entry for the test, or null if the URL cannot be found.
     */
    public Entry getEntry(String url, String testCase) {
        // XXX what if multiple entries?
        Key key = new Key(url);
        Object o = table.get(key);
        if (o == null)
            return null;
        else if (o instanceof Entry) {
            Entry e = (Entry)o;
            if (testCase == null)
                return e;
            else
                return (isInList(e.testCase, testCase) ? e : null);
        }
        else {
            Entry[] entries = (Entry[])o;
            for (int i = 0; i < entries.length; i++) {
                Entry e = entries[i];
                if (isInList(e.testCase, testCase))
                    return e;
            }
            return null;
        }
    }

    /**
     * Merge the contents of another exclude list into this one.
     * The individual entries are merged;  The title of the exclude list
     * being merged is ignored.
     * @param other the exclude list to be merged with this one.
     *
     */
    public void merge(ExcludeList other) {
        synchronized (table) {
            for (Iterator iter = other.getIterator(false); iter.hasNext(); ) {
                Entry otherEntry = (Entry) (iter.next());
                Key key = new Key(otherEntry.relativeURL);
                Object o = table.get(key);
                if (o == null) {
                    // Easy case: nothing already exists in the table, so just
                    // add this one
                    table.put(key, otherEntry);
                }
                else if (o instanceof Entry) {
                    // A single entry exists in the table
                    Entry curr = (Entry)o;
                    if (curr.testCase == null || otherEntry.testCase == null) {
                        table.put(key, new Entry(curr.relativeURL, null,
                                            mergeBugIds(curr.bugIdStrings, otherEntry.bugIdStrings),
                                            mergePlatforms(curr.platforms, otherEntry.platforms),
                                            mergeSynopsis(curr.synopsis, otherEntry.synopsis)));
                    }
                    else
                        table.put(key, new Entry[] {curr, otherEntry});
                }
                else if (otherEntry.testCase == null) {
                    // An array of test cases exist in the table, but we're merging
                    // an entry for the complete test, so flatten down to a single entry
                    // for the whole test
                    String[] bugIdStrings = otherEntry.bugIdStrings;
                    String[] platforms = otherEntry.platforms;
                    String synopsis = otherEntry.synopsis;
                    Entry[] curr = (Entry[])o;
                    for (int i = 0; i < curr.length; i++) {
                        bugIdStrings = mergeBugIds(bugIdStrings, curr[i].bugIdStrings);
                        platforms = mergePlatforms(platforms, curr[i].platforms);
                        synopsis = mergeSynopsis(synopsis, curr[i].synopsis);
                    }
                    table.put(key, new Entry(otherEntry.relativeURL, null,
                                             bugIdStrings, platforms, synopsis));
                }
                else {
                    // An array of test cases exist in the table, and we're merging
                    // an entry with another set of test cases.
                    // For now, concatenate the arrays.
                    // RFE: Replace Entry[] with Set and merge the sets.
                    table.put(key, (Entry[]) DynamicArray.append((Entry[]) o, otherEntry));
                }
            }
        }
    }

    static String[] mergeBugIds(String[] a, String[] b) {
        return merge(a, b);
    }

    static String[] mergePlatforms(String[] a, String[] b) {
        return merge(a, b);
    }

    static String[] merge(String[] a, String[] b) {
        SortedSet s = new TreeSet();
        s.addAll(Arrays.asList(a));
        s.addAll(Arrays.asList(b));
        return (String[]) (s.toArray(new String[s.size()]));
    }

    static String mergeSynopsis(String a, String b) {
        if (a == null || a.trim().length() == 0)
            return b;
        else if (b == null || b.trim().length() == 0)
            return a;
        else if (a.indexOf(b) != -1)
            return a;
        else if (b.indexOf(a) != -1)
            return b;
        else
            return a + "; " + b;
    }


    /**
     * Remove an entry from the table.
     * @param e the entry to be removed
     */
    public void removeEntry(Entry e) {
        synchronized (table) {
            Key key = new Key(e.relativeURL);
            Object o = table.get(key);
            if (o == null)
                // no such entry
                return;
            else if (o instanceof Entry) {
                if (o == e)
                    table.remove(key);
            }
            else {
                Entry[] o2 = (Entry[])DynamicArray.remove((Entry[])o, e);
                if (o2 == o)
                    // not found
                    return;
                else {
                    if (o2.length == 1)
                        table.put(key, o2[0]);
                    else
                        table.put(key, o2);
                }
            }
        }
    }

    /**
     * Check whether an exclude list has any entries or not.
     * @return true if this exclude list has no entries
     * @see #size
     */
    public boolean isEmpty() {
        return table.isEmpty();
    }

    /**
     * Get the number of entries in the table.
     * @return the number of entries in the table
     * @see #isEmpty
     */
    public int size() {
        // ouch, this is now expensive to compute
        int n = 0;
        for (Iterator i = table.values().iterator(); i.hasNext(); ) {
            Object o = i.next();
            if (o instanceof Entry[])
                n += ((Entry[])o).length;
            else
                n++;
        }
        return n;
    }

    /**
     * Iterate over the contents of the table.
     * @param group if <code>true</code>, entries for the same relative
     * URL are grouped together, and if more than one, returned in an
     * array; if <code>false</code>, the iterator always returns
     * separate entries.
     * @see Entry
     * @return an iterator for the table: the entries are either
     * single instances of @link(Entry) or a mixture of @link(Entry)
     * and @link(Entry)[], depending on the <code>group</code>
     * parameter.
     */
    public Iterator getIterator(boolean group) {
        if (group)
            return table.values().iterator();
        else {
            // flatten the enumeration into a vector, then
            // enumerate that
            Vector v = new Vector(table.size());
            for (Iterator iter = table.values().iterator(); iter.hasNext(); ) {
                Object o = iter.next();
                if (o instanceof Entry)
                    v.addElement(o);
                else {
                    Entry[] entries = (Entry[])o;
                    for (int i = 0; i < entries.length; i++)
                        v.addElement(entries[i]);
                }
            }
            return v.iterator();
        }

    }

    /**
     * Get the title for this exclude list.
     * @return the title for this exclude list
     * @see #setTitle
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the title for this exclude list.
     * @param title the title for this exclude list
     * @see #getTitle
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Write the table out to a file.
     * @param f The file to which the table should be written.
     * @throws IOException is thrown if any problems occur while the
     * file is being written.
     */
    public void write(File f) throws IOException {
        // sort the entries for convenience, and measure col widths
        int maxURLWidth = 0;
        int maxBugIdWidth = 0;
        int maxPlatformWidth = 0;
        SortedSet entries = new TreeSet();
        for (Iterator iter = getIterator(false); iter.hasNext(); ) {
            Entry entry = (Entry) (iter.next());
            entries.add(entry);
            if (entry.testCase == null)
                maxURLWidth = Math.max(entry.relativeURL.length(), maxURLWidth);
            else
                maxURLWidth = Math.max(entry.relativeURL.length() + entry.testCase.length() + 2, maxURLWidth);
            maxBugIdWidth = Math.max(bugIdsToString(entry).length(), maxBugIdWidth);
            maxPlatformWidth = Math.max(platformsToString(entry).length(), maxPlatformWidth);
        }

        BufferedWriter out = new BufferedWriter(new FileWriter(f));
        out.write("# Exclude List");
        out.newLine();
        out.write("# SCCS %" + 'W' + "% %" + 'E' + "%"); // TAKE CARE WITH SCCS HEADERS
        out.newLine();
        if (title != null) {
            out.write("### title " + title);
            out.newLine();
        }
        for (Iterator iter = entries.iterator(); iter.hasNext(); ) {
            Entry e = (Entry) (iter.next());
            if (e.testCase == null)
                write(out, e.relativeURL, maxURLWidth + 2);
            else
                write(out, e.relativeURL + "[" + e.testCase + "]", maxURLWidth + 2);
            write(out, bugIdsToString(e), maxBugIdWidth + 2);
            write(out, platformsToString(e), maxPlatformWidth + 2);
            out.write(e.synopsis);
            out.newLine();
        }
        out.close();
    }

    private void write(Writer out, String s, int width) throws IOException {
        out.write(s);
        for (int i = s.length(); i < width; i++)
            out.write(' ');
    }

    private String bugIdsToString(Entry e) {
        StringBuffer sb = new StringBuffer(e.bugIdStrings.length*10);
        sb.append(e.bugIdStrings[0]);
        for (int i = 1; i < e.bugIdStrings.length; i++) {
            sb.append(',');
            sb.append(e.bugIdStrings[i]);
        }
        return sb.toString();
    }

    private String platformsToString(Entry e) {
        StringBuffer sb = new StringBuffer(e.platforms.length*10);
        sb.append(e.platforms[0]);
        for (int i = 1; i < e.platforms.length; i++) {
            sb.append(',');
            sb.append(e.platforms[i]);
        }
        return sb.toString();
    }

    private static boolean equals(String s1, String s2) {
        return (s1 == null && s2 == null
                || s1 != null && s2 != null && s1.equals(s2));
    }

    /**
     * Is val in the comma separated list.
     *
     * @param list Comma separated list or a single value.
     * @return Null if either parameter is null.
     */
    private static boolean isInList(String list, String val) {
        // check for invalid args
        if (list == null || val == null)
            return false;

        // loop through possible matches
        for (int pos = list.indexOf(val); pos != -1; pos = list.indexOf(val, pos + 1) ) {
            // check beginning of string
            if (!(pos == 0 || list.charAt(pos -1) == ','))
                continue;

            // check end of string
            if (!(pos + val.length() == list.length()  || list.charAt(pos + val.length()) == ','))
                continue;

            // beginning and end are OK; got a match
            return true;
        }

        // no good matches found
        return false;
    }

    /**
     * @param obj - object to compare
     * @return returns true if two entry tables are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExcludeList other = (ExcludeList) obj;
        if (this.table != other.table && (this.table == null ||
                !this.table.equals(other.table))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + (this.table != null ? this.table.hashCode() : 0);
        return hash;
    }

    private Map table = new HashMap();
    private String title;
    private boolean strict;

    private static final class Parser {
        Parser(Reader in) throws IOException {
            this.in = in;
            ch = in.read();
        }

        String getTitle() {
            return title;
        }

        Entry readEntry() throws IOException, Fault {
            String url = readURL(); // includes optional test case
            if (url == null)
                return null;
            String testCase = null; // for now

            if (url.endsWith("]")) {
                int i = url.lastIndexOf("[");
                if (i != -1) {
                    testCase = url.substring(i+1, url.length()-1);
                    url = url.substring(0, i);
                }
            }
            String[] bugIdStrings = readBugIds();
            String[] platforms = readPlatforms();
            String synopsis = readRest();
            return new Entry(url, testCase, bugIdStrings, platforms, synopsis);
        }

        private boolean isEndOfLine(int ch) {
            return (ch == -1 || ch == '\n' || ch == '\r');
        }

        private boolean isWhitespace(int ch) {
            return (ch == ' ' || ch == '\t');
        }

        private String readURL() throws IOException, Fault {
            // skip white space, comments and blank lines until a word is found
            for (;;) {
                skipWhite();
                switch (ch) {
                case -1:
                    // end of file
                    return null;
                case '#':
                    // comment
                    skipComment();
                    break;
                case '\r':
                case '\n':
                    // blank line (or end of comment)
                    ch = in.read();
                    break;
                default:
                    return readWord();
                }
            }
        }

        private String[] readBugIds() throws IOException {
            // skip white space, then read and sort a list of comma-separated
            // numbers with no embedded white-space
            skipWhite();
            TreeSet s = new TreeSet();
            StringBuffer sb = new StringBuffer();
            for ( ; !isEndOfLine(ch) && !isWhitespace(ch); ch = in.read()) {
                if (ch == ',') {
                    if (sb.length() > 0) {
                        s.add(sb.toString());
                        sb.setLength(0);
                    }
                }
                else
                    sb.append((char) ch);
            }

            if (sb.length() > 0)
                s.add(sb.toString());

            if (s.size() == 0)
                s.add("0");  // backwards compatibility

            return (String[]) (s.toArray(new String[s.size()]));
        }

        private String[] readPlatforms() throws IOException {
            // skip white space, then read and sort a list of comma-separated
            // platform names with no embedded white space. Since the
            // set of platforms (and their combinations) is small,
            // share the result amongst all equivalent entries.
            skipWhite();
            String s = readWord();
            String[] platforms = (String[])platformCache.get(s);
            if (platforms == null) {
                // split string into sorted comma separated pieces
                int n = 0;
                for (int i = 0; i < s.length(); i++) {
                    if (s.charAt(i) == ',')
                        n++;
                }
                TreeSet ts = new TreeSet();
                int start = 0;
                int end = s.indexOf(',');
                while (end != -1) {
                    ts.add(s.substring(start, end));
                    start = end + 1;
                    end = s.indexOf(',', start);
                }
                ts.add(s.substring(start));
                platforms = (String[]) (ts.toArray(new String[ts.size()]));
                platformCache.put(s, platforms);
            }
            return platforms;
        }

        private String readRest() throws IOException {
            // skip white space, then read up to the end of the line
            skipWhite();
            StringBuffer word = new StringBuffer(80);
            for ( ; !isEndOfLine(ch); ch = in.read())
                word.append((char)ch);
            // skip over terminating character
            ch = in.read();
            return word.toString();
        }

        private String readWord() throws IOException {
            // read characters up to the next white space
            StringBuffer word = new StringBuffer(32);
            for ( ; !isEndOfLine(ch) && !isWhitespace(ch); ch = in.read())
                word.append((char)ch);
            return word.toString();
        }

        private void skipComment() throws IOException, Fault {
            ch = in.read();
            // first # has already been read
            if (ch == '#') {
                ch = in.read();
                if (ch == '#') {
                    ch = in.read();
                    skipWhite();
                    String s = readWord();
                    if (s.equals("title")) {
                        skipWhite();
                        title = readRest();
                        return;
                    }
                }
            }
            while (!isEndOfLine(ch))
                ch = in.read();
        }

        private void skipWhite() throws IOException {
            // skip horizontal white space
            // input is line-oriented, so do not skip over end of line
            while (ch != -1 && isWhitespace(ch))
                ch = in.read();
        }

        private Reader in;      // source stream being read
        private int ch;         // current character
        private Map platformCache = new HashMap();
                                // cache of results for readPlatforms
        private String title;
    };

    private static class Key {
        Key(String url) {
            relativeURL = url;
        }

        @Override
        public int hashCode() {
            // the hashCode for a key is the hashcode of the normalized URL.
            // The normalized URL is url.replace(File.separatorChar, '/').toLowerCase();
            int h = hash;
            if (h == 0) {
                int len = relativeURL.length();

                for (int i = 0; i < len; i++) {
                    char c = relativeURL.charAt(i);
                    if (!caseSensitive)
                        c = Character.toLowerCase(c);

                    if (c == sep)
                        c = '/';
                    h = 31*h + c;
                }
                hash = h;
            }
            return h;
        }

        @Override
        public boolean equals(Object o) {
            // Two keys are equal if their normalized URLs are equal.
            // The normalized URL is url.replace(File.separatorChar, '/').toLowerCase();
            if (o == null || !(o instanceof Key))
                return false;
            String u1 = relativeURL;
            String u2 = ((Key) o).relativeURL;
            int len = u1.length();
            if (len != u2.length())
                return false;
            for (int i = 0; i < len; i++) {
                char c1 = u1.charAt(i);

                if (c1 == sep)
                    c1 = '/';
                else if(!caseSensitive)
                    c1 = Character.toLowerCase(c1);

                char c2 = u2.charAt(i);

                if (c2 == sep)
                    c2 = '/';
                else if(!caseSensitive)
                    c2 = Character.toLowerCase(c2);

                if (c1 != c2)
                    return false;
            }
            return true;
        }

        private static final char sep = File.separatorChar;
        private String relativeURL;
        private int hash;
    }

    /**
     * An entry in the exclude list.
     */
    public static final class Entry implements Comparable {
        /**
         * Create an ExcludeList entry.
         * @param u The URL for the test, specified relative to the test suite root.
         * @param tc One or more test cases within the test to be excluded.
         * @param b An array of bug identifiers, justifying why the test is excluded.
         * @param p An array of platform identifiers, on which the faults are
         *              known to occur
         * @param s A short synopsis of the reasons why the test is excluded.
         */
        public Entry(String u, String tc, String[] b, String[] p, String s) {
            if (b == null || p == null)
                throw new NullPointerException();

            // The file format cannot support platforms but no bugids,
            // so fault that; other combinations (bugs, no platforms;
            // no bugs, no platforms etc) are acceptable.
            if (b.length == 0 &&  p.length > 0)
                throw new IllegalArgumentException();

            relativeURL = u;
            testCase = tc;
            bugIdStrings = b;
            platforms = p;
            synopsis = s;
        }
        /**
         * Create an ExcludeList entry.
         * @param u The URL for the test, specified relative to the test suite root.
         * @param tc One or more test cases within the test to be excluded.
         * @param b An array of bug numbers, justifying why the test is excluded.
         * @param p An array of platform identifiers, on which the faults are
         *              known to occur
         * @param s A short synopsis of the reasons why the test is excluded.
     * @deprecated use constructor with String[] bugIDs instead
         */
        public Entry(String u, String tc, int[] b, String[] p, String s) {
            if (b == null || p == null)
                throw new NullPointerException();

            // The file format cannot support platforms but no bugids,
            // so fault that; other combinations (bugs, no platforms;
            // no bugs, no platforms etc) are acceptable.
            if (b.length == 0 &&  p.length > 0)
                throw new IllegalArgumentException();

            relativeURL = u;
            testCase = tc;

            bugIdStrings = new String[b.length];
            for (int i = 0; i < b.length; i++)
                bugIdStrings[i] = String.valueOf(b[i]);
            bugIds = b;

            platforms = p;
            synopsis = s;
        }

        public int compareTo(Object o) {
            Entry e = (Entry) o;
            int n = relativeURL.compareTo(e.relativeURL);
            if (n == 0) {
                if (testCase == null && e.testCase == null)
                    return 0;
                else if (testCase == null)
                    return -1;
                else if (e.testCase == null)
                    return +1;
                else
                    return testCase.compareTo(e.testCase);
            }
            else
                return n;
        }

        /**
         * Get the relative URL identifying the test referenced by this entry.
         * @return the relative URL identifying the test referenced by this entry
         */
        public String getRelativeURL() {
            return relativeURL;
        }

        /**
         * Get the (possibly empty) list of test cases for this entry.
         * An entry can have zero, one, or a comma separated list of TCs.
         *
         * @return List, or null if there are no test cases.
         */
        public String getTestCases() {
            return testCase;
        }

        /**
         * Get the same data as getTestCases(), but split into many Strings
         * This method is costly, so use with care.
         *
         * @return The parsed comma list, or null if there are no test cases.
         */
        public String[] getTestCaseList() {
            // code borrowed from StringArray
            // it is a little wasteful to recalc everytime but saves space
            if (testCase == null)
                return null;

            Vector v = new Vector();
            int start = -1;
            for (int i = 0; i < testCase.length(); i++) {
                if (testCase.charAt(i) == ',') {
                    if (start != -1)
                        v.addElement(testCase.substring(start, i));
                    start = -1;
                } else
                    if (start == -1)
                        start = i;
            }
            if (start != -1)
                v.addElement(testCase.substring(start));

            if (v.size() == 0)
                return null;

            String[] a = new String[v.size()];
            v.copyInto(a);
            return a;
        }

        /**
         * Get the set of bug IDs referenced by this entry.
         * @return the bugs referenced by the entry
     * @deprecated use getBugIdStrings() instead
         */
        public int[] getBugIds() {
            if (bugIds == null) {
                bugIds = new int[bugIdStrings.length];
                for (int i = 0; i < bugIds.length; i++) {
                    try {

                        StringBuilder sb = new StringBuilder();
                        String str = bugIdStrings[i];
                        for (int j = 0; j < str.length(); j++) {
                            if (Character.isDigit(str.charAt(j))) {
                                sb.append(str.charAt(j));
                            }
                        }

                        bugIds[i] = Integer.parseInt(sb.toString());
                    } catch (NumberFormatException e) {
                        bugIds[i] = -1;
                    }
                }
            }

            return bugIds;
        }

        /**
         * Get the set of bug IDs referenced by this entry.
         * @return the bugs referenced by the entry
         */
        public String[] getBugIdStrings() {
            return bugIdStrings;
        }

        /**
         * Get the set of platforms or keywords associated with this entry.
         * These should normally give details about why the test has been
         * excluded.
         * @return the set of platforms or keywords associated with this entry
         */
        public String[] getPlatforms() {
            return platforms;
        }

        /**
         * Get a short description associated with this entry.
         * This should normally give details about why the test has been
         * excluded.
         * @return a short description associated with this entry
         */
        public String getSynopsis() {
            return synopsis;
        }

        /**
         * Create an entry from a string. The string should be formatted
         * as though it were a line of text in an exclude file.
         * @param text The text to be read
         * @return the first entry read from the supplied text
         * @throws ExcludeList.Fault if there is a problem reading the entry.
         */
        public static Entry read(String text) throws Fault {
            try {
                return new Parser(new StringReader(text)).readEntry();
            }
            catch (IOException e) {
                throw new Fault(i18n, "excl.badEntry", e);
            }
        }

        /**
         * Compare this entry against another.
         * @param o the object to compare against
         * @return true is the objects are bothe ExcludeList.Entries containing
         *    the same details
         */
        public boolean equals(Object o) {
            if (o instanceof Entry) {
                Entry e = (Entry)o;
                return equals(relativeURL, e.relativeURL)
                    && equals(testCase, e.testCase)
                    && equals(bugIdStrings, e.bugIdStrings)
                    && equals(platforms, e.platforms)
                    && equals(synopsis, e.synopsis);
            }
            else
                return false;
        }

        @Override
        public int hashCode() {
            return relativeURL.hashCode();
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer(64);
            sb.append(relativeURL);
            if (testCase != null) {
                sb.append('[');
                sb.append(testCase);
                sb.append(']');
            }
            if (bugIdStrings != null) {
                for (int i = 0; i<bugIdStrings.length; i++) {
                    sb.append(i == 0 ? ' ' : ',');
                    sb.append(bugIdStrings[i]);
                }
            }
            if (platforms != null) {
                for (int i = 0; i<platforms.length; i++) {
                    sb.append(i == 0 ? ' ' : ',');
                    sb.append(platforms[i]);
                }
            }
            if (synopsis != null) {
                sb.append(' ');
                sb.append(synopsis);
            }
            return new String(sb);
        }

        private static boolean equals(int[] i1, int[] i2) {
            if (i1 == null || i2 == null)
                return (i1 == null && i2 == null);

            if (i1.length != i2.length)
                return false;

            for (int x = 0; x < i1.length; x++)
                if (i1[x] != i2[x])
                    return false;

            return true;
        }

        private static boolean equals(String[] s1, String[] s2) {
            if (s1 == null || s2 == null)
                return (s1 == null && s2 == null);

            if (s1.length != s2.length)
                return false;

            for (int x = 0; x < s1.length; x++) {
                if (!equals(s1[x], s2[x]))
                    return false;
            }

            return true;
        }

        private static boolean equals(String s1, String s2) {
            return (s1 == null && s2 == null
                    || s1 != null && s2 != null && s1.equals(s2));
        }


        private String relativeURL;
        private String testCase;
        private String[] bugIdStrings;
        private int[] bugIds; // null, unless required
        private String[] platforms;
        private String synopsis;
    }

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(ExcludeList.class);
    /**
     * The standard extension for exclude-list files. (".jtx")
     */
    public static final String EXCLUDEFILE_EXTN = ".jtx";
    private static boolean caseSensitive = Boolean.getBoolean("javatest.caseSensitiveJtx");
}
