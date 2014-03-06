/*
 * $Id$
 *
 * Copyright (c) 2002, 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A class to facilitate writing XML via a stream.
 */
public class XMLWriter
{
    /**
     * Create an XMLWriter object, using a default header.
     * @param out a Writer to which to write the generated XML
     * @throws IOException if there is a problem writing to the underlying stream
     */
    public XMLWriter(Writer out) throws IOException {
        this(out, "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
    }

    /**
     * Create an XMLWriter object, using a specified doctype header.
     * @param out a Writer to which to write the generated XML
     * @param docType a string containing a doctype header for the XML to be generated
     * @throws IOException if there is a problem writing to the underlying stream
     */
    public XMLWriter(Writer out, String docType) throws IOException {
        if (out instanceof BufferedWriter)
            this.out = (BufferedWriter) out;
        else
            this.out = new BufferedWriter(out);
        this.out.write(docType);
        this.out.newLine();
    }

    /**
     * Create an XMLWriter object, using a specified bundle for l0calizing messages.
     * @param out a Writer to which to write the generated XML
     * @param i18n a resource bundle to use to localize messages
     * @throws IOException if there is a problem writing to the underlying stream
     */
    public XMLWriter(Writer out, I18NResourceBundle i18n) throws IOException {
        this(out);
        this.i18n = i18n;
    }


    /**
     * Create an XMLWriter object, using a specified doctype header and
     * using a specified bundle for localizing messages.
     * @param out a Writer to which to write the generated XML
     * @param docType a string containing a doctype header for the XML to be generated
     * @param i18n a resource bundle to use to localize messages
     * @throws IOException if there is a problem writing to the underlying stream
     */
    public XMLWriter(Writer out, String docType, I18NResourceBundle i18n) throws IOException {
        this(out, docType);
        this.i18n = i18n;
    }

    /**
     * Set the resource bundle to be used for localizing messages.
     * @param i18n the resource bundle to be used for localizing messages
     */
    public void setI18NResourceBundle(I18NResourceBundle i18n) {
        this.i18n = i18n;
    }

    /**
     * Flush the stream, and the underlying output stream.
     * @throws IOException if there is a problem writing to the underlying stream
     */
    public void flush() throws IOException {
        out.flush();
    }

    /**
     * Close the stream, and the underlying output stream.
     * @throws IOException if there is a problem closing the underlying stream
     */
    public void close() throws IOException {
        out.close();
    }

    /**
     * Write a newline to the underlying output stream.
     * @throws IOException if there is a problem closing the underlying stream
     */
    public void newLine() throws IOException {
        if (state == IN_TAG) {
            out.write(">");
            state = IN_BODY;
        }
        out.newLine();
    }

    /**
     * Start an XML tag.  If a prior tag has been started, it will
     * be closed first. Once a tag has been opened, attributes for the
     * tag may be written out, followed by body content before finally
     * ending the tag.
     * @param tag the tag to be started
     * @throws IOException if there is a problem closing the underlying stream
     * @see #writeAttr
     * @see #write
     * @see #endTag
     */
    public void startTag(String tag) throws IOException {
        if (state == IN_TAG) {
            out.write(">");
            state = IN_BODY;
        }
        //newLine();
        out.write("<");
        out.write(tag);
        state = IN_TAG;
    }

    /**
     * Finish an XML tag. It is expected that a call to endTag will match
     * a corresponding earlier call to startTag, but there is no formal check
     * for this.
     * @param tag the tag to be closed.
     * @throws IOException if there is a problem closing the underlying stream
     */
    public void endTag(String tag) throws IOException {
        if (state == IN_TAG) {
            out.write("/>");
            state = IN_BODY;
        }
        else {
            out.write("</");
            out.write(tag);
            out.write(">");
        }
        //out.newLine();
    }

    /**
     * Write an attribute for a tag. A tag must previously have been started.
     * All tag attributes must be written before any body text is written.
     * The value will be quoted if necessary when writing it to the underlying
     * stream. No check is made that the attribute is valid for the current tag.
     * @param name the name of the attribute to be written
     * @param value the value of the attribute to be written
     * @throws IllegalStateException if the stream is not in a state to
     * write attributes -- e.g. if this call does not follow startTag or other
     * calls of writteAttr
     * @throws IOException if there is a problem closing the underlying stream
     */
    public void writeAttr(String name, String value) throws IOException {
        if (state != IN_TAG)
            throw new IllegalStateException();

        out.write(" ");
        out.write(name);
        out.write("=");
        out.write("\"");
        out.write(value);  // should check for " ?
        out.write("\"");
    }

    /**
     * Write a line of text, followed by a newline.
     * The text will be escaped as necessary.
     * @param text the text to be written.
     * @throws IOException if there is a problem closing the underlying stream
     */
    public void writeLine(String text) throws IOException {
        write(text);
        out.newLine();
    }

    /**
     * Write a formatted date.
     * @param millis date represented in milliseconds
     * @throws IOException if a exception occurs during writing
     * @see java.util.Date
     */
    public void writeDate(long millis) throws IOException {
        writeDate(new Date(millis));
    }

    /**
     * Write a formatted date surrounded by a markup tag.
     * @param tag tag to open and close before and after the date is inserted
     * @param millis date represented in milliseconds
     * @throws IOException if a exception occurs during writing
     * @see java.util.Date
     */
    public void writeDate(String tag, long millis) throws IOException {
        writeDate(tag, new Date(millis));
    }

    /**
     * Write a formatted date.
     * @param date the date to print
     * @throws IOException if a exception occurs during writing
     */
    public void writeDate(Date date) throws IOException {
        if (dateFormatter == null)
            dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        write(dateFormatter.format(date));
    }

    /**
     * Write a formatted date surrounded by a markup tag.
     * @param tag tag to open and close before and after the date is inserted
     * @param date the date to print
     * @throws IOException if a exception occurs during writing
     * @see java.util.Date
     */
    public void writeDate(String tag, Date date) throws IOException {
        startTag(tag);
        writeDate(date);
        endTag(tag);
    }

    /**
     * Write body text, escaping it as necessary.
     * If this call follows a call of startTag, the open tag will be
     * closed -- meaning that no more attributes can be written until another
     * tag is started.
     * @param text the text to be written
     * @throws IOException if there is a problem closing the underlying stream
     */
    public void write(String text) throws IOException {
        if (text.length() == 0)
            return;

        if (state == IN_TAG) {
            out.write(">");
            state = IN_BODY;
        }

        // check to see if there are any special characters
        boolean specialChars = false;
        for (int i = 0; i < text.length() && !specialChars; i++) {
            switch (text.charAt(i)) {
            case '<': case '>': case '&':
                specialChars = true;
            }
        }

        // if there are special characters write the string character at a time;
        // otherwise, write it out as is
        if (specialChars) {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                switch (c) {
                case '<': out.write("&lt;"); break;
                case '>': out.write("&gt;"); break;
                case '&': out.write("&amp;"); break;
                default: out.write(c);
                }
            }
        }
        else
            out.write(text);
    }

