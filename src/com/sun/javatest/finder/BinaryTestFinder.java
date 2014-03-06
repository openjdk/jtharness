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
package com.sun.javatest.finder;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.sun.javatest.TestDescription;
import com.sun.javatest.TestEnvironment;
import com.sun.javatest.TestFinder;
import com.sun.javatest.util.I18NResourceBundle;


/**
 * A TestFinder to read a compressed binary file containing the
 * previously "compiled" results of some other test finder.
 */

public class BinaryTestFinder extends TestFinder
{
    /**
     * Create an uninitialized binary test finder. Use one of the init
     * methods to initialize it.
     */
    public BinaryTestFinder() { }

    /**
     * Create and initialize a binary test finder.
     * @param jtdFile   The binary file containing the precompiled test descriptions.
     *                  The file should be an absolute file.
     * @throws TestFinder.Fault
     *                  if there is a problem while reading the data file.
     * @deprecated Use BinaryTestFinder(File testSuiteRoot, File jtdFile) instead
     * @see #BinaryTestFinder(File, File)
     */
    public BinaryTestFinder(File jtdFile) throws Fault {
        this.jtdFile = jtdFile;
        openBinaryFile(true);
    }

    /**
     * Create and initialize a binary test finder.
     * @param jtdFile   The binary file containing the precompiled test descriptions.
     *                  If the file is relative, it will be evaluated relative to testSuiteRoot.
     * @param testSuiteRoot The root file of the Test Suite.
     *                  This is usually a file called testsuite.html
     * @throws TestFinder.Fault
     *                  if there is a problem while reading the data file.
     * @deprecated Use BinaryTestFinder(File, File, TestEnvironment) with null as
     *    the last argument.
     */
    public BinaryTestFinder(File testSuiteRoot, File jtdFile) throws Fault {
        init(testSuiteRoot, jtdFile);
    }

    /**
     * Initialize a new BinaryTestFinder. This method opens the binary file to read
     *          from, calls methods to read the data, and initializes other variables.
     *
     * @param args      Any arguments needed by the TestFinder. You must pass "-binary"
     *                  followed by the path to the binary file. If the file is relative,
     *                  it will be evaluated relative to testSuiteRoot.
     * @param testSuiteRoot The root file of the Test Suite.
     *                  This is usually a file called testsuite.html
     * @param env       Environment file for the TestFinder. Not used by BinaryTestFinder.
     * @throws TestFinder.Fault
     *                  if there is a problem while reading the data file.
     */
    public void init(String[] args, File testSuiteRoot, TestEnvironment env) throws Fault {
        super.init(args, testSuiteRoot, env);
        openBinaryFile(true);
    }

    /**
     * Initialize a new BinaryTestFinder. This method opens the binary file to read
     *          from, calls methods to read the data, and initializes other variables.
     *
     * @param testSuiteRoot The root file of the Test Suite.
     *                  This is usually a file called testsuite.html
     * @param jtdFile   The file containing the binary data for the test descriptions.
     *                  If the file is relative, it will be evaluated relative to testSuiteRoot.
     * @throws TestFinder.Fault
     *                  if there is a problem while reading the data file.
     * @deprecated Use init(File, File TestEnvironment) with null args as needed.
     */
    public void init(File testSuiteRoot, File jtdFile) throws Fault {
        super.init(null, testSuiteRoot, null);
        this.jtdFile = jtdFile;
        openBinaryFile(true);
    }

    /**
     * Decodes any args needed by BinaryTestFinder. The only supported args
     * are "-binary" or "-jtd" followed by the name of the binary file,
     * or the name of the binary file as the last argument.
     * @param args      An array of arguments, containing the next argument to be
     *                  decoded.
     * @param i         The position in the args array of the next argument to be
     *                  decoded.
     * @return          The number of entries in the args array that were
     *                  taken as part of the next argument.
     * @throws TestFinder.Fault
     *                  if any problems occur while decoding the next argument
     */
    protected int decodeArg(String[] args, int i) throws Fault {
        if (args[i].equalsIgnoreCase("-binary") || args[i].equalsIgnoreCase("-jtd")) {
            String e = args[i+1];
            jtdFile = new File(e);
            return 2;
        }
        else if (i == args.length - 1 &&  !args[i].startsWith("-")) {
            jtdFile = new File(args[i]);
            return 1;
        }
        else
            return super.decodeArg(args, i);
    }

