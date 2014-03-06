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

import com.sun.javatest.JavaTestSecurityManager;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Hashtable;

import com.sun.javatest.util.Timer;
import java.net.ServerSocket;

/**
 * A connection via a TCP/IP socket.
 */
public class SocketConnection implements Connection {

    /**
     * Create a connection via a TCP/IP socket.
     * @param socket The socket to use for the connection.
     * @throws IOException if an error occurs getting the streams for the connection.
     * @throws NullPointerException if socket is null
     */
    public SocketConnection(Socket socket) throws IOException {
        if (socket == null) {
            throw new NullPointerException();
        }
        this.socket = socket;
        socketInput = socket.getInputStream();
        socketOutput = socket.getOutputStream();
    }

    /**
     * Create a connection via a TCP/IP socket.
     * @param host The host to which to try to connect to try and get a socket.
     * @param port The port on the host to which to connect to try and get a socket.
     * @throws IOException if an error occurs opening the socket.
     */
    public SocketConnection(final String host, final int port) throws IOException {
        if (host == null) {
            throw new NullPointerException();
        }
        /* experimental
        try {
            socket = (Socket) AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {

                        public Object run() throws IOException {
                            return new Socket(host, port);
                        }
                    });
        } catch (PrivilegedActionException e) {
            throw (IOException) e.getException();
        }*/

        SecurityManager sm = System.getSecurityManager();
        JavaTestSecurityManager jtSm = null;
        boolean prev = false;
        if (sm != null && sm instanceof JavaTestSecurityManager) {
            jtSm = (JavaTestSecurityManager) sm;
            prev = jtSm.setAllowPropertiesAccess(true);
        }
        try {
            socket = new Socket(host, port);
        } finally {
            if (jtSm != null) {
                jtSm.setAllowPropertiesAccess(prev);
            }
        }

        socketInput = socket.getInputStream();
        socketOutput = socket.getOutputStream();
    }

    public String getName() {
        if (name == null) {
            StringBuffer sb = new StringBuffer(32);
            sb.append(getHostName(socket.getInetAddress()));
            sb.append(",port=");
            sb.append(socket.getPort());
            sb.append(",localport=");
            sb.append(socket.getLocalPort());
            name = sb.toString();
        }
        return name;
    }

    public InputStream getInputStream() {
        return socketInput;
    }

    public OutputStream getOutputStream() {
        return socketOutput;
    }

    public synchronized void close() throws IOException {
        socket.close();
        socketInput.close();
        socketOutput.close();
        closed = true;

        if (waitThread != null) {
            waitThread.interrupt();
        }
    }

    public synchronized boolean isClosed() {
        return closed;
    }

    public void waitUntilClosed(int timeout) throws InterruptedException {
        synchronized (this) {
            waitThread = Thread.currentThread();
        }

        Timer.Timeable cb = new Timer.Timeable() {

            public void timeout() {
                synchronized (SocketConnection.this) {
                    if (waitThread != null) {
                        waitThread.interrupt();
                    }
                    try {
                        socketInput.close();
                    } catch (IOException ignore) {
                    }
                    try {
                        socketOutput.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        };

        Timer.Entry e = timer.requestDelayedCallback(cb, timeout);
        try {
            while (true) {
                try {
                    int i = socketInput.read();
                    if (i == -1) {
                        break;
                    }
                } catch (IOException ignore) {
                    break;
                }
            }
        } finally {
            timer.cancel(e);

            synchronized (this) {
                waitThread = null;
            }
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }
    }

    private static String getHostName(InetAddress addr) {
        String s = (String) (addressCache.get(addr));
        if (s == null) {
            s = addr.getHostName();
            addressCache.put(addr, s);
        }
        return s;
    }

    /**
     * Creates a ServerSocket instance in the environment where reading
     * of system properties is allowed.
     *
     * @param port - port to bind
     * @return new created ServerSocket
     * @throws java.io.IOException - if ServerSocket is not created
     */
    public static ServerSocket createServerSocket(int port) throws IOException {
        return createServerSocket(port, 50);
    }

    /**
     * Creates a ServerSocket instance in the environment where reading
     * of system properties is allowed.
     *
     * @param port - port to bind
     * @param backlog - backlog
     * @return new created ServerSocket
     * @throws java.io.IOException - if ServerSocket is not created
     */
    public static synchronized ServerSocket createServerSocket(int port, int backlog)
            throws IOException {
        SecurityManager sm = System.getSecurityManager();
        JavaTestSecurityManager jtSm = null;
        boolean prev = false;
        if (sm != null && sm instanceof JavaTestSecurityManager) {
            jtSm = (JavaTestSecurityManager) sm;
            prev = jtSm.setAllowPropertiesAccess(true);
        }
        try {
            ServerSocket serverSocket = new ServerSocket(port, backlog);
            return serverSocket;
        } finally {
            if (jtSm != null) {
                jtSm.setAllowPropertiesAccess(prev);
            }
        }
    }
    private final Socket socket;
    protected final InputStream socketInput;
    private final OutputStream socketOutput;
    private String name;
    private boolean closed;
    private Thread waitThread;
    private static Timer timer = new Timer();
    private static Hashtable addressCache = new Hashtable();
}
