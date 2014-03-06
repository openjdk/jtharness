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
package com.sun.javatest;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import com.sun.javatest.util.PropertyArray;
import com.sun.javatest.util.StringArray;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * TestDescription objects embody the parameters of a test and provide the
 * ability to run a test. The parameters are normally found by parsing
 * HTML files and looking for distinguished constructions whose parameters
 * provide the necessary description of a test.
 */

public class TestDescription implements Serializable
{
    /**
     * Construct a test description from the parameters of a recognized descriptions.
     *
     * @param root      The root file of the test suite
     * @param file      The file containing the test description
     * @param params    The collected parameters of the test description
     * @throws IllegalArgumentException if the file argument is an absolute
     * filename and does not begin with the root filename.
     *
     */
    public TestDescription(File root, File file, Map params)
                throws IllegalArgumentException {

        synchronized (this.getClass()) {
            if (root.equals(cachedRoot))
                rootDir = cachedRootDir;
            else {
                if (root.exists() ? root.isFile() : root.getName().endsWith(".html"))
                    rootDir = root.getParent();
                else
                    rootDir = root.getPath();

                // cache root->rootDir map to avoid making extra files
                cachedRoot = root;
                cachedRootDir = rootDir;
            }
        }

        String fp = file.getPath();
        String rootRelativeFile;
        if (file.isAbsolute()) {
            String rp = rootDir;
            if (! (fp.startsWith(rp) && fp.charAt(rp.length()) == File.separatorChar))
                throw new IllegalArgumentException("file must be relative to root: " + file);
            rootRelativeFile = fp.substring(rp.length() + 1);
        }
        else
            rootRelativeFile = fp;
        rootRelativePath = rootRelativeFile.replace(File.separatorChar, '/');

        Vector v = new Vector(0, params.size() * 2);
        for (Iterator i = params.keySet().iterator(); i.hasNext(); ) {
            String key = (String) (i.next());
            String value = (String)(params.get(key));
            insert(v, key, value);
        }
        fields = new String[v.size()];
        v.copyInto(fields);
    }

    /**
     * Internal constructor used by load()
     */
    private TestDescription(String root, String file, String[] params) {
        rootDir = root;
        // skip over the root part of the filename.
        char sep = file.charAt(root.length());
        rootRelativePath = file.substring(root.length() + 1).replace(sep, '/');

        Vector v = new Vector(0, params.length);
        for (int i = 0; i < params.length; i += 2) {
            String key = params[i];
            if (!(key.startsWith("$") || key.equals("testsuite") ||  key.equals("file"))) {
                // don't keep synthetic values from save;
                String value = params[i+1];
                insert(v, key, value);
            }
        }
        fields = new String[v.size()];
        v.copyInto(fields);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + Arrays.deepHashCode(this.fields);
        return hash;
    }

    @Override
    public boolean equals(Object td) {
        if (!(td instanceof TestDescription))
            return false;

        TestDescription otherTd = (TestDescription)td;

        // a quick and simple check
        if (otherTd.getParameterCount() != getParameterCount()) {
            return false;
        }

        // raw compare
        int pos = 0;
        while (pos < fields.length) {
            String otherVal = otherTd.getParameter(fields[pos]);

            if (otherVal == null ||
                !otherVal.equals(fields[pos+1])) {
                return false;
            }

            pos += 2;
        }

        return true;
    }

    /**
     * Get the directory for this test description.
     * @return the directory containing this test description
     */
    public File getDir() {
        return new File(getFile().getParent());
    }

    /**
     * Get the file for this test description.
     * WARNING: If this description has been read in from a .jtr file, the rootDir
     * may be inappropriate for this system.
     * @return the file containing this test description
     */
    public File getFile() {
        return new File(rootDir, rootRelativePath.replace('/', File.separatorChar));
    }

    /**
     * Get the id within the file for this test description.
     * @return the id within the file of this test description
     */
    public String getId() {
        return getParameter("id");
    }

