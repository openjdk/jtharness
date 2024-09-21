---
hIndent: 3
title: Changing Configuration Values
---

# [Changing Configuration Values]{#edit}

When using `EditJTI` to change the values in a configuration, you can use either of the following
command formats:

-   Use *tag=value* for direct replacement of values. You must know the *tag-name* for the question
    that sets the value.
-   Use */old pattern/new pattern/* to replace all occurrences (strings) of an old pattern to a new
    pattern. This format replaces all occurrences in the file.

When using the */old pattern/new pattern/* format, the separator can be any character. However, the
string should be enclosed in quotes to avoid shell problems.

`"|/java/jdk/1.3/|/java/jck/1.4/|"`

![The following text is a note](../../images/hg_note.gif){longdesc="editFile.html"}\
To run the following examples of changing configuration values, you must replace *myoriginal*`.jti`
with a `.jti` file name that exists on your system. Win32 users must also replace the / file
separators with \\ file separators to run these examples.

Example:\
`java -cp `\[*jt_dir*`/lib/`\]`javatest.jar com.sun.javatest.EditJTI -o`
*mynew*`.jti "|/java/jdk/1.3/|/java/jck/1.4/|"` *myoriginal*`.jti`

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2003, 2009, Oracle and/or its affiliates. All rights reserved.
