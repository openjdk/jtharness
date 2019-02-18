/*
 * $Id$
 *
 * Copyright (c) 1996, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.finder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.sun.javatest.TestEnvironment;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.StringArray;

/**
 * This class allows a new tag "@expand" which allows a single test
 * description to be expanded into multiple test descriptions using
 * variable substitution.  Variables are declared in the .jte file.
 *
 * The list of valid entries is identical to those found for JCK.
 * There is no list of pre-defined keywords.  If keyword checking
 * is needed, then the list of allowed keywords must be
 * provided via the <code>-allowKeyword</code> option.
 *
 * @see TagTestFinder
 */
public class ExpandTestFinder extends TagTestFinder
{
    //--------------------------------------------------------------------------

    // This code is only needed to ensure that this test finder
    // behaves the same as JCKTagTestFinder.  If that finder ever
    // derives from this one, it may be removed.

    public ExpandTestFinder() {
        validEntries = initTable(stdValidEntries);
        validEntries = addTableItem(validEntries, "test", TRUE);
        validKeywords = initTable(stdValidKeywords);
        validKeywords = initTable();
        addExtension(".jasm", JavaCommentStream.class);
        addExtension(".jcod", JavaCommentStream.class);
    } // ExpandTestFinder()

    @Override
    protected int decodeArg(String[] args, int i) throws Fault {
        if (args[i].equals("-verify")) {
            verify = true;
            return 1;
        }
        else if (args[i].equals("-allowEntry")) {
            String e = args[i+1];
            validEntries.put(e.toLowerCase(), e);
            return 2;
        }
        else if (args[i].equals("-allowKeyword")) {
            String k = args[i+1];
            validKeywords.put(k.toLowerCase(), k);
            return 2;
        }
        else
            return super.decodeArg(args, i);
    } // decodeArg()

    private Map<String, String> initTable(String... entries) {
        Map<String, String> map = new HashMap<>();
        for (String entry : entries) map.put(entry.toLowerCase(), entry);
        return map;
    } // initTable()

    private Map<String, String> addTableItem(Map<String, String> entries, String name, String value) {
        entries.put(name, value);
        return entries;
    } // addTableItem()

    private boolean verify;
    private Map<String, String> validEntries;
    private Map<String, String> validKeywords;

    private static final String TESTSUITE_HTML = "testsuite.html";
    // end of code for JCKTagTestFinder emulation

    //--------------------------------------------------------------------------

    @Override
    public void init(String [] args, File testSuiteRoot, TestEnvironment env) throws Fault {
        // grab all environment variables open for expansion
        if (expandVars == null) {
            expandVars   = new HashMap<>(3);
            expandVarLen = new HashMap<>(3);

            for (String s : env.keys()) {
                try {
                    String n = s;

                    if (!n.startsWith("expand."))
                        continue;
                    String[] v = env.lookup(n);
                    String fqvName = n.substring("expand.".length());
                    // add to hashtable of fully-qualified varNames
                    expandVars.put(fqvName, v);

                    int pos;
                    if ((pos = fqvName.indexOf(".")) == -1)
                        continue;
                    String stem = fqvName.substring(0, pos);

                    // checking lengths of co-joined variables
                    Integer len = expandVarLen.get(stem);
                    if (len == null) {
                        // add to hashtable of valid stems
                        expandVarLen.put(stem, Integer.valueOf(v.length));
                    } else {
                        if (v.length != len.intValue()) {
                            error(i18n, "expand.lengthMismatch",
                                    stem, fqvName.substring(pos + 1));
                        }
                    }
                } catch (TestEnvironment.Fault f) {
                    error(i18n, "expand.noDefn", f.getMessage());
                }
            }
        }

        // This part of this method is only needed to ensure that this
        // test finder behaves the same as JCKTagTestFinder.  If that
        // finder ever derives from this one, it may be removed.
        if (testSuiteRoot.isDirectory()) {
            File f = new File(testSuiteRoot, TESTSUITE_HTML);
            if (!(f.exists() && !f.isDirectory() && f.canRead())) {
                throw new Fault(i18n, "expand.badRootDir",
                        TESTSUITE_HTML, testSuiteRoot.getPath());
            }
        }
        else {
            String name = testSuiteRoot.getName();
            if (!name.equals(TESTSUITE_HTML)) {
                throw new Fault(i18n, "expand.badRootFile");
            }
            // force the test suite root to be the directory
            testSuiteRoot = new File(testSuiteRoot.getParent());
        }
        // end of code for JCKTagTestFinder emulation

        super.init(args, testSuiteRoot, env);
    } // init()

    @Override
    protected void foundTestDescription(Map<String, String> entries, File file, int line) {
        // cross-product and loop call up
        String origId = entries.get("id");
        if (origId == null)
            origId = "";
        Map<String, Integer> loopVars = new HashMap<>(3);
        foundTestDescription_1(entries, file, line, loopVars, origId);
    } // foundTestDescription()

