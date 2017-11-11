# Capsula ![Travis CI Status](https://travis-ci.org/sfuhrm/capsula.svg?branch=master) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/ed538897b79543f1a4f933b2347fd7e5)](https://www.codacy.com/app/sfuhrm/capsula?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=sfuhrm/capsula&amp;utm_campaign=Badge_Grade) [![License: GPL v2](https://img.shields.io/badge/License-GPL%20v2-blue.svg)](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)

## Capsula - Linux package making tool 

Capsula is a program for creating packages for multiple
Linux platforms from one universal descriptor file. 
The name 'Capsula' is latin for the english word 'capsule'.

Linux platforms supported:

* [Debian](https://www.debian.org/)-derived systems: Debian, Ubuntu, Mint.
* [Fedora](https://www.centos.org/)-derived systems: Fedora, CentOS.
* [Archlinux](https://www.archlinux.org/) systems.
* and many more to come.

### Features

Capsula has the following features:

* Only one YAML configuration file necessary for all supported platforms.
* Can build packages for multiple target platforms.
* Dependencies can be configured for every distribution.
* Can make Java, C and other projects.

### Downloading & installation

The current version can be downloaded for Debian and Fedora derived systems here:

https://github.com/sfuhrm/capsula/releases

### Sample session for building

The following video shows building a Debian and a Fedora package from the examples.

[![Building a tool sample video](https://raw.githubusercontent.com/sfuhrm/capsula/master/images/Youtube.png)](https://www.youtube.com/watch?v=Dbo7BumrZ3A)

### Process overview

The process is described in the following picture:

![Process overview](https://raw.githubusercontent.com/sfuhrm/capsula/master/images/Capsula-Process-SF-1.png "Capsula Process")

The descriptor file is generic. For each target platform, the descriptor is used together with distribution
dependant templates to build a distribution specific package descriptor. The package definition is then
fed to a Docker container which builds the package.

## Requirements

The requirements are:

* [Java 8+](http://www.oracle.com/technetwork/java/index.html) runtime.
* [Docker](https://www.docker.com/) installation. The free Docker CE (Community Edition) is sufficient.
* Git command line client.

Capsula requires a running *Docker* installation on the host. Docker is used for each
platform to create a package. The package creation tools are usually only available on
the given platform itself. This means you'd need to have multiple machines available.
This is why Capsula uses Docker instead.

Capsula is easily extensible because it uses a simple templating approach per platform.

## Project status statement

The project is at the moment **experimental**. 
It can build simple packages for Debian and Fedora like distributions.
The [packages](https://github.com/sfuhrm/capsula/releases) are built with
the software.

The format of the descriptor file **will** change until version v1.0.0.

Releasing the project at this stage is to enable other people
to participate.

## Starting Capsula

If you've installed the Capsula package, you have a capsula command
line tool:

    capsula -f capsula.yaml -o out/

if not, you have to build Capsula and run it like this

    mvn clean package
    java -jar target/capsula-0.0.1-SNAPSHOT-jar-with-dependencies.jar -f capsula.yaml -o out/

## How to edit a configuration

Best choice is at the moment starting with one of the examples
and modifying it to your needs.

The package descriptor editing can be separated in these
phases:

1. Create initial descriptor, for example from an example:
  [1](https://github.com/sfuhrm/capsula/tree/master/examples),
  [2](https://github.com/sfuhrm/capsula/blob/master/capsula.yaml),
  [3](https://github.com/sfuhrm/radiorecorder/blob/master/radiorecorder.yaml),
  [4](https://github.com/sfuhrm/schrumpf/blob/master/schrumpf.yaml).
2. Edit the generic fields.
3. Chose which distributions you want to support.
4. Edit the distribution specific fields. The most difficult part
   is usually the relations/depends to the distribution.
5. Build your packages.
6. Fix build problems.
7. Test packages and go back to 4.

Find out which group your package belongs in Fedora world:

    docker run fedora yum grouplist

## How to test installation of a created package

It is recommended to test the produced package. At least the
relations/dependencies can be wrong and are not checked by the package
building tools.

The package installation test is at the moment manual because it can
be very specific to your package.

You can use Docker for this very easily. Please note that I have replaced
the package name to test with a generic bash expression that expands to a
pattern:

### Debian

For Debian it is a little more difficult because dependencies require a loaded
package index. This can be done in one run:

Latest Debian:

    docker run -v$PWD/out:/out debian /bin/bash -c "apt-get update; apt-get install -y --no-install-recommends /out/$(cd out;ls *_all.deb)"

Debian Jessie is more complicated. First the index needs to be loaded, then the package needs to be installed and then
the unmet dependencies need to be fetched:

    docker run -v$PWD/out:/out debian:8 /bin/bash -c "apt-get update; dpkg -i /out/$(cd out;ls *_all.deb); apt-get install -f --no-install-recommends -y"

### Fedora

    docker run -v$PWD/out:/out fedora yum install -y /out/$(cd out;ls *.noarch.rpm)

## TODOs

The following TODOs are open programming tasks as known for today:

[Github issues for Capsula project](https://github.com/sfuhrm/capsula/issues)

## Milestones

* [Finished] Milestone 1: Make Capsula package itself for Debian Stretch.
* [Finished] Milestone 2: Make Capsula package itself for CentOS 7.
* [Finished] Milestone 3: Package dependencies.
* [Finished] Milestone 4: Move template directories.
* Milestone 5: Archlinux support.

## Related Docker Hub images

The following Docker Hub images are used by this project and built from
the [Github source tree](https://github.com/sfuhrm/capsula):

* [sfuhrm/capsula](https://hub.docker.com/r/sfuhrm/capsula/) contains
multiple images. Each Linux distribution is stored in a
[tag](https://hub.docker.com/r/sfuhrm/capsula/tags/).

## Links

Links that might be useful or not:

### Debian
* [Debian Policy](https://www.debian.org/doc/debian-policy/)
* [Debian Building Tutorial](https://wiki.debian.org/BuildingTutorial#).
* [Debian Package maintainer guide](https://www.debian.org/doc/manuals/maint-guide/first.en.html).
* [Debian Virtual Package names](https://www.debian.org/doc/packaging-manuals/virtual-package-names-list.txt) contains 
  the virtual names for the JDK packages.
* [Pragmatic Debian packaging](https://vincent.bernat.im/en/blog/2016-pragmatic-debian-packaging).

### Fedora
* [RPM Spec file info](http://ftp.rpm.org/max-rpm/s1-rpm-build-creating-spec-file.html).
* [RPM Build Package example](http://www.thegeekstuff.com/2015/02/rpm-build-package-example/).
* [How to create an RPM package](https://fedoraproject.org/wiki/How_to_create_an_RPM_package).

### Others
* [Alpine package](https://wiki.alpinelinux.org/wiki/Creating_an_Alpine_package) guide.
* [Slackware package](https://docs.slackware.com/howtos:slackware_admin:building_a_package) guide.
* [Arch Linux build system](https://wiki.archlinux.org/index.php/Arch_Build_System),
* [Arch Linux Creating Packages](https://wiki.archlinux.org/index.php/creating_packages).
* [Arch Linux PKGBUILD format](https://wiki.archlinux.org/index.php/PKGBUILD)

## License

Copyright 2017 Stephan Fuhrmann

Licensed under the GNU GENERAL PUBLIC LICENSE 2.0.
Please see the licensing conditions under [LICENSE](./LICENSE)
or at [https://www.gnu.org/licenses/gpl-2.0.html](https://www.gnu.org/licenses/gpl-2.0.html).
