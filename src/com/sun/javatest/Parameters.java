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

import java.io.File;


/**
 * Configuration parameters for a test run. Methods are provided
 * to access the parameters, and to access objects which contain
 * the permanent representation of the parameters, which is otherwise
 * undefined. Different representations include implementations
 * based on configuration interviews, and on simple files. A default
 * implementation, based on configuration interviews, is available
 * to simplify the task of providing configuration parameters in
 * almost all cases.
 *
 * @since 3.0.2
 */
public interface Parameters
{
    /**
     * Get the test suite for which these parameters apply.
     * @return the test suite for which these parameters apply.
     * @see #setTestSuite
     */
    TestSuite getTestSuite();

    /**
     * Set the test suite for which these parameters apply.
     * @param ts the test suite for which these parameters apply
     * @see #getTestSuite
     */
    void setTestSuite(TestSuite ts);

    /**
     * Get the work directory in which to store the results of the test run.
     * @return the work directory in which to store the results of the test run.
     * @see #setWorkDirectory
     */
    WorkDirectory getWorkDirectory();

    /**
     * Set the work directory for which these parameters apply.
     * @param wd the work directory for which these parameters apply
     * @see #getWorkDirectory
     */
    void setWorkDirectory(WorkDirectory wd);

    /**
     * Get the paths identifying the tests or folders of tests within
     * the test suite to be run.
     * @return an array of paths identifying the tests to be run
     * @see Parameters.TestsParameters#getTests
     */
    String[] getTests();

    /**
     * Get an object which provides access to the paths identifying the tests
     * or folders of tests to be run.
     * @return an object which provides access to the paths identifying tests to be run.
     */
    TestsParameters getTestsParameters();

    /**
     * Get an exclude list which identifies tests or test cases
     * to be excluded from the test run.
     * @return an exclude list  identifying tests or test cases
     * to be excluded from the test run.
     * @see #getExcludeListFilter
     * @see Parameters.ExcludeListParameters#getExcludeList
     */
    ExcludeList getExcludeList();

    /**
     * Get an object which provides access to the exclude list which
     * identifies tests or test cases to be excluded from the test run.
     * @return an object which provides access to the exclude list
     * identifying tests or test cases to be excluded from the test run.
     */
    ExcludeListParameters getExcludeListParameters();

    /**
     * Get a keywords object which identifies tests to be run according
     * to their keywords.
     * @return  a keywords object which identifies tests to be run according
     * to their keywords.
     * @see Parameters.KeywordsParameters#getKeywords
     */
    Keywords getKeywords();

    /**
     * Get an object which provides access to the keywords object which
     * identifies tests to be run according to their keywords.
     * @return  an object which provides access to the keywords object
     * which identifies tests to be run according to their keywords.
     */
    KeywordsParameters getKeywordsParameters();

    /**
     * Get an array of booleans which identify tests to be run according to their
     * prior execution status. The array can be indexed by the constants
     * Status.PASSED, Status.FAILED, Status.ERROR, and Status.NOT_RUN.
     * For each of those values, if the corresponding boolean in the array
     * is true, a test will be selected if its status matches the index.
     * If the array is null, all tests will be selected.
     * @return an array of booleans which identifying tests to be run according
     * to their prior execution status, or null if no such criteria is required.
     * @see Parameters.PriorStatusParameters#getPriorStatusValues
     */
    boolean[] getPriorStatusValues();

    /**
     * Get an object which provides access to an array of booleans which
     * identify tests to be run according to their prior execution status.
     * @return an object which provides access to an array of booleans which
     * identify tests to be run according to their prior execution status,
     * or null if no such selection criteria is required.
     */
    PriorStatusParameters getPriorStatusParameters();

    /**
     * Get the environment of test-suite-specific configuration values,
     * to be passed to the script used to run each test.
     * @return an environment to be passed to the script used to run
     * each test.
     * @see Parameters.EnvParameters#getEnv
     */
    TestEnvironment getEnv();

    /**
     * Get an object which provides access to the environment of
     * test-suite-specific configuration values to be used when each
     * test is run.
     * @return an object which provides access to the environment to
     * be used when each test is run.
     */
    EnvParameters getEnvParameters();

