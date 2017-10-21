#!/usr/bin/make -f

%:
	dh $@

override_dh_auto_clean:
	mvn clean

override_dh_auto_build:
	mvn clean package

