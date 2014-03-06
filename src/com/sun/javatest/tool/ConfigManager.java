/*
 * $Id$
 *
 * Copyright (c) 2004, 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.tool;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

import com.sun.interview.Interview;
import com.sun.interview.NullQuestion;
import com.sun.interview.CompositeQuestion;
import com.sun.interview.Question;
import com.sun.javatest.FileParameters;
import com.sun.javatest.InterviewParameters;
import com.sun.javatest.Parameters;
import com.sun.javatest.Status;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.util.DirectoryClassLoader;
//import com.sun.javatest.util.PathClassLoader;
import com.sun.javatest.util.HelpTree;
import com.sun.javatest.util.I18NResourceBundle;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * A manager for all the various configuration commands.
 */
public class ConfigManager
    extends CommandManager
{
    public HelpTree.Node getHelp() {
        Object[] childData = {
            ConcurrencyCommand.getName(),
            ConfigCommand.getName(),
            EnvCommand.getName(),
            EnvFilesCommand.getNames(),
            ExcludeListCommand.getName(),
            KeywordsCommand.getName(),
            KflCommand.getName(),
            OpenCommand.getName(),
            ParamsCommand.getHelp(),
            PriorStatusCommand.getName(),
            SetCommand.getName(),
            TestsCommand.getName(),
            TestSuiteCommand.getNames(),
            TimeoutFactorCommand.getName(),
            WorkDirectoryCommand.getNames(),
            WriteConfigCommand.getName()
        };

        return getHelp(i18n, "cnfg", childData);
    }

    HelpTree.Node getHelp(I18NResourceBundle i18n, String prefix, Object[] childData) {
        Vector v = new Vector();
        for (int i = 0; i < childData.length; i++) {
            Object data = childData[i];
            if (data instanceof HelpTree.Node)
                v.add(data);
            else if (data instanceof String)
                v.add(new HelpTree.Node(i18n, prefix + "." + data));
            else if (data instanceof String[]) {
                String[] names = (String[]) data;
                for (int j = 0; j < names.length; j++)
                    v.add(new HelpTree.Node(i18n, prefix + "." + names[j]));
            }
            else
                throw new IllegalArgumentException();
        }
        HelpTree.Node[] childNodes = new HelpTree.Node[v.size()];
        v.copyInto(childNodes);
        return new HelpTree.Node(i18n, prefix, childNodes);
    }

    public boolean parseCommand(String cmd, ListIterator argIter, CommandContext ctx)
        throws Command.Fault
    {
        if (isMatch(cmd, ConcurrencyCommand.getName())) {
            ctx.addCommand(new ConcurrencyCommand(argIter));
            return true;
        }

        if (isMatch(cmd, ConfigCommand.getName())) {
            ctx.addCommand(new ConfigCommand(argIter));
            return true;
        }

        if (isMatch(cmd, EnvCommand.getName())) {
            ctx.addCommand(new EnvCommand(argIter));
            return true;
        }

        if (isMatch(cmd, EnvFilesCommand.getNames())) {
            ctx.addCommand(new EnvFilesCommand(argIter));
            return true;
        }

        if (isMatch(cmd, ExcludeListCommand.getName())) {
            ctx.addCommand(new ExcludeListCommand(argIter));
            return true;
        }

        if (isMatch(cmd, OpenCommand.getName())) {
            ctx.addCommand(new OpenCommand(argIter));
            return true;
        }

        if (isMatch(cmd, KeywordsCommand.getName())) {
            ctx.addCommand(new KeywordsCommand(argIter));
            return true;
        }

        if (isMatch(cmd, KflCommand.getName())) {
            ctx.addCommand(new KflCommand(argIter));
            return true;
        }

        if (isMatch(cmd, ParamsCommand.getName())) {
            ctx.addCommand(new ParamsCommand(argIter));
            return true;
        }

        if (isMatch(cmd, PriorStatusCommand.getName())) {
            ctx.addCommand(new PriorStatusCommand(argIter));
            return true;
        }

        if (isMatch(cmd, SetCommand.getName())) {
            ctx.addCommand(new SetCommand(argIter));
            return true;
        }

        if (isMatch(cmd, SetXCommand.getName())) {
            ctx.addCommand(new SetXCommand(argIter));
            return true;
        }

        if (isMatch(cmd, TestsCommand.getName())) {
            ctx.addCommand(new TestsCommand(argIter));
            return true;
        }

        if (isMatch(cmd, TestSuiteCommand.getNames())) {
            ctx.addCommand(new TestSuiteCommand(argIter));
            return true;
        }

        if (isMatch(cmd, TimeoutFactorCommand.getName())) {
            ctx.addCommand(new TimeoutFactorCommand(argIter));
            return true;
        }

        if (isMatch(cmd, WorkDirectoryCommand.getNames())) {
            ctx.addCommand(new WorkDirectoryCommand(argIter));
            return true;
        }

        if (isMatch(cmd, WriteConfigCommand.getName())) {
            ctx.addCommand(new WriteConfigCommand(argIter));
            return true;
        }


        return false;
    }

    static Command getOpenCommand(File file)
        throws Command.Fault
    {
        return new OpenCommand(file);
    }

    private static Map commandFactory;
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(ConfigManager.class);

    //--------------------------------------------------------------------------

    private static class ConcurrencyCommand extends Command
    {
        static String getName() {
            return "concurrency";
        }

        ConcurrencyCommand(ListIterator argIter) throws Fault {
            super(getName());

            if (!argIter.hasNext())
                throw new Fault(i18n, "cnfg.conc.missingArg");

            String arg = nextArg(argIter);

            NumberFormat fmt = NumberFormat.getIntegerInstance(); // will be locale-specific
            ParsePosition pos = new ParsePosition(0);
            Number num = fmt.parse(arg, pos);
            if (num != null && (pos.getIndex() == arg.length())) {
                value = num.intValue();
                if (value < Parameters.ConcurrencyParameters.MIN_CONCURRENCY
                    || value > Parameters.ConcurrencyParameters.MAX_CONCURRENCY) {
                    throw new Fault(i18n, "cnfg.conc.badRange",
                                    new Object[] {
                                        arg,
                                        new Integer(Parameters.ConcurrencyParameters.MIN_CONCURRENCY),
                                        new Integer(Parameters.ConcurrencyParameters.MAX_CONCURRENCY) });
                }
            }
            else
                throw new Fault(i18n, "cnfg.conc.badValue", arg);
        }

        public void run(CommandContext ctx) throws Fault {
            InterviewParameters p = getConfig(ctx);
            if (p.getConcurrencyParameters() instanceof Parameters.MutableConcurrencyParameters) {
                Parameters.MutableConcurrencyParameters cParams =
                    (Parameters.MutableConcurrencyParameters) (p.getConcurrencyParameters());
                cParams.setConcurrency(value);
            }
            else
                throw new Fault(i18n, "cnfg.conc.notEditable");
        }

        private int value;
    }


    //--------------------------------------------------------------------------

    private static class ConfigCommand extends Command
    {
        static String getName() {
            return "config";
        }

        ConfigCommand(ListIterator argIter) throws Fault {
            super(getName());

            if (!argIter.hasNext())
                throw new Fault(i18n, "cnfg.conf.missingArg");

            path = new File(nextArg(argIter));
        }

        ConfigCommand(File path) {
            super(path.getPath());

            this.path = path;
        }

        public void run(CommandContext ctx) throws Fault {
            /*OLD
            try {
                InterviewParameters p = getConfig(ctx);
                if (p == null)
                    ctx.setInterviewParameters(InterviewParameters.open(path));
                else {
                    // should check for compatibility?
                    p.load(path);
                }

                if (ctx.getWorkDirectory() == null)
                    ctx.setWorkDirectory(p.getWorkDirectory());

                ctx.setAutoRunReportDir(null);
            }
            catch (TestSuite.Fault e) {
                throw new Fault(i18n, "cnfg.cantSetParameters", e.getMessage());
            }
            catch (FileNotFoundException e) {
                throw new Fault(i18n, "cnfg.cantFindFile", path);
            }
            catch (IOException e) {
                throw new Fault(i18n, "cnfg.cantReadFile", new Object[] { path, e} );
            }
            catch (InterviewParameters.Fault e) {
                throw new Fault(i18n, "cnfg.cantOpenConfig", new Object[] { path, e} );
            }
            */

            try {
                ctx.setConfig(path);
                ctx.setAutoRunReportDir(null);
            }
            catch (CommandContext.Fault e) {
                throw new Fault(e);
            }
        }

        private File path;
    }

    //--------------------------------------------------------------------------

    private static class WriteConfigCommand extends Command
    {
        static String getName() {
            return "writeConfig";
        }

        WriteConfigCommand(ListIterator argIter) throws Fault {
            super(getName());

            if (!argIter.hasNext())
                throw new Fault(i18n, "cnfg.conf.missingArg");
                // XXX could provide a better error message, perhaps including the value of
                //     getName(), because the missingArg error message is general purpose
                //     EX: throw new Fault(i18n, "cnfg.conf.missingArg", getName());

            path = new File(nextArg(argIter));
        }

        WriteConfigCommand(File path) {
            super(path.getPath());

            this.path = path;
        }

        public void run(CommandContext ctx) throws Fault {
            try {
                InterviewParameters p = getConfig(ctx);
                p.saveAs(path, true, true);
            }
            catch (IOException e) {
                if (!path.canWrite())
                    throw new Fault(i18n, "cnfg.writeConfig.cantWrite", path.getPath());
                else
                    throw new Fault(i18n, "cnfg.writeConfig.writeErr", new Object[] { path, e } );
            }
            catch (Interview.Fault e) {
                throw new Fault(i18n, "cnfg.writeConfig.badConfig", new Object[] { path, e.getMessage() } );
            }   // catch
        }

        private File path;
    }

    //--------------------------------------------------------------------------

    private static class EnvCommand extends Command
    {
        static String getName() {
            return "env";
        }

        EnvCommand(ListIterator argIter) throws Fault {
            super(getName());

            if (!argIter.hasNext())
                throw new Fault(i18n, "cnfg.env.missingArg");

            name = nextArg(argIter);
        }

        public void run(CommandContext ctx) throws Fault {
            InterviewParameters p = getConfig(ctx);
            if (p.getEnvParameters() instanceof Parameters.LegacyEnvParameters) {
                Parameters.LegacyEnvParameters eParams =
                    (Parameters.LegacyEnvParameters) (p.getEnvParameters());
                eParams.setEnvName(name);
            }
            else
                throw new Fault(i18n, "cnfg.env.notEditable");
        }

        private String name;
    }

    //--------------------------------------------------------------------------

    private static class EnvFilesCommand extends Command
    {
        static String[] getNames() {
            return new String[] { "envfile", "envfiles" };
        }

        EnvFilesCommand(ListIterator argIter) throws Fault {
            super(getNames()[0]);

            Vector v = new Vector();

            while (argIter.hasNext()) {
                String arg = nextArg(argIter);
                if (arg.startsWith("-")) {
                    putbackArg(argIter);
                    break;
                }
                else
                    v.add(new File(arg));
            }

            if (v.size() == 0)
                throw new Fault(i18n, "cnfg.envFiles.noFiles");

            files = new File[v.size()];
            v.toArray(files);
        }

        public void run(CommandContext ctx) throws Fault {
            InterviewParameters p = getConfig(ctx);
            if (p.getEnvParameters() instanceof Parameters.LegacyEnvParameters) {
                Parameters.LegacyEnvParameters eParams =
                    (Parameters.LegacyEnvParameters) (p.getEnvParameters());
                eParams.setEnvFiles(files);
            }
            else
                throw new Fault(i18n, "cnfg.envFiles.notEditable");
        }

        private File[] files;
    }

    //--------------------------------------------------------------------------

    private static class ExcludeListCommand extends Command
    {
        static String getName() {
            return "excludeList";
        }

        ExcludeListCommand(ListIterator argIter) throws Fault {
            super(getName());

            // in time, we should support -none, -default, -latest etc
            Vector v = new Vector();

            while (argIter.hasNext()) {
                String arg = nextArg(argIter);
                if (arg.startsWith("-")) {
                    putbackArg(argIter);
                    break;
                }
                else
                    v.add(new File(arg));
            }

            if (v.size() == 0)
                throw new Fault(i18n, "cnfg.excl.noFiles");

            files = new File[v.size()];
            v.toArray(files);
        }

        public void run(CommandContext ctx) throws Fault {
            InterviewParameters p = getConfig(ctx);
            if (p.getExcludeListParameters() instanceof Parameters.MutableExcludeListParameters) {
                Parameters.MutableExcludeListParameters eParams =
                    (Parameters.MutableExcludeListParameters) (p.getExcludeListParameters());
                eParams.setExcludeMode(Parameters.MutableExcludeListParameters.CUSTOM_EXCLUDE_LIST);
                eParams.setCustomExcludeFiles(files);
            }
            else
                throw new Fault(i18n, "cnfg.excl.notEditable");
        }

        private File[] files;
    }
    //--------------------------------------------------------------------------

    private static class KflCommand extends Command
    {
        static String getName() {
            return "kfl";
        }

        KflCommand(ListIterator argIter) throws Fault {
            super(getName());

            // in time, we should support -none, -default, -latest etc
            Vector v = new Vector();

            while (argIter.hasNext()) {
                String arg = nextArg(argIter);
                if (arg.startsWith("-")) {
                    putbackArg(argIter);
                    break;
                }
                else
                    v.add(new File(arg));
            }

            if (v.size() == 0)
                throw new Fault(i18n, "cnfg.kfl.noFiles");

            files = new File[v.size()];
            v.toArray(files);
        }

        public void run(CommandContext ctx) throws Fault {
            InterviewParameters p = getConfig(ctx);
            p.setKnownFailureFiles(files);
//          if (p.getExcludeListParameters() instanceof Parameters.MutableExcludeListParameters) {
//              Parameters.MutableExcludeListParameters eParams =
//                  (Parameters.MutableExcludeListParameters) (p.getExcludeListParameters());
//              eParams.setExcludeMode(Parameters.MutableExcludeListParameters.CUSTOM_EXCLUDE_LIST);
//              eParams.setCustomExcludeFiles(files);
//          }
//          else
//              throw new Fault(i18n, "cnfg.excl.notEditable");
        }

        private File[] files;
    }
    //--------------------------------------------------------------------------

    private static class OpenCommand extends Command
    {
        static String getName() {
            return "open";
        }

        OpenCommand(File file) throws Fault {
            super(file.getPath());
            cmdForFile = getCommandForFile(file);
        }

        OpenCommand(ListIterator argIter) throws Fault {
            super(getName());

            if (!argIter.hasNext())
                throw new Fault(i18n, "cnfg.open.missingArg");

            String arg = nextArg(argIter);
            cmdForFile = getCommandForFile(new File(arg));
        }

        public void run(CommandContext ctx) throws Fault {
            cmdForFile.run(ctx);
        }

        Command getCommandForFile(File file)
            throws Fault
        {
            if (!file.exists())
                throw new Fault(i18n, "cnfg.open.cantFindFile", file);

            if (TestSuite.isTestSuite(file))
                return new TestSuiteCommand(file);

            if (WorkDirectory.isWorkDirectory(file))
                return new WorkDirectoryCommand(file);

            if (FileParameters.isParameterFile(file))
                return new ParamFileCommand(file);

            if (InterviewParameters.isInterviewFile(file))
                return new ConfigCommand(file);

            if (file.getPath().endsWith(".jte"))
                throw new Fault(i18n, "cnfg.open.cantOpenJTE", file);

            if (file.getPath().endsWith(".jtt"))
                throw new Fault(i18n, "cnfg.open.cantOpenJTT", file);

            if (file.getPath().endsWith(".jtx"))
                throw new Fault(i18n, "cnfg.open.cantOpenJTX", file);

            throw new Fault(i18n, "cnfg.open.unknownFileType", file);
        }

        private Command cmdForFile;
    }

    //--------------------------------------------------------------------------

    private static class KeywordsCommand extends Command
    {
        static String getName() {
            return "keywords";
        }

        KeywordsCommand(ListIterator argIter) throws Fault {
            super(getName());

            // could support -all -any
            if (!argIter.hasNext())
                throw new Fault(i18n, "cnfg.keywords.missingArg");

            expr = nextArg(argIter);
        }

        public void run(CommandContext ctx) throws Fault {
            InterviewParameters p = getConfig(ctx);
            if (p.getKeywordsParameters() instanceof Parameters.MutableKeywordsParameters) {
                Parameters.MutableKeywordsParameters kParams =
                    (Parameters.MutableKeywordsParameters) (p.getKeywordsParameters());
                if (expr == null)
                    kParams.setKeywordsMode(Parameters.MutableKeywordsParameters.NO_KEYWORDS);
                else {
                    kParams.setKeywordsMode(Parameters.MutableKeywordsParameters.MATCH_KEYWORDS);
                    kParams.setMatchKeywords(Parameters.MutableKeywordsParameters.EXPR, expr);
                }
            }
            else
                throw new Fault(i18n, "cnfg.keywords.notEditable");
        }

        private String expr;
    }

    //--------------------------------------------------------------------------
    // Legacy CLI support (-params, jte and jtp files)
    // Very deprecated.

    private static abstract class ParamsBaseCommand extends Command
    {
        ParamsBaseCommand(String name) {
            super(name);
        }

        protected void setParameters(CommandContext ctx, FileParameters fp)
            throws Fault
        {
            /*OLD
              if (ctx.getTestSuite() != null)
              throw new Command.Fault(i18n, "cnfg.testSuiteAlreadySet");

              if (ctx.getWorkDirectory() != null)
              throw new Command.Fault(i18n, "cnfg.workDirAlreadySet");
            */

            try {
                ctx.setTestSuite(fp.getTestSuite());

                if (fp.getWorkDirectory() != null)
                    ctx.setWorkDirectory(fp.getWorkDirectory());

                getConfig(ctx).load(fp);

                // support for old feature
                File autoRunReportDir = fp.getReportDir();
                if (autoRunReportDir == null) {
                    File rd = new File("reports", "report");
                    autoRunReportDir = ctx.getWorkDirectory().getFile(rd.getPath());
                }
                ctx.setAutoRunReportDir(autoRunReportDir);
            }
            catch (TestSuite.Fault e) {
                throw new Fault(i18n, "cnfg.cantSetParameters", e.getMessage());
            }
            catch (CommandContext.Fault e) {
                throw new Fault(e);
            }
        }
    }

    //--------------------------------------------------------------------------

    private static class ParamFileCommand extends ParamsBaseCommand
    {
        ParamFileCommand(File path) {
            super(path.getPath());
            this.path = path;
        }

        public void run(CommandContext ctx) throws Fault {
            try {
                FileParameters params = new FileParameters(path);
                if (!params.isValid()) {
                    throw new Fault(i18n, "cnfg.params.badParameterFile",
                                    new Object[] { path, params.getErrorMessage() } );
                }
                setParameters(ctx, params);
            }
            catch (FileNotFoundException e) {
                throw new Fault(i18n, "cnfg.params.cantFindFile", path);
            }
            catch (IOException e) {
                throw new Fault(i18n, "cnfg.params.cantReadFile",
                                new Object[] { path, e } );
            }
        }

        private File path;
    }

    //--------------------------------------------------------------------------

    private static class ParamsCommand extends ParamsBaseCommand
    {
        static String getName() {
            return "params";
        }

        static HelpTree.Node getHelp() {
            String[] opts = {
                "testSuite", "t",
                "keywords",
                "status",
                "exclude",
                "envfile",
                "env",
                "conc",
                "timeout",
                "report", "r",
                "workdir", "w"
            };
            return new HelpTree.Node(i18n, "cnfg.params", opts);
        }

        ParamsCommand(ListIterator argIter) throws Fault {
            super(getName());

            Vector v = new Vector();
            while (argIter.hasNext())
                v.add(nextArg(argIter));
            String[] args = new String[v.size()];
            v.copyInto(args);

            try {
                params = new FileParameters(args);
            } catch (IllegalArgumentException e) {
                throw new Fault(i18n, "cnfg.params.badValue", e.getMessage());
            }

            if (!params.isValid())
                throw new Fault(i18n, "cnfg.params.badValue", params.getErrorMessage());
        }

        public void run(CommandContext ctx) throws Fault {
            setParameters(ctx, params);
        }

        private FileParameters params;
    }

    //--------------------------------------------------------------------------

    private static class PriorStatusCommand extends Command
    {
        static String getName() {
            return "priorStatus";
        }

        PriorStatusCommand(ListIterator argIter) throws Fault {
            super(getName());

            if (!argIter.hasNext())
                throw new Fault(i18n, "cnfg.status.missingArg");

            String arg = nextArg(argIter);
            String[] words = split(arg.toLowerCase());
            boolean any = false;
            values = new boolean[Status.NUM_STATES];
            if (words != null) {
                for (int i = 0; i < words.length; i++) {
                    String w = words[i];
                    if (w.startsWith("pass")) {
                        values[Status.PASSED] = any = true;
                    }
                    else if (w.startsWith("fail")) {
                        values[Status.FAILED] = any = true;
                    }
                    else if (w.startsWith("error")) {
                        values[Status.ERROR] = any = true;
                    }
                    else if (w.startsWith("notrun")) {
                        values[Status.NOT_RUN] = any = true;
                    }
                    else
                        throw new Fault(i18n, "cnfg.status.badArg", w);
                }
            }


            if (!any)
                throw new Fault(i18n, "cnfg.status.noValues");
        }

        public void run(CommandContext ctx) throws Fault {
            InterviewParameters p = getConfig(ctx);
            if (p.getPriorStatusParameters() instanceof Parameters.MutablePriorStatusParameters) {
                Parameters.MutablePriorStatusParameters sParams =
                    (Parameters.MutablePriorStatusParameters) (p.getPriorStatusParameters());
                sParams.setPriorStatusMode(Parameters.MutablePriorStatusParameters.MATCH_PRIOR_STATUS);
                sParams.setMatchPriorStatusValues(values);
            }
            else
                throw new Fault(i18n, "cnfg.status.notEditable");
        }

        private static String[] split(String s) {
            if (s == null)
            return null;

            Vector v = new Vector();
            int start = -1;
            for (int i = 0; i < s.length(); i++) {
                if (Character.isLetterOrDigit(s.charAt(i))) {
                    if (start == -1)
                        start = i;
                }
                else {
                    if (start != -1)
                        v.addElement(s.substring(start, i));
                    start = -1;
                }
            }
            if (start != -1)
            v.addElement(s.substring(start));
            if (v.size() == 0)
            return null;
            String[] a = new String[v.size()];
            v.copyInto(a);
            return a;
        }

        private boolean[] values;
    }


    //--------------------------------------------------------------------------

    private static class SetCommand extends Command
    {
        static String getName() {
            return "set";
        }

        SetCommand(ListIterator argIter) throws Fault {
            super(getName());

            if (!argIter.hasNext())
                throw new Fault(i18n, "cnfg.set.insufficientArgs");

            String arg = nextArg(argIter);
            if (arg.equals("-f") || arg.equals("-file")) {
                if (!argIter.hasNext())
                    throw new Fault(i18n, "cnfg.set.insufficientArgs");
                file = new File(nextArg(argIter));
            }
            else {
                tag = arg;
                if (!argIter.hasNext())
                    throw new Fault(i18n, "cnfg.set.insufficientArgs");
                value = nextArg(argIter);
            }
        }

        public void run(CommandContext ctx) throws Fault {
            InterviewParameters p = getConfig(ctx);
            Question[] path = p.getPath();
            if (file != null) {
                Map values = loadFile(file);
                for (int i = 0; i < path.length; i++) {
                    Question q = path[i];
                    String v = (String) (values.get(q.getTag()));
                    if (v != null) {
                        setValue(q, v);
                        path = p.getPath();
                    }
                }
            }
            else {
                for (int i = 0; i < path.length; i++) {
                    Question q = path[i];
                    if (q.getTag().equals(tag)) {
                        setValue(q, value);
                        return;
                    }
                }

                // The following is not ideal but works for now.
                // It is arguably bad form to return such a long detail
                // string, rather than providing an extra method
                // to generate the trace if required -- i.e. the Fault
                // equivalent of e.printStackTrace()
                throw new Fault(i18n, "cnfg.set.tagNotFound",
                                new Object[] { tag, getPathTrace(path) });
            }
        }

        private void setValue(Question q, String value) throws Fault {
            try {
                if (q instanceof CompositeQuestion) {
                    CompositeQuestion cq = (CompositeQuestion)q;
                    int sepIndex = value.indexOf(":");
                    if (sepIndex > 0) {
                        // decode value and send to question
                        // could be handled differently in the future
                        // text to the left of the colon in the values is the key
                        // all text to the right is the value
                        String key = value.substring(0, sepIndex);
                        String val;
                        if (sepIndex == value.length() + 1)
                            // handles key:
                            val = "";
                        else
                            // handles key:value
                            val = value.substring(sepIndex + 1);

                        cq.setValue(key, val);
                    }
                    else {
                        q.setValue(value);
                    }
                }
                else {
                    q.setValue(value);
                }
            }
            catch (InterviewParameters.Fault e) {
                throw new Fault(i18n, "cnfg.set.cantSetValue",
                                new Object[] { q.getTag(), value, e.getMessage() });
            }
        }

        private static String getPathTrace(Question[] path) {
            String lineSep = System.getProperty("line.separator");
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < path.length; i++) {
                Question q = path[i];
                sb.append(q.getTag());
                if (!(q instanceof NullQuestion)) {
                    String s = q.getStringValue();
                    sb.append(" (");
                    if (s == null)
                        sb.append("null");
                    else if (s.length() < 32)
                        sb.append(s);
                    else {
                        sb.append(s.substring(0, 32));
                        sb.append("...");
                    }
                    sb.append(")");
                }
                sb.append(lineSep); // arguably better to do it later when printing to terminal
            }
            return (sb.toString());
        }

        private Map loadFile(File file) throws Fault {
            FileInputStream fis = null;
            InputStream in = null;
            try {
                fis = new FileInputStream(file);
                in = new BufferedInputStream(fis);
                Properties props = new Properties();
                props.load(in);
                in.close();
                return props;
            }
            catch (FileNotFoundException e) {
                throw new Fault(i18n, "cnfg.set.cantFindFile", file);
            }
            catch (IOException e) {
                throw new Fault(i18n, "cnfg.set.cantReadFile",
                                new Object[] { file, e.getMessage() });
            }
            finally {
                if (in != null){
                    try { in.close(); } catch (IOException e) { }
                }

                if (fis != null){
                    try { fis.close(); } catch (IOException e) { }
                }
            }
        }

        private File file;
        private String tag;
        private String value;
    }

    //--------------------------------------------------------------------------

    /**
     * Sets "external" interview values from the command line.
     */
    private static class SetXCommand extends Command
    {
        static String getName() {
            return "setX";
        }

        SetXCommand(ListIterator argIter) throws Fault {
            super(getName());

            if (!argIter.hasNext())
                throw new Fault(i18n, "cnfg.set.insufficientArgs");

            String arg = nextArg(argIter);
            if (arg.equals("-f") || arg.equals("-file")) {
                if (!argIter.hasNext())
                    throw new Fault(i18n, "cnfg.set.insufficientArgs");
                file = new File(nextArg(argIter));
            }
            else {
                name = arg;
                if (!argIter.hasNext())
                    throw new Fault(i18n, "cnfg.set.insufficientArgs");
                value = nextArg(argIter);
            }
        }

        public void run(CommandContext ctx) throws Fault {
            InterviewParameters p = getConfig(ctx);
            if (file != null) {
                Map values = loadFile(file);
                Set keys = values.keySet();
                Iterator it = keys.iterator();
                String name = null;
                for (int i = 0; it.hasNext(); i++) {
                    name = (String)(it.next());
                    /*  could do it this way to reject unknown props
                    String v = p.retrieveProperty(name);
                    if (v != null) {
                        p.storeProperty(name, (String)(values.get(name)));
                    }
                    */
                    p.storeProperty(name, (String)(values.get(name)));
                }
            }
            else {
                p.storeProperty(name, value);
            }
        }

        private Map loadFile(File file) throws Fault {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                InputStream in = new BufferedInputStream(fis);
                Properties props = new Properties();
                props.load(in);
                in.close();
                return props;
            }
            catch (FileNotFoundException e) {
                throw new Fault(i18n, "cnfg.set.cantFindFile", file);
            }
            catch (IOException e) {
                throw new Fault(i18n, "cnfg.set.cantReadFile",
                                new Object[] { file, e.getMessage() });
            }
            finally {
                if (fis != null){
                    try { fis.close(); } catch (IOException e) { }
                }
            }
        }

        private File file;
        private String name;
        private String value;
    }

    //--------------------------------------------------------------------------

    private static class TestsCommand extends Command
    {
        static String getName() {
            return "tests";
        }

        TestsCommand(ListIterator argIter) throws Fault {
            super(getName());

            Vector v = new Vector();

            while (argIter.hasNext()) {
                // could possibly support @file or similar syntax here for a list of tests
                String arg = nextArg(argIter);
                if (arg.startsWith("-")) {
                    putbackArg(argIter);
                    break;
                }
                else
                    v.add(arg);
            }

            if (v.size() == 0)
                throw new Fault(i18n, "cnfg.tests.noTests");

            tests = new String[v.size()];
            v.toArray(tests);
        }

        public void run(CommandContext ctx) throws Fault {
            InterviewParameters p = getConfig(ctx);
            if (p.getTestsParameters() instanceof Parameters.MutableTestsParameters) {
                Parameters.MutableTestsParameters iParams =
                    (Parameters.MutableTestsParameters) (p.getTestsParameters());
                iParams.setTestsMode(Parameters.MutableTestsParameters.SPECIFIED_TESTS);
                iParams.setSpecifiedTests(tests);
            }
        else
            throw new Fault(i18n, "cnfg.tests.notEditable");
        }

        private String[] tests;
    }

    //--------------------------------------------------------------------------

    private static class TestSuiteCommand extends Command
    {
        static String[] getNames() {
            return new String[] { "testsuite", "ts" };
        }

        TestSuiteCommand(ListIterator argIter) throws Fault {
            super(getNames()[0]);

            while (argIter.hasNext()) {
                String arg = nextArg(argIter);
                if (arg.equalsIgnoreCase("-preferred"))
                    preferFlag = true;
                else if (arg.startsWith("-"))
                    throw new Fault(i18n, "cnfg.ts.badArg", arg);
                else {
                    path = new File(arg);
                    return;
                }
            }   // while

            // if we are here, args are exhausted or invalid
            throw new Fault(i18n, "cnfg.ts.missingArg");
        }

        TestSuiteCommand(File path) {
            super(path.getPath());
            this.path = path;
        }

        public URL getCustomSplash() {
            String basePath = path.getAbsolutePath() +
                              File.separator + "lib";
            DirectoryClassLoader loader =
                    new DirectoryClassLoader(basePath);
            try {
                ResourceBundle b = ResourceBundle.getBundle("splash",
                                        Locale.getDefault(), loader);
                String icon = b.getString("splash.icon");
                // could support a classpath value in the bundle
                // and use PathClassLoader to generate the icon
                File f = new File(icon);
                if (!f.isAbsolute()) {
                    f = new File(basePath, icon);
                }
                if (f.canRead())
                    try {
                        return f.toURL();
                    }
                    catch (java.net.MalformedURLException e) {
                        return null;
                    }
                else
                    return null;
            }
            // catch all possible exceptions from ResourceBundle
            catch (MissingResourceException m) {
                return null;
            }
            catch (NullPointerException e) {
                return null;
            }
            catch (ClassCastException e) {
                return null;
            }
        }

        /**
         * Get custom help set for harness help.
         * A file help.properties must be in the <code>lib</code> directory of
         * the test suite.  An internationalization-aware search is then done
         * to find a properties bundle named "help".  That bundle must contain
         * a classpath entry, which is then used to generate the return value.
         * The classpath value is relative to the root of the test suite, not
         * the <code>lib</code> directory.
         */
        ClassLoader getCustomHelpLoader() {
            try {
                String basePath = path.getCanonicalPath() +
                                  File.separator + "lib";
                DirectoryClassLoader loader =
                        new DirectoryClassLoader(basePath);

                ResourceBundle b = ResourceBundle.getBundle("help",
                                        Locale.getDefault(), loader);
                String cp = b.getString("classpath");

                // NOTES: PathClassLoader does not currently support resources
                //        DirectionClassLoader cannot load from jar files
                //PathClassLoader cl = new PathClassLoader(path, cp);
                //DirectoryClassLoader cl = new DirectoryClassLoader(
                //        path.getPath() + File.separator + cp);
                File f = new File(cp);
                if (!f.isAbsolute()) {
                    f = new File(path.getPath(), cp);
                }
                URLClassLoader cl = new URLClassLoader(new URL[] {f.toURL()},
                                            this.getClass().getClassLoader());

                return cl;
            }
            // catch all possible exceptions from ResourceBundle
            catch (MissingResourceException m) {
                return null;
            }
            catch (NullPointerException e) {
                return null;
            }
            catch (ClassCastException e) {
                return null;
            }
            catch (IOException e) {
                return null;
            }
        }

        public void run(CommandContext ctx) throws Fault {
            /*OLD
            if (ctx.getTestSuite() != null)
                throw new Fault(i18n, "cnfg.testSuiteAlreadySet");

            try {
                ctx.setTestSuite(TestSuite.open(path));
            }
            catch (FileNotFoundException e) {
                throw new Fault(i18n, "cnfg.ts.cantFindTestSuite", path);
            }
            catch (TestSuite.Fault e) {
                throw new Fault(i18n, "cnfg.ts.cantOpenTestSuite", e.getMessage());
            }
            */
            if (!path.exists())
                throw new Fault(i18n, "cnfg.ts.cantFindTestSuite", path);

            if (!TestSuite.isTestSuite(path))
                throw new Fault(i18n, "cnfg.ts.notATestSuite", path);

            try {
                ctx.setTestSuite(path);

                if (preferFlag) {
                    // searching desktop history to find previous compatible workdir
                    // if none, continue as normal
                    // this is not the best logical place to do this, but there doesn't
                    // seem to be another good place in the current architecture.
                    // although perhaps it would be good to defer this processing to a
                    // little later in the command processing?
                    Properties p = Desktop.getPreviousDesktop(null);
                    String s = p.getProperty("tool.count");
                    if (s != null) {
                        int count = Integer.parseInt(s);
                        for (int i = 0; i < count; i++) {
                            s = p.getProperty("tool." + i + ".class");
                            if ("com.sun.javatest.exec.ExecTool".equals(s)) {
                                s = p.getProperty("tool." + i + ".testSuite");

                                // this tool instance does not have a test suite
                                if (s == null)
                                    continue;

                                String s1 = path.getPath();
                                String s2 = s;
                                try {
                                    s1 = path.getCanonicalPath();
                                }
                                catch (IOException e) {
                                    // ignore accept default value of s1
                                }

                                try {
                                    File file = new File(s2);
                                    s2 = file.getCanonicalPath();
                                }
                                catch (IOException e) {
                                    // ignore accept default value of s2
                                }

                                if (s1.equals(s2)) {
                                    s = p.getProperty("tool." + i + ".workDir");
                                    if (s != null &&
                                        WorkDirectory.isUsableWorkDirectory(
                                            new File(s))) {
                                        // found workdir to use as preferred workdir
                                        ctx.setDefaultWorkDir(s);

                                        // we found WD,
                                        // try to restore filter now

                                        s = p.getProperty("tool." + i + ".filter");
                                        Map data = collectSpecificData("tool." + i + ".", p);

                                        if (s != null) {
                                            ctx.setDesktopData(data);
                                        }

                                        break;
                                    }
                                    else {
                                        if (s != null)
                                            System.out.println(i18n.getString("cnfg.badWorkdir", s));
                                        continue;
                                    }
                                }
                            }
                        }   // for
                    }
                }
            }
            catch (CommandContext.Fault e) {
                throw new Fault(e);
            }
        }

        private File path;
        private boolean preferFlag;

        private Map collectSpecificData(String prefix, Properties p) {
            Set<Entry<Object, Object>> s = p.entrySet();
            Iterator<Entry<Object, Object>> it = s.iterator();
            HashMap res = new HashMap();
            while (it.hasNext()) {
                Entry en = it.next();
                if (en.getKey().toString().startsWith(prefix)) {
                    String key = (String) en.getKey();
                    key = key.substring(prefix.length());
                    if ("-workDir-config-class-mgr-".contains(key)) {
                    //if ("-class-mgr-".contains(key)) {
                        continue;
                    }
                    res.put(key, en.getValue());
                }
            }
            return res;
        }
    }

    //--------------------------------------------------------------------------

    private static class TimeoutFactorCommand extends Command
    {
        static String getName() {
            return "timeoutfactor";
        }

        TimeoutFactorCommand(ListIterator argIter) throws Fault {
            super(getName());

            if (!argIter.hasNext())
                throw new Fault(i18n, "cnfg.tf.missingArg");

            String arg = nextArg(argIter);

            NumberFormat fmt = NumberFormat.getNumberInstance(); // will be locale-specific
            ParsePosition pos = new ParsePosition(0);
            Number num = fmt.parse(arg, pos);
            if (num != null && (pos.getIndex() == arg.length())) {
                value = num.floatValue();
                if (value < Parameters.TimeoutFactorParameters.MIN_TIMEOUT_FACTOR
                    || value > Parameters.TimeoutFactorParameters.MAX_TIMEOUT_FACTOR) {
                    throw new Fault(i18n, "cnfg.tf.badRange",
                                    new Object[] {
                                        arg,
                                        new Float(Parameters.TimeoutFactorParameters.MIN_TIMEOUT_FACTOR),
                                        new Float(Parameters.TimeoutFactorParameters.MAX_TIMEOUT_FACTOR) });
                }
            }
            else
                throw new Fault(i18n, "cnfg.tf.badValue", arg);
        }

        public void run(CommandContext ctx) throws Fault {
            InterviewParameters p = getConfig(ctx);
            if (p.getTimeoutFactorParameters() instanceof Parameters.MutableTimeoutFactorParameters) {
                Parameters.MutableTimeoutFactorParameters cParams =
                (Parameters.MutableTimeoutFactorParameters) (p.getTimeoutFactorParameters());
                cParams.setTimeoutFactor(value);
            }
            else
                throw new Fault(i18n, "cnfg.tf.notEditable");
        }

        private float value;
    }

    //--------------------------------------------------------------------------

    private static class WorkDirectoryCommand extends Command
    {
        static String[] getNames() {
            return new String[] { "workdirectory", "workdir", "wd" };
        }

        WorkDirectoryCommand(ListIterator argIter) throws Fault {
            super(getNames()[0]);

            while (argIter.hasNext()) {
                String arg = nextArg(argIter);
                if (arg.equalsIgnoreCase("-create"))
                    createFlag = true;
                else if (arg.equalsIgnoreCase("-overwrite")) {
                    createFlag = true;
                    overwriteFlag = true;
                }
                else if (arg.startsWith("-"))
                    throw new Fault(i18n, "cnfg.wd.badArg", arg);
                else {
                    path = new File(arg);
                    return;
                }
            }

            // drop through if path not given
            throw new Fault(i18n, "cnfg.wd.missingArg");
        }

        WorkDirectoryCommand(File path) {
            super(path.getPath());
            this.path = path;
        }

        public void run(CommandContext ctx) throws Fault {
            /*OLD
            TestSuite ts = ctx.getTestSuite();
            WorkDirectory wd = ctx.getWorkDirectory();

            if (wd != null)
                throw new Fault(i18n, "cnfg.workDirAlreadySet");

            if (createFlag)
                wd = createWorkDirectory(ts);
            else
                wd = openWorkDirectory(ts);

            try {
                ctx.setWorkDirectory(wd);
            }
            catch (TestSuite.Fault e) {
                throw new Fault(i18n, "cnfg.wd.cantOpenTestSuiteForWorkDir", e.getMessage());
            }
            */
            if (!createFlag) {
                if (!path.exists())
                    throw new Fault(i18n, "cnfg.wd.cantFindWorkDir", path);
                if (!WorkDirectory.isWorkDirectory(path)
                    && !WorkDirectory.isEmptyDirectory(path))
                    throw new Fault(i18n, "cnfg.wd.notAWorkDirectory", path);
            }

            if (overwriteFlag) {
                remove(path);
                if (path.exists())
                    throw new Fault(i18n, "cnfg.wd.cantRemoveWorkDir", path);
            }

            try {
                ctx.setWorkDirectory(path, createFlag);
            }
            catch (CommandContext.Fault e) {
                throw new Fault(e);
            }

        }

        /*OLD
        private WorkDirectory createWorkDirectory(TestSuite ts) throws Fault {
            if (ts == null)
                throw new Fault(i18n, "cnfg.wd.cantCreateWorkDir_noTestSuite");

            if (overwriteFlag) {
                remove(path);
                if (path.exists())
                    throw new Fault(i18n, "cnfg.wd.cantRemoveWorkDir", path);
            }

            try {
                return WorkDirectory.create(path, ts);
            }
            catch (WorkDirectory.Fault e) {
                throw new Fault(i18n, "cnfg.wd.cantCreateWorkDir",
                                new Object[] { path, e.getMessage() } );
            }
        }
        */

        /*OLD
        private WorkDirectory openWorkDirectory(TestSuite ts) throws Fault {
            try {
                WorkDirectory wd;
                if (path.exists()) {
                    if (WorkDirectory.isWorkDirectory(path)) {
                        if (ts == null) {
                            wd = WorkDirectory.open(path);
                            ts = wd.getTestSuite();
                        }
                        else
                            wd = WorkDirectory.open(path, ts);
                    }
                    else if (WorkDirectory.isEmptyDirectory(path)) {
                        if (ts == null)
                            throw new Fault(i18n, "cnfg.wd.cantCreateWorkDir_noTestSuite", path);
                        else
                            wd = WorkDirectory.create(path, ts);
                    }
                    else
                        throw new Fault(i18n, "cnfg.wd.notWorkDir", path);
                }
                else
                    throw new Fault(i18n, "cnfg.wd.cantFindWorkDir", path);

                if (wd.getTestSuite().getID() != ts.getID())
                    throw new Fault(i18n, "cnfg.wd.incompatibleWorkDir", path);

                return wd;
            }
            catch (FileNotFoundException e) {
                throw new Fault(i18n, "cnfg.wd.cantFindWorkDir", path);
            }
            catch (WorkDirectory.Fault e) {
                throw new Fault(i18n, "cnfg.wd.cantOpenWorkDir", e.getMessage());
            }
        }
        */

        private void remove(File path) {
            if (path.exists()) {
                if (path.isDirectory()) {
                    File[] files = path.listFiles();
                    for (int i = 0; i < files.length; i++)
                        remove(files[i]);
                    // workaround for leftover .nfs* files
                    String[] undeletables = path.list();
                    if (undeletables != null && undeletables.length > 0) {
                        for (int i = 0; i < undeletables.length; i++) {
                            String name = undeletables[i];
                            if (name.startsWith(".nfs")) {
                                File fOld = new File(path, name);
                                File fNew = new File(path.getParentFile(), name);
                                boolean ok = fOld.renameTo(fNew);
                                // discard ok result
                            }
                        }
                    }
                }
                path.delete();
            }
        }

        private File path;
        private boolean createFlag;
        private boolean overwriteFlag;
    }
}
