/*
 * $Id$
 *
 * Copyright (c) 2004, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * An object providing support for writing log messages to a file.
 */
public class LogFile
{

    /**
     * Create a log file object to which log messages can be written.
     * The messages will be written to System.err.
     */
    public LogFile() {
        this(new OutputStreamWriter(System.err));
    }

    /**
     * Create a log file object to which log messages can be written.
     * @param out the stream to which to write the messages.
     */
    public LogFile(Writer out) {
        if (out == null)
            throw new NullPointerException();

        this.out = out;
    }

    /**
     * Create a log file object to which log messages can be written.
     * @param file the file to which to write the messages.
     */
    public LogFile(File file) {
        if (file == null)
            throw new NullPointerException();

        this.file = file;
    }

    /**
     * Print a text message to the logfile.
     * A single line of text which is as short as possible is highly
     * recommended for readability purposes.
     *
     * @param i18n a resource bundle containing the localized messages
     * @param key a key into the resource bundle for the required message
     */
    public void log(I18NResourceBundle i18n, String key) {
        log(i18n.getString(key), null);
    }

    /**
     * Print a text message to the workdir logfile.
     * A single line of text which is as short as possible is highly
     * recommended for readability purposes.
     *
     * @param i18n a resource bundle containing the localized messages
     * @param key a key into the resource bundle for the required message
     * @param arg An argument to be formatted into the specified message.
     *          If this is a <code>Throwable</code>, its stack trace
     *          will be included in the log.
     */
    public void log(I18NResourceBundle i18n, String key, Object arg) {
        if (arg instanceof Throwable)
            log(i18n.getString(key, arg), (Throwable)arg);
        else
            log(i18n.getString(key, arg), null);
    }

    /**
     * Print a text message to the workdir logfile.
     * A single line of text which is as short as possible is highly
     * recommended for readability purposes.
     *
     * @param i18n a resource bundle containing the localized messages
     * @param key a key into the resource bundle for the required message
     * @param args An array of arguments to be formatted into the specified message.
     *          If the first arg is a <code>Throwable</code>, its stack
     *          trace will be included in the log.
     */
    public void log(I18NResourceBundle i18n, String key, Object[] args) {
        if (args != null && args.length > 0 && args[0] instanceof Throwable)
            log(i18n.getString(key, args), (Throwable)args[0]);
        else
            log(i18n.getString(key, args), null);
    }

    private synchronized void log(String text, Throwable trace) {
        SimpleDateFormat format = new SimpleDateFormat("[ddMMMyyyy kk:mmz]");

        PrintWriter pw;
        boolean closeWhenDone;
        if (out != null) {
            pw = new PrintWriter(out);
            closeWhenDone = false;
        }
        else {
            FileWriter fw = null;
            try {
                // open writer in append mode
                fw = new FileWriter(file, true);
            }
            catch (IOException e) {
                // oh well
                if (!logError) {
                    // just report first instance
                    String msg = local_i18n.getString("log.error", new Object[] { file, e.toString() });
                    System.err.println(msg);
                    logError = true;
                }
                return;
            }

            pw = new PrintWriter(fw);
            closeWhenDone = true;
        }

        pw.print(format.format(new Date()));
        pw.print("  ");
        pw.println(text);

        if (trace != null)
            trace.printStackTrace(pw);

        if (closeWhenDone)
            pw.close();
        else
            pw.flush();
    }

    private File file;
    private Writer out;
    private boolean logError;

    private static I18NResourceBundle local_i18n = I18NResourceBundle.getBundleForClass(LogFile.class);
}
