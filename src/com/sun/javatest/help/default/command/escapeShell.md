---
hIndent: 3
title: Doing Escapes in a UNIX Shell
---

# [Doing Escapes in a UNIX System Shell]{#example3}

The following example uses the syntax for doing escapes in a UNIX system shell. Changes to the
original configuration file (*myoriginal*`.jti` in this example) are written to a new configuration
file (*my-newconfig*`.jti` in this example).

In the following example, *myoriginal*`.jti` represents a configuration file name that might exist
on your system. Win32 users must also replace the UNIX system file separators (\\) with Windows file
separators (/) to run these examples.

To change a value in the command line, use the *tag-name* for the question that sets the value. See
[Obtaining the Question tag-name](tagName.html) for detailed information about viewing the
*tag-name* for the question.

`java -cp` \[*jt-dir*`/lib/`\] `javatest.jar com.sun.javatest.EditJTI`\
`-o`
*my-newconfig*`.jti test.serialport.midPort=/dev/term/a test.connection.httpsCert="\"CN=<Somebody>, OU=<People>, O=<Organisation>, L=<Location>, ST=<State>, C=US\""`*myoriginal*`.jti`

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2003, 2011, Oracle and/or its affiliates. All rights reserved.
