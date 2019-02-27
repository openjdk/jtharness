/*
 * $Id$
 *
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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

/**
 * Abstract class to implement in the Agent ClassLoaders.
 * This will indicate that ClassLoader Object could create a new one with
 * the other parent ClassLoader.
 */
public abstract class InstantiationClassLoader extends ClassLoader {

    public InstantiationClassLoader(ClassLoader classLoader) {
        super(classLoader);
    }

    /**
     * Create ClassLoader object with the specified parent ClassLoader
     *
     * @param parent ClassLoader to be the new parent ClassLoader
     * @return New ClassLoader object with new parent ClassLoader
     * @throws InstantiationStateException if the state of the ClassLoader is not available
     *                                     to create a new instance (if sharedCL option is used the only one instance of
     *                                     the current ClassLoader could exist.
     */
    public abstract ClassLoader newClassLoaderInstance(ClassLoader parent) throws InstantiationStateException;

    /**
     * This exception is thrown when the ClassLoader object could not create a new instance
     */
    public static class InstantiationStateException extends Exception {
        public InstantiationStateException(String msg) {
            super(msg);
        }
    }

}
