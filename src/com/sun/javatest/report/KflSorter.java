/*
 * $Id$
 *
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.report;

import com.sun.javatest.KnownFailuresList;
import com.sun.javatest.Status;
import com.sun.javatest.TestResult;
import com.sun.javatest.TestResultTable;
import com.sun.javatest.util.StringArray;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Support class to read and process a list of tests and test cases which are
 * known to fail during execution.  The intent is to allow better post-run
 * analysis of repetitive test runs, making is easier to find out what has
 * "changed" since the list was made.  This class is loosely based on the
 * exclude list, making it easy to interchange the files and tools.
 *
 * File format:
 * Test_URL[Test_Cases] BugID_List
 * The test URL rules are defined elsewhere, but it is critical that the test
 * names do not contain spaces and nothing before the BugID_List has any
 * whitespace.  The exact format of the BugID_List must simply conform to being
 * comma separated values, no whitespace or non-printable characters.
 * @since 4.4
 */
public class KflSorter {

    /**
     *
     * @param kfl Effective known failures list to use - merge them in advance
     *    if multiple KFLs are needed.
     * @param trt Where to retrieve results from.
     * @param testcases True if test case analysis should be attempted.
     */
    KflSorter(KnownFailuresList kfl, TestResultTable trt, boolean testcases) {
        this.kfl = kfl;
        this.trt = trt;
        missing = new TreeSet();
        newFailures = new TreeSet();
        otherErrors = new TreeSet();
        fail2notrun = new TreeSet();
        fail2error = new TreeSet();
        fail2pass = new TreeSet();
        fail2fail = new TreeSet();

        tc_missing = new TreeSet();
        tc_fail2pass = new TreeSet();
        tc_fail2error = new TreeSet();
        tc_fail2notrun = new TreeSet();
        tc_newFailures = new TreeSet();

        enableTestCases = testcases;
    }

//    KFL_Sorter(KnownFailuresList kfl, TestCaseList tcl) {
//    }
    KnownFailuresList getKfl() {
        return kfl;
    }

    // NOTE - these are currently individual methods, but if each section
    //   became optional, a better API should be written to avoid massive
    //   number of set/get methods
    synchronized void setF2eEnabled(boolean state) {
        processF2e = state;
    }

    synchronized void setF2fEnabled(boolean state) {
        processF2f = state;
    }

    synchronized void setMissingEnabled(boolean state) {
        processMissing = state;
    }

    /**
     * Run the comparison of KFL vs set of results.
     * May take a long time to execute, performing the operation on a background
     * thread may be appropriate.
     * @param tests Set of results to compare to.
     * @return Number of comparison errors encountered.
     */
    synchronized int run(TestResultTable.TreeIterator iter) {
        TreeSet[] lists = new TreeSet[Status.NUM_STATES];
        int totalFound = 0;

        for (; iter.hasNext();) {
            TestResult tr = (TestResult) (iter.next());
            Status s = tr.getStatus();
            TreeSet list = lists[s == null ? Status.NOT_RUN : s.getType()];
            list.add(tr);
            totalFound++;
        }

        return run(lists);
    }

