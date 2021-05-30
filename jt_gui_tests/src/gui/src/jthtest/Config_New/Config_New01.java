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
import jthtest.tools.ConfigDialog;
import jthtest.tools.JTFrame;

/**
 *
 * @author linfar
 */
public class Config_New01 extends Test {

    public void testImpl() throws Exception {
	JTFrame frame = JTFrame.startJTWithDefaultWorkDirectory();

	ConfigDialog cd = frame.getConfiguration().openByKey();
	boolean firstly = cd.isFullConfiguration();

	cd.closeByMenu();

	cd = frame.getConfiguration().create(true);

	boolean secondly = cd.isFullConfiguration();

	if (secondly) {
	    errors.add("Configuration is full after creation");
	}
	if (!firstly) {
	    errors.add("Warning: configuration was not full before creation");
	}

//	startJavaTestWithDefaultWorkDirectory();

//	JFrameOperator mainFrame = findMainFrame();
//	openConfigDialogByKey(mainFrame);
//	JDialogOperator config = findConfigEditor(mainFrame);
//
//	boolean firstly = isFullConfiguration(config);
//
//	openConfigCreationBlock(mainFrame);
//	config = findConfigEditor(mainFrame);
//
//	boolean secondly = isFullConfiguration(config);
//	if(secondly)
//	    throw new JemmyException("Configuration is full after creation");
//
//	if(!firstly)
//	    throw new JemmyException("Warning: configuration was not full before creation");
    }

    @Override
    public String getDescription() {
	return "This tests checks that configuration is loaded properly whith -open " +
		"<WD_with_config> option and is nullified when creating new config. " +
		"Is depricated because configuration can't be changed when Configuration " +
		"Editor is shown (initially this feature was tested).";
    }
}
