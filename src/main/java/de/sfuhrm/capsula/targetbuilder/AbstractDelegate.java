/*
 * Copyright (C) 2017 Stephan Fuhrmann
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package de.sfuhrm.capsula.targetbuilder;

import de.sfuhrm.capsula.yaml.command.TargetCommand;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Stephan Fuhrmann
 */
@Slf4j
public class AbstractDelegate {
    @Getter(AccessLevel.PROTECTED)
    private final TargetBuilder targetBuilder;

    public AbstractDelegate(TargetBuilder targetBuilder) {
        this.targetBuilder = Objects.requireNonNull(targetBuilder);
    }
    
    protected void applyTargetFileModifications(final Path toPath, final TargetCommand command) throws IOException {
        FileSystem fileSystem = toPath.getFileSystem();
        UserPrincipalLookupService lookupService
                = fileSystem.getUserPrincipalLookupService();

        if (command.getOwner() != null) {
            log.debug("Setting owner of {} to {}", toPath, command.getOwner());
            UserPrincipal owner = lookupService.lookupPrincipalByName(command.getOwner());
            Files.setOwner(toPath, owner);
        }

        if (command.getGroup() != null) {
            log.debug("Setting group of {} to {}", toPath, command.getOwner());
            GroupPrincipal group = lookupService.lookupPrincipalByGroupName(command.getGroup());
            Files.getFileAttributeView(toPath, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS).setGroup(group);
        }

        if (command.getMode() != null) {
            log.debug("Setting mode of {} to {}", toPath, command.getMode());

            Set<PosixFilePermission> permissions = PosixFilePermissions.fromString(command.getMode());
            Files.getFileAttributeView(toPath, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS).setPermissions(permissions);
        }
    }
    
    protected void applyTargetFileModifications(TargetCommand command) throws IOException {
        Path toPath = targetBuilder.getTargetPath().resolve(command.getTo());
        applyTargetFileModifications(toPath, command);
    }

}
