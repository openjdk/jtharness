<?xml version='1.0' encoding='ISO-8859-1' ?>
<!--
  $Id$

  Copyright (c) 2004, 2009, Oracle and/or its affiliates. All rights reserved.
  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 
  This code is free software; you can redistribute it and/or modify it
  under the terms of the GNU General Public License version 2 only, as
  published by the Free Software Foundation.  Oracle designates this
  particular file as subject to the "Classpath" exception as provided
  by Oracle in the LICENSE file that accompanied this code.
 
  This code is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  version 2 for more details (a copy is included in the LICENSE file that
  accompanied this code).
 
  You should have received a copy of the GNU General Public License version
  2 along with this work; if not, write to the Free Software Foundation,
  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 
  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
  or visit www.oracle.com if you need additional information or have any
  questions.
-->

<!DOCTYPE helpset
  PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN"
         "http://java.sun.com/products/javahelp/helpset_1_0.dtd">

<helpset version="2.0">

  <!-- title -->
  <title>JT Harness Agent Online Help</title>

  <!-- maps -->
  <maps>
     <mapref location="default/map.xml" />
  </maps>

  <!-- views -->
  <view mergetype="javax.help.AppendMerge">
    <name>TOC</name>
    <label>Contents</label>
    <type>javax.help.TOCView</type>
    <data>default/toc.xml</data>
  </view>

  <view mergetype="javax.help.AppendMerge">
    <name>index</name>
    <label>Index</label>
    <type>javax.help.IndexView</type>
    <data>default/index.xml</data>
  </view>

  <view mergetype="javax.help.UniteAppendMerge">
    <name>glossary</name>
    <label>Glossary</label>
    <type>javax.help.GlossaryView</type>
    <data>default/glossary.xml</data>
  </view>

  <view mergetype="javax.help.NoMerge">
    <name>favorites</name>
    <label>Booksmarks</label>
    <type>javax.help.FavoritesView</type>
  </view>
<!--
  <view mergetype="javax.help.NoMerge">
    <name>Search</name>
    <label>Search</label>
    <type>javax.help.SearchView</type>
    <data>default/JavaHelpSearch</data>
  </view>
-->
</helpset>
