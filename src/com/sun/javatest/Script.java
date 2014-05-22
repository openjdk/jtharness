/*
 * $Id$
 *
 * Copyright (c) 1996, 2014, Oracle and/or its affiliates. All rights reserved.
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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.util.Date;
import java.util.Vector;

import com.sun.javatest.util.BackupPolicy;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.StringArray;
import com.sun.javatest.util.Timer;

/**
 * Script is the abstract base class providing the ability to control
 * how a test is to be compiled and executed. In addition to the primary method,
 * <em>run</em>, it has many methods that can be used by subtype classes
 * to assist them in performing a test.
 */
public abstract class Script
{
    /**
     * Initialize any custom args for the script.
     * @param args custom args for the script
     */
    public void initArgs(String[] args) {
        scriptArgs = args;
    }

    /**
     * Initialize the test description to be run by the script.
     * In addition, a mutable test result is set up, in which the results of running
     * the test can be recorded by the script.
     * @param td the test description for the test to be run
     */
    public void initTestDescription(TestDescription td) {
        this.td = td;
        testResult = new TestResult(td);
        trOut = testResult.getTestCommentWriter();
    }

    /**
     * Initialize the list of test cases to be excluded from the test.
     * The script is responsible for determining how to instruct the test
     * not to run these test cases. A recommended convention is to pass the
     * list of test cases to the test using a -exclude option.
     * @param excludedTestCases a list of test cases within the test that
     * should not be run
     */
    public void initExcludedTestCases(String[] excludedTestCases) {
        this.excludedTestCases = excludedTestCases;
    }

    /**
     * Initialize the environment to be used when running the test.
     * @param env the environment to be used when running the test
     */
    public void initTestEnvironment(TestEnvironment env) {
        this.env = env;
    }

    /**
     * Initialize the work directory to be used to store the results
     * obtained when running the test,
     * and to store any temporary files that may be required by the test.
     * @param workDir the work directory to be used to store the test's results.
     */
    public void initWorkDir(WorkDirectory workDir) {
        this.workDir = workDir;
    }

    /**
     * Initialize the backup policy to be used when creating a test result
     * file in which to store the results of running this test.
     * @param backupPolicy A backup policy object to be used when
     * creating test result files.
     */
    public void initBackupPolicy(BackupPolicy backupPolicy) {
        this.backupPolicy = backupPolicy;
    }

    /**
     * Initialize the class loader for any commands to be loaded.
     * @param loader a class loader to be used to load any commands or other
     * user-specified classes that may be required.
     */
    public void initClassLoader(ClassLoader loader) {
        this.loader = loader;
    }

    /**
     * Initialize a delegate script object. This should only be used in
     * exceptional circumstances, and is mostly provided for historical purposes.
     * @param s The delegate to be initialized
     * @param scriptArgs the arguments to be passed to the delegate object
     */
    protected void initDelegate(Script s, String[] scriptArgs) {
        s.scriptArgs = scriptArgs;
        // copy rest of values across from self
        s.td = td;
        s.env = env;
        s.workDir = workDir;
        s.backupPolicy = backupPolicy;
        s.loader = loader;
        s.testResult = testResult;
        s.trOut = trOut;
        s.jtrIfPassed = jtrIfPassed;
    }

    /**
     * Initialize the test result for the result of the script execution.
     * Normally, a test result is initialized as a side effect of calling
     * initTestDescription. This method should only be called is special
     * circumstances, and is mostly provided for historical purposes.
     * @param tr The test result to set as the result of the script's execution.
     * @throws IllegalStateException if the test result has already been set.
     * @see #initTestDescription
     */
    protected void initTestResult(TestResult tr) {
        if (testResult != null)
            throw new IllegalStateException();

        testResult = tr;
    }

    /**
     * Run the script, to fill out the test results for the test description
     * given to <code>init</code>. Most implementations will use the default
     * implementation of this method, which delegates to a simpler (abstract)
     * method @link(run(String[],TestDescription, TestEnvironment)).  If you
     * override this method, be aware that this method does insert many of the
     * standard result properties into the TestResult object - harness info,
     * start stop times, etc.
     */
    public void run() {
        if (workDir == null)
            throw new NullPointerException(i18n.getString("script.noWorkDir"));
        if (td == null)
            throw new NullPointerException(i18n.getString("script.noTestDesc"));
        if (testResult == null)
            throw new NullPointerException(i18n.getString("script.noTestRslt"));
        if (env == null)
            throw new NullPointerException(i18n.getString("script.noTestEnv"));

        Status execStatus = null;

        // "work" has the the work dir for the suite
        // "testWork" has the work dir for this test
        File testWork = workDir.getFile(td.getRootRelativeDir().getPath());

        // synchronize against interference by other scripts
        synchronized (Script.class) {
            if (!testWork.exists()) {
                testWork.mkdirs();
            }
        }

        long startMs = System.currentTimeMillis();

        String descUrl = td.getFile().toURI().toASCIIString();
        String id = td.getId();
        if (id != null)
            descUrl += "#" + id;
        testResult.putProperty(TestResult.DESCRIPTION, descUrl);
        testResult.putProperty(TestResult.START, testResult.formatDate(new Date()));
        testResult.putProperty(TestResult.VERSION, ProductInfo.getVersion());
        testResult.putProperty(TestResult.WORK, testWork.getAbsolutePath());
        testResult.putProperty(TestResult.ENVIRONMENT, env.getName());
        testResult.putProperty(TestResult.VARIETY, ProductInfo.getHarnessVariety());
        testResult.putProperty(TestResult.LOADER, ProductInfo.getPackagingType());

        if (osInfo == null) {
            String osArch = System.getProperty("os.arch");
            String osName = System.getProperty("os.name");
            String osVersion = System.getProperty("os.version");
            osInfo = osName + " " + osVersion + " (" + osArch + ")";
        }
        testResult.putProperty(TestResult.JAVATEST_OS, osInfo);
        if (excludedTestCases != null)
            testResult.putProperty("excludedTestCases", StringArray.join(excludedTestCases));

        String classDir = td.getParameter("classDir");
        File f = (classDir == null ? workDir.getFile(defaultClassDir) :
                 new File(testWork, classDir));
        env.putUrlAndFile("testClassDir", f);
        env.putUrlAndFile("testWorkDir", testWork);
        env.put("test", td.getFile().getPath());
        env.put("testDir", td.getFile().getParent());
        env.put("testURL", descUrl);
        env.put("testPath", td.getRootRelativeURL());

        int timeout = getTestTimeout();
        PrintStream out = System.out;
        PrintStream err = System.err;

        try {
            testResult.putProperty(TestResult.TEST, td.getRootRelativeURL());
            StringBuilder sb = new StringBuilder(this.getClass().getName());
            String args = StringArray.join(scriptArgs);
            if (args != null && args.length() > 0) {
                sb.append(" ");
                sb.append(args);
            }
            testResult.putProperty(TestResult.SCRIPT, sb.toString());

            if (timeout > 0) {
                testResult.putProperty("timeoutSeconds", Integer.toString(timeout));
                setAlarm(timeout*1000);
            }

            execStatus = run(scriptArgs, td, env);
        }
        finally {
            if (timeout > 0)
                setAlarm(0);

            try {
                System.setOut(System.out);
                System.setErr(System.err);
            }
            catch (SecurityException ignore) {
            }

            if (Thread.interrupted()) // will clear interrupted status of thread, as desired
                execStatus = Status.error(i18n.getString("script.interrupted"));

            testResult.putProperty(TestResult.END,
                    testResult.formatDate(new Date()));

            if (execStatus == null) {
                execStatus = Status.error(i18n.getString("script.noStatus"));
            }
            else {
                switch (execStatus.getType()) {
                case Status.PASSED:
                case Status.FAILED:
                case Status.ERROR:
                    break;
                default:
                    execStatus = Status.error(i18n.getString("script.badTestStatus", execStatus));
                }
            }

        }

        testResult.setEnvironment(env);
        testResult.putProperty("totalTime", Long.toString(System.currentTimeMillis() - startMs));
        testResult.setStatus(execStatus);

        try {
            if (execStatus.getType() != Status.PASSED || jtrIfPassed)
                testResult.writeResults(workDir, backupPolicy);
        }
        catch (IOException e) {
            // ignore it; the test will have an error status already
            //throw new JavaTestError("Unable to write result file! " + e);
        }
    }

