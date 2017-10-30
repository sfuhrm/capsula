# Capsula ![Travis CI Status](https://travis-ci.org/sfuhrm/capsula.svg?branch=master) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/ed538897b79543f1a4f933b2347fd7e5)](https://www.codacy.com/app/sfuhrm/capsula?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=sfuhrm/capsula&amp;utm_campaign=Badge_Grade) [![License: GPL v2](https://img.shields.io/badge/License-GPL%20v2-blue.svg)](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)
## Java Linux package making tool 

Capsula is a program for creating packages for multiple
Linux platforms from one universal descriptor file. 
The name 'Capsula' is latin for the english word 'capsule'.

Linux platforms supported:

* [Debian](https://www.debian.org/)
* [CentOS](https://www.centos.org/)
* and many more

### The Capsula process

The process is described in the following picture:

![Capsula Process](https://raw.githubusercontent.com/sfuhrm/capsula/master/images/Capsula-Process-SF-1.png "Capsula Process")

The descriptor file is generic. For each target platform, the descriptor is used together with distribution
dependant templates to build a distribution specific package descriptor. The package definition is then
fed to a Docker container which builds the package.

## Requirements

The requirements are:

* [Java 8+](http://www.oracle.com/technetwork/java/index.html) runtime
* [Docker](https://www.docker.com/) installation
* Git command line client

Capsula requires a running *Docker* installation on the host. Docker is used for each
platform to create a package. The package creation tools are usually only available on
the given platform itself. This means you'd need to have multiple machines available.
This is why Capsula uses Docker instead.

Capsula is easily extensible because it uses a simple templating approach per platform.

## Project status statement

The project is at the moment **experimental**. 
It is **not** recommended to use it if you're not sure what
you're doing.

Releasing the project at this stage is to enable other people
to participate.

## Features

Capsula has the following features:

* Only one YAML configuration file necessary.
* Can build packages for multiple target platforms.
* Dependencies can be configured for every distribution.

## Starting Capsula

If you've installed the Capsula package, you have a capsula command
line tool:

    capsula -f capsula.yaml -o out/

if not, you have to build Capsula and run it like this

    mvn clean package
    java -jar target/capsula-0.0.1-SNAPSHOT-jar-with-dependencies.jar -f capsula.yaml -o out/

## How to test installation of a created package

It is recommended to test the produced package. At least the
relations/dependencies can be wrong and are not checked by the package
building tools.

The package installation test is at the moment manual because it can
be very specific to your package.

You can use Docker for this very easily. Please note that I have replaced
the package name to test with a generic bash expression that expands to a
pattern:

### Fedora

    docker run -v$PWD/out:/out fedora yum install -y /out/$(cd out;ls *.noarch.rpm)

### Debian

For Debian it is a little more difficult because dependencies require a loaded
package index. This can be done in one run:

Latest Debian:

    docker run -v$PWD/out:/out debian /bin/bash -c "apt-get update; apt-get install /out/$(cd out;ls *_all.deb)"

Debian Jessie is more complicated. First the index needs to be loaded, then the package needs to be installed and then
the unmet dependencies need to be fetched:

    docker run -v$PWD/out:/out debian:8 /bin/bash -c "apt-get update; dpkg -i /out/$(cd out;ls *_all.deb); apt-get install -f --no-install-recommends -y"

## TODOs

The following TODOs are open programming tasks as known for today:

* Proper log4j config for packages.
* Documentation for usage.
* Documentation for YAML format.
* Could somehow tag which Git commit was used for building.
* Reusing of users .m2 directory for speeding up maven runs a lot.
* Stop at certain stages for debugging, for example after template generation.
* Add support for Gradle, Ant (?) besides Maven.
* Test installation of packages on each platform. This is a little difficult
  because a package could require other apt sources (like capsula does for docker).
* Scaffolding of the YAML configuration for your program.
* YAML scaffolding using an existing pom file.
* Support app icons.
* Testing.
* Support for different JDK/JRE depencendies. At the moment this can be done using the relations.

## Milestones

* [Finished] Milestone 1: Make Capsula package itself for Debian Stretch.
* [Finished] Milestone 2: Make Capsula package itself for CentOS 7.
* [Finished] Milestone 3: Package dependencies.
* [Finished] Milestone 4: Move template directories.
* Milestone 5: TBD.

## Links

Links that might be useful or not:

* [Debian Package maintainer guide](https://www.debian.org/doc/manuals/maint-guide/first.en.html) describes how to create
  packages. A short packaging tutorial is [here](https://vincent.bernat.im/en/blog/2016-pragmatic-debian-packaging).
* [RPM Spec file info](http://ftp.rpm.org/max-rpm/s1-rpm-build-creating-spec-file.html) and [RPM Package guide](http://www.thegeekstuff.com/2015/02/rpm-build-package-example/) is for CentOS.
* [Alpine package](https://wiki.alpinelinux.org/wiki/Creating_an_Alpine_package) guide.
* [Slackware package](https://docs.slackware.com/howtos:slackware_admin:building_a_package) guide.

## License

Copyright 2017 Stephan Fuhrmann

Licensed under the GNU GENERAL PUBLIC LICENSE 2.0.
Please see the licensing conditions under [LICENSE](./LICENSE)
or at [https://www.gnu.org/licenses/gpl-2.0.html](https://www.gnu.org/licenses/gpl-2.0.html).
