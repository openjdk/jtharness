/*
 * $Id$
 *
 * Copyright (c) 1996, 2019, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.agent;

import java.io.IOException;
import java.io.Writer;

/**
 * Stream passed to the class that is executed on behalf of the client.
 * Data written to the stream is buffered and eventually written back to th
 * client via the Task's sendChars method.
 */
class AgentWriter extends Writer {
    private byte type;
    private Agent.Task parent;
    private char[] buf = new char[1024];
    private int count = 0;

    /**
     * Create a stream that sends its data back to the parent Task.
     *
     * @arg type        A tag to pass back to parent.sendChars().
     * @arg parent      The parent object to which to pass the data written to the stream.
     */
    AgentWriter(byte type, Agent.Task parent) {
        this.type = type;
        this.parent = parent;
    }

    /**
     * Writes a character. This method will block until the character
     * is actually written.
     *
     * @param ch the char
     * @throws IOException If an I/O error has occurred.
     */
    @Override
    public synchronized void write(int ch) throws IOException {
        buf[count++] = (char) ch;
        if (count == buf.length) {
            try {
                parent.sendChars(type, buf, 0, count);
            } finally {
                count = 0;
            }
        }
    }

    /**
     * Writes an array of characters. This method will block until the
     * characters are actually written.
     *
     * @param c the data to be written
     * @throws IOException If an I/O error has occurred.
     */
    @Override
    public void write(char c[]) throws IOException {
        write(c, 0, c.length);
    }

    /**
     * Writes a sub array of characters.
     *
     * @param c   the data to be written
     * @param off the start offset in the data
     * @param len the number of bytes that are written
     * @throws IOException If an I/O error has occurred.
     */
    @Override
    public synchronized void write(char c[], int off, int len) throws IOException {
        if (len < buf.length - count) {
            // there is room for the bytes in the current buffer
            System.arraycopy(c, off, buf, count, len);
            count += len;
        } else {
            // not room in the current buffer, so flush it
            flush();
            if (len < buf.length) {
                // there is _now_ enough room in the current buffer, so use it
                System.arraycopy(c, off, buf, count, len);
                count += len;
            } else {
                // current buffer not big enough; send data directly
                parent.sendChars(type, c, off, len);
            }
        }
    }

    /**
     * Flushes the stream. This will write any buffered
     * output bytes.
     *
     * @throws IOException If an I/O error has occurred.
     */
    @Override
    public synchronized void flush() throws IOException {
        if (count > 0) {
            switch (type) {
                case Agent.LOG:
                    type = Agent.LOG_FLUSH;
                    break;
                case Agent.REF:
                    type = Agent.REF_FLUSH;
                    break;
            }
            try {
                parent.sendChars(type, buf, 0, count);
            } finally {
                count = 0;
            }
        }
    }

    /**
     * Closes the stream. This method must be called
     * to release any resources associated with the
     * stream.
     *
     * @throws IOException If an I/O error has occurred.
     */
    @Override
    public void close() throws IOException {
        flush();
    }
}
