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

import com.oracle.tck.lib.autd2.processors.Processor;
import com.oracle.tck.lib.autd2.processors.UseProcessors;
import com.sun.tck.lib.NotApplicableException;
import com.sun.tck.lib.tgf.ReflectionUtils;

import java.lang.reflect.*;
import java.util.*;

/**
 * Class containing a number of commonly used utility methods.
 */
public class AUTD2Utils {

    /**
     * This method smartly iterates over the provided list of testgroup or testcase life phases
     * executing all the processors associated with a particular life phase and going back and forth
     * over life phases if needed.
     */
    public static <L extends LifePhase<C>, C extends Context<P, L>, P extends Processor<C, L>> void
                iterateLifePhases(C context, L[] phasesArray) {

        List<L> lifePhases = Arrays.asList(phasesArray);
        TreeMap<L, L> stopAfterAndGetBack = new TreeMap<>();
        int i = 0;
        try {
            while (i < lifePhases.size()) {
                L phase = lifePhases.get(i);

                List<P> processors = context.getLifePhase2ProcessorsMap().get(phase);
                if (processors != null) {
                    sortProcessors(processors);
                    iterateThroughProcessorsUntilAllAreDone(context, stopAfterAndGetBack, phase, processors);
                }
                // todo create unit test that will check Processor that rolls around its lifephase several times
                L backToAfterThisLifePhase = stopAfterAndGetBack.get(phase);
                if (backToAfterThisLifePhase != null) {
                    i = lifePhases.indexOf(backToAfterThisLifePhase);
                    stopAfterAndGetBack.remove(phase);
                } else {
                    i++;
                }
            }
        } catch (NotApplicableException nae) {
            String message = nae.getMessage();
            context.recordNotApplicable();
            TestResult result = new InapplicableTestResult(
                    "Not applicable." + (message != null ? " Reason: " + message : ""), message);
            context.setOverridingResult(result);
        }

    }

    /**
     * This method sorts processors basing on lists provided by methods <code>Processor.useBefore()/useAfter()</code>
     *
     * @see com.oracle.tck.lib.autd2.processors.Processor
     */
    public static <L extends LifePhase<C>, C extends Context<P, L>, P extends Processor<C, L>> void sortProcessors(
            List<P> list) {
        ProcessorsDependenciesSorter.sort(list);
    }

    /**
     * Sorting is made by simulating processors running - processor is appended to some list instead of being run
     * provided this list is empty at simulation start.
     *
     * This class encapsulates such simulation.
     */
    private static class ProcessorsDependenciesSorter<L extends LifePhase<C>, C extends Context<P, L>, P extends Processor<C, L>> {
        /**
         * By the end of simulation this field contains the result.
         */
        private final List<P> result = new LinkedList<>();

        /**
         * While simulating this list contains processors that haven't been "run" yet.
         */
        private final List<P> remaining = new LinkedList<>();

        public static <L extends LifePhase<C>, C extends Context<P, L>, P extends Processor<C, L>> void sort(List<P> processors) {
            new ProcessorsDependenciesSorter<L, C, P>().doSort(processors);
        }

        /**
         * The simulation is started here.
         */
        private void doSort(List<P> processors) {
            remaining.clear();
            result.clear();
            remaining.addAll(processors);
            Deque<P> stack = new ArrayDeque<>();
            while (!remaining.isEmpty()) {
                assert stack.size() == 0 : "stack must be empty after previous run";
                simulateRun(remaining.get(0), stack);
            }
            assert processors.size() == result.size()
                    : "processors have been not just reordered but removed or added(!)";

            processors.clear();
            processors.addAll(result);
        }


        /**
         * This method returns processors list that must be run before one specified by <code>prc</code> parameter.
         */
        private Deque<P> getPrecedingProcessors(P prc) {
            Deque<P> precedingProcessors = new ArrayDeque<>();
            for (P another : remaining) {
                if (another.useBefore().contains(prc.getClass()) || prc.useAfter().contains(another.getClass())) {
                    precedingProcessors.push(another);
                }
            }
            return precedingProcessors;
        }

        /**
         * This method simulates running the processor specified by <code>prc</code> parameter. It recursively calls
         * itself in order to run all the processors that must be run before <code>prc</code> according to
         * <code>useAfter</code> and <code>useBefore</code> methods of all remaining processors (that haven't been
         * "run" yet).
         */
        private void simulateRun(P prc, Deque<P> stack) {
            Deque<P> precedingProcessors = getPrecedingProcessors(prc);
            for (P precedingProcessor : precedingProcessors) {
                if (stack.contains(precedingProcessor)) {
                    throw new RuntimeException(buildCyclicDependenciesMessage(prc, precedingProcessor, stack));
                }
                stack.push(precedingProcessor);
                simulateRun(precedingProcessor, stack);
                stack.pop();
            }
            remaining.remove(prc);
            //instead of running processor we append it to result:
            result.add(prc);
        }

