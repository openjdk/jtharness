/*
 * $Id$
 *
 * Copyright (c) 1996, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.agent;

/**
 * This exception is used to report bad user-specifed data.
 */
public class BadValue extends Exception
{
    /**
     * Create a BadValue exception.
     * @param msg A string giving additional details about the
     *          bad value that was found.
     */
    public BadValue(String msg) {
        super(msg);
        msgs = new String[1];
        msgs[0] = msg;
    }

    /**
     * Create a BadValue exception.
     * @param msgs An array of strings giving additional details about the
     *          bad value that was found.
     */
    public BadValue(String[] msgs) {
        super(msgs[0]);
        this.msgs = msgs;
    }

    /**
     * Get the messages giving details about the problem.
     * @return an array of strings giving additional details about the
     *          bad value that was found
     */
    public String[] getMessages() {
        return msgs;
    }

    private String[] msgs;
}
