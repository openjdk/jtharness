/*
 * $Id$
 *
 * Copyright (c) 2009, 2010, Oracle and/or its affiliates. All rights reserved.
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
package jthtest.CreateWorkdir;

import java.io.File;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JFrameOperator;

import jthtest.Test;

import jthtest.Tools;
import jthtest.tools.JTFrame;

/**
 *
 * @author linfar
 */
public class CreateWorkdir21 extends Test {
    /*
     * Any WD should be opened normaly with any name - even spaces
     */

    public void testImpl() throws Exception {
    JTFrame mainFrame = JTFrame.startJTWithDefaultTestSuite();

    File wd = mainFrame.getWorkDirectory().createWorkDirectory(Tools.TEMP_PATH, "path with spaces", true);
    addUsedFile(wd);

    if (!wd.exists()) {
        errors.add(("Workdir with spaces ('path with spaces') was not created in temp directory. Tried to create " + wd.getAbsolutePath()));
    }

//    startJavaTestWithDefaultTestSuite();
//    JFrameOperator mainFrame = findMainFrame();
//
//    String name = TEMP_PATH + "path with spaces";
//    createWorkDirectory(name, true, mainFrame);
//    addUsedFile(name);
//
//    if (!verifyWorkdirCreation(name)) {
//        throw new JemmyException("Workdir with spaces '" + name + "' was not created");
//    }
    }

    @Override
    public String getDescription() {
    return "Any WD should be opened normaly with any name - even with spaces";
    }
}
