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
package com.sun.javatest.util;


/**
 * A simple variable length first-in first-out queue.
 */

public class Fifo
{
    /**
     * Create a buffer with a default initial size.
     */
    public Fifo() {
        this(defaultInitialSlots);
    }

    /**
     * Create a buffer with a specified initial size.
     *
     * @param initialSlots      The number of initial slots in the buffer; the number of
     *                          slots required by the buffer will be expanded as required.
     */
    public Fifo(int initialSlots) {
        bufSize = initialSlots;
        buf = new Object[bufSize];
        insertSlot = 0;
        removeSlot = 0;
        entries = 0;
    }

    /**
     * Check if the buffer has an entries or not.
     *
     * @return                  true if the buffer has no entries, and false otherwise.
     */
    public synchronized boolean isEmpty() {
        return (entries == 0);
    }

    /**
     * Return the number of entries currently in the fifo.
     *
     * @return                  The number of entries currently in the fifo
     */
    public synchronized int size() {
        return entries;
    }

    /**
     * Insert an entry into the buffer. The buffer will be increased in size if necessary
     * to accommodate the new entry.
     *
     * @param obj               The object to be inserted.  It must not be null.
     */
    public synchronized void insert(Object obj) {
        if (obj == null)
            throw new NullPointerException();

        if (entries == bufSize) {
            int newBufSize = 2 * bufSize;
            Object[] newBuf = new Object[newBufSize];
            int saveEntries = entries;
            for (int i = 0; entries > 0; i++) {
                newBuf[i] = remove();
            }
            entries = saveEntries;
            buf = newBuf;
            bufSize = newBufSize;
            removeSlot = 0;
            insertSlot = entries;
        }

        buf[insertSlot] = obj;
        insertSlot = (insertSlot + 1) % bufSize;
        entries++;
    }

    /**
     * Remove an entry from the buffer if one is available.
     *
     * @return                  The next object in line to be removed, if one is available,
     *                          or null if none are available.
     */
    public synchronized Object remove() {
        if (entries == 0)
            return null;

        Object o = buf[removeSlot];
        buf[removeSlot] = null;
        removeSlot = (removeSlot + 1) % bufSize;
        entries--;

        return o;
    }

    /**
     * Flush all entries from the buffer.
     */
    public synchronized void flush() {
        insertSlot = 0;
        removeSlot = 0;
        entries = 0;
        for (int i = 0; i < buf.length; i++)
            buf[i] = null;
    }


    //----------Data members---------------------------------------------------------

    private final static int defaultInitialSlots = 16;

    private Object[] buf;               // The circular array to hold the entries
    private int bufSize;                // The size of the array: buf.length
    private int insertSlot;             // The next slot to store an entry
    private int removeSlot;             // The next slot from which to remove an entry
    private int entries;                // The number of entries in the array

}
