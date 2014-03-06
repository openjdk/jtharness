/*
 * $Id$
 *
 * Copyright (c) 2004, 2013, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

/**
 * Information about the last or current test run.  This is an interface onto
 * this meta-information stored in a work directory.
 */
public class LastRunInfo {
    private LastRunInfo() {
    }

    private LastRunInfo(WorkDirectory wd) throws IOException {
        this();

        FileInputStream in = null;
        Properties p;
        try {
            in = new FileInputStream(wd.getSystemFile(FILENAME));
            p = new Properties();
            p.load(in);
        } finally {
            try { if (in != null) in.close(); } catch (IOException e) {}
        }

        String val = p.getProperty(START);

        try {
            startTime = Long.parseLong(val);
        }
        catch (NumberFormatException e) {
        }

        val = p.getProperty(FINISH);
        try {
            finishTime = Long.parseLong(val);
        }
        catch (NumberFormatException e) {
        }

        configName = p.getProperty(CONFIG);
        testURLs = split(p.getProperty(TEST_URLS));
    }

    /**
     * When did the last test run start.
     * Warning - the time information stored in a test result is only accurate to
     * one second, so everything below a 1000ms can't be compared reliably.  If you are
     * comparing times to a TestResult, it is suggested that you either remove the ms
     * from the return value or do something other than compare the integers.
     * @return The time (in milliseconds) at which the last test run started.
     * @see java.util.Date
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * When did the last test run end.
     * Warning - the time information stored in a test result is only accurate to
     * one second, so everything below a 1000ms can't be compared reliably.  If you are
     * comparing times to a TestResult, it is suggested that you either remove the ms
     * from the return value or do something other than compare the integers.
     * @return The time (in milliseconds) at which the last test run completed (for
     *         any reason).
     * @see java.util.Date
     */
    public long getFinishTime() {
        return finishTime;
    }

    /**
     * When did the last test run start.
     * @return The time (in milliseconds) at which the last test run started.
     *         May be zero if the information is not available.
     * @see java.util.Date
     */
    public Date getStartDate() {
        return new Date(startTime);
    }

    /**
     * When did the last test run end.
     * @return The time (in milliseconds) at which the last test run completed (for
     *         any reason).  May be zero if the information is not available.
     * @see java.util.Date
     */
    public Date getFinishDate() {
        return new Date(finishTime);
    }

    /**
     * Get the name of the configuration that was used in the last
     * test run.
     * @return Configuration name as it appeared in the configuration.  May be
     *         null or empty string if this information is not available.
     */
    public String getConfigName() {
        return configName;
    }
    /**
     * Get the URLs of the tests that were executed in the last test run.
     * @return String array of testURLs executed.
     */
    public ArrayList getTestURLs() {
        return testURLs;
    }

    /**
     * Joins list of String into a single space separated String
     * @param list
     * @return A single string with all the items in the list joined.
     */
    private static String join(ArrayList list) {
        if (list == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (Iterator it = list.iterator(); it.hasNext();) {
            sb.append((String)it.next());
            sb.append(SEP);
        }

        // remove trailing space
        if (sb.length() > 0 && sb.charAt(sb.length()-1) == ' ')
            sb.deleteCharAt(sb.length()-1);

        return sb.toString();
    }
    /**
     * Split the string into list
     * @param joined - string to split
     * @return array list, never null.
     */
    private static ArrayList split(String joined) {
        ArrayList list = new ArrayList();
        if (joined == null || joined.trim().length() == 0) {
            return list;
        }
        // emulate StringTokenizer(joined, SEP) to find tokens
        int begin = 0;
        int end = joined.indexOf(SEP);
        while (end >= 0) {
            list.add(joined.substring(begin, end));
            begin = end + SEP.length();
            end = joined.indexOf(SEP, begin);
        }
        list.add(joined.substring(begin));
        return list;
    }

    /**
     * Given a work directory, attempt to create an instance using the information
     * found in it.
     * @param wd The work directory to create the information from.
     * @return Configuration name as it appeared in the configuration.  May be
     *         null or empty string if this information is not available.
     * @throws IOException Occurs if the last run info is not available or if the
     *         system has a problem while reading the file.
     */
    public static LastRunInfo readInfo(WorkDirectory wd) throws IOException {
        return new LastRunInfo(wd);
    }

    /**
     * Given a work directory, write the given run information in it.
     * @param workdir The work directory to modify.  Must be able to read-write
     *                files inside it.
     * @param start Time in milliseconds at which the last test run started.
     *              Must be a non-negative number.
     * @param stop Time in milliseconds at which the last test run terminated.
     *             Must be a non-negative number.
     * @param config Configuration name which was used to do the last test run.
     *               May be null or empty string if necessary.
     * @throws IOException If for any reason the information file cannot be
     *                     created, opened, written into or deleted.
     */
    public static void writeInfo(WorkDirectory workdir, long start, long stop,
            String config, ArrayList testURLs)
                throws IOException {
        Properties p = new Properties();
        p.setProperty(CONFIG, config);
        p.setProperty(START, Long.toString(start));
        p.setProperty(FINISH, Long.toString(stop));
        p.setProperty(TEST_URLS, join(testURLs));

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(workdir.getSystemFile(FILENAME));

            // this is a date file, does not need i18n
            p.store(out, "Last test run info");
        }
        finally {
            try { if (out != null) out.close(); } catch (IOException e) {}
        }
    }

    private String configName;
    private long startTime;
    private long finishTime;
    private ArrayList testURLs;

    // file in the work dir
    private static final String FILENAME = "lastRun.txt";

    // keys for properties
    private static final String START = "startTime";
    private static final String FINISH = "finishTime";
    private static final String CONFIG = "configName";
    private static final String TEST_URLS="testURLs";

    private static final String SEP = " ";
}
