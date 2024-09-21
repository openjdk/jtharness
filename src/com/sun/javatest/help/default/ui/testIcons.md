---
hIndent: 4
title: Test Icons
---

[]{#testIcons}

# Test Icons

The test tree uses colored icons to indicate both run status and result status. The colors of the
icons shown below are the default settings. The harness enables you to use colors other than the
default settings. See [Specifying Status Colors](../command/settingColors.html) in the *Command-Line
Interface* *User\'s Guide*.

[]{#testIcons.runstatus}

## Test Run Status

The Test Manager displays an arrow to the left of the test icon when running a test. The standard
values of the current configuration, not the view filter, determine the run status displayed by the
test tree.

[]{#testIcons.Testresultstatus}

## Test Result Status

The test icon indicates the last known test result and does not change until the test is completed.
After the harness completes the test, it displays the appropriate result status icon.

The test result icons displayed in the test tree are determined by the results in the work directory
and by the view filter set in the Test Manager. See [View Filters](../browse/viewFilters.html) for a
description of how to specify the test results displayed in the test tree. The following table
describes the test icons used to indicate the result of each test.

![The following text is a note](../../images/hg_note.gif){longdesc="foldericons.html"}\
Users can change the color of the test icons displayed in the following table. However, the symbols
displayed in the icons do not change.

  Icon                                                                         Result         Description
  ---------------------------------------------------------------------------- -------------- ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  ![Test Error](../../images/blueTest.gif){longdesc="testIcons.html"}          Error          A blue test containing an exclamation symbol ( **!**) indicates that the test is not filtered out and that harness could not execute it. These errors usually occur because the test environment is not properly configured.
  ![Test Failed](../../images/redTest.gif){longdesc="testIcons.html"}          Failed         A red test containing a **x** symbol indicates that the test is not filtered out and had a Failed result the last time it was executed.
  ![Test Not Run](../../images/whiteTest.gif){longdesc="testIcons.html"}       Not Run        A white test containing a **-** symbol indicates that the test is not filtered out but has not yet been executed.
  ![Test Passed](../../images/greenTest.gif){longdesc="testIcons.html"}        Passed         A green test containing a ![Symbol used in the passed icon](../../images/checkMark.gif){longdesc="testIcons.html"} symbol indicates that the test is not filtered out and had a Passed result the last time it was executed.
  ![Test Filtered Out](../../images/grayTest.gif){longdesc="testIcons.html"}   Filtered Out   A gray test containing no symbols indicates that the test is currently not selected to be run.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2011, Oracle and/or its affiliates. All rights reserved.
