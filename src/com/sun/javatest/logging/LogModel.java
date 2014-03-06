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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogModel {

    public LogModel(ObservedFile logFile, String fileName) {
        file = fileName;
        records = new ArrayList<LiteLogRecord>();
        loggers = new ArrayList<String>();
        messageCache = new MessageCache();
        setObservedFile(logFile);
    }

    public ArrayList<String> getLoggers() {
        return loggers;
    }

    public ArrayList<LiteLogRecord> getRecords() {
        return records;
    }

    public void init() {
        worker = new Worker("LogViewerWorker");
        //worker.setPriority(Thread.MIN_PRIORITY);
        worker.start();
    }

    boolean jobDone() {
        if (worker == null ) {
            return false;
        }
        return !worker.isAlive();
    }

    int recordsRead() {
        if (records != null) {
            return records.size();
        } else {
            return 0;
        }
    }

    public int pagesRead() {
        int records = recordsRead();
        if (records == 0) {
            return 0;
        } else {
            return (records-1) / PAGE_SIZE + 1;
        }
    }

    private class Worker extends Thread {

        public Worker(String name) {
            super(name);
        }

        boolean stop = false;

        public void run() {
            RandomAccessFile r = null;
            int firstRecordOnPage = 0;
            int recordCount = 0;
            stable = false;
            try {
                r =  new RandomAccessFile(file, "r");
                String signature = null;
                while (true) {
                    signature = r.readLine();
                    if (signature == null) {
                        if (stop) {
                            return;
                        }
                        try {
                            if (debug) System.out.println("Worker-1 sleep, first loop (empty file)");
                            stable = true;
                            fireNewPage(0, 0);
                            sleep(500);
                        } catch (InterruptedException ex) {
                            if (debug) ex.printStackTrace();
                            // it's ok
                        }
                    } else {
                        break;
                    }
                }
                if (!JTFormatter.LOG_SIGNATURE.equals(signature)) {
                    throw new IOException("Wrong logfile " + file + " signature=" + signature);
                }

                stable = false;

                // read info from index
                synchronized (of) {
                    of.readLoggers(loggers);
                    of.readRecords(records);
                    recordCount = records.size();
                    firstRecordOnPage = (pagesRead()-1)*PAGE_SIZE;
                    if (debug) System.out.println("Worker-1 read from index " + records.size() + this);
                }

                for (String logger : loggers) {
                    fireNewLoggerFound(logger);
                }

                if (records.size() > 0 ) {
                    recordCount = records.size();
                    LogModel.LiteLogRecord llr = records.get(recordCount-1);
                    r.seek(llr.endOff);
                }


                String readStr = "";
                while (true) {
                    while (readStr != null) {
                        if (stop) {
                            return;
                        }

                        String logName = r.readLine();
                        if (logName == null) {
                            break;
                        }


                        int logID = loggers.indexOf(logName);
                        if (logID < 0) {
                            logID = loggers.size();
                            loggers.add(logID, logName);
                            fireNewLoggerFound(logName);
                        }
                        // 2) Level (int)
                        readStr = r.readLine();
                        if (readStr == null) {
                            throw new IOException("Wrong logfile " + file);
                        }
                        int level = Integer.parseInt(readStr);
                        // 3) Time in mills
                        readStr = r.readLine();
                        if (readStr == null) {
                            throw new IOException("Wrong logfile " + file);
                        }
                        long mills = Long.parseLong(readStr);
                        // 4) Msg length
                        readStr = r.readLine();
                        if (readStr == null) {
                            throw new IOException("Wrong logfile " + file);
                        }
                        long length = Long.parseLong(readStr);
                        // 5) Msg
                        long read = 0;
                        long start = r.getFilePointer();
                        StringBuffer msg = new StringBuffer();

                        // Do not optimize this loop, do not use readLine() !
                        while (read <= length) {
                            byte ch = r.readByte();
                            read++;
                            msg.append(ch);
                        }

                        LiteLogRecord record = new LiteLogRecord();
                        record.loggerID = logID;
                        record.startOff = start;
                        record.endOff = r.getFilePointer();
                        record.time = mills;
                        record.severety = level;

                        records.add(record);
                        if (recordCount % PAGE_SIZE == 0 && recordCount != 0) {
                            fireNewPage(firstRecordOnPage, recordCount);
                            firstRecordOnPage = recordCount;
                        }
                        recordCount++;
                        if (debug) System.out.println("Worker-1 - record read");
                    }
                    try {
                        if (stop) {
                            return;
                        }
                        if (firstRecordOnPage  != recordCount) {
                            fireNewPage(firstRecordOnPage, recordCount-1);
                            if (debug) System.out.println("Worker-1  - fireNewPage(" + firstRecordOnPage + " , "  + (recordCount-1) + " )");
                        }
                        if (debug) System.out.println("Worker-1  - all records read, sleep");
                        sleep(500);
                        if (stop) {
                            return;
                        }
                        stable = true;
                        readStr = "";
                    } catch (InterruptedException ex) {
                        // ok
                        if (debug) ex.printStackTrace();
                    }
                }  // autoupdate loop
            } catch (IOException ex) {
                logEx(ex);
            } finally {
                try {
                    if (r != null ) {
                        r.close();
                    }
                    if (!stop && firstRecordOnPage  != (recordCount-1)) {
                        fireNewPage(firstRecordOnPage, recordCount-1);
                    }
                } catch (IOException ex) {
                    logEx(ex);
                }
            }
        }
    }

    public void addNewLoggerListener(LoggerListener lst) {
        loggerListeners.add(lst);
    }

    public void removeNewLoggerListeners() {
        loggerListeners.clear();
    }

    void addNewPageListener(NewPageListener lst) {
        pageListeners.add(lst);
    }

    boolean isStableState() {
        return stable;
    }

    void setObservedFile(ObservedFile of) {
        if (this.of != null && fileListener != null) {
            this.of.removeFileListener(fileListener);
        }

        this.of = of;
        if (of != null) {
            fileListener = new LogFileListener();
            of.addFileListener(fileListener);
        }
    }

    private void fireNewLoggerFound(String loggerName) {
        for(LoggerListener lst : loggerListeners) {
            lst.onNewLogger(loggerName);
        }
    }


    private void fireRemoveAllLoggers() {
        for(LoggerListener lst : loggerListeners) {
            lst.onRemoveAllLoggers();
        }
    }

    private void fireNewPage(int from, int to) {
        int pageNum = (to-1) / PAGE_SIZE + 1;
        for(NewPageListener lst : pageListeners) {
            lst.onNewPage(from, to, pageNum);
        }
    }


    public synchronized String getRecordMessage(LiteLogRecord rec) {
        if (rec == null)
            return "";
        if (messageCache.containsKey(rec)) {
            return messageCache.get(rec);
        }

        StringBuffer msg = new StringBuffer();
        try {
            ensureMirrorFileOpened();
            if (rec == null ||  mirrorFile == null) {
                return "";
            }
            mirrorFile.seek(rec.startOff);
            int line = 0;
            String readStr = "";
            while (readStr != null && mirrorFile.getFilePointer() < rec.endOff) {
                readStr = mirrorFile.readLine();
                if (line > 0) {
                    msg.append('\n');
                }
                msg.append(readStr);
                line++;
            }
        } catch (IOException ex) {
            // it can be after log file purge
            return "";
        }
        messageCache.put(rec, msg.toString());
        return msg.toString();
    }

    synchronized void dispose() {
        resetModel();
        loggerListeners.clear();
        pageListeners.clear();

        if (of != null && fileListener != null) {
            of.removeFileListener(fileListener);
        }

        //theThread = null;
        worker = null;
    }

    private synchronized void resetModel() {
        if (worker != null && worker.isAlive()) {
            worker.stop = true;
            worker.interrupt();
            if (debug) System.out.println("worker.interrupt()");
        }
        // wait
        if (worker != null) {
            try {
                worker.join();
                if (debug) System.out.println("worker.join()");
            } catch (InterruptedException ex) {
                if (debug) ex.printStackTrace();
            }
        }
        if (mirrorFile != null) {
            try {
                mirrorFile.close();
            } catch (IOException ex) {
                logEx(ex);
            }
        }

        synchronized (of) {
            records.clear();
            loggers.clear();
            fireRemoveAllLoggers();
        }
        messageCache.clear();
        if (mirrorFile != null) {
            try {
                mirrorFile.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            mirrorFile = null;
        }
    }

    private void ensureMirrorFileOpened() throws FileNotFoundException {
        if (mirrorFile == null) {
            mirrorFile = new RandomAccessFile(file, "r");
        }
    }

    public int getPageSize() {
        return PAGE_SIZE;
    }

    public String getLogname(int loggerID) {
        if (loggerID < loggers.size())
            return loggers.get(loggerID);
        else
            return "";
    }

    public void setLogger(Logger log) {
        logger = log;
    }

    private void logEx(Throwable th) {
        if (logger != null) {
            logger.logp(Level.SEVERE, getClass().getName(), null, th.getMessage(), th);
        } else {
            th.printStackTrace();
        }
    }

    class LogFileListener implements FileListener {
        public void fileModified(FileEvent e) {
            synchronized (LogModel.this) {
                if (e.getType().equals(FileEvent.START_ERASING)){
                    if (debug) System.out.println("FileEvent.START_ERASING");
                    resetModel();
                } else if (e.getType().equals(FileEvent.ERASED)){
                    if (debug) System.out.println("FileEvent.ERASED");
                    init();
                }

            }
        }
    }


    private class MessageCache extends LinkedHashMap<LiteLogRecord, String> {
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > PAGE_SIZE*2;
        }
    }

    private boolean stable = false;
    private ArrayList<String> loggers;
    private ArrayList<LiteLogRecord> records;

    private ArrayList<LoggerListener> loggerListeners = new ArrayList<LoggerListener>();
    private ArrayList<NewPageListener> pageListeners = new ArrayList<NewPageListener>();
    private MessageCache messageCache = new MessageCache();
    private String file;
    private RandomAccessFile mirrorFile;
    private Worker worker;
    private Logger logger;

    private ObservedFile of;
    private LogFileListener fileListener;

    static final boolean debug = false;

    public interface LoggerListener {
        void onNewLogger(String name);
        void onRemoveAllLoggers();
    }

    public interface NewPageListener {
        void onNewPage(int startRecord, int endRecord, int pageNum);
    }

    private static final int PAGE_SIZE = 1000;


    static public class LiteLogRecord {

        private String getTimeString() {
            DateFormat dfISO8601 = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            return dfISO8601.format(new Date(time));
        }

        public String getHeader(String logName) {
            StringBuffer out = new StringBuffer();
            int pos = logName.indexOf("#");
            if (pos >= 0) {
                out.append(logName.substring(pos+1));
            } else {
                out.append(logName);
            }
            out.append(", ");
            out.append(LoggerFactory.getLocalizedLevelName(Level.parse("" + severety))).append(": ");
            out.append(getTimeString());
            if (pos > 0) {
                out.append("; ");
                out.append(logName.substring(0, pos));
            }
            return out.toString();
        }
        public int loggerID;
        public long time;
        public int severety;
        public long startOff, endOff;
    }


}


