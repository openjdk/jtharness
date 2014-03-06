/*
 * $Id$
 *
 * Copyright (c) 2002, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.help.CSH;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.sun.javatest.InterviewParameters;
import com.sun.javatest.TestEnvironment;
import com.sun.javatest.Parameters.EnvParameters;
import com.sun.javatest.Parameters.LegacyEnvParameters;
import com.sun.javatest.TestEnvContext;
import com.sun.javatest.TestSuite;
import com.sun.javatest.tool.EditableFileList;
import com.sun.javatest.tool.UIFactory;

class CE_EnvironmentPane extends CE_StdPane
{
    CE_EnvironmentPane(UIFactory uif, InterviewParameters config) {
        super(uif, config, "env");
        CSH.setHelpIDString(this, "confEdit.envTab.csh");

        // save this value, so we can make files test-suite-relative later
        testSuite = config.getTestSuite();

        envParameters = config.getEnvParameters();
        if (envParameters instanceof LegacyEnvParameters)
            legacyEnvParameters = (LegacyEnvParameters) (envParameters);
        else
            legacyEnvParameters = null;

        initGUI();
    }

    boolean isOKToClose() {
        // Errors about files are posted as soon as they are entered.
        // The environment name is from a combo box, so allow that
        return true;
    }

    void load() {
        if (legacyEnvParameters != null) {
            envFilesField.setItems(legacyEnvParameters.getEnvFiles());

            // setting the envFilesField will automatically cause
            // the list of choices for the envNameField to be updated
            // and will enable it as appropriate

            // setting the field editable is questionable, since
            // it allows the user to specify a value which is not
            // currently in the env files
            envNameField.setEditable(true);

            String envName = legacyEnvParameters.getEnvName();
            if (envName != null)
                envNameField.setSelectedItem(envName);
        }
        else {
            envFilesField.clear();
            envFilesField.setEnabled(false);
            envNameField.removeAllItems();

            TestEnvironment env = envParameters.getEnv();
            String name = (env == null ? null : env.getName());
            if (name == null)
                name = uif.getI18NString("ce.env.noName");
            envNameField.addItem(name);
            envNameField.setEnabled(false);
        }
    }

    void save() {
        if (legacyEnvParameters != null) {
            legacyEnvParameters.setEnvFiles(envFilesField.getFiles());
            if (envNameField.isEnabled())
                legacyEnvParameters.setEnvName((String) (envNameField.getSelectedItem()));
        }
    }

    private void initGUI() {
        JPanel p = uif.createPanel("ce.env", new GridBagLayout(), false);

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.EAST;
        lc.gridwidth = 1;
        lc.insets.right = 5;
        lc.weightx = 0;

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1;
        fc.gridwidth = GridBagConstraints.REMAINDER;

        envFilesLabel = uif.createLabel("ce.env.envFiles", true);
        p.add(envFilesLabel, lc);

        envFilesChooser = new JFileChooser();
        envFilesChooser.setAcceptAllFileFilterUsed(false);
        envFilesChooser.addChoosableFileFilter(FileType.jteFiles);
        envFilesChooser.setFileFilter(FileType.jteFiles);
        envFilesField = new EditableFileList(envFilesChooser) {
            public Object getNewItem() {
                File f = (File) (super.getNewItem());
                if (f != null)
                    f = makeTestSuiteRelative(f);
                return f;
            }
        };
        envFilesField.setDuplicatesAllowed(false);
        envFilesField.addListDataListener(new ListDataListener() {
            public void contentsChanged(ListDataEvent e) {
                envFilesChanged();
            }
            public void intervalAdded(ListDataEvent e) {
                envFilesChanged();
            }
            public void intervalRemoved(ListDataEvent e) {
                envFilesChanged();
            }
        });

        uif.setToolTip(envFilesField, "ce.env.envFiles");
        envFilesLabel.setLabelFor(envFilesField);
        p.add(envFilesField, fc);

        envNameLabel = uif.createLabel("ce.env.envName", true);
        p.add(envNameLabel, lc);

        JPanel envNamePanel = uif.createPanel("ce.env.name",
                                    new GridBagLayout(),
                                    false);
        GridBagConstraints c = new  GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        envNameField = uif.createChoice("ce.env.envName", envNameLabel);
        envNameField.addItem(uif.getI18NString("ce.env.noEnvs"));
        envNameField.setEditable(true);
        envNameField.setEnabled(false);
        envNameLabel.setLabelFor(envNameField);
        envNamePanel.add(envNameField, c);
        p.add(envNamePanel, fc);

        addBody(p);

        if (legacyEnvParameters == null) {
            // can't accurately render info about the environment,
            // so mark the pane disabled
            setEnabled(false);
        }
    }

    private void envFilesChanged() {
        try {
            File tsd = testSuite.getRootDir();
            File[] envFiles = envFilesField.getFiles();
            for (int i = 0; i < envFiles.length; i++) {
                if (!envFiles[i].isAbsolute())
                    envFiles[i] = new File(tsd, envFiles[i].getPath());
                if (!envFiles[i].exists())
                    uif.showError("ce.env.cantFindEnvFile", envFiles[i]);
            }
            TestEnvContext tec = new TestEnvContext(envFiles);
            String[] names = tec.getEnvMenuNames();
            String[] sortedNames = new String[names.length];
            System.arraycopy(names, 0, sortedNames, 0, names.length);
            Arrays.sort(sortedNames);
            envNameField.removeAllItems();
            for (int i = 0; i < sortedNames.length; i++)
                envNameField.addItem(sortedNames[i]);
        }
        catch (TestEnvContext.Fault ex) {
            uif.showError("ce.env.cannotEvalEnvs", ex.getMessage());
        }

        if (envNameField.getItemCount() == 0) {
            envNameField.addItem(uif.getI18NString("ce.env.noEnvs"));
            envNameField.setEnabled(false);
        }
        else
            envNameField.setEnabled(legacyEnvParameters != null);
    }

    private File makeTestSuiteRelative(File file) {
        try {
            if (file.isAbsolute())
                // cannot (and should not) do anything to adjust it
                return file;

            // but if it is relative it will have been given to us relative
            // to the current dir, so we adjust it to be relative to the test
            // suite dir
            String canonFilePath = file.getCanonicalPath();
            File tsd = testSuite.getRootDir();
            String canonTSDPath = tsd.getCanonicalPath();
            if (canonFilePath.length() > canonTSDPath.length() + 1 &&
                canonFilePath.startsWith(canonTSDPath) &&
                canonFilePath.charAt(canonTSDPath.length()) == File.separatorChar) {
                return new File(canonFilePath.substring(canonTSDPath.length() + 1));
            }

            StringBuffer prefix = new StringBuffer();
            int spIndex;
            String cp = canonTSDPath;
            while ((spIndex = cp.lastIndexOf(File.separatorChar)) != -1) {
                cp = cp.substring(0, spIndex);
                if (prefix.length() != 0)
                    prefix.append(File.separator);
                prefix.append("..");
                if (canonFilePath.length() > cp.length() + 1 &&
                    canonFilePath.startsWith(cp) &&
                    canonFilePath.charAt(cp.length()) == File.separatorChar) {
                    ////System.err.println("match: (" + prefix + ") " + cp);
                    // so the guess is that "new File(prefix, file)" is what
                    // we want; the verification is that "new File(tsd, guess)
                    // has the same canonical path as the original file; if the
                    // verification fails, probably the system does not support
                    // .. and so we drop out and return the canonical path instead
                    File guess = new File(prefix.toString(), file.getPath());
                    String canonGuessPath = new File(tsd, guess.getPath()).getCanonicalPath();
                    if (canonGuessPath.equals(canonFilePath))
                        return guess;
                    else
                        break;
                }
            }

            return new File(canonFilePath);
        }
        catch (IOException e) {
            uif.showError("ce.env.makeTSRelIOError", new Object[] {file, e});
            return null;
        }
    }

    private EnvParameters envParameters;
    private LegacyEnvParameters legacyEnvParameters;
    private TestSuite testSuite;
    private JLabel envFilesLabel;
    private EditableFileList envFilesField;
    private JFileChooser envFilesChooser;
    private JLabel envNameLabel;
    private JComboBox envNameField;

}
