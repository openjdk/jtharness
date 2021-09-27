/*
 * $Id$
 *
 * Copyright (c) 2001, 2021, Oracle and/or its affiliates. All rights reserved.
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


package jthtest;

import com.sun.javatest.TestResult;
import jthtest.menu.Menu;
import org.junit.After;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.operators.Operator.StringComparator;
import org.netbeans.jemmy.util.NameComponentChooser;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Tools {

    @Deprecated
    public static final String TEMP_WD_NAME = "javatest_guitest_demowd";
    @Deprecated
    public static final String DEFAULT_WD_NAME = "demowd_config";
    @Deprecated
    public static final String WD_RUN_NAME = "demowd_run";
    @Deprecated
    public static final String TEST_SUITE_NAME = "demots";
    @Deprecated
    public static final String TEMPLATE_NAME = "demotemplate.jtm";
    @Deprecated
    public static final String REPORT_NAME = "demoreport";
    @Deprecated
    public static final String CONFIG_NAME = "democonfig.jti";
    @Deprecated
    public static final String TESTS_DIRECTORY_PREFIX = "tests" + File.separator;
    @Deprecated
    public static final String NEWDESKTOP_ARG = "-newdesktop";
    @Deprecated
    //public static final String WINDOWNAME = System.getProperty("jt_gui_test.name");
    public static final String WINDOWNAME = "JT";
    @Deprecated
    public static final String TESTSUITENAME = "DemoTS 1.0 Test Suite (Tag Tests)";
    public static final int MAX_WAIT_TIME = 20000;
    @Deprecated
    public static String TEMP_PATH;
    @Deprecated
    public static String LOCAL_PATH;
    @Deprecated
    public static String DEFAULT_PATH;
    @Deprecated
    public static String USER_HOME_PATH;
    private static ResourceBundle i18nExecResources;    // reading resources exacly from the javatest.jar to not to do mistakes in element's names
    private static ResourceBundle i18nToolResources;  // reading resources of dialog boxes exacly from the javatest.jar to not to do mistakes in element's names
    private static LinkedList<File> usedFiles = new LinkedList<File>();

    static {
        JemmyProperties.getCurrentTimeouts().setTimeout("ComponentOperator.WaitComponentTimeout", MAX_WAIT_TIME);
        JemmyProperties.setCurrentOutput(new TestOut(null, (PrintWriter) null, null));

        i18nExecResources = ResourceBundle.getBundle("com.sun.javatest.exec.i18n");
        i18nToolResources = ResourceBundle.getBundle("com.sun.javatest.tool.i18n");

        String temp = System.getProperty("user.dir") + File.separator;
        if (temp == null || "".equals(temp)) {
            File tmp = new File("");
            temp = tmp.getAbsolutePath();
        }
        DEFAULT_PATH = LOCAL_PATH = temp;


//        StringBuffer tempPath = new StringBuffer();
//        try {
//            File tmp = File.createTempFile("xxxxxxx", "yyy");
//            tempPath.append(tmp.getCanonicalPath().split("xxxxxxx")[0]);
//            tmp.delete();
//        } catch (IOException e) {
//            System.err.println("Error while initialization: cannot create temp file");
//        }
//        temp = tempPath.toString();
//        if (temp == null || "".equals(temp)) {
//            temp = LOCAL_PATH;
//        }
//        TEMP_PATH = temp;
        TEMP_PATH = LOCAL_PATH + File.separator + "temp";

        USER_HOME_PATH = System.getProperty("user.home") + File.separator;
    }

    ;

    public static JTabbedPaneOperator getPage(JFrameOperator frame, TestList list) {
        JTabbedPaneOperator tabs = new JTabbedPaneOperator(frame, new NameComponentChooser("br.tabs"));

        switch (list) {
            case PASSED:
                tabs.selectPage(getExecResource("br.tabs.tl0.tab"));
                break;
            case FAILED:
                tabs.selectPage(getExecResource("br.tabs.tl1.tab"));
                break;
            case ERROR:
                tabs.selectPage(getExecResource("br.tabs.tl2.tab"));
                break;
            case NOT_RUN:
                tabs.selectPage(getExecResource("br.tabs.tl3.tab"));
                break;
            case FILTERED_OUT:
                tabs.selectPage(getExecResource("br.tabs.fo.tab"));
                break;
        }
        return tabs;
    }

    public static String getExecResource(String key) {
        return i18nExecResources.getString(key);
    }

    public static String getToolResource(String key) {
        return i18nToolResources.getString(key);
    }

    // checks if panel is opened
    public static boolean checkPanel() {
        return true;
    }

    // checks if configuration editor contains standard config file
    public static boolean checkStandardConfig(JDialogOperator configEditor) {
        return true;
    }

    // opens standard test suite
    public static void openTestSuite(JFrameOperator frame) {

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        JMenuBarOperator jmbo = new JMenuBarOperator(frame);
        jmbo.pushMenuNoBlock("File");

        Menu.getFile_Open_TestSuiteMenu(frame).pushNoBlock();
        //Wait for dialog box to appear
        JDialogOperator openDialog = new JDialogOperator(getToolResource("tsc.title"));
        //Find edit box for file name
        new JTextFieldOperator(openDialog, "").enterText(TEST_SUITE_NAME);
    }

    // creates standard work directory using menu
    @Deprecated
    public static String createWorkDirInTemp(JFrameOperator mainFrame) {
        int attempts = 0;

        new JMenuOperator(mainFrame).pushMenuNoBlock(getExecResource("qlb.file.menu") + "|" + getExecResource("ch.newWorkDir.act"), "|");

        JDialogOperator filer = new JDialogOperator(mainFrame, getToolResource("wdc.new.title"));
        String path = TEMP_PATH + TEMP_WD_NAME;
        while (attempts < 10) {
            File file = new File(path);
            if (!file.exists()) {
                usedFiles.add(file);
                break;
            }
            deleteDirectory(file);
            file = new File(path);
            if (!file.exists()) {
                usedFiles.add(file);
                break;
            }
            path = TEMP_PATH + TEMP_WD_NAME + (int) (Math.random() * 10000);
            attempts++;
        }
        if (attempts >= 10) {
            throw new JemmyException("error");
        }
        JTextFieldOperator tf;

        tf = new JTextFieldOperator((JTextField) Tools.getComponent(filer, new String[]{"Folder name:", "File name:", "Folder Name:", "File Name:"}));
        tf.enterText(path);
        return path;
    }

    // picks standatd work directory using dialog box "work directory needed"
    public static void pickWorkDir(JFrameOperator mainFrame) {
        JDialogOperator wrkDir = new JDialogOperator(mainFrame, getExecResource("exec.wd.need.title"));

        new JButtonOperator(wrkDir, getExecResource("exec.wd.new.btn")).push();

        wrkDir = new JDialogOperator(mainFrame, getExecResource("wdc.createtitle"));

        deleteDirectory(new File(TEMP_PATH + TEMP_WD_NAME));
        getTextField(wrkDir, getExecResource("wdc.dir.name.lbl")).typeText(TEMP_WD_NAME);

        new JButtonOperator(wrkDir, getExecResource("wdc.browse.btn")).push();

        JDialogOperator filer = new JDialogOperator(mainFrame, getExecResource("wdc.filechoosertitle"));

        JTextFieldOperator tf;

        tf = new JTextFieldOperator((JTextField) Tools.getComponent(filer, new String[]{"Folder name:", "File name:"}));
        tf.enterText(TEMP_WD_NAME);

        new JButtonOperator(wrkDir, getExecResource("wdc.create.btn")).push();
    }

    // gets TextField in dialog by it's caption
    public static JTextFieldOperator getTextField(JDialogOperator dialog, String caption) {
        JLabelOperator label = new JLabelOperator(dialog, caption);
        return new JTextFieldOperator((JTextField) label.getLabelFor());
    }

    public static JComponent getComponent(final JDialogOperator dialog, final String captions[]) {
        ComponentFinder threads[] = new ComponentFinder[captions.length];
        final JLabelOperatorRef ref = new JLabelOperatorRef();
        for (int i = 0; i < captions.length; i++) {
            threads[i] = new ComponentFinder(ref, dialog, captions[i]);
            threads[i].start();
        }
        boolean someIsAlive = true;
        while (ref.isOpNull() && someIsAlive) {
            someIsAlive = false;
            for (ComponentFinder finder : threads) {
                if (finder.isAlive) {
                    someIsAlive = true;
                }
                if (!ref.isOpNull())
                    break;
            }
        }
        if (!ref.isOpNull()) {
            return (JComponent) ref.getOp().getLabelFor();
        }
        StringBuilder build = new StringBuilder("Was not found any of components: ");
        for (String caption : captions) {
            build.append(caption).append(" ");
        }
        throw new JemmyException("Error occured while searching components", new TimeoutExpiredException(build.toString()));
    }

    public static <ComponentClass extends JComponent> ComponentClass getComponentPar(final JDialogOperator dialog, final String captions[]) {
        ComponentFinder threads[] = new ComponentFinder[captions.length];
        final JLabelOperatorRef ref = new JLabelOperatorRef();
        for (int i = 0; i < captions.length; i++) {
            threads[i] = new ComponentFinder(ref, dialog, captions[i]);
            threads[i].start();
        }
        boolean anyFinderAlive;
        ComponentFindersCheck:
        do {
            anyFinderAlive = false;
            for (ComponentFinder finder : threads) {
                if (!ref.isOpNull()) {
                    try {
                        ComponentClass check = (ComponentClass) ref.getOp().getLabelFor();
                        return check;
                    } catch (Exception e) {
                    }
                }
                if (finder.isAlive) {
                    anyFinderAlive = true;
                    continue ComponentFindersCheck;
                }
            }
        } while (anyFinderAlive);
        StringBuilder build = new StringBuilder("Was not found any of components: ");
        for (String caption : captions) {
            build.append(caption).append(" ");
        }
        throw new JemmyException("Error occured while searching components", new TimeoutExpiredException(build.toString()));
    }

    public static JComboBoxOperator getComboBox(JDialogOperator dialog, String caption) {
        JLabelOperator label = new JLabelOperator(dialog, caption);
        return new JComboBoxOperator((JComboBox) label.getLabelFor());
    }

    /////////// Quick Start dialog box methods
    // opens QS using menu dialog box
    public static JDialogOperator openQuickStart(JFrameOperator mainFrame) {
        new JMenuOperator(mainFrame).pushMenu(getExecResource("qlb.file.menu") + "|" + getExecResource("mgr.openQuickStart.act"), "|");
        return findQuickStart(mainFrame);
    }

    // click "next" in QS
    public static void next(JDialogOperator quickStartDialog) {
        new JButtonOperator(quickStartDialog, getExecResource("qsw.next.btn")).push();
    }

    // click "finish" in QS with test running
    public static void finish(JDialogOperator quickStartDialog, boolean startConfigEditor, boolean runTests) {
        new JCheckBoxOperator(quickStartDialog, getExecResource("qsw.end.cfg.ckb")).setSelected(startConfigEditor);
        new JCheckBoxOperator(quickStartDialog, getExecResource("qsw.end.run.ckb")).setSelected(runTests);

        new JButtonOperator(quickStartDialog, getExecResource("qsw.done.btn")).push();
    }

    // click "finish" in QS
    public static void finish(JDialogOperator quickStartDialog, boolean startConfigEditor) {
        new JCheckBoxOperator(quickStartDialog, getExecResource("qsw.end.cfg.ckb")).setSelected(startConfigEditor);

        new JButtonOperator(quickStartDialog, getExecResource("qsw.done.btn")).push();
    }

    // uses standart testsuite in QS
    public static void pickDefaultTestsuite(JDialogOperator quickStartDialog) {

        getTextField(quickStartDialog, getExecResource("qsw.ts.hd")).typeText(TEST_SUITE_NAME);

    }

    // activates flag of creating new configuration in QS
    public static void createConfiguration(JDialogOperator quickStartDialog) {

        new JRadioButtonOperator(quickStartDialog, getExecResource("qsw.cfg.new.rb")).push();
    }

    // activates flag of starting config editor in QS
    public static void startConfigEditor(JDialogOperator quickStartDialog) {
        new JCheckBoxOperator(quickStartDialog, getExecResource("qsw.end.cfg.ckb")).push();
    }

    // uses template config in QS
    static public void useConfigTemplate(JDialogOperator quickStartDialog) {

        new JRadioButtonOperator(quickStartDialog, getExecResource("qsw.cfg.template.rb")).push();

        getTextField(quickStartDialog, getExecResource("qsw.cfg.jtm.field.lbl")).typeText(TEMPLATE_NAME);
    }

    // uses bad template config in QS
    static public void useBadConfigTemplate(JDialogOperator quickStartDialog) {

        new JRadioButtonOperator(quickStartDialog, getExecResource("qsw.cfg.template.rb")).push();

        getTextField(quickStartDialog, getExecResource("qsw.cfg.jtm.field.lbl")).typeText("democonfig_broken.jti");
    }

    // uses missing template config in QS
    static public void useMissingConfigTemplate(JDialogOperator quickStartDialog) {

        new JRadioButtonOperator(quickStartDialog, getExecResource("qsw.cfg.template.rb")).push();

        getTextField(quickStartDialog, getExecResource("qsw.cfg.jtm.field.lbl")).typeText("/tmp/missing.jti");
    }

    // checks if "next" button is available in QS
    static public boolean isNextEnabled(JDialogOperator quickStartDialog) {

        return JButtonOperator.findJButton(quickStartDialog.getContentPane(), getExecResource("qsw.next.btn"), false, false).isEnabled();
    }

    // close QS
    static public void closeQS(JFrameOperator mainFrame) {
        new JDialogOperator(mainFrame, getExecResource("qsw.title")).close();
    }

    //////////// JavaTest starting
    // starts the JT
    public static void startJavaTestWithDefaultWorkDirectory() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        new ClassReference("com.sun.javatest.tool.Main").startApplication(new String[]{"-NewDesktop", "-open", DEFAULT_WD_NAME});
    }

    public static void startJavatest() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        //Start JT Harness
        new ClassReference("com.sun.javatest.tool.Main").startApplication(new String[]{});
    }

    // starts the JT with specified params
    public static void startJavatest(String params) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        //Start JT Harness with Specified parameters
        new ClassReference("com.sun.javatest.tool.Main").startApplication(new String[]{params});
    }

    // starts the JT with specified params
    public static void startJavatest(String[] params) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        //Start JT Harness with Specified parameters
        new ClassReference("com.sun.javatest.tool.Main").startApplication(params);
    }

    // starts the JT with specified testsuite, workdir and config
    public static void startJavatest(String testsuite, String workdir, String config) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        //Start JT Harness with specified testsuite, workdir and config file
        new ClassReference("com.sun.javatest.tool.Main").startApplication(new String[]{"-open", testsuite, "-workdir", workdir, "-config", config});
    }

    public static void startJavatestNewDesktop() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        new ClassReference("com.sun.javatest.tool.Main").startApplication(new String[]{"-NewDesktop"});
    }

    // returns main frame
    public static JFrameOperator findMainFrame() {
        return new JFrameOperator(WINDOWNAME + " Harness");
    }

    // returns QS
    public static JDialogOperator findQuickStart(JFrameOperator mainFrame) {
        return new JDialogOperator(mainFrame, WINDOWNAME + " Harness Quick Start");
    }

    // returns config editor dialog box
    public static JDialogOperator findConfigEditor(JFrameOperator mainFrame) {
        return new JDialogOperator(mainFrame, getExecResource("ce.name"));
    }

    // returns dialog box of work directory creation
    public static JDialogOperator findWorkDir(JFrameOperator mainFrame) {
        return new JDialogOperator(mainFrame, getToolResource("wdc.new.title"));
    }

    // click "ok" in filter editor dialog box
    public static void ok(JDialogOperator filterEditor) {
        new JButtonOperator(filterEditor, getExecResource("fconfig.edit.done.btn")).push();
    }

    // click "cancel" in dialod box
    public static void cancel(JDialogOperator dialog) {
        new JButtonOperator(dialog, "cancel").push();        // Cancel of filter dialog not found in resource files
    }

    // check some counters?
    public static void checkCounters(JFrameOperator mainFrame, int[] counters) {
        for (int i = 0; i < counters.length; i++) {
            int value = counters[i];

            String actual = new JTextFieldOperator(mainFrame, new NameComponentChooser("br.summ." + i)).getText();

            if (!actual.equals(Integer.toString(value))) {
                throw new JemmyException("Wrong counters value in the main frame. Expected: " + value + ". Found: " + actual + ". In row: " + i);
            }
        }
    }

    static public boolean deleteDirectory(String path) {
        return deleteDirectory(new File(path));
    }

    // deletes directory
    static public boolean deleteDirectory(File path) {
        if (path != null && path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
            return (path.delete());
        }
        return true;
    }

    public static boolean copyFile(String from, String to) throws IOException {
        return copyFile(new File(from), new File(to));
    }

    public static boolean copyFile(File from, File to) throws IOException {
        if (from.isDirectory()) {
            return copyDirectory(from, to);
        }

        if (!from.exists()) {
            throw new IOException("Source file " + from.getAbsolutePath() + " doesn't exist");
        }
        if (!from.canRead()) {
            throw new IOException("Can't read source file " + from.getAbsolutePath());
        }

        if (!to.exists()) {
            if (!to.createNewFile()) {
                throw new IOException("Can't create destination file " + to.getAbsolutePath());
            }
            copyFile_(from, to);
            return true;
        } else {
            if (to.isDirectory()) {
                String name = from.getName();
                to = new File(to, name);
                return copyFile(from, to);
            } else {
                throw new IOException("Destination file " + to.getAbsolutePath() + " exists");
            }
        }
    }

    private static void copyFile_(File from, File to) throws IOException {
        to.createNewFile();
        if ("configHistory.jtl".equals(from.getName())) {
            BufferedReader reader = new BufferedReader(new FileReader(from));
            FileWriter writer = new FileWriter(to);
            String temp;
            while ((temp = reader.readLine()).startsWith("#")) {
                writer.write(temp + "\n");
            }
            while (temp != null) {
                File tempFile = new File(temp);
                if (tempFile.exists()) {
                    if (!new File(to.getParentFile(), tempFile.getName()).exists()) {
                        copyFile(tempFile, to.getParentFile());
                    }
                    writer.write(to.getParentFile().getAbsolutePath() + File.separator + tempFile.getName());
                } else {
                    writer.write(temp);
                }
                temp = reader.readLine();
            }
            writer.flush();
        } else {
            FileChannel fromChannel = new FileInputStream(from).getChannel();
            FileChannel toChannel = new FileOutputStream(to).getChannel();
            try {
                fromChannel.transferTo(0, fromChannel.size(), toChannel);
            } finally {
                fromChannel.close();
                toChannel.close();
            }
        }
    }

    public static boolean copyDirectory(File from, File to) throws IOException {
        System.out.println(from.getAbsolutePath());
        if (!from.exists()) {
            throw new IOException("Source file " + from.getAbsolutePath() + " doesn't exist");
        }
        if (!from.canRead()) {
            throw new IOException("Can't read source file " + from.getAbsolutePath());
        }
        if (from.isFile()) {
            return copyFile(from, to);
        }

        if (!to.exists()) {
            to.mkdirs();
            boolean res = true;
            for (File f : from.listFiles()) {
                if (f.isFile()) {
                    copyFile_(f, new File(to, f.getName()));
                } else if (f.isDirectory()) {
                    res = res & copyDirectory_(f, to);
                }
                return res;
            }
        } else {
            if (!to.isDirectory()) {
                throw new IOException("Destination file is not a directory");
            }
            if (!to.canWrite()) {
                throw new IOException("Can not write in destination directory");
            }

            return copyDirectory_(from, to);
        }

        return true;
    }

    private static boolean copyDirectory_(File from, File to) throws IOException {
        File newDir = new File(to, from.getName());
        newDir.mkdir();
        for (File f : from.listFiles()) {
            if (f.isFile()) {
                copyFile_(f, new File(newDir, f.getName()));
            } else if (f.isDirectory()) {
                copyDirectory_(f, newDir);
            }
        }
        return true;
    }

    // finds the test tree in the main frame
    public static JTreeOperator findTree(JFrameOperator mainFrame) {
        return new JTreeOperator(mainFrame, new NameComponentChooser("tree"));
    }

    public static JTreeOperator findTree(JFrameOperator mainFrame, int n) {
        return new JTreeOperator(mainFrame, new NameComponentChooser("tree"), n);
    }

    // checks tabs of test results
    public static void checkAllTestLists(JFrameOperator frame, String[] passed, String[] failed, String[] error, String[] notrun, String[] filtered) {
        JTabbedPaneOperator tabs = new JTabbedPaneOperator(frame, new NameComponentChooser("br.tabs"));
        if (passed != null) {
            checkTestList(frame, TestList.PASSED, passed);
        } else {
            if (tabs.isEnabledAt(2)) {
                throw new JemmyException("The 'Passed' page is enabled while it must be not");
            }
        }

        if (failed != null) {
            checkTestList(frame, TestList.FAILED, failed);
        } else {
            if (tabs.isEnabledAt(3)) {
                throw new JemmyException("The 'Failed' page is enabled while it must be not");
            }
        }

        if (error != null) {
            checkTestList(frame, TestList.ERROR, error);
        } else {
            if (tabs.isEnabledAt(4)) {
                throw new JemmyException("The 'Error' page is enabled while it must be not");
            }
        }

        if (notrun != null) {
            checkTestList(frame, TestList.NOT_RUN, notrun);
        } else {
            if (tabs.isEnabledAt(5)) {
                throw new JemmyException("The 'Not Run' page is enabled while it must be not");
            }
        }

        if (filtered != null) {
            checkTestList(frame, TestList.FILTERED_OUT, filtered);
        } else {
            if (tabs.isEnabledAt(6)) {
                throw new JemmyException("The 'Filtered Out' page is enabled while it must be not");
            }
        }

    }

    // checks a tab of test results
    public static void checkTestList(JFrameOperator frame, TestList list, String[] expected) throws JemmyException {
        JTreeOperator tree = findTree(frame);
        tree.selectRow(0);
        JTabbedPaneOperator tabs = getPage(frame, list);
        checkTestList(new JTableOperator(tabs, 0), expected);
    }

    // checks a tab of test results
    private static void checkTestList(JTableOperator list, String[] expected) throws JemmyException {

        if (list.getRowCount() != expected.length) {
            throw new JemmyException("Wrong test count. Expected: " + expected.length + ", found: " + list.getRowCount());
        }

        boolean[] appeared = new boolean[expected.length];

        for (int i = 0; i < expected.length; i++) {
            final Object o = list.getValueAt(i, 0);
            final TestResult tr = (TestResult) o;
            final String testName = tr.getTestName();
            final int pos = find(expected, testName);

            if (pos == -1) {
                throw new JemmyException("Test " + testName + " is not expected");
            }

            if (appeared[pos]) {
                throw new JemmyException("Test " + testName + " has been found more than once");
            }

            appeared[pos] = true;
        }
    }

    // find string s in string array a
    private static int find(String[] a, String s) {
        for (int i = 0; i < a.length; i++) {
            if (a[i].equals(s)) {
                return i;
            }
        }
        return -1;
    }

    // Prepares the JavaTest for the next test run
    public static void closeAll(JFrameOperator frame) throws InterruptedException {
        // Close all dialog windows
        JDialog dialog;
        while ((dialog = JDialogOperator.findJDialog(new AnyComponentChooser())) != null) {
            new JDialogOperator(dialog).close();
        }

        // Close all tabs
        JTabbedPaneOperator tabs = new JTabbedPaneOperator(frame);
        while (tabs.getTabCount() > 0) {
            Thread.sleep(500);
            JMenuBarOperator jmbo = new JMenuBarOperator(frame);
            jmbo.pushMenuNoBlock("File|Close");
            //new JMenuOperator(frame).pushMenuNoBlock(getExecResource("cb.file.menu") + "|Close", "|");   // Close from the File tab is in strange resource file
        }
    }

    public static void closeJT(JFrameOperator frame) throws InterruptedException {
        closeAll(frame);
        Thread.sleep(500);
        JMenuBarOperator jmbo = new JMenuBarOperator(frame);
        Thread.sleep(500);
        jmbo.pushMenuNoBlock("File/Exit", "/");
        //new JMenuOperator(frame).pushMenuNoBlock(getExecResource("qlb.file.menu") + "|" + getToolResource("dt.file.exit.mit"), "|");
    }

    // concat 2 string arrays
    public static String[] concat(String[] a, String[] b) {
        List<String> list = new ArrayList<String>(a.length + b.length);
        list.addAll(Arrays.asList(a));
        list.addAll(Arrays.asList(b));
        String[] result = new String[a.length + b.length];
        return list.toArray(result);
    }

    public static void pressYes(JDialogOperator dialog) {
        new JButtonOperator(dialog, "Yes").push();
    }

    public static URL urlFile(String path) {
        URL u = null;
        try {
            u = new File(path).toURI().toURL();
        } catch (MalformedURLException ex) {
            Logger.getLogger(Tools.class.getName()).log(Level.SEVERE, null, ex);
        }
        return u;
    }

    public static int findInStringArray(String[] in, String what) {
        for (int i = 0; i < in.length; i++) {
            //String tmp=in[i].replaceAll("\\W"," ");
            //String acin=tmp.replaceAll("\\d","");
            if (in[i].equals(what)) {
                return i;
            }
        }
        return -1;
    }

    public static void deleteUserData() {
        deleteDirectory(new File(USER_HOME_PATH + ".javatest"));
    }

    public static void openWorkDirectory_(JFrameOperator mainFrame, String path) {
        new JMenuOperator(mainFrame).pushMenuNoBlock(getExecResource("qlb.file.menu") + "|" + getExecResource("tmgr.openMenu.menu") + "|Work Directory ...", "|", new StringComparator() {

            public boolean equals(String arg0, String arg1) {
                return arg0.equals(arg1);
            }
        });

        JDialogOperator filer = new JDialogOperator(mainFrame, "Open Work Directory");

        JTextFieldOperator tf;

        tf = new JTextFieldOperator((JTextField) Tools.getComponent(filer, new String[]{"Folder name:", "File name:"}));
        tf.enterText(path);
    }

    public static void waitForWDLoading(JFrameOperator mainFrame, WDLoadingResult type) {
        new JTextFieldOperator(mainFrame, getExecResource("br.worst." + type.ordinal()));
    }

    public static void startTests(JFrameOperator mainFrame) {
        new JMenuOperator(mainFrame, "Run Tests").pushMenu(new String[]{"Run Tests", "Start"});
    }

    public static void pushClose(JFrameOperator mainFrame) {
        new JMenuOperator(mainFrame, "File").pushMenuNoBlock(new String[]{"File", "Close"});
    }

    public static void startJavaTestWithDefaultTestSuite() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        new ClassReference("com.sun.javatest.tool.Main").startApplication(new String[]{"-NewDesktop", "-open", TEST_SUITE_NAME});
    }

    // sleep for 10 seconds
    public static void pause() {
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
        }
    }

    ;

    public static void pause(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (Exception e) {
        }
    }

    @After
    public void removeFiles() {
        for (File f : usedFiles) {
            if (f.exists()) {
                if (f.isDirectory()) {
                    deleteDirectory(f);
                } else if (f.isFile()) {
                    f.delete();
                }
                if (f.exists()) {
                    System.err.println("Temporary file " + f.getAbsolutePath() + " was not removed. Please remove it manualy.");
                }
            }
        }
    }

    public static enum TestList {

        PASSED, FAILED, ERROR, NOT_RUN, FILTERED_OUT
    }

    public enum FiltersType {

        LAST_TEST_RUN, CURRENT_CONFIGURATION, ALL_TESTS, CUSTOM
    }

    public enum WDLoadingResult {

        ALL_PASSED, SOME_FAILED, SOME_ERRORS, SOME_NOTRUN
    }

    private static class JLabelOperatorRef {

        private JLabelOperator op1 = null;

        public synchronized boolean isOpNull() {
            return op1 == null;
        }

        public JLabelOperator getOp() {
            return op1;
        }

        public synchronized void setOp(JLabelOperator op) {
            this.op1 = op;
        }
    }

    private static class ComponentFinder extends Thread {

        private final JLabelOperatorRef ref;
        private JDialogOperator dialog;
        private String caption;
        private boolean isAlive = true;

        public ComponentFinder(JLabelOperatorRef ref, JDialogOperator dialog, String caption) {
            this.dialog = dialog;
            this.caption = caption;
            this.ref = ref;
        }

        public void run() {
            try {
                JLabelOperator op = new JLabelOperator(dialog, caption);
                if (ref != null && ref.isOpNull()) {
                    ref.setOp(op);
                }
            } catch (TimeoutExpiredException ex) {
            }
            isAlive = false;
        }
    }

    public static class AnyComponentChooser implements ComponentChooser {

        public AnyComponentChooser() {
        }

        public boolean checkComponent(Component arg0) {
            return true;
        }

        public String getDescription() {
            return "";
        }
    }

    public static class SimpleStringComparator implements StringComparator {

        public boolean equals(String arg0, String arg1) {
            return arg0.equals(arg1);
        }
    }
}