    /**
     * The primary method to be provided by Scripts. It is responsible for compiling
     * and executing the test appropiately.  Normally, a script should call `init' and
     * then decode any script-specific options it is given in `args'. It should then
     * examine the test description it is given so that it can compile and execute
     * the test as appropriate. Various convenience routines are provided to
     * simplify the task of running the compiler, an interpreter or any other commands,
     * which can be specified in a flexible manner by properties in the TestEnvironment.
     *
     * @param args      Any script-specific options specified in the script property
     * @param td        The test description for the test to be performed
     * @param env       The test environment giving the details of how to run the test
     * @return          The result of running the script
     * @see #compileIndividually
     * @see #compileTogether
     * @see #execute
     * @see #invokeCommand
     */
    public abstract Status run(String[] args, TestDescription td, TestEnvironment env);

    /**
     * Get the test description for the test which this script will run.
     * @return the test description for the test which this script will run.
     */
    public TestDescription getTestDescription() {
        return td;
    }

    /**
     * Get the test result object to be used for the results of the test run.
     * @return the test result object to be used for the results of the test run.
     */
    public TestResult getTestResult() {
        return testResult;
    }

    /**
     * Get the flag that indicates whether a result (.jtr) file should be written
     * even if the test has passed. By default, this is true.
     * @return the flag that indicates whether a result (.jtr) file should be written
     * even if the test has passed.
     * @see #setJTRIfPassed
     */
    public boolean getJTRIfPassed() {
        return jtrIfPassed;
    }

    /**
     * Set the flag that indicates whether a result (.jtr) file should be written
     * even if the test has passed. By default, this is true.
     * @param b the flag that indicates whether a result (.jtr) file should be written
     * even if the test has passed.
     * @see #getJTRIfPassed
     */
    public void setJTRIfPassed(boolean b) {
        jtrIfPassed = b;
    }

    /**
     * Set an alarm that will interrupt the calling thread after
     * a specified delay (in milliseconds), and repeatedly thereafter
     * until cancelled.
     * Typical usage:
     * <pre>
     * try {
     *     setAlarm(delay);
     *     ...
     * }
     * finally {
     *     setAlarm(0);
     * }
     * </pre>
     * @param timeout the interval (in milliseconds) after which the calling
     * thread will be interrupted, if not cancelled in the meantime.
     */
    protected void setAlarm(int timeout) {
        setAlarm(timeout, Thread.currentThread());
    }


    /**
     * Set an alarm that will interrupt a given thread after
     * a specified delay (in milliseconds), and repeatedly thereafter
     * until cancelled.
     * Typical usage:
     * <pre>
     * try {
     *     setAlarm(delay);
     *     ...
     * }
     * finally {
     *     setAlarm(0);
     * }
     * </pre>
     * @param timeout the interval (in milliseconds) after which the calling
     *      thread will be interrupted, if not cancelled in the meantime.
     * @param threadToInterrupt which thread to interrupt
     */
    protected void setAlarm(int timeout, Thread threadToInterrupt) {
        if (alarm != null) {
            alarm.cancel();
            alarm = null;
        }

        if (timeout > 0)
            alarm = new Alarm(timeout, threadToInterrupt);
    }

    /**
     * Set TimeoutProvider used to control test timeouts.
     *
     * @see TimeoutProvider
     * @see #getTestTimeout()
     * @see #getTimeoutProvider()
     * @param provider null to use default test timeout value (10 sec).
     */
    public void setTimeoutProvider(TimeoutProvider provider) {
        if(provider != this.provider) {
            this.provider = provider;
        }
    }

