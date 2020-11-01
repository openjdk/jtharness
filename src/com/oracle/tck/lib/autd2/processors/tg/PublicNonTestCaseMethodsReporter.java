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


package com.oracle.tck.lib.autd2.processors.tg;

import com.oracle.tck.lib.autd2.NonTestCase;
import com.oracle.tck.lib.autd2.TestGroupContext;
import com.oracle.tck.lib.autd2.processors.InterestedInAnnotations;
import com.oracle.tck.lib.autd2.processors.Processor;
import com.sun.tck.lib.tgf.ReflectionUtils;
import com.sun.tck.test.TestCase;
import com.sun.tck.test.TestGroup;
import com.sun.javatest.Status;


import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * This processor ensures that all public methods
 * have either {@code TestCase} or {@code NonTestCase} annotation.
 * It also reports as error if these annotations are used together.
 * Processor verifies all the methods from each of the parent "testgroup" classes and interfaces
 * as well as from this class itself. Here a class or an interface is considered to be "testgroup"
 * if it either has any parent class or interface annotated with {@code TestGroup} annotation
 * or has such annotation itself.
 */
@InterestedInAnnotations(TestGroup.class)
public class PublicNonTestCaseMethodsReporter extends Processor.TestGroupProcessor {

    private static final List<Class<? extends Processor<TestGroupContext, TestGroupContext.TestGroupLifePhase>>>
            USE_BEFORE = asList(NonPublicTestCasesReporter.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public TestGroupContext.TestGroupLifePhase[] getLifePhasesInterestedIn() {
        return new TestGroupContext.TestGroupLifePhase[] {TestGroupContext.TestGroupLifePhase.BEFORE_TESTGROUP};
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public PhaseOwnership getPhaseOwnership(
            TestGroupContext.TestGroupLifePhase interestedLifePhase) {
        return PhaseOwnership.MANY_PER_PHASE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(TestGroupContext.TestGroupLifePhase lifePhase, TestGroupContext context) throws Throwable {

        final Class<?> tgClass = context.getTestGroupInstance().getClass();

        for (Method method : getInspectedMethodsFromHierarchy(tgClass)) {
            if (method.getAnnotation(TestCase.class) != null &&
                method.getAnnotation(NonTestCase.class) != null) {
                throw new IllegalArgumentException(
                        "Please remove one of @TestCase or @NonTestCase annotation from method \"" +
                                method.getDeclaringClass().getName() + "." + method.getName() + "\".");
            }
            if (method.getAnnotation(TestCase.class) == null &&
                !method.isSynthetic() &&
                Modifier.isPublic(method.getModifiers()) &&
                !Modifier.isStatic(method.getModifiers()) &&
                !isRunMethod(method) &&
                method.getAnnotation(NonTestCase.class) == null) {
                    throw new IllegalArgumentException(
                            "Please either add @TestCase or @NonTestCase annotation to method \""
                            + method.getDeclaringClass().getName() + "." + method.getName() + "\" or make it non-public. " +
                            "All public methods must have @TestCase annotation by default. " +
                            "As an exception a method which needs to be public " +
                            "but is not a testcase should be annotated with @NonTestCase annotation."
                    );
            }
        }
    }

    /**
     * @param aClass class to start the scan from
     * @return methods from class hierarchy and methods from interface hierarchy
     * which declaring class has any parent class or interface annotated with @TestGroup
     */
    private List<Method> getInspectedMethodsFromHierarchy(Class<?> aClass) {
        final LinkedList<Method> result = new LinkedList<>();
        ReflectionUtils.getClassHierarchy(aClass).forEach(clazz -> {
            for (Class<?> c : ReflectionUtils.getClassHierarchy(clazz)) {
                if (c.getAnnotation(TestGroup.class) != null) {
                    Collections.addAll(result, clazz.getDeclaredMethods());
                    break;
                }
            }
        });
        return result;
    }


    private boolean isRunMethod(Method method) {
        return "run".equals(method.getName())
                && Arrays.equals(
                new Class<?>[]{String[].class, PrintWriter.class, PrintWriter.class},
                method.getParameterTypes())
                && Status.class.equals(method.getReturnType());
    }

    @Override
    public List<Class<? extends Processor<TestGroupContext, TestGroupContext.TestGroupLifePhase>>> useBefore() {
        return USE_BEFORE;
    }
}
