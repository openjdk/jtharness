/*
 * $Id$
 *
 * Copyright (c) 2008, 2013, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.report.HTMLWriterEx;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.RepaintManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.text.rtf.RTFEditorKit;

/**
 * A panel to display files which may be linked together by hyperlinks.
 * This panel can also display a list of local files.  The type of the file
 * to be shown is identified if possible and the display panel put into the
 * right mode to show an image, rendered HTML, etc.  If the file is not known,
 * the user may be prompted to display the file as raw text.
 */
public class MultiFormatPane extends JPanel implements Printable {

    public MultiFormatPane(UIFactory uif) {
        this.uif = uif;

        listener = new Listener();
        initGUI();

        modesToPanes = new HashMap();

        addMediaPane(TEXT, textPane);
        addMediaPane(AUDIO, musicPane);
        addMediaPane(IMAGE, imagePane);
        addMediaPane(ERROR, errorPane);

    }

    public void setNoteField(JTextField noteField) {
        this.noteField = noteField;
    }

    public void setNavigationPane(NavigationPane navPane) {
        this.navPane = navPane;
    }

    public void addMediaPane(int mode, MediaPane pane) {
        modesToPanes.put(mode, pane);
    }

    public MediaPane getMediaPane(int mode) {
        return (MediaPane)modesToPanes.get(mode);
    }

    public void clear() {
        currURL = null;
        if(navPane != null) {
            navPane.clear();
        }
        musicPane.stopAudio();
    }

    public URL getPage() {
        return currURL;
    }

    public int getCurrentMode() {
        return getCurrentPane().getMode();
    }

    public MediaPane getCurrentPane() {
        return (MediaPane)scrllBody.getViewport().getComponent(0);
    }

    // Base directory is root directory for displayable content
    // if it set, all filenames will be relative to this directory
    void setBaseDirectory(File base) {
        baseDir = base;
    }

    File getBaseDirectory() {
        return baseDir;
    }

    void setDefaultView() {
        textPane.setDocument(new HTMLDocument());
        textPane.showTextArea();
    }

