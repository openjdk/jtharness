/*
 * $Id$
 *
 * Copyright (c) 1996, 2014, Oracle and/or its affiliates. All rights reserved.
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


import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Enumeration;
import java.util.Vector;

import com.sun.javatest.util.DynamicArray;
import java.io.InterruptedIOException;

/**
 * A holding area in which to keep incoming requests from active agents
 * until they are required.
 */
public class ActiveAgentPool
{
    /**
     * An exception which is thrown when no agent is available for use.
     */
    public static class NoAgentException extends Exception
    {
        /**
         * Create an exception to indicate that no agent is available for use.
         * @param msg A string giving additional details.
         */
        public NoAgentException(String msg) {
            super(msg);
        }
    }

    /**
     * An Observer class to monitor activity of the active agent pool.
     */
    public static interface Observer {
        /**
         * Called when a connection to an agent is added to the active agent pool.
         * @param c The connection that has been added to the pool.
         */
        void addedToPool(Connection c);

        /**
         * Called when a connection to an agent is removed from the active agent pool,
         * because it is about to be used to handle a task.
         * @param c The connection that has been removed from the pool.
         */
        void removedFromPool(Connection c);
    }

    //--------------------------------------------------------------------------

    /**
     * An entry requesting an active agent that is available for
     * use.
     */
    class Entry implements Connection {
        Entry(Socket socket) throws IOException {
            this.socket = socket;
            socketInput = socket.getInputStream();
            socketOutput = socket.getOutputStream();
        }

        public String getName() {
            if (name == null) {
                StringBuffer sb = new StringBuffer(32);
                sb.append(socket.getInetAddress().getHostName());
                sb.append(",port=");
                sb.append(socket.getPort());
                sb.append(",localport=");
                sb.append(socket.getLocalPort());
                name = sb.toString();
            }
            return name;
        }

        public synchronized InputStream getInputStream() {
            // If there is no read outstanding in the watcher thread and
            // no buffered data available take the fast way out and simply
            // use the real socket stream.
            if (!reading && data == null)
                return socketInput;

            // If there is a read outstanding in the watcher  thread, or if there
            // is already buffered data available, create a stream to return that
            // data first.
            return new InputStream() {
                public int read() throws IOException {
                    // don't bother to optimize method this because stream should
                    // be wrapped in a BufferedInputStream
                    byte[] b = new byte[1];
                    int n = read(b);
                    if (n == -1) {
                        return -1;
                    }
                    else {
                        n = 0xFF & b[0];
                        return n;
                    }
                }
                public int read(byte[] buffer, int offset, int count) throws IOException {
                    if (count == 0) // we ought to check
                        return 0;

                    try {
                        // if the watcher thread has a read outstanding, wait for it to
                        // complete
                        waitWhileReading();
//                  }
//                  catch (InterruptedException ignore) {
//                  }
//
//                        if (data == null) {
//                            // no data available: must have been used already;
//                            // simply delegate to socketInput
//                            return socketInput.read(buffer, offset, count);
//                        }
                        if (data == null) {
                            return new InterruptableReader().read(buffer, offset, count);
                        }
                    }
                    catch (InterruptedException ie) {
                        InterruptedIOException iio =
                                new InterruptedIOException("Test execution timeout");
                        iio.fillInStackTrace();
                        throw iio;
                    }
                    try {
                        if (data instanceof Integer) {
                            int i = ((Integer)data).intValue();
                            if (i == -1)
                                return -1;
                            else {
                                buffer[offset] = (byte)i;
                                return 1;
                            }
                        }
                        else {
                            IOException e = (IOException)data;
                            e.fillInStackTrace();
                            throw e;
                        }
                    }
                    finally {
                        data = null;
                    }
                }
                public void close() throws IOException {
                    socketInput.close();
                }
            };
        }

