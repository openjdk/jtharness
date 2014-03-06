/*
 * $Id$
 *
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import com.sun.javatest.ProductInfo;
import com.sun.javatest.Status;
import java.io.PrintStream;

/**
 * A component to monitor the status of a Agent. This is normally displayed
 * in either an {@link AgentFrame} or an {@link AgentApplet}.
 */
class AgentPanel extends ScrollPane
{
    public interface MapReader {
        public abstract Map read(String name) throws IOException;
    };

    /**
     * Create a standard AgentPanel to control and monitor a Agent.
     *
     * @see Agent
     */
    public AgentPanel(ModeOptions[] modeOptions, MapReader mr) {
        mapReader = mr;
        initGUI(modeOptions);
    }

    /**
     * Set the mode of the agent.
     *
     * @param mode      "active" or "passive"
     * @see Agent
     */
    public synchronized void setMode(String mode) {
        if (currAgent != null)
            throw new IllegalStateException();

        paramPanel.setMode(mode);
    }

    /**
     * Set the maximum number of requests that will be serviced at any
     * one time.
     *
     * @param conc      the maximum number of requests
     * @see Agent
     */
    public void setConcurrency(int conc) {
        if (currAgent != null)
            throw new IllegalStateException();

        paramPanel.setConcurrency(conc);
    }

    /**
     * Set the map file used to translate the arguments of incoming requests.
     *
     * @param mapFile   the map file
     * @see Agent#setMap
     */
    public void setMapFile(String mapFile) {
        if (currAgent != null)
            throw new IllegalStateException();

        paramPanel.setMapFile(mapFile);
    }

    /**
     * Set the retry delay for active agents.
     *
     * @param delay     the interval (in seconds) to wait after
     *                  a connection has been refused
     * @see Agent
     */
    public void setRetryDelay(int delay) {
        if (currAgent != null)
            throw new IllegalStateException();

        paramPanel.setRetryDelay(delay);
    }

    /**
     * Enable or disable tracing output from the agent.
     * Must be called before #start().
     * @param state True to enable tracing output.
     */
    public void setTracing(boolean state, PrintStream out) {
        tracing = state;
        if (!state) {
            traceOut = null;
        }
        else {
            traceOut = out;
        }
    }

    /**
     * Set the number of requests remembered in the history.
     *
     * @param limit     the maximum number of requests to be remembered
     * @see Agent
     */
    public void setHistoryLimit(int limit) {
        if (currAgent != null)
            throw new IllegalStateException();

        historyList.setMaxTasks(limit);
    }

    /**
     * Add a user observer to the agent when it is created.
     * @param userObs an observer to monitor the agent
     */
    public void addObserver(Agent.Observer userObs) {
        if (userObs == null)
            throw new NullPointerException();
        if (this.userObs != null)
            throw new IllegalStateException("Only one observer can be registered.");
        this.userObs = userObs;
    }

    /**
     * Start a agent running, based on the parameters in the panel.
     */
    public synchronized void start() {
        try {
            currAgent = paramPanel.createAgent();
            currAgent.setTracing(tracing);
            currAgent.addObserver(agentObs);
            if (userObs != null)
                currAgent.addObserver(userObs);

            Thread t = new Thread(currAgent, "AgentPanel worker thread");
            t.start();

            paramPanel.setEnabled(false);
            buttonPanel.setStartEnabled(false);
            buttonPanel.setStopEnabled(true);
        }
        catch (BadValue e) {
            error(e.getMessages());
        }
        catch (SecurityException e) {
            String[] msgs = {"Security Exception", e.getMessage()};
            error(msgs);
        }
        catch (ConnectionFactory.Fault e) {
            String[] msgs = {"Cannot create connection", e.getMessage()};
            error(msgs);
        }
        catch (IOException e) {
            String[] msgs = {"Problem reading map file", e.getMessage()};
            error(msgs);
        }
    }