    /**
     * Get an integer specifying the maximum number of tests that may
     * be run in parallel.
     * @return an integer specifying the maximum number of tests that
     * may be run in parallel
     * @see Parameters.ConcurrencyParameters#getConcurrency
     */
    int getConcurrency();

    /**
     * Get an object which provides access to the integer specifying
     * the maximum number of tests that may be run in parallel.
     * @return an object which provides access to the integer specifying
     * the maximum number of tests that may be run in parallel.
     */
    ConcurrencyParameters getConcurrencyParameters();

    /**
     * Get an integer specifying a scale factor to be applied to the
     * standard timeout for the test.
     * @return an integer specifying a scale factor to be applied to
     * the standard timeout for each test.
     * @see Parameters.TimeoutFactorParameters#getTimeoutFactor
     */
    float getTimeoutFactor();

    /**
     * Get an object which provides access to the integer specifying
     * a scale factor to be applied to the standard timeout for the test.
     * @return an object which provides access to the integer specifying
     * a scale factor to be applied to the standard timeout for each test.
     */
    TimeoutFactorParameters getTimeoutFactorParameters();

    /**
     * Get a filter which will filter tests according to the result
     * of getExcludeList(). If the result of getExcludeList is null
     * or an empty exclude list, the result of this method will also be null.
     * @return a filter which will filter tests according to the result
     * of getExcludeList().
     * @see #getExcludeList
     */
    TestFilter getExcludeListFilter();

    /**
     * Get a filter which will filter tests according to the result
     * of getKeywords(). If the result of getKeywords is null,
     * the result of this method will also be null.
     * @return a filter which will filter tests according to the result
     * of getKeywords().
     * @see #getKeywords
     */
    TestFilter getKeywordsFilter();

    /**
     * Get a filter which will filter tests according to the result
     * of getPriorStatusValus(). If the result of getPriorStatusValues is null,
     * the result of this method will also be null.
     * @return a filter which will filter tests according to the result
     * of getPriorStatusValues().
     * @see #getPriorStatusValues
     */
    TestFilter getPriorStatusFilter();

    /**
     * Get a test-suite specific filter which will filter tests according
     * to test-suite-specific criteria, as perhaps determined by
     * a configuration interview. For example, if the platform being tested
     * does not support some optional feature, the tests for that feature
     * could be automatically filtered out. If no such filter is required,
     * null can be returned.
     * @return a test-suite-specific filter, or null if no such filter is
     * required.
     */
    TestFilter getRelevantTestFilter();

    /**
     * Get an array of the non-null filters returned from
     * getExcludeListFilter, getKeywordsFilter, getPriorStatusFilter,
     * and getRelevantTestFilter.
     * @return an array of the non-null filters returned by the
     * various getXXXFilter methods.
     * @see #getExcludeListFilter
     * @see #getKeywordsFilter
     * @see #getPriorStatusFilter
     * @see #getRelevantTestFilter
     */
    TestFilter[] getFilters();

    /**
     * Determine whether all the configuration values are valid.
     * If so, the result will be true; if not, the result will be false,
     * and getErrorMessage will provide details about at least one of the
     * invalid values.
     * @return true if and only if all the configuration values are valid
     * @see #getErrorMessage
     */
    boolean isValid();

    /**
     * If there is an error in any of the configuration values,
     * as indicated by isValid, this method will provide a detail
     * message about one or more of the invalid values.
     * The result is undefined if isValid is true.
     * @return a detail message about one or more invalid values
     * @see #isValid
     */
    String getErrorMessage();

    //----------------------------------------------------------------------------

    /**
     * An interface providing basic abstract access to the set of paths
     * defining the tests and folders of tests to be run.
     */
    interface TestsParameters {
        /**
         * Get the paths identifying the tests or folders of tests within
         * the test suite to be run.
         * @return an array of paths identifying the tests to be run
         * @see Parameters#getTests
         */
        String[] getTests();
    }

    //----------------------------------------------------------------------------

    /**
     * An interface providing abstract access to a set of paths
     * defining the tests and folders of tests to be run.
     */
    interface MutableTestsParameters extends TestsParameters {
        /**
         * Specify the tests to be executed.
         * @param tests If null, set the tests mode to ALL_TESTS;
         * if not null, set the tests mode to SPECIFIED_TESTS, and set
         * the specified tests.
         * @see #getTests
         */
        void setTests(String[] tests);

