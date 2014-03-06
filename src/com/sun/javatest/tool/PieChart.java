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
package com.sun.javatest.tool;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

/**
 * Component which draws a 2D pie chart with a drop shadow, based on supplied
 * data.
 */
public class PieChart extends JComponent /* implements Accessible*/ {
    /**
     * Create an empty pie chart.  Using this constructor is not recommended.
     */
    private PieChart() {
    }

    /**
     * Constructs a pie chart with the given initial distribution and colors.
     *
     * @param dist the numerical values to be represented in the pie.  it is
     *        assumed that the sum of these numbers equals 100% of the pie.
     * @param colors The colors that should be used to paint each of the pie
     *        slices, positionally corresponding to the values in the first
     *        argument.
     * @throws IllegalArgumentException If the length of the first parameter
     *         does not equal the second.
     * @throws NullPointerException if either argument is null
     * @see #setValue
     */
    public PieChart(int[] dist, Color[] colors) {
        this();
        setValue(dist, colors);
    }

    // could provide methods which take percentage, floats, floats, long, etc...

    /**
     * Sets the pie chart to the given distribution and corresponding colors.
     *
     * @param dist the numerical values to be represented in the pie.  it is
     *        assumed that the sum of these numbers equals 100% of the pie.
     * @param c The colors that should be used to paint each of the pie slices,
     *          positionally corresponding to the values in the first argument.
     * @throws IllegalArgumentException If the length of the first parameter
     *         does not equal the second.
     * @throws NullPointerException if either argument is null
     */
    public void setValue(int[] dist, Color[] c) {
        if (dist.length != c.length)
            throw new IllegalArgumentException();

        colors = c;
        slices = dist;
        repaint();
    }

    protected void paintComponent(Graphics g) {
        Image image = new BufferedImage(getWidth(), getHeight(),
                                  BufferedImage.TYPE_INT_ARGB);
        Graphics2D imageG = (Graphics2D)image.getGraphics();
        paintPie(imageG);
        imageG.dispose();

        g.drawImage(image, 0, 0, null);
        g.dispose();
    }

    private void paintPie(Graphics2D g2d) {
        if (isOpaque()) {
            g2d.setColor(getBackground());
            g2d.fillRect(0,0, getWidth(), getHeight());
        }

        if (slices == null || slices.length == 0)
            return;

        // now paint pie slices
        float sum = 0.0f;
        int minIndex = 0, maxIndex = 0;
        for (int i = 0; i < slices.length; i++) {
            sum += slices[i];
            if (slices[i] > slices[maxIndex])
                maxIndex = i;
            if (slices[i] > slices[minIndex])
                minIndex = i;
        }

        // do not paint shadow if there is no pie
        if (sum < 0.1f)     // floating point inaccuracy...
            return;

        // perhaps use create with a clip that excludes the border area??
        Insets inset = getInsets();

        // this is critical - pie can look ugly without
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

        int maxDia = Math.min((getWidth() - inset.left - inset.right),
                              (getHeight() - inset.top - inset.bottom));
        float targetDia = (float)maxDia * 0.8f;


        // coords of point defining the box which defines the arcs
        float boxX = inset.left + (0.1f  * (maxDia - targetDia));

        float boxY = inset.top + (0.1f  * (maxDia - targetDia));
        if (getWidth() > getHeight()) {
        }
        else {
            // vertical centering
            boxY = ((float)(getHeight() / 2)) - (0.5f * targetDia);
        }

        // center of gradient circle
        float gradx = boxX + OFFSETX;
        float grady = boxY + OFFSETY;

        // the effectiveness of the gradient will be determined by the fg and
        // bg colors, combined with whether or not this component is opaque
        /*
        GradientPaint grad = new GradientPaint(
                                        (float)(gradx-xshift),
                                        (float)(grady-yshift),
                                        Color.black,
                                        (float)(gradx+xshift),
                                        (float)(grady+yshift),
                                        getBackground());
        */
        GradientPaint grad = new GradientPaint(
                                        0.0f, 0.0f,
                                        Color.decode("#dddddd"),
                                        (float)getWidth(), (float)getHeight(),
                                        getBackground());
        g2d.setPaint(grad);
        g2d.fill(new Ellipse2D.Float(gradx, grady,
                                     targetDia*0.98f, targetDia*0.98f));

        float[] dist = new float[slices.length];
        for (int i = 0; i < slices.length; i++) {
            if (slices[i] == 0)
                dist[i] = 0.0f;
            else if (Math.abs( slices[i] - sum) < .0000001) // slices[i] == sum
                dist[i] = 360.0f;
            else
                dist[i] = 360.0f * (((float)slices[i]) / sum);
        }

        // post process to provide slices of minimal viewable size
        for (int i = 0; i < dist.length; i++) {
            if (dist[i] > 0.0f && dist[i] < MIN_SLICE) {
                dist[maxIndex] = dist[maxIndex] - (MIN_SLICE - dist[i]);
                // XXX should recalculate maxIndex
                dist[i] = MIN_SLICE;
            }
        }

        float pos = 90.0f;      // position on circle
        for (int i = 0; i < slices.length; i++) {
            if (dist[i] > 0.0) {
                g2d.setPaint(colors[i]);
                g2d.fill(new Arc2D.Float(boxX, boxY, targetDia, targetDia,
                                         pos, -1.0f*dist[i], Arc2D.PIE));

                // special outline for white slices
                if (colors[i].equals(Color.WHITE)) {
                    g2d.setPaint(Color.BLACK);

                    if (dist[i] == 360.0f)
                        g2d.draw(new Ellipse2D.Float(boxX, boxY, targetDia, targetDia));
                    else
                        g2d.draw(new Arc2D.Float(boxX, boxY, targetDia, targetDia,
                                                 pos, -1.0f*dist[i], Arc2D.PIE));
                }
                else { }

                pos -= dist[i];
            }
        }   // for

    }

    private int[] slices;
    private Color[] colors;
    private static final int OFFSETX = 7;
    private static final int OFFSETY = 7;
    private static final float MIN_SLICE = 2.0f;            // min. of 3 degrees
}
