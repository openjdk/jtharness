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

import java.io.BufferedReader;
import java.io.IOException;

/**
  * This class extract all ASCII characters within a block comments.  Any
  * leading spaces or comment-like characters are removed.
  */
abstract public class CommentStream
{
    /**
     * Initialize a CommentStream.
     * @param br The reader from which to read.
     */
    public void init(BufferedReader br) {
        cs = br;
    }

    /**
     * Close this CommentStream.
     * @throws IOException if there is a problem closing the stream.
     */
    public void close() throws IOException {
        if (cs != null)
            cs.close();
    }

    /**
     * Set this comment stream into "fast scan" mode.
     * Depending on the context, this should be set if there is a
     * constraint that limits the set of comments that might be of
     * interest.
     * @param b Set to true to enable a fast scan for comments.
     */
    public void setFastScan(boolean b) {
        fastScan = b;
    }

    /**
     * Read the next comment from the input reader.
     * @return The next comment that is read from the stream.
     * @throws IOException if there is a problem while reading the
     *          next comment.
     */
    abstract public String readComment() throws IOException;

    //----------member variables------------------------------------------------

    /**
     * The reader from which to read comments.
     */
    protected BufferedReader cs = null;

    /**
     * A flag indicating whether comments should be read in "fast scan" mode
     * or not.
     */
    protected boolean fastScan = false;
}
