---
hIndent: 2
title: Specifying Active Agent Options
---

[]{#configureActiveAgents}

# Specifying Active Agent Options

Active agents can be configured and run from the application command-line, the application or applet
GUI, or the applet tag. Refer to [Starting a JT Harness harness agent](startAgent.html) for a
description of the different features and functions that each provides.

Depending on how you choose to start the agent, you must set the following minimum set of parameters
either in the command line, the GUI Parameter pane, or the applet tag:

-   [Mode](#configureActiveAgents.mode)
-   [Host](#configureActiveAgents.host)
-   [Port](#configureActiveAgents.port)

[]{#configureActiveAgents.mode}

## Mode

The type of agent mode that you use determines how the agent communicates with the JT Harness
harness and the protocol that is used. An active agent initiates the connection to the JT Harness
harness using TCP/IP communications protocol.

To specify an active agent mode, use the appropriate setting or option from the following table.

  Interface            Option or Setting
  -------------------- ---------------------------------------------------------------------------------------------------------------------
  Default              Active
  Command line         \-`active`
  Applet tag           **`<`**`param name=mode value=active`**`>`**
  GUI Parameter pane   ![GUI setting that specifies the type of agent](../../images/activeMode.gif){longdesc="configureActiveAgents.html"}

[]{#configureActiveAgents.host}

## Host

The host option identifies the system running the JT Harness harness. Because an active agent
initiates the connection to the JT Harness harness, the location of the system running the JT
Harness harness must be set before it can run.

To specify the system running the JT Harness harness, use the appropriate setting or option from the
following table.

  Interface            Option or Setting
  -------------------- ----------------------------------------------------------------------------------------------------------------------------------------------
  Default              None
  Command line         \-`activeHost` *host-name*
  Applet tag           **`<`**`param name=activeHost value=`*`host-name`***`>`**
  GUI Parameter pane   ![GUI setting that identifies the system running the JT Harness harness](../../images/activeHost.gif){longdesc="configureActiveAgents.html"}

[]{#configureActiveAgents.port}

## Port

The port option specifies the port used by the active agent to communicate with the JT Harness
harness. The agent and JT Harness harness must use the same port. If the ports are not the same, the
agent cannot communicate with the JT Harness harness. The default value for active agents is 1907.

To specify a port other than 1907, use the appropriate setting or option from the following table.

  Interface            Option or Setting
  -------------------- --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  Default              1907
  Command line         `-activePort` *port-number*
  Applet tag           **`<`**`param name=activePort value=`*`port-number`***`>`**
  GUI Parameter pane   ![GUI setting that specifies the port used by the active agent to communicate with the JT Harness harness](../../images/activePort.gif){longdesc="configureActiveAgents.html"}

**Next task:**

[**Specifying Additional Agent Options**](additionalOptions.html): Additional parameters to display
help or configure other agent properties.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2011, Oracle and/or its affiliates. All rights reserved.
