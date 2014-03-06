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
package com.sun.javatest.exec;

import com.sun.interview.Interview;
import com.sun.interview.Question;
import com.sun.javatest.services.Service.NotConnectedException;
import com.sun.javatest.services.Service.ServiceError;
import com.sun.javatest.services.ServiceManager;
import com.sun.javatest.tool.ToolDialog;
import com.sun.javatest.tool.UIFactory;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class ServiceViewer extends ToolDialog implements Interview.Observer {

    private ServiceManager mgr;
    private String[] ids;
    private JTable table;
    private JButton btnStart;
    private JButton btnStop;

    public ServiceViewer(final ServiceManager mgr, UIFactory uif, Component parent) {
        super(parent, uif, "serviceviewer");

        this.mgr = mgr;

        Set<String> keys = mgr.getAllServices().keySet();
        ids = new String[keys.size()];
        keys.toArray(ids);
        Arrays.sort(ids);

        initGUI();
        mgr.addObserver( new ServiceManager.Observer() {

            public void handleAlive(String sID, boolean alive) {
                int i = Arrays.binarySearch(ids, sID);
                if (i != -1) {
                    if (alive) {
                        table.getModel().setValueAt(Status.ALIVE, i, 2);
                    } else {
                        table.getModel().setValueAt(Status.NOT_ALIVE, i, 2);
                    }
                }
                table.repaint();
            }

            public void handleNotConnected(String sID, NotConnectedException ex) {
                int i = Arrays.binarySearch(ids, sID);
                if (i != -1) {
                    table.getModel().setValueAt(Status.NOT_CONNECTED, i, 2);
                }
                table.repaint();
            }

            public void handleError(String sID, ServiceError ex) {
                int i = Arrays.binarySearch(ids, sID);
                if (i != -1) {
                    table.getModel().setValueAt(Status.ERROR, i, 2);
                }
                table.repaint();
            }
        });

        table.getSelectionModel().addListSelectionListener
                (new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                int[] ix = table.getSelectedRows();
//                boolean allAlive = true;
                for (int i : ix) {
                    Status stat;
                    try {
                        boolean isAlive = mgr.getAllServices().get(ids[i]).isAlive();
//                        allAlive &= isAlive;
                        stat = isAlive ? Status.ALIVE : Status.NOT_ALIVE;
                    } catch (NotConnectedException ex) {
                        stat = Status.NOT_CONNECTED;
                    } catch (ServiceError ex) {
                        stat = Status.ERROR;
                    }
                    table.getModel().setValueAt(stat , i, 2);
                }
                table.repaint();
            }

        });
    }

    ServiceManager getServiceManager() {
        return mgr;
    }

    @Override
    protected void initGUI() {

        GridBagConstraints gbc;

        TableModel model = new ServicesTableModel();
        table = uif.createTable("serviceviewer", model);
        table.setModel(model);

        JScrollPane sp = uif.createScrollPane(table);
        Border b = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        sp.setBorder(b);
//        table.setFillsViewportHeight(true);
        btnStart = uif.createButton("serviceviewer.button.start");
        btnStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] selected = table.getSelectedRows();
                for (int i : selected) {
                    mgr.startService(ids[i]);
                }
            }
        });

        btnStop = uif.createButton("serviceviewer.button.stop");
        btnStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] selected = table.getSelectedRows();
                for (int i : selected) {
                    mgr.stopService(ids[i]);
                }
            }
        });

        JButton btnClose = uif.createButton("serviceviewer.button.close");
        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        setButtons(new JButton[] {btnStart, btnStop, btnClose}, btnClose);

        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(400);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        sp.setPreferredSize(new Dimension(600, 400));
        setBody(sp);

        setI18NTitle("serviceviewer.title");
    }

    private class ServicesTableModel extends DefaultTableModel {
        private String[] names = {"ID", "Description", "Status"};
        private Status[] stats = new Status[getRowCount()];


        public ServicesTableModel() {
            for (int i = 0; i < stats.length; i++) {
                Status stat;
                try {
                    boolean isAlive = mgr.getAllServices().get(ids[i]).isAlive();
                    stat = isAlive ? Status.ALIVE : Status.NOT_ALIVE;
                } catch (NotConnectedException ex) {
                    stat = Status.NOT_CONNECTED;
                } catch (ServiceError ex) {
                    stat = Status.ERROR;
                }
                setValueAt(stat , i, 2);
            }
        }

        public int getRowCount() {
            if (ids != null) {
                return ids.length;
            }
            return 0;
        }

        public int getColumnCount() {
            return 3;
        }

        public String getColumnName(int columnIndex) {
            return names[columnIndex];
        }

        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return ids[rowIndex];
                case 1:
                    return mgr.getAllServices().get(ids[rowIndex]).getDescription();
                case 2:
                    return stats[rowIndex];
                default:
                    return null;
            }

        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            stats[rowIndex] = (Status)aValue;
        }
    }

    enum Status {
        ALIVE("Running..."), NOT_ALIVE("Stopped"),
        NOT_CONNECTED("Not Connected"), ERROR("Error");

        private String str;
        Status(String str) {
            this.str = str;
        }

        public String toString() {
            return str;
        }
    }

    public void currentQuestionChanged(Question q) {
    }

    public void pathUpdated() {
        ExecTool et = (ExecTool) parent;
        mgr.setParameters(et.getInterviewParameters());
    }


}
