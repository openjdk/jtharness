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

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.Vector;

import com.sun.javatest.Command;
import com.sun.javatest.Status;
import com.sun.javatest.Test;
import com.sun.javatest.util.DynamicArray;
import com.sun.javatest.util.Timer;
import com.sun.javatest.util.WriterStream;

/**
 * The means by which the tes harness executes requests on other machines.
 * Agents are typically started by using one of AgentMain, AgentFrame,
 * or AgentApplet. The requests themselves are made from the harness
 * via AgentManager, or a library class that uses AgentManager.
 *
 * @see AgentManager
 */

public class Agent implements Runnable {
    /**
     * An interface for observing activity on an agent.
     */
    public interface Observer {
        /**
         * Called when an agent's run method has been entered.
         * @param agent The agent being started.
         * @see Agent#run
         */
        void started(Agent agent);

        /**
         * Called if an agent's run method has trouble accepting a connection.
         * @param agent The agent trying to open a connection
         * @param e     The exception that occurred
         * @see Agent#run
         */
        void errorOpeningConnection(Agent agent, Exception e);

        /**
         * Called when an agent's run method completed.  Normally, the method will
         * run until an error occurs, or until the thread is interrupted or stopped.
         * @param agent The agent which has completed the work.
         * @see Agent#run
         */
        void finished(Agent agent);


        /**
         * Called when an agent has successfully opened a connection to service
         * a request.
         * @param agent The agent which opened the connection.
         * @param c     The connection which was opened.
         */
        void openedConnection(Agent agent, Connection c);

        /**
         * Called when an agent is about to execute a request to execute a Test object.
         * @param agent         The agent about to do the work.
         * @param c             The connection to the client requesting the work.
         * @param tag           A tag identifying the work.
         * @param className     The name of the class to be run
         * @param args          Arguments for the class to be run.
         */
        void execTest(Agent agent, Connection c, String tag, String className, String[] args);

        /**
         * Called when am agent is about to execute a request to execute a Command object.
         * @param agent         The agent about to do the work.
         * @param c             The connection to the client requesting the work.
         * @param tag           A tag identifying the work.
         * @param className     The name of the class to be run
         * @param args          Arguments for the class to be run.
         */
        void execCommand(Agent agent, Connection c, String tag, String className, String[] args);

        /**
         * Called when the agent is about to execute a request to execute a main program.
         * @param agent         The agent about to do the work.
         * @param c             The connection to the client requesting the work.
         * @param tag           A tag identifying the work.
         * @param className     The name of the class to be run
         * @param args          Arguments for the class to be run.
         */
        void execMain(Agent agent, Connection c, String tag, String className, String[] args);

        /**
         * Called when the agent has successfully completed a request to execute a class.
         * @param agent         The agent that performed the work.
         * @param c             The connection to the client requesting the work.
         * @param result        The result status of the work
         */
        void result(Agent agent, Connection c, Status result);

        /**
         * Called when the agent has failed to execute a class,
         * or has failed to report the results back to the agent requesting the action,
         * because an exception occurred.
         * @param agent         The agent that performed the work.
         * @param c             The connection to the client requesting the work.
         * @param e             The exception that occurred.
         */
        void exception(Agent agent, Connection c, Throwable e);

        /**
         * Called when the agent has completed all processing of the request
         * that arrived on a particular connection.
         * @param agent         The agent that performed the work.
         * @param c             The connection to the client requesting the work.
         */
        void completed(Agent agent, Connection c);
    }

    /**
     * Create an agent that connects to clients using a specified connection factory.
     * @param connectionFactory The factory from which to get connections to clients.
     * @param concurrency       The number of simultaneous requests to be accepted.
     */
    public Agent(ConnectionFactory connectionFactory, int concurrency) {
        if (!isValidConcurrency(concurrency))
            throw new IllegalArgumentException("bad concurrency: " + concurrency);

        this.connectionFactory = connectionFactory;
        maxThreads = concurrency;
    }

