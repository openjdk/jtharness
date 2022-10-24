/*
 * $Id$
 *
 * Copyright (c) 2001, 2022, Oracle and/or its affiliates. All rights reserved.
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

/**
 * This test case verifies that selecting clear the response to the current question button will clear the answer to that question.
 */

import java.lang.reflect.InvocationTargetException;
import jthtest.Test;
import jthtest.tools.ConfigDialog;
import jthtest.tools.Configuration;
import jthtest.tools.JTFrame;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

public class Markers10 extends Test {

    public void testImpl() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        mainFrame = new JTFrame(true);

        mainFrame.openDefaultTestSuite();
        addUsedFile(mainFrame.createWorkDirectoryInTemp());
        Configuration configuration = mainFrame.getConfiguration();
        configuration.load(CONFIG_NAME, true);
        ConfigDialog cd = configuration.openByKey();

        cd.getBookmarks_EnableBookmarks().push();
        cd.selectQuestion(2);
        JTextFieldOperator op = new JTextFieldOperator(cd.getConfigDialog(), new NameComponentChooser("str.txt"));
        op.typeText("some description that must be cleared");
        cd.setBookmarkedByMenu(2);
        cd.clearByMenu(2);

        op = new JTextFieldOperator(cd.getConfigDialog(), new NameComponentChooser("str.txt"));
        if (!op.getText().equals("")) {
            errors.add("Text wasn't cleared up: '" + op.getText() + "' while expected ''");
        }
        warnings.add(
                "Pre-defined warning: Mark sometimes desapperas while clearing by menu - bookmark saves current state of answer; First question is 'Configuratoin name' and it can't be cleared; a new question is generated while clearing up");
    }
}
