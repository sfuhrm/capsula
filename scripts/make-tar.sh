#! /bin/bash
#
# Make tar archive of a certain version.
# Call it like this:
# make-tar.sh 1.0.0-SNAPSHOT
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
TAR=/tmp/${PROJECT}-${VERSION}.tar
TARGZ=${TAR}.gz
SIGN=${TARGZ}.asc

rm -f ${TAR} ${TARGZ} ${SIGN}

git archive --format=tar --prefix=${PROJECT}-${VERSION}/ -o ${TAR} HEAD || exit
gzip -9 ${TAR} || exit

gpg --sign --detach-sign --armor --local-user ${KEYID} ${TARGZ} || exit

ls -al ${TARGZ} ${SIGN}
