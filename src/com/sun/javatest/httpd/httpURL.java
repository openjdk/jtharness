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

import java.util.Dictionary;
import java.util.Properties;

/**
 * URL object including support for key-value pairs.
 */

public class httpURL {
    public httpURL(String url) {
        firstQ = url.indexOf('?');
        file = url;
        fileLen = file.length();

        resetIterator();

        if (firstQ != -1) {
            parseURLKeys();
        }
    }

    /**
     * Get the next part of the requested path.  This is an iterator.
     * Ex: from /harness/foo/, it may return "harness" or "foo".
     *
     * @return null if the end of the file path has been reached
     */
    public String getNextFile() {
        // XXX 4 May 2000 rewrite this method so it is not so complex!
        if (pathPos == fileLen) return null;

        String ss;   // the substring of the whole file name
        if (pathPos == fileLen - 1) {   // * special cases
            if (fileLen == 1 && file.charAt(0) == '/') {        // should only happen when full URL is "/"
                ss = "/";
                pathPos = fileLen;
            }
            else {
                ss = file.substring(pathPos, fileLen);
                pathPos = fileLen;
            }
        }
        else {                          // * normal cases
            int nextPos = file.indexOf('/', pathPos+1);

            if (nextPos == -1) {
                // give the remainder of the string up to:
                // 1) the first question mark ahead
                // 2) the end of the string
                String result = file.substring(pathPos, (firstQ == -1 ? fileLen : firstQ));
                pathPos = fileLen;
                ss = result;
            }
            else {
                ss = file.substring(pathPos, nextPos);
                pathPos = nextPos + 1;
            }
        }

        //System.out.println("next file is: " + ss);
        return ss;
    }

    public void resetIterator() {
        // if the URL is only a slash, we let that through
        // otherwise we ignore the leading slash
        // this allows the web server root page to work, eg. http://foo:1903/
        if (file.indexOf('/') == 0 && fileLen > 1)
            pathPos = 1;
        else
            pathPos = 0;
    }

    /**
     * Returns the URL, minus the protocol, hostname and port.
     */
    public String getFullPath() {
        return file;
    }

    /**
     * Get the host portion of the URL.
     *
     * @throws Fault This info is not always available, so this exception may
     *               be thrown.
     */
    public String getLocalHost() throws Fault {
        if (lHost == null)
            throw new Fault("Local hostname for URL not available");
        else
            return lHost;
    }

    /**
     * Get the local hostname portion of the URL.
     *
     * @throws Fault This info is not always available, so this exception may
     *               be thrown.
     */
    public int getLocalPort() throws Fault {
        if (lPort == -1)
            throw new Fault("Local port for URL not available");
        else
            return lPort;
    }

    /**
     * Get the remote hostname.  Fully resolved host names are not required.
     *
     * @throws Fault This info is not always available, so this exception may
     *               be thrown.
     */
    public String getRemoteHost() throws Fault {
        if (rHost == null)
            throw new Fault("Remote hostname for URL not available");
        else
            return rHost;
    }

    // ------- Key-Value Processing ---------
    /**
     * Set the key-value pairs encoded in this URL.
     * Not implemented yet.
     */
    public void setProperties(Dictionary props) {
    }

    /**
     * Not implemented yet.
     */
    public String[] getKeys() {
        return null;
    }

    /**
     * Not implemented yet.
     */
    public String[] getValues() {
        return null;
    }

    /**
     * Not implemented yet.
     */
    public String getValue(String key) {
        return urlValues.getProperty(key);
    }

    // ----------- Utility -----------
    /**
     * Assembles a slash separated path from the elements of the given array.
     * Null entries in the array are ignored.
     *
     * @param path An empty or null array results in a zero length string.
     * @param leadingSlash Whether or not to put a slash at the beginning of the path
     * @param trailSlash Whether or not to put a slash at the end of the path
     */
    public static String reassemblePath(String[] path, boolean leadingSlash,
                                        boolean trailSlash) {
        if (path == null || path.length == 0)
            return "";

        StringBuffer result = new StringBuffer();

        if (leadingSlash) result.append("/");

        for (int i = 0; i < path.length; i++) {
            if (path[i] != null) {
                result.append(path[i]);
                result.append("/");
            }
        }   // for

        // remove trailing slash if needed
        if (!trailSlash && result.length() > 1) result.setLength(result.length()-1);

        return result.toString();
    }

    // ----------- NON-PUBLIC ------------

    void setRemoteHost(String name) {
        rHost = name;
    }

    void setLocalHost(String name) {
        lHost = name;
    }

    void setLocalPort(int port) {
        lPort = port;
    }

    private void parseURLKeys() {
        String data = file.substring(firstQ + 1, fileLen);

        if (data.length() == 0) return;

        String key = null;
        int pos = 0;
        int dataLen = data.length();    // optimization

        while ((key = readSegment(data, pos, dataLen)) != null) {
            pos = pos + key.length();
            if (debug) System.out.println("   Read key: " + key);

            if (pos >= dataLen || data.charAt(pos) == '&') {
                // there is no value with this key
                if (debug) System.out.println("key: " + key + "  value: null");
                urlValues.put(key, "true");
            }
            else {
                // char at pos must be '='
                String val = readSegment(data, ++pos, dataLen);
                if (debug) System.out.println("key: " + key + "  value: " + val);
                urlValues.put(key, val);

                // assume that the resulting value will either jump over
                // the & or put us past the end of the string
                // http://machine1:1904/version?apple=orange&grape=apricot
                //                                         ^
                //                                        pos
                pos = pos + val.length() + 1;

                // end of data string
                if (pos > dataLen) break;
            }
        }   // while
    }

    private String readSegment(String data, int position, int dataLen) {
        StringBuffer buf = new StringBuffer();
        int i = position;

        // loop until you hit end of string, a & or a =
        while (i < dataLen && data.charAt(i) != '&' &&
               data.charAt(i) != '=') {
            buf.append(data.charAt(i));
            i++;
        }   // while

        // a zero length string is not acceptable
        return (buf.length() == 0 ? null : buf.toString());
    }

    private String lHost;
    private int lPort = -1;
    private String rHost;
    private int pathPos;
    private int firstQ;     // location of the first question mark in the file path
    protected static final boolean debug = Boolean.getBoolean("debug." + httpURL.class.getName());

    /**
     * The URL, of the format:
     * <tt>/harness/foo/index.html?key1=value1?key2=value2</tt>
     */
    private String file;

    /**
     * The length of the file string.  Used often.
     */
    private int fileLen;

    /**
     * Any key-values encoded in the URL string.
     */
    private Properties urlValues = new Properties();

    public static class Fault extends Exception {
        public Fault(String s) {
            super(s);
        }

        /**
         * Provide description and pass-thru exception
         */
        public Fault(String s, Throwable e) {
            super(s);
        }

        /**
         * Get the original exception.
         */
        public Throwable getException() {
            return orig;
        }

        private Throwable orig;
    }
}

