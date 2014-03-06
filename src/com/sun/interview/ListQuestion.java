/*
 * $Id$
 *
 * Copyright (c) 2001, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.text.MessageFormat;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * A {@link Question question} to support the construction of an
 * open-ended set of complex values determined by a specified subinterview.
 *
 * <p>A "loop" is created by creating an instance of a subtype of ListQuestion.
 * The subtype must implement createBody() to create instances of the subinterview
 * for the body of the loop. getNext() should return the next question after the
 * loop has been completed.
 *
 * <p>Computationally, this question behaves more like a "fork" than a "loop".
 * Semantically, it is as though all the bodies are evaluated together,
 * in parallel, rather than serially one after the other.
 * In the  GUI presentation, it is expected that only one body is displayed
 * at a time, and that the user can choose which body is viewed.
 * This avoids having all the loops unrolled all the time in the display of
 * the current path.
 * Internally, each ListQuestion has a sibling that is created automatically,
 * and together, these two questions bracket the set of loop bodies.
 */
public abstract class ListQuestion extends Question
{
    /**
     * A special subtype of Interview to use for the questions in the body of
     * a loop. The body has an index, which identifies its position within
     * the list of current loop bodies, and a summary string to identify
     * this instance of the loop body.
     */
    public static abstract class Body extends Interview {
        /**
         * Create an instance of a loop body.
         * @param question The loop question for which this is a body instance.
         * @param index The position of this body within the set of all the bodies.
         * The value is normally just a hint (albeit a possibly string one).
         * The index will be updated if necessary when the body is actually
         * set as one of the bodies of the loop.
         */
        protected Body(ListQuestion question, int index) {
            super(question.getInterview(),
                  question.getBaseTag() + "." + index);
            this.question = question;
            this.index = index;
        }

        /**
         * Get a string to uniquely identify this instance of the loop body,
         * or null if there is insufficient information so far to make a
         * determination. The string will be used to identify the loop body
         * to the user.
         * @return a string to uniquely identify this instance of the loop body,
         * or null if there is insufficient information so far to make a
         * determination.
         */
        public abstract String getSummary();

        /**
         * Get the position of this loop body within the set of all the loop
         * bodies for the question.
         * @return the position of this loop body within the set of all the loop
         * bodies for the question
         */
        public int getIndex() {
            return index;
        }

        /**
         * Set the recorded position of this loop body within the set
         * of all the loop bodies for the question. By itself, this method
         * does not actually affect the loop bodies.
         * See {@link ListQuestion#setBodies} for details on updating the
         * bodies of the loop.
         * @param newIndex the new position of this loop body within the
         * set of all the loop bodies for the question
         */
        void setIndex(int newIndex) {
            if (newIndex != index) {
                index = newIndex;
                setBaseTag(question.getBaseTag() + "." + index);
            }
        }

        /**
         * Get a default summary to be used to identify this instance of the
         * the loop body, to be used when getSummary() returns null.
         * The summary will be a standard prefix string possibly followed
         * by a number to distinguish between multiple bodies using the
         * default summary. The default summary will be unique and persist
         * for the life of this body or until getSummary() returns a non-null
         * value.
         * @return a default summary to be used to identify this instance of the
         * the loop body, to be used when getSummary() returns null.
         */
        public String getDefaultSummary() {
            if (defaultSummary == null) {
                // recycle any default summaries that are no longer required
                Vector bodies = question.bodies;
                for (int i = 0; i < bodies.size(); i++) {
                    Body b = (Body) (bodies.elementAt(i));
                    if (b.defaultSummary != null
                        && b.getSummary() != null
                        && !b.defaultSummary.equals(b.getSummary())) {
                        b.defaultSummary = null;
                    }
                }

                // try and find an unused unique value v not used by any other default summary
                for (int v = 0; v < bodies.size(); v++) {
                    String s = MessageFormat.format(i18n.getString("lp.newValue"),
                                                    new Object[] { new Integer(v) });
                    // check s is not the same as any current default summary;
                    // if it is, reset it to null
                    for (int i = 0; i < bodies.size(); i++) {
                        Body b = (Body) (bodies.elementAt(i));
                        if (s.equals(b.defaultSummary)) {
                            s = null;
                            break;
                        }
                    }
                    // if s is not null, it is unique, different from other default
                    // summaries, so use it...
                    if (s != null) {
                        defaultSummary = s;
                        break;
                    }
                }
            }

            return defaultSummary;
        }

