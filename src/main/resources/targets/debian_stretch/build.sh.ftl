#! /bin/bash

# update local package cache
apt-get update || exit 1

# install required packages for build
apt-get install --assume-yes  <#list capsula.debian.relationsFor("build_depends") as rel>${rel.pkg}<#sep> </#sep></#list> || exit 1

# build the package
cd /target/${pkgdir}
debuild -us -uc || exit 1

# change the permissions to super open so I can delete the build directories later
chmod -R a+rwx . || exit 1