    /**
     * Get the title of this test description. This title is determined from
     * the "title" parameter, if present, defaulting to the value returned
     * by <code>getName()</code>.
     * @return the title of this test description
     */
    public String getTitle() {
        // default title to name
        String title = getParameter("title");
        if (title == null)
            title = getName();

        return title;
    }

    /**
     * Get the name of this test description; if not given explicitly,
     * it defaults to the filename root of the first source file.
     * @return the name of this test description
     */
    public String getName() {

        // the name used to be a parameter you could set explicitly
        // now, it is based on the file and id
        int lastSep = rootRelativePath.lastIndexOf('/');
        String name = (lastSep == -1 ?
                       rootRelativePath :
                       rootRelativePath.substring(lastSep + 1));

        // strip off extension
        int dot = name.indexOf('.');
        if (dot != -1)
            name = name.substring(0, dot);

        String id = getParameter("id");
        if (id != null)
            name = name + "_" + id;

        return name;
    }

    /**
     * Get the set of keywords for this test description,
     * as specified by the "keywords" parameter.
     * @return the set of keywords
     */
    public String[] getKeywords() {
        return StringArray.split(getParameter("keywords"));
    }

    /**
     * Get the set of keywords for this test description,
     * as specified by the "keywords" parameter.
     * They are returned in canonical form (lower-case).
     * @return the set of keywords
     */
    public Set getKeywordTable() {
        String[] keys = StringArray.split(getParameter("keywords"));
        Set s = new TreeSet();
        for (int i = 0; i < keys.length; i++) {
            String k = keys[i].toLowerCase();
            s.add(k);
        }
        return s;
    }

    /**
     * Get the set of source files for this test description,
     * as specified by the "source" parameter.
     * @return The sources as specified in the HTML test description
     * @see #getSourceFiles
     */
    public String[] getSources() {
        return StringArray.split(getParameter("source"));
    }

