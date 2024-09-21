---
hIndent: 1
title: Displaying Folder Information
---

[]{#folderInfo}

# Displaying Folder Information

The folder view contains filtered summary and status information about the tests in a test folder.
This information is displayed both as values and as a pie chart. The folder view and test tree use
the same view filter when displaying information.

To display the folder view, click a folder icon in the test tree. The folder view displays a Summary
tab, a Documentation tab, five status tabs, and a status display.

![Information pane, folder view](../../images/JT4infoArea.gif){border="0"
longdesc="folderInfo.html"}

During a test run, you can use the folder view to monitor the status of a folder and its tests. You
can also use the folder view during troubleshooting to quickly locate and open individual tests that
had errors or failed during the test run. When a status pane is empty, the harness disables its tab.

See [Summary Information](summaryTab.html) for a description of the information displayed in the
Summary pane.

The Documentation tab displays test suite documentation, assuming the test suite developer has
provided it. In the test tree, select a test (not a folder) to display its description, if any, in
the Documentation tab.

See [Status Information](statusTabs.html) for a description of the folder information displayed by
clicking the following status tabs:

-   ![Passed folder](../../images/greenTest.gif){longdesc="folderInfo.html"}Passed (green with a
    check mark)
-   ![Failed folder](../../images/redTest.gif){longdesc="folderInfo.html"}Failed (red with an x)
-   ![Error folder](../../images/blueTest.gif){longdesc="folderInfo.html"}Error (blue with an
    exclamation point)
-   ![Not Run folder](../../images/whiteTest.gif){longdesc="folderInfo.html"}Not Run (white with a
    dash)
-   ![Filtered Out folder](../../images/grayTest.gif){longdesc="folderInfo.html"}Filtered Out (gray
    or shaded)

The status message displayed at the bottom of the pane provides information about the selected tab.
The messages indicate that tests in the folder are loading or provide detailed status information
about a selected test.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2011, Oracle and/or its affiliates. All rights reserved.
