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
import com.oracle.tck.lib.autd2.processors.tc.BeforeTestCaseActions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * This annotation should be used to reference method that must be called
 * <b>before</b> execution of all micro test cases. In other words it's called before macro test case.
 * Method that is referenced via its name usually contain some initialization sctions
 * related to macro test case.
 * <p>A sample code:<br/>
 * <pre>
 protected Values data() {
     return DataFactory.createValues("a", "b", "c");
 }
 private void before() {
     System.out.println("before called");
 }
 <code>@TestData("data")</code>
 <code>@Before("before")</code>
 public void test(String s) {
     System.out.println("s = " + s);
 }
 * </pre>
 * Will produce the following output:
 * <pre>
 before called
 s = a
 s = b
 s = c
 * </pre>
 * <b>Important notes:</b>
 * If method with given name doesn't exist then test will not be executed,
 * just considered as failed.<br>
 * If method with given name throws any exception then test it will not be executed, just considered as failed.<br>
 * If method with given name overrides method from superclass, the overrider will be called.<br>
 * Method must accept no arguments. There are no restrictions for return type - it just will not be used anyhow.
 *
 * Annotating a testgroup with this annotation means that referenced method
 * will be called before each testcase in the testgroup.
 * This allows to avoid annotating each and every testcase - annotation is 'pulled up'
 * to test group level.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@InterestedProcessors(BeforeTestCaseActions.class)
public @interface Before {

    /**
     * References method that must be called before macro testcase execution
     * @return name of method that must be called before macro test case
     */
    String value();
}
