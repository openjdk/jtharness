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

/**
 * Contains wrappers for all data types that are allowed to use within annotations.
 * Classes contained in this package serve for attaching data directly to parameters of the testcase.
 * Please see the following samples:
 *
 * <pre>
    <code>@TestCase</code>
    void testIndexOf(<code>@Strings</code>({"fee", "fie", "foe", "foo"}) String s,
                     <code>@Chars</code>  ({  'f',   'i',   'e',   'e'}) char c,
                     <code>@Ints</code>   ({    0,     1,     2,    -1}) int expectedIndexOf) {
        Assert.assertEquals(expectedIndexOf, s.indexOf(c) );
    }
 * </pre>
 *
 * The following testcase:
 *
 * <pre>
        <code>@TestCase</code>
        <code>@Operation</code>(Operation.TYPE.MULTIPLY)
        public void test(<code>@Ints</code>({ 1, 2, 3 })      int i,
                         <code>@Strings</code>({ "a", "b"  }) String s) {
            System.out.println(MessageFormat.format("i = {0}, s = {1}", i, s));
        }
 * </pre>
 * will print out:
 * <pre>
    i = 1, s = a
    i = 1, s = b
    i = 2, s = a
    i = 2, s = b
    i = 3, s = a
    i = 3, s = b
 * </pre>
 *
 */
package com.sun.tck.lib.tgf.data;