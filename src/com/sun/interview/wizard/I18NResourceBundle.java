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
package com.sun.interview.wizard;

import java.awt.Color;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

/**
 * A class that lazily opens a package-specific resource bundle
 * containing localization data for a class.
 */
class I18NResourceBundle extends ResourceBundle
{
    static I18NResourceBundle getDefaultBundle() {
        if (defaultBundle == null)
            defaultBundle = getBundleForClass(I18NResourceBundle.class);

        return defaultBundle;
    }

    private static I18NResourceBundle defaultBundle;

    /**
     * Get a package-specific resource bundle for a class containing localization data.
     * The bundle is named i18n.properties in the same
     * package as the given class.
     * @param c the class for which to obtain the resource bundle
     * @return the appropriate resource bundle for the class
     */
    public static I18NResourceBundle getBundleForClass(Class c) {
        String cn = c.getName();
        int dot = cn.lastIndexOf('.');
        String rn = (dot == -1 ? "i18n" : cn.substring(0, dot) + ".i18n");
        boolean logging = (logClassPrefix == null ? false : cn.startsWith(logClassPrefix));
        return new I18NResourceBundle(rn, logging, c.getClassLoader());
    }

    /**
     * Get an entry from the resource bundle.
     * If the resource cannot be found, a message is printed to the console
     * and the result will be a string containing the method parameters.
     * @param key the name of the entry to be returned
     * @param arg an argument to be formatted into the result using
     * {@link java.text.MessageFormat#format}
     * @return the formatted string
     */
    public String getString(String key, Object arg) {
        return getString(key, new Object[] {arg});
    }

    /**
     * Get an entry from the resource bundle.
     * If the resource cannot be found, a message is printed to the console
     * and the result will be a string containing the method parameters.
     * @param key the name of the entry to be returned
     * @param args an array of arguments to be formatted into the result using
     * {@link java.text.MessageFormat#format}
     * @return the formatted string
     */
    public String getString(String key, Object[] args) {
        try {
            return MessageFormat.format(getString(key), args);
        }
        catch (MissingResourceException e) {
            showError(key, name);
            return key;
        }
    }

    /**
     * Get a color defined in the resource bundle.
     * If the resource cannot be found, a message is printed to the console
     * and the result will be a specified default.
     * @param key The base key for the resource. The actual key is this
     * value with ".clr" appended.
     * @param dflt an integer value used to construct the default result
     * if the specified resource cannot be found. The value is normally
     * most conveniently specified in hex, in the standard 0xRRGGBB format.
     * @return the color defined in the resource bundle, or a default
     * if the resource cannot be found
     */
    public Color getColor(String key, int dflt) {
        String value = getString(key + ".clr");
        try {
            if (value != null)
                return Color.decode(value);
        }
        catch (Exception e) {
            // ignore
        }
        return new Color(dflt);
    }

    /**
     * Get a color suitable for displaying short error messages.
     * The color is defined by the i18n.error.clr resource.
     * @return a color suitable for displaying short error messages
     */
    public Color getErrorColor() {
        return getColor("i18n.error", 0xff0000);
    }


    /**
     * Create a resource bundle for the given name.
     * The actual resource bundle will not be loaded until it is needed.
     * @arg name The name of the actual resource bundle to use.
     */
    private I18NResourceBundle(String name, boolean logging, ClassLoader cl) {
        this.name = name;
        this.logging = logging;
        this.classLoader = cl;
    }

    /**
     * A required internal method for ResourceBundle.
     * Load the actual resource bundle, if it has not yet been loaded,
     * then hand the request off to that bundle.
     * If the resource cannot be found, a message is printed to the console
     * and the result will be the original key.
     */
    protected Object handleGetObject(String key) throws MissingResourceException {
        if (logging)
            System.out.println("i18n: " + key);
        try {
            if (delegate == null)
                delegate = ResourceBundle.getBundle(name, Locale.getDefault(), classLoader);
            return delegate.getObject(key);
        }
        catch (MissingResourceException e) {
            showError(key, name);
            return key;
        }
    }

    /**
     * A required internal method for ResourceBundle.
     * Load the actual resource bundle, if it has not yet been loaded,
     * then hand the request off to that bundle.
     */
    public Enumeration getKeys() {
        if (delegate == null)
            delegate = ResourceBundle.getBundle(name, Locale.getDefault(), classLoader);
        return delegate.getKeys();
    }

    private void showError(String key, String name) {
        System.err.println("WARNING: missing resource: " + key + " for " + name);
    }

    private String name;
    private ResourceBundle delegate;
    private ClassLoader classLoader;
    private boolean logging;

    private static String logClassPrefix = System.getProperty("i18n.log");
}