    private void initGUI() {
        uif.initPanel(this, "mfp", false);

        textPane = new TextPane(uif, "mfp.textPane", null);
        textPane.addHyperlinkListener(listener);

        scrllBody = uif.createScrollPane(textPane,
                                 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        textPane.setParent(scrllBody);

        musicPane = new MusicPane(uif, "mfp.audioPane", scrllBody);
        imagePane = new ImagePane(uif, "mfp.imagePanelbl", scrllBody);
        errorPane = new ErrorFormatPane(uif, "mfp.errorPane", scrllBody);

        scrllBody.setViewportView(textPane);

        setLayout(new BorderLayout());

        add(scrllBody, BorderLayout.CENTER);

    }


    //Loads everything by URL
    public void loadPage(URL url) {
        // avoid recursive callbacks from updating combo
        // URL.equals can result in a big performance hit
        if (currURL != null && url.toString().equals(currURL.toString()))
            return;

        currURL = url;

        String protocol = url.getProtocol();
        File file = new File(url.getFile());
        if (protocol.equals("https")) {
            // JavaTestSecurityManager disallows Permission getProperty.ssl.SocketFactory.provider
            // so https can't be processed
            openInBrowser(url);

        } else if (protocol.equals("file") && file.isDirectory()) {
            String list = listLocalDirectory(file);
            textPane.showText(list, "text/html");
            ((HTMLDocument)textPane.getDocument()).setBase(url);
        }
        else if (ImagePane.isImageResource(url)) {
            imagePane.showImage(url);
        }
        else if(MusicPane.isAudioResource(url)) {
            musicPane.showAudio(url);
        }
        else if(isMediaResourceCorrupted(url)) {
            errorPane.showError(uif.getI18NString("mfp.errorPaneLbl.corrupted"));
        }
        else if(TextPane.isTextResource(url)){
            textPane.showText(url);
        }
        else if (url.getProtocol().equalsIgnoreCase("http")) {
            textPane.showText(url);
        }
        else {
            int response = uif.showYesNoDialog("mfp.unknownTypeDlg");
            if(response == JOptionPane.YES_OPTION) {
                textPane.showText(url);
            }
            else {
                errorPane.showError(uif.getI18NString("mfp.errorPaneLbl.unsupported"));
            }
        }

        if(navPane != null) {
            navPane.processURL(url);
        }
    }

    private void openInBrowser(URL url) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(url.toURI());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void showText(String text, String MIME) {
        textPane.showText(text, MIME);
        currURL = null;
    }

    public void showText(URL url) {
        textPane.showText(url);
    }

    public void showAudio(URL url) {
        musicPane.showAudio(url);
    }

    public void stopAudio() {
        musicPane.stopAudio();
    }

    public void showImage(URL url) {
        imagePane.showImage(url);
    }

    public void showError(String errorMessage) {
        errorPane.showError(errorMessage);
    }


    private boolean isMediaResourceSupported(URL url) {
        return ImagePane.isImageResource(url) || MusicPane.isAudioResource(url);
    }

    private boolean isMediaResourceCorrupted(URL url) {
        String filename = url.getFile();
        String ext = filename.substring(filename.lastIndexOf('.') + 1);
        ext = ext.toLowerCase();

        return (SUPPORTED_MEDIA_RESOURCE_EXTENSIONS.indexOf(ext) != -1) &&
                !isMediaResourceSupported(url);
    }


    private String listLocalDirectory(File dir) {
        if (!dir.isAbsolute())
            dir = dir.getAbsoluteFile();

        String displayPath = dir.getPath();
        // if contains base dir, only show path relative to baseDir
        if (baseDir != null) {
            String p = baseDir.getParent();
            if (p != null
                && displayPath.startsWith(p) &&
                (displayPath.length() > p.length())) {
                displayPath = displayPath.substring(p.length());
                // in case of Unix
                if (displayPath.startsWith(File.separator)) {
                    displayPath = displayPath.substring(1);
                }
            }
        }

        String[] filelist = dir.list();

        StringWriter sw = new StringWriter();
        try {
            HTMLWriterEx out = new HTMLWriterEx(sw, uif.getI18NResourceBundle());

            out.startTag(HTMLWriterEx.HTML);
            out.startTag(HTMLWriterEx.HEAD);
            out.writeContentMeta();
            out.startTag(HTMLWriterEx.TITLE);
            out.write(displayPath);
            out.endTag(HTMLWriterEx.TITLE);
            out.endTag(HTMLWriterEx.HEAD);
            out.startTag(HTMLWriterEx.BODY);
            out.writeStyleAttr("font-family: SansSerif; font-size: 12pt");
            out.startTag(HTMLWriterEx.H3);
            out.writeI18N("mfp.head", displayPath);
            out.endTag(HTMLWriterEx.H3);
            out.startTag(HTMLWriterEx.UL);
            out.writeStyleAttr("margin-left:0");

            File parent = dir.getParentFile();
            if (parent != null) {
                out.startTag(HTMLWriterEx.LI);
                out.startTag(HTMLWriterEx.OBJECT);
                out.writeAttr(HTMLWriterEx.CLASSID, "com.sun.javatest.tool.IconLabel");
                out.writeParam("type", "up");
                out.endTag(HTMLWriterEx.OBJECT);
                out.writeEntity("&nbsp;");
                try {
                    out.startTag(HTMLWriterEx.A);
                    out.writeAttr(HTMLWriterEx.HREF, parent.toURL().toString());
                    out.writeI18N("mfp.parent");
                    out.endTag(HTMLWriterEx.A);
                }
                catch (MalformedURLException e) {
                    out.writeI18N("mfp.parent");
                }
            }

            for (int i = 0; i < filelist.length; i++) {
                File file = new File(dir, filelist[i]);
                out.startTag(HTMLWriterEx.LI);
                out.startTag(HTMLWriterEx.OBJECT);
                out.writeAttr(HTMLWriterEx.CLASSID, "com.sun.javatest.tool.IconLabel");
                out.writeParam("type", (file.isDirectory() ? "folder" : "file"));
                out.endTag(HTMLWriterEx.OBJECT);
                out.writeEntity("&nbsp;");
                try {
                    out.writeLink(file.toURL(), file.getName());
                }
                catch (MalformedURLException e) {
                    out.write(file.getName());
                }
            }

            out.endTag(HTMLWriterEx.UL);
            out.endTag(HTMLWriterEx.BODY);
            out.endTag(HTMLWriterEx.HTML);
            out.close();
        }
        catch (IOException e) {
            // should not happen, writing to StringWriter
        }

        return sw.toString();
    }

    public interface MediaPane {

        public int getMode();

        public void changeURL(URL url);

        public void setParent(JScrollPane pane);
    }

// Print this panel
    public int print (Graphics g, PageFormat pf, int pageIndex) {

        int response = NO_SUCH_PAGE;

        Graphics2D g2 = (Graphics2D) g;

        Component componentToBePrinted = scrllBody.getComponent(0);
        int mode = getCurrentMode();
        switch(mode) {
            case TEXT:
                componentToBePrinted = textPane;
                break;
            case IMAGE:
                componentToBePrinted = imagePane;
                break;
            default:
                break;
        }

        //  for faster printing, turn off double buffering
        disableDoubleBuffering(componentToBePrinted);

        Dimension d = componentToBePrinted.getSize();
        double panelWidth = d.width;
        double panelHeight = d.height;

        double pageHeight = pf.getImageableHeight();
        double pageWidth = pf.getImageableWidth();

        double scale = pageWidth / panelWidth;
        int totalNumPages = (int) Math.ceil(scale * panelHeight / pageHeight);

        if (pageIndex >= totalNumPages) {
          response = NO_SUCH_PAGE;
        }
        else {

          //  shift Graphic to line up with beginning of print-imageable region
          g2.translate(pf.getImageableX(), pf.getImageableY());

          //  shift Graphic to line up with beginning of next page to print
          g2.translate(0f, -pageIndex * pageHeight);

          g2.scale(scale, scale);

          componentToBePrinted.paint(g2);

          enableDoubleBuffering(componentToBePrinted);
          response = Printable.PAGE_EXISTS;
        }
        return response;
    }

    private void disableDoubleBuffering(Component c) {
        RepaintManager currentManager = RepaintManager.currentManager(c);
        currentManager.setDoubleBufferingEnabled(false);
      }

    private void enableDoubleBuffering(Component c) {
        RepaintManager currentManager = RepaintManager.currentManager(c);
        currentManager.setDoubleBufferingEnabled(true);
    }
// END of printing

    private class Listener implements HyperlinkListener {
        public void hyperlinkUpdate(HyperlinkEvent e) {
            HyperlinkEvent.EventType et = e.getEventType();
            if (et == HyperlinkEvent.EventType.ACTIVATED) {
                if (e instanceof HTMLFrameHyperlinkEvent) {
//                  HTMLDocument doc = (HTMLDocument)
//                      ((JEditorPane) e.getSource()).getDocument();
//                  doc.processHTMLFrameHyperlinkEvent((HTMLFrameHyperlinkEvent) e);
                    processFrameLink(e);
                }
                else {
                    musicPane.stopAudio();
//                    String s = e.getDescription();
//                    URL correctURL = e.getURL();
//                    HTMLDocument doc = (HTMLDocument)((JEditorPane)e.getSource()).getDocument();
//                    if(doc != null && currURL != null && !doc.getBase().equals(currURL))
//                        currURL = doc.getBase();
//                    // JEditorPane doesn't allows javascript, so we delete this parameter and load
//                    // root page of help package
//                    if(correctURL.getProtocol().equals("file")) {

//                        int pos = correctURL.toString().indexOf("?");
//                        if (pos >= 0) {
//                            try {
//                                correctURL = new URL(correctURL.toString().substring(0, pos));
//                            } catch (MalformedURLException ex) {
//                                // NONE
//                            }
//                        }
//                    }
//                    // end of fix
//                    loadPage(correctURL);
                    if(e.getURL() == null && e.getDescription().startsWith("#")) {
                        processSharpLink(e);
                    }
                    else {
                        processGeneralLink(e);
                    }
                }
            }
            else if (et == HyperlinkEvent.EventType.ENTERED) {
                URL u = e.getURL();
                if (u != null) {
                    textPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    if(noteField != null) {
                        noteField.setText(u.toString());
                    }
                }
            }
            else if (et == HyperlinkEvent.EventType.EXITED) {
                textPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                if(noteField != null) {
                    noteField.setText("");
                }
            }
        }

        private void processFrameLink(HyperlinkEvent e) {
            HTMLDocument doc = (HTMLDocument)
                ((JEditorPane) e.getSource()).getDocument();
            doc.processHTMLFrameHyperlinkEvent((HTMLFrameHyperlinkEvent) e);
        }

        private void processSharpLink(HyperlinkEvent e) {
            String desc = e.getDescription();
            if (!(desc.startsWith("#"))) {
                return;
            }
            textPane.scrollToReference(desc.substring(1));
        }

        private void processGeneralLink(HyperlinkEvent e) {
            URL correctURL = e.getURL();

            if (correctURL == null) {
                return;
            }

            HTMLDocument doc = (HTMLDocument)((JEditorPane)e.getSource()).getDocument();
            if(doc != null && currURL != null && !doc.getBase().toString().equals(currURL.toString())) {
                currURL = doc.getBase();
            }
            // fix of CR 6451318, CR 6442782 and CR 6447246
            // JDTS use '?' notation to pass parameter (currently loaded page of one of frames)
            // to javascript.
            // JEditorPane doesn't allows javascript, so we delete this parameter and load
            // root page of help package
            if(correctURL.getProtocol().equals("file")) {
                int pos = correctURL.toString().indexOf("?");
                if (pos >= 0) {
                    try {
                        correctURL = new URL(correctURL.toString().substring(0, pos));
                    } catch (MalformedURLException ex) {
                        // NONE
                    }
                }
            }
            // end of fix
            loadPage(correctURL);
        }
    }




    private static final String SUPPORTED_MEDIA_RESOURCE_EXTENSIONS =
            ".wav .au .aif .mid .midi .rmf .jpeg .jpg .gif .png";
    private JScrollPane scrllBody;
    public static final int TEXT = 0;
    public static final int IMAGE = 1;
    public static final int AUDIO = 2;
    public static final int ERROR = 3;

    private File baseDir;
    private URL currURL;

    private TextPane textPane;
    private MusicPane musicPane;
    private ImagePane imagePane;
    private ErrorFormatPane errorPane;

    private UIFactory uif;
    private String uiKey;

    private Listener listener;

    private JTextField noteField;
    private NavigationPane navPane;

    private HashMap modesToPanes;
}


class TextPane extends JEditorPane implements MultiFormatPane.MediaPane {