    /**
     * Getter for TimeoutProvider. Generates default (10*factor)
     * provider in case no provider is set
     *
     * @return TimeoutProvider set to Script. Returns default
     * TimeoutProvider in case no TimeoutProvider
     * is set (or it is set to null).
     * The default implementation is 10 minutes scaled by
     * a value found in the environment ("javatestTimeoutFactor").
     * @see #setTimeoutProvider(com.sun.javatest.Script.TimeoutProvider)
     * @see #getTestTimeout()
     * @see TimeoutProvider
     */
    public TimeoutProvider getTimeoutProvider() {
        if(provider == null) {
            provider = new DefaultTimeoutProvider();
        }
        return provider;
    }

    /**
     * Get the timeout to be used for a test.
     * Uses TimeoutProvider to get test timeout value. The default
     * implementation of TimeoutProvider is 10 minutes scaled by
     * a value found in the environment ("javatestTimeoutFactor").
     * This method can be overriden to provide different behaviors.
     * A value of zero means no timeout.
     * @return the number of seconds in which the test is expected to
     * complete its execution.
     * @see #getTimeoutProvider()
     * @see #setTimeoutProvider(com.sun.javatest.Script.TimeoutProvider)
     * @see TimeoutProvider
     */
    protected int getTestTimeout() {
        // use "getTimeoutProvider()." instead of "provider." to generate default (10min*factor) timeout provider
        return getTimeoutProvider().getTestTimeout();
    }

    /**
     * Compile the given source files individually. One at a time, each source file
     * is passed to <em>compileTogether</em>, until they have all been
     * successfully compiled, or until one fails to compile.
     * @param srcs      The names of the file to be compiled.
     * @return          The status of the compilation: passed or failed.
     * @see #compileTogether
     */
    protected Status compileIndividually(String[] srcs) {
        return compileIndividually(DEFAULT_COMPILE_COMMAND, srcs);
    }

    /**
     * Compile the given source files individually. One at a time, each source file
     * is passed to <em>compileTogether</em>, until they have all been
     * successfully compiled, or until one fails to compile.
     * @param command the base name of the command entry in the environment to be used
     * to compile any necessary sources. The complete entry name will be
     * <code>command.</code><i>command</i><code>.</code><i>extn</i>
     * @param srcs      The names of the file to be compiled.
     * @return          The status of the compilation: passed or failed.
     * @see #compileTogether
     */
    protected Status compileIndividually(String command, String[] srcs) {
        if (srcs.length == 0)
            return error_noSource;

        for (int i = 0; i < srcs.length; i++) {
            Status s = compileOne(command, srcs[i]);
            if (!s.isPassed())
                return s;
        }
        return pass_compSuccExp;
    }

    /**
     * Compile the given source files individually. One at a time, each source file
     * is passed to <em>compileTogether</em>, until they have all been
     * successfully compiled, or until one fails to compile.
     * @param srcs      The names of the file to be compiled.
     * @return          The status of the compilation: passed or failed.
     * @see #compileTogether
     */
    protected Status compileIndividually(File[] srcs) {
        return compileIndividually(DEFAULT_COMPILE_COMMAND, filesToStrings(srcs));
    }

    /**
     * Compile the given source files individually. One at a time, each source file
     * is passed to <em>compileTogether</em>, until they have all been
     * successfully compiled, or until one fails to compile.
     * @param command the base name of the command entry in the environment to be used
     * to compile any necessary sources. The complete entry name will be
     * <code>command.</code><i>command</i><code>.</code><i>extn</i>
     * @param srcs      The names of the file to be compiled.
     * @return          The status of the compilation: passed or failed.
     * @see #compileTogether
     */
    protected Status compileIndividually(String command, File[] srcs) {
        return compileIndividually(command, filesToStrings(srcs));
    }

    /**
     * Compile the given source file.
     * @param src       The name of the file to be compiled.
     * @return          The status of the compilation: passed or failed.
     * @see #compileTogether
     */
    protected Status compileOne(String src) {
        return compileOne(DEFAULT_COMPILE_COMMAND, src);
    }

    /**
     * Compile the given source file. The file is treated as a singleton group
     * and passed to <em>compileTogether</em>.
     * @param command the base name of the command entry in the environment to be used
     * to compile any necessary sources. The complete entry name will be
     * <code>command.</code><i>command</i><code>.</code><i>extn</i>
     * @param src       The name of the file to be compiled.
     * @return          The status of the compilation: passed or failed.
     * @see #compileTogether
     */
    protected Status compileOne(String command, String src) {
        return compileTogether(command, new String[] {src});
    }

    /**
     * Compiles the given source file.
     *
     * @param src       The name of the file to be compiled.
     * @return          The status of the compilation: passed or failed.
     * @see #compileTogether
     */
    protected Status compileOne(File src) {
        return compileOne(DEFAULT_COMPILE_COMMAND, src.getPath());
    }

    /**
     * Compiles the given source file.
     *
     * @param command the base name of the command entry in the environment to be used
     * to compile any necessary sources. The complete entry name will be
     * <code>command.</code><i>command</i><code>.</code><i>extn</i>
     * @param src       The name of the file to be compiled.
     * @return          The status of the compilation: passed or failed.
     * @see #compileTogether
     */
    protected Status compileOne(String command, File src) {
        return compileOne(command, src.getPath());
    }

    /**
     * Compile the given source files together.  The compiler and arguments to be used
     * are identified by the `<code>env.<em>env</em>.compile.<em>extn</em>.*</code>'
     * properties in the script's environment, where <em>env</em>
     * is the name of the environment specified to the GUI, and <em>extn</em> is
     * the extension of the first source file.  The names of the files to be compiled
     * are added to the end of the arguments retrieved from the environment.
     * @param srcs      The names of the file to be compiled.
     * @return          The status of the compilation: passed or failed.
     * @see #invokeCommand
     */
    protected Status compileTogether(String[] srcs) {
        return compileTogether(DEFAULT_COMPILE_COMMAND, srcs);
    }