    /**
     * Set the delay to wait after failing to open a connection amd before trying again.
     * @param delay     The number of seconds to wait before attempting to open a new connection.
     * @see #getRetryDelay
     */
    public void setRetryDelay(int delay) {
        if (delay <= 0)
            throw new IllegalArgumentException("invalid delay");

        retryDelay = delay;
    }

    /**
     * Get the delay to wait after failing to open a connection and before trying again.
     * @return the number of seconds to wait before attempting to open a new connection.
     * @see #setRetryDelay
     */
    public int getRetryDelay() {
        return retryDelay;
    }

    /**
     * Set the translation map to be used to localize incoming requests.
     * If an incoming request permits it, occurrences of certain substrings
     * will be replaced with corresponding local variants. This is typically
     * used to remap file systems which might have different mount points
     * on different systems, but has been superceded by the environment's
     * map substitution facility.
     * @param map       The translation map to be used.
     */
    public synchronized void setMap(Map map) {
        this.map = map;
        if (tracing) {
            if (map == null)
                traceOut.println("set map null");
            else {
                traceOut.println("set map:");
                map.setTracing(tracing, traceOut);
                for (Enumeration e = map.enumerate(); e.hasMoreElements(); ) {
                    String[] entry = (String[])(e.nextElement());
                    traceOut.println("map-from: " + entry[0]);
                    traceOut.println("map-to:   " + entry[1]);
                }
                traceOut.println("end of map");
            }
        }
    }


    /**
     * Enable or disable tracing for agent activities.
     * It is best to call this as early as possible - objects created by
     * this class will inherit the setting as they are created/set.
     * @param state True if tracing output should be provided.
     */
    public void setTracing(boolean state) {
        tracing = state;
    }

    //--------------------------------------------------------------------------

    /**
     * Add an observer to monitor the progress of the TestFinder.
     * @param o the observer
     */
    public void addObserver(Observer o) {
        notifier.addObserver(o);
    }

    /**
     * Remove an observer form the set currently monitoring the progress
     * of the TestFinder.
     * @param o the observer
     */
    public void removeObserver(Observer o) {
        notifier.removeObserver(o);
    }

    //--------------------------------------------------------------------------

    /**
     * Run the agent. Since an Agent is {@link Runnable runnable}, this method
     * will typically be called on a separate thread.
     */
    public synchronized void run() {
        if (mainThread != null)
            throw new IllegalStateException("Agent already running");

        mainThread = Thread.currentThread();

        timer = new Timer();
        closing = false;

        try {
            if (tracing)
                traceOut.println("AGENT STARTED, maxThreads=" + maxThreads);

            notifier.started();

            if (maxThreads <= 0)
                // self defense: stops infinite wait, but the test should
                // have already been done in the constructor and an argument
                // thrown.
                return;


            while (!closing) {
                while (threads.size() < maxThreads && !closing) {
                    Thread t = new Thread(new Runnable() {
                        public void run() {
                            Thread curr = Thread.currentThread();
                            if (tracing)
                                traceOut.println("THREAD " + curr.getName() + " STARTED " + getClass().getName());

                            try {
                                handleRequestsUntilClosed();
                            } catch (InterruptedException e) {
                            } finally {
                                synchronized (Agent.this) {
                                    threads.removeElement(curr);
                                    Agent.this.notifyAll();
                                }
                                if (tracing)
                                    traceOut.println("THREAD " + curr.getName() + " EXITING");
                            }
                        }
                    });
                    t.setName("Agent" + nextThreadNum());
                    int currPrio = Thread.currentThread().getPriority();
                    int slvPrio = ((currPrio + Thread.MIN_PRIORITY) / 2);
                    t.setPriority(slvPrio);
                    t.start();
                    threads.addElement(t);
                }
                wait();
            }
        } catch (InterruptedException e) {
            try {
                close();
            } catch (InterruptedException ignore) {
            }
        } finally {
            timer.finished();
            notifier.finished();
            if (tracing)
                traceOut.println("AGENT EXITING");
            mainThread = null;
        }
    }

