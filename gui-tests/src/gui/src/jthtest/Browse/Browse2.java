/*
 * $Id$
 *
 * Copyright (c) 2001, 2021, Oracle and/or its affiliates. All rights reserved.
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


/*
 * Start JavaTest with -newdesktop. The JavaTest Quick Start wizard will be displayed.
 * Click on Browse the testsuite and click on Next button. Specify an exiting test
 * suite and click on Next. Choose the Create a new configuration and click on Next.
 * Check the Start the configuration editor. Click on Finish button. Click on Create
 * Work directory. Specify a new directory and click on Create. Verify that the
 * configuration editor is displayed
 */
package jthtest.Browse;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.netbeans.jemmy.operators.JDialogOperator;

public class Browse2 extends Browse {

    public static void main(String[] args) {
        JUnitCore.main("jthtest.gui.Browse.Browse2");
    }

    @Test
    public void testBrowse2() {
        browseTestsuite(quickStartDialog);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }

        next(quickStartDialog);

        pickDefaultTestsuite(quickStartDialog);

        next(quickStartDialog);

        createConfiguration(quickStartDialog);

        next(quickStartDialog);

        finish(quickStartDialog, true);

        pickWorkDir(mainFrame);

        new JDialogOperator(mainFrame, "Configuration Editor");

    }


}
