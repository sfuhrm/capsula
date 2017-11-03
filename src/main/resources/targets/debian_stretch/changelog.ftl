<#list capsula.versions as version>
${capsula.debian.packageName} (${version.version}-${version.release}) ${capsula.debian.distribution}; urgency=${capsula.debian.urgency}

<#list version.changes as change>
  * ${change}
</#list>

 -- ${capsula.maintainer.name} <${capsula.maintainer.email}>  ${version.date?string('EEE, dd MMM yyyy HH:mm:ss ZZZZ')}

</#list>

