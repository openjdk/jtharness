/*
 * $Id$
 *
 * Copyright (c) 2002, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Arrays;
import java.util.Map;
import java.util.Vector;

/**
 * A {@link Question question} to which the response is a set of selected
 * nodes within a tree. The nodes are identified by their paths.
 */
public abstract class TreeQuestion extends Question
{
    /**
     * An interface that provides the model for the tree whose nodes
     * are selected by a TreeQuestion.
     */
    public static interface Model {
        /**
         * Get the root node of the tree.
         * @return the root node of the tree.
         */
        Object getRoot();

        /**
         * Get the number of children of a specified node.
         * @param node the node for which to get the number of children
         * @return the number of children of the specified node
         */
        int getChildCount(Object node);

        /**
         * Get a child of a specified node, or null if no such child exists.
         * @param node the node for which to get the child
         * @param index the index of the child; this should be a
         * number greater than or equal to zero and less than the number
         * of children of the node.
         * @return the specified child, or null if index does not identify a
         * valid child.
         * @see #getChildCount
         */
        Object getChild(Object node, int index);

        /**
         * Get the name of a specified node. The name will be used to
         * construct paths. At a minimum, it should not contain '/'.
         * @param node the node whose name is required
         * @return the name of the specified node
         */
        String getName(Object node);

        /**
         * Get the full path from the root node of a specified node.
         * @param node the node whose path is required
         * @return the path of the specified node from the root node,
         * as composed from the names of this node and its ancestors,
         * using '/' to separate the individual names.
         */
        String getPath(Object node);

        /**
         * Determine if this a node is a leaf node.
         * This is primarily an attribute of the node's actual type,
         * and is not necessarily the same as having zero children.
         * (A leaf node implies zero children, but zero children does
         * not imply a node is a leaf.)
         * @param node the node to check for being a leaf
         * @return true if the specified node is a leaf node, and
         * false otherwise.
         */
        boolean isLeaf(Object node);
    };

    /**
     * Create a tree question with a nominated tag and tree model.
     * @param interview The interview containing this question.
     * @param tag A unique tag to identify this specific question.
     * @param model The tree model to which the question's path values relate.
     */
    protected TreeQuestion(Interview interview, String tag, Model model) {
        super(interview, tag);
        this.model = model;
        clear();
        setDefaultValue(value);
    }

    /**
     * Get the tree model to which the question's path values relate.
     * @return the tree model to which the question's path values relate
     */
    public Model getModel() {
        return model;
    }

    /**
     * Get the default response for this question.
     * @return the default response for this question.
     *
     * @see #setDefaultValue
     */
    public String[] getDefaultValue() {
        return defaultValue;
    }

    /**
     * Set the default response for this question,
     * used by the clear method.
     * @param v the default response for this question.
     *
     * @see #getDefaultValue
     */
    public void setDefaultValue(String[] v) {
        defaultValue = v;
    }