    public TextPane(UIFactory uif, String uiKey, JScrollPane owner) {
        super();

        this.uif = uif;
        this.owner = owner;

        setName("text");
        setEditable(false);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        uif.setAccessibleInfo(this, uiKey);

        htmlKit = new HTMLEditorKit();
        defaultKit = new DefaultEditorKit();
        rtfKit = new RTFEditorKit();

    }

    public void changeURL(URL url) {}

    public void setParent(JScrollPane owner) {
        this.owner = owner;
    }

    public int getMode() {
        return MultiFormatPane.TEXT;
    }

    public void showText(String text, String MIME) {
        setContentType(MIME);

        EditorKit kit = getKitByMIME(MIME);
        Document doc = kit.createDefaultDocument();

        setDocument(doc);

        setText(text);

        showTextArea();
    }

    public void showText(URL url) {
        try {
            String mimeType = getMIMEType(url);
            if (mimeType == null) {
                mimeType = "text/plain";
            }

            boolean loadManually = false;
            if (mimeType.indexOf("plain") != -1) {
                loadManually = true;
            }

            EditorKit kit = getKitByMIME(mimeType);

            setContentType(mimeType);

            Document doc = kit.createDefaultDocument();
            setDocument(doc);
            if (loadManually  && "file".equals(url.getProtocol())) {
                File file = new File(url.getFile());
                try {
                    Reader r = new BufferedReader(new FileReader(file));
                    read(r, url);
                    r.close();
                }
                catch (IOException e) {
                    uif.showError("mfp.load.error", new Object[] { url, e });
                }
            } else {
                setPage(url);
            }

            showTextArea();
        }
        catch (IOException e) {
            uif.showError("mfp.load.error", new Object[] { url, e });
        }
    }

