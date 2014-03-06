/*
 * $Id$
 *
 * Copyright (c) 2009, 2013, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.services;

import java.io.InputStream;

import com.sun.javatest.services.Message.MessageType;

public class ThreadExecutor implements ServiceExecutor {
    private StoppableRunnable runner;
    private Thread worker;

    public ThreadExecutor(StoppableRunnable r) {
        if (r == null) {
            throw new NullPointerException("Service executor cannot be attached to null runnable.");
        }
        runner = r;
    }

    public synchronized Message startService(Message startMsg) {
        if (startMsg.getType() != MessageType.START) {
            return new Message(MessageType.ERROR, "Bad Start Message");
        }

        if (runner != null && worker != null && worker.isAlive()) {
            // might be nice to log if we are terminating and restarting
            // tell runner to stop, recreate a new thread reusing the runner
            runner.stop();
        }

        worker = new Thread(runner);
        worker.setDaemon(true);
        worker.start();

        return new Message(MessageType.STARTED, "Execution Thread Started");
    }

    public synchronized Message stopService(Message stopMsg) {
        if (stopMsg.getType() != MessageType.STOP) {
            return new Message(MessageType.ERROR, "Bad stop Message");
        }

        runner.stop();
        if (worker == null || worker.isAlive()) {
            return new Message(MessageType.ERROR, "Thread Wasn't Stopped");
        }

        return new Message(MessageType.STOPPED, "Thread Was Stopped Successfully");
    }

    public synchronized Message isAlive(Message isAlive) {
        if (isAlive.getType() != MessageType.IS_ALIVE) {
            return new Message(MessageType.ERROR, "Bad isAlive Message");
        }

        if (worker != null && worker.isAlive()) {
            return new Message(MessageType.ALIVE, "Service is alive");
        }
        else {
            return new Message(MessageType.NOT_ALIVE, "Service is not alive");
        }
    }

    public InputStream getServiceErrorStream() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InputStream getServiceOutputStream() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