        /**
         * Get the current mode for how the tests are specified.
         * @return ALL_TESTS if all tests are to be run, irrespective of
         * the selected tests, or SPECIFIED_TESTS if  a set of specified
         * tests are to be run.
         * @see #setTestsMode
         * @see #ALL_TESTS
         * @see #SPECIFIED_TESTS
         */
        int getTestsMode();

        /**
         * Set the current mode for how the tests are specified.
         * @param mode use ALL_TESTS if all tests are to be run, irrespective of
         * the selected tests, or SPECIFIED_TESTS if  a set of specified
         * tests are to be run.
         * @throws IllegalArgumentException if neither ALL_TESTS or SPECIFIED_TESTS
         * is given
         * @see #getTestsMode
         * @see #ALL_TESTS
         * @see #SPECIFIED_TESTS
         */
        void setTestsMode(int mode);

        /**
         * Get the set of specified tests to be used as the set of tests to
         * be run when the mode is set to SPECIFIED_TESTS. When the mode is
         * set to ALL_TESTS, the specified tests are remembered, but not used,
         * until the mode is set back to SPECIFIED_TESTS.
         * @return an array of specified tests to be used as the set of tests to
         * be run when the mode is set to SPECIFIED_TESTS.
         * @see #setSpecifiedTests
         * @see #getTests
         */
        String[] getSpecifiedTests();

        /**
         * Set the specified tests to be be run when the mode is set to SPECIFIED_TESTS.
         * When the mode is set to ALL_TESTS, these tests are remembered, but not used,
         * until the mode is set back to SPECIFIED_TESTS.
         * @param tests an array of paths identifying the tests to be run
         * @see #getSpecifiedTests
         * @see #getTests
         */
        void setSpecifiedTests(String[] tests);

        /**
         * A constant used to indicate that all tests in the test
         * suite should be run.
         */
        int ALL_TESTS = 1;

        /**
         * A constant used to indicate that specified tests in the test
         * suite should be run.
         */
        int SPECIFIED_TESTS = 2;
    }

    //----------------------------------------------------------------------------

    /**
     * An interface providing basic abstract access to an exclude list,
     * defining tests to be excluded from the test run.
     */
    interface ExcludeListParameters {
        /**
         * Get an exclude list which identifies tests or test cases
         * to be excluded from the test run.
         * @return an exclude list  identifying tests or test cases
         * to be excluded from the test run.
         * @see #getExcludeListFilter
         * @see Parameters#getExcludeList
         */
        ExcludeList getExcludeList();
    }

    //----------------------------------------------------------------------------

    /**
     * An interface providing abstract access to an exclude list,
     * as defined by a set of files, defining tests to be excluded
     * from the test run.
     */
    interface MutableExcludeListParameters extends ExcludeListParameters {
        /**
         * Get the set of files which define the exclude list. The files
         * are all returned as absolute files.
         * @return the set of files which define the exclude list
         * @see #getExcludeFiles
         * @see #setExcludeFiles
         */
        File[] getExcludeFiles();

        /**
         * Set the set of files used to define the exclude list.
         * @param files If null, the exclude mode will be set to NO_EXCLUDE_LIST;
         * if not null, the exclude mode will be set to CUSTOM_EXCLUDE_LIST
         * and the custom exclude files will be set to this value
         * @see #getExcludeFiles
         */
        void setExcludeFiles(File[] files);

        /**
         * Get the current exclude list mode.
         * @return A value indicating the current exclude list mode
         * @see #setExcludeMode
         * @see #NO_EXCLUDE_LIST
         * @see #INITIAL_EXCLUDE_LIST
         * @see #LATEST_EXCLUDE_LIST
         * @see #CUSTOM_EXCLUDE_LIST
         */
        int getExcludeMode();

        /**
         * Set the current exclude list mode.
         * @param mode A value indicating the desired exclude list mode
         * @see #getExcludeMode
         * @see #NO_EXCLUDE_LIST
         * @see #INITIAL_EXCLUDE_LIST
         * @see #LATEST_EXCLUDE_LIST
         * @see #CUSTOM_EXCLUDE_LIST
         */
        void setExcludeMode(int mode);

