/*
 * $Id$
 *
 * Copyright (c) 2002, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Map;

import javax.swing.JComponent;

import com.sun.javatest.ObservableTestFilter;
import com.sun.javatest.util.I18NResourceBundle;

/**
 * This class adds the capability to have a filter which is
 * reconfigurable through the use of a GUI panel.  By definition, there
 * can be multiple instances of each <tt>ConfigurableTestFilter</tt> in
 * in the system at any one time.
 *
 * <p>
 * It is highly recommended that the GUI panel used to configure
 * this filter be a single panel, and not a complex tabbed or otherwise
 * "switchable" panel.
 *
 * <p>
 * Subclasses of this class should implement the abstract methods here and
 * those in ObservableTestFilter.
 *
 * @see com.sun.javatest.ObservableTestFilter
 */
abstract class ConfigurableTestFilter extends ObservableTestFilter {
    /**
     * Nameless filters are not allowed.
     */
    private ConfigurableTestFilter() {
    }

    /**
     * Utility constructor for subclasses.
     * @param name The name to give this filter instance.  Must never be null.
     * @param e The ExecTool that owns this instance of the filter.  Should never be null.
     * @throws IllegalArgumentException If the either parameter is null.
     */
    protected ConfigurableTestFilter(String name, ExecModel e) {
        if (name == null)
            throw new IllegalArgumentException(i18n.getString("ctf.nullName"));
        if (e == null)
            throw new IllegalArgumentException(i18n.getString("ctf.nullExec"));

        instanceName = name;
        execModel = e;
    }

    /**
     * Utility constructor for subclasses.
     * @param map The map containing previous filter settings.
     * @param e The ExecTool that owns this instance of the filter.
     * @throws IllegalStateException If the instance name is not present in the map or
     *         the exec model argument is null.
     */
    protected ConfigurableTestFilter(Map map, ExecModel e) {
        if (e == null)
            throw new IllegalArgumentException(i18n.getString("ctf.nullExec"));

        execModel = e;
        load(map);

    }

    /**
     * Create a new instance of this filter with the same associated exec model.
     * A default instance name will be supplied.
     */
    abstract ConfigurableTestFilter cloneInstance();

    /**
     * Overridden to create a new instance.
     * Settings from the original instance may not necessarily be copied.
     */
    public Object clone() {
        return cloneInstance();
    }

    /**
     * Get the basic (static) name for this filter.
     * This is different than <tt>getName()</tt> which returns a
     * customizable name.
     * @return The basic name for this filter.  Will never be null.
     */
    abstract String getBaseName();

    /**
     * Set the instance name.  This value should never be null.
     * Short meaningful names are recommended.
     *
     * @param text The name.
     * @throws IllegalArgumentException If the name parameter is null.
     * @see #getName()
     */
    void setInstanceName(String text) {
        if (text != null)
            instanceName = text;
        else
            throw new IllegalArgumentException(i18n.getString("ctf.nullName"));
    }

    /**
     * Load the state of this filter from a map.
     * This default implementation just loads the instance name.
     * @param map The map of strings to load from.
     * @return True if loading was successful and the filter is usable,
     *         false if the operation failed.
     * @throws IllegalStateException If the instance name is not present in the map.
     */
    boolean load(Map map) {
        instanceName = (String)(map.get(INSTANCE_KEY));

        if (instanceName == null)
            throw new IllegalStateException(i18n.getString("ctf.mapNoName"));

        return true;
    }

    /**
     * Save the state of this filter to a map.
     * This default implementation just saves the instance name.  You may use
     * any key strings except those starting with the string "meta".
     * @param map The map to save to.
     * @return True if saving was successful, false if the operation failed.
     */
    boolean save(Map map) {
        map.put(INSTANCE_KEY, instanceName);

        return true;
    }

    /**
     * This method should provide a component that the user can
     * use to manipulate the settings of a concrete configurable
     * filter instance.
     * <p>NOTE: This method will always be called on the event
     *   dispatch thread.
     * @return A component that the user can use to reconfigure the
     *         filter.
     * @see #commitEditorSettings
     * @see #resetEditorSettings
     */
    abstract JComponent getEditorPane();

    /**
     * Commit the settings which are currently in the GUI editor.
     * Under successful conditions, the GUI settings are saved into
     * the state of this object, saved to disk if necessary, and
     * a settings changes is emitted to observers.  If a commit
     * operation fails, it is expected that the logical outer container
     * around the editor will force the user to cancel or correct
     * the settings in the GUI.
     * <p>NOTE: This method will always be called on the event
     *   dispatch thread.
     * @return null if committing was successful, an internationalized
     *         string helpful to the
     *         user is returned if the current settings are unacceptable.
     * @see com.sun.javatest.ObservableTestFilter#notifyUpdated
     */
    abstract String commitEditorSettings();

    /**
     * Reset the editor to the settings which reflect the last
     * committed state.  This method will typically be called if the
     * user explicitly asks to restore the settings, or the user cancels
     * the editing operation.  No filter observer traffic is generated.
     * <p>NOTE: This method will always be called on the event
     *   dispatch thread.
     */
    abstract void resetEditorSettings();

    /**
     * Does the editor have any uncommitted changes?
     */
    abstract boolean isEditorChanged();

    protected String instanceName;
    protected ExecModel execModel;
    protected static final String INSTANCE_KEY = "instanceName";
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(ConfigurableTestFilter.class);
}

