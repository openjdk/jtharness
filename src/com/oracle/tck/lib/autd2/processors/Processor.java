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

package com.oracle.tck.lib.autd2.processors;

import com.oracle.tck.lib.autd2.Context;
import com.oracle.tck.lib.autd2.LifePhase;
import com.oracle.tck.lib.autd2.TestCaseContext;
import com.oracle.tck.lib.autd2.TestGroupContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;

/**
 * A processor which is responsible for some actions with TestGroup or TestCase
 * context.
 *
 * @see com.oracle.tck.lib.autd2.processors.Processor.TestCaseProcessor
 * @see com.oracle.tck.lib.autd2.processors.Processor.TestGroupProcessor
 */
public abstract class Processor<C extends Context, L extends LifePhase<C>> implements Cloneable {


    /**
     * Returned value of this method is used as an answer to "yes/no?" question
     * about applicability of a particular processor instance to the passed
     * annotation instance.
     *
     * If overriding methods return false then processor is considered as not
     * applicable even the annotation that is is interested in exists.
     *
     * This method is useful when processor is not only interested in an
     * annotation but in specific value specified in that annotation.
     *
     * For example the following annotation
     * <code>@Test(expectedExceptions=ClassCastException.class)</code> might have
     * or might have not expectations for an exception to be thrown - it means
     * that processor taking care of these expectations of an exception might not
     * be needed. That kind of processor inspects annotation passed to the method
     * and gives the final answer of its applicability.
     *
     * @return true by default
     */
    public boolean isProcessorApplicableToAnnotationInstance(Annotation ann) {
        return true;
    }

    /**
     * This method should be overridden by a processor implementation if it wants
     * to be run after some other processors. Only processors belonging to the
     * same life phase are taken into account.
     *
     * @return an empty list by default. The overriding method should return the
     * list of processors after which the overriding processor should be run.
     */
    public List<Class<? extends Processor<C, L>>> useAfter() {
        return Collections.emptyList();
    }

    /**
     * This method should be overridden by a processor implementation if it wants
     * to be run before some other processors. Only processors belonging to the
     * same life phase are taken into account.
     *
     * @return an empty list by default. The overriding method should return the
     * list of processors before which the overriding processor should be run.
     */
    public List<Class<? extends Processor<C, L>>> useBefore() {
        return Collections.emptyList();
    }

    /**
     * Convenient base class for testcase processors.
     */
    public abstract static class TestCaseProcessor
                    extends Processor<TestCaseContext, TestCaseContext.TestCaseLifePhase> {

        /**
         * This method is called before going over the particular testcase It should
         * be overridden if processor readiness is affected by its state. Stateful
         * processors must reset their state here. Default implementation does
         * nothing.
         */
        public void cleanupState() {
            // does nothing by default
        }

        /**
         * Method returns only annotations for the current testcase method
         * (specified in the passed testcase context) that this processor is
         * interested in.
         */
        protected Set<Annotation> getAnnotationsInterestedIn(TestCaseContext c) {
            return getAnnotatedElements().get(c.getTestCaseMethod());
        }

        /**
         * Returns all annotations attached to the current testcase method
         * (specified in the passed testcase context).
         */
        protected Set<Annotation> getAllAnnotations(TestCaseContext c) {
            final Method testCaseMethod = c.getTestCaseMethod();
            HashSet<Annotation> annotations = new HashSet<>();
            Collections.addAll(annotations, testCaseMethod.getDeclaredAnnotations());
            return annotations;
        }

        /**
         * Method returns the only annotation attached to a testcase from those that
         * a processor is interested in.
         *
         * Using this method makes sense when
         * <code>areAnnotationsMutuallyExclusive()</code> returns true or processor
         * is interested only in one annotation.
         *
         * @throws UnsupportedOperationException if more than one annotation is
         * returned by
         * method <code>getAnnotationsInterestedIn(TestCaseContext)</code>.
         *
         */
        protected Annotation getTheOnlyAnnotationInterestedIn(TestCaseContext c) {
            final Set<Annotation> annotations = getAnnotationsInterestedIn(c);
            if (annotations == null) {
                return null;
            }
            if (annotations.size() > 1) {
                throw new UnsupportedOperationException(
                                "More than one annotation that processor is interested in "
                                + "is attached to a testcase " + c.getTestCaseName()
                                + ". This method should not be used");
            }
            return annotations.iterator().next();
        }
    }

