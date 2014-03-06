/*
 * $Id$
 *
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.util;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Access to debugging settings which have been activated.
 * Indexing is by string matching; the key is usually but not always a fully qualified
 * classname.
 *
 * <p>
 * The system properties have three classes of "value" data.  The string may be
 * equal to the string "true" (case insensitive), it may be parsable into an
 * integer (by Integer.parseInt()), or it may be any other string.  In the
 * latter case where it is not TRUE and is not an int, it will be interpreted
 * as a zero if a integer is requested, and FALSE if a boolean is requested.
 *
 * <p>
 * The following definitions define how integer and boolean settings are
 * interpreted:
 * <dl>
 * <dt>A lookup that results in a valid integer:
 * <dd>Will return that integer for any of the getInt() methods
 * <dd>Will return false for any getBoolean() method if the integer is less than or equal to zero
 * <dd>Will return true for and getBoolean() method if the integer is greater than zero
 *
 * <dt>A lookup that results in "true" (any case combination):
 * <dd>Will return one for any of the getInt() methods
 * <dd>Will return true for any of the getBoolean() methods
 *
 * <dt>A lookup that results in neither of the above cases (not "true", nor an integer):
 * <dd>Will return zero for any of the getInt() methods
 * <dd>Will return false for any of the getBoolean() methods
 * </dl>
 *
 * @see java.lang.Integer#parseInt
 */

public class Debug {
    private Debug() {
        // no instances
    }

    /**
     * Print a debugging message.
     * @param s the message to be printed.
     */
    public static void print(String s) {
        out.print(s);
        out.flush();
    }

    /**
     * Print a debugging message and end the line.
     * @param s the message to be printed.
     */
    public static void println(String s) {
        out.println(s);
        out.flush();
    }

    /**
     * Check if overall debugging is enabled.
     * If it is not enabled, methods will return null, false, and zero,
     * as appropriate for the type.
     * @return true if debugging is enabled, and false otherwise
     */
    public static boolean isEnabled() {
        return masterSwitch;
    }

    /**
     * Do a raw lookup of a key, including matching of wildcard settings.
     * Is the given string matched by any of the debug system properties.
     * The matching is done according to import-style semantics, using the dot
     * separator.  In addition wildcards from the system properties are allowed
     * <b>at the end</b> of the key specification.  Unlike imports, the wildcard can
     * match inner class names.
     *
     * @param key The name of the setting to be returned
     * @return The setting, or null if not found.  Null is returned if debugging is
     *  disabled entirely.
     */
    public static String getSetting(String key) {
        // important because there may be uninitialized objects
        if (masterSwitch == false)
            return null;

        String match = dProps.getProperty(key);

        if (match != null)
            return match;
        else {
            match = wildProps.search(key);
            // may be null
            return match;
        }
    }

    /**
     * Find out the debugging setting for class c.
     * Lookup is done by looking for fully qualified class name.
     *
     * @param c Class whose name should be used to lookup the setting, null results in
     *    a return value of zero.
     * @return the debugging setting for the specified class
     */
    public static boolean getBoolean(Class c) {
        init(false);

        if (!masterSwitch)
            return false;

        String key = getName(c);
        String setting = getSetting(key);
        boolean state = convertToBool(setting);

        return state;
    }

    /**
     * Find out the debugging setting for class c.
     * Lookup is done by looking for fully qualified class name with a dot and the
     * given suffix appended.
     *
     * @param c Class whose name should be used to lookup the setting, null results in
     *    a return value of zero.
     * @param suffix String to append to the classname, null will result in a lookup
     *    of just the classname.
     * @return the debugging setting for the specified class
     */
    public static boolean getBoolean(Class c, String suffix) {
        init(false);

        if (!masterSwitch)
            return false;

        StringBuffer buf = new StringBuffer(getName(c));
        if (suffix != null && suffix.length() != 0) {
            buf.append(Debug.SEPARATOR);
            buf.append(suffix);
        }

        String key = buf.toString();
        String setting = getSetting(key);
        boolean state = convertToBool(setting);

        return state;
    }

