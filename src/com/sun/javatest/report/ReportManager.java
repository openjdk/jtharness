/*
 * $Id$
 *
 * Copyright (c) 2004, 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.sun.javatest.report;

import com.sun.javatest.AllTestsFilter;
import com.sun.javatest.CompositeFilter;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.LastRunFilter;
import com.sun.javatest.ParameterFilter;
import com.sun.javatest.TestFilter;
import com.sun.javatest.tool.Command;
import com.sun.javatest.tool.CommandContext;
import com.sun.javatest.tool.CommandManager;
import com.sun.javatest.tool.Preferences;
import com.sun.javatest.util.HelpTree;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.StringArray;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A command manager to provide commands for reporting test results.
 */
public class ReportManager
        extends CommandManager {
    public static final String BUGRPT_URL_PREF = "exec.report.bugurl";
    // can be improved in the future, perhaps if the filters are represented by objects which we can
    // query about their name
    private static final String[] FILTERS = {"lastRun", "currentConfig", "allTests"};
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(ReportManager.class);

    public static void writeReport(File reportDir, CommandContext ctx)
            throws Command.Fault {
        Command c = new WriteReportCommand(reportDir);
        c.run(ctx);
    }

    // copied from exec.ParameterFilter to avoid cross-package dependency
    private static File[] stringsToFiles(String... tests) {
        if (tests == null) {
            return null;
        }

        File[] files = new File[tests.length];
        for (int i = 0; i < tests.length; i++) {
            files[i] = new File(tests[i]);
        }

        return files;
    }

    @Override
    public HelpTree.Node getHelp() {
        HelpTree.Node[] cmdNodes = {
                getCommandHelp(ReportCommand.getName()),
                getCommandHelp(WriteReportCommand.getName())
        };
        return new HelpTree.Node(i18n, "rm.help", cmdNodes);

    }

    private HelpTree.Node getCommandHelp(String name) {
        return new HelpTree.Node(i18n, "rm.help." + name);
    }

    //--------------------------------------------------------------------------

    @Override
    public boolean parseCommand(String cmd, ListIterator<String> argIter, CommandContext ctx)
            throws Command.Fault {
        if (isMatch(cmd, ReportCommand.getName())) {
            ctx.addCommand(new ReportCommand(argIter));
            return true;
        }

        if (isMatch(cmd, WriteReportCommand.getName())) {
            ctx.addCommand(new WriteReportCommand(argIter));
            return true;
        }

        return false;
    }

    //--------------------------------------------------------------------------

    static class ReportCommand
            extends Command {
        private File path;

        ReportCommand(ListIterator<String> argIter) throws Fault {
            super(getName());

            if (!argIter.hasNext()) {
                throw new Fault(i18n, "rm.report.missingArg");
            }

            path = new File(nextArg(argIter));
        }

        static String getName() {
            return "report";
        }

        @Override
        public void run(CommandContext ctx) {
            ctx.setAutoRunReportDir(path);
        }
    }

    static class WriteReportCommand extends Command {
        private File path;
        private boolean createFlag;
        private List<String> types;
        private String filter;

        WriteReportCommand(Iterator<String> argIter) throws Fault {
            super(getName());

            while (argIter.hasNext()) {
                String arg = nextArg(argIter);
                if (arg.equals("-create")) {
                    createFlag = true;
                } else if (arg.equals("-type")) {
                    if (!argIter.hasNext()) {
                        throw new Fault(i18n, "rm.writeReport.missingArg");
                    }
                    if (types == null) {
                        types = new ArrayList<>();
                    }
                    types.add(nextArg(argIter));
                } else if (arg.equalsIgnoreCase("-kfl") ||
                        arg.equalsIgnoreCase("-enableKFL")) {
                    // allow user to turn on KFL section by overriding Preferences
                    Preferences.access().setPreference("rps.html.kfl", "true");
                } else if (arg.equals("-filter")) {
                    if (!argIter.hasNext()) {
                        throw new Fault(i18n, "rm.writeReport.missingArg");
                    }
                    filter = nextArg(argIter);
                    validateFilter();
                } else if (arg.startsWith("-"))
                // since the report dir is a required arg,
                // any other option must be a bad one
                {
                    throw new Fault(i18n, "rm.writeReport.badArg", arg);
                } else {
                    path = new File(arg);
                    return;
                }
            }

            // drop through if path not given
            throw new Fault(i18n, "rm.writeReport.missingArg");
        }

        WriteReportCommand(File reportDir) {
            super(reportDir.getPath());
            path = reportDir;
        }

        static String getName() {
            return "writeReport";
        }

        /**
         * Validate current filter value, throw Fault if there is a problem.
         */
        private void validateFilter() throws Fault {
            if (!StringArray.contains(FILTERS, filter)) {
                throw new Fault(i18n, "rm.writeReport.notAFilter", filter);
            }

        }

        @Override
        public boolean isActionCommand() {
            return true;
        }

        @Override
        public void run(CommandContext ctx) throws Fault {
            try {
                if (ctx.getWorkDirectory() == null) {
                    throw new Fault(i18n, "rm.writeReport.noWorkdir");
                }
            } catch (CommandContext.Fault f) {
                throw new Fault(f);
            }

            if (path.exists()) {
                if (!path.isDirectory()) {
                    throw new Fault(i18n, "rm.writeReport.notADir", path);
                }
            } else {
                if (createFlag) {
                    path.mkdirs();
                } else {
                    path.mkdir();
                }

                if (!path.exists()) {
                    throw new Fault(i18n, "rm.writeReport.cantCreate", path);
                }
            }

            InterviewParameters interviewParameters = getConfig(ctx);
            TestFilter filterO = null;

            if (filter == null || filter.equalsIgnoreCase("currentConfig")) {
                // must specially add the Tests to Run filter
                // see javadoc for interviewParameters.getTests()
                ParameterFilter pFilter = new ParameterFilter();
                pFilter.update(interviewParameters);
                filterO = pFilter;
            } else if (filter.equalsIgnoreCase("allTests")) {
                filterO = new AllTestsFilter();
            } else if (filter.equalsIgnoreCase("lastRun")) {
                try {
                    filterO = new LastRunFilter(ctx.getWorkDirectory());
                } catch (CommandContext.Fault f) {
                    // should in theory never happen in CLI mode
                    ctx.printMessage(i18n, "rm.writeReport.noWdForLast", f.getMessage());
                }
            } else    // should not happen!  use legacy setting
            {
                filterO = new CompositeFilter(interviewParameters.getFilters());
            }

            try {
                // TEMP add interviewParameters.getFilters -- in time, Report API should
                // provide more flexible reporting options
                Report r = new Report(interviewParameters, path, filterO);
                String[] typesArgs = types == null ? null : types.toArray(new String[types.size()]);
                r.writeReport(typesArgs);

                File cPath;
                try {
                    cPath = path.getCanonicalFile();
                } catch (IOException e) {
                    cPath = path;
                }

                ctx.printMessage(i18n, "rm.writeReport.done", cPath);
            } catch (IOException e) {
                ctx.printMessage(i18n, "rm.writeReport.error",
                        path, e);
            }
        }
    }

}
