#
# Debian9 + Packaging tools
#
FROM debian:stretch

RUN apt-get update && apt-get install --yes \
build-essential fakeroot devscripts \
openjdk-8-jdk-headless:amd64 maven

WORKDIR /

CMD bash

