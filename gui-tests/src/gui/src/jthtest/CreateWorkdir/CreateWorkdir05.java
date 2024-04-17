/*
 * $Id$
 *
 * Copyright (c) 2009, 2024, Oracle and/or its affiliates. All rights reserved.
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

import jthtest.workdir.*;
import java.io.File;

import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JMenuOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

import jthtest.Test;

import static jthtest.Tools.*;

public class CreateWorkdir05 extends Test {

     /**
      * This test case verifies that a path for saving a workdirectory is seeded with
      * the default while creating a workdirectory.
      */
    JFrameOperator mainFrame;

    public void testImpl() throws Exception {
     startJavaTestWithDefaultTestSuite();

     mainFrame = findMainFrame();

     deleteDirectory(TEMP_PATH + TEMP_WD_NAME);
     Workdir.createWorkDirectory(TEMP_PATH + TEMP_WD_NAME, true, mainFrame);
     addUsedFile(TEMP_PATH + TEMP_WD_NAME);

     if (new File(TEMP_PATH + TEMP_WD_NAME + File.separator + "jtData" + File.separator + "template.data").exists()) {
         throw new JemmyException("Found template config in workdir while not expected");
     }

     if (new JTextFieldOperator(mainFrame, new NameComponentChooser("bcc.Configuration")).getText().equals("demotemplate.jtm")) {
         throw new JemmyException("Template is opened while not expected");
     }

     if (new JMenuOperator(mainFrame, getExecResource("ch.menu")).getMenuComponent(1).isEnabled()) {
         throw new JemmyException("Template editing found while not expected");
     }
    }
}
