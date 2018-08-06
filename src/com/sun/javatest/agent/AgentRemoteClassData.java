/*
 * $Id$
 *
 * Copyright (c) 1996, 2016, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.agent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Class data to send from JT to Agent.
 *
 * @since 5.0
 */
class AgentRemoteClassData {

    private String className;
    private String codeSource;
    private byte[] byteData;

    static final AgentRemoteClassData NO_CLASS_DATA = new AgentRemoteClassData();

    private AgentRemoteClassData() {
    }

    AgentRemoteClassData(String className, String codeSource, byte[] byteData) {
        this.className = className;
        this.codeSource = codeSource;
        this.byteData = byteData;
    }

    AgentRemoteClassData(DataInputStream in) throws IOException, ClassNotFoundException {

        className = in.readUTF();

        if (className == null || className.isEmpty()) {
            throw new ClassNotFoundException("no class data in the InputStream");
        }

        codeSource = in.readUTF();

        int size = in.readInt();
        if (size == 0)
            throw new ClassNotFoundException(className);

        byteData = new byte[size];
        int offset = 0;
        while (offset < byteData.length) {
            int n = in.read(byteData, offset, byteData.length - offset);
            if (n == -1)
                throw new ClassNotFoundException(className + ": EOF while reading class data");
            else
                offset += n;
        }

    }

    public void write(DataOutputStream out) throws IOException {
        if (className == null || className.isEmpty() ||
                byteData == null || byteData.length == 0) {
            out.writeUTF("");
        } else {
            out.writeUTF(className);
            out.writeUTF(codeSource);
            out.writeInt(byteData.length);
            out.write(byteData, 0, byteData.length);
        }
    }

    public String getCodeSource() {
        return codeSource;
    }

    public byte[] getByteData() {
        return byteData;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" {")
                .append(" className: ").append(className)
                .append(" codeSource: ").append(codeSource)
                .append(" data.length: ").append(byteData == null ? 0 : byteData.length)
                .append(" }");
        return sb.toString();
    }

}