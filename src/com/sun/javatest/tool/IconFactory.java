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
package com.sun.javatest.tool;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.Icon;

import com.sun.javatest.Status;
import com.sun.javatest.util.I18NResourceBundle;

/**
 * A factory for standard JT Harness icons.
 * Note: some of this code is based upon the Swing icon factory for
 * the "metal" look and feel.
 */
public class IconFactory
{
    //----------------------------------------------------------------------
    //
    // File chooser and generic tree icons

    /**
     * Get an icon for a file.
     * @return an icon for afile
     */
    public static Icon getFileIcon() {
        if (file == null) {
            file = new FileIcon16();
        }
        return file;
    }

    /**
     * Get an icon for a file.
     * @return an icon for a report
     */
    public static Icon getReportIcon() {
        if (report == null) {
            report = new ReportIcon16();
        }
        return report;
    }


    /**
     * Get an icon for a folder.
     * @return an icon for a folder
     */
    public static Icon getFolderIcon() {
        return getOpenableFolderIcon();
    }

    /**
     * Get an icon for a parent folder.
     * @return an icon for a parent folder
     */
    public static Icon getUpFolderIcon() {
        if (upFolder == null)
            upFolder = new UpFolderIcon16();
        return upFolder;
    }

    /**
     * Get an icon for an openable (traversable) folder.
     * @return an icon for an openable (traversable) folder
     */
    public static Icon getOpenableFolderIcon() {
        if (openableFolder == null)
            openableFolder = new FolderIcon16(ALL_FILLED);
        return openableFolder;
    }

    /**
     * Get an icon for an selectable (non-traversable) folder.
     * @return an icon for an selectable (non-traversable) folder
     */
    public static Icon getSelectableFolderIcon() {
        if (selectableFolder == null)
            selectableFolder = new FolderIcon16(NOT_FILLED);
        return selectableFolder;
    }

    private static Icon file;
    private static Icon report;
    private static Icon openableFolder;
    private static Icon selectableFolder;
    private static Icon upFolder;

    private static final int NOT_FILLED = 0;
    private static final int PARTIALLY_FILLED = 1;
    private static final int ALL_FILLED = 2;

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(IconFactory.class);


    private static class FolderIcon16 implements Icon {
        FolderIcon16(int style) {
            this.style = style;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (image == null) {
                image = new BufferedImage(getIconWidth(), getIconHeight(),
                                          BufferedImage.TYPE_INT_ARGB);
                Graphics imageG = image.getGraphics();
                paintMe(c,imageG);
                imageG.dispose();

            }
            g.drawImage(image, x, y+getShift(), null);
        }


        private void paintMe(Component c, Graphics g) {

            int right = folderIcon16Size.width - 1;
            int bottom = folderIcon16Size.height - 1;

            // Draw tab top
            g.setColor( UIFactory.Colors.PRIMARY_CONTROL_DARK_SHADOW.getValue() );
            g.drawLine( right - 5, 3, right, 3 );
            g.drawLine( right - 6, 4, right, 4 );

            // Draw folder front
            g.setColor( UIFactory.Colors.PRIMARY_CONTROL.getValue() );
            g.fillRect( 2, 7, 13, 8 );

            // Additions/changes to standard Metal icons
            switch (style) {
            case NOT_FILLED:
                g.setColor( Color.white );
                g.fillRect( 2, 7, 13, 8 );
                break;

            case PARTIALLY_FILLED:
                g.setColor( Color.white );
                for (int i = 0; i < 8; i++)
                    g.drawLine(4 + i, 7 + i, 18, 7 + i);
                break;

            case ALL_FILLED:
                break;
            }

            // Draw tab bottom
            g.setColor( UIFactory.Colors.PRIMARY_CONTROL_SHADOW.getValue() );
            g.drawLine( right - 6, 5, right - 1, 5 );

            // Draw outline
            g.setColor( UIFactory.Colors.PRIMARY_CONTROL_INFO.getValue() );
            g.drawLine( 0, 6, 0, bottom );            // left side
            g.drawLine( 1, 5, right - 7, 5 );         // first part of top
            g.drawLine( right - 6, 6, right - 1, 6 ); // second part of top
            g.drawLine( right, 5, right, bottom );    // right side
            g.drawLine( 0, bottom, right, bottom );   // bottom

            // Draw highlight
            g.setColor( UIFactory.Colors.PRIMARY_CONTROL_HIGHLIGHT.getValue() );
            g.drawLine( 1, 6, 1, bottom - 1 );
            g.drawLine( 1, 6, right - 7, 6 );
            g.drawLine( right - 6, 7, right - 1, 7 );

        }

