/*
 * $Id$
 *
 * Copyright (c) 1996, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.httpd;

import java.util.Hashtable;
import java.io.PrintWriter;

import com.sun.javatest.util.I18NResourceBundle;

/**
 * Root level public registry for JT Harness classes which wish to publish pages
 * for the http server.
 */

public class RootRegistry extends ProviderRegistry {
    private RootRegistry() {
        // to prevent instantiation
        httpHandle = new SpecialURLHandler();
        addHandler("/version", "Version information handler", httpHandle);
    }

    /**
     * The root registry is a Singleton.
     */
    public static RootRegistry getInstance() {
        if (myInstance == null)
            myInstance = new RootRegistry();

        return myInstance;
    }

    public void addHandler(String url, String descrip, JThttpProvider obj) {
        if (!url.equals("/"))
            super.addHandler(url, descrip, obj);
        else {
            // special case for a normally invalid url
            if (debug) {
                System.out.println("RPR-Adding Handler: " + descrip);
                System.out.println("   RPR-Adding URL: " + url);
                System.out.println("   RPR-Adding OBJ: " + obj);
            }

            String[] path = {url};
            insertHandler(path, descrip, obj, false);
        }
    }

    /**
     * Given a specific object, find an associated handler.
     * This only works if an associateObject() has been done for the specified
     * object.
     *
     * @param what The object to lookup.
     * @return The object's handler, or null if none if registered.
     */
    public static JThttpProvider getObjectHandler(Object what) {
        return (JThttpProvider)(obj2prov.get(what));
    }

    public static void associateObject(Object what, JThttpProvider prov) {
        if (what != null && prov != null)
            obj2prov.put(what, prov);
    }

    public static void unassociateObject(Object what, JThttpProvider prov) {
        if (what != null && prov != null) {
            Object found = obj2prov.get(what);
            if (found == prov)
                obj2prov.remove(what);
            else
                throw new IllegalArgumentException(
                    "RR-Unable to unassociateObject, providers do not match.");
        }
    }

    private static RootRegistry myInstance = new RootRegistry();
    private SpecialURLHandler httpHandle;

    /**
     * Maps an object reference to a provider.
     * Good for associating a first-class JT Harness object with a provider.
     * For example, map a TestResultTable instance to it's HTTP handler.
     */
    protected static final Hashtable obj2prov = new Hashtable();
    private static final I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(RootRegistry.class);

    static {
        String title = i18n.getString("root.name");
        myInstance.addHandler("/", title, new IndexHandler(myInstance.url2prov));
        myInstance.addHandler("/index.html", title, new IndexHandler(myInstance.url2prov));
    }

    private static class SpecialURLHandler extends JThttpProvider {
        public void serviceRequest(httpURL url, PrintWriter out) {
            url.resetIterator();
            String target = url.getNextFile();

            if (target.equals("version")) {
                printVersionInfo(out);
            }

            out.close();
        }

        private void printVersionInfo(PrintWriter out) {
            println(out, PageGenerator.getSWName() + " " + PageGenerator.getSWVersion());
            print(out, i18n.getString("root.built.txt"));
            println(out, PageGenerator.getSWBuildDate());
        }
    }
}

