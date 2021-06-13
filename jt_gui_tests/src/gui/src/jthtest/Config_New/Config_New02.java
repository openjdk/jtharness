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
package jthtest.Config_New;

import jthtest.Test;
import jthtest.tools.JTFrame;

/**
 *
 * @author linfar
 */
public class Config_New02 extends Test {

    public void testImpl() throws Exception {
    JTFrame mainFrame = new JTFrame(true);

    mainFrame.openDefaultTestSuite();
    addUsedFile(mainFrame.createWorkDirectoryInTemp());

    // This one works veeeery slow. I don't know why. Something with menus.
    if (mainFrame.getConfiguration().create(false).isFullConfiguration()) {
        errors.add("Newly created configuration is full while unexpected");
    }

//    startJavatestNewDesktop();
//
//    JFrameOperator mainFrame = findMainFrame();
//    closeQS(mainFrame);
//
//    openTestSuite(mainFrame);
//    createWorkDirInTemp(mainFrame);
//
//    openConfigCreationBlock(mainFrame);
//    JDialogOperator config = findConfigEditor(mainFrame);
//    if (isFullConfiguration(config)) {
//        throw new JemmyException("Configuration is full after creation");
//    }
    }

    @Override
    public String getDescription() {
    return "This test checks that a newly created configuration is not full.";
    }
}