    /**
     * Convenient base class for testgroup processors.
     */
    public abstract static class TestGroupProcessor
                    extends Processor<TestGroupContext, TestGroupContext.TestGroupLifePhase> {
    }

    /**
     * Returns all annotations attached to the testgroup (specified in the passed
     * context).
     */
    protected Set<Annotation> getAllAnnotationsAttachedToTestGroup(Context c) {
        final Class testGroup = c.getTestGroupInstance().getClass();
        HashSet<Annotation> annotations = new HashSet<>();
        Collections.addAll(annotations, testGroup.getDeclaredAnnotations());
        return annotations;
    }

    /**
     * Method returns only annotations for the testgroup class (specified in the
     * passed context) that this processor is interested in.
     */
    protected Set<Annotation> getTestGroupAnnotationsInterestedIn(Context c) {
        return getAnnotatedElements().get(c.getTestGroupInstance().getClass());
    }

    /**
     * Method returns the only annotation attached to the testgroup class from
     * those that a processor is interested in.
     *
     * Using this method makes sense when
     * <code>areAnnotationsMutuallyExclusive()</code> returns true or processor is
     * interested only in one annotation.
     *
     * @throws UnsupportedOperationException if more than one annotation is
     * returned by
     * method <code>getTestGroupAnnotationsInterestedIn(TestCaseContext)</code>
     *
     */
    protected Annotation getTheOnlyTestGroupAnnotationInterestedIn(Context c) {
        final Set<Annotation> annotations =
                        getTestGroupAnnotationsInterestedIn(c);
        if (annotations == null) {
            return null;
        }
        if (annotations.size() > 1) {
            throw new UnsupportedOperationException(
                            "More than one annotation that processor is interested in "
                            + "is attached to testgroup " + c.getTestGroupInstance().getClass().getSimpleName()
                            + ". This method should not be used");
        }
        return annotations.iterator().next();
    }
    /**
     * indicates that processor was called once so method
     * <code>isReadyToWork</code> has to return
     * <code>Readiness.NOTHING_FOR_ME</code> by default.
     */
    private final Map<L, Boolean> wasCalledForPhase = new HashMap<>();

    /**
     * Returns whether this processor instance was already called by @UTD2 core.
     */
    public boolean wasProcessorCalledForThisPhase(L currentPhase) {
        final Boolean wasCalled = wasCalledForPhase.get(currentPhase);
        return wasCalled == null ? false : wasCalled;
    }

    /**
     * This is called before each life phase with 'false' argument and with 'true'
     * after process method invocation.
     */
    public void setProcessorWasCalledForThisPhase(L currentPhase, boolean wasCalled) {
        // todo test this behavior
        wasCalledForPhase.put(currentPhase, wasCalled);
    }

    /**
     * If two processors having both ONE_PER_PHASE phase ownership pretend for one
     * life phase then only one of the must be chosen but they both have to agree
     * which one. So for this case
     *
     * @UTD2 core calls this method for both processors and verifies that one
     * processors has returned true and another - false.
     *
     * Otherwise an exception is thrown reporting that two processors cannot come
     * to agreement about which should be used
     * @see <code>PhaseOwnership.ONE_PER_PHASE</code>
     */
    public boolean hasHigherPriorityThan(Processor<C, L> anotherProc) {
        throw new UnsupportedOperationException(
                        "Comparing processors is not supported by default");
    }

    public static enum PhaseOwnership {

        /**
         * only one Processor if having this kind ownership is allowed to exist for
         * each Life Phase.
         */
        ONE_PER_PHASE,
        /**
         * many processors of this kind could be bound to particular Life Phase.
         */
        MANY_PER_PHASE
    }

    /**
     *
     * When there are several processors bound for particular life phase their
     * order is not defined. However the situation when one processors must be
     * called after the other is possible. This usually means that context is not
     * supplied yet with some data required by one of the processors. If some data
     * in context is not yet provided a processor reports NOT_READY_YET and all
     * the processors will be iterated once again until noone reports
     * NOT_READY_YET.
     *
     * @see <code>Processor.isReadyToWork()</code>
     */
    public enum Readiness {

