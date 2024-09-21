---
hIndent: 0
title: Setup Commands
---

[]{#setupCommands}

# Setup Commands

Before you can perform a task from the command line, you must first use setup commands to specify a
configuration.

After setting up a configuration, you can then modify the values in the configuration for your
specific requirements. These changed values override but do not change values in the configuration
file. You can use configuration templates from a central resource to run tests on different test
platforms and configurations.

![The following text is a note](../../images/hg_note.gif){longdesc="setupCommands.html"}\
See [About the Command-Line Examples](aboutExamples.html) for a description of use of *\> jtharness*
in the following example.

The setup commands are used in the following sequence in the command line:

[*\> jtharness*](aboutExamples.html) \[*initial-setup-commands*\] \[*set-specific-values*\]
\[*additional-setup-commands*\] \[*task-commands*\]

Setting specific values and additional setup commands are optional.

The task command at the end of the example is also optional. If a task command is not included, the
harness uses the specified configuration and any changes set on the command line to open the GUI.

For additional information about using setup commands see the following topics:

-   [Initial Setup Commands](basicContext.html)
-   [Setting Specific Values](otherConfigValues.html)
-   [Additional Setup Commands](harnessSettings.html)

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2004, 2011, Oracle and/or its affiliates. All rights reserved.
