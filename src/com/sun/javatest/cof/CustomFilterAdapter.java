/*
 * $Id$
 *
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.javatest.cof;

public class CustomFilterAdapter implements CustomFilter {

    public String transformTestCaseName(String tcn) {
        // Try to
        if (isJavaIdentifier(tcn)) {
            return tcn;
        }

        // The following algorithm is not very effective
        // but simple. It executes very rarely,
        // i.e. about 5 times of 3600 testcases
        for (int i = 1; i < tcn.length(); i++) {
            String p = tcn.substring(i);
            if (isJavaIdentifier(p)) {
                return p;
            }
        }
        return null;
    }

    // See JLS 3 / 3.8
    private boolean isJavaIdentifier(String jid) {
        for (int i = 0; i < jid.length(); i++) {
            char c = jid.charAt(i);
            if ( i == 0 && !Character.isJavaIdentifierStart(c)) {
                return false;
            }
            if ( i > 0 && !Character.isJavaIdentifierPart(c) && c != '.') {
                return false;
            }
        }
        return true;
    }
}
