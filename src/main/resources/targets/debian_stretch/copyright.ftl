Format-Specification: http://svn.debian.org/wsvn/dep/web/deps/dep5.mdwn?op=file&rev=135
Name: ${capsula.debian.packageName}
Maintainer: ${capsula.maintainer.name} <${capsula.maintainer.email}>
Source: ${capsula.git.gitUrl}

Copyright: 2017 ${capsula.author.name} <${capsula.author.email}>
License: ${capsula.license.licenseName}

License: ${capsula.license.licenseName}
<#if capsula.license.debianName?has_content>
 On Debian systems, the full text of the ${capsula.license.licenseName}
 can be found in the file `/usr/share/common-licenses/${capsula.license.debianName}'.
<#else>
<#list capsula.license.licenseText as line>
<#if line?has_content>
 ${line}
<#else>
 .
</#if>
</#list>
</#if>

