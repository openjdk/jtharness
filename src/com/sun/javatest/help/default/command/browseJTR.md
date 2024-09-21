---
hIndent: 1
title: Browsing Result Files
---

[]{#testresult}

# Browsing Result Files

Included in the javatest.jar file is a servlet that enables you to use a web browser to view `.jtr`
files.

To view `.jtr` files in your web browser, you must configure your web server to use
the[]{#testresult1} harness `ResultBrowser` servlet:

`com.sun.javatest.servlets.ResultBrowser`

Refer to your server documentation for information about configuring it to use the harness
`ResultBrowser` servlet. Typically, you configure the web server to direct `.jtr` files to the
servlet for rendering.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2009, Oracle and/or its affiliates. All rights reserved.
