/*
 * $Id$
 *
 * Copyright (c) 2002, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.moreInfo;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.View;
import javax.swing.text.html.HTMLDocument;

import com.sun.java.help.impl.ViewAwareComponent;

/**
 * A lightweight component that will show a text file, suitable for
 * use in More Info help panes.
 * To use this component in More Info, use the following: <pre>
 *    &lt;object classid="java:com.sun.demotck.ShowFile"&gt;
 *    &lt;param  name=... value=...&gt;
 *    ...
 *    &lt;/object&gt;
 * </pre>
 * The following parameters are recognized:
 * <dl>
 * <dt>path
 * <dd>the resource path for the file that will be displayed when
 * the link is activated
 * <dt>startLine
 * <dd> a string that appears on the first line to be made visible
 * <dt>text
 * <dd>the text string that will be displayed as the body of the link
 * <dt>textFont
 * <dd>the font used to display the link
 * <dt>textColor
 * <dd>the color used to display the link
 * </dl>
 * @see ShowFileBeanInfo
 */
public class ShowFile
    extends JComponent
    implements ViewAwareComponent
{
    public ShowFile() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                showFile();
            }
        });
        setCursor(new Cursor(Cursor.HAND_CURSOR));
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

    public float getAlignmentY() {
        Graphics g = getGraphics();
        if (g == null)
            return super.getAlignmentY();

        FontMetrics fm = (font == null ? g.getFontMetrics() : g.getFontMetrics(font));
        float ascent = fm.getMaxAscent();
        float height = fm.getHeight();
        return (ascent / height);
    }

    public void paintComponent(Graphics g) {
        g.setColor(color);
        if (font != null)
            g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int baseLine = fm.getMaxAscent();
        g.drawString(text, 0, baseLine);
        g.drawLine(0, baseLine + 1, fm.stringWidth(text) - 1, baseLine + 1);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStartLine() {
        return startLine;
    }

    public void setStartLine(String startLine) {
        this.startLine = startLine;
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
     * @param fontName the name of font that will be used to display the link
     * @see #getTextFont
     */
    public void setTextFont(String fontName) {
        font = Font.decode(fontName);

        Graphics g = getGraphics();
        if (g == null)
            return;

        FontMetrics fm = (font == null ? g.getFontMetrics()
                          : g.getFontMetrics(font));
        int w = fm.stringWidth(text);
        int h = fm.getHeight();
        setPreferredSize(new Dimension(w, h));
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

    public void setViewData(View v) {
        view = v;
    }

    private synchronized void showFile() {
        if (frame == null) {
            Toolkit t = Toolkit.getDefaultToolkit();
            Dimension screenSize = t.getScreenSize();
            int dpi = t.getScreenResolution();

            frame = new JFrame();
            if (title != null)
                frame.setTitle(title);

            textArea = new JTextArea();
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            textArea.setLineWrap(false);

            scrollPane = new JScrollPane(textArea);
            frame.setContentPane(scrollPane);

            Color highlightColor = new Color(0xffffb0);
            highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(highlightColor);

            int w = DEFAULT_WIDTH * dpi;
            int h = DEFAULT_HEIGHT * dpi;
            frame.setBounds(screenSize.width - w, 0, w, h);
        }

        try {
            Document d = view.getDocument();
            URL base = ((HTMLDocument) d).getBase();
            URL p = new URL(base, path);
            BufferedReader r = new BufferedReader(new InputStreamReader(p.openStream()));
            StringBuffer sb = new StringBuffer();
            char[] buf = new char[1024];
            int n;
            while ((n = r.read(buf, 0, buf.length)) != -1)
                sb.append(buf, 0, n);
            r.close();
            textArea.setText(sb.toString());
        }
        catch (IOException e) {
            textArea.setText(e.toString());
        }

        textArea.setSize(textArea.getPreferredSize());
        frame.validate();

        String text = textArea.getText();
        int startLineIndex = (startLine == null ? -1 : text.indexOf(startLine));
        if (startLineIndex == -1)
            textArea.setCaretPosition(0);
        else {
            try {
                final int start = getStartOfLine(text, startLineIndex);
                int end = getEndOfLine(text, startLineIndex);
                Highlighter h = textArea.getHighlighter();
                h.removeAllHighlights();
                h.addHighlight(start, end + 1, highlightPainter);

                textArea.setCaretPosition(start);

                Rectangle startRect = textArea.modelToView(start);
                JViewport vp = scrollPane.getViewport();
                int yOffset = 50;

                if (vp != null && startRect != null) {
                    vp.setViewPosition(new Point(startRect.x,
                                                 Math.max(0, startRect.y - yOffset)));
                }
            }
            catch (BadLocationException e) {
                textArea.setCaretPosition(0);
            }
        }

        frame.setVisible(true);
    }

    int getStartOfLine(String text, int index) {
        int i = index;
        while (i > 0 && text.charAt(i - 1) != '\n')
            i--;
        return i;
    }

    int getEndOfLine(String text, int index) {
        int i = index;
        while (i < text.length() - 1 && text.charAt(i + 1) != '\n')
            i++;
        return i;
    }

    private String title;
    private String path;
    private String startLine;
    private String text = "link";
    private Font font;
    private Color color = Color.blue;
    private View view;

    private static JFrame frame;
    private static JScrollPane scrollPane;
    private static JTextArea textArea;
    private static Highlighter.HighlightPainter highlightPainter;
    private static final int DEFAULT_WIDTH = 5;
    private static final int DEFAULT_HEIGHT = 7;
}
