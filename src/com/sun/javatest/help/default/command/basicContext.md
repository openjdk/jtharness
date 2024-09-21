---
hIndent: 1
title: Initial Setup Commands
---

[]{#basicContext}

# Initial Setup Commands

Before you can perform tasks from the command line, you must first set up a configuration for the
harness to use. You can set up a configuration by performing *at least one* of the following:

1.  Specify an existing configuration (`.jti`) file. You are not required to specify either a test
    suite or a work directory.
2.  Specify an existing work directory and a configuration file. You are not required to specify a
    test suite.
3.  Open a test suite, create an empty work directory, and specify a configuration file.

After setting up a configuration, you can then change specific values for your specific
requirements. See [Setting Specific Values](otherConfigValues.html) for the commands used to modify
the values in the configuration.

You can include commands in any combination on the command line provided the initial set-up commands
are specified before any other commands in the command line.

You can use any of the following commands to set up a configuration for the harness to use when
performing tasks:

-   **`config`** - Specifies an existing configuration file. See [Specifying a Configuration File
    With `config`](config.html) for a detailed description of this command.
-   **`workDirectory`** or **`workDir`** - Specifies an existing work directory or to create a new
    work directory. See [Specifying a Work Directory With `workDir`](workdir.html) for a detailed
    description of this command.
-   **`testSuite`** - Used to specify a test suite. See [Specifying a Test Suite With
    `testSuite`](testsuite.html) for a detailed description of this command.
-   **`open`** - Specifies a test suite, work directory, configuration file, or parameter file. See
    [Specifying a Test Suite, Work Directory or Configuration With `open`](open.html) for a detailed
    description of this command.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2011, Oracle and/or its affiliates. All rights reserved.