        /**
         * Check if this body has been completed. It is considered to have
         * been completed if none of the questions in this body
         * on the current path return null as the result of getNext().
         * @return true is this body has been completed.
         */
        public boolean isBodyFinishable() {
            return isInterviewFinishable();
        }

        private ListQuestion question;
        private int index;
        private String defaultSummary;
    };

    /**
     * Create a question with a nominated tag.
     * @param interview The interview containing this question.
     * @param tag A unique tag to identify this specific question.
     */
    protected ListQuestion(Interview interview, String tag) {
        super(interview, tag);

        if (this instanceof EndQuestion) {
            end = (EndQuestion) this;
            bodies = null;
        }
        else {
            end = new EndQuestion(interview, tag, this);
            bodies = new Vector();
        }
    }

    /**
     * Create a new instance of a body for this loop question.
     * The body is a subinterview that contains the questions
     * for the body of the loop.
     * The body does not become one of the set of bodies for the loop
     * until the set is updated with {@link #setBodies}.
     * @param index the position that this body will have within
     * the set of bodies for the loop. This value should be passed
     * through to the Body constructor.
     * @return a new instance of a body for this loop question
     */
    public abstract Body createBody(int index);

    /**
     * Check if this is the question that appears at the beginning or
     * at the end of the loop. When a ListQuestion is created, a sibling
     * is automatically created that will appear at the end of the loop.
     * @return false if this is the main question, that appears at the
     * head of the loop, or true if this is the question that is
     * automatically created to appear at the end of the lop.
     */
    public final boolean isEnd() {
        return (this instanceof EndQuestion);
    }

    /**
     * Get the sibling question that appears at the other end of the loop.
     * When a ListQuestion is created, a sibling is automatically created
     * that will appear at the end of the loop. From either of these questions,
     * you can use this method to get at the other one.
     * @return the sibling question that appears at the other end of the loop
     */
    public ListQuestion getOther() {
        return end;
    }

    /**
     * Get the currently selected loop body, or null, as selected by by setValue.
     * @return the currently selected loop body, or null, if none.
     */
    public Body getSelectedBody() {
        if (value >= 0 && value < bodies.size())
            return (Body) (bodies.elementAt(value));
        else
            return null;
    }

    /**
     * Get the index of the currently selected loop body, or an out of range
     * value (typically less than zero) if none is selected.
     * @return the index of the currently selected loop body, or an out of range
     * value (typically less than zero) if none is selected
     * @see #setValue
     */
    public int getValue() {
        return value;
    }

    /**
     * Verify this question is on the current path, and if it is,
     * return the current value.
     * @return the current value of this question
     * @throws Interview.NotOnPathFault if this question is not on the
     * current path
     * @see #getValue
     */
    public int getValueOnPath()
        throws Interview.NotOnPathFault
    {
        interview.verifyPathContains(this);
        return getValue();
    }

    /**
     * Get a string representation of the index of the currently
     * selected loop body, or an out of range value
     * (typically less than zero) if none is selected.
     */
    public String getStringValue() {
        return String.valueOf(value);
    }

    /**
     * Set the index of the loop body to be selected.
     * If the value is out of range, no loop body will be selected.
     * @param newValue the index of the loop body to be selected
     * @see #getValue
     */
    public void setValue(int newValue) {
        int oldValue = value;
        value = newValue;
        if (normalizeValue(value) != normalizeValue(oldValue)) {
            interview.updatePath(this);
            interview.setEdited(true);
        }
    }

    private int normalizeValue(int value) {
        return (value >= 0 && value < bodies.size() ? value : -1);
    }

    /**
     * Set the index of the loop body to be selected.
     * If the value is out of range, no loop body will be selected.
     * @param s a string containing the index of the loop body
     * to be selected. If the string does not contain a valid
     * integer, the value will be set to -1.
     * @see #getValue
     */
    public void setValue(String s) {
        try {
            if (s != null) {
                setValue(Integer.parseInt(s));
                return;
            }
        }
        catch (NumberFormatException e) {
            // ignore
        }
        setValue(-1);
    }