        /**
         * Means that processor is ready to participate in life phase.
         */
        READY,
        /**
         * Reports that there's nothing to do for the processor on this round.
         * The framework core will iterate the processors until all of them report this status.
         */
        NOTHING_FOR_ME,
        /**
         * Context is broken for processor - this might mean conflict with other
         * processor.
         */
        CONTEXT_IS_BROKEN
    }

    /**
     * Returns list of life phases that processor should be used for. IMPORTANT:
     * It is only guaranteed that the processor's process() method should be
     * called during the specified life phases (only if
     * <code>isReadyToWork()</code> has returned READY beforehand). The relative
     * order of different processors bound to the same life phase is undefined.
     */
    public abstract L[] getLifePhasesInterestedIn();


    /**
     * Return kind of life phase ownership.
     *
     * @see PhaseOwnership
     */
    public abstract PhaseOwnership getPhaseOwnership(L interestedLifePhase);

    /**
     * All work is done by this method. Called by
     *
     * @UTD2 core is method {@code isReadyToWork} returned READY
     */
    public abstract void process(L lifePhase, C context) throws Throwable;

    /**
     * If this method returns non-null then after the particular life phase
     * process execution will return back to the specified life phase. Returns
     * null by default meaning no return is needed.
     */
    public L getBackToMineLifePhaseAfter(L currentPhase) {
        // todo for now used by TGF only. Think about redesigning this feature
        // todo test !!!
        return null;
    }
    private final HashMap<AnnotatedElement, Set<Annotation>> annotatedElements = new HashMap<>();

    /**
     * Appends annotated element to the annotated elements which this processor is
     * applicable to.
     */
    public void addAnnotatedElement(AnnotatedElement element, Annotation ann) {
        Set<Annotation> annotations = annotatedElements.get(element);
        if (annotations == null) {
            annotations = new HashSet<>();
            annotatedElements.put(element, annotations);
        }
        if (areAnnotationsMutuallyExclusive()) {
            if (annotations.size() > 0 && !annotations.contains(ann)) {
                throw new IllegalArgumentException("Processor " + this.getClass().getName()
                                + " requires mutually exclusive usage of annotations.\n"
                                + "For " + element + " you have declared both "
                                + annotations.iterator().next().annotationType().getSimpleName()
                                + " and " + ann.annotationType().getSimpleName()
                                + ", they cannot exist together");
            }
        }
        annotations.add(ann);
    }

    /**
     * Elements that were annotated with annotation that this processor is
     * interested in. AnnotatedElements could be types, fields, methods.
     */
    public Map<AnnotatedElement, Set<Annotation>> getAnnotatedElements() {
        return Collections.unmodifiableMap(annotatedElements);
    }

    /**
     * Indicates whether processor is ready to work or not.
     *
     * For example this method might be used to prevent doing some actions more
     * times than needed if method {@code getBackToMineLifePhaseAfter()} of some
     * particular processor returns non-null.
     *
     *
     * IMPORTANT: This method is called several times - so the processor should be
     * able to give
     * <code>Readiness.NOTHING_FOR_ME</code> answer after it has finished its job.
     *
     * Default implementation of this method returns Readiness.READY if a
     * processor has not been yet called and Readiness.NOTHING_FOR_ME when it was
     * called already.
     *
     * @see Readiness
     * @return whether processor is ready to work or not.
     */
    public Readiness isReadyToWork(L currentPhase, C context) {
        // todo test this method is expected to return the same result no matter how many times it was called
        // todo use and create tests
        if (!wasProcessorCalledForThisPhase(currentPhase)) {
            return Readiness.READY;
        } else {
            return Readiness.NOTHING_FOR_ME;
        }
    }

    /**
     * If this method is overridden and returns true and more than one annotation
     * that processor is interested in are used for the same annotated element an
     * <code>IllegalArgumentException</code> is thrown.
     * <P>
     * Return
     * <code>false</code> by default.
     * </P>
     *
     * @return if annotations that the processor handles should be used
     * exclusively for the same annotated element.
     */
    protected boolean areAnnotationsMutuallyExclusive() {
        return false;
    }
}
