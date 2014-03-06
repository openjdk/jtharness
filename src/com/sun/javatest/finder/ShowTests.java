/*
 * $Id$
 *
 * Copyright (c) 2001, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.finder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;

import com.sun.javatest.TestDescription;
import com.sun.javatest.TestFinder;

/**
 * ShowTests is a utility program to show the tests that are read by
 * a test finder.
 */
public class ShowTests
{
    /**
     * This exception is used to report bad command line arguments.
     */
    public class BadArgs extends Exception {
        /**
         * Create a BadArgs exception.
         * @param msg A detail message about an error that has been found.
         */
        BadArgs(String msg) {
            super(msg);
        }
    }

    /**
     * This exception is used to report problems that occur while running.
     */

    public class Fault extends Exception {
        /**
         * Create a Fault exception.
         * @param msg A detail message about a fault that has occurred.
         */
        Fault(String msg) {
            super(msg);
        }
    }

    //------------------------------------------------------------------------------------------

    /**
     * Standard program entry point.
     * @param args      An array of strings, typically provided via the command line.
     * The arguments should be of the form:<br>
     * <em>[options]</em> <em>testsuite</em>
     * <table><tr><th colspan=2>Options</th></tr>
     * <tr><td>-finder <em>finderClass</em> <em>finderArgs</em> <em>...</em> -end
     *          <td>The name of a test finder class and any arguments it might take.
     *          The results of reading this test finder will be stored in the
     *          output file.
     * <tr><td>-initial <em>initial-file</em>
     *          <td>An initial file within the test suite at which to start reading tests.
     * <tr><td>-o <em>output-file</em>
     *          <td>The output file in which to write the results. If omitted,
     *          The results will be written to the standard console output stream.
     * <tr><td>-nodes
     *          <td>By default, only the names of the tests are output.
     *          If you specify this option, the names of the parent directories
     *          will be displayed as well.
     * <tr><td>-full]tests
     *          <td>By default, only the names of the tests are output.
     *          If you specify this option, the contents of the test descriptions
     *          will be displayed as well.
     * </table>
     */
    public static void main(String[] args) {
        try {
            if (args.length == 0)
                usage(System.out);
            else {
                ShowTests m = new ShowTests();
                m.run(args);
            }
        }
        catch (BadArgs e) {
            System.err.println("Bad Arguments: " + e.getMessage());
            usage(System.err);
            System.exit(1);
        }
        catch (Fault f) {
            System.err.println("Error: " + f.getMessage());
            System.exit(2);
        }
        catch (IOException e) {
            System.err.println("Error: " + e);
            System.exit(3);
        }
    }

    /**
     * Print out command-line help.
     */
    private static void usage(PrintStream out) {
        String prog = System.getProperty("program", "java " + ShowTests.class.getName());
        out.println("Usage:");
        out.println("  " + prog + " [options]  test-suite");
        out.println("Options:");
        out.println("  -finder finderClass finderArgs... -end");
        out.println("                          specify the test finder to be used");
        out.println("  -initial initial-file   specify a starting point (optional)");
        out.println("  -o output-file          output file (default is standard output)");
        out.println("  -nodes                  show nodes in the tree");
        out.println("  -fulltests              show contents of tests");
    }

    //------------------------------------------------------------------------------------------

    /**
     * Main work method.
     * Reads all the arguments on the command line, makes sure a valid
     * testFinder is available, and then calls methods to create the tree of tests
     * and then write the binary file.
     *
     * @param args      An array of strings, typically provided via the command line
     * @throws ShowTests.BadArgs
     *                  if a problem is found in the arguments provided
     * @throws ShowTests.Fault
     *                  if a fault is found while running
     * @throws IOException
     *                  if a problem is found while trying to read a file
     * @see #main
     */
    public void run(String[] args) throws BadArgs, Fault, IOException {
        File testSuite = null;
        String finder = null;
        String[] finderArgs = null;
        File outFile = null;
        File initialFile = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-finder") && (i + 1 < args.length)) {
                finder = args[++i];
                int j = ++i;
                while ((i < args.length - 1) && !(args[i].equalsIgnoreCase("-end")))
                    ++i;
                finderArgs = new String[i - j];
                System.arraycopy(args, j, finderArgs, 0, finderArgs.length);
            }
            else if (args[i].equalsIgnoreCase("-initial") && (i + 1 < args.length)) {
                initialFile = new File(args[++i]);
            }
            else if (args[i].equalsIgnoreCase("-o") && (i + 1 < args.length)) {
                outFile = new File(args[++i]);
            }
            else if (args[i].equalsIgnoreCase("-nodes")) {
                nodes = true;
            }
            else if (args[i].equalsIgnoreCase("-fulltests")) {
                fullTests = true;
            }
            else if (args[i].startsWith("-") ) {
                throw new BadArgs(args[i]);
            }
            else
                testSuite = new File(args[i]);
        }

        if (finder == null)
            throw new BadArgs("no test finder specified");

        if (testSuite == null)
            throw new BadArgs("testsuite.html file not specified");

        testFinder = initializeTestFinder(finder, finderArgs, testSuite);

        if (initialFile == null)
            initialFile = testFinder.getRoot(); // equals testSuite, adjusted by finder as necessary .. e.g. for dirWalk, webWalk etc

        if (outFile == null)
            out = System.out;
        else
            out = new PrintStream(new BufferedOutputStream(new FileOutputStream(outFile)));

        // read the tests and write them to output
        list(initialFile);
    }

    //------------------------------------------------------------------------------------------

    /**
     * Creates and initializes an instance of a test finder
     *
     * @param finder The class name of the required test finder
     * @param args any args to pass to the TestFinder's init method.
     * @param ts The testsuite root file
     * @return The newly created TestFinder.
     */
    private TestFinder initializeTestFinder(String finder, String[] args, File ts) throws Fault {
        TestFinder testFinder;

        try {
            Class c = Class.forName(finder);
            testFinder = (TestFinder) (c.newInstance());
            testFinder.init(args, ts, null);
        }
        catch (ClassNotFoundException e) {
            throw new Fault("Error: Can't find class for TestFinder specified");
        }
        catch (InstantiationException e) {
            throw new Fault("Error: Can't create new instance of TestFinder");
        }
        catch (IllegalAccessException e) {
            throw new Fault("Error: Illegal Access Exception");
        }
        catch (TestFinder.Fault e) {
            throw new Fault("Error: Can't initialize test-finder: " + e.getMessage());
        }

        return testFinder;
    }

    //------------------------------------------------------------------------------------------

    private void list(File file) {
        if (nodes)
            out.println(file);

        testFinder.read(file);
        TestDescription[] tests = testFinder.getTests();
        File[] files = testFinder.getFiles();

        if (tests != null) {
            for (int i = 0; i < tests.length; i++) {
                TestDescription td = tests[i];
                out.println("    " + td.getRootRelativeURL());
                if (fullTests) {
                    for (Iterator iter = td.getParameterKeys(); iter.hasNext(); ) {
                        String key = (String) (iter.next());
                        String value = td.getParameter(key);
                        out.print("        ");
                        out.print(key);
                        pad(key, 15);
                        out.print(value);
                        out.println();
                    }
                }
            }
        }

        if (files != null) {
            for (int i = 0; i < files.length; i++)
                list(files[i]);
        }
    }

    void pad(String s, int length) {
        for (int i = s.length(); i < length; i++)
            out.write(' ');
        out.write(' ');
    }

    //------------------------------------------------------------------------------------------

    private TestFinder testFinder;
    private PrintStream out;
    private boolean nodes;
    private boolean fullTests;
}
