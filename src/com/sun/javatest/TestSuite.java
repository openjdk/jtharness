/*
 * $Id$
 *
 * Copyright (c) 1996, 2012, Oracle and/or its affiliates. All rights reserved.
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import com.sun.javatest.finder.BinaryTestFinder;
import com.sun.javatest.finder.HTMLTestFinder;
import com.sun.javatest.interview.LegacyParameters;
import com.sun.javatest.lib.KeywordScript;
import com.sun.javatest.logging.WorkDirLogHandler;
import com.sun.javatest.logging.ObservedFile;
import com.sun.javatest.services.ServiceManager;
import com.sun.javatest.services.ServiceReader;
import com.sun.javatest.services.PropertyServiceReader;
import com.sun.javatest.util.BackupPolicy;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.StringArray;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A class providing information about and access to the tests in a test suite.
 * The primary methods to access and run the tests are
 * <ul>
 * <li>{@link TestSuite#createTestFinder createTestFinder }
 * <li>{@link TestSuite#createTestFilter createTestFilter }
 * <li>{@link TestSuite#createScript createScript }
 * </ul>
 */
public class TestSuite
{
    /**
     * An exception used to report errors while using a TestSUite object.
     */
    public static class Fault extends Exception
    {
        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         */
        public Fault(I18NResourceBundle i18n, String s) {
            super(i18n.getString(s));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An argument to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        public Fault(I18NResourceBundle i18n, String s, Object o) {
            super(i18n.getString(s, o));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An array of arguments to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        public Fault(I18NResourceBundle i18n, String s, Object[] o) {
            super(i18n.getString(s, o));
        }
    }

    /**
     * An exception that is used to report that a given file is not a test suite.
     */
    public static class NotTestSuiteFault extends Fault
    {
        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param f The file in question, to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        public NotTestSuiteFault(I18NResourceBundle i18n, String s, File f) {
            super(i18n, s, f.getPath());
        }
    }

    public static class DuplicateLogNameFault extends Fault
    {
        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param key The internal name of the log.
         * {@link java.text.MessageFormat#format}
         */
        public DuplicateLogNameFault(I18NResourceBundle i18n, String s, String key) {
            super(i18n, s, key);
        }
    }

    public static class NoSuchLogFault extends Fault
    {
        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param key The internal name of the log.
         * {@link java.text.MessageFormat#format}
         */
        public NoSuchLogFault(I18NResourceBundle i18n, String s, String key) {
            super(i18n, s, key);
        }
    }

    /**
     * Check if a file is the root of a valid test suite. A valid test suite is identified
     * either by the root directory of the test suite, or by the file testsuite.html within
     * that directory. The directory must contain either a test suite properties file
     * (testsuite.jtt) or, for backwards compatibility, a file named testsuite.html.
     * @param root The file to be checked.
     * @return true if and only if <em>root</em> is the root of a valid test suite.
     */
    public static boolean isTestSuite(File root) {
        //System.err.println("TestSuite.isTestSuite: " + root);
        File dir;
        if (root.isDirectory())
            dir = root;
        else {
            if (root.getName().equalsIgnoreCase(TESTSUITE_HTML))
                dir = root.getParentFile();
            else
                return false;
        }
        File jtt = new File(dir, TESTSUITE_JTT);
        File parentDir = dir.getParentFile();
        File parent_jtt = (parentDir == null ? null : new File(parentDir, TESTSUITE_JTT));
        File html = new File(dir, TESTSUITE_HTML);
        return (isReadableFile(jtt)
                || isReadableFile(html) && (parent_jtt == null || !parent_jtt.exists()));
    }

    /**
     * Open a test suite.
     * @param root A file identifying the root of the test suite.
     * @return A TestSuite object for the test suite in question. The actual type of the result
     * will depend on the test suite properties found in the root directory of the test suite.
     * @throws FileNotFoundException if <em>root</em> does not exist.
     * @throws TestSuite.NotTestSuiteFault if <em>root</em> does not identify a valid test suite.
     * @throws TestSuite.Fault if any other problems occur while trying to open the test suite.
     * @see #isTestSuite
     */
    public static TestSuite open(File root) throws FileNotFoundException, Fault, NotTestSuiteFault {
        if (!root.exists())
            throw new FileNotFoundException(root.getPath());

        File canonRoot;
        try {
            canonRoot = root.getCanonicalFile();
        }
        catch (IOException e) {
            throw new Fault(i18n, "ts.cantCanonicalize",
                            new Object[] {root.getPath(), e.toString()});
        }

        File canonRootDir;
        if (canonRoot.isDirectory())
            canonRootDir = canonRoot;
        else {
            if (canonRoot.getName().equalsIgnoreCase(TESTSUITE_HTML))
                canonRootDir = canonRoot.getParentFile();
            else
                throw new NotTestSuiteFault(i18n, "ts.notTestSuiteFile", canonRoot);
        }

        File f = new File(canonRootDir, TESTSUITE_JTT);
        if (isReadableFile(f)) {
            try {
                Properties p = new Properties();
                InputStream in = new BufferedInputStream(new FileInputStream(f));
                p.load(in);
                in.close();
                return open(canonRoot, p);
            }
            catch (IOException e) {
                throw new Fault(i18n, "ts.cantReadTestSuiteFile", e.toString());
            }
        }
        else {
            // check for old style test suite
            File ts_html = new File(canonRootDir, TESTSUITE_HTML);
            File parentDir = canonRootDir.getParentFile();
            File parent_jtt = (parentDir == null ? null : new File(parentDir, TESTSUITE_JTT));
            if (isReadableFile(ts_html) && (parent_jtt == null || !parent_jtt.exists()))
                return open(canonRoot, new HashMap());
            else
                throw new NotTestSuiteFault(i18n, "ts.notTestSuiteFile", canonRoot);
        }
    }

    /**
     * Open a test suite.
     * @param root A file identifying the root of the test suite.
     * @param tsInfo Test Suite properties read from the test suite properties file.
     * @return A TestSuite object for the test suite in question.
     * @throws TestSuite.Fault if any problems occur while opening the test suite
     */
    private static TestSuite open(File root, Map tsInfo) throws Fault {
        synchronized (dirMap) {
            TestSuite ts;

            // if this test suite has already been opened, return that
            WeakReference ref = (WeakReference)(dirMap.get(root));
            if (ref != null) {
                ts = (TestSuite)(ref.get());
                if (ts != null) {
                    return ts;
                }
            }

            // otherwise, open it for real
            ts = open0(root, tsInfo);

            // save reference in case opened again
            dirMap.put(root, new WeakReference(ts));
            return ts;
        }
    }

    private static TestSuite open0(File root, Map tsInfo) throws Fault {
        String[] classPath = StringArray.split((String) (tsInfo.get("classpath")));

        ClassLoader cl;
        if (classPath.length == 0)
            cl = null;
        else {
            try {
                File rootDir = (root.isDirectory() ? root : root.getParentFile());
                URL[] p = new URL[classPath.length];
                for (int i = 0; i < classPath.length; i++) {
                    String cpi = classPath[i];
                    if (cpi.toLowerCase().startsWith("http:"))
                        p[i] = new URL(cpi);
                    else {
                        File f = new File(cpi);
                        if (!f.isAbsolute())
                            f = new File(rootDir, cpi);
                        p[i] = f.toURI().toURL();
                    }
                }
                cl = new URLClassLoader(p, TestSuite.class.getClassLoader());
            }
            catch (MalformedURLException e) {
                throw new Fault(i18n, "ts.badClassPath",
                                new Object[] {root, e.getMessage()});
            }
        }

        String[] tsClassAndArgs = StringArray.split((String) (tsInfo.get("testsuite")));

        TestSuite testSuite;
        if (tsClassAndArgs.length == 0)
            testSuite = new TestSuite(root, tsInfo, cl);
        else {
            String className = tsClassAndArgs[0];

            try {
                Class c = loadClass(className, cl);
                Class[] tsArgTypes = {File.class, Map.class, ClassLoader.class};
                Object[] tsArgs = {root, tsInfo, cl};
                testSuite = (TestSuite)(newInstance(c, tsArgTypes, tsArgs));
            }
            catch (ClassCastException e) {
                throw new Fault(i18n, "ts.notASubtype",
                                new Object[] {className, "testsuite", TestSuite.class.getName()});
            }

            String[] args = new String[tsClassAndArgs.length - 1];
            System.arraycopy(tsClassAndArgs, 1, args, 0, args.length);
            testSuite.init(args);
        }

        // initialize test finder
        testSuite.setTestFinder(testSuite.createTestFinder());

        return testSuite;
    }

    /**
     * Disposed of the shared TestSuite object for this test suite.  Use
     * the value from <code>TestSuite.getRoot()</code> as the value for
     * canonRoot.  Using this is only desired when disposal of the shared
     * TestSuite object is not desired - traditionally, it is not disposed
     * and is reused if the test suite is reopened.
     * @param canonRoot Canonical root of the test suite.
     * @see TestSuite#getRoot
     * @return The object which is about to be discarded.  Null if it was not
     *         not cached here.
     */
    /*
    public static TestSuite close(File canonRoot) {
        WeakReference ref = (WeakReference)(dirMap.remove(canonRoot));
        if (ref != null) {
            TestSuite ts = (TestSuite)(ref.get());
            if (ts != null) {
                return ts;
            }
        }

        return null;
    }
     */

    /**
     * Create a TestSuite object.
     * @param root The root file for this test suite.
     * @param tsInfo Test suite properties, typically read from the test suite properties file
     * in the root directory of the test suite.
     * @param cl A class loader to be used to load additional classes as required,
     * typically using a class path defined in the test suite properties file.
     * @throws TestSuite.Fault if a problem occurs while creating this test suite.
     */
    public TestSuite(File root, Map tsInfo, ClassLoader cl) throws Fault {
        this.root = root;
        this.tsInfo = tsInfo;
        this.loader = cl;

        String kw = (tsInfo == null ? null : (String) (tsInfo.get("keywords")));
        keywords = (kw == null ? null : StringArray.split(kw));
    }


    /**
     * Create a TestSuite object, with no additional test suite properties and no
     * class loader.
     * @param root The root file for this test suite.
     */
    public TestSuite(File root) {
        this.root = root;
    }

    /**
     * Initialize this test suite, with args typically read from a .jtt file.
     * The default implementation does not recognize any arguments and always
     * throws an exception.
     * @param args an array of strings to initialize this test suite object
     * @throws TestSuite.Fault if there are any problems initializing the
     * test suite from the specified arguments.
     */
    protected void init(String[] args) throws Fault {
        if (args.length > 0)
            throw new Fault(i18n, "ts.badArgs", args[0]);
        // should be a decodeArgs loop
    }

    /**
     * Get the path for the root file of this test suite.
     * @return the path for the root file of this test suite.
     */
    public String getPath() {
        return root.getPath();
    }

    /**
     * Get the root file of this test suite.
     * @return the root file of this test suite.
     */
    public File getRoot() {
        return root;
    }

    /**
     * Get the root directory of this test suite. If the root file is itself a directory,
     * the result will be that directory; otherwise, the result will be the parent directory
     * of the root file.
     * @return the root directory of this test suite.
     */
    public File getRootDir() {
        return (root.isDirectory() ? root : new File(root.getParent()));
    }

    /**
     * Get the directory in the test suite that contains the tests.
     * By default, the following are checked:
     * <ol>
     * <li>The <code>tests</code> property in the test suite properties file.
     * If this entry is found, it must either identify an absolute filename, or
     * a directory relative to the test suite root directory, using '/' to
     * separate the components of the path.
     * <li>If the file <em>root</em><code>/tests/testsuite.html</code> exists,
     * the result is the directory <em>root</em><code>/tests</code>. This is
     * for compatibility with standard TCK layout.
     * <li>Otherwise, the result is the root directory of the test suite.
     * </ol>
     * @return the directory that contains the tests
     */
    public File getTestsDir() {
        String t = (String) (tsInfo == null ? null : tsInfo.get("tests"));
        if (t == null || t.length() == 0) {
            File rootDir = getRootDir();
            File testsDir = new File(rootDir, "tests");
            if (testsDir.isDirectory()) {
                // if the tests directory exists, and there is no overriding
                // testsuite.jtt entry, assume the tests dir is "tests/".
                return testsDir;
            }
            // default
            return rootDir;
        }
        else {
            File f = new File(t);
            if (f.isAbsolute())
                return f;
            else
                return new File(getRootDir(), t.replace('/', File.separatorChar));
        }
    }

    /**
     * A notification method that is called when a test suite run is starting.
     * The method may be used to do any test suite specific initialization.
     * If overriding this method, be sure to call the superclass' method.  It is
     * fairly typical to register as a harness observer inside this method.  Note
     * that if an exception occurs during this method, it will be caught by the
     * harness and reported as a Harness.Observer.error().  It is recommended
     * that any implementations of this method register as an observer immediately
     * so that they can catch this error and do any cleanup to abort the
     * test suite startup sequence (check if services were started and close them
     * down, etc).
     * @param harness The harness that will be used to run the tests.
     * @throws TestSuite.Fault if an error occurred while doing test suite-specific
     * initialization that should cause the test run to be aborted.
     */
    public void starting(Harness harness) throws Fault {
        if (getServiceManager() != null) {
            serviceManager.setHarness(harness);
        }
    }

    /**
     * Create a test suite specific filter to be used to filter the tests
     * to be selected for a test run.
     * The method should return null if no test suite specific filtering is required.
     * The default is to return null.
     * @param filterEnv Configuration data that may be used by the filter.
     * @return a test suite filter, or null if no test suite specific filter is
     * required for this test suite.
     */
    public TestFilter createTestFilter(TestEnvironment filterEnv) {
        return null;
    }

    /**
     * Get a shared test finder to read the tests in this test suite.
     * @return a test finder to read the tests in this test suite
     * @see #createTestFinder
     * @see #setTestFinder
     */
    public TestFinder getTestFinder() {
        return finder;
    }

    /**
     * Set the shared test finder used to read the tests in this test suite.
     * Only one test finder may be set; attempts to change the test finder will
     * cause IllegalStateException to be thrown.
     * This method is normally called by TestSuite.open to initialize the
     * finder to the result of calling createTestFinder.
     * @param tf the test finder to be used
     * @throws IllegalStateException if the test finder has previously
     * been set to a different value
     * @see #getTestFinder
     */
    protected void setTestFinder(TestFinder tf) {
        if (tf == null)
            throw new NullPointerException();

        if (finder != null && finder != tf)
            throw new IllegalStateException();

        finder = tf;
    }

    /**
     * Create a test finder to be used to access the tests in this test suite.
     * The default implementation looks for a <code>finder</code> entry in the
     * test suite properties file, which should identify the class to be used
     * and any arguments it may require. The class will be loaded via the class
     * loader specified when the test suite was opened, if one was given;
     * otherwise, the system class loader will be used.
     *
     * The default implementation attempts to use a file <tt>testsuite.jtd</tt>
     * in the tests directory.  If found, a BinaryTestFinder will be created
     * using this file.  If it is not found, then it searches for a property
     * named <tt>finder</tt> in the test suite properties and will attempt to
     * instantiate that.  If no entry is found or it is blank, an
     * HTMLTestFinder is used, using whatever a basic settings HTMLTestFinder
     * initializes to.
     * @return a test finder to be used to read the tests in the test suite
     * @throws TestSuite.Fault if there is a problem creating the test finder
     * @see #getTestFinder
     * @see #setTestFinder
     * @see #getTestsDir
     */
    protected TestFinder createTestFinder() throws Fault {
        File testsDir = getTestsDir();

        // no BTF file; look for a finder=class args... entry
        String[] finderCmd = StringArray.split((String) (tsInfo.get("finder")));
        String finderClassName;
        String[] finderArgs = new String[0];

        if (finderCmd == null || finderCmd.length == 0) {
            //finderCmd = new String[] {HTMLTestFinder.class.getName()};
            finderCmd = null;   // ensure null for later use
            finderClassName = HTMLTestFinder.class.getName();
        }
        else {
            finderClassName = finderCmd[0];

            if (finderCmd.length > 1) {
                finderArgs = new String[finderCmd.length - 1];
                System.arraycopy(finderCmd, 1, finderArgs, 0, finderArgs.length);
            }
            else {
                // finderArgs should remain empty array
            }
        }

        // first, try looking for testsuite.jtd
        String jtd = (String) (tsInfo.get("testsuite.jtd"));
        File jtdFile = (jtd == null ? new File(testsDir, "testsuite.jtd") : new File(root, jtd));
        if (jtdFile.exists()) {
            try {
                // found a file for BinaryTestFinder
                // only pass the finder class if it was not defaulted to HTMLTestFinder
                return createBinaryTestFinder((finderCmd == null ? null : finderClassName),
                        finderArgs, testsDir, jtdFile);
            }
            catch (TestFinder.Fault e) {
                // ignore, try to continue with normal finder
            }
            catch (Fault f) {
                // ignore, try to continue with normal finder
            }
        }

        try {
            Class c = loadClass(finderClassName);
            TestFinder tf = (TestFinder) (newInstance(c));
            // called old deprecated entry till we know no-one cares
            //tf.init(finderArgs, testsRoot, null, null, tsInfo/*pass in env?*/);
            // this likely kills ExpandTestFinder, finally
            tf.init(finderArgs, testsDir, null, null, null/*pass in env?*/);
            return tf;
        }
        catch (ClassCastException e) {
            throw new Fault(i18n, "ts.notASubtype",
                            new Object[] {finderClassName, "finder", TestFinder.class.getName()});
        }
        catch (TestFinder.Fault e) {
            throw new Fault(i18n, "ts.errorInitFinder",
                            new Object[] {finderClassName, e.getMessage()});
        }
    }

    /**
     * In the case where a JTD file is found, attempt to load a binary test finder.
     * The default implementation attempts to use the finder property in the
     * test suite properties if it is a BinaryTestFinder subclass.
     *
     * @param finderClassName Finder class name to attempt to use as a BTF.  Null if
     *      the default BTF class should be used.
     * @param finderArgs Arguments to finder given from the test suite property.
     * @param testsDir Reference location to pass to finder.
     * @param jtdFile Location of the JTD file to give to the BTF.
     * @return The binary test finder which was created.
     * @throws com.sun.javatest.TestSuite.Fault
     * @throws com.sun.javatest.TestFinder.Fault
     * @see com.sun.javatest.TestFinder
     * @see com.sun.javatest.finder.BinaryTestFinder
     */
    protected TestFinder createBinaryTestFinder(String finderClassName,
            String finderArgs[], File testsDir, File jtdFile) throws Fault, TestFinder.Fault {
        try {
            TestFinder tf = null;

            if (finderClassName != null) {
                Class c = loadClass(finderClassName);
                tf = (TestFinder) (newInstance(c));
            }

            if (tf instanceof BinaryTestFinder) {
                tf.init(finderArgs, testsDir, null, null, null);
                return tf;
            }
            else {
                return new BinaryTestFinder(testsDir, jtdFile);
            }
        }
        catch (ClassCastException e) {
            throw new Fault(i18n, "ts.notASubtype",
                            new Object[] {finderClassName, "finder", TestFinder.class.getName()});
        }
        catch (TestFinder.Fault e) {
            throw new Fault(i18n, "ts.errorInitFinder",
                            new Object[] {finderClassName, e.getMessage()});
        }

    }

    /**
     * Create and initialize a TestRunner that can be used to run
     * a series of tests.
     * The default implementation returns a TestRunner that
     * creates a number of test execution threads which each
     * create and run a script for each test obtained from
     * the test runners iterator.
     * @return a TestRunner that can be used to run a series of tests
     */
    public TestRunner createTestRunner() {
        return new DefaultTestRunner();
    }

    /**
     * Create and initialize a Script that can be used to run a test.
     * The default implementation looks for a <code>script</code> entry in the configuration
     * data provided, and if not found, looks for a <code>script</code> entry in the
     * test suite properties. The script entry should define the script class
     * to use and any arguments it may require. The class will be loaded via the class
     * loader specified when the test suite was opened, if one was given;
     * otherwise, the system class loader will be used. Individual test suites will
     * typically use a more direct means to create an appropriate script object.
     * The parameters for this method are normally passed through to the script
     * that is created.
     *
     * Note that the name of this method is "create", it is not recommended
     * that the value returned ever be re-used or cached for subsequent requests
     * to this method.
     * @param td The test description for the test to be executed.
     * @param exclTestCases Any test cases within the test that should not be executed.
     * @param scriptEnv Configuration data to be given to the test as necessary.
     * @param workDir A work directory in which to store the results of the test.
     * @param backupPolicy A policy object used to control how to backup any files that
     * might be overwritten.
     * @return a script to be used to execute the given test
     * @throws TestSuite.Fault if any errors occur while creating the script
     */
    public Script createScript(TestDescription td, String[] exclTestCases, TestEnvironment scriptEnv,
                               WorkDirectory workDir,
                               BackupPolicy backupPolicy) throws Fault {
        if (scriptClass == null) {
            String[] script = envLookup(scriptEnv, "script");
            if (script.length == 0)
                script = StringArray.split((String) tsInfo.get("script"));
            if (script.length > 0) {
                scriptClass = loadClass(script[0]);
                if (!Script.class.isAssignableFrom(scriptClass)) {
                    throw new Fault(i18n, "ts.notASubtype",
                                    new Object[] {script[0], "script", Script.class.getName()});
                }
                scriptArgs = new String[script.length - 1];
                System.arraycopy(script, 1, scriptArgs, 0, scriptArgs.length);
            }
            else {
                // for backwards compatibility,
                // see if KeywordScript is a reasonable default
                boolean keywordScriptOK = false;
                for (Iterator i = scriptEnv.keys().iterator(); i.hasNext() && !keywordScriptOK; ) {
                    String key = (String)(i.next());
                    keywordScriptOK = key.startsWith("script.");
                }
                if (keywordScriptOK) {
                    scriptClass = KeywordScript.class;
                    scriptArgs = new String[] { };
                }
                else {
                    throw new Fault(i18n, "ts.noScript");
                }
            }
        }

        Script s = (Script)(newInstance(scriptClass));
        s.initArgs(scriptArgs);
        s.initTestDescription(td);
        s.initExcludedTestCases(exclTestCases);
        s.initTestEnvironment(scriptEnv);
        s.initWorkDir(workDir);
        s.initBackupPolicy(backupPolicy);
        s.initClassLoader(loader);
        return s;
    }

    /**
     * Create a configuration interview that can be used to collection the configuration
     * data for a test run.
     * <p>The default implementation returns a {@link LegacyParameters default}
     * interview suitable for use with test suites built with earlier versions
     * of the JT Harness: it provides questions equivalent to the fields in
     * the GUI Parameter Editor or command-line -params option. As such, much of the
     * necessary configuration data is provided indirectly via environment (.jte) files
     * which must be created and updated separately.
     * <p>Individual test suites should provide their own interview, with questions
     * customized to the configuration data they require.
     *
     * Note that the name of this method is "create", the harness may instantiate
     * multiple copies for temporary use, resetting data or transferring data.
     * Do not override this method with an implementation which caches the
     * return value.
     * @return A configuration interview to collect the configuration data for a test run.
     * @throws TestSuite.Fault if a problem occurs while creating the interview
     */
    public InterviewParameters createInterview()
        throws Fault
    {
        String[] classNameAndArgs = StringArray.split((String) (tsInfo.get("interview")));
        if (classNameAndArgs == null || classNameAndArgs.length == 0) {
            try {
                return new LegacyParameters(this);
            }
            catch (InterviewParameters.Fault e) {
                throw new Fault(i18n, "ts.errorInitDefaultInterview",
                                e.getMessage());
            }
        }


        String className = classNameAndArgs[0];
        String[] args = new String[classNameAndArgs.length - 1];
        System.arraycopy(classNameAndArgs, 1, args, 0, args.length);

        try {
            Class c = loadClass(className);
            InterviewParameters p = (InterviewParameters) (newInstance(c));
            p.init(args);
            p.setTestSuite(this);
            return p;
        }
        catch (ClassCastException e) {
            throw new Fault(i18n, "ts.notASubtype",
                            new Object[] {className, "interview", InterviewParameters.class.getName()});
        }
        catch (InterviewParameters.Fault e) {
            //e.printStackTrace();
            throw new Fault(i18n, "ts.errorInitInterview",
                            new Object[] {className, e.getMessage()});
        }

    }

    /**
     * Create a configuration interview based on specified map of template values
     * @return A configuration interview to collect the configuration data for a test run.
     * @throws TestSuite.Fault if a problem occurs while creating the interview
     */
    public InterviewParameters loadInterviewFromTemplate(Properties templateInfo, InterviewParameters newInterview)
        throws Fault
    {
        newInterview.storeTemplateProperties(templateInfo);
        newInterview.propagateTemplateForAll();
        return newInterview;
    }

    /**
     * Create a configuration interview based on specified template file
     * @return A configuration interview to collect the configuration data for a test run.
     *         null if specified file is not template
     * @throws TestSuite.Fault if a problem occurs while creating the interview
     *         IOException if a problem occurs while reading a template file
     */
    public InterviewParameters loadInterviewFromTemplate(File template,
                                                         InterviewParameters ip)
        throws Fault, IOException
    {
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(template));
            Properties data = new Properties();
            data.load(in);
            String tm = (String) data.get(InterviewParameters.IS_TEMPLATE);
            if (InterviewParameters.TRUE.equals(tm)) {
                data.put(InterviewParameters.TEMPLATE_PATH,
                         template.getAbsolutePath());
                ip.setTemplatePath(template.getAbsolutePath());
                return loadInterviewFromTemplate(data, ip);
            } else {
                // XXX should probably return ip
                //     or throw Fault
                return null;
            }
        } finally {
            if (in != null) in.close();
        }
    }


    /**
     * Get a string containing a unique ID identifying this test suite,
     * or null if not available.  The default is taken from the "id" entry
     * in the .jtt file.
     * @return a unique ID identifying the test suite, or null if not specified.
     * @see #getName
     */
    public String getID() {
        return (tsInfo == null ? null : (String) (tsInfo.get("id")));
    }

    /**
     * Get a string identifying this test suite, or null if not available.
     * The default is taken from the "name" entry in the .jtt file.
     * This string is for presentation to the user, and may be localized
     * if appropriate.
     * @return a string identifying the test suite, or null if not specified.
     * @see #getID
     */
    public String getName() {
        return (tsInfo == null ? null : (String) (tsInfo.get("name")));
    }

    /**
     * Get the estimated number of tests in the test suite.
     * The default is to use the value of the "testCount" property from the
     * testsuite.jtt file.
     *
     * @return The estimated number of tests, or -1 if this number is not available.
     */
    public int getEstimatedTestCount() {
        try {
            if (tsInfo != null) {
                String s = (String) (tsInfo.get("testCount"));
                if (s != null)
                    return Integer.parseInt(s);
            }
        }
        catch (NumberFormatException e) {
            // ignore
        }
        return -1; // unknown
    }

    /**
     * Get the file name of the initial exclude list associated with the test suite.
     * The default is to use the value of the "initial.jtx" property from the
     * testsuite.jtt file. If the value is a relative filename, it will be made absolute
     * by evaluating it relative to the test suite root directory.
     * @return the name of the default exclude list, or null if none specified.
     */
    public File getInitialExcludeList() {
        String s = (tsInfo == null ? null : (String) (tsInfo.get("initial.jtx")));
        if (s == null)
            return null;

        File f = new File(s.replace('/', File.separatorChar));
        if (!f.isAbsolute())
            f = new File(getRootDir(), f.getPath());
        return f;
    }

    /**
     * Check if the test suite has an initial exclude list.
     * The default is to use getInitialExcludeList, and if that returns
     * a non-null result, check whether that file exists or not.
     * @return true if the test suite has an initial exclude list,
     * and false otherwise
     */
    public boolean hasInitialExcludeList() {
        File f = getInitialExcludeList();
        return (f == null ? false : f.exists());
    }

    /**
     * Get the URL for the latest exclude list associated with the test suite.
     * The default is to use the value of the "latest.jtx" property from the
     * testsuite.jtt file., which (if present) must be a fully qualified URL
     * identifying the latest exclude list for this test suite.
     * @return the name of the latest exclude list, or null if none specified.
     */
    public URL getLatestExcludeList() {
        try {
            String s = (tsInfo == null ? null : (String) (tsInfo.get("latest.jtx")));
            return (s == null ? null : new URL(s));
        }
        catch (MalformedURLException e) {
            // ignore
            return null;
        }
    }

    /**
     * Check if the test suite has a latest exclude list.
     * The default is to use getLatestExcludeList, and to
     * check whether that return a non-null result. The URL is not
     * itself checked for validity.
     * @return true if the test suite has a latest exclude list,
     * and false otherwise
     */
    public boolean hasLatestExcludeList() {
        URL u = getLatestExcludeList();
        return (u != null);
    }

    /**
     * Get the names of any helpsets containing related documents for this
     * test suite. The names should identify JavaHelp helpset files, as
     * used by javax.help.HelpSet.findHelpSet(ClassLoader, String).
     * Thus the names should identify resources of helpsets on the classpath.
     * This means you will typically need to put the directory or jar file
     * containing the help set on the classpath as well.
     * By default, the names will be looked up under the name "additionalDocs"
     * in the testsuite.jtt file.
     * @return an array of names identifying helpsets that contain related
     * documents for this testsuite. The result may be null if there are no
     * such documents.
     */
    public String[] getAdditionalDocNames() {
        return (tsInfo == null
                ? null
                : StringArray.split((String) (tsInfo.get("additionalDocs"))));
    }

    /**
     * Get the set of valid keywords for this test suite.
     * By default, the keywords will be looked up under the name "keywords"
     * in the testsuite.jtt file.
     * @return the set of valid keywords for this test suite, or null
     * if not known.
     */
    public String[] getKeywords() {
        return keywords;
    }

    /**
     * Get a list of associated files for a specified test description.
     * Normally, this will include the file containing the test description,
     * and any source files used by the test.  By default, the source files
     * are determined from the test description's "source" entry.
     * @see TestDescription#getSourceURLs()
     * @param td The test description for which the associated files are required
     * @return a list of associated files for this test description
     */
    public URL[] getFilesForTest(TestDescription td) {
        return td.getSourceURLs();
    }

    /**
     * This method should be overridden in subclasses
     * @param path String, which determines path to currently selected test's folder.
     * This is root relative path. This shouldn't be null, for the
     * root folder use "".
     * @return array of files with documentation for test's folder, determined by path.
     * null means there no documentation for this folder
     */
    public URL[] getDocsForFolder(String path) {
        return null;
    }

    /**
     * This method should be overridden in subclasses
     * @param td TestDescription for currently selected test case. This shouldn't be null.
     * @return array of files with documentation for test case, determined td.
     * null means there no documentation for this test case
     */
    public URL[] getDocsForTest(TestDescription td) {
        return null;
    }

    /**
     * Get A URL identifying a logo for this test suite, or null if none available.
     * @return a URL for a logo for the testsuite, or null if not available
     */
    public URL getLogo() {
        try {
            String s = (tsInfo == null ? null : (String) (tsInfo.get("logo")));
            return (s == null ? null : new URL(getRootDir().toURL(), s));
        }
        catch (MalformedURLException e) {
            // ignore
            return null;
        }
    }

    private static String[] envLookup(TestEnvironment env, String name) throws Fault {
        try {
            return env.lookup(name);
        }
        catch (TestEnvironment.Fault e) {
            throw new Fault(i18n, "ts.cantFindNameInEnv",
                            new Object[] {name, e.getMessage()});
        }
    }

    /**
     * Create a new instance of a class, translating any exceptions that may arise
     * into Fault.
     * @param c the class to be instantiated
     * @return an instance of the specified class
     * @throws TestSuite.Fault if any errors arise while trying to instantiate
     * the class.
     */
    protected static Object newInstance(Class c) throws Fault {
        try {
            return c.newInstance();
        }
        catch (InstantiationException e) {
            throw new Fault(i18n, "ts.cantInstantiate",
                            new Object[] { c.getName(), e });
        }
        catch (IllegalAccessException e) {
            throw new Fault(i18n, "ts.illegalAccess",
                            new Object[] { c.getName(), e });
        }
    }


    /**
     * Create a new instance of a class using a non-default constructor,
     * translating any exceptions that may arise into Fault.
     * @param c the class to be instantiated
     * @param argTypes the types of the argument to be passed to the constructor,
     * (thus implying the constructor to be used.)
     * @param args the arguments to be passed to the constructor
     * @return an instance of the specified class
     * @throws TestSuite.Fault if any errors arise while trying to instantiate
     * the class.
     */
    protected static Object newInstance(Class c, Class[] argTypes, Object[] args)
        throws Fault
    {
        try {
            return c.getConstructor(argTypes).newInstance(args);
        }
        catch (IllegalAccessException e) {
            throw new Fault(i18n, "ts.illegalAccess",
                            new Object[] { c.getName(), e });
        }
        catch (InstantiationException e) {
            throw new Fault(i18n, "ts.cantInstantiate",
                            new Object[] { c.getName(), e });
        }
        catch (InvocationTargetException e) {
            Throwable te = e.getTargetException();
            if (te instanceof Fault)
                throw (Fault) te;
            else
                throw new Fault(i18n, "ts.cantInit", new Object[] { c.getName(), te });
        }
        catch (NoSuchMethodException e) {
            // don't recurse past the use of a single arg constructor
            if (argTypes.length > 1 && Boolean.getBoolean(FIND_LEGACY_CONSTRUCTOR)) {
                return newInstance(c, new Class[] {File.class}, new Object[] {args[0]});
            }

            throw new Fault(i18n, "ts.cantFindConstructor",
                            new Object[] { c.getName(), e });
        }
    }

    /**
     * Load a class using the class loader provided when this test suite was created.
     * @param className the name of the class to be loaded
     * @return the class that was loaded
     * @throws TestSuite.Fault if there was a problem loading the specified class
     */
    public Class loadClass(String className) throws Fault {
        return loadClass(className, loader);
    }

    /**
     * Load a class using a specified loader, translating any errors that may arise
     * into Fault.
     * @param className the name of the class to be loaded
     * @param cl the class loader to use to load the specified class
     * @return the class that was loaded
     * @throws TestSuite.Fault if there was a problem loading the specified class
     */
    protected static Class loadClass(String className, ClassLoader cl) throws Fault {
        try {
            if (cl == null)
                return Class.forName(className);
            else
                return cl.loadClass(className);
        }
        catch (ClassNotFoundException e) {
            throw new Fault(i18n, "ts.classNotFound",
                            new Object[] { className, e });
        }
        catch (IllegalArgumentException e) {
            throw new Fault(i18n, "ts.badClassName",
                            new Object[] { className });
        }
    }

    /**
     * Get the class loader specified when this test suite object was created.
     * @return the class loader specified when this test suite object was created
     */
    public ClassLoader getClassLoader() {
        return loader;
    }

    public ServiceManager getServiceManager() {
        if (!needServices()) {
            return null;
        }

        if (serviceManager == null) {
            serviceManager = new ServiceManager(this);
        }

        return serviceManager;
    }

    /**
     * Checks if serviceReader is active and file with service description does
     * exist.
     * @return true, if it's needed to start services, false otherwise.
     */
    public boolean needServices() {
        ServiceReader sr = getServiceReader();
        if (sr == null) {
            return false;
        }

        /*
         * Since jt4.5 the ServiceReader has been extended with a new method.
         * To preserve ability to use new javatest with old test suites
         * the extra check is performed: check if the newly introduced method
         * is abstract or not.
         */
        boolean isLegacy = false;
        try {
            Method m = sr.getClass().getMethod("getServiceDescriptorFileName", new Class[0]);
            if (Modifier.isAbstract(m.getModifiers())) {
                isLegacy = true;
            }
        } catch (NoSuchMethodException e) {
             isLegacy = true;
        }
        File descrFile = isLegacy ?
            new File(getRootDir(), File.separator + "lib" + File.separator + "services.xml") :
            new File(getRootDir(), sr.getServiceDescriptorFileName());

        return descrFile.exists();
    }
    /**
     * Returns a test suite specific ServiceReader, used to read Service
     * definitions.
     *
     * @return ServiceReader instance. Default is PropertyServiceReader
     */
    public ServiceReader getServiceReader() {
        if (serviceReader != null) {
            return serviceReader;
        }

        String servInfo = (String)tsInfo.get("serviceReader");
        if (servInfo != null) {
            String[] args = servInfo.split(" ");
            try {
                Class c = loadClass(args[0]);
                serviceReader = (ServiceReader) (newInstance(c));
                if (args.length > 1) {
                    // problem with java1.5, which has no Arrays.copyOfRange();
                    String[] copy = new String[args.length - 1];
                    for (int i = 1; i < args.length; i++) {
                        copy[i-1] = args[i];
                    }

                    serviceReader.init(this, copy);
                }
                else {
                    serviceReader.init(this, null);
                }
            }
            catch (TestSuite.Fault e) {
            }
        }
        else {
            serviceReader = new PropertyServiceReader();
            serviceReader.init(this, null);
        }

        return serviceReader;
    }

    /**
     * Get a map containing the test suite data in the .jtt file.
     * @return a map containing the test suite data in the .jtt file
     */
    protected Map getTestSuiteInfo() {
        return tsInfo;
    }

    /**
     * Get an entry from the data in the .jtt file.
     * @param name The name of the entry to get from the info in the .jtt file
     * @return the value of the specified entry, or null if not found.
     */
    public String getTestSuiteInfo(String name) {
        if (tsInfo == null)
            return null;
        else
            return (String) (tsInfo.get(name));
    }

    /**
     * Get the requested behavior for dealing with conflicts between
     * which tests are in the test suite vs those in the work directory.
     * @see #DELETE_NONTEST_RESULTS
     * @see #REFRESH_ON_RUN
     * @see #CLEAR_CHANGED_TEST
     */
    public boolean getTestRefreshBehavior(int event) {
        switch (event) {
        case DELETE_NONTEST_RESULTS:
            return Boolean.valueOf(getTestSuiteInfo("deleteNonExistTests")).booleanValue();
        case REFRESH_ON_RUN:
            return Boolean.valueOf( getTestSuiteInfo("refreshTestsOnRun") ).booleanValue();
        case CLEAR_CHANGED_TEST:
            return Boolean.valueOf( getTestSuiteInfo("clearChangedTests")).booleanValue();
        default:
            return false;
        }
    }


    /**
     * Returns notification logger associated with
     * given working directory or common logger if null was specified
     * @param wd - working directory or null
     */
    public Logger getNotificationLog(WorkDirectory wd) {
        return notifLogger;
    }

    public ObservedFile getObservedFile(WorkDirectory wd) {
        return getObservedFile(wd.getLogFileName());
    }


    public ObservedFile getObservedFile(String path) {
        String cPath = new File(path).getAbsolutePath();
        if (observedFiles.containsKey(cPath)) {
            return (ObservedFile) observedFiles.get(cPath);
        }
        return null;
    }

    void setLogFilePath(WorkDirectory wd) {
        ObservedFile f = new ObservedFile(wd.getLogFileName());
        if (f.length() != 0) {
            f.backup();
        }
        // return to current
        f = new ObservedFile(wd.getLogFileName());

        if(observedFiles == null) {
            observedFiles = new HashMap();
        }
        if (!observedFiles.containsKey(f.getAbsolutePath())) {
            observedFiles.put(f.getAbsolutePath(), f);
        }

    }


    /**
     * Creates general purpose logger with given key and ResourceBundleName registered for given WorkDirectory.
     * @param wd WorkDirectory logger should be registered for; may be <code>null</code> if no WorkDirectory
     * currently available (the log will be registered for the first WD created for this TestSuite
     * @param b name of ResorceBundle used for this logger; may be <code>null</code> if not required
     * @param key key for this log
     * @return general purpose logger with given key registered for given WorkDirectory or TestSuite (if WD is null)
     * @throws TestSuite.DuplicateLogNameFault if log with this key has been registered in the system already
     * @see #getLog
     */

    public Logger createLog(WorkDirectory wd, String b, String key) throws DuplicateLogNameFault {

        if (key == null || "".equals(key)) {
            throw new IllegalArgumentException("Log name can not be empty");
        }

        String logName = wd.getLogFileName();

        if (gpls == null)
            gpls = new Vector<GeneralPurposeLogger>();

        for (int i = 0; i < gpls.size(); i++)  {
            GeneralPurposeLogger gpl = gpls.get(i);
            if (gpl.getName().equals(key) && gpl.getLogFileName().equals(logName))
                throw new DuplicateLogNameFault(i18n, "ts.logger.duplicatelognamefault", key);
        }

        GeneralPurposeLogger gpl = new GeneralPurposeLogger( key, wd, b, this);
        gpls.add(gpl);
        return gpl;
    }

    /**
     * Returns general purpose logger with given key registered for given WorkDirectory.
     * The log should be created first.
     * @param wd WorkDirectory desired logger is registered for
     * @param key key for this log
     * @return general purpose logger with given key registered for given WorkDirectory
     * @throws TestSuite.NoSuchLogFault if desired log not registered in the system
     * @throws NullPointerException if <code>wd</code> is null
     * @see #createLog
     */
    public Logger getLog(WorkDirectory wd, String key) throws NoSuchLogFault {
        if (gpls == null)
            throw new NoSuchLogFault(i18n, "ts.logger.nologscreated", key);

        if (wd == null)
            throw new NullPointerException(i18n.getString("ts.logger.nullwd"));

        String logFile = wd.getLogFileName();

        for (int i = 0; i < gpls.size(); i++) {
            GeneralPurposeLogger logger = gpls.get(i);
            if (logger.getLogFileName().equals(logFile) && logger.getName().equals(key))
                return logger;
        }
        throw new NoSuchLogFault(i18n, "ts.logger.nosuchlogfault", key);
    }

    /**
     * Cleans the log file in given WorkDirectory
     * @param wd WorkDirectory desired logger is registered for
     * @throws IOException if log file's content can't be erased
     */
    public void eraseLog(WorkDirectory wd) throws IOException {
        if (wd == null)
            throw new NullPointerException(i18n.getString("ts.logger.nullwd"));

        if (gpls != null)
            for (int i = 0; i < gpls.size(); i++) {
                GeneralPurposeLogger gpl = gpls.get(i);
                if (gpl.getLogFileName().equals(wd.getLogFileName())) {
                    Handler[] h = gpl.getHandlers();
                    if (h[0] instanceof WorkDirLogHandler) {
                        ((WorkDirLogHandler)h[0]).eraseLogFile();
                        return;
                    }
                }
            }
    }

    private static boolean isReadableFile(File f) {
        return (f.exists() && f.isFile() && f.canRead());
    }

    /**
     * Should tests which no longer exist in the test suite be
     * deleted from a work directory when it is opened?
     */
    public static final int DELETE_NONTEST_RESULTS = 0;

    /*
     * Should the content of the test suite be refreshed as the
     * tests run?  So the test description should be updated from the
     * finder just before the test runs.
     */
    public static final int REFRESH_ON_RUN = 1;

    /**
     * Should a test be reset to not run if it is found that the
     * test has changed in the test suite (test description does
     * not match the one in the existing result).
     */
    public static final int CLEAR_CHANGED_TEST = 2;

    private static class NotificationLogger extends Logger {
        private NotificationLogger(String resourceBundleName) {
            super(notificationLogName, resourceBundleName);
            setLevel(Level.CONFIG);
            // needs to be reimplemented - this initializes Swing, which is not
            // allowed inside the core harness
            // should be implemented so that the GUI attaches to the logging system
            // on startup
            //addHandler(new ErrorDialogHandler());
        }

        public synchronized void log(LogRecord record) {
            record.setLoggerName(this.getName());
            if (record.getThrown() != null) {
                record.setLevel(Level.INFO);
            }
            super.log(record);
        }


        // overwrite to make sure exception is handled
        public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
            LogRecord lr = new LogRecord(Level.INFO, "THROW");
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            lr.setThrown(thrown);
            log(lr);
        }

    }

