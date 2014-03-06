/*
 * $Id$
 *
 * Copyright (c) 2002, 2009, Oracle and/or its affiliates. All rights reserved.
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import com.sun.javatest.util.I18NResourceBundle;

/**
 * A basic implementation of Parameters for all except the EnvParameters
 * subsection.
 */
public abstract class BasicParameters
    implements
        Parameters,
        Parameters.MutableTestsParameters,
        Parameters.MutableExcludeListParameters,
        Parameters.MutableKeywordsParameters,
        Parameters.MutablePriorStatusParameters,
        Parameters.MutableConcurrencyParameters,
        Parameters.MutableTimeoutFactorParameters
{
    //---------------------------------------------------------------------

    public TestSuite getTestSuite() {
        return testSuite;
    }

    /**
     * Set the test suite for the test run. The test suite may only be set once.
     * If the test suite cannot be opened, isValid will return false, and
     * getErrorMessage will contain an error message.
     * @param file a path defining the test suite to be opened and set as the test
     * suite for the test run.
     * @see #getTestSuite
     * @see #setTestSuite(TestSuite)
     */
    public void setTestSuite(File file) {
        if (file == null)
            testSuiteError = i18n.getString("bp.noTestSuite");
        else {
            try {
                setTestSuite(TestSuite.open(file));
                //System.err.println("BP.setTestSuite: " + file + " opened");
            }
            catch (FileNotFoundException e) {
                testSuiteError = i18n.getString("bp.cantFindTestSuite", file);
            }
            catch (TestSuite.Fault e) {
                testSuiteError = i18n.getString("bp.badTestSuite", e.getMessage());
            }
        }
    }

    /**
     * Set the test suite for the test run. The test suite may only be set once.
     * @param ts the test suite to be set.
     * @see #getTestSuite
     * @throws NullPointerException if ts is null
     * @throws IllegalStateException if the test suite has already been set to
     * something different
     */
    public void setTestSuite(TestSuite ts) {
        if (ts == null)
            throw new NullPointerException();

        if (testSuite != null && testSuite != ts)
            throw new IllegalStateException();

        testSuite = ts;
        testSuiteError = null;
    }

    private boolean isTestSuiteOK() {
        return (testSuiteError == null);
    }


    private TestSuite testSuite;

    /**
     * A string to identify any errors that may have occurred when
     * setting the test suite, or null if there were no such errors.
     */
    protected String testSuiteError;

    //---------------------------------------------------------------------

    public WorkDirectory getWorkDirectory() {
        return workDir;
    }

    /**
     * Set the work directory for the test run. The work directory may only
     * be set once.
     * If the work directory cannot be opened, isValid will return false, and
     * getErrorMessage will contain an error message.
     * The test suite must already be set before this method is called.
     * @param dir a path defining the work directory to be opened and set as the
     * work directory for the test run.
     * @see #getWorkDirectory
     * @see #setWorkDirectory(WorkDirectory)
     */
    public void setWorkDirectory(File dir) {
        if (dir == null)
            workDirError = i18n.getString("bp.workDirMissing");
        else if (isTestSuiteOK()) {
            try {
                TestSuite ts = getTestSuite();
                if (dir.exists()) {
                    if (WorkDirectory.isWorkDirectory(dir)) {
                        setWorkDirectory(WorkDirectory.open(dir, ts));
                        workDirError = null;
                    }
                    else if (WorkDirectory.isEmptyDirectory(dir)) {
                        workDir = WorkDirectory.create(dir, ts);
                        workDirError = null;
                    }
                    else
                        workDirError = i18n.getString("bp.badWorkDir", dir.getPath());
                }
                else
                    workDirError = i18n.getString("bp.cantFindWorkDir", dir.getPath());
            }
            catch (FileNotFoundException e) {
                workDirError = i18n.getString("bp.cantFindWorkDir", dir.getPath());
            }
            catch (WorkDirectory.Fault e) {
                workDirError = i18n.getString("bp.workDirError", e.getMessage());
            }
        }
        else
            workDirError = i18n.getString("bp.noTestSuite");
    }

    /**
     * Set the work directory for the test run.
     * The work directory may only be set once.
     * If the test suite has already been set, it must exactly match the test suite
     * for the work directory; if the test suite has not yet been set, it will
     * be set to the test suite for this work directory.
     * @param wd the work directory to be set.
     * @see #getWorkDirectory
     * @throws NullPointerException if wd is null
     * @throws IllegalStateException if the work directory has already been set to
     * something different
     */
    public void setWorkDirectory(WorkDirectory wd) {
        if (wd == null)
            throw new NullPointerException();

        if (workDir != null && workDir != wd)
            throw new IllegalStateException();

        if (testSuite != null && wd.getTestSuite() != testSuite)
            throw new IllegalArgumentException();

        if (testSuite == null)
            setTestSuite(wd.getTestSuite());
        workDir = wd;
    }

    private boolean isWorkDirectoryOK() {
        return (workDirError == null);
    }

    private WorkDirectory workDir;

    /**
     * A string to identify any errors that may have occurred when
     * setting the work directory, or null if there were no such errors.
     */
    protected String workDirError;

    //---------------------------------------------------------------------

    public Parameters.TestsParameters getTestsParameters() {
        return this;
    }

    public String[] getTests() {
        return tests;
    }

    public void setTests(String[] tests) {
        if (tests == null)
            testsMode = MutableTestsParameters.ALL_TESTS;
        else {
            testsMode = MutableTestsParameters.SPECIFIED_TESTS;
            this.tests = tests;
        }
    }

    public int getTestsMode() {
        return testsMode;
    }

    public void setTestsMode(int mode) {
        if (mode != ALL_TESTS &&
            mode != SPECIFIED_TESTS)
            throw new IllegalArgumentException();

        testsMode = mode;
    }

    public String[] getSpecifiedTests() {
        return tests;
    }

    public void setSpecifiedTests(String[] tests) {
        if (tests == null)
            throw new NullPointerException();

        this.tests = tests;
    }

    private boolean isTestsOK() {
        return true;
    }

    private int testsMode = MutableTestsParameters.ALL_TESTS;
    private String[] tests;

    //---------------------------------------------------------------------

    public Parameters.ExcludeListParameters getExcludeListParameters() {
        return this;
    }

    public File[] getExcludeFiles() {
        TestSuite ts = getTestSuite();
        switch (excludeMode) {
        case INITIAL_EXCLUDE_LIST:
            if (ts == null)
                return null;
            File df = ts.getInitialExcludeList();
            if (df == null)
                return null;
            return new File[] { df };

        case LATEST_EXCLUDE_LIST:
            if (ts == null)
                return null;
            URL u = ts.getLatestExcludeList();
            if (u == null)
                return null;
            WorkDirectory wd = getWorkDirectory();
            if (wd == null)
                return null;
            return new File[] { wd.getSystemFile("latest.jtx") };

        case CUSTOM_EXCLUDE_LIST:
            return customExcludeFiles;

        default:
            return null;
        }
    }

    public void setExcludeFiles(File[] files) {
        if (files == null || files.length == 0)
            setExcludeMode(NO_EXCLUDE_LIST);
        else {
            setExcludeMode(CUSTOM_EXCLUDE_LIST);
            setCustomExcludeFiles(files);
        }
    }

    public ExcludeList getExcludeList() {
        updateExcludeList();
        return cachedExcludeList;
    }

    public TestFilter getExcludeListFilter() {
        updateExcludeList();
        return cachedExcludeListFilter;
    }

    public int getExcludeMode() {
        return excludeMode;
    }

    public void setExcludeMode(int mode) {
        excludeMode = mode;
    }

    public File[] getCustomExcludeFiles() {
        return customExcludeFiles;
    }

    public void setCustomExcludeFiles(File[] files) {
        customExcludeFiles = files;
    }

    public boolean isLatestExcludeAutoCheckEnabled() {
        return false;
    }

    public void setLatestExcludeAutoCheckEnabled(boolean b) {
        latestExcludeAutoCheck = b;
    }

    public int getLatestExcludeAutoCheckMode() {
        return latestExcludeAutoCheckMode;
    }

    public void setLatestExcludeAutoCheckMode(int mode) {
        latestExcludeAutoCheckMode = mode;
    }

    public int getLatestExcludeAutoCheckInterval() {
        return latestExcludeAutoCheckInterval;
    }

    public void setLatestExcludeAutoCheckInterval(int days) {
        latestExcludeAutoCheckInterval = days;;
    }

    private boolean isExcludeListOK() {
        return (excludeListError == null);
    }

    private File[] getAbsoluteExcludeFiles() {
        updateAbsoluteExcludeFiles();
        return cachedAbsExcludeFiles;
    }
    private void updateAbsoluteExcludeFiles() {
        TestSuite ts = getTestSuite();
        File base = (ts == null ? null : ts.getRootDir());
        File[] excludeFiles = getExcludeFiles();
        if (cachedAbsExcludeFiles == null ||
            cachedAbsExcludeFiles_base != base ||
            cachedAbsExcludeFiles_excludeFiles != excludeFiles) {
            cachedAbsExcludeFiles = getAbsoluteFiles(base, excludeFiles);
        }
    }

    private void updateExcludeList() {
        File[] absExclFiles = getAbsoluteExcludeFiles();
        if (cachedExcludeList == null
            || !equal(cachedExcludeList_absExclFiles, absExclFiles)) {
            try {
                if (absExclFiles == null)
                    cachedExcludeList = new ExcludeList();
                else
                    cachedExcludeList = new ExcludeList(cachedAbsExcludeFiles);
                cachedExcludeList_absExclFiles = cachedAbsExcludeFiles;
                cachedExcludeListFilter = new ExcludeListFilter(cachedExcludeList);
                excludeListError = null;
            }
            catch (FileNotFoundException e) {
                cachedExcludeList = null;
                cachedExcludeListFilter = null;
                excludeListError = i18n.getString("bp.exclListNotFound", e.getMessage());
            }
            catch (IOException e) {
                cachedExcludeList = null;
                cachedExcludeListFilter = null;
                excludeListError = i18n.getString("bp.exclListFault", e);
            }
            catch (ExcludeList.Fault e) {
                cachedExcludeList = null;
                cachedExcludeListFilter = null;
                excludeListError = i18n.getString("bp.exclListFault", e.getMessage());
            }
        }
    }

    private int excludeMode = NO_EXCLUDE_LIST;
    private boolean latestExcludeAutoCheck;
    private int latestExcludeAutoCheckMode;
    private int latestExcludeAutoCheckInterval;
    private File[] customExcludeFiles = { };


    private File[] cachedAbsExcludeFiles;
    private File cachedAbsExcludeFiles_base;
    private File[] cachedAbsExcludeFiles_excludeFiles;
    private ExcludeList cachedExcludeList;
    private File[] cachedExcludeList_absExclFiles;
    private ExcludeListFilter cachedExcludeListFilter;
    /**
     * A string to identify any errors that may have occurred when
     * setting the exclude list parameters, or null if there were no such errors.
     */
    protected String excludeListError;

    //---------------------------------------------------------------------

    public Parameters.KeywordsParameters getKeywordsParameters() {
        return this;
    }

    public Keywords getKeywords() {
        updateCachedKeywords();
        return cachedKeywords;
    }

    public void setKeywords(int mode, String value) {
        if (value == null)
            keywordsMode = NO_KEYWORDS;
        else {
            keywordsMode = MATCH_KEYWORDS;
            keywordsMatchMode = mode;
            keywordsMatchValue = value;
        }
    }

    public TestFilter getKeywordsFilter() {
        updateCachedKeywords();
        if (keywordsMode == NO_KEYWORDS)
            return null;
        else
            return cachedKeywordsFilter;
    }

    public int getKeywordsMode() {
        return keywordsMode;
    }

    public void setKeywordsMode(int mode) {
        keywordsMode = mode;
    }

    public int getMatchKeywordsMode() {
        return keywordsMatchMode;
    }

    public String getMatchKeywordsValue() {
        return keywordsMatchValue;
    }

    public void setMatchKeywords(int mode, String value) {
        keywordsMatchMode = mode;
        keywordsMatchValue = value;
    }

    private void updateCachedKeywords() {
        if (keywordsMode == NO_KEYWORDS) {
            cachedKeywordsMatchMode = -1;
            cachedKeywordsMatchValue = null;
            cachedKeywords = null;
            cachedKeywordsFilter = null;
            keywordsError = null;
        }
        else {
            if (cachedKeywordsMatchMode != keywordsMatchMode
                || cachedKeywordsMatchValue == null
                || !cachedKeywordsMatchValue.equals(keywordsMatchValue)) {
                try {
                    cachedKeywordsMatchMode = keywordsMatchMode;
                    cachedKeywordsMatchValue = keywordsMatchValue;
                    String op = (keywordsMatchMode == EXPR ? "expr"
                                 : keywordsMatchMode == ALL_OF ? "all of"
                                 : "any of");
                    cachedKeywords = Keywords.create(op, keywordsMatchValue);
                    cachedKeywordsFilter = new KeywordsFilter(cachedKeywords);
                }
                catch (Keywords.Fault e) {
                    cachedKeywords = null;
                    cachedKeywordsFilter = null;
                    keywordsError = i18n.getString("bp.badKeywords", e.getMessage());
                }
            }
        }
    }

    private boolean isKeywordsOK() {
        updateCachedKeywords();
        return (keywordsError == null);
    }

    private int keywordsMode = NO_KEYWORDS;
    private int keywordsMatchMode = EXPR;
    private String keywordsMatchValue;

    private int cachedKeywordsMatchMode;
    private String cachedKeywordsMatchValue;
    private Keywords cachedKeywords;
    private TestFilter cachedKeywordsFilter;
    /**
     * A string to identify any errors that may have occurred when
     * setting the keywords parameters, or null if there were no such errors.
     */
    protected String keywordsError;

    //---------------------------------------------------------------------

    public Parameters.PriorStatusParameters getPriorStatusParameters() {
        return this;
    }

    public boolean[] getPriorStatusValues() {
        if (priorStatusMode == NO_PRIOR_STATUS)
            return null;
        else
            return priorStatusValues;
    }

    public void setPriorStatusValues(boolean[] values) {
        if (values == null)
            priorStatusMode = NO_PRIOR_STATUS;
        else {
            priorStatusMode = MATCH_PRIOR_STATUS;
            priorStatusValues = values;
        }
    }

    public TestFilter getPriorStatusFilter() {
        WorkDirectory wd = getWorkDirectory();
        TestResultTable r = (wd == null ? null : wd.getTestResultTable());
        boolean[] s = getPriorStatusValues();
        if (r == null || s == null)
            cachedPriorStatusFilter = null;
        else if (cachedPriorStatusFilter == null
                 || cachedPriorStatusFilter.getTestResultTable() != r
                 || !equal(cachedPriorStatusFilter.getStatusValues(), s)) {
            cachedPriorStatusFilter = new StatusFilter(s, r);
        }

        return cachedPriorStatusFilter;
    }

    public int getPriorStatusMode() {
        return priorStatusMode;
    }

    public void setPriorStatusMode(int mode) {
        if (mode != NO_PRIOR_STATUS &&
            mode != MATCH_PRIOR_STATUS)
            throw new IllegalArgumentException();

        priorStatusMode = mode;
    }

    public boolean[] getMatchPriorStatusValues() {
        return priorStatusValues;
    }

    public void setMatchPriorStatusValues(boolean[] v) {
        if (v == null)
            throw new NullPointerException();

        if (v.length != Status.NUM_STATES)
            throw new IllegalArgumentException();

        priorStatusValues = v;
    }

    private boolean isPriorStatusOK() {
        return true;
    }

    private int priorStatusMode = NO_PRIOR_STATUS;
    private boolean[] priorStatusValues = new boolean[Status.NUM_STATES];
    private StatusFilter cachedPriorStatusFilter;

    //---------------------------------------------------------------------

    public Parameters.ConcurrencyParameters getConcurrencyParameters() {
        return this;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(int conc) {
        if (conc <= 0) {
            concurrencyError =
                i18n.getString("bp.badConcurrency", new Integer(conc));
            concurrency = 1;
        }
        else {
            concurrencyError = null;
            concurrency = conc;
        }
    }

    private boolean isConcurrencyOK() {
        return (concurrencyError == null);
    }

    private int concurrency = 1;
    /**
     * A string to identify any errors that may have occurred when
     * setting the concurrency, or null if there were no such errors.
     */
    protected String concurrencyError;

    //---------------------------------------------------------------------

    public Parameters.TimeoutFactorParameters getTimeoutFactorParameters() {
        return this;
    }

    public float getTimeoutFactor() {
        return timeoutFactor;
    }

    public void setTimeoutFactor(float tf) {
        if (tf <= 0) {
            timeoutFactorError = i18n.getString("bp.badTimeout", new Float(tf));
            timeoutFactor = 1;
        }
        else {
            timeoutFactorError = null;
            timeoutFactor = tf;
        }
    }

    private boolean isTimeoutFactorOK() {
        return (timeoutFactorError == null);
    }

    private float timeoutFactor = 1;
    /**
     * A string to identify any errors that may have occurred when
     * setting the timeout factor, or null if there were no such errors.
     */
    protected String timeoutFactorError;

    //---------------------------------------------------------------------

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

    private TestFilter[] cachedTestFilters;

    //---------------------------------------------------------------------

    public boolean isValid() {
        return (   isTestSuiteOK()
                && isWorkDirectoryOK()
                && isTestsOK()
                && isExcludeListOK()
                && isKeywordsOK()
                && isPriorStatusOK()
                && isConcurrencyOK()
                && isTimeoutFactorOK());
    }

    public String getErrorMessage() {
        return (  testSuiteError != null ? testSuiteError
                : workDirError != null ? workDirError
                : excludeListError != null ? excludeListError
                : keywordsError != null ? keywordsError
                : concurrencyError != null ? concurrencyError
                : timeoutFactorError != null ? timeoutFactorError
                : null);
    }

    //---------------------------------------------------------------------

    /**
     * Convert a set of files to be absolute files. Files that are already
     * absolute are left unchanged; relative files are evaluated relative to
     * a specified base directory.
     * @param baseDir The base directory for any relative files
     * @param files The files to be made absolute, or null if none
     * @return the given files with any relative files having been evaluated
     * relative to the given base directory, or null if files was null.
     */
    protected static File[] getAbsoluteFiles(File baseDir, File[] files) {
        if (files == null)
            return null;

        boolean allAbsolute = true;
        for (int i = 0; i < files.length && allAbsolute; i++)
            allAbsolute = files[i].isAbsolute();

        if (allAbsolute)
            return files;

        File[] absoluteFiles = new File[files.length];
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            absoluteFiles[i] = (f.isAbsolute() ? f : new File(baseDir, f.getPath()));
        }

        return absoluteFiles;
    }

    //---------------------------------------------------------------------

    /**
     * Compare two boolean arrays for equality.
     * @param b1 the first array to be compared
     * @param b2 the second array to be compared
     * @return true and only if both arguments are null, or if both are not null
     * and are element-wise equal.
     */
    protected static boolean equal(boolean[] b1, boolean[] b2) {
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

    /**
     * Compare two arrays of Files for equality.
     * @param f1 the first array to be compared
     * @param f2 the second array to be compared
     * @return true and only if both arguments are null, or if both are not null
     * and are element-wise equal.
     */
    protected static boolean equal(File[] f1, File[] f2) {
        if (f1 == null || f2 == null)
            return (f1 == f2);

        if (f1.length != f2.length)
            return false;

        for (int i = 0; i < f1.length; i++) {
            if (f1[i] != f2[i])
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

    //---------------------------------------------------------------------

    private static final I18NResourceBundle i18n =
        I18NResourceBundle.getBundleForClass(BasicParameters.class);
}