    /**
     * Run the comparison of KFL vs set of results.
     * May take a long time to execute, performing the operation on a background
     * thread may be appropriate.
     * @param tests
     * @return Number of comparison problems encountered.
     */
    synchronized int run(TreeSet[] tests) {
        Iterator<KnownFailuresList.Entry> it = kfl.getIterator(false);
        int probs = 0;
        int tcprobs = 0;

        // iterator KFL entries
        for (;it.hasNext();) {
            KnownFailuresList.Entry entry = it.next();
            String url = entry.getRelativeURL();
            TestResult tr = trt.lookup(TestResult.getWorkRelativePath(url));

            if (tr == null) {
                if (!processMissing)
                    continue;

                if (missing.add(new TestDiff(url, null, Transitions.FAIL2MISSING)))
                    probs++;

                if (enableTestCases) {
                    // add all test cases from this entry
                    tcprobs += addAllTestCases(entry, url, null, Transitions.TC_FAIL2MISSING,
                            tc_missing);
                }

                continue;
            }

            Map<String, Status> tcs = null;
            if (enableTestCases)
                tcs = getTestCases(tr);

            if (tr.getStatus().isPassed()) {
                // PASSED test on KFL
                TestDiff diff = new TestDiff(url, tr, Transitions.FAIL2PASS);
                if(fail2pass.add(diff))
                    probs++;

                if (enableTestCases) {
                    //tcprobs += addAllTestCases(entry, url, tr, Transitions.TC_FAIL2PASS,
                    //        tc_fail2pass);
                    tcprobs += addStatusTestCases(url, tr,
                            Status.PASSED,
                            Transitions.TC_FAIL2PASS, tcs,
                            tc_fail2pass);
                }
            }
            else if (tr.getStatus().isError()) {
                // ERROR test on KFL
                if (!processF2e)
                    continue;

                TestDiff diff = new TestDiff(url, tr, Transitions.FAIL2ERROR);
                if(fail2error.add(diff))
                    probs++;

                if (enableTestCases) {
                    // add all test cases from this entry
                    tcprobs += addStatusTestCases(url, tr,
                            Status.ERROR,
                            Transitions.TC_FAIL2ERROR, tcs,
                            tc_fail2error);
                }
            }
            else if (tr.getStatus().isNotRun()) {
                // NOT RUN test on KFL
                TestDiff diff = new TestDiff(url, tr, Transitions.FAIL2NOTRUN);
                if(fail2notrun.add(diff))
                    probs++;

                if (enableTestCases) {
                    // add all test cases from this entry
                    tcprobs += addStatusTestCases(url, tr,
                            Status.NOT_RUN,
                            Transitions.TC_FAIL2NOTRUN, tcs,
                            tc_fail2notrun);
                }
            }
            else if (enableTestCases && tr.getStatus().isFailed()) {
                // FAILED test on KFL

                // look for new test cases which might be passing
                // easy to do here because we are iterating the KFL
                //tcprobs += findChangedCases(tr, tcs, entry);
            }
        }   // for

        // iterate failures, are they on the KFL?
        for(Object o: tests[Status.FAILED]) {
            TestResult tr = (TestResult)o;
            KnownFailuresList.Entry[] entries = kfl.find(tr.getTestName());

            if (entries == null || entries.length == 0) {
                // not on KFL
                TestDiff diff = new TestDiff(tr.getTestName(), tr,
                            Transitions.NEWFAILURES);
                if (newFailures.add(diff))
                    probs++;
            }
            else {
                if (!processF2f)
                    continue;

                TestDiff diff = new TestDiff(tr.getTestName(), tr,
                            Transitions.FAIL2FAIL);
                // does not count as a problem, so not added to probs
                fail2fail.add(diff);
            }

            // assumes that failed test cases can only exist in failed tests
            if (enableTestCases) {
                // tcs is likely looked up twice for failed tests,
                // optimize to store & lookup
                Map<String, Status> tcs = getTestCases(tr);
                if (tcs != null && !tcs.isEmpty()) {
                    // only record test cases errors if test cases are listed
                    // in the KFL

                    KnownFailuresList.Entry[] full = kfl.find(tr.getTestName());
                    // different behavior if entire test listed vs
                    // specific test cases
                    boolean fullTestListed =  (full != null && !hasTestCases(full));
                    for (String name: tcs.keySet()) {
                        KnownFailuresList.Entry e = kfl.find(tr.getTestName(), name);
                        switch(tcs.get(name).getType()) {
                            case Status.FAILED:
                                // check that it is listed
                                if (!fullTestListed && e == null) {
                                    TestDiff td = new TestDiff(tr.getTestName(),
                                                name, tr, Transitions.TC_NEWFAILURES);
                                    if(tc_newFailures.add(td))
                                        tcprobs++;

                                    // could optimize slightly to avoid repeated
                                    // adds if multiple test cases are new fails.
                                    // ensures that a tc-only differnece is also
                                    // reported in the non-tc new failures list
                                    TestDiff diff = new TestDiff(tr.getTestName(), tr,
                                            Transitions.NEWFAILURES);
                                    if (newFailures.add(diff)) {
                                        probs++;
                                    }
                                }
                                break;
                            case Status.PASSED:
                                if((fullTestListed || e != null) &&
                                    tc_fail2pass.add(new TestDiff(tr.getTestName(),
                                            name, tr, Transitions.TC_FAIL2PASS)))
                                    tcprobs++;
                                break;
                            case Status.ERROR:

                                if((fullTestListed || e != null) &&
                                    tc_fail2error.add(new TestDiff(tr.getTestName(),
                                        name, tr, Transitions.TC_FAIL2ERROR)))
                                    tcprobs++;
                                break;
                            case Status.NOT_RUN:
                                if((fullTestListed || e != null) &&
                                    tc_fail2notrun.add(new TestDiff(tr.getTestName(),
                                        name, tr, Transitions.TC_FAIL2NOTRUN)))
                                    tcprobs++;
                                break;
                            default:
                                // oh well
                                break;
                        }   // switch
                    }   // for
                }
            }
        }   // for FAILED

                // iterate errors, are they on the KFL?
        for(Object o: tests[Status.ERROR]) {
            TestResult tr = (TestResult)o;
            KnownFailuresList.Entry[] entries = kfl.find(tr.getTestName());

            if (entries == null || entries.length == 0) {
                // not on KFL
                // test is an error, but unrelated to the items listed on the KFL
                TestDiff diff = new TestDiff(tr.getTestName(), tr,
                            Transitions.OTHER_ERRORS);
                otherErrors.add(diff);
            }

            // no test case processing for this section
        }   // for ERROR

        errorCount = probs;
        tcErrorCount = tcprobs;
        return probs;
    }

