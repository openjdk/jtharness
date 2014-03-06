/*
 * $Id$
 *
 * Copyright (c) 2004, 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.util;

import java.io.IOException;
import java.io.Writer;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * A class that provides a tree of information nodes that can be
 * selectively printed, suitable for simple command line help.
 */
public class HelpTree
{
    /**
     * A node within a HelpTree.  A node has a name, a description,
     * and zero or more child nodes.
     */
    public static class Node {

        /**
         * Create a node, with no children.
         * @param name the name for the node
         * @param description the description for the node
         */
        public Node(String name, String description) {
            this.name = name;
            this.description = description;
        }

        /**
         * Create a node, with given children.
         * @param name the name for the node
         * @param description the description for the node
         * @param children the child nodes for the node
         */
        public Node(String name, String description, Node[] children) {
            this.name = name;
            this.description = description;
            this.children = children;
        }

        /**
         * Create a node, with no children. The name and description are
         * obtained from a resource bundle, using keys based on a common
         * prefix. The key for the name will be <i>prefix</i>.name and
         * the key for the description will be <i>prefix</i>.desc.
         * @param i18n the resource bundle from which to obtain the
         * name and description for the node.
         * @param prefix the prefix for the names of the name and description
         * entries in the resource bundle.
         */
        public Node(I18NResourceBundle i18n, String prefix) {
            name = i18n.getString(prefix + ".name");
            description = i18n.getString(prefix + ".desc");
        }

        /**
         * Create a node, with given children. The name and description are
         * obtained from a resource bundle, using keys based on a common
         * prefix. The key for the name will be <i>prefix</i>.name and
         * the key for the description will be <i>prefix</i>.desc.
         * @param i18n the resource bundle from which to obtain the
         * name and description for the node.
         * @param prefix the prefix for the names of the name and description
         * entries in the resource bundle.
         * @param children the child nodes for this node
         */
        public Node(I18NResourceBundle i18n, String prefix, Node[] children) {
            this(i18n, prefix);
            this.children = children;
        }

        /**
         * Create a node and its children. The name and description are
         * obtained from a resource bundle, using keys based on a common
         * prefix. The key for the name will be <i>prefix</i>.name and
         * the key for the description will be <i>prefix</i>.desc.
         * The children will each be created with no children of their
         * own, using a prefix of <i>prefix</i>.<i>entry</i>.
         * @param i18n the resource bundle from which to obtain the
         * name and description for the node.
         * @param prefix the prefix for the names of the name and description
         * entries in the resource bundle.
         * @param entries the array of <i>entry</i> names used to create
         * the child nodes.
         */
        public Node(I18NResourceBundle i18n, String prefix, String[] entries) {
            this(i18n, prefix);
            children = new Node[entries.length];
            for (int i = 0; i < children.length; i++)
                children[i] = new Node(i18n, prefix + '.' + entries[i]);
        }

        /**
         * Get the name of this node.
         * @return the name of this node
         */
        public final String getName() {
            return name;
        }

        /**
         * Get the description of this node.
         * @return the description of this node
         */
        public final String getDescription() {
            return description;
        }

        /**
         * Get the number of children of this node.
         * @return the number of children of this node
         */
        public int getChildCount() {
            return (children == null ? 0 : children.length);
        }

        /**
         * Get a specified child of this node.
         * @param i the index of the desired child
         * @return the specified child of this node
         */
        public Node getChild(int i) {
            if (i >= getChildCount())
                throw new IllegalArgumentException();
            return children[i];
        }

        private String name;
        private String description;
        private Node[] children;
    }

    /**
     * A selection of nodes within a HelpTree.
     * @see HelpTree#find
     */
    public class Selection {
        private Selection(Node node) {
            this(node, null);
        }

        private Selection(Map map) {
            this(null, map);
        }

        private Selection(Node node, Map map) {
            this.node = node;
            this.map = map;
        }

        private Node node;
        private Map map;
    }

    /**
     * Create an empty HelpTree object.
     */
    public HelpTree() {
        nodes = new Node[0];
    }

    /**
     * Create a HelpTree object containing a given set of nodes.
     * @param nodes the contents of the HelpTree
     */
    public HelpTree(Node[] nodes) {
        this.nodes = nodes;
    }

