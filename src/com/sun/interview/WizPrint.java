/*
 * $Id$
 *
 * Copyright (c) 1996, 2010, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.interview;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * An API (with a basic front-end application) for generating HTML printouts
 * of an {@link Interview interview}.
 */
public class WizPrint
{
    /**
     * This exception is to report problems that occur with command line arguments.
     */
    public static class BadArgs extends Exception
    {
        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         */
        public BadArgs(ResourceBundle i18n, String s) {
            super(i18n.getString(s));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An argument to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        public BadArgs(ResourceBundle i18n, String s, Object o) {
            super(MessageFormat.format(i18n.getString(s), new Object[] {o}));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An array of arguments to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        public BadArgs(ResourceBundle i18n, String s, Object[] o) {
            super(MessageFormat.format(i18n.getString(s), o));
        }
    }

    /**
     * This exception is to report problems that occur while updating an interview.
     */
    public static class Fault extends Exception
    {
        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         */
        public Fault(ResourceBundle i18n, String s) {
            super(i18n.getString(s));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An argument to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        public Fault(ResourceBundle i18n, String s, Object o) {
            super(MessageFormat.format(i18n.getString(s), new Object[] {o}));
        }

        /**
         * Create a Fault.
         * @param i18n A resource bundle in which to find the detail message.
         * @param s The key for the detail message.
         * @param o An array of arguments to be formatted with the detail message by
         * {@link java.text.MessageFormat#format}
         */
        public Fault(ResourceBundle i18n, String s, Object[] o) {
            super(MessageFormat.format(i18n.getString(s), o));
        }
    }

    /**
     * Write a short description of the command line syntax and options to System.err.
     */
    public static void usage() {
        String prog = System.getProperty("program");
        if (prog == null)
            prog = formatI18N("wp.prog", WizPrint.class.getName());
        String msg = formatI18N("wp.usage", prog);

        boolean newline = true;
        for (int i = 0; i < msg.length(); i++) {
            char c = msg.charAt(i);
            if (c == '\n') {
                System.err.println();
                newline = true;
            }
            else {
                System.err.print(c);
                newline = false;
            }
        }
        if (!newline)
            System.err.println();
    }

    /**
     * Simple command-line front-end to the facilities of the API.
     * @param args Command line arguments.
     * @see #usage
     */
    public static void main(String[] args) {
        try {
            boolean path = false;;
            String interviewClassName = null;
            File interviewFile = null;
            File outFileName = null;

            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-o") && i + 1 < args.length)
                    outFileName = new File(args[++i]);
                else if (args[i].equals("-path"))
                    path = true;
                else if (args[i].equals("-all"))
                    path = false;
                else if (args[i].startsWith("-"))
                    throw new BadArgs(i18n, "wp.badArg", args[i]);
                else if (i == args.length - 1) {
                    if (args[i].endsWith(".jti"))
                        interviewFile = new File(args[i]);
                    else
                        interviewClassName = args[i];
                }
                else
                    throw new BadArgs(i18n, "wp.badArg", args[i]);
            }

            Map interviewData = null;

            if (interviewFile != null) {
                try {
                    InputStream in = new BufferedInputStream(new FileInputStream(interviewFile));
                    Properties p = new Properties();
                    p.load(in);
                    interviewClassName = (String)p.get("INTERVIEW");
                    interviewData = p;
                }
                catch (FileNotFoundException e) {
                    throw new Fault(i18n, "wp.cantFindFile", interviewFile);
                }
                catch (IOException e) {
                    throw new Fault(i18n, "wp.cantReadFile", new Object[] { interviewFile, e });
                }
            }

            if (interviewClassName == null)
                throw new BadArgs(i18n, "wp.noInterview");

            if (outFileName == null) {
                // try and create a default
                if (interviewFile != null) {
                    String ip = interviewFile.getPath();
                    int dot = ip.lastIndexOf(".");
                    if (dot != -1)
                        outFileName = new File(ip.substring(0, dot) + ".html");
                }
                if (outFileName == null)
                    throw new BadArgs(i18n, "wp.noOutput");
            }

            Class ic = Class.forName(interviewClassName);
            Interview interview = (Interview)(ic.newInstance());
            Question[] questions;

            if (interviewData != null)
                interview.load(interviewData);

            if (path) {
                questions = interview.getPath();
            }
            else {
                // enumerate questions, sort on tag
                SortedVector v = new SortedVector();
                for (Iterator iter = interview.getQuestions().iterator(); iter.hasNext(); ) {
                    Question q = (Question) (iter.next());
                    v.insert(q);
                }
                questions = new Question[v.size()];
                v.copyInto(questions);
            }

            try {
                Writer out = new FileWriter(outFileName);
                WizPrint wp = new WizPrint(interview, questions);
                wp.setShowTags(!path);
                wp.setShowResponses(path);
                wp.setShowResponseTypes(!path);
                wp.write(out);
            }
            catch (IOException e) {
                throw new Fault(i18n, "wp.cantWriteFile", new Object[] { outFileName, e });
            }
        }
        catch (BadArgs e) {
            System.err.println(formatI18N("wp.error", e.getMessage()));
            usage();
            System.exit(1);
        }
        catch (Fault e) {
            System.err.println(e.getMessage());
            System.exit(2);
        }
        catch (Throwable e) {
            e.printStackTrace();
            System.exit(3);
        }
    }

    /**
     * Create an object for printing the current set of questions from an interview.
     * @param interview The parent interview which contains the questions.
     */
    public WizPrint(Interview interview) {
        this(interview, interview.getPath());
    }

    /**
     * Create an object for printing a set of questions from an interview.
     * @param interview The parent interview which contains the questions.
     * @param questions The selected set of questions to be printed.
     */
    public WizPrint(Interview interview, Question[] questions) {
        this.interview = interview;
        this.questions = questions;
    }

    /**
     * Determine whether or not responses should be shown when the
     * interview is "printed" to HTML.
     * @return true if responses should be shown
     * @see #setShowResponses
     */
    public boolean getShowResponses() {
        return showResponses;
    }

    /**
     * Specify whether or not responses should be shown when the
     * interview is "printed" to HTML.
     * @param showResponses should be true if responses should be shown
     * @see #getShowResponses
     */
    public void setShowResponses(boolean showResponses) {
        this.showResponses = showResponses;
    }

    /**
     * Determine whether or not the types of responses should be shown
     * when the interview is "printed" to HTML.
     * @return true if the types of responses should be shown
     * @see #setShowResponseTypes
     */
    public boolean getShowResponseTypes() {
        return showResponseTypes;
    }

    /**
     * Specify whether or not the types of responses should be shown
     * when the interview is "printed" to HTML.
     * @param showResponseTypes should be true if the types of responses should be shown
     * @see #getShowResponseTypes
     */
    public void setShowResponseTypes(boolean showResponseTypes) {
        this.showResponseTypes = showResponseTypes;
    }

    /**
     * Determine whether or not question tags should be shown
     * when the interview is "printed" to HTML.
     * @return true if the questions' tags should be shown
     * @see #setShowTags
     */
    public boolean getShowTags() {
        return showTags;
    }

    /**
     * Specify whether or not question tags should be shown
     * when the interview is "printed" to HTML.
     * @param showTags should be true if the questions' tags should be shown
     * @see #getShowTags
     */
    public void setShowTags(boolean showTags) {
        this.showTags = showTags;
    }

    /**
     * Write the selected questions to the given stream, as a complete
     * HTML document.  The stream is closed after the writing is complete.
     * @param o the Writer to which to write the specified information
     * about an interview
     * @throws IOException if there are problems writing to the given Writer
     */
    public void write(Writer o) throws IOException {
        try {
            setWriter(o);
            startTag(DOCTYPE);
            newLine();
            startTag(HTML);
            newLine();
            startTag(HEAD);
            writeTag(TITLE, interview.getTitle());
            endTag(HEAD);
            newLine();
            startTag(BODY);
            writeTag(H1, interview.getTitle());
            newLine();
            writeIndex();
            writeQuestions();
            endTag(BODY);
            endTag(HTML);
            newLine();
        }
        finally {
            out.flush();
            out.close();
        }
    }

    /**
     * Write an index to the set of questions.
     */
    private void writeIndex() throws IOException {
        startTag(UL);
        newLine();
        for (int i = 0; i < questions.length; i++) {
            Question q = questions[i];
            startTag(LI);
            startTag(A);
            writeAttr(HREF, "#" + q.getTag());
            writeText(q.getSummary());
            endTag(A);
            newLine();
        }
        endTag(UL);
        newLine();
        newLine();
    }

    /**
     * Write the body of the document, containing the questions.
     */
    private void writeQuestions() throws IOException {
        startTag(HR);
        for (int i = 0; i < questions.length; i++) {
            if (i > 0) {
                startTag(P);
                startTag(HR);
                writeAttr(ALIGN, LEFT);
                writeAttr(WIDTH, "25%");
            }

            Question q = questions[i];

            String tag = q.getTag();
            if (tag != null) {
                startTag(A);
                writeAttr(NAME, q.getTag());
                endTag(A);
                newLine();
            }

            writeTag(H3, q.getSummary());
            newLine();

            if (showTags) {
                startTag(P);
                startTag(I);
                writeI18N("wp.tag");
                if (tag == null)
                    writeTag(I, "null");  // I18N??
                else
                    writeTag(B, tag);
                endTag(I);
                endTag(P);
                newLine();
            }

            if (q instanceof ErrorQuestion) {
                startTag(P);
                startTag(FONT);
                writeAttr(SIZE, "+1");
                writeText(q.getText());
                endTag(FONT);
                endTag(P);
            }
            else {
                startTag(P);
                writeText(q.getText());
                endTag(P);
                newLine();
                if (showResponseTypes)
                    writeResponseType(q);
                if (showResponses)
                    writeResponse(q);
            }
        }
    }

    /**
     * Write the response to a question.
     * @param q The question whose response is to be printed.
     */
    private void writeResponse(Question q) throws IOException {
        if (q instanceof ChoiceArrayQuestion) {
            ChoiceArrayQuestion caq = (ChoiceArrayQuestion)q;
            writeResponse(caq.getValue(), caq.getChoices(), caq.getDisplayChoices());
        }
        else if (q instanceof ChoiceQuestion) {
            ChoiceQuestion cq = (ChoiceQuestion)q;
            if (cq.getChoices() == cq.getDisplayChoices())
                writeResponse(cq.getValue());
            else
                writeResponse(cq.getValue(), cq.getDisplayValue());
        }
        else if (q instanceof ErrorQuestion) {
            // no response
        }
        else if (q instanceof FileListQuestion) {
            FileListQuestion fq = (FileListQuestion)q;
            File[] f = fq.getValue();
            writeResponse(filesToStrings(f));
        }
        else if (q instanceof FileQuestion) {
            FileQuestion fq = (FileQuestion)q;
            File f = fq.getValue();
            writeResponse(f == null ? null : f.getPath());
        }
        else if (q instanceof FinalQuestion) {
            // no response
        }
        else if (q instanceof FloatQuestion) {
            FloatQuestion fq = (FloatQuestion)q;
            writeResponse(fq.getStringValue());
        }
        else if (q instanceof InetAddressQuestion) {
            InetAddressQuestion iq = (InetAddressQuestion)q;
            writeResponse(iq.getStringValue());
        }
        else if (q instanceof IntQuestion) {
            IntQuestion iq = (IntQuestion)q;
            writeResponse(iq.getStringValue());
        }
        else if (q instanceof NullQuestion) {
            // no response
        }
        else if (q instanceof PropertiesQuestion) {
            PropertiesQuestion pq = (PropertiesQuestion)q;
            writeResponse(pq);
        }
        else if (q instanceof StringQuestion) {
            StringQuestion sq = (StringQuestion)q;
            writeResponse(sq.getValue());
        }
        else if (q instanceof StringListQuestion) {
            StringListQuestion sq = (StringListQuestion)q;
            writeResponse(sq.getValue());
        }
        else if (q instanceof TreeQuestion) {
            TreeQuestion tq = (TreeQuestion)q;
            String[] nodes = tq.getValue();
            if (nodes == null || nodes.length == 0)
                writeResponse(i18n.getString("wp.all"));
            else
                writeResponse(nodes);
        }
        else {
            writeResponse(q.getStringValue());
        }
    }

    /**
     * Write a response.
     * @param s The text of the response.
     */
    private void writeResponse(String s) throws IOException {
        startTag(P);
        startTag(I);
        writeI18N("wp.response");
        if (s == null)
            writeI18N("wp.noResponse");
        else
            writeTag(B, s);
        endTag(I);
        endTag(P);
        newLine();
        newLine();
    }

    /**
     * Write a response.
     * @param response The text of the response.
     * @param displayText The display text of the response.
     */
    private void writeResponse(String response, String displayText) throws IOException {
        startTag(P);
        startTag(I);
        writeI18N("wp.response");
        if (response == null)
            writeI18N("wp.noResponse");
        else {
            writeTag(B, response);
            if (displayText != null) {
                writeText(" ");
                writeI18N("wp.display", displayText);
            }
        }
        endTag(I);
        endTag(P);
        newLine();
        newLine();
    }

    /**
     * Write a response based on a set of named boolean values
     * @param values An array of boolean values.
     * @param choices An array of matching names, one per boolean.
     */
    private void writeResponse(boolean[] values, String[] choices, String[] displayChoices) throws IOException {
        startTag(P);
        startTag(I);
        writeI18N("wp.response");
        if (values == null)
            writeI18N("wp.noResponse");
        else {
            for (int i = 0; i < values.length; i++) {
                if (i > 0)
                    writeI18N("wp.listSep");
                startTag(B);
                /*
                if (values[i])
                    writeText(choices[i]);
                else
                    writeTag(STRIKE, choices[i]);
                */
                if (values[i] == false)
                    startTag(STRIKE);

                writeText(choices[i]);
                if (displayChoices != null && !equal(choices[i], displayChoices[i])) {
                    writeText(" ");
                    writeI18N("wp.display", displayChoices[i]);
                }

                if (values[i] == false)
                    endTag(STRIKE);

                endTag(B);
            }
        }
        endTag(I);
        endTag(P);
        newLine();
        newLine();
    }

    /**
     * Write a response list
     * @param responses The text of the response.
     */
    private void writeResponse(String[] responses) throws IOException {
        startTag(P);
        startTag(I);
        writeI18N("wp.response");
        if (responses == null || responses.length == 0) {
            writeI18N("wp.noResponse");
            endTag(I);
            endTag(P);
        }
        else {
            endTag(I);
            endTag(P);
            startTag(UL);
            for (int i = 0; i < responses.length; i++) {
                startTag(LI);
                startTag(B);
                startTag(I);
                writeText(responses[i]);
                endTag(I);
                endTag(B);
            }
            endTag(UL);
        }
        newLine();
        newLine();
    }

    /**
     * Write the response output.  This methods is not generic like the others
     * because of the rich and optional API for this particular question.
     */
    private void writeResponse(PropertiesQuestion pq) throws IOException {
        String[] groups = pq.getGroups();
        String[] headers = new String[] {pq.getKeyHeaderName(),
                                         pq.getValueHeaderName()};
        String[][] nullGroup = pq.getGroup(null);
        if (nullGroup != null && nullGroup.length != 0)
            writePQTable(headers, nullGroup);
        else if (groups == null || groups.length == 0) {
            // no properties for this question it seems
            writeI18N("wp.noResponse");
        }
        else {
            // fall through
        }

        if (groups != null)
            for (int i = 0; i < groups.length; i++) {
                // heading
                startTag(BR);
                startTag(B);
                writeText(pq.getGroupDisplayName(groups[i]));
                endTag(B);

                // data
                startTag(BR);
                writePQTable(headers, pq.getGroup(groups[i]));
                startTag(P);
            }   // for
    }

    private void writePQTable(String[] headers, String[][] values)
            throws IOException {
        if (values == null || values.length == 0)
            return;

        startTag(TABLE);
        writeAttr("border", "2");
        writeAttr("title",
                  i18n.getString("wp.table.title"));
        writeAttr("summary",
                  i18n.getString("wp.table.summ"));
        startTag(TR);

        // headers
        startTag(TH);
        writeAttr("align", "left");
        writeAttr("scope", "col");      // 508
        writeText(headers[0]);
        startTag(TH);
        writeAttr("align", "left");
        writeAttr("scope", "col");      // 508
        writeText(headers[1]);

        for (int i = 0; i < values.length; i++) {
            startTag(TR);
            startTag(TD);
            writeText(values[i][0]);
            startTag(TD);
            writeText(values[i][1]);
            endTag(TR);
        }   // for

        endTag(TABLE);
    }

    /**
     * Write the response to a question.
     * @param q The question whose response is to be printed.
     */
    private void writeResponseType(Question q) throws IOException {
        if (q instanceof ChoiceArrayQuestion) {
            ChoiceArrayQuestion cq = (ChoiceArrayQuestion)q;
            StringBuffer sb = new StringBuffer();
            sb.append(i18n.getString("wp.type.chooseAny"));
            String[] choices = cq.getChoices();
            for (int i = 0; i < choices.length; i++) {
                if (i > 0)
                    sb.append(i18n.getString("wp.listSep"));
                sb.append(choices[i] == null ? i18n.getString("wp.unset") : choices[i]);
            }
            writeResponseType(sb.toString());
        }
        else if (q instanceof ChoiceQuestion) {
            ChoiceQuestion cq = (ChoiceQuestion)q;
            StringBuffer sb = new StringBuffer();
            sb.append(i18n.getString("wp.type.chooseOne"));
            String[] choices = cq.getChoices();
            for (int i = 0; i < choices.length; i++) {
                if (i > 0)
                    sb.append(i18n.getString("wp.listSep"));
                sb.append(choices[i] == null ? i18n.getString("wp.unset") : choices[i]);
            }
            writeResponseType(sb.toString());
        }
        else if (q instanceof ErrorQuestion) {
            // no response
        }
        else if (q instanceof FileQuestion) {
            writeResponseType(i18n.getString("wp.type.file"));
        }
        else if (q instanceof FileListQuestion) {
            writeResponseType(i18n.getString("wp.type.fileList"));
        }
        else if (q instanceof FinalQuestion) {
            // no response
        }
        else if (q instanceof FloatQuestion) {
            FloatQuestion fq = (FloatQuestion)q;
            float lwb = fq.getLowerBound();
            float upb = fq.getUpperBound();
            writeResponseType(formatI18N("wp.type.float",
                                     new Object[] {
                                         new Integer(lwb == Float.MIN_VALUE ? 0 : 1),
                                         new Float(lwb),
                                         new Integer(upb == Float.MAX_VALUE ? 0 : 1),
                                         new Float(upb) }));
        }
        else if (q instanceof InetAddressQuestion) {
            writeResponseType(i18n.getString("wp.type.inetAddress"));
        }
        else if (q instanceof IntQuestion) {
            IntQuestion iq = (IntQuestion)q;
            int lwb = iq.getLowerBound();
            int upb = iq.getUpperBound();
            writeResponseType(formatI18N("wp.type.int",
                                     new Object[] {
                                         new Integer(lwb == Integer.MIN_VALUE ? 0 : 1),
                                         new Integer(lwb),
                                         new Integer(upb == Integer.MAX_VALUE ? 0 : 1),
                                         new Integer(upb) }));
        }
        else if (q instanceof NullQuestion) {
            // no response
        }
        else if (q instanceof StringQuestion) {
            writeResponseType(i18n.getString("wp.type.string"));
        }
        else if (q instanceof StringListQuestion) {
            writeResponseType(i18n.getString("wp.type.stringList"));
        }
        else if (q instanceof TreeQuestion) {
            writeResponseType(i18n.getString("wp.type.tree"));
        }
        else {
            startTag(P);
            writeTag(I, "unknown type of question; cannot determine response type");
            endTag(P);
        }
    }

    /**
     * Write a response.
     * @param s The text of the response.
     */
    private void writeResponseType(String s) throws IOException {
        startTag(P);
        startTag(I);
        if (showResponseTypes && showResponses)
            writeI18N("wp.responseType");  // is this being too clever?
        else
            writeI18N("wp.response");
        startTag(B);
        writeText(s);
        endTag(B);
        endTag(I);
        endTag(P);
        newLine();
        newLine();
    }

    private void writeI18N(String key) throws IOException {
        writeText(i18n.getString(key));
    }

    private void writeI18N(String key, Object arg) throws IOException {
        String s = formatI18N(key, arg);
        writeText(s);
    }

    /**
     * Convert a set of files to their string paths
     */
    private String[] filesToStrings(File[] f) {
        if (f == null)
            return null;
        String[] s = new String[f.length];
        for (int i = 0; i < s.length; i++)
            s[i] = f[i].getPath();
        return s;
    }

    /**
     * Write a newline.
     */
    private void newLine() throws IOException {
        if (state == IN_TAG) {
            out.write('>');
            state = IN_BODY;
        }

        out.newLine();
    }

    /**
     * Write an opening tag.
     * @param t The tag to be written
     */
    private void startTag(String t) throws IOException {
        if (state == IN_TAG)
            out.write('>');

        out.write('<');
        out.write(t);
        state = IN_TAG;
    }

    /**
     * Write an closing tag.
     * @param t The tag to be written
     */
    private void endTag(String t) throws IOException {
        if (state == IN_TAG)
            out.write('>');

        out.write("</");
        out.write(t);
        out.write('>');
        state = IN_BODY;
    }

    private void writeAttr(String name, String value) throws IOException {
        if (state != IN_TAG)
            throw new IllegalStateException();

        out.write(" ");
        out.write(name);
        out.write("=");
        boolean alpha = true;
        for (int i = 0; i < value.length() && alpha; i++)
            alpha = Character.isLetter(value.charAt(i));
        if (!alpha)
            out.write("\"");
        out.write(value);
        if (!alpha)
            out.write("\"");
    }


    /**
     * Write a string between opening and closing tags.
     * @param t The enclosing tag
     * @param s The enclosed text
     */
    private void writeTag(String t, String s) throws IOException {
        startTag(t);
        writeText(s);
        endTag(t);
    }


    /**
     * Write body text, applying any necessary escapes.
     */
    private void writeText(String s) throws IOException {
        if (state == IN_TAG) {
            out.write(">");
            state = IN_BODY;
        }

        if (s == null) {
            out.write("<i>");
            out.write(i18n.getString("wp.null"));
            out.write("</i>");
        }
        else {
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                case '\n':
                    out.write("<br>");
                    break;
                case '<':
                out.write("&lt;");
                break;
                case '>':
                    out.write("&gt;");
                    break;
                case '&':
                    out.write("&amp;");
                    break;
                default:
                    out.write(c);
                }
            }
        }
    }