    /**
     * Process a KFL entry looking for transitions from Failed to another
     * status.
     * Not sure this method implements an algorithm we want.
     * @deprecated Method not in use.
     */
    private int findChangedCases(final TestResult tr, final Map<String, Status> tcs,
            KnownFailuresList.Entry entry) {
        String kfltcl = entry.getTestCases();

        int problems = 0;

        if (kfltcl == null) {
            if (tcs != null && !tcs.isEmpty()) {
                // entire test on KFL. check it for test cases which are not
                // failed
                for(String s: tcs.keySet()) {
                    Status stat = tcs.get(s);

                    if(stat.isError()) {
                        if (tc_fail2error.add(new TestDiff(
                            tr.getTestName(), s, tr, Transitions.TC_FAIL2ERROR)))
                            problems++;
                    }
                    else if (stat.isPassed()) {
                        if (tc_fail2pass.add(new TestDiff(
                            tr.getTestName(), s, tr, Transitions.TC_FAIL2PASS)))
                            problems++;
                    }
                    else if (stat.isNotRun()) {
                        if (tc_fail2notrun.add(new TestDiff(
                            tr.getTestName(), s, tr, Transitions.TC_FAIL2NOTRUN)))
                            problems++;
                    }
                }   // for tcs
            }
        }
        else {
            // look for test cases listed on KFL, report mismatches (non failed ones)
            String[] kfltcs = StringArray.splitList(kfltcl, ",");
            for(String s: kfltcs) {
                if (tcs != null && !tcs.isEmpty()) {
                    return addAllTestCases(entry, entry.getRelativeURL(), tr,
                            Transitions.TC_FAIL2MISSING, tc_missing);
                }

                Status stat = tcs.get(s);

                if (stat == null) {
                    if (tc_missing.add(new TestDiff(
                        tr.getTestName(), s, tr, Transitions.TC_FAIL2MISSING)))
                        problems++;
                }
                else if(stat.isError()) {
                    if (tc_fail2error.add(new TestDiff(
                        tr.getTestName(), s, tr, Transitions.TC_FAIL2ERROR)))
                        problems++;
                }
                else if (stat.isPassed()) {
                    if (tc_fail2pass.add(new TestDiff(
                        tr.getTestName(), s, tr, Transitions.TC_FAIL2PASS)))
                        problems++;
                }
                else if (stat.isNotRun()) {
                    if (tc_fail2notrun.add(new TestDiff(
                        tr.getTestName(), s, tr, Transitions.TC_FAIL2NOTRUN)))
                        problems++;
                }
            }   // for
        }

        return problems;
    }

    /**
     * Add all test cases from the KFL entry to the set.
     */
    private int addAllTestCases(final KnownFailuresList.Entry entry,
            final String url, final TestResult tr,
            Transitions t, TreeSet set) {
        // could check enableTestCases flag before processing
        // add all test cases from this entry
        int problems = 0;

        String[] tcs = entry.getTestCaseList();
        if (tcs == null || tcs.length == 0)
            return 0;

        for (String s: tcs) {
            if (set.add(new TestDiff(url, s, tr, t)))
                problems++;
        }

        return problems;
    }

    private int addListedTestCases(final KnownFailuresList.Entry entry,
            final String url, final TestResult tr,
            Transitions t, TreeSet set) {
        int problems = 0;

        String[] tcs = entry.getTestCaseList();
        if (tcs == null || tcs.length == 0)
            return 0;

        for (String s: tcs) {

        }

        return problems;
    }

