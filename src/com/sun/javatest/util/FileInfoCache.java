/*
 * $Id$
 *
 * Copyright (c) 2001, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;
import java.util.Map;
import java.util.Collections;
import java.util.LinkedHashMap;

public class FileInfoCache
{

    private final int SIZE = 500;

    // we can't use generics in util package but this is actually Map<String, Boolean>
    private Map map = Collections.synchronizedMap( new LinkedHashMap() {
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > SIZE;
        }
    });

    public void put(File f, boolean b) {
        map.put(f.getAbsolutePath(), b ? Boolean.TRUE : Boolean.FALSE);
    }

    public Boolean get(File f) {
        return (Boolean) map.get(f.getAbsolutePath());
    }

    public void clear() {
        map.clear();
    }
}