    public static boolean isTextResource(URL url) {
        String mimeType = getMIMEType(url);
        if (mimeType == null)
            return false;
        return (mimeType.equals("text/plain") || mimeType.equals("text/html") ||
                mimeType.equals("text/rtf"));
    }

    public static String getMIMEType(URL url) {
        String filename = url.getFile();
        String ext = filename.substring(filename.lastIndexOf('.') + 1);
        ext = ext.toLowerCase();

        return (String)extensionsToMIME.get(ext);
    }

    public void showTextArea() {
        owner.setViewportView(this);
        owner.revalidate();
        owner.repaint();
    }

    private EditorKit getKitByMIME(String mime) {
        if(mime.indexOf("rtf") != -1)
            return rtfKit;
        else if(mime.indexOf("html") != -1)
            return htmlKit;
        else {
            return defaultKit;
        }
    }

    private JScrollPane owner;
    private UIFactory uif;

    private HTMLEditorKit htmlKit;
    private DefaultEditorKit defaultKit;
    private RTFEditorKit rtfKit;


    private static HashMap extensionsToMIME;
    static {
        extensionsToMIME = new HashMap();
        extensionsToMIME.put("html", "text/html");
        extensionsToMIME.put("htm", "text/html");
        extensionsToMIME.put("htmls", "text/html");
        extensionsToMIME.put("htx", "text/html");
        extensionsToMIME.put("shtml", "text/html");
        extensionsToMIME.put("stm", "text/html");
        extensionsToMIME.put("jsp", "text/html");

        extensionsToMIME.put("text", "text/plain");
        extensionsToMIME.put("txt", "text/plain");
        extensionsToMIME.put("log", "text/plain");
        extensionsToMIME.put("list", "text/plain");
        extensionsToMIME.put("lst", "text/plain");
        extensionsToMIME.put("java", "text/plain");
        extensionsToMIME.put("xml", "text/plain");
        extensionsToMIME.put("jtr", "text/plain");
        extensionsToMIME.put("jti", "text/plain");
        extensionsToMIME.put("jtm", "text/plain");
        extensionsToMIME.put("jtx", "text/plain");
        extensionsToMIME.put("kfl", "text/plain");
        extensionsToMIME.put("css", "text/plain");
        extensionsToMIME.put("fx",  "text/plain");
        extensionsToMIME.put("jasm",  "text/plain");
        extensionsToMIME.put("jcod",  "text/plain");

        extensionsToMIME.put("rtf", "text/rtf");
        extensionsToMIME.put("rtx", "text/rtf");

    }


}

class MusicPane extends JPanel implements MultiFormatPane.MediaPane {
    public MusicPane(UIFactory uif, String uiKey, JScrollPane owner) {
        super();

        this.uif = uif;
        this.owner = owner;

        initGUI(uiKey);
    }

