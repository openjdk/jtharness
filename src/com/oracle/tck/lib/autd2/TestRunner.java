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

package com.oracle.tck.lib.autd2;

import com.oracle.tck.lib.autd2.processors.InterestedInAnnotations;
import com.oracle.tck.lib.autd2.processors.InterestedProcessors;
import com.oracle.tck.lib.autd2.processors.Processor;
import com.sun.tck.lib.tgf.ReflectionUtils;
import com.sun.tck.test.TestCase;
import com.sun.tck.test.TestGroup;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * A utility class containing static methods responsible for running a test.
 *
 * Not to be inherited.
 */
public final class TestRunner {

    private TestRunner() {
        // not intended to be instantiated
    }
    private static final Map<Class<?>, AnnotatedStructure> cache = new ConcurrentHashMap<>();

    private static void putUsersProcessorsIntoCache(Map<Class<? extends Annotation>, Set<Processor>> set,
                                                    Set<Processor> processors) {
        for (Processor p : processors) {
            for (Class clazz : ReflectionUtils.getClassHierarchy(p.getClass())) {
                final InterestedInAnnotations interestedIn =
                        (InterestedInAnnotations) clazz.getAnnotation(InterestedInAnnotations.class);
                if (interestedIn != null) {
                    for (Class<? extends Annotation> annotation : interestedIn.value()) {
                        Set<Processor> classes = set.get(annotation);
                        if (classes == null) {
                            classes = new HashSet<>();
                            set.put(annotation, classes);
                        }
                        classes.add(p);
                    }
                }
            }
        }
    }


    /**
     * Running with pre-defined processors provided as second argument.
     * Empty set should be provided if no predefined processors are required.
     */
    private static void performRun(TestGroupContext testGroupContext,
                                   Set<Class<? extends Processor.TestGroupProcessor>> userTGProcessors,
                                   Set<Class<? extends Processor.TestCaseProcessor>>  userTCProcessors) {

        Class<?> tgClass = testGroupContext.getTestGroupInstance().getClass();

        Map<Class<? extends Annotation>, Set<Processor>> userProcCache = new HashMap<>();

        putUsersProcessorsIntoCache(userProcCache, AUTD2Utils.getAllUserProcessors(tgClass));
        putUsersProcessorsIntoCache(userProcCache, userTGProcessors.stream().map(AUTD2Utils::instantiateProcessor).collect(Collectors.toSet()));
        putUsersProcessorsIntoCache(userProcCache, userTCProcessors.stream().map(AUTD2Utils::instantiateProcessor).collect(Collectors.toSet()));

        final Map<String, Processor> applicableProcessors = new HashMap<>();

        // todo here we have a chance process the structure on the first run through the testgroup if it has not been cached
        AnnotatedStructure annStructure = getCachedStructure(
                tgClass,
                (element, annotations) -> {
                    for (Annotation annotation : annotations) {
                        createApplicableProcessors(userProcCache, applicableProcessors, element, annotation);
                    }
                }
        );

        if (applicableProcessors.isEmpty()) {
            for (int i = 0; i < annStructure.annotatedElements.size(); i++) {
                for (Annotation ann : annStructure.annotations.get(i)) {
                    createApplicableProcessors(userProcCache, applicableProcessors, annStructure.annotatedElements.get(i), ann);
                }
            }
        }

        HashMap<TestGroupContext.TestGroupLifePhase, List<Processor.TestGroupProcessor>> phase2TGProc = new HashMap<>();

        applicableProcessors.values().forEach(p -> {
            if (p instanceof Processor.TestGroupProcessor) {
                AUTD2Utils.mapToLifePhases(phase2TGProc, (Processor.TestGroupProcessor) p);
            }
        });

        // todo fix
        testGroupContext.setAllUsedProcessors(new HashSet<>(applicableProcessors.values()));
        testGroupContext.setLifePhase2ProcessorsMap(phase2TGProc);
        AUTD2Utils.iterateLifePhases(testGroupContext, TestGroupContext.TestGroupLifePhase.values());
    }

    private static void createApplicableProcessors(
            Map<Class<? extends Annotation>, Set<Processor>> userProcCache,
            Map<String, Processor> applicableProcessors,
            AnnotatedElement element,
            Annotation annotation) {

        InterestedProcessors interestedProcessors = annotation.annotationType().getAnnotation(InterestedProcessors.class);
        List<Class<? extends Processor>> classes = new LinkedList<>();
        if (interestedProcessors != null) {
            classes.addAll(Arrays.asList(interestedProcessors.value()));
        }

        if (classes != null) {
            addApplicable(applicableProcessors, element, annotation,
                    classes.stream().map(AUTD2Utils::instantiateProcessor).collect(Collectors.toSet()));
        }
        Set<Processor> userProcessors = userProcCache.get(annotation.annotationType());
        if (userProcessors != null) {
            addApplicable(applicableProcessors, element, annotation, userProcessors);
        }
    }

    private static void addApplicable(Map<String, Processor> applicableProcessors, AnnotatedElement annElem,
                                      Annotation annotation, Set<Processor> processors) {
        for (Processor p : processors) {
            if (p.isProcessorApplicableToAnnotationInstance(annotation)) {
                Processor processor = applicableProcessors.get(p.getClass().getName());
                if (processor == null) {
                    applicableProcessors.put(p.getClass().getName(), p);
                } else {
                    p = processor;
                }
                p.addAnnotatedElement(annElem, annotation);
            }
        }
    }

