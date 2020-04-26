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

/**
 * Manages the entire lifecycle of a particular data value.
 * Implementation must provide data value instance by implementing
 * {@code create} method. Method {@code cleanUp} will be invoked after test run;
 * test developer should use field {@code value} if data value participates in cleanup process.
 * Provided {@code id} will be used as data value identifier
 *
 * <p>Please see the following code sample:
 <pre>
 static class MyData {
     public MyData() { System.out.println("MyData instance created."); }
     void cleanMe() { System.out.println("Method 'cleanMe' called."); }
 }

 public Values myValues() {
     return DataFactory.createValues( new AbstractValue<MyData>() {
         public MyData create() { return new MyData(); }
         public void cleanUp() { value.cleanMe(); }
     });
 }

 <code>@TestCase</code>
 <code>@TestData("myValues")</code>
 public void test(MyData data) {
     System.out.println("MyData object passed in.");
 }
 * </pre>
 * Will produce the following output:
 <pre>
 MyData instance created.
 MyData object passed in.
 Method 'cleanMe' called.
 * </pre>
 */
public abstract class AbstractValue<T> {

    protected T value;

    /**
     * Method is invoked by TGF
     * @return data value received from <code>create()</code> method
     */
    public final T doCreate() {
        return value = create();
    }

    /**
     * This method must be implemented to give test generation runner particular data value.
     * Method will be invoked just before particular testcase execution,
     * i.e. test data will be created by Test Generation Framework in a lazy way.
     * @return data value
     */
    public abstract T create();

    /**
     * This method should be overriden if test developer needs to
     * perform some cleaning actions with or without value that was
     * returned from <code>create()</code> method. Data object value
     * will be stored in <code>value</code> field just after <code>create</code>
     * method execution. This field should be used if needed.
     */
    public void cleanUp() {
    }

}
