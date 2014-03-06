/*
 * $Id$
 *
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import com.sun.javatest.util.BackupPolicy;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.Properties;
import com.sun.javatest.util.PropertyArray;
import com.sun.javatest.util.StringArray;
import com.sun.javatest.util.DynamicArray;

/**
 * The TestResult object encapsulates the results from a test.
 * Test results are formatted in sections of command output,
 * comments and sometimes "streams" of output (<tt>stdout</tt> for example).
 * Each of these sections is represented by a (@link TestResult.Section Section).
 * Instances of this class are mutable until the result of the section is
 * set or until the result of the test itself is set.
 *
 * Test results are stored in a structured text files.
 * The TestResult class serves as the API for accessing the various
 * components that make up the result of running a test.
 * The status is cached as its size is small and it is accessed often.
 *
 * This class and inner classes will throw IllegalStateExceptions if an
 * attempt is made to modify the any part of the object that has been
 * marked immutable.
 */

public class TestResult {
    /**
     * This exception is to report problems using TestResult objects.
     */
    public static class Fault extends Exception
    {
        Fault(I18NResourceBundle i18n, String key) {
            super(i18n.getString(key));
        }

        Fault(I18NResourceBundle i18n, String key, Object arg) {
            super(i18n.getString(key, arg));
        }

        Fault(I18NResourceBundle i18n, String key, Object[] args) {
            super(i18n.getString(key, args));
        }
    }

    /**
     * This exception is thrown if the JTR file cannot be found.
     */
    public static class ResultFileNotFoundFault extends Fault {
        ResultFileNotFoundFault(I18NResourceBundle i18n, String key) {
            super(i18n, key);
        }

        ResultFileNotFoundFault(I18NResourceBundle i18n, String key, Object arg) {
            super(i18n, key, arg);
        }

        ResultFileNotFoundFault(I18NResourceBundle i18n, String key, Object[] args) {
            super(i18n, key, args);
        }
    }

    /**
     * This exception ay occur anytime the JTR file is being read from the filesystem.
     * To optimize memory usage, the contents of a TestResult object are sometimes
     * discarded and then loaded on demand from the JTR file.  If a fault occurs
     * when reading the JTR file, this fault may occur.
     *
     * @see TestResult.ResultFileNotFoundFault
     */
    public static class ReloadFault extends Fault {
        ReloadFault(I18NResourceBundle i18n, String key) {
            super(i18n, key);
        }

        ReloadFault(I18NResourceBundle i18n, String key, Object arg) {
            super(i18n, key, arg);
        }

        ReloadFault(I18NResourceBundle i18n, String key, Object[] args) {
            super(i18n, key, args);
        }
    }

    /**
     * An interface to observe activity in a TestResult as it is created.
     */
    public interface Observer {
        /**
         * A new section has been created in the test result.
         *
         * @param tr The test result in which the section was created.
         * @param section The section that has been created
         */
        public void createdSection(TestResult tr, Section section);

        /**
         * A section has been been completed in the test result.
         *
         * @param tr The test result containing the section.
         * @param section The section that has been completed.
         */
        public void completedSection(TestResult tr, Section section);

        /**
         * New output has been created in a section of the test result.
         *
         * @param tr The test result containing the output.
         * @param section The section in which the output has been created.
         * @param outputName The name of the output.
         */
        public void createdOutput(TestResult tr, Section section, String outputName);

        /**
         * Output has been completed in a section of the test result.
         *
         * @param tr The test result containing the output.
         * @param section The section in which the output has been completed.
         * @param outputName The name of the output.
         */
        public void completedOutput(TestResult tr, Section section, String outputName);

        /**
         * The output for a section has been updated.
         *
         * @param tr The test result object being modified.
         * @param section The section in which the output is being produced.
         * @param outputName The name of the output.
         * @param start the start offset of the text that was changed
         * @param end the end offset of the text that was changed
         * @param text the text that replaced the specified range.
         */
        public void updatedOutput(TestResult tr, Section section, String outputName, int start, int end, String text);

        /**
         * A property of the test result has been updated.
         *
         * @param tr The test result containing the property that was modified.
         * @param name The key for the property that was modified.
         * @param value The new value for the property.
         *
         */
        public void updatedProperty(TestResult tr, String name, String value);

        /**
         * The test has completed, and the results are now immutable.
         * There will be no further observer calls.
         * @param tr The test result that has been completed.
         */
        public void completed(TestResult tr);

    }

    /**
     * This "section" is the logical combination of a single action during test
     * execution.  It is designed to hold multiple (or none) buffers of
     * output from test execution, such as stdout and stderr.  In addition,
     * it has a "comment" field for tracking the test run itself (progress).
     * This output is identified by the MSG_SECTION_NAME identifier.
     */
    public class Section {
        /**
         * Query if the section is still writable or not.
         * @return true if the section is still writable, and false otherwise
         */
        public boolean isMutable() {
            synchronized (TestResult.this) {
                synchronized (this) {
                    return (TestResult.this.isMutable() &&
                            this.result == inProgress);
                }
            }
        }

        /**
         * Find out what the result of the execution of this section was.
         * @return the result of the execution of this section
         * @see #setStatus
         */
        public Status getStatus() {
            return result;
        }

        /**
         * Set the result of this section.  This action makes this section
         * immutable.
         *
         * @param result The status to set as the result of this section of the test
         * @see #getStatus
         */
        public void setStatus(Status result) {
            synchronized (TestResult.this) {
                synchronized (this) {
                    checkMutable();
                    for (int i = 0; i < buffers.length; i++) {
                        OutputBuffer b = buffers[i];
                        if (b instanceof WritableOutputBuffer) {
                            WritableOutputBuffer wb = (WritableOutputBuffer)b;
                            wb.getPrintWriter().close();
                        }
                    }
                    if (env == null)
                        env = emptyStringArray;
                    this.result = result;
                    if (env == null)
                        env = emptyStringArray;
                    notifyCompletedSection(this);
                }
            }
        }

        /**
         * Get the title of this section, specified when the section
         * was created.
         * @return the title of this section
         */
        public String getTitle() {
            return title;
        }

        /**
         * Get the appropriate to writer to access the default message field.
         * @return a Writer to access the default message field
         */
        public PrintWriter getMessageWriter() {
            synchronized (TestResult.this) {
                synchronized (this) {
                    checkMutable();
                    // if it is mutable, it must have a message stream,
                    // which will be the first entry
                    return buffers[0].getPrintWriter();
                }
            }
        }

        /**
         * Find out how many output buffers this section has inside it.
         *
         * @return The number of output buffers in use (>=0).
         */
        public synchronized int getOutputCount() {
            return buffers.length;
        }

        /**
         * Add a new output buffer to the section; get PrintWriter access to it.
         *
         * @param name The symbolic name that will identify this new stream.
         * @return A PrintWriter that gives access to the new stream.
         */
        public PrintWriter createOutput(String name) {
            if (name == null)
                throw new NullPointerException();

            synchronized (TestResult.this) {
                synchronized (this) {
                    checkMutable();

                    OutputBuffer b = new WritableOutputBuffer(name);
                    buffers = (OutputBuffer[])(DynamicArray.append(buffers, b));

                    notifyCreatedOutput(this, name);

                    return b.getPrintWriter();
                }
            }
        }

        /**
         * Get the content that was written to a specified output stream.
         * @param name the name of the stream in question
         * @return All the data that was written to the specified output,
         *         or null if nothing has been written.
         */
        public String getOutput(String name) {
            if (name == null)
                throw new NullPointerException();

            synchronized (TestResult.this) {
                synchronized (this) {
                    OutputBuffer b = findOutputBuffer(name);
                    return (b == null ? null : b.getOutput());
                }
            }
        }

        /**
         * Find out the symbolic names of all the streams in this section.  You
         * can use getOutputCount to discover the number of items in
         * this enumeration (not a thread safe activity in the strictest
         * sense of course).
         *
         * @return A list of strings which are the symbolic names of the streams in this section.
         * @see #getOutputCount
         */
        public synchronized String[] getOutputNames() {
            String[] names = new String[buffers.length];

            for (int i = 0; i < buffers.length; i++) {
                names[i] = buffers[i].getName();
                if (names[i] == null)
                    throw new IllegalStateException("BUFFER IS BROKEN");
            }

            return names;
        }

        /**
         * Removes any data added to the named output up to this point, resetting
         * it to an empty state.
         * @param name The output name to erase the content of.
         * @since 4.2.1
         */
        public synchronized void deleteOutputData(String name) {
            if (name == null)
                throw new NullPointerException();

            synchronized (TestResult.this) {
                synchronized (this) {
                    OutputBuffer b = findOutputBuffer(name);
                    if (b != null && b instanceof WritableOutputBuffer)
                        ((WritableOutputBuffer)b).deleteAllOutput();
                }
            }
        }

        // ---------- PACKAGE PRIVATE ----------

        Section(String title) {
            if (title == null)
                throw new NullPointerException();
            if (title.indexOf(' ') != -1)
                throw new IllegalArgumentException("space invalid in section title");

            this.title = title;
            result = inProgress;
        }

