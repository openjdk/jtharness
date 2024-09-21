---
hIndent: 1
title: Displaying Test Information
---

[]{#testInfo}

# Displaying Test Information

The test view contains unfiltered, detailed information from the work directory about a specified
test. To display the test view, click a test icon in the test tree or double-click a test name.

The test view contains six tabs and a colored status field (which indicates Pass or Fail).

![Test information area](../../images/JT4Tabbed.gif){longdesc="testInfo.html"}

![The following text is a note](../../images/hg_note.gif){longdesc="testInfo.html"}\
The test view does not use view filtering to display information. If you are using a view filter
other than All Tests, the status color displayed in this view might not match the test icon or the
folder view tab.

The following table describes the contents of the test view panes. The status field at the bottom of
the pane contains a description of the test result and is visible in all of the test view panes.

  Tab                                                Description
  -------------------------------------------------- -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  [Test Description Pane](testDescriptionTab.html)   Displays the name-value pairs contained in the test description. The contents are input data and always available.
  [Documentation Pane](testDescriptionTab.html)      Displays test documentation, if it exists.
  [Files Pane](filesTab.html)                        Contains a drop-down list of source files from the test description. Click a file name from the drop-down list to display its contents. The contents are input data and always available.
  [Configuration Pane](configurationTab.html)        Displays a table of configuration name-value pairs used to run a specific test. The contents are output data and only enabled if the test was run.
  [Test Run Details Pane](detailsTab.html)           Displays the name-value pairs that were recorded when the test was run. The contents are output data and only enabled if the test was run.
  [Test Run Messages Pane](messagesTab.html)         Contains a tree and message panel of output from sections of the test. Click a name to display its contents. The contents are output data and only enabled if the test was run.

When an information pane is empty, the harness disables it.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2011, Oracle and/or its affiliates. All rights reserved.
