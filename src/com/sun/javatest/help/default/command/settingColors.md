---
hIndent: 1
title: Specifying Look and Feel
---

[]{#settingLAF}

# Specifying Look and Feel

The `-laf` option selects the look and feel for a JavaTest session. This option affects the GUI
only. It has no effect if the harness is used solely as a command line tool.

The look and feel option `-laf` requires one of the following arguments:

-   `default`: Use the harness default. The Java look and feel options are `nimbus` or `metal`.
    Images in the help and documentation depict these styles.
-   `nimbus`: The modern Java look and feel. This is the harness default if it is available in the
    runtime.
    <http://download.oracle.com/javase/6/docs/technotes/guides/jweb/otherFeatures/nimbus_laf.html>
-   `metal`: The legacy Java look and feel used through version 4.4 of the harness.
    <http://download.oracle.com/javase/6/docs/api/javax/swing/plaf/metal/MetalLookAndFeel.html>
    []{#a11y}
-   `sys` or `system`: Use the system-default look and feel that matches your desktop style
    (`nimbus` or `metal`). To achieve the best results from some features of your operating system,
    such as high contrast color schemes, large fonts and other accessiblity related functions, it
    might be necessary to select the `system` look and feel.

A sample command would be:

[\> *jtharness* `-laf metal`]{.indent2}

To use the native look and feel specify the `system` option. For example:

[\> `java -jar javatest.jar -laf system ...`]{.indent2}

[]{#settingColors}

# Specifying Status Colors

The harness enables you to specify the status colors used in the GUI. This property is set on the
command line as a system property when starting the harness GUI. Status colors set this way are
added to the user preferences and restored in subsequent sessions.

The user can specify each status color by declaring system properties in the following format:

`-Djavatest.color.passed=`*color-value* \...

`-Djavatest.color.failed=`*color-value* \...

`-Djavatest.color.error=`*color-value* \...

`-Djavatest.color.notrun=`*color-value* \...

`-Djavatest.color.filter=`*color-value* \...

The *color-value* used must be an RGB value parsable by the `java.awt.Color` class (octal, decimal,
or hex).

The value portion of the color property must be explicitly defined. The value portion of the
property accepts hex values, prefixed by either a pound character (`#`) or a zero-x (`0x`).

Values can also be specified in octal, in which case the value begins with a leading zero and must
be two or more digits.

The following are possible formats for setting color integers:\
`#ffaa66` (hex)\
`0xffaa66` (hex)\
`0111177` (octal)

This is a detailed example of specifying a status color. You might have to escape the pound
character for the command to work on your platform.

\> `java -Djavatest.color.passed=“#00FF00” -jar [jt_dir/lib/]javatest.jar`

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2003, 2011, Oracle and/or its affiliates. All rights reserved.
