/*
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

import java.util.HashSet;
import java.util.Vector;
import java.util.regex.Pattern;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static com.sun.jct.utils.i18ncheck.I18NStaticChecker.cutQuotes;
import static com.sun.jct.utils.i18ncheck.I18NStaticChecker.stringArg;
import static com.sun.jct.utils.i18ncheck.I18NStaticChecker.split;
import static com.sun.jct.utils.i18ncheck.I18NStaticChecker.retrieveText;

public class XMLNode {

     static class Rule {
        public String id;
        public String regexp;
        public Pattern pattern;
        public int[] uiKeyNumb;
        public Case[] cases;
        public Common common;
        public EException exception;

        public Rule(Node rule) throws Exception {
            NodeList childs = rule.getChildNodes();
            Vector<Case> vcases = new Vector<>();
            for (int i = 0; i < childs.getLength(); i++) {
                Node child = childs.item(i);
                String name = child.getNodeName();
                if (name == null) {
                    continue;
                }
                else if (name.equals("uikey")) {
                    String[] numbs = child.getAttributes().item(0).getNodeValue().split(" ");
                    uiKeyNumb = new int[numbs.length];
                    for (int j = 0; j < numbs.length; j++) {
                        uiKeyNumb[j] = Integer.parseInt(numbs[j]);
                    }
                }
                else if (name.equals("common")) {
                    common = new Common(child);
                }
                else if (name.equals("exception")) {
                    exception = new EException(child);
                }
                else if (name.equals("case")) {
                    Case ccase = new Case(child);
                    vcases.add(ccase);
                }
            }
            cases = new Case[vcases.size()];
            cases = vcases.toArray(cases);

            Element elem = (Element)rule;
            id = elem.getAttributeNode("id").getValue();

            regexp = elem.getAttributeNode("pattern").getValue();
            pattern = Pattern.compile(regexp);
        }
    }

     static class EException {
        public String[] fileset;
        public Action action;

        public EException(Node exception) throws Exception {
            NodeList childs = exception.getChildNodes();
            for (int i = 0; i < childs.getLength(); i++) {
                Node child = childs.item(i);
                String name = child.getNodeName();
                if (name.equals("fileset")) {
                    String[] files = split(retrieveText((Element)child));
                    fileset = new String[files.length];
                    int j = 0;
                    for (String file : files) {
                        fileset[j++] = file.trim();
                    }
                } else if (name.equals("action")) {
                    action = new Action(child);
                }
            }
        }
    }

     static class Common {
        public Action[] actions;

        public Common(Node common) throws Exception {
            Element elem  = (Element)common;
            NodeList list = elem.getElementsByTagName("action");
            actions = new Action[list.getLength()];
            for (int i = 0; i < list.getLength(); i++) {
                actions[i] = new Action(list.item(i));
            }
        }
    }

     static class Case {
        public Cond[] conds;
        public Action[] actions;
        public Case[] childCases;

        public Case(Node ccase) throws Exception {

            HashSet<String> tags = new HashSet<>();
            tags.add("case");
            tags.add("action");
            tags.add("cond");

            final Vector<Case> ca = new Vector<>();
            final Vector<Action> a = new Vector<>();
            final Vector<Cond> c = new Vector<>();

            getChildsByTags(ccase, tags, new Callback() {
                public void construct(String tag, Node node) throws Exception {
                    if (tag.equals("case")) {
                        ca.add(new Case(node));
                    }
                    else if (tag.equals("action")) {
                        a.add(new Action(node));
                    }
                    else if (tag.equals("cond")) {
                        c.add(new Cond(node));
                    }
                }
                public void finish() {
                    childCases = ca.toArray(new Case[0]);
                    actions = a.toArray(new Action[0]);
                    conds = c.toArray(new Cond[0]);
                }
            });
        }
    }

     static class Cond {
        public boolean isNegative;

        public MethodName[] methodNames;
        public ArgsLength[] argsLengths;
        public Arg[] args;
        public Cond[] conds;
        public StringArg[] strArgs;
        public IntArg[] intArgs;

        public int type = 0;
        public static final int TYPE_AND = 0;
        public static final int TYPE_OR = 1;

        public Cond(Node cond) throws Exception {

            HashSet<String> tags = new HashSet<>();
            tags.add("methodName");
            tags.add("argslength");
            tags.add("arg");
            tags.add("cond");
            tags.add("stringarg");
            tags.add("intarg");

            final Vector<MethodName> mn = new Vector<>();
            final Vector<ArgsLength> al = new Vector<>();
            final Vector<Arg> a = new Vector<>();
            final Vector<Cond> c = new Vector<>();
            final Vector<StringArg> sa = new Vector<>();
            final Vector<IntArg> ia = new Vector<>();

            getChildsByTags(cond, tags, new Callback() {
                public void construct(String tag, Node node) throws Exception {
                    if (tag.equals("methodName")) {
                        mn.add(new MethodName(node));
                    }
                    else if (tag.equals("argslength")) {
                        al.add(new ArgsLength(node));
                    }
                    else if (tag.equals("arg")) {
                        a.add(new Arg(node));
                    }
                    else if (tag.equals("cond")) {
                        c.add(new Cond(node));
                    }
                    else if (tag.equals("stringarg")) {
                        sa.add(new StringArg(node));
                    }
                    else if (tag.equals("intarg")) {
                        ia.add(new IntArg(node));
                    }
                }
                public void finish() {
                    methodNames = mn.toArray(new MethodName[0]);
                    argsLengths = al.toArray(new ArgsLength[0]);
                    args = a.toArray(new Arg[0]);
                    conds = c.toArray(new Cond[0]);
                    strArgs = sa.toArray(new StringArg[0]);
                    intArgs = ia.toArray(new IntArg[0]);
                }
            });


            Element elem = (Element)cond;
            Attr neg = elem.getAttributeNode("negative");
            if (neg != null) {
                isNegative = neg.getValue().equals("true");
            }

            Attr atype = elem.getAttributeNode("type");
            if (atype != null) {
                type = atype.getValue().equals("or") ? TYPE_OR : TYPE_AND;
            }
        }
    }

     static interface Callback {
        public void construct(String tag, Node node) throws Exception;
        public void finish();
    }

     static class StringArg {
        public int index;

        public StringArg(Node node) {
            Element elem = (Element)node;
            Attr attr = elem.getAttributeNode("index");
            index = Integer.parseInt(attr.getValue());
        }
    }

     static class IntArg {
        public int index;

        public IntArg(Node node) {
            Element elem = (Element)node;
            Attr attr = elem.getAttributeNode("index");
            index = Integer.parseInt(attr.getValue());
        }
    }


     static class MethodName {
        public static int TYPE_EQUALS = 0;
        public static int TYPE_CONTAINS = 1;

        public int type;
        public boolean isNegative;
        public String value;

        public MethodName(Node mn) throws Exception {
            Element elem = (Element)mn;
            Attr attr = elem.getAttributeNode("type");
            if (attr == null) {
                throw new Exception("You must specify \"type\" attribute!\n to \"equals\" or \"contains\"");
            }
            else {
                if (attr.getValue().equals("equals"))
                    type = TYPE_EQUALS;
                else if (attr.getValue().equals("contains"))
                    type = TYPE_CONTAINS;
                else
                    throw new Exception("You must specify \"type\" attribute!\n to \"equals\" or \"contains\"");
            }

            attr = elem.getAttributeNode("negative");
            if (attr != null) {
                isNegative = attr.getValue().equals("true");
            }

            value = retrieveText(elem);
        }
    }
     static class ArgsLength {

        public static final int TYPE_LESS = 0;
        public static final int TYPE_EQUAL = 1;
        public static final int TYPE_GREATER = 2;

        public boolean isNegative;
        public int type;
        public int value;

        public ArgsLength(Node argsL) {
            Element elem = (Element)argsL;
            for (int i = 0; i < elem.getAttributes().getLength(); i ++) {
                String stype = elem.getAttributes().item(i).getNodeName();
                if (stype.equals("equals")) {
                    type = TYPE_EQUAL;
                }
                else if (stype.equals("lessthan")) {
                    type = TYPE_LESS;
                }
                if (stype.equals("greaterthan")) {
                    type = TYPE_GREATER;
                }
                Attr attr = elem.getAttributeNode(stype);
                value = Integer.parseInt(attr.getValue());
                break;
            }

            Attr attr = elem.getAttributeNode("negative");
            if (attr != null) {
                isNegative = attr.getValue().equals("true");
            }
        }
    }
     static class Arg {
        public static final int TYPE_EQUALS = 0;
        public static final int TYPE_CONTAINS = 1;

        public int type;
        public boolean isNegative;
        public int index;
        public String value;

        public Arg(Node arg) throws Exception{
            Element elem = (Element)arg;
            Attr attr = elem.getAttributeNode("type");
            if (attr == null) {
                throw new Exception("You must specify \"type\" attribute!\n to \"equals\" or \"contains\"");
            }
            else {
                if (attr.getValue().equals("equals"))
                    type = TYPE_EQUALS;
                else if (attr.getValue().equals("contains"))
                    type = TYPE_CONTAINS;
                else
                    throw new Exception("You must specify \"type\" attribute!\n to \"equals\" or \"contains\"");
            }

            attr = elem.getAttributeNode("negative");
            if (attr != null) {
                isNegative = attr.getValue().equals("true");
            }

            attr = elem.getAttributeNode("index");
            if (attr != null) {
                index = Integer.parseInt(attr.getValue());
            }
            else {
                throw new Exception("You must specify argument index!");
            }
            value = retrieveText(elem);

        }
    }

     static class Action {
        public boolean isEmpty = false;
        public boolean addKey = true;
        public String value;
        public GoTo[] gotos;

        public Action(Node action) throws Exception {
            Element elem = (Element)action;

            NodeList list = elem.getElementsByTagName("goto");
            gotos = new GoTo[list.getLength()];
            for (int i = 0; i < list.getLength(); i++) {
                gotos[i] = new GoTo(list.item(i));
            }

            Attr attr = elem.getAttributeNode("empty");
            if (attr != null && attr.getValue().equals("true"))
                isEmpty = true;

            attr = elem.getAttributeNode("addkey");
            if (attr != null && attr.getValue().equals("no"))
                addKey = false;

            value = retrieveText(elem);
        }
    }

     static class GoTo {
        public String ruleID;
        public GoToArg[] args;

        public GoTo(Node goTo) throws Exception {
            Element elem = (Element)goTo;

            Attr attr = elem.getAttributeNode("ruleid");
            if (attr != null) {
                ruleID = attr.getValue();
            }
            else {
                throw new Exception("You must specify \"ruleid\" attribute!");
            }

            NodeList list = elem.getElementsByTagName("arg");
            args = new GoToArg[list.getLength()];
            for (int i = 0; i < list.getLength(); i++) {
                args[i] = new GoToArg(list.item(i));
            }
        }

        public String[] buildArgs(String[] oldArgs) {
            String[] newArgs = new String[args.length];
            int i = 0;
            for (GoToArg arg : args) {
                String s = "";
                for (Object elem : arg.elems) {
                    if (elem instanceof Integer) {
                        int index = (Integer)elem;
                        if (stringArg(oldArgs[index])) {
                            s += cutQuotes(oldArgs[index]);
                        }
                        else {
                            s += oldArgs[index];
                        }
                    }
                    else {
                        s += (String)elem;
                    }
                    s += ".";
                }
                s = s.substring(0, s.length() - 1);
                newArgs[i] = "\"" + s + "\"";
                i++;
            }
            return newArgs;
        }
    }

     static class GoToArg {
        public Vector<Object> elems;

        public GoToArg(Node arg) throws Exception {
            elems = new Vector<>();
            Element elem = (Element)arg;
            String content = retrieveText(elem);

            if (content.trim().equals("")) {
                elems.add("");
                return;
            }

            String suff = content;
            while (suff.length() > 0) {
                suff = suff.trim();
                int afterToken = suff.indexOf(' ');
                if (afterToken == -1) {
                    afterToken = suff.length();
                }
                String token = suff.substring(0, afterToken);
                suff = suff.substring(afterToken);
                try {
                    elems.add(Integer.parseInt(token));
                }
                catch (NumberFormatException e) {
                    if (token.startsWith("\"")) {
                        elems.add(token.substring(1, token.length() - 1));
                    }
                }
            }
        }
    }

     static class Static {
        public String[] fileset;
        public String[] skeys;

        public Static(Node node) {
            Element elem = (Element)node;

            NodeList list = elem.getElementsByTagName("fileset");
            fileset = split(retrieveText((Element)list.item(0)));

            list = elem.getElementsByTagName("keys");
            skeys = split(retrieveText((Element)list.item(0)));
            for (int i = 0; i < fileset.length; i++) {
                fileset[i] = fileset[i].trim();
            }
            for (int i = 0; i < skeys.length; i++) {
                skeys[i] = skeys[i].trim();
            }
        }
    }

    private static void getChildsByTags(Node node,
                                    HashSet<String> tags, Callback callback) throws Exception {
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node child = list.item(i);
            String tag = child.getNodeName();
            if (tags.contains(tag)) {
                callback.construct(tag, child);
            }
        }
        callback.finish();
    }

}
