/*
 * $Id$
 *
 * Copyright (c) 2001, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.help.CSH;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.Timer;

//import com.sun.javatest.Status;
//import com.sun.javatest.tool.I18NUtils;
import com.sun.javatest.tool.UIFactory;

/**
 * Progress bars for test run.
 */

class RunProgressMonitor extends Monitor implements MonitorState.Observer {
    RunProgressMonitor(MonitorState ms, UIFactory uif) {
        super(ms, uif);
        ms.addObserver(this);
    }

    public String getSmallMonitorName() {
        return uif.getI18NString("runprog.smName");
    }

    public Icon getSmallMonitorIcon() {
        return null;
    }

    public JComponent getSmallMonitor() {
        /* removed for JT 3.2
        if (smMeter == null) {
            Color[] colors = new Color[Status.NUM_STATES];
            colors[Status.PASSED] =  I18NUtils.getStatusBarColor(Status.PASSED);
            colors[Status.FAILED] =  I18NUtils.getStatusBarColor(Status.FAILED);
            colors[Status.ERROR] =   I18NUtils.getStatusBarColor(Status.ERROR);
            colors[Status.NOT_RUN] = I18NUtils.getStatusBarColor(Status.NOT_RUN);
            smMeter = new ProgressMeter(colors, state);
            smMeter.setToolTipText(uif.getI18NString("runprog.smName.tip"));
        }

        if (state.isRunning())
            smMeter.start();
        else
            smMeter.update();
        */
        smMeter = uif.createProgressBar("runprog.sm", JProgressBar.HORIZONTAL);
        smMeter.setMinimum(0);
        smMeter.setStringPainted(true);
        //smMeter.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        smMeter.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createEmptyBorder(3,5,3,5),
                                BorderFactory.createLineBorder(Color.black)));
        CSH.setHelpIDString(smMeter, "run.testProgress");

        return smMeter;
    }

    public String getLargeMonitorName() {
        return uif.getI18NString("runprog.lgName");
    }

    public Icon getLargeMonitorIcon() {
        return null;
    }

    public JComponent getLargeMonitor() {
        return null;
    }

    // MonitorState.Observer
    public void starting() {
        if (pmUpdate == null) {
            pmUpdate = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (lastDone != state.getTestsDoneCount()) {
                        smMeter.setMaximum(state.getTestsFoundCount());
                        lastDone = state.getTestsDoneCount();
                        smMeter.setValue(lastDone);
                    }
                }

                private int lastDone = 0;
            };
        }

        if (pmTimer == null) {
            pmTimer = new Timer(TIMER_FREQ, pmUpdate);
            pmTimer.setInitialDelay(TIMER_INITIAL);
            pmTimer.setCoalesce(false);     // ok to lose extra events
            pmTimer.setRepeats(true);
        }

        if (pmTimer.isRunning())
            pmTimer.restart();      // this should not happen
        else
            pmTimer.start();
    }

    public void postProcessing() {
        stopAll();
    }

    public void stopping() {
    }

    public void finished(boolean allOk) {
    }

    /**
     * Stop all active subthreads associated with a running harness.
     */
    private void stopAll() {
        if (pmTimer != null && pmTimer.isRunning())
            pmTimer.stop();

        // if all tests were completed, make sure that the bar shows
        // 100%.  this is necessary because depending on the timer,
        // it may not have been updated since the last test finished;
        // actually this is likely.
        if (state.getTestsDoneCount() == state.getTestsFoundCount())
            smMeter.setValue(smMeter.getMaximum());
    }

    private JProgressBar smMeter;
    private Timer pmTimer;
    private ActionListener pmUpdate;
    private static final int TIMER_FREQ= 3;
    private static final int TIMER_INITIAL = 1;
}