    /**
     * Stop a agent running. Initially, the thread running the agent is
     * interrupted. If that doesn't work, after a short timeout, the thread
     * is stopped.  If that doesn't work, after another short timeout,
     * the thread is abandoned.
     */
    public synchronized void stop() {
        buttonPanel.setStopEnabled(false);
        if (currAgent != null)
            currAgent.interrupt();
        // the panel will be set idle when the agent reports (via the AgentObserver)
        // that it is finished
    }



    //-------------------------------------------------------------------

    public Dimension getPreferredSize() {
        Insets i = getInsets();
        Dimension d = deck.getPreferredSize();
        return new Dimension(i.left + d.width + i.right, i.top + d.height + i.bottom);
    }

    //-------------------------------------------------------------------

    void showTask(TaskState task) {
        mainFolder.show(taskPanel);
        taskPanel.setTask(task);
    }

    //-------------------------------------------------------------------
    //
    // The following are private methods for the GUI

    private void initGUI(ModeOptions[] modeOptions) {
        agentObs = new AgentObserver();
        deck = new Deck();
        initMainPanel(modeOptions);
        deck.add(mainPanel);
        errorPanel = new ErrorPanel();
        deck.add(errorPanel);
        helpPanel = new HelpPanel();
        deck.add(helpPanel);
        add(deck);
    }

    Component initMainPanel(ModeOptions[] modeOptions) {
        mainPanel = new Panel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        c.weighty = 1;

        mainFolder = new Folder();

        paramPanel = new ParamPanel(modeOptions);
        mainFolder.add("parameters", paramPanel);

        statsPanel = new StatsPanel();
        mainFolder.add("statistics", statsPanel);

        historyList = new HistoryList();
        mainFolder.add("history", historyList);

        taskPanel = new TaskPanel();
        mainFolder.add("selected task", taskPanel);

        c.insets.top = c.insets.left = c.insets.right = 10;
        mainPanel.add(mainFolder, c);

        buttonPanel = new ButtonPanel();
        c.weighty= 0;
        c.insets.bottom = 20;
        mainPanel.add(buttonPanel, c);

        return mainPanel;
    }

    private void setIdle() {
        paramPanel.setEnabled(true);
        buttonPanel.setStartEnabled(true);
        buttonPanel.setStopEnabled(false);
        currAgent = null;
        notifyAll();
    }

    private void showHelp() {
        deck.show(helpPanel);
    }

    private void error(String[] msgs) {
        for (int i = 0; i < msgs.length; i++)
            System.out.println(msgs[i]);
        errorPanel.show(msgs);
        deck.show(errorPanel);
    }

    private void ackError() {
        deck.show(mainPanel);
    }

    private static int getInt(String label, TextField field) throws BadValue {
        try {
            return Integer.parseInt(field.getText(), 10);
        }
        catch (NumberFormatException e) {
            throw new BadValue("Bad value in `" + label + "' field");
        }
    }


    private MapReader mapReader;

    private Deck deck;
    private ErrorPanel errorPanel;
    private HelpPanel helpPanel;
    private Panel mainPanel;
    private Folder mainFolder;
    private ButtonPanel buttonPanel;
    private ParamPanel paramPanel;
    private StatsPanel statsPanel;
    private TaskPanel taskPanel;
    private HistoryList historyList;
    private AgentObserver agentObs;
    private Agent.Observer userObs;

    private boolean tracing;
    private PrintStream traceOut;

    private Agent currAgent;

    private static final String[] statusCodes =
        {"PASS", "FAIL", "CHCK", "ERR ", "!RUN"};



    //-------------------------------------------------------------------



    //-------------------------------------------------------------------

    private static class TaskState
    {
        TaskState(Connection c) {
            connection = c;
        }

        Connection connection;
        int mode;
        String tag;
        String className;
        String[] args;
        Status status;

        static final int TEST = 0, COMMAND = 1, MAIN=2;
    }