        /*
         * This class made to read form socket input stream from separate thread.
         * Thread, from which reading invokes (let's call it 'main'), waits
         * before end of reading from socket.
         * Waiting allows harness to interrupt 'main' thread and thus manage
         * timeout situation correctly.
         * The same thing is made for passive agent.
         * See <code>PassiveConnectionFactory.nextConnection()</code> and
         * <code>AgentManager.connectToPassiveAgent()</code> methods where
         * InterruptableSocketConnection used instead of usual SocketConnection.
         */
        private class InterruptableReader {
            private IOException ioe;
            private int n;

            public int read(byte[] buffer, int offset, int count)
                    throws IOException, InterruptedException {
                synchronized (Entry.this) {
                    ioe = null;
                    n = -1;

                    readInThread(buffer, offset, count);
                    waitWhileReading();

                    if (ioe != null) {
                        throw ioe;
                    }

                    return n;
                }
            }
            private void readInThread(byte[] buffer, int offset, int count) {
                final byte[] b = buffer;
                final int o = offset;
                final int c = count;

                Thread reader = new Thread() {
                    public void run() {
                        try {
                            n = socketInput.read(b, o, c);
                        }
                        catch (IOException io) {
                            ioe = io;
                        }
                        finally {
                            synchronized(Entry.this) {
                                reading = false;
                                Entry.this.notifyAll();
                            }
                        }
                    }
                };
                reading = true;
                reader.start();
            }
        }

        public OutputStream getOutputStream() {
            return socketOutput;
        }

        public synchronized void close() throws IOException {
            socketInput.close();
            socketOutput.close();
            closed = true;
            notifyAll();
        }

        public synchronized boolean isClosed() {
            return closed;
        }

        public synchronized void waitUntilClosed(int timeout) throws InterruptedException {
            long now = System.currentTimeMillis();
            long end = now + timeout;
            while (now < end && !closed) {
                wait(end - now);
                now = System.currentTimeMillis();
            }
        }

        void readAhead() {
            synchronized (this) {
                if (!entries.contains(this))
                    // if this entry has already been removed from the agent pool,
                    // there is no need to monitor the socket, so exit without reading.
                    // This is an optimization only; the entry could be being removed
                    // right now, but the synchronized block we are in will handle
                    // everything OK.
                    return;

                // mark this object as busy doing a read; other synchronized methods
                // (ie getInputStream()) should take this into account
                reading = true;
            }

            // initiate a blocking read call on the socket, in the hope of being
            // notified if the socket gets closed prematurely. If it does
            // (i.e. if the read terminates while the entry is still in the pool),
            // the entry is removed from the pool and the socket closed.
            // Otherwise, if the entry is removed from the pool while the read is blocked,
            // then when the read terminates the data will be saved for use by the
            // new owner (via getInputStream), and the thread will be marked as no
            // longer doing a read.
            try {
                data = new Integer(socketInput.read());
            }
            catch (IOException e) {
                data = e;
            }
            finally {
                synchronized (this) {
                    boolean ok = entries.remove(this);
                    if (ok)
                        // The read has unblocked prematurely and no one else
                        // owns the entry (since we managed to remove it ourselves.
                        // Drop the socket.
                        closeNoExceptions(this);

                    reading = false;
                    notifyAll();
                }
            }
        }

        private synchronized void waitWhileReading() throws InterruptedException {
            while (reading)
                wait();
        }

        private final Socket socket;
        private InputStream socketInput;
        private OutputStream socketOutput;
        private String name;
        private boolean reading;
        private Object data;
        private boolean closed;
    }


    class Entries {
        synchronized boolean contains(Entry e) {
            return v.contains(e);
        }

        synchronized Enumeration elements() {
            return ((Vector)(v.clone())).elements();
        }

        synchronized void add(final Entry e) {
            v.addElement(e);
            notifyAddedToPool(e);
            notifyAll();
            Runnable r = new Runnable() {
                public void run() {
                    e.readAhead();
                }
            };
            Thread t = new Thread(r, "ActiveAgentPool.EntryWatcher" + entryWatcherCount++);
            t.start();
        }

