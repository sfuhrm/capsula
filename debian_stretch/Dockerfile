#
# Debian9 + Packaging tools
#
FROM debian:stretch

LABEL maintainer="s@sfuhrm.de"

RUN apt-get update && apt-get install --no-install-recommends --yes \
build-essential fakeroot devscripts \
openjdk-8-jdk-headless:amd64 maven && \
rm -rf /var/lib/apt/lists/* && \
rm -rf /var/cache/apt/archives/*

WORKDIR /

CMD bash

