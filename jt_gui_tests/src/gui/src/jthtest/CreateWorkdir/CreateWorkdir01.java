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
import jthtest.Test;

import jthtest.Tools;
import jthtest.tools.JTFrame;
import org.netbeans.jemmy.operators.JFrameOperator;

import static jthtest.Tools.*;

/**
 *
 * @author linfar
 */
public class CreateWorkdir01 extends Test {

    public void testImpl() throws Exception {
	JTFrame mainFrame = JTFrame.startJTWithDefaultTestSuite();

	File created = mainFrame.createWorkDirectoryInTemp();
	addUsedFile(created);

	if (!created.exists()) {
	    errors.add("Work directory wasn't created propertly with path " + created.getPath() + " " + created.exists());
	}

//	startJavaTestWithDefaultTestSuite();
//
//	JFrameOperator mainFrame = findMainFrame();
//	String createdPath = Workdir.createWorkDirectoryInTemp(mainFrame);
//	addUsedFile(createdPath);
//
//	if (!Workdir.verifyWorkdirCreation(createdPath)) {
//	    errors.add("Work directory wasn't created propertly with path " + createdPath + " " + new File(createdPath).exists());
//	    Tools.pause(300000);
//	}
    }

    @Override
    public String getDescription() {
	return "This test tryes to create a work directory in temp directory using default test suite";
    }
}
