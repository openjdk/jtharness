/*
 * $Id$
 *
 * Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.sun.javatest.services.Message.MessageType;

/**
 * Implementation for {@link com.sun.javatest.services.ServiceExecutor} interface,
 * that starts services as separate processes. Content of start message should
 * have type {@link com.sun.javatest.services.ProcessParams} and contain data,
 * necessary for {@code java.lang.ProcessBuilder} to start process.
 *
 */
public class ProcessExecutor implements ServiceExecutor {

    private Process proc;

    protected List<String> getCommand(Message start) {
        ProcessParams params = (ProcessParams)start.getContent();
        return params.getCommand();
    };

    protected Map<String, String> getEnv(Message start) {
        ProcessParams params = (ProcessParams)start.getContent();
        return params.getEnvironment();

    };

    protected File getWorkDir(Message start) {
        ProcessParams params = (ProcessParams)start.getContent();
        return params.getWorkDirectory();
    };

    protected void checkMessage(Message msg, MessageType desired)
            throws BadMessageException {

        if (msg.getType() != desired) {
            throw new BadMessageException();
        }

        if (desired == MessageType.START) {
            if ( !(msg.getContent() instanceof ProcessParams) ) {
                throw new BadMessageException();
            }
        }
    }

    public static class BadMessageException extends Exception {

    }

    public Message startService(Message startMsg) {

        try {
            checkMessage(startMsg, MessageType.START);
        }
        catch (BadMessageException ex) {
            return new Message(MessageType.ERROR, ex);
        }

        List<String> command = getCommand(startMsg);

        ProcessBuilder pb = new ProcessBuilder(command);

        Map<String, String> env = getEnv(startMsg);
        if (env != null && env.size() > 0) {
            Map<String, String> currEnv = pb.environment();
            currEnv.clear();
            currEnv.putAll(env);
        }

        File wd = getWorkDir(startMsg);
        if (wd != null && wd.exists()) {
            pb.directory(wd);
        }

        try {
            proc = pb.start();
        } catch (IOException ex) {
            return new Message(MessageType.ERROR, ex);
        }

        return new Message(MessageType.STARTED, null);

    }

    public Message stopService(Message stopMsg) {

        try {
            checkMessage(stopMsg, MessageType.STOP);
        } catch (BadMessageException ex) {
            return new Message(MessageType.ERROR, ex);
        }

        if (proc == null) {
            return new Message(MessageType.ERROR, "Process was not started");
        }

        proc.destroy();
        try {
            proc.waitFor();
        } catch (InterruptedException ex) {}

        Message rsps = new Message(MessageType.STOPPED, "Process stopped successfully.\n" +
                "Exit value: " + proc.exitValue());
//        proc = null;
        return rsps;
    }

    public Message isAlive(Message isAlive) {

        try {
            checkMessage(isAlive, MessageType.IS_ALIVE);
        }
        catch (BadMessageException ex) {
            return new Message(MessageType.ERROR, ex);
        }

        if (proc != null) {
            try {
                int exitValue = proc.exitValue();
                return new Message(MessageType.NOT_ALIVE,
                        "Process already terminated.\nExit value: " + exitValue);
            }
            catch(IllegalThreadStateException e) {
                return new Message(MessageType.ALIVE, "Process not terminated yet");
            }
        }
        else {
            return new Message(MessageType.NOT_ALIVE, "Process already stopped");
        }
    }

    public InputStream getServiceErrorStream() {
        if (proc != null) {
            return proc.getErrorStream();
        }
        else {
            return null;
        }
    }

    public InputStream getServiceOutputStream() {
        if (proc != null) {
            return proc.getInputStream(); // process output!
        }
        else {
            return null;
        }
    }
}