        synchronized boolean remove(Entry e) {
            if (v.contains(e)) {
                v.removeElement(e);
                notifyRemovedFromPool(e);
                return true;
            }
            else
                return false;
        }

        synchronized Entry next() {
            Entry e = null;
            if (v.size() > 0) {
                e = (Entry)(v.elementAt(0));
                v.removeElementAt(0);
                notifyRemovedFromPool(e);
            }
            return e;
        }

        synchronized Entry next(int timeout) throws InterruptedException {
            long end = System.currentTimeMillis() + timeout;
            for (long t = timeout; t > 0;  t = end - System.currentTimeMillis()) {
                if (v.size() == 0)
                    wait(t);

                Entry e = next();
                if (e != null)
                    return e;
            }
            return null;
        }

        synchronized void addObserver(Observer o) {
            observers = (Observer[])(DynamicArray.append(observers, o));
        }


        synchronized void deleteObserver(Observer o) {
            observers = (Observer[])(DynamicArray.remove(observers, o));
        }

        private synchronized void notifyAddedToPool(Entry e) {
            for (int i = 0; i < observers.length; i++) {
                observers[i].addedToPool(e);
            }
        }

        private synchronized void notifyRemovedFromPool(Entry e) {
            for (int i = 0; i < observers.length; i++) {
                observers[i].removedFromPool(e);
            }
        }

        private Vector v = new Vector();
        private Observer[] observers = new Observer[0];
    }

    /**
     * Listen for requests from active agents. Active agents announce their
     * willingness to work on behalf of a harness by contacting the harness
     * on a nominated port.  When a agent contacts the harness, it is put in
     * a pool to be used when agent clients request an unspecified agent.
     * @param port      The port on which to listen for agents.
     * @param timeout   The maximum time to wait for a agent to contact the
     *                  harness when one is needed. The timeout should be
     *                  in milliseconds.
     * @throws IOException if there a problems with any sockets
     *                  while performing this operation.
     */
    public synchronized void listen(int port, int timeout) throws IOException {
        setListening(false);
        setPort(port);
        setTimeout(timeout);
        setListening(true);
    }

    /**
     * Get the port currently being used to listen for requests from active agents.
     * @return The port being used, or Agent.defaultActivePort if no agent pool
     * has been started.
     * @see #setPort
     */
    public synchronized int getPort()  {
        return (port == 0 && serverSocket != null ?
                serverSocket.getLocalPort() : port);
    }


    /**
     * Set the port currently to be used to listen for requests from active agents.
     * @param port the port to be used
     * @see #getPort
     */
    public synchronized void setPort(int port) {
        this.port = port; // takes effect on next setListening(true);
    }

    /**
     * Get the timeout being used when waiting for requests from active agents.
     * @return The timeout being used, in milliseconds, or 0 if no agent pool
     * has been started.
     * @see #setTimeout
     */
    public synchronized int getTimeout()  {
        return timeout;
    }


    /**
     * Set the timeout to be used when waiting for requests from active agents.
     * @param timeout Ehe timeout, in milliseconds, to be used.
     * @see #getTimeout
     */
    public synchronized void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Check whether the pool is currently listening for incoming requests.
     * @return true if the pool is currently listening
     * @see #setListening
     */
    public synchronized boolean isListening() {
        return (serverSocket != null);
    }