    /**
     * Get the current (default or latest) response to this question.
     * The strings should each represent valid paths to nodes within the
     * tree represented by the tree model.
     * @return the current value for this question
     * @see #setValue
     */
    public String[] getValue() {
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
    public String[] getValueOnPath()
        throws Interview.NotOnPathFault
    {
        interview.verifyPathContains(this);
        return getValue();
    }

    public void setValue(String newValue) {
        setValue(split(newValue));
    }

    /**
     * Set the current response to this question.
     * @param newValue a set of strings (or null if none), representing
     * paths to nodes within the tree represented by the tree model
     * @see #getValue
     */
    public void setValue(String[] newValue) {
        String[] oldValue;
        if (newValue == null) {
            oldValue = value;
            value = null;
        }
        else {
            // could arguably validate paths here and throw Fault if invalid
            oldValue = value;
            /* leave this for clients to do, if they want
            // sort and remove duplicates from the array
            TreeSet ts = new TreeSet(Arrays.asList(newValue));
            value = (String[]) (ts.toArray(new String[ts.size()]));
            */
            value = newValue;
        }

        if (!Arrays.equals(value, oldValue)) {
            interview.updatePath(this);
            interview.setEdited(true);
        }
    }

    /**
     * Set the current response to this question. The response is
     * set to the paths of a set of specified nodes within the tree
     * represented by the tree model.
     * @param nodes a set of nodes (or null if none) within
     * the tree represented by the tree model, whose paths will
     * be set as the current response to the question
     * @see #getValue
     */
    public void setValue(Object[] nodes) {
        if (nodes == null) {
            setValue((String[]) null);
        return;
    }

        String[] paths = new String[nodes.length];
        for (int i = 0; i < nodes.length; i++)
            paths[i] = model.getPath(nodes[i]);
    }

    public String getStringValue() {
        return join(value);
    }

    public boolean isValueValid() {
        return true;
    }

    public boolean isValueAlwaysValid() {
        return false;
    }

    /*
    public Object[] getNodesValue() {
        if (value == null)
            return null;

        Vector v = new Vector();
        for (int i = 0; i < value.length; i++) {
            Object node = getNode(value[i]);
            if (node != null)
                v.add(node);
        }

        Object[] nodes = new Object[v.size()];
        v.copyInto(nodes);
        return nodes;
    }
    */

    /*
    public Object getNode(String path) {
        return model.getNode(getRoot(), path);
    }
    */

    /*
    private Object getNode(Object node, String path) {
        if (node == null)
            throw new NullPointerException();

        if (path.length() == 0)
            return node;

        String head;
        String tail;

        int sep = path.indexOf("/");
        if (sep == -1) {
            head = path;
            tail = null;
        }
        else {
            head = path.substring(0, sep);
            tail = path.substring(sep + 1);
        }

        for (int i = 0; i < model.getChildCount(node); i++) {
            Object c = model.getChild(node, i);
            if (model.getName(c).equals(head))
                return (tail == null ? c : getNode(c, tail));
        }

        return null;
    }
    */

    /*
    public boolean isSelected(Object node) {
        if (value == null)
            return false;

        for (int i = 0; i < value.length; i++) {
            if (node == value[i])
                return true;
        }

        return false;
    }
    */

    /**
     * Clear any response to this question, resetting the value
     * back to its initial state.
     */
    public void clear() {
        setValue(defaultValue);
    }

    /**
     * Load the value for this question from a dictionary, using
     * the tag as the key.
     * @param data The map from which to load the value for this question.
     */
    protected void load(Map data) {
        String paths = (String) (data.get(tag));
        setValue(paths);
    }

    /**
     * Save the value for this question in a dictionary, using
     * the tag as the key.
     * @param data The map in which to save the value for this question.
     */
    protected void save(Map data) {
        data.put(tag, join(value));
    }

    public static String[] split(String s) {
        if (s == null)
            return empty;

        Vector v = new Vector();
        int start = -1;
        for (int i = 0; i < s.length(); i++) {
            if (white(s.charAt(i))) {
                if (start != -1)
                    v.addElement(s.substring(start, i));
                start = -1;
            } else
                if (start == -1)
                    start = i;
        }

        if (start != -1)
            v.addElement(s.substring(start));

        if (v.size() == 0)
            return empty;

        String[] a = new String[v.size()];
        v.copyInto(a);
        return a;
    }

    public static String join(String[] paths) {
        if (paths == null || paths.length == 0)
            return "";

        int l = paths.length - 1; // allow for spaces between words
        for (int i = 0; i < paths.length; i++)
            l += paths[i].length();

        StringBuffer sb = new StringBuffer(l);
        sb.append(paths[0]);
        for (int i = 1; i < paths.length; i++) {
            sb.append(' ');
            sb.append(paths[i]);
        }

        return sb.toString();
    }

    public static boolean white(char c) {
        return (c == ' '  ||  c == '\t'  ||  c == '\n');
    }

    private static String[] empty = { };

    private Model model;

    /**
     * The current response for this question.
     */
    protected String[] value;

    /**
     * The default response for this question.
     */
    private String[] defaultValue;
}