    /**
     * Add a node to a help tree.
     * @param node the node to be added to the tree
     */
    public void addNode(Node node) {
        nodes = (Node[]) DynamicArray.append(nodes, node);
    }

    /**
     * Get the indentation used to adjust the left margin when writing
     * the child nodes for a node.
     * @return the indentation used to adjust the left margin when writing
     * the child nodes for a node
     * @see #setNodeIndent
     */
    public int getNodeIndent() {
        return nodeIndent;
    }

    /**
     * Set the indentation used to adjust the left margin when writing
     * the child nodes for a node.
     * @param n the indentation used to adjust the left margin when writing
     * the child nodes for a node
     * @see #getNodeIndent
     */
    public void setNodeIndent(int n) {
        nodeIndent = n;
    }

    /**
     * Get the indentation used to adjust the left margin when writing
     * the description of a node.
     * @return the indentation used to adjust the left margin when writing
     * the description of a node
     * @see #setDescriptionIndent
     */
    public int getDescriptionIndent() {
        return descriptionIndent;
    }

    /**
     * Set the indentation used to adjust the left margin when writing
     * the description of a node.
     * @param n the indentation used to adjust the left margin when writing
     * the description of a node
     * @see #getDescriptionIndent
     */
    public void setDescriptionIndent(int n) {
        descriptionIndent = n;
    }

    /**
     * Get a selection representing the nodes that match the given words.
     * If there are nodes whose name or description contain all of the
     * given words, then those nodes will be returned.
     * Otherwise, all nodes whose name or description contain at least one
     * of the given words will be returned.
     * @param words the words to be searched for
     * @return a Selection containing the matching nodes
     */
    public Selection find(String[] words) {
        Selection s = find(words, ALL);

        if (s == null && words.length > 1)
            s = find(words, ANY);

        return s;
    }

    /**
     * Get a selection representing the nodes that match all of the given words.
     * @param words the words to be searched for
     * @return a Selection containing the matching nodes
     */
    public Selection findAll(String[] words) {
        return find(words, ALL);
    }

    /**
     * Get a selection representing the nodes that each match
     * at least one of the given words.
     * @param words the words to be searched for
     * @return a Selection containing the matching nodes
     */
    public Selection findAny(String[] words) {
        return find(words, ANY);
    }

    private Selection find(String[] words, int mode) {
        Map map = null;

        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            Selection s = find(node, words, mode);
            if (s != null) {
                if (map == null)
                    map = new TreeMap(nodeComparator);
                map.put(node, s);
            }
        }

