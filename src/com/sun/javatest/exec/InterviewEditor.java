/*
 * $Id$
 *
 * Copyright (c) 2010, 2012, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.interview.Help;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.AWTEvent;
import java.awt.FocusTraversalPolicy;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.help.BadIDException;
import javax.help.InvalidHelpSetContextException;
import javax.help.JHelpContentViewer;
import javax.help.Map.ID;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.sun.interview.Interview;
import com.sun.interview.Question;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.JavaTestError;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.exec.WorkDirChooseTool.ExecModelStub;
import com.sun.javatest.tool.FileChooser;
import com.sun.javatest.tool.FileHistory;
import com.sun.javatest.tool.HelpLink;
import com.sun.javatest.tool.Preferences;
import com.sun.javatest.tool.ToolDialog;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.util.Debug;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog to edit InterviewParameters object.
 *
 * InterviewEditor keeps reference to the main InterviewParameters object,
 * but never change it.
 * <br>
 * Before editing interview the main InterviewParameters object is synced
 * with the view object.
 * <br>
 * When view object is loaded or saved, all registered observers are notified.
 *
 */
public class InterviewEditor extends ToolDialog {

    public InterviewEditor(JComponent parent, UIFactory uif,
            InterviewParameters ip) {

        super(parent, uif, "ce", ToolDialog.MODAL_DOCUMENT);
        WorkDirectory wd = ip.getWorkDirectory();
        if (wd == null) {
            throw new IllegalArgumentException(uif.getI18NString("ce.wdNull.err"));
        }
        mainConfig = ip;
        try {
            //this.key = key;
            this.ext = getExtention();
            viewConfig = ip.getTestSuite().createInterview();
            viewConfig.setWorkDirectory(ip.getWorkDirectory());
            history = FileHistory.getFileHistory(wd, getHistoryFileName());
        } catch (TestSuite.Fault e) {
            // ignore, for now; should not happen
        }
    }
    public InterviewEditor(JComponent parent, UIFactory uif,
            InterviewParameters ip, ContextManager cm) {
        this(parent, uif, ip);
        setContextManager(cm);
    }

    /**
     * Sets contextManager to the passed value.
     * @param cm - ContextManager to use
     */
    void setContextManager(ContextManager cm) {
        this.contextManager = cm;
    }