    /**
     * Get a named debugging setting.
     * @param s the name of the desired debugging setting
     * @return the value of the debugging setting
     */
    public static boolean getBoolean(String s) {
        init(false);

        if (!masterSwitch || s == null)
            return false;

        String setting = getSetting(s);
        boolean state = convertToBool(setting);

        return state;
    }

    /**
     * Find out the debugging setting for class c.
     * Lookup is done by looking for fully qualified class name.
     *
     * @param c the class whose name should be used to lookup the setting
     * @return the debugging setting for the given class, or 0 if no class
     *    was specified
     */
    public static int getInt(Class c) {
        init(false);

        if (!masterSwitch || c == null)
            return 0;

        String key = getName(c);
        String setting = getSetting(key);
        int state = convertToInt(setting);

        return state;
    }

    /**
     * Find out the debugging setting for class c.
     * Lookup is done by looking for fully qualified class name with a dot and the
     * given suffix appended.
     *
     * @param c a class whose name should be used to lookup the setting;
     *    null results in a return value of zero.
     * @param suffix a string to append to the classname;
     *    null will result in a lookup of just the classname.
     * @return the debugging setting for the class
     */
    public static int getInt(Class c, String suffix) {
        init(false);

        if (!masterSwitch || c == null)
            return 0;

        StringBuffer buf = new StringBuffer(getName(c));
        if (suffix != null && suffix.length() != 0) {
            buf.append(Debug.SEPARATOR);
            buf.append(suffix);
        }

        String key = buf.toString();
        String setting = getSetting(key);
        int state = convertToInt(setting);

        return state;
    }

    /**
     * Get a named debugging setting.
     * @param s the name of the desired debugging setting
     * @return the value of the debugging setting
     */
    public static int getInt(String s) {
        init(false);

        if (!masterSwitch)
            return 0;

        String setting = getSetting(s);
        return convertToInt(setting);

        /*
        String setting = dProps.getProperty(s);
        if (setting == null)
            return 0;
        else if (setting.equalsIgnoreCase("true"))
            return 1;
        else {
            try {
                return Integer.parseInt(setting);
            }
            catch (NumberFormatException e) {
                e.printStackTrace(out);
                return 0;
            }
        }
        */
    }

    /**
     * Get the debugging stream, used for writing debug messages.
     * @return the debugging stream, used to write debug messages
     */
    public static PrintWriter getWriter() {
        return out;
    }

    /**
     * Set properties containing debugging settings.
     * This is required if the security manager does not allow access to
     * the system properties.
     * @param props A table of properties containing debugging settings
     */
    public static void setProperties(Properties props) {
        givenProps = props;
    }

    /**
     * Initialize (or re-initialize) debugging support.
     * @param force Force reprocessing of System properties.
     */
    public synchronized static void init(boolean force) {
        if (dProps != null && force != true)
            return;

        Properties props;

        try {
            props = System.getProperties();
        }
        catch (SecurityException e) {
            // this is the backup source of settings
            props = givenProps;
        }

        if (props == null) {
            // we're stuck, must disable debugging
            masterSwitch = false;
            return;
        }

        Enumeration keys = props.propertyNames();

        dProps = new Properties();
        wildProps = new WildcardProperties();

        while (keys.hasMoreElements()) {
            String key = (String)(keys.nextElement());
            if (key.startsWith(DEBUG_PREFIX)) {
                // this should be a setProperty() in JDK 1.2+
                if (key.equalsIgnoreCase(MASTER_KEY)) {
                    String val = props.getProperty(key);
                    // this will disable all debugging, all methods will return zero or false
                    if (val.equalsIgnoreCase(TRUE_STRING))
                        masterSwitch = false;
                }
                else if (key.endsWith(WILD_SUFFIX)) {
                    wildProps.put(key.substring(DEBUG_PREFIX.length()), props.getProperty(key));
                }
                else
                    dProps.put(key.substring(DEBUG_PREFIX.length()), props.getProperty(key));
            }
        }   // while
    }

