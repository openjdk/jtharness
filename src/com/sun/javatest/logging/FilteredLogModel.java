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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;


public class FilteredLogModel extends LogModel {

    public FilteredLogModel(ObservedFile logFile, String fileName) {
        super(logFile, fileName);
        setFilter(new LogFilter(this));
        super.addNewPageListener(new NewPageListener() {
            public void onNewPage(int startRecord, int endRecord, int p) {
                if (noFilter() && isStableState()) {
                    fireNewPage(startRecord, endRecord, p);
                }
            }
        });
        setObservedFile(logFile);
    }




    private void setFilter(LogFilter filter) {
        this.filter = filter;
        if (noFilter()) {
            shownRecords = getUnfilteredRecords();
        } else {
            onFilterChanged();
        }
    }

    public LogFilter getFilter() {
        return filter;
    }


    private boolean noFilter() {
        return filter == null || filter.isAllEnabled();
    }

    private synchronized void onFilterChanged() {
        if (filter == null) {
            return;
        }
        if (worker != null && worker.isAlive()) {
            try {
                worker.stopFlag = true;
                worker.interrupt();
                worker.join(100);
            } catch (InterruptedException ex) {
                if (debug) ex.printStackTrace();
                // it's ok
            }
        }
        stable = false;
        fireFilterChanged();
        if (!noFilter()) {
            worker = new FilterWorker("FilterWorker");
            worker.start();
        } else {
            // all filters disabled !
            shownRecords = getUnfilteredRecords();
            int pg = pagesRead();
            int from = (pagesRead() - 1) * getPageSize() ;
            int to = pagesRead() * getPageSize() - 1;
            fireNewPage(from, to, pg);
        }
    }

    public ArrayList<LiteLogRecord> getRecords() {
        return shownRecords;
    }

    private ArrayList<LiteLogRecord> getUnfilteredRecords() {
        return super.getRecords();
    }

    public void dispose() {
        super.dispose();
        resetModel();
    }

    private synchronized void resetModel() {
        shownRecords.clear();
        //pageListeners.clear();
        //filterListeners.clear();

        if (of != null && fileListener != null) {
            of.removeFileListener(fileListener);
        }

        if (worker != null && worker.isAlive()) {
            worker.stopFlag = true;
            worker.interrupt();
            try {
                worker.join();
            } catch (InterruptedException ex) {
                if (debug) ex.printStackTrace();
            }
        }
        worker = null;
        filter.dipose();

    }

    public void addNewPageListener(NewPageListener lst) {
        pageListeners.add(lst);
    }

    public void addFilterChangedListener(FilterChangedListener lst) {
        filterListeners.add(lst);
    }

    private void fireNewPage(int from, int to) {
        int pageNum = (to-1) / getPageSize() + 1;
        fireNewPage(from, to, pageNum);
    }

    private void fireNewPage(int from, int to, int pageNum) {
        for (NewPageListener pageListener : pageListeners) {
            pageListener.onNewPage(from, to, pageNum);
        }
    }

    private void fireFilterChanged() {
        for (FilterChangedListener filterListener : filterListeners) {
            filterListener.onFilterChanged();
        }
    }


    public int recordsRead() {
        if (shownRecords != null) {
            return shownRecords.size();
        } else {
            return 0;
        }
    }

    private boolean stable = false;
    private FilterWorker worker;
    private ArrayList<LiteLogRecord> shownRecords;
    private LogFilter filter;
    private ArrayList<NewPageListener> pageListeners = new ArrayList<NewPageListener>();
    private ArrayList<FilterChangedListener> filterListeners = new ArrayList<FilterChangedListener>();

    public boolean isStableState() {
        if (noFilter()) {
            return super.isStableState();
        } else {
            return stable;
        }
    }

    void setObservedFile(ObservedFile of) {
        super.setObservedFile(of);
        if (this.of != null && fileListener != null) {
            this.of.removeFileListener(fileListener);
        }
        this.of = of;
        if (of != null) {
            fileListener = new LogFileListener();
            of.addFileListener(fileListener);
        }
    }

    class LogFileListener implements FileListener {
        public void fileModified(FileEvent e) {
            synchronized (FilteredLogModel.this) {
                if (e.getType().equals(FileEvent.START_ERASING)){
                    resetModel();
                } else if (e.getType().equals(FileEvent.ERASED)){
                    onFilterChanged();
                }
            }
        }
    }

    private class FilterWorker extends Thread {

        FilterWorker(String name) {
            super(name);
        }

        boolean stopFlag = false;