        /**
         * A constant used to indicate that no exclude list is required.
         */
        int NO_EXCLUDE_LIST = 1;

        /**
         * A constant used to indicate that the default exclude list
         * (if any) for the test suite should be used.
         * @see TestSuite#hasInitialExcludeList
         */
        int INITIAL_EXCLUDE_LIST = 2;

        /**
         * A constant used to indicate that the latest exclude list
         * (if any) for the test suite should be used.
         * @see TestSuite#hasLatestExcludeList
         */
        int LATEST_EXCLUDE_LIST = 3;

        /**
         * A constant used to indicate that a client-supplied set
         * of exclude files should be used.
         */
        int CUSTOM_EXCLUDE_LIST = 4;

        /**
         * Get the files used to define the exclude list when the
         * exclude list mode is set to CUSTOM_EXCLUDE_LIST.
         * @return the files used to define a custom exclude list
         * @see #setCustomExcludeFiles
         */
        File[] getCustomExcludeFiles();

        /**
         * Set the files used to define the exclude list when the
         * exclude list mode is set to CUSTOM_EXCLUDE_LIST.
         * @param files the files used to define a custom exclude list
         * @see #getCustomExcludeFiles
         */
        void setCustomExcludeFiles(File[] files);

        /**
         * Check if the automatic check for newer exclude lists
         * is enabled when the exclude list mode is set to LATEST_EXCLUDE_LIST.
         * @return true if the automatic check is enabled
         * @see #setLatestExcludeAutoCheckEnabled
         */
        boolean isLatestExcludeAutoCheckEnabled();

        /**
         * Specify if the automatic check for newer exclude lists
         * is enabled when the exclude list mode is set to LATEST_EXCLUDE_LIST.
         * @param b whether or not the automatic check is enabled
         * @see #isLatestExcludeAutoCheckEnabled
         */
        void setLatestExcludeAutoCheckEnabled(boolean b);

        /**
         * Get the mode which defines how often to automatically check
         * for updated exclude lists, when the exclude list mode is set
         * to LATEST_EXCLUDE_LIST, and the automatic check is enabled.
         * @return a value indicating how often to check for the
         * availability of a newer exclude list
         * @see #setLatestExcludeAutoCheckMode
         * @see #CHECK_EVERY_X_DAYS
         * @see #CHECK_EVERY_RUN
         */
        int getLatestExcludeAutoCheckMode();

        /**
         * Set the mode which defines how often to automatically check
         * for updated exclude lists, when the exclude list mode is set
         * to LATEST_EXCLUDE_LIST, and the automatic check is enabled.
         * @param mode a value indicating how often to check for the
         * availability of a newer exclude list
         * @see #getLatestExcludeAutoCheckMode
         * @see #CHECK_EVERY_X_DAYS
         * @see #CHECK_EVERY_RUN
         */
        void setLatestExcludeAutoCheckMode(int mode);

        /**
         * A constant used to indicate that the website used to
         * supply the latest exclude list should be checked every
         * so many days, to see if a newer version is available.
         */
        int CHECK_EVERY_X_DAYS = 1;

        /**
         * A constant used to indicate that the website used to
         * supply the latest exclude list should be checked on every
         * test run to see if a newer version is available.
         */
        int CHECK_EVERY_RUN = 2;

        /**
         * Get the interval, in days, to be used when automatically
         * checking for exclude list updates and the auto check mode
         * is set to CHECK_EVERY_X_DAYS.
         * @return the interval, in days, between checks
         * @see #setLatestExcludeAutoCheckInterval
         */
        int getLatestExcludeAutoCheckInterval();

        /**
         * Set the interval, in days, to be used when automatically
         * checking for exclude list updates and the auto check mode
         * is set to CHECK_EVERY_X_DAYS.
         * @param days the number of days to wait between checks
         * @see #getLatestExcludeAutoCheckInterval
         */
        void setLatestExcludeAutoCheckInterval(int days);
    }

    //----------------------------------------------------------------------------