    /**
     * Compile the given source files together.  The compiler and arguments to be used
     * are identified by the `<code>env.<em>env</em>.command.<em>command</em>.<em>extn</em>.*</code>'
     * properties in the script's environment, where <em>env</em>
     * is the name of the environment specified to the GUI, and <em>extn</em> is
     * the extension of the first source file.  The names of the files to be compiled
     * are added to the end of the arguments retrieved from the environment.
     * @param command the base name of the command entry in the environment to be used
     * to compile any necessary sources. The complete entry name will be
     * <code>command.</code><i>command</i><code>.</code><i>extn</i>
     * @param srcs      The names of the file to be compiled.
     * @return          The status of the compilation: passed or failed.
     * @see #invokeCommand
     */
    protected Status compileTogether(String command, String[] srcs) {
        if (srcs.length == 0)
            return error_noSource;

        try {
            String[] classDir = env.lookup("testClassDir");
            if (classDir == null || classDir.length != 1)
                return error_badTestClassDir;
            File f = new File(classDir[0]);
            if (!f.exists())
                f.mkdirs();
        }
        catch (TestEnvironment.Fault e) {
            return error_badTestClassDir;
        }

        String primarySrcFile = srcs[0];
        int dot = primarySrcFile.lastIndexOf('.');
        if (dot == -1)
            return error_noExtnInSource;

        String extn = primarySrcFile.substring(dot);

        env.put("testSource", srcs);

        try {
            boolean ok = sourceTable.acquire(srcs, 10*60*1000);
            if (!ok)
                return Status.error(i18n.getString("script.srcLockTimeout"));
            return invokeCommand(command + extn);
        }
        catch (InterruptedException e) {
            return Status.error(i18n.getString("script.srcLockInterrupted"));
        }
        finally {
            sourceTable.release(srcs);
        }
    }

    private static ResourceTable sourceTable = new ResourceTable();

    /**
     * Compile the given source files together.  The compiler and arguments to be used
     * are identified by the `<code>env.<em>env</em>.command.compile.<em>extn</em>.*</code>'
     * properties in the script's environment, where <em>env</em>
     * is the name of the environment specified to the GUI, and <em>extn</em> is
     * the extension of the first source file.  The names of the files to be compiled
     * are added to the end of the arguments retrieved from the environment.
     * @param srcs      The names of the file to be compiled.
     * @return          The status of the compilation: passed or failed.
     * @see #invokeCommand
     */
    protected Status compileTogether(File[] srcs) {
        return compileTogether(DEFAULT_COMPILE_COMMAND, filesToStrings(srcs));
    }

    /**
     * Compile the given source files together.  The compiler and arguments to be used
     * are identified by the `<code>env.<em>env</em>.command.<em>command</em>.<em>extn</em>.*</code>'
     * properties in the script's environment, where <em>env</em>
     * is the name of the environment specified to the GUI, and <em>extn</em> is
     * the extension of the first source file.  The names of the files to be compiled
     * are added to the end of the arguments retrieved from the environment.
     * @param command the base name of the command entry in the environment to be used
     * to compile any necessary sources. The complete entry name will be
     * <code>command.</code><i>command</i><code>.</code><i>extn</i>
     * @param srcs      The names of the file to be compiled.
     * @return          The status of the compilation: passed or failed.
     * @see #invokeCommand
     */
    protected Status compileTogether(String command, File[] srcs) {
        return compileTogether(command, filesToStrings(srcs));
    }

    /**
     * Compile those source files for which the corresponding class file appears to
     * be out of date. Each source file is scanned to find a package statement to help
     * determine the main class defined in the source file -- the corresponding class
     * file in the given class directory is then checked, and if the source file is newer,
     * it is put on a list to be recompiled. After checking all the source files, if any
     * need to be recompiled, they will be compiled together, using the default compile
     * command ("command.compile.extn") entry in the the environment.
     * @param srcs The names of the source files to be compiled if necessary
     * @param classDir The class directory in which the corresponding class files
     * (if any) will be found.
     * @return          The status of the compilation: passed or failed.
     * @see #compileTogether
     */
    protected Status compileIfNecessary(String[] srcs, String classDir) {
        return compileIfNecessary(DEFAULT_COMPILE_COMMAND, srcs, classDir);
    }

    /**
     * Compile those source files for which the corresponding class file appears to
     * be out of date. Each source file is scanned to find a package statement to help
     * determine the main class defined in the source file -- the corresponding class
     * file in the given class directory is then checked, and if the source file is newer,
     * it is put on a list to be recompiled. After checking all the source files, if any
     * need to be recompiled, they will be compiled together, using the specified compile
     * command entry in the the environment.
     * @param command the base name of the command entry in the environment to be used
     * to compile any necessary sources. The complete entry name will be
     * <code>command.</code><i>command</i><code>.</code><i>extn</i>
     * @param srcs The names of the source files to be compiled if necessary
     * @param classDir The class directory in which the corresponding class files
     * (if any) will be found.
     * @return          The status of the compilation: passed or failed.
     * @see #compileTogether
     */
    protected Status compileIfNecessary(String command, String[] srcs, String classDir) {
        if (srcs.length == 0)
            return error_noSource;

        if (classDir == null)
            classDir = "$testClassDir";

        if (classDir.startsWith("$")) {
            try {
                String[] cd = env.resolve(classDir);
                if (cd == null || cd.length != 1)
                    return error_badTestClassDir;
                classDir = cd[0];
            }
            catch (TestEnvironment.Fault e) {
                return error_badTestClassDir;
            }
        }

        File cdf = new File(classDir);
        if (!cdf.exists())
            cdf.mkdirs();

        Vector v = new Vector(0, srcs.length);

        for (int i = 0; i < srcs.length; i++) {
            String src = srcs[i];
            int x = src.lastIndexOf(File.separatorChar);
            int y = src.indexOf('.', x+1);
            String className = src.substring(x+1, (y == -1 ? src.length() : y));
            String pkgPrefix;  // deliberately unset to have compiler check init in all required cases

            // read the source file to see if a package statement exists
            // if it does, set pkgPrefix to package directory
            // if none found, set setPrefix to empty string
            // if error, report the error, ignore pkgPrefix, and set file to
            // be unconditionally compiled.
            BufferedReader r = null;
            try {
                r = new BufferedReader(new FileReader(src));
                StreamTokenizer tr = new StreamTokenizer(r);
                tr.ordinaryChar('/');
                tr.slashStarComments(true);
                tr.slashSlashComments(true);
                tr.wordChars('.', '.'); // package separator
                int c = tr.nextToken();
                if (c == StreamTokenizer.TT_WORD && tr.sval.equals("package")) {
                    // found what looks like a package statement
                    c = tr.nextToken();
                    if (c == StreamTokenizer.TT_WORD)
                        // yes, it was a valid package statement
                        pkgPrefix = tr.sval.replace('.', File.separatorChar) + File.separatorChar;
                    else {
                        // well, sort of; malformed package statement
                        trOut.println(i18n.getString("script.badPackage"));
                        v.addElement(src);
                        continue;
                    }
                }
                else
                    // no package statement
                    pkgPrefix = "";
            }
            catch (IOException e) {
                trOut.println(i18n.getString("script.badDateStamp", new Object[] { src, e }));
                v.addElement(src);
                continue;
            }
            finally {
                if (r != null) {
                    try {
                        r.close();
                    }
                    catch (IOException ignore) {
                    }
                }
            }


            File srcFile = new File(src);
            File classFile = new File(classDir, pkgPrefix + className + ".class");
            //System.out.println("checking " + classFile);
            //System.out.println("classfile " + classFile.lastModified());
            //System.out.println("srcfile " + srcFile.lastModified());
            if (classFile.exists() && classFile.lastModified() > srcFile.lastModified())
                trOut.println(i18n.getString("script.upToDate", src));
            else
                v.addElement(src);
        }

        if (v.size() > 0) {
            String[] necessarySrcs = new String[v.size()];
            v.copyInto(necessarySrcs);

            return compileTogether(command, necessarySrcs);
        }
        else
            return Status.passed(i18n.getString("script.allUpToDate"));
    }

