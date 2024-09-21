---
hIndent: 2
title: Editing Prior Status Settings
---

# Editing Prior Status Settings {#editing-prior-status-settings .proc}

![This is the start of a procedure](../../images/hg_proc.gif){longdesc="excludeTabTE.html"}To change
the prior status settings in the template, perform the following steps:

1.  Choose View \> Quick Set Mode from the Template Editor menu bar.
2.  Click the Prior Status tab in the Template Editor.

![The Prior Status pane from the Template
Editor](../../images/JT4priorstatusTabConfigEd.gif){border="0" longdesc="statusTabTE.html"}

 

3.  Change the prior status settings in the template as required.\
    \
    See [Prior Status Pane](#priorStatusPane) for a description of this pane.

<!-- -->

4.  Change additional template settings or click the Done button to save the changes in the
    template.\
    \
    If you are creating a new template for these changes, instead of clicking the Done button,
    choose File \> Save As from the Template Editor menu bar and save the template with a relevant
    name.

[]{#priorStatusPane}

## Prior Status Pane

By choosing Select tests that match, you can run tests with restrictions based on their result from
a prior test run. The following table describes the available Prior Status filter selections.

  Prior Status   Action
  -------------- -----------------------------------------------------------------------------------------------
  Passed         Selects tests that passed the last time the test was executed.
  Failed         Selects tests that failed the last time the test was executed.
  Error          Selects tests that the harness could not execute the last time it was included in a test run.
  Not Run        Selects tests without results in the current work directory.

Prior status is evaluated on a test-by-test basis using information stored in the result files
([`.jtr`]{#jtr}) that are written in the work directory. Unless overridden by a test suite, a result
file is written in the work directory for every test that is executed. When users change work
directories between test runs, the result files in the previous work directory are no longer used
and, if the new work directory is empty, the harness behaves as though each test in the test suite
was not run.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2006, 2011, Oracle and/or its affiliates. All rights reserved.
