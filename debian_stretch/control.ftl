Source: ${jpkg.debian.packageName}
Section: ${jpkg.debian.section}
Priority: ${jpkg.debian.priority}
Maintainer: ${jpkg.maintainer.name} <${jpkg.maintainer.email}>
Build-Depends: debhelper (>=10)
Standards-Version: 4.0.0
Homepage: ${jpkg.homepage}

Package: ${jpkg.debian.packageName}
Architecture: any
Depends: ${r"${shlibs:Depends}"}, ${r"${misc:Depends}"}
Description: <insert up to 60 chars description>
 <insert long description, indented with spaces>

