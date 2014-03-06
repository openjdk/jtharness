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
package com.sun.javatest.tool;

import java.awt.AWTError;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.LayoutManager;
import javax.swing.JPanel;

/**
 * A class to providing a panel with CardLayout.
 * Components can be added to the panel, although only one will be
 * visible at a time. Although CardLayout requires components to
 * be selected by name, this class allows components to be selected
 * by reference.
 */
public class Deck extends JPanel
{
    /**
     * Create a Deck panel.
     */
    public Deck() {
        this("deck");
    }

    /**
     * Create a Deck panel with a given name.
     * @param name the name for the panel
     */
    public Deck(String name) {
        super(new CardLayout());
        setName(name);
    }


    /**
     * Create a Deck panel with a given name and set of components.
     * @param name the name for the panel
     * @param comps the components to be added into the panel
     */
    public Deck(String name, Component[] comps) {
        this(name);
        for (int i = 0; i < comps.length; i++) {
            add(comps[i]);
        }
    }

    public void setLayout(LayoutManager lm) {
        if (lm instanceof CardLayout)
            super.setLayout(lm);
        else
            throw new AWTError("Can't set layout for " + getClass().getName());
    }

    /**
     * Add a component to the panel. If the component does not have a name
     * a default name will be set. The name of the component should not be
     * changed after it has been added to the deck.
     * @param comp The component to be added.
     * @return The component that was added (comp).
     */
    public Component add(Component comp) {
        if (comp.getName() == null)
            comp.setName("card" + cardNum++);
        super.add(comp, comp.getName());
        return comp;
    }

    /**
     * Add a component to the panel. The constraints are ignored.
     * @param comp The component to be added
     * @param x Ignored
     */
    public void add(Component comp, Object x) {
        add(comp);
    }

    /**
     * Get the component that is currently visible in the deck.
     * @return the component that is currently visible in the deck.
     */
    public Component getCurrentCard() {
        for (int i = 0; i < getComponentCount(); i++) {
            Component c = getComponent(i);
            if (c.isVisible())
                return c;
        }
        return null;
    }

    /**
     * Make a component visible in the deck. If different, the previously
     * visible component will be hidden.
     * @param comp The component to be made visible. It must previously have
     * been added to the deck.
     */
    public void show(Component comp) {
        ((CardLayout)(getLayout())).show(this, comp.getName());
    }

    private int cardNum;
}
