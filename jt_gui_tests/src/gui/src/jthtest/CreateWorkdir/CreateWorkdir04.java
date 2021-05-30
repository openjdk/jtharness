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
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import jthtest.Test;

import jthtest.Tools;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import static jthtest.Tools.*;
import static jthtest.workdir.Workdir.*;

/**
 *
 * @author linfar
 */
public class CreateWorkdir04 extends Test {
    /*
     * There should be a warning-message when trying to create inexisting path
     */

    public void testImpl() throws Exception {
	startJavaTestWithDefaultTestSuite();

	JFrameOperator mainFrame = findMainFrame();

	deleteDirectory(TEMP_PATH + "bad_dir");
	createWorkDirectory(TEMP_PATH + "bad_dir" + File.separator + TEMP_WD_NAME, false, mainFrame);
	File f = new File(TEMP_PATH + "bad_dir" + File.separator + TEMP_WD_NAME);
	addUsedFile(f);

	try {
	    new JTextComponentOperator(mainFrame, TEMP_WD_NAME);
	} catch(TimeoutExpiredException e) {
	    errors.add("Component with WD name was not found");
	}
	if (!f.exists()) {
	    errors.add("Work Directory was not created properly");
	}
	if (!mainFrame.getTitle().endsWith(f.getAbsolutePath())) {
	    errors.add("Title has not new WD path (" + f.getAbsolutePath() + "), title: " + mainFrame.getTitle());
	}
    }

    @Override
    public String getDescription() {
	return "WD directory should be created recursively";
    }
}
