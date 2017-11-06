#! /bin/bash
#
# Sets the version of the current software at multiple places.
# Call it like this:
# set-version.sh 1.0.0-SNAPSHOT
#
# (c) 2017 Stephan Fuhrmann

NEWVERSION=$1

if [ "x$NEWVERSION" = "x" ]; then
	echo "Please give a version as a parameter, for example 0.1.4-SNAPSHOT"
	exit 10
fi

ROOT=$PWD
TMP=/tmp/set-version.$$

echo "- .travis.yml"
sed -e"s/\(.* VERSION: \).*/\1$NEWVERSION/" < $ROOT/.travis.yml > $TMP || exit
cp $TMP $ROOT/.travis.yml || exit

echo "- pom.xml"
mvn versions:set -DnewVersion=$NEWVERSION || exit

echo "- capsula.yaml"
sed -e"s#\(.*from: target/capsula-\).*\(-jar-with-dependencies.jar.*\)#\1$NEWVERSION\2#" < $ROOT/capsula.yaml > $TMP || exit
cp $TMP $ROOT/capsula.yaml || exit

rm $TMP

