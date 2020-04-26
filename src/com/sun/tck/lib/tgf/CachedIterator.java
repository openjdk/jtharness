/*
 * $Id$
 *
 * Copyright (c) 2001, 2020, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tck.lib.tgf;

import java.util.ArrayList;
import java.util.LinkedList;

public class CachedIterator extends ValuesIterator {

    protected ArrayList<Object[]> data;
    private int index;

    public CachedIterator(ArrayList<Object[]> data) {
        this.data = data;
    }

    public CachedIterator(Values values) {
        this(values, true);
    }

    /// used by subclasses
    protected CachedIterator(Values values, boolean unWrapAbstractValues) {
        data = new ArrayList<>();
        for (Object[] initial : values) {
            LinkedList<Object> args = new LinkedList<>();
            for (Object object : initial) {
                // this is the most important feature of cache
                if (unWrapAbstractValues && object instanceof AbstractValue) {
                    args.add(((AbstractValue) object).doCreate());
                } else {
                    args.add(object);
                }
            }
            Object[] ret = args.toArray();
            data.add(ret);
        }
        this.index = 0;
    }



    public void shift() {
        index++;
    }

    protected void rollback() {
        index=0;
    }

    protected ValuesIterator createCopy() {
        return new CachedIterator(data);
    }

    public boolean hasNext() {
        return index < data.size();
    }

    public Object[] next() {
        Object[] result = data.get(index);
        if (root) {
            shift();
        }
        return result;
    }

}
