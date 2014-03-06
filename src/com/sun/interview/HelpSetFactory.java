/*
 * $Id$
 *
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.interview;

import com.sun.interview.Interview.Fault;
import java.io.File;

/**
 * The interface that implements bridge between javax.help package and
 * JavaTest. Default implementation of this interface just provides stubs,
 * it doesn't use javax.help and could be used in the batch mode.
 * Interview while initialization checks if Desktop is available or not,
 * if yes, the rich factory will be used.
 *
 * @author Dmitry Fazunenko
 */
public interface HelpSetFactory {

    /**
     * Creates an instance of HelpSet.
     * Real implementation should create of javax.help.HelpSet, when a
     * stub implementation just object.
     *
     * @throws com.sun.interview.Interview.Fault if something went wrong
     */
    public Object createHelpSetObject(String name, Class c) throws Interview.Fault;

    /**
     * Creates an instance of HelpSet.
     * Real implementation should create of javax.help.HelpSet, when a
     * stub implementation just object.
     *
     * @throws com.sun.interview.Interview.Fault if something went wrong
     */
    public Object createHelpSetObject(String name, File file) throws Interview.Fault;

    /**
     * Updates a HelpSet instance associated with the given interview.
     *
     * @param interview - interview object to reset HelpSet
     * @param object - an instance of javax.help.HelpSet
     * @throws com.sun.interview.Interview.Fault if something went wrong
     */
    public Object updateHelpSetObject(Interview interview, Object object);

    public Object createHelpID(Object hs, String str);


    /**
     * The very default implementation of the HelpSetFactory interface.
     * It should be used in case when help is not required (command line mode)
     */
    public static final HelpSetFactory DEFAULT = new Default();

    static class Default implements HelpSetFactory {
        private static final Object EMPTY = new Object();

        public Object createHelpSetObject(String name, Class c) throws Fault {
            return EMPTY;
        }

        public Object createHelpSetObject(String name, File file) throws Fault {
            return EMPTY;
        }

        public Object createHelpID(Object hs, String str) {
            return null;
        }

        public Object updateHelpSetObject(Interview interview, Object object) {
            return object;
        }

    }

}
