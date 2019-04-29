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
package com.sun.jct.utils.i18ncheck;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class I18NDynamicMain
{
    public static void main(final String[] args) {
        System.setProperty("javatest.preferences.file", "NONE");
        String srcPath = args[0];
        String buildClasspath = args[1];
        String libClasspath = args[2];
        String[] testArgs = new String[args.length - 3];
        System.arraycopy(args, 3, testArgs, 0, testArgs.length);

        HashMap<String, String> tests = new HashMap();
        tests.put("com.sun.jct.utils.i18ncheck.javatest.exec.I18NExecTest", "com.sun.javatest.exec");
        tests.put("com.sun.jct.utils.i18ncheck.interview.wizard.I18NWizTest", "com.sun.interview.wizard");
        tests.put("com.sun.jct.utils.i18ncheck.javatest.agent.I18NAgentTest", "com.sun.javatest.agent");
        tests.put("com.sun.jct.utils.i18ncheck.javatest.audit.I18NAuditTest", "com.sun.javatest.audit");
        tests.put("com.sun.jct.utils.i18ncheck.javatest.batch.I18NBatchTest", "com.sun.javatest.batch");
        tests.put("com.sun.jct.utils.i18ncheck.javatest.mrep.I18NMrepTest", "com.sun.javatest.mrep");
        tests.put("com.sun.jct.utils.i18ncheck.javatest.report.I18NReportTest", "com.sun.javatest.report");
        tests.put("com.sun.jct.utils.i18ncheck.javatest.tool.I18NToolTest", "com.sun.javatest.tool");
        tests.put("com.sun.jct.utils.i18ncheck.javatest.services.I18NServicesTest", "com.sun.javatest.services");

        prepareInterview(testArgs[0], testArgs[1], testArgs[2]);

        for (String className : tests.keySet()) {
            try {
                File wd = new File(testArgs[1]);
                if (wd.exists()) {
                    deleteDir(wd);
                }

                Vector<String> vcmd = new Vector();
                vcmd.add(System.getProperty("java.home") + "/bin/java");

                vcmd.add("-classpath");
                vcmd.add(buildClasspath + ":" + libClasspath);

                if (className.contains("I18NWizTest")) {
                    vcmd.add("-Di18n.log" + "=" + tests.get(className));
                }
                else {
                    vcmd.add("-Djavatest.i18n.log" + "=" + tests.get(className));
                }

                vcmd.add(className);

                for (String s : testArgs) {
                    vcmd.add(s);
                }

                String[] xargs = new String[vcmd.size()];
                vcmd.toArray(xargs);
                Process test = Runtime.getRuntime().exec(xargs);

                HashSet<String> keys = new HashSet();
//                InputStreamReader isr = new InputStreamReader(test.getErrorStream());//usefull for debug
                InputStreamReader isr = new InputStreamReader(test.getInputStream());
                BufferedReader r = new BufferedReader(isr);
                String s = r.readLine();
                while (s != null)  {
                    if (s.startsWith("i18n:")) {
                        int index = s.lastIndexOf(':');
                        keys.add(s.substring(index + 2));
                    }
                    s = r.readLine();
                }
                test.waitFor();

                String packRelPath = tests.get(className).replaceAll("\\.", File.separator);
                File resFile = new File(srcPath + File.separator + packRelPath + File.separator + "dynamic.reqd");
                writeResults(keys, resFile);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void writeResults(HashSet<String> keys, File file) {
        try {
            if (!file.exists()) {
                File dir = file.getParentFile();
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            PrintStream pos = new PrintStream(fos);
            for (String key : keys) {
                pos.println(key);
            }
            pos.flush();
            pos.close();
        }
        catch (IOException ie) {
            System.err.println("Error while writing result");
            ie.printStackTrace();
        }
    }
    public static boolean deleteDir(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDir(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
        return dir.delete();
    }

    public static void prepareInterview(String tsPath, String wdPath, String jtiPath) {

        Properties props = new Properties();
        try {
            props.load(new FileInputStream(jtiPath));
            props.setProperty("TESTSUITE", tsPath);
            props.setProperty("WORKDIR", wdPath);
            props.setProperty("demoTS.jvm", System.getProperty("java.home") +
                    File.separator + "bin" + File.separator + "java");
            props.store(new FileOutputStream(jtiPath), "");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, Set<?>> table;
    private File dynamic;
}

