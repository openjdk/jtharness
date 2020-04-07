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

import com.sun.tck.lib.NotApplicableException;

import java.lang.reflect.*;
import java.util.*;
import java.io.PrintWriter;

/**
 * Class containing a number of commonly used utility methods
 */
public class TGFUtils {

    private static final String DOT = ".";

    private static final MemberProcessor<Field> FIELD_PROCESSOR = ReflectionUtils::getAllFields;
    private static final MemberProcessor<Method> METHOD_PROCESSOR = ReflectionUtils::getAllMethods;

    /**
     * Standard -exclude keyword used by the JCK harness
     */
    public static final String EXCLUDE = "-exclude";
    public static final String EXCLUDE_DELIMETER = ",";
    public static final String INDICES_START = "(";
    public static final String INDICES_END = ")";
    public static final String INDEX_SEPARATOR = ";";
    public static final String RANGE_DELIMITER = "-";

    /**
     * Returns method that is referenced by the passed string
     */
    public static Method getDeclaredMethod(Class<?> testClass, String referenceToMethod) throws SomethingIsWrong {
        return getDeclaredMember(testClass, referenceToMethod, METHOD_PROCESSOR);
    }

    static Field getDeclaredField(Class<?> testClass, String referenceToField) throws SomethingIsWrong {
        return getDeclaredMember(testClass, referenceToField, FIELD_PROCESSOR);
    }