    /**
     * Interrupt this agent. The thread running the {@link #run run} method
     * will be interrupted.
     */
    public synchronized void interrupt() {
        if (mainThread != null)
            mainThread.interrupt();
    }

    /**
     * Close this agent. Any requests in progress will be allowed to complete,
     * and no new requests will be accepted.
     * @throws InterruptedException if this thread is interrupted while waiting
     * for outstanding requests to complete.
     */
    public synchronized void close() throws InterruptedException {
        closing = true;  // will prevent new tasks from being created

        // interrupt any threads that are running
        for (int i = 0; i < threads.size(); i++) {
            Thread t = (Thread)(threads.elementAt(i));
            if (tracing)
                traceOut.println("INTERRUPTING THREAD " + t.getName());
            t.interrupt();
        }

        // wait 3s for shutdown
        traceOut.println("WAITING 3s FOR THREADS TO CLEANUP");
        Thread.currentThread().sleep(3000);

        // close any tasks that are running
        for (int i = 0; i < tasks.size(); i++) {
            Task t = (Task)(tasks.elementAt(i));
            if (tracing) {
                Connection c = t.connection; // maybe null; if it is, task is already closing
                traceOut.println("CLOSING TASK " + (c == null ? "[unknown]" : c.getName()));
            }
            t.close();
        }

        try {
            if (tracing)
                traceOut.println("CLOSING CONNECTION FACTORY");

            connectionFactory.close();
        } catch (ConnectionFactory.Fault ignore) {
        }

        // allow main loop to exit
        notifyAll();

        if (tracing)
            traceOut.println("WAITING FOR TASKS TO EXIT");

        // wait for tasks to go away
        while (tasks.size() > 0) {
            wait();
        }

        if (tracing)
            traceOut.println("CLOSED");
    }

    /**
     * Checks that given concurrency value belong to the certain range 1-256.
     * The highest value should be in sync with:
     * Parameters.ConcurrencyParameters.MAX_CONCURRENCY
     *
     * @param concurrency value to check
     * @return true, if concurrency is acceptable
     */
    static boolean isValidConcurrency(int concurrency) {
        return 1 <= concurrency && concurrency <= 256;
    }

    //----------------------------------------------------------------------------

    private void handleRequestsUntilClosed() throws InterruptedException {
        while (!closing) {
            try {
                // The call of nextConnection() will block until a connection is
                // open; this can be for an indefinite amount of time.
                // Therefore we must not hold the object lock while calling this routine.
                Connection connection = connectionFactory.nextConnection();

                Task t;
                synchronized (this) {
                    // Having opened a connection, we check that the agent has not been
                    // marked for shutdown before updating connection.
                    if (closing) {
                        closeIgnoreExceptions(connection);
                        return;
                    }

                    t = new Task(connection);
                    tasks.addElement(t);
                }

                try {
                    t.handleRequest();
                } finally {
                    synchronized (this) {
                        tasks.removeElement(t);
                    }
                }
            } catch (ConnectionFactory.Fault e) {
                notifier.errorOpeningConnection(e.getException());
                if (tracing)
                    traceOut.println("THREAD " + Thread.currentThread().getName() + " " + e);

                if (e.isFatal()) {
                    close();
                    return;
                } else {
                    int millis = 1000*min(5, getRetryDelay());
                    Thread.currentThread().sleep(millis);
                    continue;
                }
            }

        }
    }

    private static void closeIgnoreExceptions(Connection c) {
        try {
            c.close();
        } catch (IOException e) {
        }
    }

    private static final int min(int a, int b) {
        return (a < b ? a : b);
    }

    private class Notifier {
        public synchronized void addObserver(Observer o) {
            observers = (Observer[])(DynamicArray.append(observers, o));
        }

        public synchronized void removeObserver(Agent.Observer o) {
            observers = (Observer[])(DynamicArray.remove(observers, o));
        }

