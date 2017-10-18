<#list capsula.versions as version>
${capsula.debian.packageName} (${version.version}) unstable; urgency=medium

<#list version.changes as change>
  * ${change}
</#list>

 -- ${capsula.maintainer.name} <${capsula.maintainer.email}>  ${version.date?string('EEE, dd MMM yyyy HH:mm:ss ZZZZ')}

</#list>

