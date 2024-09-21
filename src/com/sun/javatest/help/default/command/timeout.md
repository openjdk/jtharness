---
hIndent: 2
title: Setting Timeout With timeoutFactor
---

[]{#timeout}

# Setting Timeout With `timeoutFactor`

Each test in a test suite has a timeout limit. The harness waits for a test to complete for the
duration of that limit before moving on to the next test. You can use the `timeoutFactor` command to
change the timeout limit:

[*\> jtharness*](aboutExamples.html) \... \[*initial-setup-commands*\] \... `-timeoutFactor`
*number* \... \[*task-command*\] \...

See [About the Command-Line Examples](aboutExamples.html) for a description of the use of *\>
jtharness* in the example.

See [Command-Line Overview](commandLine.html) for a detailed description of the command-line
structure.

Each test\'s timeout limit is multiplied by the time factor value. For example, if you specify a
value of 2.0, the timeout limit for tests with a 10 basic time limit becomes 20 minutes. See
[Formatting a Command](formatCommands.html) for descriptions of the command formats. Note that the
format of the value input for the timeout factor is dependant on the locale.

When creating a command string to change the timeout limit, include the commands in the following
sequence:

1.  Include the commands required to set up a configuration.

> See [Setup Commands](setupCommands.html) for detailed description of the available commands.

2.  Include the commands required to specify the timeout limit (`timeoutFactor` *number*).
3.  (Optional) Include the `runtests` command.

> See [Running Tests With `runtests`](runTests.html) for a detailed description of the command.

## Detailed Example of `timeFactor` Command

In the following example, *myconfig*`.jti` and *myexcludelist*`.jtx` represent file names that might
exist on your system.

**Command Options Format Example:**

[*\> jtharness*](aboutExamples.html) `-config` *myconfig*`.jti -timeoutFactor 2.0 -runtests`

See [Formatting a Command](formatCommands.html) for descriptions and examples of other command
formats that you can use.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2011, Oracle and/or its affiliates. All rights reserved.
