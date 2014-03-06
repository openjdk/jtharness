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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Vector;
import com.sun.interview.ChoiceQuestion;
import com.sun.interview.ErrorQuestion;
import com.sun.interview.ExtensionFileFilter;
import com.sun.interview.FileListQuestion;
import com.sun.interview.FinalQuestion;
import com.sun.interview.Interview;
import com.sun.interview.IntQuestion;
import com.sun.interview.Question;
import com.sun.interview.YesNoQuestion;
import com.sun.javatest.ExcludeList;
import com.sun.javatest.ExcludeListFilter;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.Parameters;
import com.sun.javatest.TestFilter;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;

/**
 * This interview collects the "exclude list" test filter parameters.
 * It is normally used as one of a series of sub-interviews that collect
 * the parameter information for a test run.
 */
public class ExcludeListInterview
    extends Interview
    implements Parameters.MutableExcludeListParameters
{
    /**
     * Create an interview.
     * @param parent The parent interview of which this is a child.
     * @throws Interview.Fault if there is a problem while creating the interview.
     */
    public ExcludeListInterview(InterviewParameters parent)
        throws Interview.Fault
    {
        super(parent, "excludeList");
        this.parent = parent;
        setResourceBundle("i18n");
        setHelpSet("/com/sun/javatest/moreInfo/moreInfo.hs");

        // had to wait to init parent before doing the following:
        qNeedExcludeLists = new NeedExcludeListsQuestion();
        qExcludeListType = new ExcludeListTypeQuestion();

        setFirstQuestion(qNeedExcludeLists);

    }

    public void dispose() {
        cachedExcludeList = null;
        cachedExcludeListFilter = null;
        cachedExcludeListError = null;
        cachedExcludeListErrorArgs = null;
        cachedExcludeList_testSuite = null;
        cachedExcludeList_files = null;
    }

    public File[] getExcludeFiles() {
        if (qNeedExcludeLists.getValue() == YesNoQuestion.YES) {
            TestSuite ts = parent.getTestSuite();
            String t = qExcludeListType.getValue();
            if (t == INITIAL) {
                File f = (ts == null ? null : ts.getInitialExcludeList());
                if (f == null)
                    return null;
                else
                    return new File[] { f };
            }
            else if (t == LATEST) {
                URL u = (ts == null ? null : ts.getLatestExcludeList());
                if (u == null)
                    return null;
                else {
                    WorkDirectory wd = parent.getWorkDirectory();
                    return new File[] { wd.getSystemFile("latest.jtx") };
                }
            }
            else
                return qCustomFiles.getValue();
        }
        else
            return null;
    }

    public void setExcludeFiles(File[] files) {
        if (files == null || files.length == 0)
            setExcludeMode(NO_EXCLUDE_LIST);
        else {
            setExcludeMode(CUSTOM_EXCLUDE_LIST);
            setCustomExcludeFiles(files);
        }
    }

    public int getExcludeMode() {
        if (qNeedExcludeLists.getValue() == YesNoQuestion.YES) {
            String t = qExcludeListType.getValue();
            if (t == INITIAL)
                return INITIAL_EXCLUDE_LIST;
            else if (t == LATEST)
                return LATEST_EXCLUDE_LIST;
            else
                return CUSTOM_EXCLUDE_LIST;
        }
        else
            return NO_EXCLUDE_LIST;
    }

    public void setExcludeMode(int mode) {
        if (mode == NO_EXCLUDE_LIST)
            qNeedExcludeLists.setValue(YesNoQuestion.NO);
        else {
            qNeedExcludeLists.setValue(YesNoQuestion.YES);
            switch (mode) {
            case INITIAL_EXCLUDE_LIST:
                qExcludeListType.setValue(INITIAL);
                break;
            case LATEST_EXCLUDE_LIST:
                qExcludeListType.setValue(LATEST);
                break;
            default:
                qExcludeListType.setValue(CUSTOM);
                break;
            }
        }
    }

    public File[] getCustomExcludeFiles() {
        return qCustomFiles.getValue();
    }

    public void setCustomExcludeFiles(File[] files) {
        qCustomFiles.setValue(files);
    }

    public boolean isLatestExcludeAutoCheckEnabled() {
        return (qLatestAutoCheck.getValue() == YesNoQuestion.YES);
    }

    public void setLatestExcludeAutoCheckEnabled(boolean b) {
        qLatestAutoCheck.setValue(b ? YesNoQuestion.YES : YesNoQuestion.NO);
    }

    public int getLatestExcludeAutoCheckMode() {
        return (qLatestAutoCheckMode.getValue() == EVERY_X_DAYS
                ? CHECK_EVERY_X_DAYS : CHECK_EVERY_RUN);
    }

    public void setLatestExcludeAutoCheckMode(int mode) {
        if (mode == CHECK_EVERY_X_DAYS)
            qLatestAutoCheckMode.setValue(EVERY_X_DAYS);
        else
            qLatestAutoCheckMode.setValue(EVERY_RUN);
    }

    public int getLatestExcludeAutoCheckInterval() {
        return qLatestAutoCheckInterval.getValue();
    }

    public void setLatestExcludeAutoCheckInterval(int days) {
        qLatestAutoCheckInterval.setValue(days);
    }

    /**
     * Get the exclude list generated from the exclude list files in the interview.
     * @return the exclude list generated from the exclude list files in the interview
     * @see #getExcludeFiles
     */
    public ExcludeList getExcludeList() {
        updateCachedExcludeListData();
        return cachedExcludeList;
    }


    /**
     * Get a test filter generated from the exclude list files in the interview.
     * @return a test filter generated from the exclude list files in the interview
     *     or null, if no filter is required
     * @see #getExcludeFiles
     */
    public TestFilter getExcludeFilter() {
        updateCachedExcludeListData();
        return cachedExcludeListFilter;
    }

    private void ensureInitializedForTestSuite() {
        if (initializedForTestSuite)
            return;

        TestSuite ts = parent.getTestSuite();
        if (ts == null)
            return;

        File ijtx = ts.getInitialExcludeList();
        hasInitialJTX = (ijtx != null);
        hasValidInitialJTX = (hasInitialJTX && ijtx.exists());

        //System.err.println("ELI initialJTX=" + ijtx);
        //System.err.println("ELI hasInitialJTX=" + hasInitialJTX);
        //System.err.println("ELI hasValidInitialJTX=" + hasInitialJTX);

        URL ljtx = ts.getLatestExcludeList();
        hasLatestJTX = (ljtx != null);
        if (hasLatestJTX) {
            WorkDirectory wd = parent.getWorkDirectory();
            hasValidLatestJTX = (wd != null && wd.getSystemFile("latest.jtx").exists());
        }
        else
            hasValidLatestJTX = false;

        //System.err.println("ELI latestJTX=" + ljtx);
        //System.err.println("ELI hasLatestJTX=" + hasLatestJTX);
        //System.err.println("ELI hasValidInitialJTX=" + haveInitialJTX);

        initializedForTestSuite = true;
    }

    //--------------------------------------------------------

    private InterviewParameters parent;
    private boolean initializedForTestSuite;
    private boolean hasInitialJTX;      // defined in testsuite.jtt
    private boolean hasValidInitialJTX; // file exists as specified
    private boolean hasLatestJTX;       // defined in testsuite.jtt
    private boolean hasValidLatestJTX;  // have downloaded copy in wd/jtData/latest.jtx

    //----------------------------------------------------------------------------
    //
    // Need exclude list

    private class NeedExcludeListsQuestion extends YesNoQuestion {
        NeedExcludeListsQuestion() {
            super(ExcludeListInterview.this, "needExcludeList");
            doneSuper = true;
            clear();
        }

        public void clear() {
            // clear will be called from the constructor once the choices have been set,
            // but we can't call out to the enclosing class before super() completes (NPE)
            if (!doneSuper)
                return;

            ensureInitializedForTestSuite();

            // default answer to yes if there is a reasonable valid exclude list already available
            setValue(hasValidInitialJTX || hasValidLatestJTX ? YesNoQuestion.YES : YesNoQuestion.NO);
            //System.err.println("ELI.needExcludeLists.clear value=" + value);
        }

        protected Question getNext() {
            if (value == null)
                return null;
            else if (value == YES)
                return qExcludeListType;
            else
                return qEnd;
        }

        private boolean doneSuper;
    };

    private NeedExcludeListsQuestion qNeedExcludeLists; // defer initialization

    //----------------------------------------------------------------------------
    //
    // Type of exclude list

    private static final String INITIAL = "initial";
    private static final String LATEST = "latest";
    private static final String CUSTOM = "custom";

    private class ExcludeListTypeQuestion extends ChoiceQuestion {
        ExcludeListTypeQuestion() {
            super(ExcludeListInterview.this, "excludeListType");

            // Difficulty here is that these depend on the test suite, which
            // will not have been set yet. So, set these full set of choices
            // for now, and refine the choices when the test suite gets set.
            setChoices(new String[] { null, INITIAL, LATEST, CUSTOM }, true);
            clear();
        }

        public void clear() {
            ensureInitialized();
            setValue(defaultValue);
            //System.err.println("ELI.excludeListType.clear value=" + value);
        }

        public String[] getChoices() {
            ensureInitialized();
            return super.getChoices();
        }

        public void save(Map data) {
            ensureInitialized();
            super.save(data);
        }


        public boolean isHidden() {
            // the following will implicitly call ensureInitialized()
            return (getChoices().length == 2); // null and CUSTOM
        }

        public String getValue() {
            // the following will implicitly call ensureInitialized()
            return (isHidden() ? CUSTOM : super.getValue());
        }

        protected Question getNext() {
            // the following will implicitly call ensureInitialized()
            if (isHidden())
                return qCustomFiles;
            else if (value == null || value.length() == 0)
                return null;
            else if (value.equals(INITIAL))
                return checkExcludeList();
            else if (value.equals(LATEST))
                return qLatestAutoCheck;
            else
                return qCustomFiles;
        }

        private void ensureInitialized() {
            if (initialized)
                return;

            if (parent.getTestSuite() == null)
                return;

            ensureInitializedForTestSuite();

            String defaultValue = CUSTOM;
            Vector v = new Vector(4);

            v.add(null); // always

            if (hasValidInitialJTX) {
                // require it to be valid, cos no way for user to make it become valid
                //if it isn't already
                v.add(INITIAL);
            }

            if (hasLatestJTX) {
                // don't require it to be valid, because they can download it if needed
                v.add(LATEST);
            }

            v.add(CUSTOM); // always

            String[] choices = new String[v.size()];
            v.copyInto(choices);
            initialized = true;

            setChoices(choices, true);

            // set the default to the best valid exclude list that is already available
            defaultValue = (hasValidLatestJTX ? LATEST : hasValidInitialJTX ? INITIAL : null);
            setValue(defaultValue);

        }

        private boolean initialized;
        private String defaultValue;
    };

    private ExcludeListTypeQuestion qExcludeListType; // defer initialization

    //----------------------------------------------------------------------------
    //
    // Auto check latest

    private YesNoQuestion qLatestAutoCheck = new YesNoQuestion(this, "latestAutoCheck", YesNoQuestion.NO) {
        protected Question getNext() {
            if (value == null)
                return null;
            else if (value == YES)
                return qLatestAutoCheckMode;
            else
                return checkExcludeList();
        }
    };

    //----------------------------------------------------------------------------
    //
    // Auto check latest mode

    private static final String EVERY_X_DAYS = "everyXDays";
    private static final String EVERY_RUN = "everyRun";

    private ChoiceQuestion qLatestAutoCheckMode = new ChoiceQuestion(this, "latestAutoCheckMode") {
        {
            setChoices(new String[] { EVERY_X_DAYS, EVERY_RUN }, true);
        }

        protected Question getNext() {
            if (value == null)
                return null;
            else if (value.equals(EVERY_X_DAYS))
                return qLatestAutoCheckInterval;
            else
                return checkExcludeList();
        }
    };

    //----------------------------------------------------------------------------
    //
    // Auto check latest interval

    private IntQuestion qLatestAutoCheckInterval = new IntQuestion(this, "latestAutoCheckInterval") {
        {
            setBounds(1, 365);
        }

        public void clear() {
            setValue(7);
        }

        protected Question getNext() {
            return checkExcludeList();
        }
    };



    //----------------------------------------------------------------------------
    //
    // Exclude List

    private FileListQuestion qCustomFiles = new FileListQuestion(this, "customFiles") {
        {
            setResourceBundle("i18n");
            setFilter(new ExtensionFileFilter(".jtx",
                    getResourceString("ExcludeListInterview.extn.desc", false)));
            setDuplicatesAllowed(false);
        }

        protected Question getNext() {
            if (value == null || value.length == 0)
                return null;

            return checkExcludeList();
        }

        public File getBaseDirectory() {
            TestSuite ts = parent.getTestSuite();
            return (ts == null ? null : ts.getRootDir());
        }
    };

    //----------------------------------------------------------------------------

    private void updateCachedExcludeListData() {
        TestSuite ts = parent.getTestSuite();
        File tsRootDir = (ts == null ? null : ts.getRootDir());
        File[] files = getAbsoluteFiles(tsRootDir, getExcludeFiles());
        if (!equal(cachedExcludeList_files, files) || cachedExcludeList_testSuite != ts) {
            try {
                if (ts == null || files == null || files.length == 0)
                    setCachedExcludeList(new ExcludeList());
                else
                    setCachedExcludeList(new ExcludeList(files));
            }
            catch (FileNotFoundException e) {
                setCachedExcludeListError(qExcludeListFileNotFound, e.getMessage());
            }
            catch (IOException e) {
                setCachedExcludeListError(qExcludeListIOError, e.toString());
            }
            catch (ExcludeList.Fault e) {
                setCachedExcludeListError(qExcludeListError, e.getMessage());
            }

            cachedExcludeList_files = files;
            cachedExcludeList_testSuite = ts;
        }
    }

    private void setCachedExcludeList(ExcludeList l) {
        cachedExcludeList = l;
        cachedExcludeListFilter = (l.isEmpty() ? null : new ExcludeListFilter(l));
        cachedExcludeListError = null;
        cachedExcludeListErrorArgs = null;
    }

    private void setCachedExcludeListError(Question q, String arg) {
        cachedExcludeList = new ExcludeList();
        cachedExcludeListFilter = null;
        cachedExcludeListError = q;
        cachedExcludeListErrorArgs = new String[] { arg };
    }


    private ExcludeList cachedExcludeList;
    private ExcludeListFilter cachedExcludeListFilter;
    private Question cachedExcludeListError;
    private Object[] cachedExcludeListErrorArgs;
    private TestSuite cachedExcludeList_testSuite;
    private File[] cachedExcludeList_files;


    //----------------------------------------------------------------------------
    //
    // Exclude List Error

    private ErrorQuestion qExcludeListFileNotFound = new ErrorQuestion(this, "excludeListFileNotFound") {
        protected Object[] getTextArgs() {
            return cachedExcludeListErrorArgs;
        }
    };

    private ErrorQuestion qExcludeListIOError = new ErrorQuestion(this, "excludeListIOError") {
        protected Object[] getTextArgs() {
            return cachedExcludeListErrorArgs;
        }
    };

    private ErrorQuestion qExcludeListError = new ErrorQuestion(this, "excludeListError") {
        protected Object[] getTextArgs() {
            return cachedExcludeListErrorArgs;
        }
    };

    //----------------------------------------------------------------------------
    //
    // End

    private Question checkExcludeList() {
        updateCachedExcludeListData();
        if (cachedExcludeListError != null)
            return cachedExcludeListError;
        else
            return qEnd;
    }

    private Question qEnd = new FinalQuestion(this);

    //---------------------------------------------------------------------

    private static File[] getAbsoluteFiles(File baseDir, File[] files) {
        if (files == null)
            return null;

        if (baseDir == null)
            return files;

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

    //----------------------------------------------------------------------------

    private static boolean equal(File f1, File f2) {
        return (f1 == null ? f2 == null : f1.equals(f2));
    }

    private static boolean equal(File[] f1, File[] f2) {
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
}

