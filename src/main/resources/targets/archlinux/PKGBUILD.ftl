<#macro relation r>${r.pkg}<#if r.op?has_content>${r.op.operator}${r.version}</#if></#macro>
<#macro relations name list><#if list?has_content>${name}=(<#list list as rel><@relation r=rel/><#sep> </#sep></#list>)
</#if></#macro>
# Maintainer: ${capsula.maintainer.name} <${capsula.maintainer.email}>
pkgname=${capsula.archlinux.packageName}
pkgver=${version.version}
pkgrel=${version.releaseNumber}
epoch=
pkgdesc="${capsula.shortSummary}"
arch=('any')
url="${capsula.homepage}"
license=('GPL')
groups=()
<@relations name="depends" list=capsula.archlinux.relationsFor("depends")/>
<@relations name="makedepends" list=capsula.archlinux.relationsFor("makedepends")/>
<@relations name="provides" list=capsula.archlinux.relationsFor("provides")/>
<@relations name="conflicts" list=capsula.archlinux.relationsFor("conflicts")/>
<@relations name="replaces" list=capsula.archlinux.relationsFor("replaces")/>
install=
changelog=
source=('${capsula.archlinux.packageName}::git+${capsula.git.gitUrl}')
noextract=()
md5sums=()
validpgpkeys=()

prepare() {
	cd "$pkgname-$pkgver"
	patch -p1 -i "$srcdir/$pkgname-$pkgver.patch"
}

build() {
	cd "$pkgname-$pkgver"
	./configure --prefix=/usr
	make
}

check() {
	cd "$pkgname-$pkgver"
	make -k check
}

package() {
	cd "$pkgname-$pkgver"
	make DESTDIR="$pkgdir/" install
}
