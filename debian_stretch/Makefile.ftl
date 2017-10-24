#!/usr/bin/make -f

install:
	mkdir -p $(DESTDIR)
<#list capsula.install as entry>
<#if entry.copy.mode?has_content>
<#assign modeStatement = "--mode="+entry.copy.octal+" ">
<#else>
<#assign modeStatement = "">
</#if>
<#if entry.copy.owner?has_content>
<#assign ownerStatement = "--owner="+entry.copy.owner+" ">
<#else>
<#assign ownerStatement = "">
</#if>
<#if entry.copy.group?has_content>
<#assign groupStatement = "--group="+entry.copy.group+" ">
<#else>
<#assign groupStatement = "">
</#if>
<#if entry.copy?has_content>
	if [ -f ${entry.copy.from} ]; then install ${modeStatement} ${ownerStatement} ${groupStatement} -D ${entry.copy.from} $(DESTDIR)/${entry.copy.to}; fi
	if [ -d ${entry.copy.from} ]; then install ${modeStatement} ${ownerStatement} ${groupStatement} -d ${entry.copy.from} $(DESTDIR)/${entry.copy.to}; fi
</#if>
</#list>
