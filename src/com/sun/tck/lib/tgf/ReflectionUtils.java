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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class containing methods to work with reflection API
 */
public final class ReflectionUtils {

    private static final Map<Class<?>, Set<Class<?>>> interfaceHierarchyCache = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<Class<?>>>   classHierarchyCache = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<Field>>   fieldCache = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<Method>>  methodCache = new ConcurrentHashMap<>();

    // not to be instantiated
    private ReflectionUtils() {}

    /**
     * Returns hierarchy of the parent classes and interfaces that the given class extends or implements.
     */
    public static List<Class<?>> getClassHierarchy(final Class<?> starting) {
        List<Class<?>> result = classHierarchyCache.get(starting);
        if (result == null) {
            Class<?> temp = starting;
            result = new LinkedList<>();
            do {
                result.add(temp);
                result.addAll(getAllInterfaces(temp));
            } while ((temp = temp.getSuperclass()) != null);
            classHierarchyCache.put(starting, result);
        }

        return new ArrayList<>(result);
    }

    /**
     * Returns recursively all the interfaces that the passed class or interface inherits.
     */
    public static Set<Class<?>> getAllInterfaces(final Class<?> clazz) {
        Set<Class<?>> result = interfaceHierarchyCache.get(clazz);
        if (result == null) {
            result = new TreeSet<>(Comparator.comparing(Class::getCanonicalName));
            for (Class<?> anInterface : clazz.getInterfaces()) {
                result.add(anInterface);
                result.addAll(getAllInterfaces(anInterface));
            }
            interfaceHierarchyCache.put(clazz, result);
        }
        return new HashSet<>(result);
    }

    /**
     * @param aClass class to start scan from scan
     * @return all, even inherited fields
     */
    public static List<Field> getAllFields(Class<?> aClass) {
        List<Field> result = fieldCache.get(aClass);
        if (result != null) {
            return new ArrayList<>(result);
        }
        LinkedList<Field> fields = new LinkedList<>();
        getClassHierarchy(aClass).forEach( c -> Collections.addAll(fields, c.getDeclaredFields()));
        fieldCache.put(aClass, fields);
        return new ArrayList<>(fields);
    }

    /**
     * @param aClass class to start scan from scan
     * @return all, even inherited methods
     */
    public static List<Method> getAllMethods(Class<?> aClass) {
        final List<Method> result = methodCache.get(aClass);
        if (result != null) {
            return new ArrayList<>(result);
        }
        LinkedList<Method> methods = new LinkedList<>();
        getClassHierarchy(aClass).forEach( c -> Collections.addAll(methods, c.getDeclaredMethods()));
        methodCache.put(aClass, methods);
        return new ArrayList<>(methods);
    }

}