    /**
     * An interface providing basic abstract access to a keywords object
     * which can be used to select tests according to their keywords.
     */
    interface KeywordsParameters {
        /**
         * Get a keywords object which identifies tests to be run according
         * to their keywords.
         * @return  a keywords object which identifies tests to be run according
         * to their keywords.
         */
        Keywords getKeywords();
    }

    //----------------------------------------------------------------------------

    /**
     * An interface providing abstract access to a keywords object
     * which can be used to select tests according to their keywords.
     */
    interface MutableKeywordsParameters extends KeywordsParameters {
        /**
         * Set the details of the keywords to be used, if any,
         * to filter tests for execution.
         * @param mode the value of the match keywords mode to be set if
         * <code>value</code> is not null
         * @param value if null, the keywords mode will be set to NO_KEYWORDS;
         * if not null, the keywords mode will be set to MATCH_KEYWORDS, the
         * match keywords mode will be set to <code>mode</code>,
         * and the match keywords value will be set to this value
         * @see #getKeywords
         * @see #NO_KEYWORDS
         * @see #MATCH_KEYWORDS
         */
        void setKeywords(int mode, String value);

        /**
         * Get the current keywords mode.
         * @return  NO_KEYWORDS if no keyword filtering will be used to
         * select tests for execution, or MATCH_KEYWORDS if  keywords
         * will be filtered according to the match mode and match value.
         * @see #setKeywordsMode
         * @see #NO_KEYWORDS
         * @see #MATCH_KEYWORDS
         */
        int getKeywordsMode();

        /**
         * Set the current keywords mode.
         * @param mode set to NO_KEYWORDS if no keyword filtering will be
         * used to select tests for execution, or MATCH_KEYWORDS if  keywords
         * will be filtered according to the match mode and match value.
         * @see #getKeywordsMode
         * @see #NO_KEYWORDS
         * @see #MATCH_KEYWORDS
         */
        void setKeywordsMode(int mode);

        /**
         * A constant used to indicate that no keyword filtering
         * should be used.
         */
        int NO_KEYWORDS = 1;

        /**
         * A constant used to indicate that only tests matching the
         * specified keywords should be selected.
         */
        int MATCH_KEYWORDS = 2;

        /**
         * Get a value which indicates how to interpret the match value,
         * when the keywords mode is set to MATCH_KEYWORDS.
         * @return a value which indicates how to interpret the match value,
         * when the keywords mode is set to MATCH_KEYWORDS
         * @see #setMatchKeywords
         * @see #ANY_OF
         * @see #ALL_OF
         * @see #EXPR
         */
        int getMatchKeywordsMode();

        /**
         * Get a value that identifies which tests are to be selected,
         * when the keywords mode is set to MATCH_KEYWORDS.
         * @return a value that identifies which tests are to be selected,
         * when the keywords mode is set to MATCH_KEYWORDS
         * @see #setMatchKeywords
         * @see #ANY_OF
         * @see #ALL_OF
         * @see #EXPR
         */
        String getMatchKeywordsValue();

        /**
         * Set how to match a tests keywords when the keywords mode
         * is set to MATCH_KEYWORDS.
         * @param mode A value indicating how to interpret <code>value</code>
         * @param value If <code>mode</code> is set to ANY_OF or ALL_OF,
         * this value should give a white-space separate list of keywords
         * to be matched; if <code>mode</code> is set to EXPR, this value
         * should be a boolean expression using terminals, &amp; (and), | (or),
         * ! (negation) and parentheses, where the terminals are true if
         * a test description contains that name as one of its keywords.
         * @see #getMatchKeywordsMode
         * @see #getMatchKeywordsValue
         * @see #ANY_OF
         * @see #ALL_OF
         * @see #EXPR
         */
        void setMatchKeywords(int mode, String value);

        /**
         * A constant used to indicate that tests that match any of
         * the given keywords should be selected.
         */
        int ANY_OF = 1;

        /**
         * A constant used to indicate that tests that match all of
         * the given keywords should be selected.
         */
        int ALL_OF = 2;

        /**
         * A constant used to indicate that tests that match the
         * given keyword expression should be selected.
         */
        int EXPR = 3;

    }

    //----------------------------------------------------------------------------

