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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.net.URL;

/**
 * An image that can be treated as a component.  It is intended for
 * use to display icons, so the size is synchronized and the preference
 * is reported to be rigid at the size of the image itself.
 */
class Icon extends Component {

    public Icon(Image i) {
        image = i;
        waitForSize();
        imageSize = new Dimension(-1, -1);
        imageSize.width = i.getWidth(this);
        imageSize.height = i.getHeight(this);
    }

    public Icon(String name) {
        Toolkit kit = getToolkit();
        imageSize = new Dimension(-1, -1);
        image = kit.getImage(name);
        waitForSize();
        imageSize.width = image.getWidth(this);
        imageSize.height = image.getHeight(this);
        setName(name);
    }

    public Icon(URL url) {
        Toolkit kit = getToolkit();
        imageSize = new Dimension(-1, -1);
        image = kit.getImage(url);
        waitForSize();
        imageSize.width = image.getWidth(this);
        imageSize.height = image.getHeight(this);
    }

    // --- Component methods ----------------------------------------------------

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public Dimension getPreferredSize() {
        return imageSize;
    }

    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    public void paint(Graphics g) {
        if (image != null) {
            Dimension size = getSize();
            g.drawImage(image, 0, 0, size.width, size.height, this);
        }
    }

    // --- local methods ---------------------------------------------

    /**
     * provides sychronization with the determination of the image size.
     */
    protected void waitForSize() {
        MediaTracker tracker = new MediaTracker(this);
        tracker.addImage(image, 0);
        try {
            tracker.waitForID(0);
        } catch (InterruptedException e) {
        }
    }

    protected Dimension getImageSize() {
        return imageSize;
    }

    // --- member variables ------------------------------------------

    protected Image image;
    private Dimension imageSize;
}
