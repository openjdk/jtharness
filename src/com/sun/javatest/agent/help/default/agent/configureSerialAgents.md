---
hIndent: 2
title: Specifying Serial Agent Options
---

[]{#configureSerialAgents}

# Specifying Serial Agent Options

Serial agents can be configured and run from a command-line, GUI, or applet tag. Refer to [Starting
a JT Harness harness agent](startAgent.html) for a description of the different features and
functions that each provides.

Depending on how you choose to start the agent, you must set the following minimum set of parameters
from the command line, GUI Parameter pane, or applet tag:

-   [Mode](#configureSerialAgents.mode)
-   [Port](#configureSerialAgents.port)

[]{#configureSerialAgents.mode}

## Mode

The type of agent mode that you use determines how the agent communicates with the JT Harness
harness and the protocol that is used. A serial agent waits for the JT Harness harness to initiate
the connection via an RS-232 serial connection or a connection added through the JT Harness harness
API that models the serial system.

To specify a serial agent, use the appropriate setting or option from the following table.

  Interface            Option or Setting
  -------------------- -----------------------------------------------------------------------------------------------
  Default              Active
  Command line         -serial
  Applet tag           **`<`**`param name=mode value=serial`**`>`**
  GUI Parameter pane   ![Specify a serial agent](../../images/serialMode.gif){longdesc="configureSerialAgents.html"}

[]{#configureSerialAgents.port}

## [Port]{#port}

Specifies the com port that the serial agent uses to listen for the JT Harness harness. The JT
Harness harness and agent must use the same port. If the ports are not the same, the JT Harness
harness cannot communicate with the agent.

To specify a port, use the appropriate setting or option from the following table.

  Interface            Option or Setting
  -------------------- --------------------------------------------------------------------------------------------------------------------------
  Command line         `-serialPort` *port-number*
  Applet tag           **`<`**`param name=serialPort value=`*`port-number`***`>`**
  GUI Parameter pane   ![Specifies the com port that the serial agent uses](../../images/serialPort.gif){longdesc="configureSerialAgents.html"}

**Next task:**

[**Specifying Additional Agent Options**](additionalOptions.html): Additional parameters to display
help or configure other agent properties.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2011, Oracle and/or its affiliates. All rights reserved.
