---
hIndent: 1
title: Creating a Configuration
---

[]{#createConfiguration}

# Creating a Configuration

You must create a configuration for the test run if an existing configuration is not provided by the
test suite or your test group.

![The following text is a note](../../images/hg_note.gif){longdesc="createConfiguration.html"}\
If you have an existing configuration, load it in the Test Manager (see [Loading a
Configuration](loadConfiguration.html)) and then edit it (see [Editing a
Configuration](editConfiguration.html)) as required for your test run.

Before creating a configuration, if you have a template provided by your test group or by the test
suite, you can load it in the Test Manager (see [Loading a
Template](../templates/loadTemplate.html)) and use it as the basis for creating a configuration. You
can also create a template (see [Creating a Template](../admin/createTemplate.html)), if an existing
template is not available, and use it as the basis for creating a configuration.

## Create a New Configuration {#create-a-new-configuration .proc}

![This is the start of a procedure](../../images/hg_proc.gif){longdesc="createConfiguration.html"}To
create a configuration for the test run, perform the following steps:

1.  If you have an appropriate template provided by your test group or by the test suite, load it in
    the Test Manager (see [Loading a Template](../templates/loadTemplate.html)).

    You can create a template (see [Creating a Template](../admin/createTemplate.html)), if an
    appropriate, existing template is not available.

    If you choose not to use a template, go to Step 2.

2.  Choose Configure **\>** New Configuration from the Test Manager menu bar or File **\>** New
    Configuration from the Configuration Editor menu bar.

3.  Answer the questions displayed in the Configuration Editor window.

    If you are creating a configuration from a template, the values set in the template are applied
    to the questions.

    ![The following text is a note](../../images/hg_note.gif){longdesc="createConfiguration.html"}\
    The Configuration Editor window displays questions in the center pane (see the [Question
    Pane](fullViewDialog.html#fullViewDialog.questionPane)). Use the text box, radio button, or
    combo box controls located beneath the question to provide the required configuration
    information. After you answer each question, click the Next button to proceed to the next
    question. Some questions provide information and do not require an answer. In these cases, click
    the Next button to proceed to the next question.

    You can go backward and forward to any question to review or change your answer by doing one of
    the following:

    -   Choosing a question directly from the Index pane
    -   Clicking the Back button, the Next button, or the Last button\
        As you move backward and forward, the Configuration Editor window saves all answers until
        you either save the configuration or change the answers.\
        Choose File **\>** Save at any time to save your answers and position in a configuration
        file. See [Saving a Configuration](saveConfiguration.html).

4.  After you complete the configuration, click the Done button to save the configuration and close
    the Configuration Editor window.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2003, 2011, Oracle and/or its affiliates. All rights reserved.
