---
hIndent: 2
title: Setting Concurrency and Timeout Factor
---

[]{#execution}

# Setting Concurrency and Timeout Factor {#setting-concurrency-and-timeout-factor .proc}

![This is the start of a procedure](../../images/hg_proc.gif){longdesc="execution.html"} To set the
concurrency and the timeout factor, perform the following steps:

1.  Click the ![QuickSet Mode button](../../images/stdValues_button.gif){width="10" height="11"
    longdesc="toolBar.html"} Quick Set Mode button on the Test Manager tool bar, choose Configure \>
    Edit Quick Set \> Concurrency, or choose Configure \> Edit Quick Set \> Timeout Factor in the
    menu bar.

> The Configuration Editor opens in Quick Set Mode.

![Execution tab](../../images/JT4executionTabConfigEd.gif){longdesc="execution.html"}

![The following text is a Note](../../images/hg_note.gif){width="18" height="13"
longdesc="keywords.html"}\
You can also use the Configuration Editor window in Question Mode to specify the concurrency and the
timeout factor.

2.  Click the Execution tab if it does not have focus.
3.  Use the text fields in the tabbed pane to set the concurrency and the timeout factor values used
    to run tests in your computing environment.

> See [Concurrency](#execution.concurrency) and [Time Factor](#execution.timeFactor) for a detailed
> description of these values and how they are used by the harness when running tests.

4.  Click the Done button to save the configuration change.

[]{#execution.concurrency}

## Concurrency

The harness can run tests concurrently. If you are running the tests on a multiprocessor computer or
are using multiple agents on a test system, concurrency can reduce the time required to run tests.
For detailed information about using agents to run tests, refer to your test suite documentation and
to the *JT Harness Agent User\'s Guide* if it is provided by the test suite.

When using multiple agents to run tests, the concurrency value must not exceed the number of agents.
If the concurrency value exceeds the total number of available agents, an error will occur in the
test run.

If you have unexpected test failures, run the tests again, one at a time. Some test suites may not
work correctly if you run tests concurrently. The default range of values used by the harness is
from `1` to `256`.

For your first test run, leave this field set to `1`. After the tests run properly, you can increase
this value. Unless your test suite restricts concurrency, the maximum *number* of threads specified
by the `concurrency` command is `256`. See your test suite documentation for additional information
about using concurrency values greater than `1`.

This field is disabled for some test suites.

[]{#execution.timeFactor}

## Time Factor

To prevent a stalled test from stopping a test run, most test suites set a timeout limit for each
test. The timeout limit is the amount of time that the harness waits for a test to complete before
moving on to the next test.

If you are running the tests on a particularly slow CPU or slow network, you can change the time
limit by specifying a floating point value in the time factor field. Each test\'s timeout limit is
multiplied by the time factor value. The default range of values used by the harness is from `0.1`
to `100.0`.

![The following text is a note](../../images/hg_note.gif){longdesc="overview.html"}\
In the Time Factor field, the harness uses the form of floating point values that is specific to the
locale in which it is run. For example, if your locale uses floating point values in the form of
`x,x`, harness uses that form of floating point value in the Time Factor field. In setting the
timeout factor in the following example, specify values of `2,0` and `0,5` if your locale uses
floating point values in the form of `x,x`.

Example:

If you specify a value of `2.0`, the timeout limit for tests with a basic 10-minute time limit
becomes 20 minutes. Specifying a value of `0.5` for tests with a 10-minute limit produces a 5-minute
timeout limit.

At first, use the default value of `1.0` to run tests and then, if necessary, increase the value.
The actual timeout calculation for any particular test suite might vary.

----------------------------------------------------------------------------------------------------

[Copyright](../copyright.html) © 2002, 2015, Oracle and/or its affiliates. All rights reserved.
