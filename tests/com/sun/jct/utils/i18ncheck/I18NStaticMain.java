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
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;

public class I18NStaticMain {

    private static HashSet<String> exludeDirs;

    public static void main(String[] args) {
        String propsFileName = "i18n.properties";
        String mergeFileName = "dynamic.reqd";
        boolean needMerge = false;
        boolean recurse = true;

        String java_v = System.getProperty("java.specification.version");
        java_version = "1.5".equals(java_v) ? JAVA_5 : JAVA_6;

        exludeDirs = new HashSet<>();
        exludeDirs.add(".svn");

        String srcRoot = "";
        String tmpRoot = "";
        String rulesXML = "";

        String opts = "-rules -srcDir -propsName -mergeName -merge -tmpDir -exclude -noRecurse";
        boolean localData = false;

        for (int i = 0; i < args.length; i++) {
            if (!opts.contains(args[i])) {
                System.out.println("Wrong option! :" + args[i]);
                System.exit(1);
            } else if (args[i].equals("-rules")) {
                rulesXML = args[++i];
            } else if (args[i].equals("-srcDir")) {
                srcRoot = args[++i];
                File srcRootFile = new File(srcRoot);
                if (!srcRootFile.isDirectory()) {
                    usage();
                    System.exit(1);
                }
            } else if (args[i].equals("-props")) {
                propsFileName = args[++i];
            } else if (args[i].equals("-merge")) {
                needMerge = true;
            } else if (args[i].equals("-mergeName")) {
                needMerge = true;
                mergeFileName = args[++i];
            } else if (args[i].equals("-tmp")) {
                tmpRoot = args[++i];
            } else if (args[i].equals("-exclude")) {
                while ((i < args.length - 1) && !opts.contains(args[i+1])) {
                    exludeDirs.add(args[++i]);
                }
            } else if (args[i].equals("-noRecurse")) {
                recurse = false;
            }

        }

        if (srcRoot.equals("") || tmpRoot.equals("")) {
            usage();
            System.exit(1);
        }

        int status = run(rulesXML, srcRoot, tmpRoot, propsFileName,
                                            mergeFileName, needMerge, recurse);
        System.exit(status);
    }

    private static int run(String rulesXML, String srcRoot, String tmpRoot,
                                     String propsFileName, String mergeFileName,
                                            boolean needMerge, boolean recurse) {

        if (checker == null) {
            File rulesFile = new File(rulesXML);
            if (!rulesFile.exists()) {
                System.exit(1);
            }
            checker = new I18NStaticChecker();
            checker.prepareXML(rulesFile);
        }

        File srcDir = new File(srcRoot);

        File propsFile = new File(srcRoot + File.separator + propsFileName);

        File tmpDir = new File(tmpRoot/* + File.separator + srcDir.getName()*/);

        if (propsFile.exists()) {
            ArrayList<File> javaFiles = new ArrayList<>();
            for (File f : srcDir.listFiles()) {
                if (f.getName().endsWith(".java"))
                    javaFiles.add(f);
            }
            checker.setFileSet(javaFiles);

            Properties props = new Properties();
            try {
                props.load(new FileInputStream(propsFile));
            } catch (IOException e) {
                System.out.println("WARNING: property file :" +
                                    propsFile.getAbsolutePath() + "can't be loaded");
                return 1;
//                return;
            }
            checker.setProperties(props);

            if (needMerge) {
                HashSet<String> dynKeys = new HashSet<>();
                String dynKeyFile = tmpRoot + File.separator + mergeFileName;

                File dynFile = new File(dynKeyFile);
                if (dynFile.exists()) {
                    dynKeys = readKeySet(dynFile);
                }
                checker.setDynKeys(dynKeys);
            }

            checker.checkI18N();

            if (!tmpDir.exists()) {
                tmpDir.mkdirs();
            }
            checker.writeResult(tmpDir);
        }

        File[] results = tmpDir.listFiles();
        int res = 0;
        if (results != null) {
            for (File f : results) {
                if (!f.isDirectory() && f.getName().contains("difference")) {
                    res = 1;
                }
            }
        }

        if (recurse) {
            File[] files = srcDir.listFiles();
            for (File f : files) {
                String path = f.getAbsolutePath();
                if (f.isDirectory() && isAppropriateDir(f)) {
                    res = res | run(rulesXML, f.getAbsolutePath(),
                            tmpRoot + File.separator + f.getName(),
                            propsFileName, mergeFileName, needMerge, recurse);
                }
            }
        }

        return res;
    }

    private static void usage() {
        System.out.println("IT IS USAGE!");
    }

    private static boolean isAppropriateDir(File dir) {
        String path = dir.getAbsolutePath();
        for (String ed : exludeDirs) {
            if (path.contains(ed)) {
                return false;
            }
        }
        return true;
    }

    private static HashSet<String> readKeySet(File f) {
        try {
            HashSet<String> set = new HashSet<>();
            BufferedReader r = new BufferedReader(new FileReader(f));
            String str;
            while ( (str = r.readLine()) != null ) {
                set.add(str);
            }
            r.close();
            return set;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static I18NStaticChecker checker;

    public static int java_version;
    public static final int JAVA_5 = 0;
    public static final int JAVA_6 = 1;
}
