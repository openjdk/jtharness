---
hIndent: 2
title: Agent Monitor Window
---

[]{#window}

# Agent Monitor Window

Open the Agent Monitor window by using the JT Harness harness GUI Test Manager to choose Window
**`->`** Open **`->`** Agent Monitor. See the *JT Harness User\'s Guide: Graphical User Interface*
for detailed description of the Test Manager window.

The Agent Monitor window contains two sections, Agent Pool and Agents Currently In Use.

![The Agent Monitor window](../../images/agentMonitor.gif){border="0" longdesc="window.html"}

[]{#window.agentPool}

## Agent Pool

Agent Pool lists the active agents that are available to run tests. When active agents connect to
the JT Harness harness, they are added to the agent pool. When the JT Harness harness requires an
active agent to run a test, it moves the agent from Agent Pool to Agents Currently In Use until the
test is completed.

The following table lists and describes the contents of the Agent Pool GUI.

  Field         Description
  ------------- -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  `Listening`   Click the check box to enable listening for active agents. If listening is not enabled when an agent starts, the agent issues a message that it cannot connect to the JT Harness harness and then waits for its timeout period to end before attempting to recontact the harness.
  `Port`        Port 1907 is the default port used by active agents. If your agent uses a different port, you must either change the value used by the agent or change this value to match the agent.
  `Timeout`     When the agent pool is empty, the timeout value sets the number of seconds that the JT Harness harness waits between tests for an available agent before reporting the test result as an error. If you run tests with one agent, a latent period might occur between the time when the agent completes the test and when it returns to the agent pool. The timeout value must be greater than the agent\'s latent period. The default value of 180 seconds is usually sufficient.

[]{#window.agentInUse}

## Agents Currently In Use

Agents Currently In Use lists all agents currently used by the JT Harness harness to run tests. When
agents are not running tests they are removed from the list (active agents re-register with the
agent pool). Click on an agent in the list to display detailed information about the agent and the
test it is running. The detailed information is displayed in the text fields at the bottom and can
be used to troubleshoot problems using an agent to run tests.

The following table lists and describes the contents of the Agents Currently In Use GUI.

  Field             Description
  ----------------- -----------------------------------------------------
  `Address`         Network address of the agent
  `Tag`             Test executed by the agent
  `Request`         Function executed by the agent
  `Execute`         Class executed by the agent
  `Args`            Arguments passed to the class executed by the agent
  `Localize Args`   Checked if the agent uses a map file

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2010, Oracle and/or its affiliates. All rights reserved.
