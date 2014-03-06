/*
 * $Id$
 *
 * Copyright (c) 2001, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.EventQueue;

import javax.accessibility.AccessibleContext;
import javax.help.CSH;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTextField;

import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.util.I18NResourceBundle;

class ElapsedTimeMonitor extends Monitor implements MonitorState.Observer {
    ElapsedTimeMonitor(MonitorState ms, UIFactory uif) {
        super(ms, uif);
        ms.addObserver(this);
    }

    public String getSmallMonitorName() {
        return uif.getI18NString("et.sm.Name");
    }

    public Icon getSmallMonitorIcon() {
        return null;
    }

    public JComponent getSmallMonitor() {
        if (smTimer == null)
            smTimer = new SmallTimer(uif, state);

        if (isRunning)
            smTimer.start();
        else
            smTimer.update();

        return smTimer;
    }

    public String getLargeMonitorName() {
        return uif.getI18NString("et.lg.Name");
    }

    public Icon getLargeMonitorIcon() {
        return null;
    }

    public JComponent getLargeMonitor() {
        return null;
    }

    // MonitorState.Observer
    public void starting() {
        isRunning = true;

        if (smTimer != null)
            smTimer.start();
    }

    public void postProcessing() {
        if (smTimer != null)
            smTimer.stop();
    }

    public void stopping() {
    }

    public void finished(boolean allOk) {
        isRunning = false;
        stopAll();
    }

    /**
     * Stop all active subthreads associated with a running harness.
     */
    private void stopAll() {
        if (smTimer != null)
            smTimer.stop();
    }

    /**
     * Current instance of the small timer.
     */
    private SmallTimer smTimer;
    private ThreadGroup activeThreads = new ThreadGroup("elapsed time monitors");
    private boolean isRunning;
    private static I18NResourceBundle i18n;

    /**
     * Message strip sized timer.  Should be reusable using start and stop.
     */
    static class SmallTimer extends JTextField {
        SmallTimer(UIFactory uif, MonitorState ms) {
            super("et.sm");
            this.state = ms;
            this.uif = uif;

            setHorizontalAlignment(JTextField.LEFT);
            setOpaque(false);
            setBackground(UIFactory.Colors.TRANSPARENT.getValue());
            setEnabled(true);
            setVisible(true);
            setEditable(false);
            setBorder(BorderFactory.createEmptyBorder());
            /*
            prefix = uif.getI18NString("et.sm.prefix");
            setText(prefix + "00:00:00");
            */
            setText("00:00:00");
            uif.setToolTip(this, "et.sm");
            AccessibleContext ac = getAccessibleContext();
            ac.setAccessibleName(uif.getI18NString("et.sm.name"));
            CSH.setHelpIDString(this, "run.testProgress");

            update();
        }

        public void start() {
            myThread = new Thread() {
                public void run() {
                    while(myThread == currentThread()) {
                        try {
                            synchronized (myThread) {
                                myThread.wait(1000);
                                update();
                            }
                        }
                        catch(InterruptedException e) {
                        }
                    }   // while

                    // one last update of the time
                    update();
                }
            };  // Thread()

            // user will notice this, so set a higher priority
            myThread.setPriority(Thread.MIN_PRIORITY + 3);
            myThread.start();
        }

        public void stop() {
            myThread = null;
        }

        private void update() {
            if (!EventQueue.isDispatchThread())
                EventQueue.invokeLater(new BranchPanel.TextUpdater(this,
                                       millisToString(state.getElapsedTime()), uif));
            else
                setText(millisToString(state.getElapsedTime()));
        }

        private String prefix;
        private MonitorState state;
        private UIFactory uif;
        private volatile Thread myThread;
    }

    /**
     * Converts time in millisections to localized HH:MM:SS (or equivalent)
     * string.
     */
    static final String millisToString(long millis) {
        // lazy init.
        if (i18n == null)
            i18n = I18NResourceBundle.getBundleForClass(ElapsedTimeMonitor.class);

        int seconds = (int)((millis / 1000) % 60);
        int minutes = (int)((millis / 60000) % 60);
        int hours = (int)(millis / 3600000);

        String h, m, s;

        if (hours < 10)
            h = "0" + Integer.toString(hours);
        else
            h = Integer.toString(hours);

        if (minutes < 10)
            m = "0" + Integer.toString(minutes);
        else
            m = Integer.toString(minutes);

        if (seconds < 10)
            s = "0" + Integer.toString(seconds);
        else
            s = Integer.toString(seconds);

        String[] args = {h, m, s};

        return i18n.getString("etm.hms", args);
    }
}


