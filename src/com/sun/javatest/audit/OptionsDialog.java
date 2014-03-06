/*
 * $Id$
 *
 * Copyright (c) 2002, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.audit;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;

import com.sun.javatest.InterviewParameters;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.tool.Desktop;
import com.sun.javatest.tool.FileHistory;
import com.sun.javatest.tool.TestSuiteChooser;
import com.sun.javatest.tool.Tool;
import com.sun.javatest.tool.ToolDialog;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.tool.WorkDirChooser;

class OptionsDialog extends ToolDialog
{
    OptionsDialog(Tool tool, ActionListener okListener, UIFactory uif) {
        super(tool, uif, "opts");
        this.tool = tool;
        this.okListener = okListener;
        setHelp("audit.options.csh");
    }

    void setParameters(InterviewParameters params) {
        if (body == null)
            initGUI();

        if (params != null) {
            TestSuite ts = params.getTestSuite();
            if (ts != null)
                tsField.setSelectedItem(ts.getPath());

            WorkDirectory wd = params.getWorkDirectory();
            if (wd != null)
                wdField.setSelectedItem(wd.getPath());

            File cf = params.getFile();
            if (cf != null)
                cfField.setSelectedItem(cf.getPath());
        }
    }

    String getTestSuitePath() {
        return (String) (tsField.getSelectedItem());
    }

    String getWorkDirPath() {
        return (String) (wdField.getSelectedItem());
    }

    String getConfigFilePath() {
        return (String) (cfField.getSelectedItem());
    }

    protected void initGUI() {
        setI18NTitle("opts.title");
        listener = new Listener();

        body = uif.createPanel("opts.fields", new GridBagLayout(), false);
        body.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.EAST;
        lc.insets.right = 10;

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1;

        GridBagConstraints bc = new GridBagConstraints();
        bc.gridwidth = GridBagConstraints.REMAINDER;
        bc.fill = GridBagConstraints.VERTICAL;

        Border smallBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createEtchedBorder(),
            BorderFactory.createEmptyBorder(0,3,0,3));

        // test suite
        JLabel tsLabel = uif.createLabel("opts.ts", true);
        body.add(tsLabel, lc);

        tsField = uif.createChoice("opts.ts", true, tsLabel);
        tsField.addItem("");
        tsField.addActionListener(listener);
        body.add(tsField, fc);

        tsBtn = uif.createIconButton("opts.ts.browse", listener);
        tsBtn.setBorder(smallBorder);
        body.add(tsBtn, bc);

        // results directory (work directory)
        JLabel wdLabel = uif.createLabel("opts.wd", true);
        body.add(wdLabel, lc);

        wdField = uif.createChoice("opts.wd", true, wdLabel);
        wdField.addItem("");
        wdField.addActionListener(listener);
        body.add(wdField, fc);

        wdBtn = uif.createIconButton("opts.wd.browse", listener);
        wdBtn.setBorder(smallBorder);
        body.add(wdBtn, bc);

        // config file
        JLabel cfLabel = uif.createLabel("opts.config", true);
        body.add(cfLabel, lc);

        cfField = uif.createChoice("opts.config", true, cfLabel);
        cfField.addItem("");
        cfField.setEditable(true);
        cfField.addActionListener(listener);
        body.add(cfField, fc);

        cfBtn = uif.createIconButton("opts.config.browse", listener);
        cfBtn.setBorder(smallBorder);
        body.add(cfBtn, bc);

        Dimension d = body.getPreferredSize();
        int dpi = uif.getDotsPerInch();
        body.setPreferredSize(new Dimension(Math.max(d.width, 5 * dpi), d.height));
        setBody(body);

        JButton okBtn = uif.createButton("opts.ok", okListener, OK);
        JButton cancelBtn = uif.createCancelButton("opts.cancel");
        JButton helpBtn = uif.createHelpButton("opts.help", "audit.options.csh");
        setButtons(new JButton[] { okBtn, cancelBtn, helpBtn }, okBtn);

        setComponentListener(listener);
    }

    private void updateFieldChoices() {
        updateTestSuiteChoices();
        updateWorkDirectoryChoices();
        updateConfigFileChoices();
    }

    private void chooseTestSuite() {
        if (testSuiteChooser == null)
            testSuiteChooser = new TestSuiteChooser();
        int action = testSuiteChooser.showDialog(tool);
        if (action != JFileChooser.APPROVE_OPTION)
            return;
        TestSuite ts = testSuiteChooser.getSelectedTestSuite();
        tsField.setSelectedItem(ts.getPath());
    }

    private void updateTestSuiteChoices() {
        // get the paths of currently loaded test suites
        // could move this to TestSuite, and use the dirMap cache
        SortedSet s = new TreeSet();
        Desktop d = tool.getDesktop();
        Tool[] tools = d.getTools();
        if (tools != null) {
            for (int i = 0; i < tools.length; i++) {
                Tool t = tools[i];
                TestSuite[] tss = t.getLoadedTestSuites();
                if (tss != null) {
                    for (int j = 0; j < tss.length; j++)
                        s.add(tss[j].getPath());
                }
            }
        }

        setItems(tsField, s);
        updateWorkDirectoryChoices();
    }

    private void chooseWorkDirectory() {
        if (workDirChooser == null) {
            workDirChooser = new WorkDirChooser(true);
            workDirChooser.setMode(WorkDirChooser.OPEN_FOR_ANY_TESTSUITE);
        }
        int action = workDirChooser.showDialog(tool);
        if (action != JFileChooser.APPROVE_OPTION)
            return;
        WorkDirectory wd = workDirChooser.getSelectedWorkDirectory();
        wdField.setSelectedItem(wd.getPath());
    }

    private void updateWorkDirectoryChoices() {
        String tsID = null;
        try {
            String tsp = (String) (tsField.getSelectedItem());
            if (tsp != null && tsp.length() > 0) {
                TestSuite ts = TestSuite.open(new File(tsp));
                tsID = ts.getID();
            }
        }
        catch (Exception e) {
            // ignore
        }

        // get the paths of currently loaded work directories
        // could move this to WorkDirectory and use the dirMap cache
        SortedSet s = new TreeSet();
        Desktop d = tool.getDesktop();
        Tool[] tools = d.getTools();
        if (tools != null) {
            for (int i = 0; i < tools.length; i++) {
                Tool t = tools[i];
                WorkDirectory[] wds = t.getLoadedWorkDirectories();
                if (wds != null) {
                    for (int j = 0; j < wds.length; j++) {
                        WorkDirectory wd = wds[j];
                        if (tsID == null || tsID.equals(wd.getTestSuite().getID()))
                            s.add(wd.getPath());
                    }
                }
            }
        }

        setItems(wdField, s);
    }

    private void chooseConfigFile() {
        if (configFileChooser == null) {
            configFileChooser = new JFileChooser();
            String userDir = System.getProperty("user.dir");
            if (userDir != null)
                configFileChooser.setCurrentDirectory(new File(userDir));
            configFileChooser.addChoosableFileFilter(new FileFilter() {
                    public boolean accept(File f) {
                        return (f.isDirectory() || f.getPath().endsWith(".jti"));
                    }

                    public String getDescription() {
                        return uif.getI18NString("opts.jtiFiles");
                    }
                });
        }

        int action = configFileChooser.showOpenDialog(tool);
        if (action != JFileChooser.APPROVE_OPTION)
            return;

        File cf = configFileChooser.getSelectedFile();
        String cfp;
        if (cf == null)
            cfp = "";
        else {
            cfp = cf.getPath();
            if (!cfp.endsWith(".jti"))
                cfp += ".jti";
        }
        cfField.setSelectedItem(cfp);
    }

    private void updateConfigFileChoices() {
        SortedSet s = new TreeSet();
        String wdp = (String) (wdField.getSelectedItem());
        try {
            WorkDirectory wd = WorkDirectory.open(new File(wdp));
            FileHistory history = FileHistory.getFileHistory(wd, "configHistory.jtl");
            File[] entries = history.getRecentEntries(10);
            for (int i = 0; i < entries.length; i++)
                s.add(entries[i].getPath());
        }
        catch (Exception e) {
            // ignore
        }
        setItems(cfField, s);
    }

    private void setItems(JComboBox field, SortedSet s) {
        // first, remove unwanted entries from field
        for (int i = field.getItemCount() - 1; i >= 0; i-- ) {
            String item = (String) (field.getItemAt(i));
            if (s.contains(item)) {
                // this item is required, so remove it from the
                // set to be added later
                s.remove(item);
            }
            else if (item.length() > 0 && !item.equals(field.getSelectedItem()))
                // this item is not required, remove it
                field.removeItemAt(i);
        }

        // those items remaining in s need to be added to the field
        for (Iterator iter = s.iterator(); iter.hasNext(); )
            field.addItem(iter.next());
    }

    private Tool tool;
    private ActionListener okListener;
    private JPanel body;

    private JComboBox tsField;
    private JButton tsBtn;
    private TestSuiteChooser testSuiteChooser;

    private JComboBox wdField;
    private JButton wdBtn;
    private WorkDirChooser workDirChooser;

    private JComboBox cfField;
    private JButton cfBtn;
    private JFileChooser configFileChooser;

    private Listener listener;

    static final String OK = "OK";

    private class Listener
        extends ComponentAdapter
        implements ActionListener
    {
        // ComponentListener
        public void componentShown(ComponentEvent e) {
            updateFieldChoices();
        }

        // ActionListener
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src == tsField)
                updateWorkDirectoryChoices();
            else if (src == tsBtn)
                chooseTestSuite();
            else if (src == wdField)
                updateConfigFileChoices();
            else if (src == wdBtn)
                chooseWorkDirectory();
            else if (src == cfBtn)
                chooseConfigFile();
        }
    };

}
