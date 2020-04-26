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

public class ReducingIterator extends CachedIterator {

    public static final String MESSAGE_INVALID_RANGE = "Ratio must be between 0.0 and 1.0 inclusively";

    public ReducingIterator(Values values, double ratio) {
        super(values, false);
        if (ratio <= 0.0 || ratio >= 1.0) {
            throw new IllegalArgumentException(MESSAGE_INVALID_RANGE);
        }
        reduce(ratio);
    }

    private void reduce(double ratio) {
        double N = (double)data.size();
        double step =  N / (N - N * ratio) ;
        double current = ratio <= (1.0 - 1.0/N) ? 0.0 : step;
        while ((int) Math.round(current) < data.size()) {
            final int toBeRemoved = (int) Math.round(current);
            data.remove(toBeRemoved);
            current += step - 1.0;
        }
    }
}
