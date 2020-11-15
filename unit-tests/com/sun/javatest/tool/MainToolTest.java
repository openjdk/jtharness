/*
 * $Id$
 *
 * Copyright (c) 2001, 2020, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.javatest.tool;


import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.sun.javatest.JavaTestSecurityManager;
import com.sun.javatest.TestSuite;
import com.sun.javatest.TestUtil;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.exec.ExecTool;
import com.sun.javatest.functional.TestSuiteRunningTestBase;
import com.sun.javatest.util.ExitCount;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class MainToolTest {

    private Properties sysProps;
    private Method[] tests;
    private File basicTestSuite;
    private File currDir;

    {
        try {
            currDir = TestUtil.createTempDirectory("MainToolTest-").toFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @BeforeClass
    public static void initialize() {
        TestSuiteRunningTestBase.predefineStandardCoreJTHManagers();
    }

    @Test
    public void test() throws InterruptedException, InvocationTargetException {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("Headless graphics env - SKIPPING the test");
            return;
        }
        sysProps = System.getProperties();
        ExitCount.inc();
        JavaTestSecurityManager.setAllowExit(true);

        boolean ok = true;
        for (Method test : tests) {
            System.err.println("test " + test.getName());
            boolean b = test(test);
            System.err.println("test " + test.getName() + " " + (b ? "ok" : "failed"));
            ok = ok && b;
        }
        Assert.assertTrue(ok);
        System.err.println("MainToolTest completed successfully");
    }

    public MainToolTest() throws Exception {
        setup();
    }

    public void setup() throws Exception {
        String[] args = {
                "-basic",
                TestUtil.getAbsPathToTestTestSuite("demotck")
        };
        String[] testNames = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-basic") && i + 1 < args.length) {
                basicTestSuite = new File(args[++i]);
            } else if (args[i].startsWith("-")) {
                throw new Error("bad option: " + args[i]);
            } else {
                testNames = new String[args.length - i];
                System.arraycopy(args, i, testNames, 0, testNames.length);
                i = args.length;
            }
        }

        if (testNames == null) {
            tests = getTestMethods();
        } else {
            tests = getTestMethods(testNames);
        }

    }

    public boolean test(Method m) throws InterruptedException, InvocationTargetException {
        try {
            m.invoke(this, (Object[]) null);
            checkCurrDirEmpty();
            return true;
        } catch (InvocationTargetException e) {
            e.getTargetException().printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
    public boolean testNoArgsNoWhereOpenTS() throws Exception {
	sysProps.put("javatest.preferences.file", "NONE");
	sysProps.put("javatest.desktop.file", "NONE");
	startJavaTest(new String[] { });
	EventQueue.invokeAndWait(new Runnable() {
	    public void run() {
		Desktop d = Desktop.access();
		JDialog init = (JDialog) (getOwnedWindow(d, "exec.init"));
		checkInitialDialog(init, NEED_TESTSUITE);
		ExecTool t = (ExecTool) (getTool(d, "exec"));
		if (t == null)
		    throw new Error("exec tool missing");
		TestSuite ts = t.getTestSuite();
		checkTestSuite(ts, null);
		WorkDirectory wd = t.getWorkDirectory();
		checkWorkDir(wd, null);
		JButton btn = (JButton) (getComponent(init.getContentPane(), "exec.init.btns/exec.init.openTestSuite"));
		if (btn == null)
		    throw new Error("Open Test Suite button missing");
		btn.doClick();
	    }
	});
	return true;
    }
    */

    /*
    public boolean testNoArgsNoWhereOpenWD() throws Exception {
	sysProps.put("javatest.preferences.file", "NONE");
	sysProps.put("javatest.desktop.file", "NONE");
	startJavaTest(new String[] { });
	EventQueue.invokeAndWait(new Runnable() {
	    public void run() {
		Desktop d = Desktop.access();
		JDialog init = (JDialog) (getOwnedWindow(d, "exec.init"));
		checkInitialDialog(init, NEED_TESTSUITE);
		ExecTool t = (ExecTool) (getTool(d, "exec"));
		if (t == null)
		    throw new Error("exec tool missing");
		TestSuite ts = t.getTestSuite();
		checkTestSuite(ts, null);
		WorkDirectory wd = t.getWorkDirectory();
		checkWorkDir(wd, null);
		JButton btn = (JButton) (getComponent(init.getContentPane(), "exec.init.btns/exec.init.openWorkDir"));
		if (btn == null)
		    throw new Error("Open Work Dir button missing");
		btn.doClick();
	    }
	});
    }
    */