    //-------------------------------------------------------------------

    private class AgentObserver implements Agent.Observer {
        public void started(Agent sl) {
            synchronized (AgentPanel.this) {
                if (sl != currAgent)
                    return;

                historyList.removeAll();
                taskPanel.setTask(null);
                statsPanel.reset();
            }
        }

        public void errorOpeningConnection(Agent sl, Exception e) {
            synchronized (AgentPanel.this) {
                if (sl != currAgent)
                    return;

                String[] msgs = {"error opening connection", e.getMessage()};
                error(msgs);
            }
        }

        public void finished(Agent sl) {
            synchronized (AgentPanel.this) {
                if (sl != currAgent)
                    return;

                setIdle();
            }
        }

        public void openedConnection(Agent sl, Connection c) {
            synchronized (AgentPanel.this) {
                if (sl != currAgent)
                    return;

                TaskState ts = new TaskState(c);
                historyList.addTask(ts);
                if (taskPanel.getTask() == null)
                    taskPanel.setTask(ts);
                statsPanel.startedTask(ts);
            }
        }

        public void execTest(Agent sl, Connection c, String tag, String className, String[] args) {
            synchronized (AgentPanel.this) {
                if (sl != currAgent)
                    return;

                TaskState ts = historyList.getTask(c);
                ts.mode = TaskState.TEST;
                ts.tag = tag;
                ts.className = className;
                ts.args = args;
                historyList.update(ts, "EXEC", tag);
                if (ts == taskPanel.getTask())
                    taskPanel.update();
            }
        }

        public void execCommand(Agent sl, Connection c, String tag, String className, String[] args) {
            synchronized (AgentPanel.this) {
                if (sl != currAgent)
                    return;

                TaskState ts = historyList.getTask(c);
                ts.mode = TaskState.COMMAND;
                ts.tag = tag;
                ts.className = className;
                ts.args = args;
                historyList.update(ts, "EXEC", tag);
                if (ts == taskPanel.getTask())
                    taskPanel.update();
            }
        }

        public void execMain(Agent sl, Connection c, String tag, String className, String[] args) {
            synchronized (AgentPanel.this) {
                if (sl != currAgent)
                    return;

                TaskState ts = historyList.getTask(c);
                ts.mode = TaskState.MAIN;
                ts.tag = tag;
                ts.className = className;
                ts.args = args;
                historyList.update(ts, "EXEC", tag);
                if (ts == taskPanel.getTask())
                    taskPanel.update();
            }
        }

        public void result(Agent sl, Connection c, Status r) {
            synchronized (AgentPanel.this) {
                if (sl != currAgent)
                    return;

                TaskState ts = historyList.getTask(c);
                // don't update history list until data passed back to AgentClient OK
                // (i.e. in completed(...)
                ts.status = r;
                if (ts == taskPanel.getTask())
                    taskPanel.update();
            }
        }

        public void exception(Agent sl, Connection c, Throwable t) {
            synchronized (AgentPanel.this) {
                if (sl != currAgent)
                    return;

                TaskState ts = historyList.getTask(c);
                if (ts.tag == null)
                    historyList.removeTask(ts);
                else
                    historyList.update(ts, "*IO*", ts.tag);
                statsPanel.finishedTask(ts, false);
            }
        }

        public void completed(Agent sl, Connection c) {
            synchronized (AgentPanel.this) {
                if (sl != currAgent)
                    return;

                TaskState ts = historyList.getTask(c);
                historyList.update(ts, statusCodes[ts.status.getType()], ts.tag);
                statsPanel.finishedTask(ts, true);
            }
        }
    }

    //-------------------------------------------------------------------

