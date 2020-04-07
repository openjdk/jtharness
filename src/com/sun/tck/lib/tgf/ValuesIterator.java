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

import java.util.Iterator;

/**
 * This class standardises way of getting set of arguments for parametrised testcase.
 * @see LeafIterator
 * @see MultiplyIterator
 * @see UniteIterator
 */
public abstract class ValuesIterator implements Iterator<Object[]> {

    /**
     * Identifies that this iterator is a root one.
     * Field is set by TGF runner when data iteration starts.
     * Field is used by many implementations. Please see sources.
     */
    protected boolean root;

    /**
     * This operation is unsupported.
     * @throws UnsupportedOperationException always will be thrown
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * This method might be called by the Test Generation Framework engine
     * (usually by other iterators participating in data production)
     * to ask for a shift in data iteration.
     * In other words - data pointer must be set to the next element of the data flow that
     * iterator produces.
     */
    public abstract void shift();

    /**
     * This method is called by Test Generation Framework engine to ask for a reset in data iteration.
     * In other words - data pointer must be set to the beginning of the data
     * flow that current iterator produces.
     */
    protected abstract void rollback();

    /**
     * Creates a copy of this iterator
     */
    protected abstract ValuesIterator createCopy();

}
