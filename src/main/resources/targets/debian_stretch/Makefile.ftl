#!/usr/bin/make -f

install:
	mkdir -p $(DESTDIR)
<#list capsula.install as entry>
<#-- install.mkdir -->
<#if entry.mkdir?has_content>
<#assign modeStatement = "">
<#if entry.mkdir.mode?has_content>
<#assign modeStatement = "--mode="+entry.mkdir.octal+" ">
</#if>
<#assign ownerStatement = "">
<#if entry.mkdir.owner?has_content>
<#assign ownerStatement = "--owner="+entry.mkdir.owner+" ">
</#if>
<#assign groupStatement = "">
<#if entry.mkdir.group?has_content>
<#assign groupStatement = "--group="+entry.mkdir.group+" ">
</#if>
	install -d ${modeStatement} ${ownerStatement} ${groupStatement} $(DESTDIR)/${entry.mkdir.to}
</#if>
<#-- install.copy -->
<#if entry.copy?has_content>
<#assign modeStatement = "">
<#if entry.copy.mode?has_content>
<#assign modeStatement = "--mode="+entry.copy.octal+" ">
</#if>
<#assign ownerStatement = "">
<#if entry.copy.owner?has_content>
<#assign ownerStatement = "--owner="+entry.copy.owner+" ">
</#if>
<#assign groupStatement = "">
<#if entry.copy.group?has_content>
<#assign groupStatement = "--group="+entry.copy.group+" ">
</#if>
	install ${modeStatement} ${ownerStatement} ${groupStatement} -D ${entry.copy.from} $(DESTDIR)/${entry.copy.to}
</#if>
<#-- install.run -->
<#if entry.run?has_content>
	${entry.run.command}
</#if>
</#list>
