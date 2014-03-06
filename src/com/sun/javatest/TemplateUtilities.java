/*
 * $Id$
 *
 * Copyright (c) 2006, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Template support utility methods.  Only meant to be used internally by
 * JT Harness.
 */
public class TemplateUtilities {

    /**
     * Get the path to the template associated with the work dir.
     * @param wd The work directory to query.
     * @return Null if no associated template could be found.  A
     *         file which refers to the template file associated with
     *         the work directories.  May or may not be possible to
     *         actually open this file, the caller is responsible for
     *         determining that.
     */
    public static File getTemplateFile(WorkDirectory wd) {
        String s = getTemplatePath(wd);
        if (s != null)
            return new File(s);
        else
            return null;
    }

    /**
     * Get the path to the template associated with the work dir.
     * @param wd The work directory to query.
     * @return Null if no associated template could be found.  An
     *         abstract path otherwise.
     */
    public static String getTemplatePath(WorkDirectory wd) {
        if (wd == null) {
            return null;
        }

        File dataFile = wd.getSystemFile(TEMPLATE_FILE);
        Properties p = new Properties();

        FileInputStream in = null;
        try {
            in = new FileInputStream(dataFile);
            p.load(in);
        }
        catch (FileNotFoundException e) {
            // should log the error
            // e.printStackTrace()
            return null;
        }
        catch (IOException e) {
            // should log the error
        }
        finally {
            try {
                if (in != null)
                    in.close();
            }
            catch (IOException e) {
            }
        }

        if (p != null)
            return p.getProperty(TEMPL_FILE_PROP);
        else
            return null;
    }

    /**
     * Associate a template file with a work directory.  None of the
     * parameters may be null.
     * @param wd Work dir to operate on.
     * @param t Template file to associate the work directory with.
     * @param changeable Can the workdir be later associated with a
     *        different template?  Not currently used.
     * @throws IOException If the complete path to the template file
     *         cannot be resolve, or if there is an error writing to
     *         the work dir.
     */
    public static void setTemplateFile(WorkDirectory wd, File t,
                        boolean changeable) throws IOException {
        if (t == null) {
            return;
        }
        File dataFile = wd.getSystemFile(TEMPLATE_FILE);
        Properties p = new Properties();
        p.put(TEMPL_FILE_PROP, t.getCanonicalPath());

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(dataFile);
        }
        catch (FileNotFoundException e) {
            // should log the error
            // e.printStackTrace()
            return;
        }

        try {
            p.save(out, "template information file - do not modify");
        }
        finally {
            out.close();
        }
    }

    /**
     * Using a lightweight method,, find out if and which template is associated
     * with a work dir.
     * @return Null if no template is associated.  Will also be null if the given
     *         dir is not a workdir - this is something that should be checked
     *         first.
     * @throws IOException If there are problems getting the template information,
     *         which usually means there is a problem reading the workdir.
     */
    public static String getTemplateFromWd(File dir) throws IOException {
        File f = new File(dir, WorkDirectory.JTDATA + File.separator +TEMPLATE_FILE);

        // workdir may not have template associated
        if (!f.exists())
            return null;

        Properties p = new Properties();
        InputStream in = new FileInputStream(f);
        p.load(in);
        in.close();
        return  p.getProperty(TEMPL_FILE_PROP);

    }


    public static boolean isTemplate(File f) throws IOException {
        Properties p = new Properties();
        InputStream in = new FileInputStream(f);
        p.load(in);
        in.close();
        return new Boolean(p.getProperty(InterviewParameters.IS_TEMPLATE)).booleanValue();
    }

    public static ConfigInfo getConfigInfo(File f) throws IOException {
        //InterviewParameters ip = InterviewParameters.open(f);
        return new ConfigInfo(f);
    }

    /*
    public static ConfigInfo getConfigInfo(TestSuite ts, File f)
                        throws IOException {
        //InterviewParameters ip = ts.createInterview();
        //ip.load(f);
        return new ConfigInfo(f);
    }
    */

    public static class ConfigInfo {
        ConfigInfo(File f) throws IOException {
            // this file is read manually to avoid initializing the
            // interview which is a very heavyweight process
            Properties p = new Properties();
            p.load(new FileInputStream(f));
            name = p.getProperty(InterviewParameters.NAME);
            description = p.getProperty(InterviewParameters.DESC);
            isTemplate = Boolean.valueOf(p.getProperty(InterviewParameters.IS_TEMPLATE)).booleanValue();
            templateUrl = p.getProperty(InterviewParameters.TEMPLATE_PATH);
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public boolean isTemplate() { return isTemplate; }
        public String getTemplatePath() { return templateUrl; }

        String name;
        String description;
        boolean isTemplate;
        String templateUrl;
    }

    private static final String TRUE = "true";
    private static final String TEMPLATE_PREF = "TEMPLATE.";
    private static final String TEMPLATE_FILE = "template.data";

    /**
     * Name of the property in the file which contains the path to the template.
     */
    private static final String TEMPL_FILE_PROP = "file";
}