        /**
         * Could be used to reconstruct the section from a stream.
         * Reads from the source until it finds a section header.  This is a JTR
         * version 2 method, don't use it for version 1 files.  The object
         * immediately immutable upon return from this constructor.
         *
         * @throws ReloadFault Probably an error while parsing the input stream.
         */
        Section(BufferedReader in) throws IOException, ReloadFault {
            String line = in.readLine();
            // find top of section and process it
            while (line != null) {
                if (line.startsWith(JTR_V2_SECTION)) {
                    title = extractSlice(line, 0, ":", null);
                    break;
                }
                else
                    // don't know what this line is, may be empty
                    line = in.readLine();
            }

            if (title == null)
                throw new ReloadFault(i18n, "rslt.noSectionTitle");

            if (title.equals(MSG_SECTION_NAME)) {
                // use standard internal copy of string
                title = MSG_SECTION_NAME;
            }

            while ((line = in.readLine()).startsWith(JTR_V2_SECTSTREAM)) {
                OutputBuffer b = new FixedOutputBuffer(line, in);
                buffers = (OutputBuffer[])(DynamicArray.append(buffers, b));
            }

            // if not in the message section, line should have the section result
            if (title != MSG_SECTION_NAME) {
                if (line != null) {
                    if (line.startsWith(JTR_V2_SECTRESULT))
                        result = Status.parse(line.substring(JTR_V2_SECTRESULT.length()));
                    else
                        throw new ReloadFault(i18n, "rslt.badLine", line);
                }
                if (result == null)
                    // no test result
                    throw new ReloadFault(i18n, "rslt.noSectionResult");
            }
        }

        void save(Writer out) throws IOException {
            out.write(JTR_V2_SECTION + getTitle());
            out.write(lineSeparator);

            for (int index = 0; index < buffers.length; index++) {
                String text = buffers[index].getOutput();
                int numLines = 0;
                int numBackslashes = 0;
                int numNonASCII = 0;
                boolean needsFinalNewline = false;
                boolean needsEscape;

                // scan for newlines and characters requiring escapes
                for (int i = 0; i < text.length(); i++) {
                    char c = text.charAt(i);
                    if (c < 32) {
                        if (c == '\n')
                            numLines++;
                        else if (c != '\t' && c != '\r')
                            numNonASCII++;
                    }
                    else if (c < 127) {
                        if (c == '\\')
                            numBackslashes++;
                    }
                    else
                        numNonASCII++;
                }

                needsEscape = (numBackslashes > 0 || numNonASCII > 0);

                // Check the text ends with a final newline ('\n', not line.separator)
                // Note this must match the check when reading the text back in,
                // when we also check for just '\n' and not line.separator, because
                // line.separator now, and line.separator then, might be different.
                if (text.length() != 0 && !text.endsWith("\n")) {
                    needsFinalNewline = true;
                    numLines++;
                }

                out.write(JTR_V2_SECTSTREAM);
                out.write(buffers[index].getName());
                out.write(":");
                out.write('(');
                out.write(String.valueOf(numLines));
                out.write('/');
                if (needsEscape) {
                    // count one per character, plus an additional one per \ (written as "\ \") and an
                    // additional 5 per nonASCII (written as "\ u x x x x")
                    out.write(String.valueOf(text.length() + numBackslashes + 5*numNonASCII));
                }
                else
                    out.write(String.valueOf(text.length()));
                out.write(')');
                if (needsEscape)
                    out.write('*');
                out.write(JTR_V2_SECTSTREAM);
                out.write(lineSeparator);

                if (needsEscape) {
                    for (int i = 0; i < text.length(); i++) {
                        char c = text.charAt(i);
                        if (32 <= c && c < 127 && c != '\\')
                            out.write(c);
                        else {
                            switch (c) {
                            case '\n': case '\r': case '\t':
                                out.write(c);
                                break;
                            case '\\':
                                out.write("\\\\");
                                break;
                            default:
                                out.write("\\u");
                                out.write(Character.forDigit((c >> 12) & 0xF, 16));
                                out.write(Character.forDigit((c >>  8) & 0xF, 16));
                                out.write(Character.forDigit((c >>  4) & 0xF, 16));
                                out.write(Character.forDigit((c >>  0) & 0xF, 16));
                                break;
                            }
                        }
                    }
                }
                else
                    out.write(text);

                if (needsFinalNewline)
                    out.write(lineSeparator);
            }

            // the default message section does not need a result line
            if (getTitle() != MSG_SECTION_NAME) {
                out.write(JTR_V2_SECTRESULT + result.toString());
                out.write(lineSeparator);
            }

            out.write(lineSeparator);
        }

        /**
         * Reload an output block. This method is called while reloading
         * a test result and so bypasses the normal immutability checks.
         */
        synchronized void reloadOutput(String name, String data) {
            if (name.equals(MESSAGE_OUTPUT_NAME))
                name = MESSAGE_OUTPUT_NAME;
            OutputBuffer b = new FixedOutputBuffer(name, data);
            buffers = (OutputBuffer[])(DynamicArray.append(buffers, b));
        }

        /**
         * Reload the status. This method is called while reloading
         * a test result and so bypasses the normal immutability checks.
         */
        synchronized void reloadStatus(Status s) {
            result = s;
        }

        // ---------- PRIVATE ----------

        private void checkMutable() {
            if (!isMutable())
                throw new IllegalStateException("This section of the test result is now immutable.");
        }

        private synchronized void makeOutputImmutable(OutputBuffer b, String name, String output) {
            for (int i = 0; i < buffers.length; i++) {
                if (buffers[i] == b) {
                    buffers[i] = new FixedOutputBuffer(name, output);
                    return;
                }
            }
        }

        private synchronized OutputBuffer findOutputBuffer(String name) {
            // search backwards
            // may help in some backward compatibility cases since the most
            // recent stream with that name will be found
            // performance of the search will still be constant
            for (int i = buffers.length-1; i >= 0 ; i--) {
                if (name.equals(buffers[i].getName()))
                    return buffers[i];
            }

            return null;
        }

        private OutputBuffer[] buffers = new OutputBuffer[0];
        private String title;
        private Status result;

        private class FixedOutputBuffer implements OutputBuffer {
            FixedOutputBuffer(String name, String output) {
                if (name == null || output == null)
                    throw new NullPointerException();

                this.name = name;
                this.output = output;
            }

            public String getName() {
                return name;
            }

            public String getOutput() {
                return output;
            }

            public PrintWriter getPrintWriter() {
                throw new IllegalStateException("This section is immutable");
            }

            FixedOutputBuffer(String header, BufferedReader in) throws ReloadFault {
                String nm = extractSlice(header, JTR_V2_SECTSTREAM.length(), null, ":");
                if (nm == null)
                    throw new ReloadFault(i18n, "rslt.noOutputTitle");

                if (nm.equals(MESSAGE_OUTPUT_NAME ))
                    nm = MESSAGE_OUTPUT_NAME;

                try {
                    int lines;
                    int chars;
                    boolean needsEscape;

                    try {
                        int start = JTR_V2_SECTSTREAM.length();
                        lines = Integer.parseInt(extractSlice(header, start, "(", "/"));
                        chars = Integer.parseInt(extractSlice(header, start, "/", ")"));
                        int rp = header.indexOf(")", start);
                        if (rp >= 0 && rp < header.length() - 2)
                            needsEscape = (header.charAt(rp + 1) == '*');
                        else
                            needsEscape = false;
                    }
                    catch (NumberFormatException e) {
                        // fatal parsing error
                        throw new ReloadFault(i18n, "rslt.badHeaderVersion", e);
                    }

                    StringBuffer buff = new StringBuffer(chars);

                    if (needsEscape) {
                        for (int i = 0; i < chars; i++) {
                            int c = in.read();
                            if (c == -1)
                                throw new ReloadFault(i18n, "rslt.badEOF");
                            else if (c == '\\') {
                                c = in.read();
                                i++;
                                if (c == 'u') {
                                    c =  Character.digit((char)in.read(), 16) << 12;
                                    c += Character.digit((char)in.read(), 16) <<  8;
                                    c += Character.digit((char)in.read(), 16) <<  4;
                                    c += Character.digit((char)in.read(), 16);
                                    i += 4;
                                }
                                // else drop through (for \\)
                            }
                            buff.append((char)c);
                        }
                    }
                    else {
                        char[] data = new char[Math.min(4096, chars)];
                        int charsRead = 0;
                        while (charsRead < chars) {
                            int n = in.read(data, 0, Math.min(data.length, chars-charsRead));
                            buff.append(data, 0, n);
                            charsRead += n;
                        }
                    }

                    /*NEW
                    while (true) {
                        int c = in.read();
                        switch (c) {
                        case -1:
                            throw new ReloadFault(i18n, "rslt.badEOF");

                        case '\\':
                            if (needEscape) {
                                c = in.read();
                                if (c == 'u') {
                                    c =  Character.digit((char)in.read(), 16) << 12;
                                    c += Character.digit((char)in.read(), 16) <<  8;
                                    c += Character.digit((char)in.read(), 16) <<  4;
                                    c += Character.digit((char)in.read(), 16);
                                }
                                // else drop through (for \\)
                            }
                            buff.append((char)c);
                        }
                    }
                    */

                    name = nm;
                    output = buff.toString();

                    if (buff.length() > 0 && buff.charAt(buff.length() - 1) != '\n') {
                        int c = in.read();
                        if (c == '\r')
                            c = in.read();
                        if (c != '\n') {
                            System.err.println("TR.badChars: output=" + (output.length() < 32 ? output : output.substring(0, 9) + " ... " + output.substring(output.length() - 10) ));
                            System.err.println("TR.badChars: '" + ((char)c) + "' (" + c + ")");
                            throw new ReloadFault(i18n, "rslt.badChars", name);
                        }
                    }
                }
                catch (IOException e) {
                    // not enough data probably fatal parsing error
                    throw new ReloadFault(i18n, "rslt.badFile", e);
                }
            }

            private final String name;
            private final String output;
        }

