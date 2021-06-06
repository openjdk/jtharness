/*
 * $Id$
 *
 * Copyright (c) 2009, 2010, Oracle and/or its affiliates. All rights reserved.
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
package jthtest.tools;

import jthtest.Tools;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 *
 * @author at231876
 */
public class QSWizard {

    public static String QUICK_START_WIZARD_TITLE = Tools.WINDOWNAME + " Harness Quick Start";
    private JDialogOperator dialog;
    private Panel panel;

    public QSWizard() {
        dialog = new JDialogOperator(QUICK_START_WIZARD_TITLE);
        panel = new GreetingsPanel();
    }

    public Panel getPanel() {
        return panel;
    }

    public abstract class Panel {

        public JDialogOperator getDialog() {
            return dialog;
        }

        public JButtonOperator next() {
            return new JButtonOperator(dialog, "Next");
        }

        public JButtonOperator cancel() {
            return new JButtonOperator(dialog, "Cancel");
        }

        public JButtonOperator finish() {
            return new JButtonOperator(dialog, "Finish");
        }

        public JButtonOperator back() {
            return new JButtonOperator(dialog, "Back");
        }

        public abstract Panel pushNext();
    }

    public class GreetingsPanel extends Panel {

        private boolean startNew = false;
        private boolean resume = false;
        private boolean browse = false;

        public void setStartNewTestRun() {
            new JRadioButtonOperator(dialog, Tools.getExecResource("qsw.tsk.new.rb")).push();
            browse = resume = false;
            startNew = true;
        }

        public void setResumeTestRun() {
            new JRadioButtonOperator(dialog, Tools.getExecResource("qsw.tsk.resume.rb")).push();
            browse = startNew = false;
            resume = true;
        }

        public void setBrowseTestSuite() {
            new JRadioButtonOperator(dialog, Tools.getExecResource("qsw.tsk.browse.rb")).push();
            startNew = resume = false;
            browse = true;
        }

        @Override
        public ChooseTestSuitePanel pushNext() {
            next().push();
            return new ChooseTestSuitePanel();
        }
    }

    public class ChooseTestSuitePanel extends Panel {
        public static final String QSW_TS_TEXTFIELD = "qsw.ts.file.ed";

        private String testsuite = null;

        public String getTestsuite() {
            JTextFieldOperator op = new JTextFieldOperator(dialog, new NameComponentChooser("ComboBox.textField"));
            return op.getText();
        }

        public void setTestsuite(String testsuite) {
            JTextFieldOperator op = Tools.getTextField(dialog, "Test Suite:");// new JTextFieldOperator(dialog);
            op.clearText();
            op.enterText(testsuite);
            this.testsuite = testsuite;
        }

        @Override
        public ChooseConfigurationPanel pushNext() {
            next().push();
            return new ChooseConfigurationPanel();
        }
    }

    public class ChooseConfigurationPanel extends Panel {

        private boolean createNew = false;
        private boolean useOld = false;

        @Override
        public ChooseWorkDirectoryPanel pushNext() {
            if (createNew || useOld) {
                next().push();
                return new ChooseWorkDirectoryPanel();
            } else {
                return null;
            }
        }

        public void setCreateANewConfiguration() {
            new JRadioButtonOperator(dialog, Tools.getExecResource("qsw.cfg.new.rb")).push();
            createNew = true;
            useOld = false;
        }

        public void setUseAConfigurationTemplate() {
            new JRadioButtonOperator(dialog, Tools.getExecResource("qsw.cfg.template.rb")).push();
            useOld = true;
            createNew = false;
        }

        public boolean setPath(String path) {
            if (!useOld || createNew)
                return false;
            JTextFieldOperator tf = getTextFieled();
            tf.clearText();
            tf.typeText(path);
            return true;
        }

        public JTextFieldOperator getTextFieled() {
            return Tools.getTextField(dialog, "File:");
        }

        public JButtonOperator browse() {
            return new JButtonOperator(dialog, Tools.getExecResource("qsw.cfg.jtm.field.browse.btn"));
        }
    }

    public class ChooseWorkDirectoryPanel extends Panel {

        @Override
        public NewTestsuiteAlmostDone pushNext() {
            next().push();
            return new NewTestsuiteAlmostDone();
        }

        public JTextFieldOperator getTextField() {
            return Tools.getTextField(dialog, Tools.getExecResource("qsw.nwd.file.lbl"));
        }

        public void setWorkDirectory(String path) {
            JTextFieldOperator tf = getTextField();
            tf.clearText();
            tf.typeText(path);
        }

        public JButtonOperator browse() {
            return new JButtonOperator(dialog, Tools.getExecResource("qsw.nwd.file.browse.btn"));
        }
    }

    public class NewTestsuiteAlmostDone extends Panel {

        @Override
        public Panel pushNext() {
            finish().push();
            return null;
        }

        public JCheckBoxOperator getStartTheConfigEditorCheckBox() {
            return new JCheckBoxOperator(dialog, Tools.getExecResource("qsw.end.cfg.ckb"));
        }

        public JCheckBoxOperator getStartTestRunCheckBox() {
            return new JCheckBoxOperator(dialog, Tools.getExecResource("qsw.end.run.ckb"));
        }

        public void setStartTheConfigEditor(boolean set) {
            getStartTheConfigEditorCheckBox().setSelected(set);
        }

        public void setStartTestRun(boolean set) {
            getStartTestRunCheckBox().setSelected(set);
        }
    }
}
