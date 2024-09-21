---
hIndent: 3
title: Showing Paths for Debugging
---

# [Showing Paths for Debugging]{#show}

The `-p` option can be used to show the path during debugging. Using `-p` options in the command
string displays how the path is changed by your edit.

Example:\
`java -cp` \[*jt_dir*`/lib/`\] `javatest.jar com.sun.javatest.EditJTI -n -o mynew.jti -l`
*myeditlog*`.html -p "|/java/jdk/1.3/|/java/jck/1.4/|"`*myoriginal*`.jti`

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2003, 2009, Oracle and/or its affiliates. All rights reserved.
