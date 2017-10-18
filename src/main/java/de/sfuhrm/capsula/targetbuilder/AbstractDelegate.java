/*
 * Copyright 2017 Stephan Fuhrmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
