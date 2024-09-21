---
hIndent: 1
title: Starting the Agent GUI
---

[]{#startGUI}

# Starting the Agent GUI

Before you can start the agent GUI, the required classes must be installed on your test system.
Refer to [Installing Agent Classes on Test Systems](loadingClasses.html) for the location and list
of classes required to use the agent GUI.

1.  Start the agent GUI by entering the following at the command prompt:

    `java -cp` *class-path* `com.sun.javatest.agent.AgentFrame` *\[options\]*

    > The `-cp` option sets the class paths required to run the agent. Use the **;** or **:**
    > separator appropriate for your test system to separate multiple paths in the argument to the
    > `-cp` option. Refer to [Classes](#startGUI.classPaths) for detailed descriptions of the
    > classes that your agent requires.
    >
    > *\[options\]* are not required to start the agent GUI, but can be included to display help,
    > set properties not available from the agent GUI, or start the GUI using a specific set of
    > parameters. Refer to [GUI Options](#startGUI.appOptions) for a list and description of the
    > options that can be included in the command line.

2.  After the GUI is displayed, use the Parameters tabbed pane to configure an agent for your test
    system. Refer to [Choosing an Agent Configuration](choosingAgents.html).

[]{#startGUI.classPaths}

## Classes

The following table describes the classes required by an agent.

  Classes         Descriptions
  --------------- -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  Agent Classes   The location of the agent classes installed on your test system. The agent classes are either located in the `javatest.jar` file or in the directory containing the minimum set of classes required to run the agent from the GUI. Some test suites include additional `.jar` files containing classes needed for an agent to run tests. These `.jar` files must also be included in the command string. Refer to [\"Installing Agent Classes on a Test System](loadingClasses.html) for a description of how agent classes can be installed.
  Test Classes    Test classes are located in the classes directory of the test suite.

The most common error in setting up a test platform to use an agent is entering the wrong class
paths in the command string. Configuring your test platform to use the simplest class paths
increases the reliability of the test run.

[]{#startGUI.appOptions}

## GUI Options

The following types of options can be included in the command line used used to start the GUI:

-   [Options to Display Help](#startGUI.help)
-   [Options Set Only in the Command Line](#startGUI.only)
-   [Options to Run the Agent](#startGUI.both)
-   [Options to Configure the Agent](#startGUI.configure)

[]{#startGUI.help}

### Options to Display Help

The following table describes the options that can be used on the command line to display help.

+-------------------------------------------------+-------------------------------------------------+
| Function                                        | Option                                          |
+=================================================+=================================================+
| Display Command-Line Help                       | `-help` or `-usage`                             |
|                                                 |                                                 |
|                                                 | Displays command-line help.                     |
+-------------------------------------------------+-------------------------------------------------+
| Display JT Harness harness Online Help          | `-onlineHelp`                                   |
|                                                 |                                                 |
|                                                 | Displays JT Harness harness online help without |
|                                                 | starting the JT Harness harness. Press          |
|                                                 | Control - C to return to the command-line       |
|                                                 | interface.                                      |
+-------------------------------------------------+-------------------------------------------------+

[]{#startGUI.only}

### Options Set Only in the Command Line

The following table describes the options that are the only parameters that cannot be set in the
GUI. If you choose to use them, they must be set in the command line.

+-------------------------------------------------+-------------------------------------------------+
| **Function**                                    | Option                                          |
+=================================================+=================================================+
| Set the Number of Tasks in History              | `-history` *number-of-items*                    |
|                                                 |                                                 |
|                                                 | Specifies the maximum number of tasks displayed |
|                                                 | in the history tabbed pane. The history tabbed  |
|                                                 | pane displays a list of the current and         |
|                                                 | recently completed tasks performed by the       |
|                                                 | agent. Each task is displayed with a code       |
|                                                 | indicating its current state. You can only set  |
|                                                 | the maximum number of tasks from the command    |
|                                                 | line.                                           |
+-------------------------------------------------+-------------------------------------------------+
| Automatically Start the Agent                   | `-start`                                        |
|                                                 |                                                 |
|                                                 | Automatically starts the agent after all        |
|                                                 | command line options are validated. This option |
|                                                 | is used only if the agent is also configured in |
|                                                 | the command line. If the `-start` option is not |
|                                                 | used, click the Start button in the agent GUI   |
|                                                 | to start testing.                               |
+-------------------------------------------------+-------------------------------------------------+
| Set Tracing                                     | `-trace`                                        |
|                                                 |                                                 |
|                                                 | Sends detailed information about agent          |
|                                                 | activities to the system output stream. You can |
|                                                 | only set the tracing option from the command    |
|                                                 | line.                                           |
+-------------------------------------------------+-------------------------------------------------+

[]{#startGUI.both}

### Options to Run the Agent

The following table describes the options that can be set either in the command line or in the GUI
that specify how the agent runs tests.

+-------------------------------------------------+-------------------------------------------------+
| Function                                        | Option                                          |
+=================================================+=================================================+
| Set Concurrency                                 | `-concurrency` *number-of-tests*                |
|                                                 |                                                 |
|                                                 | Sets the maximum number of simultaneous         |
|                                                 | requests handled by the agent. Each request     |
|                                                 | requires a separate connection to the JT        |
|                                                 | Harness harness and a separate thread inside    |
|                                                 | the agent. The request might also require a     |
|                                                 | separate process on the test system running the |
|                                                 | agent. The default setting is one.              |
+-------------------------------------------------+-------------------------------------------------+
| Specify a Map File                              | `-map` *map-file*                               |
|                                                 |                                                 |
|                                                 | Specifies a map file for the agent to use when  |
|                                                 | translating host specific values. You can also  |
|                                                 | use the parameters tabbed pane to set the map   |
|                                                 | file.                                           |
+-------------------------------------------------+-------------------------------------------------+

[]{#startGUI.configure}

### Options to Configure the Agent

The following parameters can be used to configure an agent at the time the agent GUI is started:

-   Parameters to Configure an Active Agent
-   Parameters to Configure a Passive Agent
-   Parameters to Configure a Passive Agent

These parameters duplicate functions of the GUI and must only be used if you have already determined
the configuration of the agent for you test system. Refer to [Choosing an Agent
Configuration](choosingAgents.html).

Anytime the agent is not running, you can use the Parameters tabbed pane to change any of these
parameters. Consequently, including these parameters in the command line is not recommended because
they add complexity to the command line and duplicate functions already provided by the Parameters
tabbed pane.

#### Parameters to Configure an Active Agent

The following table describes the parameters that configure the GUI for an active agent.

+-------------------------------------------------+-------------------------------------------------+
| Function                                        | Option                                          |
+=================================================+=================================================+
| Use active communication                        | `-active`                                       |
|                                                 |                                                 |
|                                                 | Starts an active agent.                         |
+-------------------------------------------------+-------------------------------------------------+
| Identifies host                                 | `-activeHost` *host-name*                       |
|                                                 |                                                 |
|                                                 | Connects to the system running the JT Harness   |
|                                                 | harness. For the *host-name* variable, you can  |
|                                                 | either use the name or the IP address of the    |
|                                                 | system running the JT Harness harness.          |
+-------------------------------------------------+-------------------------------------------------+
| Specifies port                                  | `-activePort` *port-number*                     |
|                                                 |                                                 |
|                                                 | Use only if the JT Harness harness listens for  |
|                                                 | active agents on a port other than 1907.        |
+-------------------------------------------------+-------------------------------------------------+

#### Parameters to Configure a Passive Agent

The following table describes the parameters that configure the GUI for a passive agent.

+-------------------------------------------------+-------------------------------------------------+
| Function                                        | Option                                          |
+=================================================+=================================================+
| Use passive communication                       | `-passive`                                      |
|                                                 |                                                 |
|                                                 | Starts a passive agent.                         |
+-------------------------------------------------+-------------------------------------------------+
| Specifies port                                  | `-passivePort` *port-number*                    |
|                                                 |                                                 |
|                                                 | Use only if the JT Harness harness sends        |
|                                                 | requests to passive agents on a port other than |
|                                                 | 1908.                                           |
+-------------------------------------------------+-------------------------------------------------+

#### Parameters to Configure a Serial Agent

The following table describes the parameters that configure the GUI for a serial agent.

+-------------------------------------------------+-------------------------------------------------+
| Function                                        | Option                                          |
+=================================================+=================================================+
| Use serial communication                        | `-serial`                                       |
|                                                 |                                                 |
|                                                 | Starts a serial agent.                          |
+-------------------------------------------------+-------------------------------------------------+
| Specifies port                                  | `-serialPort` *port-number*                     |
|                                                 |                                                 |
|                                                 | Sets the serial port used by the JT Harness     |
|                                                 | harness to communicate with the agent.          |
+-------------------------------------------------+-------------------------------------------------+

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2011, Oracle and/or its affiliates. All rights reserved.