        public synchronized void started() {
            for (int i = 0; i < observers.length; i++)
                observers[i].started(Agent.this);
        }

        public synchronized void finished() {
            for (int i = 0; i < observers.length; i++)
                observers[i].finished(Agent.this);
        }

        public synchronized void openedConnection(Connection connection) {
            for (int i = 0; i < observers.length; i++)
                observers[i].openedConnection(Agent.this, connection);
        }

        public synchronized void errorOpeningConnection(Exception e) {
            for (int i = 0; i < observers.length; i++)
                observers[i].errorOpeningConnection(Agent.this, e);
        }

        public synchronized void execTest(Connection cconnection, String tag, String className, String[] args) {
            for (int i = 0; i < observers.length; i++)
                observers[i].execTest(Agent.this, cconnection, tag, className, args);
        }

        public synchronized void execCommand(Connection cconnection, String tag, String className, String[] args) {
            for (int i = 0; i < observers.length; i++)
                observers[i].execCommand(Agent.this, cconnection, tag, className, args);
        }

        public synchronized void execMain(Connection connection, String tag, String className, String[] args) {
            for (int i = 0; i < observers.length; i++)
                observers[i].execMain(Agent.this, connection, tag, className, args);
        }

        public synchronized void result(Connection connection, Status status) {
            for (int i = 0; i < observers.length; i++)
                observers[i].result(Agent.this, connection, status);
        }

        public synchronized void exception(Connection connection, Exception e) {
            for (int i = 0; i < observers.length; i++)
                observers[i].exception(Agent.this, connection, e);
        }

        public synchronized void completed(Connection connection) {
            for (int i = 0; i < observers.length; i++)
                observers[i].completed(Agent.this, connection);
        }

        private Observer[] observers = new Observer[0];
    };

    private synchronized void setSystemStreams(Object owner, PrintStream out, PrintStream err)
    throws InterruptedException, SecurityException {
        if (owner == null)
            throw new NullPointerException();

        while (currSystemStreamOwner != null)
            wait();

        currSystemStreamOwner = owner;
        saveOut = System.out;
        saveErr = System.err;
        System.setOut(out);
        System.setErr(err);
    }

    private synchronized void resetSystemStreams(Object owner) throws SecurityException {
        if (owner == null)
            throw new NullPointerException();

        if (owner != currSystemStreamOwner)
            throw new IllegalStateException("expected: " + owner + " found: " + currSystemStreamOwner);

        currSystemStreamOwner = null;
        System.setOut(saveOut);
        System.setErr(saveErr);
        notifyAll();
    }

    private boolean closing;
    private Thread mainThread;
    private int maxThreads;
    private Vector threads = new Vector();
    private Vector tasks = new Vector();
    private Notifier notifier = new Notifier();
    private Object currSystemStreamOwner = null;
    private PrintStream saveOut;
    private PrintStream saveErr;

    /**
     * A flag to enable debug tracing of the operation of the agent.
     */
    protected boolean tracing = false;
    protected PrintStream traceOut = System.out;

    /**
     * The default time to wait after a failed attempt to open a connection,
     * and before trying again.
     * @see #setRetryDelay
     */
    public static final int DEFAULT_RETRY_DELAY = 5;
    private int retryDelay = DEFAULT_RETRY_DELAY;
    private ConnectionFactory connectionFactory;

    private Map map;
    private Timer timer;

    /* For autonumbering agent tasks. */
    private static int threadInitNumber;

    private static synchronized int nextThreadNum() {
        return threadInitNumber++;
    }

    // The following is used to ensure consistency between Agent and AgentManager
    static final short protocolVersion = 104;

    /**
     * The default port to which active agents will try and connect on a nominated host.
     */
    public static final int defaultActivePort = 1907;

    /**
     * The default port on which passive ports will listen for incoming connections.
     */
    public static final int defaultPassivePort = 1908;

