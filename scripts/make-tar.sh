#! /bin/bash
#
# Make tar archive of a certain version.
# Call it like this:
# set-version.sh 1.0.0-SNAPSHOT
#
# (c) 2017 Stephan Fuhrmann

VERSION=$1
PROJECT=capsula
KEYID=0AC5A45E91FA93DA25380017B0D87B063EAD41F1

if [ "x${VERSION}" = "x" ]; then
	echo "Please give a version as a parameter, for example 0.1.4-SNAPSHOT"
	exit 10
fi

ROOT=${PWD}
TMP=/tmp/${PROJECT}-${VERSION}

mkdir -p ${TMP}
cp -ar * ${TMP}
rm -fr ${TMP}/.git*
rm -fr ${TMP}/target
rm -fr ${TMP}/build
rm -fr ${TMP}/out
rm -f ${TMP}/*.iml
rm -f ${TMP}/nb*.xml
rm -f ${TMP}/pom.xml.versionsBackup

cd /tmp; tar -czvf /tmp/${PROJECT}-${VERSION}.tar.gz ${PROJECT}-${VERSION}
gpg --sign --detach-sign --armor --local-user ${KEYID} /tmp/${PROJECT}-${VERSION}.tar.gz

rm -f ${TMP}

