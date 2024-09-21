---
hIndent: 0
title: Changing Configuration Values with a Text Editor
---

# Changing Configuration Values With Text Editors

The harness enables you to use a text editor from within a script (such as `sed`) to change
responses in a configuration file and then launch the harness to run tests.

The configuration file is a standard harness properties file in which double backslashes and escaped
new lines are required. If you edit this file in a text editor, you must also remove the checksum
for harness to accept it when running tests.

Checksums are used by the harness to ensure that a configuration used to run tests is complete. By
removing the checksum, you risk introducing errors in the configuration used to run tests.

Test your changes in the Configuration Editor before applying them in a text editor. The
Configuration Editor checks the value and displays the correct set of related questions. See
[Configuring a Test Run](../confEdit/overview.html) in the *harness User\'s Guide: Graphical User
Interface* for detailed information about the Configuration Editor.

The relationship between the questions in a configuration depends on the test suite and the
interdependence of the questions. A change in the value of one question might change subsequent,
related configuration questions and values. If your response changes the set of required
configuration values, the Configuration Editor displays the incomplete configuration and provides
you with a new set of required configuration questions.

After you have tested your changes and are satisfied with the results, you can use the text editor
to apply them to the configuration. Remove the checksum from the configuration file before using the
changed configuration to run tests.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2004, 2011, Oracle and/or its affiliates. All rights reserved.
