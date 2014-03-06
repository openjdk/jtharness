/*
 * $Id$
 *
 * Copyright (c) 2010, 2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.javatest.exec.template;

import com.sun.javatest.InterviewParameters;
import com.sun.javatest.TemplateUtilities;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.exec.BasicSession;
import com.sun.javatest.exec.InterviewEditor;
import com.sun.javatest.tool.FileHistory;
import com.sun.javatest.util.I18NResourceBundle;
import java.io.File;
import java.util.Map;

/**
 * Extension of the BasicConfig with template specific features.
 */
public class TemplateSession extends BasicSession {

    /**
     * Instance of the template
     */
    protected final InterviewParameters templ;

    static final String TEMPLATE_PROP_NAME = "Template";

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(TemplateSession.class);

    /**
     * Creates a TemplateSession instance for the passed testSuite object.
     * Initializes template instance.
     * @param ts
     * @throws com.sun.javatest.exec.Config.Fault
     */
    public TemplateSession(TestSuite ts) throws Fault {
        super(ts);
        try {
            templ = ts.createInterview();
            templ.setTemplate(true);
        } catch (TestSuite.Fault f) {
            throw new Fault(f);
        }
    }

    /**
     * Extends parent's property list with the template name property.
     */
    @Override
    protected void initPropertyList() {
        super.initPropertyList();
        props.add(TEMPLATE_PROP_NAME);
    }

    @Override
    public String getValue(String name) {
        if (TEMPLATE_PROP_NAME.equals(name)) {
            if (templ == null) {
                return null;
            }
            File f = templ.getFile();
            return f == null ?  null : f.getPath();
        } else {
            return super.getValue(name);
        }
    }


    @Override
    public void dispose() {
        templ.dispose();
        super.dispose();
    }

    @Override
    public void save(Map map) {
        if (templ.getFile() != null) {
            map.put("template", templ.getFile().getPath());
        }
        super.save(map);
    }


    @Override
    public void restore(Map map) throws Fault {
        super.restore(map);
        restoreTemplate(map);
    }

    protected void restoreTemplate(Map map) {
        String templPath = (String) map.get("template");
        if (templPath != null) {
            try {
                if (!new File(templPath).exists() && getWorkDirectory() != null) {
                    // attempt to find relocated template
                    String oldWDpath = getWorkDirectory().getPrevWDPath();
                    String[] begins = WorkDirectory.getDiffInPaths(templPath, oldWDpath);
                    if (begins != null && templPath.startsWith(begins[1])) {
                        templPath = begins[0] + templPath.substring(begins[1].length());
                    }
                }
                templ.load(new File(templPath));
                if (getWorkDirectory() != null) {
                    FileHistory.getFileHistory(getWorkDirectory(), TemplateEditor.TEMPLATE_HISTORY).add(templ.getFile());
                }
                templ.setTemplate(true);
                notifyObservers(new E_NewTemplate(templ));
            } catch (Exception ex) {
                System.err.println(i18n.getString("tcc.cantRestoreTemplate.err", templPath));
            }
        }
    }


    @Override
    public void update(Update u) throws Fault {
        if (u instanceof U_NewTemplate) {
            updateTemplate(((U_NewTemplate)u).templ);
        } else {
            super.update(u);
        }
    }

    @Override
    protected void updateNewConfig(InterviewParameters ip) throws Fault {

        String oldPath = templ.getTemplatePath();
        String newPath = ip.getTemplatePath();
        if (oldPath != newPath && oldPath != null && !oldPath.equals(newPath)) {
            try {
                File templateFile = new File(newPath);
                templ.load(templateFile);
                templ.setFile(templateFile);
                TemplateUtilities.setTemplateFile(getWorkDirectory(), templateFile, true);
                notifyObservers(new E_NewTemplate(templ));
                super.updateNewConfig(ip);
            } catch (Exception ex) {
                throw new Fault(ex);
            }
        }
        else {
            super.updateNewConfig(ip);
        }
    }


    @Override
    protected void applyWorkDir(WorkDirectory wd) {
        super.applyWorkDir(wd);
        if (templ != null) {
            templ.setWorkDirectory(wd);
        }
    }

    /**
     * Loads template for the newly set workdir, notifies observers
     * with U_NewTemplate.
     * @param wd - work dir that just set
     * @throws com.sun.javatest.exec.Config.Fault
     */
    protected void loadTemplateFromWD(WorkDirectory wd) throws Fault {
        File templateFile = TemplateUtilities.getTemplateFile(wd);
        if (templateFile != null) {
            try {
                //wd.getTestSuite().loadInterviewFromTemplate(templateFile,
                //        templ);
                InterviewParameters newValue = InterviewParameters.open(templateFile, wd);
                InterviewEditor.copy(newValue, templ);
                templ.setFile(templateFile);
                TemplateUtilities.setTemplateFile(wd, templateFile, true);
                notifyObservers(new E_NewTemplate(templ));
            } catch (Exception ex) {
                throw new Fault(ex);
            }
        }
    }

    /**
     * @deprecated use getInterviewParameters()
     * @return getInterviewParameters()
     */
    @Deprecated
    public InterviewParameters getConfig() {
        return getInterviewParameters();
    }

    /**
     * Provides an access to the templ. We had to add this method to
     * preserve previous functionality...
     * @return
     */
    public InterviewParameters getTemplate() {
        return templ;
    }

    /**
     * Method invoked as a reaction on U_NewTemplate.
     * Checks if there are any changes in the update, if none - does nothing,
     * Otherwise, copies new values into the main template instance, notifies
     * observers with E_NewTemplate event.
     *
     * @param t - object with new template values.
     * @throws com.sun.javatest.exec.Config.Fault
     */
    protected void updateTemplate(InterviewParameters t) throws Fault {
        if (InterviewEditor.equal(t, templ) &&
                t.getFile() != null && t.getFile().equals(templ.getFile())) {
            return; // nothing to update
        }
        try {
            InterviewEditor.copy(t, templ);
            TemplateUtilities.setTemplateFile(getWorkDirectory(), templ.getFile(), true);
        } catch (Exception e) {
            throw new Fault(e);
        }
        if (!templ.getTemplatePath().equals(getInterviewParameters().getTemplatePath())) {
            getInterviewParameters().clear();
            notifyObservers(new E_NewConfig(getInterviewParameters()));
        }
        notifyObservers(new E_NewTemplate(this.templ));
    }

    /**
     * Event signaling of a change happened to the template.
     */
    public static class E_NewTemplate implements Event {
        /**
         * InterviewParameters object filled with new values.
         */
        public final InterviewParameters templ;
        public E_NewTemplate(InterviewParameters templ) {
            this.templ = templ;
        }
    }

    /**
     * Update object to be applied when template changed.
     */
    public static class U_NewTemplate implements Update {
        /**
         * InterviewParameters object filled with new values.
         */
        public final InterviewParameters templ;
        public U_NewTemplate(InterviewParameters templ) {
            this.templ = templ;
        }
    }
}
