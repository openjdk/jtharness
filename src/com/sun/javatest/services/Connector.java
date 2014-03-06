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

import java.io.InputStream;

/**
 * Connector is responsible for connection between harness representative -
 * {@link com.sun.javatest.services.Service} interface, and
 * {@link com.sun.javatest.services.ServiceExecutor}
 * <p>
 * Each executor type is related with service type in that messages, prepared
 * by Service type, should be understandable by ServiceExecutor.
 * <p>
 * Connector doesn't depend on Service and Service executor type, it has unified
 * interface, and any Connector implementation should work well with any
 * Service - ServiceExecutor pair.
 * <p>
 * Any Connector implementation should provide mechanism for synchronous and
 * asynchronous message exchange, and provide ServiceExecutor's output and error
 * as input streams.
 */
public interface Connector {

    /**
     * Method, invoked by harness, to establish connection with ServiceExecutor.
     */
    public void esteblishConnection();

    /**
     * Method, invoked by harness, to refuse connection with ServiceExecutor.
     */
    public void refuseConnection();

    /**
     * Method, that may be invoked, to check that connection is alive.
     * @return
     */
    public boolean connected();

    /**
     * Method for synchronous message exchange
     *
     * @param msg {@link com.sun.javatest.services.Message} container with
     * message type and parameters to be sent.
     * @return response message
     */
    public Message send(Message msg);

    /**
     * Method for asynchronous message exchange.
     * @param handler handler to process asynchronous incoming messages.
     */
    public void setMessageHandler(MessageHandler handler);

    /**
     * Method to provide access for ServiceExecutor's error output.
     * @return InputStream to read service error output.
     */
    public InputStream getServiceErrorStream();

    /**
     * Method to provide access for ServiceExecutor's output.
     * @return InputStream to read service output.
     */
    public InputStream getServiceOutputStream();
}
