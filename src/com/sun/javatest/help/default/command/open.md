---
hIndent: 2
title: Opening a Test Suite, Work Directory or Configuration (open)
---

[]{#open}

# Specifying a Test Suite, Work Directory or Configuration (`open`)

To specify a test suite, work directory, or a configuration `.jti` file, use the `open` command:

\... `open` *path/filename* \...

![The following text is a note](../../images/hg_note.gif){longdesc="open.html"}\
See [About the Command-Line Examples](aboutExamples.html) for a description of the use of *\>
jtharness* in the following example. See [Command-Line Overview](commandLine.html) for a description
of the command line structure. See [Formatting a Command](formatCommands.html) for descriptions and
examples of the following command formats.

**Command Options Example:**

[*\> jtharness*](aboutExamples.html) \... `-open` *path/filename* \... \[*task command*\] \...

**Single String Arguments Example:**

[*\> jtharness*](aboutExamples.html) \... `; open` *path/filename* ; \... \[*task command*\] \...

**Command File Example:**

[*\> jtharness*](aboutExamples.html) `@`*mycmd*`.jtb` \... \[*task command*\] \...

In addition to any other commands, for this example the *mycmd*`.jtb` command file must contain the
command:\
\"`open` *path/filename*;\"

Refer to [Using Command Files](commandFile.html) for detailed information about creating and using
command files.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2004, 2011, Oracle and/or its affiliates. All rights reserved.