    private static String formatI18N(String key, Object arg) {
        return MessageFormat.format(i18n.getString(key), new Object[] { arg });
    }

    private static String formatI18N(String key, Object[] args) {
        return MessageFormat.format(i18n.getString(key), args);
    }

    private void setWriter(Writer o) {
        if (out instanceof BufferedWriter)
            out = (BufferedWriter)o;
        else
            out = new BufferedWriter(o);
    }

    private static boolean equal(String s1, String s2) {
        return (s1 == null ? s2 == null : s1.equals(s2));
    }

    private Interview interview;
    private Question[] questions;
    private BufferedWriter out;
    private boolean showResponses;
    private boolean showResponseTypes;
    private boolean showTags;

    private int state;
    private static final int IN_TAG = 1;
    private static final int IN_BODY = 2;

    private static final ResourceBundle i18n = ResourceBundle.getBundle("com.sun.interview.i18n");

    private static final String DOCTYPE = "!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//EN\"";

    private static final String A = "a";
    private static final String ALIGN = "align";
    private static final String B = "b";
    private static final String BODY = "body";
    private static final String BR = "br";
    private static final String FONT = "font";
    private static final String H1 = "h1";
    private static final String H3 = "h3";
    private static final String HEAD = "head";
    private static final String HR = "hr";
    private static final String HREF = "href";
    private static final String HTML = "html";
    private static final String I = "i";
    private static final String LEFT = "left";
    private static final String LI = "li";
    private static final String NAME = "name";
    private static final String P = "p";
    private static final String SIZE = "size";
    private static final String STRIKE = "strike";
    private static final String TABLE = "table";
    private static final String TITLE = "title";
    private static final String TD = "td";
    private static final String TH = "th";
    private static final String TR = "tr";
    private static final String UL = "ul";
    private static final String WIDTH = "width";

