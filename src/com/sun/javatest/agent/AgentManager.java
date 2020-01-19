/*
 * $Id$
 *
 * Copyright (c) 1996, 2018, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.Status;
import com.sun.javatest.util.DynamicArray;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Access to the facilities provided by JT Harness agents.
 * Agents provide the ability to run remote and distributed tests.
 * The AgentManager provides a single point of control for creating
 * connections to agents, for managing a pool of available active agents,
 * and for registering observers for interesting events.
 **/
public class AgentManager {

    //--------------------------------------------------------------------------

    private static final AgentManager theManager = new AgentManager();
    private static final int PASSIVE_AGENT_RETRY_LIMIT = 12;
    private Observer[] observers = new Observer[0];

    //--------------------------------------------------------------------------
    private ActiveAgentPool pool = new ActiveAgentPool();

    // private, so others can't create additional managers
    private AgentManager() {
    }

    /**
     * Access the one single instance of the AgentManager.
     *
     * @return The one AgentManager.
     */
    public static AgentManager access() {
        return theManager;
    }

    /**
     * Add an observer to monitor events.
     *
     * @param o The observer to be added.
     * @see #removeObserver
     */
    public synchronized void addObserver(Observer o) {
        observers = DynamicArray.append(observers, o);
    }

    /**
     * Remove an observer that had been previously registered to monitor events.
     *
     * @param o The observer to be removed.
     */
    public synchronized void removeObserver(Observer o) {
        observers = DynamicArray.remove(observers, o);
    }

    private synchronized void notifyStarted(Connection connection,
                                            String tag, String request, String executable, String[] args,
                                            boolean localizeArgs) {
        for (Observer observer : observers) {
            observer.started(connection, tag, request, executable, args, localizeArgs);
        }
    }

    //--------------------------------------------------------------------------

    private synchronized void notifyFinished(Connection connection, Status status) {
        for (Observer observer : observers) {
            observer.finished(connection, status);
        }
    }

    /**
     * Get the active agent pool, which is a holding area for
     * agents that have registered themselves as available for use
     * by the test harness.
     *
     * @return the active agent pool
     */
    public ActiveAgentPool getActiveAgentPool() {
        return pool;
    }

    //--------------------------------------------------------------------------

    /**
     * Create a task that will connect with an agent via a given connection.
     *
     * @param c The connection to use to communicate with the agent.
     * @return a task object that can be used to initiate work on an agent
     */
    public Task connect(Connection c) {
        return new Task(c);
    }

    /**
     * Create a task that will connect to the next available active agent
     * that is available in a central pool.
     *
     * @return a task object that can be used to initiate work on an agent
     * @throws ActiveAgentPool.NoAgentException if the pool has not been
     *                                          initialized, or if there is still no agent after
     *                                          a timeout period specified when the pool was initialized.
     * @throws InterruptedException             if this thread is interrupted while waiting
     *                                          for an agent to become available in the active agent pool.
     * @throws IOException                      if a problem occurs establishing the connection
     */
    public Task connectToActiveAgent() throws ActiveAgentPool.NoAgentException, InterruptedException, IOException {
        return connect(pool.nextAgent());
    }

    /**
     * Create a task that will connect to a passive agent running on a
     * nominated host, and which is listening on the default port.
     *
     * @param host The host on which the agent should be running.
     * @return a task object that can be used to initiate work on an agent
     * @throws IOException          is there is a problem connecting to the agent
     * @throws NullPointerException if host is null
     */
    public Task connectToPassiveAgent(String host) throws IOException {
        if (host == null) {
            throw new NullPointerException();
        }

        return connectToPassiveAgent(host, Agent.defaultPassivePort);
    }


