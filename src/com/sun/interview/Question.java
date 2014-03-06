/*
 * $Id$
 *
 * Copyright (c) 1996, 2012, Oracle and/or its affiliates. All rights reserved.
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

import java.net.URL;
import java.text.MessageFormat;
import java.util.Map;

/**
 * Questions are the primary constituent elements of {@link Interview interviews}.
 * They provide text and an optional graphic to be presented to the user,
 * and they provide a place to store the user's response.
 * Various subtypes are provided, according to the type of response they
 * request and store.
 * Questions are identified internally by a unique tag, which is used to identify
 * the question in contexts such as resource bundles, dictionaries, and so on.
 */
public abstract class Question
{

    /**
     * Create a question with a nominated tag.
     * @param interview The interview containing this question.
     * @param baseTag A name to uniquely identify this question within its interview.
     */
    protected Question(Interview interview, String baseTag) {
        if (baseTag == null || baseTag.length() == 0)
            throw new IllegalArgumentException("No tag specified");

        this.interview = interview;
        this.baseTag = baseTag;

        updateTag();

        interview.add(this);

        String c = interview.getClass().getName();
        int dot = c.lastIndexOf(".");
        if (dot != -1)
            c = c.substring(dot+1);
        c = c.replace('$', '.');
        key = c + "." + baseTag;
    }

    /**
     * Create a question with no identifying tag.
     * @param interview The interview containing this question.
     */
    protected Question(Interview interview) {
        this.interview = interview;
        baseTag = null;
        tag = null;
        key = null;
    }

    /**
     * Return the interview of which this question is a part.
     * @return the interview of which this question is a part
     */
    public Interview getInterview() {
        return interview;
    }

    /**
     * Get the <em>key</em> for a question. The key is the class name of the parent
     * interview followed by a period followed by the tag given to the constructor.
     * It therefore reasonably identifies the question relative to the class of
     * its parent interview. The key is normally used to identify resources
     * such as the question text in a resource file or help information in a
     * help set.
     * @return the key for this question
     * @see #getTag
     */
    public String getKey() {
        return key;
    }

    /**
     * Get the <em>tag</em> for a question. The tag is the tag of the parent
     * interview, followed by a dot, followed by the tag given to the constructor.
     * It therefore reasonably identifies the question relative to the instance of
     * its parent interview. The tag is normally used to identify instance-specific
     * responses in a saved interview file.
     * @return the tag for this question
     * @see #getKey
     */
    public String getTag() {
        return tag;
    }

    String getBaseTag() {
        return baseTag;
    }

    void updateTag() {
        if (interview.getTag() == null)
            tag = baseTag;
        else
            tag = (interview.getTag() + "." + baseTag);
    }

    /**
     * Set the text for this question. The text can also be provided
     * in the containing interview's resource bundle, using the resource name
     * <i>key</i>.<code>text</code>.
     * @param text the text for this question
     * @see #getText
     */
    protected void setText(String text) {
        this.text = text.trim();
    }

    /**
     * Get the text for this question. If not specified, the system will
     * try and locate the text in the containing interviews resource bundle,
     * using the resource name <i>key</i><code>.text</code>.
     * @return the text for this question
     * @see #setText
     */
    public String getText() {
        if (text == null) {
            text = getResourceString(interview, key + ".text", true);
            if (text == null)
                text = key + ".text";
        }
        return MessageFormat.format(text, getTextArgs());
    }

    /**
     * Get any arguments necessary to go with the question text.
     * The text is formatted using the rules specified for
     * {@link java.text.MessageFormat#format}. By default, this
     * method returns null. If the text for the question contains
     * variable entries (using the '{' '}' notation), you should
     * override this method to provide the corresponding values.
     * @return An array of objects suitable for formatting in the text
     * of the question.
     */
    protected Object[] getTextArgs() {
        return null;
    }

    /**
     * Set the summary text for this question. The text can also be provided
     * in the containing interview's resource bundle, using the resource name
     * <i>key</i>.<code>smry</code>.
     * @param summary a short summary of the text for this question
     * @see #getSummary
     */
    protected void setSummary(String summary) {
        this.summary = summary.trim();
    }

    /**
     * Set the summary text for this question.
     * @return a short summary of the text for this question
     * @see #setSummary
     */
    public String getSummary() {
        if (summary == null) {
            summary = getResourceString(interview, key + ".smry", true);
            if (summary == null)
                summary = key + ".smry";
            /* OLD, undocumented and mildly suspect (e.g. i18n)
            if (summary == null) {
                // search text for either a newline, or for whitespace
                // following a .
                String t = getText();
                if (t != null) {
                    for (int i = 0; i < t.length(); i++) {
                        char c = t.charAt(i);
                        if (c == '\n'
                            || (Character.isWhitespace(c)
                                && i > 0
                                && t.charAt(i-1) == '.' )) {
                            summary = t.substring(0, i).trim();
                            break;
                        }
                    }
                    if (summary == null)
                        summary = t;
                }
            }
            */
        }
        return summary;
    }

