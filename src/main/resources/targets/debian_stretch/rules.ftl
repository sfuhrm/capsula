#!/usr/bin/make -f
<#include "include-install.txt">

%:
	dh $@

override_dh_auto_clean:
${"\t"}${capsula.debian.cleanCommand}

override_dh_auto_build:
${"\t"}${capsula.debian.buildCommand}

override_dh_install: build
${"\t"}dh_clean
${"\t"}dh_installdirs
${"\t"}mkdir -p ${debian_destdir}
<#list capsula.install as entry>
<#-- install.mkdir -->
<#if entry.mkdir?has_content>
${"\t"}<@install cmd=entry.mkdir arguments="-d ${debian_destdir}/${entry.mkdir.to}"/>
</#if>
<#-- install.copy -->
<#if entry.copy?has_content>
${"\t"}<@install cmd=entry.copy arguments="-D ${entry.copy.from} ${debian_destdir}/${entry.copy.to}"/>
</#if>
<#-- install.run -->
<#if entry.run?has_content>
${"\t"}${entry.run.command}
</#if>
</#list>
