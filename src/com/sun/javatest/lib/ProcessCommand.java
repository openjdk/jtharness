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
package com.sun.javatest.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


import com.sun.javatest.Command;
import com.sun.javatest.Status;
import com.sun.javatest.util.StringArray;


/**
 * A Command to execute an arbitrary OS command.
 **/
public class ProcessCommand extends Command
{
    /**
     * A stand-alone entry point for this command. An instance of this
     * command is created, and its <code>run</code> method invoked,
     * passing in the command line args and <code>System.out</code> and
     * <code>System.err</code> as the two streams.
     * @param args command line arguments for this command.
     * @see #run
     */
    public static void main(String[] args) {
        PrintWriter log = new PrintWriter(new OutputStreamWriter(System.err));
        PrintWriter ref = new PrintWriter(new OutputStreamWriter(System.out));
        Status s;
        try {
            Command cmd = new ProcessCommand();
            s = cmd.run(args, log, ref);
        }
        finally {
            log.flush();
            ref.flush();
        }
        s.exit();
    }

    /**
     * Set a status to be returned for a specific exit code, overwriting any
     * previous setting for this exit code. If the default status has not yet
     * been initialized, it is set to Status.error("unrecognized exit code").
     *
     * @param exitCode The process exit code for which to assign a status.
     * @param status The status to associate with the exit code.
     */
    public void setStatusForExit(int exitCode, Status status) {
        if (statusTable == null) {
            statusTable = new Hashtable();
            if (defaultStatus == null)
                defaultStatus = Status.error("unrecognized exit code");
        }
        statusTable.put(new Integer(exitCode), status);
    }

    /**
     * Set the default status to be returned for all exit codes.
     * This will not affect any values for specific exit codes that
     * may have been set with setStatusForExit. If this method is
     * not called, the default value will be Status.failed (for
     * backwards compatibility) unless setStatusForExit has been
     * called, which sets the default value to Status.error.
     *
     * @param status The default status to use when a specific status
     * has not been set for a particular process exit code.
     */
    public void setDefaultStatus(Status status) {
        if (statusTable == null)
            statusTable = new Hashtable();
        defaultStatus = status;
    }

    /**
     * Set the directory in which to execute the process.
     * Use null to indicate the default directory.
     * @param dir the directory in which to execute the process.
     * @see #getExecDir
     */
    public void setExecDir(File dir) {
        execDir = dir;
    }

    /**
     * Get the directory in which to execute the process,
     * or null if none set.
     * @return the directory in which to execute the process.
     * @see #setExecDir
     */
    public File getExecDir() {
        return execDir;
    }

    /**
     * Internal routine to assist argument decoding prior to calling
     * setStatusForExit or setDefaultStatus
     */
    private void setStatus(String exitSpec, Status status) {
        // for now, we just support "default" and <integer>
        // in principle we could support ranges and lists too
        if (exitSpec.equals("default"))
            setDefaultStatus(status);
        else
            setStatusForExit(Integer.parseInt(exitSpec), status);
    }

    /**
     * Run the given command.
     * @param args      An array of strings composed of
     *                  <em>command-options</em>,
     *                  <em>environment-variables</em>,
     *                  <em>command</em>,
     *                  <em>command-arguments</em>.
     *                  <br>
     *
     *                  The <em>command-options</em> are an optional
     *                  set of options, each beginning with `-', to be
     *                  used by this object.
     *                  The options are
     *                  <dt>
     *                  <dt>-v
     *                  <dd>    verbose mode
     *                  <dt>-pass|-fail|-error <i>exit-code</i> <i>string</i>
     *                  <dd>    set the status to be returned for the given
     *                          exit code to one of
     *                          Status.passed/Status.failed/Status.error.
     *                          <i>exit-code</i> can be either an integer or "default".
     *                          <i>string</i> the message string provided in the
     *                          status object.
     *                  <dt>-execDir <i>execDir</i>
     *                  <dd>    set the directory in which to execute the command.
     *                  <dt>-inheritEnv
     *                  <dd>Instructs the code which invokes the new process to
     *                      allow it to inherit the parent environment values.
     *                  </dl>
     *                  <br>
     *
     *                  The <em>environment-variables</em> are an
     *                  optional list of environment variable to be supplied
     *                  to the command. They should be in the form
     *                  <em>NAME</em><code>=</code><em>VALUE</em>.
     *                  <br>
     *
     *                  The <em>command</em> identifies the command to
     *                  be executed. This name will be platform specific.
     *                  <br>
     *
     *                  The <em>command-arguments</em> are an optional
     *                  list of strings to be passed to the command to be
     *                  executed.
     * @param log       A stream for logging output.
     * @param ref       A stream for reference output, if the test requires it.
     *                  If not, it can be used as an additional logging stream.
     *
     * @return          The result of the method is obtained by calling
     *                  <code>getStatus</code> after the command completes.
     *                  The default behaviour is to use the explicit or default
     *                  status given in the arguments, or via the API. If none
     *                  have been set up, then the following values are used:
     *                  <code>Status.passed("exit code 0")</code>
     *                  if the command exited with exit status 0, or
     *                  <code>Status.failed("exit code " + exitCode)</code>
     *                  otherwise.  <code>getStatus</code> may be overridden
     *                  to provide different behavior.
     *
     * @see #getStatus
     *
     **/
    public Status run(String[] args, PrintWriter log, PrintWriter ref) {
        // analyze options
        int i = 0;
        for (; i < args.length && args[i].startsWith("-"); i++) {
            if (args[i].equals("-v"))
                verbose = true;
            else if (args[i].equals("-execDir") && i+1 < args.length) {
                execDir = new File(args[++i]);
            }
            else if (args[i].equals("-pass") && i+2 < args.length) {
                setStatus(args[++i], Status.passed(args[++i]));
            }
            else if (args[i].equals("-fail") && i+2 < args.length) {
                setStatus(args[++i], Status.failed(args[++i]));
            }
            else if (args[i].equals("-error") && i+2 < args.length) {
                setStatus(args[++i], Status.error(args[++i]));
            }
            else if (args[i].equals("-inheritEnv")) {
                inheritEnv = true;
            }
            else if (args[i].equals("-end")) {
                // -end is supported for the improbable event someone wants an
                // env var or command beginning with -
                i++;  // because the for-loop won't get a chance to do it
                break;
            }
            else
                return Status.error("Unrecognized option: " + args[i]);
        }

        // get environment variables for command
        int cmdEnvStart = i;
        while (i < args.length && (args[i].indexOf('=') != -1))
            i++;
        String[] cmdEnv = new String[i - cmdEnvStart];
        System.arraycopy(args, cmdEnvStart, cmdEnv, 0, cmdEnv.length);

        // get command name
        if (i == args.length)
            return Status.error("no command specified for " + getClass().getName());

        String[] cmd = new String[args.length - i];
        System.arraycopy(args, i, cmd, 0, cmd.length);

        return exec(cmd, cmdEnv, log, ref);
    }

