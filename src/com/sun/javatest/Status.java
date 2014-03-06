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

/**
 * A class to embody the result of a test: a status-code and a related message.
 */

public class Status
{
    /**
     * Create a Status to indicate the successful outcome of a test.
     * @param reason A short string describing why the test passed.
     * @return a Status to indicate the successful outcome of a test.
     */
    public static Status passed(String reason) {
        return new Status(PASSED, reason);
    }

    /**
     * Create a Status to indicate the unsuccessful outcome of a test:
     * i.e. the test completed, but the test determined that what was being tested
     * did not pass the test.
     * @param reason A short string describing why the test failed.
     * @return a Status to indicate the unsuccessful outcome of a test.
     */
    public static Status failed(String reason) {
        return new Status(FAILED, reason);
    }

    /**
     * Create a Status to indicate that an error occurred while trying to run a test:
     * i.e. the test did not complete for some reason, and so it could not determine
     * whether what was being tested passed or failed.
     * @param reason A short string describing the error that occurred.
     * @return a Status to indicate the error outcome of a test.
     */
    public static Status error(String reason) {
        return new Status(ERROR, reason);
    }

    /**
     * Create a Status to indicate that the test was not run because under
     * the conditions given it was not applicable.  This method is retained
     * for backwards compatibility only; the resultant object is of FAILED
     * type.
     * @param reason A short string describing why the test was not applicable.
     * @return a Status to indicate that a test failed because it was not applicable
     * @deprecated
     */
    public static Status notApplicable(String reason) {
        return new Status(FAILED, "Not Applicable: " + reason);
    }

    /**
     * Create a Status to indicate that the test has not yet been run.
     * @param reason A short string indicating why the test has not yet been  run.
     */
    static Status notRun(String reason) {
        return new Status(NOT_RUN, reason);
    }

    /**
     * Check if the type code of the status is PASSED.
     * @return true if the type code is PASSED.
     * @see #passed
     * @see #getType
     * @see #PASSED
     */
    public boolean isPassed() {
        return (type == PASSED);
    }

    /**
     * Check if the type code of the status is FAILED.
     * @return true if the type code is FAILED.
     * @see #failed
     * @see #getType
     * @see #FAILED
     */
    public boolean isFailed() {
        return (type == FAILED);
    }

    /**
     * Check if the type code of the status is ERROR.
     * @return true if the type code is ERROR.
     * @see #error
     * @see #getType
     * @see #ERROR
     */
    public boolean isError() {
        return (type == ERROR);
    }

    /**
     * Check if the type code of the status is NOT_RUN.
     * @return true if the type code is ERROR.
     * @see #getType
     * @see #NOT_RUN
     */
    public boolean isNotRun() {
        return (type == NOT_RUN);
    }

    /**
     * A return code indicating that the test was executed and was successful.
     * @see #passed
     * @see #getType
     */
    public static final int PASSED = 0;

    /**
     * A return code indicating that the test was executed but the test
     * reported that it failed.
     * @see #failed
     * @see #getType
     */
    public static final int FAILED = 1;

    /**
     * A return code indicating that the test was not run because some error
     * occurred before the test could even be attempted. This is generally
     * a more serious error than FAILED.
     * @see #getType
     */
    public static final int ERROR = 2;

    /**
     * A return code indicating that the test has not yet been run in this context.
     * (More specifically, no status file has been recorded for this test in the
     * current work directory.)  This is for the internal use of the harness only.
     * @see #getType
     */
    public static final int NOT_RUN = 3;

    /**
     * Number of states which are predefined as "constants".
     */
    public static final int NUM_STATES = 4;

    /**
     * Get the type code indicating the type of this Status object.
     * @return the type code indicating the type of this Status object.
     * @see #PASSED
     * @see #FAILED
     * @see #ERROR
     */
    public int getType() {
        return type;
    }

    /**
     * Get the message given when the status was created.
     * @return the string given when this Status object was created.
     */
    public String getReason() {
        return reason;
    }

    /**
     * Return a new Status object with a possibly augmented reason field.
     * @param aux if not null and not empty, it will be combined with the original reason.
     * @return if <em>aux</em> is null or empty, the result will be the same as this object;
     * otherwise, it will be a new object combining the original status reason and the
     * additional information in <em>aux</em>.
     */
    public Status augment(String aux) {
        if (aux == null || aux.length() == 0)
            return this;
        else
            return new Status(type, (reason + " [" + aux + "]"));
    }