    // todo dispatch annotation events on the first pass through the class structure
    private static AnnotatedStructure getCachedStructure(Class<?> tgClass, BiConsumer<AnnotatedElement, Annotation[]> onTheFly) {
        AnnotatedStructure result = cache.get(tgClass);
        if (result == null) {
            final AnnotatedStructure structure = new AnnotatedStructure();
            BiConsumer<AnnotatedElement, Annotation[]> container = (element, annotations) -> {
                structure.annotatedElements.add(element);
                structure.annotations.add(annotations);
                onTheFly.accept(element, annotations);
            };

            ReflectionUtils.getClassHierarchy(tgClass).forEach(clazz -> {
                // this is being done to map the annotations
                // attached to superclasses to testgroup class itself
                // it is needed for proper processor selection otherwise
                // testcase processors initiated by annotations attached to testgroup parents
                // could skipped since considered as not applicable to the testgroup itself
                // (hierarchy contains the starting class as well so let's not do double job)
                if (tgClass != clazz) {
                    container.accept(tgClass, clazz.getDeclaredAnnotations());
                }
                container.accept(clazz, clazz.getDeclaredAnnotations());
                fetchMethodsAnnotations(clazz, container);
                fetchConstructorsAnnotations(clazz, container);
                fetchFieldsAnnotations(clazz, container);
            });
            cache.put(tgClass, structure);
            result = structure;
        }
        return result;
    }

    /**
     * Fetches the annotations belonging to constructors.
     */
    private static void fetchConstructorsAnnotations(
            Class<?> tgClass, BiConsumer<AnnotatedElement, Annotation[]> container) {
        for (Constructor<?> c : tgClass.getDeclaredConstructors()) {
            Annotation[] declaredAnnotations = c.getDeclaredAnnotations();
            if (declaredAnnotations.length > 0) {
                container.accept(c, declaredAnnotations);
            }
        }
    }

    /**
     * Fetches the annotations belonging to fields.
     */
    private static void fetchFieldsAnnotations(
            Class<?> tgClass, BiConsumer<AnnotatedElement, Annotation[]> container) {
        for (AnnotatedElement f : tgClass.getDeclaredFields()) {
            Annotation[] declaredAnnotations = f.getDeclaredAnnotations();
            if (declaredAnnotations.length > 0) {
                container.accept(f, declaredAnnotations);
            }
        }
    }

    /**
     * Fetches the annotations belonging to methods.
     */
    private static void fetchMethodsAnnotations(
            Class<?> testGroupClass, BiConsumer<AnnotatedElement, Annotation[]> container) {
        for (Method m : testGroupClass.getDeclaredMethods()) {
            Annotation[] declaredAnnotations = m.getDeclaredAnnotations();
            if (declaredAnnotations.length > 0) {
                container.accept(m, declaredAnnotations);
            }
            for (Annotation[] annotations : m.getParameterAnnotations()) {
                if (annotations.length > 0) {
                    container.accept(m, annotations);
                }
            }
        }
    }

    /**
     * An entry point for running the given testgroup instance
     * with the specified print writers and arguments.
     *
     * @param test a testgroup instance
     * @param log  PrintWriter instance to use as a 'log'
     * @param ref  PrintWriter instance to use as a 'ref'
     * @param args arguments to be used
     * @return resulting TestResult
     */
    public static TestResult run(
            Object test,
            PrintWriter log,
            PrintWriter ref,
            String... args) {
        return run(test, log, ref, Collections.emptySet(), Collections.emptySet(), args);
    }

    /**
     * An entry point for running the given testgroup instance
     * with the specified and arguments.
     * System.out and System.err will be used as outputs.
     *
     * @param test a testgroup instance
     * @param args arguments to be used
     * @return resulting TestResult
     */
    public static TestResult run(
            Object test,
            String... args) {
        PrintWriter log = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.err, StandardCharsets.UTF_8)), true);
        PrintWriter ref = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8)), true);
        return run(test, log, ref, Collections.emptySet(), Collections.emptySet(), args);
    }

    /**
     * An entry point for running the given testgroup instance
     * with the specified print writers and arguments as well as a map of predefined processors.
     *
     * @param test a testgroup instance
     * @param log  PrintWriter instance to use as a 'log'
     * @param ref  PrintWriter instance to use as a 'ref'
     * @param userTGProcessors a set of predefined processors (possibly empty) manually bound to @TestGroup annotation
     * @param userTCProcessors a set of predefined processors (possibly empty) manually bound to @TestCase annotation
     * @param args arguments to be used
     * @return resulting TestResult
     */
    public static TestResult run(
            Object test,
            PrintWriter log,
            PrintWriter ref,
            Set<Class<? extends Processor.TestGroupProcessor>> userTGProcessors,
            Set<Class<? extends Processor.TestCaseProcessor>>  userTCProcessors,
            String... args) {
        try {
            TestGroupContext testGroupContext = new TestGroupContext();
            testGroupContext.setLog(log);
            testGroupContext.setRef(ref);
            testGroupContext.setTestGroupInstance(test);
            testGroupContext.setExecutionArgs(args);
            performRun(testGroupContext, userTGProcessors, userTCProcessors);
            return testGroupContext.getFinalTestResult();
        } finally {
            if (log != null) {
                log.flush();
            }
            if (ref != null) {
                ref.flush();
            }
        }
    }



    static class AnnotatedStructure {
        final List<AnnotatedElement> annotatedElements = new ArrayList<>();
        final List<Annotation[]> annotations = new ArrayList<>();
    }

}
