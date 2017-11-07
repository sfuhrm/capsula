<#include "include-install.txt">
<#macro relation r>${r.pkg}<#if r.op?has_content>${r.op.operator}${r.version}</#if></#macro>
<#macro relations name list><#if list?has_content>${name}=(<#list list as rel><@relation r=rel/><#sep> </#sep></#list>)
</#if></#macro>
# See https://wiki.archlinux.org/index.php/VCS_package_guidelines
# Maintainer: ${capsula.maintainer.name} <${capsula.maintainer.email}>
pkgname=${capsula.archlinux.packageName}
pkgver=${version.version}
pkgrel=${version.release}
epoch=
pkgdesc="${capsula.shortSummary}"
arch=('${capsula.archlinux.architecture}')
url="${capsula.homepage}"
license=('${capsula.license.archlinuxName}')
groups=()
<#-- TBD: check relations -->
<@relations name="depends" list=capsula.archlinux.relationsFor("depends")/>
<@relations name="makedepends" list=capsula.archlinux.relationsFor("makedepends")/>
<@relations name="provides" list=capsula.archlinux.relationsFor("provides")/>
<@relations name="conflicts" list=capsula.archlinux.relationsFor("conflicts")/>
<@relations name="replaces" list=capsula.archlinux.relationsFor("replaces")/>
install=
changelog=
source=('${capsula.archlinux.packageName}-${version.version}.tar.gz')
noextract=()
validpgpkeys=()

prepare() {
	cd "$pkgname-$pkgver"
}

build() {
	cd "$pkgname-$pkgver"
	mvn --batch-mode clean package
}

check() {
	cd "$pkgname-$pkgver"
}

package() {
	cd "$pkgname-$pkgver"
<#list capsula.install as entry>
<#-- install.mkdir -->
<#if entry.mkdir?has_content>
${"\t"}<@install cmd=entry.mkdir arguments="-d $pkgdir/${entry.mkdir.to}"/>
</#if>
<#-- install.copy -->
<#if entry.copy?has_content>
${"\t"}<@install cmd=entry.copy arguments="-D ${entry.copy.from} $pkgdir/${entry.copy.to}"/>
</#if>
<#-- install.run -->
<#if entry.run?has_content>
${"\t"}${entry.run.command}
</#if>
</#list>

}
