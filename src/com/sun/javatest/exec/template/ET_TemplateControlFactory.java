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

package com.sun.javatest.exec.template;

import com.sun.javatest.TestSuite;
import com.sun.javatest.exec.Session.Fault;
import com.sun.javatest.exec.ContextManager;
import com.sun.javatest.exec.ET_SessionControl;
import com.sun.javatest.exec.ET_DefaultControlFactory;
import com.sun.javatest.exec.ET_FilterControl;
import com.sun.javatest.exec.ExecModel;
import com.sun.javatest.tool.Tool;
import com.sun.javatest.tool.UIFactory;
import javax.swing.JComponent;

/**
 * Factory that creates controls which are aware of template.
 *
 * @author Dmitry Fazunenko
 */
public class ET_TemplateControlFactory extends ET_DefaultControlFactory {

    /**
     * UIFactory of the original package (most likely com.sun.javatest.exec).
     * uif field will contain reference to new UIFactory where some
     * resources are overriden for the template package.
     */
    protected UIFactory uifOrig;

    private ET_SessionControl sessionControl;
    private ET_FilterControl filterControl;

    public ET_TemplateControlFactory(JComponent parent, UIFactory uif, TestSuite ts,
            ContextManager cm, ExecModel execModel, Tool tool) {
        super(parent, new UIFactory.UIFactoryExt(uif, ET_TemplateControlFactory.class),
                ts, cm, execModel, tool);
        uifOrig = uif;
    }

    /**
     * Overrides to return TemplateSessionControl instance
     * @return TemplateSessionControl instance
     * @throws com.sun.javatest.exec.Session.Fault
     */
    @Override
    public ET_SessionControl createSessionControl() throws Fault {
        if (sessionControl == null) {
            sessionControl = new TemplateSessionControl(parent, uif, ts, cm, uifOrig);
        }
        return sessionControl;
    }

    /**
     * Overrides to return TemplateFilterHandler instance
     * @return TemplateFilterHandler instance
     */
    @Override
    public ET_FilterControl createFilterControl() {
        if (filterControl == null) {
            filterControl = new TemplateFilterHandler(parent, execModel, uif);
        }
        return filterControl;
    }

}
