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

package jthtest.Config_Edit;

import jthtest.ConfigTools;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 *
 * @author linfar
 */
public class Config_Edit extends ConfigTools {
    public static void waitForConfigurationLoading(JFrameOperator mainFrame, String name) {
    JTextFieldOperator label = new JTextFieldOperator(mainFrame, new NameComponentChooser("bcc.Configuration"));
    int time = 0;
    while(!label.getText().equals(name)) {
        try {
        Thread.sleep(100);
        time += 100;
        if(time > MAX_WAIT_TIME)
            throw new JemmyException("Configuration loading error");
        } catch (InterruptedException ex) {
        Logger.getLogger(Config_Edit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    }
}
