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
package com.sun.javatest.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Note: This class works properly only with encodings, which use simple encoding
 * schemes (such as Unicode). There is no guaranty for encodings with compound or
 * compressing schemes.
 */

/**
 * Note: this class should be JDK 1.1 compatible
 */
public class WriterStream extends OutputStream
{
    /**
      * Create a stream that writes to a writer.
      *
      * @param w the writer
      */
    public WriterStream(Writer w) {
        writer = w;
        OutputStreamWriter osw = new OutputStreamWriter(this);
        charsetName = osw.getEncoding();
    }

    /**
     * Create a stream that writes to a writer.
     *
     * @param w the writer
     * @param charsetName name of encoding to be used when decode byte stream
     * (instead of default one)
     */
    public WriterStream(Writer w, String charsetName) {
        writer = w;
        this.charsetName = charsetName;
    }

    /**
      * Flush the stream.
      *
      * @exception IOException if an I/O error occurs
      */
    public void flush() throws IOException {
        String result = new String(buff, 0, index, charsetName);
        char[] chars = result.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            writer.write(chars[i]);
        }
        index = 0;

        writer.flush();
    }

    /**
     * We override default implementation to write last characters,
     * which could be lost in buffer otherwise.
     *
     * @throws java.io.IOException
     */
    public void close() throws IOException {
        flush();
    }

    /**
     * OutputStream's implementation.
     * Our goal is to convert encoded byte stream form OutputStream to char
     * stream, which can be written using specified writer.
     * We perform this by collecting bytes in buffer and checking with PERIOD_SIZE
     * interval if this buffer represents some char sequence. If so, we write to
     * writer all chars, except last one. We don't write last char, because there
     * still not enough bytes could be collected for it.
     *
     * @param b next byte from OutputStream to write.
     * @throws java.io.IOException
     */
    public void write(int b) throws IOException {

        if (index >= MAX_BUFF_SIZE) {
            flush();
        }

        buff[index++] = (byte) b;

        if (index % PERIOD_SIZE == 0) {
            String res = new String(buff, 0, index, charsetName);
            char[] chars = res.toCharArray();
            int ch_numb = chars.length;

            if (chars.length > 1) {
                int k = 0;
                while (chars.length == ch_numb) {
                    k++;
                    res = new String(buff, 0, index - k, charsetName);
                    chars = res.toCharArray();

                    if (chars.length < ch_numb) {
                        System.arraycopy(buff, index - k, buff, 0, k);
                        index = k;
                        for (int i = 0; i < chars.length; i++) {
                            writer.write(chars[i]);
                        }
                        break;
                    }
                }
            }
        }
    }

    private String charsetName;

    /**
     * We hope there is no encoding, where one unicode char encodes in more than
     * 50 bytes.
     */
    private static int MAX_BUFF_SIZE = 50;

    /**
     * Number of writings in the buffer, after which we check it's content.
     * Have an influence on performance of our writing algorithm.
     * 3 is reasonable number for popular encodings, where ASCII symbols encoded
     * using 1 byte; better than 2.
     */
    private static int PERIOD_SIZE = 3;

    private byte[] buff = new byte[MAX_BUFF_SIZE];
    private int index = 0;
    private Writer writer;

}