    /**
     * Compile those source files for which the corresponding class file appears to
     * be out of date. Each source file is scanned to find a package statement to help
     * determine the main class defined in the source file -- the corresponding class
     * file in the given class directory is then checked, and if the source file is newer,
     * it is put on a list to be recompiled. After checking all the source files, if any
     * need to be recompiled, they will be compiled together, using the default compile
     * command ("command.compile.extn") entry in the the environment.
     * @param srcs The names of the source files to be compiled if necessary
     * @param classDir The class directory in which the corresponding class files
     * (if any) will be found.
     * @return          The status of the compilation: passed or failed.
     * @see #compileTogether
     */
    protected Status compileIfNecessary(File[] srcs, String classDir) {
        return compileIfNecessary(DEFAULT_COMPILE_COMMAND, filesToStrings(srcs), classDir);
    }

    /**
     * Compile those source files for which the corresponding class file appears to
     * be out of date. Each source file is scanned to find a package statement to help
     * determine the main class defined in the source file -- the corresponding class
     * file in the given class directory is then checked, and if the source file is newer,
     * it is put on a list to be recompiled. After checking all the source files, if any
     * need to be recompiled, they will be compiled together, using the specified compile
     * command entry in the the environment.
     * @param command the base name of the command entry in the environment to be used
     * to compile any necessary sources. The complete entry name will be
     * <code>command.</code><i>command</i><code>.</code><i>extn</i>
     * @param srcs The names of the source files to be compiled if necessary
     * @param classDir The class directory in which the corresponding class files
     * (if any) will be found.
     * @return          The status of the compilation: passed or failed.
     * @see #compileTogether
     */
    protected Status compileIfNecessary(String command, File[] srcs, String classDir) {
        return compileIfNecessary(command, filesToStrings(srcs), classDir);
    }

    /**
     * Execute the given class with the given arguments, which need to be passed
     * to the environment for $ substitution and for splitting into separate strings.
     * @param executeClass      The name of the class to be executed
     * @param executeArgs       The arguments to be evaluated before passing to
     *                          the class to be executed
     * @return                  The status of the execution
     * @see #execute(java.lang.String, java.lang.String, java.lang.String)
     */
    protected Status execute(String executeClass, String executeArgs) {
        return execute(DEFAULT_EXECUTE_COMMAND, executeClass, executeArgs);
    }

    /**
     * Execute the given class with the given arguments, which need to be passed
     * to the environment for $ substitution and for splitting into separate strings.
     * @param command   The name of the command containing the template to be executed
     * @param executeClass      The name of the class to be executed
     * @param executeArgs       The arguments to be evaluated before passing to
     *                          the class to be executed
     * @return                  The status of the execution
     */
    protected Status execute(String command, String executeClass,
                             String executeArgs) {
        try {
            String[] args = (executeArgs == null ? nullArgs : env.resolve(executeArgs));
            if (excludedTestCases != null)
                args = exclude(args, excludedTestCases);
            return execute(command, executeClass, args);
        }
        catch (TestEnvironment.Fault e) {
            trOut.println(i18n.getString("script.testEnvFault",
                                         new Object[] { executeArgs, e.toString() }));
            return error_badExecuteArgs;
        }
    }

    /**
     * Execute the given class with the given arguments.  The interpreter to be used
     * and its arguments are identified by the `<code>env.<em>env</em>.execute.*</code>'
     * properties in the script's environment, where <em>env</em>
     * is the name of the environment specified to the GUI. The class to be executed and
     * its arguments are added to the end of the arguments retrieved from the environment.
     * @param executeClass      The name of the class to be executed.
     * @param executeArgs       Any arguments to be passed to the class to be executed.
     * @return                  The status of the execution
     * @see #execute(java.lang.String, java.lang.String, java.lang.String[])
     */
    protected Status execute(String executeClass, String[] executeArgs) {
        return execute(DEFAULT_EXECUTE_COMMAND, executeClass, executeArgs);
    }

