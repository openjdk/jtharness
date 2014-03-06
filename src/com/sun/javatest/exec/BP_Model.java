/*
 * $Id$
 *
 * Copyright (c) 2001, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Component;
import com.sun.javatest.TestFilter;
import com.sun.javatest.TestResult;

/**
 * This model represents the facilities available to a branch panel.  It is
 * intended to shield subpanels from knowing the underlying operations of
 * message fields and TestTreeModel.
 */
interface BP_Model {
    /**
     * Is the harness currently running tests?
     */
    public boolean isRunning();

    public void showMessage(String msg);

    /**
     * @param tr The test that should be made active in the GUI.
     * @param path Path to the target test, where the first element is the
     *        root of the appropriate TRT, and the last element is the test.
     */
    public void showTest(TestResult tr, Object[] path);

    /**
     * Do whatever may be needed to enable/disable the given component in the overall
     * view of things.  Disabling a tab for example.
     *
     * @param c The component to affect.
     * @param state True to enable, false to disable.
     */
    public void setEnabled(Component c, boolean state);

    /**
     * Is the given component enabled or not?
     *
     * @param c The component to query about.
     * @param state True to enable, false to disable.
     * @return True if the given component is enabled, false otherwise, including the
     *         case where the given component can't be found.
     * @see #setEnabled
     */
    public boolean isEnabled(Component c);

    /**
     * Return the current test filter.
     */
    public TestFilter getFilter();
}
