#! /bin/bash

yum install --assumeyes  <#list capsula.redhat.relationsFor("build_depends") as rel>${rel.pkg}<#sep> </#sep></#list> || exit 1

# rpmbuild will complain if the archive has a unmappable UID. It was created
# on the host, not the container.
chown root:root /target/${capsula.redhat.packageName}-${version.version}.spec /target/root/rpmbuild/SOURCES/${capsula.redhat.packageName}-${version.version}.tar.gz || exit 1

# build the package
rpmbuild -ba ${capsula.redhat.packageName}-${version.version}.spec || exit 1

# change the permissions to super open so I can delete the build directories later
chmod -R a+rwx . || exit 1
