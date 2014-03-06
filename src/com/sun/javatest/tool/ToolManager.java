/*
 * $Id$
 *
 * Copyright (c) 2001, 2012, Oracle and/or its affiliates. All rights reserved.
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

import java.net.URL;
import java.util.Map;
import java.util.MissingResourceException;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import com.sun.javatest.util.I18NResourceBundle;

/**
 * Tool managers are relatively lightweight managers for tools
 * that provide end-user GUI functionality.
 *
 * @see Tool
 */
public abstract class ToolManager
{
    /**
     * This exception is used to report problems while using a tool manager.
     */
    public static class Fault extends Exception
    {
        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         */
        public Fault(I18NResourceBundle i18n, String s) {
            super(i18n.getString(s));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An argument to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        public Fault(I18NResourceBundle i18n, String s, Object o) {
            super(i18n.getString(s, o));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An array of arguments to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        public Fault(I18NResourceBundle i18n, String s, Object[] o) {
            super(i18n.getString(s, o));
        }
    }

    //----------------------------------------------------------------------------

    /**
     * Create a tool manager to manage tools on a desktop.
     * @param desktop the desktop for which this manager should manage tools
     */
    protected ToolManager(Desktop desktop) {
        this.desktop = desktop;
        i18n = I18NResourceBundle.getBundleForClass(getClass());
    }

    /**
     * Get the desktop for which this manager is managing tools.
     * @return the desktop for which this manager should manage tools
     */
    public Desktop getDesktop() {
        return desktop;
    }

    /**
     * Get details about any user preferences supported by this tool manager.
     * @return an object to handle preferences supported by this tool manager,
     * or null if no preferences are supported.
     */
    public PreferencesPane getPrefsPane() {
        return null;
    }

    /**
     * Get handlers for any files that can be opened by this tool manager.
     * @return a set of handlers for files that can be opened by this tool manager,
     * or null if none available.
     */
    public FileOpener[] getFileOpeners() {
        return null;
    }

    /**
     * Get actions for any items to appear in the main section of the desktop
     * File menu.
     * @return an array of Actions to appear on the File menu, or null if none
     * are required.
     */
    public Action[] getFileMenuActions() {
        return null;
    }

    /**
     * Get primary file menu opereations for this tool.  These are placed after
     * the actions.  They will be shown in the order given in the array.  There
     * are none provided by default (null).
     * @return Array of menu items to be shown for this tool.  Nill if none.
     * @see #getFileMenuActions
     */
    public JMenuItem[] getFileMenuPrimaries() {
        return null;
    }

    /**
     * Get secondary file menu opereations for this tool.  These are placed after
     * all primary actions from all tools, but before the global operations like
     * preferences, close and exit.
     * They will be shown in the order given in the array.  There
     * are none provided by default (null).
     * @return Array of menu items to be shown for this tool.  Nill if none.
     * @see #getFileMenuActions
     */
    public JMenuItem[] getFileMenuSecondaries() {
        return null;
    }

    public JMenuItem[] getHelpPrimaryMenus() {
        return null;
    }

    public JMenuItem[] getHelpTestSuiteMenus() {
        return null;
    }

    public JMenuItem[] getHelpAboutMenus() {
        return null;
    }

    /**
     * Get actions for any items to appear in the desktop Tasks menu.
     * @return an array of Actions to appear on the Tasks menu, or null
     * if none are required.
     * @deprecated There is no tasks menu anymore.
     */
    public Action[] getTaskMenuActions() {
        return null;
    }

    /**
     * Get actions to open any windows for this tool.
     * @return an array of Actions that open windows for this tool, or null
     * if none are required.
     * @deprecated
     */
    public Action[] getWindowOpenMenuActions() {
        return null;
    }

    //----------------------------------------------------------------------------

    /**
     * Start a default instance of a tool.
     * @return the tool that was started
     */
    public abstract Tool startTool();

    /**
     * Restore a tool from previously saved information.
     * @param m a map containing the previously saved information
     * @return the tool that was started
     * @throws ToolManager.Fault if there is a problem restoring the tool
     */
    public abstract Tool restoreTool(Map m) throws Fault;

    //----------------------------------------------------------------------------

    /**
     * Create an icon from a resource specified in the standard resource bundle
     * for this tool manager.
     * @param key the base name for the resource specifying the image file for the
     *   icon. The actual name of the resource is formed as follows:
     *   <i>key</i> "<code>.icon</code>"
     * @return an icon containing the specified image
     */
    protected Icon createIcon(String key) {
        String r = i18n.getString(key + ".icon");
        URL url = ToolManager.class.getResource(r);
        if (url == null)
            throw new MissingResourceException(r, getClass().getName(), r);
        return new ImageIcon(url);
    }

    /**
     * The standard resource bundle for this tool manager, defined in the
     * file <code>i18n.properties</code> in the same package as the tool manager.
     */
    protected final I18NResourceBundle i18n;

    //----------------------------------------------------------------------------

    private final Desktop desktop;

    private static final I18NResourceBundle localI18N =
        I18NResourceBundle.getBundleForClass(ToolManager.class);
}