//    public void testNoArgsNoWhereCancel() throws Exception {
//        sysProps.put("javatest.preferences.file", "NONE");
//        sysProps.put("javatest.desktop.file", "NONE");
//        final Desktop d = startJavaTest(new String[]{});
//        EventQueue.invokeAndWait(new Runnable() {
//            public void run() {
//                JDialog init = (JDialog) getOwnedWindow(d, "exec.init");
//                checkInitialDialog(init, NEED_TESTSUITE);
//                ExecTool t = (ExecTool) getTool(d, "exec");
//                if (t == null)
//                    throw new Error("exec tool missing");
//                TestSuite ts = t.getTestSuite();
//                checkTestSuite(ts, null);
//                WorkDirectory wd = t.getWorkDirectory();
//                checkWorkDir(wd, null);
//                JButton btn = (JButton) getComponent(init.getContentPane(), "exec.init.btns/exec.init.cancel");
//                if (btn == null)
//                    throw new Error("Cancel button missing");
//                btn.doClick();
//                if (init.isShowing())
//                    throw new Error("Initial dialog still showing");
//                d.dispose();
//            }
//        });
//    }

//    public void testNoArgsNoWhereHelp() throws Exception {
//        sysProps.put("javatest.preferences.file", "NONE");
//        sysProps.put("javatest.desktop.file", "NONE");
//        final Desktop d = startJavaTest(new String[]{});
//        EventQueue.invokeAndWait(new Runnable() {
//            public void run() {
//                JDialog init = (JDialog) getOwnedWindow(d, "exec.init");
//                checkInitialDialog(init, NEED_TESTSUITE);
//                ExecTool t = (ExecTool) getTool(d, "exec");
//                if (t == null)
//                    throw new Error("exec tool missing");
//                TestSuite ts = t.getTestSuite();
//                checkTestSuite(ts, null);
//                WorkDirectory wd = t.getWorkDirectory();
//                checkWorkDir(wd, null);
//                JButton btn = (JButton) getComponent(init.getContentPane(), "exec.init.btns/exec.init.help");
//                if (btn == null)
//                    throw new Error("Help button missing");
//                btn.doClick();
//                d.dispose();
//            }
//        });
//    }
//
//    public void testNoArgsWorkDir() throws Exception {
//        checkCurrDirEmpty();
//        try {
//            TestSuite bts = TestSuite.open(basicTestSuite);
//            WorkDirectory bwd = WorkDirectory.create(currDir, bts);
//            sysProps.put("javatest.preferences.file", "NONE");
//            sysProps.put("javatest.desktop.file", "NONE");
//            final Desktop d = startJavaTest(new String[]{});
//            EventQueue.invokeAndWait(new Runnable() {
//                public void run() {
//                    JDialog init = (JDialog) getOwnedWindow(d, "exec.init");
//                    if (init != null)
//                        throw new Error("exec.init displayed and shouldn't be");
//
//                    ExecTool t = (ExecTool) getTool(d, "exec");
//                    if (t == null)
//                        throw new Error("exec tool missing");
//                    TestSuite ts = t.getTestSuite();
//                    checkTestSuite(ts, basicTestSuite);
//                    WorkDirectory wd = t.getWorkDirectory();
//                    checkWorkDir(wd, currDir);
//                    d.dispose();
//                }
//            });
//        } finally {
//            clearCurrDir();
//        }
//    }

    //    public void testNoArgsTestSuite() throws Exception {
