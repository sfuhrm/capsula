#! /bin/bash
pacman -Sy --noconfirm || exit
pacman -S --noconfirm  <#list capsula.archlinux.relationsFor("depends") as rel>${rel.pkg}<#sep> </#sep></#list> || exit
su capsula -c "makepkg -g >> PKGBUILD" || exit
su capsula -c makepkg || exit
