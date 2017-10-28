Summary: ${capsula.shortSummary}
Name: ${capsula.redhat.packageName}
Version: ${version.version}
Release: ${version.releaseNumber}
License: ${capsula.license}
Group: Utilities/System
Source: ${capsula.redhat.packageName}-${version.version}.tar.gz
Packager: ${capsula.maintainer.name} <${capsula.maintainer.email}>
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