    /**
     * Get the set of source files for this test description,
     * as specified by the "source" parameter. The files in the
     * "source" parameter should normally be relative, in which
     * case, they will be evaluated relative to the directory
     * containing this test description. Then, if any of the
     * files are under the user's current directory, they will
     * be returned relative to that directory; otherwise, they
     * will be returned as absolute filenames.
     * @return filenames specified by the source parameter.
     * @see #getSources
     */
    public File[] getSourceFiles() {
        String dir  = getFile().getParent();
        String[] srcs = getSources();
        File[] sourceFiles = new File[srcs.length];
        String userCurrDir = System.getProperty("user.dir") + File.separator;
        for (int i = 0; i < srcs.length; i++) {
            File f = new File(dir, srcs[i].replace('/', File.separatorChar));

            // normalize name
            try {
                f = f.getCanonicalFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            String s = f.getPath();
            if (s.startsWith(userCurrDir)) {
                s = s.substring(userCurrDir.length());
                sourceFiles[i] = new File(s);
            } else
                sourceFiles[i] = f;
        }
        return sourceFiles;
    }


    /**
     * Get a list of associated files for a specified test description.
     * Normally, this will include the file containing the test description,
     * and any source files used by the test.  By default, the source files
     * are determined from the test description's "source" entry.
     * @return a list of associated files for this test description
     */
    public URL[] getSourceURLs() {
        ArrayList<File> res = new ArrayList<File>();

        // always include the file containing the test description
        res.add(getFile());
        // add in files given in source parameter
        res.addAll(Arrays.asList(getSourceFiles()));

        URL[] urls = new URL[res.size()];
        for (int i = 0; i < res.size(); i++) {
            try {
                urls[i] = res.get(i).toURI().toURL();
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
                urls[i] = null;
            }
        }

        return urls;
    }


    /**
     * Get the optional class directory for this test description,
     * as specified by the "classDir" parameter.
     * @return the class directory, or null if not specified
     * @deprecated use <code>getParameter("classDir")</code> instead
     */
    public String getClassDir() {
        return getParameter("classDir");
    }

    /**
     * Get the execution class for this test description,
     * as specified by the "executeClass" parameter.
     * @return the execute class name, or null if not specified
     * @deprecated use <code>getParameter("executeClass")</code> instead
     */
    public String getExecuteClass() {
        return getParameter("executeClass");
    }

    /**
     * Get the execution args for this test description,
     * as specified by the "executeArgs" parameter.
     * @return the execute args, or null if not specified
     * @deprecated use <code>getParameter("executeArgs")</code> instead
     */
    public String getExecuteArgs() {
        return getParameter("executeArgs");
    }

    /**
     * Get the requested timeout value for this test description,
     * as specified by the "timeout" parameter.
     * @return the timeout value, or 0 if not specified
     * @deprecated  use <code>getParameter("timeout")</code> instead
     */
    public int getTimeout() {
        String t = getParameter("timeout");
        if (t == null)
            return 0;
        else
            return Integer.parseInt(t);
    }

    /**
     * Get the root file for this test suite;
     * THIS IS PROVIDED FOR BACKWARDS COMPATIBILTY FOR JCK ONLY.
     * It returns the name of testsuite.html within the root directory
     * of the test suite.
     * WARNING: If this description has been read in from a .jtr file, the rootDir
     * may be inappropriate for this system.
     * @return the root file for this test suite
     * @see #getRootDir
     * @deprecated No longer relevant for some test suites, so will not be supported
     *    in the future.  If needed the value can be determined by asking the test
     *    suite's <code>TestFinder</code>.
     */
    public File getRoot() {
        return new File(rootDir, "testsuite.html");
    }

    /**
     * Get the root directory for this test suite
     * WARNING: If this description has been read in from a .jtr file, the rootDir
     * may be inappropriate for this system.
     * @return the root directory for this test suite
     */
    public String getRootDir() {
        return rootDir;
    }

    /**
     * Get the path of the test, relative to the root dir for the test suite.
     * This is the path to the source file for this description.
     * The internal separator is always '/'.
     * @return the path for this test description within the test suite
     */
    public String getRootRelativePath() {
        return rootRelativePath;
    }

    /**
     * Get the file of the test, relative to the root dir for the test suite.
     * @return A platform specific path to the source file.
     */
    public File getRootRelativeFile() {
        return new File(rootRelativePath.replace('/', File.separatorChar));
    }

    /**
     * Get the url of the test, relative to the root dir for the test suite.
     * This is the path to the source file for this description, plus the
     * test id if necessary.  Again, the path separator is always '/'.
     * @return a relative URL for this test within the test suite
     */
    public String getRootRelativeURL() {
        if (rrurl == null) {
            String id = getParameter("id");
            rrurl = (id == null ?  rootRelativePath : rootRelativePath + "#" + id);
            rrurl = rrurl.intern();
        }

        return rrurl;
    }

    /**
     * Get the path of the test directory, relative to the root directory for the test suite.
     * @return the a relative path to the directory containing this test description
     *
     * @deprecated Use getRootRelativeFile().getParent()
     */
    public File getRootRelativeDir() {
        String p = getRootRelativeFile().getParent();
        return (p == null ? new File(".") : new File(p));
    }

    /**
     * Get the number of parameters contained in this test description.
     * @return the number of parameters
     */
    public int getParameterCount() {
        return fields.length / 2;
    }

    /**
     * Get an iterator for the names of the parameters contained in
     * this test description.
     * @return an iterator for the names of the parameters
     */
    public Iterator getParameterKeys() {
        return new Iterator() {
            int pos = 0;

            public boolean hasNext() {
                if(fields == null || fields.length == 0 ||
                   pos >= fields.length) {
                   return false;
                } else {
                   return true;
                }
            }

            public Object next() {
                if(fields == null || fields.length == 0 ||
                   pos == fields.length) {
                   return null;
                } else {
                   String current = fields[pos];
                   pos += 2;
                   return current;
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Get a parameter of the test description by name.
     * @param key the name of the parameter value to be returned
     * @return the value of the specified parameter, or null if not found
     */
    public String getParameter(String key) {
        int lower = 0;
        int upper = fields.length - 2;
        int mid;

        if (upper < 0)
            return null;

        String last = fields[upper];
        int cmp = key.compareTo(last);
        if (cmp > 0)
            return null;

        while (lower <= upper) {
            // in next line, take care to ensure that mid is always even
            mid = lower + ((upper - lower) / 4) * 2;
            String e = fields[mid];
            cmp = key.compareTo(e);
            if (cmp < 0) {
                upper = mid - 2;
            }
            else if (cmp > 0) {
                lower = mid + 2;
            }
            else
                return fields[mid+1];
        }

        // did not find an exact match
        return null;
    }

    /**
     * Simple standard debugging output.
     */
    public String toString() {
        return ("TestDescription[" + getTitle() + "]");
    }

    /**
     * Save TestDescription to a dictionary
     * WARNING: If this description has been read in from a .jtr file, the rootDir
     * may be inappropriate for this system.
     */
    void save(Map p) {
        saveField(p, "$root", rootDir);
        saveField(p, "$file", getFile().getPath());
        for (int i = 0; i < fields.length; i+=2) {
            saveField(p, fields[i], fields[i+1]);
        }
    }

    private void saveField(Map p, String key, String value) {
        if (value != null)
            p.put(key, value);
    }

    /**
     * Recover TestDescription from saved dictionary
     */
    static TestDescription load(String[] params) {
        //File r = new File((String)d.get("testsuite"));
        //if (!r.isDirectory())
        //    r = new File(r.getParent());
        //File f = new File((String)d.get("file"));
        String r = PropertyArray.get(params, "$root");
        if (r == null)
            r = PropertyArray.get(params, "testsuite");
        String f = PropertyArray.get(params, "$file");
        if (f == null)
            f = PropertyArray.get(params, "file");
        return new TestDescription(r, f, params);
    }

    private static void insert(Vector v, String key, String value) {
        int lower = 0;
        int upper = v.size() - 2;
        int mid = 0;

        if (upper < 0) {
            v.addElement(key);
            v.addElement(value);
            return;
        }

        String last = (String)v.elementAt(upper);
        int cmp = key.compareTo(last);
        if (cmp > 0) {
            v.addElement(key);
            v.addElement(value);
            return;
        }

        while (lower <= upper) {
            // in next line, take care to ensure that mid is always even
            mid = lower + ((upper - lower) / 4) * 2;
            String e = (String)(v.elementAt(mid));
            cmp = key.compareTo(e);
            if (cmp < 0) {
                upper = mid - 2;
            }
            else if (cmp > 0) {
                lower = mid + 2;
            }
            else
                throw new Error("should not happen");
        }

        // did not find an exact match (we did not expect to)
        // adjust the insert point
        if (cmp > 0)
            mid += 2;

        v.insertElementAt(key, mid);
        v.insertElementAt(value, mid+1);
    }

    //-----member variables-------------------------------------------------------

    /**
     * Root directory for the test suite
     * WARNING: If this description has been read in from a .jtr file, the rootDir
     * may be inappropriate for this system.
     * @serial
     */
    private String rootDir;

    /**
     * Root relative path for this test description within the test suite
     * @serial
     */
    private String rootRelativePath;

    /**
     * The data for this test description, organized as a sequence of
     * name-value pairs.
     * @serial
     */
    private String[] fields;

    /**
     * Cached version of the root relative path.
     * @see #getRootRelativeURL
     */
    private String rrurl;

    private static File cachedRoot;
    private static String cachedRootDir;
}

