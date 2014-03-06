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

import java.util.ArrayList;
import javax.swing.JMenuItem;

/**
 * Class to manage custom menus.  This class allows the test suite architect to
 * add various custom menu items to designated places in the menu system of the
 * Test Manager.  The constants in this class represent those designated
 * positions and are semantic locations, not absolute.  This allows
 * reorganization of menus at the harness level without necessarily breaking
 * the positioning that the architect expected.
 *
 * This class can be used in two ways.  First, the concrete class overrides
 * <code>getMenuItems()</code> to return the correct set of items for the given
 * category.  This method may be the most simple for straight forward insertions.
 * The second method is to use <code>addMenuItem()</code> to sequentially
 * specify which menu items should appear.  The default implementation of
 * <code>getMenuItems()</code> will use data provided by using this second
 * method.
 *
 * By default, no menu category will have any custom menus (<code>getMenuItems()</code>
 * will always return null).
 *
 * The <code>JMenuItem</code> objects may be "pull-right" menus if desired.  It is
 * the responsibility of the architect to manage keystroke mneumonics.
 */
public abstract class JavaTestMenuManager {
    /**
     * Get the menu items to go into the specified position in the menu system.
     * See the constants in this class for the possible value.
     * @param position The menu position, one of the constants of this class.
     * @return The custom menu items to be displayed in the given position.
     *         Null if there are none.  Never a zero-length array.
     * @throws IllegalArgumentException If the position parameter is out of
     *         range.  This is usually the fault of the harness itself, but
     *         may occur if classes are compiled against one development version
     *         of the harness and run with another.
     */
    public JMenuItem[] getMenuItems(int position) {
        if (bank == null)
            return null;
        else {
            ArrayList al = bank[position];
            if (al.size() == 0)
                return null;
            else {
                JMenuItem[] result = new JMenuItem[al.size()];
                al.toArray(result);
                return result;
            }
        }
    }

    /**
     * Add a menu item to the given menu position.
     * The item is added to the bottom, in that position, so you must add them
     * in the order you wish them to appear.
     * @param position The menu position, one of the constants of this class.
     * @param item The menu item to add.
     * @throws IndexOutOfBoundsException If the position index is out of
     *     range.  Be sure that you are using the constants given in this
     *     class to supply this parameter.
     */
    protected synchronized void addMenuItem(int position, JMenuItem item) {
        if (position > NUM_POSITIONS)
            throw new IndexOutOfBoundsException("Position index too large - " +
                position);
        if (position < 0)
            throw new IndexOutOfBoundsException("Position index too small - " +
                position);

        if (bank == null) {
            bank = new ArrayList[NUM_POSITIONS];
            for (int i = 0; i < NUM_POSITIONS; i++)
                bank[i] = new ArrayList();
        }

        bank[position].add(item);
    }

    public static final int FILE_PRIMARY = 0;
    public static final int FILE_OTHER = 2;

    public static final int CONFIG_PRIMARY = 4;
    public static final int CONFIG_VIEW = 5;
    public static final int CONFIG_OTHER = 6;

    public static final int RUN_PRIMARY = 7;
    public static final int RUN_OTHER = 8;

    public static final int WINDOWS_MAIN = 9;
    public static final int PRESENTATION = 10;
    public static final int PREFERENCES = 11;

    public static final int LOG_VIEW = 12;
    public static final int LOG_CONFIG = 13;

    public static final int HELP_PRIMARY = 14;
    public static final int HELP_TESTSUITE = 15;
    public static final int HELP_ABOUT = 16;

    public static final int TOOLS_OTHER = 17;

    private static final int NUM_POSITIONS = 18;
    private ArrayList[] bank;
}
