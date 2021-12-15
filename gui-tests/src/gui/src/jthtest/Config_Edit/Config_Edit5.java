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
package jthtest.Config_Edit;

import org.netbeans.jemmy.operators.JListOperator;

import jthtest.Test;
import jthtest.Tools;
import jthtest.tools.ConfigDialog;
import jthtest.tools.Configuration;
import jthtest.tools.JTFrame;

public class Config_Edit5 extends Test {

	@org.junit.Test
	public void testImpl() throws Exception {
		mainFrame = new JTFrame(true);

		// Open default test suite
		mainFrame.openDefaultTestSuite();
		addUsedFile(mainFrame.createWorkDirectoryInTemp());

		// load configuration
		Configuration configuration = mainFrame.getConfiguration();
		configuration.load(Tools.CONFIG_NAME, true);

		// push last button on the configuration editor
		ConfigDialog dialog = configuration.openByKey();
		dialog.pushLastConfigEditor();
		JListOperator list = new JListOperator(dialog.getConfigDialog());

		// verifies that Last button in the configuration editor will go to the last
		// question.
		if (list.getSelectedIndex() != list.getModel().getSize() - 1) {
			errors.add("Selected element (" + list.getSelectedIndex() + ") is not the last ("
					+ (list.getModel().getSize() - 1) + ") after last button pushing");
		}
	}

	// TestCase Description
	public String getDescription() {
		return "This test case verifies that Last button in the configuration editor will go to the last question.";
	}
}
