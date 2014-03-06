/*
 * $Id$
 *
 * Copyright (c) 2006, 2010, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.TestResult;
import javax.swing.JPanel;

/**
 * Base class defines custom viewers for test results. For example custom viewers
 * can be used for representation of test benchmark.
 */
public abstract class CustomTestResultViewer extends JPanel {

    /**
     * This method is called by the harness and it indicates to the viewer that it should
     * check the given TestResult object for changes.<br>
     * There can be three possible cases:
     * <li>TestResult is a different test, therefore the viewer should fully update the panel</li>
     * <li>TestResult is the current test, the viewer should check the result for updates</li>
     * <li>TestResult is null. There is no selected test.</li>
     * @param currTestResult Test result object which should be rendered in this
     *    panel immediately.  May or may not be the one currently displayed.
     */
    public abstract void setResult(TestResult currTestResult);

    /**
     * Returns the name of this CustomTestResultViewer.
     * @return <tt>title</tt> for the viewer.
     * Return value can't be null or empty String
     **/
    public abstract String getTitle();

    /**
     * Returns the description of the current CustomTestResultViewer.
     * Get the long description of this CustomTestResultViewer's purpose.
     * May be multiple sentences if desired.
     * The return value should be localized.
     * @return <tt>description</tt> for the viewer.
     * Return value can't be null or empty String
     **/
    public abstract String getDescription();

    /**
     * Makes the viewer visible or invisible.
     * @param aFlag  true to make the viewer visible; false to
     *          make it invisible
     */
    public void setViewerVisible(boolean aFlag) {
        if (aFlag != isVisible) {
            firePropertyChange(visibleProperetyName, isVisible, aFlag);
            isVisible = aFlag;
        }
    }

    /**
     * Determines whether this viewer should be visible when its
     * parent is visible
     *
     * @return <code>true</code> if the viewer is visible,
     * <code>false</code> otherwise
     */
    public boolean isViewerVisible() {
        return isVisible;
    }

    /**
     * This method is called when a new test from the test tree is selected by
     * user. It does nothing by default.
     * Subclasses might override this method, e.g. to change
     * the <tt>TestResult</tt> object to observe upon.
     *
     * @param newTR is a <tt>TestResult</tt> object for  selected test.
     * @param isActive true if this <tt>CustomTestResultViewer</tt> is currently
     * selected otherwise false
     */
    public void onCangedTestResult(TestResult newTR, boolean isActive) {
        return ;
    }


    private boolean isVisible = true;
    final static String visibleProperetyName = "visibleTab";

}
