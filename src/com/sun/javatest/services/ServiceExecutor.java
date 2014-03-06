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

import java.io.InputStream;

/**
 * Interface, which describes concept of service execution. Contains methods to
 * start, stop service, check that it is alive. Provides InputStreams to read service
 * error and output.
 *
 */
public interface ServiceExecutor {

    /**
     * Method, that starts service.
     * @param startMsg incoming message with {@link com.sun.javatest.services.Message.MessageType#START}
     * type and start arguments (in any form, acceptable by ServiceExecutor's implementation).
     * @return response message with type {@link com.sun.javatest.services.Message.MessageType#STARTED},
     * in case service was started successfully, or type {@link com.sun.javatest.services.Message.MessageType#ERROR},
     * in case some problems occurred.
     */
    public Message startService(Message startMsg);

    /**
     * Method, that stops service.
     * @param stopMsg incoming message with {@link com.sun.javatest.services.Message.MessageType#STOP}
     * type.
     * @return response message with type {@link com.sun.javatest.services.Message.MessageType#STOPPED},
     * in case service was stopped successfully, or type {@link com.sun.javatest.services.Message.MessageType#ERROR},
     * in case some problems occurred.
     */
    public Message stopService(Message stopMsg);

    /**
     * Method, that checks that service is alive and runs normally.
     * @param isAlive incoming message with {@link com.sun.javatest.services.Message.MessageType#IS_ALIVE}
     * type.
     * @return response message with type {@link com.sun.javatest.services.Message.MessageType#ALIVE},
     * in case service alive, or {@link com.sun.javatest.services.Message.MessageType#NOT_ALIVE},
     * if there any problems with running service.
     */
    public Message isAlive(Message isAlive);

    /**
     * Method, which provides access for service error output.
     * @return InputStream to read service error output.
     */
    public InputStream getServiceErrorStream();

    /**
     * Method, which provides access for service output.
     * @return InputStream to read service output.
     */
    public InputStream getServiceOutputStream();


}
