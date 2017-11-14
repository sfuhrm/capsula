<#macro relation r>${r.pkg}<#if r.op?has_content> (${r.op.operator} ${r.version})</#if></#macro>
<#macro relations name list><#if list?has_content>${name}: <#list list as rel><@relation r=rel/><#sep>, </#sep></#list>
</#if></#macro>
Source: ${capsula.debian.packageName}
Section: ${capsula.debian.section}
Priority: ${capsula.debian.priority}
Maintainer: ${capsula.maintainer.name} <${capsula.maintainer.email}>
<@relations name="Build-Depends" list=capsula.debian.relationsFor("build_depends")/>
Standards-Version: 3.9.8
Homepage: ${capsula.homepage}

Package: ${capsula.debian.packageName}
Architecture: ${capsula.debian.architecture}
<@relations name="Depends" list=capsula.debian.relationsFor("depends")/>
<@relations name="Recommends" list=capsula.debian.relationsFor("recommends")/>
<@relations name="Suggests" list=capsula.debian.relationsFor("suggests")/>
<@relations name="Conflicts" list=capsula.debian.relationsFor("conflicts")/>
<@relations name="Breaks" list=capsula.debian.relationsFor("breaks")/>
<@relations name="Provides" list=capsula.debian.relationsFor("provides")/>
<@relations name="Replaces" list=capsula.debian.relationsFor("replaces")/>
Description: ${capsula.shortSummary}
<#list capsula.longDescriptionLines as line>
<#if line?has_content>
 ${line}
<#else>
 .
</#if>
</#list>
