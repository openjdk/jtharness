---
hIndent: 0
title: Working With Templates
---

[]{#createConfigurationTemplate}

# Working With Templates

The harness enables test suites to provide users with the capability of using templates to create
configurations required to run tests on multiple test systems or test platforms.

For example, if your test suite enables using templates and your test group uses a central location
to provide and manage the resources required to run tests (the test suite, report directories,
configuration files, or the harness), a site administrator or user could provide templates
containing the known configuration values required by the test group to run tests. A user could use
that template to quickly create a configuration for their test run by including the values unique to
their own test environment.

In addition to providing users with partially completed configurations, templates can further
simplify the process of creating and maintaining configurations through the use of the following
features:

-   Bookmarks
-   Propagation

A feature of templates is the ability to propagate updates of a template to all configurations based
on that template. When template propagation is enabled, the harness checks for template updates when
a user does any of the following:

-   Starts a test run.
-   Opens a work directory.
-   Loads a configuration.

[]{#conflictDef}

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2006, 2009, Oracle and/or its affiliates. All rights reserved.
