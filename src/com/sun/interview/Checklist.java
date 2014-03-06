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
package com.sun.interview;

import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

/**
 * A Checklist is a sorted collection of messages,
 * for the purpose of providing a list of actions that must
 * be performed in association with a completed interview.
 * Messages are grouped into sections, which are sorted
 * alphabetically; within a section, messages are stored
 * in the order they are added.
 */
public class Checklist
{
    /**
     * An Item contains a message to appear in a checklist.
     */
    public static class Item {
        /**
         * Create an item to appear in a checklist.
         * @param sectionName the name of the section
         * in which the message should appear
         * @param text the text of the message to appear
         * in the checklist
         */
        public Item(String sectionName, String text) {
            this.sectionName = sectionName;
            this.text = text;
        }

        private String sectionName;
        private String text;

    };

    /**
     * Create an empty checklist.
     */
    public Checklist() {
        sections = new TreeMap();
    }

    /**
     * Get the names of the sections for which entries have been
     * added to the checklist.
     * @return an array containing the names of the sections for
     * which entries have been added to the checklist
     */
    public String[] getSectionNames() {
        if (sections == null)
            return null;
        String[] names = new String[sections.keySet().size()];
        sections.keySet().toArray(names);
        return names;
    }

    /**
     * Get the messages in a specific section of this checklist.
     * @param sectionName the name of the section for which
     * the iterator should return messages
     * @return an array containing the messages in a specific section
     * of this checklist, or null if no messages have been
     * added for the given section
     */
    public String[] getSectionMessages(String sectionName) {
        Vector v = (Vector) (sections.get(sectionName));
        if (v == null)
            return null;
        String[] msgs = new String[v.size()];
        v.copyInto(msgs);
        return msgs;
    }

    /**
     * Add a new item to this checklist.
     * @param item The Item to be added
     */
    public void add(Item item) {
        Vector msgs = (Vector) (sections.get(item.sectionName));
        if (msgs == null) {
            msgs = new Vector();
            sections.put(item.sectionName, msgs);
        }
        msgs.add(item.text);
    }

    /**
     * Determine whether or not any items have been added to the checklist.
     * @return true if the checklist has no entries, and false otherwise.
     */
    public boolean isEmpty() {
        return sections.isEmpty();
    }

    private Map sections; // section name to vector of messages
}
