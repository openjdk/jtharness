---
hIndent: 3
title: Changing Test Suites or Creating a New Interview
---

# [Changing Test Suites or Creating a New Interview]{#change}

The following example uses the `-ts` option to create an empty interview derived from the test suite
( *mytestsuite*`.ts`). Use the `-ts` option only for very simple test suites.

Example:\
`java -cp` \[*jt_dir* `/lib/`\] `javatest.jar com.sun.javatest.EditJTI -o` *mynew*`.jti -l`
*myeditlog*`.html -ts` *mytestsuite*`.ts "|/java/jdk/1.3/|/java/jck/1.4/|"` *myoriginal*`.jti`

If a change is made that is not in the current interview path, the interview will be invalid and the
tests cannot be run.

Do not use `EditJTI` to change the interview path, but only the values on the existing path. If you
are in doubt about the current interview path, open the configuration editor window in the harness
GUI and use it to change the values. The configuration editor window displays the current interview
path for that question name-value pair.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2003, 2009, Oracle and/or its affiliates. All rights reserved.
