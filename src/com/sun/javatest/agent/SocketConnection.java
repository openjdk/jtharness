/*
 * $Id$
 *
 * Copyright (c) 1996, 2023, Oracle and/or its affiliates. All rights reserved.
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
import com.sun.javatest.util.Timer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.util.Hashtable;
import java.util.Optional;

/**
 * A connection via a TCP/IP socket.
 */
public class SocketConnection implements Connection {

    private static Timer timer = new Timer();
    private static Hashtable<InetAddress, String> addressCache = new Hashtable<>();
    protected final InputStream socketInput;
    private final Optional<SocketChannel> socketChannel;
    private final Socket socket;
    private final OutputStream socketOutput;
    private String name;
    private boolean closed;
    private Thread waitThread;

    /**
     * Create a connection via a TCP/IP socket.
     *
     * @param socket The socket to use for the connection.
     * @throws IOException          if an error occurs getting the streams for the connection.
     * @throws NullPointerException if socket is null
     */
    public SocketConnection(Socket socket) throws IOException {
        if (socket == null) {
            throw new NullPointerException();
        }
        this.socket = socket;
        this.socketChannel = Optional.empty();
        socketInput = socket.getInputStream();
        socketOutput = socket.getOutputStream();
    }

    /**
     * Create a connection via a TCP/IP socketChannel.
     *
     * @param socketChannel The socketChannel to use for the connection.
     * @throws NullPointerException if socketChannel is null
     */
    public SocketConnection(SocketChannel socketChannel) {
        this.socket = socketChannel.socket();
        this.socketChannel = Optional.of(socketChannel);
        socketInput = Channels.newInputStream(socketChannel);
        socketOutput = Channels.newOutputStream(socketChannel);
    }

    /**
     * Create a connection via a TCP/IP socket.
     *
     * @param host The host to which to try to connect to try and get a socket.
     * @param port The port on the host to which to connect to try and get a socket.
     * @throws IOException if an error occurs opening the socket.
     */
    public SocketConnection(String host, int port) throws IOException {
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
            socketChannel = Optional.of(SocketChannel.open(new InetSocketAddress(host, port)));
            socket = socketChannel.get().socket();
        } finally {
            if (jtSm != null) {
                jtSm.setAllowPropertiesAccess(prev);
            }
        }

        socketInput = Channels.newInputStream(socketChannel.get());
        socketOutput = Channels.newOutputStream(socketChannel.get());
    }

    private static String getHostName(InetAddress addr) {
        String s = addressCache.get(addr);
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
    public static ServerSocketChannel createServerSocketChannel(int port) throws IOException {
        return createServerSocketChannel(port, 50);
    }

    /**
     * Creates a ServerSocket instance in the environment where reading
     * of system properties is allowed.
     *
     * @param port    - port to bind
     * @param backlog - backlog
     * @return new created ServerSocket
     * @throws java.io.IOException - if ServerSocket is not created
     */
    public static synchronized ServerSocketChannel createServerSocketChannel(int port, int backlog)
            throws IOException {
        SecurityManager sm = System.getSecurityManager();
        JavaTestSecurityManager jtSm = null;
        boolean prev = false;
        if (sm != null && sm instanceof JavaTestSecurityManager) {
            jtSm = (JavaTestSecurityManager) sm;
            prev = jtSm.setAllowPropertiesAccess(true);
        }
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            // Ensure SO_REUSEADDR is false. (It is only needed if we're
            // using a fixed port.) The default setting for SO_REUSEADDR
            // is platform-specific, and Solaris has it on by default.
            serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.FALSE);
            serverSocketChannel.bind(new InetSocketAddress(port), backlog);
            return serverSocketChannel;
        } finally {
            if (jtSm != null) {
                jtSm.setAllowPropertiesAccess(prev);
            }
        }
    }

    @Override
    public String getName() {
        if (name == null) {
            StringBuilder sb = new StringBuilder(32);
            sb.append(getHostName(socket.getInetAddress()));
            sb.append(",port=");
            sb.append(socket.getPort());
            sb.append(",localport=");
            sb.append(socket.getLocalPort());
            name = sb.toString();
        }
        return name;
    }

    @Override
    public InputStream getInputStream() {
        return socketInput;
    }

    @Override
    public OutputStream getOutputStream() {
        return socketOutput;
    }

    @Override
    public synchronized void close() throws IOException {
        socket.close();
        if (socketChannel.isPresent()) {
            socketChannel.get().close();
        }
        socketInput.close();
        socketOutput.close();
        closed = true;

        if (waitThread != null) {
            waitThread.interrupt();
        }
    }

    @Override
    public synchronized boolean isClosed() {
        return closed;
    }

    @Override
    public void waitUntilClosed(int timeout) throws InterruptedException {
        synchronized (this) {
            waitThread = Thread.currentThread();
        }

        Timer.Timeable cb = () -> {
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
}