    private class ButtonPanel extends Panel implements ActionListener {
        ButtonPanel() {
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.weightx = 1.0;

            startButton = new Button("Start");
            startButton.addActionListener(this);
            add(startButton, c);

            stopButton = new Button("Stop");
            stopButton.addActionListener(this);
            stopButton.setEnabled(false);
            add(stopButton, c);

            helpButton = new Button("Help");
            helpButton.addActionListener(this);
            add(helpButton, c);
        }

        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src == startButton) {
                start();
            }
            else if (src == stopButton) {
                stop();
            }
            else if (src == helpButton) {
                showHelp();
            }
        }

        void setStartEnabled(boolean b) {
            startButton.setEnabled(b);
        }

        void setStopEnabled(boolean b) {
            stopButton.setEnabled(b);
        }

        private Button startButton;
        private Button stopButton;
        private Button helpButton;
    }

    //-------------------------------------------------------------------

    private class ErrorPanel extends Panel implements ActionListener {
        /**
         * Display a multi-line message in the error panel.
         *
         * @param msgs  The lines of the message to display
         */
        public synchronized void show(String[] msgs) {
            removeAll();

            setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets = new Insets(30, 30, 0, 30); // top, left, bottom, right

            for (int i = 0; i < msgs.length; i++) {
                Label msg = new Label(msgs[i]);
                add(msg, c);
                c.insets.top = 0;
            }

            c.insets.top = 30;
            c.insets.bottom = 30;
            Button okBtn = new Button("OK");
            okBtn.addActionListener(this);
            add(okBtn, c);
            validate();
        }

        public void actionPerformed(ActionEvent e) {
            ackError();
        }
    }

    //-------------------------------------------------------------------

    private class HelpPanel extends Panel implements ActionListener {
        HelpPanel() {
            setLayout(new GridBagLayout());
            setBackground(Color.white);

            Panel logoPanel = new Panel(new GridBagLayout());
            GridBagConstraints lpc = new GridBagConstraints();
            lpc.gridwidth = GridBagConstraints.REMAINDER;
            lpc.weighty = 1;
            lpc.insets = new Insets(20, 20, 20, 20);

            Icon logoJava = createIcon("jticon.gif");
            if (logoJava != null) {
                lpc.anchor = GridBagConstraints.NORTH;
                logoPanel.add(logoJava, lpc);
            }

            /*
            Icon logo100pc = createIcon("100percent.gif");
            if (logo100pc != null) {
                lpc.anchor = GridBagConstraints.SOUTH;
                logoPanel.add(logo100pc, lpc);
            }
            */

            GridBagConstraints lrc = new GridBagConstraints();
            lrc.weighty = 1;
            lrc.fill = GridBagConstraints.BOTH;
            add(logoPanel, lrc);

            GridBagConstraints c = new GridBagConstraints();
            c.gridwidth = GridBagConstraints.REMAINDER;
            Panel infoPanel = new Panel(new GridBagLayout());
            Label nameLabel = new Label("JT Harness Agent");
            nameLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
            infoPanel.add(nameLabel, c);

            Label versionLabel = new Label(ProductInfo.getVersion());
            versionLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
            infoPanel.add(versionLabel, c);

            Label copyLabel1 = new Label("Copyright (c) 1996, 2012, Oracle and/or its affiliates. All rights reserved.");
            infoPanel.add(copyLabel1, c);

            Label copyLabel2 = new Label("Use is subject to license terms.");
            infoPanel.add(copyLabel2, c);

            String helpText =
                "  For full information on using the\n" +
                "  JT Harness Agent, please refer to the\n" +
                "     JT Harness User's Guide\n" +
                "  and to the documentation for the\n" +
                "  test suite you are using.\n";

            TextArea helpArea = new TextArea(helpText, 5, 35, TextArea.SCROLLBARS_NONE);
            helpArea.setEditable(false);
            c.weightx = 1;
            c.weighty = 1;
            infoPanel.add(helpArea, c);

            Button okBtn = new Button("OK");
            okBtn.addActionListener(this);
            c.weightx = 0;
            c.weighty = 0;
            c.insets.bottom = 10;
            infoPanel.add(okBtn, c);

            lrc.weightx = 1;
            add(infoPanel, lrc);
        }

        private Icon createIcon(String name)  {
            try {
                URL url = getClass().getResource(name);
                if (url == null)
                    return null;
                Icon icon = new Icon(url);
                return icon;
            }
            catch (RuntimeException e) {
                return null;
            }
            catch (Error e) {
                return null;
            }


        }

        public void actionPerformed(ActionEvent e) {
            ackError(); // hmm, not the most obvious name, but it will work
        }
    }

    //-------------------------------------------------------------------

    private class HistoryList extends List implements ItemListener {
        HistoryList() {
            super(5, false);
            super.addItemListener(this);
        }

        public synchronized void addTask(TaskState task) {
            if (tasks.size() >= maxTasks) {
                for (int i = 0; i < tasks.size(); i++) {
                    String s = (String) getItem(i);
                    // skip over active tasks that will be updated later
                    if (s.startsWith("CONN") || s.startsWith("EXEC"))
                        continue;

                    tasks.removeElementAt(i);
                    super.remove(i);
                }
            }

            tasks.addElement(task);
            super.add("CONN " + task.connection.getName());
        }

        public synchronized TaskState getTask(Connection c) {
            for (int i = 0; i < tasks.size(); i++) {
                TaskState ts = (TaskState)(tasks.elementAt(i));
                if (ts.connection == c)
                    return ts;
            }
            throw new Error("AgentPanel.HistoryList.getTask: can't find socket");
        }

        public synchronized TaskState getSelectedTask() {
            return (TaskState)(tasks.elementAt(super.getSelectedIndex()));
        }

        public synchronized void removeAll() {
            tasks.setSize(0);
            if (getItemCount() > 0)
                // calling List.removeAll on empty list generates motif
                // warning message
                super.removeAll();
        }

        public synchronized void removeTask(TaskState task) {
            int index = tasks.indexOf(task);
            tasks.removeElementAt(index);
            super.remove(index);
        }

        public synchronized void setMaxTasks(int n) {
            maxTasks = n;
        }

        public synchronized void update(TaskState task, String state, String detail) {
            super.replaceItem(getKey(state, detail), tasks.indexOf(task));
        }

        public void itemStateChanged(ItemEvent e) {
            Object src = e.getItemSelectable();
            if (src == this) {
                switch (e.getStateChange()) {
                case ItemEvent.SELECTED:
                    showTask(getSelectedTask());
                    break;
                case Event.LIST_DESELECT:
                    showTask(null);
                    break;
                }
            }
        }

        private String getKey(String state, String detail) {
            return (state + " " + detail);
        }

        private int maxTasks = 10;
        private Vector tasks = new Vector();
    }


    //-------------------------------------------------------------------

    private class ParamPanel extends Panel  implements ItemListener {
        ParamPanel(ModeOptions[] modeOptions) {
            setLayout(new GridBagLayout());

            GridBagConstraints lc = new GridBagConstraints();
            lc.anchor = GridBagConstraints.EAST;

            GridBagConstraints fc = new GridBagConstraints();
            fc.fill = GridBagConstraints.HORIZONTAL;
            fc.gridwidth = GridBagConstraints.REMAINDER;
            fc.weightx = 1;
            fc.weighty = 0;


            // First row: mode choice, and the mode options deck
            add(new Label("mode:"), lc);

            modeChoice = new Choice();
            modeDeck = new Deck();

            for (int i = 0; i < modeOptions.length; i++) {
                ModeOptions m = modeOptions[i];
                modeChoice.addItem(m.getModeName());
                modeDeck.add(m);
            }

            // choices added later by modeOption objects
            modeChoice.addItemListener(this);
            fc.weightx = 0;
            fc.gridwidth = 1;
            add(modeChoice, fc);

            fc.insets.left = 20;
            fc.fill = GridBagConstraints.HORIZONTAL;
            fc.gridwidth = GridBagConstraints.REMAINDER;
            fc.weightx = 1;
            add(modeDeck, fc);
            fc.insets.left = 0;

            // Second row: the optional map file field
            add(new Label("map:"), lc);

            mapFileField = new TextField();
            add(mapFileField, fc);

            // Third row: the concurrency field
            Label concurrencyLabel = new Label("concurrency:");
            add(concurrencyLabel, lc);

            concurrencyField = new TextField("1", 3);
            fc.gridwidth = 1;
            fc.weightx = 0;
            add(concurrencyField, fc);

            fc.gridwidth = GridBagConstraints.REMAINDER;
            fc.weightx = 1;
            add(new Label(), fc); // dummy filler

        }

        public void setEnabled(boolean b) {
            modeChoice.setEnabled(b);
            for (int i = 0; i < modeDeck.getComponentCount(); i++) {
                ModeOptions o = (ModeOptions)(modeDeck.getComponent(i));
                o.setEnabled(b);
            }
            mapFileField.setEnabled(b);
            concurrencyField.setEnabled(b);
        }

        public void setMode(String modeName) {
            modeChoice.select(modeName);
            for (int i = 0; i < modeDeck.getComponentCount(); i++) {
                ModeOptions m = (ModeOptions)(modeDeck.getComponent(i));
                if (modeName.equals(m.getModeName())) {
                    modeDeck.show(m);
                    return;
                }
            }
        }

        public void setConcurrency(int conc) {
            concurrencyField.setText(Integer.toString(conc));
        }

        public void setMapFile(String mapFile) {
            mapFileField.setText(mapFile);
        }

        public void setRetryDelay(int delay) {
            retryDelay = delay; // not available in GUI
        }

        public Agent createAgent() throws BadValue, ConnectionFactory.Fault, IOException {
            // should consider catching IllegalArgumentException in here
            // and IllegalState?
            Map map = mapReader.read(mapFileField.getText());
            int concurrency = getInt("concurrency", concurrencyField);
            if (!Agent.isValidConcurrency(concurrency)) {
                throw new BadValue("Bad value for `concurrency field': " + concurrency);
            }

            ModeOptions mo = (ModeOptions)(modeDeck.getCurrentCard());

            try {
                ConnectionFactory cf = mo.createConnectionFactory(concurrency);
                Agent agent = new Agent(cf, concurrency);

                if (retryDelay > 0)
                    agent.setRetryDelay(retryDelay);

                agent.setMap(map);

                return agent;
            }
            catch (IllegalArgumentException e) {
                throw new BadValue(e.getLocalizedMessage());
            }
        }

        private Deck createModeDeck(Choice modeChoice) {
            return deck;
        }

        public void itemStateChanged(ItemEvent e) {
            Object src = e.getItemSelectable();
            if (src == modeChoice) {
                String modeName = ((Choice)src).getSelectedItem();
                for (int i = 0; i < modeDeck.getComponentCount(); i++) {
                    ModeOptions m = (ModeOptions)(modeDeck.getComponent(i));
                    if (modeName.equals(m.getModeName())) {
                        modeDeck.show(m);
                        return;
                    }
                }
            }
        }

        private Choice modeChoice;
        private Deck modeDeck;
        private TextField mapFileField;
        private Label concurrencyLabel;
        private TextField concurrencyField;
        private int retryDelay; // not currently in GUI
    }


    //-------------------------------------------------------------------

    private class StatsPanel extends Panel {
        StatsPanel() {
            setLayout(new GridBagLayout());

            GridBagConstraints lc = new GridBagConstraints();
            lc.anchor = GridBagConstraints.EAST;
            GridBagConstraints fc = new GridBagConstraints();
            fc.weightx = 1;
            fc.anchor = GridBagConstraints.WEST;
            fc.gridwidth = GridBagConstraints.REMAINDER;

            activeField = addField("currently active:", lc, fc);
            statusFields[Status.PASSED] = addField("passed:", lc, fc);
            statusFields[Status.FAILED] = addField("failed:", lc, fc);
            statusFields[Status.ERROR] = addField("error:", lc, fc);
            statusFields[Status.NOT_RUN] = addField("not run:", lc, fc);
            exceptionsField = addField("exceptions:", lc, fc);
        }

        void reset() {
            tasks.setSize(0);
            for (int i = 0; i < statusCounts.length; i++)
                statusCounts[i] = 0;
            exceptionsCount = 0;
            activeField.setText("0");
            for (int i = 0; i < statusCounts.length; i++)
                statusFields[i].setText("0");
            exceptionsField.setText("0");
        }

        void startedTask(TaskState task) {
            tasks.addElement(task);
            activeField.setText(String.valueOf(tasks.size()));
        }

        void finishedTask(TaskState task, boolean completedNormally) {
            int index = tasks.indexOf(task);
            if (index == -1)
                return;
            tasks.removeElementAt(index);
            activeField.setText(String.valueOf(tasks.size()));
            if (!completedNormally)
                exceptionsField.setText(String.valueOf(++exceptionsCount));
            if (task.status != null) {
                int t = task.status.getType();
                statusFields[t].setText(String.valueOf(++statusCounts[t]));
            }
        }

        private TextField addField(String name, GridBagConstraints lc, GridBagConstraints fc) {
            add(new Label(name), lc);
            TextField f = new TextField("0", 10);
            f.setEditable(false);
            add(f, fc);
            return f;
        }


        private Vector tasks = new Vector();

        private int[] statusCounts = new int[Status.NUM_STATES];
        private int exceptionsCount;

        private TextField activeField;
        private TextField[] statusFields = new TextField[Status.NUM_STATES];
        private TextField exceptionsField;
    }

    //-------------------------------------------------------------------

    private class TaskPanel extends Panel {
        TaskPanel() {
            setLayout(new GridBagLayout());

            GridBagConstraints lc = new GridBagConstraints();
            lc.anchor = GridBagConstraints.EAST;
            GridBagConstraints fc = new GridBagConstraints();
            fc.weightx = 1;
            fc.fill = GridBagConstraints.HORIZONTAL;
            fc.gridwidth = GridBagConstraints.REMAINDER;

            Label clientLabel = new Label("client:");
            add(clientLabel, lc);

            clientField = new Label("");
            add(clientField, fc);

            Label tagLabel = new Label("request:");
            add(tagLabel, lc);

            tagField = new Label("");
            add(tagField, fc);

            Label classLabel = new Label("class:");
            add(classLabel, lc);

            classField = new Label("");
            add(classField, fc);

            Label argsLabel = new Label("args:");
            add(argsLabel, lc);

            argsField = new TextField("");
            // make it a text field to support scrolling of very long fields
            argsField.setEditable(false);
            add(argsField, fc);

            Label resultLabel = new Label("result:");
            add(resultLabel, lc);

            resultField = new Label("");
            add(resultField, fc);
        }

        TaskState getTask() {
            return task;
        }

        void setTask(TaskState ts) {
            task = ts;
            update();
        }

        void update() {
            if (task == null) {
                clientField.setText("");
                tagField.setText("");
                classField.setText("");
                argsField.setText("");
                resultField.setText("");
            } else {
                clientField.setText(task.connection.getName());
                tagField.setText(task.tag);
                classField.setText(task.className);
                String a = "";
                if (task.args != null) {
                    for (int i = 0; i < task.args.length; i++)
                        a += task.args[i] + " ";
                }
                argsField.setText(a);
                if (task.status == null)
                    resultField.setText("");
                else
                    resultField.setText(task.status.toString());
            }
        }

        private TaskState task;

        private Label clientField;
        private Label tagField;
        private Label classField;
        private TextField argsField;
        private Label resultField;
    }
}
