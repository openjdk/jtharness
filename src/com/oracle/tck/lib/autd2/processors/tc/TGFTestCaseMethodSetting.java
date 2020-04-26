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

package com.oracle.tck.lib.autd2.processors.tc;

import com.oracle.tck.lib.autd2.TestCaseContext;
import com.oracle.tck.lib.autd2.processors.Processor;
import com.sun.tck.lib.NotApplicableException;
import com.sun.tck.lib.tgf.AbstractValue;
import com.sun.tck.lib.tgf.SomethingIsWrong;
import com.sun.tck.lib.tgf.TGFUtils;
import com.sun.tck.lib.tgf.Values;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Iterator;
import java.util.Map;

import static com.sun.tck.lib.tgf.TestDataCollector.getData;

/**
 * Processor is responsible for delegating responsibility
 * of testcase execution to Test Generation Framework if
 * there is some data to iterate over.
 */
public class TGFTestCaseMethodSetting extends Processor.TestCaseProcessor {

    private boolean dataCreationFailure;
    private Object[] rawArgs;
    private boolean freshRawArgs;
    private Values.ExcludedIndices notApplicableRowIndices;
    // index of the current row
    private long rowIndex;
    private Iterator<Object[]> dataIterator;
    private Object[] currentValue;

    /**
     * Cleans up everything.
     */
    @Override
    public void cleanupState() {
        dataCreationFailure = false;
        rawArgs = null;
        freshRawArgs = false;
        notApplicableRowIndices = null;
        rowIndex = 0;
        dataIterator = null;
        currentValue = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(TestCaseContext.TestCaseLifePhase lifePhase, final TestCaseContext context) {
        // todo refactor
        if (lifePhase == TestCaseContext.TestCaseLifePhase.AFTER_INVOCATION) {
            cleanupArgs();
        } else {
            processSettingMethodPhase(lifePhase, context);
        }

    }

    private void cleanupArgs() {
        if (freshRawArgs) {
            for (Object object : rawArgs) {
                if (object instanceof AbstractValue) {
                    ((AbstractValue) object).cleanUp();
                }
            }
        }
        freshRawArgs = false;
    }

    private void processSettingMethodPhase(TestCaseContext.TestCaseLifePhase lifePhase, final TestCaseContext context) {

        try {
            if (dataIterator == null) {
                final Object testGroupInstance = context.getTestGroupInstance();
                Values data = collectReferencedData(context, testGroupInstance);
                // todo cache somewhere somehow the generated exclude list
                Map<String, Values.ExcludedIndices> excludeList = TGFUtils.createExcludeList(context.getParentContext().getExecutionArgs());
                String testCaseName = context.getTestCaseName();
                if (excludeList.containsKey(testCaseName)) {
                    data.markNotApplicable(excludeList.get(testCaseName));
                }
                if (data.isNotApplicable()) {
                    throw new NotApplicableException(data.getReasonNotApplicaple());
                }
                dataIterator = data.iterator();
                rowIndex = -1;
                notApplicableRowIndices = data.getNotApplicableRowIndices();
            }
        } catch (SomethingIsWrong somethingIsWrong) {
            dataCreationFailure = true;
            context.setThrownException(somethingIsWrong);
            return;
        }

        if (dataIterator.hasNext() && !wasProcessorCalledForThisPhase(lifePhase)) {
            rowIndex++;
            rawArgs = dataIterator.next();
            freshRawArgs = true;
            final Object[] args = new Object[rawArgs.length];
            for (int i = 0; i < rawArgs.length; i++) {
                if (rawArgs[i] instanceof AbstractValue) {
                    args[i] = ((AbstractValue) rawArgs[i]).doCreate();
                } else {
                    args[i] = rawArgs[i];
                }
            }
            currentValue = args;
        }

        if (currentValue != null) {
            context.setCallableTestCase(() -> {
                context.setTestCaseInvocationArgValues(currentValue);
                context.setRowIndex(rowIndex);
                if (notApplicableRowIndices.isExcluded(rowIndex)) {
                    throw new NotApplicableException();
                }

                Object[] adaptedArgs = adaptArgsForVararg(context.getTestCaseMethod(), currentValue);
                return context.getTestCaseMethod().invoke(context.getTestGroupInstance(), adaptedArgs);
            });
        }
    }

    private static Object[] adaptArgsForVararg(Method method, Object[] args) {
        if (!method.isVarArgs()) {
            return args;
        }

        Parameter[] parameters = method.getParameters();
        Class<?> varargArrayParameterType = parameters[parameters.length - 1].getType();
        Class<?> varargParameterType = varargArrayParameterType.getComponentType();

        assert varargArrayParameterType.isArray();

        if (args.length == parameters.length && varargArrayParameterType.isInstance(args[args.length - 1])) {
            return args;
        }

        int numberOfArgsToWrap = args.length - parameters.length + 1;
        Object wrapper = Array.newInstance(varargParameterType, numberOfArgsToWrap);

        if (varargParameterType.isPrimitive()) {
            for (int i = 0; i < numberOfArgsToWrap; i++) {
                Object arg = args[parameters.length - 1 + i];

                if (varargParameterType == boolean.class) {
                    ((boolean[])wrapper)[i] = (boolean)arg;
                } else if (varargParameterType == byte.class) {
                    ((byte[])wrapper)[i] = (byte)arg;
                } else if (varargParameterType == short.class) {
                    ((short[])wrapper)[i] = (short)arg;
                } else if (varargParameterType == int.class) {
                    ((int[])wrapper)[i] = (int)arg;
                } else if (varargParameterType == long.class) {
                    ((long[])wrapper)[i] = (long)arg;
                } else if (varargParameterType == float.class) {
                    ((float[])wrapper)[i] = (float)arg;
                } else if (varargParameterType == double.class) {
                    ((double[])wrapper)[i] = (double)arg;
                } else {
                    ((char[])wrapper)[i] = (char)arg;
                }
            }
        } else {
            System.arraycopy(args, args.length - numberOfArgsToWrap, wrapper, 0, numberOfArgsToWrap);
        }

        Object[] adaptedArgs = new Object[parameters.length];
        System.arraycopy(args, 0, adaptedArgs, 0, parameters.length - 1);
        adaptedArgs[parameters.length - 1] = wrapper;

        return adaptedArgs;
    }

    /**
     * Collects referenced data.
     * Subclasses may override this method if any non-default method
     * of data collection is used.
     */
    protected Values collectReferencedData(TestCaseContext context, Object testGroupInstance) {

        return getData(testGroupInstance,
                testGroupInstance.getClass(),
                context.getLog(),
                context.getTestCaseMethod());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestCaseContext.TestCaseLifePhase[] getLifePhasesInterestedIn() {
        return new TestCaseContext.TestCaseLifePhase[] {
                TestCaseContext.TestCaseLifePhase.SETTING_WHAT_TO_CALL,
                TestCaseContext.TestCaseLifePhase.AFTER_INVOCATION
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestCaseContext.TestCaseLifePhase getBackToMineLifePhaseAfter(TestCaseContext.TestCaseLifePhase currentPhase) {
        if (currentPhase == TestCaseContext.TestCaseLifePhase.SETTING_WHAT_TO_CALL) {
            return TestCaseContext.TestCaseLifePhase.PROCESSING_RESULT;
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Readiness isReadyToWork(TestCaseContext.TestCaseLifePhase currentPhase, TestCaseContext context) {

        if (currentPhase == TestCaseContext.TestCaseLifePhase.AFTER_INVOCATION) {
            return super.isReadyToWork(currentPhase, context);
        }

        // if something wrong has happened on pre-testcase stage
        // there's a predefined result, nothing to do
        if (context.getOverridingResult() != null)  {
            return Readiness.NOTHING_FOR_ME;
        }

        if(dataCreationFailure) {
            return Readiness.NOTHING_FOR_ME;
        }

        if (wasProcessorCalledForThisPhase(currentPhase)) {
            return Readiness.NOTHING_FOR_ME;
        }
        if (dataIterator == null || dataIterator.hasNext()) {
            return Readiness.READY;
        } else {
            return Readiness.NOTHING_FOR_ME;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PhaseOwnership getPhaseOwnership(TestCaseContext.TestCaseLifePhase interestedLifePhase) {
        if (interestedLifePhase == TestCaseContext.TestCaseLifePhase.SETTING_WHAT_TO_CALL) {
            return PhaseOwnership.ONE_PER_PHASE;
        }
        if (interestedLifePhase == TestCaseContext.TestCaseLifePhase.AFTER_INVOCATION) {
            return PhaseOwnership.MANY_PER_PHASE;
        }
        return PhaseOwnership.MANY_PER_PHASE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasHigherPriorityThan(Processor anotherProc) {
        return true;
    }


}