    /**
     * Add all test cases for a test which match the given status to the tree set.
     * @param entry The corresponding KFL entry.
     * @param
     * @param set the data set to add the selected test cases to
     * @return the number of test cases which matched the status
     */
//    private int addStatusTestCases(final KnownFailuresList.Entry entry,
//            final String url, final TestResult tr, int status,
//            Transitions t, Map<String, Status> tcs, TreeSet set) {
//        // could check enableTestCases flag before processing
//
//        // add all test cases from this entry
//        int problems = 0;
//
//        //String[] tcs = entry.getTestCaseList();
//        if (tcs == null || tcs.isEmpty())
//            return 0;
//
//        for (String key: tcs.keySet()) {
//            Status stat = tcs.get(key);
//
//            if (stat.getType() == status) {
//                if(set.add(new TestDiff(url, key, tr, t)))
//                    problems++;
//            }
//        }
//
//        return problems;
//    }


    /**
     * Add all test cases from the test result, listed in the KFL entry
     * which match the given status.
     * Effectively - iterate test cases in result (tcs), if it is listed in the
     * KFL and matches the given status, add to the given set.
     * @param entry The KFL entry.
     * @param tcs Test case results for the given test.
     * @param status the status type to match
     * @param set the data set to add the selected test cases to
     * @return the number of test cases which were added to the set
     * @see com.sun.javatest.Status
     */
    private int addStatusTestCases(
            final String url, final TestResult tr, int status,
            Transitions t, Map<String, Status> trtcs, TreeSet set) {
        // could check enableTestCases flag before processing
        int problems = 0;

        if (trtcs == null || trtcs.isEmpty()) {
            return 0;
        }
        else {
            for (String key: trtcs.keySet()) {
                Status stat = trtcs.get(key);
                KnownFailuresList.Entry e = kfl.find(url, key);
                KnownFailuresList.Entry[] full = kfl.find(tr.getTestName());
                // different behavior if entire test listed vs
                // specific test cases
                boolean fullTestListed = (full != null && !hasTestCases(full));

                // test case not listed in KFL
                // no need to list a passing test case which wasn't
                // listed as a failure in the KFL
                // could be a new test case, etc.
                if (!fullTestListed && e == null)
                    continue;

                if (stat.getType() == status) {
                    TestDiff diff = new TestDiff(url, key, tr, t);
                    if (fullTestListed && full != null && full.length > 0)
                        diff.setKflEntry(full[0]);

                    if(set.add(diff))
                        problems++;
                }
            }   // for
        }

        return problems;
    }


    private boolean hasTestCases(final KnownFailuresList.Entry[] es) {
        if (es == null || es.length == 0)
            return false;

        for(KnownFailuresList.Entry e: es) {
            if(e.getTestCases() != null)
                return true;
        }

        return false;
    }

    synchronized TreeSet<TestDiff> getSet(Transitions id) {
        switch (id) {
            case FAIL2PASS: return fail2pass;
            case FAIL2MISSING: return missing;
            case NEWFAILURES: return newFailures;
            case OTHER_ERRORS: return otherErrors;
            case FAIL2NOTRUN: return fail2notrun;
            case FAIL2ERROR: return fail2error;
            case FAIL2FAIL: return fail2fail;

            case TC_FAIL2PASS: return tc_fail2pass;
            case TC_FAIL2MISSING: return tc_missing;
            case TC_NEWFAILURES: return tc_newFailures;
            case TC_FAIL2NOTRUN: return tc_fail2notrun;
            case TC_FAIL2ERROR: return tc_fail2error;
            default: return null;
        } // switch
    }

    synchronized int getErrorCount() {
        return errorCount;
    }

    synchronized int getTestCasesErrorCount() {
        return tcErrorCount;
    }


