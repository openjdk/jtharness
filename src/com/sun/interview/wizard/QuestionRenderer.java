/*
 * $Id$
 *
 * Copyright (c) 2002, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.interview.wizard;

import java.awt.event.ActionListener;
import javax.swing.JComponent;
import com.sun.interview.Question;

/**
 * An interface that is used to access a component to be used to display
 * the response field for a question in an interview.
 */
public interface QuestionRenderer
{
    /**
     * Return a component that can be used to display a suitable response field
     * for a question.
     * @param q the question whose response field should appear in the component
     * @param listener a listener that should be invoked if the component supports
     * an action like "Enter" to commit the response; should also be invoked with
     * an "edited" action when the value is changed.
     * @return a component that can be used to display a suitable response field
     */
    JComponent getQuestionRendererComponent(Question q, ActionListener listener);

    /**
     * Get a string to display when the response to a question is invalid.
     * (i.e. isValid() is false or getNext() is null).
     * @param q the question which has an invalid response
     * @return a string to display when the response to a question is invalid.
     */
    String getInvalidValueMessage(Question q);

    /**
     * The name of a client property that should be put on the component
     * returned from getQuestionRendererComponent. This property should
     * be a Runnable, which will be invoked when any data in the component
     * needs to be saved back into the question.
     */
    public static final String VALUE_SAVER =  "valueSaver";

    /**
     * The action command for notifying that a question's response has been edited.
     */
    public static final String EDITED = "edited";
}
