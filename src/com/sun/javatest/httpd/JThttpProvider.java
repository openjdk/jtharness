/*
 * $Id$
 *
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.io.PrintWriter;

import com.sun.javatest.util.DynamicArray;

/**
 * The JT Harness web server will delegate handling of requested URLs to
 * registered handler classes which implement this interface.
 */

public abstract class JThttpProvider {
    /**
     * The web server is forwarding the given URL to you for processing.
     *
     * @param url The URL that the client requested.
     * @param out Send raw HTML text to this stream which will be delivered to
     *            the requestor.  Output should start at the very beginning
     *            of the document - the content type description.  Close the
     *            stream when you are done.
     */
    public abstract void serviceRequest(httpURL url, PrintWriter out);

    /**
     * Get the primary URL for this handler.  This is usually the index page
     * for this object.  If this is not overriden by subclasses, it defaults
     * to the first registered URL.
     *
     * @return Null if it is not possible to determine any URLs to this object.
     */
    public String getRootURL() {
        if (regURLs != null && regURLs.length > 0)
            return regURLs[0];
        else
            return null;
    }

    /**
     * Find out which URLs this handler is registered for.
     *
     * @return The array will never be null; it may be zero length though.
     */
    public String[] getRegisteredURLs() {
        String[] cp = new String[regURLs.length];
        System.arraycopy(regURLs, 0, cp, 0, regURLs.length);
        return cp;
    }

//  ------ non-public methods -------
    /**
     * Prints the supplied text to the writer, after filtering and replacing
     * characters which need to be "escaped" in HTML.
     */
    protected static void print(PrintWriter out, String str) {
        if (str == null) return;

        out.print(filterTags(str));
    }

    /**
     * Prints the supplied text to the writer, after filtering and replacing
     * characters which need to be "escaped" in HTML.
     */
    protected static void println(PrintWriter out, String str) {
        if (str == null) return;

        out.println(filterTags(str));
    }

    protected static String filterTags(String original) {
        // this code from javasoft.sqe.html.htmlFilter.java
        int beginIndex = 0;        // substring starting point
        int currIndex = 0;         // current position in original string
        char currChar;         // current char being checked
        StringBuffer workStr;

        if (original == null) {
            return null;
        }

        workStr = new StringBuffer(original);

        while (currIndex < workStr.length()) {
            currChar = workStr.charAt(currIndex);
            if (currChar == '<') {
                workStr.setCharAt(currIndex, '&');
                workStr.insert(currIndex+1, "lt;");
                beginIndex = currIndex + 4;
                currIndex+= 3;
            } else if(currChar == '>') {
                workStr.setCharAt(currIndex, '&');
                workStr.insert(currIndex+1, "gt;");
                beginIndex = currIndex + 4;
                currIndex+= 3;
            }

            currIndex++;
        }   // while

        return workStr.toString();
    }

    void addRegistredURL(String url) {
        if (url != null) {
            regURLs = (String[])DynamicArray.append(regURLs, url);
            if (debug) System.out.println("PROV-registered URL (" + url + ") in " + this);
        }
    }

    void removeRegisteredURL(String url) {
        for (int i = 0; i < regURLs.length; i++) {
            if (regURLs[i].equals(url)) {
                regURLs = (String[])DynamicArray.remove(regURLs, i);
                break;
            }
        }   // for
    }

    private String[] regURLs = new String[0];
    protected static boolean debug = Boolean.getBoolean("debug." + JThttpProvider.class.getName());;
}

