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

import com.oracle.tck.lib.autd2.TestGroupContext;
import com.oracle.tck.lib.autd2.processors.InterestedInAnnotations;
import com.oracle.tck.lib.autd2.processors.Processor;
import com.sun.tck.test.TestCase;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

/**
 * Verifies that methods annotated with {@code TestCase} annotation are public.
 */
@InterestedInAnnotations(TestCase.class)
public class NonPublicTestCasesReporter extends Processor.TestGroupProcessor {

    @Override
    public TestGroupContext.TestGroupLifePhase[] getLifePhasesInterestedIn() {
        return new TestGroupContext.TestGroupLifePhase[] {TestGroupContext.TestGroupLifePhase.BEFORE_TESTGROUP};
    }

    @Override
    public PhaseOwnership getPhaseOwnership(
            TestGroupContext.TestGroupLifePhase interestedLifePhase) {
        return PhaseOwnership.MANY_PER_PHASE;
    }

    @Override
    public void process(TestGroupContext.TestGroupLifePhase lifePhase, TestGroupContext context) throws Throwable {
        final Map<AnnotatedElement,Set<Annotation>> annotatedElements = getAnnotatedElements();
        for (AnnotatedElement element : annotatedElements.keySet()) {
            final Method method = (Method) element;
            if (!Modifier.isPublic(method.getModifiers())) {
                throw new IllegalArgumentException(
                        "Method annotated with @Testcase \"" + method.getName() + "\" should be public.");
            }
        }
    }

}
