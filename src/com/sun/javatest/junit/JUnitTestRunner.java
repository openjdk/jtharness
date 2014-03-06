/*/*
 * $Id$
 *
 * Copyright (c) 2008, 2011, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.javatest.junit;

import com.sun.javatest.Status;
import com.sun.javatest.TestDescription;
import com.sun.javatest.TestResult;
import com.sun.javatest.TestRunner;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.lib.MultiTest;
import com.sun.javatest.util.BackupPolicy;
import java.io.IOException;
import java.util.Iterator;

/**
 *
 */
public class JUnitTestRunner extends TestRunner {

    /** Creates a new instance of JUnitTestRunner */
    public JUnitTestRunner() {
        // could create additional constructors to accept args
        // might be useful if there are more settings that need to be passed
        // to the MultiTest class.  This class is generally constructed
        // by the corresponding TestSuite class.
    }

    protected boolean runTests(Iterator testIter) throws InterruptedException {
        WorkDirectory wd = getWorkDirectory();
        TestDescription td = null;
        //for (TestDescription td: testIter) {
        for (; testIter.hasNext() ;) {
            td = (TestDescription)(testIter.next());
            TestResult tr = new TestResult(td);
            TestResult.Section outSection = tr.createSection("Main");

            notifyStartingTest(tr);
            Status execStatus = getMultiTest(td).run(getTestArgs(td), outSection.createOutput("stdout"), outSection.createOutput("stderr"));
            tr.setStatus(execStatus);

            try {
                if (execStatus.getType() != Status.PASSED || jtrIfPassed)
                    tr.writeResults(wd, backupPolicy);
            } catch (IOException e) {
                // ignore it; the test will have an error status already
                // could log the error using the logging system
            }

            notifyFinishedTest(tr);
        }   // for

        return false;
    }

    public BackupPolicy getBackupPolicy() {
        return backupPolicy;
    }

    void setClassLoader(ClassLoader loader) {
        this.loader = loader;

        try {
            Class c = loader.loadClass("com.sun.javatest.junit.JUnitMultiTest");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the test class that should be used to execute this test.
     * The default implementation returns a JUnitBareMultiTest or
     * JUnitAnnotationMultiTest depending on the <tt>junit.finderscantype</tt>
     * value in the test description.
     *
     * Override this method if you wish to return a <tt>JUnitMultiTest</tt> of
     * your own.
     *
     * @see com.sun.javatest.junit.JUnitMultiTest
     */
    protected MultiTest getMultiTest(TestDescription td) {
        String type = td.getParameter("junit.finderscantype");
        JUnitMultiTest mt = null;

        if (type != null) {
            if (type.equals("superclass")) {
                //mt = new JUnitMultiTest();
                mt = new JUnitBareMultiTest(loader);
            } else {
                mt = new JUnitAnnotationMultiTest(loader);
            }
        }

        return mt;
    }

    // could make this protected so that custom test runners could easily
    // add args without changing other code
    private String[] getTestArgs(TestDescription td) {
        return new String[] {td.getParameter("executeClass")};
    }

    protected BackupPolicy backupPolicy = BackupPolicy.noBackups(); // optional
    protected ClassLoader loader;
    // the name of the setting is legacy, but we provide equivalent behavior
    private boolean jtrIfPassed =
            System.getProperty("javatest.script.jtrIfPassed", "true").equals("true");
}
