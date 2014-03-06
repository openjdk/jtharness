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
package com.sun.javatest.finder;

import java.io.IOException;

/**
  * This class extracts all ASCII characters within two of Java's
  * comments:  traditional comments (bound by "/*" and "*\/") and
  * documentation comments (bound by "/**" and "*\/").  The comment
  * may span multiple lines.  All leading "*" characters and any
  * preceding blanks or tabs are ignored.
  *
  * @see CommentStream
  */
public class JavaCommentStream extends CommentStream
{
    public String readComment() throws IOException {
        int c;
        StringBuffer comment = new StringBuffer(0);

    commentStart:
        // start of comment is "/*".
        while (true) {
            switch (c = cs.read()) {
            case -1:
                return null;
            case '/':
                switch (cs.read()) {
                case -1:
                    return null;
                case '*':
                    break commentStart;
                case '/':
                    skipLine();
                    break;
                default:
                    break;
                }
                break;
            case '\'':
            case '"':
                skipString(c);
                break;
            default:
                if ((!Character.isWhitespace((char) c)) && fastScan)
                    return null;
                break;
            }
        }

        startLine = true;

        // end of comment is "*/".
        // take care to handle repeated stars before end of comment
        boolean starPending = false;
    commentEnd:
        while (true) {
            switch (c = cs.read()) {
            case -1:
                return null;
            case '*':
                if (starPending)
                    putc(comment, c); // flush pending *, leave * pending
                else
                    starPending = true;
                break;
            case '/':
                if (starPending)
                    break commentEnd;  // finally got "*/"
                putc(comment, c);
                break;
            default:
                if (starPending) {
                    putc(comment, '*');
                    starPending = false;
                }
                putc(comment, c);
                break;
            }
        }
        return comment.toString();
    } // readComment()

    //-----internal routines----------------------------------------------------

    private void putc(StringBuffer s, int c) {
        switch (c) {
            //case '\b':
            //case '\f':
            //break;
        case '\n':
        case '\r':
            //XXX dump the newline info?
            s.append((char) c);
            startLine = true;
            break;
        case ' ':
        case '\t':
        case '*':
            if (!startLine)
                s.append((char) c);
            break;
        default:
            startLine = false;
            s.append((char) c);
            break;
        }
    } // putc()

    private void skipLine() throws IOException {
        while (true) {
            int c = cs.read();
            if (c == -1 || c == '\r' || c == '\n')
                return;
        }
    }

    private void skipString(int term) throws IOException {
        while (true) {
            int c = cs.read();
            if (c == -1 || c == term)
                return;
            else if (c == '\\') {
                // since we are only interested in finding the end of the
                // string, we don't need to parse the escape: it is sufficient
                // to just skip the next character, in case the escape is
                // \\ or \' or \"
                c = cs.read();
            }
        }
    }

    //----------member variables------------------------------------------------

    private boolean startLine;
}
