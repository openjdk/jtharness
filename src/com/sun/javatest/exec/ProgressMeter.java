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

import java.awt.AWTEventMulticaster;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleValue;

import javax.swing.JComponent;
import javax.swing.Timer;

import com.sun.javatest.util.I18NResourceBundle;

// this class needs to be rewritten; it's unclear what it's place in the world is

/**
 * ProgressMeter provides a horizontal bar that displays the relative proportions of
 * an array of numbers.
 */
class ProgressMeter extends JComponent implements Accessible {
    ProgressMeter(Color[] colors, MonitorState m) {
        this(colors);
        this.state = m;
        values = new int[colors.length];
    }

    ProgressMeter(Color[] colors, MonitorState m, String[] actions) {
        this(colors);
        this.actions = actions;
        this.state = m;
        values = new int[colors.length];
    }

    ProgressMeter(Color[] colors) {
        this.colors = colors;
        values = new int[colors.length];
        //setBorder(BorderFactory.createEmptyBorder(10,5,10,5));
    }

    public AccessibleContext getAccessibleContext() {
        if (ac == null)
            ac = new PM_AccessibleContext(this);

        return ac;
    }

    public void setIndeterminate(boolean state) {
        indet = state;
    }

    public void start() {
        if (indet == false) {
            myThread = new Thread() {
                public void run() {
                    while(myThread == currentThread()) {
                        try {
                            synchronized (myThread) {
                                update();

                                // this is really an assertion, sync in
                                // stop() should not allow this to happen
                                // null indicates that we are not running and
                                // should not be updating
                                if (myThread == null)
                                    break;

                                myThread.wait(5000);
                            }
                        }
                        catch(InterruptedException e) {
                        }
                    }   // while
                }
            };  // Thread()

            // user will notice this, so set a higher priority
            myThread.setPriority(Thread.MIN_PRIORITY + 2);
            myThread.start();
        }
        else {
            clear();
            indetTimer = new Timer(100, new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    System.err.println("updating, width=" + getSize().width);
                }
            });
        }
    }

    public void stop() {
        if (myThread == null)
            return;

        if (indet == false) {
            Thread old = myThread;

            synchronized (old) {
                myThread = null;
                old.notify();
            }
        }
        else {
            indetTimer.stop();
            clear();
        }
    }

    public void addActionListener(ActionListener l) {
        actionListener = AWTEventMulticaster.add(actionListener, l);
        if (actionListener != null && mouseListener == null) {
            mouseListener = new MouseAdapter() {
                public void mouseClicked(MouseEvent me) {
                    int index = resolve(me.getX(), me.getY());
                    if (index == -1)
                        index = actions.length - 1;
                    if (index != -1) {
                        ActionEvent ae =
                            new ActionEvent(ProgressMeter.this,
                                            ActionEvent.ACTION_PERFORMED,
                                            actions[index]);
                        actionListener.actionPerformed(ae);
                    }
                }
            };
            addMouseListener(mouseListener);
        }
    }

    public void removeActionListener(ActionListener l) {
        actionListener = AWTEventMulticaster.remove(actionListener, l);
        if (actionListener == null)
            removeMouseListener(mouseListener);
    }

    public synchronized void clear() {
        values = new int[colors.length];
        repaint();
    }

    public Dimension getMinimumSize() {
        return new Dimension(30, 8);
    }

    public Dimension getPreferredSize() {
        return new Dimension(50, 15);
    }


    public synchronized void set(int[] v) {
        int total = 0;
        for (int i = 0; i < v.length; i++)
            total += v[i];
        set(v, total);
    }

    /**
     * Retrieve the stats from the MonitorState and repaint.
     */
    public synchronized void update() {
        int[] stats = state.getStats();
        set(stats, state.getTestsFoundCount());
        //repaint();
    }

    public synchronized void set(int[] v, int total) {
        // convert to normalized cumulative values and see if any have changed
        boolean changed = false;
        int totalSoFar = 0;
        for (int i = 0; i < v.length; i++) {
            totalSoFar += v[i];
            int x = (total == 0 ? 0 : totalSoFar * SCALE / total);
            if (values[i] != x ) {
                values[i] = x;
                changed = true;
            }
        }

        if (changed) {
            repaint(100);
            if (ac != null)
                ac.notifyNewStats();
        }
    }

    public synchronized void paint(Graphics g) {
        Dimension dims = getSize();
        g.drawRect(0, 0, dims.width - 1, dims.height - 1);
        g.setColor(Color.white);
        g.fillRect(1, 1, dims.width - 2, dims.height - 2);
        int start = 0;
        for (int i = 0; i < values.length; i++) {
            int end = values[i] * (dims.width - 2) / SCALE;
            g.setColor(colors[i]);
            g.fillRect(1 + start, 1, end - start, dims.height - 2);
            start = end;
        }
    }

    int resolve(int x, int y) {
        Dimension dims = getSize();
        if (x > 0) {
            for (int i = 0; i < values.length; i++) {
                int end = values[i] * (dims.width - 2) / SCALE;
                if (x < end)
                    return i;
            }
        }
        return -1;
    }

    private boolean indet;
    private Timer indetTimer;

    private Color[] colors;
    private String[] actions;
    private int[] values;
    private static final int SCALE = 1024;
    private ActionListener actionListener;
    private MouseListener mouseListener;
    private MonitorState state;
    private PM_AccessibleContext ac;
    private volatile Thread myThread;
    private static I18NResourceBundle i18n;

    private class PM_AccessibleContext
            extends JComponent.AccessibleJComponent
            implements AccessibleValue /*, AccessibleText*/ {
        PM_AccessibleContext(ProgressMeter pm) {
            //super();
            this.pm = pm;
        }

        public String getAccessibleDescription() {
            String superdesc = super.getAccessibleDescription();
            if (superdesc == null)
                return pm.getToolTipText();
            else {
                if (i18n == null)
                    i18n = I18NResourceBundle.getBundleForClass(getClass());

                return i18n.getString("pmtr.desc");
            }
        }

        public String getAccessibleName() {
            String supername = super.getAccessibleName();
            if (supername == null) {
                if (i18n == null)
                    i18n = I18NResourceBundle.getBundleForClass(getClass());
                else { }

                return i18n.getString("pmtr.name");
            }
            else {
                return supername;
            }
        }

        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.PROGRESS_BAR;
        }

        public AccessibleValue getAccessibleValue() {
            return this;
        }

        // accessible value
        public Number getCurrentAccessibleValue() {
            int ttl = 0;
            int[] stats = state.getStats();

            // add up all but last component
            // this gives the info of a traditional progress bar
            for (int i = 0; i < stats.length - 1; i++)
                ttl += stats[i];

            return new Integer(ttl);
        }

        public Number getMaximumAccessibleValue() {
            return new Integer(state.getTestsFoundCount());
        }

        public Number getMinimumAccessibleValue() {
            return new Integer(0);
        }

        public boolean setCurrentAccessibleValue(Number n) {
            // not a valid action
            return false;
        }

        // specialized methods

        /**
         * External notification that the PM has changed.
         */
        void notifyNewStats() {
        }

        private ProgressMeter pm;
    }
}
