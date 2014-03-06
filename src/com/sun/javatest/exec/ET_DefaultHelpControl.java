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

import com.sun.javatest.tool.Tool;
import com.sun.javatest.tool.ToolAction;
import com.sun.javatest.tool.UIFactory;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.help.HelpBroker;
import javax.swing.Action;
import javax.swing.JMenu;

/**
 *
 * @author Dmitry Fazunenko
 */
public class ET_DefaultHelpControl implements ET_HelpControl {

    Tool tool;
    UIFactory uif;

    ET_DefaultHelpControl(Tool tool, UIFactory uif) {

        this.tool = tool;
        this.uif = uif;
        initActions();
    }

    /**
     * Does nothing
     */
    public void updateGUI() {
    }

    /**
     * Does nothing
     */
    public void save(Map m) {
    }

    /**
     * Does nothing
     */
    public void restore(Map m) {
    }

    /**
     * @return null
     */
    public JMenu getMenu() {
        return null;
    }

    public List<Action> getToolBarActionList() {
        if (actionList == null) {
            actionList = new ArrayList<Action>();
            actionList.add(helpAction);
        }
        return actionList;
    }

    /**
     * Does nothing
     */
    public void dispose() {
    }

    private List<Action>  actionList = null;
    private Action helpAction = null;

    void initActions() {
        helpAction = new ToolAction(uif, "exec.help", true) {
            public void actionPerformed(ActionEvent e) {
                HelpBroker b = tool.getHelpBroker();
                if (b != null) {
                    b.setCurrentID("browse.window.csh");
                    b.setDisplayed(true);
                } else {
                    // could internationalize this, but the error isn't that helpful because a
                    // end-user probably can't fix the problem
                    System.err.println("Unable to display Test Manager help, the help system isn't available.");
                }
            }
        };
    }


}
