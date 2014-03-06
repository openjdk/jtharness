/*
 * $Id$
 *
 * Copyright (c) 2009, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.util.LinkedList;
import javax.swing.tree.TreeNode;

/**
 * Basic part of the onscreen model of the test tree.
 */
public abstract class TT_TreeNode implements TreeNode {
    /**
     * Short name that should be shown in user interfaces.
     * @return Concise description of this node instance.
     */
    abstract String getDisplayName();

    /**
     * Description to be shown when needed.
     * @return Verbose description of this node instance.
     * @see TT_TreeNode#getDisplayName()
     */
    abstract String getLongDescription();

    /**
     * Get the long internal representation of this location.
     * @return Null if the node is the root, else a forward slash separated
     * path.
     */
    abstract String getLongPath();

    /**
     * String for use whenever you need a basic name for this node.  You can
     * assume that this name is unique within any node.
     * @return Short name for this node, containing no forward slashes or
     * spaces.
     */
    abstract String getShortName();

    TT_TreeNode[] getNodePath() {
        if (parent == null)
            return new TT_TreeNode[] {this};

        LinkedList<TT_TreeNode> l = new LinkedList();
        l.add(this);

        TT_TreeNode spot = parent;
        do {
            l.add(0, spot);
            spot = spot.parent;
        } while (spot != null);

        return l.toArray(new TT_TreeNode[l.size()]);
    }

    abstract boolean isRoot();

    public String toString() {
        return getShortName();
    }

    protected TT_BasicNode parent;
}