        private class WritableOutputBuffer extends Writer implements OutputBuffer {
            WritableOutputBuffer(String name) {
                super(TestResult.this);
                if (name == null)
                    throw new NullPointerException();

                this.name = name;
                output = new StringBuffer();
                pw = new LockedWriter(this, TestResult.this);
            }

            public String getName() {
                return name;
            }

            public String getOutput() {
                return new String(output);
            }

            public PrintWriter getPrintWriter() {
                return pw;
            }

            public void write(char[] buf, int offset, int len) throws IOException {
                if (output == null)
                    throw new IOException("stream has been closed");

                int end = output.length();
                output.append(buf, offset, len);
                // want to avoid creating the string buf(offset..len)
                // since likely case is no observers
                notifyUpdatedOutput(Section.this, name, end, end, buf, offset, len);

                if (output.length() > maxOutputSize) {
                    int overflowEnd = maxOutputSize*2/3;
                    if (overflowed) {
                        // output.delete(overflowStart, overflowEnd);
                        // JDK 1.1--start
                        String s = output.toString();
                        output = new StringBuffer(s.substring(0, overflowStart) + s.substring(overflowEnd));
                        // JDK 1.1--end
                        notifyUpdatedOutput(Section.this, name, overflowStart, overflowEnd, "");
                    }
                    else {
                        String OVERFLOW_MESSAGE =
                            "\n\n...\n"
                            + "Output overflow:\n"
                            + "JT Harness has limited the test output to the text to that\n"
                            + "at the beginning and the end, so that you can see how the\n"
                            + "test began, and how it completed.\n"
                            + "\n"
                            + "If you need to see more of the output from the test,\n"
                            + "set the system property javatest.maxOutputSize to a higher\n"
                            + "value. The current value is " + maxOutputSize
                            + "\n...\n\n";
                        overflowStart = maxOutputSize/3;
                        //output.replace(overflowStart, maxOutputSize*2/3, OVERFLOW_MESSAGE);
                        // JDK 1.1--start
                        String s = output.toString();
                        output = new StringBuffer(s.substring(0, overflowStart) + OVERFLOW_MESSAGE + s.substring(overflowEnd));
                        // JDK 1.1--end
                        notifyUpdatedOutput(Section.this, name, overflowStart, overflowEnd, OVERFLOW_MESSAGE);
                        overflowStart += OVERFLOW_MESSAGE.length();
                        overflowed = true;
                    }
                }
            }

            public void flush() {
                //no-op
            }

            public void deleteAllOutput() {
                pw.flush();
                output.setLength(0);
                overflowStart = -1;
                overflowed = false;
            }

            public void close() {
                makeOutputImmutable(this, name, new String(output));
                notifyCompletedOutput(Section.this, name);
            }

            private boolean overflowed;
            private int overflowStart;
            private final String name;
            private /*final*/ StringBuffer output; // can't easily be final in JDK 1.1 because need to reassign to it
            private final PrintWriter pw;
        }
    }

    private class LockedWriter extends PrintWriter {
        public LockedWriter(Writer out, Object theLock) {
            super(out);
            lock = theLock;
        }
    }

    // Conceptually, this belongs in Section, but that is not legal Java.
    // (It is accepted in  1.1.x; rejected by 1.2)
    private interface OutputBuffer {
        String getName();
        String getOutput();
        PrintWriter getPrintWriter();
    }




    // ------------------------- PUBLIC CONSTRUCTORS -------------------------
    /**
     * Construct a test result object that will be built as the test runs.
     * The status string will be "running..." rather than "not run".
     *
     * @param td The test description to base this new object on.  Cannot be
     *        null.
     */
    public TestResult(TestDescription td) {
        desc = td;
        execStatus = inProgress;
        testURL = desc.getRootRelativeURL();

        createSection(MSG_SECTION_NAME);

        props = emptyStringArray;  // null implies it was discarded, not empty
    }


    /**
     * Reconstruct the results of a previously run test.
     *
     * @param workDir Work directory in which the tests were run
     * @param td      Description of the test that was run
     * @throws TestResult.Fault if there is a problem recreating the results
     *                  from the appropriate file in the work directory
     */
    public TestResult(TestDescription td, WorkDirectory workDir)
                      throws Fault {
        desc = td;
        testURL = desc.getRootRelativeURL();
        execStatus = inProgress;

        reloadFromWorkDir(workDir);
    }

    /**
     * Reconstruct the results of a previously run test.
     *
     * @param file File that the results have been stored into.
     * @throws     TestResult.ReloadFault if there is a problem recreating the results
     *                  from the given file
     * @throws     TestResult.ResultFileNotFoundFault if there is a problem locating
     *                  the given file
     */
    public TestResult(File file)
        throws ResultFileNotFoundFault, ReloadFault
    {
        resultsFile = file;
        reload();

        testURL = desc.getRootRelativeURL();

        execStatus = Status.parse(PropertyArray.get(props, EXEC_STATUS));
    }

    /**
     * Reconstruct the results of a previously run test.
     *
     * @param workDir The work directory where previous results for the guven
     *        test can be found.
     * @param workRelativePath The path to the JTR to reload, relative to the
     *        workdir.
     * @throws TestResult.Fault if there is a problem recreating the results
     *            from the given file
     */
    public TestResult(WorkDirectory workDir, String workRelativePath) throws Fault {
        //resultsFile = workDir.getFile(workRelativePath.replace('/', File.separatorChar));
        resultsFile = workDir.getFile(workRelativePath);
        reload();

        testURL = desc.getRootRelativeURL();
        execStatus = Status.parse(PropertyArray.get(props, EXEC_STATUS));
    }

    /**
     * Create a temporary test result for which can be handed around
     * in situations where a reasonable test result can't be created.
     *
     * @param td     Description of the test
     * @param s      Status to associate with running the test... presumed
     *               to be of the Status.FAILED type.
     */
    public TestResult(TestDescription td, Status s) {
        desc = td;
        testURL = desc.getRootRelativeURL();
        resultsFile = null;
        execStatus = s;
        props = emptyStringArray;
    }

    /**
     * Create a placeholder TestResult for a test that has not yet been run.
     *
     * @param td     The test description for the test
     * @return       A test result that indicates that the test has not yet been run
     */
    public static TestResult notRun(TestDescription td) {
        return new TestResult(td, notRunStatus);
    }

    //------------------------ MODIFIER METHODS ------------------------------

    /**
     * Create a new section inside this test result.
     *
     * @param name The symbolic name for this new section.
     * @return The new section that was created.
     */
    public synchronized TestResult.Section createSection(String name) {
        if (!isMutable()) {
            throw new IllegalStateException(
                        "This TestResult is no longer mutable!");
        }

        Section section = new Section(name);
        sections = (Section[])(DynamicArray.append(sections, section));
        notifyCreatedSection(section);
        // avoid creating output (which will cause observer messages)
        // before the createdSection has been notified
        section.createOutput(TestResult.MESSAGE_OUTPUT_NAME);

        return section;
    }


    /**
     * Set the environment used by this test. When the test is run,
     * those entries in the environment that are referenced are noted;
     * those entries will be recorded here in the test result object.
     * @param environment the test environment used by this test.
     * @see #getEnvironment
     */
    public synchronized void setEnvironment(TestEnvironment environment) {
        if (!isMutable()) {
            throw new IllegalStateException(
                        "This TestResult is no longer mutable!");
        }
        for (Iterator i = environment.elementsUsed().iterator(); i.hasNext(); ) {
            TestEnvironment.Element elem = (TestEnvironment.Element) (i.next());
            // this is stunningly inefficient and should be fixed
            env = PropertyArray.put(env, elem.getKey(), elem.getValue());
        }
    }

    /**
     * Set the result of this test.  This action makes this object immutable.
     * If a result comparison is needed, it will be done in here.
     * @param stat A status object representing the outcome of the test
     * @see #getStatus
     */
    public synchronized void setStatus(Status stat) {
        if (!isMutable()) {
            throw new IllegalStateException(
                        "This TestResult is no longer mutable!");
        }

        if (stat == null) {
            throw new IllegalArgumentException(
                        "TestResult status cannot be set to null!");
        }

        // close out message section
        sections[0].setStatus(null);

        execStatus = stat;

        if (execStatus == inProgress)
            execStatus = interrupted;

        // verify integrity of status in all sections
        for (int i = 0; i < sections.length; i++) {
            if (sections[i].isMutable()) {
                sections[i].setStatus(incomplete);
            }
        }

        props = PropertyArray.put(props, SECTIONS,
                                  StringArray.join(getSectionTitles()));
        props = PropertyArray.put(props, EXEC_STATUS,
                                  execStatus.toString());

        // end time now required
        // mainly for writing in the TRC for the Last Run Filter
        if (PropertyArray.get(props, END) == null) {
            props = PropertyArray.put(props, END, formatDate(new Date()));
        }

        // this object is now immutable
        notifyCompleted();
    }

    /**
     * Add a new property value to this TestResult.
     *
     * @param name The name of the property to be updated.
     * @param value The new value of the specified property.
     */
    public synchronized void putProperty(String name, String value) {
        // check mutability
        if (!isMutable()) {
            throw new IllegalStateException(
                "Cannot put property, the TestResult is no longer mutable!");
        }

        props = PropertyArray.put(props, name, value);
        notifyUpdatedProperty(name, value);
    }

