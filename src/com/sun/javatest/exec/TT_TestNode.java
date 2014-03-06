/*/*
 * $Id$
 *
 * Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.TestResult;

import java.util.Enumeration;
import javax.swing.tree.TreeNode;

/**
 *
 * @author work
 */
public class TT_TestNode extends TT_TreeNode {

    TT_TestNode(TT_BasicNode parent, TestResult tr) {
        this.tr = tr;
        this.parent = parent;
    }
    // ------- interface methods --------
    public Enumeration children() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public boolean getAllowsChildren() {
        return false;
    }

    public TreeNode getChildAt(int arg0) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public int getChildCount() {
        return 0;
    }

    public int getIndex(TreeNode arg0) {
        return -1;
    }

    public TreeNode getParent() {
        return parent;
    }

    public boolean isLeaf() {
        return true;
    }

    boolean isRoot() {
        // not possible, right?
        return false;
    }

    // -------- custom methods -----------
    TestResult getTestResult() {
        return tr;
    }

    String getDisplayName() {
        return getShortName();
    }

    String getLongDescription() {
        // should be the long path to the folder, or custom
        return null;
    }

    /**
     * String for use whenever you need a basic name for this node.  You can
     * assume that this name is unique within any node.
     * @return Short name for this node, containing no forward slashes or
     *    spaces.
     */
    String getShortName() {
        if (shortN == null) {
            String fullname = tr.getTestName();
            int lastSlash = fullname.lastIndexOf("/");
            shortN = (lastSlash != -1 ? fullname.substring(lastSlash + 1) : fullname);
        }

        return shortN;
    }

    /**
     * Get the long internal representation of this location.
     * @return Null if the node is the root, else a forward slash separated
     *      path.
     */
    String getLongPath() {
        if (parent == null) // root
        {
            return null;
        }
        StringBuffer sb = new StringBuffer(getShortName());
        TT_BasicNode spot = parent;
        while (spot != null && !spot.isRoot()) {
            sb.insert(0, "/");
            sb.insert(0, spot.getShortName());
            spot = spot.parent;
        }
        return sb.toString();
    }
    private TestResult tr;
    private String shortN;      // stored, expensive to recalculate
}
