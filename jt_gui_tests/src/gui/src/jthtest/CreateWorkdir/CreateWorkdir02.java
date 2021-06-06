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
import org.netbeans.jemmy.operators.JFrameOperator;

import jthtest.Test;

import jthtest.Tools;
import jthtest.tools.JTFrame;
import static jthtest.Tools.*;
import static jthtest.workdir.Workdir.*;

/**
 *
 * @author linfar
 */
public class CreateWorkdir02 extends Test {

    public CreateWorkdir02() {
    depricated = true;
    }

    public void testImpl() throws Exception {
    deleteDirectory(DEFAULT_PATH + TO_DELETE_TEMP_WD_NAME);

    JTFrame mainFrame = JTFrame.startJTWithDefaultTestSuite();

    File created = mainFrame.getWorkDirectory().createWorkDirectory(TO_DELETE_TEMP_WD_NAME, true);
    addUsedFile(created);

    if (!created.exists()) {
        errors.add("Work directory wasn't created propertly with path " + DEFAULT_PATH + TO_DELETE_TEMP_WD_NAME);
    }

//    startJavaTestWithDefaultTestSuite();
//
//    JFrameOperator mainFrame = findMainFrame();
//
//    Workdir.createWorkDirectory(TO_DELETE_TEMP_WD_NAME, true, mainFrame);
//    addUsedFile(DEFAULT_PATH + TO_DELETE_TEMP_WD_NAME);
//
//    if (!verifyWorkdirCreation(DEFAULT_PATH + TO_DELETE_TEMP_WD_NAME)) {
//        errors.add("Work directory wasn't created propertly with path " + DEFAULT_PATH + TO_DELETE_TEMP_WD_NAME);
//    }
    }

    @Override
    public String getDescription() {
//    return "This test tryes to create work directory in the directory where Work Directory Creation dialog is initialised. It should be user.dir";
    return "This test is depricated - initial directory in 'Create Work Directory' dialog is not 'user.dir' now";
    }
}
