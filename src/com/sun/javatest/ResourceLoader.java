/*
 * $Id$
 *
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import com.sun.javatest.util.DynamicArray;

public class ResourceLoader {

    public static Enumeration getResources(String name, Class ownClass) throws IOException  {
        URL extResource = getExtResource(name, null);
        if (extResource != null) {
            Vector r = new Vector();
            r.add(extResource);
            return r.elements();
        }
        return ownClass.getClassLoader().getResources(name);
    }

    public static InputStream getResourceAsStream(String name, Class ownClass) {
        URL url = getExtResource(name, ownClass);
        try {
            if (url != null) {
                return url.openStream();
            } else {
                return ownClass.getResourceAsStream(name);
            }
        } catch (IOException e) {
            return null;
        }
    }

    public static File getResourceFile(String name, Class ownClass) {
        File f = getExtResourceFile(name, ownClass);
        if (f != null) {
            return f;
        } else {
            return new File(ownClass.getResource(name).getFile());
        }
    }

    public static URL getExtUrl(File filename) {
        URL url;
        File f = filename;
        try {
            if (!f.isAbsolute()) {
                f = new File(ResourceLoader.getExt().getAbsoluteFile(),
                            filename.getPath());
            }

            url = f.toURI().toURL();
        } catch (MalformedURLException e2) {
            url = null;
        }   // catch

        return url;
    }

    private static URL getExtResource(String name, Class ownClass) {
        URL ret = null;
        File rf = getExtResourceFile(name, ownClass);
        if (rf != null) {
            try {
                ret = rf.toURI().toURL();
            } catch (MalformedURLException ex) {
                // it's ok
            }
        }
        return ret;
    }

    private static File getExtResourceFile(String name, Class ownClass) {
        if (ext != null) {
            String relName;
            if (ownClass == null) {
                relName = name;
            } else {
                relName = resolveName(name, ownClass);
            }

            File resFile = new File(getExt(), relName);
            if (resFile.exists()) {
                return resFile;
            }
        }
        return null;
    }


    // get from java.lang.Class with minimal changes
    private static String resolveName(String name, Class baseClass) {
        if (name == null || baseClass == null) {
            return name;
        }

        if (!name.startsWith("/")) {
            String baseName = baseClass.getName();
            int index = baseName.lastIndexOf('.');
            if (index != -1) {
                name = baseName.substring(0, index).replace('.', '/')
                    +"/"+name;
            }
        } else {
            name = name.substring(1);
        }
        return name;
    }

    private static final String EXT_DIR_NAME = "jtExt";
    private static File ext = null;

    static {
        String jcp = System.getProperty("java.class.path");
        String psep = System.getProperty("path.separator");
        File altRoot = null;
        if (jcp != null && psep != null) {
            StringTokenizer tokenizer = new StringTokenizer(jcp, psep);
            while (tokenizer.hasMoreTokens()) {
                // component is javatest.jar (or similar jar) or
                // build/classes in case of netbeans project
                File component = new File(tokenizer.nextToken());
                if (component.exists()) {
                    if (component.isDirectory()) {
                        altRoot = component;
                    } else {
                        altRoot = component.getParentFile();
                    }
                    File extTmp = new File(altRoot, EXT_DIR_NAME);
                    if (extTmp.exists()) {
                        ext = extTmp;
                        break;
                    }
                }
            }
        }
    }

    static File getExt() {
        return ext;
    }

    static void setExt(File aExt) {
        ext = aExt;
    }

    public static ResourceBundle getBundle(String name, Locale aDefault, ClassLoader classLoader) {
        if (ext != null) {
            initAltClassLoader();
            ResourceBundle altB = ResourceBundle.getBundle(name, aDefault, altClassLoader);
            if (altB.getKeys().hasMoreElements()) {
                return altB;
            }
        }
        return ResourceBundle.getBundle(name, aDefault, classLoader);
    }

    public static ResourceBundle getBundle(String name) {
        if (ext != null) {
            initAltClassLoader();
            ResourceBundle altB = ResourceBundle.getBundle(name, Locale.getDefault(), altClassLoader);
            if (altB.getKeys().hasMoreElements()) {
                return altB;
            }
        }
        return ResourceBundle.getBundle(name);
    }

    private static ClassLoader altClassLoader;

    private synchronized static void initAltClassLoader() {
        if (ext != null && altClassLoader == null) {
            try {
                altClassLoader = new URLClassLoader(new URL[]{ext.toURI().toURL()});
            } catch (MalformedURLException ex) {
                // nothing
            }
        }
    }


}