    /**
     * Reconstruct the results of a previously run test.
     *
     * @param workDir Work directory in which the tests were run
     * @throws TestResult.Fault if an error occurs while reloading the results
     */
    public void reloadFromWorkDir(WorkDirectory workDir) throws Fault {
        // check mutability
        if (!isMutable()) {
            throw new IllegalStateException(
                "Cannot reload results, the TestResult is no longer mutable!");
        }


        try {
            resultsFile = workDir.getFile(getWorkRelativePath());
            props = null;
            sections = null;
            execStatus = null;

            reload(new FileReader(resultsFile));

            // this next line is dubious since the execStatus should have
            // been set during the reload
            execStatus = Status.parse(PropertyArray.get(props, EXEC_STATUS));
        }
        catch (FileNotFoundException e) {
            props = emptyStringArray;
            env = emptyStringArray;
            sections = emptySectionArray;
            execStatus = Status.notRun("no test result file found");
        }
        catch (IOException e) {
            props = emptyStringArray;
            env = emptyStringArray;
            sections = emptySectionArray;
            execStatus = Status.error("error opening result file: " + e);
            throw new Fault(i18n, "rslt.badFile", e.toString());
        }
        catch (Fault f) {
            props = emptyStringArray;
            env = emptyStringArray;
            sections = emptySectionArray;
            execStatus = Status.error(f.getMessage());
            throw f;
        }

    }

    //----------ACCESS FUNCTIONS (MISC)-----------------------------------------


    /**
     * A code indicating that no checksum was found in a .jtr file.
     * @see #getChecksumState
     */
    public static final int NO_CHECKSUM = 0;

    /**
     * A code indicating that an invalid checksum was found in a .jtr file.
     * @see #getChecksumState
     */
    public static final int BAD_CHECKSUM = 1;

    /**
     * A code indicating that a good checksum was found in a .jtr file.
     * @see #getChecksumState
     */
    public static final int GOOD_CHECKSUM = 2;

    /**
     * The number of different checksum states (none, good, bad).
     */
    public static final int NUM_CHECKSUM_STATES = 3;


    /**
     * Get info about the checksum in this object.
     * @return a value indicating the validity or otherwise of the checksum
     * found while reading this result object.
     * @see #NO_CHECKSUM
     * @see #BAD_CHECKSUM
     * @see #GOOD_CHECKSUM
     */
    public byte getChecksumState() {
        return checksumState;
    }

    /**
     * A way to write comments about the test execution into the results.
     *
     * @return If this is null, then the object is in a state in which it
     *         does not accept new messages.
     */
    public PrintWriter getTestCommentWriter() {
        return sections[0].getMessageWriter();
    }

    /**
     * Get the test name, as given by the test URL defined by
     * TestDescription.getRootRelativeURL().  This method <em>always</em>
     * returns a useful string, representing the test name.
     *
     * @return the name of the test for which this is the result object
     * @see TestDescription#getRootRelativeURL
     */
    public String getTestName() {
        return testURL;
    }

    /**
     * Check whether this test result can be reloaded from a file.
     * This method does not validate the contents of the file.
     * @return true if the result file for this object can be read
     */
    public boolean isReloadable() {
        return (resultsFile != null && resultsFile.canRead());
    }

    /**
     * Check whether this object has been "shrunk" to reduce its
     * memory footprint. If it has, some or all of the data will have
     * to be reloaded.  This method is somewhat
     * orthogonal to <code>isReloadable()</code> and should not be used as a
     * substitute.
     *
     * @return True if this object is currently incomplete, false otherwise.
     * @see #isReloadable
     */
    public boolean isShrunk() {
        if (!isMutable() &&
            (desc == null ||
             props == null ||
             env == null ||
             (sections == null && execStatus != inProgress)))
            return true;
        else
            return false;
    }

    /**
     * Get the description of the test from which this result was created.
     * Depending on how the test result was created, this information may
     * not be immediately available, and may be recreated from the test
     * result file.
     *
     * @return the test description for this test result object
     * @throws TestResult.Fault if there is a problem recreating the description
     * from the results file.
     */
    public synchronized TestDescription getDescription()
                throws Fault {
        if (desc == null) {
            // reconstitute description (probably from file)
            reload();
        }
        return desc;
    }

    /*
     * Get the title of this test. This info originally comes from the test
     * description, but is saved in the .jtr file as well.
     *
     * @deprecated Please query the test description for info.
    public String getTitle() {
        // hmm slight copout; would like to make sure never null in the first place
        String title = desc.getParameter("title");
        if (title == null)
            title = td.getRootRelativeURL();
    }
     */

    /**
     * Get the path name for the results file for this test, relative to the
     * work directory.  The internal separator is '/'.
     * @return the path name for the results file for this test,
     * relative to the work directory
     */
    public String getWorkRelativePath() {
        return getWorkRelativePath(testURL);
    }

    /**
     * Get the name, if any, for the result file for this object.
     * The path information contains platform specific path separators.
     * @return the name, if any, for the result file for this object
     */
    public File getFile() {
        return resultsFile;
    }

    public void resetFile() {
        resultsFile = null;
    }

    /**
     * Get the path name for the results file for a test, relative to the
     * work directory.  The internal separator is '/'.
     * @param td the test description for the test in question
     * @return the path name for the results file for a test, relative to the
     * work directory
     */
    public static String getWorkRelativePath(TestDescription td) {
        String baseURL = td.getRootRelativePath();

        // add in uniquifying id if
        String id = td.getParameter("id");
        return getWorkRelativePath(baseURL, id);
    }

    /**
     * Get the path name for the results file for a test, relative to the
     * work directory.  The internal separator is '/'.
     *
     * @param testURL May not be null;
     * @return The work relative path of the JTR for this test.  Null if the
     *         given URL is null.
     */
    public static String getWorkRelativePath(String testURL) {
        int pound = testURL.lastIndexOf("#");
        if (pound == -1)        // no test id
            return getWorkRelativePath(testURL, null);
        else
            return getWorkRelativePath(testURL.substring(0, pound),
                                       testURL.substring(pound + 1));
    }

    /**
     * Get the path name for the results file for a test, relative to the
     * work directory.  The internal separator is '/'.
     *
     * @param baseURL May not be null;
     * @param testId The test identifier that goes with the URL.  This may be null.
     * @return The work relative path of the JTR for this test.  Null if the
     *         given URL is null.
     */
    public static String getWorkRelativePath(String baseURL, String testId) {
        StringBuffer sb = new StringBuffer(baseURL);

        // strip off extension
    stripExtn:
        for (int i = sb.length() - 1; i >= 0; i--) {
            switch (sb.charAt(i)) {
            case '.':
                sb.setLength(i);
                break stripExtn;
            case '/':
                break stripExtn;
            }
        }

        // add in uniquifying id if
        if (testId != null) {
            sb.append('_');
            sb.append(testId);
        }

        sb.append(EXTN);

        return sb.toString();
    }

    /**
     * Get the keys of the properties that this object has stored.
     * @return the keys of the properties that this object has stored
     */
    public synchronized Enumeration getPropertyNames() {
        return PropertyArray.enumerate(props);
    }

    /**
     * Get the value of a property of this test result.
     *
     * @param name The name of the property to be retrieved.
     * @return The value corresponding to the property name, null if not
     *          found.
     * @throws TestResult.Fault if there is a problem
     *          recreating data from the results file.
     */
    public synchronized String getProperty(String name)
            throws Fault {
        if (props == null) {
            // reconstitute properties
            // this may result in a Fault, which is okay
            reload();
        }

        return PropertyArray.get(props, name);
    }

    /**
     * Get a copy of the environment that this object has stored.
     * @return a copy of the environment that this object has stored
     * @throws TestResult.Fault if there is a problem
     *          recreating data from the results file.
     * @see #setEnvironment
     */
    public synchronized Map getEnvironment() throws Fault {
        if (env == null) {
            // reconstitute environment
            // this may result in a Fault, which is okay
            reload();
        }
        return PropertyArray.getProperties(env);
    }

    /**
     * Get the parent node in the test result table that
     * contains this test result object.
     * @return the parent node in the test result table that
     * contains this test result object.
     */
    public TestResultTable.TreeNode getParent() {
        return parent;
    }


    /**
     * Set the parent node in the test result table that
     * contains this test result object.
     * @param p the parent node in the test result table that
     * contains this test result object.
     * @see #getParent
     */
    void setParent(TestResultTable.TreeNode p) {
        parent = p;
    }

    //----------ACCESS FUNCTIONS (TEST STATUS)----------------------------------

    /**
     * Determine if the test result object is still mutable.
     * Test results are only mutable while they are being created, up to
     * the point that the final status is set.
     * @return true if the test result object is still mutable,
     * and false otherwise
     */
    public synchronized boolean isMutable() {
        // must be mutable during reload (internal operation)
        // mutable as long as possible, to allow max time for writing log messages
        return (execStatus == inProgress);
    }


    /**
     * Get the status for this test.
     * @return the status for this test
     * @see #setStatus
     */
    public synchronized Status getStatus() {
        return execStatus;
    }

    //----------ACCESS METHODS (TEST OUTPUT)----------------------------------

    /**
     * Find out how many sections this test result contains.
     *
     * @return The number of sections in this result.
     */
    public synchronized int getSectionCount() {
        if (sections != null) {
            return sections.length;
        }
        else if (PropertyArray.get(props, SECTIONS) != null) {
            return parseSectionCount(PropertyArray.get(props, SECTIONS));
        }
        else {
            // hum, test props are never discarded, so we have no sections
            return 0;
        }
    }

    /**
     * Get the section specified by index.
     * Remember that index 0 is the default message section.
     *
     * @param index The index of the section to be retrieved.
     * @return The requested section.  Will be null if the section does not exist.
     * @throws TestResult.ReloadFault Will occur if an error is encountered when reloading
     *         JTR data.  This may be the result of a corrupt or missing JTR file.
     * @see #MSG_SECTION_NAME
     */
    public synchronized Section getSection(int index) throws ReloadFault {
        Section target;

        if (sections == null && execStatus != inProgress) {
            // try to reload from file
            try {
                reload();
            }
            catch (ReloadFault f) {
                throw f;
            }
            catch (Fault f) {
                throw new ReloadFault(i18n, "rslt.badFile",  f.getMessage());
            }
        }

        if (index >= sections.length) {
            target = null;
        }
        else {
            target = sections[index];
        }

        return target;
    }

