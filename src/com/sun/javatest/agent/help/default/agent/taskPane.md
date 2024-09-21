---
hIndent: 2
title: Selected Task Pane
---

[]{#taskPane}

# Selected Task Pane

The selected task tabbed pane in the agent GUI displays detailed information about a task selected
from the task list in the history pane.

Refer to [History Pane](historyPane.html) for a description of the task list.

![The selected task tabbed pane](../../images/agentGUItasks.gif){longdesc="taskPane.html"}

The following table describes the contents of the selected task tabbed pane.

  Field       Description
  ----------- ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  `client`    Displays the network address (host and port) of the source of the task request. The host is normally identified by its host name, but if JT Harness harness cannot determine the host name, the IP address of the host is displayed instead.
  `request`   Displays the tag that was supplied with the request in order to identify itself.
  `class`     Displays the name of the class that was specified in the request. This is the class that is loaded and run in fulfillment of the request.
  `args`      Displays the arguments that were specified in the request. These arguments are passed to the class that is executed.
  `result`    If and when the task is completed, this field contains the outcome of the task, as indicated by a JT Harness harness Status object.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2009, Oracle and/or its affiliates. All rights reserved.
