<#list jpkg.versions as version>
${jpkg.debian.packageName} (${version.version}) unstable; urgency=medium

<#list version.changes as change>
  * ${change}
</#list>

 -- ${jpkg.maintainer.name} <${jpkg.maintainer.email}>  ${version.date?string('EEE, dd MMM yyyy HH:mm:ss ZZZZ')}

</#list>