    /**
     * Get the titles of all sections in this test result.
     * A null result probably indicates that there are no sections.  This is
     * improbable since most test result object automatically have one section.
     *
     * @return The titles, one at a time in the array.  Null if the titles
     *          do not exist or could not be determined.
     */
    public synchronized String[] getSectionTitles() {
        if (props == null) {
            try {
                reload();
            }
            catch (Fault f) {
                // should this maybe be a JavaTestError?
                return null;
            }
        }

        // look for what we need from easiest to hardest source
        String names = PropertyArray.get(props, SECTIONS);

        if (names != null) {
            // it is cached
            return StringArray.split(names);
        }
        else if (sections != null) {
            // TR is not immutable yet, probably
            int numSections = getSectionCount();
            String[] data = new String[numSections];

            for (int i = 0; i < numSections; i++) {
                data[i] = sections[i].getTitle();
            }

            return data;
        }
        else {
            // hum, bad.  No sections exist and this data isn't cached
            // the test probably has not run
            return null;
        }
    }

    /**
     * Check if this file is or appears to be a result (.jtr) file,
     * according to its filename extension.
     * @param f the file to be checked
     * @return true if this file is or appears to be a result (.jtr) file.
     */
    public static boolean isResultFile(File f) {
        String p = f.getPath();
        return (p.endsWith(EXTN));
    }

    /**
     * Writes the TestResult into a version 2 jtr file.
     *
     * @param workDir The work directory in which to write the results
     * @param backupPolicy a policy object defining what to do if a file
     * already exists with the same name as that which is about to be written.
     * @throws IllegalStateException This will occur if you attempt to write a result
     *         which is still mutable.
     * @throws IOException Occurs when the output file cannot be created or written to.
     *         Under this condition, this object will change it status to an error.
     */
    public synchronized void writeResults(WorkDirectory workDir, BackupPolicy backupPolicy)
                throws IOException
    {
        if (isMutable())
            throw new IllegalStateException("This TestResult is still mutable - set the status!");

        // could attempt a reload() I suppose
        if (props == null)
            props = emptyStringArray;

        String wrp = getWorkRelativePath(desc).replace('/', File.separatorChar);
        resultsFile = workDir.getFile(wrp);

        File resultsDir = resultsFile.getParentFile();
        resultsDir.mkdirs(); // ensure directory created for .jtr file

        File tempFile = createTempFile(workDir, backupPolicy);
        try {
            writeResults(tempFile, backupPolicy);
        }
        finally {
            if (tempFile.exists())
                tempFile.delete();
        }
    }

    /**
     * Create a temporary file to which the results can be written, before being renamed
     * to its real name.
     */
    // don't use File.createTempFile because of issues with the internal locking there
    private File createTempFile(WorkDirectory workDir, BackupPolicy backupPolicy)
        throws IOException
    {
        final int MAX_TRIES = 100; // absurdly big limit, but a limit nonetheless
        for (int i = 0; i < MAX_TRIES; i++) {
            File tempFile = new File(resultsFile.getPath() + "." + i + ".tmp");
            if (tempFile.createNewFile())
                return tempFile;
        }
        throw new IOException("could not create temp file for " + resultsFile + ": too many tries");
    }

    /**
     * Write the results to a temporary file, and when done, rename it to resultsFile
     */
    private void writeResults(File tempFile, BackupPolicy backupPolicy)
        throws IOException
    {
        FileWriter out;
        try {
            out = new FileWriter(tempFile);
        }
        catch (IOException e) {
            execStatus = Status.error("Problem writing result file for test: " + getTestName());
            resultsFile = null; // file not successfully written after all
            throw e;
        }

        try {
            // redundant, is done in setResult
            // needed though if setResult isn't being called
            props = PropertyArray.put(props, EXEC_STATUS, execStatus.toString());

            // file header
            out.write(JTR_V2_HEADER);
            out.write(lineSeparator);

            // date and time
            out.write("#" + (new Date()).toString());
            out.write(lineSeparator);

            // checksum header and data
            //out.write(JTR_V2_CHECKSUM);
            //out.write(Long.toHexString(computeChecksum()));
            //out.write(lineSeparator);
 /*
            if (debug) {  // debugging code
                out.write("# debug: test desc checksum: ");
                out.write(Long.toHexString(computeChecksum(desc)));
                out.write(lineSeparator);

                for (Iterator iter = desc.getParameterKeys(); iter.hasNext(); ) {
                    // don't rely on enumeration in a particular order
                    // so simply add the checksum products together
                    String KEY = (String) (iter.next());
                    out.write("# debug: test desc checksum key " + KEY + ": ");
                    out.write(Long.toHexString(computeChecksum(KEY) * computeChecksum(desc.getParameter(KEY))));
                    out.write(lineSeparator);
                }

                out.write("# debug: test env checksum: ");
                if (env == null)
                    out.write("null");
                else
                    out.write(Long.toHexString(computeChecksum(env)));
                out.write(lineSeparator);

                out.write("# debug: test props checksum: ");
                out.write(Long.toHexString(computeChecksum(props)));
                out.write(lineSeparator);

                out.write("# debug: test sections checksum: ");
                out.write(Long.toHexString(computeChecksum(sections)));
                out.write(lineSeparator);

                for (int I = 0; I < sections.length; I++) {
                    out.write("# debug: test section[" + I + "] checksum: ");
                    out.write(Long.toHexString(computeChecksum(sections[I])));
                    out.write(lineSeparator);

                    String[] NAMES = sections[I].getOutputNames();
                    for (int J = 0; J < NAMES.length; J++) {
                        out.write("# debug: test section[" + I + "] name=" + NAMES[J] + " checksum: ");
                        out.write(Long.toHexString(computeChecksum(NAMES[J])));
                        out.write(lineSeparator);

                        out.write("# debug: test section[" + I + "] name=" + NAMES[J] + " output checksum: ");
                        out.write(Long.toHexString(computeChecksum(sections[I].getOutput(NAMES[J]))));
                        out.write(lineSeparator);
                    }
                }
            }*/

            // description header and data
            out.write(JTR_V2_TESTDESC);
            out.write(lineSeparator);

            Properties tdProps = new Properties();
            desc.save(tdProps);
            PropertyArray.save(PropertyArray.getArray(tdProps), out);
            out.write(lineSeparator);

            // test environment header and data
            if (env != null) {
                out.write(JTR_V2_ENVIRONMENT);
                out.write(lineSeparator);
                PropertyArray.save(env, out);
                out.write(lineSeparator);
            }

            // test result props header and data
            out.write(JTR_V2_RESPROPS);
            out.write(lineSeparator);
            PropertyArray.save(props, out);
            out.write(lineSeparator);

            // get sections into memory
            // I hope the out stream is not the same as the resultFile!
            if (sections == null) {
                throw new JavaTestError("Cannot write test result - it contains no sections.");
            }

            for (int i = 0; i < sections.length; i++) {
                sections[i].save(out);
            }

            out.write(lineSeparator);
            out.write(JTR_V2_TSTRESULT);
            out.write(execStatus.toString());
            out.write(lineSeparator);
            out.close();
        }   // try
        catch (IOException e) {
            // This exception could be raised when trying to create the directory
            // for the test results; opening the results file, or closing it.
            execStatus = Status.error("Write to temp. JTR file failed (old JTR intact): " +
                                        tempFile.getPath());
            resultsFile = null; // file not successfully written after all
            throw e;
        }   // catch

        try {
            backupPolicy.backupAndRename(tempFile, resultsFile);

            // now that it has been successfully written out, make the object
            // a candidate for shrinking
            addToShrinkList();
        }   // try
        catch (IOException e) {
            // This exception could be raised when trying to create the directory
            // for the test results; opening the results file, or closing it.
            execStatus = Status.error("Problem writing result file: " +
                                        resultsFile.getPath());
            resultsFile = null; // file not successfully written after all
            throw e;
        }   // catch
    }

    // -----observer methods ---------------------------------------------------
    /**
     * Add an observer to watch this test result for changes.
     * @param obs the observer to be added
     */
    public synchronized void addObserver(Observer obs) {
        if (isMutable()) {
            Observer[] observers = (Observer[])observersTable.get(this);

            if (observers == null) observers = new Observer[0];

            observers = (Observer[])(DynamicArray.append(observers, obs));
            observersTable.put(this, observers);
        }
    }

    /**
     * Remove an observer that was previously added.
     * @param obs the observer to be removed
     */
    public synchronized void removeObserver(Observer obs) {
        Observer[] observers = (Observer[])observersTable.get(this);
        if (observers == null)
            return;

        observers = (Observer[])(DynamicArray.remove(observers, obs));
        if (observers == null)
            observersTable.remove(this);
        else
            observersTable.put(this, observers);
    }

    /**
     * Gets the time when the test was completed, or at least the time
     * when it's final status was set.  Be aware that if the information is
     * not available in memory, it will be loaded from disk.
     *
     * @return Time when this test acquired its final status setting.
     * @see #setStatus
     * @see java.util.Date
     */
    public long getEndTime() {
        if (endTime < 0) {
            try {
                String datestr = PropertyArray.get(props, END);

                if (datestr == null) {
                    // this may be more expensive because it can cause a
                    // reload from disk
                    try {
                        datestr = getProperty(END);
                    }
                    catch (Fault f) {
                    }
                }

                if (datestr != null) {
                    Date date = parseDate(datestr);
                    endTime = date.getTime();
                }
                else {
                    // info not available
                }
            }
            catch (ParseException e) {
            }
        }

        return endTime;
    }

