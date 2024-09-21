---
hIndent: 1
title: Editing a Template
---

[]{#editingTemplate}

# Editing a Template

The Template Editor provides the following two modes of displaying and editing the questions and
values in a template.\
\
See [Question Mode](questionModeTE.html) for detailed information about using the features of this
editing mode.

-   Question Mode - Enables editing of all template questions.

<!-- -->

-   Quick Set Mode - Enables rapid editing of a limited set of template questions.\
    \
    Quick Set Mode filters out all test suite specific questions and displays a limited set of
    general questions whose values are often changed from test run to test run. In this editing
    mode, the Template Editor creates logical groupings of the values and displays them in tab form
    for easy user navigation and access.

## Editing in Question Mode {#editing-in-question-mode .proc}

![This is the start of a procedure](../../images/hg_proc.gif){longdesc="editTemplate.html"}To edit
all template questions and bookmarks in a template, perform the following steps:

1.  If you are editing the current template (named at the bottom of the Test Manager) choose
    Configure \> Edit Template in the Test Manager menu bar, otherwise choose either Configure \>
    Load Template or Configure \> Load Recent Template.\
    \
    If you are editing the current template (named at the bottom of the Test Manager), the harness
    opens the Template Editor and loads the template. Go to step 3.\
    \
    If you are editing a different template, the harness displays the Load Template dialog box.

<!-- -->

2.  Use the dialog box to locate and load the template.\
    \
    The harness opens the Template Editor and displays the template in Question Mode.

<!-- -->

3.  Use Question Mode to change the values of questions in the template.
4.  Click Done to save the changes and close the Template Editor.

You can use the View menu at any time to change from one mode to the next. Because there is only one
template file loaded in the Template Editor at a time, when you change modes during an editing
session, any changes made in one mode are automatically reflected in the other.

If a configuration is attached to the edited template, the harness opens a Template updated dialog
box that enables you to review the changes and to accept, reject, or postpone having the changes
propagated to the attached configuration. See [Resolving Configuration-Template
Conflicts](../templates/resolveConflicts.html) for a description of the dialog box and the choices
available to the user.

## Editing in Quick Set Mode {#editing-in-quick-set-mode .proc}

![This is the start of a procedure](../../images/hg_proc.gif){longdesc="editTemplate.html"}To edit
one or more of the quick set values in a template, perform the following steps:

1.  If you are editing the current template (named at the bottom of the Test Manager) choose
    Configure \> Edit Template in the Test Manager menu bar, otherwise choose either Configure \>
    Load Template or Configure \> Load Recent Template.\
    \
    If you are editing the current template (named at the bottom of the Test Manager), the harness
    opens the Template Editor and loads the template. Go to step 3.\
    \
    If you are editing a different template, the harness displays the Load Template dialog box.

<!-- -->

2.  Use the dialog box to locate and load the template.\
    \
    The harness opens the Template Editor and displays the template in Question Mode.

<!-- -->

3.  Choose View \> Quick Set Mode in the Template Editor menu bar.\
    \
    The harness displays the Template Editor in Quick Set Mode.

<!-- -->

4.  Set the quick set values by using the following tabs:

-   [Tests](testsTabTE.html) - Use this tab to edit the default set of tests that a user can run in
    this configuration.
-   [Exclude List](excludeTabTE.html) - (Optional) Use this tab to edit the default list of tests
    that are excluded from a test run using this configuration.
-   [Keywords](keywordsTabTE.html) - (Optional) Use this tab to edit the default keywords and
    expressions used in this configuration to run tests.
-   [Prior Status](statusTabTE.html) - Use this tab to edit the default restrictions on tests that
    are run by using their status from the previous test run.
-   [Execution](execTabTE.html) - (Optional) Use this tab to edit the default concurrency and
    timeout factor values used in the configuration for running tests.

5.  Click Done to save the changes and close the Template Editor.

You can use the View menu at any time to change from one mode to the next. Because there is only one
template file loaded in the Template Editor at a time, when you change modes during an editing
session, any changes made in one mode are automatically reflected in the other.

If a configuration is attached to the edited template, the harness opens a Template updated dialog
box that enables you to review the changes and to accept, reject, or postpone having the changes
propagated to the attached configuration. See [Resolving Configuration-Template
Conflicts](../templates/resolveConflicts.html) for a description of the dialog box and the choices
available to the user.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2006, 2011, Oracle and/or its affiliates. All rights reserved.
