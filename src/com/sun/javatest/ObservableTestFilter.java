/*
 * $Id$
 *
 * Copyright (c) 2001, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest;

import com.sun.javatest.util.DynamicArray;

/**
 * An extension to the basic test filter which provides observer capabilities.
 * The observer can be used to receive notification when the filter changes
 * state.  A typical response to that change message may be to run a set of
 * tests back through the filter.
 */
public abstract class ObservableTestFilter extends TestFilter {
    /**
     * An observer that is notified when the filter has been changed.
     */
    public interface Observer {
        /**
         * A notification message that is called when the filter has
         * been changed.
         * @param filter The filter that has been changed
         */
        public void filterUpdated(ObservableTestFilter filter);
    }

    /**
     * Add an observer to be notified when this filter has been changed.
     * @param o The observer to be registered.  Should never be null.
     */
    public void addObserver(Observer o) {
        observers = (Observer[])DynamicArray.append(observers, o);
    }

    /**
     * Remove a previously registered observer so that it will no longer
     * be notified of changes to this filter.
     * @param o The filter to be un-registered.
     */
    public void removeObserver(Observer o) {
        observers = (Observer[])DynamicArray.remove(observers, o);
    }

    /**
     * Notify observers that this filter has changed it's internal state
     * (behavior).
     * @param filter the filter that has changed
     */
    protected void notifyUpdated(ObservableTestFilter filter) {
        for (int i = 0; i < observers.length; i++)
            observers[i].filterUpdated(this);
    }

    /**
     * The set of observers for this filter.
     */
    protected Observer[] observers = new Observer[0];
}

