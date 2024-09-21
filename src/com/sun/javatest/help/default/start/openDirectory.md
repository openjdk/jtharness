---
hIndent: 1
title: Opening a Work Directory
---

# Opening a Work Directory

Each work directory is associated with a specific test suite, a configuration, and possibly a
template. Each time the harness runs tests, it creates test result files that store the information
collected during the test run. The harness stores these files in the work directory for all test
runs of the test suite with that configuration.

You can open a work directory from either the Quick Start wizard (when resuming work on a test run)
or the Test Manager.

![The following text is a note](../../images/hg_note.gif){longdesc="changePort.html"}\
Use File \> Recent Work Directory to quickly open a directory you have used before.

## Open a Work Directory with the Quick Start Wizard {#open-a-work-directory-with-the-quick-start-wizard .proc}

1.  Click the Browse button in the Quick Start wizard Work Directory panel.\
    \
    The harness displays a Work Directory chooser.

<!-- -->

2.  Use the chooser to navigate to the location of the work directory.
3.  Click on the work directory icon or enter the name of the work directory in the text field.
4.  Click the Open button.
5.  Complete the Quick Start wizard.

When you complete the Quick Start wizard, the harness opens a Test Manager window containing the
test results for all test runs of the associated test suite and configuration.

## Open a Work Directory with the Test Manager {#open-a-work-directory-with-the-test-manager .proc}

1.  Choose File **\>** Open Work Directory from the Test Manager menu bar.\
    \
    The harness displays the Open Work Directory dialog box.

<!-- -->

2.  Use dialog box to navigate to the location of the work directory.
3.  Enter the name of the work directory in the Path field or click its name in the tree.
4.  Click the Open button.\
    \
    The harness performs one of the following actions:

-   Displays the test results from the work directory for all test runs of the current test suite in
    the Test Manager *if* both of the following conditions are true:
    -   The current test suite and configuration are associated with the work directory.
    -   The current test suite is not associated with any other open work directory.
-   Opens a new Test Manager window for the work directory, test suite, and configuration if the
    work directory is not associated with the current test suite in the Test Manager.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2011, Oracle and/or its affiliates. All rights reserved.
