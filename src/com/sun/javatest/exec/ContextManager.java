/*
 * $Id$
 *
 * Copyright (c) 2006, 2011, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.interview.Interview.Fault;
import com.sun.javatest.exec.Session.Event;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.awt.EventQueue;

import com.sun.javatest.InterviewParameters;
import com.sun.javatest.TestResultTable;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.report.CustomReport;
import com.sun.javatest.report.Report;
import com.sun.javatest.tool.ToolDialog;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.interview.Question;
import com.sun.interview.wizard.QuestionRenderer;
import com.sun.javatest.exec.template.ET_TemplateControlFactory;
import com.sun.javatest.exec.template.TemplateSession;

/**
 * The context manager provides an interface for customization of the user
 * interface and control over certain parameters of harness operation.  It only
 * needs to be customized if the test suite wishes to override default behaviors.
 * All methods will have an implementation, allowing for easy subclassing to
 * make only what change the test suite architect wishes.
 *
 * The test manager (exec tool) will create an instance of the test suite's context
 * manager near the beginning of initialization.  Throughout the lifecycle of
 * that tool instance, the context manager instance will be reused when
 * appropriate.  For proper operation the context manager must be changed if
 * the test suite changes, in which case the current manager will be disposed,
 * although this is not a typical case, since a new tool is normally created in
 * this circumstance (in current implementation).  After a test suite is loaded
 * into a exec tool instance, the associate context manager object will be reused.
 * The implementation of that manager can learn of changes in workdir association
 * by monitoring the <code>setWorkDirectory()</code> method (be sure to call the
 * superclass implementation if overriding.
 *
 * When the exec tool is itself disposed, the <code>dispose()</code> method of
 * the associated context manager object will be invoked.  If the exec tool were
 * to diassociate a context manager object from itself (not usually done),
 * <code>dispose()</code> would be invoked.
 */
