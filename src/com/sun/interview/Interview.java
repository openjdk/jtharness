/*
 * $Id$
 *
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.interview;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;


//import com.sun.javatest.util.DirectoryClassLoader;

/**
 * The base class for an interview: a series of {@link Question questions}, to be
 * presented to the user via some tool such as an assistant or wizard.
 * Interviews may be stand-alone, or designed to be part of other interviews.
 */
public class Interview
{
    //----- inner classes ----------------------------------------

    /**
     * This exception is to report problems that occur while updating an interview.
     */
    public static class Fault extends Exception
    {
        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         */
        public Fault(ResourceBundle i18n, String s) {
            super(i18n.getString(s));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An argument to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        public Fault(ResourceBundle i18n, String s, Object o) {
            super(MessageFormat.format(i18n.getString(s), new Object[] {o}));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An array of arguments to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        public Fault(ResourceBundle i18n, String s, Object[] o) {
            super(MessageFormat.format(i18n.getString(s), o));
        }
    }

    /**
     * This exception is thrown when a question is expected to be on
     * the current path, and is not.
     */
    public static class NotOnPathFault extends Fault
    {
        NotOnPathFault(Question q) {
            super(i18n, "interview.questionNotOnPath", q.getTag());
        }
    }

    /**
     * Not for use, provided for backwards binary compatibility.
     * @deprecated No longer used in this API, direct JavaHelp usage was removed.
     */
    @Deprecated
    public static class BadHelpFault extends Fault {
        public BadHelpFault(ResourceBundle i18n, String s, Object e) {
            super(i18n, s, e);
        }
    };


    /**
     * Not for use, provided for backwards binary compatibility.
     * @deprecated No longer used in this API, direct JavaHelp usage was removed.
     */
    @Deprecated
    public static class HelpNotFoundFault extends Fault {
        public HelpNotFoundFault(ResourceBundle i18n, String s, String name) {
            super(i18n, s, name);
        }
    };


    /**
     * An observer interface for receiving notifications as the state of
     * the interview is updated.
     */
    public static interface Observer {
        /**
         * Invoked when the current question in the interview has been changed.
         * @param q the new current question
         */
        void currentQuestionChanged(Question q);

        /**
         * Invoked when the set of questions in the current path has been
         * changed. This is normally because the response to one of the
         * questions on the path has been changed, thereby causing a change
         * to its successor questions.
         */
        void pathUpdated();
    }

    //----- constructors ----------------------------------------

    /**
     * Create a top-level interview.
     * @param tag A tag that will be used to qualify the tags of any
     * questions in this interview, to help ensure uniqueness of those
     * tags.
     */
    protected Interview(String tag) {
        this(null, tag);
    }

    /**
     * Create an interview to be used as part of another interview.
     * @param parent The parent interview of which this is a part.
     * @param baseTag A name that will be used to qualify the tags of any
     * questions in this interview, to help ensure uniqueness of those
     * tags. It will be combined with the parent's tag if that has been
     * specified.
     */
    protected Interview(Interview parent, String baseTag) {
        this.parent = parent;
        setBaseTag(baseTag);

        if (parent == null)
            root = this;
        else {
            parent.add(this);
            root = parent.root;
            semantics = parent.getInterviewSemantics();
        }
    }

    //----- basic facilities ----------------------------------------

    /**
     * Get the parent interview for which this is a child.
     * @return the parent interview, or null if no parent has been specified.
     */
    public Interview getParent() {
        return parent;
    }

    /**
     * Get a tag used to qualify the tags of questions in this interview.
     * @return the title
     */
    public String getTag() {
        return tag;
    }

    /**
     * Set a descriptive title to be used to annotate this interview.
     * @param title A short descriptive title.
     * @see #getTitle
     */
    protected void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get a descriptive title associated with this interview.
     * If not specified, the system will try and locate the title in the
     * interview's resource bundle, using the resource name <code>title</code>.
     * of the interview.
     * @return the title
     * @see #setTitle
     */
    public String getTitle() {
        if (title == null) {
            // Need to dance a bit here to avoid "title" being picked up
            // by the i18n validation scripts as a necessary key in i18n
            // instead of bundle.  Another solution would be to make
            // getI18NString static and pass the resource bundle in as the
            // first arg.
            String titleKey = "title";
            title = getI18NString(titleKey).trim();
        }

        return title;
    }

    /**
     * Set a default image to be used for the questions of an interview.
     * @param u A URL for the image
     * @see Question#setImage
     * @see Question#getImage
     * @see #getDefaultImage
     */
    protected void setDefaultImage(URL u) {
        defaultImage = u;
    }

    /**
     * Get a default image to be used for the questions of an interview.
     * If no default has been set for this interview, the parent's
     * default image (if any) is used instead.
     * @return a URL for the default image to be used
     * @see #setDefaultImage
     */
    public URL getDefaultImage() {
        if (defaultImage == null && parent != null)
            return parent.getDefaultImage();

        return defaultImage;
    }

    /**
     * Set the base name of the resource bundle used to look up
     * internationalized strings, such as the title and text of each
     * question.  If the name starts with '/', it will be treated
     * as an absolute resource name, and used "as is";
     * otherwise it will be treated as relative to the
     * package in which the actual interview class is defined.
     * The default is the interview tag name if this is a root
     * interview. If this is a child interview, there is no default
     * resource bundle.
     * @param name The name of the resource bundle used to look
     * up internationalized strings.
     * @throws MissingResourceException if the resource bundle
     * cannot be found.
     * @see #getResourceBundle
     */
    protected void setResourceBundle(String name)
        throws MissingResourceException
    {
        // name is not null
        if (!name.equals(bundleName)) {
            Class c = getClass();
            ClassLoader cl = c.getClassLoader();
            String rn;
            if (name.startsWith("/"))
                rn = name.substring(1);
            else {
                String cn = c.getName();
                String pn = cn.substring(0, cn.lastIndexOf('.'));
                rn = pn + "." + name;
            }
            //System.err.println("INT: looking for bundle: " + rn);
            bundle = ResourceBundle.getBundle(rn, Locale.getDefault(), cl);
            bundleName = name;
        }
    }
    /**
     * Set the base name of the resource bundle used to look up
     * internationalized strings, such as the title and text of each
     * question. If the name is treated as filename of file
     * which is located in directory file.
     * The default is the interview tag name if this is a root
     * interview. If this is a child interview, there is no default
     * resource bundle.
     * @param name The name of the resource bundle used to look
     * up internationalized strings.
     * @param file The directory to find name.
     * @throws MissingResourceException if the resource bundle
     * cannot be found.
     * @see #getResourceBundle
     */
    protected void setResourceBundle(String name, File file)
            throws MissingResourceException {
        if (bundleName != null && bundleName.equals(name)) {
            return;
        }
        try {
            URL[] url = {new URL("file:" + file.getAbsolutePath() + "/")};
            URLClassLoader cl = new URLClassLoader(url);
            bundle = ResourceBundle.getBundle(name, Locale.getDefault(), cl);
            bundleName = name;
        } catch (MalformedURLException e) {
        }

    }

    /**
     * Get the resource bundle for this interview, used to look up
     * internationalized strings, such as the title and text of each question.
     * If the bundle has not been set explicitly, it defaults to the
     * parent's resource bundle; the root interview has a default resource
     * bundle based on the interview tag name.
     * @return the resource bundle for this interview.
     * @see #setResourceBundle
     */
    public ResourceBundle getResourceBundle() {
        if (bundle == null && parent != null)
            return parent.getResourceBundle();
        else
            return bundle;
    }

    /**
     * Set the name of the help set used to locate the "more info"
     * for each question. The name should identify a resource containing
     * a JavaHelp helpset file. If the name starts with '/', it will
     * be treated as an absolute resource name, and used "as is";
     * otherwise it will be treated as relative to the
     * package in which the actual interview class is defined.
     * If help sets are specified for child interviews, they will
     * automatically be added into the help set for the root interview.
     * @param name The name of the help set containing the "more info"
     * for each question.
     * @throws Interview.HelpNotFoundFault if the help set could not be located
     * @throws Interview.BadHelpFault if some problem occurred while opening the help set
     * @see #getHelpSet
     * @see #setHelpSet(Object)
     */
    protected void setHelpSet(String name) throws Interview.Fault {
        setHelpSet(helpSetFactory.createHelpSetObject(name, getClass()));
    }


    /**
     * Set the help set used to locate the "more info" for each question.
     * If help sets are specified for child interviews, they will
     * automatically be added into the help set for the root interview.
     * @param hs The help set containing the "more info" for each question
     * in this interview.
     * @see #getHelpSet
     * @see #setHelpSet(String)
     */
    protected void setHelpSet(Object hs) {
        helpSet = helpSetFactory.updateHelpSetObject(this, hs);
    }

    /**
     * Set the name of the help set used to locate the "more info"
     * for each question. The name should identify a resource containing
     * a JavaHelp helpset file. If the name is treated as filename of file
     * which is located in directory file.
     * If help sets are specified for child interviews, they will
     * automatically be added into the help set for the root interview.
     * @param name The name of the help set containing the "more info"
     * for each question.
     * @param file The directory to find help set.
     * @throws Interview.HelpNotFoundFault if the help set could not be located
     * @throws Interview.BadHelpFault if some problem occurred while opening the help set
     * @see #getHelpSet
     * @see #setHelpSet(Object)
     * @see #setHelpSet(String)
     */
    protected void setHelpSet(String name, File file) throws Interview.Fault {
        setHelpSet(helpSetFactory.createHelpSetObject(name, file));
    }


    /**
         * Get the help set used to locate the "more info" for each question. If the
         * help set has not been set explicitly, it defaults to the parent's help
         * set.
         *
         * @return the help set used to locate "more info" for questions in this
         *         interview.
         * @see #setHelpSet
         */
    public Object getHelpSet() {
        if (helpSet == null && parent != null)
            return parent.getHelpSet();

        return helpSet;
    }

    /**
     * Initializes the help factory - generally only called once per instance of the
     * system.
     * @return Create the help factory for the interview system.
     */
    private static HelpSetFactory createHelpFactory() {
        try {
            Class factoryClass = Class.forName("com.sun.interview.JavaHelpFactory");
            return (HelpSetFactory) factoryClass.newInstance();
        } catch (ClassNotFoundException e) {
            return HelpSetFactory.DEFAULT;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return HelpSetFactory.DEFAULT;
        }

    }

    /**
     * Mark this interview as having been edited or not.
     * @param edited whether or not this interview is marked as edited
     */
    public void setEdited(boolean edited) {
        Interview i = this;
        while (i.parent != null)
            i = i.parent;
        i.edited = edited;
    }


    /**
     * Determine if this interview as having been edited or not.
     * @return true if this interview is marked as having been edited
     */
    public boolean isEdited() {
        Interview i = this;
        while (i.parent != null)
            i = i.parent;
        return i.edited;
    }

    /**
     * Get the first question of the interview.
     * @return the first question of the interview
     * @see #setFirstQuestion
     */
    public Question getFirstQuestion() {
        return firstQuestion;
    }

    /**
     * Set the first question for an interview. This may be called more
     * than once, but only while the interview is being constructed.
     * Once any method has been called that refers to the interview
     * path, the initial question may not be changed.
     * @param q The initial question
     * @throws IllegalStateException if it is too late to change the
     * initial question.
     * @see #getFirstQuestion
     */
    protected void setFirstQuestion(Question q) {
        if (path != null)
            throw new IllegalStateException();

        firstQuestion = q;

        // OLD: the problem with this is that reset() calls updatePath()
        // which might call methods which refer to uninitialize data,
        // so can't safely call reset() here
        //
        // if (parent == null)
        //    reset();

        // if we wanted to permit the first question to be changed,
        // consider the following:
        // if (parent == null)
        //    path = null;
    }

    //---------------------------------------------------------

    /**
     * Get a sub-interview with a given tag name. All descendents are
     * searched (i.e. all children, all their children, etc.)
     * @param tag The tag of the interview to be found.
     * @return the sub-interview with the specified name.
     * @throws Interview.Fault if no interview is found with the given name.
     */
    public Interview getInterview(String tag) throws Fault {
        if (tag == null)
            throw new NullPointerException();

        Interview i = getInterview0(tag);
        if (i != null)
            return i;
        else
            throw new Fault(i18n, "interview.cantFindInterview", tag);
    }

    private Interview getInterview0(String t) {
        if (t.equals(tag))
            return this;

        for (int i = 0; i < children.size(); i++) {
            Interview c = (Interview) (children.elementAt(i));
            Interview iv = c.getInterview0(t);
            if (iv != null)
                return iv;
        }

        return null;
    }

    Set getInterviews() {
        Set s = new HashSet();
        getInterviews0(s);
        return s;
    }

    private void getInterviews0(Set s) {
        s.add(this);
        for (int i = 0; i < children.size(); i++) {
            Interview child = (Interview) (children.elementAt(i));
            child.getInterviews0(s);
        }
    }

    //----- navigation ----------------------------------------

    /**
     * Determine if a question is the first question of the interview.
     * @param q the question to check
     * @return true if this is the first question.
     */
    public boolean isFirst(Question q) {
        return (q == firstQuestion);
    }

    /**
     * Determine if a question is the last question of the interview.
     * @param q the question to check
     * @return true if this is the last question.
     */
    public boolean isLast(Question q) {
        return (q instanceof FinalQuestion && q.interview.caller == null);
    }

    /**
     * Determine if a question has a non-null successor.
     * @param q the question to check
     * @return true if this question has a non-null successor.
     */
    public boolean hasNext(Question q) {
        return (q.getNext() != null);
    }


    /**
     * Determine if a question has a successor which is neither null
     * nor an ErrorQuestion.
     * @param q the question to check
     * @return true if this question has a successor which is neither null
     * nor an ErrorQuestion
     */
    public boolean hasValidNext(Question q) {
        Question qn = q.getNext();
        return (qn != null && !(qn instanceof ErrorQuestion));
    }

    /**
     * Start (or restart) the interview. The current question is reset to the first
     * question, and the current path is evaluated from there.
     */
    public void reset() {
        ensurePathInitialized();

        // first, reset back to the beginning
        updateEnabled = true;
        caller = null;
        currIndex = 0;
        path.clear();
        path.addQuestion(firstQuestion);

        if (root == this) {
            rawPath.clear();
            rawPath.addQuestion(firstQuestion);
        }

        hiddenPath.clear();
        updatePath(firstQuestion);
        // notify observers
        notifyCurrentQuestionChanged(firstQuestion);
    }

    /**
     * Start (or restart) the interview. The current question is reset to the first
     * question, and the current path is evaluated from there.
     */
    private void reset(Question q) {
        ensurePathInitialized();

        // first, reset back to the beginning
        updateEnabled = true;
        caller = null;
        currIndex = 0;
        path.clear();
        hiddenPath.clear();
        if (root == this) {
            rawPath.clear();
            rawPath.addQuestion(firstQuestion);
        }

        path.addQuestion(firstQuestion);
        updatePath(firstQuestion);

        // now update to the selected question
        if (q == firstQuestion || q == null)
            // already there; just need to notify observers
            notifyCurrentQuestionChanged(firstQuestion);
        else {
            // try and select the specified question
            try {
                setCurrentQuestion(q);
            }
            catch (Fault e) {
                notifyCurrentQuestionChanged(firstQuestion);
            }
        }
    }

    /**
     * Advance to the next question in the interview.
     * Questions that have been {@link Question#isEnabled disabled} will
     * be skipped over.
     * @throws Interview.Fault if there are no more questions
     */
    public void next() throws Fault {
        ensurePathInitialized();

        Interview i = this;

        // first, step in until we get to the current question
        while (i.path.questionAt(i.currIndex) instanceof InterviewQuestion) {
            InterviewQuestion iq = (InterviewQuestion) (i.path.questionAt(i.currIndex));
            i = iq.getTargetInterview();
        }

        // next, step forward to the next question
        i.currIndex++;

        // finally, normalize the result
        while (true) {
            if (i.currIndex == i.path.size()) {
                i.currIndex--;
                throw new Fault(i18n, "interview.noMoreQuestions");
            }

            Question q = i.path.questionAt(i.currIndex);
            if (q instanceof InterviewQuestion) {
                InterviewQuestion iq = (InterviewQuestion) q;
                i = iq.getTargetInterview();
                i.currIndex = 0;
            }
            else if (q instanceof FinalQuestion && i.caller != null) {
                i = i.caller.getInterview();
                i.currIndex++;
            }
            else
                break;
        }

        Question q = i.path.questionAt(i.currIndex);
        notifyCurrentQuestionChanged(q);
    }

    /**
     * Back up to the previous question in the interview.
     * Questions that have been {@link Question#isEnabled disabled} will
     * be skipped over.
     * @throws Interview.Fault if there is no previous question.
     */
    public void prev() throws Fault {
        ensurePathInitialized();

        Interview i = this;

        // first, step in until we get to the current question
        while (i.path.questionAt(i.currIndex) instanceof InterviewQuestion) {
            InterviewQuestion iq = (InterviewQuestion) (i.path.questionAt(i.currIndex));
            i = iq.getTargetInterview();
        }

        // next, step back to the next question
        i.currIndex--;

        // finally, normalize the result
        while (true) {
            if (i.currIndex < 0) {
                if (i.caller == null) {
                    i.currIndex = 0;
                    throw new Fault(i18n, "interview.noMoreQuestions");
                }
                else {
                    i = i.caller.getInterview();
                    i.currIndex--;
                }
            }
            else if (i.path.questionAt(i.currIndex) instanceof InterviewQuestion) {
                InterviewQuestion iq = (InterviewQuestion) (i.path.questionAt(i.currIndex));
                i = iq.getTargetInterview();
                i.currIndex = i.path.size() - 1;
            }
            else if (i.path.questionAt(i.currIndex) instanceof FinalQuestion) {
                i.currIndex--;
            }
            else
                break;
        }

        Question q = i.path.questionAt(i.currIndex);
        notifyCurrentQuestionChanged(q);
    }

    /**
     * Advance to the last question in the interview.
     * Questions that have been {@link Question#isEnabled disabled} will
     * be skipped over.
     * @throws Interview.Fault if there are no more questions
     */
    public void last() throws Fault {
        ensurePathInitialized();

        Interview i = this;

        // first, step in until we get to the current question
        while (i.path.questionAt(i.currIndex) instanceof InterviewQuestion) {
            InterviewQuestion iq = (InterviewQuestion) (i.path.questionAt(i.currIndex));
            i = iq.getTargetInterview();
        }

        // navigate around the interview without upsetting any interview's currIndex
        int index = i.currIndex;

        // Scan forward looking for candidates for the last question.
        // The alternative is to advance i.currIndex to the end of this
        // interview and normalize the result, but that gets complicated
        // with the interaction between nested interviews and hidden
        // questions.
        Question cq = i.path.questionAt(index);
        Question lq = cq;
        index++;

        while (index < i.path.size()) {
            Question q = i.path.questionAt(index);

            if (q instanceof InterviewQuestion) {
                i = ((InterviewQuestion) q).getTargetInterview();
                index = 0;
            }
            else if (q instanceof FinalQuestion && i.caller != null) {
                Interview callInterview = i.caller.getInterview();
                int callIndex = callInterview.path.indexOf(i);
                if (callIndex == -1)
                    throw new IllegalStateException();
                i = callInterview;
                index = callIndex + 1;
            }
            else {
                // update candidate and move on
                lq = q;
                index++;
            }
        }

        if (lq == cq) {
            if ( !(lq instanceof FinalQuestion))
                throw new Fault(i18n, "interview.noMoreQuestions");
        }
        else
            setCurrentQuestion(lq);
    }

    /**
     * Check if the interview has been started. An interview is
     * considered to be at the beginning if there is only one
     * question on the current path of a type that requires a response.
     * This indirectly implies it must be the last question on
     * the current path, and must only be preceded by
     * {@link NullQuestion information-only} questions.
     * @return true if the first answerable question is unanswered.
     */
    public boolean isStarted() {
        Question[] path = root.getPath();
        for (int i = 0; i < path.length - 1; i++) {
            Question q = path[i];
            if (!(q instanceof NullQuestion))
                return true;
        }
        return false;
    }

    /**
     * Check if the interview has been completed. An interview is
     * considered to have been completed if the final question
     * on the current path is of type {@link FinalQuestion}.
     * @return true if the interview has been completed.
     */
    public boolean isFinishable() {
        ensurePathInitialized();

        Interview i = root;
        return (i.path.lastQuestion() instanceof FinalQuestion);
    }

    /**
     * Check if this subinterview has been completed. A subinterview is
     * considered to have been completed if none of the questions from
     * this subinterview on the current path return null as the result
     * of getNext().
     *<em>Note:</em>compare this to isFinishable() which checks that the
     * entire interview (of which this subinterview may be a part) is
     * complete.
     * @return true is this subinterview has been completed.
     */
    protected boolean isInterviewFinishable() {
        return (path != null && path.lastQuestion() instanceof FinalQuestion);
    }


    /**
     * Jump to a specific question in the interview. The question
     * must be on the current path, but can be either before or
     * after the current position at the time this is called.
     * @param q The question which is to become the current
     * question in the interview.
     * @throws Interview.Fault if the question given is not on the current path.
     * @see #getCurrentQuestion
     */
    public void setCurrentQuestion(Question q) throws Fault {
        if (q == null)
            throw new NullPointerException();

        if (q == getCurrentQuestion())
            return;

        boolean ok = root.setCurrentQuestion0(q);
        if (!ok)
            throw new NotOnPathFault(q);

        notifyCurrentQuestionChanged(q);
    }

    private boolean setCurrentQuestion0(Question q) {
        ensurePathInitialized();

        for (int i = 0; i < path.size(); i++) {
            Question qq = path.questionAt(i);
            if (qq.equals(q)) {
                currIndex = i;
                return true;
            }
            else if (qq instanceof InterviewQuestion) {
                if (((InterviewQuestion) qq).getTargetInterview().setCurrentQuestion0(q)) {
                    currIndex = i;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the current question in the interview.
     * @return The current question.
     * @see #setCurrentQuestion
     */
    public Question getCurrentQuestion() {
        ensurePathInitialized();

        Interview i = root;
        Question q = i.path.questionAt(i.currIndex);
        while (q instanceof InterviewQuestion) {
            i = ((InterviewQuestion) q).getTargetInterview();
            q = i.path.questionAt(i.currIndex);
        }
        return q;
    }

    private void setCurrentQuestionFromPath(Question[] path) {
        root.setCurrentQuestionFromPath0(path);
    }

    private void setCurrentQuestionFromPath0(Question[] path) {
        for (int i = path.length - 1; i >= 0; i--) {
            if (setCurrentQuestion0(path[i])) {
                notifyCurrentQuestionChanged(path[i]);
                return;
            }
        }
    }

    //----- path stuff ----------------------------------------

    /**
     * Get the set of questions on the current path.
     * The first question is determined by the interview; after that,
     * each question in turn determines its successor. The path ends
     * when a question indicates no successor (or erroneously returns
     * a question that is already on the path, that would otherwise
     * form a cycle). The special type of question, {@link FinalQuestion},
     * never returns a successor.
     * Within a particular interview, a question may refer to a
     * nested interview, before continuing within the original interview.
     * Any such references to nested interviews are automatically
     * expanded by this method, leaving just the complete set of basic
     * questions on the path.
     * @return an array containing the list of questions on the current path.
     * @see #setFirstQuestion
     * @see Question#getNext
     * @see #getPathToCurrent
     */
    public Question[] getPath() {
        Vector v = new Vector();
        iteratePath0(v, true, true, true);
        Question[] p = new Question[v.size()];
        v.copyInto(p);
        return p;
    }

    /**
     * Get the set of questions on the current path up to and
     * including the current question.
     * @return an array containing the list of questions on the
     * current path up to and including the current question
     * @see #getPath
     */
    public Question[] getPathToCurrent() {
        Vector v = new Vector();
        iteratePath0(v, true, false, true);
        Question[] p = new Question[v.size()];
        v.copyInto(p);
        return p;
    }

    /**
     * Get the current set path of questions, including some things normally
     * hidden.  Hidden, disabled and final questions are included upon demand.
     * The list of questions is flattend to only include questions, no
     * representation of the interview structure is given.
     * @param includeFinals Should FinalQuestions be included.
     * @return The current active path of questions, based on the requested
     *    options.  Returns null if no path information is available.
     */
    public Question[] getRawPath(boolean includeFinals) {
        if (rawPath == null)
            return null;
        else
            return rawPath.getQuestions();
    }

    /**
     * Get an iterator for the set of questions on the current path.
     * The first question is determined by the interview; after that,
     * each question in turn determines its successor. The path ends
     * when a question indicates no successor (or erroneously returns
     * a question that is already on the path, that would otherwise
     * form a cycle). The special type of question, {@link FinalQuestion},
     * never returns a successor.
     * Within a particular interview, a question may refer to a
     * nested interview, before continuing within the original interview.
     * Such nested interviews may optionally be expanded by this method,
     * depending on the arguments.
     * @param flattenNestedInterviews If true, any nested interviews will
     * be expanded in place and returned via the iterator; otherwise, the
     * the nested interview will be returned instead.
     * @return an Iterator for the questions on the current path
     * @see #iteratePathToCurrent
     */
    public Iterator iteratePath(boolean flattenNestedInterviews) {
        Vector v = new Vector();
        iteratePath0(v, flattenNestedInterviews, true, true);
        return v.iterator();
    }


    /**
     * Get an iterator for the set of questions on the current path
     * up to and including the current question.
     * @param flattenNestedInterviews If true, any nested interviews will
     * be expanded in place and returned via the iterator; otherwise, the
     * the nested interview will be returned instead.
     * @return an Iterator for the questions on the current path
     * up to and including the current question
     * @see #iteratePath
     */
    public Iterator iteratePathToCurrent(boolean flattenNestedInterviews) {
        Vector v = new Vector();
        iteratePath0(v, flattenNestedInterviews, false, true);
        return v.iterator();
    }


    private void iteratePath0(List l, boolean flattenNestedInterviews, boolean all, boolean addFinal) {
        ensurePathInitialized();

        int n = (all ? path.size() : currIndex + 1);
        for (int i = 0; i < n; i++) {
            Question q = path.questionAt(i);
            if (q instanceof InterviewQuestion) {
                if (flattenNestedInterviews)
                    ((InterviewQuestion) q).getTargetInterview().iteratePath0(l, true, all, false);
                else
                    l.add(q);
            }
            else if (!addFinal && q instanceof FinalQuestion)
                return;
            else
                l.add(q);
        }
    }


    /**
     * Verify that the current path contains a specified question,
     * and throw an exception if it does not.
     * @param q the question to be checked
     * @throws Interview.NotOnPathFault if the current path does not contain
     * the specified question.
     */
    public void verifyPathContains(Question q)
        throws NotOnPathFault
    {
        if (!pathContains(q))
            throw new NotOnPathFault(q);
    }

    /**
     * Check if the path contains a specific question.
     * @param q The question for which to check.
     * @return true if the question is found on the current path.
     */
    public boolean pathContains(Question q) {
        return root.pathContains0(q);
    }

    /**
     * Check if the path contains questions from a specific interview.
     * @param i The interview for which to check.
     * @return true if the interview is found on the current path.
     */
    public boolean pathContains(Interview i) {
        return  root.pathContains0(i);
    }

    private boolean pathContains0(Object o) {
        ensurePathInitialized();

        for (int index = 0; index < path.size(); index++) {
            Question q = path.questionAt(index);
            if (o == q)
                return true;

            if (q instanceof InterviewQuestion) {
                InterviewQuestion iq = (InterviewQuestion) q;
                Interview i = iq.getTargetInterview();
                if (o == i)
                    return true;

                if (i.pathContains0(o))
                    return true;
            }
        }

        return false;
    }

    /**
     * Get the complete set of questions in this interview and
     * recursively, in all child interviews.
     * @return a set of all questions in this and every child interview.
     */
    public Set getQuestions() {
        Set s = new HashSet();
        getQuestions0(s);
        return s;
    }

    private void getQuestions0(Set s) {
        s.addAll(allQuestions.values());

        for (int i = 0; i < children.size(); i++) {
            Interview child = (Interview) (children.elementAt(i));
            child.getQuestions0(s);
        }
    }

    /**
     * Get all questions in this interview and
     * recursively, in all child interviews.
     * @return a map containing all questions in this and every child interview.
     */
    public Map getAllQuestions() {
        Map m = new LinkedHashMap();
        getAllQuestions0(m);
        return m;
    }

    private void getAllQuestions0(Map m) {
        m.putAll(allQuestions);

        for (int i = 0; i < children.size(); i++) {
            Interview child = (Interview) (children.elementAt(i));
            child.getAllQuestions0(m);
        }
    }

    /**
     * Check whether any questions on the current path have any
     * associated checklist items.
     * @return true if no questions have any corresponding checklist
     * items, and false otherwise.
     */
    public boolean isChecklistEmpty() {
        for (Iterator iter = iteratePath(true); iter.hasNext(); ) {
            Question q = (Question) (iter.next());
            Checklist.Item[] items = q.getChecklistItems();
            if (items != null && items.length > 0)
                return false;
        }
        return true;
    }

    /**
     * Create a checklist composed of all checklist items
     * for questions on the current path.
     * @return a checklist composed of all checklist items
     * for questions on the current path.
     * @see #getPath
     * @see Question#getChecklistItems
     */
    public Checklist createChecklist() {
        Checklist c = new Checklist();
        for (Iterator iter = iteratePath(true); iter.hasNext(); ) {
            Question q = (Question) (iter.next());
            Checklist.Item[] items = q.getChecklistItems();
            if (items != null) {
                for (int i = 0; i < items.length; i++)
                    c.add(items[i]);
            }
        }
        return c;
    }

    /**
     * Create a checklist item based on entries in the interview's resource bundle.
     * @param sectionKey A key to identify the section name within the interview's resource bundle
     * @param textKey A key to identify the checklist item text within the interview's resource bundle
     * @return a Checklist.Item object composed from the appropriate entries in the interview's resource bundle
     */
    public Checklist.Item createChecklistItem(String sectionKey, String textKey) {
        String section = getI18NString(sectionKey);
        String text = getI18NString(textKey);
        return new Checklist.Item(section, text);
    }


    /**
     * Create a checklist item based on entries in the interview's resource bundle.
     * @param sectionKey A key to identify the section name within the interview's resource bundle
     * @param textKey A key to identify the checklist item text within the interview's resource bundle
     * @param textArg a single argument to be formatted into the checklist item text
     * @return a Checklist.Item object composed from the appropriate entries in the interview's resource bundle and the specified argument value
     */
    public Checklist.Item createChecklistItem(String sectionKey, String textKey, Object textArg) {
        String section = getI18NString(sectionKey);
        String text = getI18NString(textKey, textArg);
        return new Checklist.Item(section, text);
    }


    /**
     * Create a checklist item based on entries in the interview's resource bundle.
     * @param sectionKey A key to identify the section name within the interview's resource bundle
     * @param textKey A key to identify the checklist item text within the interview's resource bundle
     * @param textArgs an array of arguments to be formatted into the checklist item text
     * @return a Checklist.Item object composed from the appropriate entries in the interview's resource bundle and the specified argument values
     */
    public Checklist.Item createChecklistItem(String sectionKey, String textKey, Object[] textArgs) {
        String section = getI18NString(sectionKey);
        String text = getI18NString(textKey, textArgs);
        return new Checklist.Item(section, text);
    }

    //----- markers ---------------------------------

    /**
     * Add a named marker for a question.
     * @param q The question for which to add the marker
     * @param name The name of the marker to be added.
     * @throws NullPointerException if the question is null.
     */
    void addMarker(Question q, String name) {
        if (root != this) {
            root.addMarker(q, name);
            return;
        }

        if (q == null)
            throw new NullPointerException();

        if (allMarkers == null)
            allMarkers = new HashMap();

        Set markersForName = (Set) (allMarkers.get(name));
        if (markersForName == null) {
            markersForName = new HashSet();
            allMarkers.put(name, markersForName);
        }

        markersForName.add(q);
    }

    /**
     * Remove a named marker for a question.
     * @param q The question for which to remove the marker
     * @param name The name of the marker to be removeded.
     * @throws NullPointerException if the question is null.
     */
    void removeMarker(Question q, String name) {
        if (root != this) {
            root.removeMarker(q, name);
            return;
        }

        if (q == null)
            throw new NullPointerException();

        if (allMarkers == null)
            return;

        Set markersForName = (Set) (allMarkers.get(name));
        if (markersForName == null)
            return;

        markersForName.remove(q);

        if (markersForName.size() == 0)
            allMarkers.remove(name);
    }

    /**
     * Check if a question has a named marker.
     * @param q The question for which to check for the marker
     * @param name The name of the marker to be removed.
     * @throws NullPointerException if the question is null.
     */
    boolean hasMarker(Question q, String name) {
        if (root != this)
            return root.hasMarker(q, name);

        if (q == null)
            throw new NullPointerException();

        if (allMarkers == null)
            return false;

        Set markersForName = (Set) (allMarkers.get(name));
        if (markersForName == null)
            return false;

        return markersForName.contains(q);
    }

    /**
     * Remove all the markers with a specified name.
     * @param name The name of the markers to be removed
     */
    public void removeMarkers(String name) {
        if (root != this) {
            root.removeMarkers(name);
            return;
        }

        // just have to remove the appropriate set of markers
        if (allMarkers != null)
            allMarkers.remove(name);
    }

    /**
     * Remove all the markers, whatever their name.
     */
    public void removeAllMarkers() {
        if (root != this) {
            root.removeAllMarkers();
            return;
        }

        allMarkers = null;
    }

    /**
     * Clear the response to marked questions.
     * @param name The name of the markers for the questions to be cleared.
     */
    public void clearMarkedResponses(String name) {
        if (root != this) {
            root.clearMarkedResponses(name);
            return;
        }

        if (allMarkers == null) // no markers at all
            return;

        Set markersForName = (Set) (allMarkers.get(name));
        if (markersForName == null) // no markers for this name
            return;

        updateEnabled = false;
        Question oldCurrentQuestion = getCurrentQuestion();

        for (Iterator iter = markersForName.iterator(); iter.hasNext(); ) {
            Question q = (Question) (iter.next());
            q.clear();
        }

        updateEnabled = true;
        updatePath(firstQuestion);

        Question newCurrentQuestion = getCurrentQuestion();
        if (newCurrentQuestion != oldCurrentQuestion)
            notifyCurrentQuestionChanged(newCurrentQuestion);
    }

    private void loadMarkers(Map data) {
        String s = (String) (data.get(MARKERS));
        int count = 0;
        if (s != null) {
            try {
                count = Integer.parseInt(s);
            }
            catch (NumberFormatException e) {
                // ignore
            }
        }

        allMarkers = null;

        for (int i = 0; i < count; i++) {
            String name = (String) (data.get(MARKERS_PREF + i + ".name"));
            String tags = (String) (data.get(MARKERS_PREF + i));
            if (tags != null)
                loadMarkers(name, tags);
        }
    }

    private void loadMarkers(String name, String tags) {
        int start = -1;
        for (int i = 0; i < tags.length(); i++) {
            if (tags.charAt(i) == '\n') {
                if (start != -1) {
                    String tag = tags.substring(start, i).trim();
                    loadMarker(name, tag);
                    start = -1;
                }
            }
            else
                if (start == -1)
                    start = i;
        }
        if (start != -1) {
            String tag = tags.substring(start).trim();
            loadMarker(name, tag);
        }
    }

    private void loadMarker(String name, String tag) {
        if (tag.length() > 0) {
            Question q = lookup(tag);
            if (q != null)
                addMarker(q, name);
        }
    }

    private void saveMarkers(Map data) {
        if (allMarkers == null)
            return;

        int i = 0;
        for (Iterator iter = allMarkers.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry e = (Map.Entry) (iter.next());
            String name = (String) (e.getKey());
            Set markersForName = (Set) (e.getValue());
            if (name != null)
                data.put(MARKERS_PREF + i + ".name", name);
            StringBuffer sb = new StringBuffer();
            for (Iterator qIter = markersForName.iterator(); qIter.hasNext(); ) {
                Question q = (Question) (qIter.next());
                if (sb.length() > 0)
                    sb.append('\n');
                sb.append(q.getTag());
            }
            data.put(MARKERS_PREF + i, sb.toString());
            i++;
        }

        if (i > 0)
            data.put(MARKERS, String.valueOf(i));
    }


    //----- nested interview stuff ---------------------------------

    /**
     * Return a special type of question used to indicate that
     * a sub-interview interview should be called before proceeding
     * to the next question in this interview.
     * @param i The nested interview to be called next
     * @param q The next question to be asked when the nested
     * interview has completes with a {@link FinalQuestion final question}.
     * @return a pseudo-question that will call a nested interview before
     * continuing with the specified follow-on question.
     */
    protected Question callInterview(Interview i, Question q) {
        return new InterviewQuestion(this, i, q);
    }

    //----- load/save stuff ----------------------------------------

    /**
     * Clear any responses to all the questions in this interview, and then
     * recursively, in its child interviews.
     */
    public void clear() {
        updateEnabled = false;
        for (Iterator iter = allQuestions.values().iterator(); iter.hasNext(); ) {
            Question q = (Question) (iter.next());
            q.clear();
        }

        for (int i = 0; i < children.size(); i++) {
            Interview child = (Interview)children.elementAt(i);
            child.clear();
        }
        if (parent == null) {
            extraValues = null;
            templateValues = null;
            reset();
        }
    }

    /**
     * Load the state for questions from an archive map. The map
     * will be passed to each question in this interview and in any
     * child interviews, and each question should {@link Question#load load}
     * its state, according to its tag.
     * The data must normally contain a valid checksum, generated during {@link #save}.
     * @param data The archive map from which the state should be loaded.
     * @throws Interview.Fault if the checksum is found to be incorrect.
     */
    public void load(Map data) throws Fault {
        load(data, true);
    }

    /**
     * Load the state for questions from an archive map. The map
     * will be passed to each question in this interview and in any
     * child interviews, and each question should {@link Question#load load}
     * its state, according to its tag.
     * The data must normally contain a valid checksum, generated during {@link #save}.
     * @param data The archive map from which the state should be loaded.
     * @param checkChecksum If true, the checksum in the data will be checked.
     * @throws Interview.Fault if the checksum is found to be incorrect.
     */
    public void load(Map data, boolean checkChecksum) throws Fault {
        if (checkChecksum && !isChecksumValid(data, true))
            throw new Fault(i18n, "interview.checksumError");

        if (parent == null) {
            String iTag = (String)(data.get(INTERVIEW));
            if (iTag != null && !iTag.equals(getClass().getName()))
                throw new Fault(i18n, "interview.classMismatch");

            loadExternalValues(data);
            loadTemplateValues(data);
        }

        updateEnabled = false;

        // clear all the answers in this interview before loading an
        // responses from the archive
        for (Iterator iter = allQuestions.values().iterator(); iter.hasNext(); ) {
            Question q = (Question) (iter.next());
            q.clear();
        }

        for (Iterator iter = allQuestions.values().iterator(); iter.hasNext(); ) {
            Question q = (Question) (iter.next());
            q.load(data);
        }

        for (int i = 0; i < children.size(); i++) {
            Interview child = (Interview)children.elementAt(i);
            child.load(data, false);
        }

        if (parent == null) {
            String qTag = (String)(data.get(QUESTION));
            Question q = (qTag == null ? null : lookup(qTag));
            reset(q == null ? firstQuestion : q);
        }

        loadMarkers(data);
    }

    /**
     * Check if the checksum is valid for a set of responses.
     * When responses are saved to a map, they are checksummed,
     * so that they can be checked for validity when reloaded.
     * This method verifies that a set of responses are acceptable
     * for loading.
     * @param data The set of responses to be checked.
     * @param okIfOmitted A boolean determining the response if
     * there is no checksum available in the data
     * @return Always true.
     * @deprecated As of version 4.4.1, checksums are no longer
     *    calculated or checked.  True is always returned.
     */
    public static boolean isChecksumValid(Map data, boolean okIfOmitted) {
        return true;
    }

    /**
     * Save the state for questions in an archive map. The map
     * will be passed to each question in this interview and in any
     * child interviews, and each question should {@link Question#save save}
     * its state, according to its tag.
     * @param data The archive in which the values should be saved.
     */
    public void save(Map data) {
        // only in the root interview
        if (parent == null) {
            data.put(INTERVIEW, getClass().getName());
            data.put(QUESTION, getCurrentQuestion().getTag());
            writeLocale(data);

            if (extraValues != null && extraValues.size() > 0) {
                Set keys = getPropertyKeys();
                Iterator it = keys.iterator();
                while (it.hasNext()) {
                    Object key = it.next();
                    data.put(EXTERNAL_PREF + key, extraValues.get(key));
                }   // while
            }

            if (templateValues != null && templateValues.size() > 0) {
                Set keys = templateValues.keySet();
                Iterator it = keys.iterator();
                while (it.hasNext()) {
                    String key = (String)it.next();
                    data.put(TEMPLATE_PREF + key, retrieveTemplateProperty(key));
                }   // while
            }

        }

        for (Iterator iter = allQuestions.values().iterator(); iter.hasNext(); ) {
            Question q = (Question) (iter.next());
            try {
                q.save(data);
            }
            catch (RuntimeException ex) {
                System.err.println("warning: " + ex.toString());
                System.err.println("while saving value for question " + q.getTag() + " in interview " + getTag());
            }
        }

        for (int i = 0; i < children.size(); i++) {
            Interview child = (Interview)children.elementAt(i);
            child.save(data);
        }

        saveMarkers(data);

        //data.put(CHECKSUM, Long.toString(computeChecksum(data), 16));
    }

    /**
     * Writes information about current locale to the given map.
     * <br>
     * This information is used later to properly restore locale-sensitive values,
     * like numerics.
     * @param data target map to write data to
     * @see #LOCALE
     * @see #readLocale(Map)
     */
    protected static void writeLocale(Map data) {
        data.put(LOCALE, Locale.getDefault().toString());
    }

    /**
     * Reads information about locale from the given map. <br>
     * Implementation looks for the string keyed by {@link #LOCALE} and then
     * tries to decode it to valid locale object.
     * @param data map with interview values
     * @return locale, decoded from value taken from map; or default (current) locale
     * @see #LOCALE
     * @see #writeLocale(Map)
     */
    protected static Locale readLocale(Map data) {
        Locale result = null;
        Object o = data.get(LOCALE);
        if (o != null) {
            if (o instanceof Locale) {
                result = (Locale) o;
            } else if (o instanceof String) {
                /* try to decode Locale object from its string representation
                 * @see java.util.Locale#toString()
                 * Examples: "", "en", "de_DE", "_GB", "en_US_WIN", "de__POSIX", "fr__MAC"
                 */
                String s = ((String) o).trim();
                String language = "", country = "", variant = "";
                if (s.length() != 0) {
                    try {
                        // decode language
                        int i = s.indexOf('_');
                        if (i == -1) {
                            // there's no separator in the string. This can be
                            // only the language
                            language = s;
                        } else if (i == 0) {
                            language = "";
                        } else {
                            language = s.substring(0, i);
                        }
                        // now decode country
                        if (i < s.length() - 1) {
                            s = s.substring(i + 1);
                            i = s.indexOf('_');
                            if (i == -1) {
                                // there's no separator in the remaining string.
                                // This is a country
                                country = s;
                            } else if (i == 0) {
                                country = "";
                            } else {
                                country = s.substring(0, i);
                            }
                            // now decode variant
                            if (i < s.length() - 1) {
                                variant = s.substring(i + 1);
                            }
                        }
                        result = new Locale(language, country, variant);
                    } catch (Exception e) {
                        // suppress exception and use default locale
                        result = null;
                    }
                }
            }
        }
        if (result == null) {
            result = Locale.getDefault();
        }
        return result;
    }

    /*
    private static void testReadLocale() {
        String[] samples = new String[] { "", "en", "de_DE", "_GB",
                "en_US_WIN", "de__POSIX", "fr__MAC" };
        Map data = new HashMap(1);
        for (String s : samples) {
            data.put(LOCALE, s);
            System.out.println(s + " -> " + readLocale(data));
            data.clear();
        }
    }

    private static long computeChecksum(Map data) {
        long cs = 0;
        for (Iterator iter = data.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry e = (Map.Entry) (iter.next());
            String key = (String) (e.getKey());
            String value = (String)(e.getValue());
            if (!key.equals(CHECKSUM)) {
                cs += computeChecksum(key) * computeChecksum(value);
            }
        }
        // ensure result is >= 0 to avoid problems with signed hex numbers
        return (cs == Long.MIN_VALUE ? 0 : cs < 0 ? -cs : cs);
    }

    private static long computeChecksum(String s) {
        if (s == null)
            return 1;
        else {
            long cs = 0;
            for (int i = 0; i < s.length(); i++) {
                cs = cs * 37 + s.charAt(i);
            }
            return cs;
        }
    }
    */

    /**
     * Export values for questions on the current path, by calling {@link Question#export}
     * for each question returned by {@link #getPath}.
     * It should  be called on the root interview to export the values for all
     * questions on the current path, or it can be called on a sub-interview
     * to export just the values from the question in that sub-interview (and in turn,
     * in any further sub-interviews for which there are questions on the path.)
     * Unchecked exceptions that arise from each question's export method are treated
     * according to the policy set by setExportIgnoreExceptionPolicy for the interview
     * for which export was called.
     * It may be convenient to ignore runtime exceptions during export, if exceptions
     * may be thrown when the interview is incomplete.  It may be preferred not
     * to ignore any exceptions, if no exceptions are expected.
     * @param data The map in which the values will be placed.
     * @see #getPath
     * @see #setExportIgnoreExceptionPolicy
     * @see #EXPORT_IGNORE_ALL_EXCEPTIONS
     * @see #EXPORT_IGNORE_RUNTIME_EXCEPTIONS
     * @see #EXPORT_IGNORE_NO_EXCEPTIONS
     */
    public void export(Map data) {
        ArrayList<Question> path = new ArrayList();

        // new 4.3 semantics allow the path to contain InterviewQuestions, which
        // in turn allows sub-interviews to export data.
        if (semantics >= SEMANTIC_VERSION_43)
            iteratePath0(path, false, true, true);
        else
            iteratePath0(path, true, true, true);

        export0(data, path, false);

        // note - hiddenPath only used in root interview, null hiddenPaths
        //   are expected in this case
        if (semantics >= SEMANTIC_VERSION_43 && hiddenPath != null)
            export0(data, hiddenPath, true);
    }

    private void export0(Map data, ArrayList<Question> path, boolean processHidden) {
        for (int i = 0; i < path.size(); i++) {
            try {
                if (path.get(i) instanceof InterviewQuestion) {
                    if (!processHidden)
                        ((InterviewQuestion)path.get(i)).getTargetInterview().export(data);
                    else
                        continue;
                }
                else {
                    path.get(i).export(data);
                }
            }
            catch (RuntimeException e) {
                switch (exportIgnoreExceptionPolicy) {
                case EXPORT_IGNORE_ALL_EXCEPTIONS:
                case EXPORT_IGNORE_RUNTIME_EXCEPTIONS:
                    break;
                case EXPORT_IGNORE_NO_EXCEPTIONS:
                    throw e;
                }
            }
            catch (Error e) {
                switch (exportIgnoreExceptionPolicy) {
                case EXPORT_IGNORE_ALL_EXCEPTIONS:
                    break;
                case EXPORT_IGNORE_RUNTIME_EXCEPTIONS:
                case EXPORT_IGNORE_NO_EXCEPTIONS:
                    throw e;
                }
            }
        }
    }

    /**
     * Get a value representing the policy regarding how to treat
     * exceptions that may arise during export.
     * @see #setExportIgnoreExceptionPolicy
     * @return a value representing the policy regarding how to treat
     * exceptions that may arise during export.
     * @see #export
     * @see #EXPORT_IGNORE_ALL_EXCEPTIONS
     * @see #EXPORT_IGNORE_RUNTIME_EXCEPTIONS
     * @see #EXPORT_IGNORE_NO_EXCEPTIONS
     */
    public int getExportIgnoreExceptionPolicy() {
        return exportIgnoreExceptionPolicy;
    }


    /**
     * Set the policy regarding how to treat exceptions that may arise during export.
     * The default value is to ignore runtime exceptions.
     * @param policy a value representing the policy regarding how to treat exceptions that may arise during export
     * @see #getExportIgnoreExceptionPolicy
     * @see #export
     * @see #EXPORT_IGNORE_ALL_EXCEPTIONS
     * @see #EXPORT_IGNORE_RUNTIME_EXCEPTIONS
     * @see #EXPORT_IGNORE_NO_EXCEPTIONS
     */
    public void setExportIgnoreExceptionPolicy(int policy) {
        if (policy < 0 || policy >= EXPORT_NUM_IGNORE_POLICIES)
            throw new IllegalArgumentException();
        exportIgnoreExceptionPolicy = policy;
    }

    /**
     * A value indicating that export should ignore all exceptions that arise
     * while calling each question's export method.
     * @see #setExportIgnoreExceptionPolicy
     * @see #export
     */
    public static final int EXPORT_IGNORE_ALL_EXCEPTIONS = 0;

    /**
     * A value indicating that export should ignore runtime exceptions that arise
     * while calling each question's export method.
     * @see #setExportIgnoreExceptionPolicy
     * @see #export
     */
    public static final int EXPORT_IGNORE_RUNTIME_EXCEPTIONS = 1;

    /**
     * A value indicating that export should not ignore any exceptions that arise
     * while calling each question's export method.
     * @see #setExportIgnoreExceptionPolicy
     * @see #export
     */
    public static final int EXPORT_IGNORE_NO_EXCEPTIONS = 2;
    private static final int EXPORT_NUM_IGNORE_POLICIES = 3;
    private int exportIgnoreExceptionPolicy = EXPORT_IGNORE_RUNTIME_EXCEPTIONS;


    //----- observers ----------------------------------------

    /**
     * Add an observer to monitor updates to the interview.
     * @param o an observer to be notified as changes occur
     */
    synchronized public void addObserver(Observer o) {
        if (o == null)
            throw new NullPointerException();

        // we take the hit here of shuffling arrays to make the
        // notification faster and more convenient (no casting)
        Observer[] newObs = new Observer[observers.length + 1];
        System.arraycopy(observers, 0, newObs, 0, observers.length);
        newObs[observers.length] = o;
        observers = newObs;
    }

    /**
     * Remove an observer previously registered to monitor updates to the interview.
     * @param o the observer to be removed from the list taht are notified
     */
    synchronized public void removeObserver(Observer o) {
        if (o == null)
            throw new NullPointerException();

        // we take the hit here of shuffling arrays to make the
        // notification faster and more convenient (no casting)
        for (int i = 0; i < observers.length; i++) {
            if (observers[i] == o) {
                Observer[] newObs =
                    new Observer[observers.length - 1];
                System.arraycopy(observers, 0, newObs, 0, i);
                System.arraycopy(observers, i + 1, newObs, i, observers.length - i - 1);
                observers = newObs;
                return;
            }
        }
    }

    synchronized public boolean containsObserver(Observer o) {
        if (o == null)
            throw new NullPointerException();

        for (int i = 0; i < observers.length; i++) {
            if (observers[i] == o) {
                return true;
            }
        }

        return false;
    }

    private void notifyCurrentQuestionChanged(Question q) {
        for (int i = 0; i < observers.length && q == getCurrentQuestion(); i++)
            observers[i].currentQuestionChanged(q);
    }

    private void notifyPathUpdated() {
        for (int i = 0; i < observers.length; i++)
            observers[i].pathUpdated();
    }

    private Observer[] observers = new Observer[0];

    //----- tag stuff ----------------------------------------

    /**
     * Change the base tag for this interview.
     * This should not be done for most interviews, since the base tag
     * is the basis for storing loading and storing values, and changing
     * the base tag may lead to unexpected results.
     * Changing the base tag will caused the tags in all statically
     * nested interviews and questions to be updated as well.
     * This method is primarily intended to be used when renaming
     * dynamically allocated loop bodies in ListQuestion.
     * @param newBaseTag the new value for the base tag.
     */
    protected void setBaseTag(String newBaseTag) {
        baseTag = newBaseTag;
        updateTags();
    }

    private void updateTags() {
        // update our own tag
        if (parent == null || parent.tag == null)
            tag = baseTag;
        else if (baseTag == null) // should we allow this?
            tag = parent.getTag();
        else
            tag = parent.getTag() + "." + baseTag;

        // update the tags for the questions in the interview
        // and rebuild the tag map
        Map newAllQuestions = new LinkedHashMap();
        for (Iterator iter = allQuestions.values().iterator(); iter.hasNext(); ) {
            Question q = (Question) (iter.next());
            q.updateTag();
            newAllQuestions.put(q.getTag(), q);
        }
        allQuestions = newAllQuestions;

        // recursively update children
        for (Iterator iter = children.iterator(); iter.hasNext(); ) {
            Interview i = (Interview) (iter.next());
            i.updateTags();
        }
    }

    //----- adding subinterviews and questions ------------------------

    void add(Interview child) {
        children.add(child);
    }

    void add(Question question) {
        String qTag = question.getTag();
        Question prev = (Question) allQuestions.put(qTag, question);
        if (prev != null)
            throw new IllegalArgumentException("duplicate questions for tag: " + qTag);
    }

    //----- versioning ----------------------------------------

    /**
     * This method is being used to toggle changes which are not
     * backwards compatible with existing interviews.  Changing this value
     * after you first initialize the top-level interview object is not
     * recommended or supported.  This method should be called as soon as
     * possible during construction.  It is recommended that you select
     * the most recent version possible when developing a new interview.
     * As this interview ages and the harness libraries progress, the
     * interview should remain locked at the current behavior.
     *
     * @param value Which semantics the interview should use.
     * @see #SEMANTIC_PRE_32
     * @see #SEMANTIC_VERSION_32
     * @see #SEMANTIC_MAX_VERSION
     * @since 3.2
     * @see #getInterviewSemantics
     */
    public void setInterviewSemantics(int value) {
        if (value <= SEMANTIC_MAX_VERSION)
            semantics = value;
    }

    /**
     * Determine which semantics are being used for interview and question
     * behavior.  This is important because new behavior in future versions
     * can cause unanticipated code flow, resulting in incorrect behavior of
     * existing code.
     * @return The semantics that the interview is currently using.
     * @see #setInterviewSemantics
     * @since 3.2
     */
    public int getInterviewSemantics() {
        return semantics;
    }

    //----- external value management ----------------------------------------

    /**
     * Store an "external" value into the configuration.  This is a value
     * not associated with any interview question and in a separate namespace
     * than all the question keys.
     * @param key The name of the key to store.
     * @param value The value associated with the given key.  Null to remove
     *      the property from the current set of properties for this interview.
     * @return The old value of this property, null if not previously set.
     * @see #retrieveProperty
     */
    public String storeProperty(String key, String value) {
        if (getParent() != null)
            return getParent().storeProperty(key, value);
        else {
            if (value == null) {
                // remove
                if (extraValues == null)
                    return null;
                else
                    return (String)(extraValues.remove(key));
            }

            if (extraValues == null)
                extraValues = new HashMap();

            return (String)(extraValues.put(key, value));
        }
    }

    /**
     * Store a template value into the configuration.
     * @param key The name of the key to store.
     * @param value The value associated with the given key.
     * @return The old value of this property, null if not previously set.
     */
    public String storeTemplateProperty(String key, String value) {
        if (getParent() != null)
            return getParent().storeTemplateProperty(key, value);
        else {
            ensureTemValuesInitialized();
            return (String)(templateValues.put(key, value));
        }
    }

    /**
     * Clear a previous template properties and store the new into the configuration.
     * @param props The properties to store.
     */
    public void storeTemplateProperties(Properties props) {
        if (getParent() != null)
            getParent().storeTemplateProperties(props);
        else {
            ensureTemValuesInitialized();
            templateValues.clear();
            templateValues.putAll((Map) props);
        }
    }


    /**
     * Retrieve a property from the collection of "external" values being
     * stored in the configuration.
     * @param key The key which identifies the property to retrieve.
     * @return The value associated with the given key, or null if it is not
     *         found.
     * @see #storeProperty
     */
    public String retrieveProperty(String key) {
        if (getParent() != null)
            return getParent().retrieveProperty(key);
        else {
            if (extraValues == null)
                return null;

            return (String)(extraValues.get(key));
        }
    }

    /**
     * Retrieve a template property.
     * @param key The key which identifies the property to retrieve.
     * @return The value associated with the given key, or null if it is not
     *         found.
     */
    public String retrieveTemplateProperty(String key) {
        if (getParent() != null)
            return getParent().retrieveTemplateProperty(key);
        else {
            ensureTemValuesInitialized();
            return (String)(templateValues.get(key));
        }
    }

    public Set retrieveTemplateKeys() {
        if (getParent() != null)
            return getParent().retrieveTemplateKeys();
        else {
            ensureTemValuesInitialized();
            return templateValues.keySet();
        }
    }



    /**
     * Retrieve set of keys for the "external" values being stored in the
     * configuration.
     * @return The set of keys currently available.  Null if there are none.
     *         All values in the Set are Strings.
     * @see #storeProperty
     * @see #retrieveProperty
     */
    public Set getPropertyKeys() {
        if (getParent() != null)
            return parent.getPropertyKeys();
        else {
            if (extraValues == null || extraValues.size() == 0)
                return null;

            return extraValues.keySet();
        }
    }

    /**
     * Get a (shallow) copy of the current "external" values.
     * @see #storeProperty
     * @see #retrieveProperty
     * @return The copy of the properties, null if there are none.
     */
    public Map<String,String> getExternalProperties() {
        if (extraValues != null)
            return (Map)extraValues.clone();
        else
            return null;
    }


    /**
     * @see #load(Map)
     */
    private void loadExternalValues(Map data) {

        if (extraValues != null) {
            extraValues.clear();
        }

        Set keys = data.keySet();
        Iterator it = keys.iterator();

        while (it.hasNext()) {
            // look for special external value keys
            // should consider removing it from data, is it safe to alter
            // that object?
            String key = (String)(it.next());
            if (key.startsWith(EXTERNAL_PREF)) {
                if (extraValues == null)
                    extraValues = new HashMap();

                String val = (String)(data.get(key));

                // store it, minus the special prefix
                extraValues.put(key.substring(EXTERNAL_PREF.length()), val);
            }
        }   // while
    }

    private void loadTemplateValues(Map data) {

        if (templateValues != null) {
            templateValues.clear();
        }

        Set keys = data.keySet();
        Iterator it = keys.iterator();

        while (it.hasNext()) {
            String key = (String)(it.next());
            if (key.startsWith(TEMPLATE_PREF)) {
                ensureTemValuesInitialized();
                String val = (String)(data.get(key));
                // store it, minus the special prefix
                templateValues.put(key.substring(TEMPLATE_PREF.length()), val);
            }
        }
    }

    public void propagateTemplateForAll() {
        ensureTemValuesInitialized();
        for (Iterator iter = getAllQuestions().values().iterator(); iter.hasNext(); ) {
            Question q = (Question) (iter.next());
                q.load(templateValues);
        }
    }

    //----- internal utilities ----------------------------------------

    private void ensureTemValuesInitialized() {
        if (templateValues  == null) {
            templateValues  = new HashMap();
        }
    }

    private void ensurePathInitialized() {
        if (path == null) {
            path = new Path();
            hiddenPath = new ArrayList();

            if (parent == null)
                rawPath = new Path();

            reset();
        }

        if (parent == null && rawPath == null)
            rawPath = new Path();
    }


    private Question lookup(String tag) {
        Question q = (Question)(allQuestions.get(tag));
        // if q is null, search children till we find it
        for (int i = 0; i < children.size() && q == null; i++) {
            Interview child = (Interview)children.elementAt(i);
            q = child.lookup(tag);
        }

        return q;
    }

    /**
     * Determine if this is the root interview.
     * @return True if this is the root interview.
     */
    public boolean isRoot() {
        return parent == null;
    }


    /**
     * Get the root interview object for an interview series.
     * Some parts of the data are associated only with the root interview, such
     * as tags, external values and the {@link Interview#getRawPath} information.
     */
    public Interview getRoot() {
        /*
        Interview i = this;
        while (i.root != this)
            i = i.parent;
        return i;
        */
        return root;
    }

    /**
     * Update the current path, typically because a response to
     * a question has changed.
     */
    public void updatePath() {
        root.updatePath0(root.firstQuestion);
    }

    /**
     * Update the current path, typically because a response to
     * a question has changed.
     * @param q The question that was changed.
     */
    public void updatePath(Question q) {
       root.updatePath0(q);
    }

    private void updatePath0(Question q) {
        ASSERT(root == this);

        if (!updateEnabled) {
            // avoid frequent updates during load
            return;
        }

        if (path == null) {
            // path has not been initialized yet, so no need to update it
            return;
        }

        // version 4.3 and later allow path reevaluation even if the question
        // isn't an active question (probably disabled)
        if (semantics < SEMANTIC_VERSION_43 && !pathContains(q)) {
            return;
        }

        // keep a copy of the current path so that if the current
        // question is no longer on the path at the end of the update
        // we can adjust it as best we can.
        Question[] currPath = getPathToCurrent();

        trimPath(q);
        predictPath(q);
        //showPath(this, q, 0);

        if (!pathContains(currPath[currPath.length - 1]))
            setCurrentQuestionFromPath(currPath);

        notifyPathUpdated();
    }

    /* useful debug routine
    private void showPath(Interview i, Question q, int depth) {
        for (int d = 0; d < depth; d++)
            System.err.print("  ");
        System.err.println(i.getClass().getName() + " " + i.getTag());
        for (int p = 0; p < i.path.size(); p++) {
            for (int d = 0; d < depth; d++)
                System.err.print("  ");
            Question pq = i.path.questionAt(p);
            System.err.print(p + ": " + pq.getClass().getName() + " " + pq.getTag());
            if (pq == q)
                System.err.print(" *");
            System.err.println();
            if (pq instanceof InterviewQuestion)
                showPath(((InterviewQuestion)pq).getTargetInterview(), q, depth+1);
        }
    }
    */

    private void trimPath(Question q) {
        Object o = q;
        Interview i = q.getInterview();
        while (i != null) {
            // try to find o within i's path
            i.ensurePathInitialized();
            Path iPath = i.path;
            int oIndex = -1;
            for (int pi = 0; pi < iPath.size(); pi++) {
                Question qq = iPath.questionAt(pi);
                if (qq == o
                    || (qq instanceof InterviewQuestion
                        && ((InterviewQuestion) qq).getTargetInterview() == o)) {
                    oIndex = pi;
                    break;
                }
            }
            // if not found, this question is not on path
            // otherwise, trim i's path to end with o
            if (oIndex == -1)
                return;
            else
                iPath.setSize(oIndex + 1);

            // repeat with caller, all the way up the call stack
            o = i;
            i = (i.caller == null ? null : i.caller.getInterview());
        }
    }

     private void predictPath(Question q) {
        // start filling out path
        Interview i = q.getInterview();
        i.ensurePathInitialized();
        q = predictNext(q);
        while (true) {
            // note: multiple exit conditions within loop body
            if (q == null || pathContains(q))
                break;
            else if (q instanceof FinalQuestion) {
                // end of an interview; continue in caller if available
                i.path.addQuestion(q);
                i.root.rawPath.addQuestion(q);
                if (i.caller == null) {
                    break;
                }
                else {
                    q = i.caller.getNext();
                    i = i.caller.getInterview();
                }
            }
            else if (q instanceof InterviewQuestion) {
                InterviewQuestion iq = (InterviewQuestion)q;
                Interview i2 = iq.getTargetInterview();
                if (pathContains(i2))
                    break;
                else if (i2 instanceof ListQuestion.Body) {
                    // no need to predict the body, right?
                    // because it was done in predictNext()
                    i2.caller = iq;
                    i.path.addQuestion(iq);
                    i.root.rawPath.addQuestion(iq);
                    q = (i2.path.lastQuestion() instanceof FinalQuestion ? iq.getNext() : null);
                }
                else {
                    i2.caller = iq;
                    if (i2.path == null) {
                        i2.path = new Path();
                        i2.hiddenPath = new ArrayList();
                    }
                    else {
                        i2.path.clear();
                        i2.hiddenPath.clear();
                    }

                    i.path.addQuestion(iq);
                    i.root.rawPath.addQuestion(iq);
                    i = i2;
                    q = i2.firstQuestion;
                }
            }
            else {
                if (q.isEnabled())
                    i.path.addQuestion(q);
                else if (q.isHidden() && !root.hiddenPath.contains(q))
                    root.hiddenPath.add(q);

                if (root.rawPath.indexOf(q) == -1)
                    i.root.rawPath.addQuestion(q);
                q = predictNext(q);
            }
        }
    }

    private Question predictNext(Question q) {
        if (q.isEnabled() && !q.isValueValid())
            return null;

        if (q instanceof ListQuestion && q.isEnabled()) {
            final ListQuestion lq = (ListQuestion) q;
            if (lq.isEnd())
                return q.getNext();

            for (int index = 0; index < lq.getBodyCount(); index++) {
                Interview b = lq.getBody(index);
                if (b.path == null) {
                    b.path = new Path();
                }
                else {
                    b.path.clear();
                }

                b.path.addQuestion(b.firstQuestion);
                b.caller = null;
                b.predictPath(b.firstQuestion);
            }

            Interview lqBody = lq.getSelectedBody();
            Question lqOther = lq.getOther();
            if (lqBody == null)
                return lqOther.getNext();
            else {
                Interview lqInt = lq.getInterview();
                return new InterviewQuestion(lqInt, lqBody, lqOther);
            }
        }

        return q.getNext();
    }

    /**
     * Get an entry from the resource bundle.
     * If the resource cannot be found, a message is printed to the console
     * and the result will be a string containing the method parameters.
     * @param key the name of the entry to be returned
     * {@link java.text.MessageFormat#format}
     * @return the formatted string
     */
    private String getI18NString(String key) {
        return getI18NString(key, empty);
    }

    private static final Object[] empty = { };

    /**
     * Get an entry from the resource bundle.
     * If the resource cannot be found, a message is printed to the console
     * and the result will be a string containing the method parameters.
     * @param key the name of the entry to be returned
     * @param arg an argument to be formatted into the result using
     * {@link java.text.MessageFormat#format}
     * @return the formatted string
     */
    private String getI18NString(String key, Object arg) {
        return getI18NString(key, new Object[] { arg });
    }

    /**
     * Get an entry from the resource bundle.
     * If the resource cannot be found, a message is printed to the console
     * and the result will be a string containing the method parameters.
     * @param key the name of the entry to be returned
     * @param args an array of arguments to be formatted into the result using
     * {@link java.text.MessageFormat#format}
     * @return the formatted string
     */
    private String getI18NString(String key, Object[] args) {
        try {
            ResourceBundle b = getResourceBundle();
            if (b != null)
                return MessageFormat.format(b.getString(key), args);
        }
        catch (MissingResourceException e) {
            // should msgs like this be i18n and optional?
            System.err.println("WARNING: missing resource: " + key);
        }

        StringBuffer sb = new StringBuffer(key);
        for (int i = 0; i < args.length; i++) {
            sb.append('\n');
            sb.append(Arrays.toString(args));
        }
        return sb.toString();
    }

    /**
     * Get an entry from the resource bundle.
     * The parent and other ancestors bundles will be checked first before
     * this interview's bundle, allowing the root interview a chance to override
     * the default value provided by this interview.
     * @param key the name of the entry to be returned
     * @return the value of the resource, or null if not found
     */
    protected String getResourceString(String key) {
        return getResourceString(key, true);
    }

    /**
     * Get an entry from the resource bundle. If checkAncestorsFirst is true,
     * then the parent and other ancestors bundles will be checked first before
     * this interview's bundle, allowing the root interview a chance to override
     * the default value provided by this interview. Otherwise, the parent bundles
     * will only be checked if this bundle does not provide a value.
     * @param key the name of the entry to be returned
     * @param checkAncestorsFirst whether to recursively call this method on the
     * parent (if any) before checking this bundle, or only afterwards, if this
     * bundle does not provide a value
     * @return the value of the resource, or null if not found
     */
    protected String getResourceString(String key, boolean checkAncestorsFirst) {
        try {
            String s = null;
            if (checkAncestorsFirst) {
                if (parent != null)
                    s = parent.getResourceString(key, checkAncestorsFirst);
                if (s == null) {
                    ResourceBundle b = getResourceBundle();
                    if (b != null)
                        s = b.getString(key);
                }
            }
            else {
                ResourceBundle b = getResourceBundle();
                if (b != null)
                    s = b.getString(key);
                if (s == null && parent != null)
                    s = parent.getResourceString(key, checkAncestorsFirst);
            }
            return s;
        }
        catch (MissingResourceException e) {
            return null;
        }
    }

    // can change this to "assert(b)" in JDK 1.5
    private static final void ASSERT(boolean b) {
        if (!b)
            throw new IllegalStateException();
    }

    /**
     * The parent interview, if applicable; otherwise null.
     */
    private final Interview parent;

    /**
     * The root (most parent) interview; never null
     */
    private final Interview root;

    private String baseTag; // tag relative to parent

    private String tag; // full tag: parent tag + baseTag

    /**
     * A descriptive title for the interview.
     */
    private String title;

    /**
     * The first question of the interview.
     */
    private Question firstQuestion;


    /**
     * Any child interviews.
     */
    private Vector children = new Vector();

    /**
     * An index of the questions in this interview.
     */
    private Map allQuestions = new LinkedHashMap();

    /**
     * The default image for questions in the interview.
     */
    private URL defaultImage;

    private Object helpSet;

    // object to create HelpSet and Help ID
    // in batch mode, this factory should return stubs.
    protected final static HelpSetFactory helpSetFactory = createHelpFactory();
    private String bundleName;
    private ResourceBundle bundle;

    private Path path;
    private Path rawPath;
    private ArrayList<Question> hiddenPath;
    private int currIndex;
    private InterviewQuestion caller;
    private boolean updateEnabled;
    private boolean edited;

    private Map allMarkers;
    private HashMap<String,String> extraValues;        // used in top-level interview only
    private HashMap<String,String> templateValues;

    private int semantics = SEMANTIC_PRE_32;

    static final ResourceBundle i18n = ResourceBundle.getBundle("com.sun.interview.i18n");

    /**
     * Where necessary, the harness interview should behave as it did before the
     * 3.2 release.  This does not control every single possible change in
     * behavior, but does control certain behaviors which may cause problems with
     * interview code written against an earlier version of the harness.
     * @see #setInterviewSemantics
     */
    public static final int SEMANTIC_PRE_32 = 0;

    /**
     *
     * Where necessary, the harness interview should behave as it did for the
     * 3.2 release.  This does not control every single possible change in
     * behavior, but does control certain behaviors which may cause problems with
     * interview code written against an earlier version of the harness.
     * @see #setInterviewSemantics
     */
    public static final int SEMANTIC_VERSION_32 = 1;

    /**
     *
     * Where necessary, the harness interview should behave as it did for the
     * 4.3 release.  This does not control every single possible change in
     * behavior, but does control certain behaviors which may cause problems with
     * interview code written against an earlier version of the harness.
     *
     *
     * @see #setInterviewSemantics
     */
    public static final int SEMANTIC_VERSION_43 = 2;

    /**
     * The highest version number currently in use.  Note that the compiler
     * will probably inline this during compilation, so you will be locked at
     * the version which you compile against.  This is probably a useful
     * behavior in this case.
     * @see #setInterviewSemantics
     */
    public static final int SEMANTIC_MAX_VERSION = 2;

    static class Path {
        void addQuestion(Question q) {
            if (questions == null)
                questions = new Question[10];
            else if (numQuestions == questions.length) {
                Question[] newQuestions = new Question[2 * questions.length];
                System.arraycopy(questions, 0, newQuestions, 0, questions.length);
                questions = newQuestions;
            }

            questions[numQuestions++] = q;
        }

        Question questionAt(int index) throws ArrayIndexOutOfBoundsException {
            if (index < 0 || index >= numQuestions)
                throw new ArrayIndexOutOfBoundsException();
            return questions[index];
        }

        Question lastQuestion() {
            return questionAt(numQuestions - 1);
        }

        // return shallow copy
        Question[] getQuestions() {
            if (questions == null)
                return null;

            Question[] copy = new Question[questions.length];
            System.arraycopy(questions, 0, copy, 0, questions.length);
            return copy;
        }

        int indexOf(Interview interview) {
            for (int index = 0; index < numQuestions; index++) {
                Question q = questions[index];
                if (q instanceof InterviewQuestion
                    && ((InterviewQuestion) q).getTargetInterview() == interview)
                    return index;
            }
            return -1;
        }

        int indexOf(Question target) {
            for (int index = 0; index < numQuestions; index++) {
                Question q = questions[index];
                if (q == target)
                    return index;
            }
            return -1;
        }

        int size() {
            return numQuestions;
        }

        void setSize(int newSize) {
            // expected case is only to shrink size, so questions != null && newSize < questions.length
            if (questions != null) {
                if (newSize > questions.length) {
                    Question[] newQuestions = new Question[newSize];
                    System.arraycopy(questions, 0, newQuestions, 0, questions.length);
                    questions = newQuestions;

                }
                for (int i = newSize; i < numQuestions; i++)
                    questions[i] = null;
            }
            else if (newSize > 0)
                questions = new Question[newSize];

            numQuestions = newSize;
        }


        void clear() {
            for (int i = 0; i < numQuestions; i++)
                questions[i] = null;
            numQuestions = 0;
        }

        private Question[] questions;
        private int numQuestions;
    }

    protected final static String QUESTION = "QUESTION";
    protected final static String INTERVIEW = "INTERVIEW";
    protected final static String LOCALE = "LOCALE";
    //protected final static String CHECKSUM = "CHECKSUM";
    protected final static String MARKERS = "MARKERS";
    protected final static String MARKERS_PREF = "MARKERS.";
    protected static final String EXTERNAL_PREF = "EXTERNAL.";
    protected static final String TEMPLATE_PREF = "TEMPLATE.";

}
