---
hIndent: 2
title: History Pane
---

[]{#historyPane}

# History Pane

The agent GUI uses the history tabbed pane to enable monitoring and troubleshooting agent activity
by displaying a dynamic list of tasks that an agent is currently executing and tasks that an agent
recently completed. The number of tasks maintained in this list is not configurable.

![The history tabbed pane in the agent
GUI](../../images/agentGUIhistory.gif){longdesc="historyPane.html"}

To view the details about a specific task, click on it in the list. The GUI displays the selected
task tabbed pane contained details about the task.

Refer to [Selected Task Pane](taskPane.html) for a detailed description of the task information that
is displayed.

Each task in the list contains a code indicating its current state. The following table describes
the state codes displayed in the GUI.

  Current State    Description
  ---------------- ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  CONN host:port   Shows that the JT Harness harness agent has an open connection to the JT Harness harness, at the specified network address, and that the JT Harness harness agent is waiting for a request to be sent over the connection. If the JT Harness harness agent is running in active mode, it waits until JT Harness harness sends the request. If the agent is running in passive mode, this state usually appears temporarily because JT Harness harness normally initiates a connection and then immediately sends the request. The host normally is identified by its host name. If JT Harness harness cannot determine the host name, the IP address of the host is shown instead.
  EXEC tag         This state shows that the JT Harness harness agent is executing a task on behalf of JT Harness harness. The tag is an identification of the task supplied by JT Harness harness as part of the request.
  \*IO\* tag       This state shows that the JT Harness harness agent was executing a task on behalf of JT Harness harness but that some exception occurred while trying to send the results to the JT Harness harness.

If a task in the list displays a state from the following table, this indicates that the JT Harness
harness agent has completed a request for JT Harness harness. These states correspond to the various
possible outcomes of the task and are the same as the outcomes that the JT Harness harness gets when
it runs tests directly (without the assistance of the JT Harness harness agent). The following table
describes the states that a task might have.

  State   Description
  ------- -------------------------------------------------------------------------------------------
  PASS:   Task completed successfully.
  FAIL:   Task indicated that it failed.
  ERR:    Task encountered some error before it could properly be executed.
  !RUN:   Task has inappropriately indicated that it has not been run. This state must never occur.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2010, Oracle and/or its affiliates. All rights reserved.
