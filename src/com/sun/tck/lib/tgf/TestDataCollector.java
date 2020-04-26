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

import com.sun.tck.lib.tgf.data.*;
import com.sun.tck.lib.ExecuteIf;
import com.sun.tck.lib.ExecuteIfNot;
import com.sun.tck.lib.NotApplicableException;

import java.lang.reflect.Method;
import java.lang.annotation.Annotation;
import java.io.PrintWriter;
import static java.text.MessageFormat.format;

/**
 * This is a helper class to collect data
 * that is inlined or referenced by <code>@TestData</code> annotation
 * or any other annotation.
 */
public class TestDataCollector {

    private static final String TEST_DATA_ANN_NAME = "@TestData";

    private TestDataCollector() {  }

    /**
     * Collects data referenced some way by the passed testcase method
     */
    public static Values getData(Object testInstance, Class<?> testClass, PrintWriter log, Method testMethod) throws SomethingIsWrong {
        checkForConflictingData(testMethod);
        TestData testData = testMethod.getAnnotation(TestData.class);
        if (testData != null) {
            return getTestDataFromTextReference(testInstance, testClass, log, testData.value() );
        } else {
            return collectInlinedData( testMethod, testInstance, testClass, log );
        }
    }

    private static void checkForConflictingData(Method method) {
        if ( method.getAnnotation(TestData.class) != null && hasSomeInlinedData(method) ) {
            throw new SomethingIsWrong(
                    format("Method \"{0}\" is annotated with @TestData and has some data attached to parameters.", method.getName()));
        }
        if ( !hasSomeInlinedData(method) && method.getAnnotation(Operation.class) != null) {
            throw new SomethingIsWrong(
                    format("Method \"{0}\" has no inlined data, but is annotated with @Operation.", method.getName()));
        }
        if ( method.getParameterTypes().length == 1 && method.getAnnotation(Operation.class) != null) {
            throw new SomethingIsWrong(
                    format("Annotating method \"{0}\" which has only one parameter with @Operation is redundant.", method.getName()));
        }
    }

    static void checkForCorrectnessOnNonParameterizedTestCase(Method method) {
        if (method.getAnnotation(Operation.class) != null) {
            throw new SomethingIsWrong(
                    format("Method \"{0}\" has no parameters, but is annotated with @Operation.", method.getName()));
        }
    }

    /**
     * Collects data referenced some way by the passed data holder
     * @param dataHolderName string reference to the data holder
     */
    public static Values getTestDataFromTextReference(Object testInstance, Class<?> testClass, PrintWriter log,
                                           String dataHolderName) {
        Object result = TGFUtils.getRawDataFromTextReference(testInstance, testClass,
                log, dataHolderName, TEST_DATA_ANN_NAME);
        return DataFactory.adaptData(result);
    }

    /**
     * Collects inlined data
     * @param method method which has some data inlined
     * @param testInstance testgroup instance
     * @param testClass testgroup class
     * @param log output stream
     * @return resulting data instance
     */
    public static Values collectInlinedData(Method method, Object testInstance,
                                            Class<?> testClass, PrintWriter log) {
        // todo move away for better testability
        Values result = null;
        NodeIterator operationIterator;

        Operation operation = method.getAnnotation(Operation.class);

        if (operation != null) {
            switch (operation.value()) {
                case MULTIPLY : operationIterator = new MultiplyIterator();
                    break;
                case PSEUDOMULTYPLY :
                    operationIterator = new PseudoMultiplyIterator();
                    break;
                default:
                    operationIterator = new PseudoMultiplyIterator();
            }
        } else {
            operationIterator = new PseudoMultiplyIterator();
        }

        for (Annotation[] annotations : method.getParameterAnnotations()) {
            if (annotations.length > 1) {
                throw new SomethingIsWrong("Parameter annotated with more that one anoonation");
            }
            if (annotations.length == 1) {
                Annotation annotation = annotations[0];
                Processor processor = getProcessor(annotation);
                Values values = processor.process(annotation, testInstance, testClass, log);
                if (result == null) {
                    result = values;
                } else {
                    result = result.operate(values, operationIterator);
                }
            }
        }
        return result;
    }

