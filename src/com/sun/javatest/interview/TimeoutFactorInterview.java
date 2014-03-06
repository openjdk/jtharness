/*
 * $Id$
 *
 * Copyright (c) 2001, 2009, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.interview.FinalQuestion;
import com.sun.interview.FloatQuestion;
import com.sun.interview.Interview;
import com.sun.interview.Question;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.Parameters;

/**
 * This interview collects the timeout factor parameter. It is normally used as
 * one of a series of sub-interviews that collect the parameter information for
 * a test run.
 */
public class TimeoutFactorInterview
    extends Interview
    implements Parameters.MutableTimeoutFactorParameters
{
    /**
     * Create an interview.
     * @param parent The parent interview of which this is a child.
     * @throws Interview.Fault if there is a problem while creating the interview.
     */
    public TimeoutFactorInterview(InterviewParameters parent)
        throws Interview.Fault
    {
        super(parent, "timeout");
        this.parent = parent;
        setResourceBundle("i18n");
        setHelpSet("/com/sun/javatest/moreInfo/moreInfo.hs");
        setFirstQuestion(qTimeout);
    }

    /**
     * Get the value for the timeout factor from the interview.
     * @return a value representing the desired timeout factor for a test run.
     * @see #setTimeoutFactor
     */
    public float getTimeoutFactor() {
        return qTimeout.getValue();
    }

    /**
     * Set the value of the timeout factor in the interview.
     * @param t The desired value for the timeout factor.
     * @see #getTimeoutFactor
     */
    public void setTimeoutFactor(float t) {
        qTimeout.setValue(t);
    }

    //----------------------------------------------------------------------------
    //
    // Timeout

    private FloatQuestion qTimeout = new FloatQuestion(this, "timeout") {
        {
            setBounds(Parameters.TimeoutFactorParameters.MIN_TIMEOUT_FACTOR,
                      Parameters.TimeoutFactorParameters.MAX_TIMEOUT_FACTOR);
            setResolution(0.1f);
        }

        protected Question getNext() {
            return qEnd;
        }

        public void clear() {
            setValue(1);
        }
    };

    //----------------------------------------------------------------------------
    //
    // End

    private Question qEnd = new FinalQuestion(this);

    //--------------------------------------------------------

    private InterviewParameters parent;
}
