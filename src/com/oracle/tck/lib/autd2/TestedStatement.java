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

import java.lang.annotation.*;

/**
 * This annotation would contain the text of the statement
 * derived from the specification which is being tested
 * by the testcase method it is attached to.
 *
 * The statement would be included in the test description
 * generated when tests are processed by the build.
 *
 * In addition, during runtime AUTD2 prints the tested statement
 * to the generated output if a particular testcase fails.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Repeatable(TestedStatements.class)
public @interface TestedStatement {

    String DEFAULT_SOURCE = "Specified in @TestedAPI attached to testgroup";

    /**
     * The text of the statement
     */
    String[] value();

    /**
     * Source of the statement, tested member basically - package/class/method/field
     * who's specification contains the quoted text
     *
     * If not specified then by default it will be calculated from data
     * provided in <code>@TestedAPI</code> annotation:
     * {code}
     *     @TestedAPI.testedPackage() + @TestedAPI.testedClass() + @TesteadAPI.testedMember()
     * {code}
     */
    String source() default DEFAULT_SOURCE;

    /**
     * Indicates if the mentioned statement is considered as covered by the associated tests.
     * Default value is 'true'.
     * @return true if the mentioned statement is considered as covered, false otherwise
     */
    boolean covered() default true;
}
