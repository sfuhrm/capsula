<#macro relation r>${r.pkg}<#if r.op?has_content> ${r.op.operator} ${r.version}</#if></#macro>
<#macro relations name list><#if list?has_content>${name}: <#list list as rel><@relation r=rel/><#sep>, </#sep></#list>
</#if></#macro>
<#include "include-install.txt">
Summary: ${capsula.shortSummary}
Name: ${capsula.redhat.packageName}
Version: ${version.version}
Release: ${version.releaseNumber}
License: ${capsula.license}
Group: ${capsula.redhat.group}
Source: ${capsula.redhat.packageName}-${version.version}.tar.gz
Packager: ${capsula.maintainer.name} <${capsula.maintainer.email}>
URL: ${capsula.homepage}
<@relations name="BuildRequires" list=capsula.redhat.relationsFor("build_depends")/>
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
mvn --batch-mode clean package

%install
<#list capsula.install as entry>
<#-- install.mkdir -->
<#if entry.mkdir?has_content>
<@install cmd=entry.mkdir arguments="-d %{buildroot}/${entry.mkdir.to}"/>
</#if>
<#-- install.copy -->
<#if entry.copy?has_content>
<@install cmd=entry.copy arguments="-D ${entry.copy.from} %{buildroot}/${entry.copy.to}"/>
</#if>
<#-- install.run -->
<#if entry.run?has_content>
${entry.run.command}
</#if>
</#list>

%files
<#list capsula.install as entry>
<#if entry.copy?has_content>
${entry.copy.to}
</#if>
</#list>

<#-- https://docs-old.fedoraproject.org/en-US/Fedora_Draft_Documentation/0.1/html/Packagers_Guide/chap-Packagers_Guide-Spec_File_Reference-Preamble.html -->
%changelog
<#list capsula.versions as version>
* ${version.date?string('EEE MMM dd yyyy')} ${capsula.maintainer.name} <${capsula.maintainer.email}> (${version.version}-${version.releaseNumber})
<#list version.changes as change>
- ${change}
</#list>
</#list>
