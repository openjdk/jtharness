/*
 * $Id$
 *
 * Copyright (c) 2004, 2013, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.tool;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import com.sun.javatest.Harness;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.Status;
import com.sun.javatest.TemplateUtilities;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;

import com.sun.javatest.util.DynamicArray;
import com.sun.javatest.util.HelpTree;
import com.sun.javatest.util.I18NResourceBundle;
import java.util.Map;

/**
 * An object to carry the shared state required and derived from
 * executing a series of commands.
 *
 * <p>While the object does provide some behavior, as detailed below,
 * much of its functionality is to provide a repository for values to
 * be passed from one command to another.
 *
 * @see CommandManager#parseCommand
 * @see Command#run
 */
public class CommandContext
{
    /**
     * This exception is used to report problems while executing a command.
     */
    public class Fault extends Exception
    {
        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         */
        Fault(I18NResourceBundle i18n, String s) {
            super(i18n.getString(s));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An argument to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        Fault(I18NResourceBundle i18n, String s, Object o) {
            super(i18n.getString(s, o));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An array of arguments to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        Fault(I18NResourceBundle i18n, String s, Object[] o) {
            super(i18n.getString(s, o));
        }

        Fault(Command.Fault e) {
            super(e.getMessage(), e);
        }

        /**
         * Get the command context object that created this fault object.
         * @return the command context object that created this fault object
         */
        public CommandContext getContext() {
            return CommandContext.this;
        }
    }

    /**
     * Create a new context object.
     * The output stream, used by the printXXX methods, is set
     * to System.err.
     */
    public CommandContext() {
        this(new PrintWriter(System.err));
    }

    /**
     * Create a new context object, using a specified PrintWriter
     * for the output stream, used by the printXXX methods.
     * @param out the output stream to be used by the printXXX methods.
     */
    public CommandContext(PrintWriter out) {
        this.out = out;
    }

    /**
     * Add a new command to the set of commands to be executed
     * by this object.
     * @param cmd The command to be added
     * @see #runCommands
     */
    public void addCommand(Command cmd) {
        commands.add(cmd);
    }

    /**
     * Get the set of commands to be executed by this object.
     * @return the set of commands to be executed by this object
     * @see #addCommand
     * @see #runCommands
     */
    public Command[] getCommands() {
        Command[] a = new Command[commands.size()];
        commands.copyInto(a);
        return a;
    }

    /**
     * Run the set of commands that have been registered with this object.
     * If none of the commands executed are action commands, and if an
     * "auto-run" command has been registered, it will be executed after all the
     * other commands have been executed.
     * @throws CommandContext.Fault if any of the commands executed throw Command.Fault
     */
    public void runCommands()
        throws CommandContext.Fault
    {
        boolean foundAction = false;

        for (int i = 0; i < commands.size(); i++) {
            Command cmd = (Command) (commands.elementAt(i));
            foundAction |= cmd.isActionCommand();

            // can't cache this ... may change while we execute commands
            boolean verbose = getVerboseOptionValue(VERBOSE_COMMANDS, false);
            if (verbose)
                out.println(TRACE_PREFIX + cmd.toString());

            try {
                cmd.run(this);
            }
            catch (Command.Fault e) {
                throw new Fault(e);
            }
        }

        if (!foundAction && autoRunCommand != null) {
            try {
                autoRunCommand.run(this);
            }
            catch (Command.Fault e) {
                throw new Fault(i18n, "cc.errorInDefault", e.getMessage());
            }
        }
    }

    /**
     * Add an observer to the set of observers maintained by this object.
     * @param o the observer to be added
     * @see #getHarnessObservers
     */
    public void addHarnessObserver(Harness.Observer o) {
        harnessObservers = (Harness.Observer[]) (DynamicArray.append(harnessObservers, o));
    }

    /**
     * Get the set of observers that have been registered with this object.
     * @return the set of observers that have been registered with this object
     * @see #addHarnessObserver
     */
    public Harness.Observer[] getHarnessObservers() {
        return harnessObservers;
    }

    /**
     * Get the "auto run" command registered with this object. If not null,
     * this command will be executed after the other commands executed by this
     * object, and if none of those commands are action commands.
     * @return the "auto run" command registered with this object
     * @see #setAutoRunCommand
     * @see #runCommands
     */
    public Command getAutoRunCommand() {
        return autoRunCommand;
    }

    /**
     * Set the "auto run" command registered with this object. If not null,
     * this command will be executed after the other commands executed by this
     * object, and if none of those commands are action commands.
     * @param c  the "auto run" command to be registered with this object
     * @see #getAutoRunCommand
     * @see #runCommands
     */
    public void setAutoRunCommand(Command c) {
        autoRunCommand = c;
    }

    /**
     * Get the "auto run report directory" registered with this object.
     * This is primarily to support backwards compatibility with JT Harness
     * 2.x behavior.
     * @return  the "auto run report directory" registered with this object
     * @see #setAutoRunReportDir
     */
    public File getAutoRunReportDir() {
        return autoRunReportDir;
    }

    /**
     * Set the "auto run report directory" registered with this object.
     * This is primarily to support backwards compatibility with JT Harness
     * 2.x behavior.
     * @param dir  the "auto run report directory" to be registered with this object
     * @see #getAutoRunReportDir
     */
    public void setAutoRunReportDir(File dir) {
        autoRunReportDir = dir;
    }

    /**
     * Add test run statistics into the cumulative counts.
     * @param stats an array of test counts, indexed by the standard Status.XXX values.
     */
    public void addTestStats(int[] stats) {
        if (stats.length != Status.NUM_STATES)
            throw new IllegalArgumentException();

        for (int i = 0; i < stats.length; i++)
            cumulativeTestStats[i] += stats[i];
    }

    /**
     * Get the cumulative test run statistics.
     * @return an array of test counts, indexed by the standard Status.XXX values.
     */
    public int[] getTestStats() {
        int[] s = new int[Status.NUM_STATES];
        System.arraycopy(cumulativeTestStats, 0, s, 0, s.length);
        return s;
    }


    /**
     * Check whether this object indicates that the desktop should be closed
     * when all commands are done.
     * @return true if this object indicates that the desktop should be closed
     * when all commands are done, and false otherwise
     * @see #setCloseDesktopWhenDoneEnabled
     */
    public boolean isCloseDesktopWhenDoneEnabled() {
        return closeDesktopWhenDoneEnabled;
    }

    /**
     * Specify whether this object should remember that the desktop should be closed
     * when all commands are done.
     * @param b true if this object should remember that the desktop should be closed
     * when all commands are done, and false otherwise
     * @see #isCloseDesktopWhenDoneEnabled
     */
    public void setCloseDesktopWhenDoneEnabled(boolean b) {
        closeDesktopWhenDoneEnabled = b;
    }

    /**
     * Check whether the test suite has been set yet.
     * @return true if the test suite has been set, and false otherwise
     */
    public boolean isTestSuiteSet() {
        return hasConfig();
    }

    /**
     * Get the test suite associated with this object.
     * @return the test suite associated with this object
     * @throws CommandContext.Fault if there is a problem determining the test suite from
     * the available parameters
     * @see #setTestSuite
     */
    public TestSuite getTestSuite()
        throws Fault
    {
        initConfig();
        return config.getTestSuite();
    }

    /**
     * Set the path for the test suite to be associated with this object.
     * The path will not be verified until required, so that it can be
     * evaluated in conjunction with other parameters such as the work
     * directory and configuration file.
     * @param path the path for the test suite to be associated with this object
     * @see #getTestSuite
     * @throws CommandContext.Fault if the test suite has already ben set
     */
    public void setTestSuite(File path)
        throws Fault
    {
        if (testSuitePath != null && !testSuitePath.equals(path))
            throw new Fault(i18n, "cc.tsAlreadySet", testSuitePath);

        testSuitePath = path;
    }

    /**
     * Set the test suite to be associated with this object.
     * @param ts the test suite to be associated with this object
     * @see #getTestSuite
     * @throws CommandContext.Fault if the test suite has already been set to
     * something else, or if there is a problem evaluating related parameters,
     * such as a configuration file or template, or a work directory.
     * @throws TestSuite.Fault if there is a problem evaluating related
     * parameters.
     */
    public void setTestSuite(TestSuite ts)
        throws Fault, TestSuite.Fault
    {
        if (isInitConfigRequired())
            initConfig();

        if (config == null)
            config = ts.createInterview();
        else if (config.getTestSuite() != ts)
            throw new Fault(i18n, "cc.tsAlreadySet", testSuitePath);
    }

    /**
     * Check whether the work directory has been set yet.
     * @return true if the work directory has been set, and false otherwise
     */
    public boolean isWorkDirectorySet() {
        return (hasConfig() && (config.getWorkDirectory() != null));
    }

    /**
     * Get the work directory associated with this object.
     * @return the work directory associated with this object, or null if not yet set
     * @throws CommandContext.Fault if there is a problem determining the work directory from
     * the available parameters
     * @see #setWorkDirectory
     */
    public WorkDirectory getWorkDirectory()
        throws Fault
    {
        initConfig();

        return config.getWorkDirectory();
    }

    /**
     * Set the path for the work directory to be associated with this object.
     * The path will not be verified until required, so that it can be
     * evaluated in conjunction with other parameters such as the test suite
     * and configuration file.
     * The path must identify a work directory that already exists.
     * @param path the path for the work directory to be associated with this object
     * @see #getWorkDirectory
     * @throws CommandContext.Fault if the work directory has already been set
     */
    public void setWorkDirectory(File path)
        throws Fault
    {
        setWorkDirectory(path, false);
    }

    /**
     * Set the path for the work directory to be associated with this object.
     * The path will not be verified until required, so that it can be
     * evaluated in conjunction with other parameters such as the test suite
     * and configuration file.
     * The work directory identified by this path may be created if necessary.
     * @param path the path for the work directory to be associated with this object
     * @param create create the work directory if it does not already exist
     * @see #getWorkDirectory
     * @throws CommandContext.Fault if the work directory has already been set
     */
    public void setWorkDirectory(File path, boolean create)
        throws Fault
    {
        if (workDirectoryPath != null && !workDirectoryPath.equals(path))
            throw new Fault(i18n, "cc.wdAlreadySet", workDirectoryPath);

        autoCreateWorkDirectory = create;
        workDirectoryPath = path;
    }

    /**
     * Set the work directory to be associated with this object.
     * @param wd the work directory to be associated with this object
     * @see #getWorkDirectory
     * @throws CommandContext.Fault if there is a problem evaluating related
     * parameters, such as a configuration file or template, or a
     * test suite.
     * @throws TestSuite.Fault if there is a problem evaluating related
     * parameters.
     * @throws CommandContext.Fault if the work directory has already
     * been set to something else
     */
    public void setWorkDirectory(WorkDirectory wd)
        throws Fault, TestSuite.Fault
    {
        if (isInitConfigRequired())
            initConfig();

        if (config == null)
            config = wd.getTestSuite().createInterview();
        else {
            if (wd.getTestSuite() != config.getTestSuite())
                throw new Fault(i18n, "cc.wdTestSuiteMismatch",
                                        new Object[] { wd.getRoot(), config.getTestSuite().getRoot() });

            WorkDirectory cwd = config.getWorkDirectory();
            if (cwd != null && cwd != wd)
                throw new Fault(i18n, "cc.wdAlreadySet", workDirectoryPath);
        }

        config.setWorkDirectory(wd);
    }

    /**
     * Get the configuration associated with this object.
     * @return  the configuration associated with this object
     * @see #getConfig
     * @see #setInterviewParameters
     * @deprecated Use getConfig().
     * @throws CommandContext.Fault if there is a problem evaluating the parameters
     * that define the configuration
     */
    public InterviewParameters getInterviewParameters()
        throws Fault
    {
        return getConfig();
    }

    /**
     * Get the configuration associated with this object.
     * @return  the configuration associated with this object
     * @see #setConfig
     * @throws CommandContext.Fault if there is a problem evaluating the parameters
     * that define the configuration
     */
    public InterviewParameters getConfig()
        throws Fault
    {
        initConfig();

        return config;
    }


    /**
     * Check whether a configuration has been set yet.
     * @return true if a configuration has been set, and false otherwise
     */
    public boolean hasConfig() {
        return (config != null
                || testSuitePath != null
                || workDirectoryPath != null
                || configFilePath != null);
    }

    /**
     * Set the path for the configuration information to be associated
     * with this object.
     * The path will not be verified until required, so that it can be
     * evaluated in conjunction with other parameters such as the test suite
     * and work directory.
     * @param path the path for the configuration information to be associated
     * with this object.
     * @see #getConfig
     * @throws CommandContext.Fault if the configuration has already been evaluated
     */
    public void setConfig(File path)
        throws Fault
    {
        if (config != null) {
            if (configFilePath == null)
                throw new Fault(i18n, "cc.confAlreadySetDefault", path);
            else
                throw new Fault(i18n, "cc.confAlreadySet", new Object[] { path, configFilePath });
        }

        configFilePath = path;
    }

    /**
     * Workdir that should be compatible with the selected test suite.  Should be
     * null if no test suite was selected or there was no previous desktop.
     */
    void setDefaultWorkDir(String path) {
        defaultWorkDirPath = path;
    }

    /**
     * restore filter setting if -ts -preferred were specified
     */
    void setDesktopData(Map desktopData) {
        this.desktopData = desktopData;
    }

    Map getDesktopData() {
        return desktopData;
    }

    /**
     * Set the configuration associated with this object.
     * @param p the configuration to be associated with this object
     * @see #setConfig
     * @throws CommandContext.Fault if the configuration is incompatible with
     * other parameters that have previously been set up, such as
     * the test suite and work directory.
     * @see #getInterviewParameters
     * @deprecated Use setConfig().
     */
    public void setInterviewParameters(InterviewParameters p)
        throws Fault
    {
        if (isInitConfigRequired())
            initConfig();

        WorkDirectory cwd;
        if (config != null) {
            if (config.getTestSuite() != p.getTestSuite())
                throw new Fault(i18n, "cc.confTestSuiteMismatch",
                                        new Object[] { config.getTestSuite().getRoot() });

            cwd = config.getWorkDirectory();

            WorkDirectory pwd = p.getWorkDirectory();
            if (cwd != null && pwd != null && pwd != cwd)
                throw new Fault(i18n, "cc.confWorkDirMismatch",
                                        new Object[] { cwd.getRoot() });
        }
        else
            cwd = null;

        if (config != null) {
            config.dispose();
        }
        config = p;

        if (config.getWorkDirectory() == null && cwd != null)
            config.setWorkDirectory(cwd);
    }

    private boolean isInitConfigRequired() {
        return (config == null && (testSuitePath != null
                                   || workDirectoryPath != null
                                   || configFilePath != null));
    }

    private void initConfig()
        throws Fault
    {
        if (config != null)
            return;

        /*
        if (testSuitePath == null && workDirectoryPath == null && configFilePath == null)
            throw new Fault(i18n, "cc.noConfig");
        */

        // special case, should correspond to -ts -preferred <path>
        if (testSuitePath != null && workDirectoryPath == null &&
            defaultWorkDirPath != null) {
            workDirectoryPath = new File(defaultWorkDirPath);
        }

        if (workDirectoryPath != null
            && (autoCreateWorkDirectory
                || WorkDirectory.isEmptyDirectory(workDirectoryPath))) {

            // first, determine where the test suite is
            Properties configData;
            File tsPath;

            // get test suite path if we don't have it
            if (testSuitePath == null) {
                if (configFilePath == null)
                    throw new Fault(i18n, "cc.noTestSuite");

                configData = new Properties();

                try {
                    InputStream in = new BufferedInputStream(new FileInputStream(configFilePath));
                    try {
                        configData.load(in);
                    }
                    catch (RuntimeException e) {
                        // can get IllegalArgumentException if the file is corrupt
                        throw new Fault(i18n, "cc.cantReadConfig", new Object[] { configFilePath, e });
                    }
                    finally {
                        in.close();
                    }
                }
                catch (FileNotFoundException e) {
                    throw new Fault(i18n, "cc.cantFindConfig", configFilePath);
                }
                catch (IOException e) {
                    throw new Fault(i18n, "cc.cantReadConfig",
                                            new Object[] { configFilePath, e });
                }

                String tsp = (String) (configData.get("TESTSUITE"));
                if (tsp == null)
                    throw new Fault(i18n, "cc.noTestSuiteInConfigFile", configFilePath);

                tsPath = new File(tsp);
            }
            else {
                configData = null;
                tsPath = testSuitePath;
            }

            // open the test suite
            TestSuite ts;
            try {
                ts = TestSuite.open(tsPath);
            }
            catch (FileNotFoundException e) {
                throw new Fault(i18n, "cc.cantFindTS",
                                        new Object[] { tsPath,
                                                       new Integer(testSuitePath != null ? 0 : 1),
                                                       configFilePath });
            }
            catch (TestSuite.Fault e) {
                throw new Fault(i18n, "cc.cantOpenTS",
                                        new Object[] { tsPath,
                                                       new Integer(testSuitePath != null ? 0 : 1),
                                                       configFilePath,
                                                       e.getMessage() });
            }

            // create the work directory
            WorkDirectory wd;
            try {
                wd = WorkDirectory.create(workDirectoryPath, ts);
            }
            catch (WorkDirectory.Fault e) {
                throw new Fault(i18n, "cc.cantCreateWD", workDirectoryPath);
            }

            // finally, set up the config
            if (configFilePath == null || configData != null) {
                // create empty interview
                try {
                    config = ts.createInterview();
                }
                catch (TestSuite.Fault e) {
                    throw new Fault(i18n, "cc.cantCreateConfig",
                                            new Object[] { testSuitePath, e.getMessage() });
                }

                // load config data if we have it
                try {
                    if (configData != null)
                        config.load(configData, configFilePath);
                }
                catch (InterviewParameters.Fault e) {
                    throw new Fault(i18n, "cc.cantOpenConfig",
                                            new Object[] { configFilePath, e.getMessage() });
                }

                config.setWorkDirectory(wd);
            }
            else {
                try {
                    config = InterviewParameters.open(configFilePath, wd);
                }
                catch (IOException e) {
                    throw new Fault(i18n, "cc.cantReadConfig",
                                            new Object[] { configFilePath, e });
                }
                catch (InterviewParameters.Fault e) {
                    throw new Fault(i18n, "cc.cantOpenConfig",
                                            new Object[] { configFilePath, e.getMessage() });
                }
            }

            // configuration was created without template,
            // it means that there will be no template reference
            // in the work directory
            if (config.getTemplatePath() != null) {
                try {
                    TemplateUtilities.setTemplateFile(
                        config.getWorkDirectory(),
                        new File(config.getTemplatePath()),
                        true);
                } catch (IOException errorWritingTemplateInfo) {
                    throw new Fault(i18n, "cnfg.writeTemplate.cantWriteTemplateRef",
                            workDirectoryPath);
                }
            }

        }
        else {
            // reject an unusable workdir setting
            try {
                config = InterviewParameters.open(testSuitePath,
                                                    workDirectoryPath,
                                                    configFilePath);

                if (config.getWorkDirectory() != null) {
                    File templateFile = TemplateUtilities.getTemplateFile(
                                        config.getWorkDirectory());

                    // must ensure that config has template data if it
                    // exists, usually it is loaded automatically
                    if (config.getTemplatePath() == null &&
                        templateFile != null) {
                        try {
                            InterviewParameters tConfig =
                                config.getTestSuite().loadInterviewFromTemplate(
                                                templateFile, config);
                            if (tConfig != null) {
                                tConfig.setWorkDirectory(
                                        config.getWorkDirectory());
                                if (config != tConfig) {
                                    config.dispose();
                                    config = tConfig;
                                }
                            }
                        }
                        catch (IOException e) {
                            // ignore
                        }
                        catch (TestSuite.Fault f) {
                            // ignore
                        }
                    }   // if
                }
            }   // try
            catch (InterviewParameters.Fault e) {
                throw new Fault(i18n, "cc.cantInitConfig", e.getMessage());
            }
        }

        if (testSuitePath == null)
            testSuitePath = config.getTestSuite().getRoot();

        if (workDirectoryPath == null && config.getWorkDirectory() != null)
            workDirectoryPath = config.getWorkDirectory().getRoot();

        if (configFilePath == null)
            configFilePath = config.getFile();
    }

    /**
     * Check if a desktop is required by the commands registered with this object.
     * A desktop is not required if and only if one or more commands have a desktop mode of
     * "desktop not required", and none have a mode of "desktop required".
     * In other words, "desktop not required" wins over "default", but "desktop required"
     * wins over "desktop not required".
     * @return whether or not a desktop is required by the commands registered with this object
     */
    public boolean isDesktopRequired() {
        int mode = Command.DEFAULT_DTMODE;
        for (int i = 0; i < commands.size(); i++) {
            Command cmd = (Command) (commands.elementAt(i));
            mode = Math.max(mode, cmd.getDesktopMode());
        }
        return (mode == Command.DESKTOP_NOT_REQUIRED_DTMODE ? false : true);

    }

    /**
     * Set the desktop associated with this object.
     * @param d the desktop to be associated with this object
     * @throws NullPointerException if the argument is null
     * @see #getDesktop
     */
    public void setDesktop(Desktop d) {
        if (d == null)
            throw new NullPointerException();
        desktop = d;
    }

    /**
     * Get the desktop associated with this object.
     * @return the desktop associated with this object
     * @see #setDesktop
     */
    public Desktop getDesktop() {
        return desktop;
    }


    //-------------------------------------------------------------------------

    /**
     * Specify whether or not to override the setting of all other
     * verbose options to true.
     * @param on If true, the value of all other verbose options will
     * be given as true.
     */
    public void setVerboseMax(boolean on) {
        verboseMax = on;
    }

    /**
     * Specify whether or not to override the setting of all other
     * verbose options to false.
     * @param on If true, the value of all other verbose options will
     * be given as false.
     */
    public void setVerboseQuiet(boolean on) {
        verboseQuiet = on;
    }

    /**
     * Should all verbose output be quieted.
     * This generally overrides any other requests for verbosity.
     * @return True if verboseness should be quieted.
     */
    public boolean isVerboseQuiet() {
        return verboseQuiet;
    }

    /**
     * Configure whether timestamps are printed with verbose output.
     * @param on False for no timestamps.
     */
    public void setVerboseTimestampEnabled(boolean on) {
        verboseDate = on;
    }

    /**
     * Specify the value of a verbose option.
     * @param name the name of the verbose option
     * @param on the value of the verbose option
     * @see #getVerboseOptionValue
     */
    public void setVerboseOptionValue(String name, boolean on) {
        verboseOptionValues.put(name.toLowerCase(), on);
    }

    /**
     * Get the value of a verbose option.
     * If the max verbose flag has been set to true, the result will be true.
     * Otherwise, if the quiet verbose flag, the result will be false.
     * Otherwise, if the value has been set with setVerboseOptionValue,
     * the result will be the value that was set
     * Otherwise the result will be false.
     * @param name the name of the verbose option
     * @return the value of the named option
     * @see #setVerboseOptionValue
     */
    public boolean getVerboseOptionValue(String name) {
        return getVerboseOptionValue(name, false);
    }

    /**
     * Get the value of a verbose option.
     * If the max verbose flag has been set to true, the result will be true.
     * Otherwise, if the quiet verbose flag, the result will be false.
     * Otherwise, if the value has been set with setVerboseOptionValue,
     * the result will be the value that was set
     * Otherwise the result will be specified default value.
     * @param name the name of the verbose option
     * @param defaultValue the default value to be used if necessary
     * @return the value of the named option
     * @see #setVerboseOptionValue
     */
    public boolean getVerboseOptionValue(String name, boolean defaultValue) {
        if (verboseMax)
            return true;

        if (verboseQuiet)
            return false;

        Boolean b = (Boolean) (verboseOptionValues.get(name.toLowerCase()));
        return (b == null ? defaultValue : b.booleanValue());
    }

    /**
     * Check if a verbose option has been set explicitly with
     * setVerboseOptionValue.
     * @param name the name of the option to be checked
     * @return true if the option has a value that has been explicitly set,
     * and false otherwise
     */
    public boolean isVerboseOptionSet(String name) {
        return (verboseOptionValues.get(name.toLowerCase()) != null);
    }

    /**
     * Check whether timestamps should be printed with verbose output.
     * @return False for no timestamps.
     * @see #setVerboseTimestampEnabled
     */
    public boolean isVerboseTimestampEnabled() {
        return verboseDate;
    }

    /**
     * Sets preferred LookAndFeel that is used on Desktop creation (should be set before creation)
     * @param lookAndFeel LookAndFeel code to be set
     * @see #getPreferredLookAndFeel()
     * @see #DEFAULT_LAF
     * @see #METAL_LAF
     * @see #NIMBUS_LAF
     * @see #SYSTEM_LAF
     */
    public void setPreferredLookAndFeel(int lookAndFeel) {
        preferredLAF = lookAndFeel;
    }

    /**
     * @return preferred LookAndFeel (nimbus by default)
     * @see #setPreferredLookAndFeel(int)
     * @see #DEFAULT_LAF
     * @see #METAL_LAF
     * @see #NIMBUS_LAF
     * @see #SYSTEM_LAF
     */
    public int getPreferredLookAndFeel() {
        return preferredLAF;
    }

    /**
     * Set the log stream associated with this object.
     * @param out  the log stream to be associated with this object
     * @see #getLogWriter
     */
    public void setLogWriter(PrintWriter out) {
        this.out = out;
    }

    /**
     * Get the log stream associated with this object.
     * @return  the log stream associated with this object
     * @see #setLogWriter
     */
    public PrintWriter getLogWriter() {
        return out;
    }

    /**
     * Write a message to the log stream associated with this object.
     * @param i18n the resource bundle containing the localized text of the message
     * @param key the key for the required message in the bundle
     * @see #setLogWriter
     */
    public void printMessage(I18NResourceBundle i18n, String key) {
        out.println(i18n.getString(key));
    }

    /**
     * Write a message to the log stream associated with this object.
     * @param i18n the resource bundle containing the localized text of the message
     * @param key the key for the required message in the bundle
     * @param arg an argument to be formatted into the localized message
     * @see #setLogWriter
     */
    public void printMessage(I18NResourceBundle i18n, String key, Object arg) {
        out.println(i18n.getString(key, arg));
    }

    /**
     * Write a message to the log stream associated with this object.
     * @param i18n the resource bundle containing the localized text of the message
     * @param key the key for the required message in the bundle
     * @param args an array of arguments to be formatted into the localized message
     * @see #setLogWriter
     */
    public void printMessage(I18NResourceBundle i18n, String key, Object[] args) {
        out.println(i18n.getString(key, args));
    }

    /**
     * Write an error message to the log stream associated with this object.
     * @param i18n the resource bundle containing the localized text of the message
     * @param key the key for the required message in the bundle
     * @see #setLogWriter
     */
    public void printErrorMessage(I18NResourceBundle i18n, String key) {
        out.println(i18n.getString(key));
    }

    /**
     * Write an error message to the log stream associated with this object.
     * @param i18n the resource bundle containing the localized text of the message
     * @param key the key for the required message in the bundle
     * @param arg an argument to be formatted into the localized message
     * @see #setLogWriter
     */
    public void printErrorMessage(I18NResourceBundle i18n, String key, Object arg) {
        out.println(i18n.getString(key, arg));
    }

    /**
     * Write an error message to the log stream associated with this object.
     * @param i18n the resource bundle containing the localized text of the message
     * @param key the key for the required message in the bundle
     * @param args an array of arguments to be formatted into the localized message
     * @see #setLogWriter
     */
    public void printErrorMessage(I18NResourceBundle i18n, String key, Object[] args) {
        out.println(i18n.getString(key, args));
    }


    public void dispose() {
        if (commands != null) {
            commands.clear();
        }
        if (desktopData != null) {
            desktopData.clear();
        }
        if (config != null) {
            config.dispose();
            config = null;
        }

    }

    //-------------------------------------------------------------------------


    private Vector commands = new Vector();

    //private TestSuite testSuite;
    //private WorkDirectory workDir;
    private File testSuitePath;
    private File workDirectoryPath;
    private String defaultWorkDirPath;
    private Map desktopData;
    private boolean autoCreateWorkDirectory;
    private File configFilePath;

    private InterviewParameters config;
    private Command autoRunCommand;
    private File autoRunReportDir;
    private boolean closeDesktopWhenDoneEnabled;
    private Desktop desktop;
    private PrintWriter out;
    private int[] cumulativeTestStats = new int[Status.NUM_STATES];

    private HashMap verboseOptionValues = new HashMap(); // HashMap<String, Boolean>
    private boolean verboseMax;
    private boolean verboseQuiet;
    private boolean verboseDate = true;

    private int preferredLAF = DEFAULT_LAF;

    private Harness.Observer[] harnessObservers = new Harness.Observer[0];

    private static final I18NResourceBundle i18n;
    static final String VERBOSE_COMMANDS = "commands";

    /**
     * Code for setting default system LookAndFeel. Should be set before Desktop is created.
     * @see #getPreferredLookAndFeel()
     * @see #setPreferredLookAndFeel(int)
     * @see #DEFAULT_LAF
     * @see #METAL_LAF
     * @see #NIMBUS_LAF
     */
    public static final int SYSTEM_LAF = 0;
    /**
     * Code for setting Nimbus LookAndFeel. Should be set before Desktop is created.
     * @see #getPreferredLookAndFeel()
     * @see #setPreferredLookAndFeel(int)
     * @see #DEFAULT_LAF
     * @see #METAL_LAF
     * @see #SYSTEM_LAF
     */
    public static final int NIMBUS_LAF = 1;
    /**
     * Code for setting Metal LookAndFeel. Should be set before Desktop is created.
     * @see #getPreferredLookAndFeel()
     * @see #setPreferredLookAndFeel(int)
     * @see #DEFAULT_LAF
     * @see #NIMBUS_LAF
     * @see #SYSTEM_LAF
     */
    public static final int METAL_LAF = 2;
    /**
     * Code for setting JavaTest default LookAndFeel. Currently - Nimbus LookAndFeel is default. Should be set before Desktop is created.
     * @see #getPreferredLookAndFeel()
     * @see #setPreferredLookAndFeel(int)
     * @see #METAL_LAF
     * @see #NIMBUS_LAF
     * @see #SYSTEM_LAF
     */
    public static final int DEFAULT_LAF = 1;

    static {
        i18n = I18NResourceBundle.getBundleForClass(CommandContext.class);
        VerboseCommand.addOption(VERBOSE_COMMANDS, new HelpTree.Node(i18n, "cc.verbose"));
    }

    static final String TRACE_PREFIX = "+ ";
}
