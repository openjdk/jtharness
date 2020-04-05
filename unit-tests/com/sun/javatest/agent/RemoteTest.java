/*
 * $Id$
 *
 * Copyright (c) 2001, 2019, Oracle and/or its affiliates. All rights reserved.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.ServerSocket;
import java.util.Vector;

import com.sun.javatest.Status;
import com.sun.javatest.Test;
import org.junit.Assert;

public class RemoteTest {


    @org.junit.Test
    public void test() {
        RemoteTest t = new RemoteTest();
        boolean ok = t.run(System.out);
        Assert.assertTrue(ok);
    }

    public boolean run(PrintStream log) {
        String host = "localhost";
        int port = 0;
        int seed = 0;

        try {
            ServerSocket ss = new ServerSocket(port);
            if (port == 0)
                port = ss.getLocalPort();
            ConnectionFactory cf = new PassiveConnectionFactory(ss);
            Agent agent = new Agent(cf, 1);
            Thread t = new Thread(agent);
            t.setName("Test Agent");
            t.start();
            log.println("Test Agent started");

            Random rgen = new Random(seed);
            int nTestsOK = 0;

            for (int i = 0; i < nTests; i++) {
                Parent p = new Parent(host, port, Child.class.getName(), rgen.nextInt(), log);
                Status s = p.run();
                if (s.getType() == Status.PASSED)
                    nTestsOK++;
            }

            if (nTestsOK == nTests) {
                log.println("all tests succeeded");
                return true;
            } else {
                log.println((nTests - nTestsOK) + " tests failed out of " + nTests);
                return false;
            }
        } catch (IOException e) {
            log.println("Unexpected exception: " + e);
            return false;
        }
    }

    private int nTests = 10;
    static final int MAX_LINES_PER_TEST = 128;
    static final int MAX_CHARS_PER_LINE = 128;

    static class Parent {
        Parent(String host, int port, String testClassName, int seed, PrintStream log) {
            this.port = port;
            this.testClassName = testClassName;
            this.seed = seed;
            this.log = log;
            //System.err.println("Parent: seed=" + seed);
        }

        Status run() {
            try {
                ThreadSet ts = new ThreadSet();

                AgentManager.Task agentTask = AgentManager.access().connectToPassiveAgent("localhost", port);

                PipedWriter testLog = new PipedWriter();
                PipedWriter testRef = new PipedWriter();

                Random rgen = new Random(seed);

                // the sequence of the next few rgen calls must be the same for the
                // both the parent and the child
                RandomReader logReader = new RandomReader(rgen.nextInt(), new PipedReader(testLog), ts);
                RandomReader refReader = new RandomReader(rgen.nextInt(), new PipedReader(testRef), ts);
                Status exp = rgen.nextStatus();

                ts.start();

                String[] args = {Integer.toString(seed)};
                Status rcv = agentTask.executeTest("TEST", testClassName, args, false,
                        new PrintWriter(testLog), new PrintWriter(testRef));

                // close the output streams, to flush data through the pipes to the readers
                testLog.close();
                testRef.close();

                // wait for the readers to complete
                ts.join();

                boolean refOK = refReader.ok();
                boolean logOK = logReader.ok();
                boolean statusOK = equal(exp, rcv);
                if (refOK && logOK && statusOK)
                    return Status.passed("test passed");
                else {
                    if (!refOK)
                        log.println("Reference stream check failed");
                    if (!logOK)
                        log.println("Log stream check failed");
                    if (!statusOK) {
                        log.println("Status check failed");
                        log.println("Status returned from test: " + rcv);
                    }
                    return Status.failed("test failed");
                }
            } catch (InterruptedException | IOException e) {
                log.println("Unexpected exception: " + e);
                return Status.failed("Unexpected exception: " + e.getClass().getName());
            }
        }

        private boolean equal(Status s1, Status s2) {
            return s1.getType() == s2.getType() && s1.getReason().equals(s2.getReason());
        }

        int port;
        String testClassName;
        int seed;
        PrintStream log;
    }

    public static class Child implements Test {
        public Status run(String[] args, PrintWriter log, PrintWriter ref) {
            if (args.length != 1)
                return Status.failed("no seed specified for child");

            int seed = Integer.parseInt(args[0], 10);
            //System.err.println("Child: seed=" + seed);

            try {
                Random rgen = new Random(seed);
                ThreadSet t = new ThreadSet();

                // the sequence of the next few rgen calls must be the same for the
                // both the parent and the child
                RandomWriter logWriter = new RandomWriter(rgen.nextInt(), log, t);
                RandomWriter refWriter = new RandomWriter(rgen.nextInt(), ref, t);
                Status s = rgen.nextStatus();

                t.start();
                t.join();

                return s;
            } catch (InterruptedException e) {
                return Status.failed("Unexpected exception: " + e.getClass().getName());
            }
        }

    }

    private static class RandomReader extends Thread {
        RandomReader(int seed, Reader in, ThreadSet t) {
            this.seed = seed;
            this.in = new BufferedReader(in);
            this.t = t;
            t.add(this);
        }

        public void run() {
            try {
                Random rgen = new Random(seed);
                int expectCount = rgen.nextInt(RemoteTest.MAX_LINES_PER_TEST);
                int foundCount = 0;
                String line;
                try {
                    while ((line = in.readLine()) != null) {
                        String expectLine = rgen.nextString();
                        if (!line.equals(expectLine)) {
                            System.out.println("EXPECT: " + expectLine);
                            System.out.println("FOUND:  " + line);
                            errors++;
                        }
                        foundCount++;
                    }
                } catch (IOException e) {
                    System.out.println("Unexpected exception: " + e);
                    errors++;
                }
                if (expectCount != foundCount) {
                    System.out.println("expect " + expectCount + "; found " + foundCount);
                    errors++;
                }
            } finally {
                t.notifyExiting();
            }
        }

        synchronized boolean ok() {
            return errors == 0;
        }

        private int seed;
        private BufferedReader in;
        private ThreadSet t;
        private int errors;
    }

    private static class RandomWriter extends Thread {
        RandomWriter(int seed, PrintWriter out, ThreadSet t) {
            this.seed = seed;
            this.out = out;
            this.t = t;
            t.add(this);
        }

        public void run() {
            try {
                Random rgen = new Random(seed);
                int n = rgen.nextInt(RemoteTest.MAX_LINES_PER_TEST);
                for (int i = 0; i < n; i++) {
                    String s = rgen.nextString();
                    out.println(s);
                }
                out.flush();
            } finally {
                t.notifyExiting();
            }
        }

        synchronized boolean ok() {
            return errors == 0;
        }

        private int seed;
        private PrintWriter out;
        private ThreadSet t;
        private int errors;
    }

    private static class ThreadSet {
        public synchronized void add(Thread t) {
            threads.addElement(t);
        }

        public synchronized void start() {
            for (int i = 0; i < threads.size(); i++)
                threads.elementAt(i).start();
        }

        public synchronized void join() throws InterruptedException {
            while (threads.size() > 0)
                wait();
        }

        public synchronized void notifyExiting() {
            threads.removeElement(Thread.currentThread());
            notifyAll();
        }

        private Vector<Thread> threads = new Vector<>();
    }

    private static class Random extends java.util.Random {
        Random(int seed) {
            super(seed);
        }

        public int nextInt(int upb) {
            return Math.abs(nextInt()) % upb;
        }

        int nextInt(int lwb, int upb) {
            return lwb + Math.abs(nextInt()) % (upb - lwb);
        }

        String nextString() {
            int l = nextInt(RemoteTest.MAX_CHARS_PER_LINE);
            StringBuffer sb = new StringBuffer(l);
            for (int i = 0; i < l; i++) {
                sb.append((char) nextInt(32, 128)); // cheat!!
            }
            return new String(sb);

        }

        Status nextStatus() {
            return Status.passed("");
        }
    }
}
