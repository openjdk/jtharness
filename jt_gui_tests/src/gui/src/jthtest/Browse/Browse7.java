/*
 * $Id$
 *
 * Copyright (c) 2009, 2011, Oracle and/or its affiliates. All rights reserved.
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
/*
 * Start JavaTest with -newdesktop. The JavaTest Quick Start wizard will be displayed. 
 * Click on Browse the testsuite and click on Next button. Specify the testsuite. 
 * Click on Next button. Choose the Use the configuration template , specify a valid 
 * and complete configuration file and click on Next. The text should specify that 
 * the configuration is complete. Check the Start the configuration check and click 
 * on Finish button and then choose workdir. Verify that the configuration editor 
 * will be displayed to make changes
 */
package jthtest.Browse;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author naryl
 */
public class Browse7 extends Browse {
	
	public static void main(String[] args) {
		JUnitCore.main("com.sun.javatest.tests.Browse.Browse7");
	}
	
	@Test
	public void testBrowse7() {
		browseTestsuite(quickStartDialog);
		
		next(quickStartDialog);
		
		pickDefaultTestsuite(quickStartDialog);
		
		next(quickStartDialog);
		
		useConfigTemplate(quickStartDialog);
		
		next(quickStartDialog);
		
		startConfigEditor(quickStartDialog);
		
		finish(quickStartDialog, true);
		
		pickWorkDir(mainFrame);
		
		new JDialogOperator(mainFrame, "Configuration Editor");
		
	}
	
}
