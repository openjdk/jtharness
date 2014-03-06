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
package com.sun.javatest.exec;

import java.awt.Color;

import javax.swing.JPanel;

import com.sun.javatest.JavaTestError;
import com.sun.javatest.TestDescription;
import com.sun.javatest.TestResult;
import com.sun.javatest.TestSuite;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.util.I18NResourceBundle;

/**
 * Base class for the individual displays of the TestPanel.
 */

abstract class TP_Subpanel extends JPanel
{
    TP_Subpanel(UIFactory uif, String name) {
        this.uif = uif;
        setName(name);
        setBackground(Color.white);
    }

    void setTestSuite(TestSuite ts) {
        testSuite = ts;
    }

    boolean isUpdateRequired(TestResult currTest) {
        return (subpanelTest != currTest);
    }

    protected void updateSubpanel(TestResult currTest) {
        //System.out.println(getClass().getName() + ".updateSubpanel()");
        subpanelTest = currTest;

        try {
            subpanelDesc = currTest.getDescription();
        }
        catch (TestResult.Fault e) {
            I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(getClass());
            throw new JavaTestError(i18n, "test.noDesc", e);
        }
    }

    protected TestSuite testSuite;
    protected TestResult subpanelTest;
    protected TestDescription subpanelDesc;
    protected UIFactory uif;
}