    private static class GeneralPurposeLogger extends Logger {
        private GeneralPurposeLogger(String name, WorkDirectory wd, String resourceBundleName, TestSuite ts) {
            super(name, resourceBundleName);
            this.logFileName = wd.getLogFileName();

            if (wd != null) {
                if (!handlersMap.containsKey(wd.getLogFileName())) {
                    WorkDirLogHandler wdlh = new WorkDirLogHandler(ts.getObservedFile(wd));
                    handlersMap.put(wd.getLogFileName(), wdlh);
                }

                addHandler((WorkDirLogHandler)handlersMap.get(wd.getLogFileName()));
            }
            setLevel(Level.ALL);
        }

        public void log(LogRecord record) {
            Handler targets[] = getHandlers();
            if (targets != null) {
                for (int i = 0; i < targets.length; i++) {
                    if (targets[i] instanceof WorkDirLogHandler) {
                        ((WorkDirLogHandler)targets[i]).publish(record, getName());
                    } else {
                        targets[i].publish(record);
                    }
                }
            }
        }

        private String getLogFileName() {
            return logFileName;
        }

        private String logFileName;
    }


    private static final String TESTSUITE_HTML = "testsuite.html";
    private static final String TESTSUITE_JTT  = "testsuite.jtt";
    private static final String FIND_LEGACY_CONSTRUCTOR = "com.sun.javatest.ts.findLegacyCtor";

    private File root;
    private Map tsInfo;
    private ClassLoader loader;
    private TestFinder finder;

    // the following are used by the default impl of createScript
    private Class scriptClass;
    private String[] scriptArgs;

    private String[] keywords;

    private ServiceReader serviceReader;
    private ServiceManager serviceManager;

    private static HashMap dirMap = new HashMap(2);

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(TestSuite.class);
    private static String notificationLogName = i18n.getString("notification.logname");

    static HashMap handlersMap = new HashMap();
    private static Vector<GeneralPurposeLogger> gpls;
    private static HashMap observedFiles;

    private final NotificationLogger notifLogger = new NotificationLogger(null);

    public static final String TM_CONTEXT_NAME = "tmcontext";

}
