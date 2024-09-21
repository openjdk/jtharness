---
hIndent: 2
title: Monitoring With a Test Tree
---

# Monitoring With a Test Tree

The test tree uses folder icons, test icons, and two independent types of filtering (run filtering
and view filtering) to simultaneously display the following conditions:

-   Progress of a test run
-   Current test results in the work directory

When a test run begins, you can track its progress in the test tree by observing the folder and test
icons. The test tree displays an arrow at the left of each folder and test icon as it is loaded and
run.

After the harness completes running a test, it writes the test results to the work directory and
updates the folder and test icons in the test tree. The test tree displays folder and test icons
based on the view filter specified in the Test Manager window.

The harness supports using status colors specified by the user instead of the default color
settings. See [Specifying Status Colors](../command/settingColors.html) in the *Command-Line
Interface User\'s Guide*.

Regardless of whether or not a test was run, the test tree displays filtered out ![Filtered out
folder](../../images/grayFolder.gif){longdesc="../browse/folderIcons.html"} folder and ![Test
Filtered Out](../../images/grayTest.gif){longdesc="../browse/testIcons.html"} test icons for those
tests and folders filtered out by the view filter. All other icons are updated to reflect their
current result status from the work directory.

Changing either the run or the view filter settings causes the harness to immediately update the
folder and test icons displayed in the test tree.

See [View Filters](../browse/viewFilters.html) for a description of how to specify which test
results from the work directory are displayed in the test tree.

See [Test Tree](../ui/usingtree.html) for a detailed description of the icons, filters, and other
features used in the test tree.

The goal of a test run, when using the appropriate view filter, is for the root test suite folder to
display the passed ![Passed
folder](../../images/greenFolder.gif){longdesc="../browse/folderIcons.html"} folder icon. The passed
root test suite folder icon signifies that all tests in the test suite not filtered out of the test
run (by specifying tests that are run, exclude lists, keywords, and prior status) with passed test
results. See [Changing QuickSet Values](../confEdit/changeQuickSet.html) for a description how to
quickly set run filters that include or remove tests from a test run.

Click the test suite icon in the test tree to display status information for the test suite in the
Test Manager information area. The view filter used in the test tree is also used to display folder
status information. See[Displaying Folder Information](../browse/folderInfo.html) for a detailed
description of the folder information displayed in this area.

By browsing the tabbed pane and the test tree, you can find the folders that contain tests without
passed test results.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2011, Oracle and/or its affiliates. All rights reserved.
