/*
 * $Id$
 *
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javatest.Harness;
import com.sun.javatest.JavaTestSecurityManager;
import com.sun.javatest.ProductInfo;
import com.sun.javatest.Status;
import com.sun.javatest.services.ServiceManager;
import com.sun.javatest.util.ExitCount;
import com.sun.javatest.util.HelpTree;
import com.sun.javatest.util.I18NResourceBundle;
import com.sun.javatest.util.WrapWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.TreeMap;
import java.util.Vector;

/**
 * The main program class for Javatest Lite
 *
 */
public class Main {

    public final void run(String[] args, final CommandContext ctx)
        throws Command.Fault, CommandContext.Fault, CommandParser.Fault {

        if (commandManagers == null) {
            helpManager = new MiniHelpManager();
            commandManagers = new CommandManager[] {
                new com.sun.javatest.batch.BatchManager(),
                new com.sun.javatest.report.ReportManager(),
                new ConfigManager(),
                new EnvironmentManager(),
                new HttpManager(),
                new LogManager(),
                new ServiceManager.ServiceCommandManager(),
                helpManager
            };
            helpManager.setCommandManagers(commandManagers);
        }

        CommandParser p = new CommandParser(commandManagers);
        boolean urlEncoded = Boolean.getBoolean("javatest.command.urlEncoded");
        p.parse(args, urlEncoded, ctx);

        if (!initialized) {
            File classDir = ProductInfo.getJavaTestClassDir();
            Harness.setClassDir(classDir);

            // Install our own security manager. This is primarily for self-defense
            // against sameJVM tests, and not to prevent access outside the sandbox.
            // Moan to stderr if it can't be installed.
            JavaTestSecurityManager.install();

            // mark initialization done
            initialized = true;
        }

        final Command[] cmds = ctx.getCommands();

        // special case, when user requested info
        boolean helpInfoRequired = helpManager.isInfoRequired();
        if (helpInfoRequired) {
            helpManager.showRequiredInfo(ctx.getLogWriter(), ctx);
            if (cmds.length == 0)
                return;
        }

        ctx.runCommands();
        ctx.dispose();
    }

    /**
     * Run JT Harness with command-line args.
     * @param args Arguments, per the command-line spec
     */
    public static void main(String[] args) {
        checkJavaVersion();
        mainMini(args);
    }

    static void checkJavaVersion() {
        String javaVersion = System.getProperty("java.version");
        if (javaVersion != null) {
            String[] oldVersions = {"1.0", "1.1", "1.2", "1.3", "1.4", "1.5"};
            for (int i = 0; i < oldVersions.length; i++) {
                if (javaVersion.startsWith(oldVersions[i])) {
                    // I18N?
                    System.err.println("Please use Java(TM) Standard Edition, Version 6.0 or better to run the JT Harness(TM) harness.");
                    System.exit(1);
                }
            }
        }
    }

    private static void mainMini(String[] args) {
        PrintWriter out = new PrintWriter(System.err) {

            @Override
            public void close() {
                flush();
            }
        };

        try {
            ExitCount.inc();
            Main m = new Main();

            CommandContext ctx = new CommandContext(out);
            if (args.length == 0) {
                args = new String[]{"-version", "-help"};
            }
            m.run(args, ctx);

            out.flush(); // flush now in case ExitCount exits

            int[] stats = ctx.getTestStats();
            int rc = (stats[Status.ERROR] > 0 ? RC_BATCH_TESTS_ERROR
                    : stats[Status.FAILED] > 0 ? RC_BATCH_TESTS_FAILED
                    : RC_OK);

            ExitCount.dec(true, rc);
            out.flush();
        } catch (Command.Fault e) {
            // occurs when executing default commands at end of command line
            out.println(CommandContext.TRACE_PREFIX + e.getCommand().toString());
            out.println(e.getMessage());
            out.flush();
            exit(RC_USER_ERROR);
        } catch (CommandContext.Fault e) {
            // occurs when executing commands on command line or in command file
            Throwable t = e.getCause();
            if (t instanceof Command.Fault) {
                CommandContext ctx = e.getContext();
                boolean verboseCommands =
                        ctx.getVerboseOptionValue(CommandContext.VERBOSE_COMMANDS);
                if (!verboseCommands) {
                    Command.Fault ce = (Command.Fault) t;
                    Command c = ce.getCommand();
                    out.println(CommandContext.TRACE_PREFIX + c.toString());
                }
            }
            out.println(e.getMessage());
            out.flush();
            exit(RC_USER_ERROR);
        } catch (CommandParser.Fault e) {
            // occurs when parsing commands on command line or in command file
            Throwable t = e.getCause();
            if (t instanceof Command.Fault) {
                Command.Fault ce = (Command.Fault) t;
                Command c = ce.getCommand();
                out.println(CommandParser.TRACE_PREFIX + c.toString());
            }
            out.println(e.getMessage());
            out.flush();
            exit(RC_USER_ERROR);
        } catch (Error e) {
            e.printStackTrace(out);
            out.flush();
            exit(RC_INTERNAL_ERROR);
        } catch (RuntimeException e) {
            e.printStackTrace(out);
            out.flush();
            exit(RC_INTERNAL_ERROR);
        }
    }

