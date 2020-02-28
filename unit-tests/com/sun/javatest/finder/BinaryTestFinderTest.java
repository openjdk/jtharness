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
package com.sun.javatest.finder;

import java.io.File;
import java.io.PrintStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;

import com.sun.javatest.TU;
import com.sun.javatest.TestDescription;
import com.sun.javatest.TestFinder;
import com.sun.javatest.TestFinderQueue;
import org.junit.Assert;
import org.junit.Test;

public class BinaryTestFinderTest {

    public class Fault extends Exception {
        Fault(String msg) {
            super(msg);
        }
    }

    @Test
    public void testDemoTCK() throws IOException, Fault {
        boolean ok;
        BinaryTestFinderTest t = new BinaryTestFinderTest();
        Path absTmpPath = Paths.get(System.getProperty("build.tmp")).toAbsolutePath().normalize();
        String workDir = Files.createTempDirectory(absTmpPath, "BinaryTestFinderTestWorkDir_demotck").toAbsolutePath().toString();
        String testSuiteHtml = TU.getPathToTestTestSuite("demotck") + File.separator + "testsuite.html";
        ok = t.run(System.out, 11, testSuiteHtml, workDir);
        Assert.assertTrue(ok);
    }

    @Test
    public void testIniturlTCK() throws IOException, Fault {
        boolean ok;
        BinaryTestFinderTest t = new BinaryTestFinderTest();
        Path absTmpPath = Paths.get(System.getProperty("build.tmp")).toAbsolutePath().normalize();
        String workDir = Files.createTempDirectory(absTmpPath, "BinaryTestFinderTestWorkDir_initurl").toAbsolutePath().toString();
        String testSuiteHtml = TU.getPathToTestTestSuite("initurl") + File.separator + "testsuite.html";
        ok = t.run(System.out, 11, testSuiteHtml, workDir);
        Assert.assertTrue(ok);
    }

    @Test
    public void testSimpleHTMLTCK() throws IOException, Fault {
        boolean ok;
        BinaryTestFinderTest t = new BinaryTestFinderTest();
        Path absTmpPath = Paths.get(System.getProperty("build.tmp")).toAbsolutePath().normalize();
        String workDir = Files.createTempDirectory(absTmpPath, "BinaryTestFinderTestWorkDir_simplehtml").toAbsolutePath().toString();
        String testSuiteHtml = TU.getPathToTestTestSuite("simplehtml") + File.separator + "tests" + File.separator + "testsuite.html";
        ok = t.run(System.out, 17, testSuiteHtml, workDir);
        Assert.assertTrue(ok);
    }

    public boolean run(PrintStream log, int expectedTotalTestNumber, String... args) throws Fault {
        File testSuite;
        File testWorkDir;
        File binaryFile;

        if (args.length != 2) {
            log.println("Wrong # args (expected 2, got " + args.length + ")");
            return false;
        }
        testSuite = new File(args[0]);
        testWorkDir = new File(args[1]);
        binaryFile = new File(testWorkDir, "testsuite.jtd");

        try {
            log.println("basic test suite: " + testSuite.getPath());
            log.println("work: " + testWorkDir.getPath());
            log.println("binary: " + binaryFile.getPath());

            if (!testWorkDir.exists())
                testWorkDir.mkdirs();

            String[] btwArgs = {
                    "-finder", "com.sun.javatest.finder.HTMLTestFinder", "-dirWalk", "-end",
                    "-o", binaryFile.getCanonicalPath(),
                    testSuite.getPath()
            };
            BinaryTestWriter m = new BinaryTestWriter();
            m.run(btwArgs);

            log.println("Created binary file at: " + binaryFile.getCanonicalPath());
        } catch (IOException e) {
            log.println("A problem occurred");
            log.println(e.toString());
            return false;
        } catch (BinaryTestWriter.BadArgs f) {
            log.println(f.getMessage());
            return false;
        } catch (BinaryTestWriter.Fault f) {
            log.println(f.getMessage());
            return false;
        }

        HTMLTestFinder htmlTestFinder = initializeHTMLTestFinder(testSuite);

        BinaryTestFinder binaryTestFinder = initializeBTF(testSuite, binaryFile.getPath());
        binaryTestFinder.readBinaryFile();
        Assert.assertEquals(expectedTotalTestNumber, binaryTestFinder.totalNumberOfTestsInTheSuite().get().intValue());

        Map<String, DiffRecord> table = new HashMap<>();

        addTestsToTable(table, htmlTestFinder, binaryTestFinder);

        log.println("Comparing all tests");
        int diff;
        for (DiffRecord record : table.values()) {
            record.compare();
            diff = record.getDiff();
            if (diff != 0) {
                log.println("Found a different test: " + record.getPath() + " (" + diff + ")");
                if (record.diffParam != null)
                    log.println("-- " + record.diffParam);
                if (record.diffFile != null)
                    log.println("-- " + record.diffFile);
                return false;
            }
        }
        log.println("all done!");
        return true;
    }


    /*
     * Loops though each of the TestFinders and adds to the hashtable. Tests
     * with the same URL are added to the same DiffRecord class.
     */

