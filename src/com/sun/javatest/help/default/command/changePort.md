---
hIndent: 3
title: Changing the HTTP Port
---

# Changing the HTTP Port

You can use `EditJTI` to change the HTTP port and either overwrite the original configuration file
or create a new configuration file.

![The following text is a note](../../images/hg_note.gif){longdesc="changePort.html"}\
To run the following examples, you must use a `.jti` file that exists on your system and include
httpPort in your current interview path. If your current interview path does not include httpPort
you will not be able to change its value from the command line. To view the current interview path,
open your `.jti` file in the Configuration Editor. See [Obtaining the Question
tag-name](tagName.html) for detailed information about the *tag-name* for the question.

## [Change the HTTP Port and Overwrite Original Configuration File]{#example1}

The following example changes the HTTP port used when running tests and overwrites original
configuration file (*myoriginal*`.jti` in this example).

`java -cp` \[*jt-dir* `/lib/`\] `javatest.jar com.sun.javatest.EditJTI httpPort=8081`
*myoriginal*`.jti`

## [Change the HTTP Port and Create a New Configuration File]{#example2}

The following example changes the HTTP port used when running tests and writes the changed
configuration to a new configuration file (*myoutput*`.jti` in this example). The original
configuration file (*myoriginal*`.jti` in this example) remains unchanged.

`java -cp` \[*jt-dir* `/lib/`\] `javatest.jar com.sun.javatest.EditJTI -o`
*myoutput*`.jti httpPort=8081` *myoriginal*`.jti`

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2003, 2011, Oracle and/or its affiliates. All rights reserved.
