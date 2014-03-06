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
package com.sun.javatest.mrep;

import java.awt.Component;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;

import com.sun.javatest.report.Report;
import com.sun.javatest.tool.IconFactory;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.util.FileInfoCache;

/**
 * A custom JFileChooser, for a user to choose a report directory.
 */
//
// see also com.sun.javatest.tool.WorkDirChooser
//
class ReportDirChooser extends JFileChooser
{
    // the following is a workaround to force the Report class to
    // be safely loaded, before the JFileChooser starts running its background
    // thread, exposing a JVM bug in class loading.
    static {
        Class reportClass = Report.class;
    }

    /**
     * Create a ReportDirChooser, initially showing the user's current directory.
     */
    public ReportDirChooser() {
        this(new File(System.getProperty("user.dir")));
    }

    /**
     * Create a ReportDirChooser, initially showing a given directory.
     * @param initialDir the initial directory to be shown
     */
    public ReportDirChooser(File initialDir) {
        super(initialDir);
        uif = new UIFactory(this, null); // no helpBroker required

        icon = IconFactory.getSelectableFolderIcon();

        // we want a filter that only selects directories
        setAcceptAllFileFilterUsed(false);
        setFileFilter(new RDC_FileFilter());

        // we want a file view that displays special icons for
        // report directories and sets directories non-traversable,
        // so that double clicking in the list view will select
        // (ie choose) them
        setFileView(new RDC_FileView());

        // we need FILES in the mode so that non-traversable
        // items appear in the list
        // -- see BasicDirectoryModel.LoadFilesThread
        // we need DIRECTORIES in the mode so that directories
        // (eg report directories) can be selected (ie chosen)
        // -- see BasicFileChooserUI.ApproveSelectionAction
        setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        // these parameters are still not ideal ...
        // approveSelection(File) below gets called for all directories:
        // we still have to redispatch according to whether it is a report
        // directory or not.
    }

    /**
     * A constant to indicate that a new report directory is to be created.
     * @see #setMode
     */
    public static final int NEW = 0;

    /**
     * A constant to indicate that an existing report directory is to be opened.
     * @see #setMode
     */
    public static final int OPEN = 1;

    /**
     * Set whether the chooser is to be used to create a new report directory
     * or to open an existing report directory.
     * @param mode a constant to indicate how the chooser is to operate
     * @see #NEW
     * @see #OPEN
     */
    public void setMode(int mode) {
        this.mode = mode;
        switch (mode) {
        case NEW:
            setApproveButtonText(uif.getI18NString("rdc.new.btn"));
            setApproveButtonToolTipText(uif.getI18NString("rdc.new.tip"));
            setDialogTitle(uif.getI18NString("rdc.new.title"));
            break;
        case OPEN:
            setApproveButtonText(uif.getI18NString("rdc.open.btn"));
            setApproveButtonToolTipText(uif.getI18NString("rdc.open.tip"));
            setDialogTitle(uif.getI18NString("rdc.open.title"));
            break;
        default:
            throw new IllegalStateException();
        }
    }

    /**
     * Get the report directory that was most recently selected by the user.
     * @return the report directory that was most recently selected by the user
     * @see #showDialog
     */
    public File getSelectedReportDirectory() {
        return reportDir;
    }

    /**
     * Show a dialog to allow the user to select a report directory.
     * If a report directory is selected, it can be accessed via getSelectedReportDirectory.
     * @param parent the component to be used at the parent of this dialog
     * @return an integer signifying how the dialog was dismissed
     * (APPROVE_OPTION or CANCEL_OPTION).
     * @see #APPROVE_OPTION
     * @see #CANCEL_OPTION
     * @see #getSelectedReportDirectory
     */
    public int showDialog(Component parent) {
        return showDialog(parent, getApproveButtonText());
    }

    public void approveSelection() {
        // the validity of the selection depends on whether the
        // selected directory is to be created or opened.
        File file = getSelectedFile();
        if (mode == NEW)
            approveNewSelection(file);
        else
            approveOpenSelection(file);
    }

    //-------------------------------------------------------------------------

    private void approveNewSelection(File file) {
        if (file.exists()) {
            if (isReportDirectory(file) || isEmptyDirectory(file)) {
                // create new report in existing or empty report directory
                reportDir = file;
                super.approveSelection();
            }
            else if (file.isDirectory()) {
                // the directory exists, but is neither a report dir nor empty: open it
                setCurrentDirectory(file);
                setSelectedFile(null);
                setSelectedFiles(null);
            }
            else {
                uif.showError("rdc.notADir", file);
            }
        }
        else {
            // create new report in new report directory
            reportDir = file;
            super.approveSelection();
        }
    }

    //-------------------------------------------------------------------------

    private void approveOpenSelection(File file) {
        if (file.exists()) {
            if (isReportDirectory(file)) {
                reportDir = file;
                super.approveSelection();
            }
            else if (isDirectory(file)) {
                // the directory exists, but is not a report dir: open it
                setCurrentDirectory(file);
                setSelectedFile(null);
                setSelectedFiles(null);
            }
            else
                uif.showError("rdc.notADir", file);
        }
        else
            uif.showError("rdc.cantOpen", file);
    }

    //-------------------------------------------------------------------------

    private boolean isDirectory(File f) {
        return (f.isDirectory());
    }

    private boolean isReportDirectory(File f) {
        if (isIgnoreable(f))
            return false;

        Boolean b =  cache.get(f);
        if (b == null) {
            boolean v = Report.isReportDirectory(f);
            String[] l = f.list();
            if (l != null) {
                for (int i = 0; i < l.length; i++) {
                    if (l[i].endsWith(".html")) {
                        v = true;
                    }
                }
            }
            cache.put(f, v);
            return v;
        }
        else
            return b;
    }

    private boolean isEmptyDirectory(File f) {
        return (f.isDirectory() && f.list().length == 0);
    }

    private boolean isIgnoreable(File f) {
        // Take care not touch the floppy disk drive on Windows
        // because if there is no disk in it, the user will get a dialog.
        // Root directories (such as A:) have an empty name,
        // so use that to avoid touching the file itself.
        // This means we can't put a work directory in the root of
        // the file system, but that is a lesser inconvenience
        // than those floppy dialogs!
        return (f.getName().equals(""));
    }

    private FileInfoCache cache = new FileInfoCache();

    private int mode;
    private File reportDir;
    private UIFactory uif;
    private Icon icon;

    private class RDC_FileView extends FileView {
        public String getDescription(File f) {
            return null;
        }

        public Icon getIcon(File f) {
            return (isReportDirectory(f) ? icon : null);
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
            return (isDirectory(f) && !isReportDirectory(f) ? Boolean.TRUE : Boolean.FALSE);
        }
    }

    private class RDC_FileFilter extends FileFilter {
        public boolean accept(File f) {
            return (isDirectory(f));
        }

        public String getDescription() {
            return uif.getI18NString("rdc.ft");
        }
    }
}
