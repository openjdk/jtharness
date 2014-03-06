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
package com.sun.javatest.tool;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JFileChooser;

import com.sun.javatest.TemplateUtilities;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.exec.WorkDirChooseTool;
import java.io.IOException;

/**
 * A custom JFileChooser, for a user to choose a work directory.
 */
public class WorkDirChooser extends JFileChooser
{

    /**
     * Create a WorkDirChooser, initially showing the user's current directory.
     */
    public WorkDirChooser(boolean usePrefs) {
        this(getDefaultDirFromPrefs(usePrefs));
        size = getPreferredSize();
    }

    private static File getDefaultDirFromPrefs(boolean usePrefs) {
        if (!usePrefs) {
            return new File(System.getProperty("user.dir"));
        }
        else {
            Preferences prefs = Preferences.access();
            String defaultDir = prefs.getPreference(WorkDirChooseTool.DEFAULT_WD_PREF_NAME);
            if (defaultDir != null) {
                return new File(defaultDir);
            }
            else {
                return new File(System.getProperty("user.dir"));
            }
        }
    }
    /**
     * Create a WorkDirChooser, initially showing a given directory.
     * @param initialDir the initial directory to be shown
     */
    public WorkDirChooser(File initialDir) {
        super(normalize(initialDir));
        setName("wdc");

        uif = new UIFactory(this, null);  // no help broker required

        // we want a filter that only selects directories
        setAcceptAllFileFilterUsed(false);

        // we need FILES in the mode so that non-traversable
        // items appear in the list
        // -- see BasicDirectoryModel.LoadFilesThread
        // we need DIRECTORIES in the mode so that directories
        // (eg work directories) can be selected (ie chosen)
        // -- see BasicFileChooserUI.ApproveSelectionAction
        setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        // these parameters are still not ideal ...
        // approveSelection(File) below gets called for all directories:
        // we still have to redispatch according to whether it is a work
        // directory or not.

        swda = new SelectedWorkDirApprover(mode, this);

        // we want a file view that displays special icons for
        // work directories and sets work directories non-traversable,
        // so that double clicking in the list view will select
        // (ie choose) them
        setFileView(new WDC_FileView(swda));
        setAcceptAllFileFilterUsed(true);
        setFileFilter(new WDC_FileFilter(uif.getI18NString("wdc.ft")));
    }

    /**
     * A constant to indicate that a new work directory is to be created.
     * @see #setMode
     */
    public static final int NEW = 0;

    /**
     * A constant to indicate that an existing work directory is to be opened.
     * @see #setMode
     */
    public static final int OPEN_FOR_ANY_TESTSUITE = 1;

    /**
     * A constant to indicate that an existing work directory that is to be opened
     * in conjunction with a specific test suite.
     * @see #setMode
     */
    public static final int OPEN_FOR_GIVEN_TESTSUITE = 2;

