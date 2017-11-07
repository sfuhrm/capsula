#!/usr/bin/make -f

%:
	dh $@

override_dh_auto_clean:
	mvn --batch-mode clean

override_dh_auto_build:
	mvn --batch-mode clean package

