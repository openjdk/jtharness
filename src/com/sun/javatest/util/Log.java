/*
 * $Id$
 *
 * Copyright (c) 1996, 2020, Oracle and/or its affiliates. All rights reserved.
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Provides simple console logging using java.util.logging API.
 * Logging level is defined via "jtharness.logging.level" system property.
 */
public class Log {
    public static final String JTHARNESS_LOGGING_LEVEL_SYS_PROP = "jtharness.logging.level";
    private static Logger LOG;
    private static Level CURRENT_LEVEL;
    private static final String DATE_TIME_FORMAT = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss ").format(new Date());

    static {
        resetLoggingSettings(Level.parse(System.getProperty(JTHARNESS_LOGGING_LEVEL_SYS_PROP, Level.INFO.getName())));
    }

    public static void resetLoggingSettings(Level newLevel) {
        LOG = Logger.getLogger("jtharness");
        CURRENT_LEVEL = newLevel;
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter() {
            @Override
            public synchronized String formatMessage(LogRecord lr) {
                return createMessage(lr);
            }

            @Override
            public synchronized String format(LogRecord lr) {
                return createMessage(lr);
            }

            private String createMessage(LogRecord lr) {
                return DATE_TIME_FORMAT + String.format("[%1$s] %2$s%n", lr.getLevel().getName(), lr.getMessage());
            }
        });
        handler.setLevel(CURRENT_LEVEL);
        // need not to have any extra console printing other that what our custom handler does
        for (Handler defaultHandler : LOG.getHandlers()) {
            LOG.removeHandler(defaultHandler);
        }

        LOG.setUseParentHandlers(false);
        LOG.setLevel(CURRENT_LEVEL);
        LOG.addHandler(handler);
    }

    public static void info(String message) {
        // returning early to avoid unnecessary operations with stacktrace
        if (!LOG.isLoggable(CURRENT_LEVEL)) {
            return;
        }
        LOG.log(Level.INFO, callerPrefix() + message);
    }

    public static void finest(String message) {
        // returning early to avoid unnecessary operations with stacktrace
        if (!LOG.isLoggable(CURRENT_LEVEL)) {
            return;
        }
        LOG.log(Level.FINEST, callerPrefix() + message);
    }

    public static void finer(String message) {
        // returning early to avoid unnecessary operations with stacktrace
        if (!LOG.isLoggable(CURRENT_LEVEL)) {
            return;
        }
        LOG.log(Level.FINER, callerPrefix() + message);
    }

    public static void fine(String message) {
        // returning early to avoid unnecessary operations with stacktrace
        if (!LOG.isLoggable(CURRENT_LEVEL)) {
            return;
        }
        LOG.log(Level.FINE, callerPrefix() + message);
    }

    public static void warning(String message) {
        // returning early to avoid unnecessary operations with stacktrace
        if (!LOG.isLoggable(CURRENT_LEVEL)) {
            return;
        }
        LOG.log(Level.WARNING, callerPrefix() + message);
    }

    /**
     * Corresponds to Level.SEVERE level
     */
    public static void error(String message) {
        // returning early to avoid unnecessary operations with stacktrace
        if (!LOG.isLoggable(CURRENT_LEVEL)) {
            return;
        }
        LOG.log(Level.SEVERE, callerPrefix() + message);
    }

    /**
     * Creates string prefix containing caller's class simple name and caller method names
     */
    private static String callerPrefix() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String result;
        if (stackTrace.length >= 4) {
            StackTraceElement stackTraceElement = stackTrace[3];
            String callerClassName = stackTraceElement.getClassName();
            String callingMethodName = stackTraceElement.getMethodName();
            int lastDotIndex = callerClassName.lastIndexOf('.');
            String simpleClassName = lastDotIndex < 0 ? callerClassName : callerClassName.substring(lastDotIndex + 1);
            return "(" + simpleClassName + "." + callingMethodName + ") ";
        } else {
            result = "";
        }
        return result;
    }

}
