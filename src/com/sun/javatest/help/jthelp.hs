<?xml version='1.0' encoding='ISO-8859-1' ?>
<!--
  $Id$

  Copyright (c) 2001, 2009, Oracle and/or its affiliates. All rights reserved.
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
         "http://java.sun.com/products/javahelp/helpset_2_0.dtd">

<helpset version="2.0">

  <!-- title -->
  <title>JT Harness User's Guide</title>

  <!-- maps -->
  <maps>
     <homeID>home</homeID>
     <mapref location="default/map.xml"/>
  </maps>

  <!-- views -->
  <view mergetype="javax.help.AppendMerge">
    <name>TOC</name>
    <label>Contents</label>
    <type>javax.help.TOCView</type>
    <data>default/toc.xml</data>
  </view>

  <view>
    <name>Search</name>
    <label>Search</label>
    <type>javax.help.SearchView</type>
    <data>default/JavaHelpSearch</data>
  </view>

<!-- Change index to SortMerge when bug is fixed -->

  <view mergetype="javax.help.AppendMerge">
    <name>Index</name>
    <label>Index</label>
    <type>javax.help.IndexView</type>
    <data>default/index.xml</data>
  </view>

  <view mergetype="javax.help.SortMerge">
    <name>glossary</name>
    <label>Glossary</label>
    <type>javax.help.GlossaryView</type>
    <data>default/glossary.xml</data>
  </view>

  <view>
    <name>favorites</name>
    <label>Bookmarks</label>
    <type>javax.help.FavoritesView</type>
  </view>

  <presentation default="true" displayviewimages="false">
     <name>Main</name>
     <size width="900" height="600" />
     <location x="200" y="200" />
     <title>JT Harness User's Guide</title>
     <toolbar>
	<helpaction>javax.help.BackAction</helpaction>
	<helpaction>javax.help.ForwardAction</helpaction>
	<helpaction>javax.help.SeparatorAction</helpaction>
	<helpaction>javax.help.PrintAction</helpaction>
	<helpaction>javax.help.PrintSetupAction</helpaction>
	<helpaction>javax.help.SeparatorAction</helpaction>
	<helpaction>javax.help.FavoritesAction</helpaction>
     </toolbar>
  </presentation>
</helpset>