    /**
     * Returns previously created or set ContextManager. If not set, creates
     * the new instance.
     */
    ContextManager getContextManager() {
        if (contextManager == null) {
            try {
                contextManager = (ContextManager) ((Class.forName(
                        "com.sun.javatest.exec.ContextManager")).newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return contextManager;
    }


    /**
     * Returns extension for files to be saved. Subclasses like TemplateEditor
     * might override this method.
     *
     * @return default extension
     */
    protected String getExtention() {
        return CONFIG_EXTENSION;
    }

    /**
     * Returns file name to store history of configuration files.
     * This implementation returns "configHistory.jtl". Subclasses might
     * override this method to return alternative value.
     */
    protected String getHistoryFileName() {
        return CONFIG_HISTORY;
    }

    private void setRestorer(CE_View view) {
        getRestorer().setRestorePolicy(Restorer.RESTORE_ALL);
        getRestorer().setWindowKey(getRestorerWindowKey(view == fullView));
    }

    protected String getRestorerWindowKey(boolean isFullView) {
        return "confEdit.config" + (isFullView ? ".f" : ".s");
    }

    /**
     * Adds passed file to the history.
     * @param f - file to be added.
     */
    void addToHistory(File f) {
        if (history == null) {
            WorkDirectory wd = viewConfig.getWorkDirectory();
            if (wd == null) {
                return;
            }
            history = FileHistory.getFileHistory(wd, getHistoryFileName());
        }
        history.add(f);
    }

    /**
     * Syncs mainConfig and viewConfig
     */
    private void sync() {
        try {
            copy(mainConfig, viewConfig);
        } catch (Interview.Fault e) {
            uif.showError("ce.show.error", e.getMessage());
        }
    }

    /**
     * Starts editing new config. Supposed to be called outside.
     */
    public void newConfig() {
        viewConfig.clear();
        show(FULL_MODE);
    }

    /**
     * Start editing empty configuration.
     */
    private void newConfigAsk() {
        if (askAndSave("ce.clear.warn")) {
            newConfig();
        }
    }
    /**
     * Show dialog.
     */
    public void edit(int mode) {
        sync();
        show(mode);
    }

    /**
     * @return  mode that will be used by WorkDirChooseTool to select file.
     */
    public int getFileChooserMode() {
        return WorkDirChooseTool.LOAD_CONFIG;
    }

    /**
     * Show choose file dialog and then load new file.
     * Supposed to be invoked from outside of editor.
     * Doesn't expect that viewConfig can be changed.
     */
    public void loadConfig() {
        loadConfig0(false);
    }

    /**
     * Show choose file dialog and then load new file.
     * The dialog depends on fileChooserMode setting. It can be either
     * simple JFileChooser or "advanced" home made file chooser.
     * @param ask if true, dialog asking whether to save changes will appear
     *        in case of unsaved changes.
     */
    protected void loadConfig0(boolean ask) {
        TestSuite ts = viewConfig.getTestSuite();
        ExecModelStub em = new ExecModelStub(ts, contextManager);
        try {
            em.setWorkDir(mainConfig.getWorkDirectory(), true);
        } catch (Exception ignore) {
            // stub never throws exceptions
        }
        WorkDirChooseTool fc =  WorkDirChooseTool.getTool((JComponent)parent,
                uif, em, getFileChooserMode(), ts, true);
        WorkDirChooseTool.ChosenFileHandler cfh =
                new WorkDirChooseTool.ChosenFileHandler();
        fc.setChosenFileHandler(cfh);
        fc.doTool();

        File f = cfh.file;
        if (f != null && (!ask || askAndSave("ce.load.warn"))) {
            loadConfigFromFile(f);
            if (cfh.isEditConfig && !isVisible()) {
                edit(FULL_MODE);
            }
        }
    }

    /**
     * Works similar to loadConfig(), but asks to save changes if any
     * before reload.
     */
    private void loadConfigAsk() {
        loadConfig0(true);
    }

    /**
     *
     * @param f
     */
    public void loadAndEdit(File f) {
        if (f == null) {
            return;
        }
        loadConfigFromFile(f);
        notifyObservers();
        show(FULL_MODE);
    }

    /**
     * Shows file chooser dialog.
     * @return chosen file or null.
     */
    private File chooseConfigFile() {
        File mainConfigFile = mainConfig.getFile();
        FileChooser fileChooser = getFileChooser();
        if (mainConfigFile != null)
            fileChooser.setCurrentDirectory(mainConfigFile.getParentFile());
        return  loadConfigFile(getContextManager(), parent, uif, fileChooser);
    }

    /**
     * Updates viewConfig, notifies observers of the change.
     * @param file File to load.
     */
    public void loadConfigFromFile(File file) {
        if (file == null) {
            return;
        }
        try {
            viewConfig.load(file);
            if (currView != null && currView.isShowing())
                currView.load();
            addToHistory(file);
            updateTitle();
            notifyObservers();
        } catch (FileNotFoundException e) {
            uif.showError("ce.load.cantFindFile", file);
        } catch (IOException e) {
            uif.showError("ce.load.error", new Object[] { file, e } );
        } catch (Interview.Fault e) {
            uif.showError("ce.load.error", new Object[] { file, e.getMessage() } );
        }
    }


    public void save() {
        save0();
    }

    // return true if saved, false if cancelled/error
    private boolean save0() {
        // Use the filename saved in the viewConfig.
        // By default, this will have been copied from the mainConfig,
        // but it may have been cleared if clear() has been called, thereby
        // making "save" behave as "saveAs".
        return save0(viewConfig.getFile());
    }

    public void saveAs() {
        save0(null);
    }

    // return true if saved, false if cancelled/error
    private boolean save0(File file) {
        if (file == null) {
            File mainConfigFile = mainConfig.getFile();
            File mainConfigDir = (mainConfigFile == null ? null : mainConfigFile.getParentFile());
            file = getSaveFile(mainConfigDir);
            if (file == null)
                return false; // exit without saving
        }

        try {
            if (currView != null) {
                currView.save();
            }
            viewConfig.setFile(file);   // for subsequent use
            doSave(file);
            addToHistory(file);

            updateTitle();
            notifyObservers();

            return true;
        }
        catch (IOException e) {
            if (!file.canWrite())
                uif.showError("ce.save.cantWriteFile", file);
            else if (e instanceof FileNotFoundException)
                uif.showError("ce.save.cantFindFile", file);
            else
                uif.showError("ce.save.error", new Object[] { file, e } );
        }
        catch (Interview.Fault e) {
            uif.showError("ce.save.error", new Object[] { file, e.getMessage() } );
        }

        return false;
    }

    /**
     * Does actual save work. should be overriden, when needed.
     */
    protected void doSave(File file) throws Interview.Fault, IOException {
        viewConfig.save(file);
    }

    private File getSaveFile(File dir) {
        FileChooser fileChooser = getFileChooser();
        fileChooser.setDialogTitle(uif.getI18NString("ce.save.title"));
        return saveConfigFile(getContextManager(), parent, uif, fileChooser, dir,
                this.templateMode);
    }

    private FileChooser getFileChooser() {
        FileChooser fileChooser = new FileChooser(true);
        fileChooser.addChoosableExtension(ext, uif.getI18NString("ce.jtiFiles"));
        return fileChooser;
    }

    /**
     * In viewConfig differs from mainConfig asks user whether save changes
     * or not. Save changes in case of positive answer.
     * @param que
     * @return false iff user said "cancel".
     */
    private boolean askAndSave(String question) {
        if (isEdited()) {
            int rc = uif.showYesNoCancelDialog(question);

            switch (rc) {
                case JOptionPane.YES_OPTION: {
                    save();
                    return true;}
                case JOptionPane.NO_OPTION: return true;
                default: return false;
            }
        } else {
            return true;
        }
    }

    public void revert() {
        if (!isEdited())
            return;

        int rc = uif.showOKCancelDialog("ce.revert.warn");
        if (rc != JOptionPane.OK_OPTION)
            return;

        try {
            copy(mainConfig, viewConfig);
            if (currView != null && currView.isShowing())
                currView.load();
            updateTitle();
        } catch (Interview.Fault e) {
            uif.showError("ce.revert", e.getMessage());
        }
    }

    public void setRunPending(boolean b) {
        runPending = b;
    }

    public boolean isRunPending() {
        return runPending;
    }

    public void show() {
        if (stdView == null)
            initGUI();

        show(DEFAULT_MODE);
    }

    public void updateMenu() {
        FileHistory h = FileHistory.getFileHistory(viewConfig.getWorkDirectory(), getHistoryFileName());
        recentConfigMenu.setEnabled(h.getLatestEntry() != null);
    }

    public void show(int mode) {
        if (stdView == null) {
            initGUI();
        }
        updateTitle();
        updateMenu();
        switch (mode) {
            case DEFAULT_MODE:
                show(getDefaultView());
                break;

            case FULL_MODE:
                show(fullView);
                break;

            case STD_MODE:
                show(stdView);
                break;

            case STD_TESTS_MODE:
                stdView.showTab(CE_StdView.TESTS_PANE);
                show(stdView);
                break;

            case STD_EXCLUDE_LIST_MODE:
                stdView.showTab(CE_StdView.EXCLUDE_LIST_PANE);
                show(stdView);
                break;

            case STD_KEYWORDS_MODE:
                stdView.showTab(CE_StdView.KEYWORDS_PANE);
                show(stdView);
                break;

            case STD_KFL_MODE:
                stdView.showTab(CE_StdView.KFL_PANE);
                show(stdView);
                break;

            case STD_PRIOR_STATUS_MODE:
                stdView.showTab(CE_StdView.PRIOR_STATUS_PANE);
                show(stdView);
                break;

            case STD_ENVIRONMENT_MODE:
                stdView.showTab(CE_StdView.ENVIRONMENT_PANE);
                show(stdView);
                break;

            case STD_CONCURRENCY_MODE:
                stdView.showTab(CE_StdView.CONCURRENCY_PANE);
                show(stdView);
                break;

            case STD_TIMEOUT_FACTOR_MODE:
                stdView.showTab(CE_StdView.TIMEOUT_FACTOR_PANE);
                show(stdView);
                break;

            default:
                throw new IllegalArgumentException();
        }
    }

    public void show(ActionListener closeListener) {
        this.closeListener = closeListener;
        show();
    }

    public void show(int mode, ActionListener closeListener, boolean isTemplateMode) {
        this.closeListener = closeListener;
        show(mode);
    }

    public static final int DEFAULT_MODE = 0;
    public static final int FULL_MODE = 1;
    public static final int STD_MODE = 2;
    public static final int STD_TESTS_MODE = 3;
    public static final int STD_EXCLUDE_LIST_MODE = 4;
    public static final int STD_KEYWORDS_MODE = 5;
    public static final int STD_PRIOR_STATUS_MODE = 6;
    public static final int STD_ENVIRONMENT_MODE = 7;
    public static final int STD_CONCURRENCY_MODE = 8;
    public static final int STD_TIMEOUT_FACTOR_MODE = 9;
    public static final int TEMPLATE_FULL_MODE = 10;
    public static final int STD_KFL_MODE = 11;

    private void show(CE_View newView) {

        // update viewConfig from currView (if showing) else mainConfig
        if (currView != null && currView.isShowing()) {
            currView.save();
        }
        setRestorer(newView);
        setView(newView);
        setVisible(true);
    }

    @Override
    public void setVisible(boolean isVisible) {
        super.setVisible(isVisible);
        notifyObserversOfVisibility(isVisible);
    }

    private void setView(CE_View newView) {
        if (newView == null)
            throw new NullPointerException();

        if (currView != null && currView == newView) {
            currView.load();
        }

        if (currView != newView) {
            // note whether the focus is in the current view
            KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            Component fo = kfm.getPermanentFocusOwner();
            boolean focusInView = (fo != null && currView != null && currView.isAncestorOf(fo));

            currView = newView;

            // update currView from viewConfig
            currView.load();

            // set up the appropriate view and controls
            ((CardLayout)(views.getLayout())).show(views, currView.getName());

            // The following is a workaround for what may be a JDK bug.
            // As a result of changing the view, the permanent focus owner may no longer
            // be showing, and therefore not accepting any keyboard input.
            if (focusInView) {
                Container fcr = (currView.isFocusCycleRoot() ? currView : currView.getFocusCycleRootAncestor());
                FocusTraversalPolicy ftp = fcr.getFocusTraversalPolicy();
                Component c = ftp.getDefaultComponent(fcr);
                if (c != null)
                    c.requestFocusInWindow();
            }

            boolean currIsFull = (currView == fullView);
            markerMenu.setEnabled(currIsFull);
            searchMenu.setEnabled(currIsFull);
            (currIsFull ? viewFullBtn : viewStdBtn).setSelected(true);
            viewTagCheckBox.setEnabled(currIsFull);

            if (detailsBrowser != null)
                detailsBrowser.setQuestionInfoEnabled(currIsFull);

            updateTitle();
        }
    }

    public void close() {
        if (canInterruptTemplateCreation()) {
            doClose();
        } else {
            uif.showError("ce.force_close");
        }
    }

    public void doClose() {
        if (currView != null && !currView.isOKToClose()) {
            if (afterCloseCommand != null) {
                afterCloseCommand.run();
                afterCloseCommand = null;
            }
            return;
        }

        close(true);
    }

    protected void windowClosingAction(AWTEvent e) {
        if (!canInterruptTemplateCreation()) {
            uif.showError("ce.force_close");
            return;
        }

        if(fullView.isVisible()) {
            fullView.prepareClosing();
        }
        doClose();
    }


    private void close(boolean checkIfEdited) {
        if (currView == null)
            return;

        if (!isShowing())
            return;

        if (checkIfEdited && isEdited()) {
            int rc = uif.showYesNoCancelDialog("ce.close.warn");
            switch (rc) {
            case JOptionPane.YES_OPTION:
                if (save0()) {
                    break;
                } else {
                    if (afterCloseCommand != null) {
                        afterCloseCommand.run();
                        afterCloseCommand = null;
                    }
                    return;
                }

            case JOptionPane.NO_OPTION:
                break;
            default:
                if (afterCloseCommand != null) {
                    afterCloseCommand.run();
                    afterCloseCommand = null;
                }
                return;
            }
        }

        setVisible(false);

        // closeListener may have been set by show(ActionListener)
        if (closeListener != null) {
            ActionEvent e = new ActionEvent(this,
                                            ActionEvent.ACTION_PERFORMED,
                                            CLOSE);
            closeListener.actionPerformed(e);
            closeListener = null;
        }

        if (afterCloseCommand != null) {
            afterCloseCommand.run();
            afterCloseCommand = null;
        }

    }

    public void setCheckExcludeListListener(ActionListener l) {
        if (stdView == null)
            initGUI();

        stdView.setCheckExcludeListListener(l);
    }

    boolean isCurrentQuestionChanged() {
        if (currView != null && currView.isShowing())
            currView.save();

        Question mq = mainConfig.getCurrentQuestion();
        Question vq = viewConfig.getCurrentQuestion();
        return !equal(mq.getTag(), vq.getTag());
    }


    boolean isEdited() {
        if (currView != null && currView.isShowing())
            currView.save();

        return !equal(mainConfig, viewConfig);
    }

    /**
     * Compares two InterviewParameters objects for equivalence.
     * Two interview are equivalent when they both provide the same set
     * of questions and all corresponding questions have the same values.
     *
     * @param a first interview
     * @param b second interview
     * @return true, iff two interviews are equivalent.
     */
    public static boolean equal(InterviewParameters a, InterviewParameters b) {
        if (a == b) {
            return true;
        }

        if (a.isTemplate() != b.isTemplate()) {
            return false;
        }
        // do ez checks first
        if (a.getMarkersEnabled() != b.getMarkersEnabled()
            || a.getMarkersFilterEnabled() != b.getMarkersFilterEnabled()) {
            return false;
        }

        Map aQuestions = a.getAllQuestions();
        Map bQuestions = b.getAllQuestions();

        Set keys = new TreeSet();
        keys.addAll(aQuestions.keySet());
        keys.addAll(bQuestions.keySet());

        for (Iterator iter = keys.iterator(); iter.hasNext(); ) {
            String key = (String) (iter.next());
            Question aq = (Question) aQuestions.get(key);
            Question bq = (Question) bQuestions.get(key);
            if (aq == null || bq == null) {
                return false;
            }
            if (!aq.equals(bq)) {
                String empty = "";
                boolean eq = (aq.getStringValue() == null &&
                        empty.equals(bq.getStringValue())) ||
                        (empty.equals(aq.getStringValue()) &&
                        bq.getStringValue() == null);
                if(!eq) {
                    /*
                     Hopefully, question reloading is not required anymore,
                     not because questions are equal now, but because
                     the unexpected dialog no longer appears...

                      // if aq is not set, it will be set to the default value
                      // (if the default value had been specified for aq before)
                     aq.reload();
                    if (!aq.equals(bq)) {
                        return false;
                    }
                     */
                    return false;
                }
            }
        }
        // Checking external values
        Set aKeys = a.getPropertyKeys();
        Set bKeys = b.getPropertyKeys();

        if (aKeys == null || bKeys == null) {
            return aKeys == bKeys;
        }

        if (aKeys.size() != bKeys.size()) {
            return false;
        }

        for (Iterator iter = aKeys.iterator(); iter.hasNext(); ) {
            String key = (String)iter.next();
            if (!bKeys.contains(key)) {
                return false;
            }

            String aProp = a.retrieveProperty(key);
            String bProp = b.retrieveProperty(key);
            if (!equal(aProp, bProp)) {
                return false;
            }
        }


        return true;
    }

    /**
     * Registers new observer
     * @param o - observer to be added to the list
     */
    public void addObserver(Observer o) {
        if (o != null && !observers.contains(o)) {
            observers.add(o);
        }
    }
    /**
     * Removes observer from the list
     * @param o - observer to be removed from the list
     */
    public void removeObserver(Observer o) {
        if (o != null) {
            observers.remove(o);
        }
    }
    /**
     * Notifies registered observers of the change happened to viewConfig
     */
    protected void notifyObservers() {
        for (Observer obs: observers) {
            obs.changed(viewConfig);
        }
    }
    /**
     * Notifies registered observers of setVisible() method has been called.
     */
    protected void notifyObserversOfVisibility(boolean isVisible) {
        for (Observer obs: observers) {
            obs.changedVisibility(isVisible, this);
        }
    }

    private static boolean equal(String a, String b) {
        return (a == null || b == null ? a == b : a.equals(b));
    }

    @Override
    public void dispose() {
        if (viewConfig != null) {
            viewConfig.dispose();
            viewConfig = null;
        }
        super.dispose();
    }

    protected void initGUI() {
        setHelp("confEdit.window.csh");
        listener = new Listener();

        updateTitle();

        if (viewConfig.getHelpSet() != null) {
            // would prefer that the helpset came from the test suite
            infoPanel = new JHelpContentViewer(Help.getHelpSet(viewConfig));
            infoPanel.setName("info");
            int dpi = uif.getDotsPerInch();
            infoPanel.setPreferredSize(new Dimension(4*dpi, 3*dpi));
            infoPanel.putClientProperty(HelpLink.HELPBROKER_FOR_HELPLINK, uif.getHelpBroker());
        }

        fullView = new CE_FullView(viewConfig, infoPanel, uif, listener);
        if (customRenderersMap != null) {
            fullView.setCustomRenderers(customRenderersMap);
        }

        stdView = new CE_StdView(viewConfig, infoPanel, uif, listener);
        stdView.setParentToolDialog(this);

        initMenuBar();

        views = uif.createPanel("ce.views", new CardLayout(), false);
        views.add(fullView, fullView.getName());
        views.add(stdView, stdView.getName());

        if (infoPanel == null) {
            viewInfoCheckBox.setEnabled(false);
            viewInfoCheckBox.setSelected(false);
        }
        else {
            Preferences p = Preferences.access();
            boolean prefMoreInfo = p.getPreference(MORE_INFO_PREF, "true").equals("true");
            viewInfoCheckBox.setEnabled(true);
            viewInfoCheckBox.setSelected(prefMoreInfo);
        }

        // set body to be views+info or views
        if (viewInfoCheckBox.isSelected()) {
            views.setBorder(null);
            JSplitPane sp = uif.createSplitPane(JSplitPane.HORIZONTAL_SPLIT, views, infoPanel);
            sp.setDividerLocation(views.getPreferredSize().width + sp.getDividerSize());
            body = sp;
        }
        else {
            views.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            body = views;
        }

        // Don't register "shift alt D" on body, because body might change
        // if the more info is opened/closed.
        // Instead, register it on views and infoPanel
        views.registerKeyboardAction(listener, DETAILS, detailsKey,
                                           JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        if (infoPanel != null)
            infoPanel.registerKeyboardAction(listener, DETAILS, detailsKey,
                                               JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        setBody(body);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    protected JMenu createFileMenu() {
        String[] fileMenuItems = new String[] { SAVE, SAVE_AS, REVERT, null,
            NEW, LOAD, null, CLOSE };
        JMenu fileMenu = uif.createMenu("ce.file", fileMenuItems, listener);

        FileHistory h = FileHistory.getFileHistory(viewConfig.getWorkDirectory(), getHistoryFileName());
        FileHistory.Listener l = new FileHistory.Listener(h, 0, (ActionListener)listener);
        recentConfigMenu = uif.createMenu("ce.history");
        recentConfigMenu.setEnabled(h.getLatestEntry() != null);
        recentConfigMenu.addMenuListener(l);
        fileMenu.insert(recentConfigMenu, 4);

        return fileMenu;
    }

    private void initMenuBar() {
        listener = new Listener();
        JMenuBar menuBar = uif.createMenuBar("ce.menub");

        JMenu fileMenu = createFileMenu();
        menuBar.add(fileMenu);

        // marker menu
        markerMenu = fullView.getMarkerMenu();
        menuBar.add(markerMenu);

        // search menu
        searchMenu = fullView.getSearchMenu();
        menuBar.add(searchMenu);

        // view menu
        viewMenu = uif.createMenu("ce.view");
        viewMenu.addMenuListener(listener);
        ButtonGroup viewGroup = new ButtonGroup();

        viewFullBtn = uif.createRadioButtonMenuItem("ce.view", CE_View.FULL);
        viewFullBtn.setSelected(true);
        viewFullBtn.setActionCommand(CE_View.FULL);
        viewFullBtn.addActionListener(listener);
        viewGroup.add(viewFullBtn);
        viewMenu.add(viewFullBtn);

        viewStdBtn = uif.createRadioButtonMenuItem("ce.view", CE_View.STD);
        viewStdBtn.setActionCommand(CE_View.STD);
        viewStdBtn.addActionListener(listener);
        viewGroup.add(viewStdBtn);
        viewMenu.add(viewStdBtn);

        viewMenu.addSeparator();

        viewInfoCheckBox = uif.createCheckBoxMenuItem("ce.view", "info", false);
        viewInfoCheckBox.addChangeListener(listener);
        viewMenu.add(viewInfoCheckBox);

        viewTagCheckBox = uif.createCheckBoxMenuItem("ce.view", "tag", false);
        viewTagCheckBox.setAccelerator(KeyStroke.getKeyStroke("control T"));
        viewTagCheckBox.addChangeListener(listener);
        viewMenu.add(viewTagCheckBox);

        viewMenu.addSeparator();

        viewRefreshItem = uif.createMenuItem("ce.view", "refresh", listener);
        viewRefreshItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
        viewMenu.add(viewRefreshItem);

        menuBar.add(viewMenu);

        menuBar.add(uif.createHorizontalGlue("ce.pad"));

        // help menu
        JMenu helpMenu = uif.createMenu("ce.help");
        // config editor help
        JMenuItem mainItem = uif.createHelpMenuItem("ce.help.main", "confEdit.window.csh");
        helpMenu.add(mainItem);

/**
        // template editor help
        mainItem = uif.createHelpMenuItem("ce.help.maint", "confEdit.templateDialog.csh");
        helpMenu.add(mainItem);
 */
        JMenuItem fullItem = uif.createHelpMenuItem("ce.help.full", "confEdit.fullView.csh");
        helpMenu.add(fullItem);
        JMenuItem stdItem = uif.createHelpMenuItem("ce.help.std", "confEdit.stdView.csh");
        helpMenu.add(stdItem);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    protected void updateTitle() {
        File f = viewConfig.getFile();
        setI18NTitle("ce.title",
                    new Object[] { new Integer(currView == fullView ? 0 : 1),
                    new Integer(f == null ? 0 : 1), f });
    }

    private boolean isInfoVisible() {
        return (body instanceof JSplitPane);
    }

    private void setInfoVisible(boolean b) {
        // verify there is an infoPanel to be made visible
        if (infoPanel == null)
            throw new IllegalStateException();

        // check if already set as desired
        if (b == isInfoVisible())
            return;

        // get dimensions of views and info panel
        Dimension viewsSize = views.getSize();
        if (viewsSize.width == 0)
            viewsSize = views.getPreferredSize();

        Dimension infoSize = infoPanel.getSize();
        if (infoSize.width == 0)
            infoSize = infoPanel.getPreferredSize();


        if (b) {
            // set body to views+info; remove border, because JSplitPane adds in
            // its own padding
            views.setBorder(null);
            JSplitPane sp = uif.createSplitPane(JSplitPane.HORIZONTAL_SPLIT, views, infoPanel);
            sp.setDividerLocation(viewsSize.width + sp.getDividerSize());
            body = sp;
            showInfoForQuestion(viewConfig.getCurrentQuestion());
        }
        else {
            // set body to views; add a border to stand in for the padding
            // that JSplitPane would otherwise give
            views.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            body = views;
        }

        setBody(body);
        if (isShowing()) {
            // adjust the size of the window up or down as appropriate
            Dimension winSize = getSize();
            int divWidth = new JSplitPane().getDividerSize();
            int newWidth = winSize.width;
            newWidth += (b ? +1 : -1) * (infoSize.width + divWidth);
            setSize(newWidth, winSize.height);
        }
    }

    private void showInfoForQuestion(Question q) {
        try {
            ID id = Help.getHelpID(q);
            // uugh
            if (id == null)
                System.err.println("WARNING: no help for " + q.getKey());
            else
                infoPanel.setCurrentID(id);
        }
        catch (BadIDException e) {
            System.err.println("WARNING: no help for " + q.getKey());
        }
        catch (InvalidHelpSetContextException e) {
            JavaTestError.unexpectedException(e);
        }
    }


    private CE_View getDefaultView() {
        if (currView != null)
            return currView;
        Preferences p = Preferences.access();
        String prefView = p.getPreference(VIEW_PREF, CE_View.FULL);
        if (prefView.equals(CE_View.STD))
            return stdView;
        else
            return fullView;
    }

    protected void perform(String cmd) {
        if (cmd.equals(NEW))
            newConfigAsk();
        else if (cmd.equals(LOAD))
            loadConfigAsk();
/*
        else if (cmd.equals(LOADT))
            load(true);
        else if (cmd.equals(NEWT))
            clear(true);
 */
        else if (cmd.equals(SAVE))
            save();
        else if (cmd.equals(SAVE_AS))
            saveAs();
        else if (cmd.equals(REVERT))
            revert();
        else if (cmd.equals(CE_View.FULL))
            show(fullView);
        else if (cmd.equals(CE_View.STD))
            show(stdView);
        else if (cmd.equals(CLOSE)) {
            close();
        }
        else if (cmd.equals(DONE)) {
            if (currView != null && !currView.isOKToClose())
                return;

            if (!canInterruptTemplateCreation() && !viewConfig.isFinishable()) {
                uif.showError("ce.force_close");
                return;
            }

            currView.save();
            if (!viewConfig.isFinishable()) {
                Integer rp = new Integer(runPending ? 1 : 0);
                int rc = uif.showOKCancelDialog("ce.okToClose", rp);
                if (rc != JOptionPane.OK_OPTION)
                    return;
            }

            if (isEdited() || isCurrentQuestionChanged())
                saveRequired = true;

            if (saveRequired) {
                if (!save0()) {
                    // save failed, stay in CE, don't clear saveRequired flag
                    return;
                }

                // save succeeded, so safe to clear saveRequired flag
                saveRequired = false;
            }

            close(false);
        }
        else if (cmd.equals(REFRESH)) {
            if (currView != null)
                currView.refresh();
        }
        else if (cmd.equals(DETAILS)) {
            if (detailsBrowser == null) {
                detailsBrowser = new DetailsBrowser(body, viewConfig, infoPanel);
                detailsBrowser.setQuestionInfoEnabled(currView == fullView);
            }

            detailsBrowser.setVisible(true);
        }
        else
            throw new IllegalArgumentException(cmd);
    }

    private boolean canInterruptTemplateCreation () {
        /** fa
        ContextManager cm = getContextManager();
        String wdTmpl = TemplateUtilities.getTemplatePath(model.getWorkDirectory());
        if (mainConfig.isTemplate() &&
                !cm.getFeatureManager().isEnabled(FeatureManager.WD_WITHOUT_TEMPLATE) &&
                wdTmpl == null) {
            return false;
        }
         */
        return true;
    }

    public static void copy(InterviewParameters from, InterviewParameters to)
        throws Interview.Fault
    {
        copy(from, to, true); // copy filename as well, by default
    }

    private static void copy(InterviewParameters from, InterviewParameters to,
                      boolean copyFile)
        throws Interview.Fault
    {
        //System.err.println("CE.copy from " + (from==mainConfig?"main":from==viewConfig?"view":from.toString()) + " to " + (to==mainConfig?"main":to==viewConfig?"view":to.toString()));
        HashMap data = new HashMap();
        from.save(data);
        to.load(data, false);
        to.setTemplate(from.isTemplate());

        if (copyFile)
            to.setFile(from.getFile());
        if (debug) {
            Debug.println("InterviewEditor: equal(b,a) " + equal(to,from));
        }

    }

    /**
    * Checks default settings relate to config file load fron the default location
    * @param cm <code>ContextManager</code> object defining current harness' context. The following methods
    *           affect this method functionality:
    * <ul>
    * <li><code>getDefaultConfigLoadPath()</code>
    * <li><code>getAllowConfigLoadOutsideDefault()</code>
    * </ul>
    * @throws <code>IllegalArgumentException</code> if the following configuration errors found:
    * <ul>
    * <li> <code>getDefaultConfigLoadPath()</code> returns <code>null</code> when <code>getAllowConfigLoadOutsideDefault()</code> returns <code>false</code>
    * <li> <code>getDefaultConfigLoadPath()</code> returns not absolute path
    * <li> <code>getDefaultConfigLoadPath()</code> returns a file (not a directory)
    * </ul>
    * @see ContextManager#setDefaultConfigLoadPath(java.io.File)
    * @see ContextManager#setAllowConfigLoadOutsideDefault(boolean state)
    * @see ContextManager#getDefaultConfigLoadPath()
    * @see ContextManager#getAllowConfigLoadOutsideDefault()
    */

    public static File checkLoadConfigFileDefaults(ContextManager cm) {
        if (cm == null)
            return null;

        File defaultConfigLoadPath = cm.getDefaultConfigLoadPath();
        boolean allowConfigLoadOutsideDefault = cm.getAllowConfigLoadOutsideDefault();

        if (defaultConfigLoadPath == null && !allowConfigLoadOutsideDefault)
            throw new IllegalArgumentException("Default directory not specified for " +
                "load operation when allowConfigLoadOutsideDefault is false");

        if (defaultConfigLoadPath != null) {
            if (!defaultConfigLoadPath.isAbsolute())
                throw new IllegalArgumentException("Relative paths not " +
                    "currently supported. The following setting is incorrect: " +
                    "\"" + defaultConfigLoadPath.getPath() + "\" selected for " +
                    "load operation");

            if (defaultConfigLoadPath.isFile())
                throw new IllegalArgumentException("Filename selected unexpectedly " +
                    "as a default directory: " +
                    "\"" + defaultConfigLoadPath.getPath() + "\" for " +
                    "load operation");
        }

        return defaultConfigLoadPath;
    }

    static File loadConfigFile(ContextManager cm, Component parent, UIFactory uif, String ext, String key) {
        FileChooser fileChooser = new FileChooser(true);
        fileChooser.addChoosableExtension(ext, uif.getI18NString("ce.jtiFiles" + key));
        return loadConfigFile(cm, parent, uif, fileChooser);
    }

    /**
    * Provides capabilities for configuration file loading. Method takes into
    * account context settings relating to default locations for configuration
    * files loading and behaves according to them.
    * @param cm <code>ContextManager</code> object defining current harness' context. The following methods
    *           affect this method functionality:
    * <li><code>getDefaultConfigLoadPath()</code>
    * <li><code>getAllowConfigLoadOutsideDefault()</code>
    * </ul>
    * @param parent A parent frame to be used for <code>fileChooser</code>/warning dialogs
    * @param uif The UIFactory used to for configuration file loading operation
    * @param fileChooser The <code>FileChooser</code> used for configuration file loading
    * @return The configuration file selected by user if this file loading is allowed by
    *         harness' contest settings
    * @see ContextManager#setDefaultConfigLoadPath(java.io.File)
    * @see ContextManager#setAllowConfigLoadOutsideDefault(boolean state)
    * @see ContextManager#getDefaultConfigLoadPath()
    * @see ContextManager#getAllowConfigLoadOutsideDefault()
    */

    static File loadConfigFile(ContextManager cm, Component parent, UIFactory uif, FileChooser fileChooser) {

        if (cm == null)
            return null;

        File defaultConfigLoadPath = checkLoadConfigFileDefaults(cm);
        boolean allowConfigLoadOutsideDefault = cm.getAllowConfigLoadOutsideDefault();

        File file = null;

        fileChooser.setDialogTitle(uif.getI18NString("ce.load.title"));

        if (defaultConfigLoadPath != null) {
            if (!allowConfigLoadOutsideDefault) {
                if (!(new File(defaultConfigLoadPath.getAbsolutePath())).canRead()) {
                    uif.showError("ce.load.defDirNotExists", defaultConfigLoadPath);
                    return null;
                }
                fileChooser.enableDirectories(false);
            } else
                fileChooser.enableDirectories(true);
            fileChooser.setCurrentDirectory(defaultConfigLoadPath);
        }

        boolean isMatch = true;

        while (file == null) {
            int rc = fileChooser.showDialog(parent, uif.getI18NString("ce.load.btn"));
            if (rc != JFileChooser.APPROVE_OPTION)
                return null;

            file = fileChooser.getSelectedFile();

            if (!allowConfigLoadOutsideDefault) {
                if (defaultConfigLoadPath == null)
                    return null;

                File f = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(File.separator)));

                try {
                    isMatch = (f.getCanonicalPath().indexOf((defaultConfigLoadPath.getCanonicalPath())) == 0);
                } catch ( IOException ioe) {
                    ioe.printStackTrace(System.err);
                    return null;
                }

                if (!isMatch) {
                    uif.showError("ce.load.notAllowedDir", defaultConfigLoadPath);


                    file = null;
                    fileChooser.setCurrentDirectory(defaultConfigLoadPath);
                    continue;  // choose another file
                }
            }
        }

        if (file != null) {
            String path = file.getPath();
            String ext = fileChooser.getChosenExtension();
            if (ext == null) {
                ext = CONFIG_EXTENSION;
            }
            if (!path.endsWith(ext))
                file = new File(path + ext);
        }

        return file;
    }

    /**
    * Provides as the user with a dialog to chooser where to save a config. Method takes into account
    * context settings relating to default locations for configuration files saving and behaves
    * according to them.
    * @param cm <code>ContextManager</code> object defining current harness' context. The following methods
    *           affect this method functionality:
    * <ul>
    * <li><code>getDefaultConfigSavePath()</code>
    * <li><code>getAllowConfigSaveOutsideDefault()</code>
    * </ul>
    * @param parent A parent frame to be used for <code>fileChooser</code>/warning dialogs
    * @param uif The UIFactory used to for configuration file saving operation
    * @param fileChooser The <code>FileChooser</code> used for configuration file saving
    * @return The configuration file selected by user if this file saving is allowed by
    *         harness' contest settings
    * @throws <code>IllegalArgumentException</code> if the following configuration errors found:
    * <ul>
    * <li> <code>getDefaultConfigSavePath()</code> returns <code>null</code> when <code>getAllowConfigSaveOutsideDefault()</code> returns <code>false</code>
    * <li> <code>getDefaultConfigSavePath()</code> returns not absolute path
    * <li> <code>getDefaultConfigSavePath()</code> returns a file (not a directory)
    * </ul>
    * @see ContextManager#setDefaultConfigSavePath(java.io.File)
    * @see ContextManager#setAllowConfigSaveOutsideDefault(boolean state)
    * @see ContextManager#getDefaultConfigSavePath()
    * @see ContextManager#getAllowConfigSaveOutsideDefault()
    */

    static File saveConfigFile(ContextManager cm, Component parent, UIFactory uif, FileChooser fileChooser, File dir,
            boolean isTemplate) {
        if (cm == null)
            return null;

        File defaultSavePath;
        if (isTemplate) {
            defaultSavePath = cm.getDefaultTemplateSavePath();
        } else {
            defaultSavePath = cm.getDefaultConfigSavePath();
        }
        boolean allowSaveOutsideDefault;
        if (isTemplate) {
            allowSaveOutsideDefault = cm.getAllowTemplateSaveOutsideDefault();
        } else {
            allowSaveOutsideDefault = cm.getAllowConfigSaveOutsideDefault();
        }


        if (defaultSavePath == null && !allowSaveOutsideDefault)
            throw new IllegalArgumentException("Default directory not specified for " +
                "save operation when allowConfigSaveOutsideDefault is false");

        if (defaultSavePath != null) {
            if (!defaultSavePath.isAbsolute())
                throw new IllegalArgumentException("Relative paths not " +
                    "currently supported. The following setting is incorrect: " +
                    "\"" + defaultSavePath.getPath() + "\" selected for " +
                    "save operation");

            if (defaultSavePath.isFile())
                throw new IllegalArgumentException("Filename selected unexpectedly " +
                    "as a default directory: " +
                    "\"" + defaultSavePath.getPath() + "\" for " +
                    "save operation");

            if (!allowSaveOutsideDefault) {
                if (!defaultSavePath.canWrite()) {
                    uif.showError("ce.save.defDirNotExists", defaultSavePath);
                    return null;
                }
                fileChooser.enableDirectories(false);
            } else
                fileChooser.enableDirectories(true);

            fileChooser.setCurrentDirectory(defaultSavePath);
        } else
            if (dir != null)
                fileChooser.setCurrentDirectory(dir);

        File file = null;
        boolean isMatch = true;

        while (file == null) {
            int rc = fileChooser.showDialog(parent, uif.getI18NString("ce.save.btn"));
            if (rc != JFileChooser.APPROVE_OPTION)
                // user has canceled or closed the chooser
                return null;

            file = fileChooser.getSelectedFile();
            if (file == null) // just making sure
                continue;

            File f = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(File.separator)));

            if (!allowSaveOutsideDefault) {
                if (defaultSavePath == null)
                    return null;

                try {
                    isMatch = defaultSavePath.getCanonicalPath().equals(f.getCanonicalPath());
                } catch ( IOException ioe) {
                    ioe.printStackTrace(System.err);
                    return null;
                }

                if (!isMatch) {
                    uif.showError("ce.save.notAllowedDir", defaultSavePath);
                    file = null;
                    fileChooser.setCurrentDirectory(defaultSavePath);
                    continue;  // choose another file
                }
            }

            if (file.isDirectory()) {
                uif.showError("ce.save.fileIsDir", file);
                file = null;
                continue;  // choose another file
            }

            File parentFile = file.getParentFile();
            if (parentFile != null) {
                if (parentFile.exists() && !parentFile.isDirectory()) {
                    uif.showError("ce.save.parentNotADir", parentFile);
                    file = null;
                    continue;  // choose another file
                } else if (!parentFile.exists()) {
                    rc = uif.showYesNoDialog("ce.save.createParentDir",
                                             parentFile);
                    if (rc == JOptionPane.YES_OPTION) {
                        if (!parentFile.mkdirs()) {
                             uif.showError("ce.save.cantCreateParentDir",
                                           parentFile);
                             file = null;
                             continue;  // choose another file
                        }
                    } else {
                        file = null;
                        continue;  // choose another file
                    }
                }
            }

            // if file exists, leave well enough alone;
            // otherwise, make sure it ends with .jti or .jtm
            if (!file.exists()) {
                String path = file.getPath();
                String ext = fileChooser.getChosenExtension();
                if (ext != null && !path.endsWith(ext))
                    file = new File(path + ext);
            }

            // if file exists, make sure user wants to overwrite it
            if (file.exists()) {
                rc = uif.showYesNoDialog("ce.save.warn");
                switch (rc) {
                    case JOptionPane.YES_OPTION:
                        break;  // use this file

                    case JOptionPane.NO_OPTION:
                        fileChooser.setSelectedFile(null);
                        file = null;
                        continue;  // choose another file
                }
            }
        }
        return file;
    }

    void setAfterCloseCommand(Runnable runnable) {
        afterCloseCommand = runnable;
    }

    private Runnable afterCloseCommand;

    /**
     * Will be eliminated in the next release.
     * @deprecated
     */
    @Deprecated
    protected boolean templateMode = false;
    protected ContextManager contextManager = null;
    protected InterviewParameters mainConfig;
    protected InterviewParameters viewConfig;
    private FileHistory history;
    private boolean saveRequired;
    //protected String key;

    private boolean runPending;
    private static boolean debug = Debug.getBoolean(InterviewEditor.class);



    private JMenu recentConfigMenu;
    private JMenu markerMenu;
    private JMenu searchMenu;
    private JMenu viewMenu;
    private JRadioButtonMenuItem viewFullBtn;
    private JRadioButtonMenuItem viewStdBtn;
    private JCheckBoxMenuItem viewInfoCheckBox;
    private JCheckBoxMenuItem viewTagCheckBox;
    private JMenuItem viewRefreshItem;
    private JComponent body;
    private JPanel views;
    private JHelpContentViewer infoPanel;
    private CE_FullView fullView;
    private CE_StdView stdView;
    private CE_View currView;
    private Listener listener;
    //private TemplatesUI templatesUI;

    private Map customRenderersMap;
    private ActionListener closeListener;
    //private ExecModel model;
    private final List<Observer> observers = new ArrayList<Observer>();

    private DetailsBrowser detailsBrowser;
    private static final KeyStroke detailsKey = KeyStroke.getKeyStroke("shift alt D");
    protected String ext;

    // XXX this isn't the right class to define these in
    //     do not make more public than package private
    static final String CONFIG_EXTENSION = ".jti";
    static final String CONFIG_HISTORY = "configHistory.jtl";

    private static final String NEW = "new";
    private static final String LOAD = "load";
    //private static final String NEWT = "newt";
    //private static final String LOADT = "loadt";
    private static final String SAVE = "save";
    private static final String SAVE_AS = "saveAs";
    private static final String REVERT = "revert";
    private static final String DONE = "done";
    private static final String REFRESH = "refresh";
    private static final String DETAILS = "details";
            static final String CLOSE = "close";

    static final String MORE_INFO_PREF = "exec.config.moreInfo";
    static final String VIEW_PREF = "exec.config.view";


    public void setCustomRenderers(Map renderersMap) {
        customRenderersMap = renderersMap;
        if (fullView != null) {
            fullView.setCustomRenderers(customRenderersMap);
        }
    }

    private class Listener
        implements ActionListener, ChangeListener, MenuListener
    {
        // ---------- from ActionListener -----------

        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src instanceof JMenuItem) {
                JMenuItem mi = (JMenuItem) src;
                File f = (File) (mi.getClientProperty(FileHistory.FILE));
                if (f != null && askAndSave("ce.load.warn")) {
                    loadConfigFromFile(f);
                    return;
                }
            }
            perform(e.getActionCommand());
        }

        // ---------- from ChangeListener -----------

        public void stateChanged(ChangeEvent e) {
            Object src = e.getSource();
            if (src == viewInfoCheckBox && infoPanel != null)
                setInfoVisible(viewInfoCheckBox.isSelected());
            else if (src == viewTagCheckBox)
                fullView.setTagVisible(viewTagCheckBox.isSelected());
        }

        // ---------- from MenuListener -----------

        public void menuSelected(MenuEvent e) {
            Object src = e.getSource();
            if (src == viewMenu)
                viewTagCheckBox.setSelected(fullView.isTagVisible());
        }

        public void menuDeselected(MenuEvent e) {
        }

        public void menuCanceled(MenuEvent e) {
        }

    };

    /**
     * For private communication with SessionControl, not for broadcast outside
     * of core JT.
     */
    public interface Observer {
        /**
         * Invoked when value of interview parameters has been changed
         * @param p object with updated value (viewConfig)
         */
        public void changed(InterviewParameters p);

        /**
         * Invoked when setVisible() method is invoked on InterviewEditor object
         * @param isVisible argument passed to setVisible() method
         * @param source editor that changed the state
         */
        public void changedVisibility(boolean isVisible, InterviewEditor source);
    }
}
