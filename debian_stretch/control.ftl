Source: ${capsula.debian.packageName}
Section: ${capsula.debian.section}
Priority: ${capsula.debian.priority}
Maintainer: ${capsula.maintainer.name} <${capsula.maintainer.email}>
Build-Depends: debhelper (>=10), openjdk-8-jdk-headless, maven-debian-helper
Standards-Version: 3.9.8
Homepage: ${capsula.homepage}

Package: ${capsula.debian.packageName}
Architecture: all
Depends: ${r"${shlibs:Depends}"}, ${r"${misc:Depends}"}, openjdk-8-jdk-headless
Description: ${capsula.shortSummary}
<#list capsula.longDescription as line>
<#if line?has_content>
 ${line}
<#else>
 .
</#if>
</#list>
