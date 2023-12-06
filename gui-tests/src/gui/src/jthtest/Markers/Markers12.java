/*
 * $Id$
 *
 * Copyright (c) 2009, 2023, Oracle and/or its affiliates. All rights reserved.
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
package jthtest.Markers;

import java.lang.reflect.InvocationTargetException;
import jthtest.Test;
import jthtest.Tools;
import jthtest.tools.ConfigDialog;
import jthtest.tools.Configuration;
import jthtest.tools.JTFrame;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

public class Markers12 extends Test {
	/**
	 * Start JavaTest with the -newDesktop option. Create a workdirectory. Load an
	 * existing JTI file. Bring up configuration editor by doing Ctrl-E. Select the
	 * Enable Bookmarks from the Bookmarks menu. Select the first question from the
	 * index pane. Mark the question by selecting Mark Current Question from the
	 * Bookmarks menu. Select Clear the Answer for the Current Question from the
	 * Bookmarks menu. The answer will be clear. Click on Next button. Verify that
	 * the an invalid answer will be displayed if the question has not been
	 * answered.
	 */
	public void testImpl() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
		mainFrame = new JTFrame(true);

		mainFrame.openDefaultTestSuite();
		addUsedFile(mainFrame.createWorkDirectoryInTemp());
		Configuration configuration = mainFrame.getConfiguration();
		configuration.load(CONFIG_NAME, true);

		ConfigDialog config = configuration.openByKey();

		config.getBookmarks_EnableBookmarks().push();
		config.setBookmarkedByMenu(4);
		config.clearByMenu(4);
		config.pushNextConfigEditor();

		if (!new JTextFieldOperator(config.getConfigDialog(), new NameComponentChooser("qu.vmsg")).getText()
				.equals("Invalid response")) {
			errors.add("Error message wasn't found: '"
					+ new JTextFieldOperator(config.getConfigDialog(), new NameComponentChooser("qu.vmsg")).getText()
					+ "' while expected 'Invalud response'");
		}

		warnings.add("Pre-defined warning: Some questions can be optional and some can have default value");

	}

	@Override
	public String getDescription() {
		return "Start JavaTest with the -NewDesktop option. Create a workdirectory. Load an existing JTI file. Bring up configuration editor by doing Ctrl-E. Select the Enable Bookmarks from the Bookmarks menu. Select the first question from the history list. Mark the question by selecting Mark Current Question from the Bookmarks menu. Select Clear the Answer for the Current Question from the Bookmarks menu. The answer will be clear. Click on Next button. Verify that the an invalid answer will be displayed if the question has not been answered.";
	}
}
