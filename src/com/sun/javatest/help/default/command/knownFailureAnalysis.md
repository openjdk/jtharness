---
hIndent: 1
title: Specifying Known Failures Lists With kfl
---

[]{#writeReportsKFL} []{#kfl}

# Specifying Known Failures Lists With `kfl`

The ability to specify a known failures list is enabled in the configuration editor interview, as
described in [Creating Reports](../../default/report/newReports.html#specKFL) in the *Graphical User
Interface User\'s Guide*. In the user interface, if the value for \"Specify a Known Failures List?\"
is Yes and you have specified KFLs, they become the default values, and are used when you create
reports using Reports \> Create New Report or using the [`-writeReports`](writeReports.html) HTML
report type.

The `-kfl` option enables you to change the default list of KFL files from the command line.

You can call the KFL file(s) as follows. Multiple files are separated by spaces:

    java -jar ... -kfl foo.kfl bar.kfl path/foobar.kfl -runtests

![The following text is a note](../../images/hg_note.gif){longdesc="otherConfigValues.html"}\
Note, because a space is a separator, file path arguments cannot contain spaces (for example,
`C:\Program Files\myconfig\foobar` will not work).

See [Command-Line Overview](commandLine.html) for a detailed description of the command line
structure. See [Formatting a Command](formatCommands.html) for descriptions and examples of other
command formats that you can use.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2011, Oracle and/or its affiliates. All rights reserved.
