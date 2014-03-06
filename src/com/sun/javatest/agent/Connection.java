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
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A Connection embodies the communication connection between the main JT Harness
 * test harness and the Javatest agent as used to run a test. It is a bidirectional
 * pair of byte streams used to communicate requests and results between the main
 * harness and the agent.
 * <p>
 * Connections are created by a {@link ConnectionFactory}.
 */
public interface Connection
{
    /**
     * Get a name for this connection.
     * @return          A short presentation string identifying this connection.
     */
    String getName();

    /**
     * Get the input stream of the connection.
     * @return an input stream
     */
    InputStream getInputStream();

    /**
     * Get the output stream of the connection.
     * @return an output stream
     */
    OutputStream getOutputStream();

    /**
     * Close the connection, including both the input stream and the output stream.
     * @throws IOException if there is an error closing the connection.
     */
    void close() throws IOException;

    /**
     * Check if the connection has been closed.
     * @return true if the connection has been closed.
     */
    boolean isClosed();

    /**
     * Wait until the connection has been closed from the other end,
     * perhaps in response to an in-band close request written to the
     * output stream.
     * @param timeout   A maximum time to wait for the close. The timeout
     *                  should be specified in milliseconds.
     * @throws InterruptedException if the thread is interrupted while waiting
     *                  for the connection to be closed.
     */
    void waitUntilClosed(int timeout) throws InterruptedException;
}