    private static class SortedVector
    {
        public SortedVector() {
            v = new Vector();
        }

        public SortedVector(int initialSize) {
            v = new Vector(initialSize);
        }


        public int size() {
            return v.size();
        }

        public Object elementAt(int index) {
            return v.elementAt(index);
        }

        public void insert(Object o) {
            v.insertElementAt(o, findSortIndex(o));
        }

        public void insert(Object o, boolean ignoreDuplicates) {
            int i = findSortIndex(o);
            if (ignoreDuplicates && (i < v.size()) && (compare(o, v.elementAt(i)) == 0))
                return;

            v.insertElementAt(o, i);
        }

        public void copyInto(Object[] target) {
            v.copyInto(target);
        }

        protected int compare(Object o1, Object o2) {
            String p1 = ((Question)o1).getTag();
            String p2 = ((Question)o2).getTag();

            if (p1 == null && p2 == null)
                return 0;

            if (p1 == null)
                return -1;

            if (p2 == null)
                return +1;

            return p1.compareTo(p2);
        }

        private int findSortIndex(Object o) {
            int lower = 0;
            int upper = v.size() - 1;
            int mid = 0;

            if (upper == -1) {
                return 0;
            }

            int cmp = 0;
            Object last = v.elementAt(upper);
            cmp = compare(o, last);
            if (cmp > 0)
                return upper + 1;

            while (lower <= upper) {
                mid = lower + ((upper - lower) / 2);
                Object entry = v.elementAt(mid);
                cmp = compare(o, entry);

                if (cmp == 0) {
                    // found a matching description
                    return mid;
                } else if (cmp < 0) {
                    upper = mid - 1;
                } else {
                    lower = mid + 1;
                }
            }

            // didn't find it, but we indicate the index of where it would belong.
            return (cmp < 0) ? mid : mid + 1;
        }

        private Vector v;
    }

}
