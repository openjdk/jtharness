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

import java.util.List;

/**
 * Interface of factory to be used by ExecTool to create controls over
 * test configuring and running.
 *
 * @author Dmitry Fazunenko
 */
public interface ET_ControlFactory {

    /**
     * Creates a control over session.
     * @throws com.sun.javatest.exec.Session.Fault
     */
    public ET_SessionControl createSessionControl() throws Session.Fault;

    /**
     * Creates a control over filters. Due to internal reasons the returned
     * object must be instance of ET_FilterHandler. Sorry for the inconveniences
     * caused.
     */
    public ET_FilterControl createFilterControl();

    /**
     * Creates a control over help.
     */
    public ET_HelpControl createHelpControl();

    /**
     * Creates a view controls.
     */
    public ET_ViewControl createViewControl();

    /**
     * Creates a report controls.
     */
    public ET_ReportControl createReportControl();


    /**
     * Creates a list of custom controls.
     */
    public List<ET_Control> createCustomControls();

}