    static final byte CLASS = (byte)'C';
    static final byte DATA = (byte)'D';
    static final byte LOG = (byte)'L';
    static final byte LOG_FLUSH = (byte)'l';
    static final byte REF = (byte)'R';
    static final byte REF_FLUSH = (byte)'r';
    static final byte STATUS = (byte)'S';

    static final String productName = "JT Harness Agent";
    static final String productVersion = "JTA_4.6";
    static final String productCopyright = "Copyright (c) 1996, 2014, Oracle and/or its affiliates. All rights reserved.";


    /**
     * Tasks handle the individual requests received by Agent.
     * They read the request from the connection, execute the request, which means
     * running the test class on behalf of the client, and any output from the
     * test class is written back to the client via the connection.
     */
    class Task {
        Task(Connection c) {
            if (c == null)
                throw new NullPointerException();
            connection = c;
        }

        public void handleRequest() throws ConnectionFactory.Fault {

            try {
                notifier.openedConnection(connection);

                if (tracing)
                    traceOut.println("REQUEST FROM " + connection.getName());

                in = new DataInputStream(connection.getInputStream());
                short pVer = in.readShort();
                if (pVer != protocolVersion)
                    throw new IOException("protocol mismatch;" +
                            " expected " + protocolVersion +
                            " received " + pVer);

                tag = in.readUTF();

                if (tracing)
                    traceOut.println("TAG IS `" + tag + "'");

                request = in.readUTF();

                if (tracing)
                    traceOut.println("REQUEST IS `" + request + "'");

                out = new DataOutputStream(new BufferedOutputStream(connection.getOutputStream()));

                Status status;

                if (request.equals("executeTest") || request.equals("executeCommand") || request.equals("executeMain") )
                    status = execute();
                else {
                    if (tracing)
                        traceOut.println("Unrecognized request for agent: `" + request + "'");
                    status = Status.error("Unrecognized request for agent: `" + request + "'");
                }

                if (tracing)
                    traceOut.println("RETURN " + status);

                notifier.result(connection, status);

                if (tracing)
                    traceOut.println("SEND STATUS");

                sendStatus(status);

                if (tracing)
                    traceOut.println("FLUSH");

                out.flush();

                if (tracing)
                    traceOut.println("AWAIT CLOSE");

                /*
                final Thread taskThread = Thread.currentThread();

                Timer.Timeable timeOutHandler = new Timer.Timeable() {
                    public void timeout() {
                        if (tracing)
                            traceOut.println("EOF TIMEOUT");
                        IOException e = new IOException("timeout communicating with AgentManager");
                        synchronized (Agent.this) {
                            for (int i = 0; i < observers.length; i++)
                                observers[i].exception(Agent.this, connection, e);
                        }
                        close(); // give up
                        taskThread.interrupt();
                        traceOut.println("EOF TIMEOUT CLOSED");
                    }
                };

                Timer.Entry te = timer.requestDelayedCallback(timeOutHandler, 5000);
                while (in.read() != -1) ;

                if (tracing)
                    traceOut.println("RECEIVED EOF");

                timer.cancel(te);

                notifier.completed(connection);
                 */

                connection.waitUntilClosed(5000);

                if (Thread.interrupted() && tracing) {
                    traceOut.println("Thread was interrupted - clearing interrupted status!");
                }

                if (connection.isClosed())
                    notifier.completed(connection);
                else
                    notifier.exception(connection, new IOException("timeout awaiting close from AgentManager"));
            } catch (InterruptedException e) {
                if (tracing) {
                    traceOut.println("Interrupted");
                }

                notifier.exception(connection, e);
            } catch (InterruptedIOException e) {
                if (tracing) {
                    traceOut.println("Interrupted (IO)");
                }

                notifier.exception(connection, e);
            } catch (IOException e) {
                if (tracing) {
                    traceOut.println("EXCEPTION IS `" + e + "'");
                    e.printStackTrace(traceOut);
                }

                notifier.exception(connection, e);
            } finally {
                close();
            }
        }