    /**
     * Set whether the chooser is to be used to create a new work directory
     * or to open an existing work directory.
     * @param mode a constant to indicate how the chooser is to operate
     * @see #NEW
     * @see #OPEN_FOR_ANY_TESTSUITE
     * @see #OPEN_FOR_GIVEN_TESTSUITE
     */
    public void setMode(int mode) {
        this.mode = mode;
        switch (mode) {
        case NEW:
            setApproveButtonText(uif.getI18NString("wdc.new.btn"));
            setApproveButtonMnemonic(uif.getI18NMnemonic("wdc.new.mne"));
            setApproveButtonToolTipText(uif.getI18NString("wdc.new.tip"));
            setDialogTitle(uif.getI18NString("wdc.new.title"));
            break;
        case OPEN_FOR_ANY_TESTSUITE:
        case OPEN_FOR_GIVEN_TESTSUITE:
            setApproveButtonMnemonic(uif.getI18NMnemonic("wdc.open.mne"));
            setApproveButtonText(uif.getI18NString("wdc.open.btn"));
            setApproveButtonToolTipText(uif.getI18NString("wdc.open.tip"));
            setDialogTitle(uif.getI18NString("wdc.open.title"));
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set the test suite for this chooser.
     * @param ts The test suite to be used when opening or creating a work directory.
     */
    public void setTestSuite(TestSuite ts) {
        testSuite = ts;
    }

    /**
     * Set a test suite chooser to be used during error handling,
     * if the test suite referenced by an existing work directory
     * cannot be opened.
     * @param tsc the test suite chooser to be used
     */
    public void setTestSuiteChooser(TestSuiteChooser tsc) {
        testSuiteChooser = tsc;
    }

    /**
     * Get the work directory that was most recently selected by the user.
     * @return the work directorythat was most recently selected by the user
     * @see #setSelectedWorkDirectory
     * @see #showDialog
     */
    public WorkDirectory getSelectedWorkDirectory() {
        return workDir;
    }

    /**
     * Set the work directory selected by the user.
     * @param wd the work directory
     * @see #getSelectedWorkDirectory
     */
    public void setSelectedWorkDirectory(WorkDirectory wd) {
        if (wd != null)
            setSelectedFile(wd.getRoot());
        workDir = wd;
    }

    /**
     * Show a dialog to allow the user to select a work directory.
     * If a work directory is selected, it can be accessed via getSelectedWorkDirectory.
     * @param parent the component to be used at the parent of this dialog
     * @return an integer signifying how the dialog was dismissed
     * (APPROVE_OPTION or CANCEL_OPTION).
     * @see #APPROVE_OPTION
     * @see #CANCEL_OPTION
     * @see #getSelectedWorkDirectory
     */
    public int showDialog(Component parent) {
        return showDialog(parent, getApproveButtonText());
    }

    public void approveSelection() {
        // the validity of the selection depends on whether the
        // selected directory is to be created or opened.
        File wd = getSelectedFile();
        swda.setMode(mode);
        swda.setAllowNoTemplate(allowNoTemplate);
        boolean b;
        if (mode == NEW)
            b = swda.approveNewSelection(wd, testSuite);
        else
            b = swda.approveOpenSelection(wd, testSuite);

        if (b) {
            if (swda.isApprovedOpenSelection_dirExists())
                approveOpenSelection_dirExists(wd);
            workDir = swda.getWorkDirectory();

            try {
                Preferences prefs = Preferences.access();
                String defaultDir = workDir.getRoot().getParentFile().getCanonicalPath();
                prefs.setPreference(WorkDirChooseTool.DEFAULT_WD_PREF_NAME, defaultDir);
            }
            catch (IOException e) {}

            super.approveSelection();
        }
    }

    private static File normalize(File dir) {
        // check this and all parent directories, in case any one is a work directory
        for (File d = dir; d != null && !d.getName().equals(""); d = d.getParentFile()) {
            if (WorkDirectory.isWorkDirectory(d)) {
                // found a parent directory that is a test suite,
                // so normalize to this directory's parent
                File p = d.getParentFile();
                return (p != null ? p : dir);
            }
        }

        // no work directory found, so nothing wrong with this dir
        return dir;
    }

    private void approveOpenSelection_dirExists(File dir) {
        // the directory exists, but is not a work dir, so just traverse into it
        setCurrentDirectory(dir);
        setSelectedFile(null);
        setSelectedFiles(null);
    }

    private int mode;
    private TestSuite testSuite;
    private TestSuiteChooser testSuiteChooser;
    private WorkDirectory workDir;
    private boolean allowNoTemplate = false;

    private UIFactory uif;
    private SelectedWorkDirApprover swda;

    private Dimension size;
    private LinkedHashMap fileData;
    private LinkedHashMap wdData;

    public String getName(File f) {
        String retValue;
        String baseName = super.getName(f);
        if (!isWorkDir(f)) {
            return baseName;
        }

        String[] info = getInfo(f);
        if (info != null && info[1] != null && !"".equals(info[1])) {
            return baseName + " (" + info[1] + ")";
        }
        return super.getName(f);
    }


    private boolean isWorkDir(File dir) {
        if (wdData == null) wdData = new LinkedHashMap() {
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > 500;
                }
        };

        // MS Windows top-level folders ("My Computer's children")
        if (dir.getParent() == null || dir.getParent().startsWith("::") ) {
            return false;
        }

        String key = dir.getAbsolutePath();
        Boolean value = (Boolean) wdData.get(key);
        if (value != null) {
            return value.booleanValue();
        }
        value = new Boolean(WorkDirectory.isWorkDirectory(dir));
        wdData.put(key, value);
        return value.booleanValue();
    }

    private String[] getInfo(File file) {

        if (fileData == null) fileData = new LinkedHashMap() {
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > 500;
                }
        };
        String key = file.getAbsolutePath();
        String[] value = (String[]) fileData.get(key);
        if (value != null) {
            return value;
        }
        // refresh
        try {
            String[] data = new String[] {"", ""};
            String path = TemplateUtilities.getTemplateFromWd(file);
            if (path != null) {
                TemplateUtilities.ConfigInfo ci = TemplateUtilities.getConfigInfo(new File(path));
                if (ci != null) {
                    data = new String[] {ci.getName(), ci.getDescription()};
                }

            }
            fileData.put(key, data);
            return data;
        }
        catch  (Exception e) {
            return new String[] {"", ""};
        }
    }

    public void setAllowNoTemplate(boolean allowNoTemplate) {
        this.allowNoTemplate = allowNoTemplate;
    }



}