    /**
     * Write a localized message, using a specified resource bundle.
     * @param i18n the resource bundle used to localize the message
     * @param key the key for the message to be localized
     * @throws IOException if there is a problem closing the underlying stream
     */
    public void write(I18NResourceBundle i18n, String key) throws IOException {
        write(i18n.getString(key));
    }

    /**
     * Write a localized message, using a specified resource bundle.
     * @param i18n the resource bundle used to localize the message
     * @param key the key for the message to be localized
     * @param arg an argument to be formatted into the localized message
     * @throws IOException if there is a problem closing the underlying stream
     */
    public void write(I18NResourceBundle i18n, String key, Object arg) throws IOException {
        write(i18n.getString(key, arg));
    }

    /**
     * Write a localized message, using a specified resource bundle.
     * @param i18n the resource bundle used to localize the message
     * @param key the key for the message to be localized
     * @param args arguments to be formatted into the localized message
     * @throws IOException if there is a problem closing the underlying stream
     */
    public void write(I18NResourceBundle i18n, String key, Object[] args) throws IOException {
        write(i18n.getString(key, args));
    }

    /**
     * Write a localized message, using the default resource bundle.
     * @param key the key for the message to be localized
     * @throws IOException if there is a problem closing the underlying stream
     */
    public void writeI18N(String key) throws IOException {
        write(i18n.getString(key));
    }

    /**
     * Write a localized message, using the default resource bundle.
     * @param key the key for the message to be localized
     * @param arg an argument to be formatted into the localized message
     * @throws IOException if there is a problem closing the underlying stream
     */
    public void writeI18N(String key, Object arg) throws IOException {
        write(i18n.getString(key, arg));
    }

    /**
     * Write a localized message, using the default resource bundle.
     * @param key the key for the message to be localized
     * @param args arguments to be formatted into the localized message
     * @throws IOException if there is a problem closing the underlying stream
     */
    public void writeI18N(String key, Object[] args) throws IOException {
        write(i18n.getString(key, args));
    }

    private BufferedWriter out;
    private int state;
    private I18NResourceBundle i18n;
    private DateFormat dateFormatter;
    private static final int IN_TAG = 1;
    private static final int IN_BODY = 2;
}
