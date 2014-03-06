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
package com.sun.javatest.finder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.SortedSet;
import java.util.TreeSet;

import com.sun.javatest.TestDescription;
import com.sun.javatest.TestEnvironment;
import com.sun.javatest.TestFinder;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.StringArray;

/**
 * A TestFinder that delegates to different test finders in different
 * areas of the test suite, as described by a special "map" file.
 */
public class ChameleonTestFinder extends TestFinder {
    /**
     * Create an uninitialized ChameleonTestFinder.
     */
    public ChameleonTestFinder() {
        String ic = System.getProperty("javatest.chameleon.ignoreCase");
        if (ic != null)
            ignoreCase = ic.equals("true");
        else {
            String os = System.getProperty("os.name");
            ignoreCase = os.startsWith("Windows");
        }
        exclude(excludeNames);
    }

    /**
     * Exclude all files with a particular name from being scanned.
     * This will typically be for directories like SCCS, Codemgr_wsdata, etc
     * @param name The name of files to be excluded
     */
    public void exclude(String name) {
        excludeList.put(name, name);
    }

    /**
     * Exclude all files with particular names from being scanned.
     * This will typically be for directories like SCCS, Codemgr_wsdata, etc
     * @param names The names of files to be excluded
     */
    public void exclude(String[] names) {
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            excludeList.put(name, name);
        }
    }

    /**
     * Check whether or not to ignore case when matching files against entries.
     *
     * By default, case will be ignored on Windows platforms.
     * (<code><font size=-1>System.getProperty("os.name").startsWith("Windows")</font></code>)
     * This can be overridden by setting the following system property:
     * <pre>
     *    -Djavatest.chameleon.ignoreCase=true|false
     * </pre>
     *
     * This in turn can be overridden by using -ignoreCase or -dontIgnoreCase
     * in the args to {@link #init init}.
     *
     * @return  whether or not to ignore case when matching files against entries.
     * @see #setIgnoreCase
     */
    public boolean getIgnoreCase() {
        return ignoreCase;
    }

    /**
     * Set whether or not to ignore case when matching files against entries.
     *
     * @param b whether or not to ignore case when matching files against entries.
     * @see #getIgnoreCase
     */
    public void setIgnoreCase(boolean b) {
        ignoreCase = b;
    }
    /**
     * Generic initialization routine. You can also initialize the test finder
     * directly, with {@link #exclude}, {@link #readEntries}, etc.
     * @param args An array of strings giving initialization data.
     *  The primary option is "-f <em>file</em>" to specify the name
     *  of the file describing which test finder to use in which section
     *  of the test suite.
     * @param testSuiteRoot     The root file of the test suite.
     * @param env               This argument is not required by this test finder.
     * @throws TestFinder.Fault if an error is found during initialization.
     */
    public void init(String[] args, File testSuiteRoot, TestEnvironment env) throws Fault {
        super.init(args, testSuiteRoot, env);

        if (entryFile == null)
            throw new Fault(i18n, "cham.noConfigFile");

        if (!entryFile.isAbsolute())
            entryFile = new File(getRootDir(), entryFile.getPath());

        readEntries(entryFile);
    }

    /**
     * Read the entries in a file which describe which test finder to use
     * in which part of the test suite.
     * The file is read line by line. If a line is empty or begins with
     * a '#' character, the line is ignored. Otherwise the line is broken
     * up as follows:<br>
     * <em>directory-or-file</em>  <em>finder-class-name</em>  <em>finder-args</em> <em>...</em><br>
     * Then, when a file is to be read by the test finder, the entries above are
     * checked in the order they were read for an entry whose first word matches
     * the beginning of the name of the file to be read. If and when a match
     * is found, the test finder delegates the request to the test finder specified
     * in the rest of the entry that provided the match.
     * @param file              The file containing the entries to be read.
     * @throws TestFinder.Fault if a problem occurs while reading the file.
     */
    public void readEntries(File file) throws Fault {
        //System.err.println("reading " + file);
        SortedSet s = new TreeSet(new Comparator() {
            public int compare(Object o1, Object o2) {
                int n = ((Entry)o1).compareTo((Entry)o2);
                // this gives us the reverse of the order we want, so ...
                return -n;
            }
        });

        FileReader fr = null;
        BufferedReader in = null;
        try {
            fr = new FileReader(file);
            in = new BufferedReader(fr);
            String line;
            int lineNum = 0;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                lineNum++;
                if (line.startsWith("#")  ||  line.length() == 0)
                    continue;

                String[] words = StringArray.split(line);
                if (words.length < 2) {
                    throw new Fault(i18n, "cham.missingData",
                                    new Object[] {new Integer(lineNum), line});
                }

                String pattern = words[0];
                String finderClassName = words[1];
                String[] finderArgs = new String[words.length - 2];
                System.arraycopy(words, 2, finderArgs, 0, finderArgs.length);
                Entry e = new Entry(pattern, finderClassName, finderArgs);
                s.add(e);
            }
        }
        catch (FileNotFoundException e) {
            throw new Fault(i18n, "cham.cantFindFile", file);
        }
        catch (IOException e) {
            throw new Fault(i18n, "cham.ioError",
                            new Object[] {file, e});
        }
        finally {
            // attempt to close buffered writer first
            // followed by the underlying writer for leak prevention
            if (in != null){
                try { in.close(); } catch (IOException e) { }
            }

            if (fr != null){
                try { fr.close(); } catch (IOException e) { }
            }
        }

        entryFile = file;
        entries = (Entry[]) (s.toArray(new Entry[s.size()]));

        //for (int i = 0; i < entries.length; i++)
        //    System.err.println(entries[i].prefix + "   " + entries[i].suffix);
    }

    protected int decodeArg(String[] args, int i) throws Fault {
        if (args[i].equals("-f") && i + 1 < args.length) {
            entryFile = new File(args[i + 1]);
            return 2;
        }
        else if (args[i].equalsIgnoreCase("-ignoreCase")) {
            ignoreCase = true;
            return 1;
        }
        else if (args[i].equalsIgnoreCase("-dontIgnoreCase")) {
            ignoreCase = false;
            return 1;
        }
        else
            return super.decodeArg(args, i);
    }


    /**
     * Scan a file, looking for test descriptions and other files that might
     * need to be scanned.  The implementation depends on the type of test
     * finder.
     * @param file The file to scan
     */
    protected void scan(File file) {
        //System.err.println("SCAN: " + file);
        if (file.isDirectory())
            scanDirectory(file);
        else
            scanFile(file);
    }

    public File[] getFiles() {
        if (currEntry == null)
            return super.getFiles();
        else if (currEntry.finder == null)
            return new File[0];
        else
            return currEntry.finder.getFiles();
    }

    public TestDescription[] getTests() {
        if (currEntry == null)
            return super.getTests();
        else if (currEntry.finder == null)
            return new TestDescription[0];
        else
            return currEntry.finder.getTests();
    }


    //-----internal routines----------------------------------------------------

    /**
     * Scan a directory, looking for more files to scan
     * @param dir The directory to scan
     */
    private void scanDirectory(File dir) {
        // scan the contents of the directory, checking for
        // subdirectories and other files that should be scanned
        currEntry = null;
        String[] names = dir.list();
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            // if the file should be ignored, skip it
            // This is typically for directories like SCCS etc
            if (excludeList.containsKey(name))
                continue;

            foundFile(new File(dir, name));
        }
    }

    private void scanFile(File file) {
        // see if the file matches a registered test finder, and if so
        // delegate to that
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].matches(file)) {
                currEntry = entries[i];
                currEntry.scanFile(file);
                return;
            }
        }
        // no match found
        currEntry = null;
    }

    private Object newInstance(Class c) throws Fault {
        try {
            return c.newInstance();
        }
        catch (InstantiationException e) {
            throw new Fault(i18n, "cham.cantCreateClass",
                            new Object[] {c.getName(), e});
        }
        catch (IllegalAccessException e) {
            throw new Fault(i18n, "cham.cantAccessClass",
                            new Object[] {c.getName(), e});
        }
    }

    private Class loadClass(String className) throws Fault {
        try {
            if (loader == null)
                return Class.forName(className);
            else
                return loader.loadClass(className);
        }
        catch (ClassNotFoundException e) {
            throw new Fault(i18n, "cham.cantFindClass",
                            new Object[] {className, e});
        }
        catch (IllegalArgumentException e) {
            throw new Fault(i18n, "cham.badClassName", className);
        }

    }

    private TestEnvironment getEnv() {
        return env;
    }

    private File entryFile;
    private Entry[] entries;
    private boolean ignoreCase;
    private Entry currEntry;
    private ClassLoader loader;
    private Hashtable excludeList = new Hashtable();

    private static final String[] excludeNames = {
        "SCCS", "deleted_files"
    };

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(ChameleonTestFinder.class);

    private class Entry {
        Entry(String pattern, String finderClassName, String[] finderArgs) {
            int star = pattern.indexOf('*');
            if (star == -1) {
                prefix = pattern;
                suffix = null;
            }
            else {
                prefix = pattern.substring(0, star);
                suffix = pattern.substring(star+1);
            }
            prefix = new File(getRootDir(), prefix.replace('/', File.separatorChar)).getPath();
            if (suffix != null)
                suffix = suffix.replace('/', File.separatorChar);

            this.finderClassName = finderClassName;
            this.finderArgs = finderArgs;

            //System.err.println("created entry: prefix: " + prefix);
            //System.err.println("created entry: suffix: " + suffix);
            //System.err.println("created entry:  class: " + finderClassName);
            //System.err.println("created entry:   args: " + StringArray.join(finderArgs));
        }

        boolean matches(File file) {
            //System.err.println("checking " + file);
            //System.err.println(" prefix: " + prefix);
            //System.err.println(" suffix: " + suffix);

            String p = file.getPath();
            int pLen = p.length();
            int preLen = prefix.length();

            // if file does not match the prefix, return false
            if (!p.regionMatches(ignoreCase, 0, prefix, 0, preLen))
                return false;

            // if there is a suffix, and the suffix is too short or the
            // file does not match the prefix, return false
            if (suffix != null) {
                int sufLen = suffix.length();

                if (sufLen > pLen)
                    return false;

                if (!p.regionMatches(ignoreCase, pLen - sufLen, suffix, 0, sufLen))
                    return false;
            }

            // if we matched the prefix and possible suffix, we're done
            return true;
        }

        void scanFile(File file) {
            if (!initialized)
                init();

            if (finder != null)
                finder.read(file);
        }

        private void init() {
            try {
                if (!finderClassName.equals("-")) {
                    finder = (TestFinder)(newInstance(loadClass(finderClassName)));
                    finder.init(finderArgs, getRoot(), getEnv());
                }
            }
            catch (Fault e) {
                error(i18n, "cham.cantInitClass", e.getMessage());
            }
            finally {
                initialized = true;
            }
        }

        int compareTo(Entry other) {
            Entry a = this;
            Entry b = other;
            int apl = a.prefix.length();
            int bpl = b.prefix.length();
            if (apl < bpl)
                return -1;
            else if (apl == bpl) {
                int pc = a.prefix.compareTo(b.prefix);
                if (pc != 0)
                    return pc;
                else {
                    // prefixes are the same, check the suffixes
                    String as = a.suffix;
                    String bs = b.suffix;
                    if (as == null && bs == null)
                        return 0;

                    if (as == null)
                        return -1;

                    if (bs == null)
                        return +1;

                    return as.compareTo(bs);
                }
            }
            else
                return +1;
        }

        private String prefix;
        private String suffix;
        private String finderClassName;
        private String[] finderArgs;
        private TestFinder finder;
        private boolean initialized;
    }
}
