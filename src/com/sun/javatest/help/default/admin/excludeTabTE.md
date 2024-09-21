---
hIndent: 2
title: Adding or Removing Exclude Lists
---

[]{#excludeListTE}

# Adding or Removing Exclude Lists {#adding-or-removing-exclude-lists .proc}

![This is the start of a procedure](../../images/hg_proc.gif){longdesc="excludeTabTE.html"}To add or
remove exclude list files in the template, perform the following steps:

1.  Choose View \> Quick Set Mode from the Template Editor menu bar.
2.  Click the Exclude List tab in the Template Editor.

![The Exclude List pane in the Template
Editor](../../images/JT4excludelistTabConfigEd.gif){border="0" longdesc="excludeTabTE.html"}

3.  Add or remove exclude lists in the template as required.

> ![The following text is a Note](../../images/hg_note.gif){longdesc="excludeTabTE.html"}\
> Exclude list files contain a list of tests in a test suite that are not run by the harness. See
> [Exclude List Pane](#excludeListPane) for a description of this pane.

4.  Change additional template settings or click the Done button to save the changes in the
    template.\
    \
    If you are creating a new template for these changes, instead of clicking the Done button,
    choose File \> Save As from the Template Editor menu bar and save the template with a relevant
    name.

[]{#excludeListPane}

## Exclude List Pane

Use the following to change the exclude list used to run tests:

-   **None** - An Exclude List is not used.
-   **Initial** - Only enabled if the test suite provides an exclude list. If you choose Initial,
    the tests are run using the exclude list provided by the test suite.
-   **Latest** - Only enabled if the test suite provides a location for updated exclude lists. If
    you choose Latest, additional options are displayed. See [Latest Exclude
    List](#excludeList.latestTE) below for detailed information.
-   **Other** - A custom exclude list can be used. See [Other Exclude List](#excludeList.otherTE)
    below for detailed information.

If a complete test is added to the exclude list, the harness updates the test result status without
requiring the test be rerun. However, if only a test case from a test is added to the exclude list,
the harness requires that you rerun the test using the updated exclude list before updating the test
result status.

[]{#excludeList.latestTE}

## Latest Exclude List

If your test suite provides a URL for the latest test suite, the harness enables the Latest exclude
list option. The following table describes the text and controls displayed when you choose the
Latest exclude list option.

  Text and Controls                 Description
  --------------------------------- -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  Location                          Displays the location of the exclude list specified by the test suite. This is a non-editable field.
  Last updated                      Displays the date that the exclude list was last updated. This is a non-editable field.
  Check For Updates Automatically   Causes the harness to automatically check the location of the exclude list and compare the date-time stamps of the remote and local exclude lists. The harness then displays a dialog box advising you of the results. If a new exclude list is available, you can choose to download it.
  Every \_ Days                     Sets an interval for the harness to automatically check the remote location of the exclude list for updates.
  Every Test Run                    Causes the harness to check the remote location of the exclude list for updates before each test run.
  Check Now                         Causes the harness to check the remote location of the exclude list for an update.

[]{#excludeList.otherTE}

## Other Exclude List

The following table describes the buttons displayed when you choose the Other exclude list option.

  Button      Description
  ----------- ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  Add         Selects an exclude list file for your test suite. As you make selections with the file chooser dialog box, they are added to the list. After you add an exclude list, you can modify the list.
  Remove      Clears an item from the list. Select an item in the list and click Remove.
  Move Up     Moves an item one position higher in the list. Select an item in the list and click Move Up.
  Move Down   Moves an item one position lower in the list. Select an item in the list and click Move Down.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2006, 2011, Oracle and/or its affiliates. All rights reserved.
