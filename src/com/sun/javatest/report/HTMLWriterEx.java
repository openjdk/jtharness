/*
 * $Id$
 *
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.util.HTMLWriter;
import com.sun.javatest.util.I18NResourceBundle;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * A class to facilitate writing HTML via a stream.
 * Extends HTMLWriter and adds some specific functionality
 * which requires newer than JDK 1.1 API
 * The base class, according to the convention, must be compatible with jdk 1.1
 * so such functionality was implemented here outside of com.sun.javatest.util
 * @see com.sun.javatest.util.HTMLWriter
 */
public class HTMLWriterEx extends HTMLWriter {

    /**
     * Create an HTMLWriterEx object, using a default doctype for HTML 3.2.
     *
     * @param out a Writer to which to write the generated HTML
     * @throws IOException if there is a problem writing to the underlying
     * stream
     */
    public HTMLWriterEx(Writer out) throws IOException {
        super(out);
    }

    /**
     * Create an HTMLWriterEx object, using a specified doctype header.
     *
     * @param out a Writer to which to write the generated HTML
     * @param docType a string containing a doctype header for the HTML to be
     * generated
     * @throws IOException if there is a problem writing to the underlying
     * stream
     */
    public HTMLWriterEx(Writer out, String docType) throws IOException {
        super(out, docType);
    }

    /**
     * Create an HTMLWriterEx object, using a specified bundle for localizing
     * messages.
     *
     * @param out a Writer to which to write the generated HTML
     * @param i18n a resource bundle to use to localize messages
     * @throws IOException if there is a problem writing to the underlying
     * stream
     */
    public HTMLWriterEx(Writer out, I18NResourceBundle i18n) throws IOException {
        super(out, i18n);
    }

    /**
     * Create an HTMLWriterEx object, using a specified doctype header and using
     * a specified bundle for localizing messages.
     *
     * @param out a Writer to which to write the generated HTML
     * @param docType a string containing a doctype header for the HTML to be
     * generated
     * @param i18n a resource bundle to use to localize messages
     * @throws IOException if there is a problem writing to the underlying
     * stream
     */
    public HTMLWriterEx(Writer out, String docType, I18NResourceBundle i18n) throws IOException {
        super(out, docType, i18n);
    }

    /**
     * Write Content-Type meta tag using default charset:
     * <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
     * This must go inside the <head> element!
     */
    public void writeContentMeta() throws IOException {
        closePrevTag();
        writeRaw(String.format(META_CONTENT, Charset.defaultCharset()));
    }

    private static final String META_CONTENT = "\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=%s\">\n";
}
