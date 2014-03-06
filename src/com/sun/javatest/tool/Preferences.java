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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.help.HelpBroker;
import javax.swing.JFrame;
import com.sun.javatest.util.DynamicArray;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.SortedProperties;

/**
 * A class which provides a collection of user preferences,
 * and a GUI interface to edit them. Preferences are represented
 * as named string values.
 * For now, the data is stored in a standard Java properties file
 * in the user's home directory; eventually, it will be converted to
 * use the J2SE support for user preferences.
 */
public class Preferences
{
    /**
     * An observer interface for use by those that wishing to monitor changes
     * to user preferences.
     */
    public static interface Observer {
        /**
         * A preference has been changed.
         * @param name the name of the preference that has been changed
         * @param newValue the new value for the named preference
         */
        public void updated(String name, String newValue);
    }

    /**
     * Access the single Preferences object.
     * @return the single Preferences object
     */
    public static Preferences access() {
        if (theOne == null)
            theOne = new Preferences(getDefaultPrefsFile());
        return theOne;
    }

    static File getPrefsDir() {
        File userDir = new File(System.getProperty("user.home"));
        String prefDir = System.getProperty("javatest.preferences.dir", ".javatest");
        return new File(userDir, prefDir);
    }

    private static File getDefaultPrefsFile() {
        String s = System.getProperty("javatest.preferences.file");
        if (s == null) {
            File jtDir = getPrefsDir();
            return new File(jtDir, "preferences");
        }
        else if (!s.equals("NONE"))
            return new File(s);
        else
            return null;
    }

    private static Preferences theOne;

    private Preferences(File file) {
        prefsFile = file;
        isUpToDate = true;
        Properties p = new SortedProperties();

        try {
            if (prefsFile != null) {
                InputStream in = new BufferedInputStream(new FileInputStream(prefsFile));
                p.load(in);
                in.close();
        fileModifiedTime = prefsFile.lastModified();
            }
        }
        catch (FileNotFoundException ignore) {
        }
        catch (IOException ignore) {
            // ??
        }
        props = p;
    }

    /**
     * Save the current set of user preferences.
     * For now, the data is stored in a standard Java properties file
     * in the user's home directory; eventually, it will be converted to
     * use the J2SE support for user preferences.
     */
    public synchronized void save() {
        if (prefsFile != null && (!isUpToDate || fileModifiedTime != prefsFile.lastModified())) {
            try {
                File parentDir = new File(prefsFile.getParent());
                if (!parentDir.exists())
                    parentDir.mkdirs();
                OutputStream out = new BufferedOutputStream(new FileOutputStream(prefsFile));
                props.store(out, "JT Harness Preferences");
                out.close();
                isUpToDate = true;
                fileModifiedTime = prefsFile.lastModified();
            }
            catch (IOException e) {
                System.err.println(i18n.getString("prefs.cannotSave", e));
            }
        }
    }

    /**
     * Get a named preference value.
     * @param name the name of the desired preference
     * @return the value of the named preference, or null if no such preference found
     * @see #setPreference
     */
    public String getPreference(String name) {
        return getPreference(name, null);
    }

    /**
     * Get a named preference value, using a default if the named preference
     * is not found.
     * @param name the name of the desired preference
     * @param defaultValue the default value to be returned if no such preference
     * is found
     * @return the value of the named preference, or the default value if no such preference found
     * @see #setPreference
     */
    public String getPreference(String name, String defaultValue) {
        String v = (String) (props.get(name));
        if (v == null)
            v = defaultValue;
        return v;
    }

    /**
     * Set the value of a named preference. Any interested observers will
     * be notified.
     * @param name the name of the preference to be set
     * @param newValue the new value for the preference
     * @see #getPreference
     */
    public void setPreference(String name, String newValue) {
        isUpToDate = false;
        props.put(name, newValue);
        // notify observers
        for (Enumeration e = observers.keys(); e.hasMoreElements(); ) {
            String prefix = (String)(e.nextElement());
            if (name.startsWith(prefix)) {
                Observer[] obs = (Observer[])observers.get(prefix);
                for (int i = 0; i < obs.length; i++) {
                    obs[i].updated(name, newValue);
                }
            }
        }
    }

    /**
     * Add an observer to be notified of changes to all preferences
     * whose name begins with a given prefix. This allows an observer
     * to monitor a single preference or a group of preferences.
     * @param prefix the prefix to determine which preferences will
     *          be observed
     * @param o the observer to be added
     * @see #removeObserver
     */
    public void addObserver(String prefix, Observer o) {
        // very crude observer storage for now; results in o(n) per preference
        // cost when updating preferences
        Observer[] obs = (Observer[])observers.get(prefix);
        if (obs == null)
            obs = new Observer[] { o };
        else
            obs = (Observer[])DynamicArray.append(obs, o);
        observers.put(prefix, obs);
    }

    /**
     * Add an observer to be notified of changes to all preferences
     * whose name begins with any of a set of given prefixes.
     * This allows an observer to monitor a single preference
     * or a group of preferences.
     * @param prefixes the prefix to determine which preferences will
     *          be observed
     * @param o the observer to be added
     * @see #removeObserver
     */
    public void addObserver(String[] prefixes, Observer o) {
        for (int i = 0; i < prefixes.length; i++) {
            addObserver(prefixes[i], o);
        }
    }

    /**
     * Remove an observer which was previously registered to be
     * notified of changes to all preferences whose name begins
     * with a given prefix. The prefix must exactly match the
     * prefix with which it was previously registered.
     * @param prefix the prefix to identify which instance of the
     * observer to be removed
     * @param o the observer to be removed
     * @see #addObserver
     */
    public void removeObserver(String prefix, Observer o) {
        Observer[] obs = (Observer[])observers.get(prefix);
        if (obs != null) {
            obs = (Observer[])DynamicArray.remove(obs, o);
            observers.put(prefix, obs);
        }
    }

    /**
     * Remove an observer which was previously registered to be
     * notified of changes to all preferences whose name begins
     * with any of a set of prefixed. Each prefix must exactly
     * match one with which the observer was previously registered.
     * @param prefixes the prefix to identify which instances of the
     * observer to be removed
     * @param o the observer to be removed
     * @see #addObserver
     */
    public void removeObserver(String[] prefixes, Observer o) {
        for (int i = 0; i < prefixes.length; i++) {
            removeObserver(prefixes[i], o);
        }
    }

    /**
     * Access method for props to be used by PreferencesPane
     * @return props
     */
    Properties getProperties() {
        return props;
    }

    private File prefsFile;
    private Properties props;
    private Hashtable observers = new Hashtable();
    private boolean isUpToDate;
    private long fileModifiedTime;

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(Preferences.class);
}
