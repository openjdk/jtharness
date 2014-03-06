/*
 * $Id$
 *
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.javatest.exec;

import com.sun.javatest.tool.UIFactory;
import javax.swing.JComponent;

/**
 * The factory for ExecTool controls.
 * Ideally, all these method should be declared in ET_ControlFactory
 * interface. But there are some package visible classes which are not
 * ready to be made public. Might be once they will be ready...
 *
 * @author Dmitry Fazunenko
 */
class ET_PrivateControlFactory {
    protected final JComponent parent;
    protected final UIFactory uif;
    protected final ExecModel execModel;

    public ET_PrivateControlFactory(JComponent parent, UIFactory uif,
            ExecModel execModel) {

        this.parent = parent;
        this.uif = uif;
        this.execModel = execModel;
    }

    /**
     * Creates a control over test running.
     */
    ET_RunTestControl createRunTestControl() {
        return new RunTestsHandler(parent, execModel, uif);
    }

    /**
     * Creates a control over test.
     */
    ET_TestTreeControl createTestTreeControl() {
        return new TestTreePanel(parent, execModel, uif);

    }

}