//        File f = new File("testsuite.html");
//        Writer out = new BufferedWriter(new FileWriter(f));
//        out.write("dummy html file");
//        out.close();
//        try {
//            sysProps.put("javatest.preferences.file", "NONE");
//            sysProps.put("javatest.desktop.file", "NONE");
//            final Desktop d = startJavaTest(new String[]{});
//            EventQueue.invokeAndWait(new Runnable() {
//                public void run() {
//                    JDialog init = (JDialog) getOwnedWindow(d, "exec.init");
//                    checkInitialDialog(init, HAVE_TESTSUITE);
//                    ExecTool t = (ExecTool) getTool(d, "exec");
//                    if (t == null)
//                        throw new Error("exec tool missing");
//                    TestSuite ts = t.getTestSuite();
//                    checkTestSuite(ts, currDir);
//                    WorkDirectory wd = t.getWorkDirectory();
//                    checkWorkDir(wd, null);
//                    d.dispose();
//                }
//            });
//        } finally {
//            f.delete();
//        }
//    }
//
    public void testWorkDirArg() throws Exception {
        checkCurrDirEmpty();
        try {
            File toyWork = TestUtil.createTempDirectory("toyWork").toAbsolutePath().toFile();
            TestSuite bts = TestSuite.open(basicTestSuite);
            WorkDirectory bwd = WorkDirectory.create(toyWork, bts);
            sysProps.put("javatest.preferences.file", "NONE");
            sysProps.put("javatest.desktop.file", "NONE");
            final Desktop d = startJavaTest(new String[]{toyWork.getPath()});
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    JDialog init = (JDialog) getOwnedWindow(d, "qsw");
                    if (init != null) {
                        throw new Error("exec.init displayed and shouldn't be");
                    }

                    ExecTool t = (ExecTool) getTool(d, "exec");
                    if (t == null) {
                        throw new Error("exec tool missing");
                    }
                    TestSuite ts = t.getTestSuite();
                    checkTestSuite(ts, basicTestSuite);
                    WorkDirectory wd = t.getWorkDirectory();
                    checkWorkDir(wd, toyWork);
                    d.dispose();
                }
            });
        } finally {
            clearCurrDir();
        }
    }
