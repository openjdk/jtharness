/*
 * $Id$
 *
 * Copyright (c) 1996, 2014, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.GridBagConstraints;
import java.awt.Label;
import java.awt.TextField;

class ActiveModeOptions extends ModeOptions {
    ActiveModeOptions() {
        super("active");

        GridBagConstraints c = new GridBagConstraints();

        Label hostLabel = new Label("host:");
        add(hostLabel, c);

        hostField = new TextField(20);
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(hostField, c);

        portLabel = new Label("port:");
        c.weightx = 0;
        add(portLabel, c);

        String defActPort = Integer.toString(Agent.defaultActivePort);
        portField = new TextField(defActPort, 5);
        add(portField, c);
    }

    ConnectionFactory createConnectionFactory(int concurrency) throws BadValue {
        String host = hostField.getText();
        if (host == null || host.length() == 0)
            throw new BadValue("no host name set");

        int port = getInt("port", portField);
        if (port < 0)
            throw new BadValue("port may not be negative");

        return new ActiveConnectionFactory(host, port);
    }

    void setHost(String host) {
        hostField.setText(host);
    }

    void setPort(int port) {
        portField.setText(Integer.toString(port));
    }

    private TextField hostField;
    private Label portLabel;
    private TextField portField;
}