public class ContextManager implements InterviewParameters.TemplateManager,
        Report.CustomReportManager, BasicSession.OrderedObserver {

    // GUI CUSTOMIZATION

    /**
     * Get the custom menu manager for this Test Manager instance.
     * @return The custom menu manager.  If null, it can be assumed that there
     *         are no custom menus.
     */
    public JavaTestMenuManager getMenuManager() {
        return null;
    }

    public ToolBarManager getToolBarManager() {
        return new ToolBarManager();
    }

    /**
     * Called when the associated ExecTool instance is being destroyed.
     * Great care should be taken in using references to objects, as they
     * maybe already be disposed - especially GUI components.  The exact order
     * in which the exec tool disposes it's associated objects is not currently
     * defined, although exec tool is primarily responsible for GUI objects, so
     * itself will not dispose of core objects (TestSuite objects,
     * work directories, etc).
     */
    public void dispose() {
    }

    /**
     * Get the context (popup) custom menus to be added in the GUI.  This
     * method is only called when the GUI is initialized, so the value should
     * not change after its first invocation.  Any state changes (enable,
     * disable, hide, text changes) with the menu items should occur inside the
     * JavaTestContextMenu instances, not be adding and removing them from the
     * array returned by this method.
     * @return The menus to be added.  Null if there are no custom menus (the
     *         default).
     * @see JavaTestContextMenu
     */
    public JavaTestContextMenu[] getContextMenus() {
        return null;
    }

    /**
     * Get custom report types.
     * @return Null if no custom types are requested.
     */
    public CustomReport[] getCustomReports() {
        return null;
    }

    /**
     * Get the context custom test result viewers to be added in the GUI.  This
     * method is only called when the GUI is initialized, so the value should
     * not change after its first invocation.
     * @return The menus to be added.  Null if there are no custom viewers (the
     *         default).
     * @see CustomTestResultViewer
     */
    public CustomTestResultViewer[] getCustomResultViewers() {
        return null;
    }


    /**
     * Get the active test suite.
     * @return The current test suite.
     */
    public TestSuite getTestSuite() {
        return testSuite;
    }


    /**
     * Get the active work directory.
     * @return The current work directory, null if it has not been set.
     */
    public WorkDirectory getWorkDirectory() {
        return workdir;
    }

    // INTERVIEW ACCESS
    /**
     * Get the permanent instance of the interview object used as a holder
     * for both the configuration and the template. This instance is filled
     * with the current values when the user finishes editing values and
     * commits changes. Depending on what has been committed later, it
     * will be filled either with the current configuration or the current
     * template values.
     * <br>
     * Note that there is currently no API support for
     * "locking" the interview, which means that multiple parts of the system
     * could work against each other.
     * @return The active interview instance.
     */
    public InterviewParameters getInterview() {
        //return interview;
        return getCurrentInterview();
    }

    /**
     * Get the active template.
     * @return null, if there is no interview or template in context yet;
     * current interview in case it actually represents template; new
     * instance of InterviewParameters, representing template, in case there
     * exist template, associated with interview
     * @throws java.io.IOException
     * @throws com.sun.interview.Interview.Fault
     */
    public InterviewParameters getTemplate() throws IOException, Fault {
        if (interview == null) {
            return null;
        }

        if (interview.isTemplate()) {
            return interview;
        } else {
            InterviewParameters template = null;
            if (interview.getTemplatePath() != null && workdir != null) {
                File f = new File(interview.getTemplatePath());
                template = InterviewParameters.open(f, workdir);
            }
            return template;
        }
    }

    /**
     * Returns an InterviewParameters instance filled with the current
     * interview values, or null if the interview is not loaded.
     */
    public InterviewParameters getCurrentInterview() {
        //return currentConfig == null ? null : currentConfig.getInterview();
        return currentConfig;
    }

    /**
     * Returns an InterviewParameters instance filled with the current
     * template values, or null if the template is not loaded.
     */
    public InterviewParameters getCurrentTemplate() {
        return currentTemplate;
        //return currentConfig == null ? null : currentConfig.getTemplate();
    }

    /**
     * Request that the harness reload the test suite structure from the
     * test suite.  If called on the GUI event thread, it will start a new
     * thread before executing the operation, to avoid blocking the GUI.
     * It is recommended that the caller use a different thread and probably
     * show the user a "Please wait" message until this method returns.
     */
    public void refreshTests() {

        synchronized (this) {
            if (pendingRefresh)
                return;

            pendingRefresh = true;
        }   // sync

        // start the task on a new thread if needed to release
        // the GUI
        if (EventQueue.isDispatchThread()) {
            Runnable cmd = new Runnable() {
                public void run() {
                    refreshTestsImpl();
                }   // run()
            };
            Thread t = new Thread(cmd, "ContextMgr Refresh Defer");
            t.start();
            return;
        }

        refreshTestsImpl();

    }

    /**
     * Method to be called, not on the event thread.
     * We have this method because we cannot guarantee which thread
     * <code>refreshTests()</code> will be called on.  The function
     * of this method is to shove the operation to the back of the
     * GUI event queue, then run it for real.
     */
     /*
    private void refreshTests0() {
        Runnable cmd = new Runnable() {
            public void run() {
                // now do the task for real on a background thread
                Runnable cmd2 = new Runnable() {
                    public void run() {
                        refreshTestsImpl();
                    }   // run()
                };
                Thread t = new Thread(cmd2, "ContextMgr Refresh");
                t.start();
                return;
            }   // run()
        };

        // this shoves the request to the end of the event queue
        // unfortunately necessary to allow any GUI work to complete
        // before executing the operation.
        // effectively, we would like to defer the operation until as
        // late as possible to avoid conflicts with the rest of the
        // system at startup, which would decrease performance
        EventQueue.invokeLater(cmd);
    }
    */


    private void refreshTestsImpl() {
        synchronized (this) {
            pendingRefresh = false;
        }

        if (parentTool != null) {
            TestResultTable trt = parentTool.getActiveTestResultTable();

            Map pathMap = new HashMap();
            parentTool.saveTreeState(pathMap);

            if (trt != null)
                try {
                    parentTool.pauseTreeCacheWork();
                    trt.getLock().lock();
                    trt.waitUntilReady();
                    // refresh entire tree


                    // use more minimal refresh - need to check functionality in
                    // batch mode
                    // XXX need to make access indirect, TRT_TreeNode should not be
                    //     public
                    //( (com.sun.javatest.TRT_TreeNode)(trt.getRoot()) ).refreshIfNeeded();
                    trt.refreshIfNeeded(trt.getRoot());
                }
                catch (TestResultTable.Fault f) {
                    // ignore?  log?
                }
                finally {
                    trt.getLock().unlock();
                    parentTool.unpauseTreeCacheWork();
                }   // finally
                parentTool.restoreTreeState(pathMap);
        }
    }

    /**
     * Write the active interview to disk if possible.
     * For this to work, getInterview() must be non-null.  This also implies that
     * there is a test suite and work directory selected already.
     * @throws IllegalStateException if there is no interview available.
     */
    public void syncInterview() {
        parentTool.syncInterview();
    }


    /**
     * Get feature manager from this ContextManager instance.
     * @return current feature manager
     */
    public FeatureManager getFeatureManager() {
        return featureManager;
    }

    /**
     * Set given feature manager for this ContextManager instance.
     * @param featureManager new feature manager
     */
    public void setFeatureManager(FeatureManager featureManager) {
        this.featureManager = featureManager;
    }

    // TEMPLATE SAVING BEHAVIOUR

    /**
     * This method is invoked each time before saving template.
     * The template will be saved only if this method returns true.
     * The default implementation always returns true.
     * @param file template file
     * @return true if this operation is allowed, false otherwise
     */
    public boolean canSaveTemplate(File file) {
        return true;
    }

    // WORK DIRECTORY OPTIONS
    /**
     * Default path presented to user when they are prompted to create
     * a work directory.  This method does not imply any requirement that
     * the user actually load/save the workdir in the given location.
     * @param dir The initial directory where workdirs should be loaded/saved
     *            to.
     * @throws NullPointerException if the parameter given is null.
     * @see #getDefaultWorkDirPath()
     */
    public void setDefaultWorkDirPath(File dir) {
        if (dir == null)
            throw new NullPointerException();
        this.wdPath = dir;
    }

    /**
     * Get the default path for work directory.
     * @return The initial directory to load and create work directories.
     * @see  #setDefaultWorkDirPath(File)
     */
    public File getDefaultWorkDirPath() {
        return this.wdPath;
    }

    // TEMPLATE OPTIONS
    /**
     * Set the default path from which template files are loaded.
     * Does not imply a requirement that the template be loaded from that
     * location.
     * @param dir The initial directory where template files should be
     *        loaded from.
     * @throws NullPointerException if the parameter given is null.
     * @see #getDefaultTemplateLoadPath
     * @see #setAllowTemplateLoadOutsideDefault
     */
    public void setDefaultTemplateLoadPath(File dir) {
        if (dir == null)
            throw new NullPointerException();
        templateLoadPath = dir;
    }

    /**
     * Get the default path from which template files are loaded.
     * @return The initial directory where template files should be
     *         loaded from.  Null if not set.
     * @see #setDefaultTemplateLoadPath
     * @see #setAllowTemplateLoadOutsideDefault
     */
    public File getDefaultTemplateLoadPath() {
        return templateLoadPath;
    }

    /**
     * Set the default path to which template files are saved.
     * Does not imply a requirement that the template must be saved to that
     * location.
     * @param dir The initial directory where template should be saved
     *            to.
     * @throws NullPointerException if the parameter given is null.
     * @see #getDefaultTemplateLoadPath
     * @see #setAllowTemplateLoadOutsideDefault
     */
    public void setDefaultTemplateSavePath(File dir) {
        if (dir == null)
            throw new NullPointerException();
        templateSavePath = dir;
    }

    /**
     * Get the default path to which template files are saved.
     * @return The initial directory where template files should be
     *         saved to.  Null if not set.
     * @see #setDefaultTemplateSavePath(File)
     * @see #setAllowTemplateSaveOutsideDefault(boolean)
     */
    public File getDefaultTemplateSavePath() {
        return templateSavePath;
    }

    /**
     * Set ability to load templates outside default directory.
     * @param state new state
     * @see #getAllowTemplateLoadOutsideDefault()
     */
    public void setAllowTemplateLoadOutsideDefault(boolean state) {
        templateLoadOutside = state;
    }

    /**
     * Get ability to load templates outside default directory
     * @return true if the loading outside default directory is allowed or false otherwise
     * @see #setAllowTemplateLoadOutsideDefault(boolean)
     */
    public boolean getAllowTemplateLoadOutsideDefault() {
        return templateLoadOutside;
    }

    /**
     * Set ability to save templates outside default directory.
     * @param state new state
     * @see #getAllowTemplateSaveOutsideDefault()
     */
    public void setAllowTemplateSaveOutsideDefault(boolean state) {
        templateSaveOutside = state;
    }

    /**
     * Get ability to save templates outside default directory
     * @return true if the saving outside default directory is allowed or false otherwise
     * @see #setAllowTemplateSaveOutsideDefault(boolean)
     */
    public boolean getAllowTemplateSaveOutsideDefault() {
        return templateSaveOutside;
    }

    // CONFIGURATION FILE OPTIONS
    /**
     * Set the default path from which configuration files are loaded.
     * Does not imply a requirement that the config be loaded from that
     * location.
     * @param dir The initial directory where configuration files should be
     *        loaded from.
     * @throws NullPointerException if the parameter given is null.
     * @see #getDefaultConfigLoadPath
     * @see #setAllowConfigLoadOutsideDefault
     */
    public void setDefaultConfigLoadPath(File dir) {
        if (dir == null)
            throw new NullPointerException();
        configLoadPath = dir;
    }

    /**
     * Get the default path from which configuration files are loaded.
     * @return The initial directory where configuration files should be
     *         loaded from.  Null if not set.
     * @see #setDefaultConfigLoadPath
     * @see #setAllowConfigLoadOutsideDefault
     */
    public File getDefaultConfigLoadPath() {
        return configLoadPath;
    }

    /**
     * Set the default path to which configuration files are saved.
     * Does not imply a requirement that the config must be saved to that
     * location.
     * @param dir The initial directory where workdirs should be saved
     *            to.
     * @throws NullPointerException if the parameter given is null.
     * @see #getDefaultConfigLoadPath
     * @see #setAllowConfigLoadOutsideDefault
     */
    public void setDefaultConfigSavePath(File dir) {
        if (dir == null)
            throw new NullPointerException();

        // verify that it exists?
        // verify that it is a dir?
        configSavePath = dir;
    }

    /**
     * Get the default path from which configuration files are loaded.
     * @return The initial directory where configuration files should be
     *         loaded from.  Null if not set.
     * @see #setDefaultConfigSavePath(File)
     * @see #setAllowConfigSaveOutsideDefault(boolean)
     */
    public File getDefaultConfigSavePath() {
        return configSavePath;
    }

    /**
     * Set ability to load config outside default directory.
     * @param state new state
     * @see #getAllowConfigLoadOutsideDefault()
     */
    public void setAllowConfigLoadOutsideDefault(boolean state) {
        configLoadOutside = state;
    }

    /**
     * Get ability to load config outside default directory
     * @return true if the loading outside default directory is allowed or false otherwise
     * @see #setAllowConfigLoadOutsideDefault(boolean)
     */
    public boolean getAllowConfigLoadOutsideDefault() {
        return configLoadOutside;
    }

    /**
     * Set ability to save config outside default directory.
     * @param state new state
     * @see #getAllowConfigSaveOutsideDefault()
     */
    public void setAllowConfigSaveOutsideDefault(boolean state) {
        configSaveOutside = state;
    }

    /**
     * Get ability to load config outside default directory
     * @return true if the saving outside default directory is allowed or false otherwise
     * @see #setAllowConfigLoadOutsideDefault(boolean)
     */
    public boolean getAllowConfigSaveOutsideDefault() {
        return configSaveOutside;
    }



    public void loadConfiguration(File file) {
        parentTool.loadInterview(file);
    }

    /**
     * @deprecated use #setWorkDirectory(WorkDirectory) instead
     * @see #setWorkDirectory(WorkDirectory)
     */
    protected void setWorkDir(WorkDirectory w) {
        setWorkDirectory(w);
    }

    protected void setWorkDirectory(WorkDirectory w) {
        workdir = w;
    }

    protected void openTree(WorkDirectory wd) {
    }

    protected void setTestSuite(TestSuite ts) {
        testSuite = ts;
    }

    public Map getCustomRenderersMap() {
        return customRenderers;
    }

    /**
     * Register custom config editor's question renderer for specified question class.
     * It is better to register custom renderer BEFORE Configuration Editor is constructed,
     * for example in ContextManager's constructor.
     * @param question Question's class
     * @param renderer Custom question renderer fot this question
     */
    protected void registerCustomQuestionRenderer(Class<? extends Question> question,
                                                  QuestionRenderer renderer) {
        if (customRenderers == null) {
            customRenderers = new HashMap();
        }
        customRenderers.put(question, renderer);
    }

    public ET_ControlFactory getExecToolControlFactory(ExecTool et, UIFactory uif) {
        if (Boolean.parseBoolean(System.getProperty(TEMPLATE_ON_PROPERTY))) {
            return new ET_TemplateControlFactory(et, uif, testSuite, this, et, et) ;
        } else {
            return new ET_DefaultControlFactory(et, uif, testSuite, this, et, et) ;
        }
    }

    /**
     * Invoked when session has been changed.
     * @param ev
     */
    public void updated(Event ev) {
        if (ev instanceof BasicSession.E_NewWD) {
            updatedWorkDirectory(((BasicSession.E_NewWD)ev).wd);
        } else if (ev instanceof BasicSession.E_NewConfig) {
            updatedCurrentConfig(((BasicSession.E_NewConfig)ev).ip);
        } else if (ev instanceof TemplateSession.E_NewTemplate) {
            updatedCurrentTemplate(((TemplateSession.E_NewTemplate)ev).templ);
        }
    }
    /**
     * BasicSession.OrderedObserver interface method. Returns
     * Integer.MAX_VALUE - 100 to be notified after controls, but before
     * ExecTool.
     */
    public int order() {
        return Integer.MAX_VALUE - 100;
    }

    /**
     * Invoked when the value of the work directory has been modified.
     * This implementation just invokes setWorkDirectory().
     * Subclasses might implement alternative reaction on configuration change.
     * @param wd
     */
    protected void updatedWorkDirectory(WorkDirectory wd) {
        setWorkDirectory(wd);
    }

    /**
     * Invoked when the value of the current configuration has been modified.
     * This implementation does nothing. Subclasses might implement
     * some reaction on configuration change.
     *
     * @param ip - InterviewParameters object with new values
     */
    protected void updatedCurrentConfig(InterviewParameters ip) {
    }

    /**
     * Invoked when the value of the current template has been modified.
     * This implementation does nothing. Subclasses might implement
     * some reaction on configuration change.
     *
     * @param ip - InterviewParameters object with new values
     */
    protected void updatedCurrentTemplate(InterviewParameters ip) {
    }



    /**
     * Special class for creating dialogs which should be attached to the
     * context of this test manager.  It is important to use this class
     * to assure proper component parenting, and because the system will
     * properly track your dialogs if the user changes the desktop style
     * (switching to internal frames interface for example).
     */
    public abstract static class TestManagerDialog extends ToolDialog {
        /**
         * @param context The context object associated with this dialog,
         *        this parameter is required so that the proper parenting
         *        can be calculated.
         * @param uif The interface factory associated with this dialog.
         * @param key Resource key to be used when retrieving items from the
         *        uif.
         */
        public TestManagerDialog(ContextManager context, UIFactory uif,
                                 String key) {
            super(context.parentTool, uif, key);
        }
    }

    // ------------------------ PRIVATE -----------------------------

    void setInterview(InterviewParameters i) {
        // As interview managed by ExecTool, it should be disposed there.
//        interview.dispose();
        interview = i;
    }
    void setCurrentConfig(SessionExt conf) {
        setWorkDirectory(conf.getWorkDirectory());
        currentConfig = conf.getInterviewParameters();
        if (conf instanceof TemplateSession) {
            currentTemplate = ((TemplateSession)conf).getTemplate();
        }
    }


    void setTool(ExecTool t) {
        parentTool = t;
    }


    protected File configLoadPath;
    protected File configSavePath;

    protected boolean configLoadOutside = true;
    protected boolean configSaveOutside = true;

    protected File templateLoadPath;
    protected File templateSavePath;

    protected boolean templateLoadOutside = true;
    protected boolean templateSaveOutside = true;

    protected File wdPath;


    protected FeatureManager featureManager = new FeatureManager();

    private WorkDirectory workdir;
    private TestSuite testSuite;
    private InterviewParameters interview;

    // holder for a copy of the current interview and template values
    //private Session currentConfig;
    protected InterviewParameters currentConfig = null;
    protected InterviewParameters currentTemplate = null;
    private ExecTool parentTool;
    private Map customRenderers;

    private volatile boolean pendingRefresh = false;

    /**
     * This is the name of system property to turn template support on.
     * To enable templates you should specify "true" value for this property
     * or override getExecToolControlFactory() method.
     */
    static final String TEMPLATE_ON_PROPERTY = "com.sun.javatest.exec.templateMode";

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(ContextManager.class);

}