    /**
     * An interface providing basic abstract access to a set of booleans
     * which can be used to select tests according to their prior execution
     * status.
     */
    interface PriorStatusParameters {
        /**
         * Get an array of booleans which identify tests to be run according to their
         * prior execution status. The array can be indexed by the constants
         * Status.PASSED, Status.FAILED, Status.ERROR, and Status.NOT_RUN.
         * For each of those values, if the corresponding boolean in the array
         * is true, a test will be selected if its status matches the index.
         * If the array is null, all tests will be selected.
         * @return an array of booleans which identifying tests to be run according
         * to their prior execution status, or null if no such criteria is required.
         * @see Parameters#getPriorStatusValues
         */
        boolean[] getPriorStatusValues();

    }

    //----------------------------------------------------------------------------

    /**
     * An interface providing abstract access to a set of booleans
     * which can be used to select tests according to their prior execution
     * status.
     */
    interface MutablePriorStatusParameters extends PriorStatusParameters {
        /**
         * Set which prior status values should be used, if any, to select tests
         * for execution.
         * @param b if null, the prior status mode will be set to NO_PRIOR_STATUS;
         * if not null, the prior status mode will be set to MATCH_PRIOR_STATUS,
         * and the matching values will be set to this array.
         * @see #getPriorStatusValues
         */
        void setPriorStatusValues(boolean[] b);

        /**
         * Get the current mode determining whether tests are selected or not
         * according to their prior execution status.
         * @return a value of NO_PRIOR_STATUS indicates the prior execution status
         * will not be taken into account; otherwise, a value of MATCH_PRIOR_STATUS
         * means that tests will be selected if and only of their execution status
         * matches one of the matching prior status values.
         * @see #setPriorStatusMode
         * @see #NO_PRIOR_STATUS
         * @see #MATCH_PRIOR_STATUS
         */
        int getPriorStatusMode();

        /**
         * Set the current mode determining whether tests are selected or not
         * according to their prior execution status.
         * @param mode if set to NO_PRIOR_STATUS, the prior execution status
         * will not be taken into account; otherwise, if set to MATCH_PRIOR_STATUS
         * tests will be selected if and only of their execution status matches
         * one of the matching prior status values.
         * @see #getPriorStatusMode
         * @see #NO_PRIOR_STATUS
         * @see #MATCH_PRIOR_STATUS
         */
        void setPriorStatusMode(int mode);

        /**
         * A constant used to indicate that a test's prior execution status
         * should not be taken into account when selecting tests for execution.
         */
        int NO_PRIOR_STATUS = 1;

        /**
         * A constant used to indicate that tests should be selected
         * for execution if their status matched one of the matching
         * prior status values.
         */
        int MATCH_PRIOR_STATUS = 2;

        /**
         * Get an array of booleans which identify which tests to be run,
         * according to their prior execution status.
         * The array can be indexed by the constants
         * {@link Status#PASSED}, {@link Status#FAILED},
         * {@link Status#ERROR}, and {@link Status#NOT_RUN}.
         * A test will be selected for execution if the entry in the
         * array corresponding to the tests execution status is set to true.
         * @return an array of booleans which identifying
         * the prior execution status of tests to be selected to be executed.
         * @see #setMatchPriorStatusValues
         */
        boolean[] getMatchPriorStatusValues();

        /**
         * Set an array of booleans to identify which tests to be run,
         * according to their prior execution status.
         * The array can be indexed by the constants
         * {@link Status#PASSED}, {@link Status#FAILED},
         * {@link Status#ERROR}, and {@link Status#NOT_RUN}.
         * A test will be selected for execution if the entry in the
         * array corresponding to the tests execution status is set to true.
         * @param values an array of booleans which identifying
         * the prior execution status of tests to be selected to be executed.
         * @see #getMatchPriorStatusValues
         */
        void setMatchPriorStatusValues(boolean[] values);
    }

    //----------------------------------------------------------------------------

    /**
     * An interface providing basic abstract access to the environment
     * used to run each test. This interface will typically be subtyped
     * by individual test suites, to collect the necessary values for the
     * environment.
     */
    interface EnvParameters {
        /**
         * Get the environment of test-suite-specific configuration values,
         * to be passed to the script used to run each test.
         * @return an environment to be passed to the script used to run
         * each test.
         * @see Parameters#getEnv
         */
        TestEnvironment getEnv();
    }

