---
hIndent: 1
title: Specifying Keywords With keywords
---

[]{#keywords}

# Specifying Keywords With `keywords`

The test suite may provide keywords that you can use on the command line to restrict the set of
tests to be run. Use the `keyword` command to specify the keywords used to filter the tests that are
run.

[*\> jtharness*](aboutExamples.html) \... \[*initial-setup-commands*\] \... `-keywords` *expression*
\... \[*task-command*\] \...

See [About the Command-Line Examples](aboutExamples.html) for a description of the use of *\>
jtharness* in the following example.

See [Command-Line Overview](commandLine.html) for a detailed description of the command line
structure.

Refer to the test suite documentation for a list of supported [keyword
expressions](../confEdit/keywords.html#ListofExpressions) and [logical
operators](../confEdit/keywords.html#ListofOperators).

When creating a command string that specifies keywords, include the commands in the following
sequence:

1.  Include the commands required to set up a configuration.

> See [Set-up Commands](setupCommands.html) for a description of the commands.

2.  Include the commands to specify keywords used (`keywords` *expression*).
3.  (Optional) Include a task command (such as `runtests`).

> See [Task Commands](taskCommands.html) for the commands that you can include.

## Detailed Example of `keywords` Command

In the following example, *myconfig*`.jti` and *myexcludelist*`.jtx` represent file names that might
exist on your system.

**Command Options Format Example:**

[*\> jtharness*](aboutExamples.html) `-config` *myconfig*`.jti` `-keywords interactive -runtests`

See [Formatting a Command](formatCommands.html) for descriptions and examples of other command
formats that you can use.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2004, 2011, Oracle and/or its affiliates. All rights reserved.
