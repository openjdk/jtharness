/*
 * $Id$
 *
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.interview;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import com.sun.interview.FinalQuestion;
import com.sun.interview.NullQuestion;
import com.sun.interview.StringQuestion;
import com.sun.interview.Question;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.KnownFailuresList;
import com.sun.javatest.Parameters;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.util.I18NResourceBundle;

import java.io.File;

/**
 * A basic implementation of InterviewParameters that uses standard
 * interviews for all the various interview sections, except the environment
 * section, which remains to be implemented by subtypes.
 */
public abstract class BasicInterviewParameters extends InterviewParameters
{
    /**
     * Create a BasicInterviewParameters object.
     * The test suite for which this interview applies should be set
     * with setTestSuite.
     * @param tag the tag used to qualify questions in this interview
     * @throws Interview.Fault if there is a problem creating this object
     */
    protected BasicInterviewParameters(String tag)
        throws Fault
    {
        super(tag);
        // leave this to the subtype to decide whether to provide or not
        //setHelpSet("/com/sun/javatest/moreInfo/moreInfo.hs");

        iTests = createTestsInterview(this);
        iExcludeList = new ExcludeListInterview(this);
        iKeywords = new KeywordsInterview(this);
        iKfl = new KnownFailuresListInterview(this);
        iPriorStatus = new PriorStatusInterview(this);
        iConcurrency = new ConcurrencyInterview(this, getMaxConcurrency());
        iTimeoutFactor = new TimeoutFactorInterview(this);

        setFirstQuestion(qProlog);
    }

    /**
     * Create a BasicInterviewParameters object.
     * @param tag the tag used to qualify questions in this interview
     * @param ts The test suite to which this interview applies.
     * @throws Interview.Fault if there is a problem creating this object
     */
    protected BasicInterviewParameters(String tag, TestSuite ts)
        throws Fault
    {
        this(tag);
        setTestSuite(ts);
    }

    @Override
    public void dispose() {
        super.dispose();
        iConcurrency = null;

        iExcludeList.dispose();
        iExcludeList = null;

        iKeywords.dispose();
        iKeywords = null;

        iKfl.dispose();
        iKfl = null;
        iPriorStatus = null;

        iTests.dispose();
        iTests = null;

        iTimeoutFactor = null;
        workDir = null;
        testSuite = null;
    }

    /**
     * creation of {#link TestsInterview} is extracted into separate class
     * to enable 'hooks' and return {#link TestsInterview} sub class
     * @param parent
     * @return instance of {#link TestsInterview}
     * @throws com.sun.interview.Interview.Fault
     */
    protected TestsInterview createTestsInterview(InterviewParameters parent)
            throws InterviewParameters.Fault{
        return new TestsInterview(parent);
    }

    /**
     * Specify whether or not to include standard questions in the
     * prolog to get a name and description for this configuration.
     * If these standard questions are not used, it is the responsibility
     * of the EnvParameters interview to get the name and description.
     * If the standard prolog is bypassed by using setFirstQuestion,
     * this method has no effect.
     * @param on if true, questions will be included in the standard
     * prolog to get a name and description for this configuration.
     * @see #isNameAndDescriptionInPrologEnabled
     */
    public void setNameAndDescriptionInPrologEnabled(boolean on) {
        if (on) {
            // defer the initialization of these to avoid possible
            // conflicts in subtypes
            initNameQuestion();
            initDescriptionQuestion();
        }

        nameAndDescriptionInPrologEnabled = on;
    }

    /**
     * Check whether or not to include standard questions in the
     * prolog to get a name and description for this configuration.
     * If these standard questions are not used, it is the responsibility
     * of the EnvParameters interview to get the name and description.
     * @return true if the standard questions should be included,
     * and false otherwise.
     * @see #setNameAndDescriptionInPrologEnabled
     */
    public boolean isNameAndDescriptionInPrologEnabled() {
        return nameAndDescriptionInPrologEnabled;
    }

    /**
     * Get the name for this configuration.
     * If the standard question for the name has been included in the prolog,
     * it will be used to get the result; otherwise the default
     * implementation to get the name from the environment will be used.
     * @return the name for this configuration, or null if not known
     * @see #setNameAndDescriptionInPrologEnabled
     */
    @Override
    public String getName() {
        if (nameAndDescriptionInPrologEnabled) {
            return qName.getValue();
        }
        else {
            return super.getName();
        }
    }

    /**
     * Get a description for this configuration.
     * If the standard question for the description has been included in the prolog,
     * it will be used to get the result; otherwise the default
     * implementation to get the description from the environment will be used.
     * @return a description for this configuration, or null if not known
     */
    @Override
    public String getDescription() {
        if (nameAndDescriptionInPrologEnabled) {
            return qDescription.getValue();
        }
        else {
            return super.getDescription();
        }
    }


    @Override
    public TestSuite getTestSuite() {
        return testSuite;
    }