    public long lastModified(File f) {
        // with BTF, the file will never change, so rescanning should never be
        // needed.  And, after the first scan, 1 will always be earlier than the
        // current date, so rescanning won't be triggered again.
        return 1;
    }

    public boolean isFolder(File path) {
        if (zipFile != null || zipFileRead)
            readBinaryFile();

        File rootDir = getRootDir();
        String relPath = getRelativePath(rootDir, path);
        TestTree.Node node = (testTree == null ? null : testTree.getNode(relPath));

        /*
        System.out.println("isFolder() for " + path.getPath() + " = " +
            (node == null ? false : true));
        */

        // temporary fix which will handle most TCKs
        // need to find algorithm to determine if the path is a test in
        // a leightweight manner
        if (path.getPath().endsWith(".html") ||
            path.getPath().endsWith(".java") ||
            path.getPath().endsWith(".xml"))
            return false;

        if (node != null)
            return true;
        else
            return false;
    }

    /**
     * Given a File, scan to look for other files or tests. These can
     * then be used through getTests() and getFiles().
     *
     * This method first takes the file path and makes it relative to the testsuite
     * root. It then finds the corresponding node in the test tree, and calls
     * foundFile and foundTestDescription based on the info in that node, thereby
     * recreating the effect of the original read of this file by the original
     * test finder.
     */
    protected void scan(File file) {
        if (zipFile != null || zipFileRead)
            readBinaryFile();

        try {
            // locate the node for this file
            File rootDir = getRootDir();
            String relPath = getRelativePath(rootDir, file);
            TestTree.Node node = (testTree == null ? null : testTree.getNode(relPath));

            if (node == null) {
                error(i18n, "bin.cantFindPath", file.getPath());
                return;
            }

            // simulate files to be scanned for child nodes
            TestTree.Node[] children = node.children;
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    File f = (relPath.length() == 0 ? new File(children[i].name) :
                                            new File(relPath, children[i].name));
                    foundFile(f);
                }
            }

