/*
 * $Id$
 *
 * Copyright (c) 2004, 2014, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.sun.interview.Interview;
import com.sun.javatest.Harness;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.ResourceLoader;
import com.sun.javatest.TemplateUtilities;
//import com.sun.javatest.TemplateUtilities;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.tool.FileChooser;
import com.sun.javatest.tool.FileHistory;
import com.sun.javatest.tool.TestSuiteChooser;
import com.sun.javatest.tool.ToolDialog;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.tool.WorkDirChooser;


class QuickStartWizard extends ToolDialog
{
    QuickStartWizard(JComponent parent, Icon logoIcon,
            QSW_Listener finisher,  UIFactory uif) {
        super(parent, uif, "qsw");
        this.logoIcon = logoIcon;
        this.qswListener = finisher;
    }


    protected void initGUI() {
        disableDefaultDisposeHandler();
        initDirs();
        initPanes();

        setHelp("qsw.window.csh");
        setI18NTitle("qsw.title");

        body = uif.createPanel("qsw.body", new BorderLayout(), false);
        body.setBorder(BorderFactory.createLoweredBevelBorder());

        /*
         *      *-----------------------+
         *      |       | head          |
         *      |       +---------------|
         *      |       |               |
         *      | logo  | question pane |
         *      |       |               |
         *      |       +---------------|
         *      |       | error         |
         *      |-------+---------------+
         *      |         buttons       |
         *      |-----------------------+
         */

        TestSuite ts;
        try {
            //System.err.println("QSG: installDirIsTestSuite=" + installDirIsTestSuite);
            //System.err.println("QSG: installParentDirIsTestSuite=" + installParentDirIsTestSuite);
            //System.err.println("QSG: userDirIsTestSuite=" + userDirIsTestSuite);
            if (installDirIsTestSuite)
                ts = TestSuite.open(installDir);
            else if (installParentDirIsTestSuite)
                ts = TestSuite.open(installParentDir);
            else if (userDirIsTestSuite)
                ts = TestSuite.open(userDir);
            else
                ts = null;
        }
        catch (Throwable e) {
            ts = null;
        }

        // logo...
        JLabel logo;

        URL tsLogoURL = (ts == null ? null : ts.getLogo());
        //System.err.println("QSG: tsLogoURL=" + tsLogoURL);
        if (tsLogoURL == null) {
            logo = new JLabel(logoIcon);
            logo.setName("qsw.jtlogo");
            logo.setBackground(uif.getI18NColor("qsw.jtlogo.bg"));
            uif.setAccessibleInfo(logo, "qsw.jtlogo");
        }
        else {
            logo = new JLabel(new ImageIcon(tsLogoURL));
            logo.setName("qsw.tslogo");
            logo.setBackground(Color.white);
            // the following comments are to keep the i18n checker happy
            // getI18NString("qsw.tslogo.desc")
            // getI18NString("qsw.tslogo.name")
            uif.setAccessibleInfo(logo, "qsw.tslogo");
        }
        logo.setOpaque(true);
        logo.setFocusable(false); // mildly questionable, but it can't take/show focus
        body.add(logo, BorderLayout.WEST);

        main = uif.createPanel("qsw.main", new BorderLayout(), false);
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 11));
        head = uif.createOutputField("qsw.head");
        head.setBorder(null);
        head.setFont(head.getFont().deriveFont(Font.BOLD));
        main.add(head, BorderLayout.NORTH);

        foot = uif.createOutputField("qsw.foot");
        foot.setBorder(null);
        foot.setFont(foot.getFont().deriveFont(Font.BOLD));
        foot.setForeground(uif.getI18NColor("qsw.foot"));
        main.add(foot, BorderLayout.SOUTH);

        body.add(main, BorderLayout.CENTER);

        setBody(body);

        backBtn = uif.createButton("qsw.back", listener);
        nextBtn = uif.createButton("qsw.next", listener);
        doneBtn = uif.createButton("qsw.done", listener);
        JButton cancelBtn = uif.createCancelButton("qsw.cancel");
        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeExecTool(e);
            }

        });
        setPane(taskPane);
        setButtons(new JButton[] { backBtn, nextBtn, doneBtn, cancelBtn }, null);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

    }

    private void closeExecTool(AWTEvent ev) {

        Component src = (Component) ev.getSource();
        for (Container p = src.getParent(); p != null; p = p.getParent()) {
            if (p instanceof JInternalFrame || p instanceof Window &&  p.getParent() != null) {
                p.setVisible(false);
                break;
            }
        }
        qswListener.cancelQSW();
        // tool.getDesktop().removeTool(tool1);
        // tool.dispose();
    }

    private void initDirs() {
        File classDir = Harness.getClassDir();
        installDir = (classDir == null ? null : classDir.getParentFile());
        installDirIsTestSuite = (installDir == null ? false : TestSuite.isTestSuite(installDir));
        installParentDir = (installDir == null ? null : installDir.getParentFile());
        installParentDirIsTestSuite = (installParentDir == null ? false : TestSuite.isTestSuite(installParentDir));

        userDir = new File(System.getProperty("user.dir"));
        userDirIsTestSuite = (userDir == null ? false : TestSuite.isTestSuite(userDir));
        userDirIsWorkDirectory = (userDir == null ? false : WorkDirectory.isWorkDirectory(userDir));
    }

    private void initPanes() {
        taskPane = new TaskPane();
        configPane = new ConfigPane();
        testSuitePane = new TestSuitePane();
        newWorkDirPane = new CreateWorkDirPane();
        openWorkDirPane = new OpenWorkDirPane();
        endPane = new EndPane();
    }

    private void setPane(Pane p) {
        if (currPane != null)
            main.remove(currPane);

        p.update();
        head.setText(p.getHead());
        main.add(p, BorderLayout.CENTER);
        showError(null);
        backBtn.setEnabled(history.size() > 0);
        p.updateNextButton();
        doneBtn.setEnabled(p == endPane);
        currPane = p;

        main.validate();
        main.repaint();
    }

    private void showError(String key) {
        if (key == null) {
            foot.setText("");
            foot.setEnabled(false);
            foot.setVisible(false);
        }
        else {
            foot.setText(uif.getI18NString(key));
            foot.setEnabled(true);
            foot.setVisible(true);
            nextBtn.setEnabled(false);
        }
    }

    private void doBack() {
        int n = history.size();
        if (n > 0) {
            Pane p = (Pane) (history.remove(n - 1));
            setPane(p);
        }
    }

    private void doNext() {
        Pane p = currPane.getNext();
        if (p != null) {
            history.add(currPane);
            setPane(p);
        }
    }

    boolean checkSingleTestManager() {
        /*
        if (contextManager != null) {
            ExecToolManager em = tool.getExecToolManager();
            Desktop d = em.getDesktop();
            if(!em.checkOpenNewTool(d, contextManager)) {
                return false;
            }
        }
         */
        return true;

    }

    private void doDone() {
        setVisible(false);
        if (config == null)
            throw new IllegalStateException();

        if (workDir != null)
            config.setWorkDirectory(workDir);

        qswListener.finishQSW(testSuite, workDir, config,
                showConfigEditorFlag, runTestsFlag);

/*
        tool.setInterviewParameters(config);

        if (showConfigEditorFlag)
            tool.showConfigEditor(runTestsFlag);
        else if (runTestsFlag)
            tool.runTests();
*/
    }


    //private ExecTool tool;

    private Pane currPane;
    private Pane taskPane;
    private ConfigPane configPane;
    private Pane testSuitePane;
    private Pane newWorkDirPane;
    private Pane openWorkDirPane;
    private Pane endPane;

    private int task;
    private static final int UNSET = 0, NEW = 1, RESUME = 2, BROWSE = 3;
    // indicates using jtm or jti templates
    private boolean jtmTemplate = true;

    private Properties configData;
    private File configFile;
    private Properties jtmData;
    private File jtmFile;

    private QSW_Listener qswListener;
    private Icon logoIcon;
    private InterviewParameters config;
    private ContextManager contextManager;
    private TestSuite testSuite;
    private WorkDirectory workDir;
    private boolean showConfigEditorFlag;
    private boolean runTestsFlag;

    private File installDir;
    private File installParentDir;
    private boolean installDirIsTestSuite;
    private boolean installParentDirIsTestSuite;
    private File userDir;
    private boolean userDirIsTestSuite;
    private boolean userDirIsWorkDirectory;

    private JPanel body;
    private JPanel main;
    private JTextField head;
    private JTextField foot;
    private JButton backBtn;
    private JButton nextBtn;
    private JButton doneBtn;
    private Listener listener = new Listener();
    private KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    private Vector history = new Vector();
    private boolean allowConfigLoadOutsideDefault;
    private File defaultConfigSavePath;
    private boolean allowConfigSaveOutsideDefault;
    private File defaultConfigLoadPath;

    /**
     * This is the name of system property to turn QuickStartWizard off.
     * You should specify "true" value for this property to disable
     * QuickStartWizard.
     */
    static final String QSW_OFF_PROPERTY = "com.sun.javatest.qswDisabled";

    static final String QSW_OFF_FILE = "qswDisabled";

    private static final boolean qswDisabled = initQSWDisabled();

    static boolean isQswDisabled() {
        return qswDisabled;
    }

    /**
     * @return true, if disable
     */
    private static boolean initQSWDisabled() {
        if (Boolean.parseBoolean(System.getProperty(QSW_OFF_PROPERTY))) {
            return true;
        }
        try {
            return ResourceLoader.getResourceFile(QSW_OFF_FILE, null) != null;
        } catch (Exception ignore) {
            return false;
        }
    }

    private class Listener implements ActionListener
    {
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src == nextBtn)
                doNext();
            else if (src == backBtn)
                doBack();
            else if (src == doneBtn)
                doDone();
            else
                System.err.println("QSG.Listener " + e);
        }
    }

    private class FilePanel extends JPanel {

        FilePanel(String key, final JFileChooser chooser) {
            uif.initPanel(this, key, new BorderLayout(), false);

            label = uif.createLabel(key, true);

            field = uif.createInputField(key, label);
            currPathComp = field;

            button = uif.createButton(key + ".browse");
            button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        // seed the path if the user has started to enter
                        // something
                        String path = getPath();
                        if (chooser instanceof FileChooser  &&
                                ((FileChooser)chooser).getChosenExtension() != null &&
                                ((FileChooser)chooser).getChosenExtension().equals(".jti")) {

                            File f = InterviewEditor.loadConfigFile(
                                contextManager, parent, uif,
                                (FileChooser)chooser);
                            path = (f == null ? null : f.getPath());
                        }
                        else {
                            // set chooser to value in field?
                            if (path != null && path.length() > 0)
                                chooser.setSelectedFile(new File(path));

                            int rc = chooser.showDialog(body, chooser.getApproveButtonText());
                            if (rc != JFileChooser.APPROVE_OPTION)
                                return;

                            path = chooser.getSelectedFile().getPath();
                        }

                        field.setText(path);
                        if (combo != null)
                                combo.setSelectedItem(path);
                    }
                });

            add(label, BorderLayout.WEST);
            add(field, BorderLayout.CENTER); // default, may be changed to combo
            add(button, BorderLayout.EAST);
        }

        public void setEnabled(boolean b) {
            super.setEnabled(b);
            label.setEnabled(b);
            currPathComp.setEnabled(b);
            button.setEnabled(b);
        }

        void setSuggestions(String[] suggestions) {
            if (suggestions == null || suggestions.length == 0)
                setPathComponent(field);
            else {
                if (combo == null) {
                    combo = uif.createChoice(getName()/*key*/, true, label);
                    //combo.setEditable(true);
                    combo.setFont(combo.getFont().deriveFont(Font.PLAIN));
                    if (listener != null) {
                        // would be better to use editor addActionListener
                        Component c = combo.getEditor().getEditorComponent();
                        if (c instanceof JTextField)
                            ((JTextField) c).getDocument().addDocumentListener(listener);
                    }
                }
                setPathComponent(combo);
                combo.removeAllItems();
                for (int i = 0; i < suggestions.length; i++)
                    combo.addItem(suggestions[i]);
            }
        }

        /**
         * @return Null if no file path was provided, or if the given path is
         * rejected due to validation reasons.
         */
        File getFile() {
            String path = getPath();
            return (path == null || path.length() == 0 ? null : new File(path));
        }

        String getPath() {
            //System.err.println("QSG.FP.getPath currComp=" + currPathComp);
            //System.err.println("QSG.FP.getPath combo.selectedItem=" + (combo == null ? "null" : String.valueOf(combo.getSelectedItem())));
            if (currPathComp == field)
                return field.getText();
            else if (currPathComp == combo) {
                Component c = combo.getEditor().getEditorComponent();
                if (c.isShowing() && c instanceof JTextField)
                    return ((JTextField) c).getText();
                else
                    return ((String) (combo.getSelectedItem()));
            }
            else
                throw new IllegalStateException();
        }

        void setDocumentListener(DocumentListener l) {
            field.getDocument().addDocumentListener(l);
            listener = l;
        }

        private void setPathComponent(JComponent newPathComp) {
            //System.err.println("QSG.FP.setPathComponent " + newPathComp);
            if (newPathComp != currPathComp) {
                if (currPathComp != null)
                    remove(currPathComp);
                add(newPathComp, BorderLayout.CENTER);
                newPathComp.setEnabled(isEnabled());
                label.setLabelFor(newPathComp);
                currPathComp = newPathComp;
            }
        }

        private JLabel label;
        private JTextField field;
        private JComboBox combo;
        private JComponent currPathComp;
        private JButton button;
        private DocumentListener listener;
    }

    private abstract class Pane extends JPanel {
        Pane(String key) {
            paneKey = key;
            setLayout(new GridBagLayout());
            int dpi = uif.getDotsPerInch();
            setPreferredSize(new Dimension(5 * dpi, (int) (2.5 * dpi)));
            head = uif.getI18NString(paneKey + ".hd");
        }

        void stateChanged() { }

        String getHead() {
            return head;
        }

        void update() {
            updateNextButton();
        }

        abstract void updateNextButton();

        Pane getNext() {
            return null;
        }

        JCheckBox addCheck(String key) {
            JCheckBox cb = uif.createCheckBox(paneKey + "." + key);
            cb.addChangeListener(changeListener);

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets.left = 10;
            c.insets.bottom = 5;
            c.weightx = 1;
            add(cb, c);

            return cb;
        }

        JRadioButton addChoice(String key, ButtonGroup bg) {
            JRadioButton rb = uif.createRadioButton(paneKey + "." + key, bg);
            rb.addChangeListener(changeListener);

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets.left = 10;
            c.insets.bottom = 5;
            c.weightx = 1;
            add(rb, c);

            return rb;
        }

        FilePanel addFile(String key, JFileChooser chooser) {
            return addFile(key, chooser, null);
        }

        FilePanel addFile(String key, final JFileChooser chooser, final JRadioButton rb) {
            final FilePanel fp = new FilePanel(paneKey + "." + key, chooser);

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets.bottom = 10;
            c.insets.left = (rb == null ? 0 : 30);
            c.weightx = 1;
            add(fp, c);

            if (rb != null) {
                fp.addAncestorListener(new AncestorListener() {
                        public void ancestorAdded(AncestorEvent e) {
                            fp.setEnabled(rb.isSelected());
                        }
                        public void ancestorMoved(AncestorEvent e) { }
                        public void ancestorRemoved(AncestorEvent e) { }
                    });
                rb.addChangeListener(new ChangeListener() {
                        public void stateChanged(ChangeEvent e) {
                            fp.setEnabled(rb.isSelected());
                        }
                    });
            }

            fp.setDocumentListener(new DocumentListener() {
                    public void changedUpdate(DocumentEvent e) {
                        changeListener.stateChanged(new ChangeEvent(fp));
                    }
                    public void insertUpdate(DocumentEvent e) {
                        changedUpdate(e);
                    }
                    public void removeUpdate(DocumentEvent e) {
                        changedUpdate(e);
                    }
                });

            return fp;
        }

        JTextArea addText() {
            return addText(true);
        }

        JTextArea addText(boolean initContent) {
            // text area...
            JTextArea textArea = uif.createTextArea("qsw.text");
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setOpaque(false);
            textArea.setBackground(UIFactory.Colors.TRANSPARENT.getValue());
            textArea.setWrapStyleWord(true);
            // override JTextArea focus traversal keys, resetting them to
            // the Component default (i.e. the same as for the parent.)
            textArea.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
            textArea.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
            if (initContent)
                textArea.setText(uif.getI18NString(paneKey + ".txt"));
            /*
              // set enter to be same as Next
              {
              InputMap im = textArea.getInputMap();
              im.put(enterKey, "next");
              ActionMap am = textArea.getActionMap();
              am.put("next", valueAction);
              }
            */
            GridBagConstraints c = new GridBagConstraints();
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets.top = 20;
            c.insets.right = 10;
            c.insets.bottom = 10;
            c.fill = GridBagConstraints.BOTH;
            add(textArea, c);
            return textArea;
        }

        private ChangeListener changeListener = new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    Pane.this.stateChanged();
                }
            };

        private String paneKey;
        private String head;
    };

    //------------------------------------------------------------------------------

    private class TaskPane extends Pane {
        TaskPane() {
            super("qsw.tsk");
            addText();
            newRun = addChoice("new", bg);
            resumeRun = addChoice("resume", bg);
            browse = addChoice("browse", bg);
        }

        void stateChanged() {
            task = (newRun.isSelected() ? NEW
                    : resumeRun.isSelected() ? RESUME
                    : browse.isSelected() ? BROWSE
                    : UNSET);
            updateNextButton();
        }

        void updateNextButton() {
            nextBtn.setEnabled(task != UNSET);
        }


        Pane getNext() {

            return (task == NEW || task == BROWSE ? (Pane) testSuitePane
                    : task == RESUME ? (Pane) openWorkDirPane
                    : null);
        }


        private ButtonGroup bg = new ButtonGroup();
        private JRadioButton newRun;
        private JRadioButton resumeRun;
        private JRadioButton browse;
    };

    //------------------------------------------------------------------------------

    private class ConfigPane extends Pane  {
        ConfigPane() {
            super("qsw.cfg");
            addText();
            jtiChooser = new FileChooser(true);
            jtiChooser.addChoosableExtension(".jti",
                                          uif.getI18NString("qsw.cfg.jtiFiles"));

            jtmChooser = new FileChooser(true);
            jtmChooser.setDialogTitle(uif.getI18NString("qsw.cfg.jtmChooser.title"));
            jtmChooser.addChoosableExtension(".jtm",
                                          uif.getI18NString("qsw.cfg.jtmFiles"));

            newConfig = addChoice("new", bg);
            jtmConfig = addChoice("template", bg);
            jtmPanel = addFile("jtm.field", jtmChooser, jtmConfig);

        }


        void tuneTemplateFilter() {
            if (contextManager != null &&
                    contextManager.getFeatureManager() != null &&
                    contextManager.getFeatureManager().isEnabled(FeatureManager.WD_WITHOUT_TEMPLATE) ) {
                jtmChooser.addChoosableExtension(".jti",
                                              uif.getI18NString("qsw.cfg.jtiFiles"));
            }
        }


        private void tunePane() {

            if (contextManager != null) {

                jtmChooser.setCurrentDirectory(contextManager.getDefaultTemplateLoadPath());

                if (contextManager.getFeatureManager() != null) {
                    FeatureManager fm = contextManager.getFeatureManager();
                    jtmConfig.setEnabled(fm.isEnabled(FeatureManager.TEMPLATE_USAGE));
                    newConfig.setEnabled(fm.isEnabled(FeatureManager.WD_WITHOUT_TEMPLATE));
                }
            }
        }


        void stateChanged() {
            showError(null);
            updateNextButton();
        }


        void updateNextButton() {

            tunePane();

            if (newConfig.isSelected()) {
                nextBtn.setEnabled(true);
            } else if (jtmConfig.isSelected()) {
                File f = jtmPanel.getFile();
                nextBtn.setEnabled(f != null && f.exists() && f.isFile());
            }
            else
                nextBtn.setEnabled(false);
        }

        Pane getNext() {
            if (bg.getSelection() == null)
                return null;

            if (newConfig.isSelected()) {
                configData = null;
                configFile = null;
                jtmData = null;
                jtmFile = null;
            } else if (jtmConfig.isSelected()) {
                jtmData = new Properties();
                jtmFile = chkConfigFile(jtmPanel, jtmData);
                if (jtmFile == null) {
                    return null;
                }

                if (jtmData != null) {
                    String jtmDataInterview = (String) (jtmData.get("INTERVIEW"));

                    if (jtmDataInterview != null
                        && !config.getClass().getName().equals(jtmDataInterview)) {
                        uif.showError("qsw.ts.templateMismatch");
                        return null;
                    }

                    jtmTemplate = Boolean.parseBoolean((String)jtmData.get("IS_TEMPLATE"));

                    if (config != null) {
                        try {
                            if (jtmTemplate) {
                                testSuite.loadInterviewFromTemplate(jtmFile, config);
                                config.setTemplatePath(jtmFile.getAbsolutePath());
                            } else {
                                config.load(jtmFile);
                            }
                        } catch (TestSuite.Fault ex) {
                            // todo
                            ex.printStackTrace();
                        } catch (IOException ex) {
                            // todo
                            ex.printStackTrace();
                        } catch (Interview.Fault ex) {
                            uif.showError("qsw.ts.cantLoadConfig", ex.getMessage());
                            return null;
                        }
                    }
                }

                return (task == NEW ? (Pane) newWorkDirPane : (Pane) endPane);
            }



            if (configData != null) {
                String configDataInterview = (String) (configData.get("INTERVIEW"));

                if (configDataInterview != null
                    && !config.getClass().getName().equals(configDataInterview)) {
                    uif.showError("qsw.ts.configMismatch");
                    return null;
                }

                try {
                    config.load(configData, configFile);
                }
                catch (InterviewParameters.Fault e) {
                    uif.showError("qsw.ts.cantLoadConfig", e.getMessage());
                    return null;
                }
            }

            return (config == null ? null : task == NEW ? (Pane) newWorkDirPane : (Pane) endPane);

        }


        private File chkConfigFile(FilePanel panel, Properties data ) {
            String path = panel.getPath();
            File file = null;
            if (path.length() == 0) {
                showError("qsw.cfg.noFile");
                return null;
            }

            File f = new File(path);
            if (!f.exists()) {
                showError("qsw.cfg.cantFindFile");
                return null;
            }

            if (!f.isFile()) {
                showError("qsw.cfg.badFile");
                return null;
            }

            try {
                InputStream in = new BufferedInputStream(new FileInputStream(f));

                try {
                    data.clear();
                    data.load(in);
                    file = f;
                }
                catch (RuntimeException e) {
                        // can get IllegalArgumentException if the file is corrupt
                    uif.showError("qsw.cfg.cantReadFile", e);
                return null;
                }
                finally {
                    in.close();
                }
            }
            catch (IOException e) {
                uif.showError("qsw.cfg.cantReadFile", e);
                return null;
            }

            return file;
        }

        private ButtonGroup bg = new ButtonGroup();
        private JRadioButton newConfig;
        private JRadioButton fileConfig;
        private JRadioButton jtmConfig;
        private FilePanel filePanel;
        private FilePanel jtmPanel;
        private FileChooser jtiChooser;
        private FileChooser jtmChooser;

        private long configLastModified;

    };

    //------------------------------------------------------------------------------

    private class TestSuitePane extends Pane {
        TestSuitePane() {
            super("qsw.ts");
            addText();
            chooser = new TestSuiteChooser();
            testSuitePanel = addFile("file", chooser);
        }

        void stateChanged() {
            showError(null);
            updateNextButton();
        }

        void update() {
            super.update();

            // update file with suggestions based on -
            // - config template
            // - installation directory and its parent
            // - current directory

            Set s = new TreeSet();
            if (configData != null) {
                String configTestSuite = (String) (configData.get("TESTSUITE"));
                if (configTestSuite != null)
                    s.add(configTestSuite);
            }

            if (userDirIsTestSuite)
                s.add(userDir.getPath());

            if (installDirIsTestSuite)
                s.add(installDir.getPath());

            if (installParentDirIsTestSuite)
                s.add(installParentDir.getPath());

            if (s.size() > 0)
                testSuitePanel.setSuggestions((String[]) (s.toArray(new String[s.size()])));
        }

        void updateNextButton() {
            File file = testSuitePanel.getFile();
            TestSuite chooserTestSuite = chooser.getSelectedTestSuite();

            if (file == null) {
                nextBtn.setEnabled(false);
            }
            else if (chooserTestSuite != null && chooserTestSuite.getRoot().equals(file)) {
                nextBtn.setEnabled(true);
            }
            else {
                nextBtn.setEnabled(TestSuite.isTestSuite(file));
            }
        }

        Pane getNext() {
            // update test suite and check validity?
            // if ts == chooser.getSelectedFile, use chooser.getSelectedTestSuite
            File file = testSuitePanel.getFile();
            if (file == null) {
                showError("qsw.ts.noFile");
                return null;
            }
            else if (testSuite == null || !testSuite.getRoot().equals(file) || config == null) {
                try {
                    TestSuite chooserTestSuite = chooser.getSelectedTestSuite();
                    if (chooserTestSuite != null
                        && chooserTestSuite.getRoot().equals(file))
                        testSuite = chooserTestSuite;
                    else
                        testSuite = TestSuite.open(file);
                }
                catch (FileNotFoundException e) {
                    showError("qsw.ts.cantFindFile");
                    return null;
                }
                catch (TestSuite.Fault e) {
                    uif.showError("qsw.ts.cantOpen",
                                  new Object[] { file, e.getMessage() });
                    return null;
                }

                // now that test suite is set, try to update config too
                try {
                    if (config != null) {
                        config.dispose();
                    }
                    config = testSuite.createInterview();
                }
                catch (TestSuite.Fault e) {
                    uif.showError("qsw.ts.cantCreateInterview", e.getMessage());
                    return null;
                }
                catch (Throwable t) {
                    uif.showError("qsw.ts.cantCreateInterview", t.toString());
                    return null;
                }

            }

            if (testSuite != null) {
                contextManager = ExecTool.createContextManager(testSuite);
            }
            configPane.tuneTemplateFilter();

            if (!checkSingleTestManager()) {
                return null;
            }


            return (task == NEW || task == BROWSE ? (Pane) configPane
                    : task == RESUME ? (Pane) openWorkDirPane
                    : null);

           // return (config == null ? null : task == NEW ? (Pane) newWorkDirPane : (Pane) endPane);
        }

        private FilePanel testSuitePanel;
        private TestSuiteChooser chooser;
    };

    //------------------------------------------------------------------------------

    private abstract class WorkDirPane extends Pane {
        WorkDirPane(String key) {
            super(key);
            textArea = addText();
            chooser = new WorkDirChooser(true);
            workDirPanel = addFile("file", chooser);
        }

        protected final JTextArea textArea;
        protected final FilePanel workDirPanel;
        protected WorkDirChooser chooser;
    };

    private class CreateWorkDirPane extends WorkDirPane {
        CreateWorkDirPane() {
            super("qsw.nwd");
            chooser.setMode(WorkDirChooser.NEW);
        }

        void stateChanged() {
            updateNextButton();
        }

        public void update() {
            chooser.setTestSuite(testSuite);
        }

        void updateNextButton() {
            String path = workDirPanel.getPath();
            nextBtn.setEnabled(path != null && path.length() > 0);
        }

        Pane getNext() {
            File file = workDirPanel.getFile();

            if (file == null) {
                showError("qsw.nwd.noFile");
                return null;
            }
            else if (workDir == null || !workDir.getRoot().equals(file)) {
                try {
                    WorkDirectory chooserWorkDir = chooser.getSelectedWorkDirectory();
                    if (chooserWorkDir != null && canonicalEquals(chooserWorkDir.getRoot(), file)) {
                        // workdir was created inside the chooser
                        workDir = chooserWorkDir;
                    }
                    else
                        workDir = WorkDirectory.create(file, testSuite);

                    if (jtmFile != null && workDir != null && jtmTemplate) {
                        TemplateUtilities.setTemplateFile(workDir, jtmFile, true);
                        //tool.setWorkDir(workDir, true);
                        //tool.getConfigHandler().loadConfigNoUI(jtmFile);
                    }

                }
                catch (WorkDirectory.Fault e) {
                    uif.showError("qsw.nwd.cantCreate",
                                  new Object[] { file, e.getMessage() });
                    return null;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            return endPane;
        }
    };

    private static boolean canonicalEquals(File f1, File f2) {
        try {
            File c1 = f1.getCanonicalFile();
            File c2 = f2.getCanonicalFile();
            return (c1.equals(c2));
        }
        catch (IOException e) {
            return false;
        }
    }

    private class OpenWorkDirPane extends WorkDirPane {
        OpenWorkDirPane() {
            super("qsw.owd");
            chooser.setMode(WorkDirChooser.OPEN_FOR_ANY_TESTSUITE);

            if (userDirIsWorkDirectory)
                workDirPanel.setSuggestions(new String[] { userDir.getPath() });
        }

        void stateChanged() {
            updateNextButton();
        }

        void updateNextButton() {
            File file = workDirPanel.getFile();
            WorkDirectory chooserWorkDir = chooser.getSelectedWorkDirectory();

            if (file == null)
                nextBtn.setEnabled(false);
            else if (chooserWorkDir != null && chooserWorkDir.getRoot().equals(file))
                nextBtn.setEnabled(true);
            else
                nextBtn.setEnabled(WorkDirectory.isWorkDirectory(file));
        }

        Pane getNext() {
            File file = workDirPanel.getFile();

            if (file == null) {
                showError("qsw.owd.noFile");
                return null;
            }
            else if (workDir == null || !workDir.getRoot().equals(file)) {
                try {
                    WorkDirectory chooserWorkDir = chooser.getSelectedWorkDirectory();
                    if (chooserWorkDir != null
                        && chooserWorkDir.getRoot().equals(file))
                        workDir = chooserWorkDir;
                    else
                        workDir = WorkDirectory.open(file);
                    testSuite = workDir.getTestSuite();
                }
                catch (FileNotFoundException e) {
                    showError("qsw.owd.cantFindFile");
                    return null;
                }
                catch (WorkDirectory.Fault e) {
                    uif.showError("qsw.owd.cantOpen",
                                  new Object[] { file, e.getMessage() });
                    return null;
                }

                try {
                    if (config != null) {
                        config.dispose();
                    }
                    config = testSuite.createInterview();
                    // should get latest config for workDir
                }
                catch (TestSuite.Fault e) {
                    uif.showError("qsw.owd.createCreateInterview", e.getMessage());
                    return null;
                }

                FileHistory h = FileHistory.getFileHistory(workDir, "configHistory.jtl");
                File latestConfigFile = h.getLatestEntry();
                if (latestConfigFile != null) {
                    try {
                        config.load(latestConfigFile);
                    }
                    catch (IOException e) {
                        uif.showError("qsw.owd.cantLoadDefaultConfig",
                                      new Object[] { latestConfigFile, e });
                        return null;
                    }
                    catch (InterviewParameters.Fault e) {
                        uif.showError("qsw.owd.cantLoadDefaultConfig",
                                      new Object[] { latestConfigFile, e.getMessage() });
                        return null;
                    }
                }
                if (testSuite != null) {
                    contextManager = ExecTool.createContextManager(testSuite);

                    if (!checkSingleTestManager()) {
                        return null;
                    }
                }

            }

            return endPane;
        }
    };

    //------------------------------------------------------------------------------

    private class EndPane extends Pane {
        EndPane() {
            super("qsw.end");
            configTextArea = addText(false);
            configCheck = addCheck("cfg");
            runTestsTextArea = addText(false);
            runTestsCheck = addCheck("run");
        }

        void update() {
            super.update();

            // update text according to whether config is complete or not
            // (and so whether config editor required)
            // set configCheck if config incomplete
            Integer haveConfig = new Integer(configData == null ? 0 : 1);

            StringBuffer sb = new StringBuffer();
            if (config.isFinishable()) {
                // "Your configuration is complete, but you can change it by using
                // the Configuration Editor."
                sb.append(uif.getI18NString("qsw.end.cfgComplete"));
                configCheck.setSelected(task == BROWSE ? false : true);
            }
            else {
                if (configData != null) {
                    // "Your configuration is incomplete."
                    sb.append(uif.getI18NString("qsw.end.cfgIncomplete"));
                    sb.append(" ");
                }
                // "Before you can run tests, you must complete (a|your) configuration
                // by using the Configuration Editor."
                sb.append(uif.getI18NString("qsw.end.needEditor", haveConfig));
                configCheck.setSelected(task == BROWSE ? false : true);
            }
            sb.append(" ");
            // "You can open the Configuration Editor automatically by
            // checking the box below. You can also open the Configuration Editor
            // at any time from the Configure menu."
            sb.append(uif.getI18NString("qsw.end.editor"));
            configTextArea.setText(sb.toString());

            if (task == BROWSE) {
                runTestsTextArea.setVisible(false);
                runTestsCheck.setVisible(false);
            }
            else {
                runTestsTextArea.setVisible(true);
                runTestsCheck.setVisible(true);
                // "Once (the|your) configuration is complete, you can run tests automatically
                // by checking the box below. You can also run tests at any time from the
                // Run Tests menu."
                runTestsTextArea.setText(uif.getI18NString("qsw.end.runTests", haveConfig));
            }

            stateChanged();
        }

        void updateNextButton() {
            nextBtn.setEnabled(false);
        }

        void stateChanged() {
            if (runTestsCheck.isSelected()
                && (config == null || !config.isFinishable()))
                configCheck.setSelected(true);
            showConfigEditorFlag = configCheck.isSelected();
            runTestsFlag = runTestsCheck.isSelected();
        }

        private JTextArea configTextArea;
        private JCheckBox configCheck;
        private JTextArea runTestsTextArea;
        private JCheckBox runTestsCheck;
    };

    protected void windowClosingAction(AWTEvent e) {
        setVisible(false);
        closeExecTool(e);
    }

}

