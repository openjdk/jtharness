/*
 * $Id$
 *
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.finder;

import com.sun.javatest.TestDescription;
import com.sun.javatest.TestFinder;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

/**
 * A test finder that reads tests from a delegate, and returns the
 * results in the random order. This is primarily for debugging
 * and testing purposes.
 */
public class RandomTestFinder extends TestFinder {

    /**
     * Constructor to create RandomTestFinder object when the
     * original TestFinder instance is already created and initialized.
     * Finders created with this constructor do not require the init() method
     * to be invoked.
     *
     * @param delegate - the real test finder object.
     */
    public RandomTestFinder(TestFinder delegate) {
        this.delegate = delegate;
    }

    @Override
    public File getRoot() {
        return delegate.getRoot();
    }

    @Override
    public File getRootDir() {
        return delegate.getRootDir();
    }

    @Override
    public void read(File file) {
        delegate.read(file);
    }

    @Override
    public TestDescription[] getTests() {
        TestDescription[] tds = delegate.getTests();
        Collections.shuffle(Arrays.asList(tds));
        return tds;
    }

    @Override
    public File[] getFiles() {
        File[] fs = delegate.getFiles();
        Collections.shuffle(Arrays.asList(fs));
        return fs;
    }

    @Override
    protected void scan(File file) {
        throw new Error("should not be called!");
    }

    private TestFinder delegate;

}