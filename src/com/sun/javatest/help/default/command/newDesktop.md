---
hIndent: 1
title: Desktop Startup Commands
---

# [Startup Commands]{#newdesktop}

When starting the harness, include `-newDesktop` in the command string to start the harness GUI
without using a previous desktop. The harness ignores any previous desktop information and opens the
Quick Start wizard.

**Note: ** Some test suites do not implement the optional Quick Start wizard. It won\'t be available
if the test suite architect disabled it.

![The following text is a note](../../images/hg_note.gif){longdesc="newDesktop.html"}\
The harness uses a new desktop when you include GUI options in the command line. Using this option
preserves any preferences set for the desktop. Use the following example to start the harness with a
new Desktop.

[*\> jtharness*](aboutExamples.html) `-newDesktop`

See [About the Command-Line Examples](aboutExamples.html) for a description of the use of *\>
jtharness*.

# [Restore Tools State]{#resume}

Specify `-resume` to restore the last-saved tools state. Tools settings will be restored, even if
the preference [Check Restore Tools State on
Start](../ui/appearancePrefs.html#appearancePrefs.restore) was disabled from the user interface.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2004, 2011, Oracle and/or its affiliates. All rights reserved.
