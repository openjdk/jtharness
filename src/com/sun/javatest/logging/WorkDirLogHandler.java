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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.util.logging.ErrorManager;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import com.sun.javatest.util.I18NResourceBundle;

public class WorkDirLogHandler extends StreamHandler {

    private void open(File fname) throws IOException {
        bout =  new BufferedOutputStream(new FileOutputStream(fname.toString()));
        setOutputStream(bout);
        first = true;
    }

    public WorkDirLogHandler(ObservedFile of) {
        setLevel(Level.ALL);
        setFormatter(new JTFormatter());
        pattern = of.getAbsolutePath();
        this.of = of;
    }


    public void publish(LogRecord record) {
        publish(record, null);
    }

    public void publish(LogRecord record, String logName) {
        synchronized (WorkDirLogHandler.class) {
            if (bout == null) {
                try {
                    open(of);
                } catch (IOException ix) {
                    ix.printStackTrace();
                }
            }

            long startOff = of.length();

            JTFormatter frm = (JTFormatter) getFormatter();
            frm.setLogname(logName);
            super.publish(record);
            frm.setLogname(null);
            if (bout != null ) {
                flush();
            }
            if (first) {
                first = false;
                startOff += frm.getHead(this).length();
            }

            startOff += frm.getLastOffset();
            long endOff = of.length();

            of.addToIndex(record, startOff, endOff, logName);
        }
    }

    public String getPattern() {
        return pattern;
    }

    public void close() {
        synchronized (WorkDirLogHandler.class) {
            if (bout != null) {
                try {
                    byte [] bytes = getFormatter().getTail(this).getBytes();
                    bout.write(bytes, 0, bytes.length);
                    bout.flush();
                    bout.close();
                    bout = null;
                } catch (IOException ioe) {
                    reportError(null, ioe, ErrorManager.CLOSE_FAILURE);
                }
            }
        }
    }

    public void eraseLogFile() throws IOException {
        synchronized (WorkDirLogHandler.class) {
            if (bout == null) {
                return;
            }
            if (!of.canWrite()) {
                throw new IOException(i18n.getString("workdirloghandler.canterasefile") + of.getAbsolutePath());
            }

            ((JTFormatter)getFormatter()).setErasing();
            of.fireFileEvent(new FileEvent(of, FileEvent.START_ERASING));
            close();
            RandomAccessFile raf = new RandomAccessFile(of, "rw");
            raf.setLength(0);
            raf.close();

            raf = new RandomAccessFile(of.getRecordInexFile(), "rw");
            raf.setLength(0);
            raf.close();

            raf = new RandomAccessFile(of.getLoggersInexFile(), "rw");
            raf.setLength(0);
            raf.close();
            of.fireFileEvent(new FileEvent(of, FileEvent.ERASED));
        }
    }

    private String pattern;
    private BufferedOutputStream bout;
    private ObservedFile of = null;

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(WorkDirLogHandler.class);

    private boolean first;
}

