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
  * This class extracts all ASCII characters within standard shell script
  * comments.  A shell comment begins with a "#" and ends at the end of a line.
  * A single comment is regarded as a continuous set of lines that begin with
  * "#".  All leading "#: characters and any preceding blanks or tabs are
  * ignored.
  *
  * @see CommentStream
  */
public class ShScriptCommentStream extends CommentStream
{
    public String readComment() throws IOException {
        String comment, line;

        while (true) {
            if ((line = cs.readLine()) == null)
                return null;
            if ((line = getCommentLine(line)) != null)
                break;
        }
        comment = line;

        while (true) {
            if ((line = cs.readLine()) == null)
                return comment;
            if ((line = getCommentLine(line)) == null)
                return comment;
            comment += line;
        }
    }

    //----- internal routines---------------------------------------------------

    private String getCommentLine(String line) {
        boolean isLineComment = false;
        char lineArray[] = line.toCharArray();
        int pos;
        for (pos = 0; pos < lineArray.length; pos++) {
            char c = lineArray[pos];
            if (c == '#')
                isLineComment = true;
            else if ((c != ' ' ) && (c != '\t') && (c != '\f'))
                break;
        }
        if (!isLineComment)
            return null;
        return line.substring(pos) + LINESEP;
    }

    //----------member variables------------------------------------------------

    private static final String LINESEP = System.getProperty("line.separator");
}