        private Status execute() throws IOException {
            String className = in.readUTF();

            if (tracing)
                traceOut.println("CLASSNAME: " + className);

            int n = in.readShort();

            if (tracing)
                traceOut.println("nArgs: " + n);

            String[] args = new String[n];
            for (int i = 0; i < args.length; i++) {
                args[i] = in.readUTF();
                if (tracing)
                    traceOut.println("arg[" + i + "]: " + args[i]);
            }

            boolean mapArgs = in.readBoolean();

            if (tracing)
                traceOut.println("mapArgs: " + mapArgs);

            boolean remoteClasses = in.readBoolean();

            if (tracing)
                traceOut.println("remoteClasses: " + remoteClasses);

            boolean sharedClassLoader = in.readBoolean();

            if (tracing) {
                traceOut.println("sharedClassLoader: " + sharedClassLoader);
            }

            byte guard = in.readByte();
            if (guard != 0)
                throw new IOException("data format error");

            if (map != null && mapArgs)
                map.map(args);

            PrintWriter testLog = new PrintWriter(new AgentWriter(LOG, this));
            PrintWriter testRef = new PrintWriter(new AgentWriter(REF, this));

            try {
                Class c;
                ClassLoader cl = null;
                if (remoteClasses) {
                    cl = getAgentClassLoader(sharedClassLoader);
                    c = cl.loadClass(className);
                } else
                    c = Class.forName(className);

                if (request.equals("executeTest")) {
                    return executeTest(c, args, testLog, testRef);
                } else if (request.equals("executeCommand")) {
                    return executeCommand(c, args, testLog, testRef, cl);
                } else if (request.equals("executeMain")) {
                    return executeMain(c, args, testLog, testRef);
                } else
                    return Status.error("Unrecognized request for agent: `" + request + "'");
            } catch (ClassCastException e) {
                if (tracing)
                    e.printStackTrace(traceOut);
                return Status.error("Can't execute class `" + className + "': required interface not found");
            } catch (ClassNotFoundException ex) {
                return Status.error("Can't find class `" + className + "'");
            } catch (IllegalAccessException ex) {
                return Status.error("Illegal access to class `" + className + "'");
            } catch (InstantiationException ex) {
                return Status.error("Can't instantiate class`" + className + "'");
            } catch (ThreadDeath e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace(testLog);
                return Status.error("Unexpected exception: " + e);
            } catch (Error e) {
                e.printStackTrace(testLog);
                return Status.error("Unexpected error: " + e);
            } catch (Throwable e) {
                e.printStackTrace(testLog);
                return Status.error("Unexpected throwable: " + e);
            } finally {
                // close the streams used by the test and write the test status back
                if (tracing)
                    traceOut.println("CLOSE TESTREF");

                testRef.close();

                if (tracing)
                    traceOut.println("CLOSE TESTLOG");

                testLog.close();
            }
        }

        private Status executeTest(Class c, String[] args,
                PrintWriter testLog, PrintWriter testRef)
                throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
            notifier.execTest(connection, tag, c.getName(), args);
            Test t = (Test)(c.newInstance());
            return t.run(args, testLog, testRef);
        }

        private Status executeCommand(Class c, String[] args,
                PrintWriter testLog, PrintWriter testRef,
                ClassLoader cl)
                throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
            notifier.execCommand(connection, tag, c.getName(), args);

