#!/usr/bin/make -f
<#include "include-install.txt">

install:
	mkdir -p $(DESTDIR)
<#list capsula.install as entry>
<#-- install.mkdir -->
<#if entry.mkdir?has_content>
${"\t"}<@install cmd=entry.mkdir arguments="-d $(DESTDIR)/${entry.mkdir.to}"/>
</#if>
<#-- install.copy -->
<#if entry.copy?has_content>
${"\t"}<@install cmd=entry.copy arguments="-D ${entry.copy.from} $(DESTDIR)/${entry.copy.to}"/>
</#if>
<#-- install.run -->
<#if entry.run?has_content>
${"\t"}${entry.run.command}
</#if>
</#list>
