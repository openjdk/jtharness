/*
 * $Id$
 *
 * Copyright (c) 2006, 2009, Oracle and/or its affiliates. All rights reserved.
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
import javax.swing.JOptionPane;

import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.util.FileInfoCache;
import com.sun.javatest.util.I18NResourceBundle;
import javax.help.DefaultHelpBroker;

/**
 *
 */
public class SelectedWorkDirApprover {
    /**
     * Create a WorkDirChooser, initially showing the user's current directory.
     */
    public SelectedWorkDirApprover(int mode, Component parent) {
        this.mode = mode;
        this.parent = parent;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    //-------------------------------------------------------------------------
    public boolean approveNewSelection(File dir, TestSuite testSuite) {
        if (testSuite == null)
            throw new IllegalStateException();

        if (dir.exists()) {
            if (isWorkDirectory(dir))
                return approveNewSelection_workDirExists(dir, testSuite);
            else if (dir.isDirectory())
                return approveNewSelection_dirExists(dir, testSuite);
            else
                uif.showLiteralError(null, i18n.getString("wdc.notADir.err", dir));
        }
        else
            return approveNewSelection_dirNotFound(dir, testSuite);
        return false;
    }

    private boolean approveNewSelection_workDirExists(File dir, TestSuite testSuite) {
        int option = uif.showLiteralYesNoDialog(i18n.getString("wdc.exists_openIt.title"),
                            i18n.getString("wdc.exists_openIt.txt"));

        if (option != JOptionPane.YES_OPTION)
            return false;

        try {
            workDir = WorkDirectory.open(dir, testSuite);

            // I don't think the following test can ever succeed, because the open
            // will fail and throw a WorkDirectory.MismatchFault.
            if (testSuite != null && workDir.getTestSuite() != testSuite) {
                uif.showLiteralError(null, i18n.getString("wdc.wrongTS.err"));
                return false;
            }
            openInsteadOfCreate = true;
            return true;
        }
        catch (FileNotFoundException e) {
            // should not happen: work directory is known to exist
            //uif.showError("wdc.cantFindDir", dir.getPath());
            uif.showLiteralError(null, i18n.getString("wdc.cantFindDir.err", dir.getPath()));
            return false;
        }
        catch (WorkDirectory.MismatchFault e) {
            uif.showLiteralError(null, i18n.getString("wdc.wrongTS.err"));
            return false;
        }
        catch (WorkDirectory.Fault e) {
            //uif.showError("wdc.cantOpen", e.getMessage());
            uif.showLiteralError(null, i18n.getString("wdc.cantOpen.err", e.getMessage()));
            return false;
        }
    }

    private boolean approveNewSelection_dirExists(File dir, TestSuite testSuite) {
        // the directory exists, but is not a work dir
        int option = uif.showYesNoDialog("wdc.existsNotWorkDir_convert");
        if (option != JOptionPane.YES_OPTION)
            return false;

        try {
            workDir = WorkDirectory.convert(dir, testSuite);
            return true;
        }
        catch (FileNotFoundException e) {
            uif.showError("wdc.cantFindDir", dir.getPath());
            return false;
        }
        catch (WorkDirectory.Fault e) {
            uif.showError("wdc.cantConvert", e.getMessage());
            return false;
        }
    }

    private boolean approveNewSelection_dirNotFound(File dir, TestSuite testSuite) {
        try {
            workDir = WorkDirectory.create(dir, testSuite);
            return true;
        }
        catch (WorkDirectory.Fault e) {
            uif.showError("wdc.cantCreate", e.getMessage());
            return false;
        }
    }

    //-------------------------------------------------------------------------

    public boolean approveOpenSelection(File dir, TestSuite testSuite) {
        if (dir.exists()) {
            if (isWorkDirectory(dir))
                return approveOpenSelection_workDirExists(dir, testSuite);
            else if (dir.isDirectory()) {
                approveOpenSelection_dirExists = false;
                return approveOpenSelection_dirExists;
            } else {
                uif.showLiteralError(null, i18n.getString("wdc.notADir.err", dir));
            }   // inner if
        }
        else
            return approveOpenSelection_dirNotFound(dir, testSuite);
        return false;
    }

    private boolean approveOpenSelection_workDirExists(File dir, TestSuite testSuite) {

        try {
            switch (mode) {
            case WorkDirChooser.OPEN_FOR_GIVEN_TESTSUITE:
                if (testSuite == null)
                    throw new IllegalStateException();

                workDir = WorkDirectory.open(dir, testSuite);
                // I don't think the following test can ever happen, because the open
                // will fail and throw a WorkDirectory.MismatchFault.
                if (workDir.getTestSuite() != testSuite) {
                    uif.showLiteralError(null, i18n.getString("wdc.wrongTS.err"));
                    return false;
                }
                break;

            case WorkDirChooser.OPEN_FOR_ANY_TESTSUITE:
                try {
                    workDir = WorkDirectory.open(dir);
                }
                catch (WorkDirectory.TestSuiteFault e) {
                    // error opening test suite -- allow user to specify new test suite
                    int option = uif.showYesNoDialog("wdc.tsError_specifyNew", e.getMessage());
                    if (option != JOptionPane.YES_OPTION)
                        return false;

                    // ensure testSuiteChooser initialized
                    if (testSuiteChooser == null)
                        testSuiteChooser = new TestSuiteChooser();

                    // set a context in the chooser if one is available
                    if (testSuite != null)
                        testSuiteChooser.setSelectedTestSuite(testSuite);

                    // display the chooser
                    testSuiteChooser.showDialog(parent);

                    // get the alternate test suite
                    TestSuite newTestSuite = testSuiteChooser.getSelectedTestSuite();

                    // user cancelled dialog, so exit out of approve*
                    if (newTestSuite == null)
                        return false;

                    // try using that new test suite
                    workDir = WorkDirectory.open(dir, newTestSuite);

                }
                                catch(WorkDirectory.TemplateMissingFault e) {
                    // error opening test suite -- allow user to specify new test suite
                    int option = uif.showYesNoDialog("wdc.tsError_specifyTemplate", e.getMessage());
                    if (option != JOptionPane.YES_OPTION)
                        return false;

                    // display the chooser
                    FileChooser chooser = new FileChooser();
                                        chooser.addChoosableExtension(JTM, uif.getI18NString("ce.jtmFiles"));
                                        chooser.showOpenDialog(parent);

                    // get the alternate test suite
                    File newTemplate = chooser.getSelectedFile();

                    // user cancelled dialog, so exit out of approve*
                    if (newTemplate == null)
                        return false;

                    // try using that new template
                                        WorkDirectory.changeTemplate(dir, newTemplate);
                    workDir = WorkDirectory.open(dir);

                                }
                break;
            }
            return true;
        }
        catch (FileNotFoundException e) {
            uif.showLiteralError(null, i18n.getString("wdc.cantFindDir.err", dir.getPath()));
        }
        catch (WorkDirectory.MismatchFault e) {
            uif.showLiteralError(null, i18n.getString("wdc.wrongTS.err"));
        }
        catch (WorkDirectory.Fault e) {
            uif.showLiteralError(null, i18n.getString("wdc.cantOpen.err", e.getMessage()));
        }
        return false;
    }

    private boolean approveOpenSelection_dirNotFound(File dir, TestSuite testSuite) {
        if (testSuite == null) {
            uif.showError("wdc.notFound_noTestSuite");
            return false;
        } if (! allowNoTemplate) {
            uif.showLiteralError(null, i18n.getString("wdc.cantFindDir.err", dir.getPath()));
            return false;
        } else {
            int option = uif.showYesNoDialog("wdc.notFound_createIt", testSuite.getPath());
            if (option != JOptionPane.YES_OPTION)
                return false;
        }

        try {
            workDir = WorkDirectory.create(dir, testSuite);
        }
        catch (WorkDirectory.Fault e) {
            uif.showError("wdc.cantCreate", e.getMessage());
            return false;
        }
        return true;
    }

    //-------------------------------------------------------------------------

    public boolean isWorkDirectory(File f) {
        if (isIgnoreable(f))
            return false;

        Boolean b =  cache.get(f);
        if (b == null) {
            boolean v = WorkDirectory.isWorkDirectory(f);
            cache.put(f, v);
            return v;
        }
        else
            return b;
    }

    public static boolean isIgnoreable(File f) {
        // Take care not touch the floppy disk drive on Windows
        // because if there is no disk in it, the user will get a dialog.
        // Root directories (such as A:) have an empty name,
        // so use that to avoid touching the file itself.
        // This means we can't put a work directory in the root of
        // the file system, but that is a lesser inconvenience
        // than those floppy dialogs!
        return (f.getName().equals(""));
    }

    public boolean isApprovedOpenSelection_dirExists() {
        return approveOpenSelection_dirExists;
    }

    public WorkDirectory getWorkDirectory() {
        return workDir;
    }

    void setAllowNoTemplate(boolean allowNoTemplate) {
        this.allowNoTemplate = allowNoTemplate;
    }

    /**
     * @return true if the work dir is opened when expected to be created
     * (trying to create workdir which already exists)
     */
    public boolean isOpenedInsteadOfCreated() {
        return openInsteadOfCreate;
    }

    private FileInfoCache cache = new FileInfoCache();

    private int mode;
    private TestSuiteChooser testSuiteChooser;
    private WorkDirectory workDir;
    private Component parent;
    private boolean approveOpenSelection_dirExists = false;

    private UIFactory uif = new UIFactory(SelectedWorkDirApprover.class, new DefaultHelpBroker());
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(SelectedWorkDirApprover.class);

    private boolean allowNoTemplate = false;
    static final String JTM = ".jtm";
    // flag which is set if work dir is loaded when expected to be created
    private boolean openInsteadOfCreate = false;
}