    /**
     * Execute the given class with the given arguments.  The interpreter to be used
     * and its arguments are identified by the `<code>env.<em>env</em>.<em>command</em>.*</code>'
     * properties in the script's environment, where <em>env</em>
     * is the name of the environment specified to the GUI. The class to be executed and
     * its arguments are added to the end of the arguments retrieved from the environment.
     * @param command   The name of the command containing the template to be executed
     * @param executeClass      The name of the class to be executed.
     * @param executeArgs       Any arguments to be passed to the class to be executed.
     * @return                  The status of the execution
     * @see #invokeCommand
     */
    protected Status execute(String command, String executeClass, String[] executeArgs) {
        if (executeClass == null || executeClass.length() == 0)
            return error_noExecuteClass;
        env.put("testExecuteClass", executeClass);
        env.put("testExecuteArgs", executeArgs);
        return invokeCommand(command);
    }

    /**
     * RMI Compile the given class files.  The compiler and arguments to be used
     * is identified by the `<code>env.<em>env</em>.command.rmic</code>'
     * property in the script's environment, where <em>env</em>
     * is the name of the environment specified to the GUI.
     * The name of the classes to be compiled by rmic is obtained from the
     * test description.
     * @param classes   The names of the classes to be compiled by rmic.
     * @return          The status of the compilation: passed or failed.
     * @see #invokeCommand
     */
    protected Status rmiCompile(String[] classes) {
        return rmiCompile(DEFAULT_RMIC_COMMAND, classes);
    }

    /**
     * RMI Compile the given class files.  The compiler and arguments to be used
     * is identified by the `<code>env.<em>env</em>.command.<em>command</em></code>'
     * property in the script's environment, where <em>env</em>
     * is the name of the environment specified to the GUI.
     * The name of the classes to be compiled by rmic is obtained from the
     * test description.
     * @param command   The name of the command containing the template to be compiled
     * @param classes   The names of the classes to be compiled by rmic.
     * @return          The status of the compilation: passed or failed.
     * @see #invokeCommand
     */
    protected Status rmiCompile(String command, String[] classes) {
        try {
            String[] classDir = env.lookup("testClassDir");
            if (classDir == null || classDir.length != 1)
                return error_badTestClassDir;
            File f = new File(classDir[0]);
            if (!f.exists())
                f.mkdirs();
        }
        catch (TestEnvironment.Fault e) {
            return error_badTestClassDir;
        }

        if (classes == null || classes.length == 0)
            return error_noRMIClasses;

        env.put("testRmicClasses", classes);
        // backwards compatibility
        env.put("testRmicClass", classes);
        return invokeCommand(command);
    }


    /**
     * Invoke a command in the environment identified by a given key.
     * The command is identified by looking up `<code>command.<em>key</em></code>'
     * property in the environment. The first word of this property identifies
     * the name of a class that should be an implementation of <code>Command</code>,
     * and the subsequent words are the arguments to be passed to a fresh instance
     * of that class, via its <code>run</code> method.
     * Standard library implementations of <code>Command</code> are available,
     * such as:
     * <DL>
     * <DT>com.sun.javatest.lib.ProcessCommand
     * <DD>Execute a command in a separate process
     * <DT>com.sun.javatest.lib.ExecStdTestSameJVMCmd
     * <DD>Execute a standard test in the same JVM as JT Harness
     * <DT>com.sun.javatest.agent.PassiveAgentCommand
     * <DD>Execute a command on a remote machine
     * </DL>
     * For full details, the documentation for the various appropriate classes.
     *
     * <p> The use of `<code>command.<em>key</em></code>' supercedes an earlier
     * mechanism involving multiple properties. For backwards compatibility,
     * if the `<code>command.<em>key</em></code>' property is not found, the
     * properties for the earlier mechanism are checked as well.
     *
     * @param key       The tag for the command to be executed
     * @return          A status giving the outcome of the command
     *
     * @see Command
     *
     */
    protected Status invokeCommand(String key) {
        TestResult.Section section;
        Status s = null;

        try {
            String[] command = env.lookup("command." + key);

            if (command.length == 0)
                return Status.error(i18n.getString("script.noCommand",
                                                   new Object[] { env.getName(), key }));

            String className = command[0];
            String[] args = new String[command.length - 1];
            System.arraycopy(command, 1, args, 0, args.length);

            section = testResult.createSection(key);

            section.getMessageWriter().println(i18n.getString("script.command",
                                                              new Object[] {className, StringArray.join(args) }));

            PrintWriter out1 = null;
            PrintWriter out2 = null;
            try {
                out1 = section.createOutput(cmdOut1Name);
                out2 = section.createOutput(cmdOut2Name);

                s = invokeClass(className, args, out1, out2);

                out1.close();
                out2.close();
            }
            finally {
                if (out2 != null)  out2.close();
                if (out1 != null)  out1.close();
            }

            section.setStatus(s);
            return s;
        }
        catch (TestEnvironment.Fault e) {
            return Status.error(i18n.getString("script.badCommand",
                                               new Object[] { env.getName(), key }));
        }
    }

    /**
     * Set the default names of the two default output streams used when executing a
     * command.  In many cases these may correspond to the UNIX-style standard-out
     * and standard-error streams.  This API does not define what they are used for
     * though, and architects are encouraged to give descriptive names if possible.
     *
     * @param out1Name Name of the first stream.
     * @param out2Name Name of the second stream.
     */
    protected void setDefaultCommandStreamNames(String out1Name, String out2Name) {
        cmdOut1Name = out1Name;
        cmdOut2Name = out2Name;
    }

