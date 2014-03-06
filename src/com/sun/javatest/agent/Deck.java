/*
 * $Id$
 *
 * Copyright (c) 1996, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.agent;

import java.awt.AWTError;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Panel;

class Deck extends Panel
{
    public Deck() {
        super(new CardLayout());
    }

    public void setLayout(LayoutManager lm) {
        if (lm instanceof CardLayout)
            super.setLayout(lm);
        else
            throw new AWTError("Can't set layout for " + getClass().getName());
    }

    public Component add(Component comp) {
        if (comp.getName() == null)
            comp.setName("card" + cardNum++);
        super.add(comp, comp.getName());
        return comp;
    }

    /**
     * @deprecated
     */
    public Dimension preferredSize() {
        Component comp = getCurrentCard();
        Dimension d = comp.getPreferredSize();

        return d;
    }

    public void add(Component comp, Object x) {
        throw new AWTError("Can't add with constraints for " + getClass().getName());
    }

    public Component getCurrentCard() {
        for (int i = 0; i < getComponentCount(); i++) {
            Component c = getComponent(i);
            if (c.isVisible())
                return c;
        }
        return null;
    }

    public void show(Component comp) {
        // Set the wait cursor on the parent because we're going to make
        // this panel invisible while we bring the new card to the fore.
        Component parent = getParent();
        Cursor savedCursor = parent.getCursor();
        parent.setCursor(waitCursor);
        //boolean savedVisible = isVisible();
        //setVisible(false);
        comp.validate();
        ((CardLayout)(getLayout())).show(this, comp.getName());
        //setVisible(savedVisible);
        parent.setCursor(savedCursor);
    }

    private Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
    private int cardNum;
}