        private String buildCyclicDependenciesMessage(P currentProcessor, P precedingProcessor, Deque<P> stack) {
            if (stack.peekLast() == null) {
                stack.push(currentProcessor);
            }
            stack.push(precedingProcessor);

            Iterator<P> prc = stack.iterator();
            P curr = prc.next();
            P prev = null;
            StringBuilder msg = new StringBuilder("cyclic processors dependencies were found:\n\t   ");
            while (prc.hasNext()) {
                prev = curr;
                curr = prc.next();
                msg.append(prev.getClass().getName()).append(" (");
                if (prev.useBefore().contains(curr.getClass())) {
                    msg.append(prev.getClass().getName()).append(".useBefore() result contains ").
                            append(curr.getClass().getName()).append(")");
                } else if (curr.useAfter().contains(prev.getClass())) {
                    msg.append(curr.getClass().getName()).append(".useAfter() result contains ").
                            append(prev.getClass().getName()).append(")");
                } else {
                    assert false : "previous stack item is expected to have a reason to be followed by current one";
                }
                msg.append(" =>\n\t=> ");
            }
            msg.append(curr.getClass().getName());
            msg.append("\n");

            return msg.toString();
        }
    }


    /**
     *  Method performs iteration over processors
     *  which are bound to a particular life phase.
     */
    static <C extends Context<?,?>, L extends LifePhase<C>>
            void iterateThroughProcessorsUntilAllAreDone(
            C context, TreeMap<L, L> stopAfterAndGetBack,
            L currentPhase, List<? extends Processor<C, L>> processors) {
        boolean shouldGoToAnotherRound;
        for (Processor<C, L> p : processors) {
            p.setProcessorWasCalledForThisPhase(currentPhase, false);
        }
        do {
            shouldGoToAnotherRound = false;
            for (Processor<C, L> p : processors) {
                Processor.Readiness readiness = p.isReadyToWork(currentPhase, context);
                if (readiness == Processor.Readiness.CONTEXT_IS_BROKEN) {
                    throw new RuntimeException("Context is broken for processor " + p);
                }
                if (readiness == Processor.Readiness.READY) {
                    try {
                        shouldGoToAnotherRound = true;
                        p.process(currentPhase, context);
                    } catch (InvocationTargetException iae) {
                        throw new RuntimeException(AUTD2Utils.unwrapThrownException(iae));
                    } catch (RuntimeException re) {
                        throw re;
                    } catch (Throwable throwable) {
                        throw new RuntimeException(throwable);
                    } finally {
                        p.setProcessorWasCalledForThisPhase(currentPhase, true);
                    }

                    // todo unit tests, check that proc has specified life phase that is later that the phase that it is bound to
                    L backToMineLifePhaseAfter = p.getBackToMineLifePhaseAfter(currentPhase);
                    if (backToMineLifePhaseAfter != null) {
                        stopAfterAndGetBack.put(backToMineLifePhaseAfter, currentPhase);
                    }
                }
            }
            /*
             * Iterating while some processor has done something
             * so we have to go over again one more time to make sure that
             * all the processors have completed their jobs.
             * This is important if one of the processor depends on the data
             * produced by another.
             */
        } while (shouldGoToAnotherRound);
    }

    static <L extends LifePhase<C>, C extends Context<?,?>, P extends Processor<C, L>> void mapToLifePhases(Map<L, List<P>> container, P newProc) {
        L[] interestedLifePhases = newProc.getLifePhasesInterestedIn();
        for (L interestedLifePhase : interestedLifePhases) {
            List<P> processors = container.get(interestedLifePhase);
            if (processors == null) {
                processors = new LinkedList<>();
                container.put(interestedLifePhase, processors);
            }
            wiselyAddNewProcessorToTheListOfExisting(newProc, interestedLifePhase, processors);
        }
    }

    static <L extends LifePhase<C>, C extends Context<?,?>, P extends Processor<C, L>>
                    void wiselyAddNewProcessorToTheListOfExisting(P newProc, L interestedLifePhase, List<P> existingProcessors) {
        if (existingProcessors.isEmpty()) {
            existingProcessors.add(newProc);
            return;
        }

        // checking for conflicts
        Processor.PhaseOwnership newPhaseOwnership = newProc.getPhaseOwnership(interestedLifePhase);
        if (newPhaseOwnership == Processor.PhaseOwnership.MANY_PER_PHASE) {
            existingProcessors.add(newProc);
            return;
        }

        boolean needToAdd = true;
        for (int i = 0, processorsSize = existingProcessors.size(); i < processorsSize; i++) {
            P existing = existingProcessors.get(i);
            if (existing.getPhaseOwnership(interestedLifePhase) == Processor.PhaseOwnership.ONE_PER_PHASE) {
                if (getProcessorWithHigherPriority(existing, newProc) != existing) {
                    existingProcessors.set(i, newProc);
                }
                needToAdd = false;
            }
        }

        if (needToAdd) {
            existingProcessors.add(newProc);
        }
    }

