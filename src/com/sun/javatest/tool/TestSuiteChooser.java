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
import java.io.File;
import java.io.FileNotFoundException;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import com.sun.javatest.TestSuite;
import com.sun.javatest.util.FileInfoCache;

/**
 * A custom JFileChooser, for a user to choose a test suite.
 */
public class TestSuiteChooser extends JFileChooser
{

    /**
     * Create a TestSuiteChooser, initially showing the user's current directory.
     */
    public TestSuiteChooser() {
        this(new File(System.getProperty("user.dir")));
    }

    /**
     * Create a TestSuiteChooser, initially showing a given directory.
     * @param initialDir the initial directory to be shown
     */
    public TestSuiteChooser(File initialDir) {
        super(normalize(initialDir));
        setName("tsc");

        uif = new UIFactory(this, null);  // no helBroker required
        icon = IconFactory.getSelectableFolderIcon();

        setDialogTitle(uif.getI18NString("tsc.title"));
        setApproveButtonText(uif.getI18NString("tsc.open.btn"));
    setApproveButtonMnemonic(uif.getI18NMnemonic("tsc.open.mne"));
    setApproveButtonToolTipText(uif.getI18NString("tsc.open.tip"));

        // only want to accept test suites
        setAcceptAllFileFilterUsed(false);
        setFileFilter(new TSC_FileFilter());

        // we want a file view that displays special icons for
        // test suites and sets test suite directories non-traversable,
        // so that double clicking in the list view will select
        // (ie choose) them
        setFileView(new TSC_FileView());

        // we need FILES in the mode so that non-traversable
        // items appear in the list
        // -- see BasicDirectoryModel.LoadFilesThread
        // we need DIRECTORIES in the mode so that directories
        // (eg test suite directories) can be selected (ie chosen)
        // -- see BasicFileChooserUI.ApproveSelectionAction
        setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        // these parameters are still not ideal ...
        // approveSelection(File) below gets called for all directories:
        // we still have to redispatch according to whether it is a work
        // directory or not.
    }

    /**
     * Get the test suite that was most recently selected by the user.
     * @return the test suite that was most recently selected by the user
     * @see #showDialog
     * @see #setSelectedTestSuite
     */
    public TestSuite getSelectedTestSuite() {
        return selectedTestSuite;
    }

    /**
     * Set the selected test suite.
     * @param ts the test suite to select
     * @see #getSelectedTestSuite
     */
    public void setSelectedTestSuite(TestSuite ts) {
        if (ts != null)
            setSelectedFile(ts.getRoot());
        selectedTestSuite = ts;
    }

    /**
     * Show a dialog to allow the user to select a test suite.
     * If a test suite is selected, it can be accessed via getSelectedTestSuite.
     * @param parent the component to be used at the parent of this dialog
     * @return an integer signifying how the dialog was dismissed
     * (APPROVE_OPTION or CANCEL_OPTION).
     * @see #APPROVE_OPTION
     * @see #CANCEL_OPTION
     * @see #getSelectedTestSuite
     */
    public int showDialog(Component parent) {
        return showDialog(parent, getApproveButtonText());
    }

    // override JFileChooser method
    public void approveSelection() {
        File file = getSelectedFile();

        if (isTraversable(file)) {
            // This is a to work around a bug in JFileChooser.
            // When double-clicking on the list, the "traversable"
            // attribute is used to determine whether to approve the
            // selection or to set it as the current "directory".
            // That is good.  However, the Open (Approve) button
            // simply looks at whether it is a directory or not,
            // which requires that the selectionMode must include
            // DIRECTORIES.  It must separately include FILES otherwise
            // they don't show up in the list.  Bottom line is that
            // this method may be called with directories we don't want.
            // So we check if we want (to open) this directory,
            // and if not, we simply set it as the current directory.
            setCurrentDirectory(file);
            // Unfortunately, if we are here, the selectedFile has been
            // trampled prior to approveSelection being called.
            // The best that we can do is clear it
            setSelectedFile(null);
            setSelectedFiles(null);
            return;
        }

        // mainline code to approve a selection
        try {
            selectedTestSuite = TestSuite.open(file);
            super.approveSelection();
        }
        catch (FileNotFoundException e) {
            uif.showError("tsc.cantFindTestSuite", e.getMessage());
        }
        catch (TestSuite.Fault e) {
            uif.showError("tsc.notATestSuite", e.getMessage());
        }
    }

    private boolean isDirectory(File f) {
        return f.isDirectory();
    }

    private boolean isTestSuite(File f) {
        // Take care not touch the floppy disk drive on Windows
        // because if there is no disk in it, the user will get a dialog.
        // Root directories (such as A:) have an empty name,
        // so use that to avoid touching the file itself.
        // This means we can't put a test suite in the root of
        // the file system, but that is a lesser inconvenience
        // than floppy dialogs!
        if (isIgnoreable(f))
            return false;

        Boolean b = cache.get(f);
        if (b == null) {
            boolean v = TestSuite.isTestSuite(f);
            cache.put(f, v);
            return v;
        }
        else
            return b;
    }

    private static boolean isIgnoreable(File f) {
        // Take care not touch the floppy disk drive on Windows
        // because if there is no disk in it, the user will get a dialog.
        // Root directories (such as A:) have an empty name,
        // so use that to avoid touching the file itself.
        // This means we can't put a test suite in the root of
        // the file system, but that is a lesser inconvenience
        // than floppy dialogs!
        return (f.getName().equals(""));
    }

    private static File normalize(File dir) {
        // check this and all parent directories, in case any one is a test suite
        for (File d = dir; d != null && !isIgnoreable(d); d = d.getParentFile()) {
            if (TestSuite.isTestSuite(d)) {
                // found a parent directory that is a test suite,
                // so normalize to this directory's parent
                File p = d.getParentFile();
                return (p != null ? p : dir);
            }
        }

        // no test suite found, so nothing wrong with this dir
        return dir;
    }

    private FileInfoCache cache = new FileInfoCache();
    private TestSuite selectedTestSuite;
    private UIFactory uif;
    private Icon icon;

    private class TSC_FileView extends FileView {
        public String getDescription(File f) {
            return null;
        }

        public Icon getIcon(File f) {
            return (isTestSuite(f) ? icon : null);
        }

        public String getName(File f) {
            // Take care to get names of file system roots correct
            String name = f.getName();
            return (name.length() == 0 ? f.getPath() : name);
        }

        public String getTypeDescription(File f) {
            return null;
        }

        public Boolean isTraversable(File f) {
            return (isDirectory(f) && !isTestSuite(f) ? Boolean.TRUE : Boolean.FALSE);
        }
    }

    private class TSC_FileFilter extends FileFilter {
        public boolean accept(File f) {
            return (isDirectory(f) ? true : isTestSuite(f));
        }

        public String getDescription() {
            return uif.getI18NString("tsc.ft");
        }
    }
}
