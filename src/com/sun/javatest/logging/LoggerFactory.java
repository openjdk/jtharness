/*
 * $Id$
 *
 * Copyright (c) 2006, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.logging;

import java.util.logging.Level;
import com.sun.javatest.util.I18NResourceBundle;

public class LoggerFactory {

    public static String getLocalizedLevelName(Level level) {
        if (level.intValue() < Level.INFO.intValue())
            return i18n.getString("logger.level.debug");

        if (level.intValue() < Level.WARNING.intValue())
            return i18n.getString("logger.level.monitoring");

        if (level.intValue() < Level.SEVERE.intValue())
            return i18n.getString("logger.level.warning");

        return i18n.getString("logger.level.critical");
    }

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(LoggerFactory.class);

    public static final String LOGFILE_NAME = i18n.getString("logger.logfile.name");
    public static final String LOGFILE_EXTENSION = i18n.getString("logger.logfile.ext");

    // convenience levels for JT logging
    public static final Level DEBUG = Level.CONFIG;
    public static final Level MONITORING = Level.INFO;
    public static final Level WARNING = Level.WARNING;
    public static final Level CRITICAL = Level.SEVERE;
}

