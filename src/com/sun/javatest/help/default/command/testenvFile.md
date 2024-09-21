---
hIndent: 1
title: Specifying an Environment File (envFile or envFiles)
---

[]{#envFile}

# Specify an Environment File

This command only applies to older test suites that use environment files (`.jte`) instead of
configuration (`.jti`) files. Use the `envFile` command to specify an environment file (`.jte`)
containing test environments that the harness must use to run the test suite:

[*\> jtharness*](aboutExamples.html) \... \[*initial set-up commands*\] \... `-envFile`
*path/filename* \... \[*task command*\] \...

See [About the Command-Line Examples](aboutExamples.html) for a description of the use of *\>
jtharness* in the following example.

When creating a command string to specify an environment file, include the commands in the following
sequence:

1.  [Set up a configuration](setupCommands.html)
2.  Specify an environment file (`envFile` *path/filename*)
3.  [Include the `runtests` command](runTests.html) (optional).

See [Command-Line Overview](commandLine.html) for a description of the command line structure.

## Detailed Example of `envFile` Command

In the following example, *myenvFile*`.jte` represents a file name that might exist on your system.

**Command Options Format Example:**

[*\> jtharness*](aboutExamples.html) `-envFile` *myenvFile*`.jte -runtests`

See [Formatting a Command](formatCommands.html) for descriptions and examples of other command
formats that you can use.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2004, 2011, Oracle and/or its affiliates. All rights reserved.
