/*
 * $Id$
 *
 * Copyright (c) 2012, 2015, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.interview;

import com.sun.javatest.TestSuite;
import com.sun.javatest.tool.jthelp.HelpID;
import com.sun.javatest.tool.jthelp.HelpSet;

import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * Class that contains a number of static method to work with both JavaTest
 * and JavaHelp objects to make the core JavaTest classes unaware of JavaHelp.
 */
public class Help {

    static final ResourceBundle i18n = ResourceBundle.getBundle("com.sun.interview.i18n");

    /**
     * Get the help set used to locate the "more info" for each question.
     *
     * @param i interview
     * @return object returned by i.getHelpSet() casted to the HelpSet type.
     * @see Interview#setHelpSet
     */
    public static HelpSet getHelpSet(Interview i) {
        if (i == null) {
            return null;
        }
        return (HelpSet)i.getHelpSet();
    }

    /**
     * Get the JavaHelp HelpID identifying the "more info" help for this
     * question, or null if none.
     * @return the JavaHelp HelpID identifying the "more info" help for this
     * question, or null if none.
     */
    public static HelpID getHelpID(Question q) {
        if (q == null) {
            return null;
        }
        Object id = q.getHelpID();
        if (id != null && id instanceof HelpID) {
            return (HelpID)id;
        }

        HelpID helpID = getID(q.getInterview(), q.getKey());
        if (helpID != null) {
            q.setHelpID(helpID);
        }
        return helpID;
    }

    /**
     * Creates HelpID for given interview and question key
     * @param i interview
     * @param key question key
     * @return HelpID to be used for the question.
     */
    private static HelpID getID(Interview i, String key) {
        HelpID helpId = null;
        if (i.getParent() != null)
            helpId = getID(i.getParent(), key);

        if (helpId != null) {
            //System.err.println("Q: int:" + i.getTag() + " key:" + key + " helpId:" + helpId);
            return helpId;
        }

        HelpSet hs = getHelpSet(i);
        if (hs != null) {
            HashMap m = hs.getLocalMap();
            if (m != null) {
                helpId = HelpID.create(key, hs);
                //System.err.println("Q: FOUND int:" + i.getTag() + " key:" + key + " helpId:" + helpId);
                return helpId;
            }
        }
        return null;
    }

    /**
     * Get helpsets containing any related documents for this test suite.
     * By default, the resource names for the help sets are obtained from
     * getAdditionalDocNames().
     * @param ts The test suite to look for the docs within.
     * @return an array of help sets containing docs associated with this
     * testsuite. The array will be empty if there are no such docs.
     * @throws Help.Fault if there are problems opening any of the
     * helpsets.
     */
    public static HelpSet[] getAdditionalDocs(TestSuite ts) throws Help.Fault {
        HelpSet[] additionalDocs = null;
        String[] names = ts.getAdditionalDocNames();
        if (names == null)
           additionalDocs = new HelpSet[0];
        else {
            HelpSet[] docs = new HelpSet[names.length];
            ClassLoader loader = ts.getClassLoader();
            for (int i = 0; i < names.length; i++) {
                String name = names[i];
                URL u = HelpSet.findHelpSet(loader, name);
                if (u == null) {
                    throw new Help.Fault(i18n, "ts.cantFindDoc", name);
                }
                docs[i] = new HelpSet(loader, u);
            }
            // only set if all opened OK
            additionalDocs = docs;
        }
        return additionalDocs;
    }

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
        public Fault(ResourceBundle i18n, String s) {
            super(i18n.getString(s));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An argument to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        public Fault(ResourceBundle i18n, String s, Object o) {
            super(MessageFormat.format(i18n.getString(s), new Object[] {o}));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An array of arguments to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        public Fault(ResourceBundle i18n, String s, Object[] o) {
            super(MessageFormat.format(i18n.getString(s), o));
        }
    }

}
