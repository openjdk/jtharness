/*
 * $Id$
 *
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.io.PrintStream;
import java.io.PrintWriter;

import com.sun.javatest.util.I18NResourceBundle;

/**
 * Special error class to be used for errors which may pop out of JT Harness and
 * be seen by the user.
 */
public class JavaTestError extends Error
{
    /**
     * Constructs a JavaTestError object with a literal string as the message
     * text.  You should ensure that the string you supply here is subject to
     * I18N.
     *
     * @param s Literal string to use as the message text.
     */
    public JavaTestError(String s) {
        super(s);
    }

    /**
     * Constructs a JavaTestError object with a literal string as the message
     * text.  You should ensure that the string you supply here is subject to
     * I18N.
     *
     * @param s Literal string to use as the message text.
     * @param original The exception which originally caused the problem.
     */
    public JavaTestError(String s, Throwable original) {
        super(s);
        this.original = original;
    }

    /**
     * Constructs a JavaTestError object to be thrown when an unexpected
     * exception has been caught.
     *
     * @param original The exception which originally caused the problem.
     * @see #unexpectedException
     */
    public JavaTestError(Throwable original) {
        super(i18n.getString("fault.default", original));
        this.original = original;
    }

    /**
     * Constructs a JavaTestError object with an I18N string as the message.
     *
     * @param i18n The resource bundle to get the message text from.
     * @param key The key to access the resource bundle.
     */
    public JavaTestError(I18NResourceBundle i18n, String key) {
        super(i18n.getString(key));
    }

    /**
     * Constructs a JavaTestError object with an I18N string as the message.
     *
     * @param i18n The resource bundle to get the message text from.
     * @param key The key to access the resource bundle.
     * @param arg Item to be substituted into the internationalized string.
     */
    public JavaTestError(I18NResourceBundle i18n, String key, Object arg) {
        super(i18n.getString(key, arg));
    }

    /**
     * Constructs a JavaTestError object with an I18N string as the message.
     *
     * @param i18n The resource bundle to get the message text from.
     * @param key The key to access the resource bundle.
     * @param args Items to be substituted into the internationalized string.
     */
    public JavaTestError(I18NResourceBundle i18n, String key, Object[] args) {
        super(i18n.getString(key, args));
    }

    /**
     * Constructs a JavaTestError object with an I18N string as the message and
     * the error or exception which caused this error to be thrown.  The original
     * throwable will probably be something like an IOException object.
     *
     * @param i18n The resource bundle to get the message text from.
     * @param key The key to access the resource bundle.
     * @param original The problem which ultimately caused this error.
     */
    public JavaTestError(I18NResourceBundle i18n, String key, Throwable original) {
        super(i18n.getString(key, original));
        this.original = original;
    }

    /**
     * Constructs a JavaTestError object with an I18N string as the message and
     * the error or exception which caused this error to be thrown.  The original
     * throwable will probably be something like an IOException object.
     *
     * @param i18n The resource bundle to get the message text from.
     * @param key The key to access the resource bundle.
     * @param arg Item to be substituted into the internationalized string.
     * @param original The problem which ultimately caused this error.
     */
    public JavaTestError(I18NResourceBundle i18n, String key, Object arg, Throwable original) {
        super(i18n.getString(key, new Object[] {arg, original}));
        this.original = original;
    }

    /**
     * Constructs a JavaTestError object with an I18N string as the message and
     * the error or exception which caused this error to be thrown.  The original
     * throwable will probably be something like an IOException object.
     *
     * @param i18n The resource bundle to get the message text from.
     * @param key The key to access the resource bundle.
     * @param args Items to be substituted into the internationalized string.
     * @param original The problem which ultimately caused this error.
     */
    public JavaTestError(I18NResourceBundle i18n, String key, Object[] args, Throwable original) {
        super(i18n.getString(key, join(args, original)));
        this.original = original;
    }

    public void printStackTrace() {
        printStackTrace(System.err);
    }

    public void printStackTrace(PrintStream s) {
        s.println(GENERIC_START);
        super.printStackTrace(s);

        if (original != null) {
            s.println(ROOT_CAUSE);
            original.printStackTrace(s);
        }

        s.println(GENERIC_END);
    }

    public void printStackTrace(PrintWriter s) {
        s.println(GENERIC_START);
        super.printStackTrace(s);

        if (original != null) {
            s.println(ROOT_CAUSE);
            original.printStackTrace(s);
        }

        s.println(GENERIC_END);
    }

    /**
     * If available, find out what error or exception caused JT Harness to create this
     * object and to throw it.
     *
     * @return The original Throwable object that this object was created in
     *         response to.  May be null if there was none.
     */
    public Throwable getOriginalFault() {
        return original;
    }

    /**
     * Add an object to an array.
     */
    private static Object[] join(Object[] args, Object a) {
        Object[] newArgs = new Object[args.length + 1];
        System.arraycopy(args, 0, newArgs, 0, args.length);
        newArgs[args.length] = a;
        return newArgs;
    }

    /**
     * Print out a message when an unexpected exception has been caught.
     * @param t The exception that was caught.
     * @see #JavaTestError(Throwable)
     */
    public static void unexpectedException(Throwable t) {
        System.err.println(GENERIC_START);
        System.err.println(UNEXPECTED_EXCEPTION);
        t.printStackTrace(System.err);
        System.err.println(GENERIC_END);
    }

    /**
     * The original problem.  Useful if you need the stack trace.
     */
    protected Throwable original;
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(JavaTestError.class);

    private static final String GENERIC_START = i18n.getString("fault.genericMsgStart");
    private static final String GENERIC_END = i18n.getString("fault.genericMsgEnd");
    private static final String ROOT_CAUSE = i18n.getString("fault.origin");
    private static final String UNEXPECTED_EXCEPTION = i18n.getString("fault.unexpected");
}

