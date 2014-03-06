/*
 * $Id$
 *
 * Copyright (c) 2003, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Color;
import com.sun.javatest.Status;
import com.sun.javatest.util.I18NResourceBundle;

/**
 * Utility class to get general internationalization properties and
 * perform general transformations.
 */
public class I18NUtils {
    /**
     * Get the base color for a test status.
     * @param status the status for which the color is required: one of
     * {@link #PASSED}, {@link #FAILED}, {@link #ERROR}, {@link #NOT_RUN}
     * @return the base color for the specified test status
     */
    public static Color getStatusColor(int status) {
        return getColorForState(status);
    }

    /**
     * Get the color for a bar for a test status.
     * @param status the status for which the color is required: one of
     * {@link #PASSED}, {@link #FAILED}, {@link #ERROR}, {@link #NOT_RUN}
     * @return the color for a bar for the specified test status
     */
    public static Color getStatusBarColor(int status) {
        return lighter(getColorForState(status));
    }

    /**
     * Get the localized status string for a particular test status.
     * @param status the status for which the color is required: one of
     * {@link #PASSED}, {@link #FAILED}, {@link #ERROR}, {@link #NOT_RUN}
     * @return the color for a bar for the specified test status
     * @see com.sun.javatest.Status
     */
    public static String getStatusString(int status) {
        if (STATUS_STRINGS == null)
            loadStatusStrings();

        if (status < STATUS_STRINGS.length)
            return STATUS_STRINGS[status];
        else
            return i18n.getString("i18n.unknown");
    }

    /**
     * Get localized version of the message string.
     * This includes a localized version of the status (e.g. "Passed") and
     * the raw status message.
     * @param status The status object for format.  May not be null.
     * @return A formatted, internationalized string representation of the
     *         status object (state and reason).
     * @see com.sun.javatest.Status#getReason
     */
    public static String getStatusMessage(Status status) {
        if (STATUS_STRINGS == null)
            loadStatusStrings();

        return STATUS_STRINGS[status.getType()] + " " +
                status.getReason();
    }

    static Color getColorForState(int state) {
        switch (state) {
        case PASSED:
            return passedColor;

        case FAILED:
            return failedColor;

        case ERROR:
            return errorColor;

        case NOT_RUN:
            return notRunColor;

        case FILTERED_OUT:
            return filteredOutColor;

        default:
            return null;
        }
    }

    private static void loadStatusStrings() {
        synchronized (I18NUtils.class) {
            if (STATUS_STRINGS == null) {
                STATUS_STRINGS = new String[NUM_STATES];

                for (int i = 0; i < NUM_STATES; i++)
                    STATUS_STRINGS[i] = i18n.getString("i18n.status" + i);
            }
        }
    }

    /**
     * Create a color derived from the given color, but lighter.
     * This is currently done by decreasing it's saturation and brightness.
     * @param c The color to lighten.
     * @return The derived color.
     */
    public static Color lighter(Color c) {
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        hsb[1] = hsb[1] * 0.75f;                // reduce saturation by 25%
        hsb[2] = Math.min(hsb[2] * 1.1f, 1.0f); // increase brightness by 10%
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
    }

    private static Color getI18NColor(String key, int deflt) {
        String value = i18n.getString(key + ".clr");
        try {
            if (value != null)
                return Color.decode(value);
        }
        catch (Exception e) {
            // ignore
        }
        return new Color(deflt);
    }

    /**
     * This is for internal use to read startup color prefs.
     * Will return the color specifed by the color's system property,
     * previous preference, or as defined in the resource bundle.  The
     * backup value will be used if a value cannot be located in any of
     * those three locations.  If read from system properties, the pref
     * key is prefixed by <code>javatest.</code>.
     * @param pref The system property and preferences key for the color value.
     * @param bundle The resource bundle key for the colors value.
     * @param syncPrefs Write system property value to the preferences if
     *        present.
     * @param backup The color to use if no color settings are available.
     */
    private static Color getPreferredColor(String pref, String bundle,
                                           int backup, boolean syncPrefs) {
        // read colors from system props and write into prefs
        // else read from prefs
        // else use defaults from the bundle
        Color result = null;

        // read from system properties
        result = Color.getColor("javatest." + pref);
        if (result != null) {
            if (syncPrefs) {
                Preferences prefs = Preferences.access();
                prefs.setPreference(pref, Integer.toString(result.getRGB()) );
            }

            return result;
        }

        // read from existing prefs
        Preferences prefs = Preferences.access();
        String val = prefs.getPreference(pref);

        if (val != null) {
            try {
                result = Color.decode(val);
            }
            catch (NumberFormatException e) {
                // XXX log error somehow?
                result = null;
            }   // catch

            // success, otherwise fall through
            if (result != null)
                return result;
        }

        result = getI18NColor(bundle, backup);
        return result;
    }

    private static I18NResourceBundle i18n =
        I18NResourceBundle.getBundleForClass(I18NUtils.class);
    private static Color passedColor;
    private static Color failedColor;
    private static Color errorColor;
    private static Color notRunColor;
    private static Color filteredOutColor;

    private static final String PASS_COLOR_PREF = "color.passed";
    private static final String FAIL_COLOR_PREF = "color.failed";
    private static final String ERR_COLOR_PREF = "color.error";
    private static final String NOTRUN_COLOR_PREF = "color.notrun";
    private static final String FILTERED_COLOR_PREF = "color.filter";

    static {
        passedColor = getPreferredColor(PASS_COLOR_PREF, "i18n.passed",
                                        0x00ff00, true);
        failedColor = getPreferredColor(FAIL_COLOR_PREF, "i18n.failed",
                                        0xff0000, true);
        errorColor = getPreferredColor(ERR_COLOR_PREF, "i18n.error",
                                        0x3f3fff, true);
        notRunColor = getPreferredColor(NOTRUN_COLOR_PREF, "i18n.notRun",
                                        0xffffff, true);
        filteredOutColor = getPreferredColor(FILTERED_COLOR_PREF,
                                        "i18n.filtered",
                                        0xa999999, true);
    }

    /**
     * A convenience redefinition of {@link Status#PASSED Status.PASSED}.
     */
    public static final int PASSED = Status.PASSED;

    /**
     * A convenience redefinition of {@link Status#FAILED Status.FAILED}.
     */
    public static final int FAILED = Status.FAILED;

    /**
     * A convenience redefinition of {@link Status#ERROR Status.ERROR}.
     */
    public static final int ERROR  = Status.ERROR;

    /**
     * A convenience redefinition of {@link Status#NOT_RUN Status.NOT_RUN}.
     */
    public static final int NOT_RUN = Status.NOT_RUN;

    /**
     * A constant indicating that an icon should be represented as "filtered out".
     */
    public static final int FILTERED_OUT = Status.NUM_STATES;

    /**
     * A constant indicating the number of different value "state" values.
     */
    public static final int NUM_STATES = FILTERED_OUT + 1;

    /**
     * Localized status strings.
     */
    private static String[] STATUS_STRINGS;
}
