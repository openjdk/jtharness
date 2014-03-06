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
package com.sun.javatest.interview;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import com.sun.interview.FinalQuestion;
import com.sun.interview.Interview;
import com.sun.interview.Question;
import com.sun.interview.StringQuestion;
import com.sun.interview.YesNoQuestion;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.Parameters;
import com.sun.javatest.Keywords;
import com.sun.javatest.KeywordsFilter;
import com.sun.javatest.TestFilter;
import com.sun.javatest.TestSuite;
import com.sun.javatest.util.StringArray;

/**
 * This interview collects the keyword parameters. It is normally used as
 * one of a series of sub-interviews that collect the parameter information for
 * a test run.
 */
public class KeywordsInterview
    extends Interview
    implements Parameters.MutableKeywordsParameters
{
    /**
     * Create an interview.
     * @param parent The parent interview of which this is a child.
     * @throws Interview.Fault if there is a problem while creating the interview.
     */
    public KeywordsInterview(InterviewParameters parent)
        throws Interview.Fault
    {
        super(parent, "keywords");
        this.parent = parent;
        setResourceBundle("i18n");
        setHelpSet("/com/sun/javatest/moreInfo/moreInfo.hs");
        setFirstQuestion(qNeedKeywords);
    }

    public void dispose() {
        cachedKeywords = null;
        cachedKeywords_expr = null;
        cachedKeywordsError = null;
        cachedKeywordsFilter = null;
    }


    /**
     * Get a Keywords object based on the information in the interview.
     * @return A Keywords object based on the information in the interview.
     * @see #setKeywords
     */
    public Keywords getKeywords() {
        if (qNeedKeywords.getValue() == YesNoQuestion.YES) {
            updateCachedKeywordsData();
            return cachedKeywords;
        }
        else
            return null;
    }

    public void setKeywords(int mode, String value) {
        if (value == null)
            setKeywordsMode(NO_KEYWORDS);
        else {
            setKeywordsMode(MATCH_KEYWORDS);
            setMatchKeywords(mode, value);
        }
    }

    public int getKeywordsMode() {
        return (qNeedKeywords.getValue() == YesNoQuestion.YES
                ? MATCH_KEYWORDS : NO_KEYWORDS);
    }

    public void setKeywordsMode(int mode) {
        qNeedKeywords.setValue(mode == MATCH_KEYWORDS ? YesNoQuestion.YES : YesNoQuestion.NO);
    }

    public int getMatchKeywordsMode() {
        return qKeywords.getMode();
    }

    public String getMatchKeywordsValue() {
        return qKeywords.getModeValue();
    }

    public void setMatchKeywords(int mode, String value) {
        qKeywords.setValue(mode, value);
    }


    /**
     * Get a test filter based on the keyword expression in the interview.
     * @return a test filter based on the keyword expression in the interview.
     */
    public TestFilter getKeywordFilter() {
        if (qNeedKeywords.getValue() == YesNoQuestion.YES) {
            updateCachedKeywordsData();
            return cachedKeywordsFilter;
        }
        else
            return null;
    }

    //----------------------------------------------------------------------------
    //
    // Need keywords

    private YesNoQuestion qNeedKeywords = new YesNoQuestion(this, "needKeywords", YesNoQuestion.NO) {
        protected Question getNext() {
            if (value == null)
                return null;
            else if (value == YES)
                return qKeywords;
            else
                return qEnd;
        }
    };

    //----------------------------------------------------------------------------
    //
    // Keywords

    private static abstract class KeywordsQuestion extends StringQuestion
    {
        KeywordsQuestion(Interview i, String t) {
            super(i, t);
        }

        public int getMode() {
            return mode;
        }

        public String getModeValue() {
            return modeValue;
        }

        public void setValue(int m, String v) {
            //System.err.println("KI: m=" + m + " v=" + v);
            switch (m) {
            case ANY_OF:
                mode = ANY_OF;
                modeValue = v;
                super.setValue(termsToExpr(v, " | "));
                break;

            case ALL_OF:
                mode = ALL_OF;
                modeValue = v;
                super.setValue(termsToExpr(v, " & "));
                break;

            default:
                mode = EXPR;
                modeValue = v;
                super.setValue(v == null || v.length() == 0 ? null : v);
            }
        }

        public void setValue(String v) {
            //System.err.println("KI: v=" + v);
            if (v != null && v.equals(getValue()))
                return;

            mode = EXPR;
            modeValue = v;
            super.setValue(modeValue);
        }

        public void load(Map data) {
            Object m = data.get(tag + ".mode");
            Object v = data.get(tag + ".value");
            //System.err.println("KI.load: m=" + m + " v=" + v);
            if (m == null)
                super.load(data); // support existing interview files
            else if (m instanceof String && (v == null || v instanceof String)) {
                String ms = (String) m;
                String vs = (String) v;
                if (ms.equals("allOf"))
                    setValue(ALL_OF, vs);
                else if (ms.equals("anyOf"))
                    setValue(ANY_OF, vs);
                else
                    setValue(EXPR, vs);
            }
        }

        public void save(Map data) {
            super.save(data);  // for backwards compatibility
            String modeText = (mode == ANY_OF ? "anyOf" : mode == ALL_OF ? "allOf" : "expr");
            data.put(tag + ".mode", modeText);
            if (modeValue != null)
                data.put(tag + ".value", modeValue);
            //System.err.println("KI.save: m=" + mode + " v=" + modeValue);
        }

        private String termsToExpr(String list, String op) {
            if (list == null || list.length() == 0)
                return null;

            String[] keys = StringArray.split(list);
            StringBuffer sb = new StringBuffer(list.length());
            for (int i = 0; i < keys.length; i++) {
                if (i > 0)
                    sb.append(op);
                sb.append(keys[i]);
            }
            //System.err.println("KI.t2E: list=" + list + " result=" + sb);
            return sb.toString();
        }

        private int mode;
        private String modeValue;
    }

    private KeywordsQuestion qKeywords = new KeywordsQuestion(this, "keywords") {
        protected Question getNext() {
            if (value == null || value.length() == 0)
                return null;
            else {
                return qEnd;
            }
        }

        public boolean isValueValid() {
            updateCachedKeywordsData();
            return cachedKeywordsError == null;
        }
    };

    private void updateCachedKeywordsData() {
        String expr = qKeywords.getValue();
        if (!equal(cachedKeywords_expr, expr)) {
            try {
                TestSuite ts = parent.getTestSuite();

                if (ts == null) {
                    throw new IllegalStateException("Null TestSuite, cannot get keyword info");
                }

                String[] validKeywords = ts.getKeywords();
                HashSet validKeywordsSet;
                if (validKeywords == null)
                    validKeywordsSet = null;
                else
                    validKeywordsSet = new HashSet(Arrays.asList(validKeywords));

                int mode = qKeywords.getMode();
                String modeName = (mode == ANY_OF ? Keywords.ANY_OF
                                   : mode == ALL_OF ? Keywords.ALL_OF
                                   : Keywords.EXPR);
                Keywords k = Keywords.create(modeName, qKeywords.getModeValue(), validKeywordsSet);
                cachedKeywords = k;
                cachedKeywordsFilter = new KeywordsFilter(k);
                cachedKeywordsError = null;
            }
            catch (Keywords.Fault e) {
                cachedKeywords = null;
                cachedKeywordsFilter = null;
                cachedKeywordsError = e.getMessage();
            }
            cachedKeywords_expr = expr;
        }
    }

    private Keywords cachedKeywords;
    private String cachedKeywords_expr;
    private String cachedKeywordsError;
    private TestFilter cachedKeywordsFilter;

    private Question qEnd = new FinalQuestion(this);

    //----------------------------------------------------------------------------

    private static boolean equal(String s1, String s2) {
        return (s1 == null ? s2 == null : s1.equals(s2));
    }

    //--------------------------------------------------------

    private InterviewParameters parent;
}