    /**
     * Set whether or not the pool should be listening for incoming requests,
     * on the appropriate port.
     * If the pool is already in the appropriate state, this method has no effect.
     * @param listen Set to true to ensure the pool is listening for incoming requests,
     *                  and false otherwise.
     * @throws IOException if any problems occur while opening or closing the
     *                  socket on which the pool is listening for requests.
     * @see #isListening
     */
    public synchronized void setListening(boolean listen) throws IOException {
        if (debug)
            new Exception("ActiveAgentPool.setListening " + listen + ",port=" + port).printStackTrace(System.err);

        if (listen) {
            if (serverSocket != null) {
                if (port == 0 || serverSocket.getLocalPort() == port)
                    return;
                else
                    closeNoExceptions(serverSocket);
            }

            serverSocket = SocketConnection.createServerSocket(port);

            Runnable r = new Runnable() {
                public void run() {
                    acceptRequests();
                }
            };
            Thread worker = new Thread(r, "ActiveAgentPool" + counter++);
            worker.start();
            // could synchronize (wait()) with run() here
            // if it should be really necessary
        }
        else {
            if (serverSocket != null)
                serverSocket.close();
            serverSocket = null;
            // flush the agents that have already registered
            Entry e;
            while ((e = entries.next()) != null)
                closeNoExceptions(e);
        }
    }

    Entry nextAgent() throws NoAgentException, InterruptedException {
        if (!isListening())
            throw new NoAgentException("AgentPool not listening");
        Entry e = entries.next(timeout);
        if (e != null)
            return e;

        throw new NoAgentException("Timeout waiting for agent to become available");
    }

    private void acceptRequests() {
        ServerSocket ss;
        // warning: serverSocket can be mutated by other methods, but we
        // don't want to do the accept call in a synchronized block;
        // after the accept call, we make sure that serverSocket is still
        // what we think it is--if not, this specific thread instance is
        // not longer current or required
        synchronized (this) {
            ss = serverSocket;
            // could synchronize (notify()) with setListening() here
            // if it should be really necessary
        }

        try {
            int errors = 0;

            while (errors < MAX_ERRORS) {
                try {

                    // wait for connection or exception, whichever comes first
                    Socket s = ss.accept();

                    // got connection: make sure we still want it,
                    // and if so, add it to pool and notify interested parties
                    synchronized (this) {
                        if (ss == serverSocket)
                            entries.add(new Entry(s));
                        else {
                            closeNoExceptions(s);
                            return;
                        }

                    }

                    if (errors > 0)
                        errors--; // let #errors decay with each successful open
                }
                catch (IOException e) {
                    synchronized (this) {
                        if (ss != serverSocket)
                            return;
                    }

                    // perhaps need a better reporting channel here
                    System.err.println("error opening socket for remote socket pool");
                    System.err.println(e.getMessage());
                    errors++;
                }
            }
            // perhaps need a better reporting channel here
            System.err.println("too many errors opening socket for remote socket pool");
            System.err.println("server thread exiting");

            synchronized (this) {
                if (serverSocket == ss)
                    serverSocket = null;
            }
        }
        finally {
            closeNoExceptions(ss);
        }

    }

    /**
     * Get an enumeration of the entries currently in the active agent pool.
     */
    Enumeration elements() {
        return entries.elements();
    }

    /**
     * Add an observer to monitor events.
     * @param o The observer to be added.
     */
    public void addObserver(Observer o) {
        entries.addObserver(o);
    }


    /**
     * Remove an observer that had been previously registered to monitor events.
     * @param o The observer to be removed..
     */
    public void deleteObserver(Observer o) {
        entries.deleteObserver(o);
    }

    private void closeNoExceptions(Entry e) {
        try {
            e.close();
        }
        catch (IOException ignore) {
        }
    }

    private void closeNoExceptions(Socket s) {
        try {
            s.close();
        }
        catch (IOException ignore) {
        }
    }

    private void closeNoExceptions(ServerSocket ss) {
        try {
            ss.close();
        }
        catch (IOException ignore) {
        }
    }

    private Thread worker;
    private int counter;
    private Entries entries = new Entries();
    private ServerSocket serverSocket;
    private int timeout = 3*60*1000;  // 3 minutes
    private int port = Agent.defaultActivePort;
    private final int MAX_ERRORS = 10;
    private static int entryWatcherCount;
    private static boolean debug = Boolean.getBoolean("debug.ActiveAgentPool");
}