    /**
     * Parse the date format used for timestamps, such as the start/stop timestamp.
     * @param s The string containing the date to be restored.
     * @see #formatDate
     */
    public static synchronized Date parseDate(String s) throws ParseException {
        return dateFormat.parse(s);
    }

    /**
     * Format the date format used for timestamps, such as the start/stop timestamp.
     * @param d The date object to be formatted into a string.
     * @see #parseDate
     */
    public static synchronized String formatDate(Date d) {
        return dateFormat.format(d);
    }

    // ----- PACKAGE METHODS ---------------------------------------------------

    /**
     * Read a single minimal TestResult from a .jts stream.
     * The stream is not closed.
     * @deprecated JTS files are no longer supported
    TestResult(WorkDirectory workDir, DataInputStream in) throws IOException {
        workRelativePath = in.readUTF();

        // ** temp. fix ** XXX
        // make sure the path is in URL form with forward slashes
        // in the future all paths should already be of this form (TestDescription)
        int index = workRelativePath.indexOf('/');
        if (index == -1) workRelativePath = workRelativePath.replace('\\', '/');

        resultsFile = workDir.getFile(workRelativePath.replace('/', File.separatorChar));
        title = in.readUTF();
        int esc = in.readByte();
        String esr = in.readUTF();
        execStatus = new Status(esc, esr);
        boolean defIsExec = in.readBoolean();
        if (!defIsExec) {
            // have to read these, per protocol
            int dsc = in.readByte();
            String dsr = in.readUTF();
            //ignore dsc, dsr; they used to go in defStatus
        }
    }
     */

    /**
     * Read a single minimal TestResult which is capable of reloading itself.
     * None of the parameters may be null.
     *
     * @param url The full URL of this test, including test id.
     * @param workDir The work directory location, platform specfic path.
     * @param status The status that will be found in the JTR.
     * @throws JavaTestError Will be thrown if any params are null.
     */
    TestResult(String url, WorkDirectory workDir, Status status) {
        if (url == null)
            throw new JavaTestError(i18n, "rslt.badTestUrl");

        if (workDir == null)
            throw new JavaTestError(i18n, "rslt.badWorkdir");

        if (status == null)
            throw new JavaTestError(i18n, "rslt.badStatus");

        testURL = url;
        resultsFile = workDir.getFile(getWorkRelativePath());
        execStatus = status;
    }

    /**
     * Read a single minimal TestResult which is capable of reloading itself.
     * None of the parameters may be null.
     *
     * @param url The full URL of this test, including test id.
     * @param workDir The work directory location, platform specific path.
     * @param status The status that will be found in the JTR.
     * @param endTime The time when that test finished execution.
     * @throws JavaTestError Will be thrown if any params are null.
     * @see #getEndTime()
     */
    TestResult(String url, WorkDirectory workDir, Status status, long endTime) {
        if (url == null)
            throw new JavaTestError(i18n, "rslt.badTestUrl");

        if (workDir == null)
            throw new JavaTestError(i18n, "rslt.badWorkdir");

        if (status == null)
            throw new JavaTestError(i18n, "rslt.badStatus");

        testURL = url;
        resultsFile = workDir.getFile(getWorkRelativePath());
        execStatus = status;
        this.endTime = endTime;
    }

    void shareStatus(Hashtable[] tables) {
        execStatus = shareStatus(tables, execStatus);
    }

    /*
     * @deprecated JTS files no longer supported
    void writeSummary(DataOutputStream out) throws IOException {
        out.writeUTF(workRelativePath);
        out.writeUTF(title);
        out.writeByte(execStatus.getType());
        out.writeUTF(execStatus.getReason());
        out.writeBoolean(true); // defStatus == execStatus
    }
    */

    String[] getTags() {
        // Script or someone else could possibly do this w/the observer
        if (sections == null) {
            return null;
        }

        Vector tagV = new Vector(sections.length * 2);

        for (int i = 0; i < sections.length; i++) {
            String[] names = sections[i].getOutputNames();

            for (int j = 0; j < names.length; j++) {
                tagV.addElement(names[j]);
            }   // inner for
        } // outer for

        String[] tagA = new String[tagV.size()];
        tagV.copyInto(tagA);

        return tagA;
    }

    /**
     * Insert a test description into this test results.
     * This will only work if the test description is currently not available.
     * The name in the test description must match the name of this test.
     * @param td The new test description, a null value will have no effect.
     * @see #isShrunk()
     * @throws IllegalStateException If the state of this object fobiu
     */
    void setTestDescription(TestDescription td) {
        if (td == null)
            return;

        String name = td.getRootRelativeURL();
        if (!testURL.equals(name))
            throw new IllegalStateException();

        if (desc != null) {             // compare if possible
            if (!desc.equals(td)) {     // test descriptions are not the same
                // accept new TD, reset this TR
                // reset status to a special one
                execStatus = tdMismatch;
                desc = td;

                props = emptyStringArray;
                resultsFile = null;
                env = emptyStringArray;
                sections = emptySectionArray;

                if (isMutable())
                    createSection(MSG_SECTION_NAME);
            }
            else {
                // TDs are equal, no action, drop thru and return
            }
        }
        else {
            desc = td;
        }
    }

    // ----- PRIVATE METHODS ---------------------------------------------------

    /**
     * @deprecated Use the Section API to accomplish your task.
     */
    private static Reader getLastRefOutput(TestResult tr) {
        try {
            Section lastBlk = tr.getSection(tr.getSectionCount() - 1);
            return new StringReader(lastBlk.getOutput("ref"));
        }
        catch (ReloadFault f) {
            // not the best, but this method is deprecated and hopefully never
            // called
            return null;
        }
    }

    private long computeChecksum() {
        long cs = 0;
        cs = cs * 37 + computeChecksum(desc);
        // in JT2.1.1a, environment was not included in checksum,
        // so allow that for backward compatibility
        String jtv = PropertyArray.get(props, VERSION);
        if (env != null) {
            if (jtv == null || !jtv.equals("JT_2.1.1a"))
                cs = cs * 37 + computeChecksum(env);
        }
        cs = cs * 37 + computeChecksum(props);
        if (sections != null)
            cs = cs * 37 + computeChecksum(sections);
        cs = cs * 37 + execStatus.getType() + computeChecksum(execStatus.getReason());
        return Math.abs(cs);  // ensure +ve, to avoid sign issues!
    }

    private static long computeChecksum(TestDescription td) {
        long cs = 0;
        for (Iterator i = td.getParameterKeys(); i.hasNext(); ) {
            // don't rely on enumeration in a particular order
            // so simply add the checksum products together
            String key = (String) (i.next());
            cs += computeChecksum(key) * computeChecksum(td.getParameter(key));
        }
        return cs;
    }

    private static long computeChecksum(Section[] sections) {
        long cs = sections.length;
        for (int i = 0; i < sections.length; i++) {
            cs = cs * 37 + computeChecksum(sections[i]);
        }
        return cs;
    }

    private static long computeChecksum(Section s) {
        long cs = computeChecksum(s.getTitle());
        String[] names = s.getOutputNames();
        for (int i = 0; i <names.length; i++) {
            cs = cs * 37 + computeChecksum(names[i]);
            cs = cs * 37 + computeChecksum(s.getOutput(names[i]));
        }
        return cs;
    }

    private static long computeChecksum(String[] strings) {
        long cs = strings.length;
        for (int i = 0; i < strings.length; i++) {
            cs = cs * 37 + computeChecksum(strings[i]);
        }
        return cs;
    }

