/*
 * $Id$
 *
 * Copyright (c) 2002, 2010, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.help.HelpBroker;
import javax.help.JHelpContentViewer;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * A lightweight component that will activate online help, suitable for
 * use in More Info help panes.
 * To use this component in More Info, use the following: <pre>
 *    &lt;object classid="java:com.sun.javatest.tool.HelpLink"&gt;
 *    &lt;param  name=... value=...&gt;
 *    ...
 *    &lt;/object&gt;
 * </pre>
 * The following parameters are recognized:
 * <dl>
 * <dt>target
 * <dd>the help ID that will be displayed when the link is activated
 * <dt>text
 * <dd>the text string that will be displayed as the body of the link
 * <dt>textFont
 * <dd>the font used to display the link
 * <dt>textColor
 * <dd>the color used to display the link
 * </dl>
 * @see HelpLinkBeanInfo
 * @see HelpExternalLink
 */
public class HelpLink extends JComponent implements Accessible
{
    /**
     * Create a HelpLink object.
     */
    public HelpLink() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JHelpContentViewer cv = (JHelpContentViewer) SwingUtilities.getAncestorOfClass(JHelpContentViewer.class, e.getComponent());
                HelpBroker hb = (HelpBroker) (cv.getClientProperty(HELPBROKER_FOR_HELPLINK));
                hb.setCurrentID(target);
                hb.setDisplayed(true);
            }
        });
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * A constructor used to specify another MouseListener by subclasses
     * @param o fake param - not used
     */
    protected HelpLink(Object o) {
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Get the accessible context for this pane.
     * @return the accessible context for this pane
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null)
            accessibleContext = new AccessibleJComponent() { };
        return accessibleContext;
    }

    public Dimension getPreferredSize() {
        Graphics g = getGraphics();
        if (g == null)
            return new Dimension(100, 10);

        FontMetrics fm = (font == null ? g.getFontMetrics() : g.getFontMetrics(font));
        int w = fm.stringWidth(text);
        int h = fm.getHeight();
        return new Dimension(w, h);
    }

    public void paintComponent(Graphics g) {
        g.setColor(color);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int baseLine = fm.getLeading() + fm.getMaxAscent();
        g.drawString(text, 0, baseLine);
        g.drawLine(0, baseLine + 1, fm.stringWidth(text) - 1, baseLine + 1);
    }

    /**
     * Get the help ID that will be displayed when the link is activated.
     * @return the help ID that will be displayed when the link is activated
     * @see #setTarget
     */
    public String getTarget() {
        return target;
    }

    /**
     * Set the help ID that will be displayed when the link is activated.
     * The ID must be set before the link is displayed.
     * @param target the help ID that will be displayed when the link is activated
     * @see #getTarget
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * Get the text string that will be displayed as the body of the link.
     * @return the text string that will be displayed as the body of the link
     * @see #setText
     */
    public String getText() {
        return text;
    }

    /**
     * Set the text string that will be displayed as the body of the link.
     * The text must be set before the link is displayed.
     * @param text the text string that will be displayed as the body of the link
     * @see #getText
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Get the font that will be used to display the link.
     * The default font is inherited from the context in which the link appears.
     * @return the name of the font that will be used to display the link
     * @see #setTextFont
     */
    public String getTextFont() {
        return (font == null ? null : font.toString());
    }

    /**
     * Set the font that will be used to display the link.
     * @param font the name of font that will be used to display the link
     * @see #getTextFont
     */
    public void setTextFont(String font) {
        this.font = Font.decode(font);
    }

    /**
     * Get the color that will be used to display the link.
     * The default color is blue.
     * @return a string giving the color that will be used to display the link
     * @see #setTextColor
     */
    public String getTextColor() {
        return color.toString();
    }

    /**
     * Set the color that will be used to display the link.
     * @param color the name of color that will be used to display the link
     * @see #getTextColor
     */
    public void setTextColor(String color) {
        this.color = Color.decode(color);
    }


    private String target;
    private String text = "link";
    private Font font;
    private Color color = Color.blue;

    /**
     * The name of a property that must be set on the JHelpContentViewer that
     * identifies the HelpBroker that is updated when the link is activated.
     */
    public static final String HELPBROKER_FOR_HELPLINK = "helpBrokerForHelpLink";
}
