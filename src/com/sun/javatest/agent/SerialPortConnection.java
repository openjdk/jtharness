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
package com.sun.javatest.agent;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;
import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;

/**
 * A connection via a serial port.
 */
public class SerialPortConnection implements Connection
{
    /**
     * Create a connection via a serial port.
     * @param name      The name of the serial port to use.
     * @param app       The name of the application using the port.
     * @param timeout   The time, in milliseconds, to wait for the port
     *                  to be available.
     * @throws IOException if a problem occurs accessing the serial port.
     * @throws NoSuchPortException if a bad port name was specified.
     * @throws PortInUseException if the specified port was not available.
     * @throws InterruptedException if the thread was interrupted while waiting
     *                  for the port to become available.
     */
    public SerialPortConnection(String name, String app, int timeout) throws IOException, NoSuchPortException, PortInUseException, InterruptedException {
        this(open(CommPortIdentifier.getPortIdentifier(name), app, timeout));
    }

    /**
     * Create a connection via a serial port.
     * @param portId    An identifier for the serial port to use.
     * @param app       The name of the application using the port.
     * @param timeout   The time, in milliseconds, to wait for the port
     *                  to be available.
     * @throws IOException if a problem occurs accessing the serial port.
     * @throws PortInUseException if the specified port was not available.
     * @throws InterruptedException if the thread was interrupted while waiting
     *                  for the port to become available.
     */
    public SerialPortConnection(CommPortIdentifier portId, String app, int timeout) throws IOException, PortInUseException, InterruptedException {
        this(open(portId, app, timeout));
    }

    private SerialPortConnection(final SerialPort port) throws IOException, InterruptedException {
        //System.err.println("opening " + port.getName());
        this.port = port;

        portInputStream = port.getInputStream();
        portOutputStream = port.getOutputStream();

        try {
            port.setSerialPortParams(baudRate,
                                     SerialPort.DATABITS_8,
                                     SerialPort.STOPBITS_1,
                                     SerialPort.PARITY_NONE);
            port.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
        }
        catch (UnsupportedCommOperationException e) {
            throw new IOException(e.toString());
        }

        //System.err.println("opened " + port.getName() +
        //                 " " + port.getBaudRate() +
        //                 " " + port.getDataBits() + "/" + parityToString(port.getParity()) + "/" + port.getStopBits() +
        //                 " " + flowControlToString(port.getFlowControlMode()));

        // flush any input; we haven't marked ourselves as open for business set,
        // so the data cannot be for us
        int bytesToFlush = portInputStream.available();
        if (bytesToFlush > 0)
            portInputStream.skip(bytesToFlush);

        // use DTR to indicate we're open and listening;
        // we'll switch off DTR when the connection is closed
        //System.err.println("setting DTR to true");
        port.setDTR(true);

        // set up a listener to notify this thread when DSR/CD become true,
        // and to close this connection when they become false
        try {
            port.addEventListener(new SerialPortEventListener() {
                public void serialEvent(SerialPortEvent ev) {
                    int t = ev.getEventType();
                    switch (ev.getEventType()) {
                    case SerialPortEvent.CD:
                    case SerialPortEvent.DSR:
                        // WARNING: The following line may cause ThreadDeath if
                        // it determines the port should be closed.
                        updateReadyStatus();
                        break;
                    }
                }
            });
        }
        catch (TooManyListenersException e) {
            // should not happen, because ports are always opened inside this class
        }
        port.notifyOnCarrierDetect(true);
        port.notifyOnDSR(true);

        waitUntilReady();
    }

    public String getName() {
        return port.getName();
    }

    public InputStream getInputStream() {
        return portInputStream;
    }

    public OutputStream getOutputStream() {
        return new FilterOutputStream(portOutputStream) {
            // flushing stream after it has been closed causes
            // IllegalStateException
            public void flush() throws IOException {
                if (!closed)
                    super.flush();
            }
        };
    }

    public synchronized void close() throws IOException {
        if (!closed) {
            try {
                //System.err.println("setting DTR to false");
                port.setDTR(false);

                // send break for at least two character periods
                // note that char-rate = baud-rate/10 (for 1 start/8 data/1 stop bit)
                //System.err.println("send break");
                //port.sendBreak(2*Math.max(1, 10000/port.getBaudRate()));

                //System.err.println("closing streams");
                // --- leave client to connection to close these streams, since they
                // --- can only be closed once
                //portInputStream.close();
                //portOutputStream.close();

                //System.err.println("closing port");
                // WARNING: The following line may cause ThreadDeath if
                // this method is called called from an event routine.
                port.close();
            }
            finally {
                closed = true;
                //System.err.println("closed");
                notifyAll();
            }
        }
    }

    public synchronized boolean isClosed() {
        return closed;
    }

    public synchronized void waitUntilClosed(int timeout) throws InterruptedException {
        long now = System.currentTimeMillis();
        long end = now + timeout;
        while (now < end && !closed) {
            wait(end - now);
            now = System.currentTimeMillis();
        }
    }

    private static SerialPort open(CommPortIdentifier cpi, String app, int timeout) throws IOException, PortInUseException {
        if (cpi.getPortType() != CommPortIdentifier.PORT_SERIAL)
            throw new IllegalArgumentException("not a serial port: " + cpi.getName());
        return ((SerialPort)cpi.open(app, timeout));
    }

    private synchronized void updateReadyStatus() {
        if (port.isDSR() || port.isCD())
            notifyAll();
        else {
            //System.err.println("lost DSR and CD, closing connection");
            Thread t = new Thread() {
                public void run() {
                    try {
                        // WARNING: The following line may cause ThreadDeath if
                        // this method is called called from an event routine.
                        close();
                    }
                    catch (IOException e) {
                        //System.err.println("error closing port: " + e);
                    }
                }
            };
            t.start();
        }
    }

    private synchronized void waitUntilReady() throws InterruptedException {
        // wait for other end to set DTR, available at this end as DSR
        while (!(port.isDSR() || port.isCD())) {
            //System.err.println("waiting for DSR or CD");
            wait();
        }
        //System.err.println("DSR or CD available");
    }

    private String parityToString(int parity) {
        switch (parity) {
        case SerialPort.PARITY_NONE: return "none";
        case SerialPort.PARITY_ODD:  return "odd";
        case SerialPort.PARITY_EVEN: return "even";
        case SerialPort.PARITY_MARK: return "mark";
        case SerialPort.PARITY_SPACE: return "space";
        default: return "?" + parity + "?";
        }
    }

    private String flowControlToString(int flowControl) {
        switch (flowControl) {
        case SerialPort.FLOWCONTROL_NONE: return "none";
        case SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT: return "h/w";
        case SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT: return "s/w";
        default: return "?" + flowControl + "?";
        }
    }

    private String name;
    private SerialPort port;
    private InputStream portInputStream;
    private OutputStream portOutputStream;
    private boolean closed;

    private static final int baudRate =
        Integer.getInteger("javatest.serialPort.baudRate", 38400).intValue();
}
