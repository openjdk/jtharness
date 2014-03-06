/*
 * $Id$
 *
 * Copyright (c) 2001, 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

import com.sun.interview.ErrorQuestion;
import com.sun.interview.FinalQuestion;
import com.sun.interview.Interview;
import com.sun.interview.Question;
import com.sun.javatest.tool.CustomPropagationController;
import com.sun.javatest.tool.FileHistory;
import com.sun.javatest.util.BackupPolicy;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.SortedProperties;

/**
 * Configuration parameters provided via an interview.
 *
 * @since 3.0
 */
public abstract class InterviewParameters
    extends Interview
    implements Parameters
{

    /**
     * Indicates problems when accessing the work directory.
     */
    public static class WorkDirFault extends Interview.Fault {
        /**
         * Create a fault with an internationalized message.
         * @param i18n The bundle from which to get the string.
         * @param s The key for getting the string to be displayed from the
         *          supplied bundle.
         */
        public WorkDirFault(ResourceBundle i18n, String s) {
            super(i18n, s);
        }

        /**
         * Create a fault with an internationalized message.
         * @param i18n The bundle from which to get the string.
         * @param s The key for getting the string to be displayed from the
         *          supplied bundle.
         * @param o Parameter to use when resolving the string from the bundle.
         * @see java.text.MessageFormat
         */
        public WorkDirFault(ResourceBundle i18n, String s, Object o) {
            super(i18n, s, o);
        }

        /**
         * Create a fault with an internationalized message.
         * @param i18n The bundle from which to get the string.
         * @param s The key for getting the string to be displayed from the
         *          supplied bundle.
         * @param o Parameters to use when resolving the string from the bundle.
         * @see java.text.MessageFormat
         */
        public WorkDirFault(ResourceBundle i18n, String s, Object[] o) {
            super(i18n, s, o);
        }
    }

    /**
     * Indicates problems when accessing the test suite.
     */
    public static class TestSuiteFault extends Interview.Fault {
        /**
         * Create a fault with an internationalized message.
         * @param i18n The bundle from which to get the string.
         * @param s The key for getting the string to be displayed from the
         *          supplied bundle.
         */
        public TestSuiteFault(ResourceBundle i18n, String s) {
            super(i18n, s);
        }

        /**
         * Create a fault with an internationalized message.
         * @param i18n The bundle from which to get the string.
         * @param s The key for getting the string to be displayed from the
         *          supplied bundle.
         * @param o Parameter to use when resolving the string from the bundle.
         * @see java.text.MessageFormat
         */
        public TestSuiteFault(ResourceBundle i18n, String s, Object o) {
            super(i18n, s, o);
        }

        /**
         * Create a fault with an internationalized message.
         * @param i18n The bundle from which to get the string.
         * @param s The key for getting the string to be displayed from the
         *          supplied bundle.
         * @param o Parameters to use when resolving the string from the bundle.
         * @see java.text.MessageFormat
         */
        public TestSuiteFault(ResourceBundle i18n, String s, Object[] o) {
            super(i18n, s, o);
        }
    }

    /**
     * Indicates problems when accessing the configuration file.
     */
    public static class JTIFault extends Interview.Fault {
        /**
         * Create a fault with an internationalized message.
         * @param i18n The bundle from which to get the string.
         * @param s The key for getting the string to be displayed from the
         *          supplied bundle.
         */
        public JTIFault(ResourceBundle i18n, String s) {
            super(i18n, s);
        }

        /**
         * Create a fault with an internationalized message.
         * @param i18n The bundle from which to get the string.
         * @param s The key for getting the string to be displayed from the
         *          supplied bundle.
         * @param o Parameter to use when resolving the string from the bundle.
         * @see java.text.MessageFormat
         */
        public JTIFault(ResourceBundle i18n, String s, Object o) {
            super(i18n, s, o);
        }

        /**
         * Create a fault with an internationalized message.
         * @param i18n The bundle from which to get the string.
         * @param s The key for getting the string to be displayed from the
         *          supplied bundle.
         * @param o Parameters to use when resolving the string from the bundle.
         * @see java.text.MessageFormat
         */
        public JTIFault(ResourceBundle i18n, String s, Object[] o) {
            super(i18n, s, o);
        }
    }

    /**
     *  The template manager is used to change behavior of
     *  template saving, the default implementation is
     *  the context manager of corresponding test suite.
     */
    public interface TemplateManager {
        /**
         * This method is invoked each time before saving template.
         * The template will be saved only if this method returns true.
         * @param file template file
         * @return true if this operation is allowed, false otherwise
         */
        public boolean canSaveTemplate(File file);
    }

    public void setPropagationController(CustomPropagationController pc) {
        this.pc = pc;
     }

    public CustomPropagationController getPropagationController() {
        return pc;
    }

    private TemplateManager templateManager = null;
    /**
     * Create an InterviewParameters object.
     * @param tag The tag used to qualify questions in this interview
     */
    protected InterviewParameters(String tag) {
        super(tag);
    }

    /**
     * Set the work directory to be used for this test run.
     * @param workDir the work directory to be used for this test run.
     * It must match the test suite to be used for this test run
     */
    public abstract void setWorkDirectory(WorkDirectory workDir);

    /**
     * Set given template manager for this InterviewParameters.
     * @param tm new template manager
     */
    public void setTemplateManger(TemplateManager tm) {
        this.templateManager = tm;
    }

    /**
     * Return the template manager for this InterviewParameters.
     */
    public TemplateManager getTemplateManger() {
        return templateManager;
    }

    /**
     * Initialize an InterviewParameters object.
     * This method is called when the object is created
     * from an entry in a .jtt file.
     * By default, the method throws an exception if any arguments
     * are given. It should be redefined by any test suites that wish
     * to support this type of initialization.
     * @param args test suite specific args with which to initialize
     * this InterviewParameters object
     * @throws Interview.Fault if any problems occurred while processing the arguments
     */
    public void init(String[] args) throws Fault {
        if (args != null && args.length > 0)
            throw new Fault(i18n, "ip.unknownArgs");
    }

    /**
     * Clean up an InterviewParameters object.
     * This method should be invoked at the moment InterviewParameters object
     * becomes useless by the code, that controls it's lifecycle. For example,
     * at the end of the method which created it's local instance.
     *
     * Any following invocations on this object may result in unpredictable
     * exceptions because of object inconsistence.
     */
    public void dispose() {
        kflFiles = null;
        backupPolicy = null;
        cachedExcludeListFilter = null;
        cachedKeywordsFilter = null;
        cachedRelevantTestFilter = null;
        cachedRelevantTestFilterEnv = null;
        cachedStatusFilter = null;
        cachedTestFilters = null;
        pc = null;
        templateManager = null;
    }

    //----------------------------------------------------------------------------

    /**
     * Get the name for this configuration.
     * By default and for backwards compatibility, this defaults to the
     * name of the test environment, which means that the whole environment
     * may need to be evaluated to get the required value. Subtypes may
     * choose to override this method to provide a more efficient
     * implementation.
     *
     * <p> Since the default implementation gets the name from the test
     * environment, clients should not use this method to determine
     * the name for the test environment, unless this method is redefined.
     * The default implementation detects such a circular usage, and
     * returns null in this case.
     *
     * @return the name for this configuration, or null if not known.
     */
    public synchronized String getName() {
        if (inGetName)
            return null;

        try {
            inGetName = true;

            EnvParameters eParams = getEnvParameters();
            if (eParams != null) {
                // getName to get the name for the environment
                TestEnvironment e = eParams.getEnv();
                if (e != null)
                    return e.getName();
            }
            return null;
        }
        finally {
            inGetName = false;
        }
    }

    private boolean inGetName;

    /**
     * Get a description for this configuration.
     * By default and for backwards compatibility, this defaults to the
     * description entry in the test environment, which means that the
     * whole environment may need to be evaluated to get the required value.
     * Subtypes may choose to override this method to provide a more efficient
     * implementation.
     * @return a description for this configuration, or null if not known
     */
    public String getDescription() {
        EnvParameters eParams = getEnvParameters();
        if (eParams != null) {
            TestEnvironment e = eParams.getEnv();
            if (e != null)
                return e.getDescription();
        }
        return null;
    }


    //----------------------------------------------------------------------------

    /**
     * Get the next question to the asked after the initial prolog
     * of questions.
     * The default value is the result of getEnvFirstQuestion.
     * @return the next question to be asked after the initial prolog
     * of questions.
     * @see #setFirstQuestion
     */
    protected Question getPrologSuccessorQuestion() {
        return getEnvFirstQuestion();
    }

    //----------------------------------------------------------------------------


    public TestEnvironment getEnv() {
        EnvParameters eParams = getEnvParameters();
        if (eParams == null)
            throw new NullPointerException();
        else
            return eParams.getEnv();
    }

    /**
     * Get the first question to be asked concerning the environment to be
     * set up and used for each test to be run. If these questions are
     * contained in an interview, this method can be simply implemented as:<br>
     *  <code>return callInterview(</code><i>envInterview</i><code>, getEnvSuccessorQuestion);</code><br>
     * @return the first question to be asked concerning the environment to be
     * set up and used for each test to be run.
     * @see #getEnvSuccessorQuestion
     */
    protected abstract Question getEnvFirstQuestion();

    /**
     * Get the next question to be asked after those concerning
     * the environment to be set up and used for each test to be run.
     * The default value is the result of getTestsFirstQuestion.
     * @return the next question to be asked after those concerning
     * the environment to be set up and used for each test to be run.
     * @see #getEnvFirstQuestion
     */
    protected Question getEnvSuccessorQuestion() {
        return getTestsFirstQuestion();
    }

    public String[] getTests() {
        TestsParameters iParams = getTestsParameters();
        return (iParams == null ? null : iParams.getTests());
    }

    /**
     * Get the first question to be asked concerning the set of tests
     * and folders of tests to be run.
     * @return the first question to be asked concerning the set of tests
     * and folders of tests to be run.
     * @see #getTestsSuccessorQuestion
     */
    protected abstract Question getTestsFirstQuestion();

    /**
     * Get the next question to be asked after those concerning
     * the tests and folders of tests to be run.
     * The default value is the result of getExcludeListFirstQuestion.
     * @return the next question to be asked after those concerning
     * the tests and folders of tests to be run.
     * @see #getTestsFirstQuestion
     */
    protected Question getTestsSuccessorQuestion() {
        return getExcludeListFirstQuestion();
    }

    public ExcludeList getExcludeList() {
        ExcludeListParameters eParams = getExcludeListParameters();
        return (eParams == null ? new ExcludeList() : eParams.getExcludeList());
    }

    /**
     * Get the combined known failures list.
     * Interviews expecting to use known failures lists should generally override
     * this method and add support for users to change it.
     * @since 4.4
     * @see #setKnownFailureFiles(java.io.File[])
     * @see com.sun.javatest.interview.BasicInterviewParameters
     * @return Current known failures list - combined from the one or more
     *    file specified by the user.
     */
    public KnownFailuresList getKnownFailuresList() {
        try {
            if (kflFiles != null) {
            return new KnownFailuresList(getKnownFailureFiles());
        }
            else {
                return null;
            }

        }
        catch (IOException e){
            return null;
        }
        catch (KnownFailuresList.Fault f) {
            // report it?
            return null;
        }
    }

    /**
     * Set the set of KFL files.
     * @since 4.4
     * @param files The known failures list files.  The array should contain
     *     one or more elements.
     */
    public void setKnownFailureFiles(File[] files) {
        kflFiles = files;
    }

    /**
     * Get the current set of known failures list files.
     * The default implementation will return the value in the kflFiles
     * field, which subclasses may set.
     * @since 4.4
     * @see #setKnownFailureFiles(java.io.File[])
     * @return The list of known failure list files.  Null if none.
     */
    public File[] getKnownFailureFiles() {
        return kflFiles;
    }

    /**
     * Get the first question to be asked concerning the exclude list
     * to be used to exclude tests from the test run.
     * @return the first question to be asked concerning the exclude list
     * to be used to exclude tests from the test run.
     * @see #getExcludeListSuccessorQuestion
     */
    protected abstract Question getExcludeListFirstQuestion();

    /**
     * Get the first question to be asked concerning the exclude list
     * to be used to exclude tests from the test run.
     * @return the first question to be asked concerning the exclude list
     * to be used to exclude tests from the test run
     * @deprecated Use getExcludeListFirstQuestion().
     * @see #getExcludeListFirstQuestion
     */
    protected Question getExcludeTableFirstQuestion() {
        return getExcludeListFirstQuestion();
    }

    /**
     * Get the next question to be asked after those concerning
     * the exclude list to be used to exclude tests from the test run.
     * The default value is the result of getKeywordsFirstQuestion,
     * @return the next question to be asked after those concerning
     * the exclude list to be used to exclude tests from the test run.
     * @see #getExcludeListFirstQuestion
     */
    protected Question getExcludeListSuccessorQuestion() {
        return getKeywordsFirstQuestion();
    }

    /**
     * Get the next question to be asked after those concerning
     * the exclude list to be used to exclude tests from the test run.
     * @return the next question to be asked after those concerning
     * the exclude list to be used to exclude tests from the test run
     * @deprecated Use getExcludeListFirstQuestion().
     * @see #getExcludeListSuccessorQuestion
     */
    protected Question getExcludeTableSuccessorQuestion() {
        return getExcludeListSuccessorQuestion();
    }

    public Keywords getKeywords() {
        KeywordsParameters kParams = getKeywordsParameters();
        return (kParams == null ? null : kParams.getKeywords());
    }

    /**
     * Get the first question to be asked concerning the keywords
     * that may be used to select tests for the test run.
     * @return the first question to be asked concerning the keywords
     * that may be used to select tests for the test run.
     * @see #getKeywordsSuccessorQuestion
     */
    protected abstract Question getKeywordsFirstQuestion();

    /**
     * Get the next question to be asked after those concerning
     * the keywords that may be used to select tests for the test run.
     * The default value is the result of getPriorStatusQuestion.
     * @return the next question to be asked after those concerning
     * the keywords that may be used to select tests for the test run.
     * @see #getKeywordsFirstQuestion
     */
    protected Question getKeywordsSuccessorQuestion() {
        return getPriorStatusFirstQuestion();
    }

    public boolean[] getPriorStatusValues() {
        PriorStatusParameters sParams = getPriorStatusParameters();
        return (sParams == null ? null : sParams.getPriorStatusValues());
    }

    /**
     * Get the first question to be asked concerning whether tests should
     * be selected for execution according to their prior execution status.
     * @return the first question to be asked concerning whether tests should
     * be selected for execution according to their prior execution status.
     * @see #getPriorStatusSuccessorQuestion
     */
    protected abstract Question getPriorStatusFirstQuestion();

    /**
     * Get the next question to be asked after those concerning
     * whether tests should be selected for execution according to their
     * prior execution status.
     * The default value is the result of getConcurrencyFirstQuestion
     * @return the next question to be asked after those concerning
     * whether tests should be selected for execution according to their
     * prior execution status.
     * @see #getPriorStatusFirstQuestion
     */
    protected Question getPriorStatusSuccessorQuestion() {
        return getConcurrencyFirstQuestion();
    }

    public int getConcurrency() {
        ConcurrencyParameters cParams = getConcurrencyParameters();
        return (cParams == null ? 1 : cParams.getConcurrency());
    }

    /**
     * Get the first question concerning the number of tests that
     * may be run in parallel.
     * @return the first question concerning the number of tests that
     * may be run in parallel.
     * @see #getConcurrencySuccessorQuestion
     */
    protected abstract Question getConcurrencyFirstQuestion();

    /**
     * Get the next question after those concerning the number
     * of tests that may be run in parallel.
     * The default is the result of getTimeoutFactorFirstQuestion
     * @return the next question after those concerning the number
     * of tests that may be run in parallel.
     * @see #getConcurrencyFirstQuestion
     */
    protected Question getConcurrencySuccessorQuestion() {
        return getTimeoutFactorFirstQuestion();
    }

    public float getTimeoutFactor() {
        TimeoutFactorParameters tParams = getTimeoutFactorParameters();
        return (tParams == null ? 1 : tParams.getTimeoutFactor());
    }

    /**
     * Get the first question concerning the scale factor to
     * be applied to the standard timeout for each test.
     * @return the first question concerning the scale factor to
     * be applied to the standard timeout for each test.
     * @see #getTimeoutFactorSuccessorQuestion
     */
    protected abstract Question getTimeoutFactorFirstQuestion();

    /**
     * Get the next question after those concerning the scale factor to
     * be applied to the standard timeout for each test.
     * The default is the result of getEpilogFirstQuestion
     * @return the next question after those concerning the scale factor to
     * be applied to the standard timeout for each test.
     * @see #getTimeoutFactorFirstQuestion
     */
    protected Question getTimeoutFactorSuccessorQuestion() {
        return getEpilogFirstQuestion();
    }

    /**
     * Get the first question of the epilog, which should be asked after
     * all the other questions in the configuration interview have been asked.
     * The epilog should terminate in the standard way with a FinalQuestion.
     * @return the first question of the epilog, which should be asked after
     * all the other questions in the configuration interview have been asked.
     */
    protected abstract Question getEpilogFirstQuestion();

    //----------------------------------------------------------------------------

    /**
     * Determine whether all the configuration values are valid, by
     * checking if the interview has been completed.
     * If so, the result will be true; if not, the result will be false,
     * and getErrorMessage will provide details about at least one of the
     * invalid values.
     * @return true if and only if all the configuration values are valid
     * @see #getErrorMessage
     * @see #isFinishable
     */
    public boolean isValid() {
        return isFinishable();
    }

    /**
     * If there is an error in any of the configuration values,
     * as indicated by isValid, this method will provide a detail
     * message about the first question for which there is a problem.
     * @return a detail message about the first question with an invalid answer,
     * or null if none.
     * @see #isValid
     */
    public String getErrorMessage() {
        Question[] path = getPath();
        Question lastQuestion = path[path.length - 1];
        if (lastQuestion instanceof FinalQuestion)
            return null;
        else if (lastQuestion instanceof ErrorQuestion)
            return lastQuestion.getText();
        else {
            String v = lastQuestion.getStringValue();
            return i18n.getString("ip.noAnswer",
                                  new Object[] { lastQuestion.getSummary(),
                                                 lastQuestion.getText(),
                                                 lastQuestion.getTag(),
                                                 new Integer(v == null ? 0 : 1),
                                                 trim(v),
                                  } );
        }
    }

    private String trim(String text) {
        return (text == null ? null
                : text.length() < 40 ? text
                : text.substring(0, 37) + "...");
    }


    //----------------------------------------------------------------------------

    /**
     * Get a filter which will filter tests according to the result
     * of getExcludeList(). If the result of getExcludeList is null
     * or an empty exclude list, the result of this method will also be null.
     * @return a filter which will filter tests according to the result
     * of getExcludeList()
     * @deprecated Use getExcludeListFilter().
     * @see #getExcludeListFilter
     */
    public TestFilter getExcludeTableFilter() {
        return getExcludeListFilter();
    }

    public TestFilter getExcludeListFilter() {
        ExcludeList t = getExcludeList();
        if (t == null)
            cachedExcludeListFilter = null;
        else if (cachedExcludeListFilter == null
                 || cachedExcludeListFilter.getExcludeList() != t)
            cachedExcludeListFilter = new ExcludeListFilter(t);
        return cachedExcludeListFilter;
    }

    private ExcludeListFilter cachedExcludeListFilter;

    public TestFilter getKeywordsFilter() {
        Keywords k = getKeywords();
        if (k == null)
            cachedKeywordsFilter = null;
        else if (cachedKeywordsFilter == null
                 || cachedKeywordsFilter.getKeywords() != k)
            cachedKeywordsFilter = new KeywordsFilter(k);
        return cachedKeywordsFilter;
    }

    private KeywordsFilter cachedKeywordsFilter;

    public TestFilter getPriorStatusFilter() {
        WorkDirectory wd = getWorkDirectory();
        TestResultTable r = (wd == null ? null : wd.getTestResultTable());
        boolean[] s = getPriorStatusValues();
        if (r == null || s == null)
            cachedStatusFilter = null;
        else if (cachedStatusFilter == null
                 || cachedStatusFilter.getTestResultTable() != r
                 || !equal(cachedStatusFilter.getStatusValues(), s))
            cachedStatusFilter = new StatusFilter(s, r);
        // else
        //   cachedStatusFilter is OK

        return cachedStatusFilter;
    }

    private StatusFilter cachedStatusFilter;

    public TestFilter getRelevantTestFilter() {
        TestSuite ts = getTestSuite();
        TestEnvironment env = getEnv();
        if (ts == null || env == null)
            cachedRelevantTestFilter = null;
        else if (cachedRelevantTestFilter == null ||
                 ts != cachedRelevantTestFilterTestSuite ||
                 env != cachedRelevantTestFilterEnv) {
            cachedRelevantTestFilter = ts.createTestFilter(env);
        }
        return cachedRelevantTestFilter;
    }

    private TestFilter cachedRelevantTestFilter;
    private TestSuite cachedRelevantTestFilterTestSuite; // do we need this?
    private TestEnvironment cachedRelevantTestFilterEnv;

    public synchronized TestFilter[] getFilters() {
        Vector v = new Vector();

        TestFilter excludeFilter = getExcludeListFilter();
        if (excludeFilter != null)
            v.addElement(excludeFilter);

        TestFilter keywordFilter = getKeywordsFilter();
        if (keywordFilter != null)
            v.addElement(keywordFilter);

        TestFilter statusFilter = getPriorStatusFilter();
        if (statusFilter != null)
            v.addElement(statusFilter);

        TestFilter testSuiteFilter = getRelevantTestFilter();
        if (testSuiteFilter != null)
        v.addElement(testSuiteFilter);

        if (v.size() == 0)
            return null;
        else if (equal(v, cachedTestFilters))
            return cachedTestFilters;
        else {
            TestFilter[] filters = new TestFilter[v.size()];
            v.copyInto(filters);
            return filters;
        }

    }

    private static boolean equal(boolean[] b1, boolean[] b2) {
        if (b1 == null || b2 == null)
            return (b1 == b2);

        if (b1.length != b2.length)
            return false;

        for (int i = 0; i < b1.length; i++) {
            if (b1[i] != b2[i])
                return false;
        }

        return true;
    }

    private static boolean equal(Vector v, TestFilter[] f) {
        if (f == null || v.size() != f.length)
            return false;
        for (int i = 0; i < v.size(); i++) {
            if (!v.elementAt(i).equals(f[i]))
                return false;
        }
        return true;
    }

    private TestFilter[] cachedTestFilters;

    //----------------------------------------------------------------------------

    /**
     * Determine if the current instance is a template or not.
     * @return true if the current instance is a template,
     * and false otherwise
     */
    public boolean isTemplate() {
        return isTemplate;
    }

    /**
     * Set if the current instance is a template or not.
     * <b>For internal use only, architects should not use this.</b>
     */
    public void setTemplate(boolean tm) {
        isTemplate = tm;
        updatePath();
    }

    protected boolean isAutoUpdatableKey(String key) {
        return isAutoUpdatableKey(key, null);
    }

    protected boolean isAutoUpdatableKey(String key, String subkey) {
        return false;
    }

    protected boolean isUpdatableKey(String key) {
        return true;
    }


    /**
     * Return String path to the template file for the current instance.
     * If the current instance is a template (isTemplate() returns true),
     * the path to itself will be returned.
     * @return String path to the template file, or null if the instance is
     * not template-based
     */
    public String getTemplatePath() {
        if (isTemplate()) {
            File f = getFile();
            if (f != null) {
                return f.getPath();
            }
        }
        return templatePath;
    }

    /**
     * Set the location of a configuration's master template.
     * Do not change this value if this instance is a template.
     */
    public void setTemplatePath(String tu) {
        templatePath = tu;
    }



    /**
     * Get the file associated with this interview.
     * @return the file associated with this interview.
     * @see #setFile
     * @see #load
     * @see #save
     */
    public File getFile() {
        return currFile;
    }

    /**
     * Set the file associated with this interview. This file will be used
     * by subsequent load and save operations.
     * @param f The file to be associated with this interview.
     * @see #getFile
     * @see #load
     * @see #save
     */
    public void setFile(File f) {
        currFile = f;
        currFileLoaded = false;
        if (f != null) {
            currFileLastModified = f.lastModified();
        }
        else {
            // means: unknown; will likely a trigger a reload
            currFileLastModified = 0;
        }
    }

    /**
     * Determine if the specified file is an interview file,
     * as determined by whether its extension is .jti or not.
     * @param f the file to be checked
     * @return true if the specified file is an interview file,
     * and false otherwise
     */
    public static boolean isInterviewFile(File f) {
        return (f.getName().endsWith(".jti"));
    }

    /**
     * Create an InterviewParameters as determined by the contents of an
     * interview file.
     * @param file the file to be read
     * @return an InterviewParameters as determined by the contents of an
     * interview file.
     * @throws IOException is there is a problem reading the file
     * @throws Interview.Fault if there is a problem instantiating the
     * interview
     */
    public static InterviewParameters open(File file)
        throws IOException, Fault
    {
        return open(file, (TestSuite) null, (WorkDirectory) null);
    }

    /**
     * Create an InterviewParameters by populating the interview for a specified
     * test suite with responses from a given file.
     * @param file the file to be read
     * @param testSuite the test suite for which to create the interview
     * @return an InterviewParameters as determined by the test suite
     * and the contents of an interview file
     * @throws IOException is there is a problem reading the file
     * @throws Interview.Fault if there is a problem instantiating the
     * interview
     */
    public static InterviewParameters open(File file, TestSuite testSuite)
        throws IOException, Fault
    {
        if (testSuite == null)
            throw new NullPointerException();

        return open(file, testSuite, null);
    }

    /**
     * Create an InterviewParameters by populating the interview for a specified
     * work directory with responses from a given file.
     * @param file the file to be read
     * @param workDir the work directory (implying the test suite) for which
     * to create the interview
     * @return an InterviewParameters as determined by the work directory
     * and the contents of an interview file
     * @throws IOException is there is a problem reading the file
     * @throws Interview.Fault if there is a problem instantiating the
     * interview
     */
    public static InterviewParameters open(File file, WorkDirectory workDir)
        throws IOException, Fault
    {
        if (workDir == null)
            throw new NullPointerException();

        return open(file, workDir.getTestSuite(), workDir);
    }

    /**
     * @throws WorkDirFault If there is a problem finding the work directory.
     * @throws TestSuiteFault If there is a problem finding the test suite.
     * @throws JTIFault If there is a problem finding the JTI file.  Not thrown
     *                  if the file is corrupt or incompatible though.
     * @throws Fault If there is any other problem opening the interview params, such as
     *               problems with data in the JTI, incompatibilities between the workdir,
     *               test suite or work dir.
     */
    private static InterviewParameters open(File file, TestSuite testSuite, WorkDirectory workDir)
        throws IOException, Fault
    {
        // note: the additional Fault types were introduced in JT 3.2.1

        // read the .jti data
        Properties data = new Properties();

        InputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            data.load(in);
        }
        catch (RuntimeException e) {
            // can get IllegalArgumentException if the file is corrupt
            throw new JTIFault(i18n, "ip.errorReadingFile", new Object[] { file, e });
        }
        finally {
            in.close();
        }

        // if the test suite has not been given, set it from the .jti data
        if (testSuite == null) {
            String s = (String) (data.get(TESTSUITE));
            if (s == null)
                throw new Fault(i18n, "ip.noTestSuiteInFile", file);

            try {
                testSuite = TestSuite.open(new File(s));
            }
            catch (FileNotFoundException e) {
                throw new TestSuiteFault(i18n, "ip.cantFindTestSuiteInFile",
                                new Object[] { s, file });
            }
            catch (TestSuite.Fault e) {
                throw new Fault(i18n, "ip.cantOpenTestSuiteInFile",
                                new Object[] { s, file, e.getMessage() } );
            }
        }

        // if the work directory has not been given,
        // set it from the .jti data if given
        if (workDir == null) {
            String s = (String) (data.get(WORKDIR));
            if (s != null) {
                try {
                    workDir = WorkDirectory.open(new File(s), testSuite);
                }
                catch (FileNotFoundException e) {
                    throw new WorkDirFault(i18n, "ip.cantFindWorkDirInFile",
                                    new Object[] { s, file } );
                }
                catch (WorkDirectory.Fault e) {
                    throw new Fault(i18n, "ip.cantOpenWorkDirInFile",
                                    new Object[] { s, file, e.getMessage() } );
                }
            }
        }

        InterviewParameters parameters;

        // create the parameters object
        try {
            parameters = testSuite.createInterview();
        }
        catch (TestSuite.Fault e) {
            throw new Fault(i18n, "ip.cantCreateInterviewForTestSuite",
                            new Object[] { testSuite.getPath(), e.getMessage() } );
        }

        // set the work dir in the parameters object
        if (workDir != null)
            parameters.setWorkDirectory(workDir);

        // load the .jti data into the parameters object
        try {
            parameters.load(data, file);
        }
        catch (InterviewParameters.Fault e) {
            throw new Fault(i18n, "ip.cantLoadInterview",
                            new Object[] { file, e.getMessage() });
        }

        return parameters;
    }

    public void clear() {
        WorkDirectory wd = getWorkDirectory();
        super.clear();

        if (wd != null && TemplateUtilities.getTemplatePath(wd) != null) {
            if (wd.getTestSuite() != null) {
                try {
                    wd.getTestSuite().loadInterviewFromTemplate(
                        TemplateUtilities.getTemplateFile(wd), this);
                } catch (TestSuite.Fault ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        setEdited(false);
        currFile = null;
    }

    /**
     * Open a a configuration file, based on paths for the configuration file,
     * test suite and work directory. Any, but not all, of these paths may be null.
     * Any non-null path must specify an appropriate existing file, otherwise
     * an exception will be thrown.
     * @param testSuitePath the path for the test suite; if not specified,
     * the test suite will default from the work directory (if specified) or
     * the configuration file.
     * @param workDirPath the path for the work directory; if not specified,
     * the work directory will the default from the config file (if specified),
     * or will be null if no configuration file is given
     * @param configFilePath the path for the configuration file; if not specified,
     * the result will be a blank interview as created by the test suite.
     * @return an InterviewParameters object created from the given arguments
     * @throws Interview.Fault if there is any problem creating the
     * result
     */
    public static InterviewParameters open(String testSuitePath, String workDirPath, String configFilePath)
        throws InterviewParameters.Fault
    {
        File ts = (testSuitePath != null && testSuitePath.length() > 0
                       ? new File(testSuitePath) : null);
        File wd = (workDirPath != null && workDirPath.length() > 0
                       ? new File(workDirPath) : null);
        File cf = (configFilePath != null && configFilePath.length() > 0
                       ? new File(configFilePath) : null);
        return open(ts, wd, cf);
    }

    /**
     * Open a a configuration file, based on paths for the configuration file,
     * test suite and work directory. Any, but not all, of these paths may be null.
     * Any non-null path must specify an appropriate existing file, otherwise
     * an exception will be thrown.
     * @param testSuitePath the path for the test suite; if not specified,
     * the test suite will default from the work directory (if specified) or
     * the configuration file.
     * @param workDirPath the path for the work directory; if not specified,
     * the work directory will bdefault from the config file (if specified),
     * or will be null if no configuration file is given
     * @param configFilePath the path for the configuration file; if not specified,
     * the result will be a blank interview as created by the test suite.
     * @return an InterviewParameters object created from the gievn arguments
     * @throws Interview.Fault if there is any problem creating the
     * result
     */
    public static InterviewParameters open(File testSuitePath, File workDirPath, File configFilePath)
        throws InterviewParameters.Fault
    {

        // open test suite if specified

        TestSuite testSuite;

        if (testSuitePath != null) {
            try {
                testSuite = TestSuite.open(testSuitePath);
            }
            catch (FileNotFoundException e) {
                throw new Fault(i18n, "ip.cantFindTestSuite", testSuitePath);
            }
            catch (IOException e) {
                throw new Fault(i18n, "ip.cantOpenTestSuite", new Object[] { testSuitePath, e });
            }
            catch (TestSuite.Fault e) {
                throw new Fault(i18n, "ip.cantOpenTestSuite", new Object[] { testSuitePath, e.getMessage() });
            }
        }
        else
            testSuite = null;

        // open work directory if specified, defaulting test suite if appropriate

        WorkDirectory workDir;

        if (workDirPath != null) {
            try {
                if (testSuite == null) {
                    workDir = WorkDirectory.open(workDirPath);
                    testSuite = workDir.getTestSuite();
                }
                else
                    workDir = WorkDirectory.open(workDirPath, testSuite);
            }
            catch (FileNotFoundException e) {
                throw new Fault(i18n, "ip.cantFindWorkDir", workDirPath);
            }
            catch (IOException e) {
                throw new Fault(i18n, "ip.cantOpenWorkDir", new Object[] { workDirPath, e });
            }
            catch (WorkDirectory.Fault e) {
                throw new Fault(i18n, "ip.cantOpenWorkDir", new Object[] { workDirPath, e.getMessage() });
            }
        }
        else
            workDir = null;

        // open config file if specified, defaulting work dir and test suite if appropriate
        // default config from test suite if appropriate

        InterviewParameters config;

        if (configFilePath == null)  {
            if (testSuite != null) {
                try {
                    config = testSuite.createInterview();
                }
                catch (TestSuite.Fault e) {
                    throw new Fault(i18n, "ip.cantCreateInterviewForTestSuite", new Object[] { testSuitePath, e });
                }

                if (workDir != null) {
                    config.setWorkDirectory(workDir);
                    FileHistory h = FileHistory.getFileHistory(workDir, "configHistory.jtl");
                    File latestConfigFile = h.getLatestEntry();

                    if (latestConfigFile != null) {
                        try {
                            config.load(latestConfigFile);
                        }
                        catch (IOException e) {
                            // ignore?
                        }   // catch
                    }
                }   // workdir != null
            }
            else
                throw new Fault(i18n, "ip.noPaths");
        }
        else {
            try {
                if (workDir == null) {
                    if (testSuite == null) {
                        config = open(configFilePath);
                        testSuite = config.getTestSuite();
                    }
                    else
                        config = open(configFilePath, testSuite);
                    workDir = config.getWorkDirectory();
                }
                else
                    config = open(configFilePath, workDir);
            }
            catch (FileNotFoundException e) {
                throw new Fault(i18n, "ip.cantFindConfigFile", configFilePath);
            }
            catch (IOException e) {
                throw new Fault(i18n, "ip.cantOpenConfigFile", new Object[] { configFilePath, e });
            }
        }

        // if still here, and had sufficient args, config should be open
        // and fully initialized
        return config;
    }



    /**
     * Load the interview with the contents of the file associated with
     * the interview. If the file does not exist, the interview will be
     * cleared.
     * @throws IOException is there is a problem reading the file
     * @throws Interview.Fault if there is a problem loading the
     * interview
     * @return true if there was an update from template
     */
    public boolean load() throws IOException, Fault {
        File f = getFile();
        if (f != null && f.exists())
            return load(f);
        else {
            clear();
            setEdited(false);
            return false;
        }
    }

    /**
     * Load the interview with the contents of a specified file,
     * which will become the default file associated with the interview.
     * @param file the file to be loaded
     * @throws FileNotFoundException if the specified file does not exist.
     * @throws IOException is there is a problem reading the file
     * @throws Interview.Fault if there is a problem loading the
     * interview
     * @return true if there was an update from template
     */
    public boolean load(File file) throws FileNotFoundException, IOException, Fault {
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            Properties data = new Properties();
            data.load(in);
            return load(data, file);
        }
        finally {
            in.close();
        }
    }

    /**
     * Load the interview with data that has already been read from a specified file,
     * which will become the default file associated with the interview.
     * @param data the data to be loaded
     * @param file the file from which the data was read
     * @throws Interview.Fault if there is a problem loading the interview
     * @return true if there was an update from template
     */
    public boolean load(Map data, File file) throws Fault {
        load(data);

        // restore template state
        String tm = (String) data.get(IS_TEMPLATE);
        setTemplate(tm != null && tm.equalsIgnoreCase(TRUE));

        setEdited(false);
        currFile = file;
        currFileLastModified = file.lastModified();
        currFileLoaded = true;
        return checkForUpdates();
    }
    /**
     * Returns true if there was update
     */
    public boolean checkForUpdates() {
        InterviewPropagator prop = new InterviewPropagator(this,
                                    ignorableProps, ignorablePrefs);
        return prop.checkForUpdate();
    }

    public void load(Map data, boolean checkChecksum) throws Fault {
        super.load(data, checkChecksum);

        String me = (String) data.get(MARKERS_ENABLED);
        setMarkersEnabled(me != null && me.equalsIgnoreCase(TRUE));

        String mf = (String) data.get(MARKERS_FILTER);
        setMarkersFilterEnabled(mf != null && mf.equalsIgnoreCase(TRUE));

        String tm = (String) data.get(IS_TEMPLATE);
        setTemplate(tm != null && tm.equalsIgnoreCase(TRUE));

        String tu = null;
        //if (isTemplate()) {
            tu = (String) data.get(TEMPLATE_PATH);
        //} else {
            //tu = (String) data.get(TEMPLATE_PREF + TEMPLATE_PATH);
        //}
        setTemplatePath(tu);
    }

    /**
     * Load the interview as best as possible with the data in another
     * Parameters object. If any of the various sub-objects as returned by
     * get<i>XXX</i>Parameters are not recognized, they will be ignored.
     * @param other The Parameters object to be copied.
     */
    public void load(Parameters other) {
        loadTestsParameters(other.getTestsParameters());
        loadExcludeListParameters(other.getExcludeListParameters());
        loadKeywordsParameters(other.getKeywordsParameters());
        loadPriorStatusParameters(other.getPriorStatusParameters());
        loadEnvParameters(other.getEnvParameters());
        loadConcurrencyParameters(other.getConcurrencyParameters());
        loadTimeoutFactorParameters(other.getTimeoutFactorParameters());
    }

    private void loadTestsParameters(TestsParameters other) {
        TestsParameters tp = getTestsParameters();
        if (!(tp instanceof MutableTestsParameters))
            return;

        MutableTestsParameters mtp = (MutableTestsParameters) tp;

        if (other instanceof MutableTestsParameters) {
            MutableTestsParameters mop = (MutableTestsParameters) other;
            mtp.setTestsMode(mop.getTestsMode());
            mtp.setSpecifiedTests(mop.getSpecifiedTests());
        }
        else {
            String[] tests = other.getTests();
            if (tests == null) {
                mtp.setTestsMode(MutableTestsParameters.ALL_TESTS);
                mtp.setSpecifiedTests(null);
            }
            else {
                mtp.setTestsMode(MutableTestsParameters.SPECIFIED_TESTS);
                mtp.setSpecifiedTests(tests);
            }
        }
    }

    private void loadExcludeListParameters(ExcludeListParameters other) {
        ExcludeListParameters tp = getExcludeListParameters();
        if (!(tp instanceof MutableExcludeListParameters))
            return;

        MutableExcludeListParameters mtp = (MutableExcludeListParameters) tp;

        if (other instanceof MutableExcludeListParameters) {
            MutableExcludeListParameters mop = (MutableExcludeListParameters) other;
            mtp.setExcludeMode(mop.getExcludeMode());
            mtp.setCustomExcludeFiles(mop.getCustomExcludeFiles());
            mtp.setLatestExcludeAutoCheckEnabled(mop.isLatestExcludeAutoCheckEnabled());
            mtp.setLatestExcludeAutoCheckMode(mop.getLatestExcludeAutoCheckMode());
            mtp.setLatestExcludeAutoCheckInterval(mop.getLatestExcludeAutoCheckInterval());
        }
        else {
            mtp.setExcludeMode(MutableExcludeListParameters.CUSTOM_EXCLUDE_LIST);
            mtp.setCustomExcludeFiles(null);
            mtp.setLatestExcludeAutoCheckEnabled(false);
            mtp.setLatestExcludeAutoCheckMode(MutableExcludeListParameters.CHECK_EVERY_X_DAYS);
            mtp.setLatestExcludeAutoCheckInterval(0);
        }
    }

    private void loadKeywordsParameters(KeywordsParameters other) {
        KeywordsParameters tp = getKeywordsParameters();
        if (!(tp instanceof MutableKeywordsParameters))
            return;

        MutableKeywordsParameters mtp = (MutableKeywordsParameters) tp;

        if (other instanceof MutableKeywordsParameters) {
            MutableKeywordsParameters mop = (MutableKeywordsParameters) other;
            mtp.setKeywordsMode(mop.getKeywordsMode());
            mtp.setMatchKeywords(mop.getMatchKeywordsMode(), mop.getMatchKeywordsValue());
        }
        else {
            Keywords k = other.getKeywords();
            if (k == null) {
                mtp.setKeywordsMode(MutableKeywordsParameters.NO_KEYWORDS);
                mtp.setMatchKeywords(MutableKeywordsParameters.EXPR, "");
            }
            else {
                mtp.setKeywordsMode(MutableKeywordsParameters.MATCH_KEYWORDS);
                mtp.setMatchKeywords(MutableKeywordsParameters.EXPR, k.toString());
            }

        }
    }

    private void loadPriorStatusParameters(PriorStatusParameters other) {
        PriorStatusParameters tp = getPriorStatusParameters();
        if (!(tp instanceof MutablePriorStatusParameters))
            return;

        MutablePriorStatusParameters mtp = (MutablePriorStatusParameters) tp;

        if (other instanceof MutablePriorStatusParameters) {
            MutablePriorStatusParameters mop = (MutablePriorStatusParameters) other;
            mtp.setPriorStatusMode(mop.getPriorStatusMode());
            mtp.setMatchPriorStatusValues(mop.getMatchPriorStatusValues());
        }
        else {
            boolean[] b = other.getPriorStatusValues();
            if (b == null) {
                mtp.setPriorStatusMode(MutablePriorStatusParameters.NO_PRIOR_STATUS);
                mtp.setMatchPriorStatusValues(new boolean[Status.NUM_STATES]);
            }
            else {
                mtp.setPriorStatusMode(MutablePriorStatusParameters.MATCH_PRIOR_STATUS);
                mtp.setMatchPriorStatusValues(b);
            }
        }
    }

    private void loadEnvParameters(EnvParameters other) {
        EnvParameters tp = getEnvParameters();
        if (!(tp instanceof LegacyEnvParameters))
            return;

        LegacyEnvParameters ltp = (LegacyEnvParameters) tp;

        if (other instanceof LegacyEnvParameters) {
            LegacyEnvParameters lop = (LegacyEnvParameters) other;
            ltp.setEnvFiles(lop.getEnvFiles());
            ltp.setEnvName(lop.getEnvName());
        }
    }

    private void loadConcurrencyParameters(ConcurrencyParameters other) {
        ConcurrencyParameters tp = getConcurrencyParameters();
        if (!(tp instanceof MutableConcurrencyParameters))
            return;

        MutableConcurrencyParameters mtp = (MutableConcurrencyParameters) tp;
        mtp.setConcurrency(other.getConcurrency());
    }

    private void loadTimeoutFactorParameters(TimeoutFactorParameters other) {
        TimeoutFactorParameters tp = getTimeoutFactorParameters();
        if (!(tp instanceof MutableTimeoutFactorParameters))
            return;

        MutableTimeoutFactorParameters mtp = (MutableTimeoutFactorParameters) tp;
        mtp.setTimeoutFactor(other.getTimeoutFactor());
    }

    /**
     * Save the current set of answers for the interview in the standard
     * file associated with the interview.
     * @throws IOException is there is a problem writing the file
     * @throws Interview.Fault if there is a problem preparing the
     * interview to be written
     * @see #getFile
     */
    public void save() throws IOException, Fault {
        File f = getFile();
        if (f == null)
            throw new IllegalStateException();
        save(f);
    }


    /**
     * Save the current state of the interview in a specified file,
     * and make that file the new file associated with the interview.
     * @param file the file in which to save the state of the interview
     * @throws IOException is there is a problem writing the file
     * @throws Interview.Fault if there is a problem preparing the
     * interview to be written
     * @see #getFile
     */
    public void save(File file) throws IOException, Fault {
        save(file, false);
    }



    /**
     * Save the current state of the interview in a specified file,
     * and make that file the new file associated with the interview.
     * @param file the file in which to save the state of the interview
     * @param isTemplate
     * @throws IOException is there is a problem writing the file
     * @throws Interview.Fault if there is a problem preparing the
     * interview to be written
     * @see #getFile
     */
    public void save(File file, boolean isTemplate) throws IOException, Fault {
        saveAs(file, true, true, isTemplate);

        setEdited(false);
        currFile = file;
        currFileLastModified = file.lastModified();
        currFileLoaded = true;
    }


    /**
     * Save the current state of the interview in a specified file,
     * including the paths for the test suite and work directory.
     * @param file the file in which to save the state of the interview
     * @throws IOException is there is a problem writing the file
     * @throws Interview.Fault if there is a problem preparing the
     * interview to be written
     */
    public void saveAs(File file)
        throws IOException, Fault
    {
        saveAs(file, true, true);
    }


    /**
     * Save the current state of the interview in a specified file.
     * If the test suite path is not saved, the file can only be used
     * as a configuration template.
     * @param file the file in which to save the state of the interview
     * @param saveTestSuite if true, the test suite path will be saved
     * in the file.
     * @param saveWorkDir if true, the work directory path will be saved
     * in the file.
     * @param isTemplate True, the interview will be saved as template.
     * @throws IOException is there is a problem writing the file
     * @throws Interview.Fault if there is a problem preparing the
     * interview to be written
     */
    public void saveAs(File file, boolean saveTestSuite, boolean saveWorkDir, boolean isTemplate)
        throws IOException, Fault
    {
        Properties data = new SortedProperties();
        setTemplate(isTemplate);        // dubious, why do we need to do this?

        if (saveTestSuite) {
            TestSuite ts = getTestSuite();
            if (ts != null)
                data.put(TESTSUITE, ts.getPath());
        }

        if (saveWorkDir) {
            WorkDirectory wd = getWorkDirectory();
            if (wd != null)
                data.put(WORKDIR, wd.getPath());
        }

        save(data);

        if (this.isTemplate == true ) {
            TemplateManager tm = this.templateManager;
            if (tm != null && !tm.canSaveTemplate(file)) {
                throw new Interview.Fault(i18n, "ip.badTmplPath");
            }
        }

        OutputStream out;
        if (backupPolicy == null)
            out = new BufferedOutputStream(new FileOutputStream(file));
        else
            out = backupPolicy.backupAndOpenStream(file);

        try {
            data.store(out, "JT Harness Configuration Interview");
        }
        finally {
            out.close();
        }
    }



    /**
     * Save the current state of the interview in a specified file.
     * If the test suite path is not saved, the file can only be used
     * as a configuration template.
     * @param file the file in which to save the state of the interview
     * @param saveTestSuite if true, the test suite path will be saved
     * in the file.
     * @param saveWorkDir if true, the work directory path will be saved
     * in the file.
     * @throws IOException is there is a problem writing the file
     * @throws Interview.Fault if there is a problem preparing the
     * interview to be written
     */
    public void saveAs(File file, boolean saveTestSuite, boolean saveWorkDir)
        throws IOException, Fault
    {
        saveAs(file, saveTestSuite, saveWorkDir, false);
    }

    public void save(Map data) {
        if (markersEnabled)
            data.put(MARKERS_ENABLED, TRUE);

        if (markersFilterEnabled)
            data.put(MARKERS_FILTER, TRUE);

        if (isTemplate()) {
            data.put(IS_TEMPLATE, TRUE);

            storeTemplateProperties(new Properties());
        }
        else {
            WorkDirectory wd = getWorkDirectory();
            if (wd != null && TemplateUtilities.getTemplatePath(wd) != null)
                data.put(TEMPLATE_PATH, TemplateUtilities.getTemplatePath(wd));
        }

        String name = getName();
        if (name != null)
            data.put(NAME, name);

        String desc = getDescription();
        if (desc != null)
            data.put(DESC, desc);


        super.save(data);
    }

    /**
     * Get the backup policy to be used when saving configuration files.
     * @return the backup policy object to be used when saving configuration files
     * @see #setBackupPolicy
     */
    public BackupPolicy getBackupPolicy() {
        return backupPolicy;
    }

    /**
     * Set the backup policy to be used when saving configuration files.
     * @param backupPolicy the backup policy object to be used when saving configuration files
     * @see #getBackupPolicy
     */
    public void setBackupPolicy(BackupPolicy backupPolicy) {
        this.backupPolicy = backupPolicy;
    }

    /**
     * Check if the current file has been loaded into this interview,
     * or if the interview has been saved in it.
     * @return true if the file associated with the interview was set as a
     * side effect of load or save, or false if the file was just set by
     * setFile.
     */
    public boolean isFileLoaded() {
        return currFileLoaded;
    }

    /**
     * Determine if the file associated with this interview has been modified
     * on disk after the last call of load or save.
     * @return true if the file on disk has been modified after it was last
     * used by load or save.
     * @see #load()
     * @see #save()
     */
    public boolean isFileNewer() {
        File f = getFile();
        return (f != null && f.exists() && ((currFileLastModified == 0)
                               || (f.lastModified() > currFileLastModified)));
    }

    //----------------------------------------------------------------------------

    /**
     * Check whether or not markers should be enabled.
     * @return whether or not markers should be enabled
     * @see #setMarkersEnabled
     */
    public boolean getMarkersEnabled() {
        return markersEnabled;
    }

    /**
     * Specify whether or not markers should be enabled.
     * @param on whether or not markers should be enabled
     * @see #getMarkersEnabled
     */
    public void setMarkersEnabled(boolean on) {
        if (on != markersEnabled) {
            markersEnabled = on;
            setEdited(true);
        }
    }

    /**
     * Check whether or not the history list should be filtered to
     * just show questions which have been marked.
     * @return whether or not the  history list should be filtered to
     * just show questions which have been marked
     * @see #setMarkersFilterEnabled
     */
    public boolean getMarkersFilterEnabled() {
        return markersFilterEnabled;
    }

    /**
     * Specify whether or not the history list should be filtered to
     * just show questions which have been marked.
     * @param on whether or not the  history list should be filtered to
     * just show questions which have been marked
     * @see #getMarkersFilterEnabled
     */
    public void setMarkersFilterEnabled(boolean on) {
        if (on != markersFilterEnabled) {
            markersFilterEnabled = on;
            setEdited(true);
        }
    }

    //----------------------------------------------------------------------------
    private final String [] ignorableProps = new String [] {
        INTERVIEW,
        LOCALE, TESTSUITE, WORKDIR, MARKERS,
        IS_TEMPLATE, TEMPLATE_PATH, QUESTION};
    private final String [] ignorablePrefs = new String [] { MARKERS_PREF, EXTERNAL_PREF, TEMPLATE_PREF};

    private BackupPolicy backupPolicy;
    private boolean markersEnabled;
    private boolean markersFilterEnabled;

    private File currFile;
    private boolean isTemplate;
    private String templatePath;
    private long currFileLastModified;
    private boolean currFileLoaded;
    protected File[] kflFiles;

    private CustomPropagationController pc =  new CustomPropagationController();

    static final String TESTSUITE = "TESTSUITE";
    static final String WORKDIR = "WORKDIR";
    static final String NAME = "NAME";
    static final String DESC = "DESCRIPTION";
    static final String MARKERS_ENABLED = "MARKERS.enabled";
    static final String MARKERS_FILTER = "MARKERS.filter";
    static final String IS_TEMPLATE = "IS_TEMPLATE";
    static final String TEMPLATE_PATH = "TEMPLATE_PATH";
    static final String TRUE = "true";

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(InterviewParameters.class);
}
