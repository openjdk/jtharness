---
hIndent: 2
title: Resolving Configuration-Template Conflicts
---

[]{#resolveConflict}

# Resolving Configuration-Template Conflicts

When the harness detects a conflict between a configuration and its template, it displays a Template
update conflict dialog box.

![Template conflict dialog
box](../../images/JT4templateconflict.gif){longdesc="resolveConflicts.html"}

By default, any change to a template is considered a Conflict and is listed in the Conflicts tab.
Configuration users get to choose whether to accept the new template value, reject the value, or
postpone deciding.

A test suite can also declare a template question to be auto-updating. If a user changes an
auto-updating value in the template, the harness replaces the corresponding property in the attached
configuration. The updated value is listed on the Updates tab.

![Template update dialog box](../../images/JT4templateupdates.gif){longdesc="resolveConflicts.html"}

A user cannot refuse to accept an auto-updating value.

Choose a dialog button based on the following guidelines:

-   **Change Now** - Click this button if you want the new template values to overwrite the
    corresponding configuration values.
-   **Don\'t Change** - Click this button if you want the new template values to be ignored.
-   **Remind Me Later** - Click this button if you want to defer the conflict resolution until the
    next time the harness checks for updates.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2006, 2009, Oracle and/or its affiliates. All rights reserved.
