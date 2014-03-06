/*
 * $Id$
 *
 * Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.services;

import java.io.Serializable;

/**
 * Class to represent messages, sent to ServiceExecutor, and achieved in response.
 * Each message consist of message type and content, which is any Serializable
 * object
 */
public class Message implements Serializable {
    /**
     * MessageType enum lists all types of messages, available for message
     * exchange. Remember, that java enums are serializabel. <p>
     * Types, used by harness, are:<p>
     * {@link MessageType#START}<p>
     * {@link MessageType#STOP}<p>
     * {@link MessageType#IS_ALIVE}<p>
     * Types, received in response:<p>
     * {@link MessageType#STARTED}<p>
     * {@link MessageType#STOPPED}<p>
     * {@link MessageType#ALIVE}<p>
     * {@link MessageType#NOT_ALIVE}<p>
     * {@link MessageType#ERROR}<p>
     */
    public static enum MessageType {
        START, STOP, IS_ALIVE,
        STARTED, STOPPED, ALIVE, NOT_ALIVE,
        ERROR
    }

    private MessageType type;
    private Serializable content;

    public Message(MessageType type, Serializable content) {
        this.type = type;
        this.content = content;
    }

    public MessageType getType() {
        return type;
    }

    public Serializable getContent() {
        return content;
    }

    static final long serialVersionUID = -4354555956848040022L;
}
