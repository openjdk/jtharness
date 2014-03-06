/*
 * $Id$
 *
 * Copyright (c) 2002, 2010, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.exec;

import com.sun.interview.Interview;
import java.util.ResourceBundle;

import com.sun.javatest.InterviewParameters;
import com.sun.javatest.TestSuite;
import com.sun.javatest.TestResultTable;
import com.sun.javatest.WorkDirectory;
import java.awt.print.Printable;

public interface ExecModel
{
    TestSuite getTestSuite();

    WorkDirectory getWorkDirectory();

    InterviewParameters getInterviewParameters();

    FilterConfig getFilterConfig();

    ContextManager getContextManager();

    /**
     * Get the test result table currently in use for display.
     * This value provides a temporary answer if the work directory does not
     * exist yet.  If a work directory is available, the query is forwarded
     * to that object.
     * @return the test result table currently in use for display.
     */
    TestResultTable getActiveTestResultTable();

    void showWorkDirDialog(boolean allowTemplates);

    /**
     * Show the configuration editor for this tool, and optionally run
     * the tests defined by the configuration when the editor is closed.
     * @param runTestsWhenDone true if the tests should automatically
     * be run when the user closes the configuration editor,
     * and false otherwise.
     */
    void showConfigEditor(boolean runTestsWhenDone);

    /**
      * Show the configuration editor for this tool with current template
      */
    void showTemplateEditor();

    /**
     * Causes a series of actions to be performed to complete configuration.
     */
    void configure();

    /**
     * Returns true if the model is in the configuration stage, user is either
     * selecting work directory or editing configuration. One can relay on this
     * method to disable/enable some actions.
     */
    boolean isConfiguring();

    /**
     * Run specified tests.
     * @param urls These names may either be paths to folders or test names.
     * Empty string value in the array indicates that the whole test suite
     * should be run.
     */
    void runTests(String[] urls);

    void showMessage(ResourceBundle msgs, String key);

    void printSetup();

    void print(Printable p);

    void setWorkDir(WorkDirectory wd, boolean addToFileHistory)
        throws Interview.Fault, TestSuite.Fault;

    ExecToolManager getExecToolManager();
}
