/*
 * $Id$
 *
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.InterviewParameters;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;

/**
 * Interface that will be invoked, when QuickStartWizard has finished.
 *
 * @author Dmitry Fazunenko
 */
public interface QSW_Listener {
    /**
     * Invoked when user decided to open new TestSuite
     *
     * @param ts TestSuite to be viewed
     * @param wd WorkDir to be openned
     * @param ip Config to be used
     * @param showConfigEditorFlag - true, if user wants to edit config
     *        right after TestSuite appeared
     * @param runTestsFlag - true, if user wants to run tests
     *        right after TestSuite appeared
     */
    void finishQSW(TestSuite ts, WorkDirectory wd, InterviewParameters ip,
            boolean showConfigEditorFlag, boolean runTestsFlag);

    /**
     * Invoked when user decided to not open new TestSuite
     */
    void cancelQSW();


}
