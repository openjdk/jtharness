/*
 * $Id$
 *
 * Copyright (c) 1996, 2009, Oracle and/or its affiliates. All rights reserved.
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

/**
 * A ConnectionFactory provides a source of {@link Connection connections}
 * between the main JT Harness test harness and the JT Harness Agent. A fresh
 * new connection is created for each test that is executed by the agent.
 */
public interface ConnectionFactory
{
    /**
     * This exception is used to forward exception that might occur when
     * using the connection factory.
     */
    public static class Fault extends Exception {
        /**
         * Create a Fault.
         * @param e     An exception to be forwarded by this fault.
         * @param fatal Indicates whether the exception is fatal or not.
         *              For example, an exception caused by a temporary lack of resources
         *              would not be fatal, whereas an exception caused by a naming error
         *              would be fatal.
         */
        public Fault(Exception e, boolean fatal) {
            super(e.toString());
            this.exception = e;
            this.fatal = fatal;
        }

        /**
         * Get the exception being forward by this Fault.
         * @return The exception being forwarded.
         */
        public Exception getException() {
            return exception;
        }

        /**
         * Check whether this Fault is fatal or not.  If it is, there is little
         * point retrying the operation that threw the Fault.
         * @return true if this Fault is fatal.
         */
        public boolean isFatal() {
            return fatal;
        }

        private Exception exception;
        private boolean fatal;
    }

    /**
     * Create a new connection.
     * @return A new connection.
     * @throws ConnectionFactory.Fault if there is a problem creating the connection
     */
    Connection nextConnection() throws Fault;

    /**
     * Close the connection factory, releasing any resources it may be using.
     * @throws ConnectionFactory.Fault if a problem occurs while closing the factory.
     */
    void close() throws Fault;
}