    /**
     * Create and run a Command object.
     * @param className The name of the class to load and instantiate.
     * @param args      The args to pass to the `run' method of the loaded object.
     * @return          The result identifies any problems that may occur in trying
     *                  to create and run the specified object, or if it succeeds,
     *                  it returns the result from calling the object's `run' method.
     * @see Command
     */
    private Status invokeClass(String className, String[] args,
                               PrintWriter out1, PrintWriter out2) {
        // this is the central place where we get to run what the user
        // says in the environment file:
        Command testCommand;
        try {
            Class c = (loader == null ? Class.forName(className) : loader.loadClass(className));
            testCommand = (Command)(c.newInstance());
        }
        catch (ClassCastException e) {
            return Status.error(i18n.getString("script.cantRunClass",
                                               new Object[] { className, Command.class.getName() }));
        }
        catch (ClassNotFoundException ex) {
            return Status.error(i18n.getString("script.cantFindClass",
                                               new Object[] { className, env.getName() }));
        }
        catch (IllegalAccessException ex) {
            return Status.error(i18n.getString("script.cantAccessClass",
                                               new Object[] { className, env.getName() }));
        }
        catch (IllegalArgumentException ex) {
            return Status.error(i18n.getString("script.badClassName",
                                               new Object[] { className, env.getName() }));
        }
        catch (InstantiationException ex) {
            return Status.error(i18n.getString("script.cantCreateClass",
                                               new Object[] { className, env.getName() }));
        }
        catch (ThreadDeath e) {
            throw (ThreadDeath)(e.fillInStackTrace());
        }
        catch (Exception e) {
            e.printStackTrace(out1);
            return Status.error(i18n.getString("script.unexpLoadExc", new Object[] { className, e }));
        }
        catch (Error e) {
            e.printStackTrace(out1);
            return Status.error(i18n.getString("script.unexpLoadErr", new Object[] { className, e }));
        }
        catch (Throwable e) {
            e.printStackTrace(out1);
            return Status.error(i18n.getString("script.unexpLoadThr", new Object[] { className, e }));
        }

        try {
            testCommand.setClassLoader(loader);
            return testCommand.run(args, out1, out2);
        }
        catch (ThreadDeath e) {
            throw (ThreadDeath)(e.fillInStackTrace());
        }
        catch (Exception e) {
            e.printStackTrace(out1);
            // error reduced to failed in following line for benefit of negative tests
            return Status.failed(i18n.getString("script.unexpExecExc", new Object[] { className, e }));
        }
        catch (Error e) {
            e.printStackTrace(out1);
            // error reduced to failed in following line for benefit of negative tests
            return Status.failed(i18n.getString("script.unexpExecErr", new Object[] { className, e }));
        }
        catch (Throwable e) {
            e.printStackTrace(out1);
            // error *NOT* reduced to failed in following line for benefit of
            // negative tests: test should never throw something which is not
            // an Exception or Error
            return Status.error(i18n.getString("script.unexpExecThr", new Object[] { className, e }));
        }

    }

