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
package com.sun.javatest.util;

import java.util.Vector;

/**
 * A map-like structure which has two side-by-side ordered sets of Objects in pairs.
 * This is basically a map structure except that it is always ordered and has a less
 * strict an idea of key and value.  The terms key and value are still used to
 * differentiate data from the two sets.  The primary advantage to using this class
 * is that you can do lookups either by key or value, in case where translation in
 * both directions is necessary.
 */
public class OrderedTwoWayTable {
    public OrderedTwoWayTable() {
        keys = new Vector();
        values = new Vector();
    }

    /**
     * Put an object in the table.
     * @param key the key for this object
     * @param value the object to be stored in the table
     */
    public synchronized void put(Object key, Object value) {
        keys.add(key);
        values.add(value);
    }

    /**
     * Get the position of the key within the table.
     * @param key the key whose position is required
     * @return the position of the key within the table, or -1 if not found
     */
    public synchronized int getKeyIndex(Object key) {
        return findIndex(keys, key);
    }

    /**
     * Get the position of a value within the table.
     * @param value the value whose position is required
     * @return the position of the value within the table, or -1 if not found
     */
    public synchronized int getValueIndex(Object value) {
        return findIndex(values, value);
    }

    /**
     * Get the value at the given index.
     * @param index the index of the required value
     * @return the value at the given index, or null if not found or index out of bounds.
     */
    public synchronized Object getValueAt(int index) {
        if (index < values.size())
            return values.elementAt(index);
        else
            return null;
    }

    /**
     * Get the key at the given index.
     * @param index the index of the given key
     * @return the value at the given index, null if not found or index out of bounds.
     */
    public synchronized Object getKeyAt(int index) {
        if (index < keys.size())
            return keys.elementAt(index);
        else
            return null;
    }

    /**
     * Get the number of pairs in the table.
     * @return the numbver of pairs on the table
     */
    public synchronized int getSize() {
        return keys.size();
    }

    /**
     * Remove the object at a specified index.
     * @param index the index of the entry to be removed.
     */
    public synchronized void remove(int index) {
        if (index >= keys.size())
            return;

        keys.removeElementAt(index);
        values.removeElementAt(index);
    }

    /**
     * Get the index of the target in the table.
     * This is a reference equality search.
     * @param data the vector in which to search
     * @param target the object to search for
     * @return the index of the target in the vector, or -1 if not found
     */
    protected int findIndex(Vector data, Object target) {
        for (int i = 0; i < data.size(); i++)
            if (data.elementAt(i) == target)
                return i;

        return -1;
    }

    private Vector keys, values;
}