    /**
     * Return a new Status object with a possibly augmented reason field.
     * @param aux a Status to combine with this object
     * @return if <em>aux</em> is null, the result will be the same as this object;
     * otherwise, it will be a new object combining the original status reason and the
     * additional information in <em>aux</em>.
     */
    public Status augment(Status aux) {
        return (aux == null ? this : augment(aux.reason));
    }

    /**
     * Parse a string-form of a Status.
     * @param s a string containing the string form of a Status
     * as generated by {@link #toString}
     * @return the corresponding Status, or null if it could not be parsed successfully
     * @see #exit
     */
    public static Status parse(String s) {
        try {
            return new Status(decode(s));
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Status(String s) {
        for (int t = 0; t < texts.length; t++) {
            if (s.startsWith(texts[t])) {
                int l = texts[t].length();
                String r;
                if (l < s.length()) {
                    if (s.charAt(l) == ' ') {
                        r = s.substring(l + 1);
                    } else {
                        r = s.substring(l);
                    }
                } else {
                    r = "";
                }
                type = t;
                reason = normalize(r);
                return;
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Convert a Status to a string.
     * @see #parse
     */
    public String toString() {
        if (reason == null || reason.length() == 0)
            return texts[type];
        else
            return texts[type] + " " + reason;
    }

    /**
     * Translate the type number to a descriptive string.
     * For example, type 0 corresponds to the "Passed." string.
     *
     * @param typeNum A number between zero and NUM_STATES
     * @return null if the given integer was out of range, otherwise an appropriate string.
     */
    public static String typeToString(int typeNum) {
        if (typeNum < NUM_STATES)
            return texts[typeNum];
        else
            return null;
    }

    /**
     * Convenience exit() function for the main() of tests to exit in such a
     * way that the status passes up across process boundaries without losing
     * information (ie exit codes don't give the associated text of the status
     * and return codes when exceptions are thrown could cause unintended
     * results). <p>
     *
     * An identifying marker is written to the error stream, which the script
     * running the test watches for as the last output before returning,
     * followed by the type and reason
     *
     * The method does not return.  It calls System.exit with a value
     * dependent on the type.
     */
    public void exit() {
        if (System.err != null) {
            System.err.print(EXIT_PREFIX);
            System.err.print(texts[type]);
            System.err.println(encode(reason));
            System.err.flush();
        }
        System.exit(exitCodes[type]);
    }

    /**
     * Create a Status object.  See {@link #passed}, {@link #failed}, {@link #error}
     * etc. for more convenient factory methods to create Status objects.
     * @param type The type code for the Status object.
     * @param reason A short string to store in the status. Unprintable
     * characters (i.e. outside the range 040C to 177C) in the string are
     * replaced by a space.  All whitespace runs are reduced to a single
     * whitespace.
     * @throws IllegalArgumentException if the specified type is invalid.
     */
    public Status(int type, String reason) throws IllegalArgumentException {
        if (type < 0 || type >= NUM_STATES) {
            throw new IllegalArgumentException(String.valueOf(type));
        }
        /*
         // if we find any bad characters in the reason string (e.g. newline)
         // we rewrite the string replacing all such characters with a space.
         for (int i = 0; i < reason.length(); i++) {
         if (!isPrintable(reason.charAt(i))) {
         StringBuffer r = new StringBuffer(reason.length());
         for (int j = 0; j < reason.length(); j++) {
         char c = reason.charAt(j);
         r.append(isPrintable(c) ? c : ' ');
         }
         reason = r.toString();
         break;
         }
         }
         */
        this.type = type;
        this.reason = normalize(reason);
    }

    //-----internal routines----------------------------------------------------
    // equivalent to msg.trim().replaceAll("\\s+", " ");
    private static String normalize(String msg) {
        boolean ok = true;
        boolean prevIsWhite = false;
        for (int i = 0; ok && i < msg.length(); i++) {
            char ch = msg.charAt(i);
            if (Character.isWhitespace(ch)) {
                if (prevIsWhite || ch != ' ' || i == 0) {
                    ok = false;
                    break;
                }
                prevIsWhite = true;
            } else {
                prevIsWhite = false;
            }
        }
        if (prevIsWhite) {
            ok = false;
        }
        if (ok) {
            return msg;
        }

        StringBuffer sb = new StringBuffer();
        boolean needWhite = false;
        for (int i = 0; i < msg.length(); i++) {
            char ch = msg.charAt(i);
            if (Character.isWhitespace(ch)) {
                if (sb.length() > 0) {
                    needWhite = true;
                }
            } else {
                if (needWhite) {
                    sb.append(' ');
                }
                sb.append(ch);
                needWhite = false;
            }
        }
        return sb.toString();
    }

    private static final boolean isPrintable(char c) {
        return (32 <= c && c < 127);
    }

    //----------Data members----------------------------------------------------

    private final int type;
    private final String reason;

    /**
     * A string used to prefix the status when it is written to System.err
     * by {@link #exit}.
     */
    public static final String EXIT_PREFIX = "STATUS:";

    private static String[] texts = {
        // correspond to PASSED, FAILED, ERROR, NOT_RUN
        "Passed.",
        "Failed.",
        "Error.",
        "Not run."
    };

    /**
     * Exit codes used by Status.exit corresponding to
     * PASSED, FAILED, ERROR, NOT_RUN.
     * The only values that should normally be returned from a test
     * are the first three; the other value is provided for completeness.
     * <font size=-1> Note: The assignment is historical and cannot easily be changed. </font>
     */
    public static final int[] exitCodes = { 95, 97, 98, 99 };

    /**
     * Encodes strings containing non-ascii characters, where all characters
     * are replaced with with their Unicode code. Encoded string will have
     * the certain prefix and suffix to be distinguished from non-encode one.
     * Strings of ASCII chars only are encoded into themselves.<br>
     * Example:
     * <pre>
     * System.out.println(Status.encode("X \u01AB")); //<Encoded>58 20 1AB </Encoded>
     * System.out.println(Status.encode("Abc1")); // Abc1
     * </pre>
     * @param str - string to encode
     * @return Encoded string or the same string if none non-ascii chars were found
     *
     * @see #decode(java.lang.String)
     */
    public static String encode(String str) {
        if (str == null) {
            return null;
        }
        boolean isAscii = true;
        for (int i = 0; i < str.length(); i++) {
            if (!isPrintable(str.charAt(i))) {
                isAscii = false;
                break;
            }
        }
        if (isAscii) {
            return str; // no need to decode;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(ENC_PREFFIX);
        for (int i = 0; i < str.length(); i++) {
            sb.append(encodeChar(str.charAt(i)));
            sb.append(ENC_SEPARATOR);
        }
        sb.append(ENC_SUFFFIX);
        return sb.toString();

    }

    /**
     * Decodes string encoded by encode(String) method.
     * @param str - string to decode
     * @return Decoded string or the same string if encoded prefix/suffix
     * were found
     *
     * @see #encode(java.lang.String)
     */
    public static String decode(String str) {
        if (str == null) {
            return null;
        }
        int ind = str.indexOf(ENC_PREFFIX);
        if (ind < 0 || !str.endsWith(ENC_SUFFFIX)) {
            return str; // not encoded
        }

        // identify encoded part
        String encoded = str.substring(ind + ENC_PREFFIX.length(),
                str.length() - ENC_SUFFFIX.length());

        StringBuffer sb = new StringBuffer();
        sb.append(str.substring(0, ind));

        // emulate StringTokenizer(encoded, ENC_SEPARATOR) to find tokens
        int begin = 0;
        int end = encoded.indexOf(ENC_SEPARATOR);
        while (end >= 0) {
            sb.append(decodeChar(encoded.substring(begin, end)));
            begin = end + ENC_SEPARATOR.length();
            end = encoded.indexOf(ENC_SEPARATOR, begin);
        }
        sb.append(encoded.substring(begin));

        return sb.toString();
    }

    private static final String encodeChar(char c) {
        return Integer.toString((int)c, 16);
    }

    private static final char decodeChar(String s) {
        return (char)Integer.parseInt(s, 16);
    }

    /**
     * Prefix signaling that string is encoded
     */
    private static final String ENC_PREFFIX = "<EncodeD>";

    /**
     * Suffix signaling that string is encoded
     */
    private static final String ENC_SUFFFIX = "</EncodeD>";

    /**
     * Separator of encoded chars
     */
    private static final String ENC_SEPARATOR = " ";

}