            Command tc = (Command)(c.newInstance());
            tc.setClassLoader(cl);
            return tc.run(args, testLog, testRef);
        }

        private Status executeMain(Class c, String[] args,
                PrintWriter testLog, PrintWriter testRef)
                throws IOException, ClassNotFoundException, IllegalAccessException {
            notifier.execMain(connection, tag, c.getName(), args);

            PrintStream out = Deprecated.createPrintStream(new WriterStream(testRef));
            PrintStream err = Deprecated.createPrintStream(new WriterStream(testLog));
            try {
                setSystemStreams(this, out, err);
                Method main = c.getDeclaredMethod("main", new Class[] {String[].class});
                main.invoke(null, new Object[] {args});
                return Status.passed("OK");
            } catch (NoSuchMethodException e) {
                return Status.error("Can't find `public static void main(String[] args)' for `" + c.getName() + "'");
            } catch (InvocationTargetException e) {
                Throwable t = e.getTargetException();
                t.printStackTrace(err);
                return Status.failed(t.toString());
            } catch (InterruptedException e) {
                return Status.failed("interrupted while waiting for access to system streams");
            } finally {
                resetSystemStreams(this);
                out.flush();
                err.flush();
            }
        }

        /**
         * Close the task, abandoning any request in progress.
         */
        synchronized void close() {
            if (!connection.isClosed()) {
                closeIgnoreExceptions(connection);
                // don't nullify connections because handleRequest might still be using it
            }

            if (in != null) {
                try {
                    //System.err.println("closing in");
                    in.close();
                    in = null;
                } catch (IOException ignore) {
                }
            }

            if (out != null) {
                try {
                    //System.err.println("closing out");
                    out.close();
                    out = null;
                } catch (IOException ignore) {
                }
            }
        }

        /**
         * Send wrapped data back to the client.
         */
        synchronized void sendChars(byte type, char b[], int off, int len) throws IOException {
            out.write(type);
            out.writeUTF(new String(b, off, len));
            switch (type) {
                case LOG_FLUSH:
                case REF_FLUSH:
                    out.flush();
            }
        }

        /**
         * Send the final status back to the client.
         */
        private synchronized void sendStatus(Status s) throws IOException {
            out.write(STATUS);
            out.write((byte)s.getType());
            out.writeUTF(s.getReason());
        }

        /**
         * Get the bytecodes for a class
         */
        synchronized byte[] getClassData(String className) throws ClassNotFoundException {
            if (tracing)
                traceOut.println("REMOTE LOAD " + className);

            try {
                out.write(CLASS);
                out.writeUTF(className);
                out.flush();

                int size = in.readInt();
                if (size == 0)
                    throw new ClassNotFoundException(className);

                byte[] data = new byte[size];
                int offset = 0;
                while (offset < data.length) {
                    int n = in.read(data, offset, data.length - offset);
                    if (n == -1)
                        throw new ClassNotFoundException(className + ": EOF while reading class data");
                    else
                        offset += n;
                }

                //System.err.println(data.length);
                //for (int i = 0; i < min(10, data.length); i++) {
                //    System.err.print(data[i] + " ");
                //}
                //System.err.print(" ... ");
                //for (int i = max(0, data.length - 10); i < data.length; i++) {
                //    System.err.print(data[i] + " ");
                //}
                //System.err.println();

                return data;
            } catch (IOException e) {
                throw new ClassNotFoundException(className + ": " + e);
            }
        }

        /**
         * Get a resource
         */
        synchronized byte[] getResourceData(String resourceName) throws MissingResourceException, IOException {
            if (tracing)
                traceOut.println("REMOTE LOAD " + resourceName);

            out.write(DATA);
            out.writeUTF(resourceName);
            out.flush();

            int size = in.readInt();
            if (size == -1)
                throw new MissingResourceException(resourceName, null, resourceName);

            byte[] data = new byte[size];
            int offset = 0;
            while (offset < data.length) {
                int n = in.read(data, offset, data.length - offset);
                if (n == -1)
                    throw new IOException(resourceName + ": EOF while reading resource data");
                else
                    offset += n;
            }

            //System.err.println(data.length);
            //for (int i = 0; i < min(10, data.length); i++) {
            //    System.err.print(data[i] + " ");
            //}
            //System.err.print(" ... ");
            //for (int i = max(0, data.length - 10); i < data.length; i++) {
            //    System.err.print(data[i] + " ");
            //}
            //System.err.println();

            return data;
        }

        private ClassLoader getAgentClassLoader(boolean useSharedClassLoader)
                throws InstantiationException, IllegalAccessException {
            Class classLoaderClass;
            try {
                String s = getClass().getName();
                String pkg = s.substring(0, s.lastIndexOf('.'));
                classLoaderClass = Class.forName(pkg + ".AgentClassLoader2");
            } catch (Throwable t) {
                classLoaderClass = AgentClassLoader.class;
            }

            Class[] argTypes = {Task.class};
            if (useSharedClassLoader && factoryMethod == null) {
                try {
                    factoryMethod = classLoaderClass.getDeclaredMethod("getInstance", argTypes);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            if (classLoaderConstructor == null) {
                try {
                    classLoaderConstructor = classLoaderClass.getDeclaredConstructor(argTypes);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            try {
                Object[] args = {this};
                if (useSharedClassLoader && factoryMethod != null) {
                    return (ClassLoader) factoryMethod.invoke(null, args);
                } else {
                    return (ClassLoader) (classLoaderConstructor.newInstance(args));
                }
            } catch (InvocationTargetException e) {
                Throwable t = e.getTargetException();
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                } else if (t instanceof Error) {
                    throw (Error) t;
                } else {
                    throw new Error(e.toString());
                }
            }

        }

        private Connection connection;
        private DataInputStream in;
        private DataOutputStream out;
        private String tag;
        private String request;
    }

    private static Constructor classLoaderConstructor;
    private static Method factoryMethod = null;
}





/**
 * Stream passed to the class that is executed on behalf of the client.
 * Data written to the stream is buffered and eventually written back to th
 * client via the Task's sendChars method.
 */
class AgentWriter extends Writer {
    /**
     * Create a stream that sends its data back to the parent Task.
     * @arg type        A tag to pass back to parent.sendChars().
     * @arg parent      The parent object to which to pass the data written to the stream.
     */
    AgentWriter(byte type, Agent.Task parent) {
        this.type = type;
        this.parent = parent;
    }

    /**
     * Writes a character. This method will block until the character
     * is actually written.
     * @param ch        the char
     * @exception IOException If an I/O error has occurred.
     */
    public synchronized void write(int ch) throws IOException {
        buf[count++] = (char)ch;
        if (count == buf.length) {
            parent.sendChars(type, buf, 0, count);
            count = 0;
        }
    }

    /**
     * Writes an array of characters. This method will block until the
     * characters are actually written.
     * @param c the data to be written
     * @exception IOException If an I/O error has occurred.
     */
    public void write(char c[]) throws IOException {
        write(c, 0, c.length);
    }

    /**
     * Writes a sub array of characters.
     * @param c the data to be written
     * @param off       the start offset in the data
     * @param len       the number of bytes that are written
     * @exception IOException If an I/O error has occurred.
     */
    public synchronized void write(char c[], int off, int len) throws IOException {
        if (len < buf.length - count) {
            // there is room for the bytes in the current buffer
            System.arraycopy(c, off, buf, count, len);
            count += len;
        } else {
            // not room in the current buffer, so flush it
            flush();
            if (len < buf.length) {
                // there is _now_ enough room in the current buffer, so use it
                System.arraycopy(c, off, buf, count, len);
                count += len;
            } else {
                // current buffer not big enough; send data directly
                parent.sendChars(type, c, off, len);
            }
        }
    }

    /**
     * Flushes the stream. This will write any buffered
     * output bytes.
     * @exception IOException If an I/O error has occurred.
     */
    public synchronized void flush() throws IOException {
        if (count > 0) {
            switch (type) {
                case Agent.LOG: type = Agent.LOG_FLUSH; break;
                case Agent.REF: type = Agent.REF_FLUSH; break;
            }
            parent.sendChars(type, buf, 0, count);
            count = 0;
        }
    }

    /**
     * Closes the stream. This method must be called
     * to release any resources associated with the
     * stream.
     * @exception IOException If an I/O error has occurred.
     */
    public void close() throws IOException {
        flush();
    }

    private byte type;
    private Agent.Task parent;
    private char[] buf = new char[1024];
    private int count = 0;
}
