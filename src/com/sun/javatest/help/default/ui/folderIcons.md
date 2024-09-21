---
hIndent: 4
title: Folder Icons
---

[]{#folderIcons}

# Folder Icons

The test tree uses colored icons to indicate both run status and result status. The colors of the
icons shown below are the default settings. The harness enables you to use colors other than the
default settings. See [Specifying Status Colors](../command/settingColors.html) in the *Command-Line
Interface User\'s Guide*.

[]{#folderIcons.runstatus}

## Folder Run Status

When activity occurs in a folder, such as loading or running tests, the Test Manager displays an
arrow to the left of the folder icon. The standard values of the current configuration, not the view
filter, determine the run status displayed by the test tree.

[]{#folderIcons.resultstatus}

## Folder Result Status

The folder icon indicates the current test results in the work directory and does not change until
its tests are completed. After the harness completes running the tests in a folder, it displays the
appropriate result status icon.

The folder icon displayed in the test tree is determined by the result of all its tests (see [Test
Icons](testIcons.html)) and the current view filter (see [View
Filters](../browse/viewFilters.html)). The folder icons displayed in the test tree indicate the
highest priority result of any test hierarchically beneath it. The following table describes the
folder icons in order of priority.

![The following text is a note](../../images/hg_note.gif){longdesc="foldericons.html"}\
Users can change the color of the folder icons displayed in the following table. However, the
symbols displayed in the icons do not change.

  Icon                                                                               Result         Description
  ---------------------------------------------------------------------------------- -------------- ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  ![Error folder](../../images/blueFolder.gif){longdesc="folderIcons.html"}          Error          A blue folder containing an exclamation symbol ( **!)** indicates that it and or one or more of its child folders contains tests with a result of Error. Note that this folder *might* also contain tests and folders that are Failed, Not Run, Passed, and Filtered out.
  ![Failed folder](../../images/redFolder.gif){longdesc="folderIcons.html"}          Failed         A red folder containing a **x** symbol indicates that it and-or one or more of its child folders contains tests with a result of Failed. Note that this folder *might* also contain tests and folders that are Not Run, Passed, and Filtered out.
  ![Not run folder](../../images/whiteFolder.gif){longdesc="folderIcons.html"}       Not Run        A white folder containing a **-** symbol indicates that it and-or one or more of its child folders contains tests with a result of Not Run. Note that this folder *might* also contain tests and folders that are Passed and Filtered out.
  ![Passed folder](../../images/greenFolder.gif){longdesc="folderIcons.html"}        Passed         A green folder containing a ![Symbol used in the passed folder](../../images/checkMark.gif){longdesc="folderIcons.html"} symbol indicates that it and all of its children have a result of Passed. Note that this folder *might* also contain tests and folders that are Filtered out.
  ![Filtered out folder](../../images/grayFolder.gif){longdesc="folderIcons.html"}   Filtered Out   A gray folder containing no symbols indicates that it and all of its children are filtered out.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2011, Oracle and/or its affiliates. All rights reserved.