    // -------- Private ---------

    /**
     * Convert a class object into a string appropriate for lookup.
     *
     * @param c Must not be null.
     */
    private static String getName(Class c) {
        // null checking skipped

        String name = c.getName();

        // compensate for inner classes for which getName() return the internal name
        name = name.replace('$', '.');

        return name;
    }

    /*
     * Take the user setting and interpret it.
     * If the setting corresponds to the true string, true is returned.  If it corresponds to
     * an integer greater than zero, true is returned.  Otherwise, false is returned.
     */
    private static boolean convertToBool(String setting) {
        if (setting == null)
            return false;

        if (setting.equalsIgnoreCase(TRUE_STRING))
            return true;
        else {
            try {
                int num = Integer.parseInt(setting);
                if (num > 0)
                    return true;
                else
                    return false;
            }
            catch (NumberFormatException e) {
                // not "true", not an integer
                return false;
            }
        }
    }

    /**
     * Take the user setting and interpret it.
     * If it is an integer, that is returned.  If it is true, 1 is returned.  If it is
     * anything else, zero is returned.
     */
    private static int convertToInt(String setting) {
        if (setting == null)
            return 0;

        if (setting.equalsIgnoreCase(TRUE_STRING))
            return 1;
        else {
            try {
                int num = Integer.parseInt(setting);
                return num;
            }
            catch (NumberFormatException e) {
                // not "true", not an integer
                return 0;
            }
        }
    }

    // ---- activate as needed ----
    private static void setEnabled(String debugKey, boolean state) {
    }

    private static void setEnabled(String debugKey, int level) {
    }

    private static void setOutput(PrintWriter w) {
        out = w;
    }

    private static final String SEPARATOR = ".";
    private static final String DEBUG_PREFIX = "debug" + SEPARATOR;
    private static final String WILD_SUFFIX = "*";
    private static final String TRUE_STRING = "true";
    private static final String MASTER_KEY = "debug.disable";

    private static Properties givenProps;           // settings used if System won't give
    private static Properties dProps;               // explicit props
    private static WildcardProperties wildProps;    // props which contain wildcards

    private static PrintWriter out;
    private static boolean defaultBool = false;
    private static int defaultInt = 0;

    /**
     * True if debugging is enabled, and false otherwise.
     */
    private static boolean masterSwitch = true;

    static {
        out = new PrintWriter(System.err);
    }

    /**
     * Wildcards are compared against the given key minus one field.
     * So a search(<tt>"foo.bar.baz"</tt>) does a search for <tt>"foo.bar.*"</tt>.
     * <tt>"foo.*"</tt> will not be a match.
     */
    private static class WildcardProperties extends Properties {
        /**
         * This is the only method in this class that accounts for wildcards.
         */
        public String search(String key) {
            String lowerKey = key.toLowerCase();
            String target = trimTarget(lowerKey);

            Enumeration keys = propertyNames();
            while (keys.hasMoreElements()) {
                String k = (String)(keys.nextElement());
                String lowerK = k.toLowerCase();

                if (lowerK.startsWith(target)) {
                    // should strip target string and dot
                    String tail = lowerK.substring(target.length());
                    String head = lowerK.substring(0, target.length());
                    if (tail.equals(wildTail) || head.equals(lowerKey))
                        return getProperty(k);
                }
            }   // while

            return null;
        }

        /**
         * Remove the last element of the requested key to see if a wildcard fits into
         * that position.  Wildcards can only be valid up one level, so
         * foo.* cannot match foo.bar.baz, but will match foo.bar.  This method
         * turns foo.bar into foo so it the search method can use "foo".
         */
        String trimTarget(String t) {
            int index = t.lastIndexOf(Debug.SEPARATOR);
            if (index != -1)
                return t.substring(0, index);
            else
                return t;
        }

        static final String wildTail = Debug.SEPARATOR + Debug.WILD_SUFFIX;
    }
}

