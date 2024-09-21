---
hIndent: 2
title: Examples of Using Command Files
---

# Examples of Using Command Files

In the following examples, a command file (*mycommandfile*`.jtb`) is used to override the
`localHostName`value and the tests specified in the existing configuration.

The following three examples are provided:

-   Example Command File Contents
-   Command Line Using the Example Command File
-   Changing Values After the Example Command File is Set

![The following text is a note](../../images/hg_note.gif){longdesc="examplesCommandFile.html"}\
If you attempt to run these examples, you must replace *mytestsuite*`.ts`, *myworkdir*`.wd`, and
*myconfig*`.jti` with test suite, work directory, and `.jti` names that exist on your system. You
must also modify the contents of the example command file for your configuration file and test
suite. Win32 users must change / file separators to \\ to run these examples.

## Example Command File Contents

The following lines are the contents of the example command file, *mycommandfile*`.jtb`:

`#File sets localHostName and tests`\
`set jck.env.runtime.net.localHostName` *mymachine*`;`\
`tests api/javax_swing api/java_awt`

The `-set` and `-tests` command forms are not used in the command file. Command files only use the
\"Single String Arguments Format.\"

See [Setting Specific Configuration Values](settingValues.html) for additional examples of using the
`set`command. See [Specifying Tests to Run](selectingTests.html) for additional examples of using
the `tests` command.

![The following text is a note](../../images/hg_note.gif){longdesc="open.html"}\
See [About the Command-Line Examples](aboutExamples.html) for a description of the use of *\>
jtharness* in the following example. See [Command-Line Overview](commandLine.html) for a description
of the command line structure. See [Formatting a Command](formatCommands.html) for descriptions and
examples of the following command formats.

## Command Line Using the Example Command File

In the following examples, a test suite (*mytestsuite*`.ts`), work directory (*myworkdir*`.wd`), and
configuration file (*myconfig*`.jti`) are opened, and the command file (*mycommandfile*`.jtb`) is
read and executed before running tests.

### **Command Options Format Example**

[*\> jtharness*](aboutExamples.html) `-open` *myconfig*`.jti @`*mycommandfile*`.jtb -runtests`

### **Single String Arguments Format Example**

[*\> jtharness*](aboutExamples.html) `"open` *myconfig*`.jti; @`*mycommandfile*`.jtb; runtests"`

## Changing Values After the Example Command File is Set

You can also change values after the command file is set:

### **Command Options Format Example**

[*\> jtharness*](aboutExamples.html) `-open` *myconfig*`.jti @`*mycommandfile*`.jtb -excludeList`
*myexcludelist*`.jtx -runtests`

### **Single String Arguments Format Example**

[*\> jtharness*](aboutExamples.html) `"open` *myconfig*`.jti; @`*mycommandfile*`.jtb; excludeList`
*myexcludelist*`.jtx; runtests"`

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2004, 2011, Oracle and/or its affiliates. All rights reserved.