    //----------------------------------------------------------------------------

    /**
     * An interface providing abstract access to environments, as contained
     * in a set of .jte files, as used by older, legacy test suites. Newer
     * test suites should not use this interface, but should subtype
     * EnvParameters directly.
     */
    interface LegacyEnvParameters extends EnvParameters {
        /**
         * Get the set of files which define the environment used to run
         * the tests. The files are returned as they were set by setEnvFiles.
         * @return the set of files which define the exclude list
         * @see #getAbsoluteEnvFiles
         * @see #setEnvFiles
         */
        File[] getEnvFiles();

        /**
         * Set the files which contain the environment used to run
         * the tests. Relative files will be evaluated relative to the
         * test suite root directory.
         * @param files the set of files which contain the environment
         * to be used
         * @see #getEnvFiles
         * @see #getAbsoluteEnvFiles
         */
        void setEnvFiles(File[] files);

        /**
         * Get the set of files which define the environment used to run
         * the tests. The files are all returned as absolute files.
         * @return the set of files which contact the exclude list
         * @see #getEnvFiles
         * @see #setEnvFiles
         */
        File[] getAbsoluteEnvFiles();

        /**
         * Get the name of the environment to be used, from the set of
         * environments contained in the fles set by setEnvFiles.
         * @return the name of the environment to be used to run the tests
         * @see #setEnvName
         */
        String getEnvName();

        /**
         * Set the name of the environment to be used, from the set of
         * environments contained in the fles set by setEnvFiles.
         * @param name the name of the environment to be used to run the tests
         * @see #getEnvName
         */
        void setEnvName(String name);
    }

    //----------------------------------------------------------------------------

    /**
     * An interface providing basic abstract access to an integer
     * specifying the maximum number of tests that can be run in parallel.
     */
    interface ConcurrencyParameters {
        /**
         * Get an integer specifying the maximum number of tests that may
         * be run in parallel.
         * @return an integer specifying the maximum number of tests that
         * may be run in parallel
         * @see Parameters#getConcurrency
         */
        int getConcurrency();

        /**
         * The lowest permitted value for the concurrency.
         */
        static int MIN_CONCURRENCY = 1;

        /**
         * The highest allowed value for the concurrency.
         */
        static int MAX_CONCURRENCY = 256;
    }

    //----------------------------------------------------------------------------

    /**
     * An interface providing abstract access to an integer
     * specifying the maximum number of tests that can be run in parallel.
     */
    interface MutableConcurrencyParameters extends ConcurrencyParameters {
        /**
         * Set an integer specifying the maximum number of tests that may
         * be run in parallel.
         * @param conc an integer specifying the maximum number of tests that
         * may be run in parallel
         * @see #getConcurrency
         */
        void setConcurrency(int conc);
    }

    //----------------------------------------------------------------------------

    /**
     * An interface providing basic abstract access to an integer
     * specifying a scale factor for the standard timeout used for each test.
     */
    interface TimeoutFactorParameters {
        /**
         * Get an integer specifying a scale factor to be applied to the
         * standard timeout for the test.
         * @return an integer specifying a scale factor to be applied to
         * the standard timeout for each test.
         * @see Parameters#getTimeoutFactor
         */
        float getTimeoutFactor();

        /**
         * The lowest permitted value for timeout factor.
         */
        static float MIN_TIMEOUT_FACTOR = 0.1f;

        /**
         * The highest permitted value for timeout factor.
         */
        static float MAX_TIMEOUT_FACTOR = 100.f;
    }

    //----------------------------------------------------------------------------

    /**
     * An interface providing abstract access to an integer
     * specifying a scale factor for the standard timeout used for each test.
     */
    interface MutableTimeoutFactorParameters extends TimeoutFactorParameters {
        /**
         * Set an integer specifying a scale factor to be applied to the
         * standard timeout for the test.
         * @param factor an integer specifying a scale factor to be applied to
         * the standard timeout for each test.
         * @see #getTimeoutFactor
         */
        void setTimeoutFactor(float factor);
    }
}