    /**
     * Comparing processors to determine that one of them is of higher execution priority
     * and that they are both in agreement on which one.
     * Results of invocation of method  <code>hasHigherPriorityThan()</code> should conform to each other -
     * if one says 'yes' then another one should say 'no'.
     */
    public static <P extends Processor<C, L>, C extends Context<?,?>, L extends LifePhase<C>>
                                            P getProcessorWithHigherPriority(P p1, P p2) {
        if (p1.hasHigherPriorityThan(p2) && !p2.hasHigherPriorityThan(p1)) {
            return p1;
        }
        if (p2.hasHigherPriorityThan(p1) && !p1.hasHigherPriorityThan(p2)) {
            return p2;
        }
        throw new UnsupportedOperationException(
                "The following processors are not compatible: " + p1 + ", " + p2);
    }

    /**
     * Returns lists of processors mapped by a life phases.
     */
    public static Map<TestCaseContext.TestCaseLifePhase, List<Processor.TestCaseProcessor>>
                                 getProc2LifePhaseForTheTestCaseMethod(Method method, Set<Processor<?,?>> usedProcessors) {
        Map<TestCaseContext.TestCaseLifePhase, List<Processor.TestCaseProcessor>> result =
                new HashMap<>();
        usedProcessors.forEach(p -> {
            if (p instanceof Processor.TestCaseProcessor && appliesToThisTestCase(method, p)) {
                Processor.TestCaseProcessor newTCProc = (Processor.TestCaseProcessor) p;
                mapToLifePhases(result, newTCProc);
            }
        });
        return result;
    }

    static boolean appliesToThisTestCase(Method method, Processor<?, ?> p) {

        // todo ! implement - won't work if processor attached to method argument

        Set<AnnotatedElement> annotatedElements = p.getAnnotatedElements().keySet();

        if (annotatedElements.contains(method)) {
            return true;
        }

        if (annotatedElements.contains(method.getDeclaringClass())) {
            return true;
        }

        for (AnnotatedElement annotatedElement : annotatedElements) {
            if (annotatedElement instanceof Field) {
                Field field = (Field) annotatedElement;
                // todo do we really have to check it - this should always be true ?
                if (field.getDeclaringClass().equals(method.getDeclaringClass())) {
                    return true;
                }
            }
            if (annotatedElement instanceof Constructor) {
                Constructor<?> constructor = (Constructor<?>) annotatedElement;
                // todo do we really have to check it - this should always be true ?
                if (constructor.getDeclaringClass().equals(method.getDeclaringClass())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns the original exception recorded in the passed context
     * - usually wrapped by other exceptions -
     * if any was thrown during testcase execution.
     */
    public static Throwable unwrapCoreExceptionFromITEs(TestCaseContext context) {
        Throwable t = context.getTestCaseResult().getThrownException();
        return unwrapThrownException(t);
    }

    /**
     * Returns the exception wrapped in the passed exception if any is wrapped by the passed
     * exception or the exception itself if it doesn't have cause.
     * If <code>null</code> value is passed then <code>null</code> is returned back.
     */
    public static Throwable unwrapThrownException(Throwable t) {
        if (t == null) {
            return null;
        }
        if (t.getCause() != null) {
            while (t instanceof InvocationTargetException) {
                t = t.getCause();
            }
        }
        return t;
    }

    /**
     * Iterates over the passes testcase context, going through all the life phases
     * and doing all needed actions.
     */
    public static TestResult iterateTestCaseLifePhase(TestCaseContext testCaseContext) {
        // this is needed especially if invocation of the same testcase happens several times
        // via MultiTest-base frameworks
        testCaseContext.resetTestResultAccumulator();

        // cleaning state of the processors
        Map<TestCaseContext.TestCaseLifePhase,List<Processor.TestCaseProcessor>> processorsMap =
                testCaseContext.getLifePhase2ProcessorsMap();
        processorsMap.values().forEach(processors -> processors.forEach(Processor.TestCaseProcessor::cleanupState));

        iterateLifePhases(
                testCaseContext,
                TestCaseContext.TestCaseLifePhase.values());
        return testCaseContext.getFinalTestResult();
    }

    /**
     * Searches through the testgroup class hierarchy for <code>@UseProcessor</code> annotation
     * and aggregates declared processor instances into the returned set.
     * @return aggregated set of processors used through all the class and interface hierarchy
     */
    public static Set<Processor<?,?>> getAllUserProcessors(Class<?> tgClass) {
        // todo in future replace manual aggregation with usage of bulk-data API
        final HashSet<Processor<?,?>> result = new HashSet<>();
        final List<Class<?>> classHierarchy = ReflectionUtils.getClassHierarchy(tgClass);
        for (Class<?> aClass : classHierarchy) {
            final UseProcessors annotation = aClass.getAnnotation(UseProcessors.class);
            if (annotation != null) {
                for (Class<? extends Processor<?,?>> pClass : annotation.value()) {
                    result.add(instantiateProcessor(pClass));
                }
            }
        }
        return result;
    }

    /**
     * Instantiates a processor using its default constructor
     * @param pClass class to use for instantiation
     * @return processor instance
     */
    static Processor<?,?> instantiateProcessor(Class<? extends Processor<?,?>> pClass) {
        try {
            return pClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate " + pClass + " using public no-arg constructor", e);
        }
    }
}
