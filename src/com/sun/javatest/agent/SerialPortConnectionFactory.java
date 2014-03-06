/*
 * $Id$
 *
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.agent;

import java.io.IOException;
import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;

/**
 * A factory for creating connections to be used by agents communicating
 * via a serial port.
 */
public class SerialPortConnectionFactory implements ConnectionFactory
{
    /**
     * Create a factory for creating connections to be used by agents
     * communicating via a serial port.
     * @param name      The name of the serial port to be used when creating connections
     * @param app       The name of the application requesting the use of the port.
     * @param timeout   The time to wait for the port to become available when creating
     *                  a connection.
     * @throws NoSuchPortException if an invalid port name is specified.
     */
    public SerialPortConnectionFactory(String name, String app, int timeout) throws NoSuchPortException {
        this(CommPortIdentifier.getPortIdentifier(name), app, timeout);
    }

    /**
     * Create a factory for creating connections to be used by agents
     * communicating via a serial port.
     * @param portId    An identifier for the serial port to use.
     * @param app       The name of the application requesting the use of the port.
     * @param timeout   The time to wait for the port to become available when creating
     *                  a connection.
     */
    public SerialPortConnectionFactory(CommPortIdentifier portId, String app, int timeout) {
        if (portId.getPortType() != CommPortIdentifier.PORT_SERIAL)
            throw new IllegalArgumentException("Not a serial port: " + portId.getName());
        this.portId = portId;
        this.app = app;
        this.timeout = timeout;
    }

    public synchronized Connection nextConnection() throws ConnectionFactory.Fault {
        try {
            Connection c = new SerialPortConnection(portId, app, timeout);
            //System.err.println("Created connection: " + c.getName());
            return c;
        }
        catch (InterruptedException e) {
            System.err.println("Error connection: " + e);
            throw new ConnectionFactory.Fault(e, true);
        }
        catch (IOException e) {
            System.err.println("Error connection: " + e);
            throw new ConnectionFactory.Fault(e, true);
        }
        catch (PortInUseException e) {
            System.err.println("Error connection: " + e);
            throw new ConnectionFactory.Fault(e, false);
        }
    }

    public void close() {
    }

    private CommPortIdentifier portId;
    private String app;
    private int timeout;
}
