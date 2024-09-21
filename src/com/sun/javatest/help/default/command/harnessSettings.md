---
hIndent: 1
title: Additional Setup Commands
---

[]{#settings}

# Additional Setup Commands

In most cases, you use the command line to perform functions that are also available through the
GUI. However, you can also use the command line to specify how the harness starts.

When starting the harness, you can include additional commands in the command line to:

-   [Include all system properties](#settings.systemProperties) in test execution environments.
-   [Set an environment variable](#settings.envVariable) that you want inherited in every test
    environment.
-   [Set the agent pool port number.](#settings.agentPoolPort)
-   [Set the agent pool timeout.](#settings.agentPoolTimeout)
-   [Start the active agent pool.](#settings.startAgentPool)

The harness uses a new desktop when you include GUI commands in the command line.

The following table describes the commands used in the command line to specify how the harness
starts.

Command

Function

[]{#settings.systemProperties} `-EsysProps`

Includes all system properties in test execution environments.

[]{#settings.envVariable} `-E`*name=value*

Sets an environment variable that is inherited in every test environment created.

The `-E`*name=value* command tunnels in values from the external shell. The method used in previous
versions of the harness to tunnel in values from the external shell is now deprecated.

[]{#settings.agentPoolPort} `-agentPoolPort` *port*

Set the agent pool port number.

Use this command only when you are configuring the harness and the agent to use a port other than
1907.

[]{#settings.agentPoolTimeout} `-agentPoolTimeout` *#seconds*

Set the agent pool timeout.

Sets the number of seconds that the harness waits between tests for an available agent before
reporting the test result as an error. The default value of 180 seconds is usually sufficient. You
can also set this value in the GUI if you are not running the harness from the command line.

[]{#settings.startAgentPool} `-startAgentPool`

Start the active agent pool.

If you use an active agent and run the harness from the command line, you must add
`-startAgentPool`to the command string to start the agent pool.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2010, Oracle and/or its affiliates. All rights reserved.
