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


package jthtest.tools;

import org.netbeans.jemmy.TimeoutExpiredException;

public abstract class Task<resType> extends Thread {

    private final Object workingLock = new Object();
    protected resType result;
    protected Throwable exception = null;
    private State state = State.NOT_RUN;

    public Task() {
        this(true);
    }

    public Task(boolean start) {
        super();
        init();
        if (start) {
            this.start();
        }
    }

    protected void init() {
    }

    public final State getTaskState() {
        if (exception != null) {
            throw new RuntimeException(exception);
        }
        return state;
    }

    public synchronized void interruptTask() {
        if (exception != null) {
            throw new RuntimeException(exception);
        }
        if (state != State.FINISHED) {
            this.interrupt();
            state = State.INTERUPPED;
            interruptImpl();
        }
    }

    protected void interruptImpl() {
    }

    public synchronized void pause() {
        if (exception != null) {
            throw new RuntimeException(exception);
        }
        if (state != State.INTERUPPED && state != State.FINISHED) {
            state = State.INTERUPPED;
            pauseImpl();
        }
    }

    protected void pauseImpl() {
    }

    public void waitForDone() {
        synchronized (workingLock) {
            if (exception != null) {
                throw new RuntimeException(exception);
            }
        }
        return;
    }

    public resType getResult() {
        synchronized (workingLock) {
            if (exception != null) {
                throw new RuntimeException(exception);
            }
            return result;
        }
    }

    protected abstract void runImpl() throws Throwable;

    @Override
    public final void run() {
        synchronized (workingLock) {
            state = State.WORKING;
            try {
                runImpl();
                state = State.FINISHED;
            } catch (Throwable ex) {
                exception = ex;
                state = State.INTERUPPED;
            }
        }
    }

    public final void startAndWaitForDone() {
        if (state == State.NOT_RUN) {
            start();
        }
        waitForDone();
    }

    @Override
    public final void start() {
        super.start();
        while (state == State.NOT_RUN) {
            Thread.yield();
        }
    }

    public static enum State {

        NOT_RUN, WORKING, INTERUPPED, FINISHED, PAUSED
    }

    public abstract static class Waiter extends Task<Boolean> {

        protected int maxTimeToWaitMS;
        protected int stepMS;

        public Waiter() {
            this(true, 10000, 100);
        }

        public Waiter(int maxtime) {
            this(true, maxtime, 100);
        }

        public Waiter(int maxtime, int step) {
            this(true, maxtime, step);
        }

        public Waiter(boolean start, int maxtime, int step) {
            super(start);
            maxTimeToWaitMS = maxtime;
            stepMS = step;
            init();
        }

        public Waiter(boolean start) {
            this(start, 10000, 100);
        }

        @Override
        protected void runImpl() throws InterruptedException {
            int t = 0;
            while (!check() && super.getTaskState() == State.WORKING) {
                if (t > maxTimeToWaitMS) {
                    result = false;
                    exception = new TimeoutExpiredException(getTimeoutExceptionDescription());
                    return;
                }
                Waiter.sleep(stepMS);
                t += stepMS;
            }
            result = true;
        }

        public void stopWaiter() {
            interruptTask();
        }

        protected abstract boolean check();

        protected String getTimeoutExceptionDescription() {
            return "Timeout expired in waiter task " + this;
        }
    }
}
