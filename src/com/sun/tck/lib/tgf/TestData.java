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

import com.oracle.tck.lib.autd2.processors.InterestedProcessors;
import com.oracle.tck.lib.autd2.processors.tc.TGFTestCaseMethodSetting;
import com.oracle.tck.lib.autd2.processors.tg.TGFWrongAnnotationsChecking;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * A test case is marked with this annotation to bind it with
 * a support method of type <code>Values</code> that is used as a data initializer.
 * This annotation marks a test method to designate it as a 'parameterized test'.
 * Such testcases will be recognized by TGF runner and invoked iteratively
 * with different argument values.
 * The parameter of the annotation is a name of method
 * that is used as data initializer.
 * If a testcase is also annotated with <code>@Before</code>,
 * the data-initializing method will be called after it.
 * Generally call sequence looks like:
 * <pre>
   1. > @Before
   2. > @TestData
   3. > .. all the micro testcases..
   4. > @After
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
@InterestedProcessors({TGFTestCaseMethodSetting.class, TGFWrongAnnotationsChecking.class})
public @interface TestData {
    /**
     * Should return name of method that will be invoked by
     * <code>TGFRunner</code> before test execution to initialize test data.
     * Referenced method must have type <code>Values</code>
     * Please see the following code sample.
     * <pre>
     public Values myData() {
         return DataFactory.createValues(1, 2, 3);
     }
     <code>@TestCase</code>
     <code>@TestData("myData")</code>
     public void test(int i) {
         System.out.println("i = " + i);
     }
     * </pre>
     * Will produce the following output:
     * <pre>
     i = 1
     i = 2
     i = 3
     * </pre>
     * I.e. we will have three micro testcases created in the sample above.
     * @return data-initializer method name
     */
    String value();

}
