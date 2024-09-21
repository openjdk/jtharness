---
hIndent: 2
title: Starting an Agent Applet
---

[]{#startApplets}

# Starting an Agent Applet

Before you can start an agent applet, the required classes must be installed on your test system.
Refer to [Installing Agent Classes on Test Systems](loadingClasses.html) for the location and list
of classes required to start the agent applet.

1.  If an HTML page containing the required applet is not available, create it in your test suite
    root directory.\
    Refer to [Agent Applet Tag\"](#startApplets.appletTag) for a detailed description of an applet
    tag.

<!-- -->

2.  Use a web browser to open an HTML page containing the agent applet tag.\
    The applet tag must be compatible with your browser\'s VM.

<!-- -->

3.  Use the Parameters tabbed pane to configure and run the agent. See the following topics for
    additional information:
    -   [**Specifying Active Agent Options:**](configureActiveAgents.html) Parameter settings
        required to run an active agent.
    -   [**Specifying Passive Agent Options:**](configurePassiveAgents.html) Parameter settings
        required to run a passive agent.
    -   [**Specifying Serial Agent Options:**](configureSerialAgents.html) Parameter settings
        required to run a serial agent.

[]{#startApplets.appletTag}

## Agent Applet Tag

Because some browsers use built-in VMs to run applets, you must use a compatible applet or object
tag. Refer to your VM documentation for a description of the tags required to run applets on your
browser. The following example calls the agent applet and sets the parameters of the applet GUI. It
might not be compatible with your browser VM.

Agent parameters and run options can also be set in the applet tag. Refer to [Setting Parameters in
the Applet Tag](#startApplets.options).

**Sample agent applet tag:**

> **`<`**`APPLET`/\
> `code=`*applet-class-path*/\
> `archive=`*JT Harness harness-classes*/\
> `width=`*display-width*/\
> `height=`*display-height*/\
> **`>`**\
> `Applets have not been enabled`\
> `on your browser. You must enable`\
> `applets on your browser to display`\
> `the applet GUI used to run the agent. `\
> **`<`**`/APPLET`**`>`**

The following table describes the tags used in the applet.

+-------------------------------------------------+-------------------------------------------------+
| Tag                                             | Description                                     |
+=================================================+=================================================+
| `code`                                          | The agent applet class installed on your test   |
|                                                 | system.                                         |
|                                                 |                                                 |
|                                                 | Example:                                        |
|                                                 |                                                 |
|                                                 | `code=com.sun.javatest.agent.AgentApplet`       |
+-------------------------------------------------+-------------------------------------------------+
| `archive`                                       | The URL of the classes required to run the      |
|                                                 | agent applet on your test system. The classes   |
|                                                 | are either located in the `javatest.jar` file   |
|                                                 | or in a directory containing the minimum set of |
|                                                 | classes required to run the agent applet.       |
|                                                 |                                                 |
|                                                 | In the following example, the classes are       |
|                                                 | contained in the `javatest.jar` file located in |
|                                                 | the same directory as the HTML page. Refer to   |
|                                                 | [Installing Agent Classes on a Test             |
|                                                 | System](loadingClasses.html) for a description  |
|                                                 | of how the agent applet classes can be          |
|                                                 | installed.                                      |
|                                                 |                                                 |
|                                                 | Example:                                        |
|                                                 |                                                 |
|                                                 | `archive=javatest.jar`                          |
+-------------------------------------------------+-------------------------------------------------+
| `width`                                         | Sets the width of the GUI. An initial value of  |
|                                                 | 600 is suggested. However, you might need to    |
|                                                 | adjust the value based on your screen size and  |
|                                                 | resolution.                                     |
|                                                 |                                                 |
|                                                 | Example:                                        |
|                                                 |                                                 |
|                                                 | `width=600`                                     |
+-------------------------------------------------+-------------------------------------------------+
| `height`                                        | Sets the height of the applet. An initial value |
|                                                 | of 600 is suggested. However, you might need to |
|                                                 | adjust the value based on your screen size and  |
|                                                 | resolution.                                     |
|                                                 |                                                 |
|                                                 | Example:                                        |
|                                                 |                                                 |
|                                                 | `height=600`                                    |
+-------------------------------------------------+-------------------------------------------------+

[]{#startApplets.options}

## Setting Parameters in the Applet Tag

Parameters can also be set in the applet tag. Parameters in the applet tag are included as
**`<`**`param` *name \|value***`>`** pair tags.

**Sample agent applet tag:**

> **`<`**`APPLET`\
> `code=`*applet-class-path*\
> `archive=`*JT Harness harness-classes*\
> `width=`*display-width*\
> `height=`*display-height*\
> **`>`**\
> \
> \...\
> **`<`**`param name=`*parameter-name* `value=`*parameter-value***`>`**\
> `Applets have not been enabled on your browser.`\
> `You must enable applets on your browser to display`\
> `the applet GUI used to run the agent.`\
> **`<`**`/APPLET`**`>`**

The following two types of parameters can be included in the applet tag:

-   **Agent Parameters** : Specifies the agent type. Can be set either in the applet tag or in the
    GUI. Anytime the agent is not running, you can also use the Parameters tabbed pane to change the
    agent parameters. See the following topics for additional information:
    -   [**Specifying Active Agent Options**](configureActiveAgents.html): Parameter settings
        required to run an active agent.
    -   [**Specifying Passive Agent Options**](configurePassiveAgents.html): Parameter settings
        required to run a passive agent.
    -   [**Specifying Serial Agent Options**](configureSerialAgents.html): Parameter settings
        required to run a serial agent.

-   **Additional Parameters**: Specifies how an agent is run.

    See [Specifying Additional Agent Options](additionalOptions.html) for additional information.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2011, Oracle and/or its affiliates. All rights reserved.