    private void initGUI(String uiKey) {
        setName(uiKey);
        uif.setAccessibleInfo(this, uiKey);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        btnStart = uif.createButton(uiKey + ".btnStart");
        btnStop = uif.createButton(uiKey + ".btnStop");
        btnLoop = uif.createButton(uiKey + ".btnLoop");

        add(btnStart, gbc);
        add(btnStop, gbc);
        add(btnLoop, gbc);

        btnStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(isSampledAudioResource(currURL)) {
                    loadSample(currURL);
                    clip.start();
                }
                else if(isMidiAudioResource(currURL)) {
                    loadSequence(currURL);
                    sequencer.start();
                }
            }
        });

        btnStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopAudio();
            }
        });

        btnLoop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(isSampledAudioResource(currURL)) {
                    loadSample(currURL);
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                }
            }
        });

    }

    public void showAudio(URL url) {
        changeURL(url);

        owner.setViewportView(this);
        owner.revalidate();
        owner.repaint();
    }

    public void changeURL(URL url) {
        currURL = url;

        if(isMidiAudioResource(url)) {
            btnLoop.setEnabled(false);
        }
        else if(isSampledAudioResource(url)) {
            btnLoop.setEnabled(true);
        }

    }

    public void setParent(JScrollPane owner) {
        this.owner = owner;
    }

    public int getMode() {
        return MultiFormatPane.AUDIO;
    }

    public static boolean isAudioResource(URL url) {
        return isSampledAudioResource(url) || isMidiAudioResource(url);
    }

    public void stopAudio() {
        if(clip != null && clip.isRunning()) {
            clip.stop();
            clip.flush();
            clip.close();
        }
        if(sequencer != null && sequencer.isRunning())
            sequencer.stop();
    }

    private void loadSample(URL url) {
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(url);
            AudioFormat format = stream.getFormat();

            DataLine.Info info = new DataLine.Info(
                Clip.class, stream.getFormat(), ((int)stream.getFrameLength()*format.getFrameSize()));

            clip = (Clip) AudioSystem.getLine(info);
            clip.open(stream);
            clip.addLineListener(new LineListener() {
               public void update(LineEvent e) {
                   if(e.getType() == LineEvent.Type.STOP) {
                       clip.stop();
                   }
               }
            });
        } catch (Exception e) {
            clip.close();
        }
    }

    private void loadSequence(URL url) {
        try {
            Sequence sequence = MidiSystem.getSequence(url);

            // Create a sequencer for the sequence
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequencer.setSequence(sequence);

        } catch (Exception e) {}

    }

    private static boolean isSampledAudioResource(URL url) {
        try{
            AudioFileFormat fformat = AudioSystem.getAudioFileFormat(url);
            return AudioSystem.isFileTypeSupported(fformat.getType());
        } catch (UnsupportedAudioFileException unsuppExc) {
            return false;
        } catch (IOException ioExc) {
            return false;
        }
    }

    private static boolean isMidiAudioResource(URL url) {
        try {
            MidiFileFormat fformat = MidiSystem.getMidiFileFormat(url);
            Sequence sequence = MidiSystem.getSequence(url);
            return MidiSystem.isFileTypeSupported(fformat.getType());
        } catch (InvalidMidiDataException invalidDataExc) {
            return false;
        } catch (IOException ioExc) {
            return false;
        }
        catch (Exception exc) {
            return false;
        }
    }


    private UIFactory uif;
    private JScrollPane owner;

    private JButton btnStart;
    private JButton btnStop;
    private JButton btnLoop;

    private URL currURL;
    private Clip clip;
    private Sequencer sequencer;

}

