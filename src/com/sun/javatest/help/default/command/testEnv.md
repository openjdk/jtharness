---
hIndent: 1
title: Specifying a Test Environment (env) \[deprecated\]
---

[]{#env}

# Specifying a Test Environment \[deprecated\]

This command is only used for test suites with environment [(`.jte`)]{#jte} files containing
multiple environments. You can use the `env` command with the `envFiles` command to specify a
specific test environment contained in an environment file:

[*\> jtharness*](aboutExamples.html) \... \[*initial set-up commands*\] \... `-envFile`
*path/filename* `-env` *environment-name* \... \[*task command*\] \...

See [About the Command-Line Examples](aboutExamples.html) for a description of the use of *\>
jtharness* in the following example.

When creating a command string to specify a specific test environment, include the commands in the
following sequence:

1.  [Set up a configuration](setupCommands.html)
2.  Specify a test environment (`env` *environment-name*)
3.  [Include the `runtests` command](runTests.html) (optional).

See [Command-Line Overview](commandLine.html) for a description of the command line structure.

## Detailed Example of `envFile` Command

In the following example, *path/filename* represents a file name that might exist on your system and
*environment-name* represents an environment name that exists in the environment file.

**Command Options Format Example:**

[*\> jtharness*](aboutExamples.html) `-envFile` *path/filename* `-env` *environment-name*
`-runtests`

See [Formatting a Command](formatCommands.html) for descriptions and examples of other command
formats that you can use.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2004, 2011, Oracle and/or its affiliates. All rights reserved.
