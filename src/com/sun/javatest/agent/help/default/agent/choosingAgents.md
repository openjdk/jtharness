---
hIndent: 1
title: Choosing the Type of Agent
---

[]{#configureActiveAgents}

# Choosing the Type of Agent

The JT Harness harness agent is a lightweight program compatible with Java Development Kit, version
1.1, that uses a bi-directional serial connection supporting both TCP/IP and RS-232 protocols to
communicate between the test system and the JT Harness harness.

You can use the agent provided by the JT Harness harness if your test system meets the following
minimum requirements:

-   The device supports a communication layer that can last the duration of a test (several
    minutes).
-   The device must be able to have the agent classes loaded on it.

The type of agent that you use depends on the communication protocol used between your test system
and the JT Harness harness and on the type of initial connection made between the agent and the JT
Harness harness. The following table describes the types of agent and the communication protocol.

+-------------------------------------------------+-------------------------------------------------+
| Mode                                            | Description                                     |
+=================================================+=================================================+
| Active                                          | Use active mode (active agent) when you want    |
|                                                 | the agent to initiate the connection to the JT  |
|                                                 | Harness harness via TCP/IP.                     |
|                                                 |                                                 |
|                                                 | Agents using active communication allow you     |
|                                                 | perform the following actions:                  |
|                                                 |                                                 |
|                                                 | -   Run tests in parallel using many agents at  |
|                                                 |     once                                        |
|                                                 | -   Specify the test machines at the time you   |
|                                                 |     run the tests                               |
|                                                 |                                                 |
|                                                 | Active agents are used for network connections  |
|                                                 | and are recommended. If the security            |
|                                                 | restrictions of your test system prevent        |
|                                                 | incoming connections then you must use an       |
|                                                 | active agent.                                   |
|                                                 |                                                 |
|                                                 | The JT Harness harness must be running and      |
|                                                 | agent pool listening must be enabled before     |
|                                                 | starting an active agent. Use the [Agent        |
|                                                 | Monitor window](window.html) in the JT Harness  |
|                                                 | harness GUI to enable listening.                |
|                                                 |                                                 |
|                                                 | If listening is not enabled when the agent      |
|                                                 | starts, it returns an error message and waits   |
|                                                 | until its timeout period ends before            |
|                                                 | re-contacting the JT Harness harness.           |
+-------------------------------------------------+-------------------------------------------------+
| Passive                                         | Use passive mode (passive agent) when you want  |
|                                                 | the agent to wait for the JT Harness harness to |
|                                                 | initiate the connection via TCP/IP.             |
|                                                 |                                                 |
|                                                 | Because the JT Harness harness only initiates a |
|                                                 | connection to a passive agent when it runs      |
|                                                 | tests, passive communication has the following  |
|                                                 | characteristics:                                |
|                                                 |                                                 |
|                                                 | -   Requires that you specify the test machine  |
|                                                 |     as part of the test configuration, not at   |
|                                                 |     the time you run the tests                  |
|                                                 | -   Does not allow you to run tests in parallel |
|                                                 |                                                 |
|                                                 | Passive agents are used for network connections |
|                                                 | and must be started before the harness attempts |
|                                                 | to run tests. If the JT Harness harness issues  |
|                                                 | a request before the passive agent is started,  |
|                                                 | the harness waits for an available agent until  |
|                                                 | its timeout period ends. If the timeout period  |
|                                                 | ends before an agent is available, the JT       |
|                                                 | Harness harness reports an error for the test.  |
+-------------------------------------------------+-------------------------------------------------+
| Serial                                          | Use serial mode (serial agent) when you want    |
|                                                 | the agent to use an RS-232 serial connection.   |
|                                                 | Serial agents wait for the JT Harness harness   |
|                                                 | to initiate the connection. Infrared, parallel, |
|                                                 | USB, and firewire connections can also be added |
|                                                 | through the JT Harness harness API by modeling  |
|                                                 | the existing serial system.                     |
|                                                 |                                                 |
|                                                 | Because the JT Harness harness only initiates a |
|                                                 | connection to serial agent when it runs tests,  |
|                                                 | serial communication has the following          |
|                                                 | characteristics:                                |
|                                                 |                                                 |
|                                                 | -   Requires that you specify the test machine  |
|                                                 |     as part of the test configuration, not at   |
|                                                 |     the time you run the tests                  |
|                                                 | -   Does not allow you to run tests in parallel |
+-------------------------------------------------+-------------------------------------------------+
| Other                                           | If your system does not meet the minimum        |
|                                                 | requirements or if you have unique performance  |
|                                                 | requirements, you can use the JT Harness        |
|                                                 | harness API to create a custom agent. Refer to  |
|                                                 | your test suite documentation for a description |
|                                                 | of how to configure and run it.                 |
+-------------------------------------------------+-------------------------------------------------+

**Next task:**

[**Starting a JT Harness Agent**](startAgent.html): Start an agent on your test system.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2011, Oracle and/or its affiliates. All rights reserved.