    /**
     * Set the URL for a specific graphic for this question.
     * @param resource The name of a resource containing the
     * desired image.
     * @see #getImage
     */
    public void setImage(String resource) {
        image = Question.class.getResource(resource);
    }

    /**
     * Set the URL for a specific graphic for this question.
     * @param u The URL of a resource containing the
     * desired image.
     * @see #getImage
     */
    protected void setImage(URL u) {
        image = u;
    }

    /**
     * Get the graphic for this question. If {@link #setImage}
     * has been called to supply a specific image for this question,
     * that will be the result here; otherwise, the value defaults
     * first to a question-specific resource (<i>tag</i><code>.gif</code>)
     * and then to a {@link Interview#getDefaultImage default image}
     * for the interview.
     * @return a URL for the question.
     * @see #setImage
     */
    public URL getImage() {
        if (image == null)
            image = getClass().getResource(tag + ".gif");
        if (image == null)
            image = interview.getDefaultImage();
        return image;
    }

    /**
     * Get the JavaHelp ID identifying the "more info" help for this
     * question, or null if none.
     *
     * Normally, this method returns null and real work on JavaHelp ID is done
     * by com.sun.interview.wizard.Help class, if wizard presents.
     * Subclasses might override this method to return not null value,
     * in this case returned value will be used. Returned object must be
     * an instance of javax.help.Map.ID.
     *
     * @return the JavaHelp ID identifying the "more info" help for this
     * question, or null if none.
     */
    public Object getHelpID() {
        return helpID;
    }

    /**
     * Set HelpID object associated with the Question.
     *
     * @param object Should be instance of javax.help.Map.ID
     *
     */
    public void setHelpID(Object object) {
        helpID = object;
    }

    /**
     * Get any items which should be added to the interview's
     * checklist.
     * @return any items which should be added to the interview's
     * checklist, or null if none.
     * @see Interview#createChecklist
     */
    public Checklist.Item[] getChecklistItems() {
        return null;
    }

    /**
     * Add a named marker to this question, if it has not already been added.
     * @param name the name of the marker to be added.
     * @throws NullPointerException if name is null
     */
    public void addMarker(String name) {
        interview.addMarker(this, name);
    }

    /**
     * Remove a named marker to this question.
     * @param name the name of the marker to be removed.
     */
    public void removeMarker(String name) {
        interview.removeMarker(this, name);
    }

    /**
     * Check if a marker has been added to this question.
     * @param name the name of the marker to be checked.
     * @return true if the marker has been added to this question, and false otherwise.
     */
    public boolean hasMarker(String name) {
        return interview.hasMarker(this, name);
    }


    /**
     * Determine if this question is currently "hidden".
     * Hidden questions do not appear on the current path
     * and so are skipped over by {@link Interview#prev} and
     * {@link Interview#next}, etc.
     * By default, questions are not hidden, and this method returns false.
     * Override this method if you want to hide a question - note that the
     * value of {@link #isEnabled} is currently tied to the value of
     * this method.  Developers who wish to independently play with hiding and
     * enabling should override both methods.
     * @since 4.3
     * @return true if this question has been hidden.
     * @see Question#isEnabled
     */
    public boolean isHidden() {
        return false;
    }


    /**
     * Determine if this question is currently enabled.
     * Disabled questions do not appear on the current path
     * and so are skipped over by {@link Interview#prev} and
     * {@link Interview#next}, etc.
     * By default, all questions are enabled, and this method returns true.
     * Override this method if you want to hide a question.
     * It can be convenient to determine whether or not a question should be
     * disabled by using the values of earlier questions.
     * <em>Note:</em> for backwards compatibility, the default implementation
     * returns <code>!isHidden()</code>.
     * @return true if this question should appear on the current path,
     * and false otherwise.
     */
    public boolean isEnabled() {
        return (!isHidden());
    }

    /**
     * Get the next question to be asked.
     * @return the next question to be asked
     */
    protected abstract Question getNext();

    /**
     * Clear any state for this question, setting the value
     * to a question-specific default value. Most question
     * types also provide a way to set the default value.
     */
    public abstract void clear();

    /**
     * Get the response to this question as a string.
     * @return a string representing the current response to this question, or null.
     * @see #setValue(String)
     */
    public abstract String getStringValue();

    /**
     * Set the response to this question to the value represented by
     * a string-valued argument. Subtypes of Question will typically
     * have type-specific methods to set the value as well.
     * @param s A string containing a value value appropriate for the
     * particular type of question whose value is being set.
     * @throws Interview.Fault (retained for compatibility; should not be thrown)
     * @see #getStringValue
     */
    public abstract void setValue(String s) throws Interview.Fault;

    /**
     * Check if the question currently has a valid response.
     * @return true if the question currently has a valid response,
     * and false otherwise.
     **/
    public abstract boolean isValueValid();

