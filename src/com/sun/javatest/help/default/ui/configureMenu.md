---
hIndent: 3
title: Configure Menu
---

[]{#configureMenu}

# Configure Menu

Use the Configure menu to load, create, modify, and view configuration data used for a test run. The
following table describes the default menu items in the Configure menu. Because this menu can be
customized you might see additional options defined in the current test suite.

The template menu options listed in the following table are only available when template usage is
enabled.

+-------------------------------------------------+-------------------------------------------------+
| Menu Item                                       | Description                                     |
+=================================================+=================================================+
| Edit Configuration                              | Opens the Configuration Editor and loads the    |
|                                                 | current configuration. The harness uses the     |
|                                                 | Configuration Editor to change configuration    |
|                                                 | values required to run a test suite.            |
|                                                 |                                                 |
|                                                 | See [Editing a                                  |
|                                                 | Con                                             |
|                                                 | figuration](../confEdit/editConfiguration.html) |
|                                                 | for detailed information.                       |
+-------------------------------------------------+-------------------------------------------------+
| Edit Template                                   | Opens the Template Editor and loads the         |
|                                                 | template on which the current configuration is  |
|                                                 | based. If the test suite disables the template  |
|                                                 | feature, this and all other template menu items |
|                                                 | are not displayed. This menu item is also       |
|                                                 | disabled when the test suite only enables users |
|                                                 | to load but not edit templates. Use the         |
|                                                 | Template Editor to change configuration values  |
|                                                 | in the template and to propagate the changes to |
|                                                 | the current configuration.                      |
|                                                 |                                                 |
|                                                 | See [Editing a                                  |
|                                                 | Template](../admin/editTemplate.html) for       |
|                                                 | detailed information.                           |
+-------------------------------------------------+-------------------------------------------------+
| Edit Quick Set                                  | Opens an additional menu containing Tests to    |
|                                                 | Run, Exclude List (optional), Keywords          |
|                                                 | (optional), Environment (optional), Prior       |
|                                                 | Status, Concurrency, and Timeout Factor menu    |
|                                                 | items that you can use to change specific       |
|                                                 | configuration values.                           |
|                                                 |                                                 |
|                                                 | See [Editing Quick Set                          |
|                                                 | Values](../confEdit/editQuickSet.html) for      |
|                                                 | detailed information.                           |
+-------------------------------------------------+-------------------------------------------------+
| New Configuration                               | Opens the Configuration Editor and loads an     |
|                                                 | empty configuration. The harness uses the       |
|                                                 | Configuration Editor to create configuration    |
|                                                 | data containing the test environment and        |
|                                                 | standard values required to run a test suite.   |
|                                                 |                                                 |
|                                                 | See [Creating a                                 |
|                                                 | Confi                                           |
|                                                 | guration](../confEdit/createConfiguration.html) |
|                                                 | for detailed information.                       |
+-------------------------------------------------+-------------------------------------------------+
| Load Configuration                              | Opens a Load Configuration File dialog box that |
|                                                 | you use to load an existing configuration into  |
|                                                 | the Test Manager.                               |
|                                                 |                                                 |
|                                                 | The harness does not open the Configuration     |
|                                                 | Editor when you load an existing, complete      |
|                                                 | configuration interview.                        |
|                                                 |                                                 |
|                                                 | See [Loading a                                  |
|                                                 | Con                                             |
|                                                 | figuration](../confEdit/loadConfiguration.html) |
|                                                 | for detailed information.                       |
+-------------------------------------------------+-------------------------------------------------+
| Load Recent Configuration                       | Displays a list of recently opened              |
|                                                 | configuration. You can choose a configuration   |
|                                                 | from the list to use as the current             |
|                                                 | configuration.                                  |
+-------------------------------------------------+-------------------------------------------------+
| New Template                                    | Opens the Template Editor and loads an empty    |
|                                                 | template. The harness uses the Template Editor  |
|                                                 | to create a template containing known test      |
|                                                 | environment and standard values required to run |
|                                                 | a test suite.                                   |
|                                                 |                                                 |
|                                                 | See [Creating a                                 |
|                                                 | Template](../admin/createTemplate.html) for     |
|                                                 | detailed information about templates.           |
+-------------------------------------------------+-------------------------------------------------+
| Load Template                                   | Opens a Load Template dialog box that you use   |
|                                                 | to load an existing template into the Test      |
|                                                 | Manager.                                        |
|                                                 |                                                 |
|                                                 | See [Loading a                                  |
|                                                 | Template](../templates/loadTemplate.html) for   |
|                                                 | detailed information.                           |
+-------------------------------------------------+-------------------------------------------------+
| Load Recent Template                            | Displays a list of templates that have been     |
|                                                 | opened. You can choose a template from the list |
|                                                 | to use as the template for creating a           |
|                                                 | configuration.                                  |
+-------------------------------------------------+-------------------------------------------------+
| Update Template                                 | Enabled only when template propagation is       |
|                                                 | enabled and the template on which current       |
|                                                 | configuration is based has changed. Displays a  |
|                                                 | dialog box that enables you to import changes   |
|                                                 | from the template into your configuration.      |
+-------------------------------------------------+-------------------------------------------------+

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2001, 2011, Oracle and/or its affiliates. All rights reserved.
