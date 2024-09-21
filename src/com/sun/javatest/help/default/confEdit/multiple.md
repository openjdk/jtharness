---
hIndent: 1
title: Working with Multiple Configurations
---

[]{#multiple}

# Working with Multiple Configurations

In some testing situations it is useful to use separate configuration files to switch between
different configurations for different test runs. For example, one configuration could select
automated tests and another could select interactive tests.

Prior to running tests, use the Configure menu to load the required configuration file. See [Loading
a Configuration](loadConfiguration.html) for detailed information. These configuration files can be
loaded from a central resource provided by your test group. See your test group for the name and
location of the configuration files.

If your group does not provide an existing set of configuration files, you can create them by using
the Configuration Editor to edit an existing configuration and then save each variation to a file
name of your choosing. See [Editing a Configuration](editConfiguration.html) for detailed
information.

You can save these configuration files anywhere in your file system. Generally, however, the work
directory should not be used to save configuration files. Clearing the work directory would delete
the configuration file.

After these configuration files are created, they can be used by your test group to run tests.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2009, Oracle and/or its affiliates. All rights reserved.
