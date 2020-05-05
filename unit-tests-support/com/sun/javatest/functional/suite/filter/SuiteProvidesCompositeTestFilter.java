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

package com.sun.javatest.functional.suite.filter;

import com.sun.javatest.CompositeFilter;
import com.sun.javatest.TestDescription;
import com.sun.javatest.TestEnvironment;
import com.sun.javatest.TestFilter;
import com.sun.javatest.TestSuite;

import java.io.File;
import java.util.Map;

public class SuiteProvidesCompositeTestFilter extends TestSuite {
    public SuiteProvidesCompositeTestFilter(File root, Map<String, String> tsInfo, ClassLoader cl) throws Fault {
        super(root, tsInfo, cl);
    }

    @Override
    public TestFilter createTestFilter(TestEnvironment filterEnv) {
        TestFilter filter_01 = new TestFilter() {
            @Override
            public String getName() {
                return "Name: doesn't like 1s";
            }

            @Override
            public String getDescription() {
                return "Description: doesn't like 1s";
            }

            @Override
            public String getReason() {
                return "rejecting tests with names ending with 1";
            }

            @Override
            public boolean accepts(TestDescription td) throws Fault {
                return !td.getId().endsWith("1");
            }
        };

        TestFilter filter_02 = new TestFilter() {
            @Override
            public String getName() {
                return "Name: doesn't like 2s";
            }

            @Override
            public String getDescription() {
                return "Description: doesn't like 2s";
            }

            @Override
            public String getReason() {
                return "rejecting tests with names ending with 2";
            }

            @Override
            public boolean accepts(TestDescription td) throws Fault {
                return !td.getId().endsWith("2");
            }
        };

        return new CompositeFilter(filter_01, filter_02);
    }
}