class ImagePane extends JLabel implements MultiFormatPane.MediaPane {

    public ImagePane(UIFactory uif, String uiKey, JScrollPane owner) {
        super(uif.getI18NString(uiKey + ".lbl"));
//        setName(uiKey);

        this.owner = owner;
    }

    public void changeURL(URL url) {}

    public void setParent(JScrollPane owner) {
        this.owner = owner;
    }

    public int getMode() {
        return MultiFormatPane.IMAGE;
    }

    public void showImage(URL url) {
        if(isImageFormatSupported(url)) {
            ImageIcon img = new ImageIcon(url);
            setIcon(img);

            owner.setViewportView(this);
            owner.revalidate();
            owner.repaint();
        }
    }

    public static boolean isImageResource(URL url) {
        String file = url.getFile();
        String ext = file.substring(file.lastIndexOf('.')+1);
        Iterator iter = ImageIO.getImageReadersBySuffix(ext);
        return iter.hasNext();
    }

    public static boolean isImageFormatSupported(URL url) {
        try {
            ImageInputStream iis = ImageIO.createImageInputStream(new File(url.getFile()));
            Iterator iter = ImageIO.getImageReaders(iis);
            if(!iter.hasNext())
                return false;
        } catch (IOException exc) {
            exc.printStackTrace();
            return false;
        }
        return true;
    }

    private JScrollPane owner;
}

class ErrorFormatPane extends JPanel implements MultiFormatPane.MediaPane {
    public ErrorFormatPane(UIFactory uif, String uiKey, JScrollPane owner) {
        super();
        uif.initPanel(this, uiKey, true);

        this.owner = owner;

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = gbc.CENTER;
        errorLabel = uif.createLabel("mfp.errorPaneLbl", true);
        add(errorLabel, gbc);

    }

    public void changeURL(URL url) {}

    public void setParent(JScrollPane owner) {
        this.owner = owner;
    }

    public int getMode() {
        return MultiFormatPane.ERROR;
    }

    public void showError(String errorMessage) {
        errorLabel.setText(errorMessage);

        owner.setViewportView(this);
        owner.revalidate();
        owner.repaint();
    }

    private JLabel errorLabel;
    private JScrollPane owner;
}