    /**
     * Creates exclude list basing on the passed command line arguments
     * @return testcase method names mapped to entities (optionally) containing invocation indices
     */
    public static Map<String, Values.ExcludedIndices> createExcludeList(String... args) {
        final Map<String, Values.ExcludedIndices> result = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (EXCLUDE.equals(args[i]) && i < args.length - 1) {
                String excludedTestCases = args[i + 1];
                StringTokenizer stringTokenizer = new StringTokenizer(excludedTestCases, EXCLUDE_DELIMETER);
                while (stringTokenizer.hasMoreTokens()) {
                    String exclusionEntry = stringTokenizer.nextToken().trim();
                    Values.ExcludedIndices excludedCallIndices = Values.ExcludedIndices.create();
                    int i1 = exclusionEntry.indexOf(INDICES_START);
                    String methodName = exclusionEntry;
                    if (i1 >= 0) {
                        methodName = exclusionEntry.substring(0, i1);
                        int i2 = exclusionEntry.indexOf(INDICES_END);
                        if (i2 <= i1 + 1) {
                            throw new IllegalArgumentException("Incorrect exclusion entry: \"" + exclusionEntry + "\"");
                        }
                        String indices = exclusionEntry.substring(i1 + 1, i2);
                        for (String indexOrRange : indices.split(INDEX_SEPARATOR)) {
                            String[] range = indexOrRange.split(RANGE_DELIMITER);
                            if (range.length > 2) {
                                throw new IllegalArgumentException("Illegal call exclusion range: " + indexOrRange);
                            }
                            if (range.length == 1) {
                                excludedCallIndices.exclude(Long.parseLong(indexOrRange));
                            } else {
                                excludedCallIndices.exclude(Long.parseLong(range[0]), Long.parseLong(range[1]));
                            }
                        }
                    }
                    result.put(methodName, excludedCallIndices);
                }
                i++;
            }
        }
        return result;
    }


    static void printStackTraceToLog(Throwable thrownException, final PrintWriter log) {
        if (log != null) {
            thrownException.printStackTrace(log);
        }
    }

    /**
     * Searches for a method and performs its invocation
     * @param methodName method to call
     * @param testClass testgroup class
     * @param testInstance testgroup instance
     * @param log output stream to use
     * @throws SomethingIsWrong thrown ifg something went wrong
     */
    public static void searchAndInvoke(final String methodName,
                                 final Class<?> testClass,
                                 final Object testInstance,
                                 final PrintWriter log) throws SomethingIsWrong {
        try {
            Method method = getDeclaredMethod(testClass, methodName);
            if (method!= null) {
                invokeMethod(method, testInstance);
            } else {
                throw new SomethingIsWrong(java.text.MessageFormat.format("Method \"{0}\" doesn''t exist.", methodName));
            }
        } catch (SomethingIsWrong e) {
            throw e;
        } catch (Throwable throwable) {
            Throwable cause = throwable.getCause();
            if (cause == null) {
                cause = throwable;
            } else if (cause instanceof NotApplicableException) {
                throw (NotApplicableException)cause;
            }
            printStackTraceToLog(cause, log);
            throw new SomethingIsWrong(
                    java.text.MessageFormat.format("Method \"{0}\" has thrown an exception {1}", methodName, cause));
        }
    }

    /**
     * Collects data referenced by a string.
     * @param instance an instance which will be introspected
     * @param instanceClass the instance class
     * @param log output to use
     * @param dataHolderName a reference to required data
     * @param referencingAnnotationName annotation that
     * @return resulting data
     */
    public static Object getRawDataFromTextReference(Object instance, Class<?> instanceClass,
                                              PrintWriter log, String dataHolderName,
                                              String referencingAnnotationName) {
        Object result;
        Method method = null;
        final Field field = getDeclaredField(instanceClass, dataHolderName);
        try {
            if (field != null) {
                field.setAccessible(true);
                /* if field is taken from remote class, passed instance is ignored */
                result = field.get(instance);
            } else {
                method = getDeclaredMethod(instanceClass, dataHolderName);
                if (method == null) {
                    throw new SomethingIsWrong(
                            "Field or method \"" + dataHolderName
                                    + "\", referenced by " + referencingAnnotationName + ", was not found");
                }
                method.setAccessible(true);
                /* if method is taken from remote class, passed instance is ignored */
                result = invokeMethod(method, instance);
            }

            Member member = method != null ? method : field;
            String nextDataQuery;
            if (dataHolderName.startsWith(member.getDeclaringClass().getName())) {
                nextDataQuery = dataHolderName.substring(
                        member.getDeclaringClass().getName().length() + member.getName().length() + 1);
            } else {
                nextDataQuery = dataHolderName.substring(
                        member.getName().length());
            }
            // further data querying is needed
            if (!nextDataQuery.isEmpty()) {
                result = getRawDataFromTextReference(result, result.getClass(), log,
                        // removing '.' from the very beginning
                        nextDataQuery.substring(1),
                        referencingAnnotationName);
            }
        } catch (Throwable throwable) {
            // forwarding further
            if (throwable instanceof SomethingIsWrong) {
                throw (SomethingIsWrong)throwable;
            }
            // unwrapping exception wrapped by reflection API
            Throwable thrownException = throwable.getCause();
            if (thrownException == null) {
                thrownException = throwable;
            }
            if (thrownException instanceof NotApplicableException) {
                throw (NotApplicableException) thrownException;
            }
            String message = "Field or method \"" + dataHolderName + "\", referenced by " +
                    referencingAnnotationName + ", " + "has thrown an exception " + thrownException;
            printStackTraceToLog(thrownException, log);
            throw new SomethingIsWrong(message);
        }
        return result;
    }

    /**
     * Concrete implementation of this interface
     * returns either all methods or fields of a class.
     * Used by supplementary code for better code reusability.
     */
    interface MemberProcessor<Member> {
        List<Member> getAll(Class<?> c);
    }

    /**
     * Returns a field or a method referenced by the given text reference.
     * If the method/field name contained in the reference is followed by a method invocation chain
     */
    private static <M extends Member> M getDeclaredMember(Class<?> clazz,
                                        String reference,
                                        MemberProcessor<M> processor) throws SomethingIsWrong {
        List<M> allMembers;
        String name;
        Class<?> aClass = findAndLoadClass(clazz, reference);
        if (aClass != null) {
            allMembers = processor.getAll(aClass);

            // extracting the name of the initial data holder
            int endIndex = reference.indexOf(DOT, aClass.getName().length() + 1);
            if (endIndex >0) {
                name = reference.substring(aClass.getName().length() + 1, endIndex);
            } else {
                name = reference.substring(aClass.getName().length() + 1);
            }
        } else {
            name = !reference.contains(DOT) ? reference : reference.substring(0, reference.indexOf(DOT));
            allMembers = processor.getAll(clazz);
        }
        for (M member : allMembers) {
            if (name.equals(member.getName())) {
                if (aClass != null && !Modifier.isStatic(member.getModifiers())) {
                    throw new SomethingIsWrong("Member \"" + reference +
                            "\", referenced by @TestData, must be static");
                }
                return member;
            }
        }
        return null;
    }

    private static Class<?> findAndLoadClass(Class<?> testClass, String initialReference) {
        int lastDotIndex = initialReference.lastIndexOf(DOT);
        if (lastDotIndex < 0) {
            return null;
        }

        ClassLoader classLoader = getClassLoader(testClass);

        // cutting off name of a field or a method
        String proposedClassName = null;
        Class<?> clazz = null;

        while (lastDotIndex > 0) {
            try {
                proposedClassName = initialReference.substring(0, lastDotIndex);
                clazz = classLoader.loadClass(proposedClassName);
                // found ! returning class that we have loaded
                return clazz;
            } catch (ClassNotFoundException e) {
                // continuing search by cutting off next token
                initialReference = proposedClassName;
                lastDotIndex = initialReference.lastIndexOf(DOT);
            }
        }
        return clazz;
    }


    private static ClassLoader getClassLoader(Class<?> testClass) {
        ClassLoader loader = testClass.getClassLoader();
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        return loader;
    }

    static Object invokeMethod(final Method method,
                               final Object testInstance,
                               final Object... args) throws Throwable {
        method.setAccessible(true);
        // plain mode
        return method.invoke(testInstance, args);
    }

}