        public void run() {
            ArrayList<LiteLogRecord> tmp = new ArrayList<LiteLogRecord>();
            int firstRecordOnPage = 0;
            int i = 0;
            boolean first = true;

            shownRecords = tmp;
            fireFilterChanged();
            fireNewPage(firstRecordOnPage, tmp.size());

            while (!jobDone() || first) {
                first = false;
                for (; i < FilteredLogModel.this.getUnfilteredRecords().size(); i++) {
                    if (stopFlag) {
                        return;
                    }
                    if (debug) System.out.println("Worker - recored read");
                    LiteLogRecord rec = FilteredLogModel.this.getUnfilteredRecords().get(i);
                    if (filter.isApplicable(rec)) {
                        tmp.add(rec);
                        if (tmp.size() % getPageSize() == 0 && tmp.size()!=0) {
                            if (shownRecords != tmp) {
                                shownRecords = tmp;
                                fireFilterChanged();
                            }
                            //fireNewPage(firstRecordOnPage, tmp.size());
                            firstRecordOnPage = tmp.size();
                        }
                    }
                }
                if (stopFlag) {
                    return;
                }
                if (shownRecords != tmp) {
                    shownRecords = tmp;
                }
                fireNewPage(firstRecordOnPage, tmp.size());
                if (!jobDone()) {
                    try {
                        if (debug) System.out.println("Worker - 2 All parent records read, sleep 500");
                        Thread.currentThread().sleep(500);
                        stable = true;
                        if (stopFlag) {
                            return;
                        }
                    } catch (InterruptedException ex) {
                        if (debug) ex.printStackTrace();
                        // it's ok
                    }
                }
            }
        }
    }

    private LogFileListener fileListener;
    private ObservedFile of;

    public interface FilterChangedListener {
        public void onFilterChanged();
    }


    public class LogFilter {

        public LogFilter(FilteredLogModel model) {
            theMap = new HashMap<String, Boolean>();
            this.model = model;
        }

        public boolean isAllEnabled() {
            if (!"".equals(substring)) {
                return false;
            }
            synchronized(theMap) {
                for (Boolean aBoolean : theMap.values()) {
                    if (!aBoolean) {
                        return false;
                    }
                }
            }
            if (debugFilter) System.out.println("ALL enabled");
            return true;
        }

        public void enableLogger(String logName, int level, boolean enable) {
            String key = getKeyAndCheck(logName, level);
            if (debugFilter) {
                System.out.println("enableLogger " + key + " " + enable);
            }
            synchronized(theMap) {
                theMap.put(key, Boolean.valueOf(enable));
            }
            onFilterChanged();
        }

        private void onFilterChanged() {
            model.onFilterChanged();
        }

        public void setSubstring(String substring) {
            substring = substring.trim().toUpperCase();
            if (this.substring.equals(substring)) {
                return;
            }
            this.substring = substring;
            onFilterChanged();
        }

        public boolean isApplicable(LogModel.LiteLogRecord rec) {
            if (rec == null) {
                return false;
            }
            if (noFilter()) {
                return true;
            }
            String logName =  model.getLogname(rec.loggerID);
            int pos = logName.indexOf("#");
            String shortName;
            if (pos >= 0) {
                shortName = logName.substring(pos+1);
            } else {
                shortName = logName;
            }
            String key = getKeyAndCheck(shortName, rec.severety);
            Boolean b;
            synchronized(theMap) {
                b = theMap.get(key);
            }
            if (debugFilter) System.out.println("? " + key + " " + b);
            if (b == null) {
                return false;
            }
            if (!b) {
                return false;
            }
            if (!"".equals(substring)) {
                String header = rec.getHeader(logName).toUpperCase();
                if (header.indexOf(substring) >= 0) {
                    return true;
                }
                String message = model.getRecordMessage(rec).toUpperCase();
                return message.indexOf(substring) >= 0;
            }
            return true;
        }

        public void dipose() {
            synchronized(theMap) {
                theMap.clear();
            }
        }

        private String getKeyAndCheck(String logName, int level) {
            int upLevel;
            if (level < Level.INFO.intValue()) {
                upLevel = Level.FINE.intValue();
            } else if (level < Level.WARNING.intValue()) {
                upLevel = Level.INFO.intValue();
            } else if (level < Level.SEVERE.intValue()) {
                upLevel = Level.WARNING.intValue();
            } else {
                upLevel = Level.SEVERE.intValue();
            }

            String key = logName + "$$$" + upLevel;
            synchronized(theMap) {
                if (!theMap.containsKey(key)) {
                    theMap.put(key, Boolean.TRUE);
                }
            }
            return key;
        }

        private HashMap<String, Boolean> theMap;
        private FilteredLogModel model;
        private String substring = "";
        private static final boolean debugFilter = false;

        public String getSubstring() {
            return substring;
        }

    }


}



