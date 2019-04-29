/*
 * $Id$
 *
 * Copyright (c) 2002, 2010, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.exec;

import com.sun.javatest.InterviewParameters;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.finder.TestFinderDecorator;
import com.sun.javatest.tool.ToolDialog;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.tool.jthelp.ContextHelpManager;
import com.sun.javatest.util.StringArray;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;

class PropertiesBrowser extends ToolDialog {
    private static final int TEST_SUITE_PANE = 0;
    private static final int WORK_DIRECTORY_PANE = 1;
    private static final int CONFIGURATION_PANE = 2;
    private static final int CLASSES_PANE = 3;
    private static final int NUM_PANES = 4;
    private TestSuite testSuite;
    private WorkDirectory workDir;
    private InterviewParameters config;
    private Pane[] panes;
    private String unset;

    PropertiesBrowser(JComponent parent, UIFactory uif) {
        super(parent, uif, "props");
    }

    void showDialog(TestSuite ts, WorkDirectory wd, InterviewParameters p) {
        if (panes == null) {
            initGUI();
        }

        testSuite = ts;
        workDir = wd;
        config = p;
        for (Pane pane : panes) {
            pane.update();
        }

        setVisible(true);
    }

    @Override
    protected void initGUI() {
        setHelp("execProps.dialog.csh");
        setI18NTitle("props.title");

        unset = uif.getI18NString("props.unset");

        panes = new Pane[NUM_PANES];
        panes[TEST_SUITE_PANE] = new TestSuitePane();
        panes[WORK_DIRECTORY_PANE] = new WorkDirectoryPane();
        panes[CONFIGURATION_PANE] = new ConfigurationPane();
        panes[CLASSES_PANE] = new PluginsPane();

        // hmm, which of the following is better?
        // is it worth making this a preference? really?
        // setBody(createTabbedPane(panes));
        setBody(createVerticalBoxPane(panes));

        JButton helpBtn = uif.createHelpButton("props.help", "execProps.dialog.csh");
        JButton closeBtn = uif.createCloseButton("props.close");
        setButtons(new JButton[]{helpBtn, closeBtn}, closeBtn);
    }

    private JComponent createTabbedPane(Pane... panes) {
        final JTabbedPane tabs = new JTabbedPane();
        for (Pane pane : panes) {
            uif.addTab(tabs, pane.getKey(), pane);
        }

        tabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Component t = tabs.getSelectedComponent();
                ContextHelpManager.setHelpIDString(tabs, ContextHelpManager.getHelpIDString(t));
            }
        });

        Component t = tabs.getSelectedComponent();
        ContextHelpManager.setHelpIDString(tabs, ContextHelpManager.getHelpIDString(t));

        return tabs;
    }

    private JComponent createVerticalBoxPane(Pane... panes) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets.bottom = 5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        for (Pane pane : panes) {
            String title = uif.getI18NString(pane.getKey() + ".tab");
            pane.setBorder(BorderFactory.createTitledBorder(title));
            p.add(pane, c);
        }

        return p;
    }

    private abstract class Pane extends JPanel {
        private String key;

        Pane(String key) {
            this.key = key;
            setLayout(new GridBagLayout());
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        }

        String getKey() {
            return key;
        }

        JTextField addLabelledField(String key) {
            JLabel l = addLabel(key);

            JTextField tf = uif.createOutputField(key, 30, l);
            tf.setBorder(null);
            GridBagConstraints fc = new GridBagConstraints();
            fc.fill = GridBagConstraints.HORIZONTAL;
            fc.weightx = 1;
            fc.gridwidth = GridBagConstraints.REMAINDER;
            add(tf, fc);

            return tf;
        }

        void setField(JTextField f, String v) {
            if (v == null || v.isEmpty()) {
                // set f to italic font?
                f.setText(unset);
            } else {
                // set f to regular font?
                f.setText(v);
            }
        }

        JLabel addLabel(String key) {
            JLabel l = uif.createLabel(key, true);
            GridBagConstraints lc = new GridBagConstraints();
            lc.anchor = GridBagConstraints.EAST;
            lc.gridwidth = 1;
            lc.insets.right = 5;
            lc.weightx = 0;
            add(l, lc);
            return l;
        }

        abstract void update();
    }

    private class TestSuitePane extends Pane {
        private JTextField path;
        private JTextField name;
        private JTextField id;
        TestSuitePane() {
            super("props.ts");
            ContextHelpManager.setHelpIDString(this, "execProps.testSuiteTab.csh");
            path = addLabelledField("props.ts.path");
            name = addLabelledField("props.ts.name");
            id = addLabelledField("props.ts.id");
        }

        @Override
        void update() {
            if (testSuite == null) {
                setField(path, null);
                setField(name, null);
                setField(id, null);
            } else {
                setField(path, testSuite.getPath());
                setField(name, testSuite.getName());
                setField(id, testSuite.getID());
            }
        }
    }

    private class WorkDirectoryPane extends Pane {
        private JTextField path;

        WorkDirectoryPane() {
            super("props.wd");
            ContextHelpManager.setHelpIDString(this, "execProps.workDirTab.csh");
            path = addLabelledField("props.wd.path");
            // test results?
            // id?
        }

        @Override
        void update() {
            setField(path, workDir == null ? null : workDir.getPath());
        }
    }

    private class ConfigurationPane extends Pane {
        private JTextField path;
        private JTextField configName;
        private JTextField configDesc;
        private JTextField templatePath;
        private JTextField state;
        private String completed;
        private String incomplete;
        ConfigurationPane() {
            super("props.cfg");
            ContextHelpManager.setHelpIDString(this, "execProps.configTab.csh");
            path = addLabelledField("props.cfg.path");
            configName = addLabelledField("props.cfg.name");
            configDesc = addLabelledField("props.cfg.desc");
            state = addLabelledField("props.cfg.state");
            // #questions answered?
            completed = uif.getI18NString("props.cfg.completed");
            incomplete = uif.getI18NString("props.cfg.incomplete");
            templatePath = addLabelledField("props.template.path");
        }

        @Override
        void update() {
            setField(templatePath, null);
            if (config == null || config.isTemplate()) {
                setField(path, null);
                setField(configName, null);
                setField(configDesc, null);
                setField(state, null);
            } else {
                File f = config.getFile();
                setField(path, f == null ? null : f.getPath());
                //TestEnvironment env = (config == null ? null : config.getEnv());
                //setField(configName, (env == null ? null : env.getName()));
                //setField(configDesc, (env == null ? null : env.getDescription()));
                setField(configName, config.getName());
                setField(configDesc, config.getDescription());
                setField(state, config.isFinishable() ? completed : incomplete);
            }

            if (config != null)
            // XXX both of these cases should not be necessary, but the code is
            // a bit unreliable currently
            {
                if (config.getTemplatePath() != null) {
                    setField(templatePath, config.getTemplatePath());
                } else if (config.getFile() != null &&
                        config.getFile().getPath().endsWith("jtm")) {
                    setField(templatePath, config.getFile().getPath());
                }
            }
        }
    }

    private class PluginsPane extends Pane {
        private JTextField testSuiteClassName;
        private JTextField testFinderClassName;
        private JTextField testRunnerClassName;
        private JTextField configClassName;
        PluginsPane() {
            super("props.pi");
            ContextHelpManager.setHelpIDString(this, "execProps.pluginsTab.csh");
            testSuiteClassName = addLabelledField("props.pi.testSuite");
            testFinderClassName = addLabelledField("props.pi.testFinder");
            testRunnerClassName = addLabelledField("props.pi.testRunner");
            configClassName = addLabelledField("props.pi.config");
        }

        @Override
        void update() {
            if (testSuite == null) {
                setField(testSuiteClassName, null);
                setField(testFinderClassName, null);
                setField(configClassName, null);
            } else {
                setField(testSuiteClassName, testSuite.getClass().getName());
                String testFinderClassNameValue = testSuite.getTestFinder() instanceof TestFinderDecorator ?
                        ((TestFinderDecorator) testSuite.getTestFinder()).getCurrentTestFinder().getClass().getName() :
                        testSuite.getTestFinder().getClass().getName();
                setField(testFinderClassName, testFinderClassNameValue);
                setField(testRunnerClassName,
                        testSuite.createTestRunner().getClass().getName());

                if (config != null) {
                    setField(configClassName, config.getClass().getName());
                } else {
                    String[] classNameAndArgs = StringArray.split(testSuite.getTestSuiteInfo("interview"));
                    if (classNameAndArgs == null || classNameAndArgs.length == 0) {
                        setField(configClassName, null);
                    } else {
                        setField(configClassName, classNameAndArgs[0]);
                    }
                }
            }
        }
    }
}
