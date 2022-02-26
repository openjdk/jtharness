/*
 * $Id$
 *
 * Copyright (c) 1996, 2022, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Helper methods related to data formatting.
 */
public class FormattingUtils {

    /**
     * Converts the given duration in seconds to a more readable string representation:
     * for example "2 days 1 minute 4 seconds".
     * This method doesn't generates weeks, months or years, only days
     * which is expected to be sufficient for printing total time taken by even a long test run.
     *
     * @param durationSeconds the seconds to convert to human-readable form
     * @return formatted representation, i.e. "1 hour 53 seconds"
     */
    public static String formattedDuration(long durationSeconds) {

        if (durationSeconds < 1) {
            return "0 seconds";
        }
        String result = "";
        long remaining_seconds = durationSeconds;
        // have to map duration to names to have durations sorted (TreeMap maintains order for keys)
        TreeMap<Long, String> units = new TreeMap<Long, String>(Comparator.reverseOrder()) {{
            put(1L, "second");
            put(60L, "minute");
            put(60L * 60L, "hour");
            put(60L * 60L * 24L, "day");
        }};
        for (Map.Entry<Long, String> entry : units.entrySet()) {
            long amount = remaining_seconds / entry.getKey();
            if (amount > 0 ) {
                remaining_seconds = remaining_seconds % entry.getKey();
                result += " " + amount + " " + entry.getValue();
                if (amount > 1) {
                    result += "s";
                }
            }
        }
        return result.trim();
    }
}
