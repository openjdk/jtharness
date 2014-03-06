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
import java.text.DateFormat;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.TimeZone;

import com.sun.javatest.util.I18NResourceBundle;

/**
 * Utility methods to make it easier to generate HTTP/HTML.
 */

public class PageGenerator {
    /**
     * Generate the correct HTTP header for a sucessful request (200)
     */
    public static void generateOkHttp(PrintWriter out) {
        out.println(HTTP_OK);
        genServerHdr(out);
        out.println(HTTP_CONTENT_HTML);
        out.println();
    }

    /**
     * Generate the correct HTTP header for a bad request (400)
     */
    public static void generateBadHttp(PrintWriter out) {
        out.println(HTTP_BAD);
        genServerHdr(out);
        out.println(HTTP_CONTENT_HTML);
    }

    /**
     * Generate the content type header line.
     *
     * @param version The HTML spec. version you wish to use.
     */
    public static void generateDocType(PrintWriter out, int version) {
        switch (version) {
            case HTML32: out.println(HTML32_ID); break;
            case HTML40_TRANS: out.println(HTML40_TRANS_ID); break;
        }
    }

    /**
     * Creates a plain header, with only a title.
     */
    public static void writeHeader(PrintWriter out, String title) {
        out.println ("<Head>");
        out.print ("<Title>");
        out.print (title);
        out.println ("</Title>");
        out.println ("</Head>");
    }

    public static void writeBeginDoc(PrintWriter out) {
        out.println("<html>");
    }

    /**
     * Writes the address info
     */
    public static void writeEndDoc(PrintWriter out) {
        out.println("</html>");
    }

    public static void startBody(PrintWriter out) {
        out.println("<Body>");
    }

    public static void endBody(PrintWriter out) {
        out.println("</Body>");
    }

    /**
     * Writes the address block and other info at the end of the body.
     */
    public static void writeFooter(PrintWriter out) {
        out.println(dateFormat.format(new Date()));
        out.println("<Address>");

        out.print(i18n.getString("generator.produced.txt"));
        out.print(swName);
        out.print(" ");
        out.println(swVersion);

        out.print(i18n.getString("generator.built.txt"));
        out.println(swBuildDate);
        out.println("</Address>");
    }

    /**
     * Prints the contents of any dictionary in a two column table.
     */
    public static void writeDictionary(PrintWriter out, Dictionary dict,
                                       String keyHeader, String valHeader) {
        // XXX should include HTML filtering of strings

        if (keyHeader == null) keyHeader = "Key";
        if (valHeader == null) valHeader = "Value";

        out.println("<Table Border>");

        StringBuffer buf = new StringBuffer(50);

        // write the table header
        buf.append("<tr><th>");
        buf.append(keyHeader);
        buf.append("<th>");
        buf.append(valHeader);
        buf.append("</tr>");
        out.println(buf.toString());

        if (dict == null || dict.size() == 0) {
            // no values to write, fill the space
            buf.setLength(0);
            buf.append("<tr><td colspan=2>");
            buf.append("-EMPTY-");
            buf.append("</tr>");
        }
        else {
            Enumeration keys = dict.keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                out.println("<tr>");
                buf.setLength(0);
                buf.append("<td>");
                buf.append(key.toString());
                buf.append("<td>");
                buf.append((dict.get(key)).toString());
                out.println(buf.toString());
                out.println("</tr>");
            }   // while
        }

        out.println("</Table>");
    }

    public static void startTable(PrintWriter out, boolean borders) {
        out.print("<Table");
        if (borders) out.print(" Border");

        out.println(">");
    }

    public static void endTable(PrintWriter out) {
        out.println("</Table>");
    }

    public static String getSWBuildDate() {
        return swBuildDate;
    }

    public static String getSWName() {
        return swName;
    }

    public static String getSWVersion() {
        return swVersion;
    }

    // --- set environment info ---
    public static void setSWBuildDate(String date) {
        swBuildDate = date;
    }

    public static void setSWName(String name) {
        swName = name;
    }

    public static void setSWVersion(String ver) {
        swVersion = ver;
    }

// *********** PRIVATE ***************

    private static void genServerHdr(PrintWriter out) {
        if (swName != null) {
            out.print("Server: ");
            out.print(swName);
            if (swVersion != null) {
                out.print("/");
                out.print(swVersion);
            }

            if (swBuildDate != null) {
                out.print("  built ");
                out.print(swBuildDate);
            }
        }

        out.println();
        out.print("Date: ");
        out.println(dateFormat.format(new Date()));
    }

    private static DateFormat dateFormat;
    private static String swBuildDate;
    private static String swName;
    private static String swVersion;
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(PageGenerator.class);

    private static final String TIMEZONE = "UTC";

    public static final int HTML32 = 0;
    public static final int HTML40_TRANS = 1;

    private static final String HTML32_ID = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//EN\">";
    private static final String HTML40_TRANS_ID = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">";

    private static final String HTTP_CONTENT_TYPE = "Content-Type: ";
    private static final String HTTP_OK = "HTTP/1.1 200 OK";
    private static final String HTTP_BAD = "HTTP/1.1 400 Bad Request";
    private static final String HTTP_CONTENT_HTML = HTTP_CONTENT_TYPE + "text/html";

    static {
        dateFormat = DateFormat.getDateTimeInstance(DateFormat.FULL,
                                                    DateFormat.LONG);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
    }

}

