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
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import jthtest.Test;

import static jthtest.Tools.*;
import static jthtest.workdir.Workdir.*;

/**
 *
 * @author linfar
 */
public class CreateWorkdir15 extends Test {

    public CreateWorkdir15() {
    depricated = true;
    }

    public void testImpl() throws Exception {
    startJavatestNewDesktop();

    JFrameOperator mainFrame = findMainFrame();
    openTestSuite(mainFrame);

    JDialogOperator wdCreate = openOpenWorkDirectoryDialog(mainFrame);

    JComboBoxOperator cbOperator = new JComboBoxOperator(wdCreate);

    if (!((new JComboBoxOperator(wdCreate).getSelectedItem() + File.separator).equals(LOCAL_PATH))) {
        throw new JemmyException("Default work directory doesn't match directory from which JavaTest was opened");
    }
    }

    @Override
    public String getDescription() {
    return "Test is depricated - default work directory should not match directory from which JavaTest was opened";
    }
}