    /**
     * Check if the question currently has a valid response.
     * For a ListQuestion, this is normally true.
     * @return true if the question currently has a valid response,
     * and false otherwise.
     **/
    public boolean isValueValid() {
        return true;  // should probably reflect whether bodies are valid
    }

    /**
     * Check if the question always has a valid response.
     * For a ListQuestion, this is normally false.
     * @return true if the question always has a valid response,
     * and false otherwise.
     **/
    public boolean isValueAlwaysValid() {
        return false;
    }

    /**
     * Remove all the bodies currently allocated for this question,
     * and set the value of the question to indicate no loop
     * body selected.
     */
    public void clear() {
        setValue(Integer.MIN_VALUE);
        bodies.setSize(0);
    }

    /**
     * Get the summary text for the end question.
     * When a ListQuestion is created, a sibling is automatically created
     * that will appear at the end of the loop.
     * Override this method to override the default behavior to
     * get the summary text from the standard resource bundle.
     * The tag for the end question is the same as the tag for the
     * main question, with ".end" appended.
     * @return the summary text for the end question
     * @see #getSummary
     * @see #getOther
     */
    public String getEndSummary() {
        return end.getDefaultSummary();
    }

    /**
     * Get the question text for the end question.
     * When a ListQuestion is created, a sibling is automatically created
     * that will appear at the end of the loop.
     * Override this method to override the default behavior to
     * get the question text from the standard resource bundle.
     * The tag for the end question is the same as the tag for the
     * main question, with ".end" appended.
     * @return the question text for the end question
     * @see #getEndTextArgs
     * @see #getText
     * @see #getOther
     */
    public String getEndText() {
        return end.getDefaultText();
    }


    /**
     * Get the formatting arguments for the question text for the end question.
     * When a ListQuestion is created, a sibling is automatically created
     * that will appear at the end of the loop.
     * Override this method to override the default behavior to
     * return null.
     * @return the formatting arguments for the question text for the end question
     * @see #getEndText
     * @see #getTextArgs
     * @see #getOther
     */
    public Object[] getEndTextArgs() {
        return end.getDefaultTextArgs();
    }

    protected void load(Map data) {
        bodies.setSize(0);
        String c = (String) (data.get(tag + ".count"));
        if (c != null && c.length() > 0) {
            try {
                int n = Integer.parseInt(c);
                for (int i = 0; i < n; i++)
                    bodies.add(createBody(i));
                // once the bodies are created as children of this question's
                // interview, they'll be reloaded by the interviews load method
            }
            catch (NumberFormatException ignore) {
            }
        }

        String v = (String) (data.get(tag + ".curr"));
        if (v == null || v.length() == 0)
            value = 0;
        else {
            try {
                value = Integer.parseInt(v);
            }
            catch (NumberFormatException ignore) {
                value = 0;
            }
        }
    }

    protected void save(Map data) {
        data.put(tag + ".count", String.valueOf(bodies.size()));
        data.put(tag + ".curr", String.valueOf(value));
    }

    /**
     * Get the set of bodies currently allocated within the loop.
     * @return the set of bodies currently allocated within the loop
     * @see #setBodies
     */
    public Body[] getBodies() {
        Body[] b = new Body[bodies.size()];
        bodies.copyInto(b);
        return b;
    }

    /**
     * Get the number of bodies (iterations) currently allocated within the loop.
     * @return the number of bodies currently allocated within the loop
     */
    public int getBodyCount() {
        return (bodies == null ? 0 : bodies.size());
    }

    /**
     * Get a specified body from the loop.
     * @param index the position of the desired body within the set of bodies
     * currently allocated within the loop.
     * @return the specified body
     * @throws ArrayIndexOutOfBoundsException if index does not identify a
     * valid body
     */
    public Body getBody(int index) {
        return (Body) (bodies.elementAt(index));
    }

