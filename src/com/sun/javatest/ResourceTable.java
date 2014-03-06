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
package com.sun.javatest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * A table providing simple named locks for arbitrary resources.
 */
public class ResourceTable
{
    /**
     * Create a resource table.
     */
    public ResourceTable() {
        table = new HashMap();
    }

    /**
     * Create a resource table of a specified size.
     * @param initialSize a hint as to the initial capacity to make the table
     */
    public ResourceTable(int initialSize) {
        table = new HashMap(initialSize);
    }

    /**
     * Try to acquire a set of named locks. To avoid deadlocks, the
     * locks are acquired in a canonical order (alphabetical by name.)
     * @param resourceNames a list of names identifying locks to be acquired.
     * @param timeout a maximum time, in milliseconds to ait for the locks to become available.
     * @return true if and only if all the locks were successfully acquired
     * @throws InterruptedException is the method was interrupted while
     * waiting for the locks to become available.
     */
    public synchronized boolean acquire(String[] resourceNames, int timeout) throws InterruptedException {
        if (timeout <= 0)
            throw new IllegalArgumentException("timeout required");

        if (resourceNames.length > 1) {
            // sort and remove duplicates
            // canonicalize acquisition order to prevent deadlocks
            TreeSet ts = new TreeSet();
            ts.addAll(Arrays.asList(resourceNames));
            String[] s = new String[ts.size()];
            ts.toArray(s);
            resourceNames = s;
        }

        long start = System.currentTimeMillis();
        long now = start;

        try {
            for (int i = 0; i < resourceNames.length; i++) {
                String resourceName = resourceNames[i];
                Object owner = null;
                while ((owner = table.get(resourceName)) != null) {
                    long remain = start + timeout - now;
                    if (remain < 0) {
                        release(resourceNames);
                        return false;
                    }

                    //System.out.println("waiting for resource: " + resourceName);
                    wait(remain);
                    now = System.currentTimeMillis();
                }

                table.put(resourceName, Thread.currentThread());
            }
            return true;
        }
        catch (InterruptedException e) {
            release(resourceNames);
            throw e;
        }
    }

    /**
     * Release a set of previously acquired locks.
     * The named locks are only released if currently owned by the
     * same thread that acquired them.
     * @param resourceNames the names of the locks to be released
     */
    public synchronized void release(String[] resourceNames) {
        for (int i = 0; i < resourceNames.length; i++) {
            Object owner = table.get(resourceNames[i]);
            if (owner == Thread.currentThread())
                table.remove(resourceNames[i]);
        }
        notifyAll();
    }

    private HashMap table;
}
