---
hIndent: 2
title: HTML-Formatted Output
---

# HTML-Formatted Output

The HTML-formatted output is provided as human-readable pages (these pages are subject to change in
future releases of the harness), enabling users to remotely monitor batch mode test runs in a web
browser and stop test runs that are not executing as expected. The following topics describe the
HTML formatted output:

-   [Server Index Page](#index)
-   [Server Harness Page](#harness)
-   [Server Test Result Index Page](#result)
-   [Harness Environment Page](#environment)
-   [Harness Interview Page](#interview)
-   [Stop a Test Run](#stop)

## accessing HTTP Server HTML-Formatted Output

1.  Use the following command on the command line to activate the web server.

    [*\> jtharness*](aboutExamples.html){.nopdf} `-startHttp -runTests` *\[options\]*

    See [About the Command-Line Examples](aboutExamples.html) for a description of the use of *\>
    jtharness*.

2.  Copy the URL reported to the console.

    Example:\
    `JT Harness HTTPd - Success, active on port 1903`\
    `JT Harness HTTPd server available at http://129.145.162.75:1903/`

3.  Launch a web browser and enter or paste the URL in the browser URL field.

    Example:\
    `http://129.145.162.75:1903/`

## []{#index}Displaying the HTTP Server Index Page

The root of the web server provides an index page that only lists the handlers registered with the
internal web server; not all available URLs on the server. You can also display the HTTP Server
Index page by including `/index.html`at the end of the URL in the browser URL field.

Example:\
`http://129.145.162.75:1903/index.html`

Each harness has its own handler, identified by a unique number as the second component of the URL.

## []{#harness}Displaying HTTP Server Harness Page

When the harness is running tests, the harness page displays the following information:

-   Name and location of the current test suite.
-   Location of the work directory.
-   Link to view the environment information provided to the harness and used in the current test
    run. Displays an HTML-formatted view of the current environment.
-   Link to view the configuration interview used by the harness in the current test run. Displays a
    formatted view of the interview settings.
-   Link to view the current test results. Displays the Test Result Index page.

In addition to the list of registered handlers, the page also prints the UTC/GMT date on which that
page was generated (subject to the system clock on the machine which harness is running) and
provides the harness version number and build date.

You can display the HTTP Server Harness page by choosing its link on the index page or by including
`/harness` at the end of the URL in the browser URL field.

Example:\
`http://129.145.162.75:1903/harness`

## []{#result}Displaying the HTTP Server Test Result Index Page

The Test Result Index page displays the following information:

-   Work directory
-   Total number of tests in the test suite

The total number of tests is also a link to view the current test results. The test results are
displayed in a two-column table, by test name and status message.

You can display the Test Result Index page by choosing its link on the harness page.

## []{#environment}Displaying the Harness Environment Page

The Harness Environment page displays the environment information provided to the harness and used
in the current test run. The environment information is displayed in an HTML table and provides a
view of the current settings.

You can display the Harness Environment page by choosing its link on the harness page or by
including `/harness/env` at the end of the URL in the browser URL field.

Example:\
`http://129.145.162.75:1903/harness/env`

## []{#interview}Displaying the Harness Interview Page

The Harness Interview page displays the configuration interview provided to the harness and used in
the current test run.

You can display the Harness Interview page by choosing its link on the harness page or by including
`/harness/interview`at the end of the URL in the browser URL field.

Example:\
`http://129.145.162.75:1903/harness/interview`

## []{#stop}Using HTTP Server to Stop a Test Run

If you want to remotely terminate a test run, include `/harness/stop`at the end of the URL in the
browser URL field.

Example:\
`http://129.145.162.75:1903/harness/stop`

To stop the test run, you must click the STOP button on the page displayed in the browser.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2003, 2011, Oracle and/or its affiliates. All rights reserved.
