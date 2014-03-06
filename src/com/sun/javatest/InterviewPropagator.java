/*
 * $Id$
 *
 * Copyright (c) 2006, 2013, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest;

import com.sun.interview.Interview;
import com.sun.interview.Properties2;
import com.sun.interview.PropertiesQuestion;
import com.sun.interview.Question;
import com.sun.javatest.report.HTMLWriterEx;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.tool.CustomPropagationController.EventType;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class InterviewPropagator {

    InterviewPropagator(InterviewParameters par, String[] igProps, String[] igPrefs) {
        interview = par;
        ignorableProps = igProps;
        ignorablePrefs = igPrefs;
    }

    boolean checkForUpdate() {

        boolean wasUpdated = false;

        if (view == null)
            return false;

        if (isFromTemplate() && interview.getFile() != null) {

            interview.getPropagationController().setRefresher(refresher);
            interview.getPropagationController().setInterview(interview);
            fireEvent(EventType.Start, null);

            InputStream in = null;
            PrintStream psConflicts = null;
            PrintStream psUpdates = null;
            boolean needToSave1=false, needToSave2=false;

            Properties templateData = new Properties();

            try {
                File template = new File(interview.getTemplatePath());
                in = new BufferedInputStream(new FileInputStream(template));
                templateData.load(in);

                fireEvent(EventType.TemplateLoaded, templateData);
            } catch (FileNotFoundException ex) {
                notifyError(i18n.getString("tmpltProp.tmpFileError", new String[] {interview.getTemplatePath()}));
                logException(ex);
            } catch (IOException ex) {
                notifyError(i18n.getString("tmpltProp.tmpFileError", new String[] {interview.getTemplatePath()}));
                logException(ex);
            }

            try {

                pm = new PropogateMap();

                HashMap actual = new HashMap();
                interview.save(actual);

                // process custom changes such as property rename
                // interview.getPropagationController() should not be null
                needToSave1 = interview.getPropagationController().preprocessData(templateData, interview);

                // just store template values for !isUpdatableKey() keys
                needToSave2 = processNotUpdatableKeys(templateData, interview);

                // don't optimize this expression! both of methods must be invoked
                if(needToSave1 || needToSave2) {
                    // save to file!
                    saveInterview();
                }

                Map allQuestionMap = interview.getAllQuestions();
                // autoupdate partial questions
                if (processPartialQuestions(templateData, allQuestionMap, actual)) {
                    // update it
                    interview.save(actual);
                }

                // check for changes, build the conflictMap
                // 1) scan new template
                processQuestionFromSet(templateData, allQuestionMap, templateData.keySet(), actual);

                // 2) scan old template
                processQuestionFromSet(templateData, allQuestionMap, interview.retrieveTemplateKeys(), actual);

                // 3) scan configuration only values
                processQuestionFromSet(templateData, allQuestionMap, actual.keySet(), actual);

                if (pm.hasConflicts() || pm.hasUpdates()) {
                    psConflicts = new PrintStream(new FileOutputStream(pm.getConflictReportFile()));
                    psUpdates = new PrintStream(new FileOutputStream(pm.getUpdatesReportFile()));
                    pm.makeConflictsReport(psConflicts);
                    pm.makeUpdatesReport(psUpdates);
                    updateAll();
                    if (view != null) {
                        view.showView(this, interview);
                        wasUpdated = true;
                    }
                }
            } catch (IOException ex) {
                logException(ex);
            } finally {
                try {
                    if (psConflicts != null) psConflicts.close();
                    if (psUpdates != null) psUpdates.close();
                    if (in != null) in.close();     // this one can throw
                } catch (IOException ex) {
                    logException(ex);
                }
            }
            if(needToSave1 || needToSave2 || pm.hasConflicts() || pm.hasUpdates())
                fireEvent(EventType.Finish, null);
        }
        cleanup();
        return wasUpdated;
    }

    private void fireEvent(EventType eventType, Properties templateData) {
        interview.getPropagationController().notify(eventType, interview, templateData);
    }

    private boolean processNotUpdatableKeys(Properties templateData, InterviewParameters interview) {
        boolean wasUpdate = false;
        for (Object key : templateData.keySet()) {
            String templateKey = (String) key;
            if(!isSystemIgnorableTemplateProperty(templateKey) && !this.interview.isUpdatableKey(templateKey)) {
                String newTV = templateData.getProperty(templateKey);
                String oldTV = interview.retrieveTemplateProperty(templateKey);
                if (oldTV != null &&
                        !oldTV.equals(newTV)) {
                    wasUpdate = true;
                    interview.storeTemplateProperty(templateKey,  newTV);
                }
            }
        }
        return wasUpdate;
    }

    private boolean processPartialQuestions(Properties templateData, Map allQuestionMap, Map actual) throws IOException {
        Iterator keys = templateData.keySet().iterator();
        boolean updated = false;
        while (keys.hasNext()) {
            String questionKey = (String)keys.next();
            if (!interview.isUpdatableKey(questionKey)) {
                continue;
            }
            if (isPropertyQuestion(questionKey, allQuestionMap)) {
                // create template map
                String templateValue = templateData.getProperty(questionKey);
                Properties2 templateProps = InterviewPropagator.stringToProperties2(templateValue);
                // create actual map
                String actualValue = (String) actual.get(questionKey);
                Properties2 actualProps = InterviewPropagator.stringToProperties2(actualValue);
                String oldTVal = interview.retrieveTemplateProperty(questionKey);
                Properties2 oldTemplateProps = null;
                if (oldTVal != null) {
                    oldTemplateProps = InterviewPropagator.stringToProperties2(oldTVal);
                }


                Iterator itt = templateProps.keySet().iterator();
                boolean currentQuestionUpdated = false;
                Properties2 oldValuesMap = new Properties2();
                while (itt.hasNext()) {
                    String subKey = (String)itt.next();
                    if (interview.isAutoUpdatableKey(questionKey, subKey) ||
                            !actualProps.containsKey(subKey)) {
                        String templateSubValue = templateProps.getProperty(subKey);
                        String actualSubValue = actualProps.getProperty(subKey);
                        if (actualSubValue == null || !templateSubValue.equals(actualSubValue)) {
                            actualSubValue = actualSubValue == null ? notAvailable : actualSubValue;
                            oldValuesMap.put(subKey, actualSubValue);
                            currentQuestionUpdated = true;
                            actualProps.put(subKey, templateSubValue);
                            if (oldTemplateProps != null) {
                                oldTemplateProps.put(subKey, templateSubValue); // update "old template subproperty"
                            }
                        }
                    }
                }
                if (currentQuestionUpdated) {
                    StringWriter sw = new StringWriter();
                    actualProps.save(sw, null);

                    StringWriter swOld = new StringWriter();
                    oldValuesMap.save(swOld, null);

                    if (oldTemplateProps != null) {
                        StringWriter swOldT = new StringWriter();
                        oldTemplateProps.save(swOldT, null);
                        interview.storeTemplateProperty(questionKey, swOldT.toString());
                    } else {
                        interview.storeTemplateProperty(questionKey, sw.toString());
                    }

                    PropertiesQuestion pq = (PropertiesQuestion) allQuestionMap.get(questionKey);
                    pq.setValue(sw.toString());
                    interview.setEdited(true);
                    updated = true;
                    Object [] data = new Object[] {sw.toString(), swOld.toString(), swOld.toString(),
                    getQuestionText(questionKey, allQuestionMap)};
                    pm.partialUpdateMap.put(questionKey, data);
                }
            }
        }
        return updated;
    }

    private void processQuestionFromSet(Properties templateData, Map allQ, Set keySet, Map actual) {
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String questionKey = (String)keys.next();
            if (! isIgnorableTemplateProperty(questionKey)) {
                Object templateV = templateData.getProperty(questionKey);
                Object oldTemplateV = interview.retrieveTemplateProperty(questionKey);
                Object configurationV = actual.get(questionKey);
                pm.add(questionKey, templateV, oldTemplateV, configurationV,
                        getQuestionText(questionKey, allQ),
                        isPropertyQuestion(questionKey, allQ));
            }
        }
    }


    private boolean isIgnorableTemplateProperty(String propertyName) {
        return isSystemIgnorableTemplateProperty(propertyName) || !interview.isUpdatableKey(propertyName);
    }

    private boolean isSystemIgnorableTemplateProperty(String propertyName) {

        if (propertyName == null)
            return true;

        for (String ignorableProp : ignorableProps) {
            if (propertyName.equals(ignorableProp)) {
                return true;
            }
        }
        for (String ignorablePref : ignorablePrefs) {
            if (propertyName.startsWith(ignorablePref)) {
                return true;
            }
        }

        return false;
    }


    private boolean isFromTemplate() {
        return ! interview.isTemplate() && interview.getTemplatePath() != null;
    }

    private String getQuestionText(String key, Map<String, Question> allQ) {
        Question q = allQ.get(key);
        String result = key;
        if (q != null) {
            result = q.getText();
        }
        result = interview.getPropagationController().getQuestionText(key, result);
        return result;
    }


    /**
     * Gets current PropogateMap
     * @return InterviewPropagator.PropogateMap with actual data
     */
    public InterviewPropagator.PropogateMap getPropagateMap() {
        return pm;
    }


    /**
     * Accepts all changes from template to the current configuration
     */
    public void acceptAll() {
        InterviewPropagator.PropogateMap pm = getPropagateMap();
        if (pm.hasConflicts()) {
            Map map = pm.conflictMap;
            acceptTemplateDatafromMap(map);
            try {
                interview.save();
            } catch (IOException ex) {
                logException(ex);
            } catch (Interview.Fault ex) {
                logException(ex);
            }
        }
    }

    /**
     * Rejects all changes and store in the current configuration new template values as current
     */
    public void rejectAll() {
        InterviewPropagator.PropogateMap pm = getPropagateMap();
        if (pm.hasConflicts()) {
            Map map = pm.conflictMap;
            acceptTemplateDatafromMap(map, true);
            try {
                interview.save();
            } catch (IOException ex) {
                logException(ex);
            } catch (Interview.Fault ex) {
                logException(ex);
            }
        }
    }

    // autoupdatable values
    private void updateAll()  {
        InterviewPropagator.PropogateMap pm = getPropagateMap();
        if (pm.hasUpdates()) {
            Map map = pm.updateMap;
            acceptTemplateDatafromMap(map);
            saveInterview();
        }
    }

    private void saveInterview() {
        interview.setEdited(true);
        try {
            interview.save();
        } catch (Interview.Fault ex) {
            logException(ex);
        } catch (IOException ex) {
            logException(ex);
        }
    }

    private void acceptTemplateDatafromMap(final Map map) {
        acceptTemplateDatafromMap(map, false);
    }


    private void acceptTemplateDatafromMap(final Map map, boolean  templateOnly) {
        Iterator it = map.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object[] vals = (Object[]) map.get(key);
            interview.storeTemplateProperty(key, vals[NEW_TEMPLATE].toString());
        }
        if (!templateOnly) {
            Map actual = new HashMap();
            interview.save(actual);
            it = map.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                Object[] vals = (Object[]) map.get(key);
                actual.put(key, vals[NEW_TEMPLATE]);
            }
            try {
                interview.load(actual, false);
            } catch (Interview.Fault ex) {
                // TODO - warn readable message
                logException(ex);
            }
            interview.setEdited(true);
        }
    }

    private void cleanup() {
        if (pm != null) {
            pm.cleanup();
        }
        interview.getPropagationController().setRefresher(null);
        interview.getPropagationController().setInterview(null);

    }

    private void logException(Throwable th) {
        if (view != null) {
            view.logException(th, interview);
        } else {
            th.printStackTrace();
        }
    }

    private void notifyError(String message) {
        if (view != null) {
            view.notifyError(message, interview);
        }
    }

    //
    // utility functions (offered as external API)
    //

    /**
     * Converts string representation of property question to Properties2 object
     * @param str - string representation of property question
     * @return corresponding Properties2 object
     * @throws IOException
     */
    public static Properties2 stringToProperties2(String str) throws IOException {
        Properties2 result = new Properties2();
        if (str != null) {
            result.load(new StringReader(str));
        }
        return result;
    }

    /**
     * Properties2 object to its string representation.
     * Used for property question processing
     * @param pr - Properties2 object
     * @return corresponding string representation
     */
    public static String properties2ToString(Properties2 pr)  {
        StringWriter sw = new StringWriter();
        pr.save(sw, null);
        return sw.toString();
    }

    /**
     * Returns is the specified question is properties question
     * @param key - question key
     * @param interview - InterviewParameters object
     * @return true if the specified question is properties question, otherwise false
     */
    public static boolean isPropertyQuestion(String key, InterviewParameters interview) {
        return isPropertyQuestion(key, interview.getAllQuestions());
    }

    /**
     * Returns is the specified question is properties question
     * @param key - question key
     * @param allQ - question map
     * @return true if the specified question is properties question, otherwise false
     */
    public static boolean isPropertyQuestion(String key, Map<String, Question> allQ) {
        Question q = allQ.get(key);
        if (q != null) {
            return q instanceof PropertiesQuestion;
        }
        return false;
    }



    /**
     * Propagation data model.
     */
    public class PropogateMap {

        void add(String key, Object templV, Object oldTemplV, Object confV, String questionText, boolean isPropQ) {

            if (isPropQ) {
                propQs.add(key);
            }

            if (interview.isAutoUpdatableKey(key, null)) {
                add(updateMap, key, templV, oldTemplV, confV, questionText, isPropQ, updateMap);
            } else {
                add(conflictMap, key, templV, oldTemplV, confV, questionText, isPropQ, updateMap);
            }
        }


        private void add(Map aMap, String key, Object templV, Object oldTemplV, Object confV, String questionText, boolean isPropQ, Map updateMap) {


            if (templV != null ) {
                Object [] data = new Object[] {templV, oldTemplV, confV, questionText};
                Object [] comp_data = data;
                boolean added = false;
                boolean isUpdate = false;
                if (isPropQ) {
                    comp_data = convertPQ(data);
                }

                //if (comp_data[OLD_TEMPLATE] == null) comp_data[OLD_TEMPLATE] = notAvailable;

                // is it "new template value" ?
                if (comp_data[OLD_TEMPLATE] == null && !comp_data[NEW_TEMPLATE].equals(comp_data[CONFIGURATION])) {
                    comp_data[OLD_TEMPLATE] = notAvailable ;
                    if (comp_data[CONFIGURATION] == null) comp_data[CONFIGURATION] = notAvailable;
                    updateMap.put(key, data );
                    isUpdate = added = true;

                // is it "simple template update with no conflicts" ?
                } else if (comp_data[OLD_TEMPLATE] != null && comp_data[CONFIGURATION] != null) {
                    if (!comp_data[NEW_TEMPLATE].equals(comp_data[CONFIGURATION]) &&
                            comp_data[OLD_TEMPLATE].equals(comp_data[CONFIGURATION])) {
                        updateMap.put(key, data );
                        isUpdate = added = true;
                    } else if (!comp_data[NEW_TEMPLATE].equals(comp_data[CONFIGURATION]) &&
                            !comp_data[NEW_TEMPLATE].equals(comp_data[OLD_TEMPLATE])) {
                        // cases 1 and 3, notify and ask user
                        aMap.put(key,  data);
                        added = true;
                    } else if (!comp_data[NEW_TEMPLATE].equals(comp_data[CONFIGURATION]) &&
                            comp_data[NEW_TEMPLATE].equals(comp_data[OLD_TEMPLATE])) {
                        // do nothing
                        //
                    } else if (!comp_data[NEW_TEMPLATE].equals(comp_data[OLD_TEMPLATE])) {
                        // case 2 notify and show user
                        aMap.put(key, data );
                        added = true;
                    }
                }
                if (debug && added) {
                    System.out.println("= ADDED ==========================================================");
                    if (isUpdate) System.out.println("UPDATE !!!");
                    System.out.println("Key      " + key);
                    System.out.println("Value    " + confV);
                    System.out.println("Template " + templV);
                    System.out.println("Old Temp " + oldTemplV);

                }

            }

        }

        /**
         * Does the data contain conflicts?
         */
        public boolean hasConflicts() {
            return conflictMap.size() > 0;
        }

        /**
         * Does the data contain auto updates?
         */
        public boolean hasUpdates() {
            return updateMap.size() > 0 || partialUpdateMap.size() > 0;
        }

        void makeConflictsReport(PrintStream sw) {
            makeReport(sw, conflictMap, false);
        }

        void makeUpdatesReport(PrintStream sw) {
            LinkedHashMap allUpdate = new LinkedHashMap(updateMap);
            allUpdate.putAll(partialUpdateMap);
            makeReport(sw, allUpdate, true);
        }

        private void makeReport(PrintStream sw, Map m, boolean hideOldTemplate) {
            Iterator it = m.keySet().iterator();
            if (! it.hasNext()) {
                return;
            }
            try {
                HTMLWriterEx writer = new HTMLWriterEx(new PrintWriter(sw), i18n);
                writer.startTag(HTMLWriterEx.HTML);
                writer.startTag(HTMLWriterEx.HEAD);
                writer.writeContentMeta();
                writer.writeEntity(getCSS());
                writer.endTag(HTMLWriterEx.HEAD);
                writer.startTag(HTMLWriterEx.BODY);
                writer.startTag(HTMLWriterEx.TABLE);
                writer.writeAttr("border", 0);
                writer.startTag(HTMLWriterEx.TR);
                writer.startTag(HTMLWriterEx.TD);
                writer.writeAttr("colspan", 3);
                writer.writeAttr("class", "head");
                if (hideOldTemplate)
                    writer.writeI18N("templProp.updateText");
                else
                    writer.writeI18N("templProp.conflictText");

                writer.endTag(HTMLWriterEx.TD);
                writer.endTag(HTMLWriterEx.TR);

                writer.startTag(HTMLWriterEx.TR);

                writer.startTag(HTMLWriterEx.TD);
                writer.writeAttr("class", "head2");
                writer.writeI18N("tmpltProp.Configuration");
                writer.endTag(HTMLWriterEx.TD);

                if (!hideOldTemplate) {
                    writer.startTag(HTMLWriterEx.TD);
                    writer.writeAttr("class", "head2");
                    writer.writeI18N("tmpltProp.oldTmplt");
                    writer.endTag(HTMLWriterEx.TD);
                }

                writer.startTag(HTMLWriterEx.TD);
                writer.writeAttr("class", "head2");
                writer.writeI18N("tmpltProp.newTmplt");
                writer.endTag(HTMLWriterEx.TD);

                writer.endTag(HTMLWriterEx.TR);

                while (it.hasNext()) {
                    String key = (String) it.next();
                    Object [] data = (Object []) m.get(key);
                    writer.startTag(HTMLWriterEx.TR);
                    writer.startTag(HTMLWriterEx.TD);
                    writer.writeAttr("class", "pname");
                    writer.writeAttr("colspan", hideOldTemplate? 2:3);
                    writer.startTag(HTMLWriterEx.HR);
                    writer.write((String)data[QUESTION_TEXT]);
                    writer.endTag(HTMLWriterEx.TD);
                    writer.endTag(HTMLWriterEx.TR);

                    String[] s;
                    if(!propQs.contains(key)) {
                        s = new String[data.length];
                        for(int i = 0; i < data.length; i++)
                            s[i] = (String)data[i];
                    } else
                        s = convertPQ(data);

                    writer.startTag(HTMLWriterEx.TR);

                    for(int i = CONFIGURATION; i >= NEW_TEMPLATE; i--) {

                        if (i != OLD_TEMPLATE || ! hideOldTemplate) {
                            writer.startTag(HTMLWriterEx.TD);
                            writer.writeAttr("class", "val");
                            writer.writeAttr("valign", "top");
                            writer.writeEntity(s[i]);
                            writer.endTag(HTMLWriterEx.TD);
                        }
                    }

                    writer.endTag(HTMLWriterEx.TR);
                }

                writer.endTag(HTMLWriterEx.TABLE);
                writer.endTag(HTMLWriterEx.BODY);
                writer.endTag(HTMLWriterEx.HTML);
                writer.close();
            } catch (IOException ex){
                ex.printStackTrace();
            }
            // no finally block to close stream because it wasn't created here
            return;
        }

        private String [] convertPQ(Object[] data) {

            try{

                Properties2 oldT = InterviewPropagator.stringToProperties2((String) data[OLD_TEMPLATE]);
                Properties2 newT = InterviewPropagator.stringToProperties2((String) data[NEW_TEMPLATE]);
                Properties2 conf = InterviewPropagator.stringToProperties2((String) data[CONFIGURATION]);

                HTMLWriterEx[] writers = new HTMLWriterEx[3];
                StringWriter[] stringWriters = new StringWriter[writers.length];
                for(int i = 0; i < writers.length; i++) {
                    stringWriters[i] = new StringWriter();
                    writers[i] = new HTMLWriterEx(stringWriters[i], i18n);
                    writers[i].startTag(HTMLWriterEx.TABLE);
                    writers[i].writeAttr("border", 1);
                }

                for (Object o : conf.keySet()) {
                    String key = (String) o;
                    String[] props = new String[]{newT.getProperty(key), oldT.getProperty(key), conf.getProperty(key)};

                    for (int i = 0; i < props.length; i++) {
                        if (props[i] == null) props[i] = "";
                    }

                    if (!props[0].equals(props[1]) || !props[0].equals(props[2]) || !props[1].equals(props[2])) {
                        for (int i = 0; i < writers.length; i++) {
                            writers[i].startTag(HTMLWriterEx.TR);
                            writers[i].startTag(HTMLWriterEx.TD);
                            writers[i].write(key);
                            writers[i].endTag(HTMLWriterEx.TD);
                            writers[i].startTag(HTMLWriterEx.TD);
                            writers[i].write(props[i]);
                            writers[i].endTag(HTMLWriterEx.TD);
                            writers[i].endTag(HTMLWriterEx.TR);
                        }
                    }
                }

                String[] result = new String[writers.length];
                for(int i = 0; i < writers.length; i++) {
                    writers[i].endTag(HTMLWriterEx.TABLE);
                    writers[i].flush();
                    result[i] = stringWriters[i].toString();
                    writers[i].close();
                }
                return result;
            } catch (IOException ex) {
                logException(ex);
            }
            return new String[] {"", "", ""};
        }

        private String getCSS() {
            BufferedReader r = null;
            try {
                StringBuffer sb = new StringBuffer();

                InputStream is = ResourceLoader.getResourceAsStream(PROP_STYLESHEET, getClass());
                if (is == null) {
                    return "";
                }
                r = new BufferedReader(new InputStreamReader(is));

                String line;
                while( (line = r.readLine()) != null )
                    sb.append(line);
                return sb.toString();
            } catch (IOException ex) {
                ex.printStackTrace();
                return "";
            }
        }


        /**
         * Returns temporary file with html conflict report
         */
        public File getConflictReportFile() {
            if (conflictReport == null) {
                try {
                    conflictReport = File.createTempFile("conflicts", ".html");
                } catch (IOException ex) {
                    logException(ex);
                }
                conflictReport.deleteOnExit();
            }
            return conflictReport;
        }

        /**
         * Returns temporary file with html updates report
         */
        public File getUpdatesReportFile() {
            if (updateReport == null) {
                try {
                    updateReport = File.createTempFile("updates", ".html");
                } catch (IOException ex) {
                    logException(ex);
                }
                updateReport.deleteOnExit();
            }
            return updateReport;
        }

        private void cleanup() {
            conflictMap.clear();
            updateMap.clear();
            partialUpdateMap.clear();
            propQs.clear();
        }


        private HashSet propQs = new HashSet();
        private LinkedHashMap conflictMap = new LinkedHashMap();
        private LinkedHashMap updateMap = new LinkedHashMap();
        private LinkedHashMap partialUpdateMap = new LinkedHashMap();
        private File conflictReport;
        private File updateReport;

        private boolean debug = false;

    }

    public interface ViewManager {
        void showView(InterviewPropagator prop, InterviewParameters interview);
        void logException(Throwable th, InterviewParameters interview);
        void notifyError(String message, InterviewParameters interview);
    }

    public static void setViewManager(ViewManager v) {
        view = v;
    }

    public interface TestRefresher {
        void refreshTestTree(InterviewParameters ip);
    }

    public static void setTestRefresher(TestRefresher r) {
        refresher = r;
    }

    private I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(InterviewPropagator.class);
    private String notAvailable = i18n.getString("tmpltProp.notAvailable");
    private static final String PROP_STYLESHEET = "stylesheet.css";

    private static final int NEW_TEMPLATE = 0;
    private static final int OLD_TEMPLATE = 1;
    private static final int CONFIGURATION = 2;
    private static final int QUESTION_TEXT = 3;

    private static ViewManager view;
    private static TestRefresher refresher;
    private InterviewParameters interview;
    private PropogateMap pm;
    private String [] ignorableProps ;
    private String [] ignorablePrefs ;
}



