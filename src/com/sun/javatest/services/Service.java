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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.javatest.Parameters;
import com.sun.javatest.TestSuite;
import com.sun.javatest.TestSuite.DuplicateLogNameFault;
import com.sun.javatest.TestSuite.NoSuchLogFault;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.services.Message.MessageType;

/**
 * Abstract class, that represents service description and provides basic
 * facilities to manage service execution.
 * <p>
 * It is harness-side service part. Service execution is performed by
 * {@link com.sun.javatest.services.ServiceExecutor} component, which is another
 * part of service.
 * <p>
 * Each subclass should provide implementation for 2 methods.
 * {@link com.sun.javatest.services.Service#getDefaultServiceExecutor()} returns
 * default ServiceExecutor object to use.
 * {@link com.sun.javatest.services.Service#prepareMessage()}
 * create messages to be sent to executor by interpreting service properties.
 *
 */
public abstract class Service implements MessageHandler {

    protected ServiceProperties props;
    protected Connector conn;
    protected String id;
    protected String descr;
    protected Logger log;

    /**
     * Specifies default ServiceExecutor to use. This executor may be replaced
     * later, by achieving Service from ServiceManager, and setting another
     * executor to its connector.
     * @return
     */
    public abstract ServiceExecutor getDefaultServiceExecutor();

    /**
     * create messages to be sent to executor by interpreting service
     * properties.
     *
     * @param type Type of message to create.
     * @return created message.
     * @throws com.sun.javatest.services.Service.MalformedParamsException
     * in case service properties are inconsistent.
     */
    public abstract Message prepareMessage(MessageType type)
            throws MalformedParamsException;

    /**
     * Default implementation for {@link com.sun.javatest.services.MessageHandler}
     * interface. Does nothing.
     *
     * @param msg message to handle.
     */
    public void handleMessage(Message msg) {}

    /**
     * Method to start service. Checks connection, prepares start message with
     * start parameters, sends start message using connector and waits for
     * response.
     *
     * @return true, if service was started. Service started successfully,
     * if response message has
     * {@link com.sun.javatest.services.Message.MessageType.START} type.
     * @throws com.sun.javatest.services.Service.NotConnectedException if
     * connection is not opened or alive.
     * @throws com.sun.javatest.services.Service.ServiceError if response
     * message has {@link com.sun.javatest.services.Message.MessageType.ERROR}
     * type
     */
    public boolean start() throws NotConnectedException, ServiceError {
        if (conn == null || !conn.connected()) {
            throw new NotConnectedException("Not connected to executor");
        }

        Message msg;
        try {
            msg = prepareMessage(MessageType.START);
        } catch (MalformedParamsException e) {
            logMalformedException(e, MessageType.START);
            return false;
        }
        Message response = conn.send(msg);

        if (response.getType() == MessageType.ERROR) {
            throw new ServiceError(response.getContent().toString());
        }

        if (response.getType() != MessageType.STARTED) {
            logBadMsgTypeException(response.getType(), MessageType.STARTED);
            return false;
        }
        return true;
    };

    /**
     * Method to stop service. Checks connection, prepares stop message without
     * parameters, sends stop message using connector and waits for
     * response.
     *
     * @return true, if service was stopped. Service stopped successfully,
     * if response message has
     * {@link com.sun.javatest.services.Message.MessageType.STOPPED} type.
     * @throws com.sun.javatest.services.Service.NotConnectedException if
     * connection is not opened or alive.
     * @throws com.sun.javatest.services.Service.ServiceError if response
     * message has {@link com.sun.javatest.services.Message.MessageType.ERROR}
     * type
     */
    public boolean stop() throws NotConnectedException, ServiceError {
        if (conn == null || !conn.connected()) {
            throw new NotConnectedException("Not connected to executor");
        }

        Message msg;
        try {
            msg = prepareMessage(MessageType.STOP);
        } catch (MalformedParamsException e) {
            logMalformedException(e, MessageType.STOP);
            return false;
        }
        Message response = conn.send(msg);

        if (response.getType() == MessageType.ERROR) {
            throw new ServiceError(response.getContent().toString());
        }

        if (response.getType() != MessageType.STOPPED) {
            logBadMsgTypeException(response.getType(), MessageType.STOPPED);
            return false;
        }
        return true;

    };

    /**
     * Method to check that service is alive. Checks connection, prepares
     * "is alive" message without parameters, sends message using connector and
     * waits for response.
     *
     * @return true, if service is alive. Service is alive, if response message
     * has {@link com.sun.javatest.services.Message.MessageType.ALIVE} type.
     * Service is not alive, if response message has
     * {@link com.sun.javatest.services.Message.MessageType.NOT_ALIVE} type.
     * @throws com.sun.javatest.services.Service.NotConnectedException if
     * connection is not opened or alive.
     * @throws com.sun.javatest.services.Service.ServiceError if response
     * message has {@link com.sun.javatest.services.Message.MessageType.ERROR}
     * type
     */
    public boolean isAlive() throws NotConnectedException, ServiceError {
        if (conn == null || !conn.connected()) {
            throw new NotConnectedException("Not connected to executor");
        }

        Message msg;
        try {
            msg = prepareMessage(MessageType.IS_ALIVE);
        } catch (MalformedParamsException e) {
            logMalformedException(e, MessageType.IS_ALIVE);
            return false;
        }
        Message response = conn.send(msg);

        if (response.getType() == MessageType.ERROR) {
            throw new ServiceError(response.getContent().toString());
        }

        if (response.getType() != MessageType.ALIVE &&
                response.getType() != MessageType.NOT_ALIVE) {
                logBadMsgTypeException(response.getType(), MessageType.ALIVE);
                logBadMsgTypeException(response.getType(), MessageType.NOT_ALIVE);
            return false;
        }
        return response.getType() == MessageType.ALIVE;

    };

