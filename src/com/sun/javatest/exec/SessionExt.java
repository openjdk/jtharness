/*
 * $Id$
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.InterviewParameters;
import com.sun.javatest.WorkDirectory;
import java.io.File;

/**
 * Extention to the Session interface. It contains a number of methods
 * to prevent the behavior user gets used to.
 *
 * Hopefully, some day this interface will be eliminated...
 *
 * @author Dmitry Fazunenko
 */
public interface SessionExt extends Session {

    /**
     * Returns work directory assigned to the session.
     * @return The current wd set.
     */
    public WorkDirectory getWorkDirectory();

    /**
     * Returns InterviewParameters object, most likely the same object
     * as getParamaters()
     *
     * According to the original idea there should not be such method in
     * this interface, getParameters() should be enough. But JavaTest is not
     * ready yet to not use InterviewParameters.
     *
     * @see #getParameters()
     * @return an instance of InterviewParameters
     */
    public InterviewParameters getInterviewParameters();

    /**
     * Loads interview from a given file and associates it with a given
     * working directory.
     * @param wd
     * @param jti
     * @throws com.sun.javatest.exec.Session.Fault
     */
    public void loadInterviewFromFile(WorkDirectory wd, File jti) throws Session.Fault;

    /**
     * Reloads interview if out of date.
     */
    public void reloadInterview() throws Session.Fault;
}
