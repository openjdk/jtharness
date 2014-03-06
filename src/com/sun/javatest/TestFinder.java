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
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.StringArray;

/**
 * Base implementation for test finders which search for test descriptions
 * given a starting location.  When creating instances of TestFinder for use,
 * the creator should be sure to call the init() method before use.
 */
public abstract class TestFinder
{
    /**
     * This exception is to report serious problems that occur while
     * finding tests.
     */
    public static class Fault extends Exception
    {
        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param msgKey The key for the detail message.
         */
        public Fault(I18NResourceBundle i18n, String msgKey) {
            super(i18n.getString(msgKey));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param msgKey The key for the detail message.
         * @param arg An argument to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        public Fault(I18NResourceBundle i18n, String msgKey, Object arg) {
            super(i18n.getString(msgKey, arg));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param msgKey The key for the detail message.
         * @param args An array of arguments to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        public Fault(I18NResourceBundle i18n, String msgKey, Object[] args) {
            super(i18n.getString(msgKey, args));
        }
    }

    /**
     * This interface is used to report significant errors found while
     * reading files, but which are not of themselves serious enough
     * to stop reading further. More serious errors can be reported by
     * throwing TestFinder.Fault.
     * @see TestFinder#error
     * @see TestFinder#localizedError
     * @see TestFinder.Fault
     */
    public static interface ErrorHandler {
        /**
         * Report an error found while reading a file.
         * @param msg A detail string identifying the error
         */
        void error(String msg);
    }


    /**
     * Initialize the data required by the finder.
     * Clients creating instances of test finders should call this before allowing use
     * of the finder.  Not doing so may result in unexpected results.
     * @param args
     *          An array of strings specified as arguments in the environment.  Null
     *          indicates no args.
     * @param testSuiteRoot
     *          The root file that will be passed to test descriptions read
     *          by the finder.
     * @param env
     *          The environment being used to run the test.  May be null.
     * @throws TestFinder.Fault if  there is a problem interpreting any of args.
     */
    public void init(String[] args, File testSuiteRoot, TestEnvironment env) throws Fault {
        if (args != null)
            decodeAllArgs(args);

        setRoot(testSuiteRoot);
        this.env = env;
    }

    /**
     * Initialize the data required by the finder.
     * Clients creating instances of test finders should call this before allowing use
     * of the finder.  Not doing so may result in unexpected results.
     * @param args
     *          An array of strings specified as arguments in the environment.  Null
     *          indicates no args.
     * @param testSuiteRoot
     *          The root file that will be passed to test descriptions read
     *          by the finder.
     * @param tests
     *          The tests to be read by the finder. (ignored)
     * @param filters
     *          An optional array of filters to filter the tests read by the finder.
     * @param env
     *          The environment being used to run the test.  May be null.
     * @throws TestFinder.Fault if there is a problem interpreting any of args.
     * @see #init(String[],File,TestEnvironment)
     * @deprecated Use one of the other init() methods.  This functionality is no
     *    longer supported.  Methods on TestResultTable should yield similar
     *    results.
     */
    public void init(String[] args, File testSuiteRoot,
                     File[] tests, TestFilter[] filters,
                     TestEnvironment env) throws Fault {
        init(args, testSuiteRoot, env);
    }

    /**
     * Perform argument decoding, calling decodeArg for successive
     * args until exhausted.
     * @param args The arguments to be decoded
     * @throws TestFinder.Fault if decodeArg throws the exception
     * while decoding one of the arguments, or if decodeArg does
     * not recognize an argument.
     */
    protected void decodeAllArgs(String[] args) throws Fault {
        for (int i = 0; i < args.length; ) {
            int j  = decodeArg(args, i);
            if (j == 0) {
                throw new Fault(i18n, "finder.badArg", args[i]);
            }
            i += j;
        }
    }

    /**
     * Decode the arg at a specified position in the arg array.
     * If overridden by a subtype, the subtype should try and decode any
     * args it recognizes, and then call super.decodeArg to give the
     * superclass(es) a chance to recognize any arguments.
     * @param args      The array of arguments
     * @param i         The next argument to be decoded
     * @return          The number of elements consumed in the array;
     *                  for example, for a simple option like "-v" the
     *                  result should be 1; for an option with an argument
     *                  like "-f file" the result should be 2, etc.
     * @throws TestFinder.Fault If there is a problem with the value of the current
     *                  arg, such as a bad value to an option, the Fault
     *                  exception can be thrown. The exception should NOT be
     *                  thrown if the current arg is unrecognized: in that case,
     *                  an implementation should delegate the call to the
     *                  supertype.
     */
    protected int decodeArg(String[] args, int i) throws Fault {
        return 0;
    }

    /**
     * Set the test suite root file or directory.
     * @param testSuiteRoot The path to be set as the root of the
     * test suite in which files will be read.
     * @throws IllegalStateException if already set
     * @throws TestFinder.Fault if there is some test-finder-specific
     * problem with the specified file.
     * @see #getRoot
     */
    protected void setRoot(File testSuiteRoot) throws IllegalStateException, Fault {
        if (root != null)
            throw new IllegalStateException("root already set");

        root = (testSuiteRoot.isAbsolute() ?
                testSuiteRoot : new File(userDir, testSuiteRoot.getPath()));
        rootDir = (root.isDirectory() ?
                   root : new File(root.getParent()));
    }

    /**
     * Get the root file of the test suite, as passed in to the
     * <code>init</code> method.
     * @return the root file of the test suite
     * @see #setRoot
     */
    public File getRoot() {
        return root;
    }

    /**
     * Get the root directory of the test suite; this is either the
     * root passed in to the init method or if that is a file, it is
     * the directory containing the file.
     * @return the root directory of the test suite
     */
    public File getRootDir() {
        return rootDir;
    }

    //--------------------------------------------------------------------------

    /**
     * Incoming files and test descriptions are sorted by their name during
     * processing, this method allows adjustment of the comparison method to
     * be used during this sorting.  Sorting can be disabled by calling this
     * method with a <code>null</code> parameter.  By default, this class will
     * do US Locale sorting.
     *
     * @param c The comparison operator to be used.  Null indicates no sorting (old behavior).
     * @see #getComparator
     * @see #foundTestDescription(TestDescription)
     * @see #foundFile(File)
     * @since 3.2
     */
    public void setComparator(Comparator c) {
        comp = c;

    }

    /**
     * Get the current comparator being used.
     *
     * @return The current comparator, may be null.
     * @see #setComparator
     * @since 3.2
     */
    public Comparator getComparator() {
        return comp;
    }

    /**
     * Get the default to be used when the user does not want to specify
     * their own.  The default is a US Locale Collator.
     * @return The comparator which would be used if a custom one was not provided.
     */
    protected Comparator getDefaultComparator() {
        // this is the default
        Collator c = Collator.getInstance(Locale.US);
        c.setStrength(Collator.PRIMARY);
        return c;
    }

    //--------------------------------------------------------------------------

    /**
     * Get the registered error handler.
     * @return The error handler currently receiving error messages.  May
     *          be null.
     * @see #setErrorHandler
     */
    public ErrorHandler getErrorHandler() {
        return errHandler;
    }

    /**
     * Set an error handler to be informed of errors that may arise
     * while reading tests. This is typically used to report errors
     * that are not associated with any specific test, such as syntax
     * errors outside of any test description, or problems accessing files.
     * @param h The error handler that will be informed of non-fatal
     *          errors that occur while reading the test suite
     * @see #getErrorHandler
     */
    public void setErrorHandler(ErrorHandler h) {
        errHandler = h;
    }

    /**
     * Report an error to the error handler.
     * @param i18n A resource bundle containing the localized error messages
     * @param key  The name of the entry in the resource bundle containing
     * the appropriate error message.
     * The message should not need any arguments.
     */
    protected void error(I18NResourceBundle i18n, String key) {
        localizedError(i18n.getString(key));
    }

    /**
     * Report an error to the error handler.
     * @param i18n A resource bundle containing the localized error messages
     * @param key  The name of the entry in the resource bundle containing
     * the appropriate error message.
     * The message will be formatted with a single argument, using
     * MessageFormat.format.
     * @param arg The argument to be formatted in the message found in the
     * resource bundle
     */
    protected void error(I18NResourceBundle i18n, String key, Object arg) {
        localizedError(i18n.getString(key, arg));
    }

    /**
     * Report an error to the error handler.
     * @param i18n A resource bundle containing the localized error messages
     * @param key  The name of the entry in the resource bundle containing
     * the appropriate error message.
     * The message will be formatted with an array of arguments, using
     * MessageFormat.format.
     * @param args The arguments to be formatted in the message found in the
     * resource bundle
     */
    protected void error(I18NResourceBundle i18n, String key, Object[] args) {
        localizedError(i18n.getString(key, args));
    }

    /**
     * Report a message to the error handler, without additional processing.
     * @param msg The message to be reported
     * @see #error
     */
    protected void localizedError(String msg) {
        errorMessages.add(msg);
        if (errHandler != null)
            errHandler.error(msg);
    }

    /**
     * Get an count of the number of errors found by this test finder,
     * as recorded by calls to the error handler via error and localizedError.
     * The count may be reset using the clearErrors method.
     * @return the number of errors found by the test finder
     * @see #getErrors
     * @see #clearErrors
     */
    public synchronized int getErrorCount() {
        return errorMessages.size();
    }

    /**
     * Get the errors recorded by the test finder, as recorded by calls
     * to the error handler via error and localizedError. Errors reported
     * by the error methods will be given localized. If there are no errors.\,
     * an empty array (not null) will be returned.
     * @return the errors found by the test finder
     */
    public synchronized String[] getErrors() {
        String[] errs = new String[errorMessages.size()];
        errorMessages.copyInto(errs);
        return errs;
    }

    /**
     * Clear outstanding errors found by the test finder, so that until
     * a new error is reported, getErrorCount will return 0 and getErrors
     * will return an empty array.
     */
    public synchronized void clearErrors() {
        errorMessages.setSize(0);
    }


    //--------------------------------------------------------------------------

    /**
     * Determine whether a location corresponds to a directory (folder) or
     * an actual file.  If the finder implementation chooses, the locations
     * used in read() and scan() may be real or virtual.  This method will be
     * queried to determine if a location is a container or something that
     * should be scanned for tests.  If it is both...
     * @since 4.0
     * @param path The location in question.
     */
    public boolean isFolder(File path) {
        if (!path.isAbsolute()) {
            File f = new File(getRoot(), path.getPath());
            return f.isDirectory();
        }
        else
            return path.isDirectory();
    }

    /**
     * Determine when the last time this path was modified.  This is used
     * to decide whether to rescan that location or not.  The default implementation
     * defers the choice to the java.
     * @since 4.0
     * @param f The location in question.
     */
    public long lastModified(File f) {
        if (f.isAbsolute())
            return f.lastModified();
        else {
            File real = new File(getRoot(), f.getPath());
            return real.lastModified();
        }
    }

    /**
     * Read a file, looking for test descriptions and other files that might
     * need to be read.  If the file is relative, it will be evaluated relative
     * to getRootDir. Depending on the test finder, the file may be either
     * a plain file or a directory.
     * @param file The file to be read.
     */
    public synchronized void read(File file) {
        if (tests != null)
            tests.setSize(0);

        if (files != null)
            files.setSize(0);

        testsInFile.clear();

        scan(file.isAbsolute() ? file : new File(rootDir, file.getPath()));
        //scan(file);
    }

    /**
     * Scan a file, looking for test descriptions and other files that might
     * need to be scanned.  The implementation depends on the type of test
     * finder.
     * @param file The file to scan
     */
    protected abstract void scan(File file);

    /**
     * Handle a test description entry read from a file.
     * By default, the name-value pair is inserted into the entries
     * dictionary; however, the method can be overridden by a subtype
     * to adjust the name or value before putting it into the dictionary,
     * or even to ignore/fault the pair.
     * @param entries   The dictionary of the entries being read
     * @param name      The name of the entry that has been read
     * @param value     The value of the entry that has been read
     */
    protected void processEntry(Map entries, String name, String value) {
        // uniquefy the keys as they go into the entries table
        name = name.intern();

        if (name.equalsIgnoreCase("keywords")) {
            // canonicalize keywords in their own special table
            String keywordCacheValue = (String)keywordCache.get(value);
            if (keywordCacheValue == null) {
                String lv = value.toLowerCase();
                String[] lvs = StringArray.split(lv);
                Arrays.sort(lvs);
                keywordCacheValue = StringArray.join(lvs).intern();
                keywordCache.put(value, keywordCacheValue);
            }
            value = keywordCacheValue;
        }
        else
            value = value.intern();

        entries.put(name, value);
    }

    // cache for canonicalized lists of keywords
    private Map keywordCache = new HashMap();

    /**
     * "normalize" the test description entries read from a file.
     * By default, this is a no-op;  however, the method can be overridden
     * by a subtype to supply default values for missing entries, etc.
     * @param entries  A set of tag values read from a test description in a file
     * @return       A normalized set of entries
     */
    protected Map normalize(Map entries) {
        return entries;
    }


    //--------------------------------------------------------------------------

    /**
     * Report that data for a test description has been found.
     * @param entries The data for the test description
     * @param file   The file being read
     * @param line   The line number within the file (used for error messages)
     */
    protected void foundTestDescription(Map entries, File file, int line) {
        entries = normalize(entries);

        if (debug) {
            System.err.println("Found TestDescription");

            System.err.println("--------values----------------------------");
            for (Iterator i = entries.keySet().iterator() ; i.hasNext() ;) {
                Object key = i.next();
                System.err.println(">> " + key + ": " + entries.get(key) );
            }
            System.err.println("------------------------------------------");
        }

        String id = (String)(entries.get("id"));
        if (id == null)
            id = "";

        // make sure test has unique id within file
        Integer prevLine = (Integer)testsInFile.get(id);
        if (prevLine != null) {
            int i = 1;
            String newId;
            while (testsInFile.get(newId = (id + "__" + i)) != null)
                i++;

            error(i18n, "finder.nonUniqueId",
                  new Object[] { file,
                                     (id.equals("") ? "(unset)" : id),
                                     new Integer(line),
                                     prevLine,
                                     newId }
                  );

            id = newId;
            entries.put("id", id);
        }

        testsInFile.put(id, new Integer(line));

        // create the test description
        TestDescription td = new TestDescription(root, file, entries);

        if (errHandler != null) {
            // more checks: check that the path does not include white space,
            // because the exclude list parser does not handle paths with whitespace
            String rru = td.getRootRelativeURL();
            if (rru.indexOf(' ') != -1) {
                error(i18n, "finder.spaceInId", td.getRootRelativeURL());
            }
        }

        foundTestDescription(td);
    }

    /**
     * Report that a test description has been found.
     * @param td     The data for the test description.  May never be null.
     * @see #foundTestDescription(java.util.Map, java.io.File, int)
     */
    protected void foundTestDescription(TestDescription td) {
        if (debug) {
            System.err.println("Found TestDescription" + td.getName());
        }

        if (tests == null)
            tests = new Vector();

        int target = 0;

        // binary insert
        if (tests.size() == 0) {
            target = 0;
        }
        else if (comp == null) {
            target = tests.size();  // at end
        }
        else {
            int left = 0, right = tests.size()-1, center = 0;
            String name = td.getName();

            while (left < right) {
                center = ((right+left)/2);
                int cmp = comp.compare(name, ((TestDescription)(tests.get(center))).getName());
                if (cmp < 0)
                    right = center;
                else if (cmp >= 0)
                    left = center+1;
            }   // while

            if (comp.compare(name, ((TestDescription)(tests.get(left))).getName()) > 0)
                target = left+1;
            else
                target = left;

            /* old insertion sort
            for (int i = 0; i < tests.size(); i++) {
                if (comp.compare(td.getName(),
                                ((TestDescription)tests.elementAt(i)).getName()) > 0) {
                    target = i;
                    break;
                }
                else { }
            }   // for
            */
        }

        tests.insertElementAt(td, target);
    }

    /**
     * Get the test descriptions that were found by the most recent call
     * of read.
     * @return the test descriptions that were found by the most recent call
     * of read.
     * @see #read
     * @see #foundTestDescription
     */
    public TestDescription[] getTests() {
        if (tests == null)
            return noTests;
        else {
            TestDescription[] tds = new TestDescription[tests.size()];
            tests.copyInto(tds);
            return tds;
        }
    }

    private static final TestDescription[] noTests = { };

    /**
     * Report that another file that needs to be read has been found.
     * @param newFile the file that has been found that needs to be read.
     * @see #read
     * @see #getFiles
     */
    protected void foundFile(File newFile) {
        if (files == null)
            files = new Vector();

        int target = 0;

        // binary insert
        if (files.size() == 0) {
            target = 0;
        }
        else if (comp == null) {
            target = files.size();  // at end
        }
        else {
            int left = 0, right = files.size()-1, center = 0;
            String path = newFile.getPath();

            while (left < right) {
                center = ((right+left)/2);
                int cmp = comp.compare(path, ((File)(files.get(center))).getPath());
                if (cmp < 0)
                    right = center;
                else if (cmp >= 0)
                    left = center+1;
            }   // while

            if (comp.compare(path, ((File)(files.get(left))).getPath()) > 0)
                target = left+1;
            else
                target = left;
        }

        // this is insertion sort to get locale sensitive sorting of
        // the test suite content
        /*
        int target = files.size();

        if (comp != null) {
            for (int i = 0; i < files.size(); i++) {
                if (comp.compare(newFile.getPath(),
                                ((File)files.elementAt(i)).getPath()) < 0) {
                    target = i;
                    break;
                }
                else { }
            }   // for
        }
        else {
            // just let it insert at the end
        }
        */

        files.insertElementAt(newFile, target);
    }

    /**
     * Get the files that were found by the most recent call
     * of read.
     * @return the files that were found by the most recent call of read.
     * @see #read
     * @see #foundFile
     */
    public File[] getFiles() {
        if (files == null)
            return new File[0];
        else {
            File[] fs = new File[files.size()];
            files.copyInto(fs);
            return fs;
        }
    }


    //----------member variables------------------------------------------------
    private File root;
    private File rootDir;

    /**
     * The environment passed in when the test finder was initialized.
     * It is not used by the basic test finder code, but may be used
     * by individual test finders to modify test descriptions as they are
     * read.
     * @deprecated This feature was available in earlier versions of
     * JT Harness but does not interact well with JT Harness 3.0's GUI features.
     * Use with discretion, if at all.
     */
    protected TestEnvironment env;
    private ErrorHandler errHandler;
    private Comparator comp = getDefaultComparator();

    private Vector files;
    private Vector tests;

    private Map testsInFile = new HashMap();

    private Vector errorMessages = new Vector();

    /**
     * A boolean to enable trace output while debugging test finders.
     */
    protected static boolean debug = Boolean.getBoolean("debug." + TestFinder.class.getName());
    private static final File userDir = new File(System.getProperty("user.dir"));
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(TestFinder.class);

}