    private static Processor getProcessor(Annotation annotation) {

        Class<? extends Annotation> type = annotation.annotationType();
        if (TestData.class.equals(type)) {
            return (aClass, testInstance, testClass, log) -> {
                String reference = ((TestData) aClass).value();
                return getTestDataFromTextReference(testInstance, testClass, log, reference);
            };
        } else if (Ints.class.equals(type)) {
            return (aClass, testInstance, testClass, log) -> {
                int[] ints = ((Ints) aClass).value();
                Object[] array = new Object[ints.length];
                for (int i = 0; i < array.length; i++) {
                    array[i] = ints[i];
                }
                return DataFactory.createColumn(array);
            };
        } else if (Strings.class.equals(type)) {
            return (aClass, testInstance, testClass, log) -> DataFactory.createColumn((Object[]) ((Strings) aClass).value());
        } else if (Booleans.class.equals(type)) {
            return (aClass, testInstance, testClass, log) -> {
                boolean[] booleans = ((Booleans) aClass).value();
                Object[] array = new Object[booleans.length];
                for (int i = 0; i < array.length; i++) {
                    array[i] = booleans[i];
                }
                return DataFactory.createColumn(array);
            };
        } else if (Classes.class.equals(type)) {
            return (aClass, testInstance, testClass, log) -> DataFactory.createColumn((Object[]) ((Classes) aClass).value());
        } else if (Longs.class.equals(type)) {
            return (aClass, testInstance, testClass, log) -> {
                long[] longs = ((Longs) aClass).value();
                Object[] array = new Object[longs.length];
                for (int i = 0; i < array.length; i++) {
                    array[i] = longs[i];
                }
                return DataFactory.createColumn(array);
            };
        } else if (Bytes.class.equals(type)) {
            return (aClass, testInstance, testClass, log) -> {
                byte[] bytes = ((Bytes) aClass).value();
                Object[] array = new Object[bytes.length];
                for (int i = 0; i < array.length; i++) {
                    array[i] = bytes[i];
                }
                return DataFactory.createColumn(array);
            };
        } else if (Chars.class.equals(type)) {
            return (aClass, testInstance, testClass, log) -> {
                char[] chars = ((Chars) aClass).value();
                Object[] array = new Object[chars.length];
                for (int i = 0; i < array.length; i++) {
                    array[i] = chars[i];
                }
                return DataFactory.createColumn(array);
            };
        } else if (Doubles.class.equals(type)) {
            return (aClass, testInstance, testClass, log) -> {
                double[] doubles = ((Doubles) aClass).value();
                Object[] array = new Object[doubles.length];
                for (int i = 0; i < array.length; i++) {
                    array[i] = doubles[i];
                }
                return DataFactory.createColumn(array);
            };
        } else if (Floats.class.equals(type)) {
            return (aClass, testInstance, testClass, log) -> {
                float[] floats = ((Floats) aClass).value();
                Object[] array = new Object[floats.length];
                for (int i = 0; i < array.length; i++) {
                    array[i] = floats[i];
                }
                return DataFactory.createColumn(array);
            };
        } else if (Shorts.class.equals(type)) {
            return (aClass, testInstance, testClass, log) -> {
                short[] shorts = ((Shorts) aClass).value();
                Object[] array = new Object[shorts.length];
                for (int i = 0; i < array.length; i++) {
                    array[i] = shorts[i];
                }
                return DataFactory.createColumn(array);
            };
        }
        throw new RuntimeException("Data process not found for annotation of type " + type);
    }

    static boolean hasSomeDataToUse(Method method) {
        return method.getAnnotation(TestData.class) != null || hasSomeInlinedData(method);
    }

    /**
     * Checking for @ExecuteIf(Not) annotations.
     * May throw <code>SomethingIsWrong</code> exception or <code>NotApplicableException</code>
     */
    public static void checkIfTestCaseShouldBeExecuted(Method method, Object testInstance, Class<?> testClass, PrintWriter log) {
        ExecuteIf executeIf = method.getAnnotation(ExecuteIf.class);
        ExecuteIfNot executeIfNot = method.getAnnotation(ExecuteIfNot.class);
        if (executeIf == null && executeIfNot == null) {
            return;
        }
        if (executeIf != null && executeIfNot != null) {
            throw new SomethingIsWrong(
                    "@ExecuteIf and @ExecuteIfNot could not be used together for one testcase");
        }
        String fieldOrMethodName;
        String reason;

        if (executeIf != null) {
            fieldOrMethodName = executeIf.value();
            reason = executeIf.reason();
        } else {
            fieldOrMethodName = executeIfNot.value();
            reason = executeIfNot.reason();
        }

        Boolean booleanResult;
        try {
            booleanResult = (Boolean) TGFUtils.getRawDataFromTextReference(
                    testInstance, testClass, log, fieldOrMethodName,
                    executeIf != null ? "@ExecuteIf" : "@ExecuteIfNot");
        } catch (ClassCastException e) {
            throw new SomethingIsWrong("\"" + fieldOrMethodName + "\" should be of boolean type");
        }

        if (booleanResult == null) {
            throw new SomethingIsWrong("\"" + fieldOrMethodName + "\" has null value or returned null");
        }

        if (executeIf != null && !booleanResult
            || executeIfNot != null  && booleanResult) {
            throw new NotApplicableException(reason);
        }
    }

    private static interface Processor {
        Values process(Annotation aClass, Object testInstance, Class<?> testClass, PrintWriter log);
    }

    static boolean hasSomeInlinedData(Method method) {
        for (Annotation[] annotations : method.getParameterAnnotations()) {
            if (annotations.length > 1) {
                return false;
            }
            if (annotations.length == 1) {
                Class<? extends Annotation> annType = annotations[0].annotationType();
                if (isInlinedDataType(annType)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isInlinedDataType(Class<? extends Annotation> annType) {
        return TestData.class.equals(annType)
                || Ints.class.equals(annType)
                || Strings.class.equals(annType)
                || Booleans.class.equals(annType)
                || Longs.class.equals(annType)
                || Doubles.class.equals(annType)
                || Classes.class.equals(annType)
                || Floats.class.equals(annType)
                || Bytes.class.equals(annType)
                || Chars.class.equals(annType)
                || Shorts.class.equals(annType);
    }

}
