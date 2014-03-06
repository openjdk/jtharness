/*
 * $Id$
 *
 * Copyright (c) 2006, 2010, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.InterviewParameters;
import com.sun.javatest.InterviewPropagator;
import com.sun.javatest.WorkDirectory;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


class TU_ViewManager implements InterviewPropagator.ViewManager, InterviewPropagator.TestRefresher {
    // config --> TU_Dialog
    private static HashMap<InterviewParameters, TU_dialog> store =
            new HashMap<InterviewParameters, TU_dialog>();

    public void showView(InterviewPropagator prop, InterviewParameters intTerview) {
        TU_dialog tud = store.get(intTerview);
        if (tud != null)  {
            tud.show(prop);
        }
    }

    public void notifyError(String message, InterviewParameters ip) {
        TU_dialog tud = store.get(ip);
        if (tud != null)  {
            WorkDirectory wd = tud.sesssion.getParameters().getWorkDirectory();
            if (wd != null) {
                Logger logger = TU_dialog.makeNotificationLogger(wd);
                if (logger != null) {
                    logger.log(Level.SEVERE, message);
                }
            }
        }
    }

    public void logException(Throwable th, InterviewParameters ip) {
        TU_dialog tud = store.get(ip);
        if (tud != null)  {
            WorkDirectory wd = tud.sesssion.getParameters().getWorkDirectory();
            if (wd != null) {
                Logger logger = TU_dialog.makeLogger(wd);
                if (logger != null) {
                    logger.log(Level.SEVERE, th.getMessage(), th);
                }
            }
        }
    }

    /**
     * Registers InterviewParameters instance and TU_dialog instance
     * associated with it
     * @param ip
     * @param tud
     */
    static synchronized void register(InterviewParameters ip, TU_dialog tud) {
        store.put(ip, tud);
        TU_ViewManager tuv = new TU_ViewManager();
        InterviewPropagator.setViewManager(tuv);
        InterviewPropagator.setTestRefresher(tuv);
    }

    /**
     * Unregisters InterviewParameters.
     */
    static synchronized void unregister(InterviewParameters ip) {
        store.remove(ip);
    }

    public void refreshTestTree(InterviewParameters ip) {
        TU_dialog tud = store.get(ip);
        if (tud != null)  {
            tud.context.refreshTests();
        }

    }
}