    /**
     * Call System.exit, taking care to get permission from the
     * JavaTestSecurityManager, if it is installed.
     * @param exitCode an exit code to be passed to System.exit
     */
    private static void exit(int exitCode) {
        // If our security manager is installed, it won't allow a call of
        // System.exit unless we ask it nicely, pretty please, thank you.
        SecurityManager sc = System.getSecurityManager();
        if (sc instanceof JavaTestSecurityManager)
            ((JavaTestSecurityManager) sc).setAllowExit(true);
        System.exit(exitCode);
    }

    private CommandManager[] commandManagers;
    private MiniHelpManager helpManager;

    private static boolean initialized = false;

    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(Main.class);

    private static final int RC_OK = 0;
    private static final int RC_BATCH_TESTS_FAILED = 1;
    private static final int RC_BATCH_TESTS_ERROR = 2;
    private static final int RC_USER_ERROR = 3;
    private static final int RC_INTERNAL_ERROR = 4;


/**
     * A manager for command line help.
     */
    public static class MiniHelpManager extends CommandManager {
        /**
         * Create a HelpManager to manage the command line help
         * for a set of command managers.
         * The command managers should be set with setCommandManagers.
         */
        public MiniHelpManager() {
        }

        /**
         * Create a HelpManager to manage the command line help
         * for a set of command managers.
         * @param commandManagers the command managers for which
         * to give command line help
         */
        public MiniHelpManager(CommandManager[] commandManagers) {
            setCommandManagers(commandManagers);
        }

        public HelpTree.Node getHelp() {
            String[] helpOptions = {
                "help",
                "usage",
                "version"
            };
            return new HelpTree.Node(i18n, "help.cmd.opts", helpOptions);
        }

        /**
         * Parse a command (and any arguments it might take).
         * @param cmd the command to be parsed
         * @param argIter an iterator from which to get any arguments that
         * might be required by the option
         * @param ctx a context object to use while parsing the command
         * @return true if the command is recognized and successfully parsed,
         * and false otherwise
         */
        public boolean parseCommand(String cmd, ListIterator argIter, CommandContext ctx) {
            if (cmd.equalsIgnoreCase("help")
                || cmd.equalsIgnoreCase("usage")
                || cmd.equalsIgnoreCase("?")) {
                Vector v = new Vector();
                while (argIter.hasNext())
                    v.add(((String) (argIter.next())).toLowerCase());
                commandLineHelpFlag = true;
                commandLineHelpQuery = (String[]) (v.toArray(new String[v.size()]));
                return true;
            }

            if (cmd.equalsIgnoreCase("version")) {
                versionFlag = true;
                return true;
            }

            return false;
        }

        /**
         * Set the command managers for which to generate command line help.
         * @param commandManagers the command managers for which to generate command line help
         */
        public void setCommandManagers(CommandManager[] commandManagers) {
            this.commandManagers = commandManagers;
        }

        boolean isInfoRequired() {
            return (versionFlag || commandLineHelpFlag);
        }

        void showRequiredInfo(PrintWriter out, CommandContext ctx) {
            if (versionFlag)
                showVersion(out);

            if (commandLineHelpFlag)
                showCommandLineHelp(out);
        }

