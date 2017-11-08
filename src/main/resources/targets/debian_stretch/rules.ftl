#!/usr/bin/make -f

%:
	dh $@

override_dh_auto_clean:
${"\t"}${capsula.debian.cleanCommand}

override_dh_auto_build:
${"\t"}${capsula.debian.buildCommand}

