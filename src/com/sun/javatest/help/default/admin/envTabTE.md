---
hIndent: 2
title: Adding or Removing Test Environment Files
---

[]{#environmentTE}

# Adding or Removing Test Environment Files {#adding-or-removing-test-environment-files .proc}

![This is the start of a procedure](../../images/hg_proc.gif){longdesc="envTabTE.html"} To add or
remove the environment `(.jte`) files used to build the template`(.jtm`) file, perform the following
steps:

1.  Choose View \> Quick Set Mode from the Template Editor menu bar.

2.  Click the Test Environment tab in the Template Editor.

    ![The following text is a Note](../../images/hg_note.gif){longdesc="createTemplate.html"}\
    The Template Editor displays this tab only if your test suite used test environment (`.jte`)
    files to run tests.

    ![Environment files tab](../../images/env_confEditor.gif){longdesc="envTabTE.html"}

<!-- -->

3.  Click the appropriate button required to edit the list of environment (`.jte`) files.\
    \
    See [Test Environment Pane](#testEnvPane) for a description of the contents of this pane.

<!-- -->

4.  Change additional template settings or click the Done button to save the changes in the
    template.\
    \
    If you are creating a new template for these changes, instead of clicking the Done button,
    choose File **\>** Save As from the Template Editor menu bar and save the template with a
    relevant name.

[]{#testEnvPane}

## Test Environment Pane

The Test Environment pane contains following areas:

-   [Files Area](#files)
-   [Name Area](#environment)

### [Files]{#files} Area

Use the buttons described in the following table to create or modify a list of environment files
used to run tests in your computing environment.

  Button      Description
  ----------- ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  Add         Adds an environment file to the list. To select an environment file (`.jte`) for your test suite, click Add. As you make selections with the file chooser dialog box, they are added to the list. After you add an environment file, you can modify the list.
  Remove      Clears an environment file from the list. Select it in the list and click Remove.
  Move Up     Moves an environment file one position higher in the list. Select it in the list and click Move Up.
  Move Down   Moves an environment file one position lower in the list. Select it in the list and click Move Down.

### [Name]{#environment} Area

Click the drop-down arrow on the text field to see the list of test environments in the environment
files listed in the Env Files list. Click the Show button to view the contents of the selected test
environment.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2006, 2011, Oracle and/or its affiliates. All rights reserved.
