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
package com.sun.jct.utils.copyrightcheck;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.FileUtils;

/**
 * Checks for invalid/missing copyrights.
 */
public class CheckCopyrights extends BaseFileTask {

    /**
     * Owerride built-in pattern to check copyright.
     *
     * @param pattern
     *            Regular expression to match copyright
     */
    public void setPattern(String pattern) {
        log("custom copyrightPattern: "  + pattern, Project.MSG_VERBOSE);
        patterns = new Pattern[] {Pattern.compile(pattern)};
    }

    /**
     * Adds additional pattern to check copyright.
     *
     * @param extraPattern
     *            Regular expression to match copyright
     */
    public void setExtraPattern(String extraPattern) {
        log("extra copyrightPattern: "  + extraPattern, Project.MSG_VERBOSE);
        patterns = new Pattern[] {copyrightPattern,
                                            longMediumCopyrightPattern,
                                            Pattern.compile(extraPattern)};
    }

    /**
     * Returns <code>true</code> in case of positive results,
     * <code>false</code> in case of error.
     * @param srcFile The file to process.
     * @return true or false depending on results.
     */
    protected boolean processFile(File srcFile) {
        processingFile(srcFile);
        boolean copyrightFound = false;
        BufferedReader reader = null;
        try {
            reader = Utils.getFileReader(srcFile);
            String content = FileUtils.readFully(reader);
            if (content == null) {  // deal with empty files
                content = "";
            }
            for (int i = 0; i != patterns.length && !copyrightFound; ++i) {
                Matcher m = patterns[i].matcher(content);
                if (m.find()) {
                    copyrightFound = true;
                }
            }
        } catch (FileNotFoundException fnfe) {
            log("File not found: " + srcFile, Project.MSG_WARN);
        } catch (IOException ioe) {
            log("Cannot read file: " + srcFile, Project.MSG_WARN);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!copyrightFound) {
            // make a report
            errorInFile(srcFile, "Proper Copyright is not found.");
        }
        return copyrightFound;
    }

    private static String lineSunCopy = "(?s)Copyright.*\\s+((&#169;\\s+)|(&copy;\\s+))?(\\d{4}(-\\d{4})?) Sun.+Microsystems, Inc\\.";
    // only single space allowed at the beginning
    private static String lineRightsReserved_Strict = " All rights reserved\\..+";
    // anything allowed at the beginning
    private static String lineRightsReserved_Flex = ".*\\s+All rights reserved\\..+";
    private static String lineUse = "Use is subject to license terms.";
    private static String lineSunLogo = "Sun,.*Sun\\s+Microsystems,.*the\\s+Sun\\s+logo";

    private static Pattern copyrightPattern = Pattern.compile(
            lineSunCopy + lineRightsReserved_Strict + lineUse);

    // since the medium and long copyrights vary from project to project,
    // we cannot enforce an exact pattern for everybody, so instead
    // a quick check performed on some phrases that need to be present
    private static Pattern longMediumCopyrightPattern = Pattern.compile(
            lineSunCopy + lineRightsReserved_Flex + lineSunLogo);

    private Pattern [] patterns = new Pattern [] {copyrightPattern,
                                                  longMediumCopyrightPattern};
}
