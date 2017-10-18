Source: ${capsula.debian.packageName}
Section: ${capsula.debian.section}
Priority: ${capsula.debian.priority}
Maintainer: ${capsula.maintainer.name} <${capsula.maintainer.email}>
Build-Depends: debhelper (>=10)
Standards-Version: 4.0.0
Homepage: ${capsula.homepage}

Package: ${capsula.debian.packageName}
Architecture: any
Depends: ${r"${shlibs:Depends}"}, ${r"${misc:Depends}"}
Description: <insert up to 60 chars description>
 <insert long description, indented with spaces>

