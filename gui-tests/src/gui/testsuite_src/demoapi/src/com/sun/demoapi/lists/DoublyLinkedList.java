/*
 * $Id$
 *
 * Copyright (c) 2002, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.demoapi.lists;

/**
 * Simple linked lists of objects.
 * A list is represented by a series of Entry objects, each containing
 * an item of client data, and a link to the next entry in the list.
 * The list keeps references to the first and last entry in the list.
 */

// Note: this class is purely provided to be the basis of some
// examples for writing a testsuite. The code has been written with
// simplicity in mind, rather than efficiency, and may contain
// deliberate coding errors. For proper support for linked lists,
// see the classes in java.util.

public class DoublyLinkedList {
    private Entry first;
    private Entry last;

    /**
     * Create an empty linked list.
     */
    public DoublyLinkedList() {
    }

    /**
     * Determine if a linked list is empty.
     *
     * @return true if the list has no entries, and false otherwise.
     */
    public boolean isEmpty() {
        return (first == null);
    }

    /**
     * Determine if the list contains an entry with a specific item of
     * client data.
     *
     * @return true if the list contains an entry that matches the
     * given client data, and false otherwise.
     */
    public boolean contains(Object data) {
        for (Entry e = first; e != null; e = e.next) {
            if (e.data == data)
                return true;
        }
        return false;
    }


    /**
     * Get the first entry in the list.
     *
     * @return the first entry in the list
     * @see Entry#getNext
     */
    public Entry getFirst() {
        return first;
    }

    /**
     * Get the last entry in the list.
     *
     * @return the first entry in the list
     * @see Entry#getNext
     */
    public Entry getLast() {
        return last;
    }

    /**
     * Insert a new entry containing the specified client data
     * at the beginning of the list.
     *
     * @param data the client data for the new entry
     */
    public void insert(Object data) {
        Entry e = new Entry(data, null, first);
        if (first == null)
            last = e;
        else
            first.prev = e;
        first = e;
    }

    /**
     * Append an entry containing the specified client data
     * to the end of the list.
     */
    public void append(Object o) {
        Entry e = new Entry(o, last, null);
        if (first == null)
            first = e;
        else
            last.next = e;
        last = e;
    }

    /**
     * Remove the first entry from the list that contains the
     * specified client data.
     *
     * @param data The client data indicating the entry to be removed
     * @return true if an entry was found and removed that contained
     * the specified client data, and false otherwise.
     */
    public boolean remove(Object data) {
        for (Entry e = first, prev = null; e != null; prev = e, e = e.next) {
            if (e.data == data) {
                e.remove();
                return true;
            }
        }

        // not found
        return false;
    }

    /**
     * Check if the contents of this list match another.
     *
     * @param other An object to be checked for equality with this one
     * @return true if the other object is a linked list, and corresponding
     * entries in the two lists are either both null, or are equal.
     */
    public boolean equals(Object other) {
        if (!(other instanceof DoublyLinkedList))
            return false;

        Entry e1 = first;
        Entry e2 = ((DoublyLinkedList) other).first;
        while (e1 != null && e2 != null) {
            boolean match = (e1.data == null ? e2.data == null : e1.data.equals(e2.data));
            if (!match)
                return false;

            e1 = e1.next;
            e2 = e2.next;
        }

        return (e1 == null && e2 == null);
    }

    /**
     * Return a string representation of the list.
     *
     * @return a string representation of the list
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("DoublyLinkedList[");
        for (Entry p = first; p != null; p = p.next) {
            if (p != first)
                sb.append(",");
            sb.append(String.valueOf(p.data));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * An entry in a DoublyLinkedList, containing client data
     * and links to the previous and next entries.
     */
    public class Entry {
        Object data;
        Entry prev;
        Entry next;


        /**
         * Create an entry to be put in a DoublyLinkedList.
         * Entries are not created directly by the client:
         * they are created automatically by the methods that
         * insert data into the list as a whole.
         *
         * @param data Client data to be stored in this entry
         * @param next The next entry to appear after this one.
         * @see #insert
         * @see ##append
         * @see #insertAfter
         */
        Entry(Object data, Entry prev, Entry next) {
            this.data = data;
            this.prev = prev;
            this.next = next;
        }

        /**
         * Get the client data in this entry in the list
         *
         * @return the client data in this entry in the list
         */
        public Object getData() {
            return data;
        }

        /**
         * Get the next entry in the list, if any.
         *
         * @return the next entry in the list,
         * or null if this is the last entry.
         */
        public Entry getNext() {
            return next;
        }

        /**
         * Get the previous entry in the list, if any.
         *
         * @return the previous entry in the list,
         * or null if this is the first entry.
         */
        public Entry getPrevious() {
            return prev;
        }

        /**
         * Insert a new entry in the list, after this one.
         *
         * @param data the client data to be stored in this entry
         */
        public void insertAfter(Object data) {
            Entry e = new Entry(data, this, next);
            if (next == null)
                last = e;
            else
                next.prev = e;
            next = e;
        }

        /**
         * Insert a new entry in the list, after this one.
         *
         * @param data the client data to be stored in this entry
         */
        public void insertBefore(Object data) {
            Entry e = new Entry(data, prev, this);
            if (prev == null)
                first = e;
            else
                prev.next = e;
            prev = e;
        }

        /**
         * Remove this entry from the list.
         *
         * @return the next entry in the list, or null if none
         */
        public Entry remove() {
            if (prev == null)
                first = next;
            else
                prev.next = next;

            if (next == null)
                last = prev;
            else
                next.prev = prev;

            return next;
        }
    }
}
