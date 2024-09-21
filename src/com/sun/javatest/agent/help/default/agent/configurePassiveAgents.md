---
hIndent: 2
title: Specifying Passive Agent Options
---

[]{#configurePassiveAgents}

# Specifying Passive Agent Options

Passive agents can be configured and run from the application command line, the application or
applet GUI, or the applet tag. Refer to [Starting a JT Harness harness agent](startAgent.html) for a
description of the different features and functions that each provides.

Depending on how you choose to start the agent, you must set the following minimum set of parameters
either in the command line, the GUI Parameter pane, or the applet tag:

-   [Mode](#configurePassiveAgents.mode)
-   [Port](#configurePassiveAgents.port)

[]{#configurePassiveAgents.mode}

## Mode

The type of agent mode that you use determines how the agent communicates with the JT Harness
harness and the protocol that is used. A passive agent waits for the JT Harness harness to initiate
the connection using TCP/IP communications protocol.

To specify a passive agent, use the appropriate setting or option from the following table.

  Interface            Option or Setting
  -------------------- ------------------------------------------------------------------------------------------------------
  Default              Active
  Command line         \-`passive`
  Applet tag           **`<`**`param name=mode value=passive`**`>`**
  GUI Parameter pane   ![Specifies the type of agent](../../images/passiveMode.gif){longdesc="configurePassiveAgents.html"}

[]{#configurePassiveAgents.port}

## Port

The port option specifies the port that the passive agent uses to listen for the JT Harness harness.
The JT Harness harness and agent must use the same port. If the ports are not the same, the JT
Harness harness cannot communicate with the agent. The default value for passive agents is 1908.

To specify a port other than 1908, use the appropriate setting or option from the following table.

  Interface            Option or Setting
  -------------------- -------------------------------------------------------------------------------------------------------------------------
  Default              1908
  Command line         `-passivePort` *port-number*
  Applet tag           **`<`**`param name=activePort value=`*`port-number`***`>`**
  GUI Parameter Pane   ![Specifies the port that the passive agent uses](../../images/passivePort.gif){longdesc="configurePassiveAgents.html"}

**Next task:**

[**Specifying Additional Agent Options**](additionalOptions.html): Additional parameters to display
help or configure other agent properties.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2011, Oracle and/or its affiliates. All rights reserved.
