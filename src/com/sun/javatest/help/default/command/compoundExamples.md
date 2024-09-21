---
hIndent: 1
title: Extended Command-Line Examples
---

# Extended Command-Line Examples

This section provides extended examples of command-line operations.

To use the following examples on your system, you must use classpaths and directory names
appropriate for your system.

## Example 1

`java -jar lib/javatest.jar -verbose -testSuite /tmp/`*myts*` \`\
`-workdir -create /tmp/`*myworkdir* `-config /tmp/`*my*`.jti \`\
`-runtests -writereport /tmp/report`

This combination of commands does the following, in this order:

1.  Tells the harness to be verbose during test execution.
2.  Opens the test suite `/tmp/`*myts*.
3.  Creates a work directory named `/tmp/`*myworkdir*.
4.  Uses *my*`.jti` as the configuration settings.
5.  Executes the tests (as specified by the configuration).
6.  Writes a report to `/tmp/report/` after test execution.

## Example 2

`java -jar lib/javatest.jar -startHttp -testsuite /tmp/`*myts*` \`\
`-workdirectory /tmp/`*myworkdir*` -config /tmp/`*my*`.jti \`\
`-runtests -writereport /tmp/report -set` *foo.bar* `4096 \`\
`-runtests -writereport /tmp/`*report1*

This combination of commands does the following, in this order:

1.  Tells the harness to start the internal HTTP server.
2.  Opens the test suite `/tmp/`*myts*.
3.  Uses a work directory named `/tmp/`*myworkdir*.
4.  Uses *my*`.jti` as the configuration settings.
5.  Executes the tests (as specified by the configuration).
6.  Writes a report to `/tmp/report/` after test execution.
7.  Changes a configuration value (not written to JTI file).
8.  Runs tests again.
9.  Writes a new report in `/tmp/`*report1*.

## Example 3

`java -cp lib/javatest.jar:lib/comm.jar \`\
`com.sun.javatest.tool.Main \`\
`-Especial.value=lib/special.txt \`\
`-agentPoolPort 1944 -startAgentPool "testsuite /tmp/`*myts*` ; \`\
`workdir /tmp/`*myworkdir* `; config` *myconfig*`.jti ; runtests"`

This combination mixes two styles of command line arguments (quoted and dash-style). It invokes the
harness by class name, rather than executing the Java Archive (JAR) file (`-jar`). An extra item is
added to the VM\'s classpath. The following commands are given to the harness:

1.  Sets a particular value in the testing environment.
2.  Specifies the agent pool port and starts the agent pool.
3.  Loads the test suite `/tmp/`*myts*.
4.  Opens the work directory `/tmp/`*myworkdir*.
5.  Uses the configuration in *myconfig*`.jti`.
6.  Runs the tests.

## Example 4

`java -jar lib/javatest.jar -config` *foo*`.jti -runtests`

This command example relies on information in the JTI file to perform the run. Specifically, it
tries to use the work directory and test suite locations specified in the JTI file. If either of
those are invalid or missing, the harness reports an error. Otherwise, if the configuration in the
JTI is complete, the tests are run.

## Example 5

`java -jar lib/javatest.jar -config` *foo*`.jti -verbose \`\
`-set test.val1 2007 -runtests`

This is the same as Example 4 with the exception that it turns on verbose mode and changes the
answer of one of the questions in the configuration.

## Example 6

`java -jar lib/javatest.jar -config` *foo*`.jti \`\
`-priorStatus fail,error -timeoutFactor 0.1 \`\
`-set test.needColor Yes \`\
`-set test.color1 orange -tests api/java_util -runtests`

This example extends Example 4 by setting various Standard Values and the answer to particular
configuration questions.

## Example 7

`java -jar lib/javatest.jar -testsuite /tmp/`*foo*`.jti` *myts*` \`\
`-workdirectory /tmp/`*mywd* `-config /tmp/`*myconfig*`.jti`

This example stars the GUI. This combination of commands does the following, in this order:

1.  Opens the specified test suite.
2.  Opens the work directory given (assuming it is a work directory).
3.  Opens the configuration file given.
4.  Starts the GUI since no execution action is given.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2004, 2009, Oracle and/or its affiliates. All rights reserved.