        return (map == null ? null : new Selection(map));
    }

    private Selection find(Node node, String[] words, int mode) {
        if (mode == ALL) {
            if (containsAllOf(node.name, words) || containsAllOf(node.description, words))
                return new Selection(node);
        }
        else if (mode == ANY) {
            if (containsAnyOf(node.name, words) || containsAnyOf(node.description, words))
                return new Selection(node);
        }
        else
            throw new IllegalArgumentException();

        if (node.children == null)
            return null;

        Map map = null;

        for (int i = 0; i < node.children.length; i++) {
            Node child = node.children[i];
            Selection s = find(child, words, mode);
            if (s != null) {
                if (map == null)
                    map = new TreeMap(nodeComparator);
                map.put(child, s);
            }
        }

        return (map == null ? null : new Selection(node, map));
    }

    /**
     * Write out all the nodes in this HelpTree.
     * @param out the writer to which to write the nodes.
     * If out is a com.sun.javatest.util.WrapWriter, it will be
     * used directly, otherwise a WrapWriter will be created
     * that will write to the given writer.
     * @throws IOException if the is a problem writing the
     * nodes.
     * @see WrapWriter
     */
    public void write(Writer out) throws IOException {
        WrapWriter ww = getWrapWriter(out);

        for (int i = 0; i < nodes.length; i++) {
            write(ww, nodes[i]);
            ww.write('\n');
        }

        if (ww != out)
            ww.flush();
    }

    /**
     * Write out selected nodes in this HelpTree.
     * @param out the writer to which to write the nodes.
     * If out is a com.sun.javatest.util.WrapWriter, it will be
     * used directly, otherwise a WrapWriter will be created
     * that will write to the given writer.
     * @param s a Selection object containing the nodes to be written
     * @throws IOException if the is a problem writing the
     * nodes.
     * @see WrapWriter
     */
    public void write(Writer out, Selection s) throws IOException {
        WrapWriter ww = getWrapWriter(out);

        write(ww, s.map);

        if (ww != out)
            ww.flush();
    }

    /**
     * Write out a summary of all the nodes in this HelpTree.
     * The summary will contain the name and description of the
     * top level nodes, but not any of their children.
     * @param out the writer to which to write the nodes.
     * If out is a com.sun.javatest.util.WrapWriter, it will be
     * used directly, otherwise a WrapWriter will be created
     * that will write to the given writer.
     * @throws IOException if the is a problem writing the
     * nodes.
     * @see WrapWriter
     */
    public void writeSummary(Writer out) throws IOException {
        WrapWriter ww = getWrapWriter(out);

        for (int i = 0; i < nodes.length; i++)
            writeHead(ww, nodes[i]);

        if (ww != out)
            ww.flush();
    }

    private void write(WrapWriter out, Map m) throws IOException {
        int margin = out.getLeftMargin();
        for (Iterator iter = m.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry e = (Map.Entry) (iter.next());
            Node node = (Node) (e.getKey());
            Selection s = (Selection) (e.getValue());
            if (s.map == null)
                write(out, node);
            else {
                writeHead(out, node);
                out.setLeftMargin(margin + nodeIndent);
                write(out, s.map);
                out.setLeftMargin(margin);
            }
            if (margin == 0)
                out.write('\n');
        }
    }

    private void write(WrapWriter out, Node node) throws IOException {
        int baseMargin = out.getLeftMargin();

        writeHead(out, node);

        Node[] children = node.children;
        if (children != null && children.length > 0) {
            out.setLeftMargin(baseMargin + nodeIndent);
            for (int i = 0; i < children.length; i++)
                write(out, children[i]);
        }

        out.setLeftMargin(baseMargin);
    }

    private void writeHead(WrapWriter out, Node node) throws IOException {
        int baseMargin = out.getLeftMargin();

        String name = node.name;
        String desc = node.description;
        if (name != null) {
            out.write(name);
            out.write(' ');
            if (desc != null) {
                out.setLeftMargin(baseMargin + descriptionIndent);
                if (out.getCharsOnLineSoFar() + 2 > out.getLeftMargin())
                    out.write('\n');
                out.write(desc);
            }
            out.write('\n');
        }

        out.setLeftMargin(baseMargin);
    }

    private boolean containsAllOf(String text, String[] words) {
        for (int i = 0; i < words.length; i++) {
            if (!contains(text, words[i]))
                return false;
        }
        return true;
    }

    private boolean containsAnyOf(String text, String[] words) {
        for (int i = 0; i < words.length; i++) {
            if (contains(text, words[i]))
                return true;
        }
        return false;
    }

    private boolean contains(String text, String word) {
        int startIndex = text.toLowerCase().indexOf(word.toLowerCase());
        if (startIndex == -1)
            return false;

        int endIndex = startIndex + word.length();

        return ((startIndex == 0 || !Character.isLetter(text.charAt(startIndex - 1)))
                && (endIndex == text.length() || !Character.isLetter(text.charAt(endIndex))));
    }

    private WrapWriter getWrapWriter(Writer out) {
        return (out instanceof WrapWriter ? (WrapWriter) out : new WrapWriter(out));
    }

    private Node[] nodes;

    private int nodeIndent = 4;
    private int descriptionIndent = 16;

    private static final int ALL = 1;
    private static final int ANY = 2;

    private static Comparator nodeComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                Node n1 = (Node) o1;
                Node n2 = (Node) o2;

                int v = compareStrings(n1.name, n2.name);
                return (v != 0 ? v : compareStrings(n1.description, n2.description));
            }

            private int compareStrings(String s1, String s2) {
                if (s1 == null && s2 == null)
                    return 0;

                if (s1 == null || s2 == null)
                    return (s1 == null ? -1 : +1);

                return s1.toLowerCase().compareTo(s2.toLowerCase());
            }
        };
}
