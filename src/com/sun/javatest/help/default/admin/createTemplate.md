---
hIndent: 1
title: Creating a Template
---

[]{#createTemplate}

# Creating a Template {#creating-a-template .proc}

![This is the start of a procedure](../../images/hg_proc.gif){longdesc="setMarkers.html"}To create a
template, perform the following steps:

1.  Choose Configure **\>** New Template from the Test Manager menu bar.
2.  Answer the questions displayed in the Template Editor.\
    \
    The Template Editor displays questions in the center pane (the Question Pane). After answering a
    question, click the Next button to proceed to the next question.

<!-- -->

3.  Choose File **\>** Save As from the menu bar and save the template with a relevant name.\
    \
    Template file names are automatically appended with a `.jtm` extension.

An optional feature enables template updates to propagate (flow) to all of the configurations that
are based on the template. When template propagation is enabled, the harness checks for template
updates when a user does any of the following actions:

-   Starts a test run
-   Opens a work directory
-   Loads a configuration

![The following text is a Note](../../images/hg_note.gif){longdesc="createTemplate.html"}\
Changes to a template propagate to configurations based on the template *only* if propagation is
enabled in both the template and the derived configurations.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2006, 2011, Oracle and/or its affiliates. All rights reserved.