    /**
     * Execute a command, bypassing the standard argument decoding of 'run'.
     * @param cmd       The command to be executed
     * @param cmdEnv    The environment to be passed to the command
     * @param log       A stream for logging output.
     * @param ref       A stream for reference output, if the test requires it.
     *                  If not, it can be used as an additional logging stream.
     * @return          The result of the method is obtained by calling
     *                  <code>getStatus</code> after the command completes.
     * @see #run
     * @see #getStatus
     */
    public Status exec(String[] cmd, String[] cmdEnv, PrintWriter log, PrintWriter ref) {
        Process p = null;
        Status s = null;
        Reader inReader = null;
        Reader errReader = null;
        try {
            // The following is a workaround for a JDK problem ... if the cmdEnv
            // is empty, JDK assumes this means to inherit the parent environment.
            // (There is a separate call which more reasonably means that.)
            // So, to prevent the parent process' environment being inherited
            // we set the command environment to a dummy entry which will hopefully
            // not cause any problems for either the Runtime machinery or the
            // child process.
            if (inheritEnv) {
                // copy env from system
                // then apply cmdEnv
                ArrayList<String> out = new ArrayList();
                Map<String,String> sysenv = System.getenv();
                Set<String> keys = sysenv.keySet();
                for (String key : keys) {
                    String value = sysenv.get(key);
                    key = key.replaceAll(" ", "_"); // sanitize
                    key = key.replaceAll("=", ">"); // sanitize
                    out.add(key + "=" + (value == null ? "" : value));
                }   // while

                if (cmdEnv != null && cmdEnv.length != 0) {
                    for (String str : cmdEnv) {
                        out.add(str);
                    }
                }

                // set new cmdEnv with system env injected
                // NOTE: upgrade should be made to eliminate duplicate keys
                cmdEnv = out.toArray(new String[cmdEnv.length]);
            }
            else if (cmdEnv != null && cmdEnv.length == 0) {
                String[] envWithDummyEntry = {/*empty*/"="/*empty*/};
                cmdEnv=envWithDummyEntry;
            }

            if (verbose) {
                log.println("Command is: " + StringArray.join(cmd));
                if (cmdEnv == null) {
                    log.println("Command environment is inherited from parent process");
                }
                else if (cmdEnv.length == 0) {
                    log.println("Command environment is empty");
                }
                else {
                    log.println("Command environment is:");
                    for (int i = 0; i < cmdEnv.length; i++)
                        log.println(cmdEnv[i]);
                }
                if (execDir != null)
                    log.println("Execution directory is " + execDir);
            }

            Runtime r = Runtime.getRuntime();
            p = (execDir == null ? r.exec(cmd, cmdEnv) : r.exec(cmd, cmdEnv, execDir));

            inReader = new InputStreamReader(p.getInputStream()); // output stream from process
            StreamCopier refConnector = new StreamCopier(inReader, ref);
            refConnector.start();
            errReader = new InputStreamReader(p.getErrorStream());
            StreamCopier logConnector = new StreamCopier(errReader, log);
            logConnector.start();

            OutputStream out = p.getOutputStream();  // input stream to process
            if (out != null)
                out.close();

            // wait for the stream copiers to complete (which may be interrupted by the
            // timeout thread
            refConnector.waitUntilDone();
            logConnector.waitUntilDone();

            // wait for the process to complete;
            // WARNING: in JDK1.0.2 this does not appear to be interruptible, which is
            // why we waited for the stream copiers to complete first ... because they are
            // interruptible.
            int exitCode = p.waitFor();
            //if (verbose > 0)
            //  log.report("command exited, exit=" + exitCode);

            return getStatus(exitCode, logConnector.exitStatus());
        }
        catch (InterruptedException e) {
            if (p != null)
                p.destroy();
            String msg = "Program `" + cmd[0] + "' interrupted! (timed out?)";
            s = (useFailedOnException ? Status.failed(msg) : Status.error(msg));
        }
        catch (IOException e) {
            String msg = "Error invoking program `" + cmd[0] + "': " + e;
            s = (useFailedOnException ? Status.failed(msg) : Status.error(msg));
        }
        finally  {
           try {
                if (inReader != null) {
                    inReader.close();
                }
           } catch (Exception whatever) {
           }
           try {
                if (errReader != null) {
                    errReader.close();
                }
           } catch (Exception whatever) {
           }
        }
        return s;
    }

