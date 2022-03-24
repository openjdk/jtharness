/*
 * $Id$
 *
 * Copyright (c) 2001, 2022, Oracle and/or its affiliates. All rights reserved.
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

package jthtest.Sanity_Tests;

import java.lang.reflect.InvocationTargetException;

import jthtest.Test;
import jthtest.tools.ConfigDialog;
import jthtest.tools.JTFrame;

public class Test_Config_New1 extends Test {
   /**
     * This test is to verify that menu item \"Configure-> New configuration\" in an
     * existing directory and configuration will reset it to an empty state.
     *
     * @throws ClassNotFoundException
     *
     * @throws InvocationTargetException
     *
     * @throws NoSuchMethodException
     */
   public void testImpl() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {

        JTFrame frame = JTFrame.startJTWithDefaultWorkDirectory();
        ConfigDialog cd = frame.getConfiguration().openByKey();
        boolean firstly = cd.isFullConfiguration();
        cd.closeByMenu();

        cd = frame.getConfiguration().create(true);
        boolean secondly = cd.isFullConfiguration();

        if (secondly) {
            errors.add("Configuration is full after creation");
        }
        if (!firstly) {
            errors.add("Warning: configuration was not full before creation");
        }
    }
}