    /**
     * Set the test suite for the test run. The test suite may only be set once.
     * @param ts the test suite to be set.
     * @see #getTestSuite
     * @throws NullPointerException if ts is null
     * @throws IllegalStateException if the test suite has already been set to
     * something different
     */
    @Override
    public void setTestSuite(TestSuite ts) {
        if (ts == null) {
            throw new NullPointerException();
        }

        if (testSuite != null && testSuite != ts) {
            throw new IllegalStateException();
        }

        testSuite = ts;
    }

    @Override
    public WorkDirectory getWorkDirectory() {
        return workDir;
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
    @Override
    public void setWorkDirectory(WorkDirectory wd) {
        if (wd == null) {
            throw new NullPointerException();
        }

        if (workDir != null && workDir != wd) {
            throw new IllegalStateException();
        }

        workDir = wd;
    }

    //--------------------------------------------------------------------------

    @Override
    public Parameters.TestsParameters getTestsParameters() {
        return iTests;
    }

    @Override
    protected Question getTestsFirstQuestion() {
        return callInterview(iTests, getTestsSuccessorQuestion());
    }

    //--------------------------------------------------------------------------

    @Override
    public Parameters.ExcludeListParameters getExcludeListParameters() {
        return iExcludeList;
    }

    @Override
    protected Question getExcludeListFirstQuestion() {
        //return callInterview(iExcludeList, getExcludeListSuccessorQuestion());
        return callInterview(iExcludeList, getExcludeListSuccessorQuestion());
    }

    @Override
    protected Question getExcludeListSuccessorQuestion() {
        return getKflFirstQuestion();
    }

    //--------------------------------------------------------------------------

    protected Question getKflFirstQuestion() {
        return callInterview(iKfl, getKflSuccessorQuestion());
    }

    protected Question getKflSuccessorQuestion() {
        return getKeywordsFirstQuestion();
    }


    /**
     * Get the combined known failures list - a calculated combination of
     * all the input KFL files which were specified.  The value returned need
     * not be generated from a real file though, it can be software generated.
     * @return The effective known failed list, possibly combined from many
     *    physical files.  Null if there was a problem creating it or if there
     *    are no lists (files) available.
     */
    @Override
    public KnownFailuresList getKnownFailuresList() {
        return iKfl.getKfl();
    }

    @Override
    public void setKnownFailureFiles(final File[] files) {
        super.setKnownFailureFiles(files);

        iKfl.setKflFiles(files);
    }

    @Override
    public File[] getKnownFailureFiles() {
        return iKfl.getKflFiles();
    }

    //--------------------------------------------------------------------------

    @Override
    public Parameters.KeywordsParameters getKeywordsParameters() {
        TestSuite ts = getTestSuite();
        String[] kw = (ts == null ? null : ts.getKeywords());
        return (kw == null || kw.length > 0 ? iKeywords : null);
    }

    @Override
    protected Question getKeywordsFirstQuestion() {
        TestSuite ts = getTestSuite();
        String[] kw = (ts == null ? null : ts.getKeywords());
        if (kw == null || kw.length > 0) {
            return callInterview(iKeywords, getKeywordsSuccessorQuestion());
        }
        else {
            return getKeywordsSuccessorQuestion();
        }
    }

    //--------------------------------------------------------------------------

    @Override
    public Parameters.PriorStatusParameters getPriorStatusParameters() {
        return iPriorStatus;
    }

    @Override
    protected Question getPriorStatusFirstQuestion() {
        return callInterview(iPriorStatus, getPriorStatusSuccessorQuestion());
    }

    //--------------------------------------------------------------------------

    @Override
    public Parameters.ConcurrencyParameters getConcurrencyParameters() {
        return iConcurrency;
    }

    @Override
    protected Question getConcurrencyFirstQuestion() {
        return callInterview(iConcurrency, getConcurrencySuccessorQuestion());
    }

    /**
     * Discover the maximum allowable concurrency value that should be accepted.
     * @return The maximum allowable concurrency value that should be accepted
     *     by the interview.  Default value is 50 (legacy value).  A reasonable
     *     long-term ongoing value would be
     *     Parameters.ConcurrencyParameters.MAX_CONCURRENCY.
     * @see com.sun.javatest.Parameters.ConcurrencyParameters#MAX_CONCURRENCY
     */
    protected int getMaxConcurrency() {
        // return Parameters.ConcurrencyParameters.MAX_CONCURRENCY;
        return 50;
    }

    //--------------------------------------------------------------------------

    @Override
    public Parameters.TimeoutFactorParameters getTimeoutFactorParameters() {
        return iTimeoutFactor;
    }

    @Override
    protected Question getTimeoutFactorFirstQuestion() {
        return callInterview(iTimeoutFactor, getTimeoutFactorSuccessorQuestion());
    }

    //--------------------------------------------------------------------------

    @Override
    protected Question getEpilogFirstQuestion() {
        return qEpilog;
    }

    //--------------------------------------------------------------------------

    private String getResourceStringX(String key) {
        // try and get it from the interview bundles first
        String s = getResourceString(key, true);
        if (s != null) {
            return s;
        }

        // otherwise, default to using the bundle for this class
        try {
            return i18n.getString(key);
        }
        catch (MissingResourceException e) {
            return key;
        }
    }


    private Object getHelpIDX(String target) {
        return helpSetFactory.createHelpID(this.getHelpSet(), target);
    }

    private TestSuite testSuite;
    private WorkDirectory workDir;
    protected TestsInterview iTests;
    private ExcludeListInterview iExcludeList;
    private KnownFailuresListInterview iKfl;
    private KeywordsInterview iKeywords;
    private PriorStatusInterview iPriorStatus;
    private ConcurrencyInterview iConcurrency;
    private TimeoutFactorInterview iTimeoutFactor;
    private boolean nameAndDescriptionInPrologEnabled;


    private NullQuestion qProlog = new NullQuestion(this, "prolog") {
        @Override
            public Question getNext() {
                if (nameAndDescriptionInPrologEnabled) {
                    return qName;
                }
                else {
                    return getPrologSuccessorQuestion();
                }
            }

            @Override
            public Object getHelpID() {
                Object id = super.getHelpID();
                if (id == null) {
                    id = getHelpIDX("BasicInterviewParameters.prolog");
                    setHelpID(id);
                }
                return id;

            }

        @Override
            public String getSummary() {
                if (summary == null) {
                    summary = getResourceStringX("BasicInterviewParameters.prolog.smry");
                }
                return summary;
            }

        @Override
            public String getText() {
                if (text == null) {
                    text = getResourceStringX("BasicInterviewParameters.prolog.text");
                }
                return MessageFormat.format(text, getTextArgs());
            }

        @Override
            public Object[] getTextArgs() {
                String name = (testSuite == null ? null : testSuite.getName());
                return new Object[] { new Integer(name == null ? 0 : 1), name };
            }

            private Object helpID;
            private String summary;
            private String text;
        };

    private StringQuestion qName;
    private void initNameQuestion() {
        qName = new StringQuestion(this, "name") {
            @Override
                public boolean isValueValid() {
                    return isValidIdentifier(value);
                }

            @Override
                public Question getNext() {
                    return qDescription;
                }

                @Override
                public Object getHelpID() {
                    Object id = super.getHelpID();
                    if (id == null) {
                        id = getHelpIDX("BasicInterviewParameters.name");
                        setHelpID(id);
                    }
                    return id;
                }

            @Override
                public String getSummary() {
                    if (summary == null) {
                        summary = getResourceStringX("BasicInterviewParameters.name.smry");
                    }
                    return summary;
                }

            @Override
                public String getText() {
                    if (text == null) {
                        text = getResourceStringX("BasicInterviewParameters.name.text");
                    }
                    return MessageFormat.format(text, getTextArgs());
                }

            @Override
                public Object[] getTextArgs() {
                    String name = (testSuite == null ? null : testSuite.getName());
                    return new Object[] { new Integer(name == null ? 0 : 1), name };
                }

                private Object helpID;
                private String summary;
                private String text;
            };
    }

    private static boolean isValidIdentifier(String name) {
        if (name == null || name.length() == 0) {
            return false;
        }

        // first character must be a letter
        if (!Character.isLetter(name.charAt(0))) {
            return false;
        }

        // subsequent characters must be a letter or digit or _
        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if ( !Character.isLetterOrDigit(c) && c != '_') {
                return false;
            }
        }

        // all tests passed
        return true;
    }

