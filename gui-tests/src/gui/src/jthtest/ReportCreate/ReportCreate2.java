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
package jthtest.ReportCreate;

/**
 * This test case verifies that Create Report button under Report menu will create a report directory for a valid name.
 */

import java.io.File;
import static jthtest.ReportCreate.ReportCreate.*;
import jthtest.Test;
import jthtest.tools.JTFrame;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

public class ReportCreate2 extends Test {

    public void testImpl() throws Exception {
        deleteUserData();

        mainFrame = JTFrame.startJTWithRunWD();

        JDialogOperator rep = openReportCreation(mainFrame.getJFrameOperator());
        String path = TEMP_PATH + REPORT_NAME + "_new" + File.separator;
        deleteDirectory(path);
        setPath(rep, path);

        pressCreate(rep);
        addUsedFile(path);

        if (!new File(path).exists()) {
            throw new JemmyException("Directory was not created");
        }
    }

}

