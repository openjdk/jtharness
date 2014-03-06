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
 * Implementation of {@link com.sun.javatest.services.Connector} interface, which
 * redirects requests to local ServiceExecutor instance.
 *
 */
public class LocalConnector implements Connector {

    private ServiceExecutor executor;

    /**
     * Creates new LocalConnector, with executor, achieved from
     * {@link com.sun.javatest.services.Service#getDefaultServiceExecutor()}
     * method.
     *
     * @param executor executor, achieved from
     * {@link com.sun.javatest.services.Service#getDefaultServiceExecutor()}
     * method.
     */
    public LocalConnector(ServiceExecutor executor) {
        this.executor = executor;
    }

    /**
     * Method to replace default executor.
     * @param executor ServiceExecutor to connect with
     */
    public void setExecutor(ServiceExecutor executor) {
        this.executor = executor;
    }

    /**
     * @return currently used ServiceExecutor.
     */
    public ServiceExecutor getExecutor() {
        return executor;
    }

    public Message send(Message msg) {
        switch (msg.getType()) {
            case START:
                return startExecutor(msg);
            case STOP:
                return stopExecutor(msg);
            case IS_ALIVE:
                return isAlive(msg);
            default:
                break;
        }
        return null;
    }

    public InputStream getServiceErrorStream() {
        return executor.getServiceErrorStream();
    }

    public InputStream getServiceOutputStream() {
        return executor.getServiceOutputStream();
    }

    public void esteblishConnection() {
        //needs nothing to do
    }

    public void refuseConnection() {
        //needs nothing to do
    }

    public boolean connected() {
        return executor != null;
    }

    // Default implementation
    public void setMessageHandler(MessageHandler handler) {
    }

    private Message startExecutor(Message income) {
        return executor.startService(income);
    }

    private Message stopExecutor(Message income) {
        return executor.stopService(income);
    }

    private Message isAlive(Message income) {
        return executor.isAlive(income);
    }

}
