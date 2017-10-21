Source: ${capsula.debian.packageName}
Section: ${capsula.debian.section}
Priority: ${capsula.debian.priority}
Maintainer: ${capsula.maintainer.name} <${capsula.maintainer.email}>
Build-Depends: debhelper (>=10)
Standards-Version: 3.9.8
Homepage: ${capsula.homepage}

Package: ${capsula.debian.packageName}
Architecture: any
Depends: ${r"${shlibs:Depends}"}, ${r"${misc:Depends}"}
Description: ${capsula.shortSummary}
<#list capsula.longDescription as line>
<#if line?has_content>
 ${line}
<#else>
 .
</#if>
</#list>