    /**
     * Check if the question always has a valid response.
     * This may be true, for example, for a choice question with a default response.
     * @return true if the question always has a valid response,
     * and false otherwise.
     **/
    public abstract boolean isValueAlwaysValid();

    /**
     * Load any state for this question from a dictionary,
     * using the tag as the key.
     * @param data The map from which to load the value for this question.
     */
    protected abstract void load(Map data);

    /**
     * Save any state for this question in a dictionary,
     * using the tag as the key.
     * @param data The map in which to save the value for this question.
     */
    protected abstract void save(Map data);

    /**
     * Export any appropriate values to the dictionary, as part
     * of {@link Interview#export}. The default is to do nothing.
     * Note that only questions which are on the current path have
     * their export method called. Questions do not appear on the
     * current path if they are not accessible from any question on the
     * path, or if they have been disabled.
     * @param data The map in which to export any data for this question.
     * @see Interview#getPath
     * @see #isEnabled
     */
    protected void export(Map data) {
    }

    /**
     * This methods invokes save(Map), clear() and load(Map).
     * These actions will lead to the default value (if set) will become
     * the question value.
     */
    public void reload() {
        Map map = new java.util.HashMap();
        save(map);
        clear();
        load(map);
    }

    /**
     * Check if this question equals another.
     * By default, two questions are equal if they have equal tags,
     * and the string values are either both null
     * or are equal.
     */
    public boolean equals(Object other) {
        if (this == other)
            return true;

        if ( !(other instanceof Question))
            return false;

        Question oq = (Question) other;

        return (equal(tag, oq.tag) && equal(getStringValue(), oq.getStringValue()));
    }

    private static boolean equal(String s1, String s2) {
        return (s1 == null || s2 == null ? s1 == s2 : s1.equals(s2));
    }

    public int hashCode()  {
        int hash = 7;
        hash = 31 * hash + (null == tag ? 0 : tag.hashCode());
        String str_val = getStringValue();
        hash = 31 * hash + (null == str_val ? 0 : str_val.hashCode());

        return hash;
    }
    /**
     * Get an entry from the interview's resource bundle.
     * The parent and other ancestors bundles will be checked first before
     * this interview's bundle, allowing the root interview a chance to override
     * the default value provided by this interview.
     * @param key the name of the entry to be returned
     * @return the value of the resource, or null if not found
     * @see Interview#getResourceString(String)
     */
    protected String getResourceString(String key) {
        return interview.getResourceString(key);
    }

    /**
     * Get an entry from the interview's resource bundle. If checkAncestorsFirst is true,
     * then the parent and other ancestor interviews' bundles will be checked first before
     * this interview's bundle, allowing the root interview a chance to override
     * the default value provided by this interview. Otherwise, the parent bundles
     * will only be checked if this bundle does not provide a value.
     * @param key the name of the entry to be returned
     * @param checkAncestorsFirst whether to recursively call this method on the
     * parent (if any) before checking this bundle, or only afterwards, if this
     * bundle does not provide a value
     * @return the value of the resource, or null if not found
     * @see Interview#getResourceString(String, boolean)
     */
    protected String getResourceString(String key, boolean checkAncestorsFirst) {
        return interview.getResourceString(key, checkAncestorsFirst);
    }

    private String getResourceString(Interview interview, String key, boolean checkAncestorsFirst) {
        return interview.getResourceString(key, checkAncestorsFirst);
        /*
         * the following code is now in Interview.getResourceString
        try {
            //System.err.println("QU: " + interview.getTag() + " " + key);
            String s = null;
            Interview p = interview.getParent();
            if (checkAncestorsFirst) {
                if (p != null)
                    s = getResourceString(p, key, checkAncestorsFirst);
                if (s == null) {
                    ResourceBundle b = interview.getResourceBundle();
                    if (b != null)
                        s = b.getString(key);
                }
            }
            else {
                ResourceBundle b = interview.getResourceBundle();
                if (b != null)
                    s = b.getString(key);
                if (s == null && p != null)
                    s = getResourceString(p, key, checkAncestorsFirst);
            }
            //System.err.println("QU: " + interview.getTag() + " " + key + " " + s);
            return s;
        }
        catch (MissingResourceException e) {
            //System.err.println("QU: " + interview.getTag() + " " + key + " -null-");
            return null;
        }
        */
    }

    /**
     * The interview to which this question belongs.
     */
    protected Interview interview;

    /**
     * A unique tag to identify this question.
     */
    protected String tag; // parent tag + baseTag

    private String baseTag; // tag relative to parent

    /**
     * A unique key to identify the resources for this question.
     */
    protected final String key;

    /**
     * The text of this question.
     */
    private String text;

    /**
     * The summary of this question.
     */
    private String summary;

    /**
     */
    private Object helpID;

    /**
     * A URL for a graphic for this question.
     */
    private URL image;
}
