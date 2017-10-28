<#list capsula.install as entry>
<#if entry.copy?has_content>
${entry.copy.from} ${entry.copy.to}
</#if>
</#list>

