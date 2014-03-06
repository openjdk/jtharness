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
package com.sun.javatest.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.util.Locale;

/**
 * This class provides a means whereby tools in Personal Java can
 * temporarily give access to a shared GUI frame, such as the
 * one top-level Frame.
 */
public class MainFrame
{
    /**
     * Set a context that can be subsequently accessed via @link(#acquireFrame)
     * and @link(#releaseFrame).
     * @param c The container to be registered
     * @throws ClassCastException if the container being registered is not a frame
     * @see #getContext
     * @deprecated replaced by @link(#setFrame)
     */
    public static synchronized void setContext(Container c) {
        setFrame((Frame)c);
    }

    /**
     * Set a frame that can be subsequently accessed via @link(#acquireFrame)
     * and @link(#releaseFrame).
     * @param f The container to be registered
     */
    public static synchronized void setFrame(Frame f) {
        frame = f;
    }



    /**
     * Try to acquire exclusive access to a shared context previously registered
     * with @link(#setFrame). If the frame is currently in use by someone
     * else, the call will wait until the other owner is releases it.
     * @return The container previously registered with setFrame. It will
     * be returned in as clean a state as possible, with no children, and
     * a new instance of @link(FlowLayout). null is returned if the call is
     * interrupted.
     * @see #setContext
     * @deprecated replaced by acquireFrame.
     */
    public static Container getContext() {
        try {
            return acquireFrame();
        }
        catch (InterruptedException e) {
            return null;
        }
    }

    /**
     * Try to acquire exclusive access to a shared context previously registered
     * with @link(#setContext). If the context is currently in use by someone
     * else, the call will wait until the other owner is releases it.
     * @return The container previously registered with setContext. It will
     * be returned in as clean a state as possible, with no children, and
     * a new instance of @link(FlowLayout).
     * If no frame has been registered, one has been created.
     * @throws InterruptedException if the thread is interrupted while waiting
     * for the context to become available.
     * @see #releaseFrame
     */
    public static synchronized Frame acquireFrame() throws InterruptedException {
        if (frame == null) {
            frame = new Frame("JT Harness Default Frame");
            initialTitle = frame.getTitle();
            initialState = frame.getState();
            initialIconImage = frame.getIconImage();
            initialCursor = frame.getCursor();
            initialFont = frame.getFont();
            initialLocale = frame.getLocale();
            initialResizable = frame.isResizable();
            initialEnabled = frame.isEnabled();
        }

        while (inUse)
            // note: MainFrame.class provides the monitor for a static synchronized
            // method in this class.
            MainFrame.class.wait();

        inUse = true;

        // save state ... should all be free of side effects
        // the properties are all available in PBP 1.0, which is
        // currently the lowest common denominator for all MainFrame users.
        savedLayout = frame.getLayout();
        savedComponents = frame.getComponents();
        savedName = frame.getName();
        savedBounds = frame.getBounds();
        Color bg = frame.getBackground();
        savedBackground = (bg == null ? SystemColor.window : bg);
        Color fg = frame.getForeground();
        savedForeground = (fg == null ? SystemColor.windowText : fg);
        savedTitle = frame.getTitle();
        savedState = frame.getState();
        savedIconImage = frame.getIconImage();
        savedCursor = frame.getCursor();
        savedFont = frame.getFont();
        savedLocale = frame.getLocale();
        savedResizable = frame.isResizable();
        savedEnabled = frame.isEnabled();

        // now we want to clear the context ... do it carefully
        // set the layout to null while we remove the components
        // so as not to affect the savedlayoutManager
        frame.setLayout(null);
        frame.removeAll();
        frame.setLayout(new FlowLayout());
        frame.setTitle(initialTitle);
        frame.setState(initialState);
        frame.setIconImage(initialIconImage);
        frame.setCursor(initialCursor);
        frame.setFont(initialFont);
        frame.setLocale(initialLocale);
        frame.setResizable(initialResizable);
        frame.setEnabled(initialEnabled);

        return frame;
    }


    /**
     * Release access to the previously acquired context. The context is reset
     * to its state before  @link(#acquireContext) was called, and made available
     * to subsequent callers of @link(#acquireContext).
     * @param c The result of previously calling @link(#acquireContext).
     * @throws IllegalStateException if the argument is not the result of
     * calling @link(#acquireFrame), or if it has already been released.
     * @throws ClassCastException if the container is not a frame.
     * @deprecated replaced by releaseFrame.
     */
    public static void restoreContext(Container c) {
        releaseFrame((Frame)c);
    }



    /**
     * Release access to the previously acquired frame. The frame is reset
     * to its state before  @link(#acquireFrame) was called, and made available
     * to subsequent callers of @link(#acquireFrame).
     * @param f The result of previously calling @link(#acquireFrame).
     * @throws IllegalStateException if the argument is not the result of
     * calling @link(#acquireFrame), or if it has already been released.
     * @see #acquireFrame
     */
    public static synchronized void releaseFrame(Frame f) {
        if (f == null)
            return;

        if (f != frame)
            throw new IllegalStateException("wrong frame");

        if (!inUse)
            throw new IllegalStateException("frame not acquired");

        // remove what might have been put in the frame
        frame.setLayout(null);
        frame.removeAll();

        // Restore previously saved stuff, do as much as possible
        // with layout manager set to null, to minimize side effects.
        frame.setForeground(savedForeground);
        frame.setBackground(savedBackground);
        frame.setBounds(savedBounds);
        frame.setName(savedName);
        for (int i = 0; i < savedComponents.length; i++)
            frame.add(savedComponents[i]);
        frame.setLayout(savedLayout);
        frame.setTitle(savedTitle);
        frame.setState(savedState);
        frame.setIconImage(savedIconImage);
        frame.setCursor(savedCursor);
        frame.setFont(savedFont);
        frame.setLocale(savedLocale);
        frame.setResizable(savedResizable);
        frame.setEnabled(savedEnabled);

        inUse = false;
        MainFrame.class.notify();
    }

    private static Frame frame;
    private static boolean inUse;

    // saved state of context while it is in use by others
    private static LayoutManager savedLayout;
    private static Component[] savedComponents;
    private static String savedName;
    private static Rectangle savedBounds;
    private static Color savedBackground;
    private static Color savedForeground;
    private static String savedTitle;
    private static int savedState;
    private static Image savedIconImage;
    private static Cursor savedCursor;
    private static Font savedFont;
    private static Locale savedLocale;
    private static boolean savedResizable;
    private static boolean savedEnabled;

    private static String initialTitle;
    private static int initialState;
    private static Image initialIconImage;
    private static Cursor initialCursor;
    private static Font initialFont;
    private static Locale initialLocale;
    private static boolean initialResizable;
    private static boolean initialEnabled;
}