            // regenerate test descriptions
            if (node.testIndexes != null) {
                File testFile = new File(relPath);
                for (int i = 0; i < node.testIndexes.length; i++) {
                    TestDescription td = node.getTest(i, rootDir, testFile, testTable);
                    foundTestDescription(td);
                }
            }
        }
        catch (IOException e) {
            error(i18n, "bin.internalIOError", new Object[] {file.getPath(), e});
        }
    }

    /**
     * Create the relative path of a filename relative to a root directory.
     * @throws IllegalArgumentException is the file is not under the root
     * directory.
     */
    private String getRelativePath(File rootDir, File file) {
        if (file.isAbsolute()) {
            String rootDirPath = rootDir.getPath();
            String filePath = file.getPath();
            if (filePath.startsWith(rootDirPath)) {
                return (filePath.equals(rootDirPath)
                        ? ""
                        : filePath.substring(rootDirPath.length() + 1));
            }
            else
                throw new IllegalArgumentException();
        }
        else
            return file.getPath();
    }

    /**
     * Read the binary file. The input file should be a zip file as written by
     * BinaryTestWriter. All three sections are reed, but only the string table
     * and test tree are parsed at this point. The test table is read from an
     * internal byte array as required.
     * @param closeIfSuccess if set to <b>true</b>, binary file will be closed
     * after necessary integrity checks; if set to <b>false</b> binary file
     * remains opened (and probably locked by OS).
     */
    private void openBinaryFile(boolean closeIfSuccess) throws Fault {
        try {
            zipFileRead = false;
            if (jtdFile == null)
                throw new Fault(i18n, "bin.noFile");

            // open these all here to take the hit of exceptions
            // as early as possible
            File root = getRoot();
            File f = (jtdFile.isAbsolute() || root == null ? jtdFile : new File(root, jtdFile.getPath()));
            zipFile = new ZipFile(f);
            stringsEntry = zipFile.getEntry("strings");
            testsEntry = zipFile.getEntry("tests");
            treeEntry = zipFile.getEntry("tree");

            if (stringsEntry == null || testsEntry == null || treeEntry == null)
                throw new Fault(i18n, "bin.badBinFile", zipFile.getName());
            zipFileRead = true;
        }
        catch (FileNotFoundException e) {
            throw new Fault(i18n, "bin.cantFindFile", jtdFile.getPath());
        }
        catch (IOException e) {
            throw new Fault(i18n, "bin.ioError", new Object[] {jtdFile.getPath(), e});
        } finally {
            if (closeIfSuccess || !zipFileRead) {
                if (zipFile != null) {
                    try {
                        zipFile.close();
                    } catch (IOException e) {
                        throw new Fault(i18n, "bin.ioError", new Object[] {
                                jtdFile.getPath(), e });
                    }
                }
                stringsEntry = null;
                testsEntry = null;
                treeEntry = null;
                zipFile = null;
            }
        }
    }

    /**
     * Read the binary file. The input file should be a zip file as written by
     * BinaryTestWriter. All three sections are reed, but only the string table
     * and test tree are parsed at this point. The test table is read from an
     * internal byte array as required.
     */
    private synchronized void readBinaryFile() {
    if (zipFile == null && zipFileRead) {
        try {
            openBinaryFile(false);
        } catch (Fault ex) {
            localizedError(ex.getLocalizedMessage());
            zipFile = null;
        }
    }
        if (zipFile == null)
            throw new IllegalStateException();

        try {
            stringTable = StringTable.read(zipFile, stringsEntry);
            testTable = TestTable.read(zipFile, testsEntry, stringTable);
            testTree = TestTree.read(zipFile, treeEntry);
            zipFileRead = false;
        }
        catch (Fault e) {
            error(i18n, "bin.fault", new Object[] {jtdFile.getPath(), e});
        }
        catch (IOException e) {
            error(i18n, "bin.ioError", new Object[] {jtdFile.getPath(), e});
        }
        finally {
            try {
                zipFile.close();
            }
            catch (IOException e) {
                error(i18n, "bin.ioError", new Object[] {jtdFile.getPath(), e});
            }
            stringsEntry = null;
            testsEntry = null;
            treeEntry = null;
            zipFile = null;
        }
    }

    //------------------------------------------------------------------------------------------

    /**
     * Read an int from a stream using a variable length encoding.
     * The integer is read in 7 bit chunks in big-endian order, with
     * the top bit in each byte indicating more data to be read.
     */
    private static int readInt(DataInputStream in) throws IOException {
        int n = 0;
        int b;
        while ((b = in.readUnsignedByte()) >= 0x80)
            n = (n << 7) | (b & 0x7f);
        n = (n << 7) | b;
        return n;
    }

    //------------------------------------------------------------------------------------------

    private File jtdFile;
    private ZipFile zipFile;
    private ZipEntry stringsEntry;
    private ZipEntry testsEntry;
    private ZipEntry treeEntry;
    private boolean zipFileRead;
    private StringTable stringTable;
    private TestTable testTable;
    private TestTree testTree;

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(BinaryTestFinder.class);

    //------------------------------------------------------------------------------------------

    /**
     * A StringTable is an array of strings. References to these strings
     * are written either as an index into the string array, or as zero
     * followed by the string inline.
     * @see BinaryTestWriter.StringTable
     */
    static class StringTable {
        /**
         * Read a string table from a entry called "strings" in a zip file.
         */
        static StringTable read(ZipFile zf, ZipEntry ze) throws IOException, Fault {
            DataInputStream in = new DataInputStream(new BufferedInputStream(zf.getInputStream(ze)));
            return new StringTable(in);
        }

        /**
         * Read a string table from a stream.
         */
        StringTable(DataInputStream in) throws IOException {
            int count = readInt(in);
            strings = new String[count];
            for (int i = 0; i < count; i++)
                strings[i] = in.readUTF();
        }

        /**
         * Read a reference to a string in the string table. It will either
         * be the index of an entry in the table, or 0 followed by the inline
         * string, if the string is not to be found in the table.
         */
        String readRef(DataInputStream in) throws IOException {
            int index = readInt(in);
            if (index == 0)
                return in.readUTF();
            else
                return strings[index];
        }

        /**
         * Get a string from the table.
         */
        String get(int i) {
            return strings[i];
        }

        private String[] strings;
    }

    //------------------------------------------------------------------------------------------

    /**
     * A TestTable is a collection of TestDescriptions.
     */
    static class TestTable {
        /**
         * Read a test table from an entry called "tests" in a zip file.
         * The data of the entry is read and stored, but only analyzed
         * as required.
         */
        static TestTable read(ZipFile zf, ZipEntry ze, StringTable stringTable) throws IOException, Fault {
            byte[] bytes = new byte[(int) (ze.getSize())];
            InputStream in = zf.getInputStream(ze);
            for (int total = 0; total < bytes.length; ) {
                total += in.read(bytes, total, bytes.length - total);
            }
            return new TestTable(bytes, stringTable);
        }

        /**
         * Create a TestTable from a byte array as written by BinaryTestWriter.
         * The data of the entry is stored, and analyzed as required.
         */
        TestTable(byte[] data, StringTable stringTable) {
            bais = new ByteArrayInputStream(data);
            this.stringTable = stringTable;
        }

        /**
         * Get a test description from the table. It is reconstituted from the
         * data given when the table was created. The root and file for the
         * test description must be supplied separately; the index is a byte
         * offset within the data of where the test description data is to be found.
         * The test description data is encoded as a count, and then that many
         * name value pairs to be put in the test description.
         */
        TestDescription get(File root, File file, int index) throws IOException {
            bais.reset();
            bais.skip(index);
            Map m = new HashMap();
            DataInputStream in = new DataInputStream(bais);
            int n = readInt(in);
            for (int i = 0; i < n; i++) {
                String key = stringTable.readRef(in);
                String value = stringTable.readRef(in);
                m.put(key, value);
            }
            return new TestDescription(root, file, m);
        }

        private ByteArrayInputStream bais;
        private StringTable stringTable;
    }

    //------------------------------------------------------------------------------------------

    /**
     * A TestTree represents a tree of test descriptions; the data of the individual
     * descriptions is stored in a test table.
     */
    static class TestTree {
        /**
         * Read a test tree from an entry called "tree" in a zip file.
         */
        static TestTree read(ZipFile zf, ZipEntry ze) throws IOException, Fault {
            DataInputStream in = new DataInputStream(new BufferedInputStream(zf.getInputStream(ze)));
            Node root = new Node(in);
            return new TestTree(root);
        }

        /**
         * Create a test tree from a root node.
         */
        TestTree(Node root) {
            this.root = root;
        }

        /**
         * Get the node for a path within the tree.
         */
        Node getNode(String path) {
            return (path.length() == 0 ? root : root.getNode(path));
        }

        private Node root;

        /**
         * A node within the test tree. It has a name, a set of indices that can be
         * used to find the test descriptions with the test table, and a set of child
         * nodes.
         */
        static class Node {
            /**
             * Read a node from an input stream. The node is represented as
             * a name, a count, followed by that many test indexes into the
             * corresponding test table, and a count, followed (recursively)
             * by that many child nodes.
             */
            Node(DataInputStream in) throws IOException {
                name = in.readUTF();
                int testCount = readInt(in);
                if (testCount > 0) {
                    testIndexes = new int[testCount];
                    for (int i = 0; i < testIndexes.length; i++)
                        testIndexes[i] = readInt(in);
                }
                int childCount = readInt(in);
                if (childCount > 0) {
                    children = new Node[childCount];
                    for (int i = 0; i < children.length; i++)
                        children[i] = new Node(in);
                }
            }

            /**
             * Get a child node as indicated by a relative path.
             */
            Node getNode(String path) {
                int sep = path.indexOf(File.separatorChar);
                String head = (sep == -1 ? path : path.substring(0, sep));
                for (int i = 0; i < children.length; i++) {
                    Node child = children[i];
                    if (child.name.equals(head))
                        return (sep == -1 ? child : child.getNode(path.substring(sep + 1)));
                }
                // not found
                return null;
            }

            /**
             * Get a TestDescription from this node, using the data in a
             * test table.
             */
            TestDescription getTest(int index, File root, File path, TestTable testTable) throws IOException {
                if (testIndexes == null || index > testIndexes.length)
                    throw new IllegalArgumentException();
                return testTable.get(root, path, testIndexes[index]);
            }

            private String name;
            private int[] testIndexes;
            private Node[] children;
        }
    }
}



