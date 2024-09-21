---
hIndent: 2
title: Using Known Failures Lists (KFLs)
---

[]{#kfl}

# Using Known Failures Lists {#using-known-failures-lists .proc}

A *known failures list* (KFL) is a user-authored text file with the extension `.kfl`. The KFL file
lists tests in a test suite that are known to fail. The failing tests are identified by path names
that uniquely identify test names and test cases. Known failures list files are maintained by users
(they are not distributed as part of a test suite, unlike an exclude list). Currently the known
failures information is used to generate and filter information in test reports. See the help topic
[KFL Options in HTML Reports](../report/newReports.html#optKFL) for more information on creating and
using a known failures list.

![This is the start of a procedure](../../images/hg_proc.gif){longdesc="  .html"} Perform the
following steps to specify one or more known failures list files in a configuration:

1.  Click the ![Configuration Editor Quick Set mode button displayed on the tool
    bar](../../images/stdValues_button.gif){longdesc="fullViewDialog.html"}   button in the Test
    Manager toolbar or choose Configure **\>** Edit Quick Set **\>** Known Failures List from the
    Test Manager menu bar.

> The Configuration Editor opens in Quick Set Mode.

![Known Failure List tab](../../images/JT4KFLTabConfigEd.gif){longdesc="kfl.html"}

> ![The following text is a Note](../../images/hg_note.gif){width="18" height="13"
> longdesc="initialFiles.html"}\
> You can also use Question Mode view to specify known failures lists.

2.  Click the Known Failures (KFL) tab if it does not have focus.
3.  To specify one or more KFL filters, choose the Select Files radio button.\
    Click Add to open a file chooser dialog. Locate your KFL list and click Select. You can continue
    to add filters, using the Move buttons to organize them as you see fit. Note, the order of the
    files displayed does not have any effect on the report outcome.\
    Click Done when you are finished.

<!-- -->

3.  Click the Done button to save the configuration change.

For more information, see [KFL Options in HTML Reports](../report/newReports.html#createKFL), [Known
Failures List Reports](../report/newReports.html#knownFailureListReports), and [Known Failure
Analysis](../report/newReports.html#kfanalysis).

[Copyright](../copyright.html) © 2012, Oracle and/or its affiliates. All rights reserved.
