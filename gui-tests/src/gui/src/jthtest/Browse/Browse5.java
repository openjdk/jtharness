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
package jthtest.Browse;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import junit.framework.Assert;

public class Browse5 extends Browse {

	public static void main(String[] args) {
		JUnitCore.main("jthtest.gui.Browse.Browse5");
	}

	@Test
	public void test_incomplete_configuration_template() {

		// click on Browse the Test Suite Radio button
		browseTestsuite(quickStartDialog);

		// click on next button
		next(quickStartDialog);

		// Select the test suite
		pickDefaultTestsuite(quickStartDialog);

		// Click on next button
		next(quickStartDialog);

		// Select the incomplete configuration template
		useIncompleteConfigTemplate(quickStartDialog);

		// Click on next button
		next(quickStartDialog);

		Assert.assertEquals("The text should specify that the configuration is incomplete",
				getJckResource("qsw.end.needEditor"), getTextArea(quickStartDialog));

		Assert.assertEquals("Almost done text should be clear and easy to understand", getJckResource("qsw.end.hd"),
				getTextField(quickStartDialog));
	}

	// TestCase Description
	public String getDescription() {
		return "This test case verifies that using an incomplete configuration template file an error will be generated.";
	}
}