    /**
     * Set the set of bodies allocated within the loop, and the
     * index of one which should be selected.
     * The bodies will normally come from a combination of
     * the bodies returned from getBodies() or new ones
     * created by createBody().
     * @param newBodies the set of bodies to be taken as the
     * new set of loop bodies
     * @param newValue the index of the body which should be
     * the selected body.
     * @see #getBodies
     */
    public void setBodies(Body[] newBodies, int newValue) {
        Body oldSelectedBody = getSelectedBody();
        int oldIncompleteCount = getIncompleteBodyCount();

        boolean edited = false;

        if (newBodies.length != bodies.size()) {
            bodies.setSize(newBodies.length);
            edited = true;
        }

        for (int i = 0; i < newBodies.length; i++) {
            Body b = newBodies[i];
            if (b != bodies.elementAt(i)) {
                b.setIndex(i);
                bodies.setElementAt(b, i);
                edited = true;
            }
        }

        value = newValue;
        Body newSelectedBody = getSelectedBody();
        int newIncompleteCount = getIncompleteBodyCount();

        if (newSelectedBody != oldSelectedBody
            || ((oldIncompleteCount == 0) != (newIncompleteCount == 0))) {
            interview.updatePath(this);
        }

        interview.setEdited(edited);
    }

    /**
     * Get the number of bodies for this loop that are currently incomplete,
     * as determined by {@link  Body#isBodyFinishable}.
     * @return the number of bodies for this loop that are currently incomplete.
     */
    public int getIncompleteBodyCount() {
        int count = 0;
        for (int i = 0; i < bodies.size(); i++) {
            Body b = (Body) (bodies.elementAt(i));
            if (!b.isInterviewFinishable())
                count++;
        }
        return count;
    }


    private final EndQuestion end;
    private final Vector bodies;
    private int value;

    private static final ResourceBundle i18n = Interview.i18n;

    private static class EndQuestion extends ListQuestion {
        EndQuestion(Interview interview, String tag, ListQuestion head) {
            super(interview, tag + ".end");
            this.head = head;
        }

        public Question getNext() {
            boolean allBodiesFinishable = true;
            for (int i = 0; i < head.getBodyCount(); i++) {
                Body b = (Body) (head.getBody(i));
                if (!b.isInterviewFinishable()) {
                    allBodiesFinishable = false;
                    break;
                }
            }

            return (allBodiesFinishable ? head.getNext() : null);
        }

        public String getSummary() {
            // ListQuestion.getEndSummary can be overridden, but defaults to
            // getDefaultSummary() here, which calls super.getSummary()
            return head.getEndSummary();
        }

        String getDefaultSummary() {
            return super.getSummary();
        }

        public String getText() {
            // ListQuestion.getEndText can be overridden, but defaults to
            // getDefaultText() here, which calls super.getText()
            return head.getEndText();
        }

        String getDefaultText() {
            return super.getText();
        }

        public Object[] getTextArgs() {
            // ListQuestion.getEndTextArgs can be overridden, but defaults to
            // getDefaultTextArgs() here, which calls super.getText()
            return head.getEndTextArgs();
        }

        Object[] getDefaultTextArgs() {
            return super.getTextArgs();
        }

        public int getValue() {
            return head.getValue();
        }

        public String getStringValue() {
            return head.getStringValue();
        }

        public void setValue(int value) {
            head.setValue(value);
        }

        public void setValue(String s) {
            head.setValue(s);
        }

        public Body getSelectedBody() {
            return head.getSelectedBody();
        }

        public Body createBody(int index) {
            return head.createBody(index);
        }

        public ListQuestion getOther() {
            return head;
        }

        public void clear() {
            head.clear();
        }

        protected void load(Map data) {
        }

        protected void save(Map data) {
        }

        public Body[] getBodies() {
            return head.getBodies();
        }

        public int getBodyCount() {
            return head.getBodyCount();
        }

        public Body getBody(int index) {
            return head.getBody(index);
        }

        public void setBodies(Body[] newBodies, int newValue) {
            head.setBodies(newBodies, newValue);
        }

        public int getIncompleteBodyCount() {
            return head.getIncompleteBodyCount();
        }

        private ListQuestion head;

    }
}
