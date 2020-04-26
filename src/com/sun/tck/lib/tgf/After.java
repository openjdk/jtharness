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
import com.oracle.tck.lib.autd2.processors.tc.AfterTestCaseActions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * This annotation should be used to reference method that must be called
 * <b>after</b> execution of all micro test cases. In other words it's called after macro test case.
 * Method that is referenced via its name usually contain some clean-up actions related to macro test case.
 * <p>A sample code:<br/>
 * <pre>
 protected Values data() {
     return DataFactory.createValues("a", "b", "c");
 }
 private void after() {
     System.out.println("after called");
 }
 <code>@TestData("data")</code>
 <code>@After("after")</code>
 public void test(String s) {
     System.out.println("s = " + s);
 }
 * </pre>
 * Will produce the following output:
 * <pre>
 s = a
 s = b
 s = c
 after called
 * </pre>
 * <b>Important notes:</b>
 * Even if method with given name doesn't exist test result won't be affected.<br>
 * Even if method with given name throws any exception test result won't be affected.<br>
 * If method with given name overrides method from superclass, the overrider will be called.<br>
 * Method must accept no arguments. There are no restrictions for return type - it just will not be used anyhow.
 * If test itself throws exception, method referred in <code>@After</code> still will be called.
 *
 * Annotating a testgroup with this annotation means that referenced method
 * will be called after each testcase in the testgroup.
 * This allows to avoid annotating each and every testcase - annotation is 'pulled up'
 * to test group level.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@InterestedProcessors(AfterTestCaseActions.class)
public @interface After {

    /**
     * References method that must be called after macro testcase execution
     * @return name of method that must be called after macro test case
     */
    String value();
}
