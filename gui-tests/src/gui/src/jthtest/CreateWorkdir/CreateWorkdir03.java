/*
 * $Id$
 *
 * Copyright (c) 2001, 2021, Oracle and/or its affiliates. All rights reserved.
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

import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import jthtest.Test;

import static jthtest.Tools.*;
import static jthtest.workdir.Workdir.*;

public class CreateWorkdir03 extends Test {

	/**
	 * This test case verifies that creating an existing workdirectory will generate
	 * an error message.
	 */

	public void testImpl() throws Exception {
		startJavaTestWithDefaultTestSuite();
		JFrameOperator mainFrame = findMainFrame();

		String path = DEFAULT_PATH + TO_DELETE_TEMP_WD_NAME;
		deleteDirectory(path);
		createWorkDirectory(path, true, mainFrame);
		createWorkDirectory(path, false, mainFrame);
		addUsedFile(path);

		try {
			new JDialogOperator(getToolResource("wdc.exists_openIt.title"));
		} catch (JemmyException e) {
			errors.add(
					"Error message offering user to open existing work directory insted of creation it was not found");
		}
	}
}
