#! /bin/bash
pacman -Sy --noconfirm || exit 1
# TBD is here really depends needed? Looks more like build_depends
pacman -S --noconfirm  <#list capsula.archlinux.relationsFor("depends") as rel>${rel.pkg}<#sep> </#sep></#list> || exit 1
su capsula -c "makepkg -g >> PKGBUILD" || exit 1
su capsula -c makepkg || exit 1