        public int getShift() { return 0; }
        public int getAdditionalHeight() { return 0; }

        public int getIconWidth() { return folderIcon16Size.width; }
        public int getIconHeight() { return folderIcon16Size.height + getAdditionalHeight(); }

        private int style;
        private Image image;
    }

    private static final Dimension folderIcon16Size = new Dimension( 16, 16 );


    private static class TreeFolderIcon extends FolderIcon16 {
        TreeFolderIcon(int style) {
            super(style);
        }

        public int getShift() { return -1; }
        public int getAdditionalHeight() { return 2; }
    }

    // File Chooser Up Folder code
    private static class UpFolderIcon16 implements Icon {
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.translate(x, y);

            // Fill background
            g.setColor(UIFactory.Colors.PRIMARY_CONTROL.getValue());
            g.fillRect(3,5, 12,9);

            // Draw outside edge of folder
            g.setColor(UIFactory.Colors.PRIMARY_CONTROL_INFO.getValue());
            g.drawLine(1,6,    1,14); // left
            g.drawLine(2,14,  15,14); // bottom
            g.drawLine(15,13, 15,5);  // right
            g.drawLine(2,5,    9,5);  // top left
            g.drawLine(10,6,  14,6);  // top right
            // Draw the UP arrow
            //     same color as edge
            g.drawLine(8,13,  8,16); // arrow shaft
            g.drawLine(8, 9,  8, 9); // arrowhead top
            g.drawLine(7,10,  9,10);
            g.drawLine(6,11, 10,11);
            g.drawLine(5,12, 11,12);

            // Draw inner folder highlight
            g.setColor(UIFactory.Colors.PRIMARY_CONTROL_HIGHLIGHT.getValue());
            g.drawLine( 2,6,  2,13); // left
            g.drawLine( 3,6,  9,6);  // top left
            g.drawLine(10,7, 14,7);  // top right

            // Draw tab on folder
            g.setColor(UIFactory.Colors.PRIMARY_CONTROL_DARK_SHADOW.getValue());
            g.drawLine(11,3, 15,3); // top
            g.drawLine(10,4, 15,4); // bottom

            g.translate(-x, -y);
        }

        public int getIconWidth() {
            return 18;
        }

