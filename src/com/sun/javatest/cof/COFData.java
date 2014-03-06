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
package com.sun.javatest.cof;

import com.sun.javatest.InterviewParameters;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class COFData
{
    public COFData() {
    }

    COFData(Vector data) throws IOException {
        for (int i = 0; i < data.size(); i++) {
            Object o = data.elementAt(i);
            if (o == null)
                throw new NullPointerException();
            else if (o instanceof String) {
                String s = (String) o;
                int eq = s.indexOf("=");
                if (eq < 1)
                    throw new IllegalArgumentException(s);
                String name = s.substring(0, eq);
                String value = s.substring(eq + 1);
                put(name, value);
            }
            else if (o instanceof File) {
                File f = (File) o;
                InputStream in = new BufferedInputStream(new FileInputStream(f));
                Properties p = new Properties();
                try {
                    p.load(in);
                }
                finally {
                    in.close();
                }
                putAll(p);
            }
            else
                throw new IllegalArgumentException(o.toString());
        }
    }

    public String get(String name) {
        return (String) (data.get(name));
    }

    public String get(String name, String defaultValue) {
        String value = (String) (data.get(name));
        return (value == null ? defaultValue : value);
    }

    public void put(String name, String value) {
        data.put(name, value);
    }

    public void putAll(Map map) {
        data.putAll(map);
    }

    private Map data = new HashMap();

    private CustomFilter filter = new CustomFilterAdapter();

    void setCustomFilter(String filterClassName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class c = Class.forName(filterClassName);
        filter = (CustomFilter) c.newInstance();
    }

    CustomFilter getCustomFilter() {
        return filter;
    }

    private MTL mtl;

    MTL getMtl() {
        return mtl;
    }

    void setMtl(MTL mtl) {
        this.mtl = mtl;
    }

    public void setInterviewParameters(InterviewParameters ip) {
        this.ip = ip;
    }

    public InterviewParameters getInterviewParameters() {
        return ip;
    }

    public boolean isInterviewParametersAvailable() {
        return ip != null;
    }

    private InterviewParameters ip;
}