    private static long computeChecksum(String s) {
        long cs = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            //if (!Character.isISOControl(c) || c == '\n')
            cs = cs * 37 + c;
        }
        return cs;
    }

    /**
     * @throws ResultFileNotFoundFault May be thrown if the JTR file cannot be found.
     * @throws ReloadFault Generally describes any error which is encountered while
     *            reading or processing the input file.
     */
    private synchronized void reload()
        throws ResultFileNotFoundFault, ReloadFault
    {
        if (resultsFile == null)
            throw new ReloadFault(i18n, "rslt.noResultFile");

        if (isMutable())
            throw new IllegalStateException("Cannot do a reload of this object.");

        try {
            reload(new FileReader(resultsFile));

            // Well, we have successfully reloaded it, so the object is now taking
            // up a big footprint again ... put it back on the list to be shrunk again
            addToShrinkList();
        }
        catch (FileNotFoundException e) {
            throw new ResultFileNotFoundFault(i18n, "rslt.fileNotFound", resultsFile);
        }
        catch (IOException e) {
            throw new ReloadFault(i18n, "rslt.badFile", e);
        }
    }

    /**
     * @throws ReloadFault Generally describes any error which is encountered while
     *            reading or processing the input file.  This may indicate
     *            an empty file or incorrectly formatted file.
     */
    private void reload(Reader r)
        throws ReloadFault, IOException
    {
        try {
            BufferedReader br = new BufferedReader(r);
            String line = br.readLine();

            // determine JTR version
            if (line == null) {
                throw new ReloadFault(i18n, "rslt.empty", resultsFile);
            }
            if (line.equals(JTR_V2_HEADER)) {
                reloadVersion2(br);
            }
            else if (line.equals(JTR_V1_HEADER)) {
                reloadVersion1(br);
            }
            else
                throw new ReloadFault(i18n, "rslt.badHeader", resultsFile);
        }
        finally {
            r.close();
        }
    }

    private void reloadVersion1(BufferedReader in)
        throws ReloadFault, IOException
    {
        // grab property info
        StringBuffer buff = new StringBuffer();
        String line = in.readLine();
        while (!(line == null) && !(line.length() == 0)) {
            buff.append(line);
            buff.append(lineSeparator);
            line = in.readLine();
        }

        // store if needed
        Properties pairs = new Properties();
        if (props == null || desc == null) {
            StringReader sr = new StringReader(buff.toString());
            buff = null;
            line = null;

            pairs = new Properties();
            pairs.load(sr);
        }

        if (props == null) {
            // reload test result properties
            props = PropertyArray.getArray(pairs);
        }

        pairs = null;

        if (desc == null) {
            File path = new File((String)(PropertyArray.get(props, "testsuite")));
            if (!path.isDirectory())
                path = new File(path.getParent());
            File file = new File(PropertyArray.get(props, "file"));

            uniquifyStrings(props);

            desc = new TestDescription(path, file,
                                       PropertyArray.getProperties(props));
        }

        buff = new StringBuffer();
        line = in.readLine();
        while (!(line == null)) {
            if (line.startsWith("command: ")) {
                // a section
                Section blk = processOldSection(line, in);

                if (blk != null) {
                    sections = (Section[])(DynamicArray.append(sections, blk));
                }
            }
            else if (line.startsWith(JTR_V1_TSTRESULT)) {
                // test result
                if (line == null) {
                    // couldn't get the status text for some reason
                }
                else {
                    line = extractSlice(line, JTR_V1_TSTRESULT.length(), " ", null);
                    execStatus = Status.parse(line);
                }   // inner else

                break;
            }
            else {
                // message text
                buff.append(line);
                buff.append(lineSeparator);
            }   // else

            line = in.readLine();
        }   // while

        // create the test message section and put first in the array
        Section blk = new Section(MSG_SECTION_NAME);
        blk.reloadOutput(MESSAGE_OUTPUT_NAME, buff.toString());
        Section[] tempBlks = new Section[sections.length+1];
        tempBlks[0] = blk;
        System.arraycopy(sections, 0, tempBlks, 1, sections.length);
        sections = tempBlks;
    }

    private Section processOldSection(String line1, BufferedReader in)
        throws ReloadFault, IOException
    {
        StringBuffer sb = new StringBuffer();         // message stream
        Section section = null;
        String line = line1;
        while (!(line == null)) {
            if (line.startsWith("----------")) {
                String streamName = null;
                String sectionName = null;
                StringBuffer buff = new StringBuffer();
                int lines = 0;
                int chars = 0;
                try {
                    streamName = extractSlice(line, 10, null, ":");
                    sectionName = extractSlice(line, 10, ":", "(");
                    lines = Integer.parseInt(extractSlice(line, 10, "(", "/"));
                    chars = Integer.parseInt(extractSlice(line, 10, "/", ")"));

                    for (int count = 0; count < lines; count++) {
                        buff.append(in.readLine());
                    }
                }
                catch (NumberFormatException e) {
                    // confused!
                    throw new ReloadFault(i18n, "rslt.badFile", e);
                }

                if (section == null)
                    section = new Section(sectionName);

                section.reloadOutput(streamName, buff.toString());
            }
            else if (line.startsWith(JTR_V1_SECTRESULT)) {
                // set result
                if (section == null)
                    section = new Section("");

                // get the Status text
                line = extractSlice(line, JTR_V1_SECTRESULT.length(), " ", null);

                if (line == null)
                    // couldn't get the status text for some reason
                    throw new ReloadFault(i18n, "rslt.noSectionResult");
                else
                    section.reloadStatus(Status.parse(line));

                break;
            }
            else {
                // just a plain message
                sb.append(line);
                sb.append(lineSeparator);
            }

            line = in.readLine();
        }

        if (section != null)
            section.reloadOutput(MESSAGE_OUTPUT_NAME, sb.toString());

        return section;
    }

    private void reloadVersion2(BufferedReader in)
        throws ReloadFault, IOException
    {
        //String checksumText = null;
        String line;

        // look for optional checksum and then test description,
        // skipping comments
        while ((line = in.readLine()) != null) {
            if (line.equals(JTR_V2_TESTDESC))
                break;
            //else if (line.startsWith(JTR_V2_CHECKSUM)) {
                //checksumText = line.substring(JTR_V2_CHECKSUM.length());
            //}
            else if (!line.startsWith("#"))
                throw new ReloadFault(i18n, "rslt.badLine", line);
        }

        // this probably won't work with a normal Properties object
        String[] tdProps = PropertyArray.load(in);

        if (desc == null) {
            uniquifyStrings(tdProps);
            desc = TestDescription.load(tdProps);
        }
        tdProps = null;                // dump it

        // XXX compare to TD

        // remove comment lines and look for test env props
        while ((line = in.readLine()) != null) {
            if (line.startsWith(JTR_V2_RESPROPS))
                break;
            else if (line.startsWith(JTR_V2_ENVIRONMENT)) {
                env = PropertyArray.load(in);
                uniquifyStrings(env);
            }
            else if (!line.startsWith("#"))
                throw new ReloadFault(i18n, "rslt.badLine", line);
        }

        if (env == null)
            env = new String[] {};

        if (line == null) {
            throw new ReloadFault(i18n, "rslt.badFormat");
        }

        String[] trProps = PropertyArray.load(in);

        if (props == null) {
            // restore the properties of this result
            uniquifyStrings(trProps);
            props = trProps;
        }

        trProps = null;             // dump it

        // read the sections
        int sectionCount = parseSectionCount(PropertyArray.get(props, SECTIONS));
        sections = new Section[sectionCount];
        for (int i = 0; i < getSectionCount(); i++) {
            sections[i] = new Section(in);
        }

        // get the final test status
        while ((line = in.readLine()) != null) {
            if (line.startsWith(JTR_V2_TSTRESULT)) {
                execStatus = Status.parse(line.substring(JTR_V2_TSTRESULT.length()));
                break;
            }
        }

        if (execStatus == null)
            execStatus = Status.error("NO STATUS RECORDED IN FILE");

        // check whether checksum was valid or not
        //if (checksumText == null)
        checksumState = NO_CHECKSUM;
        /*else {
            try {
                long cs = Long.parseLong(checksumText, 16);
                if (cs == computeChecksum())
                    checksumState = GOOD_CHECKSUM;
                else
                    checksumState = BAD_CHECKSUM;
            }
            catch (RuntimeException e) {
                checksumState = BAD_CHECKSUM;
            }
        }*/
    }

    /**
     * This method tolerates null.  It expects a list of section names - basically
     * a space separated list and returns the number of items there.
     * @param s The section name list string to parse and count.  May be null.
     * @return Number of sections listed in the string.  Will be zero if the
     *     input was null.
     */
    int parseSectionCount(String s) {
        if (s == null || s.length() == 0) {
            return 0;
        }

        return StringArray.split(s).length;
    }

    void uniquifyStrings(String[] data) {
        for (int i = 0; i < data.length; i++)
            // don't do this for large strings
            if (data[i] != null && data[i].length() < 30)
                data[i] = data[i].intern();
    }

    /**
     * Extract a substring specified by a start and end pattern (string).
     * The start and end strings must be single chars.
     * @param s String to do this operation on
     * @param where Position in the string to start at
     * @param start Beginning pattern for the slice, exclusive.
     * @param end Ending pattern for the slice, exclusive.  Null means
     *            to-end-of-string.
     * @return The requested substring or null if error.
     */
    String extractSlice(String s, int where, String start, String end) {
        int startInd;
        int endInd;

        if (start == null)
            startInd = where;
        else {
            int i = s.indexOf(start, where);
            if (i < 0)
                return null;
            startInd = i + start.length();
        }

        if (end == null)
            endInd = s.length();
        else {
            endInd = s.indexOf(end, startInd);
            if (endInd == -1)
                return null;
        }

        try {
            return s.substring(startInd, endInd);
        }
        catch (StringIndexOutOfBoundsException e) {
            return null;
        }
    }


    private static boolean compare(Reader left, Reader right)
                throws Fault {
        try {
            try {
                for (;;) {
                    int l = left.read(), r = right.read();
                    if (l != r) {
                        return false; // different content found
                    }
                    if (l == -1)
                        return true;
                }
            }
            finally {
                left.close();
                right.close();
            }
        }
        catch (IOException e) {
            throw new Fault(i18n, "rslt.badCompare", e);
        }
    }

    private static Status shareStatus(Hashtable[] tables, Status s) {
        int type = s.getType();
        String reason = s.getReason();
        Status result = (Status)tables[type].get(reason);
        if (result == null) {
            tables[type].put(reason, s);
            result = s;
        }

        return result;
    }

    // ------------------------ OBSERVER MAINTENANCE -------------------------

    /**
     * Notify observers that a new section has been created.
     *
     * @param section The section that was created.
     */
    private synchronized void notifyCreatedSection(Section section) {
        Observer[] observers = (Observer[])(observersTable.get(this));
        if (observers != null)
            for (int i = 0; i < observers.length; i++)
                observers[i].createdSection(this, section);
    }

    /**
     * Notify observers that a section has been completed.
     *
     * @param section The section that was completed.
     */
    private synchronized void notifyCompletedSection(Section section) {
        Observer[] observers = (Observer[])(observersTable.get(this));
        if (observers != null)
            for (int i = 0; i < observers.length; i++)
                observers[i].completedSection(this, section);
    }

    /**
     * Notify observers that new output is being created.
     *
     * @param section The section that was created.
     * @param outputName The name of the output.
     */
    private synchronized void notifyCreatedOutput(Section section, String outputName) {
        Observer[] observers = (Observer[])(observersTable.get(this));
        if (observers != null)
            for (int i = 0; i < observers.length; i++)
                observers[i].createdOutput(this, section, outputName);
    }

    /**
     * Notify observers that a particular output has been completed.
     *
     * @param section The section that was completed.
     * @param outputName The name of the output.
     */
    private synchronized void notifyCompletedOutput(Section section, String outputName) {
        Observer[] observers = (Observer[])(observersTable.get(this));
        if (observers != null)
            for (int i = 0; i < observers.length; i++)
                observers[i].completedOutput(this, section, outputName);
    }

    /**
     * Notify all observers that new data has been written to some output.
     *
     * @param section The section being modified.
     * @param outputName The stream of the section that is being modified.
     * @param text The text that was added (appended).
     */
    private synchronized void notifyUpdatedOutput(Section section, String outputName, int start, int end, String text) {
        Observer[] observers = (Observer[])(observersTable.get(this));
        if (observers != null)
            for (int i = 0; i < observers.length; i++)
                observers[i].updatedOutput(this, section, outputName, start, end, text);
    }

    /**
     * Notify all observers that new data has been written to some output.
     *
     * @param section The section being modified.
     * @param outputName The stream of the section that is being modified.
     */
    private synchronized void notifyUpdatedOutput(Section section, String outputName, int start, int end,
                                                  char[] buf, int offset, int len) {
        Observer[] observers = (Observer[])(observersTable.get(this));
        if (observers != null) {
            // only create string if there are really observers who want to see it
            String text = new String(buf, offset, len);
            for (int i = 0; i < observers.length; i++)
                observers[i].updatedOutput(this, section, outputName, start, end, text);
        }
    }

    /**
     * Notify all observers that a property has been updated.
     *
     * @param key The key for the property that was modified.
     * @param value The new value for the property.
     */
    private synchronized void notifyUpdatedProperty(String key, String value) {
        Observer[] observers = (Observer[])(observersTable.get(this));
        if (observers != null)
            for (int i = 0; i < observers.length; i++)
                observers[i].updatedProperty(this, key, value);
    }

    /**
     * Notify observers the test has completed.
     */
    private synchronized void notifyCompleted() {
        // since there will be no more observer messages after this, there
        // is no need to keep any observers registered after we finish here
        // so get the observers one last time, and at the same time
        // remove them from the table
        Observer[] observers = (Observer[])(observersTable.remove(this));
        if (observers != null) {
            for (int i = 0; i < observers.length; i++)
                observers[i].completed(this);
            observersTable.remove(this);
        }

    }

    /**
     * @return Position of the specified section, or -1 if not found.
     */
    private synchronized int findSection(String name) {
        int location;

        if (sections == null || sections.length == 0) {
            return -1;
        }

        for (location = 0; location < sections.length; location++) {
            if (sections[location].getTitle().equals(name)) {
                // found
                break;
            }
        }   // for

        // loop exited because of counter, not a hit
        if (location == sections.length) {
            location = -1;
        }

        return location;
    }

    private void addToShrinkList() {
        synchronized (shrinkList) {
            // if this object is in the list; remove it;
            // if there are dead weak refs, remove them
            for (Iterator iter = shrinkList.iterator(); iter.hasNext(); ) {
                WeakReference wref = (WeakReference) (iter.next());
                Object o = wref.get();
                if (o == null || o == this)
                    iter.remove();
            }
            while (shrinkList.size() >= maxShrinkListSize) {
                WeakReference wref = (WeakReference) (shrinkList.removeFirst());
                TestResult tr = (TestResult) (wref.get());
                if (tr != null)
                    tr.shrink();
            }
            shrinkList.addLast(new WeakReference(this));
        }
    }

    /**
     * Tells the object that it can optimize itself for a small memory footprint.
     * Doing this may sacrifice performance when accessing object data.  This
     * only works on results that are immutable.
     */
    private synchronized void shrink() {
        if (isMutable()) {
            throw new IllegalStateException("Can't shrink a mutable test result!");
        }

        // Should ensure we have a resultsFile.
        sections = null;

        // NOTE: if either of these are discarded, it may be a good idea to
        //       optimize reload() to not read the section/stream data since
        //       a small property lookup could incur a huge overhead
        //props = null;         // works, may or may-not improve memory usage
        //desc = null;          // doesn't work in current implementation
    }

    // the following fields should be valid for all test results
    private File resultsFile;           // if set, location where test results are stored
    private Status execStatus;          // pre-compare result
    private String testURL;             // URL for this test, equal to the one in TD.getRootRelativeURL
    private long endTime = -1;          // when test finished
    private byte checksumState;         // checksum state
    // the following fields are candidates for shrinking although not currently done
    private TestDescription desc;       // test description for which this is the result
    private String[] props;             // table of values written during test execution
    private String[] env;
    // this field is cleared when the test result is shrunk
    private Section[] sections;         // sections of output written during test execution

    // only valid when this TR is in a TRT, should remain when shrunk
    private TestResultTable.TreeNode parent;

    // because so few test results will typically be observed (e.g. at most one)
    // we don't use a per-instance array of observers, but instead associate any
    // such arrays here.
    private static Hashtable observersTable = new Hashtable(16);

    /**
     * The name of the default output that all Sections are equipped with.
     */
    public static final String MESSAGE_OUTPUT_NAME = "messages";

    /**
     * The name of the default section that all TestResult objects are equipped with.
     */
    public static final String MSG_SECTION_NAME = "script_messages";

    /**
     * The name of the property that defines the test description file.
     */
    public static final String DESCRIPTION = "description";

    /**
     * The name of the property that defines the time at which the test
     * execution finished.
    */
    public static final String END = "end";

    /**
     * The name of the property that defines the environment name.
     */
    public static final String ENVIRONMENT = "environment";

    /**
     * The name of the property that defines the test execution status.
     */
    public static final String EXEC_STATUS = "execStatus";

    /**
     * The name of the property that defines the OS on which JT Harness
     * was running when the test was run.
     */
    public static final String JAVATEST_OS = "javatestOS";

    /**
     * The name of the property that defines the script that ran the test.
     */
    public static final String SCRIPT = "script";

    /**
     * The name of the property that defines the test output sections
     * that were recorded when the test ran.
     */
    public static final String SECTIONS = "sections";

    /**
     * The name of the property that defines the time at which the test
     * execution started.
    */
    public static final String START = "start";

    /**
     * The name of the property that defines the test for which this is
     * the result object.
    */
    public static final String TEST = "test";

    /**
     * The name of the property that defines which version of JT Harness
     * was used to run the test.
     */
    public static final String VERSION = "javatestVersion";

    /**
     * The name of the property that defines the work directory for the test.
     */
    public static final String WORK = "work";

    /**
     * The name of the property that defines the variety of harness in use.
     * Generally the full harness or the lite version.
     */
    public static final String VARIETY = "harnessVariety";

    /**
     * The name of the property that defines the type of class loader used when
     * running the harness (classpath mode or module mode generally).
     */
    public static final String LOADER = "harnessLoaderMode";

    /**
     * DateFormat, that is used to store date into TestResult
     */
    private static final DateFormat dateFormat =
            new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);

    static final String EXTN = ".jtr";

    private static final Status
        filesSame       = Status.passed("Output file and reference file matched"),
        filesDifferent  = Status.failed("Output file and reference file were different"),
        fileError       = Status.failed("Error occurred during comparison"),
        interrupted     = Status.failed("interrupted"),
        inProgress      = Status.notRun("Test running..."),
        incomplete      = Status.notRun("Section not closed, may be incomplete"),
        tdMismatch      = Status.notRun("Old test flushed, new test description located"),
        notRunStatus    = Status.notRun("");

    private static final String[] emptyStringArray = new String[0];
    private static final Section[] emptySectionArray = new Section[0];

    private static final String defaultClassDir = "classes";

    // info for reading/writing JTR files (version 1)
    private static final String JTR_V1_HEADER = "#Test Results";
    private static final String JTR_V1_SECTRESULT = "command result:";
    private static final String JTR_V1_TSTRESULT = "test result:";

    // info for reading/writing JTR files (version 2)
    private static final String JTR_V2_HEADER = "#Test Results (version 2)";
    private static final String JTR_V2_SECTION = "#section:";
    private static final String JTR_V2_CHECKSUM = "#checksum:";
    private static final String JTR_V2_TESTDESC = "#-----testdescription-----";
    private static final String JTR_V2_RESPROPS = "#-----testresult-----";
    private static final String JTR_V2_ENVIRONMENT = "#-----environment-----";
    private static final String JTR_V2_SECTRESULT = "result: ";
    private static final String JTR_V2_TSTRESULT = "test result: ";
    private static final String JTR_V2_SECTSTREAM = "----------";

    private static final String lineSeparator = System.getProperty("line.separator");

    private static final int DEFAULT_MAX_SHRINK_LIST_SIZE = 128;
    private static final int maxShrinkListSize =
        Integer.getInteger("javatest.numCachedResults", DEFAULT_MAX_SHRINK_LIST_SIZE).intValue();
    private static LinkedList shrinkList = new LinkedList();

    private static final int DEFAULT_MAX_OUTPUT_SIZE = 100000;
    private static final int maxOutputSize =
        Integer.getInteger("javatest.maxOutputSize", DEFAULT_MAX_OUTPUT_SIZE).intValue();

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(TestResult.class);

    private static boolean debug = Boolean.getBoolean("debug." + TestResult.class.getName());

}
