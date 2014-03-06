/*
 * $Id$
 *
 * Copyright (c) 2006, 2009, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jct.utils.i18ncheck;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Pattern;

import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static com.sun.jct.utils.i18ncheck.XMLNode.*;
import com.sun.jct.utils.i18ncheck.XMLNode.Action;
import com.sun.jct.utils.i18ncheck.XMLNode.Arg;
import com.sun.jct.utils.i18ncheck.XMLNode.ArgsLength;
import com.sun.jct.utils.i18ncheck.XMLNode.Case;
import com.sun.jct.utils.i18ncheck.XMLNode.Cond;
import com.sun.jct.utils.i18ncheck.XMLNode.GoTo;
import com.sun.jct.utils.i18ncheck.XMLNode.IntArg;
import com.sun.jct.utils.i18ncheck.XMLNode.MethodName;
import com.sun.jct.utils.i18ncheck.XMLNode.Rule;
import com.sun.jct.utils.i18ncheck.XMLNode.Static;
import com.sun.jct.utils.i18ncheck.XMLNode.StringArg;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class I18NStaticChecker {

    /**
     *  The list of patterns to search methods which localize strings.
     */
    protected HashMap<String, Rule> patterns = new HashMap();

    protected Vector<Static> vstatic = new Vector();

    protected Vector<Rule> newRules = new Vector();


    /**
     * The list of source files to check.
     */
    protected ArrayList<File> sources = new ArrayList<File>();
    /**
     * The localization properties.
     */
    protected Properties properties;
    /**
     * Property keys, collected during dynamic search
     */
    protected HashSet<String> dynKeys = new HashSet();


    /**
     * Property keys, collected during static search
     */
    protected HashSet<String> keys;

    /**
     * Property keys, collected during checking and not found in resource bundle
     */
    protected HashSet<String> rqnd;

    /**
     * Properties from resource bundle, which were not found during checking
     */
    protected HashSet<String> dfnr;

    protected static Pattern dynStrPtrn;
    static {
        dynStrPtrn = Pattern.compile(".*\"(\\s)*(\\+).*");
    }

    public void setFileSet(ArrayList<File> sources) {
        this.sources = sources;
    }
    public void setDynKeys(HashSet<String> dynKeys) {
        this.dynKeys = dynKeys;
    }
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    private void resetChecker() {
        keys = new HashSet();
        rqnd = new HashSet();
        dfnr = new HashSet();
    }

    public void checkI18N() {

        resetChecker();
        //searching for keys
        for (File f : sources) {
            keys.addAll(getLocalizedStrings(f));

            if (vstatic.size() > 0) {
                String path = f.getPath();
                for (Static s : vstatic) {
                    boolean sfound = false;
                    for (String fs : s.fileset) {
                        if (path.contains(fs)) {
                            performStatic(s);
                            sfound = true;
                            break;
                        }
                    }
                    if (sfound) {
                        break;
                    }
                }
            }
        }
    }

    static boolean stringArg(String arg) {
        Matcher m = dynStrPtrn.matcher(arg);
        return arg.startsWith("\"") && arg.endsWith("\"") && !m.matches();
    }

    private static boolean intArg(String arg) {
        try {
            Integer.parseInt(arg);
            return true;
        } catch (NumberFormatException e){
            return false;
        }
    }

    static String cutQuotes(String str) {
        if (str.startsWith("\""))
            str = str.substring(1);
        if (str.endsWith("\""))
            str = str.substring(0, str.length() - 1);
        return str;
    }

    private static String[] toStringArray(List<?> list) {
        int i = 0;
        String[] str = new String[list.size()];
        for (Object o : list) {
            str[i++] = o.toString();
        }

        return str;
    }

    /**
     * We use reflection here to overcome differences between
     * class names in JDK5 and JDK6 compiler API
     */
    private Set getLocalizedStrings(File source) {
        try {
            Context context = new Context();
            JavaCompiler compiler = new JavaCompiler(context);
            Object parsedTree = compiler.parse(source.getAbsolutePath());
            HashSet<String> keys = new HashSet();
            Class parsedTreeClass = parsedTree.getClass();
            Class visitorClass;
            Class stdVisitorClass;
            String visitorPckgName = "com.sun.jct.utils.i18ncheck.";
            String stdPckgName = "com.sun.tools.javac.tree.";
            if (I18NStaticMain.java_version == I18NStaticMain.JAVA_5) {
                visitorClass = Class.forName(visitorPckgName + "I18NVisitor15");
                stdVisitorClass = Class.forName(stdPckgName + "Tree$Visitor");
            }
            else {
                visitorClass = Class.forName(visitorPckgName + "I18NVisitor16");
                stdVisitorClass = Class.forName(stdPckgName + "JCTree$Visitor");
            }
            Method accept = parsedTreeClass.getMethod("accept", stdVisitorClass);
            Constructor constr = visitorClass.getConstructor
                    (String.class, this.getClass(), keys.getClass());
            Object visitor = constr.newInstance(source.getAbsolutePath(), this, keys);
            accept.invoke(parsedTree, visitor);
            return keys;
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    public void writeResult(File dir) {
        File reqdFile = new File(dir.getPath() + File.separator + "static.reqd");
        writeSet(reqdFile, keys);

        keys.addAll(dynKeys);
        File reqdMerged = new File(dir.getPath() + File.separator + "merged.reqd");
        writeSet(reqdMerged, keys);

        //collecting keys, not-existent in resource bundle
        for (String str : keys) {
            if (!properties.containsKey(str)) {
                rqnd.add(str);
            }
        }

        //collecting keys from resource bundle, which are not used
        for (Object o : properties.keySet()) {
            String str = (String)o;
            if (!keys.contains(str)) {
                dfnr.add(str);
            }
        }

        File rqndFile = new File(dir.getPath() + File.separator + "difference.rqnd");
        writeSet(rqndFile, rqnd);

        File dfnrFile = new File(dir.getPath() + File.separator + "difference.dfnr");
        writeSet(dfnrFile, dfnr);

        File defdFile = new File(dir.getPath() + File.separator + "properties.defd");
        Set<String> props = new HashSet();
        for (Object o : properties.keySet()) {
            props.add((String)o);
        }
        writeSet(defdFile, props);
    }

    private void writeSet(File f, Set<String> s) {
        if (f.exists() && f.canWrite()) {
            f.delete();
        }

        if (s.size() > 0) {
            try {
                FileOutputStream fout = new FileOutputStream(f);
                BufferedWriter bout = new BufferedWriter(new OutputStreamWriter(fout));
                String[] sortedKeys = s.toArray(new String[0]);
                Arrays.sort(sortedKeys);
                for (String key : sortedKeys) {
                    bout.write(key);
                    bout.newLine();
                }
                bout.flush();http://community.livejournal.com/fidonet/106237.html
                    bout.close();
            } catch (FileNotFoundException e) {} catch (IOException ie) {}
        }
    }

    public void prepareXML(File rulesFile) {
        try {
            DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
            DocumentBuilder parser = bf.newDocumentBuilder();
            Document dom = parser.parse(rulesFile);

            Element elem = dom.getDocumentElement();
            NodeList l = elem.getElementsByTagName("rule");
            Rule[] rules = new Rule[l.getLength()];
            for (int i = 0; i < l.getLength(); i++) {
                rules[i] = new Rule(l.item(i));
                patterns.put(rules[i].id, rules[i]);
            }

            l = elem.getElementsByTagName("new");
            for (int i = 0; i < l.getLength(); i++) {
                newRules.add(new Rule(l.item(i)));
            }

            l = elem.getElementsByTagName("static");
            for (int i = 0; i < l.getLength(); i++) {
                vstatic.add(new Static(l.item(i)));
            }

        } catch (Exception e) {
            System.out.println("XML Exception");
            e.printStackTrace();
        }
    }

    private void performAction(Action action, String uiKey,
                                                        PerfEnvironment env) {
        for (GoTo gt : action.gotos) {
            PerfEnvironment newEnv = new PerfEnvironment(env);
            newEnv.args = gt.buildArgs(env.args);
            Rule rule = patterns.get(gt.ruleID);
            performRule(rule, newEnv);
        }

        if (action.isEmpty) {
            keys.add(uiKey);
        }

        if (!action.value.trim().equals("")) {
            String[] suffs = split(action.value);
            for (String suff : suffs) {
                suff = suff.trim();
                if (action.addKey) {
                    keys.add(uiKey + suff);
                }
                else {
                    keys.add(suff);
                }
            }
        }
    }

    private void performStatic(Static sstatic) {
        for (String key : sstatic.skeys) {
            keys.add(key);
        }
    }

    public void performRule(Rule rule, Object tree, String filename) {
        PerfEnvironment env = new PerfEnvironment(tree, filename);
        performRule(rule, env);
    }

    private void performRule(Rule rule, PerfEnvironment env) {
        String uiKey="";
        if (rule.uiKeyNumb != null) {
            for (int indx : rule.uiKeyNumb) {
                if (indx > env.args.length - 1) {
                    return;
                }
                if (!stringArg(env.args[indx]))
                    return;
                uiKey += cutQuotes(env.args[indx]);
                uiKey += ".";
            }
            uiKey = uiKey.substring(0, uiKey.length() - 1);
        }

        if (rule.exception != null) {
            for (String f : rule.exception.fileset) {
                if (env.filename.contains(f)) {
                    performException(rule.exception, uiKey, env);
                    return;
                }
            }
        }

        if (rule.common != null) {
            performCommon(rule.common, uiKey, env);
        }
        for (Case ccase : rule.cases) {
            performCase(ccase, env, uiKey);
        }

    }

    private void performException(EException exception, String uiKey,
                                                    PerfEnvironment env) {
        if (exception.action != null) {
            performAction(exception.action, uiKey, env);
        }
    }

    private void performCommon(Common common, String uiKey,
                                                    PerfEnvironment env) {
        for (Action a : common.actions) {
            performAction(a, uiKey, env);
        }
    }

    private void performCase(Case ccase, PerfEnvironment env, String uiKey) {
        boolean res = true;
        for (Cond cond : ccase.conds) {
            res = res && resolveCond(cond, env);
            if (!res) { // &&
                break;
            }
        }
        if (res) {
            for (Action action : ccase.actions) {
                performAction(action, uiKey, env);
            }
            for (Case childCase : ccase.childCases) {
                performCase(childCase, env, uiKey);
            }
        }
    }

    private boolean resolveCond(Cond cond, PerfEnvironment env) {
        Vector<Boolean> results = new Vector();
        boolean result = true;
        boolean argLRes = true;

        for (ArgsLength al : cond.argsLengths) {
            boolean res = resolveArgsLength(al, env.args);
            results.add(res);
            if (cond.type == Cond.TYPE_AND)
                argLRes = argLRes && res;
        }

        if (!argLRes) {
            if (cond.isNegative) {
                return true;
            }
            return false;
        }

        for (Cond childCond : cond.conds) {
            results.add(resolveCond(childCond, env));
        }
        for (MethodName mn : cond.methodNames) {
            results.add(resolveMethodName(mn, env.methName));
        }
        for (Arg arg : cond.args) {
            results.add(resolveArg(arg, env.args));
        }
        for (StringArg sa : cond.strArgs) {
            results.add(stringArg(env.args[sa.index]));
        }
        for (IntArg ia : cond.intArgs) {
            results.add(intArg(env.args[ia.index]));
        }
        switch (cond.type) {
            case Cond.TYPE_AND :
                for (boolean res : results) {
                    result = result && res;
                }
                break;
            case Cond.TYPE_OR :
                result = false;
                for (boolean res : results) {
                    result = result | res;
                }
                break;
            default :
                break;
        }

        if (cond.isNegative) {
            result = !result;
        }

        return result;
    }

    private boolean resolveMethodName(MethodName mn, String methodName) {
        boolean result;
        if (mn.type == mn.TYPE_EQUALS) {
            result = methodName.equals(mn.value);
        }
        else {
            result = methodName.contains(mn.value);
        }
        if (mn.isNegative) {
            result = !result;
        }

        return result;
    }

    private boolean resolveArg(Arg arg, String[] args) {
        boolean result = true;
        try {
            String sarg = cutQuotes(args[arg.index]);
        switch (arg.type) {
            case (Arg.TYPE_EQUALS):
                result = sarg.equals(arg.value);
                break;
            case (Arg.TYPE_CONTAINS):
                result = sarg.contains(arg.value);
            default:
                break;
        }

        if (arg.isNegative) {
            result = !result;
        }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        return result;
    }

    private boolean resolveArgsLength(ArgsLength argsL, String[] args) {
        boolean result = true;
        switch (argsL.type) {
            case (ArgsLength.TYPE_EQUAL):
                result = args.length == argsL.value;
                break;
            case (ArgsLength.TYPE_GREATER):
                result = args.length > argsL.value;
                break;
            case (ArgsLength.TYPE_LESS):
                result = args.length > argsL.value;
                break;
            default:
                break;
        }

        if (argsL.isNegative) {
            result = !result;
        }
        return result;
    }

    private boolean resolveString(Node node, String str) {
        boolean result = true;

        boolean isNegative = false;
        NamedNodeMap attrs = node.getAttributes();
        if (attrs != null && (attrs.getNamedItem("negative") != null)) {
            isNegative = attrs.getNamedItem("negative").getNodeValue().equals("true");
        }

        String value = "";
        NodeList childs = node.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node child = childs.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                value = child.getNodeValue();
                value = value.trim();
                break;
            }
        }

        String tagName = node.getNodeName();
        if (tagName.equals("equals")) {
            result = str.equals(value);
        } else if (tagName.equals("contains")) {
            result = str.contains(value);
        }

        if (isNegative) {
            result = !result;
        }
        return result;
    }

    private static class PerfEnvironment {
        public String methName;
        public String[] args;
        public String filename;

        public PerfEnvironment(PerfEnvironment other) {
            this.methName = other.methName;
            this.args = other.args;
            this.filename = other.filename;
        }

        /**
         * We use reflection here to overcome differences between
         * class names in JDK5 and JDK6 compiler API
         */
        public PerfEnvironment(Object tree, String filename) {
            this.filename = filename;

            Class c = tree.getClass();
            try {
                Field argsF = c.getField("args");
                List l = (List)argsF.get(tree);
                args = toStringArray(l);

                String className = c.getSimpleName();
                if (className.contains("Apply") ||
                        className.contains("JCMethodInvocation")) {
                    Field meth = c.getField("meth");
                    Object o = meth.get(tree);
                    methName = o.toString();
                }
                else if (className.contains("NewClass")) {
                    Field meth = c.getField("clazz");
                    Object o = meth.get(tree);
                    methName = o.toString();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    static String[] split(String text) {
        String[] lines = new String[0];
        Vector<String> res = new Vector();

        lines = text.split(System.getProperty("line.separator"));
        for (String line : lines) {
            String[] keys = line.split(" ");
            for (String k : keys) {
                res.add(k.trim());
            }
        }
        return res.toArray(new String[0]);
    }
    static String retrieveText (Element elem) {
        String value = "";
        NodeList childs = elem.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node child = childs.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                value = child.getNodeValue();
                value = value.trim();
                break;
            }
        }
        return value;
    }

}
