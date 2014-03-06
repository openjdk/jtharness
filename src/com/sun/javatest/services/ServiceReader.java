/*
 * $Id$
 *
 * Copyright (c) 2009, 2012, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.services;

import com.sun.javatest.TestSuite;
import java.util.Map;
import java.util.Set;

/**
 * Interface, used to read information about services and map from tests to
 * services. This component is need to be provided by test suite, as it done
 * for other pluggable components.
 * <p>
 * Default implementation is {@link com.sun.javatest.services.XMLServiceReader},
 * which reads all required information from one XML file.
 */
public interface ServiceReader {

    /**
     * Invoked after ServiceReader object achieved from test suite and before
     * reading any information from reader.
     * @param ts test suite.
     * @param args any args, found in reader declaration in {@code testsuite.jtt} file,
     * except first arg, which is reader's implementation class. The key of this
     * record in {@code testsuite.jtt} file is "serviceReader"
     */
    public void init(TestSuite ts, String[] args);
    /**
     * Creates {@link com.sun.javatest.services.Service} instances and map from
     * service's IDs to services. During process of reading,
     * reader needs to instantiate all Service objects and fill them with required
     * data
     * @return map from service's IDs to services.
     */
    public Map<String, Service> readServices();
    /**
     * Creates and returns set of test paths, which describe mapping from tests
     * to services, required by this tests.
     * @return set of tests-to-services maps
     */
    public Set<TestPath> readTestServiceMap();

    /**
     * Returns file name with information on how to start services
     */
    public String getServiceDescriptorFileName();
}
