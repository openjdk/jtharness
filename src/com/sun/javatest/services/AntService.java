/*
 * $Id$
 *
 * Copyright (c) 2009, 2011, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.services.Message.*;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementation of {@link com.sun.javatest.services.Service} abstract class
 * for running services, represented as Ant targets.
 * <p>
 * Uses {@link com.sun.javatest.services.ProcessExecutor} as default service
 * executor.
 * <p>
 * Names for standard parameters are represented in:<p>
 * {@link com.sun.javatest.services.AntService#ANT_BIN}<p>
 * {@link com.sun.javatest.services.AntService#ANT_TARGETS}<p>
 * {@link com.sun.javatest.services.AntService#ANT_WORKDIR}<p>
 * {@link com.sun.javatest.services.AntService#ANT_ENV}<p>
 * All other parameters are interpreted as command arguments for {@code ant}
 * executable.
 *
 */
public class AntService extends Service {

    /**
     * Specifies default service executor. Used by
     * {@link com.sun.javatest.services.ServiceManager} to set executors for
     * {@link com.sun.javatest.services.LocalConnector}.
     *
     * @return new instance of {@link com.sun.javatest.services.ProcessExecutor}
     */
    @Override
    public ServiceExecutor getDefaultServiceExecutor() {
        return new ProcessExecutor();
    }

    /**
     * Method to prepare messages with data, required to run Ant targets.
     * Only {@link com.sun.javatest.services.Message.MessageType#START} needs
     * content, other messages have nothing to be sent, except their type
     *
     * @param type The type of Message to create.
     * @return     new message.
     * @throws com.sun.javatest.services.Service.MalformedParamsException
     */
    @Override
    public Message prepareMessage(MessageType type)
            throws MalformedParamsException {
        switch (type) {
            case START:
                return prepareStartMessage();
            default:
                return new Message(type, null);
        }
    }

    private Message prepareStartMessage() throws MalformedParamsException {
        ProcessParams params = new ProcessParams();

        Map<String, String> p = props.resolveProperties();
        String antBin = p.remove(ANT_BIN);
        if (antBin == null) {
            throw new MalformedParamsException
                    ("Path to ANT binary doesn't specified", p);
        }
        List<String> command = new LinkedList();
        command.add(antBin);

        String workDir = p.remove(ANT_WORKDIR);
        if (workDir != null) {

            params.setWorkDirectory(new File(workDir));
        }

        Map<String, String> env = new TreeMap();
        for (String name : p.keySet()) {
            if (name.startsWith(ANT_ENV)) {
                env.put(name.substring(ANT_ENV.length()), p.remove(name));
            }
            else if (!name.equals(ANT_TARGETS)) {
                command.add(p.remove(name));
            }
        }
        params.setEnvironment(env);

        if (p.get(ANT_TARGETS) != null) {
            command.add(p.get(ANT_TARGETS));
        }
        params.setCommand(command);

        Message start = new Message(MessageType.START, params);
        return start;
    }

    /**
     * The name of parameter, that specifies path to ant executable
     */
    public static final String ANT_BIN = "ant.bin";

    /**
     * The name of parameter, that specifies space-separated list of Ant targets
     * to execute
     */
    public static final String ANT_TARGETS = "ant.targets";

    /**
     * The name of parameter, that specifies working directory for ant process.
     */
    public static final String ANT_WORKDIR = "ant.workdir";

    /**
     * Each parameter, started with {@code ant.env.} interpreted as property
     * for ant process environment.
     * <p>
     * The property name is achieved by subtracting ANT_ENV prefix from
     * argument name. For example, to specify JAVA_HOME variable, arg name
     * should be {@code ant.env.JAVA_HOME}
     *
     */
    public static final String ANT_ENV = "ant.env.";
}