    /**
     * Modify the args for a test to be executed, according to a set
     * of test cases to be excluded. If there are no test cases to be excluded,
     * the result will be the original args unchanged; otherwise, the
     * result will be the original args prefixed by "-exclude" and a
     * comma-separated list of exclude test cases.
     * @param args The basic list of args for the test
     * @param testCases the set of test cases to be excluded, or null if none
     * @return The original list of args, possibly prefixed by "-exclude"
     * and a comma-separated list of test cases that should not be executed by
     * the test
     */
    protected String[] exclude(String[] args, String[] testCases) {
        if (testCases == null)
            return args;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < testCases.length; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(testCases[i]);
        }
        String[] newArgs = new String[args.length + 2];
        newArgs[0] = "-exclude";
        newArgs[1] = sb.toString();
        System.arraycopy(args, 0, newArgs, 2, args.length);
        testResult.putProperty("exclude", newArgs[1]);
        return newArgs;
    }

    /**
     * Utility routine to convert an array of filenames to a corresponding
     * array of strings.
     * @param files     The filenames to be converted
     * @return          The corresponding strings
     */
    protected static String[] filesToStrings(File[] files) {
        String[] strings = new String[files.length];
        for (int i = 0; i < files.length; i++)
            strings[i] = files[i].getPath();
        return strings;
    }

    /**
     * The test description for the test being performed.
     */
    protected TestDescription td;               // required

    /**
     * The set of test cases to be excluded for this test.
     */
    protected String[] excludedTestCases;       // optional, may be null

    /**
     * The test environment for the test being performed.
     */
    protected TestEnvironment env;              // required

    /**
     * The initialization args for the script.
     */
    protected String[] scriptArgs;              // optional

    /**
     * The work directory for the test run.
     */
    protected WorkDirectory workDir;                    // required

    /**
     * The default name for the TestResult section used to save the data written to the out1 stream
     * for a command.
     * @see Command#run
     */
    protected String cmdOut1Name = "out1";

    /**
     * The default name for the TestResult section used to save the data written to the out2 stream
     * for a command.
     * @see Command#run
     */
    protected String cmdOut2Name = "out2";

    /**
     * A backup policy object that specifies how files should be backed up,
     * if a file is found to exist when a new one of the same name is to be
     * written.
     */
    protected BackupPolicy backupPolicy = BackupPolicy.noBackups(); // optional

    /**
     * The class loader to be used to load additional user-specified classes
     * as required in the execution of the script.
     */
    protected ClassLoader loader;               // optional, may be null

    /**
     * The reporting channel for the test being performed.
     */
    protected PrintWriter trOut;

    // have to define this before the definitions that follow
    private static final I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(Script.class);

    // use getTimeoutProvider and setTimeoutProvider
    private TimeoutProvider provider = null;

    // convenience definitions

    /**
     * A status that may be used to indicate problems in the executeArgs field
     * of a test description.
     */
    protected static final Status
        error_badExecuteArgs = Status.error(i18n.getString("script.badExecuteArgs"));

    /**
     * A status that may be used to indicate a problem with a test's class directory.
     */
    protected static final Status
        error_badTestClassDir = Status.error(i18n.getString("script.badTestClassDir"));

    /**
     * A status that may be used to indicate that a compilation failed unexpectedly.
     */
    protected static final Status
        error_compFailUnexp = Status.error(i18n.getString("script.compFailUnexp"));

    /**
     * A status that may be used to indicate that no action was specified.
     */
    protected static final Status
        error_noActionSpecified = Status.error(i18n.getString("script.noAction"));

    /**
     * A status that may be used to indicate that no execute class was specified in a test description.
     */
    protected static final Status
        error_noExecuteClass = Status.error(i18n.getString("script.noExecuteClass"));

    /**
     * A status that may be used to indicate that no extension was found in a source file.
     */
    protected static final Status
        error_noExtnInSource = Status.error(i18n.getString("script.noExtnInSrc"));

    /**
     * A status that may be used to indicate that no rmi classes were specified in a test description.
     */
    protected static final Status
        error_noRMIClasses = Status.error(i18n.getString("script.noRMIClasses"));

    /**
     * A status that may be used to indicate that no sources were specified in a test description.
     */
    protected static final Status
        error_noSource = Status.error(i18n.getString("script.noSource"));

    /**
     * A status that may be used to indicate the a compilation failed unexpectedly.
     */
    protected static final Status
        fail_compFailUnexp = Status.failed(i18n.getString("script.compFailUnexp"));

    /**
     * A status that may be used to indicate that a compilation did not fail as was expected.
     */
    protected static final Status
        fail_compSuccUnexp = Status.failed(i18n.getString("script.compSuccUnexp"));

    /**
     * A status that may be used to indicate that a test execution step  did not fail as wqas expected.
     */
    protected static final Status
        fail_execSuccUnexp = Status.failed(i18n.getString("script.execSuccUnexp"));

    /**
     * A status that may be used to indicate that a compilation failed as expected.
     */
    protected static final Status
        pass_compFailExp = Status.passed(i18n.getString("script.compFailExp"));

    /**
     * A status that may be used to indicate that a compilation succeeded as expected.
     */
    protected static final Status
        pass_compSuccExp = Status.passed(i18n.getString("script.compSuccExp"));

    /**
     * A status that may be used to indicate that an execution step failed, as was expected.
     */
    protected static final Status
        pass_execFailExp = Status.passed(i18n.getString("script.execFailExp"));

    // backwards compatibility
    /**
     * A status that may be used to indicate that no source files were found in the test description.
     */
    protected static final Status noSource = error_noSource;

    /**
     * A status that may be used to indicate that no extension was found in a source file.
     */
    protected static final Status noExtnInSource = error_noExtnInSource;

    private static final String[] nullArgs = { };
    private static final String DEFAULT_COMPILE_COMMAND = "compile";
    private static final String DEFAULT_EXECUTE_COMMAND = "execute";
    private static final String DEFAULT_RMIC_COMMAND = "rmic";
    private static final String defaultClassDir = "classes";
    private static String osInfo;

    /**
     * A timer that may be used to set up timeouts.
     */
    protected static final Timer alarmTimer = new Timer();

    private TestResult testResult;
    private Alarm alarm;
    private boolean jtrIfPassed =
        System.getProperty("javatest.script.jtrIfPassed", "true").equals("true");

    /**
     * Notifier of starting/finishing tests.
     * Initialized only when useNotifer() returns true.
     * @see #useNotifier
     * @see #setNotifier
     * @since 4.2.1
     */
    protected Harness.Observer notifier;

    /**
     * Returns true if the Script uses own way of notifying the Harness
     * of starting/finishing test, false otherwise (by default).
     *
     * Normally the Harness notifies all listeners of an event of
     * starting a test when the method run() is invoked and an event of
     * finishing the test when the method run() is completed. Those Scripts
     * which need to take a control over notifying should override this method
     * to return <code>true</code>. In this case the <i>notifier</i> field will
     * be initialized and the Harness will no longer notify the listeners when
     * a test starts/stops.
     * @since 4.2.1
     */
    public boolean useNotifier() {
        return false;
    }

    /**
     * Sets notifier to be used to inform listeners of events of a test
     * starting/finishing. Invoked by the Harness iff useNotifier()
     * returns true.
     *
     * @see #useNotifier
     * @since 4.2.1
     */
    public void setNotifier(Harness.Observer notifier) {
        this.notifier = notifier;
    }

    /**
     * Interface for extended testTimeout control. Use setTimeoutProvider to
     * change test timeout value
     * @see #setTimeoutProvider(TimeoutProvider)
     */
    public static interface TimeoutProvider {
        /**
         * Implement this method returning desired test timeout value
         *
         * @return timeout in <b>seconds</b>
         */
        public int getTestTimeout();
    }

    private class DefaultTimeoutProvider implements TimeoutProvider {
        public int getTestTimeout() {
            float factor = 1;
            try {
                String[] jtf = env.lookup("javatestTimeoutFactor");
                if (jtf != null) {
                    if (jtf.length == 1)
                        factor = Float.parseFloat(jtf[0]);
                    else if (jtf.length == 2)
                        factor = Float.parseFloat(jtf[1]);
                }
            }
            catch (TestEnvironment.Fault e) {
            }
            return (int) (600 * factor); // 60 * 10 = 600 sec = 10 min
        }
    }

    private class Alarm implements Timer.Timeable {
        Alarm(int delay) {
            this(delay, Thread.currentThread());
        }

        Alarm(int delay, Thread threadToInterrupt) {
            if (threadToInterrupt == null)
                throw new NullPointerException();

            this.delay = delay;
            this.threadToInterrupt = threadToInterrupt;
            entry = alarmTimer.requestDelayedCallback(this, delay);
            if (debugAlarm)
                System.err.println(i18n.getString("script.alarm.started", this));
        }

        synchronized void cancel() {
            if (debugAlarm)
                System.err.println(i18n.getString("script.alarm.cancelled", this));
            alarmTimer.cancel(entry);
        }

        public synchronized void timeout() {
            if (count == 0)
                trOut.println(i18n.getString("script.timeout", new Float(delay/1000.f)));
            else if (count%100 == 0) {
                trOut.println(i18n.getString("script.notResponding", new Integer(count)));
                if (count%1000 == 0)
                    System.err.println(i18n.getString("script.timedOut",
                                                      new Object[] { td.getRootRelativeURL(), new Integer(count) }));
            }
            if (debugAlarm)
                System.err.println(i18n.getString("script.alarm.interrupt", new Object[] { this, threadToInterrupt }));
            threadToInterrupt.interrupt();
            count++;
            entry = alarmTimer.requestDelayedCallback(this, 100); // keep requesting interrupts until cancelled
        }

        private int delay;
        private Thread threadToInterrupt;
        private int count;
        private Timer.Entry entry;
    }

    private static boolean debugAlarm = Boolean.getBoolean("debug.com.sun.javatest.Script.Alarm");
}