//
//    public void testTestSuiteArg() throws Exception {
//        final File canonBTS = basicTestSuite.getCanonicalFile();
//        sysProps.put("javatest.preferences.file", "NONE");
//        sysProps.put("javatest.desktop.file", "NONE");
//        final Desktop d = startJavaTest(new String[]{basicTestSuite.getPath()});
//        EventQueue.invokeAndWait(new Runnable() {
//            public void run() {
//                JDialog init = (JDialog) getOwnedWindow(d, "exec.init");
//                checkInitialDialog(init, HAVE_TESTSUITE);
//                ExecTool t = (ExecTool) getTool(d, "exec");
//                if (t == null)
//                    throw new Error("exec tool missing");
//                TestSuite ts = t.getTestSuite();
//                checkTestSuite(ts, canonBTS);
//                WorkDirectory wd = t.getWorkDirectory();
//                checkWorkDir(wd, null);
//                d.dispose();
//            }
//        });
//    }

    public void testParamFileArg() {
        System.err.println("testParamFileArg not yet implemented");
    }

    public void testInterviewArg() {
        System.err.println("testInterviewArg not yet implemented");
    }

    public void testSavedDesktop() {
        System.err.println("testSavedDesktop not yet implemented");
    }

    private Method[] getTestMethods() {
        Method[] all = getClass().getDeclaredMethods();
        Vector<Method> v = new Vector<>();
        for (Method m : all) {
            if (m.getName().length() > 4
                    && m.getName().startsWith("test")
                    && m.getParameterTypes().length == 0
                    && m.getReturnType() == void.class) {
                v.addElement(m);
            }
        }
        Method[] testMethods = new Method[v.size()];
        v.copyInto(testMethods);
        return testMethods;
    }

    private Method[] getTestMethods(String[] names) throws NoSuchMethodException {
        Vector<Method> v = new Vector<>();
        for (String name : names) {
            Method m = getClass().getMethod(name);
            if (m.getReturnType() == void.class) {
                v.addElement(m);
            }
        }
        Method[] testMethods = new Method[v.size()];
        v.copyInto(testMethods);
        return testMethods;
    }

    private static final int HAVE_TESTSUITE = 1;
    private static final int NEED_TESTSUITE = 2;

    private Desktop startJavaTest(String[] args) {
        try {
            PrintWriter out = new PrintWriter(System.out);
            CommandContext ctx = new CommandContext(out);

            Main m = new Main();
            m.run(args, ctx);

            out.flush();

            return ctx.getDesktop();
        } catch (Throwable t) {
            t.printStackTrace();
            throw new Error("unexpected exception from JavaTest: " + t);
        }
    }

    private void checkInitialDialog(JDialog init, int type) {
        if (init == null) {
            throw new NullPointerException("can't find initial dialog");
        }
        JButton openTS = (JButton) getComponent(init.getContentPane(), "exec.init.btns/exec.init.openTestSuite");
        JButton openWD = (JButton) getComponent(init.getContentPane(), "exec.init.btns/exec.init.openWorkDir");
        JButton createWD = (JButton) getComponent(init.getContentPane(), "exec.init.btns/exec.init.createWorkDir");

        switch (type) {
            case HAVE_TESTSUITE:
                if (openTS != null) {
                    throw new Error("openTestSuite button displayed and shouldn't be");
                }
                if (createWD == null) {
                    throw new Error("createWorkDir button missing");
                }
                break;
            case NEED_TESTSUITE:
                if (openTS == null) {
                    throw new Error("openTestSuite button missing");
                }
                if (createWD != null) {
                    throw new Error("createWorkDir button displayed and shouldn't be");
                }
                break;
            default:
                throw new Error();
        }

        // this is always required
        if (openWD == null) {
            throw new Error("openWorkDir button missing");
        }

    }

    private void checkTestSuite(TestSuite ts, File root) {
        if (root == null) {
            if (ts != null) {
                throw new Error("test suite set unexpectedly");
            }
        } else {
            if (ts == null) {
                throw new Error("test suite not set");
            } else if (!ts.getRoot().equals(root)) {
                throw new Error("test suite set incorrectly; actual root=" + ts.getRoot() + "  expected root=" + root);
            }
        }
    }

    private void checkWorkDir(WorkDirectory wd, File root) {
        if (root == null) {
            if (wd != null) {
                throw new Error("work directory set unexpectedly");
            }
        } else {
            if (wd == null) {
                throw new Error("work directory not set");
            } else if (!wd.getRoot().equals(root)) {
                throw new Error("work directory set incorrectly; actual root=" + wd.getRoot() + "  expected root=" + root);
            }
        }
    }

    private void pause() {
        JOptionPane.showMessageDialog(null, "press OK to continue", "pause",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private static Component getComponent(Container p, String name) {
        int sep = name.indexOf('/');
        String n = sep == -1 ? name : name.substring(0, sep);
        for (int i = 0; i < p.getComponentCount(); i++) {
            Component c = p.getComponent(i);
            if (n.equals(c.getName())) {
                if (sep == -1) {
                    return c;
                } else {
                    return getComponent((Container) c, name.substring(sep + 1));
                }
            }
        }
        //System.err.println("Could not find " + n + " within " + p.getName());
        return null;
    }

    private static Window getOwnedWindow(Desktop d, String name) {
        System.err.println("getOwnedWindow (desktop), looking for " + name);
        JFrame[] frames = d.getFrames();
        for (JFrame frame : frames) {
            Window w = getOwnedWindow(frame, name);
            if (w != null) {
                //System.err.println("getOwnedWindow (desktop), found " + w);
                return w;
            }
        }
        System.err.println("getOwnedWindow (desktop), not found");
        return null;
    }

    private static Window getOwnedWindow(Window p, String name) {
        System.err.println("getOwnedWindow (" + p.getName() + "), looking for " + name);
        Window[] ownedWindows = p.getOwnedWindows();
        for (Window w : ownedWindows) {
            if (w.getName().equals(name)) {
                System.err.println("getOwnedWindow (" + p.getName() + "), found " + w);
                return w;
            }
        }
        System.err.println("getOwnedWindow (" + p.getName() + "), not found");
        return null;
    }

    private static Tool getTool(Desktop d, String name) {
        JFrame[] frames = d.getFrames();
        for (JFrame frame : frames) {
            Tool t = getTool(frame.getContentPane(), name);
            if (t != null) {
                return t;
            }
        }
        return null;
    }

    private static Tool getTool(Container p, String name) {
        int sep = name.indexOf('/');
        for (int i = 0; i < p.getComponentCount(); i++) {
            Component c = p.getComponent(i);
            if (c instanceof Tool) {
                String cn = c.getName();
                if (cn != null && (cn.equals(name) || cn.startsWith(name + ":"))) {
                    return (Tool) c;
                }
            } else if (c instanceof Container) {
                Tool t = getTool((Container) c, name);
                if (t != null) {
                    return t;
                }
            }
        }
        //System.err.println("Could not find " + n + " within " + p.getName());
        return null;
    }

    /**
     * Recursively copy a directory tree.
     */
    private void copyDir(File from, File to) throws IOException {
        if (!from.exists()) {
            throw new IOException("directory not found: " + from);
        }
        if (!to.exists() && !to.mkdirs()) {
            throw new IOException("could not create directory: " + to);
        }
        //System.err.println("copy " + from + " to " + to);
        String[] entries = from.list();
        if (entries != null) {
            for (String entry1 : entries) {
                File entry = new File(from, entry1);
                if (entry.isDirectory()) {
                    if (!entry1.equals("SCCS")) {
                        copyDir(entry, new File(to, entry1));
                    }
                } else if (entry.isFile()) {
                    copyFile(entry, new File(to, entry1));
                } else {
                    throw new IOException("unrecognized file: " + entry);
                }
            }
        }
    }


    /**
     * Copy a file.
     */
    private void copyFile(File from, File to) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(from));
        try {
            int size = (int) from.length();
            byte data[] = new byte[size];
            for (int total = 0; total < data.length; ) {
                total += in.read(data, total, data.length - total);
            }
            OutputStream out = new BufferedOutputStream(new FileOutputStream(to));
            try {
                out.write(data, 0, data.length);
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    @Before
    public void checkCurrDirEmpty() {
        String[] files = currDir.list();
        if (files != null && files.length > 0) {
            System.err.println("currDir: " + currDir);
            for (String file : files) {
                System.err.println(file);
            }
            throw new Error("current directory not empty");
        }
    }

    @Before
    @After
    public void clearCurrDir() {
        clearDir(currDir);
    }

    private static void clearDir(File dir) {
        String[] list = dir.list();
        if (list != null) {
            for (String aList : list) {
                File f = new File(dir, aList);
                if (f.isDirectory()) {
                    clearDir(f);
                }
                f.delete();
            }
        }

        String[] files = dir.list();
        if (files != null && files.length > 0) {
            for (String file : files) {
                if (file.startsWith(".nfs")) {
                    File fOld = new File(dir, file);
                    File fNew = new File(dir.getParentFile(), file);
                    if (fOld.renameTo(fNew)) {
                        System.err.println("rename from " + fOld);
                        System.err.println("rename to   " + fNew);
                    }
                }
            }
        }

        files = dir.list();
        if (files != null && files.length > 0) {
            //System.err.println("dir: " + dir);
            for (String file : files) {
                System.err.println(file);
            }
            throw new Error("directory not empty: " + dir);
        }
    }

    private void delete(File f) {
        if (f.isDirectory()) {
            String[] files = f.list();
            if (files != null) {
                for (String file : files) {
                    delete(new File(f, file));
                }
            }
        }
        f.delete();
    }

    private static void exit(int n) {
        // If the JT security manager is installed, it won't allow a call of
        // System.exit unless we ask it nicely, pretty please, thank you.
        SecurityManager sc = System.getSecurityManager();
        if (sc instanceof JavaTestSecurityManager) {
            JavaTestSecurityManager.setAllowExit(true);
        }
        System.exit(n);
    }


}