    private void foundTestDescription_1(Map<String, String> entries, File file, int line, Map<String, Integer> loopVars, String id) {
        for (String name : entries.keySet()) {
            String value = entries.get(name);
//          System.out.println("------ NAME:  " + name + " VALUE: " + value);

            if (name.equals("title") || name.equals("test") || name.equals("id"))
                // don't tokenize
                continue;

            String[] words = StringArray.split(value);
            for (int i = 0; i < words.length; i++) {
                if (words[i].startsWith("$")) {
                    String varName = words[i].substring(1);

                    // strip out {}'s
                    if (varName.startsWith("{")) {
                        if (!varName.endsWith("}")) {
                            error(i18n, "expand.missingCloseCurly", words[i]);
                            continue;
                        }
                        varName = varName.substring(1, varName.length() - 1);
                    }

                    // separate foo.bar into stem=foo and qualifier=bar
                    String stem;
                    String qualifier;
                    int pos;
                    if ((pos = varName.indexOf(".")) == -1) {
                        stem = varName;
                        qualifier = "";
                    } else {
                        stem = varName.substring(0, pos);
                        qualifier = varName.substring(pos + 1);
                    }

//                  System.out.println("stem: " + stem + " qual: " + qualifier);

                    if (testStems.get(stem) == null)
                        // this is something we shouldn't expand
                        continue;

                    String[] valueList;
                    if ((valueList = expandVars.get(varName)) == null) {
                        //String [] msgs = {"unable to find `expand' definition in environment",
                        //words[i]};
                        //error(msgs);
                        continue;
                    }

                    String saveId = id;
                    Integer idx = loopVars.get(stem);
                    if (idx != null) {
                        int j = idx.intValue();
                        words[i] = valueList[j];

                        id = saveId + "_" + words[i];
                        entries.put("id", id);

                        entries.put(name, StringArray.join(words));
                    } else {
                        for (int j = 0; j < valueList.length; j++) {
                            words[i] = valueList[j];

                            id = saveId + "_" + words[i];
                            entries.put("id", id);

                            entries.put(name, StringArray.join(words));
                            boolean loopy = !qualifier.isEmpty();
                            if (loopy) loopVars.put(stem, Integer.valueOf(j));
                            // clone needed here because we over-wrote words[i]
                            foundTestDescription_1(new HashMap<>(entries), file, line,
                                    loopVars, id);
                            if (loopy) loopVars.remove(stem);

                        }
                        return;
                    }
                }
            }
        }
        // clone may not be necessary, check for TestDescription upgrades
        // super.foundTestDescription(entries, file, line);
        super.foundTestDescription(new HashMap<>(entries), file, line);
    } // foundTestDescription_1()

    @Override
    protected void processEntry(Map<String, String> entries, String name, String value) {
//      System.out.println("NAME: " + name + " VALUE: " + value);
        if (name.equals("expand")) {
            if (testStems != null) {
                error(i18n, "expand.multipleTags");
            }
            testStems = new HashMap<>(3);
            String [] stems = StringArray.split(value);
            for (String stem : stems) testStems.put(stem, TRUE);
        } else {

            // This part of this method is only needed to ensure that this
            // test finder behaves the same as JCKTagTestFinder.  If that
            // finder ever derives from this one, it may be removed.
            boolean valid = validEntries.get(name.toLowerCase()) != null;

            if (verify) {
                if (!valid) {
                    error(i18n, "expand.unknownEntry",
                            name, getCurrentFile());
                }
                if (name.equalsIgnoreCase("keywords")) {
                    String[] keys = StringArray.split(value);
                    if (keys != null) {
                        for (String key : keys) {
                            // minor modification here to allow no keywords
                            //if (validKeywords.get(key.toLowerCase()) == null) {
                            if ((!validKeywords.isEmpty()) && (validKeywords.get(key.toLowerCase()) == null)) {
                                error(i18n, "expand.unknownKeyword",
                                        key, getCurrentFile());
                            }
                        }
                    }
                }
            }

            if (valid)
                entries.put(name, value);
            // end of code for JCKTagTestFinder emulation

            // if emulation code is removed, still need to do a put
            // super.processEntry(entries, name, value);
        }
    } // processEntry()

    //----------member variables------------------------------------------------

    private static final String TRUE = "true";
    private Map<String, String> testStems = null;
    private Map<String, String[]> expandVars;
    private Map<String, Integer> expandVarLen;

    private String[] stdValidEntries = {
        // required
        "keywords",
        "source",
        "title",
        // optional
        "context",
        "executeArgs",
        "executeClass",
        "executeNative",
        "id",           // defined and used internally by JT Harness
        "rmicClasses",
        "timeout"
    };

    private String[] stdValidKeywords = {
        // approved
        "compiler",
        "runtime",
        "positive",
        "negative",
        "idl_inherit",
        "idl_tie",
        "interactive",
        "jniinvocationapi",
        "optionalPJava",
        // will eventually be superceded/deprecated
        "serial"
    };
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(ExpandTestFinder.class);
}
