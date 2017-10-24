# Capsula ![Travis CI Status](https://travis-ci.org/sfuhrm/capsula.svg?branch=master) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/ed538897b79543f1a4f933b2347fd7e5)](https://www.codacy.com/app/sfuhrm/capsula?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=sfuhrm/capsula&amp;utm_campaign=Badge_Grade) [![License: GPL v2](https://img.shields.io/badge/License-GPL%20v2-blue.svg)](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)
## Java Linux package making tool 

Capsula is a program for creating packages for multiple
Linux platforms from one universal descriptor file. 
The name 'Capsula' is latin for the english word 'capsule'.

Linux platforms supported:

* [Debian](https://www.debian.org/)
* [CentOS](https://www.centos.org/)
* and many more

## Requirements

The requirements are:

* [Java 8+](http://www.oracle.com/technetwork/java/index.html) runtime
* [Docker](https://www.docker.com/) installation

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

## TODOs

The following TODOs are open programming tasks as known for today:

* Stop at certain stages for debugging, for example after template generation.
* Proper package copying back mechanism (.deb file to output dir).
* Support for different JDK/JRE depencendies.
* Add support for Gradle, Ant (?) besides Maven.
* Proper environment mechanism. The current one sucks. 
  Idea: environment.yaml file.
* Test installation of packages on each platform.
* Scaffolding of the YAML configuration for your program.
* Support app icons.
* Move the template directories somewhere where it makes sense.
* Testing.

## Milestones

* Milestone 1: Make Capsula package itself for Debian Stretch.
* Milestone 2: Make Capsula package itself for CentOS 7.

## Links

Links that might be useful or not:

* [Debian Package maintainer guide](https://www.debian.org/doc/manuals/maint-guide/first.en.html) describes how to create
  packages. A short packaging tutorial is [here](https://vincent.bernat.im/en/blog/2016-pragmatic-debian-packaging).
* [RPM Package guide](http://www.thegeekstuff.com/2015/02/rpm-build-package-example/) is for CentOS.
* [Alpine package](https://wiki.alpinelinux.org/wiki/Creating_an_Alpine_package) guide.

## License

Copyright 2017 Stephan Fuhrmann

Licensed under the GNU GENERAL PUBLIC LICENSE 2.0.
Please see the licensing conditions under [LICENSE](./LICENSE)
or at [https://www.gnu.org/licenses/gpl-2.0.html](https://www.gnu.org/licenses/gpl-2.0.html).