    /**
     * Method to provide access for ServiceExecutor's error output. Redirects
     * invokation to connector.
     * @return InputStream to read service error output.
     * @throws com.sun.javatest.services.Service.NotConnectedException if
     * connection is not opened or alive.
     */
    public InputStream getInputStream() throws NotConnectedException {
        if (conn == null || !conn.connected()) {
            throw new NotConnectedException("Not connected to executor");
        }

        return conn.getServiceOutputStream();
    };

    /**
     * Method to provide access for ServiceExecutor's output. Redirects
     * invokation to connector.
     * @return InputStream to read service output.
     * @throws com.sun.javatest.services.Service.NotConnectedException if
     * connection is not opened or alive.
     */
    public InputStream getErrorStream() throws NotConnectedException {
        if (conn == null || !conn.connected()) {
            throw new NotConnectedException("Not connected to executor");
        }

        return conn.getServiceErrorStream();
    };



    public static class NotConnectedException extends Exception {
        public NotConnectedException(String msg) {
            super(msg);
        }
    }

    public static class ServiceError extends Exception {
        public ServiceError(String msg) {
            super(msg);
        }
    }
    public static class MalformedParamsException extends Exception {
        private String descr;
        private Map<String, String> params;

        public MalformedParamsException(String s, Map<String, String> params) {
            descr = s;
            this.params = params;
        }

        public String getDescription() {
            return descr;
        }

        public Map<String, String> getParams() {
            return params;
        }
    }

    // Setters and getters

    /**
     * Method to replace default {@link com.sun.javatest.services.LocalConnector}
     * @param conn new {@link com.sun.javatest.services.Connector} to use
     */
    public void setConnector(Connector conn) {
        this.conn = conn;
    }

    /**
     * @return return used {@link com.sun.javatest.services.Connector}
     */
    public Connector getConnector() {
        return conn;
    }

    /**
     * Method to set ServiceProperties for this service.
     * @param props ServiceProperties of this service
     */
    public void setProperties(ServiceProperties props) {
        this.props = props;
    };

    /**
     * Method to set ServiceProperties for this service.
     * @return ServiceProperties of this service.
     */
    public ServiceProperties getProperties() {
        return props;
    }

    /**
     * Method to set {@code id} for this service.
     * @param id unique identifier of the service in test suite.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Method to get {@code id} of this service.
     * @return unique identifier of the service in test suite.
     */
    public String getId() {
        return id;
    }

    /**
     * Method to set string with description of this service.
     *
     * @param descr description of the service.
     */
    public void setDescription(String descr) {
        this.descr = descr;
    }

    /**
     * Method to get string with description of this service.
     * @return description of the service.
     */
    public String getDescription() {
        return descr;
    }

    Logger createLog(Parameters params) {
        TestSuite ts = params.getTestSuite();
        WorkDirectory wd = params.getWorkDirectory();

        String name = "service-" + id + "-log";
        try {
            log = ts.createLog(wd, null, name);
        } catch (DuplicateLogNameFault ex) {
            try {
                log = ts.getLog(wd, name);
            } catch (NoSuchLogFault ex1) {
            }
        }
        return log;
    }

    /**
     * Method to get Logger used by this service.
     * @return logger used by this service.
     */
    public Logger getLog() {
        return log;
    }

    private void logMalformedException(MalformedParamsException e, MessageType t) {
        String msg = "Message malformed.\n";
        msg += "Message type: " + t.toString() + "\n";
        msg += "Parameters used:\n";
        Map<String, String> params = e.getParams();
        for (String s : params.keySet()) {
            msg += s + '=' + params.get(s);
        }

        log.log(Level.SEVERE, msg);
    }

    private void logBadMsgTypeException(MessageType received, MessageType expected) {
        String msg = "Wrong response msg type: \n";
        msg += "Received :" + received.toString() + "\n";
        msg += "Expected :" + expected.toString() + "\n";

        log.log(Level.SEVERE, msg);
    }

//    public void setOutWriter(Writer w) {
//        outW = w;
//    }
//
//    public Writer getOutWriter() {
//        if (outW == null) {
//
//        }
//        return outW;
//    }
//
//    public void setErrtWriter(Writer w) {
//        errW = w;
//    }
//
//    public Writer getErrWriter() {
//
//        return errW;
//    }
}