        public int getIconHeight() {
            return 18;
        }
    }  // End class FileChooserUpFolderIcon




    private static class FileIcon16 implements Icon {

        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (image == null) {
                image = new BufferedImage(getIconWidth(), getIconHeight(),
                                          BufferedImage.TYPE_INT_ARGB);
                Graphics imageG = image.getGraphics();
                paintMe(c,imageG);
                imageG.dispose();

            }
            g.drawImage(image, x, y+getShift(), null);

        }

        protected void paintMe(Component c, Graphics g) {

                int right = fileIcon16Size.width - 1;
                int bottom = fileIcon16Size.height - 1;

                // Draw fill
                g.setColor( UIFactory.Colors.WINDOW_BACKGROUND.getValue() );
                g.fillRect( 4, 2, 9, 12 );

                // Draw frame
                g.setColor( UIFactory.Colors.PRIMARY_CONTROL_INFO.getValue() );
                g.drawLine( 2, 0, 2, bottom );                 // left
                g.drawLine( 2, 0, right - 4, 0 );              // top
                g.drawLine( 2, bottom, right - 1, bottom );    // bottom
                g.drawLine( right - 1, 6, right - 1, bottom ); // right
                g.drawLine( right - 6, 2, right - 2, 6 );      // slant 1
                g.drawLine( right - 5, 1, right - 4, 1 );      // part of slant 2
                g.drawLine( right - 3, 2, right - 3, 3 );      // part of slant 2
                g.drawLine( right - 2, 4, right - 2, 5 );      // part of slant 2

                // Draw highlight
                g.setColor( UIFactory.Colors.PRIMARY_CONTROL.getValue() );
                g.drawLine( 3, 1, 3, bottom - 1 );                  // left
                g.drawLine( 3, 1, right - 6, 1 );                   // top
                g.drawLine( right - 2, 7, right - 2, bottom - 1 );  // right
                g.drawLine( right - 5, 2, right - 3, 4 );           // slant
                g.drawLine( 3, bottom - 1, right - 2, bottom - 1 ); // bottom

        }

        public int getShift() { return 0; }
        public int getAdditionalHeight() { return 0; }

        public int getIconWidth() { return fileIcon16Size.width; }
        public int getIconHeight() { return fileIcon16Size.height + getAdditionalHeight(); }

        private Image image;
    }

    private static class ReportIcon16 extends FileIcon16 {
        protected void paintMe(Component c, Graphics g) {
            int [][] blacks = {{},{},{5,7,8,9}, {}, {5,6,7,9}, {}, {5,6,7,9,10,11},
                               {}, {5, 7,8,9,10,11}, {}, {5, 7,8,9,10,11}, {},
                               {5,6,7,8,9,10,11}};
            super.paintMe(c,g);
            // paint the "text"
            g.setColor( UIFactory.Colors.PRIMARY_CONTROL_INFO.getValue() );
            for (int i = 0; i < blacks.length; i++) {
                for (int j=0; j < blacks[i].length; j++) {
                    int x = blacks[i][j];
                    g.drawLine(x, i, x, i );
                }
            }
        }

    }

    private static final Dimension fileIcon16Size = new Dimension( 16, 16 );


    private static class TreeLeafIcon extends FileIcon16 {
        public int getShift() { return 2; }
        public int getAdditionalHeight() { return 4; }
    }

    /**
     * A convenience redefinition of {@link Status#PASSED Status.PASSED}.
     */
    public static final int PASSED = Status.PASSED;

    /**
     * A convenience redefinition of {@link Status#FAILED Status.FAILED}.
     */
    public static final int FAILED = Status.FAILED;

    /**
     * A convenience redefinition of {@link Status#ERROR Status.ERROR}.
     */
    public static final int ERROR  = Status.ERROR;

    /**
     * A convenience redefinition of {@link Status#NOT_RUN Status.NOT_RUN}.
     */
    public static final int NOT_RUN = Status.NOT_RUN;

    /**
     * A constant indicating that as icon should be represented as "filtered out".
     */
    public static final int FILTERED_OUT = Status.NUM_STATES;

    /**
     * A constant indicating the number of different value "state" values.
     */
    public static final int NUM_STATES = FILTERED_OUT + 1;

    //----------------------------------------------------------------------
    //
    // Test Tree and Test Result icons

    /**
     * Get a test icon.
     * @param state the state for this test: one of
     * {@link #PASSED}, {@link #FAILED}, {@link #ERROR}, {@link #NOT_RUN}, {@link #FILTERED_OUT}
     * @param active whether the icon should indicate current activity or not
     * @param glyph whether the icon should contain an accessibility glyph or not
     * @return a test icon appropriate to the arguments
     */
    public static Icon getTestIcon(int state, boolean active, boolean glyph) {
        if (state < 0 || state >= NUM_STATES)
            throw new IllegalArgumentException(Integer.toString(state));

        int index = 4 * state + (2 * (active ? 1 : 0)) + (glyph ? 1 : 0);
        Icon testIcon = testIcons[index];
        if (testIcon == null) {
            testIcon = new TestIcon(state, active, glyph);
            testIcons[index] = testIcon;
        }
        return testIcon;
    }

    private static Icon[] testIcons = new Icon[NUM_STATES * 2 * 2];

    /**
     * Get a test folder icon.
     * @param state the state for this test: one of
     * {@link #PASSED}, {@link #FAILED}, {@link #ERROR}, {@link #NOT_RUN}, {@link #FILTERED_OUT}
     * @param active whether the icon should indicate current activity or not
     * @param glyph whether the icon should contain an accessibility glyph or not
     * @return a test folder icon appropriate to the arguments
     */
    public static Icon getTestFolderIcon(int state, boolean active, boolean glyph) {
        if (state < 0 || state >= NUM_STATES)
            throw new IllegalArgumentException(Integer.toString(state));

        int index = 4 * state + (2 * (active ? 1 : 0)) + (glyph ? 1 : 0);
        Icon testFolderIcon = testFolderIcons[index];
        if (testFolderIcon == null) {
            testFolderIcon = new TestFolderIcon(state, active, glyph);
            testFolderIcons[index] = testFolderIcon;
        }
        return testFolderIcon;
    }

    private static Icon[] testFolderIcons = new Icon[NUM_STATES * 2 * 2];

    /**
     * Get a test section icon.
     * @param state the state for this test: one of
     * {@link #PASSED}, {@link #FAILED}, {@link #ERROR}, {@link #NOT_RUN}, {@link #FILTERED_OUT}
     * @return a test section icon appropriate to the arguments
     */
    public static Icon getTestSectionIcon(int state) {
        if (state < 0 || state >= NUM_STATES)
            throw new IllegalArgumentException(Integer.toString(state));

        int index = state;
        Icon testSectionIcon = testSectionIcons[index];
        if (testSectionIcon == null) {
            testSectionIcon = new TestSectionIcon(state);
            testSectionIcons[index] = testSectionIcon;
        }
        return testSectionIcon;
    }

    private static Icon[] testSectionIcons = new Icon[NUM_STATES];

    // the following constants apply to tests and testFolders
    static int testIconWidth = 20;
    static int testIconHeight = 16;
    static int lightX = 5;
    static int lightY = 1;
    static int arrowWidth = 7;
    static int arrowHeight = 8;
    // the following constants are for test icons
    static int testImageWidth = 11;
    static int testImageHeight = 15;
    static int testCornerSize = 4;
    // the following constants are for testFolders
    static int testFolderImageWidth = 16;
    static int testFolderImageHeight = 13;
    static int testFolderTabWidth = 8;
    static int testFolderTabHeight = 2;
    // the following constants are for testSection icons
    static int sectIconWidth = 16;
    static int sectIconHeight = 16;
    static int sectImageSize = 11;
    static int sectHighlightSize = 4;

    private static final Color semiWhite = new Color(255, 255, 255, 128);
    private static Color arrowColor = UIFactory.Colors.PRIMARY_CONTROL.getValue();
    private static Color arrowShadowColor = UIFactory.Colors.PRIMARY_CONTROL_SHADOW.getValue();

    private static void drawArrow(int x, int y, int w, int h, Graphics g) {
        drawArrow(x + 1, y + 1, w, h, g, arrowColor);
        drawArrow(x, y, w, h, g, arrowShadowColor);
    }

    private static void drawArrow(int x, int y, int w, int h, Graphics g, Color c) {
        g.setColor(c);
        int[] xx = { x, x + w, x };
        int[] yy = { y, y + h / 2, y + h };
        g.fillPolygon(xx, yy, 3);
    }

    private static void drawGlyph(int state, int x, int y, Graphics g) {
        drawGlyph(state, x + 1, y + 1, g, semiWhite);
        drawGlyph(state, x, y, g, Color.black);
    }

    private static void drawGlyph(int state, int x, int y, Graphics g, Color c) {
        g.setColor(c);
        switch (state) {
        case PASSED:
            g.drawLine(x + 0, y + 3, x + 2, y + 5);
            g.drawLine(x + 1, y + 3, x + 2, y + 4);
            g.drawLine(x + 2, y + 4, x + 5, y + 1);
            g.drawLine(x + 2, y + 5, x + 5, y + 2);
            break;

        case FAILED:
            g.drawLine(x + 0, y + 0, x + 4, y + 4);
            g.drawLine(x + 0, y + 1, x + 4, y + 5);
            g.drawLine(x + 0, y + 4, x + 4, y + 0);
            g.drawLine(x + 0, y + 5, x + 4, y + 1);
            break;

        case ERROR:
            g.drawLine(x + 2, y + 0, x + 2, y + 3);
            g.drawLine(x + 2, y + 5, x + 2, y + 5);
            g.drawLine(x + 3, y + 0, x + 3, y + 3);
            g.drawLine(x + 3, y + 5, x + 3, y + 5);
            break;

        case NOT_RUN:
            g.drawLine(x + 1, y + 3, x + 4, y + 3);
            break;
        }
    }

    private static void fill(BufferedImage image, int x1, int y1, int x2, int y2, Color refColor) {
        // Fill a region, varying the brightness according to the distance from a light source.
        // For simplicity, a linear grading is used. At the light source, the color is set to white,
        // which means saturation is 0 and brightness is 1.
        // At a reference point, the brightness is as given by the color refColor.
        // So, the brightness for a point is determined by the equation
        //      b = mb d + cb
        // where b is brightness, d is distance from light, and mb and cb are constants
        // From the preceding constraints, you can determine
        //      cb = 1.0
        //      mb = (b0 - cb) / d0
        // where b0 is brightness at reference point and d0 is distance of reference
        // point from light
        // Ditto
        //      s = ms d + cs
        //      cs = 0
        //      ms = (s0 - cs) / d0

        // calculate distance of reference point from light source
        int refX = testIconWidth;
        int refY = testIconHeight;
        int dRefX = refX - lightX;
        int dRefY = refY - lightY;
        float d0 = (float) (Math.sqrt(dRefX * dRefX + dRefY * dRefY));

        // calculate hsb at reference point
        int refRed =  refColor.getRed();
        int refGreen = refColor.getGreen();
        int refBlue = refColor.getBlue();
        float[] refHSB = Color.RGBtoHSB(refRed, refGreen, refBlue, null);
        float s0 = refHSB[1];
        float b0 = refHSB[2];

        // calculate gradient parameters
        float cb = 1.0f;
        float mb = (b0 - cb) / d0;

        float cs = 0.0f;
        float ms = (s0 - cs) / d0;

        // do the fill
        for (int x = x1; x < x2; x++) {
            for (int y = y1; y < y2; y++) {
                int dx = x - lightX;
                int dy = y - lightY;
                float d = (float) (Math.sqrt(dx * dx + dy * dy));
                float s = ms * d + cs;
                float b = mb * d + cb;
                int rgb = Color.HSBtoRGB(refHSB[0], s, b);
                image.setRGB(x, y, rgb);
            }
        }
    }

    private static class TestIcon implements Icon {
        TestIcon(int state, boolean active, boolean glyph) {
            this.state = state;
            this.active = active;
            this.glyph = glyph;
        }

        public int getIconWidth() {
            return testIconWidth;
        }

        public int getIconHeight() {
            return testIconHeight;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (image == null) {
                image = new BufferedImage(getIconWidth(), getIconHeight(),
                                          BufferedImage.TYPE_INT_ARGB);
                paintMe(image);

            }
            g.drawImage(image, x, y, null);
        }

        private void paintMe(BufferedImage image) {
            Graphics g = image.getGraphics();
            Color baseColor = I18NUtils.getColorForState(state);

            int left = 2 + (testIconWidth - testImageWidth) / 2;
            int right = left + testImageWidth - 1;
            int top = (testIconHeight - testImageHeight) / 2;
            int bottom = top + testImageHeight - 1;

            int cornerLeft = right - testCornerSize;

            // border
            Color borderColor = baseColor.darker().darker();
            g.setColor(borderColor);
            g.drawLine(left, top, cornerLeft, top); // top
            g.drawLine(left, top, left, bottom - 1);  // left
            g.drawLine(left, bottom - 1, right - 1, bottom - 1); // bottom
            g.drawLine(right -1, top + testCornerSize, right - 1, bottom - 1); // right
            for (int i = 0; i < testCornerSize; i++)
                g.drawLine(cornerLeft, top + i, cornerLeft + i, top + i);

            // body
            fill(image, left + 1, top + 1, cornerLeft, top + testCornerSize, baseColor);
            fill(image, left + 1, top + testCornerSize, right - 1, bottom - 1, baseColor);

            // glyph
            if (glyph)
                drawGlyph(state, (left + right) / 2 - 3, (top + bottom) / 2 - 3, g);

            // active
            if (active)
                drawArrow(0, (testIconHeight - arrowHeight) / 2, arrowWidth, arrowHeight, g);

            g.dispose();
        }

        private int state;
        private boolean active;
        private boolean glyph;
        private BufferedImage image;
    }

    private static class TestFolderIcon implements Icon {
        TestFolderIcon(int state, boolean active, boolean glyph) {
            this.state = state;
            this.active = active;
            this.glyph = glyph;
        }

        public int getIconWidth() {
            return testIconWidth;
        }

        public int getIconHeight() {
            return testIconHeight;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (image == null) {
                image = new BufferedImage(getIconWidth(), getIconHeight(),
                                          BufferedImage.TYPE_INT_ARGB);
                paintMe(image);

            }
            g.drawImage(image, x, y, null);
        }

        private void paintMe(BufferedImage image) {
            Graphics g = image.getGraphics();
            Color baseColor = I18NUtils.getColorForState(state);

            int left = 2 + (testIconWidth - testFolderImageWidth) / 2;
            int right = left + testFolderImageWidth - 1;
            int top = (testIconHeight - testFolderImageHeight) / 2;
            int bottom = top + testFolderImageHeight - 1;

            // border
            Color borderColor = baseColor.darker().darker();
            g.setColor(borderColor);
            g.drawRect(left, top + testFolderTabHeight, right - left, bottom - top - testFolderTabHeight);

            // body
            fill(image, left + 1, top + testFolderTabHeight + 1, right, bottom, baseColor);

            // tab
            g.setColor(borderColor);
            for (int i = 0; i < testFolderTabHeight; i++) {
                int fth_i = testFolderTabHeight - i;
                g.drawLine(right - testFolderTabWidth + fth_i, top + i, right - fth_i, top + i);
                g.drawLine(right - testFolderTabWidth + i, top + testFolderTabHeight + i, right, top + testFolderTabHeight + i);
            }

            // glyph
            if (glyph)
                drawGlyph(state, (left + right) / 2 - 3, testFolderTabHeight + 1 + (top - testFolderTabHeight + bottom) / 2 - 3, g);

            // active
            if (active)
                drawArrow(0, (testIconHeight - arrowHeight) / 2, arrowWidth, arrowHeight, g);

            g.dispose();
        }

        private int state;
        private boolean active;
        private boolean glyph;
        private BufferedImage image;
    }

    private static class TestSectionIcon implements Icon {
        TestSectionIcon(int state) {
            this.state = state;
        }

        public int getIconWidth() {
            return sectIconWidth;
        }

        public int getIconHeight() {
            return sectIconHeight;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (image == null) {
                image = new BufferedImage(getIconWidth(), getIconHeight(),
                                          BufferedImage.TYPE_INT_ARGB);
                paintMe(image);

            }
            g.drawImage(image, x, y, null);
        }

        private void paintMe(BufferedImage image) {
            Graphics g = image.getGraphics();
            Color baseColor = I18NUtils.getColorForState(state);
            Color borderColor = baseColor.darker().darker();

            int left = (sectIconWidth - sectImageSize) / 2;
            int right = left + sectImageSize;
            int top = (sectIconHeight - sectImageSize) / 2;
            int bottom = top + sectImageSize;

            g.setColor(borderColor);
            g.fillOval(left, top, right - left, bottom - top);

            g.setColor(baseColor);
            g.fillOval(left + 1, top + 1, right - left - 2, bottom - top - 2);

            int hCenterX = left + sectImageSize / 3;
            int hCenterY = top + sectImageSize / 3;
            for (int x = hCenterX - sectHighlightSize; x <= hCenterX + sectHighlightSize; x++) {
                for (int y = hCenterY - sectHighlightSize; y <= hCenterY + sectHighlightSize; y++) {
                    int dx = x - hCenterX;
                    int dy = y - hCenterY;
                    float d = (float) (Math.sqrt(dx * dx + dy * dy));
                    int t = Math.min((int) ( d * 255 / sectHighlightSize), 255);
                    g.setColor(new Color(255, 255, 255, 255 - t));
                    g.drawLine(x, y, x, y);
                }
            }

            g.dispose();
        }

        private int state;
        private BufferedImage image;
    }



}
