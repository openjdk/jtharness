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
package com.sun.javatest.interview;

import com.sun.interview.ChoiceArrayQuestion;
import com.sun.interview.FinalQuestion;
import com.sun.interview.Interview;
import com.sun.interview.Question;
import com.sun.interview.YesNoQuestion;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.Parameters;
import com.sun.javatest.Status;
import com.sun.javatest.StatusFilter;
import com.sun.javatest.TestFilter;
import com.sun.javatest.TestResultTable;
import com.sun.javatest.WorkDirectory;

/**
 * This interview collects the "prior status" test filter parameters.
 * It is normally used as one of a series of sub-interviews that collect
 * the parameter information for a test run.
 */
public class PriorStatusInterview
    extends Interview
    implements Parameters.MutablePriorStatusParameters
{
    /**
     * Create an interview.
     * @param parent The parent interview of which this is a child.
     * @throws Interview.Fault if there is a problem while creating the interview.
     */
    public PriorStatusInterview(InterviewParameters parent)
        throws Interview.Fault
    {
        super(parent, "priorStatus");
        this.parent = parent;
        setResourceBundle("i18n");
        setHelpSet("/com/sun/javatest/moreInfo/moreInfo.hs");
        setFirstQuestion(qNeedStatus);
    }

    /**
     * Get an array of boolean values from the interview, which indicate
     * which "prior status" values will cause a test to be selected for execution.
     * The array of values can be indexed with {@link Status#PASSED}, {@link Status#FAILED},
     * etc.
     * @return an array of boolean values which indicate which "prior status" values
     * will cause a test to be selected for execution.
     * @see #setPriorStatusValues
     */
    public boolean[] getPriorStatusValues() {
        if (qNeedStatus.getValue() == YesNoQuestion.YES)
            return getMatchPriorStatusValues();
        else
            return null;
    }

    public void setPriorStatusValues(boolean[] b) {
        if (b == null)
            setPriorStatusMode(NO_PRIOR_STATUS);
        else {
            setPriorStatusMode(MATCH_PRIOR_STATUS);
            setMatchPriorStatusValues(b);
        }
    }

    public int getPriorStatusMode() {
        return (qNeedStatus.getValue() == YesNoQuestion.YES ? MATCH_PRIOR_STATUS : NO_PRIOR_STATUS);
    }

    public void setPriorStatusMode(int mode) {
        qNeedStatus.setValue(mode == MATCH_PRIOR_STATUS ? YesNoQuestion.YES : YesNoQuestion.NO);
    }

    public boolean[] getMatchPriorStatusValues() {
        boolean[] choices = qStatus.getValue();
        boolean[] b = new boolean[Status.NUM_STATES];
        b[Status.ERROR] = choices[0];
        b[Status.FAILED] = choices[1];
        b[Status.NOT_RUN] = choices[2];
        b[Status.PASSED] = choices[3];
        return b;
    }

    /**
     * Set an array of boolean values in the interview, which indicate
     * which "prior status" values will cause a test to be selected for execution.
     * The array of values can be indexed with {@link Status#PASSED}, {@link Status#FAILED},
     * etc.
     * @param b an array of {@link Status#NUM_STATES} boolean values which indicate
     * which "prior status" values will cause a test to be selected for execution.
     * @see #getMatchPriorStatusValues
     */
    public void setMatchPriorStatusValues(boolean[] b) {
        if (b.length != Status.NUM_STATES)
            throw new IllegalArgumentException();

        boolean[] choices = new boolean[Status.NUM_STATES];
        choices[0] = b[Status.ERROR];
        choices[1] = b[Status.FAILED];
        choices[2] = b[Status.NOT_RUN];
        choices[3] = b[Status.PASSED];
        qStatus.setValue(choices);
    }


    /**
     * Get a test filter generated from the status test values in the interview.
     * @return a test filter generated from the status test values in the interview
     * @see #getPriorStatusValues
     */
    public TestFilter getStatusFilter() {
        updateCachedStatusFilter();
        return cachedStatusFilter;
    }

    //----------------------------------------------------------------------------
    //
    // Need status

    private YesNoQuestion qNeedStatus = new YesNoQuestion(this, "needStatus", YesNoQuestion.NO) {
        protected Question getNext() {
            if (value == null)
                return null;
            else if (value == YES)
                return qStatus;
            else
                return qEnd;
        }
    };


    //----------------------------------------------------------------------------
    //
    // Status

    // I18N...
    private static final String PASSED = "passed";
    private static final String FAILED = "failed";
    private static final String ERROR = "error";
    private static final String NOT_RUN = "not_run";
    private static int[] choiceToStatus =  {Status.ERROR, Status.FAILED,
                                            Status.NOT_RUN, Status.PASSED};

    private ChoiceArrayQuestion qStatus = new ChoiceArrayQuestion(this, "status") {
        {
            setChoices(new String[] {ERROR, FAILED, NOT_RUN, PASSED}, true);
        }

        public boolean isValueValid() {
            // one of the choices must be set
            for (int i = 0; i < value.length; i++) {
                if (value[i])
                    return true;
            }
            return false;
        }


        protected Question getNext() {
            return qEnd;
        }
    };

    private void updateCachedStatusFilter() {
        WorkDirectory wd = parent.getWorkDirectory();
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

    private StatusFilter cachedStatusFilter;

    //----------------------------------------------------------------------------
    //
    // End

    private Question qEnd = new FinalQuestion(this);

    //--------------------------------------------------------

    private InterviewParameters parent;
}
