/*
 * $Id$
 *
 * Copyright (c) 2001, 2011, Oracle and/or its affiliates. All rights reserved.
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
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


import com.sun.javatest.TestDescription;
import com.sun.javatest.TestFinder;

/**
 * BinaryTestWriter creates the data file used by BinaryTestFinder.
 * It uses a test finder to find all the tests in a test suite and writes
 * them out in a compact compressed form. By default it uses the standard
 * tag test finder, and writes the output in a file called
 * testsuite.jtd in the root directory of the test suite.
 * <br>
 * Options:
 * <dl>
 * <dt>-finder finderClass finderArgs ... -end
 * <dd>the test finder to be used to locate the tests; the default is the standard tag test finder
 * <dt>-strictFinder
 * <dd>Do not ignore errors from the source finder, exit with error code instead
 * <dt>-o output-file
 * <dd>specify the name of the output file; the default is testsuite.jtd in the root directory of the test suite.
 * <dt>testsuite
 * <dd>(Required.) The test suite root file.
 * <dt>initial-files
 * <dd>(Optional)Any initial starting points within the test suite: the default is the test suite root
 * </dl>
 */
public class BinaryTestWriter
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
     * <em>[options]</em> <em>testsuite</em> <em>[tests]</em>
     * <table><tr><th colspan=2>Options</th></tr>
     * <tr><td>-finder <em>finderClass</em> <em>finderArgs</em> <em>...</em> -end
     *          <td>The name of a test finder class and any arguments it might take.
     *          The results of reading this test finder will be stored in the
     *          output file.
     * <tr><td>-o <em>output-file</em>
     *          <td>The output file in which to write the results.
     * </table>
     */
    public static void main(String[] args) {
        int result = 0;

        try {
            BinaryTestWriter m = new BinaryTestWriter();
            result = m.run(args);
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

        System.exit(result);
    }

    /**
     * Print out command-line help.
     */
    private static void usage(PrintStream out) {
        String prog = System.getProperty("program", "java " + BinaryTestWriter.class.getName());
        out.println("Usage:");
        out.println("  " + prog + " [options]  test-suite [tests...]");
        out.println("Options:");
        out.println("  -finder finderClass finderArgs... -end");
        out.println("  -o output-file");
        out.println("  -strictFinder");
    }

    //------------------------------------------------------------------------------------------

    /**
     * Main work method.
     * Reads all the arguments on the command line, makes sure a valid
     * testFinder is available, and then calls methods to create the tree of tests
     * and then write the binary file.
     * @param args      An array of strings, typically provided via the command line
     * @return The disposition of the run, i.e. zero for a problem-free execution, non-zero
     *         if there was some sort of problem.
     * @throws BinaryTestWriter.BadArgs
     *                  if a problem is found in the arguments provided
     * @throws BinaryTestWriter.Fault
     *                  if a fault is found while running
     * @throws IOException
     *                  if a problem is found while trying to read a file
     *                  or write the output file
     * @see #main
     */
    public int run(String[] args) throws BadArgs, Fault, IOException {
        File testSuite = null;
        String finder = "com.sun.javatest.finder.TagTestFinder";
        String[] finderArgs = { };
        File outFile = null;
        File[] tests = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-finder") && (i + 1 < args.length)) {
                finder = args[++i];
                int j = ++i;
                while ((i < args.length - 1) && !(args[i].equalsIgnoreCase("-end")))
                    ++i;
                finderArgs = new String[i - j];
                System.arraycopy(args, j, finderArgs, 0, finderArgs.length);
            }
            else if (args[i].equalsIgnoreCase("-o") && (i + 1 < args.length)) {
                outFile = new File(args[++i]);
            }
            else if (args[i].equalsIgnoreCase("-strictFinder")) {
                strictFinder = true;
            }
            else if (args[i].startsWith("-") ) {
                throw new BadArgs(args[i]);
            }
            else {
                testSuite = new File(args[i++]);

                if (i < args.length) {
                    tests = new File[args.length - i];
                    for (int j = 0; j < tests.length; j++)
                        tests[j] = new File(args[i + j]);
                }
                break;
            }
        }

        if (testSuite == null)
            throw new BadArgs("testsuite.html file not specified");

        TestFinder testFinder = initializeTestFinder(finder, finderArgs, testSuite);

        if (tests == null)
            tests = new File[] { testFinder.getRoot() }; // equals testSuite, adjusted by finder as necessary .. e.g. for dirWalk, webWalk etc

        if (outFile == null)
            outFile = new File(testFinder.getRootDir(), "testsuite.jtd");

        if (strictFinder) {
            testFinder.setErrorHandler(new TestFinder.ErrorHandler() {
                    public void error(String msg) {
                        numFinderErrors++;
                        System.err.println("Finder reported error:\n" + msg);
                        System.err.println("");
                    }
                }
            );
        }

        StringTable stringTable = new StringTable();
        TestTable testTable = new TestTable(stringTable);
        TestTree testTree = new TestTree(testTable);

        if (log != null)
            log.println("Reading tests...");

        // read the tests into internal data structures
        read(testFinder, tests, testTree);

        if (testTree.getSize() == 0)
            throw new Fault("No tests found -- check arguments.");

        // write out the data structure into a zip file
        if (log != null)
            log.println("Writing " + outFile);

        FileOutputStream fos = new FileOutputStream(outFile);
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new BufferedOutputStream(fos));
            zos.setMethod(ZipOutputStream.DEFLATED);
            zos.setLevel(9);
            ZipEntry stringZipEntry = stringTable.write(zos);
            ZipEntry testTableZipEntry = testTable.write(zos);
            ZipEntry testTreeZipEntry = testTree.write(zos);
            zos.close();

            // report statistics
            if (log != null) {
                log.println("strings: " + stringTable.getSize() + " entries, " + zipStats(stringZipEntry));
                log.println("tests: " + testTable.getSize() + " tests, " + zipStats(testTableZipEntry));
                log.println("tree: " + testTree.getSize() + " nodes, " + zipStats(testTreeZipEntry));
            }

            if (strictFinder && numFinderErrors > 0) {
                System.err.println("*** Source finder reported " + numFinderErrors + " errors during execution. ***");
                return 4;
            }
            else {
                return 0;
            }
        }
        finally {
            // attempt to close zip writer first
            // followed by the underlying writer for leak prevention
            if (zos != null){
                try { zos.close(); } catch (IOException e) { }
            }

            if (fos != null){
                try { fos.close(); } catch (IOException e) { }
            }
        }
    }

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

        if (ts == null)
            throw new NullPointerException();

        try {
            Class c = Class.forName(finder);
            testFinder = (TestFinder) (c.newInstance());
            testFinder.init(args, ts, null);
        }
        catch (ClassNotFoundException e) {
            throw new Fault("Error: Can't find class for test finder specified: " + finder);
        }
        catch (InstantiationException e) {
            throw new Fault("Error: Can't create new instance of test finder: " + e);
        }
        catch (IllegalAccessException e) {
            throw new Fault("Error: Can't access test finder: " + e);
        }
        catch (TestFinder.Fault e) {
            throw new Fault("Error: Can't initialize test-finder: " + e.getMessage());
        }

        return testFinder;
    }


    /**
     * Gets and returns the test suite file. Adds testsuite.html or
     * tests/testsuite.html to the end of the path if necessary.
     */
    private File getTestSuiteFile(String file) throws Fault {
        File tsa = new File(file);
        if (tsa.isFile())
            return tsa;
        else {
            File tsb = new File(tsa, "testsuite.html");
            if (tsb.exists())
                return tsb;
            else {
                File tsc = new File(tsa, "tests/testsuite.html");
                if (tsc.exists())
                    return tsc;
                else
                    throw new Fault("Bad input. " + file + " is not a JCK");
            }
        }
    }

    /**
     * Create a string containing statistics about a zip file entry.
     */
    private String zipStats(ZipEntry e) {
        long size = e.getSize();
        long csize = e.getCompressedSize();
        return size + " bytes (" + csize + " compressed, " + (csize * 100 / size) + "%)";
    }

    //------------------------------------------------------------------------------------------

    /**
     * Read all the tests from a test suite and store them in a test tree
     */
    void read(TestFinder finder, File[] files, TestTree testTree) throws Fault
    {
        if (files.length < 1)
            throw new IllegalArgumentException();

        File rootDir = finder.getRootDir();
        Set allFiles = new HashSet();

        TestTree.Node r = null;
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (!f.isAbsolute())
                f = new File(rootDir, f.getPath());

            TestTree.Node n = read0(finder, f, testTree, allFiles);
            if (n == null)
                continue;

            while (!f.equals(rootDir)) {
                f = f.getParentFile();
                n = testTree.new Node(f.getName(), noTests, new TestTree.Node[] { n });
            }

            r = (r == null ? n : r.merge(n));
        }

        if (r == null)
            throw new Fault("No tests found");

        testTree.setRoot(r);
    }

    /**
     * Read the tests from a file in test suite
     */
    private TestTree.Node read0(TestFinder finder, File file, TestTree testTree, Set allFiles)
    {
        // keep track of which files we have read, and ignore duplicates
        if (allFiles.contains(file))
            return null;
        else
            allFiles.add(file);

        finder.read(file);
        TestDescription[] tests = finder.getTests();
        File[] files = finder.getFiles();

        if (tests.length == 0 && files.length == 0)
            return null;

        Arrays.sort(files);
        Arrays.sort(tests, new Comparator() {
            public int compare(Object o1, Object o2) {
                TestDescription td1 = (TestDescription) o1;
                TestDescription td2 = (TestDescription) o2;
                return td1.getRootRelativeURL().compareTo(td2.getRootRelativeURL());
            }
        });

        Vector v = new Vector();
        for (int i = 0; i < files.length; i++) {
            TestTree.Node n = read0(finder, files[i], testTree, allFiles);
            if (n != null)
                v.addElement(n);
        }
        TestTree.Node[] nodes = new TestTree.Node[v.size()];
        v.copyInto(nodes);

        return testTree.new Node(file.getName(), tests, nodes);
    }

    //------------------------------------------------------------------------------------------

    /**
     * Write an int to a data output stream using a variable length encoding.
     * The int is broken into groups of seven bits, and these are written out
     * in big-endian order. Leading zeroes are suppressed and all but the last
     * byte have the top bit set.
     * @see BinaryTestFinder#readInt
     */
    private static void writeInt(DataOutputStream out, int v) throws IOException {
        if (v < 0)
            throw new IllegalArgumentException();

        boolean leadZero = true;
        for (int i = 28; i > 0; i -= 7) {
            int b = (v >> i) & 0x7f;
            leadZero = leadZero && (b == 0);
            if (!leadZero)
                out.writeByte(0x80 | b);
        }
        out.writeByte(v & 0x7f);
    }

    //------------------------------------------------------------------------------------------

    private static final TestDescription[] noTests = { };
    private PrintStream log = System.out;
    private boolean strictFinder = false;
    private int numFinderErrors = 0;

    //------------------------------------------------------------------------------------------

    /**
     * StringTable is an array of strings. Other parts of the encoding can
     * choose to write strings as references (indexes) into the string table.
     * Strings in the table are use-counted so that only frequently used
     * strings are output.
     * @see BinaryTestFinder.StringTable
     */
    static class StringTable {
        /**
         * Add a new string to the table; if it has already been added,
         * increase its use count.
         */
        void add(String s) {
            Entry e = (Entry) (map.get(s));
            if (e == null) {
                e = new Entry();
                map.put(s, e);
            }
            e.useCount++;
        }

        /**
         * Add all the strings used in a test description to the table.
         */
        void add(TestDescription test) {
            for (Iterator i = test.getParameterKeys(); i.hasNext(); ) {
                String key = (String) (i.next());
                String param = test.getParameter(key);
                add(key);
                add(param);
            }
        }

        /**
         * Return the number of strings in the table.
         */
        int getSize() {
            return map.size();
        }

        /**
         * Return the number of sstrings that were written to the output file.
         * Not all strings are written out: only frequently used ones are.
         */
        int getWrittenSize() {
            return writtenSize;
        }

        /**
         * Get the index of a string in the table.
         */
        int getIndex(String s) {
            Entry e = (Entry) (map.get(s));
            if (e == null)
                throw new IllegalArgumentException();
            return e.index;
        }

        /**
         * Write the contents of the table to an entry called "strings"
         * in a zip file.
         */
        ZipEntry write(ZipOutputStream zos) throws IOException
        {
            ZipEntry entry = new ZipEntry("strings");
            zos.putNextEntry(entry);
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(zos));
            write(dos);
            dos.flush();
            zos.closeEntry();
            return entry;
        }

        /**
         * Write the contents of the table to a stream
         */
        void write(DataOutputStream o) throws IOException {
            Vector v = new Vector(map.size());
            v.addElement("");
            int nextIndex = 1;
            for (Iterator iter = map.entrySet().iterator(); iter.hasNext(); ) {
                Map.Entry e = (Map.Entry) (iter.next());
                String key = (String) (e.getKey());
                Entry entry = (Entry) (e.getValue());
                if (entry.isFrequent()) {
                    entry.index = nextIndex++;
                    v.addElement(key);
                }
            }

            writeInt(o, v.size());
            for (int i = 0; i < v.size(); i++)
                o.writeUTF((String) (v.elementAt(i)));

            writtenSize = nextIndex;
        }

        /**
         * Write a reference to a string to a stream.  The string must have
         * previously been added into nthe string table, and the string table
         * written out.
         * If the string is a frequent one, a pointer to its position in the
         * previously written stream will be generated. If it is not a frequent
         * string, zero will be written, followed by the value of the string itself.
         */
        void writeRef(String s, DataOutputStream o) throws IOException {
            Entry e = (Entry) (map.get(s));
            if (e == null)
                throw new IllegalArgumentException();

            if (e.isFrequent())
                writeInt(o, e.index);
            else {
                writeInt(o, 0);
                o.writeUTF(s);
            }
        }

        private TreeMap map = new TreeMap();
        private int writtenSize;

        /**
         * Data for each string in the string table.
         */
        static class Entry {
            /**
             * How many times the string has been added to the string table.
             */
            int useCount = 0;

            /**
             * The position of the string in the table when the table
             * was written.
             */
            int index = 0;

            /**
             * Determine if the string is frequent enough in the table to
             * be written out.
             */
            boolean isFrequent() {
                return (useCount > 1);
            }
        }
    }

    //------------------------------------------------------------------------------------------

    /**
     * TestTable is a table of test descriptions, whose written form is
     * based on references into a string table.
     * @see BinaryTestFinder.TestTable
     */
    static class TestTable
    {
        /**
         * Create a new TestTable.
         */
        TestTable(StringTable stringTable) {
            this.stringTable = stringTable;
        }

        /**
         * Add a test description to the test table. The strings used by the
         * test description are automatically added to the testTable's stringTable.
         */
        void add(TestDescription td) {
            tests.addElement(td);
            testMap.put(td, new Entry());
            stringTable.add(td);
        }

        /**
         * Get the number of tests in this test table.
         */
        int getSize() {
            return tests.size();
        }

        /**
         * Get the index for a test description, based on its position when the
         * test table was written out. This index is the byte offset in the
         * written stream.
         */
        int getIndex(TestDescription td) {
            Entry e = (Entry) (testMap.get(td));
            if (e == null)
                throw new IllegalArgumentException();
            return e.index;
        }

        /**
         * Write the contents of the table to an entry called "tests"
         * in a zip file.
         */
        ZipEntry write(ZipOutputStream zos) throws IOException
        {
            ZipEntry entry = new ZipEntry("tests");
            zos.putNextEntry(entry);
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(zos));
            write(dos);
            dos.flush();
            zos.closeEntry();
            return entry;
        }

        /**
         * Write the contents of the table to a stream. The position of each test
         * description in the stream is recorded, so that a random acess stream
         * can randomly access the individual test descriptions. The table is
         * written as a count, followed by that many encoded test descriptions.
         * Each test description is written as a count followed by that many
         * name-value pairs of string references.
         */
        void write(DataOutputStream o) throws IOException {
            writeInt(o, tests.size());
            for (int i = 0; i < tests.size(); i++) {
                TestDescription td = (TestDescription) (tests.elementAt(i));
                Entry e = (Entry) (testMap.get(td));
                e.index = o.size();
                write(td, o);
            }
        }

        /**
         * Write a single test description to a stream. It is written as a count,
         * followed by that many name-value pairs of string references.
         */
        private void write(TestDescription td, DataOutputStream o) throws IOException {
            // should consider using load/save here
            writeInt(o, td.getParameterCount());
            for (Iterator i = td.getParameterKeys(); i.hasNext(); ) {
                String key = (String) (i.next());
                String value = td.getParameter(key);
                stringTable.writeRef(key, o);
                stringTable.writeRef(value, o);
            }
        }

        private HashMap testMap = new HashMap();
        private Vector tests = new Vector();
        private StringTable stringTable;

        /**
         * Data for each test description in the table.
         */
        class Entry {
            /**
             * The byte offset of the test description in the stream when
             * last written out.
             */
            int index = -1;
        }
    }

    //------------------------------------------------------------------------------------------

    /**
     * TestTree is a tree of tests, whose written form is based on
     * references into a TestTable. There is a very strong correspondence
     * between a node and the results of reading a file from a test finder,
     * which yields a set of test descriptions and a set of additional files
     * to be read.
     * @see BinaryTestFinder.TestTable
     */
    static class TestTree
    {
        /**
         * Create an test tree. The root node of the tree should be set later.
         */
        TestTree(TestTable testTable) {
            this.testTable = testTable;
        }

        /**
         * Set the root node of the tree.
         */
        void setRoot(Node root) {
            this.root = root;
        }

        /**
         * Get the number of nodes in this tree.
         */
        int getSize() {
            return (root == null ? 0 : root.getSize());
        }

        /**
         * Write the contents of the tree to an entry called "tree"
         * in a zip file.
         */
        ZipEntry write(ZipOutputStream zos) throws IOException
        {
            ZipEntry entry = new ZipEntry("tree");
            zos.putNextEntry(entry);
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(zos));
            write(dos);
            dos.flush();
            zos.closeEntry();
            return entry;
        }

        /**
         * Write the contents of the tree to a stream. Each node of the tree
         * is written as 3 parts:
         * <ul>
         * <li>the name of the node
         * <li>the number of test descriptions in this node, followed by that
         * many references into the test table.
         * <li>the number of child nodes, followed by that many nodes, written
         * recursively.
         * </ul>
         */
        void write(DataOutputStream o) throws IOException {
            root.write(o);
        }

        private Node root;
        private TestTable testTable;

        /**
         * A node within the test tree. Each node has a name, a set of test
         * descriptions, and a set of child nodes.
         */
        class Node
        {
            /**
             * Create a node. The individual test descriptions are added to
             * the tree's test table.
             */
            Node(String name, TestDescription[] tests, Node[] children) {
                this.name = name;
                this.tests = tests;
                this.children = children;

                for (int i = 0; i < tests.length; i++)
                    testTable.add(tests[i]);
            }

            /**
             * Get the number of nodes at this point in the tree: count one
             * for this node and add the size of all its children.
             */
            int getSize() {
                int n = 1;
                if (children != null) {
                    for (int i = 0; i < children.length; i++)
                        n += children[i].getSize();
                }
                return n;
            }

            /**
             * Merge the contents of this node with another to produce
             * a new node.
             * @param other The node to be merged with this one.
             * @return a new Node, containing the merge of this one
             * and the specified node.
             */
            Node merge(Node other) {
                if (!other.name.equals(name))
                    throw new IllegalArgumentException(name + ":" + other.name);

                TreeMap mergedChildrenMap = new TreeMap();
                for (int i = 0; i < children.length; i++) {
                    Node child = children[i];
                    mergedChildrenMap.put(child.name, child);
                }
                for (int i = 0; i < other.children.length; i++) {
                    Node otherChild = other.children[i];
                    Node c = (Node) (mergedChildrenMap.get(otherChild.name));
                    mergedChildrenMap.put(otherChild.name,
                                      (c == null ? otherChild : otherChild.merge(c)));
                }
                Node[] mergedChildren =
                    (Node[]) (mergedChildrenMap.values().toArray(new Node[mergedChildrenMap.size()]));

                TestDescription[] mergedTests;
                if (tests.length + other.tests.length == 0)
                    mergedTests = noTests;
                else {
                    mergedTests = new TestDescription[tests.length + other.tests.length];
                    System.arraycopy(tests, 0, mergedTests, 0, tests.length);
                    System.arraycopy(other.tests, 0, mergedTests, tests.length, other.tests.length);
                }

                return new Node(name, mergedTests, mergedChildren);
            }

            /**
             * Write the contents of a node to a stream. First the name
             * is written, then the number of test descriptions, followed
             * by that many references to the test table, then the number
             * of child nodes, followed by that many child nodes in place.
             */
            void write(DataOutputStream o) throws IOException {
                o.writeUTF(name);
                writeInt(o, tests.length);
                for (int i = 0; i < tests.length; i++)
                    writeInt(o, testTable.getIndex(tests[i]));
                writeInt(o, children.length);
                for (int i = 0; i < children.length; i++)
                    children[i].write(o);
            }

            private String name;
            private TestDescription[] tests;
            private Node[] children;
        }
    }

}
