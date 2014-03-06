/*
 * $Id$
 *
 * Copyright (c) 2006, 2011, Oracle and/or its affiliates. All rights reserved.
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

import javax.swing.JMenuItem;
import com.sun.javatest.TestResult;

/**
 * Class to encapsulate a custom context menu item to be added to the GUI by a
 * test suite.  The method <code>getMenuApplication</code> determines which type
 * of situations the menu should be presented in.  Processing the actual selection
 * action event for the menu item(s) should be processed as you normally would
 * with the Swing-provided Action mechanisms.
 *
 * All methods in this API will be invoked on the GUI event thread.
 */
public abstract class JavaTestContextMenu
{
    /**
     * Create a new instance, based on this instance.  The purpose of this is to allow
     * this menu item to appear in multiple places in the interface.
     */
    public abstract JavaTestContextMenu newInstance();

    /**
     * Get the actual component for this menu.  A single menu item is
     * recommended, but a it may also be a submenu.  The reference returned
     * should not change after this method has been invoked the first time.
     * This allows for lazy initialization, but then permits caching by the
     * client of the API.
     * @return A menu to be displayed to the user.  Must never be null.
     */
    public abstract JMenuItem getMenu();

    /**
     * Determine the contexts in which this menu is applicable.  Unless the value
     * is set to <code>CUSTOM</code>, the system managing the menus assumes that
     * it is allowed to control the enabled/disabled state of the menu item.
     * @see #TESTS_AND_FOLDERS
     * @see #TESTS_ONLY
     * @see #FOLDERS_ONLY
     * @see #CUSTOM
     */
    public abstract int getMenuApplication();

    /**
     * May multiple nodes be selected at one time for this menu item to be
     * enabled.  If not allowed and multiple items are currently selected,
     * updateState() methods will not be called.  The default state of this
     * setting is <code>false</code>, override this method to change.
     *
     * If you need to enable/disabled based on the composition of the selection,
     * you should return <code>true</code>, then override <code>updateState(String[],
     * TestResult[])</code> and disable if needed.
     * @return True if multiselect is permitted for this menu action.  False
     *         otherwise.
     */
    public boolean isMultiSelectAllowed() { return false; }

    /**
     * Called upon when a test is selected by the user.  This callback allows the
     * implementation to dynamically adjust it's state.  It is recommended that
     * this method remain unimplemented unless really needed, since it is
     * processed synchronously on the event thread.  It is critical that the
     * implementation of this method be reasonably fast and non-blocking
     * because it will may be invoked each time the user requests a popup to be
     * displayed.
     *
     * Implementations which return <code>CUSTOM</code> from
     * <code>getMenuApplication</code> will generally override this.  This method
     * is called regardless of what application type the object indicates.
     *
     * The controlling class will first enable/disable the menu based on the
     * application type (unless <code>CUSTOM</code>).
     *
     * This method will be invoked on the GUI event thread.
     * @param tr The test result object which the users is acting upon.
     */
    public void updateState(TestResult tr) { }

    /**
     * Called upon when a folder is selected by the user.  It is in
     * canonical internal form - forward slash separated.
     *
     * This method will be invoked on the GUI event thread.
     * @param path
     */
    public void updateState(String path) { }

    /**
     * Called upon when multiple items have been selected by the user.  The union
     * of the two parameters represents the leafs selected by the user to act upon.
     * The intersection of those two parameters is empty.
     *
     * This method will be invoked on the GUI event thread.
     * @param folders The test paths which the user is acting upon, only the folders.
     *              The strings are forward slash separated locations within the
     *              test suite, a substring of that which would be returned by
     *              <code>TestDescription.getRootRelativeURL()</code>.  Null if
     *              none.
     * @param trs The tests which the user is acting upon.  Null if none.
     * @see com.sun.javatest.TestDescription#getRootRelativeURL
     */
    public void updateState(String[] folders, TestResult[] trs) { }

    public static final int TESTS_AND_FOLDERS = 0;
    public static final int TESTS_ONLY = 1;
    public static final int FOLDERS_ONLY = 2;
    public static final int CUSTOM = 99;
}
