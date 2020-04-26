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

package com.sun.tck.lib;

import com.oracle.tck.lib.autd2.processors.InterestedProcessors;
import com.oracle.tck.lib.autd2.processors.tc.ExceptionsExpected;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Test's method is marked with this annotation to indicate
 * that the test is passed only if the method invocation
 * leads to throwing specified exception.
 * Type of expected exception is specified through <code>value</code> field.
 * Throwing subclasses of the specified exception type is acceptable too.
 * It is strongly preferred for every framework that runs tests to recognise this annotation.
 * Please see the following code sample written in Test Generation Framework format:
 <pre>
     public Values ints() {
         return DataFactory.createValues(
                 Integer.MIN_VALUE, -100, -2, -1, 0, 1, 2, 100, Integer.MAX_VALUE);
     }

     <code>@TestCase</code>
     <code>@TestData("ints")</code>
     <code>@ExpectedException(java.lang.ArithmeticException.class)</code>
     public void testConstructor(int i) {
         int doesNotMatter = i / 0;
     }
 * </pre>
 * For every argument value <code>ArithmeticException</code> is expected to be thrown.
 * Note that expecting <code>RuntimeException</code> in the sample
 * above will also lead test to "Passed" result
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@InterestedProcessors(ExceptionsExpected.class)
public @interface ExpectedExceptions {
    /**
     * Specifies type of exception to expect
     * @return type of expected exception
     */
    Class<? extends Throwable>[] value();
}
