---
hIndent: 0
title: Configuring a Test Run
---

[]{#confEdit.overview}

# Configuring a Test Run

Before the harness can execute the tests in a test suite, it requires information about how your
computing environment is configured. You provide the harness with this information by loading a
template (`.jtm`), loading an existing configuration file (`.jti`), creating a new configuration, or
changing the values in a configuration.

![The following text is a note](../../images/hg_note.gif){longdesc="overview.html"}\
You can also specify configuration values from the command line when starting the harness GUI.

The quantity and scope of information required to run tests depends on the test suite. Some test
suites run in diverse environments (different platforms and networks), while others run in very
specific, well-defined environments. The test suite may provide a configuration interview or a
template for you to use in creating a configuration for your test run. If your test suite does not
provide any of these, consult the test suite documentation for directions about how you can supply
the required configuration information.

This chapter contains the following topics:

-   [Editing a Configuration](editConfiguration.html)- Describes how to use the Question Mode edit a
    configuration.
-   [Editing Quick Set Values](editQuickSet.html)- Describes how to use the Quick Set Mode to edit
    the runtime values in a configuration file.
-   [Creating a Configuration](createConfiguration.html)- Describes how to create a configuration
    for use in running tests.
-   [Saving a Configuration](saveConfiguration.html) - Describes how to save the current
    configuration.
-   [Loading a Configuration](loadConfiguration.html) - Describes how to load an existing
    configuration or a template for use in running tests.
-   [Using Bookmarks in Configurations](setMarkers.html) - Describes how to use bookmarks to
    simplify a configuration interview.
-   [Searching a Configuration](searchConfiguration.html) - Describes how to search a configuration
    for characters or values.
-   [Working with Multiple Configurations](multiple.html) - Describes how to use multiple
    configuration files to switch configurations between test runs.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2011, Oracle and/or its affiliates. All rights reserved.
