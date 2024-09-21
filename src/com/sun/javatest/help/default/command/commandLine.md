---
hIndent: 0
title: Command Line Summary
---

[]{#commandLine}

# Command-Line Summary

You can use commands in the command line or as a part of a product build process to configure the
harness, run tests, write test reports, and start the GUI using specific configuration values.

The harness executes the commands from left to right in the sequence that they appear in the command
string. Include commands in the command string as though you were writing a script. The harness does
not restrict either the number of commands or the groups of commands that you can use in a command
string.

[*\> jtharness*](aboutExamples.html) \[*Setup commands*\] \[*Task commands*\] \[*Desktop commands*\]
\[*Information commands*\]

The commands are included as a formatted set in the following sequence:

1.  [Setup commands](setupCommands.html) - Required by task commands to set values used for the test
    run and to set specific values used when performing other tasks. Setup commands must precede the
    task or desktop commands. Setup commands can be used to set specific values (without a task
    command) when starting the GUI.
2.  [Task commands](taskCommands.html) - Required to run tests, and write reports. Task commands
    require one or more preceding setup commands.
3.  [Desktop commands](desktopCommands.html) - Use in place of the task commands to start the GUI
    with a new desktop or to specify status colors used in the GUI. Setup commands are optional when
    using Desktop commands.
4.  [Information Commands](displayHelp.html) - Use information commands to display command-line
    help, online help, or version information without starting the harness. Information commands do
    not require any other commands on the command line.

For additional information about using the command-line interface, see the following topics:

-   [About Command-Line Examples](aboutExamples.html)
-   [Formatting a Command](formatCommands.html)
-   [Using Command Files](commandFile.html)
-   [Index of Available Commands](availableCommands.html)

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2004, 2012, Oracle and/or its affiliates. All rights reserved.
