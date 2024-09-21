---
hIndent: 1
title: Setting Specific Values
---

[]{#otherConfigValues}

# Setting Specific Values

After you set a configuration (see [Initial Set-up Commands](basicContext.html)), you can specify
individual values for a test run that override those in the configuration file.

![The following text is a note](../../images/hg_note.gif){longdesc="otherConfigValues.html"}\
Values that you specify in the command string override but do not change the values specified in the
configuration file.

You can use the following commands to specify individual values for a test run:

-   **`set`** - Sets any value in a configuration file. See [Setting Test Suite Specific Values With
    `set`](settingValues.html) for a detailed description of this command.
-   **`concurrency`** - Changes the concurrency value set in the configuration file. See [Setting
    Concurrency With `concurrency`](concurrency.html) for a detailed description of this command.
-   **`excludeList`** - Specifies or changes the exclude list set in the configuration file. See
    [Specifying Exclude List Files With `excludeList`](excludeList.html) for a detailed description
    of this command.
-   **`keywords`** - Specifies or changes the keyword values set in the configuration file. See
    [Specifying Keywords With `keywords`](keyword.html) for a detailed description of this command.
-   **`kfl`** - Specifies one or more known failure lists (KFLs), as reflected in the known failure
    analysis (part of the HTML [report type](writeReports.html#writeReportType)). See [Specifying
    Keywords with `kfl`](knownFailureAnalysis.html).
-   **`priorStatus`** - Specifies or changes prior status values set in the configuration file. See
    [Selecting Tests With `priorStatus`](prior.html) for a detailed description of this command.
-   **`tests`** - Specifies or changes the tests specified in the configuration file. See
    [Specifying Tests or Directories With `tests`](selectingTests.html) for a detailed description
    of this command.
-   **`timeoutFactor`** - Specifies or changes the test timeout value specified in the configuration
    file. See [Setting Timeout With `timeoutFactor`](timeout.html) for a detailed description of
    this command.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2011, Oracle and/or its affiliates. All rights reserved.