    /**
     * Generate a status for the command, based upon the command's exit code
     * and a status that may have been passed from the command by using
     * <code>status.exit()</code>.
     *
     * @param exitCode          The exit code from the command that was executed.
     * @param logStatus         If the command that was executed was a test program
     *                          and exited by calling <code>status.exit()</code>,
     *                          then logStatus will be set to `status'.  Otherwise,
     *                          it will be null.  The value of the status is passed
     *                          from the command by writing it as the last line to
     *                          stdout before exiting the process.   If it is not
     *                          received as the last line, the value will be lost.
     * @return          Unless overridden, the default is
     *                  <code>Status.passed("exit code 0")</code>
     *                  if the command exited with exit code 0, or
     *                  <code>Status.failed("exit code " + exitCode)</code>
     *                  otherwise.
     **/
    protected Status getStatus(int exitCode, Status logStatus) {
        if (logStatus != null)
            return logStatus;
        else if (statusTable != null) {
            Status s = (Status)(statusTable.get(new Integer(exitCode)));
            return (s == null ? defaultStatus.augment("exit code: " + exitCode) : s);
        }
        else if (exitCode == 0)
            return Status.passed("exit code 0");
        else
            return Status.failed("exit code " + exitCode);
    }

    private boolean verbose;


    /**
     * A thread to copy an input stream to an output stream
     */
    class StreamCopier extends Thread
    {
        /**
         * Create one.
         * @param from  the stream to copy from
         * @param out   the log to copy to
         */
        StreamCopier(Reader from, PrintWriter to) {
            super(Thread.currentThread().getName() + "_StreamCopier_" + (serial++));
            in = new BufferedReader(from);
            out = to;
            lastStatusLine = null;
        }

        /**
         * Set the thread going.
         */
        public void run() {
            //System.out.println("Copying stream");
            try {
                String line;
                while ((line = in.readLine()) != null) {
            // take care lastLine doesn't get set to null at EOF
            // and ignore trailing newlines
            if (line.startsWith(Status.EXIT_PREFIX)) {
                line = Status.decode(line);
                lastStatusLine = line;
            }
            out.println(line);
                }
            }
            catch (IOException e) {
            }
            //System.out.println("Stream copied");
            synchronized (this) {
                done = true;
                notifyAll();
            }
        }

        public synchronized boolean isDone() {
            return done;
        }

        /**
         * Blocks until the copy is complete, or until the thread is interrupted
         */
        public synchronized void waitUntilDone() throws InterruptedException {
            boolean interrupted = false;

            // poll interrupted flag, while waiting for copy to complete
            while (!(interrupted = Thread.interrupted()) && !done)
                wait(1000);

            //if (interrupted)
            //    System.out.println("TESTSCRIPT DETECTS interrupted() " + Thread.currentThread().getName());
            //else
            //    System.out.println("TESTSCRIPT waitUntilDone OK " + Thread.currentThread().getName());

            // workaround; if the exception hasn't been thrown already, do it now
            if (interrupted) {
                //System.out.println("Stream copier: throwing InterruptedException");
                throw new InterruptedException();
            }
        }

        /**
         * Return the status information from the child process if it returned
         * any on the log stream, otherwise return null.
         */
        public Status exitStatus() {
            if (lastStatusLine == null)
                return null;
            else
                return Status.parse(lastStatusLine.substring(Status.EXIT_PREFIX.length()));
        }


        private BufferedReader in;
        private PrintWriter out;
        private String lastStatusLine;
        private boolean done;

    }

    private static boolean useFailedOnException =
        Boolean.getBoolean("javatest.processCommand.useFailedOnException");

    private static int serial;
    private Hashtable statusTable;
    private Status defaultStatus;
    private File execDir;
    private boolean inheritEnv =
            Boolean.getBoolean("javatest.processCommand.inheritEnv");;
}

