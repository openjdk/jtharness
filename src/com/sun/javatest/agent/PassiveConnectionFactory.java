/*
 * $Id$
 *
 * Copyright (c) 1996, 2012, Oracle and/or its affiliates. All rights reserved.
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
import java.net.ServerSocket;

/**
 * A factory for creating connections to be used by agents running in "passive" mode.
 */
public class PassiveConnectionFactory implements ConnectionFactory
{
    /**
     * Create a factory for creating connections to be used by agents running
     * in "passive" mode.
     * @param port The port on which connections will listen.
     *          Specify 0 for a dynamically allocated port.
     * @param backlog The number of buffered incoming connection requests
     *          to be accepted.
     * @throws IOException if problems occur while setting up the factory
     */
    public PassiveConnectionFactory(int port, int backlog) throws IOException {
        if (port < 0) {
            throw new IllegalArgumentException("Cannot start passive agent connection - port number must be non-negative.");
        }
        else if(backlog < 0) {
            throw new IllegalArgumentException("Cannot start passive agent connection - backlog value must be zero or greater.");
        }

        serverSocket = SocketConnection.createServerSocket(port, backlog);
    }

    /**
     * Create a factory for creating connections to be used by agents running
     * in "passive" mode.
     * @param serverSocket The server socket used to accept incoming
     *          connection requests.
     */
    public PassiveConnectionFactory(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    /**
     * Get the port on which incoming connection requests will be accepted.
     * @return The port on which incoming connection requests will be accepted.
     */
    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public Connection nextConnection() throws ConnectionFactory.Fault {
        try {
//          return new SocketConnection(serverSocket.accept());
            return new InterruptableSocketConnection(serverSocket.accept());
        }
        catch (IOException e) {
            throw new ConnectionFactory.Fault(e, false);
        }
    }

    public void close() throws ConnectionFactory.Fault {
        try {
            serverSocket.close();
        }
        catch (IOException e) {
            throw new ConnectionFactory.Fault(e, true);
        }
    }

    private ServerSocket serverSocket;
}
