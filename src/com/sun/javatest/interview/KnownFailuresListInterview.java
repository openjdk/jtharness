/*
 * $Id$
 *
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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
import com.sun.interview.ErrorQuestion;
import com.sun.interview.ExtensionFileFilter;
import com.sun.interview.FileFilter;
import com.sun.interview.FileListQuestion;
import com.sun.interview.FinalQuestion;
import com.sun.interview.Interview;
import com.sun.interview.Question;
import com.sun.interview.YesNoQuestion;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.KnownFailuresList;
import com.sun.javatest.TestSuite;

/**
 * This interview collects the "kfl" test filter parameters.
 * It is normally used as one of a series of sub-interviews that collect
 * the parameter information for a test run.
 */
public class KnownFailuresListInterview
    extends Interview
{
    /**
     * Create an interview.
     * @param parent The parent interview of which this is a child.
     * @throws Interview.Fault if there is a problem while creating the interview.
     */
    public KnownFailuresListInterview(InterviewParameters parent)
        throws Interview.Fault
    {
        super(parent, "knownFailuresList");
        this.parent = parent;
        setResourceBundle("i18n");
        setHelpSet("/com/sun/javatest/moreInfo/moreInfo.hs");

        // had to wait to init parent before doing the following:
        qNeedKfl = new NeedKflQuestion();

        setFirstQuestion(qNeedKfl);
    }

    public void dispose() {
        cachedKfl = null;
        cachedKflError = null;
        cachedExcludeListErrorArgs = null;
        cachedExcludeList_testSuite = null;
        cachedExcludeList_files = null;
    }

    public void setKflFiles(File[] files) {
        if (files == null || files.length == 0) {
            qNeedKfl.setValue(YesNoQuestion.NO);
            setCustomKflFiles(null);
        }
        else {
            qNeedKfl.setValue(YesNoQuestion.YES);
            setCustomKflFiles(files);
        }

        updatePath(qNeedKfl);
        setEdited(true);
        updateCachedExcludeListData();
    }

    public File[] getKflFiles() {
        return (qNeedKfl.getValue().equals(YesNoQuestion.NO) ?
            null : qCustomFiles.getValue());
    }

    protected void setCustomKflFiles(File[] files) {
        qCustomFiles.setValue(files);
        updatePath(qCustomFiles);
    }

    /*
     * Get the exclude list generated from the exclude list files in the interview.
     * @return the exclude list generated from the exclude list files in the interview
     * @see #getExcludeFiles
     */
    public KnownFailuresList getKfl() {
        updateCachedExcludeListData();
        return cachedKfl;
    }

    /**
     * According to the interview, does the user want to use a KFL?
     * @return True if the user has indicated that they want to use a KFL, false
     *    otherwise.
     */
    public boolean isKflEnabled() {
        return qNeedKfl.getValue().equals(YesNoQuestion.YES);
    }

    //--------------------------------------------------------

    private InterviewParameters parent;
    private boolean initializedForTestSuite;

    //----------------------------------------------------------------------------
    //
    // Need exclude list

    private class NeedKflQuestion extends YesNoQuestion {
        NeedKflQuestion() {
            super(KnownFailuresListInterview.this, "needKfl");
            setDefaultValue(NO);
            setValue(NO);
            clear();
            doneSuper = true;
        }

        @Override
        public void clear() {
            // clear will be called from the constructor once the choices have been set,
            // but we can't call out to the enclosing class before super() completes (NPE)
            if (!doneSuper)
                return;

            super.clear();
        }

        protected Question getNext() {
            if (value == null)
                return null;
            else if (value == YES)
                return qCustomFiles;
            else
                return qEnd;
        }

        private boolean doneSuper;
    };

    private NeedKflQuestion qNeedKfl; // defer initialization

    //----------------------------------------------------------------------------
    //
    // Exclude List

    private FileListQuestion qCustomFiles = new FileListQuestion(this, "customFiles") {
        {
            setResourceBundle("i18n");
            FileFilter[] filters = {
                new ExtensionFileFilter(".jtx",
                    getResourceString("KnownFailuresListInterview.jtx.extn.desc", false)),
                new ExtensionFileFilter(".txt",
                    getResourceString("KnownFailuresListInterview.txt.extn.desc", false)),
                new ExtensionFileFilter(".kfl",
                    getResourceString("KnownFailuresListInterview.kfl.extn.desc", false))
            };
            setFilters(filters);
            setDuplicatesAllowed(false);
        }

        protected Question getNext() {
            if (value == null || value.length == 0)
                return null;

            return checkExcludeList();
        }

        @Override
        public File getBaseDirectory() {
            TestSuite ts = parent.getTestSuite();
            return (ts == null ? null : ts.getRootDir());
        }
    };

    //----------------------------------------------------------------------------

    private void updateCachedExcludeListData() {
        TestSuite ts = parent.getTestSuite();
        File tsRootDir = (ts == null ? null : ts.getRootDir());
        File[] files = getAbsoluteFiles(tsRootDir, getKflFiles());
        if (!equal(cachedExcludeList_files, files) || cachedExcludeList_testSuite != ts) {
            try {
                if (ts == null || files == null || files.length == 0)
                    setCachedKfl(new KnownFailuresList());
                else
                    setCachedKfl(new KnownFailuresList(files));
            }
            catch (FileNotFoundException e) {
                setCachedKflError(qKflFileNotFound, e.getMessage());
            }
            catch (IOException e) {
                setCachedKflError(qKflIOError, e.toString());
            }
            catch (KnownFailuresList.Fault e) {
                setCachedKflError(qKflError, e.getMessage());
            }

            cachedExcludeList_files = files;
            cachedExcludeList_testSuite = ts;
        }
    }

    private void setCachedKfl(KnownFailuresList l) {
        cachedKfl = l;
        cachedKflError = null;
        cachedExcludeListErrorArgs = null;
    }

    private void setCachedKflError(Question q, String arg) {
        cachedKfl = new KnownFailuresList();
        //cachedExcludeListFilter = null;
        cachedKflError = q;
        cachedExcludeListErrorArgs = new String[] { arg };
    }


    private KnownFailuresList cachedKfl;
    private Question cachedKflError;
    private Object[] cachedExcludeListErrorArgs;
    private TestSuite cachedExcludeList_testSuite;
    private File[] cachedExcludeList_files;


    //----------------------------------------------------------------------------
    //
    // KFL Error

    private ErrorQuestion qKflFileNotFound = new ErrorQuestion(this, "KflFileNotFound") {
        @Override
        protected Object[] getTextArgs() {
            return cachedExcludeListErrorArgs;
        }
    };

    private ErrorQuestion qKflIOError = new ErrorQuestion(this, "KflIOError") {
        @Override
        protected Object[] getTextArgs() {
            return cachedExcludeListErrorArgs;
        }
    };

    private ErrorQuestion qKflError = new ErrorQuestion(this, "KflError") {
        @Override
        protected Object[] getTextArgs() {
            return cachedExcludeListErrorArgs;
        }
    };

    //----------------------------------------------------------------------------
    //
    // End

    private Question checkExcludeList() {
        updateCachedExcludeListData();
        if (cachedKflError != null)
            return cachedKflError;
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

