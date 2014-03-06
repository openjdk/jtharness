/*
 * $Id$
 *
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

class AgentClassLoader2 extends ClassLoader {

    private static volatile AgentClassLoader2 instance = null;

    public AgentClassLoader2(Agent.Task parent) {
        super(parent.getClass().getClassLoader());
        this.parent = parent;
    }

    /*
     * Returns shared instance of classloader for tests where it is required.
     */
    public static AgentClassLoader2 getInstance(Agent.Task parent) {
        if (instance == null) {
            synchronized (AgentClassLoader2.class) {
                if (instance == null) {
                    instance = new AgentClassLoader2(parent);
                }
            }
        }
        instance.parent = parent;
        return instance;
    }


    public Class findClass(String className) throws ClassNotFoundException {
        if (className != null) {
            int i = className.lastIndexOf('.');
            if (i>0) {
                String pkgName = className.substring(0,i);
                if (getPackage(pkgName) == null) {
                    definePackage(pkgName,null,null,null,null,null,null,null);
                }
            }
            byte[] data = parent.getClassData(className);
            return defineClass(className, data, 0, data.length);
        }
        throw new ClassNotFoundException();
    }

    public synchronized InputStream getResourceAsStream(String resourceName) {
        // check local classpath first
        // the resource should already be absolute, if we've got here
        // through getClass().getResourceAsStream()
        InputStream in = getClass().getResourceAsStream(resourceName);
        if (in == null) {
            try {
                // if not found here, try remote load from Agent Manager
                byte[] data = parent.getResourceData(resourceName);
                in = new ByteArrayInputStream(data);
            }
            catch (Exception e) {
                // ignore
            }
        }
        return in;
    }

    private Agent.Task parent;
}