    private static Map<String, Status> getTestCases(final TestResult tr) {
        Map result = new LinkedHashMap();

        if (tr.isShrunk() && tr.isReloadable())
            tr.getSectionTitles();

        int sCount = tr.getSectionCount();
        if (sCount == 0 && tr.getStatus().getType() != Status.NOT_RUN) {
            //sCount = tr.getSectionCount();
        }

        for (int i = 0; i < sCount; i++) {
            try {
                String sectionOut = tr.getSection(i).getOutput("out1");
                if (sectionOut == null) {
                    continue;
                }
                BufferedReader reader = new BufferedReader(new StringReader(
                        sectionOut));
                String s = reader.readLine();
                while (s != null) {
                    Matcher m = testCasePattern.matcher(s);
                    if (m.matches()) {
                        String tcName = m.group(1);
                        // checking for space helps eliminate false matches
                        if (tcName == null || tcName.contains(" ") ||
                            tcName.contains("\t")) {
                            s = reader.readLine();
                            continue;
                        }
                        Status stat = Status.parse(m.group(2));
                        result.put(tcName, stat);
                    }
                    s = reader.readLine();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
            } catch (TestResult.ReloadFault e) {
                // TODO Auto-generated catch block
            }
        }

        return (result.isEmpty() ? null : result);
    }

    /**
     * Created for each result which somehow does not match what was expected
     * based on the KFL. Using this class allows the analysis to be done once
     * then queried again and again for different purposes.
     */
    public static class TestDiff implements Comparable {
        public TestDiff(String url, TestResult tr, Transitions type) {
            this.tr = tr;
            this.url = url;
        }

        public TestDiff(String url, String tc, TestResult tr, Transitions type) {
            this(url, tr, type);
            this.tc = tc;
            // resultMismatch =
            // caseMismatch =
        }

        public TestResult getTestResult() {
            return tr;
        }

        /**
         * Is the mismatch concerning the test's main result?
         * @return True if the result status is not the same as the result expected
         *    based on the KFL.  False if the main result matches.
         */
        public boolean isTestMismatch() {
            return resultMismatch;
        }


        /**
         * Get the full name for this entry, including the test case.
         * Most useful for display purposes.
         * @return An easily human readable string.
         * @see #getTestName()
         * @see #getTestCase()
         */
        public String getName() {
            String u = (url != null ? url : tr.getTestName());
            if (tc == null) {
                return u;
            }
            else {
                return u + "[" + tc + "]";
            }
        }

        /**
         * Get the name of the test involved in this diff, not including the
         * test case portion if that applies.
         * @see #getName()
         */
        public String getTestName() {
            return url;
        }

        /**
         * Get the list of the test case(s).
         * @return Null if there are no test cases associated, otherwise a
         *    comma separated string of test case names.
         */
        public String getTestCase() {
            return tc;
        }

        /**
         * Not normally used, but can be used as a secondary way to get
         * the associated KFL entry.  Typically this value will be null.
         * @return A KFL entry which caused this diff.
         */
        public KnownFailuresList.Entry getKflEntry() {
            return kflEntry;
        }

        /**
         * Not normally used, but can be used as a backup if there is a
         * special case where looking up the entry later would fail.
         * @param e The KFL entry to associate with this diff.
         */
        public void setKflEntry(KnownFailuresList.Entry e) {
            kflEntry = e;
        }

        @Override
        public int compareTo(Object o) {
            TestDiff e = (TestDiff) o;
            int n = getName().compareTo(e.getName());
/*          if (n == 0) {
                if (testCase == null && e.testCase == null)
                    return 0;
                else if (testCase == null)
                    return -1;
                else if (e.testCase == null)
                    return +1;
                else
                    return testCase.compareTo(e.testCase);
            }
            else
                return n;*/
            return n;
        }

        private TestResult tr;
        private String url;
        private String tc;
        private boolean resultMismatch = false;
        private boolean caseMismatch = false;
        private KnownFailuresList.Entry kflEntry;
    }

    public enum Transitions { FAIL2PASS, FAIL2ERROR, FAIL2MISSING, FAIL2NOTRUN, FAIL2FAIL, NEWFAILURES,
        OTHER_ERRORS, TC_FAIL2MISSING, TC_FAIL2PASS, TC_PASS2ERROR, TC_FAIL2NOTRUN, TC_FAIL2ERROR, TC_NEWFAILURES }
//    interface KflObserver {
//        public void passToFail(TestResult tr);
//        public void failToPass(TestResult tr);
//    }
    protected TreeSet<TestDiff> fail2pass;
    protected TreeSet<TestDiff> fail2error;
    protected TreeSet<TestDiff> fail2notrun;
    protected TreeSet<TestDiff> missing;
    protected TreeSet<TestDiff> newFailures;
    protected TreeSet<TestDiff> otherErrors;
    protected TreeSet<TestDiff> fail2fail;

    protected TreeSet<TestDiff> tc_missing;
    protected TreeSet<TestDiff> tc_fail2pass;
    protected TreeSet<TestDiff> tc_fail2error;
    protected TreeSet<TestDiff> tc_fail2notrun;
    protected TreeSet<TestDiff> tc_newFailures;

    protected KnownFailuresList kfl;
    //protected TestCaseList tcl;
    protected TestResultTable trt;
    protected int errorCount;
    protected int tcErrorCount;
    private boolean enableTestCases;
    private boolean processF2f, processF2e, processMissing = true;

    protected static final Pattern testCasePattern = Pattern //.compile("^(\\S+): (Passed\\.|Failed\\.|Error\\.|Not\\ run\\.)(.*)");
            .compile("^(.*): (Passed\\.|Failed\\.|Error\\.|Not\\ run\\.)(.*)");
}
