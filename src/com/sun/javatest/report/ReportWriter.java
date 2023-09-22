/*
 * $Id$
 *
 * Copyright (c) 2001, 2015, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.report;

import com.sun.javatest.ProductInfo;
import com.sun.javatest.util.I18NResourceBundle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;

class ReportWriter extends HTMLWriterEx {
    private static final String BODY = "body";
    private static final String FONT_SIZE = "font-size";
    private static final String FONT_FAMILY = "font-family";
    private static final String BGCOLOR = "bgcolor";
    private static final String MARGIN_LEFT = "margin-left";
    private static final String SANSSERIF = "SansSerif";
    private static final String WHITE = "white";
    private static final String _12PT = "12pt";
    private static final String CSS_FILENAME = "report.css";
    private I18NResourceBundle i18n;

    ReportWriter(Writer out) throws IOException {
        super(out);
    }

    ReportWriter(Writer out, I18NResourceBundle i18n) throws IOException {
        super(out, i18n);
        this.i18n = i18n;
    }

    /**
     * Creates a new ReportWriter.
     *
     * @param out the output stream
     */
    ReportWriter(Writer out, String title, I18NResourceBundle i18n)
            throws IOException {
        this(out, title, i18n, Charset.defaultCharset());
    }
    ReportWriter(Writer out, String title, I18NResourceBundle i18n, Charset cs)
            throws IOException {
        super(out, "<!DOCTYPE HTML>", i18n);
        this.i18n = i18n;

        startTag(HTMLWriterEx.HTML);
        writeAttr("lang", i18n.getString("html.lang"));
        startTag(HTMLWriterEx.HEAD);
        writeContentMeta(cs);
        startTag(HTMLWriterEx.TITLE);
        writeI18N("reportWriter.product.name", ProductInfo.getName(), title);
        endTag(HTMLWriterEx.TITLE);
        writeStyle();
        endTag(HTMLWriterEx.HEAD);
        startTag(HTMLWriterEx.BODY);
        startTag(HTMLWriterEx.H1);
        writeI18N("reportWriter.product.name", ProductInfo.getName(), title);
        endTag(HTMLWriterEx.H1);
    }

    public static void initializeDirectory(File dir) throws IOException {
        File cssFile = new File(dir, CSS_FILENAME);

        if (dir.exists() && dir.canWrite() && !cssFile.exists()) {
            Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cssFile), StandardCharsets.UTF_8));

            fw.write("h1 {font-size: 18pt;\n");
            fw.write("      font-family: SansSerif;\n");
            fw.write("      bgcolor: white;\n");
            fw.write("      margin-left: 3}\n");

            fw.write("h2 {font-size: 15pt;\n");
            fw.write("      font-family: SansSerif;\n");
            fw.write("      bgcolor: white;\n");
            fw.write("      margin-left: 3}\n");

            fw.write("body , h3 {font-size: 12pt;\n"); //12
            fw.write("           font-family: SansSerif;\n");
            fw.write("           bgcolor: white;\n");
            fw.write("           margin-left: 3}\n");

            fw.close();
        }
    }

    @Override
    public void close() throws IOException {
        Date now = new Date();
        String name = ProductInfo.getName();
        String version = ProductInfo.getVersion();
        String buildNumber = ProductInfo.getBuildNumber();

        // for i18n
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
        Date date = ProductInfo.getBuildDate();
        String build_date;
        if (date != null) {
            build_date = df.format(date);
        } else if (i18n != null) {
            build_date = i18n.getString("reportWriter.noDate");
        } else {
            build_date = "unknown";     // last resort
        }

        String build_version = ProductInfo.getBuildJavaVersion();

        //startTag(HTMLWriterEx.P);
        //endTag(HTMLWriterEx.P);
        startTag(HTMLWriterEx.HR);
        startTag(HTMLWriterEx.SMALL);
        writeI18N("reportWriter.generatedOn", now);
        startTag(HTMLWriterEx.BR);
        writeI18N("reportWriter.productInfo", name, version, buildNumber, build_date, build_version);
        endTag(HTMLWriterEx.SMALL);
        endTag(HTMLWriterEx.BODY);
        endTag(HTMLWriterEx.HTML);

        super.flush();
        super.close();
    }

    void writeStyle() throws IOException {
        startTag(HTMLWriterEx.LINK);
        writeAttr(HTMLWriterEx.REL, "stylesheet");
        writeAttr(HTMLWriterEx.HREF, "report.css");
        writeAttr(HTMLWriterEx.TYPE, "text/css");
        endEmptyTag(HTMLWriterEx.LINK);
    }

    void writeStyleSheetProperty(String key, String value, boolean separator) throws IOException {
        if (separator) {
            writeI18N("reportWriter.keyValue.separator",
                    key, value);
        } else {
            writeI18N("reportWriter.keyValue",
                    key, value);
        }
        newLine();
    }

    void writeTD(String body) throws IOException {
        startTag(HTMLWriterEx.TD);
        write(body);
        endTag(HTMLWriterEx.TD);
    }

    void writeTH(String heading) throws IOException {
        writeTH(heading, null);
    }

    void writeTH(String heading, String scope) throws IOException {
        startTag(HTMLWriterEx.TH);
        if (scope != null) {
            writeAttr(HTMLWriterEx.SCOPE, scope);
        }
        write(heading);
        endTag(HTMLWriterEx.TH);
    }

    /**
     * Write a warning, writing the warning text in red.
     *
     * @param text the warning text to be written
     * @throws IOException if there is a problem closing the underlying stream
     */
    void writeWarning(String text) throws IOException {
        startTag(FONT);
        writeAttr(COLOR, i18n.getString("reportWriter.warn.clr"));
        write(text);
        endTag(FONT);
    }
}
