---
hIndent: 1
title: Creating a Map File
---

[]{#mapFile}

# Creating a Map File

Some tests require contextual information, such as the host name on which they are executed, before
they can run. Because network file systems might be mounted differently on different systems, the
path names used by the JT Harness harness might not be the same for the agent. The agent uses a map
file to translate these strings into values it can use to run tests.

1.  Use a text editor to open a simple ASCII file and enter the following types of lines:
    -   **Comment line** : Begins with the \# symbol and provides information that is not processed
        by the agent. Comment lines are optional.

        Example:

        `#Replace all /home/user1 with /user1`

    -   **Translation line** : Contains the target and substitution strings. Enter the string that
        is to be replaced followed by one or more spaces and the replacement string. The agent
        replaces all occurrences of the first string with the second.

        Example:

        `/home/user1/user1`

        Because the agent uses the map file to perform global string substitution on *all* matching
        values received from the JT Harness harness, you must be as specific as possible when
        specifying strings in a translation line.

        Refer to [Troubleshooting JT Harness harness agents](troubleshooting.html) for additional
        information about determining the substitution strings required in a map file.
2.  Save the map file in the test suite root directory.

> You can use any name and extension. If you are unable to use the root directory, you can use any
> writable directory on the test system. When starting an agent you must specify which map file, if
> any, to use.

Example of a map file:

` #This is a sample map file`\
`#Replace all /home/user1 with /user1 `

` /home/user1 /user1 `

` #Replace all /home/user2/JT Harness with /myhome/JT Harness`\
`/home/user2/JT Harness /myhome/JT Harness `

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2011, Oracle and/or its affiliates. All rights reserved.
