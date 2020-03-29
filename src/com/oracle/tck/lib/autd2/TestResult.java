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

/**
 * The simplest state of test execution result.
 * It could be passed or failed and supplemented by text message
 * which could provide more details.
 */
public class TestResult {

    /**
     * Type of the result - passed or failed.
     */
    enum Type {
        OK(0, "Passed."),
        FAILURE(1, "Failed.");
        private int code;
        private String text;
        Type(int code, String text) {
            this.code = code;
            this.text = text;
        }
    }

    private final Type type;
    private final String message;

    public TestResult(Type type, String message) {
        this.type = type;
        this.message = message;
    }

    public static TestResult ok(String reason) {
        return new TestResult(Type.OK, reason);
    }

    public static TestResult failure(String reason) {
        return new TestResult(Type.FAILURE, reason);
    }

    public boolean isOK() {
        return type == Type.OK;
    }

    public int getTypeCode() {
        return type.code;
    }

    public String getMessage() {
        return message;
    }

    public String toString() {
        return type.text + " " + message;
    }
}