    private StringQuestion qDescription;
    private void initDescriptionQuestion() {
        qDescription = new StringQuestion(this, "description") {
            @Override
                public boolean isValueValid() {
                    return (value != null && value.length() > 0);
                }

            @Override
                public Question getNext() {
                    return getPrologSuccessorQuestion();
                }

                @Override
                public Object getHelpID() {
                    Object id = super.getHelpID();
                    if (id == null) {
                        id = getHelpIDX("BasicInterviewParameters.description");
                        setHelpID(id);
                    }
                    return id;
                }

            @Override
                public String getSummary() {
                    if (summary == null) {
                        summary = getResourceStringX("BasicInterviewParameters.description.smry");
                    }
                    return summary;
                }

            @Override
                public String getText() {
                    if (text == null) {
                        text = getResourceStringX("BasicInterviewParameters.description.text");
                    }
                    return MessageFormat.format(text, getTextArgs());
                }

            @Override
                public Object[] getTextArgs() {
                    String name = (testSuite == null ? null : testSuite.getName());
                    return new Object[] { new Integer(name == null ? 0 : 1), name };
                }

                private Object helpID;
                private String summary;
                private String text;
            };
    }

    private FinalQuestion qEpilog = new FinalQuestion(this, "epilog") {

            @Override
            public Object getHelpID() {
                Object id = super.getHelpID();
                if (id == null) {
                    id = getHelpIDX("BasicInterviewParameters.epilog");
                    setHelpID(id);
                }
                return id;
            }

        @Override
            public String getSummary() {
                if (summary == null) {
                    summary = getResourceStringX("BasicInterviewParameters.epilog.smry");
                }
                return summary;
            }

        @Override
            public String getText() {
                if (text == null) {
                    text = getResourceStringX("BasicInterviewParameters.epilog.text");
                }
                return MessageFormat.format(text, getTextArgs());
            }

            private Object helpID;
            private String summary;
            private String text;
        };

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(BasicInterviewParameters.class);
}
