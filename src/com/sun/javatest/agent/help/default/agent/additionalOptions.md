---
hIndent: 2
title: Specifying Additional Agent Options
---

[]{#additionalOptions}

# Specifying Additional Agent Options

The following topics describe the additional options for using an agent:

-   [Options Used to Display Help](#additionalOptions.help)
-   [Options Used to Run and Monitor the Agent](#additionalOptions.both)

[]{#additionalOptions.help}

## Options Used to Display Help

The help option only displays command-line help for the agent regardless of the application class
used in the command line. To start an agent application or applet after displaying command-line
help, perform the steps in [Starting a JT Harness Agent](startAgent.html).

The following table contains options that are only used on the command line to display help.

+-------------------------------------------------+-------------------------------------------------+
| Option                                          | Function                                        |
+=================================================+=================================================+
| `-help` or `-usage`                             | Displays command-line help.                     |
|                                                 |                                                 |
|                                                 | Example:                                        |
|                                                 |                                                 |
|                                                 | `java -cp /lib/javat                            |
|                                                 | est.jar com.sun.javatest.agent.AgentMain -help` |
+-------------------------------------------------+-------------------------------------------------+

You must include the path of the `javatest.jar` file (represented as `/lib/javatest.jar` in the
example). The `javatest.jar` file is usually installed in the test suite `lib` directory when the JT
Harness harness is bundled with a test suite.

[]{#additionalOptions.both}

## Options Used to Run and Monitor the Agent

The following options can be set in the application command line, the application or applet GUI, or
the applet tag:

-   [Specify a Map File](#additionalOptions.mapFile)
-   [Set Concurrency](#additionalOptions.concurrencyValue)
-   [Set Number of Tasks in the History Tabbed Pane](#additionalOptions.taskHistory)
-   [AutoStart the Agent](#additionalOptions.autostart)
-   [Set Tracing](#additionalOptions.tracing)

[]{#additionalOptions.mapFile}

### Specify a Map File

The map option specifies that the agent use a map file to translate host specific values. Refer to
[Create a Map File](mapFile.html) for additional information about map files.

To specify a map file, use the appropriate setting or option from the following table.

  Interface            Option or Setting
  -------------------- -------------------------------------------------------------------------------------------------------------------------------------------------------
  Default              None (empty)
  Command line         `-map` *map-file*
  Applet tag           **`<`**`param name=map value=`*`map-file-url`***`>`**
  GUI Parameter pane   ![GUI setting that specifies an agent use a map file to translate host specific values](../../images/agentMap.gif){longdesc="additionalOptions.html"}

[]{#additionalOptions.concurrencyValue}

### Set Concurrency

To run tests concurrently, set the maximum number of simultaneous requests handled by the agent.
Each request requires a separate connection to the JT Harness harness and a separate thread inside
the agent. The request might also require a separate process on the test system running the agent.
The default setting is one.

To run concurrent tests, use the appropriate setting or option from the following table.

  Interface            Option or Setting
  -------------------- -----------------------------------------------------------------------------------------------------------------------------------------------------------------
  Default              One
  Command line         `-concurrency` *number-of-tests*
  Applet tag           **`<`**`param name=concurrency value=`*`number-of-tests`***`>`**
  GUI Parameter pane   ![GUI setting that sets the maximum number of simultaneous requests handled by the agent](../../images/agentConcurrency.gif){longdesc="additionalOptions.html"}

[]{#additionalOptions.taskHistory}

### Set Number of Tasks in the History Tabbed Pane

The history option specifies the maximum number of tasks displayed in the history tabbed pane. Refer
to [History Tabbed Pane](historyPane.html) for a description of the history tabbed pane and how it
is used to monitor an agent.

To set the tasks displayed in the history tabbed pane, use the appropriate setting or option from
the following table.

  Interface      Option or Setting
  -------------- --------------------------------------------------------------
  Default        One
  Command line   `-history` *number-of-items*
  Applet tag     **`<`**`param name=history value=`*`number-of-items`***`>`**
  GUI            Not supported

[]{#additionalOptions.autostart}

##### AutoStart the Agent

Th start option is only used with the application GUI class or as a parameter in the applet tag.
When used, the start option automatically starts the agent after all command line options are
validated and the GUI is displayed. The agent must be completely configured in the command line or
applet tag. When the `-start` option is not used, click the Start button in the agent GUI to start
testing.

To autostart the agent when the GUI is displayed, use the appropriate setting or option from the
following table.

  Interface      Option or Setting
  -------------- -----------------------------------------------------------------------------------------------------------------------------------------------------------------
  Default        False
  Command line   `-start`
  Applet tag     **`<`**`param name=start value=`*`true`***`>`**
  GUI            ![GUI setting that automatically starts the agent after all command line options are validated](../../images/agentStart.gif){longdesc="additionalOptions.html"}

[]{#additionalOptions.tracing}

##### Set Tracing

The trace option sends detailed information about agent activity to the system output stream.

To start tracing when the agent is run, use the appropriate setting or option from the following
table.

  Interface      Option or Setting
  -------------- -------------------------------------------------
  Default        False
  Command line   `-trace`
  Applet tag     **`<`**`param name=trace value=`*`true`***`>`**
  GUI            Not Supported

**Next task:**

[Monitoring JT Harness Agents](monitoring.html): Monitor an agent while it runs tests.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2011, Oracle and/or its affiliates. All rights reserved.