    private void addTestsToTable(Map<String, DiffRecord> table,
                                 HTMLTestFinder tf1, BinaryTestFinder tf2) {
        TestDescription td;
        int count = 0;

        String[] tests = {""};

        TestFinderQueue tfq = new TestFinderQueue();

        tfq.setTestFinder(tf1);
        tfq.setTests(tests);

        while ((td = tfq.next()) != null) {
            count++;
            DiffRecord temp = new DiffRecord();
            table.put(td.getRootRelativeURL(), temp);
            temp.addLeft(td);
            System.err.println("found left: " + td.getRootRelativeURL());
        }

        System.out.println("Found left tests: " + count);

        TestFinderQueue tfq2 = new TestFinderQueue();
        tfq2.setTestFinder(tf2);
        tfq2.setTests(tests);

        count = 0;
        while ((td = tfq2.next()) != null) {
            count++;
            DiffRecord temp = table.get(td.getRootRelativeURL());
            if (temp == null) {
                temp = new DiffRecord();
                table.put(td.getRootRelativeURL(), temp);
            }
            temp.addRight(td);
            System.err.println("found right: " + td.getRootRelativeURL());
        }
        System.out.println("Found right tests: " + count);
    }


    private HTMLTestFinder initializeHTMLTestFinder(File ts) throws Fault {
        String[] args = {"-dirWalk"};

        HTMLTestFinder testFinder = new HTMLTestFinder();

        try {
            testFinder.init(args, ts, null);
        } catch (TestFinder.Fault e) {
            throw new Fault("Error: Can't initialize test-finder: " + e.getMessage());
        }
        System.out.println("Created HTMLTestFinder");
        return testFinder;
    }


    private BinaryTestFinder initializeBTF(File ts, String binary) throws Fault {
        BinaryTestFinder tf = new BinaryTestFinder();
        try {
            String[] args = {"-binary", binary};
            tf.init(args, ts, null);
        } catch (TestFinder.Fault e) {
            throw new Fault("Error: Can't initialize test-finder: " + e.getMessage());
        }
        System.out.println("Created BinaryTestFinder");
        return tf;
    }


    /*
     * gets the test suite file. Adds testsuite.html or tests/testsuite.html
     * to the end of the path if necessary.
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
                    throw new Fault("Bad input. " + file + " is not a test suite");
            }
        }
    }

    private int decodeOption(String option) {
        if (option.equals("-showSame"))
            return 0;
        if (option.equals("-showLeft"))
            return 1;
        if (option.equals("-showRight"))
            return 2;
        if (option.equals("-showDiffs"))
            return 3;
        if (option.equals("-showRightDiffs"))
            return 6;
        if (option.equals("-showLeftDiffs"))
            return 5;
        if (option.equals("-showAllDiffs"))
            return 4;
        return -1;
    }

    static class DiffRecord {
        TestDescription left, right;
        Hashtable<String, String> leftParams, rightParams;
        int diff;
        String diffParam;
        String diffFile;

        public DiffRecord() {
            left = right = null;
            leftParams = rightParams = null;
            diff = 0;
        }

        public void addLeft(TestDescription td) {
            leftParams = new Hashtable<>();

            left = td;
            for (Iterator<String> i = left.getParameterKeys(); i.hasNext(); ) {
                String param = i.next();
                leftParams.put(param, left.getParameter(param));
            }
        }

        public void addRight(TestDescription td) {
            rightParams = new Hashtable<>();

            right = td;
            for (Iterator<String> i = right.getParameterKeys(); i.hasNext(); ) {
                String param = i.next();
                rightParams.put(param, right.getParameter(param));
            }
        }

        public String getPath() {
            if (left == null)
                return right.getRootRelativeFile().getPath();
            else
                return left.getRootRelativeFile().getPath();
        }


        public TestDescription getRight() {
            return right;
        }

        public TestDescription getLeft() {
            return left;
        }


        public int getDiff() {
            return diff;
        }


        public void compare() {
            if (left == null) {
                diff = 1;
                return;
            }
            if (right == null) {
                diff = 2;
                return;
            }

            if (compareParameters())
                return;

            compareSources();
            return;
        }

        /* Compares source files. Checks if same number of files. If true, then loops through each of the files and
         * compares them between each test suite.
         */

        private void compareSources() {
            File[] files1 = left.getSourceFiles();
            File[] files2 = right.getSourceFiles();
            if (files1.length != files2.length) {
                diff = 4;
                diffFile = "Different number of files";
                return;
            }

            for (File aFiles1 : files1) {
                boolean found = false;
                for (File aFiles2 : files2)
                    if (aFiles1.getName().equals(aFiles2.getName()) && compareFile(aFiles1, aFiles2))
                        found = true;
                if (!found) {
                    diff = 4;
                    diffFile = aFiles1.getPath();
                    return;
                }
            }
        }

        /* COmpares 2 given files. Reads from them line by line and amkes sure that every line is the same.
         */

        private static boolean compareFile(File file1, File file2) {
            String line1, line2;
            BufferedReader reader1, reader2;

            try {
                reader1 = new BufferedReader(new FileReader(file1));
                reader2 = new BufferedReader(new FileReader(file2));
            } catch (FileNotFoundException f) {
                System.err.println("Error creating file reader");
                return false;
            }

            try {
                while ((line1 = reader1.readLine()) != null) {
                    if ((line2 = reader2.readLine()) == null)
                        return false;
                    if (!line1.equals(line2))
                        return false;
                }
                if (reader2.readLine() == null)
                    return true;
            } catch (IOException i) {
                System.err.println("error reading from buffered readers");
            }
            return false;

        }

        /* compares parameters. FIrst makes sure each test has same number of parameters. If it does, it loops
         * through each of them and compares the values for eahc of them.
         */

        private boolean compareParameters() {

            if (leftParams.size() != rightParams.size()) {
                diff = 3;
                diffParam = "Different number of parameters";
                return true;
            }

            for (Enumeration<String> e = leftParams.keys(); e.hasMoreElements(); ) {
                String param = e.nextElement();
                if (!rightParams.containsKey(param) || !left.getParameter(param).equals(right.getParameter(param))) {
                    diff = 3;
                    diffParam = param;
                    return true;
                }
            }

            return false;
        }
    }
}



