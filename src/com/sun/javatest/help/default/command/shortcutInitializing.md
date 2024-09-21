---
hIndent: 2
title: Shortcuts to Initialize a Configuration
---

# [Shortcuts to Initialize a Configuration]{#restrictions}

The following apply to the test suite, work directory, and `.jti` file specified in a command:

-   If you specify an existing work directory, you are not required to specify a test suite.
-   If you specify an existing `.jti` file, you are not required to specify a test suite.
-   If you specify an existing `.jti` file, you are not required to specify a work directory unless
    you want to use a work directory different from that specified in the `.jti` file.

You can include commands as any combination of options, single string arguments, or files on the
command line. However, because commands are executed in the sequence that they appear in the command
string, *if specified, the commands must occur in the following sequence* :

-   Test suites must precede the work directory or `.jti` file.
-   The work directory and `.jti` file must match the test suite.
-   The work directory must precede any standard values.
-   The `.jti` file must precede any changes to the current configuration.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2004, 2009, Oracle and/or its affiliates. All rights reserved.
