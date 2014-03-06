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
  * This class extracts all ASCII characters within HTML comments
  * bound by "<!--" and "-->".  The comment may span multiple lines.
  *
  * @see CommentStream
  */
public class HTMLCommentStream extends CommentStream
{
    public String readComment() throws IOException {
        String comment, line;
        int startComment;
        int endComment;
        boolean endFound = false;

        // beginning of comment
        while (true) {
            startComment = 0;
            endComment   = 0;
            if ((line = cs.readLine()) == null)
                return null;
            startComment = line.indexOf("<!--");
            if (startComment >= 0) {
                // handle a comment that starts and ends on the same line
                // +4 needed to offset the starting pos characters
                String tail = line.substring(startComment+4);
                if ((endComment = tail.indexOf("-->")) >= 0) {
                    comment = tail.substring(0, endComment);
                    endFound = true;
                    break;
                } else {
                    comment = tail + LINESEP;
                    break;
                }
            }
        }

        // end of comment
        while (!endFound) {
            if ((line = cs.readLine()) == null)
                throw new IOException("Comment not properly terminated");
            if ((endComment = line.indexOf("-->")) >= 0) {
                comment += line.substring(0, endComment) + LINESEP;
                endFound = true;
            } else
                comment += line + LINESEP;
        }

        return comment.replace('\n', ' ').replace('\r', ' ').trim();
    }

    //----------member variables------------------------------------------------

    private static final String LINESEP = System.getProperty("line.separator");
}