        /**
         * Print out info about the options accepted by the command line decoder.
         * @param out A stream to which to write the information.
         */
        void showCommandLineHelp(PrintWriter out) {
            HelpTree commandHelpTree = new HelpTree();

            Integer nodeIndent = Integer.getInteger("javatest.help.nodeIndent");
            if (nodeIndent != null)
                commandHelpTree.setNodeIndent(nodeIndent.intValue());

            Integer descIndent = Integer.getInteger("javatest.help.descIndent");
            if (descIndent != null)
                commandHelpTree.setDescriptionIndent(descIndent.intValue());

            // sort the command manager help nodes according to their name
            TreeMap tm = new TreeMap();
            for (int i = 0; i < commandManagers.length; i++) {
                HelpTree.Node n = commandManagers[i].getHelp();
                tm.put(n.getName(), n);
            }

            for (Iterator iter = tm.values().iterator(); iter.hasNext(); )
                commandHelpTree.addNode((HelpTree.Node) (iter.next()));

            // now add file types
            String[] fileTypes = {
                "ts",
                "wd",
                "jti"
            };
            HelpTree.Node filesNode = new HelpTree.Node(i18n, "help.cmd.files", fileTypes);
            commandHelpTree.addNode(filesNode);

            // now add syntax info
            String[] syntaxTypes = {
                "opts",
                "string",
                "atfile",
                "readfile",
                "encode"
            };
            HelpTree.Node syntaxNode = new HelpTree.Node(i18n, "help.cmd.syntax", syntaxTypes);
            commandHelpTree.addNode(syntaxNode);

            String progName =
                System.getProperty("program", "java " + Main.class.getName());

            try {
                WrapWriter ww = new WrapWriter(out);

                if (commandLineHelpQuery == null || commandLineHelpQuery.length == 0) {
                    // no keywords given
                    ww.write(i18n.getString("help.cmd.proto", progName));
                    ww.write("\n\n");
                    ww.write(i18n.getString("help.cmd.introHead"));
                    ww.write('\n');
                    commandHelpTree.writeSummary(ww);
                }
                else if (Arrays.asList(commandLineHelpQuery).contains("all")) {
                    // -help all
                    ww.write(i18n.getString("help.cmd.proto", progName));
                    ww.write("\n\n");
                    ww.write(i18n.getString("help.cmd.fullHead"));
                    ww.write('\n');
                    commandHelpTree.write(ww);
                }
                else {
                    HelpTree.Selection s = commandHelpTree.find(commandLineHelpQuery);
                    if (s != null)
                        commandHelpTree.write(ww, s);
                    else {
                        ww.write(i18n.getString("help.cmd.noEntriesFound"));
                        ww.write("\n\n");
                        ww.write(i18n.getString("help.cmd.summaryHead"));
                        ww.write('\n');
                        commandHelpTree.writeSummary(ww);
                    }
                }

                ww.write('\n');
                ww.write(i18n.getString("help.cmd.tail"));
                ww.write("\n\n");
                ww.write(i18n.getString("help.copyright.txt"));
                ww.write("\n\n");

                ww.flush();
            }
            catch (IOException e) {
                // should not happen, from PrintWriter
            }

        }

        /**
         * Show version information for JT Harness.
         * @param out the stream to which to write the information
         */
        void showVersion(PrintWriter out) {
            File classDir = Harness.getClassDir();
            String classDirPath =
                (classDir == null ? i18n.getString("help.version.unknown") : classDir.getPath());
            DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);

            Object[] versionArgs = {
                /*product*/ ProductInfo.getName(),
                /*version*/ ProductInfo.getVersion(),
                /*milestone*/ ProductInfo.getMilestone(),
                /*build*/ ProductInfo.getBuildNumber(),
                /*type of the product */ getHarnessType(),
                /*Installed in*/ classDirPath,
                /*Running on platform version*/ System.getProperty("java.version"),
                /*from*/ System.getProperty("java.home"),
                /*Built with*/ ProductInfo.getBuildJavaVersion(),
                /*Built on*/ df.format(ProductInfo.getBuildDate())
            };

            out.println(i18n.getString("help.version.txt", versionArgs));
            out.println(i18n.getString("help.copyright.txt"));
        }

        /**
         * @return a string that shortly describes functionality set included
         * into the harness.
         */
        String getHarnessType() {
            return i18n.getString("help.harnessType.lite.txt");
        }


        private CommandManager[] commandManagers;
        private boolean commandLineHelpFlag;
        private String[] commandLineHelpQuery;
        private boolean versionFlag;

    }

}