    /**
     * Create a connection to a passive agent running on a nominated host,
     * and which is listening on a specified port.
     *
     * @param host The host on which the agent should be running.
     * @param port The port on which the agent should be listening.
     * @return a task object that can be used to initiate work on an agent
     * @throws IOException          is there is a problem connecting to the agent
     * @throws NullPointerException if host is null
     */
    public Task connectToPassiveAgent(String host, int port) throws IOException {
        if (host == null) {
            throw new NullPointerException();
        }

        for (int i = 0; ; i++) {
            try {
//              return connect(new SocketConnection(host, port));
                return connect(new InterruptableSocketConnection(host, port));
            } catch (ConnectException e) {
                if (i == PASSIVE_AGENT_RETRY_LIMIT) {
                    throw e;
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    /**
     * An Observer class to monitor Agent activity.
     */
    public interface Observer {
        /**
         * Called when a task starts a request.
         *
         * @param connection   The connection used to communicate with the agent.
         * @param tag          A tag used to identify the request.
         * @param request      The type of the request.
         * @param executable   The class to be executed.
         * @param args         Arguments to be passed to the class to be executed.
         * @param localizeArgs Whether or not to localize the args remotely, using the
         *                     agent's map facility.
         */
        void started(Connection connection,
                     String tag, String request, String executable, String[] args,
                     boolean localizeArgs);

        /**
         * Called when a task completes a request.
         *
         * @param connection The connection used to communicate with the agent.
         * @param status     The outcome of the request.
         */
        void finished(Connection connection, Status status);
    }


    //--------------------------------------------------------------------------

    /**
     * A Task provides the ability to do work remotely on an agent.
     */
    public class Task {

        private Connection connection;
        private DataInputStream in;
        private DataOutputStream out;
        private File[] classPath;
        private boolean sharedCl;
        private int timeout = 0;
        private Hashtable<File, ZipFile> zips = new Hashtable<>();

        /**
         * Create a connection to a agent retrieved from the agent pool.
         *
         * @param c The connection with which to communicate to the agent.
         */
        Task(Connection c) {
            connection = c;
            in = new DataInputStream(new BufferedInputStream(c.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(c.getOutputStream()));
        }

        /**
         * Get the connection being used for this task.
         *
         * @return the connection to the remote agent.
         */
        public Connection getConnection() {
            return connection;
        }

        /**
         * Get the classpath to be used when loading classes on behalf of the
         * remote agent.
         *
         * @return An array of files and directories in which to look for classes to
         * be given to the remote agent.
         * @see #setClassPath(String)
         * @see #setClassPath(File[])
         */
        public File[] getClassPath() {
            return classPath;
        }

        /**
         * Set the classpath to be used for incoming requests for classes
         * from the remote agent.
         *
         * @param path The classpath to be set, using the <em>local</em>
         *             file and path separator characters.
         * @see #getClassPath
         * @see #setClassPath(File[])
         */
        public void setClassPath(String path) {
            classPath = split(path);
        }

        /**
         * Set the classpath to be used for incoming requests for classes
         * from the remote agent.
         *
         * @param path An array of files, representing the path to be used.
         * @see #getClassPath
         * @see #setClassPath(String)
         */
        public void setClassPath(File... path) {
            if (path == null) {
                throw new NullPointerException();
            }

            for (File aPath : path) {
                if (aPath == null) {
                    throw new IllegalArgumentException();
                }
            }

            classPath = path;
        }

        /**
         * Set the shared classloader mode to be used for incoming requests for
         * classes from the remote agent.
         *
         * @param state The shared classloader mode, true to use a shared loader.
         */
        public void setSharedClassLoader(boolean state) {
            this.sharedCl = state;
        }

        /**
         * Set the timeout after command thread will be interrupted on the agent
         *
         * @param timeout value in seconds
         */
        public void setAgentCommandTimeout(int timeout) {
            this.timeout = timeout;
        }

        /**
         * Request the agent for this client to execute a standard Test class.
         *
         * @param tag          A string to identify the request in debug and trace output.
         * @param className    The name of the class to execute. The class must be an
         *                     implementation of com.sun.javatest.Test, and must be
         *                     accessible by the agent: just the <em>name</em> of the class
         *                     is sent to the agent, not the classfile.
         * @param args         The arguments to be passed to the {@code run} method
         *                     of an instance of {@code classname} that will be executed
         *                     by the agent.
         * @param localizeArgs Whether or not to instruct the agent to localize the args
         *                     to be passed to the test class. For example, this may be
         *                     necessary if the test has arguments that involve filenames
         *                     that differ from system to system.
         * @param log          A stream to which to write any data written to the log
         *                     stream when the test class is run.
         * @param ref          A stream to which to write any data written to the ref
         *                     stream when the test class is run.
         * @return The status returned when the test class is run by the agent.
         * @see com.sun.javatest.Test
         */
        public Status executeTest(String tag, String className, String[] args,
                                  boolean localizeArgs,
                                  PrintWriter log, PrintWriter ref) {
            return run(tag, "executeTest", className, args, localizeArgs, log, ref);
        }

        /**
         * Request the agent for this client to execute a standard Command class.
         *
         * @param tag          A string to identify the request in debug and trace output.
         * @param className    The name of the class to execute. The class must be an
         *                     implementation of com.sun.javatest.Command, and must be
         *                     accessible by the agent: just the <em>name</em> of the class
         *                     is sent to the agent, not the classfile.
         * @param args         The arguments to be passed to the {@code run} method
         *                     of an instance of {@code classname} that will be executed
         *                     by the agent.
         * @param localizeArgs Whether or not to instruct the agent to localize the args
         *                     to be passed to the test class. For example, this may be
         *                     necessary if the test has arguments that involve filenames
         *                     that differ from system to system.
         * @param log          A stream to which to write any data written to the log
         *                     stream when the command class is run.
         * @param ref          A stream to which to write any data written to the ref
         *                     stream when the command class is run.
         * @return The status returned when the command class is run by the agent.
         * @see com.sun.javatest.Command
         */
        public Status executeCommand(String tag, String className, String[] args,
                                     boolean localizeArgs,
                                     PrintWriter log, PrintWriter ref) {
            return run(tag, "executeCommand", className, args, localizeArgs, log, ref);
        }

        /**
         * Request the agent for this client to execute the standard "main" method
         * for a class.
         *
         * @param tag          A string to identify the request in debug and trace output.
         * @param className    The name of the class to execute. The class must be an
         *                     implementation of com.sun.javatest.Command, and must be
         *                     accessible by the agent: just the <em>name</em> of the class
         *                     is sent to the agent, not the classfile.
         * @param args         The arguments to be passed to the {@code run} method
         *                     of an instance of {@code classname} that will be executed
         *                     by the agent.
         * @param localizeArgs Whether or not to instruct the agent to localize the args
         *                     to be passed to the test class. For example, this may be
         *                     necessary if the test has arguments that involve filenames
         *                     that differ from system to system.
         * @param log          A stream to which to write any data written to the log
         *                     stream when the command class is run.
         * @param ref          A stream to which to write any data written to the ref
         *                     stream when the command class is run.
         * @return Status.passed if the method terminated normally;
         * Status.failed if the method threw an exception;
         * Status.error if some other problem arose.
         * @see com.sun.javatest.Command
         */
        public Status executeMain(String tag, String className, String[] args,
                                  boolean localizeArgs,
                                  PrintWriter log, PrintWriter ref) {
            return run(tag, "executeMain", className, args, localizeArgs, log, ref);
        }

        private Status run(String tag, String request, String executable, String[] args,
                           boolean localizeArgs,
                           PrintWriter log, PrintWriter ref) {
            notifyStarted(connection, tag, request, executable, args, localizeArgs);
            Status result = null;
            try {
//                boolean sharedClOption = false;
                out.writeShort(Agent.protocolVersion);
                out.writeUTF(tag);
                out.writeUTF(request);
                out.writeUTF(executable);
                out.writeShort(args.length);
                for (String arg : args) {
                    out.writeUTF(arg);
//                    if ("-sharedCl".equalsIgnoreCase(args[i]) ||
//                        "-sharedClassLoader".equalsIgnoreCase(args[i])) {
//                        sharedClOption = true;
//                    }
                }

                out.writeBoolean(localizeArgs);
                out.writeBoolean(classPath != null); // specify remoteClasses if classPath has been given
                out.writeBoolean(sharedCl);
                out.writeInt(timeout);
                out.writeByte(0);
                out.flush();

                result = readResults(log, ref);
            } catch (IOException e) {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ignore) {
                }
                if (e instanceof InterruptedIOException) {
                    result = Status.error("Communication with agent interrupted! (timed out?)." +
                            "\n InterruptedException: " + e);
                } else {
                    result = Status.error("Problem communicating with agent: " + e);
                }
            } finally {
                notifyFinished(connection, result);
            }
            return result;
        }

        private Status readResults(PrintWriter log, PrintWriter ref)
                throws IOException {
            Status status = null;

            while (status == null) {
                int code = in.read();
                switch (code) {
                    case -1: // unexpected EOF
                        status = Status.error("premature EOF from agent");
                        break;

                    case Agent.CLASS:
                        String className = in.readUTF();
                        //System.err.println("received request for " + className);
                        AgentRemoteClassData classData = locateClass(className);
                        classData.write(out);
                        out.flush();
                        break;

                    case Agent.DATA:
                        String resourceName = in.readUTF();
                        //System.err.println("received request for " + resourceName);
                        byte[] resourceData = locateData(resourceName);
                        if (resourceData == null)
                        //System.err.println("resource not found: " + className);
                        {
                            out.writeInt(-1);
                        } else {
                            out.writeInt(resourceData.length);
                            out.write(resourceData, 0, resourceData.length);
                        }
                        out.flush();
                        //System.err.println("done request for " + resourceName);
                        break;

                    case Agent.STATUS:
                        int type = in.read();
                        String reason = in.readUTF();
                        switch (type) {
                            case Status.PASSED:
                                status = Status.passed(reason);
                                break;
                            case Status.FAILED:
                                status = Status.failed(reason);
                                break;
                            case Status.ERROR:
                                status = Status.error(reason);
                                break;
                            default:
                                status = Status.failed("Bad status from test: type=" + type + " reason=" + reason);
                                break;
                        }
                        break;

                    case Agent.LOG:
                        log.write(in.readUTF());
                        break;

                    case Agent.LOG_FLUSH:
                        log.write(in.readUTF());
                        log.flush();
                        break;

                    case Agent.REF:
                        ref.write(in.readUTF());
                        break;

                    case Agent.REF_FLUSH:
                        ref.write(in.readUTF());
                        ref.flush();
                        break;
                }
            }

            out.close();
            in.close();
            connection.close();

            log.flush();
            ref.flush();

            // might be better not to flush these ...
            for (Enumeration<File> e = zips.keys(); e.hasMoreElements(); ) {
                File f = e.nextElement();
                ZipFile z = zips.get(f);
                zips.remove(f);
                z.close();
            }

            return status;
        }

        private AgentRemoteClassData locateClass(String name) {
            //System.err.println("locateClass: " + name);
            if (classPath != null) {
                String cname = name.replace('.', '/') + ".class";
                for (File aClassPath : classPath) {
                    byte[] data;
                    if (aClassPath.isDirectory()) {
                        data = readFromDir(cname, aClassPath);
                    } else {
                        data = readFromJar(cname, aClassPath);
                    }
                    if (data != null) {
                        String codeSource = "";
                        try {
                            codeSource = aClassPath.toURI().toURL().getPath();
                        } catch (IOException e) {
                            //codeSource will not be set
                        }
                        return new AgentRemoteClassData(name, codeSource, data);
                    }
                }
            }

            return AgentRemoteClassData.NO_CLASS_DATA;
        }

        private byte[] locateData(String name) {
            //System.err.println("locateData: " + name);
            if (classPath != null) {
                for (File aClassPath : classPath) {
                    byte[] data;
                    if (aClassPath.isDirectory()) {
                        data = readFromDir(name, aClassPath);
                    } else {
                        data = readFromJar(name, aClassPath);
                    }
                    if (data != null) {
                        return data;
                    }
                }
            }

            return null;
        }

        private byte[] readFromDir(String name, File dir) {
            //System.err.println("readFromDir: " + name + " " + dir);
            try {
                File file = new File(dir, name);
                return read(new FileInputStream(file), (int) file.length());
            } catch (IOException e) {
                //System.err.println("readFromDir: " + e);
                return null;
            }
        }

        private byte[] readFromJar(String name, File jarFile) {
            //System.err.println("readFromJar: " + name + " " + jarFile);
            try {
                ZipFile z = zips.get(jarFile);
                if (z == null) {
                    z = new ZipFile(jarFile);
                    zips.put(jarFile, z);
                }
                ZipEntry ze = z.getEntry(name);
                if (ze == null) {
                    return null;
                }
                return read(z.getInputStream(ze), (int) ze.getSize());
            } catch (IOException e) {
                //System.err.println("readFromJar: " + e);
                return null;
            }
        }

        private byte[] read(InputStream in, int size) throws IOException {
            //System.err.println("read: " + size);
            try {
                byte data[] = new byte[size];
                for (int total = 0; total < data.length; ) {
                    int n = in.read(data, total, data.length - total);
                    if (n > 0) {
                        total += n;
                    } else {
                        throw new EOFException("unexpected end of file");
                    }
                }
                //System.err.println("read complete: " + data.length);
                return data;
            } finally {
                in.close();
            }
        }

        private File[] split(String s) {
            char pathCh = File.pathSeparatorChar;
            Vector<File> v = new Vector<>();
            int start = 0;
            for (int i = s.indexOf(pathCh); i != -1; i = s.indexOf(pathCh, start)) {
                add(s.substring(start, i), v);
                start = i + 1;
            }
            if (start != s.length()) {
                add(s.substring(start), v);
            }
            return v.toArray(new File[v.size()]);
        }

        private void add(String s, Vector<File> v) {
            if (!s.isEmpty()) {
                v.add(new File(s));
            }
        }
    }

}

