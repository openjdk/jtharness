/*
 * $Id$
 *
 * Copyright (c) 2010, 2012 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.TestSuite;
import com.sun.javatest.tool.Tool;
import com.sun.javatest.tool.UIFactory;
import java.util.List;
import javax.swing.JComponent;

/**
 * Default implementation of ET_ControlFactory interface.
 * Subclasses might reuse it overriding necessary methods to provide
 * their own controls.
 * <br>
 * All createXXX() methods construct new object only when invoked the first
 * time. All consequent calls will return the instances previously created.
 *
 * @author Dmitry Fazunenko
 */
public class ET_DefaultControlFactory implements ET_ControlFactory {

    protected final JComponent parent;
    protected final UIFactory uif;
    protected final TestSuite ts;
    protected final ContextManager cm;
    protected final ExecModel execModel;
    protected final Tool tool;

    private ET_SessionControl sessionControl = null;
    private ET_FilterControl filterControl = null;
    private ET_HelpControl helpControl = null;
    private ET_ViewControl viewControl = null;
    private ET_ReportControl reportControl = null;

    /**
     * Initializes fields, does not create any instances.
     */
    public ET_DefaultControlFactory(JComponent parent, UIFactory uif, TestSuite ts,
            ContextManager cm, ExecModel execModel, Tool tool) {

        this.parent = parent;
        this.uif = uif;
        this.ts = ts;
        this.cm = cm;
        this.execModel = execModel;
        this.tool = tool;
    }

    /**
     * @return BasicSessionControl instance, unless overriden
     * @throws com.sun.javatest.exec.Session.Fault if failed to create session
     */
    public ET_SessionControl createSessionControl() throws Session.Fault {
        if (sessionControl == null) {
            sessionControl = new BasicSessionControl(parent, uif, ts, cm);
        }
        return sessionControl;
    }

    /**
     * @return ET_FilterHandler instance, unless overriden
     */
    public ET_FilterControl createFilterControl() {
        if (filterControl == null) {
            filterControl = new ET_FilterHandler(parent, execModel, uif);
        }
        return filterControl;
    }

    /**
     * @return ET_DefaultHelpControl instance, unless overriden
     */
    public ET_HelpControl createHelpControl() {
        if (helpControl == null) {
            helpControl = new ET_DefaultHelpControl(tool, uif);
        }
        return helpControl;
    }

    /**
     * @return ET_DefaultViewControl instance, unless overriden
     */
    public ET_ViewControl createViewControl() {
        if (viewControl == null) {
            viewControl = new ET_DefaultViewControl(parent, ts, execModel, uif,
                createFilterControl());
        }
        return viewControl;
    }

    /**
     * @return ReportHandler instance, unless overriden
     */
    public ET_ReportControl createReportControl() {
        if (reportControl == null) {
            reportControl = new ReportHandler(parent, execModel, uif);
        }
        return reportControl;
    }

    /**
     * No custom controls by default.
     * @return null
     */
    public List<ET_Control> createCustomControls() {
        return null;
    }
}
