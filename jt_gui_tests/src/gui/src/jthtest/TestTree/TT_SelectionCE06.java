/*
 * $Id$
 *
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
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
package jthtest.TestTree;

import jthtest.tools.ConfigDialog;
import jthtest.tools.Configuration;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 *
 * @author andrey
 */
public class TT_SelectionCE06 extends TT_SelectionCE {

    public TT_SelectionCE06() {
        super("loading and editing (with unimportant changes) ConfigEditor", new int[]{1, 2, 3, 5, 8, 10});
    }

    public void make() {
        Configuration c = mainFrame.getConfiguration();
        c.load(CONFIG_NAME, true);
        ConfigDialog cd = c.openByMenu(true);

        cd.selectQuestion(2);
        JTextFieldOperator tf = new JTextFieldOperator(cd.getConfigDialog(), new NameComponentChooser("str.txt"));
        tf.clearText();
        tf.typeText("some_new_text");

        cd.pushLastConfigEditor();
        cd.pushDoneConfigEditor();
    }

    public String getDescription() {
        return "Check that selection on TestTree on main page doesn't dissapear when editing unimportant value (Description) in Configuration Editor and closing it with save. Doesn't include root path.";
    }
}
