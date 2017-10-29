<#macro relation r>${r.pkg}<#if r.op?has_content> ${r.op.operator} ${r.version}</#if></#macro>
<#macro relations name list><#if list?has_content>${name}: <#list list as rel><@relation r=rel/><#sep>, </#sep></#list>
</#if></#macro>
Summary: ${capsula.shortSummary}
Name: ${capsula.redhat.packageName}
Version: ${version.version}
Release: ${version.releaseNumber}
License: ${capsula.license}
Group: Utilities/System
Source: ${capsula.redhat.packageName}-${version.version}.tar.gz
Packager: ${capsula.maintainer.name} <${capsula.maintainer.email}>
<@relations name="Requires" list=capsula.redhat.relationsFor("depends")/>
<@relations name="Recommends" list=capsula.redhat.relationsFor("recommends")/>
<@relations name="Suggests" list=capsula.redhat.relationsFor("suggests")/>
<@relations name="Conflicts" list=capsula.redhat.relationsFor("conflicts")/>
<@relations name="Provides" list=capsula.redhat.relationsFor("provides")/>
BuildArch: noarch
%description
<#list capsula.longDescription as line>
${line}
</#list>

%prep
%setup

%build
mvn clean package

%install
<#list capsula.install as entry>
<#if entry.copy.mode?has_content>
<#assign modeStatement = "--mode="+entry.copy.octal+" ">
<#else>
<#assign modeStatement = "">
</#if>
<#if entry.copy.owner?has_content>
<#assign ownerStatement = "--owner="+entry.copy.owner+" ">
<#else>
<#assign ownerStatement = "">
</#if>
<#if entry.copy.group?has_content>
<#assign groupStatement = "--group="+entry.copy.group+" ">
<#else>
<#assign groupStatement = "">
</#if>
<#if entry.copy?has_content>
if [ -f ${entry.copy.from} ]; then install ${modeStatement} ${ownerStatement} ${groupStatement} -D ${entry.copy.from} %{buildroot}/${entry.copy.to}; fi
if [ -d ${entry.copy.from} ]; then install ${modeStatement} ${ownerStatement} ${groupStatement} -d ${entry.copy.from} %{buildroot}/${entry.copy.to}; fi
</#if>
</#list>

%files
<#list capsula.install as entry>
${entry.copy.to}
</#list>
