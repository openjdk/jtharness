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

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.sun.javatest.util.I18NResourceBundle;

class JTFormatter extends Formatter {


    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(JTFormatter.class);

    private int lastOffset;

    public String format(LogRecord record) {
        StringBuffer sb = new StringBuffer();
        StringBuffer msgBuf = new StringBuffer(record.getMessage());

        if (record.getThrown() != null) {
            // Report on the state of the throwable.
            Throwable th = record.getThrown();

            String excMessage = i18n.getString("logger.exception.message");
            msgBuf.append('\n');
            msgBuf.append(excMessage).append(" ").append(record.getSourceClassName()).append(",").append(record.getSourceMethodName()).append("\n");
            msgBuf.append(th.toString());
            msgBuf.append('\n');
            StackTraceElement trace[] = th.getStackTrace();
            for (int i = 0; i < trace.length; i++) {
                msgBuf.append("  at ").append(trace[i]);
                if (i != trace.length-1) {
                    msgBuf.append('\n');
                }
            }
        }
        String msg = msgBuf.toString();

        if (logName != null) {
            sb.append(logName);
        } else {
            sb.append(record.getLoggerName());
        }
        sb.append('\n');
        sb.append(record.getLevel().intValue());
        sb.append('\n');
        sb.append(record.getMillis());
        sb.append('\n');
        sb.append(msg.length());
        sb.append('\n');
        lastOffset = sb.length();
        sb.append(msg);
        sb.append('\n');
        return sb.toString();
    }

    public String getHead(Handler h) {
        return LOG_SIGNATURE + '\n';
    }

    void setErasing() {
        // TODO
    }

    public static final String LOG_SIGNATURE = "=JTLOG=";

    public int getLastOffset() {
        return lastOffset;
    }

    public void setLogname(String logName) {
        this.logName = logName;
    }

    private String logName;


}
