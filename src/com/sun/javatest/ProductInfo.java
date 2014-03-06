/*
 * $Id$
 *
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.util.I18NResourceBundle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;


/**
 * Information about this release of the JT Harness test harness.
 */
public class ProductInfo
{
    /**
     * The name of this product.
     * @return a string identifying the name of this product.
     */
    public static String getName() {
        return "JT Harness";
    }

    /**
     * The version of this product.
     * @return a string identifying the version of this product.
     */
    public static String getVersion() {
        return getProperty("version");
    }

    /**
     * Checks if the version of this product returned by getVersion() method
     * is not older than passed one.
     *
     * @param ver version to be compared with the current product version
     * @return true, if ver is exactly the same or newer than product version
     * @throws IllegalArgumentException if version cannot be parsed
     */
    public static boolean isSameVersionOrNewer(String ver) {
        int verProd[] = parseVersion(getVersion());
        int verCheck[] = parseVersion(ver);
        for (int i = 0; i < verProd.length; i++) {
            if (verProd[i] < verCheck[i]) {
                return false; // product is older
            } else if (verProd[i] > verCheck[i]) {
                return true;  // product is newer
            }
        }
        return true; // product is the same version
    }

    /**
     * Parses passed version string as array of integers.
     * Returned array will be of length 10.
     * So 4.3.1 will be parsed into {4,3,1,0,0,0,0,0,0,0}
     */
    private static int[] parseVersion(String v) {
        int[] arr = {0,0,0,0,0,0,0,0,0,0};
        ArrayList list = new ArrayList();
        StringTokenizer st = new StringTokenizer(v, ".");
        while (st.hasMoreTokens()) {
            list.add(st.nextToken());
        }
        int size = list.size();
        if (size > arr.length) {
            size = arr.length;
        }
        for (int i = 0; i < size; i++) {
            try {
                arr[i] = Integer.parseInt(list.get(i).toString());
            } catch (Exception e) {
                throw new IllegalArgumentException(v);
            }
        }
        return arr;
    }

    /**
     * The milestone of this product.
     * @return a string identifying the milestone of this product.
     */
    public static String getMilestone() {
        return getProperty("milestone");
    }

    /**
     * The build number for this product.
     * @return a string identifying the build number of this product.
     */
    public static String getBuildNumber() {
        return getProperty("build");
    }

    /**
     * The version of Java used to build this product.
     * @return a string identifying a version of Java used to build this product.
     */
    public static String getBuildJavaVersion() {
        return getProperty("java");
    }

    /**
     * The date this product was built.
     * @return A string identifying the date on which this product was built.
     *         Null will be returned if no build data is available.
     */
    public static Date getBuildDate() {
        // read en_US date, and prepare to emit it using the
        // current locale
        DateFormat endf =
            DateFormat.getDateInstance(DateFormat.LONG, Locale.US);
        Date date = null;
        try {
            date = endf.parse(getProperty("date"));
        }
        catch (ParseException pe) {
            // can't get the date
            date = null;
        }

        return date;
    }

    /**
     * Get the entry on the class path which contains the JT Harness harness.
     * This may be a classes directory or javatest.jar.
     * @return the entry on the class path which contains the JT Harness harness.
     */
    public static File getJavaTestClassDir() {
        if (javatestClassDir == null)
            javatestClassDir = findJavaTestClassDir(System.err);

        return javatestClassDir;
    }

    /**
     * Determine the type of package the harness was loaded from.
     * This value is for informational purposes only, the possible values
     * isn't defined.
     * @return Empty string if the information isn't available.
     */
    public static String getPackagingType() {
        return getProperty("bundle-type");
    }

    /**
     * What sort of subset (if any) of the full harness is this.
     * @return A short descriptor describing this variety of the harness, empty
     *     string if unset, never returns null.
     */
    public static String getHarnessVariety() {
         return getProperty("harness-variety");
    }

    private static File findJavaTestClassDir(PrintStream log) {
        String VERBOSE_CLASSDIR_PROPNAME = "verbose_javatestClassDir";
        String CLASSDIR_PROPNAME = "javatestClassDir";

        boolean verbose = (log == null ? false : Boolean.getBoolean(VERBOSE_CLASSDIR_PROPNAME));
        I18NResourceBundle i18n = (verbose ? I18NResourceBundle.getBundleForClass(ProductInfo.class) : null);

        // javatestClassDir is made available by the harness in the environment
        // so that tests running in other JVM's can access Test, Status etc
        String jc = System.getProperty(CLASSDIR_PROPNAME);
        if (jc != null) {
            File javatestClassDir = new File(new File(jc).getAbsolutePath());
            if (verbose)
            log.println("  " + CLASSDIR_PROPNAME + " = " + javatestClassDir);

            return javatestClassDir;
        }

        try {

            String className = ProductInfo.class.getName();
            String classEntry = ("/" + className.replace('.', '/') + ".class");

            URL url = ProductInfo.class.getResource(classEntry);
            if (url.getProtocol().equals("jar")) {
                String path = url.getPath();
                int sep = path.lastIndexOf("!");
                path=path.substring(0, sep);
                url = new URL(path);
            }
            if (url.getProtocol().equals("file")) {
                if (url.toString().endsWith(classEntry)) {
                    String urlString = url.toString();
                    url = new URL(urlString.substring(0, urlString.lastIndexOf(classEntry)));
                }
                if (verbose && i18n != null) {
                    log.println(i18n.getString("pi.jcd.result", javatestClassDir));
                }
                String defaultEnc = new java.io.InputStreamReader(System.in).getEncoding();
                return new File(URLDecoder.decode(url.getPath(), defaultEnc));
            }
        } catch (java.io.UnsupportedEncodingException ignore) {
        } catch (MalformedURLException ignore) {
        }

        if (verbose && i18n != null) {
            log.println(i18n.getString("pi.jcd.cant"));
        }

        if(i18n == null) {
            // we need initialized i18n for the following exception
            i18n = I18NResourceBundle.getBundleForClass(ProductInfo.class);
        }

        throw new IllegalStateException(i18n.getString("pi.jcd.noInstallDir",
                          new Object[] { VERBOSE_CLASSDIR_PROPNAME, CLASSDIR_PROPNAME }));
    }

    private static String getProperty(String name) {
        if (info == null) {
            info = new Properties();
            try {
                InputStream in = ResourceLoader.getResourceAsStream("/META-INF/buildInfo.txt", ProductInfo.class);
                if (in != null) {
                    info.load(in);
                    in.close();
                }
            }
            catch (IOException ignore) {
                //e.printStackTrace();
            }
        }

        return info.getProperty(name, "unset");
    }

    private static Properties info;
    private static File javatestClassDir;

}
