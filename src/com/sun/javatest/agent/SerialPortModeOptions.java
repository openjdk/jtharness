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
package com.sun.javatest.agent;

import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import java.awt.Choice;
import java.awt.GridBagConstraints;
import java.awt.Label;
import java.util.Enumeration;
import java.util.Vector;

import static com.sun.javatest.agent.Agent.MILLIS_PER_SECOND;


// The functionality of SerialPortModeOptions is split in two, to cope
// with the possibility that the gnu.io extension might not be present.
// SerialPortModeOptions is strictly GUI, and should always be usable.
// Access to the serial ports is handled (internally) via a private interface,
// Proxy, to a dynamically loaded implementation, ProxyImpl.

interface Proxy {
    String[] getPortNames();

    ConnectionFactory createConnectionFactory(String port) throws BadValue;
}

// A private interface for SerialPortModeOptions to access gnu.io
// functionality, which may or may not be available.

class SerialPortModeOptions extends ModeOptions {
    private Choice portChoice;
    private Proxy proxy;

    SerialPortModeOptions() {
        super("serial port");

        try {
            Class<? extends Proxy> c = Class.forName(Proxy.class.getName() + "Impl").asSubclass(Proxy.class);
            proxy = c.getDeclaredConstructor().newInstance();
        } catch (Throwable ignore) {
            // leave proxy unset if we can't load the class, and cope with it later
        }

        GridBagConstraints c = new GridBagConstraints();

        Label portLabel = new Label("port:");
        c.weightx = 0;
        add(portLabel, c);

        portChoice = new Choice();

        if (proxy != null) {
            String[] portNames = proxy.getPortNames();
            for (String portName : portNames) {
                portChoice.addItem(portName);
            }
        }

        if (portChoice.getItemCount() == 0) {
            portChoice.addItem("no serial ports found or accessible");
            portChoice.setEnabled(false);
        }

        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;
        add(portChoice, c);
    }

    @Override
    ConnectionFactory createConnectionFactory(int concurrency) throws BadValue {
        if (proxy == null) {
            throw new BadValue("no serial ports found or accessible");
        } else {
            return proxy.createConnectionFactory(portChoice.getSelectedItem());
        }

    }

    void setPort(String port) {
        portChoice.select(port);
    }
}


// A private implementation of the Proxy interface. Expect possible loading
// errors when accessing this interface, if the gnu.io API is not available.
// In the case of more lazy linkers, it is possible that the errors will not
// arise until the methods are invoked; these errors are handled internally
// and default answers or exceptions thrown.

class ProxyImpl implements Proxy {
    @Override
    public String[] getPortNames() {
        try {
            Vector<String> v = new Vector<>();
            for (Enumeration<?> e = CommPortIdentifier.getPortIdentifiers(); e.hasMoreElements(); ) {
                CommPortIdentifier p = (CommPortIdentifier) e.nextElement();
                if (p.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                    v.add(p.getName());
                }
            }
            return v.toArray(new String[v.size()]);
        } catch (Throwable t) {
            return new String[]{};
        }
    }


    @Override
    public ConnectionFactory createConnectionFactory(String port) throws BadValue {
        try {
            return new SerialPortConnectionFactory(port, Agent.PRODUCT_NAME, 10 * MILLIS_PER_SECOND);
        } catch (NoSuchPortException e) {
            throw new BadValue("invalid port: " + port);
        } catch (Throwable t) {
            throw new BadValue("problem accessing serial ports");
        }
    }
